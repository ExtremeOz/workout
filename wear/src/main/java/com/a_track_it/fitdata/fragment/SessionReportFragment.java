package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.user_model.Utilities;
import com.a_track_it.fitdata.data_model.Workout;
import com.a_track_it.fitdata.data_model.WorkoutSet;
import com.a_track_it.fitdata.user_model.MessagesViewModel;
import com.a_track_it.fitdata.model.SavedStateViewModel;
import com.a_track_it.fitdata.model.SessionViewModel;

import java.util.ArrayList;

public class SessionReportFragment extends Fragment {
    public static final String TAG = SessionReportFragment.class.getSimpleName();
    public static final String ARG_ICON_ID = "ICON_ID";
    public static final String ARG_COLOR_ID = "COLOR_ID";
    public static final String ARG_SET = "SET_ID";
    public static final String ARG_WORKOUT = "WORKOUT_ID";
    public static final String ARG_TIMED = "TIMED_ID";
    private SessionViewModel sessionViewModel;
    private MessagesViewModel messagesViewModel;
    private SavedStateViewModel savedStateViewModel;
    private OnSessionReportInteraction mListener;
    private int mIcon;
    private int mColor;
    private long mSetID;
    private long mSessionID;
    private WorkoutSet mWorkoutSet;
    private Workout mWorkout;
    private int mSetCount;
    private long restDuration;
    private boolean mIsGym = false;
    private boolean mIsArchery = false;
    private ArrayList<Workout> mSessions = new ArrayList<>();
    private ArrayList<WorkoutSet> mSets = new ArrayList<>();
    protected LinearLayout mLinear;
    protected TextView mSessionTextView;
    protected TextView mSet1TextView;

    public SessionReportFragment(){

    }
    public static SessionReportFragment newInstance(int icon, int color, long setID, long sessionID, long rest) {
        final SessionReportFragment fragment = new SessionReportFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ICON_ID, icon);
        args.putInt(ARG_COLOR_ID, color);
        args.putLong(ARG_SET, setID);
        args.putLong(ARG_WORKOUT, sessionID);
        args.putLong(ARG_TIMED, rest);
        fragment.setArguments(args);
        return fragment;
    }
    public interface OnSessionReportInteraction {
        void onSessionReportExit();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            mIcon = bundle.getInt(ARG_ICON_ID);
            mColor = bundle.getInt(ARG_COLOR_ID);
            mSetID = bundle.getLong(ARG_SET);
            mSessionID = bundle.getLong(ARG_WORKOUT);
            if (mWorkoutSet.score_card.length() > 0){
                String itemsEnds[] = mWorkoutSet.score_card.split(",");
               // mEndScores =  new ArrayList<>(Arrays.asList(itemsEnds));
            }
            mIsGym = Utilities.isGymWorkout(mWorkout.activityID);
            mIsArchery = Utilities.isShooting(mWorkout.activityID);
            if (mWorkout.score_card.length() > 0){
                String items[] = mWorkout.score_card.split(",");
               // mScoreCard =  new ArrayList<>(Arrays.asList(items));
            }
            mSetCount = mWorkoutSet.setCount;
            restDuration = bundle.getLong(ARG_TIMED);

            try {
                messagesViewModel = ViewModelProviders.of(requireActivity()).get(MessagesViewModel.class);
                savedStateViewModel = ViewModelProviders.of(requireActivity()).get(SavedStateViewModel.class);
                sessionViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
            }catch (IllegalStateException ise){
                Log.e(TAG, "illegal state on viewmodels " + ise.getLocalizedMessage());
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_session_report, container, false);
        mLinear = rootView.findViewById(R.id.linear_report);
        mSessionTextView = rootView.findViewById(R.id.workout_text_report);
        mSet1TextView = rootView.findViewById(R.id.set1_text_report);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Context context = getContext();
        mWorkout = savedStateViewModel.getActiveWorkout().getValue();
        if (mWorkout != null){
            mSessionTextView.setText(mWorkout.shortText());
        }
        mWorkoutSet = savedStateViewModel.getActiveWorkoutSet().getValue();
        if (mWorkoutSet != null)
            mSet1TextView.setText(mWorkoutSet.shortText());

        ViewGroup.LayoutParams layoutParams =  mSet1TextView.getLayoutParams(); //    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mSets = sessionViewModel.getCompletedSets().getValue();
        if (mSets != null) {
            int i = 0;
            for (WorkoutSet set : mSets) {
                if (i > 0) {
                    TextView newSet = new TextView(context);
                    newSet.setLayoutParams(layoutParams);
                    newSet.setText(set.shortText());
                    mLinear.addView(newSet);
                }
                i+=1;
            }
        }
        if (mSessionTextView.getText().length() == 0){
            Workout workout = savedStateViewModel.getActiveWorkout().getValue();
            if (workout != null) {
                mSessionTextView.setText(workout.shortText());
                Log.i(TAG, "workout from view model " + workout.setCount);
            }
        }
        if (mSet1TextView.getText().length() == 0){
            WorkoutSet set = savedStateViewModel.getActiveWorkoutSet().getValue();
            if (set != null){
                mSet1TextView.setText(set.shortText());
                Log.i(TAG, "workout from view model " + set.setCount);
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListener.onSessionReportExit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionReportInteraction) {
            mListener = (OnSessionReportInteraction) context;
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
    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        Log.d(TAG, "onEnterAmbient() " + ambientDetails);

        // Convert image to grayscale for ambient mode.
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        //mWearableRecyclerView.setColorFilter(filter);

    }

    /** Restores the UI to active (non-ambient) mode. */
    public void onExitAmbientInFragment() {
        Log.d(TAG, "onExitAmbient()");

        //mImageView.setColorFilter(mImageViewColorFilter);
    }
}
