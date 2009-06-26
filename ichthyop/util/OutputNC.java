package ichthyop.util;

import java.io.*;
import ucar.multiarray.*;
import ucar.netcdf.*;
import ichthyop.GetConfig;
import ichthyop.Simulation;

/**
 *
 * <p>Title: Netcdf output file</p>
 *
 * <p>Description: Generate a netcdf output file.</p>
 *
 * <p>Copyright: Copyright (c) Philippe VERLEY 2007</p>
 *
 */
public class OutputNC {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Declaration of the variables
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  final public static int LONGITUDE = 0;
  final public static int LATITUDE = 1;
  final public static int DEPTH = 2;
  final public static int X_GRID = 3;
  final public static int Y_GRID = 4;
  final public static int Z_GRID = 5;
  final public static int TIME = 6;
  final public static int TEMPERATURE = 7;
  final public static int SALINITY = 8;
  final public static int LENGTH = 9;
  final public static int CURRENT_ZONE = 10;
  final public static int RECRUITED = 11;
  final public static int DEATH = 12;

  final public static int NB_FIELDS = 13;

  final public static String[] FIELD = {
      "lon",
      "lat",
      "depth",
      "xgrid",
      "ygrid",
      "zgrid",
      "time",
      "temp",
      "salt",
      "length",
      "current_zone",
      "recruited",
      "death"
  };
  final public static String[] UNIT = {
      "degree east",
      "degree north",
      "meter",
      "scalar",
      "scalar",
      "scalar",
      "second",
      "celsius",
      "psu",
      "millimeter",
      "releasing zone > 0, recruitment zone < 0, out zone = 0",
      "boolean",
      "alive = 0, out = 1, cold = 2, beached = 3"
  };
  final public static String[] LONG_NAME = {
      "particle longitude",
      "particle latitude",
      "particle depth",
      "coordinate in x",
      "coordinate in y",
      "coordinate in z",
      "time in second since origin",
      "water temperature at particle location",
      "salinity at particle location",
      "particle length",
      "zone number at particle location",
      "status of recruitment",
      "cause of death"
  };

