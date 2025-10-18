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
        String report = "";
        report += "Picking Plan Summary\n";
        report += "Heuristic: " + heuristic + "\n";
        report += "Trolley Capacity: " + trolleyCapacity + " kg\n";
        report += "Total Number of Trolleys: " + getTrolleyCount() + "\n\n";

        report += "Trolley Utilization:\n";
        for (Trolley trolley : trolleys) {
            report += "Trolley " + trolley.getTrolleyNumber() + ": " + trolley.getUtilization() + "% full\n";
        }

        return report;
    }

    public String generateDetailedPickingList() {
        String report = "";
        report += "Picking Plan: " + heuristic + "\n\n";

        for (Trolley trolley : trolleys) {
            report += "Trolley " + trolley.getTrolleyNumber() + "\n";
            report += "Utilization: " + trolley.getUtilization() + "%\n\n";

            for (var allocation : trolley.getAllocations()) {
                report += "Order ID: " + allocation.getOrderID() + "\n";
                report += "Line Number: " + allocation.getLineNo() + "\n";
                report += "Aisle: " + allocation.getAisle() + "\n";
                report += "Bay Number: " + allocation.getBayNumber() + "\n";
                report += "Box ID: " + allocation.getBoxID() + "\n";
                report += "SKU: " + allocation.getSku() + "\n";
                report += "Quantity: " + allocation.getQuantityAllocated() + "\n";
                report += "\n";
            }
            report += "\n";
        }

        return report;
    }

    @Override
    public String toString() {
        return "PickingPlan: " + heuristic + ", trolleys=" + getTrolleyCount() +
                ", allocations=" + totalAllocations + ", avgUtil=" + getAverageUtilization() + "%";
    }
}