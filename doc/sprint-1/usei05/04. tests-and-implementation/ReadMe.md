# USEI05 - Returns & Quarantine

## 4. Tests

**Test 1:** Validates Returns class constructor and basic functionality.

```java
@Test
void testConstructor() {
    Returns returns = new Returns(
        "RET001", 
        "SKU123", 
        5, 
        ReturnReasons.REMORSE, 
        "2024-01-15T10:30:00", 
        "2024-12-31"
    );

    assertEquals("RET001", returns.getReturnID());
    assertEquals("SKU123", returns.getSku());
    assertEquals(5, returns.getQty());
    assertEquals(ReturnReasons.REMORSE, returns.getReason());
    assertEquals("2024-01-15T10:30:00", returns.getTimestamp());
    assertEquals("2024-12-31", returns.getExpiryDate());
}
```

**Test 2:** Validates QuarantineQueue ordering by timestamp and returnID.

```java
@Test
void testGetNextForInspectionOrderingByTimestamp() {
    Returns olderReturn = new Returns("R001", "SKU001", 5, ReturnReasons.DAMAGED, "2024-01-01", "2024-12-31");
    Returns newerReturn = new Returns("R002", "SKU002", 3, ReturnReasons.REMORSE, "2024-01-02", "2024-12-31");
    
    quarantineQueue.addReturn(olderReturn);
    quarantineQueue.addReturn(newerReturn);
    
    // Should return most recent first
    Returns nextItem = quarantineQueue.getNextForInspection();
    assertNotNull(nextItem);
    assertEquals("R002", nextItem.getReturnID());
}
```

**Test 3:** Validates ReturnsManager processing with different return reasons.

```java
@Test
void testProcessReturnsWithDifferentReasons() throws IOException {
    // Test with REMORSE reason (should be restocked)
    Returns remorseReturn = new Returns(
        "RET001", 
        "SKU123", 
        5, 
        ReturnReasons.REMORSE, 
        "2024-01-15T10:30:00", 
        "2024-12-31"
    );

    returnsManager.receiveReturn(remorseReturn);
    assertDoesNotThrow(() -> returnsManager.processReturns());

    // Test with DAMAGED reason (should be discarded)
    Returns damagedReturn = new Returns(
        "RET002", 
        "SKU456", 
        3, 
        ReturnReasons.DAMAGED, 
        "2024-01-16T11:00:00", 
        "2024-12-31"
    );

    returnsManager.receiveReturn(damagedReturn);
    assertDoesNotThrow(() -> returnsManager.processReturns());
}
```

**Test 4:** Validates AuditLog functionality.

```java
@Test
void testLogInspection_WritesExpectedContent_WithRestockedAndDiscarded() throws IOException {
    auditLog.logInspection("R001", "SKU123", 10, 5, 2, StockAction.RESTOCKED);

    List<String> lines = Files.readAllLines(Paths.get(TEST_DIR + TEST_FILE));
    assertEquals(1, lines.size(), "The file should have one line written");

    String logLine = lines.get(0);
    assertTrue(logLine.contains("returnID = R001"));
    assertTrue(logLine.contains("SKU = SKU123"));
    assertTrue(logLine.contains("action = RESTOCK"));
    assertTrue(logLine.contains("quantity = 10"));
    assertTrue(logLine.contains("qtyRestocked = 5"));
    assertTrue(logLine.contains("qtyDiscarded = 2"));
}
```

**Test 5:** Validates Returns business logic methods.

```java
@Test
void testIsExpiredWithPastDate() {
    // Test with expired date (past date)
    String expiredDate = LocalDate.now().minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE);
    Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", expiredDate);
    
    assertTrue(returns.isExpired());
}

@Test
void testIsDamagedWithDamagedReason() {
    // Test with DAMAGED reason
    Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.DAMAGED, "2024-01-15T10:30:00", "2024-12-31");
    
    assertTrue(returns.isDamaged());
}

@Test
void testIsCustomerRemorseWithRemorseReason() {
    // Test with REMORSE reason
    Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
    
    assertTrue(returns.isCustomerRemorse());
}
```

