package com.a_track_it.workout.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.a_track_it.workout.R;
import com.a_track_it.workout.common.Constants;

public class CustomAlertDialog extends DialogFragment {
    private static int action;
    private static String mMessageText;
    private static CustomAlertDialog instance;
    private static ICustomConfirmDialog callback;
    private static long confirmDuration;

    public CustomAlertDialog() {
        super();
    }
    public static CustomAlertDialog newInstance(int questionType, String sMessage,long lConfirmDuration, ICustomConfirmDialog parentActivity) {
        if (instance == null) instance = new CustomAlertDialog();
        action = questionType;
        mMessageText = sMessage;
        callback = parentActivity;
        confirmDuration = lConfirmDuration;
        return instance;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.alertdialog_confirm_cancel, container, false);
        TextView tv = rootView.findViewById(R.id.circular_text_message);
        if (tv != null) tv.setText(mMessageText);
        Context context = getContext();
        if((context != null) && context instanceof ICustomConfirmDialog) {
            callback = (ICustomConfirmDialog) context;
        }
        // dialogBuilder.setTitle(message_text);
        final androidx.wear.widget.CircularProgressLayout circularProgressLayout = rootView.findViewById(R.id.circular_progress);
        final ImageView imageView = (ImageView) rootView.findViewById(R.id.image_view);
        if (action == Constants.ACTION_QUICK_REPORT)
            imageView.setImageResource(R.drawable.ic_a_outlined);
/*        final ImageView imageView = (ImageView) rootView.findViewById(R.id.image_view);
        Resources res = getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_close_white);
        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res, bitmap);
        dr.setCircular(true);
        dr.setCornerRadius(Math.max(bitmap.getWidth(), bitmap.getHeight()) / 2.0f);
        imageView.setImageDrawable(dr);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setAlpha(0.5F);*/
        if (circularProgressLayout != null){
            circularProgressLayout.setTotalTime(confirmDuration);
            circularProgressLayout.setOnTimerFinishedListener(circularProgressLayout1 -> {
                if (callback != null) callback.onCustomConfirmButtonClicked(action,1);
                getDialog().dismiss();
            });
            circularProgressLayout.setOnClickListener(view -> {
                circularProgressLayout.stopTimer();
                if (callback != null) callback.onCustomConfirmButtonClicked(action,0);
                getDialog().dismiss();

            });
            circularProgressLayout.startTimer();
        }

        return rootView;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Log.d("CustomerAlertDialog", "onDismiss");
        super.onDismiss(dialog);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if( context instanceof ICustomConfirmDialog) {
            callback = (ICustomConfirmDialog) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
