/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
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

    ParameterTableModel model = new ParameterTableModel();

    public ParameterTable() {
        super(new DefaultTableModel());
    }

    @Override
    public ParameterTableModel getModel() {
        return model;
    }

    public JvUndoManager getUndoManager() {
        return getModel().getUndoManager();
    }

    public void setModel(XBlock block, boolean includeHiddenParameters, TableModelListener l) {
        setModel(model = new ParameterTableModel(block, includeHiddenParameters));
        model.addTableModelListener(l);
        adjustColumnSizes();
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

    public class ParameterTableModel extends AbstractTableModel {

        private String[] columnNames = {"Name", "Value"};
        private Object[][] data;
        private JvUndoManager undoManager;
        private Object[] longValues;

        ParameterTableModel() {
            data = new Object[1][1];
            longValues = new String[] {"", ""};
        }

        ParameterTableModel(XBlock block, boolean showHidden) {
            data = createData(block, showHidden);
            longValues = getLongValues(data);
            addUndoableEditListener(undoManager = new JvUndoManager());
        }

        public JvUndoManager getUndoManager() {
            return undoManager;
        }

        /**
         *
         * @param model int
         * @return Object[][]
         */
        private Object[][] createData(XBlock block, boolean includeHiddenParameters) {

            Collection<XParameter> list = block.getXParameters();
            if (includeHiddenParameters) {
                list.addAll(block.getXParameters(true));
            }
            String[][] tableData = new String[list.size()][4];
            int i = 0;
            for (XParameter xparam : list) {
                tableData[i++] = new String[]{xparam.getKey(), xparam.getValues(), String.valueOf(xparam.isHidden()), String.valueOf(xparam.isSerial())};
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

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
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
                data[row][column] = value;
                fireTableCellUpdated(row, column);
                return;
            }

            Object oldValue = getValueAt(row, column);
            data[row][column] = value;
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
            Boolean hidden = Boolean.valueOf(model.getValueAt(row, 2).toString());
            Boolean serial = Boolean.valueOf(model.getValueAt(row, 3).toString());
            if (hidden) {
                comp.setBackground(new Color(255, 0, 0, 20));
            } else if (serial) {
                comp.setBackground(new Color(0, 255, 0, 20));
            } else {
                comp.setBackground(Color.WHITE);
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
