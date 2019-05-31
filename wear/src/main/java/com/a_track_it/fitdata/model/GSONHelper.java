package com.a_track_it.fitdata.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.a_track_it.fitdata.activity.IDataLoaderCallback;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.model.Utilities;
import com.a_track_it.fitdata.common.model.Workout;
import com.a_track_it.fitdata.common.model.WorkoutSet;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;


public class GSONHelper {

    private final static String TAG = "GSONHelper";
    private static Context mContext;
    private static IDataLoaderCallback callback;
    private static ArrayList<Workout> CompletedWorkouts = new ArrayList<>();              // load / save type 1
    private static ArrayList<WorkoutSet> CompletedWorkoutSets = new ArrayList<>();  // load type 2
    private static ArrayList<WorkoutSet> ActiveSet = new ArrayList<>(); // load type 3
    private static ArrayList<WorkoutSet> ToDoSets = new ArrayList<>();  // load type 4
    private static ArrayList<Workout> ActiveWorkout = new ArrayList<>();  // load type 5
    private static ArrayList<Workout> GoogleWorkouts = new ArrayList<>(); // load type 6
    public static int LOAD_ALL_TYPES = 0;
    public static int LOAD_COMPLETED_WORKOUTS = 1;
    public static int LOAD_COMPETED_SETS = 2;
    public static int LOAD_ACTIVE_SETS = 3;
    public static int LOAD_TO_DO_SETS = 4;
    public static int LOAD_ACTIVE_WORKOUTS = 5;
    public static int LOAD_GOOGLE_WORKOUTS = 6;
    private static GSONHelper INSTANCE;
    private GSONHelper(){

    }

    public static GSONHelper getInstance(Context context, IDataLoaderCallback loaderCallback)
    {
        if (INSTANCE == null) {
            INSTANCE = new GSONHelper();
            if (context != null) mContext = context;
            if (loaderCallback != null) callback = loaderCallback;
        }
        return INSTANCE;
    }
    public void setContext(Context c){
        mContext = c;
    }
    public Context getContext(){
        return mContext;
    }

    public boolean open(int loadType) {
     //   synchronized(this) {
            Log.d(TAG, "asking for opening");
            try {
                AsyncLoadWorkouts myLoader = new AsyncLoadWorkouts(loadType);
                myLoader.execute();
                return true;
            }catch(Exception e){
                Log.e(TAG, "loader error " + e.getMessage());
                return  false;
            }
     //   } // end of synchronised
    }
    // saveFlag
    public void save(boolean deleteExisting, int saveFlag){
     //   synchronized(this) {
            try {
                AsyncSaveWorkout mySaver = new AsyncSaveWorkout(saveFlag, deleteExisting);
                mySaver.execute();
            }catch(Exception e){
                Log.d(TAG, "error save " + e.getMessage());

            }
            return;
     //   } // end of synchronised
    }

    public void deleteAll(int i){
        if (((i==LOAD_ALL_TYPES)||(i== LOAD_COMPLETED_WORKOUTS)) && Utilities.fileExist(mContext, Constants.COMPLETED_WORKOUTS)){
            Utilities.deleteFile(mContext, Constants.COMPLETED_WORKOUTS);
            CompletedWorkouts.clear();
        }
        if (((i==LOAD_ALL_TYPES)||(i== LOAD_COMPETED_SETS)) && Utilities.fileExist(mContext, Constants.COMPLETED_SETS_HISTORY)){
            Utilities.deleteFile(mContext, Constants.COMPLETED_SETS_HISTORY);
            CompletedWorkoutSets.clear();
        }
        if (((i==LOAD_ALL_TYPES)||(i== LOAD_ACTIVE_SETS)) && Utilities.fileExist(mContext, Constants.ACTIVE_WORKOUT_SET)){
            Utilities.deleteFile(mContext, Constants.ACTIVE_WORKOUT_SET);
            ActiveSet.clear();
        }
        if (((i==LOAD_ALL_TYPES)||(i== LOAD_TO_DO_SETS)) && Utilities.fileExist(mContext, Constants.ACTIVE_TODO_SETS)){
            Utilities.deleteFile(mContext, Constants.ACTIVE_TODO_SETS);
            ToDoSets.clear();
        }
        if (((i==LOAD_ALL_TYPES)||(i== LOAD_ACTIVE_WORKOUTS)) && Utilities.fileExist(mContext, Constants.ACTIVE_WORKOUT)){
            Utilities.deleteFile(mContext, Constants.ACTIVE_WORKOUT);
            ActiveWorkout.clear();
        }
        if (((i==LOAD_ALL_TYPES)||(i== LOAD_GOOGLE_WORKOUTS)) && Utilities.fileExist(mContext, Constants.HISTORY_WORKOUTS)) Utilities.deleteFile(mContext, Constants.HISTORY_WORKOUTS);
        if (callback != null) callback.actionCompleted(true, i, 3);
    }

