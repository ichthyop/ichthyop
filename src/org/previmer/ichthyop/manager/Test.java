package org.previmer.ichthyop.manager;

/**
 *
 * @author mariem
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.Template;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.ConfigurationFile;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;
import org.previmer.ichthyop.manager.UpdateManager;

public class Test extends AbstractManager {

    private UpdateManager manage = new UpdateManager();
    private HashMap<String, XBlock> map;
    //static long t_init = SimulationManager.getInstance().getTimeManager().get_tO();
    public static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("'year' yyyy 'month' MM 'day' dd 'at' HH:mm");

    public static void main(String[] args) {

        /******************************Création du tableau de dates à insérer dans le fichier xml ***********************************/
        try {
            String t0 = "year 01 month 05 day 03 at 00:00";


            //String t0= SimulationManager.getInstance().getReleaseManager().getTimeBeginning();
            System.out.println("t0: " + t0);
            System.out.println("temps initial: " + t0);
            Date date_start = (Date) INPUT_DATE_FORMAT.parse(t0);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date_start);
            System.out.println("calendar contient: " + cal.getTime());
            //int frequency= SimulationManager.getInstance().getReleaseManager().getReleaseFrequency();
            int frequency = 2;
            Double Days = Math.ceil(360 / frequency);
            int nbDays = Days.intValue();

            String[] dates = new String[nbDays];
            String timeAsStr;
            for (int i = 0; i < nbDays; i++) {
                timeAsStr = INPUT_DATE_FORMAT.format(cal.getTime());
                dates[i] = timeAsStr;
                cal.add(Calendar.DATE, frequency);
                System.out.println("date_string: " + i + " = " + dates[i]); // tableau de string                
            }
            String event="";
            for (int k=0; k<dates.length;k++){
                event = event.concat("\"");
                event = event.concat(dates[k]);
                event = event.concat("\" ");       
            }
            System.out.println(event);
        } catch (ParseException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*******************************************************************************************************************/
        /*************************************injection du tableau dans fichier xml*********************************/
        // 1) copie du fichier xml
        // 2) j'insère le block release.stain
        // 3) j'insère les parties de ichthyop selon le tableau de dates
        // 4) je retourne le noueau fichier
        // 1)
        //File evolFile = new File(SimulationManager.getInstance().getConfigurationFile().getPath());
        // 2)
        // boolean removeFrequency = getXParameter(BlockType.OPTION, "release.schedule", "release_frequency").removeAttribute("release_frequency");
        // boolean removeMinDepth =  getXParameter(BlockType.OPTION, "release.schedule", "depth_min").removeAttribute("depth_min");
        //boolean removeMaxDepth =  getXParameter(BlockType.OPTION, "release.schedule", "depth_max").removeAttribute("depth_max");
        //if(removeFrequency && removeMaxDepth && removeMinDepth){
        //    System.out.println("Suppression exces du fichier xml ok");
        //  }
        File file = new File("cfg/2011_12_20_mariem_config_ichthyop_evol_roms3d.xml");
        ConfigurationFile cfgTemplate = new ConfigurationFile(Template.getTemplateURL("cfg-generic.xml"));
        ConfigurationFile Mycfg = new ConfigurationFile(file);
        try {
            Mycfg.load();
            System.out.println("load ok");
        } catch (Exception ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
      
        /*try {
            getSimulationManager().setConfigurationFile(file);
            getSimulationManager().setup();
            getLogger().info("Setup [OK]");
            getSimulationManager().init();
            getLogger().info("Initialization [OK]");
        } catch (Exception ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        // File EvolFile= new File("cfg/2011_12_20_mariem_config_ichthyop_evol_roms3d.xml");

        if (!Mycfg.containsBlock(BlockType.OPTION, "release.schedule")) {
            System.out.println("le test marche");
            
            Mycfg.addBlock(cfgTemplate.getBlock(BlockType.OPTION, "release.schedule").detach());
            Mycfg.getBlock(BlockType.OPTION, "release.schedule").setTreePath("Advanced/Release/Schedule");     
            System.out.println("Happy !!!"+ Mycfg.getBlock(BlockType.OPTION, "release.schedule").getKey());
            try {
                Mycfg.write(new FileOutputStream(Mycfg.getFile()));
            } catch (IOException ex) {
                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            }    
        }
        else{
            System.out.println("T a cote de la plaque.");
        }

        //     Content param_type = null;
        //     elt.addContent(newContent);
        //     XParameter events= new XParameter(elt);

        //     getXBlock(BlockType.OPTION,"release.schedule").addXParameter();

    }

    public void setupPerformed(SetupEvent e) throws Exception {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void initializePerformed(InitializeEvent e) throws Exception {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    private static XParameter getXParameter(BlockType blockType, String blockKey, String key) {
        return getSimulationManager().getParameterManager().getConfigurationFile().getXParameter(blockType, blockKey, key);
    }

    private static XBlock getXBlock(BlockType blockType, String blockKey) {
        return getSimulationManager().getParameterManager().getConfigurationFile().getBlock(blockType, blockKey);
    }
}
