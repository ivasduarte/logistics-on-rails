package lapr3.uslp03;

import java.util.List;

public class Line {

    /**
     * The unique identifier for a railway line.
     * Represents the ID assigned to a specific line, providing a way to distinguish
     * between different lines in the system.
     */
    private final int lineID;
    private final String lineName;
    private final String ownerName;
    private final int startStationID;
    private final int endStationID;
    private final int gauge;
    private List<Segment> segments;

    /**
     * Constructs a new Line object with the specified parameters.
     *
     * @param lineID         the unique identifier for the railway line
     * @param lineName       the name of the railway line
     * @param ownerName      the name of the owner of the railway line
     * @param startStationID the ID of the starting station of the railway line
     * @param endStationID   the ID of the ending station of the railway line
     * @param gauge          the track gauge of the railway line (in millimeters)
     */
    public Line(int lineID, String lineName, String ownerName, int startStationID, int endStationID, int gauge) {
        this.lineID = lineID;
        this.lineName = lineName;
        this.ownerName = ownerName;
        this.startStationID = startStationID;
        this.endStationID = endStationID;
        this.gauge = gauge;
    }

    /**
     * Retrieves the unique identifier for the railway line.
     *
     * @return the line ID of the railway line
     */
    public int getLineID() {
        return lineID;
    }

    /**
     * Retrieves the name of the railway line.
     *
     * @return the name of the railway line
     */
    public String getLineName() {
        return lineName;
    }

    /**
     * Retrieves the name of the owner of the railway line.
     *
     * @return the owner's name of the railway line
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * Retrieves the ID of the starting station for the railway line.
     *
     * @return the start station ID of the railway line
     */
    public int getStartStationID() {
        return startStationID;
    }

    /**
     * Retrieves the ID of the ending station for the railway line.
     *
     * @return the end station ID of the railway line
     */
    public int getEndStationID() {
        return endStationID;
    }

    /**
     * Retrieves the track gauge of the railway line.
     *
     * @return the gauge of the railway line in millimeters
     */
    public int getGauge() {
        return gauge;
    }

    /**
     * Retrieves the list of segments associated with this railway line.
     *
     * @return a list of Segment objects that represent the individual segments of the railway line
     */
    public List<Segment> getSegments() {
        return segments;
    }

    /**
     * Sets the list of segments associated with the railway line.
     *
     * @param segments the list of Segment objects to associate with the railway line
     */
    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }
}
