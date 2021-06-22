package com.a_track_it.fitdata.common.data_model;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.InjectorUtils;
import com.a_track_it.fitdata.common.R;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
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
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.a_track_it.fitdata.common.Constants.ATRACKIT_SPACE;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_USER;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_WORKOUTID;
import static com.a_track_it.fitdata.common.Constants.MAP_HISTORY_RANGE;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_TYPE_STEPCOUNT;

public class LoadFromGoogleWorker extends Worker {
    private static final String LOG_TAG = LoadFromGoogleWorker.class.getSimpleName();

    public static final String ARG_ACTION_KEY = "action-key";
    public static final String ARG_ID_KEY = "id-key";
    public static final String ARG_START_KEY = "start-key";
    public static final String ARG_END_KEY = "end-key";

    private Calendar mCalendar;
    private String sUserID;
    private String sDeviceID;
    private String sDeviceID2;
    private long startTime = 0L;  // default to previous day
    private long endTime = 0L;  // default to previous day
    private long longID = 0L;
    private String sAction = Constants.ATRACKIT_EMPTY;

    private WorkoutRepository workoutRepository;
    private ReferencesTools referencesTools;
    private boolean bRefreshing = false;
    private boolean bLoadUDT = false;

    private WorkoutMeta mWorkoutMeta;
    private Workout mWorkout;
    private int iUDTs = 0;
    private int iWorkouts = 0;
    private int iWorkoutSets = 0;
    private int iWorkoutMetas = 0;
    private WorkManager mWorkManager;
    private List<Workout> existingWorkoutArrayList = new ArrayList<>();
    private List<WorkoutSet> existingWorkoutSetArrayList = new ArrayList<>();
    private List<WorkoutMeta> existingWorkoutMetaArrayList = new ArrayList<>();

    private List<Workout> workoutArrayList = new ArrayList<>();
    private List<WorkoutSet> workoutSetArrayList = new ArrayList<>();
    private List<WorkoutMeta> workoutMetaArrayList = new ArrayList<>();
    private List<Exercise> exercisesMissingList = new ArrayList<>();
    private List<UserDailyTotals> udtList = new ArrayList<>();
    private List<UserDailyTotals> existingUserDailyTotalList = new ArrayList<>();
    private Map<Long, Workout> mapDetected =  new HashMap<>();
    private ArrayList<OneTimeWorkRequest> workList = new ArrayList<>();