**Test 6:** Validates QuarantineQueue edge cases.

```java
@Test
void testAddReturnWithNull() {
    assertThrows(NullPointerException.class, () -> {
        quarantineQueue.addReturn(null);
    });
}

@Test
void testGetNextForInspectionEmptyQueue() {
    assertNull(quarantineQueue.getNextForInspection());
}

@Test
void testGetNextForInspectionRemovesItem() {
    Returns returnItem = new Returns("R001", "SKU001", 5, ReturnReasons.DAMAGED, "2024-01-01", "2024-12-31");
    
    quarantineQueue.addReturn(returnItem);
    
    // First call should return the item
    Returns firstCall = quarantineQueue.getNextForInspection();
    assertNotNull(firstCall);
    assertEquals("R001", firstCall.getReturnID());
    
    // Second call should return null (empty queue)
    assertNull(quarantineQueue.getNextForInspection());
}
```

**Test 7:** Validates ReturnsManager with edge cases.

```java
@Test
void testProcessReturnsWithEmptyQueue() throws IOException {
    // Test processing returns when queue is empty
    assertDoesNotThrow(() -> returnsManager.processReturns());
    
    // Should not throw exception and should handle empty queue gracefully
}

@Test
void testProcessReturnsWithExpiredItems() throws IOException {
    // Create return with expired date
    String expiredDate = LocalDate.now().minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE);
    Returns expiredReturn = new Returns(
        "RET001", 
        "SKU123", 
        5, 
        ReturnReasons.REMORSE, 
        "2024-01-15T10:30:00", 
        expiredDate
    );

    returnsManager.receiveReturn(expiredReturn);
    assertDoesNotThrow(() -> returnsManager.processReturns());
}
```

**Test 8:** Validates AuditLog with different scenarios.

```java
@Test
void testLogInspection_WritesExpectedContent_WithoutRestockedAndDiscarded() throws IOException {
    auditLog.logInspection("R002", "SKU999", 20, 0, 0, StockAction.DISCARDED);

    List<String> lines = Files.readAllLines(Paths.get(TEST_DIR + TEST_FILE));
    assertEquals(1, lines.size(), "The file should have one line written");

    String logLine = lines.get(0);
    assertTrue(logLine.contains("returnID = R002"));
    assertTrue(logLine.contains("SKU = SKU999"));
    assertTrue(logLine.contains("action = DISCARD"));
    assertTrue(logLine.contains("quantity = 20"));
    assertFalse(logLine.contains("qtyRestocked"));
    assertFalse(logLine.contains("qtyDiscarded"));
}

@Test
void testLogInspection_AppendsToExistingFile() throws IOException {
    auditLog.logInspection("R100", "SKU100", 15, 5, 0, StockAction.RESTOCKED);
    auditLog.logInspection("R101", "SKU101", 8, 0, 0, StockAction.DISCARDED);

    List<String> lines = Files.readAllLines(Paths.get(TEST_DIR + TEST_FILE));
    assertEquals(2, lines.size(), "The file should contain two log entries");
    assertTrue(lines.get(0).contains("R100"));
    assertTrue(lines.get(1).contains("R101"));
}
```

## 5. Construction (Implementation)

### Class Returns.java

