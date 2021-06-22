package com.a_track_it.fitdata.common.data_model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDailyTotalsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserDailyTotals userTotals);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserDailyTotals> totals);

    @Update
    void update(UserDailyTotals userTotals);

    @Query("DELETE FROM `userdaily_table`")
    void deleteAll();

    @Query("DELETE FROM `userdaily_table` WHERE `userID` = :userId")
    void deleteByUserId(String userId);

    @Query("SELECT * FROM `userdaily_table` WHERE `rowid` >= :startTime AND `userID` = :userId ORDER BY `rowid` ASC")
    List<UserDailyTotals> getTotalsSinceStartTime(long startTime, String userId);

    @Query("SELECT * FROM `userdaily_table` WHERE `rowid` >= :startTime AND `userID` = :userId ORDER BY `rowid` DESC")
    LiveData<List<UserDailyTotals>> liveTotalsSinceStartTime(long startTime, String userId);

    @Query("SELECT * FROM `userdaily_table` WHERE `userID` = :userId AND `rowid` BETWEEN :startTime AND :endTime AND strftime('%H',datetime(`rowid`/1000,'unixepoch','localtime')) = '23'  ORDER BY `rowid` ASC")
    List<UserDailyTotals> getEODTotalsSinceStartTime(long startTime, long endTime, String userId);

    @Query("SELECT * FROM `userdaily_table` WHERE `userID` = :userId ORDER BY `rowid` ASC")
    List<UserDailyTotals> getTotalsByUserID(String userId);

    @Query("SELECT * FROM `userdaily_table` WHERE `userID` = :userId ORDER BY `rowid` DESC LIMIT 1")
    UserDailyTotals getTopUserTotalsByUserID(String userId);

    @Query("SELECT * FROM `userdaily_table` WHERE `userID` = :userId AND `rowid` = :rowId")
    List<UserDailyTotals> getUserTotalsByUserIDRowID(String userId, long rowId);

    @Query("SELECT * FROM `userdaily_table` WHERE `userID` = :userId ORDER BY `rowid` DESC LIMIT 100")
    LiveData<List<UserDailyTotals>> liveTotalsByUserID(String userId);

    @Query("SELECT * FROM `userdaily_table` WHERE `userID` = :userId AND `rowid` BETWEEN :startTime AND :endTime ORDER BY `rowid` ASC")
    List<UserDailyTotals> getTotalsByUserIDDates(String userId, long startTime, long endTime);

    @Query("SELECT ifnull(count(`rowid`),0) AS sync_count, ifnull(min(`rowid`),0) AS mindate, ifnull(max(`rowid`),0) AS maxdate FROM `userdaily_table` WHERE `rowid` BETWEEN :startTime AND :endTime AND `userID` = :sUserID")
    DateTuple getUDTCount(String sUserID, long startTime, long endTime);

    @Query("SELECT ifnull(count(`rowid`),0) AS sync_count, ifnull(min(`rowid`),0) AS mindate, ifnull(max(`rowid`),0) AS maxdate FROM `userdaily_table` WHERE `userID` = :sUserID AND `rowid` BETWEEN :startTime AND :endTime AND strftime('%H',datetime(`rowid`/1000,'unixepoch','localtime')) = '23'")
    DateTuple getUDTEODCount(String sUserID, long startTime, long endTime);

}
