package esinf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import spatialQuerying.controller.StationController;
import freightMngt.domain.Station;
import spatialQuerying.structures.bst.LatitudeTree;
import spatialQuerying.structures.bst.LongitudeTree;
import spatialQuerying.structures.bst.TimeZoneTree;
import spatialQuerying.structures.kd.Tree2D;
import spatialQuerying.structures.kd.Point2D;
import spatialQuerying.structures.nodes.KDNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

        Tree2D kdTree = new Tree2D();
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