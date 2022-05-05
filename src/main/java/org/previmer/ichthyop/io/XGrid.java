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
import org.jdom2.Element;

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
    
    public XGrid(Element xzone) {
        super(GRID);
        if (xzone != null) {
            addContent(xzone.cloneContent());
        }
    }

    public XGrid(String key) {
        super(GRID);
        setKey(key);
    }
    
    public void setKey(String key) {
        if (null == getChild(KEY)) {
            addContent(new Element(KEY));
        }
        getChild(KEY).setText(key);
    }
    
    public String getKey() {
        return getChildTextNormalize(KEY);
    }
    
}
