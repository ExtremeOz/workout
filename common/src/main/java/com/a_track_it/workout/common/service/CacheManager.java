package com.a_track_it.workout.common.service;

import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;
import android.util.Log;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.data_model.FitSyncJobIntentService;
import com.a_track_it.workout.service.ReadCacheIntentService;
import com.a_track_it.workout.service.SummaryCacheIntentService;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.a_track_it.workout.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;

public class CacheManager {

    public static final String TAG = "CacheManager";

    public CacheManager() {
    }

    public static void getReport(int reportType, int activityID, ResultReceiver callback,
                                 Context context, String sUserId, String sDeviceID, long startTime, long endTime) {
        if (context == null){
            Log.w(CacheManager.class.getSimpleName(), "null context leaving ");
            return;
        }
    //    if (context != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_24Full, Locale.getDefault());
            Log.w(TAG, "getReport " + reportType + " Range Start: " + dateFormat.format(startTime));
            Log.w(TAG, "Range End: " + dateFormat.format(endTime) + " activityID " + activityID);
            if (reportType == -2){
                Intent mIntent = new Intent(context.getApplicationContext(), FitSyncJobIntentService.class);
                mIntent.putExtra(Constants.KEY_FIT_ACTION, Constants.TASK_ACTION_READ_HISTORY);
                mIntent.putExtra(Constants.MAP_START, startTime);
                mIntent.putExtra(Constants.MAP_END, endTime);
                mIntent.putExtra(Constants.KEY_RESULT, callback);
                mIntent.putExtra(Constants.KEY_FIT_VALUE, 1440);  // minutes in a day
                mIntent.putExtra(KEY_FIT_USER, sUserId);
                mIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
                try{
                    FitSyncJobIntentService.enqueueWork(context.getApplicationContext(), mIntent);
                }catch (Exception e){
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
            if (reportType == -1){
                Intent mIntent = new Intent(context.getApplicationContext(), FitSyncJobIntentService.class);
                mIntent.putExtra(Constants.KEY_FIT_ACTION, Constants.TASK_ACTION_READ_SESSION);
                mIntent.putExtra(Constants.MAP_START, startTime);
                mIntent.putExtra(Constants.MAP_END, endTime);
                mIntent.putExtra(Constants.KEY_RESULT, callback);
                mIntent.putExtra(Constants.KEY_FIT_VALUE, 1440);  // minutes in a day
                mIntent.putExtra(KEY_FIT_USER, sUserId);
                mIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
                try{
                    FitSyncJobIntentService.enqueueWork(context.getApplicationContext(), mIntent);
                }catch (Exception e){
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }else {  // 0 per activity by week - 1 summary type
                Intent intentService = new Intent(context.getApplicationContext(), ReadCacheIntentService.class);
                intentService.putExtra(Constants.MAP_DATA_TYPE, activityID);
                intentService.putExtra(Constants.KEY_FIT_REC, callback);
                intentService.putExtra(Constants.KEY_FIT_USER, sUserId);
                intentService.putExtra(Constants.KEY_FIT_DEVICE_ID, sDeviceID);
                intentService.putExtra(Constants.KEY_FIT_TYPE, reportType);
                intentService.putExtra(Constants.MAP_START, startTime);
                intentService.putExtra(Constants.MAP_END, endTime);
                try {
                    context.startService(intentService);
                } catch (Exception e) {
                    Log.e(CacheManager.TAG, "Error starting ReadCacheIntentService " + e.getMessage());
                }
            }
      //  }
    }
    public static void getDetails(int reportType, long workoutID, ResultReceiver callback, Context context, String sUserId, String sDeviceID) {
        if (context != null) {
            Intent intentService = new Intent(context.getApplicationContext(), ReadCacheIntentService.class);
            intentService.putExtra(Constants.KEY_FIT_TYPE, reportType);
            intentService.putExtra(Constants.KEY_FIT_WORKOUTID, workoutID);
            intentService.putExtra(Constants.KEY_FIT_REC, callback);
            intentService.putExtra(Constants.KEY_FIT_USER, sUserId);
            intentService.putExtra(Constants.KEY_FIT_DEVICE_ID, sDeviceID);
            try{
                context.startService(intentService);
            }catch (Exception e){
                Log.e(CacheManager.TAG, "Error starting ReadCacheIntentService " + e.getMessage());
            }
        }
    }
    public static void getSummary(int indexTimeFrame, long activityID,  ResultReceiver callback, Context context, String sUserId, int indexGrouping,
                                  int indexMetrics, int indexUOY, int indexFilter) {
        if (context != null) {
            if ((sUserId!=null) && (sUserId.length() > 0)) {
                Log.e(CacheManager.TAG, "getSummary " + activityID + " timeframe idx " + indexTimeFrame + " indxGrp " + indexGrouping + " indxMetric "+ indexMetrics);
                Intent intentService = new Intent(context.getApplicationContext(), SummaryCacheIntentService.class);
                intentService.putExtra(Constants.MAP_DATA_TYPE, activityID);
                intentService.putExtra(Constants.KEY_FIT_REC, callback);
                intentService.putExtra(Constants.KEY_FIT_USER, sUserId);
                intentService.putExtra(Constants.KEY_INDEX_GROUP, indexGrouping);
                intentService.putExtra(Constants.KEY_INDEX_METRIC, indexMetrics);
                intentService.putExtra(Constants.KEY_INDEX_UOY, indexUOY);
                intentService.putExtra(Constants.KEY_INDEX_FILTER, indexFilter);
                intentService.putExtra(Constants.KEY_FIT_TYPE, indexTimeFrame);
                try {
                    context.startService(intentService);
                }catch (Exception e){
                    Log.e(CacheManager.TAG, "Error SummaryCacheIntentService " + e.getMessage());
                }
            }
        }
    }

}
