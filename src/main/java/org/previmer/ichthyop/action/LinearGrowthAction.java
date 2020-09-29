/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
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
