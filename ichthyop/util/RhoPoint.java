package ichthyop.util;

import ichthyop.datanc.*;

abstract public class RhoPoint {

  double x, y, z;
  double lon, lat, depth;
  float tp, sal;

//------------------------------------------------------------------------------
//  Definition of abstract methods
//---------------------------------

  abstract public void geog2Grid();

  abstract public void grid2Geog();

  //----------------------------------------------------------------------------
  public boolean setXY(double x, double y) {
    this.x = x;
    this.y = y;
    return GetData.isInWater(this);
  }

  //--------------------------------------------------------------------------
  public double getX() {
    return x;
  }

  //--------------------------------------------------------------------------
  public double getY() {
    return y;
  }

  //--------------------------------------------------------------------------
  public double getZ() {
    return z;
  }

  //----------------------------------------------------------------------------
  public void setX(double x) {
    this.x = x;
  }

  //----------------------------------------------------------------------------
  public void setY(double y) {
    this.y = y;
  }

  //----------------------------------------------------------------------------
  public void setZ(double z) {
    this.z = z;
  }

  //----------------------------------------------------------------------------
  public void setLon(double lon) {
    this.lon = lon;
  }

  //----------------------------------------------------------------------------
  public void setLat(double lat) {
    this.lat = lat;
  }

  //----------------------------------------------------------------------------
  public void setDepth(double depth) {
    this.depth = depth;
  }

  //--------------------------------------------------------------------------
  public boolean isOnEdge(int nx, int ny) {
    return ( (x > (nx - 2.0f)) ||
        (x < 1.0f) ||
        (y > (ny - 2.0f)) ||
        (y < 1.0f));
  }

  //----------------------------------------------------------------------------
  public double getLon() {
    return lon;
  }

  //----------------------------------------------------------------------------
  public double getLat() {
    return lat;
  }

  //--------------------------------------------------------------------------
  public double getDepth() {
    return depth;
  }

  //--------------------------------------------------------------------------
  public void incrementXYZ(double dx, double dy, double dz) {
    x += dx;
    y += dy;
    z += dz;
    z = Math.max(0.d, Math.min(z, (double) GetData.get_nz() - 1.00001f));
  }

  //--------------------------------------------------------------------------
  public void incrementXY(double dx, double dy) {
    x += dx;
    y += dy;
  }

  //--------------------------------------------------------------------------
  public double[] getXYZ() {
    return new double[] {
        x, y, z};
  }

  //--------------------------------------------------------------------------
  public double[] getXY() {
    return new double[] {
        x, y};
  }

  //--------------------------------------------------------------------------
  public void setXYD(double x, double y, double depth) {
    this.x = x;
    this.y = y;
    this.z = Math.min(GetData.zGeo2Grid(x, y, depth), GetData.get_nz() - 1);
  }

  //--------------------------------------------------------------------------
  public void setLLD(double lon, double lat, double depth) {
    this.lon = lon;
    this.lat = lat;
    this.depth = depth;
    geog2Grid();
  }

}
