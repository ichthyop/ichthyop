package ichthyop.ui.chart;

/** import AWT */
import java.awt.Color;

/** import JFreeChart */
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * Creates a StackedXYAreaChart chart, in order to display the census of
 * dead-cold particle troughout time.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see <a href="http://www.jfree.org/">JFreeChart</a>
 * @see {@link AbstractChart}
 */
public class MortalityChart extends AbstractChart {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * XYSeries (a sequence of zero or more data items in the form (x, y)) for
     * dead cold eggs. (x, y) = (current time of simulation,
     * number of dead-cold eggs).
     */
    private XYSeries seriesEggs;
    /**
     * XYSeries (a sequence of zero or more data items in the form (x, y)) for
     * dead cold larvae. (x, y) = (current time of simulation,
     * number of dead-cold larvae).
     */
    private XYSeries seriesLarvae;
    /**
     * The dataset through which data about dead cold particles can be accessed.
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
     * Constructs a new empty mortality chart and sets it on screen
     *
     * @param title a String, the title of the chart
     * @param labelXAxis a String, the X-Axis label
     * @param labelYAxis a String, the Y-Axis label
     * @param xLoc an int, x-coordinate of the top-left corner of the frame
     * @param yLoc an int, y-coordinate of the top-left corner of the frame
     */
    public MortalityChart(String title, String labelXAxis, String labelYAxis,
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
     * Creates a new empty StackedXYAreaChart.
     */
    void createChart() {

        seriesEggs = new XYSeries("Egg mortality", true, false);
        seriesLarvae = new XYSeries("Larvae mortality", true, false);

        dataset = new DefaultTableXYDataset();
        dataset.addSeries(seriesEggs);
        dataset.addSeries(seriesLarvae);

        chart = ChartFactory.createStackedXYAreaChart(title, labelXAxis,
                labelYAxis, dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.LIGHT_GRAY);
        chart.getXYPlot().getRenderer().setSeriesPaint(1, Color.DARK_GRAY);
    }


    /**
     * Resets the dataset.
     */
    public void reset() {

        seriesEggs.clear();
        seriesLarvae.clear();
        istep = 0;
    }

    /**
     * Updates the mortality dataset and refreshes the display.
     *
     * @param deadCount an int[], the number of dead-cold eggs and larvae
     */
    public void refresh(int[] deadCount) {

        seriesEggs.addOrUpdate(istep, deadCount[0]);
        seriesLarvae.addOrUpdate(istep, deadCount[1]);
        istep++;
    }

    //---------- End of class
}
