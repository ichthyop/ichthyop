/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

import java.util.Set;
import org.previmer.ichthyop.event.SetupListener;

/**
 *
 * @author pverley
 */
public interface IPopulation extends Set, SetupListener {

    public void step();

    public void clear();

}
