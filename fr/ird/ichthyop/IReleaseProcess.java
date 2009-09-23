/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop;

import fr.ird.ichthyop.arch.ISimulationAccessor;
import java.io.IOException;

/**
 *
 * @author pverley
 */
public interface IReleaseProcess extends ISimulationAccessor {

    public void release(ReleaseEvent event) throws IOException;

}
