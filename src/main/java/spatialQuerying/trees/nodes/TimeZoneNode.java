package spatialQuerying.trees.nodes;

import spatialQuerying.railwayMngt.Station;

import java.util.*;

public class TimeZoneNode {
    private String timeZoneGroup;
    private Map<String, List<Station>> stationListByCountry;
    private TimeZoneNode left;
    private TimeZoneNode right;
    private int height;

    public TimeZoneNode(String timeZoneGroup) {
        this.timeZoneGroup = timeZoneGroup;
        this.stationListByCountry = new HashMap<>();
        this.height = 1;
    }

    public void addStation(Station station) {
        String country = station.getStationCountry();

        stationListByCountry.putIfAbsent(country, new ArrayList<>());
        List<Station> countryStations = stationListByCountry.get(country);

        countryStations.add(station);
        countryStations.sort(Comparator.comparing(Station::getStationName));
    }

    public String getTimeZoneGroup() {
        return timeZoneGroup;
    }
    public Map<String, List<Station>> getStationListByCountry() {
        return stationListByCountry;
    }
    public TimeZoneNode getLeft() {
        return left;
    }
    public TimeZoneNode getRight() {
        return right;
    }
    public int getHeight() {
        return height;
    }

    public void setLeft(TimeZoneNode left) {
        this.left = left;
    }
    public void setRight(TimeZoneNode right) {
        this.right = right;
    }
    public void setHeight(int height) {
        this.height = height;
    }

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

    public Set<String> getCountries() {
        return stationListByCountry.keySet();
    }
}
