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

import java.util.Vector;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *
 * @author pverley
 */
public class JMultiCellEditorsTable extends JTable {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * TableCellEditor customized to accept one JComboBox per cell (and not
     * just one per column as for the basic JTable).
     * @see ichthyop.ui.RowEditorModel
     */
    protected RowEditorModel rm;

///////////////
// Constructors
///////////////
    /**
     * See constructor JTable()
     */
    public JMultiCellEditorsTable() {
        super();
        rm = null;
    }

    /**
     * See constructor JTable(TableModel tm)
     */
    public JMultiCellEditorsTable(TableModel tm) {
        super(tm);
        rm = null;
    }

    /**
     * See constructor JTable(TableModel tm, TableColumnModel cm)
     */
    public JMultiCellEditorsTable(TableModel tm, TableColumnModel cm) {
        super(tm, cm);
        rm = null;
    }

    /**
     * See constructor JTable(TableModel tm, TableColumnModel cm,
    ListSelectionModel sm)
     */
    public JMultiCellEditorsTable(TableModel tm, TableColumnModel cm,
            ListSelectionModel sm) {
        super(tm, cm, sm);
        rm = null;
    }

    /**
     * See constructor JTable(int rows, int cols)
     */
    public JMultiCellEditorsTable(int rows, int cols) {
        super(rows, cols);
        rm = null;
    }

    /**
     * See constructor JTable(final Vector rowData, final Vector columnNames)
     */
    public JMultiCellEditorsTable(final Vector rowData, final Vector columnNames) {
        super(rowData, columnNames);
        rm = null;
    }

    /**
     * See constructor JTable(final Object[][] rowData, final Object[] colNames)
     */
    public JMultiCellEditorsTable(final Object[][] rowData, final Object[] colNames) {
        super(rowData, colNames);
        rm = null;
    }

    /**
     * This is the only new constructor compared to extended class
     * <code>JTable</code>.
     * Constructs a <code>JTableCbBox</code> with <code>numRows</code>
     * and <code>numColumns</code> of empty cells, with the specified
     * <code>RowEditorModel</code>.
     *
     * @param numRows the number of rows the table holds
     * @param numColumns the number of columns the table holds
     */
    public JMultiCellEditorsTable(TableModel tm, RowEditorModel rm) {
        super(tm, null, null);
        this.rm = rm;
    }

////////////////////////////
// Definition of the methods
////////////////////////////
    /**
     * Sets the <code>RowEditorModel</code>.
     * @param rm the <code>RowEditorModel</code> for the table.
     */
    public void setRowEditorModel(RowEditorModel rm) {
        this.rm = rm;
    }

    /**
     * Gets the <code>RowEditorModel</code>.
     * @return the <code>RowEditorModel</code> of the table.
     */
    public RowEditorModel getRowEditorModel() {
        return rm;
    }

    /**
     * Returns an appropriate editor for the cell specified by
     * <code>row</code> and <code>column</code>. If the
     * <code>RowEditorModel</code> is non-null and the cell editor at specified
     * row is non-null either, returns that. Otherwise calls the parent
     * <code>getCellEditor</code> method.
     *
     * @param row an int, the row of the cell to edit
     * @param column an int, the column of the cell to edit
     * @return TableCellEditor, the editor for this cell;

     * @see javax.swing.JTable#getCellEditor
     */
    @Override
    public TableCellEditor getCellEditor(int row, int col) {

        TableCellEditor tmpEditor = null;
        int modelRow = convertRowIndexToModel(row);
        if (rm != null) {
            tmpEditor = rm.getEditor(modelRow);
        }
        if (tmpEditor != null) {
            return tmpEditor;
        }
        // In JTable getCellEditor only reads column number
        return super.getCellEditor(row, col);
    }

    
}
