package spatialQuerying.structures.bst;

import freightMngt.domain.Station;
import spatialQuerying.structures.nodes.LongitudeNode;

import java.util.ArrayList;
import java.util.List;

public class LongitudeTree {
    private LongitudeNode root;
    private static final double EPSILON = 1e-6;

    /**
     * Inserts a station into the tree based on its longitude coordinate.
     *
     * <p>
     * If a node with a matching longitude already exists (within EPSILON tolerance),
     * the station is added to that node's station list. Otherwise, a new node is created.
     * The tree automatically rebalances after insertion.
     * </p>
     *
     * @param station the station to insert (must not be null)
     */
    public void insert(Station station) {
        root = insert(root, station);
    }

    /**
     * Recursively inserts a station into the subtree rooted at the given node.
     *
     * <p>
     * This helper method performs standard BST insertion followed by height update and AVL balancing.
     * Floating-point comparison uses EPSILON tolerance to handle precision issues.
     * </p>
     *
     * @param node the root of the subtree
     * @param station the station to insert
     *
     * @return the new root of the subtree after insertion and balancing
     */
    private LongitudeNode insert(LongitudeNode node, Station station) {
        if (node == null) {
            LongitudeNode newNode = new LongitudeNode(station.getStationLongitude());
            newNode.addStation(station);
            return newNode;
        }

        double lonDiff = station.getStationLongitude() - node.getLongitude();

        if (Math.abs(lonDiff) < EPSILON) {
            node.addStation(station);
            return node;
        } else if (lonDiff < 0) {
            node.setLeft(insert(node.getLeft(), station));
        } else {
            node.setRight(insert(node.getRight(), station));
        }

        node.setHeight(1 + Math.max(getHeight(node.getLeft()), getHeight(node.getRight())));

        return balance(node);
    }

    /**
     * Finds all stations at a specific longitude.
     *
     * <p>
     * Uses EPSILON tolerance for floating-point comparison, so longitudes
     * within 1e-6 degrees are considered equal.
     * </p>
     *
     * @param lon the longitude to search for
     *
     * @return a list of stations at the specified longitude, sorted by name, or an empty list if no stations found
     */
    public List<Station> findByLongitude(double lon) {
        LongitudeNode node = findNode(root, lon);
        return node != null ? new ArrayList<>(node.getStationList()) : new ArrayList<>();
    }

    /**
     * Recursively searches for a node with the specified longitude.
     *
     * @param node the root of the subtree to search
     * @param lon the longitude to find
     *
     * @return the node with the matching longitude, or null if not found
     */
    private LongitudeNode findNode(LongitudeNode node, double lon) {
        if (node == null) {
            return null;
        }

        double diff = lon - node.getLongitude();

        if (Math.abs(diff) < EPSILON) {
            return node;
        } else if (diff < 0) {
            return findNode(node.getLeft(), lon);
        } else {
            return findNode(node.getRight(), lon);
        }
    }

    /**
     * Finds all stations within a longitude range (inclusive).
     *
     * <p>
     * Returns stations with longitudes in [minLon, maxLon].
     * Results are ordered by longitude (ascending), then by station name for stations with identical longitudes.
     * This supports windowed queries as specified in the user story requirements.
     * </p>
     *
     * @param minLon the minimum longitude (inclusive)
     * @param maxLon the maximum longitude (inclusive)
     *
     * @return a list of stations within the range, sorted by longitude then name
     */
    public List<Station> findInLongitudeRange(double minLon, double maxLon) {
        List<Station> results = new ArrayList<>();
        findInRange(root, minLon, maxLon, results);
        return results;
    }

    /**
     * Recursively collects stations within the longitude range using in-order traversal.
     *
     * @param node the current node
     * @param minLon the minimum longitude
     * @param maxLon the maximum longitude
     * @param results the list to accumulate results
     */
    private void findInRange(LongitudeNode node, double minLon, double maxLon, List<Station> results) {
        if (node == null) {
            return;
        }

        if (node.getLongitude() > minLon) {
            findInRange(node.getLeft(), minLon, maxLon, results);
        }
        if (node.getLongitude() >= minLon && node.getLongitude() <= maxLon) {
            results.addAll(node.getStationList());
        }
        if (node.getLongitude() < maxLon) {
            findInRange(node.getRight(), minLon, maxLon, results);
        }
    }

    /**
     * Returns the height of a node, or 0 if the node is null.
     *
     * @param node the node to measure
     *
     * @return the height of the node
     */
    private int getHeight(LongitudeNode node) {
        return node == null ? 0 : node.getHeight();
    }

