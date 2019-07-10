package com.a_track_it.fitdata.activity;

import com.a_track_it.fitdata.data_model.Bodypart;
import com.a_track_it.fitdata.data_model.Exercise;
import com.a_track_it.fitdata.data_model.FitnessActivity;
import com.a_track_it.fitdata.data_model.Workout;
import com.a_track_it.fitdata.data_model.WorkoutSet;

import java.util.ArrayList;

public interface IDataLoaderCallback {
    void setExerciseList(ArrayList<Exercise> exerciseList);
    void setActivityList(ArrayList<FitnessActivity> activityList);
    void setBodypartList(ArrayList<Bodypart> bodypartList);
    void setActiveWorkout(Workout activeWorkout);
    void setActiveWorkoutSet(WorkoutSet activeSet);
    void setToDoWorkoutSets(ArrayList<WorkoutSet> toDoWorkoutSets);
    void setCompletedWorkouts(ArrayList<Workout> completedWorkouts);
    void setCompletedWorkoutSets(ArrayList<WorkoutSet> completedSets);
    void actionCompleted(boolean saved, int index, int type);
}
