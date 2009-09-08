package ichthyop.core;

/** import java.util */
import java.util.Iterator;

/** local import */
import ichthyop.bio.BuoyancyScheme;
import ichthyop.bio.GrowthModel;
import ichthyop.io.Configuration;
import ichthyop.io.Dataset;
import ichthyop.util.IParticle;
import ichthyop.util.Constant;
import ichthyop.ui.MainFrame;
import ichthyop.bio.DVMPattern;

/**
 * Individual based models simulate the behavior of the members of a population.
 * This class is the Object standing behind the Individual. Each member of the
 * population are an instance of Particle. It is one the core classes of the
 * IBM (Simulation, Population and Particle).
 * <p>
 * Particle is a mere ichthyop.core.RhoPoint with additional physical and
 * biological variables. It implements the ichthyop.util.IParticle interface
 * that defines a set of methods each Particle should override.
 * </p>
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 *
 * @see ichthyop.core.RhoPoint
 * @see ichthyop.core.IParticle
 */
public class Particle extends RhoPoint implements IParticle {

///////////////////////////////
// Declaration of the constants
///////////////////////////////
    /**
     * The maximum number of attempts to random release particles in a
     * predefined area before throwing a NullPointerException
     * @see constructor Particle(double, double, double,
    double, double, double)
     */
    private final static int DROP_MAX = 2000;
///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Index of the particle in the Population.
     */
    private int index;
    /**
     * Particle age [second]. Age = 0 when released.
     */
    private long age;
    /**
     * Number of the zone the particle has been released.
     */
    private int numReleaseZone;
    /**
     * Number of current zone. Set to -1 when not located in any zone.
     */
    private int numCurrentZone;
    /**
     * Number of the zone where the particle has been recruited.
     */
    private int numRecruitZone;
    /**
     * Recruitment status for each recruitment zone.
     */
    private boolean[] isRecruited;
    /**
     * Inform whether a particle is newly recruited.
     */
    private boolean isNewRecruited;
    /**
     * Minimum duration [second] a particle has to spend within the same zone
     * before being recruited.
     */
    private static int durationMinInRecruitArea;
    /**
     * Duration [second] presently spent by the particule within the
     * current zone.
     */
    private int timeInZone;
    /**
     * length of the particle [millimeter]
     */
    private double length;
    /**
     * Particle is alive or not.
     */
    private boolean living;
    /**
     * Information about particle death.
     * <ul>
     * <li>ERROR = -1 --> a fatal error occured while computing trajectory
     * <li>DEAD_NOT = 0 --> alive
     * <li>DEAD_OUT = 1 --> out of the domain of simulation
     * <li>DEAD_COLD = 2 --> found lethal sea water temperature
     * (for egg stage only when growth is simulated)
     * <li>DEAD_COLD_LARVA = 3 --> found lethal sea water temperature
     * at larva stage
     * <li>DEAD_BEACH = 4 --> beached particle
     * <li>DEAD_OLD = 5 --> dead old particle
     * </ul>
     */
    private int dead;
    /**
     * Sea water temperature at particle location [celsius].
     */
    private double temperature;
    /**
     * Sea water salinity at particle location [psu].
     */
    private double salinity;
    /**
     * Large phytoplankton concentration at particle location [mMol.m-3]
     */
    private double largePhyto;
    /**
     * Small zooplankton concentration at particle location [mMol.m-3]
     */
    private double smallZoo;
    /**
     * Large zooplankton concentration at particle location [mMol.m-3]
     */
    private double largeZoo;
    /**
     * Associated dataset. Declared static because it is common
     * to all Particle objects.
     * @see io.Dataset.java
     */
    static Dataset data;
    /**
     * Model time step [second]. Declared static because it is common
     * to all Particle objects.
     */
    private static double dt;
    /**
     * Lethal water temperature when growth is not simulated.
     * Declared as static because it is common to all Particle objects.
     */
    private static float lethal_tp;
    private static boolean FLAG_GROWTH, FLAG_LETHAL_TP, FLAG_ISODEPTH,
            FLAG_BUOYANCY, FLAG_DISPLAY_TP, FLAG_VDISP, FLAG_HDISP,
            FLAG_MIGRATION, FLAG_PLANKTON, FLAG_RECRUITMENT;

///////////////
// Constructors
///////////////
    /**
     * Constructs a new particle by random
     * release within the volume delimited by (xmin, ymin), (xmax, ymax)
     * on the horizontal plane and (depthMin, depthMax) on the vertical axe for
     * three-dimensions simulation only.
     * For every attempt, the application determines whether the particle is in
     * water and which zone does it belong to. If not in water or out of zone,
     * it releases again at other random location. The application throws a
     * NullPointerException after <code>DROP_MAX</code> unsuccessful attempts.
     *
     * @param xmin a double, smallest x-coordinate of the release domain.
     * @param xmax double, biggest x-coordinate of the release domain.
     * @param ymin double, smallest y-coordinate of the release domain.
     * @param ymax double, biggest y-coordinate of the release domain.
     * @param depthMin a double, minimum depth of the release domain. Useless
     * for two-dimensions simulation.
     * @param depthMax a double, maximum depth of the release domain. Useless
     * for two-dimensions simulation.
     * @see ichthyop.core.Population#releaseZone()
     */
    public Particle(int index,
            boolean is3D,
            double xmin, double xmax, double ymin, double ymax,
            double depthMin, double depthMax) {

        /** Constructs a new RhoPoint */
        super(is3D);

        living = true;
        boolean outZone = true;
        double x = 0, y = 0, depth = 0;
        int counter = 0;
        this.index = index;

        /** Attempts of random release */
        while (outZone) {

            setXY(
                    x = xmin + Math.random() * (xmax - xmin),
                    y = ymin + Math.random() * (ymax - ymin));

            numReleaseZone = getNumZone(Constant.RELEASE);
            outZone = !Dataset.isInWater(this) || (numReleaseZone == -1) || isOnEdge(Dataset.get_nx(),
                    Dataset.get_ny());

            if (is3D && !outZone) {
                depth = depthMin + Math.random() * (depthMax - depthMin);
                outZone = depth < data.getDepth(x, y, 0);
            }

            if (counter++ > DROP_MAX) {
                throw new NullPointerException(
                        "Unable to release particle. Check out the zone definitions.");
            }

        }
        /** Sets on vertical axes for 3D simulation only */
        if (is3D) {
            setXYD(x, y, depth);
        }

        /** initialises */
        grid2Geog();
        init();
    }

