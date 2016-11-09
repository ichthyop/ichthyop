/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.TypeZone;

/**
 *
 * @author pverley
 */
    public class RecruitableParticleLayer extends ParticleLayer {

    public RecruitableParticleLayer(IParticle particle) {
        super(particle);
    }

    private boolean[] isRecruited;
    private boolean isNewRecruited;
    private int numRecruitmentZone;

    public void init() {
        isNewRecruited = false;
        numRecruitmentZone = -1;
        isRecruited = (null != getSimulationManager().getZoneManager().getZones(TypeZone.RECRUITMENT))
                ? new boolean[getSimulationManager().getZoneManager().getZones(TypeZone.RECRUITMENT).size()]
                : new boolean[1];
    }

    public boolean isRecruited() {

        for (boolean recruited : isRecruited) {
            if (recruited) {
                return true;
            }
        }
        return false;
    }

    public boolean isRecruited(int numZone) {

        return isRecruited[numZone];
    }

    public boolean isNewRecruited() {
        return isNewRecruited;
    }

    /**
     *  Sets the newly recruited status of the particle to false.
     */
    public void setNewRecruited(boolean recruited) {
        isNewRecruited = recruited;
    }

    public int getNumRecruitmentZone() {
        return numRecruitmentZone;
    }

    public void setNumRecruitmentZone(int numZone) {
        numRecruitmentZone = numZone;
    }

    public void setRecruited(int num_zone, boolean recruited) {
        isRecruited[num_zone] = recruited;
    }
}
