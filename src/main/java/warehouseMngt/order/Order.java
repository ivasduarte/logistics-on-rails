package warehouseMngt.order;

import java.util.Objects;

/**
 * Represents an order with an associated ID, priority level, and due date.
 * Provides methods to access and modify the order details. The equality of
 * orders is determined based on their unique order ID.
 */
public class Order {
    private String orderID;
    private String dueDate;
    private int priority;

    public Order(String orderID, int priority, String dueDate) {
        this.orderID = orderID;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public Order(String orderID) {
        this.orderID = orderID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return Objects.equals(orderID, order.orderID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderID);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderID='" + orderID + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", priority=" + priority +
                '}';
    }
}
