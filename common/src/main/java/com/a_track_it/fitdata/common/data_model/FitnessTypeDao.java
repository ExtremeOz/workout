package com.a_track_it.fitdata.common.data_model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FitnessTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FitnessType fitnessType);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FitnessType> fitnessTypes);

    @Update
    void update(FitnessType fitnessType);


    @Query("UPDATE `fitness_type_table` SET session_count = session_count +1, last_session = :lastUpdated WHERE rowid = :id")
    void updateById(long id, long lastUpdated);

    @Query("SELECT t.`rowid`, t.`name`, t.`session_count`, t.`last_session` from `fitness_type_table` t INNER JOIN `fitness_activity_table` fa ON fa.`type` = t.`rowid` WHERE fa.`rowid` = :activityID")
    List<FitnessType> getActivityTypeByActivityId(long activityID);

    @Query("SELECT rowid, name, session_count, last_session from `fitness_type_table` ORDER BY name ASC")
    List<FitnessType> getAllActivityTypes();

}