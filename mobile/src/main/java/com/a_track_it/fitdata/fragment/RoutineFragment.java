package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.adapter.WorkoutAdapter;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutMeta;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModel;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.a_track_it.fitdata.common.user_model.LiveDataTimerViewModel;
import com.a_track_it.fitdata.common.user_model.SavedStateViewModel;
import com.google.android.gms.fitness.data.Field;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.fitdata.activity.RoutineActivity.ARG_WORKOUT_META_OBJECT;
import static com.a_track_it.fitdata.activity.RoutineActivity.ARG_WORKOUT_SET_LIST;
import static com.a_track_it.fitdata.activity.RoutineActivity.ARG_WORKOUT_SET_OBJECT;
import static com.a_track_it.fitdata.common.Constants.ATRACKIT_EMPTY;
import static com.a_track_it.fitdata.common.Constants.INTENT_EXTRA_MSG;
import static com.a_track_it.fitdata.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.fitdata.common.Constants.USER_PREF_USE_SET_TRACKING;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_TYPE_STRENGTH;

public class RoutineFragment extends Fragment {
    private static final String LOG_TAG = RoutineFragment.class.getSimpleName();
    private static final String ARG_WORKOUT_ID = "ARG_WORKOUT_ID";
    private static final String ARG_WORKOUT_OBJECT = "ARG_WORKOUT_OBJECT";
    private static int TYPE_GENERAL = 0;
    private static int TYPE_GYM = 1;
    private static int TYPE_SHOOT = 2;

    private int formatType = TYPE_GENERAL;

    private LiveDataTimerViewModel mTimerViewModel;
    private SavedStateViewModel mSavedStateViewModel;
   // private MessagesViewModel mMessagesViewModel;
    private WorkoutViewModel mSessionViewModel;
    private FragmentInterface mListener;

    private ConstraintLayout mConstraintLayout;
    private ScrollView mScrollView;
    private MaterialButton btnCurrentSet;
    private MaterialButton btnEnableBuild;

