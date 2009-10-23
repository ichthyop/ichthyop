/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public enum TypeZone {

    RELEASE,
    RECRUITMENT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
