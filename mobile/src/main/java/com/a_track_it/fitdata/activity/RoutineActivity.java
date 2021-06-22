package com.a_track_it.fitdata.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkManager;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.adapter.NameListAdapter;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Bodypart;
import com.a_track_it.fitdata.common.data_model.DateTuple;
import com.a_track_it.fitdata.common.data_model.Exercise;
import com.a_track_it.fitdata.common.data_model.FitSyncJobIntentService;
import com.a_track_it.fitdata.common.data_model.FitnessMetaWorker;
import com.a_track_it.fitdata.common.data_model.SessionCleanupWorker;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutMeta;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModel;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.a_track_it.fitdata.common.user_model.MessagesViewModel;
import com.a_track_it.fitdata.common.user_model.SavedStateViewModel;
import com.a_track_it.fitdata.fragment.CustomAlertDialog;
import com.a_track_it.fitdata.fragment.CustomConfirmDialog;
import com.a_track_it.fitdata.fragment.CustomListDialogFragment;
import com.a_track_it.fitdata.fragment.CustomListFragment;
import com.a_track_it.fitdata.fragment.CustomScoreDialogFragment;
import com.a_track_it.fitdata.fragment.FragmentInterface;
import com.a_track_it.fitdata.fragment.ICustomConfirmDialog;
import com.a_track_it.fitdata.fragment.OnCustomListItemSelectedListener;
import com.a_track_it.fitdata.fragment.RoutineFragment;
import com.a_track_it.fitdata.fragment.ShootingEntryFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.fitdata.common.Constants.ATRACKIT_EMPTY;
import static com.a_track_it.fitdata.common.Constants.FLAG_BUILDING;
import static com.a_track_it.fitdata.common.Constants.FLAG_NON_TRACKING;
import static com.a_track_it.fitdata.common.Constants.FLAG_PENDING;
import static com.a_track_it.fitdata.common.Constants.INTENT_ACTIVESET_START;
import static com.a_track_it.fitdata.common.Constants.INTENT_ACTIVE_START;
import static com.a_track_it.fitdata.common.Constants.INTENT_ACTIVE_STOP;
import static com.a_track_it.fitdata.common.Constants.INTENT_EXTRA_MSG;
import static com.a_track_it.fitdata.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.fitdata.common.Constants.INTENT_QUIT_APP;
import static com.a_track_it.fitdata.common.Constants.INTENT_WORKOUT_EDIT;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_ACTION;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_HOST;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_USER;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_VALUE;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_WORKOUTID;
import static com.a_track_it.fitdata.common.Constants.LABEL_DEVICE_USE;
import static com.a_track_it.fitdata.common.Constants.QUESTION_DELETE_SET;
import static com.a_track_it.fitdata.common.Constants.QUESTION_HISTORY_CREATE;
import static com.a_track_it.fitdata.common.Constants.SELECTION_BODYPART;
import static com.a_track_it.fitdata.common.Constants.SELECTION_BODY_REGION;
import static com.a_track_it.fitdata.common.Constants.SELECTION_CALL_DURATION;
import static com.a_track_it.fitdata.common.Constants.SELECTION_END_DURATION;
import static com.a_track_it.fitdata.common.Constants.SELECTION_EXERCISE;
import static com.a_track_it.fitdata.common.Constants.SELECTION_GOAL_DURATION;
import static com.a_track_it.fitdata.common.Constants.SELECTION_INCOMPLETE_DURATION;
import static com.a_track_it.fitdata.common.Constants.SELECTION_REST_DURATION_TARGET;
import static com.a_track_it.fitdata.common.Constants.SELECTION_TO_DO_SETS;
import static com.a_track_it.fitdata.common.Constants.SINGLE_INT;
import static com.a_track_it.fitdata.common.Constants.TASK_ACTION_INSERT_HISTORY;
import static com.a_track_it.fitdata.common.Constants.TASK_ACTION_READ_HISTORY;
import static com.a_track_it.fitdata.common.Constants.USER_PREF_USE_SET_TRACKING;

