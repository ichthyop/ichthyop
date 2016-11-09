package ichthyop.ui.chart;

/** local import */
import ichthyop.core.Population;
import ichthyop.io.Configuration;
import ichthyop.bio.GrowthModel;
import ichthyop.util.Constant;
import ichthyop.util.Resources;
import ichthyop.util.IParticle;

/** import java.util */
import java.awt.Component;
import java.util.Iterator;

/** import AWT */
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.DisplayMode;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;

/**
 * <p>For SINGLE simulations (real time display), several pre-defined charts allow
 * a real-time monitoring of the simulation.
 * <ul>
 * <li>depth distribution: histogram.
 * <li>out of area: census of the particles getting out of the area of
 * simulation and beached particles.
 * <li>recruitment counting: stacked bar chart showing a census of the
 * recruited individuals classified with the colour of the release zone.
 * <li>length distribution: histogram.
 * <li>population counting by stage: only available when growth is activated.
 * It makes a census of the particles depending on their stage
 * (egg, yolk-sac larva, feeding larva).
 * <li>mortality counting: census of dead cold particles. The chart
 * distinguishes between eggs and larvae when growth is activated.
 * </ul>
 * </p>
 *
 * This class controls the charts. It provides methods to construct and to
 * destroy the charts. The class also handles the collect of the data by
 * scanning the population of particles any time the chart datasets have to be
 * updated. The ChartManager displays the first chart at top of the screen, on
 * the right side of the MainFrame. The second chart is slightly translated
 * toward the bottom right corner of the screen, so it does not hide the first
 * chart, and so on for the following charts.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see package ichthyop.util.chart for details about the charts.
 */
public class ChartManager {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * The population of the particles trought which the particle's properties
     * can be accessed.
     */
    private static Population population;
    /**
     * The depth distribution histogram.
     */
    private static HistoChart chartDepth;
    /**
     * The length distribution histogram.
     */
    private static HistoChart chartLength;
    /**
     * The out-of-area chart: census of the particles getting out of the
     * area of simulation and beached particles.
     */
    private static AreaChart chartOut;
    /**
     * The recruitment chart: stacked bar chart showing a census of the
     * recruited individuals classified with the colour of the release zone.
     */
    private static RecruitChart chartRecruitment;
    /**
     * The mortality chart: census of dead cold particles. The chart
     * distinguishes between eggs and larvae when growth is activated.
     */
    private static MortalityChart chartMortality;
    /**
     * The counting-by-stage chart: only available when growth is activated.
     * It makes a census of the particles depending on their stage
     * (egg, yolk-sac larva, feeding larva).
     */
    private static StageChart chartStage;
    /**
     * Dataset for the number of "out" particles: beached and out of the
     * simulated domain.
     */
    private static int outCount;
    /**
     * Dataset for depth distribution of the particles.
     */
    private static double[] depthDistribution;
    /**
     * Dataset for the census of the particles by stage when
     * GROWTH is simulated.
     */
    private static int[] stageDistribution;
    /**
     * Dataset for recruitment counting
     */
    private static int[] recruitmentCount;
    /**
     * Dataset for length distribution of the particle when GROWTH is simulated.
     */
    private static double[] lengthDistribution;
    /**
     * Dataset for census of dead cold particules when
     * LETHAL TEMPERATURE is active.
     */
    private static int[] mortalityCount;
    /**
     * Coordinate of the top-left corner of the charts.
     */
    private static int xChart, yChart;
    /**
     * Dimension of the screen.
     */
    private static Dimension windows;

///////////////
// Constructors
///////////////

    /**
     * Construct a new ChartManager. Determines the screen dimension and
     * initializes the coordinates of the first chart that will be displayed.
     */
    public ChartManager() {

        GraphicsDevice ecran = GraphicsEnvironment.getLocalGraphicsEnvironment().
                               getDefaultScreenDevice();
        GraphicsConfiguration config = ecran.getDefaultConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
        DisplayMode mode = config.getDevice().getDisplayMode();
        Dimension size = new Dimension(mode.getWidth(), mode.getHeight());
        windows = new Dimension(size.width - insets.left - insets.right,
                                size.height - insets.bottom - insets.top);

        xChart = (int) (0.5 * windows.width);
        yChart = 0;
    }

