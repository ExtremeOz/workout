package com.a_track_it.workout.common.data_model;


import android.os.Parcelable;
import android.os.Parcel;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.a_track_it.workout.common.Constants;

import java.text.SimpleDateFormat;
import java.util.Locale;

@Entity(tableName = "userdaily_table", indices = {@Index(value = {"rowid", "userID"}, unique = true)})
public class UserDailyTotals implements Parcelable {
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    public Long _id;            // time of entry
    @ColumnInfo(name = "userID", index = true)
    public String userID;            // ID of user
    @ColumnInfo(name="stepCount")
    public int stepCount;
    @ColumnInfo(name="activeMinutes")
    public int activeMinutes;
    @ColumnInfo(name="distanceTravelled")
    public float distanceTravelled;
    @ColumnInfo(name="caloriesExpended")
    public float caloriesExpended;
    @ColumnInfo(name="heartIntensity")
    public float heartIntensity;
    @ColumnInfo(name="heartDuration")
    public long heartDuration;
    @ColumnInfo(name="maxBPM")
    public float maxBPM;
    @ColumnInfo(name="minBPM")
    public float minBPM;
    @ColumnInfo(name="avgBPM")
    public float avgBPM;
    @ColumnInfo(name="maxSpeed")
    public float maxSpeed;
    @ColumnInfo(name="minSpeed")
    public float minSpeed;
    @ColumnInfo(name="avgSpeed")
    public float avgSpeed;
    @ColumnInfo(name="lastLatitude")
    public double lastLatitude;
    @ColumnInfo(name="lastLongitude")
    public double lastLongitude;
    @ColumnInfo(name="lastAltitude")
    public double lastAltitude;
    @ColumnInfo(name="lastSpeed")
    public float lastSpeed;
    @ColumnInfo(name="lastLocation")
    public String lastLocation;
    @ColumnInfo(name="durationVehicle")
    public long durationVehicle; // 0
    @ColumnInfo(name="durationBiking")
    public long durationBiking; //  1
    @ColumnInfo(name="durationOnFoot")
    public long durationOnFoot; //  2
    @ColumnInfo(name="durationStill")
    public long durationStill;  //3
    @ColumnInfo(name="durationUnknown")
    public long durationUnknown; // 4
    @ColumnInfo(name="durationTilting")
    public long durationTilting; // 5
    @ColumnInfo(name="durationWalking")
    public long durationWalking; // 7
    @ColumnInfo(name="durationRunning")
    public long durationRunning; // 8
    @ColumnInfo(name="lastUpdated")
    public long lastUpdated;

    public UserDailyTotals() {
        _id = 0L;
        userID = Constants.ATRACKIT_EMPTY;
        stepCount = 0;
        activeMinutes = 0;
        distanceTravelled = 0F;
        caloriesExpended = 0F;
        heartIntensity = 0F;
        heartDuration = 0;
        maxBPM = 0F;
        minBPM = 0F;
        avgBPM = 0F;
        maxSpeed = 0F;
        minSpeed = 0F;
        avgSpeed = 0F;
        lastLatitude = 0D;
        lastLongitude = 0D;
        lastSpeed = 0F;
        lastAltitude = 0D;
        lastLocation = Constants.ATRACKIT_EMPTY;
        durationVehicle=0; // 0
        durationBiking=0; //  1
        durationOnFoot=0; //  2
        durationStill=0;  //3
        durationUnknown=0; // 4
        durationTilting=0; // 5
        durationWalking=0; // 7
        durationRunning=0; // 8        
        lastUpdated = 0;
    }

    public UserDailyTotals(Parcel in){
        _id = in.readLong();
        userID = in.readString();
        stepCount = in.readInt();
        activeMinutes = in.readInt();
        distanceTravelled = in.readFloat();
        caloriesExpended = in.readFloat();
        heartIntensity = in.readFloat();
        heartDuration = in.readLong();
        maxBPM = in.readFloat();
        minBPM = in.readFloat();
        avgBPM = in.readFloat();
        maxSpeed = in.readFloat();
        minSpeed = in.readFloat();
        avgSpeed = in.readFloat();
        lastLatitude = in.readDouble();
        lastLongitude = in.readDouble();
        lastAltitude = in.readDouble();
        lastSpeed = in.readFloat();
        lastLocation = in.readString();
        durationVehicle=in.readLong(); // 0
        durationBiking=in.readLong(); //  1
        durationOnFoot=in.readLong(); //  2
        durationStill=in.readLong();  //3
        durationUnknown=in.readLong(); // 4
        durationTilting=in.readLong(); // 5
        durationWalking=in.readLong(); // 7
        durationRunning=in.readLong(); // 8           
        lastUpdated=in.readLong();
    }
    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_24Full, Locale.getDefault());
        return "{ _id=" + Long.toString(_id) +
                ", userID=\"" + userID + "\"" +
                ", stepCount=" + stepCount +
                ", activeMinutes=" + activeMinutes +
                ", distanceTravelled=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,distanceTravelled) +
                ", caloriesExpended=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,caloriesExpended) +
                ", heartIntensity=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,heartIntensity) +
                ", heartDuration=" + heartDuration +
                ", maxBPM=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,maxBPM) +
                ", minBPM=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,minBPM) +
                ", avgBPM=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,avgBPM) +
                ", maxSpeed=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,maxSpeed) +
                ", minSpeed=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,minSpeed) +
                ", avgSpeed=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,avgSpeed) +
                ", lastLatitude=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,lastLatitude) +
                ", lastLongitude=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,lastLongitude) +
                ", lastSpeed=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,lastSpeed) +
                ", lastAltitude=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,lastAltitude) +
                ", lastLocation=\"" + lastLocation + "\"" +
                ", durationVehicle=" + durationVehicle +
                ", durationBiking=" + durationBiking +
                ", durationOnFoot=" + durationOnFoot +
                ", durationStill=" + durationStill +
                ", durationUnknown=" + durationUnknown +
                ", durationTilting=" + durationTilting +
                ", durationRunning=" + durationRunning +
                ", lastUpdated=" + ((lastUpdated > 0)? dateFormat.format(lastUpdated) : "N/A") +
                "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(userID);
        dest.writeInt(stepCount);
        dest.writeInt(activeMinutes);
        dest.writeFloat(distanceTravelled);
        dest.writeFloat(caloriesExpended);
        dest.writeFloat(heartIntensity);
        dest.writeLong(heartDuration);
        dest.writeFloat(maxBPM);
        dest.writeFloat(minBPM);
        dest.writeFloat(avgBPM);
        dest.writeFloat(maxSpeed);
        dest.writeFloat(minSpeed);
        dest.writeFloat(avgSpeed);
        dest.writeDouble(lastLatitude);
        dest.writeDouble(lastLongitude);
        dest.writeDouble(lastAltitude);
        dest.writeFloat(lastSpeed);
        dest.writeString(lastLocation);
        dest.writeLong(durationVehicle); // 0
        dest.writeLong(durationBiking); //  1
        dest.writeLong(durationOnFoot); //  2
        dest.writeLong(durationStill);  //3
        dest.writeLong(durationUnknown); // 4
        dest.writeLong(durationTilting); // 5
        dest.writeLong(durationWalking); // 7
        dest.writeLong(durationRunning); // 8           
        dest.writeLong(lastUpdated);
    }
    public static final Creator<UserDailyTotals> CREATOR = new Creator<UserDailyTotals>(){
        public UserDailyTotals createFromParcel(Parcel in)
        {
            return new UserDailyTotals(in);
        }
        public UserDailyTotals[] newArray(int size)
        {
            return new UserDailyTotals[size];
        }
    };
}