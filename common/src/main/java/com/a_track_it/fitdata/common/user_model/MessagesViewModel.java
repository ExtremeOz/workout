package com.a_track_it.fitdata.common.user_model;

import android.app.Application;
import android.content.Intent;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.a_track_it.fitdata.common.data_model.Configuration;

import java.util.ArrayList;
import java.util.List;

public class MessagesViewModel extends AndroidViewModel {
    final private MutableLiveData<String> currentMsg;
    final private MutableLiveData<String> speechMsg;
    final private MutableLiveData<String> batteryMsg;
    final private MutableLiveData<String> locationMsg;
    final private MutableLiveData<String> bpmMsg;
    final private MutableLiveData<String> device_bpmMsg;
    final private MutableLiveData<String> device2_bpmMsg;
    final private MutableLiveData<String> stepsMsg;
    final private MutableLiveData<String> device_stepsMsg;
    final private MutableLiveData<String> device2_stepsMsg;
    final private MutableLiveData<String> exercisesMsg;
    final private MutableLiveData<String> activityMsg;
    final private MutableLiveData<String> speedMsg;
    final private MutableLiveData<String> caloriesMsg;
    final private MutableLiveData<String> distanceMsg;
    final private MutableLiveData<String> pressureMsg;
    final private MutableLiveData<String> pressure2Msg;
    final private MutableLiveData<String> temperatureMsg;
    final private MutableLiveData<String> temperature2Msg;
    final private MutableLiveData<String> humidityMsg;
    final private MutableLiveData<String> humidity2Msg;
    final private MutableLiveData<String> moveMinsMsg;
    final private MutableLiveData<String> heartPtsMsg;
    final private MutableLiveData<Boolean> workInProgress;
    final private MutableLiveData<Boolean> cloudAvailable;
    final private MutableLiveData<Boolean> phoneAvailable;
    final private MutableLiveData<Boolean> useLocation;
    final private MutableLiveData<String> nodeDisplayName;
    final private MutableLiveData<Integer> detectedReps;
    final private MutableLiveData<Double> altitudeMsg;
    final private MutableLiveData<Double> altitude2Msg;
    private long mLastRep;
    private int mWorkType;
    final private MutableLiveData<Long> lastDate;
    final private MutableLiveData<List<Long>> reportParameters;
    final private MutableLiveData<Intent> liveIntents;
    final private MutableLiveData<List<Configuration>> liveConfigs;
    @Override
    protected void onCleared() {
        super.onCleared();
      //  Log.d(MessagesViewModel.class.getSimpleName(), "onCleared" );
    }

    public MessagesViewModel(Application app) {
        super(app);
        currentMsg = new MutableLiveData<>();
        speechMsg = new MutableLiveData<>();
        batteryMsg = new MutableLiveData<>();
        locationMsg = new MutableLiveData<>();
        bpmMsg = new MutableLiveData<>();
        device_bpmMsg = new MutableLiveData<>();
        device2_bpmMsg = new MutableLiveData<>();
        stepsMsg = new MutableLiveData<>();
        device_stepsMsg = new MutableLiveData<>();
        device2_stepsMsg = new MutableLiveData<>();
        exercisesMsg = new MutableLiveData<>();
        activityMsg = new MutableLiveData<>();
        speedMsg = new MutableLiveData<>();
        caloriesMsg = new MutableLiveData<>();
        distanceMsg = new MutableLiveData<>();
        pressureMsg = new MutableLiveData<>();
        pressure2Msg = new MutableLiveData<>();
        temperatureMsg = new MutableLiveData<>();
        temperature2Msg = new MutableLiveData<>();
        humidityMsg = new MutableLiveData<>();
        humidity2Msg = new MutableLiveData<>();
        moveMinsMsg = new MutableLiveData<>();
        heartPtsMsg = new MutableLiveData<>();
        workInProgress = new MutableLiveData<>();
        cloudAvailable = new MutableLiveData<>();
        phoneAvailable = new MutableLiveData<>();
        useLocation = new MutableLiveData<>();
        nodeDisplayName = new MutableLiveData<>();
        detectedReps = new MutableLiveData<>();
        altitudeMsg = new MutableLiveData<>();
        altitude2Msg = new MutableLiveData<>();
        lastDate = new MutableLiveData<>();
        reportParameters = new MutableLiveData<>();
        liveIntents = new MutableLiveData<>();
        liveConfigs = new MutableLiveData<>();
        mLastRep = 0L;
        mWorkType = 0;
    }
    public int getWorkType(){ return mWorkType;}
    public void setWorkType(int type){ mWorkType = type; }

