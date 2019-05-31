package com.a_track_it.fitdata.fragment;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
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

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.model.Utilities;
import com.a_track_it.fitdata.common.model.Workout;
import com.a_track_it.fitdata.database.SimpleDBHelper;
import com.a_track_it.fitdata.reports.BaseReportGraph;
import com.a_track_it.fitdata.reports.MultipleLineGraphs;
import com.a_track_it.fitdata.reports.SingleBarGraphWithGoal;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;
import org.achartengine.tools.PanListener;
import org.achartengine.tools.ZoomEvent;
import org.achartengine.tools.ZoomListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by Chris Black
 */
@SuppressWarnings("WeakerAccess") // Butterknife requires public reference of injected views
public class ReportsFragment extends BaseFragment {

    public static final String ARG_WORKOUT_TYPE = "workout_type";
    public static final String ARG_WORKOUT_TITLE = "workout_title";
    public static final String ARG_GROUP_COUNT = "group_count";
    public static final String TAG = "ReportsFragment";

    private BaseReportGraph reportGraph;
    private int multiplier = 1;
    private int numDays = 45;
    private int numSegments;
    private long millisecondsInSegment;

    /** The chart view that displays the data. */
    private GraphicalView mChartView;
    private String mTitle;
    private int workoutType;
    @BindView(R.id.chart) FrameLayout mChartLayout;

