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
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.ichthyop.input.Parameter;
import org.ichthyop.input.ParameterSet;

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

    public void setModel(ParameterSet parameterSet, TableModelListener l) throws Exception {
        getModel().removeTableModelListener(l);
        setModel(model = new ParameterTableModel(parameterSet));
        setEditors();
        getModel().addTableModelListener(l);
    }

    private void setEditors() throws Exception {

        RowEditorModel editorModel = new RowEditorModel();
        setRowEditorModel(editorModel);
        setDefaultRenderer(Object.class, new ParamTableCellRenderer());

        for (int row = 0; row < model.getRowCount(); row++) {
            Parameter parameter = model.getParameter(row);
            switch (parameter.getFormat()) {
                case COMBO:
                    editorModel.addEditorForRow(row, new DefaultCellEditor(new JComboBox(parameter.getAcceptedValues())));
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
                    editorModel.addEditorForRow(row, new DateEditor(DateEditor.DURATION, parameter.getValue()));
                    break;
                case HOUR:
                    editorModel.addEditorForRow(row, new HourEditor(parameter.getValue()));
                    break;
                case DATE:
                    editorModel.addEditorForRow(row, new DateEditor(DateEditor.DATE, parameter.getValue()));
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
                    editorModel.addEditorForRow(row, new TextFileEditor(parameter.getTemplate()));
                    break;
                case ZONEFILE:
                    editorModel.addEditorForRow(row, new ZoneEditor(parameter.getTemplate()));
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
    
    Parameter getParameter(int row) {
        return model.getParameter(row);
    }

    public class ParameterTableModel extends AbstractTableModel {

        private final Parameter[] parameters;
        private final String[] HEADERS = new String[]{"Name", "Value(s)"};
        private JUndoManager undoManager;

        ParameterTableModel(ParameterSet set) {
            List<Parameter> tmp = set.getParameters();
            parameters = tmp.toArray(new Parameter[tmp.size()]);
            addUndoableEditListener(undoManager = new JUndoManager());
        }

        @Override
        public int getColumnCount() {
            return HEADERS.length;
        }

        @Override
        public int getRowCount() {
            return parameters.length;
        }

        @Override
        public String getColumnName(int col) {
            return HEADERS[col];
        }

        Parameter getParameter(int row) {
            return parameters[row];
        }
        

        @Override
        public Object getValueAt(int row, int col) {
            return (col == 0)
                    ? parameters[row].getLongName()
                    : parameters[row].getValue();
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

       
        @Override
        public boolean isCellEditable(int row, int col) {
            // Only values are editable
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
                parameters[row].setValue(value.toString());
                fireTableCellUpdated(row, column);
                return;
            }

            parameters[row].setValue(value.toString());
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
    }

    class ParamTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {

            Component comp = super.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);

            comp.setForeground(table.isEnabled() ? Color.BLACK : Color.LIGHT_GRAY);
            comp.setBackground(Color.WHITE);
            return comp;
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
