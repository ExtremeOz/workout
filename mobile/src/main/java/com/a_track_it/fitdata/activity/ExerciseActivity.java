package com.a_track_it.fitdata.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.adapter.ArrayAdapterSearchView;
import com.a_track_it.fitdata.adapter.ExerciseListAdapter;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Bodypart;
import com.a_track_it.fitdata.common.data_model.DateTuple;
import com.a_track_it.fitdata.common.data_model.Exercise;
import com.a_track_it.fitdata.common.data_model.ResistanceType;
import com.a_track_it.fitdata.common.data_model.SessionCleanupWorker;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModel;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.a_track_it.fitdata.common.user_model.MessagesViewModel;
import com.a_track_it.fitdata.common.user_model.SavedStateViewModel;
import com.a_track_it.fitdata.fragment.CustomConfirmDialog;
import com.a_track_it.fitdata.fragment.ExerciseFragment;
import com.a_track_it.fitdata.fragment.FragmentInterface;
import com.a_track_it.fitdata.fragment.ICustomConfirmDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static com.a_track_it.fitdata.common.Constants.ATRACKIT_SPACE;
import static com.a_track_it.fitdata.common.Constants.INTENT_EXTRA_MSG;
import static com.a_track_it.fitdata.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_HOST;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_USER;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_VALUE;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_WORKOUTID;

