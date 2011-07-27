package org.previmer.ichthyop.util;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.io.XZone;
import org.previmer.ichthyop.io.ZoneFile;
import org.previmer.ichthyop.v2.Configuration;
import org.previmer.ichthyop.v2.ZoneV2;

/**
 *
 * @author pverley
 */
public class ZoneConverter {

    private String cfgFileStr = "/home/pverley/ichthyop/dev/cluster/agruss/ichthyop.cfg.template";
    private String zoneFileStr = "/home/pverley/ichthyop/dev/cluster/agruss/newzonefile.xml";
    private String relZoneFileStr = "/home/pverley/ichthyop/dev/cluster/agruss/newrelzonefile.xml";
    private String recZoneFileStr = "/home/pverley/ichthyop/dev/cluster/agruss/newreczonefile.xml";
    private Random numGen = new MTRandom();

    ZoneConverter() {
        loadCfgFileV2(cfgFileStr);
        makeZoneFile(zoneFileStr);
        makeReleaseZoneFile(relZoneFileStr);
        makeRecruitmentZoneFile(recZoneFileStr);

    }

    public static void main(String[] args) {
        new ZoneConverter();
    }

    private void loadCfgFileV2(String filename) {
        try {
            new Configuration(new File(cfgFileStr));
        } catch (IOException ex) {
            Logger.getLogger(ZoneConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void makeZoneFile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        ZoneFile zoneFile = new ZoneFile(file);
        String[] keys = new String[Configuration.getReleaseZones().size() + Configuration.getRecruitmentZones().size()];
        int izone = 0;
        int ikey = 0;
        for (ZoneV2 zone : Configuration.getReleaseZones()) {
            String key = "release_zone_" + izone;
            keys[ikey++] = key;
            zoneFile.addZone(key);
            zone2xzone(zoneFile.getZone(key), zone);
            zoneFile.getZone(key).setColor(new Color(getColorIndex(izone, Configuration.getReleaseZones().size()), 100, 200));
            izone++;
        }
        izone = 0;
        for (ZoneV2 zone : Configuration.getRecruitmentZones()) {
            String key = "recruitment_zone_" + izone;
            keys[ikey++] = key;
            zoneFile.addZone(key);
            zone2xzone(zoneFile.getZone(key), zone);
            zoneFile.getZone(key).setColor(new Color(200, getColorIndex(izone, Configuration.getRecruitmentZones().size()), 100));
            izone++;
        }
        try {
            zoneFile.save(keys);
            System.out.println("Created file " + filename + " [OK]");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ZoneConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ZoneConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void makeReleaseZoneFile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        ZoneFile zoneFile = new ZoneFile(file);
        String[] keys = new String[Configuration.getReleaseZones().size()];
        int izone = 0;
        for (ZoneV2 zone : Configuration.getReleaseZones()) {
            String key = "release_zone_" + izone;
            keys[izone] = key;
            zoneFile.addZone(key);
            zone2xzone(zoneFile.getZone(key), zone);
            //zoneFile.getZone(key).setColor(new Color(getColorIndex(izone, Configuration.getReleaseZones().size()), 150, 0));
            zoneFile.getZone(key).setColor(getRandomColor());
            izone++;
        }
        try {
            zoneFile.save(keys);
            System.out.println("Created file " + filename + " [OK]");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ZoneConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ZoneConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void makeRecruitmentZoneFile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        ZoneFile zoneFile = new ZoneFile(file);
        String[] keys = new String[Configuration.getRecruitmentZones().size()];
        int izone = 0;
        for (ZoneV2 zone : Configuration.getRecruitmentZones()) {
            String key = "recruitment_zone_" + izone;
            keys[izone] = key;
            zoneFile.addZone(key);
            zone2xzone(zoneFile.getZone(key), zone);
            //zoneFile.getZone(key).setColor(new Color(200, getColorIndex(izone, Configuration.getRecruitmentZones().size()), 100));
            zoneFile.getZone(key).setColor(getRandomColor());
            izone++;
        }
        try {
            zoneFile.save(keys);
            System.out.println("Created file " + filename + " [OK]");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ZoneConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ZoneConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void zone2xzone(XZone xzone, ZoneV2 zone) {
        // set type
        switch (zone.getType()) {
            case (0):
                xzone.setType(TypeZone.RELEASE);
                break;
            case (1):
                xzone.setType(TypeZone.RECRUITMENT);
                break;
            case (2):
                xzone.setType(TypeZone.ORIENTATION);
                break;
        }
        // add points
        xzone.cleanupPolygon();
        for (int i = 0; i < 4; i++) {
            xzone.addPoint(i, String.valueOf(zone.getLon(i)), String.valueOf(zone.getLat(i)));
        }
        // set bathymask
        xzone.setBathyMaskEnabled(true);
        xzone.setInshoreLine(zone.getBathyMin());
        xzone.setOffshoreLine(zone.getBathyMax());
        // unabled thickness
        xzone.setThicknessEnabled(false);
    }

    private int getColorIndex(int izone, int nbZones) {
        return (int) (((float) (izone + 1.f) / (float) nbZones) * 255);
    }

    private Color getRandomColor() {
        return new Color(numGen.nextInt(256), numGen.nextInt(256), numGen.nextInt(256));
    }
}
