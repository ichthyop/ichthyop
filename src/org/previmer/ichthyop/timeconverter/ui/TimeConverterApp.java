/*
 * TimeConverterApp.java
 */
package org.previmer.ichthyop.timeconverter.ui;

import java.io.IOException;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.previmer.ichthyop.util.TimeConverter;

/**
 * The main class of the application.
 */
public class TimeConverterApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        show(new TimeConverterView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of TimeConverterApp
     */
    public static TimeConverterApp getApplication() {
        return Application.getInstance(TimeConverterApp.class);
    }

    private static void convertNoUI(String[] args) {

        try {

            if (args[0].split("/").length > 1) {
                TimeConverter.date2time(args[0] + " " + args[1]);
            } else {

                String typeCalendar = args[0];
                if (!typeCalendar.startsWith("-")) {
                    throw new IOException("Option must be preceded by '-' characters");
                }
                typeCalendar = typeCalendar.substring(1);
                if (!typeCalendar.matches("[g c]")) {
                    throw new IOException("Accepted options: ( g | c )");
                }

                try {
                    long time = Long.parseLong(args[1]);
                    TimeConverter.time2date(time, typeCalendar.matches("g"));
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Time value must be a Long.");
                }

            }

        } catch (Exception e) {
            TimeConverter.error(e);
        }
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            convertNoUI(args);
        } else {
            launch(TimeConverterApp.class, args);
        }

    }
}
