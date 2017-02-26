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
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;
import java.util.Vector;

public class MarkerTracker extends Activity implements CvCameraViewListener2 {

    //Constants
    private static final String TAG = "Aruco";
    private static final float MARKER_SIZE = (float) 0.2;

    //Preferences
    private static final boolean SHOW_MARKERID = true;

    //You must run a calibration prior to detection
    // The activity to run calibration is provided in the repository
    private static final String DATA_FILEPATH = "/foo2";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;
 private Landmark[] locations = {new Landmark(213, "Elevator", "The bathroom is further down the hall", "The bathroom is further down the hall", "The bathroom is further down the hall","The bathroom is further down the hall"),
                                    new Landmark(265, "Bathroom", "There is food to left of here", "There is food to left of here", "There is food to left of here", "There is food to left of here"),
                                    new Landmark(341, "Hallway", "This is dangerous. Please don't go this way.", "This is dangerous. Please don't go this way.", "This is dangerous. Please don't go this way.", "This is dangerous. Please don't go this way."),
                                    new Landmark(303, "Food", "You have arrived at food", "You have arrived at food", "You have arrived at food", "You have arrived at food")};



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

        ActivityCompat.requestPermissions(MarkerTracker.this, new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
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

    public String readLandmark(Landmark loc){

        String leftright;
        String updown;
        String LocationName = loc.getLandmarkName();
        String LocationDescription = loc.getLandmarkDescription();

        int[] vel = field.getDirections(loc, field.getClosest(loc));

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

        return("You are passing the " + LocationName + ". This " + LocationDescription + ". " + direct);


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

                    //Draw id number
                    Core.putText(rgba, Integer.toString(idValue), pts.get(0), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(0,0,1));

                for (int j = 0; j < locations.length ; j++) {
                    if(detectedMarkers.get(i).getMarkerId() == locations[j].getLandmarkID()) {
                        Core.putText(rgba, readLandmark(locations[j]), pts.get(0), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(0, 0, 1));
                    }
                    }
                }

            }

        }

        return rgba;
    }
}