    /**
     * Constructs a new empty depth distribution chart and displays on screen.
     */
    public static void createChartDepth() {

        chartDepth = new HistoChart(Resources.CHART_TITLE_DEPTH,
                                    Resources.CHART_LEGEND_DEPTH + " " +
                                    Resources.UNIT_METER,
                                    Resources.CHART_LEGEND_NB_PARTICLES, 10,
                                    xChart, yChart);
        xChart += (int) (windows.width * 0.03f);
        yChart += (int) (windows.height * 0.03f);
    }

    /**
     * Destroys the chart and removes from screen.
     */
    public static void disposeOfChartDepth() {
        chartDepth.dispose();
    }

    /**
     * Constructs a new empty length distribution chart and displays on screen.
     */
    public static void createChartLength() {

        chartLength = new HistoChart(Resources.CHART_TITLE_LENGTH,
                                     Resources.CHART_LEGEND_LENGTH + " " +
                                     Resources.UNIT_MILLIMETER,
                                     Resources.CHART_LEGEND_NB_PARTICLES, 10,
                                     xChart, yChart);
        xChart += (int) (windows.width * 0.03f);
        yChart += (int) (windows.height * 0.03f);
    }

    /**
     * Destroys the chart and removes from screen.
     */
    public static void disposeOfChartLength() {
        chartLength.dispose();
    }

    /**
     * Constructs a new empty out-of-area chart and displays on screen.
     */
    public static void createChartOut() {

        chartOut = new AreaChart(Resources.CHART_TITLE_OUT,
                                 Resources.CHART_LEGEND_TIME,
                                 Resources.CHART_LEGEND_NB_PARTICLES,
                                 xChart, yChart);
        xChart += (int) (windows.width * 0.03f);
        yChart += (int) (windows.height * 0.03f);
    }

    /**
     * Destroys the chart and removes from screen.
     */
    public static void disposeOfChartOut() {
        chartOut.dispose();
    }

    /**
     * Constructs a new recruitment-counting chart and displays on screen.
     */
    public static void createChartRecruitment() {

        chartRecruitment = new RecruitChart(Resources.CHART_TITLE_RECRUITMENT, null,
                                            Resources.CHART_LEGEND_NB_PARTICLES,
                                            xChart, yChart);
        xChart += (int) (windows.width * 0.03f);
        yChart += (int) (windows.height * 0.03f);
    }

    /**
     * Destroys the chart and removes from screen.
     */
    public static void disposeOfChartChartRecruitment() {
        chartRecruitment.dispose();
    }

    /**
     * Constructs a new mortality-counting chart and displays on screen.
     */
    public static void createChartMortality() {

        chartMortality = new MortalityChart(Resources.CHART_TITLE_DEAD_COLD,
                                            Resources.CHART_LEGEND_TIME,
                                            Resources.CHART_LEGEND_NB_PARTICLES,
                                            xChart, yChart);
        xChart += (int) (windows.width * 0.03f);
        yChart += (int) (windows.height * 0.03f);
    }

    /**
     * Destroys the chart and removes from screen.
     */
    public static void disposeOfChartMortality() {
        chartMortality.dispose();
    }

    /**
     * Constructs a new counting-by-stage chart and displays on screen.
     */
    public static void createChartStage() {

        chartStage = new StageChart(Resources.CHART_TITLE_STAGE,
                                    Resources.CHART_LEGEND_TIME,
                                    Resources.CHART_LEGEND_NB_PARTICLES, xChart,
                                    yChart);
        xChart += (int) (windows.width * 0.03f);
        yChart += (int) (windows.height * 0.03f);
    }

    /**
     * Destroys the chart and removes from screen.
     */
    public static void disposeOfChartStage() {
        chartStage.dispose();
    }

