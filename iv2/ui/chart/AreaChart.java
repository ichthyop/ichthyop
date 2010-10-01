package ichthyop.ui.chart;

/** import AWT */
import java.awt.Color;

/** import JFreeChart */
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Creates a XYArea chart, in order to show the census of
 * the particles getting out of the area of simulation and beached
 * particles.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see <a href="http://www.jfree.org/">JFreeChart</a>
 * @see {@link AbstractChart}
 */
public class AreaChart extends AbstractChart {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * XYSeries (a sequence of zero or more data items in the form (x, y)) for
     * dead cold eggs. (x, y) = (current time of simulation,
     * number of "out" particles).
     */
    private XYSeries series;


///////////////
// Constructors
///////////////

    /**
     * Constructs a new empty area chart and sets it on screen
     *
     * @param title a String, the title of the chart
     * @param labelXAxis a String, the X-Axis label
     * @param labelYAxis a String, the Y-Axis label
     * @param xLoc an int, x-coordinate of the top-left corner of the frame
     * @param yLoc an int, y-coordinate of the top-left corner of the frame
     */
    public AreaChart(String title, String labelXAxis, String labelYAxis,
                     int xLoc, int yLoc) {

        this.title = title;
        this.labelXAxis = labelXAxis;
        this.labelYAxis = labelYAxis;
        this.xLoc = xLoc;
        this.yLoc = yLoc;

        createChart();
        createUI();
    }

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Creates a new empty XYAreaChart.
     */
    void createChart() {

        series = new XYSeries("Edge");
        series.add(0, 0);

        XYDataset xyDataset = new XYSeriesCollection(series);

        chart = ChartFactory.createXYAreaChart
                (title, // Title
                 labelXAxis, // X-Axis label
                 labelYAxis, // Y-Axis label
                 xyDataset, // Dataset
                 PlotOrientation.VERTICAL,
                 false, // Show legend
                 true,
                 false
                );

        chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE);

    }

    /**
     * Resets the dataset
     */
    public void reset() {
        series.clear();
        series.add(0, 0);
    }

    /**
     * Updates the "out of area" dataset and refreshes the display.
     *
     * @param value a double, the number of beached and out-of-area particles.
     */
    public void refresh(double value) {

        series.add(series.getX(series.getItemCount() - 1).intValue() + 1, value);
        chart.getXYPlot().setDataset(new XYSeriesCollection(series));
    }

    //---------- End of class
}
