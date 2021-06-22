package com.a_track_it.fitdata.common.data_model;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.a_track_it.fitdata.common.Constants;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static androidx.room.ColumnInfo.INTEGER;

@Entity(tableName = "sensordaily_table", indices = {@Index(value = {"rowid", "userID"}, unique = true)})
public class SensorDailyTotals implements Parcelable {
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    public long _id;            // time of entry
    @ColumnInfo(name = "userID", index = true)
    public String userID;            // ID of user
    @ColumnInfo(name = "activityType")
    public int activityType;            // ID of user
    @ColumnInfo(name = "lastActivityType")
    public long lastActivityType;            // last activity type
    @ColumnInfo(name="deviceStep")
    public int deviceStep;
    @ColumnInfo(name="lastDeviceStep")
    public long lastDeviceStep;
    @ColumnInfo(name="deviceBPM")
    public float deviceBPM;
    @ColumnInfo(name="lastDeviceBPM")
    public long lastDeviceBPM;
    @ColumnInfo(name="temperature")
    public float temperature;
    @ColumnInfo(name="humidity")
    public float humidity;
    @ColumnInfo(name="pressure")
    public float pressure;
    @ColumnInfo(name="lastDeviceOther")
    public long lastDeviceOther;
    @ColumnInfo(name="fitStep")
    public int fitStep;
    @ColumnInfo(name="lastFitStep")
    public long lastFitStep;
    @ColumnInfo(name="fitBPM")
    public float fitBPM;
    @ColumnInfo(name="lastFitBPM")
    public long lastFitBPM;
    @ColumnInfo(name="fitLat")
    public float fitLat;
    @ColumnInfo(name="fitLng")
    public float fitLng;
    @ColumnInfo(name="fitLocation")
    public String fitLocation;
    @ColumnInfo(name="device2Step")
    public int device2Step;
    @ColumnInfo(name="lastDevice2Step")
    public long lastDevice2Step;
    @ColumnInfo(name="device2BPM")
    public float device2BPM;
    @ColumnInfo(name="lastDevice2BPM")
    public long lastDevice2BPM;
    @ColumnInfo(name="temperature2")
    public float temperature2;
    @ColumnInfo(name="humidity2")
    public float humidity2;
    @ColumnInfo(name="pressure2")
    public float pressure2;
    @ColumnInfo(name="lastDevice2Other")
    public long lastDevice2Other;
    @TypeConverters(DateConverter.class)
    @ColumnInfo(name = "lastUpdated", index = true, typeAffinity = INTEGER)
    public long lastUpdated;

    public SensorDailyTotals() {
        _id = 0L;
        userID = Constants.ATRACKIT_EMPTY;
        activityType = -1;
        lastActivityType = 0;
        deviceStep = 0;
        lastDeviceStep = 0;
        deviceBPM = 0;
        lastDeviceBPM = 0;
        temperature = 0;
        humidity = 0;
        pressure = 0;
        lastDeviceOther = 0;
        fitStep = 0;
        lastFitStep = 0;
        fitBPM = 0;
        lastFitBPM = 0;
        fitLat = 0;
        fitLng = 0;
        fitLocation = Constants.ATRACKIT_EMPTY;
        device2Step = 0;
        lastDevice2Step = 0;
        device2BPM = 0;
        lastDevice2BPM = 0;
        temperature2 = 0;
        humidity2 = 0;
        pressure2 = 0;
        lastDevice2Other = 0;
        lastUpdated = 0;
    }
    public SensorDailyTotals(long timeMs, String sUserID) {
        _id = timeMs;
        userID = sUserID;
        activityType = -1;
        lastActivityType = 0;
        deviceStep = 0;
        lastDeviceStep = 0;
        deviceBPM = 0;
        lastDeviceBPM = 0;
        temperature = 0;
        humidity = 0;
        pressure = 0;
        lastDeviceOther = 0;
        fitStep = 0;
        lastFitStep = 0;
        fitBPM = 0;
        lastFitBPM = 0;
        fitLat = 0;
        fitLng = 0;
        fitLocation = Constants.ATRACKIT_EMPTY;
        device2Step = 0;
        lastDevice2Step = 0;
        device2BPM = 0;
        lastDevice2BPM = 0;
        temperature2 = 0;
        humidity2 = 0;
        pressure2 = 0;
        lastDevice2Other = 0;
        lastUpdated = 0;
    }

