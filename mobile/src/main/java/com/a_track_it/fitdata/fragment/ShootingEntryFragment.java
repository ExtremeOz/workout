package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutMeta;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.a_track_it.fitdata.common.user_model.LiveDataTimerViewModel;
import com.a_track_it.fitdata.common.user_model.SavedStateViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShootingEntryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShootingEntryFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_RESID = "arg_resource_id";
    private static final String ARG_COLORID = "arg_color_id";
    public static final String TAG = "ShootingEntryFragment";

    private static final String ARG_WORKOUT = "arg_workout";
    private static final String ARG_WORKOUT_SET = "arg_workoutset";
    private static final String ARG_WORKOUT_META= "arg_workoutmeta";
    private static final String ARG_EXISTING_SET = "arg_existing";
    private boolean mExistingShooting = false;
    private int mColorID;
    private int mResourceID;
    private  View rootView;

    private Workout mWorkout;
    private WorkoutSet mWorkoutSet;
    private WorkoutMeta mWorkoutMeta;
    private FragmentInterface mListener;
    private SavedStateViewModel mSavedStateViewModel;
    private LiveDataTimerViewModel mTimerViewModel;

    public ShootingEntryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param workout start time, activity id, name.
     * @param resid drawable resource id
     * @param colorid color id
     * @param set workoutset bodypart and exercise
     * @return A new instance of fragment ShootingEntryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShootingEntryFragment newInstance(Workout workout, int resid, int colorid, WorkoutSet set, WorkoutMeta meta) {
        final ShootingEntryFragment fragment = new ShootingEntryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_RESID, resid);
        args.putInt(ARG_COLORID, colorid);
        args.putParcelable(ARG_WORKOUT, workout);
        args.putParcelable(ARG_WORKOUT_SET, set);
        args.putParcelable(ARG_WORKOUT_META, meta);
        fragment.setArguments(args);
        return fragment;
    }
    /** myClicker - propagate click to RoomActivity via FragmentInterface
     - added form validation for Gym exercises
     **/
    View.OnClickListener myClicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(), 0, null);
        }
    };

    View.OnLongClickListener myLongClicker = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(), 0, Constants.LABEL_LONG);
            return true;
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedStateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        mTimerViewModel = new ViewModelProvider(requireActivity()).get(LiveDataTimerViewModel.class);

        if ((getArguments() != null) && (getArguments().containsKey(ARG_WORKOUT))){
            mWorkout = (Workout) getArguments().getParcelable(ARG_WORKOUT);
            if ((getArguments().containsKey(ARG_WORKOUT_SET))) {
                WorkoutSet this_set = (WorkoutSet) getArguments().getParcelable(ARG_WORKOUT_SET);
                if (this_set != null) Log.d(TAG, "set on create " + this_set.toString());
            }
            if (getArguments().containsKey(ARG_COLORID)) mColorID = getArguments().getInt(ARG_COLORID); else mColorID = R.color.archery;
            mResourceID = (getArguments().containsKey(ARG_RESID)) ? getArguments().getInt(ARG_RESID) : R.drawable.ic_shooting_with_arch_silhouette;
            if (getArguments().containsKey(ARG_WORKOUT_SET))  mWorkoutSet = getArguments().getParcelable(ARG_WORKOUT_SET);
            if (getArguments().containsKey(ARG_WORKOUT_META))  mWorkoutMeta = getArguments().getParcelable(ARG_WORKOUT_META);
            Log.i(TAG, "onCreate mWorkoutSet now " + mWorkoutSet.toString());
        }
        /*else{
            if ((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_WORKOUT))){
                Workout workout = savedInstanceState.getParcelable(ARG_WORKOUT);
                mWorkout = workout;
                mResourceID = (savedInstanceState.containsKey(ARG_RESID)) ? savedInstanceState.getInt(ARG_RESID) : R.drawable.ic_shooting_with_arch_silhouette;
                if (savedInstanceState.containsKey(ARG_WORKOUT_SET))  mWorkoutSet = savedInstanceState.getParcelable(ARG_WORKOUT_SET);
                if (savedInstanceState.containsKey(ARG_WORKOUT_META))  mWorkoutMeta = savedInstanceState.getParcelable(ARG_WORKOUT_META);
                mExistingShooting = (savedInstanceState.containsKey(ARG_EXISTING_SET)) && savedInstanceState.getBoolean(ARG_EXISTING_SET);
            }
        }*/

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_shooting_entry, container, false);
        final MaterialButton activity_button = rootView.findViewById(R.id.archery_activity_button);
        activity_button.setOnClickListener(myClicker);
        final MaterialButton field_btn = rootView.findViewById(R.id.archery_field_button);
        field_btn.setTag(Constants.UID_archery_field_button);
        field_btn.setOnClickListener(myClicker);
        final MaterialButton target_size_btn = rootView.findViewById(R.id.archery_target_size);
        target_size_btn.setTag(Constants.UID_archery_target_size);
        target_size_btn.setOnClickListener(myClicker);
        final MaterialButton equipment_btn = rootView.findViewById(R.id.archery_equipment_button);
        equipment_btn.setTag(Constants.UID_archery_equipment_button);
        equipment_btn.setOnClickListener(myClicker);
        final MaterialButton distance_btn = rootView.findViewById(R.id.archery_distance_button);
        distance_btn.setTag(Constants.UID_archery_distance_button);
        distance_btn.setOnClickListener(myClicker);
        final MaterialButton ends_btn = rootView.findViewById(R.id.archery_ends_button);
        ends_btn.setTag(Constants.UID_archery_ends_button);
        ends_btn.setOnClickListener(myClicker);
        final MaterialButton perEnd_btn = rootView.findViewById(R.id.archery_per_end_button);
        perEnd_btn.setTag(Constants.UID_archery_per_end_button);
        perEnd_btn.setOnClickListener(myClicker);
        final MaterialButton entry_message1 = rootView.findViewById(R.id.entry_message1);
        entry_message1.setTag(Constants.UID_entry_message1);
        entry_message1.setOnClickListener(myClicker);
        final MaterialButton entry_message2 = rootView.findViewById(R.id.entry_message2);
        entry_message2.setTag(Constants.UID_entry_message2);
        entry_message2.setOnClickListener(myClicker);
        final MaterialButton start_button = rootView.findViewById(R.id.archery_start_button);
        start_button.setTag(Constants.UID_archery_start_button);
        start_button.setOnClickListener(myClicker);
        final MaterialButton call_button = rootView.findViewById(R.id.archery_call_button);
        call_button.setTag(Constants.UID_archery_call_button);
        call_button.setOnClickListener(myClicker);
        call_button.setOnLongClickListener(myLongClicker);        
        final MaterialButton end_button = rootView.findViewById(R.id.archery_end_duration);
        end_button.setTag(Constants.UID_archery_end_button);
        end_button.setOnClickListener(myClicker);
        end_button.setOnLongClickListener(myLongClicker);
        Context context = getActivity();
        UserPreferences userPrefs = UserPreferences.getPreferences(context, mWorkout.userID);
        Drawable drawableChecked = AppCompatResources.getDrawable(context,R.drawable.ic_outline_check_white);
        Utilities.setColorFilter(drawableChecked,ContextCompat.getColor(context, R.color.colorSplash));
        Drawable drawableUnChecked = AppCompatResources.getDrawable(context,R.drawable.ic_close_white);
        Utilities.setColorFilter(drawableUnChecked,ContextCompat.getColor(context, R.color.secondaryTextColor));

        final MaterialButton archery_use_timed_rest_toggle = rootView.findViewById(R.id.archery_rest_toggle);
        archery_use_timed_rest_toggle.setChecked(userPrefs.getTimedRest());
        final MaterialButton archery_auto_start_toggle = rootView.findViewById(R.id.archery_rest_auto_start);
        if (userPrefs.getRestAutoStart()){
            archery_auto_start_toggle.setIcon(drawableChecked);
            archery_auto_start_toggle.setEnabled(true);
        }else {
            archery_auto_start_toggle.setIcon(drawableUnChecked);
        }
        archery_auto_start_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton) v).isChecked();
            userPrefs.setRestAutoStart(isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
            }
        });
        archery_use_timed_rest_toggle.setOnClickListener(v -> {
            boolean isChecked = ((MaterialButton) v).isChecked();
            userPrefs.setTimedRest(isChecked);
            if (isChecked) {
                ((MaterialButton) v).setIcon(drawableChecked);
                archery_auto_start_toggle.setVisibility(View.VISIBLE);
                String title = getString(R.string.label_archery_call_duration).replace(getString(R.string.label_duration), Constants.ATRACKIT_EMPTY);
                title = getDurationRest(title, userPrefs.getArcheryCallDuration());
                call_button.setText(title);
                call_button.setVisibility(MaterialButton.VISIBLE);
                call_button.setEnabled(true);
                title = getString(R.string.label_archery_end_duration).replace(getString(R.string.label_duration), Constants.ATRACKIT_EMPTY);
                title = getDurationRest(title, userPrefs.getArcheryEndDuration());
                call_button.setText(title);
                end_button.setVisibility(MaterialButton.VISIBLE);
                end_button.setEnabled(true);
            } else {
                ((MaterialButton) v).setIcon(drawableUnChecked);
                archery_auto_start_toggle.setVisibility(View.GONE);
                call_button.setVisibility(MaterialButton.GONE);
                end_button.setVisibility(MaterialButton.GONE);
            }
        });
        archery_use_timed_rest_toggle.setChecked(userPrefs.getTimedRest());
        if (userPrefs.getTimedRest()){
            archery_use_timed_rest_toggle.setIcon(drawableChecked);
            archery_use_timed_rest_toggle.setEnabled(true);
        }else {
            archery_use_timed_rest_toggle.setIcon(drawableUnChecked);
        }
        if (userPrefs.getTimedRest()){
            archery_use_timed_rest_toggle.setIcon(drawableChecked);
            archery_auto_start_toggle.setVisibility(View.VISIBLE);
            String title = title = getString(R.string.label_archery_call_duration).replace(getString(R.string.label_duration), Constants.ATRACKIT_EMPTY);
            title = getDurationRest(title, userPrefs.getArcheryCallDuration());
            call_button.setText(title);
            call_button.setVisibility(MaterialButton.VISIBLE);
            call_button.setEnabled(true);
            title = getString(R.string.label_archery_end_duration).replace(getString(R.string.label_duration), Constants.ATRACKIT_EMPTY);
            title = getDurationRest(title, userPrefs.getArcheryEndDuration());
            end_button.setText(title);
            end_button.setVisibility(MaterialButton.VISIBLE);
            end_button.setEnabled(true);
        }else {
            archery_use_timed_rest_toggle.setIcon(drawableUnChecked);
            archery_auto_start_toggle.setVisibility(View.GONE);
            call_button.setVisibility(MaterialButton.GONE);
            end_button.setVisibility(MaterialButton.GONE);
        }
        mSavedStateViewModel.getActiveWorkoutSet().observe(requireActivity(), workoutSet -> {
            if (workoutSet == null) return;
            String sTemp; Integer iTemp;
            if (workoutSet.call_duration != null &&  workoutSet.call_duration > 0){
                sTemp = getString(R.string.label_archery_call_duration).replace(getString(R.string.label_duration), Constants.ATRACKIT_EMPTY);
                iTemp = Math.toIntExact(workoutSet.call_duration/1000);
                sTemp = getDurationRest(sTemp, iTemp);
                call_button.setText(sTemp);
            }
            if (workoutSet.goal_duration != null &&  workoutSet.goal_duration > 0){
                sTemp = getString(R.string.label_archery_end_duration).replace(getString(R.string.label_duration), Constants.ATRACKIT_EMPTY);
                iTemp = Math.toIntExact(workoutSet.goal_duration/1000);
                sTemp = getDurationRest(sTemp, iTemp);
                end_button.setText(sTemp);
            }
        });
        mSavedStateViewModel.getActiveWorkoutMeta().observe(requireActivity(), workoutMeta -> {
            if (workoutMeta == null) return;
            activity_button.setText(workoutMeta.activityName);
            if (workoutMeta.shootFormat.length() > 0) field_btn.setText(workoutMeta.shootFormat); else field_btn.setText(getString(R.string.label_shoot_field));
            if (workoutMeta.targetSizeName.length() > 0) target_size_btn.setText(workoutMeta.targetSizeName); else target_size_btn.setText(getString(R.string.label_shoot_target_size));
            if (workoutMeta.equipmentName.length() > 0) equipment_btn.setText(workoutMeta.equipmentName); else equipment_btn.setText(getString(R.string.label_shoot_equipment));
            if (workoutMeta.distanceName.length() > 0) distance_btn.setText(workoutMeta.distanceName); else distance_btn.setText(getString(R.string.label_shoot_distance));
            String sLabelEnds = workoutMeta.setCount + Constants.ATRACKIT_SPACE + getString(R.string.label_shoot_ends);
            if (workoutMeta.setCount > 0) ends_btn.setText(sLabelEnds); else ends_btn.setText(getString(R.string.label_shoot_ends));
            sLabelEnds = workoutMeta.shotsPerEnd + Constants.ATRACKIT_SPACE + getString(R.string.label_shoot_per_end);
            if (workoutMeta.shotsPerEnd > 0) perEnd_btn.setText(sLabelEnds); else perEnd_btn.setText(getString(R.string.label_shoot_per_end));
            if ((workoutMeta.setCount > 0) && (workoutMeta.shotsPerEnd > 0)) {
                sLabelEnds = (workoutMeta.setCount * workoutMeta.shotsPerEnd * 10) + Constants.ATRACKIT_SPACE + getString(R.string.label_shoot_possible_score);
                entry_message1.setText(sLabelEnds);
            }else
                entry_message1.setText(getString(R.string.label_shoot_possible_score));
        });
        mTimerViewModel.getCurrentTime().observe(getViewLifecycleOwner(), aLong -> new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if  (entry_message2 != null)
                    if ((entry_message2.getTag() == null) || ((Integer) entry_message2.getTag() == 0))
                        entry_message2.setText(Utilities.getTimeString(aLong));
            }
        }));
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
        if (mWorkout != null) outState.putParcelable(ARG_WORKOUT, mWorkout);
        if (mWorkoutSet != null) outState.putParcelable(ARG_WORKOUT_SET, mWorkoutSet);
        outState.putBoolean(ARG_EXISTING_SET, mExistingShooting);
        outState.putInt(ARG_RESID, mResourceID);
        outState.putInt(ARG_COLORID, mColorID);
        super.onSaveInstanceState(outState);
    }

    private String getDurationRest(String type, int iRestSeconds){
        long milliseconds = (TimeUnit.SECONDS.toMillis(iRestSeconds));
        String sTemp;
        if (milliseconds == 0)
            sTemp = type + Constants.ATRACKIT_SPACE + "none";
        else {
            int seconds = (int) (milliseconds / 1000) % 60;
            int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
            int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
            sTemp = type + Constants.ATRACKIT_SPACE;
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
        }
        return sTemp;
    }
}
