/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ichthyop.core;

import ichthyop.bio.DVMPattern;
import ichthyop.io.Configuration;
import ichthyop.io.OutputNC;
import ichthyop.ui.MainFrame;
import ichthyop.ui.MainFrame.SetupSwingWorker;
import ichthyop.util.Constant;
import ichthyop.util.Resources;
import ichthyop.util.SafeSwingWorker;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import javax.swing.SwingUtilities;

/**
 *
 * @author pverley
 */
public class Runbatch {

    ///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * The {@code Simulation} object managed by the main frame
     */
    private Simulation simulation;
    /**
     * The configuration file
     */
    private static File cfgFile;
    /**
     * A flag that indicates whether or not the simulation is over
     * (came to end or interrupted by the user)
     */
    private boolean flagStop = false;
    public static int indexToStart;
    private Step step;

    public Runbatch(String path) {

        this(path, String.valueOf(0));

    }

    public Runbatch(String path, String strStart) {

        /** prints application title in the console */
        System.out.println();
        for (int i = 0; i < Resources.TITLE_LARGE.length(); i++) {
            System.out.print('%');
        }
        System.out.println();
        System.out.println(Resources.TITLE_LARGE);
        for (int i = 0; i < Resources.TITLE_LARGE.length(); i++) {
            System.out.print('%');
        }
        System.out.println();

        indexToStart = Integer.valueOf(strStart);
        try {
            cfgFile = new File(path);
            if (cfgFile.exists()) {
                setUp();
            } else {
                throw new IOException("Configuration file not found");
            }
            runSimulation();
            if (flagStop) {
                System.out.println("Simulation interrupted (by user)");
            } else {
                System.out.println("End of simulation");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception {

        if (args == null || args.length == 0) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    Toolkit.getDefaultToolkit().setDynamicLayout(true);
                    new MainFrame().setVisible(true);
                }
            });
        } else if (args.length < 2) {
            new Runbatch(args[0]);
        } else {
            new Runbatch(args[0], args[1]);
        }
    }

    /**
     * Sets up the main frame. It loads a configuration file and calls for the
     * setup SwingWorker.
     * @see inner class SetupSwingWorker
     */
    public void setUp() throws Exception {

        try {
            new Configuration(cfgFile);
        } catch (Exception e) {
            /** Print error message and stop the setup.*/
            printErr(e);
            cfgFile = null;
            return;
        }

        simulation = new Simulation();
        simulation.setUp();
    }

    private void runSimulation() throws Exception {

        step = new Step(1, 0);
        /** Starts SERIAL simulation */
        do {
            if (step.indexSimulation() >= indexToStart) {
                /** Initializes each new run */
                initSimulation();
                /** Starts the current run */
                do {
                    simulation.iniStep(step.getTime());
                    if (step.hasToRecord()) {
                        OutputNC.write(step.getTime());
                    }
                    publish(step.clone());
                    simulation.step(step.getTime());
                } while (!flagStop && step.next()); // loop for current run
                /** Writes last record and closes ouput file */
                OutputNC.write(step.getTime());
                OutputNC.close();
            }
        } while (!flagStop && step.nextSimulation());

        System.out.println();

    }

    private void initSimulation() throws Exception {

        System.out.println("\n############## New simulation  #############");
        System.out.println("Initializing");
        simulation.init();
        simulation.printParameters(step);
        OutputNC.create(step.indexSimulation(), step.getNumberOfSimulations(),
                Constant.SERIAL);
        OutputNC.init(simulation.getPopulation());

        if (Configuration.isMigration()) {
            DVMPattern.setCalendar((Calendar) step.getCalendar().clone());
        }
    }

    protected void publish(Step lstep) {

        int lengthStr = 0;
        StringBuffer strBuffer = new StringBuffer("");

        for (int i = 0; i < lengthStr + 10; i++) {
            strBuffer.append('.');
        }
        System.out.print("\r" + strBuffer.toString());

        strBuffer = new StringBuffer("");
        strBuffer.append("Simu ");
        strBuffer.append(lstep.indexSimulation() + 1);
        strBuffer.append(" / ");
        strBuffer.append(lstep.getNumberOfSimulations());
        strBuffer.append(" - Step ");
        strBuffer.append(lstep.index() + 1);
        strBuffer.append(" / ");
        strBuffer.append(lstep.getNumberOfSteps());
        strBuffer.append(" - Left ");
        strBuffer.append(lstep.timeLeft());
        System.out.print("\r" + strBuffer.toString());
        lengthStr = strBuffer.length();
    }

    /**
     * This method prints an error message in the console and shows an error
     * dialog box giving details about the exception.
     *
     * @param t a Throwable, the exception thrown.
     * @param errTitle a String, the title of the error dialog box.
     */
    private void printErr(Throwable t) {

        StackTraceElement[] stackTrace = t.getStackTrace();
        StringBuffer message = new StringBuffer(t.getClass().getSimpleName());
        message.append(" : ");
        message.append(stackTrace[0].toString());
        message.append('\n');
        message.append("  --> ");
        message.append(t.getMessage());
        System.err.println(message.toString());
    }

    public static int getIndexToStart() {
        return indexToStart;
    }
}
