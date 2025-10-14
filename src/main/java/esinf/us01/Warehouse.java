package esinf.us01;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Warehouse {
    private Map<Wagon, Set<Box>> wagons = new HashMap<>();
    private Map<String, Set<Bay>> warehouses = new HashMap<>();
    private Map<String, Item> items = new HashMap<>();
    private Map<Order, Set<OrderLines>> order_lines = new HashMap<>();
    private Map<String, Order> orders = new HashMap<>();
    private Map<String, Returns> returns = new HashMap<>();

    private List<String> importErrors = new ArrayList<>();

    private static String CSV_BASE_PATH = "csv-docs/";
    public static String WAGONS_FILE = CSV_BASE_PATH + "wagons.csv";
    public static String BAYS_FILE = CSV_BASE_PATH + "bays.csv";
    public static String ITEMS_FILE = CSV_BASE_PATH + "items.csv";
    public static String ORDER_LINES_FILE = CSV_BASE_PATH + "order_lines.csv";
    public static String ORDERS_FILE = CSV_BASE_PATH + "orders.csv";
    public static String RETURNS_FILE = CSV_BASE_PATH + "returns.csv";

    public Map<Wagon, Set<Box>> getWagonsMap() { return wagons; }
    public Map<String, Set<Bay>> getWarehouses() { return warehouses; }
    public Map<String, Item> getItems() { return items; }
    public Map<Order, Set<OrderLines>> getOrderLines() { return order_lines; }
    public Map<String, Order> getOrders() { return orders; }
    public Map<String, Returns> getReturns() { return returns; }
    public List<String> getImportErrors() { return importErrors; }

    public void loadAllData() throws IOException {
        importErrors.clear();

        getItems(Files.readAllLines(Paths.get(ITEMS_FILE)));
        getWagons(Files.readAllLines(Paths.get(WAGONS_FILE)));
        getBays(Files.readAllLines(Paths.get(BAYS_FILE)));
        getOrders(Files.readAllLines(Paths.get(ORDERS_FILE)));
        getOrderLines(Files.readAllLines(Paths.get(ORDER_LINES_FILE)));
        getReturns(Files.readAllLines(Paths.get(RETURNS_FILE)));

        // Report errors if any
        if (!importErrors.isEmpty()) {
            System.err.println("=== IMPORT ERRORS ===");
            importErrors.forEach(System.err::println);
        }
    }

    private boolean isValidInteger(String s) {
        try { Integer.parseInt(s); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private boolean isValidDouble(String s) {
        try { Double.parseDouble(s); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private boolean isValidDateOrTimestamp(String s) {
        if (s == null || s.isBlank()) return true; // allow empty expiry dates
        try { LocalDateTime.parse(s); return true; }
        catch (DateTimeParseException e1) {
            try{
                LocalDate.parse(s);
                return true;
            }catch (DateTimeParseException e2){ return false; }
        }
    }

    public void getWagons(List<String> lines){
        Wagon currentWagon = null;
        Set<Box> currentBoxes = null;

        for(int i = 1; i < lines.size(); i++){
            String[] parts = lines.get(i).split(",", -1);
            if (parts.length < 6) {
                importErrors.add("Invalid line in wagons.csv at " + (i+1) + ": wrong column count");
                System.out.println(importErrors);
                continue;
            }

            String wagonID = parts[0];
            String boxID = parts[1];
            String sku = parts[2];
            String qtyStr = parts[3];
            String expiryDate = parts[4];
            String receivedDate = parts[5];

            if (wagonID.isBlank() || boxID.isBlank() || sku.isBlank()) {
                importErrors.add("Missing IDs in wagons.csv line " + (i+1));
                System.out.println(importErrors);
                continue;
            }
            if (!isValidInteger(qtyStr)) {
                importErrors.add("Invalid quantity for box " + boxID + " in wagons.csv line " + (i+1));
                System.out.println(importErrors);
                continue;
            }
            int quantity = Integer.parseInt(qtyStr);
            if (quantity <= 0) {
                importErrors.add("Negative or zero quantity in wagons.csv line " + (i+1));
                System.out.println(importErrors);
                continue;
            }
            if (!isValidDateOrTimestamp(receivedDate)) {
                importErrors.add("Invalid receivedAt timestamp in wagons.csv line " + (i+1));
                System.out.println(importErrors);
                continue;
            }
            if (!isValidDateOrTimestamp(expiryDate)) {
                importErrors.add("Invalid expiryDate in wagons.csv line " + (i+1));
                System.out.println(importErrors);
                continue;
            }
            if (!items.containsKey(sku)) {
                importErrors.add("Unknown SKU " + sku + " in wagons.csv line " + (i+1));
                System.out.println(importErrors);
                continue;
            }

            if(currentWagon == null || !currentWagon.getWagonID().equals(wagonID)){
                if(currentWagon != null && currentBoxes !=null) wagons.put(currentWagon, currentBoxes);
                currentWagon = new Wagon(wagonID);
                currentBoxes = new HashSet<>();
            }

            Box box = new Box(boxID, sku, quantity,
                    expiryDate.isBlank() ? null : expiryDate,
                    receivedDate);
            currentBoxes.add(box);
        }
        if(currentWagon != null && currentBoxes !=null) wagons.put(currentWagon, currentBoxes);
    }

    public void getBays(List<String> lines){
        String currentWarehouseID = null;
        Set<Bay> currentBays = null;

        for(int i = 1; i < lines.size(); i++){
            String[] parts = lines.get(i).split(";", -1);
            if (parts.length < 4) {
                importErrors.add("Invalid line in bays.csv at " + (i+1));
                continue;
            }

            String warehouseID = parts[0];
            String aisleStr = parts[1];
            String bayStr = parts[2];
            String capacityStr = parts[3];

            if (!isValidInteger(aisleStr) || !isValidInteger(bayStr) || !isValidInteger(capacityStr)) {
                importErrors.add("Invalid numeric values in bays.csv line " + (i+1));
                continue;
            }

            int aisle = Integer.parseInt(aisleStr);
            int bay = Integer.parseInt(bayStr);
            int boxCapacity = Integer.parseInt(capacityStr);

            if(currentWarehouseID == null || !currentWarehouseID.equals(warehouseID)){
                if(currentWarehouseID != null && currentBays !=null) warehouses.put(currentWarehouseID, currentBays);
                currentWarehouseID = warehouseID;
                currentBays = new HashSet<>();
            }

            Bay bays = new Bay(bay, aisle, boxCapacity);
            currentBays.add(bays);
        }

        if(currentWarehouseID != null && currentBays !=null) warehouses.put(currentWarehouseID, currentBays);
    }

    public void getItems(List<String> lines){
        for(int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);
            if (parts.length < 6) {
                importErrors.add("Invalid line in items.csv at " + (i+1));
                continue;
            }

            String sku = parts[0];
            String name = parts[1];
            String category = parts[2];
            String unit = parts[3];
            String volumeStr = parts[4];
            String weightStr = parts[5];

            if (sku.isBlank() || name.isBlank()) {
                importErrors.add("Missing SKU/name in items.csv line " + (i+1));
                continue;
            }
            if (!isValidDouble(volumeStr) || !isValidDouble(weightStr)) {
                importErrors.add("Invalid numeric fields in items.csv line " + (i+1));
                continue;
            }

            Item item = new Item(sku, name, category, unit,
                    Double.parseDouble(volumeStr),
                    Double.parseDouble(weightStr));
            items.put(sku, item);
        }
    }

    public void getOrders(List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);
            if (parts.length < 3) {
                importErrors.add("Invalid line in orders.csv at " + (i + 1));
                continue;
            }

            String orderId = parts[0];
            String dueDateStr = parts[1];
            String priorityStr = parts[2];

            if (!isValidInteger(priorityStr)) {
                importErrors.add("Invalid priority in orders.csv line " + (i + 1));
                continue;
            }

            LocalDate dueDate = null;
            if (dueDateStr != null && !dueDateStr.isBlank()) {
                try {
                    dueDate = LocalDate.parse(dueDateStr); // yyy-MM-dd
                } catch (DateTimeParseException e1) {
                    try {
                        dueDate = LocalDateTime.parse(dueDateStr).toLocalDate(); // yyyy-MM-ddTHH:mm:ss
                    } catch (DateTimeParseException e2) {
                        importErrors.add("Invalid dueDate in orders.csv line " + (i + 1));
                        continue; // Order isn't created with an invalid dueDate
                    }
                }
            }

            Order order = new Order(orderId, Integer.parseInt(priorityStr),
                    dueDate != null ? dueDate.toString() : null);
            orders.put(orderId, order);
        }
    }


    public void getOrderLines(List<String> lines) {
        Order currentOrder = null;
        Set<OrderLines> currentLines = null;

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);
            if (parts.length < 4) {
                importErrors.add("Invalid line in order_lines.csv at " + (i+1));
                continue;
            }

            String orderId = parts[0];
            String lineNoStr = parts[1];
            String sku = parts[2];
            String qtyStr = parts[3];

            if (!isValidInteger(lineNoStr) || !isValidInteger(qtyStr)) {
                importErrors.add("Invalid numbers in order_lines.csv line " + (i+1));
                continue;
            }
            if (!items.containsKey(sku)) {
                importErrors.add("Unknown SKU " + sku + " in order_lines.csv line " + (i+1));
                continue;
            }

            int lineNo = Integer.parseInt(lineNoStr);
            int qty = Integer.parseInt(qtyStr);

            if (currentOrder == null || !currentOrder.getOrderID().equals(orderId)) {
                if (currentOrder != null) order_lines.put(currentOrder, currentLines);
                currentOrder = orders.getOrDefault(orderId, new Order(orderId));
                orders.put(orderId, currentOrder);
                currentLines = new HashSet<>();
            }

            OrderLines ol = new OrderLines(lineNo, sku, qty);
            currentLines.add(ol);
        }

        if (currentOrder != null)
            order_lines.put(currentOrder, currentLines);
    }

    public void getReturns(List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",", -1);
            if (parts.length < 6) {
                importErrors.add("Invalid line in returns.csv at " + (i+1));
                continue;
            }

            String returnID = parts[0];
            String sku = parts[1];
            String qtyStr = parts[2];
            String reason = parts[3];
            String timestamp = parts[4];
            String expiryDate = parts[5];

            if (!isValidInteger(qtyStr)) {
                importErrors.add("Invalid qty in returns.csv line " + (i+1));
                continue;
            }
            if (!isValidDateOrTimestamp(timestamp)) {
                importErrors.add("Invalid timestamp in returns.csv line " + (i+1));
                continue;
            }
            if (!isValidDateOrTimestamp(expiryDate)) {
                importErrors.add("Invalid expiryDate in returns.csv line " + (i+1));
                continue;
            }
            if (!items.containsKey(sku)) {
                importErrors.add("Unknown SKU " + sku + " in returns.csv line " + (i+1));
                continue;
            }

            Returns r = new Returns(returnID, sku,
                    Integer.parseInt(qtyStr), reason,
                    timestamp, expiryDate);
            returns.put(returnID, r);
        }
    }
}