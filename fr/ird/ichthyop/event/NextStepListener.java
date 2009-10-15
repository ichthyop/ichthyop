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
public interface NextStepListener extends EventListener {

     public void nextStepTriggered(NextStepEvent e);

}