    private final OnCompleteListener<DataReadResponse> summaryReadCompleter = (OnCompleteListener<DataReadResponse>) task -> {
        try {
            if (task.getResult().getStatus().isSuccess()) {
                int iRet = task.getResult().getBuckets().size();
                long idSetter = Utilities.getDayEnd(mCalendar, startTime);
                if (iRet > 0) {
                    for (Bucket bucket : task.getResult().getBuckets()) {
                        List<DataSet> dataSets = bucket.getDataSets();
                        UserDailyTotals userDailyTotal = new UserDailyTotals();
                        if (bLoadUDT)
                            userDailyTotal.userID = sUserID;

                        for (DataSet dataSet : dataSets) {
                            List<DataPoint> dpList = dataSet.getDataPoints();
                            for (DataPoint dp : dpList) {
                                Bundle result = DataPointToBundle(dp);
                                if (result.containsKey(DataType.AGGREGATE_STEP_COUNT_DELTA.getName())) {
                                    Workout w = result.getParcelable(DataType.AGGREGATE_STEP_COUNT_DELTA.getName());
                                    if (bLoadUDT) {
                                        if ((userDailyTotal._id == 0L) && (w.start > 0))
                                            userDailyTotal._id = Utilities.getDayEnd(mCalendar,w.start);
                                        userDailyTotal.stepCount = w.stepCount;
                                    }
                                }
                                if (result.containsKey(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())) {
                                    Workout w = result.getParcelable(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName());
                                    if (bLoadUDT) {
                                        if ((userDailyTotal._id == 0L) && (w.start > 0))
                                            userDailyTotal._id = Utilities.getDayEnd(mCalendar,w.start);
                                    }
                                    w._id = w._id + workoutArrayList.size();
                                    w.last_sync = w._id;
                                    w.userID = sUserID;
                                    Log.w(LOG_TAG, "summaryReader activity summary " + w.toString());
                                    workoutArrayList.add(w);
                                }
                                if (result.containsKey(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
                                    WorkoutSet s = result.getParcelable(DataType.TYPE_WORKOUT_EXERCISE.getName());
                                    s._id = Utilities.getDayStart(mCalendar,s.start) + workoutSetArrayList.size();
                                    s.last_sync = s._id;
                                    s.userID = sUserID;
                                    Log.w(LOG_TAG, "adding set " + s.toString());
                                    workoutSetArrayList.add(s);
                                }
                                if (result.containsKey(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
                                    Workout w = result.getParcelable(DataType.TYPE_ACTIVITY_SEGMENT.getName());
                                    if (bLoadUDT) {
                                        if ((userDailyTotal._id == 0L) && (w.start > 0))
                                            userDailyTotal._id = Utilities.getDayEnd(mCalendar,w.start);
                                    }
                                    if (w._id == 0L) w._id = Utilities.getDayStart(mCalendar,w.start) + w.activityID;
                                    w._id = w._id + workoutArrayList.size();
                                    w.last_sync = w._id;
                                    w.userID = sUserID;
                                    Log.w(LOG_TAG, "summaryReader TYPE_ACTIVITY_SEGMENT  " + w.activityName);
                                    workoutArrayList.add(w);
                                    Log.w(LOG_TAG, workoutArrayList.size() + " adding segment w " + w.toString());
                                }
                                if (bLoadUDT) {
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
                                    if (result.containsKey(DataType.AGGREGATE_HEART_POINTS.getName())) {
                                        Workout w = result.getParcelable(DataType.AGGREGATE_HEART_POINTS.getName());
                                        userDailyTotal.heartDuration = w.duration;
                                        userDailyTotal.heartIntensity = w.wattsTotal;
                                    }
                                }
                            }
                        }
                        if (bLoadUDT) {
                            if ((idSetter > 0) && (userDailyTotal._id == 0))
                                userDailyTotal._id = idSetter;
                            Log.e(LOG_TAG, "adding udt " + userDailyTotal.toString());
                            udtList.add(userDailyTotal);
                            mCalendar.add(Calendar.DAY_OF_YEAR, 1);
                            idSetter = mCalendar.getTimeInMillis();
                        }
                    }
                }
                bRefreshing = false;
            }
        }catch(Exception e){
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
                String sTitle = "Click to continue Load from Google refresh";
                String sContent = "Unable to continue load - click to resolve";
                buildPendingIntentNotification(sTitle, sContent, pi);
            }
            Log.e(LOG_TAG, "readFitHistoryError " + e.getMessage());
            bRefreshing = false;
        }
    };
    private final OnCompleteListener<DataReadResponse> exerciseResponse = task -> {
        if (task.isSuccessful()) {
            List<DataSet> listDataSet = task.getResult().getDataSets();
            if (listDataSet != null)
                for (DataSet dataSet : listDataSet) {
                    List<DataPoint> dpList = dataSet.getDataPoints();
                    for (DataPoint dp : dpList) {
                        Bundle result = DataPointToBundle(dp);
                        if (result.containsKey(DataType.TYPE_WORKOUT_EXERCISE.getName())){
                            WorkoutSet ws = result.getParcelable(DataType.TYPE_WORKOUT_EXERCISE.getName());
                            if ((existingWorkoutSetArrayList.size() == 0) || !isExistingOrOverLappingSet(ws)) {
                                ws.userID = sUserID;
                                ws.deviceID = mWorkout.deviceID;
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
                                        exerciseList =  workoutRepository.getExercisesByWorkoutExerciseName(sName); //new getExerciseTask(mExerciseDao).execute(Constants.ATRACKIT_EMPTY, sName).get(2, TimeUnit.MINUTES);
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
                                    }else{
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
                                workoutSetArrayList.add(ws);

                            }
                        }
                    }
                }
        }
        bRefreshing = false;
    };
    private final OnCompleteListener<DataReadResponse> metaResponse = task -> {
        if (task.isSuccessful()) {
            if (task.getResult().getBuckets().size() > 0){
                for (Bucket bucket : task.getResult().getBuckets()) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    for (DataSet dataSet : dataSets) {
                        List<DataPoint> dpList = dataSet.getDataPoints();
                        for (DataPoint dp : dpList) {
                            DataPointToMetaData(dp);
                        }
                    }
                }
            }else {
                List<DataSet> listDataSet = task.getResult().getDataSets();
                if (listDataSet != null)
                    for (DataSet dataSet : listDataSet) {
                        List<DataPoint> dpList = dataSet.getDataPoints();
                        for (DataPoint dp : dpList) {
                            DataPointToMetaData(dp);
                        }
                    }
            }
        }
        bRefreshing = false;
    };


