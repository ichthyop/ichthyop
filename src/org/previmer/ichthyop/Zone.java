package org.previmer.ichthyop;

import java.awt.Color;
import java.util.ArrayList;
import org.previmer.ichthyop.arch.IParticle;
import org.previmer.ichthyop.arch.IDataset;

/**
 * <p>
 * This class defines a geographical area used by the program to locate the
 * release areas or the recruitment areas. It provides a tool to determine
 * whether any grid point belongs to the Zone.
 * </p>
 *
 * The geographical area is defined as followed : at least three geodesic
 * demarcation points {lon, lat} P1, P2, P3, .., Pn and two depths depth1 &
 * depth2.
 * <p>
 * Longitude is expressed in East degree and latitude in North degree. Depths
 * are positive Integers.
 * </p>
 * The area is first delimited by the polygon (P1 P2 P3 P4 .. Pn). The points
 * don't have to be in the water, but make sure they belong to the geographical
 * grid of the simulation. The four demarcation points P1(plon1, plat1) to
 * P4(plon4, plat4) must be recorded in the clockwise or anticlockwise
 * direction. Then, another routine isolates the area of the polygon that is
 * contained between the bathymetric lines depth1 & depth2. At last the user can
 * choose the color (Red[0, 255], Green[0, 255], Blue[0, 255]) of the area.
 *
 * @author P.Verley (philippe.verley@ird.fr)
 * @version 3.3 2013/11/15
 */
public class Zone extends SimulationManagerAccessor {

    /**
     * A list of {@code GridPoint} that defines the a geographical area.
     */
    private final ArrayList<GridPoint> polygon;
    /**
     * The type of zone (release, recruitment, etc.)
     */
    private final TypeZone type;
    /**
     * Lower bathymetric line [meter]
     */
    private float inshoreLine;
    /**
     * Higher bathymetric line [meter]
     */
    private float offshoreLine;
    /**
     * [meter]
     */
    private float lowerDepth;
    /**
     * [meter]
     */
    private float upperDepth;
    /**
     * Zone index
     */
    int index;
    /*
     * Zone name in the configuration file
     */
    private final String key;
    /**
     * Zone color (RGB)
     */
    private Color color;
    /**
     * Whether to consider the (vertical) thickness of the zone. Not enabled
     * means that the zone covers all the water column.
     */
    private boolean enabledThickness;
    /**
     * Whether to enable the bathymetric mask.
     */
    private boolean enabledBathyMask;

    /**
     * Creates a new zone.
     *
     * @param type, the type of zone
     * @param key, the name of the zone
     * @param index, the index of the zone
     */
    public Zone(TypeZone type, String key, int index) {
        this.polygon = new ArrayList();
        this.type = type;
        this.key = key;
        this.index = index;
    }

    /**
     * Sets the color of the zone
     *
     * @param color, the color of the zone
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Returns the color of the zone.
     *
     * @return the color of the zone
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the name of the zone.
     * 
     * @return the name of the zone
     */
    public String getKey() {
        return key;
    }

    public void setThicknessEnabled(boolean enabled) {
        enabledThickness = enabled;
    }

    public void setBathyMaskEnabled(boolean enabled) {
        enabledBathyMask = enabled;
    }

    public void setInshoreLine(float inshoreLine) {
        this.inshoreLine = inshoreLine;
    }

    public void setOffshoreLine(float offshoreLine) {
        this.offshoreLine = offshoreLine;
    }

    public void setLowerDepth(float lowerDepth) {
        this.lowerDepth = lowerDepth;
    }

    public float getUpperDepth() {
        return upperDepth;
    }

    public float getLowerDepth() {
        return lowerDepth;
    }

    public void setUpperDepth(float upperDepth) {
        this.upperDepth = upperDepth;
    }

    public void addPoint(GridPoint point) {
        polygon.add(point);
    }

    public void init() throws Exception {

        for (GridPoint rhoPoint : polygon) {
            rhoPoint.geo2Grid();
            /* make sure the point belongs to the simulated domain */
            if (rhoPoint.getX() < 0 || rhoPoint.getY() < 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(getType());
                sb.append(" zone \"");
                sb.append(getKey());
                sb.append("\". Initialization error. Point [lon: ");
                sb.append(rhoPoint.getLon());
                sb.append(" ; lat: ");
                sb.append(rhoPoint.getLat());
                sb.append("] is out of domain.");
                throw new IllegalArgumentException(sb.toString());
            }
        }

        /* make sure the layer thickness is properly defined */
        if (enabledThickness) {
            if (lowerDepth < upperDepth) {
                StringBuilder sb = new StringBuilder();
                sb.append(getType());
                sb.append(" zone \"");
                sb.append(getKey());
                sb.append("\". Thickness error. Lower depth (");
                sb.append(lowerDepth);
                sb.append("m) must be deeper than upper depth (");
                sb.append(upperDepth);
                sb.append("m)");
                throw new IllegalArgumentException(sb.toString());
            }
        }

        /* make sure bathy mask is correctly defined */
        if (enabledBathyMask) {
            if (offshoreLine < inshoreLine) {
                StringBuilder sb = new StringBuilder();
                sb.append(getType());
                sb.append(" zone \"");
                sb.append(getKey());
                sb.append("\". Bathymetric mask error. Offshore line (");
                sb.append(offshoreLine);
                sb.append("m) must be deeper than inshore line (");
                sb.append(inshoreLine);
                sb.append("m)");
                throw new IllegalArgumentException(sb.toString());
            }
        }

        /* Closes the polygon adding first point as last point */
        polygon.add((GridPoint) polygon.get(0).clone());
    }

