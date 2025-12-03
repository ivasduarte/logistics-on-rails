# USEI09 - Proximity Search (Nearest-N with Filters)

## 4. Tests

**Test 1:** Tests finding nearest stations when the KD-tree is empty, ensuring the method returns an empty result with correct metadata.
```java
@Test
void testFindNearestStations_EmptyTree() {
    double targetLat = 40.0;
    double targetLon = -74.0;
    int n = 5;
    String timeZoneFilter = null;

    Map<String, Object> expected = new HashMap<>();
    expected.put("stations", new ArrayList<Station>());
    expected.put("distances", new ArrayList<Double>());

    Map<String, Double> targetCoords = new HashMap<>();
    targetCoords.put("latitude", targetLat);
    targetCoords.put("longitude", targetLon);
    expected.put("targetCoordinates", targetCoords);

    Map<String, Integer> searchParams = new HashMap<>();
    searchParams.put("requestedNeighbors", n);
    searchParams.put("treeHeight", 0);
    searchParams.put("resultsFound", 0);
    expected.put("searchParameters", searchParams);

    expected.put("appliedFilters", new ArrayList<String>());

    Map<String, Object> result = controller.findNearestStations(targetLat, targetLon, n, timeZoneFilter);

    assertEquals(expected, result);
}
```

**Test 2:** Tests finding a single nearest station when the tree contains only one station.
```java
@Test
void testFindNearestStations_SingleResult() {
    Station station = createStation("Single Station", 40.0, -74.0, "EST");
    KDNode root = new KDNode(new Point2D(40.0, -74.0));
    root.addStation(station);
    setKDTreeRoot(kdTree, root);

    double targetLat = 40.1;
    double targetLon = -74.1;
    int n = 1;
    String timeZoneFilter = null;

    Map<String, Object> result = controller.findNearestStations(targetLat, targetLon, n, timeZoneFilter);

    Map<String, Object> expected = new HashMap<>();
    expected.put("stations", Arrays.asList(station));
    expected.put("distances", (List<Double>) result.get("distances"));

    Map<String, Double> targetCoords = new HashMap<>();
    targetCoords.put("latitude", targetLat);
    targetCoords.put("longitude", targetLon);
    expected.put("targetCoordinates", targetCoords);

    Map<String, Integer> searchParams = new HashMap<>();
    searchParams.put("requestedNeighbors", n);
    searchParams.put("treeHeight", 0);
    searchParams.put("resultsFound", 1);
    expected.put("searchParameters", searchParams);

    expected.put("appliedFilters", new ArrayList<String>());

    assertEquals(expected, result);
}
```

**Test 3:** Tests finding multiple nearest stations without applying any timezone filter.
```java
@Test
void testFindNearestStations_MultipleResults_NoFilter() {
    buildSimpleTree();

    double targetLat = 40.0;
    double targetLon = -74.0;
    int n = 3;
    String timeZoneFilter = null;

    Map<String, Object> result = controller.findNearestStations(targetLat, targetLon, n, timeZoneFilter);

    Map<String, Object> expected = new HashMap<>();
    expected.put("stations", result.get("stations"));
    expected.put("distances", result.get("distances"));
    expected.put("targetCoordinates", Map.of("latitude", targetLat, "longitude", targetLon));
    expected.put("searchParameters", Map.of("requestedNeighbors", n, "treeHeight", 2, "resultsFound", 3));
    expected.put("appliedFilters", new ArrayList<String>());

    assertEquals(expected, result);
}
```

**Test 4:** Tests finding nearest stations with a timezone filter applied, returning only stations matching the specified timezone.
```java
@Test
void testFindNearestStations_WithTimeZoneFilter() {
    buildSimpleTree();

    double targetLat = 40.0;
    double targetLon = -74.0;
    int n = 5;
    String timeZoneFilter = "EST";

    Map<String, Object> result = controller.findNearestStations(targetLat, targetLon, n, timeZoneFilter);

    Map<String, Object> expected = new HashMap<>();
    expected.put("stations", result.get("stations"));
    expected.put("distances", result.get("distances"));
    expected.put("targetCoordinates", Map.of("latitude", targetLat, "longitude", targetLon));
    expected.put("searchParameters", Map.of("requestedNeighbors", n, "treeHeight", 2, "resultsFound", 3));
    expected.put("appliedFilters", Arrays.asList("EST"));

    assertEquals(expected, result);
}
```

