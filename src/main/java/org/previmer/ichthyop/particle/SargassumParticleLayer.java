package org.previmer.ichthyop.particle;

public class SargassumParticleLayer extends ParticleLayer  {
    
    private double biomass;
    private double C, N, P;
    private double NH4_env, NO3_env, P_env, T_env, I_env;
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

    /** Environmental NO2 value getter */
    public double getNO3_env() {
        return NO3_env;
    }

    /** Environmental NO3 Density setter */
    public void setNO3_env(double NO3_content) {
        this.NO3_env = NO3_content;
    }

    /** Environmental NH4 value getter */
    public double getNH4_env() {
        return NH4_env;
    }

    /** Environmental NH4 Density setter */
    public void setNH4_env(double NH4_content) {
        this.NH4_env = NH4_content;
    }


    /** Phosphor value getter */
    public double getP() {
        return P;
    }

    /** Phosphor Density setter */
    public void setP(double P_content) {
        this.P = P_content;
    }

    /** Environmental phosphor value getter */
    public double getP_env() {
        return P_env;
    }

    /** Environmental phosphor Density setter */
    public void setP_env(double P) {
        this.P_env = P;
    }



    /** Environmental temperature value getter */
    public double getT_env() {
        return T_env;
    }

    /** Environmental temperature Density setter */
    public void setT_env(double T) {
        this.T_env = T;
    }

    /** Environmental irradiance value getter */
    public double getI_env() {
        return I_env;
    }

    /** Environmental irradiance Density setter */
    public void setI_env(double I) {
        this.I_env = I;
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
