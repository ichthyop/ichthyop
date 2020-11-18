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
package org.previmer.ichthyop.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.io.IOTools;
import static org.previmer.ichthyop.io.IOTools.isFile;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.NetcdfFileWriter;

/**
 *
 * @author pverley
 */
public class Roms3dDataset extends Roms3dCommon {

    private List<String> ncfiles;
    private int ncindex;
    boolean saveWEnabled = false;
    
    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = e.getSource().getTime();
        //Logger.getAnonymousLogger().info("set fields at time " + time);
        int time_arrow = (int) Math.signum(e.getSource().get_dt());

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        w_tp0 = w_tp1;
        zeta_tp0 = zeta_tp1;
        if (z_w_tp1 != null) {
            z_w_tp0 = z_w_tp1;
        }
        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            ncindex = DatasetUtil.next(ncfiles, ncindex, time_arrow);
            ncIn = DatasetUtil.openFile(ncfiles.get(ncindex), true);
            readTimeLength();
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }
        setAllFieldsTp1AtTime(rank);
    }

    @Override
    void openDataset() throws Exception {

        ncfiles = DatasetUtil.list(getParameter("input_path"), getParameter("file_filter"));
        if (!skipSorting()) {
            DatasetUtil.sort(ncfiles, strTime, timeArrow());
        }
        ncIn = DatasetUtil.openFile(ncfiles.get(0), true);
        readTimeLength();

        try {
            if (!getParameter("grid_file").isEmpty()) {
                String path = IOTools.resolveFile(getParameter("grid_file"));  // barrier.n
                if (!isFile(path)) {
                    throw new IOException("{Dataset} " + getParameter("grid_file") + " is not a valid file.");
                }
                gridFile = path;

            } else {
                gridFile = ncIn.getLocation();
            }
        } catch (NullPointerException ex) {
            gridFile = ncIn.getLocation();
        }

    }

    @Override
    void setOnFirstTime() throws Exception {
        
        if (this.saveWEnabled) {
            this.saveWFiles();
        }

        double t0 = getSimulationManager().getTimeManager().get_tO();
        ncindex = DatasetUtil.index(ncfiles, t0, timeArrow(), strTime);
        ncIn = DatasetUtil.openFile(ncfiles.get(ncindex), true);
        readTimeLength();
        rank = DatasetUtil.rank(t0, ncIn, strTime, timeArrow());
        time_tp1 = t0;
    }

    
    public void saveWFiles() throws IOException, InvalidRangeException {
            
        // Init netcdf variables for reading
        int[] origin = new int[]{0, 0, jpo, ipo};
        int[] countU = new int[]{1, nz, ny, (nx - 1)};
        int[] countV = new int[]{1, nz, ny - 1, nx};
        int[] countZeta = new int[]{1, ny, nx};
        int[] originZeta = new int[]{0, jpo, ipo};
        
        // Init outputs and output dimension
        float[][][] w;
        int[] originOut = new int[]{1, 0, 0, 0};

        for (String f : this.ncfiles) {
            
            System.out.println("++++++++++ Processing file " + f);

            // Reconstruction of output filename
            File fin = new File(f);
            String direc = fin.getParent();
            String baseName = fin.getName().replace(".nc", "_reconW.nc");
            File fout = new File(direc, baseName);
            
            // Creation of file output + definition of dims/vars
            NetcdfFileWriter ncOut = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, fout.getAbsolutePath());
            Dimension timeDim = ncOut.addUnlimitedDimension("time");
            Dimension xDim = ncOut.addDimension(null, "x", nx);
            Dimension yDim = ncOut.addDimension(null, "y", ny);
            Dimension zDim = ncOut.addDimension(null, "z", nz + 1);
            Dimension[] dimArr = {timeDim, zDim, yDim, xDim};
            Dimension[] dimArrZeta = {timeDim, yDim, xDim};
            List<Dimension> listDimArr = new ArrayList<>(Arrays.asList(dimArr));
            List<Dimension> listDimArrZeta = new ArrayList<>(Arrays.asList(dimArrZeta));
                       
            ncOut.addVariable(null,"U", DataType.FLOAT, listDimArr);
            ncOut.addVariable(null,"V", DataType.FLOAT, listDimArr);
            ncOut.addVariable(null,"W", DataType.FLOAT, listDimArr);
            ncOut.addVariable(null,"Zeta", DataType.FLOAT, listDimArrZeta);
            ncOut.addVariable(null,"Depth", DataType.FLOAT, listDimArr);
            ncOut.create();

            // Opening of NetCDF file and reading of U/V/Zeta
            NetcdfFile nc;
            nc = NetcdfDataset.openDataset(f);
            
            // Correct the time counter to read all variables
            int ntime = nc.findDimension("time").getLength();
            
            // Update the counter of U/V/Zeta
            countU[0] = ntime;
            countV[0] = ntime;
            countZeta[0] = ntime;
            
            // Read all
            Array arrU = ncIn.findVariable(strU).read(origin, countU).reduce();
            Array arrV = ncIn.findVariable(strV).read(origin, countV).reduce();
            Array arrZeta = ncIn.findVariable(strZeta).read(originZeta, countZeta).reduce();
            Index index;
                
            nc.close();
            
            // Loop over all the time variables
            for (int t = 0; t < ntime; t++) {
                
                System.out.println("time index " + t);
                
                // Read Zeta
                index = arrZeta.getIndex();
                zeta_tp1 = new float[ny][nx];
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        zeta_tp1[j][i] = arrZeta.getFloat(index.set(t, j, i));
                    }
                }
                
                // Read U
                u_tp1 = new float[nz][ny][nx - 1];
                index = arrU.getIndex();
                for (int k = 0; k < nz; k++) {
                    for (int j = 0; j < ny; j++) {
                        for (int i = 0; i < nx - 1; i++) {
                            u_tp1[k][j][i] = arrU.getFloat(index.set(t, k, j, i));
                        }
                    }
                }

                // Read V
                v_tp1 = new float[nz][ny - 1][nx];
                index = arrV.getIndex();
                for (int k = 0; k < nz; k++) {
                    for (int j = 0; j < ny - 1; j++) {
                        for (int i = 0; i < nx; i++) {
                            v_tp1[k][j][i] = arrV.getFloat(index.set(t, k, j, i));
                        }
                    }
                }

                // Computes Z(t, z, y, x)
                z_w_tp1 = this.getSigLevels(); //[nz + 1][ny][nx];
                
                // Compute W
                w = this.computeW();  // [nz + 1][ny][nx];

                // Prepare arrays for writting
                ArrayFloat.D4 arrayW = new ArrayFloat.D4(1, nz + 1, ny, nx);
                for (int k = 0; k < nz; k++) {
                    for (int j = 0; j < ny; j++) {
                        for (int i = 0; i < nx - 1; i++) {
                            arrayW.set(0, k, j, i, (float) w[k][j][i]);
                        }
                    }
                }
                
                ArrayFloat.D4 arrayZ = new ArrayFloat.D4(1, nz + 1, ny, nx);
                for (int k = 0; k < nz; k++) {
                    for (int j = 0; j < ny; j++) {
                        for (int i = 0; i < nx - 1; i++) {
                            arrayZ.set(0, k, j, i, (float) z_w_tp1[k][j][i]);
                        }
                    }
                }

                ArrayFloat.D4 arrayU = new ArrayFloat.D4(1, nz + 1, ny, nx);
                for (int k = 0; k < nz; k++) {
                    for (int j = 0; j < ny; j++) {
                        for (int i = 0; i < nx - 1; i++) {
                            arrayU.set(0, k, j, i, u_tp1[k][j][i]);
                        }
                    }
                }
                
                ArrayFloat.D4 arrayV = new ArrayFloat.D4(1, nz + 1, ny, nx);
                for (int k = 0; k < nz; k++) {
                    for (int j = 0; j < ny - 1; j++) {
                        for (int i = 0; i < nx; i++) {
                            arrayV.set(0, k, j, i, v_tp1[k][j][i]);
                        }
                    }
                }
                
                originOut[0] = t;
                ncOut.write(ncOut.findVariable("U"), originOut, arrayU);
                ncOut.write(ncOut.findVariable("V"), originOut, arrayV);
                ncOut.write(ncOut.findVariable("Depth"), originOut, arrayZ);
                ncOut.write(ncOut.findVariable("W"), originOut, arrayW);
                
            }  // end of time loop

            ncOut.close();
            
        }  // end of ncfiles loop

        System.exit(0);

    }  // end of method

}
