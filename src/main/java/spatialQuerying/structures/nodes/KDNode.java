/**
 * KDNode.java
 */

package spatialQuerying.structures.nodes;

import freightMngt.domain.Station;
import spatialQuerying.structures.kd.Point2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KDNode {
    private Point2D coordinates;            // (lat, long)
    private List<Station> stationList;      // every station with these coordinates
    private KDNode left;
    private KDNode right;

    public KDNode(Point2D coordinates) {
        this.coordinates = coordinates;
        this.stationList = new ArrayList<>();
    }

    public KDNode(Point2D coordinates, List<Station> stationList) {
        this.coordinates = coordinates;
        this.stationList = new ArrayList<>(stationList);
    }

    public void addStation(Station station) {
        stationList.add(station);
    }

    public Point2D getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Point2D coordinates) {
        this.coordinates = coordinates;
    }

    public List<Station> getStationList() {
        return stationList;
    }

    public void setStationList(List<Station> stationList) {
        this.stationList = stationList;
    }

    public KDNode getLeft() {
        return left;
    }

    public void setLeft(KDNode left) {
        this.left = left;
    }

    public KDNode getRight() {
        return right;
    }

    public void setRight(KDNode right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return String.format("KDNode\n {coordinates: %s, stations: %d}", coordinates, stationList.size());
    }
}
