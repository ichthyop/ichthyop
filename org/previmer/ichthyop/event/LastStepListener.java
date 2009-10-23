/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.event;

import java.util.EventListener;

/**
 *
 * @author pverley
 */
public interface LastStepListener extends EventListener {

     public void lastStepOccurred(LastStepEvent e);

}
