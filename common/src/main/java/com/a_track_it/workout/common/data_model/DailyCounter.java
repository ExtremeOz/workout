package com.a_track_it.workout.common.data_model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DailyCounter implements Parcelable {
    private final static String DATE_FORMAT = "dd-MMM HH:mm:ss";
    public DailyCounter(){
        this.FirstCount = 0;
        this.LastCount = 0;
        this.LastUpdated = 0;
        this.GoalCount = 0;
        this.GoalActive = 0;

    }
    public DailyCounter(Parcel in){
        this.FirstCount = in.readLong();
        this.LastCount = in.readLong();
        this.LastUpdated = in.readLong();
        this.GoalCount = in.readLong();
        this.GoalActive = in.readLong();
    }
    public long FirstCount;
    public long LastUpdated;
    public long LastCount;
    public long GoalCount;
    public long GoalActive;

    public String toJSONString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        String sRet = "{FirstCount=" + this.FirstCount + ", LastCount=" + this.LastCount + ",";
        if (this.LastUpdated > 0) sRet += "LastUpdated=" + dateFormat.format(this.LastUpdated) + ",";
        sRet += "GoalCount=" + GoalCount + ", GoalActive=" + this.GoalCount + "}";
        return sRet;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(FirstCount);
        dest.writeLong(LastUpdated);
        dest.writeLong(LastCount);
        dest.writeLong(GoalCount);
        dest.writeLong(GoalActive);
    }
    public static final Parcelable.Creator<DailyCounter> CREATOR = new Parcelable.Creator<DailyCounter>()
    {
        public DailyCounter createFromParcel(Parcel in)
        {
            return new DailyCounter(in);
        }
        public DailyCounter[] newArray(int size)
        {
            return new DailyCounter[size];
        }
    };
}
