/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.event.LastStepEvent;
import fr.ird.ichthyop.event.NextStepEvent;
import fr.ird.ichthyop.Simulation;
import fr.ird.ichthyop.TypeZone;
import fr.ird.ichthyop.io.BlockType;
import fr.ird.ichthyop.arch.IOutputManager;
import fr.ird.ichthyop.arch.IStep;
import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.io.XBlock;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.nc2.NetcdfFileWriteable;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.ITracker;
import fr.ird.ichthyop.event.LastStepListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 *
 * @author pverley
 */
public class OutputManager implements IOutputManager, LastStepListener {

    private static final String CLASS_NAME = "class_name";
    final private static OutputManager outputManager = new OutputManager();
    private final static String block_key = "app.output";
    private int dt_record;
    private NCDimFactory dimensionFactory;
    private int i_record;
    private boolean isRecord;
    private int record_frequency;
    /**
     * Object for creating/writing netCDF files.
     */
    private static NetcdfFileWriteable ncOut;
    /**
     *
     */
    private List<ITracker> trackers;

    private OutputManager() {
        isRecord = ICFile.getInstance().getBlock(BlockType.OPTION, block_key).isEnabled();
        if (isRecord()) {
            getSimulation().getStep().addNextStepListener(this);
            getSimulation().getStep().addLastStepListener(this);
        }
    }

    public static OutputManager getInstance() {
        return outputManager;
    }

    public NCDimFactory getDimensionFactory() {
        if (null == dimensionFactory) {
            dimensionFactory = new NCDimFactory();
        }
        return dimensionFactory;
    }

    public String getParameter(String key) {
        return getSimulation().getParameterManager().getValue(block_key, key);
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }

    private String getPathName() {
        return getParameter("directory_out") + getParameter("output_filename") + ".nc";
    }

    private boolean isRecord() {
        return isRecord;
    }

    public void setUp() {
        
        if (isRecord()) {
            try {
                ncOut = NetcdfFileWriteable.createNew("");
                record_frequency = Integer.valueOf(getParameter("record_frequency"));
                ncOut.setLocation(getPathName());
                addTrackers();
                ncOut.create();
            } catch (IOException ex) {
                Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            Logger.getAnonymousLogger().info("Created output file : " + ncOut.getLocation());
        }
    }

    public void init() {
        i_record = 0;
        dt_record = record_frequency * getSimulation().getStep().get_dt();
    }

    /**
     * Closes the NetCDF file.
     */
    private void close() {
        try {
            ncOut.close();
            System.out.println("close ncOut");
        } catch (IOException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addTrackers() {
        trackers = new ArrayList();
        for (XBlock xtrack : getXTrackers()) {
            if (xtrack.isEnabled()) {
                ITracker tracker = createTracker(xtrack);
                trackers.add(tracker);
                addVar2NcOut(tracker);
            }
        }
    }

    private ITracker createTracker(XBlock xtracker) {
        try {
            return (ITracker) Class.forName(xtracker.getParameter(CLASS_NAME).getValue()).newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void nextStepTriggered(NextStepEvent e) {

        IStep step = e.getSource();
        //Logger.getAnonymousLogger().info(step.getTime() + " " + step.get_tO());
        if (((step.getTime() - step.get_tO()) % dt_record) == 0) {
            write(i_record++);
        }
    }

    private void write(int i_record) {
        Logger.getAnonymousLogger().info("  --> record " + i_record + " - time " + getSimulation().getStep().timeToString());
        for (ITracker tracker : trackers) {
            tracker.track();
            write2NcOut(tracker, i_record);
        }
    }

    /**
     * Writes data to the specified variable.
     *
     * @param field a Field, the variable to be written
     * @param origin an int[], the offset within the variable to start writing.
     * @param array the Array that will be written; must be same type and
     * rank as Field
     */
    private void write2NcOut(ITracker tracker, int index) {
        try {

            ncOut.write(tracker.short_name(), tracker.origin(index), tracker.getArray());
        } catch (IOException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Adds the specified variable to the NetCDF file.
     *
     * @param field a Field, the variable to be added in the file.
     */
    private void addVar2NcOut(ITracker tracker) {

        ncOut.addVariable(tracker.short_name(), tracker.type(), tracker.dimensions());
        ncOut.addVariableAttribute(tracker.short_name(), "long_name",
                tracker.long_name());
        ncOut.addVariableAttribute(tracker.short_name(), "unit", tracker.unit());
        for (Attribute attribute : tracker.attributes()) {
            ncOut.addVariableAttribute(tracker.short_name(), attribute);
        }
    }

    public XBlock getXTracker(String key) {
        return ICFile.getInstance().getBlock(BlockType.TRACKER, key);
    }

    public Collection<XBlock> getXTrackers() {
        Collection<XBlock> collection = new ArrayList();
        for (XBlock block : ICFile.getInstance().getBlocks(BlockType.TRACKER)) {
            collection.add(block);

        }
        return collection;
    }

    public void lastStepOccurred(LastStepEvent e) {
        write(i_record);
        close();
    }

    public class NCDimFactory {

        private Dimension time, drifter;
        private Hashtable<TypeZone, Dimension> zoneDimension = new Hashtable();
        private Hashtable<String, Dimension> dimensions = new Hashtable();

        private NCDimFactory() {
            createMainDimensions();
            fillHashtable();
        }

        private void createMainDimensions() {
            time = ncOut.addUnlimitedDimension("time");
            drifter = ncOut.addDimension("drifter", getSimulation().getReleaseManager().getNbParticles());
            for (TypeZone type : TypeZone.values()) {
                String name = type.toString() + "_zone";
                zoneDimension.put(type, ncOut.addDimension(name, getSimulation().getZoneManager().getZones(type).size()));
            }
        }

        private void fillHashtable() {
            dimensions.put(time.getName(), time);
            dimensions.put(drifter.getName(), drifter);
            for (Dimension dim : zoneDimension.values()) {
                dimensions.put(dim.getName(), dim);
            }
        }

        public Dimension addDimension(Dimension dim) {
            if (dimensions.containsKey(dim.getName())) {
                if (dim.getLength() != dimensions.get(dim.getName()).getLength()) {
                    throw new IllegalArgumentException("Variable name (" + dim.getName() + ") has already been defined with a different length.");
                } else {
                    return dimensions.get(dim.getName());
                }
            } else {
                Dimension newDim = ncOut.addDimension(dim.getName(), dim.getLength());
                dimensions.put(newDim.getName(), newDim);
                return newDim;
            }
        }

        public Dimension getTimeDimension() {
            return time;
        }

        public Dimension getDrifterDimension() {
            return drifter;
        }

        public Dimension getZoneDimension(TypeZone type) {
            return zoneDimension.get(type);
        }
    }
}
