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
 * Data model for the fitdata reports.
 * Updated Daniel Haywood 5/19 - added sets and other calculations.
 *
 */
public class WorkoutReport {
    private Map<Integer, Workout> map =  new HashMap<>();

    public void addWorkoutData(Workout workout) {
        // no steps and less than a minute or STILL - get out
        if((workout.activityID == Constants.WORKOUT_TYPE_STILL) || (workout.stepCount == 0 && workout.duration < 60000)) {
            // Ignore "still" time or workouts less than 1 minute.
            return;
        }

        if(map.get(workout.activityID) == null) {
            map.put(workout.activityID, workout);
        }else {
            Workout w = map.get(workout.activityID);
            w.stepCount += workout.stepCount;
            w.duration += workout.duration;
            w.rest_duration += workout.rest_duration;
            w.pause_duration += workout.pause_duration;
            w.wattsTotal += workout.wattsTotal;
            w.weightTotal += workout.weightTotal;
            w.setCount += workout.setCount;
            w.repCount += workout.repCount;
            if (workout.start_steps < w.start_steps) w.start_steps = workout.start_steps;
            if (workout.end_steps > w.end_steps) w.end_steps = workout.end_steps;

        }
    }

    public void clearWorkoutData() {
        map.clear();
    }

    public ArrayList<Workout> getWorkoutData() {
        Workout summary = new Workout();
        summary.activityID = Constants.WORKOUT_TYPE_TIME;
        summary.duration = getTotalDuration();
        summary.start = -1;
        replaceWorkout(summary);
        ArrayList<Workout> result = new ArrayList<>(map.values());
        Collections.sort(result);
        return result;
    }

    public void replaceWorkout(Workout workout) {
        if(map.get(workout.activityID) == null) {
            map.put(workout.activityID, workout);
        }else {
            Workout w = map.get(workout.activityID);
            w = workout;
        }
    }

    public long getTotalDuration() {
        long totalDuration = 0;
        Set keys = map.keySet();
        for (Object key1 : keys) {
            int key = (int) key1;
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
            int key = (int) key1;
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
            int key = (int) key1;
            Workout workout = map.get(key);
            if ((workout.activityID != Constants.WORKOUT_TYPE_TIME) && (workout.activityID != Constants.WORKOUT_TYPE_STILL)
                    && (workout.activityID != Constants.WORKOUT_TYPE_INVEHICLE)) {
                totalDuration += workout.rest_duration;
            }
        }
        return totalDuration;
    }
    /**
     * Special case for steps. Maybe track estimated steps separately from walking?
     * @param workout
     */
    public void setStepData(Workout workout) {
        if(map.get(workout.activityID) == null) {
            map.put(workout.activityID, workout);
        }else {
            Workout w = map.get(workout.activityID);
            w.start = 0; // TODO: Remove this when we have step summary cached
            w.stepCount = workout.stepCount;
        }
    }

    public Workout getWorkoutByType(int type) {
        return map.get(type);
    }

/*    public String toString() {
        String result = "";
        Set keys = map.keySet();
        for (Object key1 : keys) {
            int key = (int) key1;
            Workout value = map.get(key);
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
