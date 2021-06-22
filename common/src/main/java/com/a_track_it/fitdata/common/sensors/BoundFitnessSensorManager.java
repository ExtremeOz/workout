package com.a_track_it.fitdata.common.sensors;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.R;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.SensorsClient;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class BoundFitnessSensorManager {
    static BoundFitnessSensorListener mFitSensorListener;
    public static void bindFitnessSensorListenerIn(LifecycleOwner lifecycleOwner,
                                                   OnDataPointListener listener, Context context){
        mFitSensorListener = new BoundFitnessSensorListener(lifecycleOwner,listener,context);
    }
    public static void doSetup(){
        if (mFitSensorListener != null)
            mFitSensorListener.doSetupBindings();
    }
    public static void doReset(){
        if (mFitSensorListener != null)
            mFitSensorListener.doResetBindings();
    }
    public static void setState(int state){
        if (mFitSensorListener != null) mFitSensorListener.setState(state);
    }
    public static int getState(){
        if (mFitSensorListener != null)
            return mFitSensorListener.getState();
        else
            return Constants.WORKOUT_INVALID;
    }
    public static int getCount(int type){
        return (mFitSensorListener != null) ? mFitSensorListener.getSensorCount(type) : 0;
    }
    public static boolean isInitialised(){ return (mFitSensorListener != null);}

    static class BoundFitnessSensorListener implements DefaultLifecycleObserver {
        public static final String LOG_TAG = BoundFitnessSensorListener.class.getSimpleName();
        private final Context mContext;
        private final OnDataPointListener dataPointListener;
        private final SensorsClient mSensorsClient;
        private final GoogleSignInAccount gsa;
        private String sUserId;
        private int mState;
        private static int mOtherSensorCount;
        private static int mStepSensorCount;
        private static int mBPMSensorCount;

        public void setState(int s){ mState = s;}
        public int getState(){ return mState;}

        /** Gets {@link FitnessOptions} in order to check or request OAuth permission for the user. */
        private FitnessOptions getFitnessSignInOptions() {
            return FitnessOptions.builder()
                    .addDataType(DataType.TYPE_POWER_SAMPLE, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_WRITE)
                    .build();
        }
        private BoundFitnessSensorListener(LifecycleOwner lifecycleOwner,
                                          OnDataPointListener listener, Context context) {

            mOtherSensorCount = 0;
            mContext = context;
            dataPointListener = listener;
            gsa = GoogleSignIn.getAccountForExtension(mContext, getFitnessSignInOptions());
            if (((Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) || (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED))
                && (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED)){
                sUserId = gsa.getId();
                mSensorsClient = Fitness.getSensorsClient(mContext, gsa);
                lifecycleOwner.getLifecycle().addObserver(this);
            }else
                mSensorsClient = null;

        }

        @Override
        public void onStart(LifecycleOwner owner) {
            doSetupBindings();
        }

        @Override
        public void onStop(LifecycleOwner owner) {
            if (((mOtherSensorCount > 0) || (mStepSensorCount > 0) || (mBPMSensorCount > 0))) {
                if (mSensorsClient != null){
                    mSensorsClient.remove(dataPointListener);
                }
                mOtherSensorCount = 0;
                mBPMSensorCount = 0;
                mStepSensorCount = 0;
            }
        }
        private void doResetBindings(){
            if (mSensorsClient != null && dataPointListener != null){
                mSensorsClient.remove(dataPointListener);
            }
            mOtherSensorCount = 0;
            mBPMSensorCount = 0;
            mStepSensorCount = 0;
        }
        private int getSensorCount(int type){
            if (type == 0) return mOtherSensorCount;
            if (type == 1) return mStepSensorCount;
            if (type == 2) return mBPMSensorCount;
            return 0;
        }
        private void doSetupBindings(){
            if (gsa != null){
             ApplicationPreferences appPrefs = ApplicationPreferences.getPreferences(mContext);
             UserPreferences userPrefs = UserPreferences.getPreferences(mContext,gsa.getId());
             if (userPrefs != null)
                if (gsa.getId().equals(userPrefs.getUserId())) {
                    boolean bSetup = userPrefs.getReadSensorsPermissions();
                    String sLabel = mContext.getString(R.string.label_fit_sensor) + DataType.TYPE_HEART_RATE_BPM.getName();
                    if (!bSetup || (appPrefs.getPrefByLabel(sLabel)))
                        addFitSensorForDataType(DataType.TYPE_HEART_RATE_BPM);
                    sLabel = mContext.getString(R.string.label_fit_sensor) + DataType.TYPE_STEP_COUNT_DELTA.getName();
                    if (!bSetup || (appPrefs.getPrefByLabel(sLabel)))
                        addFitSensorForDataType(DataType.TYPE_STEP_COUNT_DELTA);
                    if ((appPrefs.getUseLocation()) || !bSetup)
                        addFitSensorForDataType(DataType.TYPE_LOCATION_SAMPLE);
                    sLabel = mContext.getString(R.string.label_fit_sensor) + DataType.TYPE_WORKOUT_EXERCISE.getName();
                    if ((appPrefs.getPrefByLabel(sLabel)) || !bSetup)
                        addFitSensorForDataType(DataType.TYPE_WORKOUT_EXERCISE);
                    sLabel = mContext.getString(R.string.label_fit_sensor) + DataType.TYPE_CALORIES_EXPENDED.getName();
                    if ((appPrefs.getPrefByLabel(sLabel)) || !bSetup)
                        addFitSensorForDataType(DataType.TYPE_CALORIES_EXPENDED);
                    sLabel = mContext.getString(R.string.label_fit_sensor) + DataType.TYPE_MOVE_MINUTES.getName();
                    if ((appPrefs.getPrefByLabel(sLabel)) || !bSetup)
                        addFitSensorForDataType(DataType.TYPE_MOVE_MINUTES);
                    sLabel = mContext.getString(R.string.label_fit_sensor) + DataType.TYPE_HEART_POINTS.getName();
                    if ((appPrefs.getPrefByLabel(sLabel)) || !bSetup)
                        addFitSensorForDataType(DataType.TYPE_HEART_POINTS);

                }
            }
        }

        public void addFitSensorForDataType(final DataType mDataType) {
            final String sLabelSource = mContext.getString(R.string.label_fit_sensor) + mDataType.getName();
            final String sLabelName = mContext.getString(R.string.label_fit_name) + mDataType.getName();
            ApplicationPreferences preferences = ApplicationPreferences.getPreferences(mContext);
            final UserPreferences prefs = UserPreferences.getPreferences(mContext, sUserId);

            if (mSensorsClient != null) {
                    mSensorsClient.findDataSources(new DataSourcesRequest.Builder()
                        .setDataTypes(mDataType)
                        .setDataSourceTypes(DataSource.TYPE_DERIVED)
                        .setDataSourceTypes(DataSource.TYPE_RAW)
                        .build()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<DataSource> mDataSourceList = new ArrayList<>();
                        mDataSourceList.addAll(task.getResult());
                        if (mDataSourceList.size() > 0) {
                            for (DataSource dataSource : mDataSourceList) {
                                final String sSource = dataSource.getStreamName();

                                SensorRequest request = null;
                                long sampleRate; // default
                                if (DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(dataSource.getDataType())
                                        || DataType.TYPE_STEP_COUNT_DELTA.equals(dataSource.getDataType())) {
                                    sampleRate = (prefs != null) ?  prefs.getStepsSampleRate() :120;
                                    if (sampleRate > 0)
                                        request = new SensorRequest.Builder()
                                                .setDataSource(dataSource)
                                                .setDataType(dataSource.getDataType())
                                                .setSamplingRate(sampleRate, TimeUnit.SECONDS)
                                                .build();
                                } else if (DataType.TYPE_HEART_RATE_BPM.equals(dataSource.getDataType())) {
                                    sampleRate = (prefs != null) ?  prefs.getBPMSampleRate(): 60L;
                                    if (sampleRate > 0)
                                        request = new SensorRequest.Builder()
                                                .setDataSource(dataSource)
                                                .setDataType(dataSource.getDataType())
                                                .setSamplingRate(sampleRate, TimeUnit.SECONDS)
                                                .build();
                                } else
                                    sampleRate = (prefs != null) ?  prefs.getOthersSampleRate() : 300L;
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
                                                        Log.e(LOG_TAG, "adding FIT sensory " + dataSource.getDataType().getName());
                                                        preferences.setPrefByLabel(sLabelSource, true);
                                                        preferences.setPrefStringByLabel(sLabelName, sSource);
                                                        if (DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(mDataType) || DataType.TYPE_STEP_COUNT_DELTA.equals(mDataType))
                                                            mStepSensorCount++;
                                                        else if (DataType.TYPE_HEART_RATE_BPM.equals(mDataType))
                                                            mBPMSensorCount++;
                                                        else
                                                            mOtherSensorCount++;
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
                            preferences.setPrefByLabel(sLabelSource, false);
                        }
                    }
                });
            }

        }
    }

}