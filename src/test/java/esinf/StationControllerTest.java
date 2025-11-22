package esinf;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spatialQuerying.controller.StationController;
import spatialQuerying.railwayMngt.Station;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StationControllerTest {
    private StationController controller;
    private List<Station> stationList;
    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        stationList = new ArrayList<>();
        controller = new StationController(stationList);

        tempFile = Files.createTempFile("test_stations", ".csv");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testLoadValidStations() throws IOException {
        String csvContent =
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport\n" +
                        "PT,\"('Europe/Lisbon',)\",WET/GMT,Porto-Campanha,41.1496,-8.5851,True,True,False\n" +
                        "FR,\"('Europe/Paris',)\",CET,Paris-Gare-de-Lyon,48.8447,2.3737,True,True,False\n";

        Files.writeString(tempFile, csvContent);

        List<Station> stations = controller.collectStationsData(tempFile.toString());

        assertEquals(2, stations.size());
        assertEquals("Porto-Campanha", stations.get(0).getStationName());
        assertEquals("Paris-Gare-de-Lyon", stations.get(1).getStationName());
    }

    @Test
    void testRejectInvalidLatitude() throws IOException {
        String csvContent =
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport\n" +
                        "PT,\"('Europe/Lisbon',)\",WET/GMT,Invalid-Station,91.0,-8.5851,True,True,False\n" +
                        "PT,\"('Europe/Lisbon',)\",WET/GMT,Valid-Station,41.1496,-8.5851,True,True,False\n";

        Files.writeString(tempFile, csvContent);

        List<Station> stations = controller.collectStationsData(tempFile.toString());

        assertEquals(1, stations.size());
        assertEquals("Valid-Station", stations.get(0).getStationName());
    }

    @Test
    void testRejectInvalidLongitude() throws IOException {
        String csvContent =
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport\n" +
                        "PT,\"('Europe/Lisbon',)\",WET/GMT,Invalid-Station,41.1496,-181.0,True,True,False\n" +
                        "PT,\"('Europe/Lisbon',)\",WET/GMT,Valid-Station,41.1496,-8.5851,True,True,False\n";

        Files.writeString(tempFile, csvContent);

        List<Station> stations = controller.collectStationsData(tempFile.toString());

        assertEquals(1, stations.size());
        assertEquals("Valid-Station", stations.get(0).getStationName());
    }

    @Test
    void testRejectEmptyFields() throws IOException {
        String csvContent =
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport\n" +
                        "PT,\"('Europe/Lisbon',)\",WET/GMT,,41.1496,-8.5851,True,True,False\n" + // Empty station name
                        ",\"('Europe/Lisbon',)\",WET/GMT,No-Country,41.1496,-8.5851,True,True,False\n" + // Empty country
                        "PT,\"('Europe/Lisbon',)\",,No-TZ-Group,41.1496,-8.5851,True,True,False\n" + // Empty time zone group
                        "PT,\"('Europe/Lisbon',)\",WET/GMT,Valid-Station,41.1496,-8.5851,True,True,False\n";

        Files.writeString(tempFile, csvContent);

        List<Station> stations = controller.collectStationsData(tempFile.toString());

        assertEquals(1, stations.size());
        assertEquals("Valid-Station", stations.get(0).getStationName());
    }

    @Test
    void testHandleQuotedCommas() throws IOException {
        String csvContent =
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport\n" +
                        "FR,\"('Europe/Paris',)\",CET,Paris-Nord,48.8809,2.3553,True,True,False\n";

        Files.writeString(tempFile, csvContent);

        List<Station> stations = controller.collectStationsData(tempFile.toString());

        assertEquals(1, stations.size());
        assertEquals("('Europe/Paris',)", stations.get(0).getStationTimeZone());
        assertEquals("CET", stations.get(0).getStationTimeZoneGroup());
    }

    @Test
    void testSortSameCoordinatesByName() throws IOException {
        String csvContent =
                "country,time_zone,time_zone_group,station,latitude,longitude,is_city,is_main_station,is_airport\n" +
                        "PT,\"('Europe/Lisbon',)\",WET/GMT,Lisboa-Oriente,38.7682,-9.0982,True,True,False\n" +
                        "PT,\"('Europe/Lisbon',)\",WET/GMT,Lisboa-Santa-Apolonia,38.7682,-9.0982,True,True,False\n";

        Files.writeString(tempFile, csvContent);

        List<Station> stations = controller.collectStationsData(tempFile.toString());

        assertEquals(2, stations.size());
        assertEquals("Lisboa-Oriente", stations.get(0).getStationName());
        assertEquals("Lisboa-Santa-Apolonia", stations.get(1).getStationName());
    }
}