public class RoutineActivity extends BaseActivity  implements IEntityFragmentActivityCallback,
        FragmentInterface, OnCustomListItemSelectedListener,
        CustomScoreDialogFragment.onCustomScoreSelected {
    private static final String LOG_TAG = "RoutineActivity";
    public static final String ARG_WORKOUT_ID = "ARG_WORKOUT_ID";
    public static final String ARG_WORKOUTSET_ID = "ARG_WORKOUTSET_ID";
    public static final String ARG_WORKOUT_OBJECT = "ARG_WORKOUT_OBJECT";
    public static final String ARG_WORKOUT_SET_OBJECT = "ARG_WORKOUTSET_OBJECT";
    public static final String ARG_WORKOUT_META_OBJECT = "ARG_WORKOUT_META_OBJECT";
    public static final String ARG_EXERCISE_OBJECT = "ARG_EXERCISE_OBJECT";
    public static final String ARG_WORKOUT_SET_LIST= "ARG_WORKOUT_SET_LIST";
    public static final String ARG_RETURN_RESULT = "ARG_RETURN_RESULT";
    public static final String ARG_REQUEST_SEND = "ARG_REQUEST_SEND";
    private static final long MIN_CLICK_THRESHOLD = 1000L;
    public static final int REQUEST_OAUTH_REQUEST_CODE = 5003;
    private static final int REQUEST_RECORDING_PERMISSION_CODE = 5007;
    ActivityResultLauncher<Intent> exerciseActivityResultLauncher;
    ActivityResultLauncher<Intent> microphoneActivityResultLauncher;
    private MessagesViewModel mMessagesViewModel;
    private SavedStateViewModel mSavedStateViewModel;
    private WorkoutViewModel mSessionViewModel;
    private ApplicationPreferences appPrefs;
    private UserPreferences userPrefs;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Workout mWorkout;
    private WorkoutSet mWorkoutSet;
    private WorkoutMeta mWorkoutMeta;
    private Exercise mSetExercise;
    private CustomConfirmDialog confirmDialog;
    private long mLastClickTime;
    private long backPressedTime;
    private boolean mDismissed = false;
    private boolean bUseKG = false;
    private Integer returnResultFlag;
    private WorkManager mWorkManager;
    private View container;
    private Menu mMenu;
    private ActionBar actionBar;
    private long selectedSetID;
    final private ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                mMessagesViewModel.setWorkInProgress(false);
                if (resultCode == 200) {
                    int historyAction = (resultData.containsKey(Constants.KEY_FIT_ACTION))
                            ? resultData.getInt(Constants.KEY_FIT_ACTION) : 0;
                    boolean action_successful = (resultData.getInt(KEY_FIT_VALUE,0) == 1);
                    if (historyAction == TASK_ACTION_INSERT_HISTORY) {
                        Workout w = resultData.getParcelable(Workout.class.getSimpleName());
                        if (!action_successful){
                            String sMsg = "Unable to add history for " + Utilities.getTimeDateString(w.start) + Constants.ATRACKIT_SPACE + w.activityName;
                            broadcastToast(sMsg);
                        }else{
                            // verify by reading the history
                            if (w != null) {
                                // clean-up sets removing not started
                                //List<OneTimeWorkRequest> requestList = new ArrayList<>();
                                Data.Builder builder = new Data.Builder();
                                builder.putString(KEY_FIT_USER, w.userID);
                                builder.putString(KEY_FIT_DEVICE_ID, w.deviceID);
                                builder.putLong(Constants.KEY_FIT_WORKOUTID, w._id);
                                OneTimeWorkRequest oneTimeWorkRequest =
                                        new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                                                .setInputData(builder.build())
                                                .build();
                                WorkManager mWorkManager = WorkManager.getInstance(getApplicationContext());
                                mWorkManager.beginWith(oneTimeWorkRequest).enqueue();

                                Intent mIntent = new Intent(getApplicationContext(), FitSyncJobIntentService.class);
                                mIntent.putExtra(Constants.KEY_FIT_ACTION, Constants.TASK_ACTION_READ_HISTORY);
                                mIntent.putExtra(Constants.MAP_START, w.start);
                                mIntent.putExtra(Constants.MAP_END, w.end);
                                mIntent.putExtra(Constants.KEY_RESULT, resultReceiver);
                                mIntent.putExtra(KEY_FIT_USER, w.userID);
                                mIntent.putExtra(KEY_FIT_DEVICE_ID, w.deviceID);
                                mIntent.putExtra(KEY_FIT_TYPE, TASK_ACTION_INSERT_HISTORY); // previous action - ensuring correct processing of read
                                mIntent.putExtra(Workout.class.getSimpleName(), w);

                                SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_24Full, Locale.getDefault());
                                Log.w(LOG_TAG, "insertHistory " + w.activityName + " Start: " + dateFormat.format(w.start));
                                Log.w(LOG_TAG, "End: " + dateFormat.format(w.end) + " id " + w._id);
                                mMessagesViewModel.setWorkType(Constants.TASK_ACTION_READ_CLOUD);
                                mMessagesViewModel.setWorkInProgress(true);
                                try {
                                    ReferencesTools ref = ReferencesTools.setInstance(getApplicationContext());
                                    FitnessOptions fitnessOptions = ref.getFitnessSignInOptions(1);
                                    GoogleSignInAccount mGoogleAccount = GoogleSignIn.getAccountForExtension(getApplicationContext(), fitnessOptions);
                                    if (mGoogleAccount != null) mIntent.putExtra(Constants.KEY_PAYLOAD, (Parcelable)mGoogleAccount);
                                    FitSyncJobIntentService.enqueueWork(getApplicationContext(), mIntent);
                                } catch (Exception e) {
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                }
                            }
                        }

                    }

                    if (historyAction == TASK_ACTION_READ_HISTORY){
                        int prevAction = resultData.containsKey(KEY_FIT_TYPE) ? resultData.getInt(KEY_FIT_TYPE) : 0;
                        if (action_successful){
                            Workout w = null;
                            if (prevAction > 0 && (prevAction == TASK_ACTION_INSERT_HISTORY))
                                if (resultData.containsKey(Constants.KEY_LIST_WORKOUTS)){
                                    List<Workout> list = resultData.getParcelableArrayList(Constants.KEY_LIST_WORKOUTS);
                                    if (list.size() > 0) w = list.get(0);
                                }
                                else
                                    if (resultData.containsKey(Workout.class.getSimpleName()))
                                        w = resultData.getParcelable(Workout.class.getSimpleName());
                                if (w != null){
                                    Log.i(LOG_TAG,"workout received from read after history " + w._id + " " + w.toString());

                                 //   requestList.add(workRequest);
                                    Data.Builder builder2 = new Data.Builder();
                                    builder2.putInt(FitnessMetaWorker.ARG_ACTION_KEY, Constants.TASK_ACTION_STOP_SESSION); // all
                                    builder2.putLong(FitnessMetaWorker.ARG_START_KEY, w.start);
                                    builder2.putLong(FitnessMetaWorker.ARG_END_KEY, w.end);
                                    builder2.putLong(FitnessMetaWorker.ARG_WORKOUT_ID_KEY, w._id);
                                    builder2.putLong(FitnessMetaWorker.ARG_SET_ID_KEY, 0);
                                    builder2.putString(KEY_FIT_USER, w.userID);
                                    builder2.putString(KEY_FIT_DEVICE_ID, w.deviceID);
                                    Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                                    OneTimeWorkRequest oneTimeWorkRequest =
                                            new OneTimeWorkRequest.Builder(FitnessMetaWorker.class)
                                                    .setInputData(builder2.build())
                                                    .setInitialDelay(20, TimeUnit.SECONDS)
                                                    .setConstraints(constraints).addTag(LOG_TAG)
                                                    .build();
                                    final Workout workOut = w;
                                    WorkManager mWorkManager = WorkManager.getInstance(getApplicationContext());
                                    mWorkManager.enqueue(oneTimeWorkRequest).getState().observe(RoutineActivity.this, new Observer<Operation.State>() {
                                        @Override
                                        public void onChanged(Operation.State state) {
                                            if (state instanceof Operation.State.SUCCESS) {
                                                mSessionViewModel.updateLastUpdate(workOut.userID,workOut.deviceID, workOut._id,Constants.OBJECT_TYPE_WORKOUT,2);
                                                Intent refreshIntent = new Intent(Constants.INTENT_HOME_REFRESH);
                                                refreshIntent.putExtra(KEY_FIT_ACTION, Constants.KEY_CHANGE_STATE);
                                                refreshIntent.putExtra(KEY_FIT_USER, workOut.userID);
                                                refreshIntent.putExtra(KEY_FIT_VALUE, Constants.WORKOUT_PENDING);
                                                mMessagesViewModel.addLiveIntent(refreshIntent);
                                                //doSnackbar(getString(R.string.action_report_completed), Snackbar.LENGTH_SHORT);
                                            }

                                        }
                                    });
                                }
                        }
                    }
                }
            }
        };
    final private ICustomConfirmDialog confirmDialogCallback = new ICustomConfirmDialog() {
        @Override
        public void onCustomConfirmButtonClicked(int question, int button) {
            if (question == QUESTION_HISTORY_CREATE ) {
                if (button > 0) {
                    Workout workout = confirmDialog.getWorkout();
                    long startTime = workout.start;
                    long endTime = workout.end;
                    long duration = workout.duration;
                    long rest_duration = (Utilities.isGymWorkout(workout.activityID)) ? workout.rest_duration :  0;
                    if (workout.packageName.length() == 0) workout.packageName = Constants.ATRACKIT_ATRACKIT_CLASS;
                    final long timeMs = System.currentTimeMillis();
                    final long originalWorkoutID = mWorkout._id;
                    List<WorkoutSet> sets = new ArrayList<>();
                    boolean bIsGym = Utilities.isGymWorkout(mWorkout.activityID);
                    boolean bIsShooting = Utilities.isShooting(mWorkout.activityID);
                    DateTuple setsTuple = mSessionViewModel.getWorkoutSetTupleByWorkoutID(mWorkout.userID,mWorkout.deviceID,mWorkout._id);
                    if ((setsTuple.sync_count > 0)){
                        sets = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkout._id, mWorkout.userID, mWorkout.deviceID);
                        if (mWorkoutSet == null) mWorkoutSet = sets.get(0);
                        mWorkout.setCount = sets.size();
                    }else {
                        if (mWorkoutSet == null) {
                            createWorkoutSet();
                        }
                        sets.add(mWorkoutSet);
                        mWorkout.setCount = 1;
                    }
                    if (mWorkoutMeta == null){
                        createWorkoutMeta();
                    }
                    // setup the parent
                    // create new from existing workout or from template
                    if ((mWorkout.parentID != null) && (mWorkout.parentID < 0) && (originalWorkoutID != 1))
                        mWorkout.parentID = mWorkout._id;
                    SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_24Full, Locale.getDefault());

                    mWorkout._id = timeMs;
                    mWorkout.start = startTime;
                    if (!bIsGym) mWorkout.end = mWorkout.start + mWorkout.duration;
                    mWorkout.lastUpdated = timeMs;
                    mSessionViewModel.insertWorkout(mWorkout);
                    Log.w(LOG_TAG, "insertHistory  Workout  Start: " + dateFormat.format(workout.start));
                    long lTemp = startTime;
                    if (sets.size() > 0) {
                        int defaultSet = 1;
                        for (WorkoutSet set : sets) {
                            set.workoutID = mWorkout._id;
                            set.userID = mWorkout.userID;
                            set.deviceID = mWorkout.deviceID;
                            set.scoreTotal = Constants.FLAG_PENDING;
                            set.setCount = defaultSet++;
                            set.start = lTemp;
                            set.duration = duration;
                            set.end = set.start + set.duration;
                            set.rest_duration = rest_duration;
                            Log.w(LOG_TAG, "insertHistory  Set  Start: " + dateFormat.format(set.start) + " end "  + dateFormat.format(set.end) + " duration " + set.duration);
                            set.realElapsedStart = 0L;
                            set.realElapsedEnd = 0L;
                            set.lastUpdated = timeMs;
                            mSessionViewModel.insertWorkoutSet(set);
                            lTemp  = set.end + ((set.rest_duration != null) ? set.rest_duration : 0);

                        }
                        mWorkout.end = sets.get(sets.size()-1).end; // last sets endtime
                        mWorkout.duration = mWorkout.end - mWorkout.start;
                        mSessionViewModel.updateWorkout(mWorkout);
                    }
                    else {
                        mWorkoutSet.workoutID = mWorkout._id;
                        mWorkoutSet.userID = mWorkout.userID;
                        mWorkoutSet.deviceID = mWorkout.deviceID;
                        mWorkoutSet.start = timeMs;
                        if (mWorkout.end == 0 && mWorkout.duration > 0) mWorkout.end = mWorkout.start + mWorkout.duration;
                        mWorkoutSet.end = mWorkout.end;
                        mWorkoutSet.lastUpdated = timeMs;
                        mWorkoutSet.scoreTotal = Constants.FLAG_PENDING;
                        mSessionViewModel.insertWorkoutSet(mWorkoutSet);
                    }
                    if ((bIsGym || bIsShooting) && sets.size() > 0) {
                        mWorkout.end = sets.get(sets.size() - 1).end;
                        mSessionViewModel.updateWorkout(mWorkout);
                    }

                    // can now delete the workoutID 1 sets
                 //   if (originalWorkoutID == 1L)
                 //       mSessionViewModel.deleteWorkoutSetByWorkoutID(1L);

                    mWorkoutMeta.start = mWorkout.start;
                    mWorkoutMeta.userID = mWorkout.userID;
                    mWorkoutMeta.deviceID = mWorkout.deviceID;
                    String sPartDay = Utilities.getPartOfDayString(timeMs);
                    mWorkoutMeta.description = sPartDay + Constants.ATRACKIT_SPACE + mWorkout.activityName;
                    // room inserts via viewModel - repository chain
                    mSessionViewModel.insertWorkoutMeta(mWorkoutMeta);
                    mWorkoutSet.start = mWorkout.start;
                    if ((mWorkoutSet.activityID == null) || (mWorkoutSet.activityID != mWorkout.activityID)) {
                        mWorkoutSet.activityID = mWorkout.activityID;
                        mWorkoutSet.activityName = mWorkout.activityName;
                    }
                    if (mFirebaseAnalytics == null) mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());

                    Bundle paramsA = new Bundle();
                    paramsA.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.FB_WORKOUT_HISTORY);
                    paramsA.putString(FirebaseAnalytics.Param.ITEM_ID, Long.toString(mWorkout.activityID));
                    paramsA.putString(FirebaseAnalytics.Param.ITEM_NAME, mWorkout.activityName);
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, paramsA);
                    if (Utilities.isGymWorkout(mWorkout.activityID) || Utilities.isShooting(mWorkout.activityID)) {
                        if ((mWorkoutSet.exerciseName != null) && (mWorkoutSet.exerciseName.length() > 0))
                            doSnackbar(getString(R.string.action_add_history)+ Constants.ATRACKIT_SPACE+ mWorkoutSet.exerciseName, Snackbar.LENGTH_SHORT);
                        else
                            doSnackbar(getString(R.string.action_add_history)+ Constants.ATRACKIT_SPACE+ mWorkoutSet.activityName, Snackbar.LENGTH_SHORT);
                    }else
                        doSnackbar(getString(R.string.action_add_history)+ Constants.ATRACKIT_SPACE+ mWorkout.activityName, Snackbar.LENGTH_SHORT);
                    try{
                        Intent mIntent = new Intent(getApplicationContext(), FitSyncJobIntentService.class);
                        mIntent.putExtra(Constants.KEY_FIT_ACTION, Constants.TASK_ACTION_INSERT_HISTORY);
                        mIntent.putExtra(Constants.MAP_START, startTime);
                        mIntent.putExtra(Constants.MAP_END, endTime);
                        mIntent.putExtra(Constants.KEY_RESULT, resultReceiver);
                        mIntent.putExtra(KEY_FIT_USER, workout.userID);
                        mIntent.putExtra(KEY_FIT_DEVICE_ID, workout.deviceID);
                        mIntent.putExtra(Workout.class.getSimpleName(), workout);

                        Log.w(LOG_TAG, "insertHistory " + Constants.TASK_ACTION_INSERT_HISTORY + " Range Start: " + dateFormat.format(workout.start));
                        Log.w(LOG_TAG, "Range End: " + dateFormat.format(workout.end));
                        mMessagesViewModel.setWorkType(TASK_ACTION_INSERT_HISTORY);
                        mMessagesViewModel.setWorkInProgress(true);
                        ReferencesTools ref = ReferencesTools.setInstance(getApplicationContext());
                        FitnessOptions fitnessOptions = ref.getFitnessSignInOptions(1);
                        GoogleSignInAccount mGoogleAccount = GoogleSignIn.getAccountForExtension(getApplicationContext(), fitnessOptions);
                        if (mGoogleAccount != null) mIntent.putExtra(Constants.KEY_PAYLOAD, (Parcelable)mGoogleAccount);
                        FitSyncJobIntentService.enqueueWork(getApplicationContext(), mIntent);
                    }catch (Exception e){
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
                confirmDialog.dismiss();
            }
            if (question == QUESTION_DELETE_SET){
                if ((button > 0) && (selectedSetID > 0)){
                    WorkoutSet set = mSessionViewModel.getWorkoutSetById(selectedSetID,mWorkout.userID,mWorkout.deviceID);
                    if (set != null){
                        // update workout sets where greater than current.
                        mSessionViewModel.updateWorkoutSetSetCount(set.workoutID, set.setCount,-1); // subtract one
                        mSessionViewModel.deleteWorkoutSet(set);
                        broadcastToast(getString(R.string.nav_deleted));
                    }
                    selectedSetID = 0;
                }
            }
            if (question == Constants.QUESTION_COPY_SET){
                if ((button > 0) && (selectedSetID > 0)){
                    WorkoutSet set = mSessionViewModel.getWorkoutSetById(selectedSetID,mWorkout.userID,mWorkout.deviceID);
                    if (set != null){
                        int currentIndex = set.setCount;
                        WorkoutSet newSet = new WorkoutSet(set);
                        newSet.setCount = currentIndex + 1;
                        newSet.duration = 0; newSet.pause_duration =0; newSet.scoreTotal = Constants.FLAG_PENDING;
                        if (Utilities.isGymWorkout(set.activityID) || (Utilities.isShooting(set.activityID))){
                            // update workout sets where greater than current.
                            mSessionViewModel.updateWorkoutSetSetCount(set.workoutID, newSet.setCount,1); // subtract one
                            mSessionViewModel.insertWorkoutSet(newSet);
                        }else
                            mSessionViewModel.insertWorkoutSet(newSet);

                        broadcastToast(getString(R.string.nav_copied));
                    }
                    selectedSetID = 0;
                }
            }
        }

        @Override
        public void onCustomConfirmDetach() {
            mSavedStateViewModel.setIsInProgress(0);
        }
    };
    private void showAlertDialogConfirm(final int action){
        String message_text = Constants.ATRACKIT_EMPTY;
        switch (action){
            case Constants.QUESTION_DELETE_SET:
                message_text = getString(R.string.action_delete_set);
                break;
            case Constants.QUESTION_COPY_SET:
                message_text = getString(R.string.action_copy_set);
                break;
            case Constants.ACTION_DELETE_SET:
                message_text = getString(R.string.action_delete_set);
                break;
            case Constants.ACTION_DELETE_WORKOUT:
                message_text = getString(R.string.action_delete_workout);
                break;
            case Constants.ACTION_CANCELLING:
                message_text = getString(R.string.action_cancel);
                break;
            case Constants.ACTION_STOP_QUIT:
            case Constants.ACTION_EXITING:
                message_text = getString(R.string.action_exiting);
                break;
            case Constants.ACTION_SIGNOUT_QUIT:
                message_text = getString(R.string.action_signout);
                break;
            case Constants.QUESTION_NETWORK:
                message_text = getString(R.string.no_network_connection);
                break;
        }
        try {
            if (getSupportFragmentManager().findFragmentByTag(CustomAlertDialog.class.getSimpleName()) == null) {
                CustomAlertDialog alertDialog = CustomAlertDialog.newInstance(action, message_text, confirmDialogCallback);
                alertDialog.setCancelable(true);
                alertDialog.show(getSupportFragmentManager(), CustomAlertDialog.class.getSimpleName());
            }
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "showAlertDialogConfirm " + e.getMessage());
        }
    }


    @Override
    public void onSaveCurrent() {

    }

    @Override
    public void onChangedState(boolean dirty) {
    //    workout_save_button.setEnabled(dirty);
        Log.i(LOG_TAG, "onChangedState dirty:" + dirty);


    }
