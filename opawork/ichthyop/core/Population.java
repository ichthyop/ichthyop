package ichthyop.core;

/** import java.io */
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

/** import java.text */
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/** import java.util */
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Calendar;

/** import from project */
import ichthyop.bio.DVMPattern;
import ichthyop.io.Dataset;
import ichthyop.io.Configuration;
import ichthyop.util.Constant;
import ichthyop.util.calendar.ClimatoCalendar;
import ichthyop.util.calendar.Calendar1900;
import ichthyop.util.IParticle;

/** import netcdf */
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * <p> The Population is the intermediate level of the hierarchy of the IBM:
 * Simulation > Population > Individual (Particle). In accordance
 * with the name, this class manages a collection of particles. It is indeed
 * one of the core classes of the IBM (Simulation, Population and Particle).
 * The access to the particles always goes through this class.</p>
 *
 * <p>The class extends a <code>HasSet</code> that contains Particle objects</p>
 *
 * <p>In terms of proprer methods, this class mainly controls the release of
 * the particles. It also relays the <code>step</code> function to every
 * particle. The <code>step</code> function appears in the 3 core classes
 * (see above) of the model. It handles the march of the model through time.</p>
 *
 * @see {@link java.util.HashSet} for more details about the HashSet class.
 *
 * @author P.Verley
 */
public class Population extends HashSet {


////////////////
// Debug purpose
////////////////

    private final static boolean DEBUG_PULSATION = false;
    private final static boolean DEBUG_PATCHINESS = false;
    private final static boolean DEBUG_RELEASE_ZONE = false;
    private final static boolean DEBUG_RELEASE_TXT = false;
    private final static boolean DEBUG_RELEASE_NC = false;

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /** Stores time of the release events */
    private long[] timeEvent;

    /** Index of the current release event */
    private int indexEvent;

    /** Set to true once all released events happened */
    private boolean isAllReleased;

    /** Number of particles currently alive */
    private int nbAlive;

    /** Index of the last particle added to the collection */
    private int index;

///////////////
// Constructors
///////////////

    /**
     * Constructs a new, empty set; the backing <tt>HashSet</tt> instance has
     * default initial capacity the number of particles
     */
    public Population() {
        super(Configuration.getNbParticles());
    }

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Initializes the variables and calls the method to determine the release
     * schedule.
     */
    void init() {

        indexEvent = 0;
        isAllReleased = false;
        nbAlive = 0;
        index = 0;

        schedule();
    }

    /**
     * The <code>step</code> function is the main fonction of the model.
     * It is responsible for the march of the model through time.
     * Simulation.step() calls for Population.step() that in turn calls for
     * Particle.step() where everything happens since the program is
     * "individual based".
     * It calls the <code>step</code> method for each living particle at
     * current time.
     *
     * @param time a long number, current time of the simulation [seconds]
     * @throws IOException if an input or output exception occured when
     * updating the data to current time.
     * @todo something should be done if all the particles are dead.
     */
    void step(long time) {

        Iterator<Particle> iter = iterator();
        double timeD = (double) time;
        nbAlive = 0;
        Particle particle;
        while (iter.hasNext()) {
            particle = iter.next();
            if (particle.isLiving()) {
                nbAlive++;
                particle.step(timeD);
            }
        }
        // What to do if nb_alive == 0 ? ...throw any Exception ?
    }

    /**
     * Initiliazes the step. It updates {@code Dataset} to current time and
     * checks whether or not some particles should be released.
     *
     * @param time a long number, current time of the simulation [seconds]
     * @throws IOException if an input exception occured when
     * updating the data to current time or releasing the particles.
     */
    void iniStep(long time) throws IOException {

        Particle.data.setAllFieldsAtTime(time);

        if (!isAllReleased) {
            release(time);
        }
    }

