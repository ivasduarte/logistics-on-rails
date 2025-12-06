package esinf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spatialQuerying.controller.GeographicalSearch;
import freightMngt.domain.Station;
import spatialQuerying.structures.kd.KDTree2D;
import spatialQuerying.structures.kd.Point2D;
import spatialQuerying.structures.nodes.KDNode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GeographicalSearchTest {

    private GeographicalSearch geoSearch;
    private KDTree2D kdTree;
    private List<Station> testStations;

    @BeforeEach
    void setUp() {
        // Create test stations with various properties
        testStations = new ArrayList<>();

        // Index 0: Lisbon1 (Lat: 40.0, Lon: -8.0)
        Station lisbon1 = new Station("Lisboa Santa Apolónia", "PT", "Europe/Lisbon", "Western European Time",
                40.0, -8.0, true, true, false);
        // Index 1: Lisbon2 (Lat: 40.0, Lon: -8.0)
        Station lisbon2 = new Station("Lisboa Oriente", "PT", "Europe/Lisbon", "Western European Time",
                40.0, -8.0, true, true, false);

        // Index 2: Porto (Lat: 41.5, Lon: -8.6)
        Station porto = new Station("Porto Campanhã", "PT", "Europe/Lisbon", "Western European Time",
                41.5, -8.6, true, true, false);

        // Index 3: Madrid (Lat: 40.4, Lon: -3.7)
        Station madrid = new Station("Madrid Atocha", "ES", "Europe/Madrid", "Central European Time",
                40.4, -3.7, true, true, false);

        // Index 4: Coimbra (Lat: 40.2, Lon: -8.4)
        Station smallPT = new Station("Coimbra-B", "PT", "Europe/Lisbon", "Western European Time",
                40.2, -8.4, false, false, false);

        // Index 5: Salamanca (Lat: 40.97, Lon: -5.66)
        Station smallES = new Station("Salamanca", "ES", "Europe/Madrid", "Central European Time",
                40.97, -5.66, false, false, false);

        testStations.add(lisbon1);
        testStations.add(lisbon2);
        testStations.add(porto);
        testStations.add(madrid);
        testStations.add(smallPT);
        testStations.add(smallES);

        // Build KD-Tree manually for testing
        kdTree = new KDTree2D();
        buildTestTree();

        geoSearch = new GeographicalSearch(kdTree);
    }

    /**
     * Builds the KD-Tree using the actual implementation method.
     * We pass null for the AVL trees (LatitudeTree and LongitudeTree) because
     * the buildBalancedTree method supports sorting via standard Lists if trees are unavailable.
     */
    private void buildTestTree() {
        kdTree.buildBalancedTree(testStations, null, null);
    }

    @Test
    void testSearchByArea_ExistingLocation() {
        // Test finding stations at Lisbon coordinates
        List<Station> results = geoSearch.searchByArea(40.0, 40.0, -8.0, -8.0, null, null, null);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Lisboa Oriente", results.get(0).getStationName());
        assertEquals("Lisboa Santa Apolónia", results.get(1).getStationName());
    }

    @Test
    void testSearchByArea_SingleStation() {
        // Test finding Porto
        List<Station> results = geoSearch.searchByArea(41.5, 41.5, -8.6, -8.6, null, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Porto Campanhã", results.get(0).getStationName());
    }

    @Test
    void testSearchByArea_NonExistingLocation() {
        // Test coordinates that don't exist in the tree
        List<Station> results = geoSearch.searchByArea(50.0, 50.0, 10.0, 10.0, null, null, null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchByArea_WithCityFilter_True() {
        // Search for cities only at Lisbon coordinates
        List<Station> results = geoSearch.searchByArea(40.0, 40.0, -8.0, -8.0, true, null, null);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(Station::isCity));
    }

    @Test
    void testSearchByArea_WithCityFilter_False() {
        // Search for non-cities at Coimbra coordinates
        List<Station> results = geoSearch.searchByArea(40.2, 40.2, -8.4, -8.4, false, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertFalse(results.get(0).isCity());
    }

    @Test
    void testSearchByArea_WithMainStationFilter_True() {
        // Search for main stations at Porto coordinates
        List<Station> results = geoSearch.searchByArea(41.5, 41.5, -8.6, -8.6, null, true, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isMainStation());
    }

    @Test
    void testSearchByArea_WithMainStationFilter_False() {
        // Search for non-main stations at Salamanca coordinates
        List<Station> results = geoSearch.searchByArea(40.97, 40.97, -5.66, -5.66, null, false, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertFalse(results.get(0).isMainStation());
    }

    @Test
    void testSearchByArea_WithCountryFilter_Portugal() {
        // Search for Portuguese stations at Lisbon coordinates
        List<Station> results = geoSearch.searchByArea(40.0, 40.0, -8.0, -8.0, null, null, "PT");

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(s -> s.getStationCountry().equals("PT")));
    }

    @Test
    void testSearchByArea_WithCountryFilter_Spain() {
        // Search for Spanish stations at Madrid coordinates
        List<Station> results = geoSearch.searchByArea(40.4, 40.4, -3.7, -3.7, null, null, "ES");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("ES", results.get(0).getStationCountry());
    }

    @Test
    void testSearchByArea_WithCountryFilter_All() {
        // Search with "all" country filter should return all stations
        List<Station> results = geoSearch.searchByArea(40.0, 40.0, -8.0, -8.0, null, null, "all");

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void testSearchByArea_WithCountryFilter_CaseInsensitive() {
        // Test case insensitivity
        List<Station> results1 = geoSearch.searchByArea(40.0, 40.0, -8.0, -8.0, null, null, "pt");
        List<Station> results2 = geoSearch.searchByArea(40.0, 40.0, -8.0, -8.0, null, null, "PT");

        assertEquals(results1.size(), results2.size());
    }

    @Test
    void testSearchByArea_MultipleFilters() {
        // Search with multiple filters: city=true, mainStation=true, country=PT
        List<Station> results = geoSearch.searchByArea(40.0, 40.0, -8.0, -8.0, true, true, "PT");

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(s ->
                s.isCity() && s.isMainStation() && s.getStationCountry().equals("PT")));
    }

    @Test
    void testSearchByArea_FilterEliminatesAllResults() {
        // Search for Spanish stations at Portuguese coordinates (should return empty)
        List<Station> results = geoSearch.searchByArea(40.0, 40.0, -8.0, -8.0, null, null, "ES");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchByArea_InvalidLatitude_TooLow() {
        // Test latitude below -90
        assertThrows(IllegalArgumentException.class, () -> {
            geoSearch.searchByArea(-91.0, -91.0, 0.0, 0.0, null, null, null);
        });
    }

    @Test
    void testSearchByArea_InvalidLatitude_TooHigh() {
        // Test latitude above 90
        assertThrows(IllegalArgumentException.class, () -> {
            geoSearch.searchByArea(91.0, 91.0, 0.0, 0.0, null, null, null);
        });
    }

    @Test
    void testSearchByArea_InvalidLongitude_TooLow() {
        // Test longitude below -180
        assertThrows(IllegalArgumentException.class, () -> {
            geoSearch.searchByArea(0.0, 0.0, -181.0, -181.0, null, null, null);
        });
    }

    @Test
    void testSearchByArea_InvalidLongitude_TooHigh() {
        // Test longitude above 180
        assertThrows(IllegalArgumentException.class, () -> {
            geoSearch.searchByArea(0.0, 0.0, 181.0, 181.0, null, null, null);
        });
    }

    @Test
    void testSearchByArea_BoundaryValues() {
        // Test valid boundary values
        assertDoesNotThrow(() -> {
            geoSearch.searchByArea(-90.0, -90.0, -180.0, -180.0, null, null, null);
            geoSearch.searchByArea(90.0, 90.0, 180.0, 180.0, null, null, null);
            geoSearch.searchByArea(0.0, 0.0, 0.0, 0.0, null, null, null);
        });
    }

    @Test
    void testSearchByArea_InvalidCoordinates_ExceptionMessage() {
        // Test that exception message is correct
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            geoSearch.searchByArea(100.0, 100.0, 200.0, 200.0, null, null, null);
        });

        assertTrue(exception.getMessage().contains("Invalid coordinates range"));
    }

    @Test
    void testConstructor_NullKDTree() {
        // Test constructor with null KDTree (should not throw, but searches will fail)
        assertDoesNotThrow(() -> {
            GeographicalSearch nullSearch = new GeographicalSearch(null);
        });
    }

    @Test
    void testSearchByArea_EmptyTree() {
        // Test with an empty KD-tree
        KDTree2D emptyTree = new KDTree2D();
        GeographicalSearch emptySearch = new GeographicalSearch(emptyTree);

        List<Station> results = emptySearch.searchByArea(40.0, 40.0, -8.0, -8.0, null, null, null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchByArea_NullFilters() {
        // Test that null filters are properly handled (no filtering applied)
        List<Station> results = geoSearch.searchByArea(40.0, 40.0, -8.0, -8.0, null, null, null);

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void testCompleteSearchWorkflow_Integration() {
        double targetLat = 40.0;
        double targetLon = -8.0;
        boolean filterIsCity = true;
        boolean filterIsMain = true;
        String filterCountry = "PT";

        List<Station> results = geoSearch.searchByArea(
                targetLat,
                targetLat,
                targetLon,
                targetLon,
                filterIsCity,
                filterIsMain,
                filterCountry
        );

        assertNotNull(results);
        assertEquals(2, results.size());

        List<String> resultingNames = new ArrayList<>();
        for (Station s : results) {
            resultingNames.add(s.getStationName());

            assertEquals("PT", s.getStationCountry());
            assertTrue(s.isCity());
            assertTrue(s.isMainStation());
        }

        assertTrue(resultingNames.contains("Lisboa Santa Apolónia"));
        assertTrue(resultingNames.contains("Lisboa Oriente"));
    }
}