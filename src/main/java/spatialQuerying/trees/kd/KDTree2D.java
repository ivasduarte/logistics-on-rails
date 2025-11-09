/**
 * KDTree2D.java
 */
package spatialQuerying.trees.kd;

import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.bst.LatitudeTree;
import spatialQuerying.trees.bst.LongitudeTree;
import spatialQuerying.trees.nodes.KDNode;

import java.util.*;

/**
 * A KD-Tree implementation specialized for 2D spatial queries.
 * The tree is built using an alternating subdivision of latitude and longitude
 * coordinates, creating a balanced binary structure.
 */
public class KDTree2D {
    private KDNode root;
    private int size;                                                 // number of unique points
    private int height;                                               // tree's height
    private Map<Integer, Integer> bucketSizes = new TreeMap<>();      // size, quantity

    public KDTree2D() {
    }

    public KDNode getRoot() {
        return root;
    }

    public int getSize() {
        return size;
    }

    public int getHeight() {
        return height;
    }

    public Map<Integer, Integer> getBucketSizes() {
        return new TreeMap<>(bucketSizes);
    }

    /**
     * This method builds a balanced 2D tree from a list of stations.
     * The stations are grouped by their latitude/longitude coordinates, and the tree structure is determined based
     * on an alternating division of latitude and longitude. This method also calculates relevant tree metrics
     * after the tree is constructed.
     *
     * @param stationList the list of stations to be included in the tree
     * @param latTree     sorting by latitude
     * @param lonTree     sorting by longitude
     */
    public void buildBalancedTree(List<Station> stationList, LatitudeTree latTree, LongitudeTree lonTree) {
        // group stations by identical coordinates
        Map<Point2D, List<Station>> groupedStations = groupByCoordinates(stationList);

        // get a list of identical points
        List<Point2D> uniquePoints = new ArrayList<>(groupedStations.keySet());

        // presort
        List<Point2D> byLatitude = new ArrayList<>(uniquePoints);
        byLatitude.sort(Comparator.comparingDouble(Point2D::getStationLatitude));

        List<Point2D> byLongitude = new ArrayList<>(uniquePoints);
        byLongitude.sort(Comparator.comparingDouble(Point2D::getStationLongitude));
    }

    /**
     * This method groups stations with duplicate coordinates by station name.
     *
     * @param stationList
     * @return map of coordinates to stations
     */
    public Map<Point2D, List<Station>> groupByCoordinates(List<Station> stationList) {
        Map<Point2D, List<Station>> groupedStations = new LinkedHashMap<>();

        for (Station station : stationList) {
            Point2D point = new Point2D(station.getStationLatitude(), station.getStationLongitude());

            // check if the key "point" already exists in the map
            groupedStations.computeIfAbsent(point, k -> new ArrayList<>()).add(station);
        }

        // order each group by station name
        groupedStations.values().forEach(stations -> stations.sort(Comparator.comparing(Station::getStationName)));

        return groupedStations;
    }
}