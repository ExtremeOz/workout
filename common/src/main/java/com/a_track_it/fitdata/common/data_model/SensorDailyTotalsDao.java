package com.a_track_it.fitdata.common.data_model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SensorDailyTotalsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SensorDailyTotals sensorTotals);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SensorDailyTotals> totals);

    @Update
    void update(SensorDailyTotals sensorTotals);

    @Query("DELETE FROM `sensordaily_table`")
    void deleteAll();

    @Query("DELETE FROM `sensordaily_table` WHERE `userID` = :userId")
    void deleteByUserId(String userId);

    @Query("DELETE FROM `sensordaily_table` WHERE `userID` = :userId AND `rowid` <= :minRowId")
    void deleteByUserId(String userId, long minRowId);

    @Query("SELECT * from `sensordaily_table` WHERE `rowid` >= :startTime AND `userID` = :userId ORDER BY `rowid` DESC")
    LiveData<List<SensorDailyTotals>> getSensorTotalsSinceStartTime(long startTime, String userId);

    @Query("SELECT * from `sensordaily_table` WHERE `userID` = :userId ORDER BY `rowid` DESC LIMIT 1")
    LiveData<List<SensorDailyTotals>> getSensorTotalsByUserID(String userId);

    @Query("SELECT * from `sensordaily_table` WHERE `userID` = :userId ORDER BY `rowid` DESC LIMIT 1")
    SensorDailyTotals getTopSensorTotalsByUserID(String userId);

    @Query("SELECT ifnull(count(`rowid`),0) AS sync_count, ifnull(min(`rowid`),0) AS mindate, ifnull(max(`rowid`),0) AS maxdate FROM `sensordaily_table` WHERE `rowid` BETWEEN :startTime AND :endTime AND `userID` = :sUserID")
    DateTuple getSDTDateRangeCount(String sUserID, long startTime, long endTime);

    @Query("SELECT ifnull(count(`rowid`),0) AS sync_count, ifnull(min(`rowid`),0) AS mindate, ifnull(max(`rowid`),0) AS maxdate FROM `sensordaily_table` WHERE `userID` = :sUserID AND `rowid` >= :startTime")
    DateTuple getSDTCountSinceDate(String sUserID, long startTime);

}
