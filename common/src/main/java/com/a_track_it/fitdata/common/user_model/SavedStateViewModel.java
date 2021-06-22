package com.a_track_it.fitdata.common.user_model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.data_model.Bodypart;
import com.a_track_it.fitdata.common.data_model.Configuration;
import com.a_track_it.fitdata.common.data_model.DailyCounter;
import com.a_track_it.fitdata.common.data_model.Exercise;
import com.a_track_it.fitdata.common.data_model.SensorDailyTotals;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutMeta;
import com.a_track_it.fitdata.common.data_model.WorkoutSet;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


public class SavedStateViewModel extends ViewModel {
    private static final String LOG_TAG = SavedStateViewModel.class.getSimpleName();
    private static final String ARG_ICON = "arg_activity_icon";
    private static final String ARG_COLOR = "arg_activity_color";
    private static final String ARG_USER_ID = "arg_user_id";
    private static final String ARG_DEVICE_ID = "arg_device_id";
    private static final String ARG_SESSION_GYM = "arg_session_gym";
    private static final String ARG_SESSION_SHOOT = "arg_session_shoot";
    private static final String ARG_WORKOUT = "arg_workout";
    private static final String ARG_WORKOUTSET = "arg_workoutset";
    private static final String ARG_WORKOUTMETA = "arg_workoutmeta";
    private static final String ARG_TODO_SETS = "arg_todo_sets";
    private static final String ARG_CONFIG_GOALS = "arg_config_goals";
    private static final String ARG_EXERCISE = "arg_exercise";
    private static final String ARG_BODYPART = "arg_bodypart";
    private static final String ARG_STATE = "arg_current_state";
    private static final String ARG_SET_INDEX = "arg_set_index";

    private static final String ARG_SET_DEFAULT = "arg_sets_default";
    private static final String ARG_REP_DEFAULT = "arg_reps_default";

    private static final String ARG_PAUSE_START = "arg_pause_start";
    private static final String ARG_REST_STOP = "arg_rest_stop";
    public static final String ARG_DAILY_STEP = "arg_daily_step";
    public static final String ARG_DAILY_BPM = "arg_daily_bpm";
    public static final String ARG_DAILY_CAL = "arg_daily_cal";
    public static final String ARG_DAILY_INACTIVE = "arg_daily_inactive";
    public static final String ARG_DAILY_ACTIVE = "arg_daily_active";
    public static final String ARG_DAILY_VEHICLE = "arg_daily_vehicle";
    public static final String ARG_REPS_COUNTER = "arg_reps_counter";
    private static final String ARG_SPEECH_TARGET = "arg_speech_target";
    private static final String STRING_INIT = Constants.ATRACKIT_EMPTY;
    private static final String ARG_LATITUDE = "arg_loc_lati";
    private static final String ARG_LONGITUDE = "arg_loc_longi";
    private static final String ARG_LOCATION_MSG = "arg_loc_msg";
    private static final String ARG_DIRTY = "arg_dirty";
    private static final String ARG_SDT = "arg_sdt";
    private static final String ARG_IN_PROGRESS = "arg_in_progress";
    private static final String ARG_LAST_DEVICE = "arg_last_device_msg";
    private static final String ARG_INIT = "arg_init_done";
    private static final String ARG_VIEW_PREFIX = "arg_view_";
    private final SavedStateHandle mState;
    private UserPreferences userPrefs;

    public SavedStateViewModel(SavedStateHandle savedStateHandle) {
        mState = savedStateHandle;
        if (mState != null){
            try {
                if (mState.contains(ARG_INIT) && ((Boolean) mState.get(ARG_INIT))) {
                } else {
                    initialise();
                }
            }catch(Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
                Log.e(SavedStateViewModel.class.getSimpleName(), "savedStateModel Error " + e.getMessage());
                initialise();
            }
        }

    }

    @Override
    protected void onCleared() {
        if (this.userPrefs != null) saveToPreferences(this.userPrefs);
        super.onCleared();
    }

