package com.a_track_it.fitdata.common.data_model;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import androidx.room.Update;

import java.util.List;

@Dao
public interface BodyRegionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BodyRegion bodyregion);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BodyRegion> bodyregions);


    @Update
    void update(BodyRegion bodyregion);

    @Query("SELECT * FROM bodyregion_table WHERE rowid = :id")
    BodyRegion getById(long id);

    @Query("SELECT * FROM bodyregion_table")
    List<BodyRegion> getAll();
}
