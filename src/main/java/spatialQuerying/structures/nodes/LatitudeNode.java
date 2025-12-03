package spatialQuerying.structures.nodes;

import freightMngt.domain.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LatitudeNode {
    private double lat;
    private List<Station> stationList;
    private int height;

    private LatitudeNode left;
    private LatitudeNode right;

    /**
     * Constructs a new LatitudeNode for the specified latitude.
     *
     * <p>
     * Initializes an empty station list and sets the initial height to 1
     * (leaf node height).
     * </p>
     *
     * @param lat the latitude value this node represents
     */
    public LatitudeNode(double lat) {
        this.lat = lat;
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
     * Retrieves the latitude value represented by this node.
     *
     * @return latitude
     */
    public double getLat() {
        return lat;
    }

    /**
     * Retrieves the list of stations of this latitude.
     * <p>
     *      The returned list is sorted by station name, in ascending order.
     * </p>
     *
     * @return stations list
     */
    public List<Station> getStationList() {
        return stationList;
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
     * Returns the left child of this node.
     *
     * <p>
     * The left child contains stations with latitude values less than
     * this node's latitude.
     * </p>
     *
     * @return the left child node, or null if no left child exists
     */
    public LatitudeNode getLeft() {
        return left;
    }

    /**
     * Returns the right child of this node.
     *
     * <p>
     * The right child contains stations with latitude values greater than
     * this node's latitude.
     * </p>
     *
     * @return the right child node, or null if no right child exists
     */
    public LatitudeNode getRight() {
        return right;
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
     * Sets the left child of this node.
     *
     * @param left the new left child node
     */
    public void setLeft(LatitudeNode left) {
        this.left = left;
    }

    /**
     * Sets the right child of this node.
     *
     * @param right the new right child node
     */
    public void setRight(LatitudeNode right) {
        this.right = right;
    }
}



