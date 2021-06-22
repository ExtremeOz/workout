package com.a_track_it.fitdata.reports;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.InjectorUtils;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.data_model.UserDailyTotals;
import com.a_track_it.fitdata.common.data_model.WorkoutRepository;
import com.a_track_it.fitdata.common.model.UserPreferences;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Sales demo bar chart.
 */
public class DailyCubicLineChart extends AbstractTrackItChart {
    private WorkoutRepository repository;
    private String userID;
    private long startTime;
    /**
     * Returns the chart name.
     *

     * @return the chart name
     */
    public String getName() {
        return "Daily line chart";
    }

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    public String getDesc() {
        return "The daily measures line chart)";
    }
    public void setStartFrom(long start){ startTime = start;}
    public void setUserID(String sUser){ userID = sUser;}
    /**
     * Executes the chart class.
     *
     * @param context the context
     * @return the built intent
     */
    public Intent execute(Context context, int dpi) {
        if ((startTime == 0) || (userID == null)) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        long endTime = Utilities.getDayEnd(calendar,startTime);
        String[] titles = new String[] {"Steps","Move Mins","Heart Pts","Distance","Calories"};
        List<double[]> xValues = new ArrayList<double[]>();
        List<double[]> yValues = new ArrayList<double[]>();
        xValues.add(new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,32,33,34,35,36});
        int xMax = xValues.get(0).length;
        double[] stepValues = new double[xMax];
        double[] activeMinutesValues = new double[xMax];
        double[] heartPtsValues = new double[xMax];
        double[] distanceValues = new double[xMax];
        double[] caloriesValues = new double[xMax];

        repository = InjectorUtils.getWorkoutRepository(context);
        UserPreferences userPreferences = UserPreferences.getPreferences(context, userID);
        boolean bUseKg = userPreferences.getUseKG();

