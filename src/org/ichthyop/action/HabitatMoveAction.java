/*
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2018
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Nicolas BARRIER, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
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
package org.ichthyop.action;

import org.ichthyop.particle.GriddedParticle;
import org.ichthyop.particle.IParticle;
import org.ichthyop.util.MTRandom;
import org.ichthyop.util.VonMisesRandom;

/**
 *
 * @author pverley
 */
public class HabitatMoveAction extends AbstractAction {

    private VonMisesRandom rd;
    private double alpha;
    private double vmax;
    private double tinf, tsup;
    private final double TO_RADIAN = Math.PI / 180.d;
    private final double TO_DEG = 180.d / Math.PI;
    private String variable_temp;

    @Override
    public String getKey() {
        return "action.habitat_move";
    }

    @Override
    public void loadParameters() throws Exception {

        // temperature
        variable_temp = "water_temp";
        getSimulationManager().getOceanDataset().requireVariable(variable_temp, HabitatMoveAction.class);
        tinf = 22;
        tsup = 26;

        // bathymetry
        // von mises
        rd = new VonMisesRandom();
        alpha = 1e8d;

        // vmax
        vmax = 2;
    }

    @Override
    public void execute(IParticle particle) {

        double[] xyz = GriddedParticle.xyz(particle);
        int i = (int) Math.round(xyz[0]);
        int j = (int) Math.round(xyz[1]);
        int k = (int) Math.round(xyz[2]);

        double dxij = getSimulationManager().getGrid().get_dx(i, j);
        double dyij = getSimulationManager().getGrid().get_dy(i, j);
        double dt = getSimulationManager().getTimeManager().get_dt();

        // temperature habitat
        //double tij = getSimulationManager().getOceanDataset().getVariable(variable_temp).getDouble(i, j, k);
        double tij = particle.getLat();
        double h_ij = suitability(tij, tinf, tsup, 1, 1);

        // temperature habitat gradient along x
        //double tip1j = getSimulationManager().getOceanDataset().getVariable(variable_temp).getDouble(i + 1, j, k);
        double tip1j = getSimulationManager().getGrid().getLat(i + 1, j);
        double h_tip1j = suitability(tip1j, tinf, tsup, 1, 1);
        //double tim1j = getSimulationManager().getOceanDataset().getVariable(variable_temp).getDouble(i - 1, j, k);
        double tim1j = getSimulationManager().getGrid().getLat(i - 1, j);
        double h_tim1j = suitability(tim1j, tinf, tsup, 1, 1);
        double deltahx = (h_tip1j - h_tim1j) / (2 * dxij);
        // temperature habitat gradient along y
        //double tijp1 = getSimulationManager().getOceanDataset().getVariable(variable_temp).getDouble(i, j + 1, k);
        double tijp1 = getSimulationManager().getGrid().getLat(i, j + 1);
        double h_tijp1 = suitability(tijp1, tinf, tsup, 1, 1);
        //double tijm1 = getSimulationManager().getOceanDataset().getVariable(variable_temp).getDouble(i, j - 1, k);
        double tijm1 = getSimulationManager().getGrid().getLat(i, j - 1);
        double h_tijm1 = suitability(tijm1, tinf, tsup, 1, 1);
        double deltahy = (h_tijp1 - h_tijm1) / (2 * dyij);
        // norm and angle
        double deltah = Math.sqrt(deltahx * deltahx + deltahy * deltahy);
        double thetah = Math.atan(deltahy / deltahx);

        // no active movement
        if (deltah < 1e-7) {
            return;
        }

        //System.out.println("i " + i + " j " + j + " temp " + tij + " hij " + h_ij);
        // von mises draw
        double theta = thetah + rd.nextDouble(alpha * deltah);
        //System.out.println("  deltah " + deltah + " deltahx " + deltahx + " + deltahy " + deltahy);
        //System.out.println("  thetah " + (thetah * TO_DEG) + " theta " + (theta * TO_DEG));
        double du = vmax * (1 - h_ij) * Math.cos(theta);
        double dv = vmax * (1 - h_ij) * Math.sin(theta);
        //System.out.println("  du " + du + " dv " + dv);

        // move
        double dx = du * dt / dxij;
        double dy = dv * dt / dyij;
        double[] latlon = getSimulationManager().getGrid().xy2latlon(xyz[0] + dx, xyz[1] + dy);
        particle.incrLat(latlon[0] - particle.getLat());
        particle.incrLon(latlon[1] - particle.getLon());
    }

    @Override
    public void init(IParticle particle) {
        // nothing to do
    }

    private double suitability(double value, double lim_inf, double lim_sup, double sharp_inf, double sharp_sup) {

        double s_inf = 1.d / (1 + Math.exp(sharp_inf * (lim_inf - value)));
        double s_sup = 1.d / (1.d + Math.exp(sharp_sup * (value - lim_sup)));
        return s_inf * s_sup;
    }
}
