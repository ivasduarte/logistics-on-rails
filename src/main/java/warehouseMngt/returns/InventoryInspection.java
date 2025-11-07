package warehouseMngt.returns;

import warehouseMngt.controller.WarehouseController;
import warehouseMngt.logistics.StockAction;
import warehouseMngt.warehouse.Box;
import warehouseMngt.warehouse.Warehouse;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Handles the inspection and processing of returned items from quarantine.
 */
public class InventoryInspection {
    private Warehouse warehouse;
    private AuditLog auditLog;
    private WarehouseController warehouseController;

    /**
     * Creates an inspection handler with access to warehouse and audit systems.
     *
     * @param warehouse The warehouse where restocked items will be inserted
     * @param auditLog The audit log for recording all inspection actions
     */
    public InventoryInspection(Warehouse warehouse, AuditLog auditLog) {
        this.warehouse = warehouse;
        this.auditLog = auditLog;
        this.warehouseController = new WarehouseController(warehouse);
    }

    /**
     * Determines if a returned item should be discarded based on its return reason.
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
     * @param returnedItem The item to inspect and process
     * @throws IOException If audit logging fails
     */
    public void inspect(Returns returnedItem) throws IOException {
        StockAction action;

        if (shouldBeDiscarded(returnedItem)) {
            action = StockAction.DISCARDED;
            returnedItem.setDiscarded(true);

            auditLog.logInspection(
                    returnedItem.getReturnID(),
                    returnedItem.getSku(),
                    returnedItem.getQty(),
                    0,              // qtyRestocked = 0 (nothing restocked)
                    returnedItem.getQty(),      // qtyDiscarded = all
                    action
            );

        } else {
            restock(returnedItem);

            action = StockAction.RESTOCKED;

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
     * @param returnedItem The item to restock
     */
    public void restock(Returns returnedItem) {
        String newBoxID = "RET-" + returnedItem.getReturnID();

        LocalDateTime receivedAt = LocalDateTime.now();

        LocalDateTime expiryDate = null;
        if (returnedItem.getExpiryDate() != null && !returnedItem.getExpiryDate().isBlank()) {
            try {
                expiryDate = LocalDateTime.parse(returnedItem.getExpiryDate());
            } catch (Exception e) {
                try {
                    expiryDate = java.time.LocalDate.parse(returnedItem.getExpiryDate())
                            .atStartOfDay();
                } catch (Exception e2) {
                    System.err.println("[WARN] Could not parse expiry date for return ID "
                            + returnedItem.getReturnID() + ": " + expiryDate);
                }
            }
        }

        Box newBox = new Box(
                newBoxID,                       // Unique identifier
                returnedItem.getSku(),          // Product type
                returnedItem.getQty(),          // Quantity
                expiryDate,                     // Expiry (null if non-perishable)
                receivedAt.toString()           // When restocked (ISO timestamp)
        );

        warehouseController.insertBox(newBox);
    }
}