package com.a_track_it.fitdata.database;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.model.Workout;
import com.a_track_it.fitdata.common.model.WorkoutReport;


/**
 * Created by Chris Black
 *
 * Mock data that can be used for debugging
 */
public class MockData {

    public static WorkoutReport getDailyMockData() {
        WorkoutReport workoutReport = new WorkoutReport();

        // Mock data
        Workout workout = new Workout();
        workout.activityID = Constants.WORKOUT_TYPE_WALKING;
        workout.stepCount = 8953;
        workout.duration = 3654304;

        workoutReport.addWorkoutData(workout);

        Workout workout2 = new Workout();
        workout2.activityID = Constants.WORKOUT_TYPE_BIKING;
        workout2.duration = 2654304;
        workoutReport.addWorkoutData(workout2);
        return workoutReport;
    }

    public static WorkoutReport getWeeklyMockData() {
        WorkoutReport workoutReport = new WorkoutReport();

        // Mock data
        Workout workout = new Workout();
        workout.activityID = Constants.WORKOUT_TYPE_WALKING;
        workout.stepCount = 8953;
        workout.duration = 3654304 * 5;

        workoutReport.addWorkoutData(workout);

        Workout workout2 = new Workout();
        workout2.activityID = Constants.WORKOUT_TYPE_BIKING;
        workout2.duration = 2654304 * 2;
        workoutReport.addWorkoutData(workout2);

        Workout workout3 = new Workout();
        workout3.activityID = Constants.WORKOUT_TYPE_RUNNING;
        workout3.duration = 2654304;
        workoutReport.addWorkoutData(workout3);

        Workout workout4 = new Workout();
        workout4.activityID = Constants.WORKOUT_TYPE_KAYAKING;
        workout4.duration = 4654304;
        workoutReport.addWorkoutData(workout4);

        return workoutReport;
    }

    public static WorkoutReport getMonthlyMockData() {
        WorkoutReport workoutReport = new WorkoutReport();

        // Mock data
        Workout workout = new Workout();
        workout.activityID = Constants.WORKOUT_TYPE_WALKING;
        workout.stepCount = 8953;
        workout.duration = 3654304 * 28;
        workoutReport.addWorkoutData(workout);

        Workout workout6 = new Workout();
        workout6.activityID = Constants.WORKOUT_TYPE_AEROBICS;
        workout6.duration = 4654304 * 5;
        workoutReport.addWorkoutData(workout6);

        Workout workout5 = new Workout();
        workout5.activityID = Constants.WORKOUT_TYPE_STRENGTH;
        workout5.duration = 4654304 * 2;
        workoutReport.addWorkoutData(workout5);

        Workout workout2 = new Workout();
        workout2.activityID = Constants.WORKOUT_TYPE_BIKING;
        workout2.duration = 2654304 * 8;
        workoutReport.addWorkoutData(workout2);

        Workout workout3 = new Workout();
        workout3.activityID = Constants.WORKOUT_TYPE_RUNNING;
        workout3.duration = 2654304 * 8;
        workoutReport.addWorkoutData(workout3);

        Workout workout4 = new Workout();
        workout4.activityID = Constants.WORKOUT_TYPE_KAYAKING;
        workout4.duration = 4654304 * 3;
        workoutReport.addWorkoutData(workout4);

        return workoutReport;
    }
}
