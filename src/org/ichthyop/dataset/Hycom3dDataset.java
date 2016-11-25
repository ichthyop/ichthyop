/*
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
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
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package org.ichthyop.dataset;

import java.util.Arrays;
import java.util.List;
import org.ichthyop.event.NextStepEvent;
import org.ichthyop.io.IOTools;

/**
 *
 * @author pverley
 */
public class Hycom3dDataset extends Hycom3dCommon {

    private List<String> uvFiles;
    private int index;

    @Override
    void loadParameters() {
    }

    @Override
    public void setUp() throws Exception {

        String path = "/data/hycom/glb_u008_912";

        // Bathymetry
        //nc = DatasetUtil.openFile(IOTools.resolveFile("/data/hycom/glb_u008_912/depth_GLBu0.08_07.nc"), true);
        //bathymetry = (float[][]) nc.findVariable("topo").read().reduce().copyToNDJavaArray();
        //nc.close();

        // List uv files
        uvFiles = DatasetUtil.list(path, "*uv3z*.nc");
        nc = DatasetUtil.openFile(uvFiles.get(0), true);

        // Latitude
        latitude = (double[]) nc.findVariableByAttribute(null, "standard_name", "latitude").read().copyTo1DJavaArray();
        j0 = 0;
        ny = latitude.length;
        
        // Longitude
        longitude = (double[]) nc.findVariableByAttribute(null, "standard_name", "longitude").read().copyTo1DJavaArray();
        i0 = 0;
        nx = longitude.length;
        
        // Depth
        depthLevel = (double[]) nc.findVariableByAttribute(null, "standard_name", "depth").read().copyTo1DJavaArray();
        nz = depthLevel.length;
        
        // Shrink domain
        shrink(35, 260, 10, 300);
        xTore = false;
        longitude = Arrays.copyOfRange(longitude, i0, i0+nx);
        latitude = Arrays.copyOfRange(latitude, j0, j0+ny);   
        
        // scale factors
        dyv = 111138.d * (latitude[1] - latitude[0]);
        dxu = new double[ny];
        for (int j = 0; j < ny; j++) {
            dxu[j] = dyv * Math.cos(Math.PI * latitude[j] / 180.d);
        }
        
        // extent
        getDimGeogArea();
        
        // Read U & V for the mask
        setAllFieldsTp1AtTime(0);
        
//        System.out.println(Arrays.toString(latlon2xy(-35, 15)));
//        double x = 125.5, y = 62.5;
//        System.out.println(isInWater(new double[]{x, y})+ " "+Arrays.toString(xy2latlon(x, y)));

    }

    @Override
    public void init() throws Exception {
        DatasetUtil.sort(uvFiles, "time", timeArrow());
        double t0 = getSimulationManager().getTimeManager().get_tO();
        index = DatasetUtil.index(uvFiles, t0, timeArrow(), "time");
        nc = DatasetUtil.openFile(uvFiles.get(index), true);
        nbTimeRecords = nc.findDimension("time").getLength();
        rank = DatasetUtil.rank(t0, nc, "time", timeArrow());
        time_tp1 = t0;
        setAllFieldsTp1AtTime(rank);
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = e.getSource().getTime();
        int time_arrow = timeArrow();

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        w_tp0 = w_tp1;
        //wr_tp0 = wr_tp1;
        rank += time_arrow;

        if (rank > (nbTimeRecords - 1) || rank < 0) {
            nc.close();
            index = DatasetUtil.next(uvFiles, index, time_arrow);
            nc = DatasetUtil.openFile(uvFiles.get(index), true);
            nbTimeRecords = nc.findDimension("time").getLength();
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }

        setAllFieldsTp1AtTime(rank);
    }

}
