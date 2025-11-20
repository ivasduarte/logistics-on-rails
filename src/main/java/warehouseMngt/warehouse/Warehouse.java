package warehouseMngt.warehouse;

import warehouseMngt.logistics.MovementHistory;
import warehouseMngt.logistics.Wagon;
import warehouseMngt.order.Order;
import warehouseMngt.order.OrderLines;
import warehouseMngt.returns.Returns;

import java.util.*;

/**
 * Represents the warehouse domain model, holding its core data structures.
 */
public class Warehouse {
    protected Map<Wagon, Set<Box>> wagons;
    protected Map<String, Item> items;
    protected Map<Order, Set<OrderLines>> order_lines;
    protected Map<String, Order> orders;
    protected Map<String, Returns> returns;
    protected Map<String, Set<Bay>> warehouses;

    protected MovementHistory movementHistory = new MovementHistory();
    protected List<String> importErrors = new ArrayList<>();

    public Warehouse() {
    }

    public Warehouse(Map<Wagon, Set<Box>> wagons,
                     Map<String, Item> items,
                     Map<Order, Set<OrderLines>> order_lines,
                     Map<String, Order> orders,
                     Map<String, Returns> returns,
                     Map<String, Set<Bay>> warehouses) {
        this.wagons = wagons;
        this.items = items;
        this.order_lines = order_lines;
        this.orders = orders;
        this.returns = returns;
        this.warehouses = warehouses;
    }

    // Getters
    public Map<Wagon, Set<Box>> getWagons() {
        return wagons;
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

    public Map<String, Set<Bay>> getWarehouses() {
        return warehouses;
    }

    public MovementHistory getMovementHistoryObject() {
        return movementHistory;
    }

    public List<String> getImportErrors() {
        return importErrors;
    }

    public void saveWarehouse(Warehouse warehouse) {
        if (warehouse != null && warehouse.getWarehouses() != null) {
            if (this.warehouses == null) {
                this.warehouses = new HashMap<>();
            }
            this.warehouses.putAll(warehouse.getWarehouses());
        }
    }
}