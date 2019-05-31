package com.a_track_it.fitdata.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.a_track_it.fitdata.R;


/**
 * Created by Daniel Haywood October 2018
 */
@SuppressLint("CommitPrefEdits")
public class UserPreferences {
    public static final String PREFS_NAME = "atrackit-wear";

    public static void setPrefByLabel(Context context, String label, boolean value){
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(label, value);
        // Commit the edits!
        editor.apply();
    }
    public static void setPrefStringByLabel(Context context, String label, String value){
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(label, value);
        // Commit the edits!
        editor.apply();
    }
    public static String getPrefStringByLabel(Context context, String label){
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(label, context.getString(com.a_track_it.fitdata.R.string.my_empty_string));
    }
    public static boolean getConfirmStartSession(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("ConfirmStartSession", false);
    }

    public static void setConfirmStartSession(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("ConfirmStartSession", value);

        // Commit the edits!
        editor.apply();
    }
    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);

        return settings.getBoolean("requesting_location_updates", false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("requesting_location_updates", requestingLocationUpdates);
        // Commit the edits!
        editor.apply();
    }

    public static boolean getConfirmEndSession(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("ConfirmEndSession", false);
    }

    public static void setConfirmEndSession(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("ConfirmEndSession", value);

        // Commit the edits!
        editor.apply();
    }
    public static double getLastLong(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getFloat(context.getString(R.string.LastLong), 0F);
    }

    public static void setLastLong(Context context, float value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(context.getString(R.string.LastLong), value);

        // Commit the edits!
        editor.apply();
    }
    public static double getLastLati(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getFloat(context.getString(R.string.LastLat), 0F);
    }

    public static void setLastLati(Context context, float value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(context.getString(R.string.LastLat), value);

        // Commit the edits!
        editor.apply();
    }
    public static boolean getConfirmDismissSession(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("ConfirmDismissSession", false);
    }

    public static void setConfirmDismissSession(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("ConfirmDismissSession", value);

        // Commit the edits!
        editor.apply();
    }
    public static boolean getConfirmUseSensors(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("ConfirmUseSensors", false);
    }

    public static void setConfirmUseSensors(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("ConfirmUseSensors", value);

        // Commit the edits!
        editor.apply();
    }
    public static boolean getConfirmUseRecord(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("ConfirmUseRecord", false);
    }

    public static void setConfirmUseRecord(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("ConfirmUseRecord", value);

        // Commit the edits!
        editor.apply();
    }
    public static boolean getConfirmUseLocation(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("ConfirmUseLocation", false);
    }

    public static void setConfirmUseLocation(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("ConfirmUseLocation", value);

        // Commit the edits!
        editor.apply();
    }    
    public static boolean getFeedbackMute(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("FeedbackMute", false);
    }

    public static void setFeedbackMute(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("FeedbackMute", value);

        // Commit the edits!
        editor.apply();
    }
    public static boolean getBackgroundLoadComplete(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("backgroundLoadComplete", false);
    }

    public static void setBackgroundLoadComplete(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("backgroundLoadComplete", value);

        // Commit the edits!
        editor.apply();
    }
    public static boolean getDMKeepAliveComplete(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("backgroundDMKeepAliveComplete", false);
    }

    public static void setDMKeepAliveComplete(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("backgroundDMKeepAliveComplete", value);

        // Commit the edits!
        editor.apply();
    }
    public static String getLastUserName(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("lastUserName", "");
    }

    public static void setLastUserName(Context context, String value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastUserName", value);

        // Commit the edits!
        editor.apply();
    }
    public static String getLastBodyPartID(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("lastBodyPartID", "");
    }

    public static void setLastBodyPartID(Context context, String value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastBodyPartID", value);

        // Commit the edits!
        editor.apply();
    }
    public static String getLastBodyPartName(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("lastBodyPartName", "");
    }

    public static void setLastBodyPartName(Context context, String value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastBodyPartName", value);

        // Commit the edits!
        editor.apply();
    }        
    public static String getLastUserID(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("lastUserID", "");
    }

    public static void setLastUserID(Context context, String value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastUserID", value);

        // Commit the edits!
        editor.apply();
    }
    public static String getLastUserPhotoUri(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("lastUserUri", "");
    }

    public static void setLastUserPhotoUri(Context context, String value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastUserUri", value);
        // Commit the edits!
        editor.apply();
    }

    public static String getLastUserPhotoInternal(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("lastUserPhotoInternal", "");
    }

    public static void setLastUserPhotoInternal(Context context, String value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastUserPhotoInternal", value);
        // Commit the edits!
        editor.apply();
    }
    public static Integer getWeightsRestDuration(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("WeightsRestDuration", 0);
    }

    public static void setWeightsRestDuration(Context context, Integer value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("WeightsRestDuration", value);
        // Commit the edits!
        editor.apply();
    }
    public static Integer getArcheryRestDuration(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("ArcheryRestDuration", 0);
    }

    public static void setArcheryRestDuration(Context context, Integer value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("ArcheryRestDuration", value);
        // Commit the edits!
        editor.apply();
    }
    public static Integer getBPMSampleRate(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("BPMSampleRate", 0);
    }

    public static void setBPMSampleRate(Context context, Integer value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("BPMSampleRate", value);
        // Commit the edits!
        editor.apply();
    }
    public static Integer getStepsSampleRate(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("StepsSampleRate", 0);
    }

    public static void setStepsSampleRate(Context context, Integer value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("StepsSampleRate", value);
        // Commit the edits!
        editor.apply();
    }
    public static Integer getOthersSampleRate(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("OthersSampleRate", 0);
    }

    public static void setOthersSampleRate(Context context, Integer value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("OthersSampleRate", value);
        // Commit the edits!
        editor.apply();
    }
    public static boolean getCountSteps(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("shouldCountSteps", false);
    }

    public static void setCountSteps(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("shouldCountSteps", value);

        // Commit the edits!
        editor.apply();
    }
    public static boolean getShowDataPoints(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("showShowDataPoints", false);
    }

    public static void setShowDataPoints(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("showShowDataPoints", value);

        // Commit the edits!
        editor.apply();
    }
    
    public static boolean getActivityTracking(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("shouldTrackActivity", false);
    }

    public static void setActivityTracking(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("shouldTrackActivity", value);

        // Commit the edits!
        editor.apply();
    }

    public static boolean getAppSetupCompleted(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("appSetupComplete", false);
    }

    public static void setAppSetupCompleted(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("appSetupComplete", value);
        // Commit the edits!
        editor.apply();
    }
    public static long getLastSync(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getLong("lastSuccessfulSync", 0);
    }

    public static void setLastSync(Context context, long value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("lastSuccessfulSync", value);

        // Commit the edits!
        editor.apply();
    }
    public static long getConfirmDuration(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getLong("confirm_duration", 0);
    }

    public static void setConfirmDuration(Context context, long value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("confirm_duration", value);
        // Commit the edits!
        editor.apply();
    }

    public static int getActivityID1(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("activityID1", 0);
    }

    public static void setActivityID1(Context context, int value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activityID1", value);

        // Commit the edits!
        editor.apply();
    }
    public static int getActivityID2(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("activityID2", 0);
    }

    public static void setActivityID2(Context context, int value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activityID2", value);

        // Commit the edits!
        editor.apply();
    }
    public static int getActivityID3(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("activityID3", 0);
    }

    public static void setActivityID3(Context context, int value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activityID3", value);

        // Commit the edits!
        editor.apply();
    }
    public static int getActivityID4(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("activityID4", 0);
    }

    public static void setActivityID4(Context context, int value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activityID4", value);

        // Commit the edits!
        editor.apply();
    }
    public static int getActivityID5(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("activityID5", 0);
    }

    public static void setActivityID5(Context context, int value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activityID5", value);

        // Commit the edits!
        editor.apply();
    }
    public static long getLastSyncStart(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getLong("lastSyncStartTime", 0);
    }

    public static void setLastSyncStart(Context context, long value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("lastSyncStartTime", value);

        // Commit the edits!
        editor.apply();
    }
    public static boolean getUseKG(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("useKG", false);
    }

    public static void setUseKG(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("useKG", value);

        // Commit the edits!
        editor.apply();
    }
    public static boolean getTimedRest(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("timedRest", false);
    }

    public static void setTimedRest(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("timedRest", value);

        // Commit the edits!
        editor.apply();
    }
    public static boolean getWorkOffline(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("workOffline", false);
    }

    public static void setWorkOffline(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("workOffline", value);

        // Commit the edits!
        editor.apply();
    }
    public static boolean getShouldDeleteData(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean("shouldDeleteData", false);
    }

    public static void setShouldDeleteData(Context context, boolean value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("shouldDeleteData", value);

        // Commit the edits!
        editor.apply();
    }

    public static String getUserEmail(Context context) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("userEmail", "");
    }

    public static void setUserEmail(Context context, String value) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("userEmail", value);

        // Commit the edits!
        editor.apply();
    }
}