        List<UserDailyTotals> userDailyTotalsList = repository.getUserDailyTotals(userID,startTime,endTime,0);
        float maxY1Value = 0f; int iCounter = 0;
        float maxY2Value = 0f;
        for(UserDailyTotals udt : userDailyTotalsList){
            stepValues[iCounter] = (double) udt.stepCount;
            if (udt.stepCount > maxY2Value) maxY2Value = (float)udt.stepCount;
            activeMinutesValues[iCounter] =(double)udt.activeMinutes;
            if (udt.activeMinutes > maxY1Value) maxY1Value = (float)udt.activeMinutes;
            heartPtsValues[iCounter] = (double)udt.heartIntensity;
            if (udt.heartIntensity > maxY1Value) maxY1Value = (float)udt.heartIntensity;
            if (bUseKg) {
                float distanceMetres = udt.distanceTravelled;
                // convert to kilometres and round to 2 places
                if (distanceMetres > 1000f) distanceMetres = Math.round((distanceMetres / 1000.f) * 100.0f) / 100.0f;
                distanceValues[iCounter] =(double) distanceMetres;
                if (distanceMetres > maxY2Value) maxY2Value = distanceMetres;
            }else{
                float distanceFeet = udt.distanceTravelled * Constants.METRE_TO_FEET;
                // convert to miles and round to 2 places
                if (distanceFeet > 5280f) distanceFeet = Math.round((distanceFeet / 5280.0f) * 100.0f) / 100.0f;
                distanceValues[iCounter] =(double) distanceFeet;
                if (distanceFeet > maxY2Value) maxY2Value = distanceFeet;
            }
            caloriesValues[iCounter] = (double)udt.caloriesExpended;
            if (udt.caloriesExpended > maxY2Value) maxY2Value = (float)udt.caloriesExpended;
            if (++iCounter >= xMax) break;
        }
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        int length = titles.length;
        for (int i = 0; i < length; i++) {
            // create title and then add values
            yValues.clear();
            int scale = 0;
            switch (i){
                case 0:
                    scale = 1;
                    yValues.add(stepValues);
                    break;
                case 1:
                    scale = 1;
                    yValues.add(activeMinutesValues);
                    break;
                case 2:
                    scale = 0;
                    yValues.add(heartPtsValues);
                    break;
                case 3:
                    scale = 1;
                    yValues.add(distanceValues);
                    break;
                case 4:
                    scale = 1;
                    yValues.add(caloriesValues);
                    break;
            }
            addXYSeries(dataset, new String[]{titles[i]},xValues,yValues,scale);
        }
        // setup the renderers
        int[] colors = new int[] { Color.BLUE, Color.CYAN, Color.RED, Color.MAGENTA,Color.YELLOW };
        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND,
                PointStyle.TRIANGLE, PointStyle.SQUARE, PointStyle.POINT };
        setDisplayMetrics(dpi);
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(2);
        setRenderer(renderer, colors, styles);
        int length1 = renderer.getSeriesRendererCount();
        for (int i = 0; i < length1; i++) {
            XYSeriesRenderer r = (XYSeriesRenderer) renderer.getSeriesRendererAt(i);
            r.setLineWidth(3f);
        }
        renderer.setXLabels(0);
        renderer.setYLabels(10);
        renderer.setLabelsColor(Color.WHITE);
        renderer.setPointSize(5.5f);
        renderer.setFitLegend(true);
        renderer.setShowLegend(true);
        renderer.setLegendHeight(200);
        renderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        setChartSettings(renderer, "Daily Totals", "Hour", "Count", 0,
                xMax, 0, (double)maxY2Value, Color.GRAY, Color.LTGRAY);
        renderer.setChartTitleTextSize(getDPI(20));
        renderer.setYLabelsColor(0, Color.WHITE);
        renderer.setYLabelsColor(1, Color.WHITE);
        renderer.setYTitle("Kilo", 1);
        renderer.setYAxisAlign(Align.RIGHT, 1);
        renderer.setYLabelsAlign(Align.LEFT, 1);
        renderer.setMargins(new int[] {70, 40, 100, 20});
        int xPos = 1; int iDOY = calendar.get(Calendar.DAY_OF_YEAR);
        SimpleDateFormat dateFormatDayHour = new SimpleDateFormat("E HH", Locale.getDefault());
        SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH", Locale.getDefault());
        iCounter = 0;
        for(UserDailyTotals udt : userDailyTotalsList) {
            calendar.setTimeInMillis(udt._id);
            if ((xPos == 1) || (calendar.get(Calendar.DAY_OF_YEAR) != iDOY))
                renderer.addXTextLabel(xPos, dateFormatDayHour.format(udt._id));
            else
            if ((iCounter % 2) == 0)
                renderer.addXTextLabel(xPos, dateFormatHour.format(udt._id));
            xPos +=1;
            iDOY = calendar.get(Calendar.DAY_OF_YEAR);
            if (++iCounter == 36) break;
        }

        renderer.setXLabelsAlign(Align.LEFT);
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setPanEnabled(true, false);
        renderer.setLabelsTextSize(getDPI(14));
        //renderer.setPanLimits(new double[] { -10, 20, -10, 40 });
        //renderer.setZoomLimits(new double[] { -10, 20, -10, 40 });
        repository.destroyInstance();
        renderer.setZoomRate(1.1f);
        renderer.setShowGrid(true);
     //   renderer.setClickEnabled(true);
      //  renderer.setSelectableBuffer(12);
        Intent intent = ChartFactory.getCubicLineChartIntent(context, dataset, renderer, 0.3f,
                "Daily Health Totals");
        return intent;
    }
    /**
     * Executes the chart class.
     *
     * @param context the context
     * @return the built GraphicalView
     */
    public GraphicalView getView(Context context, int dpi) {
        if ((startTime == 0) || (userID == null)) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        long endTime = Utilities.getDayEnd(calendar,startTime);
        String[] titles = new String[] {"Steps","Move Mins","Heart Pts","Distance","Calories"};
        List<double[]> xValues = new ArrayList<double[]>();
        List<double[]> yValues = new ArrayList<double[]>();
        xValues.add(new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,32,33,34,35,36});
        int xMax = xValues.get(0).length;
        double[] stepValues = new double[xMax];
        double[] activeMinutesValues = new double[xMax];
        double[] heartPtsValues = new double[xMax];
        double[] distanceValues = new double[xMax];
        double[] caloriesValues = new double[xMax];

        repository = InjectorUtils.getWorkoutRepository(context);
        UserPreferences userPreferences = UserPreferences.getPreferences(context, userID);
        boolean bUseKg = userPreferences.getUseKG();

        List<UserDailyTotals> userDailyTotalsList = repository.getUserDailyTotals(userID,startTime,endTime,0);
        float maxY1Value = 0f; int iCounter = 0;
        float maxY2Value = 0f;
        for(UserDailyTotals udt : userDailyTotalsList){
            stepValues[iCounter] = (double) udt.stepCount;
            if (udt.stepCount > maxY2Value) maxY2Value = (float)udt.stepCount;
            activeMinutesValues[iCounter] =(double)udt.activeMinutes;
            if (udt.activeMinutes > maxY1Value) maxY1Value = (float)udt.activeMinutes;
            heartPtsValues[iCounter] = (double)udt.heartIntensity;
            if (udt.heartIntensity > maxY1Value) maxY1Value = (float)udt.heartIntensity;
            if (bUseKg) {
                float distanceMetres = udt.distanceTravelled;
                // convert to kilometres and round to 2 places
                if (distanceMetres > 1000f) distanceMetres = Math.round((distanceMetres / 1000.f) * 100.0f) / 100.0f;
                distanceValues[iCounter] =(double) distanceMetres;
                if (distanceMetres > maxY2Value) maxY2Value = distanceMetres;
            }else{
                float distanceFeet = udt.distanceTravelled * Constants.METRE_TO_FEET;
                // convert to miles and round to 2 places
                if (distanceFeet > 5280f) distanceFeet = Math.round((distanceFeet / 5280.0f) * 100.0f) / 100.0f;
                distanceValues[iCounter] =(double) distanceFeet;
                if (distanceFeet > maxY2Value) maxY2Value = distanceFeet;
            }
            caloriesValues[iCounter] = (double)udt.caloriesExpended;
            if (udt.caloriesExpended > maxY2Value) maxY2Value = (float)udt.caloriesExpended;
            if (++iCounter >= xMax) break;
        }
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        int length = titles.length;
        for (int i = 0; i < length; i++) {
            // create title and then add values
            yValues.clear();
            int scale = 0;
            switch (i){
                case 0:
                    scale = 1;
                    yValues.add(stepValues);
                    break;
                case 1:
                    scale = 1;
                    yValues.add(activeMinutesValues);
                    break;
                case 2:
                    scale = 0;
                    yValues.add(heartPtsValues);
                    break;
                case 3:
                    scale = 1;
                    yValues.add(distanceValues);
                    break;
                case 4:
                    scale = 1;
                    yValues.add(caloriesValues);
                    break;
            }
            addXYSeries(dataset, new String[]{titles[i]},xValues,yValues,scale);
        }
        // setup the renderers
        int[] colors = new int[] { Color.BLUE, Color.CYAN, Color.RED, Color.MAGENTA,Color.YELLOW };
        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND,
                PointStyle.TRIANGLE, PointStyle.SQUARE, PointStyle.POINT };
        setDisplayMetrics(dpi);
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(2);
        setRenderer(renderer, colors, styles);
        int length1 = renderer.getSeriesRendererCount();
        for (int i = 0; i < length1; i++) {
            XYSeriesRenderer r = (XYSeriesRenderer) renderer.getSeriesRendererAt(i);
            r.setLineWidth(3f);
        }
        renderer.setXLabels(0);
        renderer.setYLabels(10);
        renderer.setLabelsColor(Color.WHITE);
        renderer.setPointSize(5.5f);
        renderer.setFitLegend(true);
        renderer.setShowLegend(true);
        renderer.setLegendHeight(200);
        renderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        setChartSettings(renderer, "Daily Totals", "Hour", "Count", 0,
                xMax, 0, (double)maxY2Value, Color.GRAY, Color.LTGRAY);
        renderer.setChartTitleTextSize(getDPI(20));
        renderer.setYLabelsColor(0, Color.WHITE);
        renderer.setYLabelsColor(1, Color.WHITE);
        renderer.setYTitle("Kilo", 1);
        renderer.setYAxisAlign(Align.RIGHT, 1);
        renderer.setYLabelsAlign(Align.LEFT, 1);
        renderer.setMargins(new int[] {70, 40, 100, 20});
        int xPos = 1; int iDOY = calendar.get(Calendar.DAY_OF_YEAR);
        SimpleDateFormat dateFormatDayHour = new SimpleDateFormat("E HH", Locale.getDefault());
        SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH", Locale.getDefault());
        iCounter = 0;
        for(UserDailyTotals udt : userDailyTotalsList) {
            calendar.setTimeInMillis(udt._id);
            if ((xPos == 1) || (calendar.get(Calendar.DAY_OF_YEAR) != iDOY))
                renderer.addXTextLabel(xPos, dateFormatDayHour.format(udt._id));
            else
            if ((iCounter % 2) == 0)
                renderer.addXTextLabel(xPos, dateFormatHour.format(udt._id));
            xPos +=1;
            iDOY = calendar.get(Calendar.DAY_OF_YEAR);
            if (++iCounter == 36) break;
        }

        renderer.setXLabelsAlign(Align.LEFT);
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setPanEnabled(true, false);
        renderer.setLabelsTextSize(getDPI(14));
        //renderer.setPanLimits(new double[] { -10, 20, -10, 40 });
        //renderer.setZoomLimits(new double[] { -10, 20, -10, 40 });
        repository.destroyInstance();
        renderer.setZoomRate(1.1f);
        renderer.setShowGrid(true);
        return  ChartFactory.getCubeLineChartView(context, dataset, renderer, 0.3f);
    }

}

