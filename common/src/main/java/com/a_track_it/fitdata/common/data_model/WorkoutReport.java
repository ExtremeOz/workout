package com.a_track_it.fitdata.common.data_model;

import android.content.Context;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by chris.black on 5/1/15.
 *
 * Data model for the fitdata reports.
 * Updated Daniel Haywood 5/19 - added sets and other calculations.
 *
 */
public class WorkoutReport {
    private Map<Long, Workout> map =  new HashMap<>();
    private Map<Long, WorkoutSet> mapSet =  new HashMap<>();
    private Map<String, WorkoutSet> mapSetExercise =  new HashMap<>();
    private Map<Integer, WorkoutSet> mapSetBodypart =  new HashMap<>();
    private Map<Long, UserDailyTotals> mapDailyTotals =  new HashMap<>();
    private String sUserID;
    private Calendar calendar;
    private int iDOY;
    private int iHour;

    public WorkoutReport(){
        calendar = Calendar.getInstance();
        iDOY = calendar.get(Calendar.DAY_OF_YEAR);
        iHour = calendar.get(Calendar.HOUR_OF_DAY);
    }
    public void setUserID(String userID){ sUserID = userID;}

    public void addUserDailyTotal(UserDailyTotals udt){
        calendar.setTimeInMillis(udt._id);
        int inDOY = calendar.get(Calendar.DAY_OF_YEAR);
        int inHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (inDOY < iDOY){
            // only store the midnight per day value
            if  (inHour == 0) {
                if (mapDailyTotals.get(udt._id) == null)
                    mapDailyTotals.put(udt._id, udt);
                else
                    mapDailyTotals.replace(udt._id, udt);
            }
        }else{
            UserDailyTotals existingUDT = null;
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY,0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            // remove and replace the previous hours
           if (mapDailyTotals.get(calendar.getTimeInMillis()) != null)
               existingUDT = mapDailyTotals.get(calendar.getTimeInMillis());
           else{
               for(int i=1; i < iHour; i++){
                   calendar.set(Calendar.HOUR_OF_DAY,i);
                   if (mapDailyTotals.get(calendar.getTimeInMillis()) != null) {
                       existingUDT = mapDailyTotals.get(calendar.getTimeInMillis());
                       break;
                   }
               }
           }
            if (existingUDT != null)
                mapDailyTotals.remove(calendar.getTimeInMillis());
            mapDailyTotals.put(udt._id, udt);
        }
    }
    public void addWorkoutSet(WorkoutSet set){
        if (mapSet.get((long) set._id) == null) mapSet.put(set._id, set);
    }
    public void addWorkout(Workout w) {
        if (map.get((long) w._id) == null) map.put(w._id, w);
    }
    public void addWorkoutData(Context context, Workout workout) {
        // no steps and less than a minute or STILL - get out
        ReferencesTools referencesTools = ReferencesTools.setInstance(context);
        long summaryID= Utilities.getSummaryActivityID(workout.activityID);
        String summaryTitle = Utilities.getSummaryActivityName(context, summaryID);

        if (Utilities.isDetectedActivity(workout.activityID)){
            workout.name = referencesTools.getFitnessActivityTextById(workout.activityID);
            Workout w = map.get(workout.activityID);
            if (w == null) {
                workout.scoreTotal = 1;
                map.put(workout.activityID, workout);
            }else {
                workout.scoreTotal += w.scoreTotal;
                map.replace((long) workout.activityID, workout);
            }
        }else {
            Workout w = map.get(summaryID);
            if (Utilities.isGymWorkout(workout.activityID)) {
                if (w == null) {
                    workout.activityID = summaryID;
                    workout.activityName = summaryTitle;
                    workout.scoreTotal = 1;
                    map.put(summaryID, workout);
                } else {
                    w.stepCount += workout.stepCount;
                    w.duration += workout.duration;
                    w.pause_duration += workout.pause_duration;
                    w.wattsTotal += workout.wattsTotal;
                    w.weightTotal += workout.weightTotal;
                    w.bodypartCount += workout.bodypartCount;
                    w.exerciseCount += workout.exerciseCount;
                    w.setCount += workout.setCount;
                    w.repCount += workout.repCount;
                    w.scoreTotal += 1;
                    map.replace(summaryID, w);
                }
            }
            else {
                if (w == null) {
                    workout.activityID = summaryID;
                    workout.activityName = summaryTitle;
                    workout.scoreTotal = 1;
                    map.put(summaryID, workout);
                } else {
                    w.stepCount += workout.stepCount;
                    w.duration += workout.duration;
                    w.pause_duration += workout.pause_duration;
                    w.scoreTotal += 1;
                    map.replace(summaryID, w);
                }
            }
        }
    }
    public void addAllSets(ArrayList<WorkoutSet> sets){
        for(WorkoutSet s : sets){
            if (mapSet.get(s._id) == null) mapSet.put(s._id, s);
        }
    }
    public void clearWorkoutData() {
        map.clear();
    }
    public void clearWorkoutSetData() {
        mapSet.clear();
    }
    public int getWorkoutSize(){
        ArrayList<Workout> result = new ArrayList<>(map.values());
        return (result != null) ? result.size() : 0;
    }
    public ArrayList<Workout> getWorkoutData() {
        ArrayList<Workout> result = new ArrayList<>(map.values());
        return result;
    }
    public ArrayList<WorkoutSet> getWorkoutSetData() {
        ArrayList<WorkoutSet> result = new ArrayList<>(mapSet.values());
        return result;
    }
    public ArrayList<Workout> getSummaryWorkoutData() {
        Workout summary = new Workout();
        summary.activityID = Constants.WORKOUT_TYPE_TIME;
        summary.duration = getTotalDuration();
        summary.stepCount = getTotalSteps();
        summary.scoreTotal = getTotalSessions();
        summary.start = -1L;
        summary.userID = sUserID;
        ArrayList<Workout> result = new ArrayList<>(map.values());
        Collections.sort(result);
        result.add(0,summary);
        return result;
    }

/*    public void replaceWorkout(Workout workout) {
        if(map.get((long)workout.activityID) == null) {
            map.put((long)workout.activityID, workout);
        }else {
            //Workout w = map.get((long)workout.activityID);
           // if (!w.equals(workout))
            map.replace((long)workout.activityID, workout);

        }
    }*/

