/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public class LinearGrowth extends AbstractAction {

    /**
     * The growth function assumed the sea water temperature must not be
     * be colder than this threshold. Temperature set in Celsius degree.
     */
    private double tp_threshold;// = 10.d; //°C
    private double coeff1; //0.02d
    private double coeff2; //0.03d

    public void loadParameters() {
        tp_threshold = Float.valueOf(getParameter("growth.temperature.threshold"));
        coeff1 = Float.valueOf(getParameter("growth.equation.coeff1"));
        coeff2 = Float.valueOf(getParameter("growth.equation.coeff2"));
    }

    public void execute(IBasicParticle particle) {
        IGrowingParticle gParticle = (IGrowingParticle) particle;
        gParticle.setLength(grow(gParticle.getLength(), getSimulation().getDataset().getTemperature(gParticle.getGridPoint(), getSimulation().getStep().getTime())));
    }

    private double grow(double length, double temperature) {

        double dt_day = (double) getSimulation().getStep().get_dt() / (double) Constant.ONE_DAY;
        length += (coeff1 + coeff2 * Math.max(temperature, tp_threshold)) * dt_day;
        return length;

    }
}
