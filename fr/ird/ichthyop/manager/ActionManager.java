/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.io.TypeBlock;
import fr.ird.ichthyop.action.AbstractAction;
import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.io.XAction;
import fr.ird.ichthyop.arch.IActionManager;
import fr.ird.ichthyop.io.XBlock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class ActionManager implements IActionManager {

    final private static ActionManager actionManager = new ActionManager();

    public static ActionManager getInstance() {
        return actionManager;
    }

    public XAction getXAction(String key) {
        //return (XAction) ICFile.getInstance().getBlock(TypeBlock.ACTION, key);
        return new XAction(ICFile.getInstance().getBlock(TypeBlock.ACTION, key));
    }

    /*public AbstractAction getAction(Class actionClass) {
    try {
    return (AbstractAction) actionClass.newInstance();
    } catch (InstantiationException ex) {
    Logger.getLogger(ActionManager.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
    Logger.getLogger(ActionManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
    }*/
    public AbstractAction createAction(Class actionClass) {

        try {
            return (AbstractAction) actionClass.newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(ActionManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ActionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public Collection<XAction> getActions() {
        Collection<XAction> collection = new ArrayList();
        for (XBlock block : ICFile.getInstance().getBlocks(TypeBlock.ACTION)) {
            collection.add(new XAction(block));

        }
        return collection;
    }
}