**Test 5:** Tests finding nearest stations when the timezone filter matches no stations, returning an empty result set.
```java
@Test
void testFindNearestStations_WithTimeZoneFilter_NoMatches() {
    buildSimpleTree();

    double targetLat = 40.0;
    double targetLon = -74.0;
    int n = 5;
    String timeZoneFilter = "PST";

    Map<String, Object> expected = new HashMap<>();
    expected.put("stations", new ArrayList<Station>());
    expected.put("distances", new ArrayList<Double>());
    expected.put("targetCoordinates", Map.of("latitude", targetLat, "longitude", targetLon));
    expected.put("searchParameters", Map.of("requestedNeighbors", n, "treeHeight", 2, "resultsFound", 0));
    expected.put("appliedFilters", Arrays.asList("PST"));

    Map<String, Object> result = controller.findNearestStations(targetLat, targetLon, n, timeZoneFilter);

    assertEquals(expected, result);
}
```

**Test 6:** Tests requesting more neighbors than are available in the tree, ensuring all available stations are returned.
```java
@Test
void testFindNearestStations_RequestMoreThanAvailable() {
    buildSimpleTree();

    double targetLat = 40.0;
    double targetLon = -74.0;
    int n = 10;
    String timeZoneFilter = null;

    Map<String, Object> result = controller.findNearestStations(targetLat, targetLon, n, timeZoneFilter);

    Map<String, Object> expected = new HashMap<>();
    expected.put("stations", result.get("stations"));
    expected.put("distances", result.get("distances"));
    expected.put("targetCoordinates", Map.of("latitude", targetLat, "longitude", targetLon));
    expected.put("searchParameters", Map.of("requestedNeighbors", n, "treeHeight", 2, "resultsFound", 5));
    expected.put("appliedFilters", new ArrayList<String>());

    assertEquals(expected, result);
}
```

**Test 7:** Tests requesting zero neighbors, ensuring an empty result is returned with appropriate metadata.
```java
@Test
void testFindNearestStations_ZeroNeighbors() {
    buildSimpleTree();

    double targetLat = 40.0;
    double targetLon = -74.0;
    int n = 0;
    String timeZoneFilter = null;

    Map<String, Object> expected = new HashMap<>();
    expected.put("stations", new ArrayList<Station>());
    expected.put("distances", new ArrayList<Double>());
    expected.put("targetCoordinates", Map.of("latitude", targetLat, "longitude", targetLon));
    expected.put("searchParameters", Map.of("requestedNeighbors", n, "treeHeight", 2, "resultsFound", 0));
    expected.put("appliedFilters", new ArrayList<String>());

    Map<String, Object> result = controller.findNearestStations(targetLat, targetLon, n, timeZoneFilter);

    assertEquals(expected, result);
}
```

**Test 8:** Tests finding nearest stations when multiple stations exist at the same geographic location, with a timezone filter applied.
```java
@Test
void testFindNearestStations_MultipleStationsAtSameLocation() {
    Station s1 = createStation("Alpha Station", 40.0, -74.0, "EST");
    Station s2 = createStation("Beta Station", 40.0, -74.0, "EST");
    Station s3 = createStation("Gamma Station", 40.0, -74.0, "CET");

    KDNode root = new KDNode(new Point2D(40.0, -74.0));
    root.addStation(s1);
    root.addStation(s2);
    root.addStation(s3);
    setKDTreeRoot(kdTree, root);

    double targetLat = 40.0;
    double targetLon = -74.0;
    int n = 2;
    String timeZoneFilter = "EST";

    Map<String, Object> result = controller.findNearestStations(targetLat, targetLon, n, timeZoneFilter);

    Map<String, Object> expected = new HashMap<>();
    expected.put("stations", Arrays.asList(s1, s2));
    expected.put("distances", result.get("distances"));
    expected.put("targetCoordinates", Map.of("latitude", targetLat, "longitude", targetLon));
    expected.put("searchParameters", Map.of("requestedNeighbors", n, "treeHeight", 0, "resultsFound", 2));
    expected.put("appliedFilters", Arrays.asList("EST"));

    assertEquals(expected, result);
}
```

**Test 9:** Tests finding the nearest station when the target coordinates exactly match a station in the tree.
```java
@Test
void testFindNearestStations_ExactTargetMatch() {
    buildSimpleTree();

    double targetLat = 40.0;
    double targetLon = -74.0;
    int n = 1;
    String timeZoneFilter = null;

    Map<String, Object> result = controller.findNearestStations(targetLat, targetLon, n, timeZoneFilter);

    Map<String, Object> expected = new HashMap<>();
    expected.put("stations", result.get("stations"));
    expected.put("distances", result.get("distances"));
    expected.put("targetCoordinates", Map.of("latitude", targetLat, "longitude", targetLon));
    expected.put("searchParameters", Map.of("requestedNeighbors", n, "treeHeight", 2, "resultsFound", 1));
    expected.put("appliedFilters", new ArrayList<String>());
    assertEquals(expected, result);
}
```

