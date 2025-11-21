package spatialQuerying.trees.nodes;

import spatialQuerying.railwayMngt.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LongitudeNode {
    private double lon;
    private List<Station> stationList;
    private LongitudeNode left;
    private LongitudeNode right;
    private int height;

    /**
     * Constructs a new LongitudeNode for the specified longitude.
     *
     * <p>
     * Initializes an empty station list and sets the initial height to 1
     * (leaf node height).
     * </p>
     *
     * @param lon the longitude value this node represents
     */
    public LongitudeNode(double lon) {
        this.lon = lon;
        this.stationList = new ArrayList<>();
        this.height = 1;
    }

    /**
     * Adds a station to this node and maintains alphabetical ordering.
     *
     * <p>
     * After adding the station, the list is re-sorted by station name in ascending order.
     * This ensures that stations with identical coordinates are always returned in alphabetical order.
     * </p>
     *
     * @param station the station to add to this node
     */
    public void addStation(Station station) {
        stationList.add(station);
        stationList.sort(Comparator.comparing(Station::getStationName));
    }

    /**
     * Returns the longitude value represented by this node.
     *
     * @return the longitude in decimal degrees
     */
    public double getLongitude() {
        return lon;
    }

    /**
     * Returns the list of stations at this longitude.
     *
     * <p>
     * The returned list is sorted by station name in ascending order.
     * </p>
     *
     * @return the stations list
     */
    public List<Station> getStationList() {
        return stationList;
    }

    /**
     * Returns the left child of this node.
     *
     * <p>
     * The left child contains stations with longitude values less than this node's longitude.
     * </p>
     *
     * @return the left child node, or null if no left child exists
     */
    public LongitudeNode getLeft() {
        return left;
    }

    /**
     * Returns the right child of this node.
     *
     * <p>
     * The right child contains stations with longitude values greater than this node's longitude.
     * </p>
     *
     * @return the right child node, or null if no right child exists
     */
    public LongitudeNode getRight() {
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
    public void setLeft(LongitudeNode left) {
        this.left = left;
    }

    /**
     * Sets the right child of this node.
     *
     * @param right the new right child node
     */
    public void setRight(LongitudeNode right) {
        this.right = right;
    }

    /**
     * Sets the height of this node.
     *
     * <p>
     * This method is used during tree balancing operations to update the node's height after rotations.
     * </p>
     *
     * @param height the new height value
     */
    public void setHeight(int height) {
        this.height = height;
    }
}
