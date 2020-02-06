package com.example.admin.pilotage;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;

import java.io.IOException;

import android.graphics.Canvas;

import android.graphics.Paint;
import android.graphics.PointF;

import android.os.AsyncTask;

import java.net.Socket;




/**
 * Allows real time displaying of the video stream from the drone
 * Requires the library vitamio in order to handle the live stream.
 * @author Jules Simon
 */
public class VideoManager {

    DisplayMetrics metrics;
    String path ="tcp://192.168.1.1:5555/";

    public static String SERVERIP = "192.168.1.1";
    public static final int SERVERPORT = 5555;
    private TextView mStatus;
    private ImageView mCameraView;
    public MyClientThread mClient;
    public Bitmap mLastFrame;

    private int face_count;
    private final Handler handler = new MyHandler(this);


    private Activity mMainActivity;

    public static final String TAG = "FaceTracker";

    public CameraSource mCameraSource = null;

    public CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    public static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    public static final int RC_HANDLE_CAMERA_PERM = 2;




    private Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mLastFrame!=null){
                            Log.i("k","Received Frame");
                            Bitmap mutableBitmap = mLastFrame.copy(Bitmap.Config.RGB_565, true);

                            Canvas myCanvas = new Canvas(mutableBitmap);
                            mPreview.mSurfaceView.draw(myCanvas);
                        }

                    }
                }); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                handler.postDelayed(mStatusChecker, 1000/15);
            }
        }
    };




    /**
     * Constructor
     * @param mMainActivity Used to check the librairy
     * @param mPreview Surface on which the video will be displayed
     * @param mMetrics Used to get info about the smartphone's screen
     */
    public VideoManager(Activity mMainActivity, CameraSourcePreview mPreview,GraphicOverlay mGraphicOverlay, DisplayMetrics mMetrics){

        this.mPreview = mPreview;
        this.mGraphicOverlay = mGraphicOverlay;
        this.mMainActivity = mMainActivity;
        int rc = ActivityCompat.checkSelfPermission(mMainActivity, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }


        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... unused) {
                // Background Code
                Socket s;
                try {
                    s = new Socket(SERVERIP, SERVERPORT);
                    mClient = new MyClientThread(s, handler);
                    new Thread(mClient).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute();
        mStatusChecker.run();

       metrics = mMetrics;
    }


    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    public void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(mMainActivity,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(mMainActivity, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = mMainActivity;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    public void createCameraSource() {

        Context context = mMainActivity.getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }


        /*
        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(620, 480)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build();
        */
    }



    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    public void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                mMainActivity.getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(mMainActivity, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }


}