    public MutableLiveData<Integer> getDetectedReps(){ return detectedReps;}
    public Integer getDetectedRep(){ return (detectedReps.getValue() != null) ? detectedReps.getValue() : 0;}

    public void setDetectedReps(Integer reps){ detectedReps.postValue(reps); if (reps == 0) mLastRep = 0L;}
    public void addDetectedRep(){
        int currentReps = getDetectedRep();
        detectedReps.postValue(++currentReps);
    }
    public LiveData<Intent> getLiveIntent(){ return liveIntents;}
    public void addLiveIntent(Intent toAdd){ liveIntents.postValue(toAdd);}

    public List<Configuration> getLiveConfig(){
        List<Configuration> list = new ArrayList<>();
        if (liveConfigs.getValue() != null) list = liveConfigs.getValue();
        return list;
    }
    public void addLiveConfig(Configuration toAdd){
        List<Configuration> list = new ArrayList<>();
        if (liveConfigs.getValue() == null) {
            list.add(toAdd);
        }else{
            list = liveConfigs.getValue();
            list.add(toAdd);
        }
        liveConfigs.postValue(list);
    }
    public void setLiveConfig(List<Configuration> list){
        if (list != null) liveConfigs.postValue(list);
        else clearLiveConfig();
    }
    public void clearLiveConfig(){
        List<Configuration> list = new ArrayList<>();
        liveConfigs.postValue(list);
    }
    public MutableLiveData<String> getCurrentMsg() { return currentMsg; }
    public void addCurrentMsg(String sMsg){ currentMsg.postValue(sMsg);}

    public MutableLiveData<String> getSpokenMsg() { return speechMsg; }
    public void addSpokenMsg(String sMsg){ speechMsg.postValue(sMsg);}

    public long getLastDate(){ if (this.lastDate.getValue() == null) return System.currentTimeMillis(); else return this.lastDate.getValue();}
    public void setLastDate(long lastDate){ this.lastDate.setValue(lastDate);}

    public LiveData<List<Long>> getReportParameters(){ return this.reportParameters; }
    public void addReportParameters(List<Long> params){
        this.reportParameters.setValue(params);}

    public LiveData<String> getBatteryMsg() { return batteryMsg; }
    public void addBatteryMsg(String sMsg){ this.batteryMsg.postValue(sMsg);}

    public LiveData<String> getBpmMsg() {
        return bpmMsg;
    }
    public void addBpmMsg(String sMsg){ this.bpmMsg.postValue(sMsg);}

    public LiveData<String> getDeviceBpmMsg() {
        return device_bpmMsg;
    }
    public void addDeviceBpmMsg(String sMsg){ this.device_bpmMsg.postValue(sMsg);}

    public LiveData<String> getDevice2BpmMsg() {
        return device2_bpmMsg;
    }
    public void addDevice2BpmMsg(String sMsg){ this.device2_bpmMsg.postValue(sMsg);}

    public LiveData<String> getDeviceStepsMsg() { return device_stepsMsg; }
    public void addDeviceStepsMsg(String sMsg){ this.device_stepsMsg.postValue(sMsg);}

    public LiveData<String> getDevice2StepsMsg() { return device2_stepsMsg; }
    public void addDevice2StepsMsg(String sMsg){ this.device2_stepsMsg.postValue(sMsg);}

    public LiveData<String> getStepsMsg() { return stepsMsg; }
    public void addStepsMsg(String sMsg){ this.stepsMsg.postValue(sMsg); }

