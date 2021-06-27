package com.a_track_it.workout.common.data_model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.a_track_it.workout.common.Constants;

import static com.a_track_it.workout.common.Constants.WORKOUT_COMPLETED;
import static com.a_track_it.workout.common.Constants.WORKOUT_INVALID;
import static com.a_track_it.workout.common.Constants.WORKOUT_LIVE;
import static com.a_track_it.workout.common.Constants.WORKOUT_PENDING;
import static com.a_track_it.workout.common.Constants.WORKOUT_TEMPLATE;
import static com.a_track_it.workout.common.Constants.WORKOUT_TYPE_ARCHERY;

@Entity(tableName = "workout_meta_table",foreignKeys = {@ForeignKey(entity = Workout.class, parentColumns = {"rowid"}, childColumns = {"workoutID"}, onDelete = ForeignKey.CASCADE)
        ,@ForeignKey(entity = FitnessActivity.class, parentColumns = {"rowid"}, childColumns = {"activityID"}, onDelete = ForeignKey.CASCADE)})
public class WorkoutMeta implements Comparable<WorkoutMeta>, Parcelable {
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    public long _id;
    @ColumnInfo(name = "userID", index = true)
    public String userID;            // ID of user
    @ColumnInfo(name = "deviceID", index = true)
    public String deviceID;            // ID of device
    @ColumnInfo(name = "workoutID", index = true)
    public Long workoutID;
    @ColumnInfo(name = "setID", index = true)
    public Long setID;
    public String description;
    public long duration;   // length of activity
    @ColumnInfo(name="start", index = true)
    @TypeConverters(DateConverter.class)
    public long start;      // activity start time
    @ColumnInfo(name="end", index = true)
    @TypeConverters(DateConverter.class)
    public long end;
    @ColumnInfo(name="activityID", index = true)
    public Long activityID;            // ID of activity
    public int stepCount;   
    public int repCount;
    public int setCount;
    public float avgBPM;
    public float minBPM;
    public float maxBPM;
    public float weightTotal;   //total possible score/arrows
    public float wattsTotal;
    public String packageName;
    public String activityName;
    public String identifier; // activity mimetype suffix
    public int shootFormatID;
    public String shootFormat;
    public int distanceID;
    public String distanceName;
    @ColumnInfo(name="last_sync", index = true)
    @TypeConverters(DateConverter.class)
    public long last_sync;
    @ColumnInfo(name="equipmentID", index = true)
    public int equipmentID;
    public String equipmentName;
    @ColumnInfo(name="targetSizeID", index = true)
    public int targetSizeID;
    public String targetSizeName;
    public int totalPossible;
    public int shotsPerEnd;
    public long rest_duration;
    public long call_duration;
    public String score_card;
    public int totalScore;
    public long pause_duration;
    public long start_steps;
    public long end_steps;
    public long goal_duration;
    public long goal_steps;
    public long move_mins;
    public float heart_pts;
    public float total_calories;
    public float distance;
    @ColumnInfo(name="device_sync", index = true)
    @TypeConverters(DateConverter.class)
    public long device_sync;
    @ColumnInfo(name="cloud_sync", index = true)
    @TypeConverters(DateConverter.class)
    public long cloud_sync;
    public String per_end_xy;

    public WorkoutMeta() {
        _id = 0L;
        userID = null;
        deviceID = null;
        workoutID = null;
        setID = null;
        description = Constants.ATRACKIT_EMPTY;
        duration = 0L;
        start = 0L;
        end = 0L;
        activityID = 0L;
        stepCount = 0;
        setCount = 0;
        repCount = 0;
        avgBPM = 0F;
        minBPM = 0F;
        maxBPM = 0F;
        weightTotal = 0F;
        wattsTotal = 0F;
        packageName = Constants.ATRACKIT_EMPTY;
        activityName = Constants.ATRACKIT_EMPTY;
        identifier = Constants.ATRACKIT_EMPTY;
        shootFormatID = 0;
        shootFormat = Constants.ATRACKIT_EMPTY;
        distanceID = 0;
        distanceName = Constants.ATRACKIT_EMPTY;
        last_sync = 0L;
        equipmentID = 0;
        equipmentName = Constants.ATRACKIT_EMPTY;
        targetSizeID = 0;
        targetSizeName = Constants.ATRACKIT_EMPTY;
        totalPossible = 0;
        shotsPerEnd = 0;
        rest_duration = 0L;
        call_duration = 0L;
        score_card = Constants.ATRACKIT_EMPTY;
        totalScore = 0;
        pause_duration = 0L;
        start_steps = 0L;
        end_steps = 0L;
        goal_duration = 0L;
        goal_steps = 0L;
        move_mins = 0L;
        heart_pts = 0F;
        total_calories = 0F;
        distance = 0F;
        device_sync = 0L;
        cloud_sync = 0L;
        per_end_xy = Constants.ATRACKIT_EMPTY;
    }

