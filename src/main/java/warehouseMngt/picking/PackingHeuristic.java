/**
 * PackingHeuristic.java
 */
package warehouseMngt.picking;
/**
 * Enumeration of available packing heuristics for trolley allocation.
 * <p>
 * Each heuristic represents a different strategy for packing allocations into trolleys:
 * <ul>
 *   <li><b>FIRST_FIT (FF)</b>: Place each allocation in the first trolley where it fits,
 *       processing allocations in their original input order.</li>
 *   <li><b>FIRST_FIT_DECREASING (FFD)</b>: Sort allocations by weight (largest first),
 *       then place each in the first trolley where it fits.</li>
 *   <li><b>BEST_FIT_DECREASING (BFD)</b>: Sort allocations by weight (largest first),
 *       then place each in the trolley that results in the smallest remaining capacity
 *       (tightest fit).</li>
 * </ul>
 */
public enum PackingHeuristic {
    /**
     * First Fit: Process allocations in input order, placing each in the first
     * available trolley with sufficient capacity.
     */
    FIRST_FIT,
    /**
     * First Fit Decreasing: Sort allocations by weight (descending), then apply
     * the First Fit strategy.
     */
    FIRST_FIT_DECREASING,
    /**
     * Best Fit Decreasing: Sort allocations by weight (descending), then place
     * each allocation in the trolley that will have the smallest remaining capacity
     * after the allocation is added.
     */
    BEST_FIT_DECREASING
}