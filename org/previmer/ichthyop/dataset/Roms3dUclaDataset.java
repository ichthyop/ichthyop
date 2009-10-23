/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.dataset;

/**
 *
 * @author pverley
 */
public class Roms3dUclaDataset extends Roms3dDataset {

    @Override
    float getHc() {
        return ncIn.findGlobalAttribute(getParameter("field_attrib_hc")).getNumericValue().floatValue();
    }
}
