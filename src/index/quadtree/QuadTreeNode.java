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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/Index.quadtree/QuadTreeNode.java,v $
// $RCSfile: QuadTreeNode.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:32 $
// $Author: dietrick $
// 
// **********************************************************************

package index.quadtree;


import topology.Global;
import util.MoreMath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The QuadTreeNode is the part of the QuadTree that either holds children
 * nodes, or objects as leaves. Currently, the nodes that have children do not
 * hold items that span across children boundaries, since this was designed to
 * handle point data.
 */

public class QuadTreeNode<T> implements Serializable {

    public final static float NO_MIN_SIZE = -1;
    public final static float DEFAULT_MIN_SIZE = Global.DEFAULT_MIN_SIZE;
    static final long serialVersionUID = -6111633198469889444L;
    public Collection<QuadTreeLeaf<T>> items;
    public Collection<QuadTreeNode<T>> children;
    public int maxItems;
    public double minSize;
    public QuadTreeRect bounds;
    /**
     * Added to avoid problems when a node is completely filled with a single
     * point value.
     */
    protected boolean allTheSamePoint;
    protected double firstLat;
    protected double firstLon;

    /**
     * Constructor to use if you are going to store the objects in lat/lon
     * space, and there is really no smallest node size.
     *
     * @param north        northern border of node coverage.
     * @param west         western border of node coverage.
     * @param south        southern border of node coverage.
     * @param east         eastern border of node coverage.
     * @param maximumItems number of items to hold in a node before splitting
     *                     itself into four children and redispensing the items into them.
     */
    public QuadTreeNode(double north, double west, double south, double east, int maximumItems) {
        this(north, west, south, east, maximumItems, NO_MIN_SIZE);
    }

    /**
     * Constructor to use if you are going to store the objects in x/y space,
     * and there is a smallest node size because you don't want the nodes to be
     * smaller than a group of pixels.
     *
     * @param north        northern border of node coverage.
     * @param west         western border of node coverage.
     * @param south        southern border of node coverage.
     * @param east         eastern border of node coverage.
     * @param maximumItems number of items to hold in a node before splitting
     *                     itself into four children and redispensing the items into them.
     * @param minimumSize  the minimum difference between the boundaries of the
     *                     node.
     */
    public QuadTreeNode(double north, double west, double south, double east, int maximumItems,
                        double minimumSize) {
        bounds = new QuadTreeRect(north, west, south, east);
        maxItems = maximumItems;
        minSize = minimumSize;
        items = new ArrayList<QuadTreeLeaf<T>>();
    }

    /**
     * Return true if the node has children.
     */
    public boolean hasChildren() {
        return (children != null);
    }

    /**
     * This method splits the node into four children, and disperses the items
     * into the children. The split only happens if the boundary size of the
     * node is larger than the minimum size (if we care). The items in this node
     * are cleared after they are put into the children.
     */
    protected void split() {
        // Make sure we're bigger than the minimum, if we care,
        if (minSize != NO_MIN_SIZE) {
            if (MoreMath.approximately_equal(bounds.y2, bounds.y1, minSize)
                    && MoreMath.approximately_equal(bounds.x2, bounds.x1, minSize))
                return;
        } else {
            minSize = NO_MIN_SIZE;
        }
        double nsHalf = (bounds.y2 - (bounds.y2 - bounds.y1) / 2.0);
        double ewHalf = (bounds.x2 - (bounds.x2 - bounds.x1) / 2.0);
        children = new ArrayList<QuadTreeNode<T>>(4);

        children.add(new QuadTreeNode<T>(bounds.y2, bounds.x1, nsHalf, ewHalf, maxItems, minSize));
        children.add(new QuadTreeNode<T>(bounds.y2, ewHalf, nsHalf, bounds.x2, maxItems, minSize));
        children.add(new QuadTreeNode<T>(nsHalf, ewHalf, bounds.y1, bounds.x2, maxItems, minSize));
        children.add(new QuadTreeNode<T>(nsHalf, bounds.x1, bounds.y1, ewHalf, maxItems, minSize));
        Collection<QuadTreeLeaf> temp = new ArrayList<QuadTreeLeaf>(items);
        items.clear();

        for (QuadTreeLeaf leaf : temp) {
            put(leaf);
        }
    }

