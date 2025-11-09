package spatialQuerying.trees.nodes;

import spatialQuerying.railwayMngt.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LatitudeNode {
    private double lat;
    private List<Station> stationList;
    private int height;

    private LatitudeNode left;
    private LatitudeNode right;

    public LatitudeNode(double lat) {
        this.lat = lat;
        this.stationList = new ArrayList<>();
        this.height = 1;
    }

    public void addStation(Station station) {
        stationList.add(station);
        stationList.sort(Comparator.comparing(Station::getStationName));
    }

    public double getLat() {
        return lat;
    }
    public List<Station> getStationList() {
        return stationList;
    }
    public int getHeight() {
        return height;
    }
    public LatitudeNode getLeft() {
        return left;
    }
    public LatitudeNode getRight() {
        return right;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    public void setLeft(LatitudeNode left) {
        this.left = left;
    }
    public void setRight(LatitudeNode right) {
        this.right = right;
    }
}



