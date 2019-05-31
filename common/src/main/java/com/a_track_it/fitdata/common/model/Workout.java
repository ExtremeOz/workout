package com.a_track_it.fitdata.common.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.a_track_it.fitdata.common.Constants;

import java.util.Locale;

/**
 * Created by chris.black on 5/1/15.
 *
 * Model for the Workout.
 */
public class Workout implements Comparable<Workout>, Parcelable {
    public long _id;            // same as start
    public long duration;   // length of activity
    public long start;      // activity start time
    public long end;
    public int activityID;            // ID of activity
    public int stepCount;   // number of steps for activity  - added later
    public int repCount; // total of all related exercises - arrows per end
    public int setCount;   //total sets - target ends - shooting
    public float weightTotal;   //total possible score/arrows
    public float wattsTotal;
    public String packageName;
    public String activityName;
    public String identifier; // activity mimetype suffix
    public int shootFormatID;
    public String shootFormat;
    public int distanceID;
    public String distanceName;
    public int equipmentID;
    public String equipmentName;
    public int targetSizeID;
    public String targetSizeName;
    public int totalPossible;
    public int shotsPerEnd;
    public long rest_duration;
    public String score_card;
    public long last_sync;
    public int offline_recording;
    public long pause_duration;
    public long start_steps;
    public long end_steps;

    public Workout() {
        _id = 0L;
        duration = 0L;
        start = 0L;
        end = 0L;
        activityID = 0;
        stepCount = 0;
        repCount = 0;
        setCount = 0;
        weightTotal = 0F;
        wattsTotal = 0F;
        packageName = "";
        activityName = "";
        identifier = "";
        shootFormatID = 0;
        shootFormat = "";
        distanceID = 0;
        distanceName = "";
        equipmentID = 0;
        equipmentName = "";
        targetSizeID = 0;
        targetSizeName = "";
        totalPossible=0;
        shotsPerEnd=0;
        rest_duration=0L;
        score_card="";
        last_sync=0L;
        offline_recording=0;
        pause_duration=0L;
        start_steps=0L;
        end_steps=0L;
    }

