/**
 * AllocationWeight.java
 */
package domain.order;

/**
 * Represents an allocation with its associated weight information.
 * This class wraps an Allocation object and adds weight-related data
 * necessary for the packing heuristics to make decisions about trolley assignment.
 * <p>
 * The class implements Comparable to allow sorting allocations by weight in descending order,
 * which is required for the First Fit Decreasing (FFD) and Best Fit Decreasing (BFD) heuristics.
 * <p>
 * Supports partial allocations, where only a portion of the original quantity is assigned
 * to a trolley due to capacity constraints.
 */
public class AllocationWeight implements Comparable<AllocationWeight> {

    private Allocation allocation;
    private double weight;
    private boolean isPartial;
    private Integer originalQuantity;
    /**
     * Constructs an AllocationWeight with the specified allocation and weight.
     * The allocation is not marked as partial.
     *
     * @param allocation the allocation to wrap
     * @param weight the total weight of the allocation in kilograms
     */
    public AllocationWeight(Allocation allocation, double weight) {
        this.allocation = allocation;
        this.weight = weight;
        this.isPartial = false;
        this.originalQuantity = null;
    }

    /**
     * Constructs an AllocationWeight with full data including partial allocation information.
     *
     * @param allocation the allocation to wrap
     * @param weight the total weight of the allocation in kilograms
     * @param isPartial true if this represents a partial allocation
     * @param originalQuantity the original quantity before splitting (null if not partial)
     */
    public AllocationWeight(Allocation allocation, double weight, boolean isPartial, Integer originalQuantity) {
        this.allocation = allocation;
        this.weight = weight;
        this.isPartial = isPartial;
        this.originalQuantity = originalQuantity;
    }

    /**
     * Gets the wrapped allocation.
     *
     * @return the allocation
     */
    public Allocation getAllocation() {
        return allocation;
    }

    /**
     * Gets the total weight of this allocation.
     *
     * @return the weight in kilograms
     */
    public double getWeight() {
        return weight;
    }


    /**
     * Checks if this allocation is partial (split due to capacity constraints).
     *
     * @return true if partial
     */
    public boolean isPartial() {
        return isPartial;
    }

    /**
     * Gets the original quantity before splitting.
     *
     * @return the original quantity, or null if this is not a partial allocation
     */
    public Integer getOriginalQuantity() {
        return originalQuantity;
    }

    /**
     * Sets the allocation.
     *
     * @param allocation the allocation to set
     */
    public void setAllocation(Allocation allocation) {
        this.allocation = allocation;
    }

    /**
     * Sets the weight.
     *
     * @param weight the weight in kilograms
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Sets the partial flag.
     *
     * @param partial true if this is a partial allocation
     */
    public void setPartial(boolean partial) {
        isPartial = partial;
    }

    /**
     * Sets the original quantity.
     *
     * @param originalQuantity the original quantity before splitting
     */
    public void setOriginalQuantity(Integer originalQuantity) {
        this.originalQuantity = originalQuantity;
    }

    /**
     * Compares this allocation with another one, based on weight in descending order.
     * Heavier allocations are considered "less than" lighter ones to achieve
     * descending sort order when using Collections.sort().
     *
     * @param other the allocation to compare to
     * @return a negative integer, zero, or a positive integer as this allocation
     *         is heavier than, equal to, or lighter than the specified allocation
     */
    @Override
    public int compareTo(AllocationWeight other) {
        return Double.compare(other.weight, this.weight);
    }

    /**
     * Returns a string representation of this allocation weight.
     * Includes partial allocation information if applicable.
     *
     * @return a string representation including box ID, weight, and partial status
     */
    @Override
    public String toString() {
        String partialInfo = isPartial ? " (PARTIAL: " + allocation.getQuantityAllocated() + "/" + originalQuantity + ")" : "";
        return "AllocationWeight: " + allocation.getBoxID() + ", weight=" + weight + " kg" + partialInfo;
    }
}