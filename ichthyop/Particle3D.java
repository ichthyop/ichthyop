package ichthyop;

import ichthyop.datanc.GetData;
import ichthyop.util.Resources;

/**
 *
 * <p>Title: Particle 3D</p>
 *
 * <p>Description: Extends class Particle. Adapted to 3D simulation.</p>
 */
public class Particle3D
    extends Particle {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Constructors
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  /**
   * Random releasing within the volume delimited by (xmin, ymin), (xmax, ymax)
   * and (depthMin, depthMax).
   */
  public Particle3D(int indexIndiv, double xmin, double xmax, double ymin,
      double ymax,
      double depthMin, double depthMax) {

    this.indexIndiv = indexIndiv;

    boolean outZone = true;
    double x = 0, y = 0, depth;
    while (outZone) {

      x = xmin + Math.random() * (xmax - xmin);
      y = ymin + Math.random() * (ymax - ymin);

      outZone = ! (setXY(x, y) && ( (numInitZone = getNumZone(Resources.RELEASE
          )) != -1)) ||
          isOnEdge(nx, ny);
    }
    if (GetConfig.isIsoDepth()) {
      depth = GetConfig.getIsoDepth();
    }
    else {
      depthMax = Math.min(depthMax,
          GetData.getBathy( (int) Math.round(x),
          (int) Math.round(y)));
      depth = - (depthMin + Math.random() * (depthMax - depthMin));
    }
    setXYD(x, y, depth);
    grid2Geog();

    init();
  }

  //----------------------------------------------------------------------------
  /**
   * Releases at grid point (x, y) and depth.
   */
  public Particle3D(int indexIndiv, int numZone, double x, double y,
      double depth) {

    setXYD(x, y, depth);
    grid2Geog();

    this.indexIndiv = indexIndiv;
    numInitZone = numZone;

    init();
  }

  //----------------------------------------------------------------------------
  /**
   * Releases at geographic point (lon, lat, depth)
   */
  public Particle3D(int indexIndiv, double lon, double lat, double depth) {
    this.indexIndiv = indexIndiv;
    setLLD(lon, lat, depth);
    numInitZone = 0;
    /*ytrack = new double[Simulation.getNbSteps()];
         xtrack = new double[Simulation.getNbSteps()];
         hvtrack = new int[Simulation.getNbSteps()][2];*/
    init();
    //System.out.println("P3D " + getDepth() + " " + GetData.getBathy((int)Math.round(getX()), (int)Math.round(getY())) + " " + getZ());
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the inherited abstract methods
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  /**
   * Transform geographical coordinates into grid coordinates.
   */
  public void geog2Grid() {
    double[] pGrid = GetData.geo2Grid(getLon(), getLat());
    setX(pGrid[0]);
    setY(pGrid[1]);
    setZ(GetData.zGeo2Grid(pGrid[0], pGrid[1], getDepth()));

  }

  //----------------------------------------------------------------------------
  /**
   * Transform grid coordinates into geographical coordinates.
   */
  public void grid2Geog() {
    double[] pGeog = GetData.grid2Geo(getX(), getY(), getZ());
    setLon(pGeog[1]);
    setLat(pGeog[0]);
    setDepth(pGeog[2]);
  }

  //----------------------------------------------------------------------------
  /**
   * Advects the particle with Forward Euler scheme.
   */
  void moveEuler() {
    //----------------------------------------------------
    // Calculate the new position and grow the particles

    double[] mvt;

    if (living) {
      mvt = GetData.isCloseToCost(this)
          ? Simulation.dataNC.getMoveEulerCost(getXYZ(), time, dt)
          : Simulation.dataNC.getMoveEuler(getXYZ(), time, dt);

      if (FLAG_ISODEPTH) {
        incrementXY(mvt[0], mvt[1]);
        setZ(GetData.zGeo2Grid(getX(), getY(), isoDepth));
      }
      else {
        //System.out.println(mvt[2]);
        incrementXYZ(mvt[0], mvt[1], mvt[2]);
      }

      //--------- Test if particules is living
      if (isOnEdge(nx, ny)) {
        dead = Resources.DEAD_OUT;
      }
      else if (!GetData.isInWater(this)) {
        dead = Resources.DEAD_BEACH;
      }
      living = (dead == Resources.DEAD_NOT);
    }

    if (living) {
      grid2Geog();
    }
    //System.out.println((float)getDepth() + " " + (float)getZ());
  }

  //----------------------------------------------------------------------------
  /**
   * Advects the particle with Runge Kutta 4 scheme.
   */
  void moveRK4() {
    double[] mvt;

    if (living) {
      mvt = GetData.isCloseToCost(this)
          ? Simulation.dataNC.getMoveRK4Cost(getXYZ(), time, dt)
          : Simulation.dataNC.getMoveRK4(getXYZ(), time, dt);

      if (FLAG_ISODEPTH) {
        incrementXY(mvt[0], mvt[1]);
        setZ(GetData.zGeo2Grid(getX(), getY(), isoDepth));
      }
      else {
        incrementXYZ(mvt[0], mvt[1], mvt[2]);
      }

      //--------- Test if particule is living
      if (isOnEdge(nx, ny)) {
        dead = Resources.DEAD_OUT;
      }
      else if (!GetData.isInWater(this)) {
        dead = Resources.DEAD_BEACH;
      }
      living = (dead == Resources.DEAD_NOT);

    }
    if (living) {
      grid2Geog();
    }
  }

//------------------------------------------------------------------------------
// End of class
}
