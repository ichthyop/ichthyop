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
import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.undo.UndoManager;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.previmer.ichthyop.calendar.Calendar1900;
import org.previmer.ichthyop.calendar.ClimatoCalendar;
import org.previmer.ichthyop.io.ParamType;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;

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
        getModel().addTableModelListener(l);
        getColumnExt(XParameter.TYPE_HEADER).setVisible(false);
        getColumnExt(XParameter.HIDDEN_HEADER).setVisible(false);
        getColumnExt(XParameter.KEY_HEADER).setVisible(false);
        getColumnExt(XParameter.INDEX_HEADER).setVisible(false);
        addHighlighter(new ColorHighlighter(new HighlightPredicate() {

            public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
                try {
                    return Boolean.valueOf(model.getValueAt(ca.row, getColumnExt(XParameter.HIDDEN_HEADER).getModelIndex()).toString());
                } catch (Exception ex) {
                    return false;
                }
            }
        }, new Color(255, 0, 0, 20), Color.BLACK));
        addHighlighter(new ColorHighlighter(new HighlightPredicate() {

            public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
                try {
                    return model.getValueAt(ca.row, getColumnExt(XParameter.TYPE_HEADER).getModelIndex()).toString().matches(ParamType.SERIAL.toString());
                } catch (Exception ex) {
                    return false;
                }
            }
        }, new Color(0, 255, 0, 20), Color.BLACK));
        setAllRowsVisible(false);
    }

    private void setEditors(XBlock block) {

        RowEditorModel editorModel = new RowEditorModel();
        setRowEditorModel(editorModel);

        if (block.getKey().matches("app.time")) {
            setupDateEditor(block);
        }

        for (int row = 0; row < model.getRowCount(); row++) {
            XParameter param = block.getXParameter(getParameterKey(row));
            Object value = model.getValueAt(row, getColumnExt(XParameter.VALUE_HEADER).getModelIndex());
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
                    editorModel.addEditorForRow(row, new ClassEditor());
                    break;
                case LIST:
                    editorModel.addEditorForRow(row, new ListEditor());
                    break;
                case TEXTFILE:
                    editorModel.addEditorForRow(row, new TextFileEditor());
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
                    return !Boolean.valueOf(entry.getStringValue(getColumnExt(XParameter.HIDDEN_HEADER).getModelIndex()));
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
            TableCellEditor editor =  getRowEditorModel().getEditor(i);
            if (null != editor)
            if (editor instanceof DateEditor) {
                ((DateEditor) editor).setCalendar(calendar);
            }
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
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
            key = model.getValueAt(row, getColumnExt(XParameter.KEY_HEADER).getModelIndex()).toString();
        }
        return key;
    }

    public String getParameterValue(int row) {
        String value = "";
        if (row >= 0) {
            value = model.getValueAt(row, getColumnExt(XParameter.VALUE_HEADER).getModelIndex()).toString();
        }
        return value;
    }

    public int getParameterIndex(int row) {
        if (row >= 0) {
            return Integer.valueOf(model.getValueAt(row, getColumnExt(XParameter.INDEX_HEADER).getModelIndex()).toString());
        }
        return 0;
    }

    public class ParameterTableModel extends UndoableTableModel {

        private XBlock block;

        ParameterTableModel(XBlock block) {
            this.block = block;
            setDataVector(createData(), XParameter.getHeaders());
        }

        public Object[][] createData() {

            Collection<XParameter> list = block.getXParameters();
            List<String[]> listData = new ArrayList();
            String[][] tableData;
            int i = 0;
            for (XParameter xparam : list) {
                xparam.reset();
                listData.add(xparam.toTableRow());
                if (xparam.hasNext()) {
                    do {
                        xparam.increment();
                        listData.add(xparam.toTableRow());
                    } while (xparam.hasNext());
                }
                xparam.reset();
            }
            tableData = new String[listData.size()][XParameter.getHeaders().length];
            for (String[] arr : listData) {
                tableData[i++] = arr;
            }
            return tableData;

        }

        private Object[] getLongValues() {
            
            String[] longuest = new String[] {"", ""};
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

        @Override
        public boolean isCellEditable(int row, int col) {

            if (getColumnName(col).matches(XParameter.VALUE_HEADER)) {
                return true;
            } else {
                return false;
            }
        }
    }
}
