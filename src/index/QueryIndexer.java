package index;

import index.partition.ZCurvePartitioner;
import index.quadtree.QuadTree;
import topology.Global;
import structure.POI;
import structure.Point;

import java.io.*;
import java.util.HashSet;

/**
 * Created by Xianyan Jia on 25/10/2015.
 */
public class QueryIndexer {
    static QuadTree[] quadTrees;

    public static QuadTree[] indexPOI() {
        System.out.println("start building Index.quadtree");
        QuadTree[] qt = new QuadTree[Global.N_PARTITION];
        for (int i = 0; i < Global.N_PARTITION; i++) {
            qt[i] = new QuadTree();
        }
        ZCurvePartitioner zCurve = new ZCurvePartitioner();
        HashSet<Point> points = new HashSet<Point>();
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

        try {
            BufferedReader reader = new BufferedReader(new FileReader(Global.POI_FILE));
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                try {
                    POI poi = new POI(line);
                    int partition = zCurve.overlapPartition(poi.getLat(), poi.getLng());
                    qt[partition].put(poi.getLat(), poi.getLng(), poi);
                } catch (Exception e) {
                    continue;
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Global.isSavePOITree) {
            for (int i = 0; i < Global.N_PARTITION; i++) {
                qt[i].save2File(String.format(Global.POI_INDEX_FILE_FORMAT, i));
            }
        }

        return qt;
    }

    public static QuadTree indexSingleTree() {
        QuadTree qt = new QuadTree();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Global.POI_FILE));
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                try {
                    POI poi = new POI(line);
                    qt.put(poi.getLat(), poi.getLng(), poi);
                } catch (Exception e) {
                    continue;
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        qt.save2File(String.format(Global.POI_INDEX_FILE_FORMAT, 520));
        return qt;
    }

    public static QuadTree loadQuadTree_partition(int index, int numPartition) {
        if (quadTrees == null) {
            System.out.println("build quad trees");
            quadTrees = loadPoiIndex(numPartition);
        }
        if (index >= numPartition) {
            System.err.println("the index cannot be larger than the num of partitions");
        }
        return quadTrees[index];
    }

    public static QuadTree[] loadPoiIndex(int numPartition) {
        QuadTree[] qt;
        String file0 = String.format(Global.POI_INDEX_FILE_FORMAT, 0);
        if (Global.isSavePOITree) {
            if (!new File(file0).exists()) {
                qt = QueryIndexer.indexPOI();
            } else {
                try {
                    qt = new QuadTree[numPartition];
                    for (int i = 0; i < numPartition; i++) {
                        String saveFile = String.format(Global.POI_INDEX_FILE_FORMAT, i);
                        qt[i] = QuadTree.load(saveFile);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    qt = QueryIndexer.indexPOI();
                }
            }
        } else {
            qt = QueryIndexer.indexPOI();
        }

        System.out.println("load quad tree done...");
        return qt;
    }
}
