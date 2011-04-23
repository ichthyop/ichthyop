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
public class Mars2dDataset extends Mars2dCommon {

    void openDataset() throws Exception {
        MarsIO.setTimeField(strTime);
        boolean skipSorting = false;
            try {
                skipSorting = Boolean.valueOf(getParameter("skip_sorting"));
            } catch (Exception ex) {
                skipSorting = false;
            }
        ncIn = MarsIO.openLocation(getParameter("input_path"), getParameter("file_filter"), skipSorting);
        readTimeLength();
    }

    void setOnFirstTime() throws Exception {

        long t0 = getSimulationManager().getTimeManager().get_tO();
        ncIn = MarsIO.openFile(MarsIO.getFile(t0));
        readTimeLength();
        rank = findCurrentRank(t0);
        time_tp1 = t0;
    }

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
            ncIn.close();
            ncIn = MarsIO.openFile(MarsIO.getNextFile(time_arrow));
            nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }
        setAllFieldsTp1AtTime(rank);

    }
}
