/*
 *
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full
 * description, see the LICENSE file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.previmer.ichthyop.io;

import org.previmer.ichthyop.action.WindDriftAction;
import org.previmer.ichthyop.dataset.DatasetUtil;
import org.previmer.ichthyop.dataset.RequiredExternalVariable;
import org.previmer.ichthyop.manager.SimulationManager;
import org.previmer.ichthyop.manager.TimeManager;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.util.MetaFilenameFilter;
import org.previmer.ichthyop.util.NCComparator;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDatasets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IrradianceLoader {

    /**
     * Name of the Dimensions in NetCDF file
     */
    static String strLon, strLat, strIrradiance, strTime;
    /** Name of the file mask*/
    static String fileMask;
    /** The simulation manager to access the dataset*/
    static SimulationManager simulationManager;
    /**
     * Number of time records in current NetCDF file
     */
    static int nbTimeRecords;
    /**
     * Time t + dt expressed in seconds
     */
    static double time_tp1;
    /*
     * Time between 2 wind files
     */
    static double dt_wind;
    /**
     * List on NetCDF input files in which wind dataset is read.
     */
    ArrayList<String> listInputFiles;
    /**
     * Index of the current file read in the {@code listInputFiles}
     */
    int indexFile;

    /**
     * wind NetcdfFile
     */
    static NetcdfFile ncIn;

    /**
     * Grid dimension
     */
    int nx, ny;
    /**
     * Current rank in NetCDF dataset
     */
    static int rank;
    /**
     * Current time in NetCDF dataset
     */
    static double time_current;
    /**
     * latitude and longitude arrays
     */
    static double[][] lonRho, latRho;
    /**
     * Irradiance at current time
     */
    static Array irr_tp0;
    /**
     * Irradiance at time t + dt
     */
    static Array irr_tp1;
    /**
     * irradiance variable
     */
    RequiredExternalVariable irradiance_variable;


    public IrradianceLoader(String input_path, String irradianceField, String lonField, String latField, String timeField, SimulationManager simulationManagerObject, String fileMaskName) throws Exception {
        strLon = lonField;
        strTime = timeField;
        strLat = latField;
        strIrradiance = irradianceField;
        fileMask = fileMaskName;
        simulationManager = simulationManagerObject;


        time_current = simulationManager.getTimeManager().getTime();
        openLocation(input_path);
        getDimNC();
        setOnFirstTime();
        setAllFieldsTp1AtTime(rank);
        readLonLat();

        irradiance_variable = new RequiredExternalVariable(latRho, lonRho, irr_tp0, irr_tp1, simulationManager.getDataset());
    }

    public Array readVariable(String name) throws Exception {
        try {
            Variable variable = ncIn.findVariable(name);
            int[] origin = null, shape = null;
            switch (variable.getShape().length) {
                case 4:
                    origin = new int[]{rank, 0, 0, 0};
                    shape = new int[]{1, 1, ny, nx};
                    break;
                case 3:
                    origin = new int[]{rank, 0, 0};
                    shape = new int[]{1, ny, nx};
                    break;
            }
            return variable.read(origin, shape).reduce();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading UW wind velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }

    private void openLocation(String rawPath) throws IOException {

        String path = IOTools.resolvePath(rawPath);

        if (IOTools.isDirectory(path)) {
            listInputFiles = getInputList(path);
            open(listInputFiles.get(0));
        }
    }

    private ArrayList<String> getInputList(String path) throws IOException {

        ArrayList<String> list;

        File inputPath = new File(path);
        File[] listFile = inputPath.listFiles(new MetaFilenameFilter(fileMask));
        if (listFile.length == 0) {
            throw new IOException(path + " contains no file matching mask " + fileMask);
        }
        list = new ArrayList<>(listFile.length);
        for (File file : listFile) {
            list.add(file.toString());
        }
        if (list.size() > 1) {

            Collections.sort(list, new NCComparator(strTime));

        }
        return list;
    }

    private void open(String filename) throws IOException {
        if (ncIn == null || (new File(ncIn.getLocation()).compareTo(new File(filename)) != 0)) {
            if (ncIn != null) {
                ncIn.close();
            }
            try {
                ncIn = NetcdfDatasets.openDataset(filename);
            } catch (Exception ex) {
                IOException ioex = new IOException("Error opening dataset " + filename + " ==> " + ex.toString());
                ioex.setStackTrace(ex.getStackTrace());
                throw ioex;
            }
            try {
                nbTimeRecords = ncIn.findDimension(strTime).getLength();
            } catch (Exception ex) {
                IOException ioex = new IOException("Error dataset time dimension ==> " + ex.toString());
                ioex.setStackTrace(ex.getStackTrace());
                throw ioex;
            }
        }

    }

    void getDimNC() throws IOException {
        System.out.print(strLon);
        try {

            nx = ncIn.findDimension(strLon).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset longitude dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            ny = ncIn.findDimension(strLat).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset latitude dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }

    private void nextStepTriggered() throws Exception {
        double time = simulationManager.getTimeManager().getTime();
        time_current = time;

        int time_arrow = timeArrow();

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        irr_tp0 = irr_tp1;

        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            ncIn.close();
            open(getNextFile(time_arrow));
            nbTimeRecords = ncIn.findDimension(strTime).getLength();
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }
        setAllFieldsTp1AtTime(rank);
    }

    private void setOnFirstTime() throws Exception {

        double t0 = simulationManager.getTimeManager().get_tO();
        int fileRank = DatasetUtil.index(listInputFiles, t0, timeArrow(), strTime);
        
        open(getFile(fileRank));
        readTimeLength();
        rank = DatasetUtil.rank(t0, ncIn, strTime, timeArrow());
        time_tp1 = t0;
    }

    void readTimeLength() throws IOException {
        try {
            nbTimeRecords = ncIn.findDimension(strTime).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Failed to read wind dataset time dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }

    private String getFile(int fileRank) throws Exception {
        return listInputFiles.get(fileRank);
    }

    void readLonLat() throws IOException {
        Array arrLon = null, arrLat = null;
        try {
            arrLon = ncIn.findVariable(strLon).read();

        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading wind dataset longitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            arrLat = ncIn.findVariable(strLat).read();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading wind dataset latitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        lonRho = new double[ny][nx];
        latRho = new double[ny][nx];
        Index indexLon = arrLon.getIndex();
        Index indexLat = arrLat.getIndex();
        for (int j = 0; j < ny; j++) {
            indexLat.set(j);
            for (int i = 0; i < nx; i++) {
                latRho[j][i] = arrLat.getDouble(indexLat);
                indexLon.set(i);
                lonRho[j][i] = arrLon.getDouble(indexLon);
            }
        }
    }


    private String getNextFile(int time_arrow) throws IOException {

        int index = indexFile - (1 - time_arrow) / 2;
        boolean noNext = (listInputFiles.size() == 1) || (index < 0) || (index >= listInputFiles.size() - 1);
        if (noNext) {
            throw new IOException("{Wind dataset} Unable to find any file following " + listInputFiles.get(indexFile));
        }
        indexFile += time_arrow;
        return listInputFiles.get(indexFile);
    }

    private void setAllFieldsTp1AtTime(int i_time) throws Exception {

        double time_tp0 = time_tp1;

        irr_tp1 = readVariable(strIrradiance);

        try {
            time_tp1 = DatasetUtil.getDate(ncIn.getLocation(), strTime, rank);
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        dt_wind = Math.abs(time_tp1 - time_tp0);
    }


    public double getIrradiance(IParticle particle) {
        if (time_current != simulationManager.getTimeManager().getTime()) {
            try {
                nextStepTriggered();
            } catch (Exception ex) {
                Logger.getLogger(WindDriftAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        irradiance_variable.nextStep(irr_tp1, time_tp1, dt_wind);
        double irradiance = irradiance_variable.getVariable(particle.getGridCoordinates(),simulationManager.getTimeManager().getTime());
        return irradiance;
    }
    int timeArrow() {
        return simulationManager.getParameterManager().getParameter("app.time", "time_arrow").equals(TimeManager.TimeDirection.FORWARD.toString()) ? 1 :-1;
    }
}
