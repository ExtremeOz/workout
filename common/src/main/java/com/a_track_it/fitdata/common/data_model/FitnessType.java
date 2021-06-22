package com.a_track_it.fitdata.common.data_model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 *  Fitness Type class is used for list displays of Google Fitness Data Type enumerations
 *  used to initiate a new session.
 *
 */
@Entity(tableName = "fitness_type_table")
public class FitnessType {
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    public long _id;
    @NonNull
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "session_count")
    public long session_count;
    @ColumnInfo(name = "last_session")
    public long last_session;

    public FitnessType(){
        _id = 0L;
        name = "";
        session_count = 0;
        last_session = 0;
    }
    @Override
    public String toString(){
        return  "{ _id=" + Long.toString(_id) +
                ", name=\"" + name + '\"' +
                ", session_count=" + session_count +
                ", last_session=" + last_session
                +  "}";
    }
}