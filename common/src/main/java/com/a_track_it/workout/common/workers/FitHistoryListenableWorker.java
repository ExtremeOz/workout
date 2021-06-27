package com.a_track_it.workout.common.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.ReferencesTools;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.Workout;
import com.a_track_it.workout.common.data_model.WorkoutSet;
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
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

public class FitHistoryListenableWorker extends ListenableWorker {
    private static final String LOG_TAG = "FitHistoryListenableWorker";
    public static final String ARG_ACTION_KEY = "action-key";
    public static final String ARG_START_KEY = "start-key";
    public static final String ARG_END_KEY = "end-key";
    public static final int BUCKET_TYPE_TIME = 0;
    public static final int BUCKET_TYPE_ACTIVITY = 1;
    public static final int BUCKET_TYPE_SEGMENT = 2;
    public static final int BUCKET_TYPE_SESSION = 3;
    private int mActionKey;
    private long mStart;
    private long mEnd;
    private long mId;
    private Executor executor;
    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public FitHistoryListenableWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        Data inputs = workerParams.getInputData();
        mActionKey = inputs.getInt(ARG_ACTION_KEY,0);
        mStart = inputs.getLong(ARG_START_KEY, 0);
        mEnd = inputs.getLong(ARG_END_KEY, 0);
        executor = Executors.newSingleThreadExecutor();
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        ArrayList<Workout> workoutArrayList = new ArrayList<>();
        ArrayList<WorkoutSet> workoutSetArrayList = new ArrayList<>();
        return CallbackToFutureAdapter.getFuture(completer -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.TIME_DATE_FORMAT, Locale.getDefault());
            if (mEnd > 0) cal.setTimeInMillis(mEnd);
            long endTime = cal.getTimeInMillis();
            if (mStart == 0) {
                cal.set(Calendar.DAY_OF_YEAR, -1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 1);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            }else{
                cal.setTimeInMillis(mStart);
                Log.d(LOG_TAG, "mstart set " + mStart);
            }
            long startTime = cal.getTimeInMillis();
            boolean bOutcome = false;
            Context context = getApplicationContext();
            ReferencesTools referencesTools = ReferencesTools.getInstance() ;
            referencesTools.init(context);
            DataReadRequest.Builder builder = new DataReadRequest.Builder();
            Device device = Device.getLocalDevice(context);

            if (mActionKey == 0) {
                cal.setTime(new Date());
                endTime = cal.getTimeInMillis();
                cal.set(Calendar.DAY_OF_YEAR, -7);
                startTime = cal.getTimeInMillis();
                builder.aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .enableServerQueries();// Used to retrieve data from cloud.
                bOutcome = true;
            }

