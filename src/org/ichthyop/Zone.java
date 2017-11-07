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
package org.ichthyop;

import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Objects;
import org.ichthyop.dataset.IDataset;
import org.ichthyop.ui.LonLatConverter;

/**
 * <p>
 * This class defines a geographical area used by the program to locate the
 * release areas or the recruitment areas. It provides a tool to determine
 * whether any grid point belongs to the Zone.
 * </p>
 *
 * The geographical area is defined as followed : at least three geodesic
 * demarcation points {lon, lat} P1, P2, P3, .., Pn and two depths depth1 &
 * depth2.
 * <p>
 * Longitude is expressed in East degree and latitude in North degree. Depths
 * are positive Integers.
 * </p>
 * The area is first delimited by the polygon (P1 P2 P3 P4 .. Pn). The points
 * don't have to be in the water, but make sure they belong to the geographical
 * grid of the simulation. The four demarcation points P1(plon1, plat1) to
 * P4(plon4, plat4) must be recorded in the clockwise or anticlockwise
 * direction. Then, another routine isolates the area of the polygon that is
 * contained between the bathymetric lines depth1 & depth2. At last the user can
 * choose the color (Red[0, 255], Green[0, 255], Blue[0, 255]) of the area.
 *
 * @author P.Verley (philippe.verley@ird.fr)
 * @version 3.3 2013/11/15
 */
public class Zone extends IchthyopLinker {

    /*
     * Parameter key of the zone definition in the CFG file
     */
    private final String key;

    private final float index;
    /**
     * List of geographical coordinates that defines an area
     */
    private final double[] lat;
    private final double[] lon;
    /*
     * Lower bathymetric line [meter]
     */
    private final float inshoreLine;
    /**
     * Higher bathymetric line [meter]
     */
    private final float offshoreLine;
    /*
     * [meter]
     */
    private final float lowerDepth;
    /**
     * [meter]
     */
    private final float upperDepth;
    /*
     * Zone name in the configuration file
     */
    private final String name;
    /**
     * Zone color (RGB)
     */
    private final Color color;
    /*
     * Whether to consider the (vertical) thickness of the zone. Not enabled
     * means that the zone covers all the water column.
     */
    private final boolean enabledThickness;
    /*
     * Whether to enable the bathymetric mask.
     */
    private final boolean enabledBathyMask;
    /*
     * Horizontal extent of the zone in km2
     */
    private double area;

    /**
     * Creates a new zone.
     *
     * @param key, a unique key of the zone
     * @param index, a unique index of the zone
     */
    public Zone(String key, float index) {
        this.key = key;
        this.index = index;
        // initializes variables
        String slat[] = getConfiguration().getArrayString(key + ".latitude");
        lat = new double[slat.length + 1];
        for (int k = 0; k < slat.length; k++) {
            lat[k] = Double.valueOf(LonLatConverter.convert(slat[k], LonLatConverter.LonLatFormat.DecimalDeg));
        }
        lat[slat.length] = lat[0];
        String slon[] = getConfiguration().getArrayString(key + ".longitude");
        lon = new double[slon.length + 1];
        for (int k = 0; k < slon.length; k++) {
            lon[k] = Double.valueOf(LonLatConverter.convert(slon[k], LonLatConverter.LonLatFormat.DecimalDeg));
        }
        lon[slon.length] = lon[0];
        name = getConfiguration().getString(key + ".name");
        enabledBathyMask = getConfiguration().getBoolean(key + ".bathymetry.enabled");
        inshoreLine = getConfiguration().getFloat(key + ".bathymetry.inshore");
        offshoreLine = getConfiguration().getFloat(key + ".bathymetry.offshore");
        enabledThickness = getConfiguration().getBoolean(key + ".depth.enabled");
        lowerDepth = getConfiguration().getFloat(key + ".depth.lower");
        upperDepth = getConfiguration().getFloat(key + ".depth.upper");
        color = new Color(getConfiguration().getInt(key + ".color"));
    }

