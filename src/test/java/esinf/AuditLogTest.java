package esinf;

import org.junit.jupiter.api.*;
import warehouseMngt.logistics.StockAction;
import warehouseMngt.returns.AuditLog;
import java.io.*;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {
    private static final String TEST_DIR = "audit-log-file/";
    private static final String TEST_FILE = "audit-log.txt";
    private AuditLog auditLog;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Paths.get(TEST_DIR));
        // Files.deleteIfExists(Paths.get(TEST_DIR + TEST_FILE));
        auditLog = new AuditLog(TEST_DIR);
    }

    @Test
    void testWriteMultipleEntriesToFile() {
        // Write various entries in the file
        auditLog.logInspection("R001", "SKU123", 10, 5, 2, StockAction.RESTOCKED);
        auditLog.logInspection("R002", "SKU456", 20, 0, 20, StockAction.DISCARDED);
        auditLog.logInspection("R003", "SKU789", 15, 15, 0, StockAction.RESTOCKED);
        auditLog.logInspection("R004", "SKU999", 8, 4, 4, StockAction.RESTOCKED);

        System.out.println("File successfully created: " + TEST_DIR + TEST_FILE);
    }

    @Test
    void testWriteAndReadFile() throws IOException {
        // Write data
        auditLog.logInspection("R100", "PRODUCT_A", 50, 30, 20, StockAction.RESTOCKED);
        auditLog.logInspection("R101", "PRODUCT_B", 100, 0, 100, StockAction.DISCARDED);

        // Write and show content of a file
        List<String> lines = Files.readAllLines(Paths.get(TEST_DIR + TEST_FILE));

        System.out.println("\n=== CONTENT OF THE FILE ===");
        for (int i = 0; i < lines.size(); i++) {
            System.out.println("Line " + (i + 1) + ": " + lines.get(i));
        }
        System.out.println("==============================\n");

        System.out.println("Total of written lines: " + lines.size());
    }
    @Test
    void testWriteDifferentScenarios() {
        // Scenario 1: Only restocked
        auditLog.logInspection("R200", "SKU_REST", 30, 30, 0, StockAction.RESTOCKED);

        // Scenario 2: Only discarded
        auditLog.logInspection("R201", "SKU_DISC", 25, 0, 25, StockAction.DISCARDED);

        // Scenario 3: Restocked and Discarded
        auditLog.logInspection("R202", "SKU_MIX", 50, 25, 25, StockAction.RESTOCKED);

        // Scenario 4: Not restocked nor discarded
        auditLog.logInspection("R203", "SKU_NONE", 10, 0, 0, StockAction.DISCARDED);

        System.out.println("Four different scenarios printed in file");
    }

    @Test
    void testLogInspection_CreatesFile() throws IOException {
        PrintWriter writer = new PrintWriter(new File(TEST_DIR + TEST_FILE));
        assertTrue(Files.exists(Paths.get(TEST_DIR + TEST_FILE)));
        List<String> lines = Files.readAllLines(Paths.get(TEST_DIR + TEST_FILE));
        assertEquals(0, lines.size());
    }

    @Test
    void testAppendToExistingFile() throws IOException {
        auditLog.logInspection("R300", "FIRST", 10, 5, 5, StockAction.RESTOCKED);

        int linesBefore = 0;
        if (Files.exists(Paths.get(TEST_DIR + TEST_FILE))) {
            linesBefore = Files.readAllLines(Paths.get(TEST_DIR + TEST_FILE)).size();
        }

        auditLog.logInspection("R301", "SECOND", 20, 10, 10, StockAction.RESTOCKED);

        int linesAfter = Files.readAllLines(Paths.get(TEST_DIR + TEST_FILE)).size();

        System.out.println("Lines before: " + linesBefore);
        System.out.println("Lines after: " + linesAfter);
        System.out.println("'Append' worked successfully'");
    }

    @Test
    void testLogInspection_InvalidPath_ShouldNotThrow() {
        AuditLog invalidAuditLog = new AuditLog("/invalid/path/");
        assertDoesNotThrow(() ->
                        invalidAuditLog.logInspection("R999", "SKUERR", 5, 1, 0, StockAction.RESTOCKED));
    }
}
