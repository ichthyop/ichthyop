/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.util.Vector;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author pverley
 */
public class JMultiCellEditorsTable extends JXTable {

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
        if (rm != null) {
            tmpEditor = rm.getEditor(row);
        }
        if (tmpEditor != null) {
            return tmpEditor;
        }
        return super.getCellEditor(row, col);
    }

    
}
