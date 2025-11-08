package spatialQuerying.railwayMngt.trees.nodes;

import spatialQuerying.railwayMngt.Station;
import spatialQuerying.railwayMngt.trees.bst.LongitudeTree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LongitudeNode {
    private double lon;
    private List<Station> stationList;
    private LongitudeNode left;
    private LongitudeNode right;
    private int height;

    public LongitudeNode(double lon) {
        this.lon = lon;
        this.stationList = new ArrayList<>();
        this.height = 1;
    }

    public void addStation(Station station) {
        stationList.add(station);
        stationList.sort(Comparator.comparing(Station::getStationName));
    }

    public double getLongitude() {
        return lon;
    }
    public List<Station> getStationList() {
        return stationList;
    }
    public LongitudeNode getLeft() {
        return left;
    }
    public LongitudeNode getRight() {
        return right;
    }
    public int getHeight() {
        return height;
    }

    public void setLeft(LongitudeNode left) {
        this.left = left;
    }
    public void setRight(LongitudeNode right) {
        this.right = right;
    }
    public void setHeight(int height) {
        this.height = height;
    }
}
