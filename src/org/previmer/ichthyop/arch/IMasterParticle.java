/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

/**
 *
 * @author pverley
 */
public interface IMasterParticle extends IBasicParticle {

    public void setIndex(int index);

    public void step();

    public void setX(double x);

    public void setY(double y);

    public void setZ(double z);

    public void setLon(double lon);

    public void setLat(double lat);

    public void setDepth(double depth);

    public void grid2Geo();

    public void geo2Grid();

    public void make2D();

    public boolean isInWater();

    public boolean isOnEdge();

    public void incrementAge();

    public void applyMove(boolean reflexiveBoundary);
}
