package domain.warehouse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Box represents a container of products with a specific SKU.
 * Implements Comparable for FEFO/FIFO ordering:
 * 1. By expiry date (earliest first, null last) - FEFO for perishables
 * 2. By received date (oldest first) - FIFO for non-perishables
 * 3. By boxID (ascending) - tie-breaker
 */
public class Box {
    private String boxID;
    private String sku;
    private int quantity;
    private LocalDateTime expiryDate; // ISO date string or null
    private String receivedAt; // ISO timestamp string

    // Location information
    private String warehouseId;
    private Integer aisle;
    private Integer bayNumber;

    public Box(String boxID, String sku, int quantity, LocalDateTime expiryDate, String receivedAt) {
        this.boxID = boxID;
        this.sku = sku;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.receivedAt = receivedAt;
    }

    // Getters
    public String getBoxID() {
        return boxID;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public boolean hasExpirationDate() {
        return expiryDate != null;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public Integer getAisle() {
        return aisle;
    }

    public Integer getBayNumber() {
        return bayNumber;
    }

    /**
     * Sets the location of this box in the warehouse.
     */
    public void setLocation(String warehouseId, int aisle, int bayNumber) {
        this.warehouseId = warehouseId;
        this.aisle = aisle;
        this.bayNumber = bayNumber;
    }

    /**
     * Parses a date/timestamp string into a comparable LocalDateTime.
     * Returns null if parsing fails.
     */
    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateStr);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(dateStr).atStartOfDay();
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Box box = (Box) o;
        return Objects.equals(boxID, box.boxID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boxID);
    }

    @Override
    public String toString() {
        String location = (warehouseId != null)
                ? String.format(" @%s[%d/%d]", warehouseId, aisle, bayNumber)
                : " @wagon";

        return String.format("Box[id=%s, sku=%s, qty=%d, expiry=%s, received=%s%s]",
                boxID, sku, quantity,
                expiryDate != null ? expiryDate : "none",
                receivedAt,
                location);
    }
}