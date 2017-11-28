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

import org.ichthyop.Zone;
import java.io.IOException;
import java.text.DecimalFormat;
import org.ichthyop.event.InitializeEvent;
import org.ichthyop.event.SetupEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import static org.ichthyop.IchthyopLinker.getSimulationManager;
import org.ichthyop.dataset.BathymetryDataset;
import org.ichthyop.particle.GriddedParticle;
import org.ichthyop.particle.IParticle;

/**
 *
 * @author pverley
 */
public class ZoneManager extends AbstractManager {

    private static final ZoneManager ZONE_MANAGER = new ZoneManager();
    private final HashMap<String, Zone> zones;
    private BathymetryDataset bathymetry;

    public static ZoneManager getInstance() {
        return ZONE_MANAGER;
    }

    private ZoneManager() {
        super();
        zones = new HashMap();
    }

    public void cleanup() {
        zones.clear();
    }

    public void loadZones(String prefix) throws Exception {

        LinkedHashSet set = new LinkedHashSet();
        for (Zone zone : getSimulationManager().getZoneManager().getZones()) {
            if (prefix.equals(zone.getPrefix())) {
                warning("Zones with such prefix have already be loaded.", new IOException("Zone prefix " + prefix + " duplicated"));
                return;
            }
            set.add(Math.floor(zone.getIndex()));
        }
        double index = set.size() + 1;

        List<String> keys = getConfiguration().findKeys(prefix + ".zone*.name");
        int ndecim = String.valueOf(keys.size()).length();
        DecimalFormat df = new DecimalFormat();
        df.setMinimumFractionDigits(ndecim);
        df.setMaximumFractionDigits(ndecim);
        double incr = Math.pow(10.d, -ndecim);
        for (String zname : keys) {
            String zkey = zname.substring(0, zname.lastIndexOf(".name"));
            if (getConfiguration().getBoolean(zkey + ".enabled")) {
                index += incr;
                Zone zone = new Zone(zkey, Float.valueOf(df.format(index)));
                if (zones.containsKey(zone.getKey())) {
                    error("Zones must have unique name", new IOException("Zone " + zone.getName() + " already exists"));
                }
                zones.put(zkey, zone);
            }
        }
    }

    private boolean isInside(IParticle particle, String key) {
        
        Zone zone = zones.get(key);
        boolean inside = true;
        if (zone.isEnabledDepthMask()) {
            double depth = Math.abs(particle.getDepth());
            inside = depth <= zone.getLowerDepth() & depth >= zone.getUpperDepth();
        }
        if (inside && zone.isEnabledBathyMask() && null != bathymetry) {
            double[] xyz = GriddedParticle.xyz(particle, bathymetry.getKey());
            double bathy = bathymetry.getBathymetry(xyz);
            inside = (bathy > zone.getInshoreLine()) & (bathy < zone.getOffshoreLine());
        }
        return inside && isInside(particle.getLat(), particle.getLon(), zone.getLat(), zone.getLon());
    }

    public boolean isInside(double lat, double lon, String key) {
        
        Zone zone = zones.get(key);
        boolean inside = true;
        if (zone.isEnabledBathyMask() && null != bathymetry) {
            double[] xy = bathymetry.getGrid().latlon2xy(lat, lon);
            double bathy = bathymetry.getBathymetry(xy);
            inside = (bathy > zone.getInshoreLine()) & (bathy < zone.getOffshoreLine());
        }
        return inside && isInside(lat, lon, zone.getLat(), zone.getLon());
    }

    /*
     * Return true if the given point is contained inside the boundary. See:
     * http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
     */
    public boolean isInside(double lat0, double lon0, double[] lat, double[] lon) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = lat.length - 1; i < lat.length; j = i++) {
            if ((lat[i] > lat0) != (lat[j] > lat0)
                    && (lon0 < (lon[j] - lon[i]) * (lat0 - lat[i]) / (lat[j] - lat[i]) + lon[i])) {
                result = !result;
            }
        }
        return result;
    }

    public Collection<Zone> getZones() {
        return zones.values();
    }

    public List<Zone> getZones(String prefix) {
        List<Zone> pzones = new ArrayList();
        for (Zone zone : zones.values()) {
            if (prefix.equals(zone.getPrefix())) {
                pzones.add(zone);
            }
        }
        return pzones;
    }

    public Zone getZone(String key) {
        return zones.get(key);
    }

    public List<String> findZones(IParticle particle, String prefix) {

        List<String> keys = new ArrayList();
        for (Zone zone : zones.values()) {
            String zprefix = zone.getKey().substring(0, zone.getKey().lastIndexOf("."));
            if (zprefix.equals(prefix) && isInside(particle, zone.getKey())) {
                keys.add(zone.getKey());
            }
        }
        return keys;
    }

    public List<String> findZones(IParticle particle) {

        List<String> keys = new ArrayList();
        for (Zone zone : zones.values()) {
            if (isInside(particle, zone.getKey())) {
                keys.add(zone.getKey());
            }
        }
        return keys;
    }

    @Override
    public void setupPerformed(SetupEvent e) throws Exception {
        /* Nothing to do. Zones are loaded by other classes such as Action
        or ReleaseProcess */
    }

    @Override
    public void initializePerformed(InitializeEvent e) throws Exception {

        for (Zone zone : zones.values()) {
            zone.init();
        }

        bathymetry = (BathymetryDataset) getSimulationManager().getDatasetManager().getDataset("dataset.bathymetry");

        info("[zones] Initialisation [OK]");
    }
}
