package com.a_track_it.fitdata.activity;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;

import com.a_track_it.fitdata.R;



/**
 * Created by Chris Black
 * Modified Daniel Haywood 2020
 * Contains functionality common to all Activities. Code here should be kept to the bare
 * minimum.
 */
public abstract class BaseActivity extends androidx.appcompat.app.AppCompatActivity {
    protected androidx.appcompat.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected abstract int getLayoutResource();

    protected void setActionBarIcon(int iconRes) {
        toolbar.setNavigationIcon(iconRes);
    }
}