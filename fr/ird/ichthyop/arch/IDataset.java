/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.arch;

/**
 *
 * @author pverley
 */
public interface IDataset {

    //public double[] geo2Grid(double lon, double lat);
    public double[] lonlat2xy(double lon, double lat);

    //public double[] grid2Geo(double xRho, double yRho);
    public double[] xy2lonlat(double xRho, double yRho);

    public double depth2z(double x, double y, double depth);

    public double z2depth(double x, double y, double z);

    double[] advectEuler(double[] pGrid, double time, double dt);

    public double[] advectRk4(double[] p0, double time, double dt);

    abstract public double adimensionalize(double number, double xRho, double yRho);

    public boolean isInWater(double[] pGrid);

    public boolean isInWater(int i, int j);

    public boolean isOnEdge(double[] pGrid);

    public double getLat(int i, int j);

    public double getLatMax();

    public double getLatMin();

    public double getLon(int i, int j);

    public double getLonMax();

    public double getLonMin();

    public double getBathy(int i, int j);

    public double getDepth(double xRho, double yRho, int k);

    public float getDepthMax();

    public double getTemperature(double[] pGrid, double time);

    public double getSalinity(double[] pGrid, double time);

    public double getTimeTp1();

    public double getTime();

    public int get_nx();

    public int get_ny();

    public int get_nz();

    public void setAllFieldsAtTime(long time);

    public double getdxi(int j, int i);

    public double getdeta(int j, int i);

    public double[] getKv(double[] pGrid, double time, double dt);
}
