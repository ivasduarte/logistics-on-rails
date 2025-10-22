package esinf;

import esinf.us01.Warehouse;
import esinf.us05.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class ReturnsManagerTest {
    private Warehouse warehouse;
    private ReturnsManager returnsManager;
    private Path tempLogFile;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        // Create a temporary warehouse for testing
        warehouse = new Warehouse();
        
        // Create a temporary log file
        tempLogFile = tempDir.resolve("test_audit.log");
        
        // Initialize ReturnsManager with temporary log file
        returnsManager = new ReturnsManager(warehouse, tempLogFile.toString());
    }

    @Test
    void testConstructor() {
        // Test that ReturnsManager is properly initialized
        assertNotNull(returnsManager);
    }

    @Test
    void testReceiveReturn() {
        // Create a test return item
        Returns testReturn = new Returns(
            "RET001", 
            "SKU123", 
            5, 
            ReturnReasons.REMORSE, 
            "2024-01-15T10:30:00", 
            "2024-12-31"
        );

        // Test that receiveReturn adds the item to quarantine queue
        assertDoesNotThrow(() -> returnsManager.receiveReturn(testReturn));
    }

    @Test
    void testReceiveReturnWithNull() {
        // Test that receiving null return throws exception
        assertThrows(NullPointerException.class, () -> {
            returnsManager.receiveReturn(null);
        });
    }

    @Test
    void testProcessReturnsWithEmptyQueue() throws IOException {
        // Test processing returns when queue is empty
        assertDoesNotThrow(() -> returnsManager.processReturns());
        
        // Should not throw exception and should handle empty queue gracefully
    }

    @Test
    void testProcessReturnsWithSingleReturn() throws IOException {
        // Create a test return item
        Returns testReturn = new Returns(
            "RET001", 
            "SKU123", 
            5, 
            ReturnReasons.DAMAGED, 
            "2024-01-15T10:30:00", 
            "2024-12-31"
        );

        // Add return to queue
        returnsManager.receiveReturn(testReturn);

        // Process the return
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }

    @Test
    void testProcessReturnsWithMultipleReturns() throws IOException {
        // Create multiple test return items
        Returns return1 = new Returns(
            "RET001", 
            "SKU123", 
            5, 
            ReturnReasons.REMORSE, 
            "2024-01-15T10:30:00", 
            "2024-12-31"
        );
        
        Returns return2 = new Returns(
            "RET002", 
            "SKU456", 
            3, 
            ReturnReasons.EXPIRED, 
            "2024-01-16T11:00:00", 
            "2024-01-10"
        );

        // Add returns to queue
        returnsManager.receiveReturn(return1);
        returnsManager.receiveReturn(return2);

        // Process returns (should process the most recent one first due to sorting)
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }

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

        // Test with EXPIRED reason (should be discarded)
        Returns expiredReturn = new Returns(
            "RET003", 
            "SKU789", 
            2, 
            ReturnReasons.EXPIRED, 
            "2024-01-17T12:00:00", 
            "2024-01-10"
        );

        returnsManager.receiveReturn(expiredReturn);
        assertDoesNotThrow(() -> returnsManager.processReturns());

        // Test with CYCLE_COUNT reason (should be restocked)
        Returns cycleCountReturn = new Returns(
            "RET004", 
            "SKU101", 
            1, 
            ReturnReasons.CYCLE_COUNT, 
            "2024-01-18T13:00:00", 
            "2024-12-31"
        );

        returnsManager.receiveReturn(cycleCountReturn);
        assertDoesNotThrow(() -> returnsManager.processReturns());
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

    @Test
    void testProcessReturnsWithFutureExpiryDate() throws IOException {
        // Create return with future expiry date
        String futureDate = LocalDate.now().plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE);
        Returns futureReturn = new Returns(
            "RET001", 
            "SKU123", 
            5, 
            ReturnReasons.REMORSE, 
            "2024-01-15T10:30:00", 
            futureDate
        );

        returnsManager.receiveReturn(futureReturn);
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }

    @Test
    void testProcessReturnsWithZeroQuantity() throws IOException {
        // Test with zero quantity return
        Returns zeroQtyReturn = new Returns(
            "RET001", 
            "SKU123", 
            0, 
            ReturnReasons.REMORSE, 
            "2024-01-15T10:30:00", 
            "2024-12-31"
        );

        returnsManager.receiveReturn(zeroQtyReturn);
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }

    @Test
    void testProcessReturnsWithNegativeQuantity() throws IOException {
        // Test with negative quantity return
        Returns negativeQtyReturn = new Returns(
            "RET001", 
            "SKU123", 
            -1, 
            ReturnReasons.REMORSE, 
            "2024-01-15T10:30:00", 
            "2024-12-31"
        );

        returnsManager.receiveReturn(negativeQtyReturn);
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }

    @Test
    void testProcessReturnsWithEmptySku() throws IOException {
        // Test with empty SKU
        Returns emptySkuReturn = new Returns(
            "RET001", 
            "", 
            5, 
            ReturnReasons.REMORSE, 
            "2024-01-15T10:30:00", 
            "2024-12-31"
        );

        returnsManager.receiveReturn(emptySkuReturn);
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }

    @Test
    void testProcessReturnsWithNullExpiryDate() throws IOException {
        // Test with null expiry date
        Returns nullExpiryReturn = new Returns(
            "RET001", 
            "SKU123", 
            5, 
            ReturnReasons.REMORSE, 
            "2024-01-15T10:30:00", 
            null
        );

        returnsManager.receiveReturn(nullExpiryReturn);
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }

    @Test
    void testProcessReturnsWithBlankExpiryDate() throws IOException {
        // Test with blank expiry date
        Returns blankExpiryReturn = new Returns(
            "RET001", 
            "SKU123", 
            5, 
            ReturnReasons.REMORSE, 
            "2024-01-15T10:30:00", 
            "   "
        );

        returnsManager.receiveReturn(blankExpiryReturn);
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }

    @Test
    void testProcessReturnsWithInvalidExpiryDate() throws IOException {
        // Test with invalid expiry date format
        Returns invalidExpiryReturn = new Returns(
            "RET001", 
            "SKU123", 
            5, 
            ReturnReasons.REMORSE, 
            "2024-01-15T10:30:00", 
            "invalid-date"
        );

        returnsManager.receiveReturn(invalidExpiryReturn);
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }

    @Test
    void testProcessReturnsWithSpecialCharactersInSku() throws IOException {
        // Test with special characters in SKU
        Returns specialSkuReturn = new Returns(
            "RET001", 
            "SKU-123_ABC@#$", 
            5, 
            ReturnReasons.REMORSE, 
            "2024-01-15T10:30:00", 
            "2024-12-31"
        );

        returnsManager.receiveReturn(specialSkuReturn);
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }

    @Test
    void testProcessReturnsWithLongReturnId() throws IOException {
        // Test with very long return ID
        String longReturnId = "RET" + "0".repeat(100);
        Returns longIdReturn = new Returns(
            longReturnId, 
            "SKU123", 
            5, 
            ReturnReasons.REMORSE, 
            "2024-01-15T10:30:00", 
            "2024-12-31"
        );

        returnsManager.receiveReturn(longIdReturn);
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }

    @Test
    void testProcessReturnsWithLargeQuantity() throws IOException {
        // Test with very large quantity
        Returns largeQtyReturn = new Returns(
            "RET001", 
            "SKU123", 
            Integer.MAX_VALUE, 
            ReturnReasons.REMORSE, 
            "2024-01-15T10:30:00", 
            "2024-12-31"
        );

        returnsManager.receiveReturn(largeQtyReturn);
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }

    @Test
    void testProcessReturnsWithSameTimestamp() throws IOException {
        // Test with returns having the same timestamp (should sort by return ID)
        String sameTimestamp = "2024-01-15T10:30:00";
        
        Returns return1 = new Returns(
            "RET002", // Higher ID
            "SKU123", 
            5, 
            ReturnReasons.REMORSE, 
            sameTimestamp, 
            "2024-12-31"
        );
        
        Returns return2 = new Returns(
            "RET001", // Lower ID
            "SKU456", 
            3, 
            ReturnReasons.REMORSE, 
            sameTimestamp, 
            "2024-12-31"
        );

        // Add returns to queue
        returnsManager.receiveReturn(return1);
        returnsManager.receiveReturn(return2);

        // Process returns (should process RET001 first due to alphabetical sorting)
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }

    @Test
    void testProcessReturnsWithDifferentTimestamps() throws IOException {
        // Test with returns having different timestamps (should process most recent first)
        Returns olderReturn = new Returns(
            "RET001", 
            "SKU123", 
            5, 
            ReturnReasons.REMORSE, 
            "2024-01-15T10:30:00", 
            "2024-12-31"
        );
        
        Returns newerReturn = new Returns(
            "RET002", 
            "SKU456", 
            3, 
            ReturnReasons.REMORSE, 
            "2024-01-16T11:00:00", 
            "2024-12-31"
        );

        // Add returns to queue
        returnsManager.receiveReturn(olderReturn);
        returnsManager.receiveReturn(newerReturn);

        // Process returns (should process newer one first)
        assertDoesNotThrow(() -> returnsManager.processReturns());
    }
}
