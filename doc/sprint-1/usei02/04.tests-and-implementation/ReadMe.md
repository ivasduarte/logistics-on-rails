# USEI02 - Order Eligibility & Allocation

## 4. Tests

**Test 1:** Verifies if the system allocates inventory across multiple boxes in FEFO order.

```java
void testAllocateOrder_allocatesAcrossBoxesInFEFOOrder() {
    Warehouse warehouse = new Warehouse();

    warehouse.getItems(List.of("SKU,Name,Category,Unit,Volume,UnitWeight", "SKU0001,Item1,Cat1,PC,1.0,0.5"));

    warehouse.getBays(List.of("WarehouseID,Aisle,Bay,BoxCapacity".replace(",", ";"), "WH1;1;1;100"));

    List<String> wLines = List.of(
            "WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate",
            "W1,BOX_A,SKU0001,10,2025-12-31T00:00:00,2024-01-01T10:00:00",
            "W1,BOX_B,SKU0001,10,2025-11-30T00:00:00,2024-01-01T11:00:00",
            "W1,BOX_C,SKU0001,10,2025-12-15T00:00:00,2024-01-01T12:00:00"
    );
    warehouse.getWagons(wLines);

    warehouse.unloadWagonsIntoBays();

    warehouse.getOrders(List.of("OrderID,DueDate,Priority", "O1,2024-12-31,1"));
    warehouse.getOrderLines(List.of("OrderID,LineNo,SKU,Quantity", "O1,1,SKU0001,25"));

    OrderAllocationResult result = warehouse.allocateOrder("O1", true);
    assertNotNull(result);

    List<OrderLineResult> lineResults = result.getLineResults().stream().collect(Collectors.toList());
    assertEquals(1, lineResults.size());

    OrderLineResult lr = lineResults.get(0);
    List<Allocation> allocs = lr.getAllocations().stream().collect(Collectors.toList());

    int totalAllocated = allocs.stream().mapToInt(Allocation::getQuantityAllocated).sum();
    assertEquals(25, totalAllocated);

    List<String> boxOrder = allocs.stream().map(Allocation::getBoxID).collect(Collectors.toList());
    assertEquals(List.of("BOX_B", "BOX_C", "BOX_A"), boxOrder);

    List<Integer> qtys = allocs.stream().map(Allocation::getQuantityAllocated).collect(Collectors.toList());
    assertEquals(List.of(10, 10, 5), qtys);
}
```

**Test 2:** Verifies if the system uses FIFO allocation when there are no expiry dates.

```java
void testAllocateOrder_fifoWhenNoExpiry() {
    Warehouse warehouse = new Warehouse();

    warehouse.getItems(List.of("SKU,Name,Category,Unit,Volume,UnitWeight", "SKU0002,Item2,Cat2,PC,1.0,0.5"));

    warehouse.getBays(List.of("WarehouseID,Aisle,Bay,BoxCapacity".replace(",", ";"), "WH1;1;1;100"));

    List<String> wLines = List.of(
            "WagonID,BoxID,SKU,Quantity,ExpiryDate,ReceivedDate",
            "W1,BOX_X,SKU0002,5,,2024-03-01T10:00:00",
            "W1,BOX_Y,SKU0002,5,,2024-01-01T11:00:00",
            "W1,BOX_Z,SKU0002,5,,2024-02-01T12:00:00"
    );
    warehouse.getWagons(wLines);
    warehouse.unloadWagonsIntoBays();

    warehouse.getOrders(List.of("OrderID,DueDate,Priority", "O2,2024-12-31,1"));
    warehouse.getOrderLines(List.of("OrderID,LineNo,SKU,Quantity", "O2,1,SKU0002,7"));

    OrderAllocationResult result = warehouse.allocateOrder("O2", false);
    assertNotNull(result);

    List<OrderLineResult> lineResults = result.getLineResults().stream().collect(Collectors.toList());
    assertEquals(1, lineResults.size());

    OrderLineResult lr = lineResults.get(0);
    List<Allocation> allocs = lr.getAllocations().stream().collect(Collectors.toList());

    List<String> boxOrder = allocs.stream().map(Allocation::getBoxID).collect(Collectors.toList());
    assertEquals(List.of("BOX_Y", "BOX_Z"), boxOrder);

    List<Integer> qtys = allocs.stream().map(Allocation::getQuantityAllocated).collect(Collectors.toList());
    assertEquals(List.of(5, 2), qtys);

    int totalAllocated = qtys.stream().mapToInt(Integer::intValue).sum();
    assertEquals(7, totalAllocated);
}
```

