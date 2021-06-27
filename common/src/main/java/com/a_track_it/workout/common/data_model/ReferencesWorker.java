package com.a_track_it.workout.common.data_model;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.R;

import java.util.ArrayList;
import java.util.List;

import static com.a_track_it.workout.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.workout.common.Constants.KEY_FIT_TYPE;

public class ReferencesWorker extends Worker {
    private static final String LOG_TAG = ReferencesWorker.class.getSimpleName();
    public ReferencesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    private int getFitnessActivityColorById(int id) {
        int result = R.color.other;
        switch (id) {
            case (int)Constants.WORKOUT_TYPE_STEPCOUNT:
                result = R.color.steps;
                break;
            case (int)Constants.WORKOUT_TYPE_TIME:
            case 106: case 35: case 104: case  50: case  52: case  70: case  61: case 62: case  63: case  64: case  65: case  66: case  67
                    : case 68: case  71: case  73: case  74: case  75: case  89: case  90: case  91:
                result = R.color.calendar;
                break;
            case (int)Constants.WORKOUT_TYPE_INVEHICLE:
            case 1:
            case 14:
            case 19:
            case 13:
            case 2:
            case 56:
            case 57:
            case 58:
                result = R.color.driving;
                break;
            case (int)Constants.WORKOUT_TYPE_ARCHERY:
            case 102:
            case 40:
            case 48:
            case 53:
            case 59:
            case 60:
            case 79:
            case 81:
            case 82:
            case 83:
            case 84:
            case 92:
            case 96:
            case 99:
                result = R.color.archery;
                break;
            case (int)Constants.WORKOUT_TYPE_AEROBICS:
            case (int)Constants.WORKOUT_TYPE_RUNNING:
                result = R.color.running;
                break;
            case (int)Constants.WORKOUT_TYPE_VIDEOGAME:
            case 21:
            case 117:
            case 25:
            case 39:
            case 103:
            case 118:
            case 49:
            case 54:
            case 77:
            case 78:
            case 88:
            case 7:
            case 93:
            case 94:
            case 95:
                result = R.color.walking;
                break;
            case 10: case 11: case 12: case 20: case 23:
            case 27: case 28: case 29: case 32: case 33: case 34:
            case 36: case 37: case 69: case 51: case 55: case 120:
            case 76: case 85: case 86: case 87:
                result = R.color.golfing;
                break;
            case 97:
            case (int)Constants.WORKOUT_TYPE_STRENGTH:
            case 22:
            case 41: case 47: case 113: case 114: case 115:
                result = R.color.lifting;
                break;
            default:
                result = R.color.steps;
                break;
        }
        return result;
    }

