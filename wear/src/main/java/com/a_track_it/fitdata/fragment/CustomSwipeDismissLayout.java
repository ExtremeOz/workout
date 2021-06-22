package com.a_track_it.fitdata.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

import androidx.wear.widget.SwipeDismissFrameLayout;

public class CustomSwipeDismissLayout extends SwipeDismissFrameLayout {
    public CustomSwipeDismissLayout(Context context) {
        super(context);
    }

    public CustomSwipeDismissLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSwipeDismissLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomSwipeDismissLayout(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
    }

    @Override
    public void addCallback(Callback callback) {
        super.addCallback(callback);
    }

    @Override
    public void removeCallback(Callback callback) {
        super.removeCallback(callback);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setSwipeable(boolean swipeable) {
        super.setSwipeable(swipeable);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean isSwipeable() {
        return super.isSwipeable();
    }
}
