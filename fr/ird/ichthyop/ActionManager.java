/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

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
}
