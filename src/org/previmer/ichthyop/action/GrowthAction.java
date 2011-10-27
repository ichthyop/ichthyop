/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.LengthTracker;
import org.previmer.ichthyop.io.StageTracker;
import org.previmer.ichthyop.particle.GrowingParticleLayer;
import org.previmer.ichthyop.particle.GrowingParticleLayer.Stage;
import org.previmer.ichthyop.particle.IniStageLayer;
import org.previmer.ichthyop.util.Constant;

/**
 *
 * @author pverley
 */
public class GrowthAction extends AbstractAction {

    /**
     * The growth function assumed the sea water temperature must not be
     * be colder than this threshold. Temperature set in Celsius degree.
     */
    private double tp_threshold;// = 10.d; //°C
    private double coeff1; //0.02d
    private double coeff2; //0.03d
    private double temperature;
    private String temperature_field;
    private String largePhyto_field;
    private String smallZoo_field;
    private String largeZoo_field;
    /**
     * Half saturation constant.
     */
    final private static float KS = 0.5f;
    /**
     * Preferency for large phytoplankton [0 ; 1]
     */
    final private static float E21 = 1.f / 3.f;
    /**
     * Preferency for small zooplankton [0 ; 1]
     */
    final private static float E22 = 1.f / 3.f;
    /**
     * Preferency for large zooplankton [0 ; 1]
     */
    final private static float E23 = 1.f / 3.f;
    private TypeGrowth type;

