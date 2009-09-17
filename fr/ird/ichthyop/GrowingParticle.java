/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public abstract class GrowingParticle extends RecruitableParticle implements IGrowingParticle {

    double length;
    /**
     * Characterized the egg stage. In this model, an egg is particle with a
     * length smaller to the hatch-length defined below.
     */
    private static int EGG;
    /**
     * Characterized the "yolk sac" stage. A yolk sac larva has a length ranging
     * from the hatch-length and the yolk-to-feeding-length defined below.
     */
    private static int YOLK_SAC_LARVA;
    /**
     * Characterized the "feeding larva" stage. A feeding larva has a length
     * bigger than the yolk-to-feeding-length defined below.
     */
    private static int FEEDING_LARVA;
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

    @Override
    public void init() {
        super.init();
        loadParameters();
        length = length_init;
    }
    
    private void loadParameters() {
        
        ParameterManager parameterManager = new ParameterManager(getClass());
        EGG = Integer.valueOf(parameterManager.getProperty("stage.egg.code"));
        YOLK_SAC_LARVA = Integer.valueOf(parameterManager.getProperty("stage.yolk-sac-larva.code"));
        FEEDING_LARVA = Integer.valueOf(parameterManager.getProperty("stage.feeding-larva.code"));
        length_init = Float.valueOf(parameterManager.getValue("length.initial"));
        hatch_length = Float.valueOf(parameterManager.getValue("length.hatch"));
        yolk_to_feeding_length = Float.valueOf(parameterManager.getValue("length.yolk-to-feeding"));
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public int getStage() {
        /** Yolk-Sac Larvae */
        if (length >= hatch_length & length < yolk_to_feeding_length) {
            return YOLK_SAC_LARVA;
        } /** Feeding Larvae */
        else if (length >= yolk_to_feeding_length) {
            return FEEDING_LARVA;
        }
        /** eggs */
        return EGG;
    }
}
