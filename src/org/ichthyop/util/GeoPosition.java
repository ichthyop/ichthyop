/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package org.ichthyop.util;

/**
 * An immutable coordinate in the real (geographic) world, 
 * composed of a latitude and a longitude.
 * @author rbair
 */
public class GeoPosition {
    private double latitude;
    private double longitude;
    
    /**
     * Creates a new instance of GeoPosition from the specified
     * latitude and longitude. These are double values in decimal degrees, not
     * degrees, minutes, and seconds.  Use the other constructor for those.
     * @param latitude a latitude value in decmial degrees
     * @param longitude a longitude value in decimal degrees
     */
    public GeoPosition(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    // must be an array of length two containing lat then long in that order.
    /**
     * Creates a new instance of GeoPosition from the specified
     * latitude and longitude as an array of two doubles, with the
     * latitude first. These are double values in decimal degrees, not
     * degrees, minutes, and seconds.  Use the other constructor for those.
     * @param coords latitude and longitude as a double array of length two
     */
    public GeoPosition(double [] coords) {
        this.latitude = coords[0];
        this.longitude = coords[1];
    }
    
    /**
     * Creates a new instance of GeoPosition from the specified
     * latitude and longitude. 
     * Each are specified as degrees, minutes, and seconds; not
     * as decimal degrees. Use the other constructor for those.
     * @param latDegrees the degrees part of the current latitude
     * @param latMinutes the minutes part of the current latitude
     * @param latSeconds the seconds part of the current latitude
     * @param lonDegrees the degrees part of the current longitude
     * @param lonMinutes the minutes part of the current longitude
     * @param lonSeconds the seconds part of the current longitude
     */
    public GeoPosition(double latDegrees, double latMinutes, double latSeconds,
            double lonDegrees, double lonMinutes, double lonSeconds) {
        this(latDegrees + (latMinutes + latSeconds/60.0)/60.0,
             lonDegrees + (lonMinutes + lonSeconds/60.0)/60.0);
    }
    
    /**
     * Get the latitude as decimal degrees
     * @return the latitude as decimal degrees
     */
    public double getLatitude() {
        return latitude;
    }
    
    /**
     * Get the longitude as decimal degrees
     * @return the longitude as decimal degrees
     */
    public double getLongitude() {
        return longitude;
    }
    
    /**
     * Returns true the specified GeoPosition and this GeoPosition represent
     * the exact same latitude and longitude coordinates.
     * @param obj a GeoPosition to compare this GeoPosition to
     * @return returns true if the specified GeoPosition is equal to this one
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GeoPosition) {
            GeoPosition coord = (GeoPosition)obj;
            return coord.latitude == latitude && coord.longitude == longitude;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.latitude) ^ (Double.doubleToLongBits(this.latitude) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.longitude) ^ (Double.doubleToLongBits(this.longitude) >>> 32));
        return hash;
    }
    
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    public String toString() {
        return "[" + latitude + ", " + longitude + "]";
    }
}