package lapr3;

import lapr3.uslp03.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TravelTimeTest {

    private Locomotive locomotive;
    private Line line;
    private TravelTime travelTime;

    @BeforeEach
    void setUp() {
        locomotive = new Locomotive(1, "Loc1", "Make1", "Model1", 2020, 2, "Bogie1",
                1000, 20.5, 3.0, 4.0, 80, 120, 100, 50, "Electric",
                25000, 50, "Op1", 1435, 0);

        line = new Line(1, "Line1", "Owner1", 1, 2, 1435);
        List<Segment> segments = new ArrayList<>();
        segments.add(new Segment(1, 1, 1, true, 100, 1000, 2));
        segments.add(new Segment(2, 1, 2, true, 100, 2000, 2));
        line.setSegments(segments);

        travelTime = new TravelTime(locomotive, line);
    }

    @Test
    void testGetters() {
        assertEquals(locomotive, travelTime.getLocomotive());
        assertEquals(line, travelTime.getLine());
    }

    @Test
    void testCalculateTravelDistance() {
        int expectedDistance = 3000; // 1000 + 2000
        assertEquals(expectedDistance, travelTime.calculateTravelDistance());
    }

    @Test
    void testCalculateTravelTime() {
        double expectedTime = 25.0; // 3000/120
        assertEquals(expectedTime, travelTime.calculateTravelTime(3000));
    }
}
