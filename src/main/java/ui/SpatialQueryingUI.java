package ui;

import freightMngt.domain.Station;
import spatialQuerying.controller.GeographicalSearch;
import spatialQuerying.controller.ProximitySearchController;
import spatialQuerying.controller.RadiusSearch;
import spatialQuerying.controller.StationController;
import spatialQuerying.structures.bst.LatitudeTree;
import spatialQuerying.structures.bst.LongitudeTree;
import spatialQuerying.structures.bst.TimeZoneTree;
import spatialQuerying.structures.kd.KDTree2D;

import java.io.IOException;
import java.util.*;

public class SpatialQueryingUI implements Runnable {
    private final Scanner sc = new Scanner(System.in);
    private static List<Station> stationList = null;
    private static LatitudeTree latTree = null;
    private static LongitudeTree lonTree = null;
    private static TimeZoneTree timeZoneTree = null;
    private static KDTree2D kdTree = null;
    private static GeographicalSearch geographicalSearch = null;

    @Override
    public void run() {
        boolean exit = false;

        while (!exit) {
            showMenu();

            System.out.print("\nEnter your choice: ");
            int input = sc.nextInt();
            sc.nextLine();

            switch (input) {
                case 1:
                    queries();
                    break;
                case 2:
                    kdTree();
                    break;
                case 3:
                    searchArea();
                    break;
                case 4:
                    proximitySearch();
                    break;
                case 5:
                    radiusSearch();
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void showMenu() {
        System.out.println("\n══════════════════════ SPATIAL QUERYING MENU ══════════════════════");

        System.out.println("1. Time-Zone Index and Windowed Queries");
        System.out.println("2. Build Balanced 2D-Tree on Latitude/Longitude");
        System.out.println("3. Search by Geographical Area");
        System.out.println("4. Proximity Search (Nearest-N with Filters)");
        System.out.println("5. Radius Search & Density summary");
        System.out.println("0. Exit");
        System.out.println("══════════════════════════════════════════════════════════════════════");
    }


    private void queries() {
        System.out.println("\nExecuting USEI06 - Time-Zone Index and Windowed Queries...");

        try {
            if (stationList == null) {
                String filePath = "csv-docs/train_stations_europe.csv";

                StationController stationController = new StationController(new ArrayList<>());
                stationList = stationController.collectStationsData(filePath);

                if (stationList.isEmpty()) {
                    System.out.println("No stations found or file is empty.");
                    return;
                }

                System.out.println("Loaded " + stationList.size() + " stations successfully.");
            }

            if (latTree == null || lonTree == null || timeZoneTree == null) {
                System.out.println("\nBuilding AVL trees (Latitude, Longitude, TimeZone)...");

                latTree = new LatitudeTree();
                lonTree = new LongitudeTree();
                timeZoneTree = new TimeZoneTree();

                for (Station station : stationList) {
                    latTree.insert(station);
                    lonTree.insert(station);
                    timeZoneTree.insert(station);
                }

                System.out.println("AVL trees built successfully!");
            }
            printSampleQueries();

        } catch (IOException e) {
            System.out.println("Error loading stations: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printSampleQueries() {
        System.out.println("\nPerforming example searches...\n");

        System.out.println("1. LATITUDE TREE - Range Search");
        System.out.println("Searching for stations between latitude 49.0 and 50.0:");
        long startTime = System.nanoTime();
        List<Station> latResults = latTree.findInLatitudeRange(49.0, 50.0);
        long endTime = System.nanoTime();
        long latSearchTime = endTime - startTime;

        System.out.println("Found " + latResults.size() + " stations in the given range.");
        if (!latResults.isEmpty()) {
            System.out.println("Sample results (first 10):");
            for (int i = 0; i < Math.min(10, latResults.size()); i++) {
                Station s = latResults.get(i);
                System.out.printf("- %s (Lat: %.4f)\n", s.getStationName(), s.getStationLatitude());
            }
        }
        System.out.println();

        System.out.println("2. LONGITUDE TREE - Range Search");
        System.out.println("Searching for stations between longitude 2.0 and 3.0:");
        startTime = System.nanoTime();
        List<Station> lonResults = lonTree.findInLongitudeRange(2.0, 3.0);
        endTime = System.nanoTime();
        long lonSearchTime = endTime - startTime;

        System.out.println("Found " + lonResults.size() + " stations in the given range.");
        if (!lonResults.isEmpty()) {
            System.out.println("Sample results (first 10):");
            for (int i = 0; i < Math.min(10, lonResults.size()); i++) {
                Station s = lonResults.get(i);
                System.out.printf("- %s (Lon: %.4f)\n", s.getStationName(), s.getStationLongitude());
            }
        }
        System.out.println();

        System.out.println("3. TIMEZONE TREE - Exact Search");
        System.out.println("Searching for stations in timezone group 'CET':");
        startTime = System.nanoTime();
        List<Station> tzResults = timeZoneTree.findByTimeZoneGroup("CET");
        endTime = System.nanoTime();
        long tzSearchTime = endTime - startTime;

        System.out.println("There are " + tzResults.size() + " stations in this Time Zone.");
        if (!tzResults.isEmpty()) {
            System.out.println("Sample results (first 10):");
            for (int i = 0; i < Math.min(10, tzResults.size()); i++) {
                Station s = tzResults.get(i);
                System.out.printf("- %s (TimeZone: %s)\n", s.getStationName(), s.getStationTimeZone());
            }
        }
        System.out.println();

        System.out.println("\n=== TIME COMPLEXITY ANALYSIS ===\n");

        int n = stationList.size();
        int h = (int) Math.ceil(Math.log(n) / Math.log(2)); // Tree height ≈ log₂(n)

        System.out.println("Total stations in dataset: " + n);
        System.out.println("Estimated AVL tree height: " + h + " (≈ log₂(" + n + "))");
        System.out.println();

        System.out.println("Search Results:");
        System.out.printf("Latitude Range Search:  %,10d ns (%.3f ms) - Found %d stations\n",
                latSearchTime, latSearchTime / 1_000_000.0, latResults.size());
        System.out.printf("Longitude Range Search: %,10d ns (%.3f ms) - Found %d stations\n",
                lonSearchTime, lonSearchTime / 1_000_000.0, lonResults.size());
        System.out.printf("TimeZone Exact Search:  %,10d ns (%.3f ms) - Found %d stations\n",
                tzSearchTime, tzSearchTime / 1_000_000.0, tzResults.size());
        System.out.println();
    }


    private void kdTree() {
        System.out.println("\nExecuting USEI07 - Build Balanced 2D-Tree on Latitude/Longitude...");

        try {
            if (stationList == null || latTree == null || lonTree == null) {
                System.out.println("Please run USEI06 first to load stations and build AVL trees!");
                return;
            }

            System.out.println("Building 2D-Tree from " + stationList.size() + " stations...");

            kdTree = new KDTree2D();
            kdTree.buildBalancedTree(stationList, latTree, lonTree);

            System.out.println("\n=== 2D-TREE SUCCESSFULLY BUILT ===");

            printTreeMetrics();

        } catch (Exception e) {
            System.out.println("Error building 2D-Tree: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printTreeMetrics() {
        System.out.println("\n=== TREE METRICS ===");
        System.out.println("Total Unique Points (Size): " + kdTree.getSize());
        System.out.println("Tree Height: " + kdTree.getHeight());

        System.out.println("\n=== BUCKET SIZE DISTRIBUTION ===");
        System.out.println("(Number of stations sharing the same coordinates)");
        System.out.println();

        Map<Integer, Integer> bucketSizes = kdTree.getBucketSizes();
        if (bucketSizes.isEmpty()) {
            System.out.println("No buckets found.");
        } else {
            System.out.println("Bucket Size | Frequency");
            System.out.println("------------|----------");
            bucketSizes.forEach((size, count) ->
                    System.out.printf("%11d | %d node(s)%n", size, count));

            // aditional statistics
            int totalNodes = bucketSizes.values().stream().mapToInt(Integer::intValue).sum();
            int maxBucketSize = Collections.max(bucketSizes.keySet());
            int minBucketSize = Collections.min(bucketSizes.keySet());

            System.out.println("\n=== STATISTICS ===");
            System.out.println("Total nodes: " + totalNodes);
            System.out.println("Max bucket size: " + maxBucketSize + " station(s) per coordinate");
            System.out.println("Min bucket size: " + minBucketSize + " station(s) per coordinate");
        }
    }

    private void searchArea() {
        System.out.println("\nExecuting USEI08 - Search by Geographical Area...");

        if (kdTree == null) {
            System.out.println("Please run USEI07 first to build the 2D-Tree!");
            return;
        }

        if (geographicalSearch == null) {
            geographicalSearch = new GeographicalSearch(kdTree);
        }

        System.out.println("\nEnter coordinate ranges:");
        System.out.print("Minimum latitude: ");
        double latMin = sc.nextDouble();
        System.out.print("Maximum latitude: ");
        double latMax = sc.nextDouble();
        System.out.print("Minimum longitude: ");
        double lonMin = sc.nextDouble();
        System.out.print("Maximum longitude: ");
        double lonMax = sc.nextDouble();
        sc.nextLine();

        System.out.println("\n=== Optional Filters ===");
        System.out.print("Filter by city status (y/n/enter to skip): ");
        String cityInput = sc.nextLine().trim().toLowerCase();
        Boolean isCity = cityInput.isEmpty() ? null : cityInput.startsWith("y");

        System.out.print("Filter by main station status (y/n/enter to skip): ");
        String mainInput = sc.nextLine().trim().toLowerCase();
        Boolean isMainStation = mainInput.isEmpty() ? null : mainInput.startsWith("y");

        System.out.print("Filter by country (PT/ES/all/enter to skip): ");
        String countryInput = sc.nextLine().trim();
        String country = countryInput.isEmpty() ? null : countryInput;

        try {
            List<Station> results = geographicalSearch.searchByArea(latMin, latMax, lonMin, lonMax,
                    isCity, isMainStation, country);

            System.out.println("\n=== Results ===");
            if (results.isEmpty()) {
                System.out.println("No stations found matching the criteria.");
            } else {
                System.out.println("Found " + results.size() + " station(s):");
                results.forEach(s -> System.out.printf("- %s (%s)%n    City: %s | Main Station: %s%n",
                        s.getStationName(), s.getStationCountry(),
                        s.isCity() ? "Yes" : "No",
                        s.isMainStation() ? "Yes" : "No"));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void proximitySearch() {
        System.out.println("\nExecuting USEI09 - Proximity Search (Nearest-N with Filters)...");
        try {
            if (kdTree == null || kdTree.getRoot() == null) {
                System.out.println("Please run USEI07 first to build the 2D-Tree!");
                return;
            }

            ProximitySearchController proximityController = new ProximitySearchController(kdTree);

            System.out.print("Enter target latitude: ");
            double targetLat = sc.nextDouble();

            System.out.print("Enter target longitude: ");
            double targetLon = sc.nextDouble();

            System.out.print("Enter number of nearest neighbors (N): ");
            int n = sc.nextInt();
            sc.nextLine();

            if (n <= 0) {
                System.out.println("N must be greater than 0.");
                return;
            }

            System.out.println("\nApply Time Zone Filter?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            System.out.print("Option: ");
            int filterOption = sc.nextInt();
            sc.nextLine();

            String timeZoneFilter = null;
            if (filterOption == 1) {
                System.out.print("Enter time zone filter (CET, WET, EET, etc.): ");
                timeZoneFilter = sc.nextLine().trim();

                if (timeZoneFilter.isEmpty()) {
                    System.out.println("Time zone filter cannot be empty. Proceeding without filter.");
                    timeZoneFilter = null;
                }
            }


            Map<String, Object> response =
                    proximityController.findNearestStations(targetLat, targetLon, n, timeZoneFilter);

            @SuppressWarnings("unchecked")
            List<Station> stations = (List<Station>) response.get("stations");

            @SuppressWarnings("unchecked")
            List<Double> distances = (List<Double>) response.get("distances");

            @SuppressWarnings("unchecked")
            Map<String, Double> targetCoordinates = (Map<String, Double>) response.get("targetCoordinates");

            @SuppressWarnings("unchecked")
            Map<String, Integer> searchParameters = (Map<String, Integer>) response.get("searchParameters");

            @SuppressWarnings("unchecked")
            List<String> appliedFilters = (List<String>) response.get("appliedFilters");

            printResults(stations, distances);

        } catch (Exception e) {
            System.out.println("Error during proximity search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printResults(List<Station> stations, List<Double> distances) {
        System.out.println("\n=== PROXIMITY SEARCH RESULTS ===");
        System.out.println();

        System.out.println("\n--- Nearest Stations ---");

        if (stations.isEmpty()) {
            System.out.println("No stations found matching the criteria.");
        } else {
            for (int i = 0; i < stations.size(); i++) {
                Station station = stations.get(i);
                double distance = distances.get(i);

                System.out.printf("%d. %s (%.2f km)%n",
                        i + 1,
                        station.getStationName(),
                        distance);
                System.out.printf("   Country: %s, Time Zone: %s%n",
                        station.getStationCountry(),
                        station.getStationTimeZoneGroup());
                System.out.printf("   Coordinates: (lat: %.6f, lon: %.6f)%n%n",
                        station.getStationLatitude(),
                        station.getStationLongitude());
            }
        }
    }

    private void radiusSearch() {
        System.out.println("\nExecuting USEI10 - Radius search and density summary...");
        try {
            if (kdTree == null || kdTree.getRoot() == null) {
                System.out.println("Please run USEI07 first to build the 2D-Tree!");
                return;
            }

            RadiusSearch radiusSearch = new RadiusSearch(kdTree);

            System.out.print("Enter target latitude: ");
            double targetLat = sc.nextDouble();

            System.out.print("Enter target longitude: ");
            double targetLon = sc.nextDouble();

            System.out.print("Enter search radius (km): ");
            double radius = sc.nextDouble();
            sc.nextLine();

            if (radius <= 0) {
                System.out.println("Radius must be greater than 0.");
                return;
            }

            RadiusSearch.StationResult result = radiusSearch.searchByRadius(targetLat, targetLon, radius);

            printRadiusResults(targetLat, targetLon, radius, result);

        } catch (Exception e) {
            System.out.println("Error during radius search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printRadiusResults(double targetLat, double targetLon, double radius, RadiusSearch.StationResult result) {
        System.out.println("\n=== RADIUS SEARCH RESULTS ===");
        System.out.printf("Target Location: (%.6f, %.6f)%n", targetLat, targetLon);
        System.out.printf("Search Radius: %.2f km%n", radius);
        System.out.println("Number of stations found: " + result.getStations().size());

        System.out.println("\n--- Stations within radius ---");
        if (result.getStations().isEmpty()) {
            System.out.println("No stations found within the specified radius.");
        } else {
            int count = 1;
            for (Station station : result.getStations()) {
                System.out.printf("%d. %s%n", count++, station.getStationName());
                System.out.printf("   Country: %s, Time Zone: %s%n",
                        station.getStationCountry(),
                        station.getStationTimeZoneGroup());
                System.out.printf("   City: %s, Main Station: %s%n",
                        station.isCity() ? "Yes" : "No",
                        station.isMainStation() ? "Yes" : "No");
                System.out.printf("   Coordinates: (lat: %.6f, lon: %.6f)%n%n",
                        station.getStationLatitude(),
                        station.getStationLongitude());
            }
        }

        System.out.println("\n=== DENSITY SUMMARY ===");
        System.out.println("Distribution by Country and City Status:");
        for (Map.Entry<String, Integer> entry : result.getDensitySummary().entrySet()) {
            String[] parts = entry.getKey().split("_");
            String country = parts[0];
            String cityStatus = parts[1];
            System.out.printf("%s (%s): %d stations%n",
                    country,
                    cityStatus.equals("City") ? "City" : "Not City",
                    entry.getValue());
        }
    }
}