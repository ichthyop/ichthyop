package ichthyop.config;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.util.Locale;
import java.text.*;
import ichthyop.*;
import ichthyop.util.*;

public class ZoneEditor extends JPanel implements ActionListener, MouseListener {

    private int typeZone;
    private ArrayList arrayZones;

    private JLabel lblZone = new JLabel();
    private JList listZones = new JList();
    private JToolBar tlbZone = new JToolBar(JToolBar.HORIZONTAL);
    private JScrollPane listScroller = new JScrollPane(listZones);
    private boolean HAS_CHANGED;

    NumberFormat nfLonLat, nfColor, nfDepth;

    //--------------------------------------------------------------------------
    public ZoneEditor(int typeZone, ArrayList arrayZones) {

        super(new GridBagLayout());
        lblZone.setText(typeZone == Resources.RELEASE
                        ? Resources.STR_RELEASE_ZONE
                        : Resources.STR_RECRUITMENT_ZONE);
        this.typeZone = typeZone;
        tlbZone.add(new JButton(Resources.STR_NEW));
        tlbZone.add(new JButton(Resources.STR_DUPLICATE));
        tlbZone.add(new JButton(Resources.STR_EDIT));
        tlbZone.add(new JButton(Resources.STR_DELETE));

        this.arrayZones = arrayZones;

        nfLonLat = NumberFormat.getInstance();
        nfLonLat.setMinimumFractionDigits(1);
        nfLonLat.setMaximumFractionDigits(2);
        nfLonLat.setMaximumIntegerDigits(3);
        nfLonLat.setMinimumIntegerDigits(1);

        nfDepth = NumberFormat.getInstance();
        nfDepth.setMaximumIntegerDigits(4);
        nfDepth.setMinimumIntegerDigits(1);
        nfDepth.setMaximumFractionDigits(1);

        nfColor = NumberFormat.getInstance();
        nfColor.setParseIntegerOnly(true);
        nfColor.setMaximumIntegerDigits(3);

        HAS_CHANGED = false;

        buildMainPanel();
    }

    //--------------------------------------------------------------------------
    public ZoneEditor(int typeZone) {
        this(typeZone, new ArrayList(0));
    }

    //--------------------------------------------------------------------------
    private void buildMainPanel() {

        this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        listZones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listZones.setLayoutOrientation(JList.VERTICAL);
        listZones.setVisibleRowCount(5);
        listZones.setBackground(Color.WHITE);
        listZones.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        listScroller.setPreferredSize(new Dimension(250, 80));
        tlbZone.setFloatable(false);
        for (int i = 0; i < tlbZone.getComponentCount(); i++) {
            JButton btn = (JButton) tlbZone.getComponent(i);
            btn.addActionListener(this);
        }

        listZones.addMouseListener(this);

        this.add(lblZone, new GridBagConstraints(0, 0, 1, 1, 100, 10,
                                                 GridBagConstraints.CENTER,
                                                 GridBagConstraints.NONE,
                                                 new Insets(5, 5, 5, 5), 15, 0));
        this.add(listScroller, new GridBagConstraints(0, 1, 1, 1, 90, 80,
                GridBagConstraints.WEST,
                GridBagConstraints.BOTH,
                new Insets(5, 25, 5, 25), 15,
                0));
        this.add(tlbZone, new GridBagConstraints(0, 2, 1, 1, 100, 10,
                                                 GridBagConstraints.CENTER,
                                                 GridBagConstraints.NONE,
                                                 new Insets(5, 5, 5, 5), 15, 0));

    }

    //--------------------------------------------------------------------------
    public void setZone(Zone zone) {
        if (zone.getIndexZone() > arrayZones.size()) {
            arrayZones.add(zone);
        } else {
            arrayZones.set(zone.getIndexZone() - 1, zone);
        }
        updateDisplay();
    }

