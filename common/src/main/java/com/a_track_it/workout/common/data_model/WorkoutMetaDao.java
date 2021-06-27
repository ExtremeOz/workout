package com.a_track_it.workout.common.data_model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WorkoutMetaDao {

    @Insert
    void insert(WorkoutMeta meta);

    @Update
    void update(WorkoutMeta meta);

    @Query("UPDATE workout_meta_table SET device_sync = :device_time WHERE rowid = :id")
    void updateDeviceSync(long device_time, long id);

    @Query("DELETE FROM workout_meta_table WHERE userID=:sUserID")
    void deleteAll(String sUserID);

    @Query("DELETE FROM workout_meta_table WHERE rowid = :id")
    void deleteById(long id);

    @Query("SELECT * from workout_meta_table WHERE userID=:sUserID AND deviceID=:sDeviceID AND start between :starttime and :endtime ORDER BY start ASC")
    List<WorkoutMeta> getMetaByStarts(String sUserID,String sDeviceID, long starttime, long endtime);

    @Query("SELECT * from workout_meta_table WHERE userID=:sUserID AND start between :starttime and :endtime ORDER BY start ASC")
    List<WorkoutMeta> getAllMetaByStarts(String sUserID,long starttime, long endtime);

    @Query("SELECT * from workout_meta_table WHERE rowid = :id")
    List<WorkoutMeta> getMetaById(long id);

    @Query("SELECT * from workout_meta_table WHERE rowid = :id AND `userID` = :sUserID")
    List<WorkoutMeta> getMetaByIdUserId(long id, String sUserID);

    @Query("SELECT * from workout_meta_table WHERE userID=:sUserID AND deviceID=:sDeviceID AND workoutID = :id")
    List<WorkoutMeta> getMetaByWorkoutUserDeviceId(long id, String sUserID, String sDeviceID);

    @Query("SELECT * from workout_meta_table WHERE userID=:sUserID AND workoutID = :id")
    List<WorkoutMeta> getMetaByWorkoutUserId(long id, String sUserID);

    @Query("SELECT * from workout_meta_table WHERE userID=:sUserID AND deviceID=:sDeviceID AND workoutID = :id")
    LiveData<List<WorkoutMeta>> liveMetaByWorkoutId(long id, String sUserID, String sDeviceID);

    @Query("SELECT * from workout_meta_table WHERE userID=:sUserID AND workoutID = :workoutID AND setID = :setID")
    List<WorkoutMeta> getMetaByIDs(long workoutID, long setID, String sUserID);
}