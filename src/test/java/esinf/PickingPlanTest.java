package esinf;

import esinf.us02.Allocation;
import org.junit.jupiter.api.Test;
import esinf.us03.PickingPlan;
import esinf.us03.Trolley;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PickingPlanTest {

    @Test
    void constructorAndGetters() {
        PickingPlan plan = new PickingPlan(null, 100.0);

        assertAll(
                () -> assertNull(plan.getHeuristic()),
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
        PickingPlan plan = new PickingPlan(null, 100.0);

        Trolley t = new Trolley(1, 50.0);
        t.addAllocation((Allocation) null, 20.0);

        double expectedUsed = t.getUsedWeight();
        int expectedAllocCount = t.getAllocationCount();
        double expectedUtil = t.getUtilization();

        plan.addTrolley(t);

        assertAll(
                () -> assertEquals(1, plan.getTrolleyCount()),
                () -> assertEquals(expectedAllocCount, plan.getTotalAllocations()),
                () -> assertEquals(expectedUsed, plan.getTotalWeight(), 1e-6),
                () -> assertEquals(expectedUtil, plan.getAverageUtilization(), 1e-6)
        );
    }

    @Test
    void getTrolleysReturnsCopy() {
        PickingPlan plan = new PickingPlan(null, 100.0);
        Trolley t = new Trolley(1, 10.0);
        plan.addTrolley(t);

        List<Trolley> copy = plan.getTrolleys();
        assertEquals(1, copy.size());

        copy.clear();
        assertEquals(1, plan.getTrolleyCount());
    }

    @Test
    void setTrolleysRecalculatesMetrics() {
        PickingPlan plan = new PickingPlan(null, 100.0);

        Trolley t1 = new Trolley(1, 50.0);
        t1.addAllocation((Allocation) null, 10.0);

        Trolley t2 = new Trolley(2, 50.0);
        t2.addAllocation((Allocation) null, 5.0);
        t2.addAllocation((Allocation) null, 5.0);

        List<Trolley> list = new ArrayList<>();
        list.add(t1);
        list.add(t2);

        plan.setTrolleys(list);

        assertAll(
                () -> assertEquals(2, plan.getTrolleyCount()),
                () -> assertEquals(t1.getAllocationCount() + t2.getAllocationCount(), plan.getTotalAllocations()),
                () -> assertEquals(t1.getUsedWeight() + t2.getUsedWeight(), plan.getTotalWeight(), 1e-6)
        );
    }

    @Test
    void generateSummaryReportContainsEssentialInfo() {
        PickingPlan plan = new PickingPlan(null, 100.0);

        Trolley t = new Trolley(1, 50.0);
        t.addAllocation((Allocation) null, 20.0);
        plan.addTrolley(t);

        String report = plan.generateSummaryReport();

        assertAll(
                () -> assertTrue(report.contains("Picking Plan Summary")),
                () -> assertTrue(report.contains("Trolley Capacity: " + plan.getTrolleyCapacity() + " kg")),
                () -> assertTrue(report.contains("Trolley " + t.getTrolleyNumber())),
                () -> assertTrue(report.contains(String.format("%.2f", t.getUtilization()) + "% full"))
        );
    }

    @Test
    void generateDetailedPickingListBasicHeader() {
        PickingPlan plan = new PickingPlan(null, 100.0);

        String detailed = plan.generateDetailedPickingList();
        assertTrue(detailed.contains("Picking Plan: null"));
    }
}
