/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

/**
 *
 * @author pverley
 */
public interface IAction {

    public void loadParameters();
    
    public void execute(IBasicParticle particle);

    public boolean isEnabled();

}
