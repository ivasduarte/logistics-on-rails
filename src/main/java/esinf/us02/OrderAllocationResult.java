package esinf.us02;

import java.util.*;
import java.util.stream.Collectors;

public class OrderAllocationResult {

    private String orderID;

    private List<OrderLineResult> lineResults;
    private List<Allocation> allAllocations;

    private Map<Integer, LineStatus> perLineStatuses;

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

    public void addLineResult(OrderLineResult lineResult) {
        this.lineResults.add(lineResult);
        this.perLineStatuses.put(lineResult.getLineNo(), lineResult.getStatus());
        this.allAllocations.addAll(lineResult.getAllocations());
    }

    public OrderLineResult getLineResult(int lineNo) {
        return lineResults.stream().filter(lr -> lr.getLineNo() == lineNo).findFirst().orElse(null);
    }

    public LineStatus getLineStatus(int lineNo) {
        return perLineStatuses.get(lineNo);
    }

    public boolean isFullyEligible() {
        return lineResults.stream().allMatch(lr -> lr.getStatus() == LineStatus.ELIGIBLE);
    }

    public boolean hasPartialLines() {
        return lineResults.stream().anyMatch(lr -> lr.getStatus() == LineStatus.PARTIAL);
    }

    public boolean hasUndispatchableLines() {
        return lineResults.stream().anyMatch(lr -> lr.getStatus() == LineStatus.UNDISPATCHABLE);
    }

    public List<OrderLineResult> getLinesByStatus(LineStatus status) {
        return lineResults.stream().filter(lr -> lr.getStatus() == status).collect(Collectors.toList());
    }

    public int getTotalAllocationCount() {
        return allAllocations.size();
    }

    public int getTotalAllocatedQuantity() {
        return allAllocations.stream().mapToInt(Allocation::getQuantityAllocated).sum();
    }

    public Map<LineStatus, Long> getStatusSummary() {
        return lineResults.stream().collect(Collectors.groupingBy(OrderLineResult::getStatus, Collectors.counting()));
    }

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

    @Override
    public String toString() {
        return String.format("OrderAllocationResult[order=%s, lines=%d, allocations=%d, eligible=%s]",
                orderID, lineResults.size(), allAllocations.size(), isFullyEligible());
    }

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
