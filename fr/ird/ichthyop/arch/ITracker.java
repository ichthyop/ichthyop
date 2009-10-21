/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.arch;

import java.util.List;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 *
 * @author pverley
 */
public interface ITracker {

    /**
     * Determines whether the tracker is enabled.
     * @return
     */
    public boolean isEnabled();

    /**
     *
     * @param time
     * @return
     */
    public void track();

    public Array getArray();

    public int[] origin(int index_record);

    /**
     * Gets the name of the variable.
     * @return a String, the variable name
     */
    public String short_name();

    /**
     * Gets the description of the variable.
     * @return a String, the variable description
     */
    public String long_name();

    /**
     * Gets the variable unit
     * @return a String, the variable unit
     */
    public String unit();

    /**
     * Gets the first addtionnal attribute
     * @return a String, variable attribute
     */
    public Attribute[] attributes();

    /**
     * Gets the variable data type
     * @return the DataType of the variable
     */
    public DataType type();

    /**
     * Gets the list of the variable dimensions.
     * @return the List of the variable {@code Dimension}s
     */
    public List<Dimension> dimensions();
}
