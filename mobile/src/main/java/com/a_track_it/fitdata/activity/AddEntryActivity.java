package com.a_track_it.fitdata.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.service.CacheResultReceiver;
import com.a_track_it.fitdata.fragment.AddEntryFragment;
import com.a_track_it.fitdata.model.SummaryData;

/**
 * Created by Chris Black
 *
 * The AddEntryActivity displays a_track_it.com form field allowing the user to manually add an
 * entry into the Fit API.
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // Butterknife requires public reference of injected views
public class AddEntryActivity extends BaseActivity implements CacheResultReceiver.Receiver{

    private static final String TAG = "AddEntryActivity";

    private AddEntryFragment fragment;
    public static final String ARG_ACTIVITY_ID = "ARG_ACTIVITY_ID";
    private CacheResultReceiver mReceiver;

    private String sUserID;
    View container;

    /**
     * Used to start the activity using a_track_it.com custom animation.
     *
     * @param activity Reference to the Android Activity we are animating from
     * @param entityID ID of activity 1, bodypart 2, exercise 3 used to pre-populate the form field
     */
    public static void launch(BaseActivity activity, int entityID) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.enter_anim, R.anim.no_anim);
        Intent intent = new Intent(activity, AddEntryActivity.class);
        intent.putExtra(ARG_ACTIVITY_ID, entityID);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    ///////////////////////////////////////
    // LIFE CYCLE
    ///////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Add Entry");
        }
        if (toolbar != null){
            Drawable drawableUnChecked = AppCompatResources.getDrawable(context,R.drawable.ic_close_white);
            Utilities.setColorFilter(drawableUnChecked, ContextCompat.getColor(context, R.color.secondaryTextColor));
            toolbar.setNavigationIcon(drawableUnChecked);
        }
        container = findViewById(R.id.container);
        com.google.android.material.button.MaterialButton btnSave = findViewById(R.id.save_button);
        btnSave.setOnClickListener(v -> {
            final Workout workout = fragment.getWorkout();
            if (workout != null) {
                Log.d(TAG, "Added: " + workout.toString());
                // Validate fitdata doesn't overlap
                if (false) {   //CacheManager.checkConflict(mWorkoutDao, workout)
                    Log.d(TAG, "Overlap detected.");
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddEntryActivity.this);
                    builder.setMessage("Overlap detected.")
                            .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //      mDataManager.insertData(workout);
                                    finishAfterTransition();
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel_button_text), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
            /*
            Snackbar.make(container, "Overlap detected.", Snackbar.LENGTH_INDEFINITE).setAction("OVERLAP", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDataManager.insertData(fitdata);
                    finishAfterTransition();
                }
            }).show();*/
                } else {
                    //  mDataManager.insertData(workout);
                    finishAfterTransition();
                }
            }
        });
        com.google.android.material.button.MaterialButton btnCancel = findViewById(R.id.cancel_button);
        btnCancel.setOnClickListener(v -> finishAfterTransition());

        int activityType = getIntent().getExtras().getInt(ARG_ACTIVITY_ID);
        mReceiver = new CacheResultReceiver(new Handler());
        fragment = AddEntryFragment.create(activityType);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.placeholder, fragment, AddEntryFragment.TAG);
        transaction.commit();
    }


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_add_entry;
    }

    ///////////////////////////////////////
    // OPTIONS MENU
    ///////////////////////////////////////

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                // Reverse animation back to previous activity
                ActivityCompat.finishAfterTransition(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ///////////////////////////////////////
    // EVENT HANDLERS
    ///////////////////////////////////////

    ///////////////////////////////////////
    // CALLBACKS
    ///////////////////////////////////////

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        final SummaryData data = resultData.getParcelable("workoutSummary");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO: Work in progress
                //averageText.setText("Average steps per day: " + data.averageDailyData);
                //currentText.setText("Steps today: " + data.todayData);
            }
        });
    }


}
