package esinf.us01;

import esinf.us02.Allocation;
import esinf.us02.OrderAllocationResult;
import esinf.us02.OrderLineResult;
import esinf.us05.ReturnReasons;
import esinf.us05.Returns;
import esinf.us05.StockAction;

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

    private List<Movements> movementHistory = new ArrayList<>();

    private static final String CSV_BASE_PATH = "csv-docs/";
    public static String WAGONS_FILE = CSV_BASE_PATH + "wagons.csv";
    public static final String BAYS_FILE = CSV_BASE_PATH + "bays.csv";
    public static final String ITEMS_FILE = CSV_BASE_PATH + "items.csv";
    public static final String ORDER_LINES_FILE = CSV_BASE_PATH + "order_lines.csv";
    public static final String ORDERS_FILE = CSV_BASE_PATH + "orders.csv";
    public static final String RETURNS_FILE = CSV_BASE_PATH + "returns.csv";

    // Getters
    public Map<Wagon, Set<Box>> getWagonsMap() { return wagons; }
    public Map<String, Set<Bay>> getWarehouses() { return warehouses; }
    public Map<String, Item> getItems() { return items; }
    public Map<Order, Set<OrderLines>> getOrderLines() { return order_lines; }
    public Map<String, Order> getOrders() { return orders; }
    public Map<String, Returns> getReturns() { return returns; }
    public List<String> getImportErrors() { return importErrors; }
    public List<Movements> getMovementHistory() { return movementHistory; }

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
        try { Integer.parseInt(s); return true; }
        catch (NumberFormatException e) { return false; }
    }

    /** Utility: checks if string is a valid double. */
    private boolean isValidDouble(String s) {
        try { Double.parseDouble(s); return true; }
        catch (NumberFormatException e) { return false; }
    }

    /** Utility: checks if string is a valid ISO date or timestamp (allow blank for optional fields). */
    private boolean isValidDateOrTimestamp(String s) {
        if (s == null || s.isBlank()) return true;
        try { LocalDateTime.parse(s); return true; }
        catch (DateTimeParseException e1) {
            try { LocalDate.parse(s); return true; }
            catch (DateTimeParseException e2) { return false; }
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
            String expiryStr = parts[4].trim();
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
            // Validate expiry string (can be blank)
            if (!isValidDateOrTimestamp(expiryStr)) {
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

            // Parse expiry date if provided
            LocalDateTime expiryDate = null;
            if (!expiryStr.isBlank()) {
                try {
                    expiryDate = LocalDateTime.parse(expiryStr);
                } catch (DateTimeParseException e1) {
                    try {
                        expiryDate = LocalDate.parse(expiryStr).atStartOfDay();
                    } catch (DateTimeParseException e2) {
                        importErrors.add("Invalid expiryDate in wagons.csv line " + (i + 1));
                        continue;
                    }
                }
            }

            Box box = new Box(boxID, sku, quantity, expiryDate, receivedDate);
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
            ReturnReasons reason;
            try {
                reason = ReturnReasons.parse(parts[3]);
            } catch (IllegalArgumentException e) {
                importErrors.add("Invalid return reason in returns.csv line " + (i + 1) + ": " + parts[3]);
                continue;
            }
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

    public void insertBox(Box box){
        if(box == null) throw new IllegalArgumentException("Box cannot be null");

        Bay targetBay = findBay(box.getSku()); // 1. Tries to find a bay that already has this SKU
        String targetWarehouseID = null;

        if (targetBay == null) {
            targetBay = findAlternativeBay(box.getSku()); // 2. Tries to find an alternative bay
        }

        // 3. If bay doesn't have space, launches an exception
        if (targetBay == null){
            String errorMessage = String.format("Warehouse is full. Not possible to insert box %s", box.getBoxID());
            importErrors.add(errorMessage);
            return;
        }

        // 4. Find warehouseID of this bay
        for(Map.Entry<String, Set<Bay>> warehouseEntry : warehouses.entrySet()){
            if(warehouseEntry.getValue().contains(targetBay)){
                targetWarehouseID = warehouseEntry.getKey();
                break;
            }
        }

        // 5. Inserts in the selected bay using FEFO/FIFO
        boolean inserted = targetBay.addBoxInFEFOOrder(box);
        if (!inserted){
            String errorMessage = String.format("Warehouse is full. Not possible to insert box %s", box.getBoxID());
            importErrors.add(errorMessage);
            return;
        }

        // 6. Updates box localization
        box.setLocation(targetWarehouseID, targetBay.getAisle(), targetBay.getBayNumber());

        // 7. Registers the movement
        Movements movement = new Movements(
                MovementType.RETURN,
                box.getBoxID(),
                box.getSku(),
                box.getQuantity(),
                "returns",
                targetWarehouseID + ":" + targetBay.getAisle() + "/" + targetBay.getBayNumber()
        );
        movementHistory.add(movement);
    }

    /**
     * Finds the best bay for a SKU that already exists in the warehouse.
     * Searches for a bay that already has that SKU and still has capacity.
     * @param sku The product's SKU.
     * @return The best bay for the SKU, or null if no suitable bay was found.
     */
    public Bay findBay(String sku){
        // Iterate over warehouses
        for(Map.Entry<String, Set<Bay>> warehouseEntry : warehouses.entrySet()){
            Set<Bay> bays = warehouseEntry.getValue();

            for(Bay bay : bays){
                // Checks if bay has boxes with SKU
                boolean hasSKU = false;
                for(Box b : bay.getBoxes()){
                    if(b.getSku().equals(sku)){
                        hasSKU = true;
                        break;
                    }
                }
                // If a bay has boxes with SKU, return it
                if(hasSKU && bay.hasCapacity()){
                    return bay;
                }
            }
        }
        return null; // No suitable bay found
    }

    /**
     * Finds an alternative bay for a new SKU.
     * @param sku The product's SKU.
     * @return An empty bay or null if one isn't found.
     */
    public Bay findAlternativeBay(String sku){
        // Iterate over warehouses
        for(Map.Entry<String, Set<Bay>> warehouseEntry : warehouses.entrySet()){
            Set<Bay> bays = warehouseEntry.getValue();

            for(Bay bay : bays){
                // Checks if the bay is empty and has capacity
                if(bay.getBoxes().isEmpty() && bay.hasCapacity()){
                    return bay;
                }
            }
        }
        return null; // No suitable bay found
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
                            // Register movement of UNLOAD (from wagon to bay location)
                            Movements movement = new Movements(
                                    MovementType.UNLOAD,
                                    box.getBoxID(),
                                    box.getSku(),
                                    box.getQuantity(),
                                    "wagon:" + entry.getKey().getWagonID(),
                                    warehouseId + ":" + bay.getAisle() + "/" + bay.getBayNumber()
                            );
                            movementHistory.add(movement);
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
                String bayLocation = whId + ":" + bay.getAisle() + "/" + bay.getBayNumber();
                remaining = bay.dispatchFromFront(sku, remaining, movementHistory, bayLocation, null);

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

        // Insert into a new bay in FEFO order
        targetBay.addBoxInFEFOOrder(boxToRelocate);
        boxToRelocate.setLocation(newWarehouseId, newAisle, newBayNumber);

        // Register movement of RELOCATE
        String fromLoc = currentWarehouseId + ":" + currentBay.getAisle() + "/" + currentBay.getBayNumber();
        String toLoc = newWarehouseId + ":" + newAisle + "/" + newBayNumber;
        Movements movement = new Movements(
                MovementType.RELOCATE,
                boxToRelocate.getBoxID(),
                boxToRelocate.getSku(),
                boxToRelocate.getQuantity(),
                fromLoc,
                toLoc
        );
        movementHistory.add(movement);

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

    /**
     * Prints the complete movement history to console.
     */
    public void printMovementHistory(){
        System.out.println("\nWAREHOUSE MOVEMENT HISTORY");
        System.out.println("Total number of movements: " + movementHistory.size());
        System.out.println();

        for (Movements movement : movementHistory) {
            System.out.println(movement);
        }
    }

    /**
     * Exports movement history to CSV format.
     *
     * @return CSV-formatted string containing movement history.
     */
    public String exportToCSV(){
        StringBuilder csv = new StringBuilder();
        // The tests expect a specific header
        csv.append("MovementID,Source,Destination,BoxID,SKU,Timestamp").append("\n");

        int movementId = 1;
        for(Movements movement : movementHistory){
            String source = movement.getFromLocation();
            String destination = movement.getToLocation();
            String line = movementId + "," +
                    (source == null ? "" : source) + "," +
                    (destination == null ? "" : destination) + "," +
                    movement.getBoxID() + "," +
                    movement.getSku() + "," +
                    movement.getTimestamp();
            csv.append(line).append("\n");
            movementId++;
        }
        return csv.toString();
    }

    /**
     * Allocates boxes for an order identified by its id.
     * Iterates order lines in ascending order, trying to allocate required quantities, from available boxes.
     * User a temporary per-box reservation map to avoid double allocation within the same order.
     * For each line a line result is created and its status os computed.
     *
     * @param orderId the id of the order to allocate
     * @param strictMode if True, applies the strict mode when computing line status
     *
     * @return allocation results per line in the order
     */
    public OrderAllocationResult allocateOrder(String orderId, boolean strictMode) {
        OrderAllocationResult result = new OrderAllocationResult(orderId);

        Order order = orders.get(orderId);
        if (order == null) {
            return result;
        }

        Set<OrderLines> lines = order_lines.get(order);
        if (lines == null || lines.isEmpty()) {
            return result;
        }

        List<OrderLines> sortedLines = new ArrayList<>(lines);
        sortedLines.sort(Comparator.comparingInt(OrderLines::getLineNo));

        Map<String, Integer> boxReservations = new HashMap<>();

        for (OrderLines line : sortedLines) {
            OrderLineResult lineResult = new OrderLineResult(
                    line.getSku(),
                    line.getLineNo(),
                    line.getQty()
            );

            int remainingQty = line.getQty();

            List<BoxLocation> availableBoxes = collectBoxesForSku(line.getSku());

            for (BoxLocation boxLoc : availableBoxes) {
                if (remainingQty <= 0) {
                    break; // Line fully satisfied
                }

                Box box = boxLoc.box;
                Bay bay = boxLoc.bay;
                String warehouseId = boxLoc.warehouseId;

                int reservedQty = boxReservations.getOrDefault(box.getBoxID(), 0);
                int availableQty = box.getQuantity() - reservedQty;

                if (availableQty <= 0) {
                    continue;
                }

                int allocateQty = Math.min(remainingQty, availableQty);

                Allocation allocation = new Allocation(
                        orderId,
                        box.getBoxID(),
                        warehouseId,
                        line.getSku(),
                        line.getLineNo(),
                        bay.getAisle(),
                        bay.getBayNumber(),
                        allocateQty
                );

                lineResult.addAllocation(allocation);
                boxReservations.put(box.getBoxID(), reservedQty + allocateQty);
                remainingQty -= allocateQty;
            }

            lineResult.computeStatus(strictMode);
            result.addLineResult(lineResult);
        }
        return result;
    }

    /**
     * Used to represent the location of a box within a bay and warehouse.
     *
     * Holds references to the box, the bay where it is stored and the warehouse ID.
     * Compares different boxes to ensure FEFO/FIFO ordering when placed in sorted collections.
     */
    private static class BoxLocation implements Comparable<BoxLocation> {
        Box box;
        Bay bay;
        String warehouseId;

        BoxLocation(Box box, Bay bay, String warehouseId) {
            this.box = box;
            this.bay = bay;
            this.warehouseId = warehouseId;
        }

        @Override
        public int compareTo(BoxLocation other) {
            return this.box.compareTo(other.box);
        }
    }

    /**
     * Collects all boxes matching the given SKU across all warehouses and bays.
     *
     * Scans each warehouse and its bays, creating BoxLocation entries for matching boxes.
     * The resulting list is sorted in FEFO/FIFO order based on box properties.
     *
     * @param sku the SKU to search for
     *
     * @return a sorted list of BoxLocation instances with matching boxes
     */
    private List<BoxLocation> collectBoxesForSku(String sku) {
        List<BoxLocation> boxLocations = new ArrayList<>();

        for (Map.Entry<String, Set<Bay>> warehouseEntry : warehouses.entrySet()) {
            String warehouseId = warehouseEntry.getKey();
            Set<Bay> bays = warehouseEntry.getValue();

            for (Bay bay : bays) {
                for (Box box : bay.getBoxes()) {
                    if (box.getSku().equals(sku)) {
                        boxLocations.add(new BoxLocation(box, bay, warehouseId));
                    }
                }
            }
        }

        Collections.sort(boxLocations);

        return boxLocations;
    }
}