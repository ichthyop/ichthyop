/*
 * IchthyopApp.java
 */
package org.previmer.ichthyop.ui;

import javax.swing.JFrame;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.previmer.ichthyop.IchthyopBatch;

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
        getMainFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
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
        super.shutdown();
        ((IchthyopView) getMainView()).savePreferences();
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

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            new IchthyopBatch(args[0]);
        } else {
            launch(IchthyopApp.class, args);
        }
    }
}