```java
package esinf.us05;

import java.time.LocalDate;
import java.util.Objects;

public class Returns {
    private String returnID;
    private String sku;
    private int qty;
    private ReturnReasons reason;
    private String timestamp;
    private String expiryDate;
    private boolean discarded = false;

    public Returns(String returnID, String sku, int qty, ReturnReasons reason, String timestamp, String expiryDate) {
        this.returnID = returnID;
        this.sku = sku;
        this.qty = qty;
        this.reason = reason;
        this.timestamp = timestamp;
        this.expiryDate = expiryDate;
    }

    // Getters and setters
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

    // Business logic methods
    public boolean isExpired() {
        LocalDate expiryDate = LocalDate.parse(getExpiryDate());
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isDamaged() {
        return getReason() == ReturnReasons.DAMAGED;
    }

    public boolean isCustomerRemorse() {
        return getReason() == ReturnReasons.REMORSE;
    }

    public boolean isCycleCount() {
        return getReason() == ReturnReasons.CYCLE_COUNT;
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
```

### Class ReturnsManager.java

```java
package esinf.us05;

import esinf.us01.Warehouse;

import java.io.IOException;

public class ReturnsManager {
    public QuarantineQueue quarantineQueue;
    public InventoryInspection inspection;

    public ReturnsManager(Warehouse warehouse, String logFilePath) {
        this.quarantineQueue = new QuarantineQueue();
        AuditLog auditLog = new AuditLog(logFilePath);
        this.inspection = new InventoryInspection(warehouse, auditLog);
    }

    // Receives a return item and adds it to the quarantine queue.
    public void receiveReturn(Returns returnItem) {
        quarantineQueue.addReturn(returnItem);
    }

    /**
     * Processes the next return item in the quarantine queue.
     * @throws IOException
     */
    public void processReturns() throws IOException {
        Returns returnedItem = quarantineQueue.getNextForInspection();

        if(returnedItem == null) {
            System.out.println("No returns to process.");
            return;
        }
        inspection.inspect(returnedItem);
    }
}
```

### Class QuarantineQueue.java

```java
package esinf.us05;

import java.util.ArrayList;
import java.util.List;

public class QuarantineQueue {
    private List<Returns> quarantinedItems;

    public QuarantineQueue() {
        this.quarantinedItems = new ArrayList<>();
    }

    public QuarantineQueue(List<Returns> quarantinedItems) {
        this.quarantinedItems = quarantinedItems;
    }

    // Adds a returned item to the quarantine queue.
    public void addReturn(Returns returnItem) {
        if (returnItem == null) {
            throw new NullPointerException("Return item cannot be null");
        }
        quarantinedItems.add(returnItem);
    }

    /**
     * This method gets the processing return.
     */
    public Returns getNextForInspection() {
        if (quarantinedItems.isEmpty()) return null;

        // Orders: desc for timestamp, asc for returnID
        quarantinedItems.sort((r1, r2) -> {
            int timeCmp = r2.getTimestamp().compareTo(r1.getTimestamp());
            if (timeCmp != 0) return timeCmp;
            return r1.getReturnID().compareTo(r2.getReturnID());
        });
        return quarantinedItems.remove(0);
    }
}
```

### Class AuditLog.java

```java
package esinf.us05;

import esinf.us01.Box;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLog {
    private String LOG_FILE_PATH = "audit-log-file/";
    private String LOG_FILE_NAME = "audit-log.txt";

    public AuditLog(String LOG_FILE_PATH) {
        this.LOG_FILE_PATH = LOG_FILE_PATH;
    }

    public void logInspection(String returnID, String sku, int quantity, int qtyRestocked, int qtyDiscarded, StockAction action) throws IOException {
        try(FileWriter writer = new FileWriter(LOG_FILE_PATH + LOG_FILE_NAME, true);
        BufferedWriter bw = new BufferedWriter(writer);
        PrintWriter out = new PrintWriter(bw)) {

            LocalDateTime timestamp = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            StringBuilder sb = new StringBuilder();
            sb.append(timestamp.format(formatter)).append(" | returnID = ").append(returnID).append(" | SKU = ")
                    .append(sku).append(" | action = ").append(action).append(" | quantity = ").append(quantity);

            if(qtyRestocked != 0 && qtyDiscarded != 0){
                sb.append(" | qtyRestocked = ").append(qtyRestocked).append(" | qtyDiscarded = ").append(qtyDiscarded);
            }
            out.println(sb.toString());
        } catch (IOException e){
            System.err.println("Error writing to audit log file: " + e.getMessage());
        }
    }
}
```

