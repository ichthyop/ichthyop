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
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.util.logging.Level;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.ichthyop.io.Parameter;
import org.ichthyop.io.ParameterSet;

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

    public void setModel(ParameterSet block, TableModelListener l) {
        getModel().removeTableModelListener(l);
        setModel(model = new ParameterTableModel(block));
        setEditors();
        getModel().addTableModelListener(l);
    }

    private void setEditors() {

        RowEditorModel editorModel = new RowEditorModel();
        setRowEditorModel(editorModel);

        setDefaultRenderer(Object.class, new ParamTableCellRenderer());

        for (int row = 0; row < model.getRowCount(); row++) {
            Parameter xparam = new Parameter(getParameterKey(row));
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
                    editorModel.addEditorForRow(row, new ClassEditor());
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

    public String getParameterKey(int row) {
        String key = "";
        if (row >= 0) {
            key = model.getTableParameter(row).getParameter().getKey();
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

        private ParameterSet block;
        private TableParameter[] data;
        final public static String NAME_HEADER = "Name";
        final public static String VALUE_HEADER = "Value";
        private final String[] HEADERS = new String[]{NAME_HEADER, VALUE_HEADER};
        private JUndoManager undoManager;

        ParameterTableModel(ParameterSet block) {
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
            Object oldValue = getValueAt(row, column);
            if (String.valueOf(oldValue).equals(String.valueOf(value))) {
                return;
            }
            UndoableEditListener listeners[] = getListeners(UndoableEditListener.class);
            if (undoable == false || listeners == null) {
                data[row].setValue(value.toString());
                fireTableCellUpdated(row, column);
                return;
            }

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

            Collection<Parameter> list = block.getParameters();
            List<TableParameter> listData = new ArrayList();
            TableParameter[] tableData;
            int i = 0;
            for (Parameter xparam : list) {
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
            Collection<Parameter> list = block.getParameters();
            for (Parameter xparam : list) {
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
            comp.setBackground(Color.WHITE);
            return comp;
        }
    }

    private class TableParameter {

        private final Parameter xparameter;
        private String value;

        TableParameter(Parameter xparameter) {
            this.xparameter = xparameter;
            this.value = xparameter.getValue();
        }

        Parameter getParameter() {
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
