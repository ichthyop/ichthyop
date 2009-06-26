package ichthyop.datanc;

import java.io.IOException;
import ucar.multiarray.MultiArray;
import ucar.netcdf.NetcdfFile;
import ucar.netcdf.Attribute;
import java.io.*;
import java.util.*;
import ichthyop.util.*;
import ichthyop.GetConfig;
import ichthyop.Simulation;
import ichthyop.util.*;

public class GetMData3D
    extends GetData {

  double[] xdxuT;
  double xdyvT;
  float[] s_rT;

  private String strSigma, strDxi, strDeta;

//------------------------------------------------------------------------------
//  Override abstract methods inherited from GetData
//------------------------------------------------------------------------------

  //----------------------------------------------------------------------------
  void setFieldsName() {
    strXiDim = GetConfig.getStrXiDim();
    strEtaDim = GetConfig.getStrEtaDim();
    strZDim = GetConfig.getStrZDim();
    strTimeDim = GetConfig.getStrTimeDim();
    strLon = GetConfig.getStrLon();
    strLat = GetConfig.getStrLat();
    strBathy = GetConfig.getStrBathy();
    strU = GetConfig.getStrU();
    strV = GetConfig.getStrV();
    strZeta = GetConfig.getStrZeta();
    strTp = GetConfig.getStrTp();
    strSal = GetConfig.getStrSal();
    strTime = GetConfig.getStrTime();
    strSigma = GetConfig.getStrSigma();
  }

  //----------------------------------------------------------------------------
  void getCstVarNC() {

    xlonRhoT = new double[ny][nx];
    xlatRhoT = new double[ny][nx];
    xmaskRhoT = new double[ny][nx];
    xhT = new double[ny][nx];
    xzetaTp0 = new double[ny][nx];
    xzetaTp1 = new double[ny][nx];
    s_rT = new float[nz];
    xdxuT = new double[ny];
    xdyvT = 0.d;

    try {
      //System.out.println(strLon);
      MultiArray xlonRho = ncIn.get(strLon).copyout(new int[] {
          0}
          , new int[] {
          nx});

      MultiArray xlatRho = ncIn.get(strLat).copyout(new int[] {
          0}
          , new int[] {
          ny});

      MultiArray xh = ncIn.get(strBathy).copyout(new int[] {
          0, 0}
          , new int[] {
          ny, nx});
      MultiArray zetaTp0 = ncIn.get(strZeta).copyout(new int[] {
          0, 0, 0}
          , new int[] {
          1, ny, nx});

      MultiArray s_r = ncIn.get(strSigma).copyout(new int[] {
          0},
          new int[] {
          nz});

      double[] ptGeo1, ptGeo2;
      for (int j = 0; j < ny; j++) {
        for (int i = 0; i < nx; i++) {
          int[] ind = new int[] {
              j, i};
          xlonRhoT[j][i] = xlonRho.getDouble(new int[] {
              i});
          xlatRhoT[j][i] = xlatRho.getDouble(new int[] {
              j});
          xhT[j][i] = xh.getDouble(ind);
          xmaskRhoT[j][i] = xhT[j][i] == -999.0 ? 0 : 1;
          xzetaTp0[j][i] = zetaTp0.getDouble(new int[] {
              0, j, i});
        }
        ptGeo1 = grid2Geo(1.5, j);
        ptGeo2 = grid2Geo(2.5, j);
        xdxuT[j] = geodesicDistance(ptGeo1[0], ptGeo1[1], ptGeo2[0],
            ptGeo2[1]);
      }
      ptGeo1 = grid2Geo(1, 1.5);
      ptGeo2 = grid2Geo(1, 2.5);
      xdyvT = geodesicDistance(ptGeo1[0], ptGeo1[1], ptGeo2[0], ptGeo2[1]);

      xzetaTp1 = xzetaTp0;
      int k = nz;
      while (k-- > 0) {
        s_rT[k] = s_r.getFloat(new int[] {
            k});
      }

    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  //----------------------------------------------------------------------------
  void getCstSigLevels() {
    double[][][] z_r_tmp = new double[nz][ny][nx];
    double[][][] z_w_tmp = new double[nz + 1][ny][nx];

    float[] s_w = new float[nz];

    s_w[nz - 1] = 1.f;
    for (int k = nz - 1; k-- > 0; ) {
      s_w[k] = .5f * (s_rT[k + 1] + s_rT[k]);
    }

    for (int i = nx; i-- > 0; ) {
      for (int j = ny; j-- > 0; ) {
        z_w_tmp[0][j][i] = -xhT[j][i];
        for (int k = nz; k-- > 0; ) {
          z_r_tmp[k][j][i] = (s_rT[k] - 1.f) * xhT[j][i];
          z_w_tmp[k + 1][j][i] = (s_w[k] - 1.f) * xhT[j][i];
        }
      }
    }
    z_r_cst = new double[nz][ny][nx];
    z_w_cst = new double[nz + 1][ny][nx];

    z_r_cst = z_r_tmp;
    z_w_cst = z_w_tmp;

    z_w = new double[2][nz + 1][ny][nx];

  }

  //----------------------------------------------------------------------------
  public double[] getMoveEuler(double[] pGrid, long time, long dt) {
    double co, CO, x, dw, du, dv, x_euler, dt_sec;

    //-----------------------------------------------------------
    // Interpolate the velocity, temperature and salinity fields
    // in the computational grid.

    double ix, jy, kz;
    ix = pGrid[0];
    jy = pGrid[1];
    kz = pGrid[2];

    du = 0.d;
    dv = 0.d;
    dw = 0.d;
    x_euler = (double) (dt_HyMo - (timeTp1 - time)) / (double) dt_HyMo;
    dt_sec = (double) (dt / 1000l);

    try {
      //-----------------------
      //Get dw
      int i = (int) ix;
      int j = (int) jy;
      int k = (int) Math.round(kz);
      double dx = ix - (double) i;
      double dy = jy - (double) j;
      double dz = kz - (double) k;
      CO = 0.d;
      for (int ii = 0; ii < 2; ii++) {
        for (int jj = 0; jj < 2; jj++) {
          for (int kk = 0; kk < 2; kk++) {
            //if (isInWater(i + ii, j + jj)) {
            {
              co = Math.abs( (1.d - (double) ii - dx) *
                  (1.d - (double) jj - dy) *
                  (.5d - (double) kk - dz));
              CO += co;
              x = 0.d;
              x = (1.d - x_euler) * xwTp0[k + kk][j + jj][i + ii]
                  + x_euler * xwTp1[k + kk][j + jj][i + ii];
              dw += 2.d * x * co /
                  (z_w[0][Math.min(k + kk + 1, nz)][j + jj][i + ii]
                  - z_w[0][Math.max(k + kk - 1, 0)][j + jj][i + ii]);

            }
          }
        }
      }
      dw *= dt_sec;
      if (CO != 0) {
        dw /= CO;
      }

      //------------------------
      // Get du
      kz = Math.min(kz, nz - 1.00001f);
      i = (int) Math.round(ix);
      k = (int) kz;
      dx = ix - (double) i;
      dz = kz - (double) k;
      CO = 0.d;
      for (int ii = 0; ii < 2; ii++) {
        for (int jj = 0; jj < 2; jj++) {
          for (int kk = 0; kk < 2; kk++) {
            //if (isInWater(i + ii, j + jj)) {
            {
              co = Math.abs( (.5d - (double) ii - dx) *
                  (1.d - (double) jj - dy) *
                  (1.d - (double) kk - dz));
              CO += co;
              x = 0.d;
              try {
                x = (1.d - x_euler) * xuTp0[k + kk][j + jj][i + ii - 1]
                    + x_euler * xuTp1[k + kk][j + jj][i + ii - 1];
              }
              catch (java.lang.ArrayIndexOutOfBoundsException e) {
                System.out.println("k " + (k + kk));
                System.exit(0);
              }
              du += x * co / xdxuT[j + jj];
            }
            //else {System.out.println("land " + xuTp0[k + kk][j + jj][i + ii]);}
          }
        }
      }
      du *= dt_sec;
      if (CO != 0) {
        du /= CO;
      }

      //-------------------------
      // Get dv
      i = (int) ix;
      j = (int) Math.round(jy);
      dx = ix - (double) i;
      dy = jy - (double) j;
      CO = 0.d;
      for (int kk = 0; kk < 2; kk++) {
        for (int jj = 0; jj < 2; jj++) {
          for (int ii = 0; ii < 2; ii++) {
            //if (isInWater(i + ii, j + jj)) {
            {
              co = Math.abs( (1.d - (double) ii - dx) *
                  (.5d - (double) jj - dy) *
                  (1.d - (double) kk - dz));
              CO += co;
              x = 0.d;
              x = (1.d - x_euler) * xvTp0[k + kk][j + jj - 1][i + ii]
                  + x_euler * xvTp1[k + kk][j + jj - 1][i + ii];
              dv += x * co / xdyvT;
            }
          }
        }
      }
      dv *= dt_sec;
      if (CO != 0) {
        dv /= CO;
      }
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      System.out.println("!! Error --> ArrayIndexOutOfBoundException (ignored) - Problem interpolating [du, dv, dw], return null");
      return new double[] {0.d, 0.d, 0.d};
    }
    if (du > Resources.THRESHOLD_CFL) {
      System.out.println("! WARNING : CFL broken for u " + (float) du);
    }
    if (dv > Resources.THRESHOLD_CFL) {
      System.out.println("! WARNING : CFL broken for v " + (float) dv);
    }

    return (new double[] {
        du, dv, dw});
  }

  //----------------------------------------------------------------------------
  public double[] getMoveEulerCost(double[] pGrid, long time, long dt) {
    double co, CO, x, dw, du, dv, x_euler, dt_sec;

    double ix, jy, kz;
    ix = pGrid[0];
    jy = pGrid[1];
    kz = pGrid[2];

    du = 0.d;
    dv = 0.d;
    dw = 0.d;
    x_euler = (double) (dt_HyMo - (timeTp1 - time)) / (double) dt_HyMo;
    dt_sec = (double) (dt / 1000l);

    try {
      //-----------------------
      //Get dw
      int i = (int) Math.round(ix);
      int j = (int) Math.round(jy);
      int k = (int) Math.round(kz);
      double dz = kz - (double) k;
      CO = 0.d;
      for (int kk = 0; kk < 2; kk++) {
        //if (isInWater(i, j)) {
        {
          co = Math.abs(.5d - (double) kk - dz);
          CO += co;
          x = 0.d;
          x = (1.d - x_euler) * xwTp0[k + kk][j][i]
              + x_euler * xwTp1[k + kk][j][i];
          dw += 2.d * x * co /
              (z_w[0][Math.min(k + kk + 1, nz)][j][i]
              - z_w[0][Math.max(k + kk - 1, 0)][j][i]);
        }
      }
      dw *= dt_sec;
      if (CO != 0) {
        dw /= CO;
      }

      //------------------------
      // Get du
      kz = Math.min(kz, nz - 1.00001f);
      k = (int) kz;
      double dx = ix - (double) i;
      dz = kz - (double) k;
      CO = 0.d;
      for (int ii = 0; ii < 2; ii++) {
        for (int kk = 0; kk < 2; kk++) {
          //if (isInWater(i + ii, j)) {
          {
            co = Math.abs( (.5d - (double) ii - dx) *
                (1.d - (double) kk - dz));
            CO += co;
            x = 0.d;
            x = (1.d - x_euler) * xuTp0[k + kk][j][i + ii - 1]
                + x_euler * xuTp1[k + kk][j][i + ii - 1];
            du += x * co / xdxuT[j];
          }
        }
      }
      du *= dt_sec;
      if (CO != 0) {
        du /= CO;
      }

      //-------------------------
      // Get dv
      double dy = jy - (double) j;
      CO = 0.d;
      for (int kk = 0; kk < 2; kk++) {
        for (int jj = 0; jj < 2; jj++) {
          //if (isInWater(i, j + jj)) {
          {
            co = Math.abs( (.5d - (double) jj - dy) *
                (1.d - (double) kk - dz));
            CO += co;
            x = 0.d;
            x = (1.d - x_euler) * xvTp0[k + kk][j + jj - 1][i]
                + x_euler * xvTp1[k + kk][j + jj - 1][i];
            dv += x * co / xdyvT;
          }
        }
      }
      dv *= dt_sec;
      if (CO != 0) {
        dv /= CO;
      }
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      System.out.println("!! Error --> ArrayIndexOutOfBoundException (ignored) - Problem interpolating [du, dv, dw], return null");
      return new double[] {0.d, 0.d, 0.d};
    }

    if (du > Resources.THRESHOLD_CFL) {
      System.out.println("! WARNING : CFL broken for u " + (float) du);
    }
    if (dv > Resources.THRESHOLD_CFL) {
      System.out.println("! WARNING : CFL broken for v " + (float) dv);
    }

    return (new double[] {
        du, dv, dw});

  }

  //----------------------------------------------------------------------------
  double[][][] computeW() {
    double[][][] Huon = new double[nz][ny][nx];
    double[][][] Hvom = new double[nz][ny][nx];
    double[][][] z_w_tmp = z_w[1];

    //---------------------------------------------------
    // Calculation Coeff Huon & Hvom
    for (int k = nz; k-- > 0; ) {
      for (int i = 0; i++ < nx - 1; ) {
        for (int j = ny; j-- > 0; ) {
          Huon[k][j][i] = .5d * ( (z_w_tmp[k + 1][j][i] -
              z_w_tmp[k][j][i]) +
              (z_w_tmp[k + 1][j][i - 1] -
              z_w_tmp[k][j][i - 1])) * xdyvT *
              xuTp1[k][j][i - 1];
        }
      }
      for (int i = nx; i-- > 0; ) {
        for (int j = 0; j++ < ny - 1; ) {
          Hvom[k][j][i] = .25d * ( ( (z_w_tmp[k + 1][j][i] -
              z_w_tmp[k][j][i]) +
              (z_w_tmp[k + 1][j - 1][i] -
              z_w_tmp[k][j - 1][i])) *
              (xdxuT[j] +
              xdxuT[j - 1]))
              *
              xvTp1[k][j - 1][i];
        }
      }
    }

    //---------------------------------------------------
    // Calcultaion of w(i, j, k)
    double[] wrk = new double[nx];
    double[][][] w = new double[nz + 1][ny][nx];

    for (int j = ny - 1; j-- > 0; ) {
      for (int i = nx; i-- > 0; ) {
        w[0][j][i] = 0.0d;
      }
      for (int k = 0; k++ < nz; ) {
        for (int i = nx - 1; i-- > 0; ) {
          w[k][j][i] = w[k - 1][j][i] - Huon[k - 1][j][i + 1] +
              Huon[k - 1][j][i]
              - Hvom[k - 1][j + 1][i] + Hvom[k - 1][j][i];
        }
      }
      for (int i = nx; i-- > 0; ) {
        wrk[i] = w[nz][j][i] /
            (z_w_tmp[nz][j][i] - z_w_tmp[0][j][i]);
      }
      for (int k = nz; k-- >= 2; ) {
        for (int i = nx; i-- > 0; ) {
          w[k][j][i] += -wrk[i] *
              (z_w_tmp[k][j][i] - z_w_tmp[0][j][i]);
        }
      }
      for (int i = nx; i-- > 0; ) {
        w[nz][j][i] = 0.0d;
      }
    }

    //---------------------------------------------------
    // Boundary Conditions
    for (int k = nz + 1; k-- > 0; ) {
      for (int j = ny; j-- > 0; ) {
        w[k][j][0] = w[k][j][1];
        w[k][j][nx - 1] = w[k][j][nx - 2];
      }
    }
    for (int k = nz + 1; k-- > 0; ) {
      for (int i = nx; i-- > 0; ) {
        w[k][0][i] = w[k][1][i];
        w[k][ny - 1][i] = w[k][ny - 2][i];
      }
    }

    //---------------------------------------------------
    // w * dxu * dyv
    for (int i = nx; i-- > 0; ) {
      for (int j = ny; j-- > 0; ) {
        for (int k = nz + 1; k-- > 0; ) {
          w[k][j][i] = w[k][j][i] / (xdxuT[j] * xdyvT);
        }
      }
    }

    //---------------------------------------------------
    // Return w
    return (w);
  }

  //----------------------------------------------------------------------------
  public void setAllFieldsAtTime(long time) {

    if (firstDay) {
      openFile(t0 = Simulation.get_t0());
      timeTp1 = t0;
      setAllFieldsTp1AtTime(currentNcInRank);
      firstDay = false;
    }

    if (time < timeTp1) {
      return;
    }

    xuTp0 = xuTp1;
    xvTp0 = xvTp1;
    xwTp0 = xwTp1;
    xzetaTp0 = xzetaTp1;
    xtempTp0 = xtempTp1;
    xsaltTp0 = xsaltTp1;
    z_w[0] = z_w[1];
    currentNcInRank++;

    setAllFieldsTp1AtTime(currentNcInRank);

  }

  //--------------------------------------------------------------------------
  void setAllFieldsTp1AtTime(int i_time) {

    xuTp1 = new double[nz][ny][nx - 1];
    xvTp1 = new double[nz][ny - 1][nx];
    xwTp1 = new double[nz + 1][ny][nx];

    try {
      MultiArray uTp1 = ncIn.get(strU).copyout(new int[] {i_time, 0, 0, 0},
          new int[] {1, nz, ny, (nx - 1)});

      MultiArray vTp1 = ncIn.get(strV).copyout(new int[] {
          i_time, 0, 0, 0}, new int[] {1, nz, (ny - 1), nx});
      MultiArray zetaTp1 = ncIn.get(strZeta).copyout(new int[] {i_time, 0, 0},
          new int[] {1, ny, nx});

      for (int j = 0; j < ny - 1; j++) {
        for (int i = 0; i < nx - 1; i++) {
          xzetaTp1[j][i] = zetaTp1.getDouble(new int[] {0, j, i});
          for (int k = 0; k < nz; k++) {
            xuTp1[k][j][i] = uTp1.getDouble(new int[] {0, k, j, i});
            xvTp1[k][j][i] = vTp1.getDouble(new int[] {0, k, j, i});
          }
        }
      }
      for (int i = 0; i < nx; i++) {
        xzetaTp1[ny - 1][i] = zetaTp1.getDouble(new int[] {0, ny - 1, i});
      }
      for (int j = 0; j < ny; j++) {
        xzetaTp1[j][nx - 1] = zetaTp1.getDouble(new int[] {0, j, nx - 1});
      }
      for (int k = 0; k < nz; k++) {
        for (int j = 0; j < ny - 1; j++) {
          xvTp1[k][j][nx - 1] = vTp1.getDouble(new int[] {0, k, j, nx - 1});
        }
        for (int i = 0; i < nx - 1; i++) {
          xuTp1[k][ny - 1][i] = uTp1.getDouble(new int[] {0, k, ny - 1, i});
        }
      }
    }
    catch (Exception ex) {
      System.out.println(
          "!! Error --> Fatal exception - Problem extracting fields from file "
          + ncIn.getFile().toString());
      System.out.println("Exit");
      System.exit(0);
    }

    try {
      xtempTp1 = ncIn.get(strTp).copyout(new int[] {
          i_time, 0, 0, 0},
          new int[] {
          1, nz, ny, nx});
      xsaltTp1 = ncIn.get(strSal).copyout(new int[] {
          i_time, 0, 0, 0},
          new int[] {
          1, nz, ny, nx});
    }
    catch (Exception ex) {
      System.out.println(
          "!! Error --> Exception (ignored) - Problem extracting temperature and/or salinity fields from file "
          + ncIn.getFile().toString());
    }

    try {
      MultiArray xTimeTp1 = ncIn.get(strTime).copyout(new int[] {i_time},
          new int[] {1});
      timeTp1 = (long) (xTimeTp1.getDouble(new int[] {0}) * 1e3);
    }
    catch (Exception ex) {
      timeTp1 += dt_HyMo;
    }

    getSigLevelsTp1(1);
    xwTp1 = computeW();
  }

  //--------------------------------------------------------------------------
  public double[] getMoveRK4(double[] pGrid, long time, long dt) {

    double[] dU = new double[3];
    double ix, jy, kz;

    double[] k1 = getMoveEuler(new double[] {
        pGrid[0], pGrid[1], pGrid[2]}, time, dt);

    ix = pGrid[0] + .5d * k1[0];
    jy = pGrid[1] + .5d * k1[1];
    if (isOnEdge(ix, jy)) {
      return new double[] {.5d * k1[0], .5d * k1[1], 0};
    }
    kz = Math.max(0.d, Math.min(pGrid[2] + .5d * k1[2], nz - 1.00001f));

    double[] k2 = getMoveEuler(new double[] {
        ix, jy, kz},
        time + dt / 2, dt);

    ix = pGrid[0] + .5d * k2[0];
    jy = pGrid[1] + .5d * k2[1];
    if (isOnEdge(ix, jy)) {
      return new double[] {.5d * k2[0], .5d * k2[1], 0};
    }
    kz = Math.max(0.d, Math.min(pGrid[2] + .5d * k2[2], nz - 1.00001f));

    double[] k3 = getMoveEuler(new double[] {
        ix, jy, kz},
        time + dt / 2, dt);

    ix = pGrid[0] + k3[0];
    jy = pGrid[1] + k3[1];
    if (isOnEdge(ix, jy)) {
      return new double[] {k3[0], k3[1], 0};
    }
    kz = Math.max(0.d, Math.min(pGrid[2] + k3[2], nz - 1.00001f));

    double[] k4 = getMoveEuler(new double[] {ix, jy, kz}, time + dt, dt);

    for (int i = 0; i < 3; i++) {
      dU[i] = (k1[i] + 2.d * k2[i] + 2.d * k3[i] + k4[i]) / 6.d;
    }

    return (dU);

  }

  //--------------------------------------------------------------------------
  public double[] getMoveRK4Cost(double[] pGrid, long time, long dt) {

    double[] dU = new double[3];
    double ix, jy, kz;

    double[] k1 = getMoveEulerCost(new double[] {pGrid[0], pGrid[1], pGrid[2]},
        time, dt);

    ix = pGrid[0] + .5d * k1[0];
    jy = pGrid[1] + .5d * k1[1];
    if (isOnEdge(ix, jy)) {
      return new double[] {.5d * k1[0], .5d * k1[1], 0};
    }
    kz = Math.max(0.d, Math.min(pGrid[2] + .5d * k1[2], nz - 1.00001f));

    double[] k2 = getMoveEulerCost(new double[] {ix, jy, kz},
        time + dt / 2, dt);

    ix = pGrid[0] + .5d * k2[0];
    jy = pGrid[1] + .5d * k2[1];
    if (isOnEdge(ix, jy)) {
      return new double[] {.5d * k2[0], .5d * k2[1], 0};
    }
    kz = Math.max(0.d, Math.min(pGrid[2] + .5d * k2[2], nz - 1.00001f));

    double[] k3 = getMoveEulerCost(new double[] {
        ix, jy, kz},
        time + dt / 2, dt);

    ix = pGrid[0] + k3[0];
    jy = pGrid[1] + k3[1];
    if (isOnEdge(ix, jy)) {
      return new double[] {k3[0], k3[1], 0};
    }
    kz = Math.max(0.d, Math.min(pGrid[2] + k3[2], nz - 1.00001f));

    double[] k4 = getMoveEulerCost(new double[] {ix, jy, kz},
        time + dt, dt);

    for (int i = 0; i < 3; i++) {
      dU[i] = (k1[i] + 2.d * k2[i] + 2.d * k3[i] + k4[i]) / 6.d;
    }

    return (dU);
  }

  //----------------------------------------------------------------------------
  public double adimensionalize(double number, double xRho, double yRho) {
    return 2.d * number / (xdyvT + xdxuT[ (int) Math.round(yRho)]);
  }

//------------------------------------------------------------------------------
//  End of class GetDataMARS
//---------------------------

}
