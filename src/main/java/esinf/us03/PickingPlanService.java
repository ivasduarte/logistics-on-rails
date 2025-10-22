/**
 * PickingPlanService.java
 */
package esinf.us03;

import esinf.us02.Allocation;
import java.util.*;

/**
 * Class responsible for creating and comparing picking plans using various packing heuristics.
 * <p>
 * This service implements three main packing heuristics:
 * <ul>
 *   <li><b>First Fit (FF)</b>: Places allocations in the first available trolley in input order</li>
 *   <li><b>First Fit Decreasing (FFD)</b>: Sorts by weight descending, then applies First Fit</li>
 *   <li><b>Best Fit Decreasing (BFD)</b>: Sorts by weight descending, places in trolley with tightest fit</li>
 * </ul>
 * <p>
 * This class if focused on:
 * <ul>
 *   <li>Allocation splitting: When an allocation doesn't fit, it can be split across multiple trolleys</li>
 *   <li>Weight validation: Ensures each SKU has weight information</li>
 *   <li>Capacity validation: Verifies that individual units can fit in a trolley</li>
 *   <li>Heuristic comparison: Allows comparing all three heuristics for the same input</li>
 * </ul>
 */
public class PickingPlanService {

    private Map<String, Double> itemWeights;
    private boolean allowSplitting;

    /**
     * Constructs a PickingPlanService with the specified item weights.
     * Splitting is enabled by default.
     *
     * @param itemWeights a map from SKU to weight per unit in kilograms
     * @throws IllegalArgumentException if itemWeights is null or empty
     */
    public PickingPlanService(Map<String, Double> itemWeights) {
        this(itemWeights, true);
    }

    /**
     * Constructs a PickingPlanService with the specified item weights and splitting preference.
     *
     * @param itemWeights a map from SKU to weight per unit in kilograms
     * @param allowSplitting true to allow allocation splitting, false otherwise
     * @throws IllegalArgumentException if itemWeights is null or empty
     */
    public PickingPlanService(Map<String, Double> itemWeights, boolean allowSplitting) {
        if (itemWeights == null || itemWeights.isEmpty()) {
            throw new IllegalArgumentException("Item weights map cannot be null or empty");
        }
        this.itemWeights = itemWeights;
        this.allowSplitting = allowSplitting;
    }

    /**
     * Creates a picking plan for the given allocations using the specified heuristic and trolley capacity.
     * <p>
     * The method performs the following steps:
     * <ol>
     *   <li>Validates input parameters (non-null/empty allocations, positive capacity)</li>
     *   <li>Calculates weights for all allocations using the item weights map</li>
     *   <li>Verifies that individual units can fit in a trolley (even single units must not exceed capacity)</li>
     *   <li>Applies the selected heuristic to create the plan</li>
     * </ol>
     *
     * @param allocations the list of allocations to pack into trolleys
     * @param trolleyCapacity the maximum weight capacity of each trolley in kilograms
     * @param heuristic the packing heuristic to use (FIRST_FIT, FIRST_FIT_DECREASING, or BEST_FIT_DECREASING)
     * @return a complete picking plan containing trolleys with assigned allocations
     * @throws IllegalArgumentException if allocations is null/empty or trolleyCapacity <= 0
     * @throws IllegalStateException if a SKU is missing weight info or if a single unit exceeds capacity
     */
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

    /**
     * Calculates the total weight for each allocation based on SKU and quantity.
     * <p>
     * For each allocation, retrieves the unit weight from the item weights map
     * and multiplies by the quantity to get the total weight.
     *
     * @param allocations the list of allocations
     * @return a list of allocations with their calculated weights
     * @throws IllegalStateException if a SKU is not found in the item weights map
     */
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

    /**
     * Handles splitting of an allocation that doesn't fit entirely in a trolley.
     * <p>
     * This method:
     * <ol>
     *   <li>Calculates how many units can fit in the target trolley's remaining capacity</li>
     *   <li>Creates a partial allocation with the quantity that fits</li>
     *   <li>Adds the partial allocation to the target trolley with partial marking</li>
     *   <li>Creates a remainder allocation with the remaining quantity</li>
     *   <li>Returns the remainder to be processed in subsequent iterations</li>
     * </ol>
     * <p>
     * If no units can fit (remaining capacity too small), returns the original allocation unchanged.
     *
     * @param aw the allocation to split
     * @param targetTrolley the trolley to place the partial allocation
     * @param originalQuantity the original quantity before any splitting (or null if first split)
     * @return an AllocationWeight containing the remainder, or the original if nothing fits
     */
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

