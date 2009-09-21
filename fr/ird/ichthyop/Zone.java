package fr.ird.ichthyop;

import fr.ird.ichthyop.arch.ISimulationAccessor;
import fr.ird.ichthyop.arch.ISimulation;
import java.util.ArrayList;

/** import AWT */
/**
 * <p>
 * This class defines a geographical area used by the program to locate
 * the release areas or the recruitment areas. It provides a tool to determine
 * whether any grid point belongs to the Zone.
 * </p>
 *
 * The geographical area is defined as followed :
 * Four geodesics demarcation points {lon, lat} P1, P2, P3 & P4 and
 * two depths depth1 & depth2.
 * <p>
 * Longitude is expressed in East degree and latitude in North degree.
 * Depths are positive Integers.
 * </p>
 * The area is first delimited by the polygon (P1 P2 P3 P4).
 * The points dont have to be in the water, but make sure they belong to the
 * geographical grid of the simulation.
 * The four demarcation points P1(plon1, plat1) to P4(plon4, plat4) must be
 * recorded in the clockwise or anticlockwise direction.
 * Then, another routine isolates the area of the polygone that is contained
 * between the bathymetric lines depth1 & depth2.
 * At last the user can choose the color
 * (Red[0, 255], Green[0, 255], Blue[0, 255]) of the area.

 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */
public class Zone implements ISimulationAccessor {

    private ArrayList<RhoPoint> polygon;
    private TypeZone type;
    /**
     * Lower bathymetric line [meter]
     */
    private int inshoreLine;
    /**
     * Higher bathymetric line [meter]
     */
    private int offshoreLine;
    /**
     *  [meter]
     */
    private int lowerDepth;
    /**
     *  [meter]
     */
    private int upperDepth;
    int index;

    public Zone(TypeZone type, int index) {
        this.type = type;
        this.index = index;
    }

    public void setInshoreLine(int inshoreLine) {
        this.inshoreLine = inshoreLine;
    }

    public void setOffshoreLine(int offshoreLine) {
        this.offshoreLine = offshoreLine;
    }

    public void setLowerDepth(int lowerDepth) {
        this.lowerDepth = lowerDepth;
    }

    public void setUpperDepth(int upperDepth) {
        this.upperDepth = upperDepth;
    }

    public void addPoint(RhoPoint point) {
        polygon.add(point);
    }

    private void init() {

        for (RhoPoint rhoPoint : polygon) {
            rhoPoint.geo2Grid();
        }
        polygon.add((RhoPoint) polygon.get(0).clone());
    }

    public boolean isPointInZone(RhoPoint point) {

        boolean isInZone = false;

        isInZone = isDepthInLayer(point.getDepth()) && isXYBetweenBathyLines(point.getX(), point.getY()) && isXYInPolygon(point.getX(), point.getY());

        return isInZone;
    }

    private boolean isDepthInLayer(double depth) {
        return depth >= lowerDepth & depth < upperDepth;
    }

    private boolean isXYBetweenBathyLines(double x, double y) {
        return (getSimulation().getDataset().getBathy((int) Math.round(x), (int) Math.round(y)) >
                inshoreLine &
                getSimulation().getDataset().getBathy((int) Math.round(x), (int) Math.round(y)) <
                offshoreLine);
    }

    private boolean isXYInPolygon(double x, double y) {

        boolean isInBox;
        int inc, crossings;
        double dx1, dx2, dxy;

        isInBox = true;
        crossings = 0;

        for (int k = 0; k < polygon.size(); k++) {
            if (polygon.get(k).getX() != polygon.get(k + 1).getX()) {
                dx1 = x - polygon.get(k).getX();
                dx2 = polygon.get(k + 1).getX() - x;
                dxy = dx2 * (y - polygon.get(k).getY()) - dx1 * (polygon.get(k + 1).getY() - y);
                inc = 0;
                if ((polygon.get(k).getX() == x) & (polygon.get(k).getY() == y)) {
                    crossings = 1;
                } else if (((dx1 == 0.) & (y >= polygon.get(k).getY())) |
                        ((dx2 == 0.) & (y >= polygon.get(k + 1).getY()))) {
                    inc = 1;
                } else if ((dx1 * dx2 > 0.) &
                        ((polygon.get(k + 1).getX() - polygon.get(k).getX()) * dxy >= 0.)) {
                    inc = 2;
                }
                if (polygon.get(k + 1).getX() > polygon.get(k).getX()) {
                    crossings += inc;
                } else {
                    crossings -= inc;
                }
            }
        }
        if (crossings == 0) {
            isInBox = false;
        }

        if (isInBox) {
            isInBox = (getSimulation().getDataset().getBathy((int) Math.round(x), (int) Math.round(y)) >
                    inshoreLine &
                    getSimulation().getDataset().getBathy((int) Math.round(x), (int) Math.round(y)) <
                    offshoreLine);
        }

        return (isInBox);
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }

    /**
     * Gets the type of zone, release or recruitment.
     *
     * @return an int characterizing the type of zone.
     * @see ichthyop.util.Constant for details about the labels characterizing
     * the type of zone.
     */
    public TypeZone getType() {
        return type;
    }

    /**
     * Gets the index of the zone.
     *
     * @return a positive integer, the index of the zone.
     */
    public int getIndex() {
        return index;
    }
    //---------- End of class
}
