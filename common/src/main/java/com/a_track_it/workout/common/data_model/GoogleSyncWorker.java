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

import com.a_track_it.workout.common.AppExecutors;
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
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
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
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.a_track_it.workout.common.Constants.ATRACKIT_SPACE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;
import static com.a_track_it.workout.common.Constants.KEY_FIT_WORKOUTID;
import static com.a_track_it.workout.common.Constants.MAP_HISTORY_RANGE;
import static com.a_track_it.workout.common.Constants.MAP_STEPS;
import static com.a_track_it.workout.common.Constants.WORKOUT_TYPE_STEPCOUNT;
import static com.google.android.gms.fitness.data.Field.FIELD_DURATION;
import static com.google.android.gms.fitness.data.Field.FIELD_REPETITIONS;
import static com.google.android.gms.fitness.data.Field.FIELD_RESISTANCE;
import static com.google.android.gms.fitness.data.Field.FIELD_RESISTANCE_TYPE;

public class GoogleSyncWorker extends Worker {
    private static String LOG_TAG = GoogleSyncWorker.class.getSimpleName();

    private WorkoutRepository workoutRepository;
    private ReferencesTools referencesTools;
    private Calendar mCalendar;
    private WorkManager mWorkManager;
    private ArrayList<OneTimeWorkRequest> workList = new ArrayList<>();
    private final ApplicationPreferences appPrefs;
    private boolean bRefreshing;
    private String sUserID;
    private String sDeviceID;
    private String sDeviceID2;
    private Workout mWorkout;
    private String sExerciseList;
    private long startTime = 0;
    private long endTime = 0;
    private long syncInterval = 0;
    private List<Workout> existingWorkoutArrayList = new ArrayList<>();  // checking existing when Load FROM
    private List<WorkoutSet> existingWorkoutSetArrayList = new ArrayList<>();
    private List<Exercise> exercisesMissingList = new ArrayList<>();
    private boolean requiresLogin = false;
    private List<Workout> pendingWorkoutList = new ArrayList<>();      // pending to Load TO
    private List<WorkoutSet> pendingWorkoutSetList = new ArrayList<>();

    private List<Workout> fitWorkoutList = new ArrayList<>();         // already loaded to Google to check when load TO
    private List<WorkoutSet> fitWorkoutSetList = new ArrayList<>();

    private List<Workout> loadingWorkoutList = new ArrayList<>();      // pending to Load TO
    private List<WorkoutSet> loadingWorkoutSetList = new ArrayList<>();

