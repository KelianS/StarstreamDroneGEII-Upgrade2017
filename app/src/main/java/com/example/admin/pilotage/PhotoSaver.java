package com.example.admin.pilotage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;

import io.vov.vitamio.MediaPlayer;


import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;

/**
 * Handles video recording and image capturing.
 * <p>This class is independent of VideoManager and works differently.</p>
 * @see VideoManager
 * @author Jules Simon
 */
public class PhotoSaver {



    private GraphicOverlay mGraphicOverlay;
    private SurfaceView mPreview;
    public  Canvas cv;

    private CameraSource mCameraSource = null;
    boolean bStart;




    // Variables used to save the picture
    /**
     * Base name used to name all the pictures, with values of the day/month/year
     */
    String filename;
    /**
     * Final name of the picture.
     * It is obtained by adding the seconds/minutes/hours of the moment of the capture to filename.
     * @see PhotoSaver#filename
     */
    String finalname;
    Bitmap image;
    /**
     * Date used to name the files
     */
    Calendar rightNow;
    MediaPlayer mMediaPlayer;
    Context context;
    String imgname;

    // Variables used to SavePicture the stream
    /**
     * This is the stream that will be recorded.
     */
    DataInputStream in;
    /**
     * This is the file in which the stream will be recorded
     */
    FileOutputStream VideoFile;
    int len;
    byte Buffer[] = new byte[8192];

    /**
     * This url is set to the adress of the live video feed.
     * @see PhotoSaver#path
     */
    URL url;
    URLConnection urlConnection;
    Socket feed_socket;
    Thread tRecordFeed;

    RecordFeed mRecordFeed;
    Processing mProcessor;
    byte Pattern[] = {0x50, 0x61, 0x56, 0x45}; // PaVE en hexa

    String path = "tcp://192.168.1.1:5555/";

    /**
     * Initializes the filename used to save the pictures. Initializes the objects used for video recording.
     * @param c Used to display toasts
     * @param m Mediaplayer from which the frames are saved.
     */
    public PhotoSaver(Context c, MediaPlayer m, GraphicOverlay mGraphicOverlay, SurfaceView View) {
        this.context = c;
        this.mMediaPlayer = m;
        this.mGraphicOverlay =mGraphicOverlay;
        this.mPreview = View;
        this.bStart = false;
        new GraphicFaceTracker(mGraphicOverlay);

       /* mPreview = new SurfaceView(context);
        mPreview.getHolder().addCallback(new SurfaceCallback());
        addView(mPreview);*/

        rightNow = Calendar.getInstance();
        filename = rightNow.get(Calendar.DAY_OF_MONTH) + "_" + (rightNow.get(Calendar.MONTH) + 1) + "_" + rightNow.get(Calendar.YEAR) + ".jpeg";


        // Seting up the connection in order to SavePicture the live video feed.
        try {
            url = new URL(path);
            urlConnection = url.openConnection();
            createCameraSource();
        } catch (IOException e) {

        }
        // Used to SavePicture and process the live feed.
        mProcessor = new Processing();
        mRecordFeed = new RecordFeed();
        new Thread(new UpdateVideo()).start();
    }

