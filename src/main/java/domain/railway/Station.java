package domain.railway;

public class Station {

    /**
     * The unique identifier for a station.
     * Represents the ID assigned to a specific station, enabling its distinction
     * within the railway system.
     */
    private final int stationID;
    private final String stationName;

    /**
     * Constructs a new Station object with the specified ID and name.
     *
     * @param stationID   the unique identifier for the station
     * @param stationName the name of the station
     */
    public Station(int stationID, String stationName) {
        this.stationID = stationID;
        this.stationName = stationName;
    }

    /**
     * Retrieves the unique identifier for the station.
     *
     * @return the station ID of the station
     */
    public int getStationID() {
        return stationID;
    }

    /**
     * Retrieves the name of the station.
     *
     * @return the name of the station
     */
    public String getStationName() {
        return stationName;
    }
}
