package com.a_track_it.fitdata.common.data_model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ObjectAggregateDao {
    @Insert
    void insert(ObjectAggregate agg);

    @Update
    void update(ObjectAggregate agg);

    @Delete
    void delete(ObjectAggregate agg);

    @Query("SELECT * from `object_agg_table` WHERE userID = :userID AND objectType = :objectType")
    List<ObjectAggregate> getAggregateByUserType(String userID, int objectType);

    @Query("SELECT * from `object_agg_table` WHERE userID = :userID AND objectType = :objectType AND objectID = :objectID")
    List<ObjectAggregate> getAggregateByUserTypeId(String userID, int objectType, long objectID);

    @Query("SELECT * from `object_agg_table` WHERE userID = :userID AND objectType = :objectType AND objectID = :objectID AND minStart BETWEEN :startTime AND :endTime")
    List<ObjectAggregate> getAggregateByUserTypeIdStart(String userID, int objectType, long objectID, long startTime, long endTime);

    @Query("SELECT * from `object_agg_table` WHERE userID = :userID AND objectType = :objectType AND minStart BETWEEN :startTime AND :endTime")
    List<ObjectAggregate> getAggregateByUserTypeStart(String userID, int objectType, long startTime, long endTime);

    @Query("SELECT * from `object_agg_table` WHERE userID = :userID AND lastUpdated > :lastUpdated")
    List<ObjectAggregate> getAggregateByLastUpdated(String userID, long lastUpdated);

    @Query("SELECT * from `object_agg_table` WHERE userID = :userID AND rowid > :lastID")
    List<ObjectAggregate> getAggregateByGreaterThan(String userID, long lastID);

    @Query("SELECT MAX(e.rowid) AS sync_count, COUNT(e.rowid) AS mindate, MAX(e.lastUpdated) AS maxdate FROM `object_agg_table` e WHERE (e.lastUpdated > 0)")
    DateTuple getTableCountDates();
}
