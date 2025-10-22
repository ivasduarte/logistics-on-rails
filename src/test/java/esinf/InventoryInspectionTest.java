package esinf;

import esinf.us01.Box;
import esinf.us01.Warehouse;
import esinf.us05.AuditLog;
import esinf.us05.InventoryInspection;
import esinf.us05.Returns;
import esinf.us05.ReturnReasons;
import esinf.us05.StockAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryInspectionTest {

    private FakeWarehouse warehouse;
    private FakeAuditLog auditLog;
    private InventoryInspection inspection;

    @BeforeEach
    void setUp() {
        warehouse = new FakeWarehouse();
        auditLog = new FakeAuditLog();
        inspection = new InventoryInspection(warehouse, auditLog);
    }

    @Test
    void testShouldBeDiscarded_ReturnsTrue() {
        Returns returnedItem = new Returns("R001", "SKU1", 5, ReturnReasons.DAMAGED, 
                "2024-01-01T10:00:00", "2024-12-31T00:00:00");
        assertTrue(inspection.shouldBeDiscarded(returnedItem));
    }

    @Test
    void testShouldBeDiscarded_ReturnsFalse() {
        Returns returnedItem = new Returns("R002", "SKU2", 10, ReturnReasons.REMORSE, 
                "2024-01-01T10:00:00", "2024-12-31T00:00:00");
        assertFalse(inspection.shouldBeDiscarded(returnedItem));
    }

    @Test
    void testInspect_ItemShouldBeDiscarded_LogsInspection() throws IOException {
        Returns returnedItem = new Returns("R010", "SKU10", 20, ReturnReasons.EXPIRED, 
                "2024-01-01T10:00:00", "2024-12-31T00:00:00");

        inspection.inspect(returnedItem);

        assertEquals(1, auditLog.loggedActions.size());
        String log = auditLog.loggedActions.get(0);
        assertTrue(log.contains("R010"));
        assertTrue(log.contains("SKU10"));
        assertTrue(log.contains("DISCARDED"));
    }

    @Test
    void testInspect_ItemShouldNotBeDiscarded_NoLog() throws IOException {
        Returns returnedItem = new Returns("R011", "SKU11", 8, ReturnReasons.CYCLE_COUNT, 
                "2024-01-01T10:00:00", "2024-12-31T00:00:00");

        inspection.inspect(returnedItem);

        assertTrue(auditLog.loggedActions.isEmpty(), "Nenhum log deve ser gerado");
    }

    @Test
    void testRestock_AddsBoxToWarehouse() {
        Returns returnedItem = new Returns("R100", "SKU100", 10, ReturnReasons.REMORSE, 
                "2024-01-01T10:00:00", "2030-01-01T00:00:00");

        inspection.restock(returnedItem);

        assertEquals(1, warehouse.boxes.size(), "Uma caixa deve ser adicionada ao armazém");
        Box box = warehouse.boxes.get(0);
        assertEquals("RET-R100", box.getBoxID());
        assertEquals("SKU100", box.getSku());
        assertEquals(10, box.getQuantity());
        assertEquals(LocalDateTime.of(2030, 1, 1, 0, 0), box.getExpiryDate());
    }

    @Test
    void testRestock_BoxHasRecentReceivedAt() {
        Returns returnedItem = new Returns("R200", "SKU200", 2, ReturnReasons.CYCLE_COUNT, 
                "2024-01-01T10:00:00", "2024-12-31T00:00:00");

        LocalDateTime before = LocalDateTime.now().minusSeconds(2);
        inspection.restock(returnedItem);
        LocalDateTime after = LocalDateTime.now().plusSeconds(2);

        Box box = warehouse.boxes.get(0);
        LocalDateTime receivedAt = LocalDateTime.parse(box.getReceivedAt());
        assertTrue(receivedAt.isAfter(before.minusSeconds(1)) && receivedAt.isBefore(after.plusSeconds(1)),
                "A data de receção deve ser próxima do momento atual");
    }

    static class FakeWarehouse extends Warehouse {
        List<Box> boxes = new ArrayList<>();

        @Override
        public void insertBox(Box box) {
            boxes.add(box);
        }
    }

    static class FakeAuditLog extends AuditLog {
        List<String> loggedActions = new ArrayList<>();

        public FakeAuditLog() {
            super("test-logs/");
        }

        @Override
        public void logInspection(String returnID, String sku, int quantity,
                                  int qtyRestocked, int qtyDiscarded, StockAction action) {
            loggedActions.add(returnID + " | " + sku + " | " + action);
        }
    }
}
