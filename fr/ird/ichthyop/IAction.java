/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public interface IAction extends ISimulationAccessor {

    public void loadParameters();
    
    public void execute(IBasicParticle particle);

    public boolean isActivated();

}
