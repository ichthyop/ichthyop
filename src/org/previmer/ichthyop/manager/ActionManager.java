/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.action.AbstractSysAction;
import org.previmer.ichthyop.action.SysActionAgeMonitoring;
import org.previmer.ichthyop.action.SysActionMove;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.particle.Particle;

/**
 *
 * @author pverley
 */
public class ActionManager extends AbstractManager {

    final private static ActionManager actionManager = new ActionManager();
    private HashMap<String, AbstractAction> actionMap;
    private List<AbstractSysAction> sysActionList;

    public static ActionManager getInstance() {
        return actionManager;
    }

    private void loadActions() throws InstantiationException {
        actionMap = new HashMap();
        Iterator<XBlock> it = getSimulationManager().getParameterManager().getBlocks(BlockType.ACTION).iterator();
        while (it.hasNext()) {
            XBlock xaction = it.next();
            if (xaction.isEnabled()) {
                try {
                    Class actionClass = Class.forName(xaction.getXParameter("class_name").getValue());
                    AbstractAction action = createAction(actionClass);
                    action.loadParameters();
                    actionMap.put(xaction.getKey(), action);
                    getLogger().log(Level.INFO, "Instantiated action \"{0}\"", xaction.getTreePath());
                } catch (Exception ex) {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Failed to setup action ");
                    msg.append(xaction.getTreePath());
                    msg.append("\n");
                    msg.append(ex.toString());
                    InstantiationException iex = new InstantiationException(msg.toString());
                    iex.setStackTrace(ex.getStackTrace());
                    throw iex;
                }
            }
        }
    }

    private void implementSysActions() throws InstantiationException {

        sysActionList = new ArrayList();

        sysActionList.add(new SysActionAgeMonitoring());
        sysActionList.add(new SysActionMove());

        for (AbstractSysAction sysaction : sysActionList) {
            try {
                sysaction.loadParameters();
                getLogger().log(Level.INFO, "Instantiated system action \"{0}\"", sysaction.getClass().getCanonicalName());
            } catch (Exception ex) {
                StringBuilder msg = new StringBuilder();
                msg.append("Failed to setup system action ");
                msg.append(sysaction.getClass().getCanonicalName());
                msg.append("\n");
                msg.append(ex.toString());
                InstantiationException iex = new InstantiationException(msg.toString());
                iex.setStackTrace(ex.getStackTrace());
                throw iex;
            }
        }
    }

    private List<AbstractAction> getSortedActions() {
        List<AbstractAction> actions = new ArrayList(actionMap.values());
        Collections.sort(actions, new ActionComparator());
        return actions;
    }

    public AbstractAction createAction(Class actionClass) throws InstantiationException, IllegalAccessException {
        return (AbstractAction) actionClass.newInstance();
    }

    public void executeActions(IParticle particle) {
        for (AbstractAction action : getSortedActions()) {
            if (!particle.isLocked()) {
                action.execute(particle);
            }
        }
    }

    public void executeSysActions(Particle particle) {
        for (AbstractSysAction sysaction : sysActionList) {
            sysaction.execute(particle);
        }
    }

    @Override
    public void setupPerformed(SetupEvent e) throws Exception {
        loadActions();
        implementSysActions();
        getLogger().info("Action manager setup [OK]");
    }

    @Override
    public void initializePerformed(InitializeEvent e) {
        // nothing to do
    }

    public boolean isEnabled(String actionKey) {
        return getSimulationManager().getParameterManager().isBlockEnabled(BlockType.ACTION, actionKey);
    }

    public String getParameter(String actionKey, String key) {
        return getSimulationManager().getParameterManager().getParameter(BlockType.ACTION, actionKey, key);
    }

    private class ActionComparator implements Comparator<AbstractAction> {

        @Override
        public int compare(AbstractAction action1, AbstractAction action2) {
            return action2.getPriority().rank().compareTo(action1.getPriority().rank());
        }
    }
}
