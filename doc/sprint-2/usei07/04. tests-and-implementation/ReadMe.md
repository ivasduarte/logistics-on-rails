# USEI07 — Build a Balanced 2D-Tree on Latitude and Longitude

## 4. Tests

### **COMPLETE FUNCTIONALITY TEST:**

**Purpose:**
- Read and process a CSV file containing station data.
- Build a balanced 2D-tree on latitude and longitude.
- Verify that the tree is balanced and contains the expected number of nodes.

**Test Description:** 
This test creates a temporary CSV file with 6 stations and builds a balanced 2D-tree on latitude and longitude,
including a case of duplicate coordinates (Lisbon A and Lisbon B).
It then verifies that the tree is balanced and contains the expected number of nodes.

- Delete duplicates in tree nodes;
- Keep buckets of duplicate stations sorted by coordinates;
- Build balanced tree on latitude and longitude.


```java
public class USEI07_CompleteTest {

    @Test
    void testKDTree2DCompleteFunctionality() throws IOException {
        // DUPLICATE POINTS
        String sampleCsv = String.join("\n",
                "station,country,time_zone,time_zone_group,latitude,longitude,is_city,is_main_station,is_airport",
                "Lisbon A,Portugal,WET,Europe/WET,38.72,-9.13,true,true,false",
                "Lisbon B,Portugal,WET,Europe/WET,38.72,-9.13,true,true,false",
                "Porto,Portugal,WET,Europe/WET,41.15,-8.61,true,false,false",
                "Madrid,Spain,CET,Europe/CET,40.42,-3.70,true,true,false",
                "Barcelona,Spain,CET,Europe/CET,41.38,2.17,true,false,false",
                "New York,USA,EST,America/EST,40.71,-74.01,true,true,false"
        );

        Path temp = Files.createTempFile("stations_kd_test", ".csv");
        Files.writeString(temp, sampleCsv);
        temp.toFile().deleteOnExit();

        List<Station> initial = new ArrayList<>();
        StationController controller = new StationController(initial);
        List<Station> stations = controller.collectStationsData(temp.toString());

        Assertions.assertEquals(6, stations.size(), "Expected 6 stations loaded from csv.");

        LatitudeTree latTree = new LatitudeTree();
        LongitudeTree lonTree = new LongitudeTree();
        TimeZoneTree tzTree = new TimeZoneTree();

        for (Station s : stations) {
            latTree.insert(s);
            lonTree.insert(s);
            tzTree.insert(s);
        }

        KDTree2D kdTree = new KDTree2D();
        kdTree.buildBalancedTree(stations, latTree, lonTree);

        Assertions.assertEquals(5, kdTree.getSize(), "AC: Tree size should be equal to the number of unique points (5).");

        Assertions.assertEquals(2, kdTree.getHeight(), "AC: Tree height should be 2 for 5 unique nodes.");

        KDNode root = kdTree.getRoot();

        Assertions.assertNotNull(root, "KD-Tree root should not be null.");

        Assertions.assertEquals(40.71, root.getCoordinates().getStationLatitude(), 0.0001, "Root should be the median by Latitude (New York).");
        Assertions.assertEquals(1, root.getStationList().size(), "Root bucket (New York) should have 1 station.");

        KDNode left = root.getLeft();
        Assertions.assertNotNull(left, "Root's left node should not be null.");

        KDNode lisbonNode = left; // In this case, Lisbon is the median of the left subtree.

        Point2D lisbonPoint = new Point2D(38.72, -9.13);

        Assertions.assertEquals(lisbonPoint, lisbonNode.getCoordinates(), "Lisbon node should be the median node in the left subtree.");

        Assertions.assertEquals(2, lisbonNode.getStationList().size(), "AC: Lisbon bucket should have 2 stations (duplicates).");

        List<Station> lisbonStations = lisbonNode.getStationList();
        Assertions.assertEquals("Lisbon A", lisbonStations.get(0).getStationName(), "AC: First station should be 'Lisbon A' (sorted).");
        Assertions.assertEquals("Lisbon B", lisbonStations.get(1).getStationName(), "AC: Second station should be 'Lisbon B' (sorted).");

        Map<Integer, Integer> bucketSizes = kdTree.getBucketSizes();
        Map<Integer, Integer> expectedBuckets = new TreeMap<>();
        expectedBuckets.put(1, 4); // New York, Porto, Madrid, Barcelona
        expectedBuckets.put(2, 1); // Lisbon A/B

        Assertions.assertEquals(expectedBuckets, bucketSizes, "AC: Bucket size distribution should be {1: 4, 2: 1}.");
    }
}
```

---

### **TEST EXAMPLE 1: MAINTAIN STATION ORDER AFTER ADDING A NEW STATION**

