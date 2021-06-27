package com.a_track_it.workout.common.data_model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ATrackItLatLngDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ATrackItLatLng.class)
    void insert(ATrackItLatLng latlng);

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ATrackItLatLng.class)
    void insertAll(List<ATrackItLatLng> latlngs);

    @Update
    void update(ATrackItLatLng latlng);

    @Query("DELETE FROM latlng_table")
    void deleteAll();

    @Query("DELETE FROM latlng_table WHERE rowid = :id")
    void deleteById(long id);

    @Query("DELETE FROM latlng_table WHERE rowid < :id")
    void deleteLessThanId(long id);

    @Query("SELECT * from `latlng_table` WHERE shortName LIKE :name ORDER BY `latlng_table`.rowid ASC LIMIT 1000")
    List<ATrackItLatLng> getAllLatLngByName(String name);

    @Query("SELECT * from `latlng_table` WHERE rowid = :id")
    List<ATrackItLatLng> getLatLngById(long id);

    @Query("SELECT * from `latlng_table` WHERE ROUND(Lat,4) = ROUND(:latitude,4) AND ROUND(Lng,4) = ROUND(:longitude,4)")
    List<ATrackItLatLng> getLatLngByLatLng(double latitude, double longitude);

    @Query("SELECT * from `latlng_table` WHERE rowid BETWEEN :start AND :end ORDER BY `latlng_table`.rowid ASC LIMIT 1000")
    List<ATrackItLatLng> getLatLngByStartEnd(long start, long end);

    @Query("SELECT * from `latlng_table` WHERE rowid BETWEEN :start AND :end ORDER BY `latlng_table`.rowid ASC LIMIT 1000")
    LiveData<List<ATrackItLatLng>> liveLatLngByStartEnd(long start, long end);

    @Query("SELECT * from `latlng_table` ORDER BY `latlng_table`.rowid DESC LIMIT 500")
    List<ATrackItLatLng> getLatLngLast500();


    @Query("SELECT COUNT(rowid) AS rowid, MAX(rowid) AS itemCount FROM `latlng_table`  WHERE rowid BETWEEN :start AND :end")
    List<TwoIDsTuple> getDatesCount(long start, long end);

    @Query("SELECT * from `latlng_table` WHERE rowid >= :start ORDER BY `latlng_table`.rowid ASC LIMIT 1000")
    List<ATrackItLatLng> getLatLngFromStart(long start);

    @Query("SELECT IFNULL(COUNT(rowid),0) AS rowid, MAX(rowid) AS itemCount FROM `latlng_table`  WHERE rowid >= :start")
    List<TwoIDsTuple> getFromDateCount(long start);

}
