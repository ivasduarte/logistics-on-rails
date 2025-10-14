package esinf.us01;

import java.util.Objects;

public class Box implements Comparable<Box>{
    private String boxID;
    private String sku;
    private int quantity;
    private String expiryDate;
    private String receivedAt;

    public Box(String boxID, String sku, int quantity, String expiryDate, String receivedAt) {
        this.boxID = boxID;
        this.sku = sku;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.receivedAt = receivedAt;
    }

    public String getBoxID() {
        return boxID;
    }

    public void setBoxID(String boxID) {
        this.boxID = boxID;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public boolean hasExpirationDate() {
        return expiryDate != null && !expiryDate.isBlank();
    }

    /**
     * Checks whether this box is expired compared to the given current date.
     *
     * @param currentDate current date in ISO format "YYYY-MM-DD"
     * @return true if the box has an expiry date and it is before the current date
     */
    public boolean isExpired(String currentDate) {
        // If there is no expiry date, it cannot be expired
        if (this.expiryDate == null || this.expiryDate.isBlank()) return false;

        // compareTo() from String → returns < 0 if expiryDate < currentDate
        return this.expiryDate.compareTo(currentDate) < 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Box)) return false;
        Box box = (Box) o;
        return Objects.equals(boxID, box.boxID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boxID);
    }

    @Override
    public String toString() {
        return "Box{" +
                "id='" + boxID + '\'' +
                ", sku='" + sku + '\'' +
                ", quantity=" + quantity +
                ", expiryDate='" + expiryDate + '\'' +
                ", receivedAt='" + receivedAt + '\'' +
                '}';
    }

    /**
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
}
