package com.a_track_it.fitdata.common.data_model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.service.DailySummaryJobIntentService;

import java.util.Calendar;
import java.util.Locale;

public class CurrentUserDailyTotalsWorker extends Worker {
    private static final String LOG_TAG = CurrentUserDailyTotalsWorker.class.getSimpleName();
    private UserDailyTotalsDao mUserDailyTotalsDao;
    private SensorDailyTotalsDao mSensorDailyTotalsDao;
    public CurrentUserDailyTotalsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        WorkoutRoomDatabase db = WorkoutRoomDatabase.getDatabase(context.getApplicationContext());
        mUserDailyTotalsDao = db.dailyTotalsDao();
        mSensorDailyTotalsDao = db.sdtDao();
    }
    /** doWork
     read un-synchronized
     **/
    @NonNull
    @Override
    public Result doWork() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        try{
            int iRetVal = 0;
            Data data = getInputData();
            String sUserID = data.getString(Constants.KEY_FIT_USER);
            String sDeviceID = data.getString(Constants.KEY_FIT_DEVICE_ID);
            int iType = getInputData().getInt(Constants.KEY_FIT_TYPE, 0); // 0 = google daily, 1 = location
            long endTime = calendar.getTimeInMillis();
            calendar.setTimeInMillis(endTime);
            int DOY = calendar.get(Calendar.DAY_OF_YEAR);
            int newDOY = DOY + 1;  // not equal default
            UserDailyTotals udt = null; // doesn't exist default
            SensorDailyTotals sdt = null;
            udt = mUserDailyTotalsDao.getTopUserTotalsByUserID(sUserID);
            if (udt != null) {
                calendar.setTimeInMillis(udt._id);
                newDOY = calendar.get(Calendar.DAY_OF_YEAR);
            }
            if (iType == 2){
                sdt = mSensorDailyTotalsDao.getTopSensorTotalsByUserID(sUserID);
            }
            if (DOY == newDOY) {
                Intent broadcastIntent = new Intent(Constants.INTENT_TOTALS_REFRESH);
                broadcastIntent.putExtra(Constants.KEY_FIT_USER, sUserID);
                broadcastIntent.putExtra(Constants.KEY_FIT_DEVICE_ID, sDeviceID);
                broadcastIntent.putExtra(UserDailyTotals.class.getSimpleName(), udt);
                if (sdt != null)
                    broadcastIntent.putExtra(SensorDailyTotals.class.getSimpleName(), sdt);
                broadcastIntent.putExtra(Constants.KEY_COMM_TYPE, iType);
                broadcastIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                Context context = getApplicationContext();
                context.sendBroadcast(broadcastIntent);
            }else{
                Log.w(LOG_TAG, "no existing udt - starting daily ");
                try {
                    Intent mIntent = new Intent(getApplicationContext(), DailySummaryJobIntentService.class);
                    mIntent.putExtra(Constants.KEY_FIT_USER, sUserID);
                    mIntent.putExtra(Constants.KEY_FIT_DEVICE_ID, sDeviceID);
                    mIntent.putExtra(Constants.KEY_FIT_TYPE, 0);  // force
                    mIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                    DailySummaryJobIntentService.enqueueWork(getApplicationContext(), mIntent);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "start doDailySummaryJob error " + e.getMessage());
                }
            }

            Data outputData = new Data.Builder()
                    .putInt(Constants.KEY_RESULT, iRetVal)
                    .build();
            return Result.success(outputData);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            return Result.failure();
        }
    }
}