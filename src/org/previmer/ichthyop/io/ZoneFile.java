/*
 *  Copyright (C) 2010 Philippe Verley <philippe dot verley at ird dot fr>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.io;

import java.io.FileNotFoundException;
import org.previmer.ichthyop.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.previmer.ichthyop.manager.SimulationManager;

public class ZoneFile {

    private File file;
    private Document structure;
    private HashMap<String, XZone> zones;
    private final static String ZONES = "zones";
    private List<String> sortedKey;

    public ZoneFile(File file) {
        this.file = file;
        if (file.exists()) {
            load();
        } else {
            structure = new Document(new Element(ZONES));
            zones = new HashMap();
            try {
                save(new String[]{});
            } catch (FileNotFoundException ex) {
                SimulationManager.getLogger().log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                SimulationManager.getLogger().log(Level.SEVERE, null, ex);
            }
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    void load() {

        SAXBuilder sxb = new SAXBuilder();
        try {
            Element racine = sxb.build(file).getRootElement();
            racine.detach();
            structure = new Document(racine);
            zones = createMap();
        } catch (Exception e) {
            SimulationManager.getLogger().log(Level.SEVERE, null, e);
        }
    }

    public void save(String[] keys) throws FileNotFoundException, IOException {
        removeAllZones();
        for (String key : keys) {
            addZone(zones.get(key));
        }

        write(new FileOutputStream(file));
    }

    private void write(OutputStream out) throws IOException {
        org.jdom.output.Format format = org.jdom.output.Format.getPrettyFormat();
        format.setEncoding(System.getProperty("file.encoding"));
        XMLOutputter xmlOut = new XMLOutputter(format);
        xmlOut.output(structure, out);
    }

    private void removeAllZones() {
        structure.getRootElement().removeChildren(XZone.ZONE);
    }

    private Iterable getZones(TypeZone type) {
        ArrayList<XZone> list = new ArrayList();
        for (XZone xblock : zones.values()) {
            if (xblock.getTypeZone().equals(type)) {
                list.add(xblock);
            }
        }
        return list;
    }

    public Collection<XZone> getZones() {
        List<XZone> list = new ArrayList(zones.values().size());
        if (null != sortedKey) {
            Iterator<String> it = sortedKey.iterator();
            while (it.hasNext()) {
                list.add(zones.get(it.next()));
            }
        }
        return list;
    }

    public XZone getZone(String key) {
        return zones.get(key);
    }

    private List<XZone> readZones() {
        List<Element> list = structure.getRootElement().getChildren(XZone.ZONE);
        List<XZone> listBlock = new ArrayList(list.size());
        for (Element elt : list) {
            listBlock.add(new XZone(elt));
        }
        return listBlock;
    }

    private List<XZone> readZones(final TypeZone type) {

        Filter filtre = new Filter() {

            public boolean matches(Object obj) {
                if (!(obj instanceof Element)) {
                    return false;
                }
                Element element = (Element) obj;
                if (element.getChildTextNormalize(XZone.TYPE_ZONE).equals(type.toString())) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        List<XZone> list = new ArrayList();
        for (Object elt : structure.getRootElement().getContent(filtre)) {
            list.add(new XZone((Element) elt));
        }
        return list;
    }

    private HashMap<String, XZone> createMap() {
        HashMap<String, XZone> lmap = new HashMap();
        sortedKey = new ArrayList();
        for (XZone xzone : readZones()) {
            sortedKey.add(xzone.getKey());
            lmap.put(xzone.getKey(), xzone);
        }
        return lmap;
    }

    private void addZone(XZone zone) {
        //zone.prepairForWriting();
        structure.getRootElement().addContent(zone.detach());
    }

    public void removeZone(String key) {
        zones.remove(key);
    }

    public void addZone(String key) {
        zones.put(key, new XZone(key));
    }

    public void updateKey(String oldKey, String newKey) {
        XZone zone = zones.get(oldKey);
        zones.remove(oldKey);
        zone.setKey(newKey);
        zones.put(newKey, zone);
    }
}

