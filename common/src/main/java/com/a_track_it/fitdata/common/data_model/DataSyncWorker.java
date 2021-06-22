package com.a_track_it.fitdata.common.data_model;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DataSyncWorker extends Worker {
    private static final String LOG_TAG = DataSyncWorker.class.getSimpleName();
    private ObjectAggregateDao mObjAggregateDao;
    private WorkoutDao mWorkoutDao;
    private WorkoutSetDao mWorkoutSetDao;
    private WorkoutMetaDao mWorkoutMetaDao;
    private ExerciseDao mExerciseDao;
    private BodypartDao mBodypartDao;
    private ReferencesTools referencesTools;
    private Gson gson;
//    ArrayList<Workout> workoutArrayList = new ArrayList<>();
//    ArrayList<WorkoutSet> workoutSetArrayList = new ArrayList<>();
//    ArrayList<WorkoutMeta> workoutMetaArrayList = new ArrayList<>();

    public DataSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        referencesTools = ReferencesTools.setInstance(context);
        WorkoutRoomDatabase db = WorkoutRoomDatabase.getDatabase(context.getApplicationContext());
        mWorkoutDao = db.workoutDao();
        mWorkoutSetDao = db.workoutSetDao();
        mWorkoutMetaDao = db.workoutMetaDao();
        mExerciseDao = db.exerciseDao();
        mBodypartDao = db.bodypartDao();
        mObjAggregateDao = db.aggregateDao();
        gson = new Gson();
    }

    /** startWork
     read un-synchronized workouts and sets - read summaries and insert history
     **/
    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String intentAction = data.getString(Constants.KEY_FIT_ACTION);
        String host = data.getString(Constants.KEY_FIT_HOST);
        String sUserID = data.getString(Constants.KEY_FIT_USER);
        String sWorkout = data.hasKeyWithValueOfType(Workout.class.getSimpleName(),String.class) ? data.getString(Workout.class.getSimpleName()) : null;
        String sWorkSet = data.hasKeyWithValueOfType(WorkoutSet.class.getSimpleName(),String.class) ? data.getString(WorkoutSet.class.getSimpleName()) : null;
        String sExercise = data.hasKeyWithValueOfType(Exercise.class.getSimpleName(),String.class) ? data.getString(Exercise.class.getSimpleName()) : null;
        String sBodypart = data.hasKeyWithValueOfType(Bodypart.class.getSimpleName(),String.class) ? data.getString(Bodypart.class.getSimpleName()) : null;
        String sWorkMeta = data.hasKeyWithValueOfType(WorkoutMeta.class.getSimpleName(),String.class) ? data.getString(WorkoutMeta.class.getSimpleName()) : null;
        String sListSets = data.hasKeyWithValueOfType(Constants.KEY_LIST_SETS, String.class) ? data.getString(Constants.KEY_LIST_SETS) : null;
        String sObjectAgg = data.hasKeyWithValueOfType(ObjectAggregate.class.getSimpleName(),String.class) ? data.getString(ObjectAggregate.class.getSimpleName()) : null;
        int setSize = data.hasKeyWithValueOfType(Constants.KEY_FIT_SETS, Integer.class) ? data.getInt(Constants.KEY_FIT_SETS,0) : 0;
        int commType = data.getInt(Constants.KEY_COMM_TYPE, Constants.COMM_TYPE_DEVICE_UPDATE); // will set device_sync using DeviceUpdateWorker
        Workout w = new Workout();
        WorkoutSet set = new WorkoutSet();
        WorkoutMeta meta = new WorkoutMeta();
        Exercise exercise = new Exercise();
        Bodypart bodypart = new Bodypart();
        List<WorkoutSet> workoutSets = new ArrayList<>();
        long timeMs = System.currentTimeMillis();
        long[] setArray; int iDirtyCount = 0;
        int iRetVal = 0; int setsCount = 0; int workoutCount = 0;
        try {
            if (commType == Constants.COMM_TYPE_TABLE_INFO){
                String sTableName = getInputData().getString(Constants.MAP_DATA_TYPE);
                Log.w(LOG_TAG,"new work TABLE " + intentAction + Constants.ATRACKIT_SPACE + sTableName);
            }
            else
                Log.w(LOG_TAG,"new work BUNDLE " + intentAction + Constants.ATRACKIT_SPACE + sWorkout + " " + sWorkSet + " " + sExercise);

            if ((sBodypart != null) && (sBodypart.length() > 0)) {
                bodypart = gson.fromJson(sBodypart, Bodypart.class);
                iDirtyCount++;
                List<Bodypart> tester = (bodypart._id > 0) ? mBodypartDao.getBodypartById(bodypart._id) : null;
                if ((tester == null) || (tester.size() ==0)) {
                    ++iRetVal;
                   // bodypart.lastUpdated = timeMs;
                    mBodypartDao.insert(bodypart);
                    Log.w(LOG_TAG, "adding bodypart " + bodypart.toString());
                } else {
                    if (bodypart.lastUpdated == 0 || tester.get(0).lastUpdated < bodypart.lastUpdated) {
                        //bodypart.lastUpdated = timeMs;
                        mBodypartDao.update(bodypart);
                        Log.w(LOG_TAG, "updating bodypart " + bodypart.toString());
                    }
                }

            }
            if ((sExercise != null) && (sExercise.length() > 0)) {
                exercise = gson.fromJson(sExercise, Exercise.class);
                iDirtyCount++;
                List<Exercise> tester = (exercise._id > 0) ? mExerciseDao.getExerciseById(exercise._id) : null;
                if ((tester == null) || (tester.size() ==0)) {
                    ++iRetVal;
                  //  exercise.lastUpdated = timeMs;
                    mExerciseDao.insert(exercise);
                    Log.w(LOG_TAG, "adding exercise " + exercise.toString());
                } else {
                    if (exercise.lastUpdated == 0 || tester.get(0).lastUpdated < exercise.lastUpdated) {
                   //     exercise.lastUpdated = timeMs;
                        mExerciseDao.update(exercise);
                        Log.w(LOG_TAG, "updating exercise " + exercise.toString());
                    }
                }
            }
            if ((sWorkout != null) && (sWorkout.length() > 0)) {
                w = gson.fromJson(sWorkout, Workout.class);
                iDirtyCount++;
                workoutCount++;
                List<Workout> workoutTester = (w._id > 0) ? mWorkoutDao.getWorkoutById(w._id): null;
                if (intentAction.equals(Constants.INTENT_WORKOUT_DELETE)){
                    if ((workoutTester != null) || (workoutTester.size() != 0)) {
                        mWorkoutSetDao.deleteByWorkoutID(w._id);
                        mWorkoutDao.deleteById(w._id);
                    }
                }else
                    if ((workoutTester == null) || (workoutTester.size() == 0)) {
                        w.device_sync = timeMs;
                        mWorkoutDao.insert(w);
                        Log.w(LOG_TAG, "adding workout " + w.toString());
                        ++iRetVal;
                    } else {
                        Workout test = workoutTester.get(0);
                        if (test != null)
                        if ((w.device_sync > test.device_sync)
                                || (test.lastUpdated < w.lastUpdated)) {
                            w.device_sync = timeMs;
                            mWorkoutDao.update(w);
                            Log.w(LOG_TAG, "updating workout " + w.toString());
                        }
                    }
            }
            if ((sWorkSet != null) && (sWorkSet.length() > 0)) {
                set = gson.fromJson(sWorkSet, WorkoutSet.class);
                if (set.workoutID == 0) set.workoutID = 1L;
                // Log.e(LOG_TAG, set.shortText());
                iDirtyCount++;
                List<WorkoutSet> tester = (set._id > 0) ? mWorkoutSetDao.getWorkoutSetById(set._id) : null;
                if ((tester == null) || (tester.size() == 0)) {
                    ++iRetVal;
                    set.device_sync = timeMs;
                    Log.w(LOG_TAG, "adding set " + set.toString());
                    try {
                        mWorkoutSetDao.insert(set);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                } else {
                    WorkoutSet test = tester.get(0);
                    if (set != null && test != null) {
                        if (test.lastUpdated < set.lastUpdated) {
                            set.device_sync = timeMs;
                            mWorkoutSetDao.update(set);
                            Log.w(LOG_TAG, "updating set " + set.toString());
                        }
                    }
                }

            }
            if ((sWorkMeta != null) && (sWorkMeta.length() > 0)) {
                iDirtyCount++;
                meta = gson.fromJson(sWorkMeta, WorkoutMeta.class);
                if (meta.workoutID == 0) meta.workoutID = 1L;
                Log.e(LOG_TAG, meta.toString());
                List<WorkoutMeta> tester = (meta._id == 0) ? null: mWorkoutMetaDao.getMetaById(meta._id);
                if ((tester == null) || (tester.size() == 0)) {
                    ++iRetVal;
                    meta.device_sync = timeMs;
                    Log.w(LOG_TAG, "adding meta " + meta.toString());
                    try{
                    mWorkoutMetaDao.insert(meta);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                } else {
                    if (meta.device_sync == 0 || (tester.get(0).device_sync < meta.device_sync)) {
                        meta.device_sync = timeMs;
                        mWorkoutMetaDao.update(meta);
                        Log.w(LOG_TAG, "updating meta " + meta.toString());
                    }
                }
            }
            if ((sListSets != null) && (setSize > 2)){
                iDirtyCount++;
                workoutSets = Arrays.asList(gson.fromJson(sListSets, WorkoutSet[].class));
                setArray = new long[workoutSets.size()];
                int arrayIndex = 0;
                for(WorkoutSet set2 : workoutSets){
                    if (set2.workoutID == 0) set2.workoutID = 1L;
                    setArray[arrayIndex++] = set2._id;
                    List<WorkoutSet> tester = mWorkoutSetDao.getWorkoutSetById(set2._id);
                    if ((tester == null) || (tester.size() == 0)) {
                        ++iRetVal; ++setsCount;
                        set2.device_sync = timeMs;
                        Log.w(LOG_TAG, "adding set list " + set2.toString());
                        try {
                            mWorkoutSetDao.insert(set2);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    } else {
                        WorkoutSet test = tester.get(0);
                        if (test != null)
                            if (set2.device_sync == null || set.device_sync == null || (test.device_sync < set2.device_sync)
                            || (test.lastUpdated < set2.lastUpdated)) {
                                set2.device_sync = timeMs;
                                mWorkoutSetDao.update(set2);
                                Log.w(LOG_TAG, "updating set list " + set2.toString());
                            }
                    }
                }
            }
            else
                setArray = new long[0];

            if ((sObjectAgg != null) && (sObjectAgg.length() > 0)) {
                ObjectAggregate objectAggregate = gson.fromJson(sObjectAgg, ObjectAggregate.class);
                iDirtyCount++;
                List<ObjectAggregate> tester = (objectAggregate.objectID > 0) ? mObjAggregateDao.getAggregateByUserTypeId(sUserID, objectAggregate.objectType,objectAggregate.objectID) : null;
                if ((tester == null) || (tester.size() ==0)) {
                    ++iRetVal;
                    Log.w(LOG_TAG, "adding objAgg " + objectAggregate.toString());
                    try{
                        mObjAggregateDao.insert(objectAggregate);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    if (objectAggregate.lastUpdated == 0 || tester.get(0).lastUpdated < objectAggregate.lastUpdated) {
                        mObjAggregateDao.update(objectAggregate);
                        Log.w(LOG_TAG, "updating objAgg " + bodypart.toString());
                    }
                }
            }

            List<OneTimeWorkRequest> requestList = new ArrayList<>();
            // clean-up sets removing not started
            if (iDirtyCount > 0 && (workoutCount > 0)) {
                Data.Builder builder = new Data.Builder();
                builder.putString(Constants.KEY_FIT_USER, w.userID);
                builder.putLong(Constants.KEY_FIT_WORKOUTID, w._id);
                OneTimeWorkRequest workRequest =
                        new OneTimeWorkRequest.Builder(SessionCleanupWorker.class)
                                .setInputData(builder.build())
                                .build();
                Data.Builder builder2 = new Data.Builder();
                builder2.putInt(FitnessMetaWorker.ARG_ACTION_KEY, Constants.TASK_ACTION_STOP_SESSION); // all
                builder2.putLong(FitnessMetaWorker.ARG_START_KEY, w.start);
                builder2.putLong(FitnessMetaWorker.ARG_END_KEY, w.end);
                builder2.putLong(FitnessMetaWorker.ARG_WORKOUT_ID_KEY, w._id);
                builder2.putLong(FitnessMetaWorker.ARG_SET_ID_KEY, 0);
                Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                OneTimeWorkRequest oneTimeWorkRequest =
                        new OneTimeWorkRequest.Builder(FitnessMetaWorker.class)
                                .setInputData(builder2.build())
                                .setInitialDelay(20, TimeUnit.SECONDS)
                                .setConstraints(constraints)
                                .build();
                requestList.add(oneTimeWorkRequest);
                WorkManager mWorkManager = WorkManager.getInstance(getApplicationContext());
                mWorkManager.enqueue(requestList);
            }
            Data outputData = new Data.Builder()
                    .putString(Constants.KEY_FIT_ACTION, intentAction)
                    .putString(Constants.KEY_FIT_HOST, host)
                    .putString(Constants.KEY_FIT_USER, sUserID)
                    .putLong(Constants.KEY_FIT_WORKOUTID, w._id)
                    .putLong(Constants.KEY_FIT_WORKOUT_SETID, set._id)
                    .putLong(Constants.KEY_FIT_WORKOUT_METAID, meta._id) // 0 if not loaded
                    .putLongArray(Constants.KEY_LIST_SETS, setArray)
                    .putInt(Constants.KEY_RESULT, iRetVal)
                    .putInt(Constants.KEY_FIT_SETS, setsCount)
                    .build();
           // Log.e(LOG_TAG, outputData.);
            return Result.success(outputData);
        }catch (Exception e){
            e.printStackTrace();
            iRetVal = 0;
            Data outputData = new Data.Builder()
                    .putString(Constants.KEY_FIT_ACTION, intentAction)
                    .putString(Constants.KEY_FIT_HOST, host)
                    .putString(Constants.KEY_FIT_USER, sUserID)
                    .putInt(Constants.KEY_RESULT, iRetVal)
                    .build();
            return Result.failure(outputData);
        }
    }

}