    /**
     * Collects all the information needed to update the chart dataset, by
     * scanning the population of particles.
     */
    public static void counting() {

        if (chartDepth == null
            && chartOut == null
            && chartLength == null
            && chartRecruitment == null
            && chartMortality == null
            && chartStage == null) {
            return;
        }

        int i_particle = 0;
        int nbRecruitmentZones = Configuration.getRecruitmentZones().size();
        int nbReleaseZones = Configuration.getReleaseZones().size();
        //int typeRecruitment = Configuration.getTypeRecruitment();

        /** Initialization */
        outCount = 0;
        stageDistribution = new int[3];
        mortalityCount = new int[2];
        if (chartDepth != null) {
            depthDistribution = new double[population.size()];
        }
        if (chartLength != null) {
            lengthDistribution = new double[population.size()];
        }
        if (recruitmentCount == null) {
            recruitmentCount = new int[nbRecruitmentZones * (nbReleaseZones + 1)];
        }

        IParticle particle;
        Iterator<IParticle> iter = population.iterator();
        while (iter.hasNext()) {
            particle = iter.next();

            /** Recruitment chart */
            if (chartRecruitment != null) {
                if (particle.isNewRecruited()) {
                //if (particle.checkRecruitment(typeRecruitment)) {
                    recruitmentCount[nbRecruitmentZones *
                            particle.getNumZoneInit()
                            + particle.getNumRecruitZone()]++;
                    particle.resetNewRecruited();
                }
            }

            /** Out of domain chart */
            if (chartOut != null) {
                if (particle.getDeath() == Constant.DEAD_OUT
                    || particle.getDeath() == Constant.DEAD_BEACH) {
                    outCount++;
                }
            }
            /** Mortality chart */
            if (chartMortality != null) {
                if (particle.isDeadCold()) {
                    mortalityCount[(particle.getDeath() == Constant.DEAD_COLD) ?
                            0 : 1]++;
                }
            }

            /** Depth chart */
            if (chartDepth != null) {
                depthDistribution[i_particle] = particle.getDepth();
            }

            /** Length chart */
            if (chartLength != null) {
                lengthDistribution[i_particle] = particle.getLength();
            }

            /** Stage chart */
            if (chartStage != null) {
                if (particle.isLiving()) {
                    stageDistribution[GrowthModel.getStage(particle.getLength())]++;
                }
            }

            i_particle++;
        }
    }

    /**
     * Refreshes the display of the active charts.
     */
    public static void refresh(String strTime) {

        if (chartDepth != null) {
            chartDepth.refresh(depthDistribution, strTime);
        }
        if (chartOut != null) {
            chartOut.refresh(outCount);
        }
        if (chartRecruitment != null) {
            chartRecruitment.refresh(recruitmentCount, strTime);
        }
        if (chartLength != null) {
            chartLength.refresh(lengthDistribution, strTime);
        }
        if (chartMortality != null) {
            chartMortality.refresh(mortalityCount);
        }
        if (chartStage != null) {
            chartStage.refresh(stageDistribution);
        }
    }

    /**
     * Resets the charts.
     */
    public static void reset() {

        recruitmentCount = null;

        if (chartOut != null) {
            chartOut.reset();
        }
        if (chartStage != null) {
            chartStage.reset();
        }
        if (chartMortality != null) {
            chartMortality.reset();
        }
    }
    
    public static void capture(Calendar calendar) throws IOException {
        
        if (chartDepth != null) {
            screen2File(chartDepth.getContentPane(), calendar, "depth");
        }
        if (chartOut != null) {
            screen2File(chartOut.getContentPane(), calendar, "out");
        }
        if (chartRecruitment != null) {
            screen2File(chartRecruitment, calendar, "recruit");
        }
        if (chartLength != null) {
            screen2File(chartLength, calendar, "length");
        }
        if (chartMortality != null) {
            screen2File(chartMortality, calendar, "mortality");
        }
        if (chartStage != null) {
            screen2File(chartStage, calendar, "stage");
        }
    }
    
    private static void screen2File(Component component, Calendar calendar, String name) throws
                IOException {

            SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
            dtFormat.setCalendar(calendar);
            String fileName = Configuration.getDirectorOut() +
                    name + "_" + dtFormat.format(calendar.getTime()) + ".png";
            System.out.println(fileName);

            try {
                BufferedImage bi = new BufferedImage(component.getWidth(),
                        component.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics g = bi.getGraphics();
                component.paintAll(g);
                ImageIO.write(bi, "PNG", new File(fileName));
            } catch (Exception ex) {
                throw new IOException("Problem saving picture " + fileName);
            }
        }

//////////
// Setters
//////////

    /**
     * Sets the Population object.
     *
     * @param pop a Population object, the population of particles.
     */
    public static void setPopulation(Population pop) {
        population = pop;
    }

    //---------- End of class
}
