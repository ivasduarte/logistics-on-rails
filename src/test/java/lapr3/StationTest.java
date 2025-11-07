package lapr3;

import org.junit.jupiter.api.Test;
import spatialQuerying.railwayMngt.Station;

import static org.junit.jupiter.api.Assertions.*;

public class StationTest {

    @Test
    void testStationConstructor() {
        Station station = new Station(1, "Test Station");
        assertNotNull(station);
    }

    @Test
    void testGetStationID() {
        Station station = new Station(1, "Test Station");
        assertEquals(1, station.getStationID());
    }

    @Test
    void testGetStationName() {
        Station station = new Station(1, "Test Station");
        assertEquals("Test Station", station.getStationName());
    }
}