    private void initialise(){
        if (mState != null) {
            mState.set(ARG_WORKOUT, null);
            mState.set(ARG_WORKOUTSET, null);
            mState.set(ARG_WORKOUTMETA, null);
            mState.set(ARG_EXERCISE, null);
            mState.set(ARG_ICON, 0);
            mState.set(ARG_COLOR, 0);
            mState.set(ARG_USER_ID, Constants.ATRACKIT_EMPTY);
            mState.set(ARG_DEVICE_ID, Constants.ATRACKIT_EMPTY);
            mState.set(ARG_STATE, 0);
            mState.set(ARG_PAUSE_START, 0L);
            mState.set(ARG_REST_STOP, 0L);
            mState.set(ARG_SESSION_GYM, false);
            mState.set(ARG_SESSION_SHOOT, false);
            mState.set(ARG_SET_INDEX, 0);
            mState.set(ARG_SET_DEFAULT, 3);
            mState.set(ARG_REP_DEFAULT, 10);

            DailyCounter bpmCounter = new DailyCounter();
            mState.set(ARG_DAILY_BPM, bpmCounter);
            DailyCounter stepCounter = new DailyCounter();
            mState.set(ARG_DAILY_STEP, stepCounter);
            DailyCounter calCounter = new DailyCounter();
            mState.set(ARG_DAILY_CAL, calCounter);
            DailyCounter inactiveCounter = new DailyCounter();
            mState.set(ARG_DAILY_INACTIVE, inactiveCounter);
            DailyCounter activeCounter = new DailyCounter();
            mState.set(ARG_DAILY_ACTIVE, activeCounter);
            DailyCounter vehicleCounter = new DailyCounter();
            mState.set(ARG_DAILY_VEHICLE, vehicleCounter);
            mState.set(ARG_SPEECH_TARGET, Constants.ATRACKIT_EMPTY);
            mState.set(ARG_LATITUDE,0D);
            mState.set(ARG_LONGITUDE,0D);
            mState.set(ARG_LOCATION_MSG,Constants.ATRACKIT_EMPTY);
            mState.set(ARG_DIRTY, 0);
            mState.set(ARG_SDT, null);
            DailyCounter repsCounter = new DailyCounter();
            mState.set(ARG_REPS_COUNTER, repsCounter);
            mState.set(ARG_INIT, true);
            mState.set(ARG_IN_PROGRESS, 0);
            List<WorkoutSet> todo = new ArrayList<>();
            mState.set(ARG_TODO_SETS, todo);
            List<Configuration> goals = new ArrayList<>();
            mState.set(ARG_CONFIG_GOALS, goals);
            mState.set(ARG_LAST_DEVICE, 0L);
            if (userPrefs == null) {
                String sKey;
                for (int i = 1; i <= 13; i++) {
                    sKey = ARG_VIEW_PREFIX + i;
                    mState.set(sKey, 0);
                }
                sKey = ARG_VIEW_PREFIX + 14;
                mState.set(sKey, Constants.ATRACKIT_EMPTY);
            }
            else
                loadFromPreferences(userPrefs);
        }
    }

