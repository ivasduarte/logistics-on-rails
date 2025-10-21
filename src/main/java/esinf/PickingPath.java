package esinf;

import esinf.us02.Allocation;
import esinf.us03.PickingPlan;
import esinf.us03.Trolley;

import java.util.*;

public class PickingPath {

    private final List<Trolley> trolleys;

    public PickingPath(PickingPlan plan) {
        trolleys = plan.getTrolleys();
    }

    public List<Trolley> getTrolleys() {
        return trolleys;
    }

    public List<Allocation> getTrolleyAllocations(Trolley trolley){
        return trolley.getAllocations();
    }

    public int minimizeDistanceAscending(List<Allocation> allocations) {
        if (allocations.isEmpty()) return 0;

        // Merge duplicate bays first
        List<Allocation> merged = mergeDuplicateBays(allocations);

        // Order bays in ascending order from first to last aisle and first to last bay
        merged.sort(Comparator
                .comparingInt(Allocation::getAisle)
                .thenComparingInt(Allocation::getBayNumber));

        int currentAisle = 0;
        int currentBay = 0;
        int totalDistance = 0;

        // Go through every bay to calculate the distance in ascending order
        for (Allocation allocation : merged) {
            totalDistance += calculateDistance(currentAisle, currentBay, allocation);
            currentAisle = allocation.getAisle();
            currentBay = allocation.getBayNumber();
        }

        return totalDistance;
    }

    public int minimizeDistanceNearest(List<Allocation> allocations) {
        if (allocations.isEmpty()) return 0;

        // Merge duplicate bays first
        List<Allocation> merged = mergeDuplicateBays(allocations);

        List<Allocation> unvisited = new ArrayList<>(merged);

        int currentAisle = 0;
        int currentBay = 0;
        int totalDistance = 0;

        // Check for unvisited bays to calculate the distance from the current bay to the nearest one
        while (!unvisited.isEmpty()) {
            Allocation nearest = findNearestAllocation(currentAisle, currentBay, unvisited);
            totalDistance += calculateDistance(currentAisle, currentBay, nearest);

            // Replaces current aisle and current bay to the new values
            currentAisle = nearest.getAisle();
            currentBay = nearest.getBayNumber();

            // Removes bay from the unvisited list
            unvisited.remove(nearest);
        }

        return totalDistance;
    }

    private List<Allocation> mergeDuplicateBays(List<Allocation> allocations) {
        Map<String, Allocation> merged = new LinkedHashMap<>();

        for (Allocation alloc : allocations) {
            String key = alloc.getAisle() + "-" + alloc.getBayNumber();
            if (merged.containsKey(key)) {
                // Sum the quantities for the same bay
                Allocation existing = merged.get(key);
                existing.setQuantityAllocated(existing.getQuantityAllocated() + alloc.getQuantityAllocated());
            } else {
                // Creates a copy to avoid modifying the original
                Allocation copy = new Allocation(
                        alloc.getOrderID(),
                        alloc.getBoxID(),
                        alloc.getWarehouseId(),
                        alloc.getSku(),
                        alloc.getLineNo(),
                        alloc.getAisle(),
                        alloc.getBayNumber(),
                        alloc.getQuantityAllocated()
                );
                merged.put(key, copy);
            }
        }

        return new ArrayList<>(merged.values());
    }

    private Allocation findNearestAllocation(int currentAisle, int currentBay, List<Allocation> unvisited) {
        Allocation nearest = null;
        int minDistance = Integer.MAX_VALUE;

        // Finds the nearest bay and calculates its distance
        for (Allocation alloc : unvisited) {
            int dist = calculateDistance(currentAisle, currentBay, alloc);
            if (dist < minDistance) {
                minDistance = dist;
                nearest = alloc;
            }
        }

        return nearest;
    }

    private int calculateDistance(int fromAisle, int fromBay, Allocation to) {
        // Returns the distance based on the formula given in the assignment
        return fromBay + Math.abs(fromAisle - to.getAisle()) * 3 + to.getBayNumber();
    }
}
