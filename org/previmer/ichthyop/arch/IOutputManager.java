/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.event.NextStepListener;
import fr.ird.ichthyop.manager.OutputManager.NCDimFactory;

/**
 *
 * @author pverley
 */
public interface IOutputManager extends NextStepListener {

    public String getParameter(String key);

    public void setUp();

    public NCDimFactory getDimensionFactory();

    public boolean isTrackerEnabled(String trackerKey);

    public void init();

}
