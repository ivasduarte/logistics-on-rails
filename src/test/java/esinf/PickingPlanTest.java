package esinf;

import warehouseMngt.order.Allocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import warehouseMngt.picking.PickingPlan;
import warehouseMngt.picking.PackingHeuristic;
import warehouseMngt.picking.Trolley;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PickingPlanTest {

    private Allocation testAllocation1;
    private Allocation testAllocation2;
    private Allocation testAllocation3;
    private Allocation testAllocation4;

    @BeforeEach
    void setUp() {
        testAllocation1 = createMockAllocation("ORD001", "SKU001", 1);
        testAllocation2 = createMockAllocation("ORD002", "SKU002", 2);
        testAllocation3 = createMockAllocation("ORD003", "SKU003", 3);
        testAllocation4 = createMockAllocation("ORD004", "SKU004", 4);
    }

    @Test
    void constructorAndGetters() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: constructorAndGetters");
        System.out.println("=".repeat(60));

        PickingPlan plan = new PickingPlan(PackingHeuristic.FIRST_FIT, 100.0);

        System.out.println("\nPickingPlan toString(): " + plan);
        System.out.println("\nDetailed Info:");
        System.out.println("  Heuristic: " + plan.getHeuristic());
        System.out.println("  Trolley Capacity: " + plan.getTrolleyCapacity() + " kg");
        System.out.println("  Trolley Count: " + plan.getTrolleyCount());
        System.out.println("  Total Allocations: " + plan.getTotalAllocations());
        System.out.println("  Total Weight: " + plan.getTotalWeight() + " kg");
        System.out.println("  Skipped Count: " + plan.getSkippedCount());
        System.out.println("  Average Utilization: " + String.format("%.2f", plan.getAverageUtilization()) + "%");

        assertAll(
                () -> assertEquals(PackingHeuristic.FIRST_FIT, plan.getHeuristic()),
                () -> assertEquals(100.0, plan.getTrolleyCapacity(), 1e-6),
                () -> assertEquals(0, plan.getTrolleyCount()),
                () -> assertEquals(0, plan.getTotalAllocations()),
                () -> assertEquals(0.0, plan.getTotalWeight(), 1e-6),
                () -> assertEquals(0, plan.getSkippedCount()),
                () -> assertEquals(0.0, plan.getAverageUtilization(), 1e-6)
        );
    }

    @Test
    void addTrolleyUpdatesMetricsAndAverageUtilization() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: addTrolleyUpdatesMetricsAndAverageUtilization");
        System.out.println("=".repeat(60));

        PickingPlan plan = new PickingPlan(PackingHeuristic.BEST_FIT_DECREASING, 100.0);

        Trolley t = new Trolley(1, 50.0);
        System.out.println("\nTrolley created: " + t);

        boolean added = t.addAllocation(testAllocation1, 20.0);
        System.out.println("Allocation added: " + added);
        System.out.println("Trolley after allocation: " + t);
        System.out.println("\nTrolley Details:");
        System.out.println("  Number: " + t.getTrolleyNumber());
        System.out.println("  Capacity: " + t.getCapacityWeight() + " kg");
        System.out.println("  Used Weight: " + t.getUsedWeight() + " kg");
        System.out.println("  Remaining Capacity: " + t.getRemainingCapacity() + " kg");
        System.out.println("  Utilization: " + String.format("%.2f", t.getUtilization()) + "%");
        System.out.println("  Allocation Count: " + t.getAllocationCount());
        System.out.println("  Is Empty: " + t.isEmpty());

        double expectedUsed = t.getUsedWeight();
        int expectedAllocCount = t.getAllocationCount();
        double expectedUtil = t.getUtilization();

        plan.addTrolley(t);

        System.out.println("\nPickingPlan after adding trolley:");
        System.out.println("  toString(): " + plan);
        System.out.println("  Trolley Count: " + plan.getTrolleyCount());
        System.out.println("  Total Allocations: " + plan.getTotalAllocations());
        System.out.println("  Total Weight: " + plan.getTotalWeight() + " kg");
        System.out.println("  Average Utilization: " + String.format("%.2f", plan.getAverageUtilization()) + "%");

        assertAll(
                () -> assertEquals(1, plan.getTrolleyCount()),
                () -> assertEquals(expectedAllocCount, plan.getTotalAllocations()),
                () -> assertEquals(expectedUsed, plan.getTotalWeight(), 1e-6),
                () -> assertEquals(expectedUtil, plan.getAverageUtilization(), 1e-6)
        );
    }

    @Test
    void getTrolleysReturnsCopy() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: getTrolleysReturnsCopy");
        System.out.println("=".repeat(60));

        PickingPlan plan = new PickingPlan(PackingHeuristic.FIRST_FIT, 100.0);
        Trolley t = new Trolley(1, 10.0);
        System.out.println("\nCreated trolley: " + t);

        plan.addTrolley(t);
        System.out.println("Original plan trolley count: " + plan.getTrolleyCount());

        List<Trolley> copy = plan.getTrolleys();
        System.out.println("Copy list size: " + copy.size());

        int copySize = copy.size();
        copy.clear();
        System.out.println("After clearing copy, copy size: " + copy.size());
        System.out.println("After clearing copy, original plan trolley count: " + plan.getTrolleyCount());

        assertAll(
                () -> assertEquals(1, copySize, "Copy should have had 1 trolley before clearing"),
                () -> assertEquals(1, plan.getTrolleyCount(), "Original plan should still have 1 trolley after copy is cleared")
        );
    }

    @Test
    void setTrolleysRecalculatesMetrics() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: setTrolleysRecalculatesMetrics");
        System.out.println("=".repeat(60));

        PickingPlan plan = new PickingPlan(PackingHeuristic.FIRST_FIT_DECREASING, 100.0);

        Trolley t1 = new Trolley(1, 50.0);
        t1.addAllocation(testAllocation1, 10.0);
        System.out.println("\nTrolley 1: " + t1);
        System.out.println("  Allocations: " + t1.getAllocationCount());
        System.out.println("  Used Weight: " + t1.getUsedWeight() + " kg");

        Trolley t2 = new Trolley(2, 50.0);
        t2.addAllocation(testAllocation2, 5.0);
        t2.addAllocation(testAllocation3, 5.0);
        System.out.println("\nTrolley 2: " + t2);
        System.out.println("  Allocations: " + t2.getAllocationCount());
        System.out.println("  Used Weight: " + t2.getUsedWeight() + " kg");

        List<Trolley> list = new ArrayList<>();
        list.add(t1);
        list.add(t2);

        int expectedTotalAlloc = t1.getAllocationCount() + t2.getAllocationCount();
        double expectedTotalWeight = t1.getUsedWeight() + t2.getUsedWeight();

        plan.setTrolleys(list);

        System.out.println("\nPickingPlan after setTrolleys():");
        System.out.println("  toString(): " + plan);
        System.out.println("  Trolley Count: " + plan.getTrolleyCount());
        System.out.println("  Total Allocations: " + plan.getTotalAllocations());
        System.out.println("  Total Weight: " + plan.getTotalWeight() + " kg");
        System.out.println("  Average Utilization: " + String.format("%.2f", plan.getAverageUtilization()) + "%");

        assertAll(
                () -> assertEquals(2, plan.getTrolleyCount()),
                () -> assertEquals(expectedTotalAlloc, plan.getTotalAllocations()),
                () -> assertEquals(expectedTotalWeight, plan.getTotalWeight(), 1e-6)
        );
    }

    @Test
    void generateSummaryReportContainsEssentialInfo() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: generateSummaryReportContainsEssentialInfo");
        System.out.println("=".repeat(60));

        PickingPlan plan = new PickingPlan(PackingHeuristic.FIRST_FIT, 100.0);

        Trolley t = new Trolley(1, 50.0);
        t.addAllocation(testAllocation1, 20.0);
        System.out.println("\nTrolley created: " + t);

        plan.addTrolley(t);
        System.out.println("Plan: " + plan);

        String report = plan.generateSummaryReport();

        System.out.println("\n" + "-".repeat(60));
        System.out.println("SUMMARY REPORT OUTPUT:");
        System.out.println("-".repeat(60));
        System.out.println(report);
        System.out.println("-".repeat(60));

        assertAll(
                () -> assertTrue(report.contains("Picking Plan Summary"), "Should contain title"),
                () -> assertTrue(report.contains("Trolley Capacity: " + plan.getTrolleyCapacity() + " kg"), "Should contain capacity"),
                () -> assertTrue(report.contains("Trolley " + t.getTrolleyNumber()), "Should contain trolley number"),
                () -> assertTrue(report.contains(String.format("%.2f", t.getUtilization()) + "% full"), "Should contain utilization")
        );
    }

    @Test
    void generateDetailedPickingListBasicHeader() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: generateDetailedPickingListBasicHeader");
        System.out.println("=".repeat(60));

        PickingPlan plan = new PickingPlan(PackingHeuristic.BEST_FIT_DECREASING, 100.0);
        System.out.println("\nEmpty plan: " + plan);

        String detailed = plan.generateDetailedPickingList();

        System.out.println("\n" + "-".repeat(60));
        System.out.println("DETAILED PICKING LIST OUTPUT (Empty Plan):");
        System.out.println("-".repeat(60));
        System.out.println(detailed);
        System.out.println("-".repeat(60));

        assertTrue(detailed.contains("Picking Plan: " + PackingHeuristic.BEST_FIT_DECREASING));
    }

    @Test
    void generateDetailedPickingListWithAllocations() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: generateDetailedPickingListWithAllocations");
        System.out.println("=".repeat(60));

        PickingPlan plan = new PickingPlan(PackingHeuristic.FIRST_FIT, 100.0);

        Trolley t1 = new Trolley(1, 50.0);
        t1.addAllocation(testAllocation1, 15.0);
        t1.addAllocation(testAllocation2, 10.0);
        System.out.println("\nTrolley 1: " + t1);

        Trolley t2 = new Trolley(2, 50.0);
        t2.addAllocation(testAllocation3, 20.0);
        System.out.println("Trolley 2: " + t2);

        plan.addTrolley(t1);
        plan.addTrolley(t2);
        System.out.println("\nPlan: " + plan);

        String detailed = plan.generateDetailedPickingList();

        System.out.println("\n" + "-".repeat(60));
        System.out.println("DETAILED PICKING LIST OUTPUT (With Allocations):");
        System.out.println("-".repeat(60));
        System.out.println(detailed);
        System.out.println("-".repeat(60));

        assertAll(
                () -> assertTrue(detailed.contains("Trolley 1"), "Should contain Trolley 1"),
                () -> assertTrue(detailed.contains("Trolley 2"), "Should contain Trolley 2"),
                () -> assertTrue(detailed.contains("Order ID: ORD001"), "Should contain order ORD001")
        );
    }

    @Test
    void partialAllocationTracking() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: partialAllocationTracking");
        System.out.println("=".repeat(60));

        PickingPlan plan = new PickingPlan(PackingHeuristic.FIRST_FIT, 100.0);

        Trolley t = new Trolley(1, 50.0);

        System.out.println("\nAdding normal allocation...");
        boolean added1 = t.addAllocation(testAllocation1, 15.0, false, null);
        System.out.println("  Added: " + added1);

        System.out.println("\nAdding partial allocation (allocated 3 out of original 10 units)...");
        boolean added2 = t.addAllocation(testAllocation2, 10.0, true, 10);
        System.out.println("  Added: " + added2);

        System.out.println("\nTrolley: " + t);
        System.out.println("\nTrolley Logs:");
        List<String> logs = t.getLogs();
        if (logs.isEmpty()) {
            System.out.println("  (No logs)");
        } else {
            for (String log : logs) {
                System.out.println("  " + log);
            }
        }

        plan.addTrolley(t);

        String detailed = plan.generateDetailedPickingList();

        System.out.println("\n" + "-".repeat(60));
        System.out.println("DETAILED PICKING LIST (With Partial Allocation):");
        System.out.println("-".repeat(60));
        System.out.println(detailed);
        System.out.println("-".repeat(60));

        assertTrue(detailed.contains("PARTIAL"), "Should indicate partial allocation");
    }

    @Test
    void skippedAllocationsAppearInReports() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: skippedAllocationsAppearInReports");
        System.out.println("=".repeat(60));

        PickingPlan plan = new PickingPlan(PackingHeuristic.FIRST_FIT, 50.0);

        Trolley t = new Trolley(1, 50.0);
        boolean added = t.addAllocation(testAllocation1, 30.0);
        System.out.println("\nTrolley 1: " + t);
        System.out.println("Allocation added to trolley: " + added);

        plan.addTrolley(t);

        System.out.println("\nAdding skipped allocation (too heavy: 40.0 kg)...");
        System.out.println("Skipped allocation details:");
        System.out.println("  OrderID: " + testAllocation2.getOrderID());
        System.out.println("  BoxID: " + testAllocation2.getBoxID());
        System.out.println("  SKU: " + testAllocation2.getSku());

        plan.addSkippedAllocation(testAllocation2, 40.0);

        System.out.println("\nPlan with skipped allocation: " + plan);
        System.out.println("  Skipped Count: " + plan.getSkippedCount());
        System.out.println("  Skipped Allocations:");
        for (PickingPlan.SkippedAllocation sa : plan.getSkippedAllocations()) {
            System.out.println("    OrderID: " + sa.allocation.getOrderID());
            System.out.println("    BoxID: " + sa.allocation.getBoxID());
            System.out.println("    SKU: " + sa.allocation.getSku());
            System.out.println("    Weight: " + String.format("%.2f", sa.weight) + " kg");
        }

        String summary = plan.generateSummaryReport();
        String detailed = plan.generateDetailedPickingList();

        System.out.println("\n" + "-".repeat(60));
        System.out.println("SUMMARY REPORT (With Skipped):");
        System.out.println("-".repeat(60));
        System.out.println(summary);
        System.out.println("-".repeat(60));

        System.out.println("\n" + "-".repeat(60));
        System.out.println("DETAILED PICKING LIST (With Skipped):");
        System.out.println("-".repeat(60));
        System.out.println(detailed);
        System.out.println("-".repeat(60));

        assertAll(
                () -> assertEquals(1, plan.getSkippedCount(), "Should have 1 skipped allocation"),
                () -> assertTrue(summary.contains("Skipped Allocations"), "Summary should mention skipped allocations"),
                () -> assertTrue(summary.contains(testAllocation2.getSku()), "Summary should contain the SKU"),
                () -> assertTrue(summary.contains(testAllocation2.getBoxID()), "Summary should contain the BoxID"),
                () -> assertTrue(detailed.contains("Skipped Allocations"), "Detailed list should mention skipped allocations"),
                () -> assertTrue(detailed.contains("40,00") || detailed.contains("40.00"),
                        "Detailed list should show weight 40.00 or 40,00 (locale dependent)")
        );
    }

    @Test
    void multipleUtilizationScenarios() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: multipleUtilizationScenarios");
        System.out.println("=".repeat(60));

        PickingPlan plan = new PickingPlan(PackingHeuristic.BEST_FIT_DECREASING, 100.0);

        Trolley t1 = new Trolley(1, 100.0);
        t1.addAllocation(testAllocation1, 20.0);
        System.out.println("\nTrolley 1 (Low util): " + t1);

        Trolley t2 = new Trolley(2, 100.0);
        t2.addAllocation(testAllocation2, 50.0);
        System.out.println("Trolley 2 (Medium util): " + t2);

        Trolley t3 = new Trolley(3, 100.0);
        t3.addAllocation(testAllocation3, 40.0);
        t3.addAllocation(testAllocation4, 45.0);
        System.out.println("Trolley 3 (High util): " + t3);

        plan.addTrolley(t1);
        plan.addTrolley(t2);
        plan.addTrolley(t3);

        double expectedAvg = (t1.getUtilization() + t2.getUtilization() + t3.getUtilization()) / 3.0;

        System.out.println("\nPlan: " + plan);
        System.out.println("Average Utilization: " + String.format("%.2f", plan.getAverageUtilization()) + "%");
        System.out.println("Expected Average: " + String.format("%.2f", expectedAvg) + "%");

        String summary = plan.generateSummaryReport();

        System.out.println("\n" + "-".repeat(60));
        System.out.println("SUMMARY REPORT (Multiple Utilizations):");
        System.out.println("-".repeat(60));
        System.out.println(summary);
        System.out.println("-".repeat(60));

        assertAll(
                () -> assertTrue(plan.getAverageUtilization() > 0, "Average utilization should be positive"),
                () -> assertEquals(3, plan.getTrolleyCount(), "Should have 3 trolleys"),
                () -> assertEquals(expectedAvg, plan.getAverageUtilization(), 0.01, "Average utilization should match calculated average")
        );
    }

    private Allocation createMockAllocation(String orderID, String sku, int lineNo) {
        try {
            return new Allocation(
                    orderID,
                    "BOX-" + orderID,
                    "WH001",
                    sku,
                    lineNo,
                    1,
                    lineNo,
                    10
            );
        } catch (Exception e) {
            System.err.println("Error creating mock allocation: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}