    /**
     * Determines the release schedule. Basically, a release event is the
     * time in second since the origin of time (read in the configuration file).
     * By default the time of release is set to t0. If PULSATION is active
     * (multiple releasing event), times of release are determined functions of
     * the number of release events and the time step between two
     * release events.
     */
    private void schedule() {

        timeEvent = new long[
                    (Configuration.getTypeRelease() == Constant.RELEASE_ZONE)
                    ? Simulation.getNbReleaseEvents()
                    : 1];
        for (int i = 0; i < Simulation.getNbReleaseEvents(); i++) {
            timeEvent[i] = Simulation.get_t0() +
                           (long) (i * Simulation.getReleaseDt());
        }

        if (DEBUG_PULSATION) {
            Calendar calendar = Configuration.getTypeCalendar() ==
                                Constant.CLIMATO
                                ? new ClimatoCalendar()
                                :
                                new Calendar1900(Configuration.getTimeOrigin(
                                        Calendar.YEAR),
                                                 Configuration.getTimeOrigin(
                    Calendar.MONTH),
                                                 Configuration.getTimeOrigin(
                    Calendar.DAY_OF_MONTH));
            SimpleDateFormat dateformat = new SimpleDateFormat(
                    "yyyy/MM/dd HH:mm");
            System.out.println(
                    "  ++++Debug Population - Schedule of release events");
            System.out.println("  ++++Number of release events: " +
                               Simulation.getNbReleaseEvents());
            for (int i = 0; i < timeEvent.length; i++) {
                calendar.setTimeInMillis(timeEvent[i] * 1000L);
                System.out.println("  ++++Event " + (i + 1) + " at " +
                                   dateformat.format(calendar.getTime()));
            }
            System.out.println(
                    "  ++++End debug Population - Schedule of release events");
        }
    }

    /**
     * Every step of the simulations, while <code>isAllReleased</code> is
     * <code>false</code> the program calls this release method. It is a
     * generic method that dispatch the release order to the specific release
     * methods : release from a Zone or release from text file or release
     * from NetCDF file.
     *
     * @param time a long number, current time of the simulation [second]
     * @throws IOException if an input or output exception occured when
     * executing the <code>releaseTxtFile</code> or the
     * <code>releaseNcFile</code> methods.
     * @throws NullPointerException if no release zone have been defined
     * when executing the <code>releaseZone</code> method.
     */
    private void release(long time) throws IOException, NullPointerException {

        switch (Configuration.getTypeRelease()) {
        case Constant.RELEASE_ZONE:
            releaseZone(time);
            break;
        case Constant.RELEASE_TXTFILE:
            releaseTxtFile(Configuration.getDrifterFile(), time);
            break;
        case Constant.RELEASE_NCFILE:
            releaseNcFile(Configuration.getDrifterFile(), time);
            break;
        }
    }

