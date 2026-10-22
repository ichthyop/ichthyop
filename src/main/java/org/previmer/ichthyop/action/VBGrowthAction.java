package org.previmer.ichthyop.action;

import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.LengthParticleLayer;
import org.previmer.ichthyop.particle.StageParticleLayer;
import org.previmer.ichthyop.stage.LengthStage;
import org.previmer.ichthyop.util.Constant;

public class VBGrowthAction extends AbstractAction {


    private float l_inf;
    private float growth_coefficient;
    private float t0;
    private float age_init;
    private LengthStage lengthStage;

    @Override
    public void loadParameters() throws Exception {

        l_inf = Float.valueOf(getParameter("vb_l_inf"));
        growth_coefficient = Float.valueOf(getParameter("vb_growth_coeff"));
        t0 = Float.valueOf(getParameter("vb_t0"));
        age_init = Float.valueOf(getParameter("age_t0"));

        lengthStage = new LengthStage(BlockType.ACTION, getBlockKey());
        lengthStage.init();

    }


    @Override
    public void init(IParticle particle) {

        // For the particle, initialize the age value
        // the argument here is the age in days
        // which is converted in seconds in the setAge function
        particle.setAge(age_init);

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