**Test 10:** Tests that timezone filtering is case-insensitive, accepting lowercase timezone filter strings.
```java
@Test
void testFindNearestStations_CaseInsensitiveTimeZoneFilter() {
    buildSimpleTree();

    double targetLat = 40.0;
    double targetLon = -74.0;
    int n = 5;
    String timeZoneFilter = "est";

    Map<String, Object> result = controller.findNearestStations(targetLat, targetLon, n, timeZoneFilter);

    Map<String, Object> expected = new HashMap<>();
    expected.put("stations", result.get("stations"));
    expected.put("distances", result.get("distances"));
    expected.put("targetCoordinates", Map.of("latitude", targetLat, "longitude", targetLon));
    expected.put("searchParameters", Map.of("requestedNeighbors", n, "treeHeight", 2, "resultsFound", 3));
    expected.put("appliedFilters", Arrays.asList("est"));

    assertEquals(expected, result);
}
```

**Test 11:** Tests a complete scenario with multiple stations, requesting a specific number of nearest stations with timezone filtering.
```java
@Test
void testFindNearestStations_CompleteScenario() {
    Station s1 = createStation("Central Station", 40.7128, -74.0060, "EST");
    Station s2 = createStation("North Station", 40.8128, -74.0060, "EST");
    Station s3 = createStation("South Station", 40.6128, -74.0060, "CET");

    KDNode root = new KDNode(new Point2D(40.7128, -74.0060));
    root.addStation(s1);

    KDNode left = new KDNode(new Point2D(40.6128, -74.0060));
    left.addStation(s3);

    KDNode right = new KDNode(new Point2D(40.8128, -74.0060));
    right.addStation(s2);

    root.setLeft(left);
    root.setRight(right);
    setKDTreeRoot(kdTree, root);

    double targetLat = 40.7128;
    double targetLon = -74.0060;
    int n = 2;
    String timeZoneFilter = "EST";

    Map<String, Object> result = controller.findNearestStations(targetLat, targetLon, n, timeZoneFilter);

    Map<String, Object> expected = new HashMap<>();
    expected.put("stations", Arrays.asList(s1, s2));
    expected.put("distances", result.get("distances"));

    Map<String, Double> targetCoords = new HashMap<>();
    targetCoords.put("latitude", targetLat);
    targetCoords.put("longitude", targetLon);
    expected.put("targetCoordinates", targetCoords);

    Map<String, Integer> searchParams = new HashMap<>();
    searchParams.put("requestedNeighbors", n);
    searchParams.put("treeHeight", 1);
    searchParams.put("resultsFound", 2);
    expected.put("searchParameters", searchParams);

    expected.put("appliedFilters", Arrays.asList("EST"));

    assertEquals(expected, result);
}
```

## 5. Construction (Implementation)

### Class ProximitySearchController.java

```java
package spatialQuerying.controller;

import freightMngt.domain.Station;
import kd.spatialQuerying.structures.KDTree2D;
import kd.spatialQuerying.structures.Point2D;

import java.util.*;

public class ProximitySearchController {

    private final KDTree2D kdTree;

    /**
     * Constructs a ProximitySearchController with the specified KD-tree.
     *
     * @param kdTree the KD-tree structure used for spatial queries
     */
    public ProximitySearchController(KDTree2D kdTree) {
        this.kdTree = kdTree;
    }

    /**
     * Finds the N nearest stations to the specified target coordinates, with optional timezone filtering.
     * Returns a comprehensive result map containing stations, distances, coordinates, search parameters, and applied filters.
     *
     * @param targetLat the target latitude coordinate
     * @param targetLon the target longitude coordinate
     * @param n the number of nearest neighbors to find
     * @param timeZoneFilter optional timezone filter (case-insensitive), or null for no filtering
     * @return a Map containing:
     *         - "stations": List of Station objects found
     *         - "distances": List of Double distances corresponding to each station
     *         - "targetCoordinates": Map with "latitude" and "longitude" keys
     *         - "searchParameters": Map with "requestedNeighbors", "treeHeight", and "resultsFound"
     *         - "appliedFilters": List of applied filter strings
     */
    public Map<String, Object> findNearestStations(double targetLat, double targetLon, int n, String timeZoneFilter) {
        Map<String, Object> result = new HashMap<>();

        Point2D target = new Point2D(targetLat, targetLon);
        List<Station> nearestStations = kdTree.findNearestNeighbors(target, n, timeZoneFilter);

        List<Double> distances = new ArrayList<>();
        for (Station station : nearestStations) {
            double distance = calculateDistance(targetLat, targetLon,
                    station.getLatitude(), station.getLongitude());
            distances.add(distance);
        }

        result.put("stations", nearestStations);
        result.put("distances", distances);

        Map<String, Double> targetCoords = new HashMap<>();
        targetCoords.put("latitude", targetLat);
        targetCoords.put("longitude", targetLon);
        result.put("targetCoordinates", targetCoords);

        Map<String, Integer> searchParams = new HashMap<>();
        searchParams.put("requestedNeighbors", n);
        searchParams.put("treeHeight", kdTree.getHeight());
        searchParams.put("resultsFound", nearestStations.size());
        result.put("searchParameters", searchParams);

        List<String> appliedFilters = new ArrayList<>();
        if (timeZoneFilter != null) {
            appliedFilters.add(timeZoneFilter);
        }
        result.put("appliedFilters", appliedFilters);

        return result;
    }

    /**
     * Calculates the Euclidean distance between two geographic coordinate points.
     *
     * @param lat1 latitude of the first point
     * @param lon1 longitude of the first point
     * @param lat2 latitude of the second point
     * @param lon2 longitude of the second point
     * @return the Euclidean distance between the two points
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;
        return Math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon);
    }
}
```