    //--------------------------------------------------------------------------
    private void updateDisplay() {

        listZones.removeAll();
        String titleZone = Resources.TITLE_RELEASE_ZONE_EDITOR;
        String[] strZone = new String[arrayZones.size()];
        if (typeZone == Resources.RECRUITMENT) {
            titleZone = Resources.TITLE_RECRUIT_ZONE_EDITOR;
        }
        for (int i = 0; i < arrayZones.size(); i++) {
            Zone zone = (Zone) arrayZones.get(i);
            zone.setIndexZone(i + 1);
            //arrayZones.set(i, zone);
            //nfLonLat.format(zone.getLon(0));
            strZone[i] = titleZone + " " + (i + 1) + " - " + "P1("
                         + nfLonLat.format(zone.getLon(0)) + "; " +
                         nfLonLat.format(zone.getLat(0)) + ") P2("
                         + nfLonLat.format(zone.getLon(1)) + "; " +
                         nfLonLat.format(zone.getLat(1)) + ") P3("
                         + nfLonLat.format(zone.getLon(2)) + "; " +
                         nfLonLat.format(zone.getLat(2)) + ") P4("
                         + nfLonLat.format(zone.getLon(3)) + "; " +
                         nfLonLat.format(zone.getLat(3)) + ") - D1("
                         + nfDepth.format(zone.getBathyMin()) + ") D2(" +
                         nfDepth.format(zone.getBathyMax())
                         + ") - Color(" + zone.getColorZone().getRed() + "; "
                         + zone.getColorZone().getGreen() + "; "
                         + zone.getColorZone().getBlue() + ")";
        }
        listZones.setListData(strZone);
    }

    //--------------------------------------------------------------------------
    public ArrayList getArrayZones() {
        return arrayZones;
    }

    //--------------------------------------------------------------------------
    public boolean hasChanged() {
      return HAS_CHANGED;
    }

    //--------------------------------------------------------------------------
    public void setHasChanged(boolean bln) {
      HAS_CHANGED = bln;
    }

