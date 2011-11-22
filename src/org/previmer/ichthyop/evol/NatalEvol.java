package org.previmer.ichthyop.evol;
/**
 *
 * @author mariem
 */
public class NatalEvol extends AbstractEvol {

    @Override
     public void loadParameters() throws Exception {
        try {
            double margin_loc, margin_bat;

            /* load common parameters*/
            super.loadParameters();

            /* load Natal Homing parameters */
            margin_loc= Double.valueOf("margin_loc");
            margin_bat= Double.valueOf("margin_bat");
        } catch (Exception ex) {
           
            getLogger().info("Failed to read the natal parameters.");
        }
     }



}
