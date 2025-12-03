package spatialQuerying.controller;

import freightMngt.domain.Station;
import spatialQuerying.structures.kd.KDTree2D;
import spatialQuerying.structures.kd.Point2D;
import spatialQuerying.structures.nodes.KDNode;

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