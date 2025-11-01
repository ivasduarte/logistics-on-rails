package domain.service;

import domain.logistics.Wagon;
import domain.order.Order;
import domain.order.OrderLines;
import domain.returns.ReturnReasons;
import domain.returns.Returns;
import domain.warehouse.Bay;
import domain.warehouse.Box;
import domain.warehouse.Item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;

/**
 * This class loads data from all CSV files.
 */
public class LoadData {
    private static final String BAYS_FILE = "csv-docs/bays.csv";
    private static final String ITEMS_FILE = "csv-docs/items.csv";
    private static final String ORDER_LINES_FILE = "csv-docs/order_lines.csv";
    private static final String ORDERS_FILE = "csv-docs/orders.csv";
    private static final String RETURNS_FILE = "csv-docs/returns.csv";
    private static final String WAGONS_FILE = "csv-docs/wagons.csv";

    private static Map<String, Set<Bay>> warehouses = new HashMap<>();
    public static List<String> importErrors = new ArrayList<>();
    private static Set<String> allBoxIDS = new HashSet<>();

    private static Map<Wagon, Set<Box>> wagons = new HashMap<>();
    private static Map<String, Item> items = new HashMap<>();
    private static Map<Order, Set<OrderLines>> order_lines = new HashMap<>();
    private static Map<String, Order> orders = new HashMap<>();
    private static Map<String, Returns> returns = new HashMap<>();

    /**
     * Getter Methods
     */
    public static Map<Wagon, Set<Box>> getWagons() { return wagons; }
    public static Map<String, Item> getItems() { return items; }
    public static Map<Order, Set<OrderLines>> getOrderLines() { return order_lines; }
    public static Map<String, Order> getOrders() { return orders; }
    public static Map<String, Returns> getReturns() { return returns; }
    public static Map<String, Set<Bay>> getWarehouses() { return warehouses; }
    public static List<String> getImportErrors() { return importErrors; }
    public static Set<String> getAllBoxIDS() { return allBoxIDS; }

    /**
     * This method loads all data from CSV files.
     */
    public static void loadAllData() throws IOException {
        importErrors.clear();
        allBoxIDS.clear();

        getBays(Files.readAllLines(Paths.get(BAYS_FILE)));
        getItems(Files.readAllLines(Paths.get(ITEMS_FILE)));
        getOrderLines(Files.readAllLines(Paths.get(ORDER_LINES_FILE)));
        getOrders(Files.readAllLines(Paths.get(ORDERS_FILE)));
        getReturns(Files.readAllLines(Paths.get(RETURNS_FILE)));
        getWagons(Files.readAllLines(Paths.get(WAGONS_FILE)));
    }

    /**
     * This generic method checks if a string can be parsed into a given type.
     */
    private static <T> boolean isValid(String str, Function<String, T> parser) {
        try {
            parser.apply(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This method validates that a CSV line has the minimum required number of columns.
     * If validation fails, an error message is added to the import errors list.
     *
     * @param parts           the array of column values obtained from splitting the CSV line
     * @param expectedColumns the minimum number of columns required
     * @param fileName        the name of the CSV file being validated
     * @param lineNumber      the current line number being processed (1-based index)
     * @return true if validation passes, false otherwise
     */
    private static boolean validateColumnCount(String[] parts, int expectedColumns, String fileName, int lineNumber) {
        if (parts.length < expectedColumns) {
            importErrors.add("Invalid line in " + fileName + " at " + lineNumber + ": wrong column count");
            return false;
        }
        return true;
    }

    /**
     * This method reads the bays.csv file and loads bay data.
     */
    public static void getBays(List<String> lines) {
        String currentWarehouseID = null;
        Set<Bay> currentBays = null;

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(";", -1);

            if (!validateColumnCount(parts, 4, "bays.csv", i + 1)) {
                continue;
            }

            String warehouseID = parts[0].trim();
            String aisleStr = parts[1].trim();
            String bayStr = parts[2].trim();
            String capacityStr = parts[3].trim();

            if (!isValid(aisleStr, Integer::parseInt) || !isValid(bayStr, Integer::parseInt) || !isValid(capacityStr, Integer::parseInt)) {
                importErrors.add("Invalid numeric fields in bays.csv line " + (i + 1));
                continue;
            }

            int aisle = Integer.parseInt(aisleStr);
            int bay = Integer.parseInt(bayStr);
            int boxCapacity = Integer.parseInt(capacityStr);

            if (currentWarehouseID == null || !currentWarehouseID.equals(warehouseID)) {
                saveGroupToMap(warehouses, currentWarehouseID, currentBays);
                currentWarehouseID = warehouseID;
                currentBays = new HashSet<>();
            }

            Bay b = new Bay(bay, aisle, boxCapacity);
            currentBays.add(b);
        }
        saveGroupToMap(warehouses, currentWarehouseID, currentBays);
    }

    /**
     * This method reads the items.csv file and loads item data.
     */
    public static void getItems(List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);

            if (!validateColumnCount(parts, 6, "items.csv", i + 1)) {
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
            if (!isValid(volumeStr, Double::parseDouble) || !isValid(weightStr, Double::parseDouble)) {
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
     * Loads order lines from CSV lines with validation.
     * Each line must have a valid numeric lineNo and quantity, and reference a known SKU and order.
     */
    public static void getOrderLines(List<String> lines) {
        Order currentOrder = null;
        Set<OrderLines> currentLines = null;

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);

            if (!validateColumnCount(parts, 4, "order_lines.csv", i + 1)) {
                continue;
            }

            String orderId = parts[0].trim();
            String lineNoStr = parts[1].trim();
            String sku = parts[2].trim();
            String qtyStr = parts[3].trim();

            if (!isValid(lineNoStr, Integer::parseInt) || !isValid(qtyStr, Integer::parseInt)) {
                importErrors.add("Invalid numeric values in order_lines.csv line " + (i + 1));
                continue;
            }

            if (!items.containsKey(sku)) {
                importErrors.add("Unknown SKU " + sku + " in order_lines.csv line " + (i + 1));
                continue;
            }

            int lineNo = Integer.parseInt(lineNoStr);
            int qty = Integer.parseInt(qtyStr);

            // Check if a new group needs to be created
            if (currentOrder == null || !currentOrder.getOrderID().equals(orderId)) {
                saveGroupToMap(order_lines, currentOrder, currentLines);
                currentOrder = orders.getOrDefault(orderId, new Order(orderId));
                orders.put(orderId, currentOrder);
                currentLines = new HashSet<>();
            }

            OrderLines order_line = new OrderLines(lineNo, sku, qty);
            currentLines.add(order_line);
        }
        saveGroupToMap(order_lines, currentOrder, currentLines);
    }

    /**
     * This method saves the current group of entities to the map before starting a new group.
     * It is used when processing CSV files where multiple lines belong to the same parent entity.
     *
     * @param <K>           the type of the parent entity (e.g., Order, Wagon)
     * @param <V>           the type of the child entity collection (e.g., Set of OrderLines, Set of Boxes)
     * @param map           the map where the parent-child relationship is stored
     * @param parentEntity  the parent entity to be saved
     * @param childEntities the collection of child entities associated with the parent
     */
    private static <K, V> void saveGroupToMap(Map<K, V> map, K parentEntity, V childEntities) {
        if (parentEntity != null && childEntities != null) {
            map.put(parentEntity, childEntities);
        }
    }


    /**
     * This method loads orders from CSV lines with validation.
     * Each order must have a valid ID and numeric priority.
     * The dueDate is optional but must be valid if provided.
     */
    public static void getOrders(List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);

            if (!validateColumnCount(parts, 3, "orders.csv", i + 1)) {
                continue;
            }

            String orderId = parts[0].trim();
            String dueDateStr = parts[1].trim();
            String priorityStr = parts[2].trim();

            if (orderId.isBlank()) {
                importErrors.add("Missing orderID in orders.csv line " + (i + 1));
                continue;
            }

            if (!isValid(priorityStr, Integer::parseInt)) {
                importErrors.add("Invalid priority in orders.csv line " + (i + 1));
                continue;
            }

            LocalDate dueDate = null;
            if (!dueDateStr.isBlank()) {
                try {
                    dueDate = parseFlexibleDateTime(dueDateStr).toLocalDate();
                } catch (DateTimeParseException e1) {
                    importErrors.add("Invalid dueDate in orders.csv line " + (i + 1));
                    continue;

                }
            }

            Order order = new Order(orderId, Integer.parseInt(priorityStr),
                    dueDate != null ? dueDate.toString() : null);
            orders.put(orderId, order);
        }
    }


