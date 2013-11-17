/*
 * Copyright (C) 2012 gandres
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

import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.manager.ParameterManager;

/**
 *
 * @author gandres
 */
public class DebParticleLayer extends ParticleLayer {

     /**
     * Particle length [millimeter]
     */
     private double length;
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
    
    private double Vj; // Structure at mouth opening (yolk_to_feeding)
    
    
    /*
     * DEB PARAMETERS
     */
    // Variables
    private double E; // Réserve
    private double V; // Structure
    private double E_R; // Maturité
    
    // parameters conversion
    // size related conversion params
    private double  shape_larvae ;
   

    

    public DebParticleLayer(IParticle particle) {
        super(particle);
    }
    @Override
    public void init() {
        loadParameters();
        length = length_init;
        V= Math.pow(shape_larvae*length,3);
        Vj=Math.pow(shape_larvae*yolk_to_feeding_length,3);

    }
    
     private void loadParameters() {
        ParameterManager parameterManager = getSimulationManager().getParameterManager();
        length_init = Float.valueOf(parameterManager.getParameter("app.particle_length", "initial_length"));
        hatch_length = Float.valueOf(parameterManager.getParameter("app.particle_length", "hatch_length"));
        yolk_to_feeding_length = Float.valueOf(parameterManager.getParameter("app.particle_length", "yolk2feeding_length"));
    

        shape_larvae = Double.valueOf(parameterManager.getParameter(BlockType.ACTION,"action.growthDeb","shape")); //0.152 ; larvae < 3.7cm length and weight data  - Palomera et al.
        E= Double.valueOf(parameterManager.getParameter(BlockType.ACTION,"action.growthDeb","initial_reserve"));//0.022;//0.89998;// //-0.087209;      // J, Reserve at size of hatching.

        E_R = 0;
     
   }
    
     public void computeLength(){
         length=Math.pow(V,1/3.0)/shape_larvae;
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
    
    public double getVj() {
        return Vj;
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
