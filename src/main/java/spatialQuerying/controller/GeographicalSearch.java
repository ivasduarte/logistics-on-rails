package spatialQuerying.controller;

import freightMngt.domain.Station;
import spatialQuerying.structures.kd.KDTree2D;
import spatialQuerying.structures.nodes.KDNode;

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
     * @param kdTree the KD-Tree data structure. Must not be null.
     */
    public GeographicalSearch(KDTree2D kdTree) {
        this.kdTree = kdTree;
    }

    /**
     * Searches for stations within a specified geographical area, applying optional filters for
     * station attributes such as city status, main station designation, and country.
     *
     * @param latMin the minimum latitude of the search area
     * @param latMax the maximum latitude of the search area
     * @param lonMin the minimum longitude of the search area
     * @param lonMax the maximum longitude of the search area
     * @param isCity optional filter indicating whether to include only stations located in a city;
     *               if null, this filter is ignored
     * @param isMainStation optional filter indicating whether to include only main stations;
     *                      if null, this filter is ignored
     * @param country optional filter to include only stations within the specified country;
     *                if null, this filter is ignored
     * @return a list of stations matching the specified geographical area and filter criteria
     * @throws IllegalArgumentException if the provided coordinate ranges are invalid
     */
    public List<Station> searchByArea(double latMin, double latMax, double lonMin, double lonMax,
                                      Boolean isCity, Boolean isMainStation, String country) {

        if (!isValidRange(latMin, latMax, lonMin, lonMax)) {
            throw new IllegalArgumentException("Invalid coordinates range.");
        }

        List<Station> stationsInArea = new ArrayList<>();

        searchAreaRecursive(kdTree.getRoot(), latMin, latMax, lonMin, lonMax, true, stationsInArea);

        return applyFilters(stationsInArea, isCity, isMainStation, country);
    }

    /**
     * Recursively searches the KD-Tree for nodes within the bounding box (Range Search).
     * Implements pruning to avoid scanning the whole dataset.
     *
     * @param node current node
     * @param latMin search area min latitude
     * @param latMax search area max latitude
     * @param lonMin search area min longitude
     * @param lonMax search area max longitude
     * @param divideByLatitude true if splitting by latitude, false if by longitude
     * @param result list to collect matching stations
     */
    private void searchAreaRecursive(KDNode node, double latMin, double latMax,
                                     double lonMin, double lonMax,
                                     boolean divideByLatitude, List<Station> result) {
        if (node == null) {
            return;
        }

        double nodeLat = node.getCoordinates().getStationLatitude();
        double nodeLon = node.getCoordinates().getStationLongitude();

        boolean inLatRange = nodeLat >= latMin && nodeLat <= latMax;
        boolean inLonRange = nodeLon >= lonMin && nodeLon <= lonMax;

        if (inLatRange && inLonRange) {
            result.addAll(node.getStationList());
        }

        if (divideByLatitude) {
            if (latMin <= nodeLat) {
                searchAreaRecursive(node.getLeft(), latMin, latMax, lonMin, lonMax, !divideByLatitude, result);
            }
            if (latMax >= nodeLat) {
                searchAreaRecursive(node.getRight(), latMin, latMax, lonMin, lonMax, !divideByLatitude, result);
            }
        } else {
            if (lonMin <= nodeLon) {
                searchAreaRecursive(node.getLeft(), latMin, latMax, lonMin, lonMax, !divideByLatitude, result);
            }
            if (lonMax >= nodeLon) {
                searchAreaRecursive(node.getRight(), latMin, latMax, lonMin, lonMax, !divideByLatitude, result);
            }
        }
    }

    /**
     * Applies the specified filters to a list of stations.
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
     * Validates input ranges.
     */
    private boolean isValidRange(double latMin, double latMax, double lonMin, double lonMax) {
        return latMin >= LAT_MIN && latMax <= LAT_MAX &&
                lonMin >= LON_MIN && lonMax <= LON_MAX &&
                latMin <= latMax && lonMin <= lonMax;
    }
}