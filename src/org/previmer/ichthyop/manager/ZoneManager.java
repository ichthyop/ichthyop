/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IZoneManager;
import org.previmer.ichthyop.io.XZone;
import org.previmer.ichthyop.io.XZone.XPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.previmer.ichthyop.io.ZoneFile;

/**
 *
 * @author pverley
 */
public class ZoneManager extends AbstractManager implements IZoneManager {

    private static ZoneManager zoneManager = new ZoneManager();
    private HashMap<TypeZone, ArrayList<Zone>> map;

    public static ZoneManager getInstance() {
        return zoneManager;
    }

    private ZoneManager() {
        super();
        map = new HashMap();
    }

    public void cleanup() {
        map.clear();
    }

    public void loadZonesFromFile(String filename, TypeZone type) throws Exception {

        File file = new File(System.getProperty("user.dir"));
        String pathname = new File(file.toURI().resolve(filename)).getAbsolutePath();

        File f = new File(pathname);
        if (!f.isFile()) {
            throw new FileNotFoundException("Zone file " + pathname + " not found.");
        }
        if (!f.canRead()) {
            throw new IOException("Zone file " + pathname + " cannot be read.");
        }

        ZoneFile zoneFile = new ZoneFile(f);
        if (!map.containsKey(type)) {
            map.put(type, new ArrayList());
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
                map.get(type).add(zone);
            }
        }
    }

    public ArrayList<Zone> getZones(TypeZone type) {
        return map.get(type);
    }

    public void setupPerformed(SetupEvent e) throws Exception {
        /* Nothing to do. Zones are loaded by other classes such as Action
        or ReleaseProcess */
    }

    public void initializePerformed(InitializeEvent e) throws Exception {

        for (List<Zone> listZone : map.values()) {
            for (Zone zone : listZone) {
                zone.init();
            }
        }
        getLogger().info("Zone manager initialization [OK]");
    }
}
