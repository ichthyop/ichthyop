package org.previmer.ichthyop.evol;
/**
 *
 * @author mariem
 */
public class OpportunismEvol extends AbstractEvol{
    private boolean temperature, salinity;
    private float margin_temp_opp, margin_sal_opp, optimal_sal, optimal_temp;

    @Override
     public void loadParameters() throws Exception {
        try {
            /* load common parameters*/
            super.loadParameters();

            /* load Natal Homing parameters */
            temperature= Boolean.valueOf(getParameter("var_temp"));
            salinity= Boolean.valueOf(getParameter("var_sal"));
            margin_temp_opp= Float.valueOf(getParameter("margin_loc"));
            margin_sal_opp= Float.valueOf(getParameter("margin_bat"));
            optimal_sal= Float.valueOf(getParameter("optimal_sal"));
            optimal_temp= Float.valueOf(getParameter("optimal_temp"));

        } catch (Exception ex) {

            getLogger().info("Failed to read the natal parameters.");
        }
     }

    /**
     * @return if the temperature criterion is activated
     */
    public boolean temperatureCriterion() {
        return temperature;
    }

    /**
     * @return if the salinity criterion is activated
     */
    public boolean salinityCriterion() {
        return salinity;
    }

    /**
     * @return the margin_temp_opp
     */
    public float getMarginTemperatureOpp() {
        return margin_temp_opp;
    }

    /**
     * @return the margin_sal_opp
     */
    public float getMarginSalinityOpp() {
        return margin_sal_opp;
    }

    /**
     * @return the optimal_sal
     */
    public float getOptimalSal() {
        return optimal_sal;
    }

    /**
     * @return the optimal_temp
     */
    public float getOptimalTemp() {
        return optimal_temp;
    }

}
