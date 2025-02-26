package org.previmer.ichthyop.util;

import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.LengthParticleLayer;
import org.previmer.ichthyop.particle.StageParticleLayer;

public class ParticleVariableGetter {

    @FunctionalInterface
    public interface VariableGetterInterface {
        public double getVariable(IParticle particle);
    }

    private final VariableGetterInterface variableGetter;
    private final EnumVariable variableDescription;

    public ParticleVariableGetter(String variable_name) {
        switch (variable_name) {
            case "length": {
                // Return length in cm
                variableDescription = EnumVariable.LENGTH;
                variableGetter = (particle) -> ((LengthParticleLayer) particle.getLayer(LengthParticleLayer.class)).getLength();
                break;
            }
            case "age": {
                // return age in days
                variableDescription = EnumVariable.AGE;
                variableGetter = (particle) -> particle.getAge() / (24.0 * 60 * 60);
                break;
            }

            case "stage": {
                // Return the stage value
                variableDescription = EnumVariable.STAGE;
                variableGetter = (particle) -> (double) ((StageParticleLayer) particle.getLayer(StageParticleLayer.class)).getStage();
                break;
            }

            default:
                // return age in days by default
                variableDescription = EnumVariable.AGE;
                variableGetter = (particle) -> particle.getAge() / (24.0 * 60 * 60);
                break;
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

        AGE(0, "age", "days"), LENGTH(1, "length", "cm"), STAGE(2, "stage", "");

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
            return String.format("Variable = %s, unit = %s", name().toLowerCase(), getUnit().toLowerCase());
        }
    }

}