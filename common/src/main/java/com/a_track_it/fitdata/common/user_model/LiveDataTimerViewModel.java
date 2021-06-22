package com.a_track_it.fitdata.common.user_model;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Timer;
import java.util.TimerTask;


public class LiveDataTimerViewModel extends AndroidViewModel {

    private static final int ONE_SECOND = 1000;
    private MutableLiveData<Long> mCountdownTime;
    private MutableLiveData<Long> mElapsedTime;
    private MutableLiveData<Long> mCurrentTime;
    private long mInitialTime;
    private Timer timer;
    public LiveDataTimerViewModel(Application app) {
        super(app);
        mCountdownTime = new MutableLiveData<>();
        mElapsedTime = new MutableLiveData<>();
        mCurrentTime = new MutableLiveData<>();
        mInitialTime = System.currentTimeMillis();
        mCurrentTime.setValue(System.currentTimeMillis());
        timer = new Timer();

    }
    public void setInitialTime(Long initial){ mInitialTime = initial;  }
    public void setCountdownTime(Long countdownTime){ mCountdownTime.setValue(countdownTime);}
    public void stopTime(){
        timer.cancel();
    }
    public void startTime(){
        // Update the elapsed time every second.
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final long newValue = (System.currentTimeMillis() - mInitialTime) / 1000;
                long countValue = (mCountdownTime.getValue() != null) ? mCountdownTime.getValue() : 0;
                // setValue() cannot be called from a background thread so post to main thread.
                if (countValue > 0)  mCountdownTime.postValue(--countValue);
                mElapsedTime.postValue(newValue);
                mCurrentTime.postValue(System.currentTimeMillis());
            }
        }, ONE_SECOND, ONE_SECOND);
    }
    public LiveData<Long> getCountdownTime(){ return  mCountdownTime;}
    public LiveData<Long> getCurrentTime(){ return mCurrentTime; }
    public LiveData<Long> getElapsedTime() { return mElapsedTime; }
}