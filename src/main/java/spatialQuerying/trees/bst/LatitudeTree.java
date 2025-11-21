package spatialQuerying.trees.bst;

import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.nodes.LatitudeNode;

import java.util.ArrayList;
import java.util.List;

public class LatitudeTree {
    private LatitudeNode root;
    private static final double EPSILON = 1e-6;

    /**
     * Inserts a station into the tree based on its latitude coordinate.
     *
     * <p>
     * If a node with a matching latitude already exists (within EPSILON tolerance),
     * the station is added to that node's station list. Otherwise, a new node
     * is created. The tree automatically rebalances after insertion.
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
     * This helper method performs standard BST insertion followed by height
     * update and AVL balancing. Floating-point comparison uses EPSILON tolerance
     * to handle precision issues.
     * </p>
     *
     * @param node the root of the subtree
     * @param station the station to insert
     *
     * @return the new root of the subtree after insertion and balancing
     */
    private LatitudeNode insert(LatitudeNode node, Station station) {
        if (node == null) {
            LatitudeNode newNode = new LatitudeNode(station.getStationLatitude());
            newNode.addStation(station);
            return newNode;
        }

        double latDiff = station.getStationLatitude() - node.getLat();

        if (Math.abs(latDiff) < EPSILON) {
            node.setLeft(insert(node.getLeft(), station));
        } else {
            node.setRight(insert(node.getRight(), station));
        }

        node.setHeight(1 + Math.max(getHeight(node.getLeft()), getHeight(node.getRight())));

        return balance(node);
    }

    /**
     * Finds all stations at a specific latitude.
     *
     * <p>
     * Uses EPSILON tolerance for floating-point comparison, so latitudes
     * within 1e-6 degrees are considered equal.
     * </p>
     *
     * <p>
     * Time complexity: O(log n) where n is the number of unique latitudes.
     * </p>
     *
     * @param lat the latitude to search for
     * @return a list of stations at the specified latitude, sorted by name, or an empty list if no stations found
     */
    public List<Station> findByLatitude(double lat) {
        LatitudeNode node = findNode(root, lat);
        return node != null ? new ArrayList<>(node.getStationList()) : new ArrayList<>();
    }

    /**
     * Recursively searches for a node with the specified latitude.
     *
     * @param node the root of the subtree to search
     * @param lat the latitude to find
     *
     * @return the node with the matching latitude, or null if not found
     */
    private LatitudeNode findNode(LatitudeNode node, double lat) {
        if (node == null) {
            return null;
        }

        double diff = lat - node.getLat();

        if (Math.abs(diff) < EPSILON) {
            return node;
        } else if (diff < 0) {
            return findNode(node.getLeft(), lat);
        } else {
            return findNode(node.getRight(), lat);
        }
    }

    /**
     * Finds all stations within a latitude range (inclusive).
     *
     * <p>
     * Returns stations with latitudes in [minLat, maxLat]. Results are
     * ordered by latitude (ascending), then by station name for stations
     * with identical latitudes.
     * </p>
     *
     * @param minLat the minimum latitude (inclusive)
     * @param maxLat the maximum latitude (inclusive)
     *
     * @return a list of stations within the range, sorted by latitude then name
     */
    public List<Station> findInLatitudeRange(double minLat, double maxLat) {
        List<Station> results = new ArrayList<>();
        findInRange(root, minLat, maxLat, results);
        return results;
    }

    /**
     * Recursively collects stations within the latitude range using in-order traversal.
     *
     * @param node the current node
     * @param minLat the minimum latitude
     * @param maxLat the maximum latitude
     * @param results the list to accumulate results
     */
    private void findInRange(LatitudeNode node, double minLat, double maxLat, List<Station> results) {
        if (node == null) {
            return;
        }

        if (node.getLat() > minLat) {
            findInRange(node.getLeft(), minLat, maxLat, results);
        }
        if (node.getLat() >= minLat && node.getLat() <= maxLat) {
            results.addAll(node.getStationList());
        }
        if (node.getLat() < maxLat) {
            findInRange(node.getRight(), minLat, maxLat, results);
        }
    }

    /**
     * Returns the height of a node, or 0 if the node is null.
     *
     * @param node the node to measure
     *
     * @return the height of the node
     */
    private int getHeight(LatitudeNode node) {
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
    private int getBalance(LatitudeNode node) {
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
    private LatitudeNode balance(LatitudeNode node) {
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
     * @param node the node to rotate
     *
     * @return the new root of the subtree (rightNode)
     */
    private LatitudeNode rotateLeft(LatitudeNode node) {
        LatitudeNode rightNode = node.getRight();
        LatitudeNode T2 = rightNode.getLeft();

        rightNode.setLeft(node);
        node.setRight(T2);

        node.setHeight(Math.max(getHeight(node.getLeft()), getHeight(node.getRight())) + 1);
        rightNode.setHeight(Math.max(getHeight(rightNode.getLeft()), getHeight(rightNode.getRight())) + 1);

        return rightNode;
    }

    /**
     * Performs a right rotation around node y.
     *
     * @param node the node to rotate
     *
     * @return the new root of the subtree (leftNode)
     */
    private LatitudeNode rotateRight(LatitudeNode node) {
        LatitudeNode leftNode = node.getLeft();
        LatitudeNode T2 = leftNode.getRight();

        leftNode.setRight(node);
        node.setLeft(T2);

        node.setHeight(Math.max(getHeight(node.getLeft()), getHeight(node.getRight())) + 1);
        leftNode.setHeight(Math.max(getHeight(leftNode.getLeft()), getHeight(leftNode.getRight())) + 1);

        return leftNode;
    }

    /**
     * Returns the number of unique latitude values in the tree.
     *
     * @return the count of nodes (unique latitudes)
     */
    private int size() {
        return size(root);
    }

    /**
     * Recursively counts the nodes in the tree.
     *
     * @param node the current node
     *
     * @return the count of nodes in the subtree
     */
    private int size(LatitudeNode node) {
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
    public int totalStations(LatitudeNode node) {
        if (node == null) return 0;
        return node.getStationList().size() + totalStations(node.getLeft()) + totalStations(node.getRight());
    }

    /**
     * Returns all stations in the tree in ascending order by latitude, then by name.
     *
     * <p>
     * Uses in-order traversal to maintain sorted order.
     * </p>
     *
     * @return a list of all stations sorted by latitude and name
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
    private void inOrder(LatitudeNode node, List<Station> results) {
        if (node != null) {
            inOrder(node.getLeft(), results);
            results.addAll(node.getStationList());
            inOrder(node.getRight(), results);
        }
    }
}
