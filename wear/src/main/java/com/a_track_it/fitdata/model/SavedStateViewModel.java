package com.a_track_it.fitdata.model;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.data_model.Workout;
import com.a_track_it.fitdata.data_model.WorkoutSet;


public class SavedStateViewModel extends ViewModel {
    private static final String ARG_ICON = "arg_activity_icon";
    private static final String ARG_COLOR = "arg_activity_color";
    private static final String ARG_SESSION_GYM = "arg_session_gym";
    private static final String ARG_SESSION_SHOOT = "arg_session_shoot";
    private static final String ARG_WORKOUT = "arg_workout";
    private static final String ARG_WORKOUTSET = "arg_workoutset";
    private static final String ARG_STATE = "arg_current_state";
    private static final String ARG_SETINDEX = "arg_set_index";
    private static final String ARG_GOAL_DURATION = "arg_goal_duration";
    private static final String ARG_GOAL_STEPS = "arg_goal_steps";
    private static final String ARG_PAUSE_START = "arg_pause_start";
    private static final String STRING_INIT = "";

    private SavedStateHandle mState;

    public SavedStateViewModel(SavedStateHandle savedStateHandle) {
        mState = savedStateHandle;

    }
    public void initialise(){
        if (mState != null) {
          //  mState.set(ARG_WORKOUT, null);
          //  mState.set(ARG_WORKOUTSET, null);
            mState.set(ARG_ICON, 0L);
            mState.set(ARG_COLOR, 0L);
            mState.set(ARG_STATE, 0);
            mState.set(ARG_GOAL_DURATION, 0);
            mState.set(ARG_GOAL_STEPS, 0);
            mState.set(ARG_PAUSE_START, 0L);
            mState.set(ARG_SESSION_GYM, false);
            mState.set(ARG_SESSION_SHOOT, false);
            mState.set(ARG_SETINDEX, 0);
            mState.set(ARG_STATE, Constants.STATE_HOME);
        }
    }

    // Expose an immutable LiveData


    public LiveData<Long> getIconID(){
        return (mState.contains(ARG_ICON)) ? mState.getLiveData(ARG_ICON) : null;
    }

    public LiveData<Long> getColorID(){
        return  (mState.contains(ARG_COLOR)) ? mState.getLiveData(ARG_COLOR) : null;

    }
    public LiveData<Boolean> getIsGym(){
        return (mState.contains(ARG_SESSION_GYM)) ? mState.getLiveData(ARG_SESSION_GYM) : null;

    }
    public LiveData<Boolean> getIsShoot(){
        return (mState.contains(ARG_SESSION_SHOOT)) ? mState.getLiveData(ARG_SESSION_SHOOT) : null;

    }
    public LiveData<Long> getPauseStart(){
        return (mState.contains(ARG_PAUSE_START)) ? mState.getLiveData(ARG_PAUSE_START) : null;

    }
    public LiveData<Workout> getActiveWorkout(){
        return (mState.contains(ARG_WORKOUT)) ? mState.getLiveData(ARG_WORKOUT) : null;
    }
    public LiveData<WorkoutSet> getActiveWorkoutSet(){
        return (mState.contains(ARG_WORKOUTSET)) ? mState.getLiveData(ARG_WORKOUTSET) : null;
    }
    public LiveData<Integer> getCurrentState(){
        return  (mState.contains(ARG_STATE)) ? mState.getLiveData(ARG_STATE) : null;
    }
    public LiveData<Integer> getSetIndex(){
        return (mState.contains(ARG_SETINDEX)) ? mState.getLiveData(ARG_SETINDEX) : null;
    }
    public void setSetIndex(int argSetindex){
        mState.set(ARG_SETINDEX, argSetindex);
    }
    public LiveData<Integer> getGoalDuration(){
        return (mState.contains(ARG_GOAL_DURATION)) ? mState.getLiveData(ARG_GOAL_DURATION) : null;
    }
    public void setGoalDuration(int argGoalDuration){
        mState.set(ARG_GOAL_DURATION, argGoalDuration);
    }
    public LiveData<Integer> getGoalSteps(){
        return (mState.contains(ARG_GOAL_STEPS)) ? mState.getLiveData(ARG_GOAL_STEPS) : null;
    }
    public void setGoalSteps(int argGoalSteps){
        mState.set(ARG_GOAL_STEPS, argGoalSteps);
    }
    public void setCurrentState(int state){
        mState.set(ARG_STATE, state);
    }

    public void setIconID(long iconID){
        mState.set(ARG_ICON, iconID);
    }
    public void setColorID(long colorID){
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
    public void setPauseStart(long pauseDuration){
        mState.set(ARG_PAUSE_START, pauseDuration);
    }


}

