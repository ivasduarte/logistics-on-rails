# USEI3 - Picking Plans

## 4. Tests

**Test 1:** Validates AllocationWeight constructor with default values.

```java
@Test
void constructorAndGetters_Defaults() {
    AllocationWeight aw = new AllocationWeight(null, 5.0);

    assertAll(
            () -> assertNull(aw.getAllocation()),
            () -> assertEquals(5.0, aw.getWeight(), 1e-6),
            () -> assertFalse(aw.isPartial()),
            () -> assertNull(aw.getOriginalQuantity())
    );
}
```


**Test 2:** Validates AllocationWeight constructor with all fields.

```java
@Test
void constructorWithAllFields() {
    AllocationWeight aw = new AllocationWeight(null, 3.5, true, 42);

    assertAll(
            () -> assertNull(aw.getAllocation()),
            () -> assertEquals(3.5, aw.getWeight(), 1e-6),
            () -> assertTrue(aw.isPartial()),
            () -> assertEquals(Integer.valueOf(42), aw.getOriginalQuantity())
    );
}
```


**Test 3:** Validates AllocationWeight sorting by weight (descending order).

```java
@Test
void compareTo_SortsByWeightDescending() {
    AllocationWeight a = new AllocationWeight(null, 5.0);
    AllocationWeight b = new AllocationWeight(null, 10.0);
    AllocationWeight c = new AllocationWeight(null, 7.0);

    List<AllocationWeight> list = new ArrayList<>();
    list.add(a);
    list.add(b);
    list.add(c);

    Collections.sort(list);

    assertAll(
            () -> assertEquals(10.0, list.get(0).getWeight(), 1e-6),
            () -> assertEquals(7.0, list.get(1).getWeight(), 1e-6),
            () -> assertEquals(5.0, list.get(2).getWeight(), 1e-6)
    );
}
```

**Test 4:** Validates Trolley construction and initial state.

```java
@Test
void constructorAndGetters() {
    Trolley t = new Trolley(2, 100.0);
    assertAll(
            () -> assertEquals(2, t.getTrolleyNumber()),
            () -> assertEquals(100.0, t.getCapacityWeight(), 1e-6),
            () -> assertEquals(0.0, t.getUsedWeight(), 1e-6),
            () -> assertTrue(t.isEmpty()),
            () -> assertEquals(100.0, t.getRemainingCapacity(), 1e-6),
            () -> assertEquals(0.0, t.getUtilization(), 1e-6)
    );
}
```


**Test 5:** Validates adding allocation to trolley within capacity

```java
@Test
void addAllocationFits() {
    Trolley t = new Trolley(1, 50.0);
    Allocation a = null;
    assertTrue(t.addAllocation(a, 20.0));
    assertAll(
            () -> assertEquals(20.0, t.getUsedWeight(), 1e-6),
            () -> assertEquals(30.0, t.getRemainingCapacity(), 1e-6),
            () -> assertEquals(1, t.getAllocationCount()),
            () -> assertFalse(t.isEmpty())
    );
}
```


**Test 6:** Validates rejection of allocation exceeding trolley capacity.

```java
@Test
void addAllocationTooHeavy() {
    Trolley t = new Trolley(1, 10.0);
    Allocation a = null;
    assertFalse(t.addAllocation(a, 20.0));
    assertAll(
            () -> assertEquals(0, t.getAllocationCount()),
            () -> assertEquals(0.0, t.getUsedWeight(), 1e-6)
    );
}
```


**Test 7:** Validates PickingPlan construction and initial state.

```java
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
```


**Test 8:** Validates adding trolley to plan updates metrics correctly.

```java
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
```

**Test 9:** Validates PickingPlanService rejects null or empty item weights

```java
@Test
void constructorRejectsNullOrEmptyItemWeights() {
    assertThrows(IllegalArgumentException.class, () -> new PickingPlanService(null));
    assertThrows(IllegalArgumentException.class, () -> new PickingPlanService(new HashMap<>()));
}
```


**Test 10:** Validates PickingPlanService rejects invalid inputs for picking plan creation.

