package com.a_track_it.workout.common.data_model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;

import com.a_track_it.workout.common.Constants;

public class SetAggTuple implements Parcelable {
    @ColumnInfo(name = "UOY")
    public String unit_of_year;
    @Nullable
    @ColumnInfo(name = "aggDesc")
    public String desc;
    @Nullable
    @ColumnInfo(name="sessionCount")
    public Long sessionCount;
    @Nullable
    @ColumnInfo(name="bodypartCount")
    public Long bodypartCount;
    @Nullable
    @ColumnInfo(name="exerciseCount")
    public Long exerciseCount;
    @Nullable
    @ColumnInfo(name="durationSum")
    public Long durationSum;
    @Nullable
    @ColumnInfo(name="stepSum")
    public Long stepSum;
    @Nullable
    @ColumnInfo(name="setCount")
    public Long setCount;
    @Nullable
    @ColumnInfo(name="repSum")
    public Long repSum;
    @Nullable
    @ColumnInfo(name="repAvg")
    public Float repAvg;
    @Nullable
    @ColumnInfo(name="weightSum")
    public Float weightSum;
    @Nullable
    @ColumnInfo(name="weightAvg")
    public Float weightAvg;
    @ColumnInfo(name="wattsSum")
    public Float wattsSum;
    @Nullable
    @ColumnInfo(name="scoreSum")
    public Float scoreSum;
    @Nullable
    @ColumnInfo(name="startBPM")
    public Float startBPM;
    @Nullable
    @ColumnInfo(name="endBPM")
    public Float endBPM;

    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }
    public SetAggTuple(){
        unit_of_year = Constants.ATRACKIT_EMPTY;
        desc = null;
        sessionCount = null;
        bodypartCount = null;
        exerciseCount = null;
        durationSum = null;
        stepSum = null;
        setCount = null;
        repSum = null;
        repAvg = null;
        weightSum = null;
        weightAvg = null;
        wattsSum = null;
        scoreSum = null;
        startBPM = null;
        endBPM = null;
    }

    public SetAggTuple(Parcel in){
        unit_of_year = in.readString();
        desc = (String)in.readValue(String.class.getClassLoader());
        sessionCount = (Long)in.readValue(Long.class.getClassLoader());
        bodypartCount = (Long)in.readValue(Long.class.getClassLoader());
        exerciseCount = (Long)in.readValue(Long.class.getClassLoader());
        durationSum = (Long)in.readValue(Long.class.getClassLoader());
        stepSum = (Long)in.readValue(Long.class.getClassLoader());
        setCount = (Long)in.readValue(Long.class.getClassLoader());
        repSum = (Long)in.readValue(Long.class.getClassLoader());
        repAvg = (Float)in.readValue(Float.class.getClassLoader());
        weightSum = (Float)in.readValue(Long.class.getClassLoader());
        weightAvg = (Float)in.readValue(Float.class.getClassLoader());
        wattsSum = (Float)in.readValue(Long.class.getClassLoader());
        scoreSum = (Float)in.readValue(Long.class.getClassLoader());
        startBPM =  (Float)in.readValue(Float.class.getClassLoader());
        endBPM =  (Float)in.readValue(Float.class.getClassLoader());
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(unit_of_year);
        dest.writeString(desc);
        dest.writeValue(sessionCount);
        dest.writeValue(bodypartCount);
        dest.writeValue(exerciseCount);
        dest.writeValue(durationSum);
        dest.writeValue(stepSum);
        dest.writeValue(setCount);
        dest.writeValue(repSum);
        dest.writeValue(repAvg);
        dest.writeValue(weightSum);
        dest.writeValue(weightAvg);
        dest.writeValue(wattsSum);
        dest.writeValue(scoreSum);
        dest.writeValue(startBPM);
        dest.writeValue(endBPM);
    }

    public static final Creator<SetAggTuple> CREATOR = new Creator<SetAggTuple>()
    {
        public SetAggTuple createFromParcel(Parcel in)
        {
            return new SetAggTuple(in);
        }
        public SetAggTuple[] newArray(int size)
        {
            return new SetAggTuple[size];
        }
    };
}
