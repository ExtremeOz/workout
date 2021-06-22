package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.InjectorUtils;
import com.a_track_it.fitdata.common.ReferencesTools;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.Configuration;
import com.a_track_it.fitdata.common.data_model.Workout;
import com.a_track_it.fitdata.common.data_model.WorkoutViewModel;
import com.a_track_it.fitdata.common.service.CacheResultReceiver;
import com.a_track_it.fitdata.reports.BaseReportGraph;
import com.a_track_it.fitdata.reports.MultipleLineGraphs;
import com.a_track_it.fitdata.reports.SingleBarGraphWithGoal;
import com.google.android.gms.fitness.data.DataType;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;
import org.achartengine.tools.PanListener;
import org.achartengine.tools.ZoomEvent;
import org.achartengine.tools.ZoomListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.a_track_it.fitdata.common.Constants.ATRACKIT_EMPTY;


/**
 * Created by Chris Black
 */
public class ReportsFragment extends androidx.fragment.app.Fragment implements CacheResultReceiver.Receiver{
    private static final String LOG_TAG = ReportsFragment.class.getSimpleName();
    public static final String ARG_REPORT_TYPE = "report_type";
    public static final String ARG_WORKOUT_TYPE = "workout_type";
    public static final String ARG_WORKOUT_TITLE = "workout_title";
    public static final String ARG_GROUP_COUNT = "group_count";
    public static final String TAG = "ReportsFragment";

    private List<Configuration> goalList = new ArrayList<>();
    private BaseReportGraph reportGraph;
    private int multiplier = 1;
    private int numDays = 45;
    private int numSegments;
    private long millisecondsInSegment;
    private ReferencesTools mReferenceTools;
    private Calendar mCalendar;

    /** The chart view that displays the data. */
    private GraphicalView mChartView;
    private String mTitle;
    private int reportType;
    private long workoutType;
    private long startTime;
    private long endTime;
    private WorkoutViewModel mSessionViewModel;
    private String sUserID;
    private String sDeviceID;
    FrameLayout mChartLayout;
    long currentTimeStamp;
    int densityDpi = 0;
    int month_of_year;
    int week_of_year;

    public static ReportsFragment newInstance(String sUser,String sDevice, long startTime, long endTime,
                                              int reportVariety, long workoutType, int groupCount, String title) {
        ReportsFragment f = new ReportsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_REPORT_TYPE, reportVariety);
        args.putLong(ARG_WORKOUT_TYPE, workoutType);
        args.putString(ARG_WORKOUT_TITLE, title);
        args.putInt(ARG_GROUP_COUNT, groupCount);
        args.putString(Constants.KEY_FIT_USER, sUser);
        args.putString(Constants.KEY_FIT_DEVICE_ID, sDevice);
        args.putLong(Constants.MAP_START, startTime);
        args.putLong(Constants.MAP_END, endTime);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        densityDpi = metrics.densityDpi;
        if (reportGraph != null) {
            reportGraph.setDisplayMetrics(metrics.densityDpi);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = requireContext().getApplicationContext();
        mCalendar = Calendar.getInstance();
        reportType = getArguments() != null ? getArguments().getInt(ARG_REPORT_TYPE) : 1;
        workoutType = getArguments() != null ? getArguments().getLong(ARG_WORKOUT_TYPE) : 0;
        mTitle = getArguments() != null ? getArguments().getString(ARG_WORKOUT_TITLE) : Constants.ATRACKIT_EMPTY;
        multiplier = getArguments() != null ? getArguments().getInt(ARG_GROUP_COUNT) : 1;
        sUserID = getArguments().getString(Constants.KEY_FIT_USER);
        sDeviceID = getArguments().getString(Constants.KEY_FIT_DEVICE_ID);
        startTime = getArguments().getLong(Constants.MAP_START);
        endTime = getArguments().getLong(Constants.MAP_END);
        mCalendar.setTimeInMillis(startTime);
        month_of_year = mCalendar.get(Calendar.MONTH);
        week_of_year = mCalendar.get(Calendar.WEEK_OF_YEAR);
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            reportGraph = (BaseReportGraph) savedInstanceState.getSerializable("reportGraph");
        }
        if (multiplier > 0) {
            numDays = 45;
            numSegments = (int) Math.ceil((double) numDays / (double) multiplier);
            millisecondsInSegment = 1000 * 60 * 60 * 24 * multiplier;  // 24 hours
        }else{
            numDays = 1;
            numSegments = 24;  // 24 hours
            millisecondsInSegment = 1000*60*60;  // 1 hour
        }