```java
@Test
void createPickingPlanRejectsNullOrEmptyAllocationsAndInvalidCapacity() {
    Map<String, Double> weights = Map.of("SKU1", 1.0);
    PickingPlanService svc = new PickingPlanService(weights);

    assertThrows(IllegalArgumentException.class,
            () -> svc.createPickingPlan(null, 10.0, PackingHeuristic.FIRST_FIT));
    assertThrows(IllegalArgumentException.class,
            () -> svc.createPickingPlan(new ArrayList<>(), 10.0, PackingHeuristic.FIRST_FIT));
    assertThrows(IllegalArgumentException.class,
            () -> svc.createPickingPlan(Collections.singletonList(alloc("SKU1", 1)), 0.0, PackingHeuristic.FIRST_FIT));
}
```


**Test 11:** Validates exception when SKU weight is missing

```java
@Test
void missingSkuWeightThrows() {
    Map<String, Double> weights = Map.of("OTHER_SKU", 2.0);
    PickingPlanService svc = new PickingPlanService(weights);

    List<Allocation> allocations = Collections.singletonList(alloc("SKU_MISSING", 1));
    assertThrows(IllegalStateException.class,
            () -> svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT));
}
```


**Test 12:** Validates exception when single unit weight exceeds trolley capacity.

```java
@Test
void singleUnitHeavierThanTrolleyThrows() {
    Map<String, Double> weights = Map.of("HEAVY", 15.0);
    PickingPlanService svc = new PickingPlanService(weights);

    List<Allocation> allocations = Collections.singletonList(alloc("HEAVY", 1));
    IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT));
    assertTrue(ex.getMessage().contains("weighs"));
}
```


**Test 13:** Validates splitting functionality with First Fit heuristic.

```java
@Test
void splittingAllowed_splitsAllocationAcrossTrolleys_firstFit() {
    Map<String, Double> weights = Map.of("SKU_S", 4.0);
    PickingPlanService svc = new PickingPlanService(weights, true);

    List<Allocation> allocations = Collections.singletonList(alloc("SKU_S", 3));
    PickingPlan plan = svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT);

    assertEquals(2, plan.getTrolleyCount());
    assertEquals(0, plan.getSkippedCount());
    assertEquals(12.0, plan.getTotalWeight(), 1e-6);
}
```


**Test 14:** Validates skipping oversized allocations when splitting is disabled.

```java
@Test
void splittingDisabled_skipsOversizedTotalWeight() {
    Map<String, Double> weights = Map.of("SKU_S", 4.0);
    PickingPlanService svc = new PickingPlanService(weights, false);

    List<Allocation> allocations = Collections.singletonList(alloc("SKU_S", 3));
    PickingPlan plan = svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT);

    assertEquals(0, plan.getTrolleyCount());
    assertEquals(1, plan.getSkippedCount());
}
```


**Test 15:** Validates comparison of all three heuristics.

```java
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
}
```


**Test 16:** Validates all heuristics produce correct results for basic packing scenario.

```java
@Test
void allHeuristics_basicPacking_sameTotalWeightAndSingleTrolley() {
    Map<String, Double> weights = Map.of("A", 2.0, "B", 3.0);
    PickingPlanService svc = new PickingPlanService(weights);

    List<Allocation> allocations = Arrays.asList(alloc("A", 2), alloc("B", 1));

    for (PackingHeuristic heuristic : PackingHeuristic.values()) {
        PickingPlan plan = svc.createPickingPlan(allocations, 10.0, heuristic);
        assertEquals(heuristic, plan.getHeuristic());
        assertEquals(7.0, plan.getTotalWeight(), 1e-6);
        assertEquals(1, plan.getTrolleyCount());
        assertEquals(0, plan.getSkippedCount());
    }
}
```


**Test 17:** Validates First Fit Decreasing handles splitting correctly.

```java
@Test
void firstFitDecreasing_handlesSplitting_sameResultAsFirstFitForSplitCase() {
    Map<String, Double> weights = Map.of("SKU_S", 4.0);
    PickingPlanService svc = new PickingPlanService(weights, true);

    List<Allocation> allocations = Collections.singletonList(alloc("SKU_S", 3));

    PickingPlan planFFD = svc.createPickingPlan(allocations, 10.0, PackingHeuristic.FIRST_FIT_DECREASING);
    assertEquals(PackingHeuristic.FIRST_FIT_DECREASING, planFFD.getHeuristic());
    assertEquals(2, planFFD.getTrolleyCount());
    assertEquals(0, planFFD.getSkippedCount());
    assertEquals(12.0, planFFD.getTotalWeight(), 1e-6);
}
```