    /**
     * Returns a unique index for the zone
     *
     * @return the index of the zone
     */
    public float getIndex() {
        return index;
    }
    
    public String getPrefix() {
        return key.substring(0, key.lastIndexOf("."));
    }

    public boolean isEnabledBathyMask() {
        return enabledBathyMask;
    }

    public boolean isEnabledDepthMask() {
        return enabledThickness;
    }

    /**
     * Returns the color of the zone.
     *
     * @return the color of the zone
     */
    public Color getColor() {
        return color;
    }

    public String getKey() {
        return key;
    }

    /**
     * Returns the name of the zone.
     *
     * @return the name of the zone
     */
    public String getName() {
        return name;
    }

    public float getUpperDepth() {
        return upperDepth;
    }

    public float getLowerDepth() {
        return lowerDepth;
    }

    public double[] getLat() {
        return lat;
    }

    public double[] getLon() {
        return lon;
    }

    public float getInshoreLine() {
        return inshoreLine;
    }

    public float getOffshoreLine() {
        return offshoreLine;
    }

    public void init() {

        // lon & lat same length
        if (lat.length != lon.length) {
            error("Longitude and latitude vectors must have same length", new IOException(key + " definition error"));
        }

        // upper depth > lower depth
        if (enabledThickness && (lowerDepth < upperDepth)) {
            error("Lower depth must be deeper than upper depth", new IOException(key + " definition error"));
        }

        // inshore line > offshore line
        if (enabledBathyMask && (offshoreLine < inshoreLine)) {
            error("Offshore line must be deeper than inshore line", new IOException(key + " definition error"));
        }

        // compute area in km2
        area = 0.d;
        IDataset dataset = getSimulationManager().getDataset();
        for (int i = 0; i < dataset.get_nx(); i++) {
            for (int j = 0; j < dataset.get_ny(); j++) {
                if (dataset.isInWater(i, j)) {
                    if (getSimulationManager().getZoneManager().isInside(i, j, key)) {
                        area += dataset.getdeta(j, i) * dataset.getdxi(j, i) * 1e-6;
                    }
                }
            }
        }
    }

    public double getArea() {
        return area;
    }

    public double getLonMin() {
        return min(lon);
    }

    public double getLatMin() {
        return min(lat);
    }

    public double getLonMax() {
        return max(lon);
    }

    public double getLatMax() {
        return max(lat);
    }

    @Override
    public String toString() {
        StringBuilder zoneStr = new StringBuilder();
        zoneStr.append("Zone ");
        zoneStr.append(name);
        zoneStr.append("\n  Polygon [ ");
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(3);
        for (int k = 0; k < lat.length; k++) {
            zoneStr.append("(");
            zoneStr.append(df.format(lat[k]));
            zoneStr.append(" ");
            zoneStr.append(df.format(lon[k]));
            zoneStr.append(") ");
        }
        zoneStr.append(']');
        if (this.enabledBathyMask) {
            zoneStr.append("\n  Bathymetry mask ");
            zoneStr.append(inshoreLine);
            zoneStr.append("m, ");
            zoneStr.append(offshoreLine);
            zoneStr.append("m");
        }
        if (this.enabledThickness) {
            zoneStr.append("\n  Depth ");
            zoneStr.append(upperDepth);
            zoneStr.append("m, ");
            zoneStr.append(lowerDepth);
            zoneStr.append("m");
        }
        zoneStr.append("\n  Area ");
        zoneStr.append(df.format(area));
        zoneStr.append(" km2");

        return zoneStr.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (null != object && object instanceof Zone) {
            return this.name.equals(((Zone) object).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.name);
        return hash;
    }

    private double min(double[] array) {
        double min = array[0];
        for (double d : array) {
            min = Math.min(d, min);
        }
        return min;
    }

    private double max(double[] array) {
        double max = array[0];
        for (double d : array) {
            max = Math.max(d, max);
        }
        return max;
    }

}
