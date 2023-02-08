package org.previmer.ichthyop.particle;

public class SargassumParticleLayer extends ParticleLayer  {
    
    private double biomass;
    private double C, N, P;
    private double N_env, P_env, T_env, I_env, S_env, W_env;
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

    /** Environmental nitrogen value getter */
    public double getN_env() {
        return N_env;
    }

    /** Environmental nitrogen Density setter */
    public void setN_env(double N_content) {
        this.N_env = N_content;
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

    /** Environmental temperature setter */
    public void setT_env(double T) {
        this.T_env = T;
    }

    /** Environmental irradiance value getter */
    public double getI_env() {
        return I_env;
    }

    /** Environmental irradiance setter */
    public void setI_env(double I) {
        this.I_env = I;
    }

    /** Environmental wind value getter */
    public double getW_env() {
        return W_env;
    }

    /** Environmental wind setter */
    public void setW_env(double W) {
        this.W_env = W;
    }

    /** Environmental salinity getter */
    public double getS_env() {
        return S_env;
    }

    /** Environmental salinity setter */
    public void setS_env(double S) {
        this.S_env = S;
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