    /**
     * This method loads returns from CSV lines with validation.
     * Each return must have valid SKU, quantity, and timestamps/dates if provided.
     */
    public static void getReturns(List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);

            if (!validateColumnCount(parts, 6, "returns.csv", i + 1)) {
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

            if (!isValid(qtyStr, Integer::parseInt)) {
                importErrors.add("Invalid quantity in returns.csv line " + (i + 1));
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
     * This method loads wagons and boxes from CSV lines.
     * Validates quantities, dates, SKU references, and ensures box uniqueness.
     */
    public static void getWagons(List<String> lines) {
        Wagon currentWagon = null;
        Set<Box> currentBoxes = null;

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);

            if (!validateColumnCount(parts, 6, "wagons.csv", i + 1)) {
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
            if (allBoxIDS.contains(boxID)) {
                importErrors.add("Duplicate boxID " + boxID + " in wagons.csv line " + (i + 1));
                continue;
            }

            if (!isValid(qtyStr, Integer::parseInt)) {
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
            if (!isValid(receivedDate, LoadData::parseFlexibleDateTime)) {
                importErrors.add("Invalid receivedAt timestamp in wagons.csv line " + (i + 1));
                continue;
            }

            if (!items.containsKey(sku)) {
                importErrors.add("Unknown SKU " + sku + " in wagons.csv line " + (i + 1));
                continue;
            }

            if (currentWagon == null || !currentWagon.getWagonID().equals(wagonID)) {
                saveGroupToMap(wagons, currentWagon, currentBoxes);
                currentWagon = new Wagon(wagonID);
                currentBoxes = new HashSet<>();
            }

            // Parse expiry date if provided
            LocalDateTime expiryDate = null;
            if (!expiryStr.isBlank()) {
                try {
                    expiryDate = parseFlexibleDateTime(expiryStr);
                } catch (DateTimeParseException e) {
                    importErrors.add("Invalid expiryDate in wagons.csv line " + (i + 1));
                    continue;
                }
            }

            Box box = new Box(boxID, sku, quantity, expiryDate, receivedDate);
            currentBoxes.add(box);
            allBoxIDS.add(boxID);
        }
        saveGroupToMap(wagons, currentWagon, currentBoxes);
    }

    /**
     * This method parses a date or datetime string, accepting both LocalDateTime and LocalDate formats.
     * If a LocalDate is provided, it is converted to LocalDateTime at midnight (start of the day).
     *
     * @param dateTimeStr the string to parse in ISO format (e.g., "2024-10-31" or "2024-10-31T14:30:00")
     * @return the parsed LocalDateTime object
     * @throws DateTimeParseException if the string cannot be parsed in either format
     */
    private static LocalDateTime parseFlexibleDateTime(String dateTimeStr) throws DateTimeParseException {
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(dateTimeStr).atStartOfDay();
        }
    }
}
