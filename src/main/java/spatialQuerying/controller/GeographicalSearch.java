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

    /**
     * Constructs a new instance of GeographicalSearch using the specified KD-Tree.
     *
     * @param kdTree the KD-Tree data structure that organizes geographical points
     *               based on latitude and longitude, to be used for performing
     *               geographical searches. Must not be null.
     */
    public GeographicalSearch(KDTree2D kdTree) {
        this.kdTree = kdTree;
    }

    /**
     * Searches for stations located at the specified geographical coordinates.
     * The search is performed in a KD-Tree structure, which efficiently organizes
     * and searches stations by latitude and longitude.
     *
     * @param latitude the latitude of the target location, must be between -90 and 90 (inclusive)
     * @param longitude the longitude of the target location, must be between -180 and 180 (inclusive)
     * @return a list of all stations at the specified coordinates, or an empty list if none found
     */
    public List<Station> searchByCoordinates(double latitude, double longitude) {
        return searchByCoordinates(latitude, longitude, null, null, null);
    }

    /**
     * Searches for stations located at the specified geographical coordinates with optional filters.
     * The search is performed in a KD-Tree structure, which efficiently organizes
     * and searches stations by latitude and longitude.
     *
     * @param latitude the latitude of the target location, must be between -90 and 90 (inclusive)
     * @param longitude the longitude of the target location, must be between -180 and 180 (inclusive)
     * @param isCity filter by city status (true/false), or null for no filter
     * @param isMainStation filter by main station status (true/false), or null for no filter
     * @param country filter by country code ("PT", "ES", or "all"), or null for no filter
     * @return a list of stations matching the coordinates and filters, or an empty list if none found
     */
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

    /**
     * Applies the specified filters to a list of stations.
     *
     * @param stations the list of stations to filter
     * @param isCity filter by city status (true/false), or null for no filter
     * @param isMainStation filter by main station status (true/false), or null for no filter
     * @param country filter by country code ("PT", "ES", or "all"), or null for no filter
     * @return a filtered list of stations matching all specified criteria
     */
    private List<Station> applyFilters(List<Station> stations, Boolean isCity,
                                       Boolean isMainStation, String country) {
        return stations.stream()
                .filter(station -> isCity == null || station.isCity() == isCity)
                .filter(station -> isMainStation == null || station.isMainStation() == isMainStation)
                .filter(station -> country == null || country.equalsIgnoreCase("all")
                        || station.getStationCountry().equalsIgnoreCase(country))
                .collect(Collectors.toList());
    }

    /**
     * Validates whether the provided latitude and longitude coordinates
     * are within the acceptable range of values.
     *
     * @param latitude the latitude value to validate, must be between -90 and 90 (inclusive)
     * @param longitude the longitude value to validate, must be between -180 and 180 (inclusive)
     * @return true if the provided coordinates are valid, false otherwise
     */
    private boolean isValidCoordinates(double latitude, double longitude) {
        return latitude >= LAT_MIN && latitude <= LAT_MAX &&
                longitude >= LON_MIN && longitude <= LON_MAX;
    }

    /**
     * Recursively searches for a node in a KD-Tree that matches the given target coordinates.
     * The tree is traversed by alternating between latitude and longitude as the division criteria.
     *
     * @param node the current node being evaluated during the search
     * @param target the target coordinates to search for in the KD-Tree
     * @param divideByLatitude a boolean indicating whether the search currently divides by latitude
     *                         (true for latitude, false for longitude)
     * @return the KDNode that matches the target coordinates, or null if no such node exists
     */
    private KDNode searchNode(KDNode node, Point2D target, boolean divideByLatitude) {
        if (node == null) {
            return null;
        }

        // Check if we found the target
        if (node.getCoordinates().equals(target)) {
            return node;
        }

        // Determine which subtree to search based on current dimension
        boolean goLeft;
        if (divideByLatitude) {
            goLeft = target.getStationLatitude() < node.getCoordinates().getStationLatitude();
        } else {
            goLeft = target.getStationLongitude() < node.getCoordinates().getStationLongitude();
        }

        // Recursively search the appropriate subtree
        if (goLeft) {
            return searchNode(node.getLeft(), target, !divideByLatitude);
        } else {
            return searchNode(node.getRight(), target, !divideByLatitude);
        }
    }
}