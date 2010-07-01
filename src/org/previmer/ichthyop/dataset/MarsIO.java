/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import org.previmer.ichthyop.util.MetaFilenameFilter;
import org.previmer.ichthyop.util.NCComparator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.io.IOTools;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class MarsIO extends SimulationManagerAccessor {

    /**
     * List on NetCDF input files in which dataset is read.
     */
    private static ArrayList<String> listInputFiles;
    /**
     * Index of the current file read in the {@code listInputFiles}
     */
    private static int indexFile;
    /**
     * 
     */
    private static String strTime;

    static void setTimeField(String timeField) {
        strTime = timeField;
    }

    static NetcdfFile openLocation(String rawPath, String fileMask) throws IOException {

        String path = IOTools.resolvePath(rawPath);

        if (!isDirectory(path)) {
            throw new IOException(rawPath + " is not a valid directory.");
        }
        listInputFiles = getInputList(path, fileMask);
        return openFile(listInputFiles.get(0));
    }

    static ArrayList<String> getInputList(String path, String fileMask) throws IOException {

        ArrayList<String> list = null;

        File inputPath = new File(path);
        File[] listFile = inputPath.listFiles(new MetaFilenameFilter(fileMask));
        if (listFile.length == 0) {
            throw new IOException(path + " contains no file matching mask " + fileMask);
        }
        list = new ArrayList<String>(listFile.length);
        for (File file : listFile) {
            list.add(file.toString());
        }
        if (list.size() > 1) {
            Collections.sort(list, new NCComparator(strTime));
        }
        return list;
    }

    static boolean isDirectory(String location) throws IOException {

        File f = new File(location);
        if (!f.isDirectory()) {
            throw new IOException(location + " is not a valid directory.");
        }
        return f.isDirectory();
    }

    static String getFile(long time) throws IOException {

        int indexLast = listInputFiles.size() - 1;
        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());

        for (int i = 0; i < indexLast; i++) {
            if (isTimeIntoFile(time, i)) {
                indexFile = i;
                return listInputFiles.get(i);
            } else if (isTimeBetweenFile(time, i)) {
                indexFile = i - (time_arrow - 1) / 2;
                return listInputFiles.get(indexFile);
            }
        }

        if (isTimeIntoFile(time, indexLast)) {
            indexFile = indexLast;
            return listInputFiles.get(indexLast);
        }
        StringBuffer msg = new StringBuffer();
        msg.append("Time value ");
        msg.append(getSimulationManager().getTimeManager().timeToString());
        msg.append(" (");
        msg.append(time);
        msg.append(" seconds) not contained among NetCDF files.");
        throw new IndexOutOfBoundsException(msg.toString());
    }

    static boolean isTimeIntoFile(long time, int index) throws IOException {

        String filename = "";
        NetcdfFile nc;
        Array timeArr;
        long time_r0, time_rf;

        try {
            filename = listInputFiles.get(index);
            nc = NetcdfDataset.openFile(filename, null);
            timeArr = nc.findVariable(strTime).read();
            time_r0 = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(0)));
            time_rf = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(
                    timeArr.getShape()[0] - 1)));
            nc.close();

            return (time >= time_r0 && time < time_rf);
            /*switch (time_arrow) {
            case 1:
            return (time >= time_r0 && time < time_rf);
            case -1:
            return (time > time_r0 && time <= time_rf);
            }*/
        } catch (IOException e) {
            throw new IOException("Problem reading file " + filename + " : " + e.getCause());
        } catch (NullPointerException e) {
            throw new IOException("Unable to read " + strTime
                    + " variable in file " + filename + " : " + e.getCause());
        }
        //return false;

    }

    static boolean isTimeBetweenFile(long time, int index) throws IOException {

        NetcdfFile nc;
        String filename = "";
        Array timeArr;
        long[] time_nc = new long[2];

        try {
            for (int i = 0; i < 2; i++) {
                filename = listInputFiles.get(index + i);
                nc = NetcdfDataset.openFile(filename, null);
                timeArr = nc.findVariable(strTime).read();
                time_nc[i] = DatasetUtil.skipSeconds(
                        timeArr.getLong(timeArr.getIndex().set(0)));
                nc.close();
            }
            if (time >= time_nc[0] && time < time_nc[1]) {
                return true;
            }
        } catch (IOException e) {
            throw new IOException("Problem reading file " + filename + " : " + e.getCause());
        } catch (NullPointerException e) {
            throw new IOException("Unable to read " + strTime
                    + " variable in file " + filename + " : " + e.getCause());
        }
        return false;
    }

    static String getNextFile(int time_arrow) throws IOException {

        int index = indexFile - (1 - time_arrow) / 2;
        boolean noNext = (listInputFiles.size() == 1) || (index < 0) || (index >= listInputFiles.size() - 1);
        if (noNext) {
            throw new IOException("Unable to find any file following " + listInputFiles.get(indexFile));
        }
        indexFile += time_arrow;
        return listInputFiles.get(indexFile);
    }

    static NetcdfFile openFile(String filename) throws IOException {
        NetcdfFile nc;
        try {
            nc = NetcdfDataset.openFile(filename, null);
            getLogger().info("Open dataset " + filename);
            return nc;
        } catch (Exception e) {
            IOException ioex = new IOException("Problem opening dataset " + filename + " - " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
    }

    /**
     * Loads the NetCDF dataset from the specified filename.
     * @param opendapURL a String that can be a local pathname or an OPeNDAP URL.
     * @throws IOException
     */
    static NetcdfFile openURL(String opendapURL) throws IOException {
        NetcdfFile ncIn;
        try {
            ncIn = NetcdfDataset.openFile(opendapURL, null);
            getLogger().info("Open remote dataset " + opendapURL);
            return ncIn;
        } catch (Exception e) {
            IOException ioex = new IOException("Problem opening dataset " + opendapURL + " ==> " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
    }

    public static void checkInitTime(NetcdfFile nc, String strTime) throws IOException, IndexOutOfBoundsException {

        long time = getSimulationManager().getTimeManager().get_tO();
        Array timeArr = null;
        try {
            timeArr = nc.findVariable(strTime).read();
        } catch (Exception ex) {
            IOException ioex = new IOException("Failed to read dataset time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        int ntime = timeArr.getShape()[0];
        long time0 = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(0)));
        long timeN = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(ntime - 1)));
        if (time < time0 || time > timeN) {
            StringBuffer msg = new StringBuffer();
            msg.append("Time value ");
            msg.append(getSimulationManager().getTimeManager().timeToString());
            msg.append(" (");
            msg.append(time);
            msg.append(" seconds) not contained in dataset " + nc.getLocation());
            throw new IndexOutOfBoundsException(msg.toString());
        }
    }
}
