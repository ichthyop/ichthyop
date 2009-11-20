/*
 * Copyright 2008 (C) by Philippe Verley.
 * Philippe.Verley@ird.fr
 * 
 * This file is part of AEBUS project.
 * http://www.eco-up.ird.fr/projects/aebus/
 * 
 * AEBUS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * AEBUS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with AEBUS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.ui;

/**
 *
 * @author Philippe Verley
 */
public class DrawableAvatar implements Comparable {

    private int index;
    private double x;
    private double y;
    private int width;
    private int height;
    private double zOrder;
    private double position;
    private boolean visible;

    public DrawableAvatar(int index,
            double x, double y, int width, int height,
            double position, double zOrder, boolean visible) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.position = position;
        this.zOrder = zOrder;
        this.visible = visible;
    }

    public DrawableAvatar(int index,
            double x, double y, int width, int height,
            double position, double zOrder) {
        this(index, x, y, width, height, position, zOrder, true);

    }

    public DrawableAvatar(int index,
            double x, double y, int width, int height) {
        this(index, x, y, width, height, Double.NaN, 1);
    }

    public DrawableAvatar(int index, boolean visible) {
        this(index, 0, 0, 0, 0, Double.NaN, 0, false);
    }

    public int compareTo(Object o) {
        double zOrder2 = ((DrawableAvatar) o).zOrder;

        if (zOrder < zOrder2) {
            return -1;
        } else if (zOrder > zOrder2) {
            return 1;
        }
        return 0;
    }

    public double getPosition() {
        return position;
    }

    public double getAlpha(float alphaLevel) {
        //return 1.d;
        return zOrder * alphaLevel;
    }

    public int getHeight() {
        return height;
    }

    public int getIndex() {
        return index;
    }

    public int getWidth() {
        return width;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isVisible() {
        return visible;
    }
}
