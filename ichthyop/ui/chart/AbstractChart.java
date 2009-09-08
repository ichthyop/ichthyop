package ichthyop.ui.chart;

/** import AWT */
import java.awt.GridBagLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.DisplayMode;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Dimension;

/** import Swing */
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/** import JFreeChart */
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;

/**
 * This abstract class creates a JFreeChart object within a JFrame and sets it
 * on screen at a specific location.
 *
 * @author P.Verley 2007
 * @see <a href="http://www.jfree.org/">JFreeChart</a>
 * @see java.swing.JFrame
 */
abstract class AbstractChart extends JFrame {


///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * The JFreeChart object that will be added to the contentPane()
     * @see <href=http://www.jfree.org/jfreechart/api/gjdoc/index.html>
     * JFreeChart javadoc</href>
     */
    JFreeChart chart;
    /**
     * Coordinate of the top-left corner of this JFrame
     */
    int xLoc, yLoc;
    /**
     * Title of the chart
     */
    String title;
    /**
     * X-axis label
     */
    String labelXAxis;
    /**
     * Y-axis label
     */
    String labelYAxis;

/////////////////////////////////////
// Declaration of the abstract method
/////////////////////////////////////

    /**
     * Creates the specific JFreeChart object:
     * <ul>
     * <li>XYAreaChart
     * <li>StackedXYAreaChart
     * <li>StackedBarChart
     * <li>Histogram
     * </ul>
     */
    abstract void createChart();

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Creates, sizes and locates the JFrame on screen.
     */
    void createUI() {

        this.getContentPane().setLayout(new GridBagLayout());
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        GraphicsDevice ecran = GraphicsEnvironment.getLocalGraphicsEnvironment().
                               getDefaultScreenDevice();
        GraphicsConfiguration config = ecran.getDefaultConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
        DisplayMode mode = config.getDevice().getDisplayMode();
        Dimension size = new Dimension(mode.getWidth(), mode.getHeight());
        this.setSize((int) (0.25f * (size.width - insets.left - insets.right)),
                     (int) (0.5 * (size.height - insets.bottom - insets.top)));
        this.setTitle("IBM - Control graph");
        this.setLocation(xLoc, yLoc);

        this.setContentPane(new ChartPanel(chart));
        this.setVisible(true);
    }

    //---------- End of class
}
