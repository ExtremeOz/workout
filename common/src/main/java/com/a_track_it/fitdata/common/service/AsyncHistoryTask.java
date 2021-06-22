package com.a_track_it.fitdata.common.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.NonNull;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.GoalsReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;


public class AsyncHistoryTask extends AsyncTask<Context, Void, java.lang.Integer> {
    private static final int BUCKET_TYPE_TIME = 0;
    private static final int BUCKET_TYPE_ACTIVITY = 1;
    private static final int BUCKET_TYPE_SEGMENT = 2;
    private static final int BUCKET_TYPE_SESSION = 3;
    public static final String KEY_ACTION = "HistoryAction";
    public static final int READ_STEP_DELTA_BY_DAY = 0;
    public static final int READ_ACT_EXER_BY_TIMES = 1;
    public static final int READ_ACT_SEG_BY_SUMMARY = 2;
    public static final int READ_BPM_BY_SUMMARY = 3;
    public static final int READ_SEGMENT_DETAILS = 4;
    public static final int READ_SEGMENT_SAMPLES = 5;
    public static final int READ_DAILY_TOTALS = 6;
    public static final int READ_GOAL_TOTALS = 7;
    private static final String LOG_TAG = AsyncHistoryTask.class.getSimpleName();
    private ResultReceiver receiver;
    private Integer mActionKey;
    private ReferencesTools referencesTools;
    private Workout workout;
    private WorkoutSet workoutSet;
    public AsyncHistoryTask() {
        super();
    }

    public AsyncHistoryTask(ResultReceiver resultReceiver,Integer action, Workout workout, WorkoutSet set){
        this.receiver = resultReceiver;
        if (workout != null) this.workout = workout;
        if (set != null) this.workoutSet = set;
        this.mActionKey = action;
        this.referencesTools = ReferencesTools.getInstance();
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
    }

