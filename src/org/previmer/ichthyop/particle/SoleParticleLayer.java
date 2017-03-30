/*
 * Copyright (C) 2014 pverley
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.particle;

import static org.previmer.ichthyop.SimulationManagerAccessor.getSimulationManager;
import org.previmer.ichthyop.io.BlockType;

/**
 *
 * @author pverley
 */
public class SoleParticleLayer extends ParticleLayer {

    /**
     * Particle length [millimeter]
     */
    double length;
    /**
     * Initial length [millimetre] for the particles.
     */
    private static double length_init;// 1.2mm
    /**
     * Threshold [millimetre] to distinguish eggs from larvae
     */
    private static double hatch_length;// 3mm
    /**
     * Threshold [millimetre] between Yolk-Sac Larvae and Feeding Larvae
     */
    private static double yolk_to_feeding_length;// 4mm
    /**
     * Threshold [millimetre] between First feeding Larvae and Metamorphosing
     * Larvae
     */
    private static double feeding_to_metamorphosing_length; // 8mm

    public SoleParticleLayer(IParticle particle) {
        super(particle);
    }

    @Override
    public void init() {
        
        length_init = Float.valueOf(getSimulationManager().getParameterManager().getParameter(BlockType.ACTION, "action.growth.sole", "egg_length"));
        hatch_length = Float.valueOf(getSimulationManager().getParameterManager().getParameter(BlockType.ACTION, "action.growth.sole", "hatch_length"));
        yolk_to_feeding_length =  Float.valueOf(getSimulationManager().getParameterManager().getParameter(BlockType.ACTION, "action.growth.sole", "yolk2feeding_length"));
        feeding_to_metamorphosing_length = Float.valueOf(getSimulationManager().getParameterManager().getParameter(BlockType.ACTION, "action.growth.sole", "feeding2metamorphosing_length"));
        
        length = length_init;
    }
    
    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public Stage getStage() {

        if (length >= hatch_length & length < yolk_to_feeding_length) {
            return Stage.YOLK_SAC_LARVA;
        } else if (length >= yolk_to_feeding_length & length < feeding_to_metamorphosing_length) {
            return Stage.FEEDING_LARVA;
        } else if (length >= feeding_to_metamorphosing_length) {
            return Stage.METAMORPHOSING_LARVA;
        }
        return Stage.EGG;
    }

    public enum Stage {

        /**
         * Characterized the egg stage. In this model, an egg is particle with a
         * length smaller to the hatch-length defined below.
         */
        EGG(0),
        /**
         * Characterized the "yolk sac" stage. A yolk sac larva has a length
         * ranging from the hatch-length and the yolk-to-feeding-length defined
         * below.
         */
        YOLK_SAC_LARVA(1),
        /**
         * Characterized the "feeding larva" stage. A feeding larva has a length
         * bigger than the yolk-to-feeding-length defined below.
         */
        FEEDING_LARVA(2),
        /**
         * Additional Feeding larva stage, needed by Susanne
         */
        METAMORPHOSING_LARVA(3);
        /* numerical code corresponding to the stage */
        private final int code;

        Stage(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

}