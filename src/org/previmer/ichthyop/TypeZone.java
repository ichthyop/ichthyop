/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop;

/**
 *
 * @author pverley
 */
public enum TypeZone {

    RELEASE(0),
    RECRUITMENT(1);
    
    private int code;

    TypeZone(int code) {
        this.code = code;
    }

    public int getCode() {return code;}

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
