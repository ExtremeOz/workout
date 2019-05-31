package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.model.Utilities;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomScoreDialogFragment.onCustomScoreSelected} interface
 * to handle interaction events.
 * Use the {@link CustomScoreDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CustomScoreDialogFragment extends DialogFragment {
    public static final String TAG = "CustomScoreDialogFragment";
    public static final String ARG_TYPE = "ARG_TYPE";
    public static final String ARG_PRESET = "ARG_PRESET";
    public static final String ARG_SET = "ARG_SET";
    private onCustomScoreSelected mListener;
    private int mLoadType;
    private int mPresetValue;
    private int mSetIndex;
    protected TextView mType_Title;
    protected LinearLayout mLinear_Vertical;
    protected Button mPrevious_Button;
    protected Button mNext_Button;

    protected Button mOne_Button;
    protected Button mTwo_Button;
    protected Button mThree_Button;
    protected Button mFour_Button;
    protected Button mFive_Button;
    protected Button mSix_Button;
    protected Button mSeven_Button;
    protected Button mEight_Button;
    protected Button mNine_Button;
    protected Button mTen_Button;
    protected Button mEleven_Button;
    protected Button mTwelve_Button;
    protected Button mThirteen_Button;
    protected Button mFourteen_Button;
    protected Button mFifteen_Button;

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
    public static CustomScoreDialogFragment newInstance(int iType, int iPreset, int iSet) {
        final CustomScoreDialogFragment fragment = new CustomScoreDialogFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, iType);
        args.putInt(ARG_PRESET, iPreset);
        args.putInt(ARG_SET, iSet);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLoadType = getArguments().getInt(ARG_TYPE);
            mPresetValue = getArguments().getInt(ARG_PRESET);
            mSetIndex = getArguments().getInt(ARG_SET);
        }else {
            if ((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_TYPE))) {
                mLoadType = savedInstanceState.getInt(ARG_TYPE);
                mPresetValue = savedInstanceState.getInt(ARG_PRESET);
                mSetIndex = savedInstanceState.getInt(ARG_SET);
            }
        }
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(ARG_TYPE, mLoadType);
        outState.putInt(ARG_PRESET, mPresetValue);
        outState.putInt(ARG_SET, mSetIndex);
        super.onSaveInstanceState(outState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_customscore, container, false);
        mType_Title = rootView.findViewById(R.id.score_title);

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
        mOne_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        if (mLoadType == Constants.SELECTION_GOAL_STEPS) mOne_Button.setText(R.string.label_first_step_value);
        mTwo_Button = rootView.findViewById(R.id.score2_button);
        mTwo_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        mThree_Button = rootView.findViewById(R.id.score3_button);
        mThree_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        mFour_Button = rootView.findViewById(R.id.score4_button);
        mFour_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        mFive_Button = rootView.findViewById(R.id.score5_button);
        mFive_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        mSix_Button = rootView.findViewById(R.id.score6_button);
        mSix_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        mSeven_Button = rootView.findViewById(R.id.score7_button);
        mSeven_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        mEight_Button = rootView.findViewById(R.id.score8_button);
        mEight_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        mNine_Button = rootView.findViewById(R.id.score9_button);
        mNine_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        mTen_Button = rootView.findViewById(R.id.score10_button);
        mTen_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        mEleven_Button = rootView.findViewById(R.id.score11_button);
        mEleven_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        mTwelve_Button = rootView.findViewById(R.id.score12_button);
        mTwelve_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        mThirteen_Button = rootView.findViewById(R.id.score13_button);
        mThirteen_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        mFourteen_Button = rootView.findViewById(R.id.score14_button);
        mFourteen_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        mFifteen_Button = rootView.findViewById(R.id.score15_button);
        mFifteen_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedButton(v);
            }
        });
        return rootView;
    }

    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        if (getActivity() != null) {
            Context context = getContext();

            int bgColor = ContextCompat.getColor(context, R.color.colorAmbientBackground);
            int fgColor = ContextCompat.getColor(context, R.color.colorAmbientForeground);
            mType_Title.setBackgroundColor(bgColor);
            mType_Title.setTextColor(fgColor);
            mType_Title.getPaint().setAntiAlias(false);
            Drawable img = ContextCompat.getDrawable(context,R.drawable.ic_left_arrow_outline);
            mPrevious_Button.setCompoundDrawables(null,null, img,null);
            img = ContextCompat.getDrawable(context,R.drawable.ic_right_arrow_outline);
            mNext_Button.setCompoundDrawables(img,null,null,null);
            mLinear_Vertical.setVisibility(View.INVISIBLE);
        }
    }

    /** Restores the UI to active (non-ambient) mode. */
    public void onExitAmbientInFragment() {
        if (getActivity() != null) {
            Context context = getContext();
            int bgColor = ContextCompat.getColor(context, R.color.colorAccent);
            int fgColor = ContextCompat.getColor(context, R.color.white);
            mType_Title.setBackgroundColor(bgColor);
            mType_Title.setTextColor(fgColor);
            mType_Title.getPaint().setAntiAlias(true);
            Drawable img = ContextCompat.getDrawable(context,R.drawable.ic_left_arrow);
            mPrevious_Button.setCompoundDrawables(null,null, img,null);
            img = ContextCompat.getDrawable(context,R.drawable.ic_right_arrow);
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
        Resources resources = getActivity().getResources();
        stepValue = firstValue;
        if (stepValue <= 1)
            mOne_Button.setCompoundDrawableTintList(resources.getColorStateList(R.color.light_grey,null));
        else
            mOne_Button.setCompoundDrawableTintList(resources.getColorStateList(R.color.white,null));
        sTemp = Integer.toString(stepValue);
        mOne_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mTwo_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mThree_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mFour_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mFive_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mSix_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mSeven_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mEight_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mNine_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mTen_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mEleven_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mTwelve_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mThirteen_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mFourteen_Button.setText(sTemp);
        if (mLoadType == Constants.SELECTION_GOAL_STEPS){
            stepValue += 100;
        }else
            stepValue += 1;
        sTemp = Integer.toString(stepValue);
        mFifteen_Button.setText(sTemp);


    }

    private void clickedButton(View btn){
        Button b = (Button)btn;
        String sValue = b.getText().toString();
        int iValue = Integer.parseInt(sValue);
        mListener.onCustomScoreSelected(mLoadType, iValue, sValue, mSetIndex);
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
            mOne_Button.setCompoundDrawableTintList(resources.getColorStateList(R.color.white,null));
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mOne_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mTwo_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mThree_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mFour_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mFive_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mSix_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mSeven_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mEight_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mNine_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mTen_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mEleven_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mTwelve_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mThirteen_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mFourteen_Button.setText(sTemp);
            if (mLoadType == Constants.SELECTION_GOAL_STEPS){
                stepValue += 100;
            }else
                stepValue += 1;
            sTemp = Integer.toString(stepValue);
            mFifteen_Button.setText(sTemp);
        }
        sTemp = mOne_Button.getText().toString();
        stepValue = Integer.parseInt(sTemp);
        if (mPresetValue == stepValue){
            mOne_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mTwo_Button.getText().toString());
        if (mPresetValue == stepValue){
            mTwo_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mThree_Button.getText().toString());
        if (mPresetValue == stepValue){
            mThree_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mFour_Button.getText().toString());
        if (mPresetValue == stepValue){
            mFour_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mFive_Button.getText().toString());
        if (mPresetValue == stepValue){
            mFive_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mSix_Button.getText().toString());
        if (mPresetValue == stepValue){
            mSix_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mSeven_Button.getText().toString());
        if (mPresetValue == stepValue){
            mSeven_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mEight_Button.getText().toString());
        if (mPresetValue == stepValue){
            mEight_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mNine_Button.getText().toString());
        if (mPresetValue == stepValue){
            mNine_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mTen_Button.getText().toString());
        if (mPresetValue == stepValue){
            mTen_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mEleven_Button.getText().toString());
        if (mPresetValue == stepValue){
            mEleven_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mTwelve_Button.getText().toString());
        if (mPresetValue == stepValue){
            mTwelve_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mThirteen_Button.getText().toString());
        if (mPresetValue == stepValue){
            mThirteen_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mFourteen_Button.getText().toString());
        if (mPresetValue == stepValue){
            mFourteen_Button.setSelected(true);
            return;
        }
        stepValue = Integer.parseInt(mFifteen_Button.getText().toString());
        if (mPresetValue == stepValue){
            mFifteen_Button.setSelected(true);
        }
        return;
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
