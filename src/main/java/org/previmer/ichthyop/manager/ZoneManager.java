/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.*;
import org.previmer.ichthyop.io.XZone;
import org.previmer.ichthyop.io.XZone.XPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.io.XParameter;
import org.previmer.ichthyop.io.ZoneFile;

/**
 *
 * @author pverley
 */
public class ZoneManager extends AbstractManager {

    private static final ZoneManager ZONE_MANAGER = new ZoneManager();
    private final HashMap<TypeZone, ArrayList<Zone>> map;

    public static ZoneManager getInstance() {
        return ZONE_MANAGER;
    }

    private ZoneManager() {
        super();
        map = new HashMap<>();
    }

    public void cleanup() {
        map.clear();
    }

    public void loadZonesFromFile(String filename, TypeZone type) throws Exception {

        String pathname = IOTools.resolveFile(filename);
        File f = new File(pathname);
        if (!f.isFile()) {
            throw new FileNotFoundException("Zone file " + pathname + " not found.");
        }
        if (!f.canRead()) {
            throw new IOException("Zone file " + pathname + " cannot be read.");
        }

        ZoneFile zoneFile = new ZoneFile(f);
        if (!map.containsKey(type)) {
            map.put(type, new ArrayList<>());
        }
        for (XZone xzone : zoneFile.getZones()) {
            if (xzone.getTypeZone().equals(type) && xzone.isEnabled()) {
                int index = map.get(type).size();
                Zone zone = new Zone(xzone.getTypeZone(), xzone.getKey(), index);
                zone.setBathyMaskEnabled(xzone.isBathyMaskEnabled());
                zone.setOffshoreLine(xzone.getOffshoreLine());
                zone.setInshoreLine(xzone.getInshoreLine());
                zone.setThicknessEnabled(xzone.isThicknessEnabled());
                zone.setLowerDepth(xzone.getLowerDepth());
                zone.setUpperDepth(xzone.getUpperDepth());
                zone.setColor(xzone.getColor());
                for (XPoint point : xzone.getPolygon()) {
                    zone.addPoint(point.createRhoPoint());
                }
                for (XParameter xparam : xzone.getXParameters()) {
                    zone.addParameter(xparam);
                }
                map.get(type).add(zone);
            }
        }
    }

    public ArrayList<Zone> getZones(TypeZone type) {
        return map.get(type);
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
        getLogger().info("Zone manager initialization [OK]");
    }
}
