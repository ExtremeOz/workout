package com.a_track_it.workout.reports;

import android.content.Context;
import android.content.Intent;

import org.achartengine.GraphicalView;

/**
 * Defines the demo charts.
 */
public interface ITrackItChart {
    /** A constant for the name field in a list activity. */
    String NAME = "name";
    /** A constant for the description field in a list activity. */
    String DESC = "desc";

    /**
     * Returns the chart name.
     *
     * @return the chart name
     */
    String getName();

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    String getDesc();

    /**
     * Executes the chart class using Factory to return intent.
     *
     * @param context the context
     * @return the built intent
     */
    Intent execute(Context context, int dpi);
    /**
     * Executes the chart GraphicalView.
     *
     * @param context the context
     * @return the built GraphicalView
     */
    GraphicalView getView(Context context, int dpi);

}
