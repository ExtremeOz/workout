package com.a_track_it.workout.common.data_model;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.R;
import com.a_track_it.workout.common.ReferencesTools;
import com.a_track_it.workout.common.Utilities;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FitnessMetaWorker extends Worker {
    private static final String LOG_TAG = FitnessMetaWorker.class.getSimpleName();
    public static final String ARG_RESULT_KEY = "result-key";
    public static final String ARG_ACTION_KEY = "action-key";
    public static final String ARG_START_KEY = "start-key";
    public static final String ARG_END_KEY = "end-key";
    public static final String ARG_WORKOUT_ID_KEY = "id-workout-key";
    public static final String ARG_SET_ID_KEY = "id-set-key";

    private WorkoutDao mWorkoutDao;
    private WorkoutSetDao mWorkoutSetDao;
    private WorkoutMetaDao mWorkoutMetaDao;
//    private ExerciseDao mExerciseDao;
//    private BodypartDao mBodypartDao;
    private ReferencesTools referencesTools;
    ArrayList<Workout> workoutArrayList = new ArrayList<>();
    ArrayList<WorkoutSet> workoutSetArrayList = new ArrayList<>();
    ArrayList<WorkoutMeta> workoutMetaArrayList = new ArrayList<>();

    private static int[] mIDSortedArray;
    private static String[] mNamesIDSortedArray;
    private static String[] mIdentifiersIDSortedArray;

    public FitnessMetaWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Context context2 = context.getApplicationContext();
        WorkoutRoomDatabase db = WorkoutRoomDatabase.getDatabase(context2);
        mWorkoutDao = db.workoutDao();
        mWorkoutSetDao = db.workoutSetDao();
        mWorkoutMetaDao = db.workoutMetaDao();
//        mExerciseDao = db.exerciseDao();
//        mBodypartDao = db.bodypartDao();
        referencesTools = ReferencesTools.setInstance(context2);
        Resources resources = context2.getResources();
        mNamesIDSortedArray =resources.getStringArray(R.array.activity_name_id_sorted);
        mIDSortedArray = resources.getIntArray(R.array.activity_id_sorted);
        mIdentifiersIDSortedArray = resources.getStringArray(R.array.activity_ident_id_sorted);
    }
    /** doWork
         read un-synchronized workouts and sets - read summaries and insert history
    **/
    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();
            GoogleSignInAccount gsa =  GoogleSignIn.getAccountForExtension(context, referencesTools.getFitnessSignInOptions(0));

            int metaAction = getInputData().getInt(ARG_ACTION_KEY, 0);
            long endTime  = getInputData().getLong(ARG_END_KEY,0);
            long startTime = getInputData().getLong(ARG_START_KEY,0);
            long workoutID = getInputData().getLong(ARG_WORKOUT_ID_KEY, 0);
            long setID = getInputData().getLong(ARG_SET_ID_KEY, 0);
            String sUserID = getInputData().getString(Constants.KEY_FIT_USER);
            String sDeviceID = getInputData().getString(Constants.KEY_FIT_DEVICE_ID);
           // Log.e(LOG_TAG, "meta params a " + metaAction + " s " + startTime + " e " + endTime + " workoutID " + workoutID + " setID " + setID);
            if ((sUserID == null) || (sUserID.length() == 0) || (metaAction == 0))
                return Result.failure();
            final ReferencesTools referencesTools = ReferencesTools.setInstance(context);

            if ((metaAction == Constants.TASK_ACTION_READ_SESSION) || (metaAction == Constants.TASK_ACTION_STOP_SESSION)) {
                List<Workout> listWorkouts = (workoutID == 0) ? mWorkoutDao.getWorkoutByDeviceStarts(sUserID,sDeviceID,startTime, endTime)
                        : mWorkoutDao.getWorkoutByIdUserId(workoutID, sUserID);
                if ((listWorkouts != null) && (listWorkouts.size() > 0))
                    workoutArrayList.addAll(listWorkouts);
            }
            // check to ensure
            if (workoutArrayList != null && (workoutArrayList.size() > 0)) {
                Iterator<Workout> workoutIterator = workoutArrayList.iterator();
                while (workoutIterator.hasNext()) {
                    Workout w = workoutIterator.next();
                    if ((w != null) && (w._id > 0)){
                       List<WorkoutMeta> metaList = mWorkoutMetaDao.getMetaByWorkoutUserId(w._id,w.userID);
                       if (metaList != null && (metaList.size() > 0)) workoutIterator.remove();
                    }
                }
            }

            List<WorkoutSet> listSets = (setID > 0) ? mWorkoutSetDao.getWorkoutSetByIdUserId(setID, sUserID) : mWorkoutSetDao.getWorkoutSetByWorkoutID(workoutID, sUserID);
            if ((listSets != null) && (listSets.size() > 0)) {
                workoutSetArrayList.addAll(listSets);
            }

            if ((metaAction == Constants.TASK_ACTION_READ_SESSION)
                    || (metaAction == Constants.TASK_ACTION_STOP_SESSION)) {
                for (Workout workout : workoutArrayList) {
                    boolean updateWorkout = false;
                    long timeMs = System.currentTimeMillis();
                    if (((metaAction == Constants.TASK_ACTION_READ_SESSION) && (workout.duration > Constants.DURATION_MIN_TIME_MILLIS))
                            || (metaAction == Constants.TASK_ACTION_STOP_SESSION)){
                        int duration = Math.toIntExact(workout.end - workout.start);
                        List<WorkoutMeta> listMetas = mWorkoutMetaDao.getMetaByWorkoutUserDeviceId(workout._id,sUserID,sDeviceID);
                        WorkoutMeta workoutMeta = new WorkoutMeta();
                        if (listMetas.size() > 0)
                            workoutMeta = listMetas.get(0);
                        else{
                            workoutMeta._id = 0;
                            workoutMeta.workoutID = workout._id;
                            workoutMeta.userID = workout.userID;
                            workoutMeta.setID = null;
                            workoutMeta.cloud_sync = timeMs;
                            updateWorkout = true;
                        }                        
                        DataReadRequest.Builder builder = new DataReadRequest.Builder();
                        builder.aggregate(DataType.TYPE_DISTANCE_DELTA)
                                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                                .aggregate(DataType.TYPE_SPEED)
                                .aggregate(DataType.TYPE_CALORIES_EXPENDED)
                                .aggregate(DataType.TYPE_HEART_RATE_BPM)
                                .aggregate(DataType.TYPE_POWER_SAMPLE)
                                .aggregate(DataType.TYPE_MOVE_MINUTES)
                                .aggregate(DataType.TYPE_HEART_POINTS)
                                .setTimeRange(workout.start, workout.end, TimeUnit.MILLISECONDS)
                                .bucketBySession(duration, TimeUnit.MILLISECONDS)
                                .enableServerQueries();// Used to retrieve data from cloud.
                        DataReadRequest request = builder.build();
                        Task<DataReadResponse> responseTask = Fitness.getHistoryClient(context, gsa)
                                .readData(request);
                        DataReadResponse task = Tasks.await(responseTask,5,TimeUnit.MINUTES);
                        if (task.getStatus().isSuccess()) {
                            int iRet = task.getBuckets().size();
                            if (iRet > 0) {
                                for (Bucket bucket : task.getBuckets()) {
                                    List<DataSet> dataSets = bucket.getDataSets();
                                    for (DataSet dataSet : dataSets) {
                                        List<DataPoint> dpList = dataSet.getDataPoints();
                                        for (DataPoint dp : dpList) {
                                            DataPointToMetaData(dp, referencesTools, workoutMeta);
                                        }
                                    }
                                }
                            }else{
                                iRet = task.getDataSets().size();
                                if (iRet > 0) {
                                    for (DataSet dataSet : task.getDataSets()) {
                                        List<DataPoint> dpList = dataSet.getDataPoints();
                                        for (DataPoint dp : dpList) {
                                            DataPointToMetaData(dp, referencesTools, workoutMeta);
                                        }
                                    }
                                }
                            }
                            if ((workout.stepCount < workoutMeta.stepCount)){
                                workout.stepCount = workoutMeta.stepCount;
                                updateWorkout = true;
                            }
/*                            if (!Utilities.isGymWorkout(workout.activityID) && !Utilities.isShooting(workout.activityID)){
                                if (workoutMeta.distance > 0F){
                                    workout.weightTotal = workoutMeta.distance;
                                    updateWorkout = true;
                                }
                                if (workoutMeta.avgBPM > 0F){
                                    workout.wattsTotal = workoutMeta.avgBPM;
                                    updateWorkout = true;
                                }
                                if (workoutMeta.move_mins > 0L){
                                    workout.repCount = (int)workoutMeta.move_mins;
                                    updateWorkout = true;
                                }
                                if (workoutMeta.heart_pts > 0F){
                                    workout.setCount = Math.round(workoutMeta.heart_pts);
                                    updateWorkout = true;
                                }
                            }else{*/
                                if (Utilities.isGymWorkout(workout.activityID)){
                                    if ((workoutMeta.setCount == 0) && (workout.setCount > 0)) workoutMeta.setCount = workout.setCount;
                                    if ((workoutMeta.repCount == 0) && (workout.repCount > 0)) workoutMeta.repCount = workout.repCount;
                                    if ((workoutMeta.weightTotal == 0F) && (workout.weightTotal > 0F)) workoutMeta.weightTotal = workout.weightTotal;
                                    if ((workoutMeta.wattsTotal == 0F) && (workout.wattsTotal > 0F)) workoutMeta.wattsTotal = workout.wattsTotal;
                                }
                   //         }
                            // flagged for insert
                            if (workoutMeta._id == 0){
                                workoutMeta._id = workout._id;
                                workoutMeta.activityID = workout.activityID;
                                workoutMeta.activityName = workout.activityName;
                                workoutMeta.start = workout.start;
                                workoutMeta.end = workout.end;
                                workoutMeta.cloud_sync = timeMs;
                                workoutMeta.duration = workout.duration;
                                workoutMeta.rest_duration = workout.rest_duration;
                                workoutMeta.pause_duration = workout.pause_duration;
                                if (workout.goal_duration > 0) workoutMeta.goal_duration = workout.goal_duration;
                                if (workout.goal_steps > 0) workoutMeta.goal_steps = workout.goal_steps;
                                workoutMeta.packageName = context.getPackageName();
                                workoutMeta.identifier = referencesTools.getFitnessActivityIdentifierById(workoutMeta.activityID);
                                mWorkoutMetaDao.insert(workoutMeta);
                                Log.w(LOG_TAG, "insert Meta " + workoutMeta.toString());
                            }else{
                                workoutMeta.activityID = workout.activityID;
                                workoutMeta.activityName = workout.activityName;
                                workoutMeta.start = workout.start;
                                workoutMeta.end = workout.end;
                                workoutMeta.duration = workout.duration;
                                workoutMeta.rest_duration = workout.rest_duration;
                                workoutMeta.pause_duration = workout.pause_duration;
                                if (workout.goal_duration > 0) workoutMeta.goal_duration = workout.goal_duration;
                                if (workout.goal_steps > 0) workoutMeta.goal_steps = workout.goal_steps;
                                workoutMeta.packageName = context.getPackageName();
                                workoutMeta.cloud_sync = timeMs;
                                workoutMeta.identifier = referencesTools.getFitnessActivityIdentifierById(workoutMeta.activityID);
                                mWorkoutMetaDao.update(workoutMeta);
                                Log.w(LOG_TAG, "update Meta " + workoutMeta.toString());
                            }
                            if (updateWorkout){
                                workout.meta_sync = timeMs;
                                workout.lastUpdated = timeMs;
                                mWorkoutDao.update(workout);
                                Log.w(LOG_TAG, "update Workout " + workout.toString());
                            }
                        } // task success
                    } // activity is long enough
                } // each pending activity update
             }

            if (metaAction == Constants.TASK_ACTION_ACT_SEGMENT
                    || metaAction == Constants.TASK_ACTION_EXER_SEGMENT){
                Log.e(LOG_TAG, "found set size " + workoutSetArrayList.size());
                for (WorkoutSet set : workoutSetArrayList) {
                    if (set.isValid(true)) {
                        WorkoutMeta setMeta = new WorkoutMeta();
                        if (workoutMetaArrayList.size() > 0) {
                                for (WorkoutMeta m : workoutMetaArrayList){
                                    if ((m.workoutID == set.workoutID) && (m.setID == set._id)){
                                        setMeta = m;
                                        break;
                                    }
                                }
                        }else{
                            setMeta._id  = 0L;
                            setMeta.workoutID = set.workoutID;
                            setMeta.setID = set._id;
                            setMeta.activityID = set.activityID;
                            setMeta.activityName = set.activityName;
                            setMeta.userID = set.userID;
                        }
                        int set_duration = Math.toIntExact(set.end-set.start);
                        DataReadRequest.Builder builder = new DataReadRequest.Builder();
                        builder.aggregate(DataType.TYPE_DISTANCE_DELTA)
                                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                                .aggregate(DataType.TYPE_SPEED)
                                .aggregate(DataType.TYPE_CALORIES_EXPENDED)
                                .aggregate(DataType.TYPE_HEART_RATE_BPM)
                                .aggregate(DataType.TYPE_POWER_SAMPLE)
                                .aggregate(DataType.TYPE_MOVE_MINUTES)
                                .aggregate(DataType.TYPE_HEART_POINTS)
                                .setTimeRange(set.start, set.end, TimeUnit.MILLISECONDS)
                                .bucketByTime(set_duration, TimeUnit.MILLISECONDS)
                                .enableServerQueries();// Used to retrieve data from cloud.
                        DataReadRequest request = builder.build();
                        Task<DataReadResponse> responseTask = Fitness.getHistoryClient(context, gsa)
                                .readData(request);
                        DataReadResponse task = Tasks.await(responseTask, 5, TimeUnit.MINUTES);

                        if (task.getStatus().isSuccess()) {
                            int iRet = task.getBuckets().size();
                            if (iRet > 0) {
                                Log.w(LOG_TAG, "received bucket size " + iRet);
                                for (Bucket bucket : task.getBuckets()) {
                                    List<DataSet> dataSets = bucket.getDataSets();
                                    for (DataSet dataSet : dataSets) {
                                        //dumpDataSet(dataSet);
                                        List<DataPoint> dpList = dataSet.getDataPoints();
                                        for (DataPoint dp : dpList) {
                                            Log.e(LOG_TAG, "bucket dp " + dp.getDataType().getName());
                                            DataPointToMetaData(dp, referencesTools, setMeta);
                                        }
                                    }
                                }
                                // update existing workout with meta data
                            }else
                                Log.e(LOG_TAG, "received bucket size " + iRet);
                            iRet = task.getDataSets().size();
                            if (iRet > 0)
                                Log.e(LOG_TAG, "Successful fit read for set " + iRet);
                            else
                                Log.w(LOG_TAG, "Successful fit read for set " + iRet);
                            if (iRet > 0) {
                                for (DataSet dataSet : task.getDataSets()) {
                              //      dumpDataSet(dataSet);
                                    List<DataPoint> dpList = dataSet.getDataPoints();
                                    for (DataPoint dp : dpList) {
                                        Log.d(LOG_TAG, "dataset dp " + dp.getDataType().getName());
                                        DataPointToMetaData(dp, referencesTools, setMeta);
                                    }
                                }
                            }
                            if (setMeta._id == 0){
                                setMeta._id = System.currentTimeMillis();
                                setMeta.activityID = set.activityID;
                                setMeta.activityName = set.activityName;
                                setMeta.start = set.start;
                                setMeta.end = set.end;
                                setMeta.duration = set.duration;
                                setMeta.packageName = context.getPackageName();
                                setMeta.identifier = referencesTools.getFitnessActivityIdentifierById(setMeta.activityID);
                                mWorkoutMetaDao.insert(setMeta);
                            }else{
                                setMeta.activityID = set.activityID;
                                setMeta.activityName = set.activityName;
                                setMeta.start = set.start;
                                setMeta.end = set.end;
                                setMeta.duration = set.duration;
                                setMeta.packageName = context.getPackageName();
                                setMeta.identifier = referencesTools.getFitnessActivityIdentifierById(setMeta.activityID);
                                mWorkoutMetaDao.update(setMeta);
                            }
                        }else {  // successful task
                            Log.e(LOG_TAG, task.getStatus().getStatusMessage());
                        }
                    } // valid set
                } // for each set
            }
            int iRetVal = (gsa != null) ? workoutArrayList.size() + workoutSetArrayList.size() : 0;
            referencesTools.killme();
            // If there were no errors, return SUCCESS
            Data outputData = new Data.Builder()
                    .putInt(ARG_RESULT_KEY, iRetVal)
                    .putString(Constants.KEY_FIT_USER, sUserID)
                    .putString(Constants.KEY_FIT_VALUE, FitnessMetaWorker.class.getSimpleName())
                    .putInt(ARG_ACTION_KEY, metaAction)
                    .putLong(ARG_END_KEY,endTime)
                    .putLong(ARG_START_KEY,startTime)
                    .putLong(ARG_WORKOUT_ID_KEY, workoutID)
                    .putLong(ARG_SET_ID_KEY, setID)
                    .build();
            return Result.success(outputData);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            return Result.failure();
        }

    }
    /*private void dumpSession(Session session) {
        DateFormat dateFormat = getTimeInstance();
        Log.d(LOG_TAG, "Data returned for Session: " + session.getName()
                + "\n\tDescription: " + session.getDescription()
                + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));
    }*/

    private void DataPointToMetaData(DataPoint dp, ReferencesTools referencesTools, WorkoutMeta workoutMeta){
        if (dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
            workoutMeta.activityID = (long) dp.getValue(Field.FIELD_ACTIVITY).asInt();
            boolean bIsDetected = Utilities.isDetectedActivity(workoutMeta.activityID);
            if (!bIsDetected) {
                workoutMeta.activityName = referencesTools.getFitnessActivityTextById(workoutMeta.activityID);
                workoutMeta.identifier = referencesTools.getFitnessActivityIdentifierById(workoutMeta.activityID);
                long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                if (startTime == 0)
                    startTime = System.currentTimeMillis();
                workoutMeta.start = startTime;
                long endTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
                if (endTime == 0 || (endTime == startTime)) dp.getEndTime(TimeUnit.MILLISECONDS);
                if (endTime > 0 ) workoutMeta.end = endTime;
                workoutMeta.duration = workoutMeta.end - workoutMeta.start;
                workoutMeta.packageName = dp.getOriginalDataSource().getAppPackageName();
            }
        } else if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
            float watts = dp.getValue(Field.FIELD_WATTS).asFloat();
            workoutMeta.wattsTotal = watts;
        } else if (dp.getDataType().getName().equals(DataType.TYPE_MOVE_MINUTES.getName())) {
            Integer minutes = ( dp.getValue(Field.FIELD_DURATION) != null) ? dp.getValue(Field.FIELD_DURATION).asInt() : 0;
            workoutMeta.move_mins = minutes;
        } else if (dp.getDataType().getName().equals(DataType.TYPE_CALORIES_EXPENDED.getName())) {
            float calories = dp.getValue(Field.FIELD_CALORIES).asFloat();
            workoutMeta.total_calories = calories;
        } else if (dp.getDataType().getName().contains(DataType.TYPE_DISTANCE_DELTA.getName())) {
            float distance = dp.getValue(Field.FIELD_DISTANCE).asFloat();
            workoutMeta.distance = distance;
        } else if (dp.getDataType().getName().contains(DataType.TYPE_HEART_POINTS.getName())) {
            float pts = dp.getValue(Field.FIELD_INTENSITY).asFloat();
            workoutMeta.heart_pts = pts;
        } else if (dp.getDataType().getName().contains(DataType.TYPE_STEP_COUNT_DELTA.getName())) {
            int steps = dp.getValue(Field.FIELD_STEPS).asInt();
            workoutMeta.stepCount = steps;
        }else if (dp.getDataType().getName().equals(DataType.AGGREGATE_HEART_RATE_SUMMARY.getName())) {
            float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
            float min = dp.getValue(Field.FIELD_MIN).asFloat();
            float max = dp.getValue(Field.FIELD_MAX).asFloat();
            workoutMeta.avgBPM = avg;
            workoutMeta.maxBPM = max;
            workoutMeta.minBPM = min;
        } else if (dp.getDataType().getName().equals(DataType.AGGREGATE_SPEED_SUMMARY.getName())) {
            float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
            float min = dp.getValue(Field.FIELD_MIN).asFloat();
            float max = dp.getValue(Field.FIELD_MAX).asFloat();
            workoutMeta.avgBPM = avg;
            workoutMeta.maxBPM = max;
            workoutMeta.minBPM = min;
        }

    }
    private String getFitnessActivityTextById(int id){
        String sRet = Constants.ATRACKIT_EMPTY;
        try {
            int index = Arrays.binarySearch(mIDSortedArray, id);
            if (index >= 0)
                sRet = mNamesIDSortedArray[index];
        }catch(Exception e){
            Log.e(LOG_TAG, e.getMessage());
        }
        return sRet;
    }
    private String getFitnessActivityIdentifierById(int id){
        String sRet = Constants.ATRACKIT_EMPTY;
        try{
            int index = Arrays.binarySearch(mIDSortedArray, id);
            if (index >= 0)
                sRet = mIdentifiersIDSortedArray[index];
        }catch(Exception e){
            Log.e(LOG_TAG, e.getMessage());
        }
        return sRet;
    }

    /*private void dumpDataSet(DataSet dataSet) {
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
    }*/
}
