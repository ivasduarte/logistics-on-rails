package spatialQuerying.railwayMngt.controller;

import spatialQuerying.railwayMngt.Station;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class StationController {
    private List<Station> stationList;

    public StationController(List<Station> stationList) {
        this.stationList = stationList;
    }

    public List<Station> collectStationsData(String filePath) throws IOException {
        stationList.clear();

        try (BufferedReader scan = new BufferedReader(new FileReader(filePath))) {
            String paramLine = scan.readLine();

            if (paramLine == null) {
                return stationList;
            }

            String[] params = paramLine.split(",");

            int lineNo = 2;
            String line;

            while ((line = scan.readLine()) != null) {
                Map<String, String> row = parseCsvLineToMap(line, params);
                Station station = validateAndCreateStation(row);

                if (station != null) {
                    stationList.add(station);
                }

                lineNo++;
            }
        }

        // Stations sort (first by coordinates, if coordinates are the same sort by station name)
        stationList.sort(Comparator.comparingDouble(Station::getStationLatitude).thenComparingDouble(Station::getStationLongitude).thenComparing(Station::getStationName));

        return stationList;
    }

    private Map<String, String> parseCsvLineToMap(String line, String[] params) {
        Map<String, String> row = new HashMap<>();
        List<String> values = parseCsvValues(line);

        for (int i = 0; i < params.length; i++) {
            row.put(params[i].trim(), values.get(i).trim());
        }

        return row;
    }

    private List<String> parseCsvValues(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean timeZone = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                timeZone = true;
                currentValue.append(c);
            } else if (c == ',' && !timeZone) {
                values.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }

        // This line adds the last value, not included in the previous cycle
        values.add(currentValue.toString());

        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i).trim();

            if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                values.set(i, value.substring(1, value.length() - 1));
            } else {
                values.set(i, value);
            }
        }

        return values;
    }

    private Station validateAndCreateStation(Map<String, String> line) {
        String stationName = line.getOrDefault("station", "").trim();
        String country = line.getOrDefault("country", "").trim();
        String timeZoneGroup = line.getOrDefault("time_zone_group", "").trim();

        if (stationName.isEmpty() || country.isEmpty() || timeZoneGroup.isEmpty()) {
            return null;
        }

        double latitude, longitude;
        try {
            latitude = Double.parseDouble(line.getOrDefault("latitude", ""));
            longitude = Double.parseDouble(line.getOrDefault("longitude", ""));
        } catch (NumberFormatException e) {
            return null;
        }

        if (latitude < -90 || latitude > 90) {
            return null;
        }
        if (longitude < -180 || longitude > 180) {
            return null;
        }

        String timeZone = line.getOrDefault("time_zone", "").trim();

        boolean isCity = parseBoolean(line.getOrDefault("is_city", "False"));
        boolean isMainStation = parseBoolean(line.getOrDefault("is_main_station", "False"));
        boolean isAirport = parseBoolean(line.getOrDefault("is_airport", "False"));

        return new Station(stationName, country, timeZone, timeZoneGroup, latitude, longitude, isCity, isMainStation, isAirport);
    }

    private boolean parseBoolean(String value) {
        String norm = value.trim().toLowerCase();
        return norm.equals("true");
    }
}
