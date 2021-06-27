package com.a_track_it.workout.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.a_track_it.workout.R;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.Utilities;
import com.google.android.material.button.MaterialButton;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomScoreDialogFragment.onCustomScoreSelected} interface
 * to handle interaction events.
 * Use the {@link CustomScoreDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomScoreDialogFragment extends DialogFragment implements AmbientInterface {
    public static final String TAG = "CustomScoreDialogFragment";
    public static final String ARG_TYPE = "ARG_TYPE";
    public static final String ARG_PRESET = "ARG_PRESET";
    public static final String ARG_SET = "ARG_SET";
    private onCustomScoreSelected mListener;
    private int mLoadType;
    private int mPresetValue;
    private int mSetIndex;
    private ScrollView mScrollView;
    private TextView mType_Title;
    private ConstraintLayout mLinear_Vertical;
    private Button mPrevious_Button;
    private Button mNext_Button;
    private MaterialButton mOne_Button;
    private MaterialButton mTwo_Button;
    private MaterialButton mThree_Button;
    private MaterialButton mFour_Button;
    private MaterialButton mFive_Button;
    private MaterialButton mSix_Button;
    private MaterialButton mSeven_Button;
    private MaterialButton mEight_Button;
    private MaterialButton mNine_Button;
    private MaterialButton mTen_Button;
    private MaterialButton mEleven_Button;
    private MaterialButton mTwelve_Button;
    private MaterialButton mThirteen_Button;
    private MaterialButton mFourteen_Button;
    private MaterialButton mFifteen_Button;
    private  View.OnClickListener myClicker = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            MaterialButton b = (MaterialButton)view;
            String sValue = b.getText().toString();
            int iValue = Integer.parseInt(sValue);
            b.setChecked(true);
            mListener.onCustomScoreSelected(mLoadType, iValue, sValue, mSetIndex);
            getDialog().dismiss();
        }
    };

    public CustomScoreDialogFragment() {
        // Required empty public constructor
    }
    public interface onCustomScoreSelected {
        void onCustomScoreSelected(int type, long position, String title, int iSetIndex);
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param iType Load Type
     * @param iPreset Preset Value.
     * @return A new instance of fragment CustomScoreDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CustomScoreDialogFragment newInstance(int iType, int iPreset) {
        final CustomScoreDialogFragment fragment = new CustomScoreDialogFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, iType);
        args.putInt(ARG_PRESET, iPreset);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLoadType = getArguments().getInt(ARG_TYPE);
            mPresetValue = getArguments().getInt(ARG_PRESET);
        }else {
            if ((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_TYPE))) {
                mLoadType = savedInstanceState.getInt(ARG_TYPE);
                mPresetValue = savedInstanceState.getInt(ARG_PRESET);
            }
        }
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(ARG_TYPE, mLoadType);
        outState.putInt(ARG_PRESET, mPresetValue);
        super.onSaveInstanceState(outState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView;
//        if (mLoadType != Constants.SELECTION_GOAL_STEPS)
//            rootView = inflater.inflate(R.layout.fragment_customscore, container, false);
//        else
        rootView = inflater.inflate(R.layout.fragment_customscore, container, false);
        mType_Title = rootView.findViewById(R.id.score_title);
        mScrollView = rootView.findViewById(R.id.score_scrollview);
        mPrevious_Button = rootView.findViewById(R.id.score_prev);
        mPrevious_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonText(-1);
            }
        });
        mNext_Button = rootView.findViewById(R.id.score_next);
        mNext_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonText(1);
            }
        });
        mLinear_Vertical = rootView.findViewById(R.id.linear_score_vertical);
        String sTemp = Utilities.SelectionTypeToString(getActivity(), mLoadType);
        if (sTemp.length() > 0) mType_Title.setText(sTemp);
        // now the buttons
        mOne_Button = rootView.findViewById(R.id.score1_button);
        mOne_Button.setOnClickListener(myClicker);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS) mOne_Button.setText(R.string.label_first_step_value);
        mTwo_Button = rootView.findViewById(R.id.score2_button);
        mTwo_Button.setOnClickListener(myClicker);
        mThree_Button = rootView.findViewById(R.id.score3_button);
        mThree_Button.setOnClickListener(myClicker);
        mFour_Button = rootView.findViewById(R.id.score4_button);
        mFour_Button.setOnClickListener(myClicker);
        mFive_Button = rootView.findViewById(R.id.score5_button);
        mFive_Button.setOnClickListener(myClicker);
        mSix_Button = rootView.findViewById(R.id.score6_button);
        mSix_Button.setOnClickListener(myClicker);
        mSeven_Button = rootView.findViewById(R.id.score7_button);
        mSeven_Button.setOnClickListener(myClicker);
        mEight_Button = rootView.findViewById(R.id.score8_button);
        mEight_Button.setOnClickListener(myClicker);
        mNine_Button = rootView.findViewById(R.id.score9_button);
        mNine_Button.setOnClickListener(myClicker);
        mTen_Button = rootView.findViewById(R.id.score10_button);
        mTen_Button.setOnClickListener(myClicker);
        mEleven_Button = rootView.findViewById(R.id.score11_button);
        mEleven_Button.setOnClickListener(myClicker);
        mTwelve_Button = rootView.findViewById(R.id.score12_button);
        mTwelve_Button.setOnClickListener(myClicker);
        mThirteen_Button = rootView.findViewById(R.id.score13_button);
        mThirteen_Button.setOnClickListener(myClicker);
        mFourteen_Button = rootView.findViewById(R.id.score14_button);
        mFourteen_Button.setOnClickListener(myClicker);
        mFifteen_Button = rootView.findViewById(R.id.score15_button);
        mFifteen_Button.setOnClickListener(myClicker);
