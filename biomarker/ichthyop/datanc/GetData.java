package ichthyop.datanc;

import java.io.*;
import java.util.*;

import ichthyop.*;
import ichthyop.util.*;
import ucar.multiarray.*;
import ucar.netcdf.*;

public abstract class GetData {

  static int nx, ny, nz;
  static int nbTimeRecords, nbTimeRecordsPrevFile;
  static NetcdfFile ncIn;
  static double[][] xlonRhoT, xlatRhoT, xmaskRhoT, xhT, xzetaTp0, xzetaTp1;
  static double[][][] xuTp0, xuTp1, xvTp0, xvTp1, xwTp0, xwTp1;
  static MultiArray xsaltTp1, xsaltTp0,
      xtempTp0, xtempTp1,
      biomarker1_tp0, biomarker1_tp1,
      biomarker2_tp0, biomarker2_tp1;
  static boolean firstDay;
  private static double latMin, lonMin, latMax, lonMax, depthMax;
  static long dt_HyMo;
  static double[][][] z_r_cst;
  static double[][][][] z_w;
  static double[][][] z_w_cst;

  private ArrayList listInputFiles;
  private int indexFile;
  static String DIRECTORY_IN;
  static long t0;
  static long timeTp1;
  static int currentNcInRank;

  static String strXiDim, strEtaDim, strZDim, strTimeDim;
  static String strU, strV, strTp, strSal, strTime, strZeta;
  static String strLon, strLat, strMask, strBathy;
  static String strBiomarker1, strBiomarker2;

//------------------------------------------------------------------------------
//  Definition of abstract methods
//---------------------------------

  abstract void setFieldsName();

  abstract void getCstVarNC();

  abstract void getCstSigLevels();

  abstract public double[] getMoveEuler(double[] pGrid,
      long time, long dt_ibm);

  abstract public double[] getMoveEulerCost(double[] pGrid,
      long time, long dt_ibm);

  abstract public double[] getMoveRK4(double[] pGrid, long time, long dt_ibm);

  abstract public double[] getMoveRK4Cost(double[] pGrid, long time,
      long dt_ibm);

  abstract double[][][] computeW();

  abstract public void setAllFieldsAtTime(long time);

  abstract void setAllFieldsTp1AtTime(int i_time);

  abstract public double adimensionalize(double number, double xRho,
      double yRho);

//------------------------------------------------------------------------------
//  Definition of non-abstract methods
//-------------------------------------

  public static int get_nx() {
    return nx;
  }

  public static int get_ny() {
    return ny;
  }

  public static int get_nz() {
    return nz;
  }

  public static long get_dtR() {
    return dt_HyMo;
  }

  public static double getLatMin() {
    return latMin;
  }

  public static double getLatMax() {
    return latMax;
  }

  public static double getLonMin() {
    return lonMin;
  }

  public static double getLonMax() {
    return lonMax;
  }

  public static float getDepthMax() {
    return (float) depthMax;
  }

