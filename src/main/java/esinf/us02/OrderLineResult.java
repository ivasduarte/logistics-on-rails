package esinf.us02;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrderLineResult {

    private String sku;

    private int lineNo;
    private int requestedQty;
    private int allocatedQty;

    private LineStatus status;

    private List<Allocation> allocations;

    /**
     * Creates a new order line result with the specified parameters.
     * Initial state: no allocations, allocated quantity is 0, status is UNDISPATCHABLE.
     *
     * @param sku the SKU being requested
     * @param lineNo the line number within the order
     * @param requestedQty the quantity requested for this line
     */
    public OrderLineResult(String sku, int lineNo, int requestedQty) {
        this.lineNo = lineNo;
        this.sku = sku;
        this.requestedQty = requestedQty;

        this.allocatedQty = 0;
        this.allocations = new ArrayList<>();
        this.status = LineStatus.UNDISPATCHABLE;
    }

    // Getters
    public String getSku() {
        return sku;
    }
    public int getLineNo() {
        return lineNo;
    }
    public int getRequestedQty() {
        return requestedQty;
    }
    public int getAllocatedQty() {
        return allocatedQty;
    }
    public LineStatus getStatus() {
        return status;
    }
    public List<Allocation> getAllocations() {
        return allocations;
    }

    // Setters
    public void setSku(String sku) {
        this.sku = sku;
    }
    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }
    public void setRequestedQty(int requestedQty) {
        this.requestedQty = requestedQty;
    }
    public void setAllocatedQty(int allocatedQty) {
        this.allocatedQty = allocatedQty;
    }
    public void setStatus(LineStatus status) {
        this.status = status;
    }
    public void setAllocations(List<Allocation> allocations) {
        this.allocations = allocations;
    }

    /**
     * Adds an allocation to this line and automatically updates the allocated quantity.
     * The allocation's quantity is added to the running total of allocated quantity.
     *
     * @param allocation the allocation to add
     */
    public void addAllocation(Allocation allocation) {
        this.allocations.add(allocation);
        this.allocatedQty += allocation.getQuantityAllocated();
    }

    /**
     * Computes and sets the final status based on allocation results and eligibility mode.
     *
     * @param strictMode if true, partial allocations result in UNDISPATCHABLE status;
     *                   if false, partial allocations result in PARTIAL status
     */
    public void computeStatus(boolean strictMode) {
        if (allocatedQty == 0) {
            this.status = LineStatus.UNDISPATCHABLE;
        } else if (allocatedQty >= requestedQty) {
            this.status = LineStatus.ELIGIBLE;
        } else {
            // allocatedQty > 0 but < requestedQty
            if (strictMode) {
                this.status = LineStatus.UNDISPATCHABLE;
            } else {
                this.status = LineStatus.PARTIAL;
            }
        }
    }

    /**
     * Checks if the line has been fully satisfied.
     * A line is fully satisfied when the allocated quantity meets or exceeds
     * the requested quantity.
     *
     * @return true if allocated quantity >= requested quantity, false otherwise
     */
    public boolean isFullySatisfied() {
        return allocatedQty >= requestedQty;
    }

    /**
     * Checks if any allocation has been made for this line.
     * Returns true if at least some quantity has been allocated.
     *
     * @return true if allocated quantity > 0, false otherwise
     */
    public boolean hasAllocations() {
        return allocatedQty > 0;
    }

    /**
     * Returns a formatted string representation of this line result.
     * Format: "Line [lineNo] [SKU]: allocated/requested allocated (STATUS) - N allocation(s)"
     * Example: "Line 1 [SKU-ABC]: 50/100 allocated (PARTIAL) - 3 allocation(s)"
     *
     * @return string representation of the line result
     */
    @Override
    public String toString() {
        return String.format("Line %d [%s]: %d/%d allocated (%s) - %d allocation(s)",
                lineNo, sku, allocatedQty, requestedQty, status, allocations.size());
    }

    /**
     * Compares this line result with another object for equality.
     * Two line results are equal if they have the same line number.
     *
     * @param o the object to compare with
     * @return true if the objects have the same line number, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderLineResult that = (OrderLineResult) o;
        return lineNo == that.lineNo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNo);
    }
}
