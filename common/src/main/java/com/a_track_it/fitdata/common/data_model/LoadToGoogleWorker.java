package com.a_track_it.fitdata.common.data_model;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.fitdata.common.Constants.ATRACKIT_SPACE;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.fitdata.common.Constants.MAP_STEPS;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_TYPE_STEPCOUNT;
import static com.google.android.gms.fitness.data.Field.FIELD_DURATION;
import static com.google.android.gms.fitness.data.Field.FIELD_REPETITIONS;
import static com.google.android.gms.fitness.data.Field.FIELD_RESISTANCE;
import static com.google.android.gms.fitness.data.Field.FIELD_RESISTANCE_TYPE;

public class LoadToGoogleWorker extends Worker {
    private static final String LOG_TAG = LoadToGoogleWorker.class.getSimpleName();
    public static final String ARG_ACTION_KEY = "action-key";
    public static final String ARG_ID_KEY = "id-key";
    public static final String ARG_START_KEY = "start-key";
    public static final String ARG_END_KEY = "end-key";

    private Calendar mCalendar;
    private String sUserID;
    private String sDeviceID;
    private long startTime = 0L;  // default to previous day
    private long endTime = 0L;  // default to previous day
    private long longID = 0L;
    private String sAction = Constants.ATRACKIT_EMPTY;
    private WorkoutDao mWorkoutDao;
    private WorkoutSetDao mWorkoutSetDao;
    private WorkoutMetaDao mWorkoutMetaDao;
    private ExerciseDao mExerciseDao;
    private BodypartDao mBodypartDao;
    private UserDailyTotalsDao mUserDailyTotalsDao;
    private ConfigurationDao mConfigDao;
    private ReferencesTools referencesTools;
    private int iUDTs = 0;
    private int iWorkouts = 0;
    private int iWorkoutSets = 0;
    private int iSteps = 0;
    private boolean isBusy = false;
    private boolean bRefreshing = false;
    private long waitStart = 0;
    private List<WorkoutSet> existingWorkoutSetList = new ArrayList<>();
    private List<Workout> pendingWorkoutList = new ArrayList<>();
    private List<WorkoutSet> pendingWorkoutSetList = new ArrayList<>();
    private List<WorkoutMeta> existingWorkoutMetaArrayList = new ArrayList<>();
    private List<Workout> workoutList = new ArrayList<>();
    private List<WorkoutSet> workoutSetList = new ArrayList<>();
    private final OnCompleteListener<DataReadResponse> historyReadCompleter = (OnCompleteListener<DataReadResponse>) task -> {
        try {
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
                                    s._id = getDayStart(s.start) + workoutSetList.size();
                                    s.last_sync = s._id;
                                    s.userID = sUserID;
                                    s.setCount = 1;
                                    s.deviceID = sDeviceID;
                                    Log.w(LOG_TAG, "adding set " + s.toString());
                                    workoutSetList.add(s);
                                }

                                if (result.containsKey(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
                                    w = result.getParcelable(DataType.TYPE_ACTIVITY_SEGMENT.getName());
                                    if (w._id == 0L) w._id = w.start;
                                    w._id = w._id + workoutList.size();
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
                                workoutList.add(w);
                                Log.w(LOG_TAG, "adding workout segment " + referencesTools.workoutShortText(w));
                            }
                        }
                    }
                }
                bRefreshing = false;
            }
        }catch(Exception e){
            Log.e(LOG_TAG, "readFitHistoryError " + e.getMessage());
        }
    };

    public LoadToGoogleWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        WorkoutRoomDatabase db = WorkoutRoomDatabase.getDatabase(context.getApplicationContext());
        mWorkoutDao = db.workoutDao();
        mWorkoutSetDao = db.workoutSetDao();
        mWorkoutMetaDao = db.workoutMetaDao();
        mExerciseDao = db.exerciseDao();
        mBodypartDao = db.bodypartDao();
        mUserDailyTotalsDao = db.dailyTotalsDao();
        mConfigDao = db.configDao();
        referencesTools = ReferencesTools.setInstance(context);
        mCalendar = Calendar.getInstance(Locale.getDefault());
    }

    /** doWork
     read un-synchronized
     **/
    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();
            ApplicationPreferences applicationPreferences = ApplicationPreferences.getPreferences(context);
            Data inputData = getInputData();
            boolean build = false;
            int gymCount = 0;
            int activityCount = 0;
            int stepCount = 0;
            endTime = inputData.getLong(ARG_END_KEY, 0);
            startTime = inputData.getLong(ARG_START_KEY, 0);  // default to previous day
            longID = inputData.getLong(ARG_ID_KEY, 0);
            sUserID = inputData.getString(Constants.KEY_FIT_USER);
            String sTemp = "start " + Utilities.getTimeDateString(startTime) + ATRACKIT_SPACE + " end " + Utilities.getTimeDateString(endTime);
            Log.e(LOG_TAG, sTemp);

            if (inputData.hasKeyWithValueOfType(KEY_FIT_DEVICE_ID, String.class))
                sDeviceID = inputData.getString(Constants.KEY_FIT_DEVICE_ID);
            else{
                List<Configuration> list = mConfigDao.getConfigByNameUser(Constants.KEY_DEVICE1, sUserID);
                if (list != null && (list.size() > 0)) sDeviceID = list.get(0).stringValue;
            }
            if ((startTime == 0) || (endTime == 0)) {
                if (longID > 0) {
                    pendingWorkoutList = mWorkoutDao.getWorkoutByIdUserDeviceId(longID, sUserID, sDeviceID);  // can check an existing dude!
                    pendingWorkoutSetList = mWorkoutSetDao.getWorkoutSetByWorkoutDeviceID(longID, sUserID, sDeviceID);  // can check an existing dude!
                } else {
                    pendingWorkoutList = mWorkoutDao.getWorkoutOfflineUnSync(sUserID, sDeviceID);
                    pendingWorkoutSetList = mWorkoutSetDao.getWorkoutSetByUnSync(sUserID, sDeviceID);
                }

            } else {
                pendingWorkoutList = mWorkoutDao.getWorkoutUnSyncdByStarts(sUserID,sDeviceID,startTime, endTime);
                pendingWorkoutSetList = mWorkoutSetDao.getWorkoutSetByUnSyncByDates(sUserID,sDeviceID,startTime, endTime);
            }

            // ensure it's only ATRACKIT unsync'd - not other sources or DEVICES
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
            if ((pendingWorkoutList.size() == 0) && (pendingWorkoutSetList.size() == 0))
                return Result.success();

            GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(context, referencesTools.getFitnessSignInOptions(1));
            if ((gsa.getId() == null) || (sUserID.length() == 0) || (sDeviceID.length() == 0)
                    || !referencesTools.isNetworkConnected()) return Result.failure();

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
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-dd HH:mm:ss z", Locale.getDefault());
            Log.w(LOG_TAG, "doWork " + sAction + "s " + simpleDateFormat.format(startTime) + " e " + simpleDateFormat.format(endTime));

            if ((pendingWorkoutSetList.size() > 0)) {
                for (WorkoutSet pendingWorkoutSet : pendingWorkoutSetList) {             // read existing loaded data...
                    isBusy = false;
                    if (workoutSetList.size() > 0) workoutSetList.clear();
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
                    Tasks.await(response, 5, TimeUnit.MINUTES);
                    long waitStart = SystemClock.elapsedRealtime();
                    while (bRefreshing) {
                        Log.w(LOG_TAG, "sleeping on get existing refresh");
                        SystemClock.sleep(5000);
                        if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                            bRefreshing = false;
                    }
                    WorkoutSet existingWorkoutSet = isExistingOrOverLappingSet(pendingWorkoutSet);
                    // we can add this guy!
                    if (existingWorkoutSet == null) {
                        if (Utilities.isGymWorkout(pendingWorkoutSet.activityID)) {
                            DataPoint.Builder builder = DataPoint.builder(exerciseDataSource);
                            builder.setTimestamp(pendingWorkoutSet.start, TimeUnit.MILLISECONDS);
                            builder.setField(FIELD_DURATION,(int)(pendingWorkoutSet.end-pendingWorkoutSet.start));
                            if ((pendingWorkoutSet.per_end_xy != null && pendingWorkoutSet.per_end_xy.length() > 0) && (pendingWorkoutSet.exerciseID != null && pendingWorkoutSet.exerciseID <= 90))
                                builder.setField(Field.FIELD_EXERCISE, pendingWorkoutSet.per_end_xy);
                            else
                                builder.setField(Field.FIELD_EXERCISE, pendingWorkoutSet.exerciseName);
                            if ((pendingWorkoutSet.repCount != null) && (pendingWorkoutSet.resistance_type != null) && (pendingWorkoutSet.weightTotal != null)) {
                                builder.setField(FIELD_REPETITIONS, pendingWorkoutSet.repCount);
                                builder.setField(FIELD_RESISTANCE_TYPE, Math.toIntExact(pendingWorkoutSet.resistance_type));
                                builder.setField(FIELD_RESISTANCE, pendingWorkoutSet.weightTotal);
                                exerciseDataSetBuilder.add(builder.build());
                                gymCount += 1;
                            }
                        }
                        else {
                            DataPoint.Builder activityBuilder = DataPoint.builder(activityDataSource);
                            if (pendingWorkoutSet.activityName.length() > 0) {
                                activityBuilder.setActivityField(Field.FIELD_ACTIVITY, pendingWorkoutSet.activityName);
                                if (pendingWorkoutSet.start < pendingWorkoutSet.end)
                                    activityBuilder.setTimeInterval(pendingWorkoutSet.start, pendingWorkoutSet.end, TimeUnit.MILLISECONDS);
                                else
                                    activityBuilder.setTimestamp(pendingWorkoutSet.start, TimeUnit.MILLISECONDS);
                                activityDataSetBuilder.add(activityBuilder.build());
                                activityCount += 1;
                            }
                        }
                    }
                }
            }
            if (pendingWorkoutList.size() > 0){
                for (Workout pendingWorkout : pendingWorkoutList) {             // read existing loaded data...
                    isBusy = false;
                    if (workoutList.size() > 0) workoutList.clear();
                    if (workoutSetList.size() > 0) workoutSetList.clear();
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
                    Tasks.await(response, 5, TimeUnit.MINUTES);
                    long waitStart = SystemClock.elapsedRealtime();
                    while (bRefreshing) {
                        Log.w(LOG_TAG, "sleeping on Summary refresh");
                        SystemClock.sleep(5000);
                        if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                            bRefreshing = false;
                    }
                    Workout existingWorkout = isExistingOrOverLappingWorkout(pendingWorkout);
                    if (existingWorkout != null) {
                        if ((existingWorkout.stepCount == 0) && (pendingWorkout.stepCount > 0)) {
                            DataSet updateSteps = Utilities.createStepCountDataSet(existingWorkout.start, existingWorkout.end, device, pendingWorkout.stepCount);
                            isBusy = true;
                            Fitness.getHistoryClient(context, gsa).insertData(updateSteps).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    isBusy = false;
                                }
                            });
                            waitStart = SystemClock.elapsedRealtime();
                            while (isBusy) {
                                Log.w(LOG_TAG, "sleeping on create Steps refresh");
                                SystemClock.sleep(5000);
                                if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                    isBusy = false;
                            }
                        }
                    }
                    else {
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

                        activityCount +=1;
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
                        // we have sets to load
                        if (Utilities.isGymWorkout(pendingWorkout.activityID)){
                            for (WorkoutSet workoutSet : pendingWorkoutSetList) {
                                WorkoutSet existingSet = isExistingOrOverLappingSet(workoutSet);
                                if (existingSet == null) {
                                    if ((workoutSet.workoutID == pendingWorkout._id) && (workoutSet.last_sync == 0)) {  // matching workout but NOT sync'd
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
                                } // not an existing set
                            } // if pending WorkoutSets for this workout
                        } // if gym

                    } // not overlapping workout
                    waitStart = SystemClock.elapsedRealtime();
                    while (isBusy) {
                        Log.w(LOG_TAG, "sleeping on refresh");
                        SystemClock.sleep(5000);
                        if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                            isBusy = false;
                    }
                }
            }
            if ((gymCount > 0) || (stepCount > 0) || (activityCount > 0)) {
                final long timeMs = System.currentTimeMillis();
                if (activityCount > 0) {
                    isBusy = true;
                    Fitness.getHistoryClient(context, gsa).insertData(activityDataSetBuilder.build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            iWorkouts += 1;
                            isBusy = false;
                        }
                    });
                    waitStart = SystemClock.elapsedRealtime();
                    while (isBusy) {
                        Log.w(LOG_TAG, "sleeping on insert activity DS refresh");
                        SystemClock.sleep(5000);
                        if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                            isBusy = false;
                    }
                    for (Workout w: pendingWorkoutList){
                        if (w.last_sync == 0) {
                            w.last_sync = timeMs;
                            mWorkoutDao.update(w);
                        }
                    }
                }
                if (stepCount > 0) {
                    isBusy = true;
                    Fitness.getHistoryClient(context, gsa).insertData(stepDataSetBuilder.build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            iSteps += 1;
                            isBusy = false;
                        }
                    });
                    waitStart = SystemClock.elapsedRealtime();
                    while (isBusy) {
                        Log.w(LOG_TAG, "sleeping on insertSteps refresh");
                        SystemClock.sleep(5000);
                        if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                            isBusy = false;
                    }

                }
                if (gymCount > 0) {
                    isBusy = true;
                    Fitness.getHistoryClient(context, gsa).insertData(exerciseDataSetBuilder.build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            iWorkoutSets += 1;
                            isBusy = false;
                        }
                    });
                    waitStart = SystemClock.elapsedRealtime();
                    while (isBusy) {
                        Log.w(LOG_TAG, "sleeping on insert exercise DS refresh");
                        SystemClock.sleep(5000);
                        if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                            isBusy = false;
                    }
                    for (WorkoutSet s: pendingWorkoutSetList){
                        if (s.last_sync == 0) {
                            s.last_sync = timeMs;
                            mWorkoutSetDao.update(s);
                        }
                    }
                }
            } // end of if gymCount stepCount activityCount
            Log.w(LOG_TAG, "finishing" + " total iUDT " + iUDTs + " w " + iWorkouts + " s " + iWorkoutSets + " steps "+iSteps);
         //   if (iWorkouts == 1) Log.w(LOG_TAG," " + pendingWorkoutList.get(0).toString());
         //   if (iWorkoutSets == 1) Log.w(LOG_TAG," " + pendingWorkoutSetList.get(0).toString());

            applicationPreferences.setBackgroundLoadComplete(true);
            Data outputData = new Data.Builder()
                    .putInt(Constants.KEY_RESULT, iUDTs + iWorkouts + iWorkoutSets + iSteps)
                    .putInt(UserDailyTotals.class.getSimpleName(), iUDTs)
                    .putInt(Workout.class.getSimpleName(), iWorkouts)
                    .putInt(WorkoutSet.class.getSimpleName(), iWorkoutSets)
                    .putInt(DataType.TYPE_STEP_COUNT_DELTA.getName(), iSteps)
                    .putString(ARG_ACTION_KEY, sAction)
                    .putLong(ARG_START_KEY, startTime)
                    .putLong(ARG_END_KEY, endTime)
                    .putString(Constants.KEY_FIT_USER, sUserID)
                    .putString(Constants.KEY_FIT_DEVICE_ID, sDeviceID)
                    .putString(Constants.KEY_FIT_VALUE, LOG_TAG)
                    .build();
            return Result.success(outputData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
            return Result.failure();
        }
    }

    /**
     *   DataPointToBundle
     *   build bundles from data points
     **/
    private Bundle DataPointToBundle(DataPoint dp){
        Bundle resultBundle = new Bundle();
        String sDataType = dp.getDataType().getName();
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
            else
                workout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
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
            else
                workout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
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



    private Workout isExistingOrOverLappingWorkout(Workout newTest){
        Workout bfound = null;
        if (workoutList.size() > 0)
            for (Workout w : workoutList){
                if ((newTest._id == w._id) || (newTest.overlaps(w) && (newTest.activityID == w.activityID))){
                    bfound = w;
                    break;
                }
            }
        return bfound;
    }
    private WorkoutSet isExistingOrOverLappingSet(WorkoutSet newTest){
        WorkoutSet bfound = null;
        for (WorkoutSet s : workoutSetList){
            if ((newTest._id == s._id) || (newTest.overlaps(s) && (newTest.activityID == s.activityID))){
                bfound = s;
                break;
            }
        }
        return bfound;
    }
/*
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
*/


    private long getDayStart(long inTime){
        mCalendar.setTimeInMillis(inTime);
        mCalendar.set(Calendar.HOUR, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);
        return  mCalendar.getTimeInMillis();
    }
    private long getDayEnd(long inTime){
        mCalendar.setTimeInMillis(inTime);
        mCalendar.set(Calendar.HOUR_OF_DAY, mCalendar.getMaximum(Calendar.HOUR_OF_DAY));
        mCalendar.set(Calendar.MINUTE, mCalendar.getMaximum(Calendar.MINUTE));
        mCalendar.set(Calendar.SECOND, mCalendar.getMaximum(Calendar.SECOND));
        mCalendar.set(Calendar.MILLISECOND, mCalendar.getMaximum(Calendar.MILLISECOND));
        return  mCalendar.getTimeInMillis();
    }
}
