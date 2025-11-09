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

        // build the tree
        this.root = buildBalancedTree(uniquePoints.size() - 1,
                groupedStations,
                byLatitude,
                byLongitude,
                true // starts by dividing by latitude
        );

        // calc metrics
        calculateMetrics();
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

    /**
     * Builds a balanced KD tree from a list of unique points by recursively splitting the points
     * based on median positions, alternating between latitude and longitude dimensions.
     *
     * @return the root node of the balanced KD tree
     */
    public KDNode buildBalancedTree(int uniquePoints,
                                    Map<Point2D, List<Station>> groupedStations,
                                    List<Point2D> latList,
                                    List<Point2D> lonList,
                                    boolean devideByLatitude) {

        if (uniquePoints < 0) return null;

        // choose median
        int medianIndex = uniquePoints / 2;
        Point2D medianPoint = devideByLatitude ? latList.get(medianIndex) : lonList.get(medianIndex);

        KDNode newNode = new KDNode(medianPoint, groupedStations.get(medianPoint));

        // part both lists in O(n)
        List<Point2D> leftLat = new ArrayList<>();
        List<Point2D> leftLon = new ArrayList<>();

        List<Point2D> rightLat = new ArrayList<>();
        List<Point2D> rightLon = new ArrayList<>();

        for (int i = 0; i <= uniquePoints; i++) {
            Point2D point = devideByLatitude ? latList.get(i) : lonList.get(i);

            if(point.equals(medianPoint)) continue;

            if (devideByLatitude ? point.getStationLatitude() < medianPoint.getStationLatitude() : point.getStationLongitude() < medianPoint.getStationLongitude()) {
                leftLat.add(point);
                leftLon.add(point);
            } else {
                rightLat.add(point);
                rightLon.add(point);
            }
        }
        newNode.setLeft(buildBalancedTree(leftLat.size() - 1, groupedStations, leftLat, leftLon, !devideByLatitude));
        newNode.setRight(buildBalancedTree(rightLat.size() - 1, groupedStations, rightLat, rightLon, !devideByLatitude));

        return newNode;
    }

    /**
     * This method sorts points by their appropriate dimension using AVL.
     *
     * @param points
     * @param latTree
     * @param lonTree
     * @param devideByLatitude
     * @return sorted list of points
     */
    private List<Point2D> sortPoints(List<Point2D> points, LatitudeTree latTree, LongitudeTree lonTree, boolean devideByLatitude) {
        Set<Point2D> toInclude = new HashSet<>(points);

        List<Point2D> sortedPoints = new ArrayList<>();
        Set<Point2D> added = new HashSet<>();   // avoid duplicates

        if (devideByLatitude) {
            // go through latitude's AVL in order
            List<Station> stations = latTree.inOrderTraversal();

            for (Station station : stations) {
                Point2D coords = new Point2D(station.getStationLatitude(), station.getStationLongitude());

                // if this point is in the list, it hasn't been added yet
                if (toInclude.contains(coords) && !added.contains(coords)) {
                    sortedPoints.add(coords);
                    added.add(coords);
                }
            }
        } else {
            // go through longitude's AVL in order
            List<Station> stations = lonTree.inOrderTraversal();

            for (Station station : stations) {
                Point2D coords = new Point2D(station.getStationLatitude(), station.getStationLongitude());
                if (toInclude.contains(coords) && !added.contains(coords)) {
                    sortedPoints.add(coords);
                    added.add(coords);
                }
            }
        }
        return sortedPoints;
    }

    /**
     * This method resets internal metrics (size, height, and bucketSizes),
     * and if a root node exists, recursively computes the tree height and counts
     * nodes and bucket sizes using helper methods.
     */
    public void calculateMetrics() {
        this.size = 0;
        this.height = 0;
        this.bucketSizes.clear();

        if (root != null) {
            this.height = calculateHeight(root);
            calculateNodesAndBuckets(root);
        }
    }

    /**
     * This method uses a recursive approach to calculate the height of a tree.
     *
     * @param node
     * @return height of a node = max path length to leaf
     */
    private int calculateHeight(KDNode node) {
        if (node == null) return -1;

        int leftHeight = calculateHeight(node.getLeft());
        int rightHeight = calculateHeight(node.getRight());

        return Math.max(leftHeight, rightHeight) + 1;
    }

    /**
     * This method uses a recursive approach to count nodes and bucket sizes.
     *
     * @param node
     */
    private void calculateNodesAndBuckets(KDNode node) {
        if (node == null) return;

        size++;     // count this node

        // count bucket sizes (how many stations in this point)
        int bucketSize = node.getStationList().size();
        bucketSizes.put(bucketSize, bucketSizes.getOrDefault(bucketSize, 0) + 1);

        calculateNodesAndBuckets(node.getLeft());
        calculateNodesAndBuckets(node.getRight());
    }
}