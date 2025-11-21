# USEI10 - Radius Search and Density Summary

## 1. Context and User Story

**Context:** Operational questions often ask, "what stations lie within R km of a point?" and "how are they distributed by country and isCity?" We need both the list and a quick density summary.

**User Story:** As an operations planner, I want to fetch all stations within a radius R (km) of a target (lat, lon) using the 2D-tree, and get a summary by country and by isCity, so I can assess local coverage.

**Acceptance Criteria:** Use Haversine distance (km) with Earth radius between two (lat, lon) points.

**Returns:** A BST/AVL tree sorted by distance (ASC), and station name (DESC); Temporal analysis complexity.

The search should provide:
* List of stations within the specified radius (in kilometers)
* Stations sorted by distance (ascending), then by station name (ascending)
* Density summary grouped by country and city status (e.g., "PT_City", "PT_NotCity")
* Efficient KD-Tree pruning to avoid scanning irrelevant subtrees

-----

## 2. Tests

The following tests verify the radius search functionality, including distance calculations, sorting behavior, density summaries, and edge cases.

### Radius Search Tests

```java
@Test
public void testSearchByRadius_SmallRadius() {
    // Search with 20km radius from Porto
    RadiusSearch.StationResult result = radiusSearch.searchByRadius(41.1495, -8.5849, 20.0);

    List<Station> stations = result.getStations();

    // Should find Porto-Campanha and Espinho (very close)
    assertNotNull(stations);
    assertTrue(stations.size() >= 1);
    assertTrue(stations.stream().anyMatch(s -> s.getStationName().equals("Porto-Campanha")));
}

@Test
public void testSearchByRadius_MediumRadius() {
    // Search with 100km radius from Porto
    RadiusSearch.StationResult result = radiusSearch.searchByRadius(41.1495, -8.5849, 100.0);

    List<Station> stations = result.getStations();

    // Should find several stations
    assertNotNull(stations);
    assertTrue(stations.size() >= 3);
    assertTrue(stations.stream().anyMatch(s -> s.getStationName().equals("Porto-Campanha")));
    assertTrue(stations.stream().anyMatch(s -> s.getStationName().equals("Aveiro")));
}

@Test
public void testSearchByRadius_LargeRadius() {
    // Search with 300km radius from Porto
    RadiusSearch.StationResult result = radiusSearch.searchByRadius(41.1495, -8.5849, 300.0);

    List<Station> stations = result.getStations();

    // Should find most or all stations
    assertNotNull(stations);
    assertTrue(stations.size() >= 5);
}

@Test
public void testSearchByRadius_NoResults() {
    // Search in middle of ocean (far from any station)
    RadiusSearch.StationResult result = radiusSearch.searchByRadius(30.0, -20.0, 10.0);

    List<Station> stations = result.getStations();

    assertNotNull(stations);
    assertEquals(0, stations.size());
}

@Test
public void testSearchByRadius_SortedByDistance() {
    // Search from Porto with medium radius
    RadiusSearch.StationResult result = radiusSearch.searchByRadius(41.1495, -8.5849, 100.0);

    List<Station> stations = result.getStations();

    // Verify stations are sorted by distance (ascending)
    assertTrue(stations.size() >= 2);

    // Porto-Campanha should be first (distance = 0)
    assertEquals("Porto-Campanha", stations.get(0).getStationName());

    // Verify distances are in ascending order
    double previousDistance = 0.0;
    for (int i = 1; i < stations.size(); i++) {
        double currentDistance = calculateDistance(
                41.1495, -8.5849,
                stations.get(i).getStationLatitude(),
                stations.get(i).getStationLongitude()
        );
        assertTrue(currentDistance >= previousDistance,
                "Stations should be sorted by distance");
        previousDistance = currentDistance;
    }
}

@Test
public void testSearchByRadius_SortedByNameWhenSameDistance() {
    // Add two stations at exactly the same location
    List<Station> sameLocationStations = new ArrayList<>();
    sameLocationStations.add(createStation("Zebra-Station", 41.1495, -8.5849, "PT", false));
    sameLocationStations.add(createStation("Alpha-Station", 41.1495, -8.5849, "PT", false));

    KDTree2D newTree = new KDTree2D();
    newTree.buildBalancedTree(sameLocationStations, null, null);

    RadiusSearch newSearch = new RadiusSearch(newTree);
    RadiusSearch.StationResult result = newSearch.searchByRadius(41.1495, -8.5849, 10.0);

    List<Station> stations = result.getStations();

    // Both stations should be found
    assertEquals(2, stations.size());

    // Alpha should come before Zebra (alphabetical order)
    assertEquals("Alpha-Station", stations.get(0).getStationName());
    assertEquals("Zebra-Station", stations.get(1).getStationName());
}

@Test
public void testDensitySummary_SingleCountry() {
    // Search near Porto (mostly PT stations)
    RadiusSearch.StationResult result = radiusSearch.searchByRadius(41.1495, -8.5849, 80.0);

    Map<String, Integer> density = result.getDensitySummary();

    assertNotNull(density);
    assertTrue(density.containsKey("PT_City") || density.containsKey("PT_NotCity"));
}

@Test
public void testDensitySummary_MultipleCountries() {
    // Search with large radius to include both PT and ES
    RadiusSearch.StationResult result = radiusSearch.searchByRadius(41.5, -8.5, 200.0);

    Map<String, Integer> density = result.getDensitySummary();

    assertNotNull(density);

    // Verify format: Country_CityStatus
    int totalCount = 0;
    for (Map.Entry<String, Integer> entry : density.entrySet()) {
        assertTrue(entry.getKey().contains("_"));
        assertTrue(entry.getKey().endsWith("City") || entry.getKey().endsWith("NotCity"));
        assertTrue(entry.getValue() > 0);
        totalCount += entry.getValue();
    }

    // Total count should match number of stations found
    assertEquals(result.getStations().size(), totalCount);
}

@Test
public void testDensitySummary_EmptyResults() {
    // Search where no stations exist
    RadiusSearch.StationResult result = radiusSearch.searchByRadius(30.0, -20.0, 10.0);

    Map<String, Integer> density = result.getDensitySummary();

    assertNotNull(density);
    assertTrue(density.isEmpty());
}

@Test
public void testDensitySummary_CountAccuracy() {
    // Search and manually verify counts
    RadiusSearch.StationResult result = radiusSearch.searchByRadius(41.1495, -8.5849, 150.0);

    List<Station> stations = result.getStations();
    Map<String, Integer> density = result.getDensitySummary();

    // Manually count by category
    int ptCityCount = 0;
    int ptNotCityCount = 0;
    int esCityCount = 0;
    int esNotCityCount = 0;

    for (Station station : stations) {
        if ("PT".equals(station.getStationCountry())) {
            if (station.isCity()) {
                ptCityCount++;
            } else {
                ptNotCityCount++;
            }
        } else if ("ES".equals(station.getStationCountry())) {
            if (station.isCity()) {
                esCityCount++;
            } else {
                esNotCityCount++;
            }
        }
    }

    // Verify counts match
    if (ptCityCount > 0) {
        assertEquals(ptCityCount, density.getOrDefault("PT_City", 0));
    }
    if (ptNotCityCount > 0) {
        assertEquals(ptNotCityCount, density.getOrDefault("PT_NotCity", 0));
    }
    if (esCityCount > 0) {
        assertEquals(esCityCount, density.getOrDefault("ES_City", 0));
    }
    if (esNotCityCount > 0) {
        assertEquals(esNotCityCount, density.getOrDefault("ES_NotCity", 0));
    }
}

@Test
public void testSearchByRadius_ExactBoundary() {
    // Test with radius that exactly reaches a station
    double distance = calculateDistance(41.1495, -8.5849, 41.0078, -8.6410);

    RadiusSearch.StationResult result = radiusSearch.searchByRadius(
            41.1495, -8.5849, distance
    );

    List<Station> stations = result.getStations();

    // Should include stations at or within the boundary
    assertTrue(stations.stream().anyMatch(s -> s.getStationName().equals("Espinho")));
}

@Test
public void testSearchByRadius_ZeroRadius() {
    // Search with zero radius (should only find exact matches)
    RadiusSearch.StationResult result = radiusSearch.searchByRadius(41.1495, -8.5849, 0.0);

    List<Station> stations = result.getStations();

    // Should find Porto-Campanha (exact coordinates)
    assertNotNull(stations);
    assertTrue(stations.size() >= 1);
    assertEquals("Porto-Campanha", stations.get(0).getStationName());
}

@Test
public void testStationResult_GettersWork() {
    RadiusSearch.StationResult result = radiusSearch.searchByRadius(41.1495, -8.5849, 50.0);

    assertNotNull(result.getStations());
    assertNotNull(result.getDensitySummary());

    // Verify consistency between stations and density
    int totalInDensity = result.getDensitySummary().values().stream()
            .mapToInt(Integer::intValue)
            .sum();
    assertEquals(result.getStations().size(), totalInDensity);
}

@Test
public void testSearchByRadius_CompleteIntegrationScenario() {
    List<Station> scenarioStations = new ArrayList<>();

    scenarioStations.add(new Station("Porto-Campanha", "PT", "Europe/Lisbon", "Europe", 41.1496, -8.5850, true, true, false));
    scenarioStations.add(new Station("Vila Nova de Gaia", "PT", "Europe/Lisbon", "Europe", 41.1299, -8.6042, false, false, false));
    scenarioStations.add(new Station("Braga", "PT", "Europe/Lisbon", "Europe", 41.5503, -8.4200, true, true, false));
    scenarioStations.add(new Station("Tui", "ES", "Europe/Madrid", "Europe", 42.0466, -8.6444, false, false, false));
    scenarioStations.add(new Station("Vigo-Guixar", "ES", "Europe/Madrid", "Europe", 42.2396, -8.7247, true, true, false));
    scenarioStations.add(new Station("Lisboa-Oriente", "PT", "Europe/Lisbon", "Europe", 38.7678, -9.0990, true, true, false));

    KDTree2D scenarioTree = new KDTree2D();
    scenarioTree.buildBalancedTree(scenarioStations, null, null);
    RadiusSearch scenarioSearch = new RadiusSearch(scenarioTree);

    RadiusSearch.StationResult result = scenarioSearch.searchByRadius(41.1496, -8.5850, 150.0);

    List<Station> resultStations = result.getStations();
    assertNotNull(resultStations);
    assertEquals(5, resultStations.size());

    boolean containsLisbon = resultStations.stream()
            .anyMatch(s -> s.getStationName().equals("Lisboa-Oriente"));
    assertFalse(containsLisbon);

    assertEquals("Porto-Campanha", resultStations.get(0).getStationName());
    assertEquals("Vila Nova de Gaia", resultStations.get(1).getStationName());
    assertEquals("Braga", resultStations.get(2).getStationName());
    assertEquals("Tui", resultStations.get(3).getStationName());
    assertEquals("Vigo-Guixar", resultStations.get(4).getStationName());

    Map<String, Integer> density = result.getDensitySummary();
    assertNotNull(density);
    assertEquals(2, density.get("PT_City"));
    assertEquals(1, density.get("PT_NotCity"));
    assertEquals(1, density.get("ES_City"));
    assertEquals(1, density.get("ES_NotCity"));
    assertEquals(4, density.size());
}
```