### Class InventoryInspection.java

```java
package esinf.us05;

import esinf.us01.Box;
import esinf.us01.Warehouse;

import java.io.IOException;
import java.time.LocalDateTime;

public class InventoryInspection {
    private Warehouse warehouse;
    private AuditLog auditLog;

    public InventoryInspection(Warehouse warehouse, AuditLog auditLog) {
        this.warehouse = warehouse;
        this.auditLog = auditLog;
    }

    public boolean shouldBeDiscarded(Returns returnedItem){
        return returnedItem.getReason().shouldBeDiscarded();
    }

    public void inspect(Returns returnedItem) throws IOException {
        StockAction action;

        if(shouldBeDiscarded(returnedItem)){
            action = StockAction.DISCARDED;
            auditLog.logInspection(returnedItem.getReturnID(), returnedItem.getSku(), returnedItem.getQty(), 0, 0, action);
        }
    }

    public void restock(Returns returnedItem){
        // Create a new box for the returned item
        String newBoxID = "RET-" + returnedItem.getReturnID();
        LocalDateTime receivedAt = LocalDateTime.now();
        
        // Parse expiry date from string if provided
        LocalDateTime expiryDate = null;
        if (returnedItem.getExpiryDate() != null && !returnedItem.getExpiryDate().isBlank()) {
            try {
                expiryDate = LocalDateTime.parse(returnedItem.getExpiryDate());
            } catch (Exception e) {
                // If parsing fails, try parsing as date and convert to datetime
                try {
                    expiryDate = java.time.LocalDate.parse(returnedItem.getExpiryDate()).atStartOfDay();
                } catch (Exception e2) {
                    // If both fail, leave as null
                }
            }
        }

        Box newBox = new Box(newBoxID, returnedItem.getSku(), returnedItem.getQty(), expiryDate, receivedAt.toString());
        warehouse.insertBox(newBox);
    }
}
```

### Class ReturnReasons.java

```java
package esinf.us05;

import java.util.Locale;

public enum ReturnReasons {
    REMORSE("Client didn't want the product"),
    DAMAGED("Product is damaged"),
    EXPIRED("Product is expired"),
    CYCLE_COUNT("Product was found during an audit");

    private final String description;

    ReturnReasons(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean shouldBeDiscarded() {
        return this == DAMAGED || this == EXPIRED;
    }

    public boolean shouldBeRestocked(){
        return this == REMORSE || this == CYCLE_COUNT;
    }

    public static ReturnReasons parse(String raw) {
        if (raw == null) throw new IllegalArgumentException("Reason cannot be null");
        String normalized = raw.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException("Reason cannot be blank");

        String upper = normalized.toUpperCase(Locale.ROOT);
        // Direct matches to enum names
        try {
            return ReturnReasons.valueOf(upper.replace(' ', '_').replace('-', '_'));
        } catch (IllegalArgumentException ignored) { }

        // Common aliases mapping
        switch (upper.replace(' ', '_').replace('-', '_')) {
            case "CUSTOMER_REMORSE":
            case "REMORSE":
            case "CLIENT_REMORSE":
                return REMORSE;
            case "DAMAGED":
            case "DAMAGE":
                return DAMAGED;
            case "EXPIRED":
            case "EXPIRATION":
                return EXPIRED;
            case "CYCLE_COUNT":
            case "CYCLECOUNT":
            case "AUDIT":
                return CYCLE_COUNT;
            default:
                throw new IllegalArgumentException("Unknown return reason: " + raw);
        }
    }
}
```

### Class StockAction.java

```java
package esinf.us05;

public enum StockAction {
    DISCARDED,
    RESTOCKED;
}
```