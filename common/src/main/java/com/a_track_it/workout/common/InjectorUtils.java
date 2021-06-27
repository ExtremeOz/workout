package com.a_track_it.workout.common;

import android.content.Context;

import com.a_track_it.workout.common.data_model.WorkoutRepository;
import com.a_track_it.workout.common.data_model.WorkoutRoomDatabase;
import com.a_track_it.workout.common.data_model.WorkoutViewModelFactory;

public class InjectorUtils {
    public static WorkoutRepository getWorkoutRepository(Context context) {
        return WorkoutRepository.getInstance(WorkoutRoomDatabase.getDatabase(context.getApplicationContext()));
    }
    public static WorkoutViewModelFactory getWorkoutViewModelFactory(Context context){
        return new WorkoutViewModelFactory(getWorkoutRepository(context));
    }
    public static WorkoutRoomDatabase getWorkoutDB(Context context){
        return WorkoutRoomDatabase.getDatabase(context.getApplicationContext());
    }
}
