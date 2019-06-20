/*
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics.
 * http://www.ichthyop.org
 *
 * Copyright (c) IRD (Institut de Recherche pour le Développement) 2007-2013
 *
 * Contributors:
 * Gwendoline ANDRES (),
 * Sylvain BONHOMMEAU (sylvain.bonhommeau@ifremer.fr)
 * Bruno BLANKE (blanke@univ-brest.fr),
 * Timothée BROCHIER (timothee.brochier@ird.fr),
 * Fabrice LECORNU (fabrice.lecornu@ifremer.fr),
 * Christophe LETT (christophe.lett@ird.fr),
 * Christian MULLON (christian.mullon@ird.fr),
 * Carolina PARADA (cparada@inpesca.cl),
 * Pierrick PENVEN (pierrick.penven@ird.fr),
 * Philippe VERLEY (philippe.verley@ird.fr),
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. It has been described in Lett, Christophe, Philippe
 * Verley, Christian Mullon, Carolina Parada, Timothée Brochier, Pierrick
 * Penven, and Bruno Blanke. “A Lagrangian Tool for Modelling Ichthyoplankton
 * Dynamics.” Environmental Modelling & Software 23, no. 9 (September 2008):
 * 1210–1214. doi:10.1016/j.envsoft.2008.02.005.
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
package org.previmer.ichthyop;

/**
 *
 * @author pverley
 */
public class Simulation {

    final private static Simulation simulation = new Simulation();

    public Population getPopulation() {
        return Population.getInstance();
    }

    public static Simulation getInstance() {
        return simulation;
    }

    public void step() {
        getPopulation().step();
    }
}
