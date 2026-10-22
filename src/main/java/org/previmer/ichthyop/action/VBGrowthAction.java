package org.previmer.ichthyop.action;

import java.util.Random;

import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.LengthTracker;
import org.previmer.ichthyop.io.StageTracker;
import org.previmer.ichthyop.manager.ParameterManager;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.LengthParticleLayer;
import org.previmer.ichthyop.particle.StageParticleLayer;
import org.previmer.ichthyop.stage.LengthStage;
import org.previmer.ichthyop.util.Constant;

public class VBGrowthAction extends AbstractAction {


    private float l_inf;
    private float growth_coefficient;
    private float t0;
    private float age_mean;
    private float age_std;
    private LengthStage lengthStage;
    private Random randomGenerator;

    @Override
    public void loadParameters() throws Exception {

        boolean isFixedSeed = ParameterManager.getInstance().getConfigurationFile().isFixedSeed();

        l_inf = Float.valueOf(getParameter("vb_l_inf"));
        growth_coefficient = Float.valueOf(getParameter("vb_growth_coeff"));
        t0 = Float.valueOf(getParameter("vb_t0"));
        age_mean = Float.valueOf(getParameter("vb_age_mean"));
        age_std = Float.valueOf(getParameter("vb_age_std"));

        lengthStage = new LengthStage(BlockType.ACTION, getBlockKey());
        lengthStage.init();

        // Fix the seed if needed
        if (isFixedSeed) {
            randomGenerator = new Random(getSimulationManager().getIndexSimulation());
        } else {
            randomGenerator = new Random();
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
    public void init(IParticle particle) {

        // For the particle, initialize the age value
        // the argument here is the age in days
        // which is converted in seconds in the setAge function

        double age = age_mean + age_std * randomGenerator.nextGaussian();
        age = Math.max(0, age);

        particle.setAge(age);

        // returns the length based on initial age of particle
        double initial_length = this.getLength(particle.getAge());

        LengthParticleLayer lengthLayer = (LengthParticleLayer) particle.getLayer(LengthParticleLayer.class);
        lengthLayer.setLength(initial_length);
    }

    @Override
    public void execute(IParticle particle) {

        if(!this.isActive(particle)) {
            return;
        }

        LengthParticleLayer lengthLayer = (LengthParticleLayer) particle.getLayer(LengthParticleLayer.class);
        lengthLayer.setLength(this.getLength(particle.getAge()));

        StageParticleLayer stageLayer = (StageParticleLayer) particle.getLayer(StageParticleLayer.class);
        stageLayer.setStage(lengthStage.getStage((float) lengthLayer.getLength()));

    }

    /** Computes the lenght based on the VB
     * Age is provided in seconds. It is converted in days before applying the formula
     *
     * Returns the length in cm
     */
    public double getLength(long age) {

        // convert age from days to length
        double age_days = (double) ( age / Constant.ONE_DAY);
        double length = l_inf * (1 - Math.exp(-growth_coefficient * (age_days - t0)));
        return length;

    }

}
