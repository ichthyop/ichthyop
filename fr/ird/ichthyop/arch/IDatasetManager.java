/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.io.XBlock;

/**
 *
 * @author pverley
 */
public interface IDatasetManager {

    public IDataset getDataset();

    public XBlock getXDataset(String key);

    public String getParameter(String key);

}
