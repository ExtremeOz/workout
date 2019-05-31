package com.a_track_it.fitdata.model;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.a_track_it.fitdata.common.model.Bodypart;
import com.a_track_it.fitdata.common.model.Exercise;
import com.a_track_it.fitdata.common.model.FitnessActivity;
import com.a_track_it.fitdata.common.model.Workout;
import com.a_track_it.fitdata.common.model.WorkoutSet;

import java.util.ArrayList;


public class SessionViewModel extends AndroidViewModel{
    private static final String TAG = SessionViewModel.class.getSimpleName();
    private DataRepository mDataRepository;

    private boolean doSaveInAction = false;

    private final MutableLiveData<Workout> workoutMutableLiveData ;
    private final MutableLiveData<WorkoutSet> workoutSetMutableLiveData;
    private final MutableLiveData<Integer> currentSetIndex = new MutableLiveData<>();
    private final MutableLiveData<Integer> goalDuration = new MutableLiveData<>();
    private final MutableLiveData<Integer> goalSteps = new MutableLiveData<>();
    private final MutableLiveData<Long> pauseStart = new MutableLiveData<>();

    private final MutableLiveData<ArrayList<WorkoutSet>> completedSetsListMutableLiveData;
    private final MutableLiveData<ArrayList<Workout>> completedSessionsListMutableLiveData;
    private final MutableLiveData<ArrayList<WorkoutSet>> todoSetsListMutableLiveData;

    public  final LiveData<Boolean> actionInProgress;

    public SessionViewModel(@NonNull Application application) {
        super(application);

        Log.d(TAG, TAG + " created ");
        mDataRepository = DataRepository.getInstance(application);
        workoutMutableLiveData = mDataRepository.getWorkout();
        workoutSetMutableLiveData = mDataRepository.getWorkoutSet();
        completedSessionsListMutableLiveData = mDataRepository.getCompletedSessions();
        completedSetsListMutableLiveData = mDataRepository.getCompletedSets();
        todoSetsListMutableLiveData = mDataRepository.getToDoSets();
        currentSetIndex.postValue(new Integer(0));

        goalDuration.postValue(new Integer(0));
        goalSteps.postValue(new Integer(0));
        actionInProgress = mDataRepository.getActionInProgress();

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d(TAG, "finalized");
    }

    public void getReferenceData(){
        if ((mDataRepository != null) && (!mDataRepository.getLoadRefsInProgress())) {
            if (!mDataRepository.areReferencesLoaded()) mDataRepository.loadReferences();
        }
        return;
    }

    public boolean areReferencesLoaded (){ return mDataRepository.areReferencesLoaded();}

    public boolean isLoadingRefs(){return mDataRepository.getLoadRefsInProgress();}
    // GETTERS
    public ArrayList<FitnessActivity> getFitnessActivityArrayList(){ return mDataRepository.getFitnessActivityArrayList(); }
    public ArrayList<Exercise> getExerciseArrayList(){ return mDataRepository.getExerciseArrayList();    }
    public ArrayList<Bodypart> getBodypartArrayList(){ return mDataRepository.getBodypartArrayList();    }
    // current set index maintained across states and fragments more easily
    public LiveData<Integer> getCurrentSetIndex(){ return currentSetIndex;};
    public void setCurrentSetIndex(int i){ currentSetIndex.postValue(new Integer(i));}

    public LiveData<Integer> getGoalSteps(){ return goalSteps;};
    public void setGoalSteps(int i){ goalSteps.postValue(new Integer(i));}

    public LiveData<Integer> getGoalDuration(){ return goalDuration;};
    public void setGoalDuration(int i){ goalDuration.postValue(new Integer(i));}
    
    public LiveData<Long> getPauseStart(){ return pauseStart;};
    public void setPauseStart(long i){ pauseStart.postValue(new Long(i));}

    public LiveData<WorkoutSet> getWorkoutSet() {
        return workoutSetMutableLiveData;
    }
    public LiveData<Workout> getWorkout() {
        return workoutMutableLiveData;
    }

    public LiveData<ArrayList<WorkoutSet>> getToDoSets(){
        return todoSetsListMutableLiveData;
    }
    public int getToDoSetSize(){ return (todoSetsListMutableLiveData.getValue() == null) ? 0 :todoSetsListMutableLiveData.getValue().size();}

    public LiveData<ArrayList<Workout>> getCompletedWorkouts(){ return completedSessionsListMutableLiveData;  }
    public LiveData<ArrayList<WorkoutSet>> getCompletedSets(){ return completedSetsListMutableLiveData; }
    public int getCompletedSetSize(){ return (todoSetsListMutableLiveData.getValue() == null) ? 0 :todoSetsListMutableLiveData.getValue().size();}
    @Override
    protected void onCleared() {
        super.onCleared();
        Log.i(TAG, "onCleared!");
    }

    // SETTERS
    //
    public void saveSingleType(final boolean doDelete, final int type){
        if (!doSaveInAction) {
            doSaveInAction = true;
            android.os.Handler handler = new android.os.Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mDataRepository.doSaveAction(doDelete, type);
                    doSaveInAction = false;
                }
            });

        }
    }

    public void saveAll(){
        try{
            if (!doSaveInAction){
                doSaveInAction = true;
                mDataRepository.doSaveAction(true, GSONHelper.LOAD_ALL_TYPES);
            }

        }catch(Exception e){
            Log.e(TAG,"save all " + e.getMessage());
        }finally {
            doSaveInAction = false;
        }
    }


    public void setActiveWorkoutSet(WorkoutSet activeSet) {
        if (activeSet != null) {
            Log.w(TAG, "setActiveWorkoutSet set : " + Integer.toString(activeSet.setCount));
            workoutSetMutableLiveData.postValue(activeSet);
            mDataRepository.addActiveSet(activeSet);
        }
    }

    public void setActiveWorkout(Workout activeWorkout) {
        if (activeWorkout != null) {
            Log.w(TAG, "setActiveWorkout " + activeWorkout.activityName);
            workoutMutableLiveData.postValue(activeWorkout);
            mDataRepository.addActiveWorkout(activeWorkout);
        }
    }

    public void setToDoWorkoutSets(ArrayList<WorkoutSet> toDoWorkoutSets) {
        if (toDoWorkoutSets != null) {
            todoSetsListMutableLiveData.postValue(toDoWorkoutSets);
            mDataRepository.addToDoWorkoutSets(toDoWorkoutSets);
        }
    }

    public void setCompletedWorkouts(ArrayList<Workout> completedWorkouts) {
        if (completedWorkouts != null) {
            completedSessionsListMutableLiveData.postValue(completedWorkouts);
            Log.w(TAG, "setCompletedWorkouts " + Integer.toString(completedWorkouts.size()));
            mDataRepository.addCompletedWorkouts(completedWorkouts);
        }

    }

    public void setCompletedWorkoutSets(ArrayList<WorkoutSet> completedSets) {
        if (completedSets != null) {
            completedSetsListMutableLiveData.postValue(completedSets);
            Log.w(TAG, "setCompletedWorkoutSets " + Integer.toString(completedSets.size()));
            mDataRepository.addCompletedWorkoutSets(completedSets);
        }
    }

}