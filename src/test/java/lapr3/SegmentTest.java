package lapr3;

import org.junit.jupiter.api.Test;
import spatialQuerying.railwayMngt.Segment;

import static org.junit.jupiter.api.Assertions.*;

public class SegmentTest {

    @Test
    void testSegmentConstructorAndGetters() {
        Segment segment = new Segment(1, 100, 1, true, 1000, 500, 2);

        assertEquals(1, segment.getSegmentID());
        assertEquals(100, segment.getLineID());
        assertEquals(1, segment.getOrder());
        assertTrue(segment.isElectrified());
        assertEquals(1000, segment.getMaxWeight());
        assertEquals(500, segment.getLength());
        assertEquals(2, segment.getNumberTracks());
    }

    @Test
    void testSegmentWithDifferentValues() {
        Segment segment = new Segment(2, 101, 2, false, 2000, 750, 1);

        assertEquals(2, segment.getSegmentID());
        assertEquals(101, segment.getLineID());
        assertEquals(2, segment.getOrder());
        assertFalse(segment.isElectrified());
        assertEquals(2000, segment.getMaxWeight());
        assertEquals(750, segment.getLength());
        assertEquals(1, segment.getNumberTracks());
    }
}
