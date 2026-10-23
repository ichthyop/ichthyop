package org.previmer.ichthyop.action.orientation;

import java.util.ArrayList;
import java.util.logging.Level;

import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.Zone;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.util.VonMisesRandom;

public class ReefOrientationAction extends OrientationVelocity {

    private double maximumDistance;

    public static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;

    private int nZones;
    private double longitudeReefs[][];
    private double latitudeReefs[][];
    private double xReefs[][];
    private double yReefs[][];

    private double longitudeBarycenter[];
    private double latitudeBarycenter[];
    private double xBarycenter[];
    private double yBarycenter[];

    private String mode;

    private double kappaBarycenter[];

    // Coast avoidance: if the swimming step ends on land, the heading is
    // rotated (by steps of coastAngleStep, up to +/- coastMaxAngle) and the wet
    // heading that brings the particle the closest to the target is kept.
    private boolean coastAvoidance;
    private double coastAngleStep;
    private double coastMaxAngle;

    ArrayList<Zone> zones;
    private boolean isInitialized = false;

    @FunctionalInterface
    private interface GetReefDistance {
        public double[] getDistance(IParticle particle);
    }

    @FunctionalInterface
    private interface GetClosestPoint {
        public double[] getClosestPoint(double[] xyParticleCoordinates, int closest_reef_index);
    }

    private GetReefDistance getReefDistance;
    private GetClosestPoint getClosestPoint;

    double dt;

    @Override
    public void loadParameters() throws Exception {

        super.loadParameters();

        maximumDistance = Double.valueOf(getParameter("maximum.distance"));

        // Provides age in days
        if (!isNull("attraction.mode")) {
            mode = getParameter("attraction.mode");
        } else {
            mode = "edges";
        }

        if (mode.equals("barycenter")) {
            getReefDistance = (IParticle particle) -> computeReefDistanceBarycenter(particle);
            getClosestPoint = (double[] xyCoords, int k) -> this.findClosestPointPolygonBarycenter(xyCoords, k);
        } else {
            getReefDistance = (IParticle particle) -> computeReefDistanceEdges(particle);
            getClosestPoint = (double[] xyCoords, int k) -> this.findClosestPointPolygonEdges(xyCoords, k);
        }

        // Load the target areas, i.e. the zones in which the target areas will be
        // defined:
        getSimulationManager().getZoneManager().loadZonesFromFile(getParameter("zone_file"), TypeZone.TARGET);
        zones = getSimulationManager().getZoneManager().getZones(TypeZone.TARGET);
        if (zones == null || zones.size() == 0) {
            String message = String.format("No target zones defined in %s", getParameter("zone_file"));
            getLogger().log(Level.SEVERE, message);
        }

        dt = getSimulationManager().getTimeManager().get_dt();

        coastAvoidance = !isNull("coast.avoidance.enabled") && Boolean.valueOf(getParameter("coast.avoidance.enabled"));
        coastAngleStep = isNull("coast.avoidance.angle.step") ? 10.d : Double.valueOf(getParameter("coast.avoidance.angle.step"));
        coastMaxAngle = isNull("coast.avoidance.angle.max") ? 90.d : Double.valueOf(getParameter("coast.avoidance.angle.max"));
        coastAngleStep = Math.toRadians(coastAngleStep);
        coastMaxAngle = Math.toRadians(coastMaxAngle);

    }