**Test 3:** This tests the Allocation class constructor and its getters.

```java
void testConstructorAndGetters() {
        Allocation allocation = new Allocation("O1", "B1", "W1", "SKU1", 1, 2, 3, 10);
        Assertions.assertEquals("O1", allocation.getOrderID());
        Assertions.assertEquals("B1", allocation.getBoxID());
        Assertions.assertEquals("W1", allocation.getWarehouseId());
        Assertions.assertEquals("SKU1", allocation.getSku());
        Assertions.assertEquals(1, allocation.getLineNo());
        Assertions.assertEquals(2, allocation.getAisle());
        Assertions.assertEquals(3, allocation.getBayNumber());
        Assertions.assertEquals(10, allocation.getQuantityAllocated());
    }
```

**Test 4:** This tests the Allocation class setters.

```java
void testSetters() {
    Allocation allocation = new Allocation("O1", "B1", "W1", "SKU1", 1, 2, 3, 10);
    allocation.setOrderID("O2");
    allocation.setBoxID("B2");
    allocation.setWarehouseId("W2");
    allocation.setSku("SKU2");
    allocation.setLineNo(5);
    allocation.setAisle(6);
    allocation.setBayNumber(7);
    allocation.setQuantityAllocated(20);

    Assertions.assertEquals("O2", allocation.getOrderID());
    Assertions.assertEquals("B2", allocation.getBoxID());
    Assertions.assertEquals("W2", allocation.getWarehouseId());
    Assertions.assertEquals("SKU2", allocation.getSku());
    Assertions.assertEquals(5, allocation.getLineNo());
    Assertions.assertEquals(6, allocation.getAisle());
    Assertions.assertEquals(7, allocation.getBayNumber());
    Assertions.assertEquals(20, allocation.getQuantityAllocated());
}
``` 

**Test 5:** This tests the Allocation class toString method.

```java
void testToString() {
    Allocation allocation = new Allocation("O1", "B1", "W1", "SKU1", 1, 2, 3, 10);
    String str = allocation.toString();
    assertTrue(str.contains("O1"));
    assertTrue(str.contains("B1"));
    assertTrue(str.contains("W1"));
    assertTrue(str.contains("SKU1"));
    assertTrue(str.contains("Allocated: 10"));
}
```

**Test 6:** This tests the Allocation class equals and hashCode methods.

```java
void testEqualsAndHashCode() {
    Allocation a1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 2, 3, 10);
    Allocation a2 = new Allocation("O1", "B1", "W2", "SKU2", 1, 5, 6, 20); // só orderID, lineNo e boxID importam
    Allocation a3 = new Allocation("O2", "B1", "W1", "SKU1", 1, 2, 3, 10);
    Allocation a4 = new Allocation("O1", "B2", "W1", "SKU1", 1, 2, 3, 10);
    Allocation a5 = new Allocation("O1", "B1", "W1", "SKU1", 2, 2, 3, 10);

    Assertions.assertEquals(a1, a2);
    Assertions.assertNotEquals(a1, a3);
    Assertions.assertNotEquals(a1, a4);
    Assertions.assertNotEquals(a1, a5);
    Assertions.assertEquals(a1.hashCode(), a2.hashCode());
}
```

**Test 7:** This tests the OrderAllocationResult class constructor and its getters.

```java
void testConstructorAndGetters() {
    OrderAllocationResult oar = new OrderAllocationResult("O1");
    assertEquals("O1", oar.getOrderID());
    assertTrue(oar.getLineResults().isEmpty());
    assertTrue(oar.getAllAllocations().isEmpty());
    assertTrue(oar.getPerLineStatuses().isEmpty());
}
```

**Test 8:** This tests the OrderAllocationResult class setters.

```java
void testSetters() {
    OrderAllocationResult oar = new OrderAllocationResult("O1");
    oar.setOrderID("O2");
    assertEquals("O2", oar.getOrderID());

    OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
    oar.setLineResults(List.of(olr));
    assertEquals(1, oar.getLineResults().size());

    Allocation alloc = new Allocation("O2", "B1", "W1", "SKU1", 1, 1, 1, 5);
    oar.setAllAllocations(List.of(alloc));
    assertEquals(1, oar.getAllAllocations().size());

    oar.setPerLineStatuses(Map.of(1, LineStatus.ELIGIBLE));
    assertEquals(LineStatus.ELIGIBLE, oar.getPerLineStatuses().get(1));
}
```

