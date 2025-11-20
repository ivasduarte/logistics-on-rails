# USEI08 - Search by Geographical Area

## 1\. Context and User Story

**Context:** Operations require listing all stations inside a map rectangle, optionally filtered by type or country. **KD-tree pruning** should avoid scanning the whole dataset.

**User Story:** As a planner, I want to query the 2D-tree for all stations in a **latitude/longitude range** with optional filters, so I can extract relevant stations quickly.

**Acceptance Criteria:** The region must be defined by a bounding box: $\text{lat} \in [\text{latMin}, \text{latMax}]$, $\text{lon} \in [\text{lonMin}, \text{lonMax}]$ (inclusive).

The search should allow the optional application of filters with the following criteria:

* `isCity` (true | false)
* `isMainStation` (true | false)
* `country` (PT | ES | all)

-----

## 2\. Tests

The following tests verify both the search by exact coordinates (`searchByCoordinates`) and the core functionality of searching within a geographical area (`searchByArea`), ensuring correct KD-Tree structure and filter application.

### Area Search Tests

```java
@Test
void testSearchByArea_BasicRange() {
    // Range: Lat [40.0, 40.5], Lon [-8.5, -3.5]
    // Expected to find: Lisboa (2), Madrid (1), Coimbra (1)
    // Excluded by Lat/Lon: Porto (41.5), Salamanca (40.97)
    List<Station> results = geoSearch.searchByArea(40.0, 40.5, -8.5, -3.5, null, null, null);

    assertNotNull(results);
    assertEquals(4, results.size());
    assertTrue(results.stream().anyMatch(s -> s.getStationName().contains("Lisboa")));
    assertTrue(results.stream().anyMatch(s -> s.getStationName().contains("Madrid")));
    assertFalse(results.stream().anyMatch(s -> s.getStationName().contains("Porto")));
}

@Test
void testSearchByArea_WithMultipleFilters() {
    // Range: Lat [40.0, 42.0], Lon [-9.0, -3.0] (Includes all stations in the setup)
    // Filters: isMainStation=true, Country="PT"
    // Expected: Lisboa (2), Porto (1)
    List<Station> results = geoSearch.searchByArea(40.0, 42.0, -9.0, -3.0, null, true, "PT");

    assertNotNull(results);
    assertEquals(3, results.size());
    assertTrue(results.stream().allMatch(Station::isMainStation));
    assertTrue(results.stream().allMatch(s -> s.getStationCountry().equals("PT")));
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
````

-----

## 3\. Implementation

### Class GeographicalSearch.java

This class uses the KD-Tree for spatial querying. The `rangeSearchNode` method implements the **KD-Tree pruning** logic required for efficient area searching.

```java
package spatialQuerying.controller;

