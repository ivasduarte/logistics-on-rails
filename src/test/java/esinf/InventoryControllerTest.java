package esinf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import warehouseMngt.controller.InventoryController;
import warehouseMngt.operations.StockAction;
import warehouseMngt.returns.AuditLog;
import warehouseMngt.returns.ReturnReasons;
import warehouseMngt.returns.Returns;
import warehouseMngt.domain.Bay;
import warehouseMngt.domain.Box;
import warehouseMngt.domain.Warehouse;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InventoryControllerTest {

    private FakeWarehouse warehouse;
    private FakeAuditLog auditLog;
    private InventoryController controller;

    @BeforeEach
    void setup() {
        warehouse = new FakeWarehouse();
        auditLog = new FakeAuditLog();
        controller = new InventoryController(warehouse, auditLog);
    }

    @Test
    void testShouldBeDiscarded_WhenReasonIndicatesDiscard() {
        Returns returned = new Returns("R1", "SKU1", 5, ReturnReasons.DAMAGED,
                "2024-01-01T10:00:00", null);

        assertTrue(controller.shouldBeDiscarded(returned));
    }

    @Test
    void testShouldBeDiscarded_WhenReasonDoesNotIndicateDiscard() {
        Returns returned = new Returns("R1", "SKU1", 5, ReturnReasons.REMORSE,
                "2024-01-01T10:00:00", null);

        assertFalse(controller.shouldBeDiscarded(returned));
    }

    @Test
    void testInspect_ShouldDiscardItem() {
        Returns returned = new Returns("R100", "SKU-X", 3, ReturnReasons.DAMAGED,
                "2024-01-01T10:00:00", null);

        controller.inspect(returned);

        assertEquals(0, warehouse.getWarehouses().get("WH1").iterator().next().getBoxes().size());
        assertEquals(1, auditLog.entries.size());

        FakeAuditLog.Entry logged = auditLog.entries.get(0);
        assertEquals("R100", logged.returnId);
        assertEquals(StockAction.DISCARDED, logged.action);
        assertEquals(0, logged.qtyRestocked);
        assertEquals(3, logged.qtyDiscarded);
    }

    @Test
    void testInspect_ShouldRestockItem() {
        Returns returned = new Returns("R200", "SKU-Y", 10, ReturnReasons.REMORSE,
                "2024-01-01T10:00:00", null);

        controller.inspect(returned);

        Bay bay = warehouse.getWarehouses().get("WH1").iterator().next();
        assertEquals(1, bay.getBoxes().size());

        Box inserted = bay.getBoxes().iterator().next();
        assertEquals("RET-R200", inserted.getBoxID());
        assertEquals("SKU-Y", inserted.getSku());
        assertEquals(10, inserted.getQuantity());

        FakeAuditLog.Entry logged = auditLog.entries.get(0);
        assertEquals(10, logged.qtyRestocked);
        assertEquals(0, logged.qtyDiscarded);
        assertEquals(StockAction.RESTOCKED, logged.action);
    }

    @Test
    void testRestock_CreatesBoxCorrectly() {
        Returns returned = new Returns("R300", "SKU-Z", 7, ReturnReasons.CYCLE_COUNT,
                "2024-01-01T10:00:00", "2025-12-01");

        controller.restock(returned);

        Bay bay = warehouse.getWarehouses().get("WH1").iterator().next();
        assertEquals(1, bay.getBoxes().size());

        Box box = bay.getBoxes().iterator().next();
        assertEquals("RET-R300", box.getBoxID());
        assertEquals("SKU-Z", box.getSku());
        assertEquals(7, box.getQuantity());
        assertNotNull(box.getExpiryDate());
    }

    private static class FakeWarehouse extends Warehouse {

        public FakeWarehouse() {
            super(
                    new HashMap<>(),  // wagons
                    new HashMap<>(),  // items
                    new HashMap<>(),  // order_lines
                    new HashMap<>(),  // orders
                    new HashMap<>(),  // returns
                    createFakeWarehouses()  // warehouses
            );
        }

        private static Map<String, Set<Bay>> createFakeWarehouses() {
            Map<String, Set<Bay>> warehouses = new HashMap<>();
            Set<Bay> bays = new HashSet<>();
            bays.add(new Bay(1, 1, 100)); // Bay com capacidade suficient
            warehouses.put("WH1", bays);
            return warehouses;
        }
    }

    private static class FakeAuditLog extends AuditLog {

        static class Entry {
            String returnId;
            String sku;
            int qty;
            int qtyRestocked;
            int qtyDiscarded;
            StockAction action;
        }

        List<Entry> entries = new ArrayList<>();

        public FakeAuditLog() {
            super("fake-path/");
        }

        @Override
        public void logInspection(String returnId, String sku, int qty, int qtyRestocked,
                                  int qtyDiscarded, StockAction action) {
            Entry x = new Entry();
            x.returnId = returnId;
            x.sku = sku;
            x.qty = qty;
            x.qtyRestocked = qtyRestocked;
            x.qtyDiscarded = qtyDiscarded;
            x.action = action;
            entries.add(x);
        }
    }
}