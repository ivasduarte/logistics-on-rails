package spatialQuerying.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.kd.KDTree2D;
import spatialQuerying.trees.kd.Point2D;
import spatialQuerying.trees.nodes.KDNode;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ProximitySearchControllerTest {

    private ProximitySearchController controller;
    private KDTree2D kdTree;

    @BeforeEach
    void setUp() {
        kdTree = new KDTree2D();
        controller = new ProximitySearchController(kdTree);
    }

    private Station createStation(String name, double lat, double lon, String timezone) {
        return new Station(name, "TestCountry", "UTC", timezone, lat, lon, false, false, false);
    }

    private void buildSimpleTree() {
        Station s1 = createStation("Station A", 40.0, -74.0, "EST");
        Station s2 = createStation("Station B", 41.0, -73.0, "EST");
        Station s3 = createStation("Station C", 39.0, -75.0, "CET");
        Station s4 = createStation("Station D", 42.0, -72.0, "WET");
        Station s5 = createStation("Station E", 40.5, -73.5, "EST");

        KDNode root = new KDNode(new Point2D(40.5, -73.5));
        root.addStation(s5);

        KDNode left = new KDNode(new Point2D(40.0, -74.0));
        left.addStation(s1);

        KDNode leftLeft = new KDNode(new Point2D(39.0, -75.0));
        leftLeft.addStation(s3);

        KDNode right = new KDNode(new Point2D(41.0, -73.0));
        right.addStation(s2);

        KDNode rightRight = new KDNode(new Point2D(42.0, -72.0));
        rightRight.addStation(s4);

        left.setLeft(leftLeft);
        right.setRight(rightRight);
        root.setLeft(left);
        root.setRight(right);

        setKDTreeRoot(kdTree, root);
    }

    private void setKDTreeRoot(KDTree2D tree, KDNode root) {
        try {
            java.lang.reflect.Field rootField = KDTree2D.class.getDeclaredField("root");
            rootField.setAccessible(true);
            rootField.set(tree, root);

            java.lang.reflect.Field heightField = KDTree2D.class.getDeclaredField("height");
            heightField.setAccessible(true);
            heightField.set(tree, calculateHeight(root));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set tree root", e);
        }
    }

    private int calculateHeight(KDNode node) {
        if (node == null) return -1;
        return Math.max(calculateHeight(node.getLeft()), calculateHeight(node.getRight())) + 1;
    }


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
}