/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import java.io.IOException;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.io.IOTools;
import static org.previmer.ichthyop.io.IOTools.isDirectory;

/**
 *
 * @author pverley
 */
public class Roms3dDataset extends Roms3dCommon {

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
            ncIn = DatasetIO.openFile(DatasetIO.getNextFile(time_arrow));
            readTimeLength();
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }
        setAllFieldsTp1AtTime(rank);
    }

    @Override
    void openDataset() throws Exception {

        DatasetIO.setTimeField(strTime);
        boolean skipSorting;
        try {
            skipSorting = Boolean.valueOf(getParameter("skip_sorting"));
        } catch (Exception ex) {
            skipSorting = false;
        }
        ncIn = DatasetIO.openLocation(getParameter("input_path"), getParameter("file_filter"), skipSorting);
        readTimeLength();

        try {
            if (!getParameter("grid_file").isEmpty()) {
                String path = IOTools.resolvePath(getParameter("grid_file"));
                if (!isDirectory(path)) {
                    throw new IOException("{Dataset} " + getParameter("grid_file") + " is not a valid directory.");
                }
            } else {
                gridFile = ncIn.getLocation();
            }
        } catch (NullPointerException ex) {
            gridFile = ncIn.getLocation();
        }
    }

    @Override
    void setOnFirstTime() throws Exception {
        long t0 = getSimulationManager().getTimeManager().get_tO();
        ncIn = DatasetIO.openFile(DatasetIO.getFile(t0));
        readTimeLength();
        rank = findCurrentRank(t0);
        time_tp1 = t0;
    }
}
