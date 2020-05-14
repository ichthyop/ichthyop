/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Nicolas BARRIER, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
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

package org.previmer.ichthyop.action;

import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.LengthTracker;
import org.previmer.ichthyop.io.StageTracker;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.LengthParticleLayer;
import org.previmer.ichthyop.particle.StageParticleLayer;
import org.previmer.ichthyop.stage.LengthStage;
import org.previmer.ichthyop.util.Constant;

/**
 * Linear growth but with a constrain from a holling II 
 * functional response.
 * 
 * @author pverley
 * @author nbarrier
 */
public class LinearGrowthAction extends AbstractAction {

    /**
     * The growth function assumed the sea water temperature must not be be
     * colder than this threshold. Temperature set in Celsius degree.
     */
    private double tp_threshold;// = 10.d; //°C
    private double coeff1; //0.02d
    private double coeff2; //0.03d
    private double ks = 0;
    private String temperature_field;
    private String food_field;
    private LengthStage lengthStage;

    @Override
    public void loadParameters() throws Exception {
        tp_threshold = Float.valueOf(getParameter("threshold_temp"));
        coeff1 = Float.valueOf(getParameter("coeff1"));
        coeff2 = Float.valueOf(getParameter("coeff2"));
        temperature_field = getParameter("temperature_field");
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());
        lengthStage = new LengthStage(BlockType.ACTION, getBlockKey());
        lengthStage.init();

        // barrier.n: modifications for hilaire.
        if(!isNull("half_saturation")) {
            // if half saturation parameter exists, load it
            ks = Float.valueOf(getParameter("half_saturation"));
        }
        
        if(ks > 0) { 
            // if ks is not null, need to load the food field
            food_field = getParameter("food_field");
            getSimulationManager().getDataset().requireVariable(food_field, getClass());
        }
        
        boolean addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("length_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(LengthTracker.class);
        }
        addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("stage_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(StageTracker.class);
        }
    }

    @Override
    public void init(IParticle particle
    ) {
        LengthParticleLayer lengthLayer = (LengthParticleLayer) particle.getLayer(LengthParticleLayer.class);
        lengthLayer.setLength(lengthStage.getThreshold(0));
    }

    @Override
    public void execute(IParticle particle
    ) {
        LengthParticleLayer lengthLayer = (LengthParticleLayer) particle.getLayer(LengthParticleLayer.class);
        double temperature = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
       
        // If ks == 0, give food a dummy value since is unused.
        // If ks i not null, load value from file.
        double food = (ks == 0) ? 1 : getSimulationManager().getDataset().get(food_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue();
        
        // Increments length providing temperature and food
        lengthLayer.incrementLength(grow(temperature, food));
        
        StageParticleLayer stageLayer = (StageParticleLayer) particle.getLayer(StageParticleLayer.class);
        stageLayer.setStage(lengthStage.getStage((float) lengthLayer.getLength()));
    }

    private double grow(double temperature, double food) {

        // if ks = 0, then Q is alwyas 1, independent of food
        double Q = (ks == 0) ? 1 : food / (food + ks);
        
        double dt_day = (double) getSimulationManager().getTimeManager().get_dt() / (double) Constant.ONE_DAY;
        // temperature may be NaN in dry cells at low tide
        // improvement suggested by David S Wethey, 2017.02.10
        return (Double.isNaN(temperature) || Double.isNaN(food))
                ? 0.d
                : (coeff1 + coeff2 * Math.max(temperature, tp_threshold)) * Q * dt_day;

    }
}
