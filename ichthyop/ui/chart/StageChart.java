package ichthyop.ui.chart;

/** import JFreeChart */
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;

/** import AWT */
import java.awt.Color;

/**
 * Creates a StackedXYAreaChart chart, in order to show the census of
 * the particles troughout time, depending on their change (egg, yolk-sac larva,
 * feeding larva). It is only available when growth is simulated.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see <a href="http://www.jfree.org/">JFreeChart</a>
 * @see {@link AbstractChart}
 * @see ichthyop.bio.GrowthModel for details about the stages.
 */
public class StageChart extends AbstractChart {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * XYSeries (a sequence of zero or more data items in the form (x, y)) for
     * eggs counting. (x, y) = (current time of simulation,
     * number of eggs).
     *
     */
    private XYSeries seriesEgg;
    /**
     * XYSeries (a sequence of zero or more data items in the form (x, y)) for
     * yolk-sac larvae counting. (x, y) = (current time of simulation,
     * number of yolk-sac larvae).

     */
    private XYSeries seriesYolkSac;
    /**
     * XYSeries (a sequence of zero or more data items in the form (x, y)) for
     * feeding larvae counting. (x, y) = (current time of simulation,
     * number of feeding larvae).

     */
    private XYSeries seriesFeeding;
    /**
     * The dataset through which values of the census can be accessed.
     */
    private DefaultTableXYDataset dataset;
    /**
     * Current step of the simulation (corresponding to the current time)
     */
    private int istep;

///////////////
// Constructors
///////////////

    /**
     * Constructs a new empty stage chart and sets it on screen
     *
     * @param title a String, the title of the chart
     * @param labelXAxis a String, the X-Axis label
     * @param labelYAxis a String, the Y-Axis label
     * @param xLoc an int, x-coordinate of the top-left corner of the frame
     * @param yLoc an int, y-coordinate of the top-left corner of the frame
     */
    public StageChart(String title, String labelXAxis, String labelYAxis,
                      int xLoc, int yLoc) {

        this.title = title;
        this.labelXAxis = labelXAxis;
        this.labelYAxis = labelYAxis;
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        istep = 0;

        createChart();
        createUI();
    }

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Creates a new empty StackedXYAreaChart
     */
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

        chart = ChartFactory.createStackedXYAreaChart(title, labelXAxis,
                labelYAxis, dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.ORANGE);
        chart.getXYPlot().getRenderer().setSeriesPaint(1, Color.PINK);
        chart.getXYPlot().getRenderer().setSeriesPaint(1, Color.MAGENTA);

    }

    /**
     * Resets the dataset.
     */
    public void reset() {
        seriesEgg.clear();
        seriesYolkSac.clear();
        seriesFeeding.clear();
        istep = 0;
    }

    /**
     * Updates the dataset and refreshes the display.
     *
     * @param values an int[], census of the particles depending on their stage.
     */
    public void refresh(int[] values) {

        seriesEgg.addOrUpdate(istep, values[0]);
        seriesYolkSac.addOrUpdate(istep, values[1]);
        seriesFeeding.addOrUpdate(istep, values[2]);
        istep++;
    }

    //---------- End of class
}