    public void setUserPreferences(UserPreferences u){ this.userPrefs = u; loadFromPreferences(u);}
    public void saveToPreferences(UserPreferences userPreferences){
        this.userPrefs = userPreferences;
        String sKey;
        for (int i=1; i <= 12; i++){
            sKey = ARG_VIEW_PREFIX + i;
            userPreferences.setLongPrefByLabel(sKey, getViewState(i));
        }
        int setIndex = getSetIndex();
        userPreferences.setLongPrefByLabel(ARG_VIEW_PREFIX + 13, (long)setIndex);
        SensorDailyTotals sdt = getSDT();
        if (sdt != null) {
            try {
                Gson gson = new Gson();
                String sSDT = gson.toJson(sdt,SensorDailyTotals.class);
                userPreferences.setPrefStringByLabel(ARG_VIEW_PREFIX + 14, sSDT);
            }catch (Exception e){
                userPreferences.setPrefStringByLabel(ARG_VIEW_PREFIX + 14, Constants.ATRACKIT_EMPTY);
            }
        }else
            userPreferences.setPrefStringByLabel(ARG_VIEW_PREFIX + 14, Constants.ATRACKIT_EMPTY);
    }
    public void loadFromPreferences(UserPreferences userPreferences){
        this.userPrefs = userPreferences;
        String sKey;
        for (int i=1; i <= 12; i++){
            sKey = ARG_VIEW_PREFIX + i;
            long state = userPreferences.getLongPrefByLabel(sKey);
            setViewState(i, (int)state);
        }
        sKey = ARG_VIEW_PREFIX + 13;
        int iSet = Math.toIntExact(userPreferences.getLongPrefByLabel(sKey));
        mState.set(ARG_SET_INDEX,iSet);
        sKey = ARG_VIEW_PREFIX + 14;
        String sSDT = userPreferences.getPrefStringByLabel(sKey);
        if (sSDT.length() > 0){
            try {
                Gson gson = new Gson();
                SensorDailyTotals sdt = gson.fromJson(sSDT, SensorDailyTotals.class);
                mState.set(ARG_SDT, sdt);
            }catch (Exception e){
                mState.set(ARG_SDT, null);
            }
        }else
            mState.set(ARG_SDT, null);
        mState.set(ARG_USER_ID, userPreferences.getUserId());
    }
    public void setViewState(int viewIndex, int state){
        String sKey = ARG_VIEW_PREFIX + Integer.toString(viewIndex);
        mState.set(sKey, state);
    }
    public int getViewState(int viewIndex){
        String sKey = ARG_VIEW_PREFIX + Integer.toString(viewIndex);
        return (mState.contains(sKey) && (mState.get(sKey) != null)) ? (int)mState.get(sKey) : 0;
    }
    public void setUserIDLive(String u){ mState.set(ARG_USER_ID, u);}
    public LiveData<String> getUserIDLive(){return (mState.contains(ARG_USER_ID)) ? mState.getLiveData(ARG_USER_ID, Constants.ATRACKIT_EMPTY) : null; }

    public void setDeviceID(String u){ mState.set(ARG_DEVICE_ID, u);}
    public String getDeviceID(){return (mState.contains(ARG_DEVICE_ID)) ? mState.get(ARG_DEVICE_ID) : Constants.ATRACKIT_EMPTY; }

    public Integer getIconID(){ return (mState.contains(ARG_ICON)) ? mState.get(ARG_ICON) : null; }

    public Integer getColorID(){
        return  (mState.contains(ARG_COLOR)) ? mState.get(ARG_COLOR) : null;
    }
    public Boolean getIsGym(){
        return (mState.contains(ARG_SESSION_GYM)) ? mState.get(ARG_SESSION_GYM) : false;
    }
    public Boolean getIsShoot(){
        return (mState.contains(ARG_SESSION_SHOOT)) ? mState.get(ARG_SESSION_SHOOT) : false;
    }
    public Long getPauseStart(){
        return (mState.contains(ARG_PAUSE_START)) ? mState.get(ARG_PAUSE_START) : 0L;
    }
    public Long getRestStop(){
        return (mState.contains(ARG_REST_STOP)) ? mState.get(ARG_REST_STOP) : 0L;
    }
    public Long getLastDeviceMsg(){
        return (mState.contains(ARG_LAST_DEVICE)) ? mState.get(ARG_LAST_DEVICE) : 0L;
    }
    public void setLastDeviceMsg(long last){ mState.set(ARG_LAST_DEVICE, last);}

    public boolean isSessionSetup(){
        boolean bSet = false;
        if (mState.contains(ARG_WORKOUT))
            bSet = (mState.get(ARG_WORKOUT) instanceof  Workout);
        return bSet;
    }
    public LiveData<Workout> getActiveWorkout(){
        return (mState.contains(ARG_WORKOUT)) ? mState.getLiveData(ARG_WORKOUT) : null;
    }
    public LiveData<WorkoutSet> getActiveWorkoutSet(){
        return (mState.contains(ARG_WORKOUTSET)) ? mState.getLiveData(ARG_WORKOUTSET) : null;
    }
    public LiveData<WorkoutMeta> getActiveWorkoutMeta(){
        return (mState.contains(ARG_WORKOUTMETA)) ? mState.getLiveData(ARG_WORKOUTMETA) : null;
    }
    public LiveData<List<WorkoutSet>> getToDoSets(){
        return (mState.contains(ARG_TODO_SETS)) ? mState.getLiveData(ARG_TODO_SETS) : null;
    }
    public void setToDoSets(List<WorkoutSet> listToDo){ mState.set(ARG_TODO_SETS, listToDo);}