    /**
     * @brief Initializes the target zones.
     */
    private void initializeTargets() {

        nZones = zones.size();

        // Longitude/latitude points of each zones
        longitudeReefs = new double[nZones][];
        latitudeReefs = new double[nZones][];

        // X and Y coordinates of each zone
        xReefs = new double[nZones][];
        yReefs = new double[nZones][];

        // Longitude/latitude of each barycenter
        longitudeBarycenter = new double[nZones];
        latitudeBarycenter = new double[nZones];

        // X and Y coordinates of each barycenter
        xBarycenter = new double[nZones];
        yBarycenter = new double[nZones];
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

            // Initializes the coordinates of the polygons
            longitudeReefs[iZone] = new double[nPol + 1];
            latitudeReefs[iZone] = new double[nPol + 1];
            xReefs[iZone] = new double[nPol + 1];  // indexes range from 0 to nPol
            yReefs[iZone] = new double[nPol + 1];

            // if polygon has 4 points, we set the first 4 points
            for (int i = 0; i < nPol; i++) {
                longitudeReefs[iZone][i] = lon.get(i);
                latitudeReefs[iZone][i] = lat.get(i);

                // compute the barycenter
                longitudeBarycenter[iZone] += longitudeReefs[iZone][i];
                latitudeBarycenter[iZone] += latitudeReefs[iZone][i];
            }

            // normalize the barycenter position
            longitudeBarycenter[iZone] /= nPol;
            latitudeBarycenter[iZone] /= nPol;

            // Add the last point
            longitudeReefs[iZone][nPol] = lon.get(0);
            latitudeReefs[iZone][nPol] = lat.get(0);

            kappaBarycenter[iZone] = zoneTemp.getKappa();

            // Computation of x and y coordinates of the reefs
            for (int i = 0; i < nPol; i++) {
                double xy[] = getSimulationManager().getDataset().latlon2xy(latitudeReefs[iZone][i],
                        longitudeReefs[iZone][i]);
                xReefs[iZone][i] = xy[0];
                yReefs[iZone][i] = xy[1];

                // computation of the reef barycenters
                xBarycenter[iZone] += xReefs[iZone][i];
                yBarycenter[iZone] += yReefs[iZone][i];
            }

            xBarycenter[iZone] /= nPol;
            yBarycenter[iZone] /= nPol;

            // Add the last point to the X and Y coordinates
            xReefs[iZone][nPol] = xReefs[iZone][0];
            yReefs[iZone][nPol] = yReefs[iZone][0];

        }
    }

    @Override
    public void execute(IParticle particle) {

        if(!this.isActive(particle)) {
            return;
        }

        double[] mvt = getDlonDlat(particle);
        double newLon = particle.getLon() + mvt[0];
        double newLat = particle.getLat() + mvt[1];
        double[] newPos = getSimulationManager().getDataset().latlon2xy(newLat, newLon);
        double[] posIncr = new double[] { newPos[0] - particle.getX(), newPos[1] - particle.getY() };
        particle.increment(posIncr);

    }

    public double[] getDlonDlat(IParticle particle) {

        double uorient = 0;
        double vorient = 0;

        // Computes the distance to all the target zones (either barycenter or edge point)
        double[] distance = this.getReefDistance.getDistance(particle);

        // computes the index of the closest reef
        int closestReefIndex = this.findSmallestDistance(distance);

        // extract the computes the distance of the closest reef.
        double closestReefDistance = distance[closestReefIndex];

        if (closestReefDistance <= maximumDistance) {

            // if the distance from closest point is below the detection distance, compute orientation

            // computes the xy coordinate of the particle
            double[] xyParticleCoordinates = new double[] { particle.getX(), particle.getY() };

            // extract the XY coordinates of the target point
            double[] closestPoint = this.getClosestPoint.getClosestPoint(xyParticleCoordinates, closestReefIndex);

            double thetaPref, thetaCurrent;

            double d = 1 - (closestReefDistance / maximumDistance);

            double Kappa_reef = kappaBarycenter[closestReefIndex];

            // extracts the xy coordinates of the actual position
            double xyParticule[] = getSimulationManager().getDataset().latlon2xy(particle.getLat(), particle.getLon());

            // extracts the coordinates of the previous position (to have the trajectory)
            double xyOrigin[] = getSimulationManager().getDataset().latlon2xy(particle.getOldLat(),
                    particle.getOldLon());

            double xyReef[] = { closestPoint[0], closestPoint[1] };

            // computes the destination angle
            thetaPref = Math.atan2(xyReef[1] - xyParticule[1], xyReef[0] - xyParticule[0]);
            if(thetaPref < 0) {
                thetaPref += 2 * Math.PI;
            }

            // computes the actual trajectory angle
            thetaCurrent = Math.atan2(xyOrigin[1] - xyParticule[1], xyOrigin[0] - xyParticule[0]) + Math.PI;

            if(thetaCurrent < 0) {
                thetaCurrent += 2 * Math.PI;
            }

            VonMisesRandom vonMises = new VonMisesRandom(0, Kappa_reef, getSimulationManager().getTimeManager().index(), particle.getIndex());
            double ti = vonMises.nextDouble();

            // wraps the angle difference in [-pi, pi] to turn the shortest way
            double deltaTheta = thetaPref - thetaCurrent;
            deltaTheta = Math.atan2(Math.sin(deltaTheta), Math.cos(deltaTheta));

            double turningAngle = d * deltaTheta + ti;

            double theta = thetaCurrent + turningAngle;
            double swimmingSpeed = getVelocity(particle);

            if (coastAvoidance) {
                theta = avoidCoast(particle, theta, swimmingSpeed, closestReefIndex);
            }

            uorient = swimmingSpeed * Math.cos(theta);
            vorient = swimmingSpeed * Math.sin(theta);

        }

        return computeDlonDlat(particle.getLat(), uorient, vorient);

    }

    /** Converts a velocity (m/s) into a lon/lat displacement over one time step. */
    private double[] computeDlonDlat(double lat, double u, double v) {

        double dx = u * dt;
        double dy = v * dt;

        double one_deg_lon_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * lat / 180.d);
        double dLon = dx / one_deg_lon_meter;
        double dLat = dy / ONE_DEG_LATITUDE_IN_METER;

        return new double[] { dLon, dLat };

    }

    /**
     * Returns the heading to use so that the particle does not swim on land. If
     * the step along theta ends in water, theta is returned. Else, headings
     * theta +/- k * coastAngleStep (up to coastMaxAngle) are tested and, among
     * the ones that end in water, the one that brings the particle the closest to
     * the target reef is returned. Returns NaN if no wet heading is found.
     */
    private double avoidCoast(IParticle particle, double theta, double swimmingSpeed, int reefIndex) {

        if (isWetDestination(particle, theta, swimmingSpeed)) {
            return theta;
        }

        double bestTheta = Double.NaN;
        double bestDistance = Double.MAX_VALUE;
        int nSteps = (int) Math.floor(coastMaxAngle / coastAngleStep);
        for (int k = 1; k <= nSteps; k++) {
            for (int sign = -1; sign <= 1; sign += 2) {
                double thetaTest = theta + sign * k * coastAngleStep;
                if (!isWetDestination(particle, thetaTest, swimmingSpeed)) {
                    continue;
                }
                double[] latlon = getDestination(particle, thetaTest, swimmingSpeed);
                double distance = computeReefDistance(latlon[0], latlon[1], reefIndex);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestTheta = thetaTest;
                }
            }
        }

        // If no best theta is found, then return original value and particle
        // will be onland
        if (Double.isNaN(bestTheta)) {
            return theta;
        } else {
            return bestTheta;
        }

    }

    /** Returns the lat/lon of the particle after one swimming step along theta. */
    private double[] getDestination(IParticle particle, double theta, double swimmingSpeed) {
        double[] mvt = computeDlonDlat(particle.getLat(), swimmingSpeed * Math.cos(theta),
                swimmingSpeed * Math.sin(theta));
        return new double[] { particle.getLat() + mvt[1], particle.getLon() + mvt[0] };
    }

    /** Checks whether one swimming step along theta ends in water (surface mask). */
    private boolean isWetDestination(IParticle particle, double theta, double swimmingSpeed) {
        double[] latlon = getDestination(particle, theta, swimmingSpeed);
        if (!getSimulationManager().getDataset().isInDomain(latlon[1], latlon[0])) {
            return false;
        }
        double[] xy = getSimulationManager().getDataset().latlon2xy(latlon[0], latlon[1]);
        return getSimulationManager().getDataset().isInWater(new double[] { xy[0], xy[1] });
    }

    /** Distance between a lat/lon position and the target point (edge or barycenter) of a reef. */
    private double computeReefDistance(double lat, double lon, int reefIndex) {
        double[] xy = getSimulationManager().getDataset().latlon2xy(lat, lon);
        double[] closestPoint = this.getClosestPoint.getClosestPoint(xy, reefIndex);
        double[] closestLatLon = getSimulationManager().getDataset().xy2latlon(closestPoint[0], closestPoint[1]);
        return getSimulationManager().getDataset().getDistGetter().getDistance(lat, lon, closestLatLon[0],
                closestLatLon[1]);
    }

    @Override
    public void init(IParticle particle) {
        if (isInitialized == false) {
            initializeTargets();
            isInitialized = true;
        }
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

    // Computes the distance to reef, considering the closest point to the particle.
    // Can be on edge.
    public double[] computeReefDistanceEdges(IParticle particle) {

        int NReefs = longitudeReefs.length;
        double[] distance = new double[NReefs];

        double lonParticle = particle.getLon();
        double latParticle = particle.getLat();
        double[] xyParticleCoordinates = new double[] { particle.getX(), particle.getY() };

        for (int k = 0; k < NReefs; k++) {

            // Find the closest point on the polygon (either on the edge or at a corner)
            double[] closestPointCoordinates = findClosestPointPolygonEdges(xyParticleCoordinates, k);

            // extracts the lon/lat of the closest point
            double[] closestPointLatLon = getSimulationManager().getDataset().xy2latlon(closestPointCoordinates[0], closestPointCoordinates[1]);

            // extract the distance, in km, from the closest point.
            distance[k] = getSimulationManager().getDataset().getDistGetter().getDistance(latParticle, lonParticle,
                    closestPointLatLon[0], closestPointLatLon[1]);
        }

        return distance;

    }

    // Computes the distance to reef, considering the closest point to the particle.
    // Can be on edge.
    public double[] computeReefDistanceBarycenter(IParticle particle) {

        int NReefs = longitudeReefs.length;
        double[] distance = new double[NReefs];

        double lonParticle = particle.getLon();
        double latParticle = particle.getLat();

        for (int k = 0; k < NReefs; k++) {

            double lonTemp = longitudeBarycenter[k];
            double latTemp = latitudeBarycenter[k];

            distance[k] = getSimulationManager().getDataset().getDistGetter().getDistance(latParticle, lonParticle,
                    latTemp, lonTemp);
        }

        return distance;

    }

    /**
     * Computation from a given location of the closest point to a polygon (either
     * edge or edge). Computation if performed based on particle grid coordinates.
     * It returns the x and y grid coordinates for the given point
     */
    public double[] findClosestPointPolygonEdges(double[] xyCoords, int k) {

        double minDistance = Double.MAX_VALUE;
        double[] final_closest = new double[2];

        int nEdges = xReefs[k].length - 1;

        for (int i = 0; i < nEdges; i++) {

            double[] start = new double[] { xReefs[k][i], yReefs[k][i] };
            double[] end = new double[] { xReefs[k][i + 1], yReefs[k][i + 1] };

            double[] temp_closest = closestPoint(start, end, xyCoords);

            double dist = Math
                    .sqrt(Math.pow(temp_closest[0] - xyCoords[0], 2) + Math.pow(temp_closest[1] - xyCoords[1], 2));

            if (dist <= minDistance) {
                final_closest = temp_closest;
                minDistance = dist;
            }
        }

        return final_closest;

    }

    /**
     * Computation from a given location of the closest point to a polygon (either
     * edge or edge). Computation if performed based on particle grid coordinates.
     * It returns the x and y grid coordinates for the given point
     */
    public double[] findClosestPointPolygonBarycenter(double[] xyCoords, int k) {

        double[] final_closest = new double[2];
        final_closest[0] = xBarycenter[k];
        final_closest[1] = yBarycenter[k];
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

    public void addPolygonForTest(double[] xp, double[] yp) {

        longitudeReefs = new double[1][];
        latitudeReefs = new double[1][];
        xReefs = new double[1][];
        yReefs = new double[1][];
        xReefs[0] = xp;
        yReefs[0] = yp;

    }

}
