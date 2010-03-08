package ichthyop.core;

/** import java.io */
import java.io.IOException;

/** import java.util */
import java.util.Iterator;

/** local import */
import ichthyop.io.Configuration;
import ichthyop.io.DatasetM2D;
import ichthyop.io.DatasetM3D;
import ichthyop.io.DatasetR2D;
import ichthyop.io.DatasetR3D;
import ichthyop.util.SerialParameter;
import ichthyop.util.Constant;
import ichthyop.bio.BuoyancyScheme;
import ichthyop.bio.GrowthModel;
import ichthyop.bio.DVMPattern;
import ichthyop.io.DatasetGHER3D;

/**
 * <p> Individual-based models (later on IBM) are simulations based on the
 * global consequences of local interactions of members of a population.
 * This class is the top level of the hierarchy in the IBM. It controls the set
 * of parameters and the intermediate level of the IBM, the Population (that
 * manages thereafter the last level of the hierarchy, the Particles).</p>
 * The class does gather the values of the required parameters when
 * initializing, provide methods to acces theses values (getters) and make
 * the Population step through time.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see ichthyop.core.Population
 * @see ichthyop.core.Particle
 */
public class Simulation {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Duration of simulation [second].
     * <code>duration of simulation = duration of transport + duration
     * of release</code> to ensure that the last particle released will be
     * transported for a time equals to duration of transport.
     */
    private static long simulationDuration;
    /**
     * Begining of the simulation [second] since origin.
     */
    private static long t0;
    /**
     * Duration [second] of transport of particle
     */
    private static long transportDuration;
    /**
     * Age [day] criterion for recruitment
     */
    private static float ageMinAtRecruitment;
    /**
     * Length [millimeter] criterion for recruitment
     */
    private static float lengthMinAtRecruitment;
    /**
     * Minimum depth [meter] for recruitment
     */
    private static float topDepthRecruitment;
    /**
     * Maximum depth [meter] for recruitment
     */
    private static float bottomDepthRecruitment;
    /**
     * Minimum depth of release [meter].
     */
    private static float depthReleaseMin;
    /**
     * Maximum depth of release [meter].
     */
    private static float depthReleaseMax;
    /**
     * Lethal sea water temperature [celsius] for all particles when growth is
     * not simulated, or for particle at egg stage only when growth is on.
     */
    private static float lethalTpEgg;
    /**
     * Lethal sea water temperature [celsius] for particle at larva stage, only
     * when growth is simulated.
     */
    private static float lethalTpLarvae;
    /**
     * Egg density when BUOYANCY is simulated [g/cm3]
     */
    private static float eggDensity;
    /**
     * Radius of a patch [meter] when PATCHINESS is on.
     */
    private static float patchRadius;
    /**
     * Thickness of a patch [meter] when PATCHINESS is on.
     */
    private static float patchThickness;
    /**
     * Number of release events when PULSATION is on.
     */
    private static int nbReleaseEvents;
    /**
     * Time step [second] between two release events when PULSATION is on.
     */
    private static int dtRelease;
    /**
     * Depth [meter] of the particle at daytime when DIEL VERTICAL
     * MIGRATION is simulated.
     */
    private static float depthDay;
    /**
     * Depth [meter] of the particle at nighttime when DIEL VERTICAL
     * MIGRATION is simulated.
     */
    private static float depthNight;
    /**
     * Associated population of particles.
     */
    private Population population;
    /**
     * For SERIAL mode only. Index of the current replica.
     */
    private static int replica;

////////////////////////////
// Definition of the methods
////////////////////////////
    /**
     * Sets up the simulation once a file of configuration has been loaded.
     * It constructs the <code>Dataset</code> object, according to the model
     * options (ROMS/MARS, 2D/3D) and sets it up.
     *
     * @see ichthyop.io.Dataset#setUp for details about the preliminary
     * computation.
     */
    public synchronized void setUp() throws Exception {

        System.out.println("Preliminary computation");

        switch (Configuration.getTypeModel() + Configuration.getDimSimu()) {
            case (Constant.ROMS + Constant.SIMU_2D):
                Particle.data = new DatasetR2D();
                break;
            case (Constant.ROMS + Constant.SIMU_3D):
                Particle.data = new DatasetR3D();
                break;
            case (Constant.MARS + Constant.SIMU_2D):
                Particle.data = new DatasetM2D();
                break;
            case (Constant.MARS + Constant.SIMU_3D):
                Particle.data = new DatasetM3D();
                break;
            case (Constant.GHER + Constant.SIMU_3D):
                Particle.data = new DatasetGHER3D();
        }

        Particle.data.setUp();
    }

