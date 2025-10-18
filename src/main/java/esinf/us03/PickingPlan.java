package esinf.us03;

import java.util.ArrayList;
import java.util.List;

public class PickingPlan {

    private PackingHeuristic heuristic;
    private double trolleyCapacity;
    private List<Trolley> trolleys;
    private int totalAllocations;
    private double totalWeight;

    public PickingPlan(PackingHeuristic heuristic, double trolleyCapacity) {
        this.heuristic = heuristic;
        this.trolleyCapacity = trolleyCapacity;
        this.trolleys = new ArrayList<>();
        this.totalAllocations = 0;
        this.totalWeight = 0.0;
    }

    public PackingHeuristic getHeuristic() {
        return heuristic;
    }

    public double getTrolleyCapacity() {
        return trolleyCapacity;
    }

    public List<Trolley> getTrolleys() {
        return new ArrayList<>(trolleys);
    }

    public int getTrolleyCount() {
        return trolleys.size();
    }

    public int getTotalAllocations() {
        return totalAllocations;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public double getAverageUtilization() {
        if (trolleys.isEmpty()) return 0.0;
        return trolleys.stream()
                .mapToDouble(Trolley::getUtilization)
                .average()
                .orElse(0.0);
    }

    public void setHeuristic(PackingHeuristic heuristic) {
        this.heuristic = heuristic;
    }

    public void setTrolleyCapacity(double trolleyCapacity) {
        this.trolleyCapacity = trolleyCapacity;
    }

    public void setTrolleys(List<Trolley> trolleys) {
        this.trolleys = trolleys;
        recalculateMetrics();
    }

    public void addTrolley(Trolley trolley) {
        trolleys.add(trolley);
        totalAllocations += trolley.getAllocationCount();
        totalWeight += trolley.getUsedWeight();
    }

    private void recalculateMetrics() {
        totalAllocations = trolleys.stream()
                .mapToInt(Trolley::getAllocationCount)
                .sum();
        totalWeight = trolleys.stream()
                .mapToDouble(Trolley::getUsedWeight)
                .sum();
    }

    public String generateSummaryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Picking Plan Summary ===\n");
        sb.append(String.format("Heuristic: %s\n", heuristic));
        sb.append(String.format("Trolley Capacity: %.2f kg\n", trolleyCapacity));
        sb.append(String.format("Number of Trolleys: %d\n", getTrolleyCount()));
        sb.append(String.format("Total Allocations: %d\n", totalAllocations));
        sb.append(String.format("Total Weight: %.2f kg\n", totalWeight));
        sb.append(String.format("Average Utilization: %.1f%%\n\n", getAverageUtilization()));

        sb.append("Trolley Details:\n");
        for (Trolley trolley : trolleys) {
            sb.append("  ").append(trolley.toString()).append("\n");
        }

        return sb.toString();
    }

    public String generateDetailedPickingList() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== Detailed Picking Plan (%s) ===\n\n", heuristic));

        for (Trolley trolley : trolleys) {
            sb.append(String.format("Trolley %d (%.1f%% full - %.2f/%.2f kg):\n",
                    trolley.getTrolleyNumber(),
                    trolley.getUtilization(),
                    trolley.getUsedWeight(),
                    trolley.getCapacityWeight()));

            sb.append(String.format("%-10s %-8s %-10s %-8s %-10s %-10s %-8s\n",
                    "Order ID", "Line No", "Box ID", "Aisle", "Bay", "SKU", "Quantity"));
            sb.append("-".repeat(80)).append("\n");

            for (var allocation : trolley.getAllocations()) {
                sb.append(String.format("%-10s %-8d %-10s %-8d %-10d %-10s %-8d\n",
                        allocation.getOrderID(),
                        allocation.getLineNo(),
                        allocation.getBoxID(),
                        allocation.getAisle(),
                        allocation.getBayNumber(),
                        allocation.getSku(),
                        allocation.getQuantityAllocated()));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("PickingPlan[%s, trolleys=%d, allocations=%d, avgUtil=%.1f%%]",
                heuristic, getTrolleyCount(), totalAllocations, getAverageUtilization());
    }
}