package ichthyop;

import ichthyop.datanc.GetData;
import ichthyop.util.*;

/**
 *
 * <p>Title: Particle 2D</p>
 *
 * <p>Description: Extends class Particle. Adapted to 2D simulation</p>
 * Refers to Particle3D.java for description of constructors and methods.
 */
public class Particle2D
    extends Particle {

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Constructors
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  public Particle2D(int indexIndiv, double xmin, double xmax, double ymin,
      double ymax) {

    this.indexIndiv = indexIndiv;

    boolean outZone = true;
    double x = 0, y = 0;
    while (outZone) {

      x = xmin + Math.random() * (xmax - xmin);
      y = ymin + Math.random() * (ymax - ymin);

      outZone = ! (setXY(x, y) && ( (numInitZone = getNumZone(Resources.RELEASE
          )) != -1)) ||
          isOnEdge(nx, ny);
    }
    setXY(x, y);
    grid2Geog();

    init();

  }

  //----------------------------------------------------------------------------
  public Particle2D(int indexIndiv, int numZone, double x, double y) {

    setXY(x, y);
    grid2Geog();

    this.indexIndiv = indexIndiv;
    numInitZone = numZone;
    init();
  }

  //----------------------------------------------------------------------------
  public Particle2D(int indexIndiv, double lon, double lat) {
    this.indexIndiv = indexIndiv;
    setLLD(lon, lat, 0);
    numInitZone = 0;
    init();
  }

  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  //% Definition of the inherited abstract methods
  //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

  //----------------------------------------------------------------------------
  public void geog2Grid() {
    double[] pGrid = GetData.geo2Grid(getLon(), getLat());
    setX(pGrid[0]);
    setY(pGrid[1]);
  }

  //----------------------------------------------------------------------------
  public void grid2Geog() {
    double[] pGeog = GetData.grid2Geo(getX(), getY());
    setLon(pGeog[0]);
    setLat(pGeog[1]);
  }

  //----------------------------------------------------------------------------
  void moveEuler() {

    double[] mvt;

    if (living) {
      mvt = GetData.isCloseToCost(this)
          ? Simulation.dataNC.getMoveEulerCost(getXY(), time, dt)
          : Simulation.dataNC.getMoveEuler(getXY(), time, dt);

      incrementXY(mvt[0], mvt[1]);

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

  }

  //----------------------------------------------------------------------------
  void moveRK4() {
    double[] mvt;

    if (living) {
      mvt = GetData.isCloseToCost(this)
          ? Simulation.dataNC.getMoveRK4Cost(getXY(), time, dt)
          : Simulation.dataNC.getMoveRK4(getXY(), time, dt);

      incrementXY(mvt[0], mvt[1]);

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

  }

//------------------------------------------------------------------------------
// End of class
}
