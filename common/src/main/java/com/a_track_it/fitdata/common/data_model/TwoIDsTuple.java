package com.a_track_it.fitdata.common.data_model;

import androidx.room.ColumnInfo;

public class TwoIDsTuple {
    @ColumnInfo(name = "rowid")
    public Long rowID;
    @ColumnInfo(name = "itemCount")
    public Long itemCount;
}
