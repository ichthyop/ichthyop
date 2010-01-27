/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 *
 * @author pverley
 */
public class UndoableTableModel extends DefaultTableModel {

    private String[] columnNames = {"Name", "Value"};
    private JUndoManager undoManager;
    private Object[][] data = new String[][] {{""}, {""}};
    private Object[] longValues;

    public UndoableTableModel() {
        addUndoableEditListener(undoManager = new JUndoManager());
    }

    public void setData(Object[][] data) {
        this.data = data;
        longValues = getLongValues(data);
    }
    public Object[][] createData() {
        return new Object[][] {{""}, {""}};
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

    public JUndoManager getUndoManager() {
        return undoManager;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

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

    @Override
    public void setValueAt(Object value, int row, int column) {
        setValueAt(value, row, column, true);
    }

    public void setValueAt(Object value, int row, int column, boolean undoable) {
        UndoableEditListener listeners[] = getListeners(UndoableEditListener.class);
        if (undoable == false || listeners == null) {
            data[row][column] = String.valueOf(value);
            fireTableCellUpdated(row, column);
            return;
        }

        Object oldValue = getValueAt(row, column);
        data[row][column] = String.valueOf(value);
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

    public class JUndoManager extends UndoManager {

        protected Action undoAction;
        protected Action redoAction;

        public JUndoManager() {
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

    class JvCellEdit extends AbstractUndoableEdit {

        protected UndoableTableModel tableModel;
        protected Object oldValue;
        protected Object newValue;
        protected int row;
        protected int column;

        public JvCellEdit(UndoableTableModel tableModel, Object oldValue, Object newValue, int row, int column) {
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
}
