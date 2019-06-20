/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import java.util.List;
import org.previmer.ichthyop.event.NextStepEvent;

/**
 *
 * @author pverley
 */
public class Mars2dDataset extends Mars2dCommon {
    
    private List<String> files;
    private int index;

    @Override
    void openDataset() throws Exception {
        
        files = DatasetUtil.list(getParameter("input_path"), getParameter("file_filter"));
        if (!skipSorting()) 
            DatasetUtil.sort(files, strTime, timeArrow());
        ncIn = DatasetUtil.openFile(files.get(0), true);
        readTimeLength();
    }

    @Override
    void setOnFirstTime() throws Exception {

        double t0 = getSimulationManager().getTimeManager().get_tO();
        index = DatasetUtil.index(files, t0, timeArrow, strTime);
        ncIn = DatasetUtil.openFile(files.get(index), true);
        readTimeLength();
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
            ncIn.close();
            index = index = DatasetUtil.next(files, index, timeArrow);
            ncIn = DatasetUtil.openFile(files.get(index), true);
            nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
            rank = (1 - timeArrow) / 2 * (nbTimeRecords - 1);
        }
        setAllFieldsTp1AtTime(rank);

    }
}
