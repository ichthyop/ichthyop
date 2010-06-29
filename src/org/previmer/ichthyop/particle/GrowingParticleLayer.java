/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.arch.IGrowingParticle;
import org.previmer.ichthyop.arch.IParameterManager;

/**
 *
 * @author pverley
 */
public class GrowingParticleLayer extends ParticleLayer implements IGrowingParticle {

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

    public GrowingParticleLayer(IBasicParticle particle) {
        super(particle);
    }

    public void init() {
        loadParameters();
        length = length_init;
    }

    private void loadParameters() {

        IParameterManager parameterManager = getSimulationManager().getParameterManager();
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
}
