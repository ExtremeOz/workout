package com.a_track_it.workout.common.data_model;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.model.UserPreferences;

import java.util.List;

public class HeightBodypartWorker extends Worker {
    private static final String LOG_TAG = HeightBodypartWorker.class.getSimpleName();
    private static final String TORSO = "Torso";
    private static final String ARMS = "Arms";
    private static final String SHOULDER = "Shoulders";
    private static final String LEGS = "Legs";
    private static final String CORE = "Core";

    private BodypartDao mBodypartDao;
    UserPreferences userPreferences;
    String sUserId;
    String sHeight;

    public HeightBodypartWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        WorkoutRoomDatabase db = WorkoutRoomDatabase.getDatabase(context.getApplicationContext());
        mBodypartDao = db.bodypartDao();
        sUserId = getInputData().getString(Constants.KEY_FIT_USER);
        if (sUserId.length() > 0) {
            userPreferences = UserPreferences.getPreferences(context, sUserId);
            sHeight = userPreferences.getPrefStringByLabel(Constants.INTENT_PERMISSION_HEIGHT);
        }else sHeight = Constants.ATRACKIT_EMPTY;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if (sHeight.length() > 0) {
                float fHeight = Float.parseFloat(sHeight);
                float fArmsPowerFactor = fHeight / 2;
                float fLegsPowerFactor = fHeight / 4;
                float fAbsPowerFactor = fHeight / 6;

                List<Bodypart> bodypartList = mBodypartDao.getAllBodypartByName();
                if (bodypartList.size() > 1) {
                    for (Bodypart bodypart : bodypartList) {
                        if (bodypart._id > 0) {
                            switch (bodypart.regionName) {
                                case TORSO:
                                case SHOULDER:
                                    bodypart.powerFactor = fArmsPowerFactor;
                                    break;
                                case ARMS:
                                    if (bodypart._id == 23) bodypart.powerFactor = fAbsPowerFactor;
                                    else bodypart.powerFactor = fArmsPowerFactor;
                                    break;
                                case LEGS:
                                    bodypart.powerFactor = fLegsPowerFactor;
                                    break;
                                case CORE:
                                    bodypart.powerFactor = fAbsPowerFactor;
                                    break;
                            }
                        }
                        if (bodypart.powerFactor == 0F) bodypart.powerFactor = fAbsPowerFactor;
                    }
                    mBodypartDao.updateAll(bodypartList);
                }
                return Result.success();
            }else
                return Result.failure();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            return Result.failure();
        }
    }
}
