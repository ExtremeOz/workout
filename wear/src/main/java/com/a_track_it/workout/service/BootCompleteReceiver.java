package com.a_track_it.workout.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.a_track_it.workout.common.data_model.GoogleSyncWorker;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.a_track_it.workout.common.model.UserPreferences;

import static com.a_track_it.workout.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context mContext, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                ApplicationPreferences appPrefs = ApplicationPreferences.getPreferences(mContext.getApplicationContext());
                boolean readSetup = appPrefs.getAppSetupCompleted();
                String sUserId = appPrefs.getLastUserID();
                String sDeviceId = appPrefs.getDeviceID();
                long sync_delay = appPrefs.getDailySyncInterval();
                if ((sUserId.length() == 0) || (!readSetup) || (sync_delay == 0)) return;
                // have permission, a user and a sync interval
                try {
                    UserPreferences userPreferences = UserPreferences.getPreferences(mContext.getApplicationContext(), sUserId);
                    if (userPreferences.getReadDailyPermissions()) {
                        Log.w(BootCompleteReceiver.class.getSimpleName(), "starting daily " + sUserId);
                        CustomIntentReceiver.setAlarm(mContext.getApplicationContext(), true, sync_delay, sUserId,null);
                    }
                    // ensure pending sync has completed
                    if (userPreferences.getReadSessionsPermissions()) {
                        Log.w(BootCompleteReceiver.class.getSimpleName(), "starting GoogleSyncWorker " + sUserId);
                        doPendingSync(mContext.getApplicationContext(), sUserId, sDeviceId);
                    }
                }catch (Exception e){
                    Log.e(BootCompleteReceiver.class.getSimpleName(), "Error daily sync set alarm " + e.getMessage());
                }
        }
    }
/*    private void doCloudHistory(Context context, String sUserID, long startTime,long endTime){
        Data.Builder builder = new Data.Builder();
        builder.putString(LoadFromGoogleWorker.ARG_ACTION_KEY, "ACTIVITY"); // UDT.ACTIVITY
        builder.putLong(LoadFromGoogleWorker.ARG_START_KEY, startTime);
        builder.putLong(LoadFromGoogleWorker.ARG_END_KEY, endTime);
        builder.putString(KEY_FIT_USER, sUserID);
        Data inputData = builder.build();

        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest oneTimeWorkRequest =
                new OneTimeWorkRequest.Builder(LoadFromGoogleWorker.class)
                        .setInputData(inputData)
                        .setConstraints(constraints)
                        .build();
        WorkManager mWorkManager = WorkManager.getInstance(context);
        if (mWorkManager != null)
            mWorkManager.beginWith(oneTimeWorkRequest).enqueue();
    }*/

    private void doPendingSync(Context context, String sUserID, String sDeviceID){
        Data.Builder builder = new Data.Builder();
        builder.putString(KEY_FIT_USER, sUserID);
        builder.putString(KEY_FIT_DEVICE_ID, sDeviceID);
        Data inputData = builder.build();
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest oneTimeWorkRequest =
                new OneTimeWorkRequest.Builder(GoogleSyncWorker.class)
                        .setInputData(inputData)
                        .setConstraints(constraints)
                        .build();
        WorkManager mWorkManager = WorkManager.getInstance(context);
        mWorkManager.enqueue(oneTimeWorkRequest);
    }
}
