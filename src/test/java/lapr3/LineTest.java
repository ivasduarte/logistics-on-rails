package lapr3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spatialQuerying.railwayMngt.Line;
import spatialQuerying.railwayMngt.Segment;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LineTest {

    private Line line;
    private static final int LINE_ID = 1;
    private static final String LINE_NAME = "Test Line";
    private static final String OWNER_NAME = "Test Owner";
    private static final int START_STATION = 100;
    private static final int END_STATION = 200;
    private static final int GAUGE = 1435;

    @BeforeEach
    void setUp() {
        line = new Line(LINE_ID, LINE_NAME, OWNER_NAME, START_STATION, END_STATION, GAUGE);
    }

    @Test
    void testLineConstructor() {
        assertNotNull(line);
        assertEquals(LINE_ID, line.getLineID());
        assertEquals(LINE_NAME, line.getLineName());
        assertEquals(OWNER_NAME, line.getOwnerName());
        assertEquals(START_STATION, line.getStartStationID());
        assertEquals(END_STATION, line.getEndStationID());
        assertEquals(GAUGE, line.getGauge());
    }

    @Test
    void testSetAndGetSegments() {
        List<Segment> segments = new ArrayList<>();
        segments.add(new Segment(1, LINE_ID, 1, true, 1000, 500, 2));
        segments.add(new Segment(2, LINE_ID, 2, false, 800, 300, 1));

        line.setSegments(segments);

        assertEquals(segments, line.getSegments());
        assertEquals(2, line.getSegments().size());
    }

    @Test
    void testInitialSegmentsNull() {
        assertNull(line.getSegments());
    }
}
