/*
 *  Copyright (C) 2011 mcuif
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.util.Constant;
import org.previmer.ichthyop.particle.GrowingParticleLayer;
import org.previmer.ichthyop.particle.RecruitableParticleLayer;

import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.dataset.DatasetUtil;
import org.previmer.ichthyop.io.RecruitmentStainTracker;
import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;

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

    public void loadParameters() throws Exception {

        isAgeCriterion = getParameter("criterion").equals(Criterion.AGE.toString());
        boolean isGrowth = getSimulationManager().getActionManager().isEnabled("action.growth");
        if (!isGrowth && !isAgeCriterion) {
            throw new IllegalArgumentException("{Recruitment} Recruitment criterion cannot be based on particle length since the growth model is not activated. Activate the growth model or set a recruitment criterion based on particle age.");
        }
        if (isAgeCriterion) {
            ageMinAtRecruitment = Float.valueOf(getParameter("limit_age"));
        } else {
            lengthMinAtRecruitment = Float.valueOf(getParameter("limit_length"));
        }
        stopMovingOnceRecruited = Boolean.valueOf(getParameter("stop_moving"));

        radius_stain = Float.valueOf(getParameter("radius_stain"));
        lon_stain = Double.valueOf(LonLatConverter.convert(getParameter("lon_stain"), LonLatFormat.DecimalDeg));
        lat_stain = Double.valueOf(LonLatConverter.convert(getParameter("lat_stain"), LonLatFormat.DecimalDeg));
        if (is3D) {
            thickness_stain = Float.valueOf(getParameter("thickness_stain"));
            depth_stain = Float.valueOf(getParameter("depth_stain"));
        }

        boolean addTracker = true;
        try {
            addTracker = Boolean.valueOf(getParameter("recruited_tracker"));
        } catch (Exception ex) {
            // do nothing and just add the tracker
        }
        if (addTracker) {
            getSimulationManager().getOutputManager().addPredefinedTracker(RecruitmentStainTracker.class);
        }
    }

    public void execute(IBasicParticle particle) {

        //@todo
        // catch cast exception
        RecruitableParticleLayer rParticle = (RecruitableParticleLayer) particle.getLayer(RecruitableParticleLayer.class);
        if (stopMovingOnceRecruited && rParticle.isRecruited()) {
            particle.lock();
            return;
        }

        if (!rParticle.isRecruited()) {
            if (satisfyRecruitmentCriterion(particle) && isParticleInsideStain(particle)) {
                rParticle.setRecruited(0, true);
            }
        }
    }

    private boolean isParticleInsideStain(IBasicParticle particle) {

        boolean isInside = false;
        double distance = DatasetUtil.geodesicDistance(lat_stain, lon_stain, particle.getLat(), particle.getLon());
        isInside = (distance <= radius_stain);
        if (is3D && isInside) {
            distance = Math.abs((Math.abs(particle.getDepth()) - Math.abs(depth_stain)));
            isInside = (distance <= (0.5d * thickness_stain));
        }
        return isInside;
    }

    private boolean satisfyRecruitmentCriterion(IBasicParticle particle) {
        if (isAgeCriterion) {
            return ((float) particle.getAge() / Constant.ONE_DAY) >= ageMinAtRecruitment;
        } else {
            return (((GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class)).getLength() >= lengthMinAtRecruitment);
        }
    }

    public boolean isStopMoving() {
        return stopMovingOnceRecruited;
    }

    public enum Criterion {

        LENGTH("Length criterion"),
        AGE("Age criterion");
        private String name;

        Criterion(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
