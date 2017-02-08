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
package org.ichthyop.action;

import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.ichthyop.particle.IParticle;
import org.ichthyop.util.MTRandom;

/**
 *
 * @author pverley
 */
public class LevyWalkAction extends AbstractAction {

    private double alpha;
    private double vmax;
    private UniformRealDistribution urd1;
    private UniformRealDistribution urd2;
    private MTRandom rd;

    @Override
    public void loadParameters() throws Exception {
        alpha = Double.valueOf(getParameter("alpha"));
        urd1 = new UniformRealDistribution();
        vmax = Double.valueOf(getParameter("vmax"));
        urd2 = new UniformRealDistribution(Math.pow(2, -alpha), Math.pow(1, -alpha));
        rd = new MTRandom();
    }

    @Override
    public void execute(IParticle particle) {

        double theta = urd1.sample() * 2 * Math.PI;
        //double f = vmax * levywalk1();
        double f = vmax * levywalk2();

        // Converts into dx, dy move
        double dt = getSimulationManager().getTimeManager().get_dt();
        int i = (int) Math.round(particle.getX());
        int j = (int) Math.round(particle.getY());
        double dx = f * Math.cos(theta) / getSimulationManager().getDataset().getdxi(j, i) * dt;
        double dy = f * Math.sin(theta) / getSimulationManager().getDataset().getdeta(j, i) * dt;
        particle.increment(new double[]{dx, dy});
    }

    @Override
    public void init(IParticle particle) {
        // nothing to do
    }

    /**
     * http://stackoverflow.com/questions/19208502/levy-walk-simulation-in-r
     *
     * @return
     */
    private double levywalk1() {
        return (Math.pow(urd2.sample(), -1. / alpha) - 1.);
    }

    /**
     * http://markread.info/2016/08/code-to-generate-a-levy-distribution/
     *
     *
     * Harris, T. H., Banigan, E. J., Christian, D. a., Konradt, C., Tait Wojno,
     * E. D., Norose, K., … Hunter, C. a. (2012). Generalized Lévy walks and the
     * role of chemokines in migration of effector CD8+ T cells. Nature,
     * 486(7404), 545–548. http://doi.org/10.1038/nature11098. The supplementary
     * materials of this paper contain the equation for generating a Lévy
     * distribution. It’s in Nature, so I hope it’s correct.
     *
     * Jacobs, K. (2010). Stochastic Processes for Physicists: Understanding
     * Noisy Systems. Cambridge University Press, Cambridge.
     * http://doi.org/10.1017/CBO9781107415324.004. This book contains the
     * method that the Harris paper (above) employs. Section 9.2.2.
     *
     * Plank, M. J., Auger-Méthé, M., Codling, E. A., Plank, M. J., Auger-Méthé,
     * M., & Codling, E. A. (2013). Lévy or Not? Analysing Positional Data from
     * Animal Movement Paths. In M. Lewis, P. Maini, & S. Petrovskii (Eds.),
     * Dispersal, Individual Movement and Spatial Ecology; A Mathematical
     * Perspective (pp. 33–52). Springer.
     * http://doi.org/10.1007/978-3-642-35497-7. This paper provides a very good
     * explanation of the pitfalls in determining that some phenomenon is
     * performing a Lévy walk. There are other papers too. Find the references
     * in my recent PLOS Computational Biology paper!
     *
     *
     */
    private double levywalk2() {

        double X = bounded_uniform(-Math.PI / 2.0, Math.PI / 2.0);
        // uses Mersenne Twister random number generator to retrieve a value between (0,1) (does not include 0 or 1
        // themselves)
        double Y = -Math.log(nextDouble(false, false));

        double Z = (Math.sin(alpha * X) / Math.pow(Math.cos(X), 1.0 / alpha))
                * Math.pow(Math.cos((1.0 - alpha) * X) / Y, (1.0 - alpha) / alpha);
        return Math.abs(Z);
    }

    private double nextDouble(boolean includeZero, boolean includeOne) {
        double d = 0.0;
        do {
            d = rd.nextDouble();                           // grab a value, initially from half-open [0.0, 1.0)
            if (includeOne && rd.nextBoolean()) {
                d += 1.0;  // if includeOne, with 1/2 probability, push to [1.0, 2.0)
            }
        } while ((d > 1.0)
                || // everything above 1.0 is always invalid
                (!includeZero && d == 0.0));            // if we're not including zero, 0.0 is invalid
        return d;
    }

    private double bounded_uniform(double low, double high) {
        // returns a double in inverval (0,1). IE, neither zero nor one will be returned. 		
        double x = nextDouble(false, false);

        double range = high - low;
        x *= range;	// scale onto the required range of values
        x += low;	// translate x onto the values requested

        return x;
    }

}
