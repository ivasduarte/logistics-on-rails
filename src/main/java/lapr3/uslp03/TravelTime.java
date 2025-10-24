package lapr3.uslp03;

import java.util.List;

public class TravelTime {

    /**
     * Represents the locomotive used in travel calculations.
     * This variable is part of the travel time calculations and provides characteristics
     * and capabilities of the locomotive, such as speed and other attributes, that
     * influence the overall travel time and performance on the rail line.
     * The locomotive is immutable and defined during the construction of the containing object.
     */
    private final Locomotive locomotive;
    private final Line line;

    /**
     * Constructs a TravelTime object using a specified locomotive and rail line.
     *
     * @param locomotive the locomotive used for travel time calculations. It determines
     *                   the performance characteristics such as speed and other attributes.
     * @param line       the rail line used for travel calculations. It defines the segments
     *                   and total distance of the travel path.
     */
    public TravelTime(Locomotive locomotive, Line line) {
        this.locomotive = locomotive;
        this.line = line;
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
     * Retrieves the rail line associated with this travel time calculation.
     *
     * @return the rail line used in travel calculations, representing the travel path
     *         and its associated properties such as segments and track details.
     */
    public Line getLine() {
        return line;
    }

    /**
     * Calculates the total travel distance of the railway line by summing the lengths
     * of all segments that compose the line.
     *
     * @return the total travel distance in meters
     */
    public int calculateTravelDistance() {
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
        return (double) distance / locomotive.getMaxSpeed();
    }
}