    /**
     * Calculates the balance factor of a node.
     *
     * <p>
     * Balance factor = height(left subtree) - height(right subtree).
     * A balance factor of -1, 0, or 1 indicates the node is balanced.
     * </p>
     *
     * @param node the node to measure
     *
     * @return the balance factor
     */
    private int getBalance(LongitudeNode node) {
        return node == null ? 0 : getHeight(node.getLeft()) - getHeight(node.getRight());
    }

    /**
     * Balances a node if it has become unbalanced.
     *
     * <p>
     * Determines the appropriate rotation(s) needed based on the balance factor:
     * <ul>
     *   <li>Left-heavy: Right rotation or Left-Right rotation</li>
     *   <li>Right-heavy: Left rotation or Right-Left rotation</li>
     * </ul>
     * </p>
     *
     * @param node the node to balance
     *
     * @return the new root of the balanced subtree
     */
    private LongitudeNode balance(LongitudeNode node) {
        int balance = getBalance(node);

        if (balance > 1) {
            if (getBalance(node.getLeft()) < 0) {
                node.setLeft(rotateLeft(node.getLeft()));
            }
            return rotateRight(node);
        }

        if (balance < -1) {
            if (getBalance(node.getRight()) > 0) {
                node.setRight(rotateRight(node.getRight()));
            }
            return rotateLeft(node);
        }

        return node;
    }

    /**
     * Performs a left rotation around the node.
     *
     * <p>
     * The subtree T2 must be preserved and reconnected as the right child of the node.
     * </p>
     *
     * @param node the node to rotate
     *
     * @return the new root of the subtree (rightNode)
     */
    private LongitudeNode rotateLeft(LongitudeNode node) {
        LongitudeNode rightNode = node.getRight();
        LongitudeNode T2 = rightNode.getLeft();

        rightNode.setLeft(node);
        node.setRight(T2);

        node.setHeight(Math.max(getHeight(node.getLeft()), getHeight(node.getRight())) + 1);
        rightNode.setHeight(Math.max(getHeight(rightNode.getLeft()), getHeight(rightNode.getRight())) + 1);

        return rightNode;
    }

    /**
     * Performs a right rotation around the node.
     *
     * <p>
     * The subtree T2 must be preserved and reconnected as the left child of the node.
     * </p>
     *
     * @param node the node to rotate
     *
     * @return the new root of the subtree (leftNode)
     */
    private LongitudeNode rotateRight(LongitudeNode node) {
        LongitudeNode leftNode = node.getLeft();
        LongitudeNode T2 = leftNode.getRight();

        leftNode.setRight(node);
        node.setLeft(T2);

        node.setHeight(Math.max(getHeight(node.getLeft()), getHeight(node.getRight())) + 1);
        leftNode.setHeight(Math.max(getHeight(leftNode.getLeft()), getHeight(leftNode.getRight())) + 1);

        return leftNode;
    }

    /**
     * Returns the number of unique longitude values in the tree.
     *
     * @return the count of nodes (unique longitudes)
     */
    public int size() {
        return size(root);
    }

    /**
     * Recursively counts the nodes in the tree.
     *
     * @param node the current node
     *
     * @return the count of nodes in the subtree
     */
    private int size(LongitudeNode node) {
        if (node == null) return 0;
        return 1 + size(node.getLeft()) + size(node.getRight());
    }

    /**
     * Returns the total number of stations stored in the tree.
     *
     * @return the total count of all stations
     */
    public int totalStations() {
        return totalStations(root);
    }

    /**
     * Recursively counts all stations in the tree.
     *
     * @param node the current node
     *
     * @return the total count of stations in the subtree
     */
    public int totalStations(LongitudeNode node) {
        if (node == null) return 0;
        return node.getStationList().size() + totalStations(node.getLeft()) + totalStations(node.getRight());
    }

    /**
     * Returns all stations in the tree in ascending order by longitude, then by name.
     *
     * <p>
     * Uses in-order traversal to maintain sorted order.
     * </p>
     *
     * @return a list of all stations sorted by longitude and name
     */
    public List<Station> inOrderTraversal() {
        List<Station> results = new ArrayList<>();
        inOrder(root, results);
        return results;
    }

    /**
     * Recursively performs in-order traversal.
     *
     * @param node the current node
     * @param results the list to accumulate results
     */
    private void inOrder(LongitudeNode node, List<Station> results) {
        if (node != null) {
            inOrder(node.getLeft(), results);
            results.addAll(node.getStationList());
            inOrder(node.getRight(), results);
        }
    }
}
