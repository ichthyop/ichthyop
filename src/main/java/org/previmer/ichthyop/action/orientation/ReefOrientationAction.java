package org.previmer.ichthyop.action.orientation;

import java.util.Random;

import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.particle.IParticle;

public class ReefOrientationAction extends AbstractAction {

  private double maximumDistance;
  private double horDiffOrient;
  private double dtturb;
  private double swimmingSpeedHatch;
  private double swimmingSpeedSettle;

  private double secs_in_day = 86400;

  private Random randomGenerator = new Random();
  double dt;

  @Override
  public void loadParameters() throws Exception {

    maximumDistance = Double.valueOf(getParameter("maximum.distance"));
    swimmingSpeedHatch = Double.valueOf(getParameter("swimming.speed.hatch"));
    swimmingSpeedSettle = Double.valueOf(getParameter("swimming.speed.settle"));

    dt = getSimulationManager().getTimeManager().get_dt();
  }

  @Override
  public void execute(IParticle particle) {

    double uorient, vorient;
    int N = 5;
    double[] latBarycenter = new double[N];
    double[] lonBarycenter = new double[N];
    double[] kappaBarycenter = new double[N];

    double[] distance = this.computeReefDistance(particle, lonBarycenter, latBarycenter);

    int closestReefIndex = this.findSmallestDistance(distance);
    double closestReefDistance = distance[closestReefIndex];

    if (closestReefDistance > maximumDistance) {

      double htvelscl = Math.sqrt((2 * horDiffOrient) / dtturb);
      double norm1 = randomGenerator.nextGaussian();
      double norm2 = randomGenerator.nextGaussian();

      uorient = norm1 * htvelscl;
      vorient = norm2 * htvelscl;

    } else {

      uorient = 0;
      vorient = 0;
      double d = 1 - (closestReefDistance / maximumDistance);
      double thetaPref = -haverSine(particle.getLon(), particle.getLat(), lonBarycenter[closestReefIndex],
          latBarycenter[closestReefIndex]);

      double Kappa_reef = kappaBarycenter[closestReefIndex];

      double thetaCurrent = haverSine(particle.getOldLon(), particle.getOldLat(), particle.getLon(), particle.getLat());
      double mu = -d * (thetaCurrent - thetaPref);

      double ti = randomVonMisesJava(0, Kappa_reef);
      double theta = ti - thetaCurrent - mu;

      double age = particle.getAge() / (secs_in_day);
      double timeMax = getSimulationManager().getTimeManager().getSimulationDuration() / (secs_in_day);
      double PLD = timeMax / (secs_in_day);
      double swimmingSpeed = swimmingSpeedHatch
          + Math.pow(10, ((Math.log10(age) / Math.log10(PLD)) * Math.log10(swimmingSpeedSettle - swimmingSpeedHatch)));
      swimmingSpeed = swimmingSpeed / 100;

      // Compute u and v orientation velocity;
      uorient = swimmingSpeed * Math.cos(theta);
      vorient = swimmingSpeed * Math.sin(theta);

    }

    double dx = uorient * dt;
    double dy = vorient * dt;
    particle.increment(new double[] { dx, dy });

  }

  /** Another algorithm for the computation of VM Distributions.
   * Source: https://github.com/robbymckilliam/Distributions/blob/master/src/org/mckilliam/distributions/circular/VonMises.java */
  public double randomVonMisesJava(double mu, double kappa) {

    double tau = 1 + Math.sqrt(1 + 4 * kappa * kappa);
    double rau = (tau - Math.sqrt(2 * tau)) / (2 * kappa);
    double r = (1 + rau * rau) / (2 * rau);

    while (true) {

      double z = Math.cos(Math.PI * randomGenerator.nextDouble());
      double f = (1 + r * z) / (r + z);
      double c = kappa * (r - f);

      double U2 = randomGenerator.nextDouble();
      if (c * (2 - c) - U2 > 0 || Math.log(c / U2) + 1 - c > 0)
        return mu + Math.signum(randomGenerator.nextDouble() - 0.5) * Math.acos(f); // / 2 / Math.PI;

    }

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
