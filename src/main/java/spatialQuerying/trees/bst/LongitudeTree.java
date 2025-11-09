package spatialQuerying.trees.bst;

import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.nodes.LongitudeNode;

import java.util.ArrayList;
import java.util.List;

public class LongitudeTree {
    private LongitudeNode root;
    private static final double EPSILON = 1e-6;

    public void insert(Station station) {
        root = insert(root, station);
    }

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

    // Finding Methods
    public List<Station> findByLongitude(double lon) {
        LongitudeNode node = findNode(root, lon);
        return node != null ? new ArrayList<>(node.getStationList()) : new ArrayList<>();
    }

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

    public List<Station> findInLongitudeRange(double minLon, double maxLon) {
        List<Station> results = new ArrayList<>();
        findInRange(root, minLon, maxLon, results);
        return results;
    }

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

    // Util Methods
    private int getHeight(LongitudeNode node) {
        return node == null ? 0 : node.getHeight();
    }

    private int getBalance(LongitudeNode node) {
        return node == null ? 0 : getHeight(node.getLeft()) - getHeight(node.getRight());
    }

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

    private LongitudeNode rotateLeft(LongitudeNode node) {
        LongitudeNode rightNode = node.getRight();
        LongitudeNode T2 = rightNode.getLeft();

        rightNode.setLeft(node);
        node.setRight(T2);

        node.setHeight(Math.max(getHeight(node.getLeft()), getHeight(node.getRight())) + 1);
        rightNode.setHeight(Math.max(getHeight(rightNode.getLeft()), getHeight(rightNode.getRight())) + 1);

        return rightNode;
    }

    private LongitudeNode rotateRight(LongitudeNode node) {
        LongitudeNode leftNode = node.getLeft();
        LongitudeNode T2 = leftNode.getRight();

        leftNode.setRight(node);
        node.setLeft(T2);

        node.setHeight(Math.max(getHeight(node.getLeft()), getHeight(node.getRight())) + 1);
        leftNode.setHeight(Math.max(getHeight(leftNode.getLeft()), getHeight(leftNode.getRight())) + 1);

        return leftNode;
    }

    private int size() {
        return size(root);
    }

    private int size(LongitudeNode node) {
        if (node == null) return 0;
        return 1 + size(node.getLeft()) + size(node.getRight());
    }

    public int totalStations() {
        return totalStations(root);
    }

    public int totalStations(LongitudeNode node) {
        if (node == null) return 0;
        return node.getStationList().size() + totalStations(node.getLeft()) + totalStations(node.getRight());
    }

    public List<Station> inOrderTraversal() {
        List<Station> results = new ArrayList<>();
        inOrder(root, results);
        return results;
    }

    private void inOrder(LongitudeNode node, List<Station> results) {
        if (node != null) {
            inOrder(node.getLeft(), results);
            results.addAll(node.getStationList());
            inOrder(node.getRight(), results);
        }
    }
}
