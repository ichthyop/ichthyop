/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.event.NextStepListener;

/**
 *
 * @author pverley
 */
public interface IOutputManager extends ISimulationAccessor, NextStepListener {

    public String getParameter(String key);

    public void setUp();

}
