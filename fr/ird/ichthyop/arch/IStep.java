/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import java.util.Calendar;

/**
 *
 * @author pverley
 */
public interface IStep extends Cloneable {

    public long getTime();

    public int get_dt();

    public long getTransportDuration();

    public Calendar getCalendar();

}
