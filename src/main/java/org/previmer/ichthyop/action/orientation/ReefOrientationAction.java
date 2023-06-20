package org.previmer.ichthyop.action.orientation;

import java.util.Random;

import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.particle.IParticle;

public class ReefOrientationAction extends AbstractAction {

  private double maximumDistance;
  private double horDiffOrient;
  private double dtturb;
  private int nk;
  private double[] p, theta;
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

    p = new double[20];
    theta = new double[21];

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
      double thetaPref = -Haversine(particle.getLon(), particle.getLat(), lonBarycenter[closestReefIndex],
          latBarycenter[closestReefIndex]);

      double Kappa_reef = kappaBarycenter[closestReefIndex];

      double thetaCurrent = Haversine(particle.getOldLon(), particle.getOldLat(), particle.getLon(), particle.getLat());
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


  /*
   * Algorithm VMD from: Dagpunar, J.S. (1990) `Sampling from the von Mises
   * distribution via a comparison of random numbers', J. of Appl. Statist., 17,
   * 165-168. Fortran 90 code by Alan Miller CSIRO Division of Mathematical &
   * Information Sciences Arguments: k (real) parameter of the von Mises
   * distribution. first (logical) set to .TRUE. the first time that the function
   * is called, or the first time with a new value for k. When first = .TRUE., the
   * function sets up starting values and may be very much slower.
   */

  private double randomVonMisesFortran(double k, boolean first) {

    int j, n;
    double pi = Math.PI;

    double sump, r, th = 0, lambda = 0, rlast = 0;
    double dk;
    double fn_val = 0;

    double zero = 0;
    double one = 1;

    if (first) { // Initialization, ifnecessary
      if (k < zero) {
        // Error: argument k for random_von_Mises = ', k
        return -1;
      }

      nk = (int) (k + k + one);
      if (nk > 20) {
        return -1;
      }

      dk = k;
      theta[0] = zero;
      if (k > 0.5) {

        // Set up array p of probabilities.

        sump = zero;
        for (j = 1; j <= nk; j++) {
          if (j < nk) {
            theta[j] = Math.acos(one - j / k);
          } else {
            theta[nk] = pi;
          }
          // ! Numerical integration of e^[k.cos(x)] from theta(j-1) to theta(j)

          p[j - 1] = integral(theta[j - 1], theta[j], dk);
          sump = sump + p[j - 1];
        }

        for (j = 1; j <= nk; j++) {
          p[j - 1] /= sump;
        }
      } else {
        p[0] = one;
        theta[0] = pi;
      }
    }

    r = randomGenerator.nextDouble();
    for (j = 1; j <= nk; j++) {
      r = r - p[j - 1];
      if (r < zero) {
        break;
      }
    }
    r = -r / p[j - 1];

    for (int cpt = 0; cpt < 1000; cpt++) {
      th = theta[j - 1] + r * (theta[j] - theta[j - 1]);
      lambda = k - j + one - k * Math.cos(th);
      n = 1;
      rlast = lambda;

      for (int cpt2 = 0; cpt2 < 1000; cpt2++) {
        r = randomGenerator.nextDouble();
        if (r > rlast) {
          break;
        }

        n += 1;
        rlast = r;
      }

      if (n != (2 * (n / 2))) {
        break;
      }

      r = randomGenerator.nextDouble();

    }

    double factor = (r - rlast) / (one - rlast) - 0.5 > 0 ? 1 : -1;
    fn_val = Math.abs(th) * factor;

    return (fn_val);
  }

  /** Haversine formula to compute angles from lat and lon */
  private double Haversine(double lon1, double lat1, double lon2, double lat2) {

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
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'init'");
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

  /** Gaussian integration of exp(k.cosx) from a to b. */
  private double integral(double a, double b, double dk) {

    double xmid, range, x1, x2;
    double[] x = new double[] { 0.238619186083197, 0.661209386466265, 0.932469514203152 };
    double[] w = new double[] { 0.467913934572691, 0.360761573048139, 0.171324492379170 };

    xmid = (a + b) / 2.;
    range = (b - a) / 2.;

    double result = 0.;
    for (int i = 1; i <= 3; i++) {
      x1 = xmid + x[i - 1] * range;
      x2 = xmid - x[i - 1] * range;
      result = result + w[i - 1] * (Math.exp(dk * Math.cos(x1)) + Math.exp(dk * Math.cos(x2)));
    }

    result = result * range;
    return result;

  }
}
