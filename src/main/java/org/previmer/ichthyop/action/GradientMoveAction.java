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

package org.previmer.ichthyop.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.dataset.DatasetUtil;

/**
 *
 * @author pverley
 */
public class GradientMoveAction extends AbstractAction {

    private double speed;
    private int dt;
    private String varName;
    private String direction;
    private int stride;

    @Override
    public void loadParameters() throws Exception {

        varName = getParameter("variable");
        speed = Double.valueOf(getParameter("speed"));
        direction = getParameter("direction");
        stride = 1;

        getSimulationManager().getDataset().requireVariable(varName, getClass());
        getSimulationManager().getOutputManager().addCustomTracker(varName);
        dt = getSimulationManager().getTimeManager().get_dt();
    }
    
    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

    @Override
    public void execute(IParticle particle) {

        int i = (int) Math.round(particle.getX());
        int j = (int) Math.round(particle.getY());
        int k = (int) Math.round(particle.getZ());
        Cell cell = new Cell(i, j, k);

        List<Cell> cells = getNeighborCells(cell);
        double time = getSimulationManager().getTimeManager().getTime();
        double val1 = getValue(cell, time);
        double dval = 0.d;
        Cell attractiveCell = null;
        int sign = direction.equals("\\+")
                ? 1
                : -1;
        for (Cell ncell : cells) {
            double val2 = getValue(ncell, time);
            double dvaltmp = sign * (val1 - val2) / cell.distance(ncell);
            if (dvaltmp > dval) {
                dval = dvaltmp;
                attractiveCell = ncell;
            }
        }

        if (null != attractiveCell) {
            double[] unit_direction = cell.direction(attractiveCell);
            double dx = speed * unit_direction[0] * dt;
            double dy = speed * unit_direction[1] * dt;
            particle.increment(new double[]{dx, dy});
        }
    }

    private double getValue(Cell cell, double time) {
        return getSimulationManager().getDataset().get(varName, new double[]{cell.i, cell.j, cell.k}, time).doubleValue();
    }

    private List<Cell> getNeighborCells(Cell cell) {

        int im1 = Math.max(cell.i - stride, 0);
        int ip1 = Math.min(cell.i + stride, getSimulationManager().getDataset().get_nx() - 1);
        int jm1 = Math.max(cell.j - stride, 0);
        int jp1 = Math.min(cell.j + stride, getSimulationManager().getDataset().get_ny() - 1);

        ArrayList<Cell> neighbors = new ArrayList();

        for (int ii = im1; ii <= ip1; ii++) {
            for (int jj = jm1; jj <= jp1; jj++) {
                if (getSimulationManager().getDataset().isInWater(ii, jj)) {
                    neighbors.add(new Cell(ii, jj, cell.k));
                }
            }
        }
        neighbors.remove(cell);
        Collections.shuffle(neighbors);
        return neighbors;
    }

    private class Cell {

        int i, j, k;

        Cell(int i, int j, int k) {
            this.i = i;
            this.j = j;
            this.k = k;
        }

        boolean matches(int i, int j, int k) {
            return (this.i == i) && (this.j == j) && (this.k == k);
        }

        double distance(Cell cell) {
            double[] pos1 = getSimulationManager().getDataset().xy2latlon(i, j);
            double[] pos2 = getSimulationManager().getDataset().xy2latlon(cell.i, cell.j);
            return getSimulationManager().getDataset().getDistGetter().getDistance(pos1[0], pos1[1], pos2[0], pos2[1]);
            //return DatasetUtil.geodesicDistance(pos1[0], pos1[1], pos2[0], pos2[1]);
        }

        double[] direction(Cell cell) {
            double distance = distance(cell);
            if (distance > 0) {
                double dx = (cell.i - i) / distance;
                double dy = (cell.j - j) / distance;
                return new double[]{-dx, -dy};
            } else {
                return new double[]{0.d, 0.d};
            }
        }
    }
}
