package com.a_track_it.fitdata.fragment;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;

import com.a_track_it.fitdata.R;
import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.data_model.WorkoutRepository;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.a_track_it.fitdata.reports.DailyBPMRangeChart;

import java.util.Calendar;

import static com.a_track_it.fitdata.common.Constants.LABEL_PROFILE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GraphFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GraphFragment extends Fragment {
    public static final String ARG_REPORT_TYPE = "report_type";

    public static final String ARG_WORKOUT_TITLE = "workout_title";
    public static final String ARG_GROUP_COUNT = "group_count";
    private String mTitle;
    private int reportType;

    private long startTime;
    private WorkoutRepository mRepository;
    private String sUserID;
    private String sDeviceID;

    public GraphFragment() {
        // Required empty public constructor
    }

    public static GraphFragment newInstance(String sUser,String sDevice, int reportVariety,String title) {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_REPORT_TYPE, reportVariety);
        args.putString(ARG_WORKOUT_TITLE, title);
        args.putString(Constants.KEY_FIT_USER, sUser);
        args.putString(Constants.KEY_FIT_DEVICE_ID, sDevice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reportType = getArguments() != null ? getArguments().getInt(ARG_REPORT_TYPE) : 1;

        mTitle = getArguments() != null ? getArguments().getString(ARG_WORKOUT_TITLE) : Constants.ATRACKIT_EMPTY;
        if (getArguments() != null) {
            sUserID = getArguments().getString(Constants.KEY_FIT_USER);
            sDeviceID = getArguments().getString(Constants.KEY_FIT_DEVICE_ID);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        FrameLayout mChartLayout = rootView.findViewById(R.id.chart);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int densityDpi = metrics.densityDpi;

        if (reportType == 2) {
            DailyBPMRangeChart dailyChart = new DailyBPMRangeChart();
            Calendar calendar = Calendar.getInstance();
            dailyChart.setUserID(sUserID);
            dailyChart.setPerDay(1);
            dailyChart.setStartFrom(calendar.getTimeInMillis());
            UserPreferences userPrefs = UserPreferences.getPreferences(getContext(), sUserID);
            String sTemp = userPrefs.getPrefStringByLabel(LABEL_PROFILE);
            if ((sTemp == null) || (sTemp.length() == 0)) sTemp = "30";
            dailyChart.setAgeValue(Integer.parseInt(sTemp));
            dailyChart.setDisplayMetrics(densityDpi);
            mChartLayout.addView(dailyChart.getView(getContext(), densityDpi));
        }
        return rootView;
    }
}