public class ExerciseActivity extends BaseActivity implements IEntityFragmentActivityCallback, FragmentInterface, SearchView.OnQueryTextListener {
    private static final String LOG_TAG = ExerciseActivity.class.getSimpleName();
    private ExerciseFragment fragment;
    public static final String ARG_EXERCISE_ID = "ARG_EXERCISE_ID";
    public static final String ARG_EXERCISE_OBJECT = "ARG_EXERCISE_OBJECT";
    public static final String ARG_RETURN_RESULT = "ARG_RETURN_RESULT";
    public static final String ARG_REQUEST_SEND = "ARG_REQUEST_SEND";
    private SavedStateViewModel mSavedStateViewModel;
    private WorkoutViewModel mSessionViewModel;
    private MessagesViewModel mMessagesViewModel;
    private ApplicationPreferences appPrefs;
    private Exercise mExercise;
    private Integer returnResultFlag;
    private List<Exercise> pendingExercises;
    private List<Exercise> possibleExercises;
    private List<String> possibleWorkoutExercises = new ArrayList<>();
    private List<Exercise> exerciseNameAutoList = new ArrayList<>();
    private ExerciseListAdapter mExerciseNameListAdapter;
    private ArrayAdapterSearchView mSearchView;
    private List<Long> setList;
    private List<Long> workoutList;
    private List<Long> pendingExerciseSameNameList;
    View container;
    String sUserID;
    String sDeviceID;
    int pendingFlag;
    com.google.android.material.button.MaterialButton exercise_cancel_button;
    com.google.android.material.button.MaterialButton exercise_save_button;
    com.google.android.material.button.MaterialButton exercise_send_button;
    /**
     * Used to start the activity using a_track_it.com custom animation.
     *
     * @param activity Reference to the Android Activity we are animating from
     * @param entityID ID of activity 1, bodypart 2, exercise 3 used to pre-populate the form field
     * @param ex name of entity class
     */
    public static void launch(BaseActivity activity, long entityID, Exercise ex, int iType, String sUserID, String sDeviceID, Boolean bHasPhone) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.enter_anim, R.anim.no_anim);
        Intent intent = new Intent(activity, ExerciseActivity.class);
        intent.putExtra(ARG_EXERCISE_ID, entityID);
        if (ex != null) intent.putExtra(ARG_EXERCISE_OBJECT, ex);
        intent.putExtra(Constants.KEY_FIT_TYPE, iType);
        intent.putExtra(Constants.KEY_FIT_USER, sUserID);
        intent.putExtra(Constants.KEY_FIT_DEVICE_ID, sDeviceID);
        intent.putExtra(ARG_RETURN_RESULT, 0);
        intent.putExtra(Constants.KEY_FIT_HOST, bHasPhone);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }
    private  ExerciseListAdapter.OnItemClickListener autoCompleteNameItemClickListener = new ExerciseListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, Exercise viewModel) {
            mExercise = viewModel;
            if (mSearchView != null){
                mSearchView.setText(mExercise.name);
                mSearchView.clearDisappearingChildren();
/*                fragment = ExerciseFragment.create(mExercise._id, mExercise);
                if (mExercise != null) fragment.setExercise(mExercise);
                fragment.setPendingFlag(0);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.exercise_placeholder, fragment, ExerciseFragment.TAG);
                transaction.commit();*/
                if (fragment != null){
                    fragment.setExercise(mExercise);
                    fragment.setPendingFlag(0);
                    new Handler(Looper.getMainLooper()).post(() -> fragment.loadUI());
                }
            }


        }
    };
    /* confirm apply matching exercise callback */
    final private ICustomConfirmDialog  confirmCallback = new ICustomConfirmDialog() {
        @Override
        public void onCustomConfirmButtonClicked(int question, int button) {
            if (button > 0){
                List<OneTimeWorkRequest> requestList = new ArrayList<>();
                workoutList = new ArrayList<>();
                setList = new ArrayList<>();
                pendingExerciseSameNameList = new ArrayList<>();
                final Exercise pendingExercise = fragment.getPendingExercise();
                final Exercise matchingExercise = fragment.getMatchingExercise();
                // find the original pending to find the set ids
                if ((sUserID.length() > 0) && (sDeviceID.length() > 0) && (pendingExercise != null)) {
                    pendingExerciseSameNameList.add(pendingExercise._id);
                    List<Exercise> otherpendingSameNameList = mSessionViewModel.getExercisesByName(pendingExercise.name);
                    if ((otherpendingSameNameList != null) && (otherpendingSameNameList.size() > 0)){
                        for (Exercise e : otherpendingSameNameList){
                            if  ((e._id > 50000) && (e._id != pendingExercise._id))
                             pendingExerciseSameNameList.add(e._id);
                        }
                    }
                    Runnable setUpdater = () -> {
                        if (pendingExerciseSameNameList != null) {
                            if (!matchingExercise.otherNames.contains(pendingExercise.name)){
                                if (matchingExercise.otherNames.length() == 0)
                                    matchingExercise.otherNames = pendingExercise.name;
                                else
                                    matchingExercise.otherNames += Constants.SHOT_DELIM + pendingExercise.name;
                                mSessionViewModel.updateExercise(matchingExercise);
                            }
                            for (Long pendingEx : pendingExerciseSameNameList) {
                                workoutList = mSessionViewModel.getWorkoutIDForPendingExercise(pendingEx);
                                setList = mSessionViewModel.getWorkoutSetIDForPendingExercise(pendingEx);
                                mSessionViewModel.updatePendingExerciseMatch(pendingEx, matchingExercise._id, matchingExercise.resistanceType,
                                        matchingExercise.first_BPID, matchingExercise.first_BPName, matchingExercise.workoutExercise);
                                if (setList != null)
                                    for (Long setID : setList) {
                                        WorkoutSet set = mSessionViewModel.getWorkoutSetById(setID, sUserID, sDeviceID);
                                        set.exerciseName = matchingExercise.name;
                                        set.per_end_xy = matchingExercise.workoutExercise;
                                        mSessionViewModel.updateWorkoutSet(set);
                                    }
                                if (workoutList != null)
                                    for (long workoutID : workoutList) {
                                        Data.Builder builder = new Data.Builder();
                                        builder.putString(KEY_FIT_USER, sUserID);
                                        builder.putString(KEY_FIT_DEVICE_ID, sDeviceID);
                                        builder.putLong(KEY_FIT_WORKOUTID, workoutID);
                                        OneTimeWorkRequest workRequest =
                                                new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                                                        .setInputData(builder.build())
                                                        .build();
                                        requestList.add(workRequest);
                                    }
                                Iterator<Exercise> pendingRemover = pendingExercises.iterator();
                                while (pendingRemover.hasNext()) {
                                    Exercise removePending = pendingRemover.next();
                                    if (removePending._id == pendingEx) {
                                        mSessionViewModel.deleteExercise(removePending);
                                        pendingRemover.remove();
                                        break;
                                    }

                                }

                            }
                        }
                        if (requestList.size() > 0) {
                            WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                            workManager.enqueue(requestList);
                        }
                        fragment.setList(pendingExercises);
                        runOnUiThread(() -> {
                            DateTuple exerciseTuple = mSessionViewModel.getPendingExerciseCount();
                            pendingExercises = mSessionViewModel.getPendingExercises();
                            if ((pendingExercises.size() > 0) && (exerciseTuple.sync_count > 0)) {
                                mExercise = pendingExercises.get(0);
                                if (mExercise.bodypartCount < 0)
                                    createToast("Assign bodyparts to exercises");
                                possibleExercises =  getPossibleExercises(mExercise._id, mExercise.name);
                                for(Exercise possible : possibleExercises){
                                    possibleWorkoutExercises.add(possible.workoutExercise);
                                }

                                fragment = ExerciseFragment.create(mExercise._id, mExercise);
                                if (mExercise != null) fragment.setExercise(mExercise);
                                fragment.setPendingFlag(1);
                                if (pendingExercises != null)
                                    fragment.setList(pendingExercises);
                                if (possibleExercises != null)
                                    fragment.setMatchingList(possibleExercises);
                                FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                                transaction2.replace(R.id.exercise_placeholder, fragment, ExerciseFragment.TAG);
                                transaction2.commit();
                            }else {
                                mExercise = new Exercise();
                                fragment = ExerciseFragment.create(mExercise._id, mExercise);
                                if (mExercise != null) fragment.setExercise(mExercise);
                                fragment.setPendingFlag(0);
                                FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                                transaction2.replace(R.id.exercise_placeholder, fragment, ExerciseFragment.TAG);
                                transaction2.commit();
                            }

                            doSnackbar("Sets updated with matching exercise", Snackbar.LENGTH_LONG);
                        });

                    };
                    if (pendingExerciseSameNameList.size() > 0)
                        new Handler().post(setUpdater);
                }
                else
                    doSnackbar(getString(R.string.common_signin_button_text), Snackbar.LENGTH_LONG);

            }
        }

        @Override
        public void onCustomConfirmDetach() {

        }
    };
    ///////////////////////////////////////
    // FragmentInterface
    ///////////////////////////////////////

    @Override
    public void OnFragmentInteraction(int srcId, long selectedId, String text) {
        if (srcId == R.id.listExercise){
            String sTemp = '%' + text + '%';
            List<Exercise> possible = getPossibleExercises(selectedId, sTemp);
            if ((fragment != null) && (possible.size() > 0)){
                fragment.setMatchingList(possible);
            }
        }
/*        if (srcId == R.id.btnUseMatch){
            mExercise = fragment.getExercise();
            String sUserID = appPrefs.getLastUserID();
            String sDeviceID = appPrefs.getDeviceID();
            List<Long> workoutList = new ArrayList<>();
            // find the original pending to find the set ids
            if ((sUserID.length() > 0) && (sDeviceID.length() > 0)) {
                for (Exercise pending : pendingExercises) {
                    if (pending.equals(mExercise)) {  // this will be a set id value
                        setList = pending.workoutExercise;
                        break;
                    }
                }
                Exercise matchingExercise = fragment.getMatchingExercise();
                if (setList.contains(SHOT_DELIM)) {
                    String [] perSet = setList.split(SHOT_DELIM);
                    for (int i=0; i< perSet.length; i++){
                        WorkoutSet set = mSessionViewModel.getWorkoutSetById(Long.parseLong(perSet[i]),sUserID,sDeviceID);
                        if (set != null) {
                            workoutList.add(set.workoutID);
                            set.exerciseID = Math.toIntExact(matchingExercise._id);
                            set.exerciseName = matchingExercise.name;
                            set.bodypartID = matchingExercise.first_BPID;
                            set.bodypartName = matchingExercise.first_BPName;
                            set.resistance_type = matchingExercise.resistanceType;
                            set.per_end_xy = matchingExercise.workoutExercise;
                            mSessionViewModel.updateWorkoutSet(set);
                        }
                    }
                } else {
                    WorkoutSet set = mSessionViewModel.getWorkoutSetById(Long.parseLong(setList),sUserID,sDeviceID);
                    if (set != null) {
                        workoutList.add(set.workoutID);
                        set.exerciseID = Math.toIntExact(matchingExercise._id);
                        set.exerciseName = matchingExercise.name;
                        set.bodypartID = matchingExercise.first_BPID;
                        set.bodypartName = matchingExercise.first_BPName;
                        set.resistance_type = matchingExercise.resistanceType;
                        set.per_end_xy = matchingExercise.workoutExercise;
                        mSessionViewModel.updateWorkoutSet(set);
                    }
                    
                }
                Iterator<Exercise> pendingRemover = pendingExercises.iterator();
                while (pendingRemover.hasNext()){
                    Exercise removePending = pendingRemover.next();
                    if (removePending._id == mExercise._id){
                        pendingRemover.remove();
                        break;
                    }

                }
                fragment.setList(pendingExercises);
                mSessionViewModel.deleteExercise(mExercise);
                List<OneTimeWorkRequest> requestList = new ArrayList<>();
                for(long workoutID : workoutList){
                    Data.Builder builder = new Data.Builder();
                    builder.putString(KEY_FIT_USER, sUserID);
                    builder.putString(KEY_FIT_DEVICE_ID, sDeviceID);
                    builder.putLong(KEY_FIT_WORKOUTID, workoutID);
                    OneTimeWorkRequest workRequest =
                            new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                                    .setInputData(builder.build())
                                    .build();
                    requestList.add(workRequest);
                }
                WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                workManager.enqueue(requestList);

                DateTuple exerciseTuple = mSessionViewModel.getPendingExerciseCount();
                pendingExercises = mSessionViewModel.getPendingExercises();
                if ((pendingExercises.size() > 0) && (exerciseTuple.sync_count > 0)) {
                    mExercise = pendingExercises.get(0);
                    if (mExercise.bodypartCount < 0)
                        createToast("Assign bodyparts to exercises");
                    possibleExercises =  getPossibleExercises(mExercise._id, mExercise.name);
                    for(Exercise possible : possibleExercises){
                        possibleWorkoutExercises.add(possible.workoutExercise);
                    }

                    fragment = ExerciseFragment.create(mExercise._id, mExercise);
                    if (mExercise != null) fragment.setExercise(mExercise);
                    fragment.setPendingFlag(1);
                    if (pendingExercises != null)
                        fragment.setList(pendingExercises);
                    if (possibleExercises != null)
                        fragment.setMatchingList(possibleExercises);
                    FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                    transaction2.replace(R.id.exercise_placeholder, fragment, ExerciseFragment.TAG);
                    transaction2.commit();
                }else {
                    mExercise = new Exercise();
                    fragment = ExerciseFragment.create(mExercise._id, mExercise);
                    if (mExercise != null) fragment.setExercise(mExercise);
                    fragment.setPendingFlag(0);
                    FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                    transaction2.replace(R.id.exercise_placeholder, fragment, ExerciseFragment.TAG);
                    transaction2.commit();
                }
*//*                .getState().observe(this, new Observer<Operation.State>() {
                    @Override
                    public void onChanged(Operation.State state) {
                        if (state instanceof Operation.State.SUCCESS) {
                            mSessionViewModel.updateLastUpdate(mWorkout.userID,mWorkout.deviceID, mWorkout._id,OBJECT_TYPE_WORKOUT,2);
                            Intent refreshIntent = new Intent(INTENT_HOME_REFRESH);
                            refreshIntent.putExtra(KEY_FIT_ACTION, "changeState");
                            refreshIntent.putExtra(KEY_FIT_USER, mWorkout.userID);
                            refreshIntent.putExtra(KEY_FIT_VALUE, WORKOUT_PENDING);
                            sendBroadcast(refreshIntent);
                            // doAlertMessage("Data Preparation", "Report data collection completed", 1000L);
                        }

                    }
                });*//*
                doSnackbar("Sets updated with matching exercise", Snackbar.LENGTH_LONG);
            }
            else
                doSnackbar(getString(R.string.common_signin_button_text), Snackbar.LENGTH_LONG);
        }*/

        if ((srcId == Constants.UID_btnExerciseUseMatch) || (srcId == R.id.listPossibleExercise)){
            Exercise pendingEx = fragment.getPendingExercise();
            Exercise matchingExercise = fragment.getMatchingExercise();
            if ((pendingEx != null) && (matchingExercise != null)) {
                String sQuestion = "Confirm use existing: " + matchingExercise.name + Constants.LINE_DELIMITER + "for: " + pendingEx.name;
                CustomConfirmDialog confirmDialog = CustomConfirmDialog.newInstance(Constants.QUESTION_PICK_EXERCISE, sQuestion, confirmCallback);
                confirmDialog.show(getSupportFragmentManager(), CustomConfirmDialog.class.getSimpleName());
            }
        }
    }

    @Override
    public void onItemSelected(int pos, long id, String title, long resId, String identifier) {

    }

    ///////////////////////////////////////
    // IEntityFragmentActivityCallback
    ///////////////////////////////////////
    @Override
    public void onSaveCurrent() {
        if (mSavedStateViewModel.getDirtyCount() == 0) {
            createToast(getString(R.string.action_no_changes));
            return; // nothing to save!
        }
        mExercise = fragment.getExercise();
        mSavedStateViewModel.setExercise(mExercise);
        DateTuple dateTuple = mSessionViewModel.getExerciseCount();
        Exercise exercise = fragment.getExercise();
        boolean pending = (fragment.getPendingFlag() > 0);
        long newID = (dateTuple != null) ? dateTuple.maxdate + 1: 1;
        if (exercise != null) {
            if ((exercise.first_BPID == 0)){
                doSnackbar( getString(R.string.bodypart_primary_prompt), Snackbar.LENGTH_LONG);
                return;
            }
            if ((exercise.workoutExercise.length() == 0)){
                doSnackbar(getString(R.string.exercise_workname_prompt), Snackbar.LENGTH_LONG);
                return;
            }

            int bodypartCount = 0;
            if ((exercise.first_BPID != null) && (exercise.first_BPID > 0)) bodypartCount++;
            if ((exercise.second_BPID != null) && (exercise.second_BPID > 0)) bodypartCount++; else exercise.second_BPName = Constants.ATRACKIT_EMPTY;
            if ((exercise.third_BPID != null) && (exercise.third_BPID > 0)) bodypartCount++;  else exercise.third_BPName = Constants.ATRACKIT_EMPTY;
            if ((exercise.fourth_BPID != null) && (exercise.fourth_BPID > 0)) bodypartCount++;  else exercise.fourth_BPName = Constants.ATRACKIT_EMPTY;
            if (bodypartCount == 0 ){
                doSnackbar(getString(R.string.bodypart_primary_prompt), Snackbar.LENGTH_LONG);
                return;
            }
            exercise.bodypartCount = bodypartCount;
            if (exercise.resistanceType != null)
                exercise.resistanceTypeName = Utilities.getResistanceType(exercise.resistanceType);
            String sName = ((exercise.name == null) ? Constants.ATRACKIT_EMPTY : exercise.name);
            if (sName.length() == 0){
                doSnackbar( getString(R.string.bodypart_primary_prompt), Snackbar.LENGTH_LONG);
                return;
            }

            boolean bFound = false;
            final long timeMs = System.currentTimeMillis();
            Exercise ex = new Exercise();
            List<Exercise> exList = new ArrayList<>();
            try {
                exList =  mSessionViewModel.getExercisesLike('%' + sName + '%');   //repository.getExercisesLikeName('%' + sName + '%');       // new AsyncExerciseByName(mExerciseDao).execute(sName).get();
                bFound = ((exList != null) && (exList.size() > 0));
                if (bFound) ex = exList.get(0);
            }catch (Exception e){
                Log.e(LOG_TAG,e.getMessage());
                createToast("Error " + e.getLocalizedMessage());
                return;
            }
            if (exercise._id == 0){
                if (bFound) {
                    final long existingID = ex._id;
                    String sMsg = String.format(Locale.getDefault(),getString(R.string.exercise__name_found), ex.name,ex.resistanceTypeName,ex.first_BPName);
                    Log.i(LOG_TAG, sMsg);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                    builder.setMessage(sMsg)
                            .setPositiveButton(getString(R.string.save_button_text), (dialog, which) -> {
                                exercise._id = existingID;
                                exercise.lastUpdated = timeMs;
                                try{
                                    mSessionViewModel.updateExercise(exercise);
                                }catch(Exception e){
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                    Log.e(LOG_TAG,"Exercise update failed " + e.getMessage());
                                }
                                if (returnResultFlag > 0) {
                                    setResult(RESULT_OK, getResultIntent());
                                }
                                finishAfterTransition();
                            })
                            .setNegativeButton(getString(R.string.cancel_button_text), (dialog, id) -> {
                                // User cancelled the dialog
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return;
                } else {
                    // we have a new max id
                    if (newID > 0) {
                        exercise._id = newID;
                        try{
                            exercise.lastUpdated = timeMs;
                            mSessionViewModel.insertExercise(exercise);
                        }catch(Exception e){
                            FirebaseCrashlytics.getInstance().recordException(e);
                            Log.e(LOG_TAG,"Exercise update failed " + e.getMessage());
                        }
                    }
                }
            }
            else{
                try {
                    exercise.lastUpdated = timeMs;
                    if (!pending){
                        mSessionViewModel.updateExercise(exercise);
                        createToast(getString(R.string.action_updated));
                    }else {

                    }
                }catch(Exception e){
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Log.e(LOG_TAG,"Exercise update failed " + e.getMessage());
                }
            }
        }
        if (!pending || (fragment.getExerciseList().size() == 1)) {
            if (returnResultFlag > 0) {
                Intent msgIntent = new Intent(Constants.INTENT_MESSAGE_TOAST);
                msgIntent.putExtra(INTENT_EXTRA_MSG, getString(R.string.action_saved));
                msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                getApplicationContext().sendBroadcast(msgIntent);
                //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(msgIntent);
                setResult(RESULT_OK, getResultIntent());
            }
            finishAfterTransition();
        }
    }

    @Override
    public void onChangedState(boolean dirty) {
/*        exercise_save_button.setEnabled(dirty);
        exercise_save_button.setEnabled(!dirty);*/
        if (fragment.getPendingFlag() == 0) {
            ActionBar actionBar = getSupportActionBar();
            final String sAdd = getString(R.string.action_add) + Constants.ATRACKIT_SPACE + getString(R.string.label_exercise);
            final String sEdit = getString(R.string.action_edit) + Constants.ATRACKIT_SPACE + getString(R.string.label_exercise);
            mExercise = fragment.getExercise();
            mSavedStateViewModel.setDirtyCount(dirty ? 1 : 0);
            mSavedStateViewModel.setExercise(mExercise);
            if (actionBar != null) {
                if (mExercise._id == 0)
                    actionBar.setTitle(sAdd);
                else
                    actionBar.setTitle(sEdit);
            }
        }
    }
///////////////////////////////////////
    // LIFE CYCLE
    ///////////////////////////////////////
@Override
protected void onSaveInstanceState(@NonNull Bundle outState) {
    outState.putString(KEY_FIT_USER, sUserID);
    outState.putString(KEY_FIT_DEVICE_ID, sDeviceID);
    outState.putInt(KEY_FIT_TYPE, pendingFlag);
    outState.putInt(ARG_RETURN_RESULT, returnResultFlag);
    if (mExercise != null) outState.putParcelable(Exercise.class.getSimpleName(), mExercise);
    super.onSaveInstanceState(outState);
}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();
        if (toolbar != null){
            Drawable drawableUnChecked = AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_close_white);
            Utilities.setColorFilter(drawableUnChecked, ContextCompat.getColor(getApplicationContext(), R.color.secondaryTextColor));
            toolbar.setNavigationIcon(drawableUnChecked);
        }
        WorkoutViewModelFactory factory = com.a_track_it.fitdata.common.InjectorUtils.getWorkoutViewModelFactory(getApplicationContext());
        mSessionViewModel = new ViewModelProvider(ExerciseActivity.this, factory).get(WorkoutViewModel.class);
        mMessagesViewModel = new ViewModelProvider(ExerciseActivity.this).get(MessagesViewModel.class);
        mSavedStateViewModel = new ViewModelProvider(ExerciseActivity.this, new SavedStateViewModelFactory(getApplication(), ExerciseActivity.this)).get(SavedStateViewModel.class);
        appPrefs = ApplicationPreferences.getPreferences(getApplicationContext());
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
        pendingFlag = 0; long exerciseID = 0;
        mExercise = new Exercise();
        sUserID = (getIntent().hasExtra(KEY_FIT_USER)) ? getIntent().getStringExtra(KEY_FIT_USER) : Constants.ATRACKIT_EMPTY;
        sDeviceID = (getIntent().hasExtra(KEY_FIT_DEVICE_ID)) ? getIntent().getStringExtra(KEY_FIT_DEVICE_ID) : Constants.ATRACKIT_EMPTY;
        returnResultFlag = (getIntent().hasExtra(ARG_RETURN_RESULT) ? getIntent().getIntExtra(ARG_RETURN_RESULT, 0) : 0);
        Bundle incomingBundle = getIntent().getExtras();
        if (incomingBundle != null) {
            if (incomingBundle.containsKey(Constants.KEY_FIT_TYPE))
                pendingFlag = incomingBundle.getInt(Constants.KEY_FIT_TYPE, 0);
            if (incomingBundle.containsKey(ARG_EXERCISE_OBJECT)) {
                Exercise ex = incomingBundle.getParcelable(ARG_EXERCISE_OBJECT);
                if (ex != null) {
                    mExercise = ex;
                    exerciseID = ex._id;
                }
            }else{
                exerciseID = (incomingBundle.containsKey(ARG_EXERCISE_ID)) ? incomingBundle.getLong(ARG_EXERCISE_ID, 0) : 0;
                if (exerciseID > 0) mExercise = mSessionViewModel.getExerciseById(exerciseID);
            }
            if (incomingBundle.containsKey(KEY_FIT_HOST))
                mMessagesViewModel.setPhoneAvailable(incomingBundle.getBoolean(KEY_FIT_HOST, false));
            else
                mMessagesViewModel.setPhoneAvailable(false);
        }
        else{
            if (savedInstanceState != null){
                mExercise = savedInstanceState.getParcelable(Exercise.class.getSimpleName());
                sUserID = savedInstanceState.getString(KEY_FIT_USER);
                sDeviceID = savedInstanceState.getString(KEY_FIT_DEVICE_ID);
                pendingFlag = savedInstanceState.getInt(Constants.KEY_FIT_TYPE,0);
                returnResultFlag = savedInstanceState.getInt(ARG_RETURN_RESULT, 1);
            }else {
                if (mSavedStateViewModel.getExercise().getValue() != null)
                    mExercise = mSavedStateViewModel.getExercise().getValue();
                if ((pendingFlag == 0) && (mSavedStateViewModel.getIconID() > 0))  //TODO: investigate this
                    pendingFlag = mSavedStateViewModel.getIconID();
            }

        }
        container = findViewById(R.id.exercise_placeholder);

/*        exercise_cancel_button = findViewById(R.id.exercise_cancel_button);
        exercise_cancel_button.setOnClickListener(v -> {
            if (returnResultFlag > 0)
                setResult(RESULT_CANCELED, getResultIntent());
            finishAfterTransition();
        });
        exercise_save_button = findViewById(R.id.exercise_save_button);
        exercise_save_button.setOnClickListener(v -> { onSaveCurrent();});

        if (exercise_send_button == null) exercise_send_button = findViewById(R.id.exercise_send_button);
        exercise_send_button.setOnClickListener(v -> {
            Intent resultIntent  = new Intent(Constants.INTENT_EXTRA_RESULT);
            resultIntent.putExtra(ARG_EXERCISE_ID, mExercise._id);
            resultIntent.putExtra(ARG_EXERCISE_OBJECT, mExercise);
            resultIntent.putExtra(ARG_RETURN_RESULT, returnResultFlag);
            resultIntent.putExtra(ARG_REQUEST_SEND, 1);
            if (returnResultFlag > 0)
                setResult(RESULT_OK, resultIntent);
            finishAfterTransition();
        });*/
        if ((pendingFlag == 1) || (mExercise._id > 50000)){
            pendingExercises = mSessionViewModel.getPendingExercises();
            if (pendingExercises.size() > 0){
                mExercise = pendingExercises.get(0);
                exerciseID = mExercise._id;
                if (mExercise.bodypartCount < 0)
                    createToast("Assign bodyparts to exercises");
                possibleExercises =  getPossibleExercises(mExercise._id, mExercise.name);
                for(Exercise possible : possibleExercises){
                    possibleWorkoutExercises.add(possible.workoutExercise);
                }
            }
        }

        if ((mExercise._id == 0) && (exerciseID > 0)){
            try {
                mExercise = mSessionViewModel.getExerciseById(exerciseID);
            }catch (Exception e){
                Log.e(LOG_TAG, e.getMessage());
            }
        }
        if (mExercise != null){
            mSavedStateViewModel.setExercise(mExercise);
        }
        final String sAdd = getString(R.string.action_add) + Constants.ATRACKIT_SPACE + getString(R.string.label_exercise);
        final String sEdit = getString(R.string.action_edit) + Constants.ATRACKIT_SPACE + getString(R.string.label_exercise);
        if (actionBar != null) {
            if (exerciseID == 0)
                actionBar.setTitle(sAdd);
            else
                actionBar.setTitle(sEdit);
        }
        mExerciseNameListAdapter = new ExerciseListAdapter(ExerciseActivity.this, R.layout.autocomplete_dropdown_item, exerciseNameAutoList);
        mExerciseNameListAdapter.setOnItemClickListener(autoCompleteNameItemClickListener);

        fragment = ExerciseFragment.create(exerciseID, mExercise);
        if (mExercise != null) fragment.setExercise(mExercise);
        if ((pendingFlag == 1) && (pendingExercises.size() > 0)){
            fragment.setPendingFlag(pendingFlag);
            if (pendingExercises != null)
                fragment.setList(pendingExercises);
            if (possibleExercises != null)
                fragment.setMatchingList(possibleExercises);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.exercise_placeholder, fragment, ExerciseFragment.TAG);
        transaction.commit();
    }

