package com.a_track_it.fitdata.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.model.Utilities;
import com.a_track_it.fitdata.common.model.Workout;
import com.a_track_it.fitdata.database.CacheManager;
import com.a_track_it.fitdata.database.DataManager;
import com.a_track_it.fitdata.fragment.AddEntryFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Chris Black
 *
 * The AddEntryActivity displays a_track_it.com form field allowing the user to manually add an
 * entry into the Fit API.
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // Butterknife requires public reference of injected views
public class AddEntryActivity extends BaseActivity implements DataManager.IDataManager {

    private static final String TAG = "AddEntryActivity";

    private AddEntryFragment fragment;
    public static final String ARG_ACTIVITY_ID = "ARG_ACTIVITY_ID";
    private DataManager mDataManager;

    @BindView(R.id.container) View container;

    /**
     * Used to start the activity using a_track_it.com custom animation.
     *
     * @param activity Reference to the Android Activity we are animating from
     * @param activityType Fitness activity regionID used to pre-populate the form field
     */
    public static void launch(BaseActivity activity, int activityType) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.enter_anim, R.anim.no_anim);
        Intent intent = new Intent(activity, AddEntryActivity.class);
        intent.putExtra(ARG_ACTIVITY_ID, activityType);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    ///////////////////////////////////////
    // LIFE CYCLE
    ///////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Add Entry");
        }
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_close_white, null));
        mDataManager = DataManager.getInstance(this);
        mDataManager.connect();
        int activityType = getIntent().getExtras().getInt(ARG_ACTIVITY_ID);
        fragment = AddEntryFragment.create(activityType);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.placeholder, fragment, AddEntryFragment.TAG);
        transaction.commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        mDataManager.connect();
        mDataManager.addListener(this);
    }

    @Override
    protected void onStop() {
        mDataManager.removeListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mDataManager.disconnect();
        super.onDestroy();
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
    @OnClick(R.id.save_button) void onSave() {
        final Workout workout = fragment.getWorkout();
        if (workout != null) {
            Log.d(TAG, "Added: " + workout.toString());
            // Validate fitdata doesn't overlap
            if (CacheManager.checkConflict(this, workout)) {
                Log.d(TAG, "Overlap detected.");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Overlap detected.")
                        .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDataManager.insertData(workout);
                                finishAfterTransition();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
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
                mDataManager.insertData(workout);
                finishAfterTransition();
            }
        }
    }

    @OnClick(R.id.cancel_button) void onCancel() {
        finishAfterTransition();
    }

    ///////////////////////////////////////
    // CALLBACKS
    ///////////////////////////////////////

    @Override
    public void insertData(Workout workout) {
        mDataManager.insertData(workout);
    }

    @Override
    public void removeData(Workout workout) {
        mDataManager.deleteWorkout(workout);
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDataChanged(Utilities.TimeFrame timeFrame) {

    }

    @Override
    public void onDataComplete() {

    }
}
