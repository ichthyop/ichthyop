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

        double d = 0.d;
        double lat1_rad = Math.PI * lat1 / 180.d;
        double lat2_rad = Math.PI * lat2 / 180.d;
        double lon1_rad = Math.PI * lon1 / 180.d;
        double lon2_rad = Math.PI * lon2 / 180.d;

        d = 2 * 6367000.d
                * Math.asin(Math.sqrt(Math.pow(Math.sin((lat2_rad - lat1_rad) / 2), 2)
                + Math.cos(lat1_rad) * Math.cos(lat2_rad) * Math.pow(Math.sin((lon2_rad - lon1_rad) / 2), 2)));

        double d1 = approxGeodesicDistance(lat1, lon1, lat2, lon2);
        double err = Math.abs((d - d1) / d);
        //System.out.println(d + " " + d1 + " " + err);
        return d;
    }

    private static double approxGeodesicDistance(double lat1, double lon1, double lat2,
                                   double lon2) {
        //--------------------------------------------------------------
        // Return the curvilinear absciss s(A[lat1, lon1]B[lat2, lon2])
        double d = 6367000.d * Math.sqrt(2.d
                                         - 2.d *
                                         Math.cos(Math.PI * lat1 / 180.d) *
                                         Math.cos(Math.PI * lat2 / 180.d) *
                                         Math.cos(Math.PI * (lon1 - lon2) /
                                                  180.d)
                                         - 2.d *
                                         Math.sin(Math.PI * lat1 / 180.d) *
                                         Math.sin(Math.PI * lat2 / 180.d));
        return (d);
    }

    /**
     * <p>The functions computes the 2nd order approximate
     * derivative at index i</p>
     * <code>diff2(X, i) == diff(diff(X), i) == diff(diff(X))[i]</code>
     * @param x double[]
     * @param i int
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

    private static double toRad(double angle) {
        return Math.PI * angle / 180.d;
    }

    private static double distVincenty(double lat1, double lon1, double lat2, double lon2) {
        
        double a = 6378137, b = 6356752.3142, f = 1 / 298.257223563;  // WGS-84 ellipsoid params
        double L = toRad(lon2 - lon1);
        double U1 = Math.atan((1 - f) * Math.tan(toRad(lat1)));
        double U2 = Math.atan((1 - f) * Math.tan(toRad(lat2)));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        double lambda = L, lambdaP, iterLimit = 100;
        double cosSqAlpha, cos2SigmaM, sinSigma, sigma, cosSigma;
        do {
            double sinLambda = Math.sin(lambda), cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
                    + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            if (sinSigma == 0) {
                return 0;  // co-incident points
            }
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM)) {
                cos2SigmaM = 0;  // equatorial line: cosSqAlpha=0 (§6)
            }
            double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1 - C) * f * sinAlpha
                    * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

        if (iterLimit == 0) {
            return Double.NaN; // formula failed to converge
        }
        double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)
                - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
        double s = b * A * (sigma - deltaSigma);

        return s;
    }

    public static double loxoSameLat(double lat1, double lon1, double lat2, double lon2) {
        
        double d1 = 6367000.d * Math.abs(toRad(lon2 - lon1)) * Math.cos(toRad(lat1));
        //double d2 = 1852 * 60.d * Math.abs(lon2 - lon1) * Math.cos(toRad(lat1));
        return d1;
    }

    public static double loxoSameLon(double lat1, double lon1, double lat2, double lon2) {

        double d1 = 6367000.d * Math.abs(toRad(lat2 - lat1));
        //double d2 = 1852 * 60.d * Math.abs(lat2 - lat1);
        return d1;
    }
}
