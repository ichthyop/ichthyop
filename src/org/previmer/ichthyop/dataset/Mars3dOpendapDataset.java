/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import org.previmer.ichthyop.event.NextStepEvent;

/**
 *
 * @author pverley
 */
public class Mars3dOpendapDataset extends Mars3dCommon {

    @Override
    void openDataset() throws Exception {
        ncIn = DatasetIO.openURL(getParameter("opendap_url"));
        readTimeLength();
    }

    @Override
    void setOnFirstTime() throws Exception {
        long t0 = getSimulationManager().getTimeManager().get_tO();
        DatasetIO.checkInitTime(ncIn, strTime);
        rank = findCurrentRank(t0);
        time_tp1 = t0;
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
        w_tp0 = w_tp1;
        zeta_tp0 = zeta_tp1;
        if (z_w_tp1 != null) {
            z_w_tp0 = z_w_tp1;
        }
        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            throw new IndexOutOfBoundsException(ErrorMessage.TIME_OUTOF_BOUND.message());
        }
        setAllFieldsTp1AtTime(rank);
    }
}