    private TextView textViewActivityName;
    private TextView textViewBottom;
    private MaterialButton btnBodyRegion;
    private MaterialButton btnBodypart;
    private MaterialButton btnExercise;
    private MaterialButton btnAddExercise;
    private MaterialButton btnSets;
    private MaterialButton btnReps;
    private MaterialButton btnWeight;
    private MaterialButton btnBuild;
    private MaterialButton btnHistorySave;
    private MaterialButton btnStart;
 //   private TextView textViewOfflineLabel;
    private com.google.android.material.switchmaterial.SwitchMaterial switchOffline;
    private MaterialButton btnRest;
    private MaterialButton btnRoutineName;
    private MaterialButton btnFinish;
    private MaterialButton entry_use_track_sets;
    private MaterialButton entry_use_timed_rest_toggle;
    private MaterialButton entry_auto_start_toggle;
    private RecyclerView recyclerViewItems;
    private WorkoutAdapter workoutAdapter;
    private boolean bUseKG = false;
    private boolean bOffline = false;
    private WorkoutSet mBackupSet;
    private Workout mWorkout;
    private WorkoutSet mEditorSet;
    private WorkoutMeta mWorkoutMeta;
    private UserPreferences userPrefs;
    /** myClicker - propagate click to RoomActivity via FragmentInterface
     - added form validation for Gym exercises
     **/
    View.OnClickListener myClicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(), formatType, null);
        }
    };

    View.OnLongClickListener myLongClicker = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(), formatType, Constants.LABEL_LONG);
            return true;
        }
    };

    public static RoutineFragment create(long workoutID, Workout mWorkout, WorkoutSet set, WorkoutMeta meta) {
        Bundle args = new Bundle();
        args.putLong(ARG_WORKOUT_ID, workoutID);
        if (mWorkout != null) args.putParcelable(ARG_WORKOUT_OBJECT, mWorkout);
        if (mWorkout != null) args.putParcelable(ARG_WORKOUT_SET_OBJECT, set);
        if (mWorkout != null) args.putParcelable(ARG_WORKOUT_META_OBJECT, meta);
        RoutineFragment fragment = new RoutineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void observerWorkout(Workout workout){
        if ((workout != null) && (workout.activityID != null && workout.activityID > 0)) {
            final Drawable drawableChecked = AppCompatResources.getDrawable(mConstraintLayout.getContext(),R.drawable.ic_outline_check_white);
            Utilities.setColorFilter(drawableChecked,ContextCompat.getColor(mConstraintLayout.getContext(), R.color.colorSplash));

            final Drawable drawableUnChecked = AppCompatResources.getDrawable(mConstraintLayout.getContext(),R.drawable.ic_close_white);
            Utilities.setColorFilter(drawableUnChecked,ContextCompat.getColor(mConstraintLayout.getContext(), R.color.secondaryTextColor));
            Context context1 = mConstraintLayout.getContext();
            final String startLabel = (workout.start > 0) ? context1.getString(R.string.action_continue) : context1.getString(R.string.session_start);
            Log.e(LOG_TAG, "observeWorkout " + workout.toString());
            if ((workout.parentID == null) || (workout.parentID > 0)){
                if ((workout.name != null) && (workout.name.length() > 0)){
                    textViewActivityName.setText(workout.name);
                    textViewActivityName.setTextSize(TypedValue.COMPLEX_UNIT_SP, getLabelFontSize(workout.name));
                    if (workout.parentID > 0) {
                        Drawable favList = AppCompatResources.getDrawable(mConstraintLayout.getContext(), R.drawable.ic_favlist);
                        textViewActivityName.setCompoundDrawablesRelativeWithIntrinsicBounds(favList,null,null,null);
                    }else{
                        textViewActivityName.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,null,null);
                    }
                }else
                    textViewActivityName.setText(workout.activityName);
            }else{
                if ((workout.name != null) && (workout.parentID < 0)){
                    String sTemp = getString(R.string.label_template) + Constants.LINE_DELIMITER + workout.name;
                    textViewActivityName.setText(sTemp);
                    Drawable favList = AppCompatResources.getDrawable(mConstraintLayout.getContext(), R.drawable.ic_heart_outline);
                    textViewActivityName.setCompoundDrawablesRelativeWithIntrinsicBounds(favList, null, null,null);
                }else {
                    Drawable d = AppCompatResources.getDrawable(mConstraintLayout.getContext(), R.drawable.ic_heart_add);
                    textViewActivityName.setText(workout.activityName);
                    textViewActivityName.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null,null);
                }
            }
           switchOffline.setChecked(workout.offline_recording == 1);
           if  (!Utilities.isGymWorkout(workout.activityID)){
                if ((workout.name != null) && (workout.name.length() > 0))
                    textViewBottom.setText(workout.name);
                else
                    textViewBottom.setText(workout.activityName);

                if (workout.start > 0) {
                    btnStart.setText(startLabel);
                }
                String slabel1 = context1.getString(R.string.label_goal_steps);
                if (workout.goal_steps > 0)
                    slabel1 = slabel1.replace("(steps)", "(" + workout.goal_steps + " steps)");
                btnBodypart.setText(slabel1);
                Drawable icon = AppCompatResources.getDrawable(context1,R.drawable.ic_footsteps_silhouette_variant);
                btnBodypart.setIcon(icon);
                btnBodypart.setIconTint(context1.getColorStateList(R.color.primaryTextColor));
                slabel1 = context1.getString(R.string.label_goal_duration);
                if (workout.goal_duration > 0)
                    slabel1 = slabel1.replace("Goal (min)", "Goal (" + workout.goal_duration + " min)");
                btnRoutineName.setText(slabel1);
                Drawable icon1 = AppCompatResources.getDrawable(context1,R.drawable.ic_stopwatch_white);
                btnRoutineName.setIcon(icon1);
                btnRoutineName.setIconTint(context1.getColorStateList(R.color.primaryTextColor));
                btnRoutineName.setVisibility(MaterialButton.VISIBLE);
                btnBodyRegion.setVisibility(MaterialButton.VISIBLE);
                btnBodyRegion.setText(startLabel);
                btnBodyRegion.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getContext(), R.drawable.ic_play_button_white),
                        null,null,null);
                btnCurrentSet.setVisibility(MaterialButton.GONE);
                btnEnableBuild.setVisibility(MaterialButton.GONE);
                btnBuild.setVisibility(MaterialButton.GONE);
                btnAddExercise.setVisibility(MaterialButton.GONE);
                btnReps.setVisibility(MaterialButton.GONE);
                btnExercise.setVisibility(MaterialButton.GONE);
                btnWeight.setVisibility(MaterialButton.GONE);
                btnRest.setVisibility(MaterialButton.GONE);

                btnHistorySave.setVisibility(View.VISIBLE);
                btnFinish.setVisibility((workout.start > 0) ? View.VISIBLE: View.GONE);
                entry_use_track_sets.setVisibility(MaterialButton.GONE);
                entry_auto_start_toggle.setVisibility(MaterialButton.GONE);
                entry_use_timed_rest_toggle.setVisibility(MaterialButton.GONE);
                recyclerViewItems.setVisibility(RecyclerView.GONE);
            }
           else{
               // template
               if ((workout.parentID != null) && workout.parentID < 0){
                   btnRoutineName.setVisibility(View.GONE);
                   btnFinish.setVisibility(View.GONE);
                   btnHistorySave.setVisibility(View.VISIBLE);
               }else {
                   btnStart.setText(startLabel);
                   if (((workout.start == 0) && (workout.end == 0)) || ((workout.start > 0) && (workout.end > 0))) {
                       if ((workout.name != null) && (workout.name.length() > 0)) {
                           textViewBottom.setText(workout.name);
                           btnRoutineName.setText(workout.name);
                           btnRoutineName.setTextSize(TypedValue.COMPLEX_UNIT_SP, Utilities.getLabelFontSize(workout.name));
                           btnRoutineName.setVisibility(View.VISIBLE);
                       } else {
                           btnRoutineName.setVisibility(View.GONE);
                       }
                       btnHistorySave.setVisibility(View.VISIBLE);
                   }else {
                       btnHistorySave.setVisibility(View.GONE);
                       if (mSavedStateViewModel.getSetIndex() > 1) {
                           String sName = String.format(getString(R.string.sets_summaryOf3),mSavedStateViewModel.getSetIndex(),workout.setCount);
                           textViewBottom.setText(sName);
                       }
                   }
                   btnFinish.setVisibility((workout.start > 0) ? View.VISIBLE: View.GONE);
               }
               entry_use_track_sets.setChecked(workout.scoreTotal != Constants.FLAG_NON_TRACKING);
               entry_use_timed_rest_toggle.setChecked(userPrefs.getTimedRest());
               if (userPrefs.getRestAutoStart()){
                   entry_auto_start_toggle.setIcon(drawableChecked);
               }else {
                   entry_auto_start_toggle.setIcon(drawableUnChecked);
               }
                if (userPrefs.getTimedRest() && workout.scoreTotal != Constants.FLAG_NON_TRACKING){
                    entry_use_timed_rest_toggle.setChecked(true);
                    entry_use_timed_rest_toggle.setIcon(drawableChecked);
                    entry_auto_start_toggle.setVisibility(View.VISIBLE);
                    btnRest.setVisibility(MaterialButton.VISIBLE);
                    btnRest.setEnabled(true);
                }else {
                    entry_use_timed_rest_toggle.setChecked(false);
                    entry_use_timed_rest_toggle.setIcon(drawableUnChecked);
                    entry_auto_start_toggle.setVisibility(View.GONE);
                    btnRest.setVisibility(MaterialButton.GONE);
                }
               if (userPrefs.getTimedRest()){
                   entry_use_timed_rest_toggle.setChecked(true);
                   entry_use_timed_rest_toggle.setIcon(drawableChecked);
                   entry_auto_start_toggle.setVisibility(View.VISIBLE);
                   btnRest.setVisibility(MaterialButton.VISIBLE);
                   btnRest.setEnabled(true);
               }else {
                   entry_use_timed_rest_toggle.setChecked(false);
                   entry_use_timed_rest_toggle.setIcon(drawableUnChecked);
                   entry_auto_start_toggle.setVisibility(View.GONE);
                   btnRest.setVisibility(MaterialButton.GONE);
               }
               if (workout.scoreTotal != Constants.FLAG_NON_TRACKING) {
                   btnBuild.setVisibility(View.VISIBLE);
                   btnSets.setVisibility(View.VISIBLE);
               }else{
                   btnBuild.setVisibility(View.GONE);
                   btnSets.setVisibility(View.GONE);
                   btnRest.setVisibility(View.GONE);
                   entry_use_timed_rest_toggle.setVisibility(View.GONE);
                   entry_auto_start_toggle.setVisibility(View.GONE);
               }
            } // end of if GYM
        } // valid workout
    }

    private final Observer<List<WorkoutSet>> observerSets = new Observer<List<WorkoutSet>>() {
        @Override
        public void onChanged(List<WorkoutSet> workoutSets) {
            if (workoutSets != null) {
                final ArrayList<WorkoutSet> sets1 = new ArrayList<>(workoutSets);
                sets1.sort((o1, o2) -> ((o1.setCount < o2.setCount) ? -1: ((o1.setCount > o2.setCount) ? 1 : 0)));
                int currentSet = mSavedStateViewModel.getSetIndex();
                String sSetMsg = mWorkout.activityName;
                int currentSetSize = 0;
                for (WorkoutSet set: workoutSets){
                    if (set.scoreTotal == Constants.FLAG_PENDING) currentSetSize++;
                }
                if ((mEditorSet != null && (currentSetSize > 0)) && (mEditorSet.scoreTotal != Constants.FLAG_PENDING || (mEditorSet.workoutID == 1)))
                    sSetMsg = String.format(Locale.getDefault(), getString(R.string.sets_summaryOf2), currentSetSize);
                else
                if ((currentSet > 0) && (currentSet <= currentSetSize) && (currentSetSize > 1))
                    sSetMsg = String.format(Locale.getDefault(), getString(R.string.sets_summaryOf), currentSet, currentSetSize);
                Log.w(LOG_TAG, "observerSets " + currentSetSize + " " + sSetMsg);
                final String sMsg = sSetMsg;
                final int sizer = currentSetSize;
              //  new Handler(Looper.myLooper()).post(() -> {
                workoutAdapter.setWorkoutSetArrayList(sets1);
                if (sizer == 0) recyclerViewItems.setVisibility(View.GONE); else recyclerViewItems.setVisibility(View.VISIBLE);
                textViewBottom.setText(sMsg);
               // });
            }else{
                workoutAdapter.setWorkoutSetArrayList(null);
                recyclerViewItems.setVisibility(View.GONE);
            }
            workoutAdapter.notifyDataSetChanged();
        }
    };
    private final Observer<Integer> observerNewSets = new Observer<Integer>() {
        @Override
        public void onChanged(Integer iTemp) {
            if (iTemp <= 20) {
                int resId = getNumberDrawableIdentifier(getContext(), iTemp);
                if (resId > 0){
                    btnSets.setIcon(ContextCompat.getDrawable(getContext(), resId));
                    btnSets.setIconTint(getContext().getColorStateList(R.color.primaryTextColor));
                    btnSets.setText(Constants.NEW_SET_TAIL);
                    return;
                }
            }
            String sTemp = Constants.NEW_SET_TAIL + Integer.toString(iTemp);
            btnSets.setIcon(null);
            btnSets.setText(sTemp);
        }
    };
    private final Observer<Integer> observerNewReps = new Observer<Integer>() {
        @Override
        public void onChanged(Integer iTemp) {
            if (iTemp <= 20) {
                int resId = getNumberDrawableIdentifier(getContext(), iTemp);
                if (resId > 0){
                    btnReps.setIcon(AppCompatResources.getDrawable(getContext(), resId));
                    btnReps.setIconTint(getContext().getColorStateList(R.color.primaryTextColor));
                    btnReps.setText(Constants.REPS_TAIL);
                    return;
                }
            }
            String sTemp = Integer.toString(iTemp) + Constants.REPS_TAIL;
            btnReps.setIcon(null);
            btnReps.setText(sTemp);

        }
    };
    private final Observer<WorkoutSet> observerSet = workoutSet -> {
        if (workoutSet == null) return;
        if ((workoutSet.activityID == null) && (workoutSet.activityID == 0)) return;
        Context context = getActivity();
        final String sBodypart = context.getString(R.string.label_bodypart);
        final String sExercise = context.getString(R.string.label_exercise);
        final String sWeight = context.getString(R.string.label_weight);
        final String sRest = context.getString(R.string.action_untimed_rest);
        String sTemp;
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        final boolean bDayMode = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_NO);
        Log.e(LOG_TAG, "observeSet " + workoutSet.toString());
        if (Utilities.isGymWorkout(workoutSet.activityID)) {
            boolean isValid = workoutSet.isValid(false);
            btnBuild.setEnabled(isValid);
            btnBuild.setChecked(isValid);
            if ((workoutSet.regionID != null) && (workoutSet.regionID > 0))
                btnBodyRegion.setText(workoutSet.regionName);
            else
                btnBodyRegion.setText(getString(R.string.label_bodyregion));

            if ((workoutSet.bodypartName != null) && (workoutSet.bodypartName.length() > 0))
                btnBodypart.setText(workoutSet.bodypartName);
            else {
                btnBodypart.requestFocus();
                btnBodypart.setText(sBodypart);
            }
            if ((workoutSet.exerciseName != null) && (workoutSet.exerciseName.length() > 0))
                btnExercise.setText(workoutSet.exerciseName);
            else {
                btnExercise.setText(sExercise);
                btnExercise.requestFocus();
            }
            if ((workoutSet.weightTotal != null) && (workoutSet.weightTotal > 0)) {
                if (bUseKG)
                    sTemp = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT,workoutSet.weightTotal); // + Constants.KG_TAIL;
                else
                    sTemp = Utilities.KgToPoundsDisplay(workoutSet.weightTotal) + ATRACKIT_EMPTY; // + Constants.LBS_TAIL;

                btnWeight.setText(sTemp);
            }else {
                if ((workoutSet.resistance_type != null) && (workoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY))
                    btnWeight.requestFocus();
                btnWeight.setText(sWeight);
            }
            if (workoutSet.rest_duration == null) workoutSet.rest_duration = 0L;
            if (workoutSet.rest_duration > 0){
                int iWeightRest = userPrefs.getWeightsRestDuration();
                setDurationRest(iWeightRest);
            }else
                btnRest.setText(sRest);
            if (entry_use_track_sets.isChecked())
                if (workoutSet.scoreTotal == Constants.FLAG_PENDING){
                    if (workoutSet.setCount > 0) {
                        String sSetMsg = String.format(Locale.getDefault(), getString(R.string.sets_summary1), workoutSet.setCount);
                        btnCurrentSet.setText(sSetMsg);
                    }
                    btnCurrentSet.setChecked(true);
                    btnCurrentSet.setVisibility(View.VISIBLE);
                    btnEnableBuild.setChecked(false);
                    btnSets.setVisibility(MaterialButton.GONE);
                    btnBuild.setVisibility(MaterialButton.GONE);
                    //textViewBottom.setText(sSetMsg);
                }
                else {
                    btnCurrentSet.setVisibility(View.GONE);
                    btnCurrentSet.setChecked(false);
                    btnEnableBuild.setChecked(true);
                    btnSets.setVisibility(MaterialButton.VISIBLE);
                    btnBuild.setVisibility(MaterialButton.VISIBLE);
                }
        }

    };

    private void broadcastToast(String msg){
        Context context = getContext();
        Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
        msgIntent.setAction(INTENT_MESSAGE_TOAST);
        msgIntent.putExtra(INTENT_EXTRA_MSG, msg);
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean bDayMode = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_NO);
        if (bDayMode)
            msgIntent.putExtra(KEY_FIT_TYPE, 2);
        else
            msgIntent.putExtra(KEY_FIT_TYPE, 1);
        msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
        context.sendBroadcast(msgIntent);
    }
    private float getLabelFontSize(String sName){
        float iRetVal = 14F;
        if (sName.length() < 15)
            iRetVal = 32F;
        else
        if (sName.length() <= 23)
            iRetVal = 26F;
        else
        if (sName.length() < 30)
            iRetVal = 21F;
        else
        if (sName.length() >= 30)
            iRetVal = 18F;
        return iRetVal;
    }
    private void setDurationRest(int iRestSeconds){
        long milliseconds = (TimeUnit.SECONDS.toMillis(iRestSeconds));
        String sTemp; Context context = mConstraintLayout.getContext();
        if (milliseconds == 0)
            btnRest.setText(context.getString(R.string.action_untimed_rest));
        else {
            int seconds = (int) (milliseconds / 1000) % 60;
            int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
            int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
            sTemp = context.getString(R.string.label_rest_countdown) + Constants.ATRACKIT_SPACE;
            if (hours > 0) {
                sTemp += String.format(Locale.getDefault(), Constants.SINGLE_INT, hours) + Constants.ATRACKIT_SPACE + Constants.HOURS_TAIL;
                if (hours == 1)
                    sTemp = sTemp.substring(0, sTemp.length() - 1);
                if (minutes > 1)
                    sTemp += Constants.ATRACKIT_SPACE;
            }
            if (minutes > 0) {
                if (seconds == 0) {
                    sTemp += String.format(Locale.getDefault(), Constants.SINGLE_INT, minutes) + Constants.ATRACKIT_SPACE + Constants.MINS_TAIL;
                    if (minutes == 1)
                        sTemp = sTemp.substring(0, sTemp.length() - 1);
                } else
                    sTemp += String.format(Locale.getDefault(), Constants.SINGLE_INT, minutes) + Constants.SHOT_XY_DELIM + String.format(Locale.getDefault(), Constants.SINGLE_INT, seconds);
            } else {
                if (seconds > 0)
                    sTemp += String.format(Locale.getDefault(), Constants.SINGLE_INT, seconds) + Constants.ATRACKIT_SPACE + Constants.SECS_TAIL;
            }
            if (btnRest != null) btnRest.setText(sTemp);
        }
    }
    private int getNumberDrawableIdentifier(Context context, int iVal){
        Resources res = context.getResources();
        String sIdent = "ic_number_"; String sTail = "_circle_white";
        switch (iVal){
            case 1:
                sIdent += "one" + sTail;
                break;
            case 2:
                sIdent += "two" + sTail;
                break;
            case 3:
                sIdent += "three" + sTail;
                break;
            case 4:
                sIdent += "four" + sTail;
                break;
            case 5:
                sIdent += "five" + sTail;
                break;
            case 6:
                sIdent += "six" + sTail;
                break;
            case 7:
                sIdent += "seven" + sTail;
                break;
            case 8:
                sIdent += "eight" + sTail;
                break;
            case 9:
                sIdent += "nine" + sTail;
                break;
            case 10:
                sIdent += "ten" + sTail;
                break;
            case 11:
                sIdent += "eleven_circle";
                break;
            case 12:
                sIdent += "twelve_circle";
                break;
            case 13:
                sIdent = "ic_thirteen_circle";
                break;
            case 14:
                sIdent += "fourteen_circle";
                break;
            case 15:
                sIdent += "fifteen_circle";
                break;
            case 16:
                sIdent += "sixteen_circle";
                break;
            case 17:
                sIdent += "seventeen_circle";
                break;
            case 18:
                sIdent += "eighteen_circle";
                break;
            case 19:
                sIdent += "nineteen_circle";
                break;
            case 20:
                sIdent += "twenty_circle";
                break;
            default:
                sIdent = "ic_question_mark_button";
                break;
        }
        int result = res.getIdentifier(sIdent,Constants.ATRACKIT_DRAWABLE, Constants.ATRACKIT_ATRACKIT_CLASS);
        return result;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTimerViewModel = new ViewModelProvider(requireActivity()).get(LiveDataTimerViewModel.class);
        mSavedStateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        //mMessagesViewModel = new ViewModelProvider(requireActivity()).get(MessagesViewModel.class);
        WorkoutViewModelFactory factory = com.a_track_it.fitdata.common.InjectorUtils.getWorkoutViewModelFactory(getContext());
        mSessionViewModel = new ViewModelProvider(requireActivity(), factory).get(WorkoutViewModel.class);
        String sUserId = ApplicationPreferences.getPreferences(getContext()).getLastUserID();
        userPrefs = UserPreferences.getPreferences(getContext(), sUserId);
        long timeMs = System.currentTimeMillis();
        WorkoutSet workSet = new WorkoutSet();
        if (getArguments().containsKey(ARG_WORKOUT_OBJECT)){
            mWorkout = getArguments().getParcelable(ARG_WORKOUT_OBJECT);
            workSet = getArguments().getParcelable(ARG_WORKOUT_SET_OBJECT);
            mWorkoutMeta = getArguments().getParcelable(ARG_WORKOUT_META_OBJECT);
            if (getArguments().containsKey(ARG_WORKOUT_SET_LIST)){
                ArrayList<WorkoutSet> sets = getArguments().getParcelableArrayList(ARG_WORKOUT_SET_LIST);
                if  ((sets != null) && (sets.size() > 0)) {
                    mSavedStateViewModel.setToDoSets(sets);
                }
            }
        }else{
            if (savedInstanceState !=null){
                mWorkout = savedInstanceState.getParcelable(Workout.class.getSimpleName());
                workSet = savedInstanceState.getParcelable(WorkoutSet.class.getSimpleName());
                mWorkoutMeta = savedInstanceState.getParcelable(WorkoutMeta.class.getSimpleName());
            }else{
                if (mSavedStateViewModel.getActiveWorkout().getValue() != null) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
                if (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) workSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
            }

        }
        boolean bGym = Utilities.isGymWorkout(mWorkout.activityID);
        boolean bTemplate = ((mWorkout.parentID != null) && (mWorkout.parentID < 0));
        if (sUserId.equals(mWorkout.userID)) {
            if (workSet == null){
                mEditorSet = new WorkoutSet(); mEditorSet.workoutID = mWorkout._id;
                mEditorSet._id = timeMs; mEditorSet.repCount = userPrefs.getDefaultNewReps();
                mEditorSet.scoreTotal = Constants.FLAG_BUILDING; // build mode
                mEditorSet.exerciseID = null; mEditorSet.exerciseName = ATRACKIT_EMPTY; mEditorSet.weightTotal=0F;
                mEditorSet.start = 0; mEditorSet.end = 0; mEditorSet.realElapsedEnd = null; mEditorSet.realElapsedStart = null;
                mEditorSet.pause_duration = 0;  mEditorSet.startBPM = null; mEditorSet.endBPM = null;
                mEditorSet.last_sync = 0; mEditorSet.device_sync = null; mEditorSet.meta_sync =null;
                mEditorSet.duration = 0; mEditorSet.weightTotal = 0f; mEditorSet.per_end_xy = ATRACKIT_EMPTY;
                mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);
            }else {
                if (workSet.scoreTotal == Constants.FLAG_PENDING){
                    mEditorSet =  new WorkoutSet(workSet); mEditorSet.workoutID = mWorkout._id;
                    mEditorSet._id = timeMs; mEditorSet.repCount = userPrefs.getDefaultNewReps();
                    mEditorSet.scoreTotal = Constants.FLAG_BUILDING; // build mode
                    mEditorSet.exerciseID = null; mEditorSet.exerciseName = ATRACKIT_EMPTY; mEditorSet.weightTotal=0F;
                    mEditorSet.start = 0; mEditorSet.end = 0; mEditorSet.realElapsedEnd = null; mEditorSet.realElapsedStart = null;
                    mEditorSet.pause_duration = 0;  mEditorSet.startBPM = null; mEditorSet.endBPM = null;
                    mEditorSet.last_sync = 0; mEditorSet.device_sync = null; mEditorSet.meta_sync =null;
                    mEditorSet.duration = 0; mEditorSet.weightTotal = 0f; mEditorSet.per_end_xy = ATRACKIT_EMPTY;
                    mBackupSet = workSet;
                }else{
                    mEditorSet = workSet;
                    mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);
                }
            }

            mSavedStateViewModel.setActiveWorkout(mWorkout);
            if (workSet.scoreTotal == Constants.FLAG_PENDING)
                mSavedStateViewModel.setActiveWorkoutSet(mBackupSet);
            else
                mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);

            mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);

            ReferencesTools mReferenceTools = ReferencesTools.getInstance();
            mReferenceTools.init(getContext());
            mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
            mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
            int activityIcon =  mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
            mSavedStateViewModel.setIconID(activityIcon);
            int activityColor = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
            mSavedStateViewModel.setColorID(activityColor);

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_routine, container, false);
        if (mSavedStateViewModel == null)  {
            mSavedStateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        }
        mScrollView = rootView.findViewById(R.id.scrollViewEntry);
        mConstraintLayout = rootView.findViewById(R.id.entry_constraint);
        textViewActivityName = rootView.findViewById(R.id.textViewActivityName);
        textViewBottom = rootView.findViewById(R.id.entry_bottomTextView);
        btnBodyRegion = rootView.findViewById(R.id.btnRegion);
        btnBodyRegion.setTag(Constants.UID_btnRegion);
        btnBodypart = rootView.findViewById(R.id.btnBodypart);
        btnBodypart.setTag(Constants.UID_btnBodypart);
        btnExercise = rootView.findViewById(R.id.btnExercise);
        btnExercise.setTag(Constants.UID_btnExercise);
        btnAddExercise = rootView.findViewById(R.id.btnAddExercise);
        btnAddExercise.setTag(Constants.UID_btnAddExercise);
        btnSets = rootView.findViewById(R.id.btnSets);
        btnSets.setTag(Constants.UID_btnSets);
        btnReps = rootView.findViewById(R.id.btnReps);
        btnReps.setTag(Constants.UID_btnReps);
        btnWeight = rootView.findViewById(R.id.btnWeight);
        btnWeight.setTag(Constants.UID_btnWeight);
        btnBuild = mConstraintLayout.findViewById(R.id.btnBuild);
        btnBuild.setTag(Constants.UID_btnBuild);
        btnStart = mConstraintLayout.findViewById(R.id.btnStart);
        btnStart.setTag(Constants.UID_btnStart);
        //textViewOfflineLabel = (TextView)mConstraintLayout.findViewById(R.id.offline_label);
        switchOffline = mConstraintLayout.findViewById(R.id.switchOffline);
        btnHistorySave = mConstraintLayout.findViewById(R.id.btnEntrySave);
        btnHistorySave.setTag(Constants.UID_btnSaveHistory);
        btnHistorySave.setOnClickListener(myClicker);
        btnRest = rootView.findViewById(R.id.btnEntryRest);
        btnRest.setTag(Constants.UID_btnRest);
        btnRoutineName = rootView.findViewById(R.id.btnRoutineName);
        btnRoutineName.setTag(Constants.UID_btnRoutineName);
        btnFinish = rootView.findViewById(R.id.btnEntryFinish);
        btnFinish.setTag(Constants.UID_btnFinish);
        recyclerViewItems = rootView.findViewById(R.id.recycleViewItems);
        btnEnableBuild = rootView.findViewById(R.id.btnEnableBuild);
        btnCurrentSet = rootView.findViewById(R.id.btnCurrentSet);
        entry_use_track_sets = mConstraintLayout.findViewById(R.id.entry_track_sets);
        entry_use_timed_rest_toggle = mConstraintLayout.findViewById(R.id.entry_timed_rest_toggle);
        entry_auto_start_toggle = mConstraintLayout.findViewById(R.id.entry_auto_start_toggle);

        Drawable drawableChecked = AppCompatResources.getDrawable(mConstraintLayout.getContext(),R.drawable.ic_outline_check_white);
        Utilities.setColorFilter(drawableChecked,ContextCompat.getColor(mConstraintLayout.getContext(), R.color.colorSplash));
        Drawable drawableUnChecked = AppCompatResources.getDrawable(mConstraintLayout.getContext(),R.drawable.ic_close_white);
        Utilities.setColorFilter(drawableUnChecked,ContextCompat.getColor(mConstraintLayout.getContext(), R.color.primaryLightColor));
        Context context = getActivity();
        ArrayList<Workout> workouts = new ArrayList<>();
        ArrayList<WorkoutSet> sets = new ArrayList<>();
        ApplicationPreferences appPrefs = ApplicationPreferences.getPreferences(context);
        String sUserId = appPrefs.getLastUserID();
        final int iconActivityId = ((mSavedStateViewModel.getIconID() != null) && (mSavedStateViewModel.getIconID() > 0)) ? mSavedStateViewModel.getIconID() : R.drawable.ic_strong_man_silhouette;
        int iColor = ((mSavedStateViewModel.getColorID() != null) && (mSavedStateViewModel.getColorID() > 0)) ? mSavedStateViewModel.getColorID() : R.color.primaryTextColor;

        bUseKG = userPrefs.getUseKG();
        if (mSavedStateViewModel.getToDoSetsSize() > 0){
            if ( mSavedStateViewModel.getToDoSets().getValue() != null) sets.addAll( mSavedStateViewModel.getToDoSets().getValue());
        }
        if (mWorkout == null) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
        if (mWorkout == null){
            mWorkout = new Workout();
            mWorkout._id = 1;
            mWorkout.userID = sUserId;
            mWorkout.activityID = WORKOUT_TYPE_STRENGTH;
            mWorkout.activityName = "Strength Training";
            formatType = TYPE_GYM;
        }
        if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_SET_TRACKING)){
            entry_use_track_sets.setIcon(drawableChecked);
            entry_use_track_sets.setChecked(true);
        }else {
            entry_use_track_sets.setIcon(drawableUnChecked);
            entry_use_track_sets.setChecked(false);
        }
        if (mEditorSet == null) mEditorSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();

        if (Utilities.isGymWorkout(mWorkout.activityID)){
            formatType = TYPE_GYM;
            btnHistorySave.setVisibility((sets.size() > 1) ? View.VISIBLE : View.GONE);
            int iWeightRest = userPrefs.getWeightsRestDuration();
            setDurationRest(iWeightRest);
        }else
            if (Utilities.isShooting(mWorkout.activityID)){
                formatType = TYPE_SHOOT;
                btnHistorySave.setVisibility(View.GONE);
            }else{
                btnHistorySave.setVisibility(View.VISIBLE);
                formatType = TYPE_GENERAL;
            }
        String slabel = getString(R.string.label_goal_steps);
        AssetManager asm = context.getAssets();
        if (asm != null) {
            Typeface typeface = Typeface.createFromAsset(
                    asm, Constants.ATRACKIT_FONT);
            if (typeface != null) {
                textViewActivityName.setTypeface(typeface);
                textViewBottom.setTypeface(typeface);
            }

        }

      //  Drawable vectorDrawable = AppCompatResources.getDrawable(mConstraintLayout.getContext(), iconActivityId);
     //   int colorActivity = ContextCompat.getColor(mConstraintLayout.getContext(),iColor);

     //   textViewActivityName.setCompoundDrawables(null,null,null,vectorDrawable);
        final String sReps = context.getString(R.string.label_rep);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewItems.setLayoutManager(llm);
        recyclerViewItems.setHasFixedSize(true);
        workoutAdapter = new WorkoutAdapter(context, workouts, sets, bUseKG);
        workoutAdapter.setListType(false);
        if ((mWorkout.start > 0) && (mWorkout.end > 0))
            if (mWorkout.parentID < 0)
                workoutAdapter.setEditMode(true);
            else
                workoutAdapter.setEditMode(false);
        recyclerViewItems.setAdapter(workoutAdapter);
        btnBodyRegion.setOnClickListener(myClicker);
        btnBodyRegion.setOnLongClickListener(myLongClicker);
        btnExercise.setOnClickListener(myClicker);
        btnAddExercise.setOnClickListener(myClicker);
        btnAddExercise.setOnLongClickListener(myLongClicker);
        btnExercise.setOnLongClickListener(myLongClicker);
        btnSets.setOnClickListener(myClicker);
        btnSets.setOnLongClickListener(myLongClicker);
        btnReps.setOnClickListener(myClicker);
        btnReps.setOnLongClickListener(myLongClicker);
        btnWeight.setOnClickListener(myClicker);
        btnWeight.setOnLongClickListener(myLongClicker);
        if (!bUseKG){
            Drawable lbsWeight = AppCompatResources.getDrawable(context, R.drawable.ic_pounds_weight);
            Utilities.setColorFilter(lbsWeight,ContextCompat.getColor(context, R.color.primaryTextColor));
            btnWeight.setCompoundDrawables(lbsWeight,null,null,null);
        }
        btnBodypart.setOnClickListener(myClicker);
        btnBodypart.setOnLongClickListener(myLongClicker);
        btnBuild.setOnClickListener(myClicker);
        btnRoutineName.setOnClickListener(myClicker);
        btnRest.setOnClickListener(myClicker);
        btnRest.setOnLongClickListener(myLongClicker);
        btnStart.setOnClickListener(myClicker);
        btnFinish.setOnClickListener(myClicker);
