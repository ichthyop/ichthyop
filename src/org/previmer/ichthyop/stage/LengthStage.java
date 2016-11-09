/*
 * Copyright (C) 2014 pverley
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.stage;

import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.LengthParticleLayer;

/**
 *
 * @author pverley
 */
public class LengthStage extends AbstractStage {

    public LengthStage(BlockType blockType, String blockKey) {
        super(blockType, blockKey);
    }

    @Override
    public int getStage(IParticle particle) {
        double length = ((LengthParticleLayer) particle.getLayer(LengthParticleLayer.class)).getLength();
        return getStage((float) length);
    }

    @Override
    public int getStage(float length) {
        int stage = 0;
        for (float threshold : getThresholds()) {
            if (length >= threshold) {
                stage++;
            } else {
                break;
            }
        }
        return Math.max(0, stage-1);
    }

}