    /**
     * Constructs a particle for three-dimensions simulation and releases at
     * grid point (x, y) and at the specified depth. Used for patchy initial
     * distribution.
     *
     * @param numZone the number of the zone the particle is released in.
     * @param x a double, the initial x-coordinate of the particle.
     * @param y a double, the initial y-coordiante of the particle.
     * @param depth a double, the initial depth of the particle
     * @see ichthyop.core.Population#releaseZone()
     */
    public Particle(int index, boolean is3D,
            int numZone, double x, double y, double depth) {

        super(is3D);
        this.index = index;
        living = true;
        setXYD(x, y, depth);
        numReleaseZone = numZone;
        grid2Geog();
        init();
    }

    /**
     * Constructs a particle for three-dimensions simulation and releases at
     * geographic point (lon, lat, depth).
     *
     * @param lon a double, the initial longitude of the particle
     * @param lat a double, the initial latitude of the particle
     * @param depth a double, the initial depth of the particle
     * @param living a boolean, <code>true</code> if the particle is alive
     * <code>false</code> otherwise. This parameter is used for the release
     * with a restart file. See the release section of the user-guide for
     * details.
     * @see ichthyop.core.Population#releaseTxtFile()
     * @see ichthyop.core.Population#releaseNcFile()
     */
    public Particle(int index, boolean is3D,
            double lon, double lat, double depth,
            boolean living) {

        super(is3D);
        this.index = index;
        this.living = living;
        setLLD(lon, lat, depth);
        if (living) {
            geog2Grid();
            numReleaseZone = 0;
            init();
        }
    }

////////////////////////////
// Definition of the methods
////////////////////////////
    /**
     * Initializes the variables of the particle.
     */
    void init() {

        dt = (double) (Configuration.get_dt() * Configuration.getTimeArrow());
        FLAG_BUOYANCY = Configuration.isBuoyancy();
        FLAG_GROWTH = Configuration.isGrowth();
        FLAG_LETHAL_TP = Configuration.isLethalTp();
        FLAG_DISPLAY_TP = MainFrame.getDisplayColor() == Constant.DISPLAY_TP;
        FLAG_HDISP = Configuration.isHDisp();
        FLAG_VDISP = Configuration.isVDisp();
        FLAG_MIGRATION = Configuration.isMigration();
        FLAG_PLANKTON = Configuration.isPlankton();
        FLAG_RECRUITMENT = Configuration.getTypeRecruitment() != Constant.NONE;

        if (FLAG_MIGRATION) {
            FLAG_ISODEPTH = (Simulation.getDepthDay() == Simulation.getDepthNight());
        }

        if (FLAG_LETHAL_TP) {
            lethal_tp = Simulation.getLethalTpEgg();
        }

        dead = Constant.DEAD_NOT;
        age = 0L;
        numCurrentZone = -1;
        temperature = data.getTemperature(getPGrid(), Simulation.get_t0());
        salinity = data.getSalinity(getPGrid(), Simulation.get_t0());
        length = GrowthModel.LENGTH_INIT;

        if (FLAG_RECRUITMENT) {
            numRecruitZone = -1;
            timeInZone = 0;
            durationMinInRecruitArea = Configuration.getDurationInRecruitArea();
            isRecruited = new boolean[Configuration.getRecruitmentZones().size()];
            isNewRecruited = false;
        }

        if (FLAG_PLANKTON) {
            double[] plankton = data.getPlankton(getPGrid(),
                    Simulation.get_t0());
            largePhyto = plankton[0];
            smallZoo = plankton[1];
            largeZoo = plankton[2];
        }
    }

