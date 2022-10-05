package org.previmer.ichthyop.particle;

public class SargassumParticleLayer extends ParticleLayer  {
    
    private double biomass;

    public SargassumParticleLayer(IParticle particle) {
        super(particle);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub 
    }
    
    /** Sargassum value getter */
    public double getBiomass() {
        return biomass;
    }
    
    /** Sargassum Density setter */
    public void setBiomass(double biomass) {
        this.biomass = biomass;
    }
    
    
    
}
