/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayFloat.D1;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author mariem
 */
public class CopyTime {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //ouverture du fichier src et copie de la variable time
        NetcdfFile ncfile = null;
        String filename = "/home/mariem/Bureau/testTime.nc";
        ArrayFloat.D1 timeInDays = null;
        try {
            ncfile = NetcdfFile.open(filename);
            timeInDays = (ArrayFloat.D1) ncfile.findVariable("time").read();
            ncfile.close();
        } catch (IOException ex) {
            Logger.getLogger(CopyTime.class.getName()).log(Level.SEVERE, null, ex);
        }
        ArrayFloat.D1 timeInSeconds = new D1(timeInDays.getShape()[0]);
        for (int t = 0; t < timeInDays.getShape()[0]; t++) {
            timeInSeconds.set(t, (float) (timeInDays.get(t) * 86400));
        }

        //ouverture du fichier destination et insertion de la nouvelle variable time_counter
        String filedest = "/home/mariem/Bureau/NIO12_N1_1995_malabar_vozocrtx.nc";
        NetcdfFileWriteable ncfile2 = null;
        /*
         * Open existing
         */
        try {
            ncfile2 = NetcdfFileWriteable.openExisting(filedest);
        } catch (IOException ex) {
            Logger.getLogger(CopyTime.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
         * define mode to be able to add a new variable
         */
        try {
            ncfile2.setRedefineMode(true);
        } catch (IOException ex) {
            Logger.getLogger(CopyTime.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
          * new variable time
          */
        Dimension time = ncfile2.getDimensions().get(3);
        ncfile2.addVariable("time", DataType.FLOAT, new Dimension[]{time});
        ncfile2.addVariableAttribute("time", "units", "days since 01-03 12:00:00 of the current year");
        ncfile2.addVariableAttribute("time", "calendar", "365_day");
        
        try {
            ncfile2.setRedefineMode(false);
        } catch (IOException ex) {
            Logger.getLogger(CopyTime.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
         * define the new structure
         */
 /*       try {
            ncfile2.create();
        } catch (IOException ex) {
            Logger.getLogger(CopyTime.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
         
         /*
         * write the time values
         */
        try {
            ncfile2.write("time", timeInSeconds);
        } catch (IOException ex) {
            Logger.getLogger(CopyTime.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(CopyTime.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
         * close nc file
         */
        try {
            ncfile2.close();
        } catch (IOException ex) {
            Logger.getLogger(CopyTime.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("copie effectuée");
    }
}