### Class Station.java
```java
package spatialQuerying.railwayMngt;

public class Station {

    private final String stationName;
    private final String country;
    private final String countryTimezone;
    private final String stationTimezone;
    private final double latitude;
    private final double longitude;
    private final boolean cargo;
    private final boolean longDistance;
    private final boolean regionalTraffic;

    /**
     * Constructs a new Station object with the specified parameters.
     *
     * @param stationName the name of the station
     * @param country the country where the station is located
     * @param countryTimezone the timezone of the country
     * @param stationTimezone the specific timezone of the station
     * @param latitude the latitude coordinate of the station
     * @param longitude the longitude coordinate of the station
     * @param cargo whether the station handles cargo operations
     * @param longDistance whether the station serves long-distance trains
     * @param regionalTraffic whether the station serves regional traffic
     */
    public Station(String stationName, String country, String countryTimezone, 
                   String stationTimezone, double latitude, double longitude, 
                   boolean cargo, boolean longDistance, boolean regionalTraffic) {
        this.stationName = stationName;
        this.country = country;
        this.countryTimezone = countryTimezone;
        this.stationTimezone = stationTimezone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.cargo = cargo;
        this.longDistance = longDistance;
        this.regionalTraffic = regionalTraffic;
    }

    public String getStationName() { return stationName; }
    public String getCountry() { return country; }
    public String getCountryTimezone() { return countryTimezone; }
    public String getStationTimezone() { return stationTimezone; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isCargo() { return cargo; }
    public boolean isLongDistance() { return longDistance; }
    public boolean isRegionalTraffic() { return regionalTraffic; }
}
```

### Class KDTree2D.java

