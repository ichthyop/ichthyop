package org.previmer.ichthyop.particle;

public class SargassumParticleLayer extends ParticleLayer  {
    
    private double biomass;
    private double C, N, P;
    public static final double CARBON_TO_DRY_WEIGHT_RATIO = 0.27;

    public SargassumParticleLayer(IParticle particle) {
        super(particle);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub
    }

    public void init(double biomass) {
        this.biomass = biomass;
        this.C = biomass / CARBON_TO_DRY_WEIGHT_RATIO;
    }
    
    /** Sargassum value getter */
    public double getBiomass() {
        return biomass;
    }
    
    /** Sargassum Density setter */
    public void setBiomass(double biomass) {
        this.biomass = biomass;
    }

    /** Carbon value getter */
    public double getC() {
        return C;
    }

    /** Carbon Density setter */
    public void setC(double C_content) {
        this.C = C_content;
    }

    /** Nitrogen value getter */
    public double getN() {
        return N;
    }

    /** Nitrogen Density setter */
    public void setN(double N_content) {
        this.N = N_content;
    }

    /** Phosphor value getter */
    public double getP() {
        return P;
    }

    /** Phosphor Density setter */
    public void setP(double P_content) {
        this.P = P_content;
    }

    /** Nitrogen quota value getter */
    public double getQuotaN() {
        return N/C;
    }

    /** Phosphorous quota value getter */
    public double getQuotaP() {
        return P/C;
    }

    public void updateBiomass() {this.biomass = C * CARBON_TO_DRY_WEIGHT_RATIO;}
    
}
