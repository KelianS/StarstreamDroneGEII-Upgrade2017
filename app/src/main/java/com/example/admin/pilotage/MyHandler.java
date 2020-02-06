package com.example.admin.pilotage;


import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

class MyHandler extends Handler {
    private final WeakReference<VideoManager> mActivity;

    public MyHandler(VideoManager activity) {
        mActivity = new WeakReference<VideoManager>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        VideoManager activity = mActivity.get();
        if (activity != null) {
            try {
                activity.mLastFrame = (Bitmap) msg.obj;
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    }
}

