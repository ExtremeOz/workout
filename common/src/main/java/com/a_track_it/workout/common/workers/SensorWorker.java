package com.a_track_it.workout.common.workers;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.InjectorUtils;
import com.a_track_it.workout.common.R;
import com.a_track_it.workout.common.ReferencesTools;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.DateTuple;
import com.a_track_it.workout.common.data_model.PeakDetProcessor;
import com.a_track_it.workout.common.data_model.Processor;
import com.a_track_it.workout.common.data_model.SensorDailyTotals;
import com.a_track_it.workout.common.data_model.WorkoutRepository;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.a_track_it.workout.common.model.UserPreferences;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.SensorsClient;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static com.a_track_it.workout.common.Constants.ACTIVE_CHANNEL_ID;
import static com.a_track_it.workout.common.Constants.INTENT_EXTRA_RESULT;
import static com.a_track_it.workout.common.Constants.INTENT_LOCATION_UPDATE;
import static com.a_track_it.workout.common.Constants.WORKOUT_LIVE;
import static com.a_track_it.workout.common.Constants.WORKOUT_PAUSED;

public class SensorWorker extends Worker {
    private final static String LOG_TAG = SensorWorker.class.getSimpleName();
    private WorkoutRepository workoutRepository;
    private ReferencesTools referencesTools;
    private Calendar mCalendar;
    private WorkManager mWorkManager;
    private SensorEventListener sensorEventListener;
    private SensorsClient mSensorsClient;
    private OnDataPointListener mDataPointListener;
    private final SensorManager mSensorManager;
    private final ApplicationPreferences appPrefs;
    private final UserPreferences userPrefs;
    private static int mStepSensorCount;
    private static int mBPMSensorCount;
    private static int mTempSensorCount;
    private static int mPressureSensorCount;
    private static int mHumiditySensorCount;
    private static int mCurrentState;
    private boolean bCancelNow;
    private int type;
    private SensorDailyTotals SDT;