    public static ReportsFragment newInstance(int workoutType, int groupCount, String title) {
        ReportsFragment f = new ReportsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_WORKOUT_TYPE, workoutType);
        args.putString(ARG_WORKOUT_TITLE, title);
        args.putInt(ARG_GROUP_COUNT, groupCount);
        f.setArguments(args);
        return f;
    }

    int densityDpi = 0;

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
        workoutType = getArguments() != null ? getArguments().getInt(ARG_WORKOUT_TYPE) : 0;
        mTitle = getArguments() != null ? getArguments().getString(ARG_WORKOUT_TITLE) : "";
        multiplier = getArguments() != null ? getArguments().getInt(ARG_GROUP_COUNT) : 1;
        numSegments = (int)Math.ceil((double)numDays / (double)multiplier);
        millisecondsInSegment = 1000*60*60*24*multiplier;
        Log.i(TAG, "Workout regionID:" + workoutType);
        if (workoutType == Constants.WORKOUT_TYPE_TIME) {
            reportGraph = new MultipleLineGraphs();
        } else {
            reportGraph = new SingleBarGraphWithGoal();
        }
        if (workoutType == Constants.WORKOUT_TYPE_STEPCOUNT) {
            reportGraph.setGoal(9500 * multiplier);
        } else if (workoutType == Constants.WORKOUT_TYPE_WALKING){
            // TODO: Create system for managing goals
            reportGraph.setGoal(95 * multiplier);
        } else {
            // TODO: Create system for managing goals
            reportGraph.setGoal(15 * multiplier);
        }
        reportGraph.setDisplayMetrics(densityDpi);

        if(Answers.getInstance() != null) {
            // TODO: Crash here - keep an eye out

            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName("Report graph view")
                    .putContentType("View")
                    .putContentId("ReportsFragment")
                    .putCustomAttribute("Type Id", workoutType)
                    .putCustomAttribute("Type Name", mTitle));
        }
    }

    long currentTimeStamp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_report, container, false);

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        currentTimeStamp = cal.getTimeInMillis();
        ButterKnife.bind(this, view);


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
                if (seriesSelection == null) {
                    //Toast.makeText(getActivity(), "No chart element", Toast.LENGTH_SHORT).show();
                } else if(workoutType == Constants.WORKOUT_TYPE_STEPCOUNT){
                    // display information of the clicked point
                    Toast.makeText(
                            getActivity(),
                            "" + reportGraph.getDataAtPoint(seriesSelection.getXValue()) + " steps", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mChartView.addPanListener(new PanListener() {
            @Override
            public void panApplied() {
                updateLabels();
            }
        });
        if (mChartLayout == null) mChartLayout = (FrameLayout)view.findViewById(R.id.chart);
        mChartLayout.addView(mChartView);

        // Now return the SwipeRefreshLayout as this fragment's content view
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void setGroupCount(int groupCount) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (groupCount == 1) {
            numDays = 45;
        } else {
            numDays = 150 + day;
        }
        Log.d(TAG, "GroupCount: " + groupCount + " Number of days: " + numDays);
        multiplier = groupCount;
        numSegments = (int)Math.ceil((double) (numDays) / (double) multiplier);
        millisecondsInSegment = 1000*60*60*24*multiplier;
        if (workoutType == Constants.WORKOUT_TYPE_STEPCOUNT) {
            reportGraph.setGoal(9500 * multiplier);
        } else if (workoutType == Constants.WORKOUT_TYPE_WALKING){
            // TODO: Create system for managing goals
            reportGraph.setGoal(90 * multiplier);
        } else if (workoutType == Constants.WORKOUT_TYPE_BIKING){
            // TODO: Create system for managing goals
            reportGraph.setGoal(20 * multiplier);
        } else {
            // TODO: Create system for managing goals
            reportGraph.setGoal(10 * multiplier);
        }
        showData();
    }

    private void updateLabels() {
        double start = reportGraph.getRenderer().getXAxisMin();
        double stop = reportGraph.getRenderer().getXAxisMax();
        double quarterStep = (stop - start) / 8;
        double halfStep = (stop - start) / 2;
        reportGraph.getRenderer().clearXTextLabels();
        long index = numSegments - normalize((int) (start + quarterStep)) - 1;
        reportGraph.getRenderer().addXTextLabel(start + quarterStep, Utilities.getDayString(currentTimeStamp - index * millisecondsInSegment));
        index = numSegments - normalize((int) (start + halfStep)) - 1;
        reportGraph.getRenderer().addXTextLabel(start + halfStep, Utilities.getDayString(currentTimeStamp - index * millisecondsInSegment));
        index = numSegments - normalize((int) (stop - quarterStep)) - 1;
        reportGraph.getRenderer().addXTextLabel(stop - quarterStep, Utilities.getDayString(currentTimeStamp - index * millisecondsInSegment));
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
        showData();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        SimpleDBHelper.INSTANCE.close();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the current data, for instance when changing screen orientation
        outState.putSerializable("reportGraph", reportGraph);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            reportGraph = (BaseReportGraph) savedInstanceState.getSerializable("reportGraph");
        }
    }

    // TODO: Fix this ugly function
    public void showData() {
        Map<Integer, Integer[]> map = new HashMap<>();
        reportGraph.clearData();

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        int week_of_year = cal.get(Calendar.WEEK_OF_YEAR);
        if (multiplier == 7) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, -numDays + 1);        // 30 days of history
        long startTime = cal.getTimeInMillis();
        long baseline = (startTime - startTime % millisecondsInSegment) / millisecondsInSegment;
        SQLiteDatabase db = SimpleDBHelper.INSTANCE.open(this.getActivity().getApplicationContext());
        QueryResultIterable<Workout> itr = cupboard().withDatabase(db).query(Workout.class).withSelection("start >= ?", "" + startTime).query();
        for (Workout workout : itr) {
            cal.setTimeInMillis(workout.start);
            long id = (workout.start - workout.start % millisecondsInSegment) / millisecondsInSegment - baseline;
            if (multiplier == 7) {
                id = numSegments - (week_of_year - cal.get(Calendar.WEEK_OF_YEAR)) - 1;
            }

            if (id < numSegments && id >= 0) {

                if (workoutType == Constants.WORKOUT_TYPE_TIME && Utilities.isActiveWorkout(workout.activityID)) {
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
                        if (workout.activityID == Constants.WORKOUT_TYPE_STEPCOUNT) {
                            dataMap[(int) id] = workout.stepCount;
                        } else {
                            dataMap[(int) id] = (int) (workout.duration / 1000 / 60);
                        }

                        map.put(workout.activityID, dataMap);
                    } else {
                        Integer[] dataMap = map.get(workout.activityID);
                        if (workout.activityID == Constants.WORKOUT_TYPE_STEPCOUNT) {
                            dataMap[(int) id] += workout.stepCount;
                        } else {
                            dataMap[(int) id] += (int) (workout.duration / 1000 / 60);
                        }
                    }
                }
            }
        }
        itr.close();

        // TODO: END
        int series = 0;
        for (Integer workoutType : map.keySet()) {
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