    /**
     * Get the node that covers a certain lat/lon pair.
     *
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return node if child covers the point, null if the point is out of
     * range.
     */
    protected QuadTreeNode<T> getChild(double lat, double lon) {
        if (bounds.pointWithinBounds(lat, lon)) {
            if (children != null) {
                for (QuadTreeNode<T> child : children) {
                    if (child.bounds.pointWithinBounds(lat, lon))
                        return child.getChild(lat, lon);
                }
            } else {
                return this; // no children, lat, lon here...
            }
        }
        return null;
    }

    /**
     * Add a object into the tree at a location.
     *
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @param obj object to add to the tree.
     * @return true if the put worked.
     */
    public boolean put(double lat, double lon, T obj) {
        return put(new QuadTreeLeaf<T>(lat, lon, obj));
    }

    /**
     * Add a QuadTreeLeaf into the tree at a location.
     *
     * @param leaf object-location composite
     * @return true if the pution worked.
     */
    public boolean put(QuadTreeLeaf<T> leaf) {
        if (children == null) {
            this.items.add(leaf);
            if (this.items.size() == 1) {
                this.allTheSamePoint = true;
                this.firstLat = leaf.latitude;
                this.firstLon = leaf.longitude;
            } else {
                if (this.firstLat != leaf.latitude || this.firstLon != leaf.longitude) {
                    this.allTheSamePoint = false;
                }
            }

            if (this.items.size() > maxItems && !this.allTheSamePoint) {
                split();
            }
            return true;
        } else {
            QuadTreeNode<T> node = getChild(leaf.latitude, leaf.longitude);
            if (node != null) {
                return node.put(leaf);
            }
        }
        return false;
    }

    /**
     * Remove a object out of the tree at a location.
     *
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @param obj the object to be removed.
     * @return the object removed, null if the object not found.
     */
    public T remove(double lat, double lon, T obj) {
        return remove(new QuadTreeLeaf<T>(lat, lon, obj));
    }

    /**
     * Remove a QuadTreeLeaf out of the tree at a location.
     *
     * @param leaf object-location composite
     * @return the object removed, null if the object not found.
     */
    public T remove(QuadTreeLeaf<T> leaf) {
        if (children == null) {
            // This must be the node that has it...
            for (QuadTreeLeaf<T> qtl : new ArrayList<QuadTreeLeaf<T>>(items)) {
                if (leaf.object == qtl.object) {
                    items.remove(qtl);
                    return qtl.object;
                }
            }
        } else {
            QuadTreeNode<T> node = getChild(leaf.latitude, leaf.longitude);
            if (node != null) {
                return node.remove(leaf);
            }
        }
        return null;
    }

    /**
     * Clear the tree below this node.
     */
    public void clear() {
        this.items.clear();
        if (children != null) {
            for (QuadTreeNode child : children) {
                child.clear();
            }
            children = null;
        }
    }

    /**
     * Get an object closest to a lat/lon.
     *
     * @param lat up-down location in QuadTree Grid (latitude, y)
     * @param lon left-right location in QuadTree Grid (longitude, x)
     * @return the object that matches the best distance, null if no object was
     * found.
     */
    public T get(double lat, double lon) {
        return get(lat, lon, Double.POSITIVE_INFINITY);
    }

