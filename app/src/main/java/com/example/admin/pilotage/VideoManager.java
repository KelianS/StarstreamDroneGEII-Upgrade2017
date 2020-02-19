package com.example.admin.pilotage;


import android.app.Activity;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
<<<<<<< HEAD
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.video.ARDrone;
import com.example.admin.video.VideoReader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

import android.graphics.Canvas;

import android.graphics.Paint;
import android.graphics.PointF;

import android.os.AsyncTask;

import java.net.InetAddress;
import java.net.Socket;



=======
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import io.vov.vitamio.MediaPlayer;
>>>>>>> parent of eaf636d... try manual frame reception but no decoding

/**
 * Allows real time displaying of the video stream from the drone
 * Requires the library vitamio in order to handle the live stream.
 * @author Jules Simon
 */
public class VideoManager implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener {

    SurfaceHolder holder;
    MediaPlayer mMediaPlayer;
    DisplayMetrics metrics;
    String path ="tcp://192.168.1.1:5555/";

<<<<<<< HEAD
    public static String SERVERIP = "192.168.1.1";
    public static final int SERVERPORT = 5555;

    private TextView mStatus;
    private ImageView mCameraView;
    public MyClientThread mClient;
    public Bitmap mLastFrame;

    VideoReader video;

    private int face_count;
    private final Handler handler = new MyHandler(this);

    private Activity mMainActivity;


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
                            mCameraView.draw(myCanvas);

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



=======
>>>>>>> parent of eaf636d... try manual frame reception but no decoding
    /**
     * Constructor
     * @param mMainActivity Used to check the librairy
     * @param mPreview Surface on which the video will be displayed
     * @param mMetrics Used to get info about the smartphone's screen
     */
<<<<<<< HEAD
    public VideoManager(Activity mMainActivity, ImageView View, DisplayMetrics mMetrics){

        this.mCameraView = View;
        this.mMainActivity = mMainActivity;
        this.metrics = mMetrics;

        try {
            InetAddress host = InetAddress.getByName("192.168.1.1");

            ARDrone drone = new ARDrone(host,10000,10000);


        } catch (Exception e) {
            e.printStackTrace();
        }

=======
    public VideoManager(Activity mMainActivity, SurfaceView mPreview, DisplayMetrics mMetrics){

        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(mMainActivity))
            return;

        metrics = mMetrics;

        holder = mPreview.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
                // TODO Auto-generated method stub

            }

            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub

            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub

            }
        });

        holder.setFormat(PixelFormat.RGBA_8888);
        mMediaPlayer = new MediaPlayer(mMainActivity);
    }

    /**
     * Sets up the MediaPlayer. This method does not directly play the video.
     * Instead it sets everything up and once the video is buffered then it will play.
     * @return The media player which will play the video.
     * @see VideoManager#onPrepared(MediaPlayer)
     */
    MediaPlayer PlayVideo(){

        try {
            mMediaPlayer.setDataSource(path);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        mMediaPlayer.setDisplay(holder);
        mMediaPlayer.prepareAsync();
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);

        return mMediaPlayer;

    }

    /**
     * Call this function before quitting / destroying the application to save ressources.
     */
    void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * Starts the video once everything is set up.
     */
    void startVideoPlayback() {
        holder.setFixedSize(metrics.widthPixels, metrics.heightPixels);
        mMediaPlayer.start();
    }

    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        // TODO Auto-generated method stub
    }

    /**
     * Waits for the playback to be ready before playing the video (MediaPlayer is initialized, video is buffered...)
     * @param mp The MediaPlayer that is ready for playback
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        // TODO Auto-generated method stub
        this.startVideoPlayback();
>>>>>>> parent of eaf636d... try manual frame reception but no decoding
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        // TODO Auto-generated method stub
    }
}
