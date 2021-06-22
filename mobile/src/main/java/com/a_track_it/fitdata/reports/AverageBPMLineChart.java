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
package com.a_track_it.fitdata.reports;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;

import com.a_track_it.fitdata.common.Utilities;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Average beats-per-minute demo chart.
 */
public class AverageBPMLineChart extends AbstractTrackItChart {
  String sTitle = "BPM Line Chart";
  int[] mColors;
  String[] titles = new String[] { "Avg", "Min", "Max" };
  List<double[]> x = new ArrayList<double[]>();
  List<double[]> values = new ArrayList<double[]>();
  int iSet;
  int iBackgroundColor;
  List<Double> minBPMValues = new ArrayList<>();
  List<Double> maxBPMValues = new ArrayList<>();
  List<Double> avgBPMValues = new ArrayList<>();
  Calendar calendar = Calendar.getInstance();
  private int ageValue;
  public void setAgeValue(int age){ ageValue = age;}

  public AverageBPMLineChart(){
    super();
    iSet = 0;
    calendar.setTime(new Date());
  }
  public void setTitles(String[] t){
    titles = t;
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

  /**
   * Returns the chart name.
   * 
   * @return the chart name
   */
  public String getName() {
    return "Average beats-per-minute";
  }

  /**
   * Returns the chart description.
   * 
   * @return the chart description
   */
  public String getDesc() {
    return "The average,min,max beats-per-minute (line chart)";
  }
  public void setBackgroundColor(int i){
    iBackgroundColor = i;
    iSet = 1;
  }

  public void setValuesList(List<double[]> v){
    values = v;
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
   * Executes the chart demo.
   * 
   * @param context the context
   * @return the built intent
   */
  public Intent execute(Context context, int dpi) {
    setDisplayMetrics(dpi);
    int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
    double[] hourList = new double[nowHour+1];
    for (int i = 0; i <= nowHour; i++){
      hourList[i] = i;
    }
    for (int i = 0; i < titles.length; i++) {
      x.add(hourList);
    }
    if (mColors == null)
      mColors = new int[titles.length];

    PointStyle[] styles = new PointStyle[titles.length];
    for (int i = 0; i <= titles.length; i++){
      if (i== 0) styles[i] = PointStyle.CIRCLE;
      else styles[i] = PointStyle.POINT;
      if (i==0)
        mColors[i] = Color.GREEN;
      if (i==1)
        mColors[i] = Color.CYAN;
      if (i==2)
        mColors[i] = Color.BLUE;
    }
    double dMax = 0;
    double dMin = 0;
    if (titles.length == 1){
      double[] valuesArray = values.get(0);
      for (int i=0; i < valuesArray.length; i++){
        if ((dMax == 0) ||(valuesArray[i] > dMax)) dMax = valuesArray[i];
        if ((dMin == 0) ||(valuesArray[i] < dMin)) dMin = valuesArray[i];
      }
    }else{
      double[] values1Array = values.get(1); // min
      double[] values2Array = values.get(2); // max
      for (int i=0; i < values1Array.length; i++){
        if ((dMax == 0) ||(values2Array[i] > dMax)) dMax = values2Array[i];
        if ((dMin == 0) ||(values1Array[i] < dMin)) dMin = values1Array[i];
      }
    }
    XYMultipleSeriesRenderer renderer = buildRenderer(mColors, styles);
    int length = renderer.getSeriesRendererCount();
    for (int i = 0; i < length; i++) {
      ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
    }
    setChartSettings(renderer, "Average Heart Rate", "Hour", "BPM", 0.0, nowHour,  dMin-10, dMax+10,
        Color.LTGRAY, Color.LTGRAY);
    renderer.setXLabels(12);
    renderer.setBackgroundColor(iBackgroundColor);
    renderer.setYLabels(10);
    renderer.setShowGrid(true);
    renderer.setXLabelsAlign(Align.RIGHT);
    renderer.setYLabelsAlign(Align.RIGHT);
    renderer.setZoomButtonsVisible(true);
    renderer.setPanLimits(new double[] { -10, 20, -10, 40 });
    renderer.setZoomLimits(new double[] { -10, 20, -10, 40 });
    if (ageValue == 0) ageValue = 30;
    int[] ageMinMax = Utilities.AgeValueToRange(ageValue);
    ((XYSeriesRenderer) renderer.getSeriesRendererAt(0)).setGradientStart(ageMinMax[0], Color.GREEN);
    ((XYSeriesRenderer) renderer.getSeriesRendererAt(0)).setGradientStop(ageMinMax[1], Color.RED);

    XYMultipleSeriesDataset dataset = buildDataset(titles, x, values);
    XYSeries series = dataset.getSeriesAt(0);
    String sTemp = Integer.toString((int)dMax) + " max";
    series.addAnnotation(sTemp,0,dMax);
    sTemp = Integer.toString((int)dMin) + " min";
    series.addAnnotation(sTemp,0,dMin);
    Intent intent = ChartFactory.getLineChartIntent(context, dataset, renderer,
        "Average beats-per-minute");
    return intent;
  }

  @Override
  public GraphicalView getView(Context context, int dpi) {
    setDisplayMetrics(dpi);
    int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
    double[] hourList = new double[nowHour+1];
    for (int i = 0; i <= nowHour; i++){
      hourList[i] = i;
    }
    for (int i = 0; i < titles.length; i++) {
      x.add(hourList);
    }
    if (mColors == null)
      mColors = new int[titles.length];

    PointStyle[] styles = new PointStyle[titles.length];
    for (int i = 0; i <= titles.length; i++){
      if (i== 0) styles[i] = PointStyle.CIRCLE;
      else styles[i] = PointStyle.POINT;
      if (i==0)
        mColors[i] = Color.GREEN;
      if (i==1)
        mColors[i] = Color.CYAN;
      if (i==2)
        mColors[i] = Color.BLUE;
    }
    double dMax = 0;
    double dMin = 0;
    if (titles.length == 1){
      double[] valuesArray = values.get(0);
      for (int i=0; i < valuesArray.length; i++){
        if ((dMax == 0) ||(valuesArray[i] > dMax)) dMax = valuesArray[i];
        if ((dMin == 0) ||(valuesArray[i] < dMin)) dMin = valuesArray[i];
      }
    }else{
      double[] values1Array = values.get(1); // min
      double[] values2Array = values.get(2); // max
      for (int i=0; i < values1Array.length; i++){
        if ((dMax == 0) ||(values2Array[i] > dMax)) dMax = values2Array[i];
        if ((dMin == 0) ||(values1Array[i] < dMin)) dMin = values1Array[i];
      }
    }
    XYMultipleSeriesRenderer renderer = buildRenderer(mColors, styles);
    int length = renderer.getSeriesRendererCount();
    for (int i = 0; i < length; i++) {
      ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
    }
    setChartSettings(renderer, "Average Heart Rate", "Hour", "BPM", 0.0, nowHour,  dMin-10, dMax+10,
            Color.LTGRAY, Color.LTGRAY);
    renderer.setXLabels(12);
    renderer.setBackgroundColor(iBackgroundColor);
    renderer.setYLabels(10);
    renderer.setShowGrid(true);
    renderer.setXLabelsAlign(Align.RIGHT);
    renderer.setYLabelsAlign(Align.RIGHT);
    renderer.setZoomButtonsVisible(true);
    renderer.setPanLimits(new double[] { -10, 20, -10, 40 });
    renderer.setZoomLimits(new double[] { -10, 20, -10, 40 });
    if (ageValue == 0) ageValue = 30;
    int[] ageMinMax = Utilities.AgeValueToRange(ageValue);
    ((XYSeriesRenderer) renderer.getSeriesRendererAt(0)).setGradientStart(ageMinMax[0], Color.GREEN);
    ((XYSeriesRenderer) renderer.getSeriesRendererAt(0)).setGradientStop(ageMinMax[1], Color.RED);

    XYMultipleSeriesDataset dataset = buildDataset(titles, x, values);
    XYSeries series = dataset.getSeriesAt(0);
    String sTemp = Integer.toString((int)dMax) + " max";
    series.addAnnotation(sTemp,0,dMax);
    sTemp = Integer.toString((int)dMin) + " min";
    series.addAnnotation(sTemp,0,dMin);
    return ChartFactory.getLineChartView(context,dataset,renderer);
  }
}
