package esinf.us01;

public class Movements {
    private String timestamp;
    private MovementType type;
    private String boxID;
    private String sku;
    private int qty;
    private String fromLocation;
    private String toLocation;
    private String orderID;
    private String notes;

    public enum MovementType{
        UNLOAD("Unload from wagon"),
        DISPATCH("Dispatch to order"),
        RELOCATE("Relocate between bays"),
        RETURN("Return to warehouse");

        private final String description;

        MovementType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public Movements(MovementType type, String boxID, String sku, int qty, String fromLocation, String toLocation) {
        this.type = type;
        this.boxID = boxID;
        this.sku = sku;
        this.qty = qty;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Movements{" +
                "timestamp='" + timestamp + '\'' +
                ", type=" + type +
                ", boxID='" + boxID + '\'' +
                ", sku='" + sku + '\'' +
                ", qty=" + qty +
                ", fromLocation='" + fromLocation + '\'' +
                ", toLocation='" + toLocation + '\'' +
                ", orderID='" + orderID + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }

    public String toCSV() {
        return timestamp + "," + type.getDescription() + "," + boxID + "," + sku + "," + qty + "," + fromLocation + "," + toLocation + "," + (orderID == null ? "" : orderID) + "," + (notes == null ? "" : notes);
    }

    public static String getCSVHeader() {
        return "timestamp,type,boxID,sku,qty,fromLocation,toLocation,orderID,notes";
    }
}
