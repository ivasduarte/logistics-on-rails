package esinf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.bst.TimeZoneTree;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TimeZoneTreeTest {
    private TimeZoneTree tree;
    private Station station1, station2, station3, station4, station5;

    @BeforeEach
    void setUp() {
        tree = new TimeZoneTree();

        station1 = new Station("Porto-Campanha", "PT", "('Europe/Lisbon',)", "WET/GMT", 41.1496, -8.5851, true, true, false);
        station2 = new Station("Lisboa-Oriente", "PT", "('Europe/Lisbon',)", "WET/GMT", 38.7682, -9.0982, true, true, false);
        station3 = new Station("Paris-Nord", "FR", "('Europe/Paris',)", "CET", 48.8809, 2.3553, true, true, false);
        station4 = new Station("Berlin-Hbf", "DE", "('Europe/Berlin',)", "CET", 52.5250, 13.3694, true, true, false);
        station5 = new Station("Lyon-Part-Dieu", "FR", "('Europe/Paris',)", "CET", 45.7603, 4.8592, true, true, false);
    }

    @Test
    void testInsertStations() {
        tree.insert(station1);
        tree.insert(station3);

        assertEquals(2, tree.size()); // 2 unique time zone groups
        assertEquals(2, tree.totalStations());
    }

    @Test
    void testGroupByTimeZoneGroup() {
        tree.insert(station3); // CET, FR
        tree.insert(station4); // CET, DE
        tree.insert(station5); // CET, FR

        assertEquals(1, tree.size()); // Only 1 time zone group
        assertEquals(3, tree.totalStations());
    }

    @Test
    void testFindByTimeZoneGroup() {
        tree.insert(station1); // WET/GMT, PT
        tree.insert(station2); // WET/GMT, PT
        tree.insert(station3); // CET, FR

        List<Station> wetStations = tree.findByTimeZoneGroup("WET/GMT");

        assertEquals(2, wetStations.size());
        assertTrue(wetStations.stream().allMatch(s -> s.getStationTimeZoneGroup().equals("WET/GMT")));
    }

    @Test
    void testFindByTimeZoneGroupAndCountry() {
        tree.insert(station3); // CET, FR
        tree.insert(station4); // CET, DE
        tree.insert(station5); // CET, FR

        List<Station> cetFrance = tree.findByTimeZoneGroupAndCountry("CET", "FR");

        assertEquals(2, cetFrance.size());
        assertTrue(cetFrance.stream().allMatch(s -> s.getStationCountry().equals("FR")));
        assertTrue(cetFrance.stream().allMatch(s -> s.getStationTimeZoneGroup().equals("CET")));
    }

    @Test
    void testFindNonExistentTimeZoneGroup() {
        tree.insert(station1);

        List<Station> found = tree.findByTimeZoneGroup("EET");

        assertTrue(found.isEmpty());
    }

    @Test
    void testFindNonExistentCountry() {
        tree.insert(station3); // CET, FR

        List<Station> found = tree.findByTimeZoneGroupAndCountry("CET", "ES");

        assertTrue(found.isEmpty());
    }

    @Test
    void testFindInTimeZoneRange() {
        tree.insert(station1); // WET/GMT
        tree.insert(station3); // CET

        // Alphabetically: CET < WET/GMT
        List<Station> found = tree.findInTimeZoneRange("CET", "WET/GMT");

        assertEquals(2, found.size());
    }

    @Test
    void testGetCountriesInTimeZoneGroup() {
        tree.insert(station3); // CET, FR
        tree.insert(station4); // CET, DE
        tree.insert(station5); // CET, FR

        Set<String> countries = tree.getCountriesInTimeZoneGroup("CET");

        assertEquals(2, countries.size());
        assertTrue(countries.contains("FR"));
        assertTrue(countries.contains("DE"));
    }

    @Test
    void testGetAllTimeZoneGroups() {
        tree.insert(station1); // WET/GMT
        tree.insert(station3); // CET

        List<String> tzGroups = tree.getAllTimeZoneGroups();

        assertEquals(2, tzGroups.size());
        assertTrue(tzGroups.contains("CET"));
        assertTrue(tzGroups.contains("WET/GMT"));
    }

    @Test
    void testSortByNameInSameTimeZoneAndCountry() {
        Station stationA = new Station("Z-Station", "FR", "TZ", "CET", 45.0, 4.0, true, false, false);
        Station stationB = new Station("A-Station", "FR", "TZ", "CET", 45.0, 4.0, true, false, false);

        tree.insert(stationA);
        tree.insert(stationB);

        List<Station> cetFrance = tree.findByTimeZoneGroupAndCountry("CET", "FR");

        assertEquals(2, cetFrance.size());
        assertEquals("A-Station", cetFrance.get(0).getStationName());
        assertEquals("Z-Station", cetFrance.get(1).getStationName());
    }
}
