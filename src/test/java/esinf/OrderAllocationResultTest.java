package esinf;

import org.junit.jupiter.api.Test;
import warehouseMngt.order.Allocation;
import warehouseMngt.order.OrderAllocationResult;
import warehouseMngt.order.OrderLineResult;
import utils.LineStatus;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OrderAllocationResultTest {
    @Test
    void testConstructorAndGetters() {
        OrderAllocationResult oar = new OrderAllocationResult("O1");
        assertEquals("O1", oar.getOrderID());
        assertTrue(oar.getLineResults().isEmpty());
        assertTrue(oar.getAllAllocations().isEmpty());
        assertTrue(oar.getPerLineStatuses().isEmpty());
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    void testEqualsAndHashCode() {
        OrderAllocationResult oar1 = new OrderAllocationResult("O1");
        OrderAllocationResult oar2 = new OrderAllocationResult("O1");
        OrderAllocationResult oar3 = new OrderAllocationResult("O2");

        assertEquals(oar1, oar2);
        assertNotEquals(oar1, oar3);
        assertEquals(oar1.hashCode(), oar2.hashCode());
    }
}
