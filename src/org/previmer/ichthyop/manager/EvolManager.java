package org.previmer.ichthyop.manager;

/**
 *
 * @author mariem
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.event.SetupListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.XBlock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.previmer.ichthyop.Template;
import org.previmer.ichthyop.arch.IEvol;
import org.previmer.ichthyop.io.ConfigurationFile;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayFloat.D0;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
//import org.previmer.ichthyop.evol.Stray;

public class EvolManager extends AbstractManager implements SetupListener {

    private static final EvolManager evolManager = new EvolManager();
    private IEvol evolStrategy;
    //private Stray str;

    public static EvolManager getInstance() {
        return evolManager;
    }

    public void initializePerformed(InitializeEvent e) throws Exception {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getParameter(String evolkey, String key) {
        return getSimulationManager().getParameterManager().getParameter(BlockType.EVOL, evolkey, key);
    }

    public boolean isEnabled(String evolKey) {
        return getSimulationManager().getParameterManager().isBlockEnabled(BlockType.EVOL, evolKey);
    }

    private XBlock findActiveReproductiveStrategy() throws Exception {
        List<XBlock> list = new ArrayList();
        for (XBlock block : getSimulationManager().getParameterManager().getBlocks(BlockType.EVOL)) {
            if (block.isEnabled()) {
                list.add(block);
            }
        }
        if (list.isEmpty()) {
            throw new NullPointerException("Could not find any enabled " + BlockType.EVOL.toString() + " block in the configuration file.");
        }
        if (list.size() > 1) {
            throw new IOException("Found several " + BlockType.RELEASE.toString() + " blocks enabled in the configuration file. Please only keep one enabled.");
        }
        return list.get(0);
    }

    private void instantiateReproductiveStrategy() throws Exception {

        XBlock strategyBlock = findActiveReproductiveStrategy();
        String className = getParameter(strategyBlock.getKey(), "class_name");
        //  float lost;

        if (strategyBlock != null) {
            try {
                evolStrategy = (IEvol) Class.forName(className).newInstance();
                getEvolStrategy().loadParameters();

                // A partir de la première ponte     
                //      str.loadParameters();
                //      lost=str.getRateStray();
            } catch (Exception ex) {
                StringBuilder sb = new StringBuilder();
                sb.append("Evol process instantiation failed ==> ");
                sb.append(ex.toString());
                InstantiationException ieex = new InstantiationException(sb.toString());
                ieex.setStackTrace(ex.getStackTrace());
                throw ieex;

            }
        }
    }

    private IEvol getEvolStrategy() {
        return evolStrategy;
    }

    public void setupPerformed(SetupEvent e) throws Exception {
        if (getSimulationManager().getOutputManager().getIndexGeneration() == 0) {
            cfgEvolToCfgIchthyop();
        } else {
            cfgEvolGenToCfgIchthyop();
        }
    }

    public void cfgEvolGenToCfgIchthyop() {
        // modifier le release schedule avec les nouvelles dates dans le fichier xml
        // charger time drifter release dans le fichier xml
        File evolFile = getSimulationManager().getConfigurationFile();
        ConfigurationFile cfgTemplate = new ConfigurationFile(Template.getTemplateURL("cfg-roms3d.xml"));
        ConfigurationFile Mycfg = new ConfigurationFile(evolFile);
        try {
            Mycfg.load();
        } catch (Exception ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        String fileName = getSimulationManager().getOutputManager().getFileLocation();
        String parent = new File(fileName).getParentFile().getParent();
        parent = parent.concat("/releaseG");
        int x = OutputManager.getIndexGeneration();
        parent = parent.concat(String.valueOf(x));
        String[] dates = setGenReleaseDates(parent);
        String event = StringtabToString(dates);
        Mycfg.getXParameter(BlockType.OPTION, "release.schedule", "events").reset();
        Mycfg.getXParameter(BlockType.OPTION, "release.schedule", "events").setValue(event);
        if (!Mycfg.containsBlock(BlockType.RELEASE, "release.TimeDrifterRelease")) {
            Mycfg.addBlock(cfgTemplate.getBlock(BlockType.RELEASE, "release.TimeDrifterRelease").detach());
            Mycfg.getBlock(BlockType.RELEASE, "release.TimeDrifterRelease").setEnabled(true);
            Mycfg.getXParameter(BlockType.RELEASE, "release.TimeDrifterRelease", "directory").setValue(parent);
            try {
                Mycfg.write(new FileOutputStream(Mycfg.getFile()));
            } catch (IOException ex) {
                Logger.getLogger(EvolManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Mycfg.getXParameter(BlockType.RELEASE, "release.TimeDrifterRelease", "directory").reset();
            Mycfg.getXParameter(BlockType.RELEASE, "release.TimeDrifterRelease", "directory").setValue(parent);
        }
        /*
         * Save the updated configuration file
         */
        Iterator<XBlock> it = Mycfg.getAllBlocks().iterator();
        getSimulationManager().getParameterManager().cleanup();
        while (it.hasNext()) {
            getSimulationManager().getParameterManager().addBlock(it.next());
        }
        try {
            getSimulationManager().getParameterManager().save();
        } catch (IOException ex) {
            Logger.getLogger(EvolManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String[] setGenReleaseDates(String parent) {
        String[] dates = null;
        File directory = new File(parent);
        String[] files = null;

        if (directory.isDirectory()) {
            files = directory.list();
        }
        if (files != null) {
            String date;
            SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("'year' yyyy 'month' MM 'day' dd 'at' HH:mm");

            for (int i = 0; i < files.length; i++) {
                date = (String) files[i].subSequence(files[i].indexOf("_"), files[i].indexOf("."));
                System.out.println(date);
                Date dateSchedule = null;
                try {
                    dateSchedule = (Date) INPUT_DATE_FORMAT.parse(date);
                } catch (ParseException ex) {
                    Logger.getLogger(EvolManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                Calendar cal = (Calendar) getSimulationManager().getTimeManager().getCalendar().clone();
                cal.setTime(dateSchedule);
                date = INPUT_DATE_FORMAT.format(cal.getTime());
                dates[i] = date;
            }
        }
        return dates;
    }

    private String[] setReleaseDates() {
        File evolFile = getSimulationManager().getConfigurationFile();
        ConfigurationFile Mycfg = new ConfigurationFile(evolFile);
        try {
            Mycfg.load();
        } catch (Exception ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        String t0 = Mycfg.getXParameter(BlockType.OPTION, "app.time", "initial_time").getValue();
        int frequency = Integer.parseInt(Mycfg.getXParameter(BlockType.OPTION, "release.evol", "release_frequency").getValue());
        Double Days = Math.ceil(360 / frequency);
        int nbDays = Days.intValue();
        String[] dates = new String[nbDays];
        String timeAsStr;
        try {
            SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("'year' yyyy 'month' MM 'day' dd 'at' HH:mm");

            Date date_start = (Date) INPUT_DATE_FORMAT.parse(t0);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date_start);

            for (int i = 0; i < nbDays; i++) {
                timeAsStr = INPUT_DATE_FORMAT.format(cal.getTime());
                dates[i] = timeAsStr;
                cal.add(Calendar.DATE, frequency);
            }
        } catch (ParseException ex) {
            Logger.getLogger(EvolManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dates;
    }

    private String StringtabToString(String[] dates) {
        String str = "";
        for (int k = 0; k < dates.length; k++) {
            str = str.concat("\"");
            str = str.concat(dates[k]);
            str = str.concat("\" ");
        }
        return str;
    }

    private void cfgEvolToCfgIchthyop() {
        File evolFile = getSimulationManager().getConfigurationFile();
        ConfigurationFile cfgTemplate = new ConfigurationFile(Template.getTemplateURL("cfg-generic.xml"));
        ConfigurationFile Mycfg = new ConfigurationFile(evolFile);
        try {
            Mycfg.load();
        } catch (Exception ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!Mycfg.containsBlock(BlockType.OPTION, "release.schedule")) {
            Mycfg.addBlock(cfgTemplate.getBlock(BlockType.OPTION, "release.schedule").detach());
            Mycfg.getBlock(BlockType.OPTION, "release.schedule").setTreePath("Advanced/Release/Schedule");
            Mycfg.getXParameter(BlockType.OPTION, "release.schedule", "is_enabled").setValue("true");
            String[] dates = setReleaseDates();
            String event = StringtabToString(dates);
            Mycfg.getXParameter(BlockType.OPTION, "release.schedule", "events").setValue(event);
        } else {
            System.out.println("release.schedule block is already existing in your file.");
        }
        if (!Mycfg.containsBlock(BlockType.RELEASE, "release.uniform")) {
            Mycfg.addBlock(cfgTemplate.getBlock(BlockType.RELEASE, "release.uniform").detach());
            Mycfg.getBlock(BlockType.RELEASE, "release.uniform").setEnabled(true);
            String nb_particles = Mycfg.getXParameter(BlockType.OPTION, "release.evol", "nb_particles").getValue();

            Mycfg.getXParameter(BlockType.RELEASE, "release.uniform", "number_particles").setValue(nb_particles);
            try {
                Mycfg.write(new FileOutputStream(Mycfg.getFile()));
            } catch (IOException ex) {
                Logger.getLogger(EvolManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            /*
             * Save the updated configuration file
             */
            Iterator<XBlock> it = Mycfg.getAllBlocks().iterator();
            getSimulationManager().getParameterManager().cleanup();
            while (it.hasNext()) {
                getSimulationManager().getParameterManager().addBlock(it.next());
            }
            try {
                getSimulationManager().getParameterManager().save();
            } catch (IOException ex) {
                Logger.getLogger(EvolManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /*********************************************************************************************************/
    /*******************  Lecture des sorties de la génération i-1 ******************************************/
    public Array readMortality(NetcdfFile ncIn, int[] origin, int[] size) {
        Array death = null;
        try {
            //NetcdfFile ncIn = NetcdfFile.open(File);
            try {
                // lecture de mortality à t_end
                Array mortalityArr = ncIn.findVariable("mortality").read(origin, size);
                death = mortalityArr.reduce();
            } catch (InvalidRangeException ex) {
                Logger.getLogger(TestLectureDataNC.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(TestLectureDataNC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return death;
    }

    public List<Integer> findIndexAlive(Array death) {
        List<Integer> aliveList = new ArrayList<Integer>();
        int[] mort = (int[]) death.copyTo1DJavaArray();
        int j = 0;    //compteur dans mort[]
        while (j < mort.length) {
            if ((mort[j] == 3) || (mort[j] == 0)) {
                aliveList.add(j);
            }
            //System.out.println("l'élément : " + mort[j] + " d'indice " + j);
            j++;
        }
        return aliveList;
    }

    public Array readRecruitment(NetcdfFile ncIn, int[] origin, int[] size) throws IOException, InvalidRangeException {
        // lecture de recruitment à t_end
        ArrayInt recruitArr = (ArrayInt) ncIn.findVariable("recruited_stain").read(origin, size);
        Array recruited = recruitArr.reduce();
        int buff = 0;

        while (buff < recruited.getSize()) {
            recruited.getIndex().incr();
            buff++;
        }
        return recruited;
    }

    public List<Integer> findIndexRecruitment(Array recruited) {
        List<Integer> recruitedList = new ArrayList<Integer>();
        int[] recruit = (int[]) recruited.copyTo1DJavaArray();
        int j = 0;    //compteur dans recruited[]
        while (j < recruit.length) {
            if (recruit[j] == 1) {
                recruitedList.add(j);
            }
            j++;
        }
        return recruitedList;
    }

    public List<Integer> indexAliveRecruited(List<Integer> aliveList, List<Integer> recruitedList) {
        List<Integer> indexList = new ArrayList<Integer>();
        if (recruitedList.isEmpty()) {
            System.out.println("Aucun individu recruited");
            return indexList;
        }
        int j;
        for (int i = 0; i < aliveList.size(); i++) {
            j = 0;
            while (j < recruitedList.size() && aliveList.get(i) != recruitedList.get(j)) {
                j++;
            }
            if (i < aliveList.size() && j < recruitedList.size()) {
                if (aliveList.get(i) == recruitedList.get(j)) {
                    indexList.add(recruitedList.get(j));
                    recruitedList.remove(j);
                }
            }
        }
        return indexList;
    }

    public float[][] readWhenRecruited(NetcdfFile ncIn, List<Integer> aliveRecruited, int length) {

        int[] start;
        int[] size = new int[]{length, 1};      //{61,1}
        int[] taille;
        ArrayInt recrut = null;
        int[] tabRecruited = null; 
        boolean found;

        int j; // curseur dans le tableau recrut de l'individu i;
        float[][] candidates = new float[aliveRecruited.size()][4];  // 4 = t, lon, lat, depth
        for (int i = 0; i < aliveRecruited.size(); i++) {
            start = new int[]{0, aliveRecruited.get(i)};
            found = false;
            j = 0;
            try {   // lecture de la variable mortality pour les individus recrutés -> qlq soit t.
                recrut = (ArrayInt) ncIn.findVariable("mortality").read(start, size).reduce();
            } catch (IOException ex) {
                Logger.getLogger(TestLectureDataNC.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidRangeException ex) {
                Logger.getLogger(TestLectureDataNC.class.getName()).log(Level.SEVERE, null, ex);
            }

            tabRecruited = (int[]) recrut.copyTo1DJavaArray();

            //
            // jusque là testé mais je ne peux avoir  les -99 avec benguela **********************
            // et encore moins avec mes simulations car je n'ai que des recrutés et en vie !!!
            //
            while (!found && j < tabRecruited.length) {
                if (tabRecruited[j] != -99) {
                    found = true;
                } else {
                    j++;
                    System.out.println("pas encore né");
                }
            }
            if (j == tabRecruited.length) {
                System.out.println("Erreur: individu recrute aber date de naissance introuvable !!!");
            } else {
                // j= date de naissance
                start = new int[]{j, aliveRecruited.get(i)};
                taille = new int[]{1, 1};

                ArrayFloat.D0 lonArr = null;
                ArrayFloat.D0 latArr = null;
                ArrayFloat.D0 depthArr = null;

                try {
                    lonArr = (D0) (ArrayFloat) ncIn.findVariable("lon").read(start, taille).reduce();
                    latArr = (D0) (ArrayFloat) ncIn.findVariable("lat").read(start, taille).reduce();
                    depthArr = (D0) (ArrayFloat) ncIn.findVariable("depth").read(start, taille).reduce();
                } catch (IOException ex) {
                    Logger.getLogger(TestLectureDataNC.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidRangeException ex) {
                    Logger.getLogger(TestLectureDataNC.class.getName()).log(Level.SEVERE, null, ex);
                }
                // C bon :)
                /*candidates[i][0] = j;
                System.out.println("naissance du candidat " + aliveRecruited.get(i) + " = " + candidates[i][0]);
                candidates[i][1] = lonArr.get();
                System.out.println("longitude du candidat " + aliveRecruited.get(i) + " = " + candidates[i][1]);
                candidates[i][2] = latArr.get();
                System.out.println("latitude du candidat " + aliveRecruited.get(i) + " = " + candidates[i][2]);
                candidates[i][3] = depthArr.get();
                System.out.println("profondeur du candidat " + aliveRecruited.get(i) + " = " + candidates[i][3]);*/
            }
        }
        return candidates;
    }

    // retourne le tableau des candidats à la reproduction avec leurs caractéristiques
    public float[][] getCandidate() throws IOException, InvalidRangeException {
        String File = getSimulationManager().getOutputManager().getFileLocation();
        System.out.println("dans getCandidate(), getFileLocation()= " + File);
        //String File = "/home/mariem/ichthyop/dev/evol/output/roms3d_ichthyop-run201112261623.nc";
        NetcdfFile ncIn = NetcdfFile.open(File);
        ArrayDouble.D1 timeArr = null;
        try {
            timeArr = (ArrayDouble.D1) ncIn.findVariable("time").read();
        } catch (IOException ex) {
            Logger.getLogger(TestLectureDataNC.class.getName()).log(Level.SEVERE, null, ex);
        }
        int length = timeArr.getShape()[0];     // récupérer t_end
        int[] origin = new int[]{length - 1, 0};
        Dimension drifter = ncIn.getDimensions().get(1);
        int[] size = new int[]{1, drifter.getLength()};

        Array arrMortality = readMortality(ncIn, origin, size);
        List<Integer> mortalityList = findIndexAlive(arrMortality);
        Array arrRecruitment = readRecruitment(ncIn, origin, size);
        List<Integer> recruitedList = findIndexRecruitment(arrRecruitment);
        List<Integer> aliveRecruitedList = indexAliveRecruited(mortalityList, recruitedList);

        float[][] candidats = null;
        if (!aliveRecruitedList.isEmpty()) {
            candidats = readWhenRecruited(ncIn, aliveRecruitedList, length);
        }
        ncIn.close();
        return candidats;

    }

    // retourne les indexes des parents sélectionnés autant de fois qu'ils l'ont été.
    //testée et validée.
    public List<Integer> selectIndexParents(int nbParticles, float[][] candidates) {
        List<Integer> selectedList = new ArrayList<Integer>();

        int nbParents = 0;
        int newParticle = 0;
        if (candidates != null) {
            nbParents = candidates.length;
            newParticle = nbParticles - nbParents;
        }

        if (newParticle == 0) {
            return null;
        }
        if (newParticle < 0) {
            return null;    // cas impossible dans le cas d'un an de vie
        }
        if (newParticle > 0) {
            // vérifier le nombre de répartition équitable qu'on peut faire:
            float q = nbParticles / nbParents;
            int reste = nbParticles % nbParents;

            for (int index = 0; index < nbParents; index++) {
                for (int i = 0; i < q; i++) {
                    selectedList.add(index);
                }
            }
            if (reste != 0) {
                /** Generate 'reste' random integers in the range 0 .. nbParents. */
                Random randomGenerator = new Random();
                for (int idx = 1; idx <= reste; ++idx) {
                    int randomInt = randomGenerator.nextInt(nbParents);
                    selectedList.add(randomInt);
                }
            }
        }
        return selectedList;
    }

    /**********************************************************************************************************
     *                  Mettre le tableau des nouveaux cacndidats dans des fichiers de release.txt
     */
    public int createMarge(float x, float marge) {
        float eupsilon = (float) (-marge + (Math.random() * (marge + marge)));
        x = (x + eupsilon);
        //DecimalFormat myFormatter = new DecimalFormat("### ###,00");
        //x= Float.valueOf(myFormatter.format(x));
        return (int) x;
    }

    /* public static float deuxApVir (float d){
    DecimalFormat df = new DecimalFormat("########.00"); 
    String str = df.format(d); 
    d = Float.parseFloat(str.replace(',', '.')); 
    return d;
    }*/
    public int[][] createArrayNewParticles(float[][] candidates, List<Integer> selectedList, float time_marge, float marge, float bathy_marge) {
        if ((candidates == null) || (selectedList.isEmpty())) {
            return null;
        }
        int[][] newParticles = new int[selectedList.size()][4];
        for (int i = 0; i < newParticles.length; i++) {
            int j = 0;
            newParticles[i][j] = Math.abs(createMarge(candidates[selectedList.get(i)][j], time_marge));
            for (j = 1; j < 3; j++) {
                newParticles[i][j] = createMarge(candidates[selectedList.get(i)][j], marge);

            }
            j = 3;
            newParticles[i][j] = createMarge(candidates[selectedList.get(i)][j], bathy_marge);
        }
        return newParticles;

    }

    private double dateStrToSeconds(String dateStr) {

        Calendar calendar = (Calendar) getSimulationManager().getTimeManager().getCalendar().clone();
        SimpleDateFormat releaseDateFormat = new SimpleDateFormat("'y'yyyy'm'MM'd'dd'h'HHmm");
        releaseDateFormat.setCalendar(calendar);
        double seconds = Double.NaN;
        try {
            calendar.setTime(releaseDateFormat.parse(dateStr));
        } catch (ParseException ex) {
            Logger.getLogger(EvolManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        seconds = calendar.getTimeInMillis() / 1000.d;

        return seconds;
    }

    public void createReleaseNextGeneration(int[][] newParticles) throws IOException {
        String nomFile = getSimulationManager().getOutputManager().getFileLocation();
        //String nomFile = "/home/mariem/ichthyop/dev/evol/output/roms3d_ichthyopevol_runG3-201112261546.nc";
        String parent = new File(nomFile).getParentFile().getParent();
        parent = parent.concat("/");
        //System.out.println("\n*************************** parent: " + parent);

        String g = (String) nomFile.subSequence(nomFile.indexOf("G"), nomFile.indexOf("-"));
        g = g.substring(1);
        int x = Integer.parseInt(g) + 1;
        g = String.valueOf(x);
        parent = parent.concat("releaseG").concat(g);

        boolean verif = new File(parent).mkdir();
        if (verif) {
            System.out.println("dossier créé.");
        }
        String time = "";
        List<String> timeList = new ArrayList<String>();

        if (newParticles != null) {
            for (int i = 0; i < newParticles.length; i++) {
                FileWriter fw = null;
                //time = String.valueOf(newParticles[i][0]);   // récupérer le birthday de chaque individu
                //String fichier = parent.concat("/").concat("release_").concat(time);

                Calendar calendar = (Calendar) getSimulationManager().getTimeManager().getCalendar().clone();
                calendar.setTimeInMillis((long) newParticles[i][0] * 1000L);
                calendar.add(Calendar.YEAR, 1);     // spécifier que c'estla même date, mais de l'année suivante.
                SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("'y'yyyy'm'MM'd'dd'h'HHmm");
                String timeAsStr = INPUT_DATE_FORMAT.format(calendar.getTime());

                String fichier = parent.concat("/").concat("release_").concat(timeAsStr);
                // vérifier s'il y'a un autre fichier déjà crée pour ce temps
                if (!timeList.isEmpty()) {
                    int idx = 0;
                    while (idx < timeList.size()) {
                        if (timeList.get(idx).equals(timeAsStr)) {
                            //ouvrir le fichier en question et écrire à la fin
                            String ligne = String.valueOf(newParticles[i][1]) + " " + String.valueOf(newParticles[i][2]) + " " + String.valueOf(newParticles[i][3]) + "\n";
                            try {   // ajouter la particule à la fin du fichier
                                fw = new FileWriter(fichier, true);
                                fw.write(ligne, 0, ligne.length());
                            } catch (IOException ex) {
                                Logger.getLogger(TestTableToTxt.class.getName()).log(Level.SEVERE, null, ex);
                            } finally {
                                if (fw != null) {
                                    fw.close();
                                }
                            }
                            break;
                        } else {
                            idx++;
                        }
                    }
                    if (idx == timeList.size()) {   // c'est un temps qui n'existe pas au paravant.
                        timeList.add(timeAsStr);
                        try {
                            fw = new FileWriter(fichier);
                        } catch (IOException ex) {
                            Logger.getLogger(TestTableToTxt.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        String ligne = String.valueOf(newParticles[i][1]) + " " + String.valueOf(newParticles[i][2]) + " " + String.valueOf(newParticles[i][3]) + "\n";

                        try {
                            fw.write(ligne);
                        } catch (IOException ex) {
                            Logger.getLogger(TestTableToTxt.class.getName()).log(Level.SEVERE, null, ex);
                        } finally {
                            if (fw != null) {
                                fw.close();
                            }
                        }
                    }
                } else {    // C'est la première ligne du tableau
                    timeList.add(timeAsStr);
                    try {
                        fw = new FileWriter(fichier);
                    } catch (IOException ex) {
                        Logger.getLogger(TestTableToTxt.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    String ligne = String.valueOf(newParticles[i][1]) + " " + String.valueOf(newParticles[i][2]) + " " + String.valueOf(newParticles[i][3]) + "\n";

                    try {
                        fw.write(ligne);
                    } catch (IOException ex) {
                        Logger.getLogger(TestTableToTxt.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        if (fw != null) {
                            fw.close();
                        }
                    }
                }
            }
        }
    }

    public void prepareEvolRelease() throws ParseException {
        float[][] next = null;
        try {
            next = getCandidate();
        } catch (IOException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (next == null) {
            getLogger().log(Level.INFO, "Extinction of the specie in the generation: {0}", OutputManager.getIndexGeneration());
        }
        int nbParticles = getSimulationManager().getReleaseManager().getNbParticles();
        System.out.println("--------------------------  nbParticles: " + nbParticles);
        List<Integer> newList = selectIndexParents(nbParticles, next);
        String timeMargin = getSimulationManager().getParameterManager().getParameter(BlockType.EVOL, "evol.strict", "margin_time");
        long releaseMargin = getSimulationManager().getTimeManager().day2seconds(timeMargin);
        String locationMargin = getSimulationManager().getParameterManager().getParameter(BlockType.EVOL, "evol.strict", "margin_loc");
        String bathyMargin = getSimulationManager().getParameterManager().getParameter(BlockType.EVOL, "evol.strict", "margin_bat");
        int[][] nouveaux = createArrayNewParticles(next, newList, releaseMargin, Float.valueOf(locationMargin), Float.valueOf(bathyMargin));
        if (nouveaux == null) {
            System.out.println("Aucune nouvelle particule");
        }
        try {
            createReleaseNextGeneration(nouveaux);
        } catch (IOException ex) {
            Logger.getLogger(TestTableToTxt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
