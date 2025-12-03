package spatialQuerying.controller;

import freightMngt.domain.Station;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class StationController {
    private List<Station> stationList;

    /**
     * Constructs a new StationController with the provided station list.
     *
     * <p>
     * The provided list will be cleared and repopulated when
     * collectStationsData() is called.
     * </p>
     *
     * @param stationList the list to store validated stations (must not be null)
     */
    public StationController(List<Station> stationList) {
        this.stationList = stationList;
    }

    /**
     * Reads station data from a CSV file, validates records, and populates the station list.
     *
     * <p>
     * The method clears any existing data in the station list before loading.
     * The CSV file must have a header row with the following columns:
     * country, time_zone, time_zone_group, station, latitude, longitude, is_city, is_main_station, is_airport.
     * </p>
     * <p>
     * Records that fail validation are silently skipped. After loading, stations are automatically sorted by:
     * <ol>
     *   <li>Latitude (ascending)</li>
     *   <li>Longitude (ascending)</li>
     *   <li>Station name (ascending, for stations with identical coordinates)</li>
     * </ol>
     * </p>
     *
     * @param filePath the path to the CSV file containing station data
     *
     * @return the list of successfully loaded and validated stations
     *
     * @throws IOException if the file cannot be read or does not exist
     */
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

        stationList.sort(Comparator.comparingDouble(Station::getStationLatitude).thenComparingDouble(Station::getStationLongitude).thenComparing(Station::getStationName));

        return stationList;
    }

    /**
     * Parses a CSV line into a map of column names to values.
     *
     * <p>
     * This method properly handles CSV fields enclosed in quotes, including
     * fields that contain commas (such as time zone values).
     * </p>
     *
     * @param line the CSV line to parse
     * @param params the array of column names from the header
     *
     * @return a map where keys are column names and values are the corresponding data
     */
    private Map<String, String> parseCsvLineToMap(String line, String[] params) {
        Map<String, String> row = new HashMap<>();
        List<String> values = parseCsvValues(line);

        for (int i = 0; i < params.length; i++) {
            row.put(params[i].trim(), values.get(i).trim());
        }

        return row;
    }

    /**
     * Parses a CSV line into a list of values, handling quoted fields correctly.
     *
     * <p>
     * This method recognizes double quotes as field delimiters and properly
     * handles commas within quoted fields. For example, the field
     * "('Europe/Paris',)" will be parsed as a single value despite containing a comma.
     * </p>
     * <p>
     * Algorithm:
     * <ol>
     *   <li>Track whether we're inside a quoted field</li>
     *   <li>Only split on commas that are outside quotes</li>
     *   <li>Remove surrounding quotes from quoted fields</li>
     * </ol>
     * </p>
     *
     * @param line the CSV line to parse
     *
     * @return a list of values extracted from the line
     */
    private List<String> parseCsvValues(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString());
                currentValue.setLength(0);
            } else {
                currentValue.append(c);
            }
        }

        values.add(currentValue.toString());

        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i).trim();
            if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                value = value.substring(1, value.length() - 1);
            }
            values.set(i, value);
        }

        return values;
    }

    /**
     * Validates a CSV row and creates a Station object if all validation rules pass.
     *
     * <p>
     * Validation rules:
     * <ul>
     *   <li>Station name must not be empty</li>
     *   <li>Country must not be empty</li>
     *   <li>Time zone group must not be empty</li>
     *   <li>Latitude must be a valid number in range [-90, 90]</li>
     *   <li>Longitude must be a valid number in range [-180, 180]</li>
     * </ul>
     * </p>
     * <p>
     * If any validation rule fails, this method returns null and the record is rejected.
     * </p>
     *
     * @param line a map containing the column name to value mappings for one CSV row
     *
     * @return a valid Station object, or null if validation fails
     */
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

    /**
     * Parses a string value to a boolean.
     * <p>
     * Recognizes "true" (case-insensitive) as true, all other values as false.
     * </p>
     *
     * @param value the string value to parse
     *
     * @return true if the value equals "true" (case-insensitive), false otherwise
     */
    private boolean parseBoolean(String value) {
        String norm = value.trim().toLowerCase();
        return norm.equals("true");
    }
}
