/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

/**
 *
 * @author pverley
 */
public interface IGridPoint extends Cloneable {

    /**
     * Gets the x grid coordinate
     * @return a double, the x grid coordinate of the particle
     */
    public double getX();

    public void setX(double x);

    /**
     * Gets the y grid coordinate
     * @return a double, the y grid coordinate of the particle
     */
    public double getY();

    public void setY(double y);

    /**
     * Gets the z grid coordinate
     * @return a double, the z grid coordinate of the particle
     */
    public double getZ();

    public void setZ(double z);

    public double[] getGridCoordinates();

    /**
     * Gets the longitude
     * @return a double, the longitude of the particle location [East degree]
     */
    public double getLon();

    public void setLon(double lon);

    /**
     * Gets the latitude
     * @return a double, the latitude of the particle location [North degree
     */
    public double getLat();

    public void setLat(double lat);

    /**
     * Gets the depth
     * @return a double, the depth of the particle [meter]
     */
    public double getDepth();

    public void setDepth(double depth);
    
    public double[] getGeoCoordinates();

    public void grid2Geo();

    public void geo2Grid();

    public void increment(double[] move);

    public void make2D();

    public boolean isInWater();

    public boolean isOnEdge();

}
