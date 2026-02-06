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
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
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

package org.previmer.ichthyop.ui;

/** import java.util */
import java.util.Hashtable;

/** import Swing */
import javax.swing.table.TableCellEditor;

/**
 * The class is a <code>HastTable</code> that stores
 * the <code>TableCellEditor</code>s of the <code>JTableCbBox</code> object.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see ichthyop.ui.JTableCbBox
 */
public class RowEditorModel {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * The <code>HastTable</code> that stores the
     * <code>TableCellEditor</code>s.
     */
    private Hashtable<Integer, TableCellEditor>  data;

///////////////
// Constructors
///////////////

    /**
     * Constructs an empty <code>RowEditorModel</code>.
     */
    public RowEditorModel() {

        data = new Hashtable<>();
    }

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Adds the given <code>TableCellEditor</code> for the specified
     * <code>row</code>.
     * @param row an int, the number of the row that will hold the given
     * editor.
     * @param e the TableCellEditor for the specified row.
     */
    public void addEditorForRow(int row, TableCellEditor e) {

        data.put(Integer.valueOf(row), e);
    }

    /**
     *  Removes the <code>TableCellEditor</code> for the specified
     * <code>row</code>.
     * @param row an int, the number of the row for which the
     * <code>TableCellEditor</code> should be removed.
     */
    public void removeEditorForRow(int row) {

        data.remove(Integer.valueOf(row));
    }

    /**
     * Gets the <code>TableCellEditor</code> associated to the specified row.
     * @param row an int, the number of the row that holds the
     * <code>TableCellEditor</code>
     * @return the TableCellEditor associated to the specified row.
     */
    public TableCellEditor getEditor(int row) {

        return (TableCellEditor) data.get(Integer.valueOf(row));
    }

    //---------- End of class
}
