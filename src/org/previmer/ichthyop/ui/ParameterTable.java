/*
 *  Copyright (C) 2010 Philippe Verley <philippe dot verley at ird dot fr>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.previmer.ichthyop.calendar.Calendar1900;
import org.previmer.ichthyop.calendar.ClimatoCalendar;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;
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
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class ParameterTable extends JMultiCellEditorsTable {

    ///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     *
     */
    private ParameterTableModel model;

///////////////
// Constructors
///////////////

    /*
     * No constructor needed
     */
////////////////////////////
// Definition of the methods
////////////////////////////
    @Override
    public TableModel getModel() {
        return model == null
                ? new DefaultTableModel()
                : model;
    }

    public UndoManager getUndoManager() {
        return model.getUndoManager();
    }

    public void setModel(XBlock block, TableModelListener l) {
        getModel().removeTableModelListener(l);
        setModel(model = new ParameterTableModel(block));
        setEditors(block);
        setAllRowsVisible(false);
        getModel().addTableModelListener(l);
    }

    private void setEditors(XBlock block) {

        RowEditorModel editorModel = new RowEditorModel();
        setRowEditorModel(editorModel);

        if (block.getKey().matches("app.time")) {
            setupDateEditor(block);
        }

        setDefaultRenderer(Object.class, new ParamTableCellRenderer());

        for (int row = 0; row < model.getRowCount(); row++) {
            XParameter param = block.getXParameter(getParameterKey(row));
            Object value = model.getValueAt(row, 1);
            switch (param.getFormat()) {
                case COMBO:
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
                    editorModel.addEditorForRow(row, new DateEditor(DateEditor.DURATION, value));
                    break;
                case DATE:
                    editorModel.addEditorForRow(row, new DateEditor(DateEditor.DATE, value));
                    break;
                case FILE:
                    editorModel.addEditorForRow(row, new FileEditor(JFileChooser.FILES_ONLY));
                    break;
                case PATH:
                    editorModel.addEditorForRow(row, new FileEditor(JFileChooser.DIRECTORIES_ONLY));
                    break;
                case CLASS:
                    try {
                        editorModel.addEditorForRow(row, new ClassEditor());
                    } catch (Exception ex) {
                    }
                    break;
                case LIST:
                    editorModel.addEditorForRow(row, new ListEditor());
                    break;
                case TEXTFILE:
                    editorModel.addEditorForRow(row, new TextFileEditor());
                    break;
                case ZONEFILE:
                    editorModel.addEditorForRow(row, new ZoneEditor());
                    break;
            }
        }
    }

    public void setAllRowsVisible(final boolean visible) {
        if (null != getCellEditor()) {
            getCellEditor().stopCellEditing();
        }
        TableRowSorter sorter = new TableRowSorter<ParameterTableModel>(model);
        sorter.setRowFilter(new RowFilter() {

            @Override
            public boolean include(Entry entry) {
                int row = (Integer) entry.getIdentifier();
                try {
                    boolean hidden = model.getTableParameter(row).getXParameter().isHidden();
                    if (visible) {
                        return true;
                    } else {
                        return !hidden;
                    }
                } catch (Exception ex) {
                    return false;
                }
            }
        });
        setRowSorter(sorter);
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
        Object[] longValues = model.getLongValues();
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

    private void setupDateEditor(XBlock block) {
        Calendar calendar;
        if (block.getXParameter("calendar_type").getValue().matches("climato")) {
            calendar = new ClimatoCalendar();
        } else {
            calendar = new Calendar1900();
            if (null != model.block) {
                if (null != block.getXParameter("time_origin")) {
                    String time_origin = block.getXParameter("time_origin").getValue();
                    calendar = new Calendar1900(getTimeOrigin(time_origin, Calendar.YEAR),
                            getTimeOrigin(time_origin, Calendar.MONTH),
                            getTimeOrigin(time_origin, Calendar.DAY_OF_MONTH));
                }
            }
        }
        for (int i = 0; i < getRowCount() - 1; i++) {
            TableCellEditor editor = getRowEditorModel().getEditor(i);
            if (null != editor) {
                if (editor instanceof DateEditor) {
                    ((DateEditor) editor).setCalendar(calendar);
                }
            }
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        try {
            super.tableChanged(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (null != model) {
            try {
                if (getParameterKey(e.getLastRow()).matches("calendar_type")
                        || getParameterKey(e.getLastRow()).matches("time_origin")) {
                    if (null != model.block) {
                        setupDateEditor(model.block);
                    }
                }
            } catch (Exception ex) {
            }
        }
    }

    private int getTimeOrigin(String time_origin, int field) {
        return Calendar1900.getTimeOrigin(time_origin, field);
    }

    public String getParameterKey(int row) {
        String key = "";
        if (row >= 0) {
            key = model.getTableParameter(row).getXParameter().getKey();
        }
        return key;
    }

    public String getParameterValue(int row) {
        String value = "";
        if (row >= 0) {
            value = model.getTableParameter(row).getValue();
        }
        return value;
    }

    public int getParameterIndex(int row) {
        if (row >= 0) {
            return Integer.valueOf(model.getTableParameter(row).getIndex());
        }
        return 0;
    }

    public class ParameterTableModel extends AbstractTableModel {

        private XBlock block;
        private TableParameter[] data;
        final public static String NAME_HEADER = "Name";
        final public static String VALUE_HEADER = "Value";
        private final String[] HEADERS = new String[]{NAME_HEADER, VALUE_HEADER};
        private JUndoManager undoManager;

        ParameterTableModel(XBlock block) {
            this.block = block;
            data = createData();
            addUndoableEditListener(undoManager = new JUndoManager());
        }

        @Override
        public int getColumnCount() {
            return HEADERS.length;
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public String getColumnName(int col) {
            return HEADERS[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return data[row].getLongName();
            } else {
                return data[row].getValue();
            }
        }

        TableParameter getTableParameter(int row) {
            return data[row];
        }

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
                data[row].setValue(value.toString());
                fireTableCellUpdated(row, column);
                return;
            }

            Object oldValue = getValueAt(row, column);
            data[row].setValue(value.toString());
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

        private TableParameter[] createData() {

            Collection<XParameter> list = block.getXParameters();
            List<TableParameter> listData = new ArrayList();
            TableParameter[] tableData;
            int i = 0;
            for (XParameter xparam : list) {
                xparam.reset();
                listData.add(new TableParameter(xparam, xparam.index()));
                if (xparam.hasNext()) {
                    do {
                        xparam.increment();
                        listData.add(new TableParameter(xparam, xparam.index()));
                    } while (xparam.hasNext());
                }
                xparam.reset();
            }
            tableData = new TableParameter[listData.size()];
            for (TableParameter arr : listData) {
                tableData[i++] = arr;
            }
            return tableData;
        }

        private Object[] getLongValues() {

            String[] longuest = new String[]{"", ""};
            Collection<XParameter> list = block.getXParameters();
            for (XParameter xparam : list) {
                if (xparam.getLongName().length() > longuest[0].length()) {
                    longuest[0] = xparam.getLongName();
                }
                if (xparam.getValue().length() > longuest[1].length()) {
                    longuest[1] = xparam.getValue();
                }
            }
            return longuest;
        }
    }

    class ParamTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {

            Component comp = super.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);

            if (!table.isEnabled()) {
                comp.setForeground(Color.LIGHT_GRAY);
                comp.setBackground(Color.WHITE);
                return comp;
            } else {
                comp.setForeground(Color.BLACK);
            }

            int mrow = table.convertRowIndexToModel(row);
            if (model.getTableParameter(mrow).getXParameter().isSerial()) {
                comp.setBackground(new Color(0, 255, 0, 20));
            } else if (model.getTableParameter(mrow).getXParameter().isHidden()) {
                comp.setBackground(new Color(255, 0, 0, 20));
            } else {
                comp.setBackground(Color.WHITE);
            }
            return comp;
        }
    }

    class ParamTableRowFilter extends TableRowSorter<ParameterTableModel> {
    }

    private class TableParameter {

        private XParameter xparameter;
        private int index;
        private String value;

        TableParameter(XParameter xparameter, int index) {
            this.xparameter = xparameter;
            this.index = index;
            this.value = xparameter.getValue(index);
        }

        XParameter getXParameter() {
            return xparameter;
        }

        int getIndex() {
            return index;
        }

        String getLongName() {
            String longName = xparameter.getLongName();
            if (xparameter.getLength() > 1) {
                longName += " [" + String.valueOf(index + 1) + "]";
            }
            return longName;
        }

        String getValue() {
            return value;
        }

        void setValue(String value) {
            this.value = value;
            xparameter.setValue(value, index);
        }
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
}
