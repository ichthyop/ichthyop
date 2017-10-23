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
package org.ichthyop.manager;

import java.awt.Color;
import org.ichthyop.Zone;
import java.io.IOException;
import org.ichthyop.event.InitializeEvent;
import org.ichthyop.event.SetupEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.ichthyop.GridPoint;
import org.ichthyop.ui.LonLatConverter;

/**
 *
 * @author pverley
 */
public class ZoneManager extends AbstractManager {

    private static final ZoneManager ZONE_MANAGER = new ZoneManager();
    private final HashMap<String, ArrayList<Zone>> map;

    public static ZoneManager getInstance() {
        return ZONE_MANAGER;
    }

    private ZoneManager() {
        super();
        map = new HashMap();
    }

    public void cleanup() {
        map.clear();
    }

    public void loadZones(String prefix) throws Exception {

        ArrayList<Zone> zones = new ArrayList();
        
        int index = 0;
        for (String zname : getConfiguration().findKeys(prefix + ".zone*.name")) {
            String zkey = zname.substring(0, zname.lastIndexOf(".name"));
            if (getConfiguration().getBoolean(zkey + ".enabled")) {
                Zone zone = new Zone(getConfiguration().getString(zkey + ".name"), index);
                zone.setBathyMaskEnabled(getConfiguration().getBoolean(zkey + ".bathymetry.enabled"));
                zone.setOffshoreLine(getConfiguration().getFloat(zkey + ".bathymetry.offshore"));
                zone.setInshoreLine(getConfiguration().getFloat(zkey + ".bathymetry.inshore"));
                zone.setThicknessEnabled(getConfiguration().getBoolean(zkey + ".depth.enabled"));
                zone.setLowerDepth(getConfiguration().getFloat(zkey + ".depth.lower"));
                zone.setUpperDepth(getConfiguration().getFloat(zkey + ".depth.upper"));
                zone.setColor(new Color(getConfiguration().getInt(zkey + ".color")));
                String[] slat = getConfiguration().getArrayString(zkey + ".latitude");
                String[] slon = getConfiguration().getArrayString(zkey + ".longitude");
                if (slat.length != slon.length) {
                    error("Longitude and latitude vectors must have same length", new IOException("Zone " + zone.getKey() + " definition error"));
                }
                for (int k = 0; k < slat.length; k++) {
                    GridPoint rhoPoint = new GridPoint(false);
                    double lat = Double.valueOf(LonLatConverter.convert(slat[k], LonLatConverter.LonLatFormat.DecimalDeg));
                    double lon = Double.valueOf(LonLatConverter.convert(slon[k], LonLatConverter.LonLatFormat.DecimalDeg));
                    rhoPoint.setLat(lat);
                    rhoPoint.setLon(lon);
                    zone.addPoint(rhoPoint);
                }
                zones.add(zone);
                index++;
            }
        }
        map.put(prefix, zones);
    }

    public ArrayList<Zone> getZones(String prefix) {
        return map.get(prefix);
    }

    public List<String> getPrefixes() {
        ArrayList prefixes = new ArrayList(map.keySet());
        Collections.sort(prefixes);
        return prefixes;
    }

    @Override
    public void setupPerformed(SetupEvent e) throws Exception {
        /* Nothing to do. Zones are loaded by other classes such as Action
        or ReleaseProcess */
    }

    @Override
    public void initializePerformed(InitializeEvent e) throws Exception {

        for (List<Zone> listZone : map.values()) {
            for (Zone zone : listZone) {
                zone.init();
            }
        }
        info("Zone manager initialization [OK]");
    }
}
