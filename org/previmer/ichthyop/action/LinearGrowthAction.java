/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.util.Constant;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.particle.GrowingParticleLayer;

/**
 *
 * @author pverley
 */
public class LinearGrowthAction extends AbstractAction {

    /**
     * The growth function assumed the sea water temperature must not be
     * be colder than this threshold. Temperature set in Celsius degree.
     */
    private double tp_threshold;// = 10.d; //°C
    private double coeff1; //0.02d
    private double coeff2; //0.03d

    public void loadParameters() {
        tp_threshold = Float.valueOf(getParameter("growth_tp_threshold"));
        coeff1 = Float.valueOf(getParameter("growth_coeff1"));
        coeff2 = Float.valueOf(getParameter("growth_coeff2"));
    }

    public void execute(IBasicParticle particle) {
        GrowingParticleLayer growthLayer = (GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class);
        growthLayer.setLength(grow(growthLayer.getLength(), getSimulationManager().getDataset().getTemperature(growthLayer.particle().getGridCoordinates(), getSimulationManager().getTimeManager().getTime())));
    }

    private double grow(double length, double temperature) {

        double dt_day = (double) getSimulationManager().getTimeManager().get_dt() / (double) Constant.ONE_DAY;
        length += (coeff1 + coeff2 * Math.max(temperature, tp_threshold)) * dt_day;
        return length;

    }
}
