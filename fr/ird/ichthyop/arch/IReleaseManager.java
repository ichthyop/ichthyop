/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.ReleaseListener;
import fr.ird.ichthyop.ReleaseSchedule;
import fr.ird.ichthyop.io.XBlock;

/**
 *
 * @author pverley
 */
public interface IReleaseManager extends ReleaseListener {
    
    public XBlock getXReleaseProcess(String key);

    public ReleaseSchedule getSchedule();

    public String getParameter(String key);
}