    public int getLoadedSize(int loadType){
        ArrayList<Workout> targetList = new ArrayList<>();
        if (loadType == LOAD_ALL_TYPES)
            Log.i(TAG, "find all workout sizes");
        else
        if (loadType == LOAD_COMPLETED_WORKOUTS)
            targetList = CompletedWorkouts;
        else
        if (loadType == LOAD_ACTIVE_WORKOUTS)
            targetList = ActiveWorkout;
        else
        if (loadType == LOAD_GOOGLE_WORKOUTS)
            targetList = GoogleWorkouts;


        if (targetList != null)
            return targetList.size();
        else
            return 0;
    }

    public Workout getWorkoutById(int loadType, long id){
        ArrayList<Workout> targetList = new ArrayList<>();
        if (loadType == LOAD_ALL_TYPES)
            Log.i(TAG, "find all workout by id " + Long.toString(id));
        else
            if (loadType == LOAD_COMPLETED_WORKOUTS)
                targetList = CompletedWorkouts;
            else
                if (loadType == LOAD_ACTIVE_WORKOUTS)
                    targetList = ActiveWorkout;
                else
                    if (loadType == LOAD_GOOGLE_WORKOUTS)
                        targetList = GoogleWorkouts;


        if ((targetList != null) && (targetList.size() > 0)){
            for (Workout workout : targetList){
                if (workout._id == id)
                    return workout;
            }
        }
        return null;
    }

    public void addCompletedWorkout(Workout workout){
        if ((CompletedWorkouts != null) && (CompletedWorkouts.size() > 0)){
            int i = 0;
            for (Workout old : CompletedWorkouts){
                if (old._id == workout._id){
                    CompletedWorkouts.set(i, workout);
                    return;
                }
                i++;
            }
        }
        CompletedWorkouts.add(workout);
    }

    public Workout getCompletedWorkout(int index){
        return CompletedWorkouts.get(index);
    }

    public int getCompletedWorkoutsSize(){
        return (CompletedWorkouts != null) ? CompletedWorkouts.size() : 0;
    }

    public void addActiveWorkoutSet(WorkoutSet set){
        // only 1 active set or workout at once!
        if ((ActiveSet != null) && (ActiveSet.size() > 0)){
            ActiveSet.clear();
        }
        ActiveSet.add(set);
    }
    public void addActiveWorkout(Workout workout){
        if ((ActiveWorkout != null) && (ActiveWorkout.size() > 0)){
            ActiveWorkout.clear();
        }
        ActiveWorkout.add(workout);
    }
    public WorkoutSet getActiveWorkoutSet(int index){
        return ActiveSet.get(index);
    }

    public int getActiveWorkoutSetSize(){
        return (ActiveSet != null) ? ActiveSet.size() : 0;
    }

