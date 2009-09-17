/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class ActionManager implements IActionManager {

    private static ActionManager actionManager = new ActionManager();

    public static ActionManager getInstance() {
        return actionManager;
    }

    public XAction getAction(String key) {
        return ICFile.getInstance().getAction(key);
    }

    public AbstractAction getAction(Class actionClass) {
        try {
            return (AbstractAction) actionClass.newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(ActionManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ActionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
