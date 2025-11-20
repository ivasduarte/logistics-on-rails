package esinf;

import org.junit.jupiter.api.Test;
import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.kd.Point2D;
import spatialQuerying.trees.nodes.KDNode;

import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class KDNodeTest {

    private Station createStation(String name) {
        return new Station(name,
                "Portugal",
                "Europe/Lisbon",
                "WET",
                40.0,
                -74.0,
                false,
                true,
                false);
    }

    @Test
    void testConstructorSortsStationsAlphabetically() {
        Point2D p = new Point2D(10, 10);
        List<Station> stations = Arrays.asList(
                createStation("Campanhã"),
                createStation("Aveiro"),
                createStation("Braga")
        );

        KDNode node = new KDNode(p, stations);
        List<Station> stored = node.getStationList();

        assertEquals("Aveiro", stored.get(0).getStationName());
        assertEquals("Braga", stored.get(1).getStationName());
        assertEquals("Campanhã", stored.get(2).getStationName());
    }

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

    @Test
    void testSetAndGetChildren() {
        KDNode root = new KDNode(new Point2D(10, 10));
        KDNode left = new KDNode(new Point2D(5, 5));
        KDNode right = new KDNode(new Point2D(15, 15));

        root.setLeft(left);
        root.setRight(right);

        assertSame(left, root.getLeft());
        assertSame(right, root.getRight());
    }
}