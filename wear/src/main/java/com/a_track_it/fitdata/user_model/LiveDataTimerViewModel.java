package com.a_track_it.fitdata.user_model;

import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.services.common.CurrentTimeProvider;

/**
 * A ViewModel used for the {@link com.a_track_it.fitdata.activity.RoomActivity}.
 */
public class LiveDataTimerViewModel extends ViewModel {

    private static final int ONE_SECOND = 1000;

    private MutableLiveData<Long> mElapsedTime = new MutableLiveData<>();
    private MutableLiveData<Long> mCurrentTime = new MutableLiveData<>();
    private long mInitialTime;

    public LiveDataTimerViewModel() {
        mInitialTime = SystemClock.elapsedRealtime();
        mCurrentTime.setValue(System.currentTimeMillis());
        Timer timer = new Timer();

        // Update the elapsed time every second.
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final long newValue = (SystemClock.elapsedRealtime() - mInitialTime) / 1000;
                // setValue() cannot be called from a background thread so post to main thread.
                mElapsedTime.postValue(newValue);
                mCurrentTime.postValue(System.currentTimeMillis());
            }
        }, ONE_SECOND, ONE_SECOND);

    }
    public LiveData<Long> getCurrentTime(){ return mCurrentTime; }
    public LiveData<Long> getElapsedTime() {
        return mElapsedTime;
    }
}