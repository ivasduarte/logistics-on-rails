package domain.returns;

import domain.warehouse.Warehouse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReturnsManager {
    // Queue holding returns awaiting inspection (LIFO ordering)
    public QuarantineQueue quarantineQueue;

    // Inspection service that evaluates and processes returns
    public InventoryInspection inspection;

    /**
     * Initializes the return management system.
     *
     * Setup Process:
     * 1. Create an empty quarantine queue
     * 2. Create an audit log instance for the specified file
     * 3. Create an inspection service with warehouse access and audit logging
     *
     * @param warehouse The warehouse where restocked items will be inserted
     * @param logFilePath Path to the audit log file
     */
    public ReturnsManager(Warehouse warehouse, String logFilePath) {
        // Step 1: Initialize the quarantine queue with LIFO ordering
        this.quarantineQueue = new QuarantineQueue();

        // Step 2: Create an audit logger
        AuditLog auditLog = new AuditLog(logFilePath);

        // Step 3: Create an inspection service with dependencies
        this.inspection = new InventoryInspection(warehouse, auditLog);
    }

    /**
     * Receives a returned item and places it in quarantine for inspection.
     *
     * Process:
     * - Add return to priority queue (automatically ordered by LIFO rules)
     * - Return is now awaiting inspection
     *
     * @param returnItem The return to add to quarantine
     */
    public void receiveReturn(Returns returnItem) {
        // Delegate to quarantine queue
        // Queue handles ordering based on timestamp + returnID
        quarantineQueue.addReturn(returnItem);
    }

    /**
     * Receives and processes a batch of returns.
     *
     * Process:
     * 1. Add all returns to quarantine queue
     * 2. Process them in LIFO order
     * 3. Return summary of actions taken
     *
     * @param returns List of returns to process
     * @return Map with counts of restocked and discarded items
     * @throws IOException If audit logging fails
     */
    public Map<String, Integer> processBatchReturns(List<Returns> returns) throws IOException {
        // Step 1: Add all to quarantine
        for (Returns returnItem : returns) {
            quarantineQueue.addReturn(returnItem);
        }

        // Step 2: Process all and count actions
        int restockedCount = 0;
        int discardedCount = 0;

        Returns returnedItem;
        while ((returnedItem = quarantineQueue.getNextForInspection()) != null) {
            inspection.inspect(returnedItem);

            if (inspection.shouldBeDiscarded(returnedItem)) {
                discardedCount++;
            } else {
                restockedCount++;
            }
        }

        // Step 3: Return summary
        Map<String, Integer> summary = new HashMap<>();
        summary.put("restocked", restockedCount);
        summary.put("discarded", discardedCount);

        return summary;
    }
}