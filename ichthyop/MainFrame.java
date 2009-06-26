package ichthyop;

import java.io.*;
import java.text.*;
import javax.imageio.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;

import ichthyop.config.*;
import ichthyop.datanc.*;
import ichthyop.util.*;
import ichthyop.util.calendar.*;
import ichthyop.util.chart.*;
import ichthyop.util.param.*;

/**
 *
 * <p>Title: Main frame</p>
 * essai de maj subversion
 * <p>Description: Main frame of the program. Controls the graphic interface</p>
 *
 * <p>Copyright: Copyright (c) Philippe VERLEY 2006-2007</p>
 * <p>This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation.</p>

    <p>This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.</p>

    <p>You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA.</p>
 *
 */

public class MainFrame
    extends JFrame implements ActionListener, ChangeListener {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the variables
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  private JPanel pnlSimuPanel;
  private ViewPanel pnlView;
  private JButton btnSTART;
  private JButton btnSTOP;
  private JLabel lblDate;
  static SimpleDateFormat dtFormat;
  private JButton btnEXIT;
  private JCheckBox ckBoxJPEG;
  private JSlider sldTime;
  private JPanel pnlToolBar;
  private static StatusBar statusBar;

  private JMenuItem itemNewFile, itemOpenFile, itemEditFile, itemExit;
  private static JCheckBoxMenuItem itemBG_RecruitmentZones, itemBG_ReleaseZones;
  private JCheckBoxMenuItem itemDepthChart, itemEdgeChart, itemRecruitChart,
      itemLengthChart, itemDeadChart, itemStageChart;
  private JRadioButtonMenuItem itemRdBtnBathy, itemRdBtnTp;
  private JRadioButtonMenuItem itemRdBtnZone, itemRdBtnNoColor;
  private JMenuItem itemPreferences;
  private JMenu menuDisplay, menuFile;

  private Simulation simulation;
  private boolean flagStop = false;
  private static int DISPLAY_COLOR;

  private Thread thrSimu;

  private Image[] imgSnapShot;
  private long[] timeSnapShot;

  private static File cfgFile;

  private IntegerParamIBM prmDtDisplay;

  private ColorBar clrBarBG, clrBarIndiv;
  private HistoChart depthChart, lengthChart;
  private AreaChart edgeChart;
  private RecruitChart recruitChart;
  private MortalityChart mortalityChart;
  private StageChart stageChart;
  private static float valmin, valmax;
  private float valmin_tp, valmin_bathy, valmax_tp, valmax_bathy;
  private static int xChart, yChart;
  private static Dimension windows;

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Constructor
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  public MainFrame() {

    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    System.out.println();
    for (int i = 0; i < Resources.NAME_LONG.length(); i++) {
      System.out.print('%');
    }
    System.out.println();
    System.out.println(Resources.NAME_LONG);
    for (int i = 0; i < Resources.NAME_LONG.length(); i++) {
      System.out.print('%');
    }
    System.out.println();
    try {
      simulation = new Simulation(this);
      createUI();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Waiting for config file ...");
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the main class
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  /**
   * First class read by the JVM. Creates a new instance of MainFrame().
   */
  public static void main(String[] args) {

    MainFrame frameIBM = new MainFrame();
    frameIBM.validate();
    frameIBM.setVisible(true);
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the methods
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  /**
   * Controls the events.
   */
  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();

    if (src == btnEXIT) {
      System.out.println("Exit");
      System.exit(0);
    }
    if (src == btnSTOP) {
      thrSimu.interrupt();
      flagStop = true;
      sldTime.setMaximum(sldTime.getValue());
      btnSTOP.setEnabled(false);
      btnSTART.setEnabled(true);
      menuFile.setEnabled(true);
      menuDisplay.setEnabled(true);
      return;
    }
    if (src == btnSTART) {
      if (GetConfig.isSerial()) {
        this.removeAll();
        this.setVisible(false);
        simulation.runSerial();
        System.exit(0);
      }
      flagStop = false;
      sldTime.setEnabled(false);
      init();
      return;
    }
    if (src == itemExit) {
      btnEXIT.doClick();
    }
    if (src == itemOpenFile) {
      JFileChooser chooser = new JFileChooser(".");
      chooser.setDialogType(JFileChooser.OPEN_DIALOG);
      IBMFileFilter ff = new IBMFileFilter(Resources.EXTENSION_CONFIG);
      chooser.setFileFilter(ff);
      int returnPath = chooser.showOpenDialog(MainFrame.this);
      if (returnPath == JFileChooser.APPROVE_OPTION) {
        cfgFile = chooser.getSelectedFile();
        System.out.println("Loading config file => " + cfgFile.getName()
            + " ... ");
        this.setTitle(Resources.NAME_SHORT + cfgFile.toString());
        new GetConfig(cfgFile);
        if (GetConfig.isErr()) {
          JOptionPane.showMessageDialog(this,
              "Error(s) reading the file of configuration.\nSee error message in console.");
          return;
        }
        simulation.setUp();
        setUpMenu();
        System.out.println(GetConfig.isSerial() ? "SERIAL SIMULATION"
            : "SINGLE SIMULATION");
        return;
      }
    }
    if (src == itemEditFile) {
      new ConfigFrame(cfgFile, this);
      return;
    }
    if (src == itemNewFile) {
      new ConfigFrame(this);
      itemEditFile.setEnabled(true);
      btnSTART.setEnabled(true);
      ckBoxJPEG.setEnabled(true);
      return;
    }
    if (src == itemRdBtnNoColor) {
      DISPLAY_COLOR = Resources.DISPLAY_NONE;
      itemPreferences.setEnabled(false);
      return;
    }
    if (src == itemRdBtnTp) {
      valmin = valmin_tp;
      valmax = valmax_tp;
      DISPLAY_COLOR = Resources.DISPLAY_TP;
      itemPreferences.setEnabled(true);
      return;
    }
    if (src == itemRdBtnBathy) {
      valmin = valmin_bathy;
      valmax = valmax_bathy;
      DISPLAY_COLOR = Resources.DISPLAY_DEPTH;
      itemPreferences.setEnabled(true);
      return;
    }
    if (src == itemRdBtnZone) {
      DISPLAY_COLOR = Resources.DISPLAY_ZONE;
      itemPreferences.setEnabled(false);
      return;
    }
    if (src == itemPreferences) {
      new PreferenceFrame(itemRdBtnBathy.isSelected(), valmin, valmax);
      return;
    }
    if (src == itemDepthChart) {
      if (itemDepthChart.isSelected()) {
        depthChart = new HistoChart(Resources.CHART_TITLE_DEPTH,
            Resources.CHART_LEGEND_DEPTH,
            Resources.CHART_LEGEND_NB_PARTICLES, 10,
            xChart, yChart);
        xChart += (int) (windows.width * 0.03f);
        yChart += (int) (windows.height * 0.03f);
      }
      else {
        depthChart.dispose();
      }
      return;
    }
    if (src == itemEdgeChart) {
      if (itemEdgeChart.isSelected()) {
        edgeChart = new AreaChart(Resources.CHART_TITLE_OUT,
            Resources.CHART_LEGEND_TIME,
            Resources.CHART_LEGEND_NB_PARTICLES,
            xChart, yChart);
        xChart += (int) (windows.width * 0.03f);
        yChart += (int) (windows.height * 0.03f);
      }
      else {
        edgeChart.dispose();
      }
      return;
    }
    if (src == itemLengthChart) {
      if (itemLengthChart.isSelected()) {
        lengthChart = new HistoChart(Resources.CHART_TITLE_LENGTH,
            Resources.CHART_LEGEND_LENGTH,
            Resources.CHART_LEGEND_NB_PARTICLES, 10,
            xChart, yChart);
        xChart += (int) (windows.width * 0.03f);
        yChart += (int) (windows.height * 0.03f);
      }
      else {
        lengthChart.dispose();
      }
      return;
    }
    if (src == itemRecruitChart) {
      if (itemRecruitChart.isSelected()) {
        recruitChart = new RecruitChart(Resources.CHART_TITLE_RECRUITMENT, null,
            Resources.CHART_LEGEND_NB_PARTICLES, xChart, yChart);
        xChart += (int) (windows.width * 0.03f);
        yChart += (int) (windows.height * 0.03f);
      }
      else {
        recruitChart.dispose();
      }
      return;
    }
    if (src == itemDeadChart) {
      if (itemDeadChart.isSelected()) {
        mortalityChart = new MortalityChart(Resources.CHART_TITLE_DEAD_COLD,
            Resources.CHART_LEGEND_TIME,
            Resources.CHART_LEGEND_NB_PARTICLES, xChart, yChart);
        xChart += (int) (windows.width * 0.03f);
        yChart += (int) (windows.height * 0.03f);
      }
      else {
        mortalityChart.dispose();
      }
      return;
    }
    if (src == itemStageChart) {
      if (itemStageChart.isSelected()) {
        stageChart = new StageChart(Resources.CHART_TITLE_STAGE,
            Resources.CHART_LEGEND_TIME,
            Resources.CHART_LEGEND_NB_PARTICLES, xChart, yChart);
        xChart += (int) (windows.width * 0.03f);
        yChart += (int) (windows.height * 0.03f);
      }
      else {
        stageChart.dispose();
      }
      return;
    }
  }

  //----------------------------------------------------------------------------
  /**
   * Creates the graphical user interface
   */
  private void createUI() throws Exception {

    pnlToolBar = new JPanel();
    btnEXIT = new JButton(Resources.BTN_EXIT);
    btnSTART = new JButton(Resources.BTN_START);
    btnSTOP = new JButton(Resources.BTN_STOP);

    //-------------------------
    // Main Frame
    this.getContentPane().setLayout(new GridBagLayout());
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    GraphicsDevice ecran = GraphicsEnvironment.getLocalGraphicsEnvironment().
        getDefaultScreenDevice();
    GraphicsConfiguration config = ecran.getDefaultConfiguration();
    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
    DisplayMode mode = config.getDevice().getDisplayMode();
    Dimension size = new Dimension(mode.getWidth(), mode.getHeight());
    windows = new Dimension(size.width - insets.left - insets.right,
        size.height - insets.bottom - insets.top);
    this.setSize( (int) (0.5f * windows.width), (int) (1.0f * windows.height));

    xChart = (int) (0.5 * windows.width);
    yChart = 0;

    this.setTitle(Resources.NAME_LONG);
    this.setLocation(0, 0);

    btnSTART.setEnabled(false);
    btnSTOP.setEnabled(false);

    ckBoxJPEG = new JCheckBox(Resources.BTN_CAPTURE, false);
    ckBoxJPEG.setHorizontalAlignment(ckBoxJPEG.CENTER);
    ckBoxJPEG.setBorder(BorderFactory.createLineBorder(Color.black));
    ckBoxJPEG.setBorderPainted(true);
    ckBoxJPEG.setEnabled(false);

    prmDtDisplay = new IntegerParamIBM(Resources.PRM_REFRESH, 12, "* dt", false);
    prmDtDisplay.setFormatPolicy(1, 4);
    prmDtDisplay.setBoundary(1, 9999);

    //----------------------------
    // MenuBar
    JMenuBar barMenu = new JMenuBar();
    menuFile = new JMenu(Resources.MENU_FILE);
    itemNewFile = new JMenuItem(Resources.MENU_FILE_NEW);
    itemOpenFile = new JMenuItem(Resources.MENU_FILE_OPEN);
    itemEditFile = new JMenuItem(Resources.MENU_FILE_EDIT);
    itemEditFile.setEnabled(false);
    itemExit = new JMenuItem(Resources.MENU_FILE_EXIT);
    menuFile.add(itemNewFile);
    menuFile.add(itemOpenFile);
    menuFile.add(itemEditFile);
    menuFile.addSeparator();
    menuFile.add(itemExit);
    barMenu.add(menuFile);

    menuDisplay = new JMenu(Resources.MENU_DISPLAY);
    itemBG_ReleaseZones = new JCheckBoxMenuItem(Resources.MENU_DISPLAY_BG_S, false);
    itemBG_RecruitmentZones = new JCheckBoxMenuItem(Resources.MENU_DISPLAY_BG_R, false);
    itemDepthChart = new JCheckBoxMenuItem(Resources.MENU_CHART_DEPTH, false);
    itemEdgeChart = new JCheckBoxMenuItem(Resources.MENU_CHART_EDGE, false);
    itemRecruitChart = new JCheckBoxMenuItem(Resources.MENU_CHART_RECRUIT, false);
    itemLengthChart = new JCheckBoxMenuItem(Resources.MENU_CHART_LENGTH, false);
    itemDeadChart = new JCheckBoxMenuItem(Resources.MENU_CHART_MORTALITY, false);
    itemStageChart = new JCheckBoxMenuItem(Resources.MENU_CHART_STAGE, false);

    itemRdBtnNoColor = new JRadioButtonMenuItem(Resources.MENU_DISPLAY_NONE, true);
    itemRdBtnBathy = new JRadioButtonMenuItem(Resources.MENU_DISPLAY_BATHY, false);
    itemRdBtnZone = new JRadioButtonMenuItem(Resources.MENU_DISPLAY_ZONE, false);
    itemRdBtnTp = new JRadioButtonMenuItem(Resources.MENU_DISPLAY_TP, false);
    ButtonGroup btnGrp = new ButtonGroup();
    btnGrp.add(itemRdBtnNoColor);
    btnGrp.add(itemRdBtnBathy);
    btnGrp.add(itemRdBtnZone);
    btnGrp.add(itemRdBtnTp);

    itemPreferences = new JMenuItem(Resources.MENU_DISPLAY_PREFERENCE);
    menuDisplay.add(itemBG_ReleaseZones);
    menuDisplay.add(itemBG_RecruitmentZones);
    menuDisplay.addSeparator();
    menuDisplay.add(itemDepthChart);
    menuDisplay.add(itemEdgeChart);
    menuDisplay.add(itemRecruitChart);
    menuDisplay.add(itemLengthChart);
    menuDisplay.add(itemStageChart);
    menuDisplay.add(itemDeadChart);
    menuDisplay.addSeparator();
    menuDisplay.add(itemRdBtnNoColor);
    menuDisplay.add(itemRdBtnBathy);
    menuDisplay.add(itemRdBtnZone);
    menuDisplay.add(itemRdBtnTp);
    menuDisplay.addSeparator();
    menuDisplay.add(itemPreferences);
    barMenu.add(menuDisplay);

    itemBG_ReleaseZones.setEnabled(false);
    itemBG_RecruitmentZones.setEnabled(false);
    itemPreferences.setEnabled(false);

    itemDepthChart.setEnabled(false);
    itemEdgeChart.setEnabled(false);
    itemRecruitChart.setEnabled(false);
    itemLengthChart.setEnabled(false);
    itemStageChart.setEnabled(false);
    itemDeadChart.setEnabled(false);

    itemRdBtnTp.setEnabled(false);
    itemRdBtnBathy.setEnabled(false);
    itemRdBtnZone.setEnabled(false);

    this.setJMenuBar(barMenu);

    //----------------------------
    pnlSimuPanel = new JPanel();

    lblDate = new JLabel(Resources.LBL_TIME);
    lblDate.setHorizontalAlignment(lblDate.CENTER);

    sldTime = new JSlider();
    sldTime.setEnabled(false);

    statusBar = new StatusBar();

    pnlToolBar.setBackground(this.getForeground());
    pnlToolBar.setLayout(new GridBagLayout());
    pnlSimuPanel.setBorder(BorderFactory.createLineBorder(Color.black));

    //-----------------------------
    //Sets components on screen
    this.getContentPane().add(pnlToolBar,
        new GridBagConstraints(0, 0, 1, 1, 75, 2,
        GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
        new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(pnlSimuPanel,
        new GridBagConstraints(0, 1, 1, 1, 75, 65
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(lblDate,
        new GridBagConstraints(0, 2, 1, 1, 75, 1
        , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
        new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(sldTime,
        new GridBagConstraints(0, 3, 1, 1, 75, 1
        , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
        new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(statusBar,
        new GridBagConstraints(0, 4, 1, 1, 75, 1
        , GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0), 0, 0));

    pnlToolBar.add(btnSTART, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.WEST, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 20, 0));
    pnlToolBar.add(btnSTOP, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 20, 0));
    pnlToolBar.add(ckBoxJPEG, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 20, 0));
    pnlToolBar.add(btnEXIT, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 20, 0));
    pnlToolBar.add(prmDtDisplay.createGUI(),
        new GridBagConstraints(0, 1, 4, 1, 1.0, 1.0
        , GridBagConstraints.WEST,
        GridBagConstraints.NONE,
        new Insets(5, 5, 5, 5), 20, 0));

    //Add listeners
    btnEXIT.addActionListener(this);
    btnSTART.addActionListener(this);
    btnSTOP.addActionListener(this);
    ckBoxJPEG.addActionListener(this);
    sldTime.addChangeListener(this);
    itemNewFile.addActionListener(this);
    itemOpenFile.addActionListener(this);
    itemEditFile.addActionListener(this);
    itemExit.addActionListener(this);
    itemDepthChart.addActionListener(this);
    itemEdgeChart.addActionListener(this);
    itemRecruitChart.addActionListener(this);
    itemLengthChart.addActionListener(this);
    itemStageChart.addActionListener(this);
    itemDeadChart.addActionListener(this);
    itemRdBtnNoColor.addActionListener(this);
    itemRdBtnBathy.addActionListener(this);
    itemRdBtnZone.addActionListener(this);
    itemRdBtnTp.addActionListener(this);
    itemPreferences.addActionListener(this);

    valmin_tp = Resources.TP_MIN;
    valmax_tp = Resources.TP_MAX;
    valmin_bathy = Resources.BATHY_MIN;
    valmax_bathy = Resources.BATHY_MAX;
    itemRdBtnNoColor.doClick();

  }

  //----------------------------------------------------------------------------
  /**
   * Updates the GUI.
   */
  private void update() {

    this.getContentPane().removeAll();
    this.setTitle(Resources.NAME_SHORT + cfgFile.toString());
    lblDate.setText(Resources.LBL_TIME);
    pnlSimuPanel.setBorder(BorderFactory.createLineBorder(Color.black));

    int j = 0;
    this.getContentPane().add(pnlToolBar,
        new GridBagConstraints(0, j++, 1, 1, 75, 2,
        GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
        new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(pnlSimuPanel,
        new GridBagConstraints(0, j++, 1, 1, 75, 65
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(lblDate,
        new GridBagConstraints(0, j++, 1, 1, 75, 1
        , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
        new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(sldTime,
        new GridBagConstraints(0, j++, 1, 1, 75, 1
        , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
        new Insets(5, 5, 5, 5), 0, 0));
    if (clrBarBG != null) {
      this.getContentPane().add(clrBarBG,
          new GridBagConstraints(0, j++, 1, 1, 75, 1
          , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));
    }
    if ( (DISPLAY_COLOR == Resources.DISPLAY_TP
        | DISPLAY_COLOR == Resources.DISPLAY_DEPTH) && clrBarIndiv != null) {
      this.getContentPane().add(clrBarIndiv,
          new GridBagConstraints(0, j++, 1, 1, 75, 1
          , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));
    }
    this.getContentPane().add(statusBar,
        new GridBagConstraints(0, j++, 1, 1, 75, 1
        , GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0), 0, 0));

    pnlToolBar.add(btnSTART, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.WEST, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 20, 0));
    pnlToolBar.add(btnSTOP, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 20, 0));
    pnlToolBar.add(ckBoxJPEG, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 20, 0));
    pnlToolBar.add(btnEXIT, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 20, 0));

    this.validate();
    this.repaint();
  }

  //----------------------------------------------------------------------------
  /**
   * Initializes the program for starting a new SINGLE simulation.
   */
  private void init() {

    System.out.print("Initializing ...");

    btnSTOP.setEnabled(true);
    btnSTART.setEnabled(false);
    prmDtDisplay.setEnabled(false);
    menuFile.setEnabled(false);
    menuDisplay.setEnabled(false);

    simulation.init();

    createColorBar(DISPLAY_COLOR);

    String frmt = (simulation.getCalendar().getClass() == Calendar1900.class)
        ? "yyyy/MM/dd HH:mm"
        : "yy/MM/dd HH:mm";
    dtFormat = new SimpleDateFormat(frmt);
    dtFormat.setCalendar(simulation.getCalendar());
    sldTime.setMinimum(0);
    sldTime.setMaximum(simulation.getNbSteps() - 1);
    sldTime.setValue(0);
    imgSnapShot = new BufferedImage[simulation.getNbSteps()];
    timeSnapShot = new long[simulation.getNbSteps()];

    resetCharts();
    SimuPanel.init();
    update();

    System.out.println("[OK]");

    thrSimu = new Thread(simulation);
    thrSimu.start();
  }

  //----------------------------------------------------------------------------
  /**
   * Resets control charts.
   */
  private void resetCharts() {
    if (edgeChart != null) {
      edgeChart.reset();
    }
    if (stageChart != null) {
      stageChart.reset();
    }
    if (mortalityChart != null) {
      mortalityChart.reset();
    }
  }

  //----------------------------------------------------------------------------
  /**
   * Creates the particle colour bar and sets it on the main frame.
   * @param DISPLAY_COLOR int
   */
  private void createColorBar(int DISPLAY_COLOR) {

    switch (DISPLAY_COLOR) {
      case Resources.DISPLAY_DEPTH:
        clrBarIndiv = new ColorBar(Resources.LBL_DEPTH,
            Resources.HORIZONTAL, valmin, valmax, Color.YELLOW, Color.RED);
        SimuPanel.setValMin_Max( -valmin, -valmax);
        break;
      case Resources.DISPLAY_TP:
        clrBarIndiv = new ColorBar(Resources.LBL_TP,
            Resources.HORIZONTAL, valmin, valmax, Color.YELLOW, Color.RED);
        SimuPanel.setValMin_Max(valmin, valmax);
        break;
    }
  }

  //----------------------------------------------------------------------------
  /**
   * Creates the viewver once the SINGLE simulation is over or interrupted and
   * updates GUI.
   */
  public void createViewver() {

    flagStop = true;
    this.getContentPane().removeAll();

    pnlView = new ViewPanel(imgSnapShot[sldTime.getMaximum()]);
    btnSTART.setEnabled(true);
    btnSTOP.setEnabled(false);
    menuFile.setEnabled(true);
    menuDisplay.setEnabled(true);
    prmDtDisplay.setEnabled(true);
    lblDate.setText(Resources.LBL_STEP + sldTime.getMaximum() + " - "
        + Resources.LBL_TIME +
        dtFormat.format(timeSnapShot[sldTime.getMaximum()]));
    sldTime.setEnabled(true);
    pnlView.setBorder(BorderFactory.createLineBorder(Color.black));
    statusBar.setMessage(Resources.MSG_REPLAY);

    int j = 0;
    this.getContentPane().add(pnlToolBar,
        new GridBagConstraints(0, j++, 1, 1, 75, 2,
        GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
        new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(pnlView,
        new GridBagConstraints(0, j++, 1, 1, 75, 65
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(lblDate,
        new GridBagConstraints(0, j++, 1, 1, 75, 1
        , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
        new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(sldTime,
        new GridBagConstraints(0, j++, 1, 1, 75, 1
        , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
        new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(clrBarBG,
        new GridBagConstraints(0, j++, 1, 1, 75, 1
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 0, 0));
    if ( (DISPLAY_COLOR == Resources.DISPLAY_TP
        | DISPLAY_COLOR == Resources.DISPLAY_DEPTH) && clrBarIndiv != null) {
      this.getContentPane().add(clrBarIndiv,
          new GridBagConstraints(0, j++, 1, 1, 75, 1
          , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));
    }
    this.getContentPane().add(statusBar,
        new GridBagConstraints(0, j++, 1, 1, 75, 1
        , GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0), 0, 0));

    pnlToolBar.add(btnSTART, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.WEST, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 20, 0));
    pnlToolBar.add(btnSTOP, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 20, 0));
    pnlToolBar.add(ckBoxJPEG, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 20, 0));
    pnlToolBar.add(btnEXIT, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 20, 0));

    this.validate();
    this.repaint();
  }

  //----------------------------------------------------------------------------
  /**
   * Records the current component set as argument into a PNG picture.
   */
  private void screen2File(Component cpnt) {

    SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
    dtFormat.setCalendar(simulation.getCalendar());
    String fileName = GetConfig.getDirectorOut() +
        "img_" + dtFormat.format(simulation.getCalendar().getTime())
        + ".png";
    System.out.println(fileName);

    try {
      BufferedImage bi = new BufferedImage(cpnt.getWidth(),
          cpnt.getHeight(),
          BufferedImage.TYPE_INT_RGB);
      Graphics g = bi.getGraphics();
      cpnt.paintAll(g);
      ImageIO.write(bi, "PNG", new File(fileName));
    }
    catch (Exception ex) {
      System.out.println("Problem writing file " + fileName);
    }
  }

  //----------------------------------------------------------------------------
  /**
   * Refreshes particle location on screen and control charts.
   */
  public synchronized void refresh() {

    int i_step = simulation.get_iStep();
    statusBar.setMessage(Resources.MSG_REFRESH_SCREEN);
    lblDate.setText(Resources.LBL_STEP + simulation.get_iStep() + " / " +
        simulation.getNbSteps() + " - "
        + Resources.LBL_TIME +
        dtFormat.format(simulation.getCalendar().getTime()));
    sldTime.setValue(i_step);
    imgSnapShot[i_step] = getImage(pnlSimuPanel);
    timeSnapShot[i_step] = simulation.get_time();
    synchronized (pnlSimuPanel) {
      pnlSimuPanel.repaint();
    }
    refreshCharts(dtFormat.format(simulation.getCalendar().getTime()));
    if (ckBoxJPEG.isSelected()) {
      screen2File(pnlSimuPanel);
    }
  }

  //----------------------------------------------------------------------------
  /**
   * Refreshes control chart display.
   */
  private void refreshCharts(String strTime) {
    if (depthChart != null) {
      depthChart.refresh(Population.depthDistribution, strTime);
    }
    if (edgeChart != null) {
      edgeChart.refresh(new double[] {
          Population.outCounting}, null);
    }
    if (recruitChart != null) {
      recruitChart.refresh(Population.recruitCounting, strTime);
    }
    if (lengthChart != null) {
      lengthChart.refresh(Population.lengthDistribution, strTime);
    }
    if (mortalityChart != null) {
      mortalityChart.refresh(Population.mortalityCounting, null);
    }
    if (stageChart != null) {
      stageChart.refresh(Population.stageDistribution, null);
    }

  }

  //----------------------------------------------------------------------------
  /**
   * Captures the image of the component set as argument.
   */
  public BufferedImage getImage(Component component) {
    if (component == null) {
      return null;
    }
    int width = component.getWidth();
    int height = component.getHeight();
    BufferedImage image = new BufferedImage(width, height,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    component.paintAll(g);
    g.dispose();
    return image;
  }

  //----------------------------------------------------------------------------
  /**
   * Controls the time slider events.
   */
  public void stateChanged(ChangeEvent e) {
    if (flagStop && pnlView != null) {
      int i_step = sldTime.getValue();
      pnlView.setImage(imgSnapShot[i_step]);
      lblDate.setText(Resources.LBL_STEP + i_step + " - " + Resources.LBL_TIME +
          dtFormat.format(timeSnapShot[i_step]));
    }
  }

  //----------------------------------------------------------------------------
  /**
   * Sets the main frame up once a file of configuration has been loaded
   * or created
   */
  public void setUpMenu() {

    boolean bln;

    btnSTART.setEnabled(true);

    if (GetConfig.isSerial()) {
      menuDisplay.setEnabled(false);
      prmDtDisplay.setEnabled(false);
      statusBar.setMessage(Resources.MSG_READY);
      return;
    }
    prmDtDisplay.setEnabled(true);
    if (GetConfig.isRecord()) {
      prmDtDisplay.setValue(GetConfig.getRecordFrequency());
    }
    itemEditFile.setEnabled(true);
    ckBoxJPEG.setEnabled(true);

    bln = (GetConfig.getDimSimu() == Resources.SIMU_3D);
    if (!bln) {
      if (itemRdBtnBathy.isSelected()) {
        itemRdBtnBathy.doClick();
      }
      if (itemRdBtnTp.isSelected()) {
        itemRdBtnTp.doClick();
      }
      if (itemDepthChart.isSelected()) {
        itemDepthChart.doClick();
      }
    }
    itemRdBtnBathy.setEnabled(bln);
    itemRdBtnTp.setEnabled(bln);
    itemDepthChart.setEnabled(bln);

    itemEdgeChart.setEnabled(true);

    bln = (GetConfig.getTypeRecruitment() !=
        Resources.RECRUIT_NONE);
    if (!bln) {
      itemBG_RecruitmentZones.setSelected(false);
      if (itemRecruitChart.isSelected()) {
        itemRecruitChart.doClick();
      }
    }
    itemRecruitChart.setEnabled(bln);
    itemBG_RecruitmentZones.setEnabled(bln);

    bln = GetConfig.isGrowth();
    if (!bln) {
      if (itemStageChart.isSelected()) {
        itemStageChart.doClick();
      }
      if (itemLengthChart.isSelected()) {
        itemLengthChart.doClick();
      }

    }
    itemLengthChart.setEnabled(bln);
    itemStageChart.setEnabled(bln);

    bln = GetConfig.isLethalTp();
    if (!bln) {
      if (itemDeadChart.isSelected()) {
        itemDeadChart.doClick();
      }
    }
    itemDeadChart.setEnabled(bln);

    bln = (GetConfig.getTypeRelease() == Resources.RELEASE_ZONE);
    if (!bln) {
      if (itemBG_ReleaseZones.isSelected()) {
        itemBG_ReleaseZones.doClick();
      }
      if (itemRdBtnZone.isSelected()) {
        itemRdBtnNoColor.doClick();
      }
    }
    itemBG_ReleaseZones.setEnabled(bln);
    itemRdBtnZone.setEnabled(bln);

    itemPreferences.setEnabled(itemRdBtnTp.isSelected()
        | itemRdBtnBathy.isSelected());

    prmDtDisplay.setUnit("* dt (dt = " +
        String.valueOf(GetConfig.get_dt() / 1000) +
        " sec)");

    pnlSimuPanel = new SimuPanel(simulation);
    clrBarBG = new ColorBar(Resources.LBL_BATHY, Resources.HORIZONTAL, 0,
        GetData.getDepthMax(), Color.CYAN,
        new Color(0, 0, 150));
    update();
    statusBar.setMessage(Resources.MSG_READY);
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the getters and the setters
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  public boolean isStoped() {
    return flagStop;
  }

  //----------------------------------------------------------------------------
  public void setFlagStop(boolean flagStop) {
    this.flagStop = flagStop;
  }

  //----------------------------------------------------------------------------
  public static void setCfgFile(File cfgF) {
    cfgFile = cfgF;
  }

  //----------------------------------------------------------------------------
  public long getDtDisplay() {
    return (long) (prmDtDisplay.getValue().longValue() * GetConfig.get_dt());
  }

  //----------------------------------------------------------------------------
  public Simulation getSimulation() {
    return simulation;
  }

  //----------------------------------------------------------------------------
  public static boolean isBG_SZone() {
    return itemBG_ReleaseZones.isSelected();
  }

  //----------------------------------------------------------------------------
  public static boolean isBG_RZone() {
    return itemBG_RecruitmentZones.isSelected();
  }

  //----------------------------------------------------------------------------
  public static float getValMin() {
    return valmin;
  }

  //----------------------------------------------------------------------------
  public static float getValMax() {
    return valmax;
  }

  //----------------------------------------------------------------------------
  public static StatusBar getStatusBar() {
    return statusBar;
  }

  //----------------------------------------------------------------------------
  public static int getDisplayColor() {
    return DISPLAY_COLOR;
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of inner classes
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //############################################################################
  private class PreferenceFrame
      extends JFrame implements ActionListener {

    private String strTitleMin, strTitleMax, strUnit;
    private FloatParamIBM prmMin, prmMax;
    private JButton btnOK, btnCANCEL;
    private boolean isBathy;

    //--------------------------------------------------------------------------
    PreferenceFrame(boolean isBathy, float valmin, float valmax) {

      this.isBathy = isBathy;
      strTitleMin = isBathy ? Resources.LBL_MIN_DEPTH : Resources.LBL_MIN_TP;
      strTitleMax = isBathy ? Resources.LBL_MAX_DEPTH : Resources.LBL_MAX_TP;
      strUnit = isBathy ? Resources.LBL_UNIT_DEPTH : Resources.LBL_UNIT_TP;
      prmMin = new FloatParamIBM(strTitleMin, valmin, strUnit, true);
      prmMin.setFormatPolicy(1, 3, 1, 1);
      prmMax = new FloatParamIBM(strTitleMax, valmax, strUnit, true);
      prmMax.setFormatPolicy(1, 3, 1, 1);
      btnOK = new JButton(Resources.BTN_OK);
      btnCANCEL = new JButton(Resources.BTN_CANCEL);

      createUI();
      this.validate();
      this.setVisible(true);
    }

    //--------------------------------------------------------------------------
    private void createUI() {

      this.getContentPane().setLayout(new GridBagLayout());
      this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      GraphicsDevice ecran = GraphicsEnvironment.getLocalGraphicsEnvironment().
          getDefaultScreenDevice();
      GraphicsConfiguration config = ecran.getDefaultConfiguration();
      DisplayMode mode = config.getDevice().getDisplayMode();
      Dimension size = new Dimension(mode.getWidth(), mode.getHeight());
      this.setSize( (int) (0.3f * size.width),
          (int) (0.2f * size.height));

      this.setTitle(Resources.TITLE_FRAME_PREF);
      //this.setLocation(0, 0);
      this.setLocationRelativeTo(this.getParent());

      this.getContentPane().add(new JLabel(Resources.LBL_COLORBAR),
          new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
          , GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      this.getContentPane().add(prmMin.createGUI(),
          new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
          , GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 25, 5, 5), 0, 0));
      this.getContentPane().add(prmMax.createGUI(),
          new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
          , GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 25, 5, 5), 0, 0));
      this.getContentPane().add(btnOK,
          new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
          , GridBagConstraints.EAST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      this.getContentPane().add(btnCANCEL,
          new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0
          , GridBagConstraints.EAST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));

      btnOK.addActionListener(this);
      btnCANCEL.addActionListener(this);
    }

    //--------------------------------------------------------------------------
    public void actionPerformed(ActionEvent e) {

      if (e.getSource() == btnOK) {
        if (isBathy) {
          valmin_bathy = prmMin.getValue().floatValue();
          valmax_bathy = prmMax.getValue().floatValue();
          itemRdBtnBathy.doClick();
        }
        else {
          valmin_tp = prmMin.getValue().floatValue();
          valmax_tp = prmMax.getValue().floatValue();
          itemRdBtnTp.doClick();
        }

        this.dispose();
      }
      if (e.getSource() == btnCANCEL) {
        this.dispose();
      }
    }

  }

  //----------------------------------------------------------------------------
  // End of inner class PreferenceFrame

  //############################################################################
  // Inner class Status Bar
  public class StatusBar
      extends JLabel {

    //--------------------------------------------------------------------------
    public StatusBar() {
      super();
      super.setPreferredSize(new Dimension(100, 16));
      setMessage(Resources.MSG_WAITING);
    }

    //--------------------------------------------------------------------------
    public void setMessage(String message) {
      setText(Resources.MSG_PREFIX + message);
    }

    //---------------------------------------------------------------------------
    // End of inner class StatusBar
  }

//------------------------------------------------------------------------------
// End of class
}
