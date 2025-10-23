package esinf.us02;

import java.util.*;
import java.util.stream.Collectors;

public class OrderAllocationResult {

    private String orderID;

    private List<OrderLineResult> lineResults;
    private List<Allocation> allAllocations;

    private Map<Integer, LineStatus> perLineStatuses;

    /**
     * Creates a new order allocation result for the specified order.
     * All internal collections are initialized as empty.
     *
     * @param orderID the identifier of the order
     */
    public OrderAllocationResult(String orderID) {
        this.orderID = orderID;

        this.lineResults = new ArrayList<>();
        this.allAllocations = new ArrayList<>();
        this.perLineStatuses = new HashMap<>();
    }

    // Getters
    public String getOrderID() {
        return orderID;
    }
    public List<OrderLineResult> getLineResults() {
        return lineResults;
    }
    public List<Allocation> getAllAllocations() {
        return allAllocations;
    }
    public Map<Integer, LineStatus> getPerLineStatuses() {
        return perLineStatuses;
    }

    // Setters
    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }
    public void setLineResults(List<OrderLineResult> lineResults) {
        this.lineResults = lineResults;
    }
    public void setAllAllocations(List<Allocation> allAllocations) {
        this.allAllocations = allAllocations;
    }
    public void setPerLineStatuses(Map<Integer, LineStatus> perLineStatuses) {
        this.perLineStatuses = perLineStatuses;
    }

    /**
     * Adds a line result to the complete order allocation result.
     * Automatically updates the per-line statuses and consolidates allocations
     * from the line result into the global allocations list.
     *
     * @param lineResult the line result to be added
     */
    public void addLineResult(OrderLineResult lineResult) {
        this.lineResults.add(lineResult);
        this.perLineStatuses.put(lineResult.getLineNo(), lineResult.getStatus());
        this.allAllocations.addAll(lineResult.getAllocations());
    }

    /**
     * Returns the result for a specific line result.
     *
     * @param lineNo the line number to find
     *
     * @return the results of the specified line (null if line is not found)
     */
    public OrderLineResult getLineResult(int lineNo) {
        return lineResults.stream().filter(lr -> lr.getLineNo() == lineNo).findFirst().orElse(null);
    }

    /**
     * Gets the status for a specific line number.
     *
     * @param lineNo the line number
     *
     * @return the status of a specific line (null if line is not found)
     */
    public LineStatus getLineStatus(int lineNo) {
        return perLineStatuses.get(lineNo);
    }

    /**
     * Verifies if all the lines in the order have the ELIGIBLE status.
     * An order is fully eligible when every line has been completely allocated.
     *
     * @return true if all lines are eligible
     */
    public boolean isFullyEligible() {
        return lineResults.stream().allMatch(lr -> lr.getStatus() == LineStatus.ELIGIBLE);
    }

    /**
     * Checks if any line in the order has the PARTIAL status.
     * A line is only partial if some, but not all, of its requested quantity has been allocated.
     *
     * @return true if at least one line is partial
     */
    public boolean hasPartialLines() {
        return lineResults.stream().anyMatch(lr -> lr.getStatus() == LineStatus.PARTIAL);
    }

    /**
     * Checks if any line has the UNDISPATCHABLE status.
     * A line is undispatchable when no allocation was made, or when strict mode prevents partial allocations.
     *
     * @return true if at least one line has UNDISPATCHABLE status
     */
    public boolean hasUndispatchableLines() {
        return lineResults.stream().anyMatch(lr -> lr.getStatus() == LineStatus.UNDISPATCHABLE);
    }

    /**
     * Retrieves all lines with a specific status.
     *
     * @param status status to filter by
     *
     * @return list of line results matching the specified status
     */
    public List<OrderLineResult> getLinesByStatus(LineStatus status) {
        return lineResults.stream().filter(lr -> lr.getStatus() == status).collect(Collectors.toList());
    }

    /**
     * Gets the total number of individual allocations made across all order lines.
     * Each allocation represents a quantity taken from a specific box.
     *
     * @return Allocations amount
     */
    public int getTotalAllocationCount() {
        return allAllocations.size();
    }

    /**
     * Get the total quantity allocated across all order lines and all allocations.
     *
     * @return sum of all allocated quantities
     */
    public int getTotalAllocatedQuantity() {
        return allAllocations.stream().mapToInt(Allocation::getQuantityAllocated).sum();
    }

    /**
     * Generates a summary of line statuses showing the count of lines in each status.
     *
     * @return map of LineStatus to count of lines with that status
     */
    public Map<LineStatus, Long> getStatusSummary() {
        return lineResults.stream().collect(Collectors.groupingBy(OrderLineResult::getStatus, Collectors.counting()));
    }

    /**
     * Generates a formatted text report summarizing the allocation results.
     *
     * @return multi-line formatted summary report
     */
    public String generateSummaryReport() { // Alterar posteriormente
        StringBuilder sb = new StringBuilder();
        sb.append("=== Order Allocation Result: ").append(orderID).append(" ===\n");
        sb.append("Total Lines: ").append(lineResults.size()).append("\n");

        Map<LineStatus, Long> summary = getStatusSummary();
        sb.append("Status Summary:\n");
        sb.append("  ELIGIBLE: ").append(summary.getOrDefault(LineStatus.ELIGIBLE, 0L)).append("\n");
        sb.append("  PARTIAL: ").append(summary.getOrDefault(LineStatus.PARTIAL, 0L)).append("\n");
        sb.append("  UNDISPATCHABLE: ").append(summary.getOrDefault(LineStatus.UNDISPATCHABLE, 0L)).append("\n");

        sb.append("Total Allocations: ").append(getTotalAllocationCount()).append("\n");
        sb.append("Total Quantity Allocated: ").append(getTotalAllocatedQuantity()).append("\n\n");

        sb.append("Line Details:\n");
        for (OrderLineResult lr : lineResults) {
            sb.append("  ").append(lr.toString()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Returns a concise string representation of this allocation result.
     * Format: OrderAllocationResult for order 'orderID': lines=count, allocations=count, eligible=boolean.
     *
     * @return string representation of the allocation result
     */
    @Override
    public String toString() {
        return String.format("OrderAllocationResult for order %d: lines=%d, allocations=%d, eligible=%s.", orderID, lineResults.size(), allAllocations.size(), isFullyEligible());
    }

    /**
     * Compares this order allocation result with another object for equality.
     * Two results are equal if they have the same order ID.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderAllocationResult that = (OrderAllocationResult) o;
        return Objects.equals(orderID, that.orderID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderID);
    }
}
