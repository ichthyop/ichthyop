/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

import org.previmer.ichthyop.event.NextStepListener;
import org.previmer.ichthyop.manager.OutputManager.NCDimFactory;

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