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

import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.ichthyop.calendar.Day360Calendar;
import org.ichthyop.calendar.GregorianCalendar;
import org.ichthyop.manager.TimeManager;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class ExportToKML {

    final private String ncfile;
    private NetcdfFile nc;
    private Kml kml;
    private Document kmlDocument;
    private Folder kmlMainFolder;
    private List<String> styles;
    private Calendar calendar;
    final private int particlePixel;
    final private Random random = new Random();

    public ExportToKML(String ncfile, int particlePixel) {
        this.ncfile = ncfile;
        this.particlePixel = particlePixel;
    }

    public ExportToKML(String ncfile) {
        this(ncfile, 1);
    }

    public void init() throws IOException {
        kml = KmlFactory.createKml();
        kmlDocument = kml.createAndSetDocument().withName(new File(ncfile).getName()).withOpen(true);
        kmlMainFolder = kmlDocument.createAndAddFolder();

        // open netcdf
        nc = NetcdfDataset.openFile(ncfile, null);

        // Read time variable
        Variable vtime = nc.findVariable("time");
        //Variable vcolor = nc.findVariable(colorVariable);

        // set origin of time
        Calendar calendar_o = Calendar.getInstance();
        int year_o = 1;
        int month_o = Calendar.JANUARY;
        int day_o = 1;
        int hour_o = 0;
        int minute_o = 0;
        if (null != vtime.findAttribute("origin")) {
            try {
                SimpleDateFormat dFormat = TimeManager.INPUT_DATE_FORMAT;
                dFormat.setCalendar(calendar_o);
                calendar_o.setTime(dFormat.parse(vtime.findAttribute("origin").getStringValue()));
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
        if (vtime.findAttribute("calendar").getStringValue().equals("climato")) {
            calendar = new Day360Calendar(year_o, month_o, day_o, hour_o, minute_o);
        } else {
            calendar = new GregorianCalendar(year_o, month_o, day_o, hour_o, minute_o);
        }

        // random color for particles
        styles = new ArrayList();
        int nparticle = nc.findDimension("drifter").getLength();
        for (int i = 0; i < nparticle; i++) {
            String color = colorId(newRandomColor());
            String styleid = "IconStyle" + color;
            if (!styles.contains(styleid)) {
                kmlDocument
                        .createAndAddStyle().withId(styleid)
                        .createAndSetIconStyle().withColor(color).withScale(particlePixel / 10.d)
                        .createAndSetIcon().withHref("http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png");
            }
            styles.add(styleid);
        }
    }

    public void writeKML() throws IOException, InvalidRangeException {

        // read variables
        Variable vlon = nc.findVariable("lon");
        Variable vlat = nc.findVariable("lat");
        Variable vmortality = nc.findVariable("mortality");
        Variable vtime = nc.findVariable("time");

        // number of time steps and time values
        int ntime = nc.getUnlimitedDimension().getLength();
        double[] time = (double[]) vtime.read().copyTo1DJavaArray();

        // number of particles
        int nparticle = nc.findDimension("drifter").getLength();

        // loop over time
        for (int i = 0; i < ntime; i++) {
            // time span
            SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat dtFormat2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Folder stepFolder = new Folder();
            dtFormat.setCalendar(calendar);
            dtFormat2.setCalendar(calendar);
            calendar.setTimeInMillis((long) (time[i] * 1000));
            Date currentTime = calendar.getTime();
            calendar.setTimeInMillis((long) ((i < time.length - 1) ? time[i + 1] : (2 * time[i] - time[i - 1])) * 1000L);
            Date nextTime = calendar.getTime();
            stepFolder.withName(dtFormat2.format(currentTime));//.createAndSetTimeStamp().setWhen(dtFormat.format(cld.getTime()));
            if (currentTime.before(nextTime)) {
                stepFolder.createAndSetTimeSpan().withBegin(dtFormat.format(currentTime)).withEnd(dtFormat.format(nextTime));
            } else {
                stepFolder.createAndSetTimeSpan().withBegin(dtFormat.format(nextTime)).withEnd(dtFormat.format(currentTime));
            }
            // extract lon and lat for current time step
            Array lon = vlon.read(new int[]{i, 0}, new int[]{1, nparticle}).reduce();
            Array lat = vlat.read(new int[]{i, 0}, new int[]{1, nparticle}).reduce();
            Array mortality = vmortality.read(new int[]{i, 0}, new int[]{1, nparticle}).reduce();
            // loop over particles
            for (int iparticle = 0; iparticle < nparticle; iparticle++) {
                if (0 == mortality.getInt(iparticle)) {
                    String coord = Double.toString(lon.getDouble(iparticle)) + "," + Double.toString(lat.getDouble(iparticle));
                    Placemark placeMark = stepFolder.createAndAddPlacemark();
                    placeMark.withStyleUrl("#" + styles.get(iparticle)).createAndSetPoint().addToCoordinates(coord);
                }
            }
            kmlMainFolder.addToFeature(stepFolder);
        }
    }

    public boolean toKMZ() throws IOException {
        File kmzFile = new File(ncfile.replace(".nc", ".kmz"));
        if (kmzFile.exists()) {
            kmzFile.delete();
        }
        return kml.marshalAsKmz(kmzFile.getAbsolutePath());
    }

    public String getKMZ() {
        return new File(ncfile.replace(".nc", ".kmz")).getAbsolutePath();
    }

    private String colorId(Color color) {
        // format color for KLM
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        StringBuilder hexColor = new StringBuilder(8);
        hexColor.append("ff");
        String hR = Integer.toHexString(red);
        if (hR.length() < 2) {
            hR = "0" + hR;
        }
        String hB = Integer.toHexString(blue);
        if (hB.length() < 2) {
            hB = "0" + hB;
        }
        String hG = Integer.toHexString(green);
        if (hG.length() < 2) {
            hG = "0" + hG;
        }
        hexColor.append(hB);
        hexColor.append(hG);
        hexColor.append(hR);
        return hexColor.toString();
    }

    private Color newRandomColor() {

        // generate random color
        final float hue = random.nextFloat();
        // Saturation between 0.1 and 0.3
        final float saturation = (random.nextInt(2000) + 1000) / 10000f;
        final float luminance = 0.9f;
        return Color.getHSBColor(hue, saturation, luminance);
    }
}
