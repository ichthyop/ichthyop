/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

/**
 *
 * @author pverley
 */
public interface IGrowingParticle {

    public double getLength();

    public void setLength(double length);

    public Stage getStage();

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
