package esinf.us05;

import esinf.us01.Warehouse;

import java.io.IOException;

/**
 * Facade/Controller for the returns and quarantine system.
 * Coordinates between quarantine queue and inspection processes.
 *
 * Responsibilities:
 * - Receive new returns and add them to quarantine
 * - Orchestrate the inspection workflow
 * - Manage dependencies between components
 *
 * Design Pattern: Facade
 * - Simplifies interaction with a complex subsystem (quarantine and inspection)
 * - Provides high-level interface for quality operators
 */
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
     * Processes the next return item in the quarantine queue.
     *
     * Workflow:
     * 1. Retrieve the next item from the queue (LIFO order - latest timestamp first)
     * 2. If item exists, inspect it (automatically discards or restocks)
     * 3. If queue empty, inform operator
     *
     * This method should be called repeatedly until queue is empty.
     *
     * @throws IOException If audit logging fails during inspection
     */
    public void processReturns() throws IOException {
        // Step 1: Get the next item from quarantine (poll removes from queue)
        Returns returnedItem = quarantineQueue.getNextForInspection();

        // Step 2: Check if the queue had any items
        if (returnedItem == null) {
            // No items to process - queue is empty
            System.out.println("No returns to process.");
            return;
        }

        // Step 3: Inspect the item
        // - Determine if an item should be discarded or restocked
        // - Execute the appropriate action
        // - Log to audit trail
        inspection.inspect(returnedItem);
    }
}