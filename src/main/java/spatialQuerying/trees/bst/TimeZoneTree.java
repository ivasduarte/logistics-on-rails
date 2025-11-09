package spatialQuerying.trees.bst;

import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.nodes.TimeZoneNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TimeZoneTree {
    private TimeZoneNode root;

    public void insert(Station station) {
        root = insert(root, station);
    }

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

    // Finding Methods
    public List<Station> findByTimeZoneGroup(String timeZoneGroup) {
        TimeZoneNode node = findNode(root, timeZoneGroup);
        return node != null ? node.getStationsByTimeZoneGroup(null) : new ArrayList<>();
    }

    public List<Station> findByTimeZoneGroupAndCountry(String timeZoneGroup, String country) {
        TimeZoneNode node = findNode(root, timeZoneGroup);
        return node != null ? node.getStationsByTimeZoneGroup(country) : new ArrayList<>();
    }

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

    public List<Station> findInTimeZoneRange(String minTimeZoneGroup, String maxTimeZoneGroup) {
        List<Station> results = new ArrayList<>();
        findInRange(root, minTimeZoneGroup, maxTimeZoneGroup, results);
        return results;
    }

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

    // Util Methods
    private int getHeight(TimeZoneNode node) {
        return node == null ? 0 : node.getHeight();
    }

    private int getBalance(TimeZoneNode node) {
        return node == null ? 0 : getHeight(node.getLeft()) - getHeight(node.getRight());
    }

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

    private TimeZoneNode rotateRight(TimeZoneNode node) {
        TimeZoneNode leftNode = node.getLeft();
        TimeZoneNode T2 = leftNode.getRight();

        leftNode.setRight(node);
        node.setLeft(T2);

        node.setHeight(Math.max(getHeight(node.getLeft()), getHeight(node.getRight())) + 1);
        leftNode.setHeight(Math.max(getHeight(leftNode.getLeft()), getHeight(leftNode.getRight())) + 1);

        return leftNode;
    }

    private TimeZoneNode rotateLeft(TimeZoneNode node) {
        TimeZoneNode rightNode = node.getRight();
        TimeZoneNode T2 = rightNode.getLeft();

        rightNode.setLeft(node);
        node.setRight(T2);

        node.setHeight(Math.max(getHeight(node.getLeft()), getHeight(node.getRight())) + 1);
        rightNode.setHeight(Math.max(getHeight(rightNode.getLeft()), getHeight(rightNode.getRight())) + 1);

        return rightNode;
    }

    private int size() {
        return size(root);
    }

    private int size(TimeZoneNode node) {
        if (node == null) return 0;
        return 1 + size(node.getLeft()) + size(node.getRight());
    }

    private int totalStations() {
        return totalStations(root);
    }

    private int totalStations(TimeZoneNode node) {
        if (node == null) return 0;
        int count = 0;
        for (List<Station> stations : node.getStationListByCountry().values()) {
            count += stations.size();
        }
        return count + totalStations(node.getLeft()) + totalStations(node.getRight());
    }

    public List<Station> inOrderTraversal() {
        List<Station> results = new ArrayList<>();
        inOrder(root, results);
        return results;
    }

    private void inOrder(TimeZoneNode node, List<Station> result) {
        if (node != null) {
            inOrder(node.getLeft(), result);
            result.addAll(node.getStationsByTimeZoneGroup(null));
            inOrder(node.getRight(), result);
        }
    }

    public List<String> getAllTimeZoneGroups() {
        List<String> timeZoneGroups = new ArrayList<>();
        getAllTimeZoneGroups(root, timeZoneGroups);
        return timeZoneGroups;
    }

    private void getAllTimeZoneGroups(TimeZoneNode node, List<String> timeZoneGroups) {
        if (node != null) {
            getAllTimeZoneGroups(node.getLeft(), timeZoneGroups);
            timeZoneGroups.add(node.getTimeZoneGroup());
            getAllTimeZoneGroups(node.getRight(), timeZoneGroups);
        }
    }

    public Set<String> getCountriesInTimeZoneGroup(String timeZoneGroup) {
        TimeZoneNode node = findNode(root, timeZoneGroup);
        return node != null ? node.getCountries() : new HashSet<>();
    }
}
