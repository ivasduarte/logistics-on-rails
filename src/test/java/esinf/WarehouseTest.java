package esinf;

import esinf.us01.Warehouse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
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
    void testGetWagons() {
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add("W1,B1,SKU1,10,2024-12-31,2023-10-10");
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
        testLines.add("SKU1,Item1,Cat1,PC,1.0,0.5");
        warehouse.getItems(testLines);
        assertFalse(warehouse.getItems().isEmpty());
    }

    @Test
    void testGetOrderLines() {
        testLines.add("OrderID,LineNo,SKU,Quantity");
        testLines.add("O1,1,SKU1,5");
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
    void testGetReturns() {
        testLines.add("ReturnID,SKU,Quantity,Reason,Timestamp,ExpiryDate");
        testLines.add("R1,SKU1,2,Damaged,2023-10-10,2024-12-31");
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
    void testDuplicateData() {
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add("W1,B1,SKU1,10,2024-12-31,2023-10-10");
        testLines.add("W1,B1,SKU1,10,2024-12-31,2023-10-10");
        warehouse.getWagons(testLines);
        assertEquals(1, warehouse.getWagonsMap().size());
    }

    @Test
    void testInvalidDataFormat() {
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add("W1,B1,SKU1,invalid,2024-12-31,2023-10-10");
        assertThrows(NumberFormatException.class, () -> warehouse.getWagons(testLines));
    }

    @Test
    void testMalformedCSVData() {
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add("W1,B1,SKU1,10,2024-12-31");
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> warehouse.getWagons(testLines));
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
        testLines.add("SKU1,Item1,Cat1,PC,invalid,0.5");
        assertThrows(NumberFormatException.class, () -> warehouse.getItems(testLines));
    }

    @Test
    void testDuplicateOrderLines() {
        testLines.add("OrderID,LineNo,SKU,Quantity");
        testLines.add("O1,1,SKU1,5");
        testLines.add("O1,1,SKU1,5");
        warehouse.getOrderLines(testLines);
        assertEquals(1, warehouse.getOrderLines().values().iterator().next().size());
    }

    @Test
    void testInvalidOrderPriority() {
        testLines.add("OrderID,DueDate,Priority");
        testLines.add("O1,2024-12-31,invalid");
        assertThrows(NumberFormatException.class, () -> warehouse.getOrders(testLines));
    }

    @Test
    void testInvalidReturnsQuantity() {
        testLines.add("ReturnID,SKU,Quantity,Reason,Timestamp,ExpiryDate");
        testLines.add("R1,SKU1,invalid,Damaged,2023-10-10,2024-12-31");
        assertThrows(NumberFormatException.class, () -> warehouse.getReturns(testLines));
    }

    @Test
    void testMissingRequiredFields() {
        testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
        testLines.add(",B1,SKU1,10,2024-12-31,2023-10-10");
        warehouse.getWagons(testLines);
        assertFalse(warehouse.getWagonsMap().isEmpty());
    }
}