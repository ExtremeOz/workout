package com.a_track_it.workout.common.data_model;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.InjectorUtils;
import com.a_track_it.workout.common.R;
import com.a_track_it.workout.common.ReferencesTools;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.GoalsReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.a_track_it.workout.common.Constants.ATRACKIT_EMPTY;
import static com.a_track_it.workout.common.Constants.ATRACKIT_SPACE;
import static com.a_track_it.workout.common.Constants.INTENT_SUMMARY_DAILY;
import static com.a_track_it.workout.common.Constants.KEY_FIT_ACTION;
import static com.a_track_it.workout.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.workout.common.Constants.KEY_FIT_NAME;
import static com.a_track_it.workout.common.Constants.KEY_FIT_REC;
import static com.a_track_it.workout.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;
import static com.a_track_it.workout.common.Constants.KEY_FIT_VALUE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_WORKOUTID;
import static com.a_track_it.workout.common.Constants.KEY_FIT_WORKOUT_SETID;
import static com.a_track_it.workout.common.Constants.MAP_END;
import static com.a_track_it.workout.common.Constants.MAP_HISTORY_RANGE;
import static com.a_track_it.workout.common.Constants.MAP_START;
import static com.a_track_it.workout.common.Constants.MAP_STEPS;
import static com.a_track_it.workout.common.Constants.MAP_WATTS;
import static com.a_track_it.workout.common.Constants.NOTIFICATION_SUMMARY_ID;
import static com.a_track_it.workout.common.Constants.SESSION_PREFIX;
import static com.a_track_it.workout.common.Constants.SUMMARY_CHANNEL_ID;
import static com.a_track_it.workout.common.Constants.TASK_ACTION_SYNC_WORKOUT;
import static com.a_track_it.workout.common.Constants.WORKOUT_CALL_TO_LINE;
import static com.a_track_it.workout.common.Constants.WORKOUT_LIVE;
import static com.a_track_it.workout.common.Constants.WORKOUT_PAUSED;
import static com.a_track_it.workout.common.Constants.WORKOUT_TYPE_STEPCOUNT;
import static com.google.android.gms.fitness.data.Field.FIELD_DURATION;
import static com.google.android.gms.fitness.data.Field.FIELD_REPETITIONS;
import static com.google.android.gms.fitness.data.Field.FIELD_RESISTANCE;
import static com.google.android.gms.fitness.data.Field.FIELD_RESISTANCE_TYPE;
import static java.text.DateFormat.getTimeInstance;

public class FitSyncJobIntentService extends JobIntentService {
    private final static String LOG_TAG = FitSyncJobIntentService.class.getSimpleName();
    private final Handler mHandler = new Handler();
    public static final int READ_STEP_DELTA_BY_DAY = 11;
    public static final int READ_ACT_EXER_BY_TIMES = 12;
    public static final int READ_ACT_SEG_BY_SUMMARY = 13;
    public static final int READ_BPM_BY_SUMMARY = 14;
    public static final int READ_SEGMENT_DETAILS = 15;
    public static final int READ_SEGMENT_SAMPLES = 16;
    private static final int BUCKET_TYPE_TIME = 0;
    private static final int BUCKET_TYPE_ACTIVITY = 1;
    private static final int BUCKET_TYPE_SEGMENT = 2;
    private static final int BUCKET_TYPE_SESSION = 3;
    /**
     * Unique job ID for this service.
     */
    private static final int JOB_ID = 3;
    private String sUserID;
    private String sDeviceID;
    private String sDeviceID2;
    private int iErrorCode = 0;
    private int session_action = 0;
    private boolean action_successful = false;
    private final Bundle resultBundle = new Bundle();
    private int prevAction = 0;
    private ApplicationPreferences appPrefs;
    private ReferencesTools referencesTools;
    private boolean bRefreshing = false;
    private boolean bHasGym = false;
    private GoogleSignInAccount gsa;
    private Calendar mCalendar;
    private WorkManager mWorkManager;
    private Workout mWorkout;
    private WorkoutSet mWorkoutSet;
    private WorkoutMeta mWorkoutMeta;
    private WorkoutRepository workoutRepository;

    ArrayList<Workout> existingWorkoutArrayList = new ArrayList<>();
    ArrayList<WorkoutSet> existingWorkoutSetArrayList = new ArrayList<>();
    ArrayList<WorkoutMeta> existingWorkoutMetaArrayList = new ArrayList<>();
    ArrayList<Workout> workoutArrayList = new ArrayList<>();
    ArrayList<WorkoutSet> workoutSetArrayList = new ArrayList<>();
    final ArrayList<WorkoutMeta> workoutMetaArrayList = new ArrayList<>();
    final ArrayList<UserDailyTotals> udtList = new ArrayList<>();
    ArrayList<UserDailyTotals> existingUserDailyTotalList = new ArrayList<>();
    private List<Task<DataReadResponse>> requestList = new ArrayList<>();

    List<WorkRequest> workList = new ArrayList<>();
    List<Exercise> exercisesMissingList = new ArrayList<>();
    boolean requiresLogin = false;
    List<Workout> pendingWorkoutList = new ArrayList<>();      // pending to Load TO
    List<WorkoutSet> pendingWorkoutSetList = new ArrayList<>();

    List<Workout> fitWorkoutList = new ArrayList<>();         // already loaded to Google to check when load TO
    List<WorkoutSet> fitWorkoutSetList = new ArrayList<>();

    List<Workout> summaryWorkoutList = new ArrayList<>();      // pending to Load TO
    final List<Workout> loadingWorkoutList = new ArrayList<>();      // pending to Load TO
    final List<WorkoutSet> loadingWorkoutSetList = new ArrayList<>();
    String sExerciseList;
    long startTime = 0;
    long endTime = 0;
    long syncInterval = 0;
    int iWorkouts = 0; int iSteps = 0; int iWorkoutSets = 0; int iWorkoutMeta = 0;
    int doyNOW = 0; int doySTART = 0;
    long waitStart = 0;
    long lHistoryStart = 0; long lHistoryEnd = 0;
    Configuration configSetup;

