package com.a_track_it.fitdata.common.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.a_track_it.fitdata.common.Constants;

import java.util.Locale;


/**
 * Created by Daniel Haywood October 2018
 */
@SuppressLint("CommitPrefEdits")
public class UserPreferences {
    private static final String PREFS_NAME = "atrackit_prefs_%s";
    private static final String EMPTY_JUAN = "";
    private final String mUserId;

    private static UserPreferences sInstance; 
    private final SharedPreferences settings;

    private UserPreferences(Context pContext, String sUserId){
        mUserId = sUserId;
        settings = pContext.getSharedPreferences(String.format(Locale.getDefault(),PREFS_NAME, sUserId),Context.MODE_PRIVATE);
    }
    
    public static UserPreferences getPreferences(Context context, String userId){
        if ((userId == null) ||(userId.length() == 0))
            return null;

        if ((null == sInstance) || !TextUtils.equals(sInstance.getUserId(), userId)){
            sInstance = new UserPreferences(context, userId);
        }
        return sInstance;
    }
    
    public String getUserId(){
        return mUserId;
    }

    public void setLongPrefByLabel(String label, long value){
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(label, value);
        // Commit the edits!
        editor.apply();
    }
    public long getLongPrefByLabel(String label){
        return settings.getLong(label,0);
    }

    public void setPrefByLabel(String label, boolean value){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(label, value);
        // Commit the edits!
        editor.apply();
    }
    public boolean getPrefByLabel(String label){
        return settings.getBoolean(label,false);
    }

    public void setPrefStringByLabel(String label, String value){
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(label, value);
        // Commit the edits!
        editor.apply();
    }
    public String getPrefStringByLabel(String label){
        return settings.getString(label, this.EMPTY_JUAN);
    }

    public boolean getReadDailyPermissions() {
        return settings.getBoolean("ReadDailyPermissions", false);
    }

    public void setReadDailyPermissions(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("ReadDailyPermissions", value);
        // Commit the edits!
        editor.apply();
    }
    public boolean getReadSensorsPermissions() {
        return settings.getBoolean("ReadSensorsPermissions", false);
    }

    public void setReadSensorsPermissions(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("ReadSensorsPermissions", value);
        // Commit the edits!
        editor.apply();
    }

    public boolean getConfirmStartSession() {
        return settings.getBoolean(Constants.USER_PREF_CONF_START_SESSION, false);
    }

    public void setConfirmStartSession(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.USER_PREF_CONF_START_SESSION, value);

