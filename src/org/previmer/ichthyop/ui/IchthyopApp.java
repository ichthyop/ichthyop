/*
 * IchthyopApp.java
 */
package org.previmer.ichthyop.ui;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.previmer.ichthyop.IchthyopBatch;
import static org.previmer.ichthyop.SimulationManagerAccessor.getSimulationManager;

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
            // The configuration file is provided, Ichthyop goes into batch mode
            new Thread(new IchthyopBatch(args[0])).start();
        } else {
            // No argument, open the GUI
            launch(IchthyopApp.class, args);
        }
    }
}
