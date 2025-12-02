package app;

import spatialQuerying.controller.GeographicalSearch;
import spatialQuerying.controller.ProximitySearchController;
import spatialQuerying.controller.RadiusSearch;
import spatialQuerying.controller.StationController;
import spatialQuerying.railwayMngt.Station;
import spatialQuerying.trees.bst.LatitudeTree;
import spatialQuerying.trees.bst.LongitudeTree;
import spatialQuerying.trees.bst.TimeZoneTree;
import spatialQuerying.trees.kd.KDTree2D;
import warehouseMngt.controller.ReturnsController;
import warehouseMngt.controller.WarehouseController;
import warehouseMngt.order.Allocation;
import warehouseMngt.order.Order;
import warehouseMngt.order.OrderAllocationResult;
import warehouseMngt.picking.*;
import warehouseMngt.returns.Returns;
import warehouseMngt.service.LoadData;
import warehouseMngt.warehouse.Warehouse;

import java.io.IOException;
import java.util.*;

public class MenuESINF {
    private static OrderAllocationResult lastAllocationResult = null;
    private static PickingPlan lastPlan = null;
    private static final Scanner sc = new Scanner(System.in);

    private static List<Station> stationList = null;
    private static LatitudeTree latTree = null;
    private static LongitudeTree lonTree = null;
    private static TimeZoneTree timeZoneTree = null;
    private static KDTree2D kdTree = null;
    private static GeographicalSearch geographicalSearch = null;

