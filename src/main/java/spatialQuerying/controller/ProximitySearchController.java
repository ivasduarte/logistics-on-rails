/**
 * ProximitySearchController.java
 * Controller for handling proximity search operations on the KD-Tree, specifically
 * the Nearest-N (k-NN) search algorithm with optional filters.
 */
package spatialQuerying.controller;

import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.kd.KDTree2D;
import spatialQuerying.trees.kd.Point2D;
import spatialQuerying.trees.nodes.KDNode;

import java.util.*;
import java.util.function.Predicate;

public class ProximitySearchController {
    private KDTree2D kdTree;
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Constructs a ProximitySearchController associated with a specific KD-Tree.
     *
     * @param kdTree The KDTree2D instance containing the station data.
     */
    public ProximitySearchController(KDTree2D kdTree) {
        this.kdTree = kdTree;
    }

    /**
     * Helper class to store a {@link Station} along with its calculated distance
     * from the target point. Used internally by the Priority Queue.
     */
    private static class StationDistance {
        Station station;
        double distance;

        /**
         * Creates a new instance of StationDistance.
         *
         * @param station  The {@link Station} object.
         * @param distance The distance from the target coordinates in kilometers.
         */
        StationDistance(Station station, double distance) {
            this.station = station;
            this.distance = distance;
        }
    }

    /**
     * Finds N nearest stations to a target coordinate with an optional time zone filter.
     * The search uses a Priority Queue (Max Heap) to efficiently track the N closest stations
     * and employs branch pruning to minimize tree traversal.
     *
     * @param targetLat      Target latitude in degrees.
     * @param targetLon      Target longitude in degrees.
     * @param n              Number of nearest neighbors to find (k).
     * @param timeZoneFilter Optional time zone filter (e.g., "CET", "WET"). If null or empty, no filter is applied.
     * @return Map containing search results and metadata, including "stations", "distances", and performance metrics.
     */
    public Map<String, Object> findNearestStations(double targetLat, double targetLon, int n, String timeZoneFilter) {
        List<Station> resultStations = new ArrayList<>();
        List<Double> resultDistances = new ArrayList<>();

        if (kdTree.getRoot() == null || n <= 0) {
            return buildResponse(resultStations, resultDistances, targetLat, targetLon, n, timeZoneFilter);
        }

        PriorityQueue<StationDistance> nearestStations = new PriorityQueue<>((a, b) -> {
            int distComp = Double.compare(b.distance, a.distance);
            if (distComp != 0) return distComp;
            // If distances are equal, sort by station name (descending for max heap)
            return b.station.getStationName().compareTo(a.station.getStationName());
        });

        Predicate<Station> filter = timeZoneFilter == null ? s -> true : s -> timeZoneFilter.equalsIgnoreCase(s.getStationTimeZoneGroup());

        findNearestNRecursive(kdTree.getRoot(), targetLat, targetLon, n, nearestStations, filter, true);

        List<StationDistance> tempList = new ArrayList<>();
        while (!nearestStations.isEmpty()) {
            tempList.add(0, nearestStations.poll());
        }

        // Sort final results by distance, then by station name alphabetically
        tempList.sort((a, b) -> {
            int distComp = Double.compare(a.distance, b.distance);
            if (distComp != 0) return distComp;
            return a.station.getStationName().compareTo(b.station.getStationName());
        });

        for (StationDistance sd : tempList) {
            resultStations.add(sd.station);
            resultDistances.add(sd.distance);
        }

        return buildResponse(resultStations, resultDistances, targetLat, targetLon, n, timeZoneFilter);
    }

    /**
     * Builds the final response map containing the search results and metadata.
     *
     * @param stations       The list of nearest stations found.
     * @param distances      The list of corresponding distances.
     * @param targetLat      The target latitude.
     * @param targetLon      The target longitude.
     * @param n              The number of neighbors requested.
     * @param timeZoneFilter The time zone filter applied (or null).
     * @return A Map containing all results and metadata for display.
     */
    private Map<String, Object> buildResponse(List<Station> stations, List<Double> distances, double targetLat, double targetLon, int n, String timeZoneFilter) {
        Map<String, Object> response = new HashMap<>();

        response.put("stations", new ArrayList<>(stations));
        response.put("distances", new ArrayList<>(distances));

        Map<String, Double> targetCoordinates = new HashMap<>();
        targetCoordinates.put("latitude", targetLat);
        targetCoordinates.put("longitude", targetLon);
        response.put("targetCoordinates", targetCoordinates);

        Map<String, Integer> searchParameters = new HashMap<>();
        searchParameters.put("requestedNeighbors", n);
        searchParameters.put("treeHeight", kdTree.getHeight());
        searchParameters.put("resultsFound", stations.size());
        response.put("searchParameters", searchParameters);

        List<String> appliedFilters = new ArrayList<>();
        if (timeZoneFilter != null && !timeZoneFilter.isEmpty()) {
            appliedFilters.add(timeZoneFilter);
        }
        response.put("appliedFilters", appliedFilters);

        return response;
    }

