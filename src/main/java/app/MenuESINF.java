package app;

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
                    System.out.println(plan.generateSummaryReport());

                    System.out.println("\n=== DETAILED PICKING LIST ===");
                    System.out.println(plan.generateDetailedPickingList());
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

                case 0 -> System.out.println("Returning to Main Menu...");
                default -> System.out.println("Invalid option.");
            }
        } while (op2 != 0);
    }
}