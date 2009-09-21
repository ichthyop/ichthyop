/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.*;
import fr.ird.ichthyop.arch.IZoneManager;
import fr.ird.ichthyop.io.XZone;
import fr.ird.ichthyop.io.XZone.XPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author pverley
 */
public class ZoneManager implements IZoneManager {

    private static ZoneManager zoneManager = new ZoneManager();
    private HashMap<TypeZone, ArrayList<Zone>> map;

    ZoneManager() {
        loadZones();
    }

    public static ZoneManager getInstance() {
        return zoneManager;
    }

    private void loadZones() {

        Iterator<XZone> it = ICFile.getInstance().getZones().iterator();
        map = new HashMap();
        while (it.hasNext()) {
            XZone xzone = it.next();
            if (!map.containsKey(xzone.getType())) {
                map.put(xzone.getType(), new ArrayList());
            }
        }
        it = ICFile.getInstance().getZones().iterator();
        while (it.hasNext()) {
            XZone xzone = it.next();
            Zone zone = new Zone(xzone.getType(), xzone.getIndex());
            zone.setOffshoreLine(xzone.getBathyMask().getOffshoreLine());
            zone.setInshoreLine(xzone.getBathyMask().getInshoreLine());
            zone.setLowerDepth(xzone.getBathyMask().getLowerDepth());
            zone.setUpperDepth(xzone.getBathyMask().getUpperDepth());
            for (XPoint point : xzone.getPolygon()) {
                zone.addPoint(point.createRhoPoint());
            }
            map.get(zone.getType()).add(zone.getIndex(), zone);
        }
    }

    public ArrayList<Zone> getZones(TypeZone type) {
        return map.get(type);
    }
}
