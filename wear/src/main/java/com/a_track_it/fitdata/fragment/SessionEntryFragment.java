package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.wear.ambient.AmbientModeSupport;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.model.Utilities;
import com.a_track_it.fitdata.common.model.Workout;
import com.a_track_it.fitdata.common.model.WorkoutSet;
import com.a_track_it.fitdata.model.MessagesViewModel;
import com.a_track_it.fitdata.model.SessionViewModel;
import com.a_track_it.fitdata.model.UserPreferences;

import java.util.Locale;

;

public class SessionEntryFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_RESID = "arg_resource_id";
    private static final String ARG_COLORID = "arg_color_id";
    private static final String ARG_ACTIVITYID = "arg_activityid";
    public static final String TAG = "SessionEntryFragment";
    private boolean mIsGymWorkout;
    private boolean mIsShootWorkout;
    private int mActivityID;
    private int mColorID;
    private int mResourceID;
    private int mGoalDuration;
    private int mGoalSteps;
    private boolean mBuildClicked;
    private int mBackgroundColor;
    private Workout mWorkout;
    private WorkoutSet mWorkoutSet;
    private OnSessionEntryFragmentListener mListener;
    private MessagesViewModel messagesViewModel;
    private SessionViewModel sessionViewModel;
    private LinearLayout mLinear;

    public SessionEntryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param activityid,
     * @param resid
     * @param colorid
     * @return A new instance of fragment SessionEntryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SessionEntryFragment newInstance(int activityid, int resid, int colorid) {
        final SessionEntryFragment fragment = new SessionEntryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTIVITYID, activityid);
        args.putInt(ARG_RESID, resid);
        args.putInt(ARG_COLORID, colorid);
        fragment.setArguments(args);
        return fragment;
    }
    /**
     *
     * Communicating with MainActivity - from trigger CustomListFragment
     */
    public interface OnSessionEntryFragmentListener {
        void onSessionEntryRequest(int index, String defaultValue);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((getArguments() != null) && (getArguments().containsKey(ARG_ACTIVITYID))){
            mActivityID = getArguments().getInt(ARG_ACTIVITYID);
            mColorID = getArguments().getInt(ARG_COLORID);
            mResourceID = (getArguments().containsKey(ARG_RESID)) ? getArguments().getInt(ARG_RESID) : R.drawable.ic_running;
        }else{
            if ((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_ACTIVITYID))){
                mActivityID = savedInstanceState.getInt(ARG_ACTIVITYID);
                mResourceID = (savedInstanceState.containsKey(ARG_RESID)) ? savedInstanceState.getInt(ARG_RESID) : R.drawable.ic_running;
                mColorID = savedInstanceState.getInt(ARG_COLORID);
            }
        }
        mIsGymWorkout = (mActivityID > 0) && Utilities.isGymWorkout(mActivityID);
        mIsShootWorkout = (mActivityID > 0) && Utilities.isShooting(mActivityID);
        try {
            messagesViewModel = ViewModelProviders.of(requireActivity()).get(MessagesViewModel.class);
            sessionViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        }catch (IllegalStateException ise){
            Log.e(TAG, "illegal state on viewmodels " + ise.getLocalizedMessage());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWorkout = sessionViewModel.getWorkout().getValue();
        mWorkoutSet = sessionViewModel.getWorkoutSet().getValue();

    }

    public boolean getBuildClicked(){ return mBuildClicked; }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = null;
        if (!mIsGymWorkout && !mIsShootWorkout) {
            rootView =  inflater.inflate(R.layout.fragment_session_entry, container, false);
            mLinear = rootView.findViewById(R.id.linear_session);
            Button session_activity_button = rootView.findViewById(R.id.session_activity_button);
           // mBackgroundColor = session_activity_button.getHighlightColor();
            if ((mResourceID > 0) && (session_activity_button != null)){
                Drawable d = ContextCompat.getDrawable(rootView.getContext(), mResourceID);
                if (d != null) session_activity_button.setCompoundDrawables(null, null, null, d);
            }
            rootView.findViewById(R.id.session_activity_button).setOnClickListener(view -> {
                if (mListener != null) {
                    mListener.onSessionEntryRequest(Constants.CHOOSE_ACTIIVITY, "");
                }
            });
          //  final TextView icon_button = rootView.findViewById(R.id.session_icon);

            /*icon_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onSessionEntryRequest(Constants.CHOOSE_ACTIIVITY, "");
                    }
                }
            });*/
            final Button goal1_button = rootView.findViewById(R.id.session_goal_1_button);
            goal1_button.setOnClickListener(v -> {
                if (mListener != null){ mListener.onSessionEntryRequest(Constants.CHOOSE_GOAL_1, "");}
            });
            final Button goal2_button = rootView.findViewById(R.id.session_goal_2_button);
            goal2_button.setOnClickListener(v -> {
                if (mListener != null){ mListener.onSessionEntryRequest(Constants.CHOOSE_GOAL_2, "");}
            });
            final Button start_button = rootView.findViewById(R.id.session_start_button);
            start_button.setOnClickListener(view -> {
                if (mListener != null) {
                    if (mWorkout.activityID > 0) {
                        mIsGymWorkout = Utilities.isGymWorkout(mWorkout.activityID);
                        sessionViewModel.setActiveWorkout(mWorkout);
                        sessionViewModel.setActiveWorkoutSet(mWorkoutSet);
                        mListener.onSessionEntryRequest(Constants.CHOOSE_START_SESSION, "");
                    } else{
                        session_activity_button.requestFocus();
                        String sMsg = getString(R.string.session_prompt) + getString(R.string.my_space_string) + getString(R.string.label_activity);
                        Toast.makeText(getContext(), sMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            final TextView message1  = rootView.findViewById(R.id.entry_message1);
            final TextView message2  = rootView.findViewById(R.id.entry_message2);
            // observe the live data changes
            messagesViewModel.CurrentMessage().observe(this, s -> message1.setText(s));
            messagesViewModel.getBpmMsg().observe(this, s -> message2.setText(s));
            sessionViewModel.getWorkout().observe(this, workout -> {
                Log.d(TAG, "getWorkout onChanged updateUI");
                updateUI();
            });
            sessionViewModel.getWorkoutSet().observe(this, workoutSet -> {
                Log.d(TAG, "getWorkoutSet onChanged updateUI");
                updateUI();
            });
        }
        if (mIsGymWorkout) {
            rootView = inflater.inflate(R.layout.fragment_gym_entry, container, false);
            mLinear = rootView.findViewById(R.id.linear_gym);
            Button activity_button = rootView.findViewById(R.id.gym_activity_button);
            if ((mResourceID > 0) && (activity_button != null)){
                Drawable d = ContextCompat.getDrawable(getContext(), mResourceID);
                if (d != null) activity_button.setCompoundDrawables(null, null, null, d);
            }
            rootView.findViewById(R.id.gym_activity_button).setOnClickListener(view -> {
                if (mListener != null) {
                    mListener.onSessionEntryRequest(Constants.CHOOSE_ACTIIVITY, Integer.toString(mActivityID));
                }
            });
            final Button bodypart_btn = rootView.findViewById(R.id.bodypart_button);
            mBackgroundColor = bodypart_btn.getHighlightColor();
            bodypart_btn.setOnClickListener(view -> {
                if (mListener != null) {
                    mListener.onSessionEntryRequest(Constants.CHOOSE_BODYPART, "");
                }
            });
            final Button exercise_btn = rootView.findViewById(R.id.exercise_button);
            exercise_btn.setOnClickListener(view -> {
                if (mListener != null) {
                    mListener.onSessionEntryRequest(Constants.CHOOSE_EXERCISE, "");
                }
            });
            exercise_btn.setOnLongClickListener(v -> {
                if (mListener != null) mListener.onSessionEntryRequest(Constants.CHOOSE_NEW_EXERCISE, "");
                return (mListener != null);
            });
            final Button weight_button = rootView.findViewById(R.id.weight_button);
            weight_button.setOnClickListener(view -> {
                if (mListener != null) {
                    mListener.onSessionEntryRequest(Constants.CHOOSE_WEIGHT, "");
                }
            });
            final Button sets_button = rootView.findViewById(R.id.sets_button);
            sets_button.setOnClickListener(view -> {
                if (mListener != null) {
                    mListener.onSessionEntryRequest(Constants.CHOOSE_SETS, "");
                }
            });
            final Button reps_button = rootView.findViewById(R.id.reps_button);
            reps_button.setOnClickListener(view -> {
                if (mListener != null) {
                    mListener.onSessionEntryRequest(Constants.CHOOSE_REPS, "");
                }
            });

            final Button build_button = rootView.findViewById(R.id.gym_build_button);
            build_button.setOnClickListener(view -> {
                if (mListener != null) {
                    if (mWorkoutSet.activityID > 0) {
                        mIsGymWorkout = Utilities.isGymWorkout(mWorkoutSet.activityID);
                        if (mWorkoutSet.isValid()) {
                            mBuildClicked = true;
                            mListener.onSessionEntryRequest(Constants.CHOOSE_BUILD_SESSION, "");
                        } else {
                            exercise_btn.requestFocus();
                            final String sMsg = getString(R.string.session_prompt) + getString(R.string.my_space_string) + getString(R.string.label_exercise).toLowerCase();
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), sMsg, Toast.LENGTH_SHORT).show());
                        }
                    }
                }
            });
            build_button.setOnLongClickListener(v -> {
                if (mListener != null) {
                    mListener.onSessionEntryRequest(Constants.CHOOSE_BUILD_LONGCLICK, "");
                    }
                return false;
            });
            final Button start_button = rootView.findViewById(R.id.gym_start_button);
            start_button.setOnClickListener(view -> {
                if (mListener != null) {
                    if (mWorkout.activityID > 0) {
                        mIsGymWorkout = Utilities.isGymWorkout(mWorkout.activityID);
                        sessionViewModel.setActiveWorkout(mWorkout);
                        sessionViewModel.setActiveWorkoutSet(mWorkoutSet);

                        if ((mWorkoutSet.exerciseID > 0) || mBuildClicked){
                            mListener.onSessionEntryRequest(Constants.CHOOSE_START_SESSION, "");
                        } else
                        if (!mBuildClicked){
                            exercise_btn.requestFocus();
                            String sMsg = getString(R.string.session_prompt) + getString(R.string.my_space_string) + getString(R.string.label_exercise).toLowerCase();
                            Toast.makeText(getContext(), sMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


            if (rootView.findViewById(R.id.gym_rest_button) != null) {
                final Button rest_button = rootView.findViewById(R.id.gym_rest_button);
                rest_button.setOnClickListener(view -> ShowAlertDialogRestDuration(rest_button.getRootView(), mWorkout.activityID));

            }
            final TextView message1  = rootView.findViewById(R.id.gym_message1);
            // observe the live data changes
            messagesViewModel.CurrentMessage().observe(this, s -> message1.setText(s));
            final TextView message2  = rootView.findViewById(R.id.gym_message2);
            messagesViewModel.getBpmMsg().observe(this, s -> message2.setText(s));
            sessionViewModel.getWorkout().observe(this, workout -> {
                Log.d(TAG, "getWorkout onChanged updateUI");
                updateUI();
            });
            sessionViewModel.getWorkoutSet().observe(this, workoutSet -> {
                Log.d(TAG, "getWorkoutSet onChanged updateUI");
                updateUI();
            });
            sessionViewModel.getToDoSets().observe(this, toDoSets -> {
                Log.d(TAG, "getWorkoutSet onChanged updateUI");
                updateUI();
            });

        }
        if (mIsShootWorkout){
            rootView = inflater.inflate(R.layout.fragment_archery_entry, container, false);
            mLinear = rootView.findViewById(R.id.linear_archery);

            final Button field_btn = rootView.findViewById(R.id.archery_field_button);
            mBackgroundColor = field_btn.getHighlightColor();
            field_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onSessionEntryRequest(Constants.CHOOSE_TARGET_FIELD, "");
                    }
                }
            });
            final Button target_size_btn = rootView.findViewById(R.id.archery_target_size);
            target_size_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onSessionEntryRequest(Constants.CHOOSE_TARGET_SIZE, "");
                    }
                }
            });
            final Button equipment_btn = rootView.findViewById(R.id.archery_equipment_button);
            equipment_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onSessionEntryRequest(Constants.CHOOSE_EQUIPMENT, "");
                    }
                }
            });
            final Button distance_btn = rootView.findViewById(R.id.archery_distance_button);
            distance_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onSessionEntryRequest(Constants.CHOOSE_DISTANCE, "");
                    }
                }
            });
            final Button ends_btn = rootView.findViewById(R.id.archery_ends_button);
            ends_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onSessionEntryRequest(Constants.CHOOSE_ENDS, "");
                    }
                }
            });
            if (rootView.findViewById(R.id.archery_rest_button) != null) {
                final Button rest_button = rootView.findViewById(R.id.archery_rest_button);
                rest_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAlertDialogRestDuration(rest_button.getRootView(), mWorkout.activityID);
                    }
                });

            }
            if (rootView.findViewById(R.id.archery_per_end_button) != null) {
                final Button perend_btn = rootView.findViewById(R.id.archery_per_end_button);
                perend_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            mListener.onSessionEntryRequest(Constants.CHOOSE_PER_END, "");
                        }
                    }
                });
            }
            final Button start_button = rootView.findViewById(R.id.archery_start_button);
            if (start_button != null)
                start_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            if (mWorkout.activityID > 0) {
                                if ((mWorkout.equipmentID > 0) && (mWorkout.setCount > 0)){
                                    if (mWorkoutSet.activityID != mWorkout.activityID)
                                        mWorkoutSet.activityID = mWorkout.activityID;
                                    if ((mWorkoutSet._id == 0) && (mWorkout._id > 0))
                                        mWorkoutSet._id = mWorkout._id;
                                    mListener.onSessionEntryRequest(Constants.CHOOSE_START_SESSION, "");
                                } else {
                                    String sMsg;
                                    if (mWorkout.setCount > 0) {
                                        equipment_btn.requestFocus();
                                        sMsg = getString(R.string.session_prompt) + getString(R.string.my_space_string) + getString(R.string.label_shoot_equipment);
                                    }else{
                                        ends_btn.requestFocus();
                                        sMsg = getString(R.string.session_prompt) + getString(R.string.my_space_string) + getString(R.string.label_shoot_ends);
                                    }
                                    Toast.makeText(getContext(), sMsg, Toast.LENGTH_SHORT).show();
                                }
                            }/* else {
                                activity_button.requestFocus();
                                String sMsg = getString(R.string.session_prompt) + getString(R.string.my_empty_string) + getString(R.string.label_activity);
                                Toast.makeText(getContext(), sMsg, Toast.LENGTH_SHORT).show();
                            }*/
                        }
                    }
                });
            final TextView message1  = rootView.findViewById(R.id.archery_message1);
            // observe the live data changes
            messagesViewModel.CurrentMessage().observe(this, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    message1.setText(s);
                }
            });
            final TextView message2  = rootView.findViewById(R.id.archery_message2);
            messagesViewModel.getBpmMsg().observe(this, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    message2.setText(s);
                }
            });
            sessionViewModel.getWorkout().observe(this, new Observer<Workout>() {
                @Override
                public void onChanged(Workout workout) {
                    Log.d(TAG, "getWorkout onChanged updateUI");
                    updateUI();
                }
            });
            sessionViewModel.getWorkoutSet().observe(this, new Observer<WorkoutSet>() {
                @Override
                public void onChanged(WorkoutSet workoutSet) {
                    Log.d(TAG, "getWorkoutSet onChanged updateUI");
                    updateUI();
                }
            });
        }
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionEntryFragmentListener) {
            mListener = (OnSessionEntryFragmentListener) context;
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mWorkout != null) outState.putInt(ARG_ACTIVITYID, mWorkout.activityID); else outState.putInt(ARG_ACTIVITYID,mActivityID);
        outState.putInt(ARG_RESID, mResourceID);
        outState.putInt(ARG_COLORID, mColorID);
        super.onSaveInstanceState(outState);
    }

    public void updateUI(){
        // Inflate the layout for this fragment
        View rootView = mLinear.getRootView(); String sTemp;
        Context context= getContext();
        mWorkout = sessionViewModel.getWorkout().getValue();
        mWorkoutSet = sessionViewModel.getWorkoutSet().getValue();
        if ((mWorkout == null) || (mWorkoutSet == null)) return;
        mIsGymWorkout = Utilities.isGymWorkout(mWorkout.activityID);
        mIsShootWorkout = Utilities.isShooting(mWorkout.activityID);
        mGoalDuration = sessionViewModel.getGoalDuration().getValue() == null ? 0 : sessionViewModel.getGoalDuration().getValue();
        mGoalSteps = sessionViewModel.getGoalSteps().getValue() == null ? 0 : sessionViewModel.getGoalSteps().getValue();
        if (!mIsGymWorkout && !mIsShootWorkout) {
            Button session_activity_button = rootView.findViewById(R.id.session_activity_button);
            if (mWorkout.activityName.length() == 0) {
                sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_activity);
                sTemp = sTemp.toUpperCase(Locale.getDefault());
            }else
                sTemp = mWorkout.activityName;
            if (session_activity_button != null) {
                session_activity_button.setText(sTemp);

                if (mResourceID > 0) {
                    Drawable d = ContextCompat.getDrawable(getContext(), mResourceID);
                    if (d != null) session_activity_button.setCompoundDrawables(null, null,null,d);
                }
            }

            if (mGoalDuration > 0){
                Button btn = rootView.findViewById(R.id.session_goal_1_button);
                String sTemp1 = Integer.toString(mGoalDuration) + getString(R.string.my_space_string) + getString(R.string.label_mins);
                if (btn != null) btn.setText(sTemp1);
            }
            if (mGoalSteps > 0){
                Button btn2 = rootView.findViewById(R.id.session_goal_2_button);
                String sTemp2 = Integer.toString(mGoalSteps) + getString(R.string.my_space_string) + getString(R.string.label_steps);
                if (btn2 != null) btn2.setText(sTemp2);
            }
        }else
            if (mIsGymWorkout) {
                boolean bUseKgs = UserPreferences.getUseKG(context);
                Button activity_btn = rootView.findViewById(R.id.gym_activity_button);
                if ((activity_btn != null) && (mResourceID > 0)) {
                    Drawable d = ContextCompat.getDrawable(getContext(), mResourceID);
                    if (d != null) activity_btn.setCompoundDrawables(null, null,null,d);
                }

                Button bodypart_btn = rootView.findViewById(R.id.bodypart_button);
                if ((mWorkoutSet == null) || (mWorkoutSet.bodypartID == 0))
                    sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_bodypart);
                else
                    sTemp = mWorkoutSet.bodypartName;
                if (bodypart_btn != null) bodypart_btn.setText(sTemp);

                Button exercise_btn = rootView.findViewById(R.id.exercise_button);
                if ((mWorkoutSet == null) || (mWorkoutSet.exerciseID == 0))
                    sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_exercise);
                else
                    sTemp = mWorkoutSet.exerciseName;
                if (exercise_btn != null) exercise_btn.setText(sTemp);

                Button weight_button = rootView.findViewById(R.id.weight_button);
                if ((mWorkoutSet == null) || (mWorkoutSet.weightTotal == 0))
                    sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_weight);
                else {
                    if (bUseKgs)
                        sTemp = Float.toString(mWorkoutSet.weightTotal) + context.getString(R.string.my_space_string) + context.getString(R.string.label_weight_units_kg);
                    else
                        sTemp = Float.toString(mWorkoutSet.weightTotal) + context.getString(R.string.my_space_string) + context.getString(R.string.label_weight_units_lbs);
                }
                if (weight_button != null) weight_button.setText(sTemp);
                Button sets_button = rootView.findViewById(R.id.sets_button);
                if ((mWorkoutSet == null) || (mWorkoutSet.setCount == 0))
                    sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_set);
                else
                    sTemp = Integer.toString(mWorkoutSet.setCount) + context.getString(R.string.my_space_string) + context.getString(R.string.label_set);
                if (sets_button != null) sets_button.setText(sTemp);
                Button reps_button = rootView.findViewById(R.id.reps_button);
                if ((mWorkoutSet == null) || (mWorkoutSet.repCount == 0))
                    sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_rep);
                else
                    sTemp = Integer.toString(mWorkoutSet.repCount) + context.getString(R.string.my_space_string) + context.getString(R.string.label_rep);
                if (reps_button != null) reps_button.setText(sTemp);
                Button build_button = rootView.findViewById(R.id.gym_build_button);
                if ((build_button != null) && (mWorkout.start > 0)){
                    //build_button.setVisibility(View.GONE);
                    Button btn = rootView.findViewById(R.id.gym_start_button);
                    if (btn != null) btn.setText(getString(R.string.session_continue));
                }
                if (sessionViewModel.getToDoSetSize() > 0){
                    sTemp = getString(R.string.label_build2) + getString(R.string.my_space_string) + Character.toString((char)40) + Integer.toString(sessionViewModel.getToDoSetSize()) + Character.toString((char)41);
                }else
                    sTemp = getString(R.string.label_build);
                if (build_button != null) build_button.setText(sTemp);

                if (rootView.findViewById(R.id.gym_rest_button) != null) {
                    Button rest_button = rootView.findViewById(R.id.gym_rest_button);
                    long milliseconds = (UserPreferences.getWeightsRestDuration(context) * 1000);

                    if (milliseconds == 0)
                        rest_button.setText(context.getString(R.string.action_rest));
                    else {
                        int seconds = (int) (milliseconds / 1000) % 60 ;
                        int minutes = (int) ((milliseconds / (1000*60)) % 60);
                        int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
                        sTemp = context.getString(R.string.label_rest_countdown) + context.getString(R.string.my_space_string);
                        if (hours > 0) {
                            sTemp += String.format("%02d", hours) + context.getString(R.string.my_space_string) + context.getString(R.string.label_hours);
                            if (hours == 1)
                                sTemp = sTemp.substring(0,sTemp.length()-1);
                            if (minutes > 1)
                                sTemp += context.getString(R.string.my_space_string);
                        }
                        if (minutes > 0) {
                            sTemp += String.format(" %02d", minutes) + context.getString(R.string.my_space_string) + context.getString(R.string.label_mins);
                            if (minutes == 1)
                                sTemp = sTemp.substring(0,sTemp.length()-1);
                        }else
                            sTemp += String.format(" %02d", seconds) + context.getString(R.string.my_space_string) + context.getString(R.string.label_secs);
                        if (rest_button != null) rest_button.setText(sTemp);
                    }
                }
            }else
            if (mIsShootWorkout) {
    /*            Button activity_button = rootView.findViewById(R.id.archery_activity_button);
                if (mWorkout.activityName.length() == 0)
                    sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_activity);
                else
                    sTemp = mWorkout.activityName;
                activity_button.setText(sTemp); */
                Button field_btn = rootView.findViewById(R.id.archery_field_button);
                if (mWorkout.shootFormatID == 0)
                    sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_shoot_field).toLowerCase();
                else
                    sTemp = mWorkout.shootFormat;
                if (field_btn != null) field_btn.setText(sTemp);
                Button target_size_btn = rootView.findViewById(R.id.archery_target_size);
                if (mWorkout.targetSizeID == 0)
                    sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_shoot_target_size).toLowerCase();
                else
                    sTemp = mWorkout.targetSizeName;
                if (target_size_btn != null) target_size_btn.setText(sTemp);
                Button equipment_btn = rootView.findViewById(R.id.archery_equipment_button);
                if (mWorkout.equipmentID == 0)
                    sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_shoot_equipment1).toLowerCase();
                else
                    sTemp = mWorkout.equipmentName;
                if (equipment_btn != null) equipment_btn.setText(sTemp);
                Button distance_btn = rootView.findViewById(R.id.archery_distance_button);
                if (mWorkout.distanceID == 0)
                    sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_shoot_distance).toLowerCase();
                else
                    sTemp = mWorkout.distanceName;
                if (distance_btn != null) distance_btn.setText(sTemp);
                Button ends_btn = rootView.findViewById(R.id.archery_ends_button);
                if (mWorkout.setCount == 0)
                    sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_shoot_ends).toLowerCase();
                else
                    sTemp = Integer.toString(mWorkout.setCount) + context.getString(R.string.my_space_string) + context.getString(R.string.label_shoot_ends).toLowerCase();
                if (ends_btn != null) ends_btn.setText(sTemp);

                Button perend_btn = rootView.findViewById(R.id.archery_per_end_button);
                if (mWorkout.repCount == 0)
                    sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_shoot_per_end).toLowerCase();
                else
                    sTemp = Integer.toString(mWorkout.repCount) + context.getString(R.string.my_space_string) + context.getString(R.string.label_shoot_per_end).toLowerCase();
    /*            if (mWorkout.shotsPerEnd == 0)
                    sTemp = context.getString(R.string.default_select) + context.getString(R.string.my_space_string) + context.getString(R.string.label_shoot_per_end).toLowerCase();
                else
                    sTemp = Integer.toString(mWorkout.shotsPerEnd) + context.getString(R.string.my_space_string) + context.getString(R.string.label_shoot_per_end).toLowerCase();*/
                if (perend_btn != null) perend_btn.setText(sTemp);

                if (rootView.findViewById(R.id.archery_rest_button) != null) {
                    Button rest_button = rootView.findViewById(R.id.archery_rest_button);
                    long milliseconds = (UserPreferences.getArcheryRestDuration(context) * 1000);

                    if (milliseconds == 0)
                        rest_button.setText(context.getString(R.string.label_rest_per_end));
                    else {
                        int seconds = (int) (milliseconds / 1000) % 60;
                        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
                        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
                        sTemp = context.getString(R.string.my_empty_string);
                        if (hours > 0)
                            sTemp += String.format("%02d", hours) + context.getString(R.string.my_space_string) + context.getString(R.string.label_hours);
                        if (minutes > 0) {
                            sTemp += String.format(" %02d", minutes) + context.getString(R.string.my_space_string) + context.getString(R.string.label_mins);
                        } else
                            sTemp += String.format(" %02d", seconds) + context.getString(R.string.my_space_string) + context.getString(R.string.label_secs);
                        sTemp += getString(R.string.my_space_string) + getString(R.string.label_append_per_end);
                        rest_button.setText(sTemp);
                    }
                }
            }
    }
    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        Resources resources = requireActivity().getResources();
        int bgColor = resources.getColor(R.color.colorAmbientBackground, null);
        int foreColor = resources.getColor(R.color.colorAmbientForeground, null);
        boolean IsLowBitAmbient =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false);
        boolean DoBurnInProtection =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false);
        // Convert image to grayscale for ambient mode.
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        //mWearableRecyclerView.setColorFilter(filter);
        if (mIsGymWorkout){
            mLinear.findViewById(R.id.gym_activity_button).setBackgroundColor(bgColor);
            mLinear.findViewById(R.id.bodypart_button).setBackgroundColor(bgColor);
            //   (Button)mLinear.findViewById(R.id.bodypart_button).setTextColor
            mLinear.findViewById(R.id.exercise_button).setBackgroundColor(bgColor);
            mLinear.findViewById(R.id.weight_button).setBackgroundColor(bgColor);
            mLinear.findViewById(R.id.sets_button).setBackgroundColor(bgColor);
            mLinear.findViewById(R.id.reps_button).setBackgroundColor(bgColor);
            mLinear.findViewById(R.id.gym_build_button).setBackgroundColor(bgColor);
            mLinear.findViewById(R.id.gym_rest_button).setBackgroundColor(bgColor);
            mLinear.findViewById(R.id.gym_message1).setBackgroundColor(bgColor);
            mLinear.findViewById(R.id.gym_message2).setBackgroundColor(bgColor);
            mLinear.findViewById(R.id.gym_start_button).setBackgroundColor(bgColor);
        }else{
            if (mIsShootWorkout){
                mLinear.findViewById(R.id.archery_field_button).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.archery_equipment_button).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.archery_target_size).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.archery_distance_button).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.archery_ends_button).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.archery_per_end_button).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.archery_rest_button).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.archery_message1).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.archery_message2).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.archery_start_button).setBackgroundColor(bgColor);
            }else{
                mLinear.findViewById(R.id.session_activity_button).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.session_goal_1_button).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.session_goal_2_button).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.entry_message1).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.entry_message2).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.session_icon).setBackgroundColor(bgColor);
                mLinear.findViewById(R.id.session_start_button).setBackgroundColor(bgColor);
            }
        }
    }

    /** Restores the UI to active (non-ambient) mode. */
    public void onExitAmbientInFragment() {
        Log.d(TAG, "onExitAmbient()");
        Context context = getContext();
        Drawable bg = ContextCompat.getDrawable(context, R.drawable.bg_selector);
        Drawable dark_bg =  ContextCompat.getDrawable(context, R.drawable.bg_color_green_dark);
        if (mIsGymWorkout){
            mLinear.findViewById(R.id.gym_activity_button).setBackground(bg);
            mLinear.findViewById(R.id.bodypart_button).setBackground(bg);
            mLinear.findViewById(R.id.exercise_button).setBackground(bg);
            mLinear.findViewById(R.id.weight_button).setBackground(bg);
            mLinear.findViewById(R.id.sets_button).setBackground(bg);
            mLinear.findViewById(R.id.reps_button).setBackground(bg);
            mLinear.findViewById(R.id.gym_build_button).setBackground(bg);
            mLinear.findViewById(R.id.gym_rest_button).setBackground(bg);
            mLinear.findViewById(R.id.gym_message1).setBackground(dark_bg);
            mLinear.findViewById(R.id.gym_message2).setBackground(dark_bg);
            mLinear.findViewById(R.id.gym_start_button).setBackground(bg);
        }else{
            if (mIsShootWorkout){
                mLinear.findViewById(R.id.archery_field_button).setBackground(bg);
                mLinear.findViewById(R.id.archery_equipment_button).setBackground(bg);
                mLinear.findViewById(R.id.archery_target_size).setBackground(bg);
                mLinear.findViewById(R.id.archery_distance_button).setBackground(bg);
                mLinear.findViewById(R.id.archery_ends_button).setBackground(bg);
                mLinear.findViewById(R.id.archery_per_end_button).setBackground(bg);
                mLinear.findViewById(R.id.archery_rest_button).setBackground(bg);
                mLinear.findViewById(R.id.archery_message1).setBackground(dark_bg);
                mLinear.findViewById(R.id.archery_message2).setBackground(dark_bg);
                mLinear.findViewById(R.id.archery_start_button).setBackground(bg);
            }else{
                mLinear.findViewById(R.id.session_activity_button).setBackground(bg);
                mLinear.findViewById(R.id.session_goal_1_button).setBackground(bg);
                mLinear.findViewById(R.id.session_goal_2_button).setBackground(bg);
                mLinear.findViewById(R.id.entry_message1).setBackground(dark_bg);
                mLinear.findViewById(R.id.entry_message2).setBackground(dark_bg);
                mLinear.findViewById(R.id.session_icon).setBackground(dark_bg);
                mLinear.findViewById(R.id.session_start_button).setBackground(bg);
            }
        }
        //mImageView.setColorFilter(mImageViewColorFilter);
    }

    private void ShowAlertDialogRestDuration(final View parent_view, final int activity_id){
        Context context = parent_view.getContext();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context,  R.style.AppTheme_myAlertDialog);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(parent_view.getContext(), R.layout.item_alertlist); //
        if (Utilities.isGymWorkout(activity_id)){
            String[] gym_rests = getResources().getStringArray(R.array.array_rest_duration_gym);
            arrayAdapter.addAll(gym_rests);
            dialogBuilder.setTitle("   Seconds between Sets");
        }
        if (Utilities.isShooting(activity_id)){
            String[] target_rests = getResources().getStringArray(R.array.array_rest_duration_target);
            arrayAdapter.addAll(target_rests);
            dialogBuilder.setTitle("   Seconds between Ends");

        }

        //TODO: preselect an existing rest
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_customlist_alert, null);
        dialogBuilder.setView(customView);
        ListView lv = customView.findViewById(R.id.list_view);
        lv.setAdapter(arrayAdapter);


        final AlertDialog dialog = dialogBuilder.create();
        lv.setOnItemClickListener((adapterView, view, i, l) -> {
            Button rest_button = parent_view.findViewById(R.id.archery_rest_button);
            String sTemp = arrayAdapter.getItem(i);
            if (Utilities.isShooting(activity_id)){
                rest_button = parent_view.findViewById(R.id.archery_rest_button);
                UserPreferences.setArcheryRestDuration(context, Integer.parseInt(sTemp));
            }
            if (Utilities.isGymWorkout(activity_id)){
                rest_button = parent_view.findViewById(R.id.gym_rest_button);
                UserPreferences.setWeightsRestDuration(context, Integer.parseInt(sTemp));
            }
            if (Integer.parseInt(sTemp) < 60) {
                sTemp += " sec rest";
                rest_button.setText(sTemp);
            }
            if (sTemp.equals("60")) rest_button.setText("1 min rest");
            if (sTemp.equals("75")) rest_button.setText("1:15 min rest");
            if (sTemp.equals("90")) rest_button.setText("1:30 min rest");
            if (sTemp.equals("105")) rest_button.setText("1:45 min rest");
            if (sTemp.equals("120")) rest_button.setText("2:00 min rest");
            if (sTemp.equals("180")) rest_button.setText("3:00 min rest");
            if (sTemp.equals("240")) rest_button.setText("4:00 min rest");
            if (sTemp.equals("300")) rest_button.setText("5:00 min rest");
            if (sTemp.equals("360")) rest_button.setText("6:00 min rest");
            if (sTemp.equals("420")) rest_button.setText("7:00 min rest");
            if (sTemp.equals("480")) rest_button.setText("8:00 min rest");
            if (sTemp.equals("540")) rest_button.setText("6:00 min rest");
            if (sTemp.equals("600")) rest_button.setText("10:00 min rest");
            if (sTemp.equals("900")) rest_button.setText("15:00 min rest");
            if (sTemp.equals("1200")) rest_button.setText("20:00 min rest");
            if (sTemp.equals("1800")) rest_button.setText("30:00 min rest");

            dialog.dismiss();
        });
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setCancelable(true);
        dialog.show();
    }

}
