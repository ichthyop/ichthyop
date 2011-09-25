/*
 * Copyright (C) 2011 pverley
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.previmer.ichthyop.arch.IBasicParticle;
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

    public void loadParameters() throws Exception {

        varName = getParameter("variable");
        speed = Double.valueOf(getParameter("speed"));
        direction = getParameter("direction");
        stride = 1;

        getSimulationManager().getDataset().requireVariable(varName, getClass());
        getSimulationManager().getOutputManager().addCustomTracker(varName);
        dt = getSimulationManager().getTimeManager().get_dt();
    }

    public void execute(IBasicParticle particle) {

        int i = (int) Math.round(particle.getX());
        int j = (int) Math.round(particle.getY());
        int k = (int) Math.round(particle.getZ());
        Cell cell = new Cell(i, j, k);

        List<Cell> cells = getNeighborCells(cell);
        long time = getSimulationManager().getTimeManager().getTime();
        double val1 = getValue(cell, time);
        double dval = 0.d;
        Cell attractiveCell = null;
        int sign = direction.matches("\\+")
                ? 1
                : -1;
        for (Cell ncell : cells) {
            double val2 = getValue(ncell, time);
            if (sign * (val1 - val2) > dval) {
                dval = val1 - val2;
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

    private double getValue(Cell cell, long time) {
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
            return DatasetUtil.geodesicDistance(pos1[0], pos1[1], pos2[0], pos2[1]);
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
