/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public interface IActionManager {

    public XAction getAction(String key);

    public AbstractAction getAction(Class actionClass);

}