    /**
     * Initializes the run. First gets the values of the required parameters
     * from <code>ichthyop.io.Configuration</code>, then constructs a new
     * <code>Population</code> and initializes it.
     *
     * @see ichthyop.core.Population#init() for details about initialization of
     * the <code>Population</code> object.
     * @see ichthyop.core.Particle#init() for details about initialization of
     * the <code>Particle</code> object.
     */
    public void init() throws Exception {

        transportDuration = Configuration.getTransportDuration();
        t0 = Configuration.get_t0(SerialParameter.TO.index());

        replica = SerialParameter.REPLICA.index();
        dtRelease = Configuration.getReleaseDt(SerialParameter.PULSATION.index());
        nbReleaseEvents = Configuration.getNbReleaseEvents(SerialParameter.PULSATION.index());

        if (Configuration.getTypeRelease() == Constant.RELEASE_ZONE) {
            depthReleaseMin = -1.f * Configuration.getDepthReleaseMin(
                    SerialParameter.RELASE_DEPTH.index());
            depthReleaseMax = -1.f * Configuration.getDepthReleaseMax(
                    SerialParameter.RELASE_DEPTH.index());
            if (Configuration.isPatchiness()) {
                patchRadius = Configuration.getRadiusPatchi(SerialParameter.PATCHINESS.index());
                patchThickness = Configuration.getThickPatchi(
                        SerialParameter.PATCHINESS.index());
            }
        }

        if (Configuration.isLethalTp()) {
            lethalTpEgg = Configuration.getLethalTpEgg(SerialParameter.LETHAL_TP_EGG.index());
            lethalTpLarvae = Configuration.getLethalTpLarvae(
                    SerialParameter.LETHAL_TP_LARVAE.index());
        }

        if (Configuration.isGrowth()) {
            GrowthModel.init();
        }

        if (Configuration.isBuoyancy()) {
            eggDensity = Configuration.getEggDensity(SerialParameter.BUOYANCY.index());
            BuoyancyScheme.init();
        }

        if (Configuration.getTypeRecruitment() == Constant.RECRUIT_LENGTH) {
            lengthMinAtRecruitment = Configuration.getLengthRecruitment(
                    SerialParameter.RECRUIT_LENGTH.index());
        } else if (Configuration.getTypeRecruitment()
                == Constant.RECRUIT_AGE) {
            ageMinAtRecruitment = Configuration.getAgeRecruitment(
                    SerialParameter.RECRUIT_AGE.index());
        }

        if (Configuration.isDepthRecruitment()) {
            topDepthRecruitment = Configuration.getMinDepthRecruitment(
                    SerialParameter.RECRUIT_DEPTH.index());
            bottomDepthRecruitment = Configuration.getMaxDepthRecruitment(
                    SerialParameter.RECRUIT_DEPTH.index());
        }

        if (Configuration.isMigration()) {
            depthDay = -1.f
                    * Configuration.getDepthDay(SerialParameter.DVM.index());
            depthNight = -1.f
                    * Configuration.getDepthNight(SerialParameter.DVM.index());
            DVMPattern.init();
        }

        initZone();
        population = new Population();
        population.init();
        Particle.data.init();
    }

    /**
     * Makes the simulation step through time by a call to the
     * <code>Population.step()</code> method. It is the main fonction of the
     * model. sincei t is responsible for the march of the model through time.
     * Simulation.step() calls for Population.step() that in turn calls for
     * Particle.step() where everything happens since the program is
     * "individual based".
     *
     * @param time a long, the current time [second] of the simulation.
     * @see ichthyop.core.Population#step
     */
    public void step(long time) throws IOException,
            NullPointerException {

        population.step(time);
    }

    /**
     * Initializes the current step.
     *
     * @param time a long, the current time [second] of the simulation.
     * @throws IOException if an error occurs when initializing the step.
     * @see ichthyop.core.Population#iniStep
     */
    public void iniStep(long time) throws IOException {

        population.iniStep(time);
    }

