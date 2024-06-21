/*
 *
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full
 * description, see the LICENSE file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.previmer.ichthyop.action;

import java.util.Random;

import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.manager.ParameterManager;
import org.previmer.ichthyop.particle.IParticle;

/**
 *
 * @author pverley
 */
public abstract class AbstractAction extends SimulationManagerAccessor {

    private final String actionKey;
    private final Random randomGenerator;

    abstract public void loadParameters() throws Exception;

    abstract public void execute(IParticle particle);

    abstract public void init(IParticle particle);

    public AbstractAction() {
        actionKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
        boolean isFixedSeed = ParameterManager.getInstance().getConfigurationFile().isFixedSeed();
        if (isFixedSeed) {
            randomGenerator = new Random(getSimulationManager().getIndexSimulation());
        } else {
            randomGenerator = new Random();
        }
    }

    public double getRandomDraft() {
        return randomGenerator.nextDouble();
    }

    public String getBlockKey() {
        return actionKey;
    }

    public String getParameter(String key) {
        return getSimulationManager().getParameterManager().getParameter(BlockType.ACTION, actionKey, key);
    }

    /**
     * Check whether parameter 'key' has 'null' value. The function returns
     * {@code true} in several cases: the parameter does not exist, the value of
     * the parameter is empty or the value of the parameter is set to "null".
     *
     * @param key, the key of the parameter
     * @return {@code true} if the parameter is either null, empty or does not
     * exist
     */
    public boolean isNull(String key) {
        String value;
        try {
            value = getParameter(key).trim();
        } catch (Exception ex) {
            return true;
        }
        return (null == value) || value.isEmpty() ||  value.equalsIgnoreCase("null");
    }

    public String[] getListParameter(String key) {
        return getSimulationManager().getParameterManager().getListParameter(BlockType.ACTION, actionKey, key);
    }

    public ActionPriority getPriority() {
        String priority = getParameter("priority");
        for (ActionPriority actionPriority : ActionPriority.values()) {
            if (priority.equals(actionPriority.toString())) {
                return actionPriority;
            }
        }
        return ActionPriority.NORMAL;
    }

    public boolean isEnabled() {
        return getSimulationManager().getActionManager().isEnabled(actionKey);
    }

}
