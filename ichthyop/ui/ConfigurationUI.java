package ichthyop.ui;

/** import AWT */
import ichthyop.io.Configuration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Component;

/** java.io */
import java.io.File;
import java.io.IOException;

/** import java.net */
import java.net.URI;

/** import java.text */
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/** import java.util */
import java.util.Calendar;
import java.util.Locale;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;

/** import Swing */
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JFormattedTextField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.ButtonGroup;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;
import javax.swing.DefaultCellEditor;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.WindowConstants;

/** local import */
import ichthyop.ui.param.DateParameter;
import ichthyop.ui.param.DurationParameter;
import ichthyop.ui.param.FloatParameter;
import ichthyop.ui.param.IntegerParameter;
import ichthyop.ui.param.ValueChangedEvent;
import ichthyop.ui.param.ValueListener;
import ichthyop.util.Resources;
import ichthyop.util.Structure;
import ichthyop.util.Constant;
import ichthyop.util.INIFile;
import ichthyop.util.MetaFilenameFilter;
import ichthyop.util.calendar.Calendar1900;
import ichthyop.util.calendar.ClimatoCalendar;
import ichthyop.util.NCField;
import ichthyop.util.NCComparator;

/** import netcdf */
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/** import java.net */
import java.net.URL;
import java.net.HttpURLConnection;


/**
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */

public class ConfigurationUI extends JFrame implements ActionListener,
        MouseListener, ValueListener {

//////////////////////////////////
// Declaration of the UI variables
//////////////////////////////////

    private JTabbedPane tabbedPane;

    private JButton btnSave, btnSaveas, btnExit;

    private StatusBar statusBar;
    
    private LogBar logBar;

    private TabIO tabIO;

    private TabModel tabModel;

    private TabTime tabTime;

    private TabTransport tabTransport;

    private TabRelease tabRelease;

    private TabRecruitment tabRecruitment;

    private TabBiology tabBiology;

    private TabVariable tabVariable;

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    private MainFrame mainFrame;

    private int model = Constant.ROMS;

    private int dimension = Constant.SIMU_3D;

    private File file;

    private File path;

    String statusBarMsg;


///////////////
// Constructors
///////////////

    /**
     *
     */
    public ConfigurationUI() {

        this(null);
    }

    /**
     *
     * @param mainFrame MainFrame
     */
    public ConfigurationUI(MainFrame mainFrame) {

        this(mainFrame, null);
    }

    /**
     *
     * @param mainFrame MainFrame
     * @param file File
     */
    public ConfigurationUI(MainFrame mainFrame, File file) {

        super(Resources.TITLE_SHORT + Resources.TITLE_CONFIG_EDITOR);
        this.mainFrame = mainFrame;

        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                this.path = file;
            } else if (file.isFile()) {
                this.file = file;
            }
        }
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().add(createUI());
        addListeners();
        setToolTips();
        read();
        pack();
        setLocationRelativeTo(null);
    }


////////////////////////////
// Definition of the methods
////////////////////////////

    private void read() {

        boolean err = false;

        if (file != null) {
            statusBar.setMessage(statusBarMsg = "Loads file " + file.toString());
            logBar.setMessage(statusBarMsg);
            INIFile cfgIn = new INIFile(file.toString());
            int n = tabbedPane.getTabCount();
            ITab tab;
            for (int i = 0; i < n; i++) {
                tab = (ITab) tabbedPane.getComponentAt(i);
                try {
                    tab.read(cfgIn);
                } catch (Exception e) {
                    e.printStackTrace();
                    Throwable t = new IOException("Error in section " +
                                                  tab.getName(),
                                                  e.getCause());
                    printErr(t,
                             "Configuration editor - Loading configuration file");
                    err = true;
                }
            }
            tabTime.refresh();
            tabModel.refresh();
            String message = err
                             ? "File " + file.getName() + " partially loaded"
                             :
                             "File " + file.getName() + " successfully loaded";
            logBar.setMessage(message);
            btnSave.setEnabled(false);
        }
    }

    /**
     *
     * @param file INIFile
     * @return boolean
     */
    private boolean write(INIFile file) {

        /** sets as a SINGLE run */
        file.addSection(Structure.SECTION_SIMULATION,
                        Structure.MAN_SECTION_SIMULATION);
        file.setIntegerProperty(Structure.SECTION_SIMULATION,
                                Structure.RUN,
                                Constant.SINGLE,
                                Structure.MAN_RUN);
        file.setStringProperty(Structure.SECTION_SIMULATION,
                               Structure.NB_REPLICA,
                               "null",
                               null);
        file.setIntegerProperty(Structure.SECTION_SIMULATION,
                Structure.VERSION,
                Configuration.VERSION,
                null);

        /** call the write method for each tab */
        int n = tabbedPane.getTabCount();
        ITab tab;
        for (int i = 0; i < n; i++) {
            tab = (ITab) tabbedPane.getComponentAt(i);
            try {
                tab.write(file);
            } catch (Exception e) {
                Throwable t = new IOException("Error in section " +
                                              tab.getName() + ". " +
                                              e.getMessage());
                printErr(t,
                         "Configuration editor - Writing configuration file");
                return false;
            }
        }
        return file.save();
    }

    /**
     *
     * @return JComponent
     */
    private JComponent createUI() {

        JPanel panel = new JPanel(new GridBagLayout());

        /** TabbedPane */
        tabbedPane = new JTabbedPane();
        tabIO = new TabIO();
        tabModel = new TabModel();
        tabTransport = new TabTransport();
        tabTime = new TabTime();
        tabRelease = new TabRelease();
        tabRecruitment = new TabRecruitment();
        tabBiology = new TabBiology();
        tabVariable = new TabVariable();
        tabbedPane.addTab(Resources.TAB_IO, tabIO);
        tabbedPane.addTab(Resources.TAB_MODEL, tabModel);
        tabbedPane.addTab(Resources.TAB_VARIABLE, tabVariable);
        tabbedPane.addTab(Resources.TAB_TIME, tabTime);
        tabbedPane.addTab(Resources.TAB_BIOLOGY, tabBiology);
        tabbedPane.addTab(Resources.TAB_TRANSPORT, tabTransport);
        tabbedPane.addTab(Resources.TAB_RELEASE, tabRelease);
        tabbedPane.addTab(Resources.TAB_RECRUITMENT, tabRecruitment);

        /** Navigation Buttons */
        JPanel pnlBtn = new JPanel();
        btnSave = new JButton(Resources.BTN_SAVE);
        btnSave.setEnabled(false);
        btnSaveas = new JButton(Resources.BTN_SAVEAS);
        btnExit = new JButton("Apply & Exit");
        btnExit.setEnabled(false);
        pnlBtn.add(btnSave);
        pnlBtn.add(btnSaveas);
        pnlBtn.add(btnExit);

        /** Status bar */
        statusBar = new StatusBar("");

        /** Log bar */
        logBar = new LogBar();

        panel.add(tabbedPane, new GridBagConstraints(0, 0, 2, 1, 100, 90,
                GridBagConstraints.NORTH,
                GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        panel.add(pnlBtn, new GridBagConstraints(1, 1, 1, 1, 100, 10,
                                                 GridBagConstraints.EAST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(5, 5, 5, 5), 0, 0));
        panel.add(statusBar, new GridBagConstraints(0, 1, 2, 1, 100, 10,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));

        panel.add(logBar.createUI(), new GridBagConstraints(0, 2, 2, 1, 100, 30,
                GridBagConstraints.WEST,
                GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));

        return panel;
    }

    private void addListeners() {

        btnSave.addActionListener(this);
        btnSave.addMouseListener(this);

        btnSaveas.addActionListener(this);
        btnSaveas.addMouseListener(this);

        btnExit.addActionListener(this);
        btnExit.addMouseListener(this);
    }

    private void setToolTips() {

        btnSave.setToolTipText(Resources.TIP_BTN_SAVE);
        btnSaveas.setToolTipText(Resources.TIP_BTN_SAVEAS);
        btnExit.setToolTipText(Resources.TIP_BTN_EXIT);

        int n = tabbedPane.getTabCount();
        ITab tab;
        for (int i = 0; i < n; i++) {
            tab = (ITab) tabbedPane.getComponentAt(i);
            tab.setToolTips();
        }
    }

    /**
     *
     * @param t Throwable
     * @param errTitle String
     */
    private void printErr(Throwable t, String errTitle) {

        //t.printStackTrace();
        StackTraceElement[] stackTrace = t.getStackTrace();
        StringBuffer message = new StringBuffer(errTitle);
        message.append('\n');
        message.append(t.getClass().getSimpleName());
        message.append(" : ");
        message.append(stackTrace[0].toString());
        message.append('\n');
        message.append("  --> ");
        message.append(t.getMessage());
        
        logBar.setMessage(message.toString());

    }

    /**
     *
     * @param f File
     * @param extension String
     * @return File
     */
    private File addExtension(File f, String extension) {

        if (!f.isDirectory() && f.getName().endsWith("." + extension)) {
            return f;
        }
        return new File(f.toString() + "." + extension);
    }

    public void valueChanged(ValueChangedEvent e) {
        btnSave.setEnabled(true);
        btnExit.setEnabled(true);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {

        statusBarMsg = statusBar.getMessage();
        statusBar.setMessage(((JComponent) e.getComponent()).getToolTipText());
    }

    public void mouseExited(MouseEvent e) {

        statusBar.setMessage("");
    }

    public void actionPerformed(ActionEvent e) {

        logBar.setMessage("");

        Object source = e.getSource();

        /** btnSave */
        if (source == btnSave) {
            if (file == null) {
                JFileChooser fc = new JFileChooser(path);
                fc.setDialogType(JFileChooser.SAVE_DIALOG);
                fc.setAcceptAllFileFilterUsed(false);
                fc.setFileFilter(new FileNameExtensionFilter(Resources.
                        EXTENSION_CONFIG
                        + " (*.cfg)", "cfg"));
                int returnVal = fc.showSaveDialog(ConfigurationUI.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = addExtension(fc.getSelectedFile(), "cfg");
                } else {
                    return;
                }
            }
            file.delete();
            if (write(new INIFile(file.toString()))) {
                setTitle(Resources.TITLE_SHORT + file.toString());
                btnSave.setEnabled(false);
                logBar.setMessage(statusBarMsg = "Saved " + file.toString());
            } else {
                logBar.setMessage(statusBarMsg = "Could not save " +
                        file.toString());
            }
        }

        /** btnSaveAs */
        if (source == btnSaveas) {
            JFileChooser fc = new JFileChooser(path);
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileFilter(new FileNameExtensionFilter(Resources.
                    EXTENSION_CONFIG + " (*.cfg)", "cfg"));
            fc.setSelectedFile(file);
            int returnVal = fc.showSaveDialog(ConfigurationUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = addExtension(fc.getSelectedFile(), "cfg");
                if (write(new INIFile(file.toString()))) {
                    setTitle(Resources.TITLE_SHORT + file.toString());
                    btnSave.setEnabled(false);
                    logBar.setMessage(statusBarMsg = "Saved " +
                            file.toString());
                } else {
                    logBar.setMessage(statusBarMsg = "Could not save " +
                            file.toString());
                }
            }
        }

        /** btnExit */
        if (source == btnExit) {
            if (btnSave.isEnabled()) {
                int answer = JOptionPane.showConfirmDialog(this,
                        "Do you wish to save changes ?");
                switch (answer) {
                case JOptionPane.YES_OPTION:
                    btnSave.doClick();
                    break;
                case JOptionPane.NO_OPTION:
                    break;
                case JOptionPane.CANCEL_OPTION:
                case JOptionPane.CLOSED_OPTION:
                    return;
                }
            }
            if (file != null) {
                mainFrame.setTitle(Resources.TITLE_SHORT + file.toString());
                mainFrame.setCfgFile(file);
                mainFrame.setUp();
            }
            this.dispose();
        }
    }

////////////////////////////////////////////////////////////////////////////////
// Tab IO
////////////////////////////////////////////////////////////////////////////////
    private class TabIO extends JPanel implements ITab,
            KeyListener {

        ///////////////////////////////
        // Declaration of the variables
        ///////////////////////////////

        private JPanel pnlInput, pnlOutput;

        private JTextField txtInputpath, txtOutputpath, txtFilenameFilter,
                txtDrifterPathname, txtOutputFilename;

        private JCheckBox ckBoxOutput;

        private JButton btnInputpath, btnOutputpath, btnDrifterPathname;

        private IntegerParameter prmRecordDt;

        private final URI uriCurrent = new File("").toURI();

        private ArrayList<String> listInputFiles;

        private final String DEFAULT_FILTER = "*.nc*";

        ///////////////
        // Constructors
        ///////////////

        TabIO() {

            super(new GridBagLayout());
            setName(Resources.TAB_IO);
            createUI();
            addListeners();
        }

        ////////////////////////////
        // Definition of the methods
        ////////////////////////////

        public void read(INIFile file) {

            /** input path */
            String path = file.getStringProperty(Structure.SECTION_IO,
                                                 Structure.INPUT_PATH);
            txtInputpath.setText(URI.create(path).toString());

            txtFilenameFilter.setText(file.getStringProperty(
                    Structure.SECTION_IO,
                    Structure.FILTER));

            /** drifter initial coordinate file */
            path = file.getStringProperty(Structure.SECTION_IO,
                                          Structure.DRIFTER).trim();
            if (!path.matches("null")) {
                txtDrifterPathname.setText(uriCurrent.relativize(new File(path).
                        toURI()).getPath());
            }

            /** output path */
            path = file.getStringProperty(Structure.SECTION_IO,
                                          Structure.OUTPUT_PATH);
            if (!path.matches("null")) {
                txtOutputpath.setText(uriCurrent.relativize(new File(path).
                        toURI()).
                                      getPath());
            }

            path = null;

            /** record tracks */
            boolean bln = file.getBooleanProperty(Structure.SECTION_IO,
                                                  Structure.RECORD);
            if (bln) {
                ckBoxOutput.doClick();
                prmRecordDt.setValue(file.getIntegerProperty(
                        Structure.SECTION_IO,
                        Structure.RECORD_DT));
                txtOutputFilename.setText(
                        file.getStringProperty(Structure.SECTION_IO,
                                                Structure.OUTPUT_FILENAME));
            }

            /** Refreshed information */
            getInputFiles(txtInputpath.getText(), txtFilenameFilter.getText());
        }

        /**
         *
         * @param file INIFile
         */
        public void write(INIFile file) {

            /** create section IO */
            file.addSection(Structure.SECTION_IO, Structure.MAN_SECTION_IO);

            /** input path */
            file.setStringProperty(Structure.SECTION_IO,
                                   Structure.INPUT_PATH,
                                   txtInputpath.getText(),
                                   null);

            /** filename filter */
            file.setStringProperty(Structure.SECTION_IO,
                                   Structure.FILTER,
                                   txtFilenameFilter.getText(),
                                   null);

            /** drifter pathname */
            String pathname = "null";
            if (tabRelease.rdBtnFile.isSelected()
                && !txtDrifterPathname.getText().trim().isEmpty()) {
                pathname = txtDrifterPathname.getText();
            }
            file.setStringProperty(Structure.SECTION_IO,
                                   Structure.DRIFTER,
                                   pathname,
                                   null);

            /** output path */
            pathname = txtOutputpath.getText().trim().isEmpty()
                       ? "null"
                       : txtOutputpath.getText();
            file.setStringProperty(Structure.SECTION_IO,
                                   Structure.OUTPUT_PATH,
                                   pathname,
                                   null);

            /** record output */
            file.setBooleanProperty(Structure.SECTION_IO,
                                    Structure.RECORD,
                                    ckBoxOutput.isSelected(),
                                    null);

            /** output filename */
            pathname = txtOutputFilename.getText().trim().isEmpty()
                       ? Constant.OUTPUT_FILENAME_SINGLE
                       : txtOutputFilename.getText().trim();
            file.setStringProperty(Structure.SECTION_IO,
                                   Structure.OUTPUT_FILENAME,
                                   pathname,
                                   null);

            /** record frequency */
            prmRecordDt.write(file,
                              Structure.SECTION_IO,
                              Structure.RECORD_DT);
        }

        /**
         *
         * @param path String
         * @param fileMask String
         * @throws IOException
         */
        private void getInputFiles(String rawPath, String fileMask) {

            String path = URI.create(rawPath).getPath();
            try {
                if (isDODS(rawPath)) {
                    listInputFiles = new ArrayList<String>(1);
                    listInputFiles.add(rawPath);
                } else if (isDirectory(path)) {
                    listInputFiles = getInputList(path, fileMask);
                }
            } catch (Exception e) {
                listInputFiles = null;
                printErr(e,
                         "Configuration editor - IO tab");
            }
        }

        /**
         *
         * @param path String
         * @return ArrayList
         * @throws IOException
         */
        private ArrayList<String> getInputList(String path, String fileMask) throws
                IOException {

            ArrayList<String> list = null;

            File inputPath = new File(path);
            File[] listFile = inputPath.listFiles(new MetaFilenameFilter(
                    fileMask));
            if (listFile.length == 0) {
                throw new IOException(path +
                                      " contains no file matching mask "
                                      + fileMask);
            }
            list = new ArrayList<String>(listFile.length);
            for (File file : listFile) {
                list.add(file.toString());
            }
            if (list.size() > 1) {
                Collections.sort(list,
                                 new NCComparator(NCField.time.getName(
                                         model)));
            }
            return list;
        }


        /**
         * check for existence before essence
         */
        private boolean isDODS(String location) throws IOException {

            if (location.startsWith("dods:") | location.startsWith("http:")) {
                try {
                    URL u = new URL(location + ".dds");
                    HttpURLConnection conn = (HttpURLConnection) u.
                                             openConnection();
                    conn.setRequestMethod("HEAD");
                    int code = conn.getResponseCode();

                    return (code == 200);

                } catch (Exception e) {
                    throw new IOException(location +
                                          " is not a valid OPeNDAP URL."
                                          + e.getMessage());
                }
            }
            return false;
        }

        /**
         *
         * @param location String
         * @return boolean
         * @throws IOException
         */
        private boolean isDirectory(String location) throws IOException {

            File f = new File(location);
            if (!f.isDirectory()) {
                throw new IOException(location +
                                      " is not a valid directory.");
            }
            return f.isDirectory();
        }

        /**
         *
         */
        public void createUI() {

            /** input */
            pnlInput = new JPanel(new GridBagLayout());
            pnlInput.setBorder(BorderFactory.createEtchedBorder(
                    EtchedBorder.
                    RAISED));
            txtInputpath = new JTextField();
            txtFilenameFilter = new JTextField("*.nc*");
            txtDrifterPathname = new JTextField();
            txtDrifterPathname.setEditable(false);
            txtDrifterPathname.setEnabled(false);
            btnInputpath = new JButton("...");
            btnDrifterPathname = new JButton("...");
            btnDrifterPathname.setEnabled(false);

            pnlInput.add(new JLabel(Resources.LBL_INPUT_PATH),
                         new GridBagConstraints(0, 0, 1, 1, 10, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.BOTH,
                                                new Insets(2, 5, 2, 5), 0, 0));
            pnlInput.add(txtInputpath,
                         new GridBagConstraints(1, 0, 1, 1, 80, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(2, 5, 2, 5), 0, 0));
            pnlInput.add(btnInputpath,
                         new GridBagConstraints(2, 0, 1, 1, 10, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 5, 2, 5), 0, 0));
            pnlInput.add(new JLabel(Resources.LBL_FILE_FILTER),
                         new GridBagConstraints(0, 1, 1, 1, 10, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.BOTH,
                                                new Insets(2, 5, 2, 5), 0, 0));
            pnlInput.add(txtFilenameFilter,
                         new GridBagConstraints(1, 1, 1, 1, 50, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(2, 5, 2, 5), 0, 0));
            pnlInput.add(new JLabel(Resources.LBL_DRIFTER_PATH),
                         new GridBagConstraints(0, 2, 1, 1, 10, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.BOTH,
                                                new Insets(2, 5, 2, 5), 0, 0));
            pnlInput.add(txtDrifterPathname,
                         new GridBagConstraints(1, 2, 1, 1, 80, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(2, 5, 2, 5), 0, 0));
            pnlInput.add(btnDrifterPathname,
                         new GridBagConstraints(2, 2, 1, 1, 10, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 5, 2, 5), 0, 0));

            /** output */
            pnlOutput = new JPanel(new GridBagLayout());
            pnlOutput.setBorder(BorderFactory.createEtchedBorder(
                    EtchedBorder.
                    RAISED));
            ckBoxOutput = new JCheckBox(Resources.CK_BOX_RECORD_NC, false);
            txtOutputpath = new JTextField();
            txtOutputpath.setEditable(false);
            btnOutputpath = new JButton("...");
            btnOutputpath.setEnabled(true);
            prmRecordDt = new IntegerParameter(
                    Resources.PRM_RECORD_FREQUENCY, 12,
                    Resources.UNIT_FACTOR_DT, false);
            txtOutputFilename = new JTextField(Constant.OUTPUT_FILENAME_SINGLE);
            txtOutputFilename.setEnabled(false);

            pnlOutput.add(new JLabel(Resources.LBL_OUTPUT_PATH),
                          new GridBagConstraints(0, 0, 1, 1, 10, 10,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(2, 5, 2, 5), 0, 0));
            pnlOutput.add(txtOutputpath,
                          new GridBagConstraints(1, 0, 1, 1, 80, 10,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(2, 5, 2, 5), 0, 0));
            pnlOutput.add(btnOutputpath,
                          new GridBagConstraints(2, 0, 1, 1, 10, 10,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(2, 5, 2, 5), 0, 0));
            pnlOutput.add(ckBoxOutput,
                          new GridBagConstraints(0, 1, 1, 1, 10, 10,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(2, 5, 2, 5), 0, 0));
            pnlOutput.add(new JLabel(Resources.LBL_OUTPUT_FILENAME),
                          new GridBagConstraints(0, 2, 1, 1, 10, 10,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(2, 25, 2, 5), 0, 0));
            pnlOutput.add(txtOutputFilename,
                          new GridBagConstraints(1, 2, 1, 1, 80, 10,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(2, 5, 2, 5), 0, 0));
            pnlOutput.add(new JLabel(".nc"),
                          new GridBagConstraints(2, 2, 1, 1, 10, 10,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(2, 5, 2, 5), 0, 0));

            pnlOutput.add(prmRecordDt.createUI(),
                          new GridBagConstraints(0, 3, 3, 1, 100, 10,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(5, 25, 5, 5), 0, 0));

            /** Add components in the tab */
            this.add(pnlInput, new GridBagConstraints(0, 0, 1, 1, 100, 50,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));

            this.add(pnlOutput, new GridBagConstraints(0, 1, 1, 1, 100, 50,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));

        }

        public void addListeners() {

            ckBoxOutput.addActionListener(this);
            btnInputpath.addActionListener(this);
            btnOutputpath.addActionListener(this);
            btnDrifterPathname.addActionListener(this);

            txtFilenameFilter.addKeyListener(this);
            txtInputpath.addKeyListener(this);
            txtOutputFilename.addKeyListener(this);

            ckBoxOutput.addMouseListener(ConfigurationUI.this);
            btnInputpath.addMouseListener(ConfigurationUI.this);
            btnOutputpath.addMouseListener(ConfigurationUI.this);
            btnDrifterPathname.addMouseListener(ConfigurationUI.this);
            txtFilenameFilter.addMouseListener(ConfigurationUI.this);
            txtInputpath.addMouseListener(ConfigurationUI.this);
            prmRecordDt.addMouseListener(ConfigurationUI.this);
            txtOutputFilename.addMouseListener(ConfigurationUI.this);

            prmRecordDt.addValueListener(ConfigurationUI.this);
        }

        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();
            boolean hasChanged = false;

            /** rdBtnOutputNC */
            if (source == ckBoxOutput) {
                hasChanged = true;
                boolean selected = ckBoxOutput.isSelected();
                txtOutputFilename.setEnabled(selected);
                prmRecordDt.setEnabled(selected);
            }

            /** btnInputpath */
            if (source == btnInputpath) {
                String path = URI.create(txtInputpath.getText()).getPath();
                JFileChooser chooser = new JFileChooser(path);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnPath = chooser.showOpenDialog(ConfigurationUI.this);
                if (returnPath == JFileChooser.APPROVE_OPTION) {
                    hasChanged = true;
                    txtInputpath.setText(chooser.getSelectedFile().toURI().
                                         toString());
                    logBar.setMessage(statusBarMsg = "Typed input path " +
                            txtInputpath.getText());
                    getInputFiles(txtInputpath.getText(),
                                  txtFilenameFilter.getText());
                    tabTime.refresh();
                    tabModel.refresh();
                    if (listInputFiles != null) {
                        tabVariable.refresh();
                    }
                }
            }

            /** btnOuputpath */
            if (source == btnOutputpath) {
                JFileChooser chooser = new JFileChooser(txtOutputpath.
                        getText());
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnPath = chooser.showOpenDialog(ConfigurationUI.this);
                if (returnPath == JFileChooser.APPROVE_OPTION) {
                    hasChanged = true;
                    txtOutputpath.setText(uriCurrent.relativize(chooser.
                            getSelectedFile().toURI()).getPath());
                    logBar.setMessage(statusBarMsg = "Typed output path " +
                            txtOutputpath.getText());
                }
            }

            /** btnDrifterPathname */
            if (source == btnDrifterPathname) {
                JFileChooser chooser = new JFileChooser(uriCurrent.getPath());
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                chooser.setFileFilter(new FileNameExtensionFilter(Resources.
                        EXTENSION_DRIFTER + " (*.drf; *.txt; *.nc)", "drf",
                        "txt", "nc"));
                int returnPath = chooser.showOpenDialog(ConfigurationUI.this);
                if (returnPath == JFileChooser.APPROVE_OPTION) {
                    hasChanged = true;
                    txtDrifterPathname.setText(uriCurrent.relativize(chooser.
                            getSelectedFile().toURI()).getPath());
                    logBar.setMessage(statusBarMsg = "Drifter file: " +
                            txtDrifterPathname.getText());
                }
            }

            /** Notifies a parameter has been modified */
            if (hasChanged) {
                (ConfigurationUI.this).valueChanged(new ValueChangedEvent((
                        Component) e.getSource()));
            }
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {

            Object source = e.getSource();

            /** txtFilenameFilter */
            if (source == txtFilenameFilter) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String fileMask = txtFilenameFilter.getText().trim();
                    if (fileMask.isEmpty()) {
                        fileMask = DEFAULT_FILTER;
                        txtFilenameFilter.setText(fileMask);
                    }
                    logBar.setMessage(statusBarMsg = "Filename filter: " +
                            txtFilenameFilter.getText());
                    getInputFiles(txtInputpath.getText(), fileMask);
                    tabTime.refresh();
                    tabModel.refresh();
                    tabVariable.refresh();
                }
            }

            /** txtInputpath */
            if ((source == txtInputpath) &&
                (e.getKeyCode() == KeyEvent.VK_ENTER)) {
                if (URI.create(txtInputpath.getText()).getScheme() == null) {
                    txtInputpath.setText(new File(txtInputpath.getText()).toURI().
                                         toString());
                } else {
                    txtInputpath.setText(URI.create(txtInputpath.getText()).
                                         toString());
                }
                logBar.setMessage(statusBarMsg = "Typed input path " +
                        txtInputpath.getText());
                getInputFiles(txtInputpath.getText(),
                              txtFilenameFilter.getText());
                tabTime.refresh();
                tabModel.refresh();
                tabVariable.refresh();
            }

            /** Notifies the change */
            (ConfigurationUI.this).valueChanged(new ValueChangedEvent((
                    Component) e.getSource()));
        }

        public void setToolTips() {

            btnInputpath.setToolTipText(Resources.TIP_BTN_INPUT_PATH);
            txtInputpath.setToolTipText(Resources.TIP_BTN_INPUT_PATH);
            txtFilenameFilter.setToolTipText(Resources.TIP_TXT_FILTER);
            btnOutputpath.setToolTipText(Resources.TIP_BTN_OUTPUT_PATH);
            btnDrifterPathname.setToolTipText(Resources.TIP_BTN_DRIFTER_FILE);
            txtDrifterPathname.setToolTipText(Resources.TIP_BTN_DRIFTER_FILE);
            ckBoxOutput.setToolTipText(Resources.TIP_CK_BOX_RECORD_NC);
            prmRecordDt.setToolTipText(Resources.TIP_PRM_RECORD_FREQUENCY);
            txtOutputFilename.setToolTipText(Resources.TIP_TXT_OUTPUT_FILENAME);
        }
    }


