package com.a_track_it.workout.common.data_model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "bodyregion_table")
public class BodyRegion implements Comparable<BodyRegion>, Parcelable {
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    public long _id;
    @ColumnInfo(name = "regionName")
    @NonNull
    public String regionName;
    @ColumnInfo(name = "imageName")
    @NonNull
    public String imageName;
    @TypeConverters(DateConverter.class)
    @ColumnInfo(name = "lastTrained")
    public long lastTrained;
    @ColumnInfo(name = "lastSets")
    public int lastSets = 0;
    @ColumnInfo(name = "lastReps")
    public int lastReps = 0;
    @ColumnInfo(name = "lastWeight")
    public float lastWeight = 0F;
    @ColumnInfo(name = "lastTotalWatts")
    public float lastTotalWatts = 0F;
    @ColumnInfo(name = "lastAvgWatts")
    public float lastAvgWatts = 0F;
    @ColumnInfo(name = "maxWeight")
    public float maxWeight = 0F;
    @ColumnInfo(name = "maxWatts")
    public float maxWatts = 0F;
    @ColumnInfo(name = "lastMaxWeight")
    public long lastMaxWeight = 0L;
    @ColumnInfo(name = "lastMaxWatts")
    public long lastMaxWatts = 0L;
    @ColumnInfo(name = "exerciseCount")
    public long exerciseCount = 0L;

    public BodyRegion(){
        _id = 0L;
        regionName = "";
        lastTrained = 0L;
    }

    public BodyRegion(Parcel inParcle){
        _id = inParcle.readLong();
        regionName = inParcle.readString();
        imageName = inParcle.readString();
        lastTrained = inParcle.readLong();
        lastSets = inParcle.readInt();
        lastReps = inParcle.readInt();
        lastWeight = inParcle.readFloat();
        lastTotalWatts = inParcle.readFloat();
        lastAvgWatts = inParcle.readFloat();
        maxWeight = inParcle.readFloat();
        maxWatts = inParcle.readFloat();
        lastMaxWeight = inParcle.readLong();
        lastMaxWatts = inParcle.readLong();
        exerciseCount = inParcle.readLong();
    }
    public String toString(){
        String sRet;
        sRet = "{" +
                "_id=" + Long.toString(_id) +
                ", regionName=\"" + regionName + '\"' +
                ", imageName=\"" + imageName + '\"' +
                ", lastTrained=" + Long.toString(lastTrained) +
                ", lastSets=" + Integer.toString(lastSets) +
                ", lastReps=" + Integer.toString(lastReps) +
                ", lastWeight=" + Float.toString(lastWeight) +
                ", lastTotalWatts=" + Float.toString(lastTotalWatts) +
                ", lastAvgWatts=" + Float.toString(lastAvgWatts) +
                ", maxWeight=" + Float.toString(maxWeight) +
                ", maxWatts=" + Float.toString(maxWatts) +
                ", lastMaxWeight=" + Long.toString(lastMaxWeight) +
                ", lastMaxWatts=" + Long.toString(lastMaxWatts) +
                ", exerciseCount=" + Long.toString(exerciseCount) +
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
        dest.writeString(regionName);
        dest.writeString(imageName);
        dest.writeLong(lastTrained);
        dest.writeInt(lastSets);
        dest.writeInt(lastReps);
        dest.writeFloat(lastWeight);
        dest.writeFloat(lastTotalWatts);
        dest.writeFloat(lastAvgWatts);
        dest.writeFloat(maxWeight);
        dest.writeFloat(maxWatts);
        dest.writeLong(lastMaxWeight);
        dest.writeLong(lastMaxWatts);
        dest.writeLong(exerciseCount);
    }


    @Override
    public int compareTo(@NonNull BodyRegion b) {
        if (_id == b._id)
            return 0;
        else
            return -1;
    }
    public static final Creator<BodyRegion> CREATOR = new Creator<BodyRegion>()
    {
        public BodyRegion createFromParcel(Parcel in)
        {
            return new BodyRegion(in);
        }
        public BodyRegion[] newArray(int size)
        {
            return new BodyRegion[size];
        }
    };
}
