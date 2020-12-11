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

package org.previmer.ichthyop.io;

import java.io.FileNotFoundException;
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
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
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
            zones = new HashMap<>();
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
        } catch (JDOMException | IOException e) {
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
        org.jdom2.output.Format format = org.jdom2.output.Format.getPrettyFormat();
        format.setEncoding(System.getProperty("file.encoding"));
        XMLOutputter xmlOut = new XMLOutputter(format);
        xmlOut.output(structure, out);
    }

    private void removeAllZones() {
        structure.getRootElement().removeChildren(XZone.ZONE);
    }

    /*
    private Iterable getZones(TypeZone type) {
        ArrayList<XZone> list = new ArrayList<>();
        for (XZone xblock : zones.values()) {
            if (xblock.getTypeZone().equals(type)) {
                list.add(xblock);
            }
        }
        return list;
    }
    */

    public Collection<XZone> getZones() {
        List<XZone> list = new ArrayList<>(zones.values().size());
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

    private List<XZone> readZones() throws IOException {
        List<Element> list = structure.getRootElement().getChildren(XZone.ZONE);
        List<XZone> listBlock = new ArrayList<>(list.size());
        for (Element elt : list) {
            listBlock.add(new XZone(elt));
        }
        return listBlock;
    }

    /*
    private <E extends Content> List<XZone> readZones(final TypeZone type) {

        Filter<E> filtre = new Filter<E>() {

            private static final long serialVersionUID = -97784466558307682L;

            @Override
            public boolean matches(Object obj) {
                if (!(obj instanceof Element)) {
                    return false;
                }
                Element element = (Element) obj;
                return element.getChildTextNormalize(XZone.TYPE_ZONE).equals(type.toString());
            }

            @Override
            public List<E> filter(List<?> list) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public E filter(Object o) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Filter<E> negate() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Filter<E> or(Filter<?> filter) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Filter<E> and(Filter<?> filter) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public <R> Filter<R> refine(Filter<R> filter) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        
        List<XZone> list = new ArrayList<>();
        for (Content elt : structure.getRootElement().getContent(filtre)) {
            list.add(new XZone((Element) elt));
        }

        return list;
    }
    */

    private HashMap<String, XZone> createMap() throws IOException {
        HashMap<String, XZone> lmap = new HashMap<>();
        sortedKey = new ArrayList<>();
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

    public void addZone(String key) throws IOException {
        zones.put(key, new XZone(key));
    }

    public void updateKey(String oldKey, String newKey) {
        XZone zone = zones.get(oldKey);
        zones.remove(oldKey);
        zone.setKey(newKey);
        zones.put(newKey, zone);
    }
}
