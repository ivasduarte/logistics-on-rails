package spatialQuerying.trees.bst;

import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.nodes.LatitudeNode;

import java.util.ArrayList;
import java.util.List;

public class LatitudeTree {
    private LatitudeNode root;
    private static final double EPSILON = 1e-6;

    public void insert(Station station) {
        root = insert(root, station);
    }

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

    // Finding methods
    public List<Station> findByLatitude(double lat) {
        LatitudeNode node = findNode(root, lat);
        return node != null ? new ArrayList<>(node.getStationList()) : new ArrayList<>();
    }

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

    public List<Station> findInLatitudeRange(double minLat, double maxLat) {
        List<Station> results = new ArrayList<>();
        findInRange(root, minLat, maxLat, results);
        return results;
    }

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

    // Util Methods
    private int getHeight(LatitudeNode node) {
        return node == null ? 0 : node.getHeight();
    }

    private int getBalance(LatitudeNode node) {
        return node == null ? 0 : getHeight(node.getLeft()) - getHeight(node.getRight());
    }

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

    private LatitudeNode rotateLeft(LatitudeNode node) {
        LatitudeNode rightNode = node.getRight();
        LatitudeNode T2 = rightNode.getLeft();

        rightNode.setLeft(node);
        node.setRight(T2);

        node.setHeight(Math.max(getHeight(node.getLeft()), getHeight(node.getRight())) + 1);
        rightNode.setHeight(Math.max(getHeight(rightNode.getLeft()), getHeight(rightNode.getRight())) + 1);

        return rightNode;
    }

    private LatitudeNode rotateRight(LatitudeNode node) {
        LatitudeNode leftNode = node.getLeft();
        LatitudeNode T2 = leftNode.getRight();

        leftNode.setRight(node);
        node.setLeft(T2);

        node.setHeight(Math.max(getHeight(node.getLeft()), getHeight(node.getRight())) + 1);
        leftNode.setHeight(Math.max(getHeight(leftNode.getLeft()), getHeight(leftNode.getRight())) + 1);

        return leftNode;
    }

    private int size() {
        return size(root);
    }

    private int size(LatitudeNode node) {
        if (node == null) return 0;
        return 1 + size(node.getLeft()) + size(node.getRight());
    }

    public int totalStations() {
        return totalStations(root);
    }

    public int totalStations(LatitudeNode node) {
        if (node == null) return 0;
        return node.getStationList().size() + totalStations(node.getLeft()) + totalStations(node.getRight());
    }

    public List<Station> inOrderTraversal() {
        List<Station> results = new ArrayList<>();
        inOrder(root, results);
        return results;
    }

    private void inOrder(LatitudeNode node, List<Station> results) {
        if (node != null) {
            inOrder(node.getLeft(), results);
            results.addAll(node.getStationList());
            inOrder(node.getRight(), results);
        }
    }
}
