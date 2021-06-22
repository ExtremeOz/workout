package com.a_track_it.fitdata.common.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.a_track_it.fitdata.common.Constants;

import java.util.Locale;


/**
 * Created by Daniel Haywood October 2018
 */
@SuppressLint("CommitPrefEdits")
public class ApplicationPreferences {
    private static final String PREFS_NAME = "atrackit_prefs_system";
    private static final String EMPTY_JUAN = Constants.ATRACKIT_EMPTY;
    private static ApplicationPreferences sInstance;
    private final SharedPreferences settings;

    private ApplicationPreferences(Context pContext){
        settings = pContext.getSharedPreferences(String.format(Locale.getDefault(),PREFS_NAME),Context.MODE_PRIVATE);
    }
    
    public static ApplicationPreferences getPreferences(Context context){
        if (null == sInstance){
            sInstance = new ApplicationPreferences(context);
        }
        return sInstance;
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
        return settings.getString(label, EMPTY_JUAN);
    }

    public boolean getMicrophoneAvail() {
        return settings.getBoolean("MicrophoneAvail", false);
    }

    public void setMicrophoneAvail(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("MicrophoneAvail", value);
        // Commit the edits!
        editor.apply();
    }
    public boolean getSpeakerAvail() {
        return settings.getBoolean("SpeakerAvail", false);
    }

    public void setSpeakerAvail(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("SpeakerAvail", value);
        // Commit the edits!
        editor.apply();
    }

    public boolean getFirebaseAvail() {
        return settings.getBoolean("FirebaseAvail", false);
    }

    public void setFirebaseAvail(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("FirebaseAvail", value);
        // Commit the edits!
        editor.apply();
    }
    

     public boolean getBackgroundLoadComplete() {
        return settings.getBoolean("backgroundLoadComplete", false);
    }

    public void setBackgroundLoadComplete(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("backgroundLoadComplete", value);

        // Commit the edits!
        editor.apply();
    }

    public String getLastNodeID() {
        return settings.getString("lastNodeID", Constants.ATRACKIT_EMPTY);
    }

    public void setLastNodeID(String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastNodeID", value);

        // Commit the edits!
        editor.apply();
    }
    public String getLastNodeName() {
        return settings.getString("lastNodeName", Constants.ATRACKIT_EMPTY);
    }

    public void setLastNodeName(String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastNodeName", value);

        // Commit the edits!
        editor.apply();
    }
    public long getLastNodeSync() {
        
        return settings.getLong("LastNodeSync", 0);
    }

    public void setLastNodeSync(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("LastNodeSync", value);
        // Commit the edits!
        editor.apply();
    }
    public String getLastUserID() {
        String sID = settings.getString("lastUserID", Constants.ATRACKIT_EMPTY);
        if (sID == null) sID = Constants.ATRACKIT_EMPTY;
        return sID;
    }

    public void setLastUserID(String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastUserID", value);
        // Commit the edits!
        editor.apply();
    }    
    
    public String getDeviceID() {
        return settings.getString("lastDeviceID", Constants.ATRACKIT_EMPTY);
    }

    public void setDeviceID(String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDeviceID", value);

        // Commit the edits!
        editor.apply();
    }
    public Long getLastUserLogIn() {
        return settings.getLong("LastUserLogIn", 0);
    }

    public void setLastUserLogIn(Long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("LastUserLogIn", value);
        // Commit the edits!
        editor.apply();
    }
    public Integer getTempSensorCount() {
        return settings.getInt(Constants.AP_PREF_TEMP_SENSOR_COUNT, 0);
    }

