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
import org.opencv.core.MatOfPoint3;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import es.ava.aruco.CameraParameters;
import es.ava.aruco.MarkerDetector;
import es.ava.aruco.Marker;
import es.ava.aruco.Utils;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import java.util.TimerTask;
import java.util.Timer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.Vector;
import android.speech.tts.TextToSpeech;

public class MarkerTracker extends Activity implements CvCameraViewListener2 {

    TextToSpeech tts;

    //Constants
    private static final String TAG = "Aruco";
    private static final float MARKER_SIZE = (float) 0.2;
    private boolean onLandmark_enabled = true;

    //Preferences
    private static final boolean SHOW_MARKERID = true;

    public Timer timer = new Timer();


    //You must run a calibration prior to detection
    // The activity to run calibration is provided in the repository
    private static final String DATA_FILEPATH = "/foo2";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;
    private Landmark[] locations = {new Landmark(213, "1", "First Checkpoint", 1, 1, 1),
                                    new Landmark(265, "2", "Second Checkpoint", 1, 2, 1),
                                    new Landmark(341, "3", "Third Checkpoint", 2, 1, 1),
                                    new Landmark(303, "4", "Fourth Checkpoint", 2, 2, 1)};

    private Map field = new Map(locations);

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    public MarkerTracker() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(MarkerTracker.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.US);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });

        ActivityCompat.requestPermissions(MarkerTracker.this, new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial1_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        ConvertTextToSpeech("mah fam");


    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

/*        if(tts != null){

            tts.stop();
            tts.shutdown();
        } */
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

        String leftright;
        String updown;
        String LocationName = loc.getLandmarkName();
        String LocationDescription = loc.getLandmarkDescription();

        int[] vel = field.getDirections(loc, field.getClosest(loc));



        if (onLandmark_enabled){
            if (vel[1] > 0) {
                updown = "down the hall";
            }else{
                updown = "behind you";
                vel[0] = vel[0]*(-1);

            }
            if (vel[0] > 0) {
                leftright = "right";
            }else{
                leftright = "left";
            }


            String direct = "The " + field.getClosest(loc).getLandmarkName() + " is " + updown + " and to the " + leftright + ".";

            ConvertTextToSpeech("You are passing the " + LocationName + ". This " + LocationDescription + ". " + direct);
            // rate limit landmark detection
            onLandmark_enabled = false;
            timer.schedule(new TimerTask() {
                public void run() {
                    Log.i(TAG, "sasquatch");
                    onLandmark_enabled = true;
                }
            }, 5000);
        }



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
                    if(detectedMarkers.get(i).getMarkerId() == locations[j].getLandmarkID()) {
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
}
