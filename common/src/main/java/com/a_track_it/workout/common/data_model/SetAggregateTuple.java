package com.a_track_it.workout.common.data_model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

public class SetAggregateTuple {
    @ColumnInfo(name = "objectID")
    public Long objectID;
    @ColumnInfo(name = "objectName")
    @NonNull
    public String objectName;
    @ColumnInfo(name = "countSessions")
    public Long countSessions;
    @ColumnInfo(name = "countSets")
    public Long countSets;
    @ColumnInfo(name = "minStart")
    public Long minStart;
    @ColumnInfo(name = "maxEnd")
    public Long maxEnd;
    @ColumnInfo(name = "maxReps")
    public Long maxReps;
    @ColumnInfo(name = "minReps")
    public Long minReps;
    @ColumnInfo(name = "avgReps")
    public Float avgReps;
    @ColumnInfo(name = "totalReps")
    public Long totalReps;
    @ColumnInfo(name = "maxWeight")
    public Float maxWeight;
    @ColumnInfo(name = "avgWeight")
    public Float avgWeight;
    @ColumnInfo(name = "minWeight")
    public Float minWeight;
    @ColumnInfo(name = "maxWatts")
    public Float maxWatts;
    @ColumnInfo(name = "avgWatts")
    public Float avgWatts;
    @ColumnInfo(name = "totalWatts")
    public Float totalWatts;
    @ColumnInfo(name = "maxDuration")
    public Long maxDuration;
    @ColumnInfo(name = "minDuration")
    public Long minDuration;
    @ColumnInfo(name = "avgDuration")
    public Long avgDuration;
    @ColumnInfo(name = "maxRestDuration")
    public Long maxRestDuration;
    @ColumnInfo(name = "minRestDuration")
    public Long minRestDuration;
    @ColumnInfo(name = "avgRestDuration")
    public Long avgRestDuration;
    @ColumnInfo(name = "maxElapsed")
    public Long maxElapsed;
    @ColumnInfo(name = "minElapsed")
    public Long minElapsed;
    @ColumnInfo(name = "avgElapsed")
    public Long avgElapsed;
    SetAggregateTuple(){
        super();
    }
}
