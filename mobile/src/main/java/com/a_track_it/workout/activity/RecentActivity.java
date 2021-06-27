package com.a_track_it.workout.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.a_track_it.workout.R;
import com.a_track_it.workout.common.Constants;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.Configuration;
import com.a_track_it.workout.common.data_model.DateTuple;
import com.a_track_it.workout.common.data_model.FitSessionJobIntentService;
import com.a_track_it.workout.common.data_model.UserDailyTotals;
import com.a_track_it.workout.common.data_model.Workout;
import com.a_track_it.workout.common.data_model.WorkoutSet;
import com.a_track_it.workout.common.data_model.WorkoutViewModel;
import com.a_track_it.workout.common.data_model.WorkoutViewModelFactory;
import com.a_track_it.workout.common.model.ApplicationPreferences;
import com.a_track_it.workout.common.model.UserPreferences;
import com.a_track_it.workout.fragment.CustomAlertDialog;
import com.a_track_it.workout.fragment.FragmentInterface;
import com.a_track_it.workout.fragment.ICustomConfirmDialog;
import com.a_track_it.workout.fragment.RecentFragment;
import com.a_track_it.workout.reports.AverageBPMLineChart;
import com.a_track_it.workout.reports.DailyBPMRangeChart;
import com.google.android.gms.fitness.data.DataType;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.achartengine.GraphicalView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.a_track_it.workout.common.Constants.INTENT_EXTRA_MSG;
import static com.a_track_it.workout.common.Constants.INTENT_MESSAGE_TOAST;
import static com.a_track_it.workout.common.Constants.INTENT_PERMISSION_AGE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_DEVICE_ID;
import static com.a_track_it.workout.common.Constants.KEY_FIT_TYPE;
import static com.a_track_it.workout.common.Constants.KEY_FIT_USER;
import static com.a_track_it.workout.common.Constants.KEY_FIT_WORKOUTID;
import static com.a_track_it.workout.common.Constants.KEY_FIT_WORKOUT_SETID;
import static com.a_track_it.workout.common.Constants.OBJECT_TYPE_WORKOUT;
import static com.a_track_it.workout.common.Constants.OBJECT_TYPE_WORKOUT_SET;
import static com.a_track_it.workout.common.Constants.QUESTION_KEEP_DELETE;

/**
 * Created by Chris Black
 * - updated by Daniel Haywood
 * Activity that displays a_track_it.com list of recent entries. This Activity contains an Toolbar
 * item for filtering results.
 */
public class RecentActivity extends BaseActivity implements FragmentInterface {
    private static final String TAG = "RecentActivity";
    private RecentFragment fragment;
    public static final String ARG_RETURN_RESULT = "ARG_RETURN_RESULT";
    private WorkoutViewModel mSessionViewModel;
    private Handler mHandler;
    private List<UserDailyTotals> userDailyTotalsList = new ArrayList<>();
    private UserPreferences userPrefs;
    private String sUserId;
    private String sDeviceID;
    private Calendar mCalendar;
    private SimpleDateFormat simpleDateFormat;
    private DailyBPMRangeChart dailyChart;
    private AverageBPMLineChart lineChart;
    private long startTime;
    private long endTime;
    private int dailyFlag;
   // private View container;
    private Menu mMenu;
    private int mRecentType;
    private int returnResultFlag;

