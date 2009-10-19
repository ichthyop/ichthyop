/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.arch.IBasicParticle;
import fr.ird.ichthyop.io.TypeBlock;
import fr.ird.ichthyop.action.AbstractAction;
import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.arch.IActionManager;
import fr.ird.ichthyop.io.XBlock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class ActionManager extends HashMap<String, AbstractAction> implements IActionManager {

    final private static ActionManager actionManager = new ActionManager();

    public static ActionManager getInstance() {
        return actionManager;
    }

    public void setUp() {
        loadActions();
    }

    private void loadActions() {
        Iterator<XBlock> it = getXActions().iterator();
        while (it.hasNext()) {
            XBlock xaction = it.next();
            if (xaction.isEnabled()) {
                try {
                    Class actionClass = Class.forName(xaction.getParameter(ICFile.CLASS_NAME).getValue());
                    put(xaction.getKey(), createAction(actionClass));
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ActionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public XBlock getXAction(String key) {
        return ICFile.getInstance().getBlock(TypeBlock.ACTION, key);
    }

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

    public Collection<XBlock> getXActions() {
        Collection<XBlock> collection = new ArrayList();
        for (XBlock block : ICFile.getInstance().getBlocks(TypeBlock.ACTION)) {
            collection.add(block);

        }
        return collection;
    }

    public void execute(String actionName, IBasicParticle particle) {
        AbstractAction action = get(actionName);
        if (action != null) {
            action.execute(particle);
        }
    }
}
