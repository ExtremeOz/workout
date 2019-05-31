package com.a_track_it.fitdata.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.model.UserPreferences;

public class SplashActivity extends WearableActivity {
    private ConstraintLayout mLayout;
    private void doStartUp(){
        Context context = getApplicationContext();
        boolean SessionInProgress = false;
        String sMsg = "";
        if (!UserPreferences.getAppSetupCompleted(context)){
            Intent myInitialIntent = new Intent(context, InitialActivity.class);
            startActivity(myInitialIntent);
        }else{
            Intent myMainIntent = new Intent(context, MainActivity.class);
            startActivity(myMainIntent);
        }
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        doStartUp();
/*        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doStartUp();
            }
        }, Constants.ANIM_FADE_TIME_MS);*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(R.style.AppTheme_Launcher);
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        if (!UserPreferences.getAppSetupCompleted(context)){
            Intent myInitialIntent = new Intent(context, InitialActivity.class);
            startActivity(myInitialIntent);
            finish();
            return;
        }else {
            setContentView(R.layout.activity_splash);
            androidx.constraintlayout.widget.ConstraintLayout mLayout = this.findViewById(R.id.splash_constraintLayout);
            TextView textView = this.findViewById(R.id.splash_message);
            if (UserPreferences.getLastUserName(context) != null){
                textView.setText(context.getString(R.string.label_welcome_back) + UserPreferences.getLastUserName(context));
            }else{
                textView.setText(context.getString(R.string.label_setting_up));
            }
            // Enables Always-on
            setAmbientEnabled();
            setAutoResumeEnabled(true);
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        Context context = getApplicationContext();
        int bgColor = ContextCompat.getColor(context,R.color.colorAmbientBackground);
        int foreColor = ContextCompat.getColor(context,R.color.colorAmbientForeground);
        if (mLayout != null) {
            mLayout.setBackgroundColor(bgColor);
            TextView textView = mLayout.findViewById(R.id.splash_message);
            if (textView != null) {
                if (UserPreferences.getLastUserName(context) != null){
                    textView.setText(getResources().getString(R.string.label_welcome_back) + UserPreferences.getLastUserName(context));
                }else{
                    textView.setText(getResources().getString(R.string.label_setting_up));
                }
                textView.setTextColor(foreColor);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
                Paint textPaint = textView.getPaint();
                textPaint.setAntiAlias(false);
                textPaint.setStyle(Paint.Style.STROKE);
                textPaint.setStrokeWidth(2);
            }
        }
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        Context context = getApplicationContext();
        int bgColor = ContextCompat.getColor(context, R.color.colorSplash);
        int foreColor = ContextCompat.getColor(context, R.color.semiWhite);
        if (mLayout != null) {
            mLayout.setBackgroundColor(bgColor);
            TextView textView = mLayout.findViewById(R.id.splash_message);
            if (textView != null) {
                textView.setTextColor(foreColor);
                textView.getPaint().setAntiAlias(true);
                if (UserPreferences.getLastUserName(context) != null){
                    textView.setText(getResources().getString(R.string.label_welcome_back) + UserPreferences.getLastUserName(context));
                }else{
                    textView.setText(getResources().getString(R.string.label_setting_up));
                }
            }
        }
    }
}