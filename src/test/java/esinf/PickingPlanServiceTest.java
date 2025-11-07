package esinf;

import warehouseMngt.order.Allocation;
import warehouseMngt.picking.PickingPlanService;
import warehouseMngt.picking.PackingHeuristic;
import warehouseMngt.picking.PickingPlan;
import warehouseMngt.picking.Trolley;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PickingPlanServiceTest {

    private Map<String, Double> standardWeights;
    private Map<String, Double> heavyWeights;

    @BeforeEach
    void setUp() {
        standardWeights = new HashMap<>();
        standardWeights.put("SKU_A", 2.0);
        standardWeights.put("SKU_B", 3.0);
        standardWeights.put("SKU_C", 5.0);
        standardWeights.put("SKU_D", 1.5);
        standardWeights.put("SKU_SPLIT", 4.0);

        heavyWeights = new HashMap<>();
        heavyWeights.put("HEAVY", 15.0);
    }

    private Allocation createAllocation(String orderID, String sku, int qty, int lineNo) {
        return new Allocation(
                orderID,
                "BOX-" + orderID + "-" + lineNo,
                "WH001",
                sku,
                lineNo,
                1,
                lineNo,
                qty
        );
    }

    @Test
    void constructorRejectsNullOrEmptyItemWeights() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: constructorRejectsNullOrEmptyItemWeights");
        System.out.println("=".repeat(60));

        System.out.println("\nTesting null item weights...");
        assertThrows(IllegalArgumentException.class, () -> new PickingPlanService(null),
                "Should reject null item weights");

        System.out.println("Testing empty item weights...");
        assertThrows(IllegalArgumentException.class, () -> new PickingPlanService(new HashMap<>()),
                "Should reject empty item weights");

        System.out.println("✓ Constructor correctly rejects invalid inputs");
    }

    @Test
    void createPickingPlanRejectsInvalidInputs() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: createPickingPlanRejectsInvalidInputs");
        System.out.println("=".repeat(60));

        PickingPlanService svc = new PickingPlanService(standardWeights);
        Allocation validAlloc = createAllocation("ORD001", "SKU_A", 1, 1);

        System.out.println("\nTesting null allocations...");
        assertThrows(IllegalArgumentException.class,
                () -> svc.createPickingPlan(null, 10.0, PackingHeuristic.FIRST_FIT),
                "Should reject null allocations");

        System.out.println("Testing empty allocations...");
        assertThrows(IllegalArgumentException.class,
                () -> svc.createPickingPlan(new ArrayList<>(), 10.0, PackingHeuristic.FIRST_FIT),
                "Should reject empty allocations");

        System.out.println("Testing zero capacity...");
        assertThrows(IllegalArgumentException.class,
                () -> svc.createPickingPlan(List.of(validAlloc), 0.0, PackingHeuristic.FIRST_FIT),
                "Should reject zero capacity");

        System.out.println("Testing negative capacity...");
        assertThrows(IllegalArgumentException.class,
                () -> svc.createPickingPlan(List.of(validAlloc), -5.0, PackingHeuristic.FIRST_FIT),
                "Should reject negative capacity");

        System.out.println("✓ All invalid inputs correctly rejected");
    }

    @Test
    void missingSkuWeightThrowsException() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: missingSkuWeightThrowsException");
        System.out.println("=".repeat(60));

        Map<String, Double> weights = Map.of("SKU_A", 2.0);
        PickingPlanService svc = new PickingPlanService(weights);

        System.out.println("\nAvailable SKUs in weight map: " + weights.keySet());
        System.out.println("Attempting to create plan with unknown SKU: SKU_MISSING");

        Allocation unknownSkuAlloc = createAllocation("ORD001", "SKU_MISSING", 1, 1);
        List<Allocation> allocations = List.of(unknownSkuAlloc);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT),
                "Should throw when SKU weight is not found");

        System.out.println("\nException message: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("No weight found for SKU"),
                "Exception should mention missing SKU weight");
        System.out.println("✓ Missing SKU weight correctly detected");
    }

    @Test
    void singleUnitExceedingCapacityThrowsException() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: singleUnitExceedingCapacityThrowsException");
        System.out.println("=".repeat(60));

        PickingPlanService svc = new PickingPlanService(heavyWeights);

        System.out.println("\nSKU: HEAVY, Unit weight: 15.0 kg");
        System.out.println("Trolley capacity: 10.0 kg");
        System.out.println("Single unit exceeds capacity - should throw exception");

        Allocation heavyAlloc = createAllocation("ORD001", "HEAVY", 1, 1);
        List<Allocation> allocations = List.of(heavyAlloc);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT),
                "Should throw when single unit exceeds trolley capacity");

        System.out.println("\nException message: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("weighs") && ex.getMessage().contains("exceeds"),
                "Exception should explain weight exceeds capacity");
        System.out.println("✓ Single unit capacity validation works correctly");
    }

    @Test
    void firstFit_basicPacking_noSplittingNeeded() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: firstFit_basicPacking_noSplittingNeeded");
        System.out.println("=".repeat(60));

        PickingPlanService svc = new PickingPlanService(standardWeights);

        List<Allocation> allocations = Arrays.asList(
                createAllocation("ORD001", "SKU_A", 2, 1),  // 2*2.0 = 4.0 kg
                createAllocation("ORD002", "SKU_B", 1, 1)   // 1*3.0 = 3.0 kg
        );

        System.out.println("\nAllocations:");
        for (Allocation a : allocations) {
            double weight = standardWeights.get(a.getSku()) * a.getQuantityAllocated();
            System.out.println("  " + a.getSku() + " x" + a.getQuantityAllocated() +
                    " = " + weight + " kg");
        }
        System.out.println("Total weight: 7.0 kg");
        System.out.println("Trolley capacity: 10.0 kg");

        PickingPlan plan = svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT);

        System.out.println("\n" + "-".repeat(60));
        System.out.println("PLAN RESULTS:");
        System.out.println("-".repeat(60));
        System.out.println("Plan: " + plan);
        System.out.println("\nSummary Report:");
        System.out.println(plan.generateSummaryReport());
        System.out.println("-".repeat(60));

        assertAll(
                () -> assertEquals(PackingHeuristic.FIRST_FIT, plan.getHeuristic()),
                () -> assertEquals(1, plan.getTrolleyCount(), "Should fit in one trolley"),
                () -> assertEquals(0, plan.getSkippedCount(), "Should have no skipped allocations"),
                () -> assertEquals(7.0, plan.getTotalWeight(), 1e-6, "Total weight should be 7.0 kg")
        );
    }

    @Test
    void firstFit_withSplitting_splitsAllocationAcrossTrolleys() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: firstFit_withSplitting_splitsAllocationAcrossTrolleys");
        System.out.println("=".repeat(60));

        PickingPlanService svc = new PickingPlanService(standardWeights, true);

        Allocation allocation = createAllocation("ORD001", "SKU_SPLIT", 3, 1);
        List<Allocation> allocations = List.of(allocation);

        System.out.println("\nAllocation to pack:");
        System.out.println("  SKU: " + allocation.getSku());
        System.out.println("  Quantity: " + allocation.getQuantityAllocated());
        System.out.println("  Unit weight: 4.0 kg");
        System.out.println("  Total weight: 12.0 kg");
        System.out.println("  Trolley capacity: 10.0 kg");
        System.out.println("  Splitting: ENABLED");
        System.out.println("\nExpected: Split across 2 trolleys");

        PickingPlan plan = svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT);

        System.out.println("\n" + "-".repeat(60));
        System.out.println("PLAN RESULTS:");
        System.out.println("-".repeat(60));
        System.out.println("Plan: " + plan);
        System.out.println("\nDetailed Picking List:");
        System.out.println(plan.generateDetailedPickingList());
        System.out.println("-".repeat(60));

        assertAll(
                () -> assertEquals(2, plan.getTrolleyCount(), "Should use 2 trolleys"),
                () -> assertEquals(0, plan.getSkippedCount(), "Should have no skipped allocations"),
                () -> assertEquals(12.0, plan.getTotalWeight(), 1e-6, "Total weight should be 12.0 kg")
        );
    }

    @Test
    void firstFit_withoutSplitting_skipsOversizedAllocation() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: firstFit_withoutSplitting_skipsOversizedAllocation");
        System.out.println("=".repeat(60));

        PickingPlanService svc = new PickingPlanService(standardWeights, false);

        Allocation allocation = createAllocation("ORD001", "SKU_SPLIT", 3, 1);
        List<Allocation> allocations = List.of(allocation);

        System.out.println("\nAllocation to pack:");
        System.out.println("  SKU: " + allocation.getSku());
        System.out.println("  Total weight: 12.0 kg");
        System.out.println("  Trolley capacity: 10.0 kg");
        System.out.println("  Splitting: DISABLED");
        System.out.println("\nExpected: Allocation skipped");

        PickingPlan plan = svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT);

        System.out.println("\n" + "-".repeat(60));
        System.out.println("PLAN RESULTS:");
        System.out.println("-".repeat(60));
        System.out.println("Plan: " + plan);
        System.out.println("\nSummary Report:");
        System.out.println(plan.generateSummaryReport());
        System.out.println("-".repeat(60));

        assertAll(
                () -> assertEquals(0, plan.getTrolleyCount(), "Should have no trolleys"),
                () -> assertEquals(1, plan.getSkippedCount(), "Should have 1 skipped allocation")
        );
    }

    @Test
    void firstFitDecreasing_sortsAndPacks() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: firstFitDecreasing_sortsAndPacks");
        System.out.println("=".repeat(60));

        PickingPlanService svc = new PickingPlanService(standardWeights);

        List<Allocation> allocations = Arrays.asList(
                createAllocation("ORD001", "SKU_A", 1, 1),
                createAllocation("ORD002", "SKU_C", 1, 2),
                createAllocation("ORD003", "SKU_D", 1, 3),
                createAllocation("ORD004", "SKU_B", 1, 4)
        );

        System.out.println("\nAllocations (input order):");
        for (Allocation a : allocations) {
            double weight = standardWeights.get(a.getSku()) * a.getQuantityAllocated();
            System.out.println("  " + a.getSku() + ": " + weight + " kg");
        }
        System.out.println("\nFFD will sort by weight (descending): SKU_C(5.0), SKU_B(3.0), SKU_A(2.0), SKU_D(1.5)");
        System.out.println("Total weight: 11.5 kg");
        System.out.println("Trolley capacity: 10.0 kg");

        PickingPlan plan = svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT_DECREASING);

        System.out.println("\n" + "-".repeat(60));
        System.out.println("PLAN RESULTS:");
        System.out.println("-".repeat(60));
        System.out.println("Plan: " + plan);
        System.out.println("\nDetailed Picking List:");
        System.out.println(plan.generateDetailedPickingList());
        System.out.println("-".repeat(60));

        assertAll(
                () -> assertEquals(PackingHeuristic.FIRST_FIT_DECREASING, plan.getHeuristic()),
                () -> assertEquals(2, plan.getTrolleyCount(), "Should use 2 trolleys"),
                () -> assertEquals(11.5, plan.getTotalWeight(), 1e-6, "Total weight should be 11.5 kg")
        );
    }

    @Test
    void bestFitDecreasing_findsTightestFit() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: bestFitDecreasing_findsTightestFit");
        System.out.println("=".repeat(60));

        PickingPlanService svc = new PickingPlanService(standardWeights);

        List<Allocation> allocations = Arrays.asList(
                createAllocation("ORD001", "SKU_C", 1, 1),
                createAllocation("ORD002", "SKU_B", 1, 2),
                createAllocation("ORD003", "SKU_A", 1, 3),
                createAllocation("ORD004", "SKU_D", 1, 4)
        );

        System.out.println("\nAllocations:");
        for (Allocation a : allocations) {
            double weight = standardWeights.get(a.getSku()) * a.getQuantityAllocated();
            System.out.println("  " + a.getSku() + ": " + weight + " kg");
        }
        System.out.println("Total weight: 11.5 kg");
        System.out.println("Trolley capacity: 10.0 kg");
        System.out.println("\nBFD will pack items to minimize wasted space");

        PickingPlan plan = svc.createPickingPlan(allocations, 10.0, PackingHeuristic.BEST_FIT_DECREASING);

        System.out.println("\n" + "-".repeat(60));
        System.out.println("PLAN RESULTS:");
        System.out.println("-".repeat(60));
        System.out.println("Plan: " + plan);
        System.out.println("\nSummary Report:");
        System.out.println(plan.generateSummaryReport());
        System.out.println("-".repeat(60));

        assertAll(
                () -> assertEquals(PackingHeuristic.BEST_FIT_DECREASING, plan.getHeuristic()),
                () -> assertEquals(2, plan.getTrolleyCount(), "Should use 2 trolleys"),
                () -> assertEquals(11.5, plan.getTotalWeight(), 1e-6, "Total weight should be 11.5 kg")
        );

        System.out.println("\nAverage Utilization: " + String.format("%.2f", plan.getAverageUtilization()) + "%");
    }

    @Test
    void compareHeuristics_producesResultsForAllThree() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: compareHeuristics_producesResultsForAllThree");
        System.out.println("=".repeat(60));

        PickingPlanService svc = new PickingPlanService(standardWeights);

        List<Allocation> allocations = Arrays.asList(
                createAllocation("ORD001", "SKU_A", 2, 1),
                createAllocation("ORD002", "SKU_B", 1, 2),
                createAllocation("ORD003", "SKU_C", 1, 3)
        );

        System.out.println("\nAllocations:");
        for (Allocation a : allocations) {
            double weight = standardWeights.get(a.getSku()) * a.getQuantityAllocated();
            System.out.println("  " + a.getSku() + " x" + a.getQuantityAllocated() +
                    " = " + weight + " kg");
        }
        System.out.println("Trolley capacity: 15.0 kg");

        Map<PackingHeuristic, PickingPlan> results = svc.compareHeuristics(allocations, 15.0);

        System.out.println("\n" + "-".repeat(60));
        System.out.println("COMPARISON RESULTS:");
        System.out.println("-".repeat(60));

        for (Map.Entry<PackingHeuristic, PickingPlan> entry : results.entrySet()) {
            PickingPlan plan = entry.getValue();
            System.out.println("\n" + entry.getKey() + ":");
            System.out.println("  Trolleys: " + plan.getTrolleyCount());
            System.out.println("  Avg Utilization: " + String.format("%.2f", plan.getAverageUtilization()) + "%");
            System.out.println("  Skipped: " + plan.getSkippedCount());
        }

        System.out.println("\n" + "-".repeat(60));

        assertAll(
                () -> assertEquals(3, results.size(), "Should have results for all 3 heuristics"),
                () -> assertTrue(results.containsKey(PackingHeuristic.FIRST_FIT)),
                () -> assertTrue(results.containsKey(PackingHeuristic.FIRST_FIT_DECREASING)),
                () -> assertTrue(results.containsKey(PackingHeuristic.BEST_FIT_DECREASING))
        );

        for (PickingPlan plan : results.values()) {
            assertNotNull(plan, "Each plan should not be null");
        }
    }

    @Test
    void generateComparisonReport_containsAllRequiredInfo() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: generateComparisonReport_containsAllRequiredInfo");
        System.out.println("=".repeat(60));

        PickingPlanService svc = new PickingPlanService(standardWeights);

        List<Allocation> allocations = Arrays.asList(
                createAllocation("ORD001", "SKU_A", 3, 1),
                createAllocation("ORD002", "SKU_B", 2, 2),
                createAllocation("ORD003", "SKU_C", 1, 3)
        );

        String report = svc.generateComparisonReport(allocations, 10.0);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("FULL COMPARISON REPORT:");
        System.out.println("=".repeat(60));
        System.out.println(report);
        System.out.println("=".repeat(60));
        assertAll(
                () -> assertTrue(report.contains("Packing Heuristics Comparison"),
                        "Report should have title"),
                () -> assertTrue(report.contains("Total Number of Trolleys"),
                        "Report should show total trolleys (acceptance criteria)"),
                () -> assertTrue(report.contains("Trolley Utilization"),
                        "Report should show utilization (acceptance criteria)"),
                () -> assertTrue(report.contains("% full"),
                        "Report should show percentage utilization")
        );
        for (PackingHeuristic h : PackingHeuristic.values()) {
            assertTrue(report.contains(h.toString()),
                    "Report should contain " + h);
        }
    }

    @Test
    void realWorldScenario_multipleOrders_complexPacking() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: realWorldScenario_multipleOrders_complexPacking");
        System.out.println("=".repeat(60));

        PickingPlanService svc = new PickingPlanService(standardWeights);

        List<Allocation> allocations = Arrays.asList(
                createAllocation("ORD001", "SKU_A", 5, 1),  // 10.0 kg
                createAllocation("ORD001", "SKU_D", 3, 2),  // 4.5 kg
                createAllocation("ORD002", "SKU_C", 2, 1),  // 10.0 kg
                createAllocation("ORD002", "SKU_B", 2, 2),  // 6.0 kg
                createAllocation("ORD003", "SKU_A", 1, 1),  // 2.0 kg
                createAllocation("ORD003", "SKU_D", 2, 2)   // 3.0 kg
        );

        System.out.println("Total allocations: " + allocations.size());
        double totalWeight = 0;
        for (Allocation a : allocations) {
            double weight = standardWeights.get(a.getSku()) * a.getQuantityAllocated();
            totalWeight += weight;
            System.out.println("  Order " + a.getOrderID() + ", Line " + a.getLineNo() +
                    ": " + a.getSku() + " x" + a.getQuantityAllocated() +
                    " = " + weight + " kg");
        }
        System.out.println("Total weight: " + totalWeight + " kg");
        System.out.println("Trolley capacity: 20.0 kg");

        System.out.println("\n" + "=".repeat(60));
        System.out.println("TESTING ALL HEURISTICS:");
        System.out.println("=".repeat(60));

        for (PackingHeuristic heuristic : PackingHeuristic.values()) {
            System.out.println("\n--- " + heuristic + " ---");
            PickingPlan plan = svc.createPickingPlan(allocations, 20.0, heuristic);

            System.out.println("Plan: " + plan);
            System.out.println("\nTrolley breakdown:");
            for (Trolley trolley : plan.getTrolleys()) {
                System.out.println("  " + trolley);
            }

            assertEquals(heuristic, plan.getHeuristic());
            assertTrue(plan.getTrolleyCount() >= 2, "Should need at least 2 trolleys");
            assertEquals(totalWeight, plan.getTotalWeight(), 1e-6, "Total weight should match");
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("FINAL COMPARISON REPORT:");
        System.out.println("=".repeat(60));
        System.out.println(svc.generateComparisonReport(allocations, 20.0));
        System.out.println("=".repeat(60));
    }

    @Test
    void allHeuristics_sameTotalWeight_differentEfficiency() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: allHeuristics_sameTotalWeight_differentEfficiency");
        System.out.println("=".repeat(60));

        PickingPlanService svc = new PickingPlanService(standardWeights);

        List<Allocation> allocations = Arrays.asList(
                createAllocation("ORD001", "SKU_A", 2, 1),
                createAllocation("ORD002", "SKU_B", 1, 2)
        );

        double expectedTotalWeight = 7.0;
        System.out.println("\nExpected total weight: " + expectedTotalWeight + " kg");

        for (PackingHeuristic heuristic : PackingHeuristic.values()) {
            System.out.println("\nTesting: " + heuristic);
            PickingPlan plan = svc.createPickingPlan(allocations, 10.0, heuristic);

            System.out.println("  Result: " + plan);

            assertAll(
                    () -> assertEquals(heuristic, plan.getHeuristic()),
                    () -> assertEquals(expectedTotalWeight, plan.getTotalWeight(), 1e-6,
                            "All heuristics should preserve total weight"),
                    () -> assertEquals(0, plan.getSkippedCount(),
                            "No allocations should be skipped")
            );
        }
    }
}