    public MutableLiveData<String> getExercisesMsg() {
        return exercisesMsg;
    }
    public void addExerciseMsg(String sMsg){ this.exercisesMsg.postValue(sMsg);}

    public LiveData<String> getSpeedMsg() { return speedMsg; }
    public void addSpeedMsg(String sMsg){ this.speedMsg.postValue(sMsg);}
    public LiveData<Double> getAltitudeMsg() { return altitudeMsg; }
    public void addAltitudeMsg(Double fMsg){ this.altitudeMsg.postValue(fMsg);}
    public LiveData<String> getActivityMsg() { return activityMsg; }
    public void addActivityMsg(String sMsg){ this.activityMsg.postValue(sMsg);}
    public LiveData<String> getMoveMinsMsg() { return moveMinsMsg; }
    public void addMoveMinsMsg(String sMsg){ this.moveMinsMsg.postValue(sMsg);}
    public LiveData<String> getHeartPtsMsg() { return heartPtsMsg; }
    public void addHeartPtsMsg(String sMsg){ this.heartPtsMsg.postValue(sMsg);}
    public LiveData<String> getCaloriesMsg() { return caloriesMsg; }
    public void addCaloriesMsg(String sMsg){ this.caloriesMsg.postValue(sMsg);}
    public LiveData<String> getDistanceMsg() { return distanceMsg; }
    public void addDistanceMsg(String sMsg){ this.distanceMsg.postValue(sMsg);}
    public LiveData<String> getPressureMsg() { return pressureMsg; }
    public void addPressureMsg(String sMsg){ this.pressureMsg.postValue(sMsg);}
    public LiveData<String> getTemperatureMsg() { return temperatureMsg; }
    public void addTemperatureMsg(String sMsg){ this.temperatureMsg.postValue(sMsg);}
    public LiveData<String> getPressure2Msg() { return pressure2Msg; }
    public void addPressure2Msg(String sMsg){ this.pressure2Msg.postValue(sMsg);}
    public LiveData<String> getTemperature2Msg() { return temperature2Msg; }
    public void addTemperature2Msg(String sMsg){ this.temperature2Msg.postValue(sMsg);}
    public LiveData<String> getHumidityMsg() { return humidityMsg; }
    public void addHumidityMsg(String sMsg){ this.humidityMsg.postValue(sMsg);}
    public LiveData<String> getHumidity2Msg() { return humidity2Msg; }
    public void addHumidity2Msg(String sMsg){ this.humidity2Msg.postValue(sMsg);}
    public void setCloudAvailable(Boolean available){ this.cloudAvailable.postValue(available);}
    public LiveData<Boolean> getCloudAvailable(){ return this.cloudAvailable;}
    public void setUseLocation(Boolean available){ this.useLocation.postValue(available);}
    public LiveData<Boolean> getUseLocation(){ return this.useLocation;}

    public boolean useLocation() {
        boolean bUse = false;
        if ((this.useLocation != null) && (this.useLocation.getValue() != null)) bUse = this.useLocation.getValue();
        return bUse;
    }
    public void setPhoneAvailable(Boolean available){
        this.phoneAvailable.postValue(available);}
    public LiveData<Boolean> getPhoneAvailable(){ return this.phoneAvailable;}
    public boolean hasPhone(){ if ((this.phoneAvailable != null) && (this.phoneAvailable.getValue() != null)) return this.phoneAvailable.getValue(); return false; }

    public void setNodeDisplayName(String name){ this.nodeDisplayName.postValue(name);}
    public LiveData<String> getNodeDisplayName(){ return this.nodeDisplayName;}

    public boolean isWorkInProgress(){
        boolean bUse = false;
        if ((this.workInProgress != null) && (this.workInProgress.getValue() != null)) bUse = this.workInProgress.getValue();
        return bUse;
    }
    public void setWorkInProgress(Boolean isInProgress){
        boolean bExisting = this.isWorkInProgress();
        if (isInProgress){
            if (bExisting) this.workInProgress.setValue(false); // clear it first
        }
        this.workInProgress.postValue(isInProgress);}
    public LiveData<Boolean> getWorkInProgress(){ return this.workInProgress;}

}
