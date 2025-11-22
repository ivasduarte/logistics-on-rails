package spatialQuerying.trees.bst;

import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.nodes.TimeZoneNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TimeZoneTree {
    private TimeZoneNode root;

    /**
     * Inserts a station into the tree based on its time zone group and country.
     *
     * <p>
     * If a node for the station's time zone group already exists, the station
     * is added to that node's country-organized structure. Otherwise, a new node is created.
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
     * This helper method performs standard BST insertion using lexicographic
     * comparison of time zone group strings, followed by height update and
     * AVL balancing.
     * </p>
     *
     * @param node the root of the subtree
     * @param station the station to insert
     *
     * @return the new root of the subtree after insertion and balancing
     */
    private TimeZoneNode insert(TimeZoneNode node, Station station) {
        if (node == null) {
            TimeZoneNode newNode = new TimeZoneNode(station.getStationTimeZoneGroup());
            newNode.addStation(station);
            return newNode;
        }

        int comp = station.getStationTimeZoneGroup().compareTo(node.getTimeZoneGroup());

        if (comp == 0) {
            node.addStation(station);
            return node;
        } else if (comp < 0) {
            node.setLeft(insert(node.getLeft(), station));
        } else {
            node.setRight(insert(node.getRight(), station));
        }

        node.setHeight(1 + Math.max(getHeight(node.getLeft()), getHeight(node.getRight())));

        return balance(node);
    }

    /**
     * Finds all stations in a specific time zone group.
     *
     * <p>
     * Returns stations from all countries within the specified time zone group,
     * sorted alphabetically by station name.
     * </p>
     *
     * @param timeZoneGroup the time zone group to search for (e.g., "CET", "WET/GMT")
     *
     * @return a list of stations in the time zone group, sorted by name,
     *         or an empty list if the time zone group is not found
     */
    public List<Station> findByTimeZoneGroup(String timeZoneGroup) {
        TimeZoneNode node = findNode(root, timeZoneGroup);
        return node != null ? node.getStationsByTimeZoneGroup(null) : new ArrayList<>();
    }

    /**
     * Finds all stations in a specific time zone group and country.
     *
     * <p>
     * This method directly supports the user story requirement for fast lookups
     * by time zone group and country. Results are sorted alphabetically by
     * station name in ascending order.
     * </p>
     *
     * @param timeZoneGroup the time zone group to search for (e.g., "CET")
     * @param country the country code to filter by (e.g., "FR", "PT")
     *
     * @return a list of stations matching both criteria, sorted by name,
     *         or an empty list if no matches found
     */
    public List<Station> findByTimeZoneGroupAndCountry(String timeZoneGroup, String country) {
        TimeZoneNode node = findNode(root, timeZoneGroup);
        return node != null ? node.getStationsByTimeZoneGroup(country) : new ArrayList<>();
    }

    /**
     * Recursively searches for a node with the specified time zone group.
     *
     * @param node the root of the subtree to search
     * @param timeZoneGroup the time zone group to find
     *
     * @return the node with the matching time zone group, or null if not found
     */
    private TimeZoneNode findNode(TimeZoneNode node, String timeZoneGroup) {
        if (node == null) {
            return null;
        }

        int comp = timeZoneGroup.compareTo(node.getTimeZoneGroup());

        if (comp == 0) {
            return node;
        } else if (comp < 0) {
            return findNode(node.getLeft(), timeZoneGroup);
        } else {
            return findNode(node.getRight(), timeZoneGroup);
        }
    }

    /**
     * Finds all stations within a range of time zone groups (windowed query).
     *
     * <p>
     * Returns stations from time zone groups that fall lexicographically within
     * the range [minTzGroup, maxTzGroup] inclusive.
     * For example, querying ["CET", "EET"] would include stations from "CET", "CEST", "EET", etc.
     * </p>
     *
     * @param minTimeZoneGroup the start of the range (inclusive)
     * @param maxTimeZoneGroup the end of the range (inclusive)
     *
     * @return a list of stations within the range, sorted by time zone group then by station name
     */
    public List<Station> findInTimeZoneRange(String minTimeZoneGroup, String maxTimeZoneGroup) {
        List<Station> results = new ArrayList<>();
        findInRange(root, minTimeZoneGroup, maxTimeZoneGroup, results);
        return results;
    }

    /**
     * Recursively collects stations within the time zone range using in-order traversal.
     *
     * @param node the current node
     * @param minTimeZoneGroup the minimum time zone group
     * @param maxTimeZoneGroup the maximum time zone group
     * @param results the list to accumulate results
     */
    private void findInRange(TimeZoneNode node, String minTimeZoneGroup, String maxTimeZoneGroup, List<Station> results) {
        if (node == null) {
            return;
        }

        if (node.getTimeZoneGroup().compareTo(minTimeZoneGroup) > 0) {
            findInRange(node.getLeft(), minTimeZoneGroup, maxTimeZoneGroup, results);
        }
        if (node.getTimeZoneGroup().compareTo(minTimeZoneGroup) >= 0 && node.getTimeZoneGroup().compareTo(maxTimeZoneGroup) <= 0) {
            results.addAll(node.getStationsByTimeZoneGroup(null));
        }
        if (node.getTimeZoneGroup().compareTo(minTimeZoneGroup) < 0) {
            findInRange(node.getRight(), minTimeZoneGroup, maxTimeZoneGroup, results);
        }
    }

    /**
     * Returns the height of a node, or 0 if the node is null.
     *
     * @param node the node to measure
     * @return the height of the node
     */
    private int getHeight(TimeZoneNode node) {
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
    private int getBalance(TimeZoneNode node) {
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
    private TimeZoneNode balance(TimeZoneNode node) {
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
     * Performs a right rotation around node y.
     *
     * <p>
     * The subtree T2 must be preserved and reconnected as the left child of the node.
     * </p>
     *
     * @param node the node to rotate
     *
     * @return the new root of the subtree (leftNode)
     */
    private TimeZoneNode rotateRight(TimeZoneNode node) {
        TimeZoneNode leftNode = node.getLeft();
        TimeZoneNode T2 = leftNode.getRight();

        leftNode.setRight(node);
        node.setLeft(T2);

        node.setHeight(Math.max(getHeight(node.getLeft()), getHeight(node.getRight())) + 1);
        leftNode.setHeight(Math.max(getHeight(leftNode.getLeft()), getHeight(leftNode.getRight())) + 1);

        return leftNode;
    }

    /**
     * Performs a right rotation around the node.
     *
     * <p>
     * The subtree T2 must be preserved and reconnected as the right child of the node.
     * </p>
     *
     * @param node the node to rotate
     *
     * @return the new root of the subtree (rightNode)
     */
    private TimeZoneNode rotateLeft(TimeZoneNode node) {
        TimeZoneNode rightNode = node.getRight();
        TimeZoneNode T2 = rightNode.getLeft();

        rightNode.setLeft(node);
        node.setRight(T2);

        node.setHeight(Math.max(getHeight(node.getLeft()), getHeight(node.getRight())) + 1);
        rightNode.setHeight(Math.max(getHeight(rightNode.getLeft()), getHeight(rightNode.getRight())) + 1);

        return rightNode;
    }

    /**
     * Returns the number of unique time zone groups in the tree.
     *
     * @return the count of nodes (unique time zone groups)
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
    private int size(TimeZoneNode node) {
        if (node == null) return 0;
        return 1 + size(node.getLeft()) + size(node.getRight());
    }

    /**
     * Returns the total number of stations stored in the tree.
     *
     * @return the total count of all stations across all time zone groups
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
    private int totalStations(TimeZoneNode node) {
        if (node == null) return 0;
        int count = 0;
        for (List<Station> stations : node.getStationListByCountry().values()) {
            count += stations.size();
        }
        return count + totalStations(node.getLeft()) + totalStations(node.getRight());
    }

    /**
     * Returns all stations in the tree in ascending order.
     *
     * <p>
     * Stations are ordered first by time zone group (lexicographically),
     * then by station name within each time zone group.
     * Uses in-order traversal to maintain sorted order.
     * </p>
     *
     * @return a list of all stations sorted by time zone group and name
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
     * @param result the list to accumulate results
     */
    private void inOrder(TimeZoneNode node, List<Station> result) {
        if (node != null) {
            inOrder(node.getLeft(), result);
            result.addAll(node.getStationsByTimeZoneGroup(null));
            inOrder(node.getRight(), result);
        }
    }

    /**
     * Returns a list of all time zone groups stored in the tree.
     *
     * <p>
     * The list is in ascending lexicographic order.
     * </p>
     *
     * @return a sorted list of time zone group identifiers
     */
    public List<String> getAllTimeZoneGroups() {
        List<String> timeZoneGroups = new ArrayList<>();
        getAllTimeZoneGroups(root, timeZoneGroups);
        return timeZoneGroups;
    }

    /**
     * Recursively collects all time zone groups using in-order traversal.
     *
     * @param node the current node
     * @param timeZoneGroups the list to accumulate time zone groups
     */
    private void getAllTimeZoneGroups(TimeZoneNode node, List<String> timeZoneGroups) {
        if (node != null) {
            getAllTimeZoneGroups(node.getLeft(), timeZoneGroups);
            timeZoneGroups.add(node.getTimeZoneGroup());
            getAllTimeZoneGroups(node.getRight(), timeZoneGroups);
        }
    }

    /**
     * Returns all countries that have stations in a specific time zone group.
     *
     * <p>
     * Useful for discovering which countries are represented within a time zone.
     * </p>
     *
     * @param timeZoneGroup the time zone group to query
     *
     * @return a set of country codes, or an empty set if the time zone group is not found
     */
    public Set<String> getCountriesInTimeZoneGroup(String timeZoneGroup) {
        TimeZoneNode node = findNode(root, timeZoneGroup);
        return node != null ? node.getCountries() : new HashSet<>();
    }
}
