/*
 * Copyright (C) 2012 gandres
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
 * @author gandres
 */
public class DebParticleLayer extends ParticleLayer {

    /*
     * DEB PARAMETERS
     */
    private double E; // Reserve
    private double V; // Structure
    private double E_R; // Reproduction buffer
    private double E_H;  // Cumulated energy invested into dev.
    private double Lw;  // Physical length
    private double dryW;  // dry weight (g)
    private double wetW;  // wet weight (g)
    private double F;

    public DebParticleLayer(IParticle particle) {
        super(particle);
    }

    @Override
    public void init() {
        // Nothing to do
    }

    public double getE() {
        return E;
    }

    public void setE(double E) {
        this.E = E;
    }

    public double getV() {

        return V;
    }

    public void setV(double V) {
        this.V = V;
    }

    public double getE_R() {
        return E_R;
    }

    public void setE_R(double E_R) {
        this.E_R = E_R;
    }

    public double getE_H() {
        return E_H;
    }

    public void setE_H(double E_H) {
        this.E_H = E_H;
    }

    public double getLw() {
        return Lw;
    }

    public void setLw(double Lw) {
        this.Lw = Lw;
    }

    public double getdryW() {
        return dryW;
    }

    public void setdryW(double dryW) {
        this.dryW = dryW;
    }

    public double getwetW() {
        return wetW;
    }

    public void setwetW(double wetW) {
        this.wetW = wetW;
    }

    public double getF() {
        return wetW;
    }

    public void setF(double F) {
        this.F = F;
    }

}
