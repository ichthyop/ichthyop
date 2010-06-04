/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.dataset;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.event.NextStepEvent;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author pverley
 */
public class Mars3dOpendapDataset extends Mars3dCommon {

    void openDataset() throws IOException {
        ncIn = MarsIO.openURL(getParameter("opendap_url"));
        nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
    }

    public void init() {
        try {
            long t0 = getSimulationManager().getTimeManager().get_tO();
            checkRequiredVariable(ncIn);
            setAllFieldsTp1AtTime(rank = findCurrentRank(t0));
            time_tp1 = t0;
        } catch (InvalidRangeException ex) {
            getLogger().log(Level.SEVERE, ErrorMessage.INIT.message(), ex);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, ErrorMessage.INIT.message(), ex);
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
        if (z_w_tp1 != null) {
            z_w_tp0 = z_w_tp1;
        }
        rank += time_arrow;
        try {
            if (rank > (nbTimeRecords - 1) || rank < 0) {
                throw new IndexOutOfBoundsException(ErrorMessage.TIME_OUTOF_BOUND.message());
            }
            setAllFieldsTp1AtTime(rank);
        } catch (InvalidRangeException ex) {
            getLogger().log(Level.SEVERE, ErrorMessage.NEXT_STEP.message(), ex);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, ErrorMessage.NEXT_STEP.message(), ex);
        }
    }
}