            if (mActionKey == 1){
                builder.read(DataType.TYPE_ACTIVITY_SEGMENT)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .enableServerQueries() // Used to retrieve data from cloud.
                        .build();

                bOutcome = true;
            }
            if (mActionKey == 2){
                List<DataType> dt = Arrays.asList(DataType.TYPE_ACTIVITY_SEGMENT);
                List<DataType> ag = Arrays.asList(DataType.AGGREGATE_ACTIVITY_SUMMARY);
                builder = queryDataWithBuckets(startTime, endTime, dt, ag,1,TimeUnit.DAYS,BUCKET_TYPE_ACTIVITY);
                builder.enableServerQueries() ;// Used to retrieve data from cloud.
                bOutcome = true;
            }
            if (mActionKey == 3){
                List<DataType> dt = Arrays.asList(DataType.TYPE_HEART_RATE_BPM);
                List<DataType> ag = Arrays.asList(DataType.AGGREGATE_HEART_RATE_SUMMARY);
                builder = queryDataWithBuckets(startTime, endTime, dt, ag,1,TimeUnit.DAYS,BUCKET_TYPE_TIME);
                builder.enableServerQueries() ;// Used to retrieve data from cloud.
                bOutcome = true;
            }
            if (mActionKey == 4){
                cal.setTime(new Date());
                endTime = cal.getTimeInMillis();
                cal.set(Calendar.DAY_OF_YEAR, -7);
                startTime = cal.getTimeInMillis();
                builder.aggregate(DataType.TYPE_DISTANCE_DELTA)
                        .aggregate(DataType.TYPE_ACTIVITY_SEGMENT)
                        .aggregate(DataType.TYPE_MOVE_MINUTES)
                        .aggregate(DataType.TYPE_HEART_POINTS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .enableServerQueries();// Used to retrieve data from cloud.
                bOutcome = true;
            }
            if (!bOutcome){
                completer.set(Result.failure());
                return "failed";
            }
            OnCompleteListener<DataReadResponse> completeListener = new OnCompleteListener<DataReadResponse>() {
                @Override
                public void onComplete(@NonNull Task<DataReadResponse> task) {
                    if (task.getResult().getStatus().isSuccess()) {
                        int iRetVal = 0;
                        if (task.getResult().getBuckets().size() > 0) {
                            iRetVal = task.getResult().getBuckets().size();
                            //   Log.d(LOG_TAG, "received bucket size " + iRetVal);
                            for (Bucket bucket : task.getResult().getBuckets()) {
                                List<DataSet> dataSets = bucket.getDataSets();
                                for (DataSet dataSet : dataSets) {
                                    //dumpDataSet(dataSet);
                                    List<DataPoint> dpList = dataSet.getDataPoints();
                                    for (DataPoint dp : dpList) {
                                        if (dp.getDataType().getName().equals(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())) {
                                            Workout workout = new Workout();
                                            workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
                                            if (Utilities.isGymWorkout(workout.activityID)) {
                                                workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
                                                workout.setCount = dp.getValue(Field.FIELD_NUM_SEGMENTS).asInt();
                                                workout.duration = (long) dp.getValue(Field.FIELD_DURATION).asInt();
                                                Log.d(LOG_TAG, "activity.summary " + workout.activityName + " " + workout.setCount + " duration " + workout.duration);
                                                workoutArrayList.add(workout);
                                            }
                                        }else
                                        if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
                                            Workout workout = new Workout();
                                            workout.wattsTotal = dp.getValue(Field.FIELD_WATTS).asFloat();
                                            Log.d(LOG_TAG, "power.sample " + workout.activityName + " " + workout.setCount);
                                            workoutArrayList.add(workout);
                                        }else
                                        if (dp.getDataType().getName().equals(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
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
                                            if (dp.getValue(Field.FIELD_DURATION).isSet())
                                                set.duration = (long) dp.getValue(Field.FIELD_DURATION).asInt();
                                            String sExercise = dp.getValue(Field.FIELD_EXERCISE).asString();
                                            long iExerciseID = referencesTools.getFitnessActivityIdByText(sExercise);
                                            if (iExerciseID > 0)
                                                set.exerciseID = iExerciseID; // if it fits our list
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
                                            workoutSetArrayList.add(set);
                                        }else
                                        if (dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
                                            Workout workout = new Workout();
                                            workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
                                            boolean bIsDetected = Utilities.isDetectedActivity(workout.activityID);
                                            if (!bIsDetected){
                                                workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
//                                            if (dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SAMPLE.getName())) {
//                                                float confidence = dp.getValue(Field.FIELD_ACTIVITY_CONFIDENCE).asFloat();
//                                                workout.wattsTotal = confidence;
//                                                Log.d(LOG_TAG, "dp activity sample " + workout.activityID + " " + workout.activityName + " conf " + confidence + " " + bIsDetected);
//                                            } else {
                                                if (!bIsDetected)
                                                Log.d(LOG_TAG, "dp activity segment " + workout.activityID + " " + workout.activityName + " " + workout.packageName);

                                            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                                            if (startTime == 0)
                                                startTime = System.currentTimeMillis();
                                            workout.start = startTime;
                                            workout._id = workout.start + workout.activityID;
                                            long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
                                            if (endTime > 0 ) workout.end = endTime;
                                            workout.duration = workout.end - workout.start;
                                            workout.packageName = dp.getOriginalDataSource().getAppPackageName();

                                            if (Utilities.isGymWorkout(workout.activityID)) workoutArrayList.add(workout);
                                            }
                                        }else
                                            if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
                                                float watts = dp.getValue(Field.FIELD_WATTS).asFloat();
                                                Workout workout = new Workout();
                                                workout.activityName = "Power Sample";
                                                workout.wattsTotal = watts;
                                                Log.d(LOG_TAG, "dp power sample " + watts);
                                                workoutArrayList.add(workout);
                                            }
                                    }
                                }
                            }
                        } else {
                            Log.d(LOG_TAG, "Successful fit read  " + task.getResult().getDataSets().size());
                            iRetVal = task.getResult().getDataSets().size();
                            for (DataSet dataSet : task.getResult().getDataSets()) {
                                //wroteDataToCache = wroteDataToCache || writeDataSetToCache(dataSet);
                                List<DataPoint> dpList = dataSet.getDataPoints();
                                for (DataPoint dp : dpList) {
                                    if (dp.getDataType().getName().equals(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
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
                                        if (dp.getValue(Field.FIELD_DURATION).isSet())
                                            set.duration = (long) dp.getValue(Field.FIELD_DURATION).asInt();
                                        if (mId > 0) set.workoutID = mId;
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
                                        workoutSetArrayList.add(set);
                                    }
                                    if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
                                        Workout workout = new Workout();
                                        workout.wattsTotal = dp.getValue(Field.FIELD_WATTS).asFloat();
                                        Log.d(LOG_TAG, "power.sample " + workout.activityName + " " + workout.setCount);
                                        workoutArrayList.add(workout);
                                    }
                                    if (dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
                                        Workout workout = new Workout();
                                        workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
                                        boolean bIsDetected = Utilities.isDetectedActivity(workout.activityID);
                                        workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
                                        if (!bIsDetected) {
                                            Log.d(LOG_TAG, "dp activity segment " + workout.activityID + " " + workout.activityName + " " + workout.packageName);

                                            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
                                            if (startTime == 0)
                                                startTime = System.currentTimeMillis();
                                            workout.start = startTime;
                                            workout._id = workout.start + workout.activityID;
                                            long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
                                            if (endTime > 0) workout.end = endTime;
                                            workout.duration = workout.end - workout.start;
                                            workout.packageName = dp.getOriginalDataSource().getAppPackageName();
                                            if (Utilities.isGymWorkout(workout.activityID)) {
                                                workoutArrayList.add(workout);
                                            }
                                        }
                                    }
                                    if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
                                        float watts = dp.getValue(Field.FIELD_WATTS).asFloat();
                                        Workout workout = new Workout();
                                        workout.activityName = "Power Sample";
                                        workout.wattsTotal = watts;
                                        Log.d(LOG_TAG, "dp power sample " + watts);
                                        workoutArrayList.add(workout);
                                    }
                                }
                            }
                        }
                    } else {
                        Log.d(LOG_TAG, "task read not successful " + task.getResult().toString());
                        completer.set(Result.failure());
                    }

                }
            };

            try {
                GoogleSignInAccount gsa =  GoogleSignIn.getAccountForExtension(context, referencesTools.getFitnessSignInOptions(0));
                if (gsa != null) {
                    if (mActionKey == 1){
                        SessionReadRequest sessionReadRequest = new SessionReadRequest.Builder().read(DataType.TYPE_ACTIVITY_SEGMENT)
                                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS).enableServerQueries().build();
                        Fitness.getSessionsClient(context, gsa).readSession(sessionReadRequest).addOnCompleteListener(new OnCompleteListener<SessionReadResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<SessionReadResponse> task) {
                                if (task.isSuccessful()){
                                    DateFormat dateFormat = getTimeInstance();
                                    List<Session> sessions =task.getResult().getSessions();
                                    if (sessions != null)
                                    for (Session session : sessions) {
                                        Workout w = new Workout();
                                        w.name = session.getName();
                                        w.packageName = session.getAppPackageName();
                                        w.activityName = session.getActivity();
                                        w.start = session.getStartTime(TimeUnit.MILLISECONDS);
                                        w.end = session.getEndTime(TimeUnit.MILLISECONDS);
                                        w._id = w.start;
                                        w.identifier = session.getIdentifier();
                                        if (w.activityName.equals("strength_training") || w.activityName.equals("circuit_training") || w.activityName.equals("crossfit"))
                                            workoutArrayList.add(w);
                                    }
                                    int iRetVal = 0;
                                    if (workoutArrayList.size() > 0){
                                        for (Workout workout : workoutArrayList){
                                            try {
                                                executor.execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        SessionReadRequest.Builder readRequestEx = new SessionReadRequest.Builder();
                                                        Log.d(LOG_TAG, "reading " + " " + workout.name + " " + dateFormat.format(workout.start) + " to " + dateFormat.format(workout.end) + " : " + workout.packageName + " : " + workout.identifier);
                                                        DataSource dataSource = new DataSource.Builder()
                                                                .setAppPackageName(workout.packageName)
                                                                .setDevice(device)
                                                                .setDataType(DataType.TYPE_WORKOUT_EXERCISE)
                                                                .setType(DataSource.TYPE_RAW)
                                                                .build();
                                                        if (workout.identifier.length() > 0)
                                                            readRequestEx.setTimeInterval(workout.start, workout.end, TimeUnit.MILLISECONDS)
                                                                    .read(dataSource).setSessionId(workout.identifier)
                                                                    .enableServerQueries()
                                                                    .readSessionsFromAllApps();
                                                        else
                                                            readRequestEx.setTimeInterval(workout.start, workout.end, TimeUnit.MILLISECONDS)
                                                                    .read(dataSource)
                                                                    .enableServerQueries()
                                                                    .readSessionsFromAllApps();
                                                        GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(context, referencesTools.getFitnessSignInOptions(0));
                                                        Fitness.getSessionsClient(context, gsa)
                                                                .readSession(readRequestEx.build()).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.e(LOG_TAG, "failed set read " + e.getLocalizedMessage());
                                                            }
                                                        })
                                                                .addOnSuccessListener(new OnSuccessListener<SessionReadResponse>() {
                                                                    @Override
                                                                    public void onSuccess(SessionReadResponse sessionReadResponse) {
                                                                        List<Session> sessions = sessionReadResponse.getSessions();
                                                                        for (Session session : sessions) {
                                                                            // Process the data sets for this workout
                                                                            List<DataSet> dataSets = sessionReadResponse.getDataSet(session);
                                                                            for (DataSet dataSet : dataSets) {
                                                                                List<DataPoint> dpList = dataSet.getDataPoints();
                                                                                for (DataPoint dp : dpList) {
                                                                                    if (dp.getDataType().getName().equals(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
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

                                                                                        if (set.start > 0)
                                                                                            set._id = set.start;
                                                                                        else
                                                                                            set._id = System.currentTimeMillis();
                                                                                        set.workoutID = 1L;
                                                                                        if (dp.getValue(Field.FIELD_DURATION).isSet())
                                                                                            set.duration = (long) dp.getValue(Field.FIELD_DURATION).asInt();
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
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                                iRetVal += 1;
                                            }catch (Exception e){
                                                Log.e(LOG_TAG,"READ SET " + e.getLocalizedMessage());
                                            }
                                        }
                                    }
                                    // If there were no errors, return SUCCESS
                                    Data outputData = new Data.Builder()
                                            .putInt(ARG_ACTION_KEY, iRetVal)
                                            .build();
                                    completer.set(Result.success(outputData));
                                }else
                                    completer.set(Result.failure());
                            }
                        });

                    }else{
                        Task<DataReadResponse> response = Fitness.getHistoryClient(context, gsa)
                                .readData(builder.build());
                        response.addOnCompleteListener(completeListener).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(LOG_TAG, "task read not successful " + e.getMessage());
                                completer.set(Result.failure());
                            }
                        });
                    }


                }else
                    completer.set(Result.failure());
            }catch (Exception e){
                Log.e(LOG_TAG, e.getMessage());
                completer.set(Result.failure());
            }
            return "listener";
        });
    }
    /**
     * Return a {@link DataReadRequest} for all step count changes in the past week.
     */
    private DataReadRequest.Builder queryDataWithBuckets(long startTime, long endTime,
                                                         List<DataType> types, List<DataType> aggregations,
                                                         int duration, TimeUnit time, int bucketType) {

        DataReadRequest.Builder builder = new DataReadRequest.Builder();

        for (int i=0; i< types.size(); i++) {
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
}
