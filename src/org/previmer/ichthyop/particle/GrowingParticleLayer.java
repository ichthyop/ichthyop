/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.manager.ParameterManager;

/**
 *
 * @author pverley
 */
public class GrowingParticleLayer extends ParticleLayer {

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

    public GrowingParticleLayer(IParticle particle) {
        super(particle);
    }

    public void init() {
        loadParameters();
        length = length_init;
    }

    private void loadParameters() {

        ParameterManager parameterManager = getSimulationManager().getParameterManager();
        length_init = Float.valueOf(parameterManager.getParameter("app.particle_length", "initial_length"));
        hatch_length = Float.valueOf(parameterManager.getParameter("app.particle_length", "hatch_length"));
        yolk_to_feeding_length = Float.valueOf(parameterManager.getParameter("app.particle_length", "yolk2feeding_length"));
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
