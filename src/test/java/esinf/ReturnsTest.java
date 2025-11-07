package esinf;

import org.junit.jupiter.api.Test;
import warehouseMngt.returns.ReturnReasons;
import warehouseMngt.returns.Returns;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Returns class.
 * Tests the functionality of return item management, including getters, setters,
 * business logic methods, and object comparison methods.
 */
public class ReturnsTest {

    @Test
    void testConstructor() {
        // Test constructor with all parameters
        Returns returns = new Returns(
            "RET001", 
            "SKU123", 
            5, 
            ReturnReasons.REMORSE,
            "2024-01-15T10:30:00", 
            "2024-12-31"
        );

        // Verify all fields are set correctly
        assertEquals("RET001", returns.getReturnID());
        assertEquals("SKU123", returns.getSku());
        assertEquals(5, returns.getQty());
        assertEquals(ReturnReasons.REMORSE, returns.getReason());
        assertEquals("2024-01-15T10:30:00", returns.getTimestamp());
        assertEquals("2024-12-31", returns.getExpiryDate());
        // No public getter for discarded flag; cannot assert its value directly
    }

    @Test
    void testConstructorWithNullValues() {
        // Test constructor with null values
        Returns returns = new Returns(
            null, 
            null, 
            0, 
            null, 
            null, 
            null
        );

        // Verify null values are handled
        assertNull(returns.getReturnID());
        assertNull(returns.getSku());
        assertEquals(0, returns.getQty());
        assertNull(returns.getReason());
        assertNull(returns.getTimestamp());
        assertNull(returns.getExpiryDate());
    }

    @Test
    void testConstructorWithEmptyStrings() {
        // Test constructor with empty strings
        Returns returns = new Returns(
            "", 
            "", 
            0, 
            ReturnReasons.REMORSE, 
            "", 
            ""
        );

        // Verify empty strings are handled
        assertEquals("", returns.getReturnID());
        assertEquals("", returns.getSku());
        assertEquals("", returns.getTimestamp());
        assertEquals("", returns.getExpiryDate());
    }

