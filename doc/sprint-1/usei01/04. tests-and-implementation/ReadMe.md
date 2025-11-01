# USEI01 - Wagons USEI01 - Wagons Unloading (Inventory Replenishment)

## 4. Tests

**Test 1:** Validates data in wagons.csv file.

```java
void testGetWagons() throws IOException {
    warehouse.getItems(Files.readAllLines(Paths.get(Warehouse.ITEMS_FILE)));
    testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
    testLines.add("W1,B1,SKU0001,10,2024-12-31,2023-10-10");
    warehouse.getWagons(testLines);
    assertFalse(warehouse.getWagonsMap().isEmpty());
}
```


**Test 2:** Validates data import from files

```java
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
```


**Test 3:** Validates data import from an empty file

```java
void testEmptyFiles() {
    testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
    warehouse.getWagons(testLines);
    assertTrue(warehouse.getWagonsMap().isEmpty());
}
```

**Test 4:** Validates data import with malformed csv file

```java
void testMalformedCSVData() {
    testLines.add("WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate");
    testLines.add("W1,B1,SKU0001,10,2024-12-31");
    warehouse.getWagons(testLines);
    assertFalse(warehouse.getImportErrors().isEmpty());
    assertTrue(warehouse.getImportErrors().get(0).contains("Invalid line"));
}
```


**Test 5:** Validates data import with invalid order priority

```java
void testInvalidOrderPriority() {
    testLines.add("OrderID,DueDate,Priority");
    testLines.add("O1,2024-12-31,invalid");
    warehouse.getOrders(testLines);
    assertFalse(warehouse.getImportErrors().isEmpty());
    assertTrue(warehouse.getImportErrors().get(0).contains("Invalid priority"));
}
```


**Test 6:** Validates unloading of wagons into bays

```java
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
```


**Test 7:** Validates dispatch operation

```java
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
```


**Test 8:** Validates relocation operation

```java
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
```

**Test 9:** Validates FEFO order

```java
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
```


**Test 10:** Validates FIFO order

```java
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
```




## 5. Construction (Implementation)

### Class Warehouse.java