    final private ResultReceiver localResultReceiver = new ResultReceiver(new Handler()){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == 200){
                String sKey = DataType.AGGREGATE_HEART_RATE_SUMMARY.getName();
                if (resultData.containsKey(sKey)) {
                    ArrayList<Bundle> bpmList = resultData.getParcelableArrayList(sKey);
                    sKey = Constants.KEY_RESULT;
                    int iRows = resultData.getInt(sKey,0);
                    int interval = resultData.getInt(Constants.KEY_FIT_VALUE);
                    if (iRows == 0){
                        FrameLayout frameLayout = findViewById(R.id.placeholder);
                        frameLayout.removeAllViews();
                        TextView textViewNoItems = findViewById(R.id.textViewNoItems);
                        if (textViewNoItems != null) textViewNoItems.setVisibility(View.VISIBLE);
                    }else {
                        if (interval == 60) {
                            mHandler.post(() -> {
                                for (int iHour = 0; iHour < 24; iHour++) {
                                    mCalendar.setTimeInMillis(startTime);
                                    mCalendar.add(Calendar.HOUR_OF_DAY, iHour);
                                    long lHour = mCalendar.getTimeInMillis();
                                    UserDailyTotals userDailyTotal = getExistingUDT(lHour);
                                    boolean bExisting = (userDailyTotal != null);
                                    if (!bExisting) {
                                        userDailyTotal = new UserDailyTotals();
                                        userDailyTotal._id = lHour;
                                    }
                                    if (bpmList.size() > 0) {
                                        for (Bundle bpmBundle : bpmList) {
                                            String sType = bpmBundle.getString(Constants.MAP_DATA_TYPE);
                                            if (bpmBundle.containsKey(Constants.MAP_START)) {
                                                long bpmStart = bpmBundle.getLong(Constants.MAP_START);
                                                if (bpmStart > 0) {
                                                    mCalendar.setTimeInMillis(bpmStart);
                                                    if (iHour == mCalendar.get(Calendar.HOUR_OF_DAY)) {
                                                        if (DataType.AGGREGATE_HEART_RATE_SUMMARY.getName().equals(sType)) {
                                                            userDailyTotal.avgBPM = bpmBundle.getFloat(Constants.MAP_BPM_AVG);
                                                            userDailyTotal.minBPM = bpmBundle.getFloat(Constants.MAP_BPM_MIN);
                                                            userDailyTotal.maxBPM = bpmBundle.getFloat(Constants.MAP_BPM_MAX);
                                                        }
                                                        if (DataType.TYPE_DISTANCE_DELTA.getName().equals(sType)) {
                                                            userDailyTotal.distanceTravelled = bpmBundle.getFloat(DataType.TYPE_DISTANCE_DELTA.getName());
                                                        }
                                                        if (DataType.TYPE_STEP_COUNT_DELTA.getName().equals(sType)
                                                                || DataType.AGGREGATE_STEP_COUNT_DELTA.getName().equals(sType)) {
                                                            userDailyTotal.stepCount = bpmBundle.getInt(Constants.MAP_STEPS);
                                                        }
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!bExisting) {
                                        userDailyTotalsList.add(userDailyTotal);
                                    }
                                }
                                DisplayMetrics metrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                int densityDpi = metrics.densityDpi;
                                dailyChart.setPerDay((interval == 60) ? 0 : 1); // hourly
                                dailyChart.setStartFrom(startTime);
                                dailyChart.setUserDailyTotalsList(userDailyTotalsList);
                                final GraphicalView graphicalView = dailyChart.getView(getApplicationContext(), densityDpi);
                                if (graphicalView != null) {
                                    final FrameLayout frameLayout = findViewById(R.id.placeholder);
                                    runOnUiThread(() -> {
                                        frameLayout.removeAllViews();
                                        frameLayout.addView(graphicalView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                                    });
                                }
                            });
                        }else {
                            mHandler.post(() -> {
                                long lDayOfYear;
                                for (int iDOY = 0; iDOY < 31; iDOY++) {
                                    mCalendar.setTimeInMillis(startTime);
                                    mCalendar.add(Calendar.DAY_OF_YEAR, iDOY);
                                    lDayOfYear = mCalendar.get(Calendar.DAY_OF_YEAR);
                                    long currentCal = mCalendar.getTimeInMillis();
                                    long endOfDay = Utilities.getDayEnd(mCalendar, currentCal);
                                    UserDailyTotals userDailyTotal = getExistingUDT(endOfDay);
                                    boolean bExisting = (userDailyTotal != null);
                                    if (!bExisting) {
                                        userDailyTotal = new UserDailyTotals();
                                        userDailyTotal._id = endOfDay;
                                    }
                                    if (bpmList.size() > 0) {
                                        for (Bundle bpmBundle : bpmList) {
                                            String sType = bpmBundle.getString(Constants.MAP_DATA_TYPE);
                                            if (bpmBundle.containsKey(Constants.MAP_START)) {
                                                long bpmStart = bpmBundle.getLong(Constants.MAP_START);
                                                if (bpmStart > 0) {
                                                    mCalendar.setTimeInMillis(bpmStart);
                                                    if (lDayOfYear == mCalendar.get(Calendar.DAY_OF_YEAR)) {
                                                        if (DataType.AGGREGATE_HEART_RATE_SUMMARY.getName().equals(sType)) {
                                                            userDailyTotal.avgBPM = bpmBundle.getFloat(Constants.MAP_BPM_AVG);
                                                            userDailyTotal.minBPM = bpmBundle.getFloat(Constants.MAP_BPM_MIN);
                                                            userDailyTotal.maxBPM = bpmBundle.getFloat(Constants.MAP_BPM_MAX);
                                                        }
                                                        if (DataType.TYPE_DISTANCE_DELTA.getName().equals(sType)) {
                                                            userDailyTotal.distanceTravelled = bpmBundle.getFloat(DataType.TYPE_DISTANCE_DELTA.getName());
                                                        }
                                                        if (DataType.TYPE_STEP_COUNT_DELTA.getName().equals(sType)
                                                                || DataType.AGGREGATE_STEP_COUNT_DELTA.getName().equals(sType)) {
                                                            userDailyTotal.stepCount = bpmBundle.getInt(Constants.MAP_STEPS);
                                                        }
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!bExisting) {
                                        userDailyTotalsList.add(userDailyTotal);
                                    }
                                    mCalendar.setTimeInMillis(endOfDay);
                                    mCalendar.add(Calendar.DAY_OF_YEAR, 1);
                                    lDayOfYear = mCalendar.getTimeInMillis();
                                }
                                DisplayMetrics metrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                int densityDpi = metrics.densityDpi;
                                dailyChart.setPerDay((interval == 60) ? 0 : 1); // hourly
                                dailyChart.setStartFrom(startTime);
                                dailyChart.setUserDailyTotalsList(userDailyTotalsList);
                                final GraphicalView graphicalView = dailyChart.getView(getApplicationContext(), densityDpi);
                                if (graphicalView != null) {
                                    final FrameLayout frameLayout = findViewById(R.id.placeholder);
                                    runOnUiThread(() -> {
                                        frameLayout.removeAllViews();
                                        frameLayout.addView(graphicalView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                                    });
                                }
                            });
                        }
                    }
                }
            }

        }
    };

    private UserDailyTotals getExistingUDT(long rowid){
        if (userDailyTotalsList.size() > 0){
            for (UserDailyTotals u : userDailyTotalsList){
                if (u._id == rowid) return u;
            }
        }
        return null;
    }


    ///////////////////////////////////////
    // LIFE CYCLE
    ///////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        mCalendar = Calendar.getInstance();
        mHandler = new Handler();
        long timeMs = System.currentTimeMillis();
        if (getIntent() != null) {
// return a result flat
            returnResultFlag = (getIntent().hasExtra(ARG_RETURN_RESULT) ? getIntent().getIntExtra(ARG_RETURN_RESULT, 0) : 0);
            if (getIntent().hasExtra(Constants.MAP_DATA_TYPE))
                mRecentType = getIntent().getIntExtra(Constants.MAP_DATA_TYPE, 0);
            if (getIntent().hasExtra(Constants.MAP_START))
                startTime = getIntent().getLongExtra(Constants.MAP_START, System.currentTimeMillis());
            if (startTime == 0)
                startTime = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
            else
                startTime = Utilities.getDayStart(mCalendar,startTime);
            if (Utilities.getDayEnd(mCalendar, startTime) > timeMs)
                endTime = timeMs;
            else
                endTime = Utilities.getDayEnd(mCalendar,startTime);

        }else {
            if (savedInstanceState != null){
                if (savedInstanceState.containsKey(ARG_RETURN_RESULT))
                    returnResultFlag = savedInstanceState.getInt(ARG_RETURN_RESULT, 0);
                if (savedInstanceState.containsKey(Constants.MAP_DATA_TYPE))
                    mRecentType = savedInstanceState.getInt(Constants.MAP_DATA_TYPE, 0);
                if (savedInstanceState.containsKey(Constants.MAP_START))
                    startTime = savedInstanceState.getLong(Constants.MAP_START, 0);
                if (startTime == 0)
                    startTime = Utilities.getTimeFrameStart(Utilities.TimeFrame.BEGINNING_OF_DAY);
                else
                    startTime = Utilities.getDayStart(mCalendar,startTime);
                if (Utilities.getDayEnd(mCalendar, startTime) > timeMs)
                    endTime = timeMs;
                else
                    endTime = Utilities.getDayEnd(mCalendar,startTime);

            }else {
                mRecentType = 0;
                Utilities.TimeFrame timeFrame = Utilities.TimeFrame.BEGINNING_OF_DAY;
                startTime = Utilities.getTimeFrameStart(timeFrame);
                endTime = Utilities.getTimeFrameEnd(timeFrame);
            }
        }
        //container = findViewById(R.id.container);
        Drawable drawableUnChecked = AppCompatResources.getDrawable(context,R.drawable.ic_close_white);
        Utilities.setColorFilter(drawableUnChecked, ContextCompat.getColor(context, R.color.secondaryTextColor));
        if (toolbar != null){
            toolbar.setNavigationIcon(drawableUnChecked);
        }
        WorkoutViewModelFactory factory = com.a_track_it.workout.common.InjectorUtils.getWorkoutViewModelFactory(getApplicationContext());
        mSessionViewModel = new ViewModelProvider(RecentActivity.this, factory).get(WorkoutViewModel.class);
        mCalendar.setTimeInMillis(startTime);
        simpleDateFormat = new SimpleDateFormat("MMM-dd", Locale.getDefault());
        sUserId = ApplicationPreferences.getPreferences(context).getLastUserID();
        userPrefs = UserPreferences.getPreferences(context, sUserId);

        doLoadFromData();

    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong(Constants.MAP_DATA_TYPE, mRecentType);
        outState.putInt(ARG_RETURN_RESULT, returnResultFlag);
        outState.putLong(Constants.MAP_START,startTime);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
       // mCursor.close();
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_recent;
    }

    ///////////////////////////////////////
    // OPTIONS MENU
    ///////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mRecentType != 3) {
            getMenuInflater().inflate(R.menu.history, menu);
            mMenu = menu;
            MenuItem mDateItem = mMenu.findItem(R.id.action_filter_date);

            mDateItem.setTitle(simpleDateFormat.format(mCalendar.getTime()));

            MenuItem filterItem = mMenu.findItem(R.id.action_filter);
            Drawable calDrawable = AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_calendar2);
            Utilities.setColorFilter(calDrawable, getColor(R.color.primaryTextColor));
            filterItem.setIcon(calDrawable);
        }else {
         //   getMenuInflater().inflate(R.menu.recent_template, menu);
            mMenu = menu;

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up btnConfirmBodypart, so long
        // as you specify a_track_it.com parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                ActivityCompat.finishAfterTransition(this);
                return true;
            case R.id.action_filter:
                mCalendar.setTime(new Date()); // reset back to today
            case R.id.action_filter_date:
                int year = mCalendar.get(Calendar.YEAR);
                int month = mCalendar.get(Calendar.MONTH);
                int day = mCalendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog mTimePicker = new DatePickerDialog(RecentActivity.this,
                        R.style.MyDatePickerStyle, (view, selectedYear, monthOfYear, dayOfMonth) -> {
                    mCalendar.clear();
                    mCalendar.set(selectedYear, monthOfYear, dayOfMonth, 0, 0);
                    mCalendar.set(Calendar.SECOND,0); mCalendar.set(Calendar.MILLISECOND,0);
                    long startTime = mCalendar.getTimeInMillis();
                    mCalendar.set(Calendar.HOUR_OF_DAY, 23);
                    mCalendar.set(Calendar.MINUTE, 59);
                    mCalendar.set(Calendar.SECOND, 59);
                    long timeMs = System.currentTimeMillis();
                    long endTime = Math.min(mCalendar.getTimeInMillis(), timeMs);
                    try {
                        MenuItem mDateItem = mMenu.findItem(R.id.action_filter_date);
                        mDateItem.setTitle(simpleDateFormat.format(mCalendar.getTime()));
                        if ((mRecentType == -1)||(mRecentType == 0)||(mRecentType == 3)) {
                            ApplicationPreferences applicationPreferences = ApplicationPreferences.getPreferences(getApplicationContext());
                            applicationPreferences.setHistoryOpen(startTime);
                            fragment.setStartTime(startTime);
                            fragment.setEndTime(endTime);
                            fragment.refreshData();
                        }else{
                            this.startTime = startTime;
                            doLoadFromData();
                        }
                    }catch (Exception e){
                        Log.e(TAG, e.getMessage());
                    }

                },year,month,day);
                mTimePicker.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnFragmentInteraction(int srcId, long selectedId, String text) {
/*        if (returnResultFlag > 0) {
            Intent resultIntent  = new Intent(Constants.INTENT_WORKOUT_REPORT);
            resultIntent.putExtra(KEY_FIT_WORKOUTID, selectedId);
            resultIntent.putExtra(KEY_FIT_USER, sUserId);
            resultIntent.putExtra(KEY_FIT_DEVICE_ID, sDeviceID);
            resultIntent.putExtra(Constants.MAP_DATA_TYPE,OBJECT_TYPE_WORKOUT);
            resultIntent.putExtra(Constants.KEY_FIT_TYPE,mRecentType);
            setResult(RESULT_OK, resultIntent);
        }
        finishAfterTransition();*/
    }

    @Override
    public void onItemSelected(int pos, long id, String title, long resid, String identifier) {
        if (pos == R.id.btn_recycle_item_delete){
            final WorkoutSet set = mSessionViewModel.getWorkoutSetById(id,title,identifier);
            if (set != null) {
                if (userPrefs.getConfirmDeleteSession()){
                    final ICustomConfirmDialog callback = new ICustomConfirmDialog() {
                        @Override
                        public void onCustomConfirmButtonClicked(int question, int button) {
                            if (button > 0) {
                                mSessionViewModel.deleteWorkoutSet(set);
                                broadcastToast(getString(R.string.action_delete));
                            }
                        }
                        @Override
                        public void onCustomConfirmDetach() {

                        }
                    };
                    CustomAlertDialog alertDialog = CustomAlertDialog.newInstance(QUESTION_KEEP_DELETE, getString(R.string.action_delete_set), callback);
                    alertDialog.setCancelable(true);
                    alertDialog.show(getSupportFragmentManager(), CustomAlertDialog.class.getSimpleName());
                }else {
                    mSessionViewModel.deleteWorkoutSet(set);
                    broadcastToast(getString(R.string.action_delete));
                }
            }
        }
        if (pos == R.id.btn_recycle_item_copy){
            if (returnResultFlag > 0) {
                Intent resultIntent  = new Intent(Constants.INTENT_TEMPLATE_START);
                resultIntent.putExtra(KEY_FIT_WORKOUTID, id);
                resultIntent.putExtra(KEY_FIT_WORKOUT_SETID, resid);
                resultIntent.putExtra(KEY_FIT_USER, title);
                resultIntent.putExtra(KEY_FIT_DEVICE_ID, identifier);
                resultIntent.putExtra(Constants.KEY_FIT_ACTIVITYID,resid); // activityID
                resultIntent.putExtra(Constants.MAP_DATA_TYPE,OBJECT_TYPE_WORKOUT_SET);
                resultIntent.putExtra(Constants.KEY_FIT_TYPE,mRecentType);
                setResult(RESULT_OK, resultIntent);
            }
            finishAfterTransition();
        }
        if ((pos == Constants.UID_btn_recycle_item_report) || (pos == Constants.UID_btn_recycle_item_select)){
            if (returnResultFlag > 0) {
                Intent resultIntent  = new Intent((pos == Constants.UID_btn_recycle_item_report) ? Constants.INTENT_WORKOUT_REPORT: Constants.INTENT_TEMPLATE_START);
                resultIntent.putExtra(KEY_FIT_WORKOUTID, id);
                resultIntent.putExtra(KEY_FIT_USER, title);
                resultIntent.putExtra(KEY_FIT_DEVICE_ID, identifier);
                resultIntent.putExtra(Constants.KEY_FIT_ACTIVITYID,resid); // activityID
                resultIntent.putExtra(Constants.MAP_DATA_TYPE,OBJECT_TYPE_WORKOUT);
                resultIntent.putExtra(Constants.KEY_FIT_TYPE,mRecentType);
                setResult(RESULT_OK, resultIntent);
            }
            finishAfterTransition();
        }


    }
    private void doLoadFromData(){
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (mRecentType == -1)actionBar.setTitle(simpleDateFormat.format(startTime));
            if (mRecentType == 0)actionBar.setTitle(simpleDateFormat.format(startTime));
            if (mRecentType == 3) actionBar.setTitle(getString(R.string.label_template));
            if (mRecentType == 1) actionBar.setTitle(getString(R.string.action_last_30_days));
            if (mRecentType == 2) actionBar.setTitle(getString(R.string.action_per_hour));
        }

        List<Configuration> deviceList = mSessionViewModel.getConfigurationLikeName(Constants.KEY_DEVICE1, sUserId);
        if ((deviceList != null) && (deviceList.size() > 0)) sDeviceID = deviceList.get(0).stringValue;
        // history
        if (mRecentType <= 0) {
            fragment = RecentFragment.create(sUserId,sDeviceID,mRecentType, startTime, endTime);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.placeholder, fragment, RecentFragment.TAG);
            transaction.commit();
        }
        // template
        if (mRecentType == 3) {
            fragment = RecentFragment.create(sUserId,sDeviceID,mRecentType, startTime, endTime);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.placeholder, fragment, RecentFragment.TAG);
            transaction.commit();
        }
        if (mRecentType == 1) {
            dailyFlag = 1;
            dailyChart = new DailyBPMRangeChart();
            mCalendar.setTimeInMillis(Utilities.getDayEnd(mCalendar, startTime));
            endTime = mCalendar.getTimeInMillis();
            mCalendar.add(Calendar.DAY_OF_YEAR, -31);
            startTime = mCalendar.getTimeInMillis();
            dailyChart.setUserID(sUserId);
            dailyChart.setStartFrom(startTime);
            dailyChart.setPerDay(dailyFlag);
            String sTemp = userPrefs.getPrefStringByLabel(INTENT_PERMISSION_AGE);
            if ((sTemp == null) || (sTemp.length() == 0)) sTemp = "30";
            dailyChart.setAgeValue(Integer.parseInt(sTemp));
            DateTuple tuple = mSessionViewModel.getUserDailyTotalsTuple(sUserId,startTime,endTime,1);
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_24Full, Locale.getDefault());
            if (tuple != null && tuple.sync_count > 0){
                Log.e(RecentActivity.TAG, "existing 30 days " + tuple.sync_count + " " + dateFormat.format(startTime) + " end " + dateFormat.format(endTime));
                mHandler.post(() -> {
                    userDailyTotalsList = mSessionViewModel.getUserDailyTotals(sUserId,startTime,endTime,1);
                    DisplayMetrics metrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    int densityDpi = metrics.densityDpi;
                    dailyChart.setUserDailyTotalsList(userDailyTotalsList);
                    final GraphicalView graphicalView = dailyChart.getView(getApplicationContext(), densityDpi);
                    if (graphicalView != null) {
                        final FrameLayout frameLayout = findViewById(R.id.placeholder);
                        runOnUiThread(() -> {
                            frameLayout.removeAllViews();
                            frameLayout.addView(graphicalView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        });
                    }

                });
            }else {
                Workout w = new Workout();
                w.start = startTime;
                w.end = endTime;
                Log.e(RecentActivity.TAG, "Google start 30 days " + dateFormat.format(startTime) + " end " + dateFormat.format(endTime));
                Intent mIntent = new Intent(getApplicationContext(), FitSessionJobIntentService.class);
                mIntent.putExtra(FitSessionJobIntentService.ARG_ACTION_KEY, Constants.TASK_ACTION_READ_BPM);
                mIntent.putExtra(FitSessionJobIntentService.ARG_START_KEY, startTime);
                mIntent.putExtra(FitSessionJobIntentService.ARG_END_KEY, endTime);
                mIntent.putExtra(FitSessionJobIntentService.ARG_RESULT_KEY, localResultReceiver);
                mIntent.putExtra(Constants.KEY_FIT_VALUE, 1440);  // minutes in a day
                mIntent.putExtra(KEY_FIT_USER, w.userID);
                mIntent.putExtra(KEY_FIT_DEVICE_ID, w.deviceID);
                mIntent.putExtra(Workout.class.getSimpleName(), w);
                try {
                    FitSessionJobIntentService.enqueueWork(getApplicationContext(), mIntent);
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        }
        if (mRecentType == 2){
            dailyFlag = 0;
            dailyChart = new DailyBPMRangeChart();
            lineChart = new AverageBPMLineChart();
            String sTemp = userPrefs.getPrefStringByLabel(INTENT_PERMISSION_AGE);
            if ((sTemp == null) || (sTemp.length() == 0)) sTemp = "30";
            dailyChart.setAgeValue(Integer.parseInt(sTemp));
            lineChart.setAgeValue(Integer.parseInt(sTemp));
            userDailyTotalsList = new ArrayList<>();
            endTime = System.currentTimeMillis();
            if (Utilities.getDayEnd(mCalendar,startTime) < endTime)
                endTime = Utilities.getDayEnd(mCalendar,startTime);

            dailyChart.setPerDay(dailyFlag);
            dailyChart.setUserID(sUserId);
            dailyChart.setStartFrom(startTime);
            Workout w = new Workout();
            w.start = startTime;
            w.end = endTime;
            Intent mIntent = new Intent(getApplicationContext(), FitSessionJobIntentService.class);
            mIntent.putExtra(FitSessionJobIntentService.ARG_ACTION_KEY, Constants.TASK_ACTION_READ_BPM);
            mIntent.putExtra(FitSessionJobIntentService.ARG_START_KEY, startTime);
            mIntent.putExtra(FitSessionJobIntentService.ARG_END_KEY, endTime);
            mIntent.putExtra(FitSessionJobIntentService.ARG_RESULT_KEY, localResultReceiver);
            mIntent.putExtra(Constants.KEY_FIT_VALUE, 60);
            mIntent.putExtra(KEY_FIT_USER, w.userID);
            mIntent.putExtra(KEY_FIT_DEVICE_ID, w.deviceID);
            mIntent.putExtra(Workout.class.getSimpleName(), w);
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_24Full, Locale.getDefault());
            Log.e(RecentActivity.class.getSimpleName(), "start " + dateFormat.format(startTime) + " end " + dateFormat.format(endTime)
                    + "interval " + mIntent.getIntExtra(Constants.KEY_FIT_VALUE, 0));
            try {
                FitSessionJobIntentService.enqueueWork(getApplicationContext(), mIntent);
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
    }

    private void broadcastToast(String msg){
        Intent msgIntent = new Intent(INTENT_MESSAGE_TOAST);
        msgIntent.putExtra(INTENT_EXTRA_MSG, msg);
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean bDayMode = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_NO);
        Context context = getApplicationContext();
        if (bDayMode)
            msgIntent.putExtra(KEY_FIT_TYPE, 2);
        else
            msgIntent.putExtra(KEY_FIT_TYPE, 1);
       // LocalBroadcastManager.getInstance(context).sendBroadcast(msgIntent);
       context.sendBroadcast(msgIntent);
    }
}
