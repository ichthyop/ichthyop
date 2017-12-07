/*
 * TimeConverterView.java
 */
package org.ichthyop.timeconverter.ui;

import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.Application;
import org.ichthyop.calendar.AllLeapCalendar;
import org.ichthyop.calendar.Day360Calendar;
import org.ichthyop.calendar.InterannualCalendar;
import org.ichthyop.calendar.JulianCalendar;
import org.ichthyop.calendar.NoLeapCalendar;
import org.ichthyop.calendar.ProlepticGregorianCalendar;
import org.ichthyop.calendar.StandardCalendar;

/**
 * The application's main frame.
 */
public class TimeConverterView extends FrameView {

    public TimeConverterView(SingleFrameApplication app) {
        super(app);

        initComponents();
    }

    @Action
    public void clearTextField() {
        textFieldValue.setText("");
    }

    @Action
    public void convert() {
        String origin = textFieldOrigin.getText().trim();
        String value = textFieldValue.getText().trim();
        String result = "Unable to convert " + value;

        if (value.isEmpty()) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

        try {
            calendar.setTime(dateFormat.parse(origin));
            int year_o = calendar.get(Calendar.YEAR);
            int month_o = calendar.get(Calendar.MONTH);
            int day_o = calendar.get(Calendar.DAY_OF_MONTH);
            int hour_o = calendar.get(Calendar.HOUR_OF_DAY);
            int min_o = calendar.get(Calendar.MINUTE);
            String calendarClass = (String) calendarComboBox.getSelectedItem();
            switch (calendarClass) {
                case "org.previmer.ichthyop.calendar.AllLeapCalendar":
                    calendar = new AllLeapCalendar(year_o, month_o, day_o, hour_o, min_o);
                    break;
            case "org.previmer.ichthyop.calendar.Day360Calendar":
                calendar = new Day360Calendar(year_o, month_o, day_o, hour_o, min_o);
                break;
            case "org.previmer.ichthyop.calendar.InterannualCalendar":
                calendar = new InterannualCalendar(year_o, month_o, day_o, hour_o, min_o);
                break;
            case "org.previmer.ichthyop.calendar.JulianCalendar":
                calendar = new JulianCalendar(year_o, month_o, day_o, hour_o, min_o);
                break;
            case "org.previmer.ichthyop.calendar.NoLeapCalendar":
                calendar = new NoLeapCalendar(year_o, month_o, day_o, hour_o, min_o);
                break;
            case "org.previmer.ichthyop.calendar.ProlepticGregorianCalendar":
                calendar = new ProlepticGregorianCalendar(year_o, month_o, day_o, hour_o, min_o);
                break;
            case "org.previmer.ichthyop.calendar.StandardCalendar":
                calendar = new StandardCalendar(year_o, month_o, day_o, hour_o, min_o);
                break;
            }
            btnConvert.getAction().setEnabled(true);
        } catch (ParseException ex) {

            Logger.getLogger(TimeConverterView.class.getName()).log(Level.SEVERE, null, ex);
            result = "Error parsing origin date " + origin;
            btnConvert.getAction().setEnabled(false);
        }

        if (btnConvert.getAction().isEnabled()) {
            TimeZone tz = TimeZone.getTimeZone("GMT");
            SimpleTimeZone stz = new SimpleTimeZone(0, tz.getID());
            calendar.setTimeZone(stz);
            dateFormat.setCalendar(calendar);
            if (value.split("/").length > 1) {
                try {
                    calendar.setTime(dateFormat.parse(value));
                    result = String.valueOf(calendar.getTimeInMillis() / 1000);
                } catch (ParseException ex) {
                    Logger.getLogger(TimeConverterView.class.getName()).log(Level.SEVERE, null, ex);
                    result = "Error converting " + value + " into seconds.";
                }
            } else {
                try {
                    long seconds = Double.valueOf(value).longValue();
                    value = String.valueOf(seconds);
                    calendar.setTimeInMillis(seconds * 1000L);
                    result = dateFormat.format(calendar.getTime());
                } catch (NumberFormatException ex) {
                    Logger.getLogger(TimeConverterView.class.getName()).log(Level.SEVERE, null, ex);
                    result = "Error converting " + value + " into date.";
                }
            }
        }
        calendar.setTimeInMillis(0);
        String corigin = dateFormat.format(calendar.getTime());
        DefaultTableModel model = (DefaultTableModel) tableConversion.getModel();
        model.insertRow(0, new String[]{calendar.getClass().getSimpleName(), corigin, value, result});
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {

        mainPanel = new JPanel();
        pnlCalendar = new JPanel();
        calendarComboBox = new JComboBox();
        lblOrigin = new JLabel();
        lblCalendar = new JLabel();
        textFieldOrigin = new JTextField();
        pnlConversion = new JPanel();
        textFieldValue = new JTextField();
        btnConvert = new JButton();
        btnClear = new JButton();
        scrollPaneConversion = new JScrollPane();
        tableConversion = new JTable();
        lblHelp = new JLabel();

        mainPanel.setName("mainPanel");

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.ichthyop.timeconverter.ui.TimeConverterApp.class).getContext().getResourceMap(TimeConverterView.class);
        pnlCalendar.setBorder(BorderFactory.createTitledBorder(resourceMap.getString("pnlCalendar.border.title")));
        pnlCalendar.setName("pnlCalendar");

        lblOrigin.setName("lblOrigin");
        lblOrigin.setText(resourceMap.getString("lblOrigin.text"));

        lblCalendar.setText(resourceMap.getString("lblCalendar.text"));
        lblCalendar.setName("lblCalendar");

        textFieldOrigin.setName("textFieldOrigin");
        textFieldOrigin.setText(resourceMap.getString("textFieldOrigin.text"));

        String[] calendars = new String[]{
            "org.previmer.ichthyop.calendar.AllLeapCalendar",
            "org.previmer.ichthyop.calendar.Day360Calendar",
            "org.previmer.ichthyop.calendar.InterannualCalendar",
            "org.previmer.ichthyop.calendar.JulianCalendar",
            "org.previmer.ichthyop.calendar.NoLeapCalendar",
            "org.previmer.ichthyop.calendar.ProlepticGregorianCalendar",
            "org.previmer.ichthyop.calendar.StandardCalendar"
        };
        calendarComboBox.setModel(new DefaultComboBoxModel(calendars));
        calendarComboBox.setName("calendarComboBox");
        calendarComboBox.setEditable(false);
        calendarComboBox.setSelectedIndex(0);

        GroupLayout layoutCalendar = new GroupLayout(pnlCalendar);
        pnlCalendar.setLayout(layoutCalendar);
        layoutCalendar.setHorizontalGroup(
                layoutCalendar.createParallelGroup()
                .addGroup(layoutCalendar.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblCalendar)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(calendarComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                .addGroup(layoutCalendar.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblOrigin)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldOrigin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
        );
        layoutCalendar.setVerticalGroup(
                layoutCalendar.createParallelGroup()
                .addGroup(layoutCalendar.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layoutCalendar.createParallelGroup()
                                .addComponent(lblCalendar)
                                .addComponent(calendarComboBox))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layoutCalendar.createParallelGroup()
                                .addComponent(lblOrigin)
                                .addComponent(textFieldOrigin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
        );

        pnlConversion.setBorder(BorderFactory.createTitledBorder(resourceMap.getString("pnlConversion.border.title")));
        pnlConversion.setName("pnlConversion");

        textFieldValue.setHorizontalAlignment(JTextField.RIGHT);
        textFieldValue.setText(resourceMap.getString("textFieldValue.text"));
        textFieldValue.setName("textFieldValue");
        textFieldValue.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnConvert.doClick();
                }
            }
        });

        ActionMap actionMap = Application.getInstance(TimeConverterApp.class).getContext().getActionMap(TimeConverterView.class, this);
        btnConvert.setAction(actionMap.get("convert"));
        btnConvert.setName("btnConvert");

        btnClear.setAction(actionMap.get("clearTextField"));
        btnClear.setName("btnClear");

        scrollPaneConversion.setName("scrollPaneConversion");

        tableConversion.setModel(new DefaultTableModel(
                null,
                new String[]{"Calendar", "Epoch", "User value", "Converted value"}) {

            @Override
            public Class getColumnClass(int columnIndex) {
                return String.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        tableConversion.setName("table");
        scrollPaneConversion.setViewportView(tableConversion);
        tableConversion.setCellSelectionEnabled(true);
        tableConversion.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable table = (JTable) e.getComponent();
                    int col = table.columnAtPoint(e.getPoint());
                    int row = table.rowAtPoint(e.getPoint());
                    if (col > 1) {
                        textFieldValue.setText((String) table.getModel().getValueAt(row, col));
                    }
                }
            }
        });

        lblHelp.setText(resourceMap.getString("lblHelp.text"));
        lblHelp.setName("lblHelp");

        GroupLayout layoutConversion = new GroupLayout(pnlConversion);
        pnlConversion.setLayout(layoutConversion);
        layoutConversion.setHorizontalGroup(
                layoutConversion.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layoutConversion.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layoutConversion.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(scrollPaneConversion, GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
                                .addGroup(layoutConversion.createSequentialGroup()
                                        .addComponent(textFieldValue, GroupLayout.PREFERRED_SIZE, 201, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnConvert)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnClear))
                                .addComponent(lblHelp))
                        .addContainerGap())
        );
        layoutConversion.setVerticalGroup(
                layoutConversion.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layoutConversion.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblHelp)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layoutConversion.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                .addComponent(textFieldValue)
                                .addComponent(btnConvert, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnClear, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(scrollPaneConversion, GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                        .addContainerGap())
        );

        GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
                mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(pnlConversion, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(pnlCalendar, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
                mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlCalendar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlConversion, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setComponent(mainPanel);
    }

    private JComboBox calendarComboBox;
    private JButton btnClear;
    private JButton btnConvert;
    private JLabel lblCalendar;
    private JLabel lblOrigin;
    private JLabel lblHelp;
    private JPanel pnlCalendar;
    private JPanel pnlConversion;
    private JScrollPane scrollPaneConversion;
    private JPanel mainPanel;
    private JTable tableConversion;
    private JTextField textFieldOrigin;
    private JTextField textFieldValue;
    private JDialog aboutBox;
}