    public void addCompletedWorkoutSets(ArrayList<WorkoutSet> sets){
        if ((CompletedWorkoutSets == null) || (CompletedWorkoutSets.size() == 0))
            CompletedWorkoutSets = sets;
        else
            for (WorkoutSet insertSet : sets){ // only add new workouts
                int i = 0; boolean found = false;
                for (WorkoutSet completedSet: CompletedWorkoutSets){
                    if (completedSet._id == insertSet._id){
                        CompletedWorkoutSets.set(i, insertSet);
                        found = true;
                        break;
                    }
                    i++;
                }
                if (!found)
                    CompletedWorkoutSets.add(insertSet);
            }
    }
    public void addCompletedWorkouts(ArrayList<Workout> routines){
        if ((CompletedWorkouts == null) || (CompletedWorkouts.size() == 0))
            CompletedWorkouts = routines;
        else
            for (Workout routine : routines){ // only add new workouts
                boolean found = false; int i = 0;
                for (Workout completed: CompletedWorkouts){
                    if (completed._id == routine._id){
                        CompletedWorkouts.set(i,routine);
                        found = true;
                        break;
                    }
                    i++;
                }
                if (!found)
                    CompletedWorkouts.add(routine);
            }


    }

    public void addGoogleWorkout(Workout routine){
        if ((GoogleWorkouts != null) && (GoogleWorkouts.size() > 0)){
            int i = 0;
            for (Workout old : GoogleWorkouts){
                if (old._id == routine._id){
                    GoogleWorkouts.set(i, routine);
                    return;
                }
                i++;
            }
        }
        GoogleWorkouts.add(routine);
    }

    public void addToDoSets(ArrayList<WorkoutSet> sets){
        if ((ToDoSets == null) || (ToDoSets.size() == 0))
            ToDoSets = sets;
        else
            for (WorkoutSet insertSet : sets){ // only add new workouts
                int i = 0; boolean found = false;
                for (WorkoutSet completedSet: ToDoSets){
                    if (completedSet._id == insertSet._id){
                        ToDoSets.set(i, insertSet);
                        found = true;
                        break;
                    }
                    i++;
                }
                if (!found)
                    ToDoSets.add(insertSet);
            }
    }
    public ArrayList<WorkoutSet> getCompletedWorkoutSets(){
        return CompletedWorkoutSets;
    }
    public ArrayList<Workout> getGoogleWorkouts(){
        return GoogleWorkouts;
    }
    public int getCompletedWorkoutSetsSize(){

        return  (CompletedWorkoutSets != null) ? CompletedWorkoutSets.size() : 0;
    }
    public void close() {
        synchronized(this) {
           // Log.d(TAG, "asking for closing");
            if (mContext != null) {
                mContext = null;
            }
        }
    }

