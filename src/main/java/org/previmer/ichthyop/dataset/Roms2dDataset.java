package org.previmer.ichthyop.dataset;

import java.io.IOException;
import java.util.List;
import org.previmer.ichthyop.dataset.MarsCommon.ErrorMessage;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.io.IOTools;
import static org.previmer.ichthyop.io.IOTools.isFile;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
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
     * Meridional component of the velocity field at time t + dt
     */
    static float[][] v_tp1;
    private List<String> ncfiles;
    private int ncindex;

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
    public double get_dVy(double[] pGrid, double time, boolean normalize) {
        double dv = 0.d;
        double ix, jy;

        ix = pGrid[0];
        jy = pGrid[1];

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

        double CO = 0.d;
        double co;
        double x;

        // Recover the index of the T points which are used for
        // interpolating the scale factors
        int j = (int) Math.floor(jy - 0.5);  // (j, i) is the grid cell index of V point to select (low left)
        int i = (int) Math.floor(ix);

        for (int jj = 0; jj < 2; jj++) {
            double coy = 1 - Math.abs(jy - (j + 0.5 + jj));
            for (int ii = 0; ii < 2; ii++) {
                double cox = 1 - Math.abs(ix - (i + ii));
                co = cox * coy;

                // get the dY value on the V point
                double pnv = this.get_pnv(i + ii, j + jj);

                x = (1.d - x_euler) * v_tp0[j + jj][i + ii] + x_euler * v_tp1[j + jj][i + ii];
                if (!Double.isNaN(x) && (x != 0)) {
                    CO += co;
                    if(normalize) {
                        dv += x * co * pnv;
                    } else {
                        dv += x * co;
                    }
                }

            }
        }
        if (CO != 0) {
            dv /= CO;
        }
        return dv;
    }

    @Override
    public double get_dUx(double[] pGrid, double time, boolean normalize) {

        double du = 0.d;
        double ix, jy;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

        double CO = 0.d;
        double co;
        double x;

        // Index of the closest point on the lower left in U space.
        // Shift by -0.5 for moving from Tpoint to Uspace.
        int i = (int) Math.floor(ix - 0.5);
        int j = (int) Math.floor(jy);

        for (int ii = 0; ii < 2; ii++) {
            double cox = 1 - Math.abs((ix - (i + 0.5 + ii)));
            for (int jj = 0; jj < 2; jj++) {

                double coy = 1 - Math.abs((jy - (j + jj)));
                co = cox * coy;

                // Get the dX value on the U point
                double pmu = this.get_pmu(i + ii, j + jj);

                x = (1.d - x_euler) * u_tp0[j + jj][i + ii] + x_euler * u_tp1[j + jj][i + ii];
                if (!Double.isNaN(x) && (x != 0)) {
                    CO += co;
                    if (normalize) {
                        du += x * co * pmu;
                    } else {
                        du += x * co;
                    }
                }
            }
        }
        if (CO != 0) {
            du /= CO;
        }
        return du;
    }

    @Override
    public int get_nz() {
       return 1;
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = e.getSource().getTime();
        //Logger.getAnonymousLogger().info("set fields at time " + time);
        int time_arrow = (int) Math.signum(e.getSource().get_dt());

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            ncindex = DatasetUtil.next(ncfiles, ncindex, time_arrow);
            ncIn = DatasetUtil.openFile(ncfiles.get(ncindex), true);
            readTimeLength();
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }
        setAllFieldsTp1AtTime(rank);
    }

    @Override
    void setAllFieldsTp1AtTime(int rank) throws Exception {

        getLogger().info("Reading NetCDF variables...");

        int[] origin ;
        int[] count;

        double time_tp0 = time_tp1;
        Array arr;
        Index index;

        try {
            // For U, we read only on the inner domain
            // but we have an extra U to read, which is one value less that T point.
            // i.e. inner T domain starts at 1, inner U domain starts at 0
            origin = new int[]{rank, jpo, ipo};
            count = new int[]{rank, ny, nx - 1};
            arr = ncIn.findVariable(strU).read(origin, count).reduce();
            u_tp1 = new float[count[1]][count[2]];
            index = arr.getIndex();
            for (int j = 0; j < count[1]; j++) {
                for (int i = 0; i < count[2]; i++) {
                    index.set(j, i);
                    u_tp1[j][i] = arr.getFloat(index);
                }
            }
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading dataset U velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            origin = new int[]{rank, jpo, ipo};
            count = new int[]{rank, ny - 1, nx};
            arr = ncIn.findVariable(strV).read(origin,count).reduce();
            v_tp1 = new float[count[1]][count[2]];
            index = arr.getIndex();
            for (int j = 0; j < count[1]; j++) {
                for (int i = 0; i < count[2]; i++) {
                    index.set(j, i);
                    v_tp1[j][i] = arr.getFloat(index);
                }
            }
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading dataset V velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            time_tp1 = DatasetUtil.timeAtRank(ncIn, strTime, rank);
        } catch (IOException ex) {
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
    // WARNING: Check that for reading of T and U/V points
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

    @Override
    void openDataset() throws Exception {

        ncfiles = DatasetUtil.list(getParameter("input_path"), getParameter("file_filter"));
        if (!skipSorting()) {
            DatasetUtil.sort(ncfiles, strTime, timeArrow());
        }
        ncIn = DatasetUtil.openFile(ncfiles.get(0), true);
        readTimeLength();

        try {
            if (!getParameter("grid_file").isEmpty()) {
                String path = IOTools.resolveFile(getParameter("grid_file"));
                if (!isFile(path)) {
                    throw new IOException("{Dataset} " + getParameter("grid_file") + " is not a valid file.");
                }
                gridFile = path;
            } else {
                gridFile = ncIn.getLocation();
            }
        } catch (NullPointerException ex) {
            gridFile = ncIn.getLocation();
        }
    }

    @Override
    void setOnFirstTime() throws Exception {
        double t0 = getSimulationManager().getTimeManager().get_tO();
        ncindex = DatasetUtil.index(ncfiles, t0, timeArrow(), strTime);
        ncIn = DatasetUtil.openFile(ncfiles.get(ncindex), true);
        readTimeLength();
        rank = DatasetUtil.rank(t0, ncIn, strTime, timeArrow());
        time_tp1 = t0;
    }

    @Override
    public double getBottomDepth(double[] pGrid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBottomDepth'");
    }
}
