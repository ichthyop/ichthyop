/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

import java.util.Set;

/**
 *
 * @author pverley
 */
public interface IPopulation extends Set {

    public void step();

    public void clear();

}
