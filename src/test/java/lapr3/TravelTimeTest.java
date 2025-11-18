package lapr3;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spatialQuerying.railwayMngt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TravelTimeTest {

    private Locomotive locomotive;
    private Station startStation;
    private Station endStation;
    private List<Line> lines;
    private TravelTime travelTime;

    @BeforeEach
    void setUp() {
        locomotive = new Locomotive(1, "Loc1", "Make1", "Model1", 2020, 2, "Bogie1",
                1000, 20.5, 3.0, 4.0, 80, 120, 100, 50, "Electric",
                25000, 50, "Op1", 1435, 0);

        startStation = new Station(
                "Station1",
                "Portugal",
                "Europe/Lisbon",
                "WET",
                40.0,
                -74.0,
                false,
                true,
                false
        );

        endStation = new Station(
                "Station2",
                "Portugal",
                "Europe/Lisbon",
                "WET",
                41.0,
                -73.0,
                false,
                true,
                false
        );

        Line line = new Line(1, "Line1", "Owner1", 1, 2, 1435);
        List<Segment> segments = new ArrayList<>();
        segments.add(new Segment(1, 1, 1, true, 100, 1000, 2));
        segments.add(new Segment(2, 1, 2, true, 100, 2000, 2));
        line.setSegments(segments);

        lines = new ArrayList<>();
        lines.add(line);

        travelTime = new TravelTime(locomotive, startStation, endStation);
    }

    @Test
    void testGetLocomotive() {
        Locomotive expected = locomotive;
        Locomotive result = travelTime.getLocomotive();

        assertEquals(expected, result);
    }

    @Test
    void testGetStations() {
        List<Station> expected = List.of(startStation, endStation);
        List<Station> result = travelTime.getStations();

        assertEquals(expected, result);
    }

    @Test
    void testCalculateTravelDistance() {
        int expected = 3000; // 1000 + 2000
        int result = travelTime.calculateTravelDistance(lines);

        assertEquals(expected, result);
    }

    @Test
    void testCalculateTravelTime() {
        double expected = 25.0; // 3000/120
        double result = travelTime.calculateTravelTime(3000);

        assertEquals(expected, result);
    }

    @Test
    void testCompleteGettersScenario() {
        // Expected
        Object[] expected = new Object[] {
                locomotive,
                List.of(startStation, endStation)
        };

        // Result
        Object[] result = new Object[] {
                travelTime.getLocomotive(),
                travelTime.getStations()
        };

        assertArrayEquals(expected, result);
    }

    @Test
    void testCompleteCalculationScenario() {
        // Expected results
        Object[] expected = new Object[] {
                3000,        // distance
                25.0         // time
        };

        // Actual results
        int distance = travelTime.calculateTravelDistance(lines);
        double time = travelTime.calculateTravelTime(distance);
        Object[] result = new Object[] {
                distance,
                time
        };

        assertArrayEquals(expected, result);
    }
}