package ichthyop.config;

import java.io.*;
import java.text.*;
import java.util.*;

import java.awt.*;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import ichthyop.*;
import ichthyop.util.*;
import ichthyop.util.calendar.*;
import ichthyop.util.param.*;
import ucar.multiarray.*;
import ucar.netcdf.*;

public class ConfigFrame
    extends JFrame implements ActionListener {

  private static int typeModel = Resources.ROMS;
  private static int dimSimu = Resources.SIMU_3D;
  private static int typeCalendar = Resources.INTERANNUAL;
  private static int typeScheme = Resources.EULER;
  private static int typeRecruitment = Resources.RECRUIT_NONE;
  private static int typeRelease = Resources.RELEASE_ZONE;
  private static int typeRecord = Resources.RECORD_NONE;

  private JTabbedPane tabbedPane;
  private JPanel pnlBtn;
  private JButton btnSAVE, btnSAVEAS, btnEXIT;
  private JGeneralOptionPanel pnlGeneralOption;
  private JReleasePanel pnlReleasing;
  private JRecruitPanel pnlRecruit;

  //IBM field names
  private static String strXiDim, strEtaDim, strZDim, strTimeDim;
  private static String strLon, strLat, strMask, strBathy;
  private static String strU, strV, strTp, strSal, strTime, strZeta;
  private static String strPm, strPn, strThetaS, strThetaB, strHc;
  private static String strSigma;

  //ROMS field names
  private static String strXiDim_R, strEtaDim_R, strZDim_R, strTimeDim_R;
  private static String strLon_R, strLat_R, strMask_R, strBathy_R; // strPm_R, strPn_R;
  private static String strU2D_R, strV2D_R, strTp_R, strSal_R, strTime_R,
      strZeta_R;
  private static String strU3D_R, strV3D_R;
  //private static String strThetaS_R, strThetaB_R, strHc_R;

  //MARS field names
  private static String strXiDim_M, strEtaDim_M, strZDim_M, strTimeDim_M;
  private static String strLon_M, strLat_M, strBathy_M, strSigma_M;
  private static String strU2D_M, strV2D_M, strTp_M, strSal_M, strTime_M,
      strZeta_M;
  private static String strU3D_M, strV3D_M;

  //private String strCfgPath ;
  private File cfgFile;

  //private static boolean blnFieldChanged = false;

  private INIFile cfgIn, cfgOut;

  MainFrame frame;

  private boolean HAS_CHANGED;

  //----------------------------------------------------------------------------
  public ConfigFrame(MainFrame frame) {

    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    defaultFieldNames();
    updateFieldNames();

    this.frame = frame;

    createUI();
    this.setVisible(true);
  }

  //----------------------------------------------------------------------------
  public ConfigFrame(File cfgFile, MainFrame frame) {

    this.cfgFile = cfgFile;
    cfgIn = new INIFile(cfgFile.toString());

    this.frame = frame;

    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    createUI();
    this.setVisible(true);

    setDefaultValue();

  }

  //----------------------------------------------------------------------------
  private void setDefaultValue() {
    pnlGeneralOption.setDefaultValue();
    pnlReleasing.setDefaultValue();
    pnlRecruit.setDefaultValue();
    HAS_CHANGED = false;
    resetHasChanged();
  }

  //----------------------------------------------------------------------------
  private void loadFieldNames() {

    switch (typeModel + dimSimu) {
      case (Resources.ROMS + Resources.SIMU_2D):
        strXiDim_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_XI_DIM);
        strEtaDim_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_ETA_DIM);
        strTimeDim_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_TIME_DIM); ;
        strLon_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_LON);
        strLat_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_LAT);
        strBathy_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_BATHY);
        strMask_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_MASK);
        strU2D_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_U);
        strV2D_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_V);
        strTime_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_TIME);
        break;
      case (Resources.ROMS + Resources.SIMU_3D):
        strXiDim_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_XI_DIM);
        strEtaDim_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_ETA_DIM);
        strZDim_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_Z_DIM);
        strTimeDim_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_TIME_DIM);
        strLon_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_LON);
        strLat_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_LAT);
        strBathy_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_BATHY);
        strMask_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_MASK); ;
        strU3D_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_U);
        strV3D_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_V);
        strZeta_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_ZETA);
        strTp_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_TP);
        strSal_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_SAL);
        strTime_R = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_TIME);
        strPn = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_PN);
        strPm = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_PM);
        strThetaS = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_THETA_S);
        strThetaB = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_THETA_B);
        strHc = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_HC);
        break;
      case (Resources.MARS + Resources.SIMU_2D):
        strXiDim_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_XI_DIM);
        strEtaDim_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_ETA_DIM);
        strTimeDim_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_TIME_DIM);
        strLon_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_LON);
        strLat_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_LAT); ;
        strBathy_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_BATHY);
        strU2D_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_U);
        strV2D_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_V);
        strTime_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_TIME);
        break;
      case (Resources.MARS + Resources.SIMU_3D):
        strXiDim_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_XI_DIM);
        strEtaDim_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_ETA_DIM);
        strZDim_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_Z_DIM);
        strTimeDim_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_TIME_DIM);
        strLon_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_LON);
        strLat_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_LAT);
        strBathy_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_BATHY);
        strU3D_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_U);
        strV3D_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_V);
        strZeta_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_ZETA);
        strTp_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_TP); ;
        strSal_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_SAL);
        strTime_M = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_TIME);
        strSigma = cfgIn.getStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_SIGMA);
        break;
    }
    updateFieldNames();

  }

  //----------------------------------------------------------------------------
  private void createUI() {

    this.getContentPane().setLayout(new GridBagLayout());
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    this.setSize( (int) (0.9f * screenSize.width),
        (int) (0.85f * screenSize.height));
    String strTitle = (cfgFile != null)
        ? Resources.NAME_SHORT + cfgFile.toString()
        : Resources.NAME_SHORT + Resources.TITLE_FRAME_CONFIG;
    this.setTitle(strTitle);
    this.setLocation( (int) (0.f * screenSize.width),
        (int) (0.f * screenSize.height));

    //TabbedPane
    tabbedPane = new JTabbedPane();
    tabbedPane.addTab(Resources.TAB_SIMULATION,
        pnlGeneralOption = new JGeneralOptionPanel());
    tabbedPane.addTab(Resources.TAB_RELEASING,
        pnlReleasing = new JReleasePanel());
    tabbedPane.addTab(Resources.TAB_RECRUITMENT, pnlRecruit = new JRecruitPanel());

    //Navigation Btns
    pnlBtn = new JPanel(new GridBagLayout());
    btnSAVE = new JButton(Resources.BTN_SAVE);
    btnSAVEAS = new JButton(Resources.BTN_SAVEAS);
    btnEXIT = new JButton(Resources.BTN_EXIT);
    pnlBtn.add(btnSAVE);
    pnlBtn.add(btnSAVEAS);
    pnlBtn.add(btnEXIT);

    this.getContentPane().add(tabbedPane,
        new GridBagConstraints(0, 0, 1, 1, 100, 90,
        GridBagConstraints.NORTH,
        GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(pnlBtn,
        new GridBagConstraints(0, 1, 1, 1, 100, 10,
        GridBagConstraints.EAST,
        GridBagConstraints.NONE,
        new Insets(5, 5, 5, 5), 0, 0));

    //Add listeners
    btnSAVE.addActionListener(this);
    btnSAVEAS.addActionListener(this);
    btnEXIT.addActionListener(this);

    /*I've been unable to initialize properly prmBeginingSimulation
         in class JGeneralOptionPanel, methods createUI().
     It works now but not clean to have this statement here...
     */
    pnlGeneralOption.rdBtnPhysicalTime.doClick();
  }

  //--------------------------------------------------------------------------
  private void defaultFieldNames() {

    strXiDim_R = Resources.STR_XI_DIM_R;
    strEtaDim_R = Resources.STR_ETA_DIM_R;
    strZDim_R = Resources.STR_Z_DIM_R;
    strTimeDim_R = Resources.STR_TIME_DIM_R;
    strLon_R = Resources.STR_LON_R;
    strLat_R = Resources.STR_LAT_R;
    strBathy_R = Resources.STR_BATHY_R;
    strMask_R = Resources.STR_MASK_R;
    strU2D_R = Resources.STR_U2D_R;
    strV2D_R = Resources.STR_V2D_R;
    strU3D_R = Resources.STR_U3D_R;
    strV3D_R = Resources.STR_V3D_R;
    strZeta_R = Resources.STR_ZETA_R;
    strTp_R = Resources.STR_TP_R;
    strSal_R = Resources.STR_SAL_R;
    strTime_R = Resources.STR_TIME_R;
    strPn = Resources.STR_PN;
    strPm = Resources.STR_PM;
    strThetaS = Resources.STR_THETA_S;
    strThetaB = Resources.STR_THETA_B;
    strHc = Resources.STR_HC;

    strXiDim_M = Resources.STR_XI_DIM_M;
    strEtaDim_M = Resources.STR_ETA_DIM_M;
    strZDim_M = Resources.STR_Z_DIM_M;
    strTimeDim_M = Resources.STR_TIME_DIM_M;
    strLon_M = Resources.STR_LON_M;
    strLat_M = Resources.STR_LAT_M;
    strBathy_M = Resources.STR_BATHY_M;
    strU2D_M = Resources.STR_U2D_M;
    strV2D_M = Resources.STR_V2D_M;
    strU3D_M = Resources.STR_U3D_M;
    strV3D_M = Resources.STR_V3D_M;
    strZeta_M = Resources.STR_ZETA_M;
    strTp_M = Resources.STR_TP_M;
    strSal_M = Resources.STR_SAL_M;
    strTime_M = Resources.STR_TIME_M;
    strSigma = Resources.STR_SIGMA;

  }

  //--------------------------------------------------------------------------
  private static void updateFieldNames() {

    switch (typeModel + dimSimu) {
      case (Resources.ROMS + Resources.SIMU_2D):
        strXiDim = strXiDim_R;
        strEtaDim = strEtaDim_R;
        strTimeDim = strTimeDim_R;
        strLon = strLon_R;
        strLat = strLat_R;
        strBathy = strBathy_R;
        strMask = strMask_R;
        strU = strU2D_R;
        strV = strV2D_R;
        strZeta = strZeta_R;
        strTime = strTime_R;
        break;
      case (Resources.ROMS + Resources.SIMU_3D):
        strXiDim = strXiDim_R;
        strEtaDim = strEtaDim_R;
        strZDim = strZDim_R;
        strTimeDim = strTimeDim_R;
        strLon = strLon_R;
        strLat = strLat_R;
        strBathy = strBathy_R;
        strMask = strMask_R;
        strU = strU3D_R;
        strV = strV3D_R;
        strZeta = strZeta_R;
        strTp = strTp_R;
        strSal = strSal_R;
        strTime = strTime_R;
        break;
      case (Resources.MARS + Resources.SIMU_2D):
        strXiDim = strXiDim_M;
        strEtaDim = strEtaDim_M;
        strTimeDim = strTimeDim_M;
        strLon = strLon_M;
        strLat = strLat_M;
        strBathy = strBathy_M;
        strU = strU2D_M;
        strV = strV2D_M;
        strZeta = strZeta_M;
        strTime = strTime_M;
        break;
      case (Resources.MARS + Resources.SIMU_3D):
        strXiDim = strXiDim_M;
        strEtaDim = strEtaDim_M;
        strZDim = strZDim_M;
        strTimeDim = strTimeDim_M;
        strLon = strLon_M;
        strLat = strLat_M;
        strBathy = strBathy_M;
        strU = strU3D_M;
        strV = strV3D_M;
        strZeta = strZeta_M;
        strTp = strTp_M;
        strSal = strSal_M;
        strTime = strTime_M;
        break;
    }
  }

  //----------------------------------------------------------------------------
  private void writeCfgFile() {

    boolean blnTmp = false;

    cfgOut.addSection(Resources.STR_SECTION_CONFIG, null);
    cfgOut.setIntegerProperty(Resources.STR_SECTION_CONFIG,
        Resources.STR_CFG_CONFIG, Resources.SINGLE_SIMU,
        Resources.STR_MAN_CONFIG);

    cfgOut.addSection(Resources.STR_SECTION_MODEL, null);
    cfgOut.setIntegerProperty(Resources.STR_SECTION_MODEL,
        Resources.STR_CFG_MODEL, typeModel,
        Resources.STR_MAN_MODEL);
    cfgOut.setIntegerProperty(Resources.STR_SECTION_MODEL,
        Resources.STR_CFG_2D3D, dimSimu,
        Resources.STR_MAN_2D3D);

    cfgOut.addSection(Resources.STR_SECTION_SCHEME, null);
    cfgOut.setIntegerProperty(Resources.STR_SECTION_SCHEME,
        Resources.STR_CFG_SCHEME, typeScheme,
        Resources.STR_MAN_SCHEME);

    cfgOut.addSection(Resources.STR_SECTION_IO, Resources.STR_MAN_IO);
    cfgOut.setStringProperty(Resources.STR_SECTION_IO,
        Resources.STR_CFG_DIRECTORY_IN,
        pnlGeneralOption.txtInputPath.getText().
        replace('/', File.separatorChar),
        Resources.STR_MAN_DIRECTORY_IN);
    cfgOut.setStringProperty(Resources.STR_SECTION_IO,
        Resources.STR_CFG_DIRECTORY_OUT,
        pnlGeneralOption.txtOutputPath.getText().
        replace('/', File.separatorChar),
        Resources.STR_MAN_DIRECTORY_OUT);

    cfgOut.addSection(Resources.STR_SECTION_TIME, Resources.STR_MAN_TIME);
    cfgOut.setIntegerProperty(Resources.STR_SECTION_TIME,
        Resources.STR_CFG_CALENDAR, typeCalendar,
        Resources.STR_MAN_CALENDAR);
    cfgOut.setLongProperty(Resources.STR_SECTION_TIME, Resources.STR_CFG_T0,
        pnlGeneralOption.prmBeginingSimulation.getValue().longValue(), null);
    cfgOut.setLongProperty(Resources.STR_SECTION_TIME,
        Resources.STR_CFG_TRANSPORT_DURATION,
        pnlGeneralOption.prmTransportDuration.getValue().longValue(), null);

    cfgOut.setLongProperty(Resources.STR_SECTION_TIME,
        Resources.STR_CFG_DT,
        (long) (pnlGeneralOption.prmIBMdt.getValue().intValue() *
        1e3), null);

    //Section BIOLOGY
    cfgOut.addSection(Resources.STR_SECTION_BIO, Resources.STR_MAN_BIO);
    blnTmp = pnlGeneralOption.ckBoxBuoy.isSelected();
    cfgOut.setBooleanProperty(Resources.STR_SECTION_BIO,
        Resources.STR_CFG_BUOY, blnTmp, null);
    if (blnTmp) {
      cfgOut.setDoubleProperty(Resources.STR_SECTION_BIO,
          Resources.STR_CFG_EGG_DENSITY,
          pnlGeneralOption.prmDensity.getValue().floatValue(), null);
      cfgOut.setDoubleProperty(Resources.STR_SECTION_BIO,
          Resources.STR_CFG_AGE_LIMIT,
          pnlGeneralOption.prmAgeBuoy.getValue().floatValue(), null);
    }
    else {
      cfgOut.setStringProperty(Resources.STR_SECTION_BIO,
          Resources.STR_CFG_EGG_DENSITY, null, null);
      cfgOut.setStringProperty(Resources.STR_SECTION_BIO,
          Resources.STR_CFG_AGE_LIMIT, null, null);
    }
    blnTmp = pnlGeneralOption.ckBoxLethalTp.isSelected();
    cfgOut.setBooleanProperty(Resources.STR_SECTION_BIO,
        Resources.STR_CFG_LETHAL_TP, blnTmp, null);
    if (blnTmp) {
      cfgOut.setDoubleProperty(Resources.STR_SECTION_BIO,
          Resources.STR_CFG_TP_EGG,
          pnlGeneralOption.prmTpEggs.getValue().floatValue(), null);
      cfgOut.setDoubleProperty(Resources.STR_SECTION_BIO,
          Resources.STR_CFG_TP_LARVAE,
          pnlGeneralOption.prmTpLarvae.getValue().floatValue(), null);
    }
    else {
      cfgOut.setStringProperty(Resources.STR_SECTION_BIO,
          Resources.STR_CFG_TP_EGG, null, null);
      cfgOut.setStringProperty(Resources.STR_SECTION_BIO,
          Resources.STR_CFG_TP_LARVAE, null, null);
    }
    cfgOut.setBooleanProperty(Resources.STR_SECTION_BIO,
        Resources.STR_CFG_GROWTH, pnlGeneralOption.ckBoxGrowth.isSelected(), null);

    //Section RELEASING
    cfgOut.addSection(Resources.STR_SECTION_RELEASE,
        Resources.STR_MAN_RELEASING);
    cfgOut.setIntegerProperty(Resources.STR_SECTION_RELEASE,
        Resources.STR_CFG_TYPE_RELEASE, typeRelease,
        Resources.STR_MAN_TYPE_RELEASING);
    if (typeRelease == Resources.RELEASE_ZONE) {
      cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_DRIFTERS, null, null);
      cfgOut.setIntegerProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_NB_PARTICLES,
          pnlReleasing.prmNbReleased.getValue().intValue(), null);
      cfgOut.setIntegerProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_DEPTH_MIN,
          pnlReleasing.prmDepthMin.getValue().intValue(), null);
      cfgOut.setIntegerProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_DEPTH_MAX,
          pnlReleasing.prmDepthMax.getValue().intValue(), null);

      blnTmp = pnlReleasing.ckBoxPulsation.isSelected();
      cfgOut.setBooleanProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_PULSATION, blnTmp, null);
      if (blnTmp) {
        cfgOut.setLongProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_RELEASING_DT,
            pnlReleasing.prmReleaseDt.getValue().longValue(), null);
        cfgOut.setIntegerProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_NB_RELEASING_EVENTS,
            pnlReleasing.prmNbReleaseEvents.
            getValue().intValue(), null);
      }
      else {
        cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_RELEASING_DT, null, null);
        cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_NB_RELEASING_EVENTS, null, null);
      }

      blnTmp = pnlReleasing.ckBoxPatchiness.isSelected();
      cfgOut.setBooleanProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_PATCHINESS, blnTmp, null);
      if (blnTmp) {
        cfgOut.setIntegerProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_NB_PATCHES,
            pnlReleasing.prmNbPatches.getValue().intValue(), null);
        cfgOut.setIntegerProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_RADIUS_PATCH,
            pnlReleasing.prmPatchRadius.getValue().intValue(), null);
        cfgOut.setIntegerProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_THICK_PATCH,
            pnlReleasing.prmPatchThickness.getValue().intValue(), null);
      }
      else {
        cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_NB_PATCHES, null, null);
        cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_RADIUS_PATCH, null, null);
        cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_THICK_PATCH, null, null);
      }
    }
    else {
      cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_DRIFTERS, pnlReleasing.txtFile.getText(), null);
      cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_NB_PARTICLES, null, null);
      cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_DEPTH_MIN, null, null);
      cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_DEPTH_MAX, null, null);
      cfgOut.setBooleanProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_PULSATION, false, null);
      cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_RELEASING_DT, null, null);
      cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_NB_RELEASING_EVENTS, null, null);
      cfgOut.setBooleanProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_PATCHINESS, false, null);
      cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_NB_PATCHES, null, null);
      cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_RADIUS_PATCH, null, null);
      cfgOut.setStringProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_THICK_PATCH, null, null);
    }
    cfgOut.setIntegerProperty(Resources.STR_SECTION_RELEASE,
        Resources.STR_CFG_NB_SZONES,
        pnlReleasing.releaseZoneEditor.getArrayZones().size(), null);

    //RELEASE ZONES
    for (int i = 0; i < pnlReleasing.releaseZoneEditor.getArrayZones().size(); i++) {
      Zone znTmp = (Zone) pnlReleasing.releaseZoneEditor.getArrayZones().
          get(i);
      String scTmp = Resources.STR_SECTION_SZONE + znTmp.getIndexZone();
      cfgOut.addSection(scTmp, Resources.STR_MAN_SZONE);
      for (int j = 0; j < 4; j++) {
        //System.out.println(znTmp.getLon(j) + " " + znTmp.getLat(j));
        cfgOut.setDoubleProperty(scTmp,
            Resources.STR_CFG_LON_ZONE +
            String.valueOf(j + 1), (float)znTmp.getLon(j), null);
        cfgOut.setDoubleProperty(scTmp,
            Resources.STR_CFG_LAT_ZONE +
            String.valueOf(j + 1), (float)znTmp.getLat(j), null);
      }
      cfgOut.setDoubleProperty(scTmp, Resources.STR_CFG_BATHY_MIN,
          (float)znTmp.getBathyMin(), null);
      cfgOut.setDoubleProperty(scTmp, Resources.STR_CFG_BATHY_MAX,
          (float)znTmp.getBathyMax(), null);
      cfgOut.setIntegerProperty(scTmp, Resources.STR_CFG_RED,
          znTmp.getColorZone().getRed(), null);
      cfgOut.setIntegerProperty(scTmp, Resources.STR_CFG_GREEN,
          znTmp.getColorZone().getGreen(), null);
      cfgOut.setIntegerProperty(scTmp, Resources.STR_CFG_BLUE,
          znTmp.getColorZone().getBlue(), null);
    }

    //Section RECRUITMENT
    cfgOut.addSection(Resources.STR_SECTION_RECRUIT,
        Resources.STR_MAN_RECRUIT);
    cfgOut.setIntegerProperty(Resources.STR_SECTION_RECRUIT,
        Resources.STR_CFG_RECRUIT, typeRecruitment,
        Resources.STR_MAN_TYPE_RECRUIT);
    switch (typeRecruitment) {
      case Resources.RECRUIT_NONE:
        cfgOut.setStringProperty(Resources.STR_SECTION_RECRUIT,
            Resources.STR_CFG_AGE_RECRUIT, null, null);
        cfgOut.setStringProperty(Resources.STR_SECTION_RECRUIT,
            Resources.STR_CFG_LENGTH_RECRUIT, null, null);
        cfgOut.setStringProperty(Resources.STR_SECTION_RECRUIT,
            Resources.STR_CFG_DURATION_RECRUIT, null, null);
        break;
      case Resources.RECRUIT_AGE:
        cfgOut.setDoubleProperty(Resources.STR_SECTION_RECRUIT,
            Resources.STR_CFG_AGE_RECRUIT,
            pnlRecruit.prmAgeRecruit.getValue().floatValue(), null);
        cfgOut.setStringProperty(Resources.STR_SECTION_RECRUIT,
            Resources.STR_CFG_LENGTH_RECRUIT, null, null);
        cfgOut.setDoubleProperty(Resources.STR_SECTION_RECRUIT,
            Resources.STR_CFG_DURATION_RECRUIT,
            pnlRecruit.prmDurationRecruit.getValue().floatValue(), null);
        break;
      case Resources.RECRUIT_LENGTH:
        cfgOut.setStringProperty(Resources.STR_SECTION_RECRUIT,
            Resources.STR_CFG_AGE_RECRUIT, null, null);
        cfgOut.setDoubleProperty(Resources.STR_SECTION_RECRUIT,
            Resources.STR_CFG_LENGTH_RECRUIT,
            pnlRecruit.prmLengthRecruit.getValue().floatValue(), null);
        cfgOut.setDoubleProperty(Resources.STR_SECTION_RECRUIT,
            Resources.STR_CFG_DURATION_RECRUIT,
            pnlRecruit.prmDurationRecruit.getValue().floatValue(), null);
        break;
    }
    cfgOut.setIntegerProperty(Resources.STR_SECTION_RECRUIT,
        Resources.STR_CFG_NB_RZONES,
        pnlRecruit.recruitmentZoneEditor.getArrayZones().size(), null);

    //Recruitment zones
    for (int i = 0; i < pnlRecruit.recruitmentZoneEditor.getArrayZones().size(); i++) {
      Zone znTmp = (Zone) pnlRecruit.recruitmentZoneEditor.getArrayZones().
          get(i);
      String scTmp = Resources.STR_SECTION_RZONE + znTmp.getIndexZone();
      cfgOut.addSection(scTmp, Resources.STR_MAN_RZONE);
      for (int j = 0; j < 4; j++) {
        cfgOut.setDoubleProperty(scTmp,
            Resources.STR_CFG_LON_ZONE +
            String.valueOf(j + 1), (float)znTmp.getLon(j), null);
        cfgOut.setDoubleProperty(scTmp,
            Resources.STR_CFG_LAT_ZONE +
            String.valueOf(j + 1), (float)znTmp.getLat(j), null);
      }
      cfgOut.setDoubleProperty(scTmp, Resources.STR_CFG_BATHY_MIN,
          (float)znTmp.getBathyMin(), null);
      cfgOut.setDoubleProperty(scTmp, Resources.STR_CFG_BATHY_MAX,
          (float)znTmp.getBathyMax(), null);
      cfgOut.setIntegerProperty(scTmp, Resources.STR_CFG_RED,
          znTmp.getColorZone().getRed(), null);
      cfgOut.setIntegerProperty(scTmp, Resources.STR_CFG_GREEN,
          znTmp.getColorZone().getGreen(), null);
      cfgOut.setIntegerProperty(scTmp, Resources.STR_CFG_BLUE,
          znTmp.getColorZone().getBlue(), null);
    }

    //Section general options
    cfgOut.addSection(Resources.STR_SECTION_OPTION_SIMU,
        Resources.STR_MAN_OPTION_SIMU);
    cfgOut.setIntegerProperty(Resources.STR_SECTION_OPTION_SIMU,
        Resources.STR_CFG_RECORD, typeRecord, Resources.STR_MAN_RECORD);
    if (typeRecord != Resources.RECORD_NONE) {
      cfgOut.setIntegerProperty(Resources.STR_SECTION_OPTION_SIMU,
          Resources.STR_CFG_RECORD_DT,
          pnlGeneralOption.prmRecordDt.getValue().intValue(), null);
    }
    else {
      cfgOut.setStringProperty(Resources.STR_SECTION_OPTION_SIMU,
          Resources.STR_CFG_RECORD_DT, null, null);
    }
    blnTmp = pnlGeneralOption.ckBoxIsoDepth.isSelected();
    cfgOut.setBooleanProperty(Resources.STR_SECTION_OPTION_SIMU,
        Resources.STR_CFG_ISO_DEPTH_MVT, blnTmp, null);
    if (blnTmp) {
      cfgOut.setDoubleProperty(Resources.STR_SECTION_OPTION_SIMU,
          Resources.STR_CFG_ISO_DEPTH,
          pnlGeneralOption.prmIsoDepth.getValue().floatValue(), null);
    }
    else {
      cfgOut.setStringProperty(Resources.STR_SECTION_OPTION_SIMU,
          Resources.STR_CFG_ISO_DEPTH, null, null);
    }
    cfgOut.setStringProperty(Resources.STR_SECTION_OPTION_SIMU,
        Resources.STR_CFG_NB_REPLICA, null, null);

    //Section hydrodynamic names
    cfgOut.addSection(Resources.STR_SECTION_NAMES, Resources.STR_MAN_NAMES);
    cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_XI_DIM, strXiDim, null);
    cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_ETA_DIM, strEtaDim, null);
    cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_Z_DIM, strZDim, null);
    cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_TIME_DIM, strTimeDim, null);
    cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_LON, strLon, null);
    cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_LAT, strLat, null);
    cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_BATHY, strBathy, null);
    if (typeModel == Resources.MARS) {
      cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
            Resources.STR_CFG_SIGMA, strSigma, null);
    }
    if (typeModel == Resources.ROMS) {
      cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
          Resources.STR_CFG_MASK, strMask, null);
      cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
          Resources.STR_CFG_PM, strPm, null);
      cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
          Resources.STR_CFG_PN, strPn, null);
    }
    cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_ZETA, strZeta, null);
    cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_U, strU, null);
    cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_V, strV, null);
    cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_TP, strTp, null);
    cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_SAL, strSal, null);
    cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
        Resources.STR_CFG_TIME, strTime, null);
    if (typeModel == Resources.ROMS) {
      cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
          Resources.STR_CFG_THETA_S, strThetaS, null);
      cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
          Resources.STR_CFG_THETA_B, strThetaB, null);
      cfgOut.setStringProperty(Resources.STR_SECTION_NAMES,
          Resources.STR_CFG_HC, strHc, null);
    }

    System.out.println("Saved " + cfgOut.getFileName() + " " + cfgOut.save());
  }

  //----------------------------------------------------------------------------
  private boolean paramHasChanged() {

    return! (!pnlGeneralOption.prmAgeBuoy.hasChanged() ||
        !pnlGeneralOption.prmBeginingSimulation.hasChanged() ||
        !pnlGeneralOption.prmDensity.hasChanged() ||
        !pnlGeneralOption.prmIBMdt.hasChanged() ||
        !pnlGeneralOption.prmIsoDepth.hasChanged() ||
        !pnlGeneralOption.prmRecordDt.hasChanged() ||
        !pnlGeneralOption.prmTpEggs.hasChanged() ||
        !pnlGeneralOption.prmTpLarvae.hasChanged() ||
        !pnlGeneralOption.prmTransportDuration.hasChanged() ||
        !pnlReleasing.prmDepthMax.hasChanged() ||
        !pnlReleasing.prmDepthMin.hasChanged() ||
        !pnlReleasing.prmNbPatches.hasChanged() ||
        !pnlReleasing.prmNbReleased.hasChanged() ||
        !pnlReleasing.prmNbReleaseEvents.hasChanged() ||
        !pnlReleasing.prmPatchRadius.hasChanged() ||
        !pnlReleasing.prmReleaseDt.hasChanged() ||
        !pnlReleasing.prmPatchThickness.hasChanged() ||
        !pnlReleasing.releaseZoneEditor.hasChanged() ||
        !pnlRecruit.prmAgeRecruit.hasChanged() ||
        !pnlRecruit.prmDurationRecruit.hasChanged() ||
        !pnlRecruit.prmLengthRecruit.hasChanged() ||
        !pnlRecruit.recruitmentZoneEditor.hasChanged());

  }

  //----------------------------------------------------------------------------
  private void resetHasChanged() {
    pnlGeneralOption.prmAgeBuoy.setHasChanged(false);
    pnlGeneralOption.prmBeginingSimulation.setHasChanged(false);
    pnlGeneralOption.prmDensity.setHasChanged(false);
    pnlGeneralOption.prmIBMdt.setHasChanged(false);
    pnlGeneralOption.prmIsoDepth.setHasChanged(false);
    pnlGeneralOption.prmRecordDt.setHasChanged(false);
    pnlGeneralOption.prmTpEggs.setHasChanged(false);
    pnlGeneralOption.prmTpLarvae.setHasChanged(false);
    pnlGeneralOption.prmTransportDuration.setHasChanged(false);
    pnlReleasing.prmDepthMax.setHasChanged(false);
    pnlReleasing.prmDepthMin.setHasChanged(false);
    pnlReleasing.prmNbPatches.setHasChanged(false);
    pnlReleasing.prmNbReleased.setHasChanged(false);
    pnlReleasing.prmNbReleaseEvents.setHasChanged(false);
    pnlReleasing.prmPatchRadius.setHasChanged(false);
    pnlReleasing.prmReleaseDt.setHasChanged(false);
    pnlReleasing.prmPatchThickness.setHasChanged(false);
    pnlReleasing.releaseZoneEditor.setHasChanged(false);
    pnlRecruit.prmAgeRecruit.setHasChanged(false);
    pnlRecruit.prmDurationRecruit.setHasChanged(false);
    pnlRecruit.prmLengthRecruit.setHasChanged(false);
    pnlRecruit.recruitmentZoneEditor.setHasChanged(false);

  }

  //----------------------------------------------------------------------------
  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();

    if (src == btnEXIT) {
      if (HAS_CHANGED | paramHasChanged()) {
        int answer = JOptionPane.showConfirmDialog(this,
            "Do you wish to save changes ?");
        switch (answer) {
          case JOptionPane.YES_OPTION:
            btnSAVE.doClick();
            break;
          case JOptionPane.NO_OPTION:
            break;
          case JOptionPane.CANCEL_OPTION:
          case JOptionPane.CLOSED_OPTION:
            return;
        }
      }
      if (cfgFile != null) {
        frame.setCfgFile(cfgFile);
        new GetConfig(cfgFile);
        frame.getSimulation().setUp();
        frame.setTitle(Resources.NAME_SHORT + cfgFile.toString());
        frame.setUpMenu();
      }
      this.dispose();
    }
    if (src == btnSAVE) {
      if (cfgFile != null) {
        cfgOut = new INIFile(cfgFile.toString());
      }
      else {
        JFileChooser fc = new JFileChooser(".");
        fc.setDialogType(fc.SAVE_DIALOG);
        fc.setAcceptAllFileFilterUsed(false);
        IBMFileFilter ff = new IBMFileFilter(Resources.EXTENSION_CONFIG);
        fc.setFileFilter(ff);
        int returnVal = fc.showSaveDialog(ConfigFrame.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          cfgFile = ff.addExtension(fc.getSelectedFile());
          MainFrame.setCfgFile(cfgFile);
          cfgOut = new INIFile(cfgFile.toString());
        }
        else {
          return;
        }
      }
      this.setTitle(Resources.NAME_SHORT + cfgFile.toString());
      writeCfgFile();
      HAS_CHANGED = false;
      resetHasChanged();
      //btnSAVE.setEnabled(false);

    }
    if (src == btnSAVEAS) {
      JFileChooser fc = new JFileChooser(cfgFile.getPath());
      fc.setDialogType(fc.SAVE_DIALOG);
      fc.setAcceptAllFileFilterUsed(false);
      IBMFileFilter ff = new IBMFileFilter(Resources.EXTENSION_CONFIG);
      fc.setFileFilter(ff);
      if (cfgFile != null) {
        fc.setSelectedFile(cfgFile);
      }
      int returnVal = fc.showSaveDialog(ConfigFrame.this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        cfgFile = ff.addExtension(fc.getSelectedFile());
        MainFrame.setCfgFile(cfgFile);
        cfgOut = new INIFile(cfgFile.toString());
        this.setTitle(Resources.NAME_SHORT + cfgFile.toString());
        writeCfgFile();
      }
    }
  }

  //############################################################################
  private class JGeneralOptionPanel
      extends JPanel implements ActionListener {

    private JPanel pnlModel, pnlScheme, pnlPath, pnlFile, pnlTime, pnlBio,
        pnl23D, pnlOtherOptions;
    private JRadioButton rdBtnROMS, rdBtnMARS;
    private JRadioButton rdBtnEULER, rdBtnRK4;
    private JRadioButton rdBtnClimatoTime, rdBtnPhysicalTime;
    private JRadioButton rdBtn2D, rdBtn3D;
    private JButton btnCfgModel, btnDefaultTime, btnGetInfo;
    private JButton btnEditInputPath, btnEditOutputPath;
    private JCheckBox ckBoxBuoy, ckBoxGrowth, ckBoxLethalTp, ckBoxIsoDepth;
    private JRadioButton rdBtnRecordNone, rdBtnRecordTxt, rdBtnRecordNc;
    private JTextField txtInputPath, txtOutputPath;
    private JLabel lblDt, lblRecordDt, lblLonMin, lblLonMax, lblLatMin,
        lblLatMax, lblDepthMax;

    private DateParamIBM prmBeginingSimulation = new DateParamIBM(
        Resources.PRM_BEGIN_SIMU, new Calendar1900(), Resources.UNIT_NONE, true);
    private DurationParamIBM prmTransportDuration = new DurationParamIBM(
        Resources.PRM_DURATION_TRANSPORT, "0002/00:00", Resources.UNIT_DURATION, true);
    private IntegerParamIBM prmIBMdt = new IntegerParamIBM(
        Resources.PRM_INTERNAL_DT, 3600, Resources.UNIT_SECOND, true);
    private FloatParamIBM prmTpEggs = new FloatParamIBM(
        Resources.PRM_LETHAL_TP_EGG, 10.0f, Resources.UNIT_CELSIUS, false);
    private FloatParamIBM prmTpLarvae = new FloatParamIBM(
        Resources.PRM_LETHAL_TP_LARVAE, 10.0f, Resources.UNIT_CELSIUS, false);
    private FloatParamIBM prmDensity = new FloatParamIBM(
        Resources.PRM_EGG_DENSITY, 1.025f, Resources.UNIT_DENSITY, false);
    private FloatParamIBM prmIsoDepth = new FloatParamIBM(Resources.
        PRM_ISODEPTH, 10.f, Resources.UNIT_METER, false);
    private IntegerParamIBM prmRecordDt = new IntegerParamIBM(
        Resources.PRM_RECORD_FREQUENCY, 12, Resources.UNIT_FACTOR_DT, false);
    private FloatParamIBM prmAgeBuoy = new FloatParamIBM(
        Resources.PRM_AGE_LIMIT_BUOY, 3.0f, Resources.UNIT_DAY, false);

    private double lonMin, lonMax, latMin, latMax, depthMax, dt, dtR;
    private long t0, trspDuration;
    private String firstFile;
    private File inputPath;

    //--------------------------------------------------------------------------
    public JGeneralOptionPanel() {
      super(new GridBagLayout());
      prmTpEggs.setFormatPolicy(1, 2, 1, 1);
      prmTpLarvae.setFormatPolicy(1, 2, 1, 1);
      prmDensity.setFormatPolicy(1, 1, 1, 3);
      prmAgeBuoy.setFormatPolicy(1, 2, 1, 1);
      prmIsoDepth.setFormatPolicy(1, 4, 1, 1);
      prmBeginingSimulation.setFormatPolicy("yyyy/MM/dd HH:mm");
      createUI();
    }

    //--------------------------------------------------------------------------
    private void setDefaultValue() {
      boolean bln;

      if (cfgIn != null) {
        //Panel Model
        typeModel = cfgIn.getIntegerProperty(Resources.STR_SECTION_MODEL,
            Resources.STR_CFG_MODEL);
        if (typeModel == Resources.ROMS) {
          rdBtnROMS.doClick();
        }
        else {
          rdBtnMARS.doClick();
        }
        //Panel 2D3D
        dimSimu = cfgIn.getIntegerProperty(Resources.STR_SECTION_MODEL,
            Resources.STR_CFG_2D3D);
        if (dimSimu == Resources.SIMU_2D) {
          rdBtn2D.doClick();
        }
        else {
          rdBtn3D.doClick();
        }

        //Panel Scheme
        typeScheme = cfgIn.getIntegerProperty(Resources.STR_SECTION_SCHEME,
            Resources.STR_CFG_SCHEME);
        if (typeScheme == Resources.EULER) {
          rdBtnEULER.doClick();
        }
        else {
          rdBtnRK4.doClick();
        }

        //Needs typeModel & typeSimu to be defined
        defaultFieldNames();
        loadFieldNames();

        //Panel Path
        inputPath = new File(cfgIn.getStringProperty(Resources.STR_SECTION_IO,
            Resources.STR_CFG_DIRECTORY_IN) + File.separator);
        txtInputPath.setText(inputPath.toString());
        if (inputPath.exists()) {
          getFirstFile();
          if (firstFile != null) {
            setInfoPnlFile();
          }
        }

        txtOutputPath.setText(cfgIn.getStringProperty(Resources.STR_SECTION_IO,
            Resources.STR_CFG_DIRECTORY_OUT));

        //Panel Time
        typeCalendar = cfgIn.getIntegerProperty(
            Resources.STR_SECTION_TIME, Resources.STR_CFG_CALENDAR);
        prmBeginingSimulation.setDefaultValue(cfgIn.getLongProperty(
            Resources.STR_SECTION_TIME, Resources.STR_CFG_T0));
        prmTransportDuration.setDefaultValue(cfgIn.getLongProperty(
            Resources.STR_SECTION_TIME,
            Resources.STR_CFG_TRANSPORT_DURATION));
        if (typeCalendar == Resources.INTERANNUAL) {
          rdBtnPhysicalTime.doClick();
        }
        else {
          rdBtnClimatoTime.doClick();
        }
        prmIBMdt.setValue( (int) (cfgIn.getLongProperty(Resources.
            STR_SECTION_TIME,
            Resources.STR_CFG_DT) / 1e3));

        //Panel Bio
        bln = cfgIn.getBooleanProperty(Resources.STR_SECTION_BIO,
            Resources.STR_CFG_BUOY);
        if (bln) {
          ckBoxBuoy.setSelected(false);
          ckBoxBuoy.doClick();
          prmDensity.setValue(cfgIn.getDoubleProperty(Resources.STR_SECTION_BIO,
              Resources.STR_CFG_EGG_DENSITY).floatValue());
          prmAgeBuoy.setValue(cfgIn.getDoubleProperty(Resources.STR_SECTION_BIO,
              Resources.STR_CFG_AGE_LIMIT).floatValue());
        }

        bln = cfgIn.getBooleanProperty(Resources.STR_SECTION_BIO,
            Resources.STR_CFG_LETHAL_TP);
        if (bln) {
          ckBoxLethalTp.setSelected(false);
          ckBoxLethalTp.doClick();
          prmTpEggs.setValue(cfgIn.getDoubleProperty(Resources.
              STR_SECTION_BIO, Resources.STR_CFG_TP_EGG).
              floatValue());
          prmTpLarvae.setValue(cfgIn.getDoubleProperty(Resources.
              STR_SECTION_BIO, Resources.STR_CFG_TP_LARVAE).
              floatValue());
        }

        bln = cfgIn.getBooleanProperty(Resources.STR_SECTION_BIO,
            Resources.STR_CFG_GROWTH);
        if (bln) {
          ckBoxGrowth.setSelected(false);
          ckBoxGrowth.doClick();
        }

        //Panel Other options
        typeRecord = cfgIn.getIntegerProperty(
            Resources.STR_SECTION_OPTION_SIMU, Resources.STR_CFG_RECORD);
        if (typeRecord != Resources.RECORD_NONE) {
          if (typeRecord == Resources.RECORD_TXT) {
            rdBtnRecordTxt.doClick();
          }
          else {
            rdBtnRecordNc.doClick();
          }
          prmRecordDt.setValue(cfgIn.getIntegerProperty(Resources.
              STR_SECTION_OPTION_SIMU, Resources.STR_CFG_RECORD_DT));
        }

        bln = cfgIn.getBooleanProperty(Resources.
            STR_SECTION_OPTION_SIMU,
            Resources.STR_CFG_ISO_DEPTH_MVT);
        if (bln) {
          ckBoxIsoDepth.doClick();
          prmIsoDepth.setValue(cfgIn.getDoubleProperty(Resources.
              STR_SECTION_OPTION_SIMU,
              Resources.STR_CFG_ISO_DEPTH).floatValue());
        }
      }
      //else {rdBtnPhysicalTime.doClick();}
    }

    //--------------------------------------------------------------------------
    public void createUI() {

      //Panel Model
      pnlModel = new JPanel(new GridBagLayout());
      pnlModel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
          RAISED));
      rdBtnROMS = new JRadioButton(Resources.RD_BTN_ROMS, true);
      rdBtnMARS = new JRadioButton(Resources.RD_BTN_MARS, false);
      ButtonGroup grp = new ButtonGroup();
      grp.add(rdBtnROMS);
      grp.add(rdBtnMARS);
      btnCfgModel = new JButton(Resources.BTN_CONFIGURE);
      pnlModel.add(rdBtnROMS, new GridBagConstraints(0, 0, 1, 1, 10, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlModel.add(rdBtnMARS, new GridBagConstraints(1, 0, 1, 1, 10, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlModel.add(btnCfgModel, new GridBagConstraints(2, 0, 1, 1, 20, 0,
          GridBagConstraints.EAST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));

      //Panel Kernel
      pnlScheme = new JPanel(new GridBagLayout());
      pnlScheme.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
          RAISED));
      rdBtnEULER = new JRadioButton(Resources.RD_BTN_EULER, true);
      rdBtnRK4 = new JRadioButton(Resources.RD_BTN_RK4, false);
      ButtonGroup grp2 = new ButtonGroup(); ;
      grp2.add(rdBtnEULER);
      grp2.add(rdBtnRK4);
      pnlScheme.add(rdBtnEULER, new GridBagConstraints(0, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlScheme.add(rdBtnRK4, new GridBagConstraints(1, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));

      //Panel 2D / 3D
      pnl23D = new JPanel(new GridBagLayout());
      pnl23D.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
          RAISED));
      rdBtn2D = new JRadioButton(Resources.RD_BTN_2D, false);
      rdBtn3D = new JRadioButton(Resources.RD_BTN_3D, true);
      ButtonGroup grp23D = new ButtonGroup();
      grp23D.add(rdBtn2D);
      grp23D.add(rdBtn3D);
      pnl23D.add(rdBtn2D, new GridBagConstraints(0, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));
      pnl23D.add(rdBtn3D, new GridBagConstraints(1, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));

      //Panel path
      pnlPath = new JPanel(new GridBagLayout());
      pnlPath.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
          RAISED));
      txtInputPath = new JTextField(100);
      txtOutputPath = new JTextField(100);
      btnEditInputPath = new JButton("...");
      btnEditOutputPath = new JButton("...");
      txtInputPath.setEditable(false);
      txtOutputPath.setEditable(false);
      pnlPath.add(new JLabel(Resources.LBL_INPUT_PATH),
          new GridBagConstraints(0, 0, 1, 1, 10, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlPath.add(new JLabel(Resources.LBL_OUTPUT_PATH),
          new GridBagConstraints(0, 1, 1, 1, 10, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlPath.add(txtInputPath, new GridBagConstraints(1, 0, 1, 1, 80, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlPath.add(txtOutputPath,
          new GridBagConstraints(1, 1, 1, 1, 80, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlPath.add(btnEditInputPath,
          new GridBagConstraints(2, 0, 1, 1, 10, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlPath.add(btnEditOutputPath,
          new GridBagConstraints(2, 1, 1, 1, 10, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));

      //Panel file info
      pnlFile = new JPanel(new GridBagLayout());
      pnlFile.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
          RAISED));
      lblDt = new JLabel(Resources.LBL_DT_MODEL);
      lblRecordDt = new JLabel(Resources.LBL_DT_RECORDS);
      lblLonMin = new JLabel(Resources.LBL_LON_MIN);
      lblLonMax = new JLabel(Resources.LBL_LON_MAX);
      lblLatMin = new JLabel(Resources.LBL_LAT_MIN);
      lblLatMax = new JLabel(Resources.LBL_LAT_MAX);
      lblDepthMax = new JLabel(Resources.LBL_DEPTH_MAX);
      btnGetInfo = new JButton(Resources.BTN_GET_INFO);

      pnlFile.add(lblDt, new GridBagConstraints(0, 0, 1, 1, 50, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlFile.add(lblRecordDt, new GridBagConstraints(1, 0, 1, 1, 50, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlFile.add(lblLonMin, new GridBagConstraints(0, 1, 1, 1, 50, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlFile.add(lblLonMax, new GridBagConstraints(1, 1, 1, 1, 50, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlFile.add(lblLatMin, new GridBagConstraints(0, 2, 1, 1, 50, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlFile.add(lblLatMax, new GridBagConstraints(1, 2, 1, 1, 50, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlFile.add(lblDepthMax, new GridBagConstraints(0, 3, 1, 1, 50, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlFile.add(btnGetInfo, new GridBagConstraints(1, 3, 1, 1, 50, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));

      //Panel time
      pnlTime = new JPanel(new GridBagLayout());
      pnlTime.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
          RAISED));
      rdBtnClimatoTime = new JRadioButton(Resources.RD_BTN_TIME_CLIMATO, false);
      rdBtnPhysicalTime = new JRadioButton(Resources.RD_BTN_TIME_REAL, true);
      ButtonGroup grp3 = new ButtonGroup();
      grp3.add(rdBtnClimatoTime);
      grp3.add(rdBtnPhysicalTime);
      btnDefaultTime = new JButton(Resources.BTN_RESET_TIME);

      pnlTime.add(rdBtnPhysicalTime,
          new GridBagConstraints(0, 0, 1, 1, 50, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlTime.add(rdBtnClimatoTime,
          new GridBagConstraints(1, 0, 1, 1, 50, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlTime.add(prmBeginingSimulation.createGUI(),
          new GridBagConstraints(0, 1, 2, 1, 100, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlTime.add(prmTransportDuration.createGUI(),
          new GridBagConstraints(0, 2, 2, 1, 100, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlTime.add(prmIBMdt.createGUI(),
          new GridBagConstraints(0, 3, 1, 1, 90, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlTime.add(btnDefaultTime,
          new GridBagConstraints(1, 3, 1, 1, 10, 10,
          GridBagConstraints.EAST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));

      //Panel biology
      pnlBio = new JPanel(new GridBagLayout());
      ckBoxGrowth = new JCheckBox(Resources.CK_BOX_GROWTH, false);
      ckBoxBuoy = new JCheckBox(Resources.CK_BOX_BUOYANCY, false);
      ckBoxLethalTp = new JCheckBox(Resources.CK_BOX_LETHAL_TP, false);

      pnlBio.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
          RAISED));

      pnlBio.add(ckBoxBuoy, new GridBagConstraints(0, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlBio.add(prmDensity.createGUI(),
          new GridBagConstraints(0, 1, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 25, 5, 5), 0, 0));
      pnlBio.add(prmAgeBuoy.createGUI(),
          new GridBagConstraints(0, 2, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 25, 5, 5), 0, 0));
      pnlBio.add(ckBoxLethalTp, new GridBagConstraints(0, 3, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlBio.add(prmTpEggs.createGUI(),
          new GridBagConstraints(0, 4, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 25, 5, 5), 0, 0));
      pnlBio.add(prmTpLarvae.createGUI(),
          new GridBagConstraints(0, 5, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 25, 5, 5), 0, 0));
      pnlBio.add(ckBoxGrowth, new GridBagConstraints(0, 6, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(5, 5, 5, 5), 0, 0));

      //Panel Other options
      pnlOtherOptions = new JPanel(new GridBagLayout());
      rdBtnRecordNone = new JRadioButton(Resources.RD_BTN_RECORD_NONE, true);
      rdBtnRecordTxt = new JRadioButton(Resources.RD_BTN_RECORD_TXT, false);
      // en attendant d'y travailler
      rdBtnRecordTxt.setEnabled(false);
      rdBtnRecordNc = new JRadioButton(Resources.RD_BTN_RECORD_NC, false);
      ButtonGroup grpRecord = new ButtonGroup();
      grpRecord.add(rdBtnRecordNone);
      grpRecord.add(rdBtnRecordTxt);
      grpRecord.add(rdBtnRecordNc);
      ckBoxIsoDepth = new JCheckBox(Resources.CKBOX_ISO_DEPTH, false);

      pnlOtherOptions.setBorder(BorderFactory.createEtchedBorder(
          EtchedBorder.RAISED));
      pnlOtherOptions.add(ckBoxIsoDepth,
          new GridBagConstraints(0, 0, 3, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlOtherOptions.add(prmIsoDepth.createGUI(),
          new GridBagConstraints(0, 1, 3, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 25, 5, 5), 0, 0));
      pnlOtherOptions.add(rdBtnRecordNone,
          new GridBagConstraints(0, 2, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlOtherOptions.add(rdBtnRecordTxt,
          new GridBagConstraints(1, 2, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlOtherOptions.add(rdBtnRecordNc,
          new GridBagConstraints(2, 2, 1, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      pnlOtherOptions.add(prmRecordDt.createGUI(),
          new GridBagConstraints(0, 3, 3, 1, 0, 0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 25, 5, 5), 0, 0));

      //Add the option panels on the main panel
      this.add(pnlModel, new GridBagConstraints(0, 0, 1, 1, 50, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(pnlScheme, new GridBagConstraints(1, 0, 1, 1, 50, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(pnl23D, new GridBagConstraints(2, 0, 1, 1, 50, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(5, 5, 5, 5), 0, 0));

      this.add(pnlPath, new GridBagConstraints(0, 1, 3, 1, 100, 20,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(pnlFile, new GridBagConstraints(0, 2, 1, 1, 50, 40,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(pnlTime, new GridBagConstraints(1, 2, 2, 1, 50, 40,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(pnlBio,
          new GridBagConstraints(0, 3, 1, 1, 50, 40,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(pnlOtherOptions,
          new GridBagConstraints(1, 3, 2, 1, 50, 40,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));

      //Add listeners
      rdBtnROMS.addActionListener(this);
      rdBtnMARS.addActionListener(this);
      rdBtnEULER.addActionListener(this);
      rdBtnRK4.addActionListener(this);
      rdBtn2D.addActionListener(this);
      rdBtn3D.addActionListener(this);
      btnCfgModel.addActionListener(this);
      btnEditInputPath.addActionListener(this);
      btnEditOutputPath.addActionListener(this);
      rdBtnPhysicalTime.addActionListener(this);
      rdBtnClimatoTime.addActionListener(this);
      btnDefaultTime.addActionListener(this);
      ckBoxGrowth.addActionListener(this);
      ckBoxLethalTp.addActionListener(this);
      ckBoxBuoy.addActionListener(this);
      ckBoxIsoDepth.addActionListener(this);
      //ckBoxRecordTracks.addActionListener(this);
      rdBtnRecordNone.addActionListener(this);
      rdBtnRecordTxt.addActionListener(this);
      rdBtnRecordNc.addActionListener(this);
      btnGetInfo.addActionListener(this);
    }

    //--------------------------------------------------------------------------
    private void setInfoPnlFile() {
      File fileNC = new File(txtInputPath.getText().replace('/',
          File.separatorChar) + File.separatorChar
          + firstFile);
      try {
        NetcdfFile ncIn = new NetcdfFile(fileNC, true);
        //System.out.println(ncIn.toString());
        getInfoNcIn(ncIn);
        int[] div = {
            1, 60, 3600, 86400, 31536000};
        String[] unit = {
            Resources.UNIT_SECOND, Resources.UNIT_MINUTE, Resources.UNIT_HOUR,
            Resources.UNIT_DAY};
        int i = 0;
        while ( (int) (dt / div[i + 1]) > 0) {
          i++;
        }
        int j = 0;
        while ( (int) (dtR / div[j + 1]) > 0) {
          j++;
        }
        lblDt.setText(Resources.LBL_DT_MODEL + " = " +
            ( (dt <= 0) ? "#" : NumberFormat.getInstance().
            format(dt / div[i])) + " " + unit[i]);
        lblRecordDt.setText(Resources.LBL_DT_RECORDS + " = " +
            ( (dtR <= 0) ? "#" :
            NumberFormat.getInstance().
            format(dtR / div[j])) + " " + unit[j]);
        lblLonMin.setText(Resources.LBL_LON_MIN + " = " +
            NumberFormat.getInstance().format(lonMin) +
            " °");
        lblLonMax.setText(Resources.LBL_LON_MAX + " = " +
            NumberFormat.getInstance().format(lonMax) +
            " °");
        lblLatMin.setText(Resources.LBL_LAT_MIN + " = " +
            NumberFormat.getInstance().format(latMin) +
            " °");
        lblLatMax.setText(Resources.LBL_LAT_MAX + " = " +
            NumberFormat.getInstance().format(latMax) +
            " °");
        lblDepthMax.setText(Resources.LBL_DEPTH_MAX + " = " +
            NumberFormat.getInstance().format(depthMax) + " " +
            Resources.UNIT_METER);

      }
      catch (IOException ex1) {
        resetPnlFile();
      }
      catch (java.lang.NullPointerException ex2) {
        resetPnlFile();
      }

      //prmDepthMax.setValue( (int) depthMax);

    }

    //--------------------------------------------------------------------------
    private void setInfoPnlTime() {

      //System.out.println("Info pnl file");

      String[] listFileTmp = inputPath.list();
      File fileNC = new File("");
      Netcdf ncIn;
      double dgrid = 0.d;

      try {
        ArrayList listF = new ArrayList();
        for (int i = 0; i < listFileTmp.length; i++) {
          if (listFileTmp[i].endsWith(".nc") | listFileTmp[i].endsWith(".nc.1")) {
            listF.add(listFileTmp[i]);
          }
        }
        Iterator iter = listF.iterator();
        trspDuration = 0L;
        while (iter.hasNext()) {
          String fileName = (String) iter.next();
          fileNC = new File(txtInputPath.getText().replace('/',
              File.separatorChar) + File.separatorChar
              + fileName);
          ncIn = new NetcdfFile(fileNC, true);
          MultiArray xTimeT = ncIn.get(strTime).copyout(new int[] {
              0},
              new int[] {
              2});
          int nbTimeRecords = ncIn.getDimensions().get(strTimeDim).
              getLength();
          double dt = xTimeT.getDouble(new int[] {
              1}) -
              xTimeT.getDouble(new int[] {
              0});
          trspDuration += (long) (dt * 1e3 * nbTimeRecords);
          if (fileName.matches(firstFile)) {
            t0 = (long) (xTimeT.getDouble(new int[] {
                0}) * 1e3);
          }
        }
        ncIn = new NetcdfFile(fileNC, true);
        if (typeModel == Resources.ROMS) {
          MultiArray pmT, pnT;
          int nx, ny;
          nx = ncIn.getDimensions().get(strXiDim).getLength();
          ny = ncIn.getDimensions().get(strEtaDim).getLength();
          pmT = ncIn.get(strPm);
          pnT = ncIn.get(strPn);
          for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
              dgrid = Math.max(dgrid,
                  Math.max(pmT.getDouble(new int[] {
                  j, i}),
                  pnT.getDouble(new int[] {
                  j, i})));
            }
          }
          dgrid = 1 / dgrid;
        }
        else {
          dgrid = Math.min(
              geodesicDistance(ncIn.get(strLat).getDouble(new int[] {
              0}),
              ncIn.get(strLon).getDouble(new int[] {
              0}),
              ncIn.get(strLat).getDouble(new int[] {
              0}),
              ncIn.get(strLon).getDouble(new int[] {
              1})),
              geodesicDistance(ncIn.get(strLat).getDouble(new int[] {
              0}),
              ncIn.get(strLon).getDouble(new int[] {
              0}),
              ncIn.get(strLat).getDouble(new int[] {
              1}),
              ncIn.get(strLon).getDouble(new int[] {
              0})));
        }

      }
      catch (IOException ex1) {
        System.out.println(
            "! Error --> IOException - Problem extracting time info from file "
            + fileNC.toString());
        return;
      }
      catch (java.lang.NullPointerException
          ex2) {
        //ex2.printStackTrace();
        System.out.println(
            "! Error --> NullException - Problem extracting time info from file "
            + fileNC.toString());
        return;
      }

      prmBeginingSimulation.setValue(t0);
      prmTransportDuration.setValue(trspDuration);
      prmIBMdt.setValue( (int) (0.8 * dgrid));

    }

    //--------------------------------------------------------------------------
    double geodesicDistance(double lat1, double lon1, double lat2,
        double lon2) {
      //--------------------------------------------------------------
      // Return the curvilinear abscissa s(A[lat1, lon1]B[lat2, lon2])
      double d = 6367000.0f * Math.sqrt(2.0f
          - 2.0f *
          Math.cos(Math.PI * lat1 / 180.0f) *
          Math.cos(Math.PI * lat2 / 180.0f) *
          Math.cos(Math.PI * (lon1 - lon2) /
          180.0f)
          - 2.0f *
          Math.sin(Math.PI * lat1 / 180.0f) *
          Math.sin(Math.PI * lat2 / 180.0f));
      return (d);
    }

    //--------------------------------------------------------------------------
    private void getInfoNcIn(NetcdfFile ncIn) throws IOException,
        NullPointerException {
      //--------------------------------------
      // Calculate the Physical Space extrema

      //System.out.println(ncIn);
      int nx, ny;
      MultiArray xlonRhoT, xlatRhoT, xhT, xTimeT;
      double lonMin, lonMax, latMin, latMax, depthMax;

      lonMin = Double.MAX_VALUE;
      lonMax = -lonMin;
      latMin = Double.MAX_VALUE;
      latMax = -latMin;
      depthMax = 0.d;
      double dtR = 0;
      double dt = 0;

      int[] ind0, ind;

      try {
        xTimeT = ncIn.get(strTime).copyout(new int[] {
            0},
            new int[] {
            3});
        //t0 = (long) (xTimeT.getDouble(new int[] {0}) * 1e3);
        dtR = (xTimeT.getDouble(new int[] {
            2}) -
            xTimeT.getDouble(new int[] {
            1}));

        nx = ncIn.getDimensions().get(strXiDim).getLength();
        ny = ncIn.getDimensions().get(strEtaDim).getLength();

        if (typeModel == Resources.ROMS) {
          dt = ncIn.getAttribute("dt").getNumericValue().doubleValue();
        }

        ind0 = (typeModel == Resources.ROMS)
            ? new int[] {
            0, 0}
            : new int[] {
            0};
        ind = (typeModel == Resources.ROMS)
            ? new int[] {
            ny, nx}
            : new int[] {
            nx};
        xlonRhoT = ncIn.get(strLon).copyout(ind0, ind);

        ind = (typeModel == Resources.ROMS)
            ? new int[] {
            ny, nx}
            : new int[] {
            ny};
        xlatRhoT = ncIn.get(strLat).copyout(ind0, ind);

        xhT = ncIn.get(strBathy).copyout(new int[] {
            0, 0}
            , new int[] {
            ny, nx});

        int i = nx;
        int j = 0;
        while (i-- > 0) {
          j = ny;
          while (j-- > 0) {
            ind = new int[] {
                j, i};
            if (Math.abs(xhT.getDouble(ind)) >=
                depthMax) {
              depthMax = xhT.getDouble(ind);
            }

            if (typeModel == Resources.MARS) {
              ind = new int[] {
                  i};
            }

            if (xlonRhoT.getDouble(ind) >= (lonMax)) {
              lonMax = xlonRhoT.getDouble(ind);
            }
            if ( (xlonRhoT.getDouble(ind)) <= (lonMin)) {
              lonMin = xlonRhoT.getDouble(ind);
            }
            if (typeModel == Resources.MARS) {
              ind = new int[] {
                  j};
            }

            if (xlatRhoT.getDouble(ind) >=
                (latMax)) {
              latMax = xlatRhoT.getDouble(ind);
            }
            if ( (xlatRhoT.getDouble(ind)) <=
                (latMin)) {
              latMin = xlatRhoT.getDouble(ind);
            }
          }
        }
        //System.out.println("lonmin " + lonMin + " lonmax " + lonMax + " latmin " + latMin + " latmax " + latMax);

        if (lonMin > lonMax) {
          double lontmp = lonMin;
          lonMin = lonMax;
          lonMax = lontmp;
        }

        if (latMin > latMax) {
          double lattmp = latMin;
          latMin = latMax;
          latMax = lattmp;
        }
      }
      catch (IOException ex1) {
        System.out.println(
            "! Error --> IOException - Unable to extract info from netcdf file "
            + ncIn.getFile().toString());
        JOptionPane.showMessageDialog(this,
            "Problem reading : " + ncIn.getFile().toString()
            + "\nShould check out :"
            + "\n==> ROMS / MARS"
            + "\n==> " + Resources.LBL_INPUT_PATH
            + "\n==> Variable names. Click [" + Resources.BTN_CONFIGURE + "]");
        throw ex1;
      }
      catch (NullPointerException ex2) {
        System.out.println(
            "! Error --> NullException - Unable to extract info from netcdf file "
            + ncIn.getFile().toString());
        JOptionPane.showMessageDialog(this,
            "Problem reading : " + ncIn.getFile().toString()
            + "\nShould check out :"
            + "\n==> ROMS / MARS"
            + "\n==> " + Resources.LBL_INPUT_PATH
            + "\n==> Variable names. Click [" + Resources.BTN_CONFIGURE + "]");
        throw ex2;
      }

      this.lonMin = lonMin;
      this.lonMax = lonMax;
      this.latMin = latMin;
      this.latMax = latMax;
      this.depthMax = depthMax;
      this.dtR = dtR;
      this.dt = dt;
    }

    //----------------------------------------------------------------------
    private void getFirstFile() {
      String[] listFileTmp = inputPath.list();
      ArrayList listF = new ArrayList();
      for (int i = 0; i < listFileTmp.length; i++) {
        if (listFileTmp[i].endsWith(".nc") | listFileTmp[i].endsWith(".nc.1")) {
          listF.add(listFileTmp[i]);
        }
      }
      if (listF.size() == 0) {
        JOptionPane.showMessageDialog(this,
            "No netcdf file in directory : " + inputPath.toString());
        return;
      }

      Iterator iter = listF.iterator();
      NetcdfFile ncIn;
      String strVarTime = strTime;
      VariableIterator iterVar;
      boolean blnErr = true;
      double t0 = Double.MAX_VALUE, tf;
      String f = new String();
      while (iter.hasNext()) {
        try {
          f = (String) iter.next();
          ncIn = new NetcdfFile(txtInputPath.getText().replace('/',
              File.separatorChar) + File.separatorChar + f, true);
          iterVar = ncIn.iterator();
          while (iterVar.hasNext()) {
            strVarTime = iterVar.next().getName();
            if (strVarTime.matches(strTime)) {
              blnErr = false;
              break;
            }
          }
          if (blnErr) {
            iterVar = ncIn.iterator();
            while (iterVar.hasNext()) {
              strVarTime = iterVar.next().getName();
              if (strVarTime.startsWith("time") || strVarTime.endsWith("time")) {
                blnErr = false;
                break;
              }
            }
          }
          if (blnErr) {
            strVarTime = "time";
            break;
          }
          //System.out.println("getFirstFile " + f + " " + strVarTime);
          tf = ncIn.get(strVarTime).copyout(new int[] {
              0}, new int[] {
              1}).getDouble(new int[] {
              0});
          if (tf < t0) {
            firstFile = f;
            t0 = tf;
          }
        }
        catch (IOException ex) {
          System.out.println("! Error --> IOException - Problem reading "
              + f.toString());
        }
        catch (java.lang.NullPointerException ex) {
          System.out.println(
              "! Error --> NullException - Unable to find variable "
              + strVarTime
              + " in file " + f.toString());
        }
        catch (IllegalArgumentException ex) {
          System.out.println(
              "! Error --> IllegalArgumentException - Problem reading variable "
              + strVarTime + " in file " + f.toString());
        }
      }
      if (firstFile == null) {
        resetPnlFile();
        if (rdBtnPhysicalTime.isSelected()) {
          rdBtnPhysicalTime.doClick();
        }
        else {
          rdBtnClimatoTime.doClick();
        }
        JOptionPane.showMessageDialog(this,
            "Unable to read variable " + strVarTime
            + " in netcdf files of directory : " + inputPath.toString());
        return;
      }
    }

    //--------------------------------------------------------------------------
    private void resetPnlFile() {
      lblDt.setText(Resources.LBL_DT_MODEL);
      lblRecordDt.setText(Resources.LBL_DT_RECORDS);
      lblLonMin.setText(Resources.LBL_LON_MIN);
      lblLonMax.setText(Resources.LBL_LON_MAX);
      lblLatMin.setText(Resources.LBL_LAT_MIN);
      lblLatMax.setText(Resources.LBL_LAT_MAX);
      lblDepthMax.setText(Resources.LBL_DEPTH_MAX);
    }

    //--------------------------------------------------------------------------
    public void actionPerformed(ActionEvent e) {

      Object src = e.getSource();
      boolean enabled;

      // RadioButton ROMS
      if (src == rdBtnROMS) {
        HAS_CHANGED = true;
        typeModel = Resources.ROMS;
        rdBtnPhysicalTime.setEnabled(true);
        rdBtnClimatoTime.setEnabled(true);
        updateFieldNames();
        resetPnlFile();
        if (typeCalendar == Resources.INTERANNUAL) {
          rdBtnPhysicalTime.doClick();
        }
        else {
          rdBtnClimatoTime.doClick();
        }
      }
      //RadioButton MARS
      if (src == rdBtnMARS) {
        HAS_CHANGED = true;
        typeModel = Resources.MARS;
        typeCalendar = Resources.INTERANNUAL;
        updateFieldNames();
        resetPnlFile();
        rdBtnPhysicalTime.doClick();
        //rdBtnPhysicalTime.setEnabled(false);
        rdBtnClimatoTime.setEnabled(false);
      }
      if (src == btnCfgModel) {
        HAS_CHANGED = true;
        if (firstFile != null) {
          File fileNC = new File(txtInputPath.getText().replace('/',
              File.separatorChar) + File.separatorChar
              + firstFile);
          try {
            new JFieldFrame(new NetcdfFile(fileNC, true));
          }
          catch (IOException ex) {
          }

        }
        else {
          if (inputPath != null) {
            JOptionPane.showMessageDialog(this,
                "Problem reading files of directory : " + inputPath.toString()
                + "\nShould check out :"
                + "\n==> " + Resources.LBL_INPUT_PATH
                + "\n==> ROMS / MARS");
          }
          else {
            JOptionPane.showMessageDialog(this,
                "Set input path first.");
          }
        }
      }
      if (src == rdBtnEULER) {
        HAS_CHANGED = true;
        typeScheme = Resources.EULER;
      }
      if (src == rdBtnRK4) {
        HAS_CHANGED = true;
        typeScheme = Resources.RK4;
      }
      if (src == rdBtn2D) {
        HAS_CHANGED = true;
        dimSimu = Resources.SIMU_2D;
        updateFieldNames();
        ckBoxIsoDepth.setSelected(false);
        ckBoxIsoDepth.setEnabled(false);
        prmIsoDepth.setEnabled(false);
        ckBoxLethalTp.setSelected(true);
        ckBoxLethalTp.doClick();
        ckBoxLethalTp.setEnabled(false);
        pnlReleasing.prmDepthMax.setEnabled(false);
        pnlReleasing.prmDepthMin.setEnabled(false);
        pnlReleasing.prmPatchThickness.setEnabled(false);

      }
      if (src == rdBtn3D) {
        HAS_CHANGED = true;
        dimSimu = Resources.SIMU_3D;
        updateFieldNames();
        ckBoxIsoDepth.setEnabled(true);
        ckBoxLethalTp.setEnabled(true);
        pnlReleasing.prmDepthMax.setEnabled(true);
        pnlReleasing.prmDepthMin.setEnabled(true);
        pnlReleasing.prmPatchThickness.setEnabled(pnlReleasing.ckBoxPatchiness.
            isSelected());
      }

      if (src == btnEditInputPath) {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnPath = chooser.showOpenDialog(ConfigFrame.this);
        if (returnPath == JFileChooser.APPROVE_OPTION) {
          HAS_CHANGED = true;
          inputPath = chooser.getSelectedFile();
          txtInputPath.setText(inputPath.toString());
          firstFile = null;
          getFirstFile();
          if (firstFile != null) {
            setInfoPnlFile();
            setInfoPnlTime();
          }
        }
      }
      if (src == btnEditOutputPath) {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnPath = chooser.showOpenDialog(ConfigFrame.this);
        if (returnPath == JFileChooser.APPROVE_OPTION) {
          HAS_CHANGED = true;
          File outputPath = chooser.getSelectedFile();
          txtOutputPath.setText(outputPath.toString());
        }
      }
      if (src == ckBoxBuoy) {
        HAS_CHANGED = true;
        prmDensity.setEnabled(ckBoxBuoy.isSelected());
        prmAgeBuoy.setEnabled(ckBoxBuoy.isSelected()
            && !ckBoxGrowth.isSelected());
      }
      if (src == ckBoxLethalTp) {
        HAS_CHANGED = true;
        enabled = ckBoxLethalTp.isSelected();
        prmTpEggs.setEnabled(enabled);
        prmTpLarvae.setEnabled(enabled && ckBoxGrowth.isSelected());
      }
      if (src == ckBoxGrowth) {
        prmAgeBuoy.setEnabled(ckBoxBuoy.isSelected()
            && !ckBoxGrowth.isSelected());
        prmTpLarvae.setEnabled(ckBoxLethalTp.isSelected()
            && ckBoxGrowth.isSelected());
        pnlRecruit.actionPerformed(e);
      }
      if (src == rdBtnPhysicalTime) {
        HAS_CHANGED = true;
        typeCalendar = Resources.INTERANNUAL;
        Calendar cld = new Calendar1900(Resources.YEAR_ORIGIN,
            Resources.MONTH_ORIGIN,
            Resources.DAY_ORIGIN, 0, 0, 0);
        prmBeginingSimulation.setCalendar(cld);
        prmBeginingSimulation.setFormatPolicy("yyyy/MM/dd HH:mm");
        prmBeginingSimulation.setValue(prmBeginingSimulation.
            getDefaultValue().longValue());
        prmTransportDuration.setValue(prmTransportDuration.
            getDefaultValue().longValue());
      }
      if (src == rdBtnClimatoTime) {
        HAS_CHANGED = true;
        typeCalendar = Resources.CLIMATO;
        Calendar cld = new ClimatoCalendar();
        prmBeginingSimulation.setCalendar(cld);
        prmBeginingSimulation.setFormatPolicy("yyyy/MM/dd HH:mm");
        prmBeginingSimulation.setValue(prmBeginingSimulation.
            getDefaultValue().longValue());
        prmTransportDuration.setValue(prmTransportDuration.
            getDefaultValue().longValue());
      }
      if (src == btnDefaultTime) {
        if (inputPath != null) {
          HAS_CHANGED = true;
          if (inputPath.exists() && firstFile == null) {
            getFirstFile();
          }
          if (firstFile != null) {
            setInfoPnlTime();
          }
        }
        else {
          JOptionPane.showMessageDialog(this,
              "Set input path first");
        }
      }
      if (src == ckBoxIsoDepth) {
        HAS_CHANGED = true;
        enabled = ckBoxIsoDepth.isSelected();
        prmIsoDepth.setEnabled(enabled);
        pnlReleasing.prmDepthMax.setEnabled(!enabled);
        pnlReleasing.prmDepthMin.setEnabled(!enabled);
        pnlReleasing.prmPatchThickness.setEnabled(!enabled &
            pnlReleasing.ckBoxPatchiness.isSelected());
      }
      if (src == rdBtnRecordNone) {
        HAS_CHANGED = true;
        typeRecord = Resources.RECORD_NONE;
        prmRecordDt.setEnabled(!rdBtnRecordNone.isSelected());
      }
      if (src == rdBtnRecordTxt) {
        if (txtOutputPath.getText().length() == 0) {
          JOptionPane.showMessageDialog(this, "Set output path first");
          rdBtnRecordNone.doClick();
          return;
        }
        HAS_CHANGED = true;
        typeRecord = Resources.RECORD_TXT;
        prmRecordDt.setEnabled(rdBtnRecordTxt.isSelected());
      }
      if (src == rdBtnRecordNc) {
        if (txtOutputPath.getText().length() == 0) {
          JOptionPane.showMessageDialog(this, "Set output path first");
          rdBtnRecordNone.doClick();
          return;
        }
        HAS_CHANGED = true;
        typeRecord = Resources.RECORD_NC;
        prmRecordDt.setEnabled(rdBtnRecordNc.isSelected());
      }
      if (src == btnGetInfo) {
        if (inputPath != null) {
          if (inputPath.exists() && firstFile == null) {
            getFirstFile();
          }
          if (firstFile != null) {
            setInfoPnlFile();
          }
        }
        else {
          JOptionPane.showMessageDialog(this,
              "Set input path first");
        }
      }

      //btnSAVE.setEnabled(HAS_CHANGED | paramHasChanged());
    }
    //--------------------------------------------------------------------------
    // End of class FrameConfig

  }

  //############################################################################
  private class JReleasePanel
      extends JPanel implements ActionListener {

    ZoneEditor releaseZoneEditor;
    JButton btnOpenFile;
    JTextField txtFile;
    JRadioButton rdBtnZone, rdBtnFile;
    JCheckBox ckBoxPulsation, ckBoxPatchiness;

    IntegerParamIBM prmNbReleased = new IntegerParamIBM(
        Resources.PRM_NB_RELEASED, 1000, Resources.UNIT_NONE, true);
    DurationParamIBM prmReleaseDt = new DurationParamIBM(
        Resources.PRM_RELEASING_DT, "0000/00:00",
        Resources.UNIT_DURATION, false);
    IntegerParamIBM prmNbPatches = new IntegerParamIBM(
        Resources.PRM_NB_PATCHES, 0, Resources.UNIT_NONE, false);
    IntegerParamIBM prmPatchRadius = new IntegerParamIBM(
        Resources.PRM_RADIUS_PATCH, 0, Resources.UNIT_METER, false);
    IntegerParamIBM prmPatchThickness = new IntegerParamIBM(
        Resources.PRM_THICKNESS_PATCH, 0, Resources.UNIT_METER, false);
    IntegerParamIBM prmNbReleaseEvents = new IntegerParamIBM(
        Resources.PRM_NB_RELEASING_VENTS, 1, Resources.UNIT_NONE, false);
    IntegerParamIBM prmDepthMin = new IntegerParamIBM(
        Resources.PRM_DEPTH_RELEASING_MIN, 0, Resources.UNIT_METER, true);
    IntegerParamIBM prmDepthMax = new IntegerParamIBM(
        Resources.PRM_DEPTH_RELEASING_MAX, 50, Resources.UNIT_METER, true);

    //--------------------------------------------------------------------------
    public JReleasePanel() {
      super(new GridBagLayout());
      createUI();
    }

    //--------------------------------------------------------------------------
    private void createUI() {

      releaseZoneEditor = new ZoneEditor(Resources.RELEASE);
      btnOpenFile = new JButton("...");
      txtFile = new JTextField(Resources.TXT_FIELD_DRIFTER_FILE);
      btnOpenFile.setEnabled(false);
      txtFile.setEditable(false);
      txtFile.setBackground(Color.WHITE);
      txtFile.setMargin(new Insets(1, 5, 1, 5));
      txtFile.setEnabled(false);

      ButtonGroup grp = new ButtonGroup();
      rdBtnZone = new JRadioButton(Resources.RD_BTN_RELEASE_ZONE, true);
      rdBtnFile = new JRadioButton(Resources.RD_BTN_RELEASE_FILE, false);
      grp.add(rdBtnZone);
      grp.add(rdBtnFile);

      ckBoxPulsation = new JCheckBox(Resources.CK_BOX_PULSATION, false);
      ckBoxPulsation.addActionListener(this);
      ckBoxPatchiness = new JCheckBox(Resources.CK_BOX_PATCHINESS, false);
      ckBoxPatchiness.addActionListener(this);

      this.add(rdBtnZone, new GridBagConstraints(0, 0, 1, 1, 10, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(rdBtnFile, new GridBagConstraints(1, 0, 1, 1, 10, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(txtFile, new GridBagConstraints(2, 0, 1, 1, 70, 10,
          GridBagConstraints.CENTER,
          GridBagConstraints.HORIZONTAL,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(btnOpenFile, new GridBagConstraints(3, 0, 1, 1, 10, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));

      int j = 1;
      this.add(prmNbReleased.createGUI(),
          new GridBagConstraints(0, j++, 4, 1, 100, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(prmDepthMin.createGUI(),
          new GridBagConstraints(0, j++, 4, 1, 100, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(prmDepthMax.createGUI(),
          new GridBagConstraints(0, j++, 4, 1, 100, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(ckBoxPulsation,
          new GridBagConstraints(0, j++, 4, 1, 100, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(prmNbReleaseEvents.createGUI(),
          new GridBagConstraints(0, j++, 4, 1, 100, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 30, 5, 5), 0, 0));
      this.add(prmReleaseDt.createGUI(),
          new GridBagConstraints(0, j++, 4, 1, 100, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 30, 5, 5), 0, 0));

      this.add(ckBoxPatchiness, new GridBagConstraints(0, j++, 4, 1, 100, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(prmNbPatches.createGUI(),
          new GridBagConstraints(0, j++, 4, 1, 100, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 30, 5, 5), 0, 0));
      this.add(prmPatchRadius.createGUI(),
          new GridBagConstraints(0, j++, 4, 1, 100, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 30, 5, 5), 0, 0));
      this.add(prmPatchThickness.createGUI(),
          new GridBagConstraints(0, j++, 4, 1, 100, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 30, 5, 5), 0, 0));
      this.add(releaseZoneEditor, new GridBagConstraints(0, j++, 4, 1, 100, 40,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));

      //Add listeners
      rdBtnZone.addActionListener(this);
      rdBtnFile.addActionListener(this);
      btnOpenFile.addActionListener(this);
      ckBoxPulsation.addActionListener(this);
      ckBoxPatchiness.addActionListener(this);
    }

    //----------------------------------------------------------------------------
    private void loadReleaseZones() {
      String strZone;
      double[] lon = new double[4];
      double[] lat = new double[4];
      double bathyMin, bathyMax;
      Color color;
      int colorR, colorG, colorB;
      int nbZones;

      nbZones = cfgIn.getIntegerProperty(Resources.STR_SECTION_RELEASE,
          Resources.STR_CFG_NB_SZONES);
      for (int i = 0; i < nbZones; i++) {
        strZone = Resources.STR_SECTION_SZONE + String.valueOf(i + 1);
        for (int j = 0; j < 4; j++) {
          lon[j] = cfgIn.getDoubleProperty(strZone,
              Resources.STR_CFG_LON_ZONE +
              String.valueOf(j + 1));
          lat[j] = cfgIn.getDoubleProperty(strZone,
              Resources.STR_CFG_LAT_ZONE +
              String.valueOf(j + 1));
        }
        bathyMin = cfgIn.getDoubleProperty(strZone,
            Resources.STR_CFG_BATHY_MIN);
        bathyMax = cfgIn.getDoubleProperty(strZone,
            Resources.STR_CFG_BATHY_MAX);
        colorR = cfgIn.getIntegerProperty(strZone, Resources.STR_CFG_RED);
        colorG = cfgIn.getIntegerProperty(strZone, Resources.STR_CFG_GREEN);
        colorB = cfgIn.getIntegerProperty(strZone, Resources.STR_CFG_BLUE);
        color = new Color(colorR, colorG, colorB);
        pnlReleasing.releaseZoneEditor.setZone(new Zone(Resources.RELEASE, i + 1,
            lon[0],
            lat[0],
            lon[1], lat[1], lon[2],
            lat[2], lon[3], lat[3], bathyMin, bathyMax, color));
      }

    }

    //--------------------------------------------------------------------------
    public void actionPerformed(ActionEvent e) {

      Object src = e.getSource();

      if (src == rdBtnZone) {
        HAS_CHANGED = true;
        typeRelease = Resources.RELEASE_ZONE;
        prmNbReleased.setEnabled(true);
        ckBoxPulsation.setEnabled(true);
        prmReleaseDt.setEnabled(ckBoxPulsation.isSelected());
        prmNbReleaseEvents.setEnabled(ckBoxPulsation.isSelected());
        prmDepthMin.setEnabled(!pnlGeneralOption.rdBtn2D.isSelected()
            && !pnlGeneralOption.ckBoxIsoDepth.isSelected());
        prmDepthMax.setEnabled(!pnlGeneralOption.rdBtn2D.isSelected()
            && !pnlGeneralOption.ckBoxIsoDepth.isSelected());
        releaseZoneEditor.setEnabled(true);
        btnOpenFile.setEnabled(false);
        txtFile.setEnabled(false);
        ckBoxPatchiness.setEnabled(true);
        prmNbPatches.setEnabled(ckBoxPatchiness.isSelected());
        prmPatchRadius.setEnabled(ckBoxPatchiness.isSelected());
        prmPatchThickness.setEnabled(ckBoxPatchiness.isSelected()
            && !pnlGeneralOption.ckBoxIsoDepth.isSelected()
            && !pnlGeneralOption.rdBtn2D.isSelected());
        if (cfgIn != null && (releaseZoneEditor.getArrayZones().size() == 0)) {
          loadReleaseZones();
        }
        //pnlGeneralOption.ckBoxIsoDepth.setEnabled(true);
      }

      if (src == rdBtnFile) {
        HAS_CHANGED = true;
        typeRelease = Resources.RELEASE_FILE;
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
        btnOpenFile.setEnabled(true);
        txtFile.setEnabled(true);
      }

      if (src == btnOpenFile) {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        IBMFileFilter ff = new IBMFileFilter(Resources.EXTENSION_DRIFTER);
        chooser.setFileFilter(ff);
        int returnPath = chooser.showOpenDialog(ConfigFrame.this);
        if (returnPath == JFileChooser.APPROVE_OPTION) {
          HAS_CHANGED = true;
          txtFile.setText(chooser.getSelectedFile().toString());
        }
      }

      if (src == ckBoxPulsation) {
        HAS_CHANGED = true;
        prmReleaseDt.setEnabled(ckBoxPulsation.isSelected());
        prmNbReleaseEvents.setEnabled(ckBoxPulsation.isSelected());
      }
      if (src == ckBoxPatchiness) {
        HAS_CHANGED = true;
        prmNbPatches.setEnabled(ckBoxPatchiness.isSelected());
        prmPatchRadius.setEnabled(ckBoxPatchiness.isSelected());
        prmPatchThickness.setEnabled(ckBoxPatchiness.isSelected() &&
            (!pnlGeneralOption.ckBoxIsoDepth.
            isSelected() &
            !pnlGeneralOption.rdBtn2D.isSelected()));
      }

      //btnSAVE.setEnabled(HAS_CHANGED | paramHasChanged());
    }

    //--------------------------------------------------------------------------
    private void setDefaultValue() {
      boolean bln;
      if (cfgIn != null) {
        bln = cfgIn.getIntegerProperty(Resources.STR_SECTION_RELEASE,
            Resources.STR_CFG_TYPE_RELEASE) == Resources.RELEASE_ZONE;
        if (bln) {
          rdBtnZone.doClick();
          prmNbReleased.setValue(cfgIn.getIntegerProperty(Resources.
              STR_SECTION_RELEASE,
              Resources.STR_CFG_NB_PARTICLES));
          prmDepthMin.setValue(cfgIn.getIntegerProperty(Resources.
              STR_SECTION_RELEASE,
              Resources.STR_CFG_DEPTH_MIN));
          prmDepthMax.setValue(cfgIn.getIntegerProperty(Resources.
              STR_SECTION_RELEASE,
              Resources.STR_CFG_DEPTH_MAX));

          bln = cfgIn.getBooleanProperty(Resources.STR_SECTION_RELEASE,
              Resources.STR_CFG_PULSATION);
          if (bln) {
            ckBoxPulsation.setSelected(false);
            ckBoxPulsation.doClick();
            prmNbReleaseEvents.setValue(cfgIn.getIntegerProperty(
                Resources.STR_SECTION_RELEASE,
                Resources.STR_CFG_NB_RELEASING_EVENTS));
            prmReleaseDt.setEnabled(true);
            prmReleaseDt.setValue(cfgIn.getLongProperty(
                Resources.STR_SECTION_RELEASE,
                Resources.STR_CFG_RELEASING_DT).longValue());
          }

          bln = cfgIn.getBooleanProperty(Resources.STR_SECTION_RELEASE,
              Resources.STR_CFG_PATCHINESS);
          if (bln) {
            ckBoxPatchiness.setSelected(false);
            ckBoxPatchiness.doClick();
            prmNbPatches.setValue(cfgIn.getIntegerProperty(
                Resources.STR_SECTION_RELEASE,
                Resources.STR_CFG_NB_PATCHES));
            prmPatchRadius.setValue(cfgIn.getIntegerProperty(
                Resources.STR_SECTION_RELEASE,
                Resources.STR_CFG_RADIUS_PATCH));
            prmPatchThickness.setValue(cfgIn.getIntegerProperty(
                Resources.STR_SECTION_RELEASE,
                Resources.STR_CFG_THICK_PATCH));
          }
        }
        else {
          rdBtnFile.doClick();
          txtFile.setText(cfgIn.getStringProperty(Resources.STR_SECTION_RELEASE,
              Resources.STR_CFG_DRIFTERS));
        }
      }
    }
  }

  //############################################################################
  private class JRecruitPanel
      extends JPanel implements ActionListener {

    JRadioButton rdBtnNoRecruit, rdBtnRecruitAge, rdBtnRecruitLength;
    ZoneEditor recruitmentZoneEditor;

    FloatParamIBM prmAgeRecruit = new FloatParamIBM(
        Resources.PRM_RECRUIT_AGE, 14.0f, Resources.UNIT_DAY, false);
    FloatParamIBM prmLengthRecruit = new FloatParamIBM(
        Resources.PRM_RECRUIT_LENGTH, 14.0f, Resources.UNIT_MILLIMETER, false);
    FloatParamIBM prmDurationRecruit = new FloatParamIBM(
        Resources.PRM_RECRUIT_DURATION_MIN, 0, Resources.UNIT_DAY, false);

    //------------------------------------------------------------------------
    public JRecruitPanel() {
      super(new GridBagLayout());
      createUI();
    }

    //------------------------------------------------------------------------
    private void createUI() {

      ButtonGroup grpRecruit = new ButtonGroup();
      rdBtnNoRecruit = new JRadioButton(Resources.RD_BTN_RECRUIT_NONE, true);
      rdBtnRecruitAge = new JRadioButton(Resources.RD_BTN_RECRUIT_AGE, false);
      recruitmentZoneEditor = new ZoneEditor(Resources.RECRUITMENT);
      recruitmentZoneEditor.setEnabled(false);
      rdBtnRecruitLength = new JRadioButton(Resources.RD_BTN_RECRUIT_LENGTH, false);
      rdBtnRecruitLength.setEnabled(false);
      grpRecruit.add(rdBtnNoRecruit);
      grpRecruit.add(rdBtnRecruitAge);
      grpRecruit.add(rdBtnRecruitLength);

      this.add(rdBtnNoRecruit,
          new GridBagConstraints(0, 0, 1, 1, 30, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(rdBtnRecruitAge,
          new GridBagConstraints(1, 0, 1, 1, 30, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));
      this.add(rdBtnRecruitLength,
          new GridBagConstraints(2, 0, 1, 1, 30, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 5, 5, 5), 0, 0));

      this.add(prmAgeRecruit.createGUI(),
          new GridBagConstraints(0, 1, 3, 1, 90, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 30, 5, 5), 0, 0));
      this.add(prmLengthRecruit.createGUI(),
          new GridBagConstraints(0, 2, 3, 1, 90, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 30, 5, 5), 0, 0));
      this.add(prmDurationRecruit.createGUI(),
          new GridBagConstraints(0, 3, 3, 1, 90, 10,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(5, 30, 5, 5), 0, 0));

      this.add(recruitmentZoneEditor, new GridBagConstraints(0, 4, 3, 1, 90, 40,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));

      //Add listener
      rdBtnNoRecruit.addActionListener(this);
      rdBtnRecruitAge.addActionListener(this);
      rdBtnRecruitLength.addActionListener(this);
    }

    //----------------------------------------------------------------------------
    private void loadRecruitmentZones() {
      String strZone;
      double[] lon = new double[4];
      double[] lat = new double[4];
      double bathyMin, bathyMax;
      Color color;
      int colorR, colorG, colorB;
      int nbZones;

      nbZones = cfgIn.getIntegerProperty(Resources.STR_SECTION_RECRUIT,
          Resources.STR_CFG_NB_RZONES);
      for (int i = 0; i < nbZones; i++) {
        strZone = Resources.STR_SECTION_RZONE + String.valueOf(i + 1);
        for (int j = 0; j < 4; j++) {
          lon[j] = cfgIn.getDoubleProperty(strZone,
              Resources.STR_CFG_LON_ZONE +
              String.valueOf(j + 1));
          lat[j] = cfgIn.getDoubleProperty(strZone,
              Resources.STR_CFG_LAT_ZONE +
              String.valueOf(j + 1));
        }
        bathyMin = cfgIn.getDoubleProperty(strZone,
            Resources.STR_CFG_BATHY_MIN);
        bathyMax = cfgIn.getDoubleProperty(strZone,
            Resources.STR_CFG_BATHY_MAX);
        colorR = cfgIn.getIntegerProperty(strZone, Resources.STR_CFG_RED);
        colorG = cfgIn.getIntegerProperty(strZone, Resources.STR_CFG_GREEN);
        colorB = cfgIn.getIntegerProperty(strZone, Resources.STR_CFG_BLUE);
        color = new Color(colorR, colorG, colorB);
        pnlRecruit.recruitmentZoneEditor.setZone(new Zone(Resources.RECRUITMENT, i + 1,
            lon[0],
            lat[0],
            lon[1], lat[1], lon[2],
            lat[2], lon[3], lat[3],
            bathyMin, bathyMax, color));
      }
    }

    //------------------------------------------------------------------------
    public void actionPerformed(ActionEvent e) {
      Object src = e.getSource();
      boolean enabled;

      if (src == pnlGeneralOption.ckBoxGrowth) {
        rdBtnRecruitLength.setEnabled(pnlGeneralOption.ckBoxGrowth.
            isSelected());
        if (rdBtnRecruitLength.isSelected()) {
          typeRecruitment = Resources.RECRUIT_NONE;
          rdBtnNoRecruit.setSelected(true);
          prmAgeRecruit.setEnabled(false);
          prmLengthRecruit.setEnabled(false);
          prmDurationRecruit.setEnabled(false);
          recruitmentZoneEditor.setEnabled(false);
        }
      }
      if (src == rdBtnNoRecruit) {
        HAS_CHANGED = true;
        typeRecruitment = Resources.RECRUIT_NONE;
        enabled = rdBtnNoRecruit.isSelected();
        prmAgeRecruit.setEnabled(!enabled);
        prmLengthRecruit.setEnabled(!enabled);
        prmDurationRecruit.setEnabled(!enabled);
        recruitmentZoneEditor.setEnabled(!enabled);
      }
      if (src == rdBtnRecruitAge) {
        HAS_CHANGED = true;
        typeRecruitment = Resources.RECRUIT_AGE;
        enabled = rdBtnRecruitAge.isSelected();
        prmAgeRecruit.setEnabled(enabled);
        prmLengthRecruit.setEnabled(!enabled);
        prmDurationRecruit.setEnabled(enabled);
        recruitmentZoneEditor.setEnabled(enabled);
        if (cfgIn != null && (recruitmentZoneEditor.getArrayZones().size() == 0)) {
          loadRecruitmentZones();
        }
      }
      if (src == rdBtnRecruitLength) {
        HAS_CHANGED = true;
        typeRecruitment = Resources.RECRUIT_LENGTH;
        enabled = rdBtnRecruitLength.isSelected();
        prmAgeRecruit.setEnabled(!enabled);
        prmLengthRecruit.setEnabled(enabled);
        prmDurationRecruit.setEnabled(enabled);
        recruitmentZoneEditor.setEnabled(enabled);
        if (cfgIn != null && (recruitmentZoneEditor.getArrayZones().size() == 0)) {
          loadRecruitmentZones();
        }
      }

      //btnSAVE.setEnabled(HAS_CHANGED | paramHasChanged());
    }

    //--------------------------------------------------------------------------
    void setDefaultValue() {
      if (cfgIn != null) {
        typeRecruitment = cfgIn.getIntegerProperty(
            Resources.STR_SECTION_RECRUIT, Resources.STR_CFG_RECRUIT);
        switch (typeRecruitment) {
          case Resources.RECRUIT_NONE:
            rdBtnNoRecruit.doClick();
            break;
          case Resources.RECRUIT_AGE:
            rdBtnRecruitAge.doClick();
            prmAgeRecruit.setValue(cfgIn.getDoubleProperty(
                Resources.STR_SECTION_RECRUIT,
                Resources.STR_CFG_AGE_RECRUIT).floatValue());
            prmDurationRecruit.setValue(cfgIn.getDoubleProperty(
                Resources.STR_SECTION_RECRUIT,
                Resources.STR_CFG_DURATION_RECRUIT).floatValue());
            break;
          case Resources.RECRUIT_LENGTH:
            rdBtnRecruitLength.doClick();
            prmLengthRecruit.setValue(cfgIn.getDoubleProperty(
                Resources.STR_SECTION_RECRUIT,
                Resources.STR_CFG_LENGTH_RECRUIT).floatValue());
            prmDurationRecruit.setValue(cfgIn.getDoubleProperty(
                Resources.STR_SECTION_RECRUIT,
                Resources.STR_CFG_DURATION_RECRUIT).floatValue());
            break;
        }
      }
    }
  }

  //##########################################################################
  private class JFieldFrame
      extends JFrame implements ActionListener {

    private JComboBox cbBoxXiDim, cbBoxEtaDim, cbBoxZDim, cbBoxTimeDim;
    private JComboBox cbBoxLon, cbBoxLat, cbBoxMask, cbBoxBathy, cbBoxPm,
        cbBoxPn,
        cbBoxSigma;
    private JComboBox cbBoxTime, cbBoxZeta, cbBoxU, cbBoxV, cbBoxTp, cbBoxSal;
    private JComboBox cbBoxThetaS, cbBoxThetaB, cbBoxHc;

    private JButton btnOK, btnCANCEL;
    private NetcdfFile ncIn;
    private String[] listDim, listAttrib;
    private Vector listVar;

    //----------------------------------------------------------------------
    public JFieldFrame(NetcdfFile ncIn) {

      this.ncIn = ncIn;
      fillLists();
      createUI();
      this.validate();
      this.setVisible(true);
    }

    //--------------------------------------------------------------------------
    private void fillLists() {
      int i = 0;
      listDim = new String[ncIn.getDimensions().size() + 1];
      DimensionIterator iterDim = ncIn.getDimensions().iterator();
      listDim[i++] = "[undef]";
      while (iterDim.hasNext()) {
        listDim[i++] = iterDim.next().getName();
      }

      i = 0;
      listAttrib = new String[ncIn.getAttributes().size() + 1];
      AttributeIterator iterAttrib = ncIn.getAttributes().iterator();
      listAttrib[i++] = "[undef]";
      while (iterAttrib.hasNext()) {
        listAttrib[i++] = iterAttrib.next().getName();
        //System.out.println("attrib " + listAttrib[i - 1]);
      }

      listVar = new Vector();
      listVar.add("[undef]");
      VariableIterator iterVar = ncIn.iterator();
      while (iterVar.hasNext()) {
        listVar.add(iterVar.next().getName());
      }
    }

    //--------------------------------------------------------------------------
    private void selectDefaultItem(JComboBox cbBox, String str) {
      String strCbBox;
      for (int i = 0; i < cbBox.getItemCount(); i++) {
        strCbBox = (String) cbBox.getItemAt(i);
        //System.out.println(str + " " + strCbBox);
        if (strCbBox.matches(str)) {
          cbBox.setSelectedIndex(i);
          return;
        }
        else if (strCbBox.startsWith(str)) {
          cbBox.setSelectedIndex(i);
        }
      }
    }

    //----------------------------------------------------------------------
    private void createUI() {

      this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

      btnOK = new JButton(Resources.BTN_OK);
      btnCANCEL = new JButton(Resources.BTN_CANCEL);

      cbBoxXiDim = new JComboBox(listDim);
      selectDefaultItem(cbBoxXiDim, strXiDim);
      cbBoxEtaDim = new JComboBox(listDim);
      selectDefaultItem(cbBoxEtaDim, strEtaDim);
      cbBoxTimeDim = new JComboBox(listDim);
      selectDefaultItem(cbBoxTimeDim, strTimeDim);
      cbBoxLon = new JComboBox(listVar);
      selectDefaultItem(cbBoxLon, strLon);
      cbBoxLat = new JComboBox(listVar);
      selectDefaultItem(cbBoxLat, strLat);
      cbBoxBathy = new JComboBox(listVar);
      selectDefaultItem(cbBoxBathy, strBathy);
      cbBoxTime = new JComboBox(listVar);
      selectDefaultItem(cbBoxTime, strTime);
      cbBoxU = new JComboBox(listVar);
      selectDefaultItem(cbBoxU, strU);
      cbBoxV = new JComboBox(listVar);
      selectDefaultItem(cbBoxV, strV);
      cbBoxTp = new JComboBox(listVar);
      selectDefaultItem(cbBoxTp, strTp);
      cbBoxSal = new JComboBox(listVar);
      selectDefaultItem(cbBoxSal, strSal);

      GraphicsDevice ecran = GraphicsEnvironment.getLocalGraphicsEnvironment().
          getDefaultScreenDevice();
      GraphicsConfiguration config = ecran.getDefaultConfiguration();
      Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
      DisplayMode mode = config.getDevice().getDisplayMode();
      Dimension size = new Dimension(mode.getWidth(), mode.getHeight());
      Dimension windows = new Dimension(size.width - insets.left - insets.right,
          size.height - insets.bottom - insets.top);
      this.setSize( (int) (0.5f * windows.width), windows.height);

      switch (typeModel + dimSimu) {
        case (Resources.ROMS + Resources.SIMU_2D):
          creatUI_R2D();
          break;
        case (Resources.ROMS + Resources.SIMU_3D):
          creatUI_R3D();
          break;
        case (Resources.MARS + Resources.SIMU_2D):
          creatUI_M2D();
          break;

        case (Resources.MARS + Resources.SIMU_3D):
          creatUI_M3D();
          break;
      }
      this.getContentPane().add(new JLabel(""));
      this.getContentPane().add(new JLabel(""));
      this.getContentPane().add(btnOK);
      this.getContentPane().add(btnCANCEL);

      btnOK.addActionListener(this);
      btnCANCEL.addActionListener(this);

    }

    //----------------------------------------------------------------------
    private void creatUI_R2D() {

      this.getContentPane().setLayout(new GridLayout(18, 2));
      this.setTitle(Resources.NAME_SHORT + Resources.TITLE_FIELD + "ROMS");

      cbBoxMask = new JComboBox(listVar);
      selectDefaultItem(cbBoxMask, strMask);
      cbBoxPm = new JComboBox(listVar);
      selectDefaultItem(cbBoxPm, strPm);
      cbBoxPn = new JComboBox(listVar);
      selectDefaultItem(cbBoxPn, strPn);

      this.getContentPane().add(new JLabel(Resources.LBL_DIM));
      this.getContentPane().add(new JLabel(" "));
      this.getContentPane().add(new JLabel(Resources.LBL_XI_DIM));
      this.getContentPane().add(cbBoxXiDim);
      this.getContentPane().add(new JLabel(Resources.LBL_ETA_DIM));
      this.getContentPane().add(cbBoxEtaDim);
      this.getContentPane().add(new JLabel(Resources.LBL_TIME_DIM));
      this.getContentPane().add(cbBoxTimeDim);
      this.getContentPane().add(new JLabel(Resources.LBL_CONSTANT_FIELD));
      this.getContentPane().add(new JLabel(" "));
      this.getContentPane().add(new JLabel(Resources.LBL_LONGITUDE));
      this.getContentPane().add(cbBoxLon);
      this.getContentPane().add(new JLabel(Resources.LBL_LATITUDE));
      this.getContentPane().add(cbBoxLat);
      this.getContentPane().add(new JLabel(Resources.LBL_BATHYMETRY));
      this.getContentPane().add(cbBoxBathy);
      this.getContentPane().add(new JLabel(Resources.LBL_MASK));
      this.getContentPane().add(cbBoxMask);
      this.getContentPane().add(new JLabel(Resources.LBL_PN));
      this.getContentPane().add(cbBoxPn);
      this.getContentPane().add(new JLabel(Resources.LBL_PM));
      this.getContentPane().add(cbBoxPm);
      this.getContentPane().add(new JLabel(Resources.LBL_TIME_FIELD));
      this.getContentPane().add(new JLabel(" "));
      this.getContentPane().add(new JLabel(Resources.LBL_U + " 2D"));
      this.getContentPane().add(cbBoxU);
      this.getContentPane().add(new JLabel(Resources.LBL_V + " + 2D"));
      this.getContentPane().add(cbBoxV);
      this.getContentPane().add(new JLabel(Resources.LBL_TIME));
      this.getContentPane().add(cbBoxTime);

    }

    //----------------------------------------------------------------------
    private void creatUI_R3D() {
      this.getContentPane().setLayout(new GridLayout(25, 2));
      this.setTitle(Resources.NAME_SHORT + Resources.TITLE_FIELD + "ROMS");

      cbBoxZDim = new JComboBox(listDim);
      selectDefaultItem(cbBoxZDim, strZDim);
      cbBoxMask = new JComboBox(listVar);
      selectDefaultItem(cbBoxMask, strMask);
      cbBoxZeta = new JComboBox(listVar);
      selectDefaultItem(cbBoxZeta, strZeta);
      cbBoxPm = new JComboBox(listVar);
      selectDefaultItem(cbBoxPm, strPm);
      cbBoxPn = new JComboBox(listVar);
      selectDefaultItem(cbBoxPn, strPn);
      cbBoxThetaS = new JComboBox(listAttrib);
      selectDefaultItem(cbBoxThetaS, strThetaS);
      cbBoxThetaB = new JComboBox(listAttrib);
      selectDefaultItem(cbBoxThetaB, strThetaB);
      cbBoxHc = new JComboBox(listAttrib);
      selectDefaultItem(cbBoxHc, strHc);

      this.getContentPane().add(new JLabel(Resources.LBL_DIM));
      this.getContentPane().add(new JLabel(" "));
      this.getContentPane().add(new JLabel(Resources.LBL_XI_DIM));
      this.getContentPane().add(cbBoxXiDim);
      this.getContentPane().add(new JLabel(Resources.LBL_ETA_DIM));
      this.getContentPane().add(cbBoxEtaDim);
      this.getContentPane().add(new JLabel(Resources.LBL_Z_DIM));
      this.getContentPane().add(cbBoxZDim);
      this.getContentPane().add(new JLabel(Resources.LBL_TIME_DIM));
      this.getContentPane().add(cbBoxTimeDim);
      this.getContentPane().add(new JLabel(Resources.LBL_CONSTANT_FIELD));
      this.getContentPane().add(new JLabel(" "));
      this.getContentPane().add(new JLabel(Resources.LBL_LONGITUDE));
      this.getContentPane().add(cbBoxLon);
      this.getContentPane().add(new JLabel(Resources.LBL_LATITUDE));
      this.getContentPane().add(cbBoxLat);
      this.getContentPane().add(new JLabel(Resources.LBL_BATHYMETRY));
      this.getContentPane().add(cbBoxBathy);
      this.getContentPane().add(new JLabel(Resources.LBL_MASK));
      this.getContentPane().add(cbBoxMask);
      this.getContentPane().add(new JLabel(Resources.LBL_PN));
      this.getContentPane().add(cbBoxPn);
      this.getContentPane().add(new JLabel(Resources.LBL_PM));
      this.getContentPane().add(cbBoxPm);
      this.getContentPane().add(new JLabel(Resources.LBL_TIME_FIELD));
      this.getContentPane().add(new JLabel(" "));
      this.getContentPane().add(new JLabel(Resources.LBL_U + " 3D"));
      this.getContentPane().add(cbBoxU);
      this.getContentPane().add(new JLabel(Resources.LBL_V + " + 3D"));
      this.getContentPane().add(cbBoxV);
      this.getContentPane().add(new JLabel(Resources.LBL_ZETA));
      this.getContentPane().add(cbBoxZeta);
      this.getContentPane().add(new JLabel(Resources.LBL_TEMPERATURE));
      this.getContentPane().add(cbBoxTp);
      this.getContentPane().add(new JLabel(Resources.LBL_SAL));
      this.getContentPane().add(cbBoxSal);
      this.getContentPane().add(new JLabel(Resources.LBL_TIME));
      this.getContentPane().add(cbBoxTime);
      this.getContentPane().add(new JLabel(Resources.LBL_ATTRIBUTE));
      this.getContentPane().add(new JLabel(" "));
      this.getContentPane().add(new JLabel(Resources.LBL_THETAS));
      this.getContentPane().add(cbBoxThetaS);
      this.getContentPane().add(new JLabel(Resources.LBL_THETAB));
      this.getContentPane().add(cbBoxThetaB);
      this.getContentPane().add(new JLabel(Resources.LBL_HC));
      this.getContentPane().add(cbBoxHc);
    }

    //----------------------------------------------------------------------
    private void creatUI_M2D() {
      this.getContentPane().setLayout(new GridLayout(17, 2));
      this.setTitle(Resources.NAME_SHORT + Resources.TITLE_FIELD + "MARS");

      cbBoxSigma = new JComboBox(listVar);
      selectDefaultItem(cbBoxSigma, strSigma);

      this.getContentPane().add(new JLabel(Resources.LBL_DIM));
      this.getContentPane().add(new JLabel(" "));
      this.getContentPane().add(new JLabel(Resources.LBL_XI_DIM));
      this.getContentPane().add(cbBoxXiDim);
      this.getContentPane().add(new JLabel(Resources.LBL_ETA_DIM));
      this.getContentPane().add(cbBoxEtaDim);
      this.getContentPane().add(new JLabel(Resources.LBL_TIME_DIM));
      this.getContentPane().add(cbBoxTimeDim);
      this.getContentPane().add(new JLabel(Resources.LBL_CONSTANT_FIELD));
      this.getContentPane().add(new JLabel(" "));
      this.getContentPane().add(new JLabel(Resources.LBL_LONGITUDE));
      this.getContentPane().add(cbBoxLon);
      this.getContentPane().add(new JLabel(Resources.LBL_LATITUDE));
      this.getContentPane().add(cbBoxLat);
      this.getContentPane().add(new JLabel(Resources.LBL_BATHYMETRY));
      this.getContentPane().add(cbBoxBathy);
      this.getContentPane().add(new JLabel(Resources.LBL_TIME_FIELD));
      this.getContentPane().add(new JLabel(" "));
      this.getContentPane().add(new JLabel(Resources.LBL_U + " 2D"));
      this.getContentPane().add(cbBoxU);
      this.getContentPane().add(new JLabel(Resources.LBL_V + " + 2D"));
      this.getContentPane().add(cbBoxV);
      this.getContentPane().add(new JLabel(Resources.LBL_TIME));
      this.getContentPane().add(cbBoxTime);
    }

    //----------------------------------------------------------------------
    private void creatUI_M3D() {
      this.getContentPane().setLayout(new GridLayout(20, 2));
      this.setTitle(Resources.NAME_SHORT + Resources.TITLE_FIELD + "MARS");
      cbBoxZDim = new JComboBox(listDim);
      selectDefaultItem(cbBoxZDim, strZDim);
      cbBoxZeta = new JComboBox(listVar);
      selectDefaultItem(cbBoxZeta, strZeta);
      cbBoxSigma = new JComboBox(listVar);
      selectDefaultItem(cbBoxSigma, strSigma);

      this.getContentPane().add(new JLabel(Resources.LBL_DIM));
      this.getContentPane().add(new JLabel(" "));
      this.getContentPane().add(new JLabel(Resources.LBL_XI_DIM));
      this.getContentPane().add(cbBoxXiDim);
      this.getContentPane().add(new JLabel(Resources.LBL_ETA_DIM));
      this.getContentPane().add(cbBoxEtaDim);
      this.getContentPane().add(new JLabel(Resources.LBL_Z_DIM));
      this.getContentPane().add(cbBoxZDim);
      this.getContentPane().add(new JLabel(Resources.LBL_TIME_DIM));
      this.getContentPane().add(cbBoxTimeDim);
      this.getContentPane().add(new JLabel(Resources.LBL_CONSTANT_FIELD));
      this.getContentPane().add(new JLabel(" "));
      this.getContentPane().add(new JLabel(Resources.LBL_LONGITUDE));
      this.getContentPane().add(cbBoxLon);
      this.getContentPane().add(new JLabel(Resources.LBL_LATITUDE));
      this.getContentPane().add(cbBoxLat);
      this.getContentPane().add(new JLabel(Resources.LBL_BATHYMETRY));
      this.getContentPane().add(cbBoxBathy);
      this.getContentPane().add(new JLabel(Resources.LBL_SIGMA));
      this.getContentPane().add(cbBoxSigma);
      this.getContentPane().add(new JLabel(Resources.LBL_TIME_FIELD));
      this.getContentPane().add(new JLabel(" "));
      this.getContentPane().add(new JLabel(Resources.LBL_U + " 3D"));
      this.getContentPane().add(cbBoxU);
      this.getContentPane().add(new JLabel(Resources.LBL_V + " + 3D"));
      this.getContentPane().add(cbBoxV);
      this.getContentPane().add(new JLabel(Resources.LBL_ZETA));
      this.getContentPane().add(cbBoxZeta);
      this.getContentPane().add(new JLabel(Resources.LBL_TEMPERATURE));
      this.getContentPane().add(cbBoxTp);
      this.getContentPane().add(new JLabel(Resources.LBL_SAL));
      this.getContentPane().add(cbBoxSal);
      this.getContentPane().add(new JLabel(Resources.LBL_TIME));
      this.getContentPane().add(cbBoxTime);
    }

    //----------------------------------------------------------------------
    public void actionPerformed(ActionEvent e) {

      if (e.getSource() == btnOK) {
        switch (typeModel + dimSimu) {
          case (Resources.ROMS + Resources.SIMU_2D):
            strXiDim_R = cbBoxXiDim.getSelectedItem().toString();
            strEtaDim_R = cbBoxEtaDim.getSelectedItem().toString();
            strTimeDim_R = cbBoxTimeDim.getSelectedItem().toString();
            strLon_R = cbBoxLon.getSelectedItem().toString();
            strLat_R = cbBoxLat.getSelectedItem().toString();
            strBathy_R = cbBoxBathy.getSelectedItem().toString();
            strMask_R = cbBoxMask.getSelectedItem().toString();
            strPn = cbBoxPn.getSelectedItem().toString();
            strPm = cbBoxPm.getSelectedItem().toString();
            strU2D_R = cbBoxU.getSelectedItem().toString();
            strV2D_R = cbBoxV.getSelectedItem().toString();
            strTime_R = cbBoxTime.getSelectedItem().toString();
            break;
          case (Resources.ROMS + Resources.SIMU_3D):
            strXiDim_R = cbBoxXiDim.getSelectedItem().toString();
            strEtaDim_R = cbBoxEtaDim.getSelectedItem().toString();
            strZDim_R = cbBoxZDim.getSelectedItem().toString();
            strTimeDim_R = cbBoxTimeDim.getSelectedItem().toString();
            strLon_R = cbBoxLon.getSelectedItem().toString();
            strLat_R = cbBoxLat.getSelectedItem().toString();
            strBathy_R = cbBoxBathy.getSelectedItem().toString();
            strMask_R = cbBoxMask.getSelectedItem().toString();
            strU3D_R = cbBoxU.getSelectedItem().toString();
            strV3D_R = cbBoxV.getSelectedItem().toString();
            strZeta_R = cbBoxZeta.getSelectedItem().toString();
            strTp_R = cbBoxTp.getSelectedItem().toString();
            strSal_R = cbBoxSal.getSelectedItem().toString();
            strTime_R = cbBoxTime.getSelectedItem().toString();
            strPn = cbBoxPn.getSelectedItem().toString();
            strPm = cbBoxPm.getSelectedItem().toString();
            strThetaS = cbBoxThetaS.getSelectedItem().toString();
            strThetaB = cbBoxThetaB.getSelectedItem().toString();
            strHc = cbBoxHc.getSelectedItem().toString();
            break;
          case (Resources.MARS + Resources.SIMU_2D):
            strXiDim_M = cbBoxXiDim.getSelectedItem().toString();
            strEtaDim_M = cbBoxEtaDim.getSelectedItem().toString();
            strTimeDim_M = cbBoxTimeDim.getSelectedItem().toString();
            strLon_M = cbBoxLon.getSelectedItem().toString();
            strLat_M = cbBoxLat.getSelectedItem().toString();
            strBathy_M = cbBoxBathy.getSelectedItem().toString();
            strU2D_M = cbBoxU.getSelectedItem().toString();
            strV2D_M = cbBoxV.getSelectedItem().toString();
            strTime_M = cbBoxTime.getSelectedItem().toString();
            break;
          case (Resources.MARS + Resources.SIMU_3D):
            strXiDim_M = cbBoxXiDim.getSelectedItem().toString();
            strEtaDim_M = cbBoxEtaDim.getSelectedItem().toString();
            strZDim_M = cbBoxZDim.getSelectedItem().toString();
            strTimeDim_M = cbBoxTimeDim.getSelectedItem().toString();
            strLon_M = cbBoxLon.getSelectedItem().toString();
            strLat_M = cbBoxLat.getSelectedItem().toString();
            strBathy_M = cbBoxBathy.getSelectedItem().toString();
            strU3D_M = cbBoxU.getSelectedItem().toString();
            strV3D_M = cbBoxV.getSelectedItem().toString();
            strZeta_M = cbBoxZeta.getSelectedItem().toString();
            strTp_M = cbBoxTp.getSelectedItem().toString();
            strSal_M = cbBoxSal.getSelectedItem().toString();
            strTime_M = cbBoxTime.getSelectedItem().toString();
            strSigma = cbBoxSigma.getSelectedItem().toString();
            break;
        }
        updateFieldNames();
        dispose();

      }
      if (e.getSource() == btnCANCEL) {
        dispose();
      }
    }
  }

  //End of inner class JField
  //##########################################################################

}