    public int getToDoSetsSize(){
        int retVal = 0;
        try {
            if ((mState.contains(ARG_TODO_SETS)) && (mState.get(ARG_TODO_SETS) != null)) {
                List<WorkoutSet> list = mState.get(ARG_TODO_SETS);
                retVal = (list != null) ? list.size() : 0;
            }
        }catch (Exception e){
            retVal = 0;
        }
        return retVal;
    }
    public LiveData<List<Configuration>> getGoalsList(){
        return (mState.contains(ARG_CONFIG_GOALS)) ? mState.getLiveData(ARG_CONFIG_GOALS) : null;
    }
    public void setGoalsList(List<Configuration> listGoals){   mState.set(ARG_CONFIG_GOALS, listGoals);}
    public int getGoalsListSize(){
        int retVal = 0;
        if ((mState.contains(ARG_CONFIG_GOALS)) && (mState.get(ARG_CONFIG_GOALS) != null)) {
            List<Configuration> list = mState.get(ARG_CONFIG_GOALS);
            retVal = (list != null) ? list.size() : 0;
        }
        return retVal;
    }
    public LiveData<Exercise> getExercise(){
        return (mState.contains(ARG_EXERCISE)) ? mState.getLiveData(ARG_EXERCISE) : null;
    }
    public void setExercise(Exercise exercise){   mState.set(ARG_EXERCISE, exercise);  }

    public LiveData<Bodypart> getBodypart(){  return (mState.contains(ARG_BODYPART)) ? mState.getLiveData(ARG_BODYPART) : null;  }

    public void setBodypart(Bodypart bodypart){ mState.set(ARG_BODYPART, bodypart);  }

    public Integer getSetIndex(){
        int val = mState.contains(ARG_SET_INDEX) ? mState.get(ARG_SET_INDEX) : 0;
        Log.w(LOG_TAG,"getSetIndex " + val);
        return val;
    }

    public LiveData<Integer> currentSetIndex(){
        return mState.contains(ARG_SET_INDEX) ? mState.getLiveData(ARG_SET_INDEX) : null;}

    public void setSetIndex(int argSet){
        mState.set(ARG_SET_INDEX, argSet);
        Log.e(LOG_TAG,"setSetIndex " + argSet);
    }

    public void setIconID(int iconID){
        mState.set(ARG_ICON, iconID);
    }
    public void setColorID(int colorID){
        mState.set(ARG_COLOR, colorID);
    }
    public void setSetIsGym(boolean isGym){
        mState.set(ARG_SESSION_GYM, isGym);
    }
    public void setSetIsShoot(boolean isShoot){
        mState.set(ARG_SESSION_SHOOT, isShoot);
    }
    public void setActiveWorkout(Workout w){
        mState.set(ARG_WORKOUT, w);
    }
    public void setActiveWorkoutSet(WorkoutSet s){
        mState.set(ARG_WORKOUTSET, s);
    }
    public void setActiveWorkoutMeta(WorkoutMeta m){ mState.set(ARG_WORKOUTMETA, m); }
    public void setPauseStart(long pauseDuration){ mState.set(ARG_PAUSE_START, pauseDuration); }
    public void setRestStop(long restStop){ mState.set(ARG_REST_STOP, restStop); }