    //--------------------------------------------------------------------------
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        lblZone.setEnabled(enabled);
        listZones.setEnabled(enabled);
        for (int i = 0; i < tlbZone.getComponentCount(); i++) {
            tlbZone.getComponent(i).setEnabled(enabled);
        }

    }

    //--------------------------------------------------------------------------
    public void actionPerformed(ActionEvent e) {

        Object src = e.getSource();

        //Btn New
        if (src == tlbZone.getComponentAtIndex(0)) {
          HAS_CHANGED = true;
            new JFrameZone(new Zone(typeZone, arrayZones.size() + 1));
        }
        //Btn Duplicate
        if (src == tlbZone.getComponentAtIndex(1)) {
            if (!listZones.isSelectionEmpty()) {
                Zone zonetmp = (Zone) arrayZones.get(listZones.
                        getSelectedIndex());
                zonetmp.setIndexZone(arrayZones.size() + 1);
                new JFrameZone(zonetmp);
                HAS_CHANGED = true;
            }
        }
        //Btn Edit
        if (src == tlbZone.getComponentAtIndex(2)) {
            if (!listZones.isSelectionEmpty()) {
                new JFrameZone((Zone) arrayZones.get(listZones.
                        getSelectedIndex()));
                    HAS_CHANGED = true;
            }
        }
        //Btn Delete
        if (src == tlbZone.getComponentAtIndex(3)) {
            if (!listZones.isSelectionEmpty()) {
                arrayZones.remove(listZones.getSelectedIndex());
                HAS_CHANGED = true;
            }
            updateDisplay();
        }
    }

    //----------------------------------------------------------------------------
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            if (!listZones.isSelectionEmpty()) {
                new JFrameZone((Zone) arrayZones.get(listZones.
                        getSelectedIndex()));
                    HAS_CHANGED = true;
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    //##########################################################################
    private class JFrameZone extends JFrame implements ActionListener,
            FocusListener {

        private Zone zone;
        private JRadioButton rdBtnSzone = new JRadioButton(Resources.STR_RELEASE_ZONE, true);
        private JRadioButton rdBtnRzone = new JRadioButton(Resources.STR_RECRUITMENT_ZONE, false);
        private JFormattedTextField[] txtGeogArea;
        private JFormattedTextField txtBathyMin, txtBathyMax;
        private JFormattedTextField txtRed, txtGreen, txtBlue;
        private JButton btnOK, btnCANCEL;
        private JPanel pnlType;
        private JPanel lblColor;
        private int typeOfZone, indexZone;

        //--------------------------------------------------------------------------
        JFrameZone(Zone zone) {

            this.typeOfZone = zone.getTypeZone();
            this.indexZone = zone.getIndexZone();
            this.zone = zone;
            enableEvents(AWTEvent.WINDOW_EVENT_MASK);
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.setResizable(false);
            this.validate();
            this.setVisible(true);
            buildFrame();
            setTypeOfZone(typeOfZone);
            this.setVisible(true);

        }

        //--------------------------------------------------------------------------
        private void buildFrame() {

            this.getContentPane().setLayout(new GridBagLayout());
            this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            this.setSize((int) (0.5f * screenSize.width),
                         (int) (0.5f * screenSize.height));
            this.setTitle(Resources.NAME_SHORT + Resources.TITLE_FRAME_ZONE);
            this.setLocation((int) (0.3f * screenSize.width),
                             (int) (0.3f * screenSize.height));

            String nameZone = Resources.STR_RELEASE_ZONE;
            if (typeOfZone == Resources.RECRUITMENT) {
                nameZone = Resources.STR_RECRUITMENT_ZONE;
            }
            JLabel lblFrameZone = new JLabel(nameZone + " " + indexZone);
            lblFrameZone.setBorder(BorderFactory.createEtchedBorder(
                    EtchedBorder.
                    RAISED));
            lblFrameZone.setHorizontalAlignment(JLabel.CENTER);
            lblFrameZone.setFont(new java.awt.Font(this.getFont().getFontName(),
                    java.awt.Font.BOLD, 24));

            //Panel Type
            pnlType = new JPanel(new GridBagLayout());
            pnlType.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
                    RAISED));
            ButtonGroup grp = new ButtonGroup();
            grp.add(rdBtnSzone);
            grp.add(rdBtnRzone);
            pnlType.add(new JLabel(Resources.LBL_TYPE_ZONE),
                        new GridBagConstraints(0, 0, 1, 1, 100, 40,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
            pnlType.add(rdBtnSzone,
                        new GridBagConstraints(0, 1, 1, 1, 50, 60,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 30, 5, 5), 0, 0));
            pnlType.add(rdBtnRzone,
                        new GridBagConstraints(1, 1, 1, 1, 50, 60,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));

            // Panel Geog Area
            JPanel pnlArea = new JPanel(new GridBagLayout());
            pnlArea.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
                    RAISED));

            txtGeogArea = new JFormattedTextField[8];
            for (int i = 0; i < 8; i++) {
                txtGeogArea[i] = new JFormattedTextField(nfLonLat);
                txtGeogArea[i].setValue(new Float(0.f));
                txtGeogArea[i].addFocusListener(this);
                txtGeogArea[i].setValue((i % 2 > 0)
                                        ? new Float(zone.getLat(i / 2))
                                        : new Float(zone.getLon(i / 2)));
            }
            /*txtGeogArea[0].setValue(new Float(zone.getLonP1()));
            txtGeogArea[2].setValue(new Float(zone.getLonP2()));
            txtGeogArea[4].setValue(new Float(zone.getLonP3()));
            txtGeogArea[6].setValue(new Float(zone.getLonP4()));
            txtGeogArea[1].setValue(new Float(zone.getLatP1()));
            txtGeogArea[3].setValue(new Float(zone.getLatP2()));
            txtGeogArea[5].setValue(new Float(zone.getLatP3()));
            txtGeogArea[7].setValue(new Float(zone.getLatP4()));*/

            pnlArea.add(new JLabel(Resources.LBL_GEOG_AREA),
                        new GridBagConstraints(0, 0, 3, 1, 90, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));

            pnlArea.add(new JLabel(Resources.LBL_LON),
                        new GridBagConstraints(1, 1, 1, 1, 30, 10,
                                               GridBagConstraints.NORTH,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
            pnlArea.add(new JLabel(Resources.LBL_LAT),
                        new GridBagConstraints(2, 1, 1, 1, 30, 10,
                                               GridBagConstraints.NORTH,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
            pnlArea.add(new JLabel(Resources.LBL_P1),
                        new GridBagConstraints(0, 2, 1, 1, 30, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
            pnlArea.add(new JLabel(Resources.LBL_P2),
                        new GridBagConstraints(0, 3, 1, 1, 30, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
            pnlArea.add(new JLabel(Resources.LBL_P3),
                        new GridBagConstraints(0, 4, 1, 1, 30, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
            pnlArea.add(new JLabel(Resources.LBL_P4),
                        new GridBagConstraints(0, 5, 1, 1, 30, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(5, 5, 5, 5), 0, 0));
            for (int i = 0; i < 8; i++) {
                pnlArea.add(txtGeogArea[i], new GridBagConstraints((i % 2 + 1),
                        ((int) (i / 2) + 2), 1, 1, 30, 10,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(5, 5, 5, 5), 0, 0));

            }

            // Panel Bathy
            JPanel pnlBathy = new JPanel(new GridBagLayout());
            pnlBathy.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
                    RAISED));

            txtBathyMin = new JFormattedTextField(nfDepth);
            txtBathyMax = new JFormattedTextField(nfDepth);
            txtBathyMin.setValue(new Float(zone.getBathyMin()));
            txtBathyMax.setValue(new Float(zone.getBathyMax()));

            pnlBathy.add(new JLabel(Resources.LBL_BATHYMETRIC_MASK),
                         new GridBagConstraints(0, 0, 3, 1, 50, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 5, 5, 5), 0, 0));
            pnlBathy.add(new JLabel(Resources.LBL_BATHY_LINE_MIN),
                         new GridBagConstraints(0, 1, 1, 1, 0, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 30, 5, 5), 0, 0));
            pnlBathy.add(new JLabel(Resources.LBL_BATHY_LINE_MAX),
                         new GridBagConstraints(0, 2, 1, 1, 0, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 30, 5, 5), 0, 0));
            pnlBathy.add(txtBathyMin,
                         new GridBagConstraints(1, 1, 1, 1, 5, 10,
                                                GridBagConstraints.CENTER,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5, 5, 5, 5), 0, 0));
            pnlBathy.add(txtBathyMax,
                         new GridBagConstraints(1, 2, 1, 1, 5, 10,
                                                GridBagConstraints.CENTER,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5, 5, 5, 5), 0, 0));
            pnlBathy.add(new JLabel(Resources.UNIT_METER),
                         new GridBagConstraints(2, 1, 1, 1, 5, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 5, 5, 5), 0, 0));
            pnlBathy.add(new JLabel(Resources.UNIT_METER),
                         new GridBagConstraints(2, 2, 1, 1, 5, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 5, 5, 5), 0, 0));

            //Panel Color
            JPanel pnlColor = new JPanel(new GridBagLayout());
            pnlColor.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
                    RAISED));

            txtRed = new JFormattedTextField(nfColor);
            txtGreen = new JFormattedTextField(nfColor);
            txtBlue = new JFormattedTextField(nfColor);
            txtRed.setValue(new Integer(zone.getColorZone().getRed()));
            txtGreen.setValue(new Integer(zone.getColorZone().getGreen()));
            txtBlue.setValue(new Integer(zone.getColorZone().getBlue()));
            txtRed.addFocusListener(this);
            txtGreen.addFocusListener(this);
            txtBlue.addFocusListener(this);
            lblColor = new JPanel();
            lblColor.setBackground(zone.getColorZone());

            Color[] listColor = {
                                Color.BLACK, Color.BLUE, Color.CYAN,
                                Color.DARK_GRAY,
                                Color.GRAY, Color.GREEN, Color.LIGHT_GRAY,
                                Color.MAGENTA,
                                Color.ORANGE, Color.PINK, Color.RED,
                                Color.WHITE,
                                Color.YELLOW};
            JToolBar tlbColor = new JToolBar(JToolBar.HORIZONTAL);
            tlbColor.setFloatable(false);
            for (int i = 0; i < listColor.length; i++) {
                final JButton btn = new JButton();
                btn.setBackground(listColor[i]);
                tlbColor.add(btn);
                btn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Color clr = btn.getBackground();
                        txtRed.setValue(new Integer(clr.getRed()));
                        txtGreen.setValue(new Integer(clr.getGreen()));
                        txtBlue.setValue(new Integer(clr.getBlue()));
                        lblColor.setBackground(clr);
                    }
                });
            }

            pnlColor.add(new JLabel(Resources.LBL_COLOR_ZONE),
                         new GridBagConstraints(0, 0, 5, 1, 40, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 5, 5, 5), 0, 0));
            pnlColor.add(lblColor,
                         new GridBagConstraints(5, 0, 1, 1, 10, 10,
                                                GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(5, 5, 5, 5), 0, 0));

            pnlColor.add(new JLabel(Resources.LBL_RED),
                         new GridBagConstraints(0, 1, 1, 1, 10, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 30, 5, 5), 0, 0));
            pnlColor.add(new JLabel(Resources.LBL_GREEN),
                         new GridBagConstraints(2, 1, 1, 1, 10, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 5, 5, 5), 0, 0));
            pnlColor.add(new JLabel(Resources.LBL_BLUE),
                         new GridBagConstraints(4, 1, 1, 1, 10, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 5, 5, 5), 0, 0));
            pnlColor.add(txtRed,
                         new GridBagConstraints(1, 1, 1, 1, 10, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5, 5, 5, 5), 0, 0));
            pnlColor.add(txtGreen,
                         new GridBagConstraints(3, 1, 1, 1, 10, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5, 5, 5, 5), 0, 0));
            pnlColor.add(txtBlue,
                         new GridBagConstraints(5, 1, 1, 1, 10, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5, 5, 5, 5), 0, 0));
            pnlColor.add(tlbColor,
                         new GridBagConstraints(0, 2, 6, 1, 60, 10,
                                                GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 20, 5, 5), 0, 0));

            //Navigation Buttons
            btnOK = new JButton(Resources.BTN_OK);
            btnCANCEL = new JButton(Resources.BTN_CANCEL);
            btnOK.addActionListener(this);
            btnCANCEL.addActionListener(this);
            JPanel pnlBtn = new JPanel();
            pnlBtn.add(btnOK);
            pnlBtn.add(btnCANCEL);

            //Main Frame
            this.getContentPane().add(lblFrameZone,
                                      new GridBagConstraints(0, 0, 1, 1, 35, 20,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.getContentPane().add(pnlType,
                                      new GridBagConstraints(1, 0, 1, 1, 65, 20,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.getContentPane().add(pnlArea,
                                      new GridBagConstraints(0, 1, 1, 2, 35, 80,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.getContentPane().add(pnlBathy,
                                      new GridBagConstraints(1, 1, 1, 1, 65, 40,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.getContentPane().add(pnlColor,
                                      new GridBagConstraints(1, 2, 1, 1, 65, 40,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.getContentPane().add(pnlBtn,
                                      new GridBagConstraints(1, 3, 1, 1, 0, 10,
                    GridBagConstraints.EAST,
                    GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 0), 0, 0));

        }

        //--------------------------------------------------------------------------
        private void setTypeOfZone(int typeOfZone) {
            switch (typeOfZone) {
            case Resources.RELEASE:
                rdBtnSzone.setSelected(true);
                rdBtnSzone.setEnabled(false);
                rdBtnRzone.setEnabled(false);
                break;
            case Resources.RECRUITMENT:
                rdBtnRzone.setSelected(true);
                rdBtnSzone.setEnabled(false);
                rdBtnRzone.setEnabled(false);
                break;
            }

        }

        //--------------------------------------------------------------------------
        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == btnCANCEL) {
                this.setVisible(false);
                this.dispose();
            }

            if (e.getSource() == btnOK) {
                if (isDataValid()) {
                    createZone();
                }

            }
        }

        //--------------------------------------------------------------------------
        private void createZone() {

            float[] coord = new float[8];
            float depthMin = 0, depthMax = 10;
            for (int i = 0; i < 8; i++) {
                try {
                    coord[i] = nfLonLat.parse(txtGeogArea[i].getText()).
                               floatValue();
                    depthMin = nfDepth.parse(txtBathyMin.getText()).floatValue();
                    depthMax = nfDepth.parse(txtBathyMax.getText()).floatValue();
                } catch (ParseException ex1) {
                }
            }
            zone = new Zone(typeOfZone, indexZone, coord[0], coord[1],
                               coord[2],
                               coord[3],
                               coord[4], coord[5], coord[6], coord[7],
                               depthMin, depthMax, lblColor.getBackground());

            setZone(zone);

            this.setVisible(false);
            this.dispose();

        }

        //--------------------------------------------------------------------------
        private boolean isDataValid() {

            //Check whether depth1 < depth2
            //Check P1, P2, P3, P4 belongs to the grid.
            return true;
        }

        //--------------------------------------------------------------------------
        public void focusGained(FocusEvent e) {
            if (e.getSource().getClass() == JFormattedTextField.class) {
                //System.out.println("selectAll");
                JFormattedTextField txtField = (JFormattedTextField) e.
                                               getSource();
                txtField.selectAll();
            }
        }

        //--------------------------------------------------------------------------
        public void focusLost(FocusEvent e) {

            Object src = e.getSource();
            try {
                if (src == txtRed) {
                    if (Integer.valueOf(txtRed.getText()).intValue() > 255) {
                        txtRed.setValue(new Integer(255));
                    }
                }
                if (src == txtGreen) {
                    if (Integer.valueOf(txtGreen.getText()).intValue() > 255) {
                        txtGreen.setValue(new Integer(255));
                    }
                }
                if (src == txtBlue) {
                    if (Integer.valueOf(txtBlue.getText()).intValue() > 255) {
                        txtBlue.setValue(new Integer(255));
                    }
                }
                if (src == txtRed | src == txtBlue | src == txtGreen) {
                    lblColor.setBackground(new Color(Integer.valueOf(txtRed.
                            getText()).
                            intValue()
                            ,
                            Integer.valueOf(txtGreen.getText()).
                            intValue()
                            ,
                            Integer.valueOf(txtBlue.getText()).
                            intValue()));
                }
                for (int i = 0; i < 8; i++) {
                    if (src == txtGeogArea[i]) {
                        if (Math.abs(Integer.valueOf(txtGeogArea[i].getText()).
                                     intValue()) >
                            (90 + 270 * ((i + 1) % 2))) {
                            txtGeogArea[i].setValue(new Float((90 +
                                    270 * ((i + 1) % 2))));
                        }
                    }
                }
            } catch (java.lang.NumberFormatException ex) {
                return;
            }
        }
    }
    //############################################################################
    //End of class FrameZone

    //----------------------------------------------------------------------------
    //End of class ZoneEditor
}
