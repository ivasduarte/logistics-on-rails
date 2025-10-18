package esinf.us03;

import esinf.us02.Allocation;

public class AllocationWeight implements Comparable<AllocationWeight> {

    private Allocation allocation;
    private double weight;

    public AllocationWeight(Allocation allocation, double weight) {
        this.allocation = allocation;
        this.weight = weight;
    }

    public Allocation getAllocation() {
        return allocation;
    }

    public double getWeight() {
        return weight;
    }

    public void setAllocation(Allocation allocation) {
        this.allocation = allocation;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(AllocationWeight other) {
        return Double.compare(other.weight, this.weight);
    }

    @Override
    public String toString() {
        return String.format("AllocationWeight[%s, weight=%.2f kg]",
                allocation.getBoxID(), weight);
    }
}