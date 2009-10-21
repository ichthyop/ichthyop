/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.event.InitializeEvent;
import fr.ird.ichthyop.event.SetupEvent;
import fr.ird.ichthyop.io.BlockType;
import fr.ird.ichthyop.arch.IDataset;
import fr.ird.ichthyop.arch.IDatasetManager;
import fr.ird.ichthyop.io.XBlock;
import fr.ird.ichthyop.io.XParameter;
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
    private XBlock datasetBlock;

    public static DatasetManager getInstance() {
        return datasetManager;
    }

    public IDataset getDataset() {
        if (dataset == null) {
            try {
                datasetBlock = findActiveDataset();
                if (datasetBlock != null) {
                    dataset = (IDataset) Class.forName(datasetBlock.getParameter("class_name").getValue()).newInstance();
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

    public XBlock getXDataset(String key) {
        return getSimulationManager().getParameterManager().getBlock(BlockType.DATASET, key);
    }

    public String getParameter(String key) {
        XParameter xparam = datasetBlock.getParameter(key);
        if (null != xparam) {
            return datasetBlock.getParameter(key).getValue();
        } else {
            return null;
        }
    }

    private XBlock findActiveDataset() {
        List<XBlock> list = getSimulationManager().getParameterManager().getBlocks(BlockType.DATASET);
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
