package ui;

import utils.LoadData;
import warehouseMngt.controller.ReturnsController;
import warehouseMngt.controller.WarehouseController;
import warehouseMngt.domain.Warehouse;
import warehouseMngt.order.Allocation;
import warehouseMngt.order.Order;
import warehouseMngt.order.OrderAllocationResult;
import warehouseMngt.picking.*;
import warehouseMngt.returns.Returns;

import java.io.IOException;
import java.util.*;

public class WarehouseUI implements Runnable {
    private final Scanner sc = new Scanner(System.in);
    private final Warehouse warehouse;
    private final WarehouseController warehouseController;
    private static OrderAllocationResult lastAllocationResult = null;
    private static PickingPlan lastPlan = null;

    public WarehouseUI(WarehouseController warehouseController) {
        this.warehouse = new Warehouse(
                LoadData.getWagons(),
                LoadData.getItems(),
                LoadData.getOrderLines(),
                LoadData.getOrders(),
                LoadData.getReturns(),
                LoadData.getWarehouses()
        );

        this.warehouseController = new WarehouseController(this.warehouse);

    }


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
                    wagonsUnloading();
                    break;
                case 2:
                    orderAllocation();
                    break;
                case 3:
                    pickingPlan();
                    break;
                case 4:
                    pickPath();
                    break;
                case 5:
                    returns();
                    break;
                case 0:
                    exit = true;
            }
        }
    }

    private void showMenu() {
        System.out.println("\n══════════════════════ WAREHOUSE MENU ══════════════════════");

        System.out.println("1. Wagons Unloading (FEFO/FIFO)");
        System.out.println("2. Order Eligibility & Allocation");
        System.out.println("3. Picking Plan (Packing Heuristics)");
        System.out.println("4. Pick Path Sequencing");
        System.out.println("5. Returns & Quarantine");
        System.out.println("0. Exit");
        System.out.println("══════════════════════════════════════════════════════════════");
    }

    private void wagonsUnloading() {
        System.out.println("Executing USEI01: Wagons Unloading...");
        warehouseController.unloadWagonsIntoBays();

        System.out.println("\nUnloading complete!");
        warehouseController.printMovementHistory();

        if (!warehouseController.getImportErrors().isEmpty()) {
            System.out.println("\nImport Errors:");
            warehouseController.getImportErrors().forEach(System.out::println);
        }
    }

    private void orderAllocation() {
        System.out.println("Executing USEI02 - Order Eligibility & Allocation...");

        Map<String, Order> openOrders = LoadData.getOrders();
        if (openOrders.isEmpty()) {
            System.out.println("No open orders have been found.");
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

    private void pickingPlan() {
        System.out.println("\nExecuting USEI03 - Picking Plan (Packing Heuristics)...");

        if (lastAllocationResult == null || lastAllocationResult.getAllAllocations().isEmpty()) {
            System.out.println("No allocations found! Please run USEI02 first.");
            return;
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
        printPickingPlan(lastPlan);
    }

    private void printPickingPlan(PickingPlan plan) {
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

    private void pickPath() {
        System.out.println("Executing USEI04 - Picking Path Optimization...");

        if (lastPlan == null || lastPlan.getTrolleys().isEmpty()) {
            System.out.println("No picking plan found! Please run USEI03 first.");
            return;
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

    private void returns() {
        System.out.println("\nExecuting USEI05 - Returns & Quarantine...");

        Map<String, Returns> returns = LoadData.getReturns();
        if (returns == null || returns.isEmpty()) {
            System.out.println("No returns have been found in the system.");
            return;
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
}