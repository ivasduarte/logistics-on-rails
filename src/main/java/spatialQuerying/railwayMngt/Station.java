package spatialQuerying.railwayMngt;

public class Station {

    /**
     * The unique identifier for a station.
     * Represents the ID assigned to a specific station, enabling its distinction
     * within the railway system.
     */
    private final String stationName;

    private final String stationCountry;
    private final String stationTimeZone;
    private final String stationTimeZoneGroup;
    private final double stationLatitude;
    private final double stationLongitude;

    private boolean isCity;
    private boolean isMainStation;
    private boolean isAirport;

    /**
     * Constructs a new Station object with the specified ID and name.
     *
     * @param stationName the name of the station
     */
    public Station(String stationName, String stationCountry, String stationTimeZone ,String stationTimeZoneGroup,
                   double stationLatitude, double stationLongitude,
                   boolean isCity, boolean isMainStation, boolean isAirport) {

        this.stationName = stationName;
        this.stationCountry = stationCountry;
        this.stationTimeZone = stationTimeZone;
        this.stationTimeZoneGroup = stationTimeZoneGroup;
        this.stationLatitude = stationLatitude;
        this.stationLongitude = stationLongitude;
        this.isCity = isCity;
        this.isMainStation = isMainStation;
        this.isAirport = isAirport;
    }

    /**
     * Retrieves the name of the station.
     *
     * @return the name of the station
     */
    public String getStationName() {
        return stationName;
    }

    public String getStationCountry() {
        return stationCountry;
    }

    public String getStationTimeZone() {
        return stationTimeZone;
    }

    public String getStationTimeZoneGroup() {
        return stationTimeZoneGroup;
    }

    public double getStationLatitude() {
        return stationLatitude;
    }

    public double getStationLongitude() {
        return stationLongitude;
    }

    public boolean isCity() {
        return isCity;
    }

    public boolean isMainStation() {
        return isMainStation;
    }

    public boolean isAirport() {
        return isAirport;
    }

    @Override
    public String toString() {
        return "Station " + stationName +
                "\n Country: " + stationCountry +
                "\n Time Zone: " + stationTimeZone +
                "\n Time Zone Group: " + stationTimeZoneGroup +
                "\n Latitude: " + stationLatitude +
                "\n Longitude: " + stationLongitude;
    }
}
