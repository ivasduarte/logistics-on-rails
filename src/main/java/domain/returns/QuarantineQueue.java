package domain.returns;

import java.util.ArrayList;
import java.util.Comparator;
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
    private List<Returns> quarantinedItems;

    private static final Comparator<Returns> LIFO_COMPARATOR = Comparator.comparing(Returns::getTimestamp).reversed().thenComparing(Returns::getReturnID);

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
        if (returnItem == null) {
            throw new NullPointerException("Return item cannot be null");
        }
        quarantinedItems.add(returnItem);
    }

    public Returns getNextForInspection() {
        if (quarantinedItems.isEmpty()) {
            return null; // No items to process
        }
        quarantinedItems.sort(LIFO_COMPARATOR);
        return quarantinedItems.remove(0);
    }

    public int size() {
        return quarantinedItems.size();
    }

    public boolean isEmpty() {
        return quarantinedItems.isEmpty();
    }
}