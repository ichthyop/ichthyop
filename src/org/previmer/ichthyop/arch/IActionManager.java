/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

import org.previmer.ichthyop.action.AbstractAction;

/**
 *
 * @author pverley
 */
public interface IActionManager {

    public AbstractAction createAction(Class actionClass) throws InstantiationException, IllegalAccessException;

    public void executeActions(IBasicParticle particle);

    public void executeSysActions(IMasterParticle particle);

    public boolean isEnabled(String actionName);

    public String getParameter(String actionName, String key);

}
