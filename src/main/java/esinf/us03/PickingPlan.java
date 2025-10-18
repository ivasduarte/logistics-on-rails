package esinf.us03;

import esinf.us02.Allocation;
import java.util.ArrayList;
import java.util.List;

public class PickingPlan {

    private PackingHeuristic heuristic;
    private double trolleyCapacity;
    private List<Trolley> trolleys;
    private int totalAllocations;
    private double totalWeight;
    private List<SkippedAllocation> skippedAllocations;

    public PickingPlan(PackingHeuristic heuristic, double trolleyCapacity) {
        this.heuristic = heuristic;
        this.trolleyCapacity = trolleyCapacity;
        this.trolleys = new ArrayList<>();
        this.totalAllocations = 0;
        this.totalWeight = 0.0;
        this.skippedAllocations = new ArrayList<>();
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

    public List<SkippedAllocation> getSkippedAllocations() {
        return new ArrayList<>(skippedAllocations);
    }

    public int getSkippedCount() {
        return skippedAllocations.size();
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

    public void addSkippedAllocation(Allocation allocation, double weight) {
        skippedAllocations.add(new SkippedAllocation(allocation, weight));
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

    @Override
    public String toString() {
        return "PickingPlan: " + heuristic + ", trolleys=" + getTrolleyCount() + ", allocations=" + totalAllocations + ", skipped=" + getSkippedCount() + ", avgUtil=" + String.format("%.2f", getAverageUtilization()) + "%";
    }

    public static class SkippedAllocation {
        public final Allocation allocation;
        public final double weight;

        public SkippedAllocation(Allocation allocation, double weight) {
            this.allocation = allocation;
            this.weight = weight;
        }
    }
}