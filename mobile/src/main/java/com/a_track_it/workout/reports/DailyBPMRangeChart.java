/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.a_track_it.workout.reports;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;

import com.a_track_it.workout.common.InjectorUtils;
import com.a_track_it.workout.common.Utilities;
import com.a_track_it.workout.common.data_model.UserDailyTotals;
import com.a_track_it.workout.common.data_model.WorkoutRepository;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.util.MathHelper;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * BPM  range chart.
 */
public class DailyBPMRangeChart extends AbstractTrackItChart {
  private WorkoutRepository repository;
  private String userID;
  private int iSet;
  private int iBackgroundColor;
  Calendar calendar = Calendar.getInstance();
  List<UserDailyTotals> userDailyTotalsList = new ArrayList<>();
  List<Double> minBPMValues = new ArrayList<>();
  List<Double> maxBPMValues = new ArrayList<>();
  List<Double> avgBPMValues = new ArrayList<>();
  private long startTime;
  private int perDay;
  private int ageValue;

  public DailyBPMRangeChart(){
    super();
    iSet = 0;
    calendar.setTime(new Date());
  }

  /**
   * Returns the chart name.
   * 
   * @return the chart name
   */
  public String getName() {
    return "BPM range chart";
  }

  /**
   * Returns the chart description.
   * 
   * @return the chart description
   */
  public String getDesc() {
    return "The hourly hear rate BPM (vertical range chart)";
  }
  public void setStartFrom(long start){
    startTime = start;}
  public void setUserID(String sUser){ userID = sUser;}
  public void setPerDay(int per){ perDay = per;}
  public void setAgeValue(int age){ ageValue = age;}

  public void setBackgroundColor(int i){
    iBackgroundColor = i;
    iSet = 1;
  }

  public void setAvgBPMValues(List<Double> vals){
    avgBPMValues = new ArrayList<>(vals);
    iSet = 2;
  }

