package com.a_track_it.fitdata.common.data_model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.a_track_it.fitdata.common.Constants;

@Entity(tableName = "latlng_table")
public class ATrackItLatLng implements Parcelable {
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    public long _id;
    @ColumnInfo(name = "shortName")
    public String shortName;
    @Nullable
    @ColumnInfo(name="Lat", index = true)
    public Double Lat;
    @Nullable
    @ColumnInfo(name="Lng", index = true)
    public Double Lng;
    @Nullable
    @ColumnInfo(name="Speed")
    public Float Speed;
    @Nullable
    @ColumnInfo(name="Alt")
    public Double Alt;
    @Nullable
    @ColumnInfo(name="BPM")
    public Float BPM;
    @Nullable
    @ColumnInfo(name="Steps")
    public Long Steps;

    public ATrackItLatLng(){
        _id = 0L;
        shortName = Constants.ATRACKIT_EMPTY;
        Lat = null;
        Lng = null;
        Speed = null;
        Alt = null;
        BPM = null;
        Steps = null;
    }
    public ATrackItLatLng(Double latitude, Double longitude){
        _id = System.currentTimeMillis();
        shortName = Constants.ATRACKIT_EMPTY;
        Lat = latitude;
        Lng = longitude;
        Speed = null;
        Alt = null;
        BPM = null;
        Steps = null;
    }
    public ATrackItLatLng(Parcel parcel){
        _id = parcel.readLong();
        shortName = (String)parcel.readValue(String.class.getClassLoader());
        Lat = (Double)parcel.readValue(Double.class.getClassLoader());
        Lng = (Double)parcel.readValue(Double.class.getClassLoader());
        Speed = (Float)parcel.readValue(Float.class.getClassLoader());
        Alt = (Double)parcel.readValue(Double.class.getClassLoader());
        BPM = (Float)parcel.readValue(Float.class.getClassLoader());
        Steps = (Long)parcel.readValue(Long.class.getClassLoader());
    }
    public static final Creator<ATrackItLatLng> CREATOR = new Creator<ATrackItLatLng>()
    {
        public ATrackItLatLng createFromParcel(Parcel in)
        {
            return new ATrackItLatLng(in);
        }
        public ATrackItLatLng[] newArray(int size)
        {
            return new ATrackItLatLng[size];
        }
    };
    public String toString(){
        String sRet;
        sRet = "{" +
                "_id=" + Long.toString(_id) +
                ", shortName=\"" + shortName + '\"' +
                ", Lat=" + Double.toString(Lat) +
                ", Lng=" + Double.toString(Lng) +
                ", Speed=" + Float.toString(Speed) +
                ", Alt=" + Double.toString(Alt) +
                ", BPM=" + Float.toString(BPM) +
                ", Steps=" + Long.toString(Steps) +
                "}";
        return sRet;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(shortName);
        dest.writeValue(Lat);
        dest.writeValue(Lng);
        dest.writeValue(Speed);
        dest.writeValue(Alt);
        dest.writeValue(BPM);
        dest.writeValue(Steps);
    }
}
