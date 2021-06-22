package com.a_track_it.fitdata.common.data_model;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.model.ApplicationPreferences;
import com.google.android.gms.fitness.data.DataType;

import java.util.Calendar;
import java.util.Locale;

public class Device2SensorWorker extends Worker {
    private static final String LOG_TAG = Device2SensorWorker.class.getSimpleName();
    private ConfigurationDao configurationDao;
    private ApplicationPreferences appPrefs;
    private UserDailyTotalsDao udtDao;
    private SensorDailyTotalsDao sdtDao;

    public Device2SensorWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Context c = context.getApplicationContext();
        WorkoutRoomDatabase db = WorkoutRoomDatabase.getDatabase(c);
        appPrefs = ApplicationPreferences.getPreferences(c);
        configurationDao = db.configDao();
        sdtDao = db.sdtDao();
        udtDao = db.dailyTotalsDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        int iResult = 0;
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        Data data = getInputData();
        int state = data.getInt(Constants.KEY_FIT_ACTION,Constants.WORKOUT_INVALID);
        String sUserID = data.getString(Constants.KEY_FIT_USER);
        String sDeviceID = data.getString(Constants.KEY_FIT_DEVICE_ID);
        final long timeMs = System.currentTimeMillis();
        calendar.setTimeInMillis(timeMs);
        int iDOY = calendar.get(Calendar.DAY_OF_YEAR);
        String sKey  = DataType.TYPE_STEP_COUNT_DELTA.getName();
        long previousMs = data.getLong(Constants.KEY_FIT_TIME, System.currentTimeMillis());
        Log.e(LOG_TAG, "received state " + Utilities.getTimeDateString(previousMs) + " " + state);
        try{
            UserDailyTotals udt = udtDao.getTopUserTotalsByUserID(sUserID);
            calendar.setTimeInMillis(udt.lastUpdated);
            int udtLastUpdatedDOY = calendar.get(Calendar.DAY_OF_YEAR);
            SensorDailyTotals sdt = sdtDao.getTopSensorTotalsByUserID(sUserID);
            calendar.setTimeInMillis(sdt.lastUpdated);
            int sdtLastUpdatedDOY = calendar.get(Calendar.DAY_OF_YEAR);
            boolean updatedSDT = false; boolean updatedUDT = false;
            int iTemp = -1; float fTemp = -1; long lTemp = -1; String sTemp = null;

            // get and check DOY and updated values
            long newSDTlastUpdated =0; long newSDTRowID = 0;
            sKey = SensorDailyTotals.class.getSimpleName();
            if (data.hasKeyWithValueOfType(sKey, Long.class)){
                newSDTRowID = data.getLong(sKey, -1);
                sKey += "_last";
                newSDTlastUpdated = data.getLong(sKey, -1);
            }
            // set SDT device 2 values and Update if applicable
            if ((sdtLastUpdatedDOY == iDOY) && (newSDTRowID > 0)) {  // && (newSDTlastUpdated >= sdt._id)
                sKey = DataType.TYPE_STEP_COUNT_DELTA.getName();
                if (data.hasKeyWithValueOfType(sKey, Integer.class)) {
                    iTemp = data.getInt(sKey, -1);
                    sKey += "_last";
                    lTemp = data.getLong(sKey, -1);
                    if (sdt.device2Step != iTemp && iTemp >= 0 && lTemp >= 0) {
                        sdt.device2Step = iTemp;
                        sdt.lastDevice2Step = lTemp;
                        updatedSDT = true;
                        iResult += 1;
                    }
                }
                iTemp = -1;lTemp = -1;
                sKey = DataType.TYPE_HEART_RATE_BPM.getName();
                if (data.hasKeyWithValueOfType(sKey, Float.class)) {
                    fTemp = data.getFloat(sKey, -1);
                    sKey += "_last";
                    lTemp = data.getLong(sKey, -1);
                    if (sdt.device2BPM != fTemp && fTemp >= 0 && lTemp >= 0) {
                        sdt.device2BPM = fTemp;
                        sdt.lastDevice2BPM = lTemp;
                        updatedSDT = true;
                        iResult += 1;
                    }
                }
                fTemp = -1;lTemp = -1;
                sKey = Sensor.STRING_TYPE_PRESSURE;
                if (data.hasKeyWithValueOfType(sKey, Float.class)) {
                    fTemp = data.getFloat(sKey, -1);
                    sKey += "_last";
                    lTemp = data.getLong(sKey, -1);
                    if (sdt.pressure2 != fTemp && fTemp >= 0 && lTemp >= 0) {
                        sdt.pressure2 = fTemp;
                        sdt.lastDevice2Other = lTemp;
                        updatedSDT = true;
                        iResult += 1;
                    }
                }
                fTemp = -1;lTemp = -1;
                sKey = Sensor.STRING_TYPE_RELATIVE_HUMIDITY;
                if (data.hasKeyWithValueOfType(sKey, Float.class)) {
                    fTemp = data.getFloat(sKey, -1);
                    sKey += "_last";
                    lTemp = data.getLong(sKey, -1);
                    if (sdt.humidity2 != fTemp && fTemp >= 0 && lTemp >= 0) {
                        sdt.humidity2 = fTemp;
                        sdt.lastDevice2Other = lTemp;
                        updatedSDT = true;
                        iResult += 1;
                    }
                }
                sKey = Sensor.STRING_TYPE_AMBIENT_TEMPERATURE;
                fTemp = -1;lTemp = -1;
                if (data.hasKeyWithValueOfType(sKey, Float.class)) {
                    fTemp = data.getFloat(sKey, -1);
                    sKey += "_last";
                    lTemp = data.getLong(sKey, -1);
                    if (sdt.temperature2 != fTemp && fTemp >= 0 && lTemp >= 0) {
                        sdt.temperature2 = fTemp;
                        sdt.lastDevice2Other = lTemp;
                        updatedSDT = true;
                        iResult += 1;
                    }
                }
            }
            // get and check DOY and updated values
            long newUDTlastUpdated =0; long newUDTRowID = 0;
            sKey = UserDailyTotals.class.getSimpleName();
            if (data.hasKeyWithValueOfType(sKey, Long.class)){
                newUDTRowID = data.getLong(sKey, -1);
                sKey += "_last";
                newUDTlastUpdated = data.getLong(sKey, -1);
            }
            // set UDT values and Update if applicable
            if ((udtLastUpdatedDOY == iDOY) && (newUDTRowID > 0 && newUDTlastUpdated > 0) && (udt.lastUpdated < newUDTlastUpdated)) {
                sKey = DataType.TYPE_HEART_POINTS.getName();
                fTemp = -1;
                if (data.hasKeyWithValueOfType(sKey, Float.class)) {
                    fTemp = data.getFloat(sKey, -1);
                    if (udt.heartIntensity != fTemp && fTemp >= 0) {
                        udt.heartIntensity = fTemp;
                        udt.lastUpdated = newUDTlastUpdated;
                        updatedUDT = true;
                        iResult += 1;
                    }
                }
                sKey = DataType.TYPE_DISTANCE_DELTA.getName();
                fTemp = -1;
                if (data.hasKeyWithValueOfType(sKey, Float.class)) {
                    fTemp = data.getFloat(sKey, -1);
                    if (udt.distanceTravelled != fTemp && fTemp >= 0) {
                        udt.distanceTravelled = fTemp;
                        udt.lastUpdated = newUDTlastUpdated;
                        updatedUDT = true;
                        iResult += 1;
                    }
                }
                sKey = DataType.TYPE_CALORIES_EXPENDED.getName();
                fTemp = -1;
                if (data.hasKeyWithValueOfType(sKey, Float.class)) {
                    fTemp = data.getFloat(sKey, -1);
                    if (udt.caloriesExpended != fTemp && fTemp >= 0) {
                        udt.caloriesExpended = fTemp;
                        udt.lastUpdated = newUDTlastUpdated;
                        updatedUDT = true;
                        iResult += 1;
                    }
                }
                sKey = DataType.TYPE_MOVE_MINUTES.getName();
                iTemp = -1;
                if (data.hasKeyWithValueOfType(sKey, Integer.class)) {
                    iTemp = data.getInt(sKey, -1);
                    if (udt.activeMinutes != iTemp && iTemp >= 0) {
                        udt.activeMinutes = iTemp;
                        udt.lastUpdated = newUDTlastUpdated;
                        updatedUDT = true;
                        iResult += 1;
                    }
                }
                sKey = DataType.TYPE_LOCATION_SAMPLE.getName();
                if (data.hasKeyWithValueOfType(sKey, String.class)) {
                    sTemp = data.getString(sKey);
                    if (!udt.lastLocation.equals(sTemp)){
                        udt.lastLocation = sTemp;
                        updatedUDT = true;
                        iResult += 1;
                    }
                }
            }
            if (iResult > 0){
                if (updatedSDT) sdtDao.update(sdt);
                if (updatedUDT) udtDao.update(udt);
                Intent broadcastIntent = new Intent(Constants.INTENT_TOTALS_REFRESH);
                broadcastIntent.putExtra(Constants.KEY_FIT_USER, sUserID);
                broadcastIntent.putExtra(Constants.KEY_FIT_DEVICE_ID, sDeviceID);
                if (updatedUDT) broadcastIntent.putExtra(UserDailyTotals.class.getSimpleName(), udt);
                if (updatedSDT) broadcastIntent.putExtra(SensorDailyTotals.class.getSimpleName(), sdt);
                broadcastIntent.putExtra(Constants.KEY_COMM_TYPE, (updatedSDT) ? 2 : 1);
                broadcastIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                Context context = getApplicationContext();
                context.sendBroadcast(broadcastIntent);
            }
            Log.e(LOG_TAG, "successfully finished " + iResult);
            Data outputData = new Data.Builder()
                    .putString(Constants.KEY_FIT_USER, sUserID)
                    .putInt(Constants.KEY_RESULT, iResult)
                    .build();
            return Result.success(outputData);
        }catch (Exception e){
            e.printStackTrace();
            return Result.failure();
        }

    }
}