    private ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            int resultVal = 0;
            if (resultCode == 200) {
                final long timeMs = System.currentTimeMillis();
                switch (session_action) {
                    case TASK_ACTION_SYNC_WORKOUT:
                        int resultSize = resultData.getInt(Constants.INTENT_CLOUD_POPULATE);
                        long startTime = resultData.getLong(Constants.MAP_START, timeMs);
                        long endTime = resultData.getLong(Constants.MAP_END, timeMs);
                        resultVal += resultSize;
                        if ((resultSize > 0) && (startTime < endTime)) {
                            ArrayList<Workout> workouts = new ArrayList<>();
                            ArrayList<WorkoutSet> sets = new ArrayList<>();
                            ArrayList<String> newExercises = new ArrayList<>();
                            ArrayList<UserDailyTotals> totalsArrayList = new ArrayList<>();
                            List<OneTimeWorkRequest> workRequestList = new ArrayList<>();
                            String sKey = Workout.class.getSimpleName();
                            if (resultData.containsKey(sKey)) {
                                Workout routine = resultData.getParcelable(sKey);
                                workouts.add(routine);
                                Log.e(LOG_TAG, "workouts " + workouts.size());
                            } else {
                                sKey = Constants.KEY_LIST_WORKOUTS;
                                if (resultData.containsKey(sKey)) {
                                    workouts.addAll(resultData.getParcelableArrayList(sKey));
                                    Log.e(LOG_TAG, "workouts " + workouts.size());
                                }
                            }
                            sKey = WorkoutSet.class.getSimpleName();
                            if (resultData.containsKey(sKey)) {
                                WorkoutSet workoutSet = resultData.getParcelable(sKey);
                                sets.add(workoutSet);
                                Log.e(LOG_TAG, "sets " + sets.size());
                            } else {
                                sKey = Constants.KEY_LIST_SETS;
                                if (resultData.containsKey(sKey)) {
                                    sets.addAll(resultData.getParcelableArrayList(sKey));
                                    Log.e(LOG_TAG, "sets " + sets.size());
                                }
                            }
                            sKey = UserDailyTotals.class.getSimpleName();
                            if (resultData.containsKey(sKey)) {
                                UserDailyTotals udt = resultData.getParcelable(sKey);
                                totalsArrayList.add(udt);
                                Log.e(LOG_TAG, "Totals " + totalsArrayList.size());
                            } else {
                                sKey += "_list";
                                if (resultData.containsKey(sKey)) {
                                    totalsArrayList.addAll(resultData.getParcelableArrayList(sKey));
                                    Log.e(LOG_TAG, "Totals " + totalsArrayList.size());
                                }
                            }
                            for (WorkoutSet aSet : sets) {
                                if (aSet.workoutID == 1L) {
                                    for (Workout test : workouts) {
                                        if (Utilities.isGymWorkout(test.activityID)
                                                && ((aSet.start >= test.start) && (aSet.end <= test.end))) {
                                            aSet.workoutID = test._id;
                                            break;
                                        }
                                    }
                                }
                                if ((aSet.workoutID == 1L) && (existingWorkoutArrayList.size() > 0)) {
                                    for (Workout test : existingWorkoutArrayList) {
                                        if (Utilities.isGymWorkout(test.activityID)
                                                && ((aSet.start >= test.start) && (aSet.end <= test.end))) {
                                            aSet.workoutID = test._id;
                                            break;
                                        }
                                    }
                                }
                                aSet.last_sync = timeMs;
                                if (existingWorkoutSetArrayList.size() > 0) {
                                    if (!existingWorkoutSetArrayList.contains(aSet))
                                        workoutRepository.insertWorkoutSet(aSet);
                                } else
                                    workoutRepository.insertWorkoutSet(aSet);

                                if ((aSet.exerciseID == null) || (aSet.exerciseID == 0)) {
                                    Bundle dataBundle = new Bundle();
                                    dataBundle.putString(KEY_FIT_NAME, aSet.exerciseName);
                                    dataBundle.putLong(KEY_FIT_WORKOUT_SETID, aSet._id);
                                    if ((aSet.resistance_type != null) && (aSet.resistance_type > 0))
                                        dataBundle.putLong(KEY_FIT_TYPE, aSet.resistance_type);

                                    if (newExercises.size() > 0) {
                                        if (!newExercises.contains(aSet.exerciseName)) {
                                            newExercises.add(aSet.exerciseName);
                                            // create notification for edit new exercise !!
                                            // sendNotification(INTENT_EXERCISE_NEW, dataBundle);
                                        }
                                    } else {
                                        newExercises.add(aSet.exerciseName);
                                        // sendNotification(INTENT_EXERCISE_NEW, dataBundle); // create notification for edit new exercise !!
                                    }
                                }
                            }
                            for (Workout workout : workouts) {
                                if (workout.userID.length() == 0) workout.userID = sUserID;
                                if (workout.deviceID.length() == 0) workout.deviceID = sDeviceID;
                                workout.last_sync = timeMs;
                                if ((workout._id > 0) && (workout.activityID > 0) && (workout.start < workout.end)) {
                                    List<Workout> workList = new ArrayList<>();
                                    try {
                                        Workout w = workoutRepository.getWorkoutByIdNow(workout._id, sUserID, Constants.ATRACKIT_EMPTY);
                                        if (w != null) workList.add(w);
                                    } catch (Exception e) {
                                        Log.e(LOG_TAG, "getWorkout for ID " + workout._id);
                                    }
                                    if (workList.size() == 0)
                                        workoutRepository.insertWorkout(workout);
                                    else
                                        workoutRepository.updateWorkout(workout);

                                    if (Utilities.isGymWorkout(workout.activityID)) {
                                        Data.Builder builder = new Data.Builder();
                                        builder.putString(KEY_FIT_USER, workout.userID);
                                        builder.putLong(KEY_FIT_WORKOUTID, workout._id);
                                        OneTimeWorkRequest workRequest =
                                                new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                                                        .setInputData(builder.build())
                                                        .build();
                                        workRequestList.add(workRequest);
                                    }
                                }
                            }
                            if (workRequestList.size() > 0) {
                                if (mWorkManager == null)
                                    mWorkManager = WorkManager.getInstance(getApplicationContext());
                                mWorkManager.enqueue(workRequestList);
                            }
                        }
                        break;
                    case Constants.TASK_ACTION_READ_GOALS:
                        if (appPrefs != null) appPrefs.setLastGoalSync(System.currentTimeMillis());
                        for(int i=0; i < 3; i++){
                            String recKey = KEY_FIT_REC + i;
                            String recType = KEY_FIT_TYPE + i;
                            String recValue = KEY_FIT_VALUE + i;
                            String recName = KEY_FIT_NAME + i;
                            if (resultData.containsKey(recKey)){
                                String sKey = resultData.getString(recKey);
                                String sValue = resultData.getString(recValue);
                                int iType = resultData.getInt(recType);
                                String sName = resultData.getString(recName);
                                try {
                                    List<Configuration> existingConfigs = workoutRepository.getConfigLikeName(sKey,sUserID);
                                    if (existingConfigs.size() == 0) {
                                        Configuration config = new Configuration(sKey, sUserID, sValue, timeMs, Integer.toString(iType),sName);
                                        workoutRepository.insertConfig(config);
                                    }else {
                                        Configuration config = existingConfigs.get(0);
                                        config.stringValue = sValue;
                                        config.longValue = timeMs;
                                        config.stringValue1 =  Integer.toString(iType);
                                        config.stringValue2 = sName;
                                        workoutRepository.updateConfig(config);
                                    }
                                }catch(Exception e){
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                }
                            }
                        }
                        break;
                }
            }
        }
    };
    private final OnCompleteListener<DataReadResponse> readCompleter = task -> {
        bRefreshing = false;
    };
    private final OnSuccessListener<DataReadResponse> readUDTSuccess = dataReadResponse -> {
        if (doyNOW == doySTART)
            Log.w(LOG_TAG,"segment read successful ");
        else
            Log.w(LOG_TAG,"UDT read successful ");
        if (dataReadResponse.getBuckets().size() > 0){
            List<Bucket> buckets = dataReadResponse.getBuckets();
            Log.d(LOG_TAG, "received bucket size " + buckets.size());
            for (Bucket bucket : buckets) {
                List<DataSet> dataSets = bucket.getDataSets();
                UserDailyTotals userDailyTotal = new UserDailyTotals();
                userDailyTotal.userID = sUserID;
                for (DataSet dataSet : dataSets) {
                    // dumpDataSet(dataSet);
                    List<DataPoint> dpList = dataSet.getDataPoints();
                    for (DataPoint dp : dpList) {
                        Bundle resultBundle = DataPointToResultBundle(dp);
                        if (resultBundle.containsKey(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())) {
                            Workout w = resultBundle.getParcelable(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName());
                            w.userID = sUserID;
                            if (w.duration == 0) w.duration = w.end - w.start;
                            if (w._id == 0) w._id = w.start + w.activityID;
                            w.last_sync = w._id;
                            if ((userDailyTotal._id == 0L) && (w.start > 0))
                                userDailyTotal._id = Utilities.getDayEnd(mCalendar, w.start);
                            summaryWorkoutList.add(w);
                        }
                        if (resultBundle.containsKey(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
                            Workout w = resultBundle.getParcelable(DataType.TYPE_ACTIVITY_SEGMENT.getName());
                            w.userID = sUserID;
                            if (w.duration == 0) w.duration = w.end - w.start;
                            if (w._id == 0) w._id = w.start;
                            w.last_sync = w._id;
                            if ((userDailyTotal._id == 0L) && (w.start > 0))
                                userDailyTotal._id = Utilities.getDayEnd(mCalendar, w.start);
                            loadingWorkoutList.add(w);
                        }
                        if (resultBundle.containsKey(DataType.AGGREGATE_STEP_COUNT_DELTA.getName())) {
                            Workout w = resultBundle.getParcelable(DataType.AGGREGATE_STEP_COUNT_DELTA.getName());
                            if ((userDailyTotal._id == 0L) && (w.start > 0))
                                userDailyTotal._id = Utilities.getDayEnd(mCalendar, w.start);
                            userDailyTotal.stepCount = w.stepCount;
                        }
                        if (resultBundle.containsKey(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
                            WorkoutSet s = resultBundle.getParcelable(DataType.TYPE_WORKOUT_EXERCISE.getName());
                            s._id = Utilities.getDayStart(mCalendar, s.start) + workoutSetArrayList.size();
                            s.last_sync = s._id;
                            s.userID = sUserID;
                            Log.w(LOG_TAG, "adding set " + s.toString());
                            workoutSetArrayList.add(s);
                        }
                        if (resultBundle.containsKey(DataType.TYPE_MOVE_MINUTES.getName()))
                            userDailyTotal.activeMinutes = resultBundle.getInt(DataType.TYPE_MOVE_MINUTES.getName());
                        if (resultBundle.containsKey(DataType.TYPE_CALORIES_EXPENDED.getName()))
                            userDailyTotal.caloriesExpended = resultBundle.getFloat(DataType.TYPE_CALORIES_EXPENDED.getName());
                        if (resultBundle.containsKey(DataType.TYPE_DISTANCE_DELTA.getName()))
                            userDailyTotal.distanceTravelled = resultBundle.getFloat(DataType.TYPE_DISTANCE_DELTA.getName());
                        if (resultBundle.containsKey(Constants.MAP_BPM_AVG))
                            userDailyTotal.avgBPM = resultBundle.getFloat(Constants.MAP_BPM_AVG);
                        if (resultBundle.containsKey(Constants.MAP_BPM_MIN))
                            userDailyTotal.minBPM = resultBundle.getFloat(Constants.MAP_BPM_MIN);
                        if (resultBundle.containsKey(Constants.MAP_BPM_MAX))
                            userDailyTotal.maxBPM = resultBundle.getFloat(Constants.MAP_BPM_MAX);
                        if (resultBundle.containsKey(Constants.MAP_SPEED_AVG))
                            userDailyTotal.avgSpeed = resultBundle.getFloat(Constants.MAP_SPEED_AVG);
                        if (resultBundle.containsKey(Constants.MAP_SPEED_MIN))
                            userDailyTotal.minSpeed = resultBundle.getFloat(Constants.MAP_SPEED_MIN);
                        if (resultBundle.containsKey(Constants.MAP_SPEED_MAX))
                            userDailyTotal.maxSpeed = resultBundle.getFloat(Constants.MAP_SPEED_MAX);
                        if (resultBundle.containsKey(DataType.AGGREGATE_HEART_POINTS.getName())) {
                            Workout w = resultBundle.getParcelable(DataType.AGGREGATE_HEART_POINTS.getName());
                            userDailyTotal.heartDuration = w.duration;
                            userDailyTotal.heartIntensity = w.wattsTotal;
                        }
                    }
                }
                if ((userDailyTotal._id > 0) && (doyNOW != Utilities.getDOY(mCalendar,userDailyTotal._id))){
                    Log.e(LOG_TAG, "added udt " + userDailyTotal.toString());
                    userDailyTotal.lastUpdated = userDailyTotal._id;
                    udtList.add(userDailyTotal);
                }
            }
        }
    };
    private final OnSuccessListener<DataReadResponse> readSuccess = dataReadResponse -> {
        Log.w(LOG_TAG,"segment read successful ");
        if (dataReadResponse.getBuckets().size() > 0){
            List<Bucket> buckets = dataReadResponse.getBuckets();
            Log.d(LOG_TAG, "segment read received bucket size " + buckets.size());
            for (Bucket bucket : buckets) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    // dumpDataSet(dataSet);
                    List<DataPoint> dpList = dataSet.getDataPoints();
                    for (DataPoint dp : dpList) {
                        Bundle resultBundle = DataPointToResultBundle(dp);
                        if (resultBundle.containsKey(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
                            Workout w = resultBundle.getParcelable(DataType.TYPE_ACTIVITY_SEGMENT.getName());
                            w.userID = sUserID;
                            if (w.duration == 0) w.duration = w.end - w.start;
                            if (w._id == 0) w._id = w.start;
                            w.last_sync = w._id;
                            if (!Utilities.isDetectedActivity(w.activityID))
                                loadingWorkoutList.add(w);
                        }
                        if (resultBundle.containsKey(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())) {
                            Workout w = resultBundle.getParcelable(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName());
                            w.userID = sUserID;
                            if (w.duration == 0) w.duration = w.end - w.start;
                            if (w._id == 0) w._id = w.start;
                            w.last_sync = w._id;
                            summaryWorkoutList.add(w);
                        }
                        if (resultBundle.containsKey(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
                            WorkoutSet s = resultBundle.getParcelable(DataType.TYPE_WORKOUT_EXERCISE.getName());
                            s._id = Utilities.getDayStart(mCalendar, s.start) + workoutSetArrayList.size();
                            s.last_sync = s._id;
                            s.userID = sUserID;
                            s.last_sync = s._id;
                            Log.w(LOG_TAG, "adding set " + s.toString());
                            workoutSetArrayList.add(s);
                        }
                    }
                }
            }
        }
        if (dataReadResponse.getDataSets().size() > 0){
            List<DataSet> listDataSet = dataReadResponse.getDataSets();
            Log.d(LOG_TAG, "segment read received dataset size " + listDataSet.size());
            for (DataSet dataSet : listDataSet) {
                List<DataPoint> dpList = dataSet.getDataPoints();
                for (DataPoint dp : dpList) {
                    Bundle resultBundle = DataPointToResultBundle(dp);
                    if (resultBundle.containsKey(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
                        Workout w = resultBundle.getParcelable(DataType.TYPE_ACTIVITY_SEGMENT.getName());
                        w.userID = sUserID;
                        if (w.duration == 0) w.duration = w.end - w.start;
                        if (w._id == 0) w._id = w.start;
                        w.last_sync = w._id;
                        if (!Utilities.isDetectedActivity(w.activityID))
                            loadingWorkoutList.add(w);
                    }
                    if (resultBundle.containsKey(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())) {
                        Workout w = resultBundle.getParcelable(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName());
                        w.userID = sUserID;
                        if (w.duration == 0) w.duration = w.end - w.start;
                        if (w._id == 0) w._id = w.start;
                        w.last_sync = w._id;
                        summaryWorkoutList.add(w);
                    }
                    if (resultBundle.containsKey(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
                        WorkoutSet s = resultBundle.getParcelable(DataType.TYPE_WORKOUT_EXERCISE.getName());
                        s._id = Utilities.getDayStart(mCalendar, s.start) + workoutSetArrayList.size();
                        s.last_sync = s._id;
                        s.userID = sUserID;
                        s.last_sync = s._id;
                        Log.w(LOG_TAG, "adding set " + s.toString());
                        workoutSetArrayList.add(s);
                    }
                }
            }
        }
    };
    private final OnSuccessListener<DataReadResponse> metaResponse = dataReadResponse -> {
            if (dataReadResponse.getBuckets().size() > 0){
                for (Bucket bucket : dataReadResponse.getBuckets()) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    for (DataSet dataSet : dataSets) {
                        List<DataPoint> dpList = dataSet.getDataPoints();
                        for (DataPoint dp : dpList) {
                            DataPointToMetaData(dp);
                        }
                    }
                }
            }else {
                List<DataSet> listDataSet = dataReadResponse.getDataSets();
                if (listDataSet != null)
                    for (DataSet dataSet : listDataSet) {
                        List<DataPoint> dpList = dataSet.getDataPoints();
                        for (DataPoint dp : dpList) {
                            DataPointToMetaData(dp);
                        }
                    }
            }

        bRefreshing = false;
    };

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, FitSyncJobIntentService.class, JOB_ID, intent);
    }

    public FitSyncJobIntentService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        workoutRepository = InjectorUtils.getWorkoutRepository((Application) context);
        referencesTools = ReferencesTools.setInstance(context);
        mCalendar = Calendar.getInstance(Locale.getDefault());
        mWorkManager = WorkManager.getInstance(context);
        appPrefs = ApplicationPreferences.getPreferences(context);
        configSetup = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        workoutRepository.destroyInstance();
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        try {
            Context context = getApplicationContext();
            session_action = intent.getIntExtra(KEY_FIT_ACTION, 0);
            sUserID = intent.getStringExtra(Constants.KEY_FIT_USER);
            sDeviceID = intent.getStringExtra(Constants.KEY_FIT_DEVICE_ID);
            resultBundle.putInt(KEY_FIT_ACTION, session_action);
            resultBundle.putString(Constants.KEY_FIT_USER, sUserID);
            resultBundle.putString(KEY_FIT_DEVICE_ID, sDeviceID);  // id the result uniquely
            final long timeMs = System.currentTimeMillis();
            long startTime = intent.getLongExtra(MAP_START, 0);
            long endTime = intent.getLongExtra(MAP_END, 0);
            long workoutID = 0L;
            if (intent.hasExtra(Workout.class.getSimpleName())) {
                mWorkout = intent.getParcelableExtra(Workout.class.getSimpleName());
                workoutID = mWorkout._id;
            }
            if (intent.hasExtra(WorkoutSet.class.getSimpleName())) mWorkoutSet = intent.getParcelableExtra(WorkoutSet.class.getSimpleName()); else mWorkoutSet = null;
            if (intent.hasExtra(WorkoutMeta.class.getSimpleName())) mWorkoutMeta = intent.getParcelableExtra(WorkoutMeta.class.getSimpleName()); else mWorkoutMeta = null;
            resultBundle.putLong(MAP_START, startTime);
            resultBundle.putLong(MAP_END, endTime);
            gsa = null;

            ResultReceiver resultReceiver = intent.getParcelableExtra(Constants.KEY_RESULT);
            boolean isInternal = true;
            if (resultReceiver != null) {
                mResultReceiver = resultReceiver;
                isInternal = false;
            }
            GoogleSignInAccount gsa2 = null; FitnessOptions fo = referencesTools.getFitnessSignInOptions(1);
            if (session_action == Constants.TASK_ACTION_READ_SESSION || session_action == Constants.TASK_ACTION_READ_GOALS)
                fo = referencesTools.getFitnessSignInOptions(2);

            if (session_action == Constants.TASK_ACTION_RECORD_START || session_action == Constants.TASK_ACTION_RECORD_END) fo = referencesTools.getFitnessSignInOptions(3);
            if ((session_action == Constants.TASK_ACTION_SYNC_WORKOUT) || (session_action == Constants.TASK_ACTION_START_SESSION) || (session_action == Constants.TASK_ACTION_STOP_SESSION)) fo = referencesTools.getFitnessSignInOptions(5);
            if (intent.hasExtra(Constants.KEY_PAYLOAD)) {
                gsa = (intent.getParcelableExtra(Constants.KEY_PAYLOAD));
                if (gsa != null && gsa.isExpired())
                    gsa = null;
                else
                  if (gsa != null) Log.w(LOG_TAG, "pickup acct " + gsa.getDisplayName() + "action " + session_action);
            }
            if (gsa == null){
                gsa2 = GoogleSignIn.getAccountForExtension(context,fo);
                if (gsa2 != null){
                        gsa = gsa2;
                        Log.w(LOG_TAG, session_action + " getAcct for extension " + gsa2.getGrantedScopes().toString());
                }
            }
            if (gsa == null || gsa.isExpired()){
                Log.e(LOG_TAG, "gso expired or null action " + session_action);
                mResultReceiver.send(400, resultBundle);
                return;
            }

            else {
                if (intent.hasExtra(KEY_FIT_WORKOUTID)) {
                    workoutID = intent.getLongExtra(KEY_FIT_WORKOUTID, 0);
                }else {
                    mWorkout = new Workout();
                    mWorkout._id = startTime;
                    workoutID = mWorkout._id;
                    mWorkout.userID = sUserID;
                    mWorkout.deviceID = sDeviceID;
                    mWorkout.start = startTime;
                    mWorkout.end = endTime;
                }
            }



            boolean bConnected = referencesTools.isNetworkConnected();
            if (!bConnected) {
                resultBundle.putInt(MAP_WATTS, 0); // offline indicator here
                resultBundle.putInt(KEY_FIT_VALUE, 0); // offline indicator here
                Log.w(LOG_TAG, "offline  " + action_successful + " action " + session_action);
                mResultReceiver.send(200, resultBundle);
                return;
            }
            List<Configuration> configList = workoutRepository.getConfigLikeName(context.getString(R.string.state_setup),Constants.ATRACKIT_ATRACKIT_CLASS);
            if (configList != null && (configList.size() > 0)){
                configSetup = configList.get(0);
                if (configSetup.stringValue1 == null || configSetup.stringValue1.length() == 0){
                    configSetup.stringValue1 = Constants.INTENT_CLOUD_SYNC;
                    workoutRepository.updateConfig(configSetup);
                }else{
                    action_successful = true; // currently busy! - all params returned for re-use
                    if (mWorkout != null) resultBundle.putParcelable(Workout.class.getSimpleName(), mWorkout);
                    if (mWorkoutSet != null) resultBundle.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
                    if (mWorkoutMeta != null) resultBundle.putParcelable(WorkoutMeta.class.getSimpleName(), mWorkoutMeta);
                    resultBundle.putLong(KEY_FIT_WORKOUTID, workoutID);
                    iErrorCode = 201;
                    Log.w(LOG_TAG, "currently busy  " + action_successful + " action " + session_action);
                    mResultReceiver.send(iErrorCode, resultBundle);
                    return;
                }
            }
            Log.w(LOG_TAG, "fitSyncJobIntent " + session_action + " start " + Utilities.getTimeDateString(startTime));

            bRefreshing = true;
            final String sTimeOfDayName = (mWorkout != null) ? Utilities.getPartOfDayString(mWorkout.start) + Constants.ATRACKIT_SPACE + mWorkout.activityName : ATRACKIT_EMPTY;
            final String sID = Long.toString((mWorkout != null) ? mWorkout._id : System.currentTimeMillis());
            final String mSessionID = SESSION_PREFIX + sID;
            final Device device = Device.getLocalDevice(context);
            if (sDeviceID == null)
                sDeviceID = device.getUid();
            resultBundle.putString(KEY_FIT_DEVICE_ID, sDeviceID);
            DataReadRequest.Builder builder = new DataReadRequest.Builder();
            if (this.session_action == Constants.TASK_ACTION_READ_HISTORY) {
                if (intent.hasExtra(KEY_FIT_TYPE)) {
                    prevAction = intent.getIntExtra(Constants.KEY_FIT_TYPE, 0);
                    resultBundle.putInt(KEY_FIT_TYPE, prevAction);
                }
            }
            if ((session_action != Constants.TASK_ACTION_READ_GOALS)
                    && (session_action == Constants.TASK_ACTION_SYNC_WORKOUT)) {
                existingWorkoutArrayList.clear();
                List<Workout> workouts = workoutRepository.getWorkoutsNow(sUserID, sDeviceID, startTime, endTime);
                if ((workouts != null) && (workouts.size() > 0))
                    existingWorkoutArrayList.addAll(workouts);
                existingWorkoutSetArrayList.clear();
                List<WorkoutSet> sets2 = workoutRepository.getWorkoutSetsNow(sUserID, sDeviceID, startTime, endTime);
                if ((sets2 != null) && (sets2.size() > 0)) existingWorkoutSetArrayList.addAll(sets2);
                existingWorkoutMetaArrayList.clear();
                List<WorkoutMeta> metas = workoutRepository.getWorkoutMetasNow(sUserID, sDeviceID, startTime, endTime);
                if ((metas != null) && (metas.size() > 0))
                    existingWorkoutMetaArrayList.addAll(metas);
            }
            try {
                switch (session_action) {
                    case Constants.TASK_ACTION_READ_CLOUD:
                    case Constants.TASK_ACTION_SYNC_WORKOUT:
                        boolean doIt = true;
                        List<Configuration> configState = workoutRepository.getConfigLikeName(Constants.MAP_CURRENT_STATE, sUserID);
                        if (configState.size() > 0){
                            long state = configState.get(0).longValue;
                            doIt = (state != WORKOUT_LIVE && state != WORKOUT_PAUSED && state != WORKOUT_CALL_TO_LINE);
                        }
                        if (doIt) doFullSync();
                        break;
                    case Constants.TASK_ACTION_RECORD_START:
                        subscribeRecordingApiByDataType(context, DataType.TYPE_HEART_RATE_BPM);
                        subscribeRecordingApiByDataType(context, DataType.TYPE_STEP_COUNT_DELTA);
                        subscribeRecordingApiByDataType(context, DataType.TYPE_DISTANCE_DELTA);
                        subscribeRecordingApiByDataType(context, DataType.TYPE_CALORIES_EXPENDED);
                        subscribeRecordingApiByDataType(context, DataType.TYPE_MOVE_MINUTES);
                        subscribeRecordingApiByDataType(context, DataType.TYPE_HEART_POINTS);
                        subscribeRecordingApiByDataType(context, DataType.TYPE_LOCATION_SAMPLE);
                        subscribeRecordingApiByDataType(context, DataType.TYPE_ACTIVITY_SEGMENT);
                        subscribeRecordingApiByDataType(context, DataType.TYPE_WORKOUT_EXERCISE);
                        subscribeRecordingApiByDataType(context, DataType.TYPE_POWER_SAMPLE);
                        action_successful = true; bRefreshing = false;
                        break;
                    case Constants.TASK_ACTION_RECORD_END:
                        unsubscribeRecordingApiByDataType(context, DataType.TYPE_HEART_RATE_BPM);
                        unsubscribeRecordingApiByDataType(context, DataType.TYPE_STEP_COUNT_DELTA);
                        unsubscribeRecordingApiByDataType(context, DataType.TYPE_DISTANCE_DELTA);
                        unsubscribeRecordingApiByDataType(context, DataType.TYPE_CALORIES_EXPENDED);
                        unsubscribeRecordingApiByDataType(context, DataType.TYPE_MOVE_MINUTES);
                        unsubscribeRecordingApiByDataType(context, DataType.TYPE_HEART_POINTS);
                        unsubscribeRecordingApiByDataType(context, DataType.TYPE_LOCATION_SAMPLE);
                        unsubscribeRecordingApiByDataType(context, DataType.TYPE_ACTIVITY_SEGMENT);
                        unsubscribeRecordingApiByDataType(context, DataType.TYPE_WORKOUT_EXERCISE);
                        unsubscribeRecordingApiByDataType(context, DataType.TYPE_POWER_SAMPLE);
                        action_successful = true; bRefreshing = false;
                        break;
                    case Constants.TASK_ACTION_START_SESSION:
                        Session session = new Session.Builder()
                                .setName(sTimeOfDayName)
                                .setIdentifier(mSessionID)
                                .setStartTime(mWorkout.start, TimeUnit.MILLISECONDS)
                                .setActivity(mWorkout.identifier)
                                .setDescription(sTimeOfDayName).build();
                        Task<Void> response = Fitness.getSessionsClient(context, gsa).startSession(session);
                        response.addOnCompleteListener(task -> {
                            action_successful = task.isSuccessful();
                            bRefreshing = false;
                            if (action_successful) {
                                mWorkout.last_sync = timeMs;
                                workoutRepository.updateWorkout(mWorkout);
                                resultBundle.putParcelable(Workout.class.getSimpleName(), mWorkout);
                            } else {
                                resultBundle.putLong(KEY_FIT_WORKOUTID, mWorkout._id);
                            }
                        });
                        break;
                    case Constants.TASK_ACTION_STOP_SESSION:
                        Task<List<Session>> stopTask = Fitness.getSessionsClient(context, gsa)
                                .stopSession(mSessionID);
                        stopTask.addOnCompleteListener(task -> {
                            action_successful = task.isSuccessful();
                            bRefreshing = false;
                            if (action_successful) {
                                mWorkout.last_sync = timeMs;
                                workoutRepository.updateWorkout(mWorkout);
                                resultBundle.putParcelable(Workout.class.getSimpleName(), mWorkout);
                            }
                        }).addOnFailureListener(e -> {
                            action_successful = false;
                            bRefreshing = false;
                        });
                        break;
                    case Constants.TASK_ACTION_READ_HISTORY:
                        try {
                            DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                                    .read(DataType.TYPE_ACTIVITY_SEGMENT)
                                    .setLimit(1000)
                                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                                    .enableServerQueries() // Used to retrieve data from cloud.
                                    .build();
                            Task<DataReadResponse> segmentResponse = Fitness.getHistoryClient(getApplicationContext(), gsa).readData(dataReadRequest);
                            segmentResponse.addOnCompleteListener(segmentCompleteListener);
                            Tasks.await(segmentResponse, 5, TimeUnit.MINUTES);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "readFitHistory Error " + e.getMessage());
                            bRefreshing = false;
                        }
                        if (action_successful){
                            resultBundle.putLong(KEY_FIT_WORKOUTID, mWorkout._id);
                        }
                        break;
                    case Constants.TASK_ACTION_EXER_SEGMENT:
                        if ((mWorkoutSet == null) ||(mWorkoutSet.repCount == null) || (mWorkoutSet.resistance_type == null) || (mWorkoutSet.weightTotal == null))
                            return;
                        DataSet exDataSet = Utilities.createExerciseDataSet(mWorkoutSet.start, mWorkoutSet.end, mWorkoutSet.exerciseName,
                                mWorkoutSet.repCount, mWorkoutSet.resistance_type, mWorkoutSet.weightTotal, mWorkoutSet.score_card,device);
                        if (exDataSet != null)
                            Fitness.getHistoryClient(context, gsa).insertData(exDataSet).addOnCompleteListener(task -> {
                            action_successful = task.isSuccessful();
                            bRefreshing = false;
                            if (action_successful) {
                                mWorkoutSet.last_sync = timeMs;
                                workoutRepository.updateWorkoutSet(mWorkoutSet);
                                resultBundle.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
                                if ((mWorkoutSet.startBPM != null) || (mWorkoutSet.endBPM != null)){
                                    if ((mWorkoutSet.startBPM != null) && (mWorkoutSet.startBPM > 0) && (mWorkoutSet.start > 0)){
                                        DataSet bpm1Set = createBPMDataSet(mWorkoutSet.start,mWorkoutSet.startBPM,device);
                                        if (bpm1Set != null)
                                            Fitness.getHistoryClient(context, gsa).insertData(bpm1Set);
                                    }
                                    if ((mWorkoutSet.endBPM != null) && (mWorkoutSet.endBPM > 0) && (mWorkoutSet.end > 0)){
                                        DataSet bpm2Set = createBPMDataSet(mWorkoutSet.end,mWorkoutSet.endBPM,device);
                                        if (bpm2Set != null)
                                            Fitness.getHistoryClient(context, gsa).insertData(bpm2Set);
                                    }
                                }
                                if (mWorkoutMeta != null){
                                    if (mWorkoutMeta.start_steps > 0 && mWorkoutMeta.end_steps > 0){
                                        int startStep = Math.toIntExact(mWorkoutMeta.start_steps);
                                        int endStep = Math.toIntExact(mWorkoutMeta.end_steps);
                                        if (startStep > 0 && endStep > 0){
                                            if (endStep < startStep){
                                                endStep = startStep;
                                                startStep = Math.toIntExact(mWorkoutMeta.end_steps);
                                                Log.e(LOG_TAG, "swapping step counts " + startStep + " " + endStep);
                                                mWorkoutMeta.start_steps = startStep;
                                                mWorkoutMeta.end_steps = endStep;
                                                workoutRepository.updateWorkoutMeta(mWorkoutMeta);
                                            }
                                        }
                                        if (mWorkoutSet.stepCount != (endStep - startStep)){
                                            mWorkoutSet.stepCount = (endStep - startStep);
                                            workoutRepository.updateWorkoutSet(mWorkoutSet);
                                        }
                                        if (startStep > 0) {
                                            DataSet stepsSet = createStepCountDeltaDataSet(mWorkoutSet.start, startStep, device);
                                            if (stepsSet != null)
                                                Fitness.getHistoryClient(context, gsa).insertData(stepsSet);
                                        }
                                        if (endStep > 0) {
                                            DataSet stepsSet2 = createStepCountDeltaDataSet(mWorkoutSet.end, endStep, device);
                                            if (stepsSet2 != null)
                                                Fitness.getHistoryClient(context, gsa).insertData(stepsSet2);
                                        }

                                    }else if (mWorkoutSet.stepCount > 0){
                                        int startStep = Math.toIntExact(mWorkoutSet.stepCount);
                                        DataSet stepsSet = createStepCountDeltaDataSet(mWorkoutSet.end,startStep,device);
                                        if (stepsSet != null)
                                            Fitness.getHistoryClient(context, gsa).insertData(stepsSet);

                                    }
                                }

                            }
                        });
                        break;
                    case Constants.TASK_ACTION_ACT_SEGMENT:
                        // Create a_track_it.com data source
                        DataSource activityDataSource = new DataSource.Builder()
                                .setAppPackageName(Constants.ATRACKIT_ATRACKIT_CLASS)
                                .setDevice(device)
                                .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                                .setType(DataSource.TYPE_RAW)
                                .build();
                        DataSet segDataSet = createActivityDataSet(activityDataSource, mWorkoutSet.start, mWorkoutSet.end, mWorkout.identifier);
                        Fitness.getHistoryClient(context, gsa).insertData(segDataSet).addOnCompleteListener(task -> {
                            action_successful = task.isSuccessful();
                            bRefreshing = false;
                            if (action_successful) {
                                mWorkoutSet.last_sync = timeMs;
                                workoutRepository.updateWorkoutSet(mWorkoutSet);
                                resultBundle.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
                                if ((mWorkoutSet.startBPM != null) || (mWorkoutSet.endBPM != null)){
                                    if ((mWorkoutSet.startBPM != null) && (mWorkoutSet.startBPM > 0) && (mWorkoutSet.start > 0)){
                                        DataSet bpm1Set = createBPMDataSet(mWorkoutSet.start,mWorkoutSet.startBPM,device);
                                        if (bpm1Set != null)
                                            Fitness.getHistoryClient(context, gsa).insertData(bpm1Set);
                                    }
                                    if ((mWorkoutSet.endBPM != null) && (mWorkoutSet.endBPM > 0) && (mWorkoutSet.end > 0)){
                                        DataSet bpm2Set = createBPMDataSet(mWorkoutSet.end,mWorkoutSet.endBPM,device);
                                        if (bpm2Set != null)
                                            Fitness.getHistoryClient(context, gsa).insertData(bpm2Set);
                                    }
                                }
                                if (mWorkoutMeta != null){
                                    if (mWorkoutMeta.start_steps > 0 && mWorkoutMeta.end_steps > 0){
                                        int startStep = Math.toIntExact(mWorkoutMeta.start_steps);
                                        int endStep = Math.toIntExact(mWorkoutMeta.end_steps);
                                        if (startStep > 0 && endStep > 0){
                                            if (endStep < startStep){
                                                endStep = startStep;
                                                startStep = Math.toIntExact(mWorkoutMeta.end_steps);
                                                Log.e(LOG_TAG, "swapping step counts " + startStep + " " + endStep);
                                                mWorkoutMeta.start_steps = startStep;
                                                mWorkoutMeta.end_steps = endStep;
                                                workoutRepository.updateWorkoutMeta(mWorkoutMeta);
                                            }
                                        }
                                        if (mWorkoutSet.stepCount != (endStep - startStep)){
                                            mWorkoutSet.stepCount = (endStep - startStep);
                                            workoutRepository.updateWorkoutSet(mWorkoutSet);
                                        }
                                        if (startStep > 0) {
                                            DataSet stepsSet = createStepCountDeltaDataSet(mWorkoutSet.start, startStep, device);
                                            if (stepsSet != null)
                                                Fitness.getHistoryClient(context, gsa).insertData(stepsSet);
                                        }
                                        if (endStep > 0) {
                                            DataSet stepsSet2 = createStepCountDeltaDataSet(mWorkoutSet.end, endStep, device);
                                            if (stepsSet2 != null)
                                                Fitness.getHistoryClient(context, gsa).insertData(stepsSet2);
                                        }

                                    }else if (mWorkoutSet.stepCount > 0){
                                        int startStep = Math.toIntExact(mWorkoutSet.stepCount);
                                        DataSet stepsSet = createStepCountDeltaDataSet(mWorkoutSet.end,startStep,device);
                                        if (stepsSet != null)
                                            Fitness.getHistoryClient(context, gsa).insertData(stepsSet);

                                    }
                                }
                            }
                        });
                        break;
                    case Constants.TASK_ACTION_INSERT_HISTORY:
                        // Create a_track_it.com data source
                        DataSource activityHistoryDataSource = new DataSource.Builder()
                                .setAppPackageName(Constants.ATRACKIT_ATRACKIT_CLASS)
                                .setDevice(device)
                                .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                                .setType(DataSource.TYPE_RAW)
                                .build();
                        DataSet hisDataSet = createActivityDataSet(activityHistoryDataSource, mWorkout.start, mWorkout.end, mWorkout.identifier);
                        final List<WorkoutSet> workSets = workoutRepository.getWorkoutSetByWorkoutId(mWorkout._id,sUserID,sDeviceID);
                        final List<WorkoutMeta> workMetas = workoutRepository.getWorkoutMetasByWorkoutID(mWorkout._id,sUserID,sDeviceID);
                        final List<SensorDailyTotals> sdtList = workoutRepository.getSensorDailyTotals(sUserID,mWorkout.start,mWorkout.end).getValue();
                        Fitness.getHistoryClient(context, gsa).insertData(hisDataSet).addOnCompleteListener(task -> {
                            action_successful = task.isSuccessful();
                            bRefreshing = false;
                            if (action_successful) {
                                if (mWorkout != null) {
                                    if (mWorkout.stepCount > 0){
                                        DataSet stepSet = createStepCountDeltaDataSet(mWorkout.end,mWorkout.stepCount,device);
                                        try {
                                            if (stepSet != null)
                                                Fitness.getHistoryClient(context, gsa).insertData(stepSet);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                    if (sdtList != null && sdtList.size() > 0){
                                        float prevBPM = 1F;
                                        for (SensorDailyTotals sdt : sdtList){
                                            if ((sdt.deviceBPM > 0) && (sdt.deviceBPM != prevBPM)) {
                                                DataSet bpmSet = createBPMDataSet(sdt._id,sdt.deviceBPM,device);
                                                try {
                                                    if (bpmSet != null)
                                                        Fitness.getHistoryClient(context, gsa).insertData(bpmSet);
                                                }catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                            }
                                            prevBPM = sdt.deviceBPM;
                                        }
                                    }
                                    if (Utilities.isGymWorkout(mWorkout.activityID)) {
                                        if (workSets != null && (workSets.size() > 0)) {
                                            DataSource exerciseDataSource = new DataSource.Builder()
                                                    .setAppPackageName(Constants.ATRACKIT_ATRACKIT_CLASS)
                                                    .setDevice(device)
                                                    .setDataType(DataType.TYPE_WORKOUT_EXERCISE)
                                                    .setType(DataSource.TYPE_RAW)
                                                    .build();
                                            DataSet.Builder exerciseDataSetBuilder = DataSet.builder(exerciseDataSource);
                                            for (WorkoutSet s : workSets) {
                                                if ((s.exerciseName != null) && (s.repCount != null)
                                                        && (s.resistance_type != null) && (s.weightTotal != null)) {
                                                    DataPoint.Builder ptBuilder = DataPoint.builder(exerciseDataSource);
                                                    ptBuilder.setTimestamp(s.start, TimeUnit.MILLISECONDS);
                                                    try {
                                                        ptBuilder.setTimeInterval(s.start, s.end, TimeUnit.MILLISECONDS);
                                                    }catch (Exception e){
                                                        Log.e(LOG_TAG, "time interval fail " + e.getMessage());
                                                    }
                                                    if ((s.per_end_xy != null) && (s.per_end_xy.length() > 0))
                                                        ptBuilder.setField(Field.FIELD_EXERCISE, s.per_end_xy);
                                                    else
                                                        ptBuilder.setField(Field.FIELD_EXERCISE, s.exerciseName);
                                                    ptBuilder.setField(FIELD_REPETITIONS, s.repCount);
                                                    ptBuilder.setField(FIELD_RESISTANCE_TYPE, Math.toIntExact(s.resistance_type));
                                                    ptBuilder.setField(FIELD_RESISTANCE, s.weightTotal);
                                                    exerciseDataSetBuilder.add(ptBuilder.build());
                                                }
                                                if ((s.startBPM != null) || (s.endBPM != null)){
                                                    if ((s.startBPM != null) && (s.startBPM > 0) && (s.start > 0)){
                                                        DataSet bpm1Set = createBPMDataSet(s.start,Math.round(s.startBPM),device);
                                                        if (bpm1Set != null) Fitness.getHistoryClient(context, gsa).insertData(bpm1Set);
                                                    }
                                                    if ((s.endBPM != null) && (s.endBPM > 0) && (s.end > 0)){
                                                        DataSet bpm2Set = createBPMDataSet(s.end,Math.round(s.endBPM),device);
                                                        if (bpm2Set != null) Fitness.getHistoryClient(context, gsa).insertData(bpm2Set);
                                                    }
                                                }
                                            }
                                            DataSet dataSet = exerciseDataSetBuilder.build();
                                            try {

                                                Fitness.getHistoryClient(context, gsa).insertData(dataSet).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        action_successful = task.isSuccessful();
                                                        bRefreshing = false;
                                                        if (action_successful) {
                                                            for (WorkoutSet set : workSets) {
                                                                set.last_sync = timeMs;
                                                                workoutRepository.updateWorkoutSet(set);
                                                            }
                                                            ArrayList<WorkoutSet> arraySetList = new ArrayList<>();
                                                            arraySetList.addAll(workSets);
                                                            resultBundle.putParcelable(Workout.class.getSimpleName(), mWorkout);
                                                            resultBundle.putParcelableArrayList(WorkoutSet.class.getSimpleName(), arraySetList);
                                                        }
                                                    }
                                                });
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }

                                        }
                                    }
                                    resultBundle.putLong(KEY_FIT_WORKOUTID, mWorkout._id);
                                }
                            }
                        });
                        break;
                    case Constants.TASK_ACTION_READ_SESSION:
                        DataSource dataSource = new DataSource.Builder()
                                //  .setDevice(device)
                                .setDataType(DataType.TYPE_WORKOUT_EXERCISE)
                                .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                                .setType(DataSource.TYPE_RAW)
                                .build();
                        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                                .setTimeInterval(mWorkout.start, mWorkout.end, TimeUnit.MILLISECONDS)
                                .read(dataSource)
                                .enableServerQueries()
                                .readSessionsFromAllApps()
                                .build();
                        Task<SessionReadResponse> readTask = Fitness.getSessionsClient(context, gsa)
                                .readSession(readRequest);
                        readTask.addOnCompleteListener(sessionCompleter);
                        break;

                    case Constants.TASK_ACTION_READ_GOALS:
                        GoalsReadRequest goalsReadRequest = new GoalsReadRequest.Builder()
                                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                .addDataType(DataType.TYPE_DISTANCE_DELTA)
                                .addDataType(DataType.TYPE_CALORIES_EXPENDED)
                                .addDataType(DataType.TYPE_HEART_POINTS)
                                .addDataType(DataType.TYPE_MOVE_MINUTES)
                                .build();
                        Task<List<Goal>> goalResponse = Fitness.getGoalsClient(context, gsa).readCurrentGoals(goalsReadRequest);
                        goalResponse.addOnCompleteListener(task -> {
                            bRefreshing = false;
                            action_successful = task.isSuccessful();
                            if (action_successful) {
                                List<Goal> goals = task.getResult();
                                int index = 0;
                                for (Goal goal : goals) {
                                    int goalType = goal.getObjectiveType();
                                    String sName = (goal.getActivityName() != null) ? goal.getActivityName() : Constants.ATRACKIT_EMPTY;
                                    String sMetricName = Constants.ATRACKIT_EMPTY;
                                    String sValue = Constants.ATRACKIT_EMPTY;
                                    if (goalType == Goal.OBJECTIVE_TYPE_METRIC) {
                                        double dVal = goal.getMetricObjective().getValue();
                                        sValue = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, dVal);
                                        sMetricName = goal.getMetricObjective().getDataTypeName();
                                    }
                                    if (goalType == Goal.OBJECTIVE_TYPE_DURATION) {
                                        sValue = String.format(Locale.getDefault(), "%d", goal.getDurationObjective().getDuration(TimeUnit.MILLISECONDS));
                                        sMetricName = "duration";
                                    }
                                    if (goalType == Goal.OBJECTIVE_TYPE_FREQUENCY) {
                                        sValue = String.format(Locale.getDefault(), "%d", goal.getFrequencyObjective().getFrequency());
                                        sMetricName = "frequency";
                                    }
                                    if (sValue.length() > 0) {
                                        resultBundle.putString(Constants.KEY_FIT_VALUE + index, sValue);
                                        resultBundle.putString(Constants.KEY_FIT_REC + index, sMetricName);
                                        resultBundle.putInt(Constants.KEY_FIT_TYPE + index, goalType);
                                        resultBundle.putString(Constants.KEY_FIT_NAME + index, sName);
                                        resultBundle.putString(Constants.KEY_FIT_VALUE + index, sValue);
                                    }
                                    index += 1;
                                    Log.d(LOG_TAG, "Goal " + index + " " + sValue + " " + sMetricName);
                                }

                            }
                        });
                        break;
                    case READ_ACT_EXER_BY_TIMES:
                    case READ_STEP_DELTA_BY_DAY:
                    case READ_ACT_SEG_BY_SUMMARY:
                        if (mWorkout == null) {
                            mCalendar.setTimeInMillis(System.currentTimeMillis());
                            mCalendar.set(Calendar.HOUR, 0);
                            mCalendar.set(Calendar.MINUTE, 0);
                            endTime = mCalendar.getTimeInMillis();
                            mCalendar.set(Calendar.DAY_OF_YEAR, -3);
                            startTime = mCalendar.getTimeInMillis();
                        }
                        else {
                            startTime = mWorkout.start;
                            endTime = mWorkout.end;
                        }
                        if (session_action == READ_STEP_DELTA_BY_DAY)
                            builder.aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                                    .bucketByTime(1, TimeUnit.DAYS)
                                    .enableServerQueries();// Used to retrieve data from cloud.
                        if (session_action == READ_ACT_EXER_BY_TIMES)
                            builder.aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                                    .bucketByTime(1, TimeUnit.DAYS)
                                    .enableServerQueries();// Used to retrieve data from cloud.
                        if (session_action == READ_ACT_SEG_BY_SUMMARY) {
                            List<DataType> dt = Arrays.asList(DataType.TYPE_ACTIVITY_SEGMENT, DataType.TYPE_HEART_RATE_BPM, DataType.TYPE_STEP_COUNT_DELTA);
                            List<DataType> ag = Arrays.asList(DataType.AGGREGATE_ACTIVITY_SUMMARY, DataType.AGGREGATE_HEART_RATE_SUMMARY, DataType.AGGREGATE_STEP_COUNT_DELTA);
                            builder = queryDataWithBuckets(startTime, endTime, dt, ag, 1, TimeUnit.MINUTES, BUCKET_TYPE_TIME);
                            builder.enableServerQueries();// Used to retrieve data from cloud.
                        }
                        break;
                }
            } catch (Exception e) {
                if (e instanceof  IllegalStateException){
                    Log.e(LOG_TAG, e.getLocalizedMessage());  // no connection
                }
                e.printStackTrace();
                mResultReceiver.send(505, null);
                return;
            }

            long waitStart = SystemClock.elapsedRealtime();
            while (bRefreshing) {
                Log.w(LOG_TAG, "sleeping on refresh" + " action " + session_action);
                SystemClock.sleep(5000);
                if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(1)) {
                    Log.d(LOG_TAG, "FitSyncJob: " + " action INCOMPLETE AFTER 1 min " + session_action);
                    bRefreshing = false;
                }
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_24Full, Locale.getDefault());
            Log.d(LOG_TAG, "Range Start: " + dateFormat.format(startTime) + " " + existingWorkoutArrayList.size() + " " + workoutArrayList.size());
            Log.d(LOG_TAG, "Range End: " + dateFormat.format(endTime) + " " + existingWorkoutSetArrayList.size() + " " + workoutSetArrayList.size());
            Log.d(LOG_TAG, "FitSyncJob: " + " action " + session_action);
            if (configSetup == null) {
                configList = workoutRepository.getConfigLikeName(context.getString(R.string.state_setup),Constants.ATRACKIT_ATRACKIT_CLASS);
                if (configList != null && (configList.size() > 0)) {
                    configSetup = configList.get(0);
                }
            }
            final boolean bProcessHere = isInternal;
            Log.w(LOG_TAG, "finishing " + action_successful + " action " + session_action);
            if (action_successful) {
                resultBundle.putInt(Constants.KEY_FIT_VALUE, 1);  // success indicator
                if ((udtList != null) && (udtList.size() > 0)) {
                    Log.d(LOG_TAG, "user daily total size " + udtList.size());
                    String sKey = UserDailyTotals.class.getSimpleName();
                    if (udtList.size() == 1)
                        resultBundle.putParcelable(sKey, udtList.get(0));
                    else {
                        sKey += "_list";
                        resultBundle.putParcelableArrayList(sKey, udtList);
                    }
                }
                if ((workoutArrayList != null) && (workoutArrayList.size() > 0)) {
                    Log.d(LOG_TAG, "workout size " + workoutArrayList.size());
                    String sKey = Constants.KEY_LIST_WORKOUTS;
                    resultBundle.putParcelableArrayList(sKey, workoutArrayList);
                }
                if ((workoutSetArrayList != null) && (workoutSetArrayList.size() > 0)) {
                    Log.d(LOG_TAG, "workoutset size " + workoutSetArrayList.size());
                    String sSetKey = Constants.KEY_LIST_SETS;
                    resultBundle.putParcelableArrayList(sSetKey, workoutSetArrayList);
                }
                if (session_action == Constants.TASK_ACTION_STOP_SESSION){
                    if ((workoutArrayList != null) && (workoutArrayList.size() > 0)) {
                        for(Workout workout: workoutArrayList){
                            if (Utilities.isGymWorkout(workout.activityID)) {
                                Data.Builder builder2 = new Data.Builder();
                                builder2.putString(KEY_FIT_USER, workout.userID);
                                builder2.putLong(KEY_FIT_WORKOUTID, workout._id);
                                OneTimeWorkRequest workRequest =
                                        new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                                                .setInputData(builder2.build())
                                                .build();
                                mWorkManager.enqueue(workRequest);
                            }
                        }
                    }else{
                        if (mWorkout != null &&  Utilities.isGymWorkout(mWorkout.activityID)) {
                            Data.Builder builder2 = new Data.Builder();
                            builder2.putString(KEY_FIT_USER, mWorkout.userID);
                            builder2.putLong(KEY_FIT_WORKOUTID, mWorkout._id);
                            OneTimeWorkRequest workRequest =
                                    new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                                            .setInputData(builder2.build())
                                            .build();
                            mWorkManager.enqueue(workRequest);
                        }

                    }
                }
                if (configSetup != null) {
                    configSetup.stringValue1 = null;
                    workoutRepository.updateConfig(configSetup);
                }
                mResultReceiver.send(200, resultBundle);
            }
            else {
                if (configSetup != null) {
                    configSetup.stringValue1 = null;
                    workoutRepository.updateConfig(configSetup);
                }
                resultBundle.putInt(Constants.KEY_FIT_VALUE, 0);
                mResultReceiver.send(iErrorCode, resultBundle);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (configSetup != null) {
                configSetup.stringValue1 = null;
                workoutRepository.updateConfig(configSetup);
            }
            resultBundle.putInt(Constants.KEY_FIT_VALUE, 0);  // success indicator
            mResultReceiver.send(505, null);
        }
    }
    // [end of onHandleWork]

    // session details = SETS
    private final OnCompleteListener<SessionReadResponse> sessionCompleter = task -> {
        try {
            bRefreshing = false;
            action_successful = task.isSuccessful();
            if (action_successful) {
                SessionReadResponse sessionReadResponse = task.getResult();
                List<Session> sessions = sessionReadResponse.getSessions();
                Log.d(LOG_TAG, "Session read was successful. Number of returned sessions is: " + sessions.size());
                for (Session session : sessions) {
                    Workout workout = new Workout();
                    workout.start = session.getStartTime(TimeUnit.MILLISECONDS);
                    if (workout.start > 0) workout._id = workout.start;
                    workout.userID = sUserID;
                    workout.end = session.getEndTime(TimeUnit.MILLISECONDS);
                    if (session.hasActiveTime())
                        workout.duration = session.getActiveTime(TimeUnit.MILLISECONDS);
                    workout.activityName = session.getActivity();
                    workout.identifier = session.getIdentifier();
                    if (session.getName() != null && session.getName().length() > 0)
                        workout.name = session.getName();
                    if (session.getAppPackageName() != null)
                        workout.packageName = session.getAppPackageName();
                    Log.w(LOG_TAG, "Session read : " + workout.toString());
                    workoutArrayList.add(workout);
                    List<DataSet> dataSets = sessionReadResponse.getDataSet(session);
                    for (DataSet dataSet : dataSets) {
                        List<DataPoint> dpList = dataSet.getDataPoints();
                        for (DataPoint dp : dpList) {
                            Bundle result = DataPointToResultBundle(dp);
                            if (result.containsKey(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
                                WorkoutSet s = result.getParcelable(DataType.TYPE_WORKOUT_EXERCISE.getName());
                                s._id = getDayStart(s.start) + workoutSetArrayList.size();
                                s.last_sync = s._id;
                                Log.w(LOG_TAG, "adding set " + s.toString());
                                workoutSetArrayList.add(s);
                            }
                            if (result.containsKey(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
                                Workout w = result.getParcelable(DataType.TYPE_ACTIVITY_SEGMENT.getName());
                                if (w._id == 0L) w._id = getDayStart(w.start) + w.activityID;
                                w._id = w._id + workoutArrayList.size();
                                w.last_sync = w._id;
                                if (!Utilities.isDetectedActivity(w.activityID)) {
                                    Log.w(LOG_TAG, "adding workout segment " + w.toString());
                                    workoutArrayList.add(w);
                                }
                            }
                        }
                    }
                } // each session
            } // successful
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            bRefreshing = false;
            action_successful = false;
        }
    };
    private final OnCompleteListener<DataReadResponse> segmentCompleteListener = task -> {
        try {
            if (task.getResult().getStatus().isSuccess()) {
                action_successful = true;
                for (DataSet dataSet : task.getResult().getDataSets()) {
                    List<DataPoint> dpList = dataSet.getDataPoints();
                    for (DataPoint dp : dpList) {
                        Log.w(LOG_TAG, "received data-point " + dp.getDataType().getName() + " dps " + Utilities.getTimeDateString(dp.getStartTime(TimeUnit.MILLISECONDS)));
                        Bundle resultBundle = DataPointToResultBundle(dp);
                        if (resultBundle.containsKey(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
                            Workout w = resultBundle.getParcelable(DataType.TYPE_ACTIVITY_SEGMENT.getName());
                            w.userID = sUserID;
                            if (w.duration == 0) w.duration = w.end - w.start;
                            if (w._id == 0) w._id = w.start;
                            workoutArrayList.add(w);
                        }
                    }
                }
            }
            else {
                Log.d(LOG_TAG, "task read not successful " + task.getResult().toString());
                action_successful = false;
            }
            bRefreshing = false;
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            bRefreshing = false;
            action_successful = false;
        }
    };
    private final OnCompleteListener<DataReadResponse> historyReadCompleter = task -> {
        try {
            bRefreshing=false;
            if (task.getResult().getStatus().isSuccess()) {
                int iRet = task.getResult().getBuckets().size();
                if (iRet > 0) {
                    for (Bucket bucket : task.getResult().getBuckets()) {
                        List<DataSet> dataSets = bucket.getDataSets();
                        for (DataSet dataSet : dataSets) {
                            List<DataPoint> dpList = dataSet.getDataPoints();
                            Workout w = null;
                            int stepCount = 0;
                            for (DataPoint dp : dpList) {
                                Bundle result = DataPointToBundle(dp);
                                if (result.containsKey(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
                                    WorkoutSet s = result.getParcelable(DataType.TYPE_WORKOUT_EXERCISE.getName());
                                    s._id = Utilities.getDayStart(mCalendar,s.start) + fitWorkoutSetList.size();
                                    s.last_sync = s._id;
                                    s.userID = sUserID;
                                    s.setCount = 1;
                                    s.deviceID = sDeviceID;
                                    Log.w(LOG_TAG, "adding set " + s.toString());
                                    fitWorkoutSetList.add(s);
                                }

                                if (result.containsKey(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
                                    w = result.getParcelable(DataType.TYPE_ACTIVITY_SEGMENT.getName());
                                    if (w._id == 0L) w._id = w.start;
                                    w._id = w._id + fitWorkoutList.size();
                                    w.userID = sUserID;
                                    w.deviceID = sDeviceID;
                                }
                                if (result.containsKey(MAP_STEPS)){
                                    stepCount = result.getInt(MAP_STEPS,0);
                                    if (w != null)
                                        if (stepCount > 0 && w.stepCount < stepCount) w.stepCount = stepCount;
                                }
                            }
                            if (w != null){
                                if (stepCount > 0 && w.stepCount < stepCount) w.stepCount = stepCount;
                                fitWorkoutList.add(w);
                                Log.w(LOG_TAG, "adding workout segment " + referencesTools.workoutShortText(w));
                            }
                        }
                    }
                }
                bRefreshing = false;
            }
            else handleTaskError(task);
        }catch(Exception e){
            handleTaskError(task);
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "historyReadCompleter " + e.getMessage());
            bRefreshing = false;
        }
    };
    private final OnSuccessListener<DataReadResponse> exerciseResponse = task -> {
        try{
                List<DataSet> listDataSet = task.getDataSets();
                if (listDataSet != null)
                    for (DataSet dataSet : listDataSet) {
                        List<DataPoint> dpList = dataSet.getDataPoints();
                        for (DataPoint dp : dpList) {
                            Bundle result = DataPointToBundle(dp);
                            if (result.containsKey(DataType.TYPE_WORKOUT_EXERCISE.getName())){
                                WorkoutSet ws = result.getParcelable(DataType.TYPE_WORKOUT_EXERCISE.getName());
                                if ((existingWorkoutSetArrayList.size() == 0) || !isExistingOrOverLappingSet(ws)) {
                                    ws.userID = sUserID;
                                    ws.deviceID = sDeviceID2;
                                    ws.workoutID = mWorkout._id;
                                    String sName = ws.exerciseName;
                                    sName = sName.trim();
                                    int sLen = sName.length();
                                    if (sLen == 0) continue; // jump the rest of this if not set!
                                    //if (sName.charAt(sLen-1) == 's') sName = sName.substring(0,sLen-1);
                                    sName = sName.trim() + Constants.ATRACKIT_PERCENT_SIGN;

                                    String sPackage = ws.score_card;
                                    List<Exercise> exerciseList = new ArrayList<>();
                                    List<Exercise> exerciseOtherList = new ArrayList<>();
                                    try {
                                        // try best match first
                                        if (sPackage.equals(Constants.ATRACKIT_ATRACKIT_CLASS)) {
                                            exerciseList = workoutRepository.getExercisesByName(sName);  // new getExerciseTask(mExerciseDao).execute(sName).get(2, TimeUnit.MINUTES);
                                        }else
                                            exerciseList = workoutRepository.getExercisesByWorkoutExerciseName(sName); //new getExerciseTask(mExerciseDao).execute(Constants.ATRACKIT_EMPTY, sName).get(2, TimeUnit.MINUTES);
                                        // try like name
                                        if ((exerciseList == null) || (exerciseList.size() == 0)) { // not found try google name
                                            exerciseList = workoutRepository.getExercisesLikeName(sName); //new getExerciseTask(mExerciseDao).execute(Constants.ATRACKIT_EMPTY, sName).get(2, TimeUnit.MINUTES);
                                            exerciseOtherList = workoutRepository.getExercisesLikeOtherNames(sName);
                                            if ((exerciseOtherList != null) && (exerciseOtherList.size() > 0)){
                                                if ((exerciseList == null) || (exerciseList.size() == 0)) exerciseList = exerciseOtherList;
                                                else {
                                                    for(Exercise e1 : exerciseOtherList){
                                                        if (!exerciseList.contains(e1)) exerciseList.add(e1);
                                                    }
                                                }
                                            }
                                            if ((exerciseList == null) || (exerciseList.size() == 0))
                                                exerciseList = workoutRepository.getExercisesLikeWorkoutExerciseName(sName);  // new getExerciseTask(mExerciseDao).execute(sName).get(2, TimeUnit.MINUTES);
                                        }
                                        // breakout the components
                                        if ((exerciseList == null) || (exerciseList.size() == 0)){
                                            if (sName.toLowerCase().contains("machine")) {
                                                sName = ws.exerciseName.toLowerCase().replace("machine",Constants.ATRACKIT_EMPTY);
                                                exerciseList = workoutRepository.getExercisesLikeName(sName); //exerciseList = new getExerciseTask(mExerciseDao).execute(sName).get(2, TimeUnit.MINUTES);
                                                if ((exerciseList == null) || (exerciseList.size() == 0)) exerciseList = workoutRepository.getExercisesLikeOtherNames(sName);
                                            }
                                            if (sName.toLowerCase().contains("dumbbell")) {
                                                sName = ws.exerciseName.toLowerCase().replace("dumbbell",Constants.ATRACKIT_EMPTY);
                                                sName = sName.trim();
                                                exerciseList = workoutRepository.getExercisesLikeName(sName); //exerciseList = new getExerciseTask(mExerciseDao).execute(sName).get(2, TimeUnit.MINUTES);
                                                if ((exerciseList == null) || (exerciseList.size() == 0)) exerciseList = workoutRepository.getExercisesLikeOtherNames(sName);
                                            }
                                            if (sName.toLowerCase().contains("barbell")) {
                                                sName = ws.exerciseName.toLowerCase().replace("barbell",Constants.ATRACKIT_EMPTY);
                                                sName = sName.trim();
                                                exerciseList = workoutRepository.getExercisesLikeName(sName); // exerciseList = new getExerciseTask(mExerciseDao).execute(sName).get(2, TimeUnit.MINUTES);
                                                if ((exerciseList == null) || (exerciseList.size() == 0)) exerciseList = workoutRepository.getExercisesLikeOtherNames(sName);
                                            }
                                        }
                                        sName = ws.exerciseName.toLowerCase();
                                        if ((sName.contains(ATRACKIT_SPACE)) && ((exerciseList == null) || (exerciseList.size() == 0))){
                                            sName = sName.replace(ATRACKIT_SPACE,".");
                                            if (sName.toLowerCase().contains(".machine")) {
                                                sName = sName.replace(".machine",Constants.ATRACKIT_EMPTY);
                                                sName = sName.trim();
                                                exerciseList = workoutRepository.getExercisesLikeWorkoutExerciseName(sName); //exerciseList = new getExerciseTask(mExerciseDao).execute(sName).get(2, TimeUnit.MINUTES);
                                                if ((exerciseList == null) || (exerciseList.size() == 0)) exerciseList = workoutRepository.getExercisesLikeOtherNames(sName);
                                            }else
                                            if (sName.toLowerCase().contains("machine.")) {
                                                sName = sName.replace("machine.",Constants.ATRACKIT_EMPTY);
                                                sName = sName.trim();
                                                exerciseList = workoutRepository.getExercisesLikeWorkoutExerciseName(sName); //exerciseList = new getExerciseTask(mExerciseDao).execute(sName).get(2, TimeUnit.MINUTES);
                                                if ((exerciseList == null) || (exerciseList.size() == 0)) exerciseList = workoutRepository.getExercisesLikeOtherNames(sName);
                                            }
                                            if (sName.toLowerCase().contains(".dumbbell") && ((exerciseList == null) || (exerciseList.size() == 0))) {
                                                sName = sName.replace(".dumbbell",Constants.ATRACKIT_EMPTY);
                                                sName = sName.trim();
                                                exerciseList = workoutRepository.getExercisesLikeWorkoutExerciseName(sName); //exerciseList = new getExerciseTask(mExerciseDao).execute(sName).get(2, TimeUnit.MINUTES);
                                                if ((exerciseList == null) || (exerciseList.size() == 0)) exerciseList = workoutRepository.getExercisesLikeOtherNames(sName);
                                            }else
                                            if (sName.toLowerCase().contains("dumbbell.")) {
                                                sName = sName.replace("dumbbell.",Constants.ATRACKIT_EMPTY);
                                                sName = sName.trim();
                                                exerciseList = workoutRepository.getExercisesLikeWorkoutExerciseName(sName); //exerciseList = new getExerciseTask(mExerciseDao).execute(sName).get(2, TimeUnit.MINUTES);
                                                if ((exerciseList == null) || (exerciseList.size() == 0)) exerciseList = workoutRepository.getExercisesLikeOtherNames(sName);
                                            }
                                            if (sName.toLowerCase().contains(".barbell") && ((exerciseList == null) || (exerciseList.size() == 0))) {
                                                sName = sName.replace(".barbell",Constants.ATRACKIT_EMPTY);
                                                sName = sName.trim();
                                                exerciseList = workoutRepository.getExercisesLikeWorkoutExerciseName(sName); // exerciseList = new getExerciseTask(mExerciseDao).execute(sName).get(2, TimeUnit.MINUTES);
                                                if ((exerciseList == null) || (exerciseList.size() == 0)) exerciseList = workoutRepository.getExercisesLikeOtherNames(sName);
                                            }else
                                            if (sName.toLowerCase().contains("barbell.")) {
                                                sName = sName.replace("barbell.",Constants.ATRACKIT_EMPTY);
                                                sName = sName.trim();
                                                exerciseList = workoutRepository.getExercisesLikeWorkoutExerciseName(sName); // exerciseList = new getExerciseTask(mExerciseDao).execute(sName).get(2, TimeUnit.MINUTES);
                                                if ((exerciseList == null) || (exerciseList.size() == 0)) exerciseList = workoutRepository.getExercisesLikeOtherNames(sName);
                                            }
                                        }

                                    }catch (Exception e){
                                        Log.e(LOG_TAG, e.getMessage());
                                    }
                                    sName = ws.exerciseName;
                                    Log.w(LOG_TAG, "adding set " + sName + " set " + ws.toString());
                                    if ((exerciseList != null) && (exerciseList.size() >= 1)) {
                                        if ((exerciseList.size() == 1)) {
                                            Exercise found = exerciseList.get(0);
                                            ws.exerciseID = found._id;
                                            ws.exerciseName = found.name;
                                            ws.per_end_xy = found.workoutExercise;
                                            List<Bodypart> bpLookupList = workoutRepository.getBodyPartById(found.first_BPID);
                                            if ((bpLookupList != null) && (bpLookupList.size() > 0)){
                                                ws.regionID = bpLookupList.get(0).regionID;
                                                ws.regionName = bpLookupList.get(0).regionName;
                                            }
                                            ws.bodypartID = found.first_BPID;
                                            ws.bodypartName = found.first_BPName;
                                            if (((ws.resistance_type == null) || (ws.resistance_type == 0))
                                                    && ((found.resistanceType != null) && (found.resistanceType != ws.resistance_type)))
                                                ws.resistance_type = found.resistanceType;
                                        }
                                        else{
                                            if ((ws.resistance_type != null) && (ws.resistance_type != 0)){
                                                for(Exercise ex : exerciseList){
                                                    if ((ex.resistanceType != null) && (ex.resistanceType == ws.resistance_type)){
                                                        ws.exerciseID = ex._id;
                                                        ws.exerciseName = ex.name;
                                                        ws.per_end_xy = ex.workoutExercise;
                                                        ws.bodypartID = ex.first_BPID;
                                                        ws.bodypartName = ex.first_BPName;
                                                        List<Bodypart> bpLookupList = workoutRepository.getBodyPartById(ex.first_BPID);
                                                        if ((bpLookupList != null) && (bpLookupList.size() > 0)){
                                                            ws.regionID = bpLookupList.get(0).regionID;
                                                            ws.regionName = bpLookupList.get(0).regionName;
                                                        }
                                                        break;
                                                    }
                                                }
                                            }else{
                                                Exercise found = exerciseList.get(0);
                                                ws.exerciseID = found._id;
                                                ws.exerciseName = found.name;
                                                ws.per_end_xy = found.workoutExercise;
                                                List<Bodypart> bpLookupList = workoutRepository.getBodyPartById(found.first_BPID);
                                                if ((bpLookupList != null) && (bpLookupList.size() > 0)){
                                                    ws.regionID = bpLookupList.get(0).regionID;
                                                    ws.regionName = bpLookupList.get(0).regionName;
                                                }
                                                ws.bodypartID = found.first_BPID;
                                                ws.bodypartName = found.first_BPName;
                                                if ((ws.resistance_type != null) && (found.resistanceType != null))
                                                    if ((ws.resistance_type == 0) && (found.resistanceType != ws.resistance_type))
                                                        ws.resistance_type = found.resistanceType;
                                            }
                                        }
                                    } else {
                                        Exercise notFound = new Exercise();
                                        notFound._id = ws._id;
                                        notFound.name = sName;
                                        notFound.bodypartCount = -1;  // flag to indicate missing bodypart
                                        if (ws.resistance_type != null) {
                                            notFound.resistanceType = ws.resistance_type;
                                            notFound.resistanceTypeName = Utilities.getResistanceType(ws.resistance_type);
                                        }
                                        if (ws.weightTotal != null) notFound.lastAvgWeight = ws.weightTotal;
                                        notFound.lastSets = exerciseList.size();
                                        notFound.workoutExercise = Long.toString(ws._id);
                                        if (ws.repCount != null) notFound.lastReps = ws.repCount;
                                        notFound.lastUpdated = System.currentTimeMillis();

                                        boolean bAlreadyMissing = false;
                                        if (exercisesMissingList.size() > 0)
                                            for (Exercise existing : exercisesMissingList) {
                                                if (existing.name.equals(notFound.name)) {
                                                    bAlreadyMissing = true;
                                                    existing.workoutExercise += "," + Long.toString(ws._id);
                                                    break; // finish looping
                                                }
                                            }
                                        if (!bAlreadyMissing)
                                            exercisesMissingList.add(notFound);
                                    }
                                    loadingWorkoutSetList.add(ws);
                                }
                            }
                        }
                    }
                bRefreshing = false;

        }
        catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "exerciseResponse " + e.getMessage());
            bRefreshing = false;
        }
    };
    private void readDailyFitHistory(long starting, long ending) {
        // Build a workout read request
        String sUserId = gsa.getId();
        if (ending == 0) ending = System.currentTimeMillis();
        final long startID = getDayStart(starting);
        mCalendar.setTimeInMillis(startID);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-dd HH:mm:ss z", Locale.getDefault());

        Log.d(LOG_TAG, simpleDateFormat.format(starting) + " to " + simpleDateFormat.format(ending));
/*        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA)
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_POWER_SAMPLE)
                .aggregate(DataType.TYPE_MOVE_MINUTES)
                .aggregate(DataType.TYPE_HEART_POINTS)
                .aggregate(DataType.TYPE_SPEED)
                .aggregate(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(starting, ending, TimeUnit.MILLISECONDS)
                .bucketByTime(1,TimeUnit.DAYS)
                .setLimit(1000)
                .enableServerQueries().build();// Used to retrieve data from cloud.*/

        DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_ACTIVITY_SEGMENT)
                .setLimit(1000)
                .setTimeRange(starting, ending, TimeUnit.MILLISECONDS)
                .enableServerQueries() // Used to retrieve data from cloud.
                .build();
        try {
            Task<DataReadResponse> response = Fitness.getHistoryClient(getApplicationContext(), gsa).readData(dataReadRequest);
            response.addOnCompleteListener(segmentCompleteListener);
            long waitStart = SystemClock.elapsedRealtime();
            Tasks.await(response, 5, TimeUnit.MINUTES);
            while (bRefreshing) {
                Log.w(LOG_TAG, "sleeping on refresh" + " action " + session_action);
                SystemClock.sleep(5000);
                if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                    bRefreshing = false;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "readFitHistory Error " + e.getMessage());
            bRefreshing = false;
        }
    }

    private Bundle DataPointToResultBundle(DataPoint dp) {
        Bundle resultBundle = new Bundle();
        if (dp.getDataType().getName().equals(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())) {
            Workout workout = new Workout();
            //   workout.name = dp.getDataType().getName();
            referencesTools.init(getApplicationContext());
            workout.activityID = (long) dp.getValue(Field.FIELD_ACTIVITY).asInt();
            workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
            workout.identifier = referencesTools.getFitnessActivityIdentifierById(workout.activityID);
            workout.scoreTotal = dp.getValue(Field.FIELD_NUM_SEGMENTS).asInt();
            workout.duration = (long) dp.getValue(Field.FIELD_DURATION).asInt();
            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            if (startTime == 0)
                startTime = System.currentTimeMillis();
            workout.start = startTime;
            long endTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
            if (endTime == 0 || (endTime == startTime)) dp.getEndTime(TimeUnit.MILLISECONDS);
            if (endTime > 0) workout.end = endTime;
            workout._id = workout.start + workout.activityID;
            workout.userID = sUserID;
            if ((dp.getOriginalDataSource().getAppPackageName() != null)
                    && (dp.getOriginalDataSource().getAppPackageName().length() > 0))
                workout.packageName = dp.getOriginalDataSource().getAppPackageName();
            else {
                if (Utilities.isDetectedActivity(workout.activityID))
                    workout.packageName = Constants.ATRACKIT_PLAY_CLASS;
                else
                    workout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
            }
            resultBundle.putParcelable(dp.getDataType().getName(), workout);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
            Workout workout = new Workout();
            workout.wattsTotal = dp.getValue(Field.FIELD_WATTS).asFloat();
            Log.d(LOG_TAG, "power.sample " + workout.activityName + " " + workout.setCount);
            resultBundle.putParcelable(dp.getDataType().getName(), workout);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
            WorkoutSet set = new WorkoutSet();
            try {
                long timeStamp = dp.getTimestamp(TimeUnit.MILLISECONDS);
                if (dp.getValue(Field.FIELD_DURATION).isSet())
                    set.duration = (long) dp.getValue(Field.FIELD_DURATION).asInt();
                if (timeStamp > 0) {
                    set.start = timeStamp;
                    if (set.duration > 0)
                        set.end = timeStamp + set.duration;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "set load error " + e.getMessage());
            }
            if ((dp.getStartTime(TimeUnit.MILLISECONDS) > 0) && (set.start == 0)) {
                set.start = dp.getStartTime(TimeUnit.MILLISECONDS);
            }
            if ((dp.getEndTime(TimeUnit.MILLISECONDS) > 0) && (set.end == 0)) {
                set.end = dp.getEndTime(TimeUnit.MILLISECONDS);
            }
            if (set.start > 0) set._id = (set.start);
            else set._id = (System.currentTimeMillis());
            String sExercise = dp.getValue(Field.FIELD_EXERCISE).asString();
            set.exerciseName = sExercise;
            set.per_end_xy = sExercise;
            set.resistance_type = (long) dp.getValue(Field.FIELD_RESISTANCE_TYPE).asInt();
            set.workoutID = null;
            set.weightTotal = dp.getValue(Field.FIELD_RESISTANCE).asFloat();
            set.repCount = dp.getValue(Field.FIELD_REPETITIONS).asInt();
            if ((set.weightTotal > 0) && (set.repCount > 0))
                set.wattsTotal = (set.weightTotal * set.repCount);
            set.activityName = referencesTools.getFitnessActivityTextById(Constants.WORKOUT_TYPE_STRENGTH);
            set.activityID = Constants.WORKOUT_TYPE_STRENGTH;
            set.userID = sUserID;
            set.last_sync = set._id;
            if ((dp.getOriginalDataSource().getAppPackageName() != null)
                    && (dp.getOriginalDataSource().getAppPackageName().length() > 0))
                set.score_card = dp.getOriginalDataSource().getAppPackageName();
            else
                set.score_card = Constants.ATRACKIT_ATRACKIT_CLASS;
            Log.d(LOG_TAG, "dp " + referencesTools.workoutSetShortText(set));
            resultBundle.putParcelable(dp.getDataType().getName(), set);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
            Workout workout = new Workout();
            workout.activityID = (long) dp.getValue(Field.FIELD_ACTIVITY).asInt();
            workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
            workout.identifier = referencesTools.getFitnessActivityIdentifierById(workout.activityID);
            try {
                long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                workout.start = startTime;
                workout._id = (workout.start) + workout.activityID;
                long endTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
                if (endTime == 0 || (endTime == startTime)) dp.getEndTime(TimeUnit.MILLISECONDS);
                if (endTime > 0) workout.end = endTime;
            } catch (Exception e) {
                Log.e(LOG_TAG, "set load error " + e.getMessage());
            }
            workout.userID = sUserID;
            workout.duration = workout.end - workout.start;
            if ((dp.getOriginalDataSource().getAppPackageName() != null)
                    && (dp.getOriginalDataSource().getAppPackageName().length() > 0))
                workout.packageName = dp.getOriginalDataSource().getAppPackageName();
            else {
                if (Utilities.isDetectedActivity(workout.activityID))
                    workout.packageName = Constants.ATRACKIT_PLAY_CLASS;
                else
                    workout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
            }
            resultBundle.putParcelable(dp.getDataType().getName(), workout);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_MOVE_MINUTES.getName())) {
            dp.getValue(Field.FIELD_DURATION);
            int minutes = dp.getValue(Field.FIELD_DURATION).asInt();
            resultBundle.putInt(dp.getDataType().getName(), minutes);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_CALORIES_EXPENDED.getName())) {
            float calories = dp.getValue(Field.FIELD_CALORIES).asFloat();
            resultBundle.putFloat(dp.getDataType().getName(), calories);
        } else if (dp.getDataType().getName().contains(DataType.TYPE_DISTANCE_DELTA.getName())) {
            float distance = dp.getValue(Field.FIELD_DISTANCE).asFloat();
            resultBundle.putFloat(dp.getDataType().getName(), distance);
        } else if (dp.getDataType().getName().contains(DataType.AGGREGATE_HEART_POINTS.getName())) {
            float intensity = dp.getValue(Field.FIELD_INTENSITY).asFloat();
            int duration = dp.getValue(Field.FIELD_DURATION).asInt();
            Workout w = new Workout();
            w.wattsTotal = intensity;
            w.duration = duration;
            resultBundle.putParcelable(dp.getDataType().getName(), w);
        } else if (dp.getDataType().getName().equals(DataType.AGGREGATE_HEART_RATE_SUMMARY.getName())) {
            float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
            float min = dp.getValue(Field.FIELD_MIN).asFloat();
            float max = dp.getValue(Field.FIELD_MAX).asFloat();
            resultBundle.putFloat(Constants.MAP_BPM_AVG, avg);
            resultBundle.putFloat(Constants.MAP_BPM_MIN, min);
            resultBundle.putFloat(Constants.MAP_BPM_MAX, max);
        } else if (dp.getDataType().getName().equals(DataType.AGGREGATE_SPEED_SUMMARY.getName())) {
            float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
            float min = dp.getValue(Field.FIELD_MIN).asFloat();
            float max = dp.getValue(Field.FIELD_MAX).asFloat();
            resultBundle.putFloat(Constants.MAP_SPEED_AVG, avg);
            resultBundle.putFloat(Constants.MAP_SPEED_MIN, min);
            resultBundle.putFloat(Constants.MAP_SPEED_MAX, max);
        } else if (dp.getDataType().getName().equals(DataType.AGGREGATE_STEP_COUNT_DELTA.getName())) {
            Workout workout = new Workout();
            //   workout.name = dp.getDataType().getName();
            workout.activityID = WORKOUT_TYPE_STEPCOUNT;
            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            if (startTime == 0)
                startTime = System.currentTimeMillis();
            workout.start = startTime;
            long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
            if (endTime > 0) workout.end = endTime;
            workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
            workout.stepCount = dp.getValue(Field.FIELD_STEPS).asInt();
            workout.userID = sUserID;
            workout._id = workout.start;
            resultBundle.putParcelable(dp.getDataType().getName(), workout);
            resultBundle.putInt(Constants.MAP_STEPS, workout.stepCount);
        }
        return resultBundle;
    }
    private void DataPointToMetaData(DataPoint dp){
        if (dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
            mWorkoutMeta.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
            boolean bIsDetected = Utilities.isDetectedActivity(mWorkoutMeta.activityID);
            if (!bIsDetected) {
                mWorkoutMeta.activityName = referencesTools.getFitnessActivityTextById(mWorkoutMeta.activityID);
                mWorkoutMeta.identifier = referencesTools.getFitnessActivityIdentifierById(mWorkoutMeta.activityID);
                long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                if (startTime == 0)
                    startTime = System.currentTimeMillis();
                mWorkoutMeta.start = startTime;
                long endTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
                if (endTime == 0 || (endTime == startTime)) dp.getEndTime(TimeUnit.MILLISECONDS);
                if (endTime > 0 ) mWorkoutMeta.end = endTime;
                mWorkoutMeta.duration = mWorkoutMeta.end - mWorkoutMeta.start;
                mWorkoutMeta.packageName = dp.getOriginalDataSource().getAppPackageName();
            }
        } else if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
            float watts = dp.getValue(Field.FIELD_WATTS).asFloat();
            mWorkoutMeta.wattsTotal = watts;
        } else if (dp.getDataType().getName().equals(DataType.TYPE_MOVE_MINUTES.getName())) {
            Integer minutes = ( dp.getValue(Field.FIELD_DURATION) != null) ? dp.getValue(Field.FIELD_DURATION).asInt() : 0;
            mWorkoutMeta.move_mins = minutes;
        } else if (dp.getDataType().getName().equals(DataType.TYPE_CALORIES_EXPENDED.getName())) {
            float calories = dp.getValue(Field.FIELD_CALORIES).asFloat();
            mWorkoutMeta.total_calories = calories;
        } else if (dp.getDataType().getName().contains(DataType.TYPE_DISTANCE_DELTA.getName())) {
            float distance = dp.getValue(Field.FIELD_DISTANCE).asFloat();
            mWorkoutMeta.distance = distance;
        } else if (dp.getDataType().getName().contains(DataType.TYPE_HEART_POINTS.getName())) {
            float pts = dp.getValue(Field.FIELD_INTENSITY).asFloat();
            mWorkoutMeta.heart_pts = pts;
        } else if (dp.getDataType().getName().contains(DataType.TYPE_STEP_COUNT_DELTA.getName())) {
            int steps = dp.getValue(Field.FIELD_STEPS).asInt();
            mWorkoutMeta.stepCount = steps;
        }else if (dp.getDataType().getName().equals(DataType.AGGREGATE_HEART_RATE_SUMMARY.getName())) {
            float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
            float min = dp.getValue(Field.FIELD_MIN).asFloat();
            float max = dp.getValue(Field.FIELD_MAX).asFloat();
            mWorkoutMeta.avgBPM = avg;
            mWorkoutMeta.maxBPM = max;
            mWorkoutMeta.minBPM = min;
        } else if (dp.getDataType().getName().equals(DataType.AGGREGATE_SPEED_SUMMARY.getName())) {
            float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
            float min = dp.getValue(Field.FIELD_MIN).asFloat();
            float max = dp.getValue(Field.FIELD_MAX).asFloat();
            mWorkoutMeta.avgBPM = avg;
            mWorkoutMeta.maxBPM = max;
            mWorkoutMeta.minBPM = min;
        }

    }
    private void sendNotification(Bundle dataBundle) {
        int notificationID = 0;
        long iconID = R.drawable.ic_a_outlined;
        int activityIcon = R.drawable.ic_running_white;
        Context context = getApplicationContext();
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String sTitle;
        String sContent;
        String sTime = Utilities.getTimeString(mCalendar.getTimeInMillis());
        String sChannelID;
        Intent notificationIntent;
        PendingIntent notificationPendingIntent = null;
        NotificationCompat.Builder notifyBuilder;
        PackageManager pm = context.getPackageManager();
        boolean bWear = pm.hasSystemFeature(PackageManager.FEATURE_WATCH);
        String sSplashActivity = (bWear) ? "com.a_track_it.workout.activity.RoomActivity":"com.a_track_it.workout.activity.MainActivity";
        try {
            notificationIntent = new Intent(getApplicationContext(), Class.forName(sSplashActivity));
            notificationIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
            notificationIntent.setAction(INTENT_SUMMARY_DAILY);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            notificationIntent.putExtra(KEY_FIT_TYPE, 1);
            notificationID = NOTIFICATION_SUMMARY_ID;

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            notificationID = 0;
        }
        sChannelID = SUMMARY_CHANNEL_ID;
        sTitle = String.format(Locale.getDefault(), getString(R.string.note_summary_title), sTime);
        sContent = String.format(Locale.getDefault(), getString(R.string.note_summary_desc), dataBundle.getInt(Constants.MAP_MOVE_MINS), dataBundle.getFloat(Constants.MAP_DISTANCE),
                dataBundle.getFloat(Constants.MAP_BPM_AVG), dataBundle.getFloat(Constants.MAP_BPM_MAX), dataBundle.getFloat(Constants.MAP_BPM_MIN), dataBundle.getFloat(Constants.MAP_CALORIES), dataBundle.getInt(Constants.MAP_STEPS));
        // Build the notification with all of the parameters.
        try {
            if ((notificationID > 0) && (notificationPendingIntent != null))
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setContentTitle(sTitle)
                        .setContentText(sContent)
                        .setSmallIcon(R.drawable.ic_a_outlined)
                        .setAutoCancel(true)
                        .setContentIntent(notificationPendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);
            else
                notifyBuilder = new NotificationCompat
                        .Builder(getApplicationContext(), sChannelID)
                        .setContentTitle(sTitle)
                        .setContentText(sContent)
                        .setSmallIcon(R.drawable.ic_a_outlined)
                        .setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

            if (mNotifyManager != null)
                mNotifyManager.notify(notificationID, notifyBuilder.build());

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return;
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
        NotificationCompat.BubbleMetadata metaBubble = new NotificationCompat.BubbleMetadata.Builder(pendingIntent,bubbleIcon).setAutoExpandBubble(true).build();
        // Build the notification with all of the parameters.
        NotificationCompat.Builder notifyBuilder = new NotificationCompat
                .Builder(context, Constants.MAINTAIN_CHANNEL_ID)
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
        NotificationManager mNotifyManager =(NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        if (mNotifyManager != null)
            mNotifyManager.notify(notificationID, notifyBuilder.build());
    }
    private void handleTaskError(Task task){
        Exception ex = task.getException();
        if (ex instanceof ApiException) {
            ApiException apie = ((ApiException) ex);
            PendingIntent pi = null;
            if (apie.getStatus().hasResolution()) pi = apie.getStatus().getResolution();
            if (pi == null) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(Constants.CLIENT_ID)
                        .requestEmail().requestId()
                        .build();
                // Build a GoogleSignInClient with the options specified by gso.
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
                Intent signInIntent = googleSignInClient.getSignInIntent();
                pi = PendingIntent.getActivity(getApplicationContext(),Constants.REQUEST_SIGNIN_SYNC,signInIntent,PendingIntent.FLAG_ONE_SHOT);
            }
            String sTitle = "Click to continue Google refresh";
            String sContent = "Unable to continue - click to resolve";
            buildPendingIntentNotification(sTitle, sContent, pi);
        }
    }

    /**
     *   DataPointToBundle
     *   build bundles from data points
     **/
    private Bundle DataPointToBundle(DataPoint dp){
        Bundle resultBundle = new Bundle();
        String sDataType = dp.getDataType().getName();
        referencesTools.init(getApplicationContext());
        if (sDataType != null)
            if (sDataType.equals(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())) {
                Workout workout = new Workout();
                workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
                workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
                workout.identifier = referencesTools.getFitnessActivityIdentifierById(workout.activityID);
                workout.setCount = dp.getValue(Field.FIELD_NUM_SEGMENTS).asInt();
                workout.duration = (long)dp.getValue(Field.FIELD_DURATION).asInt();
                long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                if (startTime == 0)
                    startTime = System.currentTimeMillis();
                workout.start = startTime;
                long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
                if (endTime > 0 ) workout.end = endTime;
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
                resultBundle.putParcelable(sDataType, workout);
            } else if (sDataType.equals(DataType.TYPE_POWER_SAMPLE.getName())) {
                Workout workout = new Workout();
                workout.wattsTotal = dp.getValue(Field.FIELD_WATTS).asFloat();
                Log.d(LOG_TAG, "power.sample " + workout.activityName + " " + workout.setCount);
                resultBundle.putParcelable(sDataType, workout);
            } else if (sDataType.equals(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
                WorkoutSet set = new WorkoutSet();
                try {
                    long timeStamp = dp.getTimestamp(TimeUnit.MILLISECONDS);
                    if (dp.getValue(Field.FIELD_DURATION).isSet())
                        set.duration = (long) dp.getValue(Field.FIELD_DURATION).asInt();
                    if (timeStamp > 0){
                        set.start = timeStamp;
                        if (set.duration > 0)
                            set.end = timeStamp + set.duration;
                    }
                }catch (Exception e){
                    Log.e(LOG_TAG, "set load error " + e.getMessage());
                }
                if ((dp.getStartTime(TimeUnit.MILLISECONDS) > 0) && (set.start == 0)) {
                    set.start = dp.getStartTime(TimeUnit.MILLISECONDS);
                }
                if ((dp.getEndTime(TimeUnit.MILLISECONDS) > 0) && (set.end == 0)){
                    set.end = dp.getEndTime(TimeUnit.MILLISECONDS);
                }
                if (set.start > 0) set._id = (set.start); else set._id = (System.currentTimeMillis());
                String sExercise = dp.getValue(Field.FIELD_EXERCISE).asString();
                set.exerciseName = sExercise;
                set.per_end_xy = sExercise;
                set.resistance_type = (long)dp.getValue(Field.FIELD_RESISTANCE_TYPE).asInt();
                set.workoutID = null;
                set.weightTotal = dp.getValue(Field.FIELD_RESISTANCE).asFloat();
                set.repCount = dp.getValue(Field.FIELD_REPETITIONS).asInt();
                set.setCount = 1;
                if ((set.weightTotal > 0) && (set.repCount > 0))
                    set.wattsTotal = (set.weightTotal * set.repCount);
                set.activityName = referencesTools.getFitnessActivityTextById(Constants.WORKOUT_TYPE_STRENGTH);
                set.activityID = Constants.WORKOUT_TYPE_STRENGTH;
                set.userID = sUserID;
                set.last_sync = set._id;
                if ((dp.getOriginalDataSource() != null) && (dp.getOriginalDataSource().getAppPackageName() != null)
                        && (dp.getOriginalDataSource().getAppPackageName().length() > 0))
                    set.score_card = dp.getOriginalDataSource().getAppPackageName();
                else
                    set.score_card = Constants.ATRACKIT_ATRACKIT_CLASS;
                Log.d(LOG_TAG, "dp " + referencesTools.workoutSetShortText(set));
                resultBundle.putParcelable(sDataType, set);
            } else if (sDataType.equals(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
                Workout workout = new Workout();
                workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
                workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
                workout.identifier = referencesTools.getFitnessActivityIdentifierById(workout.activityID);
                long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                if (startTime == 0)
                    startTime = System.currentTimeMillis();
                workout.start = startTime;
                workout._id = (workout.start) + workout.activityID;
                long endTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
                if (endTime == 0 || (endTime == startTime)) dp.getEndTime(TimeUnit.MILLISECONDS);
                if (endTime > 0 ) workout.end = endTime;
                workout._id = workout.start;
                workout.userID = sUserID;
                workout.duration = workout.end - workout.start;
                if ((dp.getOriginalDataSource() != null) && (dp.getOriginalDataSource().getAppPackageName() != null)
                        && (dp.getOriginalDataSource().getAppPackageName().length() > 0))
                    workout.packageName = dp.getOriginalDataSource().getAppPackageName();
                else {
                    if (Utilities.isDetectedActivity(workout.activityID))
                        workout.packageName = Constants.ATRACKIT_PLAY_CLASS;
                    else
                        workout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
                }
                resultBundle.putParcelable(sDataType, workout);
            } else if (sDataType.equals(DataType.TYPE_MOVE_MINUTES.getName())) {
                Integer minutes = ( dp.getValue(Field.FIELD_DURATION) != null) ? dp.getValue(Field.FIELD_DURATION).asInt() : 0;
                resultBundle.putInt(sDataType, minutes);
            } else if (sDataType.equals(DataType.TYPE_CALORIES_EXPENDED.getName())) {
                float calories = dp.getValue(Field.FIELD_CALORIES).asFloat();
                resultBundle.putFloat(sDataType, calories);
            } else if (sDataType.contains(DataType.TYPE_DISTANCE_DELTA.getName())) {
                float distance = dp.getValue(Field.FIELD_DISTANCE).asFloat();
                resultBundle.putFloat(sDataType, distance);
            } else if (sDataType.contains(DataType.AGGREGATE_HEART_POINTS.getName())) {
                float intensity = dp.getValue(Field.FIELD_INTENSITY).asFloat();
                int duration = dp.getValue(Field.FIELD_DURATION).asInt();
                Workout w = new Workout();
                w.wattsTotal = intensity;
                w.duration = duration;
                resultBundle.putParcelable(sDataType, w);
            }else if (sDataType.equals(DataType.AGGREGATE_HEART_RATE_SUMMARY.getName())) {
                float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
                float min = dp.getValue(Field.FIELD_MIN).asFloat();
                float max = dp.getValue(Field.FIELD_MAX).asFloat();
                resultBundle.putFloat(Constants.MAP_BPM_AVG, avg);
                resultBundle.putFloat(Constants.MAP_BPM_MIN, min);
                resultBundle.putFloat(Constants.MAP_BPM_MAX, max);
            } else if (sDataType.equals(DataType.TYPE_STEP_COUNT_DELTA.getName())) {
                Integer steps = ( dp.getValue(Field.FIELD_STEPS) != null) ? dp.getValue(Field.FIELD_STEPS).asInt() : 0;
                if (steps > 0){
                    resultBundle.putInt(MAP_STEPS, steps);
                    resultBundle.putString(Constants.MAP_DATA_TYPE, sDataType);
                }
            } else if (sDataType.equals(DataType.AGGREGATE_SPEED_SUMMARY.getName())) {
                float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
                float min = dp.getValue(Field.FIELD_MIN).asFloat();
                float max = dp.getValue(Field.FIELD_MAX).asFloat();
                resultBundle.putFloat(Constants.MAP_SPEED_AVG, avg);
                resultBundle.putFloat(Constants.MAP_SPEED_MIN, min);
                resultBundle.putFloat(Constants.MAP_SPEED_MAX, max);
            }else if (sDataType.equals(DataType.AGGREGATE_STEP_COUNT_DELTA.getName())) {
                Workout workout = new Workout();
                workout.activityID = WORKOUT_TYPE_STEPCOUNT;
                long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                if (startTime == 0)
                    startTime = System.currentTimeMillis();
                workout.start = startTime;
                long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
                if (endTime > 0 ) workout.end = endTime;
                workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
                workout.stepCount = dp.getValue(Field.FIELD_STEPS).asInt();
                workout.userID = sUserID;
                workout._id = workout.start;
                resultBundle.putParcelable(sDataType, workout);
                resultBundle.putInt(Constants.MAP_STEPS, workout.stepCount);
            }
        return resultBundle;
    }

    private Workout isExistingOrOverLappingGoogleWorkout(Workout newTest){
        Workout bfound = null;
        if (fitWorkoutList.size() > 0)
            for (Workout w : fitWorkoutList){
                if ((newTest._id == w._id) || (newTest.overlaps(w) && (newTest.activityID == w.activityID))){
                    bfound = w;
                    break;
                }
            }
        return bfound;
    }
    private Workout isWorkoutExistingOrOverLappingGoogleSet(WorkoutSet newTest){
        Workout bFound = null;
        if (fitWorkoutList.size() > 0)
            for (Workout w : fitWorkoutList){
                boolean overlapResult = false;
                if ((w.start == newTest.start) || (w.start >= newTest.start) && (w.start <= newTest.end)) {
                    overlapResult = true;
                }
                if ((w.end >= newTest.start) && (w.end <= newTest.end)) {
                    overlapResult = true;
                }
                if ((newTest.workoutID == w._id) || (overlapResult && (newTest.activityID == w.activityID))){
                    bFound = w;
                    break;
                }
            }
        return bFound;
    }
    private WorkoutSet isExistingOrOverLappingGoogleSet(WorkoutSet newTest){
        WorkoutSet bfound = null;
        for (WorkoutSet s : fitWorkoutSetList){
            if ((newTest._id == s._id) || (newTest.overlaps(s) && (newTest.activityID == s.activityID))){
                bfound = s;
                break;
            }
        }
        return bfound;
    }
    private boolean isExistingOrOverLapping(Workout newTest){
        boolean bfound = false;
        for (Workout w : existingWorkoutArrayList){
            if ((newTest._id == w._id) || (newTest.overlaps(w) && (newTest.activityID == w.activityID))){
                bfound = true;
                break;
            }
        }
        return bfound;
    }
    private boolean isExistingOrOverLappingSet(WorkoutSet newTest){
        boolean bfound = false;
        for (WorkoutSet s : existingWorkoutSetArrayList){
            if ((newTest._id == s._id) || (newTest.overlaps(s) && (newTest.activityID == s.activityID))){
                bfound = true;
                break;
            }
        }
        return bfound;
    }
    private boolean isExistingOrOverLappingMeta(WorkoutMeta newTest){
        boolean bfound = false;
        for (WorkoutMeta m : existingWorkoutMetaArrayList){
            if ((newTest._id == m._id) || (newTest.overlaps(m) && (m.activityID == newTest.activityID))){
                bfound = true;
                break;
            }
        }
        return bfound;
    }
    private void sendCapabilityMessage(final String sCapabilityName, final DataMap dataMap) {
        // Initial check of capabilities to find the phone.
        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(getApplicationContext())
                .getCapability(sCapabilityName, CapabilityClient.FILTER_REACHABLE);

        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
            @Override
            public void onComplete(Task<CapabilityInfo> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Capability request succeeded.");

                    CapabilityInfo capabilityInfo = task.getResult();
                    String phoneNodeId = pickBestNodeId(capabilityInfo.getNodes());

                    if (phoneNodeId != null) {
                        // Instantiates clients without member variables, as clients are inexpensive to
                        // create. (They are cached and shared between GoogleApi instances.)

                        Task<Integer> sendMessageTask =
                                Wearable.getMessageClient(
                                        getApplicationContext()).sendMessage(
                                        phoneNodeId,
                                        Constants.MESSAGE_PATH_PHONE,
                                        dataMap.toByteArray());

                        sendMessageTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                            @Override
                            public void onComplete(Task<Integer> task) {
                                if (task.isSuccessful()) {
                                    Log.d(LOG_TAG, "Message sent successfully");
                                    //  mMessagesViewModel.setPhoneAvailable(true);
                                } else {
                                    Log.d(LOG_TAG, "Message failed.");
                                    //   mMessagesViewModel.setPhoneAvailable(false);
                                }
                            }
                        });
                    } else {
                        Log.d(LOG_TAG, "No phone node available.");
                        //   mMessagesViewModel.setPhoneAvailable(false);
                    }
                } else {
                    Log.d(LOG_TAG, "Capability request failed to return any results.");
                    // mMessagesViewModel.setPhoneAvailable(false);
                }
            }
        });
    }

    private DataSet createStepCountDeltaDataSet(long startTime, int stepCountDelta, Device device){
        // Create a_track_it.com data source
        DataSource stepDataSource = new DataSource.Builder()
                .setAppPackageName(Constants.ATRACKIT_ATRACKIT_CLASS)
                .setDevice(device)
                .setStreamName(Constants.ATRACKIT_ATRACKIT_CLASS.concat(".steps"))
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_RAW)
                .build();
        try {
            DataSet.Builder stepDataSetBuilder = DataSet.builder(stepDataSource);
            DataPoint.Builder stepPointBuilder = DataPoint.builder(stepDataSource);
            stepPointBuilder.setField(Field.FIELD_STEPS,stepCountDelta);
            Log.w(LOG_TAG, "createStepCountDelta " + stepCountDelta + " " + Utilities.getTimeString(startTime));
            stepPointBuilder.setTimestamp(startTime, TimeUnit.MILLISECONDS);
            stepDataSetBuilder.add(stepPointBuilder.build());
            return stepDataSetBuilder.build();
        } catch (Exception e) {
            String sMsg = e.getMessage();
            if (sMsg != null) Log.e("createStepCountDeltaDataSet", sMsg);
            return null;
        }
    }
    private DataSet createBPMDataSet(long startTime, float bpm, Device device){
        // Create a_track_it.com data source
        DataSource bpmDataSource = new DataSource.Builder()
                .setAppPackageName(Constants.ATRACKIT_ATRACKIT_CLASS)
                .setDevice(device)
                .setDataType(DataType.TYPE_HEART_RATE_BPM)
                .setStreamName(Constants.ATRACKIT_ATRACKIT_CLASS.concat(".bpm"))
                .setType(DataSource.TYPE_RAW)
                .build();
        try {
            DataSet.Builder bpmDataSetBuilder = DataSet.builder(bpmDataSource);
            DataPoint.Builder bpmPointBuilder = DataPoint.builder(bpmDataSource);
            bpmPointBuilder.setField(Field.FIELD_BPM, bpm);
            bpmPointBuilder.setTimestamp(startTime, TimeUnit.MILLISECONDS);
            bpmDataSetBuilder.add(bpmPointBuilder.build());
            Log.w(LOG_TAG, "createBPM " + bpm + " " + Utilities.getTimeString(startTime));
            return bpmDataSetBuilder.build();
        } catch (Exception e) {
            String sMsg = e.getMessage();
            if (sMsg != null) Log.e("createBPMDataSet", sMsg);
            return null;
        }
    }
    private DataSet createActivityDataSet(DataSource activityDataSource, long startTime, long endTime, String activityName) {
        try {
            DataSet.Builder activityDataSetBuilder = DataSet.builder(activityDataSource);
            DataPoint.Builder activityBuilder = DataPoint.builder(activityDataSource);
            activityBuilder.setActivityField(Field.FIELD_ACTIVITY, activityName);
            if (startTime < endTime)
                activityBuilder.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
            else
                activityBuilder.setTimestamp(startTime, TimeUnit.MILLISECONDS);

            activityDataSetBuilder.add(activityBuilder.build());
            return activityDataSetBuilder.build();
        } catch (Exception e) {
            String sMsg = e.getMessage();
            if (sMsg != null) Log.e("createActivityDataSet", sMsg);
            return null;
        }
    }

    private DataReadRequest queryStepEstimate(long startTime, long endTime) {
        return new DataReadRequest.Builder()
                .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .enableServerQueries() // Used to retrieve data from cloud
                .build();
    }

    /**
     * Return a {@link DataReadRequest} for all step count changes in the past week.
     */
    private DataReadRequest.Builder queryDataWithBuckets(long startTime, long endTime,
                                                         List<DataType> types, List<DataType> aggregations,
                                                         int duration, TimeUnit time, int bucketType) {
        DataReadRequest.Builder builder = new DataReadRequest.Builder();
        for (int i = 0; i < types.size(); i++) {
            builder.aggregate(types.get(i));
        }
        switch (bucketType) {
            case BUCKET_TYPE_SESSION:
                builder.bucketBySession(duration, time);
                break;
            case BUCKET_TYPE_SEGMENT:
                builder.bucketByActivitySegment(duration, time);
                break;
            case BUCKET_TYPE_ACTIVITY:
                builder.bucketByActivityType(duration, time);
                break;
            case BUCKET_TYPE_TIME:
            default:
                builder.bucketByTime(duration, time);
                break;
        }
        return builder.setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS);
    }
    /**
     *
     * Subscribes to an available {@link DataType}. Subscriptions can exist across application
     * instances (so data is recorded even after the application closes down).  When creating
     * a new subscription, it may already exist from a previous invocation of this app.  If
     * the subscription already exists, the method is a no-op.  However, you can check this with
     * a special success code.
     */
    private void subscribeRecordingApiByDataType(final Context context, final DataType dataType) {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        // [START subscribe_to_datatype]
        final String sLabel = context.getString(R.string.label_subs) + dataType.getName();
        Boolean defaultValue = appPrefs.getPrefByLabel(sLabel);
        try {
            if (defaultValue)  // only if turned on
                Fitness.getRecordingClient(context, gsa)
                        .subscribe(dataType)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.w(LOG_TAG, "Successfully subscribed! " + dataType.getName());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(LOG_TAG, "There was a problem subscribing." + dataType.getName());
                                appPrefs.setPrefByLabel(sLabel,false);  // turn it off

                            }
                        });
        }catch (Exception e){
            Log.e(LOG_TAG, "Recording client subscribe error" + e.getMessage());
            throw e;
            // isRecordingAPIRunning = false;
        }
        // [END subscribe_to_datatype]
    }
    private void unsubscribeRecordingApiByDataType(final Context context, final DataType dataType) {
        try{
            String sLabel = context.getString(R.string.label_subs) + dataType.getName();

            Boolean defaultValue = appPrefs.getPrefByLabel(sLabel);
            if (defaultValue)
                Fitness.getRecordingClient(context, gsa).unsubscribe(dataType).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(LOG_TAG, "Successfully un-subscribed for data type: " + dataType.getName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "Un-subscribe failure " + dataType.getName() + " " + e.getMessage());
                    }
                });
        }catch (Exception e){
            throw e;
        }
        // [END unsubscribe_to_datatype]

    }    
    private void doFullSync(){
        long timeMs = System.currentTimeMillis();
        Context context = getApplicationContext();
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_24Full, Locale.getDefault());
        mCalendar.setTimeInMillis(timeMs);
        referencesTools.init(context);
        final int doyNOW = mCalendar.get(Calendar.DAY_OF_YEAR);
        List<Configuration> configList = workoutRepository.getConfigLikeName(Constants.MAP_CURRENT_STATE,sUserID);
        Configuration configHistory = null;
        int currentState = Constants.WORKOUT_INVALID;
        if (configList != null && (configList.size() > 0)){
            currentState = Math.toIntExact(configList.get(0).longValue);
            if (currentState == Constants.WORKOUT_LIVE || currentState == Constants.WORKOUT_PAUSED
                    || currentState == Constants.WORKOUT_CALL_TO_LINE) {
                Log.w(LOG_TAG, "leaving while currently busy " + currentState);
                action_successful = true;
                iErrorCode = 200;
                return;
            }
        }
        // get another device 2 if available
        configList = workoutRepository.getConfigLikeName(Constants.KEY_DEVICE2, sUserID);
        sDeviceID2 = null;
        if (configList != null && (configList.size() > 0)){
            sDeviceID2 = configList.get(0).stringValue2;
        }

        // get the date range
        long starting = 0;
        configList = workoutRepository.getConfigLikeName(Constants.MAP_HISTORY_RANGE,sUserID);
        if ((configList != null) && (configList.size() > 0)) {
            configHistory = configList.get(0);
            starting = Long.parseLong(configHistory.stringValue1);
            lHistoryStart = starting;
            lHistoryEnd = Long.parseLong(configHistory.stringValue2);
        }else{
            mCalendar.add(Calendar.DAY_OF_YEAR,-90);
            starting = mCalendar.getTimeInMillis();
        }
        if (starting > 0)
            mCalendar.setTimeInMillis(starting);
        final int doySTART = mCalendar.get(Calendar.DAY_OF_YEAR);
        if (session_action == TASK_ACTION_SYNC_WORKOUT) {
            // find unsync
            pendingWorkoutList = workoutRepository.getUnSyncWorkouts(sUserID, sDeviceID, starting, timeMs);
            pendingWorkoutSetList = workoutRepository.getUnSyncWorkoutSets(sUserID, sDeviceID);
            // ensure it's only ATRACKIT unsync'd - not other sources or DEVICES
            if (pendingWorkoutList != null)
                synchronized (pendingWorkoutList) {
                    Iterator sessionIterator = pendingWorkoutList.iterator();
                    while (sessionIterator.hasNext()) {
                        Workout tester = (Workout) sessionIterator.next();
                        if (((tester.deviceID == null) || !tester.deviceID.equals(sDeviceID)) || Utilities.isDetectedActivity(tester.activityID)
                                || (tester.packageName == null) || !tester.packageName.equals(Constants.ATRACKIT_ATRACKIT_CLASS)
                                || (tester.last_sync > 0))
                            sessionIterator.remove();
                    }
                }
            if (pendingWorkoutSetList != null)
                synchronized (pendingWorkoutSetList) {
                    Iterator setIterator = pendingWorkoutSetList.iterator();
                    while (setIterator.hasNext()) {
                        WorkoutSet tester = (WorkoutSet) setIterator.next();
                        if ((tester.deviceID == null) || (!tester.deviceID.equals(sDeviceID)) || Utilities.isDetectedActivity(tester.activityID)
                                || (tester.score_card == null) || !tester.score_card.equals(Constants.ATRACKIT_ATRACKIT_CLASS)
                                || (tester.last_sync > 0))
                            setIterator.remove();
                    }
                }
            // now Load TO Google
            if (((pendingWorkoutList != null) && (pendingWorkoutList.size() > 0))
                    || ((pendingWorkoutSetList != null) && (pendingWorkoutSetList.size() > 0))) {
                if ((pendingWorkoutList != null) && (pendingWorkoutSetList != null))
                    Log.w(LOG_TAG, "pending Workouts " + pendingWorkoutList.size() + " sets " + pendingWorkoutSetList.size());
                if (gsa == null) {
                    action_successful = false;
                    iErrorCode = 401;
                    return;
                }
                int gymCount = 0;
                int sdtCount = 0;
                int activityCount = 0;
                int stepCount = 0;
                Device device = Device.getLocalDevice(context);
                DataSource activityDataSource = new DataSource.Builder()
                        .setAppPackageName(Constants.ATRACKIT_ATRACKIT_CLASS)
                        .setDevice(device)
                        .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                        .setType(DataSource.TYPE_RAW)
                        .build();
                DataSet.Builder activityDataSetBuilder = DataSet.builder(activityDataSource);
                DataSource exerciseDataSource = new DataSource.Builder()
                        .setAppPackageName(Constants.ATRACKIT_ATRACKIT_CLASS)
                        .setDevice(device)
                        .setDataType(DataType.TYPE_WORKOUT_EXERCISE)
                        .setType(DataSource.TYPE_RAW)
                        .build();
                DataSet.Builder exerciseDataSetBuilder = DataSet.builder(exerciseDataSource);
                DataSource stepDataSource = new DataSource.Builder()
                        .setAppPackageName(Constants.ATRACKIT_ATRACKIT_CLASS)
                        .setDevice(device)
                        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .setType(DataSource.TYPE_RAW)
                        .build();
                DataSet.Builder stepDataSetBuilder = DataSet.builder(stepDataSource);
                DataSource bpmDataSource = new DataSource.Builder()
                        .setAppPackageName(Constants.ATRACKIT_ATRACKIT_CLASS)
                        .setDevice(device)
                        .setDataType(DataType.TYPE_HEART_RATE_BPM)
                        .setType(DataSource.TYPE_RAW)
                        .build();
                DataSet.Builder bpmDataSetBuilder = DataSet.builder(bpmDataSource);
                // workouts first so sync'd non gym workouts can set last_sync on sets
                List<SensorDailyTotals> sdtList = new ArrayList<>();
                if (pendingWorkoutList != null) {
                    for (Workout pendingWorkout : pendingWorkoutList) {             // read existing loaded data...
                        bRefreshing = true;
                        try {
                            if (fitWorkoutList.size() > 0) fitWorkoutList.clear();
                            if (fitWorkoutSetList.size() > 0) fitWorkoutSetList.clear();
                            if (sdtList != null && sdtList.size() > 0) sdtList.clear();
                        } catch (UnsupportedOperationException ex) {
                            while (fitWorkoutList.size() > 0) {
                                fitWorkoutList.remove(fitWorkoutList.size() - 1);
                            }
                            while (fitWorkoutSetList.size() > 0) {
                                fitWorkoutSetList.remove(fitWorkoutSetList.size() - 1);
                            }
                            if (sdtList != null)
                                while (sdtList.size() > 0) {
                                    sdtList.remove(sdtList.size() - 1);
                                }
                        }
                        if (pendingWorkout.start > 0 && pendingWorkout.end > pendingWorkout.start && pendingWorkout.activityID > 0) {
                            if (pendingWorkout.offline_recording == 1) {
                                sdtList = workoutRepository.getSensorDailyTotals(sUserID, pendingWorkout.start, pendingWorkout.end).getValue();
                            }else
                                sdtList = null;

                            DataReadRequest readSegmentRequest;
                            if (Utilities.isGymWorkout(pendingWorkout.activityID))
                                readSegmentRequest = new DataReadRequest.Builder()
                                        .read(DataType.TYPE_WORKOUT_EXERCISE)
                                        .read(DataType.TYPE_STEP_COUNT_DELTA)
                                        .read(DataType.TYPE_ACTIVITY_SEGMENT)
                                        .setTimeRange(pendingWorkout.start, pendingWorkout.end, TimeUnit.MILLISECONDS)
                                        .setLimit(2000)
                                        .enableServerQueries().build();// Used to retrieve data from cloud.
                            else
                                readSegmentRequest = new DataReadRequest.Builder()
                                        .read(DataType.TYPE_ACTIVITY_SEGMENT)
                                        .read(DataType.TYPE_STEP_COUNT_DELTA)
                                        .setTimeRange(pendingWorkout.start, pendingWorkout.end, TimeUnit.MILLISECONDS)
                                        .setLimit(2000)
                                        .enableServerQueries().build();// Used to retrieve data from cloud.
                            Task<DataReadResponse> response = Fitness.getHistoryClient(context, gsa).readData(readSegmentRequest);
                            response.addOnCompleteListener(historyReadCompleter);
                            try {
                                Tasks.await(response, 5, TimeUnit.MINUTES);
                                waitStart = SystemClock.elapsedRealtime();
                                while (bRefreshing) {
                                    Log.w(LOG_TAG, "sleeping on historyReadCompleter refresh for pending list");
                                    SystemClock.sleep(5000);
                                    if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                        bRefreshing = false;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                bRefreshing = false;
                            }
                            Workout existingWorkout = isExistingOrOverLappingGoogleWorkout(pendingWorkout);
                            if (existingWorkout == null) {
                                // create the workout now
                                DataPoint.Builder activityBuilder = DataPoint.builder(activityDataSource);
                                if (pendingWorkout.identifier.length() > 0)
                                    activityBuilder.setActivityField(Field.FIELD_ACTIVITY, pendingWorkout.identifier);
                                else
                                    activityBuilder.setActivityField(Field.FIELD_ACTIVITY, pendingWorkout.activityName);
                                if (pendingWorkout.start < pendingWorkout.end)
                                    activityBuilder.setTimeInterval(pendingWorkout.start, pendingWorkout.end, TimeUnit.MILLISECONDS);
                                else
                                    activityBuilder.setTimestamp(pendingWorkout.start, TimeUnit.MILLISECONDS);

                                activityCount += 1;
                                activityDataSetBuilder.add(activityBuilder.build());
                                if (pendingWorkout.stepCount > 0) {
                                    DataPoint.Builder stepBuilder = DataPoint.builder(stepDataSource);
                                    if (pendingWorkout.start < pendingWorkout.end)
                                        stepBuilder.setTimeInterval(pendingWorkout.start, pendingWorkout.end, TimeUnit.MILLISECONDS);
                                    else
                                        stepBuilder.setTimestamp(pendingWorkout.start, TimeUnit.MILLISECONDS);
                                    stepBuilder.setField(Field.FIELD_STEPS, pendingWorkout.stepCount);
                                    stepDataSetBuilder.add(stepBuilder.build());
                                }
                                if (pendingWorkout.offline_recording == 1 && (sdtList != null) && sdtList.size() > 0) {
                                    for (SensorDailyTotals sdt : sdtList) {
                                        if (sdt.deviceBPM > 0) {
                                            DataPoint.Builder bpmBuilder = DataPoint.builder(bpmDataSource);
                                            if (startTime < endTime)
                                                bpmBuilder.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
                                            else
                                                bpmBuilder.setTimestamp(startTime, TimeUnit.MILLISECONDS);
                                            bpmBuilder.setField(Field.FIELD_BPM, sdt.deviceBPM);
                                            bpmDataSetBuilder.add(bpmBuilder.build());
                                            sdtCount++;
                                        }
                                    }
                                }
                                // we have sets to load
                                if (Utilities.isGymWorkout(pendingWorkout.activityID)) {
                                    if ((pendingWorkoutSetList != null) && pendingWorkoutSetList.size() > 0)
                                        for (WorkoutSet workoutSet : pendingWorkoutSetList) {
                                            if ((workoutSet.workoutID == pendingWorkout._id) && (workoutSet.last_sync == 0)) { // match workoutID but NOT sync'd
                                                if ((workoutSet.repCount != null) && (workoutSet.resistance_type != null) && (workoutSet.per_end_xy != null)
                                                        && (workoutSet.exerciseID != null) && (workoutSet.weightTotal != null)) {
                                                    DataPoint.Builder builder = DataPoint.builder(exerciseDataSource);
                                                    builder.setTimestamp(workoutSet.start, TimeUnit.MILLISECONDS);
                                                    builder.setField(FIELD_DURATION, (int) (workoutSet.end - workoutSet.start));
                                                    if ((workoutSet.per_end_xy.length() > 0) && (workoutSet.exerciseID <= 90))
                                                        builder.setField(Field.FIELD_EXERCISE, workoutSet.per_end_xy);
                                                    else
                                                        builder.setField(Field.FIELD_EXERCISE, workoutSet.exerciseName);
                                                    builder.setField(FIELD_REPETITIONS, workoutSet.repCount);
                                                    builder.setField(FIELD_RESISTANCE_TYPE, Math.toIntExact(workoutSet.resistance_type));
                                                    builder.setField(FIELD_RESISTANCE, workoutSet.weightTotal);
                                                    exerciseDataSetBuilder.add(builder.build());
                                                    gymCount += 1;
                                                }
                                            }
                                        } // each unsync'd pending workoutSet
                                } // if gym
                            }
                            waitStart = SystemClock.elapsedRealtime();
                            while (bRefreshing) {
                                Log.w(LOG_TAG, "sleeping on refresh");
                                SystemClock.sleep(5000);
                                if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(3))
                                    bRefreshing = false;
                            }
                        } // valid workout check
                        else pendingWorkoutList.remove(pendingWorkout);
                    }
                }
                if (pendingWorkoutSetList != null) {
                    for (WorkoutSet pendingWorkoutSet : pendingWorkoutSetList) {             // read existing loaded data...
                        if (pendingWorkoutSet.start > 0 && pendingWorkoutSet.end > pendingWorkoutSet.start
                                && pendingWorkoutSet.activityID > 0 && pendingWorkoutSet.last_sync == 0) {
                            bRefreshing = true;
                            if (fitWorkoutSetList.size() > 0) fitWorkoutSetList.clear();
                            DataReadRequest readSegmentRequest;
                            if (Utilities.isGymWorkout(pendingWorkoutSet.activityID))
                                readSegmentRequest = new DataReadRequest.Builder()
                                        .read(DataType.TYPE_WORKOUT_EXERCISE)
                                        .read(DataType.TYPE_STEP_COUNT_DELTA)
                                        .read(DataType.TYPE_ACTIVITY_SEGMENT)
                                        .setTimeRange(pendingWorkoutSet.start, pendingWorkoutSet.end, TimeUnit.MILLISECONDS)
                                        .setLimit(2000)
                                        .enableServerQueries().build();// Used to retrieve data from cloud.
                            else
                                readSegmentRequest = new DataReadRequest.Builder()
                                        .read(DataType.TYPE_ACTIVITY_SEGMENT)
                                        .read(DataType.TYPE_STEP_COUNT_DELTA)
                                        .setTimeRange(pendingWorkoutSet.start, pendingWorkoutSet.end, TimeUnit.MILLISECONDS)
                                        .setLimit(2000)
                                        .enableServerQueries().build();// Used to retrieve data from cloud.
                            Task<DataReadResponse> response = Fitness.getHistoryClient(context, gsa).readData(readSegmentRequest);
                            response.addOnCompleteListener(historyReadCompleter);
                            Task task = null;
                            try {
                                task = Tasks.whenAllComplete(response);
                                if (task.isSuccessful()) {
                                    bRefreshing = false;
                                } else {
                                    handleTaskError(task);
                                }
                                waitStart = SystemClock.elapsedRealtime();
                                while (bRefreshing) {
                                    Log.w(LOG_TAG, "sleeping on get existing refresh");
                                    SystemClock.sleep(5000);
                                    if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                        bRefreshing = false;
                                }
                            } catch (Exception e) {
                                if (task != null) handleTaskError(task);
                                e.printStackTrace();
                                bRefreshing = false;
                            }
                            // look through existing loaded and only continue if not duplicating!
                            WorkoutSet existingWorkoutSet = isExistingOrOverLappingGoogleSet(pendingWorkoutSet);
                            if (existingWorkoutSet == null) {
                                if (Utilities.isGymWorkout(pendingWorkoutSet.activityID)) {
                                    DataPoint.Builder builder = DataPoint.builder(exerciseDataSource);
                                    builder.setTimestamp(pendingWorkoutSet.start, TimeUnit.MILLISECONDS);
                                    builder.setField(FIELD_DURATION, (int) (pendingWorkoutSet.end - pendingWorkoutSet.start));
                                    if ((pendingWorkoutSet.per_end_xy != null && pendingWorkoutSet.per_end_xy.length() > 0) && (pendingWorkoutSet.exerciseID != null && pendingWorkoutSet.exerciseID <= 90))
                                        builder.setField(Field.FIELD_EXERCISE, pendingWorkoutSet.per_end_xy);
                                    else
                                        builder.setField(Field.FIELD_EXERCISE, pendingWorkoutSet.exerciseName);
                                    if ((pendingWorkoutSet.repCount != null) && (pendingWorkoutSet.resistance_type != null) && (pendingWorkoutSet.weightTotal != null)) {
                                        builder.setField(FIELD_REPETITIONS, pendingWorkoutSet.repCount);
                                        builder.setField(FIELD_RESISTANCE_TYPE, Math.toIntExact(pendingWorkoutSet.resistance_type));
                                        builder.setField(FIELD_RESISTANCE, pendingWorkoutSet.weightTotal);
                                        exerciseDataSetBuilder.add(builder.build());
                                    }
                                } else {
                                    Workout existingWorkout = isWorkoutExistingOrOverLappingGoogleSet(pendingWorkoutSet);
                                    if (existingWorkout != null) {
                                        if (existingWorkout.last_sync > 0) {
                                            pendingWorkoutSet.last_sync = existingWorkout.last_sync;
                                            pendingWorkoutSet.lastUpdated = timeMs;
                                            workoutRepository.updateWorkoutSet(pendingWorkoutSet);
                                        }
                                    } else {
                                        Workout finder = workoutRepository.getWorkoutByIdNow(pendingWorkoutSet.workoutID, pendingWorkoutSet.userID, pendingWorkoutSet.deviceID);
                                        if (finder != null) {
                                            if (finder.last_sync > 0) {
                                                pendingWorkoutSet.last_sync = finder.last_sync;
                                                pendingWorkoutSet.lastUpdated = timeMs;
                                                workoutRepository.updateWorkoutSet(pendingWorkoutSet);
                                            }
                                        }
                                    }

                                }
                            }
                        } // valid sets only
                        else  pendingWorkoutSetList.remove(pendingWorkoutSet);
                    } // for each pending set
                }
                if ((gymCount > 0) || (stepCount > 0) || (activityCount > 0) || (sdtCount > 0)) {
                    if (activityCount > 0) {
                        bRefreshing = true;
                        Fitness.getHistoryClient(context, gsa).insertData(activityDataSetBuilder.build()).addOnCompleteListener(task -> {
                            iWorkouts += 1;
                            bRefreshing = false;
                        });
                        waitStart = SystemClock.elapsedRealtime();
                        while (bRefreshing) {
                            Log.w(LOG_TAG, "sleeping on insert activity DS refresh");
                            SystemClock.sleep(5000);
                            if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                bRefreshing = false;
                        }
                        if (pendingWorkoutList != null)
                            for (Workout w : pendingWorkoutList) {
                                if (w.last_sync == 0) {
                                    w.last_sync = timeMs;
                                    w.lastUpdated = timeMs;
                                    workoutRepository.updateWorkout(w);
                                }
                                if (!Utilities.isGymWorkout(w.activityID)) {
                                    if ((pendingWorkoutSetList != null) && pendingWorkoutSetList.size() > 0)
                                        for (WorkoutSet set : pendingWorkoutSetList) {
                                            if (set.workoutID == w._id) {
                                                set.last_sync = timeMs;
                                                set.lastUpdated = timeMs;
                                                workoutRepository.updateWorkoutSet(set);
                                                pendingWorkoutSetList.remove(set);
                                                break;
                                            }
                                        }
                                }
                            }
                    }
                    if (stepCount > 0) {
                        bRefreshing = true;
                        Fitness.getHistoryClient(context, gsa).insertData(stepDataSetBuilder.build()).addOnCompleteListener(task -> {
                            iSteps += 1;
                            bRefreshing = false;
                        });
                        waitStart = SystemClock.elapsedRealtime();
                        while (bRefreshing) {
                            Log.w(LOG_TAG, "sleeping on insertSteps refresh");
                            SystemClock.sleep(5000);
                            if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                bRefreshing = false;
                        }

                    }
                    if (sdtCount > 0) {
                        bRefreshing = true;
                        Fitness.getHistoryClient(context, gsa).insertData(bpmDataSetBuilder.build()).addOnCompleteListener(task -> {
                            bRefreshing = false;
                        });
                        waitStart = SystemClock.elapsedRealtime();
                        while (bRefreshing) {
                            Log.w(LOG_TAG, "sleeping on insertBPM refresh");
                            SystemClock.sleep(5000);
                            if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                bRefreshing = false;
                        }
                    }
                    if (gymCount > 0) {
                        bRefreshing = true;
                        Fitness.getHistoryClient(context, gsa).insertData(exerciseDataSetBuilder.build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                iWorkoutSets += 1;
                                bRefreshing = false;
                            }
                        });
                        waitStart = SystemClock.elapsedRealtime();
                        while (bRefreshing) {
                            Log.w(LOG_TAG, "sleeping on insert exercise DS refresh");
                            SystemClock.sleep(5000);
                            if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                bRefreshing = false;
                        }
                        if (pendingWorkoutSetList != null)
                            for (WorkoutSet s : pendingWorkoutSetList) {
                                if (s.last_sync == 0) {
                                    s.last_sync = timeMs;
                                    s.lastUpdated = timeMs;
                                    workoutRepository.updateWorkoutSet(s);
                                }
                            }
                    }
                    Log.e(LOG_TAG, "finishing UPLOAD" + " w " + iWorkouts + " s " + iWorkoutSets + " steps " + iSteps);
                    if (iWorkouts > 0 && (pendingWorkoutList != null)) {
                        for (Workout pendingW : pendingWorkoutList) {
                            Log.e(LOG_TAG, "UPLOADED " + pendingW.toString());
                        }
                    }
                } else // end of if gymCount stepCount activityCount
                    Log.w(LOG_TAG, "finishing UPLOAD NO DATA");
            }

        } // [end of session_action == TASK_ACTION_SYNC_WORKOUT]

        // now Load FROM Google - get the dates from Config HistoryRange
        if ((configList != null) && (configList.size() > 0)){
            startTime = Long.parseLong(configHistory.stringValue2); // last end is now our start
            if (startTime == 0){
                if (Long.parseLong(configHistory.stringValue1) == 0) {
                    mCalendar.setTimeInMillis(timeMs);
                    mCalendar.add(Calendar.DAY_OF_YEAR, -90);
                    startTime = mCalendar.getTimeInMillis();
                }else{
                    startTime = Long.parseLong(configHistory.stringValue1);
                }
            }
        }
        else
            startTime = starting;
        startTime = Utilities.getDayStart(mCalendar,startTime); // start of day always
        endTime = timeMs;
        Log.w(LOG_TAG, "Load Starting " + dateFormat.format(startTime) + " to " + dateFormat.format(endTime));
        // now the DOWNLOAD
        iWorkouts = 0; iWorkoutSets = 0;
        List<Workout> wL = workoutRepository.getWorkoutsNow(sUserID, Constants.ATRACKIT_EMPTY, startTime, endTime); // ALL NEEDED!
        if ((wL != null) && (wL.size() > 0))
            existingWorkoutArrayList.addAll(wL);
        List<WorkoutSet> sL = workoutRepository.getWorkoutSetsNow(sUserID, Constants.ATRACKIT_EMPTY, startTime, endTime);  // ALL NEEDED! - no deviceID
        if ((sL != null) && (sL.size() > 0))
            existingWorkoutSetArrayList.addAll(sL);

        List<UserDailyTotals> uL = workoutRepository.getUserDailyTotals(sUserID, startTime,endTime,1); // daily
        if ((uL != null) && (uL.size() > 0))
            existingUserDailyTotalList.addAll(uL);

        if (gsa == null){
            action_successful = false;
            iErrorCode = 401;
            return;
        }
        DataReadRequest readRequest;
        try {
            // read segments
            bRefreshing = true;
            if (doyNOW == doySTART)
                readRequest = new DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_ACTIVITY_SEGMENT)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .bucketBySession(3, TimeUnit.MINUTES)
                    .setLimit(1000)
                    .enableServerQueries().build();
            else
                readRequest = new DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_DISTANCE_DELTA)
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                    .aggregate(DataType.TYPE_ACTIVITY_SEGMENT)
                    .aggregate(DataType.TYPE_CALORIES_EXPENDED)
                    .aggregate(DataType.TYPE_POWER_SAMPLE)
                    .aggregate(DataType.TYPE_MOVE_MINUTES)
                    .aggregate(DataType.TYPE_HEART_POINTS)
                    .aggregate(DataType.TYPE_SPEED)
                    .aggregate(DataType.TYPE_HEART_RATE_BPM)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .bucketByTime(1,TimeUnit.DAYS)
                    .setLimit(2000)
                    .enableServerQueries().build();// Used to retrieve data from cloud.
            Fitness.getHistoryClient(context, gsa).readData(readRequest)
            .addOnFailureListener(e -> {
                bRefreshing = false;
                Log.e(LOG_TAG,"segment read failed " + e.getMessage());
            })
            .addOnSuccessListener(readUDTSuccess)
            .addOnCompleteListener(readCompleter);
            waitStart = SystemClock.elapsedRealtime();
            while (bRefreshing) {
                if (doyNOW == doySTART)
                    Log.w(LOG_TAG, "sleeping on TYPE_ACTIVITY_SEGMENT refresh");
                else
                    Log.w(LOG_TAG, "sleeping on UDT SUMMARY refresh");

                SystemClock.sleep(5000);
                if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                    bRefreshing = false;
            }
            Log.w(LOG_TAG, "completed wait TYPE_ACTIVITY_SEGMENT refresh");
        }
        catch (Exception e) {
            e.printStackTrace();
            bRefreshing = false;
        }
        // now LOAD FROM Google lists
        synchronized ((udtList)) {
            if (udtList.size() > 0) {
                for (UserDailyTotals udt : existingUserDailyTotalList) {
                    ListIterator<UserDailyTotals> iterator = udtList.listIterator();
                    while (iterator.hasNext()) {
                        UserDailyTotals newUDT = iterator.next();
                        if (newUDT._id == udt._id) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                for (UserDailyTotals u : udtList) {
                    int doyUDT = Utilities.getDOY(mCalendar, u._id);
                    for (Workout w : summaryWorkoutList) {
                        int doyWorkout = Utilities.getDOY(mCalendar, w.start);
                        if (doyUDT == doyWorkout) {
                            if (w.activityID == Constants.WORKOUT_TYPE_INVEHICLE)
                                u.durationVehicle = w.duration;
                            if (w.activityID == Constants.WORKOUT_TYPE_BIKING)
                                u.durationBiking = w.duration;
                            if (w.activityID == Constants.WORKOUT_TYPE_STILL)
                                u.durationStill = w.duration;
                            if (w.activityID == Constants.WORKOUT_TYPE_UNKNOWN)
                                u.durationUnknown = w.duration;
                            if (w.activityID == Constants.WORKOUT_TYPE_WALKING)
                                u.durationWalking = w.duration;
                            if (w.activityID == Constants.WORKOUT_TYPE_RUNNING)
                                u.durationRunning = w.duration;
                            if (w.activityID == 2) u.durationOnFoot = w.duration;
                            if (w.activityID == 5) u.durationTilting = w.duration;
                            break;
                        }
                    }
                    // ensure unique UDT
                    List<UserDailyTotals> existingList = workoutRepository.getUserDailyTotals(sUserID,u._id,u._id,1);
                    if (existingList == null || existingList.size() == 0) {
                        Log.e(LOG_TAG, "udt new " + u.toString());
                        workoutRepository.insertUserDailyTotal(u);
                    }

                }

                // work through the summary list getting segments for
                if (summaryWorkoutList != null && (summaryWorkoutList.size() > 0)) {
                    for (Workout w : summaryWorkoutList) {
                        if (!Utilities.isDetectedActivity(w.activityID)) {
                            long starts = Utilities.getDayStart(mCalendar, w.start);
                            long ends = Utilities.getDayEnd(mCalendar, w.start);
                            mWorkout = w;
                            DataReadRequest request = new DataReadRequest.Builder()
                                    .read(DataType.TYPE_ACTIVITY_SEGMENT)
                                    .setTimeRange(starts, ends, TimeUnit.MILLISECONDS)
                                    .setLimit(2000)
                                    .enableServerQueries().build();// Used to retrieve data from cloud.*/
                            Task<DataReadResponse> segReadResponse = Fitness.getHistoryClient(context, gsa).readData(request);
                            segReadResponse.addOnSuccessListener(readSuccess).addOnCompleteListener(readCompleter);
                            try {
                                Tasks.await(segReadResponse,3,TimeUnit.MINUTES);
                                waitStart = SystemClock.elapsedRealtime();
                                while (bRefreshing) {
                                    Log.w(LOG_TAG, "sleeping on TYPE_ACTIVITY_SEGMENT reads");
                                    SystemClock.sleep(5000);
                                    if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                        bRefreshing = false;
                                }
                                Log.w(LOG_TAG, "completed wait TYPE_ACTIVITY_SEGMENT refresh");
                            } catch (Exception e) {
                                e.printStackTrace();
                                bRefreshing = false;
                            }
                            if (Utilities.isGymWorkout(w.activityID)) {
                                long startSet = (w.start > 0) ? w.start : startTime;
                                long endSet = (w.end > 0 && (w.end > w.start)) ? w.end : endTime;
                                DataReadRequest readExerciseRequest = new DataReadRequest.Builder()
                                        .setTimeRange(startSet, endSet, TimeUnit.MILLISECONDS)
                                        .enableServerQueries().setLimit(1000)
                                        .read(DataType.TYPE_WORKOUT_EXERCISE).build();
                                bRefreshing = true;
                                mWorkout = w;
                                mWorkout.deviceID = sDeviceID2;
                                try {
                                    Log.e(LOG_TAG, "about read exercise " + ATRACKIT_SPACE + Utilities.getTimeString(startSet) + ATRACKIT_SPACE + Utilities.getTimeString(endSet));
                                    Task<DataReadResponse> exerciseTask = Fitness.getHistoryClient(context, gsa).readData(readExerciseRequest);
                                    exerciseTask.addOnSuccessListener(exerciseResponse);
                                    Tasks.await(exerciseTask, 2, TimeUnit.MINUTES);
                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "read exercise " + ATRACKIT_SPACE + e.getMessage());
                                    bRefreshing = false;
                                }
                                waitStart = SystemClock.elapsedRealtime();
                                while (bRefreshing) {
                                    Log.w(LOG_TAG, "sleeping on refresh read exercises " + referencesTools.workoutShortText(w));
                                    SystemClock.sleep(5000);
                                    if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                        bRefreshing = false;
                                }
                                Log.e(LOG_TAG, "finished reading exercise " + w.packageName);
                            }
                            int set_duration = Math.toIntExact(w.end - w.start);
                            if ((w.activityID >= DetectedActivity.WALKING)
                                    && (w.end > 0 && w.start > 0) && (TimeUnit.MILLISECONDS.toMinutes(w.end - w.start) > 5)) {
                                List<WorkoutMeta> metaList = workoutRepository.getWorkoutMetasByWorkoutID(w._id,w.userID,w.deviceID);
                                if (metaList != null && metaList.size() > 0)
                                    mWorkoutMeta = metaList.get(0);
                                else {
                                    mWorkoutMeta = new WorkoutMeta();
                                    mWorkoutMeta._id = w._id;
                                    mWorkoutMeta.userID = w.userID;
                                    mWorkoutMeta.deviceID = w.deviceID;
                                    mWorkoutMeta.workoutID = w._id;
                                }
                                mWorkoutMeta.workoutID = w._id;
                                mWorkoutMeta.activityID = w.activityID;
                                mWorkoutMeta.activityName = w.activityName;
                                mWorkoutMeta.start = w.start;
                                mWorkoutMeta.end = w.end;
                                mWorkoutMeta.duration = w.duration;
                                mWorkoutMeta.packageName = w.packageName;
                                if (!isExistingOrOverLappingMeta(mWorkoutMeta)) {
                                    DataReadRequest.Builder builder = new DataReadRequest.Builder();
                                    builder.aggregate(DataType.TYPE_DISTANCE_DELTA)
                                            .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                                            .aggregate(DataType.TYPE_SPEED)
                                            .aggregate(DataType.TYPE_CALORIES_EXPENDED)
                                            .aggregate(DataType.TYPE_HEART_RATE_BPM)
                                            .aggregate(DataType.TYPE_POWER_SAMPLE)
                                            .aggregate(DataType.TYPE_MOVE_MINUTES)
                                            .aggregate(DataType.TYPE_HEART_POINTS)
                                            .setTimeRange(w.start, w.end, TimeUnit.MILLISECONDS)
                                            .bucketByTime(set_duration, TimeUnit.MILLISECONDS)
                                            .enableServerQueries();// Used to retrieve data from cloud.
                                    DataReadRequest metaRequest = builder.build();
                                    try {
                                        bRefreshing = true;
                                        Task<DataReadResponse> responseTask = Fitness.getHistoryClient(context, gsa)
                                                .readData(metaRequest).addOnSuccessListener(metaResponse).addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) workoutMetaArrayList.add(mWorkoutMeta);
                                                    bRefreshing=false;
                                                });
                                        Tasks.await(responseTask, 3, TimeUnit.MINUTES);
                                        Log.w(LOG_TAG, "read meta success " + Utilities.getDateString(mWorkoutMeta.start) + ATRACKIT_SPACE + mWorkoutMeta.activityName);
                                    }
                                    catch (Exception e) {
                                        Log.e(LOG_TAG, "read meta " + Utilities.getDateString(mWorkoutMeta.start) + ATRACKIT_SPACE + e.getMessage());
                                    }
                                }
                            }
                        }
                        long iActivityID = w.activityID;
                        boolean existing = isExistingOrOverLapping(w);
                        try {
                            Log.w(LOG_TAG, "detected activityMap " + iActivityID + " existing " + existing);
                            if (!existing) {
                                Workout tester = workoutRepository.getWorkoutByIdNow(w._id, sUserID, Constants.ATRACKIT_EMPTY);
                                if (tester != null) {
                                    workoutRepository.updateWorkout(w);
                                    Log.w(LOG_TAG, "FOUND tester detected activityMap " + iActivityID + " existing " + tester.toString());
                                } else
                                    workoutRepository.insertWorkout(w);
                            } else
                                workoutRepository.updateWorkout(w);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                    if (requestList.size() > 0) {
                        try {
                            Log.w(LOG_TAG, "RequestList size " + requestList.size());
                            Tasks.whenAllComplete(requestList).addOnCompleteListener(task -> {
                                bRefreshing = false;
                                action_successful = task.isSuccessful();
                            });
                            waitStart = SystemClock.elapsedRealtime();
                            while (bRefreshing) {
                                Log.w(LOG_TAG, "sleeping on TYPE_ACTIVITY_SEGMENT reads");
                                SystemClock.sleep(5000);
                                if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                    bRefreshing = false;
                            }
                            Log.w(LOG_TAG, "completed wait TYPE_ACTIVITY_SEGMENT refresh");
                        } catch (Exception e) {
                            e.printStackTrace();
                            bRefreshing = false;
                        }
                    }
                }
            }
        }
        try {
            synchronized ((loadingWorkoutList)) {
                Log.e(LOG_TAG, "about to process " + loadingWorkoutList.size());
                ListIterator<Workout> workoutIterator = loadingWorkoutList.listIterator(); // find exercises for gym
                while (workoutIterator.hasNext()) {  // remove overlapping and find gym exercises
                    Workout w = workoutIterator.next();
                    if (isExistingOrOverLapping(w)) {
                        Log.e(LOG_TAG, "Existing workout " + referencesTools.workoutShortText(w));
                        workoutIterator.remove();
                    } else {
                        w.deviceID = sDeviceID2; // not from us !
                        mWorkout = w;
                        existingWorkoutArrayList.add(mWorkout);
                        if ((w._id > 0) && (w.userID.length() > 0)) {
                            Workout finder = workoutRepository.getWorkoutByIdNow(w._id, w.userID, Constants.ATRACKIT_EMPTY);
                            if (finder == null) {
                                try {
                                    workoutRepository.insertWorkout(w);
                                    Log.w(LOG_TAG, "adding new WORKOUT  " + referencesTools.workoutShortText(w));
                                    iWorkouts += 1;
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                if (w.duration > TimeUnit.MINUTES.toMillis(3)) {
                                    Data.Builder builder = new Data.Builder();
                                    builder.putString(KEY_FIT_USER, w.userID);
                                    builder.putString(KEY_FIT_DEVICE_ID, w.deviceID);
                                    builder.putLong(KEY_FIT_WORKOUTID, w._id);
                                    OneTimeWorkRequest workRequest =
                                            new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                                                    .setInputData(builder.build())
                                                    .build();
                                    workList.add(workRequest);
                                }
                            } else {
                                List<ObjectAggregate> existingWorkoutAgg =  workoutRepository.getAllObjectAggregatesByTypeObjectID(sUserID,Constants.OBJECT_TYPE_WORKOUT,w._id);
                                if (existingWorkoutAgg == null){
                                    Data.Builder builder = new Data.Builder();
                                    builder.putString(KEY_FIT_USER, w.userID);
                                    builder.putString(KEY_FIT_DEVICE_ID, w.deviceID);
                                    builder.putLong(KEY_FIT_WORKOUTID, w._id);
                                    OneTimeWorkRequest workRequest =
                                            new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                                                    .setInputData(builder.build())
                                                    .build();
                                    workList.add(workRequest);
                                }
                                Log.e(LOG_TAG, "found existing anyway " + finder.toString());
                            }
                        }
                    }
                }  // for each workout
            }
            synchronized (loadingWorkoutSetList) {
                if (loadingWorkoutSetList.size() > 0)
                    Log.e(LOG_TAG, "about to process sets " + loadingWorkoutSetList.size());
                    for (WorkoutSet s : loadingWorkoutSetList) {
                        boolean bValid = s.isValid(true);
                        if ((s._id > 0) && (s.userID.length() > 0)) {
                            try {
                                WorkoutSet finderSet = workoutRepository.getWorkoutSetByIdNow(s._id, s.userID, Constants.ATRACKIT_EMPTY);
                                if (finderSet == null) {
                                    workoutRepository.insertWorkoutSet(s);
                                    Log.w(LOG_TAG, "adding new SET  " + bValid + " " + referencesTools.workoutSetShortText(s));
                                    iWorkoutSets += 1;
                                } else
                                    Log.w(LOG_TAG, "found existing SET anyway " + bValid + " " + referencesTools.workoutSetShortText(finderSet));
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
            }
            synchronized (workoutMetaArrayList) {
                for (WorkoutMeta m : workoutMetaArrayList) {
                    List<WorkoutMeta> metaList = workoutRepository.getWorkoutMetaById(m._id,m.userID,m.deviceID);
                    Workout workoutMeta = workoutRepository.getWorkoutByIdNow(m.workoutID,m.userID,null);
                    try {
                        if (workoutMeta != null) {
                            if (metaList.size() == 0)
                                workoutRepository.insertWorkoutMeta(m);
                            else
                                workoutRepository.updateWorkoutMeta(m);
                            iWorkoutMeta++;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            if (workList.size() > 0) {
                if (mWorkManager == null) mWorkManager = WorkManager.getInstance(context);
                mWorkManager.enqueue(workList);
            }
            if (exercisesMissingList.size() > 0) {
                sExerciseList = Constants.ATRACKIT_EMPTY;
                for (Exercise ex : exercisesMissingList) {
                    List<Exercise> exList = workoutRepository.getExercisesByName(ex.name);
                    if ((exList == null) || (exList.size() == 0))
                        workoutRepository.insertExercise(ex);
                    if (sExerciseList.length() == 0)
                        sExerciseList = ex.name;
                    else
                        sExerciseList += "," + ex.name;
                }
                Log.w(LOG_TAG, sExerciseList);
                if (sExerciseList.length() > 1020) {
                    sExerciseList = sExerciseList.substring(0, 1020);
                    Log.e(LOG_TAG, "Exercise list was trimmed");
                }
                resultBundle.putString(Constants.KEY_LIST_SETS, sExerciseList);
            }
            if ((configHistory != null) && (lHistoryStart > 0)) {
                boolean bUpdate = false;
                if (startTime < lHistoryStart) {
                    configHistory.stringValue1 = Long.toString(startTime);
                    bUpdate = true;
                }
                if (endTime > lHistoryEnd) {
                    configHistory.stringValue2 = Long.toString(endTime);
                    bUpdate = true;
                }
                if (bUpdate) {
                    configHistory.longValue = timeMs;
                    workoutRepository.updateConfig(configHistory);
                }
                Log.e(LOG_TAG, "updating History Config " + configHistory.toString());
            } else {
                Configuration conf = new Configuration();
                conf.stringName = MAP_HISTORY_RANGE;
                conf.userValue = sUserID;
                conf.stringValue = sDeviceID;
                conf.longValue = timeMs;
                conf.stringValue1 = Long.toString(startTime);
                conf.stringValue2 = Long.toString(endTime);
                workoutRepository.updateConfig(conf);
                Log.e(LOG_TAG, "Inserting History Config " + conf.toString());
            }
            if (iWorkouts == 1 && loadingWorkoutList.size() > 0)
                Log.e(LOG_TAG, "w " + loadingWorkoutList.get(0).toString());
            action_successful = true;
            Log.w(LOG_TAG, action_successful + " finishing" + " total " + " w " + iWorkouts + " s " + iWorkoutSets + " m " + iWorkoutMeta);
        }catch (Exception e){
            action_successful = false;
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
    // Helper for showing tests
    void showToast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FitSyncJobIntentService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        /* Find a nearby node or pick one arbitrarily. There should be only one phone connected
         * that supports this sample.
         */
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    private long getDayStart(long inTime) {
        mCalendar.setTimeInMillis(inTime);
        mCalendar.set(Calendar.HOUR, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);
        return mCalendar.getTimeInMillis();
    }

    private long getDayEnd(long inTime) {
        mCalendar.setTimeInMillis(inTime);
        mCalendar.set(Calendar.HOUR_OF_DAY, mCalendar.getMaximum(Calendar.HOUR_OF_DAY));
        mCalendar.set(Calendar.MINUTE, mCalendar.getMaximum(Calendar.MINUTE));
        mCalendar.set(Calendar.SECOND, mCalendar.getMaximum(Calendar.SECOND));
        mCalendar.set(Calendar.MILLISECOND, mCalendar.getMaximum(Calendar.MILLISECOND));
        return mCalendar.getTimeInMillis();
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.d(LOG_TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.d(LOG_TAG, "Data point:");
            Log.d(LOG_TAG, "\tType: " + dp.getDataType().getName());
            Log.d(LOG_TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.d(LOG_TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.d(LOG_TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }
}


