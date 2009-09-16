/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public class ActionManager {

    public static XAction getAction(String key) {
        return ICFile.getInstance().getAction(key);
    }

}
