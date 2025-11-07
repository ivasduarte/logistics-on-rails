package esinf;

import warehouseMngt.order.Allocation;
import warehouseMngt.picking.PickingPlanService;
import warehouseMngt.picking.PackingHeuristic;
import warehouseMngt.picking.PickingPlan;
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

    private PickingPlan buildExpectedPlan(PackingHeuristic heuristic, double capacity) {
        return new PickingPlan(heuristic, capacity);
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

        Map<PackingHeuristic, PickingPlan> results = svc.compareHeuristics(allocations, 15.0);

        System.out.println("\nComparison results:");
        for (Map.Entry<PackingHeuristic, PickingPlan> entry : results.entrySet()) {
            System.out.println("\n" + entry.getKey() + ":");
            System.out.println(entry.getValue());
        }

        assertEquals(3, results.size(), "Should have 3 heuristic results");
        assertTrue(results.containsKey(PackingHeuristic.FIRST_FIT));
        assertTrue(results.containsKey(PackingHeuristic.FIRST_FIT_DECREASING));
        assertTrue(results.containsKey(PackingHeuristic.BEST_FIT_DECREASING));

        for (PickingPlan plan : results.values()) {
            assertNotNull(plan);
        }

        System.out.println("\n✓ All heuristics produced valid plans!");
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
        System.out.println("COMPARISON REPORT:");
        System.out.println("=".repeat(60));
        System.out.println(report);
        System.out.println("=".repeat(60));

        assertTrue(report.contains("Packing Heuristics Comparison"));
        assertTrue(report.contains("Total Number of Trolleys"));
        assertTrue(report.contains("Trolley Utilization"));
        assertTrue(report.contains("% full"));

        for (PackingHeuristic h : PackingHeuristic.values()) {
            assertTrue(report.contains(h.toString()));
        }

        System.out.println("\n✓ Report contains all required information!");
    }
}