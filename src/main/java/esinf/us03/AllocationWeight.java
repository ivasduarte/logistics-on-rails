package esinf.us03;

import esinf.us02.Allocation;

public class AllocationWeight implements Comparable<AllocationWeight> {

    private Allocation allocation;
    private double weight;
    private boolean isPartial;
    private Integer originalQuantity;

    public AllocationWeight(Allocation allocation, double weight) {
        this.allocation = allocation;
        this.weight = weight;
        this.isPartial = false;
        this.originalQuantity = null;
    }

    public AllocationWeight(Allocation allocation, double weight, boolean isPartial, Integer originalQuantity) {
        this.allocation = allocation;
        this.weight = weight;
        this.isPartial = isPartial;
        this.originalQuantity = originalQuantity;
    }

    public Allocation getAllocation() {
        return allocation;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isPartial() {
        return isPartial;
    }

    public Integer getOriginalQuantity() {
        return originalQuantity;
    }

    public void setAllocation(Allocation allocation) {
        this.allocation = allocation;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setPartial(boolean partial) {
        isPartial = partial;
    }

    public void setOriginalQuantity(Integer originalQuantity) {
        this.originalQuantity = originalQuantity;
    }

    @Override
    public int compareTo(AllocationWeight other) {
        return Double.compare(other.weight, this.weight);
    }

    @Override
    public String toString() {
        String partialInfo = isPartial ? " (PARTIAL: " + allocation.getQuantityAllocated() + "/" + originalQuantity + ")" : "";
        return "AllocationWeight: " + allocation.getBoxID() + ", weight=" + weight + " kg" + partialInfo;
    }
}