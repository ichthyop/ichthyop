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
    private Hashtable data;

///////////////
// Constructors
///////////////

    /**
     * Constructs an empty <code>RowEditorModel</code>.
     */
    public RowEditorModel() {

        data = new Hashtable();
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

        data.put(new Integer(row), e);
    }

    /**
     *  Removes the <code>TableCellEditor</code> for the specified
     * <code>row</code>.
     * @param row an int, the number of the row for which the
     * <code>TableCellEditor</code> should be removed.
     */
    public void removeEditorForRow(int row) {

        data.remove(new Integer(row));
    }

    /**
     * Gets the <code>TableCellEditor</code> associated to the specified row.
     * @param row an int, the number of the row that holds the
     * <code>TableCellEditor</code>
     * @return the TableCellEditor associated to the specified row.
     */
    public TableCellEditor getEditor(int row) {

        return (TableCellEditor) data.get(new Integer(row));
    }

    //---------- End of class
}
