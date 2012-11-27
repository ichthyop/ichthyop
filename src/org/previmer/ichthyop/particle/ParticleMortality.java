/*
 *  Copyright (C) 2010 Philippe Verley <philippe dot verley at ird dot fr>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.particle;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public enum ParticleMortality {

    ALIVE(0),
    DEAD_COLD(1),
    OUT_OF_DOMAIN(2),
    BEACHED(4),
    OLD(3),
    STARVATION(5),
    DEAD_HOT(6);
    
    private int code = 0;

    ParticleMortality(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ParticleMortality getMortality(int code) {

        for (ParticleMortality mortality : ParticleMortality.values()) {
            if (code == mortality.getCode()) {
                return mortality;
            }
        }
        return ALIVE;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