**Test 18:** Validates Best Fit Decreasing handles splitting correctly.

```java
@Test
void bestFitDecreasing_handlesSplitting_sameResultAsFirstFitForSplitCase() {
    Map<String, Double> weights = Map.of("SKU_S", 4.0);
    PickingPlanService svc = new PickingPlanService(weights, true);

    List<Allocation> allocations = Collections.singletonList(alloc("SKU_S", 3));

    PickingPlan planBFD = svc.createPickingPlan(allocations, 10.0, PackingHeuristic.BEST_FIT_DECREASING);
    assertEquals(PackingHeuristic.BEST_FIT_DECREASING, planBFD.getHeuristic());
    assertEquals(2, planBFD.getTrolleyCount());
    assertEquals(0, planBFD.getSkippedCount());
    assertEquals(12.0, planBFD.getTotalWeight(), 1e-6);
}
```


**Test 19:** Validates summary report generation contains essential information.

```java
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
```


**Test 20:** Validates trolley utilization calculation with zero capacity.

```java
@Test
void utilizationWithZeroCapacity() {
    Trolley t = new Trolley(1, 0.0);
    assertEquals(0.0, t.getUtilization(), 1e-6);
}
```
## 5. Construction (Implementation)

### Class AllocationWeight.java
```java
package esinf.us03;

import esinf.us02.Allocation;

public class AllocationWeight implements Comparable<AllocationWeight> {

    private Allocation allocation;
    private double weight;
    private boolean isPartial;
    private Integer originalQuantity;

    public AllocationWeight(Allocation allocation, double weight) {
        this.allocation = allocation;
        this.weight = weight;
        this.isPartial = false;
        this.originalQuantity = null;
    }

    public AllocationWeight(Allocation allocation, double weight, boolean isPartial, Integer originalQuantity) {
        this.allocation = allocation;
        this.weight = weight;
        this.isPartial = isPartial;
        this.originalQuantity = originalQuantity;
    }

    public Allocation getAllocation() {
        return allocation;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isPartial() {
        return isPartial;
    }

    public Integer getOriginalQuantity() {
        return originalQuantity;
    }

    public void setAllocation(Allocation allocation) {
        this.allocation = allocation;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setPartial(boolean partial) {
        isPartial = partial;
    }

    public void setOriginalQuantity(Integer originalQuantity) {
        this.originalQuantity = originalQuantity;
    }

    @Override
    public int compareTo(AllocationWeight other) {
        return Double.compare(other.weight, this.weight);
    }

    @Override
    public String toString() {
        String partialInfo = isPartial ? " (PARTIAL: " + allocation.getQuantityAllocated() + "/" + originalQuantity + ")" : "";
        return "AllocationWeight: " + allocation.getBoxID() + ", weight=" + weight + " kg" + partialInfo;
    }
}
```
### Enum PackingHeuristic
```java
package esinf.us03;

public enum PackingHeuristic {
    FIRST_FIT,
    FIRST_FIT_DECREASING,
    BEST_FIT_DECREASING
}
```

