package com.a_track_it.workout.common.data_model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.ReferencesTools;
import com.a_track_it.workout.common.Utilities;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.fitness.Fitness.getHistoryClient;
import static java.text.DateFormat.getTimeInstance;

public class FitPopulateJobIntentService extends JobIntentService {
    private final static String LOG_TAG = FitPopulateJobIntentService.class.getSimpleName();
    private final Handler mHandler = new Handler();
    public static final String ARG_RESULT_KEY = "result-key";
    public static final String ARG_ACTION_KEY = "action-key";
    public static final String ARG_START_KEY = "start-key";
    public static final String ARG_END_KEY = "end-key";
    /**
     * Unique job ID for this service.
     */
    private static final int JOB_ID = 2;

    private WorkoutDao mWorkoutDao;
    private WorkoutSetDao mWorkoutSetDao;
    private WorkoutMetaDao mWorkoutMetaDao;
    private ExerciseDao mExerciseDao;
    private BodypartDao mBodypartDao;
    private ReferencesTools referencesTools;
    ArrayList<Workout> workoutArrayList = new ArrayList<>();
    ArrayList<WorkoutSet> workoutSetArrayList = new ArrayList<>();
    ArrayList<Workout> workoutExistingArrayList = new ArrayList<>();
    ArrayList<WorkoutSet> workoutSetExistingArrayList = new ArrayList<>();
    ArrayList<WorkoutMeta> workoutMetaArrayList = new ArrayList<>();

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, FitPopulateJobIntentService.class, JOB_ID, intent);
    }

    public FitPopulateJobIntentService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        WorkoutRoomDatabase db = WorkoutRoomDatabase.getDatabase(context);
        mWorkoutDao = db.workoutDao();
        mWorkoutSetDao = db.workoutSetDao();
        mWorkoutMetaDao = db.workoutMetaDao();
        mExerciseDao = db.exerciseDao();
        mBodypartDao = db.bodypartDao();
        referencesTools = ReferencesTools.setInstance(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Calendar cal = Calendar.getInstance();
        ResultReceiver resultReceiver = intent.getParcelableExtra(LOG_TAG);
        Bundle resultBundle = new Bundle();
        String sUserID = intent.getStringExtra(Constants.KEY_FIT_USER);
        String sDeviceID = intent.getStringExtra(Constants.KEY_FIT_DEVICE_ID);
        try {
            Context context = getApplicationContext();
            Device device = Device.getLocalDevice(context);
            String sPackage = context.getPackageName();
            String sTitle;
            cal.setTime(new Date());
            long endTime  = intent.getLongExtra(ARG_END_KEY,0);
            long startTime = intent.getLongExtra(ARG_START_KEY,0);
            if (endTime == 0){
                endTime = cal.getTimeInMillis();
                cal.add(Calendar.DAY_OF_YEAR, -7);
                startTime = cal.getTimeInMillis();
            }
            int processed = 0;

            final ReferencesTools referencesTools = ReferencesTools.getInstance();
            referencesTools.init(context);
            List<Workout> listWorkouts = mWorkoutDao.getWorkoutUnSyncdByStarts(sUserID,sDeviceID,startTime,endTime);
            workoutArrayList.addAll(listWorkouts);
            List<WorkoutSet> listSets = mWorkoutSetDao.getWorkoutSetByUnSyncByDates(sUserID,sDeviceID,startTime,endTime);
            workoutSetArrayList.addAll(listSets);
            GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(context, referencesTools.getFitnessSignInOptions(1));
            if ((workoutArrayList.size() > 0) || (workoutSetArrayList.size() > 0))
                if (gsa != null) {
                    DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                            .read(DataType.TYPE_ACTIVITY_SEGMENT)
                            .read(DataType.TYPE_WORKOUT_EXERCISE)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .enableServerQueries() // Used to retrieve data from cloud.
                            .build();
                    Task<DataReadResponse> response = getHistoryClient(context, gsa)
                            .readData(dataReadRequest);

                    DataReadResponse readDataResult = Tasks.await(response, 3,TimeUnit.MINUTES);
                    if (readDataResult.getStatus().isSuccess()){
                        int iRet = readDataResult.getDataSets().size();
                        Log.d(LOG_TAG, "Successful read history size " + iRet);
                        for (DataSet dataSet : readDataResult.getDataSets()) {
                            dumpDataSet(dataSet);
                            List<DataPoint> dpList = dataSet.getDataPoints();
                            for (DataPoint dp : dpList) {
                                Log.d(LOG_TAG, "existing data " + dp.getDataType().getName() + " dps " + dpList.size());
                                Bundle dataBundle = DataPointToResultBundle(dp, referencesTools);
                                if (dataBundle.containsKey(Workout.class.getSimpleName())) {
                                    Workout w = dataBundle.getParcelable(Workout.class.getSimpleName());
                                    workoutExistingArrayList.add(w);
                                }
                                if (dataBundle.containsKey(WorkoutSet.class.getSimpleName())) {
                                    WorkoutSet ws = dataBundle.getParcelable(WorkoutSet.class.getSimpleName());
                                    workoutSetExistingArrayList.add(ws);
                                }
                            }
                        }
                        for (Workout workout : workoutArrayList){
                            if ((workoutExistingArrayList.size() == 0) || !isExistingOrOverLapping(workout)){
                                sTitle =  Utilities.getPartOfDayString(workout.start) + Constants.ATRACKIT_SPACE + workout.activityName;
                                DataSet activityDataSet = createActivityDataSet(workout.start,workout.end,workout.activityName,device);

                                Task<Void> task = Fitness.getHistoryClient(context, gsa).insertData(activityDataSet);
                                Tasks.await(task, 3L, TimeUnit.MINUTES);
                                if (task.isSuccessful()){
                                    long nowUpdated = System.currentTimeMillis();
                                    processed += 1;
                                    workout.last_sync = nowUpdated;
                                    mWorkoutDao.update(workout);
                                }
                            }
                        }
                        for (WorkoutSet workoutSet : workoutSetArrayList){
                            if (Utilities.isGymWorkout(workoutSet.activityID))
                                if ((workoutExistingArrayList.size() == 0) || !isExistingOrOverLappingSet(workoutSet)){
                                    sTitle =  Utilities.getPartOfDayString(workoutSet.start) + Constants.ATRACKIT_SPACE + workoutSet.activityName;
                                    DataSet exerciseSet = Utilities.createExerciseDataSet(workoutSet.start,workoutSet.end, ((workoutSet.per_end_xy.length() > 0)?workoutSet.per_end_xy: workoutSet.exerciseName),workoutSet.repCount,
                                            workoutSet.resistance_type,workoutSet.weightTotal,workoutSet.score_card, device);
                                    if (exerciseSet != null) {
                                        Task<Void> task = Fitness.getHistoryClient(context, gsa).insertData(exerciseSet);
                                        Tasks.await(task, 3, TimeUnit.MINUTES);
                                        if (task.isSuccessful()) {
                                            processed += 1;
                                            long nowUpdated = System.currentTimeMillis();
                                            workoutSet.last_sync = nowUpdated;
                                            mWorkoutSetDao.update(workoutSet);
                                        }
                                    }
                                }
                        }
                    }else{
                        //
                        String sErr = readDataResult.getStatus().getStatusMessage();
                        resultBundle.putString(Constants.INTENT_EXTRA_MSG, sErr);
                        resultReceiver.send(505, resultBundle);

                    }
                }else{
                    //
                    String sErr = "Google account not logged in";
                    resultBundle.putString(Constants.INTENT_EXTRA_MSG, sErr);
                    resultReceiver.send(505, resultBundle);

                }
            referencesTools.killme();
            resultBundle.putInt(Constants.INTENT_EXTRA_RESULT, processed);
            resultReceiver.send(200, resultBundle);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            String sErr = e.getMessage();
            resultBundle.putString(Constants.INTENT_EXTRA_MSG, sErr);
            resultReceiver.send(505, resultBundle);
        }
    }

    private boolean isExistingOrOverLapping(Workout newTest){
        boolean bfound = false;
        for (Workout w : workoutExistingArrayList){
            if ((newTest._id == w._id) || (newTest.overlaps(w))){
               bfound = true;
               break;
            }
        }
        return bfound;
    }
    private boolean isExistingOrOverLappingSet(WorkoutSet newTest){
        boolean bfound = false;
        for (WorkoutSet s : workoutSetExistingArrayList){
            if ((newTest._id == s._id) || (newTest.overlaps(s))){
                bfound = true;
                break;
            }
        }
        return bfound;
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
    private Bundle DataPointToResultBundle(DataPoint dp, ReferencesTools referencesTools){
        Bundle resultBundle = new Bundle();
        if (dp.getDataType().getName().equals(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())) {
            Workout workout = new Workout();
            workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
            workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
            workout.identifier = referencesTools.getFitnessActivityIdentifierById(workout.activityID);
            workout.setCount = dp.getValue(Field.FIELD_NUM_SEGMENTS).asInt();
            workout.duration = (long)dp.getValue(Field.FIELD_DURATION).asInt();
            Log.d(LOG_TAG, "activity.summary " + workout.activityName + " " + workout.setCount);
            if (!Utilities.isDetectedActivity(workout.activityID)) {
                resultBundle.putParcelable(Workout.class.getSimpleName(), workout);
            }
        } else if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
            Workout workout = new Workout();
            workout.wattsTotal = dp.getValue(Field.FIELD_WATTS).asFloat();
            Log.d(LOG_TAG, "power.sample " + workout.activityName + " " + workout.setCount);
            resultBundle.putParcelable(Workout.class.getSimpleName(), workout);
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

            if (set.start > 0) set._id = set.start; else set._id = System.currentTimeMillis();
            String sExercise = dp.getValue(Field.FIELD_EXERCISE).asString();
            set.exerciseName = sExercise;
            set.per_end_xy = sExercise;
            set.resistance_type = (long)dp.getValue(Field.FIELD_RESISTANCE_TYPE).asInt();

            set.weightTotal = dp.getValue(Field.FIELD_RESISTANCE).asFloat();
            set.repCount = dp.getValue(Field.FIELD_REPETITIONS).asInt();
            set.wattsTotal = (set.weightTotal * set.repCount);
            set.activityName = referencesTools.getFitnessActivityTextById(Constants.WORKOUT_TYPE_STRENGTH);
            set.activityID = Constants.WORKOUT_TYPE_STRENGTH;
            if ((dp.getOriginalDataSource() != null) && (dp.getOriginalDataSource().getAppPackageName().length() > 0))
                set.score_card = dp.getOriginalDataSource().getAppPackageName();
            else
                set.score_card = Constants.ATRACKIT_ATRACKIT_CLASS;
            Log.d(LOG_TAG, "dp " + referencesTools.workoutSetShortText(set));
            resultBundle.putParcelable(WorkoutSet.class.getSimpleName(), set);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
            Workout workout = new Workout();
            workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
            boolean bIsDetected = Utilities.isDetectedActivity(workout.activityID);
            workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
            workout.identifier = referencesTools.getFitnessActivityIdentifierById(workout.activityID);
//            if (dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SAMPLE.getName())) {
//                float confidence = dp.getValue(Field.FIELD_ACTIVITY_CONFIDENCE).asFloat();
//                workout.wattsTotal = confidence;
//                Log.d(LOG_TAG, "dp activity sample " + workout.activityID + " " + workout.activityName + " conf " + confidence + " " + bIsDetected);
//            } else {
            Log.d(LOG_TAG, "dp activity segment " + workout.activityID + " " + workout.activityName + " " + bIsDetected);

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
            // if (workout.activityID != 3)
            if (!bIsDetected)
                resultBundle.putParcelable(Workout.class.getSimpleName(), workout);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
            float watts = dp.getValue(Field.FIELD_WATTS).asFloat();
            Workout workout = new Workout();
            workout.activityName = "Power Sample";
            workout.wattsTotal = watts;
            Log.d(LOG_TAG, "dp power sample " + watts);
            resultBundle.putParcelable(Constants.MAP_WATTS, workout);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_MOVE_MINUTES.getName())) {
            Integer minutes = ( dp.getValue(Field.FIELD_DURATION) != null) ? dp.getValue(Field.FIELD_DURATION).asInt() : 0;
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
        }else if (dp.getDataType().getName().equals(DataType.AGGREGATE_HEART_RATE_SUMMARY.getName())) {
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
        return resultBundle;

    }

    private DataSet createActivityDataSet(long startTime, long endTime, String activityName, Device device) {
        // Create a_track_it.com data source
        DataSource activityDataSource = new DataSource.Builder()
                .setAppPackageName(Constants.ATRACKIT_ATRACKIT_CLASS)
                .setDevice(device)
                .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                .setType(DataSource.TYPE_RAW)
                .build();
        try{
            DataSet.Builder activityDataSetBuilder = DataSet.builder(activityDataSource);
            DataPoint.Builder activityBuilder = DataPoint.builder(activityDataSource);
            activityBuilder.setActivityField(Field.FIELD_ACTIVITY, activityName);
            if (startTime < endTime)
                activityBuilder.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
            else
                activityBuilder.setTimestamp(startTime, TimeUnit.MILLISECONDS);

            activityDataSetBuilder.add(activityBuilder.build());
            return activityDataSetBuilder.build();
        }catch (Exception e){
            String sMsg = e.getMessage();
            if (sMsg != null) Log.e("createActivityDataSet", sMsg);
            return null;
        }
    }
}

