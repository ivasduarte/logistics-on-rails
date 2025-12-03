package esinf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import freightMngt.domain.Station;
import spatialQuerying.structures.bst.LatitudeTree;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LatitudeTreeTest {
    private LatitudeTree tree;
    private Station station1, station2, station3, station4;

    @BeforeEach
    void setUp() {
        tree = new LatitudeTree();

        station1 = new Station("Porto-Campanha", "PT", "('Europe/Lisbon',)", "WET/GMT", 41.1496, -8.5851, true, true, false);
        station2 = new Station("Lisboa-Oriente", "PT", "('Europe/Lisbon',)", "WET/GMT", 38.7682, -9.0982, true, true, false);
        station3 = new Station("Lisboa-Santa-Apolonia", "PT", "('Europe/Lisbon',)", "WET/GMT", 38.7682, -9.0982, true, true, false);
        station4 = new Station("Paris-Nord", "FR", "('Europe/Paris',)", "CET", 48.8809, 2.3553, true, true, false);
    }

    @Test
    void testInsertStations() {
        tree.insert(station1);
        tree.insert(station2);

        assertEquals(2, tree.size());
        assertEquals(2, tree.totalStations());
    }

    @Test
    void testGroupSameLatitude() {
        tree.insert(station2); // Lisboa-Oriente
        tree.insert(station3); // Lisboa-Santa-Apolonia (same coordinates)

        // Verify we have 2 total stations
        List<Station> allStations = tree.inOrderTraversal();
        assertEquals(2, allStations.size());

        // Both stations should be at the same latitude
        List<Station> stations = tree.findByLatitude(38.7682);
        assertEquals(2, stations.size());

        // Verify both stations are present
        assertTrue(stations.stream().anyMatch(s -> s.getStationName().equals("Lisboa-Oriente")));
        assertTrue(stations.stream().anyMatch(s -> s.getStationName().equals("Lisboa-Santa-Apolonia")));
    }

    @Test
    void testSortSameLatitudeByName() {
        tree.insert(station3); // Lisboa-Santa-Apolonia
        tree.insert(station2); // Lisboa-Oriente (insert in reverse order)

        List<Station> stations = tree.findByLatitude(38.7682);

        assertEquals(2, stations.size());

        // Verify alphabetical ordering (ascending)
        assertEquals("Lisboa-Oriente", stations.get(0).getStationName());
        assertEquals("Lisboa-Santa-Apolonia", stations.get(1).getStationName());
    }

    @Test
    void testFindByLatitude() {
        tree.insert(station1);
        tree.insert(station2);

        List<Station> found = tree.findByLatitude(41.1496);

        assertEquals(1, found.size());
        assertEquals("Porto-Campanha", found.get(0).getStationName());
    }

    @Test
    void testFindNonExistentLatitude() {
        tree.insert(station1);

        List<Station> found = tree.findByLatitude(50.0);

        assertTrue(found.isEmpty());
    }

    @Test
    void testFindInLatitudeRange() {
        tree.insert(station1); // 41.1496
        tree.insert(station2); // 38.7682
        tree.insert(station4); // 48.8809

        List<Station> found = tree.findInLatitudeRange(38.0, 42.0);

        assertEquals(2, found.size()); // station1 and station2
        assertTrue(found.stream().anyMatch(s -> s.getStationName().equals("Porto-Campanha")));
        assertTrue(found.stream().anyMatch(s -> s.getStationName().equals("Lisboa-Oriente")));
    }

    @Test
    void testInOrderTraversal() {
        tree.insert(station1); // 41.1496
        tree.insert(station4); // 48.8809
        tree.insert(station2); // 38.7682

        List<Station> allStations = tree.inOrderTraversal();

        assertEquals(3, allStations.size());

        // Verify ascending order by latitude
        assertEquals("Lisboa-Oriente", allStations.get(0).getStationName());
        assertEquals("Porto-Campanha", allStations.get(1).getStationName());
        assertEquals("Paris-Nord", allStations.get(2).getStationName());

        // Verify the latitudes are in ascending order
        double lat0 = allStations.get(0).getStationLatitude();
        double lat1 = allStations.get(1).getStationLatitude();
        double lat2 = allStations.get(2).getStationLatitude();

        assertTrue(lat0 < lat1);
        assertTrue(lat1 < lat2);
    }

    @Test
    void testTreeBalance() {
        // Insert 7 stations to test balancing
        for (int i = 0; i < 7; i++) {
            Station s = new Station("Station" + i, "PT", "TZ", "WET/GMT", 40.0 + i, -9.0, true, false, false);
            tree.insert(s);
        }

        assertEquals(7, tree.size());
        assertTrue(tree.totalStations() == 7);
    }
}