```java
package esinf.us01;

import esinf.us05.Returns;
import warehouse.Bay;
import warehouse.Box;
import warehouse.Item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Warehouse class manages the storage, loading, and unloading operations
 * for boxes inside wagons and bays, following FEFO/FIFO rules for perishable
 * and non-perishable goods.
 */
public class Warehouse {
    private Map<Wagon, Set<Box>> wagons = new HashMap<>();
    private Map<String, Set<Bay>> warehouses = new HashMap<>();
    private Map<String, Item> items = new HashMap<>();
    private Map<Order, Set<OrderLines>> order_lines = new HashMap<>();
    private Map<String, Order> orders = new HashMap<>();
    private Map<String, Returns> returns = new HashMap<>();
    private List<String> importErrors = new ArrayList<>();
    private Set<String> allBoxIds = new HashSet<>(); // Track unique boxIds

    private static final String CSV_BASE_PATH = "csv-docs/";
    public static String WAGONS_FILE = CSV_BASE_PATH + "wagons.csv";
    public static final String BAYS_FILE = CSV_BASE_PATH + "bays.csv";
    public static final String ITEMS_FILE = CSV_BASE_PATH + "items.csv";
    public static final String ORDER_LINES_FILE = CSV_BASE_PATH + "order_lines.csv";
    public static final String ORDERS_FILE = CSV_BASE_PATH + "orders.csv";
    public static final String RETURNS_FILE = CSV_BASE_PATH + "returns.csv";

    // Getters
    public Map<Wagon, Set<Box>> getWagonsMap() {
        return wagons;
    }

    public Map<String, Set<Bay>> getWarehouses() {
        return warehouses;
    }

    public Map<String, Item> getItems() {
        return items;
    }

    public Map<Order, Set<OrderLines>> getOrderLines() {
        return order_lines;
    }

    public Map<String, Order> getOrders() {
        return orders;
    }

    public Map<String, Returns> getReturns() {
        return returns;
    }

    public List<String> getImportErrors() {
        return importErrors;
    }

    /**
     * Loads all CSV data into memory with validation.
     * After loading, unloads wagons into bays and processes existing orders.
     */
    public void loadAllData() throws IOException {
        importErrors.clear();
        allBoxIds.clear();

        getItems(Files.readAllLines(Paths.get(ITEMS_FILE)));
        getWagons(Files.readAllLines(Paths.get(WAGONS_FILE)));
        getBays(Files.readAllLines(Paths.get(BAYS_FILE)));
        getOrders(Files.readAllLines(Paths.get(ORDERS_FILE)));
        getOrderLines(Files.readAllLines(Paths.get(ORDER_LINES_FILE)));
        getReturns(Files.readAllLines(Paths.get(RETURNS_FILE)));

        unloadWagonsIntoBays(); // Move boxes from wagons to bays
        processExistingOrders(); // Process orders after unloading

        if (!importErrors.isEmpty()) {
            System.err.println("=== IMPORT ERRORS ===");
            importErrors.forEach(System.err::println);
        }
    }

    /** Utility: checks if string is a valid integer. */
    private boolean isValidInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Utility: checks if string is a valid double. */
    private boolean isValidDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Utility: checks if string is a valid ISO date or timestamp (allow blank for optional fields). */
    private boolean isValidDateOrTimestamp(String s) {
        if (s == null || s.isBlank()) return true;
        try {
            LocalDateTime.parse(s);
            return true;
        } catch (DateTimeParseException e1) {
            try {
                LocalDate.parse(s);
                return true;
            } catch (DateTimeParseException e2) {
                return false;
            }
        }
    }

    /**
     * Loads wagons and boxes from CSV lines.
     * Validates quantities, dates, SKU references, and ensures box uniqueness.
     */
    public void getWagons(List<String> lines) {
        Wagon currentWagon = null;
        Set<Box> currentBoxes = null;

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);
            if (parts.length < 6) {
                importErrors.add("Invalid line in wagons.csv at " + (i + 1) + ": wrong column count");
                continue;
            }

            String wagonID = parts[0].trim();
            String boxID = parts[1].trim();
            String sku = parts[2].trim();
            String qtyStr = parts[3].trim();
            String expiryDate = parts[4].trim();
            String receivedDate = parts[5].trim();

            if (wagonID.isBlank() || boxID.isBlank() || sku.isBlank()) {
                importErrors.add("Missing IDs in wagons.csv line " + (i + 1));
                continue;
            }

            // Validate boxId uniqueness
            if (allBoxIds.contains(boxID)) {
                importErrors.add("Duplicate boxID " + boxID + " in wagons.csv line " + (i + 1));
                continue;
            }

            if (!isValidInteger(qtyStr)) {
                importErrors.add("Invalid quantity for box " + boxID + " in wagons.csv line " + (i + 1));
                continue;
            }
            int quantity = Integer.parseInt(qtyStr);
            if (quantity <= 0) {
                importErrors.add("Non-positive quantity in wagons.csv line " + (i + 1));
                continue;
            }

            if (receivedDate.isBlank()) {
                importErrors.add("Missing receivedAt in wagons.csv line " + (i + 1));
                continue;
            }
            if (!isValidDateOrTimestamp(receivedDate)) {
                importErrors.add("Invalid receivedAt timestamp in wagons.csv line " + (i + 1));
                continue;
            }
            if (!isValidDateOrTimestamp(expiryDate)) {
                importErrors.add("Invalid expiryDate in wagons.csv line " + (i + 1));
                continue;
            }
            if (!items.containsKey(sku)) {
                importErrors.add("Unknown SKU " + sku + " in wagons.csv line " + (i + 1));
                continue;
            }

            if (currentWagon == null || !currentWagon.getWagonID().equals(wagonID)) {
                if (currentWagon != null && currentBoxes != null) wagons.put(currentWagon, currentBoxes);
                currentWagon = new Wagon(wagonID);
                currentBoxes = new HashSet<>();
            }

            Box box = new Box(boxID, sku, quantity,
                    expiryDate.isBlank() ? null : expiryDate,
                    receivedDate);
            currentBoxes.add(box);
            allBoxIds.add(boxID);
        }

        if (currentWagon != null && currentBoxes != null) wagons.put(currentWagon, currentBoxes);
    }

    /**
     * Loads bays for each warehouse.
     * Validates numeric fields and assigns bays to the correct warehouse.
     */
    public void getBays(List<String> lines) {
        String currentWarehouseID = null;
        Set<Bay> currentBays = null;

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(";", -1);
            if (parts.length < 4) {
                importErrors.add("Invalid line in bays.csv at " + (i + 1));
                continue;
            }

            String warehouseID = parts[0].trim();
            String aisleStr = parts[1].trim();
            String bayStr = parts[2].trim();
            String capacityStr = parts[3].trim();

            if (!isValidInteger(aisleStr) || !isValidInteger(bayStr) || !isValidInteger(capacityStr)) {
                importErrors.add("Invalid numeric fields in bays.csv line " + (i + 1));
                continue;
            }

            int aisle = Integer.parseInt(aisleStr);
            int bay = Integer.parseInt(bayStr);
            int boxCapacity = Integer.parseInt(capacityStr);

            if (currentWarehouseID == null || !currentWarehouseID.equals(warehouseID)) {
                if (currentWarehouseID != null && currentBays != null) warehouses.put(currentWarehouseID, currentBays);
                currentWarehouseID = warehouseID;
                currentBays = new HashSet<>();
            }

            Bay b = new Bay(bay, aisle, boxCapacity);
            currentBays.add(b);
        }

        if (currentWarehouseID != null && currentBays != null) warehouses.put(currentWarehouseID, currentBays);
    }

    /**
     * Loads items from CSV lines with validation.
     * Each item must have a valid SKU, name, numeric volume and weight.
     */
    public void getItems(List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);
            if (parts.length < 6) {
                importErrors.add("Invalid line in items.csv at " + (i + 1) + ": wrong column count");
                continue;
            }

            String sku = parts[0].trim();
            String name = parts[1].trim();
            String category = parts[2].trim();
            String unit = parts[3].trim();
            String volumeStr = parts[4].trim();
            String weightStr = parts[5].trim();

            if (sku.isBlank() || name.isBlank()) {
                importErrors.add("Missing SKU or name in items.csv line " + (i + 1));
                continue;
            }
            if (!isValidDouble(volumeStr) || !isValidDouble(weightStr)) {
                importErrors.add("Invalid numeric fields in items.csv line " + (i + 1));
                continue;
            }

            Item item = new Item(sku, name, category, unit,
                    Double.parseDouble(volumeStr),
                    Double.parseDouble(weightStr));
            items.put(sku, item);
        }
    }

    /**
     * Loads orders from CSV lines with validation.
     * Each order must have a valid ID and numeric priority.
     * The dueDate is optional but must be valid if provided.
     */
    public void getOrders(List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);
            if (parts.length < 3) {
                importErrors.add("Invalid line in orders.csv at " + (i + 1) + ": wrong column count");
                continue;
            }

            String orderId = parts[0].trim();
            String dueDateStr = parts[1].trim();
            String priorityStr = parts[2].trim();

            if (orderId.isBlank()) {
                importErrors.add("Missing orderID in orders.csv line " + (i + 1));
                continue;
            }

            if (!isValidInteger(priorityStr)) {
                importErrors.add("Invalid priority in orders.csv line " + (i + 1));
                continue;
            }

            LocalDate dueDate = null;
            if (!dueDateStr.isBlank()) {
                try {
                    dueDate = LocalDate.parse(dueDateStr);
                } catch (DateTimeParseException e1) {
                    try {
                        dueDate = LocalDateTime.parse(dueDateStr).toLocalDate();
                    } catch (DateTimeParseException e2) {
                        importErrors.add("Invalid dueDate in orders.csv line " + (i + 1));
                        continue;
                    }
                }
            }

            Order order = new Order(orderId, Integer.parseInt(priorityStr),
                    dueDate != null ? dueDate.toString() : null);
            orders.put(orderId, order);
        }
    }

    /**
     * Loads order lines from CSV lines with validation.
     * Each line must have a valid numeric lineNo and quantity, and reference a known SKU and order.
     */
    public void getOrderLines(List<String> lines) {
        Order currentOrder = null;
        Set<OrderLines> currentLines = null;

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);
            if (parts.length < 4) {
                importErrors.add("Invalid line in order_lines.csv at " + (i + 1));
                continue;
            }

            String orderId = parts[0].trim();
            String lineNoStr = parts[1].trim();
            String sku = parts[2].trim();
            String qtyStr = parts[3].trim();

            if (!isValidInteger(lineNoStr) || !isValidInteger(qtyStr)) {
                importErrors.add("Invalid numeric values in order_lines.csv line " + (i + 1));
                continue;
            }

            if (!items.containsKey(sku)) {
                importErrors.add("Unknown SKU " + sku + " in order_lines.csv line " + (i + 1));
                continue;
            }

            int lineNo = Integer.parseInt(lineNoStr);
            int qty = Integer.parseInt(qtyStr);

            if (currentOrder == null || !currentOrder.getOrderID().equals(orderId)) {
                if (currentOrder != null && currentLines != null)
                    order_lines.put(currentOrder, currentLines);
                currentOrder = orders.getOrDefault(orderId, new Order(orderId));
                orders.put(orderId, currentOrder);
                currentLines = new HashSet<>();
            }

            OrderLines ol = new OrderLines(lineNo, sku, qty);
            currentLines.add(ol);
        }

        if (currentOrder != null && currentLines != null)
            order_lines.put(currentOrder, currentLines);
    }

    /**
     * Loads returns from CSV lines with validation.
     * Each return must have valid SKU, quantity, and timestamps/dates if provided.
     */
    public void getReturns(List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);
            if (parts.length < 6) {
                importErrors.add("Invalid line in returns.csv at " + (i + 1));
                continue;
            }

            String returnID = parts[0].trim();
            String sku = parts[1].trim();
            String qtyStr = parts[2].trim();
            String reason = parts[3].trim();
            String timestamp = parts[4].trim();
            String expiryDate = parts[5].trim();

            if (returnID.isBlank()) {
                importErrors.add("Missing returnID in returns.csv line " + (i + 1));
                continue;
            }

            if (!isValidInteger(qtyStr)) {
                importErrors.add("Invalid quantity in returns.csv line " + (i + 1));
                continue;
            }

            if (!isValidDateOrTimestamp(timestamp)) {
                importErrors.add("Invalid timestamp in returns.csv line " + (i + 1));
                continue;
            }

            if (!isValidDateOrTimestamp(expiryDate)) {
                importErrors.add("Invalid expiryDate in returns.csv line " + (i + 1));
                continue;
            }

            if (!items.containsKey(sku)) {
                importErrors.add("Unknown SKU " + sku + " in returns.csv line " + (i + 1));
                continue;
            }

            Returns r = new Returns(returnID, sku, Integer.parseInt(qtyStr), reason, timestamp,
                    expiryDate.isBlank() ? null : expiryDate);
            returns.put(returnID, r);
        }
    }

    /**
     * Unloads all boxes from wagons into warehouse bays.
     * Distributes boxes across available bays respecting capacity limits.
     * Boxes are inserted in FEFO/FIFO order within each bay.
     */
    public void unloadWagonsIntoBays() {
        for (Map.Entry<Wagon, Set<Box>> entry : wagons.entrySet()) {
            Set<Box> boxes = entry.getValue();

            // Distribute boxes to warehouses
            for (Box box : boxes) {
                boolean placed = false;

                // Try to place in any warehouse with available capacity
                for (Map.Entry<String, Set<Bay>> warehouseEntry : warehouses.entrySet()) {
                    String warehouseId = warehouseEntry.getKey();
                    Set<Bay> bays = warehouseEntry.getValue();

                    // Sort bays by aisle and bay number
                    List<Bay> sortedBays = new ArrayList<>(bays);
                    sortedBays.sort(Comparator.comparingInt(Bay::getAisle)
                            .thenComparingInt(Bay::getBayNumber));

                    for (Bay bay : sortedBays) {
                        if (bay.hasCapacity()) {
                            bay.addBoxInFEFOOrder(box);
                            box.setLocation(warehouseId, bay.getAisle(), bay.getBayNumber());
                            placed = true;
                            break;
                        }
                    }

                    if (placed) break;
                }

                if (!placed) {
                    importErrors.add("No available bay capacity for box " + box.getBoxID());
                }
            }
        }

        // Clear wagons after unloading
        for (Wagon wagon : wagons.keySet()) {
            wagons.put(wagon, new HashSet<>());
        }
    }

    /**
     * Processes all existing orders by dispatching items from bays.
     */
    private void processExistingOrders() {
        for (Map.Entry<Order, Set<OrderLines>> entry : order_lines.entrySet()) {
            Order order = entry.getKey();
            Set<OrderLines> lines = entry.getValue();

            for (OrderLines line : lines) {
                boolean success = dispatch(line.getSku(), line.getQty(), null);
                if (!success) {
                    importErrors.add("Could not fulfill order " + order.getOrderID() +
                            " line " + line.getLineNo() + " for SKU " + line.getSku());
                }
            }
        }
    }

    /**
     * Dispatches items from warehouse bays following FEFO/FIFO rules.
     * Consumes stock from the front of bay lists, supporting partial dispatch
     * across multiple bays.
     *
     * @param sku the SKU to dispatch
     * @param quantity the quantity to dispatch
     * @param warehouseId optional warehouse ID (null = search all warehouses)
     * @return true if fully dispatched, false if insufficient stock
     */
    public boolean dispatch(String sku, int quantity, String warehouseId) {
        int remaining = quantity;

        // Determine which warehouses to search
        List<String> warehousesToSearch = new ArrayList<>();
        if (warehouseId != null && warehouses.containsKey(warehouseId)) {
            warehousesToSearch.add(warehouseId);
        } else {
            warehousesToSearch.addAll(warehouses.keySet());
        }

        for (String whId : warehousesToSearch) {
            Set<Bay> bays = warehouses.get(whId);

            // Sort bays by aisle and bay number for consistent processing
            List<Bay> sortedBays = new ArrayList<>(bays);
            sortedBays.sort(Comparator.comparingInt(Bay::getAisle)
                    .thenComparingInt(Bay::getBayNumber));

            for (Bay bay : sortedBays) {
                remaining = bay.dispatchFromFront(sku, remaining);

                if (remaining == 0) {
                    return true; // Fully dispatched
                }
            }
        }

        return remaining == 0;
    }

    /**
     * Relocates a box from its current bay to a new location.
     * The box is removed from its current bay and inserted into the new bay
     * in the correct FEFO/FIFO position.
     *
     * @param boxId the box to relocate
     * @param newWarehouseId the target warehouse
     * @param newAisle the target aisle
     * @param newBayNumber the target bay number
     * @return true if relocation successful, false otherwise
     */
    public boolean relocate(String boxId, String newWarehouseId, int newAisle, int newBayNumber) {
        // Find the box in current location
        Box boxToRelocate = null;
        Bay currentBay = null;
        String currentWarehouseId = null;

        for (Map.Entry<String, Set<Bay>> entry : warehouses.entrySet()) {
            for (Bay bay : entry.getValue()) {
                Box box = bay.removeBox(boxId);
                if (box != null) {
                    boxToRelocate = box;
                    currentBay = bay;
                    currentWarehouseId = entry.getKey();
                    break;
                }
            }
            if (boxToRelocate != null) break;
        }

        if (boxToRelocate == null) {
            importErrors.add("Box " + boxId + " not found for relocation");
            return false;
        }

        // Find target bay
        Set<Bay> targetBays = warehouses.get(newWarehouseId);
        if (targetBays == null) {
            importErrors.add("Warehouse " + newWarehouseId + " not found");
            // Return box to original location
            currentBay.addBoxInFEFOOrder(boxToRelocate);
            return false;
        }

        Bay targetBay = null;
        for (Bay bay : targetBays) {
            if (bay.getAisle() == newAisle && bay.getBayNumber() == newBayNumber) {
                targetBay = bay;
                break;
            }
        }

        if (targetBay == null) {
            importErrors.add("Bay not found: aisle " + newAisle + ", bay " + newBayNumber);
            // Return box to original location
            currentBay.addBoxInFEFOOrder(boxToRelocate);
            return false;
        }

        if (!targetBay.hasCapacity()) {
            importErrors.add("Target bay has no capacity");
            // Return box to original location
            currentBay.addBoxInFEFOOrder(boxToRelocate);
            return false;
        }

        // Insert into new bay in FEFO order
        targetBay.addBoxInFEFOOrder(boxToRelocate);
        boxToRelocate.setLocation(newWarehouseId, newAisle, newBayNumber);

        return true;
    }

    /**
     * Utility method for debugging - prints expiration dates of boxes.
     */
    public void verifyExpirationDate(List<Box> boxes) {
        for (Box box : boxes) {
            if (box.hasExpirationDate())
                System.out.printf("Box %s has an expiration date: %s%n", box.getBoxID(), box.getExpiryDate());
            else
                System.out.printf("Box %s has no expiration date%n", box.getBoxID());
        }
    }
}
```