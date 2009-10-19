/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.event.NextStepListener;
import fr.ird.ichthyop.io.XBlock;
import fr.ird.ichthyop.manager.OutputManager.NCDimFactory;

/**
 *
 * @author pverley
 */
public interface IOutputManager extends ISimulationAccessor, NextStepListener {

    public String getParameter(String key);

    public XBlock getXTracker(String key);

    public void setUp();

    public NCDimFactory getDimensionFactory();

}
