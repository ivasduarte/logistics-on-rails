package lapr3.uslp03;

import java.util.List;

public class Segment {

    /**
     * The unique identifier for a segment within a railway system.
     * Represents the ID assigned to a specific segment, allowing its distinction
     * in the context of associated railway lines and their divisions.
     */
    private final int segmentID;
    private final int lineID;
    private final int order;
    private final boolean electrified;
    private final int maxWeight;
    private final int length;
    private final int numberTracks;

    /**
     * Constructs a new Segment object with the specified parameters.
     *
     * @param segmentID   the unique identifier for the segment within the railway system
     * @param lineID      the unique identifier for the associated railway line
     * @param order       the sequential order of the segment within its associated line
     * @param electrified whether the segment is electrified
     * @param maxWeight   the maximum allowable weight on the segment, typically in tons
     * @param length      the length of the segment, typically in meters
     * @param numberTracks the number of tracks within the segment
     */
    public Segment(int segmentID, int lineID, int order, boolean electrified, int maxWeight, int length, int numberTracks) {
        this.segmentID = segmentID;
        this.lineID = lineID;
        this.order = order;
        this.electrified = electrified;
        this.maxWeight = maxWeight;
        this.length = length;
        this.numberTracks = numberTracks;
    }

    /**
     * Retrieves the unique identifier for this segment.
     *
     * @return the segment ID of this segment
     */
    public int getSegmentID() {
        return segmentID;
    }

    /**
     * Retrieves the unique identifier for the railway line associated with this segment.
     *
     * @return the line ID associated with this segment
     */
    public int getLineID() {
        return lineID;
    }

    /**
     * Retrieves the sequential order of the segment within its associated railway line.
     *
     * @return the order of the segment in the associated railway line
     */
    public int getOrder() {
        return order;
    }

    /**
     * Determines whether this segment is electrified.
     *
     * @return true if the segment is electrified, false otherwise
     */
    public boolean isElectrified() {
        return electrified;
    }

    /**
     * Retrieves the maximum allowable weight for this segment.
     *
     * @return the maximum weight in tons that can be supported by this segment
     */
    public int getMaxWeight() {
        return maxWeight;
    }

    /**
     * Retrieves the length of the segment.
     *
     * @return the length of the segment in meters
     */
    public int getLength() {
        return length;
    }

    /**
     * Retrieves the number of tracks within this segment.
     *
     * @return the number of tracks in the segment
     */
    public int getNumberTracks() {
        return numberTracks;
    }
}
