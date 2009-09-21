/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

/**
 *
 * @author pverley
 */
public interface IRecruitableParticle extends IZoneParticle {

    public boolean isRecruited();

    public boolean isRecruited(int num_zone);

    public void setRecruited(int num_zone, boolean recruited);

    public boolean isNewRecruited();

    public void setNewRecruited(boolean recruited);

    public int getNumRecruitmentZone();

    public void setNumRecruitmentZone(int numZone);

}
