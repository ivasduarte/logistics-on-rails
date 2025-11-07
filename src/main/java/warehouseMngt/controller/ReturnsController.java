package warehouseMngt.controller;

import warehouseMngt.logistics.StockAction;
import warehouseMngt.returns.*;
import warehouseMngt.warehouse.Warehouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReturnsController {
    public QuarantineQueue quarantineQueue;
    public InventoryInspection inspection;

    /**
     * Initializes the return management system.
     *
     * @param warehouse The warehouse where restocked items will be inserted
     * @param logFilePath Path to the audit log file
     */
    public ReturnsController(Warehouse warehouse, String logFilePath) {
        this.quarantineQueue = new QuarantineQueue();
        AuditLog auditLog = new AuditLog(logFilePath);
        this.inspection = new InventoryInspection(warehouse, auditLog);
    }

    /**
     * Receives a returned item and places it in quarantine for inspection.
     *
     * @param returnItem The return to add to quarantine
     */
    public void receiveReturn(Returns returnItem) {
        quarantineQueue.addReturn(returnItem);
    }

    /**
     * Processes all returns currently in the quarantine queue.
     * Returns a summary of actions taken.
     *
     * @return Map with counts of restocked and discarded items
     * @throws IOException If audit logging fails
     */
    public Map<String, Integer> processAllReturns() throws IOException {
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

        Map<String, Integer> summary = new HashMap<>();
        summary.put("restocked", restockedCount);
        summary.put("discarded", discardedCount);
        summary.put("total", restockedCount + discardedCount);

        return summary;
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