    public boolean isParticleInZone(IParticle particle) {

        boolean isInZone = true;
        if (!getSimulationManager().getDataset().isInWater(particle.getGridCoordinates())) {
            return false;
        }
        if (particle.getGridCoordinates().length > 2 && enabledThickness) {
            isInZone = isDepthInLayer(Math.abs(particle.getDepth()));
        }
        if (enabledBathyMask) {
            isInZone = isInZone && isXYBetweenBathyLines(particle.getX(), particle.getY());
        }
        isInZone = isInZone && isXYInPolygon(particle.getX(), particle.getY());

        return isInZone;
    }

    public double getArea() {

        IDataset dataset = getSimulationManager().getDataset();
        double area = 0.d;
        for (int i = 0; i < dataset.get_nx(); i++) {
            for (int j = 0; j < dataset.get_ny(); j++) {
                if (dataset.isInWater(i, j)) {
                    if (isGridPointInZone(i, j)) {
                        area += dataset.getdeta(j, i) * dataset.getdxi(j, i) * 1e-6;
                    }
                }
            }
        }
        return area;
    }

    private boolean isDepthInLayer(double depth) {
        return depth <= lowerDepth & depth >= upperDepth;
    }

    public boolean isGridPointInZone(double x, double y) {
        boolean isIn = true;
        if (!getSimulationManager().getDataset().isInWater(new double[]{x, y})) {
            return false;
        }
        if (enabledBathyMask) {
            isIn = isXYBetweenBathyLines(x, y);
        }
        return isIn && isXYInPolygon(x, y);
    }

    private boolean isXYBetweenBathyLines(double x, double y) {
        return (getSimulationManager().getDataset().getBathy((int) Math.round(x), (int) Math.round(y))
                > inshoreLine
                & getSimulationManager().getDataset().getBathy((int) Math.round(x), (int) Math.round(y))
                < offshoreLine);
    }

    private boolean isXYInPolygon(double x, double y) {

        int inc, crossings;
        double dx1, dx2, dxy;

        crossings = 0;

        for (int k = 0; k < polygon.size() - 1; k++) {
            if (polygon.get(k).getX() != polygon.get(k + 1).getX()) {
                dx1 = x - polygon.get(k).getX();
                dx2 = polygon.get(k + 1).getX() - x;
                dxy = dx2 * (y - polygon.get(k).getY()) - dx1 * (polygon.get(k + 1).getY() - y);
                inc = 0;
                if ((polygon.get(k).getX() == x) & (polygon.get(k).getY() == y)) {
                    crossings = 1;
                } else if (((dx1 == 0.) & (y >= polygon.get(k).getY()))
                        | ((dx2 == 0.) & (y >= polygon.get(k + 1).getY()))) {
                    inc = 1;
                } else if ((dx1 * dx2 > 0.)
                        & ((polygon.get(k + 1).getX() - polygon.get(k).getX()) * dxy >= 0.)) {
                    inc = 2;
                }
                if (polygon.get(k + 1).getX() > polygon.get(k).getX()) {
                    crossings += inc;
                } else {
                    crossings -= inc;
                }
            }
        }

        return (crossings != 0);
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

    /**
     * Gets the smallest x-coordinate of the demarcation points.
     *
     * @return a double, the x-coordinate of the demarcation point closest to
     * the grid origin.
     */
    public double getXmin() {

        double xmin = polygon.get(0).getX();
        for (int k = 0; k < polygon.size() - 1; k++) {
            xmin = Math.min(xmin, polygon.get(k).getX());
        }
        return xmin;
    }

    /**
     * Gets the smallest y-coordinate of the demarcation points.
     *
     * @return a double, the y-coordinate of the demarcation point closest to
     * the grid origin.
     */
    public double getYmin() {

        double ymin = polygon.get(0).getY();
        for (int i = 1; i < polygon.size() - 1; i++) {
            ymin = Math.min(ymin, polygon.get(i).getY());
        }
        return ymin;
    }

    /**
     * Gets the biggest x-coordinate of the demarcation points.
     *
     * @return a double, the x-coordinate of the demarcation point farthest from
     * the grid origin.
     */
    public double getXmax() {
        double xmax = polygon.get(0).getX();
        for (int i = 1; i < polygon.size() - 1; i++) {
            xmax = Math.max(xmax, polygon.get(i).getX());
        }
        return xmax;
    }

    /**
     * Gets the biggest y-coordinate of the demarcation points.
     *
     * @return a double, the y-coordinate of the demarcation point farthest from
     * the grid origin.
     */
    public double getYmax() {
        double ymax = polygon.get(0).getY();
        for (int i = 1; i < polygon.size() - 1; i++) {
            ymax = Math.max(ymax, polygon.get(i).getY());
        }
        return ymax;
    }

    @Override
    public String toString() {
        StringBuilder zoneStr = new StringBuilder(getType().toString());
        zoneStr.append(' ');
        zoneStr.append("zone ");
        zoneStr.append(getIndex());
        zoneStr.append('\n');
        zoneStr.append("  Polygon [");
        for (GridPoint point : polygon) {
            zoneStr.append(point.toString());
            zoneStr.append(" ");
        }
        zoneStr.append(']');
        zoneStr.append('\n');
        zoneStr.append("  Shore-lines (");
        zoneStr.append(inshoreLine);
        zoneStr.append("m, ");
        zoneStr.append(offshoreLine);
        zoneStr.append("m)\n  Depth-lines (");
        zoneStr.append(upperDepth);
        zoneStr.append("m, ");
        zoneStr.append(lowerDepth);
        zoneStr.append("m)");
        zoneStr.append('\n');
        zoneStr.append("  Area ");
        zoneStr.append((float) getArea());
        zoneStr.append(" km2");

        return zoneStr.toString();
    }
    //---------- End of class
}
