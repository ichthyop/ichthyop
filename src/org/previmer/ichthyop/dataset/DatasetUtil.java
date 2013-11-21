/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

/**
 *
 * @author pverley
 */
public class DatasetUtil {

    /**
     * Computes the Hyperbolic Sinus of x
     * @param x
     * @return 
     */
    public static double sinh(double x) {
        return ((Math.exp(x) - Math.exp(-x)) / 2.d);
    }

    /**
     * Computes the Hyperbolic Cosinus of x
     * @param x
     * @return 
     */
    public static double cosh(double x) {
        return ((Math.exp(x) + Math.exp(-x)) / 2.d);
    }

    /**
     * Computes the Hyperbolic Tangent of x
     * @param x
     * @return 
     */
    public static double tanh(double x) {
        return (sinh(x) / cosh(x));
    }

    public static long skipSeconds(long time) {
        return time - time % 100L;
    }

    /**
     * Computes the geodesic distance between the two points
     * (lat1, lon1) and (lat2, lon2)
     * @param lat1 a double, the latitude of the first point
     * @param lon1 a double, the longitude of the first point
     * @param lat2 double, the latitude of the second point
     * @param lon2 double, the longitude of the second point
     * @return a double, the curvilinear absciss s(A[lat1, lon1]B[lat2, lon2])
     */
    public static double geodesicDistance(double lat1, double lon1, double lat2, double lon2) {

        double lat1_rad = Math.PI * lat1 / 180.d;
        double lat2_rad = Math.PI * lat2 / 180.d;
        double lon1_rad = Math.PI * lon1 / 180.d;
        double lon2_rad = Math.PI * lon2 / 180.d;

        double d = 2 * 6367000.d
                * Math.asin(Math.sqrt(Math.pow(Math.sin((lat2_rad - lat1_rad) / 2), 2)
                + Math.cos(lat1_rad) * Math.cos(lat2_rad) * Math.pow(Math.sin((lon2_rad - lon1_rad) / 2), 2)));

        return d;
    }

    /**
     * <p>The functions computes the 2nd order approximate
     * derivative at index i</p>
     * <code>diff2(X, i) == diff(diff(X), i) == diff(diff(X))[i]</code>
     * @param X double[]
     * @param k
     * @return double
     */
    public static double diff2(double[] X, int k) {

        int length = X.length;
        /** Returns NaN if size <= 2 */
        if (length < 3) {
            return Double.NaN;
        }

        /** This return statement traduces the natural spline hypothesis
         * M(0) = M(nz - 1) = 0 */
        if ((k <= 0) || (k >= (length - 1))) {
            return 0.d;
        }

        return (X[k + 1] - 2.d * X[k] + X[k - 1]);
    }
}
