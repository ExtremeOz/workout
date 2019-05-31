package com.a_track_it.fitdata.common.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Exercise implements Comparable<Exercise>, Parcelable {
    public long _id;
    public String name;
    public int resistanceType;
    public String resistanceTypeName;
    public float powerFactor;
    public int bodypartCount;
    public int first_BPID;
    public String first_BPName;
    public int second_BPID;
    public String second_BPName;
    public int third_BPID;
    public String third_BPName;
    public int fourth_BPID;
    public String fourth_BPName;
    public long lastTrained;  // datetime
    public float lastWeight;
    public int lastSets;
    public int lastReps;
    public float maxWeight;
    public long lastMaxWeight;  // when max weight
    public Exercise(){
        _id = 0L;
        name = "";
        resistanceType = 0;
        resistanceTypeName="";
        powerFactor = 1F;
        bodypartCount = 0;
        first_BPID = 0;
        first_BPName = "";
        second_BPID = 0;
        second_BPName = "";
        third_BPID = 0;
        third_BPName = "";
        fourth_BPID = 0;
        fourth_BPName = "";
        lastTrained = 0L;
        lastWeight = 0F;
        lastSets = 0;
        lastReps = 0;
        maxWeight = 0F;
        lastMaxWeight = 0L;
    }

    public Exercise(Parcel in){
        _id = in.readLong();
        name = in.readString();
        resistanceType = in.readInt();
        resistanceTypeName = in.readString();
        powerFactor = in.readFloat();
        bodypartCount = in.readInt();
        first_BPID = in.readInt();
        first_BPName = in.readString();
        second_BPID = in.readInt();
        second_BPName = in.readString();
        third_BPID = in.readInt();
        third_BPName = in.readString();
        fourth_BPID = in.readInt();
        fourth_BPName = in.readString();
        lastTrained = in.readLong();
        lastWeight = in.readFloat();
        lastSets = in.readInt();
        lastReps = in.readInt();
        maxWeight = in.readFloat();
        lastMaxWeight = in.readLong();
    }

    public String toString(){
        String sRet;
        sRet = "" + name +
                " using " + Utilities.getResistanceType(resistanceType) + " " + resistanceTypeName +
                "" + ((bodypartCount > 0) ? " body parts " + Integer.toString(bodypartCount) : "") +
                " with " + resistanceTypeName+ " resistance type " +
                " body part 1 " + first_BPName + " 2 " + second_BPName + " " +
                " 3 " + third_BPName + " 4th " + fourth_BPName + " " +
                " power factor " + Float.toString(powerFactor) + " ";
        return sRet;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(name);
        dest.writeInt(resistanceType);
        dest.writeString(resistanceTypeName);
        dest.writeFloat(powerFactor);
        dest.writeInt(bodypartCount);
        dest.writeInt(first_BPID);
        dest.writeString(first_BPName);
        dest.writeInt(second_BPID);
        dest.writeString(second_BPName);
        dest.writeInt(third_BPID);
        dest.writeString(third_BPName);
        dest.writeInt(fourth_BPID);
        dest.writeString(fourth_BPName);
        dest.writeLong(lastTrained);
        dest.writeFloat(lastWeight);
        dest.writeInt(lastSets);
        dest.writeInt(lastReps);
        dest.writeFloat(maxWeight);
        dest.writeLong(lastMaxWeight);
    }

    @Override
    public int compareTo(@NonNull Exercise o) {
        if (_id == o._id) return 0;
        if (first_BPID == o.first_BPID)
            return 1;
        else
            return -1;
    }
    public static final Parcelable.Creator<Exercise> CREATOR = new Parcelable.Creator<Exercise>()
    {
        public Exercise createFromParcel(Parcel in)
        {
            return new Exercise(in);
        }
        public Exercise[] newArray(int size)
        {
            return new Exercise[size];
        }
    };
}
