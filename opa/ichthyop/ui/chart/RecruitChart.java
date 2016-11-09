package ichthyop.ui.chart;

/** import JFreeChart */
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/** local import */
import ichthyop.io.Configuration;
import ichthyop.core.Zone;

/**
 * Creates a StackedBarChart chart, in order to show the census of
 * the recruited particles troughout time, classified function of the
 * colour of the release zone.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see <a href="http://www.jfree.org/">JFreeChart</a>
 * @see {@link AbstractChart}
 */
public class RecruitChart extends AbstractChart {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * Number of release zones (S for spawning)
     */
    private int nbSZone;
    /**
     * Number of recruitment zone (R for recruitment)
     */
    private int nbRZone;
    /**
     * Dataset with release zone as Series and recruitment zone as Categories.
     */
    private DefaultCategoryDataset categoryDataset;

///////////////
// Constructors
///////////////

    /**
     * Constructs a new empty recruitment chart and sets it on screen
     *
     * @param title a String, the title of the chart
     * @param labelXAxis a String, the X-Axis label
     * @param labelYAxis a String, the Y-Axis label
     * @param xLoc an int, x-coordinate of the top-left corner of the frame
     * @param yLoc an int, y-coordinate of the top-left corner of the frame
     */
    public RecruitChart(String title, String labelXAxis, String labelYAxis,
                        int xLoc, int yLoc) {

        this.title = title;
        this.labelXAxis = labelXAxis;
        this.labelYAxis = labelYAxis;
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        nbSZone = Configuration.getReleaseZones().size();
        nbRZone = Configuration.getRecruitmentZones().size();

        createChart();
        createUI();
    }

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Creates a new empty StackedBarChart
     */
    void createChart() {

        categoryDataset = new DefaultCategoryDataset();

        Comparable categoryName, serieName;
        for (int i_rzone = 0; i_rzone < nbRZone; i_rzone++) {
            categoryName = new String("Recruitment zone " + (i_rzone + 1));
            for (int i_szone = 0; i_szone < nbSZone; i_szone++) {
                serieName = new String("Release zone " + (i_szone + 1));
                categoryDataset.setValue(0, serieName, categoryName);
            }
        }

        chart = ChartFactory.createStackedBarChart
                (title, labelXAxis, labelYAxis, categoryDataset,
                 PlotOrientation.VERTICAL,
                 true, true, false);

        Zone zone;
        for (int i = 0; i < nbSZone; i++) {
            zone = (Zone) Configuration.getReleaseZones().get(i);
            chart.getCategoryPlot().getRenderer().setSeriesPaint(i,
                    zone.getColor());
        }

    }

    /**
     * Updates the recruitment CategoryDataset and refreshes the display.
     *
     * @param values an int[], the number of recruited particles in each
     * recruitment zone.
     * @param strTime a String, the current time of the simulation.
     */
    public void refresh(int[] values, String strTime) {

        Comparable categoryName, serieName;
        for (int i_rzone = 0; i_rzone < nbRZone; i_rzone++) {
            categoryName = new String("Recruitment zone " + (i_rzone + 1));
            for (int i_szone = 0; i_szone < nbSZone; i_szone++) {
                serieName = new String("Release zone " + (i_szone + 1));
                categoryDataset.setValue(values[nbRZone * i_szone + i_rzone],
                                         serieName, categoryName);
            }
        }
        chart.setTitle(title + " - " + strTime);
    }

    //---------- End of class
}
