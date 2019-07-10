package com.a_track_it.fitdata.workers;


import android.content.Context;

import android.util.Log;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.user_model.UserPreferences;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.SensorsClient;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BoundFitnessSensorManager {
    public static void bindFitnessSensorListenerIn(LifecycleOwner lifecycleOwner,
                                                   OnDataPointListener listener, Context context){
        new BoundFitnessSensorListener(lifecycleOwner,listener,context);
    }

    static class BoundFitnessSensorListener implements LifecycleObserver {
        public static final String LOG_TAG = BoundFitnessSensorListener.class.getSimpleName();
        private final Context mContext;
        private final OnDataPointListener dataPointListener;
        private final SensorsClient mSensorsClient;
        private final GoogleSignInAccount gsa;
        private static int mBPMCount;
        private static int mStepSensorCount;
        private static int mBPMSensorCount;
        private ArrayList<DataSource> mExerciseDataSourceList = new ArrayList<>();
        private ArrayList<DataSource> mStepDataSourceList = new ArrayList<>();
        private ArrayList<DataSource> mBPMDataSourceList = new ArrayList<>();

        public BoundFitnessSensorListener(LifecycleOwner lifecycleOwner,
                                          OnDataPointListener listener, Context context) {

            mBPMCount = 0;
            mContext = context;
            dataPointListener = listener;
            gsa = GoogleSignIn.getLastSignedInAccount(mContext);
            mSensorsClient = Fitness.getSensorsClient(mContext, gsa);
            lifecycleOwner.getLifecycle().addObserver(this);
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        void buildSensorLists() {
            if (UserPreferences.getConfirmUseSensors(mContext)) {
                addFitSensorForDataType(DataType.TYPE_HEART_RATE_BPM);
                addFitSensorForDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE);
                addFitSensorForDataType(DataType.TYPE_LOCATION_SAMPLE);
                addFitSensorForDataType(DataType.TYPE_ACTIVITY_SAMPLES);
                addFitSensorForDataType(DataType.TYPE_WORKOUT_EXERCISE);
                addFitSensorForDataType(DataType.TYPE_CALORIES_EXPENDED);
                addFitSensorForDataType(DataType.TYPE_MOVE_MINUTES);
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        void addSensorListener() {
            Log.d(LOG_TAG, "BoundFitSensor onResume");
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        void removeSensorListener() {
            Log.d(LOG_TAG, "BoundFitSensor onPause");
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        void destroyListener() {
            if (((mBPMCount > 0) || (mStepSensorCount > 0) || (mBPMSensorCount > 0))) {
                mSensorsClient.remove(dataPointListener);
                mBPMCount = 0;
                mBPMSensorCount = 0;
                mStepSensorCount = 0;
            }
        }

        public void addFitSensorForDataType(final DataType mDataType) {
            final String sLabel = mContext.getString(R.string.label_sensor) + mDataType.getName();
            Boolean defaultValue = UserPreferences.getPrefByLabel(mContext, sLabel);
            if (mSensorsClient != null && defaultValue)
                mSensorsClient.findDataSources(new DataSourcesRequest.Builder()
                        .setDataTypes(mDataType)
                        .setDataSourceTypes(DataSource.TYPE_DERIVED)
                        .setDataSourceTypes(DataSource.TYPE_RAW)
                        .build()).addOnCompleteListener(new OnCompleteListener<List<DataSource>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<DataSource>> task) {
                        if (task.isSuccessful()) {
                            ArrayList<DataSource> mDataSourceList = new ArrayList<>();
                            mDataSourceList.addAll(task.getResult());
                            if (mDataSourceList.size() > 0) {
                                for (DataSource dataSource : mDataSourceList) {
                                    Log.i(LOG_TAG, "Adding sensor listener " + dataSource.toString());
                                    SensorRequest request = null;
                                    int sampleRate; // default

                                    if (DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(dataSource.getDataType())){
                                        sampleRate = UserPreferences.getStepsSampleRate(mContext);
                                        if (sampleRate > 0)
                                            request = new SensorRequest.Builder()
                                                    .setDataSource(dataSource)
                                                    .setDataType(dataSource.getDataType())
                                                    .setSamplingRate(sampleRate, TimeUnit.SECONDS)
                                                    .build();
                                    }else
                                    if (DataType.TYPE_HEART_RATE_BPM.equals(dataSource.getDataType())) {
                                        sampleRate = UserPreferences.getBPMSampleRate(mContext);
                                        if (sampleRate > 0)
                                            request = new SensorRequest.Builder()
                                                    .setDataSource(dataSource)
                                                    .setDataType(dataSource.getDataType())
                                                    .setSamplingRate(sampleRate, TimeUnit.SECONDS)
                                                    .build();
                                    }else
                                        sampleRate = UserPreferences.getOthersSampleRate(mContext);
                                    if (sampleRate > 0)
                                        request = new SensorRequest.Builder()
                                                .setDataSource(dataSource)
                                                .setDataType(dataSource.getDataType())
                                                .setSamplingRate(sampleRate, TimeUnit.SECONDS)
                                                .build();
                                    if ((sampleRate > 0) && (request != null))
                                                mSensorsClient.add(request, dataPointListener).addOnCompleteListener(
                                                        new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.i(LOG_TAG, "Listener registered!");
                                                                } else {
                                                                    // intSensorDataListenerCount not added
                                                                    Log.e(LOG_TAG, "Listener not registered.", task.getException());
                                                                }
                                                            }
                                                        });
                                    break;
                                }
                            } else {
                                Log.d(LOG_TAG, "No sensors for " + mDataType.toString());
                                UserPreferences.setPrefByLabel(mContext, sLabel, false); // we wont try again!
                            }

                        }
                    }
                });

        }
    }
}