////////////////////////////////////////////////////////////////////////////////
// Tab Model
////////////////////////////////////////////////////////////////////////////////
    private class TabModel extends JPanel implements ITab {

        ///////////////////////////////
        // Declaration of the variables
        ///////////////////////////////

        private JPanel pnlType, pnlScheme, pnlRange, pnlBoundary;

        private JRadioButton rdBtnRoms, rdBtnMars;

        private JRadioButton rdBtnEuler, rdBtnRk4;

        private JCheckBox ckBoxRange;

        private JFormattedTextField[] txtRange, txtBoundary;

        private float lonMin, lonMax, latMin, latMax;

        ///////////////
        // Constructors
        ///////////////

        TabModel() {

            super(new GridBagLayout());
            setName(Resources.TAB_MODEL);
            createUI();
            addListeners();
        }

        ////////////////////////////
        // Definition of the methods
        ////////////////////////////

        public void read(INIFile file) {

            /** Roms vs Mars */
            model = file.getIntegerProperty(Structure.SECTION_MODEL,
                                            Structure.MODEL);
            if (model == Constant.ROMS) {
                rdBtnRoms.doClick();
            } else {
                rdBtnMars.doClick();
            }

            /** Euler vs Rk4 */
            int typeScheme = file.getIntegerProperty(Structure.
                    SECTION_MODEL,
                    Structure.SCHEME);
            if (typeScheme == Constant.EULER) {
                rdBtnEuler.doClick();
            } else {
                rdBtnRk4.doClick();
            }

            /** Range */
            boolean bln = file.getBooleanProperty(Structure.SECTION_MODEL,
                                                  Structure.RANGE);
            if (bln) {
                ckBoxRange.doClick();
                txtRange[0].setValue(file.getDoubleProperty(
                        Structure.SECTION_MODEL,
                        Structure.LON + String.valueOf(1)));
                txtRange[1].setValue(file.getDoubleProperty(
                        Structure.SECTION_MODEL,
                        Structure.LAT + String.valueOf(1)));
                txtRange[2].setValue(file.getDoubleProperty(
                        Structure.SECTION_MODEL,
                        Structure.LON + String.valueOf(2)));
                txtRange[3].setValue(file.getDoubleProperty(
                        Structure.SECTION_MODEL,
                        Structure.LAT + String.valueOf(2)));
            }
        }

        public void write(INIFile file) {

            /** creates section model */
            file.addSection(Structure.SECTION_MODEL,
                            Structure.MAN_SECTION_MODEL);

            /** model ROMS vs MARS */
            file.setIntegerProperty(Structure.SECTION_MODEL,
                                    Structure.MODEL,
                                    model,
                                    Structure.MAN_MODEL);

            /** scheme Euler vs Rk4 */
            int scheme = rdBtnEuler.isSelected()
                         ? Constant.EULER
                         : Constant.RK4;
            file.setIntegerProperty(Structure.SECTION_MODEL,
                                    Structure.SCHEME,
                                    scheme,
                                    Structure.MAN_SCHEME);

            /** Range */
            file.setBooleanProperty(Structure.SECTION_MODEL,
                                    Structure.RANGE,
                                    ckBoxRange.isSelected(),
                                    null);
            if (ckBoxRange.isSelected()) {
                for (int i = 0; i < 2; i++) {
                    Number nb = (Number) txtRange[2 * i].getValue();
                    file.setDoubleProperty(Structure.SECTION_MODEL,
                                           Structure.LON +
                                           String.valueOf(i + 1),
                                           nb.floatValue(),
                                           null);
                    nb = (Number) txtRange[2 * i + 1].getValue();
                    file.setDoubleProperty(Structure.SECTION_MODEL,
                                           Structure.LAT +
                                           String.valueOf(i + 1),
                                           nb.floatValue(),
                                           null);
                }
            } else {
                for (int i = 0; i < 2; i++) {
                    file.setStringProperty(Structure.SECTION_MODEL,
                                           Structure.LON +
                                           String.valueOf(i + 1),
                                           "null",
                                           null);
                    file.setStringProperty(Structure.SECTION_MODEL,
                                           Structure.LAT +
                                           String.valueOf(i + 1),
                                           "null",
                                           null);
                }
            }
        }

        /**
         *
         */
        private void refresh() {

            if (tabIO.listInputFiles != null) {
                try {
                    getNcInfo(tabIO.listInputFiles.get(0));
                    logBar.setMessage(statusBarMsg =
                            "Extraction grid boundaries [OK]");
                } catch (Exception e) {
                    lonMin = latMin = lonMax = latMax = 0.f;
                    printErr(new IOException(
                            "Problem extracting grid boundaries from file " +
                            tabIO.listInputFiles.get(0)),
                             "Configuration editor - Model tab");
                }
            }
            txtBoundary[0].setValue(lonMin);
            txtBoundary[1].setValue(latMin);
            txtBoundary[2].setValue(lonMax);
            txtBoundary[3].setValue(latMax);
        }

        /**
         *
         * @param path String
         * @throws IOException
         */
        private void getNcInfo(String path) throws IOException {

            NetcdfFile ncIn;
            Array arrLon, arrLat;
            float lonMin, lonMax, latMin, latMax;

            ncIn = NetcdfDataset.openFile(path, null);

            lonMin = Float.MAX_VALUE;
            lonMax = -lonMin;
            latMin = Float.MAX_VALUE;
            latMax = -latMin;

            arrLon = ncIn.findVariable(NCField.lon.getName(model)).read();
            arrLat = ncIn.findVariable(NCField.lat.getName(model)).read();

            IndexIterator iter = arrLon.getIndexIteratorFast();
            Number nb;
            float nb_f;
            while (iter.hasNext()) {
                nb = (Number) iter.next();
                nb_f = nb.floatValue();
                if (nb_f >= lonMax) {
                    lonMax = nb_f;
                }
                if (nb_f <= lonMin) {
                    lonMin = nb_f;
                }
            }

            iter = arrLat.getIndexIteratorFast();
            while (iter.hasNext()) {
                nb = (Number) iter.next();
                nb_f = nb.floatValue();
                if (nb_f >= latMax) {
                    latMax = nb_f;
                }
                if (nb_f <= latMin) {
                    latMin = nb_f;
                }
            }

            arrLon = null;
            arrLat = null;

            if (lonMin > lonMax) {
                float lontmp = lonMin;
                lonMin = lonMax;
                lonMax = lontmp;
            }

            if (latMin > latMax) {
                float lattmp = latMin;
                latMin = latMax;
                latMax = lattmp;
            }

            this.lonMin = lonMin;
            this.lonMax = lonMax;
            this.latMin = latMin;
            this.latMax = latMax;
        }

        /**
         *
         */
        public void createUI() {

            /** panel Type of model */
            pnlType = new JPanel(new GridLayout(1, 2, 5, 5));
            pnlType.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
                    RAISED));
            rdBtnRoms = new JRadioButton(Resources.RD_BTN_ROMS, true);
            rdBtnMars = new JRadioButton(Resources.RD_BTN_MARS, false);
            ButtonGroup btnGroup1 = new ButtonGroup();
            btnGroup1.add(rdBtnRoms);
            btnGroup1.add(rdBtnMars);
            pnlType.add(rdBtnRoms);
            pnlType.add(rdBtnMars);

            /** panel Scheme */
            pnlScheme = new JPanel(new GridLayout(1, 2, 5, 5));
            pnlScheme.setBorder(BorderFactory.createEtchedBorder(
                    EtchedBorder.
                    RAISED));
            rdBtnEuler = new JRadioButton(Resources.RD_BTN_EULER, true);
            rdBtnRk4 = new JRadioButton(Resources.RD_BTN_RK4, false);
            ButtonGroup btnGroup2 = new ButtonGroup();
            btnGroup2.add(rdBtnEuler);
            btnGroup2.add(rdBtnRk4);
            pnlScheme.add(rdBtnEuler);
            pnlScheme.add(rdBtnRk4);

            /** panel Range domain */
            pnlRange = new JPanel(new GridBagLayout());
            txtRange = new JFormattedTextField[4];
            ckBoxRange = new JCheckBox(Resources.CK_BOX_RANGE);
            NumberFormat nfLonLat = NumberFormat.getInstance(Locale.US);
            nfLonLat.setMinimumFractionDigits(1);
            nfLonLat.setMaximumFractionDigits(2);
            nfLonLat.setMaximumIntegerDigits(3);
            nfLonLat.setMinimumIntegerDigits(1);
            for (int i = 0; i < 4; i++) {
                txtRange[i] = new JFormattedTextField(nfLonLat);
                txtRange[i].setPreferredSize(new Dimension(40, 20));
                txtRange[i].setValue(0);
                txtRange[i].setEnabled(false);
            }
            pnlRange.setBorder(BorderFactory.createEtchedBorder(
                    EtchedBorder.
                    RAISED));

            pnlRange.add(ckBoxRange,
                         new GridBagConstraints(0, 0, 3, 1, 90, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 5, 2, 5), 0, 0));
            pnlRange.add(new JLabel(Resources.LBL_LON),
                         new GridBagConstraints(1, 1, 1, 1, 30, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 5, 2, 5), 0, 0));
            pnlRange.add(new JLabel(Resources.LBL_LAT),
                         new GridBagConstraints(2, 1, 1, 1, 30, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 5, 2, 5), 0, 0));
            pnlRange.add(new JLabel(Resources.LBL_P1),
                         new GridBagConstraints(0, 2, 1, 1, 30, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 25, 5, 5), 0, 0));
            pnlRange.add(new JLabel(Resources.LBL_P3),
                         new GridBagConstraints(0, 3, 1, 1, 30, 10,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 25, 5, 5), 0, 0));
            for (int i = 0; i < 4; i++) {
                pnlRange.add(txtRange[i],
                             new GridBagConstraints((i % 2 + 1),
                        ((int) (i / 2) + 2), 1, 1, 30,
                        10,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(2, 5, 2, 5), 0, 0));
            }

            /** panel Boundary */
            pnlBoundary = new JPanel(new GridBagLayout());
            txtBoundary = new JFormattedTextField[4];
            for (int i = 0; i < 4; i++) {
                txtBoundary[i] = new JFormattedTextField(nfLonLat);
                txtBoundary[i].setPreferredSize(new Dimension(40, 20));
                txtBoundary[i].setValue(0);
                txtBoundary[i].setEnabled(true);
                txtBoundary[i].setEditable(false);
            }
            pnlBoundary.setBorder(BorderFactory.createEtchedBorder(
                    EtchedBorder.
                    RAISED));

            pnlBoundary.add(new JLabel(Resources.LBL_BOUNDARY),
                            new GridBagConstraints(0, 0, 3, 1, 90, 10,
                    GridBagConstraints.WEST,
                    GridBagConstraints.NONE,
                    new Insets(2, 5, 2, 5), 0, 0));
            pnlBoundary.add(new JLabel(Resources.LBL_LON),
                            new GridBagConstraints(1, 1, 1, 1, 30, 10,
                    GridBagConstraints.WEST,
                    GridBagConstraints.NONE,
                    new Insets(2, 5, 2, 5), 0, 0));
            pnlBoundary.add(new JLabel(Resources.LBL_LAT),
                            new GridBagConstraints(2, 1, 1, 1, 30, 10,
                    GridBagConstraints.WEST,
                    GridBagConstraints.NONE,
                    new Insets(2, 5, 2, 5), 0, 0));
            pnlBoundary.add(new JLabel(Resources.LBL_MINIMA),
                            new GridBagConstraints(0, 2, 1, 1, 30, 10,
                    GridBagConstraints.WEST,
                    GridBagConstraints.NONE,
                    new Insets(5, 25, 5, 5), 0, 0));
            pnlBoundary.add(new JLabel(Resources.LBL_MAXIMA),
                            new GridBagConstraints(0, 3, 1, 1, 30, 10,
                    GridBagConstraints.WEST,
                    GridBagConstraints.NONE,
                    new Insets(5, 25, 5, 5), 0, 0));
            for (int i = 0; i < 4; i++) {
                pnlBoundary.add(txtBoundary[i],
                                new GridBagConstraints((i % 2 + 1),
                        ((int) (i / 2) + 2), 1, 1, 30, 10,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(2, 5, 2, 5), 0, 0));
            }

            /** Adds components in the tab */
            this.add(pnlType, new GridBagConstraints(0, 0, 1, 1, 50, 10,
                    GridBagConstraints.WEST,
                    GridBagConstraints.HORIZONTAL,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.add(pnlScheme, new GridBagConstraints(1, 0, 1, 1, 50, 10,
                    GridBagConstraints.WEST,
                    GridBagConstraints.HORIZONTAL,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.add(pnlRange, new GridBagConstraints(0, 1, 2, 1, 100, 40,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.add(pnlBoundary,
                     new GridBagConstraints(0, 2, 2, 1, 100, 40,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.BOTH,
                                            new Insets(5, 5, 5, 5), 0, 0));
        }

        public void addListeners() {

            ckBoxRange.addActionListener(this);
            rdBtnRoms.addActionListener(this);
            rdBtnMars.addActionListener(this);
            rdBtnEuler.addActionListener(this);
            rdBtnRk4.addActionListener(this);

            ckBoxRange.addMouseListener(ConfigurationUI.this);
            rdBtnRoms.addMouseListener(ConfigurationUI.this);
            rdBtnMars.addMouseListener(ConfigurationUI.this);
            rdBtnEuler.addMouseListener(ConfigurationUI.this);
            rdBtnRk4.addMouseListener(ConfigurationUI.this);
        }

        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();

            /** Checkbox Range */
            if (source == ckBoxRange) {
                boolean enabled = ckBoxRange.isSelected();
                for (int i = 0; i < 4; i++) {
                    txtRange[i].setEnabled(enabled);
                }
            }

            /** Radio button Roms */
            if (source == rdBtnRoms) {
                model = Constant.ROMS;
                tabVariable.refresh();
            }

            /** Radio button Mars */
            if (source == rdBtnMars) {
                model = Constant.MARS;
                tabVariable.refresh();
            }

            /** Notifies the action */
            (ConfigurationUI.this).valueChanged(new ValueChangedEvent((
                    Component) e.getSource()));
        }

        public void setToolTips() {

            rdBtnEuler.setToolTipText(Resources.TIP_BTN_EULER);
            rdBtnMars.setToolTipText(Resources.TIP_BTN_MARS);
            rdBtnRk4.setToolTipText(Resources.TIP_BTN_RK4);
            rdBtnRoms.setToolTipText(Resources.TIP_BTN_ROMS);
            ckBoxRange.setToolTipText(Resources.TIP_CK_BOX_RANGE);
        }

    }


////////////////////////////////////////////////////////////////////////////////
// Tab Time
////////////////////////////////////////////////////////////////////////////////
    private class TabTime extends JPanel implements ITab,
            KeyListener {

        ///////////////////////////////
        // Declaration of the variables
        ///////////////////////////////

        private JPanel pnlInfo, pnlTime, pnlArrow;

        private JRadioButton rdBtnForward, rdBtnBackward;

        private JRadioButton rdBtnClimato, rdBtnGregorian;

        private DateParameter prmBeginingSimulation, prmFirst;

        private DurationParameter prmTransportDuration, MaxDuration;

        private IntegerParameter prmDt;

        private JFormattedTextField txtOrigin;

        private JTextField txtDatasetDt, txtDatasetRecordDt,
                txtAcceptableDt;

        private JButton btnFillBeginingSimulation, btnFillTransportDuration,
                btnFillDt;

        private Calendar calendarOrigin;

        private long firstTime, maxDuration;

        private long[] timeBoundaries;

        private int acceptableDt, datasetDt, datasetDtR;

        private static final int VMAX = 1;

        ///////////////
        // Constructors
        ///////////////

        TabTime() {

            super(new GridBagLayout());
            setName(Resources.TAB_TIME);
            createUI();
            addListeners();
        }

        ////////////////////////////
        // Definition of the methods
        ////////////////////////////

        public void read(INIFile file) {

            int typeCalendar = file.getIntegerProperty(
                    Structure.SECTION_TIME, Structure.CALENDAR);
            if (typeCalendar == Constant.GREGORIAN) {
                SimpleDateFormat dtFormat = new SimpleDateFormat(
                        "yyyy/MM/dd HH:mm");
                dtFormat.setCalendar(calendarOrigin);
                try {
                    calendarOrigin.setTime(dtFormat.parse(file.
                            getStringProperty(
                                    Structure.SECTION_TIME,
                                    Structure.TIME_ORIGIN)));
                } catch (ParseException e) {
                    e.printStackTrace();
                    calendarOrigin.setTimeInMillis(0L);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    calendarOrigin.setTimeInMillis(0L);
                }
                txtOrigin.setValue(calendarOrigin.getTime());
            }
            prmBeginingSimulation.setDefaultValue(file.getLongProperty(
                    Structure.SECTION_TIME, Structure.T0) *
                                                  1000L);
            prmBeginingSimulation.setValue(prmBeginingSimulation.
                                           getDefaultValue().longValue());
            prmTransportDuration.setDefaultValue(file.getLongProperty(
                    Structure.SECTION_TIME,
                    Structure.TRANSPORT_DURATION) * 1000L);
            prmTransportDuration.setValue(prmTransportDuration.
                                          getDefaultValue().longValue());
            if (typeCalendar == Constant.GREGORIAN) {
                rdBtnGregorian.doClick();
            } else {
                rdBtnClimato.doClick();
            }
            prmDt.setValue(file.getIntegerProperty(Structure.
                    SECTION_TIME, Structure.DT));

            int arrow = file.getIntegerProperty(
            Structure.SECTION_TIME, Structure.ARROW);
            if (arrow == Constant.FORWARD) {
                rdBtnForward.doClick();
            } else {
                rdBtnBackward.doClick();
            }
        }

        public void write(INIFile file) {

            /** creates section time */
            file.addSection(Structure.SECTION_TIME, Structure.MAN_SECTION_TIME);

            /** arrow */
            int arrow = rdBtnForward.isSelected()
                        ? Constant.FORWARD
                        : Constant.BACKWARD;
            file.setIntegerProperty(Structure.SECTION_TIME,
                                    Structure.ARROW,
                                    arrow,
                                    Structure.MAN_ARROW);

            /** calendar */
            int typeCalendar = rdBtnGregorian.isSelected()
                               ? Constant.GREGORIAN
                               : Constant.CLIMATO;
            file.setIntegerProperty(Structure.SECTION_TIME,
                                    Structure.CALENDAR,
                                    typeCalendar,
                                    Structure.MAN_CALENDAR);

            /** origin */
            file.setStringProperty(Structure.SECTION_TIME,
                                   Structure.TIME_ORIGIN,
                                   txtOrigin.getText(),
                                   null);

            /** beginning simulation */
            prmBeginingSimulation.write(file,
                                        Structure.SECTION_TIME,
                                        Structure.T0);

            /** transport duration */
            prmTransportDuration.write(file,
                                       Structure.SECTION_TIME,
                                       Structure.TRANSPORT_DURATION);

            /** time step */
            prmDt.write(file,
                        Structure.SECTION_TIME,
                        Structure.DT);
        }

        private void getNcInfo(String path) throws IOException,
                NullPointerException {

            NetcdfFile ncIn;
            Array arrTime, arrPm, arrPn, arrLon, arrLat;
            Index index0, index1, indexN;
            int length, nx, ny;
            double t0, tf, maxDuration, dgrid, datasetDtR, datasetDt;

            /** Load the dataset */
            ncIn = NetcdfDataset.openFile(path, null);

            try {
                /** get first time available */
                arrTime = ncIn.findVariable(NCField.time.getName(model)).
                          read();
                index0 = arrTime.getIndex().set(0);
                t0 = arrTime.getDouble(index0);

                /** get dataset record dt */
                index1 = arrTime.getIndex().set(1);
                datasetDtR = arrTime.getDouble(index1) - t0;

                /** get dataset record dt */
                datasetDt = Double.NaN;
                if (model == Constant.ROMS) {
                    try {
                        if (ncIn.findGlobalAttribute(NCField.thetaS.getName(
                                Constant.ROMS)) == null) {
                            datasetDt = ncIn.findVariable("dt").
                                        readScalarInt();
                        } else {
                            datasetDt = ncIn.findGlobalAttribute("dt").
                                        getNumericValue().
                                        floatValue();
                        }
                    } catch (Exception e) {
                        datasetDt = Double.NaN;
                    }
                }

                /** Get last time record available */
                ncIn = NetcdfDataset.openFile(tabIO.listInputFiles.get(tabIO.
                        listInputFiles.size() - 1), null);
                arrTime = ncIn.findVariable(NCField.time.getName(model)).read();
                length = arrTime.getShape()[0];
                indexN = arrTime.getIndex().set(length - 1);
                tf = arrTime.getDouble(indexN);

                /** get max duration possible */
                maxDuration = tf - t0;

                /** get the maximum dt allowed */
                dgrid = 0.d;
                if (model == Constant.ROMS) {
                    nx = ncIn.findDimension(NCField.xiDim.getName(Constant.
                            ROMS)).
                         getLength();
                    ny = ncIn.findDimension(NCField.etaDim.getName(Constant.
                            ROMS)).
                         getLength();
                    arrPm = ncIn.findVariable(NCField.pm.getName(Constant.
                            ROMS)).
                            read();
                    arrPn = ncIn.findVariable(NCField.pn.getName(Constant.
                            ROMS)).
                            read();
                    for (int i = 0; i < nx; i++) {
                        for (int j = 0; j < ny; j++) {
                            dgrid = Math.max(dgrid,
                                             Math.max(arrPm.getDouble(arrPm.
                                    getIndex().set(j, i)),
                                    arrPn.getDouble(arrPn.
                                    getIndex().set(j, i))));
                        }
                    }
                    dgrid = 1 / dgrid;
                } else {
                    try {
                        arrLat = ncIn.findVariable(NCField.lat.getName(
                                Constant.
                                MARS)).read("0:1");
                        arrLon = ncIn.findVariable(NCField.lon.getName(
                                Constant.
                                MARS)).read("0:1");
                        index0 = arrLat.getIndex().set(0);
                        index1 = arrLat.getIndex().set(1);
                        dgrid = Math.min(
                                geodesicDistance(
                                        arrLat.getDouble(index0),
                                        arrLon.getDouble(index0),
                                        arrLat.getDouble(index0),
                                        arrLon.getDouble(index1)),
                                geodesicDistance(
                                        arrLat.getDouble(index0),
                                        arrLon.getDouble(index0),
                                        arrLat.getDouble(index1),
                                        arrLon.getDouble(index0)));
                    } catch (InvalidRangeException e) {
                        e.printStackTrace();
                        throw new IOException("Error reading fields " +
                                              NCField.lat.getName(Constant.
                                MARS)
                                              + " " +
                                              NCField.lon.getName(Constant.
                                MARS));
                    }
                }
            } catch (NullPointerException e) {
                throw new NullPointerException(
                        "Unable to find time / lon / lat fields in file " +
                        ncIn.getLocation());
            }
            ncIn.close();

            /** return the values to tabIO variables */
            timeBoundaries = new long[] {(long) (t0 * 1e3), (long) (tf * 1e3)};
            this.firstTime = rdBtnForward.isSelected()
                             ? timeBoundaries[0]
                             : timeBoundaries[1];
            this.maxDuration = (long) (maxDuration * 1e3);
            this.acceptableDt = (int) (0.7f * dgrid / VMAX);
            this.datasetDt = (int) datasetDt;
            this.datasetDtR = (int) datasetDtR;
        }

        /**
         *
         */
        private void refresh() {

            if (tabIO.listInputFiles != null) {
                try {
                    getNcInfo(tabIO.listInputFiles.get(0));
                    prmFirst.setCalendar(prmBeginingSimulation.getCalendar());
                    prmFirst.setValue(firstTime);
                    MaxDuration.setValue(maxDuration);
                    txtDatasetDt.setText(humanReadable(datasetDt));
                    txtDatasetRecordDt.setText(humanReadable(datasetDtR));
                    txtAcceptableDt.setText(humanReadable(acceptableDt));
                    logBar.setMessage(statusBarMsg = "Extraction time info [OK]");
                } catch (Exception e) {
                    prmFirst.setValue(0);
                    MaxDuration.setValue(0);
                    txtDatasetDt.setText("NaN");
                    txtDatasetRecordDt.setText("NaN");
                    txtAcceptableDt.setText("NaN");
                    printErr(e,
                             "Configuration editor - Time tab");
                }
            } else {
                prmFirst.setValue(0);
                MaxDuration.setValue(0);
                txtDatasetDt.setText("NaN");
                txtDatasetRecordDt.setText("NaN");
                txtAcceptableDt.setText("NaN");
            }
        }

        /**
         *
         * @param typeCalendar int
         */
        private void reset(int typeCalendar) {

            txtOrigin.setEnabled(typeCalendar == Constant.GREGORIAN);
            Calendar cld = (typeCalendar == Constant.GREGORIAN)
                           ? new Calendar1900(
                                   calendarOrigin.get(Calendar.YEAR),
                                   calendarOrigin.get(Calendar.MONTH),
                                   calendarOrigin.get(Calendar.DAY_OF_MONTH))
                           : new ClimatoCalendar();

            prmBeginingSimulation.setCalendar(cld);
            prmBeginingSimulation.setValue(prmBeginingSimulation.
                                           getDefaultValue().longValue());
            prmFirst.setCalendar(cld);
            prmFirst.setValue(firstTime);
        }

        /**
         *
         * @param durationSecond double
         * @return String
         */
        private String humanReadable(double durationSecond) {

            StringBuffer duration;
            int[] div = {1, 60, 3600, 86400, 31536000};
            String[] unit = {
                            Resources.UNIT_SECOND, Resources.UNIT_MINUTE,
                            Resources.UNIT_HOUR,
                            Resources.UNIT_DAY};
            int i = 0;
            NumberFormat nbFormat = NumberFormat.getIntegerInstance(Locale.
                    US);

            /** Return NaN if duration not defined */
            if (Double.isNaN(durationSecond)) {
                return "NaN";
            }

            while ((int) (durationSecond / div[i + 1]) > 0) {
                i++;
            }
            duration = new StringBuffer(nbFormat.format(durationSecond /
                    div[i]));
            duration.append(" ");
            duration.append(unit[i]);

            return duration.toString();
        }

        /**
         *
         * @param lat1 double
         * @param lon1 double
         * @param lat2 double
         * @param lon2 double
         * @return double
         */
        double geodesicDistance(double lat1, double lon1, double lat2,
                                double lon2) {
            //--------------------------------------------------------------
            // Return the curvilinear abscissa s(A[lat1, lon1]B[lat2, lon2])
            double d = 6367000.0f * Math.sqrt(2.0f
                                              - 2.0f *
                                              Math.cos(Math.PI * lat1 /
                    180.0f) *
                                              Math.cos(Math.PI * lat2 /
                    180.0f) *
                                              Math.cos(Math.PI *
                    (lon1 - lon2) /
                    180.0f)
                                              - 2.0f *
                                              Math.sin(Math.PI * lat1 /
                    180.0f) *
                                              Math.sin(Math.PI * lat2 /
                    180.0f));
            return (d);
        }

        /**
         *
         */
        public void createUI() {

            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy/MM/dd HH:mm");
            calendarOrigin = new Calendar1900();

            /** panel time arrow */
            pnlArrow = new JPanel(new GridBagLayout());
            pnlArrow.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
                    RAISED));
            rdBtnForward = new JRadioButton("Forward", true);
            rdBtnBackward = new JRadioButton("Backward");
            ButtonGroup btnGroup1 = new ButtonGroup();
            btnGroup1.add(rdBtnForward);
            btnGroup1.add(rdBtnBackward);
            pnlArrow.add(rdBtnForward,
                        new GridBagConstraints(0, 0, 1, 1, 50, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));
           pnlArrow.add(rdBtnBackward,
                        new GridBagConstraints(1, 0, 1, 1, 50, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));

            /** panel time options */
            pnlTime = new JPanel(new GridBagLayout());
            pnlTime.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
                    RAISED));
            rdBtnClimato = new JRadioButton(Resources.RD_BTN_TIME_CLIMATO, false);
            rdBtnGregorian = new JRadioButton(Resources.RD_BTN_TIME_REAL, true);
            ButtonGroup btnGroup = new ButtonGroup();
            btnGroup.add(rdBtnClimato);
            btnGroup.add(rdBtnGregorian);
            txtOrigin = new JFormattedTextField(dateFormat);
            dateFormat.setCalendar(calendarOrigin);
            calendarOrigin.setTimeInMillis(0);
            txtOrigin.setValue(calendarOrigin.getTime());
            JPanel pnlTimeOrigin = new JPanel(new GridLayout(1, 2, 10, 0));
            pnlTimeOrigin.add(new JLabel(Resources.LBL_ORIGIN));
            pnlTimeOrigin.add(txtOrigin);

            prmBeginingSimulation = new DateParameter(
                    Resources.PRM_BEGIN_SIMU, new Calendar1900(),
                    Resources.UNIT_NONE, true);
            prmTransportDuration = new DurationParameter(
                    Resources.PRM_DURATION_TRANSPORT, "0002/00:00",
                    Resources.UNIT_DURATION, true);
            prmDt = new IntegerParameter(
                    Resources.PRM_INTERNAL_DT, 3600, Resources.UNIT_SECOND, true);

            btnFillBeginingSimulation = new JButton(Resources.BTN_FILL_T0);
            btnFillTransportDuration = new JButton(Resources.BTN_FILL_DURATION);
            btnFillDt = new JButton(Resources.BTN_FILL_DT);

            pnlTime.add(rdBtnClimato,
                        new GridBagConstraints(0, 0, 1, 1, 50, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));
            pnlTime.add(rdBtnGregorian,
                        new GridBagConstraints(1, 0, 1, 1, 50, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));
            pnlTime.add(pnlTimeOrigin,
                        new GridBagConstraints(0, 1, 2, 1, 100, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));
            pnlTime.add(prmBeginingSimulation.createUI(),
                        new GridBagConstraints(0, 2, 1, 1, 60, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));
            pnlTime.add(btnFillBeginingSimulation,
                        new GridBagConstraints(1, 2, 1, 1, 40, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(2, 5, 2, 5), 0, 0));
            pnlTime.add(prmTransportDuration.createUI(),
                        new GridBagConstraints(0, 3, 1, 1, 60, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));
            pnlTime.add(btnFillTransportDuration,
                        new GridBagConstraints(1, 3, 1, 1, 40, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(2, 5, 2, 5), 0, 0));
            pnlTime.add(prmDt.createUI(),
                        new GridBagConstraints(0, 4, 1, 1, 60, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));
            pnlTime.add(btnFillDt,
                        new GridBagConstraints(1, 4, 1, 1, 40, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(2, 5, 2, 5), 0, 0));

            /** panel Info */
            pnlInfo = new JPanel(new GridBagLayout());
            pnlInfo.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
                    RAISED));
            prmFirst = new DateParameter(Resources.LBL_FIRST_TIME,
                                         new Calendar1900(),
                                         Resources.UNIT_NONE, true);
            prmFirst.setEditable(false);
            MaxDuration = new DurationParameter(Resources.LBL_MAX_DURATION,
                                                "NaN",
                                                Resources.UNIT_DURATION, true);
            MaxDuration.setEditable(false);
            txtDatasetDt = new JTextField("NaN");
            txtDatasetDt.setEditable(false);
            txtDatasetRecordDt = new JTextField("NaN");
            txtDatasetRecordDt.setEditable(false);
            txtAcceptableDt = new JTextField("NaN");
            txtAcceptableDt.setEditable(false);

            pnlInfo.add(prmFirst.createUI(),
                        new GridBagConstraints(0, 0, 2, 1, 100, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));

            pnlInfo.add(MaxDuration.createUI(),
                        new GridBagConstraints(0, 1, 2, 1, 100, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));

            pnlInfo.add(new JLabel(Resources.LBL_DT),
                        new GridBagConstraints(0, 2, 1, 1, 50, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));
            pnlInfo.add(txtDatasetDt,
                        new GridBagConstraints(1, 2, 1, 1, 50, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));

            pnlInfo.add(new JLabel(Resources.LBL_DT_RECORD),
                        new GridBagConstraints(0, 3, 1, 1, 50, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));
            pnlInfo.add(txtDatasetRecordDt,
                        new GridBagConstraints(1, 3, 1, 1, 50, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));
            pnlInfo.add(new JLabel(Resources.LBL_DT_CFL),
                        new GridBagConstraints(0, 4, 1, 1, 50, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));
            pnlInfo.add(txtAcceptableDt,
                        new GridBagConstraints(1, 4, 1, 1, 50, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(2, 5, 2, 5), 0, 0));

            /** Adds the component in the tab */
            this.add(pnlArrow, new GridBagConstraints(0, 0, 1, 1, 100, 10,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.add(pnlInfo, new GridBagConstraints(0, 1, 1, 1, 100, 45,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));

            this.add(pnlTime, new GridBagConstraints(0, 2, 1, 1, 100, 45,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
        }

        /**
         *
         */
        public void addListeners() {

            rdBtnForward.addActionListener(this);
            rdBtnBackward.addActionListener(this);
            rdBtnGregorian.addActionListener(this);
            rdBtnClimato.addActionListener(this);
            btnFillBeginingSimulation.addActionListener(this);
            btnFillTransportDuration.addActionListener(this);
            btnFillDt.addActionListener(this);


            txtOrigin.addKeyListener(this);

            prmBeginingSimulation.addValueListener(ConfigurationUI.this);
            prmTransportDuration.addValueListener(ConfigurationUI.this);
            prmDt.addValueListener(ConfigurationUI.this);

            rdBtnForward.addMouseListener(ConfigurationUI.this);
            rdBtnBackward.addMouseListener(ConfigurationUI.this);
            prmBeginingSimulation.addMouseListener(ConfigurationUI.this);
            prmDt.addMouseListener(ConfigurationUI.this);
            prmTransportDuration.addMouseListener(ConfigurationUI.this);
            txtOrigin.addMouseListener(ConfigurationUI.this);

        }

        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();

            /** rdBtnForward */
            if (source == rdBtnForward) {
                firstTime = timeBoundaries[0];
                prmFirst.setValue(firstTime);
            }

            /** rdBtnBackward */
            if (source == rdBtnBackward) {
                firstTime = timeBoundaries[1];
                prmFirst.setValue(firstTime);
            }

            /** rdBtnGregorian */
            if (source == rdBtnGregorian) {
                reset(Constant.GREGORIAN);
            }

            /** rdBtnClimato */
            if (source == rdBtnClimato) {
                reset(Constant.CLIMATO);
            }

            /** btnFillBeginingSimulation */
            if (source == btnFillBeginingSimulation) {
                prmBeginingSimulation.setValue(firstTime);
            }

            /** btnFillTransportDuration */
            if (source == btnFillTransportDuration) {
                prmTransportDuration.setValue(maxDuration);
            }

            /** btnFillDt */
            if (source == btnFillDt) {
                prmDt.setValue(acceptableDt);
            }

            /** Notifies the action */
            (ConfigurationUI.this).valueChanged(new ValueChangedEvent((
                    Component) e.getSource()));
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {

            /** txtOrigin */
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                long time = prmBeginingSimulation.getValue().longValue();
                reset(rdBtnGregorian.isSelected()
                      ? Constant.GREGORIAN
                      : Constant.CLIMATO);
                prmBeginingSimulation.setValue(time);
                (ConfigurationUI.this).valueChanged(new ValueChangedEvent(e.
                        getComponent()));
            }

            /** Notifies the action */
            (ConfigurationUI.this).valueChanged(new ValueChangedEvent((
                    Component) e.getSource()));
        }

        public void setToolTips() {

            rdBtnForward.setToolTipText(Resources.TIP_RD_BTN_FORWARD);
            rdBtnBackward.setToolTipText(Resources.TIP_RD_BTN_BACKWARD);
            rdBtnClimato.setToolTipText(Resources.
                                        TIP_RD_BTN_CLIMATO_CALENDAR);
            rdBtnGregorian.setToolTipText(Resources.
                                          TIP_BTN_GREGORIAN_CALENDAR);
            prmBeginingSimulation.setToolTipText(Resources.TIP_PRM_BEGIN_SIMU);
            prmDt.setToolTipText(Resources.TIP_PRM_DT);
            prmTransportDuration.setToolTipText(Resources.
                                                TIP_PRM_DURATION_TRANSPORT);
            txtOrigin.setToolTipText(Resources.TIP_TXT_ORIGIN);
            btnFillBeginingSimulation.setToolTipText(Resources.TIP_BTN_FILL_T0);
            btnFillTransportDuration.setToolTipText(Resources.
                                TIP_BTN_FILL_DURATION);
            btnFillDt.setToolTipText(Resources.TIP_BTN_FILL_DT);

        }
    }


////////////////////////////////////////////////////////////////////////////////
// Tab Transport
////////////////////////////////////////////////////////////////////////////////
    private class TabTransport extends JPanel implements ITab {

        ///////////////////////////////
        // Declaration of the variables
        ///////////////////////////////

        private JCheckBox ckBoxAdvectH, ckBoxAdvectV, ckBoxDisperseH,
                ckBoxDisperseV, ckBoxBuoyancy, ckBoxMigration;

        private JRadioButton rdBtn2D, rdBtn3D;

        private FloatParameter prmDensity = new FloatParameter(
                Resources.PRM_EGG_DENSITY, 1.025f, Resources.UNIT_DENSITY, false);

        private FloatParameter prmAgeBuoyancy = new FloatParameter(
                Resources.PRM_AGE_LIMIT_BUOYANCY, 3.0f, Resources.UNIT_DAY, false);

        private FloatParameter prmAgeMigration = new FloatParameter(
                Resources.PRM_AGE_LIMIT_MIGRATION, 3.0f, Resources.UNIT_DAY, false);

        private boolean migration = false;

        private FloatParameter prmDepthDay = new FloatParameter(
                Resources.PRM_DEPTH_DAY, 10.0f, Resources.UNIT_METER, false);

        private FloatParameter prmDepthNight = new FloatParameter(
                Resources.PRM_DEPTH_NIGHT, 30.0f, Resources.UNIT_METER, false);

        ///////////////
        // Constructors
        ///////////////

        public TabTransport() {

            super(new GridBagLayout());
            setName(Resources.TAB_TRANSPORT);
            createUI();
            addListeners();
        }

        ////////////////////////////
        // Definition of the methods
        ////////////////////////////

        public void read(INIFile file) {

            /** dimension of simulation 2D vs 3D */
            dimension = file.getIntegerProperty(Structure.SECTION_TRANSPORT,
                                                Structure.DIMENSION);
            if (dimension == Constant.SIMU_2D) {
                /** 2D simulation */
                rdBtn2D.doClick();
            } else {
                /** 3D simulation */
                rdBtn3D.doClick();

                /** vertical dispersion */
                if (file.getBooleanProperty(Structure.SECTION_TRANSPORT,
                                            Structure.VDISP)) {
                    ckBoxDisperseV.doClick();
                }

                /** buoyancy */
                boolean bln = file.getBooleanProperty(Structure.
                        SECTION_TRANSPORT,
                        Structure.BUOYANCY);
                if (bln) {
                    ckBoxBuoyancy.setSelected(false);
                    ckBoxBuoyancy.doClick();
                    prmDensity.setValue(file.getDoubleProperty(Structure.
                            SECTION_TRANSPORT,
                            Structure.EGG_DENSITY).floatValue());
                    prmAgeBuoyancy.setValue(file.getDoubleProperty(Structure.
                            SECTION_TRANSPORT,
                            Structure.BUOYANCY_AGE_LIMIT).floatValue());
                }

                /** vertical migration */
                migration = file.getBooleanProperty(Structure.
                        SECTION_TRANSPORT,
                        Structure.MIGRATION);
                if (migration) {
                    ckBoxMigration.doClick();
                    prmDepthDay.setValue(file.getDoubleProperty(
                            Structure.SECTION_TRANSPORT,
                            Structure.MIGRATION_DEPTH_DAY).floatValue());
                    prmDepthNight.setValue(file.getDoubleProperty(
                            Structure.SECTION_TRANSPORT,
                            Structure.MIGRATION_DEPTH_NIGHT).floatValue());
                    if (!tabBiology.ckBoxGrowth.isSelected()) {
                        prmAgeMigration.setValue(file.getDoubleProperty(
                                Structure.SECTION_TRANSPORT,
                                Structure.MIGRATION_AGE_LIMIT).floatValue());
                    }
                }
            }

            /** horizontal dispersion */
            if (file.getBooleanProperty(Structure.SECTION_TRANSPORT,
                                        Structure.HDISP)) {
                ckBoxDisperseH.doClick();
            }
        }

        public void write(INIFile file) throws IOException {

            /** First check for relevancy */
            /*if (ckBoxBuoyancy.isSelected()
                && ckBoxMigration.isSelected()
                && !tabBiology.ckBoxGrowth.isSelected()) {
                if (prmAgeBuoyancy.getValue().floatValue() >
                    prmAgeMigration.getValue().floatValue()) {
                    throw new IOException(Resources.PRM_AGE_LIMIT_BUOYANCY
                                          + " should be inferior to "
                                          + Resources.PRM_AGE_LIMIT_MIGRATION);
                }
            }*/

            /** creates section transport */
            file.addSection(Structure.SECTION_TRANSPORT,
                            Structure.MAN_SECTION_TRANSPORT);

            /** dimension 2D vs 3D */
            file.setIntegerProperty(Structure.SECTION_TRANSPORT,
                                    Structure.DIMENSION,
                                    dimension,
                                    Structure.MAN_DIMENSION);

            /** horizontal dispersion */
            file.setBooleanProperty(Structure.SECTION_TRANSPORT,
                                    Structure.HDISP,
                                    ckBoxDisperseH.isSelected(),
                                    null);

            /** vertical dispersion */
            file.setBooleanProperty(Structure.SECTION_TRANSPORT,
                                    Structure.VDISP,
                                    ckBoxDisperseV.isSelected(),
                                    null);

            /** buoyancy */
            file.setBooleanProperty(Structure.SECTION_TRANSPORT,
                                    Structure.BUOYANCY,
                                    ckBoxBuoyancy.isSelected(),
                                    null);
            prmDensity.write(file,
                             Structure.SECTION_TRANSPORT,
                             Structure.EGG_DENSITY);
            prmAgeBuoyancy.write(file,
                                 Structure.SECTION_TRANSPORT,
                                 Structure.BUOYANCY_AGE_LIMIT);

            /** migration */
            file.setBooleanProperty(Structure.SECTION_TRANSPORT,
                                    Structure.MIGRATION,
                                    ckBoxMigration.isSelected(),
                                    null);
            prmAgeMigration.write(file,
                                  Structure.SECTION_TRANSPORT,
                                  Structure.MIGRATION_AGE_LIMIT);
            prmDepthDay.write(file,
                              Structure.SECTION_TRANSPORT,
                              Structure.MIGRATION_DEPTH_DAY);
            prmDepthNight.write(file,
                                Structure.SECTION_TRANSPORT,
                                Structure.MIGRATION_DEPTH_NIGHT);
        }

        public void createUI() {

            rdBtn2D = new JRadioButton(Resources.RD_BTN_2D, false);
            rdBtn3D = new JRadioButton(Resources.RD_BTN_3D, true);
            ButtonGroup btnGroup1 = new ButtonGroup();
            btnGroup1.add(rdBtn2D);
            btnGroup1.add(rdBtn3D);
            ckBoxAdvectH = new JCheckBox(Resources.CK_BOX_ADVECTH, true);
            ckBoxAdvectH.setEnabled(false);
            ckBoxAdvectV = new JCheckBox(Resources.CK_BOX_ADVECTV, true);
            ckBoxAdvectV.setEnabled(false);
            ckBoxDisperseH = new JCheckBox(Resources.CK_BOX_HDISP, false);
            ckBoxDisperseV = new JCheckBox(Resources.CK_BOX_VDISP, false);
            ckBoxBuoyancy = new JCheckBox(Resources.CK_BOX_BUOYANCY, false);
            ckBoxMigration = new JCheckBox(Resources.CK_BOX_MIGRATION, false);
            prmDepthDay.setFormatPolicy(1, 4, 1, 1);
            prmDepthNight.setFormatPolicy(1, 4, 1, 1);

            int j = 0;
            /** Adds the components in the tab */
            this.add(rdBtn2D, new GridBagConstraints(0, j, 1, 1, 50, 10,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(2, 5, 2, 5), 0, 0));
            this.add(rdBtn3D, new GridBagConstraints(1, j++, 1, 1, 50, 10,
                    GridBagConstraints.WEST,
                    GridBagConstraints.BOTH,
                    new Insets(2, 5, 2, 5), 0, 0));

            this.add(ckBoxAdvectH,
                     new GridBagConstraints(0, j++, 2, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.BOTH,
                                            new Insets(2, 5, 2, 5), 0, 0));
            this.add(ckBoxAdvectV,
                     new GridBagConstraints(0, j++, 2, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.BOTH,
                                            new Insets(2, 5, 2, 5), 0, 0));
            this.add(ckBoxDisperseH,
                     new GridBagConstraints(0, j++, 2, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.BOTH,
                                            new Insets(2, 5, 2, 5), 0, 0));
            this.add(ckBoxDisperseV,
                     new GridBagConstraints(0, j++, 2, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.BOTH,
                                            new Insets(2, 5, 2, 5), 0, 0));
            this.add(ckBoxBuoyancy,
                     new GridBagConstraints(0, j, 1, 1, 50, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.BOTH,
                                            new Insets(2, 5, 2, 5), 0, 0));

            this.add(prmAgeBuoyancy.createUI(),
                     new GridBagConstraints(1, j++, 1, 1, 50, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 30, 2, 5), 0, 0));

            this.add(prmDensity.createUI(),
                     new GridBagConstraints(0, j++, 2, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 30, 2, 5), 0, 0));
            this.add(ckBoxMigration,
                     new GridBagConstraints(0, j, 1, 1, 50, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.BOTH,
                                            new Insets(2, 5, 2, 5), 0, 0));

            this.add(prmAgeMigration.createUI(),
                     new GridBagConstraints(1, j++, 1, 1, 50, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 30, 2, 5), 0, 0));

            this.add(prmDepthDay.createUI(),
                     new GridBagConstraints(0, j++, 2, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 30, 2, 5), 0, 0));
            this.add(prmDepthNight.createUI(),
                     new GridBagConstraints(0, j++, 2, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 30, 2, 5), 0, 0));
        }

        public void addListeners() {

            rdBtn2D.addActionListener(this);
            rdBtn3D.addActionListener(this);
            ckBoxDisperseH.addActionListener(this);
            ckBoxDisperseV.addActionListener(this);
            ckBoxBuoyancy.addActionListener(this);
            ckBoxMigration.addActionListener(this);

            prmDensity.addValueListener(ConfigurationUI.this);
            prmAgeBuoyancy.addValueListener(ConfigurationUI.this);
            prmAgeMigration.addValueListener(ConfigurationUI.this);
            prmDepthNight.addValueListener(ConfigurationUI.this);
            prmDepthDay.addValueListener(ConfigurationUI.this);

            rdBtn2D.addMouseListener(ConfigurationUI.this);
            rdBtn3D.addMouseListener(ConfigurationUI.this);
            ckBoxDisperseH.addMouseListener(ConfigurationUI.this);
            ckBoxDisperseV.addMouseListener(ConfigurationUI.this);
            ckBoxBuoyancy.addMouseListener(ConfigurationUI.this);
            ckBoxMigration.addMouseListener(ConfigurationUI.this);
            prmDepthDay.addMouseListener(ConfigurationUI.this);
            prmDepthNight.addMouseListener(ConfigurationUI.this);
            prmDensity.addMouseListener(ConfigurationUI.this);
            prmAgeBuoyancy.addMouseListener(ConfigurationUI.this);
            prmAgeMigration.addMouseListener(ConfigurationUI.this);
        }

        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();
            boolean selected;

            /** rdBtn2D */
            if (source == rdBtn2D) {
                dimension = Constant.SIMU_2D;
                ckBoxAdvectV.setSelected(false);
                ckBoxDisperseV.setSelected(false);
                ckBoxDisperseV.setEnabled(false);
                ckBoxBuoyancy.setSelected(false);
                ckBoxBuoyancy.setEnabled(false);
                prmAgeBuoyancy.setEnabled(false);
                prmDensity.setEnabled(false);
                ckBoxMigration.setSelected(false);
                ckBoxMigration.setEnabled(false);
                prmAgeMigration.setEnabled(false);
                prmDepthDay.setEnabled(false);
                prmDepthNight.setEnabled(false);
                tabBiology.ckBoxLethalTp.setSelected(true);
                tabBiology.ckBoxLethalTp.doClick();
                tabBiology.ckBoxLethalTp.setEnabled(false);
                tabRelease.prmDepthMax.setEnabled(false);
                tabRelease.prmDepthMin.setEnabled(false);
                tabRelease.prmPatchThickness.setEnabled(false);
                tabVariable.refresh();
            }

            /** rdBtn3D */
            if (source == rdBtn3D) {
                dimension = Constant.SIMU_3D;
                ckBoxAdvectV.setSelected(true);
                ckBoxDisperseV.setEnabled(true);
                ckBoxBuoyancy.setEnabled(true);
                prmAgeBuoyancy.setEnabled(ckBoxBuoyancy.isSelected() && true);
                prmDensity.setEnabled(ckBoxBuoyancy.isSelected());
                ckBoxMigration.setEnabled(true);
                prmAgeMigration.setEnabled(ckBoxMigration.isSelected() && true);
                prmDepthDay.setEnabled(ckBoxMigration.isSelected());
                prmDepthNight.setEnabled(ckBoxMigration.isSelected());
                tabBiology.ckBoxLethalTp.setEnabled(true);
                tabRelease.prmDepthMax.setEnabled(true);
                tabRelease.prmDepthMin.setEnabled(true);
                tabRelease.prmPatchThickness.setEnabled(tabRelease.
                        ckBoxPatchiness.isSelected());
                tabVariable.refresh();
            }

            /** ckBoxMigration */
            if (source == ckBoxMigration) {
                selected = ckBoxMigration.isSelected();
                ckBoxDisperseV.setEnabled(!selected);
                ckBoxAdvectV.setSelected(!selected);
                prmDepthDay.setEnabled(selected);
                prmDepthNight.setEnabled(selected);
                if (selected == true) {
                    ckBoxDisperseV.setSelected(false);
                } else if (tabRelease.rdBtnZone.isSelected()) {
                    tabRelease.prmDepthMax.setEnabled(true);
                    tabRelease.prmDepthMin.setEnabled(true);
                    tabRelease.prmPatchThickness.setEnabled(tabRelease.
                            ckBoxPatchiness.isSelected());
                }
                prmAgeMigration.setEnabled(selected &&
                                           !tabBiology.ckBoxGrowth.
                                           isSelected());
            }

            /** ckBoxBuoyancy */
            if (source == ckBoxBuoyancy) {
                selected = ckBoxBuoyancy.isSelected();
                prmDensity.setEnabled(selected);
                prmAgeBuoyancy.setEnabled(selected
                                          && !tabBiology.ckBoxGrowth.isSelected());
            }

            /** Notifies the action */
            (ConfigurationUI.this).valueChanged(new ValueChangedEvent((
                    Component) e.getSource()));
        }

        public void setToolTips() {

            rdBtn2D.setToolTipText(Resources.TIP_BTN_2D);
            rdBtn3D.setToolTipText(Resources.TIP_BTN_3D);
            ckBoxDisperseH.setToolTipText(Resources.TIP_CK_BOX_HDISP);
            ckBoxDisperseV.setToolTipText(Resources.TIP_CK_BOX_VDISP);
            ckBoxBuoyancy.setToolTipText(Resources.TIP_CK_BOX_BUOYANCY);
            ckBoxMigration.setToolTipText(Resources.TIP_CK_BOX_MIGRATION);
            prmDepthDay.setToolTipText(Resources.TIP_PRM_DEPTH_DAY);
            prmDepthNight.setToolTipText(Resources.TIP_PRM_DEPTH_NIGHT);
            prmDensity.setToolTipText(Resources.TIP_PRM_EGG_DENSITY);
            prmAgeBuoyancy.setToolTipText(Resources.TIP_PRM_AGE_LIMIT_BUOY);
            prmAgeMigration.setToolTipText(Resources.
                                           TIP_PRM_AGE_LIMIT_MIGRATION);
        }
    }


