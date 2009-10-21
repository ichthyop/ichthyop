/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.arch.IBasicParticle;
import fr.ird.ichthyop.event.InitializeEvent;
import fr.ird.ichthyop.event.SetupEvent;
import fr.ird.ichthyop.io.BlockType;
import fr.ird.ichthyop.action.AbstractAction;
import fr.ird.ichthyop.arch.IActionManager;
import fr.ird.ichthyop.event.SetupListener;
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
public class ActionManager extends AbstractManager implements IActionManager, SetupListener {

    final private static ActionManager actionManager = new ActionManager();
    private HashMap<String, AbstractAction> actionMap;

    public static ActionManager getInstance() {
        return actionManager;
    }

    public void setUp() {
        loadActions();
    }

    private void loadActions() {
        actionMap = new HashMap();
        Iterator<XBlock> it = getXActions().iterator();
        while (it.hasNext()) {
            XBlock xaction = it.next();
            if (xaction.isEnabled()) {
                try {
                    Class actionClass = Class.forName(xaction.getParameter("class_name").getValue());
                    actionMap.put(xaction.getKey(), createAction(actionClass));
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ActionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public XBlock getXAction(String key) {
        return getSimulationManager().getParameterManager().getBlock(BlockType.ACTION, key);
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
        for (XBlock block : getSimulationManager().getParameterManager().getBlocks(BlockType.ACTION)) {
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

    public AbstractAction get(Object key) {
        return actionMap.get((String) key);
    }

    public void setupPerformed(SetupEvent e) {
        loadActions();
    }

    public void initializePerformed(InitializeEvent e) {
        // do nothing
    }
}
