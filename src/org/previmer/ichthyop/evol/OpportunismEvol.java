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
            temperature= Boolean.valueOf("var_temp");
            salinity= Boolean.valueOf("var_sal");
            margin_temp_opp= Float.valueOf("margin_loc");
            margin_sal_opp= Float.valueOf("margin_bat");
            optimal_sal= Float.valueOf("optimal_sal");
            optimal_temp= Float.valueOf("optimal_temp");

        } catch (Exception ex) {

            getLogger().info("Failed to read the natal parameters.");
        }
     }

}