import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.kd.KDTree2D;
import spatialQuerying.trees.kd.Point2D;
import spatialQuerying.trees.nodes.KDNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GeographicalSearch {
    private static final int LAT_MIN = -90;
    private static final int LAT_MAX = 90;
    private static final int LON_MIN = -180;
    private static final int LON_MAX = 180;

    private final KDTree2D kdTree;

    public GeographicalSearch(KDTree2D kdTree) {
        this.kdTree = kdTree;
    }

    // --- Exact Coordinate Search ---

    public List<Station> searchByCoordinates(double latitude, double longitude,
                                             Boolean isCity, Boolean isMainStation, String country) {
        if (!isValidCoordinates(latitude, longitude)) {
            throw new IllegalArgumentException("Invalid coordinates: latitude must be between -90 and 90, longitude must be between -180 and 180");
        }
        Point2D target = new Point2D(latitude, longitude);
        KDNode result = searchNode(kdTree.getRoot(), target, true);

        if (result != null) {
            List<Station> stations = new ArrayList<>(result.getStationList());
            return applyFilters(stations, isCity, isMainStation, country);
        }
        return new ArrayList<>();
    }
    
    // --- Area Search (Range Query) ---

    /**
     * Searches for stations within a specified geographical area (rectangle defined by ranges)
     * and applies optional filters. This method utilizes KD-Tree pruning for efficiency.
     */
    public List<Station> searchByArea(double latMin, double latMax, double lonMin, double lonMax,
                                      Boolean isCity, Boolean isMainStation, String country) {
        if (!isValidCoordinates(latMin, lonMin) || !isValidCoordinates(latMax, lonMax) || latMin > latMax || lonMin > lonMax) {
            throw new IllegalArgumentException("Invalid range coordinates or bounds.");
        }
        
        List<Station> foundStations = new ArrayList<>();
        // Start search from root, splitting by Latitude (true)
        rangeSearchNode(kdTree.getRoot(), latMin, latMax, lonMin, lonMax, true, foundStations);

        // Apply filters to the collected list
        return applyFilters(foundStations, isCity, isMainStation, country);
    }

    /**
     * Recursively searches the KD-Tree for points falling within the defined rectangular range,
     * using the current dimension to prune unnecessary branches.
     */
    private void rangeSearchNode(KDNode node, double latMin, double latMax, double lonMin, double lonMax,
                                 boolean divideByLatitude, List<Station> results) {
        if (node == null) {
            return;
        }

        Point2D point = node.getCoordinates();
        double currentLat = point.getStationLatitude();
        double currentLon = point.getStationLongitude();

        // 1. Check if the current point is within the query range
        if (currentLat >= latMin && currentLat <= latMax &&
            currentLon >= lonMin && currentLon <= lonMax) {
            results.addAll(node.getStationList());
        }

        // 2. Determine which subtrees to search (Pruning)
        if (divideByLatitude) {
            // Splitting by Latitude
            if (latMin <= currentLat) { // Query range crosses or is left/below the plane
                rangeSearchNode(node.getLeft(), latMin, latMax, lonMin, lonMax, false, results);
            }
            if (latMax >= currentLat) { // Query range crosses or is right/above the plane
                rangeSearchNode(node.getRight(), latMin, latMax, lonMin, lonMax, false, results);
            }
        } else {
            // Splitting by Longitude
            if (lonMin <= currentLon) { // Query range crosses or is left/below the plane
                rangeSearchNode(node.getLeft(), latMin, latMax, lonMin, lonMax, true, results);
            }
            if (lonMax >= currentLon) { // Query range crosses or is right/above the plane
                rangeSearchNode(node.getRight(), latMin, latMax, lonMin, lonMax, true, results);
            }
        }
    }
    
    // --- Helper Methods ---

    private KDNode searchNode(KDNode node, Point2D target, boolean divideByLatitude) {
        if (node == null) { return null; }
        if (node.getCoordinates().equals(target)) { return node; }
        
        boolean goLeft;
        if (divideByLatitude) {
            goLeft = target.getStationLatitude() < node.getCoordinates().getStationLatitude();
        } else {
            goLeft = target.getStationLongitude() < node.getCoordinates().getStationLongitude();
        }
        
        if (goLeft) {
            return searchNode(node.getLeft(), target, !divideByLatitude);
        } else {
            return searchNode(node.getRight(), target, !divideByLatitude);
        }
    }

    private List<Station> applyFilters(List<Station> stations, Boolean isCity, Boolean isMainStation, String country) {
        return stations.stream()
                .filter(station -> isCity == null || station.isCity() == isCity)
                .filter(station -> isMainStation == null || station.isMainStation() == isMainStation)
                .filter(station -> country == null || country.equalsIgnoreCase("all")
                        || station.getStationCountry().equalsIgnoreCase(country))
                .collect(Collectors.toList());
    }

    private boolean isValidCoordinates(double latitude, double longitude) {
        return latitude >= LAT_MIN && latitude <= LAT_MAX &&
                longitude >= LON_MIN && longitude <= LON_MAX;
    }
}
```