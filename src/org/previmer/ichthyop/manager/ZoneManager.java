/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import java.awt.Color;
import java.io.File;
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
import org.previmer.ichthyop.io.ParameterFormat;
import org.previmer.ichthyop.io.XParameter;
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

    public void loadZonesFromFile(String filename, TypeZone type) {
        ZoneFile zoneFile = new ZoneFile(new File(filename));
        if (!map.containsKey(type)) {
            map.put(type, new ArrayList());
        }
        for (XZone xzone : zoneFile.getZones()) {
            if (xzone.getTypeZone().equals(type)) {
                Zone zone = new Zone(xzone.getTypeZone(), xzone.getKey(), getNewIndex());
                zone.setOffshoreLine(xzone.getBathyMask().getOffshoreLine());
                zone.setInshoreLine(xzone.getBathyMask().getInshoreLine());
                zone.setLowerDepth(xzone.getThickness().getLowerDepth());
                zone.setUpperDepth(xzone.getThickness().getUpperDepth());
                zone.setColor(xzone.getColor());
                for (XPoint point : xzone.getPolygon()) {
                    zone.addPoint(point.createRhoPoint());
                }
                map.get(type).add(zone);
            }
        }
    }

    private int getNewIndex() {
        int index = 0;
        for (ArrayList<Zone> zones : map.values()) {
            index += zones.size();
        }
        return index;
    }

    public void init() {
        //loadZones();
        for (List<Zone> listZone : map.values()) {
            for (Zone zone : listZone) {
                zone.init();
            }
        }
    }

    /*public void loadZones() {

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
    zone.setLowerDepth(xzone.getThickness().getLowerDepth());
    zone.setUpperDepth(xzone.getThickness().getUpperDepth());
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
    }
    }*/
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
        /*cleanup();
        for (XBlock block : getSimulationManager().getParameterManager().readBlocks()) {
            if (block.isEnabled()) {
                for (XParameter param : block.getXParameters()) {
                    if (param.getFormat().equals(ParameterFormat.ZONEFILE)) {
                        for (TypeZone type : TypeZone.values()) {
                            getSimulationManager().getZoneManager().loadZonesFromFile(param.getValue(), type);
                        }
                    }
                }
            }
        }*/
    }

    public void initializePerformed(InitializeEvent e) {
        for (List<Zone> listZone : map.values()) {
            for (Zone zone : listZone) {
                zone.init();
            }
        }
    }
}