////////////////////////////////////////////////////////////////////////////////
// Tab Release
////////////////////////////////////////////////////////////////////////////////
    private class TabRelease extends JPanel implements ITab {

        ///////////////////////////////
        // Declaration of the variables
        ///////////////////////////////

        ZoneEditor releaseZoneEditor;

        JRadioButton rdBtnZone, rdBtnFile;

        JCheckBox ckBoxPulsation, ckBoxPatchiness;

        IntegerParameter prmNbReleased = new IntegerParameter(
                Resources.PRM_NB_RELEASED, 1000, Resources.UNIT_NONE, true);
        DurationParameter prmReleaseDt = new DurationParameter(
                Resources.PRM_RELEASE_DT, "0000/00:00",
                Resources.UNIT_DURATION, false);
        IntegerParameter prmNbPatches = new IntegerParameter(
                Resources.PRM_NB_PATCHES, 0, Resources.UNIT_NONE, false);
        IntegerParameter prmPatchRadius = new IntegerParameter(
                Resources.PRM_RADIUS_PATCH, 0, Resources.UNIT_METER, false);
        IntegerParameter prmPatchThickness = new IntegerParameter(
                Resources.PRM_THICKNESS_PATCH, 0, Resources.UNIT_METER, false);
        IntegerParameter prmNbReleaseEvents = new IntegerParameter(
                Resources.PRM_NB_RELEASE_VENTS, 1, Resources.UNIT_NONE, false);
        IntegerParameter prmDepthMin = new IntegerParameter(
                Resources.PRM_DEPTH_RELEASING_MIN, 0, Resources.UNIT_METER, true);
        IntegerParameter prmDepthMax = new IntegerParameter(
                Resources.PRM_DEPTH_RELEASING_MAX, 50, Resources.UNIT_METER, true);

        ///////////////
        // Constructors
        ///////////////

        public TabRelease() {

            super(new GridBagLayout());
            setName(Resources.TAB_RELEASE);
            createUI();
            addListeners();
        }

        ////////////////////////////
        // Definition of the methods
        ////////////////////////////

        public void read(INIFile file) {

            boolean bln = (file.getIntegerProperty(Structure.
                    SECTION_RELEASE,
                    Structure.TYPE_RELEASE)
                           == Constant.RELEASE_ZONE);
            if (bln) {
                rdBtnZone.doClick();
                prmNbReleased.setValue(file.getIntegerProperty(Structure.
                        SECTION_RELEASE,
                        Structure.NB_PARTICLES));
                prmDepthMin.setValue(file.getIntegerProperty(Structure.
                        SECTION_RELEASE,
                        Structure.DEPTH_MIN));
                prmDepthMax.setValue(file.getIntegerProperty(Structure.
                        SECTION_RELEASE,
                        Structure.DEPTH_MAX));

                bln = file.getBooleanProperty(Structure.SECTION_RELEASE,
                                              Structure.PULSATION);
                if (bln) {
                    ckBoxPulsation.setSelected(false);
                    ckBoxPulsation.doClick();
                    prmNbReleaseEvents.setValue(file.getIntegerProperty(
                            Structure.SECTION_RELEASE,
                            Structure.NB_RELEASE_EVENTS));
                    prmReleaseDt.setEnabled(true);
                    prmReleaseDt.setValue(file.getLongProperty(
                            Structure.SECTION_RELEASE,
                            Structure.RELEASE_DT).longValue() * 1000L);
                }

                bln = file.getBooleanProperty(Structure.SECTION_RELEASE,
                                              Structure.PATCHINESS);
                if (bln) {
                    ckBoxPatchiness.setSelected(false);
                    ckBoxPatchiness.doClick();
                    prmNbPatches.setValue(file.getIntegerProperty(
                            Structure.SECTION_RELEASE,
                            Structure.NB_PATCHES));
                    prmPatchRadius.setValue(file.getIntegerProperty(
                            Structure.SECTION_RELEASE,
                            Structure.RADIUS_PATCH));
                    prmPatchThickness.setValue(file.getIntegerProperty(
                            Structure.SECTION_RELEASE,
                            Structure.THICK_PATCH));
                }
            } else {
                rdBtnFile.doClick();
            }
        }

        public void write(INIFile file) throws IOException {

            /** creates section release */
            file.addSection(Structure.SECTION_RELEASE,
                            Structure.MAN_SECTION_RELEASE);

            /** type Release */
            int typeRelease = Constant.RELEASE_ZONE;
            if (rdBtnFile.isSelected()) {
                String pathname = tabIO.txtDrifterPathname.getText().trim();
                if (!pathname.isEmpty()) {
                    if (pathname.endsWith("nc")) {
                        typeRelease = Constant.RELEASE_NCFILE;
                    } else {
                        typeRelease = Constant.RELEASE_TXTFILE;
                    }
                } else {
                    throw new IOException("Drifter pathname is not specified");
                }
            }
            file.setIntegerProperty(Structure.SECTION_RELEASE,
                                    Structure.TYPE_RELEASE,
                                    typeRelease,
                                    Structure.MAN_TYPE_RELEASE);

            /** number particles released */
            prmNbReleased.write(file,
                                Structure.SECTION_RELEASE,
                                Structure.NB_PARTICLES);

            /** release depth */
            prmDepthMin.write(file,
                              Structure.SECTION_RELEASE,
                              Structure.DEPTH_MIN);
            prmDepthMax.write(file,
                              Structure.SECTION_RELEASE,
                              Structure.DEPTH_MAX);

            /** pulsation */
            file.setBooleanProperty(Structure.SECTION_RELEASE,
                                    Structure.PULSATION,
                                    ckBoxPulsation.isSelected(),
                                    null);
            prmNbReleaseEvents.write(file,
                                     Structure.SECTION_RELEASE,
                                     Structure.NB_RELEASE_EVENTS);
            prmReleaseDt.write(file,
                               Structure.SECTION_RELEASE,
                               Structure.RELEASE_DT);

            /** Patchiness */
            file.setBooleanProperty(Structure.SECTION_RELEASE,
                                    Structure.PATCHINESS,
                                    ckBoxPatchiness.isSelected(),
                                    null);
            prmNbPatches.write(file,
                               Structure.SECTION_RELEASE,
                               Structure.NB_PATCHES);
            prmPatchRadius.write(file,
                                 Structure.SECTION_RELEASE,
                                 Structure.RADIUS_PATCH);
            prmPatchThickness.write(file,
                                    Structure.SECTION_RELEASE,
                                    Structure.THICK_PATCH);

            /** Zones */
            releaseZoneEditor.write(file,
                                    Structure.SECTION_RELEASE_ZONE,
                                    Structure.MAN_RELEASE_ZONE);
        }

        /**
         *
         */
        public void createUI() {

            releaseZoneEditor = new ZoneEditor(Constant.RELEASE);

            ButtonGroup grp = new ButtonGroup();
            rdBtnZone = new JRadioButton(Resources.RD_BTN_RELEASE_ZONE, true);
            rdBtnFile = new JRadioButton(Resources.RD_BTN_RELEASE_FILE, false);
            grp.add(rdBtnZone);
            grp.add(rdBtnFile);

            ckBoxPulsation = new JCheckBox(Resources.CK_BOX_PULSATION, false);
            ckBoxPatchiness = new JCheckBox(Resources.CK_BOX_PATCHINESS, false);

            int j = 0;
            this.add(rdBtnZone,
                     new GridBagConstraints(0, j, 1, 1, 10, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 5, 2, 5), 0, 0));
            this.add(rdBtnFile,
                     new GridBagConstraints(1, j++, 1, 1, 10, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 5, 2, 5), 0, 0));
            this.add(prmNbReleased.createUI(),
                     new GridBagConstraints(0, j, 1, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 5, 2, 5), 0, 0));
            this.add(prmDepthMin.createUI(),
                     new GridBagConstraints(1, j++, 1, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 5, 2, 5), 0, 0));
            this.add(prmDepthMax.createUI(),
                     new GridBagConstraints(1, j++, 1, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 5, 2, 5), 0, 0));
            this.add(ckBoxPulsation,
                     new GridBagConstraints(0, j++, 2, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 5, 2, 5), 0, 0));
            this.add(prmNbReleaseEvents.createUI(),
                     new GridBagConstraints(0, j++, 2, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 30, 5, 5), 0, 0));
            this.add(prmReleaseDt.createUI(),
                     new GridBagConstraints(0, j++, 2, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 30, 5, 5), 0, 0));
            this.add(ckBoxPatchiness,
                     new GridBagConstraints(0, j++, 2, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 5, 2, 5), 0, 0));
            this.add(prmNbPatches.createUI(),
                     new GridBagConstraints(0, j, 1, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 30, 5, 5), 0, 0));
            this.add(prmPatchRadius.createUI(),
                     new GridBagConstraints(1, j++, 1, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 30, 5, 5), 0, 0));
            this.add(prmPatchThickness.createUI(),
                     new GridBagConstraints(1, j++, 1, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 30, 5, 5), 0, 0));
            this.add(releaseZoneEditor,
                     new GridBagConstraints(0, j++, 2, 1, 100,
                                            10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 5, 5, 5), 0, 0));
        }

        public void addListeners() {

            rdBtnZone.addActionListener(this);
            rdBtnFile.addActionListener(this);
            ckBoxPulsation.addActionListener(this);
            ckBoxPatchiness.addActionListener(this);

            prmNbReleased.addValueListener(ConfigurationUI.this);
            prmDepthMin.addValueListener(ConfigurationUI.this);
            prmDepthMax.addValueListener(ConfigurationUI.this);
            prmNbPatches.addValueListener(ConfigurationUI.this);
            prmPatchRadius.addValueListener(ConfigurationUI.this);
            prmPatchThickness.addValueListener(ConfigurationUI.this);
            prmNbReleaseEvents.addValueListener(ConfigurationUI.this);
            prmReleaseDt.addValueListener(ConfigurationUI.this);
            releaseZoneEditor.addValueListener(ConfigurationUI.this);

            ckBoxPatchiness.addMouseListener(ConfigurationUI.this);
            ckBoxPulsation.addMouseListener(ConfigurationUI.this);
            prmDepthMax.addMouseListener(ConfigurationUI.this);
            prmDepthMin.addMouseListener(ConfigurationUI.this);
            prmNbPatches.addMouseListener(ConfigurationUI.this);
            prmNbReleased.addMouseListener(ConfigurationUI.this);
            prmNbReleaseEvents.addMouseListener(ConfigurationUI.this);
            prmPatchRadius.addMouseListener(ConfigurationUI.this);
            prmPatchThickness.addMouseListener(ConfigurationUI.this);
            prmReleaseDt.addMouseListener(ConfigurationUI.this);
            rdBtnFile.addMouseListener(ConfigurationUI.this);
            rdBtnZone.addMouseListener(ConfigurationUI.this);

        }

        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();

            /** rdBtnZone */
            if (source == rdBtnZone) {
                prmNbReleased.setEnabled(true);
                ckBoxPulsation.setEnabled(true);
                prmReleaseDt.setEnabled(ckBoxPulsation.isSelected());
                prmNbReleaseEvents.setEnabled(ckBoxPulsation.isSelected());
                prmDepthMin.setEnabled(!tabTransport.rdBtn2D.isSelected());
                prmDepthMax.setEnabled(prmDepthMin.isEnabled());
                releaseZoneEditor.setEnabled(true);
                tabIO.btnDrifterPathname.setEnabled(false);
                tabIO.txtDrifterPathname.setEnabled(false);
                ckBoxPatchiness.setEnabled(true);
                prmNbPatches.setEnabled(ckBoxPatchiness.isSelected());
                prmPatchRadius.setEnabled(ckBoxPatchiness.isSelected());
                prmPatchThickness.setEnabled(ckBoxPatchiness.isSelected()
                                             &&
                                             !tabTransport.rdBtn2D.
                                             isSelected());
                if (file != null &&
                    (releaseZoneEditor.length() == 0)) {
                    releaseZoneEditor.read(new INIFile(file.toString()));
                }
            }

            /** rdBtnFile */
            if (source == rdBtnFile) {
                prmNbReleased.setEnabled(false);
                prmNbPatches.setEnabled(false);
                prmReleaseDt.setEnabled(false);
                prmNbReleaseEvents.setEnabled(false);
                prmDepthMin.setEnabled(false);
                prmDepthMax.setEnabled(false);
                prmNbPatches.setEnabled(false);
                prmPatchRadius.setEnabled(false);
                prmPatchThickness.setEnabled(false);
                ckBoxPatchiness.setEnabled(false);
                ckBoxPulsation.setEnabled(false);
                releaseZoneEditor.setEnabled(false);
                tabIO.btnDrifterPathname.setEnabled(true);
                tabIO.txtDrifterPathname.setEnabled(true);
            }

            /** ckBoxPulstation */
            if (source == ckBoxPulsation) {
                prmReleaseDt.setEnabled(ckBoxPulsation.isSelected());
                prmNbReleaseEvents.setEnabled(ckBoxPulsation.isSelected());
            }

            /** ckBoxPatchiness */
            if (source == ckBoxPatchiness) {
                prmNbPatches.setEnabled(ckBoxPatchiness.isSelected());
                prmPatchRadius.setEnabled(ckBoxPatchiness.isSelected());
                prmPatchThickness.setEnabled(ckBoxPatchiness.isSelected() &&
                                             !tabTransport.rdBtn2D.
                                             isSelected());
            }

            /** Notifies the action */
            (ConfigurationUI.this).valueChanged(new ValueChangedEvent((
                    Component) e.getSource()));
        }

        public void setToolTips() {

            ckBoxPatchiness.setToolTipText(Resources.TIP_CK_BOX_PATCHINESS);
            ckBoxPulsation.setToolTipText(Resources.TIP_CK_BOX_PULSATION);
            prmDepthMax.setToolTipText(Resources.TIP_PRM_DEPTH_MAX);
            prmDepthMin.setToolTipText(Resources.TIP_PRM_DEPTH_MAX);
            prmNbPatches.setToolTipText(Resources.TIP_PRM_NB_PACTHES);
            prmNbReleased.setToolTipText(Resources.TIP_PRM_NB_RELEASED);
            prmNbReleaseEvents.setToolTipText(Resources.TIP_PRM_NB_EVENTS);
            prmPatchRadius.setToolTipText(Resources.TIP_PRM_RADIUS_PATCH);
            prmPatchThickness.setToolTipText(Resources.TIP_PRM_THICKNESS_PATCH);
            prmReleaseDt.setToolTipText(Resources.TIP_PRM_RELEASE_DT);
            rdBtnFile.setToolTipText(Resources.TIP_RD_BTN_RELEASE_FILE);
            rdBtnZone.setToolTipText(Resources.TIP_RD_BTN_RELEASE_ZONE);
        }
    }


