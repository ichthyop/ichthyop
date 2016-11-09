package ichthyop.util.chart;

import java.awt.*;
import javax.swing.*;

import org.jfree.chart.*;

// -----------------------------------------------------------------------------
abstract class AbstractChart
    extends JFrame {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the variables
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  JFreeChart chart;
  int xLoc, yLoc;
  String title, strX, strY;

  //----------------------------------------------------------------------------
  abstract void createChart();

  //----------------------------------------------------------------------------
  abstract public void refresh(double[] d, String strTime);

  //----------------------------------------------------------------------------
  void createUI() {

    this.getContentPane().setLayout(new GridBagLayout());
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    //this.setUndecorated(true);
    GraphicsDevice ecran = GraphicsEnvironment.getLocalGraphicsEnvironment().
        getDefaultScreenDevice();
    GraphicsConfiguration config = ecran.getDefaultConfiguration();
    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
    DisplayMode mode = config.getDevice().getDisplayMode();
    Dimension size = new Dimension(mode.getWidth(), mode.getHeight());
    this.setSize( (int) (0.25f * (size.width - insets.left - insets.right)),
        (int) (0.5 * (size.height - insets.bottom - insets.top)));
    this.setTitle("IBM - Control graph");
    this.setLocation(xLoc, yLoc);

    this.setContentPane(new ChartPanel(chart));
    this.show();
  }
}
