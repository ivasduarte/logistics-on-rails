package app;

import freightMngt.domain.*;

import java.util.ArrayList;
import java.util.List;

public class LaprMain {

    /**
     * The main method serves as the entry point for the application. It demonstrates
     * the creation of station, locomotive, segment, and line data, followed by
     * calculations for travel distance and estimated travel time between two stations.
     */
    public static void main() {

        // Creation of example stations
        Station startStation = new Station("1", "PT", "WET", "WET/GMT",
                38.71387, -9.122271, true, true, false);
        Station endStation = new Station("2", "PT", "WET", "WET/GMT",
                41.14961, -8.58519, true, true, false);

        // Creation of example locomotive
        Locomotive locomotive = new Locomotive(1, "Express 1", "Siemens", "Vectron",
                2020, 4, "Bo-Bo", 6400, 18.9, 2.9, 4.3, 87, 200,
                160, 300, "Electric", 25000, 50, "Rail Co", 1435, 0);

        // Creation of an example segment connecting stations 1 and 2
        Segment segment = new Segment(1, 1, 2, true, 1000, 1000, 2);

        // Creation of an example line
        Line line = new Line(1, "Main Line", "Rail Co", 1, 2, 1435);

        List<Segment> segments = new ArrayList<>();
        segments.add(segment);
        line.setSegments(segments);

        TravelTime travelTime = new TravelTime(locomotive, startStation, endStation);

        List<Line> lines = new ArrayList<>();
        lines.add(line);

        // Calculation of travel distance
        int distance = travelTime.calculateTravelDistance(lines);

        // Calculation of travel time
        double time = travelTime.calculateTravelTime(distance);

        // Output of travel distance and time
        System.out.printf("Travel distance: %d meters%n", distance);
        System.out.printf("Estimated travel time: %.2f seconds%n", time);
    }
}