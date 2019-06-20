/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author pverley
 */
public class LinearGrowthAction extends AbstractAction {

    /**
     * The growth function assumed the sea water temperature must not be be
     * colder than this threshold. Temperature set in Celsius degree.
     */
    private double tp_threshold;// = 10.d; //°C
    private double coeff1; //0.02d
    private double coeff2; //0.03d
    private String temperature_field;
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
        lengthLayer.incrementLength(grow(getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).doubleValue()));
        StageParticleLayer stageLayer = (StageParticleLayer) particle.getLayer(StageParticleLayer.class);
        stageLayer.setStage(lengthStage.getStage((float) lengthLayer.getLength()));
    }

    private double grow(double temperature) {

        double dt_day = (double) getSimulationManager().getTimeManager().get_dt() / (double) Constant.ONE_DAY;
        // temperature may be NaN in dry cells at low tide
        // improvement suggested by David S Wethey, 2017.02.10
        return Double.isNaN(temperature)
                ? 0.d
                : (coeff1 + coeff2 * Math.max(temperature, tp_threshold)) * dt_day;

    }
}
