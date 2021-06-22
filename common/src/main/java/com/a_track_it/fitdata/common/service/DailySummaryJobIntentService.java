package com.a_track_it.fitdata.common.service;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.InjectorUtils;
import com.a_track_it.fitdata.common.R;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Configuration;
import com.a_track_it.fitdata.common.data_model.UserDailyTotalsWorker;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutRepository;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.a_track_it.fitdata.service.CustomIntentReceiver;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.a_track_it.fitdata.common.Constants.ATRACKIT_EMPTY;
import static com.a_track_it.fitdata.common.Constants.ATRACKIT_SPACE;
import static com.a_track_it.fitdata.common.Constants.INTENT_EXTRA_MSG;
import static com.a_track_it.fitdata.common.Constants.INTENT_HOME_REFRESH;
import static com.a_track_it.fitdata.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.fitdata.common.Constants.INTENT_SUMMARY_DAILY;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_ACTION;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_USER;
import static com.a_track_it.fitdata.common.Constants.MAP_HEART_POINTS;
import static com.a_track_it.fitdata.common.Constants.MAP_MOVE_MINS;
import static com.a_track_it.fitdata.common.Constants.MAP_STEPS;
import static com.a_track_it.fitdata.common.Constants.NOTIFICATION_MAINTAIN_ID;
import static com.a_track_it.fitdata.common.Constants.SUMMARY_CHANNEL_ID;

public class DailySummaryJobIntentService extends JobIntentService {
    private final static String LOG_TAG = DailySummaryJobIntentService.class.getSimpleName();