    /**
     * Moves the particle. The method controls the options of transport:
     * <ul>
     * <li> advection
     * <li> horizontal dispersion
     * <li> vertical dispersion
     * <li> buoyancy
     * <li> vertical migration
     * </ul>
     * The method traduces the following equation
     * <code>X(t + dt) = X(t) + [Ua(t, x, y, z) + Ud(x, y) + Wd(t, z) + Wm(t, z)]dt</code>
     * with X the location of the particle, Ua the input model velocity vector,
     * Ur a random component simulating horizontal dispersion, Wd a
     * random component for the vertical dispersion and Wm a term attempting
     * to simulate vertical migration of larvae.
     *
     * @param time a double, the current time [second] of the simulation
     * @throws an ArrayIndexOutOfBoundsException if an error occured when
     * moving the particle.
     * @see #advectForward() and advectBackward() methods for more details about
     * advection.
     * @see io/Dataset#getHDispersion() for details about horizontal dispersion
     * @see io/Dataset#getVDispersion() for details about vertical dispersion
     * @see #migrate() for details about vertical migration
     */
    void move(double time) throws ArrayIndexOutOfBoundsException {

        /** Advection diffusion */
        if (dt >= 0) {
            advectForward(time);
        } else {
            advectBackward(time);
        }

        if (FLAG_HDISP) {
            increment(data.getHDispersion(getPGrid(), dt));
        }

        if (FLAG_VDISP) {
            increment(data.getVDispersion(getPGrid(), time, dt));
        }

        /** Test if particules is living */
        if (isOnEdge(Dataset.get_nx(), Dataset.get_ny())) {
            die(Constant.DEAD_OUT);
        } else if (!Dataset.isInWater(this)) {
            die(Constant.DEAD_BEACH);
        }

        /** buoyancy */
        if (FLAG_BUOYANCY && living) {
            addBuoyancy(time);
        }

        /** vertical migration */
        if (FLAG_MIGRATION && living) {
            migrate(time);
        }

        /** Transform (x, y, z) into (lon, lat, depth) */
        if (living) {
            grid2Geog();
        }
    }