////////////////////////////////////////////////////////////////////////////////
// Tab Recruitment
////////////////////////////////////////////////////////////////////////////////
    private class TabRecruitment extends JPanel implements ITab {

        ///////////////////////////////
        // Declaration of the variables
        ///////////////////////////////

        JRadioButton rdBtnRecruitNone, rdBtnRecruitAge, rdBtnRecruitLength;
        
        JCheckBox ckBoxDepthRecruit;

        ZoneEditor recruitmentZoneEditor;

        FloatParameter prmAgeRecruit = new FloatParameter(
                Resources.PRM_RECRUIT_AGE, 14.0f, Resources.UNIT_DAY, false);

        FloatParameter prmLengthRecruit = new FloatParameter(
                Resources.PRM_RECRUIT_LENGTH, 14.0f,
                Resources.UNIT_MILLIMETER, false);

        FloatParameter prmDurationRecruit = new FloatParameter(
                Resources.PRM_RECRUIT_DURATION_MIN, 0, Resources.UNIT_DAY, false);
        
        FloatParameter prmMinDepthRecruit = new FloatParameter(
                Resources.PRM_RECRUIT_MIN_DEPTH, 0,
                Resources.UNIT_METER, false);
        
        FloatParameter prmMaxDepthRecruit = new FloatParameter(
                Resources.PRM_RECRUIT_MAX_DEPTH, 12000,
                Resources.UNIT_METER, false);
        
        JCheckBox ckBoxStopMoving;

        ///////////////
        // Constructors
        ///////////////

        public TabRecruitment() {

            super(new GridBagLayout());
            setName(Resources.TAB_RECRUITMENT);
            createUI();
            addListeners();
        }

        ////////////////////////////
        // Definition of the methods
        ////////////////////////////

        public void read(INIFile file) {

            int typeRecruitment = file.getIntegerProperty(
                    Structure.SECTION_RECRUIT,
                    Structure.RECRUIT);

            switch (typeRecruitment) {

            case Constant.NONE:
                rdBtnRecruitNone.doClick();
                break;

            case Constant.RECRUIT_AGE:
                rdBtnRecruitAge.doClick();
                prmAgeRecruit.setValue(file.getDoubleProperty(
                        Structure.SECTION_RECRUIT,
                        Structure.AGE_RECRUIT).floatValue());
                prmDurationRecruit.setValue(file.getDoubleProperty(
                        Structure.SECTION_RECRUIT,
                        Structure.DURATION_RECRUIT).floatValue());
                break;

            case Constant.RECRUIT_LENGTH:
                rdBtnRecruitLength.doClick();
                prmLengthRecruit.setValue(file.getDoubleProperty(
                        Structure.SECTION_RECRUIT,
                        Structure.LENGTH_RECRUIT).floatValue());
                prmDurationRecruit.setValue(file.getDoubleProperty(
                        Structure.SECTION_RECRUIT,
                        Structure.DURATION_RECRUIT).floatValue());
                break;
            }
            
            if (typeRecruitment != Constant.NONE) {
                prmDurationRecruit.setValue(file.getDoubleProperty(
                        Structure.SECTION_RECRUIT,
                        Structure.DURATION_RECRUIT).floatValue());
                boolean depthCriterion = file.getBooleanProperty(
                    Structure.SECTION_RECRUIT,
                    Structure.DEPTH_RECRUIT);
                if (depthCriterion) {
                    ckBoxDepthRecruit.doClick();
                    prmMinDepthRecruit.setValue(file.getDoubleProperty(
                        Structure.SECTION_RECRUIT,
                        Structure.DEPTH_MIN).floatValue());
                    prmMaxDepthRecruit.setValue(file.getDoubleProperty(
                        Structure.SECTION_RECRUIT,
                        Structure.DEPTH_MAX).floatValue());
                }
                boolean stopMoving = file.getBooleanProperty(
                        Structure.SECTION_RECRUIT,
                        Structure.STOP);
                ckBoxStopMoving.setSelected(stopMoving);
            }
        }

        public void write(INIFile file) {

            /** Creates section recruitment */
            file.addSection(Structure.SECTION_RECRUIT,
                            Structure.MAN_RECRUIT);

            /** type recruitment */
            int typeRecruitment = Constant.NONE;
            if (rdBtnRecruitAge.isSelected()) {
                typeRecruitment = Constant.RECRUIT_AGE;
            } else if (rdBtnRecruitLength.isSelected()) {
                typeRecruitment = Constant.RECRUIT_LENGTH;
            }
            file.setIntegerProperty(Structure.SECTION_RECRUIT,
                                    Structure.RECRUIT,
                                    typeRecruitment,
                                    Structure.MAN_TYPE_RECRUIT);

            /** recruitment criteria */
            prmAgeRecruit.write(file,
                                Structure.SECTION_RECRUIT,
                                Structure.AGE_RECRUIT);
            prmLengthRecruit.write(file,
                                   Structure.SECTION_RECRUIT,
                                   Structure.LENGTH_RECRUIT);
            prmDurationRecruit.write(file,
                                     Structure.SECTION_RECRUIT,
                                     Structure.DURATION_RECRUIT);
            
            /** Depth criteria */
            file.setBooleanProperty(Structure.SECTION_RECRUIT,
                    Structure.DEPTH_RECRUIT,
                    ckBoxDepthRecruit.isSelected(),
                    null);
            prmMinDepthRecruit.write(file,
                    Structure.SECTION_RECRUIT,
                    Structure.DEPTH_MIN);
            prmMaxDepthRecruit.write(file,
                    Structure.SECTION_RECRUIT,
                    Structure.DEPTH_MAX);
            
            /** Stop moving */
            file.setBooleanProperty(Structure.SECTION_RECRUIT,
                    Structure.STOP,
                    ckBoxStopMoving.isSelected(),
                    null);

            /** Zones */
            recruitmentZoneEditor.write(file,
                                        Structure.SECTION_RECRUITMENT_ZONE,
                                        Structure.MAN_RECRUITMENT_ZONE);
        }

        /**
         *
         */
        public void createUI() {

            ButtonGroup grpRecruit = new ButtonGroup();
            rdBtnRecruitNone = new JRadioButton(Resources.
                                                RD_BTN_RECRUIT_NONE, true);
            rdBtnRecruitAge = new JRadioButton(Resources.RD_BTN_RECRUIT_AGE, false);
            recruitmentZoneEditor = new ZoneEditor(Constant.RECRUITMENT);
            recruitmentZoneEditor.setEnabled(false);
            rdBtnRecruitLength = new JRadioButton(Resources.
                                                  RD_BTN_RECRUIT_LENGTH, false);
            rdBtnRecruitLength.setEnabled(false);
            grpRecruit.add(rdBtnRecruitNone);
            grpRecruit.add(rdBtnRecruitAge);
            grpRecruit.add(rdBtnRecruitLength);
            
            ckBoxDepthRecruit = new JCheckBox(Resources.CK_BOX_RECRUIT_DEPTH,
                    false);
            ckBoxDepthRecruit.setEnabled(false);
            
            ckBoxStopMoving = new JCheckBox(Resources.CK_BOX_STOP_MOVING, false);
            ckBoxStopMoving.setEnabled(false);

            this.add(rdBtnRecruitNone,
                     new GridBagConstraints(0, 0, 1, 1, 30, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 5, 2, 5), 0, 0));
            this.add(rdBtnRecruitAge,
                     new GridBagConstraints(1, 0, 1, 1, 30, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 5, 2, 5), 0, 0));
            this.add(rdBtnRecruitLength,
                     new GridBagConstraints(2, 0, 1, 1, 30, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 5, 2, 5), 0, 0));

            this.add(prmAgeRecruit.createUI(),
                     new GridBagConstraints(0, 1, 3, 1, 90, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 30, 5, 5), 0, 0));
            this.add(prmLengthRecruit.createUI(),
                     new GridBagConstraints(0, 2, 3, 1, 90, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 30, 2, 5), 0, 0));
            this.add(prmDurationRecruit.createUI(),
                     new GridBagConstraints(0, 3, 3, 1, 90, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 30, 2, 5), 0, 0));
            
            this.add(ckBoxDepthRecruit,
                     new GridBagConstraints(0, 4, 3, 1, 90, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 30, 2, 5), 0, 0));
            
            this.add(prmMinDepthRecruit.createUI(),
                     new GridBagConstraints(0, 5, 3, 1, 90, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 55, 2, 5), 0, 0));
            
            this.add(prmMaxDepthRecruit.createUI(),
                     new GridBagConstraints(0, 6, 3, 1, 90, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 55, 2, 5), 0, 0));
            this.add(ckBoxStopMoving,
                     new GridBagConstraints(0, 7, 3, 1, 90, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 30, 2, 5), 0, 0));

            this.add(recruitmentZoneEditor,
                     new GridBagConstraints(0, 8, 3, 1, 90,
                                            40,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(2, 5, 2, 5), 0, 0));
        }

        public void addListeners() {

            rdBtnRecruitNone.addActionListener(this);
            rdBtnRecruitAge.addActionListener(this);
            rdBtnRecruitLength.addActionListener(this);
            ckBoxDepthRecruit.addActionListener(this);
            ckBoxStopMoving.addActionListener(this);

            prmAgeRecruit.addValueListener(ConfigurationUI.this);
            prmLengthRecruit.addValueListener(ConfigurationUI.this);
            prmDurationRecruit.addValueListener(ConfigurationUI.this);
            prmMinDepthRecruit.addValueListener(ConfigurationUI.this);
            prmMaxDepthRecruit.addValueListener(ConfigurationUI.this);
            recruitmentZoneEditor.addValueListener(ConfigurationUI.this);

            prmAgeRecruit.addMouseListener(ConfigurationUI.this);
            prmDurationRecruit.addMouseListener(ConfigurationUI.this);
            prmLengthRecruit.addMouseListener(ConfigurationUI.this);
            rdBtnRecruitNone.addMouseListener(ConfigurationUI.this);
            rdBtnRecruitAge.addMouseListener(ConfigurationUI.this);
            rdBtnRecruitLength.addMouseListener(ConfigurationUI.this);
            ckBoxDepthRecruit.addMouseListener(ConfigurationUI.this);
            prmMinDepthRecruit.addMouseListener(ConfigurationUI.this);
            prmMaxDepthRecruit.addMouseListener(ConfigurationUI.this);

        }

        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();

            if (source == tabBiology.ckBoxGrowth) {
                rdBtnRecruitLength.setEnabled(tabBiology.ckBoxGrowth.
                                              isSelected());
                if (rdBtnRecruitLength.isSelected()) {
                    rdBtnRecruitNone.doClick();
                }
            }

            if (source == rdBtnRecruitNone) {
                prmAgeRecruit.setEnabled(false);
                prmLengthRecruit.setEnabled(false);
                prmDurationRecruit.setEnabled(false);
                recruitmentZoneEditor.setEnabled(false);
                ckBoxDepthRecruit.setSelected(false);
                prmMinDepthRecruit.setEnabled(false);
                prmMaxDepthRecruit.setEnabled(false);
                ckBoxDepthRecruit.setEnabled(false);
                ckBoxStopMoving.setSelected(false);
                ckBoxStopMoving.setEnabled(false);
                
            }
            if (source == rdBtnRecruitAge) {
                prmAgeRecruit.setEnabled(true);
                prmLengthRecruit.setEnabled(false);
                prmDurationRecruit.setEnabled(true);
                recruitmentZoneEditor.setEnabled(true);
                ckBoxDepthRecruit.setEnabled(true);
                ckBoxStopMoving.setEnabled(true);
                if (file != null
                    && (recruitmentZoneEditor.length() == 0)) {
                    recruitmentZoneEditor.read(new INIFile(file.toString()));
                }
            }
            if (source == rdBtnRecruitLength) {
                prmAgeRecruit.setEnabled(false);
                prmLengthRecruit.setEnabled(true);
                prmDurationRecruit.setEnabled(true);
                recruitmentZoneEditor.setEnabled(true);
                ckBoxDepthRecruit.setEnabled(true);
                ckBoxStopMoving.setEnabled(true);
                if (file != null
                    && (recruitmentZoneEditor.length() == 0)) {
                    recruitmentZoneEditor.read(new INIFile(file.toString()));
                }
            }
            
            if (source == ckBoxDepthRecruit) {
                boolean selected = ckBoxDepthRecruit.isSelected();
                prmMinDepthRecruit.setEnabled(selected);
                prmMaxDepthRecruit.setEnabled(selected);
            }

            /** Notifies the action */
            (ConfigurationUI.this).valueChanged(new ValueChangedEvent((
                    Component) e.getSource()));
        }

        public void setToolTips() {

            prmAgeRecruit.setToolTipText(Resources.TIP_PRM_RECRUIT_AGE);
            prmDurationRecruit.setToolTipText(Resources.
                                              TIP_RECRUIT_DURATION_MIN);
            prmLengthRecruit.setToolTipText(Resources.TIP_PRM_RECRUIT_LENGTH);
            rdBtnRecruitNone.setToolTipText(Resources.TIP_RD_BTN_RECRUIT_NONE);
            rdBtnRecruitAge.setToolTipText(Resources.TIP_RD_BTN_RECRUIT_AGE);
            rdBtnRecruitLength.setToolTipText(Resources.
                                              TIP_RD_BTN_RECRUIT_LENGTH);
            ckBoxDepthRecruit.setToolTipText(Resources.
                    TIP_CK_BOX_RECRUIT_DEPTH);
            prmMinDepthRecruit.setToolTipText(Resources.
                    TIP_PRM_RECRUIT_MIN_DEPTH);
            prmMaxDepthRecruit.setToolTipText(Resources.
                    TIP_PRM_RECRUIT_MAX_DEPTH);
            ckBoxStopMoving.setToolTipText(Resources.TIP_CK_BOX_STOP_MOVING);
        }
    }


