package com.a_track_it.fitdata.common.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class Bodypart implements Comparable<Bodypart>, Parcelable {

    public long _id;
    public String shortName;
    public String fullName;
    public int regionID;
    public String regionName;
    public long parentID;
    public String parentName;
    public float powerFactor;
    public int exerciseCount;
    public int setMin;
    public int setMax;
    public int repMin;
    public int repMax;
    public long lastTrained;
    public int lastSets;
    public int lastReps;
    public float lastWeight;   // last best
    public float maxWeight;
    public long lastMaxWeight;  // when max weight
    public Bodypart(){
        _id = 0L;
        shortName = "";
        fullName = "";
        regionID = 1;  //
        regionName = "";
        parentID = 0L;
        parentName = "";
        powerFactor = 0F;
        exerciseCount = 0;
        setMin = 1;
        setMax = 1;
        repMin = 1;
        repMax = 1;
        lastTrained = 0L;
        lastSets = 0;
        lastReps = 0;
        lastWeight = 0F;
        maxWeight = 0F;
        lastMaxWeight = 0L;
    }
    
    public Bodypart(Parcel inParcle){
        _id = inParcle.readLong();
        shortName = inParcle.readString();
        fullName = inParcle.readString();
        regionID = inParcle.readInt();
        regionName = inParcle.readString();
        parentID = inParcle.readLong();
        parentName = inParcle.readString();
        powerFactor = inParcle.readFloat();
        exerciseCount = inParcle.readInt();
        setMin = inParcle.readInt();
        setMax = inParcle.readInt();
        repMin = inParcle.readInt();
        repMax = inParcle.readInt();
        lastTrained = inParcle.readLong();
        lastWeight = inParcle.readFloat();
        lastSets = inParcle.readInt();
        lastReps = inParcle.readInt();
        maxWeight = inParcle.readFloat();
        lastMaxWeight = inParcle.readLong();
    }
    public String toString(){
        String sRet;
        sRet = "{" +
                "_id=" + Long.toString(_id) +
                ", shortName=\"" + shortName + '\"' +
                ", fullName=\"" + fullName + '\"' +
                ", regionID=" + Integer.toString(regionID) +
                ", regionName=\"" + regionName + '\"' +
                ", parentID=" + Long.toString(parentID) +
                ", parentName=\"" + parentName + '\"' +
                ", powerFactor=" + Float.toString(powerFactor) +
                ", exerciseCount=" + Integer.toString(exerciseCount) +
                ", setMin=" + Integer.toString(setMin) +
                ", setMax=" + Integer.toString(setMax) +
                ", repMin=" + Integer.toString(repMin) +
                ", repMax=" + Integer.toString(repMax) +
                ", lastTrained=" + Long.toString(lastTrained) +
                ", lastWeight=" + Float.toString(lastWeight) +
                ", lastSets=" + Integer.toString(lastSets) +
                ", lastReps=" + Integer.toString(lastReps) +
                ", maxWeight=" + Float.toString(powerFactor) +
                ", lastMaxWeight=" + Long.toString(lastMaxWeight) +
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
        dest.writeString(fullName);
        dest.writeInt(regionID);
        dest.writeString(regionName);
        dest.writeLong(parentID);
        dest.writeString(parentName);
        dest.writeFloat(powerFactor);
        dest.writeInt(exerciseCount);
        dest.writeInt(setMin);
        dest.writeInt(setMax);
        dest.writeInt(repMin);
        dest.writeInt(repMax);
        dest.writeLong(lastTrained);
        dest.writeFloat(lastWeight);
        dest.writeInt(lastSets);
        dest.writeInt(lastReps);
        dest.writeFloat(maxWeight);
        dest.writeLong(lastMaxWeight);

    }


    @Override
    public int compareTo(@NonNull Bodypart b) {
        if (_id == b._id) return 0;
        if (regionID == b.regionID)
            return 1;
        else
            return -1;
    }
    public static final Parcelable.Creator<Bodypart> CREATOR = new Parcelable.Creator<Bodypart>()
    {
        public Bodypart createFromParcel(Parcel in)
        {
            return new Bodypart(in);
        }
        public Bodypart[] newArray(int size)
        {
            return new Bodypart[size];
        }
    };
}
