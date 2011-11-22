/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import java.io.IOException;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.arch.IDataset;
import org.previmer.ichthyop.io.XBlock;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pverley
 */
public class DatasetManager extends AbstractManager {

    final private static DatasetManager datasetManager = new DatasetManager();
    private IDataset dataset;

    public static DatasetManager getInstance() {
        return datasetManager;
    }

    private void instantiateDataset() throws Exception {

        XBlock datasetBlock = findActiveDataset();
        String className = getParameter(datasetBlock.getKey(), "class_name");
        if (datasetBlock != null) {
            try {
            dataset = (IDataset) Class.forName(className).newInstance();
            } catch (Exception ex) {
                StringBuffer sb = new StringBuffer();
                sb.append("Dataset instantiation failed ==> ");
                sb.append(ex.toString());
                InstantiationException ieex = new InstantiationException(sb.toString());
                ieex.setStackTrace(ex.getStackTrace());
                throw ieex;
            }
        }
    }

    public IDataset getDataset() {
        return dataset;
    }

    public String getParameter(String datasetKey, String key) {
        return getSimulationManager().getParameterManager().getParameter(BlockType.DATASET, datasetKey, key);
    }

    private XBlock findActiveDataset() throws Exception {
        List<XBlock> list = new ArrayList();
        for (XBlock block : getSimulationManager().getParameterManager().getBlocks(BlockType.DATASET)) {
            if (block.isEnabled()) {
                list.add(block);
            }
        }
        if (list.isEmpty()) {
            throw new NullPointerException("Could not find any " + BlockType.DATASET.toString() + " block in the configuration file.");
        }
        if (list.size() > 1) {
            throw new IOException("Found several " + BlockType.DATASET.toString() + " blocks enabled in the configuration file. Please only keep one enabled.");
        }
        return list.get(0);
    }

    public void setupPerformed(SetupEvent e) throws Exception {
        instantiateDataset();
        getDataset().setUp();
    }

    public void initializePerformed(InitializeEvent e) throws Exception {
        getSimulationManager().getTimeManager().addNextStepListener(getDataset());
        getDataset().init();
    }
}
