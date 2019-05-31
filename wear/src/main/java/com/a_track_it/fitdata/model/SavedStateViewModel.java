package com.a_track_it.fitdata.model;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;

import com.a_track_it.fitdata.common.model.Workout;
import com.a_track_it.fitdata.common.model.WorkoutSet;


public class SavedStateViewModel extends ViewModel {
    private static final String ARG_ACTIVITYID = "arg_activityid";
    private static final String ARG_NAME = "arg_activity_name";
    private static final String ARG_SESSIONID = "arg_session_id";
    private static final String ARG_SETID = "arg_set_id";
    private static final String ARG_WORKOUT = "arg_workout";
    private static final String ARG_WORKOUTSET = "arg_workoutset";
    private static final String ARG_STATE = "arg_current_state";


    private SavedStateHandle mState;

    public SavedStateViewModel(SavedStateHandle savedStateHandle) {
        mState = savedStateHandle;
    }

    // Expose an immutable LiveData
    LiveData<String> getActivityName() {
        // getLiveData obtains an object that is associated with the key wrapped in a LiveData
        // so it can be observed for changes.
        return (mState.contains(ARG_NAME)) ? mState.getLiveData(ARG_NAME) : null;
    }

    LiveData<Long> getActivityID(){
        return (mState.contains(ARG_ACTIVITYID)) ? mState.getLiveData(ARG_ACTIVITYID) : null;
    }

    LiveData<Long> getSessionID(){
        return  (mState.contains(ARG_SESSIONID)) ? mState.getLiveData(ARG_SESSIONID) : null;

    }
    LiveData<Long> getSetID(){
        return (mState.contains(ARG_SETID)) ? mState.getLiveData(ARG_SETID) : null;

    }
    LiveData<Workout> getActiveWorkout(){
        return (mState.contains(ARG_WORKOUT)) ? mState.getLiveData(ARG_WORKOUT) : null;
    }
    LiveData<WorkoutSet> getActiveWorkoutSet(){
        return (mState.contains(ARG_WORKOUTSET)) ? mState.getLiveData(ARG_WORKOUTSET) : null;
    }
    LiveData<Integer> getCurrentState(){
        return  (mState.contains(ARG_STATE)) ? mState.getLiveData(ARG_STATE) : null;
    }

    void setCurrentState(int state){
        mState.set(ARG_STATE, state);
    }

    void setActivityName(String newName) {
        // Sets a new value for the object associated to the key. There's no need to set it
        // as a LiveData.
        mState.set(ARG_NAME, newName);
    }

    void setActivityID(long activityID){
        mState.set(ARG_SESSIONID, activityID);
    }
    void setSessionID(long sessionID){
        mState.set(ARG_SESSIONID, sessionID);
    }
    void setSetID(long setID){
        mState.set(ARG_SESSIONID, setID);
    }
    void setActiveWorkout(Workout w){
        mState.set(ARG_WORKOUT, w);
    }
    void setActiveWorkoutSet(WorkoutSet s){
        mState.set(ARG_WORKOUTSET, s);
    }


}

