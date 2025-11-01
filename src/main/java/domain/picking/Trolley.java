/**
 * Trolley.java
 */
package domain.picking;

import domain.order.Allocation;
import java.util.ArrayList;
import java.util.List;
/**
 * Represents a trolley (cart) used for picking items in a warehouse.
 * <p>
 * A trolley has:
 * <ul>
 *   <li>A unique identifying number</li>
 *   <li>A maximum weight capacity in kilograms</li>
 *   <li>A collection of allocations assigned to it</li>
 *   <li>Tracking of used weight and remaining capacity</li>
 *   <li>A log of operations for audit purposes</li>
 * </ul>
 * <p>
 * The trolley manages allocations through AllocationEntry objects that track
 * whether an allocation is partial (split due to capacity constraints) and
 * maintains the original quantity for reporting purposes.
 * <p>
 * Utilization is calculated as the percentage of capacity used: (usedWeight / capacityWeight) * 100.
 */
public class Trolley {

    private int trolleyNumber;
    private double capacityWeight;
    private double usedWeight;
    private List<AllocationEntry> allocationEntries;
    private List<String> logs;

    /**
     * Constructs a new trolley with the specified number and capacity.
     * <p>
     * The trolley starts empty with zero used weight.
     *
     * @param trolleyNumber the unique identifier for this trolley
     * @param capacityWeight the maximum weight capacity in kilograms
     */
    public Trolley(int trolleyNumber, double capacityWeight) {
        this.trolleyNumber = trolleyNumber;
        this.capacityWeight = capacityWeight;
        this.usedWeight = 0.0;
        this.allocationEntries = new ArrayList<>();
        this.logs = new ArrayList<>();
    }

    /**
     * Gets the trolley number.
     *
     * @return the trolley's unique identifier
     */
    public int getTrolleyNumber() {
        return trolleyNumber;
    }

    /**
     * Gets the maximum weight capacity of this trolley.
     *
     * @return the capacity in kilograms
     */
    public double getCapacityWeight() {
        return capacityWeight;
    }

    /**
     * Gets the current used weight of this trolley.
     *
     * @return the used weight in kilograms
     */
    public double getUsedWeight() {
        return usedWeight;
    }

    /**
     * Gets the remaining capacity of this trolley.
     *
     * @return the remaining capacity in kilograms (capacityWeight - usedWeight)
     */
    public double getRemainingCapacity() {
        return capacityWeight - usedWeight;
    }

    /**
     * Gets a list of all allocations in this trolley.
     * <p>
     * Returns only the Allocation objects without partial information.
     * Use getAllocationEntries() to get full metadata including partial status.
     *
     * @return a new list containing all allocations
     */
    public List<Allocation> getAllocations() {
        List<Allocation> allocations = new ArrayList<>();
        for (AllocationEntry entry : allocationEntries) {
            allocations.add(entry.allocation);
        }
        return allocations;
    }

    /**
     * Gets a list of all allocation entries with full metadata.
     * <p>
     * Each entry includes the allocation, weight, partial status, and original quantity.
     *
     * @return a new list containing all allocation entries
     */
    public List<AllocationEntry> getAllocationEntries() {
        return new ArrayList<>(allocationEntries);
    }

    /**
     * Gets a copy of the operation logs for this trolley.
     *
     * @return a new list containing all log messages
     */
    public List<String> getLogs() {
        return new ArrayList<>(logs);
    }

    /**
     * Calculates the utilization percentage of this trolley.
     *
     * @return the utilization as a percentage (0-100), calculated as (usedWeight / capacityWeight) * 100
     */
    public double getUtilization() {
        return capacityWeight > 0 ? (usedWeight / capacityWeight) * 100 : 0;
    }

    /**
     * Sets the trolley number.
     *
     * @param trolleyNumber the new trolley number
     */
    public void setTrolleyNumber(int trolleyNumber) {
        this.trolleyNumber = trolleyNumber;
    }

