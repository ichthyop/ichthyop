/*
 * IchthyopApp.java
 */
package org.previmer.ichthyop.ui;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JFrame;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.previmer.ichthyop.IchthyopBatch;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.manager.SimulationManager;
import org.previmer.ichthyop.ui.logging.SystemOutHandler;

/**
 * The main class of the application.
 */
public class IchthyopApp extends SingleFrameApplication {

    private boolean shouldRestorePreferences = true;

    /**
     * At startup create and show the main frame of the application.
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
     * @return the instance of IchthyopApp
     */
    public static IchthyopApp getApplication() {
        return Application.getInstance(IchthyopApp.class);
    }

    public static IchthyopView getIchthyopView() {
        return (IchthyopView) getApplication().getMainView();
    }

    private static void initLogging() {

        /* Create a FileHandler (logs will be recorded in a file */
        try {
            String logPath = System.getProperty("user.dir") + File.separator + "ichthyop-log.txt";
            IOTools.makeDirectories(logPath.toString());
            FileHandler fh = new FileHandler(logPath.toString());
            getLogger().addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            getLogger().info("Created log file " + logPath);
        } catch (IOException ex) {
        } catch (SecurityException ex) {
        }

        /* Connect to the java console */
        getLogger().addHandler(new SystemOutHandler());
    }

    private static Logger getLogger() {
        return SimulationManager.getLogger();
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        initLogging();
        if (args.length > 0) {
            new Thread(new IchthyopBatch(args[0])).start();
        } else {
            launch(IchthyopApp.class, args);
        }
    }
}
