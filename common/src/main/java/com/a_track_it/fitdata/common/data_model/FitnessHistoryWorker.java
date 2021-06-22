package com.a_track_it.fitdata.common.data_model;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.R;
import com.a_track_it.fitdata.common.Utilities;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

public class FitnessHistoryWorker extends Worker {
    private static final String LOG_TAG = FitnessHistoryWorker.class.getSimpleName();
    public static final String ARG_RESULT_KEY = "result-key";
    public static final String ARG_ACTION_KEY = "action-key";
    public static final String ARG_ID_KEY = "id-key";
    public static final String ARG_START_KEY = "start-key";
    public static final String ARG_END_KEY = "end-key";
    private Executor executor;
    private WorkoutDao mWorkoutDao;
    private WorkoutSetDao mWorkoutSetDao;
    private ExerciseDao mExerciseDao;
    private BodypartDao mBodypartDao;
    private static int[] mIDSortedArray;
    private static String[] mNamesIDSortedArray;
    private static String[] mIdentifiersIDSortedArray;
    private ArrayList<Workout> workoutArrayList = new ArrayList<>();

    public FitnessHistoryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Context context2 = context.getApplicationContext();
        WorkoutRoomDatabase db = WorkoutRoomDatabase.getDatabase(context2);
        mWorkoutDao = db.workoutDao();
        mWorkoutSetDao = db.workoutSetDao();
        mExerciseDao = db.exerciseDao();
        mBodypartDao = db.bodypartDao();
        executor = Executors.newSingleThreadExecutor();
        Resources resources = context2.getResources();

