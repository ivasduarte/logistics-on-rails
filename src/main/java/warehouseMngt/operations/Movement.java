package warehouseMngt.operations;

import java.time.LocalDateTime;

public abstract class Movement {
    public String boxId;
    public String sku;
    protected int qty;
    public String fromLocation;
    public String toLocation;
    public LocalDateTime timestamp;
    protected String orderID;

    public Movement(String boxId, String sku, int qty, String fromLocation, String toLocation) {
        this.boxId = boxId;
        this.sku = sku;
        this.qty = qty;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.timestamp = LocalDateTime.now();
    }

    public abstract MovementType getType();

    public String getOrderId() {
        return orderID;
    }

    public void setOrderId(String orderId) {
        this.orderID = orderId;
    }

    @Override
    public String toString() {
        return String.format("[%s] Box %s (%s) %d units from %s → %s",
                getType(), boxId, sku, qty, fromLocation, toLocation);
    }
}