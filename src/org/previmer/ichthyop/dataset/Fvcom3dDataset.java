
package org.previmer.ichthyop.dataset;

import org.previmer.ichthyop.event.NextStepEvent;

/**
 *
 * @author pverley
 */
public class Fvcom3dDataset extends AbstractDataset {

    @Override
    void loadParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setUp() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double[] lonlat2xy(double lon, double lat) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double[] xy2lonlat(double xRho, double yRho) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double depth2z(double x, double y, double depth) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double z2depth(double x, double y, double z) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double get_dUx(double[] pGrid, double time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double get_dVy(double[] pGrid, double time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double get_dWz(double[] pGrid, double time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isInWater(double[] pGrid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isInWater(int i, int j) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isCloseToCost(double[] pGrid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isOnEdge(double[] pGrid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getBathy(int i, int j) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int get_nx() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int get_ny() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int get_nz() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getdxi(int j, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getdeta(int j, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void init() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getLatMin() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getLatMax() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getLonMin() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getLonMax() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getLon(int igrid, int jgrid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getLat(int igrid, int jgrid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getDepthMax() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean is3D() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void nextStepTriggered(NextStepEvent e) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
