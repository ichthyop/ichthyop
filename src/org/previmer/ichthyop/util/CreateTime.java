/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

/**
 *
 * @author mariem
 */
public class CreateTime {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        float[] tabDate = new float[73];
        int j = 0;
        for (int i = 0; i < 73; i++) {
            tabDate[i] = j;
            j = j + 5;
        }

        String filename = "/home/mariem/Bureau/testTime.nc";
        NetcdfFileWriteable ncfile = null;
        try {
            ncfile = NetcdfFileWriteable.createNew(filename, false);
        } catch (IOException ex) {
            Logger.getLogger(CreateTime.class.getName()).log(Level.SEVERE, null, ex);
        }

        // declare dimension & variable

        Dimension timeDim = ncfile.addDimension("time", 73);
        ArrayList dims = new ArrayList();
        dims.add(timeDim);
        ncfile.addVariable("time", DataType.FLOAT, dims);

        // validate the netcdf structure of the file

        try {
            ncfile.create();
        } catch (IOException e) {
            System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
        }

        // fill time variable

        ArrayFloat.D1 timeArray = new ArrayFloat.D1(tabDate.length);
        for (int i = 0; i < tabDate.length; i++) {
            timeArray.set(i, tabDate[i]);
        }
        try {
            ncfile.write("time", timeArray);
        } catch (IOException ex) {
            Logger.getLogger(CreateTime.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(CreateTime.class.getName()).log(Level.SEVERE, null, ex);
        }

        // close the file

        try {
            ncfile.close();
        } catch (IOException ex) {
            Logger.getLogger(CreateTime.class.getName()).log(Level.SEVERE, "ERROR closing file " + ncfile.getLocation(), ex);
        }


    }
}