    /**
     Random releases particles within pre-defined geographical zones.
     * The implementation handles several cases:
     * <ul>
     * <li> single release at the begining of the simulation.
     * <li> multiple release events throughout the simulation (PULSATION).
     * <li> patchy initial spatial distribution for particles (PATCHINESS).
     * <li> 2D or 3D particles
     * </ul>
     *
     * @see {@link ichthyop.Zone} for a thorough description of a Zone.
     * @param time a long number, current time of the simulation [second]
     * @throws NullPointerException if no release zone have been defined.
     */
    private synchronized void releaseZone(long time) throws
            NullPointerException {

        /** Local variable declaration */
        int nbReleasedNow, nbReleased, nbReleaseZones, nbAgregatedIndiv,
                nbInPatch = 0, i_part;
        double radius_patch = 0.d, thickness_patch = 0.d;
        double radius_grid, r, teta;
        double xmin, xmax, ymin, ymax;
        double depthMin = 0.d, depthMax = 0.d;
        Zone zone;
        Particle particle, particlePatch;
        boolean bln3D = Configuration.is3D();

        /** Initilization of the parameters */
        nbReleaseZones = Configuration.getReleaseZones().size();
        if (nbReleaseZones == 0) {
            throw new NullPointerException("No release zone defined.");
        }

        nbReleasedNow = Configuration.isPulsation()
                        ? Configuration.getNbParticles() /
                        Simulation.getNbReleaseEvents()
                        : Configuration.getNbParticles();
        int mod = Configuration.getNbParticles() %
                  Simulation.getNbReleaseEvents();
        nbReleasedNow += (indexEvent < mod) ? 1 : 0;

        nbReleased = nbReleasedNow;
        if (Configuration.isPatchiness()) {
            nbInPatch = Math.max(0,
                                 nbReleasedNow / Configuration.getNbPatches() -
                                 1);
            nbReleased = Configuration.getNbPatches();
            radius_patch = Simulation.getRadiusPatchi();
            thickness_patch = Simulation.getThickPatchi();
        }

        /** Determines the range in depth */
        if (bln3D) {
            depthMin = Simulation.getDepthReleaseMin();
            depthMax = Simulation.getDepthReleaseMax();
            if (Configuration.isMigration()
                && Configuration.getMigrationAgeLimit() == 0) {
                depthMin = depthMax = (DVMPattern.isDaytime(time))
                                      ? Simulation.getDepthDay()
                                      : Simulation.getDepthNight();
            }
        }

        /** Reduces the release area function of the user-defined zones */
        xmin = Double.MAX_VALUE;
        ymin = Double.MAX_VALUE;
        xmax = 0.d;
        ymax = 0.d;
        for (int i_zone = 0; i_zone < nbReleaseZones; i_zone++) {
            zone = (Zone) Configuration.getReleaseZones().get(i_zone);
            xmin = Math.min(xmin, zone.getXmin());
            xmax = Math.max(xmax, zone.getXmax());
            ymin = Math.min(ymin, zone.getYmin());
            ymax = Math.max(ymax, zone.getYmax());
        }

        if (Dataset.DEBUG_VDISP) {
            depthMin = depthMax = 0.5d *
                                  Dataset.getDepth(0);

        }

        /** Release process */
        while (!isAllReleased && timeEvent[indexEvent] >= time &&
               timeEvent[indexEvent] < (time + Configuration.get_dt())) {

            if (DEBUG_PATCHINESS && Configuration.isPatchiness()) {
                System.out.println("  ++++Debug Population - Patchiness");
                System.out.println("  ++++Release event " + (indexEvent + 1) +
                                   "/" +
                                   timeEvent.length);
                System.out.println("  ++++Number patches: " +
                                   Configuration.getNbPatches()
                                   + "  radius: " + radius_patch +
                                   " thickness: " + thickness_patch
                                   + " number particles per patch: "
                                   + (nbInPatch + 1));
            }

            i_part = 0;
            /** Loop on the number of particle to be released */
            for (int p = 0; p < nbReleased; p++) {
                /** Instantiate a new Particle */
                particle = new Particle(index,
                                        bln3D,
                                        xmin, xmax, ymin, ymax,
                                        depthMin, depthMax);
                add(particle);
                nbAlive++;
                index++;
                /** Make a patch of Particles */
                if (Configuration.isPatchiness()) {
                    nbAgregatedIndiv = nbInPatch +
                                       (i_part <
                                        (nbReleasedNow %
                                         Configuration.getNbPatches()) ?
                                        1 : 0);
                    radius_grid = Particle.data.adimensionalize(
                            radius_patch, particle.getX(), particle.getY());
                    for (int f = 0; f < nbAgregatedIndiv; f++) {
                        r = radius_grid * Math.random();
                        teta = 2.0f * Math.PI * Math.random();
                        double depth = 0.d;
                        if (bln3D) {
                            depth = particle.getDepth() +
                                    thickness_patch * (Math.random() - 0.5f);
                        }
                        particlePatch = new Particle(index,
                                bln3D,
                                particle.getNumZoneInit(),
                                particle.getX() + r * Math.cos(teta),
                                particle.getY() + r * Math.sin(teta),
                                depth);
                        add(particlePatch);
                        nbAlive++;
                        index++;
                    }

                    if (DEBUG_PATCHINESS) {
                        System.out.println("  ++++Patch " + i_part +
                                           " number particles: " +
                                           (nbAgregatedIndiv + 1));
                    }
                }
                i_part++;
            }

            if (DEBUG_PATCHINESS && Configuration.isPatchiness()) {
                System.out.println("  ++++Total released at this time: " +
                                   nbAlive);
                System.out.println("  ++++End debug Population - Patchiness");
            }

            if (DEBUG_PULSATION) {
                System.out.println("  ++++Debug Population - Pulsation");
                System.out.println("  ++++Release event " + (indexEvent + 1) +
                                   "/" + timeEvent.length);
                System.out.println("  ++++Number to be released now: " +
                                   nbReleasedNow);
                System.out.println("  ++++Total released at this time: " +
                                   index + "/" +
                                   Configuration.getNbParticles());
                System.out.println("  ++++End debug Population - Pulsation");
            }

            indexEvent++;
            System.out.println("Release event " + indexEvent + " [done]");
            isAllReleased = indexEvent >= timeEvent.length;
        }

    }

