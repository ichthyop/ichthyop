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

public class HistoChart
    extends AbstractChart {

  private int nbClasses;
  private double[] data;

  //----------------------------------------------------------------------------
  public HistoChart(String title, String strX, String strY, int nbClasses, int xLoc, int yLoc) {
    this.title = title;
    this.strX = strX;
    this.strY = strY;
    this.nbClasses = nbClasses;
    data = new double[nbClasses];
    this.xLoc = xLoc;
    this.yLoc = yLoc;

    createChart();
    createUI();
  }

  //----------------------------------------------------------------------------
  void createChart() {
    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);
    dataset.addSeries("", data, nbClasses);

    chart = ChartFactory.createHistogram
        (title,
         strX,
         strY,
         dataset,
         PlotOrientation.VERTICAL,
         false,
         false,
         false
        );
  }

  //----------------------------------------------------------------------------
  public void refresh(double[] d, String strTime) {
    this.data = d;
    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);
    dataset.addSeries("", d, nbClasses);
    chart.getXYPlot().setDataset(dataset);
    chart.setTitle(title + " - " + strTime);
  }

}
