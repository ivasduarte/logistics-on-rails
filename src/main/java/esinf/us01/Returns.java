package esinf.us01;

import java.util.Objects;

public class Returns {
    private String returnID;
    private String sku;
    private int qty;
    private String reason;
    private String timestamp;
    private String expiryDate;

    public Returns(String returnID, String sku, int qty, String reason, String timestamp, String expiryDate) {
        this.returnID = returnID;
        this.sku = sku;
        this.qty = qty;
        this.reason = reason;
        this.timestamp = timestamp;
        this.expiryDate = expiryDate;
    }

    public String getReturnID() {
        return returnID;
    }

    public void setReturnID(String returnID) {
        this.returnID = returnID;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Returns)) return false;
        Returns returns = (Returns) o;
        return Objects.equals(returnID, returns.returnID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnID);
    }

    @Override
    public String toString() {
        return "Returns{" +
                "returnID='" + returnID + '\'' +
                ", sku='" + sku + '\'' +
                ", qty=" + qty +
                ", reason='" + reason + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                '}';
    }
}
