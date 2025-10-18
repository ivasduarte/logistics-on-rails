package esinf.us03;

import esinf.us02.Allocation;
import java.util.ArrayList;
import java.util.List;

public class Trolley {

    private int trolleyNumber;
    private double capacityWeight;
    private double usedWeight;
    private List<AllocationEntry> allocationEntries;
    private List<String> logs;

    public Trolley(int trolleyNumber, double capacityWeight) {
        this.trolleyNumber = trolleyNumber;
        this.capacityWeight = capacityWeight;
        this.usedWeight = 0.0;
        this.allocationEntries = new ArrayList<>();
        this.logs = new ArrayList<>();
    }

    public int getTrolleyNumber() {
        return trolleyNumber;
    }

    public double getCapacityWeight() {
        return capacityWeight;
    }

    public double getUsedWeight() {
        return usedWeight;
    }

    public double getRemainingCapacity() {
        return capacityWeight - usedWeight;
    }

    public List<Allocation> getAllocations() {
        List<Allocation> allocations = new ArrayList<>();
        for (AllocationEntry entry : allocationEntries) {
            allocations.add(entry.allocation);
        }
        return allocations;
    }

    public List<AllocationEntry> getAllocationEntries() {
        return new ArrayList<>(allocationEntries);
    }

    public List<String> getLogs() {
        return new ArrayList<>(logs);
    }

    public double getUtilization() {
        return capacityWeight > 0 ? (usedWeight / capacityWeight) * 100 : 0;
    }

    public void setTrolleyNumber(int trolleyNumber) {
        this.trolleyNumber = trolleyNumber;
    }

    public void setCapacityWeight(double capacityWeight) {
        this.capacityWeight = capacityWeight;
    }

    public boolean addAllocation(Allocation allocation, double weight) {
        return addAllocation(allocation, weight, false, null);
    }

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

    public void addLog(String logMessage) {
        logs.add(logMessage);
    }

    public boolean canFit(double weight) {
        return weight <= getRemainingCapacity();
    }

    public int getAllocationCount() {
        return allocationEntries.size();
    }

    public boolean isEmpty() {
        return allocationEntries.isEmpty();
    }

    @Override
    public String toString() {
        return "Trolley " + trolleyNumber + ": " +
                String.format("%.2f", usedWeight) + "/" + capacityWeight +
                " kg (" + String.format("%.2f", getUtilization()) + "% full) - " +
                allocationEntries.size() + " allocation(s)";
    }

    public static class AllocationEntry {
        public final Allocation allocation;
        public final double weight;
        public final boolean isPartial;
        public final Integer originalQuantity;

        public AllocationEntry(Allocation allocation, double weight, boolean isPartial, Integer originalQuantity) {
            this.allocation = allocation;
            this.weight = weight;
            this.isPartial = isPartial;
            this.originalQuantity = originalQuantity;
        }
    }
}