    private class AsyncLoadWorkouts extends AsyncTask<Void, Void, Integer>{
        int loadType;
        public AsyncLoadWorkouts(int type){
            loadType = type;
        }
        @Override
        protected Integer doInBackground(Void... params) {
            int result = 0;
            Gson gson = new Gson();
            if ((loadType == LOAD_ALL_TYPES) || (loadType == LOAD_COMPLETED_WORKOUTS)) {
                if (Utilities.fileExist(mContext, Constants.COMPLETED_WORKOUTS)) {
                    String sJSON = GetFromLocalFile(Constants.COMPLETED_WORKOUTS);
                    if (sJSON.length() > 0) {
                        if (sJSON.substring(0,1).equals("[")) {
                            Workout[] workout_array = gson.fromJson(sJSON, Workout[].class);
                            CompletedWorkouts = new ArrayList<>(Arrays.asList(workout_array));
                        }else{
                            Workout w = gson.fromJson(sJSON, Workout.class);
                            CompletedWorkouts.add(w);
                        }
                        if (CompletedWorkouts != null) Log.i(TAG, "loaded workout " + Integer.toString(CompletedWorkouts.size()));
                        if (callback != null) callback.setCompletedWorkouts (CompletedWorkouts);
                        if (CompletedWorkouts != null) result = 1;
                    }
                } else
                    result = 1;
            }
            if ((loadType == LOAD_ALL_TYPES) || (loadType == LOAD_COMPETED_SETS)) {
                if (Utilities.fileExist(mContext, Constants.COMPLETED_SETS_HISTORY)) {
                    String sJSON = GetFromLocalFile(Constants.COMPLETED_SETS_HISTORY);
                    if (sJSON.length() > 0) {
                        if (sJSON.substring(0,1).equals("[")) {
                            WorkoutSet[] set_array = gson.fromJson(sJSON, WorkoutSet[].class);
                            CompletedWorkoutSets = new ArrayList<>(Arrays.asList(set_array));
                        }else{
                            WorkoutSet set = gson.fromJson(sJSON, WorkoutSet.class);
                            CompletedWorkoutSets = new ArrayList<>();
                            CompletedWorkoutSets.add(set);
                        }
                        if (CompletedWorkoutSets != null) Log.i(TAG, "loaded workout sets " + Integer.toString(CompletedWorkoutSets.size()));
                        if (callback != null) callback.setCompletedWorkoutSets(CompletedWorkoutSets);
                        if (CompletedWorkoutSets != null) result++;
                    }
                }
            }
            if ((loadType == LOAD_ALL_TYPES) || (loadType == LOAD_ACTIVE_SETS)) {
                if (Utilities.fileExist(mContext, Constants.ACTIVE_WORKOUT_SET)) {
                    String sJSON = GetFromLocalFile(Constants.ACTIVE_WORKOUT_SET);
                    if (sJSON.length() > 0) {
                        if (sJSON.substring(0,1).equals("[")) {
                            WorkoutSet[] set_array = gson.fromJson(sJSON, WorkoutSet[].class);
                            ActiveSet = new ArrayList<>(Arrays.asList(set_array));
                            if (ActiveSet != null)
                                Log.i(TAG, "loaded active workout set " + Integer.toString(ActiveSet.size()));
                            if ((ActiveSet != null) && (ActiveSet.size() > 0)) {
                                if (callback != null) callback.setActiveWorkoutSet(ActiveSet.get(ActiveSet.size() - 1));
                                result++;
                            } else
                            if (callback != null) callback.setActiveWorkoutSet(null);
                        }else{
                            WorkoutSet set = gson.fromJson(sJSON, WorkoutSet.class);
                            ActiveSet = new ArrayList<>();
                            ActiveSet.add(set);
                            if (callback != null) callback.setActiveWorkoutSet(set);
                        }

                    }
                }
            }
            if (loadType == LOAD_TO_DO_SETS) {
                if (Utilities.fileExist(mContext, Constants.ACTIVE_TODO_SETS)) {
                    String sJSON = GetFromLocalFile(Constants.ACTIVE_TODO_SETS);
                    if (sJSON.length() > 0) {
                        if (sJSON.substring(0,1).equals("[")) {
                            WorkoutSet[] todoset_array = gson.fromJson(sJSON, WorkoutSet[].class);
                            ToDoSets = new ArrayList<>(Arrays.asList(todoset_array));
                            if (ToDoSets != null)
                                Log.i(TAG, "loaded to do sets " + Integer.toString(ToDoSets.size()));
                            if (callback != null) callback.setToDoWorkoutSets(ToDoSets);
                            if (ToDoSets != null) result++;
                        }else{
                            WorkoutSet set = gson.fromJson(sJSON, WorkoutSet.class);
                            ToDoSets = new ArrayList<>();
                            ToDoSets.add(set);
                            if (callback != null) callback.setToDoWorkoutSets(ToDoSets);
                        }
                    }
                }
            }
            if ((loadType == LOAD_ALL_TYPES) || (loadType == LOAD_ACTIVE_WORKOUTS)) {
                if (Utilities.fileExist(mContext, Constants.ACTIVE_WORKOUT)) {
                    String sJSON = GetFromLocalFile(Constants.ACTIVE_WORKOUT);
                    if (sJSON.length() > 0) {
                        if (sJSON.substring(0,1).equals("[")) {
                            Workout[] workout_array = gson.fromJson(sJSON, Workout[].class);
                            ActiveWorkout = new ArrayList<>(Arrays.asList(workout_array));
                            if (ActiveWorkout != null)
                                Log.i(TAG, "loaded active workouts " + Integer.toString(ActiveWorkout.size()));
                        }else{
                            Workout w = gson.fromJson(sJSON, Workout.class);
                            ActiveWorkout = new ArrayList<>();
                            ActiveWorkout.add(w);
                        }
                        if ((ActiveWorkout != null) && (ActiveWorkout.size() > 0)) {
                            if (callback != null) callback.setActiveWorkout(ActiveWorkout.get(ActiveWorkout.size() - 1));
                            result++;
                        } else
                            callback.setActiveWorkout(null);
                    }
                }
            }
            if ((loadType == LOAD_ALL_TYPES) || (loadType == LOAD_GOOGLE_WORKOUTS)) {
                if (Utilities.fileExist(mContext, Constants.HISTORY_WORKOUTS)) {
                    String sJSON = GetFromLocalFile(Constants.HISTORY_WORKOUTS);
                    if (sJSON.length() > 0) {
                        if (sJSON.substring(0,1).equals("[")) {
                            Workout[] workout_array = gson.fromJson(sJSON, Workout[].class);
                            GoogleWorkouts = new ArrayList<>(Arrays.asList(workout_array));
                        }else{
                            Workout w = gson.fromJson(sJSON,Workout.class);
                            GoogleWorkouts = new ArrayList<>();
                            GoogleWorkouts.add(w);
                        }
                        if ((GoogleWorkouts != null) && (GoogleWorkouts.size() > 0)){
                            Log.i(TAG, "loaded google workouts " + Integer.toString(GoogleWorkouts.size()));
                            //callback.setCompletedWorkouts(HistoryWorkout);
                            result++;
                        }

                    }
                }
            }
            if (callback != null) callback.actionCompleted((result > 0), loadType, 1);
            return result;
        }
    }

