package com.a_track_it.fitdata.common.data_model;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.InjectorUtils;
import com.a_track_it.fitdata.common.Utilities;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.a_track_it.fitdata.common.Constants.KEY_FIT_VALUE;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_CALL_TO_LINE;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_LIVE;
import static com.a_track_it.fitdata.common.Constants.WORKOUT_PAUSED;

public class SensorDailyWorker extends Worker {
    private final static String LOG_TAG = SensorDailyWorker.class.getSimpleName();
    public SensorDailyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        int intentAction = getInputData().getInt(Constants.KEY_FIT_ACTION, 0);
        String sUserID = getInputData().getString(Constants.KEY_FIT_USER);
        long activityID = getInputData().getLong(Constants.KEY_FIT_TYPE, -1);
        Calendar calendar = Calendar.getInstance();
        long timeMs = System.currentTimeMillis();
        long endTime = timeMs;
        long startHour = Utilities.getDayStart(calendar, timeMs);
        SensorDailyTotals topSDT = null;
        SensorDailyTotals SDT = null;
        Context context = getApplicationContext();
        WorkoutRepository workoutRepository = InjectorUtils.getWorkoutRepository((Application) context);
        List<Configuration> configList = workoutRepository.getConfigLikeName(Constants.MAP_CURRENT_STATE,sUserID);
        Configuration configHistory = null;
        int currentState = Constants.WORKOUT_INVALID;
        if (configList != null){
            currentState = Math.toIntExact(configList.get(0).longValue);
        }
        long newSDTPeriod = TimeUnit.MINUTES.toMillis(15);
        if (currentState == WORKOUT_LIVE || currentState == WORKOUT_PAUSED || currentState == WORKOUT_CALL_TO_LINE){
            if ((activityID != -1)){
                if (Utilities.isGymWorkout(activityID) || Utilities.isShooting(activityID)) newSDTPeriod = TimeUnit.SECONDS.toMillis(20);
                else
                if (Utilities.isInActiveWorkout(activityID)) newSDTPeriod = TimeUnit.MINUTES.toMillis(2);
                else
                    newSDTPeriod = TimeUnit.MINUTES.toMillis(1);
            }else
                newSDTPeriod = TimeUnit.MINUTES.toMillis(5);
        }else
            newSDTPeriod = TimeUnit.MINUTES.toMillis(10);

        DateTuple dt = workoutRepository.getSensorDailyDateTuple(sUserID, startHour, timeMs);
        boolean bExisting = ((dt != null) && (dt.sync_count > 0));

        if (bExisting) {
            topSDT = workoutRepository.getTopSensorDailyTotal(sUserID);
            if (topSDT != null && (topSDT._id >= startHour)) {
                if ((endTime - topSDT._id) > newSDTPeriod)
                    bExisting = false;
                else
                    SDT = topSDT;
            } else bExisting = false;
        }
        if (!bExisting) {
            Log.e(LOG_TAG, "doSetupSDT not existing - adding new " + Utilities.getTimeDateString(timeMs));
            SDT = new SensorDailyTotals(timeMs, sUserID);
            SDT.userID = sUserID;
            // carry forward!
            if (topSDT != null){
                if (topSDT.activityType != -1) SDT.activityType = topSDT.activityType;
                if (topSDT.lastActivityType > 0) SDT.lastActivityType = topSDT.lastActivityType;
                if (topSDT.deviceStep != 0) SDT.deviceStep = topSDT.deviceStep;
                if (topSDT.lastDeviceStep != 0) SDT.lastDeviceStep = topSDT.lastDeviceStep;
                if (topSDT.deviceBPM != 0) SDT.deviceBPM = topSDT.deviceBPM;
                if (topSDT.lastDeviceBPM != 0) SDT.lastDeviceBPM = topSDT.lastDeviceBPM;
                if (topSDT.fitBPM != 0) SDT.fitBPM = topSDT.fitBPM;
                if (topSDT.lastFitBPM != 0) SDT.lastFitBPM = topSDT.lastFitBPM;
                if (topSDT.fitStep != 0) SDT.fitStep = topSDT.fitStep;
                if (topSDT.lastFitStep != 0) SDT.lastFitStep = topSDT.lastFitStep;
                if (topSDT.pressure != 0) SDT.pressure = topSDT.pressure;
                if (topSDT.temperature != 0) SDT.temperature = topSDT.temperature;
                if (topSDT.humidity != 0) SDT.humidity = topSDT.humidity;
                if (topSDT.lastDeviceOther != 0) SDT.lastDeviceOther = topSDT.lastDeviceOther;
                if (topSDT.fitLat != 0) SDT.fitLat = topSDT.fitLat;
                if (topSDT.fitLng != 0) SDT.fitLng = topSDT.fitLng;
                if (topSDT.fitLocation.length() != 0) SDT.fitLocation = topSDT.fitLocation;
                if (topSDT.device2Step != 0) SDT.device2Step = topSDT.device2Step;
                if (topSDT.lastDevice2Step != 0) SDT.lastDevice2Step = topSDT.lastDevice2Step;
                if (topSDT.device2BPM != 0) SDT.device2BPM = topSDT.device2BPM;
                if (topSDT.lastDevice2BPM != 0) SDT.lastDevice2BPM = topSDT.lastDevice2BPM;
                if (topSDT.pressure2 != 0) SDT.pressure2 = topSDT.pressure2;
                if (topSDT.temperature2 != 0) SDT.temperature2 = topSDT.temperature2;
                if (topSDT.humidity2 != 0) SDT.humidity2 = topSDT.humidity2;
                if (topSDT.lastDevice2Other != 0) SDT.lastDevice2Other = topSDT.lastDevice2Other;
                if (topSDT.lastUpdated != 0) SDT.lastUpdated = topSDT.lastUpdated;
            }
        }
        if (intentAction == 1){
            int iValue = getInputData().getInt(KEY_FIT_VALUE, -1);
            if (iValue > -1) {
                SDT.activityType = iValue;
                SDT.lastActivityType = timeMs;
            }
        }
        if (intentAction == 1){
            long iValue = getInputData().getLong(KEY_FIT_VALUE, -1);
            if (iValue > -1) {
                SDT.activityType = Math.toIntExact(iValue);
                SDT.lastActivityType = timeMs;
            }
        }
        if (SDT.lastUpdated == 0){
            SDT.lastUpdated = timeMs;
            workoutRepository.insertSensorDailyTotal(SDT);
        }else {
            SDT.lastUpdated = timeMs;
            workoutRepository.updateSensorDailyTotal(SDT);

        }
        return Result.success();
    }
}
