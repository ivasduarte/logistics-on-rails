package warehouseMngt.returns;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Manages the quarantine queue for returned items awaiting inspection.
 *
 * Implements a LIFO (Last In, First Out) retrieval order using a custom comparator:
 *  - Latest timestamp first (descending)
 *  - Lowest returnID first (ascending)
 */
public class QuarantineQueue {
    private final Queue<Returns> quarantinedItems;

    private static final Comparator<Returns> LIFO_COMPARATOR =
            Comparator.comparing(Returns::getTimestamp)
                    .reversed()
                    .thenComparing(Returns::getReturnID);

    /**
     * Creates an empty quarantine queue.
     * Items are automatically ordered by the LIFO comparator.
     */
    public QuarantineQueue() {
        this.quarantinedItems = new PriorityQueue<>(LIFO_COMPARATOR);
    }

    /**
     * Adds a returned item to the quarantine queue.
     * @param returnItem The return to add
     * @throws NullPointerException if the return item is null
     */
    public void addReturn(Returns returnItem) {
        if (returnItem == null) {
            throw new NullPointerException("Return item cannot be null");
        }
        quarantinedItems.offer(returnItem);
    }

    /**
     * Retrieves and removes the next item for inspection.
     * Returns null if the queue is empty.
     */
    public Returns getNextForInspection() {
        return quarantinedItems.poll();
    }

    /** @return number of items currently in quarantine */
    public int size() {
        return quarantinedItems.size();
    }

    /** @return true if no items are waiting for inspection */
    public boolean isEmpty() {
        return quarantinedItems.isEmpty();
    }
}