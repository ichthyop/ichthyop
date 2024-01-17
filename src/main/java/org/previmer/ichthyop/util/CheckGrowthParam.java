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

package org.previmer.ichthyop.util;

//import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.SimulationManagerAccessor;

/**
 *
 * @author Nicolas
 */
public class CheckGrowthParam extends SimulationManagerAccessor {

    public static boolean checkParams() {
        boolean isGrowth = getSimulationManager().getActionManager().isEnabled("action.growth");
        boolean isDebGrowth;
        try {
            isDebGrowth = getSimulationManager().getActionManager().isEnabled("action.growthdeb");
        } catch (NullPointerException e) {
            isDebGrowth = false;
        }
        if (isGrowth || isDebGrowth) { // if one or two growth action are on
            boolean paramOk = (isGrowth ^ isDebGrowth);  // check that only one of the two is true using xor operator
            if (!paramOk) {
                throw new IllegalArgumentException("Only one among action.growth and action.growthDeb should be ");
            }
        }
        // Redefines isGrowth, taking into account the possibility to set it using deb.
        isGrowth = (isGrowth || isDebGrowth);
        return (isGrowth);
    }

}