    /**
     * For SERIAL mode only. Displays the current set of parameters in the
     * console.
     *
     * @param step the current Step of the simulation
     */
    public void printParameters(Step step) {

        StringBuffer console = new StringBuffer();

        console.append("----- Current parameters ----- \n");

        console.append("t0: ");
        console.append(step.timeToString());
        console.append('\n');

        if (Configuration.getTypeRelease()
                == Constant.RELEASE_ZONE) {
            console.append("Release: ZONE\n");
            console.append("Release depth [meter]: [");
            console.append(depthReleaseMin);
            console.append(" , ");
            console.append(depthReleaseMax);
            console.append("]\n");

            if (Configuration.isPulsation()) {
                console.append("Pulsation : DEFINED\n");
                console.append("--> Number release events: ");
                console.append(nbReleaseEvents);
                console.append(
                        "\n--> Release dt [second]: ");
                console.append(dtRelease);
                console.append('\n');
            } else {
                console.append("Pulsation: UNDEF\n");
            }
            if (Configuration.isPatchiness()) {
                console.append("Patchiness: DEFINED\n");
                console.append("--> Radius patch: ");
                console.append(patchRadius);
                console.append("\n--> Thickness patch: ");
                console.append(patchThickness);
                console.append('\n');
            } else {
                console.append("Patchiness: UNDEF\n");
            }
        } else {
            console.append("Release: FILE\n");
            console.append("--> ");
            console.append(Configuration.getDrifterFile());
            console.append('\n');
        }

        if (Configuration.isLethalTp()) {
            console.append("Lethal temperature: DEFINED\n");
            if (Configuration.isGrowth()) {
                console.append(
                        "--> Tp for egg [celsius]:");
                console.append(lethalTpEgg);
                console.append(
                        "\n--> Tp for larva [celsius]: ");
                console.append(lethalTpLarvae);
            } else {
                console.append("--> Lethal tp [celsius]: ");
                console.append(lethalTpEgg);
            }
            console.append('\n');
        } else {
            console.append("Lethal temperature: UNDEF\n");
        }
        if (Configuration.isBuoyancy()) {
            console.append("Buoyancy: DEFINED\n");
            console.append("Egg density [g.cm-3]: ");
            console.append(eggDensity);
            console.append('\n');
        } else {
            console.append("Buoyancy: UNDEF\n");
        }
        if (Configuration.getTypeRecruitment()
                != Constant.NONE) {
            console.append("Recruitment: DEFINED\n");
            switch (Configuration.getTypeRecruitment()) {
                case Constant.RECRUIT_AGE:
                    console.append(
                            "Age min at recruitment [day]: ");
                    console.append(ageMinAtRecruitment);
                    break;
                case Constant.RECRUIT_LENGTH:
                    console.append(
                            "Length min at recruitment [millimeter]: ");
                    console.append(lengthMinAtRecruitment);
                    break;
            }
            console.append('\n');
            if (Configuration.isDepthRecruitment()) {
                console.append("Recruitment depth [meter]: [");
                console.append(topDepthRecruitment);
                console.append(" , ");
                console.append(bottomDepthRecruitment);
                console.append("]\n");
            } else {
                console.append("Recruitment depth: UNDEF\n");
            }
        } else {
            console.append("Recruitment: UNDEF\n");
        }
        if (Configuration.isMigration()) {
            console.append("Vertical migration: DEFINED\n");
            console.append("--> depth daytime [meter] : ");
            console.append(depthDay);
            console.append('\n');
            console.append("--> depth nighttime [meter] : ");
            console.append(depthNight);
            console.append('\n');
        } else {
            console.append("Vertical migration : UNDEF\n");
        }
        console.append("Replica: ");
        console.append(replica + 1);
        console.append(" / ");
        console.append(SerialParameter.REPLICA.length());
        console.append('\n');
        console.append("------------------------------");

        System.out.println(console.toString());
    }

    /**
     * Initializes the user-predifined zones, both release and recruitment.
     *
     * @see ichthyop.core.Zone
     */
    private void initZone() {

        Zone znTmp;
        Iterator iter = Configuration.getReleaseZones().iterator();
        while (iter.hasNext()) {
            znTmp = (Zone) iter.next();
            znTmp.geo2Grid();
        }
        iter = Configuration.getRecruitmentZones().iterator();
        while (iter.hasNext()) {
            znTmp = (Zone) iter.next();
            znTmp.geo2Grid();
        }

    }

//////////
// Getters
//////////
    /**
     * Gets the duration of transport
     *
     * @return a long, the duration [second] each particle will be transported.
     */
    public static long getTransportDuration() {
        return transportDuration;
    }

