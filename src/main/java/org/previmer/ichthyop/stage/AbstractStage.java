/*
 *
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software.
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full
 * description, see the LICENSE file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.previmer.ichthyop.stage;

import java.util.logging.Level;
import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.particle.IParticle;

/**
 *
 * @author pverley
 */
public abstract class AbstractStage extends SimulationManagerAccessor {

    private float[] thresholds;
    private String[] tags;

    private final BlockType blockType;
    private final String blockKey;

    public abstract int getStage(IParticle particle);
    public abstract int getStage(float value);

    AbstractStage(BlockType blockType, String blockKey) {
        this.blockType = blockType;
        this.blockKey = blockKey;
    }

    public void init() {

        // Load the stage tags
        tags = getSimulationManager().getParameterManager().getListParameter(blockType, blockKey, "stage_tags");

        // Load the stage thresholds
        String[] sThresholds = getSimulationManager().getParameterManager().getListParameter(blockType, blockKey, "stage_thresholds");
        thresholds = new float[sThresholds.length];
        for (int i = 0; i < sThresholds.length; i++) {
            thresholds[i] = Float.valueOf(sThresholds[i]);
        }

        // Make sure that tags.length == thresholds.length
        if (tags.length != thresholds.length) {
            getLogger().log(Level.WARNING, "Stages defined in block {0} has {1} tags and {2} thresholds, this is not consistent (we expect n tags and n thresholds). Please fix it.", new Object[]{blockKey, tags.length, thresholds.length});
        }
    }

    public int getNStage() {
        return tags.length;
    }

    public String getTag(int iStage) {
        return tags[iStage];
    }

    public float getThreshold(int iStage) {
        return thresholds[iStage];
    }

    float[] getThresholds() {
        return thresholds;
    }

}
