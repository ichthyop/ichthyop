/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ichthyop.core;

import ichthyop.bio.DVMPattern;
import ichthyop.io.Configuration;
import ichthyop.io.OutputNC;
import ichthyop.ui.MainFrame.SetupSwingWorker;
import ichthyop.util.Constant;
import ichthyop.util.Resources;
import ichthyop.util.SafeSwingWorker;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 *
 * @author pverley
 */
public class RunBatch {

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

    public RunBatch(String path) {

        try {
            cfgFile = new File(path);
            if (cfgFile.exists()) {
                setUp();
            } else {
                throw new IOException("Configuration file not found");
            }
            new SingleSwingWorker().execute();
        } catch (Exception e) {
            e.printStackTrace();
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
    
    private class SingleSwingWorker extends SafeSwingWorker<Object, Step> {

        /**
         * Date and time of the current step
         */
        String strTime;
        /**
         * Refresh time step [second]
         */
        private int dt_refresh;
        /**
         * Index of the current step
         */
        private int i_step;
        /**
         * Total number of steps of the run
         */
        private int nb_steps;
        /**
         * The {@code Step} object holding information about the current
         * step of the run.
         */
        private Step step;

        /**
         * This method is the backbone of the SINGLE mode. It controls the
         * march of the simulation through time, thanks to the {@code Step}
         * object. It calls the <code>Simulation.step</code> methods every
         * time step, controls the ouput writer and the chart manager.
         * @throws any Exception that occurs while running the simulation.
         * The method sends a copy of the {@code Step} object to the
         * {@link #process} method every time the display has to be refresh.
         *
         * @see ichthyop.util.SafeSwingWorker for details about the SwingWorkers.
         */
        @Override
        protected Object doInBackground() throws Exception {

            /** Initializes */
            init();

            /** Starts the run */
            do {
                simulation.iniStep(step.getTime());
                if (Configuration.isRecordNc() && step.hasToRecord()) {
                    OutputNC.write(step.getTime());
                }
                publish(step.clone());
                simulation.step(step.getTime());
            } while (!flagStop && step.next());

            /** writes last record on file and closes */
            if (Configuration.isRecordNc()) {
                OutputNC.write(step.getTime());
                OutputNC.close();
            }
            return null;
        }

        /**
         * * Invoked only when the simulation ends or when interrupted by the
         * user. It creates a new {@link ReplayPanel} for a fast replay of
         * the run.
         */
        protected void onSuccess(Object result) {
            if (flagStop) {
                System.out.println("Simulation interrupted (by user)");
            } else {
                System.out.println("End of simulation");
                flagStop = true;
            }
        }

        /**
         * Invoked if an error occured while running the simulation.
         * It prints information about the exception and attempts to create a
         * new {@link ReplayPanel} to allow the user to replay the part of the
         * run that is antecedent to the error.
         *
         * @param t Throwable
         */
        protected void onFailure(Throwable t) {
            printErr(t, "Error while running the simulation");
            System.out.println("Simulation interrupted (error)");
            flagStop = true;
        }

        /**
         * Receives intermediate results from the {@code publish} method
         * asynchronously on the <i>Event Dispatch Thread</i>. The method
         * refreshed the display of the simulation thanks to the information
         * held by the {@code Step}s objects.
         * @param steps List of the {@code Step} objects sent by the
         * {@link #doInBackground} method through the {@code publish} method.
         * @see ichthyop.core.Step for details about the information contained
         * in a {@code Step}.
         */
        @Override
        protected void process(java.util.List<Step> steps) {

            //Updates the UI
            for (Step step : steps) {
                strTime = step.timeToString();
                if (!flagStop && step.hasToRefresh()) {
                    i_step++;
                    System.out.println(Resources.LBL_STEP + i_step + " / " +
                            nb_steps + " - " + Resources.LBL_TIME + strTime);
                }
            }
        }

        /**
         * Initializes the simulation and the UI before starting the run.
         * @throws any Exception that occurs while initializing the simulation.
         */
        private void init() throws Exception {

            System.out.println("Initializing");
            simulation.init();
            i_step = 0;
            dt_refresh = Configuration.get_dt();
            step = new Step(Configuration.getTimeArrow(), dt_refresh);
            nb_steps = (int) (step.getSimulationDuration() / dt_refresh);
            step.setData(simulation.getPopulation());
            if (Configuration.isRecordNc()) {
                OutputNC.create(0, 1, Constant.SINGLE);
                OutputNC.init(simulation.getPopulation());
            }

            if (Configuration.isMigration()) {
                DVMPattern.setCalendar((Calendar) step.getCalendar().clone());
            }
        }
        //----------- End of inner class SingleSwingWorker
    }
}
