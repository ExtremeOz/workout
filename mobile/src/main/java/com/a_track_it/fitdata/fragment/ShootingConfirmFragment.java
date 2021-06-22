package com.a_track_it.fitdata.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutMeta;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.a_track_it.fitdata.common.user_model.LiveDataTimerViewModel;
import com.a_track_it.fitdata.common.user_model.MessagesViewModel;
import com.a_track_it.fitdata.common.user_model.SavedStateViewModel;
import com.google.android.material.button.MaterialButton;
import com.richpath.RichPath;
import com.richpath.RichPathView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShootingConfirmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShootingConfirmFragment extends DialogFragment {
    public static final String LOG_TAG = ShootingConfirmFragment.class.getSimpleName();
    public static final String ARG_ICON_ID = "ICON_ID";
    public static final String ARG_COLOR_ID = "COLOR_ID";
    public static final String ARG_SET = "SET_ID";
    public static final String ARG_WORKOUT = "WORKOUT_ID";
    public static final String ARG_TIMED = "TIMED_ID";

    private int mIcon;
    private int mColor;
    private WorkoutMeta mWorkoutMeta;
    private WorkoutSet mWorkoutSet;
    private Workout mWorkout;
    private int mSetCount;
    private long restDuration;
    private int currentShotIndex;
    private String currentPath;
    private String currentXY;
    private FragmentInterface mListener;
    private MessagesViewModel messagesViewModel;
    private ImageButton mShotButton;
    private TextView textView_message1;
    private TextView textView_message2;
    private List<String> mScoreCard = new ArrayList<>();
    private List<String> mScoreXY = new ArrayList<>();
    private List<String> mMetaScores = new ArrayList<>(); // meta = all ends scorecards
    private List<String> mMetaXY = new ArrayList<>(); // meta = all ends xy
    private View rootView;
    private SavedStateViewModel mSavedStateViewModel;
    private LiveDataTimerViewModel mTimerViewModel;
    private boolean bRestAutoStart;
    private boolean bUseTimedRest;
    private boolean hasAlarmed = false;

    public ShootingConfirmFragment() {
        // Required empty public constructor
    }

       public static ShootingConfirmFragment newInstance(int icon, int color, WorkoutSet set, Workout workout, long rest, WorkoutMeta workoutMeta) {
        final ShootingConfirmFragment fragment = new ShootingConfirmFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ICON_ID, icon);
        args.putInt(ARG_COLOR_ID, color);
        args.putParcelable(ARG_SET, set);
        args.putParcelable(ARG_WORKOUT, workout);
        args.putParcelable(WorkoutMeta.class.getSimpleName(), workoutMeta);
        args.putLong(ARG_TIMED, rest);
        fragment.setArguments(args);
        return fragment;
    }
    public void SetMessage_Text(String sMsg, int index){
        if ((index == 1) &&(textView_message1 != null)) textView_message1.setText(sMsg);
        if ((index == 2) &&(textView_message2 != null)) textView_message2.setText(sMsg);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedStateViewModel = new ViewModelProvider(requireActivity()).get(SavedStateViewModel.class);
        messagesViewModel = new ViewModelProvider(requireActivity()).get(MessagesViewModel.class);
        mTimerViewModel = new ViewModelProvider(requireActivity()).get(LiveDataTimerViewModel.class);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            mIcon = bundle.getInt(ARG_ICON_ID);
            mColor = bundle.getInt(ARG_COLOR_ID);
            restDuration = bundle.getLong(ARG_TIMED);
            mWorkoutSet = bundle.getParcelable(ARG_SET);
            mWorkout = bundle.getParcelable(ARG_WORKOUT);
            mWorkoutMeta = bundle.getParcelable(WorkoutMeta.class.getSimpleName());
            if ((mWorkoutMeta == null) && (mSavedStateViewModel.getActiveWorkoutMeta().getValue()!= null)) mWorkoutMeta = mSavedStateViewModel.getActiveWorkoutMeta().getValue();
            if (mWorkoutMeta != null) {
                if (mWorkoutMeta.score_card.length() > 0) {
                    String itemsEnds[] = mWorkoutMeta.score_card.split(Constants.SHOT_DELIM);
                    mMetaScores = new ArrayList<>(Arrays.asList(itemsEnds));
                } else
                    mMetaScores = Arrays.asList(new String[mWorkoutMeta.setCount]);
            }
            if (mSavedStateViewModel.getActiveWorkoutSet().getValue() != null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet != null) {
                if ((mWorkoutSet.score_card != null) && mWorkoutSet.score_card.length() > 0) {
                    String items[] = mWorkoutSet.score_card.split(Constants.SHOT_DELIM);
                    mScoreCard = new ArrayList<>(Arrays.asList(items));
                } else {
                    mScoreCard = Arrays.asList(new String[mWorkoutMeta.shotsPerEnd]);
                    for (int i = 0; i < mScoreCard.size(); i++) {
                        if (mScoreCard.get(i) == null) mScoreCard.set(i, Constants.ATRACKIT_EMPTY);
                    }

                }
                if ((mWorkoutSet.per_end_xy != null) && mWorkoutSet.per_end_xy.length() > 0) {
                    String itemXY[] = mWorkoutSet.per_end_xy.split(Constants.SHOT_DELIM);  // using x:y, x:y
                    mScoreXY = new ArrayList<>(Arrays.asList(itemXY));
                } else {
                    mScoreXY = Arrays.asList(new String[mWorkoutMeta.shotsPerEnd]);
                    for (int i = 0; i < mScoreXY.size(); i++) {
                        if (mScoreXY.get(i) == null) mScoreXY.set(i, Constants.ATRACKIT_EMPTY);
                    }
                }
                currentShotIndex = 0;
                currentPath = ((mScoreCard.size() >= (currentShotIndex + 1)) ? mScoreCard.get(currentShotIndex): Constants.ATRACKIT_EMPTY);
                currentXY = ((mScoreXY.size() >= (currentShotIndex + 1)) ? mScoreXY.get(currentShotIndex): Constants.ATRACKIT_EMPTY);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String sTemp;
        Context context = getContext();
        rootView = inflater.inflate(R.layout.dialog_shooting_confirm, container, false);
        final androidx.appcompat.widget.Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.confirm_shooting_prompt);
            if (toolbar != null){
                Drawable drawableUnChecked = AppCompatResources.getDrawable(context,R.drawable.ic_close_white);
                Utilities.setColorFilter(drawableUnChecked, ContextCompat.getColor(context, R.color.secondaryTextColor));
                toolbar.setNavigationIcon(drawableUnChecked);
            }
            toolbar.setNavigationOnClickListener(v -> mListener.OnFragmentInteraction(android.R.id.home,0,null));
        }
        final RichPathView targetRichPath = rootView.findViewById(R.id.ic_target);
        rootView.findViewById(R.id.image_button_shot1).setOnClickListener(shotClicker);
        rootView.findViewById(R.id.image_button_shot2).setOnClickListener(shotClicker);
        rootView.findViewById(R.id.image_button_shot3).setOnClickListener(shotClicker);
        rootView.findViewById(R.id.image_button_shot4).setOnClickListener(shotClicker);
        rootView.findViewById(R.id.image_button_shot5).setOnClickListener(shotClicker);
        rootView.findViewById(R.id.image_button_shot6).setOnClickListener(shotClicker);
        rootView.findViewById(R.id.image_button_shot7).setOnClickListener(shotClicker);
        rootView.findViewById(R.id.image_button_shot8).setOnClickListener(shotClicker);
        rootView.findViewById(R.id.image_button_shot9).setOnClickListener(shotClicker);
        rootView.findViewById(R.id.image_button_shot10).setOnClickListener(shotClicker);

        try {
            targetRichPath.findRichPathByName("pathX").setOnPathClickListener(pathClickListener);
            targetRichPath.findRichPathByName("path10").setOnPathClickListener(pathClickListener);
            targetRichPath.findRichPathByName("path9").setOnPathClickListener(pathClickListener);
            targetRichPath.findRichPathByName("path8").setOnPathClickListener(pathClickListener);
            targetRichPath.findRichPathByName("path7").setOnPathClickListener(pathClickListener);
            targetRichPath.findRichPathByName("path6").setOnPathClickListener(pathClickListener);
            targetRichPath.findRichPathByName("path5").setOnPathClickListener(pathClickListener);
            targetRichPath.findRichPathByName("path4").setOnPathClickListener(pathClickListener);
            targetRichPath.findRichPathByName("path3").setOnPathClickListener(pathClickListener);
            targetRichPath.findRichPathByName("path2").setOnPathClickListener(pathClickListener);
            targetRichPath.findRichPathByName("path1").setOnPathClickListener(pathClickListener);
            targetRichPath.setOnTouchListener((v, event) -> {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN){
                    float downx = event.getX();
                    float downy = event.getY();
                    if (mShotButton != null) {
                        mShotButton.setX(downx);
                        mShotButton.setY(downy);
                        mShotButton.setVisibility(ImageButton.VISIBLE);
                    }
                    if ((mScoreXY != null) && (mScoreXY.size() > 0) && (mScoreXY.size() > currentShotIndex))
                        currentXY = mScoreXY.get(currentShotIndex);
                    else currentXY = Constants.ATRACKIT_EMPTY;

                    String setXY = downx + Constants.SHOT_XY_DELIM + downy;  // set the XY
                    if (!setXY.equals(currentXY) && (mScoreXY.size() > currentShotIndex)){
                        currentXY = setXY;
                        mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                        mScoreXY.set(currentShotIndex, setXY);
                        mWorkoutSet.per_end_xy = String.join(Constants.SHOT_DELIM, mScoreXY);
                        mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                    }
                    return true;
                }
                return false;
            });

        }catch(NullPointerException ne){
            Log.e(ShootingConfirmFragment.class.getSimpleName(), " path click null pointer");
        }
        final MaterialButton setNoScore_btn = rootView.findViewById(R.id.no_score_button);
        setNoScore_btn.setOnClickListener(v -> {
            mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            currentPath = Constants.SHOT_SCORE; currentXY = Constants.SHOT_XY.replace("0", "-1"); // flag as no-score rather than score of 0
            mScoreCard.set(currentShotIndex, currentPath);
            mScoreXY.set(currentShotIndex, currentXY);
            mWorkoutSet.score_card = String.join(Constants.SHOT_DELIM, mScoreCard);
            mWorkoutSet.per_end_xy = String.join(Constants.SHOT_DELIM, mScoreXY);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
        });
        final ApplicationPreferences appPrefs = ApplicationPreferences.getPreferences(context);
        final String sUserId = appPrefs.getLastUserID();
        final UserPreferences userPrefs = UserPreferences.getPreferences(context, sUserId);

        final ImageButton one_btn = rootView.findViewById(R.id.arrow1_button);
        one_btn.setOnClickListener(shotButtonClickListener);
        one_btn.setOnLongClickListener(shotButtonLongClickListener);
       // String sPackage = Constants.ATRACKIT_ATRACKIT_CLASS;
        one_btn.setEnabled(mWorkoutMeta.shotsPerEnd >= 1);
        one_btn.setSelected(currentShotIndex == 0);
        one_btn.setTag(1);

        final ImageButton two_btn = rootView.findViewById(R.id.arrow2_button);
        two_btn.setOnClickListener(shotButtonClickListener);
        two_btn.setOnLongClickListener(shotButtonLongClickListener);
        two_btn.setEnabled(mWorkoutMeta.shotsPerEnd >= 2);
        two_btn.setSelected(currentShotIndex == 1);
        two_btn.setTag(2);

        final ImageButton three_btn = rootView.findViewById(R.id.arrow3_button);
        three_btn.setEnabled(mWorkoutMeta.shotsPerEnd >= 3);
        three_btn.setOnClickListener(shotButtonClickListener);
        three_btn.setOnLongClickListener(shotButtonLongClickListener);
        three_btn.setSelected(currentShotIndex == 2);
        three_btn.setTag(3);

        final ImageButton four_btn = rootView.findViewById(R.id.arrow4_button);
        four_btn.setOnClickListener(shotButtonClickListener);
        four_btn.setOnLongClickListener(shotButtonLongClickListener);
        four_btn.setEnabled(mWorkoutMeta.shotsPerEnd >= 4);
        four_btn.setSelected(currentShotIndex == 3);
        four_btn.setTag(4);

        final ImageButton five_btn = rootView.findViewById(R.id.arrow5_button);
        five_btn.setOnClickListener(shotButtonClickListener);
        five_btn.setOnLongClickListener(shotButtonLongClickListener);
        five_btn.setEnabled(mWorkoutMeta.shotsPerEnd >= 5);
        five_btn.setSelected(currentShotIndex == 4);
        five_btn.setTag(5);

        final ImageButton six_btn = rootView.findViewById(R.id.arrow6_button);
        six_btn.setOnClickListener(shotButtonClickListener);
        six_btn.setOnLongClickListener(shotButtonLongClickListener);
        six_btn.setEnabled(mWorkoutMeta.shotsPerEnd >= 6);
        six_btn.setSelected(currentShotIndex == 5);
        six_btn.setTag(6);

        final ImageButton seven_btn = rootView.findViewById(R.id.arrow7_button);
        seven_btn.setOnClickListener(shotButtonClickListener);
        seven_btn.setOnLongClickListener(shotButtonLongClickListener);
        seven_btn.setEnabled(mWorkoutMeta.shotsPerEnd >= 7);
        seven_btn.setSelected(currentShotIndex == 6);
        seven_btn.setTag(7);

        final ImageButton eight_btn = rootView.findViewById(R.id.arrow8_button);
        eight_btn.setOnClickListener(shotButtonClickListener);
        eight_btn.setOnLongClickListener(shotButtonLongClickListener);
        eight_btn.setEnabled(mWorkoutMeta.shotsPerEnd >= 8);
        eight_btn.setSelected(currentShotIndex == 7);
        eight_btn.setTag(8);

        final ImageButton nine_btn = rootView.findViewById(R.id.arrow9_button);
        nine_btn.setOnClickListener(shotButtonClickListener);
        nine_btn.setOnLongClickListener(shotButtonLongClickListener);
        nine_btn.setEnabled(mWorkoutMeta.shotsPerEnd >= 9);
        nine_btn.setSelected(currentShotIndex == 8);
        nine_btn.setTag(9);

        final ImageButton ten_btn = rootView.findViewById(R.id.arrow10_button);
        ten_btn.setOnClickListener(shotButtonClickListener);
        ten_btn.setOnLongClickListener(shotButtonLongClickListener);
        ten_btn.setEnabled(mWorkoutMeta.shotsPerEnd >= 10);
        ten_btn.setSelected(currentShotIndex == 9);
        ten_btn.setTag(10);

        final Button exit_btn  = rootView.findViewById(R.id.archery_confirm_exit_button);
        exit_btn.setTag(Constants.UID_archery_confirm_exit_button);

        exit_btn.setOnClickListener(v -> {
            boolean bIsValid = false;
            if (mWorkoutSet == null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet != null){
                bIsValid = !mWorkoutSet.per_end_xy.contains(Constants.SHOT_XY);  // all set - no 0:0 values
            }
            if (bIsValid){
                String items[] = mWorkoutSet.score_card.split(Constants.SHOT_DELIM);
                mScoreCard =  new ArrayList<>(Arrays.asList(items));
                int scoreVal = 0;
                for (String score : mScoreCard){
                    if (score.equals(Constants.SHOT_X))
                        scoreVal += 10;
                    else {
                        try {
                            scoreVal += Integer.parseInt(score);
                        }catch (Exception e){
                            scoreVal += 0;
                        }
                    }
                }
                mWorkoutSet.scoreTotal = scoreVal;
                mWorkoutSet.repCount = mScoreCard.size();
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                getActivity().getSupportFragmentManager().popBackStack();
                myClicker.onClick(v);
            }else{
                String items[] = mWorkoutSet.per_end_xy.split(Constants.SHOT_DELIM);
                mScoreXY =  new ArrayList<>(Arrays.asList(items));
                int iEnd = 1;
                for (String sXY : mScoreXY){
                    if (sXY.equals(Constants.SHOT_XY))
                        break;
                    else
                        iEnd += 1;
                }
                final String sMsg = "Missing shot entry " + iEnd;
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, sMsg,Toast.LENGTH_SHORT).show();
                });
            }
        });
        final Button next_btn = rootView.findViewById(R.id.archery_confirm_next_end_button);
        next_btn.setTag(Constants.UID_archery_confirm_next_end_button);
        next_btn.setOnClickListener(v -> {
            boolean bIsValid = false;
            if (mWorkoutSet == null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            if (mWorkoutSet != null){
                bIsValid = !mWorkoutSet.per_end_xy.contains(Constants.SHOT_XY);  // all set
            }
            if (bIsValid){
                String items[] = mWorkoutSet.score_card.split(Constants.SHOT_DELIM);
                mScoreCard =  new ArrayList<>(Arrays.asList(items));
                int scoreVal = 0;
                for (String score : mScoreCard){
                    if (score.equals(Constants.SHOT_X))
                        scoreVal += 10;
                    else {
                        try {
                            scoreVal += Integer.parseInt(score);
                        }catch (Exception e){
                            scoreVal += 0;
                        }
                    }
                }
                mWorkoutSet.scoreTotal = scoreVal;
                mWorkoutSet.repCount = mScoreCard.size();
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                getActivity().getSupportFragmentManager().popBackStack();
                if (v.getTag() != null) mListener.OnFragmentInteraction((int)v.getTag(),0, Constants.ATRACKIT_EMPTY );
            }else{
                String items[] = mWorkoutSet.per_end_xy.split(Constants.SHOT_DELIM);
                mScoreXY =  new ArrayList<>(Arrays.asList(items));
                int iEnd = 1;
                for (String sXY : mScoreXY){
                    if (sXY.equals(Constants.SHOT_XY))
                        break;
                    else
                        iEnd += 1;
                }
                final String sMsg = "Missing shot entry " + iEnd;
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, sMsg,Toast.LENGTH_SHORT).show();
                });
            }
        });
        final Chronometer chronometer = rootView.findViewById(R.id.archery_chronometer_confirm);
        mSetCount = mWorkoutMeta.setCount;
        mShotButton = rootView.findViewById(R.id.image_button_shot1);
        ViewGroup.LayoutParams params;
        LinearLayout linearLayout;
        if (mWorkoutMeta.shotsPerEnd < 4){
            rootView.findViewById(R.id.archery_confirm_row3_container).setVisibility(View.GONE);
            rootView.findViewById(R.id.archery_confirm_row4_container).setVisibility(View.GONE);
            linearLayout = rootView.findViewById(R.id.archery_confirm_row2_container);
            params = linearLayout.getLayoutParams();
            params.height = params.height * 2;
            linearLayout.setLayoutParams(params);
            if (mWorkoutMeta.shotsPerEnd <= 2) rootView.findViewById(R.id.arrow3_button).setVisibility(View.GONE);
            if (mWorkoutMeta.shotsPerEnd == 1) rootView.findViewById(R.id.arrow2_button).setVisibility(View.GONE);
        }else
        if (mWorkoutMeta.shotsPerEnd < 8){
            rootView.findViewById(R.id.archery_confirm_row4_container).setVisibility(View.GONE);
            linearLayout = rootView.findViewById(R.id.archery_confirm_row2_container);
            params = linearLayout.getLayoutParams();
            int existing = params.height;
            params.height = params.height +(int)(existing / 2);
            linearLayout.setLayoutParams(params);
            rootView.findViewById(R.id.archery_confirm_row3_container).setLayoutParams(params);
            if (mWorkoutMeta.shotsPerEnd <= 6) rootView.findViewById(R.id.arrow7_button).setVisibility(View.GONE);
            if (mWorkoutMeta.shotsPerEnd <= 5) rootView.findViewById(R.id.arrow6_button).setVisibility(View.GONE);
            if (mWorkoutMeta.shotsPerEnd <= 4) rootView.findViewById(R.id.arrow5_button).setVisibility(View.GONE);
        }else{
            if (mWorkoutMeta.shotsPerEnd <= 9) rootView.findViewById(R.id.arrow10_button).setVisibility(View.GONE);
            if (mWorkoutMeta.shotsPerEnd == 8) rootView.findViewById(R.id.arrow6_button).setVisibility(View.GONE);
        }
        if (mWorkoutMeta.shotsPerEnd <= 9) ten_btn.setEnabled(false);
        if (mWorkoutMeta.shotsPerEnd <= 8) nine_btn.setEnabled(false);
        if (mWorkoutMeta.shotsPerEnd <= 7) eight_btn.setEnabled(false);
        if (mWorkoutMeta.shotsPerEnd <= 6) seven_btn.setEnabled(false);
        if (mWorkoutMeta.shotsPerEnd <= 5) six_btn.setEnabled(false);
        if (mWorkoutMeta.shotsPerEnd <= 4) five_btn.setEnabled(false);
        if (mWorkoutMeta.shotsPerEnd <= 3) four_btn.setEnabled(false);
        if (mWorkoutMeta.shotsPerEnd <= 2) three_btn.setEnabled(false);
        if (mWorkoutMeta.shotsPerEnd == 1) two_btn.setEnabled(false);
        textView_message1 = rootView.findViewById(R.id.archery_msg1_text);
        textView_message2 = rootView.findViewById(R.id.archery_msg2_text);
        if (mWorkoutSet.setCount == 0) mWorkoutSet.setCount = mSavedStateViewModel.getSetIndex();
        sTemp = Integer.toString(mWorkoutSet.setCount) + "/" + Integer.toString(mWorkoutMeta.setCount) + Constants.ATRACKIT_SPACE + getString(R.string.label_shoot_ends);
        textView_message1.setText(sTemp);
        if (mScoreCard != null && (mScoreCard.size() >0) && (mScoreCard.size() >= currentShotIndex))
            currentPath = ((mScoreCard.get(currentShotIndex) == null) ? Constants.ATRACKIT_EMPTY : mScoreCard.get(currentShotIndex));
        else currentPath = Constants.ATRACKIT_EMPTY;
        if (mScoreXY != null && (mScoreXY.size() > 0) && (mScoreXY.size() >= currentShotIndex))
            currentXY = ((mScoreXY.get(currentShotIndex) == null) ? Constants.ATRACKIT_EMPTY : mScoreXY.get(currentShotIndex));
        else currentXY = Constants.ATRACKIT_EMPTY;

        mSetCount = mWorkoutMeta.setCount;
        mTimerViewModel.getCurrentTime().observe(getViewLifecycleOwner(), aLong -> new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if  (textView_message2 != null)
                    if ((textView_message2.getTag() == null) || ((Integer) textView_message2.getTag() == 0))
                        textView_message2.setText(Utilities.getTimeString(aLong));
            }
        }));
        long pauseStart = mSavedStateViewModel.getPauseStart();
        bRestAutoStart = userPrefs.getRestAutoStart();
        bUseTimedRest = userPrefs.getTimedRest();
        //
        WorkoutSet workoutset = mSavedStateViewModel.getActiveWorkoutSet().getValue();
        if (workoutset == null) workoutset = new WorkoutSet(mWorkout);
        if (bUseTimedRest && (pauseStart > 0)){
            if ((workoutset.rest_duration != null) && workoutset.rest_duration > 0) {
                hasAlarmed = false;
                chronometer.setBase(pauseStart + workoutset.rest_duration);
                chronometer.setCountDown(true);
                chronometer.setOnChronometerTickListener(chronometer1 -> {
                    long startTime = chronometer1.getBase();
                    long now = System.currentTimeMillis();
                    if  (!hasAlarmed && ((startTime - now) < TimeUnit.MILLISECONDS.toMillis(500L))) {
                        chronometer1.setCountDown(false);
                        Intent vibrateIntent = new Intent(Constants.INTENT_VIBRATE);
                        vibrateIntent.putExtra(Constants.KEY_FIT_TYPE, 2);
                        messagesViewModel.addLiveIntent(vibrateIntent);
                        hasAlarmed = true;
                        // TODO - look at conditions
                        if (bRestAutoStart){
                            mListener.OnFragmentInteraction(Constants.UID_btnContinue, 0, null);
                        }
                    }
                });
            }else
                chronometer.setBase(pauseStart);
            chronometer.start();
        }
        mSavedStateViewModel.getActiveWorkoutSet().observe(getViewLifecycleOwner(), workoutSet -> {
            if (workoutSet == null) return;
            if ((workoutSet.score_card != null) && (workoutSet.score_card.length() > 0)){
                String items[] = workoutSet.score_card.split(Constants.SHOT_DELIM);
                mScoreCard =  new ArrayList<>(Arrays.asList(items));
            }else {
                mScoreCard = Arrays.asList(new String[mWorkoutMeta.shotsPerEnd]);
                for (int i=0; i < mScoreCard.size(); i++){
                    if (mScoreCard.get(i) == null) mScoreCard.set(i,Constants.ATRACKIT_EMPTY);
                }

            }
            if ((workoutSet.per_end_xy != null) && workoutSet.per_end_xy.length() > 0){
                String itemXY[] = mWorkoutSet.per_end_xy.split(Constants.SHOT_DELIM);  // using x:y, x:y
                mScoreXY =  new ArrayList<>(Arrays.asList(itemXY));
            }else {
                mScoreXY = Arrays.asList(new String[mWorkoutMeta.shotsPerEnd]);
            }
            for (int i=0; i < mScoreXY.size(); i++){
                if (mScoreXY.get(i) == null) mScoreXY.set(i,Constants.SHOT_XY);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                setButtonScores();
            });
        });
        if ((mWorkout != null) && (mSavedStateViewModel.getActiveWorkout().getValue() == null))
            mSavedStateViewModel.setActiveWorkout(mWorkout);
        if ((mWorkoutMeta != null) && (mSavedStateViewModel.getActiveWorkoutMeta().getValue() == null))
            mSavedStateViewModel.setActiveWorkoutMeta(mWorkoutMeta);
        if ((mWorkoutSet != null) && (mSavedStateViewModel.getActiveWorkoutSet().getValue() == null)){
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
        }else{
            new Handler(Looper.getMainLooper()).post(() -> {
                setButtonScores();
            });
        }
        return rootView;
    }

    // remove existing shot if clicking on imagebutton
    private View.OnClickListener shotClicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setVisibility(View.INVISIBLE);
            String sContentDescription = ((ImageButton)v).getContentDescription().toString();
            if (sContentDescription.length() > 0) {
                if (sContentDescription.contains(getString(R.string.label_shoot_shot).toLowerCase())) sContentDescription = sContentDescription.replace(getString(R.string.label_shoot_shot).toLowerCase(),Constants.ATRACKIT_EMPTY);
                if (sContentDescription.length() > 0){
                    currentShotIndex = Integer.parseInt(sContentDescription)-1;
                    mShotButton = (ImageButton) v;
                    if (mWorkoutSet == null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
                    currentPath = Constants.SHOT_SCORE; currentXY = Constants.SHOT_XY;
                    mScoreCard.set(currentShotIndex, currentPath);
                    mScoreXY.set(currentShotIndex, currentXY);
                    mWorkoutSet.score_card = String.join(Constants.SHOT_DELIM, mScoreCard);
                    mWorkoutSet.per_end_xy = String.join(Constants.SHOT_DELIM, mScoreXY);
                    mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
                }
            }
        }
    };
    // sets score from path name
    private RichPath.OnPathClickListener pathClickListener = new RichPath.OnPathClickListener() {
        @Override
        public void onClick(RichPath richPath) {
            String sPath = richPath.getName().replace(getString(R.string.label_shoot_path).toLowerCase(), Constants.ATRACKIT_EMPTY);
            if (mWorkoutSet == null) mWorkoutSet = mSavedStateViewModel.getActiveWorkoutSet().getValue();
            currentPath = sPath;
            if (currentShotIndex < mScoreCard.size()) {
                mScoreCard.set(currentShotIndex, sPath);
                mWorkoutSet.score_card = String.join(Constants.SHOT_DELIM, mScoreCard);
                mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            }
        }
    };
    // which shot selector
    View.OnClickListener shotButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int buttonIndex = ((Integer)view.getTag());
            currentShotIndex = (buttonIndex-1);
            switch (buttonIndex){
                case 1:
                    mShotButton = rootView.findViewById(R.id.image_button_shot1);
                    break;
                case 2:
                    mShotButton = rootView.findViewById(R.id.image_button_shot2);
                    break;
                case 3:
                    mShotButton = rootView.findViewById(R.id.image_button_shot3);
                    break;
                case 4:
                    mShotButton = rootView.findViewById(R.id.image_button_shot4);
                    break;
                case 5:
                    mShotButton = rootView.findViewById(R.id.image_button_shot5);
                    break;
                case 6:
                    mShotButton = rootView.findViewById(R.id.image_button_shot6);
                    break;
                case 7:
                    mShotButton = rootView.findViewById(R.id.image_button_shot7);
                    break;
                case 8:
                    mShotButton = rootView.findViewById(R.id.image_button_shot8);
                    break;
                case 9:
                    mShotButton = rootView.findViewById(R.id.image_button_shot9);
                    break;
                case 10:
                    mShotButton = rootView.findViewById(R.id.image_button_shot10);
                    break;
            }
            if (currentShotIndex < mScoreXY.size()) currentXY = mScoreXY.get(currentShotIndex);
            if (currentShotIndex < mScoreCard.size()) currentPath = mScoreCard.get(currentShotIndex);
            new Handler(Looper.getMainLooper()).post(() -> {
                setButtonScores();
            });
        }
    };

    View.OnLongClickListener shotButtonLongClickListener = new View.OnLongClickListener(){
        @Override
        public boolean onLongClick(View v) {
            int shotIndex = currentShotIndex;
            if ((v.getTag() != null) && (((Integer) v.getTag()) != shotIndex))
                shotIndex = (Integer) v.getTag();
            if ((shotIndex-1) != currentShotIndex) currentShotIndex = (shotIndex-1);
            currentPath = Constants.SHOT_SCORE;
            currentXY = Constants.SHOT_XY;
            mScoreCard.set(currentShotIndex, currentPath);
            mScoreXY.set(currentShotIndex, currentXY);
            mWorkoutSet.score_card = String.join(Constants.SHOT_DELIM, mScoreCard);
            mWorkoutSet.per_end_xy = String.join(Constants.SHOT_DELIM, mScoreXY);
            mSavedStateViewModel.setActiveWorkoutSet(mWorkoutSet);
            return true;
        }
    };
    private void setButtonScores(){
        String sTemp; String sPackage = Constants.ATRACKIT_ATRACKIT_CLASS;
        String setScore;
        ImageButton one_btn = rootView.findViewById(R.id.arrow1_button);
        one_btn.setSelected(currentShotIndex == 0);
        if ((mScoreCard.size() >= 1) && (mScoreCard.get(0) != null) && (mScoreCard.get(0).length() > 0)){
            setScore = mScoreCard.get(0).toLowerCase();
            sTemp = "ic_score_" + setScore;
            int ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
            if ((one_btn != null) && (ic_resource_id > 0)){
                one_btn.setImageResource(ic_resource_id);
            }
            if ((mScoreXY.size() >= 1) && (mScoreXY.get(0).length() > 1)){
                String itemXY[] = mScoreXY.get(0).split(Constants.SHOT_XY_DELIM);
                float shotX = Float.parseFloat(itemXY[0]);
                float shotY = Float.parseFloat(itemXY[1]);
                ImageButton imageButton = rootView.findViewById(R.id.image_button_shot1);
                if ((shotX > 0) && (shotY > 0)){
                    Log.e(LOG_TAG, mScoreXY.get(0) + " image shot1");
                    imageButton.setX(shotX); imageButton.setY(shotY);
                    imageButton.setVisibility(ImageButton.VISIBLE);
                }else {
                    imageButton.setVisibility(ImageButton.INVISIBLE);
                    if ((shotX < 0) && (shotY < 0)){
                        sTemp = "ic_number_zero_circle_white";
                        ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
                        if ((one_btn != null) && (ic_resource_id > 0)){
                            one_btn.setImageResource(ic_resource_id);
                        }
                    }
                }
            }
        }
        ImageButton two_btn = rootView.findViewById(R.id.arrow2_button);
        two_btn.setSelected(currentShotIndex == 1);
        if ((mScoreCard.size() >= 2) && (mScoreCard.get(1) != null) && (mScoreCard.get(1).length() > 0)){
            setScore = mScoreCard.get(1).toLowerCase();
            sTemp = "ic_score_" + setScore;
            int ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
            if ((two_btn != null) && (ic_resource_id > 0)){
                two_btn.setImageResource(ic_resource_id);
            }
            if ((mScoreXY.size() >= 2) && (mScoreXY.get(1).length() > 1)){
                String itemXY[] = mScoreXY.get(1).split(Constants.SHOT_XY_DELIM);
                float shotX = Float.parseFloat(itemXY[0]);
                float shotY = Float.parseFloat(itemXY[1]);
                ImageButton imageButton = rootView.findViewById(R.id.image_button_shot2);
                if ((shotX > 0) && (shotY > 0)){
                    Log.e(LOG_TAG, mScoreXY.get(1) + " image shot2");
                    imageButton.setX(shotX); imageButton.setY(shotY);
                    imageButton.setVisibility(ImageButton.VISIBLE);
                }else {
                    imageButton.setVisibility(ImageButton.INVISIBLE);
                    if ((shotX < 0) && (shotY < 0)){
                        sTemp = "ic_number_zero_circle_white";
                        ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
                        if ((two_btn != null) && (ic_resource_id > 0)){
                            two_btn.setImageResource(ic_resource_id);
                        }
                    }
                }
            }
        }
        ImageButton three_btn = rootView.findViewById(R.id.arrow3_button);
        three_btn.setSelected(currentShotIndex == 2);
        if ((mScoreCard.size() >= 3) && (mScoreCard.get(2) != null) && (mScoreCard.get(2).length() > 0)){
            setScore = mScoreCard.get(2).toLowerCase();
            sTemp = "ic_score_" + setScore;
            int ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
            if ((three_btn != null) && (ic_resource_id > 0)){
                three_btn.setImageResource(ic_resource_id);
            }
            if ((mScoreXY.size() >= 3) && (mScoreXY.get(2).length() > 1)){
                String itemXY[] = mScoreXY.get(2).split(Constants.SHOT_XY_DELIM);
                float shotX = Float.parseFloat(itemXY[0]);
                float shotY = Float.parseFloat(itemXY[1]);
                ImageButton imageButton = rootView.findViewById(R.id.image_button_shot3);
                if ((shotX > 0) && (shotY > 0)){
                    Log.e(LOG_TAG, mScoreXY.get(2) + " image shot3");
                    imageButton.setX(shotX); imageButton.setY(shotY);
                    imageButton.setVisibility(ImageButton.VISIBLE);
                }else{
                    imageButton.setVisibility(ImageButton.INVISIBLE);
                    if ((shotX < 0) && (shotY < 0)){
                        sTemp = "ic_number_zero_circle_white";
                        ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
                        if ((three_btn != null) && (ic_resource_id > 0)){
                            three_btn.setImageResource(ic_resource_id);
                        }
                    }
                }
            }
        }
        ImageButton four_btn = rootView.findViewById(R.id.arrow4_button);
        four_btn.setSelected(currentShotIndex == 3);
        if ((mScoreCard.size() >= 4) && (mScoreCard.get(3) != null) && (mScoreCard.get(3).length() > 0)){
            setScore = mScoreCard.get(3).toLowerCase();
            sTemp = "ic_score_" + setScore;
            int ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
            if ((four_btn != null) && (ic_resource_id > 0)){
                four_btn.setImageResource(ic_resource_id);
            }
            if ((mScoreXY.size() >= 4) && (mScoreXY.get(3).length() > 1)){
                String itemXY[] = mScoreXY.get(3).split(Constants.SHOT_XY_DELIM);
                float shotX = Float.parseFloat(itemXY[0]);
                float shotY = Float.parseFloat(itemXY[1]);
                ImageButton imageButton = rootView.findViewById(R.id.image_button_shot4);
                if ((shotX > 0) && (shotY > 0)){
                    imageButton.setX(shotX); imageButton.setY(shotY);
                    imageButton.setVisibility(ImageButton.VISIBLE);
                }else{
                    imageButton.setVisibility(ImageButton.INVISIBLE);
                    if ((shotX < 0) && (shotY < 0)){
                        sTemp = "ic_number_zero_circle_white";
                        ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
                        if ((four_btn != null) && (ic_resource_id > 0)){
                            four_btn.setImageResource(ic_resource_id);
                        }
                    }
                }
            }
        }
        ImageButton five_btn = rootView.findViewById(R.id.arrow5_button);
        five_btn.setSelected(currentShotIndex == 4);
        if ((mScoreCard.size() >= 5) && (mScoreCard.get(4) != null) && (mScoreCard.get(4).length() > 0)){
            setScore = mScoreCard.get(4).toLowerCase();
            sTemp = "ic_score_" + setScore;
            int ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
            if ((five_btn != null) && (ic_resource_id > 0)){
                five_btn.setImageResource(ic_resource_id);
            }
            if ((mScoreXY.size() >= 5) && (mScoreXY.get(4).length() > 1)){
                String itemXY[] = mScoreXY.get(0).split(Constants.SHOT_XY_DELIM);
                float shotX = Float.parseFloat(itemXY[0]);
                float shotY = Float.parseFloat(itemXY[1]);
                ImageButton imageButton = rootView.findViewById(R.id.image_button_shot5);
                if ((shotX > 0) && (shotY > 0)){
                    imageButton.setX(shotX); imageButton.setY(shotY);
                    imageButton.setVisibility(ImageButton.VISIBLE);
                }else{
                    imageButton.setVisibility(ImageButton.INVISIBLE);
                    if ((shotX < 0) && (shotY < 0)){
                        sTemp = "ic_number_zero_circle_white";
                        ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
                        if ((five_btn != null) && (ic_resource_id > 0)){
                            five_btn.setImageResource(ic_resource_id);
                        }
                    }
                }
            }
        }
        ImageButton six_btn = rootView.findViewById(R.id.arrow6_button);
        six_btn.setSelected(currentShotIndex == 5);
        if ((mScoreCard.size() >= 6) && (mScoreCard.get(5) != null) && (mScoreCard.get(5).length() > 0)){
            setScore = mScoreCard.get(5).toLowerCase();
            sTemp = "ic_score_" + setScore;
            int ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
            if ((six_btn != null) && (ic_resource_id > 0)){
                six_btn.setImageResource(ic_resource_id);
            }
            if ((mScoreXY.size() >= 6) && (mScoreXY.get(5).length() > 1)){
                String itemXY[] = mScoreXY.get(0).split(Constants.SHOT_XY_DELIM);
                float shotX = Float.parseFloat(itemXY[0]);
                float shotY = Float.parseFloat(itemXY[1]);
                ImageButton imageButton = rootView.findViewById(R.id.image_button_shot6);
                if ((shotX > 0) && (shotY > 0)){
                    imageButton.setX(shotX); imageButton.setY(shotY);
                    imageButton.setVisibility(ImageButton.VISIBLE);
                }else{
                    imageButton.setVisibility(ImageButton.INVISIBLE);
                    if ((shotX < 0) && (shotY < 0)){
                        sTemp = "ic_number_zero_circle_white";
                        ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
                        if ((six_btn != null) && (ic_resource_id > 0)){
                            six_btn.setImageResource(ic_resource_id);
                        }
                    }
                }
            }
        }
        ImageButton seven_btn = rootView.findViewById(R.id.arrow7_button);
        seven_btn.setSelected(currentShotIndex == 6);
        if ((mScoreCard.size() >= 7) && (mScoreCard.get(6) != null) && (mScoreCard.get(6).length() > 0)){
            setScore = mScoreCard.get(6).toLowerCase();
            sTemp = "ic_score_" + setScore;
            int ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
            if ((seven_btn != null) && (ic_resource_id > 0)){
                seven_btn.setImageResource(ic_resource_id);
            }
            if ((mScoreXY.size() >= 7) && (mScoreXY.get(6).length() > 1)){
                String itemXY[] = mScoreXY.get(0).split(Constants.SHOT_XY_DELIM);
                float shotX = Float.parseFloat(itemXY[0]);
                float shotY = Float.parseFloat(itemXY[1]);
                ImageButton imageButton = rootView.findViewById(R.id.image_button_shot7);
                if ((shotX > 0) && (shotY > 0)){
                    imageButton.setX(shotX); imageButton.setY(shotY);
                    imageButton.setVisibility(ImageButton.VISIBLE);
                }else{
                    imageButton.setVisibility(ImageButton.INVISIBLE);
                    if ((shotX < 0) && (shotY < 0)){
                        sTemp = "ic_number_zero_circle_white";
                        ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
                        if ((seven_btn != null) && (ic_resource_id > 0)){
                            seven_btn.setImageResource(ic_resource_id);
                        }
                    }
                }
            }
        }
        ImageButton eight_btn = rootView.findViewById(R.id.arrow8_button);
        eight_btn.setSelected(currentShotIndex == 7);
        if ((mScoreCard.size() >= 8) && (mScoreCard.get(7) != null) && (mScoreCard.get(7).length() > 0)){
            setScore = mScoreCard.get(7).toLowerCase();
            sTemp = "ic_score_" + setScore;
            int ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
            if ((eight_btn != null) && (ic_resource_id > 0)){
                eight_btn.setImageResource(ic_resource_id);
            }
            if ((mScoreXY.size() >= 8) && (mScoreXY.get(7).length() > 1)){
                String itemXY[] = mScoreXY.get(0).split(Constants.SHOT_XY_DELIM);
                float shotX = Float.parseFloat(itemXY[0]);
                float shotY = Float.parseFloat(itemXY[1]);
                ImageButton imageButton = rootView.findViewById(R.id.image_button_shot8);
                if ((shotX > 0) && (shotY > 0)){
                    imageButton.setX(shotX); imageButton.setY(shotY);
                    imageButton.setVisibility(ImageButton.VISIBLE);
                }else{
                    imageButton.setVisibility(ImageButton.INVISIBLE);
                    if ((shotX < 0) && (shotY < 0)){
                        sTemp = "ic_number_zero_circle_white";
                        ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
                        if ((eight_btn != null) && (ic_resource_id > 0)){
                            eight_btn.setImageResource(ic_resource_id);
                        }
                    }
                }
            }
        }
        ImageButton nine_btn = rootView.findViewById(R.id.arrow9_button);
        nine_btn.setSelected(currentShotIndex == 8);
        if ((mScoreCard.size() >= 9) && (mScoreCard.get(8) != null) && (mScoreCard.get(8).length() > 0)){
            setScore = mScoreCard.get(8).toLowerCase();
            sTemp = "ic_score_" + setScore;
            int ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
            if ((nine_btn != null) && (ic_resource_id > 0)){
                nine_btn.setImageResource(ic_resource_id);
            }
            if ((mScoreXY.size() >= 9) && (mScoreXY.get(8).length() > 1)){
                String itemXY[] = mScoreXY.get(0).split(Constants.SHOT_XY_DELIM);
                float shotX = Float.parseFloat(itemXY[0]);
                float shotY = Float.parseFloat(itemXY[1]);
                ImageButton imageButton = rootView.findViewById(R.id.image_button_shot9);
                if ((shotX > 0) && (shotY > 0)){
                    imageButton.setX(shotX); imageButton.setY(shotY);
                    imageButton.setVisibility(ImageButton.VISIBLE);
                }else{
                    imageButton.setVisibility(ImageButton.INVISIBLE);
                    if ((shotX < 0) && (shotY < 0)){
                        sTemp = "ic_number_zero_circle_white";
                        ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
                        if ((nine_btn != null) && (ic_resource_id > 0)){
                            nine_btn.setImageResource(ic_resource_id);
                        }
                    }
                }
            }
        }
        ImageButton ten_btn = rootView.findViewById(R.id.arrow10_button);
        ten_btn.setSelected(currentShotIndex == 9);
        if ((mScoreCard.size() >= 10) && (mScoreCard.get(9) != null) && (mScoreCard.get(9).length() > 0)){
            setScore = mScoreCard.get(9).toLowerCase();
            sTemp = "ic_score_" + setScore;
            int ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
            if ((ten_btn != null) && (ic_resource_id > 0)){
                ten_btn.setImageResource(ic_resource_id);
            }
            if ((mScoreXY.size() >= 10) && (mScoreXY.get(9).length() > 1)){
                String itemXY[] = mScoreXY.get(0).split(Constants.SHOT_XY_DELIM);
                float shotX = Float.parseFloat(itemXY[0]);
                float shotY = Float.parseFloat(itemXY[1]);
                ImageButton imageButton = rootView.findViewById(R.id.image_button_shot10);
                if ((shotX > 0) && (shotY > 0)){
                    imageButton.setX(shotX); imageButton.setY(shotY);
                    imageButton.setVisibility(ImageButton.VISIBLE);
                }else{
                    imageButton.setVisibility(ImageButton.INVISIBLE);
                    if ((shotX < 0) && (shotY < 0)){
                        sTemp = "ic_number_zero_circle_white";
                        ic_resource_id = getResources().getIdentifier(sTemp,Constants.ATRACKIT_DRAWABLE,sPackage);
                        if ((ten_btn != null) && (ic_resource_id > 0)){
                            ten_btn.setImageResource(ic_resource_id);
                        }
                    }
                }
            }
        }
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

}
