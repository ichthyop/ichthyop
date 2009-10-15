/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.event.ReleaseEvent;

/**
 *
 * @author pverley
 */
public interface IReleaseProcess extends ISimulationAccessor {

    public void release(ReleaseEvent event);

    public int getNbParticles();

}
