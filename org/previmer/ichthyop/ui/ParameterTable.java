/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;

/**
 *
 * @author pverley
 */
public class ParameterTable extends JTable {

    ///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * TableCellEditor customized to accept one JComboBox per cell (and not
     * just one per column as for the basic JTable).
     * @see ichthyop.ui.RowEditorModel
     */
    protected RowEditorModel rm;
    /**
     *
     */
    private ParameterTableModel model = new ParameterTableModel();
    /**
     *
     */
    private ParameterCellRenderer renderer = new ParameterCellRenderer();

///////////////
// Constructors
///////////////
    /**
     * See constructor JTable()
     */
    public ParameterTable() {
        super();
        rm = null;
    }

    /**
     * See constructor JTable(TableModel tm)
     */
    public ParameterTable(TableModel tm) {
        super(tm);
        rm = null;
    }

    /**
     * See constructor JTable(TableModel tm, TableColumnModel cm)
     */
    public ParameterTable(TableModel tm, TableColumnModel cm) {
        super(tm, cm);
        rm = null;
    }

    /**
     * See constructor JTable(TableModel tm, TableColumnModel cm,
    ListSelectionModel sm)
     */
    public ParameterTable(TableModel tm, TableColumnModel cm,
            ListSelectionModel sm) {
        super(tm, cm, sm);
        rm = null;
    }

    /**
     * See constructor JTable(int rows, int cols)
     */
    public ParameterTable(int rows, int cols) {
        super(rows, cols);
        rm = null;
    }

    /**
     * See constructor JTable(final Vector rowData, final Vector columnNames)
     */
    public ParameterTable(final Vector rowData, final Vector columnNames) {
        super(rowData, columnNames);
        rm = null;
    }

    /**
     * See constructor JTable(final Object[][] rowData, final Object[] colNames)
     */
    public ParameterTable(final Object[][] rowData, final Object[] colNames) {
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
    public ParameterTable(TableModel tm, RowEditorModel rm) {
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

    @Override
    public ParameterTableModel getModel() {
        return model;
    }

    public JvUndoManager getUndoManager() {
        return getModel().getUndoManager();
    }

    public void setModel(XBlock block, TableModelListener l) {
        setModel(model = new ParameterTableModel(block));
        setEditors(block);
        setDefaultRenderer(Object.class, renderer);
        model.addTableModelListener(l);
        adjustColumnSizes();
    }

    private void setEditors(XBlock block) {

        RowEditorModel editorModel = new RowEditorModel();
        setRowEditorModel(editorModel);

        for (int row = 0; row < model.getRowCount(true); row++) {
            XParameter param = block.getXParameter(model.getParameterKey(row));
            switch (param.getFormat()) {
                case LIST:
                    editorModel.addEditorForRow(row, new DefaultCellEditor(new JComboBox(param.getAcceptedValues())));
                    break;
                case INTEGER:
                    editorModel.addEditorForRow(row, new IntegerEditor());
                    break;
                case FLOAT:
                    editorModel.addEditorForRow(row, new FloatEditor());
                    break;
                case BOOLEAN:
                    editorModel.addEditorForRow(row, new DefaultCellEditor(new JComboBox(new String[]{"true", "false"})));
                    break;
                case DURATION:
                    editorModel.addEditorForRow(row, new DurationEditor());
                    break;
                case DATE:
                    editorModel.addEditorForRow(row, new DateEditor());
                    break;
                case FILE:
                    editorModel.addEditorForRow(row, new FileEditor(JFileChooser.FILES_ONLY));
                    break;
                case PATH:
                    editorModel.addEditorForRow(row, new FileEditor(JFileChooser.DIRECTORIES_ONLY));
                    break;
            }
        }
    }

    /*
     * This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     */
    public void adjustColumnSizes() {

        getModel();
        if (!(getModel().getRowCount() > 0)) {
            return;
        }
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        Object[] longValues = getModel().getLongValues();
        TableCellRenderer headerRenderer = getTableHeader().getDefaultRenderer();

        for (int i = 0; i < getModel().getColumnCount(); i++) {
            column = getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                    null, column.getHeaderValue(),
                    false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = getDefaultRenderer(getColumnClass(i)).getTableCellRendererComponent(this, longValues[i], false, false, 0, i);
            cellWidth = comp.getPreferredSize().width;

            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

    public class ParameterTableModel extends DefaultTableModel {

        private String[] columnNames = {"Name", "Value"};
        private Object[][] data;
        private JvUndoManager undoManager;
        private Object[] longValues;
        private int visibleRows = 1, allRows = 1;
        private boolean isAllRowsVisible;
        private XBlock block;

        ParameterTableModel() {
            data = new Object[1][1];
            longValues = new String[]{"", ""};
            isAllRowsVisible = false;
        }

        ParameterTableModel(XBlock block) {
            isAllRowsVisible = false;
            this.block = block;
            data = createData(block);
            longValues = getLongValues(data);
            addUndoableEditListener(undoManager = new JvUndoManager());
        }

        public JvUndoManager getUndoManager() {
            return undoManager;
        }

        public void setAllRowsVisible(boolean visible) {
            isAllRowsVisible = visible;
            fireTableChanged(null);
        }

        /**
         *
         * @param model int
         * @return Object[][]
         */
        private Object[][] createData(XBlock block) {

            Collection<XParameter> list = block.getXParameters();
            List<String[]> listData = new ArrayList();
            allRows = list.size();
            visibleRows = allRows - block.getNbHiddenParameters();
            String[][] tableData = new String[list.size()][2];
            int i = 0;
            for (XParameter xparam : list) {
                xparam.reset();
                if (xparam.hasNext()) {
                    listData.add(new String[]{xparam.getKey() + " [1]", xparam.getValue()});
                    while (xparam.hasNext()) {
                        xparam.increment();
                        listData.add(new String[]{xparam.getKey() + " [" + (xparam.index() + 1) + "]", xparam.getValue()});
                    }
                } else {
                    listData.add(new String[]{xparam.getKey(), xparam.getValue()});
                }
            }
            allRows = listData.size();
            visibleRows = allRows - block.getNbHiddenParameters();
            tableData = new String[listData.size()][2];
            for (String[] arr : listData) {
                tableData[i++] = arr;
            }
            return tableData;

        }

        public Object[] getLongValues() {
            return longValues;
        }

        /**
         *
         * @param data Object[][]
         * @return Object[]
         */
        private Object[] getLongValues(Object[][] data) {

            String[] longuest = new String[2];
            for (int j = 0; j < 2; j++) {
                longuest[j] = "";
                for (int i = 0; i < data.length; i++) {
                    String value = (String) data[i][j];
                    if (value != null && (value.length() > longuest[j].length())) {
                        longuest[j] = value;
                    }
                }
            }
            return longuest;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return getRowCount(this.isAllRowsVisible);
        }

        public int getRowCount(boolean isAllRowsVisible) {
            return isAllRowsVisible ? allRows : visibleRows;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        public String getParameterKey(int row) {
            String key = "";
            if (row >= 0) {
                key = getValueAt(row, 0).toString();
                if (key.trim().endsWith("]")) {
                    key = key.substring(0, key.lastIndexOf("[")).trim();
                }
            }
            return key;
        }

        public int getParameterIndex(int row) {
            if (row >= 0) {
                String key = getValueAt(row, 0).toString();
                if (key.trim().endsWith("]")) {
                    return Integer.valueOf(key.substring(key.lastIndexOf("[") + 1, key.lastIndexOf("]")).trim()) - 1;
                }
            }
            return 0;
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        @Override
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col == 1) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            setValueAt(value, row, column, true);
        }

        public void setValueAt(Object value, int row, int column, boolean undoable) {
            UndoableEditListener listeners[] = getListeners(UndoableEditListener.class);
            if (undoable == false || listeners == null) {
                data[row][column] = value.toString();
                fireTableCellUpdated(row, column);
                return;
            }

            Object oldValue = getValueAt(row, column);
            data[row][column] = value.toString();
            fireTableCellUpdated(row, column);
            JvCellEdit cellEdit = new JvCellEdit(this, oldValue, value, row, column);
            UndoableEditEvent editEvent = new UndoableEditEvent(this, cellEdit);
            for (UndoableEditListener listener : listeners) {
                listener.undoableEditHappened(editEvent);
            }
        }

        public void addUndoableEditListener(UndoableEditListener listener) {
            listenerList.add(UndoableEditListener.class, listener);
        }
    }

    class JvCellEdit extends AbstractUndoableEdit {

        protected ParameterTableModel tableModel;
        protected Object oldValue;
        protected Object newValue;
        protected int row;
        protected int column;

        public JvCellEdit(ParameterTableModel tableModel, Object oldValue, Object newValue, int row, int column) {
            this.tableModel = tableModel;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.row = row;
            this.column = column;
        }

        @Override
        public String getPresentationName() {
            return "Cell Edit";
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();

            tableModel.setValueAt(oldValue, row, column, false);
        }

        @Override
        public void redo() throws CannotUndoException {
            super.redo();

            tableModel.setValueAt(newValue, row, column, false);
        }
    }

    class ParameterCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row,
                int column) {

            Component comp = super.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);

            ParameterTableModel model = (ParameterTableModel) table.getModel();
            XParameter param = model.block.getXParameter(model.getParameterKey(row));
            if (null != param) {
                if (param.isHidden()) {
                    comp.setBackground(new Color(255, 0, 0, 20));
                } else if (param.isSerial()) {
                    comp.setBackground(new Color(0, 255, 0, 20));
                } else {
                    comp.setBackground(Color.WHITE);
                }
            }

            return comp;
        }
    }

    public class JvUndoManager extends UndoManager {

        protected Action undoAction;
        protected Action redoAction;

        public JvUndoManager() {
            this.undoAction = new JvUndoAction(this);
            this.redoAction = new JvRedoAction(this);

            synchronizeActions();           // to set initial names
        }

        public Action getUndoAction() {
            return undoAction;
        }

        public Action getRedoAction() {
            return redoAction;
        }

        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            try {
                return super.addEdit(anEdit);
            } finally {
                synchronizeActions();
            }
        }

        @Override
        protected void undoTo(UndoableEdit edit) throws CannotUndoException {
            try {
                super.undoTo(edit);
            } finally {
                synchronizeActions();
            }
        }

        @Override
        protected void redoTo(UndoableEdit edit) throws CannotRedoException {
            try {
                super.redoTo(edit);
            } finally {
                synchronizeActions();
            }
        }

        protected void synchronizeActions() {
            undoAction.setEnabled(canUndo());
            undoAction.putValue(Action.NAME, getUndoPresentationName());

            redoAction.setEnabled(canRedo());
            redoAction.putValue(Action.NAME, getRedoPresentationName());
        }
    }

    class JvUndoAction extends AbstractAction {

        protected final UndoManager manager;

        public JvUndoAction(UndoManager manager) {
            this.manager = manager;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                manager.undo();
            } catch (CannotUndoException ex) {
                ex.printStackTrace();
            }
        }
    }

    class JvRedoAction extends AbstractAction {

        protected final UndoManager manager;

        public JvRedoAction(UndoManager manager) {
            this.manager = manager;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                manager.redo();
            } catch (CannotRedoException ex) {
                ex.printStackTrace();
            }
        }
    }
}
