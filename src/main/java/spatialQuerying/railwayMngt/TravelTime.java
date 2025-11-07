package spatialQuerying.railwayMngt;

import java.util.List;

public class TravelTime {

    private final Locomotive locomotive;
    private final List<Station> stations;

    /**
     * Constructs a TravelTime object, initializing the locomotive to be used
     * and the start and end stations for the travel.
     *
     * @param locomotive the locomotive used for travel calculations, which
     *                   provides characteristics such as speed and performance.
     * @param startStation the station where the travel begins.
     * @param endStation the station where the travel ends.
     */
    public TravelTime(Locomotive locomotive, Station startStation, Station endStation) {
        this.locomotive = locomotive;
        this.stations = List.of(startStation, endStation);
    }

    /**
     * Retrieves the locomotive associated with this travel time calculation.
     *
     * @return the locomotive used in travel calculations, providing its characteristics
     *         and capabilities such as speed and performance.
     */
    public Locomotive getLocomotive() {
        return locomotive;
    }

    /**
     * Retrieves the list of stations associated with this travel.
     *
     * @return a list of Station objects representing the stations involved in this travel
     */
    public List<Station> getStations() {
        return stations;
    }

    /**
     * Finds a railway line that directly connects the start station and the end station
     * from the provided list of lines.
     *
     * @param lines a list of Line objects, representing available railway lines
     * @return a Line object representing the direct connection between the start
     *         and end station
     * @throws IllegalArgumentException if no direct line connects the start and end stations
     */
    private Line findConnectingLine(List<Line> lines) {
        int startStationId = stations.get(0).getStationID();
        int endStationId = stations.get(1).getStationID();

        return lines.stream()
                .filter(l -> (l.getStartStationID() == startStationId && l.getEndStationID() == endStationId) ||
                        (l.getStartStationID() == endStationId && l.getEndStationID() == startStationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No direct line connects these stations"));
    }

    /**
     * Calculates the total travel distance of the railway line by summing the lengths
     * of all segments that compose the line.
     *
     * @return the total travel distance in meters
     */
    public int calculateTravelDistance(List<Line> lines) {
        Line line = findConnectingLine(lines);

        List<Segment> segments = line.getSegments();
        int travelDistance = 0;

        for (Segment segment : segments) {
            travelDistance += segment.getLength();
        }

        return travelDistance;
    }

    /**
     * Calculates the travel time based on the provided distance and the locomotive's maximum speed.
     *
     * @param distance the distance to travel in meters
     * @return the travel time in hours as a double
     */
    public double calculateTravelTime(int distance) {
        final double speedConversion = 3.6;
        return (double) distance / ( (double) locomotive.getMaxSpeed() / speedConversion);
    }
}
