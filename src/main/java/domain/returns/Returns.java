package domain.returns;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a returned product that enters the quarantine system.
 * Each return contains product information, reason for return, and timestamps
 * to enable proper tracking and processing according to warehouse quality control rules.
 *
 * Returns are processed in LIFO order (Latest In, First Out) based on timestamp,
 * with ties broken by returnID in the ascending order.
 */
public class Returns {
    // Unique identifier for this return transaction
    private String returnID;

    // Stock Keeping Unit - identifies the product type
    private String sku;

    // Quantity of items being returned
    private int qty;

    // Business reason for the return (determines if item should be discarded or restocked)
    private ReturnReasons reason;

    // ISO timestamp when the return was received (used for LIFO ordering)
    private String timestamp;

    // Product expiry date (if applicable) - used for FEFO rules when restocking
    private String expiryDate;

    // Flag to mark if this return has been discarded (not restocked)
    private boolean discarded = false;

    /**
     * Creates a new Returns object representing a product return.
     *
     * @param returnID Unique identifier for this return
     * @param sku Product identifier
     * @param qty Quantity being returned
     * @param reason Business reason for return (REMORSE, DAMAGED, EXPIRED, CYCLE_COUNT)
     * @param timestamp When the return was received (ISO format)
     * @param expiryDate Product expiry date (can be null for non-perishables)
     */
    public Returns(String returnID, String sku, int qty, ReturnReasons reason,
                   String timestamp, String expiryDate) {
        this.returnID = returnID;
        this.sku = sku;
        this.qty = qty;
        this.reason = reason;
        this.timestamp = timestamp;
        this.expiryDate = expiryDate;
    }

    // Standard getters and setters omitted for brevity
    public String getReturnID() { return returnID; }
    public void setReturnID(String returnID) { this.returnID = returnID; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public ReturnReasons getReason() { return reason; }
    public void setReason(ReturnReasons reason) { this.reason = reason; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public void setDiscarded(boolean discarded) { this.discarded = discarded; }

    /**
     * Checks if the product has expired based on its expiry date.
     * Used to validate EXPIRED returns during inspection.
     *
     * @return true if the product is past its expiry date, false otherwise
     */
    public boolean isExpired() {
        LocalDate expiryDate = LocalDate.parse(getExpiryDate());
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Two Returns are equal if they have the same returnID.
     * This allows using Returns in Sets and as Map keys.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Returns)) return false;
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