    public static void menuESINF() {
        Warehouse warehouse = new Warehouse(
                LoadData.getWagons(),
                LoadData.getItems(),
                LoadData.getOrderLines(),
                LoadData.getOrders(),
                LoadData.getReturns(),
                LoadData.getWarehouses()
        );

        WarehouseController warehouseController = new WarehouseController(warehouse);

        int op2;

        do {
            System.out.println("\n==== ESINF MENU ====");
            System.out.println("1. USEI01 - Wagons Unloading (FEFO/FIFO)");
            System.out.println("2. USEI02 - Order Eligibility & Allocation");
            System.out.println("3. USEI03 - Picking Plan (Packing Heuristics)");
            System.out.println("4. USEI04 - Pick Path Sequencing");
            System.out.println("5. USEI05 - Returns & Quarantine");
            System.out.println("6. USEI06 - Time-Zone Index and Windowed Queries");
            System.out.println("7. USEI07 - Build Balanced 2D-Tree on Latitude/Longitude");
            System.out.println("8. USEI08 - Search by Geographical Area");
            System.out.println("9. USEI09 - Proximity Search (Nearest-N with Filters)");
            System.out.println("10. USEI10 - Radius search and density summary");
            System.out.println("0. Return to Main Menu");
            System.out.print("Select option: ");
            op2 = sc.nextInt();
            sc.nextLine();

            switch (op2) {
                case 1 -> {
                    System.out.println("Executing USEI01: Wagons Unloading...");
                    warehouseController.unloadWagonsIntoBays();

                    System.out.println("\nUnloading complete!");
                    warehouseController.printMovementHistory();

                    if (!warehouseController.getImportErrors().isEmpty()) {
                        System.out.println("\nImport Errors:");
                        warehouseController.getImportErrors().forEach(System.out::println);
                    }
                }

                case 2 -> {
                    System.out.println("Executing USEI02 - Order Eligibility & Allocation...");

                    Map<String, Order> openOrders = LoadData.getOrders();
                    if (openOrders.isEmpty()) {
                        System.out.println("No open orders have been found.");
                        break;
                    }

                    for (String orderId : openOrders.keySet()) {
                        System.out.println("\nProcessing Order ID: " + orderId);
                        OrderAllocationResult result = warehouseController.allocateOrder(orderId, false);

                        System.out.println("\n=== Allocation Summary ===");
                        System.out.println(result.generateSummaryReport());

                        lastAllocationResult = result;
                    }
                    System.out.println("\nAll orders have been processed.");
                }

                case 3 -> {
                    System.out.println("\nExecuting USEI03 - Picking Plan (Packing Heuristics)...");

                    if (lastAllocationResult == null || lastAllocationResult.getAllAllocations().isEmpty()) {
                        System.out.println("No allocations found! Please run USEI02 first.");
                        break;
                    }

                    Map<String, Double> itemWeights = new HashMap<>();
                    LoadData.getItems().forEach((sku, item) ->
                            itemWeights.put(sku, item.getWeight())
                    );

                    PickingPlanService planService = new PickingPlanService(itemWeights);

                    System.out.print("Enter trolley capacity (e.g., 20.0): ");
                    double capacity = Double.parseDouble(sc.nextLine().trim());

                    System.out.println("\nSelect packing heuristic:");
                    System.out.println("1. FIRST_FIT");
                    System.out.println("2. FIRST_FIT_DECREASING");
                    System.out.println("3. BEST_FIT_DECREASING");
                    System.out.print("Option: ");
                    int h = Integer.parseInt(sc.nextLine().trim());

                    PackingHeuristic heuristic = switch (h) {
                        case 1 -> PackingHeuristic.FIRST_FIT;
                        case 2 -> PackingHeuristic.FIRST_FIT_DECREASING;
                        case 3 -> PackingHeuristic.BEST_FIT_DECREASING;
                        default -> throw new IllegalStateException("Unexpected value: " + h);
                    };

                    PickingPlan plan = planService.createPickingPlan(
                            lastAllocationResult.getAllAllocations(),
                            capacity,
                            heuristic
                    );

                    lastPlan = plan;

                    System.out.println("\n=== PICKING PLAN SUMMARY ===");
                    System.out.println("Heuristic: " + plan.getHeuristic());
                    System.out.println("Trolley Capacity: " + plan.getTrolleyCapacity() + " kg");
                    System.out.println("Total Number of Trolleys: " + plan.getTrolleyCount());
                    System.out.println("Skipped Allocations: " + plan.getSkippedCount());
                    System.out.println("\nTrolley Utilization:");
                    for (Trolley trolley : plan.getTrolleys()) {
                        System.out.printf("Trolley %d: %.2f%% full%n",
                                trolley.getTrolleyNumber(), trolley.getUtilization());
                    }

                    if (plan.getSkippedCount() > 0) {
                        System.out.println("\nSkipped Allocations (exceeded capacity):");
                        for (PickingPlan.SkippedAllocation skipped : plan.getSkippedAllocations()) {
                            System.out.printf("  BoxID: %s, SKU: %s, Weight: %.2f kg%n",
                                    skipped.allocation.getBoxID(),
                                    skipped.allocation.getSku(),
                                    skipped.weight);
                        }
                    }

                    System.out.println("\n=== DETAILED PICKING LIST ===");
                    System.out.println("Picking Plan: " + plan.getHeuristic() + "\n");

                    for (Trolley trolley : plan.getTrolleys()) {
                        System.out.println("Trolley " + trolley.getTrolleyNumber());
                        System.out.printf("Utilization: %.2f%%%n%n", trolley.getUtilization());

                        for (var entry : trolley.getAllocationEntries()) {
                            Allocation alloc = entry.allocation;
                            System.out.println("Order ID: " + alloc.getOrderID());
                            System.out.println("Line Number: " + alloc.getLineNo());
                            System.out.println("Aisle: " + alloc.getAisle());
                            System.out.println("Bay Number: " + alloc.getBayNumber());
                            System.out.println("Box ID: " + alloc.getBoxID());
                            System.out.println("SKU: " + alloc.getSku());
                            System.out.print("Quantity: " + alloc.getQuantityAllocated());
                            if (entry.isPartial) {
                                System.out.printf(" (PARTIAL: %d/%d)",
                                        alloc.getQuantityAllocated(), entry.originalQuantity);
                            }
                            System.out.println("\n");
                        }
                        System.out.println();
                    }
                }

                case 4 -> {
                    System.out.println("Executing USEI04 - Picking Path Optimization...");

                    if (lastPlan == null || lastPlan.getTrolleys().isEmpty()) {
                        System.out.println("No picking plan found! Please run USEI03 first.");
                        break;
                    }

                    PickingPath pickingPath = new PickingPath();

                    System.out.println("\nSelect path optimization strategy:");
                    System.out.println("1. Ascending Order Strategy");
                    System.out.println("2. Nearest Neighbor Strategy");
                    System.out.print("Option: ");
                    int pathOption = sc.nextInt();
                    sc.nextLine();

                    int trolleyCounter = 1;

                    for (Trolley trolley : lastPlan.getTrolleys()) {
                        System.out.println("\n=== Trolley #" + trolleyCounter++ + " ===");
                        List<Allocation> allocations = trolley.getAllocations();

                        if (allocations.isEmpty()) {
                            System.out.println("No allocations for this trolley.");
                            continue;
                        }

                        List<Allocation> optimizedPath = (pathOption == 1)
                                ? pickingPath.getPathAscending(allocations)
                                : pickingPath.getPathNearest(allocations);

                        int totalDistance = pickingPath.calculatePathDistance(optimizedPath);
                        System.out.println("Total Distance for this Trolley: " + totalDistance + " units");

                        System.out.println("\nOptimized Path:");
                        optimizedPath.forEach(a ->
                                System.out.printf("Aisle: %d, Bay: %d, SKU: %s, Qty: %d%n",
                                        a.getAisle(), a.getBayNumber(), a.getSku(), a.getQuantityAllocated()));
                    }
                }

                case 5 -> {
                    System.out.println("\nExecuting USEI05 - Returns & Quarantine...");

                    Map<String, Returns> returns = LoadData.getReturns();
                    if (returns == null || returns.isEmpty()) {
                        System.out.println("No returns have been found in the system.");
                        break;
                    }

                    System.out.println("\nFound " + returns.size() + " returns to process.");

                    String logPath = "audit-log-file/";
                    ReturnsController returnsController = new ReturnsController(warehouse, logPath);

                    try {
                        List<Returns> returnsList = new ArrayList<>(returns.values());
                        List<ReturnsController.ReturnProcessingResult> results =
                                returnsController.processBatchReturns(returnsList);

                        for (ReturnsController.ReturnProcessingResult result : results) {
                            System.out.println(result);
                        }

                        System.out.println("\nAudit log saved to: " + logPath + "audit-log.txt");
                    } catch (IOException e) {
                        System.out.println("Error processing returns: " + e.getMessage() + ".");
                        e.printStackTrace();
                    }
                }

                case 6 -> {
                    System.out.println("\nExecuting USEI06 - Time-Zone Index and Windowed Queries...");

                    try {
                        if (stationList == null) {
                            String filePath = "csv-docs/train_stations_europe.csv";

                            StationController stationController = new StationController(new ArrayList<>());
                            stationList = stationController.collectStationsData(filePath);

                            if (stationList.isEmpty()) {
                                System.out.println("No stations found or file is empty.");
                                break;
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

                        System.out.println("Theoretical Time Complexity:");
                        System.out.println("• Exact Search (TimeZone):  O(log n + k) = O(" + h + " + k)");
                        System.out.println("    where k = number of matching results");
                        System.out.println("• Range Search (Lat/Lon):   O(log n + k) = O(" + h + " + k)");
                        System.out.println("    where k = number of results in range");
                        System.out.println();
                        System.out.println("Explanation:");
                        System.out.println("- AVL trees maintain O(log n) height for balanced searching");
                        System.out.println("- Finding the starting point takes O(log n) time");
                        System.out.println("- Collecting k results takes O(k) time");
                        System.out.println("- Total: O(log n + k) where k is the result size");

                    } catch (IOException e) {
                        System.out.println("Error loading stations: " + e.getMessage());
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                case 7 -> {
                    System.out.println("\nExecuting USEI07 - Build Balanced 2D-Tree on Latitude/Longitude...");

                    try {
                        if (stationList == null || latTree == null || lonTree == null) {
                            System.out.println("Please run USEI06 first to load stations and build AVL trees!");
                            break;
                        }

                        System.out.println("Building 2D-Tree from " + stationList.size() + " stations...");

                        kdTree = new KDTree2D();
                        kdTree.buildBalancedTree(stationList, latTree, lonTree);

                        System.out.println("\n=== 2D-TREE SUCCESSFULLY BUILT ===");
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

                        System.out.println("\n=== TEMPORAL COMPLEXITY ANALYSIS ===");

                        int totalStations = stationList.size();
                        int uniquePoints = kdTree.getSize();

                        System.out.println("Building the 2D-Tree:");
                        System.out.println("1. Grouping stations by coordinates: O(m)");
                        System.out.println("   - m = " + totalStations + " total stations");
                        System.out.println("   - Results in n = " + uniquePoints + " unique points");

                        System.out.println("\n2. Sorting using AVL in-order traversal: O(m)");
                        System.out.println("   - Called twice (latitude and longitude)");
                        System.out.println("   - Total: O(2m) = O(m)");
                        System.out.println("   - Advantage: avoids O(n log n) comparison-based sorting");

                        System.out.println("\n3. Recursive balanced tree construction: O(n log n)");
                        System.out.println("   - n = " + uniquePoints + " unique coordinate points");
                        System.out.println("   - Each level processes all n points: O(n) per level");
                        System.out.println("   - Tree has log₂(n) ≈ " + (int)Math.ceil(Math.log(uniquePoints)/Math.log(2)) + " levels");
                        System.out.println("   - Total: O(n) × log(n) = O(n log n)");

                        System.out.println("\n4. Calculate metrics: O(n)");
                        System.out.println("   - Preorder traversal of all n nodes");

                        System.out.println("\n" + "=".repeat(50));
                        System.out.println("OVERALL COMPLEXITY:");
                        System.out.println("Time: O(m + n log n)");
                        System.out.println("  where m = " + totalStations + " total stations");
                        System.out.println("        n = " + uniquePoints + " unique points");

                        if (totalStations == uniquePoints) {
                            System.out.println("  → Simplifies to O(n log n) (no duplicates)");
                        } else {
                            System.out.println("  → m > n due to " + (totalStations - uniquePoints) + " duplicate coordinates");
                        }

                        System.out.println("\nSpace: O(m + n)");
                        System.out.println("  - Tree structure: O(n) nodes");
                        System.out.println("  - Station buckets: O(m) total stations");
                        System.out.println("  - Actual height: " + kdTree.getHeight() +
                                " (optimal: " + (int)Math.ceil(Math.log(uniquePoints)/Math.log(2)) + ")");


                    } catch (Exception e) {
                        System.out.println("Error building 2D-Tree: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                case 8 -> {
                    System.out.println("\nExecuting USEI08 - Search by Geographical Area...");

                    if (kdTree == null) {
                        System.out.println("Please run USEI07 first to build the 2D-Tree!");
                        break;
                    }

                    if (geographicalSearch == null) {
                        geographicalSearch = new GeographicalSearch(kdTree);
                    }

                    System.out.print("Enter latitude: ");
                    double lat = sc.nextDouble();
                    System.out.print("Enter longitude: ");
                    double lon = sc.nextDouble();
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

                    List<Station> results = geographicalSearch.searchByCoordinates(lat, lon, isCity, isMainStation, country);

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

                    System.out.println("\n=== TEMPORAL COMPLEXITY ANALYSIS ===");
                    System.out.println("Geographical Area Search on Balanced KD-Tree (USEI08):");
                    System.out.println("1. Initial Search: O(log n)");
                    System.out.println("   - Tree traversal to find target coordinate");
                    System.out.println("2. Area Search: O(m)");
                    System.out.println("   - m = number of points in target region");
                    System.out.println("3. Filter Application: O(m)");
                    System.out.println("   - One pass through found points to apply filters");

                    System.out.println("\nOverall Time Complexity:");
                    System.out.println("O(log n + m)");
                    System.out.println("where:");
                    System.out.println("- n is total number of points in tree");
                    System.out.println("- m is number of points in search area");
                }
                case 9 -> {
                    System.out.println("\nExecuting USEI09 - Proximity Search (Nearest-N with Filters)...");
                    try {
                        if (kdTree == null || kdTree.getRoot() == null) {
                            System.out.println("Please run USEI07 first to build the 2D-Tree!");
                            break;
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
                            break;
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
                        System.out.println("\n=== TEMPORAL COMPLEXITY ANALYSIS ===");
                        System.out.println();

                        System.out.println("What does each variabe mean?");
                        System.out.println("   • n  → total stations in the tree");
                        System.out.println("   • k → number of neighbors we want to find");
                        System.out.println();

                        System.out.println("Step 1: Navigate the tree → O(log n)");
                        System.out.println("   Walk down the tree to find the target area");
                        System.out.println();
                        System.out.println("Step 2: Track k best stations → O(log k) per station visited");
                        System.out.println("   Keep only the k closest stations in a priority queue");
                        System.out.println();
                        System.out.println("Step 3: Pruning → skip branches that are too far");
                        System.out.println("   Avoids checking stations that can't be closer");
                        System.out.println();

                        System.out.println("Time Complexity:");
                        System.out.println("   • Best case:    O(log n + k log k) - neighbors are all in the same region");
                        System.out.println("   • Worst case:   O(n log k) - must check all n stations");
                        System.out.println();
                    } catch (Exception e) {
                        System.out.println("Error during proximity search: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                case 10 -> {
                    System.out.println("\nExecuting USEI10 - Radius search and density summary...");
                    try {
                        if (kdTree == null || kdTree.getRoot() == null) {
                            System.out.println("Please run USEI07 first to build the 2D-Tree!");
                            break;
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
                            break;
                        }

                        RadiusSearch.StationResult result = radiusSearch.searchByRadius(targetLat, targetLon, radius);

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

                        System.out.println("\n=== TEMPORAL COMPLEXITY ANALYSIS ===");
                        System.out.println("Radius Search on Balanced KD-Tree (USEI10):");
                        System.out.println("1. Initial Traversal and Pruning: O(log n)");
                        System.out.println("2. Area Coverage: O(m), where m is number of nodes in search area");
                        System.out.println("3. Distance Calculation: O(1) per point");
                        System.out.println("4. Result Sorting: O(k log k), where k is number of results");

                        System.out.println("\nOverall Time Complexity:");
                        System.out.println("O(log n + m + k log k)");
                        System.out.println("where:");
                        System.out.println("- n is total number of points in tree");
                        System.out.println("- m is number of points in search area");
                        System.out.println("- k is number of results found");

                    } catch (Exception e) {
                        System.out.println("Error during radius search: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                case 0 -> System.out.println("Returning to Main Menu...");
                default -> System.out.println("Invalid option.");
            }
        } while (op2 != 0);
    }
}