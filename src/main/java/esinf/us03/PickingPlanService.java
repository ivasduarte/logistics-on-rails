package esinf.us03;

import esinf.us02.Allocation;
import java.util.*;

public class PickingPlanService {

    private Map<String, Double> itemWeights;

    public PickingPlanService(Map<String, Double> itemWeights) {
        this.itemWeights = itemWeights;
    }

    public PickingPlan createPickingPlan(List<Allocation> allocations,
                                         double trolleyCapacity,
                                         PackingHeuristic heuristic) {

        List<AllocationWeight> allocationsWithWeights = calculateWeights(allocations);

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
                throw new IllegalArgumentException(
                        "No weight found for SKU: " + allocation.getSku());
            }

            double totalWeight = unitWeight * allocation.getQuantityAllocated();
            result.add(new AllocationWeight(allocation, totalWeight));
        }

        return result;
    }

    private PickingPlan firstFit(List<AllocationWeight> allocationsWithWeights,
                                 double trolleyCapacity) {

        PickingPlan plan = new PickingPlan(PackingHeuristic.FIRST_FIT, trolleyCapacity);
        List<Trolley> trolleys = new ArrayList<>();

        for (AllocationWeight aw : allocationsWithWeights) {
            boolean placed = false;
            for (Trolley trolley : trolleys) {
                if (trolley.addAllocation(aw.getAllocation(), aw.getWeight())) {
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                Trolley newTrolley = new Trolley(trolleys.size() + 1, trolleyCapacity);
                if (!newTrolley.addAllocation(aw.getAllocation(), aw.getWeight())) {
                    throw new IllegalArgumentException(
                            "Allocation exceeds trolley capacity: " + aw.getWeight() + " > " + trolleyCapacity);
                }
                trolleys.add(newTrolley);
            }
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

        for (AllocationWeight aw : sorted) {
            boolean placed = false;

            for (Trolley trolley : trolleys) {
                if (trolley.addAllocation(aw.getAllocation(), aw.getWeight())) {
                    placed = true;
                    break;
                }
            }

            if (!placed) {
                Trolley newTrolley = new Trolley(trolleys.size() + 1, trolleyCapacity);
                if (!newTrolley.addAllocation(aw.getAllocation(), aw.getWeight())) {
                    throw new IllegalArgumentException(
                            "Allocation exceeds trolley capacity: " + aw.getWeight() + " > " + trolleyCapacity);
                }
                trolleys.add(newTrolley);
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

        for (AllocationWeight aw : sorted) {
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
                Trolley newTrolley = new Trolley(trolleys.size() + 1, trolleyCapacity);
                if (!newTrolley.addAllocation(aw.getAllocation(), aw.getWeight())) {
                    throw new IllegalArgumentException(
                            "Allocation exceeds trolley capacity: " + aw.getWeight() + " > " + trolleyCapacity);
                }
                trolleys.add(newTrolley);
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

        StringBuilder sb = new StringBuilder();
        sb.append("=== Packing Heuristics Comparison ===\n\n");
        sb.append(String.format("Total Allocations: %d\n", allocations.size()));
        sb.append(String.format("Trolley Capacity: %.2f kg\n\n", trolleyCapacity));

        sb.append(String.format("%-25s %10s %15s\n", "Heuristic", "Trolleys", "Avg Util (%)"));
        sb.append("-".repeat(52)).append("\n");

        for (Map.Entry<PackingHeuristic, PickingPlan> entry : results.entrySet()) {
            PickingPlan plan = entry.getValue();
            sb.append(String.format("%-25s %10d %15.1f\n",
                    entry.getKey(),
                    plan.getTrolleyCount(),
                    plan.getAverageUtilization()));
        }

        sb.append("\n");
        for (Map.Entry<PackingHeuristic, PickingPlan> entry : results.entrySet()) {
            sb.append(entry.getValue().generateSummaryReport()).append("\n");
        }

        return sb.toString();
    }
    public Map<String, Double> getItemWeights() {
        return itemWeights;
    }

    public void setItemWeights(Map<String, Double> itemWeights) {
        this.itemWeights = itemWeights;
    }
}