  public void setMaxBPMValues(List<Double> maxs){
    maxBPMValues = new ArrayList<>(maxs);
    iSet = 2;
  }
  public void setMinBPMValues(List<Double> mins){
    minBPMValues = new ArrayList<>(mins);
    iSet = 2;
  }
  public void setUserDailyTotalsList(List<UserDailyTotals> udtList){
    userDailyTotalsList = new ArrayList<>(udtList);
    iSet = 1;
  }
  public List<UserDailyTotals> getUserDailyTotalsList(){
    return userDailyTotalsList;
  }
  /**
   * Executes the chart demo.
   * 
   * @param context the context
   * @return the built intent
   */
  public Intent execute(Context context, int dpi) {
    if ((startTime == 0) || (userID == null)) return null;
    int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
    calendar.setTimeInMillis(startTime);
    long endTime = Utilities.getDayEnd(calendar,startTime);
    setDisplayMetrics(dpi);
    XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
    RangeCategorySeries series = new RangeCategorySeries("BPM");
    XYSeries avgSeries = new XYSeries("avg");
    float maxValue = 0;
    float minValue = 0;
    int iEntries = 1;
    int firstHour = 0;
    if ((iSet == 0) || (iSet == 1)) {
      if (userDailyTotalsList.size() == 0) {
        repository = InjectorUtils.getWorkoutRepository(context.getApplicationContext());
        userDailyTotalsList = repository.getUserDailyTotals(userID, startTime,endTime, perDay);
      }
      for (int i=0; i < (perDay == 1 ? nowHour : 36); i++){
        for (UserDailyTotals udt : userDailyTotalsList) {
          calendar.setTimeInMillis(udt._id);
          int udtHour = calendar.get(Calendar.HOUR_OF_DAY);
          if ((udtHour == i) || (perDay == 0)){
             if (firstHour < udtHour) firstHour = udtHour;
              minBPMValues.add((double) udt.minBPM);
              maxBPMValues.add((double) udt.maxBPM);
              avgBPMValues.add((double) udt.avgBPM);
              if (iEntries == 1) {
                maxValue = udt.maxBPM;
                minValue = udt.minBPM;
              } else {
                if (udt.maxBPM > maxValue) maxValue = udt.maxBPM;
                if (udt.minBPM < minValue) minValue = udt.minBPM;
              }
              iEntries++;
          }
          if ((perDay == 0) && (iEntries == 36)) break;
        }
      }
    }else{
      if (iSet == 2){
        for (int i=0; i < (perDay == 1 ? nowHour : 36); i++){
            series.add((double) minBPMValues.get(i), maxBPMValues.get(i));
            avgSeries.add(iEntries, avgBPMValues.get(i));
            if ((firstHour < i) && (avgBPMValues.get(i) > 0)) firstHour = i;
          iEntries++;
          if ((perDay == 0) && (iEntries == 36)) break;
        }
      }
    }
    dataSet.addSeries(series.toXYSeries());
  //  dataSet.addSeries(avgSeries);
    int[] colors = new int[] { Color.CYAN };
    XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
    if (perDay == 0)
      setChartSettings(renderer, "Hourly BPM range", "Hour", "Beats per Minute", 1,32,
              minValue-10, maxValue+20, Color.GRAY, Color.LTGRAY);
    else
      setChartSettings(renderer, "Daily BPM range", "Day", "Beats per Minute", firstHour,nowHour+1,
              40, 190, Color.GRAY, Color.LTGRAY);

    renderer.setXLabels(0);
    renderer.setYLabels(10);
    renderer.setShowLegend(false);
    renderer.setYLabelsPadding(4);
    renderer.setBackgroundColor(iBackgroundColor);

    int xPos = 1; int iDOY = calendar.get(Calendar.DAY_OF_YEAR);
    SimpleDateFormat dateFormatDay = new SimpleDateFormat("MMM d", Locale.getDefault());
    SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH", Locale.getDefault());
    int iCounter = 0;
    if (perDay == 0)
      for(UserDailyTotals udt : userDailyTotalsList) {
/*          calendar.setTimeInMillis(udt._id);
          if ((xPos == 1) || (calendar.get(Calendar.DAY_OF_YEAR) != iDOY))
            renderer.addXTextLabel(xPos, dateFormatDay.format(udt._id));
          else*/
          if ((iCounter % 2) == 0)
            renderer.addXTextLabel(xPos, dateFormatHour.format(udt._id));
          xPos +=1;
         // iDOY = calendar.get(Calendar.DAY_OF_YEAR);

      }
    else
      for(UserDailyTotals udt : userDailyTotalsList) {
        calendar.setTimeInMillis(udt._id);
        if ((iCounter++ % 3) == 0)
          renderer.addXTextLabel(xPos, dateFormatDay.format(udt._id));
        xPos +=1;
      }
    renderer.setMargins(new int[] {10, 20, 10, 20});
    renderer.setYLabelsAlign(Align.LEFT);

    XYSeriesRenderer r = (XYSeriesRenderer) renderer.getSeriesRendererAt(0);
    r.setDisplayChartValues(true);
    r.setChartValuesTextSize(getDPI(11));
    r.setChartValuesSpacing(3);
    r.setChartValuesTextAlign(Align.CENTER);
    NumberFormat nf = NumberFormat.getIntegerInstance();
    r.setChartValuesFormat(nf);
    renderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
    renderer.setZoomRate(1.1f);
    renderer.setBarSpacing(1f);
    renderer.setBarWidth(50f);
    r.setGradientEnabled(true);
    if (ageValue == 0) ageValue = 30;
    int[] ageMinMax = Utilities.AgeValueToRange(ageValue);
    r.setGradientStart(ageMinMax[0], Color.GREEN);
    r.setGradientStop(ageMinMax[1], Color.RED);
 //   if (!renderer.isInitialRangeSet()){
  //      renderer.setInitialRange(new double[]{(double)minValue,(double)maxValue},2);
  //  }
  //  XYSeriesRenderer r2 = (XYSeriesRenderer) renderer.getSeriesRendererAt(0);
    if (repository != null)
      repository.destroyInstance();
    return ChartFactory.getRangeBarChartIntent(context, dataSet, renderer, Type.DEFAULT,
        "Heart Rate Per Hour Summary");
  }

