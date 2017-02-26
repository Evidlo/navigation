package org.opencv.samples.tutorial1;

/**
 * Created by cheyenne on 2/26/17.
 */

import android.app.Activity;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;



public class Orientation extends Activity implements SensorEventListener {
        private final SensorManager mSensorManager;
        private final Sensor mAccelerometer;
        private static final String TAG = "Aruco";

    float azimuth = 0;


        public Orientation() {
            mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            Log.i("lkj","lkj");

        }

        public float getDirection() {return azimuth; }

        protected void onResume() {
            super.onResume();
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        protected void onPause() {
            super.onPause();
            mSensorManager.unregisterListener(this);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            azimuth = -event.values[0] * 360 / (2 * 3.14159f);
            System.out.println(azimuth);
            Log.i(TAG, "got sensor reading");
        }

}