    private int iWorkouts = 0; private int iSteps = 0; private int iWorkoutSets = 0;
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
                                    s._id = Utilities.getDayStart(mCalendar,s.start) + dpList.size();
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
            bRefreshing=false;
        }catch(Exception e){
            handleTaskError(task);
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "historyReadCompleter " + e.getMessage());
            bRefreshing = false;
        }
    };
    private final OnCompleteListener<DataReadResponse> summaryReadCompleter = task -> {
        try {
            if (task.getResult().getStatus().isSuccess()) {
                int iRet = task.getResult().getBuckets().size();
                if (iRet > 0) {
                    for (Bucket bucket : task.getResult().getBuckets()) {
                        List<DataSet> dataSets = bucket.getDataSets();
                        for (DataSet dataSet : dataSets) {
                            List<DataPoint> dpList = dataSet.getDataPoints();
                            for (DataPoint dp : dpList) {
                                Bundle result = DataPointToBundle(dp);
                                if (result.containsKey(DataType.TYPE_WORKOUT_EXERCISE.getName())) {
                                    WorkoutSet s = result.getParcelable(DataType.TYPE_WORKOUT_EXERCISE.getName());
                                    s._id = Utilities.getDayStart(mCalendar,s.start) + loadingWorkoutSetList.size();
                                    s.last_sync = s._id;
                                    s.userID = sUserID;
                                    //  s.deviceID = sDeviceID;
                                    Log.w(LOG_TAG, "adding set " + s.toString());
                                    loadingWorkoutSetList.add(s);
                                }
                                if (result.containsKey(DataType.TYPE_ACTIVITY_SEGMENT.getName())) {
                                    Workout w = result.getParcelable(DataType.TYPE_ACTIVITY_SEGMENT.getName());
                                    if (w._id == 0L) w._id = Utilities.getDayStart(mCalendar,w.start) + w.activityID;
                                    w._id = w._id + loadingWorkoutList.size();
                                    w.last_sync = w._id;
                                    w.userID = sUserID;
                                    Log.w(LOG_TAG, "summaryReader TYPE_ACTIVITY_SEGMENT  " + w.activityName);
                                    loadingWorkoutList.add(w);
                                    Log.w(LOG_TAG, loadingWorkoutList.size() + " adding segment w " + w.toString());
                                }
                            }
                        }
                    }
                }
                bRefreshing = false;
            }else handleTaskError(task);
        }catch(Exception e){
            if (e instanceof RuntimeException) handleTaskError(task);
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "summaryReadCompleter " + e.getMessage());
            bRefreshing = false;
        }
    };
    private final OnCompleteListener<DataReadResponse> exerciseResponse = task -> {
        try{
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
            }else{
                handleTaskError(task);
            }
            bRefreshing = false;
        }
        catch(Exception e){
            if (e instanceof RuntimeException) handleTaskError(task);
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "exerciseResponse " + e.getMessage());
            bRefreshing = false;
        }
    };


    public GoogleSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        workoutRepository =  InjectorUtils.getWorkoutRepository((Application) context);
        referencesTools = ReferencesTools.setInstance(context);
        appPrefs = ApplicationPreferences.getPreferences(context);
        mCalendar = Calendar.getInstance(Locale.getDefault());
        mWorkManager = WorkManager.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        Context context = getApplicationContext();
        sUserID = inputData.getString(Constants.KEY_FIT_USER);
        sDeviceID = inputData.getString(Constants.KEY_FIT_DEVICE_ID);
        final long timeMs = System.currentTimeMillis();
        syncInterval = appPrefs.getLastSyncInterval();
        final long lastSync = appPrefs.getLastSync();
        if ((lastSync > 0) && ((timeMs - lastSync) < syncInterval)){
            Log.e(LOG_TAG, "SyncInterval not expired " + Utilities.getDurationBreakdown((timeMs - lastSync)) + " interval " + Utilities.getDurationBreakdown(syncInterval));
            return Result.success();
        }
        if (sUserID == null || sUserID.length() == 0 || !appPrefs.getAppSetupCompleted()) return Result.failure();
        if (!sUserID.equals(appPrefs.getLastUserID()) || (appPrefs.getLastUserID().length() == 0)) {
            Log.e(LOG_TAG, "mismatch sUserID " + sUserID + " appPrefs " + appPrefs.getLastUserID());
            return Result.failure();
        }
        long waitStart = 0;
        long lHistoryStart = 0; long lHistoryEnd = 0;
        mCalendar.setTimeInMillis(timeMs);
        List<Configuration> configList = workoutRepository.getConfigLikeName(Constants.MAP_CURRENT_STATE,sUserID);
        Configuration configHistory = null;
        int currentState = Constants.WORKOUT_INVALID;
        if (configList != null){
            currentState = Math.toIntExact(configList.get(0).longValue);
            if (currentState == Constants.WORKOUT_LIVE || currentState == Constants.WORKOUT_PAUSED || currentState == Constants.WORKOUT_CALL_TO_LINE) return Result.success();
        }
        appPrefs.setLastSync(timeMs);
        // get another device 2 if available
        configList = workoutRepository.getConfigLikeName(Constants.KEY_DEVICE2, sUserID);
        if (configList != null && (configList.size() > 0)) sDeviceID2 = configList.get(0).stringValue2;
        // get the date range
        configList = workoutRepository.getConfigLikeName(Constants.MAP_HISTORY_RANGE,sUserID);
        if ((configList != null) && (configList.size() > 0)) {
            configHistory = configList.get(0);
            lHistoryStart = Long.parseLong(configHistory.stringValue1);
            lHistoryEnd = Long.parseLong(configHistory.stringValue2);
        }else{
            configHistory = new Configuration(Constants.MAP_HISTORY_RANGE, sUserID, Constants.ATRACKIT_ATRACKIT_CLASS, 0L, "0", "0");
            mCalendar.add(Calendar.DAY_OF_YEAR,-14);
            configHistory.stringValue1 = Long.toString(mCalendar.getTimeInMillis());
            configHistory.stringValue2 = Long.toString(timeMs);
        }
        long starting = Long.parseLong(configHistory.stringValue1);
        mCalendar.setTimeInMillis(starting);
        // find unsync
        pendingWorkoutList = workoutRepository.getUnSyncWorkouts(sUserID, sDeviceID,starting,timeMs);
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
                || ((pendingWorkoutSetList != null) && (pendingWorkoutSetList.size() > 0))){

            FitnessOptions fo = referencesTools.getFitnessSignInOptions(5); // read write sesssions
            GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(context,fo);
            if (gsa == null || (gsa.isExpired() || ((sUserID.length() > 0) && !sUserID.equals(gsa.getId())))) {
                if (gsa != null && gsa.isExpired()) {
                    Log.e(LOG_TAG, "expired login account ");
                } else
                    Log.e(LOG_TAG, "no previous login account ");
                gsa = GoogleSignIn.getLastSignedInAccount(context);
                if (gsa == null || gsa.isExpired()) {
                    if (gsa != null &&  gsa.isExpired()) {
                        Log.e(LOG_TAG, "last login account ");
                    } else
                        Log.e(LOG_TAG, "no last login account ");
                    doGetSilentGoogleAccountCallable getSilentCallable = new doGetSilentGoogleAccountCallable(context);
                    GoogleSignInAccount googleSignInAccount = null;
                    try {
                        Future<GoogleSignInAccount> future = AppExecutors.getInstance().diskService().submit(getSilentCallable);
                        googleSignInAccount = future.get(5, TimeUnit.MINUTES);
                        if (googleSignInAccount == null) {
                            Log.e(LOG_TAG, "fail to silent sign-in ");
                            return Result.failure();
                        }
                        Log.w(LOG_TAG, "successful silent sign-in " + googleSignInAccount.getDisplayName());
                        gsa = googleSignInAccount;
                    } catch (IllegalStateException e) {
                        Log.e(LOG_TAG, "fail execution illegalState exception " + e.getMessage());
                        e.printStackTrace();
                        return Result.failure();
                    } catch (ExecutionException e) {
                        Log.e(LOG_TAG, "fail execution exception " + e.getMessage());
                        e.printStackTrace();
                        return Result.failure();
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, "fail interrupted exception " + e.getMessage());
                        e.printStackTrace();
                        return Result.failure();
                    } catch (TimeoutException e) {
                        Log.e(LOG_TAG, "fail timeout exception " + e.getMessage());
                        e.printStackTrace();
                        return Result.failure();
                    }
                }
            }
            if (gsa == null){
                return Result.failure();
            }
            int gymCount = 0;
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
            // workouts first so sync'd non gym workouts can set last_sync on sets
            if ((pendingWorkoutList != null) && pendingWorkoutList.size() > 0){
                for (Workout pendingWorkout : pendingWorkoutList) {             // read existing loaded data...
                    bRefreshing = true;
                    if (fitWorkoutList.size() > 0) fitWorkoutList.clear();
                    if (fitWorkoutSetList.size() > 0) fitWorkoutSetList.clear();
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
                            Log.w(LOG_TAG, "sleeping on Summary refresh");
                            SystemClock.sleep(5000);
                            if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                bRefreshing = false;
                        }
                    }catch (Exception e){
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
                        if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                            bRefreshing = false;
                    }
                }
            }
            if ((pendingWorkoutList != null) && (pendingWorkoutSetList.size() > 0)) {
                for (WorkoutSet pendingWorkoutSet : pendingWorkoutSetList) {             // read existing loaded data...
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
                        if (task.isSuccessful()){
                            bRefreshing = false;
                        }else{
                            handleTaskError(task);
                        }
                        waitStart = SystemClock.elapsedRealtime();
                        while (bRefreshing) {
                            Log.w(LOG_TAG, "sleeping on get existing refresh");
                            SystemClock.sleep(5000);
                            if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                                bRefreshing = false;
                        }
                    }
                    catch (Exception e){
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
                            if (existingWorkout != null){
                                if (existingWorkout.last_sync > 0){
                                    pendingWorkoutSet.last_sync = existingWorkout.last_sync;
                                    pendingWorkoutSet.lastUpdated = timeMs;
                                    workoutRepository.updateWorkoutSet(pendingWorkoutSet);
                                }
                            }else{
                                Workout finder = workoutRepository.getWorkoutByIdNow(pendingWorkoutSet.workoutID,pendingWorkoutSet.userID,pendingWorkoutSet.deviceID);
                                if (finder != null){
                                    if (finder.last_sync > 0){
                                        pendingWorkoutSet.last_sync = finder.last_sync;
                                        pendingWorkoutSet.lastUpdated = timeMs;
                                        workoutRepository.updateWorkoutSet(pendingWorkoutSet);
                                    }
                                }
                            }

                        }
                    }
                } // for each pending set
            }
            if ((gymCount > 0) || (stepCount > 0) || (activityCount > 0)) {
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
                    for (Workout w: pendingWorkoutList){
                        if (w.last_sync == 0) {
                            w.last_sync = timeMs;
                            w.lastUpdated = timeMs;
                            workoutRepository.updateWorkout(w);
                        }
                        if (!Utilities.isGymWorkout(w.activityID)){
                            if ((pendingWorkoutSetList != null) && pendingWorkoutSetList.size() > 0)
                                for (WorkoutSet set: pendingWorkoutSetList){
                                    if (set.workoutID == w._id){
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
                    for (WorkoutSet s: pendingWorkoutSetList){
                        if (s.last_sync == 0) {
                            s.last_sync = timeMs;
                            s.lastUpdated = timeMs;
                            workoutRepository.updateWorkoutSet(s);
                        }
                    }
                }
                Log.e(LOG_TAG, "finishing UPLOAD" +  " w " + iWorkouts + " s " + iWorkoutSets + " steps " + iSteps);
                if (iWorkouts > 0 && (pendingWorkoutList != null)){
                    for(Workout pendingW: pendingWorkoutList){
                        Log.e(LOG_TAG, "UPLOADED " +  pendingW.toString());
                    }
                }
            }else // end of if gymCount stepCount activityCount
                Log.w(LOG_TAG, "finishing UPLOAD NO DATA");
        }


        // now Load FROM Google - get the dates from Config HistoryRange
        if ((configList != null) && (configList.size() > 0)){
            startTime = Long.parseLong(configHistory.stringValue2); // last end is now our start
            if (startTime == 0){
                mCalendar.setTimeInMillis(timeMs);
                mCalendar.add(Calendar.DAY_OF_YEAR,-14);
                startTime = mCalendar.getTimeInMillis();
            }
        }
        else{
            mCalendar.setTimeInMillis(timeMs);
            mCalendar.add(Calendar.DAY_OF_YEAR,-14);
            startTime = Utilities.getDayStart(mCalendar,mCalendar.getTimeInMillis());
        }
        endTime = timeMs;
        // now the DOWNLOAD
        iWorkouts = 0; iWorkoutSets = 0;
        if ((endTime - startTime) >= syncInterval) {
            List<Workout> wL = workoutRepository.getWorkoutsNow(sUserID, Constants.ATRACKIT_EMPTY, startTime, endTime); // ALL NEEDED!
            if ((wL != null) && (wL.size() > 0))
                existingWorkoutArrayList.addAll(wL);
            List<WorkoutSet> sL = workoutRepository.getWorkoutSetsNow(sUserID, Constants.ATRACKIT_EMPTY, startTime, endTime);  // ALL NEEDED! - no deviceID
            if ((sL != null) && (sL.size() > 0))
                existingWorkoutSetArrayList.addAll(sL);

            FitnessOptions fo = referencesTools.getFitnessSignInOptions(2); // read sesssions
            GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(context,fo);

            if (gsa == null || (gsa.isExpired() || ((sUserID.length() > 0) && !sUserID.equals(gsa.getId())))){
                Log.e(LOG_TAG,"no previous login account ");
                doGetSilentGoogleAccountCallable getSilentCallable = new doGetSilentGoogleAccountCallable(context);
                GoogleSignInAccount googleSignInAccount = null;
                try {
                    Future<GoogleSignInAccount> future = AppExecutors.getInstance().diskService().submit(getSilentCallable);
                    googleSignInAccount = future.get(4,TimeUnit.MINUTES);
                    if (googleSignInAccount == null) {
                        Log.e(LOG_TAG,"fail to silent sign-in ");
                        return Result.failure();
                    }
                    Log.w(LOG_TAG,"successful silent sign-in " + googleSignInAccount.getDisplayName());
                    gsa = googleSignInAccount;
                } catch (ExecutionException e) {
                    Log.e(LOG_TAG,"fail execution exception " + e.getMessage());
                    e.printStackTrace();
                    return Result.failure();
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG,"fail interrupted exception " + e.getMessage());
                    e.printStackTrace();
                    return Result.failure();
                } catch (IllegalStateException e) {
                    Log.e(LOG_TAG,"fail illegal state exception " + e.getMessage());
                    e.printStackTrace();
                    return Result.failure();
                } catch (TimeoutException e) {
                    Log.e(LOG_TAG,"fail timeout exception " + e.getMessage());
                    e.printStackTrace();
                    return Result.failure();
                }

            }
            if (gsa == null){
                return Result.failure();
            }
            // read segments
            bRefreshing = true;
            Task task = null;
                DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_ACTIVITY_SEGMENT)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketBySession(3, TimeUnit.MINUTES)
                .setLimit(1000)
                .enableServerQueries().build();// Used to retrieve data from cloud.*/
            Task<DataReadResponse> response2 = Fitness.getHistoryClient(context, gsa).readData(readRequest);
            response2.addOnCompleteListener(summaryReadCompleter);
            try {

               task = Tasks.whenAllComplete(response2);
               if (task.isSuccessful()){
                   bRefreshing = false;
               }else{
                   handleTaskError(task);
               }
                waitStart = SystemClock.elapsedRealtime();
                while (bRefreshing) {
                    Log.w(LOG_TAG, "sleeping on TYPE_ACTIVITY_SEGMENT refresh");
                    SystemClock.sleep(5000);
                    if ((SystemClock.elapsedRealtime() - waitStart) > TimeUnit.MINUTES.toMillis(5))
                        bRefreshing = false;
                }
            } catch (Exception e) {
                ApiException aei = (ApiException)response2.getException();
                if (aei.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_REQUIRED){
                    handleTaskError(task);
                }
                e.printStackTrace();
                bRefreshing = false;
            }
            // now LOAD FROM Google lists
            synchronized ((fitWorkoutList)) {
                Log.e(LOG_TAG, "about to process " + fitWorkoutList.size());
                ListIterator<Workout> workoutIterator = fitWorkoutList.listIterator(); // find exercises for gym
                while (workoutIterator.hasNext()) {
                    Workout w = workoutIterator.next();
                    if (isExistingOrOverLapping(w)) {
                        Log.e(LOG_TAG, "Existing workout " + referencesTools.workoutShortText(w));
                        workoutIterator.remove();
                    } else {
                        w.deviceID = sDeviceID2; // not from us !
                        mWorkout = w;
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
                            Log.e(LOG_TAG, "about read exercise " + ATRACKIT_SPACE + Utilities.getTimeString(startSet) + ATRACKIT_SPACE + Utilities.getTimeString(endSet));
                            Task<DataReadResponse> exerciseTask = Fitness.getHistoryClient(context, gsa).readData(readExerciseRequest);
                            exerciseTask.addOnCompleteListener(exerciseResponse);
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
                }  // for each workout
                ListIterator wIterator = fitWorkoutList.listIterator();
                while (wIterator.hasNext()) {
                    Workout w = (Workout) wIterator.next();
                    if ((w._id > 0) && (w.userID.length() > 0)) {
                        Workout finder = workoutRepository.getWorkoutByIdNow(w._id, w.userID, Constants.ATRACKIT_EMPTY);
                        if (finder == null) {
                            Log.w(LOG_TAG, "adding new WORKOUT  " + referencesTools.workoutShortText(w));
                            workoutRepository.insertWorkout(w);
                            iWorkouts += 1;
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
                            Log.e(LOG_TAG, "found existing anyway " + finder.toString());
                        }
                    }
                }
            }
            synchronized (fitWorkoutSetList) {
                if (fitWorkoutSetList.size() > 0)
                    for (WorkoutSet s : fitWorkoutSetList) {
                        boolean bValid = s.isValid(true);
                        if ((s._id > 0) && (s.userID.length() > 0)) {
                            WorkoutSet finderSet = workoutRepository.getWorkoutSetByIdNow(s._id, s.userID, Constants.ATRACKIT_EMPTY);
                            if (finderSet == null) {
                                Log.w(LOG_TAG, "adding new SET  " + bValid + " " + referencesTools.workoutSetShortText(s));
                                workoutRepository.insertWorkoutSet(s);
                                iWorkoutSets += 1;
                            } else
                                Log.w(LOG_TAG, "found existing SET anyway " + bValid + " " + referencesTools.workoutSetShortText(finderSet));
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
                if (bUpdate) workoutRepository.updateConfig(configHistory);
                Log.e(LOG_TAG, "updating History Config " + configHistory.toString());
            } else {
                Configuration conf = new Configuration();
                conf.stringName = MAP_HISTORY_RANGE;
                conf.userValue = sUserID;
                conf.stringValue = sDeviceID;
                conf.stringValue1 = Long.toString(startTime);
                conf.stringValue2 = Long.toString(endTime);
                workoutRepository.updateConfig(conf);
                Log.e(LOG_TAG, "Inserting History Config " + configHistory.toString());
            }
            Log.w(LOG_TAG, "finishing" + " total " + " w " + iWorkouts + " s " + iWorkoutSets);
            if (iWorkouts == 1 && fitWorkoutList.size() > 0) Log.e(LOG_TAG, "w " + fitWorkoutList.get(0).toString());
        } // if (endTime - startTime > LastSyncInterval)
        workoutRepository.destroyInstance();

        Data outputData = new Data.Builder()
                .putInt(Constants.KEY_RESULT, iWorkouts + iWorkoutSets)
                .putInt(Workout.class.getSimpleName(), iWorkouts)
                .putInt(WorkoutSet.class.getSimpleName(), iWorkoutSets)
                .putString(Constants.KEY_FIT_USER, sUserID)
                .putString(Constants.KEY_LIST_SETS, sExerciseList)
                .putString(Constants.KEY_FIT_VALUE, LOG_TAG)
                .build();
        return Result.success(outputData);
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
}
