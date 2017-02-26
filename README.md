# MARKO | HackIllinois 2017

Before running the first time, edit `openCVTutorial1CameraPreview/src/main/AndroidManifest.xml`
        <activity android:name=".MarkerTracker"
to
        <activity android:name="calibration.CameraCalibrationActivity"

Then run the application.  The app will prompt to install OpenCV manager, so accept the installation.  Close the app and run once more to calibrate the camera and create the calibration file.

Change the activity line back to `<activity android:name=".MarkerTracker"` and run the application again to start ArUco.


This project is based on [Arco-Marker-Tracking-Android]https://github.com/jsmith613/Aruco-Marker-Tracking-Android()
