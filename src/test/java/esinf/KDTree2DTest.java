package esinf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import freightMngt.domain.Station;
import spatialQuerying.structures.kd.KDTree2D;
import spatialQuerying.structures.kd.Point2D;
import spatialQuerying.structures.nodes.KDNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KDTree2DTest {

    private KDTree2D tree;

    @BeforeEach
    void setUp() {
        tree = new KDTree2D();
    }

    private Station createStation(String name, double lat, double lon) {
        return new Station(name, "Portugal",
                "Europe/Lisbon",
                "WET",
                lat,
                lon,
                false,
                true,
                false);
    }

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

    @Test
    void testBuildBalancedTree_SimpleStructure() {
        /*
         * Scenario to test Lat/Long division:
         * P1: (10, 50)
         * P2: (20, 10)  <- Latitude Median (10, 20, 30)
         * P3: (30, 50)
         */
        List<Station> stations = new ArrayList<>();
        stations.add(createStation("Left", 10, 50));
        stations.add(createStation("Root", 20, 10));
        stations.add(createStation("Right", 30, 50));

        // We pass null in AVLs because the buildBalancedTree method provided
        // does the sorting internally and doesn't use latTree/lonTree parameters for core logic.
        tree.buildBalancedTree(stations, null, null);

        KDNode root = tree.getRoot();
        assertNotNull(root);

        // 1. Check Root (Division by Latitude)
        // Sorted Lat list: 10, 20, 30. Median index 1 = 20.
        assertEquals(20.0, root.getCoordinates().getStationLatitude(), 0.001);

        // 2. Check Left Child (Latitude < 20)
        assertNotNull(root.getLeft());
        assertEquals(10.0, root.getLeft().getCoordinates().getStationLatitude(), 0.001);

        // 3. Check Right Child (Latitude >= 20)
        assertNotNull(root.getRight());
        assertEquals(30.0, root.getRight().getCoordinates().getStationLatitude(), 0.001);
    }

    @Test
    void testBuildBalancedTree_AlternatingLevels() {
        /*
         * Test of 2 levels (Lat -> Lon)
         * Points:
         * A: (20, 10)
         * B: (20, 30)
         * C: (20, 20)
         * * All Latitudes are equal (20).
         * The median of the first division (Lat) will pick one of them (e.g.: B, index 1 sorted).
         * Longitude should divide the next level.
         */

        List<Station> stations = new ArrayList<>();
        // Insert unordered
        stations.add(createStation("C", 20, 20));
        stations.add(createStation("A", 20, 10));
        stations.add(createStation("B", 20, 30));

        tree.buildBalancedTree(stations, null, null);

        KDNode root = tree.getRoot();

        assertEquals(3, tree.getSize());
        assertTrue(tree.getHeight() >= 1);
    }

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

    @Test
    void testEmptyTree() {
        tree.buildBalancedTree(new ArrayList<>(), null, null);
        assertNull(tree.getRoot());
        assertEquals(0, tree.getSize());
        assertEquals(0, tree.getHeight()); // Code returns 0 if root is null? No, metric checks root!=null.
        // If root is null, calculateMetrics sets height=0.
    }
}