**Test 9:** Verifies if line results are being correctly added to OrderAllocationResult.

```java
void testAddLineResultAndGetters() {
    OrderAllocationResult oar = new OrderAllocationResult("O1");
    OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
    olr.setAllocatedQty(10);
    olr.setStatus(LineStatus.ELIGIBLE);
    Allocation alloc = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 1, 10);
    olr.setAllocations(List.of(alloc));

    oar.addLineResult(olr);

    assertEquals(1, oar.getLineResults().size());
    assertEquals(1, oar.getAllAllocations().size());
    assertEquals(LineStatus.ELIGIBLE, oar.getLineStatus(1));
    assertEquals(olr, oar.getLineResult(1));
}
```

**Test 10:** Verifies if the order lines status are being correctly identified and filtered.

```java
void testStatusMethods() {
    OrderAllocationResult oar = new OrderAllocationResult("O1");
    OrderLineResult eligible = new OrderLineResult("SKU1", 1, 10);
    eligible.setAllocatedQty(10);
    eligible.setStatus(LineStatus.ELIGIBLE);

    OrderLineResult partial = new OrderLineResult("SKU2", 2, 10);
    partial.setAllocatedQty(5);
    partial.setStatus(LineStatus.PARTIAL);

    OrderLineResult undispatchable = new OrderLineResult("SKU3", 3, 10);
    undispatchable.setAllocatedQty(0);
    undispatchable.setStatus(LineStatus.UNDISPATCHABLE);

    oar.addLineResult(eligible);
    oar.addLineResult(partial);
    oar.addLineResult(undispatchable);

    assertFalse(oar.isFullyEligible());
    assertTrue(oar.hasPartialLines());
    assertTrue(oar.hasUndispatchableLines());

    List<OrderLineResult> partialLines = oar.getLinesByStatus(LineStatus.PARTIAL);
    assertEquals(1, partialLines.size());
    assertEquals(partial, partialLines.get(0));
}
```

**Test 11:** Verifies if allocation summary statistics are being calculated correctly.

```java
void testAllocationSummaryMethods() {
    OrderAllocationResult oar = new OrderAllocationResult("O1");
    Allocation alloc1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 1, 10);
    Allocation alloc2 = new Allocation("O1", "B2", "W1", "SKU2", 2, 1, 1, 5);

    OrderLineResult olr1 = new OrderLineResult("SKU1", 1, 10);
    olr1.setAllocatedQty(10);
    olr1.setStatus(LineStatus.ELIGIBLE);
    olr1.setAllocations(List.of(alloc1));

    OrderLineResult olr2 = new OrderLineResult("SKU2", 2, 10);
    olr2.setAllocatedQty(5);
    olr2.setStatus(LineStatus.PARTIAL);
    olr2.setAllocations(List.of(alloc2));

    oar.addLineResult(olr1);
    oar.addLineResult(olr2);

    assertEquals(2, oar.getTotalAllocationCount());
    assertEquals(15, oar.getTotalAllocatedQuantity());

    Map<LineStatus, Long> summary = oar.getStatusSummary();
    assertEquals(1L, summary.get(LineStatus.ELIGIBLE));
    assertEquals(1L, summary.get(LineStatus.PARTIAL));
}
```

**Test 12:** Verifies the generation of summary reports and the toString method.

```java
void testGenerateSummaryReportAndToString() {
    OrderAllocationResult oar = new OrderAllocationResult("O1");
    OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
    olr.setAllocatedQty(10);
    olr.setStatus(LineStatus.ELIGIBLE);
    oar.addLineResult(olr);

    String report = oar.generateSummaryReport();
    assertTrue(report.contains("Order Allocation Result: O1"));
    assertTrue(report.contains("Total Lines: 1"));
    assertTrue(report.contains("ELIGIBLE: 1"));

    String str = oar.toString();
    assertTrue(str.contains("order O1"));
    assertTrue(str.contains("lines=1"));
    assertTrue(str.contains("eligible=true"));
}
```

**Test 13:** This tests the OrderAllocationResult class equals and hashCode methods.

```java
void testEqualsAndHashCode() {
    OrderAllocationResult oar1 = new OrderAllocationResult("O1");
    OrderAllocationResult oar2 = new OrderAllocationResult("O1");
    OrderAllocationResult oar3 = new OrderAllocationResult("O2");

    assertEquals(oar1, oar2);
    assertNotEquals(oar1, oar3);
    assertEquals(oar1.hashCode(), oar2.hashCode());
}
```

