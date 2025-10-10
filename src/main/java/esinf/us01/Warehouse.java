package esinf.us01;

import java.util.*;

public class Warehouse {
    private Map<Wagon, Set<Box>> wagons = new HashMap<>();
    private Map<String, Set<Bay>> warehouses = new HashMap<>();
    private Map<String, Item> items = new HashMap<>();
    private Map<Order, Set<OrderLines>> order_lines = new HashMap<>();
    private Map<String, Order> orders = new HashMap<>();

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

    public void getWagons(List<String> lines){
        Wagon currentWagon = null;
        Set<Box> currentBox = null;

        for(int i = 1; i < lines.size(); i++){
            String line = lines.get(i);
            String[] parts = line.split(",");

            String wagonID = parts[0];
            String boxID = parts[1];
            String sku = parts[2];
            int quantity = Integer.parseInt(parts[3]);
            String expiryDate = parts[4].isEmpty() ? null : parts[4];
            String receivedDate = parts[5];

            if(currentWagon == null || !currentWagon.getWagonID().equals(wagonID)){
                if(currentWagon != null && currentBox !=null) wagons.put(currentWagon, currentBox);
                currentWagon = new Wagon(wagonID);
                currentBox = new HashSet<>();
            }

            Box box = new Box(boxID, sku, quantity, expiryDate, receivedDate);
            currentBox.add(box);
        }

        if(currentWagon != null && currentBox !=null) wagons.put(currentWagon, currentBox);
    }

    public void getBays(List<String> lines){
        String currentWarehouseID = null;
        Set<Bay> currentBays = null;

        for(int i = 1; i < lines.size(); i++){
            String line = lines.get(i);
            String[] parts = line.split(",");

            String warehouseID = parts[0];
            int aisle = Integer.parseInt(parts[1]);
            int bay = Integer.parseInt(parts[2]);
            int boxCapacity = Integer.parseInt(parts[3]);

            // If we change warehouse its interior gets saved
            if(currentWarehouseID != null || !currentWarehouseID.equals(warehouseID)){
                if(currentWarehouseID != null && currentBays !=null) warehouses.put(currentWarehouseID, currentBays);
                currentWarehouseID = warehouseID;
                currentBays = new HashSet<>();
            }

            Bay bays = new Bay(bay, aisle, boxCapacity);
            currentBays.add(bays);
        }
        // Save the last warehouse
        if(currentWarehouseID != null && currentBays !=null) warehouses.put(currentWarehouseID, currentBays);
    }

    public void getItems(List<String> lines){
        String currentSKU = null;
        Set<Item> currentItem = null;

        for(int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split(",");

            String sku = parts[0];
            String name = parts[1];
            String category = parts[2];
            String unit = parts[3];
            double volume = Double.parseDouble(parts[4]);
            double unitWeight = Double.parseDouble(parts[5]);

            Item item = new Item(sku, name, category, unit, volume, unitWeight);
            items.put(sku, item);
        }
    }

    public void getOrderLines(List<String> lines) {
        Order currentOrder = null;
        Set<OrderLines> currentLines = null;

        for (int i = 1; i < lines.size(); i++) { // Ignora cabeçalho
            String linha = lines.get(i);
            String[] partes = linha.split(",");

            String orderId = partes[0];
            int lineNo = Integer.parseInt(partes[1]);
            String sku = partes[2];
            int qty = Integer.parseInt(partes[3]);

            // Checks if it's a new order
            if (currentOrder == null || !currentOrder.getOrderID().equals(orderId)) {
                // Saves previous before changing
                if (currentOrder != null && currentLines != null) {
                    order_lines.put(currentOrder, currentLines);
                }

                // If orders with dueDate/priority have already been loaded, reuse the same object
                if (orders.containsKey(orderId)) {
                    currentOrder = orders.get(orderId);
                } else {
                    currentOrder = new Order(orderId); // fallback se não existir
                    orders.put(orderId, currentOrder);
                }

                currentLines = new HashSet<>();
            }

            OrderLines orderLine = new OrderLines(lineNo, sku, qty);
            currentLines.add(orderLine);
        }

        // Saves the last order
        if (currentOrder != null && currentLines != null) {
            order_lines.put(currentOrder, currentLines);
        }
    }


    public void getOrders(List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split(",");

            String orderId = parts[0];
            String dueDate = parts[1];
            int priority = Integer.parseInt(parts[2]);

            Order order = new Order(orderId, priority, dueDate);
            orders.put(orderId, order);
        }
    }
}