    /**
     * Sets the capacity weight.
     *
     * @param capacityWeight the new capacity in kilograms
     */
    public void setCapacityWeight(double capacityWeight) {
        this.capacityWeight = capacityWeight;
    }

    /**
     * Attempts to add an allocation to this trolley as a complete (non-partial) allocation.
     * <p>
     * This is a convenience method that calls addAllocation(allocation, weight, false, null).
     *
     * @param allocation the allocation to add
     * @param weight the weight of the allocation in kilograms
     * @return true if the allocation was added successfully, false if it doesn't fit
     */
    public boolean addAllocation(Allocation allocation, double weight) {
        return addAllocation(allocation, weight, false, null);
    }

    /**
     * Attempts to add an allocation to this trolley with full metadata.
     * <p>
     * The allocation is added only if its weight fits within the remaining capacity.
     * When added, the used weight is updated and a log entry is created for partial allocations.
     *
     * @param allocation the allocation to add
     * @param weight the weight of the allocation in kilograms
     * @param isPartial true if this is a partial allocation (split from a larger allocation)
     * @param originalQuantity the original quantity before splitting (null if not partial)
     * @return true if the allocation was added successfully, false if it doesn't fit
     */
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

    /**
     * Adds a log message to this trolley's operation log.
     *
     * @param logMessage the message to log
     */
    public void addLog(String logMessage) {
        logs.add(logMessage);
    }

    /**
     * Checks if an allocation of the specified weight can fit in this trolley.
     *
     * @param weight the weight to check in kilograms
     * @return true if the weight fits within remaining capacity, false otherwise
     */
    public boolean canFit(double weight) {
        return weight <= getRemainingCapacity();
    }

    /**
     * Gets the number of allocations in this trolley.
     *
     * @return the count of allocation entries
     */
    public int getAllocationCount() {
        return allocationEntries.size();
    }

    /**
     * Checks if this trolley is empty (has no allocations).
     *
     * @return true if the trolley has no allocations, false otherwise
     */
    public boolean isEmpty() {
        return allocationEntries.isEmpty();
    }

    /**
     * Returns a string representation of this trolley.
     * <p>
     * The format includes:
     * <ul>
     *   <li>Trolley number</li>
     *   <li>Used weight / capacity weight</li>
     *   <li>Utilization percentage</li>
     *   <li>Number of allocations</li>
     * </ul>
     *
     * @return a formatted string representation of the trolley
     */
    @Override
    public String toString() {
        return "Trolley " + trolleyNumber + ": " +
                String.format("%.2f", usedWeight) + "/" + capacityWeight +
                " kg (" + String.format("%.2f", getUtilization()) + "% full) - " +
                allocationEntries.size() + " allocation(s)";
    }

    /**
     * Inner class representing an allocation entry in a trolley.
     * <p>
     * An AllocationEntry contains:
     * <ul>
     *   <li>The allocation itself</li>
     *   <li>The weight of the allocation</li>
     *   <li>Whether it's a partial allocation (split due to capacity)</li>
     *   <li>The original quantity before splitting (if partial)</li>
     * </ul>
     * <p>
     * This immutable record helps track partial allocations for reporting purposes,
     * allowing the system to show pickers that an allocation was split (e.g., "3/5"
     * means 3 units picked from an original 5).
     */
    public static class AllocationEntry {
        public final Allocation allocation;
        public final double weight;
        public final boolean isPartial;
        public final Integer originalQuantity;

        /**
         * Constructs an allocation entry with full metadata.
         *
         * @param allocation the allocation
         * @param weight the weight in kilograms
         * @param isPartial true if this is a partial allocation
         * @param originalQuantity the original quantity before splitting (null if not partial)
         */
        public AllocationEntry(Allocation allocation, double weight, boolean isPartial, Integer originalQuantity) {
            this.allocation = allocation;
            this.weight = weight;
            this.isPartial = isPartial;
            this.originalQuantity = originalQuantity;
        }
    }
}