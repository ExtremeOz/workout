package com.a_track_it.fitdata.model;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.a_track_it.fitdata.data_model.Bodypart;
import com.a_track_it.fitdata.data_model.Exercise;
import com.a_track_it.fitdata.data_model.FitnessActivity;
import com.a_track_it.fitdata.data_model.Workout;
import com.a_track_it.fitdata.data_model.WorkoutSet;

import java.util.ArrayList;


public class SessionViewModel extends AndroidViewModel{
    private static final String TAG = SessionViewModel.class.getSimpleName();
    private DataRepository mDataRepository;

    private boolean doSaveInAction = false;

    private final MutableLiveData<ArrayList<WorkoutSet>> completedSetsListMutableLiveData;
    private final MutableLiveData<ArrayList<Workout>> completedSessionsListMutableLiveData;
    private final MutableLiveData<ArrayList<WorkoutSet>> todoSetsListMutableLiveData;



    public SessionViewModel(@NonNull Application application) {
        super(application);

        Log.d(TAG, TAG + " created ");
        mDataRepository = DataRepository.getInstance(application);
        completedSessionsListMutableLiveData = mDataRepository.getCompletedSessions();
        completedSetsListMutableLiveData = mDataRepository.getCompletedSets();
        todoSetsListMutableLiveData = mDataRepository.getToDoSets();

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


    public LiveData<ArrayList<Workout>> getCompletedWorkouts(){ return completedSessionsListMutableLiveData;  }
    public LiveData<ArrayList<WorkoutSet>> getCompletedSets(){ return completedSetsListMutableLiveData; }
    public LiveData<ArrayList<WorkoutSet>> getToDoSets(){ return todoSetsListMutableLiveData; }

    public int getToDoSetSize(){ return (todoSetsListMutableLiveData.getValue() == null) ? 0 :todoSetsListMutableLiveData.getValue().size();}
    public int getCompletedSetSize(){ return (completedSetsListMutableLiveData.getValue() == null) ? 0 :completedSetsListMutableLiveData.getValue().size();}
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