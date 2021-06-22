package com.a_track_it.fitdata.common.data_model;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.Utilities;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SessionCleanupWorker extends Worker {
    private static final String LOG_TAG = SessionCleanupWorker.class.getSimpleName();
    public static final String ARG_RESULT_KEY = "result-key";
    private WorkoutDao mWorkoutDao;
    private WorkoutSetDao mWorkoutSetDao;
    private WorkoutMetaDao mWorkoutMetaDao;
    private ExerciseDao mExerciseDao;
    private BodypartDao mBodypartDao;
    private FitnessActivityDao mFADao;
    private FitnessTypeDao mFTDao;
    private ObjectAggregateDao mAggregateDao;


    public SessionCleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        WorkoutRoomDatabase db = WorkoutRoomDatabase.getDatabase(context.getApplicationContext());
        mWorkoutDao = db.workoutDao();
        mWorkoutSetDao = db.workoutSetDao();
        mWorkoutMetaDao = db.workoutMetaDao();
        mExerciseDao = db.exerciseDao();
        mBodypartDao = db.bodypartDao();
        mAggregateDao = db.aggregateDao();
        mFADao = db.fitnessActivityDao();
        mFTDao = db.fitnessTypeDao();
       // referencesTools = ReferencesTools.setInstance(context);
    }
    /** doWork
     read un-synchronized workouts and sets - read summaries and insert history
     **/
    @NonNull
    @Override
    public Result doWork() {
        try{
            int iRetVal = 0;
            float totalWatts = 0F;
            float totalWeight = 0F;
            float tempFloat = 0F;
            long totalElapsed = 0L;
            long totalCall = 0L;
            long totalRest = 0L;
            long totalPause = 0L;
            int totalSets = 0;
            int totalScore = 0;
            String totalScoreCard = Constants.ATRACKIT_EMPTY;
            String totalXYCard = Constants.ATRACKIT_EMPTY;
            List<String> bodypartList = new ArrayList<>();
            List<String> exerciseList = new ArrayList<>();
            List<Float> bodypartWeightList = new ArrayList();
            List<Float> exerciseWeightList = new ArrayList();
            List<Integer> bodypartSetsList = new ArrayList();
            List<Integer> exerciseSetsList = new ArrayList();
            List<Integer> bodypartRepsList = new ArrayList();
            List<Integer> exerciseRepsList = new ArrayList();
            List<Long> bodypartDurationList = new ArrayList();
            List<Long> exerciseDurationList = new ArrayList();
            final long timeMs = System.currentTimeMillis();
            int totalSteps = 0;
            int totalReps = 0;
            int lastReps = 0;
            long lastEnd = 0l;
            Data data = getInputData();
            String sUserID = data.getString(Constants.KEY_FIT_USER);
            String sDeviceID = data.getString(Constants.KEY_FIT_DEVICE_ID);
            long workoutID = data.getLong(Constants.KEY_FIT_WORKOUTID, 0);
            if (workoutID == 0){
                Log.e(LOG_TAG, "workoutID not found 0");
                return Result.failure();
            }else{
                Log.w(LOG_TAG, "params d " + sDeviceID + " u " + sUserID + " rowid " + workoutID);
            }
            if (sUserID == null) return Result.failure();

            List<WorkoutSet> sl = mWorkoutSetDao.getWorkoutSetByWorkoutID(workoutID, sUserID);
            List<Workout> wl = mWorkoutDao.getWorkoutByIdUserId(workoutID, sUserID);

            if (((wl == null) || (wl.size() == 0)) && ((sl == null) || (sl.size() == 0))) {
                Log.e(LOG_TAG, "workout is null " + workoutID + " " + sUserID + " " + sDeviceID);
                return Result.failure();
            }
            Workout w = wl.get(0);

            WorkoutMeta meta = new WorkoutMeta();
            boolean updateMeta = false;
            List<WorkoutMeta> metaList = mWorkoutMetaDao.getMetaByWorkoutUserDeviceId(workoutID, sUserID,w.deviceID);
            if ((metaList != null) && (metaList.size() > 0))
                meta = metaList.get(0);

            if ((sl != null)) {
                // remove unused or invalid
                if (sl.size() > 0) {
                    Iterator it = sl.iterator();
                    WorkoutSet tester;
                    long startTest = 0;
                    // clean-up existing sets
                    while (it.hasNext()) {
                        tester = (WorkoutSet) it.next();
                        if ((tester.scoreTotal != Constants.FLAG_NON_TRACKING) && (Utilities.isGymWorkout(tester.activityID) && !tester.isValid(false))) {
                            Log.w(LOG_TAG, "removing invalid set " + tester.toString());
                            mWorkoutSetDao.deleteById(tester._id);
                            if (w.setCount > 0) w.setCount--;
                            w.lastUpdated = timeMs;
                            mWorkoutDao.update(w);
                            it.remove();
                        }else {
                            if ((tester.start == 0) && (tester.end == 0)) {
                                mWorkoutSetDao.deleteById(tester._id);
                                it.remove(); // remove not used
                                if (w.setCount > 0) w.setCount--;
                                w.lastUpdated = timeMs;
                                mWorkoutDao.update(w);
                                Log.w(LOG_TAG, "removing dates set " + tester.toString());
                            }
                            else {
                                if (tester.start == 0) {
                                    if (tester.setCount == 1) tester.start = w.start;
                                    else {
                                        WorkoutSet set2 = sl.get(tester.setCount - 2);
                                        tester.start = set2.end;
                                    }
                                    tester.duration = tester.end - tester.start;
                                    tester.lastUpdated = timeMs;
                                    mWorkoutSetDao.update(tester);
                                    Log.w(LOG_TAG, "fixing set start " + tester.toString());
                                }
                                if (tester.end == 0) {
                                    if (tester.setCount == sl.size()) tester.end = w.end;
                                    else {
                                        WorkoutSet set2 = sl.get(tester.setCount); // the next
                                        tester.end = set2.start;
                                    }
                                    tester.duration = tester.end - tester.start;
                                    tester.lastUpdated = timeMs;
                                    mWorkoutSetDao.update(tester);
                                    Log.w(LOG_TAG, "fixing set end " + tester.toString());
                                }
                            }
                            if (tester.start == tester.end) {
                                if (tester.start > startTest) startTest = tester.start;
                                else tester.start = startTest;
                                tester.end = startTest + 60000; // 1 min exercise
                                startTest = tester.end + 45000; // 45 sec rest
                                Log.w(LOG_TAG, "adjusting set times " + tester.start + " " + tester.end);
                                tester.duration = tester.end - tester.start;
                                tester.lastUpdated = timeMs;
                                mWorkoutSetDao.update(tester);
                            }
                            if (tester.duration != (tester.end - tester.start) && (tester.end > tester.start)){
                                Log.w(LOG_TAG, "adjusting set duration " + tester.duration + " new " + (tester.end - tester.start));
                                tester.duration = tester.end - tester.start;
                                tester.lastUpdated = timeMs;
                                mWorkoutSetDao.update(tester);
                            }
                        }
                    }

                    // build the aggregates for each
                    it = sl.iterator();
                    while (it.hasNext()) {
                        WorkoutSet set = (WorkoutSet) it.next();
                        boolean bUpdated = false;
                        totalSets += 1;
                        totalReps += (set.repCount == null)? 0: set.repCount;
                        totalSteps +=(set.stepCount == null)? 0: set.stepCount;
                        totalWeight += (set.weightTotal == null)? 0: set.weightTotal;
                        totalCall += (set.call_duration == null)? 0: set.call_duration;
                        totalRest += (set.rest_duration == null)? 0: set.rest_duration;
                        totalPause += set.pause_duration;
                        totalElapsed += set.duration;
                        if (Utilities.isGymWorkout(w.activityID)) {
                            if ((set.bodypartName != null) && (set.bodypartName.length() > 0)) {
                                if (bodypartList.size() == 0) {
                                    bodypartList.add(set.bodypartName);
                                    bodypartSetsList.add(1);
                                    if (set.repCount != null ) bodypartRepsList.add(set.repCount);
                                    if (set.weightTotal != null) bodypartWeightList.add(set.weightTotal);
                                    bodypartDurationList.add(set.duration);
                                }else if (!bodypartList.contains(set.bodypartName)) {
                                    bodypartList.add(set.bodypartName);
                                    bodypartSetsList.add(1);
                                    if (set.repCount != null) bodypartRepsList.add(set.repCount);
                                    if (set.weightTotal != null) bodypartWeightList.add(set.weightTotal);
                                    bodypartDurationList.add(set.duration);
                                }else{
                                    int iTemp = bodypartRepsList.get(bodypartRepsList.size()-1) + set.repCount;
                                    bodypartRepsList.set(bodypartRepsList.size()-1, iTemp);
                                    iTemp = bodypartSetsList.get(bodypartSetsList.size()-1) + 1;
                                    bodypartSetsList.set(bodypartSetsList.size()-1, iTemp);
                                    float fTemp = bodypartWeightList.get(bodypartWeightList.size()-1) + set.weightTotal;
                                    bodypartWeightList.set(bodypartWeightList.size()-1, fTemp);
                                    long lTemp = bodypartDurationList.get(bodypartDurationList.size()-1) + set.duration;
                                    bodypartDurationList.set(bodypartDurationList.size()-1, lTemp);
                                }
                            }
                            if (set.exerciseName != null && set.exerciseName.length() > 0) {
                                if (exerciseList.size() == 0) {
                                    exerciseList.add(set.exerciseName);
                                    exerciseSetsList.add(1);
                                    exerciseRepsList.add(set.repCount);
                                    exerciseWeightList.add(set.weightTotal);
                                    exerciseDurationList.add(set.duration);
                                }else if (!exerciseList.contains(set.exerciseName)) {
                                    exerciseList.add(set.exerciseName);
                                    exerciseSetsList.add(1);
                                    exerciseRepsList.add(set.repCount);
                                    exerciseWeightList.add(set.weightTotal);
                                    exerciseDurationList.add(set.duration);
                                }else{
                                    int iTemp = exerciseRepsList.get(exerciseRepsList.size()-1) + set.repCount;
                                    exerciseRepsList.set(exerciseRepsList.size()-1, iTemp);
                                    iTemp = exerciseSetsList.get(exerciseSetsList.size()-1) + 1;
                                    exerciseSetsList.set(exerciseSetsList.size()-1, iTemp);
                                    float fTemp = exerciseWeightList.get(exerciseWeightList.size()-1) + set.weightTotal;
                                    exerciseWeightList.set(exerciseWeightList.size()-1, fTemp);
                                    long lTemp = exerciseDurationList.get(exerciseDurationList.size()-1) + set.duration;
                                    exerciseDurationList.set(exerciseDurationList.size()-1, lTemp);
                                }
                            }
                            if (set.weightTotal != null && set.weightTotal > 0F && set.repCount != null) {
                                tempFloat = set.repCount * set.weightTotal;
                                if ((set.wattsTotal == null) || (set.wattsTotal != tempFloat)) {
                                    set.wattsTotal = tempFloat;
                                    bUpdated = true;
                                }
                                totalWatts += tempFloat;
                            }
                        }
                        if (Utilities.isShooting(w.activityID)){
                            String items[] = set.score_card.split(Constants.SHOT_DELIM);
                            List<String> mScoreCard = new ArrayList<>(Arrays.asList(items));
                            int scoreVal = 0;
                            for (String score : mScoreCard){
                                if (score.equals(Constants.SHOT_X))
                                    scoreVal += 10;
                                else {
                                    try {
                                        scoreVal += Integer.parseInt(score);
                                    }catch (Exception e){
                                        scoreVal += 0;
                                    }
                                }
                            }
                            totalScore += scoreVal;
                            if (totalScoreCard != null && totalScoreCard.length() > 0)
                                totalScoreCard += Constants.SHOT_DELIM + set.score_card;
                            else
                                totalScoreCard = set.score_card;
                            if (totalXYCard != null && totalXYCard.length() > 0)
                                totalXYCard += Constants.SHOT_DELIM + set.per_end_xy;
                            else
                                totalXYCard = set.per_end_xy;
                        }
                        if ((set.realElapsedStart != null) && (set.realElapsedEnd != null) && (set.realElapsedStart > 0) && (set.realElapsedEnd > 0))
                            totalElapsed += (set.realElapsedEnd - set.realElapsedStart);
                        else{
                            if ((set.start > 0) && (set.end > 0) && (set.end > set.start))
                                totalElapsed += (set.end - set.start);
                        }

                        // bUpdated = (tempFloat != setMeta.wattsTotal);
                        if (set.setCount == 0 || set.setCount != totalSets) {
                            bUpdated = true;
                            set.setCount = totalSets;
                        }
                        if (bUpdated) {
                            set.lastUpdated = timeMs;
                            mWorkoutSetDao.update(set);
                        }

                    } // for each set
                    w.pause_duration = totalPause;
                    w.rest_duration = totalRest;
                    w.call_duration = totalCall;
                    w.goal_duration = totalElapsed;
                    w.setCount = totalSets;
                    w.repCount = totalReps;
                    if (Utilities.isGymWorkout(w.activityID)) {
                        w.bodypartCount = bodypartList.size();
                        w.exerciseCount = exerciseList.size();
                        w.wattsTotal = totalWatts;
                        w.weightTotal = totalWeight;
                    }
                }
                if ((w.duration != (w.end - w.start)) && (w.end > w.start)) w.duration = w.end - w.start;

                if ((totalSteps > 0) && (w.stepCount < totalSteps)) {
                    w.stepCount = totalSteps;
                }
                if (meta._id > 0){
                    if (Utilities.isGymWorkout(w.activityID) && bodypartList.size() > 0) {
                        meta.description = String.join(Constants.SHOT_DELIM, bodypartList);
                        updateMeta = true;
                    }
                    if (Utilities.isShooting(w.activityID)){
                        w.scoreTotal = totalScore;
                        meta.score_card = totalScoreCard;
                        meta.per_end_xy = totalXYCard;
                        meta.totalScore = totalScore;
                        updateMeta = true;
                    }
                    meta.duration = totalElapsed;
                    if ((meta.setCount == 0 && w.setCount != 0) || (meta.setCount < w.setCount)){
                        updateMeta = true;
                        meta.setCount = w.setCount;
                    }else{
                        if (meta.setCount > w.setCount) w.setCount = meta.setCount;
                    }
                    if ((meta.repCount == 0 && w.repCount != 0) || (meta.repCount < w.repCount)){
                        updateMeta = true;
                        meta.repCount = w.repCount;
                    }
                    if ((meta.rest_duration == 0 && w.rest_duration != 0) || (meta.rest_duration < w.rest_duration)){
                        updateMeta = true;
                        meta.rest_duration = w.rest_duration;
                    }
                    if ((meta.pause_duration == 0 && w.pause_duration != 0) || (meta.pause_duration < w.pause_duration)){
                        updateMeta = true;
                        meta.pause_duration = w.pause_duration;
                    }
                }
                mWorkoutDao.update(w);
                if (updateMeta){
                    meta.last_sync = timeMs;
                    mWorkoutMetaDao.update(meta);
                }
                mFADao.updateById(w.activityID,timeMs);
                List<FitnessType> listFT = mFTDao.getActivityTypeByActivityId(w.activityID);
                if ((listFT != null) && (listFT.size() > 0)){
                    FitnessType ft = listFT.get(0);
                    mFTDao.updateById(ft._id, timeMs);
                    Log.w(LOG_TAG, "updating FT " + ft.name);
                }
                if (Utilities.isGymWorkout(w.activityID) && (sl.size() > 0)) {
                    List<SetAggregateTuple> aggWorkoutTupleList = mWorkoutSetDao.getSetAggregateWorkoutById(workoutID);
                    SetAggregateTuple setAggregateTuple = (aggWorkoutTupleList.size() > 0) ? aggWorkoutTupleList.get(0) : null;
                    List<ObjectAggregate> existingWorkoutAgg = mAggregateDao.getAggregateByUserTypeId(sUserID,Constants.OBJECT_TYPE_WORKOUT,workoutID);
                    if (setAggregateTuple != null) {
                        if ((existingWorkoutAgg != null) && (existingWorkoutAgg.size() > 0)) {
                            ObjectAggregate workoutAggregate = existingWorkoutAgg.get(0);
                            if (setAggregateTuple.objectID == workoutID) {
                                workoutAggregate.copyFromAggregate(setAggregateTuple);
                                workoutAggregate.objectType = Constants.OBJECT_TYPE_WORKOUT;
                                workoutAggregate.objectName = Workout.class.getSimpleName();
                                workoutAggregate.objectID = workoutID;
                                workoutAggregate.userID = sUserID;
                                workoutAggregate.lastUpdated = timeMs;
                                try {
                                    mAggregateDao.update(workoutAggregate);
                                }catch (Exception ee){
                                    ee.printStackTrace();
                                }
                            }
                        }
                        else {
                            ObjectAggregate workoutAggregate = new ObjectAggregate();
                            workoutAggregate.copyFromAggregate(setAggregateTuple);
                            workoutAggregate.objectType = Constants.OBJECT_TYPE_WORKOUT;
                            workoutAggregate.objectName = Workout.class.getSimpleName();
                            workoutAggregate.objectID = workoutID;
                            workoutAggregate.userID = sUserID;
                            workoutAggregate.lastUpdated = timeMs;
                            try {
                                mAggregateDao.insert(workoutAggregate);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(LOG_TAG, "failed on workout ins agg " + workoutID + " " + sUserID);
                                try {
                                    mAggregateDao.update(workoutAggregate);
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                    Log.e(LOG_TAG, "failed on workout update 2 agg " + workoutID + " " + sUserID);
                                }
                            }
                        }
                        // aggregates for exercises
                        List<SetAggregateTuple> aggExerciseForWorkoutTupleList = mWorkoutSetDao.getSetAggregateExerciseByWorkoutId(workoutID);
                        if ((aggExerciseForWorkoutTupleList != null) && (aggExerciseForWorkoutTupleList.size() > 0)) {
                            for (SetAggregateTuple exTuple : aggExerciseForWorkoutTupleList) {
                                Exercise exercise = mExerciseDao.getExerciseById(exTuple.objectID).get(0);
                                if (exercise != null){
                                    List<SetAggregateTuple> aggExTotalsTupleList = mWorkoutSetDao.getSetAggregateExerciseByExerciseId(exercise._id);
                                    SetAggregateTuple exTotalTuple = null;
                                    if (aggExTotalsTupleList.size() > 0) {
                                        exTotalTuple = aggExTotalsTupleList.get(0);
                                    }
                                    if (exTotalTuple != null) {
                                        List<ObjectAggregate> existingExerciseAgg = mAggregateDao.getAggregateByUserTypeId(sUserID, Constants.OBJECT_TYPE_EXERCISE, exercise._id);
                                        if (existingExerciseAgg.size() > 0) {
                                            ObjectAggregate exerciseAggregate = existingExerciseAgg.get(0);
                                            exerciseAggregate.copyFromAggregate(exTotalTuple);
                                            exerciseAggregate.lastUpdated = timeMs;
                                            try {
                                                mAggregateDao.update(exerciseAggregate);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Log.e(LOG_TAG, "failed on ex agg update" + exTuple.objectID + " " + exercise.name + " " + sUserID);
                                            }
                                        } else {
                                            ObjectAggregate exerciseAggregate = new ObjectAggregate();
                                            exerciseAggregate.copyFromAggregate(exTotalTuple);
                                            exerciseAggregate.objectType = Constants.OBJECT_TYPE_EXERCISE;
                                            exerciseAggregate.objectName = exercise.name;
                                            exerciseAggregate.objectID = exercise._id;
                                            exerciseAggregate.userID = sUserID;
                                            exerciseAggregate.lastUpdated = timeMs;
                                            try {
                                                mAggregateDao.insert(exerciseAggregate);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Log.e(LOG_TAG, "failed on ex insert agg " + exercise._id + " " + exercise.name + " " + sUserID);
                                                try {
                                                    mAggregateDao.update(exerciseAggregate);
                                                } catch (Exception e2) {
                                                    e2.printStackTrace();
                                                    Log.e(LOG_TAG, "failed on ex agg update" + exTuple.objectID + " " + exercise.name + " " + sUserID);
                                                }
                                            }
                                        }
                                    }
                                    WorkoutSet foundSet = null;
                                    for (WorkoutSet findSet : sl) {
                                        if ((findSet.exerciseID != null) && findSet.exerciseID == exTuple.objectID) {
                                            foundSet = findSet;
                                            lastReps = (findSet.repCount != null) ? findSet.repCount : 0;
                                        }
                                    }
                                    exercise.sessionCount = exTuple.countSessions;
                                    exercise.lastTrained = (foundSet == null) ? exTuple.minStart : foundSet.start;
                                    if ((exercise.first_BPID != null) && (exercise.first_BPID > 0)) {
                                        List<Bodypart> bpList1 = mBodypartDao.getBodypartById(exercise.first_BPID);
                                        if ((bpList1 != null) && (bpList1.size() == 1)) {
                                            Bodypart bp1 = bpList1.get(0);
                                            bp1.lastTrained = exercise.lastTrained;
                                            bp1.lastUpdated = timeMs;
                                            mBodypartDao.update(bp1);
                                        }
                                    }
                                    if ((exercise.second_BPID != null) && (exercise.second_BPID > 0)) {
                                        List<Bodypart> bpList2 = mBodypartDao.getBodypartById(exercise.second_BPID);
                                        if ((bpList2 != null) && (bpList2.size() == 1)) {
                                            Bodypart bp2 = bpList2.get(0);
                                            bp2.lastTrainedBP2 = exercise.lastTrained;
                                            bp2.lastUpdated = timeMs;
                                            mBodypartDao.update(bp2);
                                        }
                                    }
                                    if ((exercise.third_BPID != null) && (exercise.third_BPID > 0)) {
                                        List<Bodypart> bpList3 = mBodypartDao.getBodypartById(exercise.third_BPID);
                                        if ((bpList3 != null) && (bpList3.size() == 1)) {
                                            Bodypart bp3 = bpList3.get(0);
                                            bp3.lastTrainedBP3 = exercise.lastTrained;
                                            bp3.lastUpdated = timeMs;
                                            mBodypartDao.update(bp3);
                                        }
                                    }
                                    exercise.lastAvgWeight = exTuple.avgWeight;
                                    exercise.lastAvgWatts = exTuple.avgWatts;
                                    exercise.totalReps = Math.toIntExact(exTuple.totalReps);
                                    if (exercise.lastSets != exTuple.countSets)
                                        exercise.lastSets = Math.toIntExact(exTuple.countSets);
                                    if ((exercise.lastReps != lastReps) && (lastReps > 0))
                                        exercise.lastReps = lastReps;

                                    if (exTotalTuple != null && exTotalTuple.maxWeight != null && exercise.maxWeight < exTotalTuple.maxWeight) {
                                        exercise.maxWeight = exTotalTuple.maxWeight;
                                        WorkoutSet foundSet2 = null;
                                        for (WorkoutSet findSet : sl) {
                                            if ((findSet.exerciseID != null) && findSet.exerciseID == exTuple.objectID
                                                    && findSet.weightTotal == exTotalTuple.maxWeight) {
                                                exercise.lastMaxWeight = (findSet.repCount != null) ? findSet.repCount : lastReps;
                                                exercise.lastMaxWeight = findSet.start;
                                            }
                                        }
                                    }
                                    if ((exercise.minWeight > exTotalTuple.minWeight) || (exercise.minWeight == 0)) {
                                        exercise.minWeight = exTotalTuple.minWeight;
                                    }

                                    exercise.avgWatts = exTotalTuple.avgWatts;
                                    if ((exercise.totalWatts < exTotalTuple.totalWatts) || (exercise.totalWatts == 0)) {
                                        exercise.totalWatts = exTotalTuple.totalWatts;
                                        exercise.lastTotalWatts = exercise.lastTrained;
                                    }
                                    if (exercise.maxReps < exTotalTuple.maxReps)
                                        exercise.maxReps = Math.toIntExact(exTotalTuple.maxReps);
                                    if ((exercise.minReps > exTotalTuple.minReps) || (exercise.minReps == 0))
                                        exercise.minReps = Math.toIntExact(exTotalTuple.minReps);
                                    exercise.lastUpdated = timeMs;
                                    mExerciseDao.update(exercise);
                                }
                            }
                        }
                        List<SetAggregateTuple> aggBodypartForWorkoutTupleList = mWorkoutSetDao.getSetAggregateBodypartByWorkoutId(workoutID);
                        if ((aggBodypartForWorkoutTupleList != null) && (aggBodypartForWorkoutTupleList.size() > 0)) {
                            for (SetAggregateTuple tupleWorkoutBodypart : aggBodypartForWorkoutTupleList) {
                                Bodypart bodypart = mBodypartDao.getBodypartById(tupleWorkoutBodypart.objectID).get(0);
                                if (bodypart != null) {
                                    Bodypart parent = new Bodypart();
                                    if ((bodypart.parentID != null) && (bodypart.parentID > 0))
                                        parent = mBodypartDao.getBodypartById(bodypart.parentID).get(0);
                                    else
                                        parent = null;
                                    List<SetAggregateTuple> aggBPTotalsTupleList = mWorkoutSetDao.getSetAggregateBodypartByBodypartId(tupleWorkoutBodypart.objectID);
                                    SetAggregateTuple bpTotalTuple = null;
                                    if (aggBPTotalsTupleList.size() > 0) {
                                        bpTotalTuple = aggBPTotalsTupleList.get(0);
                                    }
                                    if (bpTotalTuple != null) {
                                        List<ObjectAggregate> existingBodypartAgg = mAggregateDao.getAggregateByUserTypeId(sUserID, Constants.OBJECT_TYPE_BODYPART, tupleWorkoutBodypart.objectID);
                                        if (existingBodypartAgg.size() > 0) {
                                            ObjectAggregate bodypartAggregate = existingBodypartAgg.get(0);
                                            bodypartAggregate.copyFromAggregate(bpTotalTuple);
                                            bodypartAggregate.lastUpdated = timeMs;
                                            try {
                                                mAggregateDao.update(bodypartAggregate);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Log.e(LOG_TAG, "failed on agg update bp " + tupleWorkoutBodypart.objectID + " " + bodypart.shortName + " " + sUserID);
                                            }
                                        } else {
                                            ObjectAggregate bodypartAggregate = new ObjectAggregate();
                                            bodypartAggregate.copyFromAggregate(bpTotalTuple);
                                            bodypartAggregate.objectType = Constants.OBJECT_TYPE_BODYPART;
                                            bodypartAggregate.objectName = bodypart.shortName;
                                            bodypartAggregate.objectID = tupleWorkoutBodypart.objectID;
                                            bodypartAggregate.userID = sUserID;
                                            bodypartAggregate.lastUpdated = timeMs;
                                            try {
                                                mAggregateDao.insert(bodypartAggregate);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Log.e(LOG_TAG, "failed on agg insert bp " + bodypart._id + " " + bodypart.shortName + " " + sUserID);
                                                try {
                                                    mAggregateDao.update(bodypartAggregate);
                                                } catch (Exception e2) {
                                                    e2.printStackTrace();
                                                    Log.e(LOG_TAG, "failed on agg update bp " + tupleWorkoutBodypart.objectID + " " + bodypart.shortName + " " + sUserID);
                                                }
                                            }
                                        }
                                    }

                                    WorkoutSet foundSet = null;
                                    for (WorkoutSet findSet : sl) {
                                        if (findSet.bodypartID == tupleWorkoutBodypart.objectID)
                                            foundSet = findSet;
                                    }
                                    bodypart.lastTrained = (foundSet != null) ? foundSet.start : w.start;
                                    bodypart.lastReps = (foundSet != null) ? (foundSet.repCount != null) ? foundSet.repCount : 0 : Math.toIntExact(tupleWorkoutBodypart.maxReps);
                                    bodypart.lastSets = Math.toIntExact(tupleWorkoutBodypart.countSets);
                                    bodypart.lastWeight = (foundSet != null) ? (foundSet.weightTotal != null) ? foundSet.weightTotal : 0 : tupleWorkoutBodypart.maxWeight;
                                    if (bpTotalTuple != null) {
                                        bodypart.sessionCount = bpTotalTuple.countSessions;
                                        bodypart.avgWeight = bpTotalTuple.avgWeight;
                                        bodypart.avgTotalWatts = bpTotalTuple.avgWatts;
                                        bodypart.avgReps = bpTotalTuple.avgReps;
                                    }
                                    if (tupleWorkoutBodypart.maxWatts > bodypart.maxWatts) {
                                        bodypart.maxWatts = tupleWorkoutBodypart.maxWatts;
                                        bodypart.lastMaxWatts = bodypart.lastTrained;
                                    }
                                    bodypart.lastAvgWatts = tupleWorkoutBodypart.avgWatts;
                                    if (bpTotalTuple != null && bpTotalTuple.maxReps != null && bodypart.repMax < bpTotalTuple.maxReps)
                                        bodypart.repMax = Math.toIntExact(bpTotalTuple.maxReps);
                                    if (bpTotalTuple != null && bpTotalTuple.minReps != null && (bodypart.repMin > bpTotalTuple.minReps) || (bodypart.repMin == 0))
                                        bodypart.repMin = Math.toIntExact(bpTotalTuple.minReps);
                                    if (bpTotalTuple != null && bpTotalTuple.maxWeight != null && bodypart.maxWeight < bpTotalTuple.maxWeight) {
                                        bodypart.maxWeight = bpTotalTuple.maxWeight;
                                        bodypart.lastMaxWeight = bodypart.lastTrained;
                                    }
                                    bodypart.lastUpdated = timeMs;
                                    mBodypartDao.update(bodypart);
                                    if ((parent != null) && (parent._id > 0)) {
                                        List<SetAggregateTuple> aggBPParentTotalsTupleList = mWorkoutSetDao.getSetAggregateBodypartByParentId(parent._id);
                                        SetAggregateTuple parentTuple = (aggBPParentTotalsTupleList.size() > 0) ? aggBPParentTotalsTupleList.get(0) : null;
                                        if (parentTuple != null) {
                                            List<ObjectAggregate> existingBodypartAgg = mAggregateDao.getAggregateByUserTypeId(sUserID, Constants.OBJECT_TYPE_BODYPART, parent._id);
                                            if (existingBodypartAgg.size() > 0) {
                                                ObjectAggregate bodypartAggregate = existingBodypartAgg.get(0);
                                                bodypartAggregate.countSessions = parentTuple.countSessions;
                                                bodypartAggregate.countSets = parentTuple.countSets;
                                                bodypartAggregate.minStart = parentTuple.minStart;
                                                bodypartAggregate.maxEnd = parentTuple.maxEnd;
                                                bodypartAggregate.maxReps = parentTuple.maxReps;
                                                bodypartAggregate.minReps = parentTuple.minReps;
                                                bodypartAggregate.avgReps = parentTuple.avgReps;
                                                bodypartAggregate.totalReps = parentTuple.totalReps;
                                                bodypartAggregate.maxWeight = parentTuple.maxWeight;
                                                bodypartAggregate.avgWeight = parentTuple.avgWeight;
                                                bodypartAggregate.minWeight = parentTuple.minWeight;
                                                bodypartAggregate.maxWatts = parentTuple.maxWatts;
                                                bodypartAggregate.avgWatts = parentTuple.avgWatts;
                                                bodypartAggregate.totalWatts = parentTuple.totalWatts;
                                                bodypartAggregate.maxDuration = parentTuple.maxDuration;
                                                bodypartAggregate.minDuration = parentTuple.minDuration;
                                                bodypartAggregate.avgDuration = parentTuple.avgDuration;
                                                bodypartAggregate.maxRestDuration = parentTuple.maxRestDuration;
                                                bodypartAggregate.minRestDuration = parentTuple.minRestDuration;
                                                bodypartAggregate.avgRestDuration = parentTuple.avgRestDuration;
                                                bodypartAggregate.maxElapsed = parentTuple.maxElapsed;
                                                bodypartAggregate.minElapsed = parentTuple.minElapsed;
                                                bodypartAggregate.avgElapsed = parentTuple.avgElapsed;
                                                bodypartAggregate.lastUpdated = timeMs;
                                                try {
                                                    mAggregateDao.update(bodypartAggregate);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    Log.e(LOG_TAG, "failed on bp agg update parent " + parent._id + " " + parent.shortName + " " + sUserID);
                                                }
                                            } else {
                                                ObjectAggregate bodypartAggregate = new ObjectAggregate();
                                                bodypartAggregate.objectType = Constants.OBJECT_TYPE_BODYPART;
                                                bodypartAggregate.objectName = parent.shortName;
                                                bodypartAggregate.objectID = parent._id;
                                                bodypartAggregate.userID = sUserID;
                                                bodypartAggregate.countSessions = parentTuple.countSessions;
                                                bodypartAggregate.countSets = parentTuple.countSets;
                                                bodypartAggregate.minStart = parentTuple.minStart;
                                                bodypartAggregate.maxEnd = parentTuple.maxEnd;
                                                bodypartAggregate.maxReps = parentTuple.maxReps;
                                                bodypartAggregate.minReps = parentTuple.minReps;
                                                bodypartAggregate.avgReps = parentTuple.avgReps;
                                                bodypartAggregate.totalReps = parentTuple.totalReps;
                                                bodypartAggregate.maxWeight = parentTuple.maxWeight;
                                                bodypartAggregate.avgWeight = parentTuple.avgWeight;
                                                bodypartAggregate.minWeight = parentTuple.minWeight;
                                                bodypartAggregate.maxWatts = parentTuple.maxWatts;
                                                bodypartAggregate.avgWatts = parentTuple.avgWatts;
                                                bodypartAggregate.totalWatts = parentTuple.totalWatts;
                                                bodypartAggregate.maxDuration = parentTuple.maxDuration;
                                                bodypartAggregate.minDuration = parentTuple.minDuration;
                                                bodypartAggregate.avgDuration = parentTuple.avgDuration;
                                                bodypartAggregate.maxRestDuration = parentTuple.maxRestDuration;
                                                bodypartAggregate.minRestDuration = parentTuple.minRestDuration;
                                                bodypartAggregate.avgRestDuration = parentTuple.avgRestDuration;
                                                bodypartAggregate.maxElapsed = parentTuple.maxElapsed;
                                                bodypartAggregate.minElapsed = parentTuple.minElapsed;
                                                bodypartAggregate.avgElapsed = parentTuple.avgElapsed;
                                                bodypartAggregate.lastUpdated = timeMs;
                                                try {
                                                    mAggregateDao.insert(bodypartAggregate);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    Log.e(LOG_TAG, "failed on bp agg insert parent " + parent._id + " " + parent.shortName + " " + sUserID);
                                                    try {
                                                        mAggregateDao.update(bodypartAggregate);
                                                    } catch (Exception e3) {
                                                        e3.printStackTrace();
                                                        Log.e(LOG_TAG, "failed on bp agg update parent " + parent._id + " " + parent.shortName + " " + sUserID);
                                                    }
                                                }
                                            }
                                            parent.sessionCount = parentTuple.countSessions;
                                            parent.lastTrained = (foundSet != null) ? foundSet.start : w.start;
                                            parent.lastReps = bodypart.lastReps;
                                            parent.lastSets = bodypart.lastSets;
                                            parent.lastWeight = bodypart.lastWeight;
                                            parent.avgWeight = parentTuple.avgWeight;
                                            parent.avgReps = parentTuple.avgReps;
                                            parent.avgTotalWatts = parentTuple.avgWatts;
                                            if (tupleWorkoutBodypart.maxWatts > parent.maxWatts) {
                                                parent.maxWatts = tupleWorkoutBodypart.maxWatts;
                                                parent.lastMaxWatts = timeMs;
                                            }
                                            parent.lastAvgWatts = parentTuple.avgWatts;
                                            if (parent.repMax < parentTuple.maxReps)
                                                parent.repMax = Math.toIntExact(parentTuple.maxReps);
                                            if ((parent.repMin > parentTuple.minReps) || (parent.repMin == 0))
                                                parent.repMin = Math.toIntExact(parentTuple.minReps);
                                            if (parent.maxWeight < parentTuple.maxWeight) {
                                                parent.maxWeight = parentTuple.maxWeight;
                                                parent.lastMaxWeight = timeMs;
                                            }
                                            parent.lastUpdated = timeMs;
                                            mBodypartDao.update(parent);
                                        }
                                    }
                                }
                            }
                        }
                        // now do the bodypart and exercise overall statistics
                    }
                }
            }
            // If there were no errors, return SUCCESS
            Data outputData = new Data.Builder()
                    .putInt(ARG_RESULT_KEY, iRetVal)
                    .putString(Constants.KEY_FIT_USER, sUserID)
                    .putString(Constants.KEY_FIT_VALUE, UserDailyTotalsWorker.class.getSimpleName())
                    .putLong(Constants.KEY_FIT_WORKOUTID, workoutID)
                    .build();
            return Result.success(outputData);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(LOG_TAG, e.getMessage());
            return Result.failure();
        }
    }
}