    /**
     * Get an object closest to a lat/lon. If there are children at this node,
     * then the children are searched. The children are checked first, to see if
     * they are closer than the best distance already found. If a closer object
     * is found, bestDistance will be updated with a new Double object that has
     * the new distance.
     *
     * @param lat            up-down location in QuadTree Grid (latitude, y)
     * @param lon            left-right location in QuadTree Grid (longitude, x)
     * @param withinDistance maximum get distance.
     * @return the object that matches the best distance, null if no closer
     * object was found.
     */
    public T get(double lat, double lon, double withinDistance) {
        return get(lat, lon, new MutableDistance(withinDistance));
    }

    /**
     * Get an object closest to a lat/lon. If there are children at this node,
     * then the children are searched. The children are checked first, to see if
     * they are closer than the best distance already found. If a closer object
     * is found, bestDistance will be updated with a new Double object that has
     * the new distance.
     *
     * @param lat          up-down location in QuadTree Grid (latitude, y)
     * @param lon          left-right location in QuadTree Grid (longitude, x)
     * @param bestDistance the closest distance of the object found so far.
     * @return the object that matches the best distance, null if no closer
     * object was found.
     */
    public T get(double lat, double lon, MutableDistance bestDistance) {
        T closest = null;
        // This must be the node that has it...
        if (children == null) {
            for (QuadTreeLeaf<T> qtl : items) {
                double dx = lon - qtl.longitude;
                double dy = lat - qtl.latitude;
                double distanceSqr = dx * dx + dy * dy;

                if (distanceSqr < bestDistance.value) {
                    bestDistance.value = distanceSqr;
                    closest = qtl.object;
                }
            }
            return closest;
        } else {
            // Check the distance of the bounds of the children,
            // versus the bestDistance. If there is a boundary that
            // is closer, then it is possible that another node has an
            // object that is closer.
            for (QuadTreeNode<T> child : children) {
                double childDistance = child.bounds.borderDistanceSqr(lat, lon);
                if (childDistance < bestDistance.value) {
                    T test = child.get(lat, lon, bestDistance);
                    if (test != null)
                        closest = test;
                }
            }
        }
        return closest;
    }

    /**
     * Get all the objects within a bounding box.
     *
     * @param north top location in QuadTree Grid (latitude, y2)
     * @param west  left location in QuadTree Grid (longitude, x1)
     * @param south lower location in QuadTree Grid (latitude, y1)
     * @param east  right location in QuadTree Grid (longitude, x2)
     * @return Collection of objects.
     */
    public Collection<T> get(double north, double west, double south, double east) {
        return get(new QuadTreeRect(north, west, south, east), new ArrayList<T>());
    }

    /**
     * Get all the objects within a bounding box.
     *
     * @param north      top location in QuadTree Grid (latitude, y)
     * @param west       left location in QuadTree Grid (longitude, x)
     * @param south      lower location in QuadTree Grid (latitude, y)
     * @param east       right location in QuadTree Grid (longitude, x)
     * @param collection current Collection of objects.
     * @return collection of objects.
     */
    public Collection<T> get(double north, double west, double south, double east,
                             Collection<T> collection) {
        return get(new QuadTreeRect(north, west, south, east), collection);
    }

    /**
     * Get all the objects within a bounding box.
     *
     * @param rect       boundary of area to fill.
     * @param collection current Collection of objects.
     * @return updated Collection of objects.
     */
    public Collection<T> get(QuadTreeRect rect, Collection<T> collection) {
//        for (QuadTreeLeaf<T> qtl : this.items) {
//            if (rect.pointWithinBounds(qtl.latitude, qtl.longitude)) {
//                collection.add(qtl.object);
//            }
//        }
        if (children == null) {
            for (QuadTreeLeaf<T> qtl : this.items) {
                if (rect.pointWithinBounds(qtl.latitude, qtl.longitude)) {
                    collection.add(qtl.object);
                }
            }
        } else {
            for (QuadTreeNode<T> child : children) {
//                if (child.bounds.within(rect)) {
//                    child.get(rect, collection);
//                }
                if (child.bounds.overlap(rect)) {
                    child.get(rect, collection);
                }
            }
        }
        return collection;
    }
}