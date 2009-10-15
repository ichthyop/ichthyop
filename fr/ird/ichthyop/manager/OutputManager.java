/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.event.NextStepEvent;
import fr.ird.ichthyop.Simulation;
import fr.ird.ichthyop.io.TypeBlock;
import fr.ird.ichthyop.arch.IOutputManager;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.IStep;
import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.io.OutputNC;
import fr.ird.ichthyop.io.XBlock;
import fr.ird.ichthyop.util.Constant;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class OutputManager implements IOutputManager {

    final private static OutputManager outputManager = new OutputManager();
    private final static String block_key = "app.output";
    private OutputNC output;
    private int dt_record;

    public static OutputManager getInstance() {
        return outputManager;
    }

    public String getParameter(String key) {
        return getSimulation().getParameterManager().getValue(block_key, key);
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }

    public void setUp() {
        if (ICFile.getInstance().getBlock(TypeBlock.OPTION, block_key).isEnabled()) {
            getSimulation().getStep().addNextStepListener(this);
            int record_frequency = Integer.valueOf(getParameter("record_frequency"));
            dt_record = record_frequency * getSimulation().getStep().get_dt();
            output = new OutputNC();
            try {
                output.create(0, 1, Constant.SINGLE);
            } catch (IOException ex) {
                Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void nextStepTriggered(NextStepEvent e) {

        IStep step = e.getSource();
        if (((step.getTime() - step.get_tO()) % dt_record) == 0) {
            output.write(step.getTime());
        }
    }

    public XBlock getXTracker(String key) {
        return ICFile.getInstance().getBlock(TypeBlock.TRACKER, key);
    }
}
