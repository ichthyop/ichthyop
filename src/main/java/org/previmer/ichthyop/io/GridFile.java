/*
 *ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 *http://www.ichthyop.org
 *
 *Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-today
 *http://www.ird.fr
 *
 *Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 *Contributors (alphabetically sorted):
 *Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 *Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 *Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 *Stephane POUS, Nathan PUTMAN.
 *
 *Ichthyop is a free Java tool designed to study the effects of physical and
 *biological factors on ichthyoplankton dynamics. It incorporates the most
 *important processes involved in fish early life: spawning, movement, growth,
 *mortality and recruitment. The tool uses as input time series of velocity,
 *temperature and salinity fields archived from oceanic models such as NEMO,
 *ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 *generates output files that can be post-processed easily using graphic and
 *statistical software.
 *
 *To cite Ichthyop, please refer to Lett et al. 2008
 *A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 *Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 *doi:10.1016/j.envsoft.2008.02.005
 *
 *This program is free software: you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation (version 3 of the License). For a full
 *description, see the LICENSE file.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program.  If not, see <https://www.gnu.org/licenses/>.
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


/**
 * Class that manages the different grid used in the model. Adpated from
 * ZoneFile.java. It contains the settings for different grids defined. 
 * 
 * It uses XML libraries
 * 
 */
public class GridFile {
    
    private File file;
    private Document structure;
    private HashMap<String, XGrid> grids;
    private final static String GRID = "grids";
    private List<String> sortedKey;
    
    /**
     * Grid constructor. If the file exists, the XML file is loaded.
     * 
     * Else, an empty "grids" object is created (i.e. no grid defined).
     *
     */
    public GridFile(File file) {
        this.file = file;
        if (file.exists()) {
            load();
        } else {
            structure = new Document(new Element(GRID));
            grids = new HashMap<>();
            try {
                save(new String[]{});
            } catch (FileNotFoundException ex) {
                SimulationManager.getLogger().log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                SimulationManager.getLogger().log(Level.SEVERE, null, ex);
            }
        }
    }

    /** File getter */
    public File getFile() {
        return file;
    }

    /** File setter */
    public void setFile(File file) {
        this.file = file;
    }
            
    /** Load an XML file and regenerate the structure of the grids architecture. */
    void load() {

        SAXBuilder sxb = new SAXBuilder();
        try {
            Element racine = sxb.build(file).getRootElement();
            racine.detach();
            structure = new Document(racine);
            grids = createMap();
        } catch (JDOMException | IOException e) {
            SimulationManager.getLogger().log(Level.SEVERE, null, e);
        }
    }

    public void save(String[] keys) throws FileNotFoundException, IOException {
        removeAllGrids();
        for (String key : keys) {
            addGrid(grids.get(key));
        }

        write(new FileOutputStream(file));
    }
    
    /**
     * Remove all the "grid" child of the current "grids" tree.
     */
    private void removeAllGrids() {
        structure.getRootElement().removeChildren(XGrid.GRID);
    }
    
    /**
     * Add a "grid" child to the current "grids" tree.
     */ 
    private void addGrid(XGrid grid) {
        structure.getRootElement().addContent(grid.detach());
    }
    
    /**
     * Add an empty "grid" child to the current "grids" tree.
     */
    public void addGrid(String key, String type) {
        grids.put(key, new XGrid(key, type));
    }
    
    /** Create the "grids" HashMap object. */
    private HashMap<String, XGrid> createMap() {
        HashMap<String, XGrid> lmap = new HashMap<>();
        sortedKey = new ArrayList<>();
        for (XGrid xgrid : readGrids()) {
            sortedKey.add(xgrid.getKey());
            lmap.put(xgrid.getKey(), xgrid);
        }
        return lmap;
    }
    
    /**
     * Reads the XML structure (loaded from file) and returns a list of XGrid
     * objects
     */
    private List<XGrid> readGrids() {
        List<Element> list = structure.getRootElement().getChildren(XGrid.GRID);
        List<XGrid> listBlock = new ArrayList<>(list.size());
        for (Element elt : list) {
            listBlock.add(new XGrid(elt));
        }
        return listBlock;
    }
    
    /** Write the XML file */
    private void write(OutputStream out) throws IOException {
        org.jdom2.output.Format format = org.jdom2.output.Format.getPrettyFormat();
        //format.setEncoding(System.getProperty("file.encoding"));
        XMLOutputter xmlOut = new XMLOutputter(format);
        xmlOut.output(structure, out);
    }
    
    /** Get the grid object based and convert it into a list */
    public List<XGrid> getGrids() {
        List<XGrid> list = new ArrayList<>(grids.values().size());
        if (null != sortedKey) {
            Iterator<String> it = sortedKey.iterator();
            while (it.hasNext()) {
                list.add(grids.get(it.next()));
            }
        }
        return list;
    }
    
    /** Returns getter for a specific grid, based from key */
    public XGrid getGrid(String key) {
        return grids.get(key);
    }
    
}