////////////////////////////////////////////////////////////////////////////////
// Tab Biology
////////////////////////////////////////////////////////////////////////////////
    private class TabBiology extends JPanel implements ITab {

        ///////////////////////////////
        // Declaration of the variables
        ///////////////////////////////

        private JCheckBox ckBoxGrowth, ckBoxPlankton, ckBoxLethalTp;

        private FloatParameter prmLethalTpEgg, prmLethalTpLarva;

        private JLabel lblGrowth, lblPlankton;

        ///////////////
        // Constructors
        ///////////////

        public TabBiology() {

            super(new GridBagLayout());
            setName(Resources.TAB_BIOLOGY);
            createUI();
            addListeners();
        }

        ////////////////////////////
        // Definition of the methods
        ////////////////////////////

        public void read(INIFile file) {

            boolean bln = file.getBooleanProperty(Structure.SECTION_BIO,
                                                  Structure.GROWTH);
            if (bln) {
                ckBoxGrowth.setSelected(false);
                ckBoxGrowth.doClick();

                bln = file.getBooleanProperty(Structure.SECTION_BIO,
                                              Structure.PLANKTON);
                if (bln) {
                    ckBoxPlankton.setSelected(false);
                    ckBoxPlankton.doClick();
                }
            }

            bln = file.getBooleanProperty(Structure.SECTION_BIO,
                                          Structure.LETHAL_TP);
            if (bln) {
                ckBoxLethalTp.setSelected(false);
                ckBoxLethalTp.doClick();
                prmLethalTpEgg.setValue(file.getDoubleProperty(Structure.
                        SECTION_BIO, Structure.LETHAL_TP_EGG).
                                        floatValue());
                if (ckBoxGrowth.isSelected()) {
                    prmLethalTpLarva.setValue(file.getDoubleProperty(Structure.
                            SECTION_BIO, Structure.LETHAL_TP_LARVA).
                                              floatValue());
                }
            }

        }

        /**
         *
         * @param file INIFile
         */
        public void write(INIFile file) {

            /** creates section bio */
            file.addSection(Structure.SECTION_BIO, Structure.MAN_BIO);

            /** growth */
            file.setBooleanProperty(Structure.SECTION_BIO,
                                    Structure.GROWTH,
                                    ckBoxGrowth.isSelected(),
                                    null);

            /** prey limitation */
            file.setBooleanProperty(Structure.SECTION_BIO,
                                    Structure.PLANKTON,
                                    ckBoxPlankton.isSelected(),
                                    null);

            /** lethal temperature */
            file.setBooleanProperty(Structure.SECTION_BIO,
                                    Structure.LETHAL_TP,
                                    ckBoxLethalTp.isSelected(),
                                    null);
            prmLethalTpEgg.write(file,
                                 Structure.SECTION_BIO,
                                 Structure.LETHAL_TP_EGG);
            prmLethalTpLarva.write(file,
                                   Structure.SECTION_BIO,
                                   Structure.LETHAL_TP_LARVA);
        }

        public void createUI() {

            ckBoxGrowth = new JCheckBox(Resources.CK_BOX_GROWTH, false);
            ckBoxPlankton = new JCheckBox(Resources.CK_BOX_PLAKTON, false);
            ckBoxPlankton.setEnabled(false);
            ckBoxLethalTp = new JCheckBox(Resources.CK_BOX_LETHAL_TP, false);

            prmLethalTpEgg = new FloatParameter(
                    Resources.PRM_LETHAL_TP_EGG, 10.0f,
                    Resources.UNIT_CELSIUS, false);
            prmLethalTpLarva = new FloatParameter(
                    Resources.PRM_LETHAL_TP_LARVAE, 10.0f,
                    Resources.UNIT_CELSIUS, false);

            String text = Resources.TEXT_GROWTH;
            lblGrowth = new JLabel(text);
            lblGrowth.setForeground(Color.LIGHT_GRAY);
            text = Resources.TEXT_PLANKTON;
            lblPlankton = new JLabel(text);
            lblPlankton.setForeground(Color.LIGHT_GRAY);

            int j = 0;
            this.add(ckBoxGrowth,
                     new GridBagConstraints(0, j++, 1, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 5, 5, 5), 0, 0));
            this.add(lblGrowth,
                     new GridBagConstraints(0, j++, 1, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 30, 5, 5), 0, 0));
            this.add(ckBoxPlankton,
                     new GridBagConstraints(0, j++, 1, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 5, 5, 5), 0, 0));
            this.add(lblPlankton,
                     new GridBagConstraints(0, j++, 1, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 30, 5, 5), 0, 0));
            this.add(ckBoxLethalTp,
                     new GridBagConstraints(0, j++, 1, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 5, 5, 5), 0, 0));
            this.add(prmLethalTpEgg.createUI(),
                     new GridBagConstraints(0, j++, 1, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 30, 5, 5), 0, 0));
            this.add(prmLethalTpLarva.createUI(),
                     new GridBagConstraints(0, j++, 1, 1, 100, 10,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 30, 5, 5), 0, 0));

        }

        public void addListeners() {

            /** action listener */
            ckBoxGrowth.addActionListener(this);
            ckBoxPlankton.addActionListener(this);
            ckBoxLethalTp.addActionListener(this);

            /** value listener */
            prmLethalTpEgg.addValueListener(ConfigurationUI.this);
            prmLethalTpLarva.addValueListener(ConfigurationUI.this);

            /** mouse listener */
            ckBoxGrowth.addMouseListener(ConfigurationUI.this);
            ckBoxPlankton.addMouseListener(ConfigurationUI.this);
            ckBoxLethalTp.addMouseListener(ConfigurationUI.this);
            prmLethalTpEgg.addMouseListener(ConfigurationUI.this);
            prmLethalTpLarva.addMouseListener(ConfigurationUI.this);
        }

        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();
            boolean selected;

            /** ckBoxLethalTp */
            if (source == ckBoxLethalTp) {
                selected = ckBoxLethalTp.isSelected();
                prmLethalTpEgg.setEnabled(selected);
                prmLethalTpLarva.setEnabled(selected &&
                                            ckBoxGrowth.isSelected());
            }

            /** ckBoxGrowth */
            if (source == ckBoxGrowth) {
                selected = ckBoxGrowth.isSelected();
                if (selected) {
                    lblGrowth.setForeground(Color.black);
                } else {
                    lblGrowth.setForeground(Color.lightGray);
                }
                ckBoxPlankton.setEnabled(selected);
                ckBoxPlankton.setSelected(false);
                tabTransport.prmAgeBuoyancy.setEnabled(tabTransport.
                        ckBoxBuoyancy.
                        isSelected()
                        && !ckBoxGrowth.isSelected());
                tabTransport.prmAgeMigration.setEnabled(tabTransport.
                        ckBoxMigration.isSelected()
                        && !ckBoxGrowth.isSelected());
                prmLethalTpLarva.setEnabled(ckBoxLethalTp.isSelected()
                                            && ckBoxGrowth.isSelected());
                tabRecruitment.actionPerformed(e);
            }

            /** ckBoxPlankton */
            if (source == ckBoxPlankton) {
                if (ckBoxPlankton.isSelected()) {
                    lblPlankton.setForeground(Color.black);
                } else {
                    lblPlankton.setForeground(Color.lightGray);
                }

            }

            /** Notifies the action */
            (ConfigurationUI.this).valueChanged(new ValueChangedEvent((
                    Component) e.getSource()));
        }

        public void setToolTips() {

            ckBoxGrowth.setToolTipText(Resources.TIP_CK_BOX_GROWTH);
            ckBoxPlankton.setToolTipText(Resources.TIP_CK_BOX_PLANKTON);
            prmLethalTpEgg.setToolTipText(Resources.TIP_PRM_LETHAL_TP_EGG);
            prmLethalTpLarva.setToolTipText(Resources.TIP_PRM_LETHAL_TP_LARVAE);
        }
    }


