package com.a_track_it.workout.common.data_model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.R;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

import static com.a_track_it.workout.common.Constants.ATRACKIT_EMPTY;
import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;
import static com.a_track_it.workout.common.Constants.KEY_FIT_VALUE;
import static com.a_track_it.workout.common.Constants.SALT;

public class LicenceJobIntentService extends JobIntentService {
    private final static String LOG_TAG = LicenceJobIntentService.class.getSimpleName();
    private String userID = null;
    private String sUid = ATRACKIT_EMPTY;
    private long lastLicenceCheck = 0L;
    private int initialLoad = 0;

    private Bundle resultBundle = new Bundle();
    private ResultReceiver resultReceiver;
    private LicenseChecker licenseChecker;
    private LicenseCheckerCallback mLicenseCheckerCallback = new MyLicenseChecker();

    private class MyLicenseChecker implements LicenseCheckerCallback{
        @Override
        public void allow(int reason) {
            Log.e(LOG_TAG, "licence check ALLOWED  " + reason);
            Log.d(LOG_TAG, "Verified");
            resultBundle.putInt(KEY_FIT_VALUE, reason);
            resultReceiver.send(200, resultBundle);
        }

        @Override
        public void dontAllow(int reason) {
            Log.e(LOG_TAG, "licence check not allowed reason " + reason);
            if (reason == Policy.RETRY) {
                // If the reason received from the policy is RETRY, it was probably
                // due to a loss of connection with the service, so we should give the
                // user a chance to retry. So show a dialog to retry.
                Log.d(LOG_TAG, "Retrying...");
                resultBundle.putInt(KEY_FIT_VALUE, reason);
                resultReceiver.send(505, resultBundle);
              //  finish();
            } else {
                // Otherwise, the user is not licensed to use this app.
                // Your response should always inform the user that the application
                // is not licensed, but your behavior at that point can vary. You might
                // provide the user a limited access version of your app or you can
                // take them to Google Play to purchase the app.
                //  showDialog(DIALOG_GOTOMARKET);
                Log.d(LOG_TAG, "Go to Google Market to purchase A-Track-It");
                resultBundle.putInt(KEY_FIT_VALUE, reason);
                resultReceiver.send(504, resultBundle);
              //  finish();
            }
        }

        @Override
        public void applicationError(int errorCode) {
            Log.e(LOG_TAG, "applicationError " + errorCode);
            String sResult = "application error";
            if (errorCode == 3 ){
                sResult = "Not market managed";
                Log.d(LOG_TAG, sResult);
            }else
                if (errorCode == 4 ){
                    sResult = "Server error";
                    Log.d(LOG_TAG, sResult);

                }else {
                    Log.d(LOG_TAG, sResult);
                }
            resultBundle.putInt(KEY_FIT_VALUE, errorCode);
            resultReceiver.send(500, resultBundle);
        }
    }
    /**
     * Unique job ID for this service.
     */
    private static final int JOB_ID = 6;

    public LicenceJobIntentService() { super(); }

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, LicenceJobIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        resultReceiver = intent.getParcelableExtra(Constants.KEY_FIT_REC);
        userID = intent.getStringExtra(Constants.KEY_FIT_USER);
        sUid = intent.getStringExtra(Constants.ATRACKIT_ATRACKIT_CLASS);
        lastLicenceCheck = intent.getLongExtra("lastLic", 0);
        initialLoad = intent.getIntExtra(Constants.KEY_FIT_TYPE, 0);
        resultBundle.putString(KEY_FIT_USER, userID);
        resultBundle.putString(Constants.ATRACKIT_ATRACKIT_CLASS, sUid);
        Log.e(LOG_TAG, "before check access");
        try {
            if (licenseChecker != null)
                licenseChecker.checkAccess(mLicenseCheckerCallback);
        }catch (Exception e){
            Log.e(LOG_TAG, "licence check ERROR  " + e.getMessage());
            int reason = 1;
            Log.d(LOG_TAG, "licence check ALLOWED  " + reason);
            Log.d(LOG_TAG, "Verified");
            resultBundle.putInt(KEY_FIT_VALUE, reason);
            resultReceiver.send(200, resultBundle);
        }

    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        Context context = getApplicationContext();
        // Try to use more data here. ANDROID_ID is a single point of attack.
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String sPackage = context.getString(R.string.app_package);
        // Library calls this when it's done.
        licenseChecker = new LicenseChecker(LicenceJobIntentService.this,new ServerManagedPolicy(context,
                new AESObfuscator( SALT, sPackage, deviceId)), Constants.ATRACKIT_LICENCE_KEY);

    }

    @Override
    public void onDestroy() {
        licenseChecker.onDestroy();
        super.onDestroy();
    }

}
