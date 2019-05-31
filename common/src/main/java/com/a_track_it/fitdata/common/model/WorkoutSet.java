package com.a_track_it.fitdata.common.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.a_track_it.fitdata.common.Constants;

import java.util.Locale;


/**
 * Created by daniel.haywood on 4/10/18.
 *
 * Model for a Workout Set - simplistic data types for storage.
 */
public class WorkoutSet implements Comparable<WorkoutSet>, Parcelable {

    public long _id;            // same as start
    public long duration;   // length of activity
    public long pause_duration;
    public long start;      // activity start time
    public long end;
    public int activityID;            // ID of activity - archery = field
    public int exerciseID;            // exercise ID
    public String exerciseName;
    public int resistance_type;
    public int bodypartID;            // DISTANCE ID
    public String bodypartName;
    public int stepCount;   // number of steps for activity
    public int repCount;    // total of all related exercises // arrows per end
    public int setCount;    // allows saving a routine - used as the sequential set number in completed sets - set/shot end index once active.
    public float weightTotal;
    public float wattsTotal;
    public String activityName;
    public long rest_duration;
    public String score_card; // coma delimited string of per arrow scores - tennis scores
    public long last_sync;

    public WorkoutSet() {
        _id = 0L;
        duration = 0L;
        pause_duration = 0L;
        start = 0L;
        end = 0L;
        activityID = 0;
        exerciseID = 0;
        exerciseName = "";
        resistance_type = 0;
        bodypartID = 0;
        bodypartName = "";
        stepCount = 0;
        repCount = 0;
        setCount = 0;
        weightTotal = 0F;
        wattsTotal = 0F;
        activityName = "";
        rest_duration=0L;
        score_card ="";
        last_sync=0L;
    }

    public WorkoutSet(Parcel in) {
        _id = in.readLong();
        duration = in.readLong();
        pause_duration = in.readLong();
        start = in.readLong();
        end = in.readLong();
        activityID = in.readInt();
        exerciseID = in.readInt();
        exerciseName = in.readString();
        resistance_type = in.readInt();
        bodypartID = in.readInt();
        bodypartName = in.readString();
        stepCount = in.readInt();
        repCount = in.readInt();
        setCount = in.readInt();
        weightTotal= in.readFloat();
        wattsTotal= in.readFloat();
        activityName = in.readString();
        rest_duration = in.readLong();
        score_card = in.readString();
        last_sync = in.readLong();
    }

    public boolean overlaps(WorkoutSet another) {
        boolean result = false;

        if ((another.start > start) && (another.start < end)) {
            result = true;
        }

        if ((start > another.start) && (start < another.end)) {
            result = true;
        }

        return result;
    }

    @Override
    public int compareTo(WorkoutSet another) {
        int result;
        Long obj1 = this.start;
        Long obj2 = another.start;

        // Summary is always first, walking is always second
        if(this.start == -2) {
            result = -1;
        } else if(another.start == -1) {
            result = 1;
        } else if (this._id == another._id) {
            result = 0;
//        } else if (this.activityID == another.activityID) {
//            result = (this.setCount - another.setCount);  // set count provides the sort order!
        } else if(this.activityID == Constants.WORKOUT_TYPE_TIME) {
            result = Constants.WORKOUT_TYPE_TIME;
        } else if(another.activityID == Constants.WORKOUT_TYPE_STEPCOUNT) {
            result = 1;
        }else {
            result = obj1.compareTo(obj2);
        }
        return result;
    }

    @Override
    public String toString() {
        return  "{ _id=" + Long.toString(_id) +
                ", duration=" + Long.toString(duration) +
                ", pause_duration=" + Long.toString(pause_duration) +
                ", start=" + Long.toString(start) +
                ", end=" + Long.toString(end) +
                ", activityID=" + Integer.toString(activityID) +
                ", exerciseID=" + Integer.toString(exerciseID) +
                ", exerciseName=\"" + exerciseName + '\"' +
                ", resistance_type=" + Integer.toString(resistance_type) +
                ", bodypartID=" + Integer.toString(bodypartID) +
                ", bodypartName=\"" + bodypartName + '\"' +
                ", stepCount=" + Integer.toString(stepCount) +
                ", repCount=" + Integer.toString(repCount) +
                ", setCount=" + Integer.toString(setCount) +
                ", weightTotal=" + Float.toString(weightTotal) +
                ", wattsTotal=" + Float.toString(wattsTotal) +
                ", activityName=\"" + activityName + '\"' +
                ", rest_duration=" + Long.toString(rest_duration) +
                ", score_card=\"" + score_card + "\"" +
                ", last_sync=" + Long.toString(last_sync) + "}";
    }
    public String longText(){
        String s = "";

        return s;
    }
    public String removeText() {
        String s = "Removed: " + activityName;
        if (start > 0)  s += " on " + Utilities.getDayString(start);
        if (duration > 0) s += " for " + Utilities.getTimeString(duration);
        return s;
    }

    public String shortText() {
        String result;
        if (Utilities.isGymWorkout(activityID)){
            result = exerciseName + " ";
            if (setCount > 0)
                result += "set " + Integer.toString(setCount) + " ";
            if (repCount == 1)
                result += Integer.toString(repCount) + " rep ";
            else
                result += Integer.toString(repCount) + " reps ";
            if (weightTotal > 0)
                result += String.format(Locale.getDefault(),"%.1f", weightTotal) + " kg ";
            if (duration > 0) result += " for " + Utilities.getDurationBreakdown(duration);
        }else{
            if (Utilities.isShooting(activityID)){
                result = activityName;
                if (start > 0)
                    result += " on " + Utilities.getDayString(start) + " at " + Utilities.getTimeString(start);
                if (duration > 0) result += " for " + Utilities.getDurationBreakdown(duration);
            }else{
                result = activityName;
                if (start > 0)
                    result += " on " + Utilities.getDayString(start) + " at " + Utilities.getTimeString(start);
                if (duration > 0) result += " for " + Utilities.getDurationBreakdown(duration);
                if (stepCount > 0)
                    result += Integer.toString(stepCount) + " steps ";
            }
        }
        return result;
    }

    public boolean isValid(){
        if (activityID > 0){
            if (Utilities.isGymWorkout(activityID))
                return (exerciseID > 0);
            else
                return true;

        }else
            return false;
    }
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeLong(duration);
        dest.writeLong(pause_duration);
        dest.writeLong(start);
        dest.writeLong(end);
        dest.writeInt(activityID);
        dest.writeInt(exerciseID);
        dest.writeString(exerciseName);
        dest.writeInt(resistance_type);
        dest.writeInt(bodypartID);
        dest.writeString(bodypartName);
        dest.writeInt(stepCount);
        dest.writeInt(repCount);
        dest.writeInt(setCount);
        dest.writeFloat(weightTotal);
        dest.writeFloat(wattsTotal);
        dest.writeString(activityName);
        dest.writeLong(rest_duration);
        dest.writeString(score_card);
        dest.writeLong(last_sync);
    }

    public static final Parcelable.Creator<WorkoutSet> CREATOR = new Parcelable.Creator<WorkoutSet>()
    {
        public WorkoutSet createFromParcel(Parcel in)
        {
            return new WorkoutSet(in);
        }
        public WorkoutSet[] newArray(int size)
        {
            return new WorkoutSet[size];
        }
    };
}
