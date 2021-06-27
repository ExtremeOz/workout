package com.a_track_it.workout.activity;

public interface IEntityFragmentActivityCallback {
    void onChangedState(boolean dirty);
    void onSaveCurrent();
}
