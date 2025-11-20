package esinf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spatialQuerying.controller.GeographicalSearch;
import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.kd.KDTree2D;
import spatialQuerying.trees.kd.Point2D;
import spatialQuerying.trees.nodes.KDNode;

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
     * Constrói uma árvore válida manualmente para teste.
     * Estrutura corrigida para respeitar a alternância Lat/Lon:
     *
     * ROOT (Split by Lat): Madrid (40.4)
     * |
     * +-- LEFT (Lat < 40.4): Lisbon (40.0) -> (Split by Lon: -8.0)
     * |      |
     * |      +-- LEFT (Lon < -8.0): Coimbra (-8.4)
     * |
     * +-- RIGHT (Lat >= 40.4): Salamanca (40.97) -> (Split by Lon: -5.66)
     * |
     * +-- LEFT (Lon < -5.66): Porto (-8.6)
     */
    private void buildTestTree() {
        // 1. Root Node: Madrid (Index 3)
        // Split dimension: Latitude
        Point2D madridPoint = new Point2D(40.4, -3.7);
        List<Station> madridStations = new ArrayList<>();
        madridStations.add(testStations.get(3));
        KDNode root = new KDNode(madridPoint, madridStations);

        // 2. Left Child of Root: Lisbon (Index 0, 1)
        // Logic: Lat 40.0 < 40.4 -> Left
        // Split dimension: Longitude
        Point2D lisbonPoint = new Point2D(40.0, -8.0);
        List<Station> lisbonStations = new ArrayList<>();
        lisbonStations.add(testStations.get(0));
        lisbonStations.add(testStations.get(1));
        KDNode lisbonNode = new KDNode(lisbonPoint, lisbonStations);

        // 3. Right Child of Root: Salamanca (Index 5)
        // Logic: Lat 40.97 >= 40.4 -> Right
        // Split dimension: Longitude
        Point2D salamancaPoint = new Point2D(40.97, -5.66);
        List<Station> salamancaStations = new ArrayList<>();
        salamancaStations.add(testStations.get(5));
        KDNode salamancaNode = new KDNode(salamancaPoint, salamancaStations);

        // 4. Child of Lisbon: Coimbra (Index 4)
        // Path: Root(Left) -> Lisbon
        // Logic: Lon -8.4 < -8.0 -> Left
        Point2D coimbraPoint = new Point2D(40.2, -8.4);
        List<Station> coimbraStations = new ArrayList<>();
        coimbraStations.add(testStations.get(4));
        KDNode coimbraNode = new KDNode(coimbraPoint, coimbraStations);

        // 5. Child of Salamanca: Porto (Index 2)
        // Path: Root(Right) -> Salamanca
        // Logic: Lon -8.6 < -5.66 -> Left
        Point2D portoPoint = new Point2D(41.5, -8.6);
        List<Station> portoStations = new ArrayList<>();
        portoStations.add(testStations.get(2));
        KDNode portoNode = new KDNode(portoPoint, portoStations);

        // Link nodes
        lisbonNode.setLeft(coimbraNode);      // Coimbra is Left of Lisbon (Lon comparison)
        salamancaNode.setLeft(portoNode);     // Porto is Left of Salamanca (Lon comparison)
        root.setLeft(lisbonNode);             // Lisbon is Left of Madrid (Lat comparison)
        root.setRight(salamancaNode);         // Salamanca is Right of Madrid (Lat comparison)

        // Set the root using reflection
        try {
            Field rootField = KDTree2D.class.getDeclaredField("root");
            rootField.setAccessible(true);
            rootField.set(kdTree, root);
        } catch (Exception e) {
            fail("Could not set root node: " + e.getMessage());
        }
    }

    @Test
    void testSearchByCoordinates_ExistingLocation() {
        // Test finding stations at Lisbon coordinates
        List<Station> results = geoSearch.searchByCoordinates(40.0, -8.0);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Lisboa Oriente", results.get(0).getStationName());
        assertEquals("Lisboa Santa Apolónia", results.get(1).getStationName());
    }

    @Test
    void testSearchByCoordinates_SingleStation() {
        // Test finding Porto
        List<Station> results = geoSearch.searchByCoordinates(41.5, -8.6);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Porto Campanhã", results.get(0).getStationName());
    }

    @Test
    void testSearchByCoordinates_NonExistingLocation() {
        // Test coordinates that don't exist in the tree
        List<Station> results = geoSearch.searchByCoordinates(50.0, 10.0);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchByCoordinates_WithCityFilter_True() {
        // Search for cities only at Lisbon coordinates
        List<Station> results = geoSearch.searchByCoordinates(40.0, -8.0, true, null, null);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(Station::isCity));
    }

    @Test
    void testSearchByCoordinates_WithCityFilter_False() {
        // Search for non-cities at Coimbra coordinates
        List<Station> results = geoSearch.searchByCoordinates(40.2, -8.4, false, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertFalse(results.get(0).isCity());
    }

    @Test
    void testSearchByCoordinates_WithMainStationFilter_True() {
        // Search for main stations at Porto coordinates
        List<Station> results = geoSearch.searchByCoordinates(41.5, -8.6, null, true, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isMainStation());
    }

    @Test
    void testSearchByCoordinates_WithMainStationFilter_False() {
        // Search for non-main stations at Salamanca coordinates
        List<Station> results = geoSearch.searchByCoordinates(40.97, -5.66, null, false, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertFalse(results.get(0).isMainStation());
    }

    @Test
    void testSearchByCoordinates_WithCountryFilter_Portugal() {
        // Search for Portuguese stations at Lisbon coordinates
        List<Station> results = geoSearch.searchByCoordinates(40.0, -8.0, null, null, "PT");

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(s -> s.getStationCountry().equals("PT")));
    }

    @Test
    void testSearchByCoordinates_WithCountryFilter_Spain() {
        // Search for Spanish stations at Madrid coordinates
        List<Station> results = geoSearch.searchByCoordinates(40.4, -3.7, null, null, "ES");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("ES", results.get(0).getStationCountry());
    }

    @Test
    void testSearchByCoordinates_WithCountryFilter_All() {
        // Search with "all" country filter should return all stations
        List<Station> results = geoSearch.searchByCoordinates(40.0, -8.0, null, null, "all");

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void testSearchByCoordinates_WithCountryFilter_CaseInsensitive() {
        // Test case insensitivity
        List<Station> results1 = geoSearch.searchByCoordinates(40.0, -8.0, null, null, "pt");
        List<Station> results2 = geoSearch.searchByCoordinates(40.0, -8.0, null, null, "PT");

        assertEquals(results1.size(), results2.size());
    }

    @Test
    void testSearchByCoordinates_MultipleFilters() {
        // Search with multiple filters: city=true, mainStation=true, country=PT
        List<Station> results = geoSearch.searchByCoordinates(40.0, -8.0, true, true, "PT");

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(s ->
                s.isCity() && s.isMainStation() && s.getStationCountry().equals("PT")));
    }

    @Test
    void testSearchByCoordinates_FilterEliminatesAllResults() {
        // Search for Spanish stations at Portuguese coordinates (should return empty)
        List<Station> results = geoSearch.searchByCoordinates(40.0, -8.0, null, null, "ES");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchByCoordinates_InvalidLatitude_TooLow() {
        // Test latitude below -90
        assertThrows(IllegalArgumentException.class, () -> {
            geoSearch.searchByCoordinates(-91.0, 0.0);
        });
    }

    @Test
    void testSearchByCoordinates_InvalidLatitude_TooHigh() {
        // Test latitude above 90
        assertThrows(IllegalArgumentException.class, () -> {
            geoSearch.searchByCoordinates(91.0, 0.0);
        });
    }

    @Test
    void testSearchByCoordinates_InvalidLongitude_TooLow() {
        // Test longitude below -180
        assertThrows(IllegalArgumentException.class, () -> {
            geoSearch.searchByCoordinates(0.0, -181.0);
        });
    }

    @Test
    void testSearchByCoordinates_InvalidLongitude_TooHigh() {
        // Test longitude above 180
        assertThrows(IllegalArgumentException.class, () -> {
            geoSearch.searchByCoordinates(0.0, 181.0);
        });
    }

    @Test
    void testSearchByCoordinates_BoundaryValues() {
        // Test valid boundary values
        assertDoesNotThrow(() -> {
            geoSearch.searchByCoordinates(-90.0, -180.0);
            geoSearch.searchByCoordinates(90.0, 180.0);
            geoSearch.searchByCoordinates(0.0, 0.0);
        });
    }

    @Test
    void testSearchByCoordinates_InvalidCoordinates_ExceptionMessage() {
        // Test that exception message is correct
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            geoSearch.searchByCoordinates(100.0, 200.0);
        });

        assertTrue(exception.getMessage().contains("Invalid coordinates"));
        assertTrue(exception.getMessage().contains("latitude must be between -90 and 90"));
        assertTrue(exception.getMessage().contains("longitude must be between -180 and 180"));
    }

    @Test
    void testConstructor_NullKDTree() {
        // Test constructor with null KDTree (should not throw, but searches will fail)
        assertDoesNotThrow(() -> {
            GeographicalSearch nullSearch = new GeographicalSearch(null);
        });
    }

    @Test
    void testSearchByCoordinates_EmptyTree() {
        // Test with an empty KD-tree
        KDTree2D emptyTree = new KDTree2D();
        GeographicalSearch emptySearch = new GeographicalSearch(emptyTree);

        List<Station> results = emptySearch.searchByCoordinates(40.0, -8.0);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchByCoordinates_NullFilters() {
        // Test that null filters are properly handled (no filtering applied)
        List<Station> results = geoSearch.searchByCoordinates(40.0, -8.0, null, null, null);

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

        List<Station> results = geoSearch.searchByCoordinates(
                targetLat,
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