package ichthyop;

import java.awt.*;
import ichthyop.datanc.*;

/**
 * This class defines a geographical area used by the program to locate
 * the releasing areas or the recruitment areas. It gives some tools to use the
 * zone within the program.
 * <p></p>
 * The geographical area is defined as followed :
 * Four geodesics points {lon, lat} P1, P2, P3 & P4 and two depths depth1 & depth2.
 * <pre>
 * lat = 40°N => lat = 40.0
 * lat = 32.7°S => lat = -32.7
 * lon = 70.2°W => lon = -70.2
 * lon = 15°E => lon = 15.0
 * The depths are positive Integers.
 * </pre>
 * The area is first delimited by the polygon (P1 P2 P3 P4).
 * The points dont have to be in the water, but make sure they belong to the geographical grid of the simulation.
 * The four points P1(plon1, plat1) to P4(plon4, plat4) must be recorded in the clockwise or anticlockwise direction.
 * Then, another routine isolates the area of the polygone that is contained between the bathymetric lines depth1 & depth2.
 * At last the user determines the color (Red[0, 255], Green[0, 255], Blue[0, 255]) of the area for the stepGO.
 */

public class Zone {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the variables
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  /**
   * Type of zone : RELEASING or RECRUITMENT
   */
  final private int typeZone;
  /**
   * Longitude of the geographic point P(lon, lat)
   */
  final private double[] lon;
  /**
   * Latitude of the geographic point P(lon, lat)
   */
  final private double[] lat;
  /**
   * Bathymetric lines
   */
  final private double bathyLineMin, bathyLineMax;
  /**
   * Color RGB of the zone.
   */
  private Color colorZone;

  private int indexZone;

  private double[] xGrid, yGrid;

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Constructors
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  /**
   * The four points P1(plon1, plat1) to P4(plon4, plat4) must be recorded
   * in the clockwise or anticlockwise direction.
   *<pre>
   * lat = 40°N => lat = 40.0
   * lat = 32.7°S => lat = -32.7
   * lon = 70.2°W => lon = -70.2
   * lon = 15°E => lon = 15.0
   * The depths are positive Integers.
   * colorZone = Color(Red[0, 255], Green[0, 255], Blue[0, 255])
   * </pre>
   */
  public Zone(int typeZone, int indexZone, double lon1, double lat1,
      double lon2, double lat2
      , double lon3, double lat3, double lon4, double lat4,
      double depth1,
      double depth2, Color colorZone) {

    lon = new double[4];
    lat = new double[4];

    this.typeZone = typeZone;
    this.indexZone = indexZone;
    this.lon[0] = lon1;
    this.lon[1] = lon2;
    this.lon[2] = lon3;
    this.lon[3] = lon4;
    this.lat[0] = lat1;
    this.lat[1] = lat2;
    this.lat[2] = lat3;
    this.lat[3] = lat4;
    this.bathyLineMin = depth1;
    this.bathyLineMax = depth2;
    this.colorZone = colorZone;

    xGrid = new double[5];
    yGrid = new double[5];

  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the methods
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  public Zone(int typeZone, int indexZone) {

    this(typeZone, indexZone, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Color.WHITE);
  }

  //----------------------------------------------------------------------------
  /**
   * Transforms longitude and latitude into grid coordinates
   */
  public void geo2Grid() {
    for (int i = 0; i < 4; i++) {
      double[] po = GetData.geo2Grid(lon[i], lat[i]);
      xGrid[i] = po[0]; // xgrid
      yGrid[i] = po[1]; // ygrid
    }
    xGrid[4] = xGrid[0];
    yGrid[4] = yGrid[0];
  }

  //----------------------------------------------------------------------------
  /**
   * Checks out whether the grid point (x, y) belongs to the zone
   * @param x : xgrid
   * @param y : ygrid
   * @return TRUE if (x, y) belongs to the zone
   */
  public boolean isXYInZone(double x, double y) {

    boolean isInBox;
    int inc, crossings;
    double dx1, dx2, dxy;

    isInBox = true;
    crossings = 0;

    for (int k = 0; k < 4; k++) {
      if (xGrid[k] != xGrid[k + 1]) {
        dx1 = x - xGrid[k];
        dx2 = xGrid[k + 1] - x;
        dxy = dx2 * (y - yGrid[k]) - dx1 * (yGrid[k + 1] - y);
        inc = 0;
        if ( (xGrid[k] == x) & (yGrid[k] == y)) {
          crossings = 1;
        }
        else if ( ( (dx1 == 0.) & (y >= yGrid[k])) |
            ( (dx2 == 0.) & (y >= yGrid[k + 1]))) {
          inc = 1;
        }
        else if ( (dx1 * dx2 > 0.) &
            ( (xGrid[k + 1] - xGrid[k]) * dxy >= 0.)) {
          inc = 2;
        }
        if (xGrid[k + 1] > xGrid[k]) {
          crossings += inc;
        }
        else {
          crossings -= inc;
        }
      }
    }
    if (crossings == 0) {
      isInBox = false;
    }

    if (isInBox) {
      isInBox = (GetData.getBathy( (int) Math.round(x), (int) Math.round(y)) >
          bathyLineMin &
          GetData.getBathy( (int) Math.round(x), (int) Math.round(y)) <
          bathyLineMax);
    }

    return (isInBox);
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the getters ans setters
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  public double getLon(int i) {
    return lon[i];
  }

  //----------------------------------------------------------------------------
  public double getLat(int i) {
    return lat[i];
  }

  //----------------------------------------------------------------------------
  public double getBathyMin() {
    return bathyLineMin;
  }

  //----------------------------------------------------------------------------
  public double getBathyMax() {
    return bathyLineMax;
  }

  //----------------------------------------------------------------------------
  public Color getColorZone() {
    return colorZone;
  }

//------------------------------------------------------------------------------
  public int getTypeZone() {
    return typeZone;
  }

//------------------------------------------------------------------------------
  public int getIndexZone() {
    return indexZone;
  }

  //----------------------------------------------------------------------------
  public void setIndexZone(int indexZone) {
    this.indexZone = indexZone;
  }

  //----------------------------------------------------------------------------
  public double getXmin() {
    double xmin = xGrid[0];
    for (int i = 1; i < 4; i++) {
      xmin = Math.min(xmin, xGrid[i]);
    }
    return xmin;
  }

  //----------------------------------------------------------------------------
  public double getYmin() {
    double ymin = yGrid[0];
    for (int i = 1; i < 4; i++) {
      ymin = Math.min(ymin, yGrid[i]);
    }
    return ymin;
  }

  //----------------------------------------------------------------------------
  public double getXmax() {
    double xmax = xGrid[0];
    for (int i = 1; i < 4; i++) {
      xmax = Math.max(xmax, xGrid[i]);
    }
    return xmax;
  }

  //----------------------------------------------------------------------------
  public double getYmax() {
    double ymax = yGrid[0];
    for (int i = 1; i < 4; i++) {
      ymax = Math.max(ymax, yGrid[i]);
    }
    return ymax;
  }

//------------------------------------------------------------------------------
// End of class
}
