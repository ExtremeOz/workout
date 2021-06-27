package com.a_track_it.workout.adapter;

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;

/**
 * Created by Chris Black
 */
public class AnimatedLayoutManager extends GridLayoutManager {


    public AnimatedLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }
}