**Test 14:** This tests the OrderLineResult class constructor and its getters.

```java
void testConstructorAndGetters() {
    OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
    assertEquals("SKU1", olr.getSku());
    assertEquals(1, olr.getLineNo());
    assertEquals(10, olr.getRequestedQty());
    assertEquals(0, olr.getAllocatedQty());
    assertEquals(LineStatus.UNDISPATCHABLE, olr.getStatus());
    assertTrue(olr.getAllocations().isEmpty());
}
```

**Test 15:** This tests the OrderLineResult class setters.

```java
void testSetters() {
    OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
    olr.setSku("SKU2");
    olr.setLineNo(2);
    olr.setRequestedQty(20);
    olr.setAllocatedQty(5);
    olr.setStatus(LineStatus.PARTIAL);
    olr.setAllocations(List.of(new Allocation("O1", "B1", "W1", "SKU2", 2, 1, 1, 5)));

    assertEquals("SKU2", olr.getSku());
    assertEquals(2, olr.getLineNo());
    assertEquals(20, olr.getRequestedQty());
    assertEquals(5, olr.getAllocatedQty());
    assertEquals(LineStatus.PARTIAL, olr.getStatus());
    assertEquals(1, olr.getAllocations().size());
}
```

**Test 16:** Verifies if allocations are being correctly added to OrderLineResult.

```java
void testAddAllocation() {
    OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
    Allocation alloc = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 1, 7);
    olr.addAllocation(alloc);

    assertEquals(7, olr.getAllocatedQty());
    assertEquals(1, olr.getAllocations().size());
    assertTrue(olr.getAllocations().contains(alloc));
}
```

**Test 17:** Verifies if the computeStatus method of OrderLineResult correctly works in strict mode.

```java
void testComputeStatusStrictMode() {
    OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
    olr.setAllocatedQty(0);
    olr.computeStatus(true);
    assertEquals(LineStatus.UNDISPATCHABLE, olr.getStatus());

    olr.setAllocatedQty(10);
    olr.computeStatus(true);
    assertEquals(LineStatus.ELIGIBLE, olr.getStatus());

    olr.setAllocatedQty(5);
    olr.computeStatus(true);
    assertEquals(LineStatus.UNDISPATCHABLE, olr.getStatus());
}
```

**Test 18:** Verifies if the computeStatus method of OrderLineResult correctly works in non-strict mode.

```java
void testComputeStatusNonStrictMode() {
    OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
    olr.setAllocatedQty(5);
    olr.computeStatus(false);
    assertEquals(LineStatus.PARTIAL, olr.getStatus());
}
```

**Test 19:** Verifies if the order line request is fully satisfied.

```java
void testIsFullySatisfiedAndHasAllocations() {
    OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
    assertFalse(olr.isFullySatisfied());
    assertFalse(olr.hasAllocations());

    olr.setAllocatedQty(10);
    assertTrue(olr.isFullySatisfied());
    assertTrue(olr.hasAllocations());
}
```

**Test 20:** This tests the OrderLineResult class toString method.

```java
void testToString() {
    OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
    String str = olr.toString();
    assertTrue(str.contains("Line 1"));
    assertTrue(str.contains("SKU1"));
    assertTrue(str.contains("allocated"));
    assertTrue(str.contains("UNDISPATCHABLE"));
}
```

**Test 21:** This tests the OrderLineResult class equals and hashCode methods.

```java
void testEqualsAndHashCode() {
    OrderLineResult olr1 = new OrderLineResult("SKU1", 1, 10);
    OrderLineResult olr2 = new OrderLineResult("SKU2", 1, 20);
    OrderLineResult olr3 = new OrderLineResult("SKU1", 2, 10);

    assertEquals(olr1, olr2);
    assertNotEquals(olr1, olr3);
    assertEquals(olr1.hashCode(), olr2.hashCode());
}
```

## 5. Construction (Implementation)

### Class Warehouse.java