   // private static final String KEY_ACTION = "HistoryAction";
    private static final String GOAL_PREFIX = "goal.";
    private static final int READ_DAILY_TOTALS = 6;
    private static final long GOOGLE_WAIT_MAX = 60000L;
    private NotificationManager mNotifyManager;
    private ReferencesTools referencesTools;
    private Handler mHandler;
    private Bundle resultBundle;
    private WorkoutRepository repository;
    private ApplicationPreferences appPrefs;
    private String sUserID;
    private String sDeviceID;
    private boolean useNotification;
    private long mStartTime;
    private long mEndTime;
    private long sync_delay;
    private long last_sync;
    private Map<Long, Workout> map =  new HashMap<>();
    private Map<Long, Workout> mapDetected =  new HashMap<>();
    /**
     * Unique job ID for this service.
     */
    private static final int JOB_ID = 11;
    /**
     * Result receiver object to send results
     */
    private ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == 200 && (resultData != null)) {
                String sKeyWorkoutList = Workout.class.getSimpleName() + "_list";
                if (resultData.containsKey(Constants.MAP_CALORIES) || resultData.containsKey(Constants.MAP_DISTANCE) || resultData.containsKey(Constants.MAP_MOVE_MINS)
                        || resultData.containsKey(Constants.MAP_BPM_AVG) || resultData.containsKey(Constants.MAP_STEPS) || resultData.containsKey(Constants.MAP_WATTS)
                        || (resultData.containsKey(Constants.MAP_HEART_POINTS))
                        || resultData.containsKey(sKeyWorkoutList)) {
                        Log.w(LOG_TAG, "onReceiveResult ");
                        // existing total  -update
                        Data.Builder builder = new Data.Builder();
                        builder.putString(KEY_FIT_USER, sUserID);
                        builder.putString(KEY_FIT_DEVICE_ID, sDeviceID);
                        builder.putInt(KEY_FIT_TYPE, 0);
                        int iAdded = 0;
                        if (resultData.containsKey(Constants.MAP_MOVE_MINS)){
                            builder.putInt(Constants.MAP_MOVE_MINS, resultData.getInt(Constants.MAP_MOVE_MINS));
                            iAdded++;
                        }
                        if (resultData.containsKey(Constants.MAP_STEPS)){
                            builder.putInt(Constants.MAP_STEPS, resultData.getInt(Constants.MAP_STEPS));
                            iAdded++;
                        }
                        if (resultData.containsKey(Constants.MAP_DISTANCE)){
                            builder.putFloat(Constants.MAP_DISTANCE, resultData.getFloat(Constants.MAP_DISTANCE));
                            iAdded++;
                        }
                        if (resultData.containsKey(Constants.MAP_HEART_POINTS)){
                            builder.putFloat(Constants.MAP_HEART_POINTS, resultData.getFloat(Constants.MAP_HEART_POINTS));
                            iAdded++;
                        }
                        if (resultData.containsKey(Constants.MAP_HEART_DURATION)){
                            builder.putInt(Constants.MAP_HEART_DURATION, resultData.getInt(Constants.MAP_HEART_DURATION));
                            iAdded++;
                        }
                        if (resultData.containsKey(Constants.MAP_CALORIES)){
                            builder.putFloat(Constants.MAP_CALORIES, resultData.getFloat(Constants.MAP_CALORIES));
                            iAdded++;
                        }
                        if (resultData.containsKey(Constants.MAP_BPM_MIN)) {
                            builder.putFloat(Constants.MAP_BPM_MIN,resultData.getFloat(Constants.MAP_BPM_MIN));
                            iAdded++;
                        }
                        if (resultData.containsKey(Constants.MAP_BPM_MAX)){
                            builder.putFloat(Constants.MAP_BPM_MAX,resultData.getFloat(Constants.MAP_BPM_MAX));
                            iAdded++;
                        }
                        if (resultData.containsKey(Constants.MAP_BPM_AVG)){
                            builder.putFloat(Constants.MAP_BPM_AVG,resultData.getFloat(Constants.MAP_BPM_AVG));
                            iAdded++;
                        }
                        if (resultData.containsKey(Constants.MAP_SPEED_MIN)){
                            builder.putFloat(Constants.MAP_SPEED_MIN,resultData.getFloat(Constants.MAP_SPEED_MIN));
                            iAdded++;
                        }
                        if (resultData.containsKey(Constants.MAP_SPEED_MAX)) builder.putFloat(Constants.MAP_SPEED_MAX,resultData.getFloat(Constants.MAP_SPEED_MAX));
                        if (resultData.containsKey(Constants.MAP_SPEED_AVG)) builder.putFloat(Constants.MAP_SPEED_AVG,resultData.getFloat(Constants.MAP_SPEED_AVG));
                        Log.e(LOG_TAG, "added metrics " + iAdded);

                        if (resultData.containsKey(sKeyWorkoutList)) {
                            ArrayList<Workout> list = resultData.getParcelableArrayList(sKeyWorkoutList);
                            int iSize = list.size();
                            if (iSize > 0) {
                                Gson gson = new Gson();
                                String sList = gson.toJson(list);
                                if (sList.length() > 0)
                                    builder.putString(Workout.class.getSimpleName() + "_list", sList);
                            }
                        }
                        if (useNotification) sendNotification(resultData);

                        OneTimeWorkRequest workRequest =
                                new OneTimeWorkRequest.Builder(UserDailyTotalsWorker.class)
                                        .setInputData(builder.build())
                                        .build();
                        WorkManager  mWorkManager = WorkManager.getInstance(getApplicationContext());
                        mWorkManager.enqueue(workRequest);

                }
                if (resultData.containsKey(sKeyWorkoutList)) {
                        ArrayList<Workout> list = new ArrayList<>(resultData.getParcelableArrayList(sKeyWorkoutList));
                        for (Workout workout : list) {
                            if (Utilities.isDetectedActivity(workout.activityID)) {
                                if (workout._id == 0)
                                    workout._id = mStartTime + workout.activityID;
                                if (workout.start == 0) workout.start = mStartTime;
                                if (workout.end == 0) workout.end = mEndTime;
                                workout.userID = sUserID;
                                //workout.deviceID = sDeviceID;
                                Workout w = repository.getWorkoutByIdNow(workout._id,workout.userID, ATRACKIT_EMPTY);
                                if(mapDetected.get(workout.activityID) == null) {
                                    mapDetected.put(workout.activityID, workout);
                                    String s = "insert";
                                    try{
                                        if (w == null)
                                            repository.insertWorkout(workout);
                                        else {
                                            s = "update";
                                            repository.updateWorkout(workout);
                                        }
                                    }catch (Exception e){
                                        Log.e(LOG_TAG, "error doing " + s + " " + workout.toString());
                                        e.printStackTrace();
                                    }
                                }
                                else {
                                    Workout existingWorkout = mapDetected.get(workout.activityID);
                                    boolean doUpdate = false;
                                    if (workout.duration != existingWorkout.duration)
                                        doUpdate = true;
                                    else if (workout.stepCount != existingWorkout.stepCount)
                                        doUpdate = true;
                                    try{
                                        if (doUpdate)
                                            repository.updateWorkout(workout);
                                    }catch (Exception e){
                                        Log.e(LOG_TAG, "error updating " + workout.toString());
                                        e.printStackTrace();
                                    }
                                    mapDetected.replace(workout.activityID, workout);
                                }
                            }
                        }

                }
            }
        }
    };
    private OnCompleteListener<DataSet> totalsListener = (OnCompleteListener<DataSet>) task -> {
        try {
            if (task.isSuccessful()) {
                DataSet dataSet = task.getResult();
                String sType = dataSet.getDataType().getName();
                //    DateFormat dateFormat = getTimeInstance();
                if (dataSet.isEmpty()) {
                    Log.w(LOG_TAG, "empty daily " + sType);
                } else {
                    List<DataPoint> dpList = dataSet.getDataPoints();
                    if (sType.equals(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName()) || sType.equals(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
                        for (DataPoint dp : dpList) {
                            Bundle bundle = DataPointToResultBundle(dp);
                            Workout workout = bundle.getParcelable(dp.getDataType().getName());
                            if ((workout != null) && (workout.activityID != null))
                                if (map.get(workout.activityID) == null) {
                                    map.put(workout.activityID, workout);
                                } else {
                                    map.replace(workout.activityID, workout);
                                }
                        }
                    } else {
                        for (DataPoint dp : dpList) {
                            Bundle bundle = DataPointToResultBundle(dp);
                            resultBundle.putAll(bundle);
                        }
                    }
                }
            }
        }catch (Exception e){
            Log.e(LOG_TAG,"totalsListener error " + e.getMessage());
        }
    };
    // [end of totalsListener OnComplete]
    private OnSuccessListener<DataReadResponse> readSuccessor = (OnSuccessListener<DataReadResponse>) task -> {
     try {
         int iRet = task.getBuckets().size();
         if (iRet > 0) {
             for (Bucket bucket : task.getBuckets()) {
                 List<DataSet> dataSets = bucket.getDataSets();
                 for (DataSet dataSet : dataSets) {
                     List<DataPoint> dpList = dataSet.getDataPoints();
                     for (DataPoint dp : dpList) {
                         Bundle dpBundle = DataPointToResultBundle(dp);
                         resultBundle.putAll(dpBundle);
                     }
                 }
             }
         }
         iRet = task.getDataSets().size();
         if (iRet > 0)
             for (DataSet dataSet : task.getDataSets()) {
                 List<DataPoint> dpList = dataSet.getDataPoints();
                 for (DataPoint dp : dpList) {
                     resultBundle.putAll(DataPointToResultBundle(dp));
                 }
             }
     }catch (Exception e){
         Log.e(LOG_TAG,"readSuccessor error " + e.getMessage());
     }
    };
    private OnCompleteListener<DataReadResponse> readCompleter = (OnCompleteListener<DataReadResponse>) task -> {
        try{
            if (task.getResult().getStatus().isSuccess()) {
                int iRet = task.getResult().getBuckets().size();
                if (iRet > 0) {
                    for (Bucket bucket : task.getResult().getBuckets()) {
                        List<DataSet> dataSets = bucket.getDataSets();
                        for (DataSet dataSet : dataSets) {
                            List<DataPoint> dpList = dataSet.getDataPoints();
                            for (DataPoint dp : dpList) {
                                Bundle dpBundle = DataPointToResultBundle(dp);
                                resultBundle.putAll(dpBundle);
                            }
                        }
                    }
                }
                iRet = task.getResult().getDataSets().size();
                if (iRet > 0)
                    for (DataSet dataSet : task.getResult().getDataSets()) {
                        List<DataPoint> dpList = dataSet.getDataPoints();
                        for (DataPoint dp : dpList) {
                            resultBundle.putAll(DataPointToResultBundle(dp));
                        }
                    }
            }else{
                Exception ex = task.getException();
                Log.e(LOG_TAG,"readCompleter task exception " + ex.getMessage());
/*                if (ex != null){
                    FirebaseCrashlytics.getInstance().recordException(ex);
                    if (ex instanceof ResolvableApiException){
                        PendingIntent pendingIntent = ((ResolvableApiException) ex).getResolution();
                        if (pendingIntent != null){
                            String sTitle = "Click to continue Google refresh";
                            String sContent = "Unable to continue - click to resolve";
                            if (ex.getLocalizedMessage() != null) sContent = ex.getLocalizedMessage();
                            buildPendingIntentNotification(sTitle, sContent, pendingIntent);
                        }
                    }
                    if (ex instanceof ApiException) {
                        int resultCode = ((ApiException) ex).getStatusCode();
                        if (resultCode == GoogleSignInStatusCodes.SIGN_IN_REQUIRED) { // sign-in required
                            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(Constants.CLIENT_ID)
                                    .requestEmail().requestId()
                                    .build();
                            // Build a GoogleSignInClient with the options specified by gso.
                            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
                            Intent signInIntent = googleSignInClient.getSignInIntent();
                            PendingIntent mPhoneSyncPendingIntent = PendingIntent.getActivity(getApplicationContext(), 5005, signInIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                            String sTitle = "Click to continue Google refresh";
                            String sContent = "Unable to continue - click to resolve";
                            buildPendingIntentNotification(sTitle, sContent, mPhoneSyncPendingIntent);
                        }
                    }
                }*/
            }
        }catch (Exception e){
            Log.e(LOG_TAG,"readCompleter error " + e.getMessage());
       //     FirebaseCrashlytics.getInstance().recordException(e);
/*            if (e instanceof ResolvableApiException){
                PendingIntent pendingIntent = ((ResolvableApiException) e).getResolution();
                if (pendingIntent != null){
                    String sTitle = "Click to continue Google refresh";
                    String sContent = "Unable to continue - click to resolve";
                    if (e.getLocalizedMessage() != null) sContent = e.getLocalizedMessage();
                    buildPendingIntentNotification(sTitle, sContent, pendingIntent);
                }
            }
            if (e instanceof RuntimeException) {
                Exception ex = task.getException();
                if (ex instanceof ApiException) {
                    int resultCode = ((ApiException) ex).getStatusCode();
                    if (resultCode == GoogleSignInStatusCodes.SIGN_IN_REQUIRED) { // sign-in required
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(Constants.CLIENT_ID)
                                .requestEmail().requestId()
                                .build();
                        // Build a GoogleSignInClient with the options specified by gso.
                        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
                        Intent signInIntent = googleSignInClient.getSignInIntent();
                        PendingIntent mPhoneSyncPendingIntent = PendingIntent.getActivity(getApplicationContext(), 5005, signInIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        String sTitle = "Click to continue Google refresh";
                        String sContent = "Unable to continue - click to resolve";
                        buildPendingIntentNotification(sTitle, sContent, mPhoneSyncPendingIntent);
                    }
                }
            }*/
        }
    };
    // [end of readCompleter OnComplete]

    public DailySummaryJobIntentService() {
        super();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        this.referencesTools = ReferencesTools.getInstance();
        Context context = getApplicationContext();
        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        this.repository = InjectorUtils.getWorkoutRepository((Application)context);

        this.appPrefs = ApplicationPreferences.getPreferences(context);
        this.resultBundle = new Bundle();
        this.sync_delay = appPrefs.getDailySyncInterval();
        this.last_sync = appPrefs.getLastDailySync();
        this.sDeviceID = ATRACKIT_EMPTY;
        this.sUserID = ATRACKIT_EMPTY;
        mHandler = new Handler();
        resultBundle.putInt(KEY_FIT_ACTION, READ_DAILY_TOTALS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNotifyManager != null) mNotifyManager.cancel(NOTIFICATION_MAINTAIN_ID);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final Context context = getApplicationContext();
        String sUser = ATRACKIT_EMPTY;
        int iForceOveride = 0;
        this.referencesTools.init(context);
        if (intent.hasExtra(Constants.KEY_FIT_USER) && intent.hasExtra(KEY_FIT_DEVICE_ID) && appPrefs.getAppSetupCompleted()) {
            sUser = intent.getStringExtra(Constants.KEY_FIT_USER);
            if (sUser == null || sUser.length() == 0) {
                Log.e(LOG_TAG, "NO sUserID " + sUser);
                mResultReceiver.send(504, null);
                return;            }
            if (!sUser.equals(appPrefs.getLastUserID()) || (appPrefs.getLastUserID().length() == 0)) {
                Log.e(LOG_TAG, "mismatch sUserID " + sUser + " appPrefs " + appPrefs.getLastUserID());
                mResultReceiver.send(505, null);
                return;
            }
            this.sDeviceID = intent.getStringExtra(KEY_FIT_DEVICE_ID);
            String sTemp = ATRACKIT_EMPTY;
            List<Configuration> listConfigs = repository.getConfigLikeName(Constants.KEY_DEVICE1, sUser);
            if (listConfigs.size() > 0) sTemp = listConfigs.get(0).stringValue2;
            if ((sTemp == null || sDeviceID == null) || !sDeviceID.equals(sTemp)){
                mResultReceiver.send(401, null);
                return;
            }
        }else {
            mResultReceiver.send(401, null);
            return;
        }
        if ((sync_delay == 0) || !appPrefs.getAppSetupCompleted()){
            mResultReceiver.send(200, null);
            return;  // turned off
        }
        sUserID = sUser;
        UserPreferences userPrefs = UserPreferences.getPreferences(context, sUserID);
        useNotification = userPrefs.getPrefByLabel(Constants.USER_PREF_USE_NOTIFICATION);
        Log.e(LOG_TAG, "useNotification is set to " + useNotification + " u " + sUserID);
        if (intent.hasExtra(KEY_FIT_TYPE))
            iForceOveride = intent.getIntExtra(KEY_FIT_TYPE, 0);
        long timeNow = System.currentTimeMillis();

        // within 5 seconds
        if ((((timeNow - last_sync) + 5000) < sync_delay) && (iForceOveride == 0)) {
            Log.w(LOG_TAG, "returning without force too soon " + (timeNow-last_sync) + " duration " + Utilities.getDurationBreakdown((timeNow-last_sync)));
            mResultReceiver.send(200, null);
            return;
        }
        GoogleSignInAccount gsa = null;
        if (intent.hasExtra(Constants.KEY_FIT_VALUE)) {
            gsa = (intent.getParcelableExtra(Constants.KEY_FIT_VALUE));
            if (gsa != null && gsa.isExpired()) {
                Log.e(LOG_TAG, "pickup acct is expired " + gsa.getDisplayName());
                gsa = null;
            }else
               if (gsa != null) Log.e(LOG_TAG, "pickup acct " + gsa.getDisplayName());
        }
        if (gsa == null) {
            FitnessOptions fo = referencesTools.getFitnessSignInOptions(0); // read and aggregates
            gsa = GoogleSignIn.getAccountForExtension(context,fo);
            Log.e(LOG_TAG, "gsa null now set for extensions " + gsa.getDisplayName());
        }
        if (gsa == null){
            Log.e(LOG_TAG, "gsa is null ");
            mResultReceiver.send(400, null);
            return;
        }

        CustomIntentReceiver.cancelAlarm(context);
        if (this.useNotification) startWorkingNotification(); // busy now
        Configuration config = new Configuration(DataType.TYPE_HEART_POINTS.getName(),sUserID,Constants.ATRACKIT_EMPTY, 0L, null,null);
        List<Configuration> heartGoalList = this.repository.getConfigs(config,sUserID);
        if ((heartGoalList != null) && (heartGoalList.size() > 0)){
            Configuration c = heartGoalList.get(0);
            resultBundle.putString(GOAL_PREFIX + c.stringName,c.stringValue);
        }
        config.stringName = DataType.TYPE_STEP_COUNT_DELTA.getName();
        List<Configuration> stepGoalList = this.repository.getConfigs(config,sUserID);
        if ((stepGoalList != null) && (stepGoalList.size() > 0)){
            Configuration c = stepGoalList.get(0);
            resultBundle.putString(GOAL_PREFIX + c.stringName,c.stringValue);
        }
        config.stringName = DataType.TYPE_MOVE_MINUTES.getName();
        List<Configuration> activeGoalList = this.repository.getConfigs(config,sUserID);
        if ((activeGoalList != null) && (activeGoalList.size() > 0)){
            Configuration c = activeGoalList.get(0);
            resultBundle.putString(GOAL_PREFIX + c.stringName,c.stringValue);
        }
        config.stringName = DataType.TYPE_CALORIES_EXPENDED.getName();
        List<Configuration> calGoalList = this.repository.getConfigs(config,sUserID);
        if ((calGoalList != null) && (calGoalList.size() > 0)){
            Configuration c = calGoalList.get(0);
            resultBundle.putString(GOAL_PREFIX + c.stringName,c.stringValue);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        Long endTime = cal.getTimeInMillis();
        mEndTime = endTime;
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND, 0);
        try {
            if (gsa != null){
                FitnessOptions fitnessOptions = referencesTools.getFitnessSignInOptions(0);
                if (!GoogleSignIn.hasPermissions(gsa, fitnessOptions)){
                    resultBundle.putString(Constants.INTENT_DAILY, INTENT_SUMMARY_DAILY);
                    resultBundle.putString(Constants.KEY_FIT_USER, gsa.getId());
                    Log.e(LOG_TAG,"gsa NO PERMISSIONS " + gsa.getDisplayName());
                    mResultReceiver.send(504, resultBundle);
                    return;
                }
                if (gsa.isExpired()) {
                    Log.e(LOG_TAG,"gsa expired " + gsa.getDisplayName());
                    gsa = GoogleSignIn.getAccountForExtension(context, fitnessOptions);
                }
            }
            DataReadRequest heartHourRequest = queryBPMSummaryByHour(cal.getTimeInMillis(),mEndTime);
            Task<DataReadResponse> bpmResponse = Fitness.getHistoryClient(context,gsa).readData(heartHourRequest);
            bpmResponse.addOnSuccessListener(readSuccessor);
            //bpmResponse.addOnCompleteListener(readCompleter);
            DataReadRequest speedReadRequest = new DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_SPEED)
                    .bucketByTime(1, TimeUnit.HOURS)
                    .setTimeRange(cal.getTimeInMillis(), endTime, TimeUnit.MILLISECONDS)
                    .enableServerQueries() // Used to retrieve data from cloud
                    .build();
            Task<DataReadResponse> speedResponse = Fitness.getHistoryClient(context, gsa)
                    .readData(speedReadRequest);
            speedResponse.addOnSuccessListener(readSuccessor);
            //speedResponse.addOnCompleteListener(readCompleter);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            Long startTime = cal.getTimeInMillis();
            mStartTime = startTime;
            DataReadRequest stepCountRequest = queryStepCount(startTime, endTime);
            Task<DataReadResponse> step2Response = Fitness.getHistoryClient(context, gsa)
                    .readData(stepCountRequest);
            step2Response.addOnSuccessListener(readSuccessor);
            //step2Response.addOnCompleteListener(readCompleter);
            Task<DataSet> stepResponse = Fitness.getHistoryClient(context, gsa)
                    .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA);
            stepResponse.addOnCompleteListener(totalsListener);
            Task<DataSet> activityResponse = Fitness.getHistoryClient(context, gsa)
                    .readDailyTotal(DataType.TYPE_ACTIVITY_SEGMENT);
            activityResponse.addOnCompleteListener(totalsListener);
            Task<DataSet> distanceResponse = Fitness.getHistoryClient(context, gsa)
                    .readDailyTotal(DataType.TYPE_DISTANCE_DELTA);
            distanceResponse.addOnCompleteListener(totalsListener);
            Task<DataSet> caloriesResponse = Fitness.getHistoryClient(context, gsa)
                    .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED);
            caloriesResponse.addOnCompleteListener(totalsListener);
            Task<DataSet> moveResponse = Fitness.getHistoryClient(context, gsa)
                    .readDailyTotal(DataType.TYPE_MOVE_MINUTES);
            moveResponse.addOnCompleteListener(totalsListener);
            Task<DataSet> heartResponse = Fitness.getHistoryClient(context, gsa)
                    .readDailyTotal(DataType.TYPE_HEART_POINTS);
            heartResponse.addOnCompleteListener(totalsListener);
            final String SID = sUserID;  final GoogleSignInAccount gAcct = gsa;
            Tasks.whenAllComplete(activityResponse, bpmResponse, stepResponse, speedResponse,
                    distanceResponse, caloriesResponse, moveResponse, heartResponse)
                    .addOnCompleteListener(task -> {
                        if (this.useNotification) mNotifyManager.cancel(NOTIFICATION_MAINTAIN_ID);
                        if (task.isSuccessful()){
                            appPrefs.setLastDailySync(System.currentTimeMillis());
                            CustomIntentReceiver.setAlarm(context, false, sync_delay, sUserID,(Parcelable)gAcct);
                            // collect workout summaries together into an array
                            if (!map.isEmpty()){
                                ArrayList<Workout> list = new ArrayList<>();
                                for(Map.Entry<Long, Workout> entry: map.entrySet()){
                                    list.add(entry.getValue());
                                }
                                if (list.size() > 0) {
                                    Log.e(LOG_TAG, "map not empty "  + list.size());
                                    String sKey = Workout.class.getSimpleName() + "_list";
                                    resultBundle.putParcelableArrayList(sKey, list);
                                }
                            }
                            resultBundle.putString(Constants.INTENT_DAILY, INTENT_SUMMARY_DAILY);
                            resultBundle.putString(Constants.KEY_FIT_USER, SID);
                            mResultReceiver.send(200, resultBundle);
                        }
                        else {
                            Exception e = task.getException();
                            if (e != null) {
                                e.printStackTrace();
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                            Log.e(LOG_TAG,"all tasks complete error " + e.getMessage());
                            mResultReceiver.send(505, null);
                        }

                    }).addOnFailureListener(e -> {
                        if (this.useNotification) mNotifyManager.cancel(NOTIFICATION_MAINTAIN_ID);
                    mResultReceiver.send(505, null);
            });

        } catch (Exception e) {
                e.printStackTrace();
             //   FirebaseCrashlytics.getInstance().recordException(e);
                String sMsg = e.getMessage();
                Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                msgIntent.setAction(INTENT_MESSAGE_TOAST);
                msgIntent.putExtra(INTENT_EXTRA_MSG, sMsg);
                msgIntent.putExtra(KEY_FIT_TYPE, 2);
                msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                sendBroadcast(msgIntent);
                repository.destroyInstance();
                Log.e(LOG_TAG, "DailySummaryJobIntentService ERROR " + sMsg);
                mResultReceiver.send(505, resultBundle);
           // }
        }
        repository.destroyInstance();
    }

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, DailySummaryJobIntentService.class, JOB_ID, intent);
    }

    private Bundle DataPointToResultBundle(DataPoint dp){
        Bundle resultBundle = new Bundle();
        if (dp.getDataType().getName().equals(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())) {
            Workout workout = new Workout();
            workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
            workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
            workout.identifier = referencesTools.getFitnessActivityIdentifierById(workout.activityID);
            workout.scoreTotal = dp.getValue(Field.FIELD_NUM_SEGMENTS).asInt();
            workout.duration = (long)dp.getValue(Field.FIELD_DURATION).asInt();
            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            if (startTime == 0)
                startTime = mStartTime;
            workout.start = startTime;
            long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
            if (endTime > 0 ) workout.end = endTime; else workout.end = mEndTime;
            workout._id = workout.start;
            workout.userID = sUserID;
            if ((dp.getOriginalDataSource() != null) && (dp.getOriginalDataSource().getAppPackageName() != null)
                    && (dp.getOriginalDataSource().getAppPackageName().length() > 0))
                workout.packageName = dp.getOriginalDataSource().getAppPackageName();
            else {
                if (Utilities.isDetectedActivity(workout.activityID))
                    workout.packageName = Constants.ATRACKIT_PLAY_CLASS;
                else
                    workout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
            }
            // Log.d(LOG_TAG, "activity.summary " + workout.activityName + " " + workout.setCount + " duration " + workout.duration);
            resultBundle.putParcelable(dp.getDataType().getName(), workout);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
            WorkoutSet set = new WorkoutSet();
            try {
                long timeStamp = dp.getTimestamp(TimeUnit.MILLISECONDS);
                set.duration = (long) dp.getValue(Field.FIELD_DURATION).asInt();
                if ((timeStamp > 0) && (set.duration > 0)){
                    set.start = timeStamp;
                    set.end = timeStamp + set.duration;
                }
            }catch (Exception e){
                Log.e(LOG_TAG, "set load error " + e.getMessage());
            }
            if (set.start == 0) {
                set.start = dp.getStartTime(TimeUnit.MILLISECONDS);
                if (dp.getEndTime(TimeUnit.MILLISECONDS) > 0) {
                    set.end = dp.getEndTime(TimeUnit.MILLISECONDS);
                    set.duration = set.end - set.start;
                }
            }
            if (set.start > 0) set._id = (set.start); else set._id = System.currentTimeMillis();
            String sExercise = dp.getValue(Field.FIELD_EXERCISE).asString();
            set.exerciseName = sExercise;
            set.per_end_xy = sExercise;
            set.resistance_type = (long)dp.getValue(Field.FIELD_RESISTANCE_TYPE).asInt();
            set.weightTotal = dp.getValue(Field.FIELD_RESISTANCE).asFloat();
            set.repCount = dp.getValue(Field.FIELD_REPETITIONS).asInt();
            if ((set.weightTotal > 0) && (set.repCount > 0))
                set.wattsTotal = (set.weightTotal * set.repCount);
            set.activityName = referencesTools.getFitnessActivityTextById(Constants.WORKOUT_TYPE_STRENGTH);
            set.activityID = Constants.WORKOUT_TYPE_STRENGTH;
            if ((dp.getOriginalDataSource() != null) && (dp.getOriginalDataSource().getAppPackageName().length() > 0))
                set.score_card = dp.getOriginalDataSource().getAppPackageName();
            else
                set.score_card = Constants.ATRACKIT_ATRACKIT_CLASS;
        //    Log.d(LOG_TAG, "dp " + set.shortText());
            resultBundle.putParcelable(WorkoutSet.class.getSimpleName(), set);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
            Workout workout = new Workout();
            workout.identifier = dp.getDataType().getName();
            workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
            workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
            workout.identifier = referencesTools.getFitnessActivityIdentifierById(workout.activityID);
          //  Log.d(LOG_TAG, "dp activity segment " + workout.activityID + " " + workout.activityName);
            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            if (startTime == 0)
                startTime = mStartTime;
            workout.start = startTime;
            long endTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
            if (endTime == 0 || (endTime == startTime)) dp.getEndTime(TimeUnit.MILLISECONDS);
            if (endTime > 0 ) workout.end = endTime;
            if (workout.end > 0 && workout.start > 0)
                workout.duration = workout.end - workout.start;
            workout._id = workout.start + workout.activityID;
            if ((dp.getOriginalDataSource() != null) && (dp.getOriginalDataSource().getAppPackageName().length() > 0))
                workout.packageName = dp.getOriginalDataSource().getAppPackageName();
            else {
                if (Utilities.isDetectedActivity(workout.activityID))
                    workout.packageName = Constants.ATRACKIT_PLAY_CLASS;
                else
                    workout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
            }
            resultBundle.putParcelable(dp.getDataType().getName(), workout);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
            float watts = dp.getValue(Field.FIELD_WATTS).asFloat();
            if (watts > 0F) {
                Workout workout = new Workout();
                workout.identifier = dp.getDataType().getName();
                workout.wattsTotal = watts;
               // Log.d(LOG_TAG, "dp power sample " + watts);
                resultBundle.putParcelable(Constants.MAP_WATTS, workout);
            }
        } else if (dp.getDataType().getName().equals(DataType.TYPE_STEP_COUNT_DELTA.getName())) {
            Integer steps = ( dp.getValue(Field.FIELD_STEPS) != null) ? dp.getValue(Field.FIELD_STEPS).asInt() : 0;
            if (steps > 0){
                resultBundle.putInt(MAP_STEPS, steps);
                resultBundle.putString(Constants.MAP_DATA_TYPE, dp.getDataType().getName());
            }
       } else if (dp.getDataType().getName().equals(DataType.TYPE_MOVE_MINUTES.getName())) {
            Integer minutes = ( dp.getValue(Field.FIELD_DURATION) != null) ? dp.getValue(Field.FIELD_DURATION).asInt() : 0;
            if (minutes > 0){
                resultBundle.putInt(Constants.MAP_MOVE_MINS, minutes);
                resultBundle.putString(Constants.MAP_DATA_TYPE, dp.getDataType().getName());
            }
        } else if (dp.getDataType().getName().equals(DataType.TYPE_CALORIES_EXPENDED.getName())) {
            float calories = dp.getValue(Field.FIELD_CALORIES).asFloat();
            resultBundle.putFloat(Constants.MAP_CALORIES, calories);
        } else if (dp.getDataType().getName().contains(DataType.TYPE_DISTANCE_DELTA.getName())) {
            float distance = dp.getValue(Field.FIELD_DISTANCE).asFloat();
            if (distance > 0F){
                resultBundle.putFloat(Constants.MAP_DISTANCE, distance);
            }
        } else if (dp.getDataType().getName().equals(DataType.AGGREGATE_HEART_POINTS.getName())
            || dp.getDataType().getName().equals(DataType.TYPE_HEART_POINTS.getName())) {
            try {
                float points = dp.getValue(Field.FIELD_INTENSITY).asFloat();
                int duration = dp.getValue(Field.FIELD_DURATION).asInt();
                resultBundle.putFloat(Constants.MAP_HEART_POINTS, points);
                resultBundle.putInt(Constants.MAP_HEART_DURATION, duration);
            }catch (Exception e){
                Log.w(LOG_TAG, "error " + e.getMessage());
            }
        } else if (dp.getDataType().getName().contains("step_count")) {
            int steps = dp.getValue(Field.FIELD_STEPS).asInt();
            if (steps > 0) {
                resultBundle.putInt(Constants.MAP_STEPS, steps);
            }
        }else if (dp.getDataType().getName().equals(DataType.AGGREGATE_HEART_RATE_SUMMARY.getName())) {
            float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
            float min = dp.getValue(Field.FIELD_MIN).asFloat();
            float max = dp.getValue(Field.FIELD_MAX).asFloat();
            if (avg > 0F) resultBundle.putFloat(Constants.MAP_BPM_AVG, avg);
            if (min > 0F) resultBundle.putFloat(Constants.MAP_BPM_MIN, min);
            if (max > 0F) resultBundle.putFloat(Constants.MAP_BPM_MAX, max);
           // Log.e(LOG_TAG, "heart BPM " + avg + " " + min + " " + max);
        } else if (dp.getDataType().getName().equals(DataType.AGGREGATE_SPEED_SUMMARY.getName())) {
            float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
            float min = dp.getValue(Field.FIELD_MIN).asFloat();
            float max = dp.getValue(Field.FIELD_MAX).asFloat();
            if (avg > 0F) resultBundle.putFloat(Constants.MAP_SPEED_AVG, avg);
            if (min > 0F) resultBundle.putFloat(Constants.MAP_SPEED_MIN, min);
            if (max > 0F) resultBundle.putFloat(Constants.MAP_SPEED_MAX, max);
        }
        return resultBundle;
    }

    private void buildPendingIntentNotification(String sTitle, String sContent, PendingIntent pendingIntent){
        Context context = getApplicationContext();
        int notificationID = Constants.NOTIFICATION_SUMMARY_ID;
        Bitmap bitmap = null;
        PackageManager pm = context.getPackageManager();
        boolean bWear = pm.hasSystemFeature(PackageManager.FEATURE_WATCH);
        //Drawable home = ContextCompat.getDrawable(context,R.drawable.ic_launcher_home);
        try {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_home); // vectorToBitmap(home);
        }catch (Exception ee){
            Log.e(LOG_TAG, "load bitmap error " + ee.getMessage());
        }
        if (bitmap == null){
            Log.e(LOG_TAG, "loading bitmap is null" );
        }
        IconCompat bubbleIcon = IconCompat.createWithResource(context, R.drawable.ic_a_outlined);
        NotificationCompat.BubbleMetadata metaBubble = new NotificationCompat.BubbleMetadata.Builder().setAutoExpandBubble(true)
                .setIntent(pendingIntent).setIcon(bubbleIcon).build();
        // Build the notification with all of the parameters.
        NotificationCompat.Builder notifyBuilder = new NotificationCompat
                .Builder(context, SUMMARY_CHANNEL_ID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(sContent).setBigContentTitle(sTitle))
                .setBubbleMetadata(metaBubble)
                .setSmallIcon(R.drawable.ic_a_outlined)
                .setLargeIcon(bitmap)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        if (bWear){
            NotificationCompat.Action actionView = new NotificationCompat.Action.Builder(R.drawable.ic_launch, sTitle, pendingIntent).build();
            NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender().addAction(actionView);
            notifyBuilder.extend(extender);
        }
        if (mNotifyManager != null)
            mNotifyManager.notify(notificationID, notifyBuilder.build());
    }

    private void sendNotification(Bundle dataBundle){
        int notificationID = Constants.NOTIFICATION_SUMMARY_ID;
        Context context = getApplicationContext();
        Calendar mCalendar = Calendar.getInstance(Locale.getDefault());
        String sTime = Utilities.getTimeString(mCalendar.getTimeInMillis());
        PackageManager pm = context.getPackageManager();
        boolean bWear = pm.hasSystemFeature(PackageManager.FEATURE_WATCH);
        String sClassName = (bWear) ? "com.a_track_it.fitdata.activity.RoomActivity" : "com.a_track_it.fitdata.activity.MainActivity";
        try{
            Intent notificationIntent = new Intent(context, Class.forName(sClassName));
            notificationIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
            notificationIntent.setAction(INTENT_SUMMARY_DAILY);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            notificationIntent.putExtra(KEY_FIT_TYPE, 1);
            notificationIntent.putExtra(Constants.KEY_FIT_USER, sUserID);
            PendingIntent notificationPendingIntent = PendingIntent.getActivity
                    (getApplicationContext(), notificationID, notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);


            String sChannelID = SUMMARY_CHANNEL_ID;
            String sTitle = getString(R.string.note_summary_title2); // String.format(Locale.getDefault(),getString(R.string.note_summary_title), sTime);
            String sContent = ATRACKIT_EMPTY; String sTemp;
            String sDesc;
            String sKey = GOAL_PREFIX + DataType.TYPE_STEP_COUNT_DELTA.getName();
            try {
                int steps = dataBundle.getInt(MAP_STEPS);
                if (dataBundle.containsKey(sKey)) {
                    sTemp = dataBundle.getString(sKey, "0.0");
                    int goalSteps = Math.round(Float.parseFloat(sTemp));
                    if (goalSteps > 0){
                        float percentGoal = (steps > 0) ? ((float)steps/goalSteps)*100 : 0F;
                        sContent = String.format(Locale.getDefault(),getString(R.string.note_summary_steps_goals), steps, goalSteps, percentGoal);
                    }
                }else
                    sContent = String.format(Locale.getDefault(),getString(R.string.note_summary_steps), steps);

                sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(),getString(R.string.note_summary_dist),dataBundle.getFloat(Constants.MAP_DISTANCE));

                sKey = GOAL_PREFIX + DataType.TYPE_MOVE_MINUTES.getName();
                int moveMins = dataBundle.getInt(MAP_MOVE_MINS);
                if (dataBundle.containsKey(sKey)){
                    sTemp = dataBundle.getString(sKey, "0.0");
                    int goalMove = Math.round(Float.parseFloat(sTemp));
                    if (goalMove > 0){
                        float percentGoal = (moveMins > 0) ? ((float)moveMins/goalMove)*100 : 0F;
                        sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(),getString(R.string.note_summary_move_goals), moveMins, goalMove, percentGoal);
                    }
                }else
                    sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(),getString(R.string.note_summary_move), moveMins);

                sKey = GOAL_PREFIX + DataType.TYPE_HEART_POINTS.getName();
                int heartPts = Math.round(dataBundle.getFloat(MAP_HEART_POINTS));
                if (dataBundle.containsKey(sKey)){
                    sTemp = dataBundle.getString(sKey, "0");
                    int goalHeart = Math.round(Float.parseFloat(sTemp));
                    if (goalHeart> 0){
                        float percentGoal = (heartPts > 0) ? ((float)heartPts/goalHeart)*100 : 0F;
                        sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(),getString(R.string.note_summary_heart_pts_goals), heartPts, goalHeart, percentGoal);
                    }
                }else
                    sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(),getString(R.string.note_summary_heart_pts), heartPts);

                sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(), getString(R.string.note_summary_heart),dataBundle.getFloat(Constants.MAP_BPM_MIN),
                        dataBundle.getFloat(Constants.MAP_BPM_MAX),  dataBundle.getFloat(Constants.MAP_BPM_AVG));
                sContent = sContent + ATRACKIT_SPACE + String.format(Locale.getDefault(),getString(R.string.note_summary_calories),dataBundle.getFloat(Constants.MAP_CALORIES));
            }
            catch(Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(LOG_TAG,e.getMessage());
            }
            // No goals option!
            if (!dataBundle.containsKey(GOAL_PREFIX + DataType.TYPE_MOVE_MINUTES.getName()) && !dataBundle.containsKey(GOAL_PREFIX + DataType.TYPE_STEP_COUNT_DELTA.getName()) && !dataBundle.containsKey(GOAL_PREFIX + DataType.TYPE_HEART_POINTS.getName())) {
                sContent = String.format(Locale.getDefault(), getString(R.string.note_summary_desc), dataBundle.getInt(Constants.MAP_MOVE_MINS), dataBundle.getFloat(Constants.MAP_DISTANCE),
                        dataBundle.getFloat(Constants.MAP_BPM_MIN), dataBundle.getFloat(Constants.MAP_BPM_MAX), dataBundle.getFloat(Constants.MAP_BPM_AVG), dataBundle.getFloat(Constants.MAP_CALORIES), dataBundle.getInt(Constants.MAP_STEPS));
            }
            sKey = Workout.class.getSimpleName() + "_list";
            if (dataBundle.containsKey(sKey)){
                List<Workout> list = dataBundle.getParcelableArrayList(sKey);
                sContent = sContent + "\nDetected Activity\n";
                for(Workout w: list){
                    sContent = sContent + referencesTools.getFitnessActivityTextById(w.activityID) + ATRACKIT_SPACE + Utilities.getDurationBreakdown(w.duration) + "\n";
                }
            }
            Bitmap bitmap = null; //= BitmapFactory.decodeResource(resources, R.drawable.ic_launcher);
            //Drawable home = ContextCompat.getDrawable(context,R.drawable.ic_launcher_home);
            try {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_home); // vectorToBitmap(home);
            }catch (Exception ee){
                Log.e(LOG_TAG, "load bitmap error " + ee.getMessage());
            }
            if (bitmap == null){
                Log.e(LOG_TAG, "loading bitmap is null" );
            }
            IconCompat bubbleIcon = IconCompat.createWithResource(context, R.drawable.ic_a_outlined);
            NotificationCompat.BubbleMetadata metaBubble = new NotificationCompat.BubbleMetadata.Builder().setAutoExpandBubble(true).setIntent(notificationPendingIntent).setIcon(bubbleIcon).build();
            // Build the notification with all of the parameters.
            NotificationCompat.Builder notifyBuilder = new NotificationCompat
                    .Builder(context, sChannelID)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(sContent).setBigContentTitle(sTitle))
                    .setBubbleMetadata(metaBubble)
                    .setSmallIcon(R.drawable.ic_a_outlined)
                    .setLargeIcon(bitmap)
                    .setAutoCancel(true)
                    .setContentIntent(notificationPendingIntent)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);
            /*NotificationCompat.Action actionView = new NotificationCompat.Action.Builder(R.drawable.ic_launch, context.getString(R.string.action_open), notificationPendingIntent).build();
            NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender().addAction(actionView);
            notifyBuilder.extend(extender);*/
            if (mNotifyManager != null)
                mNotifyManager.notify(notificationID, notifyBuilder.build());

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return;
    }

    private void startWorkingNotification(){
        Context context = getApplicationContext();
        Drawable icon_outlined = AppCompatResources.getDrawable(context, R.drawable.ic_a_outlined);
        Utilities.setColorFilter(icon_outlined, context.getColor(R.color.primaryLightColor));
        PackageManager pm = context.getPackageManager();
        boolean bWear = pm.hasSystemFeature(PackageManager.FEATURE_WATCH);
        String sSplashActivity = (bWear) ? "com.a_track_it.fitdata.activity.RoomActivity":"com.a_track_it.fitdata.activity.MainActivity";
        Intent viewIntent;
        try {
            viewIntent = new Intent(context, Class.forName(sSplashActivity));

        viewIntent.setAction(INTENT_HOME_REFRESH);
        viewIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        viewIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        int notificationID = Constants.NOTIFICATION_SUMMARY_ID;
        String sTitle = getString(R.string.action_cloud_running);
        String sContent = getString(R.string.app_name);
        String sChannelID = SUMMARY_CHANNEL_ID;
        PendingIntent pendingViewIntent = PendingIntent.getActivity(context, notificationID, viewIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action actionView5 =
                new NotificationCompat.Action.Builder(R.drawable.ic_a_outlined,
                        getString(R.string.action_open), pendingViewIntent)
                        .build();
        PendingIntent pendingViewIntent5 = PendingIntent.getActivity
                (getApplicationContext(), notificationID, viewIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifyBuilder5 = new NotificationCompat
                .Builder(context, sChannelID)
                .setContentTitle(sTitle)
                .setContentText(sContent)
                .setSmallIcon(R.drawable.ic_a_outlined)
                .setProgress(0, 0, true)
                .setOngoing(true)
                .setContentInfo(getString(R.string.action_db_running))
                .setContentIntent(pendingViewIntent5)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

            if (mNotifyManager != null)
                mNotifyManager.notify(notificationID, notifyBuilder5.build());
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }
    /**
     * GET step count for a_track_it.com specific ACTIVITY.
     *
     * Retrieves a_track_it.com raw step count.
     *
     */
    private DataReadRequest queryStepCount(long startTime, long endTime) {
        return new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .enableServerQueries() // Used to retrieve data from cloud
                .build();
    }
    private DataReadRequest queryBPMSummaryByHour(long startTime, long endTime) {
        return new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_HEART_RATE_BPM)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .enableServerQueries() // Used to retrieve data from cloud
                .build();
    }
    private Bitmap vectorToBitmap(Drawable drawable){
        try {
            Bitmap bitmap;

            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            // Handle the error
            return null;
        }
    }
      // Helper for showing tests
    void showToast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DailySummaryJobIntentService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