///////////////////////////////////////
    // LIFE CYCLE
    ///////////////////////////////////////
    @Override
    public void onBackPressed() {
        long t = SystemClock.elapsedRealtime();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0){
            fragmentManager.popBackStack();
            return;
        }
        if (t - backPressedTime > 2000) {    // 2 secs
            backPressedTime = t;
            broadcastToast(getString(R.string.app_check_exit));
        } else {
            boolean bSetup = appPrefs.getAppSetupCompleted();
            String sUserId = appPrefs.getLastUserID();
            int state = mSavedStateViewModel.getState();
            // clean up
            if ((sUserId.length() > 0) && userPrefs.getConfirmExitApp()) {
                setResult(RESULT_CANCELED, getResultIntent(INTENT_QUIT_APP));
            }else {
                if (returnResultFlag > 0)
                    setResult(RESULT_CANCELED, getResultIntent(INTENT_QUIT_APP));
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(ARG_WORKOUT_ID, ((mWorkout != null) ? mWorkout._id : 0));
        if (mWorkout != null){
            outState.putParcelable(Workout.class.getSimpleName(), mWorkout);
            outState.putString(KEY_FIT_USER, mWorkout.userID);
            outState.putString(KEY_FIT_DEVICE_ID, mWorkout.deviceID);
        }
        if (mWorkoutSet != null) outState.putParcelable(WorkoutSet.class.getSimpleName(), mWorkoutSet);
        if (mWorkoutMeta != null) outState.putParcelable(WorkoutMeta.class.getSimpleName(), mWorkoutMeta);
        outState.putInt(ARG_RETURN_RESULT, returnResultFlag);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        appPrefs = ApplicationPreferences.getPreferences(context);
        ReferencesTools mReferenceTools = ReferencesTools.getInstance();
        mReferenceTools.init(context);
        final ActionBar actionBar = getSupportActionBar();
        WorkoutViewModelFactory factory = com.a_track_it.fitdata.common.InjectorUtils.getWorkoutViewModelFactory(getApplicationContext());
        mSessionViewModel = new ViewModelProvider(RoutineActivity.this, factory).get(WorkoutViewModel.class);
        mMessagesViewModel = new ViewModelProvider(RoutineActivity.this).get(MessagesViewModel.class);
        mSavedStateViewModel = new ViewModelProvider(RoutineActivity.this).get(SavedStateViewModel.class);

        long mWorkoutID = 0; String sUserID = ATRACKIT_EMPTY; String sDeviceID = ATRACKIT_EMPTY;
        List<WorkoutSet> list = new ArrayList<>();
        Intent incomingIntent = getIntent();
        if (incomingIntent != null) {
            mWorkoutID = incomingIntent.getExtras().getLong(ARG_WORKOUT_ID, 0);
            sUserID = (incomingIntent.hasExtra(KEY_FIT_USER)) ? incomingIntent.getStringExtra(KEY_FIT_USER) : ATRACKIT_EMPTY;
            userPrefs = UserPreferences.getPreferences(context, sUserID);
            sDeviceID = (incomingIntent.hasExtra(KEY_FIT_DEVICE_ID)) ? incomingIntent.getStringExtra(KEY_FIT_DEVICE_ID) : ATRACKIT_EMPTY;
            // return a result flat
            returnResultFlag = (incomingIntent.hasExtra(ARG_RETURN_RESULT) ? incomingIntent.getIntExtra(ARG_RETURN_RESULT, 0) : 0);
            // if workoutID is 0 - an external load from a notification
            if (incomingIntent.hasExtra(ARG_WORKOUT_OBJECT)) {
                Workout ex = incomingIntent.getParcelableExtra(ARG_WORKOUT_OBJECT);
                if (ex != null) mWorkout = ex;
                Log.w(LOG_TAG, "set workout saved state from intent");
            }
            if (incomingIntent.hasExtra(ARG_WORKOUT_SET_OBJECT)) {
                WorkoutSet ex = incomingIntent.getParcelableExtra(ARG_WORKOUT_SET_OBJECT);
                if (ex != null) mWorkoutSet = ex;
                Log.w(LOG_TAG, "set workout SET saved state from intent");
            }
            if (incomingIntent.hasExtra(ARG_WORKOUT_META_OBJECT)) {
                WorkoutMeta meta = incomingIntent.getParcelableExtra(ARG_WORKOUT_META_OBJECT);
                if (meta != null) mWorkoutMeta = meta;
                Log.w(LOG_TAG, "set workout META saved state from intent");
            }
            if (incomingIntent.hasExtra(ARG_WORKOUT_SET_LIST)) {
                list = incomingIntent.getParcelableArrayListExtra(ARG_WORKOUT_SET_LIST);
                Log.w(LOG_TAG, "set workout LIST " + list.size() + " mSessionViewModel from intent");
            } else {
                list = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkout._id, mWorkout.userID, mWorkout.deviceID);
            }
            if (incomingIntent.hasExtra(KEY_FIT_HOST)) {
                mMessagesViewModel.setWorkInProgress(incomingIntent.getBooleanExtra(KEY_FIT_HOST, false));
            }
        }
        else
            if (savedInstanceState != null){
                mWorkoutID = savedInstanceState.getLong(ARG_WORKOUT_ID,0);
                sUserID = savedInstanceState.getString(KEY_FIT_USER);
                userPrefs = UserPreferences.getPreferences(context, sUserID);
                sDeviceID = savedInstanceState.getString(KEY_FIT_DEVICE_ID);
                mWorkout = savedInstanceState.getParcelable(Workout.class.getSimpleName());
                mWorkoutSet = savedInstanceState.getParcelable(WorkoutSet.class.getSimpleName());
                mWorkoutMeta = savedInstanceState.getParcelable(WorkoutMeta.class.getSimpleName());
                list = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkoutID, sUserID, sDeviceID);
            }
        if (mWorkout == null)
            if ((mWorkoutID > 0) && (sUserID.length() > 0) && (sDeviceID.length() > 0)){
                Workout loadedWorkout = mSessionViewModel.getWorkoutById(mWorkoutID,sUserID, sDeviceID);
                DateTuple setsTuple = mSessionViewModel.getWorkoutSetTupleByWorkoutID(sUserID, sDeviceID,mWorkoutID);
                if (setsTuple.sync_count > 0){
                    mWorkout = loadedWorkout;  // don't load the default - use the intent version
                    list = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkoutID, sUserID, sDeviceID);
                    if ((list != null) && (list.size() > 0)){
                        mWorkoutSet = list.get(0);
                    }
                    createWorkoutMeta();
                }
            }
        Drawable drawableUnChecked = AppCompatResources.getDrawable(context,R.drawable.ic_close_white);
        Utilities.setColorFilter(drawableUnChecked, ContextCompat.getColor(context, R.color.secondaryTextColor));
        if (toolbar != null){
            toolbar.setNavigationIcon(drawableUnChecked);
            //toolbar.setNavigationOnClickListener(v -> OnFragmentInteraction(android.R.id.home,0,null));
        }
        container = findViewById(R.id.workout_placeholder);

        String sUserId = appPrefs.getLastUserID();

        bUseKG = userPrefs.getUseKG();
        int activityIcon=0; int activityColor=0;
        // setup the saved state session info for the first time
        if ((mWorkout != null) && (sUserId.equals(mWorkout.userID))) {
            mSavedStateViewModel.setActiveWorkout(mWorkout);
            mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
            mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
            activityIcon =  mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
            mSavedStateViewModel.setIconID(activityIcon);
            activityColor = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
            mSavedStateViewModel.setColorID(activityColor);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            mSavedStateViewModel.setSetIndex(mWorkoutSet.setCount);
            mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
      //      if ((mWorkoutSet != null) && (mWorkoutSet.setCount != null)) mSavedStateViewModel.setSetIndex(mWorkoutSet.setCount);
            if ((list != null) && (list.size() > 0)) mSavedStateViewModel.setToDoSets(list);
        }
        mSavedStateViewModel.setSetsDefault(userPrefs.getDefaultNewSets());
        mSavedStateViewModel.setRepsDefault(userPrefs.getDefaultNewReps());
        mSavedStateViewModel.getActiveWorkout().observe(RoutineActivity.this, new Observer<Workout>() {
            @Override
            public void onChanged(Workout workout) {
                if (workout != null) {
                    mWorkout = workout;  // set in fragments for setting here!
                }
                prepareOptionsMenu();
            }
        });
        mMessagesViewModel.getLiveIntent().observe(this, new Observer<Intent>() {
            @Override
            public void onChanged(Intent intent) {
                if (intent == null) return;
                final String intentAction = intent.getAction();
                final Context ctx = getApplicationContext();
                switch (intentAction){
                   case INTENT_MESSAGE_TOAST:
                        if (intent.hasExtra(Constants.INTENT_EXTRA_MSG)){
                            final int iType = intent.getIntExtra(KEY_FIT_TYPE,0);
                            final int length = intent.getIntExtra(KEY_FIT_VALUE, Toast.LENGTH_SHORT);
                            String sMessage = intent.getStringExtra(Constants.INTENT_EXTRA_MSG);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast toast = Toast.makeText(ctx, sMessage, length);
                                    // toast.
                                    View view = toast.getView();
                                    TextView toastMessage2 = (TextView) view.findViewById(android.R.id.message);
                                    if (iType == 1) {
                                        view.setBackgroundResource(android.R.drawable.toast_frame);
                                        view.setBackgroundColor(Color.TRANSPARENT);
                                        toastMessage2.setBackground(AppCompatResources.getDrawable(ctx, com.a_track_it.fitdata.common.R.drawable.custom_toast));
                                        toastMessage2.setTextColor(AppCompatResources.getColorStateList(ctx, com.a_track_it.fitdata.common.R.color.white_pressed));
                                    } else {
                                        if (iType == 2) {
                                            view.setBackgroundResource(android.R.drawable.toast_frame);
                                            view.setBackgroundColor(Color.TRANSPARENT);
                                            toastMessage2.setBackground(AppCompatResources.getDrawable(ctx, com.a_track_it.fitdata.common.R.drawable.custom_wear_toast));
                                            toastMessage2.setTextColor(AppCompatResources.getColorStateList(ctx, com.a_track_it.fitdata.common.R.color.secondaryTextColor));
                                        } else
                                            toastMessage2.setTextColor(AppCompatResources.getColorStateList(ctx, android.R.color.black));
                                    }
                                    toast.show();
                                }
                            });
                        }
                        break;
                }
            }
        });
        mMessagesViewModel.getSpokenMsg().observe(RoutineActivity.this, s -> {
            if ((s == null) || (s.length()==0)) return;
            String sTarget = mSavedStateViewModel.getSpeechTarget();
            broadcastToast(s);
            if (sTarget.equals(Constants.TARGET_EXERCISE_NAME) ) {
                List<Exercise> exlist = mSessionViewModel.getExercisesLike(s);
                if ((exlist == null) || (exlist.size() == 0)){
                   Exercise newExercise = new Exercise();
                   newExercise._id = 0; newExercise.name = s;
                   startExerciseActivityForResult(newExercise);
                }else{
                    if (mWorkoutSet != null){
                        mSetExercise = exlist.get(0);
                        if (mSetExercise != null) {
                            int defReps = ((userPrefs != null) ? userPrefs.getDefaultNewReps() : 10);
                            int defSets = ((userPrefs != null) ? userPrefs.getDefaultNewSets() : 3);
                            if (mSetExercise.resistanceType != null)
                                mWorkoutSet.resistance_type = mSetExercise.resistanceType;
                            //    if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                            mSavedStateViewModel.setRepsDefault((mSetExercise.lastReps > 0) ? mSetExercise.lastReps :  defReps);
                            //   if (mWorkoutSet.setCount == 0)
                            mSavedStateViewModel.setSetsDefault((mSetExercise.lastSets > 0) ? mSetExercise.lastSets : defSets);
                            if ((mWorkoutSet.weightTotal != null) && mWorkoutSet.weightTotal == 0)
                                if ((mSetExercise.first_BPID != null) && (mSetExercise.first_BPID > 0)) {
                                    mWorkoutSet.bodypartID = mSetExercise.first_BPID;
                                    mWorkoutSet.bodypartName = mSetExercise.first_BPName;
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Bodypart bodypart = mSessionViewModel.getBodypartById(mSetExercise.first_BPID);
                                              mWorkoutSet.repCount = defReps;
                                              mSavedStateViewModel.setRepsDefault(defReps);
                                              mSavedStateViewModel.setSetsDefault(defSets);
                                            if ((mWorkoutSet.regionID == null)||(mWorkoutSet.regionID == 0)){
                                                mWorkoutSet.regionID = bodypart.regionID;
                                                mWorkoutSet.regionName = bodypart.regionName;
                                            }
                                        }
                                    });
                                }
                            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                            mSavedStateViewModel.setExercise(mSetExercise);
                        }
                    }
                }
            }
            if (sTarget.equals(Constants.TARGET_ROUTINE_NAME) && (mWorkout != null)){
                List<Workout> sessionNames = mSessionViewModel.getWorkoutByName(mWorkout.userID, sTarget);
                if ((sessionNames== null) || (sessionNames.size() == 0)){
                    // prompt confirm name for new session..
                    mWorkoutMeta.description = s;
                    mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                    mWorkout.parentID = -1L;
                    mWorkout.start = 0; mWorkout.end = 0;
                    mWorkout.name = s;
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    doGetTemplateName(false);
                }else {
                    // prompt to load new session
                    broadcastToast("a routine already exists with that name");
                    doGetTemplateName((mWorkout.parentID == null) || (mWorkout.parentID > 0));
                }
            }

        });
        // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
        exerciseActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if ((result.getResultCode() == Activity.RESULT_OK) && (result.getData() != null)) {
                        // There are no request codes
                        Intent data = result.getData();
                        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                        Exercise returnedExercise = data.getParcelableExtra(ExerciseActivity.ARG_EXERCISE_OBJECT);
                        if (returnedExercise != null){
                            mWorkoutSet.exerciseID = returnedExercise._id;
                            mWorkoutSet.exerciseName = returnedExercise.name;
                            mWorkoutSet.per_end_xy = returnedExercise.workoutExercise;
                            int defReps = ((userPrefs != null) ? userPrefs.getDefaultNewReps() : 10);
                            int defSets = ((userPrefs != null) ? userPrefs.getDefaultNewSets() : 3);
                            if (returnedExercise.resistanceType != null)
                                mWorkoutSet.resistance_type = returnedExercise.resistanceType;
                            mSavedStateViewModel.setRepsDefault((returnedExercise.lastReps > 0) ? returnedExercise.lastReps :  defReps);
                            mSavedStateViewModel.setSetsDefault((returnedExercise.lastSets > 0) ? returnedExercise.lastSets : defSets);
                            if ((returnedExercise.first_BPID != null) && (returnedExercise.first_BPID > 0)) {
                                mWorkoutSet.bodypartID = returnedExercise.first_BPID;
                                mWorkoutSet.bodypartName = returnedExercise.first_BPName;
                            }
                            mSavedStateViewModel.setDirtyCount(1);
                            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                        }
                    }
                });

        microphoneActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null){
                            List<String> results = data.getStringArrayListExtra(
                                    RecognizerIntent.EXTRA_RESULTS);
                            final String spokenText = (results != null) ? results.get(0) : Constants.ATRACKIT_EMPTY;
                            if (spokenText.length() > 0) {
                                if (mMessagesViewModel != null){
                                    mMessagesViewModel.addSpokenMsg(spokenText);
                                }
                            }
                        }
                    }
                });

        prepareSupportActionBar();
        if ((mWorkout != null) && (Utilities.isShooting(mWorkout.activityID))) {
            ShootingEntryFragment shootingEntryFragment = ShootingEntryFragment.newInstance(mWorkout, activityIcon, activityColor, mWorkoutSet, mWorkoutMeta);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.workout_placeholder, shootingEntryFragment, ShootingEntryFragment.class.getSimpleName());
            transaction.commit();
        }else{
            RoutineFragment routineFragment = RoutineFragment.create(mWorkoutID, mWorkout, mWorkoutSet, mWorkoutMeta);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.workout_placeholder, routineFragment, RoutineFragment.class.getSimpleName());
            transaction.commit();
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_routine;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORDING_PERMISSION_CODE) {
            if ((permissions != null) && permissions.length != 0)
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String sMsg = getResources().getString(R.string.recording_permission_granted);
                    broadcastToast(sMsg);
                    try {
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        // Start the activity, the intent will be populated with the speech text
                        microphoneActivityResultLauncher.launch(intent);
                    } catch (ActivityNotFoundException anf) {
                        broadcastToast(getString(R.string.action_nospeech_activity));
                    }
                }
        }
    }

    @Override
    public void onItemSelected(int pos, long id, String title, long resid, String identifier) {
        // mis-clicking prevention, using threshold of 1000 ms
        if ((SystemClock.elapsedRealtime() - mLastClickTime) < MIN_CLICK_THRESHOLD){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
    }

    @Override
    public void OnFragmentInteraction(int srcId, long selectedId, String text) {
        String sDefault = "";
        if ((SystemClock.elapsedRealtime() - mLastClickTime) < MIN_CLICK_THRESHOLD)
            return;
        mLastClickTime = SystemClock.elapsedRealtime();
        String sUserID = mWorkout.userID;
        String sDeviceID = mWorkout.deviceID;
        switch (srcId){
            case android.R.id.home:
                if (returnResultFlag > 0) setResult(RESULT_CANCELED, getResultIntent(ATRACKIT_EMPTY));
                ActivityCompat.finishAfterTransition(RoutineActivity.this);
                break;

            //  session entry
            case Constants.UID_btnRegion:
                if ((text != null) && text.equals(Constants.LABEL_LONG)){
                    if (mWorkoutSet != null){
                        if (Utilities.isGymWorkout(mWorkout.activityID)) {  // GYM tag
                            mWorkoutSet.regionName = Constants.ATRACKIT_EMPTY;
                            mWorkoutSet.regionID = null;
                            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                        }
                    }
                }else {
                    if (Utilities.isGymWorkout(mWorkout.activityID))
                        startCustomList(SELECTION_BODY_REGION, sDefault, sUserID, sDeviceID);
                    else{
                        if (mSavedStateViewModel.getToDoSetsSize() > 0){
                            List<WorkoutSet> todo =  ( mSavedStateViewModel.getToDoSets().getValue() != null) ?  mSavedStateViewModel.getToDoSets().getValue() : new ArrayList<>();
                            if (mSavedStateViewModel.getSetIndex() == 0) {
                                mWorkoutSet = todo.get(0);
                                if (mWorkoutSet.setCount > 1) mWorkoutSet.setCount = 1;
                            }else{
                                mWorkoutSet = todo.get(mSavedStateViewModel.getSetIndex()-1);
                            }
                            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                        }
                        sessionStart();
                    }
                }
                break;
          case  Constants.UID_btnBodypart:
                if ((text != null) && text.equals(Constants.LABEL_LONG)){
                    if (mWorkoutSet != null){
                        if (Utilities.isGymWorkout(mWorkout.activityID)) {  // GYM tag
                            mWorkoutSet.bodypartName = Constants.ATRACKIT_EMPTY;
                            mWorkoutSet.bodypartID = null;
                            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                        }else {
                            mWorkout.goal_steps = 0L;
                            mSavedStateViewModel.setActiveWorkout(mWorkout);
                        }
                    }
                }else {
                    if (Utilities.isGymWorkout(mWorkout.activityID))  // GYM tag
                        startCustomList(SELECTION_BODYPART, sDefault, sUserID,sDeviceID);
                    else {
                        sDefault = Long.toString(mWorkout.goal_steps);
                        startCustomList(Constants.SELECTION_GOAL_STEPS, sDefault, sUserID,sDeviceID);
                    }
                }
                break;
            case Constants.UID_btnAddExercise:
                if ((text != null) && text.equals(Constants.LABEL_LONG)) {
                    mSavedStateViewModel.setSpeechTarget(Constants.TARGET_EXERCISE_NAME);
                    displaySpeechRecognizer();
                }else {
                    Exercise newExercise = new Exercise();
                    newExercise._id = 0;
                    newExercise.name = "";
                    startExerciseActivityForResult(newExercise);
                }
                break;
            case Constants.UID_btnExercise:
                if (Utilities.isGymWorkout(mWorkout.activityID)) {
                    if ((text != null) && text.equals(Constants.LABEL_LONG)) { // clear exercise
                        if (mWorkoutSet != null) {
                            mWorkoutSet.exerciseName = Constants.ATRACKIT_EMPTY;
                            mWorkoutSet.per_end_xy = ATRACKIT_EMPTY;
                            mWorkoutSet.exerciseID = null;
                            mSavedStateViewModel.setDirtyCount(1);
                            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                        }
                    }else
                        startCustomList(SELECTION_EXERCISE, sDefault,sUserID,sDeviceID);
                } else{
                   sessionStart();
                }
                break;
            case Constants.UID_btnRoutineName:
                if (Utilities.isGymWorkout(mWorkout.activityID)) {
                    doGetTemplateName((mWorkout.parentID == null) || (mWorkout.parentID > 0));
                }else{
                    if ((text != null) && text.equals(Constants.LABEL_LONG)){
                        mWorkout.goal_duration = 0L;
                        mSavedStateViewModel.setActiveWorkout(mWorkout);
                    }else {
                        sDefault = Long.toString(mWorkout.goal_duration);
                        startCustomList(SELECTION_GOAL_DURATION, sDefault,sUserID,sDeviceID);
                    }
                }
                break;
            case Constants.UID_btnReps:
                startCustomList(Constants.SELECTION_REPS, mSavedStateViewModel.getRepsDefault().toString(), sUserID,sDeviceID);
                break;
            case Constants.UID_btnSets:
                startCustomList(Constants.SELECTION_SETS, mSavedStateViewModel.getSetsDefault().toString(),sUserID,sDeviceID);
                break;
            case Constants.UID_btnWeight:
                if ((text != null) && text.equals(Constants.LABEL_LONG)){
                    if (mWorkoutSet != null){
                        mWorkoutSet.weightTotal = 0F;
                        mSavedStateViewModel.setDirtyCount(1);
                        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    }
                }else {
                    sDefault = Long.toString(0);
                    if ((mWorkoutSet != null) && (mWorkoutSet.resistance_type != null && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY))
                            && (mWorkoutSet.exerciseID != null && mWorkoutSet.exerciseID > 0)){
                        if (mWorkoutSet.weightTotal == null || mWorkoutSet.weightTotal == 0) {
                            Exercise ex = mSessionViewModel.getExerciseById(mWorkoutSet.exerciseID);
                            if (ex.lastSelectedWeight > 0)
                                sDefault = Float.toString(ex.lastSelectedWeight);
                        }else
                            sDefault = Float.toString(mWorkoutSet.weightTotal);
                    }
                    if ((mWorkoutSet != null) && ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type == Field.RESISTANCE_TYPE_BODY)))
                        startCustomList(Constants.SELECTION_WEIGHT_BODYWEIGHT, sDefault,sUserID,sDeviceID);
                    else if (bUseKG)
                        startCustomList(Constants.SELECTION_WEIGHT_KG, sDefault,sUserID,sDeviceID);
                    else
                        startCustomList(Constants.SELECTION_WEIGHT_LBS, sDefault,sUserID,sDeviceID);
                }
                break;

            case Constants.UID_btnBuild:
                mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                if (mWorkoutSet == null) return;
                if (Utilities.isGymWorkout(mWorkoutSet.activityID)) {
                    if (mWorkoutSet.isValid(false)) {
                        if (mWorkoutSet.workoutID <= 2) sessionBuild(true);
                    } else {
                        if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0)) {
                            if (mWorkoutSet.repCount != null && mWorkoutSet.repCount > 0) {
                                if ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type == Field.RESISTANCE_TYPE_BODY))
                                    startCustomList(Constants.SELECTION_WEIGHT_BODYWEIGHT, sDefault, sUserID, sDeviceID);
                                else if (bUseKG)
                                    startCustomList(Constants.SELECTION_WEIGHT_KG, sDefault, sUserID, sDeviceID);
                                else
                                    startCustomList(Constants.SELECTION_WEIGHT_LBS, sDefault, sUserID, sDeviceID);
                            } else
                                startCustomList(Constants.SELECTION_REPS, sDefault, sUserID, sDeviceID);
                        } else
                            startCustomList(SELECTION_EXERCISE, sDefault, sUserID, sDeviceID);
                    }
                }else
                    OnFragmentInteraction(Constants.UID_btnStart,0,null);
                break;

            case Constants.UID_btnStart:
                mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                // finalise a non saved entry first
                if (Utilities.isGymWorkout(mWorkoutSet.activityID)) {
                        if ((!mWorkoutSet.isValid(false)
                                && (mWorkoutSet.scoreTotal != FLAG_NON_TRACKING))) {
                            String sMsg = ATRACKIT_EMPTY;
                            if ((mWorkoutSet.exerciseName == null) || (mWorkoutSet.exerciseName.length() == 0)) {
                                sMsg = getString(R.string.edit_set_exercise);
                            } else {
                                if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0)) {
                                    sMsg = getString(R.string.edit_set_reps);
                                } else if ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY) && ((mWorkoutSet.weightTotal == null) || (mWorkoutSet.weightTotal == 0F))) {
                                    sMsg = getString(R.string.edit_set_weight);
                                }
                            }
                            if (sMsg.length() > 0)
                                doSnackbar(sMsg, Snackbar.LENGTH_SHORT);
                            return;
                        }
                        else
                            if (mWorkoutSet.scoreTotal != FLAG_NON_TRACKING)
                                if (mWorkoutSet.isValid(false) && (mWorkoutSet.workoutID <=2))
                                    sessionBuild(false);
                                else
                                    mSessionViewModel.deleteWorkoutSet(mWorkoutSet);

                }else{
                    if (mWorkoutSet.isValid(false) && (mWorkoutSet.workoutID <=2)) {
                        sessionBuild(false);
                    }
                }
                sessionStart();
                break;
            case Constants.UID_btnSaveHistory:
                mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
                if (mWorkout == null) return;
                mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                if (mWorkoutSet.isValid(false) && mSavedStateViewModel.getDirtyCount() > 0)
                    sessionBuild(false);
                else
                    if (!mWorkoutSet.isValid(false)) mSessionViewModel.deleteWorkoutSet(mWorkoutSet);
                doConfirmDialog(QUESTION_HISTORY_CREATE,mWorkout.activityName,confirmDialogCallback);
                break;
            case Constants.UID_toggle_track_activity:
                MenuItem menuDeviceItem = mMenu.findItem(R.id.action_routine_device);
                if (menuDeviceItem != null)
                    menuDeviceItem.setVisible((selectedId > 0));
                MenuItem menuTemplate = mMenu.findItem(R.id.action_template);
                if (menuTemplate != null)
                    menuTemplate.setVisible((selectedId > 0));
                prepareSupportActionBar();
                break;
            case Constants.UID_btnFinish:
                if (mWorkout.start > 0)
                    sessionEnd();
                break;
            case Constants.UID_btnRest:
                if ((text != null) && text.equals(Constants.LABEL_LONG)){
                    if (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null){
                        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                        mWorkoutSet.rest_duration = 0L;
                        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    }
                }else{
                    if (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null){
                        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                        startCustomList(Constants.SELECTION_REST_DURATION_GYM,String.format(Locale.getDefault(),SINGLE_INT, (mWorkoutSet.rest_duration != null) ?  mWorkoutSet.rest_duration : 0),sUserID,sDeviceID);
                    }
                }
                break;
            case Constants.UID_btn_recycle_item_delete:
                selectedSetID = selectedId;
                if (userPrefs.getConfirmDeleteSession()){
                    showAlertDialogConfirm(QUESTION_DELETE_SET);
                }else
                    confirmDialogCallback.onCustomConfirmButtonClicked(QUESTION_DELETE_SET, 1);
                break;
            case Constants.UID_btn_recycle_item_copy:
                selectedSetID = selectedId;
                if (userPrefs.getConfirmStartSession()){
                    showAlertDialogConfirm(Constants.QUESTION_COPY_SET);
                }else
                    confirmDialogCallback.onCustomConfirmButtonClicked(Constants.QUESTION_COPY_SET, 1);
                break;
            case Constants.UID_archery_start_button:
                if (mSavedStateViewModel.getActiveWorkoutMeta().getValue() == null) return;
                mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
                if (!mWorkoutMeta.isValid(false)) {
                    if (mWorkoutMeta.shootFormatID == 0)
                        broadcastToast(getString(R.string.label_shoot_field));
                    else
                        if (mWorkoutMeta.distanceID == 0)
                            broadcastToast(getString(R.string.label_shoot_distance));
                        else
                            if (mWorkoutMeta.equipmentID == 0)
                                broadcastToast(getString(R.string.label_shoot_equipment));
                            else
                                if (mWorkoutMeta.shotsPerEnd == 0)
                                    broadcastToast(getString(R.string.label_shoot_per_end));
                                else
                                    if (mWorkoutMeta.setCount == 0)
                                        broadcastToast(getString(R.string.label_shoot_ends));
                    return;
                }
                mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
                mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
                sessionBuild(false);
                sessionStart();
                break;
            case Constants.UID_archery_field_button:
                startCustomList(Constants.SELECTION_TARGET_FIELD, ATRACKIT_EMPTY, sUserID,sDeviceID);
                break;
            case Constants.UID_archery_equipment_button:
                startCustomList(Constants.SELECTION_TARGET_EQUIPMENT, ATRACKIT_EMPTY, sUserID,sDeviceID);
                break;
            case Constants.UID_archery_distance_button:
                startCustomList((mWorkoutMeta.shootFormat.equals("target")) ? Constants.SELECTION_TARGET_DISTANCE_TARGET : Constants.SELECTION_TARGET_DISTANCE_FIELD, ATRACKIT_EMPTY, sUserID,sDeviceID);
                break;
            case Constants.UID_archery_target_size:
                startCustomList((mWorkoutMeta.shootFormat.equals("target")) ? Constants.SELECTION_TARGET_TARGET_SIZE_TARGET : Constants.SELECTION_TARGET_TARGET_SIZE_FIELD, ATRACKIT_EMPTY, sUserID,sDeviceID);
                break;
            case Constants.UID_archery_ends_button:
                startCustomList(Constants.SELECTION_TARGET_ENDS, ATRACKIT_EMPTY, sUserID,sDeviceID);
                break;
            case Constants.UID_archery_per_end_button:
                startCustomList(Constants.SELECTION_TARGET_SHOTS_PER_END, ATRACKIT_EMPTY, sUserID,sDeviceID);
                break;
            case Constants.UID_archery_rest_button:
                startCustomList(Constants.SELECTION_REST_DURATION_TARGET, ATRACKIT_EMPTY, sUserID,sDeviceID);
                break;
            case Constants.UID_archery_call_button:
                startCustomList(Constants.SELECTION_CALL_DURATION, ATRACKIT_EMPTY, sUserID,sDeviceID);
                break;
             case Constants.UID_archery_end_button:
                startCustomList(Constants.SELECTION_END_DURATION, ATRACKIT_EMPTY, sUserID,sDeviceID);
                break;
        }
    }

    /**
     * onCustomItemSelected - Fragment callbacks for @link CustomListFragment lists, scores and generic
     *
     */
    @Override
    public void onCustomItemSelected(int type, long id, String title, int resid, String identifier) {
        String defaultValue = ATRACKIT_EMPTY;
        String sTarget = Long.toString(id);
        // mis-clicking prevention, using threshold of 1000 ms
        if ((SystemClock.elapsedRealtime() - mLastClickTime) < MIN_CLICK_THRESHOLD){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        if (type == Constants.SELECTION_BODY_REGION){
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet != null) {
                mWorkoutSet.regionID = id;
                mWorkoutSet.regionName = title;
                if ((mWorkoutSet.bodypartID != null) && (mWorkoutSet.bodypartID > 0)){
                    List<Bodypart> list = mSessionViewModel.getBodypartListByRegion(id);
                    boolean bFound = false;
                    if (list != null && (list.size() > 0))
                        for(Bodypart bp : list){
                            if (bp._id == mWorkoutSet.bodypartID){
                                bFound = true;
                                break;
                            }
                        }
                    if (!bFound){
                        mWorkoutSet.bodypartID = null;
                        mWorkoutSet.bodypartName = ATRACKIT_EMPTY;
                    }

                }
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
        }
        if (type == Constants.SELECTION_BODYPART) {
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet != null) {
                mWorkoutSet.bodypartID = id;
                mWorkoutSet.bodypartName = title;
                mSavedStateViewModel.setDirtyCount(1);
                int defaultInt;
                Bodypart bodypart = (((mWorkoutSet.bodypartID != null) &&  (mWorkoutSet.bodypartID > 0)) ? mSessionViewModel.getBodypartById(mWorkoutSet.bodypartID) : null);
                if (bodypart != null) {
                    //if ((mWorkoutSet.repCount == null) || (mWorkoutSet.repCount == 0))
                    defaultInt = userPrefs.getDefaultNewReps();
                    mWorkoutSet.repCount = defaultInt;
                    defaultInt = userPrefs.getDefaultNewSets();
                    mSavedStateViewModel.setSetsDefault(defaultInt);
                //    Float defaultFloat = (bodypart.lastWeight > 0) ? bodypart.lastWeight : 0;
                //    mWorkoutSet.weightTotal = defaultFloat;
                    mWorkoutSet.regionID = bodypart.regionID;
                    mWorkoutSet.regionName = bodypart.regionName;
                }
               sessionSaveCurrentSet();
            }
            userPrefs.setLastBodyPartID(Long.toString(id));
            userPrefs.setLastBodyPartName(title);
        }
        if (type == Constants.SELECTION_EXERCISE) {
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            mWorkoutSet.exerciseID = id;
            mWorkoutSet.exerciseName = title;
            mSavedStateViewModel.setDirtyCount(1);
            mSetExercise = mSessionViewModel.getExerciseById(id);
            if (mSetExercise != null) {
                mWorkoutSet.per_end_xy = mSetExercise.workoutExercise;
                if (mSetExercise.resistanceType != null)
                    mWorkoutSet.resistance_type = mSetExercise.resistanceType;
                if (mWorkoutSet.scoreTotal == FLAG_BUILDING) {
                    if (mSetExercise.lastReps > 0)
                        mWorkoutSet.repCount = mSetExercise.lastReps;
                    else
                        mWorkoutSet.repCount = userPrefs.getDefaultNewReps();
                    if (mSetExercise.lastSets > 0)
                        mSavedStateViewModel.setSetsDefault(mSetExercise.lastSets);
                    else
                        mSavedStateViewModel.setSetsDefault(userPrefs.getDefaultNewSets());
                }
                if (mSetExercise.lastSelectedWeight > 0) mWorkoutSet.weightTotal = mSetExercise.lastSelectedWeight;
                else
                    if (mSetExercise.lastAvgWeight > 0) mWorkoutSet.weightTotal =  (float)Math.floor(mSetExercise.lastAvgWeight);

                if ((mWorkoutSet.weightTotal != null) && (mWorkoutSet.repCount != null))
                    if ((mWorkoutSet.weightTotal > 0f) && (mWorkoutSet.repCount > 0)) mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);

                if ((mSetExercise.first_BPID != null) && (mSetExercise.first_BPID > 0)) {
                    mWorkoutSet.bodypartID = mSetExercise.first_BPID;
                    mWorkoutSet.bodypartName = mSetExercise.first_BPName;
                    if((mWorkoutSet.regionID == null)||(mWorkoutSet.regionID == 0)){
                        Bodypart part = mSessionViewModel.getBodypartById(mSetExercise.first_BPID);
                        if (part != null) {
                            mWorkoutSet.regionID = part.regionID;
                            mWorkoutSet.regionName = part.regionName;
                        }
                    }
                }
                mSavedStateViewModel.setDirtyCount(1);
                sessionSaveCurrentSet();
            }
        }
        if (type == Constants.SELECTION_SETS){
            int nbrSets = Math.toIntExact(id);
            mSavedStateViewModel.setSetsDefault(nbrSets);
        }
        if (type == Constants.SELECTION_REPS){
            int nbrReps = Math.toIntExact(id);
            mSavedStateViewModel.setRepsDefault(nbrReps);
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if ((mWorkoutSet != null) && (mWorkoutSet.scoreTotal == Constants.FLAG_PENDING)) {
                mWorkoutSet.repCount = nbrReps;
                if (mWorkoutSet.weightTotal != null && mWorkoutSet.weightTotal > 0f)
                    mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);
                mSavedStateViewModel.setDirtyCount(1);
                sessionSaveCurrentSet();
            }else {
                mSavedStateViewModel.setRepsDefault(nbrReps);
                mSavedStateViewModel.setDirtyCount(1);
            }
        }
        if ((type == Constants.SELECTION_REST_DURATION_GYM)||(type == Constants.SELECTION_REST_DURATION_TARGET)
                ||(type == Constants.SELECTION_CALL_DURATION)||(type == Constants.SELECTION_END_DURATION)){
            if (!TextUtils.isDigitsOnly(title)) return;
            int seconds = Integer.parseInt(title);
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
            if (type == Constants.SELECTION_REST_DURATION_GYM) userPrefs.setWeightsRestDuration(seconds);
            if (type == Constants.SELECTION_REST_DURATION_TARGET) userPrefs.setArcheryRestDuration(seconds);
            if (type == Constants.SELECTION_CALL_DURATION) userPrefs.setArcheryCallDuration(seconds);
            if (type == Constants.SELECTION_END_DURATION) userPrefs.setArcheryEndDuration(seconds);
            if (mWorkoutSet != null) {
                if ((type == Constants.SELECTION_REST_DURATION_GYM)||(type == Constants.SELECTION_REST_DURATION_TARGET))
                    mWorkoutSet.rest_duration = TimeUnit.SECONDS.toMillis(seconds);
                if ((type == SELECTION_CALL_DURATION)) mWorkoutSet.call_duration = TimeUnit.SECONDS.toMillis(seconds);
                if ((type == SELECTION_END_DURATION)) mWorkoutSet.goal_duration = TimeUnit.SECONDS.toMillis(seconds);
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
            if (mWorkoutMeta != null) {
                if ((type == Constants.SELECTION_REST_DURATION_GYM)||(type == Constants.SELECTION_REST_DURATION_TARGET))
                    mWorkoutMeta.rest_duration = TimeUnit.SECONDS.toMillis(seconds);
                if ((type == SELECTION_CALL_DURATION)) mWorkoutMeta.call_duration = TimeUnit.SECONDS.toMillis(seconds);
                if ((type == SELECTION_END_DURATION)) mWorkoutMeta.goal_duration = TimeUnit.SECONDS.toMillis(seconds);
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
            }
        }
        if ((type == Constants.SELECTION_WEIGHT_KG)){
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet != null) {
                float newFloat = Float.parseFloat(title);
                if (mWorkoutSet.weightTotal == null || mWorkoutSet.weightTotal != newFloat) {
                    mWorkoutSet.weightTotal = newFloat;
                    if (mWorkoutSet.repCount != null && mWorkoutSet.repCount > 0) mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);
                    mSavedStateViewModel.setDirtyCount(1);
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0)) {
                        Exercise ex = mSessionViewModel.getExerciseById(mWorkoutSet.exerciseID);
                        ex.lastSelectedWeight = mWorkoutSet.weightTotal;
                        mSessionViewModel.updateExercise(ex);
                    }
                    if (mSavedStateViewModel.getToDoSetsSize() > 0) {
                        List<WorkoutSet> sets = (mSavedStateViewModel.getToDoSets().getValue() != null) ? mSavedStateViewModel.getToDoSets().getValue() : new ArrayList<>();
                        boolean updated = false;
                        if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0))
                            for (WorkoutSet set : sets) {
                                    if ((set.setCount > mWorkoutSet.setCount) && (set.exerciseID == mWorkoutSet.exerciseID) && (set.start == 0)) {
                                        set.weightTotal = mWorkoutSet.weightTotal;
                                        updated = true;
                                    }
                            }
                        if (updated) {
                            mSavedStateViewModel.setToDoSets(sets);
                        }
                    }
                    sessionSaveCurrentSet();
                }
            }
        }
        if (type == Constants.SELECTION_WEIGHT_LBS){
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet != null) {
                float newFloat = (Float.parseFloat(title) > 0) ? Float.parseFloat(title)/Constants.KG_TO_LBS : 0;  // stored as KG!
                if (mWorkoutSet.weightTotal == null || newFloat != mWorkoutSet.weightTotal) {
                    mWorkoutSet.weightTotal = newFloat;
                    if (mWorkoutSet.repCount != null && mWorkoutSet.repCount > 0) mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);
                    mSavedStateViewModel.setDirtyCount(1);
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0)) {
                        Exercise ex = mSessionViewModel.getExerciseById(mWorkoutSet.exerciseID);
                        ex.lastSelectedWeight = mWorkoutSet.weightTotal;
                        mSessionViewModel.updateExercise(ex);
                    }
                    if (mSavedStateViewModel.getToDoSetsSize() > 0) {
                        List<WorkoutSet> sets = (mSavedStateViewModel.getToDoSets().getValue() != null) ? mSavedStateViewModel.getToDoSets().getValue() : new ArrayList<>();
                        boolean updated = false;
                        if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0))
                            for (WorkoutSet set : sets) {
                                    if ((set.setCount > mWorkoutSet.setCount) && (set.exerciseID == mWorkoutSet.exerciseID) && (set.start == 0)) {
                                        set.weightTotal = mWorkoutSet.weightTotal;
                                        updated = true;
                                    }
                            }
                        if (updated) {
                            mSavedStateViewModel.setToDoSets(sets);
                        }
                    }
                    sessionSaveCurrentSet();
                }
            }
        }
        if (type == Constants.SELECTION_TARGET_FIELD){
            mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
            boolean bUpdated = false;
            if (mWorkoutMeta != null) {
                if (mWorkoutMeta.shootFormatID != id) {
                    mWorkoutMeta.shootFormatID = (int) id;
                    mWorkoutMeta.shootFormat = title;
                    mSavedStateViewModel.setDirtyCount(1);
                    mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                    String sLabel = "last_" + Utilities.SelectionTypeToString(this,type);
                    userPrefs.setPrefStringByLabel(sLabel, title);
                    sLabel = "last_" + title + "_" + Utilities.SelectionTypeToString(this, Constants.SELECTION_TARGET_DISTANCE_FIELD);
                    String sValue = userPrefs.getPrefStringByLabel(sLabel);
                    int index = 1;
                    String resourceID ="archery_distance_" + title.toLowerCase();
                    int resID = getResources().getIdentifier(resourceID,"array", Constants.ATRACKIT_ATRACKIT_CLASS);
                    if (sValue.length() > 0){
                        String[] distances = getResources().getStringArray(resID);
                        for (String distance : distances){
                            if (distance.equals(sValue)) {
                                mWorkoutMeta.distanceID = index;
                                mWorkoutMeta.distanceName = sValue;
                                bUpdated = true;
                                break;
                            }
                            index++;
                        }
                    }
                    sLabel = "last_" + title + "_" + Utilities.SelectionTypeToString(this, Constants.SELECTION_TARGET_TARGET_SIZE_FIELD);
                    sValue = userPrefs.getPrefStringByLabel(sLabel);
                    index = 1;
                    resourceID ="archery_target_size_" + title.toLowerCase();
                    resID = getResources().getIdentifier(resourceID,"array", Constants.ATRACKIT_ATRACKIT_CLASS);
                    if (sValue.length() > 0){
                        String[] targetSizes = getResources().getStringArray(resID);
                        for (String distance : targetSizes){
                            if (distance.equals(sValue)) {
                                mWorkoutMeta.targetSizeID = index;
                                mWorkoutMeta.targetSizeName = sValue;
                                bUpdated = true;
                                break;
                            }
                            index++;
                        }
                    }
                    sLabel = "last_" + title + "_" + Utilities.SelectionTypeToString(this, Constants.SELECTION_TARGET_EQUIPMENT);
                    sValue = userPrefs.getPrefStringByLabel(sLabel);
                    index = 1;
                    resourceID ="archery_equipment";
                    resID = getResources().getIdentifier(resourceID,"array", Constants.ATRACKIT_ATRACKIT_CLASS);
                    if (sValue.length() > 0){
                        String[] equipment = getResources().getStringArray(resID);
                        for (String bow : equipment){
                            if (bow.equals(sValue)) {
                                mWorkoutMeta.equipmentID = index;
                                mWorkoutMeta.equipmentName = sValue;
                                bUpdated = true;
                                break;
                            }
                            index++;
                        }
                    }
                    sLabel = "last_" + title + "_" + Utilities.SelectionTypeToString(this, Constants.SELECTION_TARGET_ENDS);
                    sValue = userPrefs.getPrefStringByLabel(sLabel);
                    if (sValue.length() > 0){
                        mWorkoutMeta.setCount = Integer.parseInt(sValue);
                        mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                    }
                    sLabel = "last_" + title + "_Shots per End";
                    sValue = userPrefs.getPrefStringByLabel(sLabel);
                    if (sValue.length() > 0){
                        mWorkoutMeta.shotsPerEnd = Integer.parseInt(sValue);
                        bUpdated = true;
                    }
                    if (bUpdated)
                        mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                }
            }

        }
        if (type == Constants.SELECTION_TARGET_EQUIPMENT){
            mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
            if (mWorkoutMeta != null) {
                mWorkoutMeta.equipmentID = (int) id;
                mWorkoutMeta.equipmentName = title;
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                if (mWorkoutMeta.shootFormat.length() > 0) {
                    String sLabel = "last_" + mWorkoutMeta.shootFormat + "_" + Utilities.SelectionTypeToString(this, type);
                    userPrefs.setPrefStringByLabel(sLabel, title);
                }
            }
        }
        if ((type == Constants.SELECTION_TARGET_DISTANCE_FIELD) || (type == Constants.SELECTION_TARGET_DISTANCE_TARGET)) {
            mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
            if (mWorkoutMeta != null) {
                mWorkoutMeta.distanceID = (int) id;
                mWorkoutMeta.distanceName = title;
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                if (mWorkoutMeta.shootFormat.length() > 0) {
                    String sLabel = "last_" + mWorkoutMeta.shootFormat + "_" + Utilities.SelectionTypeToString(this, type);
                    userPrefs.setPrefStringByLabel(sLabel, title);
                }
            }
        }
        if ((type == Constants.SELECTION_TARGET_TARGET_SIZE_FIELD)|| (type == Constants.SELECTION_TARGET_TARGET_SIZE_TARGET)){
            mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
            if (mWorkoutMeta != null) {
                mWorkoutMeta.targetSizeID = (int) id;
                mWorkoutMeta.targetSizeName = title;
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                if (mWorkoutMeta.shootFormat.length() > 0) {
                    String sLabel = "last_" + mWorkoutMeta.shootFormat + "_" + Utilities.SelectionTypeToString(this, type);
                    userPrefs.setPrefStringByLabel( sLabel, title);
                }
            }
        }
        if (type == Constants.SELECTION_TARGET_ENDS){
            mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
            if (mWorkoutMeta != null) {
                mWorkoutMeta.setCount = (int) id;
                if (mWorkoutMeta.shotsPerEnd > 0) mWorkoutMeta.totalPossible = (mWorkoutMeta.setCount * mWorkoutMeta.shotsPerEnd * 10);
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                if (mWorkoutMeta.shootFormat.length() > 0) {
                    String sLabel = "last_" + mWorkoutMeta.shootFormat + "_" + Utilities.SelectionTypeToString(this, type);
                    String value = Long.toString(id);
                    userPrefs.setPrefStringByLabel(sLabel, value);
                }
            }
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet != null){
                mWorkoutSet.setCount = (int) id;
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
        }
        if (type == Constants.SELECTION_TARGET_SHOTS_PER_END){
            mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
            if (mWorkoutMeta != null) {
                mWorkoutMeta.shotsPerEnd = (int)id;
                if (mWorkoutMeta.setCount > 0) mWorkoutMeta.totalPossible = (mWorkoutMeta.setCount * mWorkoutMeta.shotsPerEnd * 10);
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                if (mWorkoutMeta.shootFormat.length() > 0) {
                    String sLabel = "last_" + mWorkoutMeta.shootFormat + "_" + Utilities.SelectionTypeToString(this, type);
                    String value = Long.toString(id);
                    userPrefs.setPrefStringByLabel(sLabel, value);
                }
            }
        }
        if (type == Constants.SELECTION_TARGET_POSSIBLE_SCORE) {
            mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
            if (mWorkoutMeta != null) {
                mWorkoutMeta.totalPossible = (int) id;
                mSavedStateViewModel.setDirtyCount(1);
                mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
                if (mWorkoutMeta.shootFormat.length() > 0) {
                    String sLabel = "last_" + mWorkoutMeta.shootFormat + "_" + Utilities.SelectionTypeToString(this, type);
                    String value = Long.toString(id);
                    userPrefs.setPrefStringByLabel(sLabel, value);
                }
            }
        }
    }
    @Override
    public void onCustomScoreSelected(int type, long id, String title, int iSetIndex) {
        // mis-clicking prevention, using threshold of 1000 ms
        if ((SystemClock.elapsedRealtime() - mLastClickTime) < MIN_CLICK_THRESHOLD) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        if (type == Constants.SELECTION_SETS){
            mSavedStateViewModel.setSetsDefault((int)id);
            mSavedStateViewModel.setDirtyCount(1);
        }
        if (type == Constants.SELECTION_REPS){
            mSavedStateViewModel.setRepsDefault((int)id);
            mSavedStateViewModel.setDirtyCount(1);
        }
        if (type == Constants.SELECTION_GOAL_DURATION){
            if (mSavedStateViewModel.getActiveWorkout().getValue() != null){
                mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
                if (mWorkout.activityID > 0){
                    String sLabel = getString(R.string.default_act_dur) + Long.toString(mWorkout.activityID);
                    userPrefs.setPrefStringByLabel(sLabel, Long.toString(id));
                }
                mWorkout.goal_duration = id;
                mSavedStateViewModel.setActiveWorkout(mWorkout);
            }
        }
        if (type == Constants.SELECTION_GOAL_STEPS){
            if (mSavedStateViewModel.getActiveWorkout().getValue() != null){
                mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
                if (mWorkout.activityID > 0){
                    String sLabel = getString(R.string.default_act_steps) + Long.toString(mWorkout.activityID);
                    userPrefs.setPrefStringByLabel( sLabel, Long.toString(id));
                }
                mWorkout.goal_steps = id;
                mSavedStateViewModel.setActiveWorkout(mWorkout);
            }

        }
    }
    ///////////////////////////////////////
    // OPTIONS MENU
    ///////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.routine,menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menu != null) {
            mMenu = menu;
            prepareOptionsMenu();
        }
        return true;
    }
    final private void prepareSupportActionBar(){
        if (actionBar == null) actionBar = getSupportActionBar();
        if (mWorkout == null) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        String sType =  getString(R.string.label_routine);
        if ((mWorkout != null) &&  Utilities.isShooting(mWorkout.activityID)) sType = getString(R.string.label_shoot);
        if ((mWorkout != null) && (mWorkout.parentID !=null) && (mWorkout.parentID < 0)) sType = getString(R.string.label_template);
        final String sAdd = getString(R.string.action_add) + Constants.ATRACKIT_SPACE + sType;
        final String sEdit = getString(R.string.action_edit) + Constants.ATRACKIT_SPACE + sType;
        final String sStart = getString(R.string.action_start) + Constants.ATRACKIT_SPACE + sType;
        if (actionBar != null) {
            if ((mWorkout._id  > 1))
                actionBar.setTitle(sEdit);
            else
                if (!userPrefs.getPrefByLabel(USER_PREF_USE_SET_TRACKING))
                    actionBar.setTitle(sStart);
                else
                    actionBar.setTitle(sAdd);
        }
    }
    final private void prepareOptionsMenu(){
        if (mMenu == null) return;
        MenuItem mTemplateItem = mMenu.findItem(R.id.action_template);
        MenuItem mOpenItem = mMenu.findItem(R.id.action_open);
        mOpenItem.setVisible(false);
        MenuItem mDeviceItem = mMenu.findItem(R.id.action_routine_device);
        Drawable iconTemplate;
        boolean doTracking = userPrefs.getPrefByLabel(Constants.USER_PREF_USE_SET_TRACKING);
        if (mWorkout != null) {
            if (!Utilities.isGymWorkout(mWorkout.activityID) && !(Utilities.isShooting(mWorkout.activityID))) {
                mTemplateItem.setVisible(false);
                mDeviceItem.setVisible(false);
            } else {
                if (mWorkout.parentID != null) {
                    if (mWorkout.parentID < 0) {
                        if ((mWorkout.name != null) && (mWorkout.name.length() > 0))
                            iconTemplate = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_heart_outline);
                        else
                            iconTemplate = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_heart_add);
                        Utilities.setColorFilter(iconTemplate, getColor(R.color.primaryTextColor));
                    } else {
                        iconTemplate = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_favlist);
                    }
                } else {
                    iconTemplate = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_heart_add);
                }
                mTemplateItem.setIcon(iconTemplate);
                mTemplateItem.setEnabled((((mWorkout.start == 0) && (mWorkout.end == 0)) || ((mWorkout.start > 0) && (mWorkout.end > 0))));
                if (!userPrefs.getPrefByLabel(LABEL_DEVICE_USE) || (!doTracking)) {
                    if (mDeviceItem != null) mDeviceItem.setVisible(false);
                }
                mTemplateItem.setVisible(doTracking);
            }
        }else{
            mDeviceItem.setVisible(false);
            mTemplateItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // Reverse animation back to previous activity
            if (returnResultFlag > 0)
                setResult(RESULT_CANCELED, getResultIntent(ATRACKIT_EMPTY));
            ActivityCompat.finishAfterTransition(RoutineActivity.this);
        }
        if (id == R.id.action_routine_device) {
            if ((mWorkout != null && mWorkout.name != null) && (mWorkout.name.length() > 0)) {
                Gson gson = new Gson();
                PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.WEAR_DATA_BUNDLE_RECEIVED_PATH);
                dataMap.getDataMap().putString(KEY_FIT_ACTION, INTENT_WORKOUT_EDIT);
                Workout workout = mWorkout;
                WorkoutSet set = mWorkoutSet;
                String sUserID = workout.userID;
                if ((set != null) && (set.start > 0L))
                    dataMap.getDataMap().putLong(Constants.KEY_FIT_TIME, set.start);
                else
                    dataMap.getDataMap().putLong(Constants.KEY_FIT_TIME, new Date().getTime());
                if (workout != null) {
                    dataMap.getDataMap().putString(Workout.class.getSimpleName(), gson.toJson(workout));
                    List<WorkoutSet> setList = mSessionViewModel.getWorkoutSetByWorkoutID(workout._id, workout.userID, workout.deviceID);
                    int iSetCount = ((setList != null && setList.size() > 0) ? setList.size() : 0);
                    if (iSetCount > 0) {
                        dataMap.getDataMap().putString(Constants.KEY_LIST_SETS, gson.toJson(setList));
                    }
                    dataMap.getDataMap().putInt(Constants.KEY_FIT_SETS, iSetCount);
                    dataMap.getDataMap().putString(KEY_FIT_USER, sUserID);
                    PutDataRequest request = dataMap.asPutDataRequest();
                    request.setUrgent();
                    Log.i(LOG_TAG, "sendDataClientMessage request " + INTENT_WORKOUT_EDIT);
                    Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
                    dataItemTask.addOnCompleteListener(task -> Log.d(LOG_TAG, "sendDataClientMessage outcome: " + request.toString()));
                }
            }
            return true;
        }
        if (id == R.id.action_open) {
            if (mWorkout == null) return true;
            if (Utilities.isGymWorkout(mWorkout.activityID)) {
                doOpenWorkouts();
            }
            return true;
        }
        if (id == R.id.action_template){
            if (mWorkout == null) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
            if (mWorkout != null) {
                doGetTemplateName((mWorkout.parentID == null) || (mWorkout.parentID > 0));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void doConfirmDialog(int questionType, String sQuestion, ICustomConfirmDialog callback){
        try {
            int iExistingQuestion = mSavedStateViewModel.getIsInProgress();
            boolean bNewQ = (iExistingQuestion == 0 || (questionType != iExistingQuestion));
            FragmentManager fm = getSupportFragmentManager();
            if (bNewQ) {
                if (fm.findFragmentByTag(CustomConfirmDialog.class.getSimpleName()) != null) {
                    CustomConfirmDialog confirmDialog2 = (CustomConfirmDialog) fm.findFragmentByTag(CustomConfirmDialog.class.getSimpleName());
                    if (confirmDialog2 != null) fm.beginTransaction().remove(confirmDialog2).commit();
                }
            }else {  /// same question
                if (fm.findFragmentByTag(CustomConfirmDialog.class.getSimpleName()) != null) {
                    CustomConfirmDialog confirmDialog2 = (CustomConfirmDialog) fm.findFragmentByTag(CustomConfirmDialog.class.getSimpleName());
                    if (confirmDialog2 != null) confirmDialog2.getView().bringToFront();
                }
                return;
            }

            CustomConfirmDialog confirmDialog = CustomConfirmDialog.newInstance(questionType, sQuestion, ((callback == null)? confirmDialogCallback : callback));
            if (questionType == Constants.QUESTION_NETWORK)
                confirmDialog.setSingleButton(true);

            mSavedStateViewModel.setIsInProgress(questionType);
            confirmDialog.show(fm, CustomConfirmDialog.class.getSimpleName());
            if (callback != null) confirmDialog.setCallback(callback);
        }catch (IllegalStateException ie){
            FirebaseCrashlytics.getInstance().recordException(ie);
            getSupportFragmentManager().executePendingTransactions();
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, "doConfirmDialog Error " + e.getMessage());
        }

    }
    private void doSnackbar(String sMsg, int length){
        Snackbar.make(container, sMsg, length).setAction(getString(R.string.action_okay)
                , view -> {return;}).show();
    }
    private void doGetTemplateName(boolean bNew){
            final AlertDialog  mAlertDialog = new MaterialAlertDialogBuilder(RoutineActivity.this,  R.style.Widget_MyApp_CustomAlertDialog).create();
            if (mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE) != null) mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
            if (mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE) != null) mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.INVISIBLE);

            final View.OnClickListener startRecog = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doSnackbar("Enter a unique mWorkout name", Snackbar.LENGTH_LONG);
                    mSavedStateViewModel.setSpeechTarget(Constants.TARGET_ROUTINE_NAME);
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    displaySpeechRecognizer();
                    mAlertDialog.dismiss();
                }
            };
            View view = getLayoutInflater().inflate(R.layout.dialog_confirmation_name,null,false);
            AutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.autoCompleteFindName);
            com.google.android.material.textfield.TextInputLayout nameLayout = view.findViewById(R.id.editInputLayoutName);
            ((MaterialButton)view.findViewById(R.id.nameMic)).setOnClickListener(startRecog);
            ((TextView)view.findViewById(R.id.filterLabel)).setOnClickListener(startRecog);
            if (!bNew) {
                ((TextView) view.findViewById(R.id.filterLabel)).setText("Change Template Name");
                if (mWorkout.name != null)autoCompleteTextView.setText(mWorkout.name,false); // set not filter
            }
            ((ImageButton)view.findViewById(R.id.NegativeButton)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAlertDialog.dismiss();
                }
            });
            ((ImageButton)view.findViewById(R.id.PositiveButton)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ensure autoComplete is clean
                    if (nameLayout.getError() == null){
                        mWorkout.name = autoCompleteTextView.getText().toString();
                        if ((mWorkout._id > 1) || ((mWorkout.parentID != null) && mWorkout.parentID < 0)) {
                            mWorkout.parentID = -1L;
                            mWorkout.start = 0; mWorkout.end = 0;
                            mWorkout.lastUpdated = System.currentTimeMillis();
                            mSessionViewModel.updateWorkout(mWorkout);
                        }else{
                            mWorkout.parentID = -1L; // create a template
                            mWorkout.start = 0; mWorkout.end = 0;
                            sessionNewFromOld(null);   // todo: look at this decision
                        }
                        mSavedStateViewModel.setActiveWorkout(mWorkout);
                    }
                    mAlertDialog.dismiss();
                }
            });

            List<String> matchingAutoList  = new ArrayList<>();
            NameListAdapter workoutNameListAdapter = new NameListAdapter(RoutineActivity.this,R.layout.autocomplete_dropdown_item,matchingAutoList, Constants.OBJECT_TYPE_WORKOUT);
            workoutNameListAdapter.setOnItemClickListener(new NameListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, String name) {
                    Log.i(LOG_TAG, "name clicked " + name);
                    List<Workout> list = mSessionViewModel.getWorkoutByName(mWorkout.userID,name);
                    Workout selectedWorkout = (list.size() > 0) ? list.get(0): null; // selected or flagged
                    if (bNew){
                        if (selectedWorkout == null) nameLayout.setError(null); else nameLayout.setError(getString(R.string.template_error_name_helper));
                    }{
                        if (selectedWorkout == null) nameLayout.setError(null);
                        else{
                            if (selectedWorkout._id == mWorkout._id) nameLayout.setError(null);
                            else nameLayout.setError(getString(R.string.template_error_name_helper));
                        }
                    }
                }
            });
            autoCompleteTextView.setAdapter(workoutNameListAdapter);
        try {

            mAlertDialog.setView(view);
            mAlertDialog.show();
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.i(LOG_TAG, "doAlertMessage " + e.getMessage());
        }
    }

    private void sessionNewFromOld(List<WorkoutSet> sets){
        if ((mWorkout == null) || (sets == null)) return;
        long timeMs = System.currentTimeMillis();
        long originalWorkoutID = mWorkout._id;
        List<WorkoutSet> list = new ArrayList<>(sets.size());
        if (sets == null) sets = mSessionViewModel.getWorkoutSetByWorkoutID(originalWorkoutID,mWorkout.userID,mWorkout.deviceID);
        mWorkout._id = timeMs;
        mWorkout.start = timeMs;
        mWorkout.lastUpdated = timeMs;
        mSessionViewModel.insertWorkout(mWorkout);
        if (mWorkout.setCount > 0) {
            int defaultSet = 1;
            for (WorkoutSet set : sets) {
                set.workoutID = timeMs;
                set.userID = mWorkout.userID;
                set.deviceID = mWorkout.deviceID;
                if (set.scoreTotal == FLAG_BUILDING) set.scoreTotal = FLAG_PENDING;
                set.setCount = defaultSet++;
                set.lastUpdated = timeMs;
                if (defaultSet == 2) mWorkoutSet = set;
                mSessionViewModel.insertWorkoutSet(set);
                list.add(set);
            }
        } else {
            mWorkoutSet.workoutID = timeMs;
            mWorkoutSet.userID = mWorkout.userID;
            mWorkoutSet.deviceID = mWorkout.deviceID;
            mWorkoutSet.lastUpdated = timeMs;
            if (mWorkoutSet.scoreTotal == FLAG_BUILDING) mWorkoutSet.scoreTotal = FLAG_PENDING;
            mSessionViewModel.insertWorkoutSet(mWorkoutSet);
            list.add(mWorkoutSet);
        }
        mSavedStateViewModel.setToDoSets(list);
        // can now delete the workoutID 1 sets
        if (originalWorkoutID == 1L)
            mSessionViewModel.deleteWorkoutSetByWorkoutID(originalWorkoutID);
    }
    private void doOpenWorkouts(){
        String sUserId = mWorkout.userID;
        String sDeviceId = mWorkout.deviceID;
        long startTime = userPrefs.getLastUserOpen();
        long endTime = (startTime > 0) ? startTime + (TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1))
                : Utilities.getTimeFrameEnd(Utilities.TimeFrame.BEGINNING_OF_DAY);
        if (startTime == 0){
            startTime = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
            endTime = Utilities.getTimeFrameEnd(Utilities.TimeFrame.BEGINNING_OF_DAY);
            userPrefs.setLastUserOpen(startTime);
        }
        ArrayList<Workout> workouts = new ArrayList<>(mSessionViewModel.getWorkoutsByTimesNow(sUserId, sDeviceId, startTime, endTime));
        if (workouts.size() == 0) broadcastToast("None found - set date");

        mSessionViewModel.setWorkoutList(workouts);
        CustomListFragment customListFragment = CustomListFragment.create(Constants.SELECTION_WORKOUT_HISTORY, 0, sUserId, sDeviceId);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(customListFragment, customListFragment.TAG);
        transaction.commit();
        //customListFragment.setCancelable(true);
        //customListFragment.show(getSupportFragmentManager(), "Workout");

    }

    private void startCustomList(int selectionType, String defaultValue, String sUserID, String sDeviceID){
        CustomListDialogFragment customListFragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
        if ((selectionType == Constants.SELECTION_SETS) || (selectionType == Constants.SELECTION_REPS) || (selectionType == SELECTION_INCOMPLETE_DURATION)
                || (selectionType == Constants.SELECTION_GOAL_DURATION) || (selectionType == Constants.SELECTION_GOAL_STEPS)){
            Integer iPreset = 0;
            if (defaultValue.length() > 0)
                iPreset = Integer.parseInt(defaultValue);
            else {
                if (selectionType == Constants.SELECTION_SETS) {
                    iPreset = mSavedStateViewModel.getSetsDefault();
                    if (iPreset == 0) iPreset = 3;
                }
                if (selectionType == Constants.SELECTION_REPS) {
                    iPreset = mSavedStateViewModel.getRepsDefault();
                    if (iPreset == 0) iPreset = 10;
                }
            }
            CustomScoreDialogFragment customScoreDialogFragment = CustomScoreDialogFragment.newInstance(selectionType,iPreset);
            customScoreDialogFragment.show(fragmentManager, CustomScoreDialogFragment.TAG);
            customScoreDialogFragment.setInterface(RoutineActivity.this);

        }
        else {
            String sTag = CustomListFragment.TAG;
            Long iPreset = 10L;
            if ((selectionType == Constants.SELECTION_WEIGHT_KG) || (selectionType == Constants.SELECTION_WEIGHT_LBS)) {
                sTag = getString(R.string.label_weight);
                iPreset = (long)Math.round(Float.parseFloat(defaultValue));
            }else {
                if ((defaultValue != null) && (defaultValue.length() > 0))
                    iPreset = Long.parseLong(defaultValue);
            }

            if (selectionType == Constants.SELECTION_EXERCISE) {
                sTag = getString(R.string.label_exercise);
            }

            if (selectionType == Constants.SELECTION_BODYPART) {
                sTag = getString(R.string.label_bodypart);
            }
            if ((selectionType == Constants.SELECTION_WEIGHT_KG) || (selectionType == Constants.SELECTION_WEIGHT_LBS)) {
                sTag = getString(R.string.label_weight);
            }
            if (selectionType == Constants.SELECTION_USER_PREFS) {
                sTag = getString(R.string.nav_setting);
            }
            if (selectionType == Constants.SELECTION_TARGET_FIELD) {
                sTag = getString(R.string.label_shoot_field);
            }
            if ((selectionType == Constants.SELECTION_TARGET_DISTANCE_FIELD) || (selectionType == Constants.SELECTION_TARGET_DISTANCE_TARGET)) {
                sTag = getString(R.string.label_shoot_distance);
            }
            if (selectionType == Constants.SELECTION_TARGET_EQUIPMENT) {
                sTag = getString(R.string.label_shoot_equipment);
            }
            if (selectionType == Constants.SELECTION_TARGET_ENDS) {
                sTag = getString(R.string.label_shoot_ends);
            }
            if ((selectionType == Constants.SELECTION_TARGET_TARGET_SIZE_FIELD) || (selectionType == Constants.SELECTION_TARGET_TARGET_SIZE_TARGET)) {
                sTag = getString(R.string.label_shoot_target_size);
            }
            if (selectionType == Constants.SELECTION_TARGET_POSSIBLE_SCORE) {
                sTag = getString(R.string.label_shoot_possible_score);
            }
            if (selectionType == Constants.SELECTION_TARGET_SHOTS_PER_END) {
                sTag = getString(R.string.label_shoot_per_end);
            }
            if (selectionType == SELECTION_TO_DO_SETS){
                sTag = getString(R.string.label_routine);
                //customListFragment.set
            }
            if (selectionType == SELECTION_CALL_DURATION) sTag = getString(R.string.label_archery_call_duration);
            if (selectionType == SELECTION_END_DURATION) sTag = getString(R.string.label_archery_end_duration);
            if (selectionType == SELECTION_REST_DURATION_TARGET) sTag = getString(R.string.label_archery_rest_duration);
            if (selectionType == Constants.SELECTION_WORKOUT_INPROGRESS) sTag = "Incomplete workouts";
            customListFragment = CustomListDialogFragment.create(selectionType, iPreset,sTag, sUserID, sDeviceID);
            //customListFragment.setCancelable(true);
            //customListFragment.show(fragmentManager, sTag);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(customListFragment, customListFragment.TAG);
            transaction.commit();

        }
    }
    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        if (ActivityCompat.checkSelfPermission(RoutineActivity.this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
                // Start the activity, the intent will be populated with the speech text
                microphoneActivityResultLauncher.launch(intent);

            }catch (ActivityNotFoundException anf){
                broadcastToast(getString(R.string.action_nospeech_activity));
            }
        }else{
            ActivityCompat.requestPermissions(RoutineActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORDING_PERMISSION_CODE);
        }
    }
     private void sessionSaveCurrentSet(){
        try{
            if (mWorkoutSet == null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (((mWorkoutSet.workoutID == null) ||(mWorkoutSet.workoutID == 0)) && (mWorkout._id > 0)) mWorkoutSet.workoutID = mWorkout._id;
            // replace the existing set in the to do list
/*            List<WorkoutSet> setList = ( mSavedStateViewModel.getToDoSetsSize() > 0) ?  mSavedStateViewModel.getToDoSets().getValue() : new ArrayList<>();
            if (setList.size() > 0) {
                int index = 0;
                for (WorkoutSet set2 : setList) {
                    if (set2._id == mWorkoutSet._id) {
                        setList.set(index, mWorkoutSet);
                        break;
                    }
                    index++;
                }
                mSavedStateViewModel.setToDoSets(setList);
            }*/
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            if (((mWorkoutSet.scoreTotal == FLAG_BUILDING) && !mWorkoutSet.isValid(false)) || (mWorkoutSet.scoreTotal == FLAG_BUILDING)  || (mWorkoutSet.scoreTotal == Constants.FLAG_NON_TRACKING)) return;
            WorkoutSet testSet = mSessionViewModel.getWorkoutSetById(mWorkoutSet._id, mWorkoutSet.userID, mWorkoutSet.deviceID);
            boolean bNew = (testSet == null);
            if (!bNew){
                mSessionViewModel.updateWorkoutSet(mWorkoutSet);
            }else{
                mSessionViewModel.insertWorkoutSet(mWorkoutSet);
            }
        }catch(Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, e.getMessage());
        }
    }
    private void sessionEnd(){
        if (returnResultFlag > 0)
            setResult(RESULT_OK, getResultIntent(INTENT_ACTIVE_STOP));
        finishAfterTransition();
    }

    // change the state and
    private void sessionStart(){
        try{
            if (mWorkout == null) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
            if (mWorkout == null) return;
            // good to go now!
            if (mWorkout.activityID > 0) {
                if (returnResultFlag > 0) {
                    if (mWorkoutSet != null){
                        // check to remove empty "builder set"
                        if (mWorkoutSet.scoreTotal != FLAG_NON_TRACKING || Utilities.isShooting(mWorkoutSet.activityID)) {
                            List<WorkoutSet> sets = mSavedStateViewModel.getToDoSets().getValue();
                            if ((sets != null) && (sets.size() > 0)) mWorkoutSet = sets.get(0);
                        }
                    }
                    if ((mWorkout.start > 0) && (mWorkout.end == 0))
                        setResult(RESULT_OK, getResultIntent(INTENT_ACTIVESET_START));
                    else {
                        if (mWorkout.parentID == null){
                            if ((mWorkout.start == 0) && (mWorkout.end == 0))
                                setResult(RESULT_OK, getResultIntent(INTENT_ACTIVE_START));  // fresh workout
                            else
                                setResult(RESULT_OK, getResultIntent(Constants.INTENT_TEMPLATE_START));  // reuse an existing workout
                        }else
                            setResult(RESULT_OK, getResultIntent(Constants.INTENT_TEMPLATE_START));  // start using template
                    }
                }
                finishAfterTransition();
            }

        }catch(Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            String sMsg = "Error starting session " + e.getMessage();
            Log.e(LOG_TAG, sMsg);
            Toast.makeText(this, sMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void sessionNewFromTemplate(Workout w){
        List<WorkoutSet>  sets = mSessionViewModel.getWorkoutSetByWorkoutID(w._id, w.userID, w.deviceID);
        long timeMs = System.currentTimeMillis();
        mWorkout = new Workout(w);
        mWorkout._id = timeMs;
        mWorkout.lastUpdated = mWorkout._id;
        mWorkout.parentID = w._id;  // control setting this outside the function
        mWorkout.start = 0; mWorkout.end = 0;
        mWorkout.duration = 0; mWorkout.last_sync = 0;
        mWorkout.device_sync = 0;
        mSessionViewModel.insertWorkout(mWorkout);
        mSavedStateViewModel.setActiveWorkout(mWorkout);
        if (sets != null) {
            for (WorkoutSet oldSet : sets) {
                WorkoutSet s = new WorkoutSet(oldSet);
                s._id = timeMs + (oldSet.setCount * 20);
                s.scoreTotal = Constants.FLAG_PENDING; s.last_sync = 0;
                s.device_sync = 0L;
                s.pause_duration = 0;
                s.duration = 0;
                s.lastUpdated = timeMs;
                if (s.scoreTotal == FLAG_BUILDING) s.scoreTotal = FLAG_PENDING;
                s.workoutID = mWorkout._id;
                mSessionViewModel.insertWorkoutSet(s);
                if (s.setCount == mWorkoutSet.setCount) mWorkoutSet = s;
            }
            mSavedStateViewModel.setToDoSets(sets);
        }
    }

    private void createWorkoutSet(){
        // start fresh and re-initialise the controls
        mWorkoutSet = new WorkoutSet();
        long timeMs = System.currentTimeMillis();
        mWorkoutSet._id = (timeMs);
        String sUserId = ATRACKIT_EMPTY;
        String sDeviceId = ATRACKIT_EMPTY;
        if (mWorkout != null) {
            mWorkoutSet.workoutID = mWorkout._id;
            sDeviceId = mWorkout.deviceID;
            if (mWorkout.userID.length() > 0) sUserId = mWorkout.userID;
            if (mWorkout.activityID > 0) {
                mWorkoutSet.activityID = mWorkout.activityID;
                mWorkoutSet.activityName = mWorkout.activityName;
            }
            mWorkout.setCount++;
            if (mWorkout.setCount > 0) mWorkoutSet.setCount = mWorkout.setCount;
        }
        if (sUserId.length() == 0)
            sUserId = appPrefs.getLastUserID();
        if (sDeviceId.length() == 0)
            sDeviceId = appPrefs.getDeviceID();

        if (sUserId.length() > 0){
            mWorkoutSet.userID = sUserId;
            mWorkoutSet.deviceID = sDeviceId;
        }
    }
    private void createWorkoutMeta(){
        // start fresh and re-initialise the controls
        mWorkoutMeta = new WorkoutMeta();
        long timeMs = System.currentTimeMillis();
        String sUserId = ATRACKIT_EMPTY;
        String sDeviceId = ATRACKIT_EMPTY;
        mWorkoutMeta._id = (timeMs);
        if (mWorkout != null) {
            mWorkoutMeta.workoutID = mWorkout._id;
            sDeviceId = mWorkout.deviceID;
            sUserId = mWorkout.userID;
            if (mWorkout.activityID > 0) {
                mWorkoutMeta.activityID = mWorkout.activityID;
                mWorkoutMeta.activityName = mWorkout.activityName;
                mWorkoutMeta.identifier = mWorkout.identifier;
                mWorkoutMeta.packageName = mWorkout.packageName;
            }
            if (mWorkout.start > 0){
                mWorkoutMeta.start = mWorkout.start;
                if (mWorkout.end > 0){
                    mWorkoutMeta.end = mWorkout.end;
                    mWorkoutMeta.duration = mWorkout.duration;
                }
            }
        }
        if (sUserId.length() == 0)
            sUserId = appPrefs.getLastUserID();
        if (sDeviceId.length() == 0)
            sDeviceId = appPrefs.getDeviceID();

        if (sUserId.length() > 0){
            mWorkoutMeta.userID = sUserId;
            mWorkoutMeta.deviceID = sDeviceId;
        }
        mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
    }
    private void sessionArcheryBuild(){
        long timeMs = System.currentTimeMillis();
        List<WorkoutSet> todo =  new ArrayList<>();
        int iCountSetsToAdd = mWorkoutMeta.setCount;
        boolean useTimedRest = userPrefs.getTimedRest();
        for(int index = 1; index <= iCountSetsToAdd; index++){
            WorkoutSet set = new WorkoutSet(mWorkout);
            set._id  = timeMs + (index * 50);
            set.start = 0L;
            set.end = 0L;
            if (useTimedRest) {
                set.rest_duration = mWorkoutMeta.rest_duration;
                set.call_duration = mWorkoutMeta.call_duration;
                set.goal_duration = mWorkoutMeta.goal_duration;
            }
            set.repCount = 0;
            set.setCount = index;
            set.scoreTotal = FLAG_PENDING;
            String shotEntry = Constants.SHOT_DELIM + Constants.SHOT_SCORE;
            String shotXY = Constants.SHOT_DELIM + Constants.SHOT_XY;
            for (int shotIndex = 1; shotIndex <= mWorkoutMeta.shotsPerEnd; shotIndex++){
                if ((set.score_card != null) && (set.score_card.length() > 0)) {
                    set.score_card += shotEntry;
                    set.per_end_xy += shotXY;
                } else{
                    set.score_card = Constants.SHOT_SCORE;
                    set.per_end_xy = Constants.SHOT_XY;
                }
                if ((mWorkoutMeta.per_end_xy != null) &&(mWorkoutMeta.per_end_xy.length() > 0)){
                    mWorkoutMeta.score_card += shotEntry;
                    mWorkoutMeta.per_end_xy += shotXY;
                }else{
                    mWorkoutMeta.score_card = Constants.SHOT_SCORE;
                    mWorkoutMeta.per_end_xy = Constants.SHOT_XY;
                }
            }
            todo.add(set);
        }
        mSavedStateViewModel.setDirtyCount(0);
         mSavedStateViewModel.setToDoSets(todo);
        mWorkoutSet = todo.get(0);
        mSavedStateViewModel.setActiveWorkout(mWorkout);
        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
        mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
    }

    private void sessionBuild(boolean bAddNext){
        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        String sUserId = mWorkout.userID;
        if ((sUserId == null) || (sUserId.length() == 0)) return;
        long timeMs = System.currentTimeMillis();
        if (Utilities.isShooting(mWorkout.activityID)) {
            mWorkoutSet.repCount = 0;  // this is the score later!
            sessionArcheryBuild();
            return;
        }
        boolean bIsGym = Utilities.isGymWorkout(mWorkoutSet.activityID);
       // Long existingRegionID = mWorkoutSet.regionID;
       // String existingRegionName = mWorkoutSet.regionName;
        List<WorkoutSet> todoList =  mSessionViewModel.getWorkoutSetByWorkoutID(mWorkout._id, mWorkout.userID, mWorkout.deviceID);
        int iCountSetsToAdd = mSavedStateViewModel.getSetsDefault();
        int currentSize = (todoList == null) ? 0 : todoList.size();
        int currentIndex = 1;
        for (WorkoutSet s : todoList){
            if (s.start == 0) break;
            currentIndex++;
        }
        String sToast = "Added " + iCountSetsToAdd + Constants.ATRACKIT_SPACE + getString(R.string.label_set);
        if ((mWorkoutSet.exerciseID != null) && (mWorkoutSet.exerciseID > 0)){
            sToast += " of " + mWorkoutSet.exerciseName;
        }
        int defaultReps = mSavedStateViewModel.getRepsDefault();
        if (bIsGym && (mWorkoutSet.repCount != null) && (mWorkoutSet.repCount > 0)
                && (mWorkoutSet.weightTotal != null && mWorkoutSet.weightTotal > 0F))
            mWorkoutSet.wattsTotal = (mWorkoutSet.repCount * mWorkoutSet.weightTotal);
        // only if build
        if (bIsGym && mWorkoutSet.scoreTotal == FLAG_BUILDING) {
            for (int index = 1; index <= iCountSetsToAdd; index++) {
                WorkoutSet set = new WorkoutSet(mWorkoutSet);
                set._id = timeMs + (index * 50);
                set.start = 0L;
                set.end = 0L;
                set.repCount = defaultReps;
                set.setCount = currentSize + index;
                set.duration = 0;
                set.lastUpdated = timeMs;
                set.scoreTotal = FLAG_PENDING;
                set.realElapsedStart = null;
                set.realElapsedEnd = null;
                set.startBPM = null;
                set.endBPM = null;
                mSessionViewModel.insertWorkoutSet(set);
            }
            broadcastToast(sToast);
            // set up the next build set
            if (bAddNext) {
                Bodypart bpSource = (((mWorkoutSet != null) && (mWorkoutSet.bodypartID != null)) ? mSessionViewModel.getBodypartById(mWorkoutSet.bodypartID) : null);
                if ((bpSource != null) && (bpSource.parentID != null)) {
                    mWorkoutSet.bodypartID = bpSource.parentID;
                    mWorkoutSet.bodypartName = bpSource.parentName;
                }
                mWorkoutSet.setCount = currentSize + iCountSetsToAdd + 1;
                mWorkoutSet.repCount = defaultReps;
                mWorkoutSet.resistance_type = null;
                mWorkoutSet.exerciseID = null;
                mWorkoutSet.exerciseName = ATRACKIT_EMPTY;
                mWorkoutSet.per_end_xy = ATRACKIT_EMPTY;
                mWorkoutSet.weightTotal = 0f;
                mWorkoutSet.wattsTotal = 0f;
                mWorkoutSet.scoreTotal = Constants.FLAG_BUILDING;
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
        }
        else
            sessionSaveCurrentSet();

        todoList = mSessionViewModel.getWorkoutSetByWorkoutID(mWorkout._id,mWorkout.userID,mWorkout.deviceID);
        if ((todoList != null) && (todoList.size() > 0) && (currentSize != todoList.size())) {
            mWorkout.setCount = todoList.size();
            mSessionViewModel.updateWorkout(mWorkout);
            mSavedStateViewModel.setToDoSets(todoList);
        }
        mSavedStateViewModel.setDirtyCount(0);
        mSavedStateViewModel.setActiveWorkout(mWorkout);
    }
    private void startExerciseActivityForResult(Exercise exercise){
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(RoutineActivity.this, R.anim.enter_anim, R.anim.no_anim);
        Intent intent = new Intent(RoutineActivity.this, ExerciseActivity.class);
        if (exercise != null) intent.putExtra(ExerciseActivity.ARG_EXERCISE_ID, exercise._id);
        if (exercise != null) intent.putExtra(ARG_EXERCISE_OBJECT, exercise);
        intent.putExtra(ExerciseActivity.ARG_RETURN_RESULT, 1);
        intent.putExtra(KEY_FIT_HOST, mMessagesViewModel.hasPhone());
        try {
            exerciseActivityResultLauncher.launch(intent, options);
        }catch (Exception e){
            Log.e(LOG_TAG, "Failed to launch Exercise " + e.getMessage());
        }
/*        ActionBar actionBar = getSupportActionBar();
        final String sAdd = getString(R.string.action_add) + Constants.ATRACKIT_SPACE + getString(R.string.label_exercise);
        final String sEdit = getString(R.string.action_edit) + Constants.ATRACKIT_SPACE + getString(R.string.label_exercise);
        if (actionBar != null) {

                actionBar.setTitle(sAdd);
           // else
             //   actionBar.setTitle(sEdit);
        }*/
/*        try {
            Exercise mExercise = new Exercise();
            ExerciseDialogFragment fragment = ExerciseDialogFragment.create(mExercise._id, mExercise);
            fragment.show(getSupportFragmentManager(), ExerciseDialogFragment.TAG);
            //if (mExercise != null) fragment.setExercise(mExercise);
          //  FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
          //  transaction.add(R.id.workout_placeholder, fragment, ExerciseDialogFragment.TAG);
          //  transaction.commit();
        }catch (Exception e){
            Log.e(LOG_TAG,"exercise frag " + e.getMessage());
        }*/
    }

    private Intent getResultIntent(String intentAction){
        Intent resultIntent  = new Intent(intentAction);
        resultIntent.putExtra(KEY_FIT_WORKOUTID, mWorkout._id);
        resultIntent.putExtra(KEY_FIT_TYPE, 0);
        resultIntent.putExtra(ARG_WORKOUT_OBJECT, mWorkout);
        if (mWorkoutSet == null) createWorkoutSet();
        resultIntent.putExtra(ARG_WORKOUT_SET_OBJECT, mWorkoutSet);
        resultIntent.putExtra(ARG_WORKOUT_META_OBJECT, mWorkoutMeta);
        resultIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
        List<WorkoutSet> sets =  (mSavedStateViewModel.getToDoSets().getValue() != null) ?  mSavedStateViewModel.getToDoSets().getValue() : new ArrayList<>();
        if (sets.size() > 0){
          ArrayList<WorkoutSet> arraySets = new ArrayList<>();
          arraySets.addAll(sets);
          resultIntent.putParcelableArrayListExtra(ARG_WORKOUT_SET_LIST, arraySets);
        }
        return resultIntent;
    }
    private void broadcastToast(String msg){
        new Runnable(){
            @Override
            public void run() {
                Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                msgIntent.putExtra(INTENT_EXTRA_MSG, msg);
                msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                Context context = getApplicationContext();
                context.sendBroadcast(msgIntent);
            }
        };

    }

}

