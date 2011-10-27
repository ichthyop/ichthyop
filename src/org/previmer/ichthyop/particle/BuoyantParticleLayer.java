package org.previmer.ichthyop.particle;

import java.util.logging.Level;
import org.previmer.ichthyop.action.BuoyancyAction;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.manager.ParameterManager;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 * Modifications: Andrés Ospina
 */
public class BuoyantParticleLayer extends ParticleLayer {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    private double spawningDensity;
    private double specificGravity;
    private double temperature;
    private double salinity;
    private double waterDensity;

    public BuoyantParticleLayer(IBasicParticle particle) {
        super(particle);
    }

    @Override
    public void init() {

        try {
            ParameterManager parameterManager = getSimulationManager().getParameterManager();
            String salinity_field = parameterManager.getParameter(BlockType.OPTION, "option.biology_dataset", "salinity_field");
            String temperature_field = parameterManager.getParameter(BlockType.OPTION, "option.biology_dataset", "temperature_field");
            double time = getSimulationManager().getTimeManager().get_tO();
            double sal = getSimulationManager().getDataset().get(salinity_field, particle().getGridCoordinates(), time).doubleValue();
            double tp = getSimulationManager().getDataset().get(temperature_field, particle().getGridCoordinates(), time).doubleValue();
            spawningDensity = BuoyancyAction.waterDensity(sal, tp);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
            spawningDensity = 0.d;
        }
        //System.out.println("spawning density = " + spawningDensity);
        specificGravity = spawningDensity;
    }


    public double getSpecificGravity() {
        return specificGravity;
    }

    public double computeSpecificGravity(double temperature, double salinity, double waterDensity, IBasicParticle particle) {

        double ratioStage = ((GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class)).ratiostage(temperature, salinity, waterDensity);
        this.salinity = salinity;
        this.waterDensity = waterDensity;

        int dt = getSimulationManager().getTimeManager().get_dt();

        double ratDev = -1.424E-03 * Math.pow(ratioStage, 3) + 2.070E-02
                * Math.pow(ratioStage, 2) + 3.554E-02 * ratioStage;

        specificGravity = (41.568 * Math.pow(ratDev, 6) - 149.788 * Math.pow(ratDev, 5)
                + 200.864 * Math.pow(ratDev, 4) - 114.05 * Math.pow(ratDev, 3) + 19.789
                * Math.pow(ratDev, 2) + 2.423 * ratDev) * 0.0012 + spawningDensity;

        return specificGravity;
    }

    /**
     * @return the temperature
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * @return the salinity
     */
    public double getSalinity() {
        return salinity;
    }

    /**
     * @return the waterDensity
     */
    public double getWaterDensity() {
        return waterDensity;
    }
}