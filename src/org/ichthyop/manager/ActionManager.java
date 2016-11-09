/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package org.ichthyop.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.ichthyop.action.AbstractAction;
import org.ichthyop.action.AbstractSysAction;
import org.ichthyop.action.SysActionAgeMonitoring;
import org.ichthyop.action.SysActionMove;
import org.ichthyop.event.InitializeEvent;
import org.ichthyop.event.SetupEvent;
import org.ichthyop.io.BlockType;
import org.ichthyop.io.XBlock;
import org.ichthyop.particle.Particle;

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

    public void executeActions(Particle particle) {
        // Pre-defined actions
        for (AbstractAction action : getSortedActions()) {
            if (!particle.isLocked()) {
                action.execute(particle);
            }
        }
        
        // System actions
        for (AbstractSysAction sysaction : sysActionList) {
            sysaction.execute(particle);
        }
    }
    
    public void initActions(Particle particle) {
        // Pre-defined actions
        for (AbstractAction action : getSortedActions()) {
                action.init(particle);
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

    private class ActionComparator implements Comparator<AbstractAction> {

        @Override
        public int compare(AbstractAction action1, AbstractAction action2) {
            return action2.getPriority().rank().compareTo(action1.getPriority().rank());
        }
    }
}
