package com.a_track_it.fitdata.common.model;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

public class ActiveWorkoutSet implements Parcelable {
    private static ArrayList<FloatDataPoint> BPMArrayList = new ArrayList<>();
    private static ArrayList<FloatDataPoint> PowerArrayList = new ArrayList<>();
    private static ArrayList<FloatDataPoint> StepsArrayList = new ArrayList<>();
    private static ArrayList<FloatDataPoint> ActivityArrayList = new ArrayList<>();

    private static ArrayList<DataPoint> DPArrayList = new ArrayList<>();

    public WorkoutSet set;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        set.writeToParcel(dest, flags);
        if ((DPArrayList != null) && (DPArrayList.size() > 0)) {
            dest.writeString("dp:[");
            for (DataPoint dataPoint : DPArrayList) {
                dest.writeString(DataPointToString(dataPoint));
            }
            dest.writeString("]");
        }
    }

    public class FloatDataPoint {
        public long start;
        public float float_point;
        public int id;

        FloatDataPoint(long Begins, float Val, int ID){
            float_point = Val;
            start = Begins;
            id = ID;
        }
        public String toString(){
            String sRet;
            DateFormat dateFormat = getTimeInstance();
            sRet = "{" +
                    "_id=" + Long.toString(id) +
                    ", start=" + dateFormat.format(start) +
                    ", float_point=" + Float.toString(float_point) +
                    "}";
            return sRet;
        }
    }
    public String DataPointToString(DataPoint dp){
        String sRet = "{";
        DateFormat dateFormat = getTimeInstance();
            sRet+= "type=\'" + dp.getDataType().getName() + "\'";
            sRet+= ",start=" + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
            sRet+=",end=" + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS));
            for(Field field : dp.getDataType().getFields()) {
                sRet+="," + field.getName() + "=" + dp.getValue(field);
            }
        return sRet;
    }

    public ActiveWorkoutSet(){
        this.set = new WorkoutSet();
    }
    public ActiveWorkoutSet(Parcel in){
        this.set = new WorkoutSet(in);
    }

    public void addActivityPoint(long start, float confidence, int actid){
        FloatDataPoint pt = new FloatDataPoint(start, confidence, actid);
        BPMArrayList.add(pt);
    }
    public void addBPMPoint(long start, float BPM, int id){
        FloatDataPoint pt = new FloatDataPoint(start, BPM, id);
        BPMArrayList.add(pt);
    }
    public void addPowerPoint(long start, float powerVal, int id){
        FloatDataPoint pt = new FloatDataPoint(start, powerVal, id);
        PowerArrayList.add(pt);
    }
    public void addStepPoint(long start, float vacant, int steps){
        FloatDataPoint pt = new FloatDataPoint(start, vacant, steps);
        StepsArrayList.add(pt);
    }

    public void addDataPoint(DataPoint dp){
        DPArrayList.add(dp);
    }

    public int getDataPointListSize(){
        return (DPArrayList != null) ? DPArrayList.size(): 0;
    }

    public int getBPMListSize(){
        return (BPMArrayList != null) ? BPMArrayList.size(): 0;
    }

    public ArrayList<FloatDataPoint> getBPMArrayList(){
        return BPMArrayList;
    }
    public int getActivityListSize(){
        return (ActivityArrayList != null) ? ActivityArrayList.size(): 0;
    }
    public ArrayList<FloatDataPoint> getActivityArrayList(){
        return ActivityArrayList;
    }
    public ArrayList<FloatDataPoint> getStepsArrayList(){
        return StepsArrayList;
    }
    public ArrayList<FloatDataPoint> getPowerArrayList(){
        return PowerArrayList;
    }
    public ArrayList<DataPoint> getDataPointArrayList(){
        return DPArrayList;
    }

    public DataPoint getDataPoint(int index){
        return DPArrayList.get(index);
    }
    public FloatDataPoint getBPMPoint(int index){
        return BPMArrayList.get(index);
    }
    public FloatDataPoint getActivityPoint(int index){
        return ActivityArrayList.get(index);
    }

    public static final Parcelable.Creator<ActiveWorkoutSet> CREATOR = new Parcelable.Creator<ActiveWorkoutSet>()
    {
        public ActiveWorkoutSet createFromParcel(Parcel in)
        {
            return new ActiveWorkoutSet(in);
        }
        public ActiveWorkoutSet[] newArray(int size)
        {
            return new ActiveWorkoutSet[size];
        }
    };
}
