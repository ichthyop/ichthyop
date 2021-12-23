/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 * 
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 * 
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import org.previmer.ichthyop.calendar.Day360Calendar;
import org.previmer.ichthyop.manager.SimulationManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author pverley
 */
public class DateEditor extends DefaultCellEditor {

    /**
     *
     */
    private static final long serialVersionUID = 4300145258790167715L;
    /**
     * The simple date format parses and formats dates in human readable format. The
     * pattern for date-time formatting depends on the calendar (Calendar1900 or
     * ClimatoCalendar)
     */
    // private SimpleDateFormat dtFormat;
    /**
     * The calendar to convert specific instant in time to date.
     */
    private JFormattedTextField ftf;
    //private boolean DEBUG = false;
    public final static int DATE = 0;
    public final static int DURATION = 1;
    final String datePattern = "year \\d\\d\\d\\d month \\d\\d day \\d\\d at \\d\\d:\\d\\d";
    final String durationPattern = "\\d\\d\\d day\\(s\\) \\d\\d hour\\(s\\) \\d\\d minute\\(s\\)";
    final String displayDatePattern = "year YYYY month \\d\\d day \\d\\d at \\d\\d:\\d\\d";
    final String displayDurationPattern = "DDD day(s) HH hour(s) MM minute(s)";
    String displayPattern;
    Matcher matcher;
    Pattern pattern;
    
    public DateEditor(int type, Object value) {
        super(new JFormattedTextField());
        ftf = (JFormattedTextField) getComponent();
        // dtFormat = (type == DATE)
        //         ? getSimulationManager().getTimeManager().getInputDateFormat()
        //         : getSimulationManager().getTimeManager().getInputDurationFormat();
        pattern = (type == DATE) ? Pattern.compile(datePattern) : Pattern.compile(durationPattern);
        displayPattern = (type == DATE) ? displayDatePattern : displayDurationPattern;
        //dtFormat.setCalendar(getCalendar());
        //ftf.setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(dtFormat)));=
        // ftf.setValue(dtFormat.parse(value.toString()));
        ftf.setValue(value.toString());
        ftf.setHorizontalAlignment(JTextField.TRAILING);
        ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);

        //React when the user presses Enter while the editor is
        //active.  (Tab is handled as specified by
        //JFormattedTextField's focusLostBehavior property.)
        ftf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
        ftf.getActionMap().put("check", new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 8211738114672943087L;

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean test1 = ftf.isEditValid();
                boolean test2 = pattern.matcher(ftf.getText()).matches();
                if (!ftf.isEditValid() || !pattern.matcher(ftf.getText()).matches()) { //The text is invalid.
                    if (userSaysRevert()) { //reverted
                        ftf.postActionEvent(); //inform the editor
                    }
                } else {
                    try {              //The text is valid,
                        ftf.commitEdit();     //so use it.
                        ftf.postActionEvent(); //stop editing
                    } catch (java.text.ParseException exc) {
                    }
                }
            }
        });
    }

    // private Calendar getCalendar() {
    //     return calendar;
    // }

    // public void setCalendar(Calendar calendar) {
    //     this.calendar = calendar;
    //     dtFormat.setCalendar(calendar);
    // }

    //Override to check whether the edit is valid,
    //setting the value if it is and complaining if
    //it isn't.  If it's OK for the editor to go
    //away, we need to invoke the superclass's version
    //of this method so that everything gets cleaned up.
    @Override
    public boolean stopCellEditing() {
        JFormattedTextField txtField = (JFormattedTextField) getComponent();
        if (txtField.isEditValid()) {
            try {
                txtField.commitEdit();
            } catch (java.text.ParseException exc) {
            }

        } else { //text is invalid
            if (!userSaysRevert()) { //user wants to edit
                return false; //don't let the editor go away
            }
        }
        return super.stopCellEditing();
    }

    /**
     * Lets the user know that the text they entered is
     * bad. Returns true if the user elects to revert to
     * the last good value.  Otherwise, returns false,
     * indicating that the user wants to continue editing.
     * @return true to revert to last good code
     */
    protected boolean userSaysRevert() {
        Toolkit.getDefaultToolkit().beep();
        ftf.selectAll();
        Object[] options = {"Edit", "Revert"};
        int answer = JOptionPane.showOptionDialog(
                SwingUtilities.getWindowAncestor(ftf),
                "The value must match the pattern " + displayPattern + "\n"
                + "You can either continue editing "
                + "or revert to the last valid value.",
                "Invalid Text Entered",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[1]);

        if (answer == 1) { //Revert!
            ftf.setValue(ftf.getValue());
            return true;
        }
        return false;
    }

    // private SimulationManager getSimulationManager() {
    //     return SimulationManager.getInstance();
    // }
}
