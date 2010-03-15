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
     */
    public static double sinh(double x) {
        return ((Math.exp(x) - Math.exp(-x)) / 2.d);
    }

    /**
     * Computes the Hyperbolic Cosinus of x
     */
    public static double cosh(double x) {
        return ((Math.exp(x) + Math.exp(-x)) / 2.d);
    }

    /**
     * Computes the Hyperbolic Tangent of x
     */
    public static double tanh(double x) {
        return (sinh(x) / cosh(x));
    }

    public static long skipSeconds(long time) {
        return time - time % 60L;
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
    public static double geodesicDistance(double lat1, double lon1, double lat2,
            double lon2) {
        //--------------------------------------------------------------
        // Return the curvilinear absciss s(A[lat1, lon1]B[lat2, lon2])
        double d = 6367000.d * Math.sqrt(2.d
                - 2.d
                * Math.cos(Math.PI * lat1 / 180.d)
                * Math.cos(Math.PI * lat2 / 180.d)
                * Math.cos(Math.PI * (lon1 - lon2)
                / 180.d)
                - 2.d
                * Math.sin(Math.PI * lat1 / 180.d)
                * Math.sin(Math.PI * lat2 / 180.d));
        return (d);
    }

}
