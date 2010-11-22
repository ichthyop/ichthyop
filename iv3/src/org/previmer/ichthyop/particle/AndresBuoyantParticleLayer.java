package org.previmer.ichthyop.particle;

import java.util.logging.Level;
import org.previmer.ichthyop.action.BuoyancyAction;
import org.previmer.ichthyop.arch.IAndresBuoyantParticle;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.arch.IParameterManager;
import org.previmer.ichthyop.io.BlockType;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class AndresBuoyantParticleLayer extends ParticleLayer implements IAndresBuoyantParticle {

///////////////////////////////
// Declaration of the constants
///////////////////////////////
    final public static double alfaK = 42922.0767d;
    final public static double betaK = 2.290236d;
    final public static double K2 = 6953.4d;
    final public static double K3 = 3562.5d;
    final public static double K4 = 6438.3d;
    final public static double K5 = 3476.7d;
    final public static double K6 = 8284d;
    final public static double K7 = 4678.5d;
    final public static double K8 = 4807.2d;
    final public static double K9 = 2704.1d;
    final public static double K10 = 2017.4d;
///////////////////////////////
// Declaration of the variables
///////////////////////////////
    private double ratioStage;
    private double spawningDensity;
    private double specificGravity;
    private double temperature;
    private double salinity;
    private double waterDensity;

    public AndresBuoyantParticleLayer(IBasicParticle particle) {
        super(particle);
    }

    @Override
    public void init() {

        ratioStage = 1.d;
        try {
            IParameterManager parameterManager = getSimulationManager().getParameterManager();
            String salinity_field = parameterManager.getParameter(BlockType.ACTION, "action.andres_buoyancy", "salinity_field");
            String temperature_field = parameterManager.getParameter(BlockType.ACTION, "action.andres_buoyancy", "temperature_field");
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

    public int getStage() {
        return (int) Math.min(Math.floor(ratioStage), 10);
    }

    public double getSpecificGravity() {
        return specificGravity;
    }

    public double computeSpecificGravity(double temperature, double salinity, double waterDensity) {

        this.temperature = temperature;
        this.salinity = salinity;
        this.waterDensity = waterDensity;

        int dt = getSimulationManager().getTimeManager().get_dt();
        double stageDuration = 0.d;

        int stage = getStage();

        if (stage == 1) {
            stageDuration = K2 * Math.pow(temperature, -betaK);

        } else if (stage == 2) // Stage 3
        {
            stageDuration = K3 * Math.pow(temperature, -betaK);

        } else if (stage == 3) // Stage 4
        {
            stageDuration = K4 * Math.pow(temperature, -betaK);

        } else if (stage == 4) // Stage 5
        {
            stageDuration = K5 * Math.pow(temperature, -betaK);

        } else if (stage == 5) // Stage 6
        {
            stageDuration = K6 * Math.pow(temperature, -betaK);

        } else if (stage == 6) // Stage 7
        {
            stageDuration = K7 * Math.pow(temperature, -betaK);

        } else if (stage == 7) // Stage 8
        {
            stageDuration = K8 * Math.pow(temperature, -betaK);

        } else if (stage == 8) // Stage 9
        {
            stageDuration = K9 * Math.pow(temperature, -betaK);

        } else if (stage == 9) // Stage 10
        {
            stageDuration = K10 * Math.pow(temperature, -betaK);

        }
        ratioStage = ratioStage + (dt / 3600.f) / stageDuration;


        double ratDev = -1.424E-03 * Math.pow(ratioStage, 3) + 2.070E-02
                * Math.pow(ratioStage, 2) + 3.554E-02 * ratioStage;

        specificGravity = (41.568 * Math.pow(ratDev, 6) - 149.788 * Math.pow(ratDev, 5)
                + 200.864 * Math.pow(ratDev, 4) - 114.05 * Math.pow(ratDev, 3) + 19.789
                * Math.pow(ratDev, 2) + 2.423 * ratDev) * 0.0012 + spawningDensity;


        /*
        StringBuffer info = new StringBuffer();
        info.append("index:           ");
        info.append(particle().getIndex());
        info.append("\n");
        info.append("spawningDensity: ");
        info.append((float) spawningDensity);
        info.append("\n");
        info.append("age in days:     ");
        info.append(particle().getAge() / 3600.f);
        info.append("\n");
        info.append("ratioStage:      ");
        info.append((float) ratioStage);
        info.append("\n");
        info.append("stage:           ");
        info.append(getStage());
        info.append("\n");
        info.append("ratDev:          ");
        info.append((float) ratDev);
        info.append("\n");
        info.append("temp:            ");
        info.append((float) temperature);
        info.append("\n");
        info.append("specGravity:     ");
        info.append((float) spec_gravity);
        info.append("\n");
        info.append("stageDuration:   ");
        info.append((float) stageDuration);
        info.append("\n");
        info.append("-----------------");
        System.out.println(info.toString());
         */

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
