/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.action.AbstractAction;
import fr.ird.ichthyop.io.XAction;
import java.util.Collection;

/**
 *
 * @author pverley
 */
public interface IActionManager {

    public XAction getXAction(String key);

    public AbstractAction createAction(Class actionClass);

    public Collection<XAction> getActions();

}
