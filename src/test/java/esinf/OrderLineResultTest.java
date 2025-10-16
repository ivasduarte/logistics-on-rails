package esinf;

import esinf.us02.Allocation;
import esinf.us02.LineStatus;
import esinf.us02.OrderLineResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderLineResultTest {

    @Test
    void testConstructorAndGetters() {
        OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
        assertEquals("SKU1", olr.getSku());
        assertEquals(1, olr.getLineNo());
        assertEquals(10, olr.getRequestedQty());
        assertEquals(0, olr.getAllocatedQty());
        assertEquals(LineStatus.UNDISPATCHABLE, olr.getStatus());
        assertTrue(olr.getAllocations().isEmpty());
    }

    @Test
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

    @Test
    void testAddAllocation() {
        OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
        Allocation alloc = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 1, 7);
        olr.addAllocation(alloc);

        assertEquals(7, olr.getAllocatedQty());
        assertEquals(1, olr.getAllocations().size());
        assertTrue(olr.getAllocations().contains(alloc));
    }

    @Test
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

    @Test
    void testComputeStatusNonStrictMode() {
        OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
        olr.setAllocatedQty(5);
        olr.computeStatus(false);
        assertEquals(LineStatus.PARTIAL, olr.getStatus());
    }

    @Test
    void testIsFullySatisfiedAndHasAllocations() {
        OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
        assertFalse(olr.isFullySatisfied());
        assertFalse(olr.hasAllocations());

        olr.setAllocatedQty(10);
        assertTrue(olr.isFullySatisfied());
        assertTrue(olr.hasAllocations());
    }

    @Test
    void testToString() {
        OrderLineResult olr = new OrderLineResult("SKU1", 1, 10);
        String str = olr.toString();
        assertTrue(str.contains("Line 1"));
        assertTrue(str.contains("SKU1"));
        assertTrue(str.contains("allocated"));
        assertTrue(str.contains("UNDISPATCHABLE"));
    }

    @Test
    void testEqualsAndHashCode() {
        OrderLineResult olr1 = new OrderLineResult("SKU1", 1, 10);
        OrderLineResult olr2 = new OrderLineResult("SKU2", 1, 20);
        OrderLineResult olr3 = new OrderLineResult("SKU1", 2, 10);

        assertEquals(olr1, olr2);
        assertNotEquals(olr1, olr3);
        assertEquals(olr1.hashCode(), olr2.hashCode());
    }
}
