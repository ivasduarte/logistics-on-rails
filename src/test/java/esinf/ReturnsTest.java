package esinf;

import org.junit.jupiter.api.Test;
import warehouseMngt.returns.ReturnReasons;
import warehouseMngt.returns.Returns;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Returns class.
 * Each test creates complete expected results and compares them with actual results in single assertions.
 */
public class ReturnsTest {

    @Test
    void testConstructorAndGetters() {
        // Input
        String returnID = "RET001";
        String sku = "SKU123";
        int qty = 5;
        ReturnReasons reason = ReturnReasons.REMORSE;
        String timestamp = "2024-01-15T10:30:00";
        String expiryDate = "2024-12-31";

        // Expected
        Object[] expected = new Object[] {
                returnID,
                sku,
                qty,
                reason,
                timestamp,
                expiryDate
        };

        // Execute
        Returns returns = new Returns(returnID, sku, qty, reason, timestamp, expiryDate);

        // Result
        Object[] result = new Object[] {
                returns.getReturnID(),
                returns.getSku(),
                returns.getQty(),
                returns.getReason(),
                returns.getTimestamp(),
                returns.getExpiryDate()
        };

        // Assert - single comparison
        assertArrayEquals(expected, result);
    }

    @Test
    void testConstructorWithNullValues() {
        // Expected
        Object[] expected = new Object[] {
                null,  // returnID
                null,  // sku
                0,     // qty
                null,  // reason
                null,  // timestamp
                null   // expiryDate
        };

        // Execute
        Returns returns = new Returns(null, null, 0, null, null, null);

        // Result
        Object[] result = new Object[] {
                returns.getReturnID(),
                returns.getSku(),
                returns.getQty(),
                returns.getReason(),
                returns.getTimestamp(),
                returns.getExpiryDate()
        };

        // Assert - single comparison
        assertArrayEquals(expected, result);
    }

    @Test
    void testConstructorWithEmptyStrings() {
        // Expected
        Object[] expected = new Object[] {
                "",                    // returnID
                "",                    // sku
                0,                     // qty
                ReturnReasons.REMORSE, // reason
                "",                    // timestamp
                ""                     // expiryDate
        };

        // Execute
        Returns returns = new Returns("", "", 0, ReturnReasons.REMORSE, "", "");

        // Result
        Object[] result = new Object[] {
                returns.getReturnID(),
                returns.getSku(),
                returns.getQty(),
                returns.getReason(),
                returns.getTimestamp(),
                returns.getExpiryDate()
        };

        // Assert - single comparison
        assertArrayEquals(expected, result);
    }

    @Test
    void testSetReturnID() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");

        // Expected after multiple sets
        Object[] expected = new Object[] {
                "RET002",
                null,
                ""
        };

        // Execute
        returns.setReturnID("RET002");
        String result1 = returns.getReturnID();

        returns.setReturnID(null);
        String result2 = returns.getReturnID();

        returns.setReturnID("");
        String result3 = returns.getReturnID();

        Object[] result = new Object[] {
                result1,
                result2,
                result3
        };

