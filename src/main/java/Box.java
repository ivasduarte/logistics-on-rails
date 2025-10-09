import java.time.LocalDateTime;

public class Box implements Comparable<Box> {
    private String boxID;
    private String sku;
    private int quantity;
    private LocalDateTime expiryDate;
    private LocalDateTime receivedAt;
    private String warehouseID;
    private int aisle;
    private int bay;

    /*
     * Constructor.
     * @param boxID Box identifier.
     * @param bay Bay number.
     * @param aisle Aisle number.
     * @param receivedAt Date and time when the box was received.
     * @param warehouseID Warehouse identifier.
     * @param expiryDate Date and time when the box expires.
     * @param quantity Box quantity.
     * @param sku Stock Keeping Unit of the box.
     */
    public Box(String boxID, int bay, int aisle, LocalDateTime receivedAt, String warehouseID, LocalDateTime expiryDate, int quantity, String sku) {
        this.boxID = boxID;
        this.bay = bay;
        this.aisle = aisle;
        this.receivedAt = receivedAt;
        this.warehouseID = warehouseID;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.sku = sku;
    }

    public String getBoxID() { return boxID; }

    public void setBoxID(String boxID) { this.boxID = boxID; }

    public int getBay() { return bay; }

    public void setBay(int bay) { this.bay = bay; }

    public int getAisle() { return aisle; }

    public void setAisle(int aisle) { this.aisle = aisle; }

    public String getWarehouseID() { return warehouseID; }

    public void setWarehouseID(String warehouseID) { this.warehouseID = warehouseID; }

    public LocalDateTime getReceivedAt() { return receivedAt; }

    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public LocalDateTime getExpiryDate() { return expiryDate; }

    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public String getSku() { return sku; }

    public void setSku(String sku) { this.sku = sku; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    /*
    * This method defines the order of the boxes, according to the FEFO/FIFO rule:
    * 1. We compare the expiry dates of the boxes (<expiryDate>) - the closest expire first.
    *    - If both dates are null, the expiry date isn't considered.
    *    - If one date is null, that box is considered "bigger" (comes after).
    * 2. If the expiry dates are equal or null, we compare the received dates (<receivedAt>)
    * being the oldest (first received) considered "minor".
    * 3. If the received dates are equal, we compare the box IDs (<boxID>), in ascending order.
     */
    @Override
    public int compareTo(Box o) {
        // 1. Both dates are null
        if(this.expiryDate == null && o.expiryDate == null){
            // Both dates are null, compare receive date
            int comp = this.receivedAt.compareTo(o.receivedAt);
            // If the dates are different, return the comparison result
            if(comp != 0) return comp;
            // If the dates are equal, return box IDs in ascending order
            else return this.boxID.compareTo(o.boxID);
        }

        // 2. One date is null
        // Boxes without an expiry date go to the end of the list
        if(this.expiryDate == null) return 1; // ("this" is bigger than "o")
        else if(o.expiryDate == null) return -1; // ("this" is smaller than "o")

        // 3. Both have expiry dates
        int comp = this.expiryDate.compareTo(o.expiryDate);
        // If the dates are different, return the comparison result
        if(comp != 0) return comp;

        // 4. Expiry dates are equal, compare received dates
        comp = this.receivedAt.compareTo(o.receivedAt);
        // If the dates are different, return the comparison result
        if(comp != 0) return comp;

        // 5. Received dates are equal, compare box IDs in ascending order
        return this.boxID.compareTo(o.boxID);
    }

    @Override
    public String toString() {
        return "Box{" +
                "boxID='" + boxID + '\'' +
                ", sku='" + sku + '\'' +
                ", quantity=" + quantity +
                ", expiryDate=" + expiryDate +
                ", receivedAt=" + receivedAt +
                ", warehouseID='" + warehouseID + '\'' +
                ", aisle=" + aisle +
                ", bay=" + bay +
                '}';
    }
}
