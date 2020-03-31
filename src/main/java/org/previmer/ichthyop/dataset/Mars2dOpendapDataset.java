/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import java.io.IOException;
import org.previmer.ichthyop.event.NextStepEvent;

/**
 *
 * @author pverley
 */
public class Mars2dOpendapDataset extends Mars2dCommon {

    /**
     * Loads the NetCDF dataset from the specified filename.
     * @param opendapURL a String that can be a local pathname or an OPeNDAP URL.
     * @throws IOException
     */
    @Override
    void openDataset() throws Exception {
        ncIn = DatasetUtil.openURL(getParameter("opendap_url"), true);
        readTimeLength();
    }

    @Override
    void setOnFirstTime() throws Exception {
        double t0 = getSimulationManager().getTimeManager().get_tO();
        rank = DatasetUtil.rank(t0, ncIn, strTime, timeArrow);
        time_tp1 = t0;
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {
        double time = e.getSource().getTime();
        //Logger.getAnonymousLogger().info("set fields at time " + time);

        if (timeArrow * time < timeArrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        rank += timeArrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            throw new IndexOutOfBoundsException(ErrorMessage.TIME_OUTOF_BOUND.message());
        }
        setAllFieldsTp1AtTime(rank);
    }
}
