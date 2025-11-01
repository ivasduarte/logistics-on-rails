package domain.returns;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the quarantine queue for returned items awaiting inspection.
 *
 * This queue implements a LIFO (Last In, First Out) strategy with tiebreaking rules:
 * - Primary order: Latest timestamp first (descending)
 * - Tie-breaker: Lowest returnID first (ascending)
 *
 */
public class QuarantineQueue {
    // List storing all returns currently in quarantine
    // Unsorted until getNextForInspection() is called
    private List<Returns> quarantinedItems;

    /**
     * Creates an empty quarantine queue.
     * Initializes the internal list to store returned items.
     */
    public QuarantineQueue() {
        this.quarantinedItems = new ArrayList<>();
    }

    /**
     * Creates a quarantine queue with pre-existing items.
     * Useful for testing or initializing from a data source.
     *
     * @param quarantinedItems Initial list of returns already in quarantine
     */
    public QuarantineQueue(List<Returns> quarantinedItems) {
        this.quarantinedItems = quarantinedItems;
    }

    /**
     * Adds a returned item to the quarantine queue.
     *
     * Process:
     * 1. Validate that the return item is not null
     * 2. Add to the internal list (unsorted - sorting happens on retrieval)
     *
     * The item will be positioned according to LIFO rules when getNextForInspection() is called.
     *
     *
     * @param returnItem The return to add to quarantine
     * @throws NullPointerException if the return item is null
     */
    public void addReturn(Returns returnItem) {
        // Step 1: Validate input - prevent null entries that would cause issues later
        if (returnItem == null) {
            throw new NullPointerException("Return item cannot be null");
        }

        // Step 2: Add to the list
        // No sorting needed here - deferred until retrieval for efficiency
        quarantinedItems.add(returnItem);
    }

    /**
     * Retrieves and removes the next return item for inspection.
     *
     * LIFO Processing Order:
     * 1. Sort the entire queue by timestamp (descending) - most recent first
     * 2. If timestamps equal, sort by returnID (ascending) - deterministic tie-breaker
     * 3. Remove and return the first item (the highest priority)
     *
     * Edge Case Handling:
     * - Empty queue → returns null (caller must check)
     *
     * @return The next Returns object to inspect, or null if the queue is empty
     */
    public Returns getNextForInspection() {
        // Step 1: Handle an empty queue case
        if (quarantinedItems.isEmpty()) {
            return null; // No items to process
        }

        // Step 2: Sort the entire queue according to LIFO rules
        // Lambda comparator implements two-level sorting:
        quarantinedItems.sort((r1, r2) -> {
            // Level 1: Compare timestamps (descending order - latest first)
            // r2.compareTo(r1) instead of r1.compareTo(r2) reverses natural order
            int timeCmp = r2.getTimestamp().compareTo(r1.getTimestamp());

            if (timeCmp != 0) {
                // Timestamps are different - use timestamp ordering
                return timeCmp;
            }

            // Level 2: Timestamps are equal - use returnID as tie-breaker (ascending)
            // r1.compareTo(r2) maintains natural ascending order
            // This ensures deterministic, reproducible ordering for testing/auditing
            return r1.getReturnID().compareTo(r2.getReturnID());
        });

        // Step 3: Remove and return the first element (highest priority)
        // remove(0) removes from index 0 and shifts remaining elements
        // Returns the removed element
        return quarantinedItems.remove(0);
    }
}