/**
 * PickingPlanService.java
 */
package esinf.us03;

import esinf.us02.Allocation;
import java.util.ArrayList;
import java.util.List;

/**
 /**
 * Represents a complete picking plan for a set of allocations using a specific packing heuristic.
 * <p>
 * A picking plan contains:
 * <ul>
 *   <li>The heuristic used to create the plan</li>
 *   <li>The trolley capacity constraint</li>
 *   <li>A list of trolleys with their assigned allocations</li>
 *   <li>Statistics about the plan (total allocations, total weight, utilization)</li>
 *   <li>Any allocations that were skipped due to capacity constraints</li>
 * </ul>
 * <p>
 * The plan provides methods to generate summary and detailed reports for pickers and planners.
 */
public class PickingPlan {

    private PackingHeuristic heuristic;
    private double trolleyCapacity;
    private List<Trolley> trolleys;
    private int totalAllocations;
    private double totalWeight;
    private List<SkippedAllocation> skippedAllocations;

    /**
     * Constructs a new empty picking plan with the specified heuristic and trolley capacity.
     *
     * @param heuristic the packing heuristic used to create this plan
     * @param trolleyCapacity the maximum weight capacity of each trolley in kilograms
     */
    public PickingPlan(PackingHeuristic heuristic, double trolleyCapacity) {
        this.heuristic = heuristic;
        this.trolleyCapacity = trolleyCapacity;
        this.trolleys = new ArrayList<>();
        this.totalAllocations = 0;
        this.totalWeight = 0.0;
        this.skippedAllocations = new ArrayList<>();
    }

    /**
     * Gets the packing heuristic used for this plan.
     *
     * @return the packing heuristic
     */
    public PackingHeuristic getHeuristic() {
        return heuristic;
    }

    /**
     * Gets the trolley capacity for this plan.
     *
     * @return the trolley capacity in kilograms
     */
    public double getTrolleyCapacity() {
        return trolleyCapacity;
    }

    /**
     * Gets a copy of the list of trolleys in this plan.
     *
     * @return a new list containing all trolleys
     */
    public List<Trolley> getTrolleys() {
        return new ArrayList<>(trolleys);
    }


    /**
     * Gets the number of trolleys in this plan.
     *
     * @return the trolley count
     */
    public int getTrolleyCount() {
        return trolleys.size();
    }

    /**
     * Gets the total number of allocations across all trolleys.
     *
     * @return the total allocation count
     */
    public int getTotalAllocations() {
        return totalAllocations;
    }

    /**
     * Gets the total weight of all allocations across all trolleys.
     *
     * @return the total weight in kilograms
     */
    public double getTotalWeight() {
        return totalWeight;
    }

    /**
     * Gets a copy of the list of skipped allocations.
     *
     * @return a new list containing all skipped allocations
     */
    public List<SkippedAllocation> getSkippedAllocations() {
        return new ArrayList<>(skippedAllocations);
    }

    /**
     * Gets the number of skipped allocations.
     *
     * @return the skipped allocation count
     */
    public int getSkippedCount() {
        return skippedAllocations.size();
    }

    /**
     * Calculates the average utilization across all trolleys.
     *
     * @return the average utilization as a percentage (0-100), or 0 if no trolleys
     */
    public double getAverageUtilization() {
        if (trolleys.isEmpty()) return 0.0;
        return trolleys.stream()
                .mapToDouble(Trolley::getUtilization)
                .average()
                .orElse(0.0);
    }

    /**
     * Sets the packing heuristic.
     *
     * @param heuristic the heuristic to set
     */
    public void setHeuristic(PackingHeuristic heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * Sets the trolley capacity.
     *
     * @param trolleyCapacity the capacity in kilograms
     */
    public void setTrolleyCapacity(double trolleyCapacity) {
        this.trolleyCapacity = trolleyCapacity;
    }

    /**
     * Replaces the list of trolleys and recalculates metrics.
     *
     * @param trolleys the new list of trolleys
     */
    public void setTrolleys(List<Trolley> trolleys) {
        this.trolleys = trolleys;
        recalculateMetrics();
    }

    /**
     * Adds a trolley to the plan and updates metrics.
     *
     * @param trolley the trolley to add
     */
    public void addTrolley(Trolley trolley) {
        trolleys.add(trolley);
        totalAllocations += trolley.getAllocationCount();
        totalWeight += trolley.getUsedWeight();
    }

    /**
     * Records an allocation that was skipped due to capacity constraints.
     *
     * @param allocation the allocation that was skipped
     * @param weight the weight of the skipped allocation in kilograms
     */
    public void addSkippedAllocation(Allocation allocation, double weight) {
        skippedAllocations.add(new SkippedAllocation(allocation, weight));
    }

    /**
     * Recalculates total allocations and total weight from the current trolley list.
     */
    private void recalculateMetrics() {
        totalAllocations = trolleys.stream()
                .mapToInt(Trolley::getAllocationCount)
                .sum();
        totalWeight = trolleys.stream()
                .mapToDouble(Trolley::getUsedWeight)
                .sum();
    }

    /**
     * Generates a summary report of the picking plan.
     * <p>
     * The report includes:
     * <ul>
     *   <li>Heuristic used</li>
     *   <li>Trolley capacity</li>
     *   <li>Total number of trolleys</li>
     *   <li>Number of skipped allocations</li>
     *   <li>Utilization of each trolley</li>
     *   <li>Details of skipped allocations (if any)</li>
     * </ul>
     *
     * @return a formatted summary report string
     */
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

    /**
     * Generates a detailed picking list for warehouse pickers.
     * <p>
     * For each trolley, the report includes:
     * <ul>
     *   <li>Trolley number and utilization</li>
     *   <li>For each allocation: order ID, line number, aisle, bay number, box ID, SKU, quantity</li>
     *   <li>Indication of partial allocations</li>
     * </ul>
     * Also includes details of any skipped allocations.
     *
     * @return a formatted detailed picking list string
     */
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

    /**
     * Returns a concise string representation of the picking plan.
     *
     * @return a string containing key metrics of the plan
     */
    @Override
    public String toString() {
        return "PickingPlan: " + heuristic + ", trolleys=" + getTrolleyCount() + ", allocations=" + totalAllocations + ", skipped=" + getSkippedCount() + ", avgUtil=" + String.format("%.2f", getAverageUtilization()) + "%";
    }

    /**
     * Inner class representing an allocation that was skipped during planning.
     * <p>
     * Skipped allocations occur when an allocation cannot fit in any trolley
     * and splitting is not enabled or not possible.
     */
    public static class SkippedAllocation {
        public final Allocation allocation;
        public final double weight;

        /**
         * Constructs a skipped allocation record.
         *
         * @param allocation the allocation that was skipped
         * @param weight the weight of the allocation in kilograms
         */
        public SkippedAllocation(Allocation allocation, double weight) {
            this.allocation = allocation;
            this.weight = weight;
        }
    }
}