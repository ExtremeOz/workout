package com.a_track_it.fitdata.reports;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;

import com.a_track_it.fitdata.R;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SetsStackedBarChart extends AbstractTrackItChart {
    List<double[]> values = new ArrayList<double[]>();
    List<String[]> titles = new ArrayList<String[]>();
    String sTitle = "Fitness Bar Chart";
    long mWorkoutID;
    int[] mColors;
    int iMetric;
    int iSet;
    int iBackgroundColor;

    public SetsStackedBarChart(){
        super();
        iSet = 0;
        iMetric = 0;
    }
    public long getWorkoutID(){ return this.mWorkoutID;}
    public void setWorkoutID(long ID){ this.mWorkoutID = ID;}

    public int getIsSet(){ return iSet;}
    public void setMetric(int iMet){ iMetric = iMet; }

    public void setBackgroundColor(int i){
        iBackgroundColor = i;
        iSet = 1;
    }

    public void setTitlesList(List<String[]> t){
        titles = t;
        iSet = 1;
    }
    public void setValuesList(List<double[]> v){
        values = v;
        iSet = 1;
    }
    public void addTitles(String[] vals){
        titles.add(vals);
        iSet = 1;
    }
    public void addValues(double[] vals){
        values.add(vals);
        iSet = 1;
    }
    public void addReportTitle(String s){
        sTitle = s;
    }
    public void addColors(int[] c){
        mColors = c;
    }
    /**
     * Returns the chart name.
     *
     * @return the chart name
     */
    public String getName() {
        return sTitle;
    }

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    public String getDesc() {
        return "The exercise and bodypart metics per workout (doughnut chart)";
    }

    @Override
    public GraphicalView getView(Context context, int dpi) {
        setDisplayMetrics(dpi);
        if (iSet == 0) {
            String[] titles = new String[]{"2008", "2007"};
            List<double[]> values = new ArrayList<double[]>();
            values.add(new double[]{14230, 12300, 14240, 15244, 15900, 19200, 22030, 21200, 19500, 15500,
                    12600, 14000});
            values.add(new double[]{5230, 7300, 9240, 10540, 7900, 9200, 12030, 11200, 9500, 10500,
                    11600, 13500});
            int[] colors = new int[]{Color.BLUE, Color.CYAN};
            XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
            setChartSettings(renderer, "Monthly sales in the last 2 years", "Month", "Units sold", 0.5,
                    12.5, 0, 24000, Color.GRAY, Color.LTGRAY);
            ((XYSeriesRenderer) renderer.getSeriesRendererAt(0)).setDisplayChartValues(true);
            ((XYSeriesRenderer) renderer.getSeriesRendererAt(1)).setDisplayChartValues(true);
            renderer.setXLabels(12);
            renderer.setYLabels(10);
            renderer.setXLabelsAlign(Paint.Align.LEFT);
            renderer.setYLabelsAlign(Paint.Align.LEFT);
            renderer.setPanEnabled(true, false);
            // renderer.setZoomEnabled(false);
            renderer.setZoomRate(1.1f);
            renderer.setBarSpacing(0.5f);
            return ChartFactory.getBarChartView(context, buildBarDataset(titles, values), renderer,
                    BarChart.Type.STACKED);
        }
        else{
            String[] metricTitles = this.titles.get(0);
            String[] loadTitles = new String[1];
            loadTitles[0] = metricTitles[iMetric];
            double[] plots = this.values.get(iMetric);
            List<double[]> loadValues = new ArrayList<>();
            loadValues.add(plots);
            double xMax = (double)plots.length;
            double yMax = 0d;
            for (int a=0; a < (int)xMax; a++) {
                if (plots[a] > yMax)
                    yMax = plots[a];
            }
            int loadColors[] = new int[1];
            int iColor = new Random().nextInt(mColors.length);
            loadColors[0] = mColors[iColor];
            XYMultipleSeriesRenderer renderer = buildBarRenderer(loadColors);
            setChartSettings(renderer, sTitle, "Sets", "", 0,
                    xMax, 0, yMax+5, Color.GRAY, Color.LTGRAY);
          //  ((XYSeriesRenderer) renderer.getSeriesRendererAt(0)).setDisplayChartValues(true);
            iBackgroundColor = context.getColor(R.color.primaryDarkColor);
            renderer.setXLabels((int)xMax);
            renderer.setBackgroundColor(iBackgroundColor);
            renderer.setMarginsColor(iBackgroundColor);
            renderer.setYLabels(10);
            renderer.setXLabelsAlign(Paint.Align.LEFT);
            renderer.setYLabelsAlign(Paint.Align.LEFT);
            renderer.setPanEnabled(true, false);
            renderer.setBarWidth(50f);
            renderer.setBarSpacing(0.5f);
            renderer.setLabelsColor(Color.WHITE);
            renderer.setShowLabels(true);
            renderer.setChartTitle(sTitle);
            renderer.setChartTitleTextSize(getDPI(20));
            renderer.setDisplayValues(true);
            renderer.setLabelsTextSize(getDPI(12));
            renderer.setZoomEnabled(true);
            renderer.setZoomButtonsVisible(true);
            renderer.setShowLegend(false);
            renderer.setBackgroundColor(iBackgroundColor);
            return ChartFactory.getBarChartView(context, buildBarDataset(loadTitles, loadValues), renderer,
                    BarChart.Type.DEFAULT);
        }
    }

    /**
         * Executes the chart demo.
         *
         * @param context the context
         * @return the built intent
         */
        public Intent execute(Context context, int dpi) {
            setDisplayMetrics(dpi);
            if (iSet == 0) {
                String[] titles = new String[]{"2008", "2007"};
                List<double[]> values = new ArrayList<double[]>();
                values.add(new double[]{14230, 12300, 14240, 15244, 15900, 19200, 22030, 21200, 19500, 15500,
                        12600, 14000});
                values.add(new double[]{5230, 7300, 9240, 10540, 7900, 9200, 12030, 11200, 9500, 10500,
                        11600, 13500});
                int[] colors = new int[]{Color.BLUE, Color.CYAN};
                XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
                setChartSettings(renderer, "Monthly sales in the last 2 years", "Month", "Units sold", 0.5,
                        12.5, 0, 24000, Color.GRAY, Color.LTGRAY);
                ((XYSeriesRenderer) renderer.getSeriesRendererAt(0)).setDisplayChartValues(true);
                ((XYSeriesRenderer) renderer.getSeriesRendererAt(1)).setDisplayChartValues(true);
                renderer.setXLabels(12);
                renderer.setYLabels(10);
                renderer.setXLabelsAlign(Paint.Align.LEFT);
                renderer.setYLabelsAlign(Paint.Align.LEFT);
                renderer.setPanEnabled(true, false);
                // renderer.setZoomEnabled(false);
                renderer.setZoomRate(1.1f);
                renderer.setBarSpacing(0.5f);
                return ChartFactory.getBarChartIntent(context, buildBarDataset(titles, values), renderer,
                        BarChart.Type.STACKED);
            }else{
                String[] titles = this.titles.get(iMetric);
                double[] plots = this.values.get(iMetric);
                List<double[]> loadValues = new ArrayList<>();
                loadValues.add(plots);
                double xMax = (double)plots.length;
                double yMax = 0d;
                for (int a=0; a < (int)xMax; a++) {
                    if (plots[a] > yMax)
                        yMax = plots[a];
                }
                XYMultipleSeriesRenderer renderer = buildBarRenderer(mColors);
                setChartSettings(renderer, sTitle, "Sets", sTitle, 0,
                        xMax, 0, yMax, Color.GRAY, Color.LTGRAY);
                ((XYSeriesRenderer) renderer.getSeriesRendererAt(0)).setDisplayChartValues(true);
                renderer.setXLabels((int)xMax);
                renderer.setYLabels(10);
                renderer.setXLabelsAlign(Paint.Align.LEFT);
                renderer.setYLabelsAlign(Paint.Align.LEFT);
                renderer.setPanEnabled(true, false);
                // renderer.setZoomEnabled(false);
                renderer.setZoomRate(1.1f);
                renderer.setBarSpacing(0.5f);
                return ChartFactory.getBarChartIntent(context, buildBarDataset(titles, loadValues), renderer,
                        BarChart.Type.DEFAULT);
        }
    }
}
