package org.previmer.ichthyop.evol;

/**
 *
 * @author mariem
 */
public class NatalEvol extends AbstractEvol {

    private double margin_loc, margin_bat, margin_time;

    @Override
    public void loadParameters() throws Exception {
        try {
            /* load common parameters*/
            super.loadParameters();

            /* load Natal Homing parameters */
            margin_loc = Double.valueOf(getParameter("margin_loc"));
            margin_bat = Double.valueOf(getParameter("margin_bat"));
            margin_time = Double.valueOf(getParameter("margin_time"));
        
        } catch (Exception ex) {

            getLogger().info("Failed to read the natal parameters.");
        }
    }

    /**
     * @return the margin_loc
     */
    public double getMarginLoc() {
        return margin_loc;
    }

    /**
     * @return the margin_bat
     */
    public double getMarginBat() {
        return margin_bat;
    }

    /**
     * @return the margin_time
     */
    public double getMarginTime() {
        return margin_time;
    }
}
