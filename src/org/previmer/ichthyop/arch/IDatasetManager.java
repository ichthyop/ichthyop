/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

/**
 *
 * @author pverley
 */
public interface IDatasetManager {

    public IDataset getDataset();

    public String getParameter(String datasetKey, String key);

}
