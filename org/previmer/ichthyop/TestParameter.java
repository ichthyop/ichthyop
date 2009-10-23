/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop;

import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.XParameter;
import java.io.File;

/**
 *
 * @author pverley
 */
public class TestParameter extends SimulationManagerAccessor {

    File file;

    public TestParameter() {

        String filename = System.getProperty("user.dir") + File.separator + "cfg2.xic";
        file = new File(filename);

        getSimulationManager().setConfigurationFile(file);
        XParameter xparam = getSimulationManager().getParameterManager().getXParameter(BlockType.OPTION, "app.time", "time_step");
        //xparam = getSimulationManager().getParameterManager().getXParameter(BlockType.TRACKER, "tracker.time", "class_name");
        //xparam = getSimulationManager().getParameterManager().getXParameter(BlockType.DATASET, "dataset.roms_3d_ucla", "input_path");
        System.out.println(getSimulationManager().getDatasetManager().getParameter("dataset.roms_3d_ucla", "input_path"));
        System.out.println(xparam.getKey() + " " + xparam.getValue() + " " + xparam.isSerial());

    }



    public static void main(String[] arg) {
        new TestParameter();
    }
}
