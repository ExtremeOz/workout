package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.data_model.Exercise;
import com.a_track_it.fitdata.user_model.Utilities;
import com.a_track_it.fitdata.data_model.Workout;
import com.a_track_it.fitdata.data_model.WorkoutSet;
import com.a_track_it.fitdata.user_model.MessagesViewModel;
import com.a_track_it.fitdata.model.SavedStateViewModel;
import com.a_track_it.fitdata.model.SessionViewModel;
import com.a_track_it.fitdata.user_model.UserPreferences;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnEndOfSetInteraction} interface
 * to handle interaction events.
 * Use the {@link EndOfSetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EndOfSetFragment extends Fragment {
    public static final String TAG = EndOfSetFragment.class.getSimpleName();
    public static final String ARG_ICON_ID = "ICON_ID";
    public static final String ARG_COLOR_ID = "COLOR_ID";
    public static final String ARG_ACTIVITY_ID = "ACTIVITY_ID";
    public static final String ARG_TIMED = "TIMED_ID";
    public static final String ARG_SET_INDEX = "SET_INDEX";
    public static final String ARG_SET_COUNT = "SET_COUNT";
    private int mIcon;
    private int mColor;
    private int mActivityID;
    private WorkoutSet mWorkoutSet;
    private Workout mWorkout;
    private int mCurrentSet;
    private int mSetCount;
    private long restDuration;

    private boolean mIsGym = false;
    private boolean mIsArchery = false;
    private ArrayList<Exercise> exercises;
    private OnEndOfSetInteraction mListener;
    private MessagesViewModel messagesViewModel;
    private SavedStateViewModel savedStateViewModel;
    private SessionViewModel sessionViewModel;
    private LinearLayout mLinear;
    private TextView textView_message1;
    private TextView textView_message2;
    private TextView textView_rest_duration;

    private int mActiveForegroundColor;
    private int mActiveBackgroundColor;
    private List<String> mScoreCard = new ArrayList<>();
    private List<String> mEndScores = new ArrayList<>();

    public EndOfSetFragment() {
        // Required empty public constructor
    }

    public static EndOfSetFragment newInstance(int icon, int color, int activityID, long rest, int setIndex, int setCount) {
        final EndOfSetFragment fragment = new EndOfSetFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ICON_ID, icon);
        args.putInt(ARG_COLOR_ID, color);
        args.putInt(ARG_ACTIVITY_ID, activityID);
        args.putLong(ARG_TIMED, rest);
        args.putInt(ARG_SET_INDEX, setIndex);
        args.putInt(ARG_SET_COUNT, setCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            mIcon = bundle.getInt(ARG_ICON_ID);
            mColor = bundle.getInt(ARG_COLOR_ID);
            mActivityID = bundle.getInt(ARG_ACTIVITY_ID);
            mCurrentSet = bundle.getInt(ARG_SET_INDEX);
            mSetCount = bundle.getInt(ARG_SET_COUNT);
            mIsGym = Utilities.isGymWorkout(mActivityID);
            mIsArchery = Utilities.isShooting(mActivityID);

            restDuration = bundle.getLong(ARG_TIMED);
            try{
                messagesViewModel = ViewModelProviders.of(requireActivity()).get(MessagesViewModel.class);
                savedStateViewModel = ViewModelProviders.of(requireActivity()).get(SavedStateViewModel.class);
                sessionViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
            }catch (IllegalStateException ise){
                Log.e(TAG, "illegal state on viewmodels " + ise.getLocalizedMessage());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView; String sTemp; String sScore;
        final String defaultValue = "";
        // Inflate the layout for this fragment

        if (mIsGym){
            rootView = inflater.inflate(R.layout.fragment_gym_confirm, container, false);
            mLinear = rootView.findViewById(R.id.linear_gym_confirm);
            final com.google.android.material.button.MaterialButton exercise_btn = rootView.findViewById(R.id.exercise_confirm_button);
            exercise_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onEndOfSetRequest(Constants.CHOOSE_EXERCISE, mCurrentSet, defaultValue);
                }
            });
            final com.google.android.material.button.MaterialButton reps_btn = rootView.findViewById(R.id.reps_confirm_button);
            reps_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onEndOfSetRequest(Constants.CHOOSE_REPS, mCurrentSet, defaultValue);
                }
            });
            final com.google.android.material.button.MaterialButton weight_btn = rootView.findViewById(R.id.weight_confirm_button);
            weight_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onEndOfSetRequest(Constants.CHOOSE_WEIGHT, mCurrentSet, defaultValue);
                }
            });

            final com.google.android.material.button.MaterialButton next_btn = rootView.findViewById(R.id.gym_confirm_next_set_button);
            next_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onEndOfSetMethod(Constants.CHOOSE_CONTINUE_SESSION,mCurrentSet);
                }
            });
            textView_rest_duration = rootView.findViewById(R.id.gym_rest_duration_text);

            final com.google.android.material.button.MaterialButton repeat_btn = rootView.findViewById(R.id.set_repeat_button);
            repeat_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEndOfSetMethod(Constants.CHOOSE_REPEAT_SET, mCurrentSet);
                }
            });
            final com.google.android.material.button.MaterialButton add_btn = rootView.findViewById(R.id.set_add_button);
            add_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEndOfSetMethod(Constants.CHOOSE_ADD_SET, mCurrentSet);
                }
            });
            final com.google.android.material.button.MaterialButton next_exercise = rootView.findViewById(R.id.exercise_next_button);
            next_exercise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEndOfSetRequest(Constants.SELECTION_EXERCISE, mCurrentSet+1,defaultValue);
                }
            });
            final com.google.android.material.button.MaterialButton next_weight = rootView.findViewById(R.id.weight_next_button);
            next_weight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEndOfSetRequest(Constants.SELECTION_WEIGHT_KG, mCurrentSet+1,defaultValue);
                }
            });
            final com.google.android.material.button.MaterialButton next_reps = rootView.findViewById(R.id.reps_next_button);
            next_reps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEndOfSetRequest(Constants.SELECTION_REPS, mCurrentSet+1,defaultValue);
                }
            });
            final com.google.android.material.button.MaterialButton confirm_next = rootView.findViewById(R.id.gym_confirm_next_set_button);
            confirm_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEndOfSetMethod(Constants.CHOOSE_CONTINUE_SESSION, mCurrentSet+1);
                }
            });
            final com.google.android.material.button.MaterialButton exit_btn  = rootView.findViewById(R.id.gym_confirm_start_confirm_button);
            exit_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onEndOfSetExit();
                }
            });

            textView_message1 = rootView.findViewById(R.id.gym_text_message);
            textView_message2 = rootView.findViewById(R.id.gym_next_set_text);

            textView_rest_duration = rootView.findViewById(R.id.gym_rest_duration_text);

        }else {
            if (mIsArchery){
                rootView = inflater.inflate(R.layout.fragment_archery_confirm, container, false);
                mLinear = rootView.findViewById(R.id.linear_archery_confirm);
                final ImageButton one_btn = rootView.findViewById(R.id.arrow1_button);
                one_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAlertDialogScores(view, 1);
                    }
                });
                final ImageButton two_btn = rootView.findViewById(R.id.arrow2_button);
                two_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAlertDialogScores(view, 2);
                    }
                });
                final ImageButton three_btn = rootView.findViewById(R.id.arrow3_button);
                three_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAlertDialogScores(view, 3);
                    }
                });
                final ImageButton four_btn = rootView.findViewById(R.id.arrow4_button);
                four_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAlertDialogScores(view, 4);
                    }
                });
                final ImageButton five_btn = rootView.findViewById(R.id.arrow5_button);
                five_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAlertDialogScores(view, 5);
                    }
                });
                final ImageButton six_btn = rootView.findViewById(R.id.arrow6_button);
                six_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAlertDialogScores(view, 6);
                    }
                });
                final ImageButton seven_btn = rootView.findViewById(R.id.arrow7_button);
                seven_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAlertDialogScores(view, 7);
                    }
                });
                final ImageButton eight_btn = rootView.findViewById(R.id.arrow8_button);
                eight_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAlertDialogScores(view, 8);
                    }
                });
                final ImageButton nine_btn = rootView.findViewById(R.id.arrow9_button);
                nine_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAlertDialogScores(view, 9);
                    }
                });
                final ImageButton ten_btn = rootView.findViewById(R.id.arrow10_button);
                ten_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAlertDialogScores(view, 10);
                    }
                });
                final com.google.android.material.button.MaterialButton exit_btn  = rootView.findViewById(R.id.archery_confirm_exit_button);
                exit_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onEndOfSetExit();
                    }
                });
                final com.google.android.material.button.MaterialButton next_btn = rootView.findViewById(R.id.archery_confirm_next_end_button);
                next_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onEndOfSetMethod(Constants.CHOOSE_CONTINUE_SESSION, mWorkoutSet.setCount);
                    }
                });

                textView_message1 = rootView.findViewById(R.id.archery_msg1_text);
                textView_message2 = rootView.findViewById(R.id.archery_msg2_text);

            }else {
                rootView = inflater.inflate(R.layout.fragment_session_finalise, container, false);
                mLinear = rootView.findViewById(R.id.linear_session_confirm);
                final com.google.android.material.button.MaterialButton exit_btn  = rootView.findViewById(R.id.confirm_finish_confirm_button);
                exit_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onEndOfSetExit();
                    }
                });
                final com.google.android.material.button.MaterialButton next_btn = rootView.findViewById(R.id.confirm_add_confirm_button);
                next_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.onEndOfSetMethod(Constants.CHOOSE_CONTINUE_SESSION, mWorkoutSet.setCount);
                    }
                });
                textView_message1 = rootView.findViewById(R.id.confirm_message1);
                textView_message2 = rootView.findViewById(R.id.confirm_message2);

                textView_rest_duration = rootView.findViewById(R.id.confirm_rest_duration_text);

            }
        }
        savedStateViewModel.getActiveWorkout().observe(this, new Observer<Workout>() {
            @Override
            public void onChanged(Workout workout) {
                Log.d(TAG, "getWorkout onChanged updateUI");
                updateUI();
            }
        });

        savedStateViewModel.getActiveWorkoutSet().observe(this, new Observer<WorkoutSet>() {
            @Override
            public void onChanged(WorkoutSet workoutSet) {
                Log.d(TAG, "getWorkoutSet onChanged updateUI");
                updateUI();
            }
        });

        sessionViewModel.getToDoSets().observe(this, new Observer<ArrayList<WorkoutSet>>() {
            @Override
            public void onChanged(ArrayList<WorkoutSet> workoutSets) {
                updateUI();
            }
        });

        // observe the live data changes
        if (textView_message1 != null)
            messagesViewModel.CurrentMessage().observe(this, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    textView_message1.setText(s);
                }
            });
        if (textView_message2 != null)
            messagesViewModel.OtherMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView_message2.setText(s);
            }
        });
        if (textView_rest_duration != null)
            messagesViewModel.getRestMsg().observe(this, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    textView_rest_duration.setText(s);
                }
            });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWorkout = savedStateViewModel.getActiveWorkout().getValue();
        mIsGym = savedStateViewModel.getIsGym().getValue();
        mIsArchery = savedStateViewModel.getIsShoot().getValue();

        mWorkoutSet = savedStateViewModel.getActiveWorkoutSet().getValue();
        Integer i = savedStateViewModel.getSetIndex().getValue();
        if (i.intValue() != mCurrentSet){
            Log.e(TAG,"current set discrepancy " + Integer.toString(i) + " current " + Integer.toString(mCurrentSet));
            mCurrentSet = i.intValue();
        }

        if (mWorkout != null) {
            if (mIsArchery){
                if ((mWorkoutSet.score_card.length() > 0)) {
                    String itemsEnds[] = mWorkoutSet.score_card.split(",");
                    mEndScores = new ArrayList<>(Arrays.asList(itemsEnds));
                }
                if (mWorkout.score_card.length() > 0) {
                    String items[] = mWorkout.score_card.split(",");
                    mScoreCard = new ArrayList<>(Arrays.asList(items));
                }
                String sTemp = Integer.toString(mWorkoutSet.setCount) + "/" + Integer.toString(mWorkout.setCount) + " ends";
                messagesViewModel.addMessage(sTemp);

            }
            if (mIsGym){
                String sTemp = Integer.toString(mWorkoutSet.setCount) + "/" + Integer.toString(mWorkout.setCount) + " sets";
                messagesViewModel.addMessage(sTemp);
            }
            mSetCount = mWorkout.setCount;
            String sMsg = Utilities.getTimeString(mWorkout.start) + " to " + Utilities.getTimeString(mWorkout.end);
            messagesViewModel.addMessage(sMsg);
        }else
            Log.e(TAG, "NULL workout");



    }
    public void updateUI(){
        View rootView = mLinear.getRootView() ; String sTemp;
        Context context = getContext();
        ArrayList<WorkoutSet> todo = sessionViewModel.getToDoSets().getValue();
        mWorkout = savedStateViewModel.getActiveWorkout().getValue();
        mWorkoutSet = savedStateViewModel.getActiveWorkoutSet().getValue();
        mSetCount = (todo != null) ? todo.size() : 0;

        mIsGym = savedStateViewModel.getIsGym().getValue();
        mIsArchery = savedStateViewModel.getIsShoot().getValue();
        if (mIsGym){
            int nextIndex = mCurrentSet + 1;
            // check if we have another set to go... update
            if (nextIndex <= mSetCount){
                if ((todo != null) && (nextIndex < todo.size())){
                    WorkoutSet nextSet = todo.get(nextIndex-1);

                    com.google.android.material.button.MaterialButton next_exercise = rootView.findViewById(R.id.exercise_next_button);
                    if (nextSet.exerciseID == 0)
                        sTemp = context.getString(R.string.default_select) + " " + context.getString(R.string.label_exercise);
                    else
                        sTemp = nextSet.exerciseName;
                    next_exercise.setText(sTemp);
                    com.google.android.material.button.MaterialButton next_weight = rootView.findViewById(R.id.weight_next_button);
                    if (nextSet.weightTotal == 0) {
                        sTemp = context.getString(R.string.default_select) + " " + context.getString(R.string.label_weight);
                    }else {
                        if (UserPreferences.getUseKG(context))
                            sTemp = Float.toString(nextSet.weightTotal) + " " + context.getString(R.string.label_weight_units_kg);
                        else
                            sTemp = Float.toString(nextSet.weightTotal) + " " + context.getString(R.string.label_weight_units_lbs);
                    }
                    next_weight.setText(sTemp);
                    com.google.android.material.button.MaterialButton next_reps = rootView.findViewById(R.id.reps_confirm_button);
                    if (nextSet.repCount == 0)
                        sTemp = context.getString(R.string.default_select) + " " + context.getString(R.string.label_rep);
                    else
                        sTemp = Integer.toString(nextSet.repCount) + " " + context.getString(R.string.label_rep);
                    next_reps.setText(sTemp);
                    rootView.findViewById(R.id.gym_confirm_row2_container).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.gym_confirm_row6_container).setVisibility(View.VISIBLE); // continue is allowed
                }else{
                    // no next set available.
                    rootView.findViewById(R.id.gym_confirm_row2_container).setVisibility(View.GONE); // no exercise info
                    rootView.findViewById(R.id.gym_confirm_row6_container).setVisibility(View.GONE); // no continue button avail
                }

            }else{
                // no next set available.
                rootView.findViewById(R.id.gym_confirm_row2_container).setVisibility(View.GONE); // no exercise info
                rootView.findViewById(R.id.gym_confirm_row6_container).setVisibility(View.GONE); // no continue button avail
            }

            com.google.android.material.button.MaterialButton exercise_btn = rootView.findViewById(R.id.exercise_confirm_button);
            if (mWorkoutSet.exerciseID == 0)
                sTemp = context.getString(R.string.default_select) + " " + context.getString(R.string.label_exercise);
            else
                sTemp = mWorkoutSet.exerciseName;
            exercise_btn.setText(sTemp);
            com.google.android.material.button.MaterialButton reps_btn = rootView.findViewById(R.id.reps_confirm_button);
            if (mWorkoutSet.repCount == 0)
                sTemp = context.getString(R.string.default_select) + " " + context.getString(R.string.label_rep);
            else
                sTemp = Integer.toString(mWorkoutSet.repCount) + " " + context.getString(R.string.label_rep);
            reps_btn.setText(sTemp);
            com.google.android.material.button.MaterialButton weight_btn = rootView.findViewById(R.id.weight_confirm_button);
            if (mWorkoutSet.weightTotal == 0) {
                sTemp = context.getString(R.string.default_select) + " " + context.getString(R.string.label_weight);
            }else {
                if (UserPreferences.getUseKG(context))
                    sTemp = Float.toString(mWorkoutSet.weightTotal) + " " + context.getString(R.string.label_weight_units_kg);
                else
                    sTemp = Float.toString(mWorkoutSet.weightTotal) + " " + context.getString(R.string.label_weight_units_lbs);
            }
            weight_btn.setText(sTemp);

            sTemp = Integer.toString(mWorkoutSet.setCount) + "/" + Integer.toString(mWorkout.setCount) + " sets";
            messagesViewModel.addOtherMessage(sTemp);
        }
        if (mIsArchery){
            ImageButton one_btn = rootView.findViewById(R.id.arrow1_button);
            one_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowAlertDialogScores(view, 1);
                }
            });
            one_btn.setEnabled(mWorkout.repCount >= 1);
            if ((mEndScores.size() >= 1) && (mEndScores.get(0).length() > 0)){
                sTemp = "ic_score_" + mEndScores.get(0).toLowerCase();
                int ic_resource_id = getResources().getIdentifier(sTemp,"drawable",getActivity().getPackageName());
                if ((one_btn != null) && (ic_resource_id > 0)){
                    one_btn.setImageResource(ic_resource_id);
                }
            }
            ImageButton two_btn = rootView.findViewById(R.id.arrow2_button);
            two_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowAlertDialogScores(view, 2);
                }
            });
            two_btn.setEnabled(mWorkout.repCount >= 2);
            if ((mEndScores.size() >= 2) && (mEndScores.get(1).length() > 0)){
                sTemp = "ic_score_" + mEndScores.get(1).toLowerCase();
                int ic_resource_id = getResources().getIdentifier(sTemp,"drawable",getActivity().getPackageName());
                if ((two_btn != null) && (ic_resource_id > 0)){
                    two_btn.setImageResource(ic_resource_id);
                }
            }
            ImageButton three_btn = rootView.findViewById(R.id.arrow3_button);
            three_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowAlertDialogScores(view, 3);
                }
            });
            three_btn.setEnabled(mWorkout.repCount >= 3);
            if ((mEndScores.size() >= 3) && (mEndScores.get(2).length() > 0)){
                sTemp = "ic_score_" + mEndScores.get(2).toLowerCase();
                int ic_resource_id = getResources().getIdentifier(sTemp,"drawable",getActivity().getPackageName());
                if ((three_btn != null) && (ic_resource_id > 0)){
                    three_btn.setImageResource(ic_resource_id);
                }
            }
            ImageButton four_btn = rootView.findViewById(R.id.arrow4_button);
            four_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowAlertDialogScores(view, 4);
                }
            });
            four_btn.setEnabled(mWorkout.repCount >= 4);
            if ((mEndScores.size() >= 4) && (mEndScores.get(3).length() > 0)){
                sTemp = "ic_score_" + mEndScores.get(3).toLowerCase();
                int ic_resource_id = getResources().getIdentifier(sTemp,"drawable",getActivity().getPackageName());
                if ((four_btn != null) && (ic_resource_id > 0)){
                    four_btn.setImageResource(ic_resource_id);
                }
            }
            ImageButton five_btn = rootView.findViewById(R.id.arrow5_button);
            five_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowAlertDialogScores(view, 5);
                }
            });
            five_btn.setEnabled(mWorkout.repCount >= 5);
            if ((mEndScores.size() >= 5) && (mEndScores.get(4).length() > 0)){
                sTemp = "ic_score_" + mEndScores.get(4).toLowerCase();
                int ic_resource_id = getResources().getIdentifier(sTemp,"drawable",getActivity().getPackageName());
                if ((five_btn != null) && (ic_resource_id > 0)){
                    five_btn.setImageResource(ic_resource_id);
                }
            }
            ImageButton six_btn = rootView.findViewById(R.id.arrow6_button);
            six_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowAlertDialogScores(view, 6);
                }
            });
            six_btn.setEnabled(mWorkout.repCount >= 6);
            if ((mEndScores.size() >= 6) && (mEndScores.get(5).length() > 0)){
                sTemp = "ic_score_" + mEndScores.get(5).toLowerCase();
                int ic_resource_id = getResources().getIdentifier(sTemp,"drawable",getActivity().getPackageName());
                if ((six_btn != null) && (ic_resource_id > 0)){
                    six_btn.setImageResource(ic_resource_id);
                }
            }
            ImageButton seven_btn = rootView.findViewById(R.id.arrow7_button);
            seven_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowAlertDialogScores(view, 7);
                }
            });
            seven_btn.setEnabled(mWorkout.repCount >= 7);
            if ((mEndScores.size() >= 7) && (mEndScores.get(6).length() > 0)){
                sTemp = "ic_score_" + mEndScores.get(6).toLowerCase();
                int ic_resource_id = getResources().getIdentifier(sTemp,"drawable",getActivity().getPackageName());
                if ((seven_btn != null) && (ic_resource_id > 0)){
                    seven_btn.setImageResource(ic_resource_id);
                }
            }
            ImageButton eight_btn = rootView.findViewById(R.id.arrow8_button);
            eight_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowAlertDialogScores(view, 8);
                }
            });
            eight_btn.setEnabled(mWorkout.repCount >= 8);
            if ((mEndScores.size() >= 8) && (mEndScores.get(7).length() > 0)){
                sTemp = "ic_score_" + mEndScores.get(7).toLowerCase();
                int ic_resource_id = getResources().getIdentifier(sTemp,"drawable",getActivity().getPackageName());
                if ((eight_btn != null) && (ic_resource_id > 0)){
                    eight_btn.setImageResource(ic_resource_id);
                }
            }
            ImageButton nine_btn = rootView.findViewById(R.id.arrow9_button);
            nine_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowAlertDialogScores(view, 9);
                }
            });
            nine_btn.setEnabled(mWorkout.repCount >= 9);
            if ((mEndScores.size() >= 9) && (mEndScores.get(8).length() > 0)){
                sTemp = "ic_score_" + mEndScores.get(8).toLowerCase();
                int ic_resource_id = getResources().getIdentifier(sTemp,"drawable",getActivity().getPackageName());
                if ((nine_btn != null) && (ic_resource_id > 0)){
                    nine_btn.setImageResource(ic_resource_id);
                }
            }
            ImageButton ten_btn = rootView.findViewById(R.id.arrow10_button);
            ten_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowAlertDialogScores(view, 10);
                }
            });
            ten_btn.setEnabled(mWorkout.repCount >= 10);
            if ((mEndScores.size() >= 10) && (mEndScores.get(9).length() > 0)){
                sTemp = "ic_score_" + mEndScores.get(9).toLowerCase();
                int ic_resource_id = getResources().getIdentifier(sTemp,"drawable",getActivity().getPackageName());
                if ((ten_btn != null) && (ic_resource_id > 0)){
                    ten_btn.setImageResource(ic_resource_id);
                }
            }
            ViewGroup.LayoutParams params;
            LinearLayout linearLayout;
            if (mWorkout.repCount < 4){
                rootView.findViewById(R.id.archery_confirm_row3_container).setVisibility(View.GONE);
                rootView.findViewById(R.id.archery_confirm_row4_container).setVisibility(View.GONE);
                linearLayout = rootView.findViewById(R.id.archery_confirm_row2_container);
                if (linearLayout.getTag() == null) {
                    params = linearLayout.getLayoutParams();
                    params.height = params.height * 2;
                    linearLayout.setLayoutParams(params);
                    linearLayout.setTag(new Integer(1));
                }
                if (mWorkout.repCount <= 2)
                    rootView.findViewById(R.id.arrow3_button).setVisibility(View.GONE);
                if (mWorkout.repCount == 1)
                    rootView.findViewById(R.id.arrow2_button).setVisibility(View.GONE);
            }else
            if (mWorkout.repCount < 8){
                rootView.findViewById(R.id.archery_confirm_row4_container).setVisibility(View.GONE);
                linearLayout = rootView.findViewById(R.id.archery_confirm_row2_container);
                if (linearLayout.getTag() == null) {
                    params = linearLayout.getLayoutParams();
                    int existing = params.height;
                    params.height = params.height + (int) (existing / 2);
                    linearLayout.setLayoutParams(params);
                    linearLayout.setTag(new Integer(1));
                    linearLayout = rootView.findViewById(R.id.archery_confirm_row3_container);
                    linearLayout.setLayoutParams(params);
                }
                if (mWorkout.repCount <= 6) rootView.findViewById(R.id.arrow7_button).setVisibility(View.GONE);
                if (mWorkout.repCount <= 5) rootView.findViewById(R.id.arrow6_button).setVisibility(View.GONE);
                if (mWorkout.repCount <= 4) rootView.findViewById(R.id.arrow5_button).setVisibility(View.GONE);
            }else{
                if (mWorkout.repCount <= 9) rootView.findViewById(R.id.arrow10_button).setVisibility(View.GONE);
                if (mWorkout.repCount == 8) rootView.findViewById(R.id.arrow6_button).setVisibility(View.GONE);
            }
            if (mWorkout.repCount <= 9) ten_btn.setEnabled(false);
            if (mWorkout.repCount <= 8) nine_btn.setEnabled(false);
            if (mWorkout.repCount <= 7) eight_btn.setEnabled(false);
            if (mWorkout.repCount <= 6) seven_btn.setEnabled(false);
            if (mWorkout.repCount <= 5) six_btn.setEnabled(false);
            if (mWorkout.repCount <= 4) five_btn.setEnabled(false);
            if (mWorkout.repCount <= 3) four_btn.setEnabled(false);
            if (mWorkout.repCount <= 2) three_btn.setEnabled(false);
            if (mWorkout.repCount == 1) two_btn.setEnabled(false);
        }

    }
    private void ShowAlertDialogExerciseList(final View parent_view){
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(parent_view.getContext(), R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        String[] items = new String[exercises.size()]; int i = 0;
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(parent_view.getContext(), R.layout.item_alertlist);
        int selectedPos = -1;

        for (Exercise e : exercises){
            items[i] = e.name;
            if (mWorkoutSet.exerciseID == (int)e._id) selectedPos = i;
            arrayAdapter.add(e.name);
            i++;
        }
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_customlist_alert, null);
        dialogBuilder.setView(customView);
        ListView lv = (ListView) customView.findViewById(R.id.list_view);
        lv.setAdapter(arrayAdapter);
        if (selectedPos > -1) lv.setSelection(selectedPos);
        final AlertDialog dialog = dialogBuilder.create();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Exercise e = exercises.get(i);
                mWorkoutSet.exerciseID = (int) e._id;
                mWorkoutSet.exerciseName = e.name;
                Log.i(TAG, "selected " + e.name);
                com.google.android.material.button.MaterialButton ex = parent_view.findViewById(R.id.exercise_confirm_button);
                if(ex != null) ex.setText(e.name);
                dialog.dismiss();
            }
        });
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle("         Select Exercise");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void ShowAlertDialogRepsList(final View parent_view){
        //AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(),  R.style.AppTheme_myAlertDialog);
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(parent_view.getContext(), R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(parent_view.getContext(), R.layout.item_alertlist);
        int selectedPos = -1;
        for (int i = 0; i < 50 ; i++){
            if (mWorkoutSet.repCount == i) selectedPos = i;
            arrayAdapter.add(Integer.toString(i));
        }
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_customlist_alert, null);
        dialogBuilder.setView(customView);
        ListView lv = (ListView) customView.findViewById(R.id.list_view);
        lv.setAdapter(arrayAdapter);
        if (selectedPos > -1) lv.setSelection(selectedPos);
        dialogBuilder.setTitle("         Select Reps");
        final AlertDialog dialog = dialogBuilder.create();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mWorkoutSet.repCount = i;
                com.google.android.material.button.MaterialButton repBtn = parent_view.findViewById(R.id.reps_confirm_button);
                if (repBtn != null) repBtn.setText(Integer.toString(mWorkoutSet.repCount) + " reps");
                dialog.dismiss();
            }
        });
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setCancelable(true);
        dialog.show();
    }


    private void ShowAlertDialogScores(final View parent_view, final int shot_number){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(),  R.style.AppTheme_myAlertDialog);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(parent_view.getContext(), R.layout.item_alertlist); // 
        arrayAdapter.add("M");
        for (int i = 1; i <= 10 ; i++){
            arrayAdapter.add(Integer.toString(i));
        }
        arrayAdapter.add("X");
        //TODO: preselect an existing score
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_customlist_alert, null);
        dialogBuilder.setView(customView);
        ListView lv = (ListView) customView.findViewById(R.id.list_view);
        lv.setAdapter(arrayAdapter);

        dialogBuilder.setTitle("         Select Score");
        final AlertDialog dialog = dialogBuilder.create();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "adapterView click i " + Integer.toString(i) + " l " + Long.toString(l));
                mWorkoutSet.repCount = (int)l;  // l is the row is zero based - shots start at 1
                int btn_id = 0;
                switch (shot_number){
                    case 1:
                        btn_id = R.id.arrow1_button;
                        break;
                    case 2:
                        btn_id = R.id.arrow2_button;
                        break;
                    case 3:
                        btn_id = R.id.arrow3_button;
                        break;
                    case 4:
                        btn_id = R.id.arrow4_button;
                        break;
                    case 5:
                        btn_id = R.id.arrow5_button;
                        break;
                    case 6:
                        btn_id = R.id.arrow6_button;
                        break;
                    case 7:
                        btn_id = R.id.arrow7_button;
                        break;
                    case 8:
                        btn_id = R.id.arrow8_button;
                        break;
                    case 9:
                        btn_id = R.id.arrow9_button;
                        break;
                    case 10:
                        btn_id = R.id.arrow10_button;
                        break;
                }
                if (btn_id > 0) {
                    ImageButton repBtn = parent_view.findViewById(btn_id);
                    String ic_score_id;
                    if (i == 11)
                        ic_score_id = "ic_score_x";
                    else
                        ic_score_id = "ic_score_" + Long.toString(i);
                    Log.d(TAG, "adapterView click id " + ic_score_id);
                    int ic_resource_id = getResources().getIdentifier(ic_score_id,"drawable",getActivity().getPackageName());
                    if ((repBtn != null) && (ic_resource_id > 0)){
                        repBtn.setImageResource(ic_resource_id);
                    }
                }
                savedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                dialog.dismiss();
            }
        });
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setCancelable(true);
        dialog.show();
    }
    private void ShowAlertDialogWeightsList(final View parent_view){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(),  R.style.AppTheme_myAlertDialog);
        String[] items; final boolean bUseKG = UserPreferences.getUseKG(getContext());

        if (bUseKG) {
            items = getContext().getResources().getStringArray(R.array.array_weights_kg);
            dialogBuilder.setTitle("         Select kg");
        }else {
            items = getContext().getResources().getStringArray(R.array.array_weights_lbs);
            dialogBuilder.setTitle("         Select lbs");
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),R.layout.item_alertlist);
        int iPos = -1;
        int iSelectedPos = iPos;
        for (String weight : items){
            iPos++;
            if (mWorkoutSet.weightTotal == Float.parseFloat(weight)) iSelectedPos = iPos;
            arrayAdapter.add(weight);
        }
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_customlist_alert, null);
        dialogBuilder.setView(customView);
        ListView lv = (ListView) customView.findViewById(R.id.list_view);
        lv.setAdapter(arrayAdapter);
        if (iSelectedPos > -1) lv.setSelection(iSelectedPos);
        final AlertDialog dialog = dialogBuilder.create();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String sTemp;
                if (bUseKG) {
                    String[] weights = getContext().getResources().getStringArray(R.array.array_weights_kg);
                    mWorkoutSet.weightTotal = Float.parseFloat(weights[i]);
                    Log.i(TAG, "weight selected" + weights[i]);
                    sTemp = weights[i] + " " + getContext().getString(R.string.label_weight_units_kg);
                }else {
                    String[] weights = getContext().getResources().getStringArray(R.array.array_weights_kg);
                    mWorkoutSet.weightTotal = Float.parseFloat(weights[i]);
                    Log.i(TAG, "weight selected" + weights[i]);
                    sTemp = weights[i] + " " + getContext().getString(R.string.label_weight_units_lbs);
                }

                com.google.android.material.button.MaterialButton weightBtn = parent_view.findViewById(R.id.weight_confirm_button);
                if (weightBtn != null) weightBtn.setText(sTemp);
                dialog.dismiss();
            }
        });
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEndOfSetInteraction) {
            mListener = (OnEndOfSetInteraction) context;
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

    public interface OnEndOfSetInteraction {
        void onEndOfSetRequest(int index, int setIndex, String defaultValue);
        void onEndOfSetMethod(int index, int setIndex);
        void onEndOfSetExit();
    }

    public void setExerciseList(ArrayList<Exercise> exList){
        exercises = exList;
    }

    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        Log.d(TAG, "EndOfSetFragment.onEnterAmbient() " + ambientDetails);

        // Convert image to grayscale for ambient mode.
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        //mWearableRecyclerView.setColorFilter(filter);
        if (textView_message1 != null) {
            mActiveForegroundColor = textView_message1.getPaint().getColor();
            textView_message1.getPaint().setAntiAlias(false);
            if (textView_message2 != null) textView_message2.getPaint().setAntiAlias(false);
            if (textView_rest_duration != null) textView_rest_duration.getPaint().setAntiAlias(false);
        }

    }

    /** Restores the UI to active (non-ambient) mode. */
    public void onExitAmbientInFragment() {
        Log.d(TAG, "EndOfSetFragment.onExitAmbient()");
        if (textView_message1 != null) {
            textView_message1.setTextColor(mActiveForegroundColor);
            textView_message1.getPaint().setAntiAlias(true);
        }

        if (textView_message2 != null) textView_message2.setTextColor(mActiveForegroundColor);
        if (textView_message2 != null) textView_message2.getPaint().setAntiAlias(true);
        if (textView_rest_duration != null) textView_rest_duration.setTextColor(mActiveForegroundColor);
        if (textView_rest_duration != null) textView_rest_duration.getPaint().setAntiAlias(true);
        //mImageView.setColorFilter(mImageViewColorFilter);
    }
}