```java
package spatialQuerying.trees.kd;

import freightMngt.domain.Station;
import kd.spatialQuerying.structures.Point2D;
import nodes.spatialQuerying.structures.KDNode;

import java.util.*;

public class KDTree2D {

    private KDNode root;
    private int height;

    /**
     * Constructs an empty KD-tree.
     */
    public KDTree2D() {
        this.root = null;
        this.height = 0;
    }

    /**
     * Returns the height of the KD-tree.
     *
     * @return the height of the tree, or 0 if empty
     */
    public int getHeight() {
        return height;
    }

    /**
     * Finds the N nearest neighbors to the target point, with optional timezone filtering.
     * Uses a priority queue to maintain the N closest stations found during tree traversal.
     *
     * @param target the target point to search from
     * @param n the number of nearest neighbors to find
     * @param timeZoneFilter optional timezone filter (case-insensitive), or null for no filtering
     * @return a list of the N nearest Station objects, sorted by distance
     */
    public List<Station> findNearestNeighbors(Point2D target, int n, String timeZoneFilter) {
        if (root == null || n <= 0) {
            return new ArrayList<>();
        }

        PriorityQueue<StationDistance> maxHeap = new PriorityQueue<>(
                (a, b) -> Double.compare(b.distance, a.distance)
        );

        findNearestNeighborsHelper(root, target, n, timeZoneFilter, maxHeap, 0);

        List<Station> result = new ArrayList<>();
        while (!maxHeap.isEmpty()) {
            result.add(0, maxHeap.poll().station);
        }

        return result;
    }

    /**
     * Recursive helper method for finding nearest neighbors.
     *
     * @param node the current node in the traversal
     * @param target the target point
     * @param n the maximum number of neighbors to find
     * @param timeZoneFilter optional timezone filter
     * @param maxHeap priority queue maintaining the N closest stations
     * @param depth the current depth in the tree (determines split dimension)
     */
    private void findNearestNeighborsHelper(KDNode node, Point2D target, int n,
                                            String timeZoneFilter,
                                            PriorityQueue<StationDistance> maxHeap,
                                            int depth) {
        if (node == null) return;

        // Check stations at this node
        for (Station station : node.getStations()) {
            if (timeZoneFilter == null ||
                    station.getStationTimezone().equalsIgnoreCase(timeZoneFilter)) {

                double distance = target.distanceTo(node.getPoint());

                if (maxHeap.size() < n) {
                    maxHeap.offer(new StationDistance(station, distance));
                } else if (distance < maxHeap.peek().distance) {
                    maxHeap.poll();
                    maxHeap.offer(new StationDistance(station, distance));
                }
            }
        }

        // Determine which subtree to explore first
        boolean goLeft = shouldGoLeft(target, node.getPoint(), depth);
        KDNode firstChild = goLeft ? node.getLeft() : node.getRight();
        KDNode secondChild = goLeft ? node.getRight() : node.getLeft();

        // Explore subtrees
        findNearestNeighborsHelper(firstChild, target, n, timeZoneFilter, maxHeap, depth + 1);

        // Check if we need to explore the other subtree
        if (maxHeap.size() < n || shouldExploreOtherSide(target, node.getPoint(),
                maxHeap.peek().distance, depth)) {
            findNearestNeighborsHelper(secondChild, target, n, timeZoneFilter, maxHeap, depth + 1);
        }
    }

    /**
     * Determines which direction to traverse first based on the target point and split dimension.
     *
     * @param target the target point
     * @param splitPoint the point at the current node
     * @param depth the current depth (determines dimension: 0=x, 1=y)
     * @return true if should go left, false otherwise
     */
    private boolean shouldGoLeft(Point2D target, Point2D splitPoint, int depth) {
        int axis = depth % 2;
        return axis == 0 ? target.getX() < splitPoint.getX() : target.getY() < splitPoint.getY();
    }

    /**
     * Determines if the other subtree should be explored based on the splitting plane distance.
     *
     * @param target the target point
     * @param splitPoint the splitting point
     * @param maxDistance the current maximum distance in the result set
     * @param depth the current depth
     * @return true if the other subtree might contain closer points
     */
    private boolean shouldExploreOtherSide(Point2D target, Point2D splitPoint,
                                           double maxDistance, int depth) {
        int axis = depth % 2;
        double axisDistance = Math.abs((axis == 0 ? target.getX() : target.getY()) -
                (axis == 0 ? splitPoint.getX() : splitPoint.getY()));
        return axisDistance < maxDistance;
    }

    /**
     * Helper class to store a station with its distance from the target.
     */
    private static class StationDistance {
        Station station;
        double distance;

        StationDistance(Station station, double distance) {
            this.station = station;
            this.distance = distance;
        }
    }
}
```

### Class Point2D.java
```java
package spatialQuerying.trees.kd;

public class Point2D {

    private final double x;
    private final double y;

    /**
     * Constructs a 2D point with the specified coordinates.
     *
     * @param x the x-coordinate (latitude)
     * @param y the y-coordinate (longitude)
     */
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    /**
     * Calculates the Euclidean distance to another point.
     *
     * @param other the other point
     * @return the Euclidean distance
     */
    public double distanceTo(Point2D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
```

### Class KDNode.java

```java
package spatialQuerying.trees.nodes;

import freightMngt.domain.Station;
import kd.spatialQuerying.structures.Point2D;

import java.util.ArrayList;
import java.util.List;

public class KDNode {

    private final Point2D point;
    private final List<Station> stations;
    private KDNode left;
    private KDNode right;

    /**
     * Constructs a KD-tree node with the specified point.
     *
     * @param point the 2D point representing this node's location
     */
    public KDNode(Point2D point) {
        this.point = point;
        this.stations = new ArrayList<>();
        this.left = null;
        this.right = null;
    }

    public Point2D getPoint() {
        return point;
    }

    public List<Station> getStations() {
        return stations;
    }

    public KDNode getLeft() {
        return left;
    }

    public KDNode getRight() {
        return right;
    }

    public void setLeft(KDNode left) {
        this.left = left;
    }

    public void setRight(KDNode right) {
        this.right = right;
    }

    /**
     * Adds a station to this node.
     *
     * @param station the station to add
     */
    public void addStation(Station station) {
        this.stations.add(station);
    }
}
```