/*    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }*/

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_exercise;
    }

    ///////////////////////////////////////
    // OPTIONS MENU
    ///////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.exercise, menu);

       // mMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menu != null) {
            MenuItem mPendingItem = menu.findItem(R.id.menu_pending_exercise);
            DateTuple exerciseTuple = mSessionViewModel.getPendingExerciseCount();
            if (exerciseTuple.sync_count > 0){
                String sTitle = String.format(getString(R.string.action_pending_exercise),exerciseTuple.sync_count);
                mPendingItem.setTitle(sTitle);
                Drawable icon = AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_edit_black_24dp);
                Utilities.setColorFilter(icon, getColor(R.color.white));
                mPendingItem.setIcon(icon);
            }else {
                Drawable icon = AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_outline_check_white);
                mPendingItem.setTitle(getString(R.string.action_no_pending_exercise));
                mPendingItem.setIcon(icon);
            }

            //MenuItem mSendItem = menu.findItem(R.id.action_new_exercise);
            //mSendItem.setVisible(mMessagesViewModel.hasPhone());

            MenuItem mSearchItem = menu.findItem(R.id.search_exercise);
            mSearchView = (ArrayAdapterSearchView)mSearchItem.getActionView();
            mSearchView.setAdapter(mExerciseNameListAdapter);
            mSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    mSearchView.setText(mExerciseNameListAdapter.getItem(position).toString());
                }
            });
