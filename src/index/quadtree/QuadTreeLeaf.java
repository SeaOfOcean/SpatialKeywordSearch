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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/Index.quadtree/QuadTreeLeaf.java,v
// $
// $RCSfile: QuadTreeLeaf.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:32 $
// $Author: dietrick $
// 
// **********************************************************************

package index.quadtree;

import java.io.Serializable;

public class QuadTreeLeaf<T> implements Serializable {

    static final long serialVersionUID = 7885745536157252519L;

    public double latitude;
    public double longitude;
    public T object;

    public QuadTreeLeaf(double lat, double lon, T obj) {
        latitude = lat;
        longitude = lon;
        object = obj;
    }
}