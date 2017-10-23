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
package org.ichthyop.xml;

import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.ichthyop.IchthyopLinker;
import org.ichthyop.util.StringUtil;
import org.ichthyop.xml.XZone.XPoint;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

public class XZoneFile extends IchthyopLinker {

    private File file;
    private Document structure;
    private HashMap<String, XZone> zones;
    private final static String ZONES = "zones";
    private List<String> sortedKey;

    public XZoneFile(File file) {
        this.file = file;
        if (file.exists()) {
            load();
        } else {
            structure = new Document(new Element(ZONES));
            zones = new HashMap();

            try {
                save(new String[]{});
            } catch (IOException ex) {
                error("Error saving zone file " + file, ex);
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
            error("Error loading zone file " + file, e);
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

    public HashMap<String, String> toProperties(String prefix, boolean extended) throws IOException {

        HashMap<String, String> parameters = new LinkedHashMap();
        int index = 0;
        for (XZone zone : zones.values()) {
            String zkey = prefix + ".zone" + index;
            String name = zone.getKey().trim().isEmpty() ? "Zone" + index : zone.getKey().trim();
            parameters.put(zkey + ".name", name);
            parameters.put(zkey + ".enabled", String.valueOf(zone.isEnabled()));
            String[] lat = new String[zone.getPolygon().size()];
            String[] lon = new String[lat.length];
            int i = 0;
            for (XPoint point : zone.getPolygon()) {
                lat[i] = point.getLat();
                lon[i] = point.getLon();
                i++;
            }
            parameters.put(zkey + ".latitude", StringUtil.handleArray(lat));
            parameters.put(zkey + ".longitude", StringUtil.handleArray(lon));
            parameters.put(zkey + ".bathymetry.enabled", String.valueOf(zone.isBathyMaskEnabled()));
            parameters.put(zkey + ".bathymetry.inshore", StringUtil.nullify(String.valueOf(zone.getInshoreLine())));
            parameters.put(zkey + ".bathymetry.offshore", StringUtil.nullify(String.valueOf(zone.getOffshoreLine())));
            parameters.put(zkey + ".depth.enabled", String.valueOf(zone.isThicknessEnabled()));
            parameters.put(zkey + ".depth.lower", StringUtil.nullify(String.valueOf(zone.getLowerDepth())));
            parameters.put(zkey + ".depth.upper", StringUtil.nullify(String.valueOf(zone.getUpperDepth())));
            parameters.put(zkey + ".color", String.valueOf(zone.getColor().getRGB()));
            if (extended) {
                parameters.put(zkey + ".name.longname", "Name of the zone");
                parameters.put(zkey + ".name.description", "Name of the zone (alphanumerical characters only).");
                parameters.put(zkey + ".latitude.format", "lonlat");
                parameters.put(zkey + ".latitude.longname", "Polygon latitudes");
                parameters.put(zkey + ".latitude.description", "Vector of latitudes of the polygonal chain.");
                parameters.put(zkey + ".longitude.format", "lonlat");
                parameters.put(zkey + ".longitude.longname", "Polygon longitudes");
                parameters.put(zkey + ".longitude.description", "Vector of longitudes of the polygonal chain.");
                parameters.put(zkey + ".bathymetry.enabled.format", "boolean");
                parameters.put(zkey + ".bathymetry.enabled.longname", "Enabled bathymetry mask");
                parameters.put(zkey + ".bathymetry.enabled.description", "Whether to refine the zone definition by specifying inshore and offshore bathymetry lines.");
                parameters.put(zkey + ".bathymetry.inshore.format", "float");
                parameters.put(zkey + ".bathymetry.inshore.longname", "Inshore bathymetry line (meter)");
                parameters.put(zkey + ".bathymetry.inshore.description", "Inshore bathymetry line (meter) to refine the zone definition.");
                parameters.put(zkey + ".bathymetry.offshore.format", "float");
                parameters.put(zkey + ".bathymetry.offshore.longname", "Offshore bathymetry line (meter)");
                parameters.put(zkey + ".bathymetry.offshore.description", "Offshore bathymetry line (meter) to refine the zone definition.");
                parameters.put(zkey + ".depth.enabled.format", "boolean");
                parameters.put(zkey + ".depth.enabled.longname", "Crop zone vertical extension");
                parameters.put(zkey + ".depth.enabled.description", "Whether to crop the zone vertical extension in 3D simulation. By default the zone covers the whole water column.");
                parameters.put(zkey + ".depth.upper.format", "float");
                parameters.put(zkey + ".depth.upper.longname", "Upper depth (meter)");
                parameters.put(zkey + ".depth.upper.description", "Upper depth (close surface), in meter below surface level, of the zone.");
                parameters.put(zkey + ".depth.lower.format", "float");
                parameters.put(zkey + ".depth.lower.longname", "Lower depth (meter)");
                parameters.put(zkey + ".depth.lower.description", "Lower depth (close bottom), in meter below surface level, of the zone.");
                parameters.put(zkey + ".parameters", StringUtil.handleArray(new String[]{
                    "name",
                    "latitude", "longitude",
                    "bathymetry.enabled", "bathymetry.inshore", "bathymetry.offshore",
                    "depth.enabled", "depth.upper", "depth.lower"
                }));
                parameters.put(zkey + ".treepath", "Zones/" + prefix + "/zone" + index);
                parameters.put(zkey + ".description", "Zone definition linked to submodel " + prefix);
                String[] subsets = getConfiguration().getParameterSubsets();
                String[] newsubsets = Arrays.copyOf(subsets, subsets.length + 1);
                newsubsets[subsets.length] = zkey;
                parameters.put("configuration.subsets", StringUtil.handleArray(newsubsets));
            }
            index++;
        }

        return parameters;
    }
}
