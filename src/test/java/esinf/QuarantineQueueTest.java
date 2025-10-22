package esinf;

import esinf.us05.QuarantineQueue;
import esinf.us05.Returns;
import esinf.us05.ReturnReasons;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuarantineQueueTest {
    private QuarantineQueue quarantineQueue;
    private List<Returns> testReturns;

    @BeforeEach
    void setUp() {
        quarantineQueue = new QuarantineQueue();
        testReturns = new ArrayList<>();
    }

    @Test
    void testEmptyConstructor() {
        QuarantineQueue emptyQueue = new QuarantineQueue();
        assertNotNull(emptyQueue);

        // Checks if getNextForInspection returns null for empty queue
        assertNull(emptyQueue.getNextForInspection());
    }

    @Test
    void testConstructorWithList() {
        Returns return1 = new Returns("R001", "SKU001", 5, ReturnReasons.DAMAGED, "2024-01-01", "2024-12-31");
        Returns return2 = new Returns("R002", "SKU002", 3, ReturnReasons.REMORSE, "2024-01-02", "2024-12-31");
        
        testReturns.add(return1);
        testReturns.add(return2);
        
        QuarantineQueue queueWithList = new QuarantineQueue(testReturns);
        assertNotNull(queueWithList);
        
        // Check if items were added correctly
        assertNotNull(queueWithList.getNextForInspection());
    }

    @Test
    void testAddReturn() {
        Returns returnItem = new Returns("R001", "SKU001", 5, ReturnReasons.DAMAGED, "2024-01-01", "2024-12-31");
        
        quarantineQueue.addReturn(returnItem);
        
        // Checks if the item was added (getNextForInspection should not return null)
        assertNotNull(quarantineQueue.getNextForInspection());
    }

    @Test
    void testAddMultipleReturns() {
        Returns return1 = new Returns("R001", "SKU001", 5, ReturnReasons.DAMAGED, "2024-01-01", "2024-12-31");
        Returns return2 = new Returns("R002", "SKU002", 3, ReturnReasons.REMORSE, "2024-01-02", "2024-12-31");
        
        quarantineQueue.addReturn(return1);
        quarantineQueue.addReturn(return2);
        
        // Should return the most recent item first (timestamp desc)
        Returns nextItem = quarantineQueue.getNextForInspection();
        assertNotNull(nextItem);
        assertEquals("R002", nextItem.getReturnID());
    }

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

    @Test
    void testGetNextForInspectionOrderingByReturnID() {
        Returns return1 = new Returns("R002", "SKU001", 5, ReturnReasons.DAMAGED, "2024-01-01", "2024-12-31");
        Returns return2 = new Returns("R001", "SKU002", 3, ReturnReasons.REMORSE, "2024-01-01", "2024-12-31");
        
        quarantineQueue.addReturn(return1);
        quarantineQueue.addReturn(return2);
        
        // With equal timestamps, it should return the smallest returnID first
        Returns nextItem = quarantineQueue.getNextForInspection();
        assertNotNull(nextItem);
        assertEquals("R001", nextItem.getReturnID());
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

    @Test
    void testGetNextForInspectionMultipleItems() {
        Returns return1 = new Returns("R001", "SKU001", 5, ReturnReasons.DAMAGED, "2024-01-01", "2024-12-31");
        Returns return2 = new Returns("R002", "SKU002", 3, ReturnReasons.REMORSE, "2024-01-02", "2024-12-31");
        Returns return3 = new Returns("R003", "SKU003", 2, ReturnReasons.CYCLE_COUNT, "2024-01-03", "2024-12-31");
        
        quarantineQueue.addReturn(return1);
        quarantineQueue.addReturn(return2);
        quarantineQueue.addReturn(return3);
        
        // Should return in the order: R003 (most recent), R002, R001
        Returns first = quarantineQueue.getNextForInspection();
        assertEquals("R003", first.getReturnID());
        
        Returns second = quarantineQueue.getNextForInspection();
        assertEquals("R002", second.getReturnID());
        
        Returns third = quarantineQueue.getNextForInspection();
        assertEquals("R001", third.getReturnID());
        
        // Queue must be empty
        assertNull(quarantineQueue.getNextForInspection());
    }

    @Test
    void testDifferentReturnReasons() {
        Returns damagedReturn = new Returns("R001", "SKU001", 5, ReturnReasons.DAMAGED, "2024-01-01", "2024-12-31");
        Returns remorseReturn = new Returns("R002", "SKU002", 3, ReturnReasons.REMORSE, "2024-01-02", "2024-12-31");
        Returns expiredReturn = new Returns("R003", "SKU003", 2, ReturnReasons.EXPIRED, "2024-01-03", "2024-12-31");
        Returns cycleCountReturn = new Returns("R004", "SKU004", 1, ReturnReasons.CYCLE_COUNT, "2024-01-04", "2024-12-31");
        
        quarantineQueue.addReturn(damagedReturn);
        quarantineQueue.addReturn(remorseReturn);
        quarantineQueue.addReturn(expiredReturn);
        quarantineQueue.addReturn(cycleCountReturn);

        // Should process in timestamp order (most recent first)
        assertEquals("R004", quarantineQueue.getNextForInspection().getReturnID());
        assertEquals("R003", quarantineQueue.getNextForInspection().getReturnID());
        assertEquals("R002", quarantineQueue.getNextForInspection().getReturnID());
        assertEquals("R001", quarantineQueue.getNextForInspection().getReturnID());
    }

    @Test
    void testSameTimestampDifferentReturnIDs() {
        String sameTimestamp = "2024-01-01";
        Returns returnA = new Returns("R003", "SKU001", 5, ReturnReasons.DAMAGED, sameTimestamp, "2024-12-31");
        Returns returnB = new Returns("R001", "SKU002", 3, ReturnReasons.REMORSE, sameTimestamp, "2024-12-31");
        Returns returnC = new Returns("R002", "SKU003", 2, ReturnReasons.CYCLE_COUNT, sameTimestamp, "2024-12-31");
        
        quarantineQueue.addReturn(returnA);
        quarantineQueue.addReturn(returnB);
        quarantineQueue.addReturn(returnC);
        
        // With equal timestamps, you should sort by returnED (ascending
        assertEquals("R001", quarantineQueue.getNextForInspection().getReturnID());
        assertEquals("R002", quarantineQueue.getNextForInspection().getReturnID());
        assertEquals("R003", quarantineQueue.getNextForInspection().getReturnID());
    }

    @Test
    void testZeroQuantity() {
        Returns zeroQtyReturn = new Returns("R001", "SKU001", 0, ReturnReasons.DAMAGED, "2024-01-01", "2024-12-31");
        
        quarantineQueue.addReturn(zeroQtyReturn);
        
        Returns retrieved = quarantineQueue.getNextForInspection();
        assertNotNull(retrieved);
        assertEquals(0, retrieved.getQty());
    }

    @Test
    void testEmptySKU() {
        Returns emptySKUReturn = new Returns("R001", "", 5, ReturnReasons.DAMAGED, "2024-01-01", "2024-12-31");
        
        quarantineQueue.addReturn(emptySKUReturn);
        
        Returns retrieved = quarantineQueue.getNextForInspection();
        assertNotNull(retrieved);
        assertEquals("", retrieved.getSku());
    }

    @Test
    void testCompleteIntegration() {
        // Add various items with different characteristics
        Returns return1 = new Returns("R001", "SKU001", 10, ReturnReasons.DAMAGED, "2024-01-01", "2024-12-31");
        Returns return2 = new Returns("R002", "SKU002", 5, ReturnReasons.REMORSE, "2024-01-02", "2024-12-31");
        Returns return3 = new Returns("R003", "SKU003", 3, ReturnReasons.EXPIRED, "2024-01-01", "2024-12-31");
        Returns return4 = new Returns("R004", "SKU004", 7, ReturnReasons.CYCLE_COUNT, "2024-01-03", "2024-12-31");
        
        // Add all items to the queue
        quarantineQueue.addReturn(return1);
        quarantineQueue.addReturn(return2);
        quarantineQueue.addReturn(return3);
        quarantineQueue.addReturn(return4);

        // Checks ordering by timestamp (most recent first) and returnID (ascending)
        assertEquals("R004", quarantineQueue.getNextForInspection().getReturnID());
        assertEquals("R002", quarantineQueue.getNextForInspection().getReturnID());
        assertEquals("R001", quarantineQueue.getNextForInspection().getReturnID());
        assertEquals("R003", quarantineQueue.getNextForInspection().getReturnID());
        
        // Queue must be empty
        assertNull(quarantineQueue.getNextForInspection());
    }

    @Test
    void testConstructorWithInitialList() {
        Returns return1 = new Returns("R001", "SKU001", 5, ReturnReasons.DAMAGED, "2024-01-01", "2024-12-31");
        Returns return2 = new Returns("R002", "SKU002", 3, ReturnReasons.REMORSE, "2024-01-02", "2024-12-31");
        
        List<Returns> initialList = new ArrayList<>();
        initialList.add(return1);
        initialList.add(return2);
        
        QuarantineQueue queueWithInitialList = new QuarantineQueue(initialList);

        // Must process in the correct order
        assertEquals("R002", queueWithInitialList.getNextForInspection().getReturnID());
        assertEquals("R001", queueWithInitialList.getNextForInspection().getReturnID());
        assertNull(queueWithInitialList.getNextForInspection());
    }

    @Test
    void testConstructorWithEmptyList() {
        List<Returns> emptyList = new ArrayList<>();
        QuarantineQueue queueWithEmptyList = new QuarantineQueue(emptyList);
        
        assertNull(queueWithEmptyList.getNextForInspection());

        // Must be able to add items after initialization
        Returns returnItem = new Returns("R001", "SKU001", 5, ReturnReasons.DAMAGED, "2024-01-01", "2024-12-31");
        queueWithEmptyList.addReturn(returnItem);
        
        assertNotNull(queueWithEmptyList.getNextForInspection());
    }
}
