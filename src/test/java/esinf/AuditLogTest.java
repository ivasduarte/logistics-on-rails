package esinf;

import esinf.us05.AuditLog;
import esinf.us05.StockAction;
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {
    private static final String TEST_DIR = "test-audit-log/";
    private static final String TEST_FILE = "audit-log.txt";
    private AuditLog auditLog;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Paths.get(TEST_DIR));
        Files.deleteIfExists(Paths.get(TEST_DIR + TEST_FILE));
        auditLog = new AuditLog(TEST_DIR);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_DIR + TEST_FILE));
        Files.deleteIfExists(Paths.get(TEST_DIR));
    }

    @Test
    void testLogInspection_WritesExpectedContent_WithRestockedAndDiscarded() throws IOException {
        auditLog.logInspection("R001", "SKU123", 10, 5, 2, StockAction.RESTOCKED);

        List<String> lines = Files.readAllLines(Paths.get(TEST_DIR + TEST_FILE));
        assertEquals(1, lines.size(), "O ficheiro deve ter uma linha escrita");

        String logLine = lines.get(0);
        assertTrue(logLine.contains("returnID = R001"));
        assertTrue(logLine.contains("SKU = SKU123"));
        assertTrue(logLine.contains("action = RESTOCK"));
        assertTrue(logLine.contains("quantity = 10"));
        assertTrue(logLine.contains("qtyRestocked = 5"));
        assertTrue(logLine.contains("qtyDiscarded = 2"));
    }

    @Test
    void testLogInspection_WritesExpectedContent_WithoutRestockedAndDiscarded() throws IOException {
        auditLog.logInspection("R002", "SKU999", 20, 0, 0, StockAction.DISCARDED);

        List<String> lines = Files.readAllLines(Paths.get(TEST_DIR + TEST_FILE));
        assertEquals(1, lines.size(), "O ficheiro deve ter uma linha escrita");

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
        assertEquals(2, lines.size(), "O ficheiro deve conter duas entradas de log");
        assertTrue(lines.get(0).contains("R100"));
        assertTrue(lines.get(1).contains("R101"));
    }

    @Test
    void testLogInspection_InvalidPath_ShouldNotThrow() {
        AuditLog invalidAuditLog = new AuditLog("/invalid/path/");
        assertDoesNotThrow(() ->
                        invalidAuditLog.logInspection("R999", "SKUERR", 5, 1, 0, StockAction.RESTOCKED),
                "Mesmo com caminho inválido não deve lançar exceção (apenas imprimir erro)");
    }
}
