package org.previmer.ichthyop.manager;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.Level;
import org.previmer.ichthyop.Template;
import org.previmer.ichthyop.Version;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.exception.InvalidVersionNumberException;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.ConfigurationFile;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;
import org.previmer.ichthyop.ui.IchthyopApp;

/**
 *
 * @author pverley
 */
public class UpdateManager extends AbstractManager {

    private static final UpdateManager UPDATE_MANAGER = new UpdateManager();

    public static UpdateManager getInstance() {
        return UPDATE_MANAGER;
    }

    /*
     * Upgrade the configuration file to the application version.
     */
    public void upgrade() throws Exception {

        /*
         * Backup configuration file
         */
        File bak = new File(getConfigurationFile().getFile().getPath() + ".bak");
        try {
            IOTools.copyFile(getConfigurationFile().getFile(), bak);
            getLogger().log(Level.INFO, "[Configuration] A copy of the original configuration file has been saved as {0}", bak.getName());
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "[Configuration] Failed to backup the configuration file.", ex);
        }
        /*
         * Upgrade the configuration file to latest version
         */
        if (getConfigurationVersion().priorTo(Version.V31)) {
            u30bTo31();
        }
        if (getConfigurationVersion().priorTo(Version.V32)) {
            u31To32();
        }
        if (getConfigurationVersion().priorTo(Version.V33B)) {
            u32To33();
        }
        /*
         * Save the updated configuration file
         */
        Iterator<XBlock> it = getConfigurationFile().getAllBlocks().iterator();
        getSimulationManager().getParameterManager().cleanup();
        while (it.hasNext()) {
            getSimulationManager().getParameterManager().addBlock(it.next());
        }
        getSimulationManager().getParameterManager().save();
    }

    /*
     * Upgrade the 3.2 configuration file to 3.3
     */
    private void u32To33() throws Exception {
        //@TODO
        getLogger().warning("The automatic upgrade of the configuration file from v3.2 to v3.3 is not implemented yet. Ichthyop will update the version number but parts of your configuration may not work anymore. Sorry for the inconvenience.");
        /*
         * Update version number and date
         */
        getConfigurationFile().setVersion(getApplicationVersion());
        StringBuilder str = new StringBuilder(getConfigurationFile().getDescription());
        str.append("  --@@@--  ");
        str.append((new GregorianCalendar()).getTime());
        str.append(" File updated to version ");
        str.append(Version.V33B);
        str.append('.');
        getConfigurationFile().setDescription(str.toString());
    }

    /*
     * Upgrade the 3.1 configuration file to 3.2
     */
    private void u31To32() throws Exception {
        ConfigurationFile cfg32 = new ConfigurationFile(Template.getTemplateURL("cfg-generic.xml"));
        String treepath, newTreepath;
        /*
         * Update block action.lethal_temp
         */
        if (null != getXBlock(BlockType.ACTION, "action.lethal_temp")) {
            treepath = getXBlock(BlockType.ACTION, "action.lethal_temp").getTreePath();
            newTreepath = treepath.startsWith("Advanced")
                    ? "Advanced/Biology/Lethal temperatures"
                    : "Biology/Lethal temperatures";
            getConfigurationFile().removeBlock(BlockType.ACTION, "action.lethal_temp");
            getConfigurationFile().addBlock(cfg32.getBlock(BlockType.ACTION, "action.lethal_temp"));
            getXBlock(BlockType.ACTION, "action.lethal_temp").setTreePath(newTreepath);
        }
        /*
         * Update version number and date
         */
        getConfigurationFile().setVersion(getApplicationVersion());
        StringBuilder str = new StringBuilder(getConfigurationFile().getDescription());
        str.append("  --@@@--  ");
        str.append((new GregorianCalendar()).getTime());
        str.append(" File updated to version ");
        str.append(Version.V32);
        str.append('.');
        getConfigurationFile().setDescription(str.toString());
    }

    /*
     * Upgrade the 3.0b configuration file to 3.1
     */
    private void u30bTo31() throws Exception {
        ConfigurationFile cfg31 = new ConfigurationFile(Template.getTemplateURL("cfg-generic_3.1.xml"));
        String treepath, newTreepath;
        /*
         * Add the density_file parameter in the action.buoyancy block
         */
        if (null != getXBlock(BlockType.ACTION, "action.buoyancy")) {
            if (null == getXBlock(BlockType.ACTION, "action.buoyancy").getXParameter("density_file")) {
                getXBlock(BlockType.ACTION, "action.buoyancy").addXParameter(cfg31.getXParameter(BlockType.ACTION, "action.buoyancy", "density_file"));
            }
        }

        /*
         * Update the recruitment in zone block
         */
        if (null != getXBlock(BlockType.ACTION, "action.recruitment")) {
            getXParameter(BlockType.ACTION, "action.recruitment", "class_name").setValue(org.previmer.ichthyop.action.RecruitmentZoneAction.class.getCanonicalName());
            treepath = getXBlock(BlockType.ACTION, "action.recruitment").getTreePath();
            newTreepath = treepath.startsWith("Advanced")
                    ? "Advanced/Biology/Recruitment/In zones"
                    : "Biology/Recruitment/In zones";
            getXBlock(BlockType.ACTION, "action.recruitment").setTreePath(newTreepath);
            getConfigurationFile().updateBlockKey("action.recruitment.zone", getXBlock(BlockType.ACTION, "action.recruitment"));
        }
        /*
         * Add the recruitment in stain block
         */
        if (!getConfigurationFile().containsBlock(BlockType.ACTION, "action.recruitment.stain")) {
            getConfigurationFile().addBlock(cfg31.getBlock(BlockType.ACTION, "action.recruitment.stain").detach());
            treepath = getXBlock(BlockType.ACTION, "action.recruitment.zone").getTreePath();
            newTreepath = treepath.startsWith("Advanced")
                    ? "Advanced/Biology/Recruitment/In stain"
                    : "Biology/Recruitment/In stain";
            getXBlock(BlockType.ACTION, "action.recruitment.stain").setTreePath(newTreepath);
        }
        /*
         * Add the coastline behavior block
         */
        if (!getConfigurationFile().containsBlock(BlockType.OPTION, "app.transport")) {
            getConfigurationFile().addBlock(cfg31.getBlock(BlockType.OPTION, "app.transport").detach());
            treepath = getXBlock(BlockType.ACTION, "action.advection").getTreePath();
            newTreepath = treepath.startsWith("Advanced")
                    ? "Advanced/Transport/General"
                    : "Transport/General";
            getXBlock(BlockType.OPTION, "app.transport").setTreePath(newTreepath);
        }
        /*
         * Update MARS OpendDAP URL
         */
        if (null != getXBlock(BlockType.DATASET, "dataset.mars_2d_opendap")) {
            getXParameter(BlockType.DATASET, "dataset.mars_2d_opendap", "opendap_url").setValue("http://tds1.ifremer.fr/thredds/dodsC/PREVIMER-MANGA4000-MARS3DF1-FOR_FULL_TIME_SERIE");
        }
        if (null != getXBlock(BlockType.DATASET, "dataset.mars_3d_opendap")) {
            getXParameter(BlockType.DATASET, "dataset.mars_3d_opendap", "opendap_url").setValue("http://tds1.ifremer.fr/thredds/dodsC/PREVIMER-MANGA4000-MARS3DF1-FOR_FULL_TIME_SERIE");
        }
        /*
         * Update MARS Generelized Sigma parameters
         */
        if (null != getXBlock(BlockType.DATASET, "dataset.mars_3d")) {
            if (null == getXBlock(BlockType.DATASET, "dataset.mars_3d").getXParameter("field_var_hc")) {
                getXBlock(BlockType.DATASET, "dataset.mars_3d").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d", "field_var_hc"));
            }
            if (null == getXBlock(BlockType.DATASET, "dataset.mars_3d").getXParameter("field_var_a")) {
                getXBlock(BlockType.DATASET, "dataset.mars_3d").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d", "field_var_a"));
            }
            if (null == getXBlock(BlockType.DATASET, "dataset.mars_3d").getXParameter("field_var_b")) {
                getXBlock(BlockType.DATASET, "dataset.mars_3d").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d", "field_var_b"));
            }
        }
        if (null != getXBlock(BlockType.DATASET, "dataset.mars_3d_opendap")) {
            if (null == getXBlock(BlockType.DATASET, "dataset.mars_3d_opendap").getXParameter("field_var_hc")) {
                getXBlock(BlockType.DATASET, "dataset.mars_3d_opendap").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d_opendap", "field_var_hc"));
            }
            if (null == getXBlock(BlockType.DATASET, "dataset.mars_3d_opendap").getXParameter("field_var_a")) {
                getXBlock(BlockType.DATASET, "dataset.mars_3d_opendap").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d_opendap", "field_var_a"));
            }
            if (null == getXBlock(BlockType.DATASET, "dataset.mars_3d_opendap").getXParameter("field_var_b")) {
                getXBlock(BlockType.DATASET, "dataset.mars_3d_opendap").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d_opendap", "field_var_b"));
            }
        }
        /*
         * Update OPA NEMO parameters
         */
        if (null != getXBlock(BlockType.DATASET, "dataset.nemo")) {
            if (null == getXBlock(BlockType.DATASET, "dataset.nemo").getXParameter("field_var_e3u")) {
                getXBlock(BlockType.DATASET, "dataset.nemo").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.nemo", "field_var_e3u"));
            }
            if (null == getXBlock(BlockType.DATASET, "dataset.nemo").getXParameter("field_var_e3v")) {
                getXBlock(BlockType.DATASET, "dataset.nemo").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.nemo", "field_var_e3v"));
            }
        }
        /*
         * Add skip_sorting option in Dataset blocks
         */
        for (XBlock xblock : getConfigurationFile().getBlocks(BlockType.DATASET)) {
            if (null == xblock.getXParameter("skip_sorting")) {
                if (null != cfg31.getXParameter(BlockType.DATASET, xblock.getKey(), "skip_sorting")) {
                    xblock.addXParameter(cfg31.getXParameter(BlockType.DATASET, xblock.getKey(), "skip_sorting"));
                }
            }
        }
        /*
         * Fix lethal_temperature_larva value 12.0 instead of 12.O  
         */
        if (null != getXBlock(BlockType.ACTION, "action.lethal_temp")) {
            try {
                float f = Float.valueOf(getXParameter(BlockType.ACTION, "action.lethal_temp", "lethal_temperature_larva").getValue());
            } catch (NumberFormatException ex) {
                getXParameter(BlockType.ACTION, "action.lethal_temp", "lethal_temperature_larva").setValue("12.0");
            }
        }
        /*
         * Add grid_file parameter in ROMS configuration
         */
        if (null != getXBlock(BlockType.DATASET, "dataset.roms_2d")) {
            if (null == getXBlock(BlockType.DATASET, "dataset.roms_2d").getXParameter("grid_file")) {
                getXBlock(BlockType.DATASET, "dataset.roms_2d").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.roms_2d", "grid_file"));
            }
        }
        if (null != getXBlock(BlockType.DATASET, "dataset.roms_3d")) {
            if (null == getXBlock(BlockType.DATASET, "dataset.roms_3d").getXParameter("grid_file")) {
                getXBlock(BlockType.DATASET, "dataset.roms_3d").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.roms_3d", "grid_file"));
            }
        }
        /*
         * Update version number and date
         */
        getConfigurationFile().setVersion(Version.V31);
        StringBuilder str = new StringBuilder(getConfigurationFile().getDescription());
        str.append("  --@@@--  ");
        str.append((new GregorianCalendar()).getTime());
        str.append(" File updated to version ");
        str.append(Version.V31);
        str.append('.');
        getConfigurationFile().setDescription(str.toString());
    }

    private ConfigurationFile getConfigurationFile() {
        return getSimulationManager().getParameterManager().getConfigurationFile();
    }

    private XParameter getXParameter(BlockType blockType, String blockKey, String key) {
        return getSimulationManager().getParameterManager().getConfigurationFile().getXParameter(blockType, blockKey, key);
    }

    private XBlock getXBlock(BlockType blockType, String blockKey) {
        return getSimulationManager().getParameterManager().getConfigurationFile().getBlock(blockType, blockKey);
    }

    public boolean versionMismatch() throws Exception {
        Version appVersion = getApplicationVersion();
        Version cfgVersion = getConfigurationVersion();
        validateVersion(cfgVersion);
        try {
            return !(appVersion.getNumber().equals(cfgVersion.getNumber()))
                    || !(appVersion.getDate().equals(cfgVersion.getDate()));
        } catch (Exception ex) {
            return true;
        }
    }

    public Version getApplicationVersion() {
        return Version.VALUES[Version.VALUES.length - 1];
    }

    public Version getConfigurationVersion() {
        return getSimulationManager().getParameterManager().getConfigurationVersion();
    }

    @Override
    public void setupPerformed(SetupEvent e) throws Exception {
        // does nothing
    }

    @Override
    public void initializePerformed(InitializeEvent e) throws Exception {
        // does nothing
    }

    private void validateVersion(Version testedVersion) {

        for (Version version : Version.VALUES) {
            if (version.getNumber().equals(testedVersion.getNumber())) {
                return;
            }
        }
        throw new InvalidVersionNumberException("Version number " + testedVersion + " is not identified as a valid ichthyop version number.");
    }
}
