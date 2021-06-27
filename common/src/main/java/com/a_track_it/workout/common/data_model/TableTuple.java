package com.a_track_it.workout.common.data_model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

import com.a_track_it.workout.common.Constants;

public class TableTuple implements Parcelable {
    @ColumnInfo(name = "table_name")
    public String table_name;

    @ColumnInfo(name = "sync_count")
    public Long sync_count;

    @ColumnInfo(name = "mindate")
    public Long mindate;

    @ColumnInfo(name = "maxdate")
    @NonNull
    public Long maxdate;
    public TableTuple(){
        table_name = Constants.ATRACKIT_EMPTY;
        sync_count = null;
        mindate = null;
        maxdate = null;
    }
    public TableTuple(String sName,long syncCount, long minDate, long maxDate){
        table_name = sName;
        sync_count = syncCount;
        mindate = minDate;
        maxdate = maxDate;
    }

    @NonNull
    public String toString() {
        String sTemp = "{table_name=\"" + table_name + "\",";
        sTemp += "sync_count=" + sync_count + ",";
        sTemp += "mindate=" + mindate + ",";
        sTemp += "maxdate=" + maxdate + "}";
        return sTemp;
    }

    protected TableTuple(Parcel in) {
        table_name = in.readString();
        if (in.readByte() == 0) {
            sync_count = null;
        } else {
            sync_count = in.readLong();
        }
        if (in.readByte() == 0) {
            mindate = null;
        } else {
            mindate = in.readLong();
        }
        if (in.readByte() == 0) {
            maxdate = null;
        } else {
            maxdate = in.readLong();
        }
    }

    public static final Creator<TableTuple> CREATOR = new Creator<TableTuple>() {
        @Override
        public TableTuple createFromParcel(Parcel in) {
            return new TableTuple(in);
        }

        @Override
        public TableTuple[] newArray(int size) {
            return new TableTuple[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(table_name);
        if (sync_count == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(sync_count);
        }
        if (mindate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(mindate);
        }
        if (maxdate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(maxdate);
        }
    }
}