    public LoadFromGoogleWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        workoutRepository =  InjectorUtils.getWorkoutRepository((Application) context);
        referencesTools = ReferencesTools.setInstance(context);
        mCalendar = Calendar.getInstance(Locale.getDefault());
        mWorkManager = WorkManager.getInstance(context);
    }
    /** doWork
     read un-synchronized
     **/
    @NonNull
    @Override
    public Result doWork() {
                Context context = getApplicationContext();
                ApplicationPreferences applicationPreferences = ApplicationPreferences.getPreferences(context);
                if (!applicationPreferences.getBackgroundLoadComplete()) return Result.success();
                applicationPreferences.setBackgroundLoadComplete(false);
                Data inputData = getInputData();
                String sExerciseList = Constants.ATRACKIT_EMPTY;
                sAction = inputData.getString(ARG_ACTION_KEY);
                endTime  = inputData.getLong(ARG_END_KEY,mCalendar.getTimeInMillis());
                mCalendar.add(Calendar.DAY_OF_YEAR,-1);
                startTime = inputData.getLong(ARG_START_KEY,mCalendar.getTimeInMillis());  // default to previous day
                longID = inputData.hasKeyWithValueOfType(ARG_ID_KEY,Long.class) ? inputData.getLong(ARG_ID_KEY,0) : 0L;
                sUserID = inputData.getString(Constants.KEY_FIT_USER);
                String sTemp = "start " + Utilities.getTimeDateString(startTime) + ATRACKIT_SPACE + " end " + Utilities.getTimeDateString(endTime);
                Log.e(LOG_TAG, sTemp);
                bLoadUDT = true;
                FitnessOptions fo = referencesTools.getFitnessSignInOptions(2); // read sesssions
                GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(context,fo);
                if (gsa == null || (gsa.isExpired() || ((sUserID.length() > 0) && !sUserID.equals(gsa.getId())))) {
                    return Result.failure();
                }



                long timeMs = System.currentTimeMillis();
                mCalendar.setTimeInMillis(timeMs);
                if (inputData.hasKeyWithValueOfType(KEY_FIT_DEVICE_ID, String.class))
                    sDeviceID = inputData.getString(Constants.KEY_FIT_DEVICE_ID);
                else{
                    List<Configuration> list = workoutRepository.getConfigLikeName(Constants.KEY_DEVICE1, sUserID);
                    if (list != null && (list.size() > 0)) sDeviceID = list.get(0).stringValue;
                }
                List<Configuration> listConfigs = workoutRepository.getConfigLikeName(Constants.KEY_DEVICE2, sUserID);
                if (listConfigs != null && (listConfigs.size() > 0)) sDeviceID2 = listConfigs.get(0).stringValue; else sDeviceID2 = Constants.ATRACKIT_EMPTY;

                Long lHistoryStart = 0L;
                Long lHistoryEnd = 0L;
                Configuration configHistory = null;
                List<Configuration> existingConfigs = workoutRepository.getConfigLikeName(Constants.MAP_HISTORY_RANGE, sUserID);
                if (existingConfigs.size() > 0) {
                    configHistory = existingConfigs.get(0);
                    lHistoryStart = Long.parseLong(configHistory.stringValue1);
                    lHistoryEnd = Long.parseLong(configHistory.stringValue2);
                }

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-dd HH:mm:ss z", Locale.getDefault());
                Log.w(LOG_TAG,"doWork " + sAction + " s " + simpleDateFormat.format(startTime) + " e " + simpleDateFormat.format(endTime));
                if ((lHistoryStart > 0) && (lHistoryEnd > 0))
                    Log.w(LOG_TAG,"History Values now " +  "s " + simpleDateFormat.format(lHistoryStart) + " e " + simpleDateFormat.format(lHistoryEnd));
                else
                    Log.w(LOG_TAG,"History Values NOT SET YET ");

                List<Workout> wL = workoutRepository.getWorkoutsNow(sUserID,Constants.ATRACKIT_EMPTY,startTime,endTime); // ALL NEEDED!
                if ((wL != null) && (wL.size() > 0))
                    existingWorkoutArrayList.addAll(wL);
                List<WorkoutSet> sL = workoutRepository.getWorkoutSetsNow(sUserID,Constants.ATRACKIT_EMPTY,startTime, endTime);  // ALL NEEDED!
                if ((sL != null) && (sL.size() > 0))
                    existingWorkoutSetArrayList.addAll(sL);
                List<WorkoutMeta> mL = workoutRepository.getWorkoutMetasNow(sUserID,Constants.ATRACKIT_EMPTY,startTime, endTime);
                if ((mL != null) && (mL.size() > 0))
                    existingWorkoutMetaArrayList.addAll(mL);
                List<UserDailyTotals> uL = workoutRepository.getUserDailyTotals(sUserID, startTime,endTime,1); // daily
                if ((uL != null) && (uL.size() > 0))
                    existingUserDailyTotalList.addAll(uL);
                try{
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
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .bucketByTime(1,TimeUnit.DAYS)
                            .setLimit(2000)
                            .enableServerQueries().build();// Used to retrieve data from cloud.
                    bRefreshing = true;
                    Task<DataReadResponse> response = Fitness.getHistoryClient(context, gsa).readData(readRequest);
                    response.addOnCompleteListener(summaryReadCompleter);
                    Tasks.await(response, 5, TimeUnit.MINUTES);
                    long waitStart = SystemClock.elapsedRealtime();
                    while (bRefreshing) {
                        Log.w(LOG_TAG, "sleeping on Summary refresh");
                        SystemClock.sleep(5000);
                        if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                            bRefreshing = false;
                    }
                    if ((udtList != null) && (udtList.size() > 0)) {
                        for (UserDailyTotals udt : existingUserDailyTotalList){
                            ListIterator<UserDailyTotals> iterator = udtList.listIterator();
                            while (iterator.hasNext()){
                                UserDailyTotals newUDT = iterator.next();
                                if (newUDT._id == udt._id){
                                    iterator.remove();
                                    break;
                                }
                            }
                        }
                        iUDTs = udtList.size();
                        if (iUDTs > 0) {
                            for (UserDailyTotals u : udtList) {
                                int doyUDT = Utilities.getDOY(mCalendar, u._id);
                                for(Workout w: workoutArrayList){
                                    int doyWorkout = Utilities.getDOY(mCalendar, w.start);
                                    if (doyUDT == doyWorkout){
                                        if (w.activityID == Constants.WORKOUT_TYPE_INVEHICLE) u.durationVehicle = w.duration;
                                        if (w.activityID == Constants.WORKOUT_TYPE_BIKING) u.durationBiking = w.duration;
                                        if (w.activityID == Constants.WORKOUT_TYPE_STILL) u.durationStill = w.duration;
                                        if (w.activityID == Constants.WORKOUT_TYPE_UNKNOWN) u.durationUnknown = w.duration;
                                        if (w.activityID == Constants.WORKOUT_TYPE_WALKING) u.durationWalking = w.duration;
                                        if (w.activityID == Constants.WORKOUT_TYPE_RUNNING) u.durationRunning = w.duration;
                                        if (w.activityID == 2) u.durationOnFoot = w.duration;
                                        if (w.activityID == 5) u.durationTilting = w.duration;
                                        break;
                                    }
                                }
                                workoutRepository.insertUserDailyTotal(u);
                            }
                        }
                    }
                    mapDetected.clear();
                    bLoadUDT = false; // just the activity segments now - to query for other things
                    bRefreshing = true;
                    readRequest = new DataReadRequest.Builder()
                            .aggregate(DataType.TYPE_ACTIVITY_SEGMENT)
                            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                            .bucketBySession(3,TimeUnit.MINUTES)
                            .setLimit(1000)
                            .enableServerQueries().build();// Used to retrieve data from cloud.*/
                    Task<DataReadResponse> response2 = Fitness.getHistoryClient(context, gsa).readData(readRequest);
                    response2.addOnCompleteListener(summaryReadCompleter);
                    Tasks.await(response2, 5, TimeUnit.MINUTES);
                    waitStart = SystemClock.elapsedRealtime();
                    while (bRefreshing) {
                        Log.w(LOG_TAG, "sleeping on TYPE_ACTIVITY_SEGMENT refresh");
                        SystemClock.sleep(5000);
                        if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                            bRefreshing = false;
                    }
                    if (!mapDetected.isEmpty()){
                        for(Map.Entry<Long, Workout> entry: mapDetected.entrySet()){
                            Workout w = entry.getValue();
                            long iActivityID = entry.getKey();
                            boolean existing = isExistingOrOverLapping(w);
                            Log.w(LOG_TAG,"detected activityMap " + iActivityID + " existing " + existing);
                            if (!existing) {
                                Workout tester = workoutRepository.getWorkoutByIdNow(w._id, sUserID, Constants.ATRACKIT_EMPTY);
                                if (tester != null){
                                    workoutRepository.updateWorkout(w);
                                    Log.w(LOG_TAG, "FOUND tester detected activityMap " + iActivityID + " existing " + tester.toString());
                                }else
                                    workoutRepository.insertWorkout(w);
                            }else
                                workoutRepository.updateWorkout(w);
                        }
                        mapDetected.clear();
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "read " + sAction + ATRACKIT_SPACE + e.getMessage());
                    return Result.failure();
                }

                long waitStart = SystemClock.elapsedRealtime();
                while (bRefreshing) {
                    Log.w(LOG_TAG, "sleeping on refresh");
                    SystemClock.sleep(5000);
                    if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                        bRefreshing = false;
                }
                // rebuild the existing list to include any new additions - missing
                wL.clear();
                existingWorkoutArrayList.clear();
                wL = workoutRepository.getWorkoutsNow(sUserID, Constants.ATRACKIT_EMPTY, startTime, endTime); // ALL NEEDED!
                if ((wL != null) && (wL.size() > 0))
                    existingWorkoutArrayList.addAll(wL);

            synchronized ((workoutArrayList)) {
                Log.e(LOG_TAG, "about to process " + workoutArrayList.size());
                ListIterator<Workout> workoutIterator = workoutArrayList.listIterator();
                while (workoutIterator.hasNext()) {
                    Workout w = workoutIterator.next();
                    if (isExistingOrOverLapping(w)) {
                        Log.e(LOG_TAG, "Existing workout " + referencesTools.workoutShortText(w));
                        workoutIterator.remove();
                    } else {
                        w.deviceID = sDeviceID2; // not from us !
                        mWorkout = w;
                        int set_duration = Math.toIntExact(w.end - w.start);
                        // only active things build a Meta
                        if (!Utilities.isDetectedActivity(w.activityID)
                                && (set_duration >= TimeUnit.MINUTES.toMillis(2))) {
                            mWorkoutMeta = new WorkoutMeta();
                            mWorkoutMeta._id = System.currentTimeMillis();
                            mWorkoutMeta.userID = mWorkout.userID;
                            mWorkoutMeta.deviceID = mWorkout.deviceID;
                            mWorkoutMeta.workoutID = mWorkout._id;
                            mWorkoutMeta.activityID = mWorkout.activityID;
                            mWorkoutMeta.activityName = mWorkout.activityName;
                            mWorkoutMeta.start = mWorkout.start;
                            mWorkoutMeta.end = mWorkout.end;
                            mWorkoutMeta.duration = mWorkout.duration;
                            mWorkoutMeta.packageName = mWorkout.packageName;
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
                                DataReadRequest request = builder.build();
                                try {
                                    bRefreshing = true;
                                    Task<DataReadResponse> responseTask = Fitness.getHistoryClient(context, gsa)
                                            .readData(request);
                                    responseTask.addOnCompleteListener(metaResponse);
                                    Tasks.await(responseTask, 5, TimeUnit.MINUTES);
                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "read meta " + Utilities.getDateString(mWorkoutMeta.start) + ATRACKIT_SPACE + e.getMessage());
                                    bRefreshing = false;
                                }
                                workoutMetaArrayList.add(mWorkoutMeta);
                            }
                        }
                        waitStart = SystemClock.elapsedRealtime();
                        while (bRefreshing) {
                            Log.w(LOG_TAG, "sleeping on refresh");
                            SystemClock.sleep(5000);
                            if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                bRefreshing = false;
                        }
                        existingWorkoutArrayList.add(mWorkout);
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
                            Task<DataReadResponse> exerciseTask = Fitness.getHistoryClient(context, gsa).readData(readExerciseRequest);
                            exerciseTask.addOnCompleteListener(exerciseResponse);
                            Tasks.await(exerciseTask, 2, TimeUnit.MINUTES);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "read exercise " + sAction + ATRACKIT_SPACE + e.getMessage());
                            bRefreshing = false;
                        }
                        waitStart = SystemClock.elapsedRealtime();
                        while (bRefreshing) {
                            Log.w(LOG_TAG, "sleeping on refresh read exercises " + referencesTools.workoutShortText(w));
                            SystemClock.sleep(5000);
                            if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                bRefreshing = false;
                        }
                    }
                }  // for each workout
                ListIterator wIterator = workoutArrayList.listIterator();
                while (wIterator.hasNext()) {
                    Workout w = (Workout) wIterator.next();
                    if ((w._id > 0) && (w.userID.length() > 0)) {
                        Workout finder = workoutRepository.getWorkoutByIdNow(w._id, w.userID, Constants.ATRACKIT_EMPTY);
                        if (finder == null) {
                            Log.w(LOG_TAG, "adding new WORKOUT  " + referencesTools.workoutShortText(w));
                            workoutRepository.insertWorkout(w);
                            iWorkouts += 1;
                            if (!Utilities.isDetectedActivity(w.activityID)) {
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
                            Log.e(LOG_TAG, "found existing anyway " + finder.toString());
                        }
                    }
                }
            }
            synchronized (workoutSetArrayList) {
                for (WorkoutSet s : workoutSetArrayList) {
                    boolean bValid = s.isValid(true);
                    if ((s._id > 0) && (s.userID.length() > 0)) {
                        WorkoutSet finderSet = workoutRepository.getWorkoutSetByIdNow(s._id, s.userID, Constants.ATRACKIT_EMPTY);
                        if (finderSet == null) {
                            Log.w(LOG_TAG, "adding new SET  " + bValid + " " + referencesTools.workoutSetShortText(s));
                            workoutRepository.insertWorkoutSet(s);
                            iWorkoutSets += 1;
                        } else Log.w(LOG_TAG, "found existing SET anyway " + bValid + " " + referencesTools.workoutSetShortText(finderSet));
                    }
                }
            }
            for (WorkoutMeta m: workoutMetaArrayList){
                workoutRepository.insertWorkoutMeta(m);
                //mWorkoutMetaDao.insert(m);
                iWorkoutMetas += 1;
            }
            if (workList.size() > 0){
                if (mWorkManager == null) mWorkManager = WorkManager.getInstance(context);
                mWorkManager.enqueue(workList);
            }
