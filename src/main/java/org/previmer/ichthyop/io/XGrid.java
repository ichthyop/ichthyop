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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.previmer.GridType;
import org.previmer.ichthyop.manager.SimulationManager;

/**
 * Class that manages XML tree for grid settings. Adapted from XZone.java. It
 * contains the settings for one specific grid.
 * 
 * @author Nicolas Barrier
 */
public class XGrid extends org.jdom2.Element  {
    
    private static final long serialVersionUID = 3940266835048156944L;
    
    /** Name of the grid children */
    final public static String GRID = "grid";
    
    /** Identifier of the grid */
    final public static String KEY = "key";
    
    /** Type of the grid (NEMO, ROMS, MARS, REGULAR, FVCOM) */
    final public static String TYPE = "type";
    
    /** Type of the grid (NEMO, ROMS, MARS, REGULAR, FVCOM) */
    final public static String ENABLED = "enabled";
    
    /** Specific grid parameters */
    final public static String PARAMETERS = "parameters";
    
    public List<XParameter> listParameters = new ArrayList<>();
    
    public XGrid(Element xzone) {
        super(GRID);
        if (xzone != null) {
            addContent(xzone.cloneContent());
        }
    }

    public XGrid(String key, String type) {
        super(GRID);
        setKey(key);
        setType(type.toUpperCase());
    }
    
    public List<XParameter> getParameters() { 
        return this.listParameters;   
    }
    
    public void setKey(String key) {
        if (null == getChild(KEY)) {
            addContent(new Element(KEY));
        }
        getChild(KEY).setText(key);
    }
    
    public void setType(String type) {
        if (null == getChild(TYPE)) {
            addContent(new Element(TYPE));
        }
        getChild(TYPE).setText(type);
    }
        
    public String getKey() {
        return getChildTextNormalize(KEY);
    }
    
    public GridType getType() {
        return GridType.valueOf(getChildTextNormalize(TYPE));
    }
    
    /** Enables or disable the current grid. */
    public void setEnabled(boolean enabled) {
        if (null == getChild(ENABLED)) {
            addContent(new Element(ENABLED));
        }
        getChild(ENABLED).setText(String.valueOf(enabled));
    }
    
    /** Returns whether the grid is enabled or not. */
    public boolean isEnabled() {
        if (null != getChild(ENABLED)) {
            return Boolean.valueOf(getChildTextNormalize(ENABLED));
        } else {
            return true;
        }
    }
    
    public void setParameters(String paramFile) {
        if(null != getChild(PARAMETERS)) { 
            return;
        } else {
           addContent(this.load(paramFile));
        }
        this.setListParameters();
    }
        
    private Element load(String file) {
        SAXBuilder sxb = new SAXBuilder();
        try {
            Element racine = sxb.build(file).getRootElement().clone();
            racine.detach();
            return racine;
        } catch (JDOMException | IOException e) {
            SimulationManager.getLogger().log(Level.SEVERE, null, e);
            return null;
        }
    }

    /** Get the list of parameters */
    public void setListParameters() { 
        for(Element element : getChild(PARAMETERS).getChildren()) {
            listParameters.add(new XParameter(element));   
        }

    }
            
    /** Add a parameter to the parameter list */
    public void addParameters(XParameter parameter) { 
        listParameters.add(parameter);
        getChild(PARAMETERS).addContent(parameter);   
    }
    
    /** Method to recover a parameter name based on the key value */
    public XParameter getParameter(String key) { 
        for(XParameter param : listParameters) {
            if(param.getKey().compareTo(key) == 0) {
                return param;   
            }
        }
        return null;       
    }
    
    
}
