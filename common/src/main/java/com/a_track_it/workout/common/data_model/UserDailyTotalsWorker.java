package com.a_track_it.workout.common.data_model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.Utilities;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UserDailyTotalsWorker extends Worker {
    private static final String LOG_TAG = UserDailyTotalsWorker.class.getSimpleName();
    private UserDailyTotalsDao mUserDailyTotalsDao;
    private SensorDailyTotalsDao mSensorDailyTotalsDao;
    private Calendar mCalendar;

    public UserDailyTotalsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        WorkoutRoomDatabase db = WorkoutRoomDatabase.getDatabase(context.getApplicationContext());
        mUserDailyTotalsDao = db.dailyTotalsDao();
        mSensorDailyTotalsDao = db.sdtDao();
        mCalendar = Calendar.getInstance(Locale.getDefault());
    }
    /** doWork
     read un-synchronized
     **/
    @NonNull
    @Override
    public Result doWork() {
        try{
            int iRetVal = 1;
            Data data = getInputData();
            String sUserID = data.getString(Constants.KEY_FIT_USER);
            int iType = getInputData().getInt(Constants.KEY_FIT_TYPE, 0); // 0 = google daily, 1 = location
            String sLoc = getInputData().getString(Constants.KEY_LOC_LOC);
            Log.e(LOG_TAG, "userDailyTotalsWorker " + iType + " " + sLoc);
            final long timeMs = System.currentTimeMillis();
            mCalendar.setTimeInMillis(timeMs);
            long endTime = Utilities.getDayEnd(mCalendar,timeMs);
            long startHour = Utilities.getDayStart(mCalendar,endTime);
            int DOY = mCalendar.get(Calendar.DAY_OF_YEAR);
            DateTuple dt = mUserDailyTotalsDao.getUDTCount(sUserID, startHour, endTime);
            boolean bExisting = ((dt != null) && (dt.sync_count >0));
            boolean bUpdated = false; boolean bSDTUpdated = false;
            UserDailyTotals udt = null;
            SensorDailyTotals sdt = mSensorDailyTotalsDao.getTopSensorTotalsByUserID(sUserID);
            if (bExisting) {
                udt = mUserDailyTotalsDao.getTopUserTotalsByUserID(sUserID);
                mCalendar.setTimeInMillis(udt._id);
                if (DOY != mCalendar.get(Calendar.DAY_OF_YEAR)) udt = null;
                if (sdt != null){
                    mCalendar.setTimeInMillis(sdt._id);
                    if (DOY != mCalendar.get(Calendar.DAY_OF_YEAR)) sdt = null;
                }
            }
            int iTemp = 0;
            float fTemp = 0F;
            long lTemp = 0L;
            // not found
            if ((udt == null) || udt._id == 0){
                udt = new UserDailyTotals();
                udt._id = Utilities.getDayEnd(mCalendar,endTime);
                udt.userID = sUserID;
            }
   //         if (iType == 1) {
                Double dTemp = data.getDouble(Constants.KEY_LOC_LAT, -1D);
                if (dTemp != -1D) udt.lastLatitude = dTemp;
                dTemp = data.getDouble(Constants.KEY_LOC_LNG, -1D);
                if (dTemp != -1D) udt.lastLongitude = dTemp;
                dTemp = data.getDouble(Constants.KEY_LOC_ALT, -1D);
                if (dTemp != -1) udt.lastAltitude = dTemp;
                fTemp = data.getFloat(Constants.KEY_LOC_SPD, -1F);
                if (fTemp != -1) udt.lastSpeed = fTemp;

                if ((sLoc != null) && (sLoc.length() > 0)) udt.lastLocation = sLoc;
                iTemp = data.getInt(Constants.MAP_MOVE_MINS, -1);
                if ((iTemp != -1) && (udt.activeMinutes != iTemp)) {
                    udt.activeMinutes = iTemp;
                    bUpdated = true;
                }
                iTemp = data.getInt(Constants.MAP_STEPS, -1);
                if (iTemp != -1 && (udt.stepCount != iTemp)) {
                    udt.stepCount = iTemp;
                    bUpdated = true;
                    if (sdt != null && sdt.fitStep != udt.stepCount) {
                        sdt.fitStep = udt.stepCount;
                        sdt.lastFitStep = timeMs;
                        bSDTUpdated = true;
                    }
                }
                fTemp = data.getFloat(Constants.MAP_DISTANCE, -1F);
                if (fTemp != -1F && (udt.distanceTravelled != fTemp)) {
                    udt.distanceTravelled = fTemp;
                    bUpdated = true;
                }
                fTemp = data.getFloat(Constants.MAP_HEART_POINTS, -1F);
                if (fTemp != -1F && (udt.heartIntensity != fTemp)) {
                    udt.heartIntensity = fTemp;
                    bUpdated = true;
                }
                lTemp = data.getLong(Constants.MAP_HEART_DURATION, -1);
                if (iTemp != -1 && (udt.heartDuration != fTemp)) {
                    udt.heartDuration = lTemp;
                    bUpdated = true;
                }
                fTemp = data.getFloat(Constants.MAP_CALORIES, -1F);
                if (fTemp != -1F && (udt.caloriesExpended != fTemp)) {
                    udt.caloriesExpended = fTemp;
                    bUpdated = true;
                }
                fTemp = data.getFloat(Constants.MAP_BPM_MIN, -1F);
                if (fTemp != -1F && (udt.minBPM > fTemp)) {
                    udt.minBPM = fTemp;
                    bUpdated = true;
                }
                fTemp = data.getFloat(Constants.MAP_BPM_MAX, -1F);
                if (fTemp != -1F && (udt.maxBPM != fTemp)) {
                    udt.maxBPM = fTemp;
                    bUpdated = true;
                }
                fTemp = data.getFloat(Constants.MAP_BPM_AVG, -1F);
                if (fTemp != -1F && (udt.avgBPM != fTemp)) {
                    udt.avgBPM = fTemp;
                    bUpdated = true;
                    if (sdt != null && sdt.fitBPM != udt.avgBPM) {
                        sdt.fitBPM = udt.avgBPM;
                        sdt.lastFitBPM = timeMs;
                        bSDTUpdated = true;
                    }
                }
                fTemp = data.getFloat(Constants.MAP_SPEED_MIN, -1F);
                if (fTemp != -1F && (udt.minSpeed > fTemp)) {
                    udt.minSpeed = fTemp;
                    bUpdated = true;
                }
                fTemp = data.getFloat(Constants.MAP_SPEED_MAX, -1F);
                if (fTemp != -1F && (udt.maxSpeed != fTemp)) {
                    udt.maxSpeed = fTemp;
                    bUpdated = true;
                }
                fTemp = data.getFloat(Constants.MAP_SPEED_AVG, -1F);
                if (fTemp != -1F && (udt.avgSpeed != fTemp)) {
                    udt.avgSpeed = fTemp;
                    bUpdated = true;
                }
                String sList = data.getString(Workout.class.getSimpleName() + "_list");
                if (sList != null && sList.length() > 0) {
                    List<Workout> workoutList = new ArrayList<>();
                    Gson gson = new Gson();
                    workoutList = Arrays.asList(gson.fromJson(sList, Workout[].class));
                    for (Workout w : workoutList) {
                        int idSearch = Math.toIntExact(w.activityID);
                        switch (idSearch) {
                            case 0:
                                if (udt.durationVehicle != w.duration) {
                                    bUpdated = true;
                                    udt.durationVehicle = w.duration;
                                }
                                break;
                            case 1:
                                if (udt.durationBiking != w.duration) {
                                    bUpdated = true;
                                    udt.durationBiking = w.duration;
                                }
                                break;
                            case 2:
                                if (udt.durationOnFoot != w.duration) {
                                    bUpdated = true;
                                    udt.durationOnFoot = w.duration;
                                }
                                break;
                            case 3:
                                if (udt.durationStill != w.duration) {
                                    bUpdated = true;
                                    udt.durationStill = w.duration;
                                }
                                break;
                            case 4:
                                if (udt.durationUnknown != w.duration) {
                                    bUpdated = true;
                                    udt.durationUnknown = w.duration;
                                }
                                break;
                            case 5:
                                if (udt.durationTilting != w.duration) {
                                    bUpdated = true;
                                    udt.durationTilting = w.duration;
                                }
                                break;
                            case 7:
                                if (udt.durationWalking != w.duration) {
                                    bUpdated = true;
                                    udt.durationWalking = w.duration;
                                }
                                break;
                            case 8:
                                if (udt.durationRunning != w.duration) {
                                    bUpdated = true;
                                    udt.durationRunning = w.duration;
                                }
                                break;
                        }
                    }
                }
                Log.e(LOG_TAG, "UDT DB action updated:" + bExisting + " " + iType + " " + udt.toString());
                if (bUpdated || !bExisting) {
                    udt.lastUpdated = timeMs;
                    if (bExisting)
                        mUserDailyTotalsDao.update(udt);
                    else
                        mUserDailyTotalsDao.insert(udt);
                }
          //  }
            if (sdt != null || bSDTUpdated){
                sdt.fitLng = (float)udt.lastLongitude;
                sdt.fitLat = (float)udt.lastLatitude;
                sdt.fitLocation = udt.lastLocation;

                bSDTUpdated = true;
                sdt.lastUpdated = timeMs;
                mSensorDailyTotalsDao.update(sdt);
            }
            if (!bExisting) iRetVal++;
            Intent broadcastIntent = new Intent(Constants.INTENT_TOTALS_REFRESH);
            broadcastIntent.putExtra(Constants.KEY_FIT_USER, sUserID);
            broadcastIntent.putExtra(Constants.KEY_COMM_TYPE, iType);
            broadcastIntent.putExtra(UserDailyTotals.class.getSimpleName(), udt);
            if (bSDTUpdated && sdt != null) broadcastIntent.putExtra(SensorDailyTotals.class.getSimpleName(), sdt);
            broadcastIntent.putExtra(Constants.KEY_COMM_TYPE, iType);
            broadcastIntent.putExtra(Constants.KEY_INDEX_METRIC, bUpdated);
            broadcastIntent.putExtra(Constants.KEY_INDEX_FILTER, bExisting);
            broadcastIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
            getApplicationContext().sendBroadcast(broadcastIntent);
            Log.w(LOG_TAG, "sent broadcast " + iType + " " + Constants.INTENT_TOTALS_REFRESH);
            Data outputData = new Data.Builder()
                    .putInt(Constants.KEY_RESULT, iRetVal)
                    .putString(Constants.KEY_FIT_USER, sUserID)
                    .putString(Constants.KEY_FIT_VALUE, UserDailyTotalsWorker.class.getSimpleName())
                    .build();
            return Result.success(outputData);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            return Result.failure();
        }
    }
}
