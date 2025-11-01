package app;

import domain.order.Allocation;
import domain.order.Order;
import domain.order.OrderAllocationResult;
import domain.picking.*;
import domain.returns.Returns;
import domain.returns.ReturnsManager;
import domain.service.LoadData;
import domain.warehouse.Movement;
import domain.warehouse.Warehouse;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;

public class MenuESINF {
    private static OrderAllocationResult lastAllocationResult = null;
    public static Scanner sc = new Scanner(System.in);
    private static PickingPlan lastPlan = null;

    public static void menuESINF() {
        Warehouse warehouse = new Warehouse(
                LoadData.getWagons(),
                LoadData.getItems(),
                LoadData.getOrderLines(),
                LoadData.getOrders(),
                LoadData.getReturns(),
                LoadData.getWarehouses()
        );

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
                    warehouse.unloadWagonsIntoBays();

                    System.out.println("\nUnloading complete!");
                    warehouse.printMovementHistory();

                    if (!LoadData.getImportErrors().isEmpty()) {
                        System.out.println("\nImport Errors:");
                        LoadData.getImportErrors().forEach(System.out::println);
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
                        System.out.println("\n Processing Order ID: " + orderId);
                        OrderAllocationResult result = warehouse.allocateOrder(orderId, false);

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
                    String capacityInput = sc.nextLine().trim();
                    double capacity = Double.parseDouble(capacityInput);

                    System.out.println("\nSelect packing heuristic:");
                    System.out.println("1. FIRST_FIT");
                    System.out.println("2. FIRST_FIT_DECREASING");
                    System.out.println("3. BEST_FIT_DECREASING");
                    System.out.print("Option: ");
                    String hInput = sc.nextLine().trim();
                    int h = Integer.parseInt(hInput);

                    PackingHeuristic heuristic = switch (h) {
                        case 1 -> PackingHeuristic.FIRST_FIT;
                        case 2 -> PackingHeuristic.FIRST_FIT_DECREASING;
                        case 3 -> PackingHeuristic.BEST_FIT_DECREASING;
                        default -> throw new IllegalStateException("Unexpected value: " + h);
                    };

                    // Creates picking plan
                    PickingPlan plan = planService.createPickingPlan(
                            lastAllocationResult.getAllAllocations(),
                            capacity,
                            heuristic
                    );

                    lastPlan = plan;

                    // Shows report summary
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

                    PickingPath pickingPath = new PickingPath(lastPlan);

                    System.out.println("\nSelect path optimization strategy:");
                    System.out.println("1. Ascending Order Strategy");
                    System.out.println("2. Nearest Neighbor Strategy");
                    System.out.print("Option: ");
                    int pathOption = sc.nextInt();
                    sc.nextLine();

                    int trolleyCounter = 1;

                    for (Trolley trolley : pickingPath.getTrolleys()) {
                        System.out.println("\n=== Trolley #" + trolleyCounter++ + " ===");
                        List<Allocation> allocations = pickingPath.getTrolleyAllocations(trolley);

                        if (allocations.isEmpty()) {
                            System.out.println("No allocations for this trolley.");
                            continue;
                        }

                        int totalDistance;
                        if (pathOption == 1) {
                            totalDistance = pickingPath.minimizeDistanceAscending(allocations);
                        } else {
                            totalDistance = pickingPath.minimizeDistanceNearest(allocations);
                        }

                        System.out.println("Total Distance for this Trolley: " + totalDistance + " units");
                    }
                }

                case 5 -> {
                    System.out.println("\nExecuting USEI05 - Returns & Quarantine...");

                    // Check if there are any loaded returns
                    Map<String, Returns> returns = LoadData.getReturns();
                    if(returns == null || returns.isEmpty()){
                        System.out.println("No returns have been found in the system.");
                        break;
                    }

                    System.out.println("\nFound " + returns.size() + " returns to process.");

                    String logPath = "audit-log-file/";
                    ReturnsManager returnsManager = new ReturnsManager(warehouse, logPath);

                    try{
                        List<Returns> returnsList = new ArrayList<>(returns.values());

                        List<ReturnsManager.ReturnProcessingResult> results = returnsManager.processBatchReturns(returnsList);

                        for(ReturnsManager.ReturnProcessingResult result : results){
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
        } while(op2 != 0);
    }
}