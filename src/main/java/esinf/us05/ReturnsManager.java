package esinf.us05;

import esinf.us01.Warehouse;

import java.io.IOException;

public class ReturnsManager {
    public QuarantineQueue quarantineQueue;
    public InventoryInspection inspection;

    public ReturnsManager(Warehouse warehouse, String logFilePath) {
        this.quarantineQueue = new QuarantineQueue();
        AuditLog auditLog = new AuditLog(logFilePath);
        this.inspection = new InventoryInspection(warehouse, auditLog);
    }

    // Receives a return item and adds it to the quarantine queue.
    public void receiveReturn(Returns returnItem) {
        quarantineQueue.addReturn(returnItem);
    }

    /**
     * Processes the next return item in the quarantine queue.
     * @throws IOException
     */
    public void processReturns() throws IOException {
        Returns returnedItem = quarantineQueue.getNextForInspection();

        if(returnedItem == null) {
            System.out.println("No returns to process.");
            return;
        }
        inspection.inspect(returnedItem);
    }
}
