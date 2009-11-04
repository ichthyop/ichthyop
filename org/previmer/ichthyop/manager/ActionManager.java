/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.arch.IActionManager;
import org.previmer.ichthyop.event.SetupListener;
import org.previmer.ichthyop.io.XBlock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.arch.IAction;

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

    private void loadActions() {
        actionMap = new HashMap();
        Iterator<XBlock> it = getSimulationManager().getParameterManager().getBlocks(BlockType.ACTION).iterator();
        while (it.hasNext()) {
            XBlock xaction = it.next();
            if (xaction.isEnabled()) {
                try {
                    Class actionClass = Class.forName(xaction.getXParameter("class_name").getValue());
                    actionMap.put(xaction.getKey(), createAction(actionClass));
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ActionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public List<AbstractAction> getSortedActions() {
        List<AbstractAction> actions = new ArrayList(actionMap.values());
        Collections.sort(actions, new ActionComparator());
        return actions;
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
        for (AbstractAction action : getSortedActions()) {
            System.out.println(action.getClass() + " " + action.getPriority().toString());
        }
    }

    public boolean isEnabled(String actionKey) {
        return getSimulationManager().getParameterManager().isBlockEnabled(BlockType.ACTION, actionKey);
    }

    public String getParameter(String actionKey, String key) {
        return getSimulationManager().getParameterManager().getParameter(BlockType.ACTION, actionKey, key);
    }

    private class ActionComparator implements Comparator<IAction> {

        public int compare(IAction action1, IAction action2) {
            return action2.getPriority().rank().compareTo(action1.getPriority().rank());
        }
    }
}
