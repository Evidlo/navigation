package org.opencv.samples.tutorial1;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;

import es.ava.aruco.CameraParameters;
import es.ava.aruco.MarkerDetector;
import es.ava.aruco.Marker;

import android.Manifest;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import java.util.TimerTask;
import java.util.Timer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.List;
import java.util.Locale;
import java.util.Vector;


public class MarkerTracker extends Activity implements CvCameraViewListener2, SensorEventListener {

    TextToSpeech tts;

    //Constants
    private static final String TAG = "Aruco";
    private static final float MARKER_SIZE = (float) 0.2;
    private boolean onLandmark_enabled = true;

    //Preferences
    private static final boolean SHOW_MARKERID = true;

    public Timer timer = new Timer();
    public TimerTask timer_task;
    public boolean timer_running = false;

    private float rotation = 0;


    //You must run a calibration prior to detection
    // The activity to run calibration is provided in the repository
    private static final String DATA_FILEPATH = "/foo2";

    private CameraBridgeViewBase mOpenCvCameraView;
    private Landmark[] locations = {new Landmark(213, "Kitchen", "go down the hallway and turn right to get to the bathroom", "testing", "Testing","oijij"),
                                    new Landmark(265, "Bathroom", "gorbachev bleep boop", "hihihi", "nonon", "dont go that way"),
                                    new Landmark(341, "Garage", "this way leads to certain death", "donuts are here", "run away", "doggo"),
                                    new Landmark(303, "Bedroom", "meepmorp", "wejfijef", "123123", "lkjlkj")};


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.d(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    public MarkerTracker() {
        Log.d(TAG, "Instantiated new " + this.getClass());

    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        tts = new TextToSpeech(MarkerTracker.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    }
                } else
                    Log.e("error", "Initilization Failed!");
            }
        });

        ActivityCompat.requestPermissions(MarkerTracker.this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial1_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        mSensorManager.unregisterListener(this);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);


    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public void onLandmark(Landmark loc){

        String Name = loc.getName();
        String message;

        // check which way we are facing and speak the directions
        if (onLandmark_enabled){
            if((45 > rotation) & (rotation >= -45)) {
                message = loc.getNorthText();
            }else if((-45 > rotation) & (rotation >= -135)){
                message = loc.getEastText();
            }else if((-135 > rotation) | (rotation > 135)){
                message = loc.getSouthText();
            }else{
                message = loc.getWestText();
            }

            ConvertTextToSpeech(message);
        }

        // rate limit landmark detection
        onLandmark_enabled = false;
        if (timer_running){
            timer_task.cancel();
        }
        timer_task = new TimerTask() {
            public void run() {
                onLandmark_enabled = true;
            }
        };
        timer_running = true;
        timer.schedule(timer_task, 5000);



    }
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //Convert input to rgba
        Mat rgba = inputFrame.rgba();

        //Setup required parameters for detect method
        MarkerDetector mDetector = new MarkerDetector();
        Vector<Marker> detectedMarkers = new Vector<>();
        CameraParameters camParams = new CameraParameters();
        camParams.readFromFile(Environment.getExternalStorageDirectory().toString() + DATA_FILEPATH);

        //Populate detectedMarkers
        mDetector.detect(rgba, detectedMarkers, camParams, MARKER_SIZE);

        //Draw Axis for each marker detected
        if (detectedMarkers.size() != 0) {
            for (int i = 0; i < detectedMarkers.size(); i++) {
                Marker marker = detectedMarkers.get(i);
                detectedMarkers.get(i).draw3dAxis(rgba, camParams, new Scalar(0,0,0));

                if (SHOW_MARKERID) {
                    //Setup
                    int idValue = detectedMarkers.get(i).getMarkerId();
                    Vector<Point3> points = new Vector<>();
                    points.add(new Point3(0, 0, 0));
                    MatOfPoint3f pointMat = new MatOfPoint3f();
                    pointMat.fromList(points);
                    MatOfPoint2f outputPoints = new MatOfPoint2f();

                    //Project point to marker origin
                    Calib3d.projectPoints(pointMat, marker.getRvec(), marker.getTvec(), camParams.getCameraMatrix(), camParams.getDistCoeff(), outputPoints);
                    List<Point> pts = new Vector<>();
                    pts = outputPoints.toList();

                    // Draw id number
                    Core.putText(rgba, Integer.toString(idValue), pts.get(0), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(0,0,1));

                for (int j = 0; j < locations.length ; j++) {
                    if(detectedMarkers.get(i).getMarkerId() == locations[j].getId()) {
                        // if a landmark if found
                            onLandmark(locations[j]);

                        }
                    }
                }
            }
        }
        return rgba;
    }

    private void ConvertTextToSpeech(String text) {
        // TODO Auto-generated method stub
        //text = et.getText().toString();
        if(text == null||"".equals(text))
        {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;


    float azimuth = 0;

    public float getDirection() {return azimuth; }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    float[] mGravity;
    float[] mGeomagnetic;
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = event.values;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            mGeomagnetic = event.values;

        }
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0]; // orientation contains: azimut, pitch and roll
                rotation = -azimuth * 360 / (2 * 3.14159f);

                Log.i("compass", Float.toString(rotation));
            }
        }
    }
}