    /**
     * Advects the particle forward in time with the apropriate scheme (Euler
     * or Runge Kutta 4).
     * <code>dX(t) = Ua(t, x, y, z)dt</code> with
     * <code>dX(t) = X(t + dt) - X(t)</code> the
     * displacement of the particle due to advection and <code>Ua</code>
     * the input model velocity vector.
     *
     * @param time a double, the current time [second] of the simulation
     * @throws an ArrayIndexOutOfBoundsException if an error occured while
     * advecting the particle.
     * @see ichthyop.io.Dataset#advectEuler() for details about the euler
     * advection scheme.
     * @see ichthyop.io.Dataset#advectRk4() for details about the Runge Kutta 4
     * advection scheme.
     */
    private void advectForward(double time) throws
            ArrayIndexOutOfBoundsException {

        double[] mvt = (Configuration.getScheme() == Constant.EULER)
                ? data.advectEuler(getPGrid(), time, dt)
                : data.advectRk4(getPGrid(), time, dt);

        increment(mvt);
    }

    /**
     * Advects the particle forward in time with the apropriate scheme (Euler
     * or Runge Kutta 4).
     * The process is a bit more complex than forward advection.
     * <pre>
     * Let's take X(t) = |x, y, z the particle vector position at time = t.
     * X1(t - dt) = X(t) - Ua(t, x, y, z)dt with vector X1 = |x1, y1, z1
     * X(t - dt) = X(t) - Ua(t, x1, y1, z1)dt
     * With Ua the input model velocity vector.
     * </pre>
     *
     * @param time a double, the current time [second] of the simulation
     * @throws an ArrayIndexOutOfBoundsException if an error occured while
     * advecting the particle.
     * @see ichthyop.io.Dataset#advectEuler() for details about the euler
     * advection scheme.
     * @see ichthyop.io.Dataset#advectRk4() for details about the Runge Kutta 4
     * advection scheme.
     */
    private void advectBackward(double time) throws
            ArrayIndexOutOfBoundsException {

        double[] mvt, pgrid;

        if (Configuration.getScheme() == Constant.EULER) {
            mvt = data.advectEuler(pgrid = getPGrid(), time, dt);
            for (int i = 0; i < mvt.length; i++) {
                pgrid[i] += mvt[i];
            }
            mvt = data.advectEuler(pgrid, time, dt);
        } else {
            mvt = data.advectRk4(pgrid = getPGrid(), time, dt);
            for (int i = 0; i < mvt.length; i++) {
                pgrid[i] += mvt[i];
            }
            mvt = data.advectRk4(pgrid, time, dt);
        }

        increment(mvt);
    }

    /**
     * Gets the number of the zone at current location.
     *
     * @param typeZone an integer codind for the type of zone, recruitment zone
     * or release zone.
     * @return the number of the zone at particle current location or -1
     * if the particle is not within any zone.
     * @see util.Constant for details about the labels characterizing the type
     * of zone.
     */
    int getNumZone(int typeZone) {

        int nZone = -1;
        boolean foundZone = false;
        Iterator iter = typeZone == Constant.RELEASE
                ? Configuration.getReleaseZones().iterator()
                : Configuration.getRecruitmentZones().iterator();
        while (!foundZone && iter.hasNext()) {
            Zone znTmp = (Zone) iter.next();
            if (znTmp.isXYInZone(getX(), getY())) {
                nZone = znTmp.getIndex();
                foundZone = true;
            }
        }
        return nZone;

    }

    /**
     * Gets the number of the zone at current location, expressed in the output
     * format. Let's call N the number of release zones and M the number of
     * recruitment zones. Within the application, release zones are indexed
     * from 0 to N - 1, and recruitment zones from 0 to M - 1.
     * There would be no way, in the output file, to distinguish release and
     * recruitment zones, just using their natural index. It is been decided to
     * format the zone index the following way:
     * <ul>
     * <li>Release zone are indexed from 1 to N
     * <li>Recruitment zone are indexed from -1 to -M
     * <li>Zero for out of zone.
     * </ul>
     * @return a strictly postive integer, the number of the release zone OR a
     * stricly negative integer, the number of the recruitment zone OR zero if
     * the particle is not in any zone.
     */
    public int getNumZoneNC() {

        numCurrentZone = getNumZone(Constant.RECRUITMENT);
        return (numCurrentZone != -1)
                ? -1 * (numCurrentZone + 1)
                : getNumZone(Constant.RELEASE) + 1;
    }

