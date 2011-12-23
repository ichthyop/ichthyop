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

    private static UpdateManager updateManager = new UpdateManager();

    public static UpdateManager getInstance() {
        return updateManager;
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
            getLogger().info("{Configuration} A copy of the original configuration file has been saved as " + bak.getName());
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "{Configuration} Failed to backup the configuration file.", ex);
        }
        /*
         * Upgrade the configuration file to latest version
         */
        if (getConfigurationVersion().priorTo(getApplicationVersion())) {
            u30bTo31();
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
     * Upgrade the 3.0b configuration file to 3.1
     */
    private void u30bTo31() throws Exception {
        ConfigurationFile cfg31 = new ConfigurationFile(Template.getTemplateURL("cfg-generic.xml"));
        /*
         * Update the recruitment in zone block
         */
        getXParameter(BlockType.ACTION, "action.recruitment", "class_name").setValue(org.previmer.ichthyop.action.RecruitmentZoneAction.class.getCanonicalName());
        String treepath = getXBlock(BlockType.ACTION, "action.recruitment").getTreePath();
        String newTreepath = treepath.startsWith("Advanced")
                ? "Advanced/Biology/Recruitment/In zones"
                : "Biology/Recruitment/In zones";
        getXBlock(BlockType.ACTION, "action.recruitment").setTreePath(newTreepath);
        getConfigurationFile().updateKey("action.recruitment.zone", getXBlock(BlockType.ACTION, "action.recruitment"));
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
            getXParameter(BlockType.DATASET, "dataset.mars_2d_opendap", "opendap_url").setValue("http://www.ifremer.fr/thredds/dodsC/PREVIMER-MANGA4000-MARS3DF1-FOR_FULL_TIME_SERIE");
        }
        if (null != getXBlock(BlockType.DATASET, "dataset.mars_3d_opendap")) {
            getXParameter(BlockType.DATASET, "dataset.mars_3d_opendap", "opendap_url").setValue("http://www.ifremer.fr/thredds/dodsC/PREVIMER-MANGA4000-MARS3DF1-FOR_FULL_TIME_SERIE");
        }
        /*
         * Update MARS Generelized Sigma parameters
         */
        if (null != getXBlock(BlockType.DATASET, "dataset.mars_3d")) {
            getXBlock(BlockType.DATASET, "dataset.mars_3d").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d", "field_var_hc"));
            getXBlock(BlockType.DATASET, "dataset.mars_3d").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d", "field_var_a"));
            getXBlock(BlockType.DATASET, "dataset.mars_3d").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d", "field_var_b"));
        }
        if (null != getXBlock(BlockType.DATASET, "dataset.mars_3d_opendap")) {
            getXBlock(BlockType.DATASET, "dataset.mars_3d_opendap").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d_opendap", "field_var_hc"));
            getXBlock(BlockType.DATASET, "dataset.mars_3d_opendap").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d_opendap", "field_var_a"));
            getXBlock(BlockType.DATASET, "dataset.mars_3d_opendap").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d_opendap", "field_var_b"));
        }
        /*
         * Update OPA NEMO parameters
         */
        if (null != getXBlock(BlockType.DATASET, "dataset.nemo")) {
            getXBlock(BlockType.DATASET, "dataset.nemo").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.nemo", "field_var_e3u"));
            getXBlock(BlockType.DATASET, "dataset.nemo").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.nemo", "field_var_e3v"));
        }
        /*
         * Add skip_sorting option in Dataset blocks
         */
        for (XBlock xblock : getConfigurationFile().getBlocks(BlockType.DATASET)) {
            if (null != cfg31.getXParameter(BlockType.DATASET, xblock.getKey(), "skip_sorting")) {
                xblock.addXParameter(cfg31.getXParameter(BlockType.DATASET, xblock.getKey(), "skip_sorting"));
            }
        }
        /*
         * Update version number
         */
        getConfigurationFile().setVersion(Version.v3_1.getNumber());
        StringBuilder str = new StringBuilder(getConfigurationFile().getDescription());
        str.append('\n');
        str.append((new GregorianCalendar()).getTime());
        str.append(" File updated to version ");
        str.append(Version.v3_1.getNumber());
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
        return !(appVersion.getNumber().equals(cfgVersion.getNumber()))
                || !(appVersion.getDate().equals(cfgVersion.getDate()));
    }

    public Version getApplicationVersion() {
        return new Version(
                IchthyopApp.getApplication().getContext().getResourceMap().getString("Application.version"),
                IchthyopApp.getApplication().getContext().getResourceMap().getString("Application.version.date"));
    }

    public Version getConfigurationVersion() {
        return identifyVersion(getSimulationManager().getParameterManager().getConfigurationVersion());
    }

    public void setupPerformed(SetupEvent e) throws Exception {
        // does nothing
    }

    public void initializePerformed(InitializeEvent e) throws Exception {
        // does nothing
    }

    private Version identifyVersion(String s) {
        if (null == s) {
            return Version.v3_0_beta;
        }
        for (Version version : Version.values()) {
            if (version.getNumber().equals(s)) {
                return version;
            }
        }
        throw new NullPointerException("Version number " + s + " is not identified as a valid ichthyop version number.");
    }
}
