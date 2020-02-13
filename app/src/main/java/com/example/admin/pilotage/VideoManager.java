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



    /**
     * Constructor
     * @param mMainActivity Used to check the librairy
     * @param mPreview Surface on which the video will be displayed
     * @param mMetrics Used to get info about the smartphone's screen
     */
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

    }


}