### Class PickingPlan.java
```java
package esinf.us03;

import esinf.us02.Allocation;
import java.util.ArrayList;
import java.util.List;

public class PickingPlan {

    private PackingHeuristic heuristic;
    private double trolleyCapacity;
    private List<Trolley> trolleys;
    private int totalAllocations;
    private double totalWeight;
    private List<SkippedAllocation> skippedAllocations;

    public PickingPlan(PackingHeuristic heuristic, double trolleyCapacity) {
        this.heuristic = heuristic;
        this.trolleyCapacity = trolleyCapacity;
        this.trolleys = new ArrayList<>();
        this.totalAllocations = 0;
        this.totalWeight = 0.0;
        this.skippedAllocations = new ArrayList<>();
    }

    public PackingHeuristic getHeuristic() {
        return heuristic;
    }

    public double getTrolleyCapacity() {
        return trolleyCapacity;
    }

    public List<Trolley> getTrolleys() {
        return new ArrayList<>(trolleys);
    }

    public int getTrolleyCount() {
        return trolleys.size();
    }

    public int getTotalAllocations() {
        return totalAllocations;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public List<SkippedAllocation> getSkippedAllocations() {
        return new ArrayList<>(skippedAllocations);
    }

    public int getSkippedCount() {
        return skippedAllocations.size();
    }

    public double getAverageUtilization() {
        if (trolleys.isEmpty()) return 0.0;
        return trolleys.stream()
                .mapToDouble(Trolley::getUtilization)
                .average()
                .orElse(0.0);
    }

    public void setHeuristic(PackingHeuristic heuristic) {
        this.heuristic = heuristic;
    }

    public void setTrolleyCapacity(double trolleyCapacity) {
        this.trolleyCapacity = trolleyCapacity;
    }

    public void setTrolleys(List<Trolley> trolleys) {
        this.trolleys = trolleys;
        recalculateMetrics();
    }

    public void addTrolley(Trolley trolley) {
        trolleys.add(trolley);
        totalAllocations += trolley.getAllocationCount();
        totalWeight += trolley.getUsedWeight();
    }

    public void addSkippedAllocation(Allocation allocation, double weight) {
        skippedAllocations.add(new SkippedAllocation(allocation, weight));
    }

    private void recalculateMetrics() {
        totalAllocations = trolleys.stream()
                .mapToInt(Trolley::getAllocationCount)
                .sum();
        totalWeight = trolleys.stream()
                .mapToDouble(Trolley::getUsedWeight)
                .sum();
    }

    public String generateSummaryReport() {
        String report = "";
        report += "Picking Plan Summary\n";
        report += "Heuristic: " + heuristic + "\n";
        report += "Trolley Capacity: " + trolleyCapacity + " kg\n";
        report += "Total Number of Trolleys: " + getTrolleyCount() + "\n";
        report += "Skipped Allocations: " + getSkippedCount() + "\n\n";

        report += "Trolley Utilization:\n";
        for (Trolley trolley : trolleys) {
            report += "Trolley " + trolley.getTrolleyNumber() + ": " + String.format("%.2f", trolley.getUtilization()) + "% full\n";
        }

        if (!skippedAllocations.isEmpty()) {
            report += "\nSkipped Allocations (exceeded capacity):\n";
            for (SkippedAllocation skipped : skippedAllocations) {
                report += "  BoxID: " + skipped.allocation.getBoxID() + ", SKU: " + skipped.allocation.getSku() + ", Weight: " + String.format("%.2f", skipped.weight) + " kg\n";
            }
        }

        return report;
    }

    public String generateDetailedPickingList() {
        String report = "";
        report += "Picking Plan: " + heuristic + "\n\n";

        for (Trolley trolley : trolleys) {
            report += "Trolley " + trolley.getTrolleyNumber() + "\n";
            report += "Utilization: " + String.format("%.2f", trolley.getUtilization()) + "%\n\n";

            for (var allocationEntry : trolley.getAllocationEntries()) {
                Allocation allocation = allocationEntry.allocation;
                report += "Order ID: " + allocation.getOrderID() + "\n";
                report += "Line Number: " + allocation.getLineNo() + "\n";
                report += "Aisle: " + allocation.getAisle() + "\n";
                report += "Bay Number: " + allocation.getBayNumber() + "\n";
                report += "Box ID: " + allocation.getBoxID() + "\n";
                report += "SKU: " + allocation.getSku() + "\n";
                report += "Quantity: " + allocation.getQuantityAllocated();
                if (allocationEntry.isPartial) {
                    report += " (PARTIAL: " + allocation.getQuantityAllocated() + "/" + allocationEntry.originalQuantity + ")";
                }
                report += "\n\n";
            }
            report += "\n";
        }

        if (!skippedAllocations.isEmpty()) {
            report += "\nSkipped Allocations (exceeded capacity):\n";
            for (SkippedAllocation skipped : skippedAllocations) {
                report += "  Order ID: " + skipped.allocation.getOrderID() + "\n";
                report += "  Box ID: " + skipped.allocation.getBoxID() + "\n";
                report += "  SKU: " + skipped.allocation.getSku() + "\n";
                report += "  Quantity: " + skipped.allocation.getQuantityAllocated() + "\n";
                report += "  Weight: " + String.format("%.2f", skipped.weight) + " kg\n\n";
            }
        }

        return report;
    }

    @Override
    public String toString() {
        return "PickingPlan: " + heuristic + ", trolleys=" + getTrolleyCount() + ", allocations=" + totalAllocations + ", skipped=" + getSkippedCount() + ", avgUtil=" + String.format("%.2f", getAverageUtilization()) + "%";
    }

    public static class SkippedAllocation {
        public final Allocation allocation;
        public final double weight;

        public SkippedAllocation(Allocation allocation, double weight) {
            this.allocation = allocation;
            this.weight = weight;
        }
    }
}
```
### Class PickingPlanService.java