```java
public OrderAllocationResult allocateOrder(String orderId, boolean strictMode) {
    OrderAllocationResult result = new OrderAllocationResult(orderId);

    Order order = orders.get(orderId);
    if (order == null) {
        return result;
    }

    Set<OrderLines> lines = order_lines.get(order);
    if (lines == null || lines.isEmpty()) {
        return result;
    }

    List<OrderLines> sortedLines = new ArrayList<>(lines);
    sortedLines.sort(Comparator.comparingInt(OrderLines::getLineNo));

    Map<String, Integer> boxReservations = new HashMap<>();

    for (OrderLines line : sortedLines) {
        OrderLineResult lineResult = new OrderLineResult(
                line.getSku(),
                line.getLineNo(),
                line.getQty()
        );

        int remainingQty = line.getQty();

        List<BoxLocation> availableBoxes = collectBoxesForSku(line.getSku());

        for (BoxLocation boxLoc : availableBoxes) {
            if (remainingQty <= 0) {
                break; // Line fully satisfied
            }

            Box box = boxLoc.box;
            Bay bay = boxLoc.bay;
            String warehouseId = boxLoc.warehouseId;

            int reservedQty = boxReservations.getOrDefault(box.getBoxID(), 0);
            int availableQty = box.getQuantity() - reservedQty;

            if (availableQty <= 0) {
                continue;
            }

            int allocateQty = Math.min(remainingQty, availableQty);

            Allocation allocation = new Allocation(
                    orderId,
                    box.getBoxID(),
                    warehouseId,
                    line.getSku(),
                    line.getLineNo(),
                    bay.getAisle(),
                    bay.getBayNumber(),
                    allocateQty
            );

            lineResult.addAllocation(allocation);
            boxReservations.put(box.getBoxID(), reservedQty + allocateQty);
            remainingQty -= allocateQty;
        }

        lineResult.computeStatus(strictMode);
        result.addLineResult(lineResult);
    }
    return result;
}    

private List<BoxLocation> collectBoxesForSku(String sku) {
    List<BoxLocation> boxLocations = new ArrayList<>();

    for (Map.Entry<String, Set<Bay>> warehouseEntry : warehouses.entrySet()) {
        String warehouseId = warehouseEntry.getKey();
        Set<Bay> bays = warehouseEntry.getValue();

        for (Bay bay : bays) {
            for (Box box : bay.getBoxes()) {
                if (box.getSku().equals(sku)) {
                    boxLocations.add(new BoxLocation(box, bay, warehouseId));
                }
            }
        }
    }

    Collections.sort(boxLocations);

    return boxLocations;
}
```

### Class Allocation.java

```java
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
        return String.format("%s_%d: %s at Warehouse: %s, Aisle: %d, Bay: %d -> Allocated: %d units of %s",
                orderID, lineNo, boxID, warehouseId, aisle, bayNumber, quantityAllocated, sku);
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
```

### Class OrderAllocationResult.java

```java
public class OrderAllocationResult {

    private String orderID;

    private List<OrderLineResult> lineResults;
    private List<Allocation> allAllocations;

    private Map<Integer, LineStatus> perLineStatuses;
    
    public OrderAllocationResult(String orderID) {
        this.orderID = orderID;

        this.lineResults = new ArrayList<>();
        this.allAllocations = new ArrayList<>();
        this.perLineStatuses = new HashMap<>();
    }
    
    public String getOrderID() {
        return orderID;
    }
    public List<OrderLineResult> getLineResults() {
        return lineResults;
    }
    public List<Allocation> getAllAllocations() {
        return allAllocations;
    }
    public Map<Integer, LineStatus> getPerLineStatuses() {
        return perLineStatuses;
    }
    
    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }
    public void setLineResults(List<OrderLineResult> lineResults) {
        this.lineResults = lineResults;
    }
    public void setAllAllocations(List<Allocation> allAllocations) {
        this.allAllocations = allAllocations;
    }
    public void setPerLineStatuses(Map<Integer, LineStatus> perLineStatuses) {
        this.perLineStatuses = perLineStatuses;
    }
    
    public void addLineResult(OrderLineResult lineResult) {
        this.lineResults.add(lineResult);
        this.perLineStatuses.put(lineResult.getLineNo(), lineResult.getStatus());
        this.allAllocations.addAll(lineResult.getAllocations());
    }
    
    public OrderLineResult getLineResult(int lineNo) {
        return lineResults.stream().filter(lr -> lr.getLineNo() == lineNo).findFirst().orElse(null);
    }
    
    public LineStatus getLineStatus(int lineNo) {
        return perLineStatuses.get(lineNo);
    }
    
    public boolean isFullyEligible() {
        return lineResults.stream().allMatch(lr -> lr.getStatus() == LineStatus.ELIGIBLE);
    }
    
    public boolean hasPartialLines() {
        return lineResults.stream().anyMatch(lr -> lr.getStatus() == LineStatus.PARTIAL);
    }
    
    public boolean hasUndispatchableLines() {
        return lineResults.stream().anyMatch(lr -> lr.getStatus() == LineStatus.UNDISPATCHABLE);
    }
    
    public List<OrderLineResult> getLinesByStatus(LineStatus status) {
        return lineResults.stream().filter(lr -> lr.getStatus() == status).collect(Collectors.toList());
    }
    
    public int getTotalAllocationCount() {
        return allAllocations.size();
    }
    
    public int getTotalAllocatedQuantity() {
        return allAllocations.stream().mapToInt(Allocation::getQuantityAllocated).sum();
    }
    
    public Map<LineStatus, Long> getStatusSummary() {
        return lineResults.stream().collect(Collectors.groupingBy(OrderLineResult::getStatus, Collectors.counting()));
    }
    
    public String generateSummaryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Order Allocation Result: ").append(orderID).append(" ===\n");
        sb.append("Total Lines: ").append(lineResults.size()).append("\n");

        Map<LineStatus, Long> summary = getStatusSummary();
        sb.append("Status Summary:\n");
        sb.append("  ELIGIBLE: ").append(summary.getOrDefault(LineStatus.ELIGIBLE, 0L)).append("\n");
        sb.append("  PARTIAL: ").append(summary.getOrDefault(LineStatus.PARTIAL, 0L)).append("\n");
        sb.append("  UNDISPATCHABLE: ").append(summary.getOrDefault(LineStatus.UNDISPATCHABLE, 0L)).append("\n");

        sb.append("Total Allocations: ").append(getTotalAllocationCount()).append("\n");
        sb.append("Total Quantity Allocated: ").append(getTotalAllocatedQuantity()).append("\n\n");

        sb.append("Line Details:\n");
        for (OrderLineResult lr : lineResults) {
            sb.append("  ").append(lr.toString()).append("\n");
        }

        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("OrderAllocationResult for order %s: lines=%d, allocations=%d, eligible=%s.", orderID, lineResults.size(), allAllocations.size(), isFullyEligible());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderAllocationResult that = (OrderAllocationResult) o;
        return Objects.equals(orderID, that.orderID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderID);
    }
}
```

