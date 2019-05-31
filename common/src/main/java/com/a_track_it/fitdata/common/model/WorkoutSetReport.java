package com.a_track_it.fitdata.common.model;
import com.a_track_it.fitdata.common.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by chris.black on 5/1/15.
 *
 * Data model for the fitdata set reports.
 * Updated Daniel Haywood 5/19 - added sets and other calculations.
 *
 */
public class WorkoutSetReport {
    private Map<Integer, WorkoutSet> map =  new HashMap<>();

    public void addWorkoutSetData(WorkoutSet workoutSet) {
        // no steps and less than a minute or STILL - get out
        if((workoutSet.activityID == Constants.WORKOUT_TYPE_STILL) || (workoutSet.stepCount == 0 && workoutSet.duration < 60000)) {
            // Ignore "still" time or workoutSets less than 1 minute.
            return;
        }

        if(map.get(workoutSet.activityID) == null) {
            map.put(workoutSet.activityID, workoutSet);
        }else {
            WorkoutSet w = map.get(workoutSet.activityID);
            w.stepCount += workoutSet.stepCount;
            w.duration += workoutSet.duration;
            w.rest_duration += workoutSet.rest_duration;
            w.pause_duration += workoutSet.pause_duration;
            w.wattsTotal += workoutSet.wattsTotal;
            w.weightTotal += workoutSet.weightTotal;
            w.setCount += workoutSet.setCount;
            w.repCount += workoutSet.repCount;
        }
    }

    public void clearWorkoutSetData() {
        map.clear();
    }

    public ArrayList<WorkoutSet> getWorkoutSetData() {
        WorkoutSet summary = new WorkoutSet();
        summary.activityID = Constants.WORKOUT_TYPE_TIME;
        summary.duration = getTotalDuration();
        summary.start = -1;
        replaceWorkoutSet(summary);
        ArrayList<WorkoutSet> result = new ArrayList<>(map.values());
        Collections.sort(result);
        return result;
    }

    public void replaceWorkoutSet(WorkoutSet workoutSet) {
        if(map.get(workoutSet.activityID) == null) {
            map.put(workoutSet.activityID, workoutSet);
        }else {
            WorkoutSet w = map.get(workoutSet.activityID);
            w = workoutSet;
        }
    }

    public long getTotalDuration() {
        long totalDuration = 0;
        Set keys = map.keySet();
        for (Object key1 : keys) {
            int key = (int) key1;
            WorkoutSet workoutSet = map.get(key);
            if ((workoutSet.activityID != Constants.WORKOUT_TYPE_TIME) && (workoutSet.activityID != Constants.WORKOUT_TYPE_STILL)
                    && (workoutSet.activityID != Constants.WORKOUT_TYPE_INVEHICLE)) {
                totalDuration += workoutSet.duration;
            }
        }
        return totalDuration;
    }
    public long getTotalPauseDuration() {
        long totalDuration = 0;
        Set keys = map.keySet();
        for (Object key1 : keys) {
            int key = (int) key1;
            WorkoutSet workoutSet = map.get(key);
            if ((workoutSet.activityID != Constants.WORKOUT_TYPE_TIME) && (workoutSet.activityID != Constants.WORKOUT_TYPE_STILL)
                    && (workoutSet.activityID != Constants.WORKOUT_TYPE_INVEHICLE)) {
                totalDuration += workoutSet.pause_duration;
            }
        }
        return totalDuration;
    }

    public long getTotalRestDuration() {
        long totalDuration = 0;
        Set keys = map.keySet();
        for (Object key1 : keys) {
            int key = (int) key1;
            WorkoutSet workoutSet = map.get(key);
            if ((workoutSet.activityID != Constants.WORKOUT_TYPE_TIME) && (workoutSet.activityID != Constants.WORKOUT_TYPE_STILL)
                    && (workoutSet.activityID != Constants.WORKOUT_TYPE_INVEHICLE)) {
                totalDuration += workoutSet.rest_duration;
            }
        }
        return totalDuration;
    }
    /**
     * Special case for steps. Maybe track estimated steps separately from walking?
     * @param workoutSet
     */
    public void setStepData(WorkoutSet workoutSet) {
        if(map.get(workoutSet.activityID) == null) {
            map.put(workoutSet.activityID, workoutSet);
        }else {
            WorkoutSet w = map.get(workoutSet.activityID);
            w.start = 0; // TODO: Remove this when we have step summary cached
            w.stepCount = workoutSet.stepCount;
        }
    }

    //   public WorkoutSet getWorkoutSetByType(int type) {
    //       return map.get(type);
    //   }

/*    public String toString() {
        String result = "";
        Set keys = map.keySet();
        for (Object key1 : keys) {
            int key = (int) key1;
            WorkoutSet value = map.get(key);
            //TODO: change this to greater varieties
            result += Utilities.getFitnessActivityTextById( value.regionID) + " steps: " + value.stepCount + "\n";
            result += Utilities.getWorkOutTextById(value.regionID) + " duration: " + getDurationBreakdown(value.duration) + "\n";
        }
        return result;
    }*/

    private double miliToMinutes(long mili) {
        return Math.floor(mili / 1000 / 60);
    }

    /**
     * Convert a_track_it.com millisecond duration to a_track_it.com string format
     *
     * @param millis A duration to convert to a_track_it.com string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long millis)
    {
        if(millis < 0)
        {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if(days > 0) {
            sb.append(days);
            sb.append(" days ");
        }
        if(hours > 0) {
            sb.append(hours);
            sb.append(" hrs ");
        }
        sb.append(minutes);
        sb.append(" mins ");

        return(sb.toString());
    }
}

