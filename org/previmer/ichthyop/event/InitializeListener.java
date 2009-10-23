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
public interface InitializeListener extends EventListener {

    public void initializePerformed(InitializeEvent e);

}
