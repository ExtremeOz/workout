package com.a_track_it.fitdata.reports;

import android.app.Activity;

import org.achartengine.GraphicalView;

/**
 * Created by Chris Black
 *
 * Interface for report graphs
 */
public interface IReportGraph {
    GraphicalView getChartGraph(Activity activity);
    void clearData();
    void addWorkout(int series, int data, int position);
    void updateRenderer();
    void setType(int reportType);
    void setGoal(int goalValue);
    void setDisplayMetrics(int dpi);
    void addRenderer(int series, Activity activity, int color);
    double getDataAtPoint(double xPos);
}