    class UpdateVideo implements Runnable {
        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (bStart) {

                    mPreview.buildDrawingCache();
                    Bitmap bmp = mPreview.getDrawingCache();
                    if (bmp != null) {
                        Log.i("BITMAP", "Bitmap");
                        cv = new Canvas(bmp);
                        //mGraphicOverlay.clear();
                        mGraphicOverlay.draw(cv);
                    } else Log.i("BITMAP", "Bitmap failed");

                }
            }
        }
    }

    /**
     * Draws the overlay with its associated graphic objects.
     */



    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = this.context;
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
            Log.w("FACE", "Face detector dependencies are not yet available.");
        }

       /* mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build();*/
    }


    /**
     * Gets the current frame of the MediaPlayer and saves it in the local storage of the phone at
     * the PNG format.
     */
    public void SavePicture() {
        if (Environment.getExternalStorageState() != null) {
           // try {
            this.bStart = !this.bStart;
                    /*mMediaPlayer.setUseCache(true);
                    mMediaPlayer.setCacheDirectory("./cache");
                    Bitmap bmp = mMediaPlayer.getCurrentFrame();

                    if(bmp!=null) Log.i("BITMAP","Bitmap");
                    else Log.i("BITMAP","No Bitmap");*/



              /*  FileOutputStream fos = new FileOutputStream(picture);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                Toast.makeText(context, "Picture saved:" + imgname, Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                Toast.makeText(context, "File error", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(context, "Failed to close", Toast.LENGTH_SHORT).show();
            }*/
        } else {
            Toast.makeText(context, "Directory unavailable", Toast.LENGTH_SHORT).show();
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

    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {

            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
            Log.i("Face","Create Tracker");
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
            Log.i("Face","New Face");
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {

            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
            Log.i("Face","Update");
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
            Log.i("Face","Lost Face");
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
            Log.i("Face","Done");
        }
    }





















    /**
     * Initializes the file which will be used to save the frame. Called in the SavePicture() method.
     *
     * @return the file to be used.
     */
    private File getOutputMediaFile() {

        rightNow = Calendar.getInstance();
        finalname = "DronePicture_" + rightNow.get(Calendar.HOUR) + ":" + rightNow.get(Calendar.MINUTE) + ":" + rightNow.get(Calendar.SECOND) + "_" + filename;
        //Create a media file name
        File mediaFile;
        imgname = Environment.getExternalStorageDirectory() + "/Pictures/" + finalname;
        mediaFile = new File(imgname);
        return mediaFile;
    }

    /**
     * Starts the video recording thread.
     */
    void RecordVideo(){
        tRecordFeed = new Thread(new RecordFeed());
        tRecordFeed.start();
    }

    /**
     * Stops the video recording.
     */
    void StopRecord(){
        mRecordFeed.Flag(true);
    }

    /**
     * Makes a copy of the video file without the PaVE header.
     */
    void StreamCopy_No_PaVE(){
        try{
            // Reading the original file
            InputStream in = new FileInputStream(Environment.getExternalStorageDirectory() + "/Pictures/" + "video.mp4");
            // Creating the output file
            OutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Pictures/" + "videocopy.mp4");
            // Size of the bytes read from the InputStream
            int length = 0;
            // Buffer used to scan through the list and gather the index of each PaVE header
            byte Tampon_Liste[] = new byte[8192];
            // Buffer used to go through the list
            byte Tampon_Sup[] = new byte[1];
            // Used to keep track of all the bytes read in total
            Integer bytes_lus = 0;
            int i = 0;
            // Used to check the amount of bytes skipped when calling the skip() method
            long bytes_skipped = 0;
            // Value of the header list's last index
            int LastIndex_Totaux = 0;

            Integer tmp = 0;
            Integer Tmp2 = 0;

            ArrayList<Integer> ListeHeader_Tampon = new ArrayList<>();
            ArrayList<Integer> ListeHeader_Totaux = new ArrayList<>();

            // Boucle de lecture des headers
            while((length=in.read(Tampon_Liste)) != -1){

                ListeHeader_Tampon = mProcessor.indexOf_bufferedData(Tampon_Liste, Pattern, bytes_lus);
                for (i = 0; i < ListeHeader_Tampon.size(); i++) {
                    tmp = (ListeHeader_Tampon.get(i));
                    ListeHeader_Totaux.add(tmp);
                }

                bytes_lus = length + bytes_lus;
            }

            LastIndex_Totaux = ListeHeader_Totaux.size()-1;
            // Fermeture du fichier
            in.close();

            bytes_lus = 0;
            in = new FileInputStream(Environment.getExternalStorageDirectory() + "/Pictures/" + "video.mp4");
            i = 0;

            // Recherche des headers et recopiage du fichier
            while((length=in.read(Tampon_Sup)) != -1){

                Tmp2 = ListeHeader_Totaux.get(i);

                if((bytes_lus.equals(Tmp2))&& (i<LastIndex_Totaux)){
                    Log.v("Header trouve:", "-----------" + ListeHeader_Totaux.get(i));
                    bytes_skipped = in.skip(63);
                    i++;
                    Log.v("PhotoSaver", "Bytes skipped : " + bytes_skipped);
                    bytes_lus = bytes_lus + (int)bytes_skipped;
                }
                else {
                    out.write(Tampon_Sup, 0, length);
                }

                bytes_lus = length + bytes_lus;

            }

            out.close();
            in.close();

            Log.v("PhotoSaver", "Copie terminee");
        }catch(FileNotFoundException fn){
            Log.v("PhotoSaver", "Copie : file not found");
        }catch (IOException io){
            Log.v("PhotoSaver", "Copie : io exception");
        }

    }

    /**
     * Video recording thread
     */
    class RecordFeed implements Runnable{

        public boolean bStop;

        public void run(){
            try{
                bStop = false;
                // Connection au drone
                feed_socket = new Socket("192.168.1.1",5555);
                Log.v("PhotoSaver", "Socket cree");

                if(feed_socket.isConnected()){
                    Log.v("PhotoSaver", "Socket connecte");
                }
                // Lecture du flux
                in = new DataInputStream(feed_socket.getInputStream());
                // Ecriture du flux
                VideoFile = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Pictures/" + "video.mp4");
                Log.v("PhotoSaver", "Démarrrage enregistrement");

                while((bStop == false)){
                    len = in.read(Buffer);
                    VideoFile.write(Buffer,0,len);
                }

                VideoFile.close();
                Log.v("PhotoSaver", "Fin enregistrement");
                VideoFile.close();
            }catch (IOException io){
                Log.v("PhotoSaver", "IO exeption enregistrement");
            }
        }

        public void Flag(boolean bFlag){
            bStop = bFlag;
        }
    }

}