    public WorkoutMeta(Parcel in) {
        _id = in.readLong();
        userID = (String)in.readValue(String.class.getClassLoader());
        deviceID = (String)in.readValue(String.class.getClassLoader());
        workoutID = (Long)in.readValue(Long.class.getClassLoader());
        setID = (Long)in.readValue(Long.class.getClassLoader());
        description = in.readString();
        duration = in.readLong();
        start = in.readLong();
        end = in.readLong();
        activityID = in.readLong();
        stepCount = in.readInt();
        repCount = in.readInt();
        setCount = in.readInt();
        avgBPM = in.readFloat();
        minBPM = in.readFloat();
        maxBPM = in.readFloat();
        weightTotal= in.readFloat();
        wattsTotal= in.readFloat();
        packageName = in.readString();
        activityName = in.readString();
        identifier = in.readString();
        shootFormatID = in.readInt();
        shootFormat = in.readString();
        distanceID = in.readInt();
        distanceName = in.readString();
        last_sync = in.readLong();
        equipmentID = in.readInt();
        equipmentName = in.readString();
        targetSizeID = in.readInt();
        targetSizeName = in.readString();
        totalPossible = in.readInt();
        shotsPerEnd = in.readInt();
        rest_duration = in.readLong();
        call_duration = in.readLong();
        score_card = in.readString();
        totalScore = in.readInt();
        pause_duration = in.readLong();
        start_steps = in.readLong();
        end_steps = in.readLong();
        goal_duration = in.readLong();
        goal_steps = in.readLong();
        move_mins = in.readLong();
        heart_pts = in.readFloat();
        total_calories = in.readFloat();
        distance = in.readFloat();
        device_sync = in.readLong();
        cloud_sync = in.readLong();
        per_end_xy = in.readString();
    }

    public boolean overlaps(WorkoutMeta another) {
        boolean result = false;

        if ((another.start == this.start) || (another.start > start) && (another.start < end)) {
            result = true;
        }

        if ((start > another.start) && (start < another.end)) {
            result = true;
        }

        return result;
    }
    public int currentState(){
        int retState = WORKOUT_INVALID;
        if (this.start == -1) {
            retState = WORKOUT_TEMPLATE;  // template
        }else
            if ((this.start == 0) && ((this.userID != null) && (this.userID.length() > 0))){
                retState = WORKOUT_PENDING;
            }else{
                if (this.end == 0)
                    retState += WORKOUT_LIVE;
                else
                    retState += WORKOUT_COMPLETED;
            }
        return retState;
    }
    @Override
    public int compareTo(WorkoutMeta another) {
        return (int)(this._id - another._id);
    }

    public String toString() {
        String sRet;
        sRet = "{" +
                "_id =" + _id +
                ", userID =\"" + ((userID != null) ? userID : Constants.ATRACKIT_EMPTY) + "\"" +
                ", deviceID =\"" + ((deviceID != null) ? deviceID : Constants.ATRACKIT_EMPTY) + "\"" +
                ", workoutID =" + ((workoutID != null) ? workoutID : Constants.ATRACKIT_EMPTY) +
                ", setID =" + ((setID != null) ? setID : Constants.ATRACKIT_EMPTY) +
                ", description =\"" + description + '\"' +
                ", duration =" + duration +
                ", start =" + start +
                ", end =" + end +
                ", activityID =" + ((activityID != null) ? activityID : Constants.ATRACKIT_EMPTY) +
                ", stepCount =" + stepCount +
                ", repCount =" + repCount +
                ", setCount =" + setCount +
                ", avgBPM =" + avgBPM +
                ", minBPM =" + minBPM +
                ", minBPM =" + minBPM +
                ", weightTotal =" + weightTotal +
                ", wattsTotal =" + wattsTotal +
                ", packageName =\"" + packageName + '\"' +
                ", activityName =\"" + activityName + '\"' +
                ", identifier =\"" + identifier + '\"' +
                ", shootFormatID =" + shootFormatID +
                ", shootFormat =\"" + shootFormat + '\"' +
                ", regionID =" + distanceID +
                ", regionName =\"" + distanceName + '\"' +
                ", last_sync =" + last_sync +
                ", equipmentID=" + equipmentID +
                ", equipmentName=\"" + equipmentName + '\"' +
                ", targetSizeID=" + targetSizeID +
                ", targetSizeName=\"" + targetSizeName + '\"' +
                ", totalPossible=" + totalPossible +
                ", shotsPerEnd=" + shotsPerEnd +
                ", rest_duration=" + rest_duration +
                ", call_duration=" + call_duration +
                ", score_card=\"" + score_card + '\"' +
                ", totalScore=" + totalScore +
                ", pause_duration=" + pause_duration +
                ", start_steps=" + start_steps +
                ", end_steps=" + end_steps +
                ", goal_duration=" + goal_duration +
                ", steps_duration=" + goal_steps +
                ", move_mins=" + move_mins +
                ", heart_pts =" + heart_pts +
                ", total_calories=" + total_calories +
                ", distance=" + distance +
                ", device_sync=" + device_sync +
                ", cloud_sync=" + cloud_sync +
                ", per_end_xy= \"" + per_end_xy + "\"}";
        return sRet;
    }


