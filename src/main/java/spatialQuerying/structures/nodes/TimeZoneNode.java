package spatialQuerying.structures.nodes;

import freightMngt.domain.Station;

import java.util.*;

public class TimeZoneNode {
    private String timeZoneGroup;
    private Map<String, List<Station>> stationListByCountry;
    private TimeZoneNode left;
    private TimeZoneNode right;
    private int height;

    /**
     * Constructs a new TimeZoneNode for the specified time zone group.
     *
     * <p>
     * Initializes an empty map for country-to-stations organization
     * and sets the initial height to 1 (leaf node height).
     * </p>
     *
     * @param timeZoneGroup the time zone group identifier (e.g., "CET", "EET")
     */
    public TimeZoneNode(String timeZoneGroup) {
        this.timeZoneGroup = timeZoneGroup;
        this.stationListByCountry = new HashMap<>();
        this.height = 1;
    }

    /**
     * Adds a station to this node, organizing it by country.
     *
     * <p>
     * The station is added to the list for its country.
     * If this is the first station from that country in this time zone group, a new list is created.
     * After adding, the country's station list is re-sorted
     * alphabetically to maintain ascending order by name.
     * </p>
     *
     * @param station the station to add
     */
    public void addStation(Station station) {
        String country = station.getStationCountry();

        stationListByCountry.putIfAbsent(country, new ArrayList<>());
        List<Station> countryStations = stationListByCountry.get(country);

        countryStations.add(station);
        countryStations.sort(Comparator.comparing(Station::getStationName));
    }

    /**
     * Returns the time zone group represented by this node.
     *
     * @return the time zone group identifier
     */
    public String getTimeZoneGroup() {
        return timeZoneGroup;
    }

    /**
     * Returns the map organizing stations by country.
     *
     * <p>
     * The map structure is: Country Code → List of Stations (sorted by name)
     * </p>
     *
     * @return the stations-by-country map
     */
    public Map<String, List<Station>> getStationListByCountry() {
        return stationListByCountry;
    }

    /**
     * Returns the left child of this node.
     *
     * <p>
     * The left child contains time zone groups that are lexicographically
     * less than this node's time zone group.
     * </p>
     *
     * @return the left child node, or null if no left child exists
     */
    public TimeZoneNode getLeft() {
        return left;
    }

    /**
     * Returns the right child of this node.
     *
     * <p>
     * The right child contains time zone groups that are lexicographically
     * greater than this node's time zone group.
     * </p>
     *
     * @return the right child node, or null if no right child exists
     */
    public TimeZoneNode getRight() {
        return right;
    }

    /**
     * Returns the height of this node in the tree.
     *
     * <p>
     * Height is defined as the longest path from this node to a leaf.
     * Leaf nodes have height 1.
     * </p>
     *
     * @return the height of this node
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the left child of this node.
     *
     * @param left the new left child node
     */
    public void setLeft(TimeZoneNode left) {
        this.left = left;
    }

    /**
     * Sets the right child of this node.
     *
     * @param right the new right child node
     */
    public void setRight(TimeZoneNode right) {
        this.right = right;
    }

    /**
     * Sets the height of this node.
     *
     * <p>
     * This method is used during tree balancing operations to update
     * the node's height after rotations.
     * </p>
     *
     * @param height the new height value
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Retrieves stations from this node, optionally filtered by country.
     *
     * <p>
     * If country is null, returns all stations from all countries in this
     * time zone group, sorted alphabetically by name.
     * If country is specified, returns only stations from that country.
     * </p>
     *
     * @param country the country code to filter by, or null for all countries
     *
     * @return a list of stations matching the criteria, sorted by name
     */
    public List<Station> getStationsByTimeZoneGroup(String country) {
        if (country != null) {
            return stationListByCountry.getOrDefault(country, new ArrayList<>());
        }

        List<Station> allStations = new ArrayList<>();
        for (List<Station> countryList : stationListByCountry.values()) {
            allStations.addAll(countryList);
        }

        allStations.sort(Comparator.comparing(Station::getStationName));
        return allStations;
    }

    /**
     * Returns the set of countries that have stations in this time zone group.
     *
     * @return a set of country codes
     */
    public Set<String> getCountries() {
        return stationListByCountry.keySet();
    }
}
