package spatialQuerying.railwayMngt;

public class Station {

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
     * Constructs a Station Object with the parameters listed below.
     *
     * @param stationName the name of the station
     * @param stationCountry the country of the station
     * @param stationTimeZone the station time zone
     * @param stationTimeZoneGroup the station time zone group
     * @param stationLatitude the station latitude
     * @param stationLongitude the station longitude
     * @param isCity true if this station is located in a city
     * @param isMainStation true if this is a main station
     * @param isAirport true if this station serves an airport
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

    /**
     * Retrieves the country of the station.
     *
     * @return the station's country
     */
    public String getStationCountry() {
        return stationCountry;
    }

    /**
     * Retrieves the time zone of the station.
     *
     * @return the station's time zone
     */
    public String getStationTimeZone() {
        return stationTimeZone;
    }

    /**
     * Retrieves the time zone group of the station.
     *
     * @return the station's time zone group
     */
    public String getStationTimeZoneGroup() {
        return stationTimeZoneGroup;
    }

    /**
     * Retrieves the latitude of the station.
     *
     * @return the station's latitude
     */
    public double getStationLatitude() {
        return stationLatitude;
    }

    /**
     * Retrieves the longitude of the station.
     *
     * @return the station's longitude
     */
    public double getStationLongitude() {
        return stationLongitude;
    }

    /**
     * Indicates whether this station is located in a city.
     *
     * @return true if the station is in a city, false otherwise
     */
    public boolean isCity() {
        return isCity;
    }

    /**
     * Indicates whether this is a main station.
     *
     * @return true if this is a main station, false otherwise
     */
    public boolean isMainStation() {
        return isMainStation;
    }

    /**
     * Indicates whether this station serves an airport.
     *
     * @return true if the station serves an airport, false otherwise
     */
    public boolean isAirport() {
        return isAirport;
    }

    /**
     * Returns a string representation of this station.
     * <p>
     * The string includes the station name, country, time zone, time zone group
     * and coordinates for easy identification and debugging.
     * </p>
     *
     * @return a string representation of this station
     */
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
