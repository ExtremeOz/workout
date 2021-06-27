package com.a_track_it.workout.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;
import android.util.Log;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.FitSyncJobIntentService;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.a_track_it.workout.common.model.UserPreferences;

import static com.a_track_it.workout.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final int ALARM_PENDING_SYNC_CODE = 55343;
    @Override
    public void onReceive(Context mContext, Intent intent) {
        Log.w(BootCompleteReceiver.class.getSimpleName(), "starting bootCompleteReceiver");
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
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
                        CustomIntentReceiver.setAlarm(mContext.getApplicationContext(), true, sync_delay, sUserId, null);
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
    }

    private void doPendingSync(Context context, String sUserID, String sDeviceID){
        Intent mIntent = new Intent();
        mIntent.putExtra(Constants.KEY_FIT_ACTION, Constants.TASK_ACTION_SYNC_WORKOUT);
        long startTime = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_WEEK);
        long endTime = System.currentTimeMillis();
        mIntent.putExtra(Constants.MAP_START, startTime);
        mIntent.putExtra(Constants.MAP_END, endTime);
        mIntent.putExtra(Constants.KEY_RESULT, (ResultReceiver)null);
        mIntent.putExtra(KEY_FIT_USER, sUserID);
        mIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
        try{
            FitSyncJobIntentService.enqueueWork(context, mIntent);
        }catch (Exception e){
            Log.e(BootCompleteReceiver.class.getSimpleName(), "Error daily sync set alarm " + e.getMessage());
        }
/*        Data.Builder builder = new Data.Builder();
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
        mWorkManager.beginWith(oneTimeWorkRequest).enqueue();*/
    }
}
