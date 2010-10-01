package ichthyop.ui.chart;

/** import JFreeChart */
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

/**
 * Creates a histogram chart, in order to monitor the depth and the length
 * distribution of the particle troughout time.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see <a href="http://www.jfree.org/">JFreeChart</a>
 * @see {@link AbstractChart}
 */
public class HistoChart extends AbstractChart {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * Number of bins of the histogram
     */
    private int bins;
    /**
     * Values of the histogram dataset
     */
    private double[] values;

///////////////
// Constructors
///////////////

    /**
     * Constructs a new empty histogram chart and sets it on screen
     *
     * @param title a String, the title of the histogram
     * @param labelXAxis a String, the X-Axis label
     * @param labelYAxis a String, the Y-Axis label
     * @param bins an int, the number of bins
     * @param xLoc an int, x-coordinate of the top-left corner of the frame
     * @param yLoc an int, y-coordinate of the top-left corner of the frame
     */
    public HistoChart(String title, String labelXAxis, String labelYAxis,
                      int bins, int xLoc, int yLoc) {

        this.title = title;
        this.labelXAxis = labelXAxis;
        this.labelYAxis = labelYAxis;
        this.bins = bins;
        values = new double[bins];
        this.xLoc = xLoc;
        this.yLoc = yLoc;

        createChart();
        createUI();
    }

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Creates a new empty histogram
     */
    void createChart() {

        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.FREQUENCY);
        dataset.addSeries("", this.values, bins);

        chart = ChartFactory.createHistogram
                (title,
                 labelXAxis,
                 labelYAxis,
                 dataset,
                 PlotOrientation.VERTICAL,
                 false,
                 false,
                 false
                );
    }

    /**
     * Updates the histogram dataset and refreshes the display.
     *
     * @param values a double[], the values of the histogram
     * @param strTime a String, current time of the simulation
     */
    public void refresh(double[] values, String strTime) {

        this.values = values;
        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.FREQUENCY);
        dataset.addSeries("", values, bins);
        chart.getXYPlot().setDataset(dataset);
        chart.setTitle(title + " - " + strTime);
    }

    //---------- End of class
}
