package ichthyop.ui;

/** import java.util */
import java.util.Hashtable;

/** import Swing */
import javax.swing.table.TableCellEditor;

/**
 * The class is a <code>HastTable</code> that stores
 * the <code>TableCellEditor</code>s of the <code>JTableCbBox</code> object.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see ichthyop.ui.JTableCbBox
 */
public class RowEditorModel {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * The <code>HastTable</code> that stores the
     * <code>TableCellEditor</code>s.
     */
    private Hashtable data;

///////////////
// Constructors
///////////////

    /**
     * Constructs an empty <code>RowEditorModel</code>.
     */
    public RowEditorModel() {

        data = new Hashtable();
    }

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Adds the given <code>TableCellEditor</code> for the specified
     * <code>row</code>.
     * @param row an int, the number of the row that will hold the given
     * editor.
     * @param e the TableCellEditor for the specified row.
     */
    public void addEditorForRow(int row, TableCellEditor e) {

        data.put(new Integer(row), e);
    }

    /**
     *  Removes the <code>TableCellEditor</code> for the specified
     * <code>row</code>.
     * @param row an int, the number of the row for which the
     * <code>TableCellEditor</code> should be removed.
     */
    public void removeEditorForRow(int row) {

        data.remove(new Integer(row));
    }

    /**
     * Gets the <code>TableCellEditor</code> associated to the specified row.
     * @param row an int, the number of the row that holds the
     * <code>TableCellEditor</code>
     * @return the TableCellEditor associated to the specified row.
     */
    public TableCellEditor getEditor(int row) {

        return (TableCellEditor) data.get(new Integer(row));
    }

    //---------- End of class
}
