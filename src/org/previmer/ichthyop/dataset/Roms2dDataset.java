/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import java.io.IOException;
import org.previmer.ichthyop.dataset.MarsCommon.ErrorMessage;
import org.previmer.ichthyop.event.NextStepEvent;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
public class Roms2dDataset extends RomsCommon {

    /**
     * Zonal component of the velocity field at current time
     */
    static float[][] u_tp0;
    /**
     * Zonal component of the velocity field at time t + dt
     */
    static float[][] u_tp1;
    /**
     * Meridional component of the velocity field at current time
     */
    static float[][] v_tp0;
    /**
     *  Meridional component of the velocity field at time t + dt
     */
    static float[][] v_tp1;

    @Override
    public boolean is3D() {
        return false;
    }

    @Override
    public double depth2z(double x, double y, double depth) {
        throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());
    }

    @Override
    public double z2depth(double x, double y, double z) {
        throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());
    }

    @Override
    public double get_dWz(double[] pGrid, double time) {
        throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());
    }

    @Override
    public double get_dVy(double[] pGrid, double time) {
        double dv = 0.d;
        double ix, jy;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (n == 1) ? (int) Math.round(ix) : (int) ix;
        int j = (int) Math.round(jy);
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double CO = 0.d;
        double co;
        double x;

        for (int jj = 0; jj < 2; jj++) {
            for (int ii = 0; ii < n; ii++) {
                co = Math.abs((1.d - (double) ii - dx)
                        * (.5d - (double) jj - dy));
                CO += co;
                x = (1.d - x_euler) * v_tp0[j + jj - 1][i + ii] + x_euler * v_tp1[j + jj - 1][i + ii];
                dv += .5d * x * co * (pn[Math.max(j + jj - 1, 0)][i + ii] + pn[j + jj][i + ii]);

            }
        }
        if (CO != 0) {
            dv /= CO;
        }
        return dv;
    }

    @Override
    public double get_dUx(double[] pGrid, double time) {

        double du = 0.d;
        double ix, jy;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (int) Math.round(ix);
        int j = (n == 1) ? (int) Math.round(jy) : (int) jy;
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double CO = 0.d;
        double co;
        double x;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < n; jj++) {

                co = Math.abs((.5d - (double) ii - dx)
                        * (1.d - (double) jj - dy));
                CO += co;
                x = (1.d - x_euler) * u_tp0[j + jj][i + ii - 1] + x_euler * u_tp1[j + jj][i + ii - 1];
                du += .5d * x * co * (pm[j + jj][Math.max(i + ii - 1, 0)] + pm[j + jj][i + ii]);
            }
        }
        if (CO != 0) {
            du /= CO;
        }
        return du;
    }

    @Override
    public int get_nz() {
        throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        long time = e.getSource().getTime();
        //Logger.getAnonymousLogger().info("set fields at time " + time);
        int time_arrow = (int) Math.signum(e.getSource().get_dt());

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            open(getNextFile(time_arrow));
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }
        setAllFieldsTp1AtTime(rank);
    }

    @Override
    void setAllFieldsTp1AtTime(int rank) throws Exception {

        int[] origin = new int[]{rank, 0, jpo, ipo};
        double time_tp0 = time_tp1;

        try {
            u_tp1 = (float[][]) ncIn.findVariable(strU).read(origin, new int[]{1, ny, (nx - 1)}).reduce().copyToNDJavaArray();

        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset U velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            v_tp1 = (float[][]) ncIn.findVariable(strV).read(origin,
                    new int[]{1, (ny - 1), nx}).reduce().copyToNDJavaArray();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset V velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            Array xTimeTp1 = ncIn.findVariable(strTime).read();
            time_tp1 = xTimeTp1.getDouble(xTimeTp1.getIndex().set(rank));
            time_tp1 -= time_tp1 % 100;
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
        for (RequiredVariable variable : requiredVariables.values()) {
            variable.nextStep(readVariable(ncIn, variable.getName(), rank), time_tp1, dt_HyMo);
        }
    }

    @Override
    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception {
        Variable variable = nc.findVariable(name);
        int[] origin = null, shape = null;
        switch (variable.getShape().length) {
            case 2:
                origin = new int[]{jpo, ipo};
                shape = new int[]{ny, nx};
                break;
            case 3:
                origin = new int[]{rank, jpo, ipo};
                shape = new int[]{1, ny, nx};
                break;
            default:
                throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());

        }

        return variable.read(origin, shape).reduce();
    }
}
