package domain.order;

import java.util.Objects;

public class Allocation {

    private String orderID;
    private String boxID;
    private String warehouseId;
    private String sku;

    private int lineNo;
    private int aisle;
    private int bayNumber;
    private int quantityAllocated;

    /**
     * Class constructor.
     * This method is used to create a new allocation record.
     *
     * @param orderID the order to be fulfilled
     * @param boxID the box from which the items are allocated
     * @param warehouseId the warehouse location
     * @param sku the SKU being allocated
     * @param lineNo the line number within the order
     * @param aisle the aisle number
     * @param bayNumber the bay number
     * @param quantityAllocated the quantity allocated from the box
     */
    public Allocation(String orderID, String boxID, String warehouseId, String sku,
                      int lineNo, int aisle, int bayNumber, int quantityAllocated) {

        this.orderID = orderID;
        this.boxID = boxID;
        this.warehouseId = warehouseId;
        this.sku = sku;
        this.lineNo = lineNo;
        this.aisle = aisle;
        this.bayNumber = bayNumber;
        this.quantityAllocated = quantityAllocated;
    }

    // Getters
    public String getOrderID() {
        return orderID;
    }
    public String getBoxID() {
        return boxID;
    }
    public String getWarehouseId() {
        return warehouseId;
    }
    public String getSku() {
        return sku;
    }
    public int getLineNo() {
        return lineNo;
    }
    public int getAisle() {
        return aisle;
    }
    public int getBayNumber() {
        return bayNumber;
    }
    public int getQuantityAllocated() {
        return quantityAllocated;
    }

    // Setters
    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }
    public void setBoxID(String boxID) {
        this.boxID = boxID;
    }
    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }
    public void setSku(String sku) {
        this.sku = sku;
    }
    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }
    public void setAisle(int aisle) {
        this.aisle = aisle;
    }
    public void setBayNumber(int bayNumber) {
        this.bayNumber = bayNumber;
    }
    public void setQuantityAllocated(int quantityAllocated) {
        this.quantityAllocated = quantityAllocated;
    }

    /**
     * Returns a formatted string of the allocation made.
     * Format: 'orderID'_'lineNo': 'boxID' at Warehouse: 'boxId', Aisle: 'aisle', Bay: 'bayNumber' -> Allocated: 'quantityAllocated' units of 'sku'
     */
    @Override
    public String toString() {
        return String.format("%s_%d: %s at Warehouse: %s, Aisle: %d, Bay: %d -> Allocated: %d units of %s",
                orderID, lineNo, boxID, warehouseId, aisle, bayNumber, quantityAllocated, sku);
    }

    /**
     * Verifies if two allocations are the same.
     * Allocations are equal if they represent the same order line and box.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Allocation that = (Allocation) o;
        return lineNo == that.lineNo && Objects.equals(orderID, that.orderID) && Objects.equals(boxID, that.boxID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderID, lineNo, boxID);
    }
}
