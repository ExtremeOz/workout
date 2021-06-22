package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.ambient.AmbientModeSupport;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

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
import com.a_track_it.fitdata.common.user_model.MessagesViewModel;
import com.a_track_it.fitdata.common.user_model.SavedStateViewModel;
import com.google.android.gms.fitness.data.Field;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.fitdata.common.Constants.ATRACKIT_EMPTY;
import static com.a_track_it.fitdata.common.Constants.ATRACKIT_SPACE;
import static com.a_track_it.fitdata.common.Constants.FLAG_BUILDING;
import static com.a_track_it.fitdata.common.Constants.FLAG_PENDING;
import static com.a_track_it.fitdata.common.Constants.INTENT_ACTIVE_RESUMED;
import static com.a_track_it.fitdata.common.Constants.INTENT_EXTRA_MSG;
import static com.a_track_it.fitdata.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_USER;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_VALUE;
import static com.a_track_it.fitdata.common.Constants.OBJECT_TYPE_WORKOUT_SET;
import static com.a_track_it.fitdata.common.Constants.REPS_TAIL;
import static com.a_track_it.fitdata.common.Constants.USER_PREF_USE_SET_TRACKING;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_TYPE_STRENGTH;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentInterface} interface
 * to handle interaction events.
 * Use the {@link EntryRoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EntryRoomFragment extends Fragment implements AmbientInterface {
    private static final String LOG_TAG = EntryRoomFragment.class.getSimpleName();
    private final static String LABEL_LAST_SET = "Confirm Last Set";
    private final static String LABEL_REST= "Rest ";
    private final static String LABEL_ROUTINE= "Routine";
    private final static String LABEL_GO = "Go !";
    private final static String LABEL_DIV = " / ";
    private final static String LABEL_COLON = " : ";

    private static final int TYPE_GENERAL = 0;
    private static final int TYPE_GYM = 1;
    private static final int TYPE_SHOOT = 2;
    private static final String TIME_FORMAT = "HH:mm:ss";
    private int formatType = TYPE_GENERAL;
    private ApplicationPreferences appPrefs;
    private ReferencesTools mReferenceTools;
    private UserPreferences userPrefs;
    private LiveDataTimerViewModel mTimerViewModel;
    private SavedStateViewModel mSavedStateViewModel;
    private WorkoutViewModel mSessionViewModel;
    private MessagesViewModel mMessagesViewModel;
    private FragmentInterface mListener;
    private ConstraintLayout mConstraintLayout;
    private ScrollView mScrollView;
    private com.google.android.material.switchmaterial.SwitchMaterial switchOffline;
    private MaterialButton btnCurrentSet;
    private MaterialButton btnEnableBuild;
    private TextView textViewActivityName;
    private TextView textViewBottom;
    private MaterialButton btnBodyRegion;
    private MaterialButton btnBodypart;
    //private MaterialButton btnViewCenter;
    private MaterialButton btnExercise;
    private MaterialButton btnSets;
    private MaterialButton btnReps;
    private MaterialButton btnAddExercise;
    private MaterialButton btnRoutineName;
    private MaterialButton btnWeight;
    private MaterialButton btnBuild;
    private MaterialButton btnStart;
    private MaterialButton btnHistorySave;
    private MaterialButton btnRest;
    private MaterialButton entry_use_track_sets;
    private MaterialButton entry_use_timed_rest_toggle;
    private MaterialButton entry_auto_start_toggle;
    private MaterialButton btnFinish;
    private WearableRecyclerView recyclerViewItems;
    private WorkoutAdapter workoutAdapter;
    private boolean bUseKG = false;

    private Workout mWorkout;
    private WorkoutSet mEditorSet;
    private WorkoutSet mBackupSet;

    public EntryRoomFragment() {
        // Required empty public constructor
    }
    private class CustomScrollingLayoutCallback extends WearableLinearLayoutManager.LayoutCallback {
        /** How much should we scale the icon at most. */
        private static final float MAX_ICON_PROGRESS = 0.65f;
        private float mProgressToCenter;

        @Override
        public void onLayoutFinished(View child, RecyclerView parent) {

            // Figure out % progress from top to bottom
            float centerOffset = ((float) child.getHeight() / 2.0f) / (float) parent.getHeight();
            float yRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;

            // Normalize for center
            mProgressToCenter = Math.abs(0.5f - yRelativeToCenterOffset);
            // Adjust to the maximum scale
            mProgressToCenter = Math.min(mProgressToCenter, MAX_ICON_PROGRESS);
            if (!Float.isNaN(mProgressToCenter)) {
                child.setScaleX(1 - mProgressToCenter);
                child.setScaleY(1 - mProgressToCenter);
            }
        }

    }
    /** myClicker - propagate click to RoomActivity via FragmentInterface
        - added form validation for Gym exercises
     **/
    final private View.OnClickListener myClicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer iSrc = (Integer)v.getTag();
            if (mWorkout != null) mSavedStateViewModel.setActiveWorkout(mWorkout);
            if (mEditorSet != null) mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);
            if (iSrc == Constants.UID_btnStart){
                  if (Utilities.isGymWorkout(mWorkout.activityID)){
                      WorkoutSet set = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                      String sMsg = Constants.ATRACKIT_EMPTY;
                      if ((set != null && (set.scoreTotal != Constants.FLAG_NON_TRACKING))
                              && (mSavedStateViewModel.getDirtyCount() >0)){
                          if (!set.isValid(false)) {
                              if (set.exerciseName.length() == 0) {
                                  sMsg = getString(R.string.edit_set_exercise);
                              } else {
                                  if (set.repCount == 0) {
                                      sMsg = getString(R.string.edit_set_reps);
                                  }else if ((set.resistance_type != null) && (set.resistance_type != Field.RESISTANCE_TYPE_BODY) && (set.weightTotal == 0F)) {
                                      sMsg = getString(R.string.edit_set_weight);
                                  }
                              }
                              if (sMsg.length() > 0)
                                  broadcastToast(sMsg);
                          }else
                              mListener.OnFragmentInteraction(iSrc, formatType, null);
                      }
                      else{
                          if (set.scoreTotal != FLAG_PENDING)
                              mListener.OnFragmentInteraction(iSrc, formatType, "true");
                          else
                              mListener.OnFragmentInteraction(iSrc, formatType, "false");
                      }
                  }
                  else
                      mListener.OnFragmentInteraction(iSrc, formatType, null);
            }
            else {
                mListener.OnFragmentInteraction(iSrc, formatType, null);
            }

            final int pos = v.getTop();
            if (iSrc.equals(Constants.UID_btnRegion) || iSrc.equals(Constants.UID_btnBodypart) || iSrc.equals(Constants.UID_btnExercise)){
                mScrollView.postDelayed(() -> mScrollView.scrollTo(0, pos),300);
            }else{
                if (iSrc.equals(Constants.UID_btnBuild)){
                    final int pos2 = btnExercise.getTop();
                    mScrollView.postDelayed(() -> mScrollView.scrollTo(0, pos2),300);
                }
            }
        }
    };

    final View.OnLongClickListener myLongClicker = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mEditorSet != null) mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);
            mListener.OnFragmentInteraction((Integer)v.getTag(), formatType, Constants.LABEL_LONG);
            return true;
        }
    };

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EntryRoomFragment.
     */

    public static EntryRoomFragment newInstance() {
        EntryRoomFragment fragment = new EntryRoomFragment();
        return fragment;
    }
    private void bindFragment(){
        textViewActivityName = mConstraintLayout.findViewById(R.id.textViewActivityName);
        switchOffline = mConstraintLayout.findViewById(R.id.switchOffline);
        textViewBottom = mConstraintLayout.findViewById(R.id.entry_bottomTextView);
        btnBodyRegion = mConstraintLayout.findViewById(R.id.btnRegion);
        btnBodyRegion.setTag(Constants.UID_btnRegion);
        btnBodypart = mConstraintLayout.findViewById(R.id.btnBodypart);
        btnBodypart.setTag(Constants.UID_btnBodypart);
        btnCurrentSet = mConstraintLayout.findViewById(R.id.btnCurrentSet);
        btnExercise = mConstraintLayout.findViewById(R.id.btnExercise);
        btnExercise.setTag(Constants.UID_btnExercise);
        btnSets = mConstraintLayout.findViewById(R.id.btnSets);
        btnSets.setTag(Constants.UID_btnSets);
        btnReps = mConstraintLayout.findViewById(R.id.btnReps);
        btnReps.setTag(Constants.UID_btnReps);
        btnAddExercise = mConstraintLayout.findViewById(R.id.btnAddExercise);
        btnAddExercise.setTag(Constants.UID_btnAddExercise);
        btnRoutineName = mConstraintLayout.findViewById(R.id.btnRoutineName);
        btnRoutineName.setTag(Constants.UID_btnRoutineName);
        btnEnableBuild = mConstraintLayout.findViewById(R.id.btnEnableBuild);
        btnWeight = mConstraintLayout.findViewById(R.id.btnWeight);
        btnWeight.setTag(Constants.UID_btnWeight);
        btnBuild = mConstraintLayout.findViewById(R.id.btnBuild);
        btnBuild.setTag(Constants.UID_btnBuild);
        btnStart = mConstraintLayout.findViewById(R.id.btnStart);
        btnStart.setTag(Constants.UID_btnStart);
        btnHistorySave = mConstraintLayout.findViewById(R.id.btnEntrySave);
        btnHistorySave.setTag(Constants.UID_btnSaveHistory);
        btnHistorySave.setOnClickListener(myClicker);
        btnRest = mConstraintLayout.findViewById(R.id.btnEntryRest);
        btnRest.setTag(Constants.UID_btnRest);
        entry_use_track_sets = mConstraintLayout.findViewById(R.id.entry_track_sets);
        entry_use_timed_rest_toggle = mConstraintLayout.findViewById(R.id.entry_timed_rest_toggle);
        entry_auto_start_toggle = mConstraintLayout.findViewById(R.id.entry_auto_start_toggle);
        btnFinish = mConstraintLayout.findViewById(R.id.btnEntryFinish);
        btnFinish.setTag(Constants.UID_btnFinish);
        recyclerViewItems = mConstraintLayout.findViewById(R.id.recycleViewItems);
        btnBodyRegion.setOnClickListener(myClicker);
        btnBodyRegion.setOnLongClickListener(myLongClicker);
        btnBodypart.setOnClickListener(myClicker);
        btnAddExercise.setOnClickListener(myClicker);
        btnExercise.setOnClickListener(myClicker);
        btnExercise.setOnLongClickListener(myLongClicker);
        btnSets.setOnClickListener(myClicker);
        btnSets.setOnLongClickListener(myLongClicker);
        btnReps.setOnClickListener(myClicker);
        btnReps.setOnLongClickListener(myLongClicker);
        btnWeight.setOnClickListener(myClicker);
        btnWeight.setOnLongClickListener(myLongClicker);
        btnBuild.setOnClickListener(myClicker);
        btnRest.setOnClickListener(myClicker);
        btnRest.setOnLongClickListener(myLongClicker);
        btnFinish.setOnClickListener(myClicker);
        btnStart.setOnClickListener(v -> {
                mListener.OnFragmentInteraction(Constants.UID_btnStart, formatType, Boolean.toString(btnEnableBuild.isChecked()));
        });
        btnFinish.setOnClickListener(myClicker);
        btnRoutineName.setOnClickListener(myClicker);

    }
    final private Observer<Integer> observerNewSets = new Observer<Integer>() {
        @Override
        public void onChanged(Integer iTemp) {
            String sNewSet = getString(R.string.label_new_sets) + ATRACKIT_SPACE;
            Context context = getContext();
            if (iTemp <= 20 && (context != null)) {
                int resId = getNumberDrawableIdentifier(context, iTemp);
                Drawable d = AppCompatResources.getDrawable(context, resId);
                int color = ContextCompat.getColor(context,R.color.primaryTextColor);
                Utilities.setColorFilter(d,color);
                if (d != null){
                    btnSets.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,d,null);
                    //TextViewCompat.setCompoundDrawableTintList(btnSets,getContext().getColorStateList(R.color.primaryTextColor));
                    //((MaterialButton)btnSets).setCompoundDrawableTintList(getContext().getColorStateList(R.color.primaryTextColor));
                    btnSets.setText(sNewSet);
                    return;
                }
            }
            String sTemp = sNewSet + Integer.toString(iTemp);
            btnSets.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,null,null);
            btnSets.setText(sTemp);
        }
    };
    final private Observer<Integer> observerNewReps = new Observer<Integer>() {
        @Override
        public void onChanged(Integer iTemp) {
            Context context = getContext();
            if (iTemp <= 20 && (context != null)) {
                int resId = getNumberDrawableIdentifier(context, iTemp);
                if (resId > 0){
                    Drawable d = AppCompatResources.getDrawable(context, resId);
                    ColorStateList color = AppCompatResources.getColorStateList(context,R.color.primaryTextColor);
                    btnReps.setIcon(d);
                    btnReps.setIconTint(color);
                    btnReps.setText(Constants.REPS_TAIL);
                    return;
                }
            }
            String sTemp = Integer.toString(iTemp) + Constants.REPS_TAIL;
            btnReps.setIcon(null);
            btnReps.setText(sTemp);

        }
    };
    final private Observer<List<WorkoutSet>> observerSets = new Observer<List<WorkoutSet>>() {
        @Override
        public void onChanged(List<WorkoutSet> workoutSets) {
            if (workoutSets != null) {
                final ArrayList<WorkoutSet> sets1 = new ArrayList<>(workoutSets);
                sets1.sort((o1, o2) -> ((o1.setCount < o2.setCount) ? -1: ((o1.setCount > o2.setCount) ? 1 : 0)));
                int currentSetSize = workoutSets.size();
                String sSetMsg = mWorkout.activityName;
                int currentSet = mSavedStateViewModel.getSetIndex();
                if (mWorkout.start == 0) {
                    if (mEditorSet.scoreTotal == FLAG_BUILDING){
                        if (currentSet == 1) sSetMsg = getString(R.string.label_build);
                        else
                            if (currentSet == currentSetSize)
                                sSetMsg = getString(R.string.sets_summaryOf5, currentSetSize);
                            else
                                sSetMsg = getString(R.string.sets_summaryOf4,currentSet,currentSetSize);
                    }else {
                        sSetMsg = getString(R.string.sets_summaryOf,currentSet,currentSetSize);
                    }

                }else{
                    if (mEditorSet.scoreTotal == FLAG_BUILDING){
                        if (currentSet == 1) sSetMsg = getString(R.string.label_build);
                        else
                        if (currentSet == currentSetSize)
                            sSetMsg = getString(R.string.sets_summaryOf5, currentSetSize);
                        else
                            sSetMsg = getString(R.string.sets_summaryOf4,currentSet,currentSetSize);
                    }else {
                        if (currentSet == 1) sSetMsg = getString(R.string.sets_summary1, currentSet);
                        else
                            sSetMsg = getString(R.string.sets_summaryOf,currentSet,currentSetSize);
                    }
                }
                Log.w(LOG_TAG, "observerSets " + currentSetSize + " " + sSetMsg);
                final String sMsg = sSetMsg;
                new Handler(Looper.myLooper()).post(() -> {
                    workoutAdapter.setWorkoutSetArrayList(sets1);
                    if (sets1.size() == 0)
                        recyclerViewItems.setVisibility(View.GONE);
                    else {
                        recyclerViewItems.setVisibility(View.VISIBLE);
                        textViewBottom.setText(sMsg);
                        textViewBottom.setTextSize(TypedValue.COMPLEX_UNIT_SP,Utilities.getLabelFontSize(sMsg));
                    }


                });
            }else{
                new Handler(Looper.myLooper()).post(() -> {
                    workoutAdapter.setWorkoutSetArrayList(null);
                    recyclerViewItems.setVisibility(View.GONE);
                });
            }
            workoutAdapter.notifyDataSetChanged();
        }
    };
    final private Observer<WorkoutMeta> metaObserver = new Observer<WorkoutMeta>() {
        @Override
        public void onChanged(WorkoutMeta workoutMeta) {
            if (workoutMeta != null){
                if (Utilities.isGymWorkout(workoutMeta.activityID)) {
                    if (workoutMeta.description.length() > 0)
                        btnRoutineName.setText(workoutMeta.description);
                    else
                        btnRoutineName.setText(R.string.action_name);
                }
            }
        }
    };

    private void observerWorkout(Workout w){
        if ((w != null) && (w.activityID != null) && (w.activityID > 0)) {
            Context context1 = mConstraintLayout.getContext();
            final Drawable drawableChecked = AppCompatResources.getDrawable(context1,R.drawable.ic_outline_check_white);
            Utilities.setColorFilter(drawableChecked,ContextCompat.getColor(context1, R.color.colorSplash));
            final Drawable drawableUnChecked = AppCompatResources.getDrawable(context1,R.drawable.ic_close_white);
            drawableUnChecked.setColorFilter(ContextCompat.getColor(context1, R.color.secondaryTextColor), PorterDuff.Mode.SRC_IN);
            Utilities.setColorFilter(drawableUnChecked,ContextCompat.getColor(context1, R.color.primaryLightColor));
            final String startLabel = (w.start > 0) ? context1.getString(R.string.action_continue) : context1.getString(R.string.session_start);
            Log.e(LOG_TAG, "observeWorkout " + w.toString());
            if ((w.parentID == null) || (w.parentID > 0)){
                if ((w.name != null) && (w.name.length() > 0)){
                    textViewActivityName.setText(w.name);
                    textViewActivityName.setTextSize(TypedValue.COMPLEX_UNIT_SP, Utilities.getLabelFontSize(w.name));
                    if (w.parentID != null && w.parentID > 0) {
                        Drawable favList = AppCompatResources.getDrawable(mConstraintLayout.getContext(), R.drawable.ic_favlist);
                        textViewActivityName.setCompoundDrawablesRelativeWithIntrinsicBounds(favList,null,null,null);
                    }else{
                        textViewActivityName.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,null,null);
                    }
                }else
                    textViewActivityName.setText(w.activityName);
            }else{
                if ((w.name != null) && (w.parentID < 0)){
                    String sTemp = getString(R.string.label_template).substring(0,2) + Constants.ATRACKIT_SPACE + w.name;
                    textViewActivityName.setText(sTemp);
                    Drawable favList = AppCompatResources.getDrawable(mConstraintLayout.getContext(), R.drawable.ic_heart_outline);
                    textViewActivityName.setCompoundDrawablesRelativeWithIntrinsicBounds(favList, null, null,null);
                }else {
                    Drawable d = AppCompatResources.getDrawable(mConstraintLayout.getContext(), R.drawable.ic_heart_add);
                    textViewActivityName.setText(w.activityName);
                    textViewActivityName.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null,null);
                }
            }
            switchOffline.setChecked(w.offline_recording == 1);
            if  (!Utilities.isGymWorkout(w.activityID)){
                if ((w.name != null) && (w.name.length() > 0) & (!Utilities.isGymWorkout(w.activityID)))
                    textViewBottom.setText(w.name);
                else
                    textViewBottom.setText(w.activityName);
                textViewBottom.setTextColor(ContextCompat.getColor(context1, R.color.colorAccent));
                btnBodyRegion.setText(startLabel);
                Drawable iconR = AppCompatResources.getDrawable(context1,R.drawable.ic_play_button_white);
                btnBodyRegion.setIcon(iconR);
                if (w.start > 0) btnStart.setText(LABEL_GO);
                String slabel1 = context1.getString(R.string.label_goal_steps);
                if (w.goal_steps > 0)
                    slabel1 = slabel1.replace("(steps)", "(" + w.goal_steps + " steps)");
                btnBodypart.setText(slabel1);
                Drawable icon = AppCompatResources.getDrawable(context1,R.drawable.ic_footsteps_silhouette_variant);
                btnBodypart.setIcon(icon);
                btnBodypart.setIconTint(context1.getColorStateList(R.color.primaryTextColor));
                slabel1 = context1.getString(R.string.label_goal_duration);
                if (w.goal_duration > 0)
                    slabel1 = slabel1.replace("Goal (min)", "Goal (" + w.goal_duration + " min)");
                Drawable icon1 = AppCompatResources.getDrawable(context1,R.drawable.ic_stopwatch_white);
                btnRoutineName.setIcon(icon1);
                btnRoutineName.setIconTint(context1.getColorStateList(R.color.primaryTextColor));
                btnRoutineName.setText(slabel1);
                btnRoutineName.setVisibility(MaterialButton.VISIBLE);
            }
            else{
                // template
                if ((w.parentID != null) && w.parentID < 0){
                    btnRoutineName.setVisibility(View.GONE);
                    btnFinish.setVisibility(View.GONE);
                    btnHistorySave.setVisibility(View.VISIBLE);
                }else {
                    btnStart.setText(startLabel);
                    if (((w.start == 0) && (w.end == 0)) || ((w.start > 0) && (w.end > 0))) {
                        if ((w.name != null) && (w.name.length() > 0)) {
                            textViewBottom.setText(w.name);
                            btnRoutineName.setText(w.name);
                            btnRoutineName.setTextSize(TypedValue.COMPLEX_UNIT_SP, Utilities.getLabelFontSize(w.name));
                            btnRoutineName.setVisibility(View.VISIBLE);
                        } else {
                            btnRoutineName.setVisibility(View.GONE);
                        }
                        btnHistorySave.setVisibility(View.VISIBLE);
                    }else
                        btnHistorySave.setVisibility(View.GONE);
                    btnFinish.setVisibility((w.start > 0) ? View.VISIBLE: View.GONE);
                }
                entry_use_track_sets.setChecked(w.scoreTotal != Constants.FLAG_NON_TRACKING);
                entry_use_timed_rest_toggle.setChecked(userPrefs.getTimedRest());
                if (userPrefs.getRestAutoStart()){
                    entry_auto_start_toggle.setIcon(drawableChecked);
                }else {
                    entry_auto_start_toggle.setIcon(drawableUnChecked);
                }
                if (userPrefs.getTimedRest() && w.scoreTotal != Constants.FLAG_NON_TRACKING){
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
                if (w.scoreTotal != Constants.FLAG_NON_TRACKING) {
                    btnBuild.setVisibility(View.VISIBLE);
                    btnSets.setVisibility(View.VISIBLE);
                }else{
                    btnBuild.setVisibility(View.GONE);
                    btnSets.setVisibility(View.GONE);
                    btnRest.setVisibility(View.GONE);
                    entry_use_timed_rest_toggle.setVisibility(View.GONE);
                    entry_auto_start_toggle.setVisibility(View.GONE);
                }
            }
        }
    }

    private void observerSet(WorkoutSet workoutSet){
        if (workoutSet == null) return;
        if ((workoutSet.activityID == null) || (workoutSet.activityID == 0)) return;
        final Context context = getContext();
        if (context == null) return;
        final String sBodypart = context.getString(R.string.label_bodypart);
        final String sExercise = context.getString(R.string.label_exercise);
        final String sWeight = context.getString(R.string.label_weight);
        final String sRest = context.getString(R.string.action_untimed_rest);
        String sTemp;
        Log.e(LOG_TAG, "observeSet " + workoutSet.toString());
        Drawable kgDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_weight);
        int imageColor = ContextCompat.getColor(context, R.color.primaryTextColor);
        if (kgDrawable != null)
            Utilities.setColorFilter(kgDrawable,imageColor);
        Drawable lbsDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_pounds_weight);
        if (lbsDrawable != null)
            Utilities.setColorFilter(lbsDrawable,imageColor);
        final Drawable kgWeight = kgDrawable;
        final Drawable lbsWeight = lbsDrawable;

        if (Utilities.isGymWorkout(workoutSet.activityID)) {
            boolean isValid = workoutSet.isValid(false);
            if ((workoutSet.regionID != null) && (workoutSet.regionID > 0))
                btnBodyRegion.setText(workoutSet.regionName);
            else
                btnBodyRegion.setText(getString(R.string.label_bodyregion));

            if ((workoutSet.bodypartID != null) && (workoutSet.bodypartID > 0))
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
                float tempWeight = ((bUseKG) ? workoutSet.weightTotal : Utilities.KgToPoundsDisplay(workoutSet.weightTotal));
                double intWeight =  Math.floor(tempWeight);
                if (tempWeight % intWeight != 0)
                    sTemp = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                else
                    sTemp = new DecimalFormat("#").format(intWeight);
                btnWeight.setText(sTemp);
            }else {
                if ((workoutSet != null) && (workoutSet.resistance_type != null) && (workoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY))
                    btnWeight.requestFocus();
                btnWeight.setText(sWeight);
            }
            if (bUseKG) {
                btnWeight.setCompoundDrawablesRelativeWithIntrinsicBounds(kgWeight, null,null,null);
            }else {
                btnWeight.setCompoundDrawablesRelativeWithIntrinsicBounds(lbsWeight, null,null,null);
            }
            btnWeight.setCompoundDrawablePadding(6);
            if (workoutSet.rest_duration == null) workoutSet.rest_duration = 0L;
            if (workoutSet.rest_duration > 0){
                int iWeightRest = userPrefs.getWeightsRestDuration();
                setDurationRest(iWeightRest);
            }else
                btnRest.setText(sRest);
            if (entry_use_track_sets.isChecked())
                if (workoutSet.scoreTotal == Constants.FLAG_PENDING){
                    Log.e(LOG_TAG, "observeSet pending " + workoutSet.scoreTotal);
                    btnCurrentSet.setChecked(true);
                    btnCurrentSet.setVisibility(View.VISIBLE);
                    btnEnableBuild.setChecked(false);
                    btnSets.setVisibility(MaterialButton.GONE);
                    btnBuild.setVisibility(MaterialButton.GONE);
                    //textViewBottom.setText(sSetMsg);
                }else {
                    Log.e(LOG_TAG, "observeSet not pending " + workoutSet.scoreTotal);
                    btnCurrentSet.setChecked(false);
                    btnCurrentSet.setVisibility(View.GONE);
                    btnEnableBuild.setChecked(true);
                    btnSets.setVisibility(MaterialButton.VISIBLE);
                    btnBuild.setVisibility(MaterialButton.VISIBLE);
                    btnBuild.setEnabled(isValid);
                    btnBuild.setChecked(isValid);
                }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTimerViewModel = new ViewModelProvider(requireActivity()).get(LiveDataTimerViewModel.class);
        mSavedStateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        WorkoutViewModelFactory factory = com.a_track_it.fitdata.common.InjectorUtils.getWorkoutViewModelFactory(getContext());
        mSessionViewModel = new ViewModelProvider(requireActivity(), factory).get(WorkoutViewModel.class);
        mMessagesViewModel = new ViewModelProvider(requireActivity()).get(MessagesViewModel.class);
        appPrefs = ApplicationPreferences.getPreferences(requireActivity());
        String sUserId = appPrefs.getLastUserID();
        userPrefs = UserPreferences.getPreferences(requireActivity(), sUserId);
        WorkoutSet workSet = new WorkoutSet();
        long timeMs = System.currentTimeMillis();
        if (getArguments() != null) {
            mWorkout = getArguments().getParcelable(Workout.class.getSimpleName());
            workSet = getArguments().getParcelable(WorkoutSet.class.getSimpleName());
        }else
            if (savedInstanceState !=null){
                mWorkout = savedInstanceState.getParcelable(Workout.class.getSimpleName());
                workSet = savedInstanceState.getParcelable(WorkoutSet.class.getSimpleName());
            }else{
                if (mSavedStateViewModel.getActiveWorkout().getValue() != null) mWorkout = mSavedStateViewModel.getActiveWorkout().getValue();
                if (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) workSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            }
        if (mWorkout == null){
            mWorkout = new Workout();
            mWorkout._id = 1;
            mWorkout.userID = sUserId;
            mWorkout.activityID = WORKOUT_TYPE_STRENGTH;
            mWorkout.activityName = "Strength Training";
            formatType = TYPE_GYM;
        }
        boolean bGym = Utilities.isGymWorkout(mWorkout.activityID);
        boolean bTemplate = ((mWorkout.parentID != null) && (mWorkout.parentID < 0));
        mSavedStateViewModel.setActiveWorkout(mWorkout);
        if (Utilities.isGymWorkout(mWorkout.activityID)){
            formatType = TYPE_GYM;
        }else
            if (Utilities.isShooting(mWorkout.activityID)){
                formatType = TYPE_SHOOT;
            }else{
                formatType = TYPE_GENERAL;
            }
        if (sUserId.equals(mWorkout.userID)){
            if (workSet == null){
                mEditorSet = new WorkoutSet(mWorkout);
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
                    mBackupSet = workSet;
                    mEditorSet =  new WorkoutSet(workSet);
                    mEditorSet._id = timeMs; mEditorSet.repCount = userPrefs.getDefaultNewReps();
                    mEditorSet.scoreTotal = Constants.FLAG_BUILDING; // build mode
                    mEditorSet.exerciseID = null; mEditorSet.exerciseName = ATRACKIT_EMPTY; mEditorSet.weightTotal=0F;
                    mEditorSet.start = 0; mEditorSet.end = 0; mEditorSet.realElapsedEnd = null; mEditorSet.realElapsedStart = null;
                    mEditorSet.pause_duration = 0;  mEditorSet.startBPM = null; mEditorSet.endBPM = null;
                    mEditorSet.last_sync = 0; mEditorSet.device_sync = null; mEditorSet.meta_sync =null;
                    mEditorSet.duration = 0; mEditorSet.weightTotal = 0f; mEditorSet.per_end_xy = ATRACKIT_EMPTY;
                    mSavedStateViewModel.setActiveWorkoutSet(workSet);
                }else{
                    mEditorSet = workSet;
                    mBackupSet = workSet;
                    mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);
                }
            }
            mReferenceTools = ReferencesTools.getInstance();
            mReferenceTools.init(getContext());
            mSavedStateViewModel.setSetIsGym(Utilities.isGymWorkout(mWorkout.activityID));
            mSavedStateViewModel.setSetIsShoot(Utilities.isShooting(mWorkout.activityID));
            int activityIcon =  mReferenceTools.getFitnessActivityIconResById(mWorkout.activityID);
            mSavedStateViewModel.setIconID(activityIcon);
            int activityColor = mReferenceTools.getFitnessActivityColorById(mWorkout.activityID);
            mSavedStateViewModel.setColorID(activityColor);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_entry, container, false);
        mScrollView = rootView.findViewById(R.id.scrollViewEntry);
        mConstraintLayout = rootView.findViewById(R.id.entry_constraint);
        bindFragment();
        Drawable drawableChecked = AppCompatResources.getDrawable(mConstraintLayout.getContext(),R.drawable.ic_outline_check_white);
        Utilities.setColorFilter(drawableChecked,ContextCompat.getColor(mConstraintLayout.getContext(), R.color.colorSplash));
        Drawable drawableUnChecked = AppCompatResources.getDrawable(mConstraintLayout.getContext(),R.drawable.ic_close_white);
        Utilities.setColorFilter(drawableUnChecked,ContextCompat.getColor(mConstraintLayout.getContext(), R.color.secondaryTextColor));
        Context context = getActivity();
        ArrayList<Workout> workouts = new ArrayList<>();
        List<WorkoutSet> sets = new ArrayList<>();
        //boolean bOffline = !mReferenceTools.isNetworkConnected();
        int iconActivityId = (mSavedStateViewModel.getIconID() != null) ? mSavedStateViewModel.getIconID() : R.drawable.ic_strong_man_silhouette;
        int iColor = (mSavedStateViewModel.getColorID() != null) ? mSavedStateViewModel.getColorID() : R.color.primaryTextColor;
        String sUserId =  (mWorkout == null) ? appPrefs.getLastUserID() : mWorkout.userID;
        userPrefs = UserPreferences.getPreferences(context, sUserId);
        bUseKG = userPrefs.getUseKG();
        if (mSavedStateViewModel.getToDoSetsSize() > 0){
            if ( mSavedStateViewModel.getToDoSets().getValue() != null){
                sets.addAll( mSavedStateViewModel.getToDoSets().getValue());
            }
        }
        if (Utilities.isGymWorkout(mWorkout.activityID)){
            formatType = TYPE_GYM;
            btnHistorySave.setVisibility((mWorkout.setCount > 1 && (mWorkout.start == 0 && mWorkout.end == 0)) ? View.VISIBLE : View.GONE);
        }else
        if (Utilities.isShooting(mWorkout.activityID)){
            formatType = TYPE_SHOOT;
            btnHistorySave.setVisibility(View.GONE);
        }else{
            btnHistorySave.setVisibility(View.VISIBLE);
            formatType = TYPE_GENERAL;
        }

        final String sReps = context.getString(R.string.label_rep);
        String slabel = getString(R.string.label_goal_steps);
        if ((formatType != TYPE_GYM) && (mWorkout.activityName.length() > 0))
            textViewActivityName.setText(mWorkout.activityName);

        AssetManager asm = context.getAssets();
        if (asm != null) {
            Typeface typeface = Typeface.createFromAsset(
                    asm, Constants.ATRACKIT_FONT);
            if (typeface != null) {
                textViewActivityName.setTypeface(typeface);
                textViewBottom.setTypeface(typeface);
            }
          //  textViewActivityName.setTextSize(TypedValue.COMPLEX_UNIT_SP,Utilities.getLabelFontSize(mWorkout.activityName));
        }

        Drawable vectorDrawable = AppCompatResources.getDrawable(mConstraintLayout.getContext(), iconActivityId);
        //final int idColor = res.getColor(iColor, null);
       textViewActivityName.setCompoundDrawables(null,null,null,vectorDrawable);
       TextViewCompat.setCompoundDrawableTintList(textViewActivityName,AppCompatResources.getColorStateList(getContext(),iColor));
        switchOffline.setChecked(mWorkout.offline_recording == 1);
        if (switchOffline.isChecked()) switchOffline.setText(getString(R.string.action_offline)); else switchOffline.setText(R.string.action_online);
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
        if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_SET_TRACKING)){
            entry_use_track_sets.setIcon(drawableChecked);
            entry_use_track_sets.setChecked(true);
        }else {
            entry_use_track_sets.setIcon(drawableUnChecked);
            entry_use_track_sets.setChecked(false);
        }
        if (formatType != TYPE_GYM) {
            btnBodypart.setOnLongClickListener(view -> {
                mListener.OnFragmentInteraction((int)view.getTag(), formatType, Constants.LABEL_LONG);
                return true;
            });
            btnRoutineName.setOnLongClickListener(view -> {
                mListener.OnFragmentInteraction((int)view.getTag(), formatType, Constants.LABEL_LONG);
                return true;
            });
            if (formatType != TYPE_SHOOT) {
                btnBuild.setVisibility(MaterialButton.GONE);
                btnRest.setVisibility(MaterialButton.GONE);
                btnCurrentSet.setVisibility(MaterialButton.GONE);
                btnExercise.setVisibility(MaterialButton.GONE);
                btnAddExercise.setVisibility(MaterialButton.GONE);
                btnWeight.setVisibility(MaterialButton.GONE);
                btnEnableBuild.setVisibility(MaterialButton.GONE);
                btnBuild.setVisibility(MaterialButton.GONE);
                btnReps.setVisibility(MaterialButton.GONE);
                btnRest.setVisibility(MaterialButton.GONE);
                btnSets.setVisibility(MaterialButton.GONE);
                btnFinish.setVisibility(MaterialButton.GONE);
                entry_auto_start_toggle.setVisibility(MaterialButton.GONE);
                entry_use_timed_rest_toggle.setVisibility(MaterialButton.GONE);
                entry_use_track_sets.setVisibility(MaterialButton.GONE);
            }
            else{
                int iWeightRest = userPrefs.getArcheryRestDuration();
                setDurationRest(iWeightRest);
                entry_use_timed_rest_toggle.setChecked(userPrefs.getTimedRest());
                entry_use_track_sets.setChecked(userPrefs.getPrefByLabel(Constants.USER_PREF_USE_SET_TRACKING));

                if (userPrefs.getTimedRest()){
                    entry_use_timed_rest_toggle.setIcon(drawableChecked);
                    entry_auto_start_toggle.setVisibility(View.VISIBLE);
                    btnRest.setVisibility(MaterialButton.VISIBLE);
                    btnRest.setChecked(true);
                }else {
                    entry_use_timed_rest_toggle.setIcon(drawableUnChecked);
                    entry_auto_start_toggle.setVisibility(View.GONE);
                    btnRest.setVisibility(MaterialButton.GONE);
                }
                if (userPrefs.getRestAutoStart()){
                    entry_auto_start_toggle.setIcon(drawableChecked);
                    entry_auto_start_toggle.setChecked(true);
                }else {
                    entry_auto_start_toggle.setIcon(drawableUnChecked);
                    entry_auto_start_toggle.setChecked(false);
                }

            }
        }
        else { // gym
            if ((mEditorSet.regionID != null) && (mEditorSet.regionID > 0))
                btnBodyRegion.setText(mEditorSet.regionName);
            else
                btnBodyRegion.setText(getString(R.string.label_bodyregion));

            int iWeightRest = userPrefs.getWeightsRestDuration();
            setDurationRest(iWeightRest);
            entry_use_track_sets.setChecked(userPrefs.getPrefByLabel(Constants.USER_PREF_USE_SET_TRACKING));
            entry_use_timed_rest_toggle.setChecked(userPrefs.getTimedRest());
            if (userPrefs.getTimedRest()){
                entry_use_timed_rest_toggle.setIcon(drawableChecked);
                entry_auto_start_toggle.setVisibility(View.VISIBLE);
                btnRest.setVisibility(MaterialButton.VISIBLE);
                btnRest.setChecked(true);
            }else {
                entry_use_timed_rest_toggle.setIcon(drawableUnChecked);
                entry_auto_start_toggle.setVisibility(View.GONE);
                btnRest.setVisibility(MaterialButton.GONE);
            }
            if (userPrefs.getRestAutoStart()){
                entry_auto_start_toggle.setIcon(drawableChecked);
                entry_auto_start_toggle.setChecked(true);
            }else {
                entry_auto_start_toggle.setIcon(drawableUnChecked);
                entry_auto_start_toggle.setChecked(false);
            }
            btnBodypart.setOnLongClickListener(myLongClicker);
            if (mWorkout.start > 0) {
                btnCurrentSet.setVisibility(MaterialButton.VISIBLE);
                btnCurrentSet.setChecked(true);
            //    btnBuild.setVisibility(View.GONE);
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
              //  btnCurrentSet.setVisibility(MaterialButton.GONE);
            }

            btnCurrentSet.setOnClickListener(v -> {
                boolean isChecked = ((MaterialButton) v).isChecked();
                if (!isChecked) {
                    if (mBackupSet == null){
                        return;
                    }else {
                        mBackupSet.scoreTotal = FLAG_PENDING;
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
            iWeightRest = userPrefs.getDefaultNewSets();
            String sNewSet = getString(R.string.label_new_sets) + ATRACKIT_SPACE;
            if (iWeightRest <= 20) {
                Drawable d = ContextCompat.getDrawable(context,getNumberDrawableIdentifier(context, iWeightRest));
                btnSets.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,d,null);
                btnSets.setIconTint(context.getColorStateList(R.color.primaryTextColor));
                btnSets.setText(sNewSet);
            }else{
                String sTemp = sNewSet + Integer.toString(iWeightRest);
                btnSets.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,null,null);
                btnSets.setText(sTemp);
            }
            iWeightRest = userPrefs.getDefaultNewReps();
            if (iWeightRest <= 20) {
                Drawable d = ContextCompat.getDrawable(context,getNumberDrawableIdentifier(context, iWeightRest));
                btnReps.setIcon(d);
                btnReps.setIconTint(context.getColorStateList(R.color.primaryTextColor));
                btnReps.setText(sReps);
            }else{
                String sTemp = Integer.toString(iWeightRest) + REPS_TAIL;
                btnReps.setIcon(null);
                btnReps.setText(sTemp);
            }
            entry_auto_start_toggle.setOnClickListener(v -> {
                boolean isChecked = ((MaterialButton) v).isChecked();
                userPrefs.setRestAutoStart(isChecked);
                if (isChecked) {
                    ((MaterialButton) v).setIcon(drawableChecked);
                } else {
                    ((MaterialButton) v).setIcon(drawableUnChecked);
                }
            });
            entry_use_track_sets.setChecked(userPrefs.getPrefByLabel(Constants.USER_PREF_USE_SET_TRACKING));
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
            if (!userPrefs.getPrefByLabel(USER_PREF_USE_SET_TRACKING)){
                btnBuild.setVisibility(MaterialButton.GONE);
                btnRest.setVisibility(MaterialButton.GONE);
                btnCurrentSet.setVisibility(MaterialButton.GONE);
                btnExercise.setVisibility(MaterialButton.GONE);
                btnAddExercise.setVisibility(MaterialButton.GONE);
                btnWeight.setVisibility(MaterialButton.GONE);
                btnEnableBuild.setVisibility(MaterialButton.GONE);
                btnBuild.setVisibility(MaterialButton.GONE);
                btnReps.setVisibility(MaterialButton.GONE);
                btnRest.setVisibility(MaterialButton.GONE);
                btnFinish.setVisibility(MaterialButton.GONE);
                entry_auto_start_toggle.setVisibility(MaterialButton.GONE);
                entry_use_timed_rest_toggle.setVisibility(MaterialButton.GONE);

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
            }
        }
        workoutAdapter = new WorkoutAdapter(context, null, null, bUseKG);
        workoutAdapter.setListType(false);
        //if (mSavedStateViewModel.getSetIndex() > 0) workoutAdapter.setSelectedSet(mSavedStateViewModel.getSetIndex());
      //  CustomScrollingLayoutCallback customScrollingLayoutCallback = new CustomScrollingLayoutCallback();
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
       // WearableLinearLayoutManager mWearableLinearLayoutManager = new WearableLinearLayoutManager(context, customScrollingLayoutCallback);
        LinearLayoutManager llm = new LinearLayoutManager(requireActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(context) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        recyclerViewItems.setLayoutManager(llm);
        recyclerViewItems.setCircularScrollingGestureEnabled(false);
        recyclerViewItems.setBezelFraction(0.5f);
        recyclerViewItems.setScrollDegreesPerScreen(90);

        // To align the edge children (first and last) with the center of the screen
        recyclerViewItems.setEdgeItemsCenteringEnabled(true);
        recyclerViewItems.setHasFixedSize(true);
        recyclerViewItems.setAdapter(workoutAdapter);
        smoothScroller.setTargetPosition(0);
        llm.startSmoothScroll(smoothScroller);
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
                    mEditorSet.scoreTotal = Constants.FLAG_BUILDING;  // flag for building
                    mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);
                    mWorkout.scoreTotal = Constants.FLAG_BUILDING;
                    mSavedStateViewModel.setActiveWorkout(mWorkout);
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
        mSavedStateViewModel.getActiveWorkout().observe(getViewLifecycleOwner(), this::observerWorkout);
        mSavedStateViewModel.getActiveWorkoutSet().observe(getViewLifecycleOwner(), this::observerSet);
        mSavedStateViewModel.getActiveWorkoutMeta().observe(getViewLifecycleOwner(), metaObserver);
        mSavedStateViewModel.newSets().observe(getViewLifecycleOwner(), observerNewSets);
        mSavedStateViewModel.newReps().observe(getViewLifecycleOwner(), observerNewReps);
        mSavedStateViewModel.getToDoSets().observe(getViewLifecycleOwner(), observerSets);
        workoutAdapter.setOnItemClickListener((View view, Object viewModel, int startMode, int position) -> {
            //final String sSetMsg2 = getString(R.string.label_add_sets) + LABEL_COLON + mWorkout.setCount;
            if (workoutAdapter.getSelectedPos() == position){
                btnEnableBuild.setChecked(true);
                btnCurrentSet.setChecked(false);
               // btnCurrentSet.setVisibility(View.GONE);
//                if (userPrefs.getPrefByLabel(Constants.USER_PREF_USE_SET_TRACKING)) {
//                    btnBuild.setVisibility(View.VISIBLE);
//                    btnSets.setVisibility(View.VISIBLE);
//                }else{
//                    btnBuild.setVisibility(View.GONE);
//                    btnSets.setVisibility(View.GONE);
//                }
                mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);
               // textViewBottom.setText(sSetMsg2);
            }
            else {
                btnEnableBuild.setChecked(false);
                btnCurrentSet.setVisibility(View.VISIBLE);
                btnCurrentSet.setChecked(true);
                btnSets.setVisibility(View.GONE);
                btnBuild.setVisibility(View.GONE);
                mBackupSet =  (WorkoutSet)viewModel;
                doActionSet(mBackupSet);
            }
        });
      //  if (mWorkout != null) mSavedStateViewModel.setActiveWorkout(mWorkout);
       // if (mEditorSet != null) mSavedStateViewModel.setActiveWorkoutSet(mEditorSet);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInterface) {
            mListener = (FragmentInterface) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mWorkout != null) outState.putParcelable(Workout.class.getSimpleName(),mWorkout);
        if (mEditorSet != null) outState.putParcelable(WorkoutSet.class.getSimpleName(), mEditorSet);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        boolean IsLowBitAmbient =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false);
        boolean DoBurnInProtection =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false);
        Context context = getContext();
        int backColor = ContextCompat.getColor(context, R.color.ambientBackground);
        int foreColor = ContextCompat.getColor(context, R.color.ambientForeground);

        ColorStateList colorStateList = ContextCompat.getColorStateList(context, R.color.ambientForeground);
        ColorStateList colorStateList2 = ContextCompat.getColorStateList(context, R.color.ambientBackground);
        mConstraintLayout.setBackgroundTintList(colorStateList2);
        for (int i = 0; i < mConstraintLayout.getChildCount(); i++) {
            View v = mConstraintLayout.getChildAt(i);
            if (v instanceof com.google.android.material.button.MaterialButton) {
                ((MaterialButton) v).setTextColor(foreColor);
                ((MaterialButton) v).setBackgroundColor(backColor);
                ((MaterialButton) v).setIconTint(colorStateList);
            }
            if (v instanceof TextView){
                ((TextView) v).setTextColor(foreColor);
                ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                TextViewCompat.setCompoundDrawableTintList((TextView)v,colorStateList);
            }
        }
    }

    @Override
    public void loadDataAndUpdateScreen(){
        if ((mEditorSet == null) && (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null)) mEditorSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        observerSet(mEditorSet);
    }
    /** Restores the UI to active (non-ambient) mode. */
    @Override
    public void onExitAmbientInFragment() {
        Context context = mConstraintLayout.getContext();
        Resources.Theme myTheme = context.getTheme();

        int backColor = getResources().getColor(R.color.primaryDarkColor, myTheme);
        int foreColor = getResources().getColor(R.color.primaryTextColor, myTheme);
        ColorStateList btnBackColor =  AppCompatResources.getColorStateList(context,R.color.primaryColor);
        //  getResources().getColor(R.color.my_app_primary_color, myTheme);
        ColorStateList btnTransparent = AppCompatResources.getColorStateList(context, R.color.bg_mtrl_btn_bg_selector);
        ColorStateList colorStateList = getResources().getColorStateList(R.color.primaryTextColor, myTheme);
        ColorStateList colorStateList2 = getResources().getColorStateList(R.color.primaryDarkColor, myTheme);
        mConstraintLayout.setBackgroundTintList(colorStateList2);
        for (int i = 0; i < mConstraintLayout.getChildCount(); i++) {
            View v = mConstraintLayout.getChildAt(i);
            if (v instanceof com.google.android.material.button.MaterialButton) {
                ((MaterialButton) v).setTextColor(foreColor);
                ((MaterialButton) v).setIconTint(colorStateList);
                ((MaterialButton) v).setBackgroundTintList(btnBackColor);
            }
            if (v instanceof TextView){
                ((TextView)v).setTextColor(foreColor);
                if (v.getId() != R.id.textViewActivityName)  ((TextView)v).setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
                TextViewCompat.setCompoundDrawableTintList((TextView)v,AppCompatResources.getColorStateList(context,R.color.primaryTextColor));
            }
        }
    }

    private void doActionSet(WorkoutSet set){
        if (set == null) return;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_action_set,null,false);
        TextView title = view.findViewById(R.id.title_action);
        int setCount = set.setCount;
        String sTemp;
        if (Utilities.isShooting(set.activityID))
            sTemp = getString(R.string.label_shoot_end) + Constants.ATRACKIT_SPACE +  setCount + Constants.ATRACKIT_SPACE;
        else {
            sTemp = getString(R.string.label_action) + Constants.ATRACKIT_SPACE + setCount + Constants.SHOT_XY_DELIM + Constants.ATRACKIT_SPACE + set.exerciseName;
            if (sTemp.length() > 30)
                sTemp = setCount + Constants.SHOT_XY_DELIM + Constants.ATRACKIT_SPACE + set.exerciseName;
        }
        title.setText(sTemp);
        MaterialButton btnStart = view.findViewById(R.id.btn_action_start);
        MaterialButton btnEdit = view.findViewById(R.id.btn_action_edit);
        MaterialButton btnDelete = view.findViewById(R.id.btn_confirm_delete);

        builder.setIcon(R.drawable.noti_white_logo);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        if (title != null)
            title.setOnClickListener(v -> {
                if (alertDialog != null) alertDialog.dismiss();
            });

        btnStart.setOnClickListener(v -> {
            // start current set !!!!
            Intent start_setIntent = new Intent(INTENT_ACTIVE_RESUMED);
            if (userPrefs.getConfirmStartSession())
                start_setIntent.putExtra(KEY_FIT_TYPE, 1);  //ask
            else
                start_setIntent.putExtra(KEY_FIT_TYPE, 0);  // not external
            start_setIntent.putExtra(KEY_FIT_VALUE, (set.end == 0) ? 0 : 1); // start next indicator = start current
            start_setIntent.putExtra(Constants.KEY_FIT_WORKOUTID, set.workoutID);
            start_setIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, set._id);
            mMessagesViewModel.addLiveIntent(start_setIntent);
            if (alertDialog != null) alertDialog.dismiss();

        });
        btnEdit.setOnClickListener(v -> {
            set.scoreTotal = FLAG_PENDING;
            final String sSetMsg = String.format(Locale.getDefault(), getString(R.string.sets_summary1), set.setCount);
            mSavedStateViewModel.setActiveWorkoutSet(set);
            mScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mScrollView.scrollTo(0,btnExercise.getTop());
                    textViewBottom.setText(sSetMsg);
                }
            });
            if (alertDialog != null) alertDialog.dismiss();
        });
        btnDelete.setOnClickListener(v -> {
            // start current set !!!!
            Intent delete_setIntent = new Intent(Constants.INTENT_SET_DELETE);
            delete_setIntent.putExtra(KEY_FIT_USER, set.userID);
            if (userPrefs.getConfirmDeleteSession())
                delete_setIntent.putExtra(KEY_FIT_TYPE, 1);  //ask
            else
                delete_setIntent.putExtra(KEY_FIT_TYPE, 0);  // not external
            delete_setIntent.putExtra(Constants.KEY_COMM_TYPE, OBJECT_TYPE_WORKOUT_SET);
            delete_setIntent.putExtra(Constants.KEY_FIT_WORKOUTID, set.workoutID);
            delete_setIntent.putExtra(Constants.KEY_FIT_WORKOUT_SETID, set._id);
            mMessagesViewModel.addLiveIntent(delete_setIntent);
            if (alertDialog != null) alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void broadcastToast(String msg){
        Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
        msgIntent.setAction(INTENT_MESSAGE_TOAST);
        msgIntent.putExtra(INTENT_EXTRA_MSG, msg);
        msgIntent.putExtra(KEY_FIT_TYPE, 2);
        mMessagesViewModel.addLiveIntent(msgIntent);
    }
    private void doCountDownToggle(View v){
        String sPackageName = Constants.ATRACKIT_ATRACKIT_CLASS;
        int resId = 0; String sValue = "";
        int iType = (Integer)v.getTag();
        Long lValue = 0L;
        Resources res = getResources();
        UserPreferences userPrefs = UserPreferences.getPreferences(getContext(), appPrefs.getLastUserID());
        if (iType == 1){
            lValue = mTimerViewModel.getCurrentTime().getValue();
            resId = res.getIdentifier("ic_clock",Constants.ATRACKIT_DRAWABLE,sPackageName);
            sValue = Utilities.getTimeString(lValue);
        }
        if (iType == 0){
            resId = res.getIdentifier("ic_stopwatch_white",Constants.ATRACKIT_DRAWABLE,sPackageName);
            lValue = (mTimerViewModel.getCountdownTime().getValue() == null) ? 0L : mTimerViewModel.getCountdownTime().getValue();
            if (lValue == 0){
                // find the users default countdown
                Integer iCountDown = userPrefs.getCountdownDuration();
                if (iCountDown == 0){
                    iCountDown = 10;
                    userPrefs.setCountdownDuration(iCountDown);
                }
                mTimerViewModel.setCountdownTime(iCountDown.longValue());
                lValue = iCountDown.longValue();
            }
            sValue =  res.getQuantityString(R.plurals.start_countdown,lValue.intValue(),lValue.intValue());
        }
        final int resourceId = resId;
        final String sMsg = sValue;
        final long lVal = lValue;

        new Handler(Looper.getMainLooper()).post(() -> {
            if (iType != 1) {
                if (lVal <= 3)
                    ((MaterialButton) v).setTextSize(40F);
                else
                if (lVal <= 7)
                    ((MaterialButton) v).setTextSize(32F);
                else
                if (lVal <= 9)
                    ((MaterialButton) v).setTextSize(28F);
                else
                    ((MaterialButton) v).setTextSize(18F);

            }else
                ((MaterialButton) v).setTextSize(18F);
            // textViewCenter.setCompoundDrawables(getContext().getDrawable(resourceId), null, null, null);
            ((MaterialButton) v).setCompoundDrawablesWithIntrinsicBounds(resourceId,0,0,0);
            ((MaterialButton) v).setCompoundDrawablePadding(6);
            if ((sMsg != null) && (sMsg.length() > 0)) ((MaterialButton) v).setText(sMsg);
        });
    }

    private void setDurationRest(int iRestSeconds){
        com.google.android.material.button.MaterialButton btnRest = mConstraintLayout.findViewById(R.id.btnEntryRest);
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
}
