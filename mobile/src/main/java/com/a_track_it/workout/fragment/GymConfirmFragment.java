package com.a_track_it.workout.fragment;


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
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.a_track_it.workout.R;
import com.a_track_it.workout.adapter.WorkoutAdapter;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.Workout;
import com.a_track_it.workout.common.data_model.WorkoutSet;
import com.a_track_it.workout.common.data_model.WorkoutViewModel;
import com.a_track_it.workout.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.a_track_it.workout.common.model.UserPreferences;
import com.a_track_it.workout.common.user_model.MessagesViewModel;
import com.a_track_it.workout.common.user_model.SavedStateViewModel;
import com.google.android.gms.fitness.data.Field;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.workout.common.Constants.ATRACKIT_EMPTY;
import static com.a_track_it.workout.common.Constants.ATRACKIT_SPACE;
import static com.a_track_it.workout.common.Constants.FLAG_PENDING;
import static com.a_track_it.workout.common.Constants.INTENT_EXTRA_MSG;
import static com.a_track_it.workout.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.workout.common.Constants.KEY_FIT_TYPE;

/**
 * A simple {@link Fragment} subclass.
 */
public class GymConfirmFragment extends Fragment {
    private final static String LOG_TAG = GymConfirmFragment.class.getSimpleName();
    private final static String LABEL_LAST_SET = "Confirm Last Set ";
    private final static String LABEL_NEXT_SET = "Next Set ";
    private final static String LABEL_COMPLETED_SET = "Completed ";
    private final static String LABEL_FINAL_SET = "Workout Completed!";
    private final static String LABEL_NO_SET = "Set";
    private final static String LABEL_DIV = " / ";
    private MessagesViewModel mMessagesViewModel;
    private Context mContext;

    private SavedStateViewModel mSavedStateViewModel;
    private WorkoutViewModel mSessionViewModel;
    private FragmentInterface mListener;
    private ConstraintLayout mConstraintLayout;
    private ScrollView mScrollView;
    private LinearLayoutManager mLinearLayoutManager;
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
    private Chronometer chronometer;
    private RecyclerView recyclerViewItems;    
    
    private boolean bUseKg = false;
    private boolean hasAlarmed = false;
    private boolean bUseTimedRest = false;
    private boolean bRestAutoStart = false;
    public int flagFragment;
    private Workout mWorkout;
    private WorkoutSet mWorkoutSet;
    ApplicationPreferences appPrefs;
    String sUserId;
    UserPreferences userPrefs;

