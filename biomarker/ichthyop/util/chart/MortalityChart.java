package ichthyop.util.chart;

import java.awt.*;
import javax.swing.*;
import java.text.*;
import java.util.*;

import ichthyop.*;

import org.jfree.chart.*;
import org.jfree.data.general.*;
import org.jfree.data.xy.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.statistics.*;
import org.jfree.data.time.*;

public class MortalityChart
    extends AbstractChart {

  private XYSeries seriesEggs, seriesLarvae;
  DefaultTableXYDataset dataset;
  private int istep;

  //----------------------------------------------------------------------------
  public MortalityChart(String title, String strX, String strY, int xLoc,
      int yLoc) {
    this.title = title;
    this.strX = strX;
    this.strY = strY;
    this.xLoc = xLoc;
    this.yLoc = yLoc;
    istep = 0;

    createChart();
    createUI();
  }

  //----------------------------------------------------------------------------
  void createChart() {
    seriesEggs = new XYSeries("Egg mortality", true, false);
    seriesLarvae = new XYSeries("Larvae mortality", true, false);

    dataset = new DefaultTableXYDataset();
    dataset.addSeries(seriesEggs);
    dataset.addSeries(seriesLarvae);

    chart = ChartFactory.createStackedXYAreaChart(title, strX, strY, dataset,
        PlotOrientation.VERTICAL,
        true,
        true,
        false);
    chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.LIGHT_GRAY);
    chart.getXYPlot().getRenderer().setSeriesPaint(1, Color.DARK_GRAY);
  }

  //----------------------------------------------------------------------------
  public void reset() {
    seriesEggs.clear();
    seriesLarvae.clear();
    istep = 0;
  }

  //----------------------------------------------------------------------------
  public void refresh(double[] d, String strTime) {
    seriesEggs.addOrUpdate(istep, d[0]);
    seriesLarvae.addOrUpdate(istep, d[1]);
    istep++;
  }
}