```java
package esinf.us03;

import esinf.us02.Allocation;
import java.util.*;

public class PickingPlanService {

    private Map<String, Double> itemWeights;
    private boolean allowSplitting;

    public PickingPlanService(Map<String, Double> itemWeights) {
        this(itemWeights, true);
    }

    public PickingPlanService(Map<String, Double> itemWeights, boolean allowSplitting) {
        if (itemWeights == null || itemWeights.isEmpty()) {
            throw new IllegalArgumentException("Item weights map cannot be null or empty");
        }
        this.itemWeights = itemWeights;
        this.allowSplitting = allowSplitting;
    }

    public PickingPlan createPickingPlan(List<Allocation> allocations,
                                         double trolleyCapacity,
                                         PackingHeuristic heuristic) {
        if (allocations == null || allocations.isEmpty()) {
            throw new IllegalArgumentException("Allocations list cannot be null or empty");
        }

        if (trolleyCapacity <= 0) {
            throw new IllegalArgumentException("Trolley capacity must be greater than 0");
        }

        List<AllocationWeight> allocationsWithWeights = calculateWeights(allocations);

        for (AllocationWeight aw : allocationsWithWeights) {
            double unitWeight = aw.getWeight() / aw.getAllocation().getQuantityAllocated();
            if (unitWeight > trolleyCapacity) {
                throw new IllegalStateException(
                        "Single unit of SKU " + aw.getAllocation().getSku() +
                                " weighs " + String.format("%.2f", unitWeight) + " kg, " +
                                "which exceeds trolley capacity of " + trolleyCapacity + " kg. " +
                                "Cannot create picking plan even with splitting enabled.");
            }
        }

        return switch (heuristic) {
            case FIRST_FIT -> firstFit(allocationsWithWeights, trolleyCapacity);
            case FIRST_FIT_DECREASING -> firstFitDecreasing(allocationsWithWeights, trolleyCapacity);
            case BEST_FIT_DECREASING -> bestFitDecreasing(allocationsWithWeights, trolleyCapacity);
        };
    }

    private List<AllocationWeight> calculateWeights(List<Allocation> allocations) {
        List<AllocationWeight> result = new ArrayList<>();

        for (Allocation allocation : allocations) {
            Double unitWeight = itemWeights.get(allocation.getSku());

            if (unitWeight == null) {
                throw new IllegalStateException(
                        "No weight found for SKU: " + allocation.getSku() +
                                ". All SKUs must have weight information in the item weights map.");
            }

            double totalWeight = unitWeight * allocation.getQuantityAllocated();
            result.add(new AllocationWeight(allocation, totalWeight));
        }

        return result;
    }

    private AllocationWeight handleSplitting(AllocationWeight aw, Trolley targetTrolley, Integer originalQuantity) {
        double unitWeight = aw.getWeight() / aw.getAllocation().getQuantityAllocated();
        int quantityThatFits = (int) (targetTrolley.getRemainingCapacity() / unitWeight);

        if (quantityThatFits <= 0) {
            return aw;
        }

        int currentQuantity = aw.getAllocation().getQuantityAllocated();
        int origQty = (originalQuantity != null) ? originalQuantity : currentQuantity;

        Allocation partialAllocation = new Allocation(
                aw.getAllocation().getOrderID(),
                aw.getAllocation().getBoxID(),
                aw.getAllocation().getWarehouseId(),
                aw.getAllocation().getSku(),
                aw.getAllocation().getLineNo(),
                aw.getAllocation().getAisle(),
                aw.getAllocation().getBayNumber(),
                quantityThatFits
        );

        double partialWeight = unitWeight * quantityThatFits;
        targetTrolley.addAllocation(partialAllocation, partialWeight, true, origQty);

        int remainingQty = currentQuantity - quantityThatFits;
        Allocation remainderAllocation = new Allocation(
                aw.getAllocation().getOrderID(),
                aw.getAllocation().getBoxID(),
                aw.getAllocation().getWarehouseId(),
                aw.getAllocation().getSku(),
                aw.getAllocation().getLineNo(),
                aw.getAllocation().getAisle(),
                aw.getAllocation().getBayNumber(),
                remainingQty
        );

        double remainderWeight = unitWeight * remainingQty;
        return new AllocationWeight(remainderAllocation, remainderWeight, true, origQty);
    }

    private PickingPlan firstFit(List<AllocationWeight> allocationsWithWeights,
                                 double trolleyCapacity) {
        PickingPlan plan = new PickingPlan(PackingHeuristic.FIRST_FIT, trolleyCapacity);
        List<Trolley> trolleys = new ArrayList<>();

        List<AllocationWeight> currentBatch = new ArrayList<>(allocationsWithWeights);
        List<AllocationWeight> nextBatch = new ArrayList<>();

        while (!currentBatch.isEmpty()) {
            for (AllocationWeight aw : currentBatch) {
                boolean placed = false;

                for (Trolley trolley : trolleys) {
                    if (trolley.addAllocation(aw.getAllocation(), aw.getWeight())) {
                        placed = true;
                        break;
                    }
                }

                if (!placed) {
                    if (aw.getWeight() <= trolleyCapacity) {
                        Trolley newTrolley = new Trolley(trolleys.size() + 1, trolleyCapacity);
                        newTrolley.addAllocation(aw.getAllocation(), aw.getWeight());
                        trolleys.add(newTrolley);
                    } else if (allowSplitting && aw.getAllocation().getQuantityAllocated() > 1) {
                        Trolley targetTrolley = null;
                        for (Trolley trolley : trolleys) {
                            if (trolley.getRemainingCapacity() > 0) {
                                targetTrolley = trolley;
                                break;
                            }
                        }

                        if (targetTrolley == null) {
                            targetTrolley = new Trolley(trolleys.size() + 1, trolleyCapacity);
                            trolleys.add(targetTrolley);
                        }

                        AllocationWeight remainder = handleSplitting(aw, targetTrolley, aw.getOriginalQuantity());
                        if (remainder.getAllocation().getQuantityAllocated() > 0) {
                            nextBatch.add(remainder);
                        }
                    } else {
                        plan.addSkippedAllocation(aw.getAllocation(), aw.getWeight());
                    }
                }
            }
            currentBatch = nextBatch;
            nextBatch = new ArrayList<>();
        }

        trolleys.forEach(plan::addTrolley);
        return plan;
    }

    private PickingPlan firstFitDecreasing(List<AllocationWeight> allocationsWithWeights,
                                           double trolleyCapacity) {
        PickingPlan plan = new PickingPlan(PackingHeuristic.FIRST_FIT_DECREASING, trolleyCapacity);

        List<AllocationWeight> sorted = new ArrayList<>(allocationsWithWeights);
        Collections.sort(sorted);

        List<Trolley> trolleys = new ArrayList<>();
        Queue<AllocationWeight> pendingQueue = new LinkedList<>(sorted);

        while (!pendingQueue.isEmpty()) {
            AllocationWeight aw = pendingQueue.poll();
            boolean placed = false;

            for (Trolley trolley : trolleys) {
                if (trolley.addAllocation(aw.getAllocation(), aw.getWeight())) {
                    placed = true;
                    break;
                }
            }

            if (!placed) {
                if (aw.getWeight() <= trolleyCapacity) {
                    Trolley newTrolley = new Trolley(trolleys.size() + 1, trolleyCapacity);
                    newTrolley.addAllocation(aw.getAllocation(), aw.getWeight());
                    trolleys.add(newTrolley);
                } else if (allowSplitting && aw.getAllocation().getQuantityAllocated() > 1) {
                    Trolley targetTrolley = null;
                    for (Trolley trolley : trolleys) {
                        if (trolley.getRemainingCapacity() > 0) {
                            targetTrolley = trolley;
                            break;
                        }
                    }

                    if (targetTrolley == null) {
                        targetTrolley = new Trolley(trolleys.size() + 1, trolleyCapacity);
                        trolleys.add(targetTrolley);
                    }

                    AllocationWeight remainder = handleSplitting(aw, targetTrolley, aw.getOriginalQuantity());
                    if (remainder.getAllocation().getQuantityAllocated() > 0) {
                        pendingQueue.offer(remainder);
                    }
                } else {
                    plan.addSkippedAllocation(aw.getAllocation(), aw.getWeight());
                }
            }
        }

        trolleys.forEach(plan::addTrolley);
        return plan;
    }

    private PickingPlan bestFitDecreasing(List<AllocationWeight> allocationsWithWeights,
                                          double trolleyCapacity) {
        PickingPlan plan = new PickingPlan(PackingHeuristic.BEST_FIT_DECREASING, trolleyCapacity);

        List<AllocationWeight> sorted = new ArrayList<>(allocationsWithWeights);
        Collections.sort(sorted);

        List<Trolley> trolleys = new ArrayList<>();
        Queue<AllocationWeight> pendingQueue = new LinkedList<>(sorted);
        while (!pendingQueue.isEmpty()) {
            AllocationWeight aw = pendingQueue.poll();
            Trolley bestTrolley = null;
            double minRemainingCapacity = Double.MAX_VALUE;

            for (Trolley trolley : trolleys) {
                if (trolley.canFit(aw.getWeight())) {
                    double remainingAfter = trolley.getRemainingCapacity() - aw.getWeight();
                    if (remainingAfter < minRemainingCapacity) {
                        minRemainingCapacity = remainingAfter;
                        bestTrolley = trolley;
                    }
                }
            }

            if (bestTrolley != null) {
                bestTrolley.addAllocation(aw.getAllocation(), aw.getWeight());
            } else {
                if (aw.getWeight() <= trolleyCapacity) {
                    Trolley newTrolley = new Trolley(trolleys.size() + 1, trolleyCapacity);
                    newTrolley.addAllocation(aw.getAllocation(), aw.getWeight());
                    trolleys.add(newTrolley);
                } else if (allowSplitting && aw.getAllocation().getQuantityAllocated() > 1) {
                    Trolley targetTrolley = null;
                    int maxQuantityThatFits = 0;
                    double unitWeight = aw.getWeight() / aw.getAllocation().getQuantityAllocated();

                    for (Trolley trolley : trolleys) {
                        if (trolley.getRemainingCapacity() > 0) {
                            int qtyThatFits = (int) (trolley.getRemainingCapacity() / unitWeight);
                            if (qtyThatFits > maxQuantityThatFits) {
                                maxQuantityThatFits = qtyThatFits;
                                targetTrolley = trolley;
                            }
                        }
                    }

                    if (targetTrolley == null) {
                        targetTrolley = new Trolley(trolleys.size() + 1, trolleyCapacity);
                        trolleys.add(targetTrolley);
                    }

                    AllocationWeight remainder = handleSplitting(aw, targetTrolley, aw.getOriginalQuantity());
                    if (remainder.getAllocation().getQuantityAllocated() > 0) {
                        pendingQueue.offer(remainder);
                    }
                } else {
                    plan.addSkippedAllocation(aw.getAllocation(), aw.getWeight());
                }
            }
        }

        trolleys.forEach(plan::addTrolley);
        return plan;
    }

    public Map<PackingHeuristic, PickingPlan> compareHeuristics(
            List<Allocation> allocations,
            double trolleyCapacity) {

        Map<PackingHeuristic, PickingPlan> results = new LinkedHashMap<>();

        for (PackingHeuristic heuristic : PackingHeuristic.values()) {
            results.put(heuristic, createPickingPlan(allocations, trolleyCapacity, heuristic));
        }

        return results;
    }

    public String generateComparisonReport(List<Allocation> allocations,
                                           double trolleyCapacity) {

        Map<PackingHeuristic, PickingPlan> results = compareHeuristics(allocations, trolleyCapacity);

        String report = "";
        report += "Packing Heuristics Comparison\n\n";

        for (Map.Entry<PackingHeuristic, PickingPlan> entry : results.entrySet()) {
            PickingPlan plan = entry.getValue();
            report += "Heuristic: " + entry.getKey() + "\n";
            report += "Total Number of Trolleys: " + plan.getTrolleyCount() + "\n";
            report += "Skipped Allocations: " + plan.getSkippedCount() + "\n";
            report += "Trolley Utilization:\n";
            for (Trolley trolley : plan.getTrolleys()) {
                report += "  Trolley " + trolley.getTrolleyNumber() + ": " +
                        String.format("%.2f", trolley.getUtilization()) + "% full\n";
            }
            report += "\n";
        }

        return report;
    }

    public Map<String, Double> getItemWeights() {
        return itemWeights;
    }

    public void setItemWeights(Map<String, Double> itemWeights) {
        this.itemWeights = itemWeights;
    }

    public boolean isAllowSplitting() {
        return allowSplitting;
    }

    public void setAllowSplitting(boolean allowSplitting) {
        this.allowSplitting = allowSplitting;
    }
}
```

