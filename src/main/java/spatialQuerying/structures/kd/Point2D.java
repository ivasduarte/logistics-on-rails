/**
 * Point2D.java
 */
package spatialQuerying.structures.kd;

import java.util.Objects;

public class Point2D {
    private final double stationLatitude;
    private final double stationLongitude;

    public Point2D(double stationLatitude, double stationLongitude) {
        this.stationLatitude = stationLatitude;
        this.stationLongitude = stationLongitude;
    }

    public double getStationLatitude() {
        return stationLatitude;
    }
    public double getStationLongitude() {
        return stationLongitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point2D)) return false;
        Point2D other = (Point2D) o;
        return Double.compare(stationLatitude, other.stationLatitude) == 0 &&
                Double.compare(stationLongitude, other.stationLongitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stationLatitude, stationLongitude);
    }

    @Override
    public String toString() {
        return String.format("(%.6f, %.6f)", stationLatitude,  stationLongitude);
    }
}
