/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.io.XBlock;
import java.util.Collection;

/**
 *
 * @author pverley
 */
public interface IActionManager {

    //public XBlock getXAction(String key);

    public AbstractAction createAction(Class actionClass);

    public void execute(String actionName, IBasicParticle particle);

    public AbstractAction get(Object key);

    public boolean isEnabled(String actionName);

    public String getParameter(String actionName, String key);

}
