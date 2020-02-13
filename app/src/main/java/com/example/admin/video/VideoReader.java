
package com.example.admin.video;

import android.util.Log;

import  com.example.admin.video.VideoDataProcessor;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class VideoReader extends DataReader {
    /**
     * Image data buffer. It should be big enough to hold single full frame
     * (encoded).
     */
    private static final int BUFSIZE = 100 * 1024;
        
    VideoDataProcessor videoProcessor;
    
    public VideoReader(ARDrone drone, InetAddress drone_addr, int video_port, int reconnect_timeout) throws IOException
    {
        super(drone, drone_addr, video_port, BUFSIZE, reconnect_timeout);
        videoProcessor = new VideoDataProcessor(drone, BUFSIZE);
        videoProcessor.start();
    }

    @Override
    void handleData(ByteBuffer inbuf, int len)
            throws Exception {
        if (len > 0) {        
            videoProcessor.addDataToProcess(inbuf, len);
            Log.i("video","addDataToProcess");
        }
    }
    
    public void finish()
    {
        videoProcessor.finish();
        super.finish();
    }

}
