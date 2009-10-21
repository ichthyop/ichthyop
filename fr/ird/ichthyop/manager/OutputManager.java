/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.event.InitializeEvent;
import fr.ird.ichthyop.event.LastStepEvent;
import fr.ird.ichthyop.event.NextStepEvent;
import fr.ird.ichthyop.TypeZone;
import fr.ird.ichthyop.event.SetupEvent;
import fr.ird.ichthyop.io.BlockType;
import fr.ird.ichthyop.arch.IOutputManager;
import fr.ird.ichthyop.io.XBlock;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.nc2.NetcdfFileWriteable;
import fr.ird.ichthyop.arch.ITimeManager;
import fr.ird.ichthyop.arch.ITracker;
import fr.ird.ichthyop.event.LastStepListener;
import fr.ird.ichthyop.event.SetupListener;
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
public class OutputManager extends AbstractManager implements IOutputManager, LastStepListener, SetupListener {

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
        super();
        isRecord = getSimulationManager().getParameterManager().getBlock(BlockType.OPTION, block_key).isEnabled();
        if (isRecord()) {
            getSimulationManager().getTimeManager().addNextStepListener(this);
            getSimulationManager().getTimeManager().addLastStepListener(this);
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
        return getSimulationManager().getParameterManager().getParameter(block_key, key);
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
        dt_record = record_frequency * getSimulationManager().getTimeManager().get_dt();
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

        ITimeManager timeManager = e.getSource();
        //Logger.getAnonymousLogger().info(step.getTime() + " " + step.get_tO());
        if (((timeManager.getTime() - timeManager.get_tO()) % dt_record) == 0) {
            write(i_record++);
        }
    }

    private void write(int i_record) {
        Logger.getAnonymousLogger().info("  --> record " + i_record + " - time " + getSimulationManager().getTimeManager().timeToString());
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
        return getSimulationManager().getParameterManager().getBlock(BlockType.TRACKER, key);
    }

    public Collection<XBlock> getXTrackers() {
        Collection<XBlock> collection = new ArrayList();
        for (XBlock block : getSimulationManager().getParameterManager().getBlocks(BlockType.TRACKER)) {
            collection.add(block);

        }
        return collection;
    }

    public void lastStepOccurred(LastStepEvent e) {
        write(i_record);
        close();
    }

    public void setupPerformed(SetupEvent e) {

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

    public void initializePerformed(InitializeEvent e) {
        i_record = 0;
        dt_record = record_frequency * getSimulationManager().getTimeManager().get_dt();
    }

    public class NCDimFactory {

        private Dimension time, drifter;
        private Hashtable<TypeZone, Dimension> zoneDimension = new Hashtable();
        private Hashtable<String, Dimension> dimensions = new Hashtable();

        private void fillHashtable() {
            dimensions.put(time.getName(), time);
            dimensions.put(drifter.getName(), drifter);
            for (Dimension dim : zoneDimension.values()) {
                dimensions.put(dim.getName(), dim);
            }
        }

        public Dimension addDimension(Dimension dim) {
            if (dimensions.isEmpty()) {
                fillHashtable();
            }
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
            if (null == time) {
                time = ncOut.addUnlimitedDimension("time");
            }
            return time;
        }

        public Dimension getDrifterDimension() {
            if (null == drifter) {
                drifter = ncOut.addDimension("drifter", getSimulationManager().getReleaseManager().getNbParticles());
            }
            return drifter;
        }

        public Dimension getZoneDimension(TypeZone type) {
            if (null == zoneDimension.get(type)) {
                String name = type.toString() + "_zone";
                zoneDimension.put(type, ncOut.addDimension(name, getSimulationManager().getZoneManager().getZones(type).size()));
            }
            return zoneDimension.get(type);
        }
    }
}
