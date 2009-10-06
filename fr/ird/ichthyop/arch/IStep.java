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

    public long get_tO();

    public int get_dt();

    public long getTransportDuration();

    public Calendar getCalendar();

    public boolean hasNext();

    public void next();

    public String timeToString();

    public void setUp();

}
