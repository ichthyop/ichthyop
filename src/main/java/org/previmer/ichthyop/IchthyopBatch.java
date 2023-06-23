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

package org.previmer.ichthyop;

import java.io.File;
import java.util.logging.Level;
import org.previmer.ichthyop.io.IOTools;

/**
 *
 * @author pverley
 */
public class IchthyopBatch extends SimulationManagerAccessor implements Runnable {

    /**
     * The path of the configuration file.
     */
    private final String filename;

    /**
     * Creates a new run in batch mode with the specified configuration file.
     *
     * @param filename, the path of the configuration file
     */
    public IchthyopBatch(String filename) {
        this.filename = filename;
    }

    /**
     * Runs the simulation.
     */
    @Override
    public void run() {

        try {
            File file = new File(IOTools.resolveFile(filename));
            getSimulationManager().setConfigurationFile(file);
            getLogger().log(Level.INFO, "Opened configuration file {0}", file.getPath());
            getLogger().info("===== Simulation started =====");
            getSimulationManager().resetId();
            getSimulationManager().resetTimerGlobal();
            /* */
            long startTime = System.currentTimeMillis();
            do {
                getLogger().log(Level.INFO, "++++ Run {0}", getSimulationManager().indexSimulationToString());
                /* setup */
                getLogger().info("Setting up...");
                getSimulationManager().setup();
                /* initialization */
                getLogger().info("Initializing...");
                getSimulationManager().init();
                /* first time step */
                getSimulationManager().getTimeManager().firstStepTriggered();
                getSimulationManager().resetTimerCurrent();
                do {
                    /* check whether the simulation has been interrupted by user */
                    if (getSimulationManager().isStopped()) {
                        break;
                    }
                    /* step simulation */
                    getSimulationManager().getSimulation().step();
                    progress(getSimulationManager().getTimeManager().index());
                } while (getSimulationManager().getTimeManager().hasNextStep());
                long endTime = System.currentTimeMillis();
                getLogger().log(Level.INFO, "Current run took {0} seconds.", ((endTime - startTime) / 1000L));
            } while (getSimulationManager().hasNextSimulation());
            getLogger().info("===== Simulation completed =====");
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "An error occured while running the simulation", ex);
        }
    }

    /**
     * Logs the progress of the simulation.
     */
    private void progress(int iStep) {

        int detail = 20;
        StringBuilder msg = new StringBuilder();
        msg.append(getSimulationManager().getTimeManager().stepToString());
        if (iStep % detail == 0) {
            msg.append(" (time ");
            msg.append(getSimulationManager().getTimeManager().timeToString());
            msg.append(")");
        }
        if ((iStep + (detail / 2)) % detail == 0) {
            msg.append(" (progress run ");
            msg.append(getSimulationManager().indexSimulationToString());
            msg.append(" ");
            msg.append(getSimulationManager().timeLeftGlobal());
            msg.append(")");
        }
        getLogger().info(msg.toString());
    }
}
