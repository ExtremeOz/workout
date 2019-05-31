package com.a_track_it.fitdata.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.adapter.TabPagerAdapter;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.model.Utilities;
import com.a_track_it.fitdata.common.model.Workout;
import com.a_track_it.fitdata.database.DataManager;
import com.a_track_it.fitdata.fragment.PageFragment;
import com.a_track_it.fitdata.fragment.SettingsFragment;
import com.a_track_it.fitdata.model.UserPreferences;
import com.a_track_it.fitdata.service.BackgroundRefreshService;
import com.a_track_it.fitdata.service.MySetupIntentService;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.SearchEvent;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This sample demonstrates how to use the History API of the Google Fit platform to insert data,
 * query against existing data, and remove data. It also demonstrates how to authenticate
 * a_track_it.com user with Google Play Services and how to properly represent data in a_track_it.com {@link DataSet}.
 *
 * https://developers.google.com/fit/android/get-api-key
 */
@SuppressWarnings("WeakerAccess") // Butterknife requires public reference of injected views
public class MainActivity extends BaseActivity implements SearchView.OnQueryTextListener,
        FragmentManager.OnBackStackChangedListener, AppBarLayout.OnOffsetChangedListener,
        IMainActivityCallback, DataManager.IDataManager, FloatingActionsMenu.OnFloatingActionsMenuUpdateListener {

    private static final String TAG = "MainActivity";
    public static final  String RECEIVER_TAG = "MainActivityReceiver";

    public static final String ACTION_VIEW_ACTIVITY = "com.a_track_it.com.fitdata.VIEW_ACTIVITY";
    public static final String ACTION_ADD_ACTIVITY = "com.a_track_it.com.fitdata.ADD_ACTIVITY";
    public static final String ARG_ACTIVITY_ID = "ARG_ACTIVITY_ID";
    public static boolean active = false;
    protected DataManager mDataManager;
    private ReferencesTools mReferenceTools;
    private TabPagerAdapter mAdapter;

    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appBarLayout) AppBarLayout appBarLayout;
    @BindView(R.id.viewPager) ViewPager mViewPager;
    @BindView(R.id.main_overlay) View overlay;
    @BindView(R.id.floatingActionMenu) FloatingActionsMenu floatingActionMenu;
    @BindView(R.id.floatingActionButton) com.google.android.material.floatingactionbutton.FloatingActionButton fab;


    ///////////////////////////////////////
    // LIFE CYCLE
    ///////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Transition fade = new ChangeBounds();
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setExitTransition(fade);
        getWindow().setEnterTransition(fade);

        setActionBarIcon(R.drawable.atrackit_logo);
        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance(this);
        mAdapter = new TabPagerAdapter(this.getSupportFragmentManager());
        mReferenceTools = ReferencesTools.getInstance();
        mReferenceTools.init(this);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mAdapter);

        if (tabLayout != null) {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setupWithViewPager(mViewPager);
            tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    mViewPager.setCurrentItem(tab.getPosition());
                    //animateFab(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        if (overlay == null) overlay = (View) findViewById(R.id.main_overlay);

        overlay.setVisibility(View.GONE);

        overlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                floatingActionMenu.collapse();
                return true;
            }
        });
        if (floatingActionMenu == null) floatingActionMenu = (FloatingActionsMenu)findViewById(R.id.floatingActionMenu);
        floatingActionMenu.setOnFloatingActionsMenuUpdateListener(this);
        if (appBarLayout == null) appBarLayout = (AppBarLayout)findViewById(R.id.appBarLayout);

        Intent intent = new Intent(Constants.INTENT_REFRESH);
        sendBroadcast(intent);
        if (ACTION_ADD_ACTIVITY.equals(getIntent().getAction())) {
            // Invoked via the manifest shortcut.
            int activityType = getIntent().getExtras().getInt(ARG_ACTIVITY_ID);
            AddEntryActivity.launch(MainActivity.this, activityType);
        }
        if (ACTION_VIEW_ACTIVITY.equals(getIntent().getAction())) {
            // Invoked via the manifest shortcut.
            int activityType = getIntent().getExtras().getInt(ARG_ACTIVITY_ID);
            Workout workout = new Workout();
            long timeMs = System.currentTimeMillis();
            workout._id = timeMs;
            workout.packageName = getPackageName();
            workout.activityID = activityType;
            workout.activityName = mReferenceTools.getFitnessActivityTextById(activityType);

            DetailActivity.launch(MainActivity.this, null, workout);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();
        overlay.setAlpha(0f);
        floatingActionMenu.collapse();
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
        mDataManager = DataManager.getInstance(this);
        mDataManager.connect();
        mDataManager.addListener(this);
        mDataManager.setContext(this);
        appBarLayout.addOnOffsetChangedListener(this);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

    }

    @Override
    protected void onStop() {
        active = false;
        mDataManager.removeListener(this);
        mDataManager.disconnect();
        appBarLayout.removeOnOffsetChangedListener(this);
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        super.onStop();
    }

    @Override
    protected void onPause() {
        floatingActionMenu.collapse();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mAdapter.destroy();
        //mDataManager.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DataManager.REQUEST_OAUTH) {
            mDataManager.authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                mDataManager.connect();

                // check if initial setup is required.
                if (mDataManager.getExerciseCount() == 0){
                    Intent intentSetup = new Intent(this, MySetupIntentService.class);
                    intentSetup.setAction(Constants.INTENT_SETUP);
                    this.startService(intentSetup);
                }

            }
        }
    }

    int index = 0;

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        index = i;
        // position the pageViewer properly
        //MarginLayoutParamsCompat layoutParamsCompat = (MarginLayoutParamsCompat) mViewPager.get
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                PageFragment pageFragment = mAdapter.getFragment(mViewPager.getCurrentItem());
                if (pageFragment != null) {
                    if (index == 0) {
                        pageFragment.setSwipeToRefreshEnabled(true);
                    } else {
                        pageFragment.setSwipeToRefreshEnabled(false);
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackStackChanged()
    {
        try {
            FragmentManager manager = getSupportFragmentManager();
            if (manager.getBackStackEntryCount() == 0 && overlay.getVisibility() == View.VISIBLE) {
                overlay.setAlpha(0f);
                floatingActionMenu.collapse();
            }
        }catch (Exception e){
            Log.i(TAG,"onBackStackChanged " + e.getLocalizedMessage());
        }
    }

    ///////////////////////////////////////
    // OPTIONS MENU
    ///////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menu != null) {
            MenuItem mSearchItem = menu.findItem(R.id.search_participants);
            SearchView mSearchView = (SearchView) mSearchItem.getActionView();
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setQueryHint("Search");
            mSearchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_history:
                RecentActivity.launch(MainActivity.this);
                break;
            case R.id.action_reload_data:
                boolean loadComplete = UserPreferences.getBackgroundLoadComplete(this);
                long lastSync = UserPreferences.getLastSync(this);
                if (loadComplete) {
                    long syncStart = lastSync - (1000 * 60 * 24 * 7);
                    UserPreferences.setLastSync(this, syncStart);
                    startService(new Intent(this, BackgroundRefreshService.class));
                }
                return true;
            case R.id.action_settings:
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim, R.anim.enter_anim, R.anim.exit_anim);
                transaction.add(R.id.top_container, new SettingsFragment());
                transaction.addToBackStack("settings");
                transaction.commit();
                return true;
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ///////////////////////////////////////
    // SEARCH CALLBACKS
    ///////////////////////////////////////

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAdapter.filter = newText;
        if (mAdapter.getFragment(mViewPager.getCurrentItem()) != null) {
            mAdapter.getFragment(mViewPager.getCurrentItem()).setFilterText(newText);
        }
        if (mAdapter.getFragment(mViewPager.getCurrentItem() + 1) != null) {
            mAdapter.getFragment(mViewPager.getCurrentItem() + 1).setFilterText(newText);
        }
        if (mAdapter.getFragment(mViewPager.getCurrentItem() - 1) != null) {
            mAdapter.getFragment(mViewPager.getCurrentItem() - 1).setFilterText(newText);
        }
        Answers.getInstance().logSearch(new SearchEvent()
                .putQuery(newText));
        Log.d("MainActivity", "Query text: " + newText);
        return false;
    }

    @OnClick({R.id.gym_button, R.id.archery_button, R.id.run_button, R.id.other_button})
    void showAddEntryFragment(View view) {
        int activityType;
        switch (view.getId()) {
            case R.id.archery_button:
                activityType = Constants.WORKOUT_TYPE_ARCHERY;
                break;
            case R.id.run_button:
                activityType = Constants.WORKOUT_TYPE_RUNNING;
                break;
            case R.id.gym_button:
                activityType = Constants.WORKOUT_TYPE_STRENGTH;
                break;
            default:
                activityType = 3;
        }

        AddEntryActivity.launch(MainActivity.this, activityType);

    }

    ///////////////////////////////////////
    // ACTIVITY CALLBACKS - IMainActivity
    ///////////////////////////////////////

    @Override
    public void insertData(Workout workout) {
        Log.d(TAG, "insertData - new session");
        Answers.getInstance().logCustom(new CustomEvent("Insert Session")
                .putCustomAttribute("Type Id", workout.activityID)
                .putCustomAttribute("Details", workout.activityName)
                .putCustomAttribute("Step Count", workout.stepCount));
        mDataManager.insertData(workout);
    }

    @Override
    public void removeData(Workout workout) {
        mDataManager.deleteWorkout(workout);
    }

    @Override
    public void onConnected() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        final long currentTime = cal.getTimeInMillis();

        Context context = getApplicationContext();
        long lastSync = UserPreferences.getLastSync(context);
        long lastStart = UserPreferences.getLastSyncStart(context);
        if (lastSync == 0 && (currentTime - lastStart) >  1000 * 60 * 20) {
            startService(new Intent(this, BackgroundRefreshService.class));
        }
    }

    @Override
    public void onDataChanged(final Utilities.TimeFrame timeFrame) {
        Log.d(TAG, "DATA CHANGED");
        // TODO: Refresh prev / next page too
        runOnUiThread(() -> {
            if (timeFrame == Utilities.TimeFrame.BEGINNING_OF_DAY) {
                if (mAdapter.getFragment(0) != null) {
                    mAdapter.getFragment(0).refreshData();
                }
            } else if (timeFrame == Utilities.TimeFrame.BEGINNING_OF_WEEK) {
                if (mAdapter.getFragment(1) != null) {
                    mAdapter.getFragment(1).refreshData();
                }
            } else {
                if (mAdapter.getFragment(mViewPager.getCurrentItem()) != null) {
                    mAdapter.getFragment(mViewPager.getCurrentItem()).refreshData();
                }
                if (mAdapter.getFragment(mViewPager.getCurrentItem() + 1) != null) {
                    mAdapter.getFragment(mViewPager.getCurrentItem() + 1).refreshData();
                }
                if (mAdapter.getFragment(mViewPager.getCurrentItem() - 1) != null) {
                    mAdapter.getFragment(mViewPager.getCurrentItem() - 1).refreshData();
                }
            }
        });
    }

    @Override
    public void onDataComplete() {
        Log.d(TAG, "onDataComplete");
    }

    public void setStepCounting(boolean active) {
        if (mDataManager != null && UserPreferences.getCountSteps(this) != active) {
            mDataManager.setStepCounting(active);
        }
    }

    public void setActivityTracking(boolean active) {
        if (mDataManager != null && UserPreferences.getActivityTracking(this) != active) {
            mDataManager.setActivityTracking(active);
        }
    }

    @Override
    public void quickDataRead() {
        startService(new Intent(this, BackgroundRefreshService.class));
    }

    @Override
    public void launch(View transitionView, Workout workout) {
        DetailActivity.launch(MainActivity.this, transitionView, workout);
    }

    ///////////////////////////////////////
    // FLOATING MENU
    ///////////////////////////////////////

    @Override
    public void onMenuExpanded() {
        overlay.clearAnimation();
        float viewAlpha = overlay.getAlpha();
        overlay.setVisibility(View.VISIBLE);

        ValueAnimator fadeAnim = ObjectAnimator.ofFloat(overlay, "alpha", viewAlpha, 1f);
        fadeAnim.setDuration(200L);

        fadeAnim.start();
    }

    @Override
    public void onMenuCollapsed() {
        overlay.clearAnimation();
        float viewAlpha = overlay.getAlpha();
        ValueAnimator fadeAnim = ObjectAnimator.ofFloat(overlay, "alpha", viewAlpha, 0f);
        fadeAnim.setDuration(300L);
        fadeAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                overlay.setVisibility(View.GONE);
            }
        });

        fadeAnim.start();
    }
}