/*            Intent broadcastIntent = new Intent(Constants.INTENT_CLOUD_POPULATE);
            broadcastIntent.putExtra(Constants.KEY_FIT_USER, sUserID);
            broadcastIntent.putExtra(Constants.KEY_FIT_VALUE, iWorkouts + iWorkoutSets + iWorkoutMetas);
            context.sendBroadcast(broadcastIntent);*/
            if (exercisesMissingList.size() > 0){
                for (Exercise ex : exercisesMissingList){
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
            }
            if (configHistory != null){
                boolean bUpdate = false;
                if ((startTime < lHistoryStart) || (lHistoryStart == 0)) {
                    configHistory.stringValue1 = Long.toString(startTime);
                    bUpdate = true;
                }
                if ((endTime > lHistoryEnd) || (lHistoryEnd == 0)){
                    configHistory.stringValue2 = Long.toString(endTime);
                    bUpdate = true;
                }
                if (bUpdate) workoutRepository.updateConfig(configHistory);
                Log.e(LOG_TAG, "updating History Config " + configHistory.toString());
            }else{
                Configuration conf = new Configuration();
                conf.stringName = MAP_HISTORY_RANGE;
                conf.userValue = sUserID;
                conf.stringValue = Constants.ATRACKIT_ATRACKIT_CLASS;
                conf.stringValue1 = Long.toString(startTime);
                conf.stringValue2 = Long.toString(endTime);
                workoutRepository.insertConfig(conf);
                Log.e(LOG_TAG, "Inserting History Config " + configHistory.toString());
            }
            Log.w(LOG_TAG, "finishing" + " total iUDT " + iUDTs + " w " + iWorkouts + " s " +iWorkoutSets + " m "+iWorkoutMetas);
            if (iWorkouts == 1) Log.w(LOG_TAG," " + referencesTools.workoutShortText(workoutArrayList.get(0)));
            if (iWorkoutSets == 1) Log.w(LOG_TAG," " + referencesTools.workoutSetShortText(workoutSetArrayList.get(0)));
            workoutRepository.destroyInstance();
            applicationPreferences.setBackgroundLoadComplete(true);
            Data outputData = new Data.Builder()
                    .putInt(Constants.KEY_RESULT, iUDTs + iWorkouts + iWorkoutSets + iWorkoutMetas)
                    .putInt(UserDailyTotals.class.getSimpleName(), iUDTs)
                    .putInt(Workout.class.getSimpleName(), iWorkouts)
                    .putInt(WorkoutSet.class.getSimpleName(), iWorkoutSets)
                    .putInt(WorkoutMeta.class.getSimpleName(), iWorkoutMetas)
                    .putString(ARG_ACTION_KEY, sAction)
                    .putLong(ARG_START_KEY, startTime)
                    .putLong(ARG_END_KEY, endTime)
                    .putString(Constants.KEY_FIT_USER, sUserID)
                    .putString(Constants.KEY_LIST_SETS, sExerciseList)
                    .putString(Constants.KEY_FIT_VALUE, LOG_TAG)
                    .build();
            return Result.success(outputData);

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

    private static class doGetSilentGoogleAccountCallable implements Callable<GoogleSignInAccount> {
        private Context mContext;

        doGetSilentGoogleAccountCallable(Context c){
            mContext = c;
        }

        @Override
        public GoogleSignInAccount call() throws Exception {
            try {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(Constants.CLIENT_ID)
                        .requestEmail().build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(mContext, gso);
                Task<GoogleSignInAccount> signInAccountTask = googleSignInClient.silentSignIn();
                return signInAccountTask.getResult();
            }catch (Exception e){
                throw e;
            }
        }
    }
    /**
     *   DataPointToBundle
     *   build bundles from data points
     **/
    private Bundle DataPointToBundle(DataPoint dp){
        Bundle resultBundle = new Bundle();
        if (dp.getDataType().getName().equals(DataType.AGGREGATE_ACTIVITY_SUMMARY.getName())) {
            Workout workout = new Workout();
            workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
            workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
            workout.identifier = referencesTools.getFitnessActivityIdentifierById(workout.activityID);
            workout.scoreTotal = dp.getValue(Field.FIELD_NUM_SEGMENTS).asInt();
            workout.duration = (long)dp.getValue(Field.FIELD_DURATION).asInt();
            long lStartTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            if (lStartTime == 0)
                workout.start = startTime;
            else
                workout.start = lStartTime;
            workout._id = (workout.start) + workout.activityID;
            long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
            if (endTime > 0 ) workout.end = endTime;
            workout._id = workout.start;
            workout.userID = sUserID;
          //  workout.deviceID = sDeviceID;
            if ((dp.getOriginalDataSource() != null) && (dp.getOriginalDataSource().getAppPackageName() != null)
                    && (dp.getOriginalDataSource().getAppPackageName().length() > 0))
                workout.packageName = dp.getOriginalDataSource().getAppPackageName();
            else
                workout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
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
                if (timeStamp > 0){
                    set.start = timeStamp;
                    if (set.duration > 0)
                        set.end = timeStamp + set.duration;
                }else {
                    set.start = startTime;
                    if (set.duration > 0)
                        set.end = set.start + set.duration;

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
            set.setCount = 1;
            set.repCount = dp.getValue(Field.FIELD_REPETITIONS).asInt();
            if ((set.weightTotal > 0) && (set.repCount > 0))
                set.wattsTotal = (set.weightTotal * set.repCount);
            set.activityName = referencesTools.getFitnessActivityTextById(Constants.WORKOUT_TYPE_STRENGTH);
            set.activityID = Constants.WORKOUT_TYPE_STRENGTH;
            set.userID = sUserID;
          //  set.deviceID = sDeviceID;
            set.last_sync = set._id;
            if ((dp.getOriginalDataSource() != null) && (dp.getOriginalDataSource().getAppPackageName() != null)
                    && (dp.getOriginalDataSource().getAppPackageName().length() > 0))
                set.score_card = dp.getOriginalDataSource().getAppPackageName();
            else
                set.score_card = Constants.ATRACKIT_ATRACKIT_CLASS;
            Log.d(LOG_TAG, "dp " + referencesTools.workoutSetShortText(set));
            resultBundle.putParcelable(dp.getDataType().getName(), set);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
            Workout workout = new Workout();
            workout.activityID = (long)dp.getValue(Field.FIELD_ACTIVITY).asInt();
            workout.activityName = referencesTools.getFitnessActivityTextById(workout.activityID);
            workout.identifier = referencesTools.getFitnessActivityIdentifierById(workout.activityID);
            long lStartTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            if (lStartTime == 0)
                workout.start = startTime;
            else
                workout.start = lStartTime;
            workout._id = (workout.start) + workout.activityID;
            long endTime = dp.getTimestamp(TimeUnit.MILLISECONDS);
            if (endTime == 0 || (endTime == startTime)) dp.getEndTime(TimeUnit.MILLISECONDS);
            if (endTime > 0 ) workout.end = endTime;
            workout.userID = sUserID;
          //  workout.deviceID = sDeviceID;
            workout.duration = workout.end - workout.start;
            if ((dp.getOriginalDataSource() != null) && (dp.getOriginalDataSource().getAppPackageName() != null)
                    && (dp.getOriginalDataSource().getAppPackageName().length() > 0))
                workout.packageName = dp.getOriginalDataSource().getAppPackageName();
            else
                workout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
            resultBundle.putParcelable(dp.getDataType().getName(), workout);
        } else if (dp.getDataType().getName().equals(DataType.TYPE_MOVE_MINUTES.getName())) {
            Integer minutes = ( dp.getValue(Field.FIELD_DURATION) != null) ? dp.getValue(Field.FIELD_DURATION).asInt() : 0;
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
        }else if (dp.getDataType().getName().equals(DataType.AGGREGATE_STEP_COUNT_DELTA.getName())) {
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
          //  workout.deviceID = sDeviceID;
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
}
