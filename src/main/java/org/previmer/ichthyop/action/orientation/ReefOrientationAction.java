package org.previmer.ichthyop.action.orientation;

import java.util.ArrayList;
import java.util.logging.Level;
import java.awt.geom.Line2D;

import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.Zone;
import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.util.VonMisesRandom;

public class ReefOrientationAction extends AbstractAction {

    private double maximumDistance;
    private double swimmingSpeedHatch;
    private double swimmingSpeedSettle;
    public static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;

    private int nZones;
    private double lonBarycenter[][];
    private double latBarycenter[][];
    private double xBarycenter[][];
    private double yBarycenter[][];
    private double kappaBarycenter[];
    ArrayList<Zone>zones;
    double PLD;

    private double secs_in_day = 86400;

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

        if (swimmingSpeedHatch > swimmingSpeedSettle) {
            getLogger().log(Level.WARNING, "Hatch and Settle velocity have been swapped");
            double temp = swimmingSpeedHatch;
            swimmingSpeedHatch = swimmingSpeedSettle;
            swimmingSpeedSettle = temp;
        }

    }

    private void initializeTargets() {

        nZones = zones.size();
        lonBarycenter = new double[nZones][];
        latBarycenter = new double[nZones][];
        xBarycenter = new double[nZones][];
        yBarycenter = new double[nZones][];
        kappaBarycenter = new double[nZones];

        for (int iZone = 0; iZone < nZones; iZone++) {
            Zone zoneTemp = zones.get(iZone);

            // Compute the barycenter of the zone
            ArrayList<Float> lon = zoneTemp.getLon();
            ArrayList<Float> lat = zoneTemp.getLat();

            // Got the number of points
            int nPol = lon.size();

            // If the polygon is closed, remove the last point
            if ((lon.get(0) == lon.get(nPol - 1)) && (lat.get(0) == lat.get(nPol - 1))) {
                nPol--;
            }

            if (nPol == 0) {
                String message = String.format("No Polygon defined in %s", zoneTemp.getKey());
                getLogger().log(Level.SEVERE, message);
            }

            lonBarycenter[iZone] = new double[nPol + 1];
            latBarycenter[iZone] = new double[nPol + 1];
            xBarycenter[iZone] = new double[nPol + 1];
            yBarycenter[iZone] = new double[nPol + 1];

            // if polygon has 4 points, we set the first 4 points
            for (int i = 0; i < nPol; i++) {
                lonBarycenter[iZone][i] += lon.get(i);
                latBarycenter[iZone][i] += lat.get(i);
            }

            // Add the last point
            lonBarycenter[iZone][nPol] += lon.get(0);
            latBarycenter[iZone][nPol] += lat.get(0);

            kappaBarycenter[iZone] = zoneTemp.getKappa();

            for (int i = 0; i < nPol; i++) {

                double xy[] = getSimulationManager().getDataset().latlon2xy(latBarycenter[iZone][i],
                        lonBarycenter[iZone][i]);
                xBarycenter[iZone][i] = xy[0];
                yBarycenter[iZone][i] = xy[1];

            }
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

        uorient = 0;
        vorient = 0;

        if (closestReefDistance <= maximumDistance) {

            double thetaPref, thetaCurrent;

            double d = 1 - (closestReefDistance / maximumDistance);

            // thetaPref = haverSine(particle.getLon(), particle.getLat(), lonBarycenter[closestReefIndex],
            //         latBarycenter[closestReefIndex]);
            // thetaCurrent = haverSine(particle.getOldLon(), particle.getOldLat(), particle.getLon(),
            //         particle.getLat());

            double Kappa_reef = kappaBarycenter[closestReefIndex];

            // double xyParticule[] = getSimulationManager().getDataset().latlon2xy(particle.getLat(), particle.getLon());
            // double xyOrigin[] = getSimulationManager().getDataset().latlon2xy(particle.getOldLat(), particle.getOldLon());
            // double xyReef[] = new double[] {xBarycenter[closestReefIndex], yBarycenter[closestReefIndex]};

            // thetaPref = Math.atan2(xyReef[1] - xyParticule[1], xyReef[0] - xyParticule[0]);
            // thetaCurrent = Math.atan2(xyOrigin[1] - xyParticule[1], xyOrigin[0] - xyParticule[0]) + Math.PI;
            thetaPref = 0;
            thetaCurrent = 0;

            double mu = d * (thetaPref - thetaCurrent);

            VonMisesRandom vonMises = new VonMisesRandom(0, Kappa_reef);

            double ti = vonMises.nextDouble();

            double theta = ti + mu + thetaCurrent;

            double age = particle.getAge() / (secs_in_day) + Float.MIN_VALUE;

            double swimmingSpeed = swimmingSpeedHatch + Math.pow(10, (Math.log10(age) / Math.log10(PLD)) * Math.log10(swimmingSpeedSettle - swimmingSpeedHatch));
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

    /** Haversine formula to compute angles from lat and lon.
     * Cf. https://www.movable-type.co.uk/scripts/latlong.html
     * Cf. https://copyprogramming.com/howto/javascript-find-degree-between-two-geo-coordinates-javascript
     *
     * TODO Check formula (inversion of X and Y)
     *
     * @param lonstart
     * @param latstart
     * @param lonend
     * @param latend
     * @return
     */
    private double haverSine(double lonstart, double latstart, double lonend, double latend) {

        double rlonstart = Math.toRadians(lonstart);
        double rlatstart = Math.toRadians(latstart);
        double rlonend = Math.toRadians(lonend);
        double rlatend = Math.toRadians(latend);
        double Y = Math.sin(rlonend - rlonstart) * Math.cos(rlatend);
        double X = Math.cos(rlatstart) * Math.sin(rlatend) - Math.sin(rlatstart) * Math.cos(rlatend) * Math.cos(rlonend - rlonstart);
        return Math.atan2(Y, X);

    }

    @Override
    public void init(IParticle particle) {

        initializeTargets();
        double timeMax = getSimulationManager().getTimeManager().getSimulationDuration();
        PLD = timeMax / (secs_in_day);

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

    public double[] computeReefDistance(IParticle particle, double[][] lonBarycenter, double[][] latBarycenter) {

        int NReefs = lonBarycenter.length;
        double[] distance = new double[NReefs];

        double lonParticle = particle.getLon();
        double latParticle = particle.getLat();

        // for (int k = 0; k < NReefs; k++) {
        //     distance[k] = getSimulationManager().getDataset().getDistGetter().getDistance(latParticle, lonParticle,
        //             latBarycenter[k], lonBarycenter[k]);
        // }

        return distance;

    }

    public double[] findClosestPointPolygon(double[] point, double[] xPolygon, double[] yPolygon) {

        double minDistance = Double.MAX_VALUE;
        double[] final_closest = new double[2];

        int nEdges = xPolygon.length - 1;

        for (int i = 0; i < nEdges; i++) {

            double[] start = new double[] { xPolygon[i], yPolygon[i] };
            double[] end = new double[] { xPolygon[i + 1], yPolygon[i + 1] };

            double[] temp_closest = closestPoint(start, end, point);

            double dist = Math.sqrt(Math.pow(temp_closest[0] - point[0], 2) + Math.pow(temp_closest[1] - point[1], 2));

            if (dist <= minDistance) {
                final_closest = temp_closest;
                minDistance = dist;
            }
        }

        return final_closest;

    }


    public double dotproduct(double[] lineStartA, double[] lineEndB) {
        return lineStartA[0] * lineEndB[0] + lineStartA[1] * lineEndB[1];
    }

    public double[] closestPoint(double[] lineStartA, double[] lineEndB, double[] thePoint) {

        // calculates displacement from point to start edge
        double[] w = new double[2];
        w[0] = thePoint[0] - lineStartA[0];
        w[1] = thePoint[1] - lineStartA[1];

        // computes the displacement between start and end of the line
        double[] tempLineEndB = new double[2];
        tempLineEndB[0] = lineEndB[0] - lineStartA[0];
        tempLineEndB[1] = lineEndB[1] - lineStartA[1];

        // calculates the projection of w onto the line
        double proj = dotproduct(w, tempLineEndB);
        double[] output = new double[2];

        // endpoint 0 is closest point
        if (proj <= 0.0) {
            output[0] = lineStartA[0];
            output[1] = lineStartA[1];
        } else {
            // square
            double vsq = dotproduct(tempLineEndB, tempLineEndB);
            // B^2
            proj /= vsq;
            // endpoint 1 is closest point
            if (proj >= 1) {
                output[0] = lineEndB[0];
                output[1] = lineEndB[1];
            } else {
                output[0] = lineStartA[0] + (tempLineEndB[0] * proj);
                output[1] = lineStartA[1] + (tempLineEndB[1] * proj);
            }
        }
        return output;
    }


}
