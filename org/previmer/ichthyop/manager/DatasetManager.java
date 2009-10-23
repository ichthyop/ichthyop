/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.arch.IDataset;
import org.previmer.ichthyop.arch.IDatasetManager;
import org.previmer.ichthyop.io.XBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class DatasetManager extends AbstractManager implements IDatasetManager {

    final private static DatasetManager datasetManager = new DatasetManager();
    private IDataset dataset;

    public static DatasetManager getInstance() {
        return datasetManager;
    }

    public IDataset getDataset() {
        if (dataset == null) {
            try {
                XBlock datasetBlock = findActiveDataset();
                if (datasetBlock != null) {
                    dataset = (IDataset) Class.forName(datasetBlock.getXParameter("class_name").getValue()).newInstance();
                }
            } catch (InstantiationException ex) {
                Logger.getLogger(DatasetManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(DatasetManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DatasetManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return dataset;
    }

    public String getParameter(String datasetKey, String key) {
        return getSimulationManager().getParameterManager().getXParameter(BlockType.DATASET, datasetKey, key).getValue();
    }

    private XBlock findActiveDataset() {
        List<XBlock> list = new ArrayList();
        for (XBlock block : getSimulationManager().getParameterManager().getBlocks(BlockType.DATASET)) {
            if (block.isEnabled()) {
                list.add(block);
            }
        }
        if (list.size() > 0 && list.size() < 2) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public void setupPerformed(SetupEvent e) {
        getDataset().setUp();
    }

    public void initializePerformed(InitializeEvent e) {
        getDataset().init();
    }
}
