package esinf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.bst.LongitudeTree;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LongitudeTreeTest {
    private LongitudeTree tree;
    private Station station1, station2, station3, station4;

    @BeforeEach
    void setUp() {
        tree = new LongitudeTree();

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
    void testGroupSameLongitude() {
        tree.insert(station2);
        tree.insert(station3); // Same longitude as station2

        assertEquals(1, tree.size()); // Only 1 unique longitude
        assertEquals(2, tree.totalStations());

        List<Station> stations = tree.findByLongitude(-9.0982);
        assertEquals(2, stations.size());
    }

    @Test
    void testFindByLongitude() {
        tree.insert(station1);
        tree.insert(station4);

        List<Station> found = tree.findByLongitude(2.3553);

        assertEquals(1, found.size());
        assertEquals("Paris-Nord", found.get(0).getStationName());
    }

    @Test
    void testFindInLongitudeRange() {
        tree.insert(station1); // -8.5851
        tree.insert(station2); // -9.0982
        tree.insert(station4); // 2.3553

        // Query for Portuguese longitude range
        List<Station> found = tree.findInLongitudeRange(-10.0, -8.0);

        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(s -> s.getStationName().equals("Porto-Campanha")));
        assertTrue(found.stream().anyMatch(s -> s.getStationName().equals("Lisboa-Oriente")));
    }

    @Test
    void testInOrderTraversal() {
        tree.insert(station1); // -8.5851
        tree.insert(station4); // 2.3553
        tree.insert(station2); // -9.0982

        List<Station> allStations = tree.inOrderTraversal();

        assertEquals(3, allStations.size());
        assertEquals("Lisboa-Oriente", allStations.get(0).getStationName()); // Lowest longitude
        assertEquals("Porto-Campanha", allStations.get(1).getStationName());
        assertEquals("Paris-Nord", allStations.get(2).getStationName()); // Highest longitude
    }
}
