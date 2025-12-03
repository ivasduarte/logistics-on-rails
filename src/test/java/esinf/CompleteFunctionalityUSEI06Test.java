package esinf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import spatialQuerying.controller.StationController;
import freightMngt.domain.Station;
import spatialQuerying.structures.bst.LatitudeTree;
import spatialQuerying.structures.bst.LongitudeTree;
import spatialQuerying.structures.bst.TimeZoneTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CompleteFunctionalityUSEI06Test {

    @Test
    void testCompleteFunctionality() throws IOException {
        // This is a temporary csv with sample stations
        String sampleCsv = String.join("\n",
                "station,country,time_zone,time_zone_group,latitude,longitude,is_city,is_main_station,is_airport",
                "Lisbon,Portugal,WET,Europe/WET,38.72,-9.13,true,true,false",
                "Porto,Portugal,WET,Europe/WET,41.15,-8.61,true,false,false",
                "Madrid,Spain,CET,Europe/CET,40.42,-3.70,true,true,false",
                "Barcelona,Spain,CET,Europe/CET,41.38,2.17,true,false,false",
                "New York,USA,EST,America/EST,40.71,-74.01,true,true,false",
                "Chicago,USA,CST,America/CST,41.88,-87.63,true,false,false",
                "Reykjavik,Iceland,GMT,Europe/WET,64.13,-21.82,true,false,false",
                "Dublin,Ireland,GMT,Europe/WET,53.35,-6.26,true,false,false"
        );

        Path temp = Files.createTempFile("stations_test", ".csv");
        Files.writeString(temp, sampleCsv);
        temp.toFile().deleteOnExit();

        List<Station> initial = new ArrayList<>();
        StationController controller = new StationController(initial);
        List<Station> stations = controller.collectStationsData(temp.toString());

        Assertions.assertEquals(8, stations.size(), "Expected 8 stations loaded from csv.");

        // Tree building phase
        LatitudeTree latTree = new LatitudeTree();
        LongitudeTree lonTree = new LongitudeTree();
        TimeZoneTree tzTree = new TimeZoneTree();

        for (Station s : stations) {
            latTree.insert(s);
            lonTree.insert(s);
            tzTree.insert(s);
        }

        // Tree Totals
        Assertions.assertEquals(8, latTree.totalStations(), "Latitude tree total");
        Assertions.assertEquals(8, lonTree.totalStations(), "Longitude tree total");
        Assertions.assertEquals(8, tzTree.inOrderTraversal().size(), "TimeZone tree total via inOrderTraversal");

        // Test finding exact Latitude and Longitude values
        List<Station> latFind = latTree.findByLatitude(38.72);
        List<String> latFindNames = latFind.stream().map(Station::getStationName).collect(Collectors.toList());
        Assertions.assertTrue(latFindNames.contains("Lisbon"), "findByLatitude should return Lisbon");

        List<Station> lonFind = lonTree.findByLongitude(-9.13);
        List<String> lonFindNames = lonFind.stream().map(Station::getStationName).collect(Collectors.toList());
        Assertions.assertTrue(lonFindNames.contains("Lisbon"), "findByLongitude should return Lisbon");

        // Test finding Latitude in specific range
        List<Station> latRange = latTree.findInLatitudeRange(38.0, 42.0);
        Set<String> latRangeNames = latRange.stream().map(Station::getStationName).collect(Collectors.toSet());
        Set<String> expectedLatRange = new HashSet<>(Arrays.asList("Lisbon", "Porto", "Madrid", "Barcelona", "New York", "Chicago"));
        Assertions.assertEquals(expectedLatRange, latRangeNames, "Latitude range 38.0-42.0");

        // Test finding Longitude in specific range
        List<Station> lonRange = lonTree.findInLongitudeRange(-90.0, -70.0);
        Set<String> lonRangeNames = lonRange.stream().map(Station::getStationName).collect(Collectors.toSet());
        Set<String> expectedLonRange = new HashSet<>(Arrays.asList("New York", "Chicago"));
        Assertions.assertEquals(expectedLonRange, lonRangeNames, "Longitude range -90 to -70");

        // Test TimeZone group findings
        List<Station> wetStations = tzTree.findByTimeZoneGroup("Europe/WET");
        Set<String> wetNames = wetStations.stream().map(Station::getStationName).collect(Collectors.toSet());
        Set<String> expectedWET = new HashSet<>(Arrays.asList("Lisbon", "Porto", "Reykjavik", "Dublin"));
        Assertions.assertEquals(expectedWET, wetNames, "Europe/WET group stations");

        List<Station> portugalInWET = tzTree.findByTimeZoneGroupAndCountry("Europe/WET", "Portugal");
        Set<String> portugalNames = portugalInWET.stream().map(Station::getStationName).collect(Collectors.toSet());
        Set<String> expectedPortugal = new HashSet<>(Arrays.asList("Lisbon", "Porto"));
        Assertions.assertEquals(expectedPortugal, portugalNames, "Portugal stations in Europe/WET");

        // Test set time zone groups
        List<String> tzGroups = tzTree.getAllTimeZoneGroups();
        Set<String> tzGroupSet = new HashSet<>(tzGroups);
        Set<String> expectedGroups = new HashSet<>(Arrays.asList("Europe/WET", "Europe/CET", "America/EST", "America/CST"));
        Assertions.assertTrue(tzGroupSet.containsAll(expectedGroups), "Time zone groups contain expected entries");

        // Test finding countries in a time zone group
        Set<String> countriesInWET = tzTree.getCountriesInTimeZoneGroup("Europe/WET");
        Set<String> expectedCountries = new HashSet<>(Arrays.asList("Portugal", "Iceland", "Ireland"));
        Assertions.assertEquals(expectedCountries, countriesInWET, "Countries in Europe/WET");

        // Test in-order traversal sizes
        Assertions.assertEquals(8, latTree.inOrderTraversal().size(), "Latitude in-order traversal size");
        Assertions.assertEquals(8, lonTree.inOrderTraversal().size(), "Longitude in-order traversal size");
        Assertions.assertEquals(8, tzTree.inOrderTraversal().size(), "TimeZone in-order traversal size");
    }
}