    private class AsyncSaveWorkout extends AsyncTask<Void, Void, Integer>{
        int flagWhich;
        boolean deleteExisting;
        public AsyncSaveWorkout(int which, Boolean delete){
            flagWhich = which;
            deleteExisting = delete;
        }
        @Override
        protected Integer doInBackground(Void... params) {
            int result = 0;
            Gson gson = new Gson();
            if ((flagWhich == LOAD_ALL_TYPES) || (flagWhich == LOAD_COMPLETED_WORKOUTS)) {
                if ((deleteExisting) && (Utilities.fileExist(mContext, Constants.COMPLETED_WORKOUTS))) {
                    result = Utilities.deleteFile(mContext, Constants.COMPLETED_WORKOUTS) ? 1 : 0;
                }
                if ((CompletedWorkouts != null) && (CompletedWorkouts.size() > 0))  {
                    String sJSONWorkout = gson.toJson(CompletedWorkouts);
/*                    sJSONWorkout = (CompletedWorkouts.size() > 0) ? "[" : "";
                    for (Workout w : CompletedWorkouts){
                        sJSONWorkout += gson.toJson(w);
                        sJSONWorkout += (CompletedWorkouts.size() > 1) ? "," : "";
                    }
                    if (CompletedWorkouts.size() > 0){
                        if (sJSONWorkout.endsWith(","))  sJSONWorkout = sJSONWorkout.substring(1,sJSONWorkout.length()-1);
                        sJSONWorkout += "]";
                    }*/
                    if (sJSONWorkout.length() > 0) {
                        SaveToLocalFile(Constants.COMPLETED_WORKOUTS, sJSONWorkout);
                        Log.i(TAG, "CompletedWorkouts size " + Integer.toString(CompletedWorkouts.size()));
                        result = 1;
                    }
                    if ((callback != null) && (flagWhich != LOAD_ALL_TYPES)) callback.actionCompleted((result > 0), flagWhich, 2);
                }
            }
            if ((flagWhich == LOAD_ALL_TYPES) || (flagWhich == LOAD_COMPETED_SETS)) {
                if (deleteExisting && (Utilities.fileExist(mContext, Constants.COMPLETED_SETS_HISTORY)))
                    result = Utilities.deleteFile(mContext, Constants.COMPLETED_SETS_HISTORY) ? result + 1 : result;
                if ((CompletedWorkoutSets != null) && (CompletedWorkoutSets.size() > 0)) {
                    String sJSONSets = gson.toJson(CompletedWorkoutSets);
/*                    sJSONSets = (CompletedWorkoutSets.size() > 0) ? "[" : "";
                    for (WorkoutSet workoutSet : CompletedWorkoutSets) {
                        sJSONSets += gson.toJson(workoutSet);
                        sJSONSets += (CompletedWorkoutSets.size() > 1) ? "," : "";
                    }
                    if (CompletedWorkoutSets.size() > 0) {
                        if (sJSONSets.endsWith(","))  sJSONSets = sJSONSets.substring(1,sJSONSets.length()-1);
                        sJSONSets += "]";
                    }*/
                    if (sJSONSets.length() > 0) {
                        SaveToLocalFile(Constants.COMPLETED_SETS_HISTORY, sJSONSets);
                    }
                    if ((callback != null) && (flagWhich != LOAD_ALL_TYPES)) callback.actionCompleted((result > 0), flagWhich, 2);
                    Log.i(TAG, "saved completed sets size " + Integer.toString(CompletedWorkoutSets.size()));
                    result++;
                }
            }
            if ((flagWhich == LOAD_ALL_TYPES) || (flagWhich == LOAD_ACTIVE_SETS)) {
                if (deleteExisting && (Utilities.fileExist(mContext, Constants.ACTIVE_WORKOUT_SET)))
                    result = Utilities.deleteFile(mContext, Constants.ACTIVE_WORKOUT_SET) ? result + 1 : result;
                if ((ActiveSet != null) && (ActiveSet.size() > 0)){
                    String sJSONWorkoutSet = gson.toJson(ActiveSet);
/*                    sJSONWorkoutSet = (ActiveSet.size() > 0) ? "[" : "";
                    for (WorkoutSet workoutSet : ActiveSet) {
                        sJSONWorkoutSet += gson.toJson(workoutSet);
                        sJSONWorkoutSet += (ActiveSet.size() > 1) ? "," : "";
                    }
                    if (ActiveSet.size() > 0){
                        if (sJSONWorkoutSet.endsWith(","))  sJSONWorkoutSet = sJSONWorkoutSet.substring(1,sJSONWorkoutSet.length()-1);
                        sJSONWorkoutSet += "]";
                    }*/
                    if (sJSONWorkoutSet.length() > 0) {
                        SaveToLocalFile(Constants.ACTIVE_WORKOUT_SET, sJSONWorkoutSet);
                        if ((callback != null) && (flagWhich != LOAD_ALL_TYPES)) callback.actionCompleted((result > 0), flagWhich, 2);
                        Log.i(TAG, "saved active workout set size " + Integer.toString(ActiveSet.size()));
                        result = 1;
                    }
                }
            }
            if ((flagWhich == LOAD_ALL_TYPES) || (flagWhich == LOAD_TO_DO_SETS)) {
                if (deleteExisting && Utilities.fileExist(mContext, Constants.ACTIVE_TODO_SETS)) result = Utilities.deleteFile(mContext, Constants.ACTIVE_TODO_SETS) ? result + 1 : result;
                if ((ToDoSets != null) && (ToDoSets.size() > 0)) {
                    String sJSONSets = gson.toJson(ToDoSets);
/*                sJSONSets = (ToDoSets.size() > 0) ? "[" : "";
                for (WorkoutSet workoutSet : ToDoSets) {
                    sJSONSets += gson.toJson(workoutSet);
                    sJSONSets += (ToDoSets.size() > 1) ? "," : "";
                }
                if (ToDoSets.size() > 0) {
                    if (sJSONSets.endsWith(","))  sJSONSets = sJSONSets.substring(1,sJSONSets.length()-1);
                    sJSONSets += "]";
                }*/
                    if (sJSONSets.length() > 0) {
                        SaveToLocalFile(Constants.ACTIVE_TODO_SETS, sJSONSets);
                        if ((callback != null) && (flagWhich != LOAD_ALL_TYPES))
                            callback.actionCompleted((result > 0), flagWhich, 2);
                        Log.i(TAG, "saved todo sets size " + Integer.toString(ToDoSets.size()));
                        result++;
                    }
                }
            }
            if ((flagWhich == LOAD_ALL_TYPES) || (flagWhich == LOAD_ACTIVE_WORKOUTS)) {
                if (deleteExisting && Utilities.fileExist(mContext, Constants.ACTIVE_WORKOUT)) result = Utilities.deleteFile(mContext, Constants.ACTIVE_WORKOUT) ? result + 1 : result;
                if ((ActiveWorkout != null) && (ActiveWorkout.size() > 0)) {
                    String sJSONWorkout = gson.toJson(ActiveWorkout);
/*                    sJSONWorkout = (ActiveWorkout.size() > 0) ? "[" : "";
                    for (Workout w : ActiveWorkout) {
                        sJSONWorkout += gson.toJson(w);
                        sJSONWorkout += (ActiveWorkout.size() > 1) ? "," : "";
                    }
                    if (ActiveWorkout.size() > 0) {
                        if (sJSONWorkout.endsWith(","))
                            sJSONWorkout = sJSONWorkout.substring(1, sJSONWorkout.length() - 1);
                        sJSONWorkout += "]";
                    }*/
                    if (sJSONWorkout.length() > 0) {
                        SaveToLocalFile(Constants.ACTIVE_WORKOUT, sJSONWorkout);
                        if ((callback != null) && (flagWhich != LOAD_ALL_TYPES))
                            callback.actionCompleted((result > 0), flagWhich, 2);
                        Log.i(TAG, "saved active workout size " + Integer.toString(ActiveWorkout.size()));
                        result++;
                    }
                }
            }
            if ((flagWhich == LOAD_ALL_TYPES) || (flagWhich == LOAD_GOOGLE_WORKOUTS)) {
                if (deleteExisting && Utilities.fileExist(mContext, Constants.HISTORY_WORKOUTS)) result = Utilities.deleteFile(mContext, Constants.HISTORY_WORKOUTS) ? result + 1 : result;
                if ((GoogleWorkouts != null) && (GoogleWorkouts.size() > 0)) {
                    String sJSONWorkout = gson.toJson(GoogleWorkouts);
/*                    sJSONWorkout = (GoogleWorkouts.size() > 0) ? "[" : "";
                    for (Workout w : GoogleWorkouts) {
                        sJSONWorkout += gson.toJson(w);
                        sJSONWorkout += (GoogleWorkouts.size() > 1) ? "," : "";
                    }
                    if (GoogleWorkouts.size() > 0) {
                        if (sJSONWorkout.endsWith(","))
                            sJSONWorkout = sJSONWorkout.substring(1, sJSONWorkout.length() - 1);
                        sJSONWorkout += "]";
                    }*/
                    if (sJSONWorkout.length() > 0) {
                        SaveToLocalFile(Constants.HISTORY_WORKOUTS, sJSONWorkout);
                        if ((callback != null) && (flagWhich != LOAD_ALL_TYPES))
                            callback.actionCompleted((result > 0), flagWhich, 2);
                        Log.i(TAG, "saved active workout size " + Integer.toString(GoogleWorkouts.size()));
                        result++;
                    }
                }
            }
            if ((callback != null) && (flagWhich == LOAD_ALL_TYPES)) callback.actionCompleted((result > 0), flagWhich, 2);
            return result;
        }
    }


    private void SaveToLocalFile(String filename, String sJSON){
        FileOutputStream outputStream;

        try {
            outputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(sJSON.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String GetFromLocalFile(String filename){
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        FileInputStream inputStream;
        String result = "";
        try {
            inputStream = mContext.openFileInput(filename);
            Reader in = new InputStreamReader(inputStream, "UTF-8");
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            result = out.toString();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            return result;
        }

    }
}