    private String getResistanceType(int rt){
        String sRet = "N/A";
        switch (rt){
            case 1:
                sRet ="Barbell";
                break;
            case 6:
                sRet ="Bodyweight";
                break;
            case 2:
                sRet ="Cable";
                break;
            case 3:
                sRet ="Dumbell";
                break;
            case 4:
                sRet ="Kettlebell";
                break;
            case 5:
                sRet ="Machine";
                break;
            case 0:
                sRet ="N/A";
                break;
        }
        return sRet;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context applicationContext = getApplicationContext();
            WorkoutRoomDatabase db = WorkoutRoomDatabase.getDatabase(applicationContext);
            final BodypartDao mBodypartDao;
            final ExerciseDao mExerciseDao;
            final FitnessActivityDao mFADao;
            final BodyRegionDao mBodyRegionDao;
            final ConfigurationDao mConfigurationDao;
            List<BodyRegion> regionList = new ArrayList<>();
            List<FitnessActivity> fitnessActivityArrayList = new ArrayList<>();
            List<Bodypart> bodypartArrayList = new ArrayList<>();
            List<Exercise> exerciseArrayList = new ArrayList<>();
            int iTotal = 0;
            mBodyRegionDao = db.bodyRegionDao();
            mBodypartDao = db.bodypartDao();
            mExerciseDao = db.exerciseDao();
            mFADao = db.fitnessActivityDao();
            mConfigurationDao = db.configDao();
            if (mConfigurationDao.liveAllConfigs(applicationContext.getPackageName()).getValue().size() < 2) {
                Resources resources = applicationContext.getResources();

                int[] reg_ids = resources.getIntArray(R.array.region_ids);
                String[] reg_fullname = resources.getStringArray(R.array.region_names);

                String[] names = resources.getStringArray(R.array.activity_names_sorted);
                int[] ids = resources.getIntArray(R.array.activity_id_names_sorted);
                String[] icons = resources.getStringArray(R.array.activity_icon_name_sorted);
                String[] identifiers = resources.getStringArray(R.array.activity_ident_name_sorted);
                int i = 0; String sMsg;
                if (mBodyRegionDao.getById(1) != null) {
                   // Log.w(LOG_TAG, "Setup  already 60");
                    iTotal = 60;
                } else {
                    while (i < reg_ids.length) {
                        BodyRegion region = new BodyRegion();
                        region._id = reg_ids[i];
                        region.regionName = reg_fullname[i];
                        regionList.add(region);
                        i++;
                    }
                    if (i > 0) {
                        iTotal += i;
                        mBodyRegionDao.insertAll(regionList);
                        sMsg = "added regions " + i;
                        Log.w(LOG_TAG, sMsg);
                        Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                        msgIntent.setAction(INTENT_MESSAGE_TOAST);
                        msgIntent.putExtra(Constants.INTENT_EXTRA_MSG, sMsg);
                        msgIntent.putExtra(KEY_FIT_TYPE, 2);
                        msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                        //LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(msgIntent);
                        applicationContext.sendBroadcast(msgIntent);
                    }
                    i = 0;
                    while (i < ids.length) {
                        FitnessActivity fa = new FitnessActivity();
                        fa._id = ids[i];
                        fa.name = names[i];
                        fa.resource_id = applicationContext.getResources().getIdentifier(icons[i], "drawable", applicationContext.getPackageName());
                        fa.color = getFitnessActivityColorById(ids[i]);
                        fa.identifier = identifiers[i];
                        mFADao.insert(fa);
                        fitnessActivityArrayList.add(fa);
                        i++;
                    }
                    if (i > 0) {
                        iTotal += i;
                        mFADao.insertAll(fitnessActivityArrayList);
                        sMsg = "added activities " + i;
                        Log.w(LOG_TAG, sMsg);
                        Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                        msgIntent.setAction(INTENT_MESSAGE_TOAST);
                        msgIntent.putExtra(Constants.INTENT_EXTRA_MSG, sMsg);
                        msgIntent.putExtra(KEY_FIT_TYPE, 2);
                        msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                        applicationContext.sendBroadcast(msgIntent);
                        //LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(msgIntent);
                    }

                    int[] bp_ids = resources.getIntArray(R.array.bodypart_ids);
                    String[] bp_fullname = resources.getStringArray(R.array.bodypart_fullnames);
                    String[] bp_shortname = resources.getStringArray(R.array.bodypart_shortnames);
                    int[] bp_regionid = resources.getIntArray(R.array.bodypart_regionids);
                    String[] bp_regionNames = resources.getStringArray(R.array.bodypart_regionnames);
                    int[] bp_setmins = resources.getIntArray(R.array.bodypart_setmin);
                    int[] bp_setmaxs = resources.getIntArray(R.array.bodypart_setmax);
                    int[] bp_repmins = resources.getIntArray(R.array.bodypart_repmin);
                    int[] bp_repmaxs = resources.getIntArray(R.array.bodypart_repmax);
                    String[] bp_powerfactors = resources.getStringArray(R.array.bodypart_power_rating);
                    int[] bp_parentids = resources.getIntArray(R.array.bodypart_parentids);
                    String[] bp_parentNames = resources.getStringArray(R.array.bodypart_parentnames);
                    i = 0;
                    while (i < bp_ids.length) {
                        Bodypart bodypart = new Bodypart();
                        bodypart._id = (long) bp_ids[i];
                        bodypart.shortName = bp_shortname[i];
                        bodypart.fullName = bp_fullname[i];
                        if (bp_regionid[i] > 0)
                            bodypart.regionID = (long)bp_regionid[i];
                        bodypart.regionName = bp_regionNames[i];
                        bodypart.setMin = bp_setmins[i];
                        bodypart.setMax = bp_setmaxs[i];
                        bodypart.repMin = bp_repmins[i];
                        bodypart.repMax = bp_repmaxs[i];
                        if (bp_parentids[i] > 0) {
                            bodypart.parentID = (long) bp_parentids[i];
                            bodypart.parentName = bp_parentNames[i];
                        }
                        if ((bp_powerfactors[i] != null) && (bp_powerfactors[i].length() > 0))
                            bodypart.powerFactor = Float.parseFloat(bp_powerfactors[i]);

                        bodypartArrayList.add(bodypart);
                        i++;
                    }
                    if (i > 0) {
                        iTotal += i;
                        mBodypartDao.insertAll(bodypartArrayList);
                        Log.w(LOG_TAG, "added bodyparts " + i);
                    }
                    i = 0;  // start again with exercises now!
                    String ex_fullname[] = resources.getStringArray(R.array.exercise_name_list);
                    String ex_othernames[] = resources.getStringArray(R.array.exercise_other_name_list);
                    int[] ex_resistancetype = resources.getIntArray(R.array.exercise_resistance_type);
                    int[] ex_bodypartcount = resources.getIntArray(R.array.exercise_bodypart_count);
                    int[] ex_bodypart1 = resources.getIntArray(R.array.exercise_bodypart1_id);
                    String[] ex_bp1_name = resources.getStringArray(R.array.exercise_bodypart1_name);
                    int[] ex_bodypart2 = resources.getIntArray(R.array.exercise_bodypart2_id);
                    String[] ex_bp2_name = resources.getStringArray(R.array.exercise_bodypart2_name);
                    int[] ex_bodypart3 = resources.getIntArray(R.array.exercise_bodypart3_id);
                    String[] ex_bp3_name = resources.getStringArray(R.array.exercise_bodypart3_name);
                    int[] ex_bodypart4 = resources.getIntArray(R.array.exercise_bodypart4_id);
                    String[] ex_bp4_name = resources.getStringArray(R.array.exercise_bodypart4_name);
                    String[] ex_powerfactor = resources.getStringArray(R.array.exercise_power_factor);
                    while (i < ex_fullname.length) {
                        Exercise exercise = new Exercise();
                        exercise._id = (i + 1);
                        exercise.name = ex_fullname[i];
                        exercise.otherNames = ex_othernames[i];
                        exercise.resistanceType =(long)ex_resistancetype[i];
                        exercise.resistanceTypeName = getResistanceType(ex_resistancetype[i]);
                        exercise.bodypartCount = ex_bodypartcount[i];
                        if (ex_bodypart1[i] > 0) {
                            exercise.first_BPID = (long)ex_bodypart1[i];
                            exercise.first_BPName = ex_bp1_name[i];
                        }
                        if (ex_bodypart2[i] > 0) {
                            exercise.second_BPID = (long)ex_bodypart2[i];
                            exercise.second_BPName = ex_bp2_name[i];
                        }
                        if (ex_bodypart3[i] > 0) {
                            exercise.third_BPID = (long)ex_bodypart3[i];
                            exercise.third_BPName = ex_bp3_name[i];
                        }
                        if (ex_bodypart4[i] > 0) {
                            exercise.fourth_BPID = (long)ex_bodypart4[i];
                            exercise.fourth_BPName = ex_bp4_name[i];
                        }
                        if ((ex_powerfactor[i] != null) && (ex_powerfactor[i].length() > 0))
                            exercise.powerFactor = Float.parseFloat(ex_powerfactor[i]);
                        exerciseArrayList.add(exercise);
                        i++;

                    }
                    if (i > 0) {
                        iTotal += i;
                        mExerciseDao.insertAll(exerciseArrayList);
                        sMsg = "added exercises " + i;
                        Log.w(LOG_TAG, sMsg);
                        Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
                        msgIntent.setAction(INTENT_MESSAGE_TOAST);
                        msgIntent.putExtra(Constants.INTENT_EXTRA_MSG, sMsg);
                        msgIntent.putExtra(KEY_FIT_TYPE, 2);
                        msgIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                       // LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(msgIntent);
                        applicationContext.sendBroadcast(msgIntent);
                    }
                }
            } else {
                // already setup
                iTotal = 60;
            }
            Data outputData = new Data.Builder()
                    .putInt(Constants.KEY_PAYLOAD, iTotal)
                    .build();
            return Result.success(outputData);
        }
        catch (Exception e){
            Data outputData = new Data.Builder()
                    .putInt(Constants.KEY_PAYLOAD, 0)
                    .build();
            return Result.failure(outputData);
        }
    }
}
