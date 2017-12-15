/*
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2017
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
package org.ichthyop.dataset.variable;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import org.ichthyop.dataset.DatasetUtil;
import org.ichthyop.grid.IGrid;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public class NetcdfDatasetVariable extends AbstractDatasetVariable {

    private final List<String> locations;
    private final String name;
    private final int tilingh;
    private final int tilingv;
    private String variable_time;
    private final boolean enhanced;
    private int index;

    public NetcdfDatasetVariable(List<String> locations, String name,
            int nlayer, IGrid grid, int tilingh, int tilingv,
            Calendar calendar, double t0,
            boolean enhanced) {
        super(nlayer, grid, calendar, t0);
        this.locations = locations;
        this.name = name;
        this.tilingh = tilingh;
        this.tilingv = tilingv;
        this.enhanced = enhanced;
        this.index = 0;
    }

    @Override
    public void init(double t0, int time_arrow) throws IOException {

        // find time variable
        NetcdfFile nc = open();
        variable_time = DatasetUtil.findTimeVariable(nc);
        nc.close();

        double time = t0;
        index = locations.size() > 1
                ? DatasetUtil.index(locations, time, time_arrow, variable_time)
                : 0;
        nc = open();
        int rank = DatasetUtil.rank(time, nc, variable_time, time_arrow) - time_arrow;
        for (int ilayer = 0; ilayer < nlayer - 1; ilayer++) {
            rank += time_arrow;
            int ntime = nc.findVariable(variable_time).getShape()[0];
            if (rank > (ntime - 1) || rank < 0) {
                nc.close();
                if (DatasetUtil.hasNext(locations, index)) {
                    index = DatasetUtil.next(locations, index, time_arrow);
                    nc = open();
                    ntime = nc.findVariable(variable_time).getShape()[0];
                    rank = (1 - time_arrow) / 2 * (ntime - 1);
                    time = DatasetUtil.timeAtRank(nc, variable_time, rank);
                    update(new TiledVariable(open(), name, grid, rank, time, tilingh, tilingv));
                } else {
                    update(null);
                }
            } else {
                time = DatasetUtil.timeAtRank(nc, variable_time, rank);
                update(new TiledVariable(open(), name, grid, rank, time, tilingh, tilingv));
            }
        }
        nc.close();
    }

    @Override
    public void update(double currenttime, int time_arrow) throws IOException {

        double time = currenttime;
        if (updateNeeded(time, time_arrow)) {
            int rank;
            NetcdfFile nc = open();
            int ntime = nc.findVariable(variable_time).getShape()[0];
            rank = DatasetUtil.rank(time, nc, variable_time, time_arrow);
            rank += (nlayer - 1) * time_arrow;
            if (rank > (ntime - 1) || rank < 0) {
                if (DatasetUtil.hasNext(locations, index)) {
                    nc.close();
                    index = DatasetUtil.next(locations, index, time_arrow);
                    nc = open();
                    ntime = nc.findVariable(variable_time).getShape()[0];
                    rank = (1 - time_arrow) / 2 * (ntime - 1);
                    time = DatasetUtil.timeAtRank(nc, variable_time, rank);
                    update(new TiledVariable(open(), name, grid, rank, time, tilingh, tilingv));
                } else {
                    update(null);
                }
            } else {
                time = DatasetUtil.timeAtRank(nc, variable_time, rank);
                update(new TiledVariable(open(), name, grid, rank, time, tilingh, tilingv));
            }
            nc.close();
        }
    }

    private NetcdfFile open() throws IOException {
        return DatasetUtil.open(locations.get(index), enhanced);
    }
}