        mNamesIDSortedArray =resources.getStringArray(R.array.activity_name_id_sorted);
        mIDSortedArray = resources.getIntArray(R.array.activity_id_sorted);
        mIdentifiersIDSortedArray = resources.getStringArray(R.array.activity_ident_id_sorted);
    }



    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        //  Bundle resultBundle = new Bundle();
        long endTime  = getInputData().getLong(ARG_END_KEY,0);
        long startTime = getInputData().getLong(ARG_START_KEY,0);
        long longID = getInputData().getLong(ARG_ID_KEY,0);
        String sUserID = getInputData().getString(Constants.KEY_FIT_USER);
        String sDeviceID = getInputData().getString(Constants.KEY_FIT_DEVICE_ID);
            try {

                final long timeNowMs = System.currentTimeMillis();

                List<Workout> listWorkouts = mWorkoutDao.getWorkoutByDeviceStarts(sUserID,sDeviceID,startTime,endTime);
                List<WorkoutSet> listSets = mWorkoutSetDao.getWorkoutSetByDeviceStarts(sUserID,sDeviceID,startTime, endTime);

                // session details = SETS
                final OnCompleteListener<SessionReadResponse> sessionCompleter = task -> {
                    try {
                        if (task.isSuccessful()) {
                            SessionReadResponse sessionReadResponse = task.getResult();
                            List<Session> sessions = sessionReadResponse.getSessions();
                            Log.d(LOG_TAG, "Session read was successful. Number of returned sessions is: " + sessions.size());
                            for (Session session : sessions) {
                                dumpSession(session);
                                List<DataSet> dataSets = sessionReadResponse.getDataSet(session);
                                for (DataSet dataSet : dataSets) {
                                    dumpDataSet(dataSet);
                                    List<DataPoint> dpList = dataSet.getDataPoints();
                                    Log.d(LOG_TAG, "received session data-point " + dataSet.getDataType().getName() + " dps " + dpList.size());
                                    for (DataPoint dp : dpList) {
                                        Bundle resultBundle1 = datapointToResultBundle(dp);
                                        if (resultBundle1.containsKey(WorkoutSet.class.getSimpleName())) {
                                            WorkoutSet ws = resultBundle1.getParcelable(WorkoutSet.class.getSimpleName());
                                            boolean bFound = false;
                                            for (WorkoutSet test : listSets) {
                                                if ((ws._id == test._id)||(ws.start == test.start)) {
                                                    bFound = true;
                                                    break;
                                                }
                                            }
                                            if (!bFound) {
                                                ws.userID = sUserID;
                                                if (ws._id == 0L) ws._id = ws.start;
                                                // now find the corresponding workout & details
                                                for (Workout w: workoutArrayList){
                                                    if ((w.start <= ws.start) && (w.end >= ws.end)){
                                                        Log.e(LOG_TAG,"found workout for set " + w.toString());
                                                        ws.workoutID = w._id;
                                                        ws.activityName = w.activityName;
                                                        ws.activityID = w.activityID;
                                                        ws.duration = ws.end - ws.start;
                                                        ws.last_sync = timeNowMs;
                                                        break;
                                                    }
                                                }
                                                if ((ws.workoutID == null) || (ws.workoutID == 0L)) ws.workoutID = 1L;
                                                if (ws.isValid(true)) {
                                                    executor.execute(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mWorkoutSetDao.insert(ws);
                                                        }
                                                    });
                                                }
                                            } // not found so adding it!
                                        }
                                    }
                                }
                            } // each session
                        } // successful
                    }catch(Exception e){
                        Log.e(LOG_TAG, e.getMessage());
                    }
                };
                // SEGMENT RESULTS = WORKOUTS
                OnCompleteListener<DataReadResponse> completeListener = task -> {
                    try {
                        if (task.getResult().getStatus().isSuccess()) {
                            int iRet = task.getResult().getBuckets().size();
                            if (iRet > 0) {
                                for (Bucket bucket : task.getResult().getBuckets()) {
                                    List<DataSet> dataSets = bucket.getDataSets();
                                    for (DataSet dataSet : dataSets) {
                                        dumpDataSet(dataSet);
                                        List<DataPoint> dpList = dataSet.getDataPoints();
                                        for (DataPoint dp : dpList) {
                                            Log.d(LOG_TAG, "received workout data-point " + dp.getDataType().getName() + " dps " + dpList.size());
                                            Bundle resultBundle = datapointToResultBundle(dp);
                                            if (resultBundle.containsKey(Workout.class.getSimpleName())) {
                                                boolean bFound = false;
                                                Workout w = resultBundle.getParcelable(Workout.class.getSimpleName());
                                                if (!Utilities.isDetectedActivity(w.activityID) && (w.duration > Constants.DURATION_MIN_TIME_MILLIS))
                                                    for (Workout test : listWorkouts) {
                                                        if ((w._id == test._id) || (w.overlaps(test))) {
                                                            bFound = true;
                                                            break;
                                                        }
                                                    }
                                                if (!bFound) {
                                                    if (!Utilities.isDetectedActivity(w.activityID) && (w.duration > Constants.DURATION_MIN_TIME_MILLIS)) {
                                                        w.userID = sUserID;
                                                        if (w.duration == 0) w.duration = w.end - w.start;
                                                        w.last_sync = timeNowMs;
                                                        if (w._id == 0) w._id = w.start;
                                                        workoutArrayList.add(w);
                                                        executor.execute(() -> {
                                                            mWorkoutDao.insert(w);
                                                        });
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                for (DataSet dataSet : task.getResult().getDataSets()) {
                                    List<DataPoint> dpList = dataSet.getDataPoints();
                                    for (DataPoint dp : dpList) {
                                        Log.d(LOG_TAG, "received data-point " + dp.getDataType().getName() + " dps " + dpList.size());
                                        Bundle resultBundle = datapointToResultBundle(dp);
                                        if (resultBundle.containsKey(Workout.class.getSimpleName())) {
                                            Workout w = resultBundle.getParcelable(Workout.class.getSimpleName());
                                            boolean bFound = false;
                                            if (!Utilities.isDetectedActivity(w.activityID) && (w.duration > Constants.DURATION_MIN_TIME_MILLIS))
                                                for (Workout test : listWorkouts) {
                                                    if ((w._id == test._id) || (w.overlaps(test))){
                                                        bFound = true;
                                                        break;
                                                    }
                                                }
                                            if (!bFound) {
                                                if (!Utilities.isDetectedActivity(w.activityID) && (w.duration > Constants.DURATION_MIN_TIME_MILLIS)) {
                                                    w.userID = sUserID;
                                                    if (w.duration == 0) w.duration = w.end - w.start;
                                                    w.last_sync = timeNowMs;
                                                    if (w._id == 0) w._id = w.start;
                                                    workoutArrayList.add(w);
                                                    executor.execute(() -> {
                                                        Log.e(LOG_TAG,"insert " + w.toString());
                                                        mWorkoutDao.insert(w);
                                                    });

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(LOG_TAG, "task read not successful " + task.getResult().toString());

                        }
                    }catch (Exception e){
                        Log.e(LOG_TAG, e.getMessage());
                    }
                };

                final GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(context, getFitnessSignInOptions());
                if (gsa != null) {
                    DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                            .read(DataType.TYPE_ACTIVITY_SEGMENT)
                            .setLimit(2000)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .enableServerQueries() // Used to retrieve data from cloud.
                            .build();
                    Task<DataReadResponse> responseTask = Fitness.getHistoryClient(context, gsa)
                            .readData(dataReadRequest);
                    responseTask.addOnCompleteListener(completeListener);
                    responseTask.continueWith(task -> {
                        if (task.getResult().getStatus().isSuccess()){
                            Log.e(LOG_TAG, "workout read success " + workoutArrayList.size());
                            for (Workout workout : workoutArrayList) {
                                if (Utilities.isGymWorkout(workout.activityID) && (workout.packageName.length() > 0) && ((workout.start > 0) && (workout.end > 0))) {
                                    Log.e(LOG_TAG, "reading for workout " + workout.packageName);
                                    Device device = Device.getLocalDevice(context);
                                    DataSource dataSource = new DataSource.Builder()
                                            .setAppPackageName(workout.packageName)
                                            .setDevice(device)
                                            .setDataType(DataType.TYPE_WORKOUT_EXERCISE)
                                            .setType(DataSource.TYPE_RAW)
                                            .build();
                                    SessionReadRequest readRequest = new SessionReadRequest.Builder()
                                            .setTimeInterval(workout.start, workout.end, TimeUnit.MILLISECONDS)
                                            .read(dataSource)
                                            .enableServerQueries()
                                            .readSessionsFromAllApps()
                                            .build();
                                    Task<SessionReadResponse> taskSet = Fitness.getSessionsClient(context, gsa)
                                            .readSession(readRequest);
                                    taskSet.addOnCompleteListener(sessionCompleter);
                                    if (taskSet.getResult().getStatus().isSuccess())
                                        Log.e(LOG_TAG, "session set read success");
                                }
                            }
                        }
                        return null;
                    });
                }else
                    return Result.retry();

            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                return Result.failure();
            } finally{
                // If there were no errors, return SUCCESS
                Data outputData = new Data.Builder()
                        .putInt(ARG_RESULT_KEY, 1)
                        .putString(Constants.KEY_FIT_USER, sUserID)
                        .putString(Constants.KEY_FIT_VALUE, FitnessHistoryWorker.class.getSimpleName())
                        .putLong(ARG_END_KEY,endTime)
                        .putLong(ARG_START_KEY,startTime)
                        .build();
                return Result.success(outputData);

            }
    }

    private String getFitnessActivityTextById(long id){
        String sRet = Constants.ATRACKIT_EMPTY;
        int idSearch = Math.toIntExact(id);
        try {
            int index = Arrays.binarySearch(mIDSortedArray, idSearch);
            if (index >= 0)
                sRet = mNamesIDSortedArray[index];
        }catch(Exception e){
            Log.e(LOG_TAG, e.getMessage());
        }
        return sRet;
    }
    private String getFitnessActivityIdentifierById(long id){
        String sRet = Constants.ATRACKIT_EMPTY;
        int idSearch = Math.toIntExact(id);
        try{
            int index = Arrays.binarySearch(mIDSortedArray, idSearch);
            if (index >= 0)
                sRet = mIdentifiersIDSortedArray[index];
        }catch(Exception e){
            Log.e(LOG_TAG, e.getMessage());
        }
        return sRet;
    }
    /** Gets {@link FitnessOptions} in order to check or request OAuth permission for the user. */
    private FitnessOptions getFitnessSignInOptions() {
        return FitnessOptions.builder()
                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_POWER_SAMPLE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                //  .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_WORKOUT_EXERCISE,FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.AGGREGATE_ACTIVITY_SUMMARY, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_POWER_SUMMARY, FitnessOptions.ACCESS_READ)
                .build();
    }
    private Bundle datapointToResultBundle(DataPoint dp){
        Bundle resultBundle = new Bundle();
        try {
            if (dp.getDataType().getName().equals(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())) {
                Workout workout = new Workout();
                workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
                workout.activityName = getFitnessActivityTextById(workout.activityID);
                workout.identifier = getFitnessActivityIdentifierById(workout.activityID);
                workout.setCount = dp.getValue(Field.FIELD_NUM_SEGMENTS).asInt();
                workout.duration = (long) dp.getValue(Field.FIELD_DURATION).asInt();
                Log.d(LOG_TAG, "activity.summary " + workout.activityName + " " + workout.setCount);
                if (!Utilities.isDetectedActivity(workout.activityID)) {
                    resultBundle.putParcelable(dp.getDataType().getName(), workout);
                }
            } else if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
                Workout workout = new Workout();
                workout.wattsTotal = dp.getValue(Field.FIELD_WATTS).asFloat();
                Log.d(LOG_TAG, "power.sample " + workout.activityName + " " + workout.setCount);
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
                if (set.start == 0)
                    if ((dp.getStartTime(TimeUnit.MILLISECONDS) > 0) && (dp.getEndTime(TimeUnit.MILLISECONDS) > 0)) {
                        set.start = dp.getStartTime(TimeUnit.MILLISECONDS);
                        set.end = dp.getEndTime(TimeUnit.MILLISECONDS);
                    }

                if (set.start > 0) set._id = set.start;
                else set._id = System.currentTimeMillis();
                String sExercise = dp.getValue(Field.FIELD_EXERCISE).asString();
                set.exerciseName = sExercise;
                set.per_end_xy = sExercise;
                set.resistance_type = (long)dp.getValue(Field.FIELD_RESISTANCE_TYPE).asInt();

                set.weightTotal = dp.getValue(Field.FIELD_RESISTANCE).asFloat();
                set.repCount = dp.getValue(Field.FIELD_REPETITIONS).asInt();
                set.wattsTotal = (set.weightTotal * set.repCount);
                set.activityName = getFitnessActivityTextById(Constants.WORKOUT_TYPE_STRENGTH);
                set.activityID = Constants.WORKOUT_TYPE_STRENGTH;
                if ((dp.getOriginalDataSource() != null) && (dp.getOriginalDataSource().getAppPackageName().length() > 0))
                    set.score_card = dp.getOriginalDataSource().getAppPackageName();
                else
                    set.score_card = Constants.ATRACKIT_ATRACKIT_CLASS;
                resultBundle.putParcelable(WorkoutSet.class.getSimpleName(), set);
            } else if (dp.getDataType().getName().equals(dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SEGMENT.getName()))) {
                Workout workout = new Workout();
                workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
                boolean bIsDetected = Utilities.isDetectedActivity(workout.activityID);
                if (!bIsDetected) {
                    workout.activityName = getFitnessActivityTextById(workout.activityID);
                    workout.identifier = getFitnessActivityIdentifierById(workout.activityID);

//                    float confidence = dp.getValue(Field.FIELD_ACTIVITY_CONFIDENCE).asFloat();
//                    workout.wattsTotal = confidence;
//                    Log.d(LOG_TAG, "dp activity sample " + workout.activityID + " " + workout.activityName + " conf " + confidence + " " + bIsDetected);
                    long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                    if (startTime == 0)
                        startTime = System.currentTimeMillis();
                    workout.start = startTime;
                    workout._id = workout.start + workout.activityID;
                    long endTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
                    if (endTime == 0 || (endTime == startTime)) dp.getEndTime(TimeUnit.MILLISECONDS);
                    if (endTime > 0 ) workout.end = endTime;
                    workout.duration = workout.end - workout.start;
                    workout.packageName = dp.getOriginalDataSource().getAppPackageName();
                    resultBundle.putParcelable(Workout.class.getSimpleName(), workout);
                }
            } else if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
                float watts = dp.getValue(Field.FIELD_WATTS).asFloat();
                Workout workout = new Workout();
                workout.activityName = "Power Sample";
                workout.wattsTotal = watts;
                Log.d(LOG_TAG, "dp power sample " + watts);
                resultBundle.putParcelable(Constants.MAP_WATTS, workout);
            } else if (dp.getDataType().getName().equals(DataType.TYPE_MOVE_MINUTES.getName())) {
                Integer minutes = (dp.getValue(Field.FIELD_DURATION) != null) ? dp.getValue(Field.FIELD_DURATION).asInt() : 0;
                resultBundle.putInt(Constants.MAP_MOVE_MINS, minutes);
            } else if (dp.getDataType().getName().equals(DataType.TYPE_CALORIES_EXPENDED.getName())) {
                float calories = dp.getValue(Field.FIELD_CALORIES).asFloat();
                resultBundle.putFloat(Constants.MAP_CALORIES, calories);
            } else if (dp.getDataType().getName().contains(DataType.TYPE_DISTANCE_DELTA.getName())) {
                float distance = dp.getValue(Field.FIELD_DISTANCE).asFloat();
                resultBundle.putFloat(Constants.MAP_DISTANCE, distance);
            } else if (dp.getDataType().getName().contains("step_count")) {
                int steps = dp.getValue(Field.FIELD_STEPS).asInt();
                resultBundle.putInt(Constants.MAP_STEPS, steps);
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
            }
        }catch(Exception e){
            Log.e(LOG_TAG, e.getMessage());
        }
        return resultBundle;

    }
    private void dumpSession(Session session) {
        DateFormat dateFormat = getTimeInstance();
        Log.d(LOG_TAG, "Data returned for Session: " + session.getName()
                + "\n\tDescription: " + session.getDescription()
                + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
    }
    private void dumpDataSet(DataSet dataSet) {
        Log.d(LOG_TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.d(LOG_TAG, "Data point:");
            Log.d(LOG_TAG, "\tType: " + dp.getDataType().getName());
            Log.d(LOG_TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.d(LOG_TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.d(LOG_TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }
    /*
    private static class insertWorkoutAsyncTask extends AsyncTask<Workout, Void, Void> {

        private WorkoutDao mAsyncTaskDao;

        insertWorkoutAsyncTask(WorkoutDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Workout... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
    private static class insertWorkoutSetAsyncTask extends AsyncTask<WorkoutSet, Void, Void> {

        private WorkoutSetDao mAsyncTaskDao;

        insertWorkoutSetAsyncTask(WorkoutSetDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final WorkoutSet... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
    private static class getWorkoutAsyncTask extends AsyncTask<Long, Void, List<Workout>> {

        private WorkoutDao mAsyncTaskDao;
        private String userID;
        getWorkoutAsyncTask(WorkoutDao dao, String sUserId) {
            mAsyncTaskDao = dao; userID = sUserId;
        }

        @Override
        protected List<Workout>doInBackground(final Long... params) {
            if (params.length == 2)
                return mAsyncTaskDao.getWorkoutByStarts(userID, params[0], params[1]);
            else
                return  mAsyncTaskDao.getWorkoutByIdUserId(params[0], userID);

        }

    }
    private static class getWorkoutSetAsyncTask extends AsyncTask<Long, Void, List<WorkoutSet>> {

        private WorkoutSetDao mAsyncTaskDao;
        private String userID;

        getWorkoutSetAsyncTask(WorkoutSetDao dao, String sUserID) {
            mAsyncTaskDao = dao;userID = sUserID;
        }

        @Override
        protected List<WorkoutSet>doInBackground(final Long... params) {
            if (params.length == 2)
                return mAsyncTaskDao.getWorkoutSetByStarts(userID, params[0], params[1]);
            else
                return  mAsyncTaskDao.getWorkoutSetByIdUserId(params[0], userID);

        }

    }*/
}