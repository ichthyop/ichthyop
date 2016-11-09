/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
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
