package com.a_track_it.fitdata.common.data_model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WorkoutSetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WorkoutSet workoutSet);

    @Update
    void update(WorkoutSet workoutSet);

    @Query("UPDATE `workout_set_table` SET `setCount` = `setCount` + 1 WHERE `workoutID` = :workoutID AND `setCount` > :setThreshold ")
    void updatePlusSetCount(long workoutID, int setThreshold);

    @Query("UPDATE `workout_set_table` SET `setCount` = `setCount` - 1 WHERE `workoutID` = :workoutID AND `setCount` > :setThreshold ")
    void updateMinusSetCount(long workoutID, int setThreshold);

    @Query("UPDATE `workout_set_table` SET device_sync = :device_time WHERE rowid = :id")
    void updateDeviceSync(long device_time, long id);

    @Query("UPDATE `workout_set_table` SET exerciseID = :newID, resistance_type = :resist_type, bodypartID = :bpID, bodypartName = :bpName, per_end_xy = :workout_exercise WHERE exerciseID IS NULL AND exerciseName = (SELECT `exercise_table`.exerciseName FROM `exercise_table` WHERE `exercise_table`.rowid = :oldID)")
    void updatePendingMatch(long oldID, long newID, long resist_type, long bpID, String bpName, String workout_exercise);

    @Query("SELECT DISTINCT workoutID FROM `workout_set_table` WHERE exerciseID IS NULL AND exerciseName = (SELECT `exercise_table`.exerciseName FROM `exercise_table` WHERE `exercise_table`.rowid = :oldID)")
    List<Long> getWorkoutIDForPendingExercise(long oldID);

    @Query("SELECT DISTINCT rowid FROM `workout_set_table` WHERE exerciseID IS NULL AND exerciseName = (SELECT `exercise_table`.exerciseName FROM `exercise_table` WHERE `exercise_table`.rowid = :oldID)")
    List<Long> getWorkoutSetIDForPendingExercise(long oldID);

    @Query("DELETE FROM `workout_set_table` WHERE `userID` = :sUserID")
    void deleteAll(String sUserID);

    @Query("DELETE FROM `workout_set_table` WHERE rowid = :id")
    void deleteById(long id);

    @Query("DELETE FROM `workout_set_table` WHERE workoutID = :id")
    void deleteByWorkoutID(long id);

    @Query("UPDATE `workout_set_table` SET workoutID = :id WHERE workoutID = :oldID")
    void updateFromDraftByWorkoutID(long id, long oldID);

    @Query("SELECT * from `workout_set_table` WHERE `userID` = :sUserID AND `workoutID` = :workoutId ORDER BY `setCount` ASC")
    List<WorkoutSet> getWorkoutSetByWorkoutID(long workoutId, String sUserID);

    @Query("SELECT * from `workout_set_table` WHERE `userID` = :sUserID AND `workoutID` = :workoutId AND `deviceID` =:sDeviceID ORDER BY `setCount` ASC")
    List<WorkoutSet> getWorkoutSetByWorkoutDeviceID(long workoutId, String sUserID, String sDeviceID);

    @Query("SELECT * from `workout_set_table` WHERE `userID` = :sUserID AND `workoutID` = :workoutId AND `deviceID` =:sDeviceID ORDER BY `setCount` ASC")
    LiveData<List<WorkoutSet>> liveWorkoutSetByWorkoutDeviceID(long workoutId, String sUserID, String sDeviceID);

    @Query("SELECT * from `workout_set_table` WHERE `userID` = :sUserID AND `rowid` = :Id ORDER BY `setCount` ASC")
    LiveData<List<WorkoutSet>> liveWorkoutSetByID(long Id, String sUserID);

    @Query("SELECT * from `workout_set_table` WHERE `userID` = :sUserID AND `workoutID` = :workoutId ORDER BY `setCount` ASC")
    LiveData<List<WorkoutSet>> liveWorkoutSetByWorkoutID(long workoutId, String sUserID);

    @Query("SELECT * from `workout_set_table` WHERE `userID` = :sUserID AND `deviceID` = :sDeviceID AND `start` between :starttime and :endtime ORDER BY `workoutID`, `setCount` ASC")
    List<WorkoutSet> getWorkoutSetByDeviceStarts(String sUserID, String sDeviceID, long starttime, long endtime);

    @Query("SELECT * from `workout_set_table` WHERE `userID` = :sUserID AND `deviceID` = :sDeviceID AND `start` between :starttime and :endtime ORDER BY `workoutID`, `setCount` ASC")
    LiveData<List<WorkoutSet>> liveWorkoutSetByDeviceStarts(String sUserID, String sDeviceID, long starttime, long endtime);

    @Query("SELECT * from `workout_set_table` WHERE `userID` = :sUserID AND (`activityID` = :activityID) AND (`start` between :startTime and :endTime) ORDER BY `workoutID`, `setCount` ASC")
    LiveData<List<WorkoutSet>> liveActivityWorkoutSetByStarts(String sUserID, long activityID, long startTime, long endTime);

    @Query("SELECT * from `workout_set_table` WHERE `userID` = :sUserID AND `start` between :starttime and :endtime ORDER BY `workoutID`, `setCount` ASC")
    List<WorkoutSet> getAllWorkoutSetByStarts(String sUserID, long starttime, long endtime);

    @Query("SELECT * from `workout_set_table` WHERE `userID` = :sUserID AND `start` between :starttime and :endtime ORDER BY `workoutID`, `setCount` ASC")
    LiveData<List<WorkoutSet>> liveAllWorkoutSetByStarts(String sUserID, long starttime, long endtime);

    @Query("SELECT `workout_set_table`.* FROM `workout_set_table` WHERE (`workout_set_table`.userID = :sUserID AND `workout_set_table`.deviceID = :sDeviceID) AND (`workout_set_table`.`workoutID` > 2) AND (`workout_set_table`.`score_card` = 'com.a_track_it.fitdata') AND (`workout_set_table`.`activityID` > 8) AND (`workout_set_table`.`last_sync` = 0) AND (`workout_set_table`.`end` > 0) AND `workout_set_table`.start BETWEEN :startTime AND :endTime ORDER BY `workout_set_table`.workoutID ASC, `workout_set_table`.start ASC")
    List<WorkoutSet> getWorkoutSetByUnSyncByDates(String sUserID, String sDeviceID, long startTime, long endTime);

    @Query("SELECT `workout_set_table`.* FROM `workout_set_table` WHERE (`workout_set_table`.userID = :sUserID AND `workout_set_table`.deviceID = :sDeviceID) AND (`workout_set_table`.`workoutID` > 2)  AND (`workout_set_table`.`end` > 0) AND `workout_set_table`.`lastUpdated` > :startTime ORDER BY `workout_set_table`.workoutID ASC, `workout_set_table`.start ASC")
    List<WorkoutSet> getWorkoutSetByLastUpdated(String sUserID, String sDeviceID, long startTime);

    @Query("SELECT `workout_set_table`.* FROM `workout_set_table` WHERE (`workout_set_table`.userID = :sUserID AND `workout_set_table`.deviceID = :sDeviceID) AND (`workout_set_table`.`workoutID` > 2) AND (`workout_set_table`.`score_card` = 'com.a_track_it.fitdata')  AND (`workout_set_table`.`last_sync` = 0)  AND (`workout_set_table`.`activityID` > 8) AND (`workout_set_table`.`end` > 0) ORDER BY `workout_set_table`.workoutID ASC, `workout_set_table`.start ASC")
    List<WorkoutSet> getWorkoutSetByUnSync(String sUserID, String sDeviceID);

    @Query("SELECT * from `workout_set_table` WHERE rowid = :id")
    List<WorkoutSet> getWorkoutSetById(long id);

    @Query("SELECT * from `workout_set_table` WHERE exerciseID = :id AND `userID` = :sUserID  AND `start` between :starttime and :endtime ORDER BY `workoutID`, `setCount` ASC")
    List<WorkoutSet> getWorkoutSetByExerciseIdDates(long id, String sUserID, long starttime, long endtime);

    @Query("SELECT * from `workout_set_table` WHERE rowid = :id AND `userID` = :sUserID")
    List<WorkoutSet> getWorkoutSetByIdUserId(long id, String sUserID);

    @Query("SELECT ifnull(count(`workout_set_table`.`rowid`),0) AS sync_count, ifnull(min(`workout_set_table`.`start`),0) AS mindate, ifnull(max(`workout_set_table`.`end`),0) AS maxdate FROM `workout_set_table` WHERE (`workout_set_table`.`userID` = :sUserID) AND (`workout_set_table`.`deviceID` = :sDeviceID)  AND (`workout_set_table`.`workoutID` > 2) AND (`workout_set_table`.`last_sync` = 0)  AND (`workout_set_table`.`end` > 0) AND (`workout_set_table`.`activityID` > 8) AND (`workout_set_table`.`score_card` = 'com.a_track_it.fitdata')")
    DateTuple getUnSyncCount(String sUserID, String sDeviceID);

    @Query("SELECT ifnull(count(`workout_set_table`.`rowid`),0) AS sync_count, ifnull(min(`workout_set_table`.`start`),0) AS mindate, ifnull(max(`workout_set_table`.`end`),0) AS maxdate FROM `workout_set_table` WHERE `workout_set_table`.`userID` = :sUserID AND `workout_set_table`.`deviceID` = :sDeviceID  AND (`workout_set_table`.`rowid` > 0) AND (`workout_set_table`.`workoutID` > 2)  AND (`workout_set_table`.`start` > 0 AND `workout_set_table`.`end` > 0) AND (`workout_set_table`.`score_card` = 'com.a_track_it.fitdata')")
    DateTuple getWorkoutSetTableStats(String sUserID, String sDeviceID);

    @Query("SELECT ifnull(count(s.`rowid`),0) AS sync_count, ifnull(min(s.`start`),0) AS mindate, ifnull(max(s.`end`),0) AS maxdate FROM `workout_set_table` s WHERE s.`workoutID` = :workoutID")
    DateTuple getSetTupleByWorkoutID(long workoutID);

    @Query("SELECT ifnull(count(s.`rowid`),0) AS sync_count, ifnull(min(s.`start`),0) AS mindate, ifnull(max(s.`end`),0) AS maxdate FROM `workout_set_table` s WHERE s.`workoutID`= 1 AND s.`userID` = :sUserID")
    DateTuple getDraftSetTuple(String sUserID);

    @Query("SELECT ifnull(count(s.`rowid`),0) AS sync_count, ifnull(min(s.`start`),0) AS mindate, ifnull(max(s.`end`),0) AS maxdate FROM `workout_set_table` s WHERE s.`userID` = :sUserID  AND s.`deviceID` = :sDeviceID  AND s.`workoutID` = :workoutID")
    DateTuple getSetTupleByDeviceWorkoutID(String sUserID, String sDeviceID, long workoutID);

    @Query("SELECT `exerciseID` AS objectID, `exerciseName` AS objectName,COUNT(DISTINCT workoutID) AS countSessions,COUNT(`rowid`) AS countSets, MIN(`start`) AS minStart, MAX(`end`) AS maxEnd, MAX(`repCount`) AS maxReps, MIN(`repCount`) AS minReps,AVG(`repCount`) AS avgReps, SUM(`repCount`) AS totalReps, MAX(`weightTotal`) AS maxWeight, AVG(`weightTotal`) AS avgWeight, MIN(`weightTotal`) AS minWeight, " +
            "MAX(`wattsTotal`) AS maxWatts, AVG(`wattsTotal`) AS avgWatts, SUM(`wattsTotal`) AS totalWatts, MAX(`duration`) AS maxDuration, MIN(`duration`) AS minDuration, AVG(`duration`) AS avgDuration,  " +
            "MAX(`rest_duration`) AS maxRestDuration, MIN(`rest_duration`) AS minRestDuration, AVG(`rest_duration`) AS avgRestDuration, " +
            "MAX(`realElapsedEnd`-`realElapsedStart`) AS maxElapsed, MIN(`realElapsedEnd`-`realElapsedStart`) AS minElapsed, AVG(`realElapsedEnd`-`realElapsedStart`) AS avgElapsed " +
            "FROM `workout_set_table` WHERE `exerciseID` = :id " +
            "GROUP BY `objectID`, `objectName`")
    List<SetAggregateTuple> getSetAggregateExerciseByExerciseId(long id);

    @Query("SELECT bodypartID AS objectID, bodypartName AS objectName,COUNT(DISTINCT workoutID) AS countSessions,COUNT(rowid) AS countSets, MIN(s.`start`) AS minStart, MAX(s.`end`) AS maxEnd, MAX(repCount) AS maxReps, MIN(repCount) AS minReps,AVG(repCount) AS avgReps, SUM(repCount) AS totalReps, MAX(weightTotal) AS maxWeight, AVG(weightTotal) AS avgWeight, MIN(weightTotal) AS minWeight, " +
            "MAX(wattsTotal) AS maxWatts, AVG(wattsTotal) AS avgWatts, SUM(wattsTotal) AS totalWatts, MAX(duration) AS maxDuration, MIN(duration) AS minDuration, AVG(duration) AS avgDuration,  " +
            "MAX(rest_duration) AS maxRestDuration, MIN(rest_duration) AS minRestDuration, AVG(rest_duration) AS avgRestDuration, " +
            "MAX(realElapsedEnd-realElapsedStart) AS maxElapsed, MIN(realElapsedEnd-realElapsedStart) AS minElapsed, AVG(realElapsedEnd-realElapsedStart) AS avgElapsed " +
            "FROM `workout_set_table` s WHERE `bodypartID` = :id " +
            "GROUP BY objectID, objectName")
    List<SetAggregateTuple> getSetAggregateBodypartByBodypartId(long id);

    @Query("SELECT 0 AS objectID, 'parent' AS objectName,  COUNT(DISTINCT s.workoutID) AS countSessions,COUNT(s.rowid) AS countSets, MIN(s.`start`) AS minStart, MAX(s.`end`) AS maxEnd, MAX(repCount) AS maxReps, MIN(repCount) AS minReps,AVG(repCount) AS avgReps, SUM(repCount) AS totalReps, MAX(weightTotal) AS maxWeight, AVG(weightTotal) AS avgWeight, MIN(weightTotal) AS minWeight, " +
            "MAX(s.wattsTotal) AS maxWatts, AVG(s.wattsTotal) AS avgWatts, IFNULL(SUM(s.wattsTotal),0) AS totalWatts, MAX(s.duration) AS maxDuration, MIN(s.duration) AS minDuration, AVG(s.duration) AS avgDuration,  " +
            "MAX(s.rest_duration) AS maxRestDuration, MIN(s.rest_duration) AS minRestDuration, AVG(s.rest_duration) AS avgRestDuration,  " +
            "MAX(s.realElapsedEnd-s.realElapsedStart) AS maxElapsed, MIN(s.realElapsedEnd-s.realElapsedStart) AS minElapsed, AVG(s.realElapsedEnd-s.realElapsedStart) AS avgElapsed  " +
            "FROM `workout_set_table` s INNER JOIN `bodypart_table` b ON b.`rowid` = s.`bodypartID` WHERE (b.`parentID` = :id) OR (b.`rowid` = :id)")
    List<SetAggregateTuple> getSetAggregateBodypartByParentId(long id);

    @Query("SELECT `exerciseID` AS objectID, `exerciseName` AS objectName,COUNT(DISTINCT workoutID) AS countSessions,COUNT(`rowid`) AS countSets, MIN(`start`) AS minStart, MAX(`end`) AS maxEnd, MAX(`repCount`) AS maxReps, MIN(`repCount`) AS minReps,AVG(`repCount`) AS avgReps, SUM(`repCount`) AS totalReps, MAX(`weightTotal`) AS maxWeight, AVG(`weightTotal`) AS avgWeight, MIN(`weightTotal`) AS minWeight, " +
            "MAX(`wattsTotal`) AS maxWatts, AVG(`wattsTotal`) AS avgWatts, SUM(`wattsTotal`) AS totalWatts, MAX(`duration`) AS maxDuration, MIN(`duration`) AS minDuration, AVG(`duration`) AS avgDuration,  " +
            "MAX(`rest_duration`) AS maxRestDuration, MIN(`rest_duration`) AS minRestDuration, AVG(`rest_duration`) AS avgRestDuration, " +
            "MAX(`realElapsedEnd`-`realElapsedStart`) AS maxElapsed, MIN(`realElapsedEnd`-`realElapsedStart`) AS minElapsed, AVG(`realElapsedEnd`-`realElapsedStart`) AS avgElapsed " +
            "FROM `workout_set_table` WHERE `workoutID` = :id AND `exerciseID` > 0 " +
            "GROUP BY `objectID`, `objectName`")
    List<SetAggregateTuple> getSetAggregateExerciseByWorkoutId(long id);

   @Query("SELECT bodypartID AS objectID, bodypartName AS objectName,COUNT(DISTINCT workoutID) AS countSessions,COUNT(rowid) AS countSets, MIN(s.`start`) AS minStart, MAX(s.`end`) AS maxEnd, MAX(repCount) AS maxReps, MIN(repCount) AS minReps,AVG(repCount) AS avgReps, SUM(repCount) AS totalReps, MAX(weightTotal) AS maxWeight, AVG(weightTotal) AS avgWeight, MIN(weightTotal) AS minWeight, " +
            "MAX(wattsTotal) AS maxWatts, AVG(wattsTotal) AS avgWatts, SUM(wattsTotal) AS totalWatts, MAX(duration) AS maxDuration, MIN(duration) AS minDuration, AVG(duration) AS avgDuration,  " +
            "MAX(rest_duration) AS maxRestDuration, MIN(rest_duration) AS minRestDuration, AVG(rest_duration) AS avgRestDuration, " +
            "MAX(realElapsedEnd-realElapsedStart) AS maxElapsed, MIN(realElapsedEnd-realElapsedStart) AS minElapsed, AVG(realElapsedEnd-realElapsedStart) AS avgElapsed " +
            "FROM `workout_set_table` s WHERE workoutID = :id AND bodypartID > 0 " +
            "GROUP BY objectID, objectName")
    List<SetAggregateTuple> getSetAggregateBodypartByWorkoutId(long id);

    @Query("SELECT workoutID as objectID, 'com.a_track_it.fitdata.workout' AS objectName,COUNT(DISTINCT workoutID) AS countSessions,COUNT(rowid) AS countSets, MIN(`start`) AS minStart, MAX(`end`) AS maxEnd, MAX(repCount) AS maxReps, MIN(repCount) AS minReps,AVG(repCount) AS avgReps, SUM(repCount) AS totalReps, MAX(weightTotal) AS maxWeight, AVG(weightTotal) AS avgWeight, MIN(weightTotal) AS minWeight, " +
            "MAX(wattsTotal) AS maxWatts, AVG(wattsTotal) AS avgWatts, SUM(wattsTotal) AS totalWatts, MAX(duration) AS maxDuration, MIN(duration) AS minDuration, AVG(duration) AS avgDuration, " +
            "MAX(rest_duration) AS maxRestDuration, MIN(rest_duration) AS minRestDuration, AVG(rest_duration) AS avgRestDuration, " +
            "MAX(realElapsedEnd-realElapsedStart) AS maxElapsed, MIN(realElapsedEnd-realElapsedStart) AS minElapsed, AVG(realElapsedEnd-realElapsedStart) AS avgElapsed " +
            "FROM `workout_set_table` WHERE workoutID = :id AND exerciseID > 0 " +
            "GROUP BY objectID, objectName")
   List<SetAggregateTuple> getSetAggregateWorkoutById(long id);
    
    @Query("SELECT strftime('%j',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.regionName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetRegionAggTupleDOY(String sUserID, long startTime, long endTime);

    @Query("SELECT strftime('%j',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.bodypartName AS aggDesc, " +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetBodypartAggTupleDOY(String sUserID, long startTime, long endTime);

    @Query("SELECT strftime('%j',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.exerciseName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetExerciseAggTupleDOY(String sUserID, long startTime, long endTime);

    @Query("SELECT strftime('%j',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, r.resistanceName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg,IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s INNER JOIN `resistance_type_table` r ON s.resistance_type = r.rowid WHERE (s.`bodypartID` > 0) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetResistanceTypeAggTupleDOY(String sUserID, long startTime, long endTime);

    @Query("SELECT strftime('%W',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.regionName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetRegionAggTupleWOY(String sUserID, long startTime, long endTime);

    @Query("SELECT strftime('%W',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.bodypartName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetBodypartAggTupleWOY(String sUserID, long startTime, long endTime);

    @Query("SELECT strftime('%W',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.exerciseName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetExerciseAggTupleWOY(String sUserID, long startTime, long endTime);

    @Query("SELECT strftime('%W',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, r.resistanceName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s INNER JOIN `resistance_type_table` r ON s.resistance_type = r.rowid WHERE (s.`bodypartID` > 0) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetResistanceTypeAggTupleWOY(String sUserID, long startTime, long endTime);

    @Query("SELECT strftime('%m',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.regionName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetRegionAggTupleMOY(String sUserID, long startTime, long endTime);

    @Query("SELECT strftime('%m',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.bodypartName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetBodypartAggTupleMOY(String sUserID, long startTime, long endTime);

    @Query("SELECT strftime('%m',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.exerciseName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetExerciseAggTupleMOY(String sUserID, long startTime, long endTime);

    @Query("SELECT strftime('%m',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, r.resistanceName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s INNER JOIN `resistance_type_table` r ON s.resistance_type = r.rowid WHERE (s.`bodypartID` > 0) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetResistanceTypeAggTupleMOY(String sUserID, long startTime, long endTime);

    @Query("SELECT strftime('%j',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.bodypartName AS aggDesc, " +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`regionID` = :regionID) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetBodypartByRegionAggTupleDOY(String sUserID, long startTime, long endTime, long regionID);

    @Query("SELECT strftime('%j',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.exerciseName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`regionID` = :regionID) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetExerciseByRegionAggTupleDOY(String sUserID, long startTime, long endTime, long regionID);
    @Query("SELECT strftime('%W',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.bodypartName AS aggDesc, " +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`regionID` = :regionID) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetBodypartByRegionAggTupleWOY(String sUserID, long startTime, long endTime, long regionID);

    @Query("SELECT strftime('%W',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.exerciseName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`regionID` = :regionID) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetExerciseByRegionAggTupleWOY(String sUserID, long startTime, long endTime, long regionID);

    @Query("SELECT strftime('%m',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.bodypartName AS aggDesc, " +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`regionID` = :regionID) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetBodypartByRegionAggTupleMOY(String sUserID, long startTime, long endTime, long regionID);

    @Query("SELECT strftime('%m',datetime(s.start/1000,'unixepoch','localtime')) AS UOY, s.exerciseName AS aggDesc," +
            "COUNT(DISTINCT s.workoutID) AS sessionCount, COUNT(DISTINCT s.bodypartID) AS bodypartCount,COUNT(DISTINCT s.exerciseID) AS exerciseCount, " +
            "SUM(s.duration) AS durationSum,SUM(s.stepCount) AS stepSum, COUNT(s.rowid) AS setCount, IFNULL(SUM(s.repCount),0) AS repSum, IFNULL(AVG(s.repCount),0) AS repAvg," +
            "IFNULL(SUM(s.weightTotal),0) AS weightSum,IFNULL(AVG(s.weightTotal),0) AS weightAvg, IFNULL(SUM(s.wattsTotal),0) AS wattsSum, SUM(s.scoreTotal) AS scoreSum,IFNULL(AVG(s.startBPM),0) AS startBPM,IFNULL(AVG(s.endBPM),0) AS endBPM " +
            "FROM `workout_set_table` s WHERE (s.`bodypartID` > 0) AND (s.`regionID` = :regionID) AND (s.`activityID` IN (80,97,113,114,115,41)) " +
            "AND (s.`userID` = :sUserID) AND (s.`start` between :startTime and :endTime) " +
            "GROUP BY UOY,aggDesc;")
    List<SetAggTuple> getSetExerciseByRegionAggTupleMOY(String sUserID, long startTime, long endTime, long regionID);
}