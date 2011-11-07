/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.manager.ParameterManager;

/**
 *
 * @author pverley
 */
public class GrowingParticleLayer extends ParticleLayer {

///////////////////////////////
// Declaration of the constants
///////////////////////////////
    final public static double alfaK = 42922.0767d;
    final public static double betaK = 2.290236d;
    final public static double K2 = 6953.4d;
    final public static double K3 = 3562.5d;
    final public static double K4 = 6438.3d;
    final public static double K5 = 3476.7d;
    final public static double K6 = 8284d;
    final public static double K7 = 4678.5d;
    final public static double K8 = 4807.2d;
    final public static double K9 = 2704.1d;
    final public static double K10 = 2017.4d;
///////////////////////////////
// Declaration of the variables
///////////////////////////////
    private double ratioStage;
    private double temperature;
    /**
     * Particle length [millimeter]
     */
    double length;
    /**
     * Initial length [millimeter] for the particles.
     */
    private static double length_init;// = 0.025d; // mm
    /**
     * Threshold [millimiter] to distinguish eggs from larvae
     */
    private static double hatch_length;// = 2.8d; //mm
    /**
     * Threshold [millimeter] between Yolk-Sac Larvae and Feeding Larvae
     */
    private static double yolk_to_feeding_length;// = 4.5d; //mm
    private int step;

    public GrowingParticleLayer(IBasicParticle particle) {
        super(particle);
    }

    @Override
    public void init() {
        loadParameters();
        ratioStage = 1.d;
        length = length_init;
        step = -1;
    }

    private void loadParameters() {

        ParameterManager parameterManager = getSimulationManager().getParameterManager();
        length_init = Float.valueOf(parameterManager.getParameter("app.particle_length", "initial_length"));
        hatch_length = Float.valueOf(parameterManager.getParameter("app.particle_length", "hatch_length"));
        yolk_to_feeding_length = Float.valueOf(parameterManager.getParameter("app.particle_length", "yolk2feeding_length"));
    }

        public double getLength_init() {
        return length_init;
    }
        
        public double getHatch_length() {
        return hatch_length;
    }
                
        public double getYolk_to_feeding_length() {
        return yolk_to_feeding_length;
    }
     
    public int getEggStage() {
        return (int) Math.min(Math.floor(ratioStage), 10);
    }

    public void updateRatioStage(double temperature, double salinity, double waterDensity) {

        /*
         * First, ensure that the function has not already been called
         * during this time step.
         */
        int currentStep = getSimulationManager().getTimeManager().index();
        if (step == currentStep) {
            /*
             * It means updateRatioStage has already been called this time step
             * So we exit the function.
             */
            return;
        } else {
            /*
             * updateRatioStage has not been called yet. So we can continue.
             */
            step = currentStep;
        }

        this.temperature = temperature;

        int dt = getSimulationManager().getTimeManager().get_dt();
        double stageDuration = 0.d;

        int stage = getEggStage();

        if (stage == 1) {
            stageDuration = K2 * Math.pow(temperature, -betaK);

        } else if (stage == 2) // Stage 3
        {
            stageDuration = K3 * Math.pow(temperature, -betaK);

        } else if (stage == 3) // Stage 4
        {
            stageDuration = K4 * Math.pow(temperature, -betaK);

        } else if (stage == 4) // Stage 5
        {
            stageDuration = K5 * Math.pow(temperature, -betaK);

        } else if (stage == 5) // Stage 6
        {
            stageDuration = K6 * Math.pow(temperature, -betaK);

        } else if (stage == 6) // Stage 7
        {
            stageDuration = K7 * Math.pow(temperature, -betaK);

        } else if (stage == 7) // Stage 8
        {
            stageDuration = K8 * Math.pow(temperature, -betaK);

        } else if (stage == 8) // Stage 9
        {
            stageDuration = K9 * Math.pow(temperature, -betaK);

        } else if (stage == 9) // Stage 10
        {
            stageDuration = K10 * Math.pow(temperature, -betaK);

        }
        ratioStage = ratioStage + (dt / 3600.f) / stageDuration;

        //System.out.println("ratioStage Buoyant: " + (float)ratioStage);
        //System.out.println("Stage Buoyant:      " + (float)stage);
    }

    public double getRationStage() {
        return ratioStage;
    }

    /**
     * @return the temperature
     */
    public double getTemperature() {
        return temperature;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public Stage getStage() {
        /** Yolk-Sac Larvae */
        if (length >= hatch_length & length < yolk_to_feeding_length) {
            return Stage.YOLK_SAC_LARVA;
        } /** Feeding Larvae */
        else if (length >= yolk_to_feeding_length) {
            return Stage.FEEDING_LARVA;
        }
        /** eggs */
        return Stage.EGG;
    }

    public enum Stage {

        /**
         * Characterized the egg stage. In this model, an egg is particle with a
         * length smaller to the hatch-length defined below.
         */
        EGG(0),
        /**
         * Characterized the "yolk sac" stage. A yolk sac larva has a length ranging
         * from the hatch-length and the yolk-to-feeding-length defined below.
         */
        YOLK_SAC_LARVA(1),
        /**
         * Characterized the "feeding larva" stage. A feeding larva has a length
         * bigger than the yolk-to-feeding-length defined below.
         */
        FEEDING_LARVA(2);
        /* numerical code corresponding to the stage */
        private int code;

        Stage(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
