package com.a_track_it.fitdata.model;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.a_track_it.fitdata.activity.IDataLoaderCallback;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.model.Bodypart;
import com.a_track_it.fitdata.common.model.Exercise;
import com.a_track_it.fitdata.common.model.FitnessActivity;
import com.a_track_it.fitdata.common.model.Utilities;
import com.a_track_it.fitdata.common.model.Workout;
import com.a_track_it.fitdata.common.model.WorkoutSet;
import com.a_track_it.fitdata.service.AsyncLoadReferencesTask;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


public class DataRepository implements IDataLoaderCallback {
    public final static String TAG = "DataRepository";
    private static DataRepository sInstance;
    private final Application mApp;
    private static Context mContext;
    private final GSONHelper mGSONHelper;
    private ArrayList<FitnessActivity> fitnessActivityArrayList = new ArrayList<>();
    private ArrayList<Exercise> exerciseArrayList = new ArrayList<>();
    private ArrayList<Bodypart> bodypartArrayList = new ArrayList<>();

    private final MutableLiveData<Workout> mutableWorkout;
    private final MutableLiveData<WorkoutSet> mutableWorkoutSet;
    private final MutableLiveData<ArrayList<Workout>> mutableSessionHistory;
    private final MutableLiveData<ArrayList<WorkoutSet>> mutableSetHistory;
    private final MutableLiveData<ArrayList<WorkoutSet>> mutableToDoSets;
    private boolean mLoadRefsInProgress = false;
    private boolean mReferencesAreLoaded = false;
    private MutableLiveData<Boolean> mDoActionInProgress;

    private DataRepository(final Application app) {
        mApp = app;
        mContext = app.getApplicationContext();
        mGSONHelper = GSONHelper.getInstance(mContext, this);

        mutableWorkout = new MutableLiveData<>();
        mutableWorkoutSet = new MutableLiveData<>();
        mutableSessionHistory = new MutableLiveData<>();
        mutableSetHistory = new MutableLiveData<>();
        mutableToDoSets = new MutableLiveData<>();
        mDoActionInProgress = new MutableLiveData<>();
        mDoActionInProgress.postValue(false);
    }

