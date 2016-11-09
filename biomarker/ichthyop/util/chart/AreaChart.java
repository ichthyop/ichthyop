package ichthyop.util.chart;

import java.awt.*;
import javax.swing.*;
import java.text.*;
import java.util.*;

import org.jfree.chart.*;
import org.jfree.data.general.*;
import org.jfree.data.xy.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.statistics.*;
import org.jfree.data.time.*;

public class AreaChart
    extends AbstractChart {

  private XYSeries series;


  //----------------------------------------------------------------------------
  public AreaChart(String title, String strX, String strY, int xLoc, int yLoc) {
    this.title = title;
    this.strX = strX;
    this.strY = strY;
    this.xLoc = xLoc;
    this.yLoc = yLoc;

    createChart();
    createUI();

  }

  //----------------------------------------------------------------------------
  void createChart() {
    series = new XYSeries("Edge");
    series.add(0, 0);

    XYDataset xyDataset = new XYSeriesCollection(series);

    chart = ChartFactory.createXYAreaChart
        (title, // Title
         strX, // X-Axis label
         strY, // Y-Axis label
         xyDataset, // Dataset
         PlotOrientation.VERTICAL,
         false, // Show legend
         true,
         false
        );

    chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE);

  }

  //----------------------------------------------------------------------------
  public void reset() {
    series.clear();
    series.add(0, 0);
  }

  //----------------------------------------------------------------------------
  public void refresh(double[] d, String strTime) {
    series.add(series.getX(series.getItemCount() - 1).intValue() + 1, d[0]);
    chart.getXYPlot().setDataset(new XYSeriesCollection(series));
  }

}
