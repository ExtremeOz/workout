package com.a_track_it.fitdata.reports;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.a_track_it.fitdata.R;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.List;

public class StrengthSetBarChart extends AbstractTrackItChart {
    List<double[]> values = new ArrayList<double[]>();
    List<String[]> titles = new ArrayList<String[]>();
    String sTitle = "Fitness Bar Chart";
    int[] mColors;
    int iSet;
    int iBackgroundColor;
    int iMetrics;

    public StrengthSetBarChart(){
        super();
        iSet = 0;
        iMetrics = 0;
    }

    public void setMetrics(int i){
        iMetrics = i;
    }
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
    public Intent execute(Context context, int dpi) {
        setDisplayMetrics(dpi);
        if (iSet == 0){
            String[] titles = new String[] { "2007", "2008" };
            List<double[]> values = new ArrayList<double[]>();
            values.add(new double[] { 5230, 7300, 9240, 10540, 7900, 9200, 12030, 11200, 9500, 10500,
                    11600, 13500 });
            values.add(new double[] { 14230, 12300, 14240, 15244, 15900, 19200, 22030, 21200, 19500, 15500,
                    12600, 14000 });
            int[] colors = new int[] { Color.CYAN, Color.BLUE };
            XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
            renderer.setOrientation(XYMultipleSeriesRenderer.Orientation.VERTICAL);
            setChartSettings(renderer, "Monthly sales in the last 2 years", "Month", "Units sold", 0.5,
                    12.5, 0, 24000, Color.GRAY, Color.LTGRAY);
            renderer.setXLabels(1);
            renderer.setYLabels(10);
            renderer.addXTextLabel(1, "Jan");
            renderer.addXTextLabel(3, "Mar");
            renderer.addXTextLabel(5, "May");
            renderer.addXTextLabel(7, "Jul");
            renderer.addXTextLabel(10, "Oct");
            renderer.addXTextLabel(12, "Dec");
            int length = renderer.getSeriesRendererCount();
            for (int i = 0; i < length; i++) {
                XYSeriesRenderer seriesRenderer = (XYSeriesRenderer) renderer.getSeriesRendererAt(i);
                seriesRenderer.setDisplayChartValues(true);
            }
            return ChartFactory.getBarChartIntent(context, buildBarDataset(titles, values), renderer,
                    BarChart.Type.DEFAULT);
        }
        return null;
    }

    @Override
    public GraphicalView getView(Context context, int dpi) {
        setDisplayMetrics(dpi);
        if (iSet == 0){
            String[] titles = new String[] { "2007", "2008" };
            List<double[]> values = new ArrayList<double[]>();
            values.add(new double[] { 5230, 7300, 9240, 10540, 7900, 9200, 12030, 11200, 9500, 10500,
                    11600, 13500 });
            values.add(new double[] { 14230, 12300, 14240, 15244, 15900, 19200, 22030, 21200, 19500, 15500,
                    12600, 14000 });
            int[] colors = new int[] { Color.CYAN, Color.BLUE };
            XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
            renderer.setOrientation(XYMultipleSeriesRenderer.Orientation.VERTICAL);
            setChartSettings(renderer, "Monthly sales in the last 2 years", "Month", "Units sold", 0.5,
                    12.5, 0, 24000, Color.GRAY, Color.LTGRAY);
            renderer.setXLabels(1);
            renderer.setYLabels(10);
            renderer.addXTextLabel(1, "Jan");
            renderer.addXTextLabel(3, "Mar");
            renderer.addXTextLabel(5, "May");
            renderer.addXTextLabel(7, "Jul");
            renderer.addXTextLabel(10, "Oct");
            renderer.addXTextLabel(12, "Dec");
            int length = renderer.getSeriesRendererCount();
            for (int i = 0; i < length; i++) {
                XYSeriesRenderer seriesRenderer = (XYSeriesRenderer) renderer.getSeriesRendererAt(i);
                seriesRenderer.setDisplayChartValues(true);
            }
            return ChartFactory.getBarChartView(context,buildBarDataset(titles,values),renderer, BarChart.Type.DEFAULT);
        }else{
            XYMultipleSeriesRenderer renderer = buildBarRenderer(mColors);
            renderer.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
            renderer.setApplyBackgroundColor(true);
            renderer.setBackgroundColor(iBackgroundColor);
            renderer.setLabelsColor(Color.WHITE);
            renderer.setShowLabels(true);
            renderer.setChartTitle(sTitle);
            renderer.setChartTitleTextSize(getDPI(20));
            renderer.setDisplayValues(true);
            renderer.setLabelsTextSize(getDPI(12));
            renderer.setZoomEnabled(true);
            renderer.setZoomButtonsVisible(true);
            renderer.setShowLegend(false);
            double xMax =  (double)values.get(0).length;
            double[] yValues = values.get(0);
            double yMax = (yValues.length == 0) ? 0 : yValues[0];
            for (int counter = 0; counter < yValues.length; counter++){
                if (yValues[counter] > yMax) yMax = yValues[counter];
            }
            String xTitle = context.getString(R.string.label_set);
            setChartSettings(renderer, sTitle, xTitle, sTitle, 0,
                    xMax, 0, yMax, Color.GRAY, Color.LTGRAY);

            renderer.setYLabels(yValues.length);
            return ChartFactory.getBarChartView(context,buildSingleBarDataset(titles.get(0),values.get(iMetrics)),renderer, BarChart.Type.DEFAULT);
        }

    }
}
