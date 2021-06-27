package com.a_track_it.workout.common.data_model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.a_track_it.workout.common.Constants;

@Entity(tableName = "object_agg_table", indices = {@Index(value={"userID","objectType","objectID"}, unique = true)})
public class ObjectAggregate implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    @NonNull
    public long _id;
    @ColumnInfo(name = "userID", index = true)
    @NonNull
    public String userID;
    @ColumnInfo(name = "objectType", index = true)
    @NonNull
    public int objectType;
    @ColumnInfo(name = "objectID", index = true)
    @NonNull
    public long objectID;
    @ColumnInfo(name = "objectName")
    @NonNull
    public String objectName;
    @ColumnInfo(name = "countSessions")
    public Long countSessions;
    @ColumnInfo(name = "countSets")
    public Long countSets;
    @ColumnInfo(name = "minStart")
    @NonNull
    public Long minStart;
    @ColumnInfo(name = "maxEnd")
    @NonNull
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
    @ColumnInfo(name = "lastUpdated", index = true)
    public long lastUpdated;

    public ObjectAggregate(){
        _id = 0L;
        userID = null;
        objectType = 0;
        objectID = 0L;
        objectName = Constants.ATRACKIT_EMPTY;
        countSessions = 0L;
        countSets = 0L;
        minStart = 0L;
        maxEnd = 0L;
        maxReps = 0L;
        minReps = 0L;
        avgReps = 0F;
        totalReps = 0L;
        maxWeight = 0f;
        avgWeight = 0f;
        minWeight = 0f;
        maxWatts = 0f;
        avgWatts = 0f;
        totalWatts = 0f;
        maxDuration = 0L;
        minDuration = 0L;
        avgDuration = 0L;
        maxRestDuration = 0L;
        minRestDuration = 0L;
        avgRestDuration = 0L;
        maxElapsed = 0L;
        minElapsed = 0L;
        avgElapsed = 0L;
        lastUpdated = 0L;
    }
    public ObjectAggregate(Parcel in){
        _id = in.readLong();
        userID = (String)in.readValue(String.class.getClassLoader());
        objectType = in.readInt();
        objectID = in.readLong();
        objectName = in.readString();
        countSessions = in.readLong();
        countSets = in.readLong();
        minStart = in.readLong();
        maxEnd = in.readLong();
        maxReps = in.readLong();
        minReps = in.readLong();
        avgReps = in.readFloat();
        totalReps = in.readLong();
        maxWeight = in.readFloat();
        avgWeight = in.readFloat();
        minWeight = in.readFloat();
        maxWatts = in.readFloat();
        avgWatts = in.readFloat();
        totalWatts = in.readFloat();
        maxDuration = in.readLong();
        minDuration = in.readLong();
        avgDuration = in.readLong();
        maxRestDuration = in.readLong();
        minRestDuration = in.readLong();
        avgRestDuration = in.readLong();
        maxElapsed = in.readLong();
        minElapsed = in.readLong();
        avgElapsed = in.readLong();
        lastUpdated = in.readLong();
    }
    public void copyFromAggregate(SetAggregateTuple setAgg){
        this.objectID = setAgg.objectID;
        this.objectName = setAgg.objectName;
        this.countSessions = setAgg.countSessions;
        this.countSets = setAgg.countSets;
        this.minStart = setAgg.minStart;
        this.maxEnd = setAgg.maxEnd;
        this.maxReps = setAgg.maxReps;
        this.minReps = setAgg.minReps;
        this.avgReps = setAgg.avgReps;
        this.totalReps = setAgg.totalReps;
        this.maxWeight = setAgg.maxWeight;
        this.avgWeight = setAgg.avgWeight;
        this.minWeight = setAgg.minWeight;
        this.maxWatts = setAgg.maxWatts;
        this.avgWatts = setAgg.avgWatts;
        this.totalWatts = setAgg.totalWatts;
        this.maxDuration = setAgg.maxDuration;
        this.minDuration = setAgg.minDuration;
        this.avgDuration = setAgg.avgDuration;
        this.maxRestDuration = setAgg.maxRestDuration;
        this.minRestDuration = setAgg.minRestDuration;
        this.avgRestDuration = setAgg.avgRestDuration;
        this.maxElapsed = setAgg.maxElapsed;
        this.minElapsed = setAgg.minElapsed;
        this.avgElapsed = setAgg.avgElapsed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeValue(userID);
        dest.writeInt(objectType);
        dest.writeLong(objectID);
        dest.writeString(objectName);
        dest.writeLong(countSessions);
        dest.writeLong(countSets);
        dest.writeLong(minStart);
        dest.writeLong(maxEnd);
        dest.writeLong(maxReps);
        dest.writeLong(minReps);
        dest.writeFloat(avgReps);
        dest.writeLong(totalReps);
        dest.writeFloat(maxWeight);
        dest.writeFloat(avgWeight);
        dest.writeFloat(minWeight);
        dest.writeFloat(maxWatts);
        dest.writeFloat(avgWatts);
        dest.writeFloat(totalWatts);
        dest.writeLong(maxDuration);
        dest.writeLong(minDuration);
        dest.writeLong(avgDuration);
        dest.writeLong(maxRestDuration);
        dest.writeLong(minRestDuration);
        dest.writeLong(avgRestDuration);
        dest.writeLong(maxElapsed);
        dest.writeLong(minElapsed);
        dest.writeLong(avgElapsed);
        dest.writeLong(lastUpdated);
    }
    public static final Creator<ObjectAggregate> CREATOR = new Creator<ObjectAggregate>()
    {
        public ObjectAggregate createFromParcel(Parcel in)
        {
            return new ObjectAggregate(in);
        }
        public ObjectAggregate[] newArray(int size)
        {
            return new ObjectAggregate[size];
        }
    };
}
