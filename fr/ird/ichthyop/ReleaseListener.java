/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop;

import java.util.EventListener;

/**
 *
 * @author pverley
 */
public interface ReleaseListener extends EventListener {

    public void releaseTriggered(ReleaseEvent e);

}
