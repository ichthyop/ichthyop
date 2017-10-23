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

package org.ichthyop.action;

import org.ichthyop.util.Constant;
import org.ichthyop.particle.RecruitableParticle;
import org.ichthyop.particle.IParticle;
import org.ichthyop.dataset.DatasetUtil;
import org.ichthyop.output.RecruitmentStainTracker;
import org.ichthyop.particle.LengthParticle;
import org.ichthyop.ui.LonLatConverter;
import org.ichthyop.ui.LonLatConverter.LonLatFormat;

/**
 *
 * @author mcuif
 */
public class RecruitmentStainAction extends AbstractAction {

    /**
     * Minimum duration [second] a particle has to spend within the same zone
     * before being recruited.
     */
    private static int durationMinInRecruitArea;
    /**
     * Duration [second] presently spent by the particule within the
     * current zone.
     */
    private float ageMinAtRecruitment;
    private float lengthMinAtRecruitment;
    private boolean isAgeCriterion;
    private boolean stopMovingOnceRecruited;
    private double lon_stain, lat_stain, depth_stain;
    private double radius_stain;
    private double thickness_stain;
    private boolean is3D;
    private static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    
    @Override
    public String getKey() {
        return "action.recruitment.stain";
    }

    @Override
    public void loadParameters() throws Exception {

        isAgeCriterion = getConfiguration().getString("action.recruitment.stain.criterion").equals("Age criterion");
        boolean isGrowth = getConfiguration().getBoolean("action.growth.enabled");
        if (!isGrowth && !isAgeCriterion) {
            throw new IllegalArgumentException("{Recruitment} Recruitment criterion cannot be based on particle length since the growth model is not activated. Activate the growth model or set a recruitment criterion based on particle age.");
        }
        if (isAgeCriterion) {
            ageMinAtRecruitment = getConfiguration().getFloat("action.recruitment.stain.limit_age");
        } else {
            lengthMinAtRecruitment = getConfiguration().getFloat("action.recruitment.stain.limit_length");
        }
        stopMovingOnceRecruited = getConfiguration().getBoolean("action.recruitment.stain.stop_moving");

        radius_stain = getConfiguration().getFloat("action.recruitment.stain.radius_stain");
        lon_stain = Double.valueOf(LonLatConverter.convert(getConfiguration().getString("action.recruitment.stain.lon_stain"), LonLatFormat.DecimalDeg));
        lat_stain = Double.valueOf(LonLatConverter.convert(getConfiguration().getString("action.recruitment.stain.lat_stain"), LonLatFormat.DecimalDeg));
        if (is3D) {
            thickness_stain = getConfiguration().getFloat("action.recruitment.stain.thickness_stain");
            depth_stain = getConfiguration().getFloat("action.recruitment.stain.depth_stain");
        }

        boolean addTracker = true;
        try {
            addTracker = getConfiguration().getBoolean("action.recruitment.stain.recruited_tracker");
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(RecruitmentStainTracker.class);
        }
    }
    
    @Override
    public void init(IParticle particle) {
        RecruitableParticle.init(particle);
    }

    @Override
    public void execute(IParticle particle) {

        if (stopMovingOnceRecruited && RecruitableParticle.isRecruited(particle)) {
            particle.lock();
            return;
        }

        if (!RecruitableParticle.isRecruited(particle)) {
            if (satisfyRecruitmentCriterion(particle) && isParticleInsideStain(particle)) {
                RecruitableParticle.setRecruited(particle, 0, true);
            }
        }
    }

    private boolean isParticleInsideStain(IParticle particle) {

        double distance = DatasetUtil.geodesicDistance(lat_stain, lon_stain, particle.getLat(), particle.getLon());
        boolean isInside = (distance <= radius_stain);
        if (is3D && isInside) {
            distance = Math.abs((Math.abs(particle.getDepth()) - Math.abs(depth_stain)));
            isInside = (distance <= (0.5d * thickness_stain));
        }
        return isInside;
    }

    private boolean satisfyRecruitmentCriterion(IParticle particle) {
        if (isAgeCriterion) {
            return ((float) particle.getAge() / Constant.ONE_DAY) >= ageMinAtRecruitment;
        } else {
            return (LengthParticle.getLength(particle) >= lengthMinAtRecruitment);
        }
    }

    public boolean isStopMoving() {
        return stopMovingOnceRecruited;
    }
}