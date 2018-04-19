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
import java.util.List;
import org.ichthyop.action.AbstractAction;
import org.ichthyop.action.ActionPriority;
import org.ichthyop.action.SysActionAgeMonitoring;
import org.ichthyop.action.SysActionMove;
import org.ichthyop.event.InitializeEvent;
import org.ichthyop.event.SetupEvent;
import org.ichthyop.particle.Particle;

/**
 *
 * @author pverley
 */
public class ActionManager extends AbstractManager {

    final private static ActionManager ACTION_MANAGER = new ActionManager();
    private List<AbstractAction> actions;

    public static ActionManager getInstance() {
        return ACTION_MANAGER;
    }

    private void loadActions() throws InstantiationException {

        actions = new ArrayList();

        // instantiate user defined action
        getConfiguration().getParameterSubsets().stream().filter((key)
                -> (!getConfiguration().isNull(key + ".type")
                && getConfiguration().getString(key + ".type").equalsIgnoreCase("action")
                && getConfiguration().getBoolean(key + ".enabled")))
                .forEach((key) -> {
                    String className = getConfiguration().getString(key + ".class");
                    try {
                        Class actionClass = Class.forName(className);
                        AbstractAction action = (AbstractAction) actionClass.newInstance();
                        actions.add(action);
                        info("[processes] Instantiated \"{0}\" ({1})", new Object[]{key, className});
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                        StringBuilder msg = new StringBuilder();
                        msg.append("[processes] Failed to instantiate \"");
                        msg.append(key);
                        msg.append("\" (");
                        msg.append(className);
                        msg.append(")");
                        error(msg.toString(), ex);
                    }
                });

        // sort actions by priority
        Collections.sort(actions, new ActionComparator());

        // add internal actions
        actions.add(new SysActionAgeMonitoring());
        actions.add(new SysActionMove());
    }

    public void executeActions(Particle particle) {

        if (!particle.isLocked()) {
            actions.forEach((action) -> {
                action.execute(particle);
            });
        }
    }

    public void initActions(Particle particle) {

        actions.forEach((action) -> {
            action.init(particle);
        });
    }

    @Override
    public void setupPerformed(SetupEvent e) throws Exception {

        loadActions();
        for (AbstractAction action : actions) {
            action.loadParameters();
        }
        info("[processes] Setup [OK]");
    }

    @Override
    public void initializePerformed(InitializeEvent e) {
        // nothing to do
    }

    private ActionPriority getPriority(String actionKey) {

        if (!getConfiguration().isNull(actionKey + ".priority")) {
            String priority = getConfiguration().getString(actionKey + ".priority");
            for (ActionPriority actionPriority : ActionPriority.values()) {
                if (priority.equals(actionPriority.toString())) {
                    return actionPriority;
                }
            }
        }
        return ActionPriority.NORMAL;
    }

    private class ActionComparator implements Comparator<AbstractAction> {

        @Override
        public int compare(AbstractAction action1, AbstractAction action2) {
            return getPriority(action2.getKey()).rank().compareTo(getPriority(action1.getKey()).rank());
        }
    }
}
