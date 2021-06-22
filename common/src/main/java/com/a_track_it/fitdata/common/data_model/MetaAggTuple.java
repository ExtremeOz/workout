package com.a_track_it.fitdata.common.data_model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;

import com.a_track_it.fitdata.common.Constants;

public class MetaAggTuple implements Parcelable {
    @ColumnInfo(name = "UOY")
    public String unit_of_year;
    @ColumnInfo(name="aggID")
    public long aggID;
    @ColumnInfo(name = "aggDesc")
    public String desc;
    @Nullable
    @ColumnInfo(name = "category")
    public String category;
    @Nullable
    @ColumnInfo(name="sessionCount")
    public Long sessionCount;
    @Nullable
    @ColumnInfo(name="durationSum")
    public Long durationSum;
    @Nullable
    @ColumnInfo(name = "durationText")
    public String durationText;
    @Nullable
    @ColumnInfo(name="stepSum")
    public Long stepSum;
    @Nullable
    @ColumnInfo(name="metaDurationSum")
    public Long metaDurationSum;
    @Nullable
    @ColumnInfo(name = "metaDurationText")
    public String metaDurationText;
    @Nullable
    @ColumnInfo(name="metaStep")
    public Long metaStep;
    @Nullable
    @ColumnInfo(name="metaAvgBPM")
    public Float metaAvgBPM;
    @Nullable
    @ColumnInfo(name="metaMinBPM")
    public Float metaMinBPM;
    @Nullable
    @ColumnInfo(name="metaMaxBPM")
    public Float metaMaxBPM;
    @Nullable
    @ColumnInfo(name="metaCount")
    public Long metaCount;
    @Nullable
    @ColumnInfo(name="metaMoveMins")
    public Long metaMoveMins;
    @Nullable
    @ColumnInfo(name="metaHeartPoints")
    public Long metaHeartPoints;
    @Nullable
    @ColumnInfo(name="metaCalories")
    public Float metaCalories;
    @Nullable
    @ColumnInfo(name="metaDistance")
    public Float metaDistance;

    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }
    public MetaAggTuple(){
        unit_of_year = Constants.ATRACKIT_EMPTY;
        aggID = 0;
        desc = Constants.ATRACKIT_EMPTY;
        category = Constants.ATRACKIT_EMPTY;
        sessionCount = null;
        durationSum = null;
        durationText = null;
        stepSum = null;
        metaDurationSum = null;
        metaDurationText = null;
        metaStep = null;
        metaAvgBPM = null;
        metaMinBPM = null;
        metaMaxBPM = null;
        metaMoveMins = null;
        metaHeartPoints = null;
        metaCalories = null;
        metaDistance = null;
    }

    public MetaAggTuple(Parcel in){
        unit_of_year = in.readString();
        aggID = in.readLong();
        desc = (String)in.readValue(String.class.getClassLoader());
        category = (String)in.readValue(String.class.getClassLoader());
        sessionCount = (Long)in.readValue(Long.class.getClassLoader());
        durationSum = (Long)in.readValue(Long.class.getClassLoader());
        durationText = (String)in.readValue(String.class.getClassLoader());
        stepSum = (Long)in.readValue(Long.class.getClassLoader());
        metaDurationSum = (Long)in.readValue(Long.class.getClassLoader());
        metaDurationText = (String)in.readValue(String.class.getClassLoader());
        metaStep = (Long)in.readValue(Long.class.getClassLoader());
        metaAvgBPM = (Float)in.readValue(Float.class.getClassLoader());
        metaMinBPM = (Float)in.readValue(Float.class.getClassLoader());
        metaMaxBPM = (Float)in.readValue(Float.class.getClassLoader());
        metaMoveMins = (Long)in.readValue(Long.class.getClassLoader());
        metaHeartPoints = (Long)in.readValue(Long.class.getClassLoader());
        metaCalories = (Float)in.readValue(Float.class.getClassLoader());
        metaDistance =(Float)in.readValue(Float.class.getClassLoader());
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(unit_of_year);
        dest.writeValue(aggID);
        dest.writeString(desc);
        dest.writeString(category);
        dest.writeValue(sessionCount);
        dest.writeValue(durationSum);
        dest.writeString(durationText);
        dest.writeValue(stepSum);
        dest.writeValue(metaDurationSum);
        dest.writeString(metaDurationText);
        dest.writeValue(metaStep);
        dest.writeValue(metaAvgBPM);
        dest.writeValue(metaMinBPM);
        dest.writeValue(metaMaxBPM);
        dest.writeValue(metaMoveMins);
        dest.writeValue(metaHeartPoints);
        dest.writeValue(metaCalories);
        dest.writeValue(metaDistance);
    }

    public static final Creator<MetaAggTuple> CREATOR = new Creator<MetaAggTuple>()
    {
        public MetaAggTuple createFromParcel(Parcel in)
        {
            return new MetaAggTuple(in);
        }
        public MetaAggTuple[] newArray(int size)
        {
            return new MetaAggTuple[size];
        }
    };
}
