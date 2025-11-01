package domain.picking;

import domain.order.Allocation;
import domain.picking.PickingPlan;
import domain.picking.Trolley;

import java.util.*;

public class PickingPath {

    private final List<Trolley> trolleys;

    /**
     * Constructs a PickingPath instance using the given PickingPlan.
     *
     * @param plan the PickingPlan object that contains the trolleys and allocations
     */
    public PickingPath(PickingPlan plan) {
        trolleys = plan.getTrolleys();
    }

    /**
     * Retrieves the list of trolleys associated with this PickingPath.
     *
     * @return a list of Trolley objects representing the trolleys in this PickingPath
     */
    public List<Trolley> getTrolleys() {
        return trolleys;
    }

    /**
     * Retrieves the list of allocations associated with a specific trolley.
     *
     * @param trolley the Trolley object from which allocations are to be retrieved
     * @return a list of Allocation objects representing the allocations assigned to the given trolley
     */
    public List<Allocation> getTrolleyAllocations(Trolley trolley){
        return trolley.getAllocations();
    }

    /**
     * Minimizes the total traveling distance by ordering allocation points in ascending order
     * based on their aisle and bay numbers. The method considers all allocation points, merges duplicates,
     * and calculates the total distance required to visit these points in the optimized order.
     *
     * @param allocations the list of {@link Allocation} objects representing the allocation points,
     *                    including their aisle and bay numbers
     * @return the total distance required to fulfill all allocations in ascending order
     */
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

        System.out.println("\n--- ASCENDING ORDER STRATEGY ---");
        System.out.println("Path:");
        System.out.println("  0. Start (0-0)");

        // Go through every bay to calculate the distance in ascending order
        int step = 1;
        for (Allocation allocation : merged) {
            int distance = calculateDistance(currentAisle, currentBay, allocation);
            totalDistance += distance;
            System.out.printf("  %d. Aisle %d, Bay %d (distance: %d)%n",
                    step++, allocation.getAisle(), allocation.getBayNumber(), distance);
            currentAisle = allocation.getAisle();
            currentBay = allocation.getBayNumber();
        }

        System.out.println("Total Distance: " + totalDistance + " units\n");

        return totalDistance;
    }

    /**
     * Minimizes the total traveling distance by visiting the nearest allocation point
     * each time from the current position. The method processes all allocation points,
     * merges duplicates, and calculates the total distance required to complete all allocations.
     *
     * @param allocations the list of Allocation objects representing the allocation points,
     *                    including their aisle and bay numbers
     * @return the total minimum distance required to visit all allocation points
     *         in the order of nearest unvisited points
     */
    public int minimizeDistanceNearest(List<Allocation> allocations) {
        if (allocations.isEmpty()) return 0;

        // Merge duplicate bays first
        List<Allocation> merged = mergeDuplicateBays(allocations);

        List<Allocation> unvisited = new ArrayList<>(merged);

        int currentAisle = 0;
        int currentBay = 0;
        int totalDistance = 0;

        System.out.println("\n--- NEAREST NEIGHBOR STRATEGY ---");
        System.out.println("Path:");
        System.out.println("  0. Start (0-0)");

        // Check for unvisited bays to calculate the distance from the current bay to the nearest one
        int step = 1;
        while (!unvisited.isEmpty()) {
            Allocation nearest = findNearestAllocation(currentAisle, currentBay, unvisited);
            int distance = calculateDistance(currentAisle, currentBay, nearest);
            totalDistance += distance;
            System.out.printf("  %d. Aisle %d, Bay %d (distance: %d)%n",
                    step++, nearest.getAisle(), nearest.getBayNumber(), distance);

            // Replaces current aisle and current bay to the new values
            currentAisle = nearest.getAisle();
            currentBay = nearest.getBayNumber();

            // Removes bay from the unvisited list
            unvisited.remove(nearest);
        }

        System.out.println("Total Distance: " + totalDistance + " units\n");

        return totalDistance;
    }

    /**
     * Merges duplicate allocation entries that have the same aisle and bay number
     * into a single unified allocation by summing the quantities.
     *
     * @param allocations a list of Allocation objects representing the allocation points
     *                     to be processed. Duplicate allocations are determined by
     *                     matching both aisle and bay number.
     * @return a list of Allocation objects where duplicate bays have been merged into
     *         a single entry, with aggregate quantities computed.
     */
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

    /**
     * Identifies the nearest allocation from the current position based on the calculated distance.
     *
     * @param currentAisle the current aisle position
     * @param currentBay the current bay position
     * @param unvisited a list of unvisited Allocation objects
     * @return the nearest Allocation object from the current position; returns null if the list is empty
     */
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

    /**
     * Calculates the distance between a starting position and a target allocation based on
     * their aisle and bay numbers.
     *
     * The distance is computed using the formula:
     * fromBay + (absolute difference between aisles * 3) + target bay number.
     *
     * @param fromAisle the aisle number at the starting position
     * @param fromBay the bay number at the starting position
     * @param to the target {@link Allocation} object containing the aisle and bay numbers
     * @return the calculated distance as an integer
     */
    private int calculateDistance(int fromAisle, int fromBay, Allocation to) {
        // Returns the distance based on the formula given in the assignment
        return fromBay + Math.abs(fromAisle - to.getAisle()) * 3 + to.getBayNumber();
    }
}