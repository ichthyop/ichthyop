/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.action.AbstractAction;
import fr.ird.ichthyop.io.XBlock;
import java.util.Collection;

/**
 *
 * @author pverley
 */
public interface IActionManager {

    public XBlock getXAction(String key);

    public AbstractAction createAction(Class actionClass);

    public Collection<XBlock> getXActions();

    public void execute(String actionName, IBasicParticle particle);

    public AbstractAction get(Object key);

    public void setUp();

}
