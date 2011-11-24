package org.previmer.ichthyop.evol;
/**
 *
 * @author mariem
 */
public class OpportunismEvol extends AbstractEvol{

    @Override
     public void loadParameters() throws Exception {
        try {
            boolean temperature, salinity;
            float margin_temp_opp, margin_sal_opp, optimal_sal, optimal_temp;

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

}
