package ichthyop.util;

import java.awt.*;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import ichthyop.*;
import ichthyop.datanc.*;

//------------------------------------------------------------------------------
public class SimuPanel
    extends JPanel {

  private Simulation simulation;
  private static int hi, wi;
  private Raster rasterFond;
  private static double LAT_MIN;
  private static double LAT_MAX;
  private static double LON_MIN;
  private static double LON_MAX;
  private static Color clrBG_B;
  private static Color clrBG_S;
  private static Color clrIndiv_min;
  private static Color clrIndiv_max;
  private static float valmin, valmax;
  static ArrayList listZones;

  private BufferedImage bfImgBG, bfImgSimu;
  private Graphics2D g2dBG, g2dSimu;

  //----------------------------------------------------------------------------
  public SimuPanel(Simulation simulation) {

    this.simulation = simulation;
    LAT_MIN = GetData.getLatMin();
    LAT_MAX = GetData.getLatMax();
    LON_MIN = GetData.getLonMin();
    LON_MAX = GetData.getLonMax();
    hi = -1;
    wi = -1;
    clrBG_B = new Color(0, 0, 150);
    clrBG_S = Color.CYAN;
    clrIndiv_min = Color.YELLOW;
    clrIndiv_max = Color.RED;
    listZones = new ArrayList(0);


    bfImgBG = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    bfImgSimu = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

  }

  //----------------------------------------------------------------------------
  public void paintComponent(Graphics g) {

    Graphics2D g2D = (Graphics2D) g;

    if (simulation != null) {
      final int h = this.getHeight();
      final int w = this.getWidth();
      if (hi != h || wi != w) {
        bfImgBG = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        g2dBG = bfImgBG.createGraphics();
        drawBG(g2dBG, w, h);
        rasterFond = bfImgBG.getRaster();
        hi = h;
        wi = w;
      }
      bfImgSimu = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      bfImgSimu.setData(rasterFond);
      g2dSimu = bfImgSimu.createGraphics();
      simulation.draw(g2dSimu, w, h);
      g2D.drawImage(bfImgSimu, 0, 0, this);
    }
  }

  //----------------------------------------------------------------------------
  public static int[] xy2hv(double x, double y, int w, int h) {

    final int i = (int) (x);
    final int j = (int) (y);
    final double dx = x - i;
    final double dy = y - j;
    final double[] hv1 = ij2hvR(i, j, w, h);
    final double[] hv2 = ij2hvR(i + 1, j, w, h);
    final double[] hv3 = ij2hvR(i, j + 1, w, h);
    final double[] hv4 = ij2hvR(i + 1, j + 1, w, h);
    int[] hv = new int[2];
    for (int l = 0; l < 2; l++) {
      hv[l] = (int) ( (1.0f - dx) * (1.0f - dy) * hv1[l] +
                     dx * (1.0f - dy) * hv2[l] +
                     (1.0f - dx) * dy * hv3[l] + dx * dy * hv4[l]);
    }
    return (hv);
  }

// -----------------------------------------------------------------------------
  private static double[] ij2hvR(int i, int j, int w, int h) {
    double[] hv = new double[2];
    hv[0] = ( (GetData.getLon(i, j) - LON_MIN) * w /
             Math.abs(LON_MAX - LON_MIN));
    hv[1] = h -
        ( (GetData.getLat(i, j) - LAT_MIN) * h /
         Math.abs(LAT_MAX - LAT_MIN));
    return (hv);
  }

  //----------------------------------------------------------------------------
  public static void init() {
    hi = -1;
    wi = -1;
    listZones.clear();
    if (MainFrame.isBG_SZone()) listZones.addAll(GetConfig.getReleaseZones());
    if (MainFrame.isBG_RZone()) listZones.addAll(GetConfig.getRecruitmentZones());
  }

// -----------------------------------------------------------------------------
  private static void drawBG(Graphics G, int w, int h) {

    G.setColor(new Color(223, 212, 200));
    G.fillRect(0, 0, w, h);
    for (int i = 1; i < (GetData.get_nx() - 1); i++) {
      for (int j = 1; j < (GetData.get_ny() - 1); j++) {
        drawCell(G, i, j, w, h);
      }
    }
  }

  //----------------------------------------------------------------------------
  private static Color getBottomColor(double depth) {

    float xdepth = 0.f;
    if (depth < 0.1f) {
      return (Color.darkGray);
    }
    else {
      xdepth = (float) Math.abs( (GetData.getDepthMax() - depth) /
                                GetData.getDepthMax());

    }
    return (new Color( (int) (xdepth * clrBG_S.getRed() +
                              (1 - xdepth) * clrBG_B.getRed()),
                      (int) (xdepth * clrBG_S.getGreen() +
                             (1 - xdepth) * clrBG_B.getGreen()),
                      (int) (xdepth * clrBG_S.getBlue() +
                             (1 - xdepth) * clrBG_B.getBlue())));

  }

//------------------------------------------------------------------------------
  public static Color getColorIndividual(double value) {

    float xval = 0.f;
    xval = bound((valmax - value) / (valmax - valmin));
    return (new Color( ( (int) (xval * clrIndiv_min.getRed() +
                                (1 - xval) * clrIndiv_max.getRed())),
                      ( (int) (xval * clrIndiv_min.getGreen() +
                               (1 - xval) * clrIndiv_max.getGreen())),
                      ( (int) (xval * clrIndiv_min.getBlue() +
                               (1 - xval) * clrIndiv_max.getBlue()))));

  }

//------------------------------------------------------------------------------
  public static Color getColorIndividual(int nZone) {

    Zone znTmp = (Zone) GetConfig.getReleaseZones().get(nZone);
    return znTmp.getColorZone();
  }

  //----------------------------------------------------------------------------
  private static void drawCell(Graphics AreaTrace, int i, int j, int w, int h) {

    final int[] hv1 = xy2hv(i - 0.5f, j - 0.5f, w, h);
    final int[] hv2 = xy2hv(i + 0.5f, j - 0.5f, w, h);
    final int[] hv3 = xy2hv(i + 0.5f, j + 0.5f, w, h);
    final int[] hv4 = xy2hv(i - 0.5f, j + 0.5f, w, h);
    Polygon pol = new Polygon();
    pol.addPoint(hv1[0], hv1[1]);
    pol.addPoint(hv2[0], hv2[1]);
    pol.addPoint(hv3[0], hv3[1]);
    pol.addPoint(hv4[0], hv4[1]);

    AreaTrace.setColor(getCellColor(i, j));

    AreaTrace.fillPolygon(pol);
    //AreaTrace.drawPolygon(pol);

  }

  //----------------------------------------------------------------------------
  private static Color getCellColor(int i, int j) {

    if (GetData.isInWater(i, j)) {
      Color clrZn = getBottomColor(GetData.getBathy(i, j));
      boolean foundZone = false;
      Iterator iter = listZones.iterator();
      while (!foundZone && iter.hasNext()) {
        Zone znTmp = (Zone) iter.next();
        if (znTmp.isXYInZone(i, j)) {
          clrZn = znTmp.getColorZone();
          foundZone = true;
        }
      }
      return (clrZn);
    }
    else {
      return Color.darkGray;
    }
  }

  //----------------------------------------------------------------------------
  private static float bound(double x) {

    return (float)Math.max(Math.min(1, x), 0);
  }

  //----------------------------------------------------------------------------
  public static void setValMin_Max(float val_min, float val_max) {

    valmin = val_min;
    valmax = val_max;
  }

//------------------------------------------------------------------------------
// End of class
}