-----

## 3. Implementation

### Class RadiusSearch.java

This class implements radius-based spatial search using a KD-Tree with Haversine distance calculations. The `searchRadius` method implements **KD-Tree pruning** to efficiently eliminate subtrees that cannot contain results within the search radius.

```java
package spatialQuerying.controller;

import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.kd.KDTree2D;
import spatialQuerying.trees.kd.Point2D;
import spatialQuerying.trees.nodes.KDNode;

import java.util.*;

public class RadiusSearch {
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final KDTree2D kdTree;

    public RadiusSearch(KDTree2D kdTree) {
        this.kdTree = kdTree;
    }

    /**
     * Searches for all stations within a given radius of a target point.
     * Results are sorted by distance (ascending) and then by station name (ascending).
     *
     * @param latitude target latitude
     * @param longitude target longitude
     * @param radiusKm search radius in kilometers
     * @return StationResult containing sorted stations and density summary
     */
    public StationResult searchByRadius(double latitude, double longitude, double radiusKm) {
        Point2D target = new Point2D(latitude, longitude);
        List<StationDistance> candidates = new ArrayList<>();

        // Search the KD-Tree for stations within radius
        searchRadius(kdTree.getRoot(), target, radiusKm, true, candidates);

        // Sort by distance (ASC), then by station name (ASC)
        candidates.sort(Comparator
                .comparingDouble(StationDistance::getDistance)
                .thenComparing(sd -> sd.getStation().getStationName()));

        // Extract just the stations
        List<Station> stations = new ArrayList<>();
        for (StationDistance sd : candidates) {
            stations.add(sd.getStation());
        }

        // Generate density summary
        Map<String, Integer> densitySummary = generateDensitySummary(stations);

        return new StationResult(stations, densitySummary);
    }

    /**
     * Recursively searches the KD-Tree for nodes within the radius.
     * Uses pruning to avoid searching subtrees that cannot contain results.
     */
    private void searchRadius(KDNode node, Point2D target, double radiusKm,
                              boolean divideByLatitude, List<StationDistance> results) {
        if (node == null) {
            return;
        }

        // Calculate distance to this node
        double distance = haversineDistance(
                target.getStationLatitude(), target.getStationLongitude(),
                node.getCoordinates().getStationLatitude(), node.getCoordinates().getStationLongitude()
        );

        // If this node is within radius, add all its stations
        if (distance <= radiusKm) {
            for (Station station : node.getStationList()) {
                results.add(new StationDistance(station, distance));
            }
        }

        // Determine which subtrees need to be searched
        double splitValue = divideByLatitude
                ? node.getCoordinates().getStationLatitude()
                : node.getCoordinates().getStationLongitude();
        double targetValue = divideByLatitude
                ? target.getStationLatitude()
                : target.getStationLongitude();

        // Calculate perpendicular distance to splitting plane
        double perpendicularDist = Math.abs(targetValue - splitValue);
        // Convert to approximate km (rough estimate for pruning)
        double perpendicularDistKm = perpendicularDist * 111.0; // 1 degree ≈ 111 km

        // Determine search order (search near side first)
        boolean targetOnLeft = targetValue < splitValue;
        KDNode nearNode = targetOnLeft ? node.getLeft() : node.getRight();
        KDNode farNode = targetOnLeft ? node.getRight() : node.getLeft();

        // Always search the near side
        searchRadius(nearNode, target, radiusKm, !divideByLatitude, results);

        // Only search far side if the splitting plane intersects the search circle
        if (perpendicularDistKm <= radiusKm) {
            searchRadius(farNode, target, radiusKm, !divideByLatitude, results);
        }
    }

    /**
     * Calculates the Haversine distance between two points on Earth.
     *
     * @return distance in kilometers
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Generates a density summary showing count by country and isCity status.
     * Format: {"PT_City": 5, "PT_NotCity": 3, "ES_City": 2, "ES_NotCity": 1}
     */
    private Map<String, Integer> generateDensitySummary(List<Station> stations) {
        Map<String, Integer> summary = new TreeMap<>();

        for (Station station : stations) {
            String country = station.getStationCountry();
            String cityStatus = station.isCity() ? "City" : "NotCity";
            String key = country + "_" + cityStatus;

            summary.put(key, summary.getOrDefault(key, 0) + 1);
        }

        return summary;
    }

    /**
     * Helper class to store a station with its distance from target.
     */
    private static class StationDistance {
        private final Station station;
        private final double distance;

        public StationDistance(Station station, double distance) {
            this.station = station;
            this.distance = distance;
        }

        public Station getStation() {
            return station;
        }

        public double getDistance() {
            return distance;
        }
    }

    /**
     * Result class containing stations and density summary.
     */
    public static class StationResult {
        private final List<Station> stations;
        private final Map<String, Integer> densitySummary;

        public StationResult(List<Station> stations, Map<String, Integer> densitySummary) {
            this.stations = stations;
            this.densitySummary = densitySummary;
        }

        public List<Station> getStations() {
            return stations;
        }

        public Map<String, Integer> getDensitySummary() {
            return densitySummary;
        }
    }
}
```