    /**
     * Makes the particle die for the specified cause. The particle is
     * desactivated and geographical coordinates and properties are set to NaN.
     * A dead particle is not transported anymore, neither grow.
     *
     * @param dead an int that characterized the cause of death.
     * @see util.Constant for details about the labels characterizing the death
     * causes.
     */
    private void die(int dead) {

        this.dead = dead;
        living = false;
        setLLD(Double.NaN, Double.NaN, Double.NaN);
        length = temperature = salinity = Double.NaN;
    }

    /**
     * Makes the particle going on through time. It is the core method of the
     * Individual based model. The method calls all the processes the particle
     * expresses in a time step: transport, growth, cold lethal temperature,
     * age control.
     *
     * @param time a double, the current time of the simulation.
     * @see core.Population#step() to understand the time ongoing movement of
     * the Individual based model.
     */
    void step(double time) {

        if (age <= Simulation.getTransportDuration()) {

            if (Configuration.isStopMoving() && isRecruited()) {
                return;
            }

            try {
                move(time);

                if (FLAG_GROWTH && living) {
                    grow(time);
                } else if (FLAG_LETHAL_TP && living) {
                    checkTemperature(time);
                }

                if (FLAG_DISPLAY_TP && living) {
                    getTemperature(time);
                }

                if (FLAG_RECRUITMENT && living) {
                    checkRecruitment(Configuration.getTypeRecruitment());
                }

            } catch (ArrayIndexOutOfBoundsException e) {
                printErr(e);
                die(Constant.ERROR);
            }

            age += Configuration.get_dt();
        } else {
            die(Constant.DEAD_OLD);
        }
    }

    /**
     * Simulates the vertical migration of larvae. It first checks wether the
     * particles reached the larva stage with a test baed on age or length, and
     * then applies the appropiate migration pattern: diel vertical migration
     * or isodepth transport (wich is an particular case of DVM with identical
     * daytime and nightime depths.
     *
     * @param time a double, the current time [second] of the simulation.
     * @see ichthyop.bio.DVMPattern for details about the Diel Vertical
     * Migration.
     */
    private void migrate(double time) {

        /** Ensures larva stage */
        boolean isLarva = FLAG_GROWTH
                ? length > GrowthModel.HATCH_LENGTH
                : age > Configuration.getMigrationAgeLimit();

        if (isLarva) {
            double depth = 0.d;
            if (FLAG_ISODEPTH) {
                /** isodepth migration */
                depth = Simulation.getDepthDay();
            } else {
                /** diel vertical migration */
                depth = DVMPattern.getDepth(getX(), getY(), time, data);
            }
            setZ(Dataset.depth2z(getX(), getY(), depth));
        }
    }

    /**
     * Simulates egg buoyancy. It first ensures the particle is at egg stage
     * and then calculates the vertical motion due to buoyancy.
     *
     * @param time a double, the current time [second] of the simulation
     * @see ichthyop.bio.BuoyancyScheme for details about the buoyancy scheme.
     *
     */
    private void addBuoyancy(double time) {

        /** Ensures egg stage */
        if (FLAG_GROWTH
                ? length < GrowthModel.HATCH_LENGTH
                : age < BuoyancyScheme.age_lim_buoy) {
            /** update geog coordinates */
            grid2Geog();
            salinity = data.getSalinity(getPGrid(), time);
            temperature = data.getTemperature(getPGrid(), time);
            setZ(Dataset.depth2z(getX(), getY(),
                    getDepth() +
                    BuoyancyScheme.move(salinity, temperature)));
        }
    }

