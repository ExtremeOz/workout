package com.a_track_it.fitdata.common.data_model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class WorkoutViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private WorkoutRepository repository;

    public WorkoutViewModelFactory(WorkoutRepository rep){
        super();
        this.repository = rep;
    }
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new WorkoutViewModel(repository);
    }
}
