/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public abstract class RecruitableParticle extends ZoneParticle implements IRecruitableParticle {

    private boolean[] isRecruited;
    private boolean isNewRecruited;
    private int numRecruitmentZone;

    public void init() {
        isNewRecruited = false;
        numRecruitmentZone = -1;
        isRecruited = new boolean[0];
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
