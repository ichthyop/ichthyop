/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.TypeBlock;
import fr.ird.ichthyop.arch.IDataset;
import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.io.XBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class DatasetManager {

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
        return ICFile.getInstance().getBlock(TypeBlock.DATASET, key);
    }

    public String getParameter(String key) {
        return datasetBlock.getParameter(key).getValue();
    }

    private XBlock findActiveDataset() {
        List<XBlock> list = ICFile.getInstance().getBlocks(TypeBlock.DATASET);
        if (list.size() > 0 && list.size() < 2) {
            return list.get(0);
        } else {
            return null;
        }
    }
}
