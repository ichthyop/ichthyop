/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import java.awt.Color;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IZoneManager;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XZone;
import org.previmer.ichthyop.io.XZone.XPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
        loadZones();
    }

    public void init() {
        loadZones();
        for (List<Zone> listZone : map.values()) {
            for (Zone zone : listZone) {
                zone.init();
            }
        }
    }

    public void loadZones() {

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
            zone.setColor(xzone.getColor());
            for (XPoint point : xzone.getPolygon()) {
                zone.addPoint(point.createRhoPoint());
            }
            //zone.init();
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
        for (XBlock block : getSimulationManager().getParameterManager().getBlocks(BlockType.ZONE)) {
            if (block.isEnabled()) {
                collection.add(new XZone(block));
            }

        }
        return collection;
    }

    public ArrayList<Zone> getZones(TypeZone type) {
        return map.get(type);
    }

    public void setupPerformed(SetupEvent e) {
        // do nothing
    }

    public void initializePerformed(InitializeEvent e) {
        for (List<Zone> listZone : map.values()) {
            for (Zone zone : listZone) {
                zone.init();
            }
        }
    }
}
