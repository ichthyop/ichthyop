/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.dataset;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class Roms3dRutgersDataset extends Roms3dDataset {

    @Override
    float getHc() {
        try {
            return ncIn.findVariable(getParameter("S-coordinate critical depth")).readScalarFloat();
        } catch (IOException ex) {
            Logger.getLogger(Roms3dRutgersDataset.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Float.NaN;
    }
}