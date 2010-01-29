/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.undo.UndoManager;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
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
    private ParameterTableModel model;
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
    public TableModel getModel() {
        return model == null
                ? new DefaultTableModel()
                : model;
    }

    public UndoManager getUndoManager() {
        return model.getUndoManager();
    }

    public void setModel(XBlock block, TableModelListener l) {
        setModel(model = new ParameterTableModel(block));
        setEditors(block);
        //setDefaultRenderer(Object.class, renderer);
        getModel().addTableModelListener(l);
        getColumnExt("Serial").setVisible(false);
        getColumnExt("Hidden").setVisible(false);
        addHighlighter(new ColorHighlighter(new HighlightPredicate() {

            public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
                try {
                    return Boolean.valueOf(ca.getValueAt(ca.row, 2).toString());
                } catch (Exception ex) {
                    return false;
                }
            }
        }, new Color(0, 255, 0, 20), Color.BLACK));
        addHighlighter(new ColorHighlighter(new HighlightPredicate() {

            public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
                try {
                    return Boolean.valueOf(ca.getValueAt(ca.row, 3).toString());
                } catch (Exception ex) {
                    return false;
                }
            }
        }, new Color(255, 0, 0, 20), Color.BLACK));
        setRowFilter(new RowFilter<Object, Object>() {

            public boolean include(Entry<? extends Object, ? extends Object> entry) {
                return !Boolean.valueOf(entry.getStringValue(3));
            }
        });
        //adjustColumnSizes();
    }

    private void setEditors(XBlock block) {

        RowEditorModel editorModel = new RowEditorModel();
        setRowEditorModel(editorModel);

        if (block.getKey().matches("Time/General")) {
            setupDateEditor(block);
        }

        for (int row = 0; row < model.getRowCount(); row++) {
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

    public void setAllRowsVisible(boolean visible) {
        if (visible) {
            setRowFilter(null);
        } else {
            setRowFilter(new RowFilter<Object, Object>() {

                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    return !Boolean.valueOf(entry.getStringValue(3));
                }
            });
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

        private XBlock block;

        ParameterTableModel(XBlock block) {
            this.block = block;
            setDataVector(createData(), new String[]{"Name", "Value", "Serial", "Hidden"});
        }

        public Object[][] createData() {

            Collection<XParameter> list = block.getXParameters();
            List<String[]> listData = new ArrayList();
            String[][] tableData = new String[list.size()][4];
            int i = 0;
            for (XParameter xparam : list) {
                xparam.reset();
                if (xparam.hasNext()) {
                    listData.add(new String[]{xparam.getKey() + " [1]", xparam.getValue(), String.valueOf(xparam.isSerial()), String.valueOf(xparam.isHidden())});
                    while (xparam.hasNext()) {
                        xparam.increment();
                        listData.add(new String[]{xparam.getKey() + " [" + (xparam.index() + 1) + "]", xparam.getValue(), String.valueOf(xparam.isSerial()), String.valueOf(xparam.isHidden())});
                    }
                } else {
                    listData.add(new String[]{xparam.getKey(), xparam.getValue(), String.valueOf(xparam.isSerial()), String.valueOf(xparam.isHidden())});
                }
            }
            tableData = new String[listData.size()][2];
            for (String[] arr : listData) {
                tableData[i++] = arr;
            }
            return tableData;

        }

        private Object[] getLongValues() {

            Object[][] data = createData();
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
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col == 1) {
                return true;
            } else {
                return false;
            }
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
}
