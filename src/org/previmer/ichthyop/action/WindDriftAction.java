/* <p>Copyright: Copyright (c) 2007-2011. Free software under GNU GPL</p>
 * 
 * @author G.Andres
 */

package org.previmer.ichthyop.action;


import org.previmer.ichthyop.arch.IBasicParticle;

public class WindDriftAction extends AbstractAction {
    static double wind_factor;
    public static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strUW, strVW, strTime;
    
    /**
     * Depth of wind drift application 
     */
    static float depth_application;
    
    /**
     * Angle deviation imposed by wind 
     */
    static double angle;
    
    @Override
    public void loadParameters() throws Exception {
        System.out.println("*****************LOAD PARAMMMMMM");
        wind_factor=Double.valueOf(getParameter("wind_factor"));
        strUW=getParameter("wind_u");
        strVW=getParameter("wind_v");
        
        depth_application = Float.valueOf(getParameter("depth_application"));
        angle = Double.valueOf(getParameter("angle"))*Math.PI/180.0;
        getSimulationManager().getDataset().requireVariable(strUW, getClass());
        getSimulationManager().getDataset().requireVariable(strVW, getClass());
    }

    
    
    

    @Override
    public void execute(IBasicParticle particle) {
        System.out.println("***** dz : " + particle.getZ());
        if (particle.getZ() >= 0) {
            double depth = getSimulationManager().getDataset().z2depth(particle.getX(), particle.getY(), particle.getZ());
            System.out.println("depth " + depth);
            if (- depth > depth_application) {
                return;
            }
        }
        
        double[] mvt = getDLonLat(particle.getGridCoordinates(),getSimulationManager().getTimeManager().getTime(),getSimulationManager().getTimeManager().get_dt());
        double newLon = particle.getLon() + mvt[0];
        double newLat = particle.getLat() + mvt[1];
        double[] newPos = getSimulationManager().getDataset().latlon2xy(newLat, newLon);
        double[] windincr = new double[]{newPos[0] - particle.getX(), newPos[1] - particle.getY()};
        particle.increment(windincr);
        
    }
   
    
    private double[] getDLonLat(double[] pgrid, double time, double dt){
        double[] dWi = new double[2];
        double dx,dy;
        double[] latlon = getSimulationManager().getDataset().xy2latlon(pgrid[0], pgrid[1]);
        double one_deg_lon_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * latlon[0] / 180.d);
        dx = dt*getSimulationManager().getDataset().get(strUW, pgrid, time).doubleValue()/one_deg_lon_meter;
        dy = dt*getSimulationManager().getDataset().get(strVW, pgrid, time).doubleValue()/ ONE_DEG_LATITUDE_IN_METER;
        dWi[0] = wind_factor*(dx*Math.cos(angle)-dy*Math.sin(angle));
        dWi[1] = wind_factor*(dx*Math.sin(angle)+dy*Math.cos(angle));
        return dWi;
    }

    
}
