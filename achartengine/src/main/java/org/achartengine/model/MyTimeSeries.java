package org.achartengine.model;

import java.util.Date;

public class MyTimeSeries extends XYSeries {
    /**
     * Builds a new date/time series.
     *
     * @param title
     * the series title
     */
    public MyTimeSeries (String title) {
        super (title);
    }

    public MyTimeSeries (String title, int scaleNumber) {
        super (title, scaleNumber);//In fact, compared to the original TimeSeries, only this constructor is added
    }
    /**
     * Adds a new value to the series.
     *
     * @param x
     * the date/time value for the X axis
     * @param y
     * the value for the Y axis
     */
    public synchronized void add (Date x, double y) {
        super.add (x.getTime (), y);
    }

    protected double getPadding (double x) {
        return 1;
    }
}