**Purpose:**
- Check if method `addStation()` in class `KDNode` maintains the alphabetical order of stations in the tree.

**Test Description:**
1. Create a new KDNode with a station in coordinates (0,0);
2. Add two more stations to the node;
3. Automatically check if the stations are added in alphabetical order.

```java
@Test
    void testAddStationMaintainsOrder() {
        KDNode node = new KDNode(new Point2D(0,0));
        node.addStation(createStation("Zebra"));
        node.addStation(createStation("Alpha"));
        node.addStation(createStation("Beta"));

        assertEquals("Alpha", node.getStationList().get(0).getStationName());
        assertEquals("Beta", node.getStationList().get(1).getStationName());
        assertEquals("Zebra", node.getStationList().get(2).getStationName());
    }
```


--- 

### **TEST EXAMPLE 2: GROUP STATIONS BY COORDINATES**

**Purpose:**
- Make sure that the method `groupByCoordinates()` in class `KDTree2D` groups stations by the same
geographical coordinates, creating a map where each key represents a unique point and the value is a list
of stations located in said point.

**Test Description:**
1. Create a list of 3 stations with coordinates (10,10) and (20,20);
2. Call the method `groupByCoordinates()` and check if the returned map contains the expected number of points and stations.

```java
    @Test
    void testGroupByCoordinates() {
        List<Station> stations = new ArrayList<>();
        stations.add(createStation("A", 10, 10));
        stations.add(createStation("B", 20, 20));
        stations.add(createStation("A_DuplicateCoord", 10, 10)); // Same coordinates as A

        Map<Point2D, List<Station>> groups = tree.groupByCoordinates(stations);

        assertEquals(2, groups.size(), "Should have only 2 unique points");

        Point2D p10 = new Point2D(10, 10);
        assertTrue(groups.containsKey(p10));
        assertEquals(2, groups.get(p10).size(), "Point (10,10) should have 2 stations");
    }
```

---

### **TEST EXAMPLE 3: TREE METRICS**

**Purpose:**
- Validate that the KD-tree correctly calculates its internal metrics:
1. **Tree Size** (`getSize()`), which should reflect the number of unique points;
2. **Bucket Size Distribution** (`getBucketSizes()`), which should reflect the number of stations in each bucket.
- This test ensures that duplicate coordinates don't create multiple buckets, but increase
the bucket size of the corresponding node.

```java
    @Test
    void testMetrics() {
        List<Station> stations = new ArrayList<>();
        // 3 unique points
        stations.add(createStation("S1", 10, 10));
        stations.add(createStation("S2", 20, 20));
        stations.add(createStation("S3", 30, 30));
        // 1 duplicate at S1
        stations.add(createStation("S4", 10, 10));

        tree.buildBalancedTree(stations, null, null);

        assertEquals(3, tree.getSize(), "Size should count unique points (KDNodes)");

        Map<Integer, Integer> buckets = tree.getBucketSizes();
        // Expected:
        // 1 node with 2 stations (point 10,10)
        // 2 nodes with 1 station (20,20 and 30,30)
        assertTrue(buckets.containsKey(2));
        assertEquals(1, buckets.get(2));

        assertTrue(buckets.containsKey(1));
        assertEquals(2, buckets.get(1));
    }
```

---

### **TEST EXAMPLE 4: BUILD EMPTY TREE**

**Purpose:**
- Check if the method `buildBalancedTree()` correctly handles an empty list of stations.
- This test assures that the tree is built correctly even if the input list is empty.

**Test Description:**
1. Call the method `buildBalancedTree()` with an empty list of stations;
2. Check if the tree is empty (`getRoot()` should return `null`) and that its metrics are correct.

```java
    @Test
    void testEmptyTree() {
        tree.buildBalancedTree(new ArrayList<>(), null, null);
        assertNull(tree.getRoot());
        assertEquals(0, tree.getSize());
        assertEquals(0, tree.getHeight()); // Code returns 0 if root is null? No, metric checks root!=null.
        // If root is null, calculateMetrics sets height=0.
    }
```


---

## 5. Implementation

### Class `Point2D`

```java
/**
 * Point2D.java
 */
package spatialQuerying.trees.kd;

import java.util.Objects;

public class Point2D {
    private final double stationLatitude;
    private final double stationLongitude;

    public Point2D(double stationLatitude, double stationLongitude) {
        this.stationLatitude = stationLatitude;
        this.stationLongitude = stationLongitude;
    }

    public double getStationLatitude() {
        return stationLatitude;
    }
    public double getStationLongitude() {
        return stationLongitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point2D)) return false;
        Point2D other = (Point2D) o;
        return Double.compare(stationLatitude, other.stationLatitude) == 0 &&
                Double.compare(stationLongitude, other.stationLongitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stationLatitude, stationLongitude);
    }

    @Override
    public String toString() {
        return String.format("(%.6f, %.6f)", stationLatitude,  stationLongitude);
    }
}
```