    @Test
    void testGetReturnID() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        assertEquals("RET001", returns.getReturnID());
    }

    @Test
    void testSetReturnID() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        // Test setting new return ID
        returns.setReturnID("RET002");
        assertEquals("RET002", returns.getReturnID());
        
        // Test setting null return ID
        returns.setReturnID(null);
        assertNull(returns.getReturnID());
        
        // Test setting empty return ID
        returns.setReturnID("");
        assertEquals("", returns.getReturnID());
    }

    @Test
    void testGetSku() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        assertEquals("SKU123", returns.getSku());
    }

    @Test
    void testSetSku() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        // Test setting new SKU
        returns.setSku("SKU456");
        assertEquals("SKU456", returns.getSku());
        
        // Test setting null SKU
        returns.setSku(null);
        assertNull(returns.getSku());
        
        // Test setting empty SKU
        returns.setSku("");
        assertEquals("", returns.getSku());
    }

    @Test
    void testGetQty() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        assertEquals(5, returns.getQty());
    }

    @Test
    void testSetQty() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        // Test setting new quantity
        returns.setQty(10);
        assertEquals(10, returns.getQty());
        
        // Test setting zero quantity
        returns.setQty(0);
        assertEquals(0, returns.getQty());
        
        // Test setting negative quantity
        returns.setQty(-5);
        assertEquals(-5, returns.getQty());
        
        // Test setting maximum integer value
        returns.setQty(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, returns.getQty());
        
        // Test setting minimum integer value
        returns.setQty(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, returns.getQty());
    }

    @Test
    void testGetReason() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        assertEquals(ReturnReasons.REMORSE, returns.getReason());
    }

    @Test
    void testSetReason() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        // Test setting different reasons
        returns.setReason(ReturnReasons.DAMAGED);
        assertEquals(ReturnReasons.DAMAGED, returns.getReason());
        
        returns.setReason(ReturnReasons.EXPIRED);
        assertEquals(ReturnReasons.EXPIRED, returns.getReason());
        
        returns.setReason(ReturnReasons.CYCLE_COUNT);
        assertEquals(ReturnReasons.CYCLE_COUNT, returns.getReason());
        
        // Test setting null reason
        returns.setReason(null);
        assertNull(returns.getReason());
    }

    @Test
    void testGetTimestamp() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        assertEquals("2024-01-15T10:30:00", returns.getTimestamp());
    }

    @Test
    void testSetTimestamp() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        // Test setting new timestamp
        returns.setTimestamp("2024-01-16T11:00:00");
        assertEquals("2024-01-16T11:00:00", returns.getTimestamp());
        
        // Test setting null timestamp
        returns.setTimestamp(null);
        assertNull(returns.getTimestamp());
        
        // Test setting empty timestamp
        returns.setTimestamp("");
        assertEquals("", returns.getTimestamp());
    }

    @Test
    void testGetExpiryDate() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        assertEquals("2024-12-31", returns.getExpiryDate());
    }

    @Test
    void testSetExpiryDate() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        // Test setting new expiry date
        returns.setExpiryDate("2025-01-01");
        assertEquals("2025-01-01", returns.getExpiryDate());
        
        // Test setting null expiry date
        returns.setExpiryDate(null);
        assertNull(returns.getExpiryDate());
        
        // Test setting empty expiry date
        returns.setExpiryDate("");
        assertEquals("", returns.getExpiryDate());
    }

    @Test
    void testSetDiscarded() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        // Test that setting a discarded flag does not throw
        assertDoesNotThrow(() -> returns.setDiscarded(true));
        assertDoesNotThrow(() -> returns.setDiscarded(false));
    }

    @Test
    void testIsExpiredWithPastDate() {
        // Test with expired date (past date)
        String expiredDate = LocalDate.now().minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE);
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", expiredDate);
        
        assertTrue(returns.isExpired());
    }

    @Test
    void testIsExpiredWithFutureDate() {
        // Test with future date (not expired)
        String futureDate = LocalDate.now().plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE);
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", futureDate);
        
        assertFalse(returns.isExpired());
    }

    @Test
    void testIsExpiredWithTodayDate() {
        // Test with today's date (not expired)
        String todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", todayDate);
        
        assertFalse(returns.isExpired());
    }

    @Test
    void testIsExpiredWithNullDate() {
        // Test with null expiry date
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", null);
        
        // Should throw exception when trying to parse null date
        assertThrows(NullPointerException.class, () -> returns.isExpired());
    }

    @Test
    void testIsExpiredWithEmptyDate() {
        // Test with empty expiry date
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "");
        
        // Should throw exception when trying to parse empty date
        assertThrows(Exception.class, () -> returns.isExpired());
    }

    @Test
    void testIsExpiredWithInvalidDate() {
        // Test with invalid date format
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "invalid-date");
        
        // Should throw exception when trying to parse invalid date
        assertThrows(Exception.class, () -> returns.isExpired());
    }

    @Test
    void testIsDamagedWithDamagedReason() {
        // Test with DAMAGED reason
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.DAMAGED, "2024-01-15T10:30:00", "2024-12-31");
        
        assertTrue(returns.isDamaged());
    }

    @Test
    void testIsDamagedWithOtherReasons() {
        // Test with other reasons (should return false)
        Returns remorseReturn = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        assertFalse(remorseReturn.isDamaged());
        
        Returns expiredReturn = new Returns("RET002", "SKU456", 3, ReturnReasons.EXPIRED, "2024-01-16T11:00:00", "2024-01-10");
        assertFalse(expiredReturn.isDamaged());
        
        Returns cycleCountReturn = new Returns("RET003", "SKU789", 2, ReturnReasons.CYCLE_COUNT, "2024-01-17T12:00:00", "2024-12-31");
        assertFalse(cycleCountReturn.isDamaged());
    }

    @Test
    void testIsCustomerRemorseWithRemorseReason() {
        // Test with REMORSE reason
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        assertTrue(returns.isCustomerRemorse());
    }

    @Test
    void testIsCustomerRemorseWithOtherReasons() {
        // Test with other reasons (should return false)
        Returns damagedReturn = new Returns("RET001", "SKU123", 5, ReturnReasons.DAMAGED, "2024-01-15T10:30:00", "2024-12-31");
        assertFalse(damagedReturn.isCustomerRemorse());
        
        Returns expiredReturn = new Returns("RET002", "SKU456", 3, ReturnReasons.EXPIRED, "2024-01-16T11:00:00", "2024-01-10");
        assertFalse(expiredReturn.isCustomerRemorse());
        
        Returns cycleCountReturn = new Returns("RET003", "SKU789", 2, ReturnReasons.CYCLE_COUNT, "2024-01-17T12:00:00", "2024-12-31");
        assertFalse(cycleCountReturn.isCustomerRemorse());
    }

    @Test
    void testIsCycleCountWithCycleCountReason() {
        // Test with CYCLE_COUNT reason
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.CYCLE_COUNT, "2024-01-15T10:30:00", "2024-12-31");
        
        assertTrue(returns.isCycleCount());
    }

    @Test
    void testIsCycleCountWithOtherReasons() {
        // Test with other reasons (should return false)
        Returns remorseReturn = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        assertFalse(remorseReturn.isCycleCount());
        
        Returns damagedReturn = new Returns("RET002", "SKU456", 3, ReturnReasons.DAMAGED, "2024-01-16T11:00:00", "2024-12-31");
        assertFalse(damagedReturn.isCycleCount());
        
        Returns expiredReturn = new Returns("RET003", "SKU789", 2, ReturnReasons.EXPIRED, "2024-01-17T12:00:00", "2024-01-10");
        assertFalse(expiredReturn.isCycleCount());
    }

    @Test
    void testEqualsWithSameObject() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        // Test with same object reference
        assertTrue(returns.equals(returns));
    }

    @Test
    void testEqualsWithSameReturnID() {
        Returns returns1 = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        Returns returns2 = new Returns("RET001", "SKU456", 10, ReturnReasons.DAMAGED, "2024-01-16T11:00:00", "2024-01-10");
        
        // Should be equal because they have the same return ID
        assertTrue(returns1.equals(returns2));
        assertTrue(returns2.equals(returns1));
    }

    @Test
    void testEqualsWithDifferentReturnID() {
        Returns returns1 = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        Returns returns2 = new Returns("RET002", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        // Should not be equal because they have different return IDs
        assertFalse(returns1.equals(returns2));
        assertFalse(returns2.equals(returns1));
    }

    @Test
    void testEqualsWithNull() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        // Should not be equal to null
        assertFalse(returns.equals(null));
    }

    @Test
    void testEqualsWithDifferentClass() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        String otherObject = "not a Returns object";
        
        // Should not be equal to different class
        assertFalse(returns.equals(otherObject));
    }

    @Test
    void testEqualsWithNullReturnID() {
        Returns returns1 = new Returns(null, "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        Returns returns2 = new Returns(null, "SKU456", 10, ReturnReasons.DAMAGED, "2024-01-16T11:00:00", "2024-01-10");
        
        // Should be equal because both have null return IDs
        assertTrue(returns1.equals(returns2));
        assertTrue(returns2.equals(returns1));
    }

    @Test
    void testEqualsWithOneNullReturnID() {
        Returns returns1 = new Returns(null, "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        Returns returns2 = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        // Should not be equal because one has null return ID and other doesn't
        assertFalse(returns1.equals(returns2));
        assertFalse(returns2.equals(returns1));
    }

    @Test
    void testHashCode() {
        Returns returns1 = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        Returns returns2 = new Returns("RET001", "SKU456", 10, ReturnReasons.DAMAGED, "2024-01-16T11:00:00", "2024-01-10");
        
        // Should have same hash code because they have the same return ID
        assertEquals(returns1.hashCode(), returns2.hashCode());
    }

    @Test
    void testHashCodeWithDifferentReturnID() {
        Returns returns1 = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        Returns returns2 = new Returns("RET002", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        // Should have different hash codes because they have different return IDs
        assertNotEquals(returns1.hashCode(), returns2.hashCode());
    }

    @Test
    void testHashCodeWithNullReturnID() {
        Returns returns1 = new Returns(null, "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        Returns returns2 = new Returns(null, "SKU456", 10, ReturnReasons.DAMAGED, "2024-01-16T11:00:00", "2024-01-10");
        
        // Should have same hash code because both have null return IDs
        assertEquals(returns1.hashCode(), returns2.hashCode());
    }

    @Test
    void testToString() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        
        String expected = "Returns{" +
                "returnID='RET001'" +
                ", sku='SKU123'" +
                ", qty=5" +
                ", reason='REMORSE'" +
                ", timestamp='2024-01-15T10:30:00'" +
                ", expiryDate='2024-12-31'" +
                '}';
        
        assertEquals(expected, returns.toString());
    }

    @Test
    void testToStringWithNullValues() {
        Returns returns = new Returns(null, null, 0, null, null, null);
        
        String expected = "Returns{" +
                "returnID='null'" +
                ", sku='null'" +
                ", qty=0" +
                ", reason='null'" +
                ", timestamp='null'" +
                ", expiryDate='null'" +
                '}';
        
        assertEquals(expected, returns.toString());
    }

    @Test
    void testToStringWithEmptyStrings() {
        Returns returns = new Returns("", "", 0, ReturnReasons.REMORSE, "", "");
        
        String expected = "Returns{" +
                "returnID=''" +
                ", sku=''" +
                ", qty=0" +
                ", reason='REMORSE'" +
                ", timestamp=''" +
                ", expiryDate=''" +
                '}';
        
        assertEquals(expected, returns.toString());
    }

    @Test
    void testToStringWithSpecialCharacters() {
        Returns returns = new Returns("RET-001", "SKU@123", 5, ReturnReasons.DAMAGED, "2024-01-15T10:30:00", "2024-12-31");
        
        String expected = "Returns{" +
                "returnID='RET-001'" +
                ", sku='SKU@123'" +
                ", qty=5" +
                ", reason='DAMAGED'" +
                ", timestamp='2024-01-15T10:30:00'" +
                ", expiryDate='2024-12-31'" +
                '}';
        
        assertEquals(expected, returns.toString());
    }

    @Test
    void testToStringWithNegativeQuantity() {
        Returns returns = new Returns("RET001", "SKU123", -5, ReturnReasons.EXPIRED, "2024-01-15T10:30:00", "2024-12-31");
        
        String expected = "Returns{" +
                "returnID='RET001'" +
                ", sku='SKU123'" +
                ", qty=-5" +
                ", reason='EXPIRED'" +
                ", timestamp='2024-01-15T10:30:00'" +
                ", expiryDate='2024-12-31'" +
                '}';
        
        assertEquals(expected, returns.toString());
    }

    @Test
    void testToStringWithLargeQuantity() {
        Returns returns = new Returns("RET001", "SKU123", Integer.MAX_VALUE, ReturnReasons.CYCLE_COUNT, "2024-01-15T10:30:00", "2024-12-31");
        
        String expected = "Returns{" +
                "returnID='RET001'" +
                ", sku='SKU123'" +
                ", qty=" + Integer.MAX_VALUE +
                ", reason='CYCLE_COUNT'" +
                ", timestamp='2024-01-15T10:30:00'" +
                ", expiryDate='2024-12-31'" +
                '}';
        
        assertEquals(expected, returns.toString());
    }

    @Test
    void testBusinessLogicMethodsWithAllReasons() {
        // Test all business logic methods with each reason type
        Returns[] returns = {
            new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31"),
            new Returns("RET002", "SKU456", 3, ReturnReasons.DAMAGED, "2024-01-16T11:00:00", "2024-12-31"),
            new Returns("RET003", "SKU789", 2, ReturnReasons.EXPIRED, "2024-01-17T12:00:00", "2024-01-10"),
            new Returns("RET004", "SKU101", 1, ReturnReasons.CYCLE_COUNT, "2024-01-18T13:00:00", "2024-12-31")
        };

        // Test REMORSE reason
        assertTrue(returns[0].isCustomerRemorse());
        assertFalse(returns[0].isDamaged());
        assertFalse(returns[0].isCycleCount());

        // Test DAMAGED reason
        assertFalse(returns[1].isCustomerRemorse());
        assertTrue(returns[1].isDamaged());
        assertFalse(returns[1].isCycleCount());

        // Test EXPIRED reason
        assertFalse(returns[2].isCustomerRemorse());
        assertFalse(returns[2].isDamaged());
        assertFalse(returns[2].isCycleCount());

        // Test CYCLE_COUNT reason
        assertFalse(returns[3].isCustomerRemorse());
        assertFalse(returns[3].isDamaged());
        assertTrue(returns[3].isCycleCount());
    }

    @Test
    void testEdgeCasesWithExtremeValues() {
        // Test with extreme values
        Returns extremeReturns = new Returns(
            "RET" + "0".repeat(100), // Very long return ID
            "SKU" + "A".repeat(50), // Very long SKU
            Integer.MAX_VALUE, // Maximum quantity
            ReturnReasons.REMORSE,
            "2024-01-15T10:30:00",
            "2024-12-31"
        );

        // Verify all fields are set correctly
        assertEquals("RET" + "0".repeat(100), extremeReturns.getReturnID());
        assertEquals("SKU" + "A".repeat(50), extremeReturns.getSku());
        assertEquals(Integer.MAX_VALUE, extremeReturns.getQty());
        assertEquals(ReturnReasons.REMORSE, extremeReturns.getReason());
        assertEquals("2024-01-15T10:30:00", extremeReturns.getTimestamp());
        assertEquals("2024-12-31", extremeReturns.getExpiryDate());
    }

    @Test
    void testEdgeCasesWithMinimumValues() {
        // Test with minimum values
        Returns minimumReturns = new Returns(
            "A", // Single character return ID
            "B", // Single character SKU
            Integer.MIN_VALUE, // Minimum quantity
            ReturnReasons.DAMAGED,
            "2024-01-15T10:30:00",
            "2024-12-31"
        );

        // Verify all fields are set correctly
        assertEquals("A", minimumReturns.getReturnID());
        assertEquals("B", minimumReturns.getSku());
        assertEquals(Integer.MIN_VALUE, minimumReturns.getQty());
        assertEquals(ReturnReasons.DAMAGED, minimumReturns.getReason());
        assertEquals("2024-01-15T10:30:00", minimumReturns.getTimestamp());
        assertEquals("2024-12-31", minimumReturns.getExpiryDate());
    }
}