  @Override
  public GraphicalView getView(Context context, int dpi) {
    if ((startTime == 0) || (userID == null)) return null;
    calendar.setTimeInMillis(System.currentTimeMillis());
    int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
    calendar.setTimeInMillis(startTime);
    int firstDayIndex = calendar.get(Calendar.DAY_OF_YEAR);
    int firstDay = 0;
    setDisplayMetrics(dpi);
    XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
    int[] colors = new int[] { Color.CYAN };
    SimpleDateFormat dateFormatDay = new SimpleDateFormat("MMM d", Locale.getDefault());
    SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH", Locale.getDefault());
    XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
    renderer.setXLabels(0);
    renderer.setMargins(new int[] {10, 20, 10, 20});
    renderer.setYLabelsAlign(Align.LEFT);
    renderer.setYLabels(10);
    renderer.setShowLegend(false);
    renderer.setYLabelsPadding(4);
/*    XYSeriesRenderer avgRenderer = new XYSeriesRenderer();
    avgRenderer.setColor(Color.YELLOW);
    avgRenderer.setPointStyle(PointStyle.POINT);
    avgRenderer.setFillPoints(false);
    //avgRenderer.setPointStrokeWidth(getDPI(1));
    avgRenderer.setLineWidth(2);
    avgRenderer.setShowLegendItem(false);
    renderer.addSeriesRenderer(avgRenderer);*/
    RangeCategorySeries series = new RangeCategorySeries("BPM");
 //   XYSeries xySeries = new XYSeries("BPM");
    XYSeries avgSeries = new XYSeries("Avg");
    renderer.setBackgroundColor(iBackgroundColor);
    float maxValue = 0;
    float minValue = 0;
    int iEntries = 1;
    int firstHour = 0;
 //   if (iSet <= 1) {
        for (int i = 0; i < (perDay == 0 ? nowHour : 31); i++) {
          boolean bFound = false;
          for (UserDailyTotals udt : userDailyTotalsList) {
            calendar.setTimeInMillis(udt._id);
            int udtHour = calendar.get(Calendar.HOUR_OF_DAY);
            int udtDay = calendar.get(Calendar.DAY_OF_YEAR);
            if ((perDay == 0)) {
              if (udtHour == i) {
                if ((udt.maxBPM > 0) && (udt.minBPM > 0)) {
                  if (firstHour == 0) firstHour = udtHour;
                  minBPMValues.add((double) udt.minBPM);
                  maxBPMValues.add((double) udt.maxBPM);
                  avgBPMValues.add((double) udt.avgBPM);
                  if (iEntries == 1) {
                    maxValue = udt.maxBPM;
                    minValue = udt.minBPM;
                  } else {
                    if (udt.maxBPM > maxValue) maxValue = udt.maxBPM;
                    if (udt.minBPM < minValue) minValue = udt.minBPM;
                  }
                  series.add(udt.minBPM, udt.maxBPM);
                  avgSeries.add(i, udt.avgBPM);
                  iEntries++;
                  bFound = true;
                  break;
                }

              }
            }
            if (perDay == 1){
                if (udtDay == firstDayIndex){
                  if ((udt.maxBPM > 0) && (udt.minBPM > 0)) {
                    if (firstDay == 0) firstDay = i;
                    minBPMValues.add((double) udt.minBPM);
                    maxBPMValues.add((double) udt.maxBPM);
                    avgBPMValues.add((double) udt.avgBPM);
                    if (iEntries == 1) {
                      maxValue = udt.maxBPM;
                      minValue = udt.minBPM;
                    } else {
                      if (udt.maxBPM > maxValue) maxValue = udt.maxBPM;
                      if (udt.minBPM < minValue) minValue = udt.minBPM;
                    }
                    series.add(udt.minBPM, udt.maxBPM);
                    avgSeries.add(i, udt.avgBPM);
                    iEntries++;
                    bFound = true;
                    break;
                  }
                }
            }
          }
          if (!bFound){
            series.add((double) MathHelper.NULL_VALUE, (double)MathHelper.NULL_VALUE);
            avgSeries.add(i,(double) MathHelper.NULL_VALUE);
          }
          firstDayIndex += 1; // next day of the year
        }
  //  }
    XYSeries xy = series.toXYSeries();
  //  Log.e(DailyBPMRangeChart.class.getSimpleName(),"Count " + xy.getItemCount());
   dataSet.addSeries(xy);
  // dataSet.addSeries(avgSeries);
   if (minValue > 10) minValue = minValue - 10;
    if (perDay == 0)
      setChartSettings(renderer, "Hourly BPM range", "Hour", "Beats per Minute", firstHour,nowHour,
              minValue, maxValue+20, Color.GRAY, Color.LTGRAY);
    else {
      renderer.setXLabelsAngle(270);
      setChartSettings(renderer, "Daily BPM range", "Day", "Beats per Minute", firstDay, 31,
              minValue, maxValue + 20, Color.GRAY, Color.LTGRAY);
    }
    int xPos = 1; int iDOY = calendar.get(Calendar.DAY_OF_YEAR);
    int iCounter = 0;
    if (perDay == 0)
      for(UserDailyTotals udt : userDailyTotalsList) {
        if ((udt.maxBPM > 0) && (udt.minBPM > 0)) {
          renderer.addXTextLabel(xPos, dateFormatHour.format(udt._id));
        }
        xPos +=1;
      }
    if (perDay == 1)
      for(UserDailyTotals udt : userDailyTotalsList) {
        if ((iCounter++ % 4) == 0) {
          renderer.addXTextLabel(xPos, dateFormatDay.format(udt._id));
        }
        xPos +=1;
      }
    XYSeriesRenderer r = (XYSeriesRenderer) renderer.getSeriesRendererAt(0);
    r.setDisplayChartValues(true);
    r.setChartValuesTextSize(getDPI(11));
    r.setChartValuesSpacing(3);
    r.setChartValuesTextAlign(Align.CENTER);
    NumberFormat nf = NumberFormat.getIntegerInstance();
    r.setChartValuesFormat(nf);
    renderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
    renderer.setZoomRate(1.1f);
    renderer.setBarSpacing(1f);
    renderer.setBarWidth(50f);
    r.setGradientEnabled(true);
    if (ageValue == 0) ageValue = 30;
    int[] ageMinMax = Utilities.AgeValueToRange(ageValue);
    r.setGradientStart(ageMinMax[0], Color.GREEN);
    r.setGradientStop(ageMinMax[1], Color.RED);

    return ChartFactory.getRangeBarChartView(context,dataSet,renderer,Type.DEFAULT);
  }

}