    @Override
    protected Integer doInBackground(Context... params) {
        Context context = params[0];
        referencesTools.init(context);
        Calendar cal = Calendar.getInstance();
        long startTime = 0L; long endTime = 0L;
        ArrayList<Workout> workoutArrayList = new ArrayList<>();
        ArrayList<WorkoutSet> workoutSetArrayList = new ArrayList<>();
        Bundle resultBundle = new Bundle();
        resultBundle.putInt(KEY_ACTION,mActionKey);
        FitnessOptions fa = referencesTools.getFitnessSignInOptions(0);
        GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(context, fa);
        Integer iRetVal = 0;
        if (gsa != null) {
            OnCompleteListener<DataReadResponse> readCompleter = new OnCompleteListener<DataReadResponse>() {
                @Override
                public void onComplete(@NonNull Task<DataReadResponse> task) {
                 try {
                    if (task.getResult().getStatus().isSuccess()) {
                        int iRet = task.getResult().getBuckets().size();
                        if (iRet > 0) {
                               Log.d(LOG_TAG, "onComplete received bucket size " + iRet);
                            for (Bucket bucket : task.getResult().getBuckets()) {
                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet dataSet : dataSets) {
                                     dumpDataSet(dataSet);
                                    List<DataPoint> dpList = dataSet.getDataPoints();
                                    for (DataPoint dp : dpList) {
                                      //  Log.d(LOG_TAG, "received data-point " + dp.getDataType().getName() + " dps " + dpList.size());
                                        Bundle result = datapointToResultBundle(dp);
                                        if (result.containsKey(Workout.class.getSimpleName())) {
                                            Workout w = result.getParcelable(Workout.class.getSimpleName());
                                            workoutArrayList.add(w);
                                        }
                                        if (result.containsKey(WorkoutSet.class.getSimpleName())) {
                                            WorkoutSet ws = result.getParcelable(WorkoutSet.class.getSimpleName());
                                            workoutSetArrayList.add(ws);
                                        }
                                    }
                                }
                            }
                        } else {
                            iRet = task.getResult().getDataSets().size();
                            Log.e(LOG_TAG, "OnComplete Successful fit read  " + iRet);
                            if (iRet > 0)
                                for (DataSet dataSet : task.getResult().getDataSets()) {
                                    dumpDataSet(dataSet);
                                    List<DataPoint> dpList = dataSet.getDataPoints();
                                    for (DataPoint dp : dpList) {
                                        Bundle result = datapointToResultBundle(dp);
                                        if (result.containsKey(Workout.class.getSimpleName())) {
                                            Workout w = result.getParcelable(Workout.class.getSimpleName());
                                            workoutArrayList.add(w);
                                        }
                                        if (result.containsKey(WorkoutSet.class.getSimpleName())) {
                                            WorkoutSet ws = result.getParcelable(WorkoutSet.class.getSimpleName());
                                            workoutSetArrayList.add(ws);
                                        }
                                    }
                                }
                        }
                       // if (mActionKey != READ_SEGMENT_DETAILS) {
                            if (workoutArrayList.size() > 0) {
                                String sKey = Workout.class.getSimpleName();
                                if (workoutArrayList.size() == 1)
                                    resultBundle.putParcelable(sKey, workoutArrayList.get(0));
                                else {
                                    sKey += "_list";
                                    resultBundle.putParcelableArrayList(sKey, workoutArrayList);
                                }
                            }
                            if (workoutSetArrayList.size() > 0) {
                                String sSetKey = WorkoutSet.class.getSimpleName();
                                if (workoutSetArrayList.size() > 1) {
                                    sSetKey += "_list";
                                    resultBundle.putParcelableArrayList(sSetKey, workoutSetArrayList);
                                } else
                                    resultBundle.putParcelable(sSetKey, workoutSetArrayList.get(0));
                            }
                     //   }
                    }
                }catch (Exception e){
                    Log.e(LOG_TAG,"doInBackground " + e.getMessage());
                     receiver.send(505, null);
                }
            } // [end of OnComplete]
        };
            OnCompleteListener<DataSet> totalsListener = new OnCompleteListener<DataSet>() {
                @Override
                public void onComplete(@NonNull Task<DataSet> task) {
                    if (task.isSuccessful()) {
                        DataSet dataSet = task.getResult();
                        if (dataSet.isEmpty()) {
                            Log.w(LOG_TAG, "empty daily " + dataSet.getDataType().getName());
                        }else {
                            List<DataPoint> dpList = dataSet.getDataPoints();
                            for (DataPoint dp : dpList) {
                                resultBundle.putAll(datapointToResultBundle(dp));
                            }
                        }
                    }
                }
            };
            if (mActionKey < READ_DAILY_TOTALS) {
                try {
                    DataReadRequest.Builder builder = new DataReadRequest.Builder();
                    if (mActionKey == READ_STEP_DELTA_BY_DAY) {
                        if (this.workout == null) {
                            cal.setTimeInMillis(System.currentTimeMillis());
                            cal.set(Calendar.HOUR, 0);
                            cal.set(Calendar.MINUTE, 0);
                            endTime = cal.getTimeInMillis();
                            cal.set(Calendar.DAY_OF_YEAR, -3);
                            startTime = cal.getTimeInMillis();
                        } else {
                            startTime = workout.start;
                            endTime = workout.end;
                        }
                        builder.aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                                .bucketByTime(1, TimeUnit.DAYS)
                                .enableServerQueries();// Used to retrieve data from cloud.
                    }
                    if (mActionKey == READ_ACT_EXER_BY_TIMES) {
                        if (this.workout == null) {
                            cal.setTimeInMillis(System.currentTimeMillis());
                            cal.set(Calendar.HOUR, 0);
                            cal.set(Calendar.MINUTE, 0);
                            endTime = cal.getTimeInMillis();
                            cal.set(Calendar.DAY_OF_YEAR, -3);
                            startTime = cal.getTimeInMillis();
                        } else {
                            startTime = workout.start;
                            endTime = workout.end;
                        }

                        builder.read(DataType.TYPE_WORKOUT_EXERCISE).setLimit(1000)
                                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                                .enableServerQueries();// Used to retrieve data from cloud.
                    }
                    if (mActionKey == READ_ACT_SEG_BY_SUMMARY) {
                        if (this.workout == null) {
                            endTime = cal.getTimeInMillis();
                            cal.setTimeInMillis(System.currentTimeMillis());
                            cal.set(Calendar.HOUR, 0);
                            cal.set(Calendar.MINUTE, 0);
                            startTime = cal.getTimeInMillis();
                        } else {
                            startTime = workout.start;
                            endTime = workout.end;
                        }
                        List<DataType> dt = Arrays.asList(DataType.TYPE_ACTIVITY_SEGMENT, DataType.TYPE_HEART_RATE_BPM, DataType.TYPE_STEP_COUNT_DELTA);
                        List<DataType> ag = Arrays.asList(DataType.AGGREGATE_ACTIVITY_SUMMARY, DataType.AGGREGATE_HEART_RATE_SUMMARY, DataType.AGGREGATE_STEP_COUNT_DELTA);
                        builder = queryDataWithBuckets(startTime, endTime, dt, ag, 1, TimeUnit.MINUTES, BUCKET_TYPE_TIME);
                        builder.enableServerQueries();// Used to retrieve data from cloud.

                    }
                    if (mActionKey == READ_BPM_BY_SUMMARY) {
                        if (this.workout == null) {
                            cal.setTimeInMillis(System.currentTimeMillis());
                            cal.set(Calendar.HOUR, 0);
                            cal.set(Calendar.MINUTE, 0);
                            endTime = cal.getTimeInMillis();
                            cal.set(Calendar.DAY_OF_YEAR, -3);
                            startTime = cal.getTimeInMillis();
                        } else {
                            startTime = workout.start;
                            endTime = workout.end;
                        }
                        List<DataType> dt = Arrays.asList(DataType.TYPE_HEART_RATE_BPM);
                        List<DataType> ag = Arrays.asList(DataType.AGGREGATE_HEART_RATE_SUMMARY);
                        builder = queryDataWithBuckets(startTime, endTime, dt, ag, 1, TimeUnit.DAYS, BUCKET_TYPE_TIME);
                        builder.enableServerQueries();// Used to retrieve data from cloud.

                    }
                    if (mActionKey == READ_SEGMENT_SAMPLES) {
                        cal.setTime(new Date());
                        endTime = cal.getTimeInMillis();
                        cal.set(Calendar.DAY_OF_YEAR, -3);
                        startTime = cal.getTimeInMillis();
                        builder.read(DataType.TYPE_WORKOUT_EXERCISE)
                                .read(DataType.TYPE_POWER_SAMPLE)
                                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                                .setLimit(2000)
                                .enableServerQueries();// Used to retrieve data from cloud.
                    }

                    if (mActionKey == READ_SEGMENT_DETAILS) {
                        if (this.workout == null) {
                            cal.setTime(new Date());
                            cal.set(Calendar.HOUR, 0);
                            cal.set(Calendar.MINUTE, 0);
                            endTime = cal.getTimeInMillis();
                            cal.set(Calendar.DAY_OF_YEAR, -3);
                            startTime = cal.getTimeInMillis();
                        } else {
                            startTime = workout.start;
                            endTime = workout.end;
                        }

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM HH:mm");

                        Log.d(LOG_TAG, "Range Start: " + dateFormat.format(startTime));
                        Log.d(LOG_TAG, "Range End: " + dateFormat.format(endTime));

                        builder.aggregate(DataType.TYPE_DISTANCE_DELTA)
                                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT)
                                .aggregate(DataType.TYPE_CALORIES_EXPENDED)
                                .aggregate(DataType.TYPE_POWER_SAMPLE)
                                .aggregate(DataType.TYPE_MOVE_MINUTES)
                                .aggregate(DataType.TYPE_HEART_POINTS)
                                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                                .bucketByActivitySegment(9,TimeUnit.MINUTES)
                                .setLimit(600)
                                .enableServerQueries();// Used to retrieve data from cloud.
                        DataReadRequest request = builder.build();
                        Task<DataReadResponse> response = Fitness.getHistoryClient(context, gsa)
                                .readData(request);
                        builder = new DataReadRequest.Builder();
                        builder.read(DataType.TYPE_WORKOUT_EXERCISE)
                                .read(DataType.TYPE_POWER_SAMPLE)
                                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                                .setLimit(600)
                                .enableServerQueries();// Used to retrieve data from cloud.
                        DataReadRequest request2 = builder.build();
                        Task<DataReadResponse> response2 = Fitness.getHistoryClient(context, gsa)
                                .readData(request2);
                        response2.addOnCompleteListener(readCompleter);
                        Tasks.whenAllComplete(response2).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    //    if (mActionKey == READ_SEGMENT_DETAILS) {
                                    if (workoutArrayList.size() > 0) {
                                        String sKey = Workout.class.getSimpleName();
                                        if (workoutArrayList.size() == 1)
                                            resultBundle.putParcelable(sKey, workoutArrayList.get(0));
                                        else {
                                            sKey += "_list";
                                            resultBundle.putParcelableArrayList(sKey, workoutArrayList);
                                        }
                                    }
                                    if (workoutSetArrayList.size() > 0) {
                                        String sSetKey = WorkoutSet.class.getSimpleName();
                                        if (workoutSetArrayList.size() > 1) {
                                            sSetKey += "_list";
                                            resultBundle.putParcelableArrayList(sSetKey, workoutSetArrayList);
                                        } else
                                            resultBundle.putParcelable(sSetKey, workoutSetArrayList.get(0));
                                    }
                                    //     }
                                    receiver.send(200, resultBundle);
                                }else
                                    receiver.send(505, null);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                receiver.send(505, null);
                            }
                        });
                    }else{
                        DataReadRequest request = builder.build();

                        Task<DataReadResponse> response = Fitness.getHistoryClient(context, gsa)
                                .readData(request);

                        response.addOnCompleteListener(readCompleter);
                        Tasks.whenAllComplete(response).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    //    if (mActionKey == READ_SEGMENT_DETAILS) {
                                    if (workoutArrayList.size() > 0) {
                                        String sKey = Workout.class.getSimpleName();
                                        if (workoutArrayList.size() == 1)
                                            resultBundle.putParcelable(sKey, workoutArrayList.get(0));
                                        else {
                                            sKey += "_list";
                                            resultBundle.putParcelableArrayList(sKey, workoutArrayList);
                                        }
                                    }
                                    if (workoutSetArrayList.size() > 0) {
                                        String sSetKey = WorkoutSet.class.getSimpleName();
                                        if (workoutSetArrayList.size() > 1) {
                                            sSetKey += "_list";
                                            resultBundle.putParcelableArrayList(sSetKey, workoutSetArrayList);
                                        } else
                                            resultBundle.putParcelable(sSetKey, workoutSetArrayList.get(0));
                                    }
                                    //     }
                                    receiver.send(200, resultBundle);
                                }else
                                    receiver.send(505, null);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                receiver.send(505, null);
                            }
                        });
                    }
                    return 1;
                } catch (Exception e) {
                    //  Log.e(LOG_TAG, e.getMessage());
                    e.printStackTrace();
                    receiver.send(505, null);
                    return 0;
                }
            }
            if (mActionKey == READ_DAILY_TOTALS) {
                try {
                    Task<DataSet> bpmResponse = Fitness.getHistoryClient(context, gsa)
                            .readDailyTotal(DataType.TYPE_HEART_RATE_BPM);
                    bpmResponse.addOnCompleteListener(totalsListener);

                    cal.setTime(new Date());
                    endTime = cal.getTimeInMillis();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    startTime = cal.getTimeInMillis();
                    DataReadRequest stepCountRequest = queryStepEstimate(startTime, endTime);
                    DataReadRequest.Builder builder = new DataReadRequest.Builder();
                    builder.aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .bucketByTime(1, TimeUnit.DAYS)
                            .enableServerQueries();
                    Task<DataReadResponse> step2Response = Fitness.getHistoryClient(context, gsa)
                            .readData(stepCountRequest);
                    step2Response.addOnCompleteListener(readCompleter);
                    Task<DataSet> stepResponse = Fitness.getHistoryClient(context, gsa)
                            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA);
                    stepResponse.addOnCompleteListener(totalsListener);
                    Task<DataSet> speedResponse = Fitness.getHistoryClient(context, gsa)
                            .readDailyTotal(DataType.TYPE_SPEED);
                    speedResponse.addOnCompleteListener(totalsListener);
                    Task<DataSet> distanceResponse = Fitness.getHistoryClient(context, gsa)
                            .readDailyTotal(DataType.TYPE_DISTANCE_DELTA);
                    distanceResponse.addOnCompleteListener(totalsListener);
                    Task<DataSet> caloriesResponse = Fitness.getHistoryClient(context, gsa)
                            .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED);
                    caloriesResponse.addOnCompleteListener(totalsListener);
                    Task<DataSet> moveResponse = Fitness.getHistoryClient(context, gsa)
                            .readDailyTotal(DataType.TYPE_MOVE_MINUTES);
                    moveResponse.addOnCompleteListener(totalsListener);
                    Tasks.whenAllComplete(bpmResponse, stepResponse, step2Response, speedResponse, distanceResponse, caloriesResponse, moveResponse)
                            .addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            receiver.send(200, resultBundle);
                        else
                            receiver.send(505, null);
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            receiver.send(505, resultBundle);
                        }
                    });
                    return 1;
                } catch (Exception e) {
                    resultBundle.putInt(KEY_ACTION, mActionKey);
                    receiver.send(505, resultBundle);
                    return 0;
                }
            }

            if (mActionKey == Constants.TASK_ACTION_READ_GOALS){
                GoogleSignInOptionsExtension fitnessOptions =
                        FitnessOptions.builder()
                                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                                .addDataType(DataType.TYPE_CALORIES_EXPENDED,FitnessOptions.ACCESS_READ)
                                .addDataType(DataType.TYPE_HEART_POINTS, FitnessOptions.ACCESS_READ)
                                .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
                                .build();

                GoogleSignInAccount googleSignInAccount =
                        GoogleSignIn.getAccountForExtension(context, fitnessOptions);
                GoalsReadRequest goalsReadRequest = new GoalsReadRequest.Builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA)
                        .addDataType(DataType.TYPE_CALORIES_EXPENDED)
                        .addDataType(DataType.TYPE_HEART_POINTS)
                        .addDataType(DataType.TYPE_MOVE_MINUTES).build();
                        //.addActivity(FitnessActivities.STRENGTH_TRAINING)
                       // .addObjectiveType(Goal.OBJECTIVE_TYPE_FREQUENCY).build();
                try {

                    Task<List<Goal>> response = Fitness.getGoalsClient(context, googleSignInAccount).readCurrentGoals(goalsReadRequest);
                    List<Goal> goals = Tasks.await(response);
                    int index = 0;
                    resultBundle.putInt(Constants.KEY_FIT_ACTION, mActionKey);
                    for (Goal goal : goals){
                        int goalType = goal.getObjectiveType();
                        resultBundle.putInt(Constants.KEY_FIT_TYPE + index, goalType);
                        String sName = (goal.getActivityName() != null) ? goal.getActivityName() : Constants.ATRACKIT_EMPTY;
                        resultBundle.putString(Constants.KEY_FIT_NAME + index, sName);
                        String sMetricName=Constants.ATRACKIT_EMPTY;
                        String sValue=Constants.ATRACKIT_EMPTY;
                        if (goalType == Goal.OBJECTIVE_TYPE_METRIC){
                           double dVal = goal.getMetricObjective().getValue();
                           sValue = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, dVal);
                           sMetricName = goal.getMetricObjective().getDataTypeName();
                        }
                        if (goalType == Goal.OBJECTIVE_TYPE_DURATION){
                            sValue = String.format(Locale.getDefault(), "%d", goal.getDurationObjective().getDuration(TimeUnit.MILLISECONDS));
                            sMetricName = "duration";
                        }
                        if (goalType == Goal.OBJECTIVE_TYPE_FREQUENCY){
                            sValue = String.format(Locale.getDefault(),"%d", goal.getFrequencyObjective().getFrequency());
                            sMetricName = "frequency";
                        }
                        if (sValue.length() > 0){
                            resultBundle.putString(Constants.KEY_FIT_VALUE+ index, sValue);
                            resultBundle.putString(Constants.KEY_FIT_REC + index, sMetricName);
                        }
                        index +=1;
                    }
                    receiver.send(200, resultBundle);
                }catch(Exception e){
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                    receiver.send(505, null);
                    return 0;
                }
            }
        }
        return mActionKey;
    }

    /**
     * GET total estimated STEP_COUNT
     *
     * Retrieves an estimated step count.
     *
     */
    private DataReadRequest queryStepEstimate(long startTime, long endTime) {
        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();

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
        for (int i=0; i< types.size(); i++) {
            builder.aggregate(types.get(i), aggregations.get(i));
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
    private Bundle datapointToResultBundle(DataPoint dp){
        Bundle resultBundle = new Bundle();
        if (dp.getDataType().getName().equals(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())) {
            Workout workout = new Workout();
            workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
            workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
            workout.setCount = dp.getValue(Field.FIELD_NUM_SEGMENTS).asInt();
            workout.duration = (long)dp.getValue(Field.FIELD_DURATION).asInt();
           // Log.d(LOG_TAG, "activity.summary " + workout.activityName + " " + workout.setCount + " duration " + workout.duration);
            resultBundle.putParcelable(Workout.class.getSimpleName(), workout);
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

            set._id = (set.start);
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
            Log.d(LOG_TAG, "dp " + referencesTools.workoutSetShortText(set));
            resultBundle.putParcelable(WorkoutSet.class.getSimpleName(), set);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
            Workout workout = new Workout();
            workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
            workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
            workout.identifier = referencesTools.getFitnessActivityIdentifierById(workout.activityID);
//            if (dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SAMPLE.getName())) {
//                float confidence = dp.getValue(Field.FIELD_ACTIVITY_CONFIDENCE).asFloat();
//                workout.wattsTotal = confidence;
//                Log.d(LOG_TAG, "dp activity sample " + workout.activityID + " " + workout.activityName + " conf " + confidence);
//            } else
                Log.d(LOG_TAG, "dp activity sample " + workout.activityID + " " + workout.activityName);
            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            if (startTime == 0)
                startTime = System.currentTimeMillis();
            workout.start = startTime;
            workout._id = (workout.start);
            long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
            if (endTime > 0 ) workout.end = endTime;
            workout.duration = workout.end - workout.start;
            workout.packageName = dp.getOriginalDataSource().getAppPackageName();
            resultBundle.putParcelable(Workout.class.getSimpleName(), workout);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
            float watts = dp.getValue(Field.FIELD_WATTS).asFloat();
            if (watts > 0F) {
                Workout workout = new Workout();
                workout.activityName = "Power Sample";
                workout.wattsTotal = watts;
                Log.d(LOG_TAG, "dp power sample " + watts);
                resultBundle.putParcelable(Constants.MAP_WATTS, workout);
            }
        } else if (dp.getDataType().getName().equals(DataType.TYPE_MOVE_MINUTES.getName())) {
            Integer minutes = ( dp.getValue(Field.FIELD_DURATION) != null) ? dp.getValue(Field.FIELD_DURATION).asInt() : 0;
            if (minutes > 0) resultBundle.putInt(Constants.MAP_MOVE_MINS, minutes);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_CALORIES_EXPENDED.getName())) {
            float calories = dp.getValue(Field.FIELD_CALORIES).asFloat();
            resultBundle.putFloat(Constants.MAP_CALORIES, calories);
        } else if (dp.getDataType().getName().contains(DataType.TYPE_DISTANCE_DELTA.getName())) {
            float distance = dp.getValue(Field.FIELD_DISTANCE).asFloat();
            if (distance > 0F) resultBundle.putFloat(Constants.MAP_DISTANCE, distance);
        } else if (dp.getDataType().getName().contains("step_count")) {
            int steps = dp.getValue(Field.FIELD_STEPS).asInt();
            if (steps > 0) resultBundle.putInt(Constants.MAP_STEPS, steps);
        }else if (dp.getDataType().getName().equals(DataType.AGGREGATE_HEART_RATE_SUMMARY.getName())) {
            float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
            float min = dp.getValue(Field.FIELD_MIN).asFloat();
            float max = dp.getValue(Field.FIELD_MAX).asFloat();
            if (avg > 0F) resultBundle.putFloat(Constants.MAP_BPM_AVG, avg);
            if (min > 0F) resultBundle.putFloat(Constants.MAP_BPM_MIN, min);
            if (max > 0F) resultBundle.putFloat(Constants.MAP_BPM_MAX, max);
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
}