    public boolean isValid(boolean bDatesInclusive){
        if ((activityID != null) && (activityID > 0) && ((userID != null) && (userID.length() > 0))){
            boolean isGym = false;
            switch (Math.toIntExact(activityID)){
                case (int)Constants.WORKOUT_TYPE_STRENGTH:
                case 97:    // weight-lifting
                case 22:     // CIRCUIT TRAINING
                case 113:  // crossfit
                case 114:  //HIIT
                case 115: //interval training
                case 41: //kettle bell
                    isGym = true;
                    break;
            }
            if (isGym) {
                if ((weightTotal > 0F) && (setCount > 0) && (repCount > 0)){
                    if (bDatesInclusive){
                        return ((start > 0) && (end > 0) && (start < end));  // check both dates
                    }else
                        return true;
                }else
                    return false;
            }else{
                if (activityID == WORKOUT_TYPE_ARCHERY){
                    if ((shootFormatID > 0) && (equipmentID > 0) && (distanceID > 0) && (shotsPerEnd > 0) && (targetSizeID > 0) && (setCount > 0)){
                        if (bDatesInclusive){
                            return ((start > 0) && (end > 0) && (start < end));
                        }else
                            return true;
                    }else
                        return false;
                }
                if (bDatesInclusive){
                    return ((start > 0) && (end > 0) && (start < end));
                }else
                    return true;
            }

        }else
            return false;
    }

    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeValue(userID);
        dest.writeValue(deviceID);
        dest.writeValue(workoutID);
        dest.writeValue(setID);
        dest.writeString(description);
        dest.writeLong(duration);
        dest.writeLong(start);
        dest.writeLong(end);
        dest.writeLong(activityID);
        dest.writeInt(stepCount);
        dest.writeInt(repCount);
        dest.writeInt(setCount);
        dest.writeFloat(avgBPM);
        dest.writeFloat(minBPM);
        dest.writeFloat(maxBPM);
        dest.writeFloat(weightTotal);
        dest.writeFloat(wattsTotal);
        dest.writeString(packageName);
        dest.writeString(activityName);
        dest.writeString(identifier);
        dest.writeInt(shootFormatID);
        dest.writeString(shootFormat);
        dest.writeInt(distanceID);
        dest.writeString(distanceName);
        dest.writeLong(last_sync);
        dest.writeInt(equipmentID);
        dest.writeString(equipmentName);
        dest.writeInt(targetSizeID);
        dest.writeString(targetSizeName);
        dest.writeInt(totalPossible);
        dest.writeInt(shotsPerEnd);
        dest.writeLong(rest_duration);
        dest.writeLong(call_duration);
        dest.writeString(score_card);
        dest.writeInt(totalScore);
        dest.writeLong(pause_duration);
        dest.writeLong(start_steps);
        dest.writeLong(end_steps);
        dest.writeLong(goal_duration);
        dest.writeLong(goal_steps);
        dest.writeLong(move_mins);
        dest.writeFloat(heart_pts);
        dest.writeFloat(total_calories);
        dest.writeFloat(distance);
        dest.writeLong(device_sync);
        dest.writeLong(cloud_sync);
        dest.writeString(per_end_xy);
    }

    public static final Creator<WorkoutMeta> CREATOR = new Creator<WorkoutMeta>()
    {
        public WorkoutMeta createFromParcel(Parcel in)
        {
            return new WorkoutMeta(in);
        }
        public WorkoutMeta[] newArray(int size)
        {
            return new WorkoutMeta[size];
        }
    };
}
