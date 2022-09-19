package org.previmer.ichthyop.particle;

public class SargassumParticleLayer extends ParticleLayer  {
    
    private double density;

    public SargassumParticleLayer(IParticle particle) {
        super(particle);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub 
    }
    
    /** Sargassum value getter */
    public double getDensity() {
        return density;   
    }
    
    /** Sargassum Density setter */
    public void setDensity(double density) {
        this.density = density;   
    }
    
    
    
}
