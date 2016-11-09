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
