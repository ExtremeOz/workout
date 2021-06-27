package com.a_track_it.workout.fragment;

import android.os.Bundle;

public interface AmbientInterface {
    void onEnterAmbientInFragment(Bundle ambientDetails);
    void onExitAmbientInFragment();
    void loadDataAndUpdateScreen();
}
