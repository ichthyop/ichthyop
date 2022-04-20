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

package org.previmer.ichthyop.ui;

import java.util.logging.Level;
import ml.options.OptionSet;
import ml.options.Options;
import ml.options.Options.Multiplicity;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.previmer.ichthyop.IchthyopBatch;
import static org.previmer.ichthyop.SimulationManagerAccessor.getSimulationManager;
import org.previmer.ichthyop.manager.SimulationManager;

/**
 * The main class of the application.
 */
public class IchthyopApp extends SingleFrameApplication {

    private boolean shouldRestorePreferences = true;

    /**
     * At startup creates and shows the main frame of the application.
     */
    @Override
    protected void startup() {
        show(new IchthyopView(this));
        getMainFrame().setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     *
     * @param root
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
        if (shouldRestorePreferences) {
            ((IchthyopView) getMainView()).restorePreferences();
            shouldRestorePreferences = false;
        }
    }

    @Override
    protected void shutdown() {
        ((IchthyopView) getMainView()).savePreferences();
        super.shutdown();
    }

    /**
     * A convenient static getter for the application instance.
     *
     * @return the instance of IchthyopApp
     */
    public static IchthyopApp getApplication() {
        return Application.getInstance(IchthyopApp.class);
    }

    public static IchthyopView getIchthyopView() {
        return (IchthyopView) getApplication().getMainView();
    }

    /**
     * Main method launching the application.
     *
     * @param args, list of input arguments. It only takes one argument, the
     * path of the configuration file
     */
    public static void main(String[] args) {

        // Initialize the logger
        getSimulationManager().setupLogger();

        // Check for input arguments
        if (args.length > 0) {
            // Sets of command line options
            Options opt = new Options(args);
            // Default set: Ichthyop configuration file is given as argument
            opt.addSet("Usage", 1, 1);
            // For all sets, enable verbose output
            opt.addOptionAllSets("verbose", Multiplicity.ZERO_OR_ONE);
            // For all sets, enable quiet output (only error)
            opt.addOptionAllSets("quiet", Multiplicity.ZERO_OR_ONE);

            // Get the matching set and throw error if none found
            OptionSet set = opt.getMatchingSet(false, false);
            if (set == null) {
                SimulationManager.getLogger().log(Level.SEVERE, "Invalid command line usage.", new IllegalArgumentException(opt.getCheckErrors()));
            } else {
                if (set.isSet("verbose") && set.isSet("quiet")) {
                    SimulationManager.getLogger().log(Level.SEVERE, "Invalid command usage, -verbose and -quiet options are exclusive", new IllegalArgumentException("Osmose logging cannot be both verbose and quiet."));
                }
                if (set.isSet("verbose")) {
                    SimulationManager.getLogger().setLevel(Level.FINE);
                }
                if (set.isSet("quiet")) {
                    SimulationManager.getLogger().setLevel(Level.SEVERE);
                }
                // The configuration file is provided, Ichthyop goes into batch mode
                new Thread(new IchthyopBatch(set.getData().get(0))).start();
            }
        } else {
            // No argument, open the GUI
            launch(IchthyopApp.class, null);
        }
    }
}
