package esinf;

import esinf.us01.Warehouse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
    }

    @Test
    void testInvalidFilePath() {
        Warehouse.WAGONS_FILE = "invalid/path/wagons.csv";
        assertThrows(IOException.class, () -> warehouse.loadAllData());
    }

    @Test
    void testEmptyFiles() {
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        warehouse.getWagons(testLines);
        assertTrue(warehouse.getWagonsMap().isEmpty());
    }

    @Test
    void testDuplicateData() throws IOException {
        warehouse.getItems(Files.readAllLines(Paths.get(Warehouse.ITEMS_FILE)));
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add("W1,B1,SKU0001,10,2024-12-31,2023-10-10");
        testLines.add("W1,B1,SKU0001,10,2024-12-31,2023-10-10");
        warehouse.getWagons(testLines);
        assertEquals(1, warehouse.getWagonsMap().size());
    }

    @Test
    void testInvalidDataFormat() {
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add("W1,B1,SKU0001,invalid,2024-12-31,2023-10-10");
        warehouse.getWagons(testLines);
        assertFalse(warehouse.getImportErrors().isEmpty());
        assertTrue(warehouse.getImportErrors().getFirst().contains("Invalid quantity"));
    }

    @Test
    void testMalformedCSVData() {
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add("W1,B1,SKU0001,10,2024-12-31");
        warehouse.getWagons(testLines);
        assertFalse(warehouse.getImportErrors().isEmpty());
        assertTrue(warehouse.getImportErrors().getFirst().contains("Invalid line"));
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
        assertTrue(warehouse.getImportErrors().getFirst().contains("Invalid numeric"));
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
        assertTrue(warehouse.getImportErrors().getFirst().contains("Invalid priority"));
    }

    @Test
    void testInvalidReturnsQuantity() {
        testLines.add("ReturnID,SKU,Quantity,Reason,Timestamp,ExpiryDate");
        testLines.add("R1,SKU0001,invalid,Damaged,2023-10-10,2024-12-31");
        warehouse.getReturns(testLines);
        assertFalse(warehouse.getImportErrors().isEmpty());
        assertTrue(warehouse.getImportErrors().getFirst().contains("Invalid qty"));
    }

    @Test
    void testMissingRequiredFields() {
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add(",B1,SKU0001,10,2024-12-31,2023-10-10");
        warehouse.getWagons(testLines);
        assertTrue(warehouse.getWagonsMap().isEmpty());
    }
}