    /**
     * Implements the First Fit (FF) heuristic.
     * <p>
     * Algorithm:
     * <ol>
     *   <li>Process allocations in input order (no sorting)</li>
     *   <li>For each allocation, try to place it in the first trolley that has sufficient capacity</li>
     *   <li>If no existing trolley fits, create a new trolley</li>
     *   <li>If allocation exceeds capacity and splitting is enabled, split across trolleys</li>
     *   <li>If allocation cannot be placed and splitting is disabled/impossible, skip it</li>
     * </ol>
     * <p>
     * Uses a batch processing approach to handle split allocations iteratively.
     *
     * @param allocationsWithWeights the allocations with calculated weights
     * @param trolleyCapacity the trolley capacity in kilograms
     * @return a picking plan using the First Fit heuristic
     */
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

    /**
     * Implements the First Fit Decreasing (FFD) heuristic.
     * <p>
     * Algorithm:
     * <ol>
     *   <li>Sort allocations by weight in descending order (heaviest first)</li>
     *   <li>For each allocation, try to place it in the first trolley that has sufficient capacity</li>
     *   <li>If no existing trolley fits, create a new trolley</li>
     *   <li>If allocation exceeds capacity and splitting is enabled, split across trolleys</li>
     *   <li>Split remainders are added back to the queue for processing</li>
     * </ol>
     * <p>
     * Uses a queue-based approach to efficiently handle split allocations.
     * Generally produces better packing efficiency than First Fit due to sorting.
     *
     * @param allocationsWithWeights the allocations with calculated weights
     * @param trolleyCapacity the trolley capacity in kilograms
     * @return a picking plan using the First Fit Decreasing heuristic
     */
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

    /**
     * Implements the Best Fit Decreasing (BFD) heuristic.
     * <p>
     * Algorithm:
     * <ol>
     *   <li>Sort allocations by weight in descending order (heaviest first)</li>
     *   <li>For each allocation, find the trolley that will have the smallest remaining capacity
     *       after placing the allocation (tightest fit)</li>
     *   <li>If no existing trolley fits, create a new trolley</li>
     *   <li>If allocation exceeds capacity and splitting is enabled, find the trolley that can
     *       fit the maximum quantity and split there</li>
     * </ol>
     * <p>
     * This heuristic typically achieves the best packing efficiency by minimizing wasted space,
     * as it always chooses the trolley that will be most fully utilized after the allocation.
     *
     * @param allocationsWithWeights the allocations with calculated weights
     * @param trolleyCapacity the trolley capacity in kilograms
     * @return a picking plan using the Best Fit Decreasing heuristic
     */
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

    /**
     * Compares all three packing heuristics for the same set of allocations and capacity.
     * <p>
     * Creates a picking plan for each heuristic (FIRST_FIT, FIRST_FIT_DECREASING, BEST_FIT_DECREASING)
     * and returns them in a map for comparison.
     *
     * @param allocations the list of allocations to pack
     * @param trolleyCapacity the trolley capacity in kilograms
     * @return a map from each heuristic to its resulting picking plan
     */
    public Map<PackingHeuristic, PickingPlan> compareHeuristics(
            List<Allocation> allocations,
            double trolleyCapacity) {

        Map<PackingHeuristic, PickingPlan> results = new LinkedHashMap<>();

        for (PackingHeuristic heuristic : PackingHeuristic.values()) {
            results.put(heuristic, createPickingPlan(allocations, trolleyCapacity, heuristic));
        }

        return results;
    }

    /**
     * Generates a comparison report for all three heuristics.
     * <p>
     * The report includes for each heuristic:
     * <ul>
     *   <li>Total number of trolleys needed</li>
     *   <li>Number of skipped allocations</li>
     *   <li>Utilization of each trolley</li>
     * </ul>
     *
     * @param allocations the list of allocations to pack
     * @param trolleyCapacity the trolley capacity in kilograms
     * @return a formatted comparison report string
     */
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

    /**
     * Gets the item weights map.
     *
     * @return the map from SKU to weight per unit in kilograms
     */
    public Map<String, Double> getItemWeights() {
        return itemWeights;
    }

    /**
     * Sets the item weights map.
     *
     * @param itemWeights the new map from SKU to weight per unit
     */
    public void setItemWeights(Map<String, Double> itemWeights) {
        this.itemWeights = itemWeights;
    }

    /**
     * Checks if allocation splitting is allowed.
     *
     * @return true if splitting is allowed, false otherwise
     */
    public boolean isAllowSplitting() {
        return allowSplitting;
    }

    /**
     * Sets whether allocation splitting is allowed.
     *
     * @param allowSplitting true to allow splitting, false to disallow
     */
    public void setAllowSplitting(boolean allowSplitting) {
        this.allowSplitting = allowSplitting;
    }
}