    public GymConfirmFragment() {
        // Required empty public constructor
    }
    public static GymConfirmFragment newInstance(int iIndicator) {
        GymConfirmFragment fragment = new GymConfirmFragment();
        fragment.flagFragment = iIndicator;
        return fragment;
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
        final String sTop = set.activityName + Constants.LINE_DELIMITER + LABEL_LAST_SET;
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
                        if (!btnExercise.hasFocus() && btnExercise.isFocusable()) btnExercise.requestFocus();
                    }
                    if (set.weightTotal != null &&  set.weightTotal > 0) {
                        if (bUseKg)
                            sTemp2 = String.format(Locale.getDefault(),Constants.SINGLE_FLOAT, set.weightTotal); // + Constants.KG_TAIL;
                        else
                            sTemp2 = Utilities.KgToPoundsDisplay(set.weightTotal) + Constants.LBS_TAIL;
                        btnConfirmWeight.setText(sTemp2);
                    }else
                        if (!btnConfirmWeight.hasFocus() && btnConfirmWeight.isFocusable()) btnConfirmWeight.requestFocus();
                    if (set.repCount == null) set.repCount = userPrefs.getDefaultNewReps();
                    sTemp2 = Integer.toString(set.repCount);
                    btnConfirmReps.setText(sTemp2);
                    if (set.rest_duration != null &&  set.rest_duration > 0){
                        int iWeightRest = userPrefs.getWeightsRestDuration();
                        setDurationRest(iWeightRest);
                    }
                }
                if (nbrOfSets < 2){
                    textViewNextSetsLabel.setVisibility(View.INVISIBLE);
                }else{
                    textViewNextSetsLabel.setVisibility(View.VISIBLE);
                    String sText = LABEL_LAST_SET + currentIndex + LABEL_DIV + nbrOfSets;
                    textViewNextLabel.setText(sText);
                }
                textViewConfirmContinue.setText(nextLabel);

                topView.setText(sTop);
                topView.setTextSize(TypedValue.COMPLEX_UNIT_SP, Utilities.getLabelFontSize(sTop));
            }
        });
    }

    private void setDurationRest(int iRestSeconds){
        com.google.android.material.button.MaterialButton btnRest = mConstraintLayout.findViewById(R.id.btnConfirmRest);
        long milliseconds = (TimeUnit.SECONDS.toMillis(iRestSeconds));
        String sTemp;
        if (mContext == null) mContext = getActivity();
        if (mContext == null) return;
        if (milliseconds == 0)
            btnRest.setText(mContext.getString(R.string.action_untimed_rest));
        else {
            int seconds = (int) (milliseconds / 1000) % 60;
            int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
            int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
            sTemp = mContext.getString(R.string.label_rest_countdown) + Constants.ATRACKIT_SPACE;
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
   final View.OnClickListener myClicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(), 0, null);
        }
    };
    final View.OnLongClickListener myLongClicker = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(), 0, Constants.LABEL_LONG);
            return true;
        }
    };
    final View.OnClickListener finishClicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            WorkoutSet set = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (set == null) return;
            if (set.isValid(true)){
               // textViewConfirmContinue.setVisibility(View.GONE);
              //  textViewNextSetsLabel.setVisibility(View.GONE);
                if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(),set.activityID, set.activityName);
               // getActivity().getSupportFragmentManager().popBackStack();
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
                    if (sMsg.length() > 0) {
                        Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                        msgIntent.putExtra(INTENT_EXTRA_MSG, sMsg);
                        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
                        boolean bDayMode = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_NO);
                        if (bDayMode)
                            msgIntent.putExtra(KEY_FIT_TYPE, 2);
                        else
                            msgIntent.putExtra(KEY_FIT_TYPE, 1);
                        mMessagesViewModel.addLiveIntent(msgIntent);                    }
                }
                if ((set.exerciseName == null) || (set.exerciseName.length() == 0))
                    btnExercise.requestFocus();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessagesViewModel = new ViewModelProvider(requireActivity()).get(MessagesViewModel.class);
        mSavedStateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        WorkoutViewModelFactory factory = com.a_track_it.workout.common.InjectorUtils.getWorkoutViewModelFactory(getContext());
        mSessionViewModel = new ViewModelProvider(requireActivity(), factory).get(WorkoutViewModel.class);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_gym_confirm, container, false);
        if (mContext == null) mContext = getContext();
        appPrefs = ApplicationPreferences.getPreferences(mContext);
        sUserId = appPrefs.getLastUserID();
        userPrefs = UserPreferences.getPreferences(mContext, sUserId);
        mScrollView = rootView.findViewById(R.id.scrollViewConfirm);
        mConstraintLayout = rootView.findViewById(R.id.confirm_constraint);
        if (this.flagFragment > 0){
            final androidx.appcompat.widget.Toolbar toolbar = rootView.findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.confirm_action_prompt);
            if (toolbar != null){
                Drawable drawableUnChecked = AppCompatResources.getDrawable(mContext,R.drawable.ic_close_white);
                Utilities.setColorFilter(drawableUnChecked, ContextCompat.getColor(mContext, R.color.secondaryTextColor));
                toolbar.setNavigationIcon(drawableUnChecked);
            }
            toolbar.setNavigationOnClickListener(v -> mListener.OnFragmentInteraction(android.R.id.home,0,null));
        }

        bRestAutoStart = userPrefs.getRestAutoStart();
        bUseTimedRest = userPrefs.getTimedRest();

        topView = rootView.findViewById(R.id.textViewConfirmName);
        btnBodypart = rootView.findViewById(R.id.btnConfirmBodypart);
        btnBodypart.setTag(Constants.UID_btnBodypart);
        chronometerConfirm = rootView.findViewById(R.id.chronometerConfirm);
        btnConfirmAddExercise = rootView.findViewById(R.id.btnConfirmAddExercise);
        btnConfirmAddExercise.setTag(Constants.UID_btnAddExercise);
        btnExercise = rootView.findViewById(R.id.btnConfirmExercise);
        btnExercise.setTag(Constants.UID_btnExercise);
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
        chronometer = rootView.findViewById(R.id.chronoConfirmCenter);
        recyclerViewItems = rootView.findViewById(R.id.recycleViewConfirmItems);
        final int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        final boolean bDayMode = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_NO);

        Drawable drawableChecked = AppCompatResources.getDrawable(mConstraintLayout.getContext(),R.drawable.ic_outline_check_white);
        if (drawableChecked != null) Utilities.setColorFilter(drawableChecked, ContextCompat.getColor(mConstraintLayout.getContext(), R.color.colorSplash));
        Drawable drawableUnChecked = AppCompatResources.getDrawable(mConstraintLayout.getContext(),R.drawable.ic_close_white);
        if (drawableUnChecked != null) Utilities.setColorFilter(drawableUnChecked,ContextCompat.getColor(mConstraintLayout.getContext(), R.color.primaryLightColor));
        chronometerConfirm.setOnClickListener(myClicker);
        chronometerConfirm.setBase(0);
        topView.setOnClickListener(myClicker);
        btnBodypart.setOnClickListener(myClicker);
        btnBodypart.setOnLongClickListener(myLongClicker);
        btnConfirmAddExercise.setOnClickListener(myClicker);
        btnConfirmAddExercise.setTag(Constants.UID_btnAddExercise);
        btnExercise.setOnClickListener(myClicker);
        btnExercise.setOnLongClickListener(myLongClicker);
        btnMinusWeight.setOnClickListener(myClicker);
        btnPlusWeight.setOnClickListener(myClicker);
        btnMinusReps.setOnClickListener(myClicker);
        btnConfirmReps.setOnClickListener(myClicker);
        btnConfirmReps.setOnLongClickListener(myLongClicker);
        btnPlusReps.setOnClickListener(myClicker);
        btnRest.setOnClickListener(myClicker);
        btnRest.setOnLongClickListener(myLongClicker);
        btnConfirmContinue.setOnClickListener(finishClicker);
        btnConfirmSave.setOnClickListener(v -> {
            mListener.OnFragmentInteraction(Constants.UID_btnSave,0,null);
            final int pos = (chronometer.getVisibility() == Chronometer.VISIBLE) ? chronometer.getTop() : btnConfirmContinue.getTop();
            if (mScrollView != null)
            mScrollView.post(() -> mScrollView.scrollTo(0,pos));
        });
        btnFinish.setOnClickListener(finishClicker);
        btnTopFinish.setOnClickListener(finishClicker);
        btnConfirmWeight.setOnClickListener(myClicker);
        btnConfirmEdit.setOnClickListener(finishClicker);
        btnConfirmContinue.setOnLongClickListener(myLongClicker);
        btnConfirmExerciseRepeat.setOnClickListener(v -> {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(),0,null);
        });
        textViewNextLabel.setOnClickListener(myClicker);
        textViewConfirmContinue.setOnClickListener(myClicker);
        textViewNextSetsLabel.setOnClickListener(myClicker);

        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        ArrayList<Workout> workouts = new ArrayList<>();
        ArrayList<WorkoutSet> sets = new ArrayList<>();
        bUseKg = userPrefs.getUseKG();

        if (mWorkoutSet == null) mWorkoutSet = new WorkoutSet();
        if (mWorkoutSet.activityName.length() > 0) topView.setText(mWorkoutSet.activityName);

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
                ((MaterialButton) v).setEnabled(true);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
                ((MaterialButton) v).setEnabled(false);
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
        AssetManager asm;
        int intBestVPos = 0;
        try {
            asm = getContext().getAssets();
        }catch (NullPointerException ne){
            asm = null;
        }
        if (asm != null) {
            Typeface typeface = Typeface.createFromAsset(
                    asm, Constants.ATRACKIT_FONT);
            if (typeface != null) {
                topView.setTypeface(typeface);
                chronometerConfirm.setTypeface(typeface);
            }
            if (mWorkoutSet.activityName.length() <= 15)
                topView.setTextSize(TypedValue.COMPLEX_UNIT_SP,32);
            else if (mWorkoutSet.activityName.length() <= 35)
                topView.setTextSize(TypedValue.COMPLEX_UNIT_SP,26);
            else
                topView.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        }

        long iconActivityId = (mSavedStateViewModel.getIconID() != null && mSavedStateViewModel.getIconID() > 0) ? mSavedStateViewModel.getIconID() : R.drawable.ic_strong_man_silhouette;
        final int id = Math.toIntExact(iconActivityId);
        int iColor = (mSavedStateViewModel.getColorID() != null && (mSavedStateViewModel.getColorID() > 0)) ?mSavedStateViewModel.getColorID() : R.color.primaryTextColor;
        Resources res = getResources();
        final Drawable vectorDrawable = VectorDrawableCompat.create(res, id, mContext.getTheme());
        //final int idColor = res.getColor(iColor, null);
        ColorStateList colorStateList = ContextCompat.getColorStateList(mContext, iColor);
        topView.setCompoundDrawables(null,null,null,vectorDrawable);
        TextViewCompat.setCompoundDrawableTintList(topView,colorStateList);
       // if (!bUseTimedRest) chronometer.setVisibility(Chronometer.GONE);
        int setIndex = mSavedStateViewModel.getSetIndex();
        long pauseStart = SystemClock.elapsedRealtime();
        //final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-dd HH:mm:ss z", Locale.getDefault());
        final long rest_duration = (mWorkoutSet != null && mWorkoutSet.rest_duration != null) ? mWorkoutSet.rest_duration : userPrefs.getWeightsRestDuration();

        if (bUseTimedRest && (rest_duration > 0)){
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
        }
        String sTemp;
        TextView tvWeight = mConstraintLayout.findViewById(R.id.textViewWeightLabel);
        if (bUseKg)
            sTemp = getString(R.string.label_weight) + Constants.KG_TAIL;
        else
            sTemp = getString(R.string.label_weight) + Constants.LBS_TAIL;

        tvWeight.setText(sTemp);
        if (!bUseKg){
            Drawable lbsWeight = AppCompatResources.getDrawable(mContext, R.drawable.ic_pounds_weight);
            if (lbsWeight != null) {
                if (bDayMode)
                    Utilities.setColorFilter(lbsWeight, ContextCompat.getColor(mContext, R.color.my_app_night_surface_color));
                else
                    Utilities.setColorFilter(lbsWeight, ContextCompat.getColor(mContext, R.color.primaryTextColor));
                tvWeight.setCompoundDrawables(lbsWeight, null, null, null);
            }
        }
        if (mWorkoutSet.weightTotal != null && mWorkoutSet.weightTotal > 0) {
            if (bUseKg)
                sTemp = String.format(Locale.getDefault(), Constants.SINGLE_FLOAT,mWorkoutSet.weightTotal); // + Constants.KG_TAIL;
            else
                sTemp = Utilities.KgToPoundsDisplay(mWorkoutSet.weightTotal) + ATRACKIT_EMPTY; // + Constants.LBS_TAIL;

            btnConfirmWeight.setText(sTemp);
        }else {
            if (mWorkoutSet.resistance_type != null && mWorkoutSet.resistance_type != Field.RESISTANCE_TYPE_BODY)
                btnConfirmWeight.requestFocus();
        }
        sTemp = Integer.toString(mWorkoutSet.repCount);
        btnConfirmReps.setText(sTemp);
      //  if (mSavedStateViewModel.getToDoSetsSize() > 0) sets.addAll(mSavedStateViewModel.getToDoSets().getValue());
        final WorkoutAdapter workoutAdapter = new WorkoutAdapter(mContext, workouts, sets, bUseKg);
        workoutAdapter.setListType(false);

        if (sets.size() > 1){
            if (setIndex < sets.size()) btnConfirmContinue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            else btnConfirmContinue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            workoutAdapter.setSelectedSet(setIndex);
        }
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mLinearLayoutManager = new LinearLayoutManager(mContext);

        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerViewItems.setLayoutManager(mLinearLayoutManager);
        recyclerViewItems.setHasFixedSize(true);
        // To align the edge children (first and last) with the center of the screen
        recyclerViewItems.setAdapter(workoutAdapter);

        final int Vpos = intBestVPos;
        if ((mScrollView != null) && (intBestVPos > 0))
            mScrollView.postDelayed(() -> {
                mScrollView.scrollTo(0,Vpos);
            },2000);
        mSavedStateViewModel.getActiveWorkout().observe(requireActivity(), new Observer<Workout>() {
            @Override
            public void onChanged(Workout workout) {
                if ((workout != null) && (chronometerConfirm.getBase() == 0)){
                    mWorkout = workout;
                    long elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
                    chronometerConfirm.setBase(mWorkout.start - elapsedRealtimeOffset);
                    chronometerConfirm.start();
                }
            }
        });
        mSavedStateViewModel.getActiveWorkoutSet().observe(requireActivity(), set -> {
            observerSet(set);
        });
      //  mSavedStateViewModel.currentSetIndex().observe(requireActivity(), setIndex1 -> workoutAdapter.setSelectedSet(setIndex1));
        mSavedStateViewModel.getToDoSets().observe(getViewLifecycleOwner(), new Observer<List<WorkoutSet>>() {
            @Override
            public void onChanged(List<WorkoutSet> workoutSets) {
                ArrayList<WorkoutSet> sets1 = new ArrayList<>(workoutSets);
                sets1.sort((o1, o2) -> ((o1.setCount < o2.setCount) ? -1: ((o1.setCount > o2.setCount) ? 1 : 0)));
                int iCompletedCount1 = 0; int iToDoCount1 = 0; int nextIndex = 0; int setIndex = 0;
                WorkoutSet setNext = null; WorkoutSet setLast = null;
                for (WorkoutSet s : sets1) {
                    if (s.scoreTotal == FLAG_PENDING) {
                        if (s.end == 0L){
                            if (iToDoCount1 == 0){
                                setNext = s;
                                nextIndex = s.setCount;
                            }
                            iToDoCount1 += 1;
                        }else{
                            setLast = s;
                            setIndex = s.setCount;
                            iCompletedCount1 += 1;
                        }
                    }
                }
                if (setIndex == 0){ setIndex = 1; setLast = sets1.get(0);}
                int setCount = workoutSets.size();
                final int iCompletedCount = iCompletedCount1;
                final int iToDoCount = iToDoCount1;
                final int mySetIndex =  setIndex;
                final String sTextNext = LABEL_LAST_SET + mySetIndex + LABEL_DIV + setCount;

                String sText;
                if (mySetIndex < setCount) {
                    if ((setNext.exerciseName != null) && (setNext.exerciseName.length() > 0)) {
                        sText = LABEL_NEXT_SET + Constants.LINE_DELIMITER + (mySetIndex +1) + LABEL_DIV + setCount + ATRACKIT_SPACE + setNext.exerciseName;
                    } else sText = LABEL_NEXT_SET + Constants.LINE_DELIMITER + (mySetIndex +1) + LABEL_DIV + setCount;
                } else {
                    if (setCount > 0)
                        sText = LABEL_NEXT_SET + (mySetIndex +1);
                    else
                        sText = ATRACKIT_EMPTY;
                }
                final String sTextContinue = sText;
                sText = ATRACKIT_EMPTY;
                if (iCompletedCount > 0){
                    if (iToDoCount > 0) sText = LABEL_COMPLETED_SET + iCompletedCount + LABEL_DIV + setCount; else sText = LABEL_COMPLETED_SET + iCompletedCount;
                }else
                    sText = iToDoCount + getString(R.string.label_set).toLowerCase();

                String sTextNextSets = sText;
                new Handler(Looper.myLooper()).post(() -> {
                    workoutAdapter.setWorkoutSetArrayList(sets1);
                    try {
                        if (setCount < 2){
                            textViewNextSetsLabel.setVisibility(View.INVISIBLE);
                            recyclerViewItems.setVisibility(View.GONE);
                        }else{
                            textViewNextSetsLabel.setVisibility(View.VISIBLE);
                            recyclerViewItems.setVisibility(View.VISIBLE);
                        }
                        textViewNextLabel.setText(sTextNext);
                        textViewConfirmContinue.setText(sTextContinue);
                        textViewNextSetsLabel.setText(sTextNextSets);

                    }catch (Exception ne){
                        String sMsg = ne.getMessage();
                        if (sMsg != null)
                            Log.e(LOG_TAG, sMsg);
                    }
                });
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInterface) {
            mListener = (FragmentInterface) context;
            mContext = context.getApplicationContext();
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
}
