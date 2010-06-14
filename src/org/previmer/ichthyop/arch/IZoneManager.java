/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

import org.previmer.ichthyop.*;
import java.util.ArrayList;

/**
 *
 * @author pverley
 */
public interface IZoneManager {

    public ArrayList<Zone> getZones(TypeZone type);

    public void loadZonesFromFile(String filename, TypeZone type);

    public void cleanup();

}
