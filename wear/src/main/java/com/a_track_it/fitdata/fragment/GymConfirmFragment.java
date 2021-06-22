package com.a_track_it.fitdata.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.wear.ambient.AmbientModeSupport;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.adapter.WorkoutAdapter;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModel;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.a_track_it.fitdata.common.user_model.MessagesViewModel;
import com.a_track_it.fitdata.common.user_model.SavedStateViewModel;
import com.google.android.gms.fitness.data.Field;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.fitdata.common.Constants.FLAG_NON_TRACKING;
import static com.a_track_it.fitdata.common.Constants.INTENT_EXTRA_MSG;
import static com.a_track_it.fitdata.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.fitdata.common.Constants.KEY_FIT_TYPE;

public class GymConfirmFragment extends Fragment implements AmbientInterface{
    private final static String LOG_TAG = GymConfirmFragment.class.getSimpleName();
    private final static String LABEL_LAST_SET = "Confirm Last Set ";
    private final static String LABEL_NEXT_SET = "Next Set ";
    private final static String LABEL_COMPLETED_SET = "Completed ";
    private final static String LABEL_FINAL_SET = "Workout Completed!";
    private final static String LABEL_NO_SET = "Set";
    private final static String LABEL_DIV = " / ";
    private MessagesViewModel mMessagesViewModel;
    private SavedStateViewModel mSavedStateViewModel;
    private WorkoutViewModel mSessionViewModel;
    private FragmentInterface mListener;
    private UserPreferences userPrefs;
    private Context mContext;
    private ConstraintLayout mConstraintLayout;
    private androidx.core.widget.NestedScrollView mScrollView;
    private WearableLinearLayoutManager mWearableLinearLayoutManager;
    private TextView topView;
    private MaterialButton btnBodypart;
    private MaterialButton btnExercise;
    private MaterialButton btnConfirmAddExercise;
    private Chronometer chronometerConfirm;
    private MaterialButton btnMinusWeight;
    private MaterialButton btnPlusWeight;
    private MaterialButton btnMinusReps;
    private MaterialButton btnPlusReps;
    private MaterialButton btnConfirmContinue;
    private MaterialButton btnConfirmEdit;
    private MaterialButton btnConfirmSave;
    private MaterialButton btnFinish;
    private MaterialButton btnTopFinish;
    private MaterialButton btnConfirmWeight;
    private MaterialButton btnConfirmReps;
    private MaterialButton btnRest;
    private MaterialButton confirm_use_timed_rest_toggle;
    private MaterialButton confirm_auto_start_toggle;
    private MaterialButton btnConfirmExerciseRepeat;
    private TextView textViewNextLabel;
    private TextView textViewConfirmContinue;
    private TextView textViewNextSetsLabel;
    private TextView textViewBottom;
    private Chronometer chronometer;
    private WearableRecyclerView recyclerViewItems;
    private Workout mWorkout;
    private WorkoutSet mWorkoutSet;
    private boolean bUseKg = false;
    private boolean hasAlarmed = false;
    private boolean bUseTimedRest = false;
    private boolean bRestAutoStart = false;
    public GymConfirmFragment() {
        // Required empty public constructor
    }
    public static GymConfirmFragment newInstance() {
        GymConfirmFragment fragment = new GymConfirmFragment();
        return fragment;
    }
    private void setDurationRest(int iRestSeconds){
        com.google.android.material.button.MaterialButton btnRest = mConstraintLayout.findViewById(R.id.btnConfirmRest);
        long milliseconds = (TimeUnit.SECONDS.toMillis(iRestSeconds));
        Context context = (mContext != null) ? mContext : getContext();
        if (context == null) return;
        String sTemp;
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

    private void broadcastToast(String msg){
        Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
        msgIntent.setAction(INTENT_MESSAGE_TOAST);
        msgIntent.putExtra(INTENT_EXTRA_MSG, msg);
        msgIntent.putExtra(KEY_FIT_TYPE, 2);
        msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
        if (mContext == null) mContext = getContext();
        mContext.sendBroadcast(msgIntent);
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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessagesViewModel = new ViewModelProvider(requireActivity()).get(MessagesViewModel.class);
        mSavedStateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        WorkoutViewModelFactory factory = com.a_track_it.fitdata.common.InjectorUtils.getWorkoutViewModelFactory(getContext());
        mSessionViewModel = new ViewModelProvider(requireActivity(),factory).get(WorkoutViewModel.class);
        if (getArguments() != null) {
            mWorkout = getArguments().getParcelable(Workout.class.getSimpleName());
            mWorkoutSet = getArguments().getParcelable(WorkoutSet.class.getSimpleName());
        }else
        if (savedInstanceState !=null){
            mWorkout = savedInstanceState.getParcelable(Workout.class.getSimpleName());
            mWorkoutSet = savedInstanceState.getParcelable(WorkoutSet.class.getSimpleName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gymconfirm, container, false);
        Context context = (mContext!=null)? mContext : getContext();

        mScrollView = rootView.findViewById(R.id.scrollViewConfirm);
        mConstraintLayout = rootView.findViewById(R.id.confirm_constraint);
        ApplicationPreferences appPrefs = ApplicationPreferences.getPreferences(context);
        String sUserId = appPrefs.getLastUserID();
        userPrefs = UserPreferences.getPreferences(context, sUserId);
        bRestAutoStart = userPrefs.getRestAutoStart();
        bUseTimedRest = userPrefs.getTimedRest();
        topView = rootView.findViewById(R.id.textViewConfirmName);
        btnBodypart = rootView.findViewById(R.id.btnConfirmBodypart);
        btnBodypart.setTag(Constants.UID_btnBodypart);
        btnExercise = rootView.findViewById(R.id.btnConfirmExercise);
        btnExercise.setTag(Constants.UID_btnExercise);
        chronometerConfirm = rootView.findViewById(R.id.chronometerConfirm);
        btnConfirmAddExercise = rootView.findViewById(R.id.btnConfirmAddExercise);
        btnConfirmAddExercise.setTag(Constants.UID_btnAddExercise);
        btnMinusWeight = rootView.findViewById(R.id.btnWeightMinus);
        btnMinusWeight.setTag(Constants.UID_btnWeightMinus);
        btnPlusWeight = rootView.findViewById(R.id.btnWeightPlus);
        btnPlusWeight.setTag(Constants.UID_btnWeightPlus);
        btnMinusReps = rootView.findViewById(R.id.btnRepsMinus);
        btnMinusReps.setTag(Constants.UID_btnRepsMinus);
        btnPlusReps = rootView.findViewById(R.id.btnRepsPlus);
        btnPlusReps.setTag(Constants.UID_btnRepsPlus);
        btnConfirmContinue = rootView.findViewById(R.id.btnConfirmContinue);
        btnConfirmContinue.setTag(Constants.UID_btnContinue);
        btnConfirmEdit = rootView.findViewById(R.id.btnConfirmEdit);
        btnConfirmEdit.setTag(Constants.UID_btnConfirmEdit);
        btnConfirmSave = rootView.findViewById(R.id.btnSaveLast);
        btnConfirmSave.setTag(Constants.UID_btnSave);
        btnFinish = rootView.findViewById(R.id.btnConfirmFinish);
        btnTopFinish = rootView.findViewById(R.id.btnTopFinish);
        btnFinish.setTag(Constants.UID_btnConfirmFinish);
        btnTopFinish.setTag(Constants.UID_btnConfirmFinish);
        btnConfirmWeight = rootView.findViewById(R.id.btnConfirmWeight);
        btnConfirmWeight.setTag(Constants.UID_btnWeight);
        btnConfirmReps = rootView.findViewById(R.id.btnConfirmReps);
        btnConfirmReps.setTag(Constants.UID_btnReps);
        btnRest = rootView.findViewById(R.id.btnConfirmRest);
        btnRest.setTag(Constants.UID_btnRest);
        confirm_use_timed_rest_toggle = rootView.findViewById(R.id.confirm_timed_rest_toggle);
        confirm_auto_start_toggle = rootView.findViewById(R.id.confirm_auto_start_toggle);
        btnConfirmExerciseRepeat = rootView.findViewById(R.id.btnConfirmExerciseRepeat);
        btnConfirmExerciseRepeat.setTag(Constants.UID_btnRepeat);
        textViewNextLabel = rootView.findViewById(R.id.textViewNextLabel);
        textViewConfirmContinue = rootView.findViewById(R.id.textViewConfirmContinue);
        textViewNextSetsLabel = rootView.findViewById(R.id.textViewNextSetsLabel);
        textViewBottom = rootView.findViewById(R.id.confirm_bottom_ATrackIt);
        chronometer = rootView.findViewById(R.id.chronoConfirmCenter);
        recyclerViewItems = rootView.findViewById(R.id.recycleViewConfirmItems);
        final Drawable drawableChecked = AppCompatResources.getDrawable(mConstraintLayout.getContext(),R.drawable.ic_outline_check_white);
        Utilities.setColorFilter(drawableChecked,ContextCompat.getColor(mConstraintLayout.getContext(), R.color.colorSplash));
        final Drawable drawableUnChecked = AppCompatResources.getDrawable(mConstraintLayout.getContext(),R.drawable.ic_close_white);
        Utilities.setColorFilter(drawableUnChecked,ContextCompat.getColor(mConstraintLayout.getContext(), R.color.secondaryTextColor));
        final Drawable drawableKG = AppCompatResources.getDrawable(mConstraintLayout.getContext(),R.drawable.ic_weight);
        Utilities.setColorFilter(drawableKG,ContextCompat.getColor(mConstraintLayout.getContext(), R.color.colorSplash));
        final Drawable drawableLBS = AppCompatResources.getDrawable(mConstraintLayout.getContext(),R.drawable.ic_pounds_weight);
        Utilities.setColorFilter(drawableLBS,ContextCompat.getColor(mConstraintLayout.getContext(), R.color.colorSplash));
        chronometerConfirm.setOnClickListener(myClicker);
        chronometerConfirm.setBase(0);
        topView.setOnClickListener(myClicker);
        btnBodypart.setOnClickListener(myClicker);
        btnBodypart.setOnLongClickListener(myLongClicker);
        btnConfirmAddExercise.setOnClickListener(myClicker);
        btnExercise.setOnClickListener(myClicker);
        btnExercise.setOnLongClickListener(myLongClicker);
        btnMinusWeight.setOnClickListener(myClicker);
        btnPlusWeight.setOnClickListener(myClicker);
        btnMinusReps.setOnClickListener(myClicker);
        btnConfirmReps.setOnClickListener(myClicker);
        btnConfirmReps.setOnLongClickListener(myLongClicker);
        btnPlusReps.setOnClickListener(myClicker);
        btnConfirmContinue.setOnClickListener(myClicker);
        btnRest.setOnClickListener(myClicker);
        btnRest.setOnLongClickListener(myLongClicker);
        btnConfirmSave.setOnClickListener(v -> {
            mListener.OnFragmentInteraction(Constants.UID_btnSave,0,null);
            final int pos = (chronometer.getVisibility() == Chronometer.VISIBLE) ? chronometer.getTop() : btnConfirmContinue.getTop();
            if (mScrollView != null)
                mScrollView.postDelayed(() -> mScrollView.scrollTo(0,pos),1000);
        });

        btnFinish.setOnClickListener(finishClicker);
        btnTopFinish.setOnClickListener(finishClicker);
        btnConfirmWeight.setOnClickListener(finishClicker);
        btnConfirmEdit.setOnClickListener(finishClicker);
        btnConfirmContinue.setOnLongClickListener(myLongClicker);
        btnConfirmExerciseRepeat.setOnClickListener(myClicker);
        textViewNextLabel.setOnClickListener(myClicker);
        textViewConfirmContinue.setOnClickListener(myClicker);
        textViewNextSetsLabel.setOnClickListener(myClicker);


        bUseKg = userPrefs.getUseKG();

        ArrayList<Workout> workouts = new ArrayList<>();
        ArrayList<WorkoutSet> sets = new ArrayList<>();
        final WorkoutAdapter workoutAdapter = new WorkoutAdapter(context, workouts, sets, bUseKg);
        workoutAdapter.setListType(false);
        CustomScrollingLayoutCallback customScrollingLayoutCallback = new CustomScrollingLayoutCallback();
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mWearableLinearLayoutManager = new WearableLinearLayoutManager(context, customScrollingLayoutCallback);
        recyclerViewItems.setLayoutManager(mWearableLinearLayoutManager);
        recyclerViewItems.setCircularScrollingGestureEnabled(false);
        recyclerViewItems.setBezelFraction(0.5f);
        recyclerViewItems.setScrollDegreesPerScreen(90);
        // To align the edge children (first and last) with the center of the screen
        recyclerViewItems.setEdgeItemsCenteringEnabled(true);
        recyclerViewItems.setHasFixedSize(true);
        recyclerViewItems.setAdapter(workoutAdapter);

        int currentSetIndex = (mWorkoutSet != null) ? mWorkoutSet.setCount : mSavedStateViewModel.getSetIndex();
        if (sets.size() > 1){
            if (currentSetIndex < sets.size()) btnConfirmContinue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            else btnConfirmContinue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        }
        confirm_use_timed_rest_toggle.setChecked(userPrefs.getTimedRest());
        if (userPrefs.getTimedRest()){
            confirm_use_timed_rest_toggle.setIcon(drawableChecked);
            confirm_auto_start_toggle.setVisibility(View.VISIBLE);
            btnRest.setVisibility(MaterialButton.VISIBLE);
            btnRest.setEnabled(true);
        }else {
            confirm_use_timed_rest_toggle.setIcon(drawableUnChecked);
            confirm_auto_start_toggle.setVisibility(View.GONE);
            btnRest.setVisibility(MaterialButton.GONE);
        }
        if (userPrefs.getRestAutoStart()){
            confirm_auto_start_toggle.setIcon(drawableChecked);
        }else {
            confirm_auto_start_toggle.setIcon(drawableUnChecked);
        }
        confirm_auto_start_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton) v).isChecked();
            userPrefs.setRestAutoStart(isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
            }
        });
        confirm_use_timed_rest_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton) v).isChecked();
            userPrefs.setTimedRest(isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
                confirm_auto_start_toggle.setVisibility(View.VISIBLE);
                btnRest.setVisibility(MaterialButton.VISIBLE);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
                confirm_auto_start_toggle.setVisibility(View.GONE);
                btnRest.setVisibility(MaterialButton.GONE);
            }
        });
        
        AssetManager asm = getContext().getAssets();
        int intBestVPos = 0;
        if (asm != null) {
            Typeface typeface = Typeface.createFromAsset(
                    asm, Constants.ATRACKIT_FONT);
            if (typeface != null) {
                chronometerConfirm.setTypeface(typeface);
                topView.setTypeface(typeface);
                textViewBottom.setTypeface(typeface);
                textViewBottom.setTextColor(ContextCompat.getColor(getContext(),R.color.colorSplash));
            }
        }
        long iconActivityId = (mSavedStateViewModel.getIconID() != null) ? mSavedStateViewModel.getIconID() : R.drawable.ic_strong_man_silhouette;
        final int id = Math.toIntExact(iconActivityId);
        int iColor = (mSavedStateViewModel.getColorID() != null) ?mSavedStateViewModel.getColorID() : R.color.primaryTextColor;
        Resources res = getResources();
        final Drawable vectorDrawable = VectorDrawableCompat.create(res, id, mContext.getTheme());
        //final int idColor = res.getColor(iColor, null);
        ColorStateList colorStateList = ContextCompat.getColorStateList(context, iColor);
        topView.setCompoundDrawables(null,null,null,vectorDrawable);

        TextViewCompat.setCompoundDrawableTintList(topView,colorStateList);
       // if (!bUseTimedRest) chronometer.setVisibility(Chronometer.GONE);

        long pauseStart = SystemClock.elapsedRealtime();
      //  final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-dd HH:mm:ss z", Locale.getDefault());
        final long rest_duration = ((mWorkoutSet != null) && mWorkoutSet.rest_duration != null)? mWorkoutSet.rest_duration : 0;
        if ((mWorkoutSet != null && mWorkoutSet.rest_duration != null) && bUseTimedRest && (mWorkoutSet.rest_duration > 0)){
            hasAlarmed = false;
            if (bRestAutoStart) mSavedStateViewModel.setRestStop(System.currentTimeMillis() + rest_duration);
            chronometer.setBase(pauseStart + rest_duration);
            chronometer.setCountDown(true);
            chronometer.setOnChronometerTickListener(chronometer1 -> {
                long endTime = mSavedStateViewModel.getRestStop();
                long now = System.currentTimeMillis();
                if  (!hasAlarmed && ((endTime - now) < TimeUnit.MILLISECONDS.toMillis(500L))) {
                    chronometer1.setCountDown(false);
                    Intent vibrateIntent = new Intent(Constants.INTENT_VIBRATE);
                    vibrateIntent.putExtra(Constants.KEY_FIT_TYPE, 2);
                    mMessagesViewModel.addLiveIntent(vibrateIntent);
                    hasAlarmed = true;
                    // auto start by sending the click - ha, ha
                    if (bRestAutoStart){
                        mListener.OnFragmentInteraction(Constants.UID_btnContinue, 0, null);
                    }
                }
            });
        }else
            chronometer.setBase(pauseStart);
        chronometer.start();

        if ((mWorkoutSet.bodypartName != null) && (mWorkoutSet.bodypartName.length() > 0)) {
            btnBodypart.setText(mWorkoutSet.bodypartName);
            intBestVPos = btnBodypart.getTop();
        }
        if ((mWorkoutSet.exerciseName != null) && (mWorkoutSet.exerciseName.length() > 0)) {
            btnExercise.setText(mWorkoutSet.exerciseName);
            if (intBestVPos == 0) intBestVPos = btnExercise.getTop();
        }else {
            btnConfirmExerciseRepeat.setVisibility(View.GONE);
            btnExercise.requestFocus();
            broadcastToast(getString(R.string.label_exercise));
        }
        String sTemp;
        Drawable kgDrawable = AppCompatResources.getDrawable(mContext, R.drawable.ic_weight);
        int imageColor = ContextCompat.getColor(mContext, R.color.primaryTextColor);
        if (kgDrawable != null)
            Utilities.setColorFilter(kgDrawable,imageColor);
        Drawable lbsDrawable = AppCompatResources.getDrawable(mContext, R.drawable.ic_pounds_weight);
        if (lbsDrawable != null)
            Utilities.setColorFilter(lbsDrawable,imageColor);
        final Drawable kgWeight = kgDrawable;
        final Drawable lbsWeight = lbsDrawable;
        TextView tvWeight = mConstraintLayout.findViewById(R.id.textViewWeightLabel);
        if (bUseKg) {
            sTemp = getString(R.string.label_weight);
            tvWeight.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,kgWeight,null);
            tvWeight.setCompoundDrawablePadding(6);
        }else {
            sTemp = getString(R.string.label_weight);
            tvWeight.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,lbsWeight,null);
            tvWeight.setCompoundDrawablePadding(6);
        }
        tvWeight.setText(sTemp);
        if ((mWorkoutSet.weightTotal != null) && (mWorkoutSet.weightTotal > 0)) {
            intBestVPos = btnExercise.getTop();
            float tempWeight = ((bUseKg) ? mWorkoutSet.weightTotal : Utilities.KgToPoundsDisplay(mWorkoutSet.weightTotal));
            double intWeight =  Math.floor(tempWeight);
            if (tempWeight % intWeight != 0)
                sTemp = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
            else
                sTemp = new DecimalFormat("#").format(intWeight);
            btnConfirmWeight.setText(sTemp);
        }else {
            if ((mWorkoutSet.resistance_type != null) && (mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY))
                btnConfirmWeight.requestFocus();
        }
        sTemp = Integer.toString(mWorkoutSet.repCount != null ? mWorkoutSet.repCount:0);
        btnConfirmReps.setText(sTemp);
        final int Vpos = intBestVPos;
        if ((mScrollView != null) && (intBestVPos > 0)) {
            new Handler(Looper.myLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Log.i(LOG_TAG, "scrollTo " + Vpos);
                    mScrollView.scrollTo(0, Vpos);

                }
            });
        }
        mSavedStateViewModel.getActiveWorkout().observe(getViewLifecycleOwner(), new Observer<Workout>() {
            @Override
            public void onChanged(Workout workout) {
                if (workout == null) return;
                Log.e(LOG_TAG, "observeWorkout " + workout.toString());
                if (chronometerConfirm.getBase() == 0){
                 //   mWorkout = workout;
                    long elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
                    chronometerConfirm.setBase(mWorkout.start - elapsedRealtimeOffset);
                    chronometerConfirm.start();
                }
            }
        });
        mSavedStateViewModel.getActiveWorkoutSet().observe(getViewLifecycleOwner(), set -> {
            observerSet(set);
        });
        final String sSets1 = getString(R.string.sets_summaryOf2);
        final String sSets2 = getString(R.string.sets_summaryOf);
        // display next set and other decisions
        mSavedStateViewModel.getToDoSets().observe(requireActivity(), workoutSets -> {
            if (workoutSets != null) {
                ArrayList<WorkoutSet> sets1 = new ArrayList<>(workoutSets);
                sets1.sort((o1, o2) -> ((o1.setCount < o2.setCount) ? -1: ((o1.setCount > o2.setCount) ? 1 : 0)));
                int currentSetSize = workoutSets.size();
                String sSetMsg = mWorkout.activityName;
                int currentSet = mSavedStateViewModel.getSetIndex();
                if (currentSetSize < 2)
                    sSetMsg = String.format(Locale.getDefault(), sSets1, currentSetSize);
                else
                if ((currentSet > 0) && (currentSet <= currentSetSize))
                    sSetMsg = String.format(Locale.getDefault(), sSets2, currentSet, currentSetSize);
                workoutAdapter.setWorkoutSetArrayList(sets1);
                try {
                    final String sBottom = sSetMsg;
                    new Handler(Looper.myLooper()).post(() -> {
                        if (currentSetSize < 2){
                            textViewNextSetsLabel.setVisibility(View.INVISIBLE);
                            recyclerViewItems.setVisibility(View.GONE);
                        }else{
                            textViewNextSetsLabel.setVisibility(View.VISIBLE);
                            recyclerViewItems.setVisibility(View.VISIBLE);
                        }
                        textViewBottom.setText(sBottom);
                    });
                }catch (Exception ne){
                    String sMsg = ne.getMessage();
                    if (sMsg != null)
                        Log.e(LOG_TAG, sMsg);
                }
            }else{
                workoutAdapter.setWorkoutSetArrayList(null);
            }
            workoutAdapter.notifyDataSetChanged();
        });
        if (mWorkout != null) mSavedStateViewModel.setActiveWorkout(mWorkout);
        if (mWorkoutSet != null) mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
        return rootView;
    }
    private void observerSet(WorkoutSet set){
        if (set == null) return;
        Log.e(LOG_TAG, "observeSet " + set.toString());
        List<WorkoutSet> setList = mSavedStateViewModel.getToDoSets().getValue();
        final int nbrOfSets = (setList == null) ? 0 : setList.size();
        final int currentIndex = set.setCount;
        String sTempNext = Constants.ATRACKIT_EMPTY;
        if ((currentIndex > 0) && currentIndex <= nbrOfSets) {
            if (setList != null && currentIndex < setList.size()) {
                WorkoutSet setNext = setList.get(currentIndex);
                if ((setNext.exerciseName != null) && (setNext.exerciseName.length() > 0)) {
                    sTempNext = LABEL_NEXT_SET + (set.setCount + 1) + LABEL_DIV + nbrOfSets + " " + setNext.exerciseName;
                } else sTempNext = Constants.ATRACKIT_EMPTY;
            }
        }
        if (sTempNext.length() > 0) Log.e(LOG_TAG, "observeSet next " + sTempNext);
        final String nextLabel = sTempNext;
        final String sTop = set.activityName + Constants.LINE_DELIMITER + getString(R.string.label_confirm_prev);

        final boolean bIsValid = set.isValid(true);
        final Context myContext = mConstraintLayout.getContext();
        new Handler(Looper.getMainLooper()).post(() -> {
            String sTemp2;
            if (set.activityID != null &&  set.activityID > 0){
                if (Utilities.isGymWorkout(set.activityID)) {
                    if (bIsValid){
                        btnFinish.setEnabled(true);
                        btnTopFinish.setEnabled(true);
                        btnConfirmExerciseRepeat.setVisibility(MaterialButton.VISIBLE);
                        textViewConfirmContinue.setVisibility(View.VISIBLE);
                        btnConfirmEdit.setVisibility(MaterialButton.VISIBLE);
                        btnConfirmSave.setEnabled(true);
                    }else{
                        btnFinish.setEnabled(false);
                        btnTopFinish.setEnabled(false);
                        btnConfirmSave.setEnabled(false);
                        btnConfirmExerciseRepeat.setVisibility(MaterialButton.GONE);
                        textViewConfirmContinue.setVisibility(View.GONE);
                        btnConfirmEdit.setVisibility(MaterialButton.GONE);
                    }
                    if ((set.bodypartName != null) && (set.bodypartName.length() > 0)){
                        btnBodypart.setText(set.bodypartName);
                    }else
                        btnBodypart.setText(myContext.getString(R.string.label_bodypart));
                    if ((set.exerciseName != null) && (set.exerciseName.length() > 0)) {
                        btnExercise.setText(set.exerciseName);
                        btnConfirmExerciseRepeat.setVisibility(View.VISIBLE);
                    }else {
                        btnConfirmExerciseRepeat.setVisibility(View.GONE);
                        btnExercise.requestFocus();
                    }
                    if (set.weightTotal != null && set.weightTotal > 0) {
                        if (bUseKg)
                            sTemp2 = String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, set.weightTotal); // + Constants.KG_TAIL;
                        else
                            sTemp2 = Utilities.KgToPoundsDisplay(set.weightTotal) + Constants.LBS_TAIL;
                        btnConfirmWeight.setText(sTemp2);
                    }else
                        btnConfirmWeight.requestFocus();
                    if (set.repCount == null) set.repCount = userPrefs.getDefaultNewReps();
                    sTemp2 = Integer.toString(set.repCount);
                    btnConfirmReps.setText(sTemp2);
                    if (set.rest_duration != null &&  set.rest_duration > 0){
                        int iWeightRest = userPrefs.getWeightsRestDuration();
                        setDurationRest(iWeightRest);
                    }
                    if (nbrOfSets < 2){
                        textViewNextSetsLabel.setVisibility(View.INVISIBLE);
                    }else{
                        textViewNextSetsLabel.setVisibility(View.VISIBLE);
                        String sText = LABEL_LAST_SET + set.setCount + LABEL_DIV + nbrOfSets;
                        textViewNextLabel.setText(sText);
                    }
                    textViewConfirmContinue.setText(nextLabel);

                    topView.setText(sTop);
                    topView.setTextSize(TypedValue.COMPLEX_UNIT_SP, Utilities.getLabelFontSize(sTop));
                }
            }
        });
    }
    private void observerActiveSet(WorkoutSet set){
        if (set == null) return;
        final boolean bIsValid = set.isValid(true);
        final Context myContext = mConstraintLayout.getContext();
        final int nbrOfSets = mWorkout.setCount;
        final int currentIndex = set.setCount;
        String sTempNext = Constants.ATRACKIT_EMPTY;
        Log.e(LOG_TAG, "observeSet " + set.toString());
        if ((currentIndex > 0) && currentIndex <= nbrOfSets) {
            WorkoutSet setNext = mSavedStateViewModel.getToDoSets().getValue().get(currentIndex-1);
            if ((setNext.exerciseName != null) && (setNext.exerciseName.length() > 0)) {
                sTempNext = LABEL_NEXT_SET + (set.setCount +1) + LABEL_DIV + nbrOfSets + " " + setNext.exerciseName;
            } else sTempNext = Constants.ATRACKIT_EMPTY;
        }
        final String nextLabel = sTempNext;
        final String sTop = set.activityName + Constants.LINE_DELIMITER + myContext.getString(R.string.label_confirm_prev);
        new Handler(Looper.myLooper()).post(() -> {
            String sTemp2;
            if ((set.activityID != null) && (set.activityID > 0)){
                if (Utilities.isGymWorkout(set.activityID)) {
                    if (bIsValid){
                        btnFinish.setEnabled(true);
                        btnTopFinish.setVisibility(View.VISIBLE);
                        btnConfirmExerciseRepeat.setVisibility(MaterialButton.VISIBLE);
                        textViewConfirmContinue.setVisibility(View.VISIBLE);
                        btnConfirmEdit.setVisibility(MaterialButton.VISIBLE);
                        btnConfirmSave.setEnabled(true);
                    }
                    else{
                        // btnFinish.setEnabled(false);
                        btnTopFinish.setVisibility(View.GONE);
                        btnConfirmSave.setEnabled(false);
                        btnConfirmExerciseRepeat.setVisibility(MaterialButton.GONE);
                        textViewConfirmContinue.setVisibility(View.GONE);
                        btnConfirmEdit.setVisibility(MaterialButton.GONE);
                    }
                    if ((set.bodypartName != null) && (set.bodypartName.length() > 0)){
                        btnBodypart.setText(set.bodypartName);
                    }else
                        btnBodypart.setText(myContext.getString(R.string.label_bodypart));
                    if ((set.exerciseName != null) && (set.exerciseName.length() > 0)) {
                        btnExercise.setText(set.exerciseName);
                        btnConfirmExerciseRepeat.setVisibility(View.VISIBLE);
                    }else {
                        btnConfirmExerciseRepeat.setVisibility(View.GONE);
                        btnExercise.requestFocus();
                    }
                    if (set.weightTotal > 0) {
                        float tempWeight = ((bUseKg) ? set.weightTotal : Utilities.KgToPoundsDisplay(set.weightTotal));
                        double intWeight =  Math.floor(tempWeight);
                        if (tempWeight % intWeight != 0)
                            sTemp2 = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT, tempWeight);
                        else
                            sTemp2 = new DecimalFormat("#").format(intWeight);
                        btnConfirmWeight.setText(sTemp2);
                    }else
                        btnConfirmWeight.requestFocus();

                    sTemp2 = Integer.toString(set.repCount != null ? set.repCount : 0);
                    btnConfirmReps.setText(sTemp2);
                    if (set.rest_duration != null &&  set.rest_duration > 0){
                        int iWeightRest = userPrefs.getWeightsRestDuration();
                        setDurationRest(iWeightRest);
                    }
                    if (nbrOfSets < 2){
                        textViewNextSetsLabel.setVisibility(View.INVISIBLE);
                    }else{
                        textViewNextSetsLabel.setVisibility(View.VISIBLE);
                        String sText = LABEL_LAST_SET + set.setCount + LABEL_DIV + nbrOfSets;
                        textViewNextLabel.setText(sText);
                    }
                    textViewConfirmContinue.setText(nextLabel);

                    topView.setText(sTop);
                    topView.setTextSize(TypedValue.COMPLEX_UNIT_SP, Utilities.getLabelFontSize(sTop));
                }
            }
        });
    }
    final View.OnClickListener finishClicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            WorkoutSet set = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if ((set != null) && set.isValid(true) && (set.scoreTotal != FLAG_NON_TRACKING)){
                textViewConfirmContinue.setVisibility(View.GONE);
                textViewNextSetsLabel.setVisibility(View.GONE);
                if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(),set.activityID, set.activityName);
            }else {
                String sMsg = Constants.ATRACKIT_EMPTY;
                if (!set.isValid(false)) {
                    if ((set.exerciseName != null) && (set.exerciseName.length() == 0)) {
                        sMsg = getString(R.string.edit_set_exercise);
                    } else {
                        if (set.repCount == 0) {
                            sMsg = getString(R.string.edit_set_reps);
                        } else if ((set.resistance_type != null) && (set.resistance_type != Field.RESISTANCE_TYPE_BODY) && (set.weightTotal == null) || (set.weightTotal == 0F)) {
                            sMsg = getString(R.string.edit_set_weight);
                        }
                    }
                    if (sMsg.length() > 0)
                        broadcastToast(sMsg);
                }
                if ((set.exerciseName == null) || (set.exerciseName.length() == 0))
                    btnExercise.requestFocus();
            }
        }
    };
    final View.OnClickListener myClicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(), 0, null);
        }
    };
    final  View.OnLongClickListener myLongClicker = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(), 0, Constants.LABEL_LONG);
            return true;
        }
    };
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInterface) {
            mListener = (FragmentInterface) context;
            mContext = context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mContext = null;
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mWorkout != null) outState.putParcelable(Workout.class.getSimpleName(),mWorkout);
        if (mWorkoutSet != null) outState.putParcelable(WorkoutSet.class.getSimpleName(),mWorkoutSet);
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        boolean IsLowBitAmbient =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false);
        boolean DoBurnInProtection =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false);
        int backColor = ContextCompat.getColor(mContext, R.color.ambientBackground);
        int foreColor = ContextCompat.getColor(mContext, R.color.ambientForeground);
        ColorStateList colorStateList = ContextCompat.getColorStateList(mContext, R.color.ambientForeground);
        ColorStateList colorStateList2 = ContextCompat.getColorStateList(mContext, R.color.ambientBackground);
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
                if ((v.getId() != R.id.textViewActivityName) && (v.getId() != R.id.chronoConfirmCenter) && (v.getId() != R.id.chronometerConfirm)) ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                TextViewCompat.setCompoundDrawableTintList((TextView)v,colorStateList);
            }
        }
    }

    @Override
    public void loadDataAndUpdateScreen(){
        if ((mWorkoutSet == null) && (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null)) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        observerSet(mWorkoutSet);
    }

    @Override
    public void onExitAmbientInFragment() {

        int foreColor = getResources().getColor(R.color.primaryTextColor, getContext().getTheme());
        int btnBackColor = getResources().getColor(R.color.primaryColor, getContext().getTheme());
        ColorStateList colorStateList2 = ContextCompat.getColorStateList(getContext(), R.color.primaryDarkColor);
        mConstraintLayout.setBackgroundTintList(colorStateList2);
        ColorStateList colorStateList = ContextCompat.getColorStateList(getContext(),R.color.primaryTextColor);
        for (int i = 0; i < mConstraintLayout.getChildCount(); i++) {
            View v = mConstraintLayout.getChildAt(i);
            if (v instanceof com.google.android.material.button.MaterialButton) {
                ((MaterialButton) v).setTextColor(foreColor);
                ((MaterialButton) v).setIconTint(colorStateList);
                ((MaterialButton) v).setBackgroundColor(btnBackColor);
            }
            if (v instanceof TextView){
                ((TextView)v).setTextColor(foreColor);
                if ((v.getId() != R.id.textViewActivityName) && (v.getId() != R.id.chronoConfirmCenter) && (v.getId() != R.id.chronometerConfirm)) ((TextView)v).setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
                TextViewCompat.setCompoundDrawableTintList((TextView)v,colorStateList);
            }
        }
    }
}
