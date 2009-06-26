package ichthyop.util.chart;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.Color;

public class StageChart
    extends AbstractChart {

  private XYSeries seriesEgg, seriesYolkSac, seriesFeeding;
  DefaultTableXYDataset dataset;
  private int istep;

  //----------------------------------------------------------------------------
  public StageChart(String title, String strX, String strY, int xLoc,
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
    seriesEgg = new XYSeries("Egg", true, false);
    //seriesEgg.add(0, 0);
    seriesYolkSac = new XYSeries("Yolk-sac larva", true, false);
    //seriesYolkSac.add(0, 0);
    seriesFeeding = new XYSeries("Feeding larva", true, false);
    //seriesFeeding.add(0, 0);

    dataset = new DefaultTableXYDataset();
    dataset.addSeries(seriesEgg);
    dataset.addSeries(seriesYolkSac);
    dataset.addSeries(seriesFeeding);

    chart = ChartFactory.createStackedXYAreaChart(title, strX, strY, dataset,
        PlotOrientation.VERTICAL,
        true,
        true,
        false);
    chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.ORANGE);
    chart.getXYPlot().getRenderer().setSeriesPaint(1, Color.PINK);
    chart.getXYPlot().getRenderer().setSeriesPaint(1, Color.MAGENTA);

  }

  //----------------------------------------------------------------------------
  public void reset() {
    seriesEgg.clear();
    seriesYolkSac.clear();
    seriesFeeding.clear();
    istep = 0;
  }

  //----------------------------------------------------------------------------
  public void refresh(double[] d, String strTime) {
    seriesEgg.addOrUpdate(istep, d[0]);
    seriesYolkSac.addOrUpdate(istep, d[1]);
    seriesFeeding.addOrUpdate(istep, d[2]);
    istep++;
  }

}
