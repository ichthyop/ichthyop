package org.previmer.ichthyop.evol;

import org.previmer.ichthyop.SimulationManagerAccessor;
/**
 *
 * @author mariem
 */
public class Stray extends SimulationManagerAccessor{
    
    private String evolKey;
    
    public Stray() {
        evolKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
    }
    
    public String getParameter(String key) {
        return getSimulationManager().getActionManager().getParameter(evolKey, key);
    }

     public void loadParameters() throws Exception {
        try {
            boolean temp_str, sal_str;
            float rate_str, temp_min_str, temp_max_str, sal_min_str, sal_max_str;

            /* load Natal Homing parameters */
            temp_str= Boolean.valueOf(getParameter("temp_str"));
            sal_str= Boolean.valueOf(getParameter("sal_str"));
            rate_str= Float.valueOf(getParameter("rate_stray"));
            temp_min_str= Float.valueOf(getParameter("temp_min_str"));
            temp_max_str= Float.valueOf(getParameter("temp_max_str"));
            sal_min_str= Float.valueOf(getParameter("sal_min_str"));
            sal_max_str= Float.valueOf(getParameter("sal_max_str"));

        } catch (Exception ex) {

            getLogger().info("Failed to read stray's parameters.");
        }
     }
     
}