    /**
     * <p>Releases particle from initial coordinates stored in text file *.drf.
     * The method is only called once at the begining of the simulation:
     * the boolean flag <code>isAllReleased</code> is set to
     * <code>true</code> at the end of the method.</p>
     * The drifter file should have one line per particle, first column is
     * longitude, second column is latitude and third column is depth. Comments
     * are preceded by a hash sign # at the beginning of the line. Empty lines
     * (only containing a return caracter) are ignored. Below is reproduced the
     * content of a standard drifter file:
     * <pre>
     * #%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
     * # Drifter initial coordinates
     * #%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
     * # Adapted to <i>name of the configuration</i>
     * # one line = one drifter
     * # longitude latitude depth
     * 16 34 10
     * 16 34 50
     * 17 34 10
     * 17 34 50
     * 16 35 10
     * 16 35 50
     * # ...
     * # as much as wanted
     * </pre>
     *
     * @param pathname a String, indicates the full pathname of the drifter file
     * that stores the initial coordinates.
     * @throws IOException if an input or output exception ocurred when
     * reading the file.
     */
    private void releaseTxtFile(String pathname, double time) throws
            IOException {

        File fDrifter = new File(pathname);
        if (!fDrifter.exists() || !fDrifter.canRead()) {
            throw new IOException("Drifter file " + fDrifter +
                                  " cannot be read");
        }

        String[] strCoord;
        double[] coord;
        NumberFormat nbFormat = NumberFormat.getInstance(Locale.US);
        Particle particle;
        boolean bln3D = Configuration.is3D();
        double depth = 0.d;
        try {
            BufferedReader bfIn = new BufferedReader(new FileReader(fDrifter));
            String line;
            while ((line = bfIn.readLine()) != null) {
                if (!line.startsWith("#") & !(line.length() < 1)) {
                    strCoord = line.split(" ");
                    coord = new double[strCoord.length];
                    for (int i = 0; i < strCoord.length; i++) {
                        try {
                            coord[i] = nbFormat.parse(strCoord[i].trim()).
                                       doubleValue();
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (bln3D) {
                        depth = -coord[2];
                        if (Configuration.isMigration()
                            && Configuration.getMigrationAgeLimit() == 0) {
                            depth = (DVMPattern.isDaytime(time))
                                    ? -1.f * Simulation.getDepthDay()
                                    : -1.f * Simulation.getDepthNight();
                        }
                    }
                    particle = new Particle(index,
                                            bln3D,
                                            coord[0], coord[1], depth,
                                            true);
                    if (Dataset.isInWater(particle)) {
                        add(particle);
                        index++;
                    } else {
                        throw new IOException("Drifter at line " + (index + 1)
                                + "is not in water");
                    }
                }
            }
        } catch (java.io.IOException e) {
            throw new IOException("Problem reading drifter file " + fDrifter);
        }

        nbAlive = size();
        isAllReleased = true;

        /** DEBUG */
        if (DEBUG_RELEASE_TXT) {
            System.out.println("  ++++Debug release from text file");
            System.out.println("  ++++Text file: " + fDrifter.toString());
            Iterator<IParticle> iter = iterator();
            int i = 0;
            while (iter.hasNext()) {
                IParticle p = iter.next();
                i++;
                System.out.println("  ++++Particle " + i + "/" + size()
                                   + " lon: " + (float) p.getLon()
                                   + " lat: " + (float) p.getLat()
                                   + " depth: " + (float) p.getDepth());
            }
            System.out.println("  ++++End debug release from text file");
        }
    }

    /**
     * Releases particle from initial coordinates stored in the model
     * output file (NetCDF format). If the <code>time</code> parameter
     * matches with one of the values stored in the time variable of the file,
     * the release method just reads the longitude, latitude and depth
     * (only for 3D particles) at the rank corresponding to the
     * <code>time</code> value.
     * On the contrary, if the <code>time</code> parameter is in between
     * two records of the file, the release method interpolates the geographical
     * coordinates between the two ranks corresponding to the surrounding time
     * values.
     *
     * @param pathname a String, full pathname of the NetCDF file.
     * @param time a double number, current time of the simulation [second]
     * @throws IOException if problem reading file or if the current time
     * is not contained within the range of time of the file.
     */
    private void releaseNcFile(String pathname, double time) throws IOException {

        try {
            NetcdfFile nc = NetcdfDataset.openFile(pathname, null);
            ArrayDouble.D1 timeArr = (ArrayDouble.D1) nc.findVariable("time").
                                     read();
            int rank = 0;
            int length = timeArr.getShape()[0];

            /** Find current rank */
            double time_rank0 = timeArr.get(0), time_rank1;
            while (rank < length) {
                rank++;
                time_rank1 = timeArr.get(rank);
                if ((time >= time_rank0 && time < time_rank1)
                   || (time <= time_rank0 && time > time_rank1)) {
                   break;
                }
                if (time == time_rank1) {
                    rank++;
                    time_rank0 = time_rank1;
                    break;
                }
                time_rank0 = time_rank1;
            }
            rank--;

            double x = 0.f;
            if (time != time_rank0) {
                x = (time - timeArr.get(rank))
                    / (timeArr.get(rank + 1) - timeArr.get(rank));
            }

            ArrayFloat.D2
                    lonArr = (ArrayFloat.D2) nc.findVariable("lon").read(),
                             latArr = (ArrayFloat.D2) nc.findVariable("lat").
                                      read(),
                                      depthArr = (ArrayFloat.D2) nc.
                                                 findVariable("depth").read();
            ArrayInt.D2
                    deathArr = (ArrayInt.D2) nc.findVariable("death").read();
            double lon, lat, depth = 0.d;
            Particle particle;
            boolean bln3D = Configuration.is3D();
            int nb_particles = Configuration.getNbParticles();

            boolean migration = false;
            double constantDepth = 0.d;
            if (Configuration.isMigration()
                && Configuration.getMigrationAgeLimit() == 0) {
                migration = true;
                constantDepth = (DVMPattern.isDaytime(time))
                                ? -1.f * Simulation.getDepthDay()
                                : -1.f * Simulation.getDepthNight();
            }

            boolean living;
            for (int i = 0; i < nb_particles; i++) {

                if (x == 0.f) {
                    lon = lonArr.get(rank, i);
                    lat = latArr.get(rank, i);
                    depth = depthArr.get(rank, i);
                } else {
                    lon = x * lonArr.get(rank + 1, i)
                          + (1 - x) * lonArr.get(rank, i);
                    lat = x * latArr.get(rank + 1, i) +
                          (1 - x) * latArr.get(rank, i);
                    depth = x * depthArr.get(rank + 1, i) +
                            (1 - x) * depthArr.get(rank, i);
                }

                if (migration) {
                    depth = constantDepth;
                }

                living = (deathArr.get(rank, i) == Constant.DEAD_NOT);
                particle = new Particle(i,
                                        bln3D,
                                        lon, lat, depth,
                                        living);
                add(particle);
                if (living) nbAlive++;
            }

            lonArr = null;
            latArr = null;
            depthArr = null;
            deathArr = null;
            nc.close();
        } catch (IOException e) {
            throw new IOException("Problem opening file " + pathname + " : "
                                  + e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem reading variables in file "
                                  + pathname + " : " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            throw new IOException("Time value " + (long) time
                                  +
                                  " not contained within Ichthyop output file "
                                  + pathname);
        }
        isAllReleased = true;
    }

    /**
     * Gets the number of particles currently alive
     *
     * @return the number of alive particles.
     */
    public int getNbAlive() {
        return nbAlive;
    }

    //------- End of class
}
