// ==================== StationTest.java ====================
package lapr3;

import org.junit.jupiter.api.Test;
import freightMngt.domain.Station;

import static org.junit.jupiter.api.Assertions.*;

public class StationTest {

    @Test
    void testStationConstructor() {
        Station station = new Station(
                "Test Station",
                "Portugal",
                "Europe/Lisbon",
                "WET",
                40.7128,
                -74.0060,
                true,
                true,
                false
        );
        assertNotNull(station);
    }

    @Test
    void testGetStationName() {
        Station station = new Station(
                "Test Station",
                "Portugal",
                "Europe/Lisbon",
                "WET",
                40.7128,
                -74.0060,
                true,
                true,
                false
        );
        assertEquals("Test Station", station.getStationName());
    }

    @Test
    void testGetStationCountry() {
        Station station = new Station(
                "Test Station",
                "Portugal",
                "Europe/Lisbon",
                "WET",
                40.7128,
                -74.0060,
                true,
                true,
                false
        );
        assertEquals("Portugal", station.getStationCountry());
    }

    @Test
    void testGetStationTimeZone() {
        Station station = new Station(
                "Test Station",
                "Portugal",
                "Europe/Lisbon",
                "WET",
                40.7128,
                -74.0060,
                true,
                true,
                false
        );
        assertEquals("Europe/Lisbon", station.getStationTimeZone());
    }

    @Test
    void testGetStationTimeZoneGroup() {
        Station station = new Station(
                "Test Station",
                "Portugal",
                "Europe/Lisbon",
                "WET",
                40.7128,
                -74.0060,
                true,
                true,
                false
        );
        assertEquals("WET", station.getStationTimeZoneGroup());
    }

    @Test
    void testGetStationLatitude() {
        Station station = new Station(
                "Test Station",
                "Portugal",
                "Europe/Lisbon",
                "WET",
                40.7128,
                -74.0060,
                true,
                true,
                false
        );
        assertEquals(40.7128, station.getStationLatitude());
    }

    @Test
    void testGetStationLongitude() {
        Station station = new Station(
                "Test Station",
                "Portugal",
                "Europe/Lisbon",
                "WET",
                40.7128,
                -74.0060,
                true,
                true,
                false
        );
        assertEquals(-74.0060, station.getStationLongitude());
    }

    @Test
    void testIsCity() {
        Station station = new Station(
                "Test Station",
                "Portugal",
                "Europe/Lisbon",
                "WET",
                40.7128,
                -74.0060,
                true,
                false,
                false
        );
        assertTrue(station.isCity());
    }

    @Test
    void testIsMainStation() {
        Station station = new Station(
                "Test Station",
                "Portugal",
                "Europe/Lisbon",
                "WET",
                40.7128,
                -74.0060,
                false,
                true,
                false
        );
        assertTrue(station.isMainStation());
    }

    @Test
    void testIsAirport() {
        Station station = new Station(
                "Test Station",
                "Portugal",
                "Europe/Lisbon",
                "WET",
                40.7128,
                -74.0060,
                false,
                false,
                true
        );
        assertTrue(station.isAirport());
    }

    @Test
    void testToString() {
        Station station = new Station(
                "Test Station",
                "Portugal",
                "Europe/Lisbon",
                "WET",
                40.7128,
                -74.0060,
                true,
                true,
                false
        );

        String expected = "Station Test Station\n" +
                " Country: Portugal\n" +
                " Time Zone: Europe/Lisbon\n" +
                " Time Zone Group: WET\n" +
                " Latitude: 40.7128\n" +
                " Longitude: -74.006";

        assertEquals(expected, station.toString());
    }
}