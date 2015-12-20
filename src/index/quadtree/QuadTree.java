// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/Index.quadtree/QuadTree.java,v $
// $RCSfile: QuadTree.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:31 $
// $Author: dietrick $
// 
// **********************************************************************

package index.quadtree;


import topology.Global;
import util.GsonUtil;
import util.MoreMath;
import util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * The QuadTree lets you organize objects in a grid, that redefines itself and
 * refines the gridding over locations where more objects are gathered.
 */
public class QuadTree<T> implements DataOrganizer<T>, Serializable {

    public QuadTreeNode<T> top;
    HashSet<Pair<Double, Double>> existedPoint = new HashSet<Pair<Double, Double>>();

    public QuadTree() {
        this(90.0, -180.0, -90.0, 180.0, Global.MAX_ITEMS, QuadTreeNode.NO_MIN_SIZE);
    }

    public QuadTree(int maxItems) {
        this(90.0, -180.0, -90.0, 180.0, maxItems);
    }

    public QuadTree(int maxItems, int minSize) {
        this(90.0, -180.0, -90.0, 180.0, maxItems, minSize);
    }

    public QuadTree(double north, double west, double south, double east, int maxItems) {
        this(north, west, south, east, maxItems, QuadTreeNode.NO_MIN_SIZE);
    }

    public QuadTree(int up, int left, int down, int right, int maxItems) {
        this(up, left, down, right, maxItems, QuadTreeNode.DEFAULT_MIN_SIZE);
    }

    public QuadTree(double north, double west, double south, double east, int maxItems,
                    double minSize) {
        top = new QuadTreeNode<T>(north, west, south, east, maxItems, minSize);
    }

    public static QuadTree load(String saveFile) {
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(saveFile));
//            String content = reader.readLine();
//            Gson gson = new Gson();
//            return gson.fromJson(content, QuadTree.class);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
        return (QuadTree) GsonUtil.load(saveFile, QuadTree.class);
    }

    /**
     * Add a object into the tree at a location.
     *
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @param obj the object to insert into the tree
     * @return true if the insertion worked.
     */
    public boolean put(double lat, double lon, T obj) {
        Pair<Double, Double> loc = new Pair<Double, Double>(lat, lon);
        if (existedPoint.contains(loc)) {
            return false;
        }
        existedPoint.add(loc);
        return top.put(lat, lon, obj);
    }

    /**
     * Remove a object out of the tree at a location.
     *
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @param obj the object to remove
     * @return the object removed, null if the object not found.
     */
    public T remove(double lat, double lon, T obj) {
        return top.remove(lat, lon, obj);
    }

    /**
     * Clear the tree.
     */
    public void clear() {
        top.clear();
    }

    /**
     * Get an object closest to a lat/lon.
     *
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return the object that was found.
     */
    public T get(double lat, double lon) {
        return top.get(lat, lon);
    }

    /**
     * Get an object closest to a lat/lon, within a maximum distance.
     *
     * @param lat            up-down location in QuadTree Grid (latitude, y)
     * @param lon            left-right location in QuadTree Grid (longitude, x)
     * @param withinDistance the maximum distance to get a hit, in decimal
     *                       degrees.
     * @return the object that was found, null if nothing is within the maximum
     * distance.
     */
    public T get(double lat, double lon, double withinDistance) {
        return top.get(lat, lon, withinDistance);
    }

    /**
     * Get all the objects within a bounding box.
     *
     * @param north top location in QuadTree Grid (latitude, y)
     * @param west  left location in QuadTree Grid (longitude, x)
     * @param south lower location in QuadTree Grid (latitude, y)
     * @param east  right location in QuadTree Grid (longitude, x)
     * @return Vector of objects.
     */
    public Collection get(double north, double west, double south, double east) {
        return get(north, west, south, east, null);
    }

    /**
     * Get all the objects within a bounding box, and return the objects in the
     * provided Collection.
     *
     * @param north      top location in QuadTree Grid (latitude, y)
     * @param west       left location in QuadTree Grid (longitude, x)
     * @param south      lower location in QuadTree Grid (latitude, y)
     * @param east       right location in QuadTree Grid (longitude, x)
     * @param collection a Collection to add objects to.
     * @return Collection of objects.
     */
    public Collection get(double north, double west, double south, double east,
                          Collection collection) {

        if (collection == null) {
            collection = new ArrayList<Object>();
        }
        // crossing the dateline, right?? Or at least containing the
        // entire earth. Might be trouble for VERY LARGE scales. The
        // last check is for micro-errors that happen to lon points
        // where there might be a smudge overlap for very small
        // scales.
        if (west > east || MoreMath.approximately_equal(west, east, .001)) {
            return top.get(north, west, south, 180, top.get(north, -180, south, east, collection));
        } else
            return top.get(north, west, south, east, collection);
    }

    public void save2File(String filename) {
//        Gson gson = new Gson();
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
//            writer.write(gson.toJson(top));
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        GsonUtil.save(top, filename);
    }
}