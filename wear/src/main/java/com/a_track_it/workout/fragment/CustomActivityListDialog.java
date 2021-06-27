package com.a_track_it.workout.fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.wear.ambient.AmbientModeSupport;

import com.a_track_it.workout.R;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.ReferencesTools;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.material.button.MaterialButton;

public class CustomActivityListDialog extends DialogFragment implements AmbientInterface {
    // Container Activity must implement this interface
    FragmentInterface mCallback;
    Context mContext;
    ConstraintLayout mConstraintLayout;

    public CustomActivityListDialog() {
        super();
    }

    public static CustomActivityListDialog getInstance(){
        final CustomActivityListDialog dialog = new CustomActivityListDialog();
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_customactivitylist, container, false);
        mConstraintLayout = rootView.findViewById(R.id.activityListConstraint);
        ReferencesTools mRefTools = ReferencesTools.getInstance();
        Context context = (getContext() != null) ? getContext() : mConstraintLayout.getContext();
        mRefTools.init(context);
        final String sActivityName = mRefTools.getFitnessActivityTextById(Constants.WORKOUT_TYPE_STRENGTH);
        final int iconActivity = mRefTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_STRENGTH);
        rootView.findViewById(R.id.home_action1_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemSelected((int)Constants.WORKOUT_TYPE_STRENGTH, view.getId(), sActivityName,
                        iconActivity, FitnessActivities.STRENGTH_TRAINING);
                CustomActivityListDialog.this.dismiss();
            }
        });
        final String sActivityName2 = mRefTools.getFitnessActivityTextById(Constants.WORKOUT_TYPE_ARCHERY);
        final int iconActivity2 = mRefTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_ARCHERY);
        rootView.findViewById(R.id.home_action2_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemSelected(Constants.SELECTION_TEMPLATE, view.getId(), sActivityName2,
                        iconActivity2, FitnessActivities.ARCHERY);
                CustomActivityListDialog.this.dismiss();
            }
        });
        final String sActivityName3 = mRefTools.getFitnessActivityTextById(Constants.WORKOUT_TYPE_AEROBICS);
        final int iconActivity3 =  mRefTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_AEROBICS);
        rootView.findViewById(R.id.home_action3_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemSelected(Constants.SELECTION_ACTIVITY_CARDIO,view.getId(), sActivityName3,
                       iconActivity3, FitnessActivities.AEROBICS);
                CustomActivityListDialog.this.dismiss();
            }
        });
        final String sActivityName4 = mRefTools.getFitnessActivityTextById(Constants.WORKOUT_TYPE_RUNNING);
        final int iconActivity4 = mRefTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_RUNNING);
        rootView.findViewById(R.id.home_action4_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemSelected(Constants.SELECTION_ACTIVITY_RUN, view.getId(), sActivityName4,
                        iconActivity4, FitnessActivities.RUNNING);
                CustomActivityListDialog.this.dismiss();
            }
        });
        final String sActivityName5 = mRefTools.getFitnessActivityTextById(Constants.SELECTION_ACTIVITY_SPORT);
        final int iconActivity5 = mRefTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_KAYAKING);
        rootView.findViewById(R.id.home_action5_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemSelected(Constants.SELECTION_ACTIVITY_SPORT,view.getId(),sActivityName5,
                        iconActivity5, FitnessActivities.SWIMMING);
                CustomActivityListDialog.this.dismiss();
            }
        });
        final String sActivityName6 = mRefTools.getFitnessActivityTextById(Constants.WORKOUT_TYPE_RUNNING);
        final int iconActivity6 = mRefTools.getFitnessActivityIconResById(68);
        rootView.findViewById(R.id.home_action6_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemSelected(Constants.SELECTION_ACTIVITY_RUN, view.getId(),sActivityName6,
                        iconActivity6, FitnessActivities.RUNNING);
                CustomActivityListDialog.this.dismiss();
            }
        });
        final String sActivityName7 = mRefTools.getFitnessActivityTextById(83);
        final int iconActivity7 = mRefTools.getFitnessActivityIconResById(83);
        rootView.findViewById(R.id.home_action7_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemSelected(Constants.SELECTION_ACTIVITY_WATER, view.getId(), sActivityName7,
                        iconActivity7, FitnessActivities.SWIMMING);
                CustomActivityListDialog.this.dismiss();
            }
        });
        final String sActivityName8 = mRefTools.getFitnessActivityTextById(16);
        final int iconActivity8 = mRefTools.getFitnessActivityIconResById(16);
        rootView.findViewById(R.id.home_action8_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemSelected(Constants.SELECTION_ACTIVITY_BIKE, view.getId(), sActivityName8,
                        iconActivity8, FitnessActivities.BIKING);
                CustomActivityListDialog.this.dismiss();
            }
        });
        final String sActivityName9 = mRefTools.getFitnessActivityTextById(24);
        final int iconActivity9 = mRefTools.getFitnessActivityIconResById(24);
        rootView.findViewById(R.id.home_action9_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemSelected(Constants.SELECTION_ACTIVITY_MISC, view.getId(), sActivityName9,
                        iconActivity9, FitnessActivities.DANCING);
                CustomActivityListDialog.this.dismiss();
            }
        });
        final String sActivityName10 = mRefTools.getFitnessActivityTextById(40);
        final int iconActivity10 = mRefTools.getFitnessActivityIconResById(40);
        rootView.findViewById(R.id.home_action10_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemSelected(Constants.SELECTION_ACTIVITY_SPORT, view.getId(), sActivityName10,
                        iconActivity10, FitnessActivities.SWIMMING);
                CustomActivityListDialog.this.dismiss();
            }
        });

        return  rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if( context instanceof FragmentInterface) {
            mCallback = (FragmentInterface) context;
            mContext = context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
        mContext = null;
    }

    @Override
    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        boolean IsLowBitAmbient =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false);
        boolean DoBurnInProtection =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false);
        Context context = getContext();
        int backColor = ContextCompat.getColor(context, R.color.ambientBackground);
        int foreColor = ContextCompat.getColor(context, R.color.ambientForeground);

        ColorStateList colorStateList = ContextCompat.getColorStateList(context, R.color.ambientForeground);
        ColorStateList colorStateList2 = ContextCompat.getColorStateList(context, R.color.ambientBackground);
        mConstraintLayout.setBackgroundTintList(colorStateList2);
        for (int i = 0; i < mConstraintLayout.getChildCount(); i++) {
            View v = mConstraintLayout.getChildAt(i);
            if (v instanceof com.google.android.material.button.MaterialButton) {
                ((MaterialButton) v).setTextColor(foreColor);
                ((MaterialButton) v).setBackgroundColor(backColor);
                ((MaterialButton) v).setIconTint(colorStateList);
            }
        }

    }

    @Override
    public void loadDataAndUpdateScreen(){

    }
    /** Restores the UI to active (non-ambient) mode. */
    @Override
    public void onExitAmbientInFragment() {
        Context context = getContext();
        ColorStateList backColor= ContextCompat.getColorStateList(context, R.color.primaryDarkColor);
        int foreColor = ContextCompat.getColor(context,R.color.primaryTextColor);
        int btnBackColor = ContextCompat.getColor(context,R.color.primaryColor);
        ColorStateList colorStateList = getResources().getColorStateList(R.color.primaryTextColor, getContext().getTheme());
        mConstraintLayout.setBackgroundTintList(backColor);
        for (int i = 0; i < mConstraintLayout.getChildCount(); i++) {
            View v = mConstraintLayout.getChildAt(i);
            if (v instanceof com.google.android.material.button.MaterialButton) {
                ((MaterialButton) v).setTextColor(foreColor);
                ((MaterialButton) v).setIconTint(colorStateList);
                ((MaterialButton) v).setBackgroundColor(btnBackColor);
            }
        }
    }
}
