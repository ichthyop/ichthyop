package org.previmer.ichthyop.evol;

/**
 *
 * @author mariem
 */
public class EnvironmentalEvol extends AbstractEvol{
    private boolean var_temp, var_sal;
    private float margin_temp, margin_sal;
    
    @Override
     public void loadParameters() throws Exception {
        try {
            /* load common parameters*/
            super.loadParameters();

            /* load Natal Homing parameters */
            var_temp= Boolean.valueOf(getParameter("var_temp"));
            var_sal= Boolean.valueOf(getParameter("var_sal"));
            margin_temp= Float.valueOf(getParameter("margin_loc"));
            margin_sal= Float.valueOf(getParameter("margin_bat"));

        } catch (Exception ex) {

            getLogger().info("Failed to read the natal parameters.");
        }
     }

    /**
     * @return the var_temp
     */
    public boolean temperatureCriterionEnv() {
        return var_temp;
    }

    /**
     * @return the var_sal
     */
    public boolean salinityCriterionEnv() {
        return var_sal;
    }

    /**
     * @return the margin_temp
     */
    public float getMarginTempEnv() {
        return margin_temp;
    }

    /**
     * @return the margin_sal
     */
    public float getMarginSalEnv() {
        return margin_sal;
    }

}
