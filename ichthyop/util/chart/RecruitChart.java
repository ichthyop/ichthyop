package ichthyop.util.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import ichthyop.*;

public class RecruitChart
    extends AbstractChart {

  private int nbSZone, nbRZone;
  private DefaultCategoryDataset categoryDataset;

  //----------------------------------------------------------------------------
  public RecruitChart(String title, String strX, String strY, int xLoc,
      int yLoc) {
    this.title = title;
    this.strX = strX;
    this.strY = strY;
    this.xLoc = xLoc;
    this.yLoc = yLoc;
    nbSZone = GetConfig.getReleaseZones().size();
    nbRZone = GetConfig.getRecruitmentZones().size();

    createChart();
    createUI();
  }

  void createChart() {
    categoryDataset = new DefaultCategoryDataset();

    Comparable categoryName, serieName;
    for (int i_rzone = 0; i_rzone < nbRZone; i_rzone++) {
      categoryName = new String("Recruitment zone " + (i_rzone + 1));
      for (int i_szone = 0; i_szone < nbSZone; i_szone++) {
        serieName = new String("Releasing zone " + (i_szone + 1));
        categoryDataset.setValue(0, serieName, categoryName);
      }
    }

    chart = ChartFactory.createStackedBarChart
        (title, strX, strY, categoryDataset, PlotOrientation.VERTICAL,
        true, true, false);

    Zone zone;
    for (int i = 0; i < nbSZone; i++) {
      zone = (Zone)GetConfig.getReleaseZones().get(i);
      chart.getCategoryPlot().getRenderer().setSeriesPaint(i, zone.getColorZone());
    }

  }

  //----------------------------------------------------------------------------
  public void refresh(double[] d, String strTime) {
    //DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();

    Comparable categoryName, serieName;
    for (int i_rzone = 0; i_rzone < nbRZone; i_rzone++) {
      categoryName = new String("Recruitment zone " + (i_rzone + 1));
      for (int i_szone = 0; i_szone < nbSZone; i_szone++) {
        serieName = new String("Releasing zone " + (i_szone + 1));
        categoryDataset.setValue(d[nbRZone * i_szone + i_rzone], serieName, categoryName);
      }
    }

    //chart.getCategoryPlot().setDataset(categoryDataset);
    chart.setTitle(title + " - " + strTime);

  }
}