  private static Schema schema;
  private static Dimension time, drifter, recruit_zone;
  private static NetcdfFile ncOut;

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Constructor
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  public static void createFile(int i_simu, int nb_simu, int typeSimu) {

    String strNcOut;
    int n = 10;
    int nf_simu = 1;
    int nf_isimu = 1;
    while ( (nb_simu % n) != nb_simu) {
      n *= 10;
      nf_simu++;
    }
    n = 10;
    while ( (i_simu % n) != i_simu) {
      n *= 10;
      nf_isimu++;
    }
    if (typeSimu == Resources.SERIAL_SIMU) {
      strNcOut = GetConfig.getDirectorOut() + "serial_simu_";
      for (int i = 0; i < (nf_simu - nf_isimu); i++) {
        strNcOut += String.valueOf(0);
      }
      strNcOut += String.valueOf(i_simu) + ".nc";

    }
    else {
      strNcOut = GetConfig.getDirectorOut() + "single_simu.nc";
    }

    schema = new Schema();
    schema.putAttribute(new Attribute("title", "drifter monitoring"));
    schema.putAttribute(new Attribute("model",
        GetConfig.getTypeModel() == Resources.ROMS ? "roms" : "mars"));
    schema.putAttribute(new Attribute("scheme",
        GetConfig.getScheme() == Resources.EULER ? "euler" : "rk4"));
    schema.putAttribute(new Attribute("t0",
        String.valueOf(Simulation.get_t0())));
    schema.putAttribute(new Attribute("t0_expl", "beginning of simulation"));
    schema.putAttribute(new Attribute("t0_units", "millisecond"));
    schema.putAttribute(new Attribute("transport_duration",
        String.valueOf(Simulation.getTransportDuration())));
    schema.putAttribute(new Attribute("transport_duration_units", "millisecond"));
    schema.putAttribute(new Attribute("dt", String.valueOf(Simulation.get_dt())));
    schema.putAttribute(new Attribute("dt_expl", "computational time step"));
    schema.putAttribute(new Attribute("dt_units", "millisecond"));
    if (GetConfig.getTypeRelease() == Resources.RELEASE_ZONE) {
      schema.putAttribute(new Attribute("release_depth",
          String.valueOf(Simulation.getDepthReleaseMin()) + " to "
          + String.valueOf(Simulation.getDepthReleaseMax())));
      schema.putAttribute(new Attribute("release_depth_units", "meter"));
      if (GetConfig.isPulsation()) {
        schema.putAttribute(new Attribute("release_dt",
            String.valueOf(Simulation.getReleaseDt() / Resources.ONE_SECOND)));
        schema.putAttribute(new Attribute("release_dt_expl",
            "time between two release events"));
        schema.putAttribute(new Attribute("release_dt_units", "millisecond"));
        schema.putAttribute(new Attribute("number_release_event",
            String.valueOf(Simulation.getNbReleaseEvents())));
      }
      if (GetConfig.isPatchiness()) {
        schema.putAttribute(new Attribute("number_patches",
            String.valueOf(GetConfig.getNbPatches())));
        schema.putAttribute(new Attribute("patch_radius",
            String.valueOf(Simulation.getRadiusPatchi())));
        schema.putAttribute(new Attribute("patch_radius_units", "meter"));
        schema.putAttribute(new Attribute("patch_thickness",
            String.valueOf(Simulation.getThickPatchi())));
        schema.putAttribute(new Attribute("patch_thickness_units", "meter"));
      }
    }
    if (GetConfig.isLethalTp()) {
      schema.putAttribute(new Attribute("tp_egg",
          String.valueOf(Simulation.getLethalTpEgg())));
      schema.putAttribute(new Attribute("tp_egg_expl",
          "lower lethal temperature for egg"));
      schema.putAttribute(new Attribute("tp_egg_units", "Celsius"));
      if (GetConfig.isGrowth()) {
        schema.putAttribute(new Attribute("tp_larva",
            String.valueOf(Simulation.getLethalTpLarvae())));
        schema.putAttribute(new Attribute("tp_larva_expl",
            "lower lethal temperature for larva"));
        schema.putAttribute(new Attribute("tp_larva_units", "Celsius"));
      }
    }
    if (GetConfig.isBuoyancy()) {
      schema.putAttribute(new Attribute("egg_dentity",
          String.valueOf(Simulation.getEggDensity())));
      schema.putAttribute(new Attribute("egg_dentity_units", "g.cm-3"));
    }
    if (GetConfig.getTypeRecruitment() == Resources.RECRUIT_AGE) {
      schema.putAttribute(new Attribute("age_recruit",
          String.valueOf(Simulation.getAgeMinAtRecruitment())));
      schema.putAttribute(new Attribute("age_recruit_units", "day"));
    }
    if (GetConfig.getTypeRecruitment() == Resources.RECRUIT_LENGTH) {
      schema.putAttribute(new Attribute("length_recruit",
          String.valueOf(Simulation.getLengthMinAtRecruitment())));
      schema.putAttribute(new Attribute("length_recruit_units", "millimeter"));
    }
    if (GetConfig.getTypeRecruitment() != Resources.RECRUIT_NONE) {
      schema.putAttribute(new Attribute("duration_min",
          String.valueOf(Simulation.getDurationInRecruitArea())));
      schema.putAttribute(new Attribute("duration_min_expl",
          "duration min in recruitment zone before being recruited"));
      schema.putAttribute(new Attribute("duration_min_units", "millisecond"));
    }
    if (GetConfig.isSerial()) {
      schema.putAttribute(new Attribute("replica",
          String.valueOf(Simulation.getReplica())));
    }

    addDoubleVar2Schema(TIME);
    addDoubleVar2Schema(LONGITUDE);
    addDoubleVar2Schema(LATITUDE);
    addDoubleVar2Schema(DEPTH);
    /*addNcVar2Schema(X_GRID);
         addNcVar2Schema(Y_GRID);
         addNcVar2Schema(Z_GRID);*/
    addIntegerVar2Schema(DEATH);
    if (GetConfig.is3D()) {
      addDoubleVar2Schema(TEMPERATURE);
      addDoubleVar2Schema(SALINITY);
    }
    if (GetConfig.isGrowth()) {
      addDoubleVar2Schema(LENGTH);
    }
    if (GetConfig.getTypeRelease() == Resources.RELEASE_ZONE) {
      addIntegerVar2Schema(CURRENT_ZONE);
    }
    if (GetConfig.getTypeRecruitment() != Resources.RECRUIT_NONE) {
      addIntegerVar2Schema(RECRUITED);
    }

    try {
      ncOut = new NetcdfFile(strNcOut, true, true, schema);
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    System.out.println("Creating output file : " + ncOut.getFile().toString());
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the methods
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //------------------------------------------------------------------------------
  private static void addDoubleVar2Schema(int index_field) {

    String field, unit, long_name;

    drifter = new Dimension("drifter", GetConfig.getNbParticles());
    time = new UnlimitedDimension("time");

    field = FIELD[index_field];
    unit = UNIT[index_field];
    long_name = LONG_NAME[index_field];

    if (index_field == TIME) {
      String calendar = (GetConfig.getTypeCalendar() == Resources.CLIMATO) ?
        "360_days" : "gregorian";
    schema.put(new ProtoVariable(field, double.class,
        new ucar.netcdf.Dimension[] {
      time}, new Attribute[] {
      new Attribute("long_name", long_name),
          new Attribute("units", unit),
          new Attribute("calendar", calendar)}));
    return;
    }

    schema.put(new ProtoVariable(field, double.class,
        new ucar.netcdf.Dimension[] {
      time, drifter
    }, new Attribute[] {
      new Attribute("long_name", long_name), new Attribute("units", unit)}));
  }

  //------------------------------------------------------------------------------
  private static void addIntegerVar2Schema(int index_field) {

    String field, unit, long_name;

    drifter = new Dimension("drifter", GetConfig.getNbParticles());
    time = new UnlimitedDimension("time");

    field = FIELD[index_field];
    unit = UNIT[index_field];
    long_name = LONG_NAME[index_field];

    if (index_field == RECRUITED) {
      recruit_zone = new Dimension("recruit_zone", GetConfig.getRecruitmentZones().size());
      schema.put(new ProtoVariable(field, int.class,
          new ucar.netcdf.Dimension[] {
        time, drifter, recruit_zone},
          new Attribute[] {
        new Attribute("long_name", long_name),
            new Attribute("units", unit)}));
      return;
    }

    schema.put(new ProtoVariable(field, int.class,
        new ucar.netcdf.Dimension[] {
      time, drifter
    }, new Attribute[] {
      new Attribute("long_name", long_name), new Attribute("units", unit)}));
  }

  // ----------------------------------------------------------------------------
  public static void add2ncOut(int index_field, MultiArray coord, int i_time,
      int numIndiv) {

    //System.out.println(index_field + " " + FIELD[index_field]);
    Variable vOut = ncOut.get(FIELD[index_field]);
    //System.out.println(vOut.toString());
    int[] originOut = new int[vOut.getRank()];
    originOut[0] = i_time;
    originOut[1] = numIndiv;

    try {
      //System.out.println(numIndiv + " - t = "+time+" "+nomChampOut+" = "+coord.getDouble(new int[] {0,0}));
      vOut.copyin(originOut, coord);
    }
    catch (java.io.IOException e) {
      e.printStackTrace();
    }
  }

  // ----------------------------------------------------------------------------
  public static void add2ncOut(int index_field, MultiArray coord, int i_time) {

    Variable vOut = ncOut.get(FIELD[index_field]);
    int[] originOut = new int[vOut.getRank()];
    originOut[0] = i_time;

    try {
      //System.out.println(numIndiv + " - t = "+time+" "+nomChampOut+" = "+coord.getDouble(new int[] {0,0}));
      vOut.copyin(originOut, coord);
    }
    catch (java.io.IOException e) {
      e.printStackTrace();
    }
  }

  //----------------------------------------------------------------------------
  public static void close() {
    try {
      ncOut.close();
    }
    catch (java.io.IOException e) {
      e.printStackTrace();
    }

  }

}
