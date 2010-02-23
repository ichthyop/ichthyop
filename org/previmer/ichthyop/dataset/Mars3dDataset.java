/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import org.previmer.ichthyop.util.MetaFilenameFilter;
import org.previmer.ichthyop.util.NCComparator;
import org.previmer.ichthyop.event.NextStepEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class Mars3dDataset extends Mars3dDatasetCommon {

    /**
     * List on NetCDF input files in which dataset is read.
     */
    private ArrayList<String> listInputFiles;
    /**
     * Index of the current file read in the {@code listInputFiles}
     */
    private int indexFile;

    private void openLocation(String rawPath) throws IOException {

        if (!isDirectory(rawPath)) {
            throw new IOException(rawPath + " is not a valid directory.");
        }
        listInputFiles = getInputList(rawPath);
        open(listInputFiles.get(0));
    }

    private ArrayList<String> getInputList(String path) throws IOException {

        ArrayList<String> list = null;

        File inputPath = new File(path);
        String fileMask = getParameter("file_filter");
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

    private boolean isDirectory(String location) throws IOException {

        File f = new File(location);
        if (!f.isDirectory()) {
            throw new IOException(location + " is not a valid directory.");
        }
        return f.isDirectory();
    }

    public void setUp() {

        loadParameters();

        try {
            openLocation(getParameter("input_path"));
            getDimNC();
            if (Boolean.valueOf(getParameter("shrink_domain"))) {
                float[] p1 = new float[]{Float.valueOf(getParameter("north-west-corner.lon")), Float.valueOf(getParameter("north-west-corner.lat"))};
                float[] p2 = new float[]{Float.valueOf(getParameter("south-east-corner.lon")), Float.valueOf(getParameter("south-east-corner.lat"))};
                range(p1, p2);
            }
            readConstantField();
            getDimGeogArea();
            getCstSigLevels();
            z_w_tp0 = getSigLevels();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void init() {
        try {
            long t0 = getSimulationManager().getTimeManager().get_tO();
            open(getFile(t0));
            FLAG_TP = FLAG_SAL = FLAG_VDISP = false;
            setAllFieldsTp1AtTime(rank = findCurrentRank(t0));
            time_tp1 = t0;
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    private String getFile(long time) throws IOException {

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

        throw new IOException("Time value " + (long) time + " not contained among NetCDF files " + getParameter("file_filter") + " of folder " + getParameter("input_path"));
    }

    private boolean isTimeIntoFile(long time, int index) throws IOException {

        String filename = "";
        NetcdfFile nc;
        Array timeArr;
        long time_r0, time_rf;

        try {
            filename = listInputFiles.get(index);
            nc = NetcdfDataset.openFile(filename, null);
            timeArr = nc.findVariable(strTime).read();
            time_r0 = skipSeconds(timeArr.getLong(timeArr.getIndex().set(0)));
            time_rf = skipSeconds(timeArr.getLong(timeArr.getIndex().set(
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

    private boolean isTimeBetweenFile(long time, int index) throws IOException {

        NetcdfFile nc;
        String filename = "";
        Array timeArr;
        long[] time_nc = new long[2];

        try {
            for (int i = 0; i < 2; i++) {
                filename = listInputFiles.get(index + i);
                nc = NetcdfDataset.openFile(filename, null);
                timeArr = nc.findVariable(strTime).read();
                time_nc[i] = skipSeconds(
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

    public void nextStepTriggered(NextStepEvent e) {

        long time = e.getSource().getTime();
        //Logger.getAnonymousLogger().info("set fields at time " + time);
        int time_arrow = (int) Math.signum(e.getSource().get_dt());

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        w_tp0 = w_tp1;
        zeta_tp0 = zeta_tp1;
        temp_tp0 = temp_tp1;
        salt_tp0 = salt_tp1;
        kv_tp0 = kv_tp1;
        if (z_w_tp1 != null) {
            z_w_tp0 = z_w_tp1;
        }
        rank += time_arrow;
        try {
            if (rank > (nbTimeRecords - 1) || rank < 0) {
                open(getNextFile(time_arrow));
                rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
            }
            setAllFieldsTp1AtTime(rank);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    private String getNextFile(int time_arrow) throws IOException {

        int index = indexFile - (1 - time_arrow) / 2;
        boolean noNext = (listInputFiles.size() == 1) || (index < 0) || (index >= listInputFiles.size() - 1);
        if (noNext) {
            throw new IOException("Unable to find any file following " + listInputFiles.get(indexFile));
        }
        indexFile += time_arrow;
        return listInputFiles.get(indexFile);
    }

    private void open(String filename) throws IOException {
        try {
            if (ncIn == null || (new File(ncIn.getLocation()).compareTo(new File(filename)) != 0)) {
                ncIn = NetcdfDataset.openFile(filename, null);
                nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
            }
            getLogger().info("Open dataset " + filename);
        } catch (IOException e) {
            throw new IOException("Problem opening dataset " + filename + " - " + e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem reading " + strTimeDim + " dimension at location " + filename
                    + " : " + e.getMessage());
        }
    }
}
