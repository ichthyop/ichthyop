/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.io.TypeBlock;
import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.*;
import fr.ird.ichthyop.arch.IZoneManager;
import fr.ird.ichthyop.io.XBlock;
import fr.ird.ichthyop.io.XZone;
import fr.ird.ichthyop.io.XZone.XPoint;
import java.util.ArrayList;
import java.util.Collection;
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

        Iterator<XZone> it = getZones().iterator();
        map = new HashMap();
        while (it.hasNext()) {
            XZone xzone = it.next();
            if (!map.containsKey(xzone.getTypeZone())) {
                map.put(xzone.getTypeZone(), new ArrayList());
            }
        }
        it = getZones().iterator();
        while (it.hasNext()) {
            XZone xzone = it.next();
            Zone zone = new Zone(xzone.getTypeZone(), xzone.getIndex());
            zone.setOffshoreLine(xzone.getBathyMask().getOffshoreLine());
            zone.setInshoreLine(xzone.getBathyMask().getInshoreLine());
            zone.setLowerDepth(xzone.getBathyMask().getLowerDepth());
            zone.setUpperDepth(xzone.getBathyMask().getUpperDepth());
            for (XPoint point : xzone.getPolygon()) {
                zone.addPoint(point.createRhoPoint());
            }
            zone.setUp();
            map.get(zone.getType()).add(zone.getIndex(), zone);
        }

        /*for (TypeZone type : map.keySet()) {
            System.out.println(type.toString());
            for (Zone zone : map.get(type)) {
                System.out.println(zone.toString());
            }
        }*/
    }

    private Collection<XZone> getZones() {
        Collection<XZone> collection = new ArrayList();
        for (XBlock block : ICFile.getInstance().getBlocks(TypeBlock.ZONE)) {
            collection.add(new XZone(block));

        }
        return collection;
    }

    public ArrayList<Zone> getZones(TypeZone type) {
        return map.get(type);
    }
}
