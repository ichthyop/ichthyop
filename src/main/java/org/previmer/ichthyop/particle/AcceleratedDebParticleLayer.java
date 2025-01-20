package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.manager.ParameterManager;

public class AcceleratedDebParticleLayer extends ParticleLayer {

    /**
     * Particle length [millimeter]
     */
    private double length;

    /**
     * Initial length [millimeter] for the particles.
     */
    // private static double length_init;// = 0.025d; // mm

    /**
     * Threshold [millimiter] to distinguish eggs from larvae
     */
    // private static double hatch_length;// = 2.8d; //mm

    /**
     * Threshold [millimeter] between Yolk-Sac Larvae and Feeding Larvae
     */
    // private static double yolk_to_feeding_length;// = 4.5d; //mm
    // private double Vj; // Structure at mouth opening (yolk_to_feeding)

    /*
     * DEB PARAMETERS
     */
    // Variables
    private double E; // Reserve
    private double V; // Structure
    private double E_H; // Cumulated energy invested into development
    private double E_R; // Reproduction buffer

    // parameters conversion
    // size related conversion params
    private double shape_larvae;

    public AcceleratedDebParticleLayer(IParticle particle) {
        super(particle);
    }

    @Override
    public void init() {
        loadParameters();
        // length = length_init;
        V = 0.0000001; // Math.pow(shape_larvae*length,3);
    }

    private void loadParameters() {
        ParameterManager parameterManager = getSimulationManager().getParameterManager();
        // length_init =
        // Float.valueOf(parameterManager.getParameter("app.particle_length",
        // "initial_length"));
        // shape_larvae =
        // Double.valueOf(parameterManager.getParameter(BlockType.ACTION,"action.growthDeb","shape"));
        // //0.152 ; larvae < 3.7cm length and weight data - Palomera et al.
        E = Double.valueOf(parameterManager.getParameter(BlockType.ACTION, "action.growthDeb", "initial_reserve"));// 0.022;//0.89998;//
                                                                                                                   // //-0.087209;
                                                                                                                   // //
                                                                                                                   // J,
                                                                                                                   // Reserve
                                                                                                                   // at
                                                                                                                   // size
                                                                                                                   // of
                                                                                                                   // hatching.
        E_R = 0;
        E_H = 0;
    }

    // public void computeLength(){
    // length=Math.pow(V,1/3.0)/shape_larvae;
    // }

    public void computeLength() {

        // Shape larvae abj model
        double shape; // Shape coefficient (-)

        // Maturity at birth (E_Hb) = 0.3339
        // Maturity at metamorphosis (E_Hj) = 59.66
        // Shape coefficient for standard length = 0.1879
        // shape coefficient for standard length of larvae = 0.0791
        if (E_H < 0.3339) {
            shape = 0.0791;
        } else if ((0.3339 <= E_H) && (E_H < 59.66)) {
            shape = (0.0791 * (59.66 - E_H) + 0.1879 * (E_H - 0.3339)) / (59.66 - 0.3339);
        } else {
            shape = 0.1879;
        }
        length = Math.pow(V, 1 / 3.0) / shape;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getE() {
        return E;
    }

    public void setE(double E) {
        this.E = E;
    }

    public double getV() {
        return V;
    }

    public void setV(double V) {
        this.V = V;
    }

    public double getE_R() {
        return E_R;
    }

    public void setE_R(double E_R) {
        this.E_R = E_R;
    }

    public double getE_H() {
        return E_H;
    }

    public void setE_H(double E_H) {
        this.E_H = E_H;
    }

    // public Stage getStage() {
    /** Yolk-Sac Larvae */
    // if (length >= hatch_length & length < yolk_to_feeding_length) {
    // return Stage.YOLK_SAC_LARVA;
    // } /** Feeding Larvae */
    // else if (length >= yolk_to_feeding_length) {
    // return Stage.FEEDING_LARVA;
    // }
    /** eggs */
    // return Stage.EGG;
    // }

    // public enum Stage {

    /**
     * Characterized the egg stage. In this model, an egg is particle with a length
     * smaller to the hatch-length defined below.
     */
    // EGG(0),
    /**
     * Characterized the "yolk sac" stage. A yolk sac larva has a length ranging
     * from the hatch-length and the yolk-to-feeding-length defined below.
     */
    // YOLK_SAC_LARVA(1),
    /**
     * Characterized the "feeding larva" stage. A feeding larva has a length bigger
     * than the yolk-to-feeding-length defined below.
     */
    // FEEDING_LARVA(2);
    /* numerical code corresponding to the stage */
    // private int code;

    // Stage(int code) {
    // this.code = code;
    // }

    // public int getCode() {
    // return code;
    // }
    // }
}