---

### Class `KDNode`

```java
/**
 * KDNode.java
 */

package spatialQuerying.trees.nodes;

import freightMngt.domain.Station;
import kd.spatialQuerying.structures.Point2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KDNode {
    private Point2D coordinates;            // (lat, long)
    private List<Station> stationList;      // every station with these coordinates
    private KDNode left;
    private KDNode right;

    public KDNode(Point2D coordinates) {
        this.coordinates = coordinates;
        this.stationList = new ArrayList<>();
    }

    public KDNode(Point2D coordinates, List<Station> stationList) {
        this.coordinates = coordinates;
        this.stationList = new ArrayList<>(stationList);

        // Guarantee alphabetical order
        this.stationList.sort(Comparator.comparing(Station::getStationName));
    }

    public void addStation(Station station) {
        stationList.add(station);
        stationList.sort(Comparator.comparing(Station::getStationName));
    }

    public Point2D getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Point2D coordinates) {
        this.coordinates = coordinates;
    }

    public List<Station> getStationList() {
        return stationList;
    }

    public void setStationList(List<Station> stationList) {
        this.stationList = stationList;
    }

    public KDNode getLeft() {
        return left;
    }

    public void setLeft(KDNode left) {
        this.left = left;
    }

    public KDNode getRight() {
        return right;
    }

    public void setRight(KDNode right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return String.format("KDNode\n {coordinates: %s, stations: %d}", coordinates, stationList.size());
    }
}

```

---

### Class `KDTree2D`

```java
/**
 * KDTree2D.java
 */
package spatialQuerying.trees.kd;

import freightMngt.domain.Station;
import spatialQuerying.structures.bst.LatitudeTree;
import spatialQuerying.structures.bst.LongitudeTree;
import kd.spatialQuerying.structures.Point2D;
import nodes.spatialQuerying.structures.KDNode;

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
        List<Point2D> byLatitude = sortPoints(uniquePoints, latTree, lonTree, true);
        List<Point2D> byLongitude = sortPoints(uniquePoints, latTree, lonTree, false);

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
                                    boolean divideByLatitude) {

        if (uniquePoints < 0) return null;

        // choose median
        int medianIndex = uniquePoints / 2;
        Point2D medianPoint = divideByLatitude ? latList.get(medianIndex) : lonList.get(medianIndex);

        KDNode newNode = new KDNode(medianPoint, groupedStations.get(medianPoint));

        // part both lists in O(n)
        List<Point2D> leftLat = new ArrayList<>();
        List<Point2D> rightLat = new ArrayList<>();

        List<Point2D> leftLon = new ArrayList<>();
        List<Point2D> rightLon = new ArrayList<>();

        for (int i = 0; i <= uniquePoints; i++) {
            Point2D point = latList.get(i);

            if (!point.equals(medianPoint)) {
                if (divideByLatitude) {
                    if (i < medianIndex) leftLat.add(point);
                    else rightLat.add(point);
                } else {
                    if (point.getStationLongitude() < medianPoint.getStationLongitude()) leftLat.add(point);
                    else rightLat.add(point);
                }
            }
        }

        for (int i = 0; i <= uniquePoints; i++) {
            Point2D point = lonList.get(i);
            if (!point.equals(medianPoint)) {
                if (!divideByLatitude) {
                    if (i < medianIndex) leftLon.add(point);
                    else rightLon.add(point);
                } else {
                    // when divided by latitude, must compare with latitude
                    if (point.getStationLatitude() < medianPoint.getStationLatitude()) leftLon.add(point);
                    else rightLon.add(point);
                }
            }
        }
        newNode.setLeft(buildBalancedTree(leftLat.size() - 1, groupedStations, leftLat, leftLon, !divideByLatitude));
        newNode.setRight(buildBalancedTree(rightLat.size() - 1, groupedStations, rightLat, rightLon, !divideByLatitude));

        return newNode;
    }

    /**
     * This method sorts points by their appropriate dimension using AVL.
     *
     * @param points
     * @param latTree
     * @param lonTree
     * @param divideByLatitude
     * @return sorted list of points
     */
    private List<Point2D> sortPoints(List<Point2D> points, LatitudeTree latTree, LongitudeTree lonTree, boolean divideByLatitude) {
        Set<Point2D> toInclude = new HashSet<>(points);

        List<Point2D> sortedPoints = new ArrayList<>();
        Set<Point2D> added = new HashSet<>();   // avoid duplicates

        if (divideByLatitude) {
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
```