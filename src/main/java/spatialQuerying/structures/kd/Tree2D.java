package spatialQuerying.structures.kd;

import freightMngt.domain.Station;
import spatialQuerying.structures.bst.LatitudeTree;
import spatialQuerying.structures.bst.LongitudeTree;
import spatialQuerying.structures.nodes.KDNode;

import java.util.*;

public class Tree2D {
    private KDNode root;
    private int size;
    private int height;
    private Map<Integer, Integer> bucketSizes = new TreeMap<>();

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
        return bucketSizes;
    }

    public void buildBalancedTree(List<Station> stationList, LatitudeTree latTree, LongitudeTree lonTree) {
        // Group stations by coordinates
        Map<Point2D, List<Station>> groupedStations = groupByCoordinates(stationList);
        List<Point2D> uniquePoints = new ArrayList<>(groupedStations.keySet());

        // Pre-sorting of the tree structure
        List<Point2D> byLatitude = sortPoints(uniquePoints, latTree, lonTree, true);
        List<Point2D> byLongitude = sortPoints(uniquePoints, latTree, lonTree, false);

        this.root = buildBalancedRecursive(groupedStations, byLatitude, byLongitude, true);

        organizeTree();
        calculateMetrics();
    }

    /**
     * Recursively builds balanced KD‑tree nodes from point lists
     */
    private KDNode buildBalancedRecursive(Map<Point2D, List<Station>> groupedStations,
                                          List<Point2D> latList,
                                          List<Point2D> lonList,
                                          boolean divideByLatitude) {
        if (latList == null || latList.isEmpty()) return null;

        // Median calculation
        int medianIndex = (divideByLatitude ? latList : lonList).size() / 2;
        Point2D medianPoint = divideByLatitude ? latList.get(medianIndex) : lonList.get(medianIndex);

        KDNode newNode = new KDNode(medianPoint); // Criar o nó apenas com coordenadas
        newNode.setStationList(groupedStations.get(medianPoint)); // Inserir os valores

        // Parting for subtrees
        List<Point2D> leftLat = new ArrayList<>(), rightLat = new ArrayList<>();
        List<Point2D> leftLon = new ArrayList<>(), rightLon = new ArrayList<>();

        distributePoints(latList, lonList, medianPoint, divideByLatitude, leftLat, rightLat, leftLon, rightLon);

        newNode.setLeft(buildBalancedRecursive(groupedStations, leftLat, leftLon, !divideByLatitude));
        newNode.setRight(buildBalancedRecursive(groupedStations, rightLat, rightLon, !divideByLatitude));

        return newNode;
    }

    /**
     * Distributes the points throughout the tree so that it remains balanced.
     */
    private void distributePoints(List<Point2D> latList, List<Point2D> lonList,
                                  Point2D medianPoint, boolean divideByLatitude,
                                  List<Point2D> leftLat, List<Point2D> rightLat,
                                  List<Point2D> leftLon, List<Point2D> rightLon) {

        Set<Point2D> leftSet = new HashSet<>(); // Identificar quais pontos pertencem à esquerda

        if (divideByLatitude) {
            // Organise based on latitude (X axis)
            for (Point2D p : latList) {
                if (p.equals(medianPoint)) continue;

                if (p.getStationLatitude() < medianPoint.getStationLatitude()) {
                    leftLat.add(p);
                    leftSet.add(p);
                } else {
                    rightLat.add(p);
                }
            }

            for (Point2D p : lonList) {
                if (p.equals(medianPoint)) continue;
                if (leftSet.contains(p)) leftLon.add(p);
                else rightLon.add(p);
            }

        } else {
            // Organise based on longitude (Y axis)
            for (Point2D p : lonList) {
                if (p.equals(medianPoint)) continue;

                if (p.getStationLongitude() < medianPoint.getStationLongitude()) {
                    leftLon.add(p);
                    leftSet.add(p);
                } else {
                    rightLon.add(p);
                }
            }

            for (Point2D p : latList) {
                if (p.equals(medianPoint)) continue;
                if (leftSet.contains(p)) leftLat.add(p);
                else rightLat.add(p);
            }
        }
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

            // Verifica se a chave "Point" já existe no mapa
            groupedStations.computeIfAbsent(point, k -> new ArrayList<>()).add(station);
        }
        return groupedStations;
    }

    /**
     * Sorts points by their appropriate dimension using AVL trees from USEI06.
     * This method leverages the pre-sorted structure of the AVL trees to achieve
     * O(n) sorting complexity instead of O(n log n).
     *
     * @param points the list of unique points to be sorted
     * @param latTree the AVL tree sorted by latitude (from USEI06)
     * @param lonTree the AVL tree sorted by longitude (from USEI06)
     * @param divideByLatitude true to sort by latitude, false to sort by longitude
     * @return a list of points sorted by the specified dimension
     */
    private List<Point2D> sortPoints(List<Point2D> points, LatitudeTree latTree, LongitudeTree lonTree, boolean divideByLatitude) {
        // If AVL trees are not available, use standard sorting
        if (latTree == null || lonTree == null) {
            List<Point2D> sortedPoints = new ArrayList<>(points);
            if (divideByLatitude) {
                sortedPoints.sort(Comparator.comparingDouble(Point2D::getStationLatitude));
            } else {
                sortedPoints.sort(Comparator.comparingDouble(Point2D::getStationLongitude));
            }
            return sortedPoints;
        }

        // Use AVL trees for O(n) sorting
        Set<Point2D> toInclude = new HashSet<>(points);

        List<Point2D> sortedPoints = new ArrayList<>();
        Set<Point2D> added = new HashSet<>();   // avoid duplicates

        if (divideByLatitude) {
            // Go through latitude's AVL in order
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
            // Go through longitude's AVL in order
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

    /**
     * Iterates through the tree after construction to organise the internal content of each node.
     */
    public void organizeTree() {
        if (root != null) {
            organizeNodesRecursive(root);
        }
    }

    private void organizeNodesRecursive(KDNode node) {
        if (node == null) return;

        // Sort stations by name
        node.getStationList().sort(Comparator.comparing(Station::getStationName));

        organizeNodesRecursive(node.getLeft());
        organizeNodesRecursive(node.getRight());
    }
}