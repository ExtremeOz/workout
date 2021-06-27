package com.a_track_it.workout.common.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.List;

public class BoundAccelSensorManager {
    static BoundAccelSensorManager.BoundSensorListener INSTANCE;
        public static void bindSensorListenerIn(LifecycleOwner lifecycleOwner,
                                                SensorEventListener listener, Context context){
            INSTANCE = new BoundSensorListener(lifecycleOwner,listener,context);
        }

    static public void DestroyListener(){
        if (INSTANCE != null) INSTANCE.destroyListener();
    }

        static class BoundSensorListener implements DefaultLifecycleObserver {
            private final Context mContext;
            private final SensorEventListener sensorEventListener;
            private final SensorManager mSensorManager;
            private static int mAccelermeterCount;
            private static int mLinearSensorCount;
            private static int mRotateSensorCount;

            private static List<Sensor> acceleroList;
            private static List<Sensor> linearSensorList;
            private static List<Sensor> rotateSensorList;
            private static List<Sensor> gyroSensorList;
            private static List<Sensor> gravitySensorList;
          //  private static  List<Sensor> stepDetectorList;

            private BoundSensorListener(LifecycleOwner lifecycleOwner,
                                       SensorEventListener listener, Context context) {
                mAccelermeterCount = 0;
                mContext = context;
                sensorEventListener = listener;
                mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                lifecycleOwner.getLifecycle().addObserver(this);
            }
            @Override
            public void onStart(LifecycleOwner owner) {
                acceleroList = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
                linearSensorList = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
                rotateSensorList = mSensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
                gyroSensorList = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
                gravitySensorList = mSensorManager.getSensorList(Sensor.TYPE_GRAVITY);
           //     stepDetectorList = mSensorManager.getSensorList(Sensor.TYPE_STEP_DETECTOR);
            }

            @Override
            public void onResume(LifecycleOwner owner) {
/*                for(int i = 0; i < stepDetectorList.size(); i++){
                    mSensorManager.registerListener(sensorEventListener, stepDetectorList.get(i), SensorManager.SENSOR_DELAY_NORMAL);
                    mStepDetectorCount++;
                }*/
                for(int i = 0; i < acceleroList.size(); i++){
                    mSensorManager.registerListener(sensorEventListener, acceleroList.get(i), SensorManager.SENSOR_DELAY_NORMAL);
                    mAccelermeterCount++;
                }
                for(int i = 0; i < linearSensorList.size(); i++){
                    mSensorManager.registerListener(sensorEventListener, linearSensorList.get(i), SensorManager.SENSOR_DELAY_NORMAL);
                    mLinearSensorCount++;
                }
                for(int i = 0; i < rotateSensorList.size(); i++){
                    mSensorManager.registerListener(sensorEventListener, rotateSensorList.get(i), SensorManager.SENSOR_DELAY_NORMAL);
                    mRotateSensorCount++;
                }
                if (gyroSensorList.size() != 0)
                for(int i = 0; i < gyroSensorList.size(); i++){
                    mSensorManager.registerListener(sensorEventListener, gyroSensorList.get(i), SensorManager.SENSOR_DELAY_NORMAL);
                }
                for(int i = 0; i < gravitySensorList.size(); i++){
                    mSensorManager.registerListener(sensorEventListener, gravitySensorList.get(i), SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
            public void destroyListener(){
                if (((mAccelermeterCount > 0)||(mLinearSensorCount > 0)||(mRotateSensorCount > 0)) && (mSensorManager != null)) {
                    mSensorManager.unregisterListener(sensorEventListener);
                    mAccelermeterCount = 0; mRotateSensorCount = 0; mLinearSensorCount = 0;
                }
            }
            @Override
            public void onPause(LifecycleOwner owner) {
                destroyListener();
            }

            @Override
            public void onDestroy(LifecycleOwner owner) {
                destroyListener();
            }
        }


}
