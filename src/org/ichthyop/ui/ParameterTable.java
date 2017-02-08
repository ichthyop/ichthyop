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
package org.ichthyop.ui;

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
import org.ichthyop.calendar.InterannualCalendar;
import org.ichthyop.io.XBlock;
import org.ichthyop.io.XParameter;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.logging.Level;
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
import org.ichthyop.calendar.Day360Calendar;
import org.ichthyop.manager.TimeManager;

/*
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

        if (block.getKey().equals("app.time")) {
            setupDateEditor(block);
        }

        setDefaultRenderer(Object.class, new ParamTableCellRenderer());

        for (int row = 0; row < model.getRowCount(); row++) {
            XParameter xparam = block.getXParameter(getParameterKey(row));
            Object value = model.getValueAt(row, 1);
            switch (xparam.getFormat()) {
                case COMBO:
                    editorModel.addEditorForRow(row, new DefaultCellEditor(new JComboBox(xparam.getAcceptedValues())));
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
                case HOUR:
                    editorModel.addEditorForRow(row, new HourEditor(value));
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
                    editorModel.addEditorForRow(row, new TextFileEditor(xparam.getTemplate()));
                    break;
                case ZONEFILE:
                    editorModel.addEditorForRow(row, new ZoneEditor(xparam.getTemplate()));
                    break;
                case LONLAT:
                    editorModel.addEditorForRow(row, new LonLatEditor());
                    break;
                default:
                    editorModel.addEditorForRow(row, new StringCellEditor());
            }
        }
    }

    public void stopEditing() {
        if (null != getCellEditor()) {
            getCellEditor().stopCellEditing();
        }
    }

    public void setAllRowsVisible(final boolean visible) {
        stopEditing();
        TableRowSorter<ParameterTableModel> sorter = new TableRowSorter(model);
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
        TableColumn column;
        Component comp;
        int headerWidth;
        int cellWidth;
        Object[] longValues = model.getLongValues();
        TableCellRenderer headerRenderer = getTableHeader().getDefaultRenderer();

        for (int i = 0; i < getModel().getColumnCount(); i++) {
            column = getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                    null, column.getHeaderValue(),
                    false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;
            try {
                comp = getDefaultRenderer(getColumnClass(i)).getTableCellRendererComponent(this, longValues[i], false, false, 0, i);
            } catch (Exception ex) {
                java.util.logging.Logger.getAnonymousLogger().log(Level.WARNING, ex.toString());
            }
            cellWidth = comp.getPreferredSize().width;

            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

    private void setupDateEditor(XBlock block) {
        Calendar calendar;

        String origin = "1900/01/01 00:00";
        if (null != model.block) {
            if (null != block.getXParameter("time_origin")) {
                origin = block.getXParameter("time_origin").getValue();
            }
        }
        Calendar calendar_o = Calendar.getInstance();
        try {
            calendar_o.setTime(TimeManager.INPUT_DATE_FORMAT.parse(origin));
        } catch (ParseException ex) {
            calendar_o.setTimeInMillis(0);
        }
        int year_o = calendar_o.get(Calendar.YEAR);
        int month_o = calendar_o.get(Calendar.MONTH);
        int day_o = calendar_o.get(Calendar.DAY_OF_MONTH);
        int hour_o = calendar_o.get(Calendar.HOUR_OF_DAY);
        int minute_o = calendar_o.get(Calendar.MINUTE);
        if (block.getXParameter("calendar_type").getValue().equals("climato")) {
            calendar = new Day360Calendar(year_o, month_o, day_o, hour_o, minute_o);
        } else {
            calendar = new InterannualCalendar(year_o, month_o, day_o, hour_o, minute_o);
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
        super.tableChanged(e);

        if (null != model) {
            try {
                if (getParameterKey(e.getLastRow()).equals("calendar_type")
                        || getParameterKey(e.getLastRow()).equals("time_origin")) {
                    if (null != model.block) {
                        setupDateEditor(model.block);
                    }
                }
            } catch (Exception ex) {
            }
        }
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
            return col == 1;
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

        private void addUndoableEditListener(UndoableEditListener listener) {
            listenerList.add(UndoableEditListener.class, listener);
        }

        private TableParameter[] createData() {

            Collection<XParameter> list = block.getXParameters();
            List<TableParameter> listData = new ArrayList();
            TableParameter[] tableData;
            int i = 0;
            for (XParameter xparam : list) {
                listData.add(new TableParameter(xparam));
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
            if (model.getTableParameter(mrow).getXParameter().isHidden()) {
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

        private final XParameter xparameter;
        private String value;

        TableParameter(XParameter xparameter) {
            this.xparameter = xparameter;
            this.value = xparameter.getValue();
        }

        XParameter getXParameter() {
            return xparameter;
        }

        String getLongName() {
            return xparameter.getLongName();
        }

        String getValue() {
            return value;
        }

        void setValue(String value) {
            this.value = value;
            xparameter.setValue(value);
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

        private void synchronizeActions() {
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

        @Override
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

        @Override
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
