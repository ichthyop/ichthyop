package ichthyop.ui;

/** import AWT */
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowListener;
import java.awt.GridLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.DisplayMode;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** import java.io */
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

/** import java.imageio */
import javax.imageio.ImageIO;

/** import java.text */
import java.text.SimpleDateFormat;

/** import java.util */
import java.util.Calendar;

/** import SWING */
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.ButtonGroup;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.BorderFactory;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSlider;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JProgressBar;

/** local import */
import ichthyop.core.Simulation;
import ichthyop.core.Step;
import ichthyop.io.Configuration;
import ichthyop.io.OutputNC;
import ichthyop.io.Dataset;
import ichthyop.ui.ConfigurationUI;
import ichthyop.ui.chart.ChartManager;
import ichthyop.ui.param.FloatParameter;
import ichthyop.ui.param.IntegerParameter;
import ichthyop.util.Resources;
import ichthyop.util.SafeSwingWorker;
import ichthyop.util.Constant;
import ichthyop.bio.DVMPattern;
import java.awt.event.WindowEvent;

/**
 * <p>This is the main class of the application. It provides a Graphical User
 * Interface that helps the user to interact with the program.</p>
 *
 * This tool has been developed to study how physical factors (e.g., currents,
 * water temperature) and biological factors (e.g., egg buoyancy, larva growth)
 * affect the dynamics of fish eggs and larvae. It uses velocity, temperature
 * and salinity three-dimensional fields archived from simulations of the
 * Regional Oceanic Modelling System (ROMS) or of the Model for Applications
 * at Regional Scale (MARS).
 *
 * The tool also enables to track trajectories of virtual drifters and the
 * water properties (temperature, salinity) they experience along the way.
 *
 * The tool offers two functioning modes. The first one, SINGLE mode, allows
 * a visualisation of the transport of virtual eggs and larvae in a
 * user-friendly graphic interface. The second mode, SERIAL, enables to run
 * series of simulations based on different pre-defined sets of parameters
 * and produces output files. In these is stored information about the
 * simulated dynamics of individuals (time, longitude, latitude, depth,
 * length, etc.). Output files are in netcdf format and can be post-processed
 * easily. Routines in R can be sent upon request for plotting trajectories or
 * computing the numbers of individuals transported from pre-defined release
 * (spawning) areas to pre-defined destination (recruitment) areas.
 *
 * Previous/modified versions of this tool have been used to investigate
 * the effects of physical and biological factors on the dynamics of anchovy
 * (Engraulis encrasicolus, Engraulis ringens) and sardine (Sardinops sagax)
 * ichthyoplankton in the southern Benguela and in the northern Humboldt
 * upwelling systems. These works associated Institut de Recherche pour le
 * Développement (IRD, teams R079 GEODES and R097 ECO-UP) from France,
 * University of Cape Town (UCT) and Marine & Coastal Management (MCM)
 * from South Africa, and Instituto del Mar del Perú (IMARPE) from Peru.
 * References for published works are provided below.
 *
 * <p>We are grateful to PREVIMER for financial support. We also thank the
 * Laboratoire de Physique des Océans (LPO) and the DYNECO department of
 * IFREMER.</p>
 *
 * Any correspondance should be adressed to {@link mailto:info@previmer.org} or
 * {@link mailto:christophe.lett@ird.fr} for scientific questions.
 *
 * <p>REFERENCES (please send an e-mail to receive pdf)
 * <ul>
 * <li>Huggett J, Fréon P, Mullon C, Penven P (2003) Modelling the transport
 * success of anchovy Engraulis encrasicolus eggs and larvae in the southern
 * Benguela: the effect of spatio-temporal spawning patterns. Mar Ecol Prog
 * Ser 250:247-262.
 * <li>Lett C, Roy C, Levasseur A, van der Lingen CD, Mullon C (2006)
 * Simulation and quantification of enrichment and retention processes in the
 * southern Benguela upwelling ecosystem. Fish Oceanogr 15:363-372.
 * <li>Lett C, Penven P, Ayón P, Fréon P (2007) Enrichment, concentration and
 * retention processes in relation to anchovy (Engraulis ringens) eggs and
 * larvae distributions in the northern Humboldt upwelling ecosystem. J Mar
 * Syst 64:189-200.
 * <li>Lett C, Veitch J, van der Lingen CD, Hutchings L (2007) Assessment of
 * an environmental barrier to transport of ichthyoplankton from the southern
 * to the northern Benguela ecosystems. Mar Ecol Prog Ser 347:247-259.
 * <li>Miller DCM, Moloney CL, van der Lingen CD, Lett C, Mullon C, Field JG
 * (2006) Modelling the effects of physical-biological interactions and
 * spatial variability in spawning and nursery areas on transport and retention
 * of sardine eggs and larvae in the southern Benguela ecosystem. J Mar Syst
 * 61:212-229
 * <li>Mullon C, Cury P, Penven P (2002) Evolutionary individual-based model
 * for the recruitment of anchovy (Engraulis capensis) in the southern Benguela.
 * Can J Fish Aquat Sci 59:910-922.
 * <li>Mullon C, Fréon P, Parada C, van der Lingen C, Huggett J (2003) From
 * particles to individuals: modelling the early stages of anchovy (Engraulis
 * capensis/encrasicolus) in the southern Benguela. Fish Oceanogr 12:396-406.
 * <li>Parada C, van der Lingen CD, Mullon C, Penven P (2003) Modelling the
 * effect of buoyancy on the transport of anchovy (Engraulis capensis) eggs
 * from spawning to nursery grounds in the southern Benguela: an IBM approach.
 * Fish Oceanogr 12:170-184.
 * </p>
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */
public class MainFrame extends JFrame implements ActionListener, ChangeListener,
        WindowListener {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * The {@code Simulation} object managed by the main frame
     */
    private Simulation simulation;
    /**
     * The configuration file
     */
    private static File cfgFile;
    /**
     * The {@code SwingWorker} that controls the setup of the simulation.
     */
    private SetupSwingWorker setupSW;
    /**
     * A flag that indicates whether or not the simulation is over
     * (came to end or interrupted by the user)
     */
    private boolean flagStop = false;
    /**
     * A flag that indicated wether or not the simulation should stores
     * screenshots in heap memory for replay at the end of the run.
     */
    private boolean isReplayEnabled = true;
    /**
     * Array that stores the images of the steps of the simulation for later
     * replay.
     */
    private Image[] imgSnapShot;
    /**
     * Arrays that stores the date and time of the steps of the simulation.
     */
    private String[] timeSnapShot;
    /**
     * Current extreme value for particle color range
     */
    private static float valmin,  valmax;
    /**
     * Extreme value for temperature color range
     */
    private float valmin_tp,  valmax_tp;
    /**
     * Extreme value for depth color range
     */
    private float valmin_bathy,  valmax_bathy;
    /**
     * The time arrow read in configuration file (since v2.1)
     */
    private static int timeArrow;
    /**
     * An int characterizing the diplay options.
     * @see ichthyop.util.Constant for details about the labels
     * characterizing the display options.
     */
    private static int displayColor;

    // Common components to SERIAL and SINGLE
    private Dimension windows;
    private JButton btnStart;
    private JButton btnStop;
    private JButton btnExit;
    private JPanel toolbar;
    private static StatusBar statusbar;
    private JMenuItem itemNew,  itemOpen,  itemEdit,  itemPath,  itemExit;
    private JMenu menuFile;

    // Components for SINGLE
    private JPanel singleUI;
    private JCheckBox ckBoxCapture;
    private JCheckBox ckBoxReplay;
    private JSlider sldTime;
    private SimulationUI simulationUI;
    private JLabel lblDate;
    private static IntegerParameter prmRefresh;
    private ReplayPanel viewer;
    private static JCheckBoxMenuItem itemBgRecruitmentZone,  itemBgReleaseZone;
    private JCheckBoxMenuItem itemDepthChart,  itemEdgeChart,  itemRecruitChart,  itemLengthChart,  itemDeadChart,  itemStageChart;
    private JRadioButtonMenuItem itemRdBtnBathy,  itemRdBtnTp;
    private JRadioButtonMenuItem itemRdBtnZone,  itemRdBtnNoColor;
    private JMenuItem itemPreferences;
    private JMenu menuSingleUI;
    private ColorBar colorbarBathy,  colorbarParticle;

    // Components for SERIAL
    private JPanel serialUI;
    private JLabel lblSimulation;
    private JProgressBar barSimulation;
    private JProgressBar barRun;

///////////////
// Constructors
///////////////
    /**
     * Constructs the main frame and builds the UI.
     */
    public MainFrame() {

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        addWindowListener(this);

        /** prints application title in the console */
        System.out.println();
        for (int i = 0; i < Resources.TITLE_LARGE.length(); i++) {
            System.out.print('%');
        }
        System.out.println();
        System.out.println(Resources.TITLE_LARGE);
        for (int i = 0; i < Resources.TITLE_LARGE.length(); i++) {
            System.out.print('%');
        }
        System.out.println();

        /** Builds the UI */
        setJMenuBar(createMenuBar());
        buildUI();
    }

////////////////////////////
// Definition of the methods
////////////////////////////
    /**
     * The main class is the first class read by the Java Virtual Machine.
     * It creates a new instance of {@code MainFrame}.
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Toolkit.getDefaultToolkit().setDynamicLayout(true);
                new MainFrame().setVisible(true);
            }
        });
    }

    /**
     * Builds the UI
     */
    private void buildUI() {

        // Sizes the frame
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

        this.setTitle(Resources.TITLE_LARGE);
        this.setLocationRelativeTo(null);

        // Creates objects
        toolbar = new JPanel(new GridLayout(1, 3, 5, 0));
        btnStart = new JButton(Resources.BTN_START);
        btnExit = new JButton(Resources.BTN_EXIT);
        btnStop = new JButton(Resources.BTN_STOP);
        statusbar = new StatusBar();

        // Enabled
        btnStart.setEnabled(false);
        btnStop.setEnabled(false);

        // Makes toolbar
        toolbar.add(btnStart);
        toolbar.add(btnStop);
        toolbar.add(btnExit);

        // Adds components on the contentPane
        updateUI();
        pack();

        // Listeners
        btnStart.addActionListener(this);
        btnStop.addActionListener(this);
        btnExit.addActionListener(this);
    }

    /**
     * Updates the UI
     */
    private void updateUI() {

        getContentPane().removeAll();

        getContentPane().add(toolbar,
                new GridBagConstraints(0, 0, 1, 1, 1.0, 5,
                GridBagConstraints.SOUTHWEST,
                GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
        if (singleUI != null) {
            getContentPane().add(singleUI,
                    new GridBagConstraints(0, 1, 1, 1, 1.0, 90,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
        }
        if (serialUI != null) {
            setSize((int) (0.5 * windows.width), (int) (0.25 * windows.height));
            setLocationRelativeTo(null);
            getContentPane().add(serialUI,
                    new GridBagConstraints(0, 1, 1, 1, 1.0, 40,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        }
        getContentPane().add(statusbar,
                new GridBagConstraints(0, 2, 1, 1, 1.0, 5,
                GridBagConstraints.SOUTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

        validate();
        repaint();
    }

    /**
     * Enables (or disables) the toolbar buttons.
     * @param enabled <code>true</code> to enable the button,
     *                <code>false</code> otherwise
     */
    private void setToolbarEnabled(boolean enabled) {

        /** Common buttons */
        btnStart.setEnabled(enabled);
        btnStop.setEnabled(!enabled);
        menuFile.setEnabled(enabled);

        /** only for SINGLE */
        if (singleUI != null) {
            menuSingleUI.setEnabled(enabled);
            prmRefresh.setEditable(enabled);
        }
    }

    /**
     * Creates the main frame menu bar.
     * @return the JMenuBar to display.
     */
    private JMenuBar createMenuBar() {

        JMenuBar menuBar = new JMenuBar();
        menuFile = new JMenu(Resources.MENU_FILE);
        itemNew = new JMenuItem(Resources.MENU_FILE_NEW);
        itemOpen = new JMenuItem(Resources.MENU_FILE_OPEN);
        itemEdit = new JMenuItem(Resources.MENU_FILE_EDIT);
        itemEdit.setEnabled(false);
        itemPath = new JMenuItem(Resources.MENU_FILE_PATH);
        itemExit = new JMenuItem(Resources.MENU_FILE_EXIT);
        menuFile.add(itemNew);
        menuFile.add(itemOpen);
        menuFile.add(itemEdit);
        menuFile.addSeparator();
        menuFile.add(itemPath);
        menuFile.addSeparator();
        menuFile.add(itemExit);
        menuBar.add(menuFile);

        itemNew.addActionListener(this);
        itemOpen.addActionListener(this);
        itemEdit.addActionListener(this);
        itemPath.addActionListener(this);
        itemExit.addActionListener(this);

        return menuBar;
    }

    /**
     * Builds up a specific UI for SINGLE mode, with a {@code SimulationUI}
     * components to display the steps of the simulation on screen.
     */
    private JPanel buildSingleUI() {

        JPanel panel = new JPanel(new GridBagLayout());

        prmRefresh = new IntegerParameter(Resources.PRM_REFRESH, 12, "* dt", false);
        prmRefresh.setFormatPolicy(1, 4);
        prmRefresh.setBoundary(1, 9999);
        prmRefresh.createUI();

        ckBoxReplay = new JCheckBox(Resources.CK_BOX_REPLAY, true);
        ckBoxReplay.setHorizontalAlignment(JCheckBox.CENTER);
        ckBoxReplay.setEnabled(true);

        ckBoxCapture = new JCheckBox(Resources.BTN_CAPTURE, false);
        ckBoxCapture.setHorizontalAlignment(JCheckBox.CENTER);
        ckBoxCapture.setEnabled(false);

        lblDate = new JLabel(Resources.LBL_TIME);
        lblDate.setHorizontalAlignment(JLabel.CENTER);

        sldTime = new JSlider();
        sldTime.setEnabled(false);

        new ChartManager();

        colorbarBathy = new ColorBar(Resources.LBL_BATHY + " " +
                Resources.UNIT_METER, Constant.HORIZONTAL,
                0, Dataset.getDepthMax(), Color.CYAN,
                new Color(0, 0, 150));

        // Listeners
        sldTime.addChangeListener(this);
        ckBoxCapture.addActionListener(this);
        ckBoxReplay.addActionListener(this);

        return panel;
    }

    /**
     * Builds up a specific UI for the SERIAL mode. Displays two progress bars,
     * one for the ongoing run and the other for the progress of all the runs
     * of the simulation.
     */
    private JPanel buildSerialUI() {

        btnStart.setEnabled(true);

        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));

        lblDate = new JLabel(Resources.LBL_TIME);
        lblDate.setHorizontalAlignment(JLabel.LEFT);
        lblSimulation = new JLabel("Simulation #/# - Time left : ");
        lblSimulation.setHorizontalAlignment(JLabel.LEFT);
        barSimulation = new JProgressBar(0, 100);
        barRun = new JProgressBar(0, 100);

        panel.add(lblDate);
        panel.add(barSimulation);
        panel.add(lblSimulation);
        panel.add(barRun);

        return panel;
    }

    /**
     * Initializes the components for the SINGLE mode.
     */
    private void initSingleUI() {

        viewer = null;
        sldTime.setEnabled(false);
        ckBoxReplay.setEnabled(ckBoxReplay.isSelected());

        if (simulation != null) {
            simulationUI = new SimulationUI();
        }

        ChartManager.reset();

        if (itemRdBtnBathy.isSelected()) {
            colorbarParticle = new ColorBar(Resources.LBL_DEPTH,
                    Constant.HORIZONTAL,
                    Math.abs(valmin), Math.abs(valmax),
                    Color.YELLOW, Color.RED);
        } else if (itemRdBtnTp.isSelected()) {
            colorbarParticle = new ColorBar(Resources.LBL_TEMPERATURE,
                    Constant.HORIZONTAL,
                    Math.abs(valmin), Math.abs(valmax),
                    Color.YELLOW, Color.RED);
        }

        updateSingleUI();
    }

    /**
     * Updates the components of the SINGLE mode.
     */
    private void updateSingleUI() {

        singleUI.removeAll();
        int j = 0;
        singleUI.add(prmRefresh.createUI(),
                new GridBagConstraints(0, j, 1, 1, 50, 1,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(0, 5, 5, 5), 0, 0));
        singleUI.add(ckBoxReplay,
                new GridBagConstraints(1, j, 1, 1, 25, 1,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(0, 5, 5, 5), 0, 0));
        singleUI.add(ckBoxCapture,
                new GridBagConstraints(2, j++, 1, 1, 25, 1,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(0, 5, 5, 5), 0, 0));
        if (simulationUI != null) {
            singleUI.add(simulationUI,
                    new GridBagConstraints(0, j++, 3, 1, 100, 75,
                    GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
        }
        if (viewer != null) {
            singleUI.add(viewer,
                    new GridBagConstraints(0, j++, 3, 1, 100, 75,
                    GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
        }
        singleUI.add(lblDate,
                new GridBagConstraints(0, j++, 3, 1, 100, 1,
                GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        singleUI.add(sldTime,
                new GridBagConstraints(0, j++, 3, 1, 100, 1,
                GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        if (colorbarParticle != null) {
            singleUI.add(colorbarParticle,
                    new GridBagConstraints(0, j++, 3, 1, 100, 1,
                    GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
        }
        singleUI.add(colorbarBathy,
                new GridBagConstraints(0, j++, 3, 1, 100, 1,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(5, 0, 5, 5), 0, 0));
    }

    /**
     * Creates a specific menu for the SINGLE mode that includes display
     * options.
     * @return the JMenu to display for the SINGLE mode.
     */
    private JMenu createMenuSingleUI() {

        JMenu menu = new JMenu(Resources.MENU_DISPLAY);
        itemBgReleaseZone = new JCheckBoxMenuItem(Resources.MENU_DISPLAY_BG_S, false);
        itemBgRecruitmentZone = new JCheckBoxMenuItem(Resources.MENU_DISPLAY_BG_R, false);
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
        menu.add(itemBgReleaseZone);
        menu.add(itemBgRecruitmentZone);
        menu.addSeparator();
        menu.add(itemDepthChart);
        menu.add(itemEdgeChart);
        menu.add(itemRecruitChart);
        menu.add(itemLengthChart);
        menu.add(itemStageChart);
        menu.add(itemDeadChart);
        menu.addSeparator();
        menu.add(itemRdBtnNoColor);
        menu.add(itemRdBtnBathy);
        menu.add(itemRdBtnZone);
        menu.add(itemRdBtnTp);
        menu.addSeparator();
        menu.add(itemPreferences);

        itemBgReleaseZone.setEnabled(false);
        itemBgRecruitmentZone.setEnabled(false);
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

        // Listeners
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
        itemBgReleaseZone.addActionListener(this);
        itemBgRecruitmentZone.addActionListener(this);

        valmin_tp = Constant.TP_MIN;
        valmax_tp = Constant.TP_MAX;
        valmin_bathy = Constant.BATHY_MIN;
        valmax_bathy = Constant.BATHY_MAX;
        itemRdBtnNoColor.doClick();

        return menu;
    }

    /**
     * Sets up the main frame for the SINGLE mode once a file of configuration
     * has been loaded or created.
     */
    private void setupSingleUI() {

        boolean bln;

        btnStart.setEnabled(true);
        viewer = null;
        simulationUI = new SimulationUI();
        simulationUI.setBorder(BorderFactory.createLineBorder(Color.black));

        sldTime.setMaximum(0);
        sldTime.setValue(0);
        sldTime.setEnabled(false);
        lblDate.setText(Resources.LBL_TIME);

        prmRefresh.setEnabled(true);
        if (Configuration.isRecord()) {
            prmRefresh.setValue(Configuration.getRecordFrequency());
        }

        ckBoxCapture.setEnabled(true);

        bln = (Configuration.getDimSimu() == Constant.SIMU_3D);
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

        bln = (Configuration.getTypeRecruitment() != Constant.NONE);
        if (!bln) {
            itemBgRecruitmentZone.setSelected(false);
            if (itemRecruitChart.isSelected()) {
                itemRecruitChart.doClick();
            }
        }
        itemRecruitChart.setEnabled(bln);
        itemBgRecruitmentZone.setEnabled(bln);

        bln = Configuration.isGrowth();
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

        bln = Configuration.isLethalTp();
        if (!bln) {
            if (itemDeadChart.isSelected()) {
                itemDeadChart.doClick();
            }
        }
        itemDeadChart.setEnabled(bln);

        bln = (Configuration.getTypeRelease() == Constant.RELEASE_ZONE);
        if (!bln) {
            if (itemBgReleaseZone.isSelected()) {
                itemBgReleaseZone.doClick();
            }
            if (itemRdBtnZone.isSelected()) {
                itemRdBtnNoColor.doClick();
            }
        }
        itemBgReleaseZone.setEnabled(bln);
        itemRdBtnZone.setEnabled(bln);

        itemPreferences.setEnabled(itemRdBtnTp.isSelected() | itemRdBtnBathy.isSelected());

        prmRefresh.setUnit("* dt (dt = " + String.valueOf(Configuration.get_dt()) + " sec)");

        setSize((int) (0.5f * windows.width), (int) (1.0f * windows.height));
        setLocation(0, 0);
    }

    /**
     * Sets up the main frame. It loads a configuration file and calls for the
     * setup SwingWorker.
     * @see inner class SetupSwingWorker
     */
    public void setUp() {

        btnStart.setEnabled(false);
        try {
            new Configuration(cfgFile);
        } catch (Exception e) {
            /** Print error message and stop the setup.*/
            printErr(e, "Error in configuration file");
            cfgFile = null;
            return;
        }

        itemEdit.setEnabled(true);
        statusbar.setMessage(Resources.MSG_SETUP);
        setupSW = new SetupSwingWorker();
        setupSW.execute();
    }

    /**
     * This method prints an error message in the console and shows an error
     * dialog box giving details about the exception.
     *
     * @param t a Throwable, the exception thrown.
     * @param errTitle a String, the title of the error dialog box.
     */
    private void printErr(Throwable t, String errTitle) {

        StackTraceElement[] stackTrace = t.getStackTrace();
        StringBuffer message = new StringBuffer(t.getClass().getSimpleName());
        message.append(" : ");
        message.append(stackTrace[0].toString());
        message.append('\n');
        message.append("  --> ");
        message.append(t.getMessage());
        System.err.println(message.toString());

        JOptionPane.showMessageDialog(null,
                errTitle + "\n" +
                t.getClass().getSimpleName() + " : " +
                t.getMessage(),
                "Error " + t.getClass().getSimpleName(),
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Creates a new {@code ReplayPanel} once the SINGLE simulation is over
     * or interrupted and updates the UI.
     */
    private void createViewver() {

        setToolbarEnabled(true);
        if (isReplayEnabled) {
            viewer = new ReplayPanel();
            sldTime.setMaximum(sldTime.getMaximum() * (1 - timeArrow) / 2 + timeArrow * sldTime.getValue());
            if (imgSnapShot != null) {
                viewer.setImage(imgSnapShot[sldTime.getMaximum()]);
            } else {
                viewer.setImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
            }
            if (timeSnapShot != null) {
                lblDate.setText(Resources.LBL_STEP + (sldTime.getMaximum() + 1) +
                        " / " + (sldTime.getMaximum() + 1) + " - " +
                        Resources.LBL_TIME +
                        timeSnapShot[sldTime.getMaximum()]);
                sldTime.setEnabled(true);
            } else {
                lblDate.setText(Resources.LBL_TIME);
            }
            statusbar.setMessage(Resources.MSG_REPLAY);
        }
        ckBoxReplay.setEnabled(true);
    }

    /**
     * Gets the path of the folders containing the configuration files.
     * It first tries to get this information from file ".ichthyop.cong"
     * recorded in the working directory. If the file does not exist it
     * returns the home directory.
     * @return a File, the path of the folder that contains the configuration
     * files.
     */
    private File getPath() {

        File file = new File(".ichthyop.conf");
        if (!file.exists() || !file.canRead()) {
            return new File("./cfg");
        }
        try {
            BufferedReader bfIn = new BufferedReader(new FileReader(file));
            String path = bfIn.readLine().trim();
            bfIn.close();
            return new File(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sets the path of the folder that contains the configuration files and
     * records the property in file ".ichthyop.conf"
     * @param path a File, the path of the folder that contains the
     * configuration files.
     */
    private void setPath(File path) {

        File file = new File(".ichthyop.conf");
        try {
            BufferedWriter bfOut = new BufferedWriter(new FileWriter(file));
            bfOut.write(path.toString());
            bfOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Invoked when an action occurs
     */
    public void actionPerformed(ActionEvent e) {

        Object source = e.getSource();

        /** Exit button */
        if (source == btnExit) {
            System.out.println("Exit");
            System.exit(0);
        }
        /** Stop button */
        if (source == btnStop) {
            flagStop = true;
            setToolbarEnabled(true);
            return;
        }
        /** Start button */
        if (source == btnStart) {
            flagStop = false;
            setToolbarEnabled(false);
            timeArrow = Configuration.getTimeArrow();
            if (Configuration.isSerial()) {
                new SerialSwingWorker().execute();
            } else {
                initSingleUI();
                updateUI();
                new SingleSwingWorker().execute();
            }
            return;
        }

        /** Capture check box*/
        if (source == ckBoxCapture) {

            if (Configuration.getDirectorOut() != null) {
                File file = new File(Configuration.getDirectorOut());
                if (file.isDirectory()) {
                    return;
                }
            }
            Throwable t = new IOException("Ouput path incorrect " +
                    Configuration.getDirectorOut());
            printErr(t, "Output error");
            ckBoxCapture.setSelected(false);
        }

        /** Replay check box */
        if (source == ckBoxReplay) {

            isReplayEnabled = ckBoxReplay.isSelected();
            if (!isReplayEnabled && !btnStart.isEnabled()) {
                ckBoxReplay.setEnabled(false);
                imgSnapShot = null;
                timeSnapShot = null;
            }
        }

        /** Exit menu item */
        if (source == itemExit) {
            btnExit.doClick();
        }
        /** Open menu item */
        if (source == itemOpen) {
            JFileChooser chooser = new JFileChooser(getPath());
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setFileFilter(new FileNameExtensionFilter(Resources.EXTENSION_CONFIG + " (*.cfg)", "cfg"));
            int returnPath = chooser.showOpenDialog(MainFrame.this);
            if (returnPath == JFileChooser.APPROVE_OPTION) {
                cfgFile = chooser.getSelectedFile();
                setUp();
                return;
            }
        }
        /** Edit menu item */
        if (source == itemEdit) {
            new ConfigurationUI(this, cfgFile).setVisible(true);
            return;
        }
        /** New menu item */
        if (source == itemNew) {
            new ConfigurationUI(this, getPath()).setVisible(true);
            return;
        }

        /** Path menu item */
        if (source == itemPath) {

            JFileChooser chooser = new JFileChooser(getPath());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnPath = chooser.showOpenDialog(MainFrame.this);
            if (returnPath == JFileChooser.APPROVE_OPTION) {
                setPath(chooser.getSelectedFile());
            }
        }

        if (source == itemBgReleaseZone || source == itemBgRecruitmentZone) {
            simulationUI.repaintBackground();
        }

        if (source == itemRdBtnNoColor) {
            displayColor = Constant.NONE;
            itemPreferences.setEnabled(false);
            return;
        }
        if (source == itemRdBtnTp) {
            valmin = valmin_tp;
            valmax = valmax_tp;
            displayColor = Constant.DISPLAY_TP;
            itemPreferences.setEnabled(true);
            return;
        }
        if (source == itemRdBtnBathy) {
            valmin = valmin_bathy;
            valmax = valmax_bathy;
            displayColor = Constant.DISPLAY_DEPTH;
            itemPreferences.setEnabled(true);
            return;
        }
        if (source == itemRdBtnZone) {
            displayColor = Constant.DISPLAY_ZONE;
            itemPreferences.setEnabled(false);
            return;
        }
        if (source == itemPreferences) {
            new PreferenceFrame(itemRdBtnBathy.isSelected(), valmin, valmax);
            return;
        }
        if (source == itemDepthChart) {
            if (itemDepthChart.isSelected()) {
                ChartManager.createChartDepth();
            } else {
                ChartManager.disposeOfChartDepth();
            }
            return;
        }
        if (source == itemEdgeChart) {
            if (itemEdgeChart.isSelected()) {
                ChartManager.createChartOut();
            } else {
                ChartManager.disposeOfChartOut();
            }
            return;
        }
        if (source == itemLengthChart) {
            if (itemLengthChart.isSelected()) {
                ChartManager.createChartLength();
            } else {
                ChartManager.disposeOfChartLength();
            }
            return;
        }
        if (source == itemRecruitChart) {
            if (itemRecruitChart.isSelected()) {
                ChartManager.createChartRecruitment();
            } else {
                ChartManager.disposeOfChartChartRecruitment();
            }
            return;
        }
        if (source == itemDeadChart) {
            if (itemDeadChart.isSelected()) {
                ChartManager.createChartMortality();
            } else {
                ChartManager.disposeOfChartMortality();
            }
            return;
        }
        if (source == itemStageChart) {
            if (itemStageChart.isSelected()) {
                ChartManager.createChartStage();
            } else {
                ChartManager.disposeOfChartStage();
            }
            return;
        }

    }

    /**
     * Invoked when the slider position has changed.
     * @param e ChangeEvent
     */
    public void stateChanged(ChangeEvent e) {

        if (flagStop && viewer != null) {
            int i_step = sldTime.getMaximum() * (1 - timeArrow) / 2 + timeArrow * sldTime.getValue();
            viewer.setImage(imgSnapShot[i_step]);
            lblDate.setText(Resources.LBL_STEP + (i_step + 1) + " / " +
                    (sldTime.getMaximum() + 1) + " - " +
                    Resources.LBL_TIME + timeSnapShot[i_step]);
            viewer.repaint();
        }
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }


//////////////////////
// Getters and setters
//////////////////////
    /**
     * Sets the appropriate configuration file.
     * @param f the File of configuration
     */
    public void setCfgFile(File f) {
        cfgFile = f;
    }

    /**
     * Determines whether the menu item "display release zones" is selected
     * @return <code>true</code> if selected, <code>false</code> otherwise.
     */
    public static boolean isBgReleaseZone() {
        return itemBgReleaseZone.isSelected();
    }

    /**
     * Determines whether the menu item "display recruitment zones" is selected
     * @return <code>true</code> if selected, <code>false</code> otherwise.
     */
    public static boolean isBgRecruitmentZone() {
        return itemBgRecruitmentZone.isSelected();
    }

    /**
     * Gets the refresh time step.
     * @return an int, the time [second] between two steps
     */
    public static int getDtDisplay() {
        return (prmRefresh == null)
                ? Configuration.get_dt()
                : prmRefresh.getValue().intValue() * Configuration.get_dt();
    }

    /**
     * Gets the minimum value of the particle color range
     * @return a float, the minimum value of the particle color range
     * (temperature or depth depending on display options).
     */
    public static float getValMin() {
        return valmin;
    }

    /**
     * Gets the maximum value of the particle color range
     * @return a float, the maximum value of the particle color range
     * (temperature or depth depending on display options).
     */
    public static float getValMax() {
        return valmax;
    }

    /**
     * Gets the application statusbar.
     * @return the StatusBar of the main frame.
     */
    public static StatusBar getStatusBar() {
        return statusbar;
    }

    /**
     * Gets the display options.
     * @return an int characterizing the display option.
     * @see ichthyop.util.Constant for details about the labels
     * characterizing the display options.
     */
    public static int getDisplayColor() {
        return displayColor;
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     *  A SwingWorker to setup the simulation.
     * A SwingWorker helps to perform lengthy GUI-interacting tasks in a
     * dedicated thread. It is typically the case of the application setup.
     * The {@code SetupSwingWorker} allows to load a new configuration file,
     * to make preliminary computations and display the map on screen, without
     * freezing the UI.
     *
     * @see ichthyop.util.SafeSwingWorker for details about the SwingWorkers.
     */
    public class SetupSwingWorker extends SafeSwingWorker {

        /**
         * Constructs a new simulation and starts to set it up.
         * @throws any Exception that occurs during the setup.
         * @see ichthyop.core.Simulation#setUp
         */
        @Override
        protected Object doInBackground() throws Exception {
            // Launch simulation setup
            simulation = new Simulation();
            simulation.setUp();
            return null;
        }

        /**
         * Invoked if an error occured while setting up the simulation.
         * It prints information about the error and waits for a new
         * configurtion file to be loaded
         * @param t the Throwable thrown during the setup.
         */
        protected void onFailure(Throwable t) {
            //Print error message and disable buttons of the main frame.
            printErr(t, "Error in setup");
            btnStart.setEnabled(false);
            btnStop.setEnabled(false);
        }

        /**
         * Invoked only if the simulation successfully set up.
         * It updates the UI and waits for the simulation to start.
         * @param result Object
         */
        protected void onSuccess(Object result) {
            //Setup the main frame if no exception
            (MainFrame.this).setVisible(false);
            if (Configuration.isSerial()) {
                singleUI = null;
                if ((MainFrame.this).getJMenuBar().getMenuCount() > 1) {
                    (MainFrame.this).getJMenuBar().remove(menuSingleUI);
                }
                itemEdit.setEnabled(false);
                serialUI = buildSerialUI();
            } else {
                serialUI = null;
                if (menuSingleUI == null) {
                    menuSingleUI = createMenuSingleUI();
                }
                if ((MainFrame.this).getJMenuBar().getMenuCount() == 1) {
                    (MainFrame.this).getJMenuBar().add(menuSingleUI);
                }
                if (singleUI == null) {
                    singleUI = buildSingleUI();
                }
                setupSingleUI();
                updateSingleUI();
            }
            if (btnStart.isEnabled()) {
                statusbar.setMessage(Resources.MSG_READY);
            }
            updateUI();
            (MainFrame.this).setVisible(true);
        }

        //----------- End of inner class SetupSwingWorker
    }


    ////////////////////////////////////////////////////////////////////////////
    /**
     * A SwingWorker to control the run of the simulation in SINGLE mode.
     * A SwingWorker helps to perform lengthy GUI-interacting tasks in a
     * dedicated thread. It is typically the case of the SINGLE mode,
     * in which heavy numerical computations must go on besides a display
     * of particle's trajectories on screen.
     * On a hand, the SwingWorker controls the ongoing processes of the core
     * classes of the Individual-based-model
     * (Simulation / Population / Particle). On the other hand it refreshes
     * the graphical part of the simulation with intermediate results
     * provided by the numerical core.
     */
    private class SingleSwingWorker extends SafeSwingWorker<Object, Step> {

        /**
         * Date and time of the current step
         */
        String strTime;
        /**
         * Refresh time step [second]
         */
        private int dt_refresh;
        /**
         * Index of the current step
         */
        private int i_step;
        /**
         * Total number of steps of the run
         */
        private int nb_steps;
        /**
         * The {@code Step} object holding information about the current
         * step of the run.
         */
        private Step step;

        /**
         * This method is the backbone of the SINGLE mode. It controls the
         * march of the simulation through time, thanks to the {@code Step}
         * object. It calls the <code>Simulation.step</code> methods every
         * time step, controls the ouput writer and the chart manager.
         * @throws any Exception that occurs while running the simulation.
         * The method sends a copy of the {@code Step} object to the
         * {@link #process} method every time the display has to be refresh.
         *
         * @see ichthyop.util.SafeSwingWorker for details about the SwingWorkers.
         */
        @Override
        protected Object doInBackground() throws Exception {

            /** Waits for setup to end up */
            try {
                setupSW.get();
            } catch (Exception e) {
                this.cancel(true);
            }

            /** Initializes */
            init();

            /** Starts the run */
            do {
                simulation.iniStep(step.getTime());
                if (Configuration.isRecordNc() && step.hasToRecord()) {
                    OutputNC.write(step.getTime());
                }
                if (step.hasToRefresh()) {
                    System.out.println("-----< " + step.timeToString() +
                            " >-----");
                    step.setData(simulation.getPopulation());
                    ChartManager.counting();
                }
                publish(step.clone());
                simulation.step(step.getTime());
            } while (!flagStop && step.next());

            /** writes last record on file and closes */
            if (Configuration.isRecordNc()) {
                OutputNC.write(step.getTime());
                OutputNC.close();
            }
            return null;
        }

        /**
         * * Invoked only when the simulation ends or when interrupted by the
         * user. It creates a new {@link ReplayPanel} for a fast replay of
         * the run.
         */
        protected void onSuccess(Object result) {
            if (flagStop) {
                System.out.println("Simulation interrupted (by user)");
            } else {
                System.out.println("End of simulation");
                flagStop = true;
            }
            //simulationUI.setPopulation(null);
            simulationUI.setStep(null);
            simulationUI = null;
            createViewver();
            updateSingleUI();
        }

        /**
         * Invoked if an error occured while running the simulation.
         * It prints information about the exception and attempts to create a
         * new {@link ReplayPanel} to allow the user to replay the part of the
         * run that is antecedent to the error.
         *
         * @param t Throwable
         */
        protected void onFailure(Throwable t) {
            printErr(t, "Error while running the simulation");
            System.out.println("Simulation interrupted (error)");
            flagStop = true;
            //simulationUI.setPopulation(null);
            simulationUI.setStep(null);
            simulationUI = null;
            createViewver();
            updateSingleUI();
            ;
        }

        /**
         * Receives intermediate results from the {@code publish} method
         * asynchronously on the <i>Event Dispatch Thread</i>. The method
         * refreshed the display of the simulation thanks to the information
         * held by the {@code Step}s objects.
         * @param steps List of the {@code Step} objects sent by the
         * {@link #doInBackground} method through the {@code publish} method.
         * @see ichthyop.core.Step for details about the information contained
         * in a {@code Step}.
         */
        @Override
        protected void process(java.util.List<Step> steps) {

            //Updates the UI
            for (Step step : steps) {
                strTime = step.timeToString();
                statusbar.setMessage(Resources.MSG_COMPUTE + strTime);
                if (!flagStop && step.hasToRefresh()) {
                    simulationUI.setStep(step);
                    statusbar.setMessage(Resources.MSG_REFRESH);
                    sldTime.setValue((nb_steps - 1) * (1 - timeArrow) / 2 + timeArrow * i_step);
                    if (isReplayEnabled) {
                        imgSnapShot[i_step] = getImage(simulationUI);
                        timeSnapShot[i_step] = strTime;
                    }
                    i_step++;
                    lblDate.setText(Resources.LBL_STEP + i_step + " / " +
                            nb_steps + " - " + Resources.LBL_TIME + strTime);
                    simulationUI.repaint();
                    ChartManager.refresh(strTime);
                    if (ckBoxCapture.isSelected()) {
                        try {
                            screen2File(simulationUI, step.getCalendar());
                        } catch (IOException e) {
                            printErr(e, "Capture error");
                        }
                    }
                }
                if (timeArrow * (step.getTime() + step.get_dt()) >= timeArrow * Dataset.getTimeTp1()) {
                    statusbar.setMessage(Resources.MSG_GET_FIELD);
                }
            }
        }

        /**
         * Initializes the simulation and the UI before starting the run.
         * @throws any Exception that occurs while initializing the simulation.
         */
        private void init() throws Exception {

            System.out.println("Initializing");
            statusbar.setMessage(Resources.MSG_INIT);
            simulation.init();
            i_step = 0;
            dt_refresh = Math.max((prmRefresh.getValue().intValue() * Configuration.get_dt()),
                    Configuration.get_dt());
            step = new Step(timeArrow, dt_refresh);
            nb_steps = (int) (step.getSimulationDuration() / dt_refresh);
            sldTime.setMinimum(0);
            sldTime.setMaximum(nb_steps - 1);
            sldTime.setValue(sldTime.getMaximum() * (1 - timeArrow) / 2);
            if (isReplayEnabled) {
                imgSnapShot = new BufferedImage[nb_steps];
                timeSnapShot = new String[nb_steps];
            }
            step.setData(simulation.getPopulation());
            simulationUI.setStep(step);
            ChartManager.setPopulation(simulation.getPopulation());

            if (Configuration.isRecordNc()) {
                OutputNC.create(0, 1, Constant.SINGLE);
                OutputNC.init(simulation.getPopulation());
            }

            if (Configuration.isMigration()) {
                DVMPattern.setCalendar((Calendar) step.getCalendar().clone());
            }
        }

        /**
         * Saves the snapshot of the specified component as a PNG picture.
         * The name of the picture includes the current time of the simulation.
         * @param cpnt the Component to save as a PNG picture.
         * @param cld the Calendar of the current {@code Step} object.
         * @throws an IOException if an ouput exception occurs when saving the
         * picture.
         */
        private void screen2File(Component component, Calendar calendar) throws
                IOException {

            SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
            dtFormat.setCalendar(calendar);
            String fileName = Configuration.getDirectorOut() +
                    "img_" + dtFormat.format(calendar.getTime()) + ".png";
            System.out.println(fileName);

            try {
                BufferedImage bi = new BufferedImage(component.getWidth(),
                        component.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics g = bi.getGraphics();
                component.paintAll(g);
                ImageIO.write(bi, "PNG", new File(fileName));
            } catch (Exception ex) {
                throw new IOException("Problem saving picture " + fileName);
            }
        }

        /**
         * Creates a {@code BufferedImage} of the specified component.
         * @param component the JComponent to paint in the {@code BufferedImage}
         * @return a BufferedImage, snapshot of the component.
         */
        private BufferedImage getImage(JComponent component) {
            if (component == null) {
                return null;
            }
            int width = component.getWidth();
            int height = component.getHeight();
            BufferedImage image = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            component.paintAll(g2);
            g2.dispose();
            return image;
        }

        //----------- End of inner class SingleSwingWorker
    }


    ////////////////////////////////////////////////////////////////////////////
    /**
     * A SwingWorker to control the runs of the simulation in SERIAL mode.
     * A SwingWorker helps to perform lengthy GUI-interacting tasks in a
     * dedicated thread. Contrarily to SINGLE mode, the SERIAL mode does not
     * refresh particle's trajectories on screen, in order to improve the
     * efficiency of the numerical core. The UI is limited to the minimum. Two
     * progress bars inform the user about the progress of the current run and
     * the progress of the whole runs. Intermediate results provided by the
     * numerical core are recorded in a NetCDF output file through the
     * output writer {@link ichthyop.io.OutputNC}
     *
     * @see ichthyop.util.SafeSwingWorker for details about the SwingWorkers.
     */
    public class SerialSwingWorker extends SafeSwingWorker<Object, Step> {

        /**
         * Text displayed in the simulation progress bar
         */
        private StringBuffer strBfSimu = new StringBuffer();
        /**
         * Text displayed above the current run progress bar
         */
        private StringBuffer strBfRun = new StringBuffer();
        /**
         * The {@code Step} object holding information about the current
         * step of the run.
         */
        private Step step;

        /**
         * This method is the backbone of the SERIAL mode. It controls the
         * march of the simulation through time, thanks to the {@code Step}
         * object. For each run, the method initializes the simulation, creates
         * a new output file and controls the march of the run through time.
         * The {@code Step} object detects the end of the current run and inform
         * the method a new run (with the following set of parameter) should be
         * launched.
         * Every time step, the method sends progress information about the
         * current run and the simulation to the <i>Event Dispatch Thread</i>
         * through the {@code publish} method.
         * @throws any Exception that occurs while running the simulation.
         */
        protected Object doInBackground() throws Exception {

            /** Waits for setup to end up */
            try {
                setupSW.get();
            } catch (Exception e) {
                this.cancel(true);
            }

            step = new Step(1, 0);
            /** Starts SERIAL simulation */
            do {
                /** Initializes each new run */
                init();
                /** Starts the current run */
                do {
                    simulation.iniStep(step.getTime());
                    if (step.hasToRecord()) {
                        OutputNC.write(step.getTime());
                    }
                    publish(step.clone());
                    simulation.step(step.getTime());
                } while (!flagStop && step.next()); // loop for current run
                /** Writes last record and closes ouput file */
                OutputNC.write(step.getTime());
                OutputNC.close();
            } while (!flagStop && step.nextSimulation());

            return null;
        }

        /**
         * Receives intermediate results from the {@code publish} method
         * asynchronously on the <i>Event Dispatch Thread</i>. The method
         * refreshed the progress bars thanks to the information
         * held in the {@code Step}s objects.
         * @param steps List of the {@code Step} objects sent by the
         * {@link #doInBackground} method through the {@code publish} method.
         * @see ichthyop.core.Step for details about the information contained
         * in a {@code Step}.
         */
        @Override
        protected void process(java.util.List<Step> steps) {
            for (Step step : steps) {
                strBfSimu.delete(0, strBfSimu.length());
                strBfRun.delete(0, strBfRun.length());
                barSimulation.setValue(step.progressCurrent());
                strBfSimu.append("Current simulation - ");
                strBfSimu.append(Resources.LBL_STEP);
                strBfSimu.append(step.index() + 1);
                strBfSimu.append(" / ");
                strBfSimu.append(step.getNumberOfSteps());
                strBfSimu.append(" - ");
                strBfSimu.append(Resources.LBL_TIME);
                strBfSimu.append(step.timeToString());
                lblDate.setText(strBfSimu.toString());
                barRun.setValue(step.progressGlobal());
                strBfRun.append("Simulation ");
                strBfRun.append(step.indexSimulation() + 1);
                strBfRun.append(" / ");
                strBfRun.append(step.getNumberOfSimulations());
                strBfRun.append(" - ");
                strBfRun.append(step.timeLeft());
                lblSimulation.setText(strBfRun.toString());
                statusbar.setMessage("running...");
                if (step.hasToRecord()) {
                    statusbar.setMessage("records on file...");
                }
                if (timeArrow * (step.getTime() + step.get_dt()) >= timeArrow * Dataset.getTimeTp1()) {
                    statusbar.setMessage(Resources.MSG_GET_FIELD);
                }
            }
        }

        /**
         * Invoked when the simulation ends or when it is interrupted by the
         * user.
         */
        protected void onSuccess(Object result) {
            if (flagStop) {
                System.out.println("Simulation interrupted (by user)");
                statusbar.setMessage("interrupted (by user)");
            } else {
                System.out.println("End of simulation");
                statusbar.setMessage("job done.");
                btnStop.doClick();
            }

            btnStop.doClick();
        }

        /**
         * Invoked if an error occurs while running the simulation. It prints
         * information about the error.
         * @param t Throwable
         */
        protected void onFailure(Throwable t) {
            printErr(t, "Error while running the simulation");
            btnStop.doClick();
            statusbar.setMessage("interrupted (error).");
        }

        /**
         * Initiliazes the current run and creates a new netcdf output file
         * @see ichthyop.core.Simulation#init
         */
        private void init() throws Exception {

            System.out.println("\n############## New simulation  #############");
            System.out.println("Initializing");
            statusbar.setMessage(Resources.MSG_INIT);
            simulation.init();
            simulation.printParameters(step);
            OutputNC.create(step.indexSimulation(), step.getNumberOfSimulations(),
                    Constant.SERIAL);
            OutputNC.init(simulation.getPopulation());

            if (Configuration.isMigration()) {
                DVMPattern.setCalendar((Calendar) step.getCalendar().clone());
            }
        }

        //----------- End of inner class SerialSwingWorker
    }


    ////////////////////////////////////////////////////////////////////////////
    /**
     * A frame to set up extreme values, depth or temperature
     * depending on the display options, for the color range.
     */
    public class PreferenceFrame extends JFrame implements ActionListener {

        private String lblMin,  lblMax;
        private String strUnit;
        private FloatParameter prmMin,  prmMax;
        private JButton btnOk,  btnCancel;
        /**
         * <code>true</code> to set up the range of particle depth, false to
         * set up the range of sea water temperature at particle location.
         */
        private boolean isBathy;

        ///////////////
        // Constructors
        ///////////////
        /**
         * Constructs a new preference frame for the specified range (depth or
         * temperature).
         * @param isBathy <code>true</code> to set up the range of particle depth, false to
         * set up the range of sea water temperature at particle location.
         * @param valmin a float, the current minimum value for the range of the
         * particle color bar
         * @param valmax a float, the current maximum value for the range of the
         * particle color bar
         */
        PreferenceFrame(boolean isBathy, float valmin, float valmax) {

            this.isBathy = isBathy;
            lblMin = isBathy ? Resources.LBL_MIN_DEPTH : Resources.LBL_MIN_TP;
            lblMax = isBathy ? Resources.LBL_MAX_DEPTH : Resources.LBL_MAX_TP;
            strUnit = isBathy ? Resources.UNIT_METER : Resources.UNIT_CELSIUS;
            prmMin = new FloatParameter(lblMin, valmin, strUnit, true);
            prmMin.setFormatPolicy(1, 4, 1, 1);
            prmMax = new FloatParameter(lblMax, valmax, strUnit, true);
            prmMax.setFormatPolicy(1, 4, 1, 1);
            btnOk = new JButton(Resources.BTN_OK);
            btnCancel = new JButton(Resources.BTN_CANCEL);

            createUI();
            this.validate();
            this.setVisible(true);
        }

        ////////////////////////////
        // Definition of the methods
        ////////////////////////////
        /**
         * Creates the UI of the frame
         */
        private void createUI() {

            this.getContentPane().setLayout(new GridBagLayout());
            this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            GraphicsDevice ecran = GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice();
            GraphicsConfiguration config = ecran.getDefaultConfiguration();
            DisplayMode mode = config.getDevice().getDisplayMode();
            Dimension size = new Dimension(mode.getWidth(), mode.getHeight());
            this.setSize((int) (0.3f * size.width),
                    (int) (0.2f * size.height));

            this.setTitle(Resources.TITLE_PREFERENCES);
            //this.setLocation(0, 0);
            this.setLocationRelativeTo(this.getParent());

            this.getContentPane().add(new JLabel(Resources.LBL_COLORBAR),
                    new GridBagConstraints(0, 0, 1, 1, 1.0,
                    1.0, GridBagConstraints.WEST,
                    GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.getContentPane().add(prmMin.createUI(),
                    new GridBagConstraints(0, 1, 2, 1, 1.0,
                    1.0, GridBagConstraints.WEST,
                    GridBagConstraints.NONE,
                    new Insets(5, 25, 5, 5), 0, 0));
            this.getContentPane().add(prmMax.createUI(),
                    new GridBagConstraints(0, 2, 2, 1, 1.0,
                    1.0, GridBagConstraints.WEST,
                    GridBagConstraints.NONE,
                    new Insets(5, 25, 5, 5), 0, 0));
            this.getContentPane().add(btnOk,
                    new GridBagConstraints(0, 3, 1, 1, 1.0,
                    1.0, GridBagConstraints.EAST,
                    GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.getContentPane().add(btnCancel,
                    new GridBagConstraints(1, 3, 1, 1, 1.0,
                    1.0, GridBagConstraints.EAST,
                    GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 5), 0, 0));

            btnOk.addActionListener(this);
            btnCancel.addActionListener(this);
        }

        /**
         * Invoked when an action occurs
         * Validates or cancels the new value set by the user.
         */
        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == btnOk) {
                if (isBathy) {
                    valmin_bathy = prmMin.getValue().floatValue();
                    valmax_bathy = prmMax.getValue().floatValue();
                    itemRdBtnBathy.doClick();
                } else {
                    valmin_tp = prmMin.getValue().floatValue();
                    valmax_tp = prmMax.getValue().floatValue();
                    itemRdBtnTp.doClick();
                }

                this.dispose();
            }
            if (e.getSource() == btnCancel) {
                this.dispose();
            }
        }

        //---------- Enf of inner class Preference frame
    }

    //---------- End of class
}