    public Workout(Parcel in) {
        _id = in.readLong();
        duration = in.readLong();
        start = in.readLong();
        end = in.readLong();
        activityID = in.readInt();
        stepCount = in.readInt();
        repCount = in.readInt();
        setCount = in.readInt();
        weightTotal= in.readFloat();
        wattsTotal= in.readFloat();
        packageName = in.readString();
        activityName = in.readString();
        identifier = in.readString();
        shootFormatID = in.readInt();
        shootFormat = in.readString();
        distanceID = in.readInt();
        distanceName = in.readString();
        equipmentID = in.readInt();
        equipmentName = in.readString();
        targetSizeID = in.readInt();
        targetSizeName = in.readString();
        totalPossible = in.readInt();
        shotsPerEnd = in.readInt();
        rest_duration = in.readLong();
        score_card = in.readString();
        last_sync = in.readLong();
        offline_recording = in.readInt();
        pause_duration=in.readLong();
        start_steps=in.readLong();
        end_steps=in.readLong();
    }
    public boolean overlaps(Workout another) {
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
    public int compareTo(Workout another) {
        int result;
        Long obj1 = this.start;
        Long obj2 = another.start;

        // Summary is always first, walking is always second
        if (this.start == -1) {
            result = -1;
        } else if (another.start == -1) {
            result = 1;
        } else if (this._id == another._id) {  // these are my _ids so I know its the same
            result = 0;
        } else if (this.identifier.equals(another.identifier)) {
            result = obj1.compareTo(obj2);
        } else if(this.activityID ==Constants.WORKOUT_TYPE_TIME) {
            result = Constants.WORKOUT_TYPE_TIME;
        } else if(another.activityID ==Constants.WORKOUT_TYPE_STEPCOUNT) {
            result = 1;
        }else {
            result = obj1.compareTo(obj2);
        }
        return result;
    }

    public String toString() {
        String sRet;
        sRet = "{" +
                "_id=" + _id+
                ", duration=" + duration +
                ", start=" + start +
                ", end=" + end +
                ", activityID=" + activityID +
                ", stepCount=" + stepCount +
                ", repCount=" + repCount +
                ", setCount=" + setCount +
                ", weightTotal=" + weightTotal +
                ", wattsTotal=" + wattsTotal +
                ", packageName=\"" + packageName + '\"' +
                ", activityName=\"" + activityName + '\"' +
                ", identifier=\"" + identifier + '\"' +
                ", shootFormatID=" + shootFormatID +
                ", shootFormat=\"" + shootFormat + '\"' +
                ", distanceID=" + distanceID +
                ", distanceName=\"" + distanceName + '\"' +
                ", equipmentID=" + equipmentID +
                ", equipmentName=\"" + equipmentName + '\"' +
                ", targetSizeID=" + targetSizeID +
                ", targetSizeName=\"" + targetSizeName + '\"' +
                ", totalPossible=" + totalPossible +
                ", shotsPerEnd=" + shotsPerEnd +
                ", rest_duration=" + rest_duration +
                ", score_card=\"" + score_card + '\"' +
                ", last_sync=" + last_sync +
                ", offline_recording=" + offline_recording +
                ", pause_duration=" + pause_duration +
                ", start_steps=" + start_steps +
                ", end_steps=" + end_steps + "}";

        return sRet;
    }

    public String removeText() {
        return "Removed: " + activityName +
                " on " + Utilities.getDayString(start) +
                " for " + Utilities.getDurationBreakdown(duration);
    }

    public String shortText() {
        String result = (start > 0) ? Utilities.getPartOfDayString(start) : "";

        if (Utilities.isGymWorkout(activityID)){
            result += " " + activityName + " ";
            if (setCount > 0)
                result += "sets: " + setCount + " ";
            if (repCount == 1)
                result += repCount + " rep ";
            else
                if (repCount > 0) result += repCount + " reps ";
            if (weightTotal > 0)
                result += String.format(Locale.getDefault(),"%.1f", weightTotal) + " kg ";
            if (weightTotal > 0)
                result += String.format(Locale.getDefault(),"%.1f", wattsTotal) + " kJ ";
            if (start > 0)
                result += " on " + Utilities.getDayString(start) + " at " + Utilities.getTimeString(start);
            if (duration > 0)
                result += " for " + Utilities.getDurationBreakdown(duration);
        }else{
            result += " " + activityName + " ";
            if (Utilities.isShooting(activityID)){
                if (setCount > 0)
                    result += "ends: " + setCount + " ";
                if (repCount == 1)
                    result += repCount + " arrow per end ";
                else
                    result += repCount + " arrows per end ";
                if (shootFormat.length() > 0) result += shootFormat + " ";
                if (equipmentName.length() > 0) result += equipmentName + " ";
                if (targetSizeName.length() > 0) result += "target " + targetSizeName + " ";
                if (distanceName.length() > 0) result += " at " + distanceName + " ";
                if (score_card.length() > 0) result += " " + score_card + " ";
                if (start > 0)
                    result += " on " + Utilities.getDayString(start) + " at " + Utilities.getTimeString(start);
                if (duration > 0) result += " for " + Utilities.getDurationBreakdown(duration);
            }else{
                if (start > 0)
                    result += " on " + Utilities.getDayString(start) + " at " + Utilities.getTimeString(start);
                if (duration > 0) result += " for " + Utilities.getDurationBreakdown(duration);
            }
        }
        if (stepCount > 0)
            result += " taking " + stepCount + " steps ";
        return result;
    }

    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeLong(duration);
        dest.writeLong(start);
        dest.writeLong(end);
        dest.writeInt(activityID);
        dest.writeInt(stepCount);
        dest.writeInt(repCount);
        dest.writeInt(setCount);
        dest.writeFloat(weightTotal);
        dest.writeFloat(wattsTotal);
        dest.writeString(packageName);
        dest.writeString(activityName);
        dest.writeString(identifier);
        dest.writeInt(shootFormatID);
        dest.writeString(shootFormat);
        dest.writeInt(distanceID);
        dest.writeString(distanceName);
        dest.writeInt(equipmentID);
        dest.writeString(equipmentName);
        dest.writeInt(targetSizeID);
        dest.writeString(targetSizeName);
        dest.writeInt(totalPossible);
        dest.writeInt(shotsPerEnd);
        dest.writeLong(rest_duration);
        dest.writeString(score_card);
        dest.writeLong(last_sync);
        dest.writeInt(offline_recording);
        dest.writeLong(pause_duration);
        dest.writeLong(start_steps);
        dest.writeLong(end_steps);
    }

    public static final Parcelable.Creator<Workout> CREATOR = new Parcelable.Creator<Workout>()
    {
        public Workout createFromParcel(Parcel in)
        {
            return new Workout(in);
        }
        public Workout[] newArray(int size)
        {
            return new Workout[size];
        }
    };
}
