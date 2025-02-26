package org.previmer.ichthyop.util;

import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.LengthParticleLayer;

public class ParticleVariableGetter {

    @FunctionalInterface
    public interface VariableGetterInterface {
        public double getVariable(IParticle particle);
    }

    private final VariableGetterInterface variableGetter;

    public ParticleVariableGetter(String variable_name) {
        switch (variable_name) {
            case "length": {
                // Return length in cm
                variableGetter = (particle) -> ((LengthParticleLayer) particle.getLayer(LengthParticleLayer.class)).getLength();
                break;
            }
            case "age": {
                // return age in days
                variableGetter = (particle) -> particle.getAge() / (24 * 60 * 60);
                break;
            }
            default:
                // return age in days by default
                variableGetter = (particle) -> particle.getAge() / (24 * 60 * 60);
                break;
        }
    }

    public VariableGetterInterface getVariableGetter() {
        return variableGetter;
    }

    public double getValue(IParticle particle) {
        return variableGetter.getVariable(particle);
    }

    public enum EnumVariable {

        AGE(0, "age"), LENGTH(1, "length");

        private int code;
        private String name;

        EnumVariable(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public int getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

}