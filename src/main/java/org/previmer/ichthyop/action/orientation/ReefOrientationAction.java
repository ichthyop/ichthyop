package org.previmer.ichthyop.action.orientation;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.Zone;
import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.util.VonMisesRandom;

public class ReefOrientationAction extends AbstractAction {

    private double maximumDistance;
    private double horDiffOrient;
    private double dtturb;
    private double swimmingSpeedHatch;
    private double swimmingSpeedSettle;
    public static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;

    private int nZones;
    private double lonBarycenter[];
    private double latBarycenter[];
    private double kappaBarycenter[];
    ArrayList<Zone>zones;

    private double secs_in_day = 86400;

    private Random randomGenerator = new Random();
    double dt;

    @Override
    public void loadParameters() throws Exception {

        maximumDistance = Double.valueOf(getParameter("maximum.distance"));
        swimmingSpeedHatch = Double.valueOf(getParameter("swimming.speed.hatch"));
        swimmingSpeedSettle = Double.valueOf(getParameter("swimming.speed.settle"));

        // Load the target areas, i.e. the zones in which the target areas will be
        // defined:
        getSimulationManager().getZoneManager().loadZonesFromFile(getParameter("zone_file"), TypeZone.TARGET);
        zones = getSimulationManager().getZoneManager().getZones(TypeZone.TARGET);
        if (zones == null || zones.size() == 0) {
            String message = String.format("No target zones defined in %s", getParameter("target_file"));
            getLogger().log(Level.SEVERE, message);
        }

        dt = getSimulationManager().getTimeManager().get_dt();

    }

    private void initializeTargets() {

        nZones = zones.size();
        lonBarycenter = new double[nZones];
        latBarycenter = new double[nZones];
        kappaBarycenter = new double[nZones];

        for (int iZone = 0; iZone < nZones; iZone++) {
            Zone zoneTemp = zones.get(iZone);

            // Compute the barycenter of the zone
            ArrayList<Float> lon = zoneTemp.getLon();
            ArrayList<Float> lat = zoneTemp.getLat();
            int nPol = lon.size();
            if (nPol == 0) {
                String message = String.format("No Polygon defined in %s", zoneTemp.getKey());
                getLogger().log(Level.SEVERE, message);
            }
            for(int i = 0; i < nPol - 1; i++) {
                lonBarycenter[iZone] += lon.get(i);
                latBarycenter[iZone] += lat.get(i);
            }
            lonBarycenter[iZone] /= (nPol - 1);
            latBarycenter[iZone] /= (nPol - 1);
            kappaBarycenter[iZone] = zoneTemp.getKappa();
        }

    }

    @Override
    public void execute(IParticle particle) {

        double[] mvt = getDlonDlat(particle);
        double newLon = particle.getLon() + mvt[0];
        double newLat = particle.getLat() + mvt[1];
        double[] newPos = getSimulationManager().getDataset().latlon2xy(newLat, newLon);
        double[] posIncr = new double[] { newPos[0] - particle.getX(), newPos[1] - particle.getY() };
        particle.increment(posIncr);

    }

    public double[] getDlonDlat(IParticle particle) {

        double uorient, vorient;

        double[] distance = this.computeReefDistance(particle, lonBarycenter, latBarycenter);

        int closestReefIndex = this.findSmallestDistance(distance);
        double closestReefDistance = distance[closestReefIndex];

        if (closestReefDistance > maximumDistance) {

            // double htvelscl = Math.sqrt((2 * horDiffOrient) / dt);
            // double norm1 = randomGenerator.nextGaussian();
            // double norm2 = randomGenerator.nextGaussian();

            // uorient = norm1 * htvelscl;
            // vorient = norm2 * htvelscl;
            uorient = 0;
            vorient = 0;

        } else {

            uorient = 0;
            vorient = 0;

            double d = 1 - (closestReefDistance / maximumDistance);
            double thetaPref = -haverSine(particle.getLon(), particle.getLat(), lonBarycenter[closestReefIndex],
                    latBarycenter[closestReefIndex]);

            double Kappa_reef = kappaBarycenter[closestReefIndex];

            double thetaCurrent = haverSine(particle.getOldLon(), particle.getOldLat(), particle.getLon(),
                    particle.getLat());
            double mu = -d * (thetaCurrent - thetaPref);

            VonMisesRandom vonMises = new VonMisesRandom(0, Kappa_reef);

            double ti = vonMises.nextDouble();
            double theta = ti - thetaCurrent - mu;

            double age = particle.getAge() / (secs_in_day);
            double timeMax = getSimulationManager().getTimeManager().getSimulationDuration() / (secs_in_day);
            double PLD = timeMax / (secs_in_day);
            // double swimmingSpeed = swimmingSpeedHatch + Math.pow(10,
            //         ((Math.log10(age) / Math.log10(PLD)) * Math.log10(swimmingSpeedSettle - swimmingSpeedHatch)));
            double swimmingSpeed = swimmingSpeedHatch + Math.pow(10,
                    ((Math.log10(age) / Math.log10(PLD)) * (swimmingSpeedSettle - swimmingSpeedHatch)));
            swimmingSpeed = swimmingSpeed / 100;

            // Compute u and v orientation velocity;
            uorient = swimmingSpeed * Math.cos(theta);
            vorient = swimmingSpeed * Math.sin(theta);

        }

        double dx = uorient * dt;
        double dy = vorient * dt;

        double[] latlon = getSimulationManager().getDataset().xy2latlon(particle.getX(), particle.getY());
        double one_deg_lon_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * latlon[0] / 180.d);
        double dLon = dx / one_deg_lon_meter;
        double dLat = dy / ONE_DEG_LATITUDE_IN_METER;

        return new double[] { dLon, dLat };

    }

    /** Haversine formula to compute angles from lat and lon */
    private double haverSine(double lon1, double lat1, double lon2, double lat2) {

        double rlon1 = Math.toRadians(lon1);
        double rlat1 = Math.toRadians(lat1);
        double rlon2 = Math.toRadians(lon2);
        double rlat2 = Math.toRadians(lat2);
        double X = Math.cos(rlat2) * Math.sin(rlon2 - rlon1);
        double Y = Math.cos(rlat1) * Math.sin(rlat2) - Math.sin(rlat1) * Math.cos(rlat2) * Math.cos(rlon2 - rlon1);
        return Math.atan2(Y, X);

    }

    @Override
    public void init(IParticle particle) {

        initializeTargets();

    }

    public int findSmallestDistance(double[] distance) {

        int p = -1;
        double minDistance = Double.MAX_VALUE;
        for (int k = 0; k < distance.length; k++) {
            if (distance[k] <= minDistance) {
                p = k;
                minDistance = distance[k];
            }
        }

        return p;

    }

    public double[] computeReefDistance(IParticle particle, double[] lonBarycenter, double[] latBarycenter) {

        int NReefs = lonBarycenter.length;
        double[] distance = new double[NReefs];

        double lonParticle = particle.getLon();
        double latParticle = particle.getLat();

        for (int k = 0; k < NReefs; k++) {
            distance[k] = getSimulationManager().getDataset().getDistGetter().getDistance(latParticle, lonParticle,
                    latBarycenter[k], lonBarycenter[k]);
        }

        return distance;

    }

}
