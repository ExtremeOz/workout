package com.a_track_it.fitdata.common.data_model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

public class DateTuple implements Parcelable {
    @ColumnInfo(name = "sync_count")
    public Long sync_count;

    @ColumnInfo(name = "mindate")
    public Long mindate;

    @ColumnInfo(name = "maxdate")
    @NonNull
    public Long maxdate;

  public DateTuple(){ sync_count = null; mindate = null; maxdate = null; }
  public DateTuple(long sync, long minDate, long maxDate){ sync_count = sync; mindate = minDate; maxdate = maxDate;}

    protected DateTuple(Parcel in) {
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

    public static final Creator<DateTuple> CREATOR = new Creator<DateTuple>() {
        @Override
        public DateTuple createFromParcel(Parcel in) {
            return new DateTuple(in);
        }

        @Override
        public DateTuple[] newArray(int size) {
            return new DateTuple[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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

    @NonNull
    @Override
    public String toString() {
        return "{ sync_count=" + ((sync_count!=null) ? sync_count:"") +
                ", mindate=" + ((mindate!=null) ? mindate:"") +
                ", maxdate=" + ((maxdate!=null) ? maxdate:"") +
                "}";
    }
}
