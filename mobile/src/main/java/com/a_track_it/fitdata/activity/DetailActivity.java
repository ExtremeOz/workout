package com.a_track_it.fitdata.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentManager;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.model.Utilities;
import com.a_track_it.fitdata.common.model.Workout;
import com.a_track_it.fitdata.database.CacheManager;
import com.a_track_it.fitdata.fragment.ReportsFragment;
import com.a_track_it.fitdata.model.SummaryData;
import com.a_track_it.fitdata.service.CacheResultReceiver;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Chris Black
 *
 * Displays a_track_it.com detail page for the selected fitdata regionID
 */
@SuppressWarnings("WeakerAccess") // Butterknife requires public reference of injected views
public class DetailActivity extends BaseActivity implements CacheResultReceiver.Receiver {

    private static final String EXTRA_TYPE = "DetailActivity:regionID";
    private static final String EXTRA_TITLE = "DetailActivity:title";
    private static final String EXTRA_IMAGE = "DetailActivity:image";

    @BindView(R.id.spinner) Spinner navigationSpinner;
    @BindView(R.id.average_text)
    TextView averageText;

    @BindView(R.id.current_text)
    TextView currentText;

    private CacheResultReceiver mReceiver;
    private ReportsFragment mReportsFragment;
    private static ReferencesTools mRefTools;
    /**
     * Used to start the activity using a_track_it.com custom animation.
     *
     * @param activity Reference to the Android Activity we are animating from
     * @param transitionView Target view used in the scene transition animation
     * @param workout Type of fitdata the DetailActivity should load
     */
    public static void launch(BaseActivity activity, View transitionView, Workout workout) {
        ActivityOptionsCompat options = null;
        if (transitionView != null)
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity, transitionView, EXTRA_IMAGE);
        Intent intent = new Intent(activity, DetailActivity.class);
        if (mRefTools == null) {
            mRefTools = ReferencesTools.getInstance();
            mRefTools.init(activity);
        }
        int iconId = (workout != null) ? mRefTools.getFitnessActivityIconResById(workout.activityID) : mRefTools.getFitnessActivityIconResById(Constants.WORKOUT_TYPE_AEROBICS);
        intent.putExtra(EXTRA_IMAGE, iconId);
        intent.putExtra(EXTRA_TITLE, workout.activityName);
        intent.putExtra(EXTRA_TYPE, workout.activityID);
        if (options != null)
            ActivityCompat.startActivity(activity, intent, options.toBundle());
        else
            ActivityCompat.startActivity(activity, intent, null);
    }

    ///////////////////////////////////////
    // LIFE CYCLE
    ///////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Transition fade = new Fade();
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        getWindow().setExitTransition(fade);
        getWindow().setEnterTransition(fade);


        ButterKnife.bind(this);
        int workoutType = getIntent().getIntExtra(EXTRA_TYPE, 0);
        String workoutTitle = getIntent().getStringExtra(EXTRA_TITLE);
        mRefTools = ReferencesTools.getInstance();
        mRefTools.init(this);
        mReceiver = new CacheResultReceiver(new Handler());
        ImageView image = (ImageView) findViewById(R.id.image);
        ViewCompat.setTransitionName(image, EXTRA_IMAGE);
        image.setImageResource(getIntent().getIntExtra(EXTRA_IMAGE, R.drawable.heart_icon));
        image.setColorFilter(this.getApplicationContext().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
        int vibrant = ContextCompat.getColor(this, mRefTools.getFitnessActivityColorById(workoutType));


        /*
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Palette palette = Palette.from(bitmap).generate();
        Palette.Swatch swatch = palette.getLightVibrantSwatch();

        /*
        int vibrant = 0xFF110000;
        if (swatch != null) {
            vibrant = swatch.getRgb();//palette.getVibrantColor(0xFF110000);
        }
        if(vibrant == 0xFF110000) {
            swatch = palette.getVibrantSwatch();
            //vibrant = palette.getLightMutedColor(0x000000);
            if (swatch != null) {
                vibrant = swatch.getRgb();//palette.getVibrantColor(0xFF110000);
            }
        }
        */
        image.setBackgroundColor(Utilities.lighter(vibrant, 0.4f));

        View container = findViewById(R.id.container);
        container.setBackgroundColor(vibrant);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(workoutTitle);
        }

        toolbar.setBackgroundColor(vibrant);

        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.graph_types, R.layout.spinner_item);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        if (navigationSpinner == null) navigationSpinner =(Spinner)findViewById(R.id.spinner);
        navigationSpinner.setAdapter(spinnerAdapter);

        navigationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateReport();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        getWindow().setStatusBarColor(vibrant);


        // Report fragment used to display charts and graphs
        FragmentManager fragmentManager = getSupportFragmentManager();
        mReportsFragment = ReportsFragment.newInstance(workoutType, 1, workoutTitle);
        fragmentManager.beginTransaction()
                       .replace(R.id.chart_container, mReportsFragment)
                       .commit();


    }

    @Override
    public void onResume() {
        super.onResume();
        mReceiver.setReceiver(this);
        CacheManager.getSummary(-2, mReceiver, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mReceiver.setReceiver(null);
    }

    private void updateReport() {
        int selectedIndex = navigationSpinner.getSelectedItemPosition();
        switch (selectedIndex) {
            case 0:
                mReportsFragment.setGroupCount(1);
                break;
            case 1:
                mReportsFragment.setGroupCount(7);
                break;
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_detail;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    ///////////////////////////////////////
    // OPTIONS MENU
    ///////////////////////////////////////

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a_track_it.com parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                // Reverse the animation back to the previous view.
                finishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

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