    /**
     * Recursive helper method for nearest neighbor search (k-NN).
     * Explores the KD-tree efficiently by pruning branches that cannot contain closer stations
     * than the current k-th nearest neighbor found so far.
     *
     * @param node             The current node being examined.
     * @param targetLat        Target latitude.
     * @param targetLon        Target longitude.
     * @param n                Number of neighbors requested (k).
     * @param nearestStations  Priority Queue (Max Heap) storing the k closest stations found.
     * @param filter           Predicate used to filter stations (e.g., by time zone).
     * @param divideByLatitude True if the current node splits based on latitude, false for longitude.
     */
    private void findNearestNRecursive(KDNode node, double targetLat, double targetLon, int n, PriorityQueue<StationDistance> nearestStations, Predicate<Station> filter, boolean divideByLatitude) {
        if (node == null) return;

        Point2D coords = node.getCoordinates();
        double distance = haversineDistance(targetLat, targetLon, coords.getStationLatitude(), coords.getStationLongitude());

        for (Station station : node.getStationList()) {
            if (filter.test(station)) {
                if (nearestStations.size() < n) {
                    nearestStations.offer(new StationDistance(station, distance));
                } else {
                    StationDistance farthest = nearestStations.peek();
                    boolean shouldReplace = distance < farthest.distance || (distance == farthest.distance && station.getStationName().compareTo(farthest.station.getStationName()) < 0);

                    if (shouldReplace) {
                        nearestStations.poll();
                        nearestStations.offer(new StationDistance(station, distance));
                    }
                }
            }
        }

        double targetValue = divideByLatitude ? targetLat : targetLon;
        double nodeValue = divideByLatitude ? coords.getStationLatitude() : coords.getStationLongitude();

        KDNode nearSide, farSide;
        if (targetValue < nodeValue) {
            nearSide = node.getLeft();
            farSide = node.getRight();
        } else {
            nearSide = node.getRight();
            farSide = node.getLeft();
        }

        findNearestNRecursive(nearSide, targetLat, targetLon, n, nearestStations, filter, !divideByLatitude);

        if (shouldExploreFarSide(targetLat, targetLon, nodeValue, divideByLatitude, nearestStations, n)) {
            findNearestNRecursive(farSide, targetLat, targetLon, n, nearestStations, filter, !divideByLatitude);
        }
    }

    /**
     * Determines if the far side of the tree should be explored.
     * This is the pruning step: if the perpendicular distance from the target to the
     * dividing line is greater than the current maximum distance in the Priority Queue,
     * the far side cannot contain any closer points.
     *
     * @param targetLat        Target latitude.
     * @param targetLon        Target longitude.
     * @param nodeValue        The coordinate value (lat or lon) of the dividing plane.
     * @param divideByLatitude True if the current split is along latitude, false for longitude.
     * @param nearestStations  Priority Queue of nearest stations found so far.
     * @param n                Number of neighbors requested.
     * @return True if the far side might contain a closer point, false otherwise.
     */
    private boolean shouldExploreFarSide(double targetLat, double targetLon, double nodeValue, boolean divideByLatitude, PriorityQueue<StationDistance> nearestStations, int n) {
        if (nearestStations.size() < n) {
            return true;
        }

        double perpendicularDistance;
        if (divideByLatitude) {
            perpendicularDistance = Math.abs(targetLat - nodeValue) * 111.0;
        } else {
            double latRad = Math.toRadians(targetLat);
            perpendicularDistance = Math.abs(targetLon - nodeValue) * 111.0 * Math.cos(latRad);
        }

        return perpendicularDistance < nearestStations.peek().distance;
    }

    /**
     * Calculates the Haversine distance between two points on Earth.
     * This formula provides an accurate great-circle distance.
     *
     * @param lat1 Latitude of point 1 (degrees).
     * @param lon1 Longitude of point 1 (degrees).
     * @param lat2 Latitude of point 2 (degrees).
     * @param lon2 Longitude of point 2 (degrees).
     * @return The distance in kilometers.
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}