    public static DataRepository getInstance(final Application application) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(application);
                }
            }
        }
        return sInstance;
    }
    @Override
    public void setExerciseList(ArrayList<Exercise> exerciseList) {
        if ((exerciseArrayList == null) || (exerciseArrayList.size() == 0))
            exerciseArrayList = exerciseList;
    }

    @Override
    public void setActivityList(ArrayList<FitnessActivity> activityList) {
        if ((fitnessActivityArrayList == null) || (fitnessActivityArrayList.size() == 0))
            fitnessActivityArrayList = activityList;
    }

    @Override
    public void setBodypartList(ArrayList<Bodypart> bodypartList) {
        bodypartArrayList = bodypartList;
    }

    public void loadReferences(){
        if ((Utilities.fileExist(mContext, Constants.BODYPART_FILENAME)) &&
                ((fitnessActivityArrayList == null) || (fitnessActivityArrayList.size() == 0))) {
            mReferencesAreLoaded = false;
            if (!mLoadRefsInProgress) {
                mLoadRefsInProgress = true;
                AsyncLoadReferencesTask myLoadRefsAsyncTask = new AsyncLoadReferencesTask(this, mContext);
                myLoadRefsAsyncTask.execute();

            }
        }
    }
    public boolean getLoadRefsInProgress(){
        return  mLoadRefsInProgress;
    }

    public void addActiveWorkout(Workout activeWorkout){
        mGSONHelper.addActiveWorkout(activeWorkout);
    }

    public void addActiveSet(WorkoutSet activeSet){
        mGSONHelper.addActiveWorkoutSet(activeSet);
    }
    public void addToDoWorkoutSets(ArrayList<WorkoutSet> toDoSets){
        mGSONHelper.addToDoSets(toDoSets);
    }
    public void addCompletedWorkouts(ArrayList<Workout> completedWorkouts) {
        mGSONHelper.addCompletedWorkouts(completedWorkouts);
    }
    public void addCompletedWorkoutSets(ArrayList<WorkoutSet> completedSets){
        mGSONHelper.addCompletedWorkoutSets(completedSets);
    }
    public boolean areReferencesLoaded(){ return mReferencesAreLoaded;}
    /*
        GSONHelper interface functions
        set functions are load callbacks
        actionActionCompleted = save or deletes
     */
    @Override
    public void setActiveWorkout(Workout activeWorkout) {
        mutableWorkout.postValue(activeWorkout);
    }
    @Override
    public void setActiveWorkoutSet(WorkoutSet activeSet) {
        mutableWorkoutSet.postValue(activeSet);
    }
    @Override
    public void setToDoWorkoutSets(ArrayList<WorkoutSet> toDoWorkoutSets) {
        mutableToDoSets.postValue(toDoWorkoutSets);
    }
    @Override
    public void setCompletedWorkouts(ArrayList<Workout> completedWorkouts) {
       mutableSessionHistory.postValue(completedWorkouts);
    }
    @Override
    public void setCompletedWorkoutSets(ArrayList<WorkoutSet> completedSets) {
        mutableSetHistory.postValue(completedSets);
    }
    @Override
    public void actionCompleted(boolean saved, int index, int type) {
        Log.i(TAG, "actionCompleted index " + Integer.toString(index) + " type " + Integer.toString(type));

        mDoActionInProgress.postValue(false);
        if (type == 1) {
            if (index == 0)
            Log.i(TAG, "load ALL completed " + Boolean.toString(saved));
            else
            Log.i(TAG, "load completed " + Boolean.toString(saved));
        }
        if (type == 2) {
            Log.i(TAG, "save completed " + Boolean.toString(saved));

        }
        if (type == 3)
            Log.i(TAG, "delete completed " + Boolean.toString(saved));

        if (type == 4){
            Log.i(TAG, "load references completed " + Boolean.toString(saved));
            mReferencesAreLoaded = true;
           /* if (Utilities.fileExist(mContext, Utilities.ACTIVE_TODO_SETS) || Utilities.fileExist(mContext, Utilities.COMPLETED_SETS_HISTORY) || Utilities.fileExist(mContext, Utilities.COMPLETED_WORKOUTS)){
                Log.d(TAG, "doGetAction called file exists!");
                doGetAction();  // restore all saved json files
            }*/
        }
    }


    // GETTERS
    public LiveData<Boolean> getActionInProgress(){
        return mDoActionInProgress;
    }

    public ArrayList<FitnessActivity> getFitnessActivityArrayList(){
        return fitnessActivityArrayList;
    }
    public ArrayList<Exercise> getExerciseArrayList(){
        return exerciseArrayList;
    }
    public ArrayList<Bodypart> getBodypartArrayList(){
        return bodypartArrayList;
    }
    public MutableLiveData<Workout> getWorkout(){ return mutableWorkout;   }
    public MutableLiveData<WorkoutSet> getWorkoutSet(){
        return mutableWorkoutSet;
    }
    public MutableLiveData<ArrayList<Workout>> getCompletedSessions(){
        return mutableSessionHistory;
    }

    public int getCompletedSessionsSize(){
            if (mutableSessionHistory != null){
               if (mutableSessionHistory.getValue() != null)
                   return mutableSessionHistory.getValue().size();
            }
            return 0;
    }
    public MutableLiveData<ArrayList<WorkoutSet>> getCompletedSets(){
        return mutableSetHistory;
    }
    public int getCompletedSetsSize(){
        if (mutableSetHistory != null) {
            if (mutableSetHistory.getValue() != null)
                return mutableSetHistory.getValue().size();
        }
        return 0;

    }

    public MutableLiveData<ArrayList<WorkoutSet>> getToDoSets(){
        return mutableToDoSets;
    }

    public int getToDoSetsSize(){
        return (mutableToDoSets != null && (mutableToDoSets.getValue() != null)) ? mutableToDoSets.getValue().size() : 0;
    }

    public void doSaveAction(final boolean delete,final int saveFlag){
        try {
            Log.d(TAG, "doSaveAction " + Integer.toString(saveFlag) + " del " + Boolean.toString(delete));
            boolean inProgress = mDoActionInProgress.getValue();
            if (!inProgress) {
                mDoActionInProgress.setValue(true);
                if (mContext != null)
                    mGSONHelper.setContext(mContext);
                else{
                    mGSONHelper.setContext(mApp.getApplicationContext());
                }
                mGSONHelper.save(delete, saveFlag);
            }else{
                Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mDoActionInProgress.getValue()) {
                            mDoActionInProgress.postValue(true);
                            if (mContext != null)
                                mGSONHelper.setContext(mContext);
                            else{
                                mGSONHelper.setContext(mApp.getApplicationContext());
                            }
                            mGSONHelper.save(delete, saveFlag);
                        }
                    }
                },500);
            }
        }catch(Exception e){
            Log.e(TAG,"doSave Action error " + e.getMessage());
        }
    }

    public void doGetAction(){
        try {
            Log.d(TAG, "doGetAction ALL");
            boolean inProgress = mDoActionInProgress.getValue();
            if (!inProgress) {
                mDoActionInProgress.setValue(true);
                if (mContext != null)
                    mGSONHelper.setContext(mContext);
                else{
                    mGSONHelper.setContext(mApp.getApplicationContext());
                }
                mGSONHelper.open(mGSONHelper.LOAD_ALL_TYPES);
            }
        }catch (Exception e){
            Log.e(TAG, "GSONHelper doGetAction Error " + e.getMessage());
        }

    }
}
