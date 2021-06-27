package com.a_track_it.workout.common.data_model;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.InjectorUtils;

import java.util.List;

public class DeviceUpdateWorker extends Worker {
    private static final String LOG_TAG = DeviceUpdateWorker.class.getSimpleName();
    private WorkoutRepository mRepository;

    /* DeviceUpdateWorker - update the device_sync for the set object ID
    *
    *
    * */

    public DeviceUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
       // ReferencesTools referencesTools = ReferencesTools.setInstance(context);
        mRepository =  InjectorUtils.getWorkoutRepository((Application) context);

    }
    /** startWork
     read un-synchronized workouts and sets - read summaries and insert history
     **/
    @NonNull
    @Override
    public Result doWork() {
        String intentAction = getInputData().getString(Constants.KEY_FIT_ACTION);
        String UserId = getInputData().getString(Constants.KEY_FIT_USER);
        long workoutID = getInputData().getLong(Constants.KEY_FIT_WORKOUTID, 0L);
        long setID = getInputData().getLong(Constants.KEY_FIT_WORKOUT_SETID, 0L);
        long metaID = getInputData().getLong(Constants.KEY_FIT_WORKOUT_METAID, 0L);
        String sDeviceID = getInputData().getString(Constants.KEY_FIT_DEVICE_ID);
        long timeMs = System.currentTimeMillis();
        int setsCount = getInputData().getInt(Constants.KEY_FIT_SETS, 0);
        long[] setsID;
        setsID = getInputData().getLongArray(Constants.KEY_LIST_SETS);
        Log.e(LOG_TAG, "ID " + UserId + " " + intentAction + " w" + workoutID + " s" + setID + " m" + metaID + " " + setsID.toString());
        int iRetVal = 0;
        try {
            if (workoutID > 0) {
                Workout workoutTester = mRepository.getWorkoutByIdNow(workoutID, UserId,sDeviceID);
                if (workoutTester != null) {
                    String testerID = workoutTester.userID;
                    if ((testerID != null) && testerID.equals(UserId) && (UserId.length() > 0)) {
                        mRepository.updateWorkoutDeviceSync(workoutID, timeMs);
                        iRetVal++;
                        Log.e(LOG_TAG, "update workout " + iRetVal);
                    }
                }

            }

            if (setID > 0) {
                WorkoutSet tester = mRepository.getWorkoutSetByIdNow(setID, UserId,sDeviceID);
                if (tester != null) {
                    String testerID = tester.userID;
                    if (testerID.equals(UserId) && (UserId.length() > 0)) {
                        iRetVal++;
                        mRepository.updateWorkoutSetDeviceSync(setID, timeMs);
                        Log.e(LOG_TAG, "update workoutSet " + iRetVal);
                    }
                }

            }
            if (metaID > 0) {
                List<WorkoutMeta> tester = mRepository.getWorkoutMetaById(metaID, UserId,sDeviceID);
                if ((tester != null) && (tester.size() > 0)) {
                    String testerID = tester.get(0).userID;
                    if (testerID.equals(UserId) && (UserId.length() > 0)) {
                        iRetVal++;
                        mRepository.updateWorkoutMetaDeviceSync(metaID, timeMs);
                        Log.e(LOG_TAG, "update workoutMeta " + iRetVal);
                    }
                }
            }
            if (setsCount > 0){
                for(int arrayIndex = 0; arrayIndex < setsCount; arrayIndex++){
                    long workoutSetID = setsID[arrayIndex];
                    if (workoutSetID > 0) {
                        List<WorkoutSet> tester = mRepository.liveWorkoutSetById(workoutSetID, UserId,sDeviceID).getValue();
                        String testerID = tester.get(0).userID;
                        if (testerID.equals(UserId) && (UserId.length() > 0)) {
                            ++iRetVal;
                            mRepository.updateWorkoutSetDeviceSync(workoutSetID, timeMs);
                        }

                    }
                }
            }
            mRepository.destroyInstance();
            Log.e(LOG_TAG, "device updated " + iRetVal + " " + setsCount);
            Data outputData = new Data.Builder()
                    .putInt(Constants.KEY_RESULT, iRetVal)
                    .putInt(Constants.KEY_FIT_SETS, setsCount)
                    .build();
            return Result.success(outputData);
        }catch (Exception e){
            Log.e(LOG_TAG, "failure " + e.getMessage());
            iRetVal = 0;
            mRepository.destroyInstance();
            Data outputData = new Data.Builder()
                    .putInt(Constants.KEY_RESULT, iRetVal)
                    .build();
            return Result.failure(outputData);
        }
    }

}
