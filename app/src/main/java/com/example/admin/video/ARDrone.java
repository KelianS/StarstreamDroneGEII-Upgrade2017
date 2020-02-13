
package com.example.admin.video;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.util.Log;
import java.io.IOException;


public class ARDrone
{
    public enum State
    {
        DISCONNECTED, CONNECTING, BOOTSTRAP, DEMO, ERROR, TAKING_OFF, LANDING
    }

    public enum VideoChannel
    {
        HORIZONTAL_ONLY, VERTICAL_ONLY, VERTICAL_IN_HORIZONTAL, HORIZONTAL_IN_VERTICAL
    }

    public enum Animation
    {
        PHI_M30_DEG(0), PHI_30_DEG(1), THETA_M30_DEG(2), THETA_30_DEG(3), THETA_20DEG_YAW_200DEG(4), THETA_20DEG_YAW_M200DEG(
                5), TURNAROUND(6), TURNAROUND_GODOWN(7), YAW_SHAKE(8), YAW_DANCE(9), PHI_DANCE(10), THETA_DANCE(11), VZ_DANCE(
                12), WAVE(13), PHI_THETA_MIXED(14), DOUBLE_PHI_THETA_MIXED(15), ANIM_MAYDAY(16);

        private int value;

        private Animation(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }
    }

    public enum LED
    {
        BLINK_GREEN_RED(0), BLINK_GREEN(1), BLINK_RED(2), BLINK_ORANGE(3), SNAKE_GREEN_RED(4), FIRE(5), STANDARD(6), RED(
                7), GREEN(8), RED_SNAKE(9), BLANK(10), RIGHT_MISSILE(11), LEFT_MISSILE(12), DOUBLE_MISSILE(13), FRONT_LEFT_GREEN_OTHERS_RED(
                14), FRONT_RIGHT_GREEN_OTHERS_RED(15), REAR_RIGHT_GREEN_OTHERS_RED(16), REAR_LEFT_GREEN_OTHERS_RED(17), LEFT_GREEN_RIGHT_RED(
                18), LEFT_RED_RIGHT_GREEN(19), BLINK_STANDARD(20);

        private int value;

        private LED(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }
    }

    public enum ConfigOption
    {
        ACCS_OFFSET("control:accs_offset"), ACCS_GAINS("control:accs_gains"), GYROS_OFFSET("control:gyros_offset"), GYROS_GAINS(
                "control:gyros_gains"), GYROS110_OFFSET("control:gyros110_offset"), GYROS110_GAINS(
                "control:gyros110_gains"), GYRO_OFFSET_THR_X("control:gyro_offset_thr_x"), GYRO_OFFSET_THR_Y(
                "control:gyro_offset_thr_y"), GYRO_OFFSET_THR_Z("control:gyro_offset_thr_z"), PWM_REF_GYROS(
                "control:pwm_ref_gyros"), CONTROL_LEVEL("control:control_level"), SHIELD_ENABLE("control:shield_enable"), EULER_ANGLE_MAX(
                "control:euler_angle_max"), ALTITUDE_MAX("control:altitude_max"), ALTITUDE_MIN("control:altitude_min"), CONTROL_TRIM_Z(
                "control:control_trim_z"), CONTROL_IPHONE_TILT("control:control_iphone_tilt"), CONTROL_VZ_MAX(
                "control:control_vz_max"), CONTROL_YAW("control:control_yaw"), OUTDOOR("control:outdoor"), FLIGHT_WITHOUT_SHELL(
                "control:flight_without_shell"), BRUSHLESS("control:brushless"), AUTONOMOUS_FLIGHT(
                "control:autonomous_flight"), MANUAL_TRIM("control:manual_trim"), INDOOR_EULER_ANGLE_MAX(
                "control:indoor_euler_angle_max"), INDOOR_CONTROL_VZ_MAX("control:indoor_control_vz_max"), INDOOR_CONTROL_YAW(
                "control:indoor_control_yaw"), OUTDOOR_EULER_ANGLE_MAX("control:outdoor_euler_angle_max"), OUTDOOR_CONTROL_VZ_MAX(
                "control:outdoor_control_vz_max"), OUTDOOR_CONTROL_YAW("outdoor_control:control_yaw");

        private String value;

        private ConfigOption(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }
    }

    private Logger                          log               = Logger.getLogger(getClass().getName());

    private static final int                CMD_QUEUE_SIZE    = 64;
    private State                           state             = State.DISCONNECTED;
    private Object                          state_mutex       = new Object();

    private static final int                NAVDATA_PORT      = 5554;
    private static final int                VIDEO_PORT        = 5555;
    // private static final int CONTROL_PORT = 5559;

    final static byte[]                     DEFAULT_DRONE_IP  = { (byte) 192, (byte) 168, (byte) 1, (byte) 1 };

    private InetAddress                     drone_addr;
    private DatagramSocket                  cmd_socket;
    // private Socket control_socket;



    private VideoReader                     video_reader;


    private Thread                          nav_data_reader_thread;
    private Thread                          cmd_sending_thread;
    private Thread                          video_reader_thread;

    private boolean                         combinedYawMode   = true;

    private boolean                         emergencyMode     = true;
    private Object                          emergency_mutex   = new Object();


    private List<DroneVideoListener>        image_listeners   = new LinkedList<DroneVideoListener>();


    private static int                      navDataReconnectTimeout = 1000; // 1 second

    private static int                      videoReconnectTimeout   = 1000; // 1 second

    public ARDrone() throws UnknownHostException
    {
        this(InetAddress.getByAddress(DEFAULT_DRONE_IP), navDataReconnectTimeout, videoReconnectTimeout);
    }

    public ARDrone(InetAddress drone_addr, int navDataReconnectTimeout, int videoReconnectTimeout)
    {
        this.drone_addr = drone_addr;
        this.navDataReconnectTimeout = navDataReconnectTimeout;
        this.videoReconnectTimeout = videoReconnectTimeout;

            try{
                video_reader = new VideoReader(this, drone_addr, VIDEO_PORT, videoReconnectTimeout);
                video_reader_thread = new Thread(video_reader);
                video_reader_thread.setName("Video Reader");
                video_reader_thread.start();
            }catch (IOException io){
                Log.i("Thread","Failed");
            }




    }

    public void addImageListener(DroneVideoListener l)
    {
        synchronized(image_listeners)
        {
            image_listeners.add(l);
        }
    }

    public void removeImageListener(DroneVideoListener l)
    {
        synchronized(image_listeners)
        {
            image_listeners.remove(l);
        }
    }

    public void clearImageListeners()
    {
        synchronized(image_listeners)
        {
            image_listeners.clear();
        }
    }


    // Callback used by VideoReciver
    public void videoFrameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize)
    {
        synchronized(image_listeners)
        {
            for(DroneVideoListener l : image_listeners)
                l.frameReceived(startX, startY, w, h, rgbArray, offset, scansize);
        }
    }



}
