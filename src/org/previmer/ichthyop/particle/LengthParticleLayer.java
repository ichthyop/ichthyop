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
package org.previmer.ichthyop.particle;

/**
 *
 * @author pverley
 */
public class LengthParticleLayer extends ParticleLayer {

    private double length;

    public LengthParticleLayer(IParticle particle) {
        super(particle);
    }

    @Override
    public void init() {
        length = 0;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public void incrementLength(double dlength) {
        length += dlength;
    }

}