    public void loadParameters() throws Exception {

        type = TypeGrowth.getType(getParameter("type"));
        tp_threshold = Float.valueOf(getParameter("threshold_temp"));
        if (type.equals(TypeGrowth.LINEAR)) {
            coeff1 = Float.valueOf(getParameter("coeff1"));
            coeff2 = Float.valueOf(getParameter("coeff2"));
        }
        temperature_field = getSimulationManager().getParameterManager().getParameter(BlockType.OPTION, "option.biology_dataset", "temperature_field");
        getSimulationManager().getDataset().requireVariable(temperature_field, getClass());
        if (type.equals(TypeGrowth.FOOD_LIMITATED)) {
            largePhyto_field = getParameter("largePhyto_field");
            smallZoo_field = getParameter("smallZoo_field");
            largeZoo_field = getParameter("largeZoo_field");
            getSimulationManager().getDataset().requireVariable(largePhyto_field, getClass());
            getSimulationManager().getDataset().requireVariable(smallZoo_field, getClass());
            getSimulationManager().getDataset().requireVariable(largeZoo_field, getClass());
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

    public void execute(IBasicParticle particle) {
        GrowingParticleLayer gparticle = (GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class);
        double length = -1.d;
        double time = getSimulationManager().getTimeManager().getTime();
        double tp = getSimulationManager().getDataset().get(temperature_field, particle.getGridCoordinates(), time).doubleValue();
        double spawningTp;
        switch (type) {
            case LINEAR:
                length = grow(gparticle.getLength(), tp, particle);
                break;
            case STAGE_DEPENDANT:
                spawningTp = ((IniStageLayer) particle.getLayer(IniStageLayer.class)).getSpawingTemperature();
                length = grow(spawningTp, particle.getAge(), gparticle.getLength(), tp, particle);
                break;
            case FOOD_LIMITATED:
                spawningTp = ((IniStageLayer) particle.getLayer(IniStageLayer.class)).getSpawingTemperature();
                double lphyto = getSimulationManager().getDataset().get(largePhyto_field, particle.getGridCoordinates(), time).doubleValue();
                double lzoo = getSimulationManager().getDataset().get(largeZoo_field, particle.getGridCoordinates(), time).doubleValue();
                double szoo = getSimulationManager().getDataset().get(smallZoo_field, particle.getGridCoordinates(), time).doubleValue();
                length = grow(spawningTp, particle.getAge(), gparticle.getLength(), tp, lphyto, szoo, lzoo, gparticle.getStage());
                break;
        }
        gparticle.setLength(length);
    }

    private double grow(double length, double temperature, IBasicParticle particle) {

        int eggstage = ((GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class)).getEggStage();
        this.temperature = temperature;

        System.out.println("Length GrowthAction: " + (float)length);

        if (eggstage < 10) {
            return length = 2.79d;
        } else {
            double dt_day = (double) getSimulationManager().getTimeManager().get_dt() / (double) Constant.ONE_DAY;
            length += (coeff1 + coeff2 * Math.max(temperature, tp_threshold)) * dt_day;
            return length;}
    }


    public double getTemperature() {
        return temperature;
    }

private double grow(double temp_ini, double age, double length, double temperature, IBasicParticle particle) {
    
        int eggstage = ((GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class)).getEggStage();
        Stage stage = ((GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class)).getStage();this.temperature = temperature;

        double dt_day = (double) getSimulationManager().getTimeManager().get_dt() / (double) Constant.ONE_DAY;
        double edad = age / 86400;
        double constante = 0.20466 + (0.369659 * temperature) - (0.00893519 * temperature * temperature);
        double constante2 = 0.335907 + (0.001603 * temperature);
        double constante3 = 7.87357 - (0.841969 * temperature) + (0.028809 * temperature * temperature);
        double zeta = -constante2 * Math.exp(-1 * constante3 * edad);
        double Regner = (1 / 1.012896) * (1 + Math.exp((4.914322) - (0.257451 * temp_ini)));

        double Lini = 3.4d;
        double Akte = 0.1868d;
        double alfa = 0.0781d;

//        System.out.println("New length:               " + (float)length);
//        System.out.println("==========================================");


        /** eggs */
        if (eggstage < 10){
            return length = 2.79d;
        }else {

        /** Yolk-Sac Larvae */
        if (stage==Stage.YOLK_SAC_LARVA) {
            //length += Lini *  Math.exp((Akte/alfa)*(1- Math.exp(-alfa*dt_day)));
            length += (constante * constante2 * constante3 * Math.exp(-1 * constante3 * (edad - Regner)) * Math.exp(zeta)) * dt_day;
            return length;
        } /** Feeding Larvae */
        else if (stage==Stage.FEEDING_LARVA){
            //length += (coeff1 + coeff2 * Math.max(temperature, tp_threshold)) * dt_day;
            length += (0.02 + 0.03 * Math.max(temperature, tp_threshold)) * dt_day;
            return length;
        }else {
            length = 2.8;
            return length;}
        }
}

//    private double grow(double temp_ini, double age, double length, double temperature, Stage stage) {
//        double erre;
//        /**constante para la expresion de los huevos*/
//        double constante;
//        double constante2;
//        double constante3;
//        double zeta;
//        double edad;
//        /**para pasar el age (sg) a edad (dias) */
//        double Regner;
//        edad = age / 86400;
//        temperature = Math.max(temperature, tp_threshold);
//        erre = 0.0016 * temperature * temperature;
//        constante = 0.20466 + (0.369659 * temperature) - (0.00893519 * temperature * temperature);
//        constante2 = 0.335907 + (0.001603 * temperature);
//        constante3 = 7.87357 - (0.841969 * temperature) + (0.028809 * temperature * temperature);
//        zeta = -constante2 * Math.exp(-1 * constante3 * edad);
//        Regner = (1 / 1.012896) * (1 + Math.exp((4.914322) - (0.257451 * temp_ini)));
//        double dt_day = (double) getSimulationManager().getTimeManager().get_dt() / (double) Constant.ONE_DAY;
//        switch (stage) {
//            case EGG:
//                length += (erre * Math.exp(erre * edad)) * dt_day;
//                break;
//            case YOLK_SAC_LARVA:
//                length += (constante * constante2 * constante3 * Math.exp(-1 * constante3 * (edad - Regner)) * Math.exp(zeta)) * dt_day;
//                break;
//            case FEEDING_LARVA:
//                length += (.02d + .03d * Math.max(temperature,
//                        tp_threshold)) * dt_day;
//                break;
//
//        }
//        return length;
//    }

    /**
     * Esta seria la expresion de crecimiento segun la equacion de Gompertz limitada por comida
     * para los huevos y yolk sac larva sigue la de Gompertz. Si es feeding sigue la original de
     * Ichthyop con el food limiting factor
     */
    private double grow(double temp_ini, double age, double length, double temperature, double lPhyto,
            double sZoo, double lZoo, Stage stage) {

        double erre;
        /* constante para la expresion de los huevos */
        double constante;
        double constante2;
        double constante3;
        double zeta;
        double edad;
        double Regner;
        double foodLimFactor, food;
        double dt_day = (double) getSimulationManager().getTimeManager().get_dt() / (double) Constant.ONE_DAY;

        edad = age / 86400;
        temperature = Math.max(temperature, tp_threshold);
        erre = 0.0016 * temperature * temperature;
        constante = 0.20466 + (0.369659 * temperature) - (0.00893519 * temperature * temperature);
        constante2 = 0.335907 + (0.001603 * temperature);
        constante3 = 7.87357 - (0.841969 * temperature) + (0.028809 * temperature * temperature);
        zeta = -constante2 * Math.exp(-1 * constante3 * edad);
        Regner = (1 / 1.012896) * (1 + Math.exp((4.914322) - (0.257451 * temp_ini)));
        switch (stage) {
            case EGG:
                foodLimFactor = 1.f;
                length += foodLimFactor * (erre * Math.exp(erre * edad)) * dt_day;
                break;
            case YOLK_SAC_LARVA:
                foodLimFactor = 1.f;
                length += foodLimFactor * (constante * constante2 * constante3 * Math.exp(-1 * constante3 * (edad - Regner)) * Math.exp(zeta)) * dt_day;
                break;
            case FEEDING_LARVA:
                food = E21 * lPhyto + E22 * sZoo + E23 * lZoo;
                foodLimFactor = food / (KS + food);
                length += foodLimFactor * (.02d + .03d * Math.max(temperature,
                        tp_threshold)) * dt_day;

                break;

        }
        return length;
    }

    enum TypeGrowth {

        LINEAR("Linear growth"),
        STAGE_DEPENDANT("Equation based on particle stage"),
        FOOD_LIMITATED("Food limitated");
        private String type;

        TypeGrowth(String type) {
            this.type = type;
        }

        String getType() {
            return type;
        }

        static TypeGrowth getType(String type) {

            for (TypeGrowth typeGrowth : values()) {
                if (type.matches(typeGrowth.getType())) {
                    return typeGrowth;
                }
            }
            return LINEAR;
        }
    }
}
