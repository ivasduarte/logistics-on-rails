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

    public ProximitySearchController(KDTree2D kdTree) {
        this.kdTree = kdTree;
    }

    private static class StationDistance {
        Station station;
        double distance;

        StationDistance(Station station, double distance) {
            this.station = station;
            this.distance = distance;
        }
    }

    public Map<String, Object> findNearestStations(double targetLat, double targetLon, int n) {
        return findNearestStations(targetLat, targetLon, n, null);
    }

    public Map<String, Object> findNearestStations(double targetLat, double targetLon, int n, String timeZoneFilter) {
        long startTime = System.nanoTime();

        List<Station> resultStations = new ArrayList<>();
        List<Double> resultDistances = new ArrayList<>();

        if (kdTree.getRoot() == null || n <= 0) {
            long endTime = System.nanoTime();
            return buildResponse(resultStations, resultDistances, targetLat, targetLon, n,
                    timeZoneFilter, endTime - startTime);
        }

        PriorityQueue<StationDistance> nearestStations = new PriorityQueue<>(
                (a, b) -> Double.compare(b.distance, a.distance)
        );

        Predicate<Station> filter = timeZoneFilter == null ?
                s -> true :
                s -> timeZoneFilter.equalsIgnoreCase(s.getStationTimeZoneGroup());

        findNearestNRecursive(kdTree.getRoot(), targetLat, targetLon, n, nearestStations, filter, true);

        List<StationDistance> tempList = new ArrayList<>();
        while (!nearestStations.isEmpty()) {
            tempList.add(0, nearestStations.poll());
        }

        for (StationDistance sd : tempList) {
            resultStations.add(sd.station);
            resultDistances.add(sd.distance);
        }

        long endTime = System.nanoTime();
        return buildResponse(resultStations, resultDistances, targetLat, targetLon, n,
                timeZoneFilter, endTime - startTime);
    }


    private Map<String, Object> buildResponse(List<Station> stations, List<Double> distances,
                                              double targetLat, double targetLon, int n,
                                              String timeZoneFilter, long executionTimeNanos) {
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

        Map<String, Long> performanceMetrics = new HashMap<>();
        performanceMetrics.put("executionTimeNanos", executionTimeNanos);
        response.put("performanceMetrics", performanceMetrics);

        List<String> appliedFilters = new ArrayList<>();
        if (timeZoneFilter != null && !timeZoneFilter.isEmpty()) {
            appliedFilters.add(timeZoneFilter);
        }
        response.put("appliedFilters", appliedFilters);

        return response;
    }

    private void findNearestNRecursive(KDNode node,
                                       double targetLat,
                                       double targetLon,
                                       int n,
                                       PriorityQueue<StationDistance> nearestStations,
                                       Predicate<Station> filter,
                                       boolean divideByLatitude) {
        if (node == null) return;

        Point2D coords = node.getCoordinates();
        double distance = haversineDistance(targetLat, targetLon,
                coords.getStationLatitude(),
                coords.getStationLongitude());

        for (Station station : node.getStationList()) {
            if (filter.test(station)) {
                if (nearestStations.size() < n) {
                    nearestStations.offer(new StationDistance(station, distance));
                } else if (distance < nearestStations.peek().distance) {
                    nearestStations.poll();
                    nearestStations.offer(new StationDistance(station, distance));
                }
            }
        }

        double targetValue = divideByLatitude ? targetLat : targetLon;
        double nodeValue = divideByLatitude ?
                coords.getStationLatitude() : coords.getStationLongitude();

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

    private boolean shouldExploreFarSide(double targetLat, double targetLon,
                                         double nodeValue, boolean divideByLatitude,
                                         PriorityQueue<StationDistance> nearestStations, int n) {
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

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}