/*        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            mOne_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mTwo_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mThree_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mFour_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mFive_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mSix_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mSeven_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mEight_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mNine_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mTen_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mEleven_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mTwelve_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mThirteen_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mFourteen_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            mFifteen_Button.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
        }*/
        return rootView;
    }

    @Override
    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        if (getActivity() != null) {
            Context context = getContext();

            int bgColor = ContextCompat.getColor(context, R.color.ambientBackground);
            int fgColor = ContextCompat.getColor(context, R.color.ambientForeground);
            mType_Title.setBackgroundColor(bgColor);
            mType_Title.setTextColor(fgColor);
            mType_Title.getPaint().setAntiAlias(false);
            Drawable img = AppCompatResources.getDrawable(context,R.drawable.ic_left_arrow_outline);
            mPrevious_Button.setCompoundDrawables(null,null, img,null);
            img = AppCompatResources.getDrawable(context,R.drawable.ic_right_arrow_outline);
            mNext_Button.setCompoundDrawables(img,null,null,null);
            mLinear_Vertical.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void loadDataAndUpdateScreen(){

    }
    /** Restores the UI to active (non-ambient) mode. */
    @Override
    public void onExitAmbientInFragment() {
        if (getActivity() != null) {
            Context context = getContext();
            int bgColor = ContextCompat.getColor(context, R.color.primaryDarkColor);
            int fgColor = ContextCompat.getColor(context, R.color.white);
            mType_Title.setBackgroundColor(bgColor);
            mType_Title.setTextColor(fgColor);
            mType_Title.getPaint().setAntiAlias(true);
            Drawable img = AppCompatResources.getDrawable(context,R.drawable.ic_left_arrow);
            mPrevious_Button.setCompoundDrawables(null,null, img,null);
            img = AppCompatResources.getDrawable(context,R.drawable.ic_right_arrow);
            mNext_Button.setCompoundDrawables(img,null,null,null);
            mLinear_Vertical.setVisibility(View.VISIBLE);
        }
    }
    private void setButtonText(int direction){
        String sTemp = mOne_Button.getText().toString();
        int firstValue = Integer.parseInt(sTemp);
        int stepValue = 0;

        if ((direction < 0) &&
                (((mLoadType != Constants.SELECTION_GOAL_STEPS) && (firstValue > 15))
                        || ((mLoadType == Constants.SELECTION_GOAL_STEPS) && (firstValue > 1500)))){
            if (mLoadType == Constants.SELECTION_GOAL_STEPS)
                firstValue -= 1500;
            else
                firstValue -= 15;
        }else
            if (direction > 0){
                if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                    firstValue += 1500;
                }else
                    firstValue += 15;
            }else
                return;
      //  Resources resources = getActivity().getResources();
        stepValue = firstValue;
        sTemp = Integer.toString(stepValue);
        mOne_Button.setText(sTemp);
        mOne_Button.setSelected(stepValue == mPresetValue);
        mOne_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mOne_Button.getTop()));
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mTwo_Button.setText(sTemp);
        mTwelve_Button.setSelected(stepValue == mPresetValue);
        mTwelve_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mTwo_Button.getTop()));
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mThree_Button.setText(sTemp);
        mThree_Button.setSelected(stepValue == mPresetValue);
        mThree_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.scrollTo(0,mThree_Button.getTop());
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mFour_Button.setText(sTemp);
        mFour_Button.setSelected(stepValue == mPresetValue);
        mFour_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.scrollTo(0,mFour_Button.getTop());
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mFive_Button.setText(sTemp);
        mFive_Button.setSelected(stepValue == mPresetValue);
        mFive_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.scrollTo(0,mFive_Button.getTop());
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mSix_Button.setText(sTemp);
        mSix_Button.setSelected(stepValue == mPresetValue);
        mSix_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.scrollTo(0,mSix_Button.getTop());
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mSeven_Button.setText(sTemp);
        mSeven_Button.setSelected(stepValue == mPresetValue);
        mSeven_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.scrollTo(0,mSeven_Button.getTop());
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mEight_Button.setText(sTemp);
        mEight_Button.setSelected(stepValue == mPresetValue);
        mEight_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.scrollTo(0,mEight_Button.getTop());
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mNine_Button.setText(sTemp);
        mNine_Button.setSelected(stepValue == mPresetValue);
        mNine_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.scrollTo(0,mNine_Button.getTop());
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mTen_Button.setText(sTemp);
        mTen_Button.setSelected(stepValue == mPresetValue);
        mTen_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.scrollTo(0,mTen_Button.getTop());
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mEleven_Button.setText(sTemp);
        mEleven_Button.setSelected(stepValue == mPresetValue);
        mEleven_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.scrollTo(0,mEleven_Button.getTop());
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mTwelve_Button.setText(sTemp);
        mTwelve_Button.setSelected(stepValue == mPresetValue);
        mTwelve_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.scrollTo(0,mTwelve_Button.getTop());
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mThirteen_Button.setText(sTemp);
        mThirteen_Button.setSelected(stepValue == mPresetValue);
        mThirteen_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.scrollTo(0,mThirteen_Button.getTop());
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mFourteen_Button.setText(sTemp);
        mFourteen_Button.setSelected(stepValue == mPresetValue);
        mFourteen_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.scrollTo(0,mFourteen_Button.getTop());
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mFifteen_Button.setText(sTemp);
        mFifteen_Button.setSelected(stepValue == mPresetValue);
        mFifteen_Button.setChecked(stepValue == mPresetValue);
        if (stepValue == mPresetValue) mScrollView.scrollTo(0,mFifteen_Button.getTop());


    }

    private void clickedButton(View btn){

    }

    @Override
    public void onStart() {
        super.onStart();
        int lastValue = Integer.parseInt(mFifteen_Button.getText().toString());
        String sTemp = mOne_Button.getText().toString();
        int firstValue = Integer.parseInt(sTemp);
        int stepValue; Resources resources;
        // adjust starting value
        while (mPresetValue > lastValue){
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                firstValue += 1500;
                lastValue += 1500;
            }else{
                firstValue += 15;
                lastValue += 15;
            }
        }
        if (firstValue != 1){
            try {
                resources = getActivity().getResources();
                if (resources == null) return;
            }catch (Exception e){
                return;
            }

            stepValue = firstValue;
            //mOne_Button.setCompoundDrawableTintList(resources.getColorStateList(R.color.white,null));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mOne_Button.setText(sTemp);
            mOne_Button.setSelected(stepValue == mPresetValue);
            mOne_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() ->  mScrollView.scrollTo(0,mOne_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mTwo_Button.setText(sTemp);
            mTwo_Button.setSelected(stepValue == mPresetValue);
            mTwo_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mTwo_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mThree_Button.setText(sTemp);
            mThree_Button.setSelected(stepValue == mPresetValue);
            mThree_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mThree_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mFour_Button.setText(sTemp);
            mFour_Button.setSelected(stepValue == mPresetValue);
            mFour_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mFour_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mFive_Button.setText(sTemp);
            mFive_Button.setSelected(stepValue == mPresetValue);
            mFive_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mFive_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mSix_Button.setText(sTemp);
            mSix_Button.setSelected(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mSix_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mSeven_Button.setText(sTemp);
            mSeven_Button.setSelected(stepValue == mPresetValue);
            mSeven_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mSeven_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mEight_Button.setText(sTemp);
            mEight_Button.setSelected(stepValue == mPresetValue);
            mEight_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mEight_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mNine_Button.setText(sTemp);
            mNine_Button.setSelected(stepValue == mPresetValue);
            mNine_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() ->  mScrollView.scrollTo(0,mNine_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mTen_Button.setText(sTemp);
            mTen_Button.setSelected(stepValue == mPresetValue);
            mTen_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mTen_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mEleven_Button.setText(sTemp);
            mEleven_Button.setSelected(stepValue == mPresetValue);
            mEleven_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mEleven_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mTwelve_Button.setText(sTemp);
            mTwelve_Button.setSelected(stepValue == mPresetValue);
            mTwelve_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mTwelve_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mThirteen_Button.setText(sTemp);
            mThirteen_Button.setSelected(stepValue == mPresetValue);
            mThirteen_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mThirteen_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mFourteen_Button.setText(sTemp);
            mFourteen_Button.setSelected(stepValue == mPresetValue);
            mFourteen_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mFourteen_Button.getTop()));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mFifteen_Button.setText(sTemp);
            mFifteen_Button.setSelected(stepValue == mPresetValue);
            mFifteen_Button.setChecked(stepValue == mPresetValue);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mFifteen_Button.getTop()));
        }
        sTemp = mOne_Button.getText().toString();
        stepValue = Integer.parseInt(sTemp);
        if (mPresetValue == stepValue){
            mOne_Button.setSelected(true);
            mOne_Button.setChecked(true);
            return;
        }
        stepValue = Integer.parseInt(mTwo_Button.getText().toString());
        if (mPresetValue == stepValue){
            mTwo_Button.setSelected(true);
            mTwo_Button.setChecked(true);
            return;
        }
        stepValue = Integer.parseInt(mThree_Button.getText().toString());
        if (mPresetValue == stepValue){
            mThree_Button.setSelected(true);
            mThree_Button.setChecked(true);
            return;
        }
        stepValue = Integer.parseInt(mFour_Button.getText().toString());
        if (mPresetValue == stepValue){
            mFour_Button.setSelected(true);
            mFour_Button.setChecked(true);
            return;
        }
        stepValue = Integer.parseInt(mFive_Button.getText().toString());
        if (mPresetValue == stepValue){
            mFive_Button.setSelected(true);
            mFive_Button.setChecked(true);
            return;
        }
        stepValue = Integer.parseInt(mSix_Button.getText().toString());
        if (mPresetValue == stepValue){
            mSix_Button.setSelected(true);
            mSix_Button.setChecked(true);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mSix_Button.getTop()));
            return;
        }
        stepValue = Integer.parseInt(mSeven_Button.getText().toString());
        if (mPresetValue == stepValue){
            mSeven_Button.setSelected(true);
            mSeven_Button.setChecked(true);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mSeven_Button.getTop()));
            return;
        }
        stepValue = Integer.parseInt(mEight_Button.getText().toString());
        if (mPresetValue == stepValue){
            mEight_Button.setSelected(true);
            mEight_Button.setChecked(true);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mEight_Button.getTop()));
            return;
        }
        stepValue = Integer.parseInt(mNine_Button.getText().toString());
        if (mPresetValue == stepValue){
            mNine_Button.setSelected(true);
            mNine_Button.setChecked(true);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mNine_Button.getTop()));
            return;
        }
        stepValue = Integer.parseInt(mTen_Button.getText().toString());
        if (mPresetValue == stepValue){
            mTen_Button.setSelected(true);
            mTen_Button.setChecked(true);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mTen_Button.getTop()));
            return;
        }
        stepValue = Integer.parseInt(mEleven_Button.getText().toString());
        if (mPresetValue == stepValue){
            mEleven_Button.setSelected(true);
            mEleven_Button.setChecked(true);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mEleven_Button.getTop()));
            return;
        }
        stepValue = Integer.parseInt(mTwelve_Button.getText().toString());
        if (mPresetValue == stepValue){
            mTwelve_Button.setSelected(true);
            mTwelve_Button.setChecked(true);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mTwelve_Button.getTop()));
            return;
        }
        stepValue = Integer.parseInt(mThirteen_Button.getText().toString());
        if (mPresetValue == stepValue){
            mThirteen_Button.setSelected(true);
            mThirteen_Button.setChecked(true);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mThirteen_Button.getTop()));
            return;
        }
        stepValue = Integer.parseInt(mFourteen_Button.getText().toString());
        if (mPresetValue == stepValue){
            mFourteen_Button.setSelected(true);
            mFourteen_Button.setChecked(true);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mFourteen_Button.getTop()));
            return;
        }
        stepValue = Integer.parseInt(mFifteen_Button.getText().toString());
        if (mPresetValue == stepValue){
            mFifteen_Button.setSelected(true);
            mFifteen_Button.setChecked(true);
            if (stepValue == mPresetValue) mScrollView.post(() -> mScrollView.scrollTo(0,mFifteen_Button.getTop()));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onCustomScoreSelected) {
            mListener = (onCustomScoreSelected) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCustomListItemSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    // to access the selected type for setting preferences etc.
    public int getLoadType(){
        return mLoadType;
    }
}
