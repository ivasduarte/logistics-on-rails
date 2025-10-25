package esinf.us05;

import esinf.us01.Box;
import esinf.us01.Warehouse;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Handles the inspection and processing of returned items from quarantine.
 *
 * Workflow:
 * 1. Inspect return to determine if it should be discarded or restocked
 * 2. Execute appropriate action (discard or restock)
 * 3. Log the action to audit trail
 *
 * Business Rules:
 * - DAMAGED/EXPIRED items → Discard (mark as unusable)
 * - REMORSE/CYCLE_COUNT items → Restock (return to inventory)
 */
public class InventoryInspection {
    // Reference to warehouse for reinserting restocked items
    private Warehouse warehouse;

    // Audit logger for compliance tracking
    private AuditLog auditLog;

    /**
     * Creates an inspection handler with access to warehouse and audit systems.
     *
     * @param warehouse The warehouse where restocked items will be inserted
     * @param auditLog The audit log for recording all inspection actions
     */
    public InventoryInspection(Warehouse warehouse, AuditLog auditLog) {
        this.warehouse = warehouse;
        this.auditLog = auditLog;
    }

    /**
     * Determines if a returned item should be discarded based on its return reason.
     *
     * Decision Logic:
     * - DAMAGED: Physical integrity compromised → cannot resell → discard
     * - EXPIRED: Past safety date → health/quality risk → discard
     * - REMORSE: Customer changed mind → item still good → restock
     * - CYCLE_COUNT: Inventory audit adjustment → verified stock → restock
     *
     * @param returnedItem The return to evaluate
     * @return true if an item should be discarded, false if it can be restocked
     */
    public boolean shouldBeDiscarded(Returns returnedItem) {
        // Delegates to enum business logic
        return returnedItem.getReason().shouldBeDiscarded();
    }

    /**
     * Inspects a returned item and executes the appropriate action.
     * This is the main entry point for processing quarantine items.
     *
     * Process Flow:
     * 1. Evaluate return reason to determine action
     * 2a. If should discard → log as DISCARDED
     * 2b. If should restock → create a new box, insert to warehouse, log as RESTOCKED
     * 3. Ensure all actions are recorded in the audit log
     *
     * @param returnedItem The item to inspect and process
     * @throws IOException If audit logging fails
     */
    public void inspect(Returns returnedItem) throws IOException {
        StockAction action;

        // Step 1: Determine action based on return reason
        if (shouldBeDiscarded(returnedItem)) {
            // Path A: Item cannot be restocked
            action = StockAction.DISCARDED;

            // Mark item as discarded (prevents accidental reuse)
            returnedItem.setDiscarded(true);

            // Step 2a: Log discard action
            // Parameters: returnID, sku, total qty, restocked (0), discarded (all), action
            auditLog.logInspection(
                    returnedItem.getReturnID(),
                    returnedItem.getSku(),
                    returnedItem.getQty(),
                    0,              // qtyRestocked = 0 (nothing restocked)
                    returnedItem.getQty(),      // qtyDiscarded = all
                    action
            );

        } else {
            // Path B: Item is good and can be restocked
            // Step 2b: Restock the item (creates new box and inserts to warehouse)
            restock(returnedItem);

            action = StockAction.RESTOCKED;

            // Step 2c: Log restock action
            // Parameters: returnID, sku, total qty, restocked (all), discarded (0), action
            auditLog.logInspection(
                    returnedItem.getReturnID(),
                    returnedItem.getSku(),
                    returnedItem.getQty(),
                    returnedItem.getQty(),      // qtyRestocked = all
                    0,                          // qtyDiscarded = 0 (nothing discarded)
                    action
            );
        }
    }

    /**
     * Restocks a returned item by creating a new box and inserting it into the warehouse.
     *
     * Restock Process:
     * 1. Create a unique box ID using return ID (format: "RET-{returnID}")
     * 2. Set the received timestamp to the current time
     * 3. Parse expiry date if provided (null if unknown/non-perishable)
     * 4. Create a new Box object with all product information
     * 5. Insert the box into the warehouse using FEFO/FIFO rules (from USEI01)
     *
     * FEFO/FIFO Rules (handled by warehouse.insertBox):
     * - Products with expiry dates sorted by the earliest expiry first (FEFO)
     * - Products without expiry dates sorted by the oldest received first (FIFO)
     * - Tie-breaker: boxID ascending
     *
     * @param returnedItem The item to restock
     */
    public void restock(Returns returnedItem) {
        // Step 1: Generate unique box ID for this restocked item
        // Prefix "RET-" identifies it as a returned item for traceability
        String newBoxID = "RET-" + returnedItem.getReturnID();

        // Step 2: Record when this box is being restocked (now)
        LocalDateTime receivedAt = LocalDateTime.now();

        // Step 3: Parse expiry date from string format to LocalDateTime
        LocalDateTime expiryDate = null;
        if (returnedItem.getExpiryDate() != null && !returnedItem.getExpiryDate().isBlank()) {
            try {
                // Try parsing as full datetime (ISO format: yyyy-MM-ddTHH:mm:ss)
                expiryDate = LocalDateTime.parse(returnedItem.getExpiryDate());
            } catch (Exception e) {
                // Fallback: try parsing as date only, convert to datetime at midnight
                try {
                    expiryDate = java.time.LocalDate.parse(returnedItem.getExpiryDate())
                            .atStartOfDay();
                } catch (Exception e2) {
                    // If both parsing attempts fail, leave expiryDate as null
                    // (non-perishable item or invalid date format)
                }
            }
        }

        // Step 4: Create a new Box object representing the restocked item
        Box newBox = new Box(
                newBoxID,                       // Unique identifier
                returnedItem.getSku(),          // Product type
                returnedItem.getQty(),          // Quantity
                expiryDate,                     // Expiry (null if non-perishable)
                receivedAt.toString()           // When restocked (ISO timestamp)
        );

        // Step 5: Insert box into warehouse
        // The warehouse.insertBox() method handles:
        // - Finding the appropriate bay based on SKU
        // - Positioning box according to FEFO/FIFO rules
        // - Updating warehouse data structures
        warehouse.insertBox(newBox);
    }
}