    public SensorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        workoutRepository =  InjectorUtils.getWorkoutRepository((Application) context);
        referencesTools = ReferencesTools.setInstance(context);
        appPrefs = ApplicationPreferences.getPreferences(context);
        userPrefs = UserPreferences.getPreferences(context, appPrefs.getLastUserID());
        mCalendar = Calendar.getInstance(Locale.getDefault());
        mWorkManager = WorkManager.getInstance(context);
        sensorEventListener = new xSensorListener();
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mDataPointListener = new xFITSensorListener();

    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String sUserID = data.getString(Constants.KEY_FIT_USER);
        mCurrentState = data.getInt(Constants.KEY_FIT_VALUE, Constants.WORKOUT_INVALID);
        type = data.getInt(Constants.KEY_FIT_TYPE, 0);
        int interVal = data.getInt(Constants.KEY_FIT_TIME, 30);  // wait period in seconds
        final long realNow = SystemClock.elapsedRealtime();
        int delay = (mCurrentState == Constants.WORKOUT_LIVE || mCurrentState == Constants.WORKOUT_PAUSED) ? SensorManager.SENSOR_DELAY_GAME : SensorManager.SENSOR_DELAY_NORMAL;
        long fitBPM = userPrefs.getBPMSampleRate();
        long fitStep = userPrefs.getStepsSampleRate();
        long fitOthers = userPrefs.getOthersSampleRate();
        int iRetVal = 0;
        if (type == 2 && (mCurrentState == Constants.WORKOUT_LIVE || mCurrentState == Constants.WORKOUT_PAUSED)){
            fitBPM = 5;
            fitStep = 5;
            fitOthers = 30;
        }
        if ((ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED)){
            Data outputData = new Data.Builder()
                    .putInt(Constants.KEY_RESULT, -550)
                    .putString(Constants.KEY_FIT_USER, sUserID)
                    .putString(Constants.KEY_FIT_VALUE, "No permissions")
                    .build();
            return Result.failure(outputData);
        }
        try {
            bCancelNow = false;
            Context context = getApplicationContext();
            String sMsg = context.getString(R.string.action_sensor_start);
            setForegroundAsync(createForegroundInfo(sMsg));
            Date now = new Date();
            mCalendar.setTime(now);

            long endTime = mCalendar.getTimeInMillis();
            long startHour = Utilities.getDayStart(mCalendar,endTime);
            DateTuple dt = workoutRepository.getSensorDailyDateTuple(sUserID, startHour,endTime);
            boolean bExisting = ((dt != null) && (dt.sync_count > 0));
            if (bExisting) {
                SensorDailyTotals topSDT = workoutRepository.getTopSensorDailyTotal(sUserID);
                if (topSDT != null){
                    if (mCurrentState == WORKOUT_LIVE || mCurrentState == WORKOUT_PAUSED){
                        if ((type != 1) && (endTime - topSDT._id) > TimeUnit.SECONDS.toMillis(20))
                            bExisting = false;
                        else
                            SDT = topSDT;
                    }else{
                        if (type != 1) {
                            if ((endTime - topSDT._id) > TimeUnit.MINUTES.toMillis(20))
                                bExisting = false;
                            else
                                SDT = topSDT;
                        }else{
                            if ((endTime - topSDT._id) > TimeUnit.MINUTES.toMillis(30))
                                bExisting = false;
                            else
                                SDT = topSDT;
                        }
                    }
                }else bExisting = false;
            }
            if (!bExisting) {
                Log.e(LOG_TAG,"not existing - adding new " + Utilities.getTimeDateString(endTime));
                SDT = new SensorDailyTotals(endTime, sUserID);
                SDT.userID = appPrefs.getLastUserID();
            }

            if (type == 0) {
                if (appPrefs.getTempSensorCount() > 0) {
                    List<Sensor> tempSensorList = mSensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);
                    if (tempSensorList.size() == 0) mTempSensorCount = 1;
                    else
                        for (int i = 0; i < tempSensorList.size(); i++) {
                            Log.w(LOG_TAG, "adding temp sensor ");
                            mSensorManager.registerListener(sensorEventListener, tempSensorList.get(i), delay);
                        }
                }
                if (appPrefs.getPressureSensorCount() > 0) {
                    List<Sensor> pressureSensorList = mSensorManager.getSensorList(Sensor.TYPE_PRESSURE);
                    if (pressureSensorList.size() == 0) mPressureSensorCount = 1;
                    else
                        for (int i = 0; i < pressureSensorList.size(); i++) {
                            Log.w(LOG_TAG, "adding pressure sensor ");
                            mSensorManager.registerListener(sensorEventListener, pressureSensorList.get(i), delay);
                        }
                }
                if (appPrefs.getHumiditySensorCount() > 0) {
                    List<Sensor> humiditySensorList = mSensorManager.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY);
                    if (humiditySensorList.size() == 0) mHumiditySensorCount = 1; // allows cancelNow to work without this sensor
                    for (int i = 0; i < humiditySensorList.size(); i++) {
                        Log.w(LOG_TAG, "adding humidity sensor ");
                        mSensorManager.registerListener(sensorEventListener, humiditySensorList.get(i), delay);
                    }
                }
            }
            if (type == 1) {
                if (appPrefs.getStepsSensorCount() > 0) {
                    List<Sensor> stepSensorList = mSensorManager.getSensorList(Sensor.TYPE_STEP_COUNTER);
                    if (stepSensorList.size() == 0) mStepSensorCount = 1;
                    else
                        for (int i = 0; i < stepSensorList.size(); i++) {
                            mSensorManager.registerListener(sensorEventListener, stepSensorList.get(i), delay);
                            Log.w(LOG_TAG, "adding step sensor " + stepSensorList.get(i).getName() + " delay " + delay);
                        }
                }
                if (appPrefs.getBPMSensorCount() > 0) {
                    List<Sensor> bpmSensorList = mSensorManager.getSensorList(Sensor.TYPE_HEART_RATE);
                    if (bpmSensorList.size() == 0) mBPMSensorCount = 1;
                    else
                        for (int i = 0; i < bpmSensorList.size(); i++) {
                            mSensorManager.registerListener(sensorEventListener, bpmSensorList.get(i), delay);
                            Log.w(LOG_TAG, "adding BPM sensor " + bpmSensorList.get(i).getName());
                        }
                }
            }
            if (type == 2) {
                GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(context, getFitnessSignInOptions());
                if (gsa != null) {
                    mSensorsClient = Fitness.getSensorsClient(context, gsa);
                    if (mSensorsClient != null){
                        String sLabel =  context.getString(R.string.label_fit_sensor) + DataType.TYPE_HEART_RATE_BPM.getName();
                        if (appPrefs.getPrefByLabel(sLabel))
                            addFitSensorForDataType(DataType.TYPE_HEART_RATE_BPM, fitBPM);
                        else
                            mBPMSensorCount = 1;
                        sLabel =  context.getString(R.string.label_fit_sensor) + DataType.TYPE_STEP_COUNT_DELTA.getName();
                        if (appPrefs.getPrefByLabel(sLabel))
                            addFitSensorForDataType(DataType.TYPE_STEP_COUNT_DELTA, fitStep);
                        else
                            mStepSensorCount = 1;
                        if (appPrefs.getUseLocation())
                            addFitSensorForDataType(DataType.TYPE_LOCATION_SAMPLE,fitOthers );
                        sLabel =  context.getString(R.string.label_fit_sensor) + DataType.TYPE_WORKOUT_EXERCISE.getName();
                        if (appPrefs.getPrefByLabel(sLabel))
                            addFitSensorForDataType(DataType.TYPE_WORKOUT_EXERCISE, fitOthers);
                        sLabel =  context.getString(R.string.label_fit_sensor) + DataType.TYPE_CALORIES_EXPENDED.getName();
                        if (appPrefs.getPrefByLabel(sLabel))
                            addFitSensorForDataType(DataType.TYPE_CALORIES_EXPENDED, fitOthers);
                        sLabel =  context.getString(R.string.label_fit_sensor) + DataType.TYPE_MOVE_MINUTES.getName();
                        if (appPrefs.getPrefByLabel(sLabel))
                            addFitSensorForDataType(DataType.TYPE_MOVE_MINUTES, fitOthers);
                        sLabel =  context.getString(R.string.label_fit_sensor) + DataType.TYPE_HEART_POINTS.getName();
                        if (appPrefs.getPrefByLabel(sLabel))
                            addFitSensorForDataType(DataType.TYPE_HEART_POINTS, fitOthers);
                    }else
                        Log.e(LOG_TAG, "SensorClient is NULL");
                }else{
                    Log.e(LOG_TAG, "gsa is NULL");
                }
            }
            if (interVal == 0){
                while (!bCancelNow){
                    Thread.sleep(1000);
                    if ((SystemClock.elapsedRealtime() - realNow) >= TimeUnit.MINUTES.toMillis(5))
                        bCancelNow = true;
                }
            }else
                while (!bCancelNow){
                    Thread.sleep(1000);
                    if ((SystemClock.elapsedRealtime() - realNow) >= TimeUnit.SECONDS.toMillis(interVal))
                        bCancelNow = true;
                }
            iRetVal = mStepSensorCount+mBPMSensorCount+mHumiditySensorCount+mPressureSensorCount+mTempSensorCount;
            if (bExisting){
                SDT.lastUpdated = endTime;
                workoutRepository.updateSensorDailyTotal(SDT);
                Log.e(LOG_TAG, "finally update " + type + " " + SDT.toString());
            }else {
                workoutRepository.insertSensorDailyTotal(SDT);
                Log.e(LOG_TAG, "finally insert " + type + " " + SDT.toString());
            }
        }catch (Exception e){
            e.printStackTrace();
            mSensorManager.unregisterListener(sensorEventListener);
            Data outputData = new Data.Builder()
                    .putInt(Constants.KEY_FIT_TYPE, type)
                    .putLong(Constants.KEY_FIT_TIME, SDT.lastUpdated)
                    .putString(Constants.KEY_FIT_USER, sUserID)
                    .putString(Constants.KEY_FIT_VALUE, e.getMessage())
                    .build();
            return Result.failure(outputData);
        }

        mSensorManager.unregisterListener(sensorEventListener);
        String sTag = SensorWorker.LOG_TAG + type;
        WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(sTag);
        Data outputData = new Data.Builder()
                .putInt(Constants.KEY_FIT_VALUE, mCurrentState)
                .putLong(Constants.KEY_FIT_TIME, SDT.lastUpdated)
                .putInt(Constants.KEY_FIT_TYPE, type)
                .putInt(Constants.KEY_RESULT, iRetVal)
                .putString(Constants.KEY_FIT_USER, sUserID)
                .build();
        return Result.success(outputData);
    }

    private class xSensorListener implements SensorEventListener {
        private final Processor peakDetProcessor = new PeakDetProcessor(Constants.DELTA);
        public xSensorListener(){   }
        @Override
        public void onSensorChanged(SensorEvent event) {
            int iType = event.sensor.getType();
            String sName = event.sensor.getName();
            Float val;
            String sValue;
            switch (iType) {
                case Sensor.TYPE_ACCELEROMETER:
                    // Log.d(LOG_TAG, "sensor accel " + event.values[0] + " 1 " + event.values[1] + " 2 " + event.values[2]);
                    boolean isRepetition = peakDetProcessor.processPoint(event.timestamp, event.values[0],
                            event.values[1], event.values[2]);

                    if (isRepetition) {
                        Log.w(LOG_TAG, "sensor accel  rep detected ");
                        //      sValue = getString(R.string.label_rep) + Constants.ATRACKIT_SPACE
                        //mMessagesViewModel.addDetectedRep();
                    }
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    //    Log.d(LOG_TAG, "sensor linear accel " + simpleDateFormat.format(new Date(event.timestamp)) + " : " +
                    //          event.values[0] + " 1 " + event.values[1] + " 2 " + event.values[2]);
                    boolean isRepetition2 = peakDetProcessor.processPoint(event.timestamp, event.values[0],
                            event.values[1], event.values[2]);

                    if (isRepetition2) {
                        //  sValue = getString(R.string.label_rep) + Constants.ATRACKIT_SPACE;
                        Log.w(LOG_TAG, "sensor linear accel  rep detected ");
                        // mMessagesViewModel.addDetectedRep();
                    }
                    break;
                case Sensor.TYPE_GYROSCOPE:
//                       Log.d(LOG_TAG, "sensor gyro " + simpleDateFormat.format(event.timestamp)
//                             + " : " + event.values[0] + " 1 " + event.values[1] + " 2 " + event.values[2]);
                    boolean isRepetition3 = peakDetProcessor.processPoint(event.timestamp, event.values[0],
                            event.values[1], event.values[2]);
                    if (isRepetition3) {
                        //  sValue = getString(R.string.label_rep) + Constants.ATRACKIT_SPACE;
                        Log.w(LOG_TAG, "sensor gyro  rep detected ");
                        //   mMessagesViewModel.addDetectedRep();
                    }
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    val = event.values[0];
                    int iVal = Math.round(val);
                    sValue = Integer.toString(iVal);
                    if (!Utilities.isInteger(sValue, 0)) return;
                    if (SDT.deviceStep == 0 || SDT.deviceStep != iVal){
                        mStepSensorCount++;
                        Log.w(LOG_TAG, "step detected " + sValue + " " + mStepSensorCount);
                        SDT.deviceStep = iVal;
                        bCancelNow = ((mStepSensorCount > 0) && (mBPMSensorCount > 0));
                    }
                    break;
                case Sensor.TYPE_HEART_RATE:
                    val = event.values[0];
                    if (!Float.isNaN(val)) {
                        sValue = Integer.toString(Math.round(val));
                        if (!Utilities.isInteger(sValue, 0)) return;
                        if (SDT.deviceBPM == 0 || (Math.round(SDT.deviceBPM) != Math.round(val))) {
                            mBPMSensorCount++;
                            Log.w(LOG_TAG, "bpm detected " + sValue + " " + mBPMSensorCount);
                            SDT.deviceBPM = val;
                            bCancelNow = ((mStepSensorCount > 0) && (mBPMSensorCount > 0));
                        }
                    }
                    break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    val = event.values[0];
                    if (!Float.isNaN(val)) {
                        sValue = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, val);
                        if (!Utilities.isFloat(sValue, 0)) return;
                        if (SDT.temperature == 0 || (Math.round(SDT.temperature) != Math.round(val))) {
                            mTempSensorCount++;
                            Log.w(LOG_TAG, "temp detected " + sValue + " " + mTempSensorCount);
                            SDT.temperature = val;
                            bCancelNow = ((mTempSensorCount > 0) && (mPressureSensorCount > 0) && (mHumiditySensorCount > 0));
                        }
                    }
                    break;
                case Sensor.TYPE_PRESSURE:
                    val = event.values[0];
                    if (!Float.isNaN(val)) {
                        sValue = Integer.toString(Math.round(val));
                        if (!Utilities.isFloat(sValue, 0)) return;
                        if (SDT.pressure == 0 || (Math.round(SDT.pressure) != Math.round(val))) {
                            mPressureSensorCount++;
                            Log.w(LOG_TAG, "hPA detected " + sValue + " " + mPressureSensorCount);
                            SDT.pressure = val;
                            bCancelNow = ((mTempSensorCount > 0) && (mPressureSensorCount > 0) && (mHumiditySensorCount > 0));
                        }
                    }
                    break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    val = event.values[0];
                    if (!Float.isNaN(val)) {
                        sValue = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, val);
                        if (!Utilities.isFloat(sValue, 0)) return;
                        if (SDT.humidity == 0 || (Math.round(SDT.humidity) != Math.round(val))) {
                            mHumiditySensorCount++;
                            Log.w(LOG_TAG, "humidity detected " + sValue + " " + mHumiditySensorCount);
                            SDT.humidity = val;
                            bCancelNow = ((mTempSensorCount > 0) && (mPressureSensorCount > 0) && (mHumiditySensorCount > 0));
                        }
                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(LOG_TAG, "sensor changed accuracy " + sensor.getName());
        }
    }

    private class xFITSensorListener implements OnDataPointListener {
        @Override
        public void onDataPoint(DataPoint dataPoint) {
            String sSuggested;
            float val;
            if (DataType.TYPE_HEART_RATE_BPM.equals(dataPoint.getDataType())) {
                val = dataPoint.getValue(Field.FIELD_BPM).asFloat();
                if (val > 0F) {
                    if (SDT.fitBPM == 0 || ((Math.round(SDT.fitBPM) != Math.round(val)))) {
                        sSuggested = Constants.ATRACKIT_EMPTY + val;
                        sSuggested = "google bpm" + sSuggested;
                        Log.e(LOG_TAG, sSuggested);
                        SDT.fitBPM = val;
                        bCancelNow = ((mStepSensorCount > 0) && (mBPMSensorCount > 0));
                    }else
                        Log.e(LOG_TAG, "same fit BPM");
                }
            }
            if (DataType.TYPE_MOVE_MINUTES.equals(dataPoint.getDataType())) {
                long moveDuration = dataPoint.getEndTime(TimeUnit.MILLISECONDS) - dataPoint.getStartTime(TimeUnit.MILLISECONDS);
                if (moveDuration > 0) {
                    sSuggested = Long.toString(TimeUnit.MILLISECONDS.toMinutes(moveDuration));

                    sSuggested = "google move min  " + sSuggested;
                    Log.e(LOG_TAG, sSuggested);
                }
            }
            if (DataType.TYPE_WORKOUT_EXERCISE.equals(dataPoint.getDataType())) {
                sSuggested = dataPoint.getValue(Field.FIELD_EXERCISE).asString() + " ";
                Log.e(LOG_TAG,"exercise " +  sSuggested);

            }
            if (DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(dataPoint.getDataType())
                    || DataType.TYPE_STEP_COUNT_DELTA.equals(dataPoint.getDataType())) {
                int iVal = dataPoint.getValue(Field.FIELD_STEPS).asInt();
                if (iVal > 0) {
                    if (SDT.fitStep == 0 || SDT.fitStep != iVal) {
                        sSuggested = Integer.toString(dataPoint.getValue(Field.FIELD_STEPS).asInt());
                        Log.e(LOG_TAG, "google step " + sSuggested);
                        SDT.fitStep = iVal;
                        bCancelNow = ((mStepSensorCount > 0) && (mBPMSensorCount > 0));
                    }else
                        Log.e(LOG_TAG, "same google step " + iVal);
                }
            }
            if (DataType.TYPE_POWER_SAMPLE.equals(dataPoint.getDataType())) {
                sSuggested = "watts " + dataPoint.getValue(Field.FIELD_WATTS).asFloat() + " ";
                Log.e(LOG_TAG,"power " +  sSuggested);
            }
            if (DataType.TYPE_LOCATION_SAMPLE.equals(dataPoint.getDataType())) {
                Location loc = new Location(LocationManager.GPS_PROVIDER);
                loc.setLatitude(dataPoint.getValue(Field.FIELD_LATITUDE).asFloat());
                loc.setLongitude(dataPoint.getValue(Field.FIELD_LONGITUDE).asFloat());
                if ((Math.abs(SDT.fitLat - loc.getLatitude()) > 0.001)
                        || (Math.abs(SDT.fitLat - loc.getLatitude()) > 0.001)){
                    SDT.fitLat = (float) loc.getLatitude();
                    SDT.fitLng = (float)loc.getLongitude();
                }
                Intent intent = new Intent(INTENT_LOCATION_UPDATE);
                intent.putExtra(INTENT_LOCATION_UPDATE, loc);
                intent.putExtra(INTENT_EXTRA_RESULT, (loc != null) ? 1 : 0);
                intent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                getApplicationContext().sendBroadcast(intent);
                //sSuggested = "xFIT location lat: " + dataPoint.getValue(Field.FIELD_LATITUDE).asFloat() + " long: " + dataPoint.getValue(Field.FIELD_LONGITUDE).asFloat();
                // Log.e(LOG_TAG, sSuggested);

            }else{
                String sDataType = dataPoint.getDataType().getName();
                sSuggested = sDataType + ": " + dataPoint.toString();
                Log.e(LOG_TAG, "other data point received: " + sSuggested);
            }
        }
    }

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
    private void addFitSensorForDataType(final DataType mDataType, final long sampleRate) {
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
                            SensorRequest request = new SensorRequest.Builder()
                                            .setDataSource(dataSource)
                                            .setDataType(dataSource.getDataType())
                                            .setSamplingRate(sampleRate, TimeUnit.SECONDS)
                                            .build();
                                mSensorsClient.add(request, mDataPointListener).addOnCompleteListener(
                                        new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.e(LOG_TAG, "adding FIT sensory " + sSource + " " + dataSource.getDataType().getName());
                                                } else {
                                                    Log.e(LOG_TAG, "Listener not registered." + sSource, task.getException());
                                                }
                                            }
                                        });
                            break;
                        }
                    } else {
                        Log.d(LOG_TAG, "No sensors for " + mDataType.toString());
                    }

                }
            });
        }
    }
    @NonNull
    private ForegroundInfo createForegroundInfo(@NonNull String progress) {
        // Build a notification using bytesRead and contentLength

        Context context = getApplicationContext();
        String id = Constants.ACTIVE_CHANNEL_ID;
        String title = context.getString(R.string.notification_title);
        String cancel = context.getString(R.string.action_cancel);
        // This PendingIntent can be used to cancel the worker
        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        Notification notification = new NotificationCompat.Builder(context, id)
                .setContentTitle(title)
                .setTicker(title)
                .setSmallIcon(R.drawable.ic_a_outlined)
                .setOngoing(true)
                // Add the cancel action to the notification which can
                // be used to cancel the worker
                .addAction(android.R.drawable.ic_delete, cancel, intent)
                .build();
        return new ForegroundInfo(Constants.NOTIFICATION_ACTIVE_ID,notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        // Create a Notification channel
        // Create a notification manager object.
        Context context = getApplicationContext();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.

        // Create the NotificationChannel with all the parameters.
        NotificationChannel notificationChannelActive = new NotificationChannel
                (ACTIVE_CHANNEL_ID,
                        context.getString(R.string.notify_channel_activity_name),
                        IMPORTANCE_DEFAULT);

        notificationChannelActive.enableLights(true);
        notificationChannelActive.setLightColor(Color.GREEN);
        notificationChannelActive.enableVibration(true);
        notificationChannelActive.setDescription
                (context.getString(R.string.notify_channel_activity_desc));
        notificationChannelActive.setShowBadge(true);
        notificationChannelActive.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationManager.createNotificationChannel(notificationChannelActive);

    }

    
}
