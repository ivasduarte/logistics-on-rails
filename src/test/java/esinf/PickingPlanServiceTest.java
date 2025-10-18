// java
package esinf;

import esinf.us02.Allocation;
import esinf.us03.PickingPlanService;
import esinf.us03.PackingHeuristic;
import esinf.us03.PickingPlan;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PickingPlanServiceTest {

    private Allocation alloc(String sku, int qty) {
        return new Allocation("order1", "box1", "wh1", sku, 1, 1, 2, qty);
    }

    @Test
    void constructorRejectsNullOrEmptyItemWeights() {
        assertThrows(IllegalArgumentException.class, () -> new PickingPlanService(null));
        assertThrows(IllegalArgumentException.class, () -> new PickingPlanService(new HashMap<>()));
    }

    @Test
    void createPickingPlanRejectsNullOrEmptyAllocationsAndInvalidCapacity() {
        Map<String, Double> weights = Map.of("SKU1", 1.0);
        PickingPlanService svc = new PickingPlanService(weights);

        assertThrows(IllegalArgumentException.class, () -> svc.createPickingPlan(null, 10.0, PackingHeuristic.FIRST_FIT));
        assertThrows(IllegalArgumentException.class, () -> svc.createPickingPlan(new ArrayList<>(), 10.0, PackingHeuristic.FIRST_FIT));
        assertThrows(IllegalArgumentException.class, () -> svc.createPickingPlan(Collections.singletonList(alloc("SKU1", 1)), 0.0, PackingHeuristic.FIRST_FIT));
        assertThrows(IllegalArgumentException.class, () -> svc.createPickingPlan(Collections.singletonList(alloc("SKU1", 1)), -5.0, PackingHeuristic.FIRST_FIT));
    }

    @Test
    void missingSkuWeightThrows() {
        Map<String, Double> weights = Map.of("OTHER_SKU", 2.0);
        PickingPlanService svc = new PickingPlanService(weights);

        List<Allocation> allocations = Collections.singletonList(alloc("SKU_MISSING", 1));
        assertThrows(IllegalStateException.class, () -> svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT));
    }

    @Test
    void singleUnitHeavierThanTrolleyThrows() {
        Map<String, Double> weights = Map.of("HEAVY", 15.0);
        PickingPlanService svc = new PickingPlanService(weights);

        List<Allocation> allocations = Collections.singletonList(alloc("HEAVY", 1));
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT));
        assertTrue(ex.getMessage().contains("weighs"));
    }

    @Test
    void splittingAllowed_splitsAllocationAcrossTrolleys_firstFit() {
        Map<String, Double> weights = Map.of("SKU_S", 4.0);
        PickingPlanService svc = new PickingPlanService(weights, true);

        List<Allocation> allocations = Collections.singletonList(alloc("SKU_S", 3));
        PickingPlan plan = svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT);

        assertEquals(2, plan.getTrolleyCount(), "espera 2 trolleys após split (2 + 1 unidades)");
        assertEquals(0, plan.getSkippedCount(), "não deve ter alocações ignoradas quando splitting ativado");
        assertEquals(12.0, plan.getTotalWeight(), 1e-6);
    }

    @Test
    void splittingDisabled_skipsOversizedTotalWeight() {
        Map<String, Double> weights = Map.of("SKU_S", 4.0);
        PickingPlanService svc = new PickingPlanService(weights, false);

        List<Allocation> allocations = Collections.singletonList(alloc("SKU_S", 3));
        PickingPlan plan = svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT);

        assertEquals(0, plan.getTrolleyCount(), "sem splitting e total > capacidade, nenhum trolley deve ser criado");
        assertEquals(1, plan.getSkippedCount(), "alocação deve ser marcada como ignorada");
    }

    @Test
    void compareHeuristicsAndReportProduceResults() {
        Map<String, Double> weights = Map.of("A", 2.0, "B", 3.0);
        PickingPlanService svc = new PickingPlanService(weights);

        List<Allocation> allocations = Arrays.asList(alloc("A", 2), alloc("B", 1));

        Map<PackingHeuristic, PickingPlan> results = svc.compareHeuristics(allocations, 10.0);
        assertEquals(PackingHeuristic.values().length, results.size());
        for (PackingHeuristic h : PackingHeuristic.values()) {
            assertTrue(results.containsKey(h));
            assertNotNull(results.get(h));
        }

        String report = svc.generateComparisonReport(allocations, 10.0);
        assertTrue(report.contains("Packing Heuristics Comparison"));
        assertTrue(report.contains("Heuristic:"));
        for (PackingHeuristic h : PackingHeuristic.values()) {
            assertTrue(report.contains(h.toString()));
        }
    }
}
