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
import javax.swing.table.AbstractTableModel;
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
public abstract class UndoableTableModel extends AbstractTableModel {

    private JUndoManager undoManager;

    public UndoableTableModel() {
        super();
        addUndoableEditListener(undoManager = new JUndoManager());
    }

    public JUndoManager getUndoManager() {
        if (undoManager == null) {
            addUndoableEditListener(undoManager = new JUndoManager());
        }
        return undoManager;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        setValueAt(value, row, column, true);
    }

    public void setValueAt(Object value, int row, int column, boolean undoable) {
        UndoableEditListener listeners[] = getListeners(UndoableEditListener.class);
        if (undoable == false || listeners == null) {
            super.setValueAt(value, row, column);
            return;
        }

        Object oldValue = getValueAt(row, column);
        super.setValueAt(value, row, column);
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