    public LiveData<Integer> getCurrentState(){ return  (mState.contains(ARG_STATE)) ? mState.getLiveData(ARG_STATE) : null; }
    public void setCurrentState(int state){
        mState.set(ARG_STATE, state);
    }
    public int getState(){
        int iVal;
        try{
            iVal = (mState.contains(ARG_STATE)) ? (Integer)mState.get(ARG_STATE) : Constants.WORKOUT_INVALID;
        }catch (NullPointerException np){
            iVal = Constants.WORKOUT_INVALID;
        };
        return iVal;
    }
    public int getIsInProgress(){ return (mState.contains(ARG_IN_PROGRESS)) ? mState.get(ARG_IN_PROGRESS) : 0;  }
    public void setIsInProgress(int isInProgress){ mState.set(ARG_IN_PROGRESS, isInProgress); }

    public void setReps(DailyCounter reps){ mState.set(ARG_REPS_COUNTER, reps);}
    public void setBPM(DailyCounter bpm){ mState.set(ARG_DAILY_BPM, bpm);}
    public void setSteps(DailyCounter steps){ mState.set(ARG_DAILY_STEP, steps);}
    public void setCal(DailyCounter calories){ mState.set(ARG_DAILY_CAL, calories);}
    public DailyCounter getBPM(){ return mState.get(ARG_DAILY_BPM);}
    public DailyCounter getSteps(){ return mState.get(ARG_DAILY_STEP);}
    public DailyCounter getCal(){ return mState.get(ARG_DAILY_CAL);}
    public DailyCounter getReps(){ return mState.get(ARG_REPS_COUNTER);}

    public DailyCounter getCounterByName(String argName){ return mState.get(argName);}
    public void setCounterByName(String argName, DailyCounter counter){ mState.set(argName, counter);}

    public String getSpeechTarget(){String sTarget = mState.get(ARG_SPEECH_TARGET); return sTarget; }
    public void setSpeechTarget(String speechTarget){ mState.set(ARG_SPEECH_TARGET, speechTarget);}

    public void setLongitude(double longi){ mState.set(ARG_LONGITUDE, longi);}
    public void setLatitude(double lati){ mState.set(ARG_LATITUDE, lati);}

    public double getLongitude(){
        double fLong;
        try{
            fLong = mState.get(ARG_LONGITUDE);
        }catch (NullPointerException e){
            fLong = -1D;
        }
        return fLong;
    }
    public double getLatitude(){
        double fLat;
        try{
            fLat = mState.get(ARG_LATITUDE);
        }catch (NullPointerException e){
            fLat = -1D;
        }
        return fLat;
    }
    public LiveData<String> getLocationMsg() { return mState.getLiveData(ARG_LOCATION_MSG);}
    public String getLocationAddress(){ return (mState.get(ARG_LOCATION_MSG) != null) ? mState.get(ARG_LOCATION_MSG) : Constants.ATRACKIT_EMPTY; }
    public void addLocationMsg(String sMsg){ this.mState.set(ARG_LOCATION_MSG, sMsg);}

    public Integer getDirtyCount(){ return (mState.contains(ARG_DIRTY)) ? mState.get(ARG_DIRTY) : 0; }
    public void setDirtyCount(int argCount){
        Log.e(LOG_TAG,"dirty count set " + argCount);
        mState.set(ARG_DIRTY, argCount);  }

    public SensorDailyTotals getSDT(){ return (mState.contains(ARG_SDT)) ? (SensorDailyTotals)mState.get(ARG_SDT) : null; }
    public void setSDT(SensorDailyTotals sdt){ mState.set(ARG_SDT, sdt);  }

    public Integer getSetsDefault(){ return (mState.contains(ARG_SET_DEFAULT)) ? mState.get(ARG_SET_DEFAULT) : 3; }
    public LiveData<Integer> newSets(){ return mState.getLiveData(ARG_SET_DEFAULT); }
    public void setSetsDefault(int argNewVal){ mState.set(ARG_SET_DEFAULT, argNewVal);  }
    public Integer getRepsDefault(){ return (mState.contains(ARG_REP_DEFAULT)) ? mState.get(ARG_REP_DEFAULT) : 10; }
    public void setRepsDefault(int argNewVal){ mState.set(ARG_REP_DEFAULT, argNewVal);  }
    public LiveData<Integer> newReps(){ return mState.getLiveData(ARG_REP_DEFAULT); }

}