/*            SearchView mSearchView = (SearchView) mSearchItem.getActionView();
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setQueryHint("Search");
            mSearchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    return true;
                }
            });*/
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
           //     mExercise = fragment.getExercise();
                // dirty and savable
/*                if (exercise_save_button.isEnabled() && ((mExercise.name != null) && (mExercise.name.length() > 0))
                && (mExercise.first_BPID == 0) && (mExercise.resistanceType == 0)){
                    exercise_save_button.callOnClick();
                }*/
                // Reverse animation back to previous activity
                if (returnResultFlag > 0)
                    setResult(RESULT_CANCELED, getResultIntent());
                ActivityCompat.finishAfterTransition(this);
                return true;
            case R.id.menu_new_exercise:
                mExercise = fragment.getExercise();
                if (mSavedStateViewModel.getDirtyCount() > 0)
                    onSaveCurrent();
                else {
                    mExercise = new Exercise();
                    fragment = ExerciseFragment.create(mExercise._id, mExercise);
                    if (mExercise != null) fragment.setExercise(mExercise);
                    fragment.setPendingFlag(0);
                    if (pendingExercises != null)
                        fragment.setList(pendingExercises);
                    if (possibleExercises != null)
                        fragment.setMatchingList(possibleExercises);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.exercise_placeholder, fragment, ExerciseFragment.TAG);
                    transaction.commit();
                }
                break;
            case R.id.menu_pending_exercise:
                DateTuple exerciseTuple = mSessionViewModel.getPendingExerciseCount();
                pendingExercises = mSessionViewModel.getPendingExercises();
                if ((pendingExercises.size() > 0) && (exerciseTuple.sync_count > 0)) {
                        mExercise = pendingExercises.get(0);
                        if (mExercise.bodypartCount < 0)
                            createToast("Assign bodyparts to exercises");
                        possibleExercises =  getPossibleExercises(mExercise._id, mExercise.name);
                        for(Exercise possible : possibleExercises){
                            possibleWorkoutExercises.add(possible.workoutExercise);
                        }

                    fragment = ExerciseFragment.create(mExercise._id, mExercise);
                    if (mExercise != null) fragment.setExercise(mExercise);
                    fragment.setPendingFlag(1);
                    if (pendingExercises != null)
                        fragment.setList(pendingExercises);
                    if (possibleExercises != null)
                        fragment.setMatchingList(possibleExercises);
                    FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                    transaction2.replace(R.id.exercise_placeholder, fragment, ExerciseFragment.TAG);
                    transaction2.commit();
                }else
                    createToast(getString(R.string.action_no_pending_exercise));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doSnackbar(String sMsg, int length){
        Snackbar.make(container, sMsg, length).setAction(getString(R.string.action_okay)
                , view -> {return;}).show();
    }
    private List<Exercise> getPossibleExercises(long originId, String sName){
        String[] nameComponents = new String[]{};
        List<Exercise> possibleList = new ArrayList<>();
        List<ResistanceType> typeList = mSessionViewModel.getResistanceTypeList();
        List<Bodypart> bodypartList = mSessionViewModel.getBodypartList();
        String sTemp;
        if (sName.contains(ATRACKIT_SPACE)){
            nameComponents = sName.split(ATRACKIT_SPACE);
        }else
            if (sName.contains(String.valueOf(95))){
                nameComponents = sName.split(String.valueOf(95));  // under_score
            }else
            if (sName.contains(String.valueOf(46))){
                nameComponents = sName.split(String.valueOf(46));  // dot .
            }else
                if (sName.contains(String.valueOf(45))){
                    nameComponents = sName.split(String.valueOf(45));  // dash -
                }
        int componentsSize = nameComponents.length;
        if (componentsSize > 0){
            for(int i=0; i < componentsSize; i++){
                sTemp = nameComponents[i].toLowerCase().trim();
                boolean bProcessed = false;
                // check if component is a type name
                for(ResistanceType rt : typeList){
                    if (rt.resistanceName.toLowerCase().equals(sTemp)
                            || rt.resistanceName.toLowerCase().equals(Constants.ATRACKIT_HAMMER_STRENGTH.toLowerCase())){
                        // search for possibles without the type name included
                        if (i+1 < componentsSize){
                            int y = i+1;
                            String sTemp2 = nameComponents[y];
                            while (++y < componentsSize){
                                sTemp2 = sTemp2 + ATRACKIT_SPACE + nameComponents[y];
                            }
                            String sTemp3 = '%' + sTemp2.toLowerCase() + '%';
                            List<Exercise> newList = mSessionViewModel.getExercisesLike(sTemp3);
                            if ((newList != null) && (newList.size() > 0)) {
                                Iterator<Exercise> iterator = newList.iterator();
                                while (iterator.hasNext()) {
                                    if (iterator.next()._id == originId)
                                        iterator.remove();
                                }
                                if (newList.size() > 0) {
                                    if (possibleList.size() > 0) {
                                        for (Exercise existing : possibleList) {
                                            Iterator<Exercise> iterator2 = newList.iterator();
                                            while (iterator2.hasNext()) {
                                                if (iterator2.next()._id == existing._id)
                                                    iterator2.remove();
                                            }
                                        }

                                    }
                                    possibleList.addAll(newList);
                                }
                            }
                        }
                        bProcessed = true;
                        break;
                    }
                }
                if (!bProcessed){
                    for(Bodypart bp: bodypartList){
                        if (bp.shortName.toLowerCase().equals(sTemp)){
                            // search for possibles without the bodypart name included
                            if (i+1 < componentsSize){
                                int y = i+1;
                                String sTemp2 = nameComponents[y];
                                while (++y < componentsSize){
                                    sTemp2 = sTemp2 + ATRACKIT_SPACE + nameComponents[y];
                                }
                                String sTemp3 = '%' + sTemp2.toLowerCase() + '%';
                                List<Exercise> newList = mSessionViewModel.getExercisesLike(sTemp3);
                                if ((newList != null) && (newList.size() > 0)) {
                                    Iterator<Exercise> iterator = newList.iterator();
                                    while (iterator.hasNext()) {
                                        if (iterator.next()._id == originId)
                                            iterator.remove();
                                    }
                                    if (newList.size() > 0) {
                                        if (possibleList.size() > 0) {
                                            for (Exercise existing : possibleList) {
                                                Iterator<Exercise> iterator2 = newList.iterator();
                                                while (iterator2.hasNext()) {
                                                    if (iterator2.next()._id == existing._id)
                                                        iterator2.remove();
                                                }
                                            }

                                        }
                                        possibleList.addAll(newList);
                                    }
                                }
                            }
                            bProcessed = true;
                            break;

                        }
                    }
                }
                if (!bProcessed){  // now try with the rest of the components
                    if (i+1 < componentsSize){
                        int y = i+1;
                        String sTemp2 = nameComponents[y];
                        while (++y < componentsSize){
                            sTemp2 = sTemp2 + ATRACKIT_SPACE + nameComponents[y];
                        }
                        String sTemp3 = '%' + sTemp2.toLowerCase() + '%';
                        List<Exercise> newList = mSessionViewModel.getExercisesLike(sTemp3);
                        if ((newList != null) && (newList.size() > 0)) {
                            Iterator<Exercise> iterator = newList.iterator();
                            while (iterator.hasNext()) {
                                if (iterator.next()._id == originId)
                                    iterator.remove();
                            }
                            if (newList.size() > 0) {
                                if (possibleList.size() > 0) {
                                    for (Exercise existing : possibleList) {
                                        Iterator<Exercise> iterator2 = newList.iterator();
                                        while (iterator2.hasNext()) {
                                            if (iterator2.next()._id == existing._id)
                                                iterator2.remove();
                                        }
                                    }

                                }
                                possibleList.addAll(newList);
                            }
                        }
                        // other names
                        List<Exercise> newList2 = mSessionViewModel.getExercisesLikeOther(sTemp3);
                        if ((newList2 != null) && (newList2.size() > 0)) {
                            Iterator<Exercise> iterator = newList2.iterator();
                            while (iterator.hasNext()) {
                                if (iterator.next()._id == originId)
                                    iterator.remove();
                            }
                            if (newList2.size() > 0) {
                                if (possibleList.size() > 0) {
                                    for (Exercise existing : possibleList) {
                                        Iterator<Exercise> iterator2 = newList2.iterator();
                                        while (iterator2.hasNext()) {
                                            if (iterator2.next()._id == existing._id)
                                                iterator2.remove();
                                        }
                                    }

                                }
                                possibleList.addAll(newList2);
                            }
                        }

                    }
                }
            }
        }
        return possibleList;
    }

    private void createToast(String msg){
        Handler handler = new Handler(Looper.myLooper());
        handler.post(() -> Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show());
    }
    private Intent getResultIntent(){
        Intent resultIntent  = new Intent(Constants.INTENT_EXERCISE_NEW);
        resultIntent.putExtra(ARG_EXERCISE_ID, mExercise._id);
        resultIntent.putExtra(ARG_EXERCISE_OBJECT, mExercise);
        resultIntent.putExtra(ARG_RETURN_RESULT, returnResultFlag);
        return resultIntent;
    }

    ///////////////////////////////////////
    // SEARCH CALLBACKS
    ///////////////////////////////////////
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
       // mExerciseNameListAdapter.getFilter().filter(newText);
        return false;
    }
}

