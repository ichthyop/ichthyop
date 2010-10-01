package ichthyop.ui;

/** import AWT */
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

/** import java.text */
import java.text.NumberFormat;

/** import java.util */
import java.util.ArrayList;
import java.util.Locale;

/** import Swing */
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.JColorChooser;
import javax.swing.ListSelectionModel;

/** local import */
import ichthyop.core.Zone;
import ichthyop.ui.param.ValueChangedEvent;
import ichthyop.ui.param.ValueListener;
import ichthyop.util.Resources;
import ichthyop.util.Constant;
import ichthyop.util.INIFile;
import ichthyop.util.Structure;

/**
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */
public class ZoneEditor extends JPanel implements ActionListener, MouseListener,
        FocusListener, KeyListener {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * Specialization of the editor for release or recruitment zones.
     */
    private int type;
    /**
     * The array that stores the zones.
     */
    private ArrayList<Zone> arrayZones;
    /**
     * Label to display the title of the editor
     */
    private JLabel lblTitle;
    /**
     * Display the list of the zones
     */
    private JList list;
    /**
     * Panel containing the toolbar.
     */
    private JPanel pnlButton;
    /**
     * The scroller associated to the <code>list</code>.
     */
    private JScrollPane listScroller;
    /**
     * Array of text components to display longitude and latitude of the
     * demarcation points of the zone.
     */
    private JFormattedTextField[] txtGeogArea;
    /**
     * Text component to display the bathymetric line delimitating the zone.
     */
    private JFormattedTextField txtBathyMin, txtBathyMax;
    /**
     * Button to show the <code>JColorChooser</code>
     */
    private JButton btnColor;
    /**
     * Toolbar button.
     */
    private JButton btnNew, btnDelete, btnDuplicate;
    /**
     * Number format for longitude and latitude
     */
    NumberFormat nfLonLat;
    /**
     * Number format for depth
     */
    NumberFormat nfDepth;
    /**
     * Event listener list that stores <code>ValueChangedListener</code>
     */
    private EventListenerList listeners;

///////////////
// Constructors
///////////////

    /**
     * Constructs a zone editor for the specified type of zone
     * and the given zones.
     *
     * @param typeZone an int, characterizing the type of zone.
     * @param arrayZones the ArrayList that stores the zones.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the type of zone.
     */
    public ZoneEditor(int typeZone, ArrayList arrayZones) {

        super(new GridBagLayout());

        this.type = typeZone;
        this.arrayZones = arrayZones;

        nfLonLat = NumberFormat.getInstance(Locale.US);
        nfLonLat.setMinimumFractionDigits(1);
        nfLonLat.setMaximumFractionDigits(2);
        nfLonLat.setMaximumIntegerDigits(3);
        nfLonLat.setMinimumIntegerDigits(1);

        nfDepth = NumberFormat.getInstance(Locale.US);
        nfDepth.setParseIntegerOnly(true);
        nfDepth.setGroupingUsed(false);
        nfDepth.setMaximumIntegerDigits(5);
        nfDepth.setMinimumIntegerDigits(1);

        listeners = new EventListenerList();

        createUI();
    }

    /**
     * Constructs and empty zone editor, for the specified type of zone.
     * @param typeZone an int, characterizing the type of zone.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the type of zone.
     */
    public ZoneEditor(int typeZone) {
        this(typeZone, new ArrayList(0));
    }

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Creates the UI of the zone editor
     */
    private void createUI() {

        lblTitle = new JLabel();
        pnlButton = new JPanel(new GridLayout(1, 4, 5, 0));
        list = new JList();
        listScroller = new JScrollPane(list);

        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        lblTitle.setText(type == Constant.RELEASE
                         ? Resources.TITLE_RELEASE_ZONE_EDITOR
                         : Resources.TITLE_RECRUIT_ZONE_EDITOR);

        btnNew = new JButton(Resources.STR_NEW);
        btnDuplicate = new JButton(Resources.STR_DUPLICATE);
        btnDelete = new JButton(Resources.STR_DELETE);
        pnlButton.add(btnNew);
        pnlButton.add(btnDuplicate);
        pnlButton.add(btnDelete);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(5);
        list.setBackground(Color.WHITE);
        list.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        listScroller.setPreferredSize(new Dimension(60, 80));

        // Panel Geog Area
        JPanel pnlArea = new JPanel(new GridBagLayout());
        pnlArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        txtGeogArea = new JFormattedTextField[8];
        for (int i = 0; i < 8; i++) {
            txtGeogArea[i] = new JFormattedTextField(nfLonLat);
        }

        pnlArea.add(new JLabel(Resources.LBL_GEOG_AREA),
                    new GridBagConstraints(0, 0, 3, 1, 90, 10,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(2, 5, 2, 5), 0, 0));

        pnlArea.add(new JLabel(Resources.LBL_LON),
                    new GridBagConstraints(1, 1, 1, 1, 30, 10,
                                           GridBagConstraints.NORTH,
                                           GridBagConstraints.NONE,
                                           new Insets(2, 5, 2, 5), 0, 0));
        pnlArea.add(new JLabel(Resources.LBL_LAT),
                    new GridBagConstraints(2, 1, 1, 1, 30, 10,
                                           GridBagConstraints.NORTH,
                                           GridBagConstraints.NONE,
                                           new Insets(2, 5, 2, 5), 0, 0));
        pnlArea.add(new JLabel(Resources.LBL_P1),
                    new GridBagConstraints(0, 2, 1, 1, 30, 10,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(2, 5, 2, 5), 0, 0));
        pnlArea.add(new JLabel(Resources.LBL_P2),
                    new GridBagConstraints(0, 3, 1, 1, 30, 10,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(2, 5, 2, 5), 0, 0));
        pnlArea.add(new JLabel(Resources.LBL_P3),
                    new GridBagConstraints(0, 4, 1, 1, 30, 10,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(2, 5, 2, 5), 0, 0));
        pnlArea.add(new JLabel(Resources.LBL_P4),
                    new GridBagConstraints(0, 5, 1, 1, 30, 10,
                                           GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(2, 5, 2, 5), 0, 0));
        for (int i = 0; i < 8; i++) {
            pnlArea.add(txtGeogArea[i], new GridBagConstraints((i % 2 + 1),
                    ((int) (i / 2) + 2), 1, 1, 30, 10,
                    GridBagConstraints.CENTER,
                    GridBagConstraints.HORIZONTAL,
                    new Insets(2, 5, 2, 5), 0, 0));

        }

        // Panel Bathy
        JPanel pnlBathy = new JPanel(new GridBagLayout());
        pnlBathy.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        txtBathyMin = new JFormattedTextField(nfDepth);
        txtBathyMax = new JFormattedTextField(nfDepth);
        txtBathyMin.setPreferredSize(new Dimension(50, 20));
        txtBathyMax.setPreferredSize(new Dimension(50, 20));

        pnlBathy.add(new JLabel(Resources.LBL_BATHYMETRIC_MASK),
                     new GridBagConstraints(0, 0, 3, 1, 50, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 5, 5, 5), 0, 0));
        pnlBathy.add(new JLabel(Resources.LBL_BATHY_LINE_MIN),
                     new GridBagConstraints(0, 1, 1, 1, 20, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 30, 5, 5), 0, 0));
        pnlBathy.add(new JLabel(Resources.LBL_BATHY_LINE_MAX),
                     new GridBagConstraints(0, 2, 1, 1, 20, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 30, 5, 5), 0, 0));
        pnlBathy.add(txtBathyMin,
                     new GridBagConstraints(1, 1, 1, 1, 15, 10,
                                            GridBagConstraints.CENTER,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(2, 5, 2, 5), 0, 0));
        pnlBathy.add(txtBathyMax,
                     new GridBagConstraints(1, 2, 1, 1, 15, 10,
                                            GridBagConstraints.CENTER,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(2, 5, 2, 5), 0, 0));
        pnlBathy.add(new JLabel(Resources.UNIT_METER),
                     new GridBagConstraints(2, 1, 1, 1, 15, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 5, 2, 5), 0, 0));
        pnlBathy.add(new JLabel(Resources.UNIT_METER),
                     new GridBagConstraints(2, 2, 1, 1, 15, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 5, 2, 5), 0, 0));

        //Panel Color
        JPanel pnlColor = new JPanel(new GridBagLayout());
        pnlColor.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        btnColor = new JButton("Click");
        btnColor.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        pnlColor.add(new JLabel(Resources.LBL_COLOR_ZONE),
                     new GridBagConstraints(0, 0, 1, 1, 50, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 5, 2, 5), 0, 0));
        pnlColor.add(btnColor,
                     new GridBagConstraints(1, 0, 1, 1, 50, 10,
                                            GridBagConstraints.CENTER,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(2, 5, 2, 5), 0, 0));

        // Add listeners
        btnNew.addActionListener(this);
        btnDelete.addActionListener(this);
        btnDuplicate.addActionListener(this);
        list.addMouseListener(this);
        btnColor.addActionListener(this);
        for (int i = 0; i < 8; i++) {
            txtGeogArea[i].addFocusListener(this);
            txtGeogArea[i].addKeyListener(this);
        }
        txtBathyMin.addFocusListener(this);
        txtBathyMax.addFocusListener(this);
        txtBathyMin.addKeyListener(this);
        txtBathyMax.addKeyListener(this);

        //Set components within zone editor
        this.add(lblTitle, new GridBagConstraints(0, 0, 1, 1, 10, 10,
                                                  GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE,
                                                  new Insets(5, 5, 5, 5), 15, 0));
        this.add(pnlButton, new GridBagConstraints(1, 0, 2, 1, 90, 10,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5), 15, 0));
        this.add(listScroller, new GridBagConstraints(0, 1, 1, 2, 10, 80,
                GridBagConstraints.EAST,
                GridBagConstraints.VERTICAL,
                new Insets(5, 5, 5, 5), 15, 0));
        this.add(pnlArea, new GridBagConstraints(1, 1, 1, 2, 40, 80,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(5, 5, 5, 5), 15, 0));
        this.add(pnlBathy, new GridBagConstraints(2, 1, 1, 1, 50, 40,
                                                  GridBagConstraints.WEST,
                                                  GridBagConstraints.BOTH,
                                                  new Insets(5, 5, 5, 5), 15, 0));
        this.add(pnlColor, new GridBagConstraints(2, 2, 1, 1, 50, 40,
                                                  GridBagConstraints.WEST,
                                                  GridBagConstraints.BOTH,
                                                  new Insets(5, 5, 5, 5), 15, 0));

    }

    /**
     * Reads the definition of the zone in the specified configuration file and
     * displays them in the editor.
     *
     * @param file an INIFile, the configuration file where the zone definitions
     * are read.
     * @see #set
     */
    public void read(INIFile file) {

        double[] lon = new double[4];
        double[] lat = new double[4];
        int bathyMin, bathyMax;
        Color color;
        int colorR, colorG, colorB;
        int nbZones = 0;

        String prefix = (type == Constant.RELEASE)
                        ? Structure.SECTION_RELEASE_ZONE
                        : Structure.SECTION_RECRUITMENT_ZONE;

        String[] sections = file.getAllSectionNames();
        for (String section : sections) {
            if (section.toLowerCase().startsWith(prefix.toLowerCase())) {
                nbZones++;
            }
        }

        for (int i = 0; i < nbZones; i++) {
            String section = prefix + String.valueOf(i + 1);
            for (int j = 0; j < 4; j++) {
                lon[j] = file.getDoubleProperty(section,
                                                Structure.LON_ZONE +
                                                String.valueOf(j + 1));
                lat[j] = file.getDoubleProperty(section,
                                                Structure.LAT_ZONE +
                                                String.valueOf(j + 1));
            }
            bathyMin = file.getIntegerProperty(section,
                                               Structure.BATHY_MIN);
            bathyMax = file.getIntegerProperty(section,
                                               Structure.BATHY_MAX);
            colorR = file.getIntegerProperty(section,
                                             Structure.RED);
            colorG = file.getIntegerProperty(section,
                                             Structure.GREEN);
            colorB = file.getIntegerProperty(section,
                                             Structure.BLUE);
            color = new Color(colorR, colorG, colorB);

            set(new Zone(type,
                         i,
                         lon[0], lat[0],
                         lon[1], lat[1],
                         lon[2], lat[2],
                         lon[3], lat[3],
                         bathyMin, bathyMax,
                         color));
        }
        file = null;
    }

    /**
     * Writes the zone definitions in the specified configuration file and
     * under the specified section.
     *
     * @param file an INIFile, the configuration file for recording the zones.
     * @param prefix a String, the prefix of the section name.
     * @param comment a String the section comment.
     */
    public void write(INIFile file, String prefix, String comment) {

        for (int i = 0; i < arrayZones.size(); i++) {
            Zone zone = arrayZones.get(i);
            String section = prefix + String.valueOf(zone.getIndex() + 1);
            file.addSection(section, comment);
            for (int j = 0; j < 4; j++) {
                file.setDoubleProperty(section,
                                       Structure.LON_ZONE +
                                       String.valueOf(j + 1),
                                       zone.getLon(j),
                                       null);
                file.setDoubleProperty(section,
                                       Structure.LAT_ZONE +
                                       String.valueOf(j + 1),
                                       zone.getLat(j),
                                       null);
            }
            file.setIntegerProperty(section,
                                    Structure.BATHY_MIN,
                                    zone.getBathyMin(),
                                    null);
            file.setIntegerProperty(section,
                                    Structure.BATHY_MAX,
                                    zone.getBathyMax(),
                                    null);
            file.setIntegerProperty(section,
                                    Structure.RED,
                                    zone.getColor().getRed(),
                                    null);
            file.setIntegerProperty(section,
                                    Structure.GREEN,
                                    zone.getColor().getGreen(),
                                    null);
            file.setIntegerProperty(section,
                                    Structure.BLUE,
                                    zone.getColor().getBlue(),
                                    null);
        }
    }

    /**
     * Replaces the zone in the editor at position <code>zone.getIndex()</code>
     * with the specified element and updates the display.
     * If <code>zone.getIndex()</code> is superior to the editor's size, the
     * method just appends the specified zone at the end.
     *
     * @param zone the Zone to add.
     */
    public void set(Zone zone) {
        if (zone.getIndex() == arrayZones.size()) {
            arrayZones.add(zone);
        } else {
            arrayZones.set(zone.getIndex(), zone);
        }
        update();
    }

    /**
     * Reindexes the zones when one has been deleted.
     */
    private void reindex() {

        for (int i = 0; i < arrayZones.size(); i++) {
            Zone zone = arrayZones.get(i);
            zone.setIndex(i);
        }
    }

    /**
     * Updates the display of the editor.
     */
    private void update() {

        list.removeAll();
        String titleZone = Resources.STR_ZONE;
        int nbZones = arrayZones.size();
        int lastZone = nbZones - 1;
        String[] strZone = new String[nbZones];
        for (int i = nbZones; i-- > 0; ) {
            strZone[i] = new StringBuffer(titleZone).append(" ").
                         append(i + 1).toString();
        }
        list.setListData(strZone);
        list.setSelectedIndex(lastZone);
        list.ensureIndexIsVisible(lastZone);
        try {
            display(arrayZones.get(lastZone));
        } catch (java.lang.ArrayIndexOutOfBoundsException ex) {}

    }

    /**
     * Displays the specified zone in the editor.
     *
     * @param zone the Zone to display.
     */
    private void display(Zone zone) {

        for (int i = 0; i < 8; i++) {
            txtGeogArea[i].setValue((i % 2 > 0)
                                    ? zone.getLat(i / 2)
                                    : zone.getLon(i / 2));
        }
        txtBathyMin.setValue(zone.getBathyMin());
        txtBathyMax.setValue(zone.getBathyMax());
        btnColor.setBackground(zone.getColor());
    }

    /**
     * Duplicates the specified zone and appends the duplicated zone at the end
     * of the list.
     *
     * @param zone the Zone to duplicate.
     * @return Zone the duplicated zone, with new index.
     */
    private Zone duplicate(Zone zone) {
        return new Zone(zone.getType(), arrayZones.size(), zone.getLon(0),
                        zone.getLat(0), zone.getLon(1), zone.getLat(1),
                        zone.getLon(2),
                        zone.getLat(2), zone.getLon(3), zone.getLat(3),
                        zone.getBathyMin(),
                        zone.getBathyMax(), zone.getColor());
    }

    /**
     * Gets the length of the editor.
     * @return the number of zones contained in the editor.
     */
    public int length() {
        return arrayZones.size();
    }

    /**
     * Sets whether or not this editor is enabled.
     * @param enabled true if this component should be enabled, false otherwise
     */
    @Override
            public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        JPanel panel;
        Component component;
        for (int i = this.getComponentCount(); i-- > 0; ) {
            component = this.getComponent(i);
            if (component.getClass() == JPanel.class) {
                panel = (JPanel) component;
                for (int j = panel.getComponentCount(); j-- > 0; ) {
                    panel.getComponent(j).setEnabled(enabled);
                }
            }
            component.setEnabled(enabled);
        }
        list.setEnabled(enabled);
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {

        Object source = e.getSource();

        /** Btn New */
        if (source == btnNew) {
            set(new Zone(type, arrayZones.size()));
        }

        /** Btn Duplicate */
        if (source == btnDuplicate) {
            if (!list.isSelectionEmpty()) {
                set(duplicate(arrayZones.get(list.getSelectedIndex())));
            }
        }

        /** Btn Delete */
        if (source == btnDelete) {
            if (!list.isSelectionEmpty()) {
                arrayZones.remove(list.getSelectedIndex());
            }
            reindex();
            update();
        }

        /** Btn Color */
        if (source == btnColor) {
            btnColor.setBackground(JColorChooser.showDialog(ZoneEditor.this,
                    Resources.TITLE_COLOR_CHOOSER, btnColor.getBackground()));
            if (!list.isSelectionEmpty()) {
                Zone zone = arrayZones.get(list.getSelectedIndex());
                zone.setColor(btnColor.getBackground());
                arrayZones.set(list.getSelectedIndex(), zone);
            } else {
                return;
            }
        }

        /** Inform the container that a change has occured */
        fireValueChanged();

    }

    /**
     * Displays the zone selected by a click in the <code>list</code>
     */
    public void mouseClicked(MouseEvent e) {

        Object src = e.getSource();
        if (src == list) {
            try {
                display(arrayZones.get(list.getSelectedIndex()));
            } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                return;
            }

        }
    }

    /**
     * Displays the latitude of the specified demarcation point in its
     * corresponding text component.
     *
     * @param zone the selected Zone
     * @param the index of the demarcation point.
     */
    private void setLatitude(Zone zone, int index) {

        int indexTxt = 2 * index + 1;
        float lat = ((Number) txtGeogArea[indexTxt].getValue()).floatValue();
        if (Math.abs(lat) > 90.f) {
            txtGeogArea[indexTxt].setValue(zone.getLat(index));
        } else if (lat != zone.getLat(index)) {
            zone.setLat(index, lat);
        }
    }

    /**
     * Displays the longitude of the specified demarcation point in its
     * corresponding text component.
     *
     * @param zone the selected Zone
     * @param the index of the demarcation point.
     */
    private void setLongitude(Zone zone, int index) {

        int indexTxt = 2 * index;
        float lon = ((Number) txtGeogArea[indexTxt].getValue()).floatValue();
        if (lon >= 360.f) {
            txtGeogArea[indexTxt].setValue(zone.getLon(index));
        } else if (lon != zone.getLon(index)) {
            zone.setLon(index, lon);
        }
    }

    /**
     * The list requires the focus when the mouse enters.
     */
    public void mouseEntered(MouseEvent e) {
        if (e.getSource() == list) {
            list.requestFocusInWindow();
        }
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Invoked when a text component gains the keyboard focus.
     * Selects all the text in the text component.
     */
    public void focusGained(FocusEvent e) {

        Object src = e.getSource();
        final JFormattedTextField txtField;
        if (src.getClass() == JFormattedTextField.class) {
            txtField = (JFormattedTextField) src;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    txtField.selectAll();
                }
            });
        }
    }

    /**
     * Invoked when a text component looses the keyboard focus.
     * Updates the zone definition if the component has changed.
     */
    public void focusLost(FocusEvent e) {

        if (!list.isSelectionEmpty()) {
            final Zone zone = arrayZones.get(list.getSelectedIndex());
            Object source = e.getSource();

            if (source == txtGeogArea[0]) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setLongitude(zone, 0);
                    }
                });
            }
            if (source == txtGeogArea[2]) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setLongitude(zone, 1);
                    }
                });
            }
            if (source == txtGeogArea[4]) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setLongitude(zone, 2);
                    }
                });
            }
            if (source == txtGeogArea[6]) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setLongitude(zone, 3);
                    }
                });
            }
            if (source == txtGeogArea[1]) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setLatitude(zone, 0);
                    }
                });
            }
            if (source == txtGeogArea[3]) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setLatitude(zone, 1);
                    }
                });
            }
            if (source == txtGeogArea[5]) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setLatitude(zone, 2);
                    }
                });
            }
            if (source == txtGeogArea[7]) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setLatitude(zone, 3);
                    }
                });
            }
            if (source == txtBathyMin) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        int bathy = ((Number) txtBathyMin.getValue()).intValue();
                        if (bathy != zone.getBathyMin()) {
                            zone.setBathyMin(bathy);
                        }
                    }
                });
            }
            if (source == txtBathyMax) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        int bathy = ((Number) txtBathyMax.getValue()).intValue();
                        if (bathy != zone.getBathyMax()) {
                            zone.setBathyMax(bathy);
                        }
                    }
                });
            }
        }
    }

    /**
     * Adds a ValueListener to the editor.
     * @param listener the <code>ValueListener</code> to add.
     */
    public void addValueListener(ValueListener listener) {
        listeners.add(ValueListener.class, listener);
    }

    /**
     * Removes a ValueListener from the editor.
     * @param listener the <code>ValueListener</code> to remove.
     */

    public void removeValueListener(ValueListener listener) {
        listeners.remove(ValueListener.class, listener);
    }

    /**
     * Reports to all the value listeners the value of the parameter has
     * changed, throwing a new ValueChanged event.
     *
     * @see ichthyop.util.param.ValueChangedEvent
     * @see ichthyop.util.param.ValueListener
     */
    public void fireValueChanged() {

        ValueListener[] listenerList = (ValueListener[]) listeners.getListeners(
                ValueListener.class);

        for (ValueListener listener : listenerList) {
            listener.valueChanged(new ValueChangedEvent(this));
        }
    }

    /**
     * Invoked when a key has been typed. Calls the
     * <code>fireValueChanged</code> method.
     */
    public void keyTyped(KeyEvent e) {

        fireValueChanged();
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    //---------- End of class
}
