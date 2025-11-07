package warehouseMngt.controller;

import warehouseMngt.order.Allocation;
import warehouseMngt.picking.PickingPath;
import warehouseMngt.picking.PickingPlan;
import warehouseMngt.picking.Trolley;

import java.util.List;

/**
 * Controller class responsible for coordinating picking operations
 * and managing interactions between PickingPlan, Trolley, and PickingPath.
 */
public class PickingPathController {

    private final PickingPlan pickingPlan;
    private final PickingPath pickingPath;

    /**
     * Constructs a PickingPathController instance using the given PickingPlan.
     *
     * @param plan the PickingPlan object that contains the trolleys and allocations
     */
    public PickingPathController(PickingPlan plan) {
        this.pickingPlan = plan;
        this.pickingPath = new PickingPath();
    }

    /**
     * Retrieves the list of trolleys associated with the picking plan.
     *
     * @return a list of Trolley objects representing the trolleys in the plan
     */
    public List<Trolley> getTrolleys() {
        return pickingPlan.getTrolleys();
    }

    /**
     * Retrieves the list of allocations associated with a specific trolley.
     *
     * @param trolley the Trolley object from which allocations are to be retrieved
     * @return a list of Allocation objects representing the allocations assigned to the given trolley
     */
    public List<Allocation> getTrolleyAllocations(Trolley trolley) {
        return trolley.getAllocations();
    }

    /**
     * Calculates the optimized path using ascending order strategy.
     *
     * @param allocations the list of allocations to optimize
     * @return an ordered list of allocations representing the optimized path
     */
    public List<Allocation> getPathAscending(List<Allocation> allocations) {
        return pickingPath.getPathAscending(allocations);
    }

    /**
     * Calculates the optimized path using nearest neighbor strategy.
     *
     * @param allocations the list of allocations to optimize
     * @return an ordered list of allocations representing the optimized path
     */
    public List<Allocation> getPathNearest(List<Allocation> allocations) {
        return pickingPath.getPathNearest(allocations);
    }

    /**
     * Minimizes the total traveling distance by ordering allocation points in ascending order
     * based on their aisle and bay numbers.
     *
     * @param allocations the list of {@link Allocation} objects representing the allocation points
     * @return the total distance required to fulfill all allocations in ascending order
     */
    public int minimizeDistanceAscending(List<Allocation> allocations) {
        List<Allocation> path = pickingPath.getPathAscending(allocations);
        return pickingPath.calculatePathDistance(path);
    }

    /**
     * Minimizes the total traveling distance by visiting the nearest allocation point
     * each time from the current position.
     *
     * @param allocations the list of Allocation objects representing the allocation points
     * @return the total minimum distance required to visit all allocation points
     */
    public int minimizeDistanceNearest(List<Allocation> allocations) {
        List<Allocation> path = pickingPath.getPathNearest(allocations);
        return pickingPath.calculatePathDistance(path);
    }

    /**
     * Calculates the total distance for a given trolley using ascending order strategy.
     *
     * @param trolley the trolley whose allocations should be optimized
     * @return the total distance required to complete the trolley's allocations
     */
    public int calculateTrolleyDistanceAscending(Trolley trolley) {
        List<Allocation> allocations = getTrolleyAllocations(trolley);
        return minimizeDistanceAscending(allocations);
    }

    /**
     * Calculates the total distance for a given trolley using nearest neighbor strategy.
     *
     * @param trolley the trolley whose allocations should be optimized
     * @return the total distance required to complete the trolley's allocations
     */
    public int calculateTrolleyDistanceNearest(Trolley trolley) {
        List<Allocation> allocations = getTrolleyAllocations(trolley);
        return minimizeDistanceNearest(allocations);
    }
}