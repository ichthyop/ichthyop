/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.io;

/**
 *
 * @author pverley
 */
public enum BlockType {

    OPTION("blue"),
    ACTION("green"),
    ZONE("brown"),
    RELEASE("violet"),
    DATASET("orange"),
    TRACKER("red");
    
    String color;

    BlockType(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public String getColor() {
        return color;
    }

}
