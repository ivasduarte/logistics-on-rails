package esinf.us05;

import esinf.us01.Box;
import esinf.us01.Warehouse;

import java.io.IOException;
import java.time.LocalDateTime;

public class InventoryInspection {
    private Warehouse warehouse;
    private AuditLog auditLog;

    public InventoryInspection(Warehouse warehouse, AuditLog auditLog) {
        this.warehouse = warehouse;
        this.auditLog = auditLog;
    }

    public boolean shouldBeDiscarded(Returns returnedItem){
        return returnedItem.getReason().shouldBeDiscarded();
    }

    public void inspect(Returns returnedItem) throws IOException {
        StockAction action;

        if(shouldBeDiscarded(returnedItem)){
            action = StockAction.DISCARDED;
            auditLog.logInspection(returnedItem.getReturnID(), returnedItem.getSku(), returnedItem.getQty(), 0, 0, action);
        }
    }

    public void restock(Returns returnedItem){
        // Create a new box for the returned item
        String newBoxID = "RET-" + returnedItem.getReturnID();
        LocalDateTime receivedAt = LocalDateTime.now();
        
        // Parse expiry date from string if provided
        LocalDateTime expiryDate = null;
        if (returnedItem.getExpiryDate() != null && !returnedItem.getExpiryDate().isBlank()) {
            try {
                expiryDate = LocalDateTime.parse(returnedItem.getExpiryDate());
            } catch (Exception e) {
                // If parsing fails, try parsing as date and convert to datetime
                try {
                    expiryDate = java.time.LocalDate.parse(returnedItem.getExpiryDate()).atStartOfDay();
                } catch (Exception e2) {
                    // If both fail, leave as null
                }
            }
        }

        Box newBox = new Box(newBoxID, returnedItem.getSku(), returnedItem.getQty(), expiryDate, receivedAt.toString());
        warehouse.insertBox(newBox);
    }
}