/*        switchOffline.setChecked(bOffline);
        if (bOffline) {
            mWorkout.offline_recording = 1;
            switchOffline.setText(getString(R.string.action_offline));
        }else{*/
            switchOffline.setChecked(mWorkout.offline_recording == 1);
            if (switchOffline.isChecked()) switchOffline.setText(getString(R.string.action_offline)); else switchOffline.setText(R.string.action_online);
   //     }
        switchOffline.setOnClickListener(v -> {
            if (mWorkout == null) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
            if (mWorkout != null)
                if (((com.google.android.material.switchmaterial.SwitchMaterial)v).isChecked()){
                    mWorkout.offline_recording = 1;
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    ((com.google.android.material.switchmaterial.SwitchMaterial)v).setText(getString(R.string.action_offline));
                }else {
                    mWorkout.offline_recording = 0;
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                    ((com.google.android.material.switchmaterial.SwitchMaterial)v).setText(getString(R.string.action_online));
                }
        });
        if (mWorkout.start > 0) {
            btnCurrentSet.setVisibility(MaterialButton.VISIBLE);
            btnCurrentSet.setChecked(true);
            btnBuild.setVisibility(View.GONE);
            btnSets.setVisibility(View.GONE);
            btnFinish.setVisibility(View.VISIBLE);
        }
        else {
            btnCurrentSet.setChecked(false);
            if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_SET_TRACKING)) {
                btnBuild.setVisibility(View.VISIBLE);
                btnSets.setVisibility(View.VISIBLE);
            }else{
                btnBuild.setVisibility(View.GONE);
                btnSets.setVisibility(View.GONE);
            }
            btnFinish.setVisibility(View.GONE);
            btnCurrentSet.setVisibility(MaterialButton.GONE);
        }
        btnCurrentSet.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton) v).isChecked();
            if (!isChecked) {
                if (mBackupSet == null){
                    return;
                }else {
                    mSavedStateViewModel.setActiveWorkoutSet(mBackupSet);
                    btnEnableBuild.setChecked(false);
                }
            }else {
                workoutAdapter.clearSelected();
                mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);
                btnEnableBuild.setChecked(true);
            }
        });
        btnEnableBuild.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton) v).isChecked();
            if (isChecked) {
                //  mEditorSet.scoreTotal = Constants.FLAG_BUILDING;
                workoutAdapter.clearSelected();
                btnCurrentSet.setChecked(false);
                if (mEditorSet != null) mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);
            }else{
                if (mBackupSet != null) {
                    mSavedStateViewModel.setActiveWorkoutSet(mBackupSet);
                    btnCurrentSet.setChecked(true);
                }
            }
        });
        if (!userPrefs.getPrefByLabel(USER_PREF_USE_SET_TRACKING)){
            btnBuild.setVisibility(MaterialButton.GONE);
            btnRest.setVisibility(MaterialButton.GONE);
            //switchOffline.setVisibility(MaterialButton.GONE);
            btnCurrentSet.setVisibility(MaterialButton.GONE);
            btnExercise.setVisibility(MaterialButton.GONE);
            btnAddExercise.setVisibility(MaterialButton.GONE);
            btnWeight.setVisibility(MaterialButton.GONE);
            btnEnableBuild.setVisibility(MaterialButton.GONE);
            btnBuild.setVisibility(MaterialButton.GONE);
            btnSets.setVisibility(MaterialButton.GONE);
            btnReps.setVisibility(MaterialButton.GONE);

            btnFinish.setVisibility(MaterialButton.GONE);
            btnBodyRegion.setVisibility(View.GONE);
            btnBodypart.setVisibility(View.GONE);
            btnRoutineName.setVisibility(View.GONE);
            entry_use_timed_rest_toggle.setVisibility(View.GONE);
            entry_auto_start_toggle.setVisibility(View.GONE);
        }
        workoutAdapter.setOnItemClickListener((UID, viewModel, position) -> {
            if (viewModel instanceof WorkoutSet){
                WorkoutSet set = (WorkoutSet)viewModel;
                if (UID == Constants.UID_btn_recycle_item_delete){
                    mListener.OnFragmentInteraction(UID,set._id,set.exerciseName);
                    broadcastToast(getString(R.string.nav_deleted));
                }
                if (UID == Constants.UID_btn_recycle_item_copy){
                    mListener.OnFragmentInteraction(UID,set._id,set.exerciseName);
                    broadcastToast(getString(R.string.nav_copied));
                }
                if (UID == Constants.UID_btn_recycle_item_select){
                    if (workoutAdapter.getSelectedPos() == position){
                        btnEnableBuild.setChecked(true);
                        btnCurrentSet.setChecked(false);
                        btnCurrentSet.setVisibility(View.GONE);
                        btnSets.setVisibility(View.VISIBLE);
                        btnBuild.setVisibility(View.VISIBLE);
                        mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);;
                    }
                    else {
                        mBackupSet = set;
                        btnEnableBuild.setChecked(false);
                        btnCurrentSet.setVisibility(View.VISIBLE);
                        btnCurrentSet.setChecked(true);
                        btnSets.setVisibility(View.GONE);
                        btnBuild.setVisibility(View.GONE);
                        mSavedStateViewModel.setActiveWorkoutSet(set);
                    }

                }
            }
        });

        entry_use_track_sets.setOnClickListener(v -> {
            final boolean isChecked = ((MaterialButton) v).isChecked();
            userPrefs.setPrefByLabel(Constants.USER_PREF_USE_SET_TRACKING, isChecked);
            mListener.OnFragmentInteraction(Constants.UID_toggle_track_activity, (isChecked ? 1 : 0), Constants.USER_PREF_USE_SET_TRACKING);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isChecked) {
                    ((MaterialButton) v).setIcon(drawableChecked);
                    btnBodyRegion.setVisibility(View.VISIBLE);
                    btnBodypart.setVisibility(View.VISIBLE);
                    btnCurrentSet.setVisibility(View.VISIBLE);
                    btnExercise.setVisibility(View.VISIBLE);
                    btnSets.setVisibility(View.VISIBLE);
                    btnReps.setVisibility(View.VISIBLE);
                    btnAddExercise.setVisibility(View.VISIBLE);
                    btnRoutineName.setVisibility(View.VISIBLE);
                    btnEnableBuild.setVisibility(View.VISIBLE);
                    btnWeight.setVisibility(View.VISIBLE);
                    btnBuild.setVisibility(View.VISIBLE);
                    //switchOffline.setVisibility(MaterialButton.VISIBLE);
                    entry_use_timed_rest_toggle.setVisibility(View.VISIBLE);
                    if (entry_use_timed_rest_toggle.isChecked()){
                        entry_auto_start_toggle.setVisibility(View.VISIBLE);
                        btnRest.setVisibility(MaterialButton.VISIBLE);
                        btnRest.setEnabled(true);
                    }else {
                        entry_auto_start_toggle.setVisibility(View.GONE);
                        btnRest.setVisibility(MaterialButton.GONE);
                    }
                    mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);
                }
                else {
                    ((MaterialButton) v).setIcon(drawableUnChecked);
                    btnBuild.setVisibility(MaterialButton.GONE);
                    btnRest.setVisibility(MaterialButton.GONE);
                    //  switchOffline.setVisibility(MaterialButton.GONE);
                    btnCurrentSet.setVisibility(MaterialButton.GONE);
                    btnRest.setVisibility(MaterialButton.GONE);
                    btnFinish.setVisibility(MaterialButton.GONE);
                    btnBodyRegion.setVisibility(View.GONE);
                    btnBodypart.setVisibility(View.GONE);
                    btnCurrentSet.setVisibility(View.GONE);
                    btnExercise.setVisibility(View.GONE);
                    btnSets.setVisibility(View.GONE);
                    btnReps.setVisibility(View.GONE);
                    btnAddExercise.setVisibility(View.GONE);
                    btnRoutineName.setVisibility(View.GONE);
                    btnEnableBuild.setVisibility(View.GONE);
                    btnWeight.setVisibility(View.GONE);
                    btnBuild.setVisibility(View.GONE);
                    entry_use_timed_rest_toggle.setVisibility(View.GONE);
                    entry_auto_start_toggle.setVisibility(View.GONE);
                    mEditorSet.scoreTotal = Constants.FLAG_NON_TRACKING;  // flag no tracking
                    mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);
                    mWorkout.scoreTotal = Constants.FLAG_NON_TRACKING;
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
                }
            });
        });
        entry_auto_start_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton) v).isChecked();
            userPrefs.setRestAutoStart(isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
            }
        });
        entry_use_timed_rest_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton) v).isChecked();
            userPrefs.setTimedRest(isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
                entry_auto_start_toggle.setVisibility(View.VISIBLE);
                btnRest.setVisibility(MaterialButton.VISIBLE);
                btnReps.setEnabled(true);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
                entry_auto_start_toggle.setVisibility(View.GONE);
                btnRest.setVisibility(MaterialButton.GONE);
            }
        });
        mSavedStateViewModel.getActiveWorkout().observe(getViewLifecycleOwner(), this::observerWorkout);
        mSavedStateViewModel.getActiveWorkoutSet().observe(getViewLifecycleOwner(), observerSet);
        mSavedStateViewModel.newSets().observe(getViewLifecycleOwner(), observerNewSets);
        mSavedStateViewModel.newReps().observe(getViewLifecycleOwner(), observerNewReps);
        mSavedStateViewModel.getToDoSets().observe(getViewLifecycleOwner(), observerSets);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInterface) {
            mListener = (FragmentInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
