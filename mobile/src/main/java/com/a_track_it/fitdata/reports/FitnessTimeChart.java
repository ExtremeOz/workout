package com.a_track_it.fitdata.reports;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.ScatterChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FitnessTimeChart extends AbstractTrackItChart {
    long mWorkoutID;
    int[] mColors;
    PointStyle[] styles;
    int iSet;
    int iBackgroundColor;
    int iUOY;
    String sXTitle;
    String sYTitle;
    double yMin;
    double yMax;
    double xMin;
    double xMax;
    List<double[]> values = new ArrayList<double[]>();
    List<String[]> titles = new ArrayList<String[]>();
    List<Date[]> dates = new ArrayList<Date[]>();
    List<String> xLabels = new ArrayList<>();
    String sTitle = "FitnessTimeChart";
    String sType = BarChart.TYPE;
    public FitnessTimeChart(){
        super(); iSet = 0;
    }
    public void setChartType(String s){ sType = s;}
    public void setBackgroundColor(int i){
        iBackgroundColor = i;
        iSet = 1;
    }

    public void setUOY(int uoy){ iUOY = uoy;}
    public void setYMin(double d){yMin = d;}
    public void setYMax(double d){yMax = d;}
    public void setXMin(double d){xMin = d;}
    public void setXMax(double d){xMax = d;}

    /**
     * Returns the chart name.
     *
     * @return the chart name
     */
    public String getName() {
        return "Sales growth";
    }

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    public String getDesc() {
        return "The sales growth across several years (time chart)";
    }
    public void setValuesList(List<double[]> v){
        values = v;
        iSet = 1;
    }

    public void addTitles(String[] vals){
        titles.add(vals);
        iSet = 1;
    }
    public void addDates(Date[] vals){
        dates.add(vals);
        iSet = 1;
    }
    public void addXTitle(String s){ sXTitle = s;}
    public void addYTitle(String s){ sYTitle = s;}
    public void addXLabels(List<String> labels){
        xLabels.addAll(labels);
    }
    public void addReportTitle(String s){
        sTitle = s;
    }
    public void addColors(int[] c){ mColors = c; }
    public void addStyles(PointStyle[] s){ styles = s;}
    /**
     * Executes the chart demo.
     *
     * @param context the context
     * @return the built intent
     */
    public Intent execute(Context context, int dpi) {
        setDisplayMetrics(dpi);
        if (iSet == 0) {
            String[] titles = new String[]{"Sales growth January 1995 to December 2000"};

            Date[] dateValues = new Date[]{new Date(95, 0, 1), new Date(95, 3, 1), new Date(95, 6, 1),
                    new Date(95, 9, 1), new Date(96, 0, 1), new Date(96, 3, 1), new Date(96, 6, 1),
                    new Date(96, 9, 1), new Date(97, 0, 1), new Date(97, 3, 1), new Date(97, 6, 1),
                    new Date(97, 9, 1), new Date(98, 0, 1), new Date(98, 3, 1), new Date(98, 6, 1),
                    new Date(98, 9, 1), new Date(99, 0, 1), new Date(99, 3, 1), new Date(99, 6, 1),
                    new Date(99, 9, 1), new Date(100, 0, 1), new Date(100, 3, 1), new Date(100, 6, 1),
                    new Date(100, 9, 1), new Date(100, 11, 1)};
            dates.add(dateValues);

            values.add(new double[]{4.9, 5.3, 3.2, 4.5, 6.5, 4.7, 5.8, 4.3, 4, 2.3, -0.5, -2.9, 3.2, 5.5,
                    4.6, 9.4, 4.3, 1.2, 0, 0.4, 4.5, 3.4, 4.5, 4.3, 4});
            mColors = new int[]{Color.BLUE};
            PointStyle[] styles = new PointStyle[]{PointStyle.POINT};
            XYMultipleSeriesRenderer renderer = buildRenderer(mColors, styles);
            setChartSettings(renderer, "Sales growth", "Date", "%", dateValues[0].getTime(),
                    dateValues[dateValues.length - 1].getTime(), -4, 11, Color.GRAY, Color.LTGRAY);
            renderer.setYLabels(10);
            renderer.setXRoundedLabels(false);
    /*        XYSeriesRenderer xyRenderer = (XYSeriesRenderer) renderer.getSeriesRendererAt(0);
            FillOutsideLine fill = new FillOutsideLine(FillOutsideLine.Type.BOUNDS_ABOVE);
            fill.setColor(Color.GREEN);
            xyRenderer.addFillOutsideLine(fill);
            fill = new FillOutsideLine(FillOutsideLine.Type.BOUNDS_BELOW);
            fill.setColor(Color.MAGENTA);
            xyRenderer.addFillOutsideLine(fill);
            fill = new FillOutsideLine(FillOutsideLine.Type.BOUNDS_ABOVE);
            fill.setColor(Color.argb(255, 0, 200, 100));
            fill.setFillRange(new int[] {10, 19});
            xyRenderer.addFillOutsideLine(fill);*/
            return ChartFactory.getTimeChartIntent(context, buildDateDataset(titles, dates, values),
                    renderer, "MMM yyyy");
        }else{
            PointStyle[] styles = new PointStyle[]{PointStyle.POINT};
            XYMultipleSeriesRenderer renderer = buildRenderer(mColors, styles);
            setChartSettings(renderer, sTitle, sXTitle, sYTitle,xMin,xMax, yMin, yMax, Color.GRAY, Color.LTGRAY);

            String sFormat = "dd";
            if (iUOY == 1) sFormat = "ww";
            if (iUOY == 2) sFormat = "M";
            return ChartFactory.getTimeChartIntent(context, buildDateDataset(titles.get(0), dates, values),
                    renderer, sFormat);

        }

    }

    @Override
    public GraphicalView getView(Context context, int dpi) {
        setDisplayMetrics(dpi);
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        setChartSettings(renderer, sTitle, sXTitle, sYTitle,xMin,xMax,yMin,yMax+5, Color.GRAY, Color.LTGRAY);
        String[] titlesArray = titles.get(0);
        renderer.setChartTitleTextSize(getDPI(20));
        renderer.setAxisTitleTextSize(getDPI(18));
        renderer.setLabelsTextSize(getDPI(10));
        renderer.setLegendTextSize(getDPI(14));
        renderer.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
        renderer.setBackgroundColor(iBackgroundColor);
        renderer.setMarginsColor(iBackgroundColor);
        renderer.setLabelsColor(Color.WHITE);
        renderer.setShowLabels(true);
        renderer.setDisplayValues(true);
        renderer.setLabelsTextSize(getDPI(11));
        renderer.setZoomEnabled(true);
        renderer.setZoomButtonsVisible(true);
        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Paint.Align.CENTER);
        renderer.setYLabelsAlign(Paint.Align.CENTER);
        renderer.setShowLegend(true);
        if (!BarChart.TYPE.equals(sType)) {
            renderer.setPointSize(4f);
        }else{
            renderer.setBarSpacing(0.5f);
            renderer.setBarWidth(12f);
        }
        int length = mColors.length;
        NumberFormat nf = NumberFormat.getIntegerInstance();
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(mColors[i]);
            r.setDisplayChartValues(true);
            r.setChartValuesTextSize(getDPI(11));
            r.setChartValuesSpacing(3);
            r.setFillPoints(true);
            if (BarChart.TYPE.equals(sType)) {
                r.setLineWidth(0f);
            }else{
                if (styles != null) r.setPointStyle(styles[i]);
                r.setLineWidth(5f);
                r.setPointStrokeWidth(4f);
            }
            r.setChartValuesTextAlign(Paint.Align.CENTER);
            r.setChartValuesFormat(nf);
            renderer.addSeriesRenderer(0,r);
        }
        if (BarChart.TYPE.equals(sType)) {
            renderer.setBarSpacing(0.5);
            renderer.setBarWidth(12f);
        }
        renderer.setXLabels(0);
        renderer.setPanEnabled(true);
        renderer.setPanLimits(new double[] { 0, xLabels.size(), yMin, yMax});
        renderer.setYLabels(5);
        renderer.setXLabels(0);
        for (int i=0; i < xLabels.size(); i++){
           renderer.addXTextLabel(i+1, xLabels.get(i));
        }
      //  renderer.setMargins(new int[] {getDPI(40), getDPI(25), getDPI(40), getDPI(20)});
        renderer.setMargins(new int[] {getDPI(20), getDPI(35), getDPI(40), getDPI(20)});
        String sFormat = "dd";
        if (iUOY == 1) sFormat = "ww";
        if (iUOY == 2) sFormat = "M";
    //    XYMultipleSeriesDataset dataSet = buildDateDataset(titles.get(0), dates, values);
      //  XYMultipleSeriesDataset dataSet2 = buildBarDataset(titles.get(0),values);
        List<double[]> x = new ArrayList<double[]>();
        int datesCount = dates.get(0).length;
        for (int i = 0; i < length; i++) {
            double[] xVals = new double[datesCount];
            for (int y=1; y <= datesCount; y++)
                xVals[y-1] = y;
            x.add(xVals);
        }
        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset(); // buildDataset(titles.get(0), x, values);
        for (int i = 0; i < length; i++) {
            XYSeries series = new XYSeries(titlesArray[i]);
            double[] xV = x.get(i);
            double[] yV = values.get(i);
            int seriesLength = xV.length;
            for (int k = 0; k < seriesLength; k++) {
                series.add(xV[k], yV[k]);
            }
            dataSet.addSeries(0,series);
        }
        // for each series add x values for dates length
        if (sType.equals(BarChart.TYPE))
            return ChartFactory.getBarChartView(context,dataSet,renderer, BarChart.Type.DEFAULT);
        if (sType.equals(ScatterChart.TYPE))
            return ChartFactory.getScatterChartView(context,dataSet,renderer);
        if (sType.equals(LineChart.TYPE))
            return ChartFactory.getLineChartView(context,dataSet,renderer);
        return null;

    }
}