    /**
     * Makes the particle grow in length throughout time. By default, it
     * simulates the growth in length of particles as a function of water
     * temperature. It also makes egg/larva die when moving through
     * cold water if the lethal temperature option is activated.
     * <p>The growth model alos allows considering growth of feeding larvae
     * as functions of both temperature and prey availability. This function
     * requires prey concentration fields in the input model. Outputs of
     * physical-biogeochemical coupled models like "ROMS-BIO" (Koné et al. 2005)
     * provide such fields.</p>
     *
     * @param time a double, the current time [second] of the simulation.
     * @see ichthyop.bio.GrowthModel for details about the growth model.
     */
    private void grow(double time) {

        /** growth as function of temperature and prey availability */
        if (FLAG_PLANKTON) {
            double[] plankton = data.getPlankton(getPGrid(), time);
            length = GrowthModel.grow(length,
                    temperature = data.getTemperature(
                    getPGrid(), time),
                    largePhyto = plankton[0],
                    smallZoo = plankton[1],
                    largeZoo = plankton[2]);
        } else {
            /** growth as function of temperature only */
            length = GrowthModel.grow(length,
                    temperature = data.getTemperature(
                    getPGrid(),
                    time));
        }

        /** checks for lethal water temperature */
        if (FLAG_LETHAL_TP && GrowthModel.isDeadCold()) {
            die((GrowthModel.getStage(length) > 0)
                    ? Constant.DEAD_COLD_LARVE
                    : Constant.DEAD_COLD);
        }
    }

    /**
     * Checks whether particle is moving through lethal water temperature.
     * Water temperature threshold is set by the user.
     *
     * @param time a double, the current time [second] of the simulation.
     */
    private void checkTemperature(double time) {

        temperature = data.getTemperature(getPGrid(), time);
        if (temperature < lethal_tp) {
            die(Constant.DEAD_COLD);
        }
    }

    /**
     * Determines whether the particle is recruited, according to the specified
     * recruitment criteria.
     * <p>
     * Ichthyplankton is considered as recruited when it has stayed within
     * a specified area for a certain amount of time and satisfied a condition
     * of minimal length or age. The idea behind these criteria is that the
     * zones correspond to favourable areas for larval survival
     * (e.g., good feeding conditions), and that the minimal length or age
     * correspond to larvae that have enough swimming abilities to retain
     * themselves within these areas.
     * </p>
     *
     * @param typeRecruit, and integer characterizing the type of recruitmen:
     * based on age criteria or on length criteria.
     * @return <code>true</code> if the particle is recruited,
     * <code>false</code> otherwise.
     */
    public boolean checkRecruitment(int typeRecruit) {

        /** get current zone */
        numCurrentZone = getNumZone(Constant.RECRUITMENT);
        if ((numCurrentZone != -1) && !isRecruited[numCurrentZone]) {
            boolean satisfyCriterion = false;
            switch (typeRecruit) {
                /** age criterion */
                case Constant.RECRUIT_AGE:
                    satisfyCriterion = (float) (age / Constant.ONE_DAY) >=
                            Simulation.getAgeMinAtRecruitment();
                    break;
                /** length criterion */
                case Constant.RECRUIT_LENGTH:
                    satisfyCriterion =
                            (length >= Simulation.getLengthMinAtRecruitment());
                    break;
                /** no recruitment */
                case Constant.NONE:
                    return (isRecruited[numCurrentZone] = false);
            }
            if (Configuration.isDepthRecruitment()) {
                satisfyCriterion = satisfyCriterion &&
                        (Math.abs(getDepth()) <= Simulation.getMaxDepthRecruitment() && Math.abs(getDepth()) >= Simulation.getMinDepthRecruitment());
            }
            if (satisfyCriterion) {
                timeInZone = (numRecruitZone == numCurrentZone)
                        ? timeInZone + MainFrame.getDtDisplay()
                        : 0;
                numRecruitZone = numCurrentZone;
                isNewRecruited = (timeInZone >= durationMinInRecruitArea);
                return (isRecruited[numCurrentZone] = isNewRecruited);
            }
        }
        return false;
    }

    /**
     * Checks whether the particle has been recruited in the specified zone.
     *
     * @param num_zone the index of the release zone
     * @return <code>true</code> if the particle has been recruited in the
     * specified zone, <code>false</code> otherwise.
     */
    public boolean isRecruited(int num_zone) {

        return isRecruited[num_zone];
    }