### Class Trolley.java

```java
package esinf.us03;

import esinf.us02.Allocation;
import java.util.ArrayList;
import java.util.List;

public class Trolley {

    private int trolleyNumber;
    private double capacityWeight;
    private double usedWeight;
    private List<AllocationEntry> allocationEntries;
    private List<String> logs;

    public Trolley(int trolleyNumber, double capacityWeight) {
        this.trolleyNumber = trolleyNumber;
        this.capacityWeight = capacityWeight;
        this.usedWeight = 0.0;
        this.allocationEntries = new ArrayList<>();
        this.logs = new ArrayList<>();
    }

    public int getTrolleyNumber() {
        return trolleyNumber;
    }

    public double getCapacityWeight() {
        return capacityWeight;
    }

    public double getUsedWeight() {
        return usedWeight;
    }

    public double getRemainingCapacity() {
        return capacityWeight - usedWeight;
    }

    public List<Allocation> getAllocations() {
        List<Allocation> allocations = new ArrayList<>();
        for (AllocationEntry entry : allocationEntries) {
            allocations.add(entry.allocation);
        }
        return allocations;
    }

    public List<AllocationEntry> getAllocationEntries() {
        return new ArrayList<>(allocationEntries);
    }

    public List<String> getLogs() {
        return new ArrayList<>(logs);
    }

    public double getUtilization() {
        return capacityWeight > 0 ? (usedWeight / capacityWeight) * 100 : 0;
    }

    public void setTrolleyNumber(int trolleyNumber) {
        this.trolleyNumber = trolleyNumber;
    }

    public void setCapacityWeight(double capacityWeight) {
        this.capacityWeight = capacityWeight;
    }

    public boolean addAllocation(Allocation allocation, double weight) {
        return addAllocation(allocation, weight, false, null);
    }

    public boolean addAllocation(Allocation allocation, double weight, boolean isPartial, Integer originalQuantity) {
        if (weight <= getRemainingCapacity()) {
            allocationEntries.add(new AllocationEntry(allocation, weight, isPartial, originalQuantity));
            usedWeight += weight;
            if (isPartial) {
                addLog("PARTIAL ALLOCATION: BoxID=" + allocation.getBoxID() +
                        ", SKU=" + allocation.getSku() +
                        " (Qty: " + allocation.getQuantityAllocated() + "/" + originalQuantity + ")");
            }
            return true;
        }
        return false;
    }

    public void addLog(String logMessage) {
        logs.add(logMessage);
    }

    public boolean canFit(double weight) {
        return weight <= getRemainingCapacity();
    }

    public int getAllocationCount() {
        return allocationEntries.size();
    }

    public boolean isEmpty() {
        return allocationEntries.isEmpty();
    }

    @Override
    public String toString() {
        return "Trolley " + trolleyNumber + ": " +
                String.format("%.2f", usedWeight) + "/" + capacityWeight +
                " kg (" + String.format("%.2f", getUtilization()) + "% full) - " +
                allocationEntries.size() + " allocation(s)";
    }

    public static class AllocationEntry {
        public final Allocation allocation;
        public final double weight;
        public final boolean isPartial;
        public final Integer originalQuantity;

        public AllocationEntry(Allocation allocation, double weight, boolean isPartial, Integer originalQuantity) {
            this.allocation = allocation;
            this.weight = weight;
            this.isPartial = isPartial;
            this.originalQuantity = originalQuantity;
        }
    }
}
```