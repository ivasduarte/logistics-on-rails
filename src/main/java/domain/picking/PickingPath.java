package domain.picking;

import domain.order.Allocation;

import java.util.*;

/**
 * Domain class responsible for path calculation and optimization logic.
 */
public class PickingPath {

    /**
     * Calculates the path by ordering allocation points in ascending order
     * based on their aisle and bay numbers.
     *
     * @param allocations the list of {@link Allocation} objects representing the allocation points
     * @return an ordered list of allocations representing the optimized path
     */
    public List<Allocation> getPathAscending(List<Allocation> allocations) {
        if (allocations.isEmpty()) return new ArrayList<>();

        List<Allocation> merged = mergeDuplicateBays(allocations);

        merged.sort(Comparator
                .comparingInt(Allocation::getAisle)
                .thenComparingInt(Allocation::getBayNumber));

        return merged;
    }

    /**
     * Calculates the path by visiting the nearest allocation point each time from the current position.
     *
     * @param allocations the list of Allocation objects representing the allocation points
     * @return an ordered list of allocations representing the path in nearest neighbor order
     */
    public List<Allocation> getPathNearest(List<Allocation> allocations) {
        if (allocations.isEmpty()) return new ArrayList<>();

        List<Allocation> merged = mergeDuplicateBays(allocations);
        List<Allocation> unvisited = new ArrayList<>(merged);
        List<Allocation> path = new ArrayList<>();

        int currentAisle = 0;
        int currentBay = 0;

        while (!unvisited.isEmpty()) {
            Allocation nearest = findNearestAllocation(currentAisle, currentBay, unvisited);
            path.add(nearest);
            currentAisle = nearest.getAisle();
            currentBay = nearest.getBayNumber();
            unvisited.remove(nearest);
        }

        return path;
    }

    /**
     * Calculates the total distance for a given path of allocations.
     *
     * @param path an ordered list of Allocation objects representing the path to follow
     * @return the total distance required to traverse the path
     */
    public int calculatePathDistance(List<Allocation> path) {
        if (path.isEmpty()) return 0;

        int currentAisle = 0;
        int currentBay = 0;
        int totalDistance = 0;

        for (Allocation allocation : path) {
            int distance = calculateDistance(currentAisle, currentBay, allocation);
            totalDistance += distance;
            currentAisle = allocation.getAisle();
            currentBay = allocation.getBayNumber();
        }

        return totalDistance;
    }

    /**
     * Merges duplicate allocation entries that have the same aisle and bay number
     * into a single unified allocation by summing the quantities.
     *
     * @param allocations a list of Allocation objects representing the allocation points
     * @return a list of Allocation objects where duplicate bays have been merged
     */
    private List<Allocation> mergeDuplicateBays(List<Allocation> allocations) {
        Map<String, Allocation> merged = new LinkedHashMap<>();

        for (Allocation alloc : allocations) {
            String key = alloc.getAisle() + "-" + alloc.getBayNumber();
            if (merged.containsKey(key)) {
                Allocation existing = merged.get(key);
                existing.setQuantityAllocated(existing.getQuantityAllocated() + alloc.getQuantityAllocated());
            } else {
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

    /**
     * Identifies the nearest allocation from the current position based on the calculated distance.
     *
     * @param currentAisle the current aisle position
     * @param currentBay the current bay position
     * @param unvisited a list of unvisited Allocation objects
     * @return the nearest Allocation object from the current position
     */
    private Allocation findNearestAllocation(int currentAisle, int currentBay, List<Allocation> unvisited) {
        Allocation nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Allocation alloc : unvisited) {
            int dist = calculateDistance(currentAisle, currentBay, alloc);
            if (dist < minDistance) {
                minDistance = dist;
                nearest = alloc;
            }
        }

        return nearest;
    }

    /**
     * Calculates the distance between a starting position and a target allocation based on
     * their aisle and bay numbers.
     *
     * @param fromAisle the aisle number at the starting position
     * @param fromBay the bay number at the starting position
     * @param to the target {@link Allocation} object containing the aisle and bay numbers
     * @return the calculated distance as an integer
     */
    private int calculateDistance(int fromAisle, int fromBay, Allocation to) {
        return fromBay + Math.abs(fromAisle - to.getAisle()) * 3 + to.getBayNumber();
    }
}