    public boolean isRecruited() {

        for (boolean recruited : isRecruited) {
            if (recruited) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prints an error message in the console, relative to the specified
     * Throwable object.
     *
     * @param t a Throwable, the exception thrown by the application.
     */
    private void printErr(Throwable t) {

        StackTraceElement[] stackTrace = t.getStackTrace();
        StringBuffer message = new StringBuffer(t.getClass().getSimpleName());
        message.append(" : ");
        message.append(stackTrace[0].toString());
        message.append('\n');
        message.append("  --> ");
        message.append(t.getMessage());
        System.err.println(message.toString());
    }

//////////
// Getters
//////////
    /**
     * Gets the the number of the zone in wich the particle has been released.
     * @return the index of the release zone.
     */
    public int getNumZoneInit() {
        return numReleaseZone;
    }

    /**
     * Gets the number of the last zone in wich the particle has been recruited.
     *
     * @return the index of the recruitment zone.
     */
    public int getNumRecruitZone() {
        return numRecruitZone;
    }

    /**
     * Determines whether the particle is still alive.
     *
     * @return <code>true</code> if the particle is living,
     *         <code>false</code> otherwise
     */
    public boolean isLiving() {
        return living;
    }

    /**
     * Determines whether the particle is dead old.
     * @return <code>true</code> if the particle is dead old,
     *         <code>false</code> otherwise
     */
    public boolean isOld() {
        return dead == Constant.DEAD_OLD;
    }

    /**
     * Determines whether the particle is dead cold.
     * @return <code>true</code> if the particle is dead cold,
     *         <code>false</code> otherwise
     */
    public boolean isDeadCold() {

        return (dead == Constant.DEAD_COLD ||
                dead == Constant.DEAD_COLD_LARVE);
    }

    /**
     * Gets the cause of depth.
     *
     * @return an integer characterizing the causes of death.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the causes of death.
     */
    public int getDeath() {
        return dead;
    }

    /**
     * Gets the length of the particle.
     *
     * @return a double, the length [millimeter] of the particle.
     */
    public double getLength() {
        return length;
    }

    /**
     * Computes sea water temperature at particle location for the specified
     * time.
     *
     * @param time a double, the current time [second] of the simulation
     * @return a double, the see water temperature [celsius] at particule
     * location
     */
    public double getTemperature(double time) {

        if (isLiving() && !(FLAG_GROWTH || FLAG_LETHAL_TP)) {
            temperature = data.getTemperature(getPGrid(), time);
        }
        return temperature;
    }

    /**
     * Gets sea water temperature at particle location
     * @return a double, the see water temperature [celsius] at particule
     * location
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Computes sea water salinity at particle location for the specified
     * time.
     *
     * @param time a double, the current time [second] of the simulation
     * @return a double, the see water salinity [psu] at particule location
     */
    public double getSalinity(double time) {

        if (isLiving()) {
            salinity = data.getSalinity(getPGrid(), time);
        }
        return salinity;
    }

    /**
     * Gets small zooplankton concentration at particle location.
     *
     * @return a double, the small zooplankton concentration [mMol/m3] at
     * particle location
     */
    public double getSmallZoo() {
        return smallZoo;
    }

    /**
     * Gets large zooplankton concentration at particle location.
     *
     * @return a double, the large zooplankton concentration [mMol/m3] at
     * particle location
     */
    public double getLargeZoo() {
        return largeZoo;
    }

    /**
     * Gets small zooplankton concentration at particle location.
     *
     * @return a double, the large phytoplankton concentration [mMol/m3] at
     * particle location
     */
    public double getLargePhyto() {
        return largePhyto;
    }

    /**
     * Gets the particle index
     *
     * @return an int, the particle index in the Population
     */
    public int index() {
        return index;
    }

    /**
     * Determines whether a particle is newly recruited.
     *
     * @return {@code true} if the particle is newly recruited,
     *         {@code false} otherwise.
     */
    public boolean isNewRecruited() {
        return isNewRecruited;
    }

    /**
     *  Sets the newly recruited status of the particle to false.
     */
    public void resetNewRecruited() {
        isNewRecruited = false;
    }
    //---------- End of class
}
