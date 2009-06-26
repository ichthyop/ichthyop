package ichthyop.datanc;

import ucar.multiarray.MultiArray;
import java.io.IOException;
import ichthyop.*;
import ichthyop.util.*;

public class GetMData2D
    extends GetData {

  double[] xdxuT;
  double xdyvT;

  private String strSigma, strDxi, strDeta;

  //--------------------------------------------------------------------------
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
  }

  //--------------------------------------------------------------------------
  void getCstVarNC() {

    xlonRhoT = new double[ny][nx];
    xlatRhoT = new double[ny][nx];
    xmaskRhoT = new double[ny][nx];
    xhT = new double[ny][nx];
    xdxuT = new double[ny];
    xdyvT = 0.d;

    try {
      //System.out.println(strLon);
      MultiArray xlonRho = ncIn.get(strLon).copyout(new int[] {0}
          , new int[] {nx});

      MultiArray xlatRho = ncIn.get(strLat).copyout(new int[] {0}
          , new int[] {ny});

      MultiArray xh = ncIn.get(strBathy).copyout(new int[] {0, 0}
                                                 , new int[] {ny, nx});

      double[] ptGeo1, ptGeo2;
      for (int j = 0; j < ny; j++) {
        for (int i = 0; i < nx; i++) {
          int[] ind = new int[] {
              j, i};
          xlonRhoT[j][i] = xlonRho.getDouble(new int[] {i});
          xlatRhoT[j][i] = xlatRho.getDouble(new int[] {j});
          xhT[j][i] = xh.getDouble(ind);
          xmaskRhoT[j][i] = xhT[j][i] == -999.0 ? 0 : 1;
        }
        ptGeo1 = grid2Geo(1.5, j);
        ptGeo2 = grid2Geo(2.5, j);
        xdxuT[j] = geodesicDistance(ptGeo1[0], ptGeo1[1], ptGeo2[0], ptGeo2[1]);
      }
      ptGeo1 = grid2Geo(1, 1.5);
      ptGeo2 = grid2Geo(1, 2.5);
      xdyvT = geodesicDistance(ptGeo1[0], ptGeo1[1], ptGeo2[0], ptGeo2[1]);

    }
    catch (IOException ex) {
      ex.printStackTrace();
    }

  }

  //--------------------------------------------------------------------------
  void getCstSigLevels() {}

  //--------------------------------------------------------------------------
  public double[] getMoveEuler(double[] pGrid, long time, long dt) {
    double co, CO, x, du, dv, x_euler;

    //-----------------------------------------------------------
    // Interpolate the velocity, temperature and salinity fields
    // in the computational grid.

    double ix, jy;
    ix = pGrid[0];
    jy = pGrid[1];

    du = 0.d;
    dv = 0.d;

    x_euler = (double) (dt_HyMo - (timeTp1 - time)) / (double) dt_HyMo;
    double dt_sec = (double) (dt / 1000l);

    try {
      //------------------------
      // Get du
      int i = (int) Math.round(ix);
      int j = (int) jy;
      double dx = ix - (double) i;
      double dy = jy - (double) j;
      CO = 0.d;
      for (int ii = 0; ii < 2; ii++) {
        for (int jj = 0; jj < 2; jj++) {
          co = Math.abs( (.5d - (double) ii - dx) *
              (1.d - (double) jj - dy));
          CO += co;
          x = 0.d;
          x = (1.d - x_euler) * xuTp0[0][j + jj][i + ii - 1]
              + x_euler * xuTp1[0][j + jj][i + ii - 1];

          du += x * co / xdxuT[j + jj];

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

      for (int jj = 0; jj < 2; jj++) {
        for (int ii = 0; ii < 2; ii++) {
          co = Math.abs( (1.d - (double) ii - dx) *
              (.5d - (double) jj - dy));
          CO += co;
          x = 0.d;
          x = (1.d - x_euler) * xvTp0[0][j + jj - 1][i + ii]
              + x_euler * xvTp1[0][j + jj - 1][i + ii];
          dv += x * co / xdyvT;

        }
      }

      dv *= dt_sec;
      if (CO != 0) {
        dv /= CO;
      }
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      System.out.println("!! Error --> ArrayIndexOutOfBoundException (ignored) - Problem interpolating [du, dv, dw], return null");
      return new double[] {0.d, 0.d};
    }

    if (du > Resources.THRESHOLD_CFL) {
      System.out.println("! WARNING : CFL broken for u " + (float) du);
    }
    if (dv > Resources.THRESHOLD_CFL) {
      System.out.println("! WARNING : CFL broken for v " + (float) dv);
    }


    return (new double[] {du, dv});

  }

  //--------------------------------------------------------------------------
  public double[] getMoveEulerCost(double[] pGrid, long time, long dt) {

    double co, CO, x, du, dv, x_euler;

    double ix, jy;
    ix = pGrid[0];
    jy = pGrid[1];

    du = 0.d;
    dv = 0.d;

    x_euler = (double) (dt_HyMo - (timeTp1 - time)) / (double) dt_HyMo;
    double dt_sec = (double) (dt / 1000l);

    try {
      //------------------------
      // Get du
      int i = (int) Math.round(ix);
      int j = (int) Math.round(jy);
      double dx = ix - (double) i;
      CO = 0.d;
      for (int ii = 0; ii < 2; ii++) {

        co = Math.abs( (.5d - (double) ii - dx));
        CO += co;
        x = 0.d;
        x = (1.d - x_euler) * xuTp0[0][j][i + ii - 1]
            + x_euler * xuTp1[0][j][i + ii - 1];
        du += x * co / xdxuT[j];
      }
      du *= dt_sec;
      if (CO != 0) {
        du /= CO;
      }

      //-------------------------
      // Get dv
      double dy = jy - (double) j;
      CO = 0.d;
      for (int jj = 0; jj < 2; jj++) {
        co = Math.abs( (.5d - (double) jj - dy));
        CO += co;
        x = 0.d;
        x = (1.d - x_euler) * xvTp0[0][j + jj - 1][i]
            + x_euler * xvTp1[0][j + jj - 1][i];
        dv += x * co / xdyvT;
      }
      dv *= dt_sec;
      if (CO != 0) {
        dv /= CO;
      }
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      System.out.println("!! Error --> ArrayIndexOutOfBoundException (ignored) - Problem interpolating [du, dv, dw], return null");
      return new double[] {0.d, 0.d};
    }

    if (du > Resources.THRESHOLD_CFL) {
      System.out.println("! WARNING : CFL broken for u " + (float) du);
    }
    if (dv > Resources.THRESHOLD_CFL) {
      System.out.println("! WARNING : CFL broken for v " + (float) dv);
    }

    return (new double[] {du, dv});

  }

  //--------------------------------------------------------------------------
  double[][][] computeW() {
    return null;
  }

  //--------------------------------------------------------------------------
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
    currentNcInRank++;

    setAllFieldsTp1AtTime(currentNcInRank);

  }

  //--------------------------------------------------------------------------
  void setAllFieldsTp1AtTime(int i_time) {

    xuTp1 = new double[1][ny][nx - 1];
    xvTp1 = new double[1][ny - 1][nx];

    try {

      MultiArray uTp1 = ncIn.get(strU).copyout(new int[] {i_time, 0, 0},
                                               new int[] {1, ny, (nx - 1)});

      MultiArray vTp1 = ncIn.get(strV).copyout(new int[] {i_time, 0, 0},
                                               new int[] {1, (ny - 1), nx});

      for (int j = 0; j < ny - 1; j++) {
        for (int i = 0; i < nx - 1; i++) {
          xuTp1[0][j][i] = uTp1.getDouble(new int[] {0, j, i});
          xvTp1[0][j][i] = vTp1.getDouble(new int[] {0, j, i});
        }
      }

      for (int j = 0; j < ny - 1; j++) {
        xvTp1[0][j][nx - 1] = vTp1.getDouble(new int[] {0, j, nx - 1});
      }
      for (int i = 0; i < nx - 1; i++) {
        xuTp1[0][ny - 1][i] = uTp1.getDouble(new int[] {0, ny - 1, i});
      }
    }
    catch (Exception ex) {
      System.out.println(
          "!! Error --> Fatal exception - Problem extracting fields from file "
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


  }

  //--------------------------------------------------------------------------
  public double[] getMoveRK4(double[] pGrid, long time, long dt) {

    double[] dU = new double[2];
    double ix, jy;

    double[] k1 = getMoveEuler(new double[] {pGrid[0], pGrid[1]}, time, dt);

    ix = pGrid[0] + .5d * k1[0];
    jy = pGrid[1] + .5d * k1[1];
    if (isOnEdge(ix, jy)) {
      return new double[] {.5d * k1[0], .5d * k1[1]};
    }

    double[] k2 = getMoveEuler(new double[] {ix, jy}, time + dt / 2, dt);

    ix = pGrid[0] + .5d * k2[0];
    jy = pGrid[1] + .5d * k2[1];
    if (isOnEdge(ix, jy)) {
      return new double[] {.5d * k2[0], .5d * k2[1]};
    }

    double[] k3 = getMoveEuler(new double[] {ix, jy}, time + dt / 2, dt);

    ix = pGrid[0] + k3[0];
    jy = pGrid[1] + k3[1];
    if (isOnEdge(ix, jy)) {
      return new double[] {k3[0], k3[1]};
    }

    double[] k4 = getMoveEuler(new double[] {ix, jy}, time + dt, dt);

    for (int i = 0; i < 2; i++) {
      dU[i] = (k1[i] + 2.d * k2[i] + 2.d * k3[i] + k4[i]) / 6.d;
    }

    return (dU);

  }

  //--------------------------------------------------------------------------
  public double[] getMoveRK4Cost(double[] pGrid, long time, long dt) {

    double[] dU = new double[2];
    double ix, jy;

    double[] k1 = getMoveEulerCost(new double[] {pGrid[0], pGrid[1]}, time, dt);

    ix = pGrid[0] + .5d * k1[0];
    jy = pGrid[1] + .5d * k1[1];
    if (isOnEdge(ix, jy)) {
      return new double[] {.5d * k1[0], .5d * k1[1]};
    }

    double[] k2 = getMoveEulerCost(new double[] {ix, jy}, time + dt / 2, dt);

    ix = pGrid[0] + .5d * k2[0];
    jy = pGrid[1] + .5d * k2[1];
    if (isOnEdge(ix, jy)) {
      return new double[] {.5d * k2[0], .5d * k2[1]};
    }

    double[] k3 = getMoveEulerCost(new double[] {ix, jy}, time + dt / 2, dt);

    ix = pGrid[0] + k3[0];
    jy = pGrid[1] + k3[1];
    if (isOnEdge(ix, jy)) {
      return new double[] {k3[0], k3[1]};
    }

    double[] k4 = getMoveEulerCost(new double[] {ix, jy}, time + dt, dt);

    for (int i = 0; i < 2; i++) {
      dU[i] = (k1[i] + 2.d * k2[i] + 2.d * k3[i] + k4[i]) / 6.d;
    }

    return (dU);

  }

  //----------------------------------------------------------------------------
  public double adimensionalize(double number, double xRho, double yRho) {
    return 2.d * number / (xdyvT + xdxuT[ (int) Math.round(yRho)]);
  }

}
