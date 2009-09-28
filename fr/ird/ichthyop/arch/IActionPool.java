/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.action.AbstractAction;

/**
 *
 * @author pverley
 */
public interface IActionPool {

    public void execute(String actionName, IBasicParticle particle);

    public AbstractAction get(Object key);

}
