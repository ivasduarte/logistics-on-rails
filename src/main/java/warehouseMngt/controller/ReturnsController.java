package warehouseMngt.controller;

import warehouseMngt.operations.StockAction;
import warehouseMngt.returns.*;
import warehouseMngt.domain.Warehouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReturnsController {
    public QuarantineQueue quarantineQueue;
    public InventoryController inspection;

    /**
     * Initializes the return management system.
     *
     * @param warehouse The warehouse where restocked items will be inserted
     * @param logFilePath Path to the audit log file
     */
    public ReturnsController(Warehouse warehouse, String logFilePath) {
        this.quarantineQueue = new QuarantineQueue();
        AuditLog auditLog = new AuditLog(logFilePath);
        this.inspection = new InventoryController(warehouse, auditLog);
    }

    /**
     * Processes a batch of returns with detailed results.
     *
     * @param returns List of returns to process
     * @return List of processing results
     * @throws IOException If audit logging fails
     */
    public List<ReturnProcessingResult> processBatchReturns(List<Returns> returns)
            throws IOException {

        // Add all to quarantine
        for (Returns returnItem : returns) {
            quarantineQueue.addReturn(returnItem);
        }

        // Process and collect results
        List<ReturnProcessingResult> results = new ArrayList<>();

        Returns returnedItem;
        while ((returnedItem = quarantineQueue.getNextForInspection()) != null) {
            inspection.inspect(returnedItem);

            StockAction action = inspection.shouldBeDiscarded(returnedItem)
                    ? StockAction.DISCARDED
                    : StockAction.RESTOCKED;

            results.add(new ReturnProcessingResult(
                    returnedItem.getReturnID(),
                    returnedItem.getSku(),
                    returnedItem.getQty(),
                    returnedItem.getReason(),
                    action
            ));
        }

        return results;
    }

    /**
     * Inner class to store processing results.
     */
    public static class ReturnProcessingResult {
        private String returnId;
        private String sku;
        private int quantity;
        private ReturnReasons reason;
        private StockAction action;

        public ReturnProcessingResult(String returnId, String sku, int quantity,
                                      ReturnReasons reason, StockAction action) {
            this.returnId = returnId;
            this.sku = sku;
            this.quantity = quantity;
            this.reason = reason;
            this.action = action;
        }

        public String getReturnId() { return returnId; }
        public String getSku() { return sku; }
        public int getQuantity() { return quantity; }
        public ReturnReasons getReason() { return reason; }
        public StockAction getAction() { return action; }

        @Override
        public String toString() {
            return returnId + " | SKU: " + sku + " | Qty: " + quantity +
                    " | Reason: " + reason + " | Action: " + action;
        }
    }
}