        if (workoutType == Constants.WORKOUT_TYPE_TIME) {
            reportGraph = new MultipleLineGraphs();
        } else {
            reportGraph = new SingleBarGraphWithGoal();
        }
        reportGraph.setType(reportType);
        reportGraph.setDisplayMetrics(densityDpi);

        mReferenceTools = ReferencesTools.setInstance(context);
        mSessionViewModel = new ViewModelProvider(requireActivity(), InjectorUtils.getWorkoutViewModelFactory(context)).get(WorkoutViewModel.class);
        if (sUserID != null){
            Configuration config = new Configuration(DataType.TYPE_HEART_POINTS.getName(), sUserID, ATRACKIT_EMPTY, 0L,null,null);
            List<Configuration> goalsList = mSessionViewModel.getConfiguration(config,sUserID);
            if ((goalsList != null) && (goalsList.size() > 0)) {
                goalsList.add(goalsList.get(0));
            }
            config = new Configuration(DataType.TYPE_MOVE_MINUTES.getName(), sUserID, ATRACKIT_EMPTY, 0L,null,null);
            List<Configuration> moveList = mSessionViewModel.getConfiguration(config,sUserID);
            if ((moveList != null) && (moveList.size() > 0)) {
                goalsList.add(moveList.get(0));
            }
            config = new Configuration(DataType.TYPE_STEP_COUNT_DELTA.getName(), sUserID, ATRACKIT_EMPTY, 0L,null,null);
            List<Configuration> stepsList = mSessionViewModel.getConfiguration(config,sUserID);
            if ((stepsList != null) && (stepsList.size() > 0)) {
                goalsList.add(stepsList.get(0));
            }
        }
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CONTENT, ReportsFragment.class.getSimpleName());
        params.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(workoutType));
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, String.valueOf(mTitle));
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_report, container, false);
        currentTimeStamp = mCalendar.getTimeInMillis();
        if (endTime == 0) endTime = System.currentTimeMillis();
        DateFormat simpleDateFormat =  SimpleDateFormat.getDateTimeInstance();
        Log.e(TAG, "Workout activityID:" + workoutType + " start " + simpleDateFormat.format(startTime) + " end " + simpleDateFormat.format(endTime));
        mChartView = reportGraph.getChartGraph(getActivity());
        mChartView.addZoomListener(new ZoomListener() {
            public void zoomApplied(ZoomEvent e) {
                updateLabels();
            }

            public void zoomReset() {
            }
        }, true, true);
        mChartView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // handle the click event on the chart
                SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                if (seriesSelection != null)
                    if ((workoutType == Constants.WORKOUT_TYPE_STEPCOUNT) || Utilities.isCardioWorkout(workoutType) || Utilities.isRunning(workoutType)){
                    // display information of the clicked point
                        Toast.makeText(
                                getActivity(),
                            Constants.ATRACKIT_EMPTY + reportGraph.getDataAtPoint(seriesSelection.getXValue()) + " steps", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(
                                getActivity(),
                                Constants.ATRACKIT_EMPTY + reportGraph.getDataAtPoint(seriesSelection.getXValue()) + " mins", Toast.LENGTH_SHORT).show();
                    }
            }
        });
        mChartView.addPanListener(new PanListener() {
            @Override
            public void panApplied() {
                updateLabels();
            }
        });
        mChartLayout = (FrameLayout)view.findViewById(R.id.chart);
        if (mChartLayout != null) mChartLayout.addView(mChartView,new ViewGroup.LayoutParams (
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    public void setReportType(int viewType){
        reportType = viewType;
    }
    public void setStartTime(long starting){ startTime = starting;}
    public void setEndTime(long ending){ endTime = ending;}
    public void setGroupCount(int groupCount) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int month = calendar.get(Calendar.MONTH);
        multiplier = groupCount;

        if (multiplier == -1) {  // by hour
            numDays = 1;
            numSegments = 24;
            millisecondsInSegment = 1000*60*60;
        }
        if (multiplier == 1) {  // by day
            numDays = 45;
            numSegments = (int)Math.ceil((double) (numDays) / (double) multiplier);
            millisecondsInSegment = 1000*60*60*24*multiplier;   // how many days
        }
        if (multiplier == 7){
            numDays = 150 + day;
            numSegments = (int)Math.ceil((double) (numDays) / (double) multiplier);
            millisecondsInSegment = 1000*60*60*24*multiplier;   // how many days in week
        }
        if (multiplier == 30){
            numDays = 180;
            numSegments = 6; // (int)Math.ceil((double) (numDays) / (double) multiplier);
            millisecondsInSegment = 1000*60*60*24*multiplier;   // 28 days in a month
        }
        Log.d(TAG, "GroupCount: " + groupCount + " Number of days: " + numDays);
       // showData();
    }

    private void updateLabels() {
        double start = reportGraph.getRenderer().getXAxisMin();
        double stop = reportGraph.getRenderer().getXAxisMax();
        double YStop = reportGraph.getRenderer().getYAxisMax();
        double quarterStep = (stop - start) / 8;
        double halfStep = (stop - start) / 2;
        reportGraph.getRenderer().clearXTextLabels();
        reportGraph.getRenderer().clearYTextLabels();
        long index = numSegments - normalize((int) (start + quarterStep)) - 1;
        if (multiplier > 0) {
            if ((multiplier == 1) || (multiplier == 7)) {
                reportGraph.getRenderer().addXTextLabel(start + quarterStep, Utilities.getDayString(currentTimeStamp - index * millisecondsInSegment));
                index = numSegments - normalize((int) (start + halfStep)) - 1;
                reportGraph.getRenderer().addXTextLabel(start + halfStep, Utilities.getDayString(currentTimeStamp - index * millisecondsInSegment));
                index = numSegments - normalize((int) (stop - quarterStep)) - 1;
                reportGraph.getRenderer().addXTextLabel(stop - quarterStep, Utilities.getDayString(currentTimeStamp - index * millisecondsInSegment));
            }
            if (multiplier == 30){
                index = numSegments - normalize((int) (start)) - 1;
                reportGraph.getRenderer().addXTextLabel(start + quarterStep, Utilities.getMonthString(currentTimeStamp - index * millisecondsInSegment));
            }
        }else{  // per hour
            reportGraph.getRenderer().addXTextLabel(start + quarterStep, Utilities.getTimeString(currentTimeStamp - index * millisecondsInSegment));
            index = numSegments - normalize((int) (start + halfStep)) - 1;
            reportGraph.getRenderer().addXTextLabel(start + halfStep, Utilities.getTimeString(currentTimeStamp - index * millisecondsInSegment));
            index = numSegments - normalize((int) (stop - quarterStep)) - 1;
            reportGraph.getRenderer().addXTextLabel(stop - quarterStep, Utilities.getTimeString(currentTimeStamp - index * millisecondsInSegment));
        }
        if ((workoutType == Constants.WORKOUT_TYPE_STEPCOUNT) || Utilities.isCardioWorkout(workoutType) || Utilities.isRunning(workoutType))
            reportGraph.getRenderer().addYTextLabel(YStop, "Steps");
        else
            reportGraph.getRenderer().addYTextLabel(YStop, "Duration");
    }

    private int normalize(int index) {
        int result = index > 0 ? index : 0;
        result = result < numSegments ? result : numSegments - 1;
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mChartView != null) {
            mChartView.repaint();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
      //  showData();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the current data, for instance when changing screen orientation
        outState.putSerializable("reportGraph", reportGraph);
    }



    public void showData() {
        Map<Long, Integer[]> map = new HashMap<>();
        reportGraph.clearData();
        if ((workoutType == Constants.WORKOUT_TYPE_STEPCOUNT) || Utilities.isCardioWorkout(workoutType) || Utilities.isRunning(workoutType)) {
            if (goalList.size() > 0)
                for (Configuration config : goalList){
                    if (config.stringName.equals(DataType.TYPE_STEP_COUNT_DELTA.getName()) && (config.stringValue != null)){
                        try {
                            float fSteps = Float.parseFloat(config.stringValue);
                            reportGraph.setGoal(Math.round(fSteps) * multiplier);
                        }catch(NumberFormatException ne){
                            reportGraph.setGoal(5000 * multiplier);
                        }
                    }
                }
        } else {
            // TODO: Create system for managing goals
           // reportGraph.setGoal(15 * multiplier);
        }
        Date now = new Date();
        if (multiplier > 0)
            mCalendar.setTime(now);
        else
            mCalendar.setTimeInMillis(startTime);
        if (multiplier > 1) {
            if (multiplier == 30)
                mCalendar.set(Calendar.DAY_OF_MONTH, 1);
            else
                mCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }
        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);
        long endTime = mCalendar.getTimeInMillis();
        mCalendar.add(Calendar.DAY_OF_YEAR, -numDays + 1);        // 30 days of history
        long startTime = mCalendar.getTimeInMillis();
        long baseline = (startTime - startTime % millisecondsInSegment) / millisecondsInSegment;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-dd HH:mm:ss z", Locale.getDefault());
            Log.w(LOG_TAG,"showData " + reportType + " activityID " + workoutType + " multi " + multiplier + " " + simpleDateFormat.format(startTime) + " e " + simpleDateFormat.format(endTime));

            List<Workout> itr = mSessionViewModel.getWorkoutsByTimesNow(sUserID,sDeviceID,startTime,endTime);
            for (Workout workout : itr) {
                mCalendar.setTimeInMillis(workout.start);
                long id = (workout.start - workout.start % millisecondsInSegment) / millisecondsInSegment - baseline;
                if (multiplier == -1){  // per hour

                }
                if ((multiplier == 7)) {
                    id = numSegments - (week_of_year - mCalendar.get(Calendar.WEEK_OF_YEAR)) - 1;
                }
                if (multiplier == 30) {
                    id = numSegments - (month_of_year - mCalendar.get(Calendar.MONTH));
                }

                if (id < numSegments && id >= 0) {
                    if (workoutType == Constants.WORKOUT_TYPE_TIME) {
                        // Put all data here to show totals
                        if (map.get(workoutType) == null) {
                            Integer[] dataMap = new Integer[numSegments];
                            Arrays.fill(dataMap, 0);
                            dataMap[(int) id] = (int) (workout.duration / 1000 / 60);
                            map.put(workoutType, dataMap);
                        } else {
                            Integer[] dataMap = map.get(workoutType);
                            dataMap[(int) id] += (int) (workout.duration / 1000 / 60);
                        }

                        if (map.get(workout.activityID) == null) {
                            Integer[] dataMap = new Integer[numSegments];
                            Arrays.fill(dataMap, 0);
                            dataMap[(int) id] = (int) (workout.duration / 1000 / 60);
                            map.put(workout.activityID, dataMap);
                        } else {
                            Integer[] dataMap = map.get(workout.activityID);
                            dataMap[(int) id] += (int) (workout.duration / 1000 / 60);
                        }
                    } else if (workout.activityID == workoutType) {
                        if (map.get(workout.activityID) == null) {
                            Integer[] dataMap = new Integer[numSegments];
                            Arrays.fill(dataMap, 0);
                            if ((workout.activityID == Constants.WORKOUT_TYPE_STEPCOUNT)|| Utilities.isCardioWorkout(workout.activityID) || Utilities.isRunning(workout.activityID)) {
                                dataMap[(int) id] = workout.stepCount;
                            } else {
                                dataMap[(int) id] = (int) (workout.duration / 1000 / 60);
                            }

                            map.put(workout.activityID, dataMap);
                        } else {
                            Integer[] dataMap = map.get(workout.activityID);
                            if ((workout.activityID == Constants.WORKOUT_TYPE_STEPCOUNT)|| Utilities.isCardioWorkout(workout.activityID) || Utilities.isRunning(workout.activityID))  {
                                dataMap[(int) id] += workout.stepCount;
                            } else {
                                dataMap[(int) id] += (int) (workout.duration / 1000 / 60);
                            }
                        }
                    }
                }
            }
        }catch (Exception e){ Log.e(TAG, e.getMessage());}

        int series = 0;
        for (Long workoutType : map.keySet()) {
            int color = ContextCompat.getColor(this.getActivity(),R.color.other_graph);
            if (workoutType == Constants.WORKOUT_TYPE_WALKING) {
                color = ContextCompat.getColor(this.getActivity(),R.color.walking_graph);
            } else if (workoutType == Constants.WORKOUT_TYPE_RUNNING) {
                color = ContextCompat.getColor(this.getActivity(),R.color.running_graph);
            } else if (workoutType == Constants.WORKOUT_TYPE_BIKING) {
                color = ContextCompat.getColor(this.getActivity(),R.color.biking_graph);
            } else if (workoutType == Constants.WORKOUT_TYPE_STRENGTH) {
                color = ContextCompat.getColor(this.getActivity(),R.color.lifting_graph);
            } else if (workoutType == Constants.WORKOUT_TYPE_ARCHERY) {
                color = ContextCompat.getColor(this.getActivity(),R.color.archery_graph);
            }
            reportGraph.addRenderer(series, getActivity(), color);
            Integer[] dataMap = map.get(workoutType);
            for (int n = 0; n < numSegments; n++) {
                reportGraph.addWorkout(series, dataMap[n], n);
            }
            series++;
        }

        reportGraph.updateRenderer();
        updateLabels();
        mChartView.repaint();
    }

}
