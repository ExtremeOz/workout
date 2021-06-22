package com.a_track_it.fitdata.common.data_model;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import androidx.room.Update;

import java.util.List;

@Dao
public interface ResistanceTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ResistanceType resistance_type);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ResistanceType> resistance_types);


    @Update
    void update(ResistanceType resistance_type);

    @Query("SELECT rowid, resistanceName, imageName FROM `resistance_type_table` WHERE rowid = :id")
    ResistanceType getById(int id);

    @Query("SELECT * FROM `resistance_type_table`")
    List<ResistanceType> getAll();
}