    public SensorDailyTotals(Parcel in){
        _id = in.readLong();
        userID = in.readString();
        activityType = in.readInt();
        lastActivityType = in.readLong();
        deviceStep = in.readInt();
        lastDeviceStep = in.readLong();
        deviceBPM = in.readFloat();
        lastDeviceBPM = in.readLong();
        temperature = in.readFloat();
        humidity = in.readFloat();
        pressure = in.readFloat();
        lastDeviceOther = in.readLong();
        fitStep = in.readInt();
        lastFitStep = in.readLong();
        fitBPM = in.readFloat();
        lastFitBPM = in.readLong();
        fitLat = in.readFloat();
        fitLng = in.readFloat();
        fitLocation = in.readString();
        device2Step = in.readInt();
        lastDevice2Step = in.readLong();
        device2BPM = in.readFloat();
        lastDevice2BPM = in.readLong();
        temperature2 = in.readFloat();
        humidity2 = in.readFloat();
        pressure2 = in.readFloat();
        lastDevice2Other = in.readLong();
        lastUpdated = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(_id);
            dest.writeString(userID);
            dest.writeInt(activityType);
            dest.writeLong(lastActivityType);
            dest.writeInt(deviceStep);
            dest.writeLong(lastDeviceStep);
            dest.writeFloat(deviceBPM);
            dest.writeLong(lastDeviceBPM);
            dest.writeFloat(temperature);
            dest.writeFloat(humidity);
            dest.writeFloat(pressure);
            dest.writeLong(lastDeviceOther);
            dest.writeInt(fitStep);
            dest.writeLong(lastFitStep);
            dest.writeFloat(fitBPM);
            dest.writeLong(lastFitBPM);
            dest.writeFloat(fitLat);
            dest.writeFloat(fitLng);
            dest.writeString(fitLocation);
            dest.writeInt(device2Step);
            dest.writeLong(lastDevice2Step);
            dest.writeFloat(device2BPM);
            dest.writeLong(lastDevice2BPM);
            dest.writeFloat(temperature2);
            dest.writeFloat(humidity2);
            dest.writeFloat(pressure2);
            dest.writeLong(lastDevice2Other);
            dest.writeLong(lastUpdated);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SensorDailyTotals> CREATOR = new Creator<SensorDailyTotals>() {
        @Override
        public SensorDailyTotals createFromParcel(Parcel in) {
            return new SensorDailyTotals(in);
        }

        @Override
        public SensorDailyTotals[] newArray(int size) {
            return new SensorDailyTotals[size];
        }
    };

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_24Full, Locale.getDefault());
        return "{ _id=" + _id +
                ", userID=\"" + userID + "\"" +
                ", activityType=" + activityType +
                ", lastActivityType=" + ((lastActivityType > 0)? dateFormat.format(lastActivityType) : "N/A") +
                ", deviceStep=" + deviceStep +
                ", lastDeviceStep=" + ((lastDeviceStep > 0)? dateFormat.format(lastDeviceStep) : "N/A") +
                ", deviceBPM=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,deviceBPM) +
                ", lastDeviceBPM=" + ((lastDeviceBPM > 0)? dateFormat.format(lastDeviceBPM) : "N/A") +
                ", temperature=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,temperature) +
                ", humidity=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,humidity) +
                ", pressure=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,pressure) +
                ", lastDeviceOther=" + ((lastDeviceOther > 0)? dateFormat.format(lastDeviceOther) : "N/A") +
                ", fitStep=" +  fitStep +
                ", lastFitStep=" + ((lastFitStep > 0)? dateFormat.format(lastFitStep) : "N/A") +
                ", fitBPM=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,fitBPM) +
                ", lastFitBPM=" + ((lastFitBPM > 0)? dateFormat.format(lastFitBPM) : "N/A") +
                ", device2Step=" + device2Step +
                ", lastDevice2Step=" + ((lastDevice2Step > 0)? dateFormat.format(lastDevice2Step) : "N/A") +
                ", device2BPM=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,device2BPM) +
                ", lastDevice2BPM=" + ((lastDevice2BPM > 0)? dateFormat.format(lastDevice2BPM) : "N/A") +
                ", temperature2=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,temperature2) +
                ", humidity2=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,humidity2) +
                ", pressure2=" + String.format(Locale.getDefault(),Constants.SINGLE_FLOAT,pressure2) +
                ", lastDevice2Other=" + ((lastDeviceOther > 0)? dateFormat.format(lastDevice2Other) : "N/A") +
                ", lastUpdated=" + ((lastUpdated > 0)? dateFormat.format(lastUpdated) : "N/A") +
                ", fitLocation=\"" + fitLocation + "\"" +
                "}";
    }

}