        // Commit the edits!
        editor.apply();
    }
    public boolean getConfirmEndSession() {
        return settings.getBoolean(Constants.USER_PREF_CONF_END_SESSION, false);
    }

    public void setConfirmEndSession(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.USER_PREF_CONF_END_SESSION, value);

        // Commit the edits!
        editor.apply();
    }
    public boolean getConfirmDeleteSession() {
        return settings.getBoolean(Constants.USER_PREF_CONF_DEL_SESSION, false);
    }

    public void setConfirmDeleteSession(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.USER_PREF_CONF_DEL_SESSION, value);

        // Commit the edits!
        editor.apply();
    }    
    public boolean getConfirmSetSession() {
        return settings.getBoolean(Constants.USER_PREF_CONF_SET_SESSION, false);
    }

    public void setConfirmSetSession(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.USER_PREF_CONF_SET_SESSION, value);

        // Commit the edits!
        editor.apply();
    }
    public boolean getAskAge() {
        return settings.getBoolean(Constants.AP_PREF_ASK_AGE, false);
    }

    public void setAskAge(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.AP_PREF_ASK_AGE, value);
        // Commit the edits!
        editor.apply();
    }
    public boolean getReadSessionsPermissions() {
        return settings.getBoolean("ReadSessionsPermissions", false);
    }

    public void setReadSessionsPermissions(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("ReadSessionsPermissions", value);
        // Commit the edits!
        editor.apply();
    }

     public boolean getConfirmExitApp() {
        return settings.getBoolean(Constants.USER_PREF_CONF_EXIT_APP, false);
    }

    public void setConfirmExitApp(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.USER_PREF_CONF_EXIT_APP, value);

        // Commit the edits!
        editor.apply();
    }
    public boolean getUseRoundedImage() {
        return settings.getBoolean(Constants.USER_PREF_USE_ROUND_IMAGE, false);
    }

    public void setUseRoundedImage(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.USER_PREF_USE_ROUND_IMAGE, value);
        // Commit the edits!
        editor.apply();
    }
    public boolean getUseFirebase() {
        return settings.getBoolean("UseFirebase", false);
    }

    public void setUseFirebase(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("UseFirebase", value);

        // Commit the edits!
        editor.apply();
    }
    public boolean getUseLiveAnimation() {
        return settings.getBoolean("UseLiveAnimation", false);
    }

    public void setUseLiveAnimation(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("UseLiveAnimation", value);

        // Commit the edits!
        editor.apply();
    }
    public String getLastUserName() {
        return settings.getString("lastUserName", Constants.ATRACKIT_EMPTY);
    }

    public void setLastUserName(String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastUserName", value);

        // Commit the edits!
        editor.apply();
    }
    public Long getLastUserSignIn() {
        return settings.getLong("LastUserSignIn", 0);
    }

    public void setLastUserSignIn(Long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("LastUserSignIn", value);
        // Commit the edits!
        editor.apply();
    }
    public Long getLastUserTemplate() {
        return settings.getLong("LastUserTemplate", 0);
    }

    public void setLastUserTemplate(Long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("LastUserTemplate", value);
        // Commit the edits!
        editor.apply();
    }    
    public Long getLastUserOpen() {
        return settings.getLong("LastUserOpen", 0);
    }

    public void setLastUserOpen(Long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("LastUserOpen", value);
        // Commit the edits!
        editor.apply();
    }
    public String getLastBodyPartID() {
        return settings.getString("lastBodyPartID", Constants.ATRACKIT_EMPTY);
    }

    public void setLastBodyPartID(String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastBodyPartID", value);

        // Commit the edits!
        editor.apply();
    }
    public String getLastBodyPartName() {
        return settings.getString("lastBodyPartName", Constants.ATRACKIT_EMPTY);
    }

    public void setLastBodyPartName(String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastBodyPartName", value);

        // Commit the edits!
        editor.apply();
    }
    public Integer getCountdownDuration() {
        return settings.getInt("CountdownDuration", 0);
    }

    public void setCountdownDuration(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("CountdownDuration", value);
        // Commit the edits!
        editor.apply();
    }    
    public Integer getWeightsRestDuration() {
        return settings.getInt(Constants.USER_PREF_GYM_REST_DURATION, 0);
    }

    public void setWeightsRestDuration(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.USER_PREF_GYM_REST_DURATION, value);
        // Commit the edits!
        editor.apply();
    }
    public Integer getArcheryRestDuration() {
        return settings.getInt(Constants.USER_PREF_SHOOT_REST_DURATION, 0);
    }

    public void setArcheryRestDuration(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.USER_PREF_SHOOT_REST_DURATION, value);
        // Commit the edits!
        editor.apply();
    }    
    public Integer getArcheryCallDuration() {
        return settings.getInt(Constants.USER_PREF_SHOOT_CALL_DURATION, 0);
    }

    public void setArcheryCallDuration(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.USER_PREF_SHOOT_CALL_DURATION, value);
        // Commit the edits!
        editor.apply();
    }
    public Integer getArcheryEndDuration() {
        return settings.getInt(Constants.USER_PREF_SHOOT_END_DURATION, 0);
    }

    public void setArcheryEndDuration(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.USER_PREF_SHOOT_END_DURATION, value);
        // Commit the edits!
        editor.apply();
    }    
    public Long getBPMSampleRate() {
        return settings.getLong(Constants.USER_PREF_BPM_SAMPLE_RATE, 30L);
    }

    public void setBPMSampleRate(Long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Constants.USER_PREF_BPM_SAMPLE_RATE, value);
        // Commit the edits!
        editor.apply();
    }
    public Long getStepsSampleRate() {
        return settings.getLong(Constants.USER_PREF_STEP_SAMPLE_RATE, 30L);
    }

    public void setStepsSampleRate(Long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Constants.USER_PREF_STEP_SAMPLE_RATE, value);
        // Commit the edits!
        editor.apply();
    }
    public Long getOthersSampleRate() {
        return settings.getLong(Constants.USER_PREF_OTHERS_SAMPLE_RATE, 30L);
    }

    public void setOthersSampleRate(Long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Constants.USER_PREF_OTHERS_SAMPLE_RATE, value);
        // Commit the edits!
        editor.apply();
    }

    public boolean getShowDataPoints() {
        return settings.getBoolean("showShowDataPoints", false);
    }

    public void setShowDataPoints(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("showShowDataPoints", value);

        // Commit the edits!
        editor.apply();
    }

    public long getLastSKUCheck() {
        
        return settings.getLong("lastSKUCheck", 0);
    }

    public void setLastSKUCheck(long value) {
        
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("lastSKUCheck", value);
        // Commit the edits!
        editor.apply();
    }


    public long getConfirmDuration() {
        return settings.getLong("confirm_duration", 0);
    }

    public void setConfirmDuration(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("confirm_duration", value);
        // Commit the edits!
        editor.apply();
    }

    public int getActivityID1() {
        return settings.getInt("activityID1", 0);
    }

    public void setActivityID1(int value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activityID1", value);

        // Commit the edits!
        editor.apply();
    }
    public int getActivityID2() {
        return settings.getInt("activityID2", 0);
    }

    public void setActivityID2(int value) {
        
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activityID2", value);

        // Commit the edits!
        editor.apply();
    }
    public int getActivityID3() {
        
        return settings.getInt("activityID3", 0);
    }

    public void setActivityID3(int value) {
        
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activityID3", value);

        // Commit the edits!
        editor.apply();
    }
    public int getActivityID4() {
        
        return settings.getInt("activityID4", 0);
    }

    public void setActivityID4(int value) {
        
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activityID4", value);

        // Commit the edits!
        editor.apply();
    }
    public int getActivityID5() {
        
        return settings.getInt("activityID5", 0);
    }

    public void setActivityID5(int value) {
        
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activityID5", value);

        // Commit the edits!
        editor.apply();
    }

    public boolean getUseKG() {
        return settings.getBoolean("useKG", false);
    }

    public void setUseKG(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("useKG", value);
        // Commit the edits!
        editor.apply();
    }
    public boolean getRestAutoStart() {
        return settings.getBoolean(Constants.AP_PREF_USE_TIMED_AUTO_START, false);
    }

    public void setRestAutoStart(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.AP_PREF_USE_TIMED_AUTO_START, value);
        // Commit the edits!
        editor.apply();
    }
    public boolean getTimedRest() {
        return settings.getBoolean(Constants.AP_PREF_USE_TIMED_REST, false);
    }

    public void setTimedRest(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.AP_PREF_USE_TIMED_REST, value);
        // Commit the edits!
        editor.apply();
    }

    public String getUserEmail() {
        
        return settings.getString("userEmail", Constants.ATRACKIT_EMPTY);
    }

    public void setUserEmail(String value) {
        
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("userEmail", value);

        // Commit the edits!
        editor.apply();
    }
    public Integer getDefaultNewSets() {
        
        return settings.getInt(Constants.USER_PREF_DEF_NEW_SETS, 3);
    }

    public void setDefaultNewSets(Integer value) {
        
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.USER_PREF_DEF_NEW_SETS, value);
        // Commit the edits!
        editor.apply();
    }
    public Integer getDefaultNewReps() {
        
        return settings.getInt(Constants.USER_PREF_DEF_NEW_REPS, 10);
    }

    public void setDefaultNewReps(Integer value) {
        
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.USER_PREF_DEF_NEW_REPS, value);
        // Commit the edits!
        editor.apply();
    }

}