    public long getTotalDuration() {
        long totalDuration = 0;
        Set keys = map.keySet();
        for (Object key1 : keys) {
            long key = (long) key1;
            Workout workout = map.get(key);
            if ((workout.activityID != Constants.WORKOUT_TYPE_TIME) && (workout.activityID != Constants.WORKOUT_TYPE_STILL)
                    && (workout.activityID != Constants.WORKOUT_TYPE_INVEHICLE)) {
                totalDuration += workout.duration;
            }
        }
        return totalDuration;
    }
    public long getTotalPauseDuration() {
        long totalDuration = 0;
        Set keys = map.keySet();
        for (Object key1 : keys) {
            long key = (long) key1;
            Workout workout = map.get(key);
            if ((workout.activityID != Constants.WORKOUT_TYPE_TIME) && (workout.activityID != Constants.WORKOUT_TYPE_STILL)
                    && (workout.activityID != Constants.WORKOUT_TYPE_INVEHICLE)) {
                totalDuration += workout.pause_duration;
            }
        }
        return totalDuration;
    }

    public long getTotalRestDuration() {
        long totalDuration = 0;
        Set keys = map.keySet();
        for (Object key1 : keys) {
            long key = (long) key1;
            Workout workout = map.get(key);
            if ((workout.activityID != Constants.WORKOUT_TYPE_TIME) && (workout.activityID != Constants.WORKOUT_TYPE_STILL)
                    && (workout.activityID != Constants.WORKOUT_TYPE_INVEHICLE)) {
                totalDuration += workout.rest_duration;
            }
        }
        return totalDuration;
    }
    public int getTotalSteps() {
        int totalSteps = 0;
        Set keys = map.keySet();
        for (Object key1 : keys) {
            long key = (long) key1;
            Workout workout = map.get(key);
            if ((workout.activityID != Constants.WORKOUT_TYPE_TIME) && (workout.activityID != Constants.WORKOUT_TYPE_STILL)
                    && (workout.activityID != Constants.WORKOUT_TYPE_INVEHICLE)) {
                totalSteps += workout.stepCount;
            }
        }
        return totalSteps;
    }
    public int getTotalSessions() {
        int totalSteps = 0;
        Set keys = map.keySet();
        for (Object key1 : keys) {
            long key = (long) key1;
            Workout workout = map.get(key);
            if ((workout.activityID != Constants.WORKOUT_TYPE_TIME) && !Utilities.isDetectedActivity(workout.activityID)){
               if (workout.scoreTotal > 0) totalSteps += workout.scoreTotal;
            }
        }
        return totalSteps;
    }
    /**
     * Special case for steps. Maybe track estimated steps separately from walking?
     * @param workout
     */
    public void setStepData(Workout workout) {
        if(map.get((long)workout.activityID) == null) {
            map.put((long)workout.activityID, workout);
        }else {
            Workout w = map.get((long)workout.activityID);
            w.stepCount = workout.stepCount;
        }
    }

    public Workout getWorkoutByType(int type) {
        return map.get(type);
    }



}
