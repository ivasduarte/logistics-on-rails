package esinf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import warehouseMngt.order.Allocation;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AllocationTest {

    @Test
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

    @Test
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

    @Test
    void testToString() {
        Allocation allocation = new Allocation("O1", "B1", "W1", "SKU1", 1, 2, 3, 10);
        String str = allocation.toString();
        assertTrue(str.contains("O1"));
        assertTrue(str.contains("B1"));
        assertTrue(str.contains("W1"));
        assertTrue(str.contains("SKU1"));
        assertTrue(str.contains("Allocated: 10"));
    }

    @Test
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
}