  //----------------------------------------------------------------------------
  public void setUp() {
    try {
      DIRECTORY_IN = GetConfig.getDirectorIn();
      t0 = GetConfig.get_t0(0);
      setFieldsName();
      openLocation(GetConfig.getDirectorIn());
      init();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  //--------------------------------------------------------------------------
  void init() {

    //openFile(t0);
    getDimNC();
    getCstVarNC();
    getDimGeogArea();
    getCstSigLevels();
    if (GetConfig.getDimSimu() == Resources.SIMU_3D) {
      getSigLevelsTp1(0);
    }
  }

  //--------------------------------------------------------------------------
  private void getDimNC() {

    nx = ncIn.getDimensions().get(strXiDim).getLength();
    ny = ncIn.getDimensions().get(strEtaDim).getLength();
    nz = (GetConfig.getDimSimu() == Resources.SIMU_3D)
        ? ncIn.getDimensions().get(strZDim).getLength()
        : 1;

    //System.out.println("nx " + nx + " ny " + ny + " nz " + nz);
  }

  //--------------------------------------------------------------------------
  static void getSigLevelsTp1(int iTp) {

    //-----------------------------------------------------
    // Daily recalculation of z_w and z_r with zeta

    double[][][] z_w_tmp = new double[nz + 1][ny][nx];
    double[][][] z_w_cst_tmp = z_w_cst;

    //System.out.print("Calculation of the s-levels\n");
    for (int i = nx; i-- > 0; ) {
      for (int j = ny; j-- > 0; ) {
        for (int k = 0; k < nz + 1; k++) {
          z_w_tmp[k][j][i] = z_w_cst_tmp[k][j][i] +
              xzetaTp1[j][i] *
              (1.f + z_w_cst_tmp[k][j][i] / xhT[j][i]);
        }
      }
    }
    z_w[iTp] = z_w_tmp;

    //System.out.println("getSigLevels : fin");

  }

  //--------------------------------------------------------------------------
  private void getDimGeogArea() {

    //--------------------------------------
    // Calculate the Physical Space extrema

    lonMin = Double.MAX_VALUE;
    lonMax = -lonMin;
    latMin = Double.MAX_VALUE;
    latMax = -latMin;
    depthMax = 0.d;
    int i = nx;
    int j = 0;

    while (i-- > 0) {
      j = ny;
      while (j-- > 0) {
        if ( (xlonRhoT[j][i]) >= (lonMax)) {
          lonMax = xlonRhoT[j][i];
        }
        if ( (xlonRhoT[j][i]) <= (lonMin)) {
          lonMin = xlonRhoT[j][i];
        }
        if ( (xlatRhoT[j][i]) >=
            (latMax)) {
          latMax = xlatRhoT[j][i];
        }
        if ( (xlatRhoT[j][i]) <=
            (latMin)) {
          latMin = xlatRhoT[j][i];
        }

        if (Math.abs(xhT[j][i]) >= depthMax) {
          depthMax = xhT[j][i];
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

  //--------------------------------------------------------------------------
  public static double getLat(int i, int j) {
    return xlatRhoT[j][i];
  }

  //--------------------------------------------------------------------------
  public static double getLon(int i, int j) {
    return xlonRhoT[j][i];
  }

  //--------------------------------------------------------------------------
  public static double getBathy(int i, int j) {

    if (isInWater(i, j)) {
      return xhT[j][i];
    }
    return ( -10000.f);
  }

  // -------------------------------------------------------------------------
  private static double getDepth(double xRho, double yRho, int k) {

    final int i = (int) xRho;
    final int j = (int) yRho;
    double hh = 0.d;
    final double dx = (xRho - i);
    final double dy = (yRho - j);
    double co = 0.d;
    for (int ii = 0; ii < 2; ii++) {
      for (int jj = 0; jj < 2; jj++) {
        if (isInWater(i + ii, j + jj)) {
          co = Math.abs( (1 - ii - dx) * (1 - jj - dy));
          double z_r = 0.d;
          z_r = z_r_cst[k][j + jj][i + ii] +
              xzetaTp0[j + jj][i + ii]
              *
              (1.d + z_r_cst[k][j + jj][i +
              ii] / xhT[j + jj][i + ii]);
          hh += co * z_r;
        }
      }
    }
    return (hh);
  }

  //--------------------------------------------------------------------------
  public static boolean isInWater(int i, int j) {
    return (xmaskRhoT[j][i] > 0.1f);
  }

  //--------------------------------------------------------------------------
  public static boolean isInWater(RhoPoint ptRho) {
    return (xmaskRhoT[ (int) Math.round(ptRho.getY())][ (int) Math.round(
        ptRho.getX())] >
        0.1f);
  }

  //--------------------------------------------------------------------------
  private static boolean isSurroundedByWater(int i, int j) {
    return (isInWater(i, j)
        && isInWater(i + 1, j + 1)
        && isInWater(i - 1, j + 1)
        && isInWater(i + 1, j - 1)
        && isInWater(i - 1, j - 1));
  }

  //--------------------------------------------------------------------------
  public static boolean isSurroundedByWater(RhoPoint ptRho) {
    return isSurroundedByWater( (int) Math.round(ptRho.getX()),
        (int) Math.round(ptRho.getY()));
  }

  //----------------------------------------------------------------------------
  public static boolean isCloseToCost(RhoPoint ptRho) {

    double x = ptRho.getX();
    double y = ptRho.getY();
    int i, j, ii, jj;
    i = (int) (Math.round(x));
    j = (int) (Math.round(y));
    ii = (i - (int) x) == 0 ? 1 : -1;
    jj = (j - (int) y) == 0 ? 1 : -1;
    return! (isInWater(i + ii, j) && isInWater(i + ii, j + jj) &&
        isInWater(i, j + jj));
  }

  //--------------------------------------------------------------------------
  public static double zGeo2Grid(double xRho, double yRho, double depth) {

    //-----------------------------------------------
    // Return z[grid] corresponding to depth[meters]
    double z = 0.d;
    int lk = nz - 1;
    while ( (lk > 0) && (getDepth(xRho, yRho, lk) > depth)) {
      lk--;
    }
    if (lk == (nz - 1)) {
      z = (double) lk;
    }
    else {
      double pr = getDepth(xRho, yRho, lk);
      z = Math.max(0.d,
          (double) lk +
          (depth - pr) / (getDepth(xRho, yRho, lk + 1) - pr));
    }
    return (z);
  }

  //--------------------------------------------------------------------------
  public static double[] grid2Geo(double xRho, double yRho, double zRho) {

    //--------------------------------------------------------------------
    // Computational space (x, y , z) => Physical space (lat, lon, depth)

    final double kz = Math.max(0.d, Math.min(zRho, (double) nz - 1.00001f));
    final int i = (int) Math.floor(xRho);
    final int j = (int) Math.floor(yRho);
    final int k = (int) Math.floor(kz);
    double latitude = 0.d;
    double longitude = 0.d;
    double depth = 0.d;
    final double dx = xRho - (double) i;
    final double dy = yRho - (double) j;
    final double dz = kz - (double) k;
    double co = 0.d;
    for (int ii = 0; ii < 2; ii++) {
      for (int jj = 0; jj < 2; jj++) {
        for (int kk = 0; kk < 2; kk++) {
          co = Math.abs( (1.d - (double) ii - dx) * (1.d - (double) jj - dy) *
              (1.d - (double) kk - dz));
          latitude += co * xlatRhoT[j + jj][i + ii];
          longitude += co * xlonRhoT[j + jj][i + ii];
          if (isInWater(i + ii, j + jj)) {
            double z_r = z_r_cst[k + kk][j + jj][i + ii] +
                xzetaTp0[j + jj][i + ii]
                *
                (1.d + z_r_cst[k + kk][j + jj][i +
                ii] / xhT[j + jj][i + ii]);

            depth += co * z_r;
          }
        }
      }
    }
    return (new double[] {latitude, longitude, depth});
  }

  //--------------------------------------------------------------------------
  public static double[] grid2Geo(double xRho, double yRho) {

    //--------------------------------------------------------------------
    // Computational space (x, y , z) => Physical space (lat, lon, depth)

    final double ix = Math.max(0.00001f,
        Math.min(xRho, (double) nx - 1.00001f));
    final double jy = Math.max(0.00001f,
        Math.min(yRho, (double) ny - 1.00001f));

    final int i = (int) Math.floor(ix);
    final int j = (int) Math.floor(jy);
    double latitude = 0.d;
    double longitude = 0.d;
    final double dx = ix - (double) i;
    final double dy = jy - (double) j;
    double co = 0.d;
    for (int ii = 0; ii < 2; ii++) {
      for (int jj = 0; jj < 2; jj++) {
        co = Math.abs( (1 - ii - dx) * (1 - jj - dy));
        latitude += co * xlatRhoT[j + jj][i + ii];
        longitude += co * xlonRhoT[j + jj][i + ii];
      }
    }
    return (new double[] {latitude, longitude});
  }

  //--------------------------------------------------------------------------
  public static double[] geo2Grid(double lon, double lat) {

    //--------------------------------------------------------------------
    // Physical space (lat, lon) => Computational space (x, y)

    boolean found;
    int imin, imax, jmin, jmax, i0, j0;
    double dx1, dy1, dx2, dy2, c1, c2, deltax, deltay, xgrid, ygrid;

    xgrid = 0.;
    ygrid = 0.;
    found = isInsidePolygone(0, nx - 1, 0, ny - 1, lon, lat);

    //-------------------------------------------
    // Research surrounding grid-points
    if (found) {
      imin = 0;
      imax = nx - 1;
      jmin = 0;
      jmax = ny - 1;
      while ( ( (imax - imin) > 1) | ( (jmax - jmin) > 1)) {
        if ( (imax - imin) > 1) {
          i0 = (imin + imax) / 2;
          found = isInsidePolygone(imin, i0, jmin, jmax, lon, lat);
          if (found) {
            imax = i0;
          }
          else {
            imin = i0;
          }
        }
        if ( (jmax - jmin) > 1) {
          j0 = (jmax + jmin) / 2;
          found = isInsidePolygone(imin, imax, jmin, j0, lon, lat);
          if (found) {
            jmax = j0;
          }
          else {
            jmin = j0;
          }
        }
      }

      //--------------------------------------------
      // Trilinear interpolation
      dy1 = getLat(imin, jmin + 1) - getLat(imin, jmin);
      dx1 = getLon(imin, jmin + 1) - getLon(imin, jmin);
      dy2 = getLat(imin + 1, jmin) - getLat(imin, jmin);
      dx2 = getLon(imin + 1, jmin) - getLon(imin, jmin);

      c1 = lon * dy1 - lat * dx1;
      c2 = getLon(imin, jmin) * dy2 - getLat(imin, jmin) * dx2;
      deltax = (c1 * dx2 - c2 * dx1) / (dx2 * dy1 - dy2 * dx1);
      deltax = (deltax - getLon(imin, jmin)) / dx2;
      xgrid = (double) imin + Math.min(Math.max(0.d, deltax), 1.d);

      c1 = getLon(imin, jmin) * dy1 - getLat(imin, jmin) * dx1;
      c2 = lon * dy2 - lat * dx2;
      deltay = (c1 * dy2 - c2 * dy1) / (dx2 * dy1 - dy2 * dx1);
      deltay = (deltay - getLat(imin, jmin)) / dy1;
      ygrid = (double) jmin + Math.min(Math.max(0.d, deltay), 1.d);
    }
    return (new double[] {xgrid, ygrid});
  }

  //--------------------------------------------------------------------------
  public static boolean isInsidePolygone(int imin, int imax, int jmin,
      int jmax,
      double lon, double lat) {

    //--------------------------------------------------------------
    // Return true if (lon, lat) is insidide the polygon defined by
    // (imin, jmin) & (imin, jmax) & (imax, jmax) & (imax, jmin)

    //-----------------------------------------
    // Build the polygone
    int nb, shft;
    double[] xb, yb;
    boolean isInPolygone = true;

    nb = 2 * (jmax - jmin + imax - imin);
    xb = new double[nb + 1];
    yb = new double[nb + 1];
    shft = 0 - imin;
    for (int i = imin; i <= (imax - 1); i++) {
      xb[i + shft] = getLon(i, jmin);
      yb[i + shft] = getLat(i, jmin);
    }
    shft = 0 - jmin + imax - imin;
    for (int j = jmin; j <= (jmax - 1); j++) {
      xb[j + shft] = getLon(imax, j);
      yb[j + shft] = getLat(imax, j);
    }
    shft = jmax - jmin + 2 * imax - imin;
    for (int i = imax; i >= (imin + 1); i--) {
      xb[shft - i] = getLon(i, jmax);
      yb[shft - i] = getLat(i, jmax);
    }
    shft = 2 * jmax - jmin + 2 * (imax - imin);
    for (int j = jmax; j >= (jmin + 1); j--) {
      xb[shft - j] = getLon(imin, j);
      yb[shft - j] = getLat(imin, j);
    }
    xb[nb] = xb[0];
    yb[nb] = yb[0];

    //---------------------------------------------
    //Check if {lon, lat} is inside polygone
    int inc, crossings;
    double dx1, dx2, dxy;
    crossings = 0;

    for (int k = 0; k < nb; k++) {
      if (xb[k] != xb[k + 1]) {
        dx1 = lon - xb[k];
        dx2 = xb[k + 1] - lon;
        dxy = dx2 * (lat - yb[k]) - dx1 * (yb[k + 1] - lat);
        inc = 0;
        if ( (xb[k] == lon) & (yb[k] == lat)) {
          crossings = 1;
        }
        else if ( ( (dx1 == 0.) & (lat >= yb[k])) |
            ( (dx2 == 0.) & (lat >= yb[k + 1]))) {
          inc = 1;
        }
        else if ( (dx1 * dx2 > 0.) & ( (xb[k + 1] - xb[k]) * dxy >= 0.)) {
          inc = 2;
        }
        if (xb[k + 1] > xb[k]) {
          crossings += inc;
        }
        else {
          crossings -= inc;
        }
      }
    }
    if (crossings == 0) {
      isInPolygone = false;
    }
    return (isInPolygone);
  }

  //--------------------------------------------------------------------------
  public static double getTp(RhoPoint ptRho, long time) {

    double co, CO, x, xf, tp;
    int[] ind;

    xf = (double) (dt_HyMo - (timeTp1 - time)) / (double) dt_HyMo;

    //-----------------------------------------------------------
    // Interpolate the temperature fields
    // in the computational grid.
    int i = (int) ptRho.getX();
    int j = (int) ptRho.getY();
    final double kz = Math.max(0.d,
        Math.min(ptRho.getZ(), (double) nz - 1.00001f));
    int k = (int) kz;
    double dx = ptRho.getX() - (double) i;
    double dy = ptRho.getY() - (double) j;
    double dz = kz - (double) k;
    tp = 0.d;
    CO = 0.d;
    for (int kk = 0; kk < 2; kk++) {
      for (int jj = 0; jj < 2; jj++) {
        for (int ii = 0; ii < 2; ii++) {
          if (isInWater(i + ii, j + jj)) {
            ind = new int[] {
                0, k + kk, j + jj, i + ii};
            co = Math.abs( (1.d - (double) ii - dx) *
                (1.d - (double) jj - dy) *
                (1.d - (double) kk - dz));
            CO += co;
            x = 0.d;
            try {
              x = (1.d - xf) * xtempTp0.getDouble(ind) +
                  xf * xtempTp1.getDouble(ind);
              tp += x * co;
            }
            catch (Exception ex) {
              return Double.NaN;
            }
          }
        }
      }
    }
    if (CO != 0) {
      tp /= CO;
    }

    return tp;

  }

  //--------------------------------------------------------------------------
  public static double getBiomarker1(RhoPoint ptRho, long time) {

    double co, CO, x, xf, biomarker1;
    int[] ind;

    xf = (double) (dt_HyMo - (timeTp1 - time)) / (double) dt_HyMo;

    //-----------------------------------------------------------
    // Interpolate the temperature fields
    // in the computational grid.
    int i = (int) ptRho.getX();
    int j = (int) ptRho.getY();
    final double kz = Math.max(0.d,
        Math.min(ptRho.getZ(), (double) nz - 1.00001f));
    int k = (int) kz;
    double dx = ptRho.getX() - (double) i;
    double dy = ptRho.getY() - (double) j;
    double dz = kz - (double) k;
    biomarker1 = 0.d;
    CO = 0.d;
    for (int kk = 0; kk < 2; kk++) {
      for (int jj = 0; jj < 2; jj++) {
        for (int ii = 0; ii < 2; ii++) {
          if (isInWater(i + ii, j + jj)) {
            ind = new int[] {
                0, k + kk, j + jj, i + ii};
            co = Math.abs( (1.d - (double) ii - dx) *
                (1.d - (double) jj - dy) *
                (1.d - (double) kk - dz));
            CO += co;
            x = 0.d;
            try {
              x = (1.d - xf) * biomarker1_tp0.getDouble(ind) +
                  xf * biomarker1_tp1.getDouble(ind);
              biomarker1 += x * co;
            }
            catch (Exception ex) {
              return Double.NaN;
            }
          }
        }
      }
    }
    if (CO != 0) {
      biomarker1 /= CO;
    }

    return biomarker1;

  }

  //--------------------------------------------------------------------------
  public static double getBiomarker2(RhoPoint ptRho, long time) {

    double co, CO, x, xf, biomarker2;
    int[] ind;

    xf = (double) (dt_HyMo - (timeTp1 - time)) / (double) dt_HyMo;

    //-----------------------------------------------------------
    // Interpolate the temperature fields
    // in the computational grid.
    int i = (int) ptRho.getX();
    int j = (int) ptRho.getY();
    final double kz = Math.max(0.d,
        Math.min(ptRho.getZ(), (double) nz - 1.00001f));
    int k = (int) kz;
    double dx = ptRho.getX() - (double) i;
    double dy = ptRho.getY() - (double) j;
    double dz = kz - (double) k;
    biomarker2 = 0.d;
    CO = 0.d;
    for (int kk = 0; kk < 2; kk++) {
      for (int jj = 0; jj < 2; jj++) {
        for (int ii = 0; ii < 2; ii++) {
          if (isInWater(i + ii, j + jj)) {
            ind = new int[] {
                0, k + kk, j + jj, i + ii};
            co = Math.abs( (1.d - (double) ii - dx) *
                (1.d - (double) jj - dy) *
                (1.d - (double) kk - dz));
            CO += co;
            x = 0.d;
            try {
              x = (1.d - xf) * biomarker2_tp0.getDouble(ind) +
                  xf * biomarker2_tp1.getDouble(ind);
              biomarker2 += x * co;
            }
            catch (Exception ex) {
              return Double.NaN;
            }
          }
        }
      }
    }
    if (CO != 0) {
      biomarker2 /= CO;
    }

    return biomarker2;

  }

  //--------------------------------------------------------------------------
  public static double[] getSaltnTp(RhoPoint ptRho, long time) {

    double co, CO, x, xf, sal, tp;
    int[] ind;

    xf = (double) (dt_HyMo - (timeTp1 - time)) / (double) dt_HyMo;

    //-----------------------------------------------------------
    // Interpolate the temperature and salinity fields
    // in the computational grid.
    int i = (int) ptRho.getX();
    int j = (int) ptRho.getY();
    final double kz = Math.max(0.d,
        Math.min(ptRho.getZ(), (double) nz - 1.00001f));
    int k = (int) kz;
    double dx = ptRho.getX() - (double) i;
    double dy = ptRho.getY() - (double) j;
    double dz = kz - (double) k;
    sal = 0.d;
    tp = 0.d;
    CO = 0.d;
    for (int kk = 0; kk < 2; kk++) {
      for (int jj = 0; jj < 2; jj++) {
        for (int ii = 0; ii < 2; ii++) {
          if (isInWater(i + ii, j + jj)) {
            ind = new int[] {
                0, k + kk, j + jj, i + ii};
            co = Math.abs( (1.d - (double) ii - dx) *
                (1.d - (double) jj - dy) *
                (1.d - (double) kk - dz));
            CO += co;
            x = 0.d;
            try {
              x = (1.d - xf) * xsaltTp0.getDouble(ind) +
                  xf * xsaltTp1.getDouble(ind);
              sal += x * co;
              x = (1.d - xf) * xtempTp0.getDouble(ind) +
                  xf * xtempTp1.getDouble(ind);
              tp += x * co;
            }
            catch (Exception ex) {
              return new double[] {Double.NaN, Double.NaN};
            }
          }
        }
      }
    }
    if (CO != 0) {
      sal /= CO;
      tp /= CO;
    }

    return new double[] {sal, tp};
  }

  //--------------------------------------------------------------------------
  static void openFile(long time) {

    File inputPath = new File(DIRECTORY_IN);
    String[] listFileTmp = inputPath.list();

    ArrayList listF = new ArrayList();
    try {
      for (int i = 0; i < listFileTmp.length; i++) {
        if (listFileTmp[i].endsWith(".nc") | listFileTmp[i].endsWith(".nc.1")) {
          listF.add(listFileTmp[i]);
        }
      }
    }
    catch (java.lang.ArrayIndexOutOfBoundsException ex) {}
    catch (NullPointerException ex) {
      System.out.println(
          "!! Error --> NullPointerException - Unknown directory "
          + inputPath.toString());
      return;
    }

    Iterator iter = listF.iterator();
    while (iter.hasNext()) {
      String fileName = (String) iter.next();
      try {
        if (ncIn != null) {
          ncIn.close();
        }
        ncIn = new NetcdfFile(DIRECTORY_IN + fileName, true);
        //System.out.println(ncIn.toString());
        MultiArray xTimeT = ncIn.get(strTime).copyout(new int[] {0},
            new int[] {2});
        nbTimeRecords = ncIn.getDimensions().get(strTimeDim).getLength();
        dt_HyMo = (long) ( (xTimeT.getDouble(new int[] {1}) -
            xTimeT.getDouble(new int[] {0})) * 1e3);

        if (time >= (long) (xTimeT.getDouble(new int[] {0}) * 1e3) &&
            time <=
            (long) (xTimeT.getDouble(new int[] {0}) * 1e3 +
            nbTimeRecords * dt_HyMo)) {
          System.out.print("Open File : " + fileName + "\n");
          long time_r = (long) (xTimeT.getDouble(new int[] {0}) * 1e3);
          int rank = 0;
          while (time >= time_r) {
            rank++;
            time_r += dt_HyMo;
          }
          currentNcInRank = --rank;
          //System.out.println("rank " + currentNcInRank);
          MainFrame.getStatusBar().setMessage(Resources.MSG_LOADING + fileName);
          return;
        }
      }
      catch (java.io.IOException e) {
        System.out.println(
            "!! Error --> IOException - Problem reading time field in file "
            + fileName);
      }
    }
    System.out.println("!! Error --> Unable to find t0 in files of folder "
        + inputPath.toString());
  }

  //--------------------------------------------------------------------------
  private ArrayList getInputList(String path) throws IOException {

    ArrayList list = null;

    File inputPath = new File(path);
    String fileMask = "*.nc*";
    File[] listFile = inputPath.listFiles(new MetaFilenameFilter(fileMask));
    if (listFile.length == 0) {
      throw new IOException(path + " contains no file matching mask "
          + fileMask);
    }
    list = new ArrayList(listFile.length);
    for (File file : listFile) {
      list.add(file.toString());
    }
    if (list.size() > 1) {
      Collections.sort(list, new NCComparator(strTime));
    }
    return list;
  }

  //--------------------------------------------------------------------------
  void openLocation(String path) throws IOException {

    if (isDirectory(path)) {
      listInputFiles = getInputList(path);
      open( (String) listInputFiles.get(0));
    }
  }

  //----------------------------------------------------------------------------
  private boolean isDirectory(String location) throws IOException {

    File f = new File(location);
    if (!f.isDirectory()) {
      throw new IOException(location + " is not a valid directory.");
    }
    return f.isDirectory();
  }

  //--------------------------------------------------------------------------
  void open(String filename) throws IOException {

    try {
      if (ncIn == null || !ncIn.getFile().toString().matches(filename)) {
        System.out.print("Open dataset " + filename + "\n");
        MainFrame.getStatusBar().setMessage(Resources.MSG_LOADING + filename);
        ncIn = new NetcdfFile(filename, true);
        ncIn.getDimensions().get(strTimeDim).getLength();
        nbTimeRecords = ncIn.getDimensions().get(strTimeDim).getLength();
      }
      else {
        System.out.println("Dataset " + filename + " [OK]");
      }
    }
    catch (IOException e) {
      throw new IOException("Problem opening dataset "
          + filename + " - " + e.getMessage());
    }
    catch (NullPointerException e) {
      throw new IOException("Problem reading " + strTimeDim
          + " dimension at location " + filename + " : " + e.getMessage());
    }
  }

  //--------------------------------------------------------------------------
  String getNextFile(long time) throws IOException {

    int index = indexFile;
    boolean noNext
        = (listInputFiles.size() == 1)
        || (index < 0)
        || (index >= listInputFiles.size() - 1);
    if (noNext) {
      throw new IOException("Unable to find any file following "
          + listInputFiles.get(indexFile));
    }
    if (isTimeBetweenFile(time, index)) {
      indexFile += 1;
      return (String) listInputFiles.get(indexFile);
    }
    throw new IOException("Unable to find any file following "
        + listInputFiles.get(indexFile));
  }

  //--------------------------------------------------------------------------
  String getFile(long time) throws IOException {

    int indexLast = listInputFiles.size() - 1;

    for (int i = 0; i < indexLast; i++) {
      if (isTimeBetweenFile(time, i)) {
        indexFile = i;
        return (String) listInputFiles.get(i);
      }
    }

    if (isTimeIntoFile(time, indexLast)) {
      indexFile = indexLast;
      return (String) listInputFiles.get(indexLast);
    }

    throw new IOException("Time value " + (long) time
        + " not contained among NetCDF files *.nc* of folder "
        + GetConfig.getDirectorIn());
  }

  //--------------------------------------------------------------------------
  private boolean isTimeIntoFile(long time, int index) throws IOException {

    String filename = "";
    NetcdfFile nc;
    MultiArray timeArr;
    long time_r0, time_rf;

    try {
      filename = (String) listInputFiles.get(index);
      nc = new NetcdfFile(filename, true);
      timeArr = nc.get(strTime);
      time_r0 = timeArr.getLong(new int[] {0});
      time_rf = timeArr.getLong(new int[] {timeArr.getLengths()[0] - 1});
      nc.close();

      return (time >= time_r0 && time < time_rf);
    }
    catch (IOException e) {
      throw new IOException("Problem reading file " + filename + " : "
          + e.getCause());
    }
    catch (NullPointerException e) {
      throw new IOException("Unable to read " + strTime + " variable in file "
          + filename + " : " + e.getCause());
    }
  }

  //--------------------------------------------------------------------------
  private boolean isTimeBetweenFile(long time, int index) throws IOException {

    NetcdfFile nc;
    String filename = "";
    MultiArray timeArr;
    long[] time_nc = new long[2];

    try {
      for (int i = 0; i < 2; i++) {
        filename = (String) listInputFiles.get(index + i);
        nc = new NetcdfFile(filename, true);
        timeArr = nc.get(strTime);
        time_nc[i] = timeArr.getLong(new int[] {0});
        nc.close();
      }
      /*System.out.println("isBetween " + index + " & " + (index + 1));
      System.out.println(time_nc[0]);
      System.out.println(time);
      System.out.println(time_nc[1]);*/
      if (time >= time_nc[0] && time < time_nc[1]) {
        return true;
      }
    }
    catch (IOException e) {
      throw new IOException("Problem reading file " + filename + " : "
          + e.getCause());
    }
    catch (NullPointerException e) {
      throw new IOException("Unable to read " + strTime + " variable in file "
          + filename
          + " : " + e.getCause());
    }
    return false;
  }

//----------------------------------------------------------------------------
  int findCurrentRank(long time) throws IOException {

    int rank = 0;
    long time_rank;
    MultiArray timeArr;
    try {
      timeArr = ncIn.get(strTime);
      time_rank = timeArr.getLong(new int[] {rank});
      while (time >= time_rank) {
        //System.out.println("find rank " + rank + " " + time + " " + time_rank);
        rank++;
        time_rank = timeArr.getLong(new int[] {rank});
      }
    }
    catch (EOFException e) {
      //e.printStackTrace();
      rank = nbTimeRecords;
    }
    catch (IOException e) {
      throw new IOException("Problem reading file "
          + ncIn.getFile().toString() + " : " + e.getCause());
    }
    catch (NullPointerException e) {
      throw new IOException("Unable to read " + strTime + " variable in file "
          + ncIn.getFile().toString() + " : " + e.getCause());
    }
    catch (ArrayIndexOutOfBoundsException e) {
      rank = nbTimeRecords;
    }
    rank = rank - 1;

    return rank;
  }

  //----------------------------------------------------------------------------
  boolean isOnEdge(double x, double y) {
    return ( (x > (nx - 2.0f)) ||
        (x < 1.0f) ||
        (y > (ny - 2.0f)) ||
        (y < 1.0f));
  }

  //--------------------------------------------------------------------------
  public void setFirstDay(boolean bln) {
    firstDay = bln;
  }

  //--------------------------------------------------------------------------
  public long getTimeTp1() {
    return timeTp1;
  }

  //--------------------------------------------------------------------------
  static double geodesicDistance(double lat1, double lon1, double lat2,
      double lon2) {
    //--------------------------------------------------------------
    // Return the curvilinear abscissa s(A[lat1, lon1]B[lat2, lon2])
    double d = 6367000.d * Math.sqrt(2.d
        - 2.d *
        Math.cos(Math.PI * lat1 / 180.d) *
        Math.cos(Math.PI * lat2 / 180.d) *
        Math.cos(Math.PI * (lon1 - lon2) /
        180.d)
        - 2.d *
        Math.sin(Math.PI * lat1 / 180.d) *
        Math.sin(Math.PI * lat2 / 180.d));
    return (d);
  }

//------------------------------------------------------------------------------
//  End of class GetData
//--------------------------------
}
