package org.previmer.ichthyop.evol;

import org.previmer.ichthyop.SimulationManagerAccessor;
/**
 *
 * @author mariem
 */
public class Stray extends SimulationManagerAccessor{

     public void loadParameters() throws Exception {
        try {
            boolean temp_str, sal_str;
            float rate_str, temp_min_str, temp_max_str, sal_min_str, sal_max_str;

            /* load Natal Homing parameters */
            temp_str= Boolean.valueOf("temp_str");
            sal_str= Boolean.valueOf("sal_str");
            rate_str= Float.valueOf("rate_stray");
            temp_min_str= Float.valueOf("temp_min_str");
            temp_max_str= Float.valueOf("temp_max_str");
            sal_min_str= Float.valueOf("sal_min_str");
            sal_max_str= Float.valueOf("sal_max_str");

        } catch (Exception ex) {

            getLogger().info("Failed to read the natal parameters.");
        }
     }

}