    public void setTempSensorCount(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.AP_PREF_TEMP_SENSOR_COUNT, value);
        // Commit the edits!
        editor.apply();
    }
    public Integer getPressureSensorCount() {
        return settings.getInt(Constants.AP_PREF_HPA_SENSOR_COUNT, 0);
    }

    public void setPressureSensorCount(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.AP_PREF_HPA_SENSOR_COUNT, value);
        // Commit the edits!
        editor.apply();
    }
    public Integer getHumiditySensorCount() {
        return settings.getInt(Constants.AP_PREF_HUMIDITY_SENSOR_COUNT, 0);
    }

    public void setHumiditySensorCount(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.AP_PREF_HUMIDITY_SENSOR_COUNT, value);
        // Commit the edits!
        editor.apply();
    }    
    public Integer getBPMSensorCount() {
        return settings.getInt(Constants.AP_PREF_BPM_SENSOR_COUNT, 0);
    }

    public void setBPMSensorCount(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.AP_PREF_BPM_SENSOR_COUNT, value);
        // Commit the edits!
        editor.apply();
    }
    public Integer getStepsSensorCount() {
        return settings.getInt(Constants.AP_PREF_STEP_SENSOR_COUNT, 0);
    }

    public void setStepsSensorCount(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.AP_PREF_STEP_SENSOR_COUNT, value);
        // Commit the edits!
        editor.apply();
    }
    public Integer getTemp2SensorCount() {
        return settings.getInt(Constants.AP_PREF_TEMP_SENSOR2_COUNT, 0);
    }

    public void setTemp2SensorCount(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.AP_PREF_TEMP_SENSOR2_COUNT, value);
        // Commit the edits!
        editor.apply();
    }
    public Integer getPressure2SensorCount() {
        return settings.getInt(Constants.AP_PREF_HPA_SENSOR2_COUNT, 0);
    }

    public void setPressure2SensorCount(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.AP_PREF_HPA_SENSOR2_COUNT, value);
        // Commit the edits!
        editor.apply();
    }
    public Integer getHumidity2SensorCount() {
        return settings.getInt(Constants.AP_PREF_HUMIDITY_SENSOR2_COUNT, 0);
    }

    public void setHumidity2SensorCount(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.AP_PREF_HUMIDITY_SENSOR2_COUNT, value);
        // Commit the edits!
        editor.apply();
    }
    public Integer getBPM2SensorCount() {
        return settings.getInt(Constants.AP_PREF_BPM_SENSOR2_COUNT, 0);
    }

    public void setBPM2SensorCount(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.AP_PREF_BPM_SENSOR2_COUNT, value);
        // Commit the edits!
        editor.apply();
    }
    public Integer getSteps2SensorCount() {
        return settings.getInt(Constants.AP_PREF_STEP_SENSOR2_COUNT, 0);
    }

    public void setSteps2SensorCount(Integer value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.AP_PREF_STEP_SENSOR2_COUNT, value);
        // Commit the edits!
        editor.apply();
    }
    public boolean getAppSetupCompleted() {
        return settings.getBoolean(Constants.ATRACKIT_SETUP, false);
    }

    public void setAppSetupCompleted(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.ATRACKIT_SETUP, value);
        // Commit the edits!
        editor.apply();
    }
    public long getNetworkCheckInterval() {
        return settings.getLong(Constants.AP_PREF_SYNC_INT_NETWORK, 60000);
    }

    public void setNetworkCheckInterval(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Constants.AP_PREF_SYNC_INT_NETWORK, value);
        // Commit the edits!
        editor.apply();
    }
    public long getLastSyncInterval() {
        return settings.getLong(Constants.AP_PREF_SYNC_INT, 900000);
    }

    public void setLastSyncInterval(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Constants.AP_PREF_SYNC_INT, value);
        // Commit the edits!
        editor.apply();
    }
    public long getPhoneSyncInterval() {
        return settings.getLong(Constants.AP_PREF_SYNC_INT_PHONE, 600000);
    }

    public void setPhoneSyncInterval(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Constants.AP_PREF_SYNC_INT_PHONE, value);
        // Commit the edits!
        editor.apply();
    }
    public long getLastPhoneSync() {
        return settings.getLong("lastPhoneSync", 0);
    }

    public void setLastPhoneSync(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("lastPhoneSync", value);
        // Commit the edits!
        editor.apply();
    }
    public long getLastSync() {
        return settings.getLong("lastSync", 0);
    }

    public void setLastSync(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("lastSync", value);
        // Commit the edits!
        editor.apply();
    }
    public long getLastNetworkCheck() {
        return settings.getLong("lastNetworkCheck", 0);
    }

    public void setLastNetworkCheck(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("lastNetworkCheck", value);
        // Commit the edits!
        editor.apply();
    }
    public long getHistoryOpen() {
        return settings.getLong("HistoryOpen", 0);
    }

    public void setHistoryOpen(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("HistoryOpen", value);
        // Commit the edits!
        editor.apply();
    }

    public long getLastSyncStart() {
        return settings.getLong("lastSyncStartTime", 0);
    }

    public void setLastSyncStart(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("lastSyncStartTime", value);

        // Commit the edits!
        editor.apply();
    }
    public long getDailySyncInterval() {
        return settings.getLong(Constants.AP_PREF_SYNC_INT_DAILY, 600000);
    }

    public void setDailySyncInterval(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Constants.AP_PREF_SYNC_INT_DAILY, value);
        // Commit the edits!
        editor.apply();
    }
    public long getLastDailySync() {
        return settings.getLong("LastDailySync", 0);
    }

    public void setLastDailySync(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("LastDailySync", value);
        // Commit the edits!
        editor.apply();
    }
    public long getLastSensorBind(int type) {
        String sKey = "LastSensorBind" + type;
        return settings.getLong(sKey, 0);
    }

    public void setLastSensorBind(int type, long value) {
        String sKey = "LastSensorBind" + type;
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(sKey, value);
        // Commit the edits!
        editor.apply();
    }
    public long getLastGoalSync() {
        return settings.getLong("LastGoalSync", 0);
    }

    public void setLastGoalSync(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("LastGoalSync", value);
        // Commit the edits!
        editor.apply();
    }    
    public long getLastLocationUpdate() {
        return settings.getLong("lastLocationUpd", 0);
    }

    public void setLastLocationUpdate(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("lastLocationUpd", value);
        // Commit the edits!
        editor.apply();
    }

    public boolean getUseSensors() {
        return settings.getBoolean(Constants.AP_PREF_USE_SENSORS, false);
    }

    public void setUseSensors(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.AP_PREF_USE_SENSORS, value);
        // Commit the edits!
        editor.apply();
    }

    public boolean getUseLocation() {
        return settings.getBoolean(Constants.AP_PREF_USE_LOCATION, false);
    }

    public void setUseLocation(boolean value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.AP_PREF_USE_LOCATION, value);
        // Commit the edits!
        editor.apply();
    }

    public long getLastLicCheck() {
        
        return settings.getLong("lastLicCheck", 0);
    }

    public void setLastLicCheck(long value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("lastLicCheck", value);
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
}
