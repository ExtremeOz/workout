package com.a_track_it.workout.common.data_model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "resistance_type_table")
public class ResistanceType implements Comparable<ResistanceType>, Parcelable {
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    public long _id;
    public String resistanceName;
    public String imageName;

    public ResistanceType(){
        _id = 0L;
        resistanceName = "";
        imageName = "";
    }

    public ResistanceType(Parcel inParcle){
        _id = inParcle.readLong();
        resistanceName = inParcle.readString();
    }
    public String toString(){
        String sRet;
        sRet = "{" +
                "_id=" + Long.toString(_id) +
                ", resistanceName=\"" + resistanceName + '\"' +
                ", imageName=\"" + imageName + '\"' +
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
        dest.writeString(resistanceName);
        dest.writeString(imageName);
    }


    @Override
    public int compareTo(@NonNull ResistanceType b) {
        if (_id == b._id)
            return 0;
        else
            return -1;
    }
    public static final Creator<ResistanceType> CREATOR = new Creator<ResistanceType>()
    {
        public ResistanceType createFromParcel(Parcel in)
        {
            return new ResistanceType(in);
        }
        public ResistanceType[] newArray(int size)
        {
            return new ResistanceType[size];
        }
    };
}
