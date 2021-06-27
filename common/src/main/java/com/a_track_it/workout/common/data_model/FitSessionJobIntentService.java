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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.workout.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.workout.common.Constants.KEY_FIT_ACTION;
import static com.a_track_it.workout.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_WORKOUTID;
import static com.a_track_it.workout.common.Constants.SESSION_PREFIX;
import static java.text.DateFormat.getDateTimeInstance;
import static java.text.DateFormat.getTimeInstance;

public class FitSessionJobIntentService extends JobIntentService {
    private static final String LOG_TAG = FitSessionJobIntentService.class.getSimpleName();
    public static final String ARG_RESULT_KEY = "result-key";
    public static final String ARG_ACTION_KEY = "action-key";
    public static final String ARG_START_KEY = "start-key";
    public static final String ARG_WORKOUT_KEY = "workout-key";
    public static final String ARG_SET_KEY = "set-key";
    public static final String ARG_END_KEY = "end-key";
    private String sUserID;
    private String sDeviceID;
    private Workout mWorkout;
    private WorkoutSet mWorkoutSet;
    private boolean action_success = false;
    private int session_action = 0;
    private int totalFinal = 0;
    private ReferencesTools referencesTools;
    private Calendar calendar;
    private int mInterval;
    private static final int JOB_ID = 5;
    private ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
        }
    };
    public FitSessionJobIntentService(){
        super();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        calendar = Calendar.getInstance();
        referencesTools = ReferencesTools.setInstance(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, FitSessionJobIntentService.class, JOB_ID, intent);
    }
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Context context = getApplicationContext();
        session_action = intent.getIntExtra(ARG_ACTION_KEY,0);
        sUserID = intent.getStringExtra(Constants.KEY_FIT_USER);
        sDeviceID = intent.getStringExtra(Constants.KEY_FIT_DEVICE_ID);
        mWorkout = intent.getParcelableExtra(Workout.class.getSimpleName());
        mInterval = (intent.hasExtra(Constants.KEY_FIT_VALUE) ? intent.getIntExtra(Constants.KEY_FIT_VALUE, 60) : 60);
        if (intent.hasExtra(WorkoutSet.class.getSimpleName())) mWorkoutSet = intent.getParcelableExtra(WorkoutSet.class.getSimpleName());
        ResultReceiver resultReceiver = intent.getParcelableExtra(ARG_RESULT_KEY);

        boolean isInternal = true;
        if (resultReceiver != null) {
            mResultReceiver = resultReceiver;
            isInternal = false;
        }
        FitnessOptions fo;
        if (session_action == Constants.TASK_ACTION_READ_BPM){
            fo = referencesTools.getFitnessSignInOptions(2);
        }else
            fo = referencesTools.getFitnessSignInOptions(1);

        GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(context, fo);
        if ((gsa != null) && (mWorkout != null)){
            if (this.session_action == Constants.TASK_ACTION_START_SESSION)
                startLiveFitSession(gsa, context);
            if (this.session_action == Constants.TASK_ACTION_STOP_SESSION)
                stopLiveFitSession(gsa, context);
            if (this.session_action == Constants.TASK_ACTION_READ_HISTORY)
                readFitHistory(this.mWorkout, gsa, context);
            if (this.session_action == Constants.TASK_ACTION_EXER_SEGMENT)
                insertExercise(gsa, context);
            if (this.session_action == Constants.TASK_ACTION_ACT_SEGMENT)
                insertSegment(gsa, context, mWorkoutSet.activityName);
            if (this.session_action == Constants.TASK_ACTION_INSERT_HISTORY)  // History workout insert
                insertFitHistory(this.mWorkout,gsa,context);
            if (this.session_action == Constants.TASK_ACTION_READ_SESSION)
                readFitSession(this.mWorkout,gsa,context);
            if (this.session_action == Constants.TASK_ACTION_READ_BPM)
                readBPMDaily(this.mWorkout,gsa,context);
        }else
            mResultReceiver.send(550,null);
    }

    private long getDayStart(long inTime){
        return Utilities.getDayStart(calendar, inTime);
    }
    private void insertExercise(GoogleSignInAccount googleSignInAccount, Context context){
        if (mWorkoutSet.end == 0) mWorkoutSet.end =  System.currentTimeMillis();
        if (!mWorkoutSet.isValid(true)) return;
        if ((mWorkoutSet.repCount == null) || (mWorkoutSet.resistance_type == null) || (mWorkoutSet.weightTotal == null)) return;
        final Bundle resultBundle = new Bundle();
        resultBundle.putInt(KEY_FIT_ACTION, Constants.TASK_ACTION_EXER_SEGMENT);
        try {
            //String packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
            Device device = Device.getLocalDevice(context);
            DataSet dataSet = Utilities.createExerciseDataSet(mWorkoutSet.start, mWorkoutSet.end, mWorkoutSet.exerciseName,
                    mWorkoutSet.repCount, mWorkoutSet.resistance_type, mWorkoutSet.weightTotal, mWorkoutSet.score_card,device);
            if (dataSet != null) {
                Fitness.getHistoryClient(context, googleSignInAccount).insertData(dataSet).addOnCompleteListener(task -> {
                    this.action_success = task.isSuccessful();
                    if (this.action_success) {
                        long timeMs = System.currentTimeMillis();
                        mWorkoutSet.last_sync = timeMs;
                        resultBundle.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
                        mResultReceiver.send(200, resultBundle);
                    } else {
                        mResultReceiver.send(505, resultBundle);
                    }
                });
            }else
                mResultReceiver.send(401, resultBundle);
        }catch (Exception e){
            Log.e(LOG_TAG, "insertExercise error " + e.getMessage());
            mResultReceiver.send(402, resultBundle);
        }
    }
    private void insertSegment(GoogleSignInAccount googleSignInAccount, Context context, String segmentName){
        if (mWorkoutSet.end == 0) mWorkoutSet.end =  System.currentTimeMillis();
        if (!mWorkoutSet.isValid(true)) return;
        final Bundle resultBundle = new Bundle();
        resultBundle.putInt(KEY_FIT_ACTION, Constants.TASK_ACTION_ACT_SEGMENT);

        try {
            Log.w(LOG_TAG, "SESSION insertSegment " + this.mWorkout.activityName + " " + Utilities.getDateString(this.mWorkout.start));
            Log.w(LOG_TAG, "mWorkout \n" +  mWorkout.toString());
            Log.w(LOG_TAG, "mWorkoutSet \n" +  mWorkoutSet.toString());

            Device device = Device.getLocalDevice(context);
            DataSet dataSet = createActivityDataSet(mWorkoutSet.start, mWorkoutSet.end, mWorkoutSet.activityName, device);
            if (dataSet != null) {
                Fitness.getHistoryClient(context, googleSignInAccount).insertData(dataSet).addOnCompleteListener(task -> {
                    this.action_success = task.isSuccessful();
                    if (this.action_success) {
                        long timeMs = System.currentTimeMillis();
                        mWorkoutSet.last_sync = timeMs;
                        resultBundle.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
                        mResultReceiver.send(200, resultBundle);
                    } else {
                        mResultReceiver.send(505, resultBundle);
                    }
                });
            }else
                mResultReceiver.send(401, resultBundle);
        }catch (Exception e){
            Log.e(LOG_TAG, "insertSegment error " + e.getMessage());
            mResultReceiver.send(402, resultBundle);
        }

    }
    private void startLiveFitSession(GoogleSignInAccount googleSignInAccount, Context context){
        String sDesc =  referencesTools.workoutShortText(this.mWorkout);
        String sTimeOfDayName = Utilities.getPartOfDayString(this.mWorkout.start) + Constants.ATRACKIT_SPACE + this.mWorkout.activityName;

        String sID = Long.toString(this.mWorkout._id);
        String mSessionID = SESSION_PREFIX + sID;
        final String finalID = mSessionID;
        Log.w(LOG_TAG, "SESSION Start API for " + this.mWorkout.activityName + " " + Utilities.getDateString(this.mWorkout.start));
        Log.w(LOG_TAG, "mWorkout \n" +  mWorkout.toString());
        Log.w(LOG_TAG, "mWorkoutSet \n" +  mWorkoutSet.toString());
        final Bundle resultBundle = new Bundle();
        resultBundle.putInt(KEY_FIT_ACTION, Constants.TASK_ACTION_START_SESSION);
        try {
            Session session = new Session.Builder().setName(sTimeOfDayName)
                    .setIdentifier(mSessionID)
                    .setStartTime(this.mWorkout.start, TimeUnit.MILLISECONDS)
                    .setActivity(this.mWorkout.identifier).setDescription(sDesc).build();
            Task<Void> response = Fitness.getSessionsClient(context, googleSignInAccount).startSession(session);
            response.addOnCompleteListener(task -> {
                this.action_success = task.isSuccessful();
                if (this.action_success) {
                    long timeMs = System.currentTimeMillis();
                    mWorkout.last_sync = timeMs;
                    resultBundle.putParcelable(Workout.class.getSimpleName(), mWorkout);
                    mResultReceiver.send(200, resultBundle);
                } else {
                    resultBundle.putLong(KEY_FIT_WORKOUTID, mWorkout._id);
                    resultBundle.putParcelable(Workout.class.getSimpleName(), session);
                    mResultReceiver.send(505, resultBundle);
                }
            });
        }catch (Exception e){
            mResultReceiver.send(402,resultBundle);
        }
    }
    private void stopLiveFitSession(GoogleSignInAccount mGoogleAccount, Context context){
        String sID = Long.toString(mWorkout._id);
        String mSessionID = SESSION_PREFIX + sID;
        final String finalID = mSessionID;

        Log.w(LOG_TAG, "mWorkout \n" +  mWorkout.toString());
        Log.w(LOG_TAG, "mWorkoutSet \n" +  mWorkoutSet.toString());
        Log.w(LOG_TAG, "SESSION API STOP for " + mSessionID + " " + mWorkout.activityName + " " + Utilities.getDateString(mWorkout.start));
        if (mWorkoutSet.end == 0) mWorkoutSet.end =  System.currentTimeMillis();
        final Bundle resultBundle = new Bundle();
        resultBundle.putInt(KEY_FIT_ACTION, Constants.TASK_ACTION_STOP_SESSION);

        try {
            Device device = Device.getLocalDevice(context);
            Task<List<Session>> stopTask = Fitness.getSessionsClient(context, mGoogleAccount)
                    .stopSession(mSessionID);
            if (Utilities.isGymWorkout(mWorkout.activityID)) {
                if (mWorkoutSet != null) {
                    DataSet dataSetEx = Utilities.createExerciseDataSet(mWorkoutSet.start, mWorkoutSet.end, ((mWorkoutSet.per_end_xy.length() > 0) ? mWorkoutSet.per_end_xy : mWorkoutSet.exerciseName),
                            mWorkoutSet.repCount, mWorkoutSet.resistance_type, mWorkoutSet.weightTotal, mWorkoutSet.score_card, device);
                    if (dataSetEx != null) {
                        Task<Void> segTask = Fitness.getHistoryClient(context, mGoogleAccount).insertData(dataSetEx);
                        Tasks.whenAll(segTask, stopTask).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                long timeMs = System.currentTimeMillis();
                                mWorkout.last_sync = timeMs;
                                resultBundle.putParcelable(Workout.class.getSimpleName(), mWorkoutSet);
                                mWorkoutSet.last_sync = timeMs;
                                resultBundle.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
                                mResultReceiver.send(200, resultBundle);
                                Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                                msgIntent.setAction(INTENT_MESSAGE_TOAST);
                                msgIntent.putExtra(KEY_FIT_TYPE, 2);
                                msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                                msgIntent.putExtra(Constants.INTENT_EXTRA_MSG, mWorkout.activityName + " completed successfully");
                            } else
                                mResultReceiver.send(505, null);

                        });
                    }else
                        Tasks.whenAllComplete(stopTask).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                long timeMs = System.currentTimeMillis();
                                mWorkout.last_sync = timeMs;
                                resultBundle.putParcelable(Workout.class.getSimpleName(), mWorkoutSet);
                                mResultReceiver.send(200, resultBundle);
                                Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                                msgIntent.setAction(INTENT_MESSAGE_TOAST);
                                msgIntent.putExtra(KEY_FIT_TYPE, 2);
                                msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                                msgIntent.putExtra(Constants.INTENT_EXTRA_MSG, mWorkout.activityName + " completed successfully");
                            } else
                                mResultReceiver.send(505, null);

                        });
                }else{
                    Tasks.whenAllComplete(stopTask).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            long timeMs = System.currentTimeMillis();
                            mWorkout.last_sync = timeMs;
                            resultBundle.putParcelable(Workout.class.getSimpleName(), mWorkoutSet);
                            mResultReceiver.send(200, resultBundle);
                            Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                            msgIntent.setAction(INTENT_MESSAGE_TOAST);
                            msgIntent.putExtra(KEY_FIT_TYPE, 2);
                            msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                            msgIntent.putExtra(Constants.INTENT_EXTRA_MSG, mWorkout.activityName + " completed successfully");
                        } else
                            mResultReceiver.send(505, null);

                    });
                }
            } else {
                if (mWorkoutSet != null) {
                    DataSet dataSet = createActivityDataSet(mWorkoutSet.start, mWorkoutSet.end, mWorkoutSet.activityName, device);
                    if (dataSet != null) {
                        Task<Void> segTask = Fitness.getHistoryClient(context, mGoogleAccount).insertData(dataSet);
                        Tasks.whenAll(segTask, stopTask).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                long timeMs = System.currentTimeMillis();
                                mWorkout.last_sync = timeMs;
                                resultBundle.putParcelable(Workout.class.getSimpleName(), mWorkout);
                                mWorkoutSet.last_sync = timeMs;
                                resultBundle.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
                                mResultReceiver.send(200, resultBundle);
                                Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                                msgIntent.setAction(INTENT_MESSAGE_TOAST);
                                msgIntent.putExtra(KEY_FIT_TYPE, 2);
                                msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                                msgIntent.putExtra(Constants.INTENT_EXTRA_MSG, mWorkout.activityName + " completed successfully");
                            } else
                                mResultReceiver.send(505, null);

                        });
                    }else
                        Tasks.whenAllComplete(stopTask).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                long timeMs = System.currentTimeMillis();
                                mWorkout.last_sync = timeMs;
                                resultBundle.putParcelable(Workout.class.getSimpleName(), mWorkoutSet);
                                mResultReceiver.send(200, resultBundle);
                                Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                                msgIntent.setAction(INTENT_MESSAGE_TOAST);
                                msgIntent.putExtra(KEY_FIT_TYPE, 2);
                                msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                                msgIntent.putExtra(Constants.INTENT_EXTRA_MSG, mWorkout.activityName + " completed successfully");
                            } else
                                mResultReceiver.send(505, null);

                        });
                }else
                    Tasks.whenAllComplete(stopTask).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            long timeMs = System.currentTimeMillis();
                            mWorkout.last_sync = timeMs;
                            resultBundle.putParcelable(Workout.class.getSimpleName(), mWorkoutSet);
                            mResultReceiver.send(200, resultBundle);
                            Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                            msgIntent.setAction(INTENT_MESSAGE_TOAST);
                            msgIntent.putExtra(KEY_FIT_TYPE, 2);
                            msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                            msgIntent.putExtra(Constants.INTENT_EXTRA_MSG, mWorkout.activityName + " completed successfully");
                        } else
                            mResultReceiver.send(505, null);

                    });
            }
        }catch (Exception e){
            e.printStackTrace();
            mResultReceiver.send(401, null);
        }
    }
    private void readBPMDaily(Workout workout, GoogleSignInAccount googleSignInAccount, Context context){ // interval in mins
        OnCompleteListener<DataReadResponse> readCompleter = (OnCompleteListener<DataReadResponse>) task -> {
                Bundle resultBundle = new Bundle();
                resultBundle.putInt(Constants.KEY_FIT_VALUE, mInterval);
                resultBundle.putInt(KEY_FIT_ACTION, session_action);
                resultBundle.putParcelable(Workout.class.getSimpleName(), workout);
                try {
                    if (task.getResult().getStatus().isSuccess()) {
                        int iRet = task.getResult().getBuckets().size();
                        if (iRet > 0) {
                            ArrayList<Bundle> arrayBundles = new ArrayList<>();
                            Log.d(LOG_TAG, "received bucket size " + iRet);
                            for (Bucket bucket : task.getResult().getBuckets()) {
                                List<DataSet> dataSets = bucket.getDataSets();
                                UserDailyTotals userDailyTotal = new UserDailyTotals();
                                userDailyTotal.userID = sUserID;
                                for (DataSet dataSet : dataSets) {
                                    List<DataPoint> dpList = dataSet.getDataPoints();
                                    if (dpList.size() > 0) iRet++;
                                    for (DataPoint dp : dpList) {
                                        Bundle result = dataPointToResultBundle(dp);
                                        arrayBundles.add(result);
                                    }
                                }
                            }
                            resultBundle.putParcelableArrayList(DataType.AGGREGATE_HEART_RATE_SUMMARY.getName(), arrayBundles);
                        }
                        resultBundle.putInt(Constants.KEY_RESULT, iRet);
                        mResultReceiver.send(200, resultBundle);
                    }
                    else {
                        mResultReceiver.send(505, resultBundle);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    mResultReceiver.send(505, resultBundle);
                }

        };
        DataReadRequest readRequest = new DataReadRequest.Builder()
//                .aggregate(DataType.TYPE_DISTANCE_DELTA)
//                .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(workout.start, workout.end, TimeUnit.MILLISECONDS)
                .bucketByTime(mInterval,TimeUnit.MINUTES)
                .setLimit(1000)
                .enableServerQueries().build();// Used to retrieve data from cloud.
        try {
            Task<DataReadResponse> response = Fitness.getHistoryClient(context, googleSignInAccount).readData(readRequest);
            response.addOnCompleteListener(readCompleter);
            Tasks.await(response, 5, TimeUnit.MINUTES);
            Log.e(LOG_TAG, "readBPMDaily completed ");
        }catch (Exception e){
            Log.e(LOG_TAG, "readBPMDaily Error " + e.getMessage());
        }
    }
    private void readFitSession(Workout mWorkout, GoogleSignInAccount mGoogleAccount, Context context){
        SessionReadRequest readRequest;
        String SessionID = SESSION_PREFIX;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-dd HH:mm", Locale.US);
        Bundle resultBundle = new Bundle();
        resultBundle.putInt(KEY_FIT_ACTION, Constants.TASK_ACTION_READ_SESSION);
        Log.d(LOG_TAG,"start " + simpleDateFormat.format(new Date(mWorkout.start)) + " " + mWorkout.packageName);

        Device device = Device.getLocalDevice(context);
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(mWorkout.packageName)
                .setDevice(device)
                .setDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                .setDataType(DataType.TYPE_WORKOUT_EXERCISE)
                .setType(DataSource.TYPE_RAW)
                .build();
        readRequest= new SessionReadRequest.Builder()
                .setTimeInterval(mWorkout.start, mWorkout.end, TimeUnit.MILLISECONDS)
                .read(dataSource)
                .enableServerQueries()
                .readSessionsFromAllApps()
                .build();

        Task<SessionReadResponse> task =  Fitness.getSessionsClient(context, mGoogleAccount)
                .readSession(readRequest)
                .addOnSuccessListener(sessionReadResponse -> {
                    this.action_success = true;
                    // Get a list of the sessions that match the criteria to check the result.

                    List<Session> sessions = sessionReadResponse.getSessions();
                    Log.d(LOG_TAG, "Session read was successful. Number of returned sessions is: "
                            + sessions.size());

                    for (Session session : sessions) {
                        // Process the mWorkout
                        dumpSession(session);

                        // Process the data sets for this mWorkout
                        List<DataSet> dataSets = sessionReadResponse.getDataSet(session);
                        for (DataSet dataSet : dataSets) {
                            dumpDataSet2(dataSet);
                            // dumpDataSet(resultBundle, dataSet);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        mResultReceiver.send(505, resultBundle);
                        Log.d(LOG_TAG, "Failed to read mWorkout");
                    }
                });
        task.addOnCompleteListener(new OnCompleteListener<SessionReadResponse>() {
            @Override
            public void onComplete(@NonNull Task<SessionReadResponse> task) {
                Log.d(LOG_TAG, "onComplete " + task.isSuccessful());
                if (task.isSuccessful()) {
                    mResultReceiver.send(200, resultBundle);
                }else {
                    mResultReceiver.send(505, resultBundle);
                }
            }
        });
    }
    private void readFitHistory(Workout mWorkout, GoogleSignInAccount mGoogleAccount, Context context){
// Build a mWorkout read request
        List<UserDailyTotals> udtList = new ArrayList<>();
        List<Workout> workoutList = new ArrayList<>();
        List<WorkoutSet> workoutSetList = new ArrayList<>();
        if (mWorkout.end == 0) mWorkout.end = System.currentTimeMillis();
        final long startID = getDayStart(mWorkout.start);
        calendar.setTimeInMillis(startID);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-dd HH:mm:ss z", Locale.getDefault());
        Log.d(LOG_TAG, simpleDateFormat.format(mWorkout.start) + " to " + simpleDateFormat.format(mWorkout.end));
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA)
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_POWER_SAMPLE)
                .aggregate(DataType.TYPE_MOVE_MINUTES)
                .aggregate(DataType.TYPE_HEART_POINTS)
                .aggregate(DataType.TYPE_SPEED)
                .aggregate(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(mWorkout.start, mWorkout.end, TimeUnit.MILLISECONDS)
                .bucketByTime(1,TimeUnit.DAYS)
                .setLimit(1000)
                .enableServerQueries().build();// Used to retrieve data from cloud.
        OnCompleteListener<DataReadResponse> readCompleter = new OnCompleteListener<DataReadResponse>() {
            @Override
            public void onComplete(@NonNull Task<DataReadResponse> task) {
                Bundle resultBundle = new Bundle();
                resultBundle.putInt(KEY_FIT_ACTION, Constants.TASK_ACTION_READ_HISTORY);
                if (task.getResult().getStatus().isSuccess()) {
                    int iRet = task.getResult().getBuckets().size();
                    long idSetter = startID;
                    if (iRet > 0) {
                        Log.d(LOG_TAG, "received bucket size " + iRet);
                        for (Bucket bucket : task.getResult().getBuckets()) {
                            List<DataSet> dataSets = bucket.getDataSets();
                            UserDailyTotals userDailyTotal = new UserDailyTotals();
                            userDailyTotal.userID = sUserID;
                            for (DataSet dataSet : dataSets) {
                                List<DataPoint> dpList = dataSet.getDataPoints();
                                for (DataPoint dp : dpList) {
                                    Bundle result = dataPointToResultBundle(dp);
                                    if (result.containsKey(DataType.AGGREGATE_STEP_COUNT_DELTA.getName())){
                                        Workout w = result.getParcelable(DataType.AGGREGATE_STEP_COUNT_DELTA.getName());
                                        if ((userDailyTotal._id == 0L) && (w.start > 0))
                                            userDailyTotal._id = getDayStart(w.start);
                                        userDailyTotal.stepCount = w.stepCount;
                                    }
                                    if (result.containsKey(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())){
                                        Workout w = result.getParcelable(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName());
                                        if ((userDailyTotal._id == 0L) && (w.start > 0))
                                            userDailyTotal._id = getDayStart(w.start);
                                        w._id = getDayStart(w.start);
                                        workoutList.add(w);
                                    }
                                    if (result.containsKey(DataType.TYPE_WORKOUT_EXERCISE.getName())){
                                        WorkoutSet s = result.getParcelable(DataType.TYPE_WORKOUT_EXERCISE.getName());
                                        if ((userDailyTotal._id == 0L) && (s.start > 0))
                                            userDailyTotal._id = getDayStart(s.start);
                                        s._id = getDayStart(s.start);
                                        workoutSetList.add(s);
                                    }
                                    if (result.containsKey(DataType.TYPE_ACTIVITY_SEGMENT.getName())){
                                        Workout w = result.getParcelable(DataType.TYPE_ACTIVITY_SEGMENT.getName());
                                        if ((userDailyTotal._id == 0L) && (w.start > 0))
                                            userDailyTotal._id = getDayStart(w.start);
                                        w._id = getDayStart(w.start) + w.activityID;
                                        workoutList.add(w);
                                    }
                                    if (result.containsKey(DataType.TYPE_MOVE_MINUTES.getName()))
                                        userDailyTotal.activeMinutes = result.getInt(DataType.TYPE_MOVE_MINUTES.getName());
                                    if (result.containsKey(DataType.TYPE_CALORIES_EXPENDED.getName()))
                                        userDailyTotal.caloriesExpended = result.getFloat(DataType.TYPE_CALORIES_EXPENDED.getName());
                                    if (result.containsKey(DataType.TYPE_DISTANCE_DELTA.getName()))
                                        userDailyTotal.distanceTravelled = result.getFloat(DataType.TYPE_DISTANCE_DELTA.getName());
                                    if (result.containsKey(Constants.MAP_BPM_AVG))
                                        userDailyTotal.avgBPM = result.getFloat(Constants.MAP_BPM_AVG);
                                    if (result.containsKey(Constants.MAP_BPM_MIN))
                                        userDailyTotal.minBPM = result.getFloat(Constants.MAP_BPM_MIN);
                                    if (result.containsKey(Constants.MAP_BPM_MAX))
                                        userDailyTotal.maxBPM = result.getFloat(Constants.MAP_BPM_MAX);
                                    if (result.containsKey(Constants.MAP_SPEED_AVG))
                                        userDailyTotal.avgSpeed = result.getFloat(Constants.MAP_SPEED_AVG);
                                    if (result.containsKey(Constants.MAP_SPEED_MIN))
                                        userDailyTotal.minSpeed = result.getFloat(Constants.MAP_SPEED_MIN);
                                    if (result.containsKey(Constants.MAP_SPEED_MAX))
                                        userDailyTotal.maxSpeed = result.getFloat(Constants.MAP_SPEED_MAX);

                                    if (result.containsKey(DataType.AGGREGATE_HEART_POINTS.getName())){
                                        Workout w = result.getParcelable(DataType.AGGREGATE_HEART_POINTS.getName());
                                        userDailyTotal.heartDuration = w.duration;
                                        userDailyTotal.heartIntensity = w.wattsTotal;
                                    }
                                }
                                if ((idSetter > 0) && (userDailyTotal._id == 0)) userDailyTotal._id = idSetter;
                                udtList.add(userDailyTotal);
                                calendar.add(Calendar.DAY_OF_YEAR, 1);
                                idSetter = calendar.getTimeInMillis();
                            }
                        }
                    } else {
                        iRet = task.getResult().getDataSets().size();
                        Log.d(LOG_TAG, "Successful fit read  " + iRet);
                        if (iRet > 0)
                            for (DataSet dataSet : task.getResult().getDataSets()) {
                                UserDailyTotals userDailyTotal = new UserDailyTotals();
                                userDailyTotal.userID = sUserID;
                                List<DataPoint> dpList = dataSet.getDataPoints();
                                for (DataPoint dp : dpList) {
                                    Bundle result = dataPointToResultBundle(dp);
                                    if (result.containsKey(DataType.AGGREGATE_STEP_COUNT_DELTA.getName())){
                                        Workout w = result.getParcelable(DataType.AGGREGATE_STEP_COUNT_DELTA.getName());
                                        if ((userDailyTotal._id == 0L) && (w.start > 0))
                                            userDailyTotal._id = Utilities.getDayEnd(calendar,w.start);
                                        userDailyTotal.stepCount = w.stepCount;
                                    }
                                    if (result.containsKey(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())){
                                        Workout w = result.getParcelable(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName());
                                        if ((userDailyTotal._id == 0L) && (w.start > 0))
                                            userDailyTotal._id = getDayStart(w.start);
                                        w._id = getDayStart(w.start);
                                        workoutList.add(w);
                                    }
                                    if (result.containsKey(DataType.TYPE_WORKOUT_EXERCISE.getName())){
                                        WorkoutSet s = result.getParcelable(DataType.TYPE_WORKOUT_EXERCISE.getName());
                                        if ((userDailyTotal._id == 0L) && (s.start > 0))
                                            userDailyTotal._id = getDayStart(s.start);
                                        s._id = getDayStart(s.start);
                                        workoutSetList.add(s);
                                    }
                                    if (result.containsKey(DataType.TYPE_ACTIVITY_SEGMENT.getName())){
                                        Workout w = result.getParcelable(DataType.TYPE_ACTIVITY_SEGMENT.getName());
                                        if ((userDailyTotal._id == 0L) && (w.start > 0))
                                            userDailyTotal._id = getDayStart(w.start);
                                        w._id = getDayStart(w.start) + w.activityID;
                                        workoutList.add(w);
                                    }
                                    if (result.containsKey(DataType.TYPE_MOVE_MINUTES.getName()))
                                        userDailyTotal.activeMinutes = result.getInt(DataType.TYPE_MOVE_MINUTES.getName());
                                    if (result.containsKey(DataType.TYPE_CALORIES_EXPENDED.getName()))
                                        userDailyTotal.caloriesExpended = result.getFloat(DataType.TYPE_CALORIES_EXPENDED.getName());
                                    if (result.containsKey(DataType.TYPE_DISTANCE_DELTA.getName()))
                                        userDailyTotal.distanceTravelled = result.getFloat(DataType.TYPE_DISTANCE_DELTA.getName());
                                    if (result.containsKey(Constants.MAP_BPM_AVG))
                                        userDailyTotal.avgBPM = result.getFloat(Constants.MAP_BPM_AVG);
                                    if (result.containsKey(Constants.MAP_BPM_MIN))
                                        userDailyTotal.minBPM = result.getFloat(Constants.MAP_BPM_MIN);
                                    if (result.containsKey(Constants.MAP_BPM_MAX))
                                        userDailyTotal.maxBPM = result.getFloat(Constants.MAP_BPM_MAX);
                                    if (result.containsKey(Constants.MAP_SPEED_AVG))
                                        userDailyTotal.avgSpeed = result.getFloat(Constants.MAP_SPEED_AVG);
                                    if (result.containsKey(Constants.MAP_SPEED_MIN))
                                        userDailyTotal.minSpeed = result.getFloat(Constants.MAP_SPEED_MIN);
                                    if (result.containsKey(Constants.MAP_SPEED_MAX))
                                        userDailyTotal.maxSpeed = result.getFloat(Constants.MAP_SPEED_MAX);

                                    if (result.containsKey(DataType.AGGREGATE_HEART_POINTS.getName())){
                                        Workout w = result.getParcelable(DataType.AGGREGATE_HEART_POINTS.getName());
                                        userDailyTotal.heartDuration = w.duration;
                                        userDailyTotal.heartIntensity = w.wattsTotal;
                                    }
                                }
                                udtList.add(userDailyTotal);
                            }
                    }
                    if (udtList.size() > 0) {
                        ArrayList<UserDailyTotals> arraySets = new ArrayList<>();
                        arraySets.addAll(udtList);
                        String sKey = UserDailyTotals.class.getSimpleName() + "_list";
                        resultBundle.putParcelableArrayList(sKey, arraySets);
                    }
                    if (workoutList.size() > 0){
                        ArrayList<Workout> arrayList = new ArrayList<>();
                        arrayList.addAll(workoutList);
                        String sKey = Workout.class.getSimpleName() + "_list";
                        resultBundle.putParcelableArrayList(sKey, arrayList);
                    }
                    if (workoutSetList.size() > 0){
                        ArrayList<WorkoutSet> arraySetList = new ArrayList<>();
                        arraySetList.addAll(workoutSetList);
                        String sKey = WorkoutSet.class.getSimpleName() + "_list";
                        resultBundle.putParcelableArrayList(sKey, arraySetList);
                    }
                    mResultReceiver.send(200, resultBundle);
                } else {
                    // not successful!
                    // resultBundle.putInt(KEY_ACTION,mActionKey);
                    mResultReceiver.send(505, resultBundle);
                }
            } // [end of OnComplete]
        };
        Fitness.getHistoryClient(context, mGoogleAccount)
                .readData(readRequest)
                .addOnCompleteListener(readCompleter);
    }
    private void dumpDataSet2(DataSet dataSet) {
        Log.d(LOG_TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getDateTimeInstance();
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
    private void insertFitHistory(Workout mWorkout, GoogleSignInAccount mGoogleAccount, Context context){
        String sPackage = Constants.ATRACKIT_ATRACKIT_CLASS;
        Device device = Device.getLocalDevice(context);
        DataSet dataSet = createActivityDataSet(mWorkout.start, mWorkout.end, mWorkout.activityName,device);
        Fitness.getHistoryClient(context, mGoogleAccount).insertData(dataSet).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Bundle resultBundle = new Bundle();
                resultBundle.putInt(KEY_FIT_ACTION, Constants.TASK_ACTION_INSERT_HISTORY);
                boolean action_success = task.isSuccessful();
                Log.d(LOG_TAG, "insertFITHistory " + action_success);
                if (action_success) {
                    long timeMs = System.currentTimeMillis();
                 //   this.mWworkout.last_sync = timeMs;
                  //  resultBundle.putParcelable("insertHistory", this.mWorkout);
                    mResultReceiver.send(200, resultBundle);
                } else {
                    mResultReceiver.send(505, resultBundle);
                }
            }
        });

    }
    /** Clears all the logging message in the LogView. */
    private void dumpSession(Session session) {
        DateFormat dateFormat = getTimeInstance();
        Log.d(LOG_TAG, "Data returned for Session: " + session.getName()
                + "\n\tDescription: " + session.getDescription()
                + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS)));

    }

    private Bundle dataPointToResultBundle(DataPoint dp){
        Bundle resultBundle = new Bundle();
        resultBundle.putString(Constants.MAP_DATA_TYPE, dp.getDataType().getName());
        if (dp.getDataType().getName().equals(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())) {
            Workout mWorkout = new Workout();
            //   mWorkout.name = dp.getDataType().getName();
            mWorkout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
            mWorkout.activityName = referencesTools.getFitnessActivityTextById(mWorkout.activityID);
            mWorkout.setCount = dp.getValue(Field.FIELD_NUM_SEGMENTS).asInt();
            mWorkout.duration = (long)dp.getValue(Field.FIELD_DURATION).asInt();
            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            if (startTime == 0)
                startTime = System.currentTimeMillis();
            mWorkout.start = startTime;
            long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
            if (endTime > 0 ) mWorkout.end = endTime;
            Log.d(LOG_TAG, "activity.summary " + mWorkout.activityName + " " + mWorkout.setCount + " duration " + mWorkout.duration);
            resultBundle.putParcelable(dp.getDataType().getName(), mWorkout);
        }
        else if (dp.getDataType().getName().equals(DataType.TYPE_POWER_SAMPLE.getName())) {
            Workout mWorkout = new Workout();
            mWorkout.wattsTotal = dp.getValue(Field.FIELD_WATTS).asFloat();
            Log.d(LOG_TAG, "power.sample " + mWorkout.activityName + " " + mWorkout.setCount);
            resultBundle.putParcelable(dp.getDataType().getName(), mWorkout);
        }
        else if (dp.getDataType().getName().equals(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
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

            if (set.start > 0) set._id = (set.start); else set._id = (System.currentTimeMillis());
            if (dp.getValue(Field.FIELD_DURATION).isSet())
                set.duration = (long) dp.getValue(Field.FIELD_DURATION).asInt();
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
            resultBundle.putParcelable(dp.getDataType().getName(), set);
        }
        else if (dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
            Workout mWorkout = new Workout();
            mWorkout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
            mWorkout.activityName = referencesTools.getFitnessActivityTextById(mWorkout.activityID);
            mWorkout.identifier = referencesTools.getFitnessActivityIdentifierById(mWorkout.activityID);
            //  mWorkout.name = dp.getDataType().getName();
            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            if (startTime == 0)
                startTime = System.currentTimeMillis();
            mWorkout.start = startTime;
            mWorkout._id = (mWorkout.start);
            long endTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
            if (endTime == 0 || (endTime == startTime)) dp.getEndTime(TimeUnit.MILLISECONDS);
            if (endTime > 0 ) mWorkout.end = endTime;
            mWorkout.duration = mWorkout.end - mWorkout.start;
            mWorkout.packageName = dp.getOriginalDataSource().getAppPackageName();
            resultBundle.putParcelable(dp.getDataType().getName(), mWorkout);
        }
        else if (dp.getDataType().getName().equals(DataType.TYPE_MOVE_MINUTES.getName())) {
            Integer minutes = (dp.getValue(Field.FIELD_DURATION) != null) ? dp.getValue(Field.FIELD_DURATION).asInt() : 0;
            resultBundle.putInt(dp.getDataType().getName(), minutes);
        }
        else if (dp.getDataType().getName().equals(DataType.TYPE_CALORIES_EXPENDED.getName())) {
            float calories = dp.getValue(Field.FIELD_CALORIES).asFloat();
            resultBundle.putFloat(dp.getDataType().getName(), calories);
        }
        else if (dp.getDataType().getName().contains(DataType.TYPE_DISTANCE_DELTA.getName())) {
            float distance = dp.getValue(Field.FIELD_DISTANCE).asFloat();
            resultBundle.putFloat(dp.getDataType().getName(), distance);
            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            if (startTime == 0) startTime = mWorkout.start;
            resultBundle.putLong(Constants.MAP_START, startTime);
        }
        else if (dp.getDataType().getName().contains(DataType.AGGREGATE_HEART_POINTS.getName())) {
            float intensity = dp.getValue(Field.FIELD_INTENSITY).asFloat();
            int duration = dp.getValue(Field.FIELD_DURATION).asInt();
            Workout w = new Workout();
            w.wattsTotal = intensity;
            w.duration = duration;
            resultBundle.putParcelable(dp.getDataType().getName(), w);
        }
        else if (dp.getDataType().getName().equals(DataType.AGGREGATE_HEART_RATE_SUMMARY.getName())) {
            float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
            float min = dp.getValue(Field.FIELD_MIN).asFloat();
            float max = dp.getValue(Field.FIELD_MAX).asFloat();
            resultBundle.putFloat(Constants.MAP_BPM_AVG, avg);
            resultBundle.putFloat(Constants.MAP_BPM_MIN, min);
            resultBundle.putFloat(Constants.MAP_BPM_MAX, max);
            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            if (startTime == 0) startTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
            long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
            if (endTime == 0) dp.getTimestamp(TimeUnit.MILLISECONDS);
            resultBundle.putLong(Constants.MAP_START, startTime);
            resultBundle.putLong(Constants.MAP_END, endTime);
        } else if (dp.getDataType().getName().equals(DataType.AGGREGATE_SPEED_SUMMARY.getName())) {
            float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
            float min = dp.getValue(Field.FIELD_MIN).asFloat();
            float max = dp.getValue(Field.FIELD_MAX).asFloat();
            resultBundle.putFloat(Constants.MAP_SPEED_AVG, avg);
            resultBundle.putFloat(Constants.MAP_SPEED_MIN, min);
            resultBundle.putFloat(Constants.MAP_SPEED_MAX, max);
            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            if (startTime == 0) startTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
            long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
            if (endTime == 0) dp.getTimestamp(TimeUnit.MILLISECONDS);
            resultBundle.putLong(Constants.MAP_START, startTime);
            resultBundle.putLong(Constants.MAP_END, endTime);
        }else if (dp.getDataType().getName().equals(DataType.AGGREGATE_STEP_COUNT_DELTA.getName())) {
            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            if (startTime == 0) startTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
            long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
            if (endTime == 0) dp.getTimestamp(TimeUnit.MILLISECONDS);
            resultBundle.putLong(Constants.MAP_START, startTime);
            resultBundle.putLong(Constants.MAP_END, endTime);
            resultBundle.putInt(Constants.MAP_STEPS, mWorkout.stepCount);
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
