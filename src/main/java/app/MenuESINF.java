package app;

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

                        int queryOption;
                        do {
                            System.out.println("\n=== TIME-ZONE QUERIES ===");
                            System.out.println("1. Search by TimeZone Group");
                            System.out.println("2. Search by TimeZone Group and Country");
                            System.out.println("3. Search in TimeZone Range (Window)");
                            System.out.println("4. Search by Latitude Range");
                            System.out.println("5. Search by Longitude Range");
                            System.out.println("0. Back to ESINF Menu");
                            System.out.print("Select query option: ");
                            queryOption = sc.nextInt();
                            sc.nextLine();

                            switch (queryOption) {
                                case 1 -> {
                                    System.out.print("Enter TimeZone Group (e.g., CET, WET): ");
                                    String tzGroup = sc.nextLine().trim();

                                    List<Station> results = timeZoneTree.findByTimeZoneGroup(tzGroup);

                                    System.out.println("\n=== Results ===");
                                    System.out.println("Found " + results.size() + " stations in " + tzGroup);

                                    if (!results.isEmpty()) {
                                        System.out.println("\nFirst 10 stations:");
                                        results.stream().limit(10).forEach(s ->
                                                System.out.printf("- %s (%s)%n", s.getStationName(), s.getStationCountry()));
                                    }

                                    System.out.println("\nTemporal Complexity: O(log n + k), where k = number of results");
                                }

                                case 2 -> {
                                    System.out.print("Enter TimeZone Group: ");
                                    String tzGroup = sc.nextLine().trim();
                                    System.out.print("Enter Country (e.g., PT, ES, FR): ");
                                    String country = sc.nextLine().trim();

                                    List<Station> results = timeZoneTree.findByTimeZoneGroupAndCountry(tzGroup, country);

                                    System.out.println("\n=== Results ===");
                                    System.out.println("Found " + results.size() + " stations in " + tzGroup + " (" + country + ")");

                                    if (!results.isEmpty()) {
                                        System.out.println("\nStations (sorted by name):");
                                        results.forEach(s -> System.out.println("- " + s.getStationName()));
                                    }

                                    System.out.println("\nTemporal Complexity: O(log n + k), where k = number of results");
                                }

                                case 3 -> {
                                    System.out.print("Enter minimum TimeZone Group (e.g., CET): ");
                                    String minTz = sc.nextLine().trim();
                                    System.out.print("Enter maximum TimeZone Group (e.g., WET): ");
                                    String maxTz = sc.nextLine().trim();

                                    List<Station> results = timeZoneTree.findInTimeZoneRange(minTz, maxTz);

                                    System.out.println("\n=== Results ===");
                                    System.out.println("Found " + results.size() + " stations in range [" + minTz + ", " + maxTz + "]");

                                    if (!results.isEmpty()) {
                                        System.out.println("\nFirst 10 stations:");
                                        results.stream().limit(10).forEach(s ->
                                                System.out.printf("- %s (%s, %s)%n",
                                                        s.getStationName(),
                                                        s.getStationCountry(),
                                                        s.getStationTimeZoneGroup()));
                                    }

                                    System.out.println("\nTemporal Complexity: O(log n + k), where k = number of results");
                                }

                                case 4 -> {
                                    System.out.print("Enter minimum latitude: ");
                                    double minLat = sc.nextDouble();
                                    System.out.print("Enter maximum latitude: ");
                                    double maxLat = sc.nextDouble();
                                    sc.nextLine();

                                    List<Station> results = latTree.findInLatitudeRange(minLat, maxLat);

                                    System.out.println("\n=== Results ===");
                                    System.out.println("Found " + results.size() + " stations in latitude range [" + minLat + ", " + maxLat + "]");

                                    if (!results.isEmpty()) {
                                        System.out.println("\nFirst 10 stations:");
                                        results.stream().limit(10).forEach(s ->
                                                System.out.printf("- %s (lat: %.6f)%n", s.getStationName(), s.getStationLatitude()));
                                    }

                                    System.out.println("\nTemporal Complexity: O(log n + k), where k = number of results");
                                }

                                case 5 -> {
                                    System.out.print("Enter minimum longitude: ");
                                    double minLon = sc.nextDouble();
                                    System.out.print("Enter maximum longitude: ");
                                    double maxLon = sc.nextDouble();
                                    sc.nextLine();

                                    List<Station> results = lonTree.findInLongitudeRange(minLon, maxLon);

                                    System.out.println("\n=== Results ===");
                                    System.out.println("Found " + results.size() + " stations in longitude range [" + minLon + ", " + maxLon + "]");

                                    if (!results.isEmpty()) {
                                        System.out.println("\nFirst 10 stations:");
                                        results.stream().limit(10).forEach(s ->
                                                System.out.printf("- %s (lon: %.6f)%n", s.getStationName(), s.getStationLongitude()));
                                    }

                                    System.out.println("\nTemporal Complexity: O(log n + k), where k = number of results");
                                }

                                case 0 -> System.out.println("Returning to ESINF Menu...");

                                default -> System.out.println("Invalid option.");
                            }
                        } while (queryOption != 0);

                    } catch (IOException e) {
                        System.out.println("Error loading stations: " + e.getMessage());
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                case 0 -> System.out.println("Returning to Main Menu...");
                default -> System.out.println("Invalid option.");
            }
        } while (op2 != 0);
    }
}