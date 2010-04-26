/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.arch;

import org.previmer.ichthyop.event.NextStepListener;

/**
 *
 * @author pverley
 */
public interface IDataset extends NextStepListener {

    public void setUp();

    public double[] lonlat2xy(double lon, double lat);

    public double[] xy2lonlat(double xRho, double yRho);

    public double depth2z(double x, double y, double depth);

    public double z2depth(double x, double y, double z);

    double get_dUx(double[] pGrid, double time);

    double get_dVy(double[] pGrid, double time);

    double get_dWz(double[] pGrid, double time);

    abstract public double adimensionalize(double number, double xRho, double yRho);

    public boolean isInWater(double[] pGrid);

    public boolean isInWater(int i, int j);

    boolean isCloseToCost(double[] pGrid);

    public boolean isOnEdge(double[] pGrid);

    public double getBathy(int i, int j);

    public int get_nx();

    public int get_ny();

    public int get_nz();

    public double getdxi(int j, int i);

    public double getdeta(int j, int i);

    public void init();

    public Number get(String variableName, double[] pGrid, double time);

    public void requireVariable(String name);

    public double getLatMin();

    public double getLatMax();

    public double getLonMin();

    public double getLonMax();

    public double getLon(int igrid, int jgrid);

    public double getLat(int igrid, int jgrid);

    public double getDepthMax();

    public boolean is3D();
}