        // Assert - single comparison
        assertArrayEquals(expected, result);
    }

    @Test
    void testSetSku() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");

        // Expected after multiple sets
        Object[] expected = new Object[] {
                "SKU456",
                null,
                ""
        };

        // Execute
        returns.setSku("SKU456");
        String result1 = returns.getSku();

        returns.setSku(null);
        String result2 = returns.getSku();

        returns.setSku("");
        String result3 = returns.getSku();

        Object[] result = new Object[] {
                result1,
                result2,
                result3
        };

        // Assert - single comparison
        assertArrayEquals(expected, result);
    }

    @Test
    void testSetQty() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");

        // Expected after multiple sets
        Object[] expected = new Object[] {
                10,
                0,
                -5,
                Integer.MAX_VALUE,
                Integer.MIN_VALUE
        };

        // Execute
        returns.setQty(10);
        int result1 = returns.getQty();

        returns.setQty(0);
        int result2 = returns.getQty();

        returns.setQty(-5);
        int result3 = returns.getQty();

        returns.setQty(Integer.MAX_VALUE);
        int result4 = returns.getQty();

        returns.setQty(Integer.MIN_VALUE);
        int result5 = returns.getQty();

        Object[] result = new Object[] {
                result1,
                result2,
                result3,
                result4,
                result5
        };

        // Assert - single comparison
        assertArrayEquals(expected, result);
    }

    @Test
    void testSetReason() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");

        // Expected after multiple sets
        Object[] expected = new Object[] {
                ReturnReasons.DAMAGED,
                ReturnReasons.EXPIRED,
                ReturnReasons.CYCLE_COUNT,
                null
        };

        // Execute
        returns.setReason(ReturnReasons.DAMAGED);
        ReturnReasons result1 = returns.getReason();

        returns.setReason(ReturnReasons.EXPIRED);
        ReturnReasons result2 = returns.getReason();

        returns.setReason(ReturnReasons.CYCLE_COUNT);
        ReturnReasons result3 = returns.getReason();

        returns.setReason(null);
        ReturnReasons result4 = returns.getReason();

        Object[] result = new Object[] {
                result1,
                result2,
                result3,
                result4
        };

        // Assert - single comparison
        assertArrayEquals(expected, result);
    }

    @Test
    void testSetTimestamp() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");

        // Expected after multiple sets
        Object[] expected = new Object[] {
                "2024-01-16T11:00:00",
                null,
                ""
        };

        // Execute
        returns.setTimestamp("2024-01-16T11:00:00");
        String result1 = returns.getTimestamp();

        returns.setTimestamp(null);
        String result2 = returns.getTimestamp();

        returns.setTimestamp("");
        String result3 = returns.getTimestamp();

        Object[] result = new Object[] {
                result1,
                result2,
                result3
        };

        // Assert - single comparison
        assertArrayEquals(expected, result);
    }

    @Test
    void testSetExpiryDate() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");

        // Expected after multiple sets
        Object[] expected = new Object[] {
                "2025-01-01",
                null,
                ""
        };

        // Execute
        returns.setExpiryDate("2025-01-01");
        String result1 = returns.getExpiryDate();

        returns.setExpiryDate(null);
        String result2 = returns.getExpiryDate();

        returns.setExpiryDate("");
        String result3 = returns.getExpiryDate();

        Object[] result = new Object[] {
                result1,
                result2,
                result3
        };

        // Assert - single comparison
        assertArrayEquals(expected, result);
    }

    @Test
    void testSetDiscarded() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");

        // Test that setting discarded flag does not throw
        assertDoesNotThrow(() -> {
            returns.setDiscarded(true);
            returns.setDiscarded(false);
        });
    }

    @Test
    void testIsExpiredWithPastDate() {
        String expiredDate = LocalDate.now().minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE);
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", expiredDate);

        boolean expected = true;
        boolean result = returns.isExpired();

        assertEquals(expected, result);
    }

    @Test
    void testIsExpiredWithFutureDate() {
        String futureDate = LocalDate.now().plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE);
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", futureDate);

        boolean expected = false;
        boolean result = returns.isExpired();

        assertEquals(expected, result);
    }

    @Test
    void testIsExpiredWithTodayDate() {
        String todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", todayDate);

        boolean expected = false;
        boolean result = returns.isExpired();

        assertEquals(expected, result);
    }

    @Test
    void testIsExpiredWithNullDate() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", null);

        assertThrows(NullPointerException.class, () -> returns.isExpired());
    }

    @Test
    void testIsExpiredWithEmptyDate() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "");

        assertThrows(Exception.class, () -> returns.isExpired());
    }

    @Test
    void testIsExpiredWithInvalidDate() {
        Returns returns = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "invalid-date");

        assertThrows(Exception.class, () -> returns.isExpired());
    }

    @Test
    void testEqualsAndHashCode() {
        Returns returns1 = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        Returns returns2 = new Returns("RET001", "SKU456", 10, ReturnReasons.DAMAGED, "2024-01-16T11:00:00", "2024-01-10");
        Returns returns3 = new Returns("RET002", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");

        // Expected: same returnID = equal objects with same hashCode
        Object[] expected = new Object[] {
                true,   // returns1.equals(returns1)
                true,   // returns1.equals(returns2)
                true,   // returns2.equals(returns1)
                false,  // returns1.equals(returns3)
                false,  // returns1.equals(null)
                false,  // returns1.equals("string")
                true    // returns1.hashCode() == returns2.hashCode()
        };

        Object[] result = new Object[] {
                returns1.equals(returns1),
                returns1.equals(returns2),
                returns2.equals(returns1),
                returns1.equals(returns3),
                returns1.equals(null),
                returns1.equals("not a Returns object"),
                returns1.hashCode() == returns2.hashCode()
        };

        assertArrayEquals(expected, result);
    }

    @Test
    void testEqualsWithNullReturnID() {
        Returns returns1 = new Returns(null, "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        Returns returns2 = new Returns(null, "SKU456", 10, ReturnReasons.DAMAGED, "2024-01-16T11:00:00", "2024-01-10");
        Returns returns3 = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");

        // Expected: both null returnIDs = equal, one null = not equal
        Object[] expected = new Object[] {
                true,   // returns1.equals(returns2) - both null
                true,   // returns2.equals(returns1) - both null
                false,  // returns1.equals(returns3) - one null
                false,  // returns3.equals(returns1) - one null
                true    // returns1.hashCode() == returns2.hashCode()
        };

        Object[] result = new Object[] {
                returns1.equals(returns2),
                returns2.equals(returns1),
                returns1.equals(returns3),
                returns3.equals(returns1),
                returns1.hashCode() == returns2.hashCode()
        };

        assertArrayEquals(expected, result);
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

        String result = returns.toString();

        assertEquals(expected, result);
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

        String result = returns.toString();

        assertEquals(expected, result);
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

        String result = returns.toString();

        assertEquals(expected, result);
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

        String result = returns.toString();

        assertEquals(expected, result);
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

        String result = returns.toString();

        assertEquals(expected, result);
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

        String result = returns.toString();

        assertEquals(expected, result);
    }

    @Test
    void testEdgeCasesWithExtremeValues() {
        String longReturnID = "RET" + "0".repeat(100);
        String longSKU = "SKU" + "A".repeat(50);

        // Expected
        Object[] expected = new Object[] {
                longReturnID,
                longSKU,
                Integer.MAX_VALUE,
                ReturnReasons.REMORSE,
                "2024-01-15T10:30:00",
                "2024-12-31"
        };

        // Execute
        Returns returns = new Returns(
                longReturnID,
                longSKU,
                Integer.MAX_VALUE,
                ReturnReasons.REMORSE,
                "2024-01-15T10:30:00",
                "2024-12-31"
        );

        // Result
        Object[] result = new Object[] {
                returns.getReturnID(),
                returns.getSku(),
                returns.getQty(),
                returns.getReason(),
                returns.getTimestamp(),
                returns.getExpiryDate()
        };

        assertArrayEquals(expected, result);
    }

    @Test
    void testEdgeCasesWithMinimumValues() {
        // Expected
        Object[] expected = new Object[] {
                "A",
                "B",
                Integer.MIN_VALUE,
                ReturnReasons.DAMAGED,
                "2024-01-15T10:30:00",
                "2024-12-31"
        };

        // Execute
        Returns returns = new Returns(
                "A",
                "B",
                Integer.MIN_VALUE,
                ReturnReasons.DAMAGED,
                "2024-01-15T10:30:00",
                "2024-12-31"
        );

        // Result
        Object[] result = new Object[] {
                returns.getReturnID(),
                returns.getSku(),
                returns.getQty(),
                returns.getReason(),
                returns.getTimestamp(),
                returns.getExpiryDate()
        };

        assertArrayEquals(expected, result);
    }

    @Test
    void testCompleteScenario_AllReasons() {
        // Test creation and toString for all return reasons
        Returns returns1 = new Returns("RET001", "SKU123", 5, ReturnReasons.REMORSE, "2024-01-15T10:30:00", "2024-12-31");
        Returns returns2 = new Returns("RET002", "SKU456", 3, ReturnReasons.DAMAGED, "2024-01-16T11:00:00", "2024-12-31");
        Returns returns3 = new Returns("RET003", "SKU789", 2, ReturnReasons.EXPIRED, "2024-01-17T12:00:00", "2024-01-10");
        Returns returns4 = new Returns("RET004", "SKU101", 1, ReturnReasons.CYCLE_COUNT, "2024-01-18T13:00:00", "2024-12-31");

        Object[] expected = new Object[] {
                ReturnReasons.REMORSE,
                ReturnReasons.DAMAGED,
                ReturnReasons.EXPIRED,
                ReturnReasons.CYCLE_COUNT
        };

        Object[] result = new Object[] {
                returns1.getReason(),
                returns2.getReason(),
                returns3.getReason(),
                returns4.getReason()
        };

        assertArrayEquals(expected, result);
    }
}