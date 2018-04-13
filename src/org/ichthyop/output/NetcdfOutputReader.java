/*
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2017
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
package org.ichthyop.output;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.ichthyop.IchthyopLinker;
import org.ichthyop.calendar.GregorianCalendar;
import org.ichthyop.ui.DrawableParticle;
import org.ichthyop.ui.DrawableZone;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class NetcdfOutputReader extends IchthyopLinker {

    private final String file;
    private String colorVariableName;
    private NetcdfFile nc;
    private Calendar calendar;
    private double lonmin, lonmax, latmin, latmax;

    public NetcdfOutputReader(String file) {
        this.file = file;
    }

    public File getFile() {
        return new File(file);
    }

    public void setColorVariableName(String name) {
        this.colorVariableName = name;
    }

    public String getColorVariableName() {
        return colorVariableName;
    }

    public String getColorVariableLongname() {
        try {
            String unit = nc.findVariable(colorVariableName).findAttribute("unit").getStringValue();
            return colorVariableName + " (" + unit + ")";
        } catch (Exception ex) {
        }
        return colorVariableName;
    }

    public void init() {

        // open NetCDF file
        try {
            nc = NetcdfDataset.openFile(file, null);
        } catch (IOException ex) {
            error("[output] Failed to open NetCDF output file " + file, ex);
        }

        // calendar
        Variable vtime = nc.findVariable("time");
        // Set origin of time
        Calendar calendar_o = Calendar.getInstance();
        int year_o = 1;
        int month_o = Calendar.JANUARY;
        int day_o = 1;
        int hour_o = 0;
        int minute_o = 0;
        if (null != vtime.findAttribute("units")) {
            try {
                SimpleDateFormat dFormat = new SimpleDateFormat("'seconds since' yyyy-MM-dd HH:mm:ss");
                dFormat.setCalendar(calendar_o);
                calendar_o.setTime(dFormat.parse(vtime.findAttribute("units").getStringValue()));
                year_o = calendar_o.get(Calendar.YEAR);
                month_o = calendar_o.get(Calendar.MONTH);
                day_o = calendar_o.get(Calendar.DAY_OF_MONTH);
                hour_o = calendar_o.get(Calendar.HOUR_OF_DAY);
                minute_o = calendar_o.get(Calendar.MINUTE);
            } catch (ParseException ex) {
                // Something went wrong, default origin of time
                // set to 0001/01/01 00:00
            }
        }
        if (null != vtime.findAttribute("calendar_class")) {
            String classname = vtime.findAttribute("calendar_class").getStringValue();
            try {
                calendar = (Calendar) Class.forName(classname).getConstructor(int.class, int.class, int.class, int.class, int.class).newInstance(year_o, month_o, day_o, hour_o, minute_o);
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                // we do not really mond if something goes wrong here
                // will use fallback calendar
            }
        }
        // fallback calendar
        if (null == calendar) {
            calendar = new GregorianCalendar(year_o, month_o, day_o, hour_o, minute_o);
            warning("Could not find calendar attribute in the NetCDF output file. It will only affect the display of time on the maps.");
        }

        // lon, lat min and max
        lonmin = Double.MAX_VALUE;
        lonmax = -lonmin;
        latmin = Double.MAX_VALUE;
        latmax = -latmin;

        for (GeoPosition gp : readEdge()) {
            if (gp.getLongitude() >= lonmax) {
                lonmax = gp.getLongitude();
            }
            if (gp.getLongitude() <= lonmin) {
                lonmin = gp.getLongitude();
            }
            if (gp.getLatitude() >= latmax) {
                latmax = gp.getLatitude();
            }
            if (gp.getLatitude() <= latmin) {
                latmin = gp.getLatitude();
            }
        }
        double double_tmp;
        if (lonmin > lonmax) {
            double_tmp = lonmin;
            lonmin = lonmax;
            lonmax = double_tmp;
        }
        if (latmin > latmax) {
            double_tmp = latmin;
            latmin = latmax;
            latmax = double_tmp;
        }
    }

    public void close() {
        try {
            nc.close();
        } catch (IOException ex) {
            debug(ex);
        }
    }

    public int getNTime() {
        return nc.getUnlimitedDimension().getLength();
    }

    public List<GeoPosition> readEdge() {

        List<GeoPosition> edge = new ArrayList();
        try {
            ArrayFloat.D2 regionEdge = (ArrayFloat.D2) nc.findVariable("edge").read();
            for (int i = 0; i < regionEdge.getShape()[0]; i++) {
                edge.add(new GeoPosition(regionEdge.get(i, 0), regionEdge.get(i, 1)));
            }
        } catch (IOException ex) {
            warning("[output] Failed to read NetCDF variable \"edge\"", ex);
        }

        return edge;
    }

    public List<DrawableZone> readZones() {

        List<DrawableZone> zones = new ArrayList();
        if (null != nc.findGlobalAttribute("number_of_zones")) {
            int nbZones = nc.findGlobalAttribute("number_of_zones").getNumericValue().intValue();
            for (int iZone = 0; iZone < nbZones; iZone++) {
                try {
                    Variable varZone = nc.findVariable("zone" + iZone);
                    ArrayFloat.D2 arrZone = (ArrayFloat.D2) varZone.read();
                    int color = varZone.findAttribute("color").getNumericValue().intValue();
                    DrawableZone zone = new DrawableZone(color);
                    for (int i = 0; i < arrZone.getShape()[0]; i++) {
                        zone.addPoint(new GeoPosition(arrZone.get(i, 0), arrZone.get(i, 1)));
                    }
                    zones.add(zone);
                } catch (IOException ex) {
                    warning("[output] Failed to read NetCDF variable \"zone" + iZone + "\"", ex);
                }
            }
        }
        return zones;
    }

    public List<GeoPosition> readMask() {

        List<GeoPosition> mask = new ArrayList();
        try {
            ArrayFloat.D2 maskVar = (ArrayFloat.D2) nc.findVariable("mask").read();
            for (int i = 0; i < maskVar.getShape()[0]; i++) {
                mask.add(new GeoPosition(maskVar.get(i, 0), maskVar.get(i, 1)));
            }
        } catch (IOException ex) {
            warning("[output] Failed to read NetCDF variable \"mask\"", ex);
        }
        return mask;
    }

    public List<DrawableParticle> readParticles(int itime) {

        List<DrawableParticle> list = new ArrayList();
        try {
            int nparticle = nc.findDimension("drifter").getLength();
            Variable vlon = nc.findVariable("lon");
            ArrayFloat.D1 arrLon = (ArrayFloat.D1) vlon.read(new int[]{itime, 0}, new int[]{1, nparticle}).reduce();
            Variable vlat = nc.findVariable("lat");
            ArrayFloat.D1 arrLat = (ArrayFloat.D1) vlat.read(new int[]{itime, 0}, new int[]{1, nparticle}).reduce();
            Variable vmortality = nc.findVariable("mortality");
            ArrayInt.D1 arrMortality = (ArrayInt.D1) vmortality.read(new int[]{itime, 0}, new int[]{1, nparticle}).reduce();
            // read custom colorbar variable
            if (null != colorVariableName && null != nc.findVariable(colorVariableName)) {
                Variable variable = nc.findVariable(colorVariableName);
                Array arrColorVariable;
                if (colorVariableName.equals("time")) {
                    arrColorVariable = variable.read(new int[]{itime}, new int[]{1}).reduce();
                } else {
                    arrColorVariable = variable.read(new int[]{itime, 0}, new int[]{1, nparticle}).reduce();
                }
                for (int iparticle = 0; iparticle < nparticle; iparticle++) {
                    float value = arrColorVariable.getSize() < 2 ? arrColorVariable.getFloat(0) : arrColorVariable.getFloat(iparticle);
                    list.add(new DrawableParticle(arrLon.get(iparticle), arrLat.get(iparticle),
                            value, arrMortality.get(iparticle) == 0));
                }
            } else {
                for (int iparticle = 0; iparticle < nparticle; iparticle++) {
                    list.add(new DrawableParticle(arrLon.get(iparticle), arrLat.get(iparticle),
                            Float.NaN, arrMortality.get(iparticle) == 0));
                }
            }
        } catch (Exception ex) {
            warning("[output] Error reading NetCDF \"lon\" or \"lat\" or \"mortality\" variables for particle " + itime, ex);
        }
        return list;
    }

    public double readTime(int itime) {
        double timeD = 0.d;
        try {
            ArrayDouble.D0 arrTime = (ArrayDouble.D0) nc.findVariable("time").read(new int[]{itime}, new int[]{1}).reduce();
            timeD = arrTime.get();
        } catch (IOException | InvalidRangeException ex) {
            warning("[output] Error reading NetCDF \"time\" variable.", ex);
        }
        return timeD;
    }

    public double[] readTime() {

        try {
            return (double[]) nc.findVariable("time").read().copyTo1DJavaArray();
        } catch (IOException ex) {
            warning("[output] Error reading NetCDF \"time\" variable.", ex);
        }
        return null;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public double getLatmin() {
        return latmin;
    }

    public double getLatmax() {
        return latmax;
    }

    public double getLonmin() {
        return lonmin;
    }

    public double getLonmax() {
        return lonmax;
    }

    public String[] getVariables() {
        List<String> list = new ArrayList();
        list.add("None");
        list.add("time");
        for (Variable variable : nc.getVariables()) {
            List<Dimension> dimensions = variable.getDimensions();
            boolean excluded = (dimensions.size() != 2);
            if (!excluded) {
                excluded = !(dimensions.get(0).getFullName().equals("time") && dimensions.get(1).getFullName().equals("drifter"));
            }
            if (!excluded) {
                list.add(variable.getFullName());
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public float[] getRange(String variable) throws IOException {

        Array array = nc.findVariable(variable).read();

        float[] dataset = (float[]) array.get1DJavaArray(Float.class);
        if (variable.equals("time")) {
            if (dataset[0] > dataset[dataset.length - 1]) {
                return new float[]{dataset[dataset.length - 1], dataset[0]};
            } else {
                return new float[]{dataset[0], dataset[dataset.length - 1]};
            }
        } else {
            double mean = getMean(dataset);
            double stdDeviation = getStandardDeviation(dataset, mean);
            float lower = (float) Math.max((float) (mean - 2 * stdDeviation), getMin(dataset));
            float upper = (float) Math.min((float) (mean + 2 * stdDeviation), getMax(dataset));
            return new float[]{lower, upper};
        }
    }

    private double getMin(float[] dataset) {
        float min = Float.MAX_VALUE;
        for (float num : dataset) {
            if (num < min) {
                min = num;
            }
        }
        return min;
    }

    private double getMax(float[] dataset) {
        float max = -1 * Float.MAX_VALUE;
        for (float num : dataset) {
            if (num > max) {
                max = num;
            }
        }
        return max;
    }

    private double getMean(float[] dataset) {
        double sum = 0;
        for (double num : dataset) {
            if (!Double.isNaN(num)) {
                sum += num;
            }
        }
        return sum / dataset.length;
    }

    private double getSquareSum(float[] dataset) {
        double sum = 0;
        for (double num : dataset) {
            if (!Double.isNaN(num)) {
                sum += num * num;
            }
        }
        return sum;
    }

    private double getStandardDeviation(float[] dataset, double mean) {
        // Return standard deviation of all the items that have been entered.
        // Value will be Double.NaN if count == 0.
        double squareSum = getSquareSum(dataset);
        return Math.sqrt(squareSum / dataset.length - mean * mean);
    }
}
