package esinf.us02;

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

    @Override
    public String toString() {
        return String.format("%s.%d -> %s @ %s[%d/%d]: %d units of %s",
                orderID, lineNo, boxID, warehouseId, aisle, bayNumber, quantityAllocated, sku); // Alterar posteriormente
    }

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
