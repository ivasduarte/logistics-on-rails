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

    public void addAllocation(Allocation allocation) {
        this.allocations.add(allocation);
        this.allocatedQty += allocation.getQuantityAllocated();
    }

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

    public boolean isFullySatisfied() {
        return allocatedQty >= requestedQty;
    }

    public boolean hasAllocations() {
        return allocatedQty > 0;
    }

    @Override
    public String toString() {
        return String.format("Line %d [%s]: %d/%d allocated (%s) - %d allocation(s)",
                lineNo, sku, allocatedQty, requestedQty, status, allocations.size()); // Alterar posteriormente
    }

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
