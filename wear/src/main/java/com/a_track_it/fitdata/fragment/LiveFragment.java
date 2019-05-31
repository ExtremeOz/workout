package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

/**
 * Activities that contain this fragment must implement the
 * {@link OnLiveFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LiveFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String TAG = LiveFragment.class.getSimpleName();
    public static final String ARG_LIVEPAGE_ACTIVITY_ID = "live_activity_id";
    public static final String ARG_LIVEPAGE_IMAGE_ID = "live_image_id";
    public static final String ARG_LIVEPAGE_TEXT_ID = "live_text_id";
    public static final String ARG_LIVEPAGE_COLOR_ID = "live_color_id";
    public static final String ARG_LIVEPAGE_WORKOUT_ID = "live_workout_id";
    public static final String ARG_LIVEPAGE_WORKOUTSET_ID = "live_set_id";
    public static final String ARG_LIVEPAGE_DURATION_ID = "duration_id";
    public static final String ARG_LIVEPAGE_STEPS_ID = "steps_id";
    public static final String TAG_PAUSE = "pause";
    public static final String TAG_RESUME = "resume";

    public static final int MSG_HOME = 0;
    public static final int MSG_BMP = 1;
    public static final int MSG_STEP = 2;
    public static final int MSG_LOCATION = 3;
    public static final int MSG_GOALS = 4;

    private RelativeLayout mRelativeLayoutLive;
    private ImageView mTopImageView;
    private ImageView mImageViewOne;
    private ImageView mImageViewTwo;
    private AnimationDrawable mRecordingAnimation;
    private ImageView mImageViewThree;
    private Drawable mActivityDrawable;
    private TextView mTextView;
    private ColorFilter mImageViewColorFilter;
    private ColorFilter mBackgroundColorFilter;
    private ColorFilter mBackground2ColorFilter;
    private TextView mMessageText;
    private TextView mMessageText2;
    private TextView mMessageText3;
    private TextView mLiveMessageText2;
    private long mSessionID;
    private long mSetID;
    private int mActivityID;
    private int mParam1;
    private String mParam2;
    private int mParam3;
    private long mRestDuration;
    private boolean mSessionLive = false;
    private Workout mWorkout;
    private WorkoutSet mSet;
    private OnLiveFragmentInteractionListener mListener;
    private MessagesViewModel messagesViewModel;
    private SessionViewModel sessionViewModel;

    private int mGoalDuration;
    private int mGoalSteps;

    private int mMessageSource = 0;
    private boolean mIsGym;
    private boolean mIsShoot;

   // private SessionViewModel sessionViewModel;
    public LiveFragment() {
        // Required empty public constructor
    }
/*
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imageid int resource id
     * @param text String - heading
     * @param colorid int - color resource id
     * @param workout Workout - current workout object
     * @param set WorkoutSet - current set in progress
     * @return A new instance of fragment HomPageFragment.
     */

    public static LiveFragment newInstance(int activityID, int imageid, String text, int colorid, long sessionID, long setID, int StepTarget, int DurationTarget) {
        final LiveFragment fragment = new LiveFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LIVEPAGE_ACTIVITY_ID, activityID);
        args.putInt(ARG_LIVEPAGE_IMAGE_ID, imageid);
        args.putString(ARG_LIVEPAGE_TEXT_ID, text);
        args.putInt(ARG_LIVEPAGE_COLOR_ID, colorid);
        args.putLong(ARG_LIVEPAGE_WORKOUT_ID, sessionID);
        args.putLong(ARG_LIVEPAGE_WORKOUTSET_ID, setID);
        args.putInt(ARG_LIVEPAGE_STEPS_ID, StepTarget);
        args.putInt(ARG_LIVEPAGE_DURATION_ID, DurationTarget);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mIsShoot = false; mIsGym = false;
        if (args != null) {
            if (args.containsKey(ARG_LIVEPAGE_ACTIVITY_ID)) {
                mActivityID = args.getInt(ARG_LIVEPAGE_ACTIVITY_ID);
                mIsGym = (mWorkout != null) && Utilities.isGymWorkout(mActivityID);
                mIsShoot = (mWorkout != null) && Utilities.isShooting(mActivityID);
            }
            if (args.containsKey(ARG_LIVEPAGE_IMAGE_ID)) mParam1 = args.getInt(ARG_LIVEPAGE_IMAGE_ID);
            if (args.containsKey(ARG_LIVEPAGE_TEXT_ID)) mParam2 = args.getString(ARG_LIVEPAGE_TEXT_ID);
            if (args.containsKey(ARG_LIVEPAGE_COLOR_ID)) mParam3 = args.getInt(ARG_LIVEPAGE_COLOR_ID);
            if (args.containsKey(ARG_LIVEPAGE_WORKOUT_ID)) mSessionID = args.getLong(ARG_LIVEPAGE_WORKOUT_ID);
            if (args.containsKey(ARG_LIVEPAGE_WORKOUTSET_ID)) mSetID = args.getLong(ARG_LIVEPAGE_WORKOUTSET_ID);
            if (args.containsKey(ARG_LIVEPAGE_DURATION_ID)) mGoalDuration = args.getInt(ARG_LIVEPAGE_DURATION_ID);
            if (args.containsKey(ARG_LIVEPAGE_STEPS_ID)) mGoalSteps = args.getInt(ARG_LIVEPAGE_STEPS_ID);
        }
        try {
            messagesViewModel = ViewModelProviders.of(requireActivity()).get(MessagesViewModel.class);
            sessionViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        }catch (IllegalStateException ise){
            Log.e(TAG, "illegal state on view models " + ise.getLocalizedMessage());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mRecordingAnimation.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_live_relative, container, false);
        Context context = container.getContext();
        mTopImageView = rootView.findViewById(R.id.live_image);
        mRelativeLayoutLive = rootView.findViewById(R.id.live_relative_main);
        mImageViewOne = rootView.findViewById(R.id.live_image_one);
        mImageViewOne.setTag(TAG_PAUSE);
        mImageViewTwo = rootView.findViewById(R.id.live_image_two);
        mImageViewTwo.setBackgroundResource(R.drawable.recording_animation);
        mRecordingAnimation = (AnimationDrawable) mImageViewTwo.getBackground();
        mImageViewThree = rootView.findViewById(R.id.live_image_three);
       // mImageViewFour = rootView.findViewById(R.id.live_image_four);
        mTextView = rootView.findViewById(R.id.live_text);
        mMessageText = rootView.findViewById(R.id.live_message);
        mMessageText2 = rootView.findViewById(R.id.live_message2);
        mMessageText3 = rootView.findViewById(R.id.live_message3);
        mLiveMessageText2 = rootView.findViewById(R.id.live_text_two);
        if (mParam1 > 0){
            mTopImageView.setImageResource(mParam1);
            mTopImageView.setColorFilter(ContextCompat.getColor(context, android.R.color.white), PorterDuff.Mode.SRC_IN);
            mActivityDrawable = ContextCompat.getDrawable(context, mParam1);
            mActivityDrawable.setColorFilter(ContextCompat.getColor(context, android.R.color.white), PorterDuff.Mode.SRC_IN);
            mRecordingAnimation.addFrame(mActivityDrawable, (int)Constants.ANIM_FADE_TIME_MS);
            //mImageViewTwo.setImageResource(mParam1);
            //mImageViewTwo.setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.white), PorterDuff.Mode.SRC_IN);
        }
        if (!mParam2.equals("ATrackIt")) {
            if (mParam3 > 0) {
              //  rootView.findViewById(R.id.live_linear_title).setBackgroundColor(getActivity().getColor(mParam3));
            }
            // crucial setting text

            if (mParam2.length() > 0) mTextView.setText(mParam2);

        } else {
            mParam3 = ContextCompat.getColor(context, R.color.colorAccent);
            // crucial setting text
            if (mParam2.length() > 0) mTextView.setText(mParam2);
            rootView.findViewById(R.id.live_linear_title).setBackgroundColor(ContextCompat.getColor(context, mParam3));
        }

        messagesViewModel.OtherMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                mMessageText2.setText(s);
            }
        });
        messagesViewModel.getStepsMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mMessageText3.setText(s);
            }
        });
        messagesViewModel.CurrentMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (mMessageSource == MSG_HOME) mMessageText.setText(s);
            }
        });

        messagesViewModel.getBpmMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (mMessageSource == MSG_BMP) mMessageText.setText(s);
            }
        });
        messagesViewModel.getLocationMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (mMessageSource == MSG_LOCATION) mMessageText.setText(s);
            }
        });
        messagesViewModel.getStepsMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (mMessageSource == MSG_STEP) mMessageText.setText(s);
            }
        });
        mMessageText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String sPackageName = getActivity().getPackageName();
                    int resId = getResources().getIdentifier("ic_clock","drawable",sPackageName);
                    if (mMessageSource == MSG_LOCATION) mMessageSource = MSG_HOME; else mMessageSource++;
                    mListener.onLiveFragmentInteraction(5, mMessageSource, mParam2, mParam3);
                    switch (mMessageSource){
                        case MSG_HOME:
                            resId = getResources().getIdentifier("ic_clock","drawable",sPackageName);
                            sPackageName = messagesViewModel.getMessage();
                            break;
                        case MSG_BMP:
                            resId = getResources().getIdentifier("ic_heart_solid","drawable",sPackageName);
                            sPackageName = messagesViewModel.getBpmMsg().getValue();
                            break;
                        case MSG_STEP:
                            resId = getResources().getIdentifier("ic_footsteps_silhouette_variant","drawable",sPackageName);
                            sPackageName = messagesViewModel.getStepsMsg().getValue();
                            break;
                        case MSG_LOCATION:
                            resId = getResources().getIdentifier("ic_placeholder","drawable",sPackageName);
                            sPackageName = messagesViewModel.getLocationMsg().getValue();
                            break;
                    }
                    final int resourceId = resId;
                    if (sPackageName == null) sPackageName = getString(R.string.requesting_data);
                    if (sPackageName.length() == 0) sPackageName = getString(R.string.requesting_data);
                    final String sMsg = sPackageName; // latest value from each msg type
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mMessageText.setCompoundDrawablesWithIntrinsicBounds(resourceId,0,0,0);
                            if ((sMsg != null) && (sMsg.length() > 0)) mMessageText.setText(sMsg);
                        }
                    });
                }
            }
        );
        mMessageText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLiveFragmentInteraction(6, mParam1,mMessageText2.getText().toString(),mParam3);
            }
        }
        );
        mTextView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mListener.onLiveFragmentInteraction(0, mParam1,mParam2,mParam3);
             }
         }
        );

        // image callbacks to MainActivity

        mImageViewOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLiveFragmentInteraction(1, mParam1, mParam2, mParam3);
                if ((mImageViewOne.getTag().equals(TAG_PAUSE))) {
                    mImageViewOne.setImageResource(R.drawable.ic_play_button_white);
                    mImageViewOne.setTag(TAG_RESUME);
                }else{
                    mImageViewOne.setImageResource(R.drawable.ic_pause_white);
                    mImageViewOne.setTag(TAG_PAUSE);
                }
            }
        });

        mImageViewTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLiveFragmentInteraction(2, mParam1, mParam2, mParam3);
            }
        });
        mImageViewTwo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onLiveFragmentInteraction(2, mParam1, "long", mParam3);
                return true;
            }
        });
        mImageViewThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLiveFragmentInteraction(3, mParam1, mParam2, mParam3);
            }
        });
        mImageViewThree.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onLiveFragmentInteraction(3, mParam1, "long", mParam3);
                return true;
            }
        });
        return rootView;
    }

    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        Log.d(TAG, "LiveFragment.onEnterAmbient() " + ambientDetails);
        boolean IsLowBitAmbient =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false);
        boolean DoBurnInProtection =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false);
        if (getActivity() == null) return;
        Resources resources = getActivity().getResources();
        // Convert image to grayscale for ambient mode.
        mImageViewColorFilter = mImageViewOne.getColorFilter();
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0); // grey scale it
        mBackgroundColorFilter = mRelativeLayoutLive.getBackground().getColorFilter();
        mBackground2ColorFilter =  mRelativeLayoutLive.findViewById(R.id.live_linear_title).getBackground().getColorFilter();

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        mImageViewOne.setColorFilter(filter);
        mImageViewTwo.setBackground(mActivityDrawable);
        mImageViewTwo.setColorFilter(filter);
        mImageViewThree.setColorFilter(filter);
        if (IsLowBitAmbient || DoBurnInProtection) {
            int backCol = resources.getColor(R.color.colorAmbientBackground,null);
            int foreCol = resources.getColor(R.color.colorAmbientForeground, null);
            mRelativeLayoutLive.setBackgroundColor(backCol);
            mRelativeLayoutLive.findViewById(R.id.live_linear_title).setBackgroundColor(backCol);
            mMessageText.setTextColor(foreCol);
            mMessageText.getPaint().setAntiAlias(false);
            mMessageText2.setTextColor(foreCol);
            mMessageText2.getPaint().setAntiAlias(false);
        }
    }

    /** Restores the UI to active (non-ambient) mode. */
    public void onExitAmbientInFragment() {
        Log.d(TAG, "LiveFragment.onExitAmbient()");
        if (getActivity() == null) return;
        Resources resources = getActivity().getResources();
        int foreCol = resources.getColor(R.color.semiWhite, null);
        mRelativeLayoutLive.getBackground().setColorFilter(mBackgroundColorFilter);
        mRelativeLayoutLive.findViewById(R.id.live_linear_title).getBackground().setColorFilter(mBackground2ColorFilter);
        mMessageText.setTextColor(foreCol);
        mMessageText.getPaint().setAntiAlias(true);
        mMessageText2.setTextColor(foreCol);
        mMessageText2.getPaint().setAntiAlias(true);
        mImageViewTwo.setBackground(mRecordingAnimation);
        mImageViewOne.setColorFilter(mImageViewColorFilter);
        mImageViewTwo.setColorFilter(mImageViewColorFilter);
        mImageViewThree.setColorFilter(mImageViewColorFilter);
        mRecordingAnimation.start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach DONE");
        if (context instanceof OnLiveFragmentInteractionListener) {
            mListener = (OnLiveFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLiveFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach DONE");
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnLiveFragmentInteractionListener {
        void onLiveFragmentInteraction(int src, int id, String text, int color);
        void onLiveFragmentComplete(int id);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mActivityID = savedInstanceState.getInt(LiveFragment.ARG_LIVEPAGE_ACTIVITY_ID);
            mParam1 = savedInstanceState.getInt(LiveFragment.ARG_LIVEPAGE_IMAGE_ID);
            mParam2 = savedInstanceState.getString(LiveFragment.ARG_LIVEPAGE_TEXT_ID);
            mParam3 = savedInstanceState.getInt(LiveFragment.ARG_LIVEPAGE_COLOR_ID);
            mSessionID= savedInstanceState.getLong(LiveFragment.ARG_LIVEPAGE_WORKOUT_ID);
            mGoalSteps = savedInstanceState.getInt(ARG_LIVEPAGE_STEPS_ID);
            mGoalDuration = savedInstanceState.getInt(ARG_LIVEPAGE_DURATION_ID);
            mSessionLive = (mWorkout.start > 0);

        }
        mWorkout = sessionViewModel.getWorkout().getValue();
        mSet = sessionViewModel.getWorkoutSet().getValue();

        mSessionLive = (mWorkout.start > 0);
        mRestDuration = (mWorkout == null) ? 0 : mWorkout.rest_duration;

        if ((messagesViewModel.getMessageCount() > 0) && (mMessageText != null)) mMessageText.setText(messagesViewModel.getMessage());
        if ((messagesViewModel.getOtherMessageCount() > 0) && (mMessageText2 != null)) mMessageText2.setText(messagesViewModel.getOtherMessage());


        mListener.onLiveFragmentComplete(mParam1);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LiveFragment.ARG_LIVEPAGE_ACTIVITY_ID, mActivityID);
        outState.putInt(LiveFragment.ARG_LIVEPAGE_IMAGE_ID, mParam1);
        outState.putString(LiveFragment.ARG_LIVEPAGE_TEXT_ID, mParam2);
        outState.putInt(LiveFragment.ARG_LIVEPAGE_COLOR_ID, mParam3);
        outState.putLong(LiveFragment.ARG_LIVEPAGE_WORKOUT_ID, mSessionID);
        outState.putLong(LiveFragment.ARG_LIVEPAGE_WORKOUTSET_ID, mSetID);
        outState.putInt(ARG_LIVEPAGE_STEPS_ID, mGoalSteps);
        outState.putInt(ARG_LIVEPAGE_DURATION_ID, mGoalDuration);
    }
}