### Class OrderLineResult.java

```java
public class OrderLineResult {

    private String sku;

    private int lineNo;
    private int requestedQty;
    private int allocatedQty;

    private LineStatus status;

    private List<Allocation> allocations;
    
    public OrderLineResult(String sku, int lineNo, int requestedQty) {
        this.lineNo = lineNo;
        this.sku = sku;
        this.requestedQty = requestedQty;

        this.allocatedQty = 0;
        this.allocations = new ArrayList<>();
        this.status = LineStatus.UNDISPATCHABLE;
    }
    
    public String getSku() {
        return sku;
    }
    public int getLineNo() {
        return lineNo;
    }
    public int getRequestedQty() {
        return requestedQty;
    }
    public int getAllocatedQty() {
        return allocatedQty;
    }
    public LineStatus getStatus() {
        return status;
    }
    public List<Allocation> getAllocations() {
        return allocations;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }
    public void setRequestedQty(int requestedQty) {
        this.requestedQty = requestedQty;
    }
    public void setAllocatedQty(int allocatedQty) {
        this.allocatedQty = allocatedQty;
    }
    public void setStatus(LineStatus status) {
        this.status = status;
    }
    public void setAllocations(List<Allocation> allocations) {
        this.allocations = allocations;
    }
    
    public void addAllocation(Allocation allocation) {
        this.allocations.add(allocation);
        this.allocatedQty += allocation.getQuantityAllocated();
    }
    
    public void computeStatus(boolean strictMode) {
        if (allocatedQty == 0) {
            this.status = LineStatus.UNDISPATCHABLE;
        } else if (allocatedQty >= requestedQty) {
            this.status = LineStatus.ELIGIBLE;
        } else {
            // allocatedQty > 0 but < requestedQty
            if (strictMode) {
                this.status = LineStatus.UNDISPATCHABLE;
            } else {
                this.status = LineStatus.PARTIAL;
            }
        }
    }
    
    public boolean isFullySatisfied() {
        return allocatedQty >= requestedQty;
    }
    
    public boolean hasAllocations() {
        return allocatedQty > 0;
    }
    
    @Override
    public String toString() {
        return String.format("Line %d [%s]: %d/%d allocated (%s) - %d allocation(s)",
                lineNo, sku, allocatedQty, requestedQty, status, allocations.size());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderLineResult that = (OrderLineResult) o;
        return lineNo == that.lineNo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNo);
    }
}
```