////////////////////////////////////////////////////////////////////////////////
// Tab Variables
////////////////////////////////////////////////////////////////////////////////
    private class TabVariable extends JPanel implements ITab {

        ///////////////////////////////
        // Declaration of the variables
        ///////////////////////////////

        private JTableCbBox table;

        private JComboBox cbBoxDimension, cbBoxVariable, cbBoxAttribute;

        private JButton btnApply, btnDefault, btnReset;

        ///////////////
        // Constructors
        ///////////////

        public TabVariable() {

            super(new GridBagLayout());
            setName(Resources.TAB_VARIABLE);
            createUI();
            addListeners();
        }

        ////////////////////////////
        // Definition of the methods
        ////////////////////////////

        public void read(INIFile file) {

            NCField[] variables = NCField.values();

            for (int i = 0; i < variables.length; i++) {
                if (variables[i].isRequired(model + dimension)) {
                    variables[i].setName(model,
                                         file.getStringProperty(
                                                 Structure.SECTION_VARIABLE,
                                                 variables[i].name()));
                    table.getModel().setValueAt(variables[i].getName(
                            model),
                                                i, 1);
                }
            }
        }

        public void write(INIFile file) {

            /** create section variable */
            file.addSection(Structure.SECTION_VARIABLE,
                            Structure.MAN_SECTION_VARIABLE);

            /** variable names */
            NCField[] variable = NCField.values();
            int config = model + dimension;
            for (int i = 0; i < variable.length; i++) {
                String name = variable[i].isRequired(config)
                              ? variable[i].getName(model)
                              : "null";
                file.setStringProperty(Structure.SECTION_VARIABLE,
                                       variable[i].name(),
                                       name,
                                       null);
            }
            variable = null;
        }

        public void createUI() {

            this.setOpaque(true);
            table = new JTableCbBox(new MyTableModel());
            table.setDefaultRenderer(Object.class, new MyCellRenderer());
            table.setFillsViewportHeight(true);

            //Create the scroll pane and add the table to it.
            JScrollPane scrollPane = new JScrollPane(table);

            //Fiddle with the name column's cell editors/renderers.
            setUpNameColumn(null);
            initColumnSizes(table);

            btnApply = new JButton(Resources.BTN_APPLY);
            btnApply.setEnabled(false);
            btnDefault = new JButton(Resources.BTN_DEFAULT);
            btnReset = new JButton(Resources.BTN_RESET);

            this.add(scrollPane, new GridBagConstraints(0, 0, 3, 1, 100, 90,
                    GridBagConstraints.NORTH,
                    GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.add(btnApply, new GridBagConstraints(0, 1, 1, 1, 33, 10,
                    GridBagConstraints.NORTHEAST,
                    GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.add(btnReset, new GridBagConstraints(1, 1, 1, 1, 33, 10,
                    GridBagConstraints.NORTH,
                    GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 5), 0, 0));
            this.add(btnDefault, new GridBagConstraints(2, 1, 1, 1, 33, 10,
                    GridBagConstraints.NORTHWEST,
                    GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 5), 0, 0));
        }

        public void addListeners() {

            btnApply.addActionListener(this);
            btnDefault.addActionListener(this);
            btnReset.addActionListener(this);

            btnApply.addMouseListener(ConfigurationUI.this);
            btnDefault.addMouseListener(ConfigurationUI.this);
            btnReset.addMouseListener(ConfigurationUI.this);

        }

        /**
         *
         */
        private void defaultNames() {

            NCField[] variables = NCField.values();

            for (int i = 0; i < variables.length; i++) {
                if (variables[i].isRequired(model + dimension)) {
                    table.getModel().setValueAt(variables[i].getDefaultName(
                            model),
                                                i, 1);
                }
            }
        }

        /**
         *
         */
        private void reset() {

            NCField[] variables = NCField.values();

            for (int i = 0; i < variables.length; i++) {
                if (variables[i].isRequired(model + dimension)) {
                    table.getModel().setValueAt(variables[i].getName(
                            model),
                                                i, 1);
                }
            }
        }

        /**
         *
         */
        private boolean apply() {

            NCField[] variables = NCField.values();
            String name;

            /** first checks none of the required name is empty */
            int nRow = table.getModel().getRowCount();
            for (int i = 0; i < nRow; i++) {
                if (variables[i].isRequired(model + dimension)) {
                    if (((String) table.getModel().getValueAt(i, 1)).trim().
                        isEmpty()) {
                        printErr(new IOException("The variable " +
                                                 variables[i].getDescription() +
                                                 " must not be null."),
                                 "Configuration editor - Variable tab");
                        return false;
                    }
                }
            }

            /** change names */
            for (int i = 0; i < variables.length; i++) {
                if (variables[i].isRequired(model + dimension)) {
                    name = ((String) table.getModel().getValueAt(i, 1)).
                           trim();
                    variables[i].setName(model, name);
                }
            }
            return true;
        }

        /**
         *
         */
        private void refresh() {

            String path = null;
            if (tabIO.listInputFiles != null) {
                path = tabIO.listInputFiles.get(0);
            }

            /** Change type of some variable for Rutgers version */
            int type = Constant.ATTRIBUTE;
            if (isRutgers(path)) {
                type = Constant.VARIABLE;
            }
            NCField.thetaB.setType(type);
            NCField.thetaS.setType(type);
            NCField.hc.setType(type);

            setUpNameColumn(path);
            findUndefined();
        }

        /**
         *
         * @param path String
         * @return boolean
         */
        private boolean isRutgers(String path) {

            if (model == Constant.ROMS && path != null) {
                try {
                    NetcdfFile ncIn = NetcdfDataset.openFile(path, null);
                    if (ncIn.findVariable(NCField.thetaS.getName(Constant.
                            ROMS)) != null) {
                        return true;
                    }
                } catch (IOException e) {
                    printErr(new IOException(
                            "Error determining UCLA / Rutgers version.\n" +
                            e.getMessage()),
                             "Configuration editor - Variable tab");
                }
            }
            return false;
        }

        /*
         * This method picks good column sizes.
         * If all column heads are wider than the column's cells'
         * contents, then you can just use column.sizeWidthToFit().
         */
        private void initColumnSizes(JTable table) {

            MyTableModel model = (MyTableModel) table.getModel();
            TableColumn column = null;
            Component comp = null;
            int headerWidth = 0;
            int cellWidth = 0;
            Object[] longValues = model.longValues;
            TableCellRenderer headerRenderer =
                    table.getTableHeader().getDefaultRenderer();

            for (int i = 0; i < table.getModel().getColumnCount(); i++) {
                column = table.getColumnModel().getColumn(i);

                comp = headerRenderer.getTableCellRendererComponent(
                        null, column.getHeaderValue(),
                        false, false, 0, 0);
                headerWidth = comp.getPreferredSize().width;

                comp = table.getDefaultRenderer(model.getColumnClass(i)).
                       getTableCellRendererComponent(
                               table, longValues[i],
                               false, false, 0, i);
                cellWidth = comp.getPreferredSize().width;

                column.setPreferredWidth(Math.max(headerWidth, cellWidth));
            }
        }


        /**
         *
         * @param path String
         */
        public void setUpNameColumn(String path) {

            try {
                fillComboBox(path);
            } catch (IOException e) {
                printErr(e,
                         "Configuration editor - Problem setting up Variable tab");
            }

            RowEditorModel rm = new RowEditorModel();
            table.setRowEditorModel(rm);

            DefaultCellEditor ceDimension = new DefaultCellEditor(
                    cbBoxDimension);
            DefaultCellEditor ceVariable = new DefaultCellEditor(
                    cbBoxVariable);
            DefaultCellEditor ceAttribute = new DefaultCellEditor(
                    cbBoxAttribute);
            NCField[] variables = NCField.values();
            for (int i = 0; i < variables.length; i++) {
                switch (variables[i].getType()) {
                case Constant.DIMENSION:
                    rm.addEditorForRow(i, ceDimension);
                    break;
                case Constant.VARIABLE:
                    rm.addEditorForRow(i, ceVariable);
                    break;
                case Constant.ATTRIBUTE:
                    rm.addEditorForRow(i, ceAttribute);
                }
            }
        }

        /**
         *
         */
        private void fillComboBox(String path) throws IOException {

            String[] listDim
                    = new String[] {"[undef]"},
                      listVar = new String[] {"[undef]"},
                                listAttrib = new String[] {"[undef]"};
            int i = 0;
            try {
                if (path != null) {
                    NetcdfFile ncIn = NetcdfDataset.openFile(path, null);

                    listDim = new String[ncIn.getDimensions().size() + 1];
                    Iterator iterDim = ncIn.getDimensions().iterator();
                    listDim[i++] = "[undef]";
                    ucar.nc2.Dimension dim;
                    while (iterDim.hasNext()) {
                        dim = (ucar.nc2.Dimension) iterDim.next();
                        listDim[i++] = dim.getName();
                    }

                    i = 0;
                    listAttrib = new String[ncIn.getGlobalAttributes().
                                 size() +
                                 1];
                    Iterator iterAttrib = ncIn.getGlobalAttributes().
                                          iterator();
                    listAttrib[i++] = "[undef]";
                    Attribute attrib;
                    while (iterAttrib.hasNext()) {
                        attrib = (Attribute) iterAttrib.next();
                        listAttrib[i++] = attrib.getName();
                    }

                    i = 0;
                    listVar = new String[ncIn.getVariables().size() + 1];
                    listVar[i++] = "[undef]";
                    Iterator iterVar = ncIn.getVariables().iterator();
                    Variable var;
                    while (iterVar.hasNext()) {
                        var = (Variable) iterVar.next();
                        listVar[i++] = var.getName();
                    }

                    ncIn.close();
                }
            } catch (IOException e) {
                listDim = listVar = listAttrib = new String[] {"[undef]"};
                throw new IOException(
                        "Error listing the variables of dataset " +
                        path);
            } finally {
                cbBoxDimension = new JComboBox(listDim);
                cbBoxVariable = new JComboBox(listVar);
                cbBoxAttribute = new JComboBox(listAttrib);
                cbBoxDimension.addActionListener(this);
                cbBoxVariable.addActionListener(this);
                cbBoxAttribute.addActionListener(this);
            }

            listDim = listVar = listAttrib = null;
        }

        private void findUndefined() {

            boolean foundAll = true;
            NCField[] variable = NCField.values();
            int config = model + dimension;
            for (int i = 0; i < variable.length; i++) {
                if (variable[i].isRequired(config)) {
                    foundAll = foundAll & scanVariable(variable[i], model);
                } else {
                    table.getModel().setValueAt(variable[i].getName(model), i,
                                                1);
                }
            }
            if (foundAll) {
                btnApply.setEnabled(true);
                btnApply.doClick();
            }
        }

        private boolean scanVariable(NCField variable, int model) {

            JComboBox cbBox = null;
            String strCbBox;
            String name = variable.getName(model).toLowerCase();

            switch (variable.getType()) {
            case Constant.DIMENSION:
                cbBox = cbBoxDimension;
                break;
            case Constant.VARIABLE:
                cbBox = cbBoxVariable;
                break;
            case Constant.ATTRIBUTE:
                cbBox = cbBoxAttribute;
            }
            for (int i = 0; i < cbBox.getItemCount(); i++) {
                strCbBox = (String) cbBox.getItemAt(i);
                //System.out.println(str + " " + strCbBox);
                if (strCbBox.toLowerCase().matches(name)) {
                    table.getModel().setValueAt(strCbBox, variable.ordinal(), 1);
                    return true;
                } else {
                    table.getModel().setValueAt("[undef]", variable.ordinal(),
                                                1);
                }
            }
            //statusBar.setMessage(statusBarMsg = "Check out variable names.");
            return false;
        }

        /**
         *
         * @param e ActionEvent
         */
        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();

            /** btnReset */
            if (source == btnReset) {
                table.clearSelection();
                reset();
                btnApply.setEnabled(true);
            }

            /** btnDefault */
            if (source == btnDefault) {
                table.clearSelection();
                defaultNames();
                btnApply.setEnabled(true);
            }

            /** btnApply */
            if (source == btnApply) {
                if (apply()) {
                    tabTime.refresh();
                    tabModel.refresh();
                    btnApply.setEnabled(false);
                    /** Notifies the action */
                    (ConfigurationUI.this).valueChanged(new
                            ValueChangedEvent((
                                    Component) e.getSource()));
                }
            }

            /** one of the combo box */
            if (source.getClass() == JComboBox.class) {
                btnApply.setEnabled(true);
            }
        }

        public void setToolTips(MouseListener ml) {
        }

        public void setToolTips() {

            btnApply.setToolTipText(Resources.TIP_BTN_APPLY);
            btnDefault.setToolTipText(Resources.TIP_BTN_DEFAULT);
            btnReset.setToolTipText(Resources.TIP_BTN_RESET);
        }


        ////////////////////////////////////////////////////////////////////////
        class MyTableModel extends AbstractTableModel {

            private String[] columnNames = {"Description", "Name"};
            private Object[][] data = createData(model);
            Object[] longValues = getLongValues(data);

            /**
             *
             * @param model int
             * @return Object[][]
             */
            private Object[][] createData(int model) {

                NCField[] variables = NCField.values();
                String[][] data = new String[variables.length][2];
                for (int i = 0; i < variables.length; i++) {
                    data[i] = new String[] {variables[i].getDescription(),
                              variables[i].getName(model)};
                }
                return data;
            }

            /**
             *
             * @param data Object[][]
             * @return Object[]
             */
            private Object[] getLongValues(Object[][] data) {

                String[] longuest = new String[data.length];
                for (int i = 0; i < data.length; i++) {
                    longuest[i] = "";
                    for (int j = 0; j < data[i].length; j++) {
                        String value = (String) data[i][j];
                        if (value != null
                            && (value.length() > longuest[i].length())) {
                            longuest[i] = value;
                        }
                    }
                }
                return longuest;
            }

            public int getColumnCount() {
                return columnNames.length;
            }

            public int getRowCount() {
                return data.length;
            }

            @Override
            public String getColumnName(int col) {
                return columnNames[col];
            }

            public Object getValueAt(int row, int col) {
                return data[row][col];
            }

            /*
             * JTable uses this method to determine the default renderer/
             * editor for each cell.  If we didn't implement this method,
             * then the last column would contain text ("true"/"false"),
             * rather than a check box.
             */
            @Override
            public Class getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }

            /*
             * Don't need to implement this method unless your table's
             * editable.
             */
            @Override
            public boolean isCellEditable(int row, int col) {
                //Note that the data/cell address is constant,
                //no matter where the cell appears onscreen.
                if (col < 1) {
                    return false;
                } else {
                    return NCField.values()[row].isRequired(model +
                            dimension);
                }
            }

            /*
             * Don't need to implement this method unless your table's
             * data can change.
             */
            @Override
            public void setValueAt(Object value, int row, int col) {

                data[row][col] = value;
                fireTableCellUpdated(row, col);
            }
        }


        ////////////////////////////////////////////////////////////////////////
        class MyCellRenderer extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row,
                    int column) {

                Component comp = super.getTableCellRendererComponent(table,
                        value, isSelected, hasFocus, row, column);

                //System.out.println(row + " " + column + " " + NCField.values()[row].isRequired(0));
                if (!NCField.values()[row].isRequired(model + dimension)) {
                    comp.setForeground(Color.LIGHT_GRAY);
                } else {
                    comp.setForeground(Color.BLACK);
                }

                if (column == 1) {
                }
                return comp;
            }
        }
    }


    public static void main(String[] args) {

        // Gestion par défaut des exceptions :
        try {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.
                    UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable e) {
                    e.printStackTrace();
                }
            });
        } catch (SecurityException e) {
            // ignored (interdit dans les appli JWS non-signé)
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                try {
                    // LookAndFell système :
                    UIManager.setLookAndFeel(UIManager.getLookAndFeel());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Redimensionnement dynamique des fenêtres
                Toolkit.getDefaultToolkit().setDynamicLayout(true);

                File file = new File(
                        "/home/pverley/previmer/program/cfg/cfg-2.1/single_safe_wc_1.cfg");
                new ConfigurationUI(null, file).setVisible(true);
                //new ConfigurationUI().setVisible(true);
            }
        });
    }

//---------- end of class
}
