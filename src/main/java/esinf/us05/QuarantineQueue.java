package esinf.us05;

import java.util.ArrayList;
import java.util.List;

public class QuarantineQueue {
    private List<Returns> quarantinedItems;

    public QuarantineQueue() {
        this.quarantinedItems = new ArrayList<>();
    }

    public QuarantineQueue(List<Returns> quarantinedItems) {
        this.quarantinedItems = quarantinedItems;
    }

    // Adds a returned item to the quarantine queue.
    public void addReturn(Returns returnItem) {
        if (returnItem == null) {
            throw new NullPointerException("Return item cannot be null");
        }
        quarantinedItems.add(returnItem);
    }

    /**
     * This method gets the processing return.
     */
    public Returns getNextForInspection() {
        if (quarantinedItems.isEmpty()) return null;

        // Orders: desc for timestamp, asc for returnID
        quarantinedItems.sort((r1, r2) -> {
            int timeCmp = r2.getTimestamp().compareTo(r1.getTimestamp());
            if (timeCmp != 0) return timeCmp;
            return r1.getReturnID().compareTo(r2.getReturnID());
        });
        return quarantinedItems.remove(0);
    }
}
