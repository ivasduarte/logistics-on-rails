package esinf.us03;

import esinf.us02.Allocation;
import java.util.ArrayList;
import java.util.List;

public class Trolley {

    private int trolleyNumber;
    private double capacityWeight;
    private double usedWeight;
    private List<Allocation> allocations;

    public Trolley(int trolleyNumber, double capacityWeight) {
        this.trolleyNumber = trolleyNumber;
        this.capacityWeight = capacityWeight;
        this.usedWeight = 0.0;
        this.allocations = new ArrayList<>();
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
        return new ArrayList<>(allocations);
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
        if (weight <= getRemainingCapacity()) {
            allocations.add(allocation);
            usedWeight += weight;
            return true;
        }
        return false;
    }

    public boolean canFit(double weight) {
        return weight <= getRemainingCapacity();
    }

    public int getAllocationCount() {
        return allocations.size();
    }

    public boolean isEmpty() {
        return allocations.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Trolley %d: %.2f/%.2f kg (%.1f%% full) - %d allocation(s)",
                trolleyNumber, usedWeight, capacityWeight, getUtilization(), allocations.size());
    }
}