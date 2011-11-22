package org.previmer.ichthyop.evol;

/**
 *
 * @author mariem
 */
public class EnvironmentalEvol extends AbstractEvol{

    @Override
     public void loadParameters() throws Exception {
        try {
            boolean var_temp, var_sal;
            float margin_temp, margin_sal;

            /* load common parameters*/
            super.loadParameters();

            /* load Natal Homing parameters */
            var_temp= Boolean.valueOf("var_temp");
            var_sal= Boolean.valueOf("var_sal");
            margin_temp= Float.valueOf("margin_loc");
            margin_sal= Float.valueOf("margin_bat");

        } catch (Exception ex) {

            getLogger().info("Failed to read the natal parameters.");
        }
     }

}