    /**
     * Gets the recruitment length criterion.
     *
     * @return a float, the minimum length [millimeter] a particle must reach,
     * before being considered as recruited.
     */
    public static float getLengthMinAtRecruitment() {
        return lengthMinAtRecruitment;
    }

    /**
     * Gets the recruitment age criterion.
     *
     * @return a float, the minimum age [day] a particle must reach, before
     * being considered as recruited.
     */
    public static float getAgeMinAtRecruitment() {
        return ageMinAtRecruitment;
    }

    /**
     * Gets the recruitment minimum depth. 
     * 
     * @return a float, the minimum depth of recruitment criterion
     * based on depth.
     */
    public static float getMinDepthRecruitment() {
        return topDepthRecruitment;
    }

    /**
     * Gets the recruitment maximum depth.
     *
     * @return a float, the maximum depth of recruitment criterion
     * based on depth.
     */
    public static float getMaxDepthRecruitment() {
        return bottomDepthRecruitment;
    }

    /**
     * Gets the simulation duration.
     * <code>duration of simulation = duration of transport + duration
     * of release</code>
     *
     * @return a long, the simulation duration [second].
     */
    public static long getSimulationDuration() {
        return simulationDuration;
    }

    /**
     * Gets the time at wich the simulation starts.
     *
     * @return a long, the date and time expressed in seconds since origin at
     * wich the simulation starts.
     */
    public static long get_t0() {
        return t0;
    }

    /**
     * Gets egg density when Buoyancy is simulated.
     *
     * @return a float, the egg density [g/cm3]
     */
    public static float getEggDensity() {
        return eggDensity;
    }

    /**
     * Gets the lethal sea water temperature for all particles when growth is
     * not simulated or for particle at egg stage only when growth is on.
     *
     * @return a float, the lethal sea water temperature [celsius].
     */
    public static float getLethalTpEgg() {
        return lethalTpEgg;
    }

    /**
     * Gets the lethal sea water temperature for particle at larva stage, only
     * when growth is simulated.
     *
     * @return float the lethal sea water temperature [celsius]
     */
    public static float getLethalTpLarvae() {
        return lethalTpLarvae;
    }

    /**
     * Gets the minimum depth for release.
     * @return a double, the minimum depth [meter] for release.
     */
    public static double getDepthReleaseMin() {
        return depthReleaseMin;
    }

    /**
     * Gets the maximum depth for release.
     * @return a double, the maximum depth [meter] for release.
     */
    public static double getDepthReleaseMax() {
        return depthReleaseMax;
    }

    /**
     * Gets the number of release events, when PULSATION is on.
     * @return an int, the number of release events.
     */
    public static int getNbReleaseEvents() {
        return nbReleaseEvents;
    }

    /**
     * Gets the time step between two release events, when PULSATION is on.
     * @return an int, the time [second] between two release events.
     */
    public static int getReleaseDt() {
        return dtRelease;
    }

    /**
     * Gets the current index of the replica (a replica is one of the
     * simulations run with the same set of parameters).
     *
     * @return the index of the current replica.
     */
    public static int getReplica() {
        return replica;
    }

    /**
     * Gets the thickness of the patch, for patchy initial spacial distribution.
     *
     * @return a float, the thickness [meter] of the patch.
     */
    public static float getThickPatchi() {
        return patchThickness;
    }

    /**
     * Gets the radius of the patch, for patchy initial spacial distribution.
     *
     * @return a float, the radius [meter] of the patch.
     */
    public static float getRadiusPatchi() {
        return patchRadius;
    }

    /**
     * Gets the particle daytime depth when DVM is simulated.
     * @return a float, the depth [meter] of particle at daytime.
     */
    public static float getDepthDay() {
        return depthDay;
    }

    /**
     * Gets the particle nighttime depth when DVM is simulated.
     * @return a float, the depth [meter] of particle at nighttime.
     */
    public static float getDepthNight() {
        return depthNight;
    }

    /**
     * Gets the <code>Population</code> object associated to the current
     * <code>Simulation</code> object.
     *
     * @return the Population associated to the current simulation.
     */
    public Population getPopulation() {
        return population;
    }
    //---------- End of class
}
