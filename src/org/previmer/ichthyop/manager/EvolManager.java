package org.previmer.ichthyop.manager;

/**
 *
 * @author mariem
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.previmer.ichthyop.Template;
import org.previmer.ichthyop.arch.IEvol;
import org.previmer.ichthyop.io.ConfigurationFile;
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
        cfgEvolToCfgIchthyop();

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

    private String StringtabToString() {
        String str = "";
        String[] dates = setReleaseDates();
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
            String event = StringtabToString();
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
}
