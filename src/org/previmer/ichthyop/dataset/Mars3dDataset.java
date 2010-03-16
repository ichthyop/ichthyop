/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import org.previmer.ichthyop.event.NextStepEvent;
import java.io.IOException;
import java.util.logging.Level;

/**
 *
 * @author pverley
 */
public class Mars3dDataset extends Mars3dDatasetCommon {


    public void setUp() {

        loadParameters();
        MarsDatasetIO.setTimeField(strTime);

        try {
            ncIn = MarsDatasetIO.openLocation(getParameter("input_path"), getParameter("file_filter"));
            nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
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
            ncIn = MarsDatasetIO.open(MarsDatasetIO.getFile(t0));
            nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
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
                ncIn = MarsDatasetIO.open(MarsDatasetIO.getNextFile(time_arrow));
                nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
                rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
            }
            setAllFieldsTp1AtTime(rank);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }
}
