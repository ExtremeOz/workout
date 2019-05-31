package com.a_track_it.fitdata.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.wear.ambient.AmbientModeSupport;

import com.a_track_it.fitdata.R;

public class CustomConfirmDialog extends DialogFragment{
    private static final int POSITIVE_RESULT = 1;
    private static final int NEGATIVE_RESULT = -1;
    private static final String ARG_QUESTION_TYPE = "q_type";
    private static final String ARG_MSG_TEXT = "msg_text";
    private static final String ARG_BTN_SINGLE = "btn_single";

    private static int mQuestionType;
    private static String mMessageText;
    private static ICustomConfirmDialog callback;
    private static CustomConfirmDialog instance = null;
    private static boolean singleButton = false;
    private ColorFilter mImageViewColorFilter;

    public CustomConfirmDialog(){
    }
    public static CustomConfirmDialog newInstance(int questionType, String sMessage, ICustomConfirmDialog parentActivity) {
        if (instance == null) instance = new CustomConfirmDialog();
        mQuestionType = questionType;
        mMessageText = sMessage;
        callback = parentActivity;
        return instance;
    }
    public void setQuestionType(int type){mQuestionType = type;}
    public void setSingleButton(boolean b){ singleButton = b;}
    public void setMessageText(String s){
        mMessageText = s;
    }

    public void setCallback(ICustomConfirmDialog parentActivity){
        callback = parentActivity;
    }

    public interface ICustomConfirmDialog {
        void onCustomConfirmButtonClicked(int question, int button);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //If don't want toolbar
        try {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }catch (Exception e){
        }
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirmation, container, false);
        if (mMessageText.length() > 0){
            TextView textView = view.findViewById(R.id.confirm_message);
            if (textView != null) textView.setText(mMessageText);
        }
        ImageButton negButton = view.findViewById(R.id.NegativeButton);
        if (negButton != null){
            negButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,NEGATIVE_RESULT);
                }
            });
            if (singleButton) negButton.setVisibility(View.GONE);
        }
        ImageButton posButton = view.findViewById(R.id.PositiveButton);
        if (posButton != null){
            posButton.requestFocus();
            posButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { if (callback != null) callback.onCustomConfirmButtonClicked(mQuestionType,POSITIVE_RESULT);
                }
            });
        }
        mImageViewColorFilter = posButton.getColorFilter();
        return view;
    }


/*    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null){
            mQuestionType = savedInstanceState.getInt(ARG_QUESTION_TYPE);
            mMessageText = savedInstanceState.getString(ARG_MSG_TEXT);
            singleButton = savedInstanceState.getBoolean(ARG_BTN_SINGLE);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(ARG_QUESTION_TYPE, mQuestionType);
        outState.putString(ARG_MSG_TEXT, mMessageText);
        outState.putBoolean(ARG_BTN_SINGLE, singleButton);
        super.onSaveInstanceState(outState);
    }*/

    public void onEnterAmbientInFragment(Bundle ambientDetails) {
        Context context = getContext();
        int bgColor = ContextCompat.getColor(context, R.color.colorAmbientBackground);
        int foreColor = ContextCompat.getColor(context, R.color.colorAmbientForeground);
        boolean IsLowBitAmbient =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_LOWBIT_AMBIENT, false);
        boolean DoBurnInProtection =
                ambientDetails.getBoolean(AmbientModeSupport.EXTRA_BURN_IN_PROTECTION, false);

        // Convert image to grayscale for ambient mode.
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        //mWearableRecyclerView.setColorFilter(filter);
        View rootView = getView();
        if (rootView.findViewById(R.id.confirm_message) != null){
            TextView textView = rootView.findViewById(R.id.confirm_message);
            textView.setTextColor(foreColor);
/*            Paint textPaint = textView.getPaint();
            textPaint.setAntiAlias(false);
            textPaint.setStyle(Paint.Style.STROKE);
            textPaint.setStrokeWidth(1);*/
        }
        ImageButton imageButton1 = rootView.findViewById(R.id.NegativeButton);
        imageButton1.getBackground().setColorFilter(filter);
        ImageButton imageButton2 = rootView.findViewById(R.id.PositiveButton);
        imageButton2.getBackground().setColorFilter(filter);
        if (DoBurnInProtection){
            imageButton1.setVisibility(View.INVISIBLE);
            imageButton2.setVisibility(View.INVISIBLE);
        }
    }

    /** Restores the UI to active (non-ambient) mode. */
    public void onExitAmbientInFragment() {
      //  Log.d(TAG, "CustomLisFragment.onExitAmbient()");
        View rootView = getView();
        Context context = getContext();
        int foreColor = ContextCompat.getColor(context, R.color.semiWhite);
        TextView textView = rootView.findViewById(R.id.confirm_message);
        textView.setTextColor(foreColor);
        ImageButton imageButton1 = rootView.findViewById(R.id.NegativeButton);
        imageButton1.setVisibility(View.VISIBLE);
        imageButton1.setColorFilter(mImageViewColorFilter);
        ImageButton imageButton2 = rootView.findViewById(R.id.PositiveButton);
        imageButton2.setVisibility(View.VISIBLE);
        imageButton2.setColorFilter(mImageViewColorFilter);
    }
}