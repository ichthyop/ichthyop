/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.dataset;

import java.io.IOException;
import java.util.logging.Level;
import org.previmer.ichthyop.event.NextStepEvent;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class Mars3dOpendapDataset extends Mars3dDatasetCommon {

    public void setUp() {
       loadParameters();

        try {
            open(getParameter("opendap_url"));
            getDimNC();
            if (Boolean.valueOf(getParameter("shrink_domain"))) {
                float[] p1 = new float[]{Float.valueOf(getParameter("north-west-corner.lon")), Float.valueOf(getParameter("north-west-corner.lat"))};
                float[] p2 = new float[]{Float.valueOf(getParameter("south-east-corner.lon")), Float.valueOf(getParameter("south-east-corner.lat"))};
                range(p1, p2);
            }
            readConstantField();
            getDimGeogArea();
            getCstSigLevels();
            z_w_tp0 = getSigLevels();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void init() {
        try {
            long t0 = getSimulationManager().getTimeManager().get_tO();
            FLAG_TP = FLAG_SAL = FLAG_VDISP = false;
            setAllFieldsTp1AtTime(rank = findCurrentRank(t0));
            time_tp1 = t0;
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void nextStepTriggered(NextStepEvent e) {
        long time = e.getSource().getTime();
        //Logger.getAnonymousLogger().info("set fields at time " + time);
        int time_arrow = (int) Math.signum(e.getSource().get_dt());

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        w_tp0 = w_tp1;
        zeta_tp0 = zeta_tp1;
        temp_tp0 = temp_tp1;
        salt_tp0 = salt_tp1;
        kv_tp0 = kv_tp1;
        if (z_w_tp1 != null) {
            z_w_tp0 = z_w_tp1;
        }
        rank += time_arrow;
        try {
            if (rank > (nbTimeRecords - 1) || rank < 0) {
                throw new IOException("Time OutOfBoundException");
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Loads the NetCDF dataset from the specified filename.
     * @param opendapURL a String that can be a local pathname or an OPeNDAP URL.
     * @throws IOException
     */
    private void open(String opendapURL) throws IOException {

        try {
            if (ncIn == null) {
                ncIn = NetcdfDataset.openFile(opendapURL, null);
                nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
            }
            System.out.print("Open remote dataset " + opendapURL + "\n");
        } catch (IOException e) {
            throw new IOException("Problem opening dataset "
                                  + opendapURL + " - " + e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem reading " + strTimeDim
                                  + " dimension at location " + opendapURL +
                                  " : " + e.getMessage());
        }
    }

}
