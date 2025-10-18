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