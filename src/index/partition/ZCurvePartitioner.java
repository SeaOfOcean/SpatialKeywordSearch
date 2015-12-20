/***********************************************************************
 * Copyright (c) 2015 by Regents of the University of Minnesota.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 *************************************************************************/
package index.partition;

import index.quadtree.QuadTreeRect;
import structure.GeoLocation;
import topology.Global;
import structure.POI;
import structure.Point;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Partition the space based on Z-curve.
 *
 * @author Ahmed Eldawy
 */
public class ZCurvePartitioner implements Serializable {
    protected static final int Resolution = Integer.MAX_VALUE;
    private static final Log LOG = LogFactory.getLog(ZCurvePartitioner.class);
    /**
     * MBR of the input file
     */
    protected QuadTreeRect mbr = new QuadTreeRect();
    /**
     * Upper bound of all partitions
     */
    protected long[] zSplits;

    /**
     * A default constructor to be able to dynamically instantiate it
     * and deserialize it
     */
    public ZCurvePartitioner() {
        mbr = new QuadTreeRect(90.0, -180.0, -90.0, 180.0);
    }

    public ZCurvePartitioner(double n, double w, double s, double e) {
        mbr = new QuadTreeRect(n, w, s, e);
    }

    public static ZCurvePartitioner createZcurveP() {
        ZCurvePartitioner zCurve = new ZCurvePartitioner();
        HashSet<Point> points = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Global.POI_FILE));
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                try {
                    POI poi = new POI(line);
                    points.add(new Point(poi.getLat(), poi.getLng()));
                } catch (Exception e) {
                    continue;
                }

            }
            zCurve.createFromPoints_inputNumSplits(points, Global.N_PARTITION);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zCurve;
    }

    /**
     * Computes the Z-order of a point relative to a containing rectangle
     *
     * @param mbr
     * @param x
     * @param y
     * @return
     */
    public static long computeZ(QuadTreeRect mbr, double x, double y) {
        int ix = (int) ((x - mbr.x1) / mbr.getWidth() * Resolution);
        int iy = (int) ((y - mbr.y1) / mbr.getHeight() * Resolution);
        return computeZOrder(ix, iy);
    }

    /**
     * Computes the Z-order (Morton order) of a two-dimensional point.
     *
     * @param x - integer value of the x-axis (cannot exceed Integer.MAX_VALUE)
     * @param y - integer value of the y-axis (cannot exceed Integer.MAX_VALUE)
     * @return
     */
    public static long computeZOrder(long x, long y) {
        long morton = 0;

        for (long bitPosition = 0; bitPosition < 32; bitPosition++) {
            long mask = 1L << bitPosition;
            morton |= (x & mask) << (bitPosition + 1);
            morton |= (y & mask) << bitPosition;
        }
        return morton;
    }

    public static long unComputeZOrder(long morton) {
        long x = 0, y = 0;
        for (long bitPosition = 0; bitPosition < 32; bitPosition++) {
            long mask = 1L << (bitPosition << 1);
            y |= (morton & mask) >> bitPosition;
            x |= (morton & (mask << 1)) >> (bitPosition + 1);
        }
        return (x << 32) | y;
    }



    public void createFromPoints_inputNumSplits(HashSet<Point> points, int numSplits) {
        long[] zValues = new long[points.size()];
        int i = 0;
        for (Point point : points) {
            zValues[i] = computeZ(mbr, point.x, point.y);
            i++;
        }
        createFromZValues_numSplits(zValues, numSplits);
    }

    protected void createFromZValues_numSplits(final long[] zValues, int numSplits) {
        Arrays.sort(zValues);
        this.zSplits = new long[numSplits];
        long maxZ = computeZ(mbr, mbr.x2, mbr.y2);
        for (int i = 0; i < numSplits; i++) {
            int quantile = (int) ((long) (i + 1) * zValues.length / numSplits);
            this.zSplits[i] = quantile == zValues.length ? maxZ : zValues[quantile];
        }
    }

    public int overlapPartition(double x, double y) {
        long zValue = computeZ(mbr, x, y);
        int partition = Arrays.binarySearch(zSplits, zValue);
        if (partition < 0)
            partition = -partition - 1;
        return partition;
    }

    public Set<Integer> overlapPartitions(GeoLocation[] boundaries) {
        HashSet<Integer> matchedGrpIds = new HashSet<Integer>();
        int groupId0 = overlapPartition(
                boundaries[0].getLatitudeInDegrees(),
                boundaries[0].getLongitudeInDegrees());
        matchedGrpIds.add(groupId0);
        int groupId1 = overlapPartition(
                boundaries[0].getLatitudeInDegrees(),
                boundaries[1].getLongitudeInDegrees());
        matchedGrpIds.add(groupId1);
        int groupId2 = overlapPartition(
                boundaries[1].getLatitudeInDegrees(),
                boundaries[0].getLongitudeInDegrees());
        matchedGrpIds.add(groupId2);
        int groupId3 = overlapPartition(
                boundaries[1].getLatitudeInDegrees(),
                boundaries[1].getLongitudeInDegrees());
        matchedGrpIds.add(groupId3);
        return matchedGrpIds;
    }

    public Set<Integer> overlapPartitions(Double lat, Double lng, Double withinDis) {
        GeoLocation geoLocation = GeoLocation.fromDegrees(lat, lng);
        GeoLocation[] boundaries = geoLocation.boundingCoordinates(withinDis);
        Set<Integer> matchedGrpIds = overlapPartitions(boundaries);
        matchedGrpIds.add(overlapPartition(lat, lng));
        return matchedGrpIds;
    }
}
