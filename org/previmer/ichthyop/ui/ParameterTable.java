/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.undo.UndoManager;
import org.previmer.ichthyop.calendar.Calendar1900;
import org.previmer.ichthyop.calendar.ClimatoCalendar;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;
import org.previmer.ichthyop.manager.SimulationManager;

/**
 *
 * @author pverley
 */
public class ParameterTable extends JMultiCellEditorsTable {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     *
     */
    private ParameterTableModel model = new ParameterTableModel();
    /**
     *
     */
    private ParameterCellRenderer renderer = new ParameterCellRenderer();
    /**
     *
     */
    private DateEditor dateEditor = new DateEditor();

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
    public ParameterTableModel getModel() {
        return model;
    }

    public UndoManager getUndoManager() {
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

        if (block.getKey().matches("Time/General")) {
            setupDateEditor(block);
        }

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
                    editorModel.addEditorForRow(row, dateEditor);
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
        if (block.getXParameter("Type of calendar").getValue().matches("climato")) {
            dateEditor.setCalendar(new ClimatoCalendar());
        } else {
            Calendar1900 calendar = new Calendar1900();
            if (null != model.block) {
                if (null != block.getXParameter("Time origin")) {
                    String time_origin = block.getXParameter("Time origin").getValue();
                    calendar = new Calendar1900(getTimeOrigin(time_origin, Calendar.YEAR),
                            getTimeOrigin(time_origin, Calendar.MONTH),
                            getTimeOrigin(time_origin, Calendar.DAY_OF_MONTH));
                }
            }
            dateEditor.setCalendar(calendar);
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        if (null != model) {
            try {
                if (model.getParameterKey(e.getLastRow()).matches("Type of calendar")
                        || model.getParameterKey(e.getLastRow()).matches("Time origin")) {
                    if (null != model.block) {
                        setupDateEditor(model.block);
                    }
                }
            } catch (Exception ex) {
            }
        }
    }

    private int getTimeOrigin(String time_origin, int field) {
        return SimulationManager.getInstance().getTimeManager().getTimeOrigin(time_origin, field);
    }

    public class ParameterTableModel extends UndoableTableModel {

        private int visibleRows = 1, allRows = 1;
        private boolean isAllRowsVisible;
        private XBlock block;

        ParameterTableModel() {
            isAllRowsVisible = false;
        }

        ParameterTableModel(XBlock block) {
            isAllRowsVisible = false;
            this.block = block;
            setData(createData());
        }

        public void setAllRowsVisible(boolean visible) {
            isAllRowsVisible = visible;
            fireTableChanged(null);
        }

        
        @Override
        public Object[][] createData() {

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

        @Override
        public int getRowCount() {
            return getRowCount(this.isAllRowsVisible);
        }

        public int getRowCount(boolean isAllRowsVisible) {
            return isAllRowsVisible ? allRows : visibleRows;
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

        public String getParameterValue(int row) {
            String value = "";
            if (row >= 0) {
                value = getValueAt(row, 1).toString();
            }
            return value;
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
            if (!table.isEnabled()) {
                comp.setBackground(new Color(192, 192, 192, 50));
            }

            return comp;
        }
    }
}
