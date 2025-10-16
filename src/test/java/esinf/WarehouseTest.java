package esinf;

import esinf.us01.Bay;
import esinf.us01.Box;
import esinf.us01.Wagon;
import esinf.us01.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class WarehouseTest {
    private Warehouse warehouse;
    private List<String> testLines;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        testLines = new ArrayList<>();
    }

    @Test
    void testGetWagons() throws IOException {
        warehouse.getItems(Files.readAllLines(Paths.get(Warehouse.ITEMS_FILE)));
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add("W1,B1,SKU0001,10,2024-12-31,2023-10-10");
        warehouse.getWagons(testLines);
        assertFalse(warehouse.getWagonsMap().isEmpty());
    }

    @Test
    void testGetBays() {
        testLines.add("WarehouseID,Aisle,Bay,BoxCapacity");
        testLines.add("WH1;1;1;100");
        warehouse.getBays(testLines);
        assertFalse(warehouse.getWarehouses().isEmpty());
    }

    @Test
    void testGetItems() {
        testLines.add("SKU,Name,Category,Unit,Volume,UnitWeight");
        testLines.add("SKU0001,Item1,Cat1,PC,1.0,0.5");
        warehouse.getItems(testLines);
        assertFalse(warehouse.getItems().isEmpty());
    }

    @Test
    void testGetOrderLines() throws IOException {
        warehouse.getItems(Files.readAllLines(Paths.get(Warehouse.ITEMS_FILE)));
        testLines.add("OrderID,LineNo,SKU,Quantity");
        testLines.add("O1,1,SKU0001,5");
        warehouse.getOrderLines(testLines);
        assertFalse(warehouse.getOrderLines().isEmpty());
    }

    @Test
    void testGetOrders() {
        testLines.add("OrderID,DueDate,Priority");
        testLines.add("O1,2024-12-31,1");
        warehouse.getOrders(testLines);
        assertFalse(warehouse.getOrders().isEmpty());
    }

    @Test
    void testGetReturns() throws IOException {
        warehouse.getItems(Files.readAllLines(Paths.get(Warehouse.ITEMS_FILE)));
        testLines.add("ReturnID,SKU,Quantity,Reason,Timestamp,ExpiryDate");
        testLines.add("R1,SKU0001,2,Damaged,2023-10-10,2024-12-31");
        warehouse.getReturns(testLines);
        assertFalse(warehouse.getReturns().isEmpty());
    }

    @Test
    void testGetWagonsMap() {
        assertTrue(warehouse.getWagonsMap().isEmpty());
    }

    @Test
    void testGetWarehousesMap() {
        assertTrue(warehouse.getWarehouses().isEmpty());
    }

    @Test
    void testGetItemsMap() {
        assertTrue(warehouse.getItems().isEmpty());
    }

    @Test
    void testGetOrderLinesMap() {
        assertTrue(warehouse.getOrderLines().isEmpty());
    }

    @Test
    void testGetOrdersMap() {
        assertTrue(warehouse.getOrders().isEmpty());
    }

    @Test
    void testGetReturnsMap() {
        assertTrue(warehouse.getReturns().isEmpty());
    }

    @Test
    void testLoadAllData() throws IOException {
        warehouse.loadAllData();
        assertFalse(warehouse.getWagonsMap().isEmpty());
        assertFalse(warehouse.getWarehouses().isEmpty());
        assertFalse(warehouse.getItems().isEmpty());
        assertFalse(warehouse.getOrderLines().isEmpty());
        assertFalse(warehouse.getOrders().isEmpty());
        assertFalse(warehouse.getReturns().isEmpty());

        // After loadAllData, wagons should be unloaded into bays,
        // So wagons should be empty, but bays should have boxes
        for (Set<Box> boxes : warehouse.getWagonsMap().values()) {
            assertTrue(boxes.isEmpty(), "Wagons should be empty after unloading");
        }

        // Verify that at least one bay has boxes
        boolean foundBoxesInBays = false;
        for (Set<Bay> bays : warehouse.getWarehouses().values()) {
            for (Bay bay : bays) {
                if (!bay.getBoxes().isEmpty()) {
                    foundBoxesInBays = true;
                    break;
                }
            }
        }
        assertTrue(foundBoxesInBays, "Bays should contain boxes after unloading");
    }


    @Test
    void testEmptyFiles() {
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        warehouse.getWagons(testLines);
        assertTrue(warehouse.getWagonsMap().isEmpty());
    }

    @Test
    void testDuplicateBoxId() throws IOException {
        warehouse.getItems(Files.readAllLines(Paths.get(Warehouse.ITEMS_FILE)));
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add("W1,B1,SKU0001,10,2024-12-31,2023-10-10");
        testLines.add("W2,B1,SKU0001,10,2024-12-31,2023-10-10"); // Same BoxID
        warehouse.getWagons(testLines);

        // Should have error about duplicate boxID
        assertFalse(warehouse.getImportErrors().isEmpty());
        assertTrue(warehouse.getImportErrors().stream()
                .anyMatch(e -> e.contains("Duplicate boxID")));
    }

    @Test
    void testInvalidDataFormat() {
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add("W1,B1,SKU0001,invalid,2024-12-31,2023-10-10");
        warehouse.getWagons(testLines);
        assertFalse(warehouse.getImportErrors().isEmpty());
        assertTrue(warehouse.getImportErrors().get(0).contains("Invalid quantity"));
    }

    @Test
    void testMalformedCSVData() {
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add("W1,B1,SKU0001,10,2024-12-31");
        warehouse.getWagons(testLines);
        assertFalse(warehouse.getImportErrors().isEmpty());
        assertTrue(warehouse.getImportErrors().get(0).contains("Invalid line"));
    }

    @Test
    void testMissingReceivedAt() throws IOException {
        warehouse.getItems(Files.readAllLines(Paths.get(Warehouse.ITEMS_FILE)));
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add("W1,B1,SKU0001,10,2024-12-31,"); // Empty receivedAt
        warehouse.getWagons(testLines);
        assertFalse(warehouse.getImportErrors().isEmpty());
        assertTrue(warehouse.getImportErrors().stream()
                .anyMatch(e -> e.contains("Missing receivedAt")));
    }

    @Test
    void testEmptyBaysData() {
        testLines.add("WarehouseID,Aisle,Bay,BoxCapacity");
        warehouse.getBays(testLines);
        assertTrue(warehouse.getWarehouses().isEmpty());
    }

    @Test
    void testInvalidItemFormat() {
        testLines.add("SKU,Name,Category,Unit,Volume,UnitWeight");
        testLines.add("SKU0001,Item1,Cat1,PC,invalid,0.5");
        warehouse.getItems(testLines);
        assertFalse(warehouse.getImportErrors().isEmpty());
        assertTrue(warehouse.getImportErrors().get(0).contains("Invalid numeric"));
    }

    @Test
    void testDuplicateOrderLines() throws IOException {
        warehouse.getItems(Files.readAllLines(Paths.get(Warehouse.ITEMS_FILE)));
        testLines.add("OrderID,LineNo,SKU,Quantity");
        testLines.add("O1,1,SKU0001,5");
        testLines.add("O1,1,SKU0001,5");
        warehouse.getOrderLines(testLines);
        assertEquals(1, warehouse.getOrderLines().values().iterator().next().size());
    }

    @Test
    void testInvalidOrderPriority() {
        testLines.add("OrderID,DueDate,Priority");
        testLines.add("O1,2024-12-31,invalid");
        warehouse.getOrders(testLines);
        assertFalse(warehouse.getImportErrors().isEmpty());
        assertTrue(warehouse.getImportErrors().get(0).contains("Invalid priority"));
    }

    @Test
    void testInvalidReturnsQuantity() {
        testLines.add("ReturnID,SKU,Quantity,Reason,Timestamp,ExpiryDate");
        testLines.add("R1,SKU0001,invalid,Damaged,2023-10-10,2024-12-31");
        warehouse.getReturns(testLines);
        assertFalse(warehouse.getImportErrors().isEmpty());
        assertTrue(warehouse.getImportErrors().stream()
                .anyMatch(e -> e.contains("Invalid quantity")));
    }

    @Test
    void testMissingRequiredFields() {
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add(",B1,SKU0001,10,2024-12-31,2023-10-10");
        warehouse.getWagons(testLines);
        assertTrue(warehouse.getWagonsMap().isEmpty());
        assertFalse(warehouse.getImportErrors().isEmpty());
    }

    @Test
    void testUnloadWagonsIntoBays() {
        // Setup test data
        warehouse.getItems(List.of("SKU,Name,Category,Unit,Volume,UnitWeight",
                "SKU0001,Item1,Cat1,PC,1.0,0.5"));
        warehouse.getBays(List.of("WarehouseID;Aisle;Bay;BoxCapacity",
                "WH1;1;1;100"));
        warehouse.getWagons(List.of("wagonId,boxId,sku,qty,expiryDate,receivedAt",
                "WGN001,BOX00001,SKU0001,26,2026-01-05,2025-07-21T22:02:00"));

        // Before unloading, wagons should have boxes
        Wagon wagon = warehouse.getWagonsMap().keySet().iterator().next();
        assertFalse(warehouse.getWagonsMap().get(wagon).isEmpty());

        // Unload wagons into bays
        warehouse.unloadWagonsIntoBays();

        // After unloading, wagons should be empty
        assertTrue(warehouse.getWagonsMap().get(wagon).isEmpty());

        // Bays should now have boxes
        Set<Bay> bays = warehouse.getWarehouses().get("WH1");
        assertNotNull(bays);
        Bay bay = bays.iterator().next();
        assertFalse(bay.getBoxes().isEmpty());
        assertEquals(1, bay.getBoxes().size());
        assertEquals("BOX00001", bay.getBoxes().get(0).getBoxID());
    }

    @Test
    void testDispatchOperation() {
        // Setup test data
        warehouse.getItems(List.of("SKU,Name,Category,Unit,Volume,UnitWeight",
                "SKU0001,Item1,Cat1,PC,1.0,0.5"));
        warehouse.getBays(List.of("WarehouseID;Aisle;Bay;BoxCapacity",
                "WH1;1;1;100"));
        warehouse.getWagons(List.of("wagonId,boxId,sku,qty,expiryDate,receivedAt",
                "WGN001,BOX00001,SKU0001,20,2026-01-05,2025-07-21T22:02:00"));

        warehouse.unloadWagonsIntoBays();

        // Get the bay
        Bay bay = warehouse.getWarehouses().get("WH1").iterator().next();
        int initialQuantity = bay.getBoxes().get(0).getQuantity();
        assertEquals(20, initialQuantity);

        // Dispatch 5 units
        boolean success = warehouse.dispatch("SKU0001", 5, null);
        assertTrue(success);

        // Quantity should be reduced
        assertEquals(15, bay.getBoxes().get(0).getQuantity());

        // Dispatch all remaining
        success = warehouse.dispatch("SKU0001", 15, null);
        assertTrue(success);

        // Box should be removed
        assertTrue(bay.getBoxes().isEmpty());
    }

    @Test
    void testDispatchInsufficientStock() {
        warehouse.getItems(List.of("SKU,Name,Category,Unit,Volume,UnitWeight",
                "SKU0001,Item1,Cat1,PC,1.0,0.5"));
        warehouse.getBays(List.of("WarehouseID;Aisle;Bay;BoxCapacity",
                "WH1;1;1;100"));
        warehouse.getWagons(List.of("wagonId,boxId,sku,qty,expiryDate,receivedAt",
                "WGN001,BOX00001,SKU0001,10,2026-01-05,2025-07-21T22:02:00"));

        warehouse.unloadWagonsIntoBays();

        // Try to dispatch more than available
        boolean success = warehouse.dispatch("SKU0001", 50, null);
        assertFalse(success);
    }

    @Test
    void testRelocationOperation() {
        // Setup test data with multiple bays
        warehouse.getItems(List.of("SKU,Name,Category,Unit,Volume,UnitWeight",
                "SKU0001,Item1,Cat1,PC,1.0,0.5"));
        warehouse.getBays(List.of("WarehouseID;Aisle;Bay;BoxCapacity",
                "WH1;1;1;100",
                "WH1;1;2;100"));
        warehouse.getWagons(List.of("wagonId,boxId,sku,qty,expiryDate,receivedAt",
                "WGN001,BOX00001,SKU0001,20,2026-01-05,2025-07-21T22:02:00"));

        warehouse.unloadWagonsIntoBays();

        // Relocate box from bay 1 to bay 2
        boolean success = warehouse.relocate("BOX00001", "WH1", 1, 2);
        assertTrue(success);

        // Find the bays
        Bay bay1 = null, bay2 = null;
        for (Bay bay : warehouse.getWarehouses().get("WH1")) {
            if (bay.getBayNumber() == 1) bay1 = bay;
            if (bay.getBayNumber() == 2) bay2 = bay;
        }

        assertNotNull(bay1);
        assertNotNull(bay2);

        // Bay 1 should be empty, bay 2 should have the box
        assertTrue(bay1.getBoxes().isEmpty());
        assertFalse(bay2.getBoxes().isEmpty());
        assertEquals("BOX00001", bay2.getBoxes().get(0).getBoxID());
    }

    @Test
    void testRelocationNonExistentBox() {
        warehouse.getItems(List.of("SKU,Name,Category,Unit,Volume,UnitWeight",
                "SKU0001,Item1,Cat1,PC,1.0,0.5"));
        warehouse.getBays(List.of("WarehouseID;Aisle;Bay;BoxCapacity",
                "WH1;1;1;100"));

        boolean success = warehouse.relocate("NONEXISTENT", "WH1", 1, 1);
        assertFalse(success);
        assertFalse(warehouse.getImportErrors().isEmpty());
    }

    @Test
    void testFEFOOrdering() {
        warehouse.getItems(List.of("SKU,Name,Category,Unit,Volume,UnitWeight",
                "SKU0001,Item1,Cat1,PC,1.0,0.5"));
        warehouse.getBays(List.of("WarehouseID;Aisle;Bay;BoxCapacity",
                "WH1;1;1;100"));
        warehouse.getWagons(List.of("wagonId,boxId,sku,qty,expiryDate,receivedAt",
                "WGN001,BOX001,SKU0001,10,2025-12-31,2024-01-01T10:00:00",
                "WGN001,BOX002,SKU0001,10,2025-11-30,2024-01-01T11:00:00",
                "WGN001,BOX003,SKU0001,10,2025-12-15,2024-01-01T12:00:00"));

        warehouse.unloadWagonsIntoBays();

        Bay bay = warehouse.getWarehouses().get("WH1").iterator().next();
        List<Box> boxes = bay.getBoxes();

        // Debug: print actual order
        System.out.println("=== FEFO Order Test ===");
        for (int i = 0; i < boxes.size(); i++) {
            Box box = boxes.get(i);
            System.out.printf("%d: %s - expiry: %s, received: %s%n",
                    i, box.getBoxID(), box.getExpiryDate(), box.getReceivedAt());
        }

        // Boxes should be ordered by expiry date (earliest first - FEFO)
        // 2025-11-30 (Nov) < 2025-12-15 (Dec) < 2025-12-31 (Dec)
        assertEquals(3, boxes.size());
        assertEquals("BOX002", boxes.get(0).getBoxID()); // 2025-11-30 (expires first - November)
        assertEquals("BOX003", boxes.get(1).getBoxID()); // 2025-12-15 (expires second - mid December)
        assertEquals("BOX001", boxes.get(2).getBoxID()); // 2025-12-31 (expires last - end December)
    }

    @Test
    void testFIFOOrdering() {
        warehouse.getItems(List.of("SKU,Name,Category,Unit,Volume,UnitWeight",
                "SKU0001,Item1,Cat1,PC,1.0,0.5"));
        warehouse.getBays(List.of("WarehouseID;Aisle;Bay;BoxCapacity",
                "WH1;1;1;100"));
        warehouse.getWagons(List.of("wagonId,boxId,sku,qty,expiryDate,receivedAt",
                "WGN001,BOX001,SKU0001,10,,2024-03-01T10:00:00",
                "WGN001,BOX002,SKU0001,10,,2024-01-01T11:00:00", // Oldest
                "WGN001,BOX003,SKU0001,10,,2024-02-01T12:00:00"));

        warehouse.unloadWagonsIntoBays();

        Bay bay = warehouse.getWarehouses().get("WH1").iterator().next();
        List<Box> boxes = bay.getBoxes();

        // Boxes without expiry should be ordered by receivedAt (oldest first - FIFO)
        assertEquals("BOX002", boxes.get(0).getBoxID()); // 2024-01-01
        assertEquals("BOX003", boxes.get(1).getBoxID()); // 2024-02-01
        assertEquals("BOX001", boxes.get(2).getBoxID()); // 2024-03-01
    }

    @Test
    void testProcessExistingOrders() {
        warehouse.getItems(List.of("SKU,Name,Category,Unit,Volume,UnitWeight",
                "SKU0001,Item1,Cat1,PC,1.0,0.5"));
        warehouse.getBays(List.of("WarehouseID;Aisle;Bay;BoxCapacity",
                "WH1;1;1;100"));
        warehouse.getWagons(List.of("wagonId,boxId,sku,qty,expiryDate,receivedAt",
                "WGN001,BOX00001,SKU0001,26,2026-01-05,2025-07-21T22:02:00"));
        warehouse.getOrders(List.of("orderId,dueDate,priority", "O1,2024-12-31,1"));
        warehouse.getOrderLines(List.of("orderId,lineNo,sku,qty", "O1,1,SKU0001,6"));

        // Manually trigger the full flow
        warehouse.unloadWagonsIntoBays();

        // Get initial quantity in bay
        Bay bay = warehouse.getWarehouses().get("WH1").iterator().next();
        int initialQty = bay.getBoxes().get(0).getQuantity();
        assertEquals(26, initialQty);

        // Process the order
        boolean success = warehouse.dispatch("SKU0001", 6, null);
        assertTrue(success);

        // After processing order, quantity should be reduced
        int finalQty = bay.getBoxes().get(0).getQuantity();
        assertEquals(20, finalQty);

        assertTrue(warehouse.getImportErrors().isEmpty());
    }

    @Test
    void testPrintMovementHistory(){
        // Setup test data
        warehouse.getItems(List.of("SKU,Name,Category,Unit,Volume,UnitWeight",
                "SKU0001,Item1,Cat1,PC,1.0,0.5"));
        warehouse.getBays(List.of("WarehouseID;Aisle;Bay;BoxCapacity",
                "WH1;1;1;100"));
        warehouse.getWagons(List.of("wagonId,boxId,sku,qty,expiryDate,receivedAt",
                "WGN001,BOX001,SKU0001,20,2026-01-05,2025-07-21T22:02:00"));

        warehouse.unloadWagonsIntoBays();

        assertDoesNotThrow(() -> warehouse.printMovementHistory());
        assertFalse(warehouse.getMovementHistory().isEmpty()); // verify if movement exists
    }

    @Test
    void testExportToCSV() {
        // Setup test data
        warehouse.getItems(List.of("SKU,Name,Category,Unit,Volume,UnitWeight",
                "SKU0001,Item1,Cat1,PC,1.0,0.5"));
        warehouse.getBays(List.of("WarehouseID;Aisle;Bay;BoxCapacity",
                "WH1;1;1;100"));
        warehouse.getWagons(List.of("wagonId,boxId,sku,qty,expiryDate,receivedAt",
                "WGN001,BOX001,SKU0001,20,2026-01-05,2025-07-21T22:02:00"));

        warehouse.unloadWagonsIntoBays();

        // Print movement to simulate a process
        warehouse.printMovementHistory();

        // Export to CSV
        String csvContent = warehouse.exportToCSV();
        assertNotNull(csvContent);
        String[] csvLines = csvContent.split("\n");

        // Verify header
        assertTrue(csvLines.length > 1);
        assertEquals("MovementID,Source,Destination,BoxID,SKU,Timestamp", csvLines[0]);

        // Verify at least one movement exists in the CSV content
        assertTrue(csvLines.length > 1);
    }

}