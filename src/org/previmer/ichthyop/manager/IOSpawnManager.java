package org.previmer.ichthyop.manager;

import java.io.File;
import java.util.logging.Logger;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.event.SetupEvent;
import java.io.IOException;
import java.util.logging.Level;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.NetcdfFile;
import org.previmer.ichthyop.arch.ITracker;
import java.util.ArrayList;
import java.util.List;
import org.previmer.ichthyop.evol.AbstractEvol;
import org.previmer.ichthyop.io.IOTools;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author mariem
 */
public class IOSpawnManager extends AbstractManager{

    private final static String block_key = "app.output";
    final private static IOSpawnManager IOSpawnManager = new IOSpawnManager();
    ;
    private int dt_record;
    private int i_record;
    private List<ITracker> trackers;
    /**
     * Object for creating/writing netCDF files.
     */
    private static NetcdfFileWriteable ncOut;
    private NetcdfFile ncIn;
    private String basename;
    private String last_name= null;
    public int count=0;

    public static IOSpawnManager getInstance() {
        return IOSpawnManager;
    }

    public String getFileLocation() {
        return basename;
    }

    public String getParameter(String key) {
        return getSimulationManager().getParameterManager().getParameter(block_key, key);
    }

    private String makeFileLocation() throws IOException {

        String filename = IOTools.resolvePath(getParameter("output_path"));
        if (!getParameter("file_prefix").isEmpty()) {
            filename += getParameter("file_prefix") + "_";
        }
        filename += getSimulationManager().getId() + ".nc";
        File file = new File(filename);
        try {
            IOTools.makeDirectories(file.getAbsolutePath());
            file.createNewFile();
            file.delete();
        } catch (Exception ex) {
            IOException ioex = new IOException("{Ouput} Failed to create NetCDF file " + filename + " ==> " + ex.getMessage());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        basename = filename;
        return filename + ".part";
    }

    /**
     * Closes the NetCDF file.
     */
    private void close() {
        try {
            ncOut.close();
            String strFilePart = ncOut.getLocation();
            String strFileBase = strFilePart.substring(0, strFilePart.indexOf(".part"));
            File filePart = new File(strFilePart);
            File fileBase = new File(strFileBase);
            filePart.renameTo(fileBase);
            getLogger().info("Closed NetCDF output file.");
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Problem closing the NetCDF output file ==> " + ex.toString());
        }
    }

    public void nextStepTriggered(NextStepEvent e) throws Exception {

        if (e.isInterrupted()) {
            return;
        }
        TimeManager timeManager = e.getSource();
        if (((timeManager.getTime() - timeManager.get_tO()) % dt_record) == 0) {
            writeToNetCDF(i_record++);
        }
    }

    
    private void writeToNetCDF(int i_record) {
        getLogger().info("NetCDF output file, writing record " + i_record + " - time " + getSimulationManager().getTimeManager().timeToString());
        List<ITracker> errTrackers = new ArrayList();
        for (ITracker tracker : trackers) {
            try {
                tracker.track();
            } catch (Exception ex) {
                errTrackers.add(tracker);
                getSimulationManager().getDataset().removeRequiredVariable(tracker.short_name(), tracker.getClass());
                StringBuffer sb = new StringBuffer();
                sb.append("Error tracking variable \"");
                sb.append(tracker.short_name());
                sb.append("\" == >");
                sb.append(ex.toString());
                sb.append("\n");
                sb.append("The variable will no longer be recorded in the NetCDF output file.");
                getLogger().log(Level.WARNING, sb.toString());
                continue;
            }
            /* Exclude tracker that caused error */
            if (!writeTrackerToNetCDF(tracker, i_record)) {
                errTrackers.add(tracker);
            }
        }
        /* Remove trackers that caused error */
        // trackers.removeAll(errTrackers);
    }

    /**
     * Writes data to the specified variable.
     *
     * @param field a Field, the variable to be written
     * @param origin an int[], the offset within the variable to start writing.
     * @param array the Array that will be written; must be same type and
     * rank as Field
     */
    private boolean writeTrackerToNetCDF(ITracker tracker, int index) {
        try {
            ncOut.write(tracker.short_name(), tracker.origin(index), tracker.getArray());
        } catch (Exception ex) {
            getSimulationManager().getDataset().removeRequiredVariable(tracker.short_name(), tracker.getClass());
            StringBuffer sb = new StringBuffer();
            sb.append("Error writing ");
            sb.append(tracker.short_name());
            sb.append(" in the NetCDF output file == >");
            sb.append(ex.toString());
            sb.append("\n");
            sb.append("The variable will no longer be recorded in the NetCDF output file.");
            getLogger().log(Level.WARNING, sb.toString());
            return false;
        }
        return true;
    }

    /**
     * Adds the specified variable to the NetCDF file.
     *
     * @param field a Field, the variable to be added in the file.
     */
    private void addVar2NcOut(ITracker tracker) {

        ncOut.addVariable(tracker.short_name(), tracker.type(), tracker.dimensions());
        try {
            if (null != tracker.long_name()) {
                ncOut.addVariableAttribute(tracker.short_name(), "long_name", tracker.long_name());
            }
            if (null != tracker.unit()) {
                ncOut.addVariableAttribute(tracker.short_name(), "unit", tracker.unit());
            }
            if (tracker.attributes() != null) {
                for (Attribute attribute : tracker.attributes()) {
                    ncOut.addVariableAttribute(tracker.short_name(), attribute);
                }
            }
        } catch (Exception ex) {
            // do nothing, attributes have minor importance
        }
    }

   
    public void setupPerformed(SetupEvent e) throws Exception {

        /* Create the NetCDF writeable object */
        ncOut = NetcdfFileWriteable.createNew("");
        ncOut.setLocation(makeFileLocation());

        if (SimulationManager.getInstance().getTimeManager().getTime()
                == SimulationManager.getInstance().getTimeManager().get_tO()) {
            // A compléter
            writeToNetCDF(i_record);
        } else {
            copy();
            clean();
        }
        // to verify
        writeToNetCDF(i_record);
        close();
        last_name = basename;
    }
    
    public int setupPerformed() throws Exception {

        /* Create the NetCDF writeable object */
        ncOut = NetcdfFileWriteable.createNew("");
        ncOut.setLocation(makeFileLocation());

        if (SimulationManager.getInstance().getTimeManager().getTime()
                == SimulationManager.getInstance().getTimeManager().get_tO()) {
            // A compléter
            writeToNetCDF(i_record);
        } else {
            copy();
            clean();
        }
        // to verify
        writeToNetCDF(i_record);
        close();
        return count;
    }

    public int[] canBeParent(){
        int[] candidate=null;
        try {
            ncIn = NetcdfFile.open(getLast_name());
        } catch (IOException ex) {
            Logger.getLogger(IOSpawnManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return candidate;
        
    }
    
    private void copy() {
        try {
            ncIn = NetcdfFile.open(getLast_name());
            /* copie du fichier de la ponte à t-1, dans celui de la ponte à t*/
            ncOut = (NetcdfFileWriteable) ncIn;
            /*  vérification copie effectuée    */
            if (!ncOut.equals(ncIn)) {
                System.out.println("failed to copy the netcdf file.");
                // je dois copier vecteur par vecteur
            }
        } catch (IOException ex) {
            Logger.getLogger(IOSpawnManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clean() {
        try {
            int[] dead = findIndexDead(readMortality(basename));
            int[] outOfAge = this.oldNotRecruited();
           
            for (int i = 0; i < dead.length; i++) {
                ncOut.removeDimension(null, String.valueOf(dead[i])); // dimname => l'individu en question
                count++;
            }
            for (int i = 0; i < outOfAge.length; i++) {
                ncOut.removeDimension(null, String.valueOf(outOfAge[i]));
                count++;
            }

        } catch (IOException ex) {
            Logger.getLogger(IOSpawnManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int[] oldNotRecruited(){
        Array age = null;
        Array NotRecruited = null; 
        int[] oldNotRecruited = null;
        int k=0;
        try {
            age = readAge(last_name);
            NotRecruited= readRecruitment(last_name);
        } catch (IOException ex) {
            Logger.getLogger(IOSpawnManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        int[] old = findIndexOld(age);
        int[] notRecruit= findIndexNotRecruited(NotRecruited);
        
        for (int i = 0; i < notRecruit.length; i++) {
            int ind = notRecruit[i];
            int j = 0;
            while (ind > old[j] && j < old.length) {
                j++;
            }
            if (j < old.length) {
                oldNotRecruited[k] = ind;
            }
        }        
        // Les tableaux sont triés car comportent les indexs des individus lors de la lecture
        // pour éviter tout problème potentiel, fonction de tri, à réaliser plus tard.
        return oldNotRecruited;
    }
    public Array readMortality(String File) throws IOException {

        Array death;
        NetcdfFile temp = NetcdfDataset.openFile(File, null);
        try {
            death = ncIn.findVariable("mortality").read();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading last mortality. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        temp.close();
        death = (Array) death.copyTo1DJavaArray();
        return death;        
    }
        
    public int[] findIndexDead(Array death){
        int[] index = null;
        Integer buff= null;
        int j=0;    //compteur dans index[]
        while(death.hasNext()){
            buff= Integer.class.cast(death.getIndex());
            if(death.getInt(buff.intValue()) != 0){
                index[j] = buff.intValue();
                j++;
            }
            death.getIndex().incr();
        }
        return index;
    }
    
    public Array readRecruitment(String File) throws IOException {

        Array recruitment;
        NetcdfFile temp = NetcdfDataset.openFile(File, null);
        try {
            recruitment = ncIn.findVariable("recruitment").read();      // A vérifier le nom dans le fichier netcdf
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading recruitment. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        temp.close();
        recruitment = (Array) recruitment.copyTo1DJavaArray();
        return recruitment;        
    }
    
    
    public int[] findIndexNotRecruited(Array recruitment){
        int[] index = null;
       Integer buff = null;
        int j=0;    //compteur dans index[]
        while(recruitment.hasNext()){
            buff= Integer.class.cast(recruitment.getIndex());
            if(recruitment.getInt(buff.intValue()) == 0){
                index[j] = buff.intValue();
                j++;
            }
            recruitment.getIndex().incr();
        }
        return index;
    }
    
    public Array readAge(String File) throws IOException {

        Array age;
        NetcdfFile temp = NetcdfDataset.openFile(File, null);
        try {
            age = ncIn.findVariable("age").read();      
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading age. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        temp.close();
        age = (Array) age.copyTo1DJavaArray();
        return age;        
    }
    
    
    public int[] findIndexOld(Array age){
        long limit= SimulationManager.getInstance().getTimeManager().getTransportDuration();
        int[] index = null;
        Integer buff= null;
        int j=0;    //compteur dans index[]
        while(age.hasNext()){
            buff= Integer.class.cast(age.getIndex());
            if(age.getInt(buff.intValue()) > limit){
                index[j] = buff.intValue();
                j++;
            }
            age.getIndex().incr();
        }
        return index;
    }
    
    public int[] findIndexCanBeParent(Array age){
        AbstractEvol param= new AbstractEvol();
        int min = param.getAge_min();
        int max = param.getAge_max();
        int[] index = null;
        Integer buff= null;
        int j=0;    //compteur dans index[]
        while( age.hasNext() ){
            buff= Integer.class.cast(age.getIndex());
            if( age.getInt(buff.intValue()) >= min && (max >= age.getInt(buff.intValue()))){
                index[j] = buff.intValue();
                j++;
            }
            age.getIndex().incr();
        }
        return index;
    }

    /**
     * @return the last_name
     */
    private String getLast_name() {
        // to do 
        return last_name;
    }

    public void initializePerformed(InitializeEvent e) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
