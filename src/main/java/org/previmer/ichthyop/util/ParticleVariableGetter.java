package org.previmer.ichthyop.util;

import java.util.logging.Level;

import org.apache.commons.logging.Log;
import org.previmer.ichthyop.manager.SimulationManager;
import org.previmer.ichthyop.particle.DebParticleLayer;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.LengthParticleLayer;
import org.previmer.ichthyop.particle.StageParticleLayer;

public class ParticleVariableGetter {

    @FunctionalInterface
    public interface VariableGetterInterface {
        public double getVariable(IParticle particle);
    }

    private VariableGetterInterface variableGetter;
    private EnumVariable variableDescription;

    public ParticleVariableGetter(String variable_name) {

        boolean isGrowth = CheckGrowthParam.checkParams();

        // Age can be used if growth is not activated
        if (variable_name.equals("age")) {
            variableDescription = EnumVariable.AGE;
            variableGetter = (particle) -> particle.getAge() / (24.0 * 60 * 60);
            return;
        }

        if (isGrowth) {

            // If growth is activated (either classical or DEB), we can use length and stage.
            switch (variable_name) {
                case "length": {
                    // Return length in cm
                    variableDescription = EnumVariable.LENGTH;
                    variableGetter = (particle) -> ((LengthParticleLayer) particle.getLayer(LengthParticleLayer.class))
                            .getLength();
                    break;
                }
                case "stage": {
                    // Return the stage value
                    variableDescription = EnumVariable.STAGE;
                    variableGetter = (
                            particle) -> (double) ((StageParticleLayer) particle.getLayer(StageParticleLayer.class))
                                    .getStage();
                    break;
                }
            } // end of length/stage switch

            // If Deb growth (eigher classical or accelerated DEB), can use V, E or E_R
            if (CheckGrowthParam.isAcceleratedDebGrowth || CheckGrowthParam.isDebGrowth) {
                switch (variable_name) {
                    case "V": {
                        // Return length in cm
                        variableDescription = EnumVariable.STRUCTURE;
                        variableGetter = (particle) -> ((DebParticleLayer) particle.getLayer(DebParticleLayer.class)).getV();
                        break;
                    }
                    case "E": {
                        // Return the stage value
                        variableDescription = EnumVariable.RESERVE;
                        variableGetter = (particle) -> ((DebParticleLayer) particle.getLayer(DebParticleLayer.class)).getE();
                        break;
                    }
                    case "E_R": {
                        variableDescription = EnumVariable.REPRO_BUFFER;
                        variableGetter = (particle) -> ((DebParticleLayer) particle.getLayer(DebParticleLayer.class)).getE_R();
                        break;
                    }
                } // end if E/V/E_R switch
            } // end of DEB check

            if(CheckGrowthParam.isAcceleratedDebGrowth && variable_name.equals("E_H")) {
                variableDescription = EnumVariable.CUMU_ENERGY;
                variableGetter = (particle) -> ((DebParticleLayer) particle.getLayer(DebParticleLayer.class)).getE_H();
            }

        } // end of is growth statement

        if (variableGetter == null) {
            SimulationManager.getLogger().log(Level.WARNING, "Wrong threshold provided.");
            SimulationManager.getLogger().log(Level.WARNING, "Should be among:");
            for (Enum<EnumVariable> temp : EnumVariable.values()) {
                SimulationManager.getLogger().log(Level.WARNING, temp.toString());
            }
            variableDescription = EnumVariable.AGE;
            variableGetter = (particle) -> particle.getAge() / (24.0 * 60 * 60);
        }

    }

    public VariableGetterInterface getVariableGetter() {
        return variableGetter;
    }

    public double getValue(IParticle particle) {
        return variableGetter.getVariable(particle);
    }

    public String toString() {
        return variableDescription.toString();
    }

    public enum EnumVariable {

        AGE(0, "age", "days"),
        LENGTH(1, "length", "cm"),
        STAGE(2, "stage", ""),
        STRUCTURE(3, "V", "J"),
        RESERVE(4, "E", "J"),
        REPRO_BUFFER(5, "E_R", "J"),
        CUMU_ENERGY(6, "E_H", "J");

        private int code;
        private String name;
        private String unit;

        EnumVariable(int code, String name, String unit) {
            this.code = code;
            this.name = name;
            this.unit = unit;
        }

        public int getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getUnit() {
            return unit;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}