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

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Budget demo pie chart.
 */
public class StrengthPieChart extends AbstractTrackItChart {
  List<double[]> values = new ArrayList<double[]>();
  List<String[]> titles = new ArrayList<String[]>();
  String sTitle = "Fitness PieChart";
  int[] mColors;
  int iSet;
  int iBackgroundColor;

  public StrengthPieChart(){
    super();
    iSet = 0;
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

  /**
   * Executes the chart demo.
   * 
   * @param context the context
   * @return the built intent
   */
  public Intent execute(Context context, int dpi) {
    setDisplayMetrics(dpi);
    if (iSet == 0) {
      double[] values = new double[]{12, 14, 11, 10, 19};
      int[] colors = new int[]{Color.BLUE, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.CYAN};
      String[] titles = {"Try 1","Try 2","Try 3","Try 4", "Try 5"};
      DefaultRenderer renderer = buildCategoryRenderer(colors);
      renderer.setZoomButtonsVisible(true);
      renderer.setZoomEnabled(true);
      renderer.setChartTitleTextSize(getDPI(20));
      renderer.setDisplayValues(true);
      renderer.setShowLabels(true);
      SimpleSeriesRenderer r = renderer.getSeriesRendererAt(0);
      r.setGradientEnabled(true);
      r.setGradientStart(0, Color.BLUE);
      r.setGradientStop(0, Color.GREEN);
      r.setHighlighted(true);
      Intent intent = ChartFactory.getPieChartIntent(context,
              buildCategoryDataset("Project budget",titles, values), renderer, "Budget");
      return intent;
    }else{
      DefaultRenderer renderer = buildCategoryRenderer(mColors);
      renderer.setApplyBackgroundColor(true);
      renderer.setBackgroundColor(iBackgroundColor);
      renderer.setChartTitleTextSize(getDPI(20));
      renderer.setLabelsColor(Color.WHITE);
      renderer.setShowLabels(true);
      renderer.setChartTitle(sTitle);
      renderer.setChartTitleTextSize(getDPI(20));
      renderer.setDisplayValues(true);
      renderer.setLabelsTextSize(getDPI(12));
      renderer.setZoomEnabled(true);
      renderer.setZoomButtonsVisible(true);
      renderer.setShowLegend(false);
      Intent intent = ChartFactory.getPieChartIntent(context,
              buildCategoryDataset(sTitle,titles.get(0), values.get(0)), renderer, sTitle);
      return intent;
    }

  }

  @Override
  public GraphicalView getView(Context context, int dpi) {
    setDisplayMetrics(dpi);
    if (iSet == 0) {
      double[] values = new double[]{12, 14, 11, 10, 19};
      int[] colors = new int[]{Color.BLUE, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.CYAN};
      String[] titles = {"Try 1","Try 2","Try 3","Try 4", "Try 5"};
      DefaultRenderer renderer = buildCategoryRenderer(colors);
      renderer.setZoomButtonsVisible(true);
      renderer.setZoomEnabled(true);
      renderer.setChartTitleTextSize(getDPI(20));
      renderer.setDisplayValues(true);
      renderer.setShowLabels(true);
      SimpleSeriesRenderer r = renderer.getSeriesRendererAt(0);
      r.setGradientEnabled(true);
      r.setGradientStart(0, Color.BLUE);
      r.setGradientStop(0, Color.GREEN);
      r.setHighlighted(true);
      return ChartFactory.getPieChartView(context, buildCategoryDataset("Project budget",titles, values), renderer);
    }else {
      DefaultRenderer renderer = buildCategoryRenderer(mColors);
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
      return ChartFactory.getPieChartView(context, buildCategoryDataset(sTitle,titles.get(0), values.get(0)), renderer);
    }
  }
}
