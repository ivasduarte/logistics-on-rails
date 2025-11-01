package domain.returns;

import domain.logistics.StockAction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles audit logging for all return inspection activities.
 *
 * Purpose:
 * - Creates permanent, immutable record of all quality control actions
 * - Enables compliance auditing and traceability
 * - Supports root cause analysis for quality issues
 */
public class AuditLog {
    // Directory path where an audit log file will be stored
    private String LOG_FILE_PATH = "audit-log-file/";

    // Name of the audit log file
    private String LOG_FILE_NAME = "audit-log.txt";

    /**
     * Creates an AuditLog instance that writes to the specified directory.
     *
     * Note: The directory must exist before logging; this class does not create it.
     * File will be created automatically on first writing if it doesn't exist.
     *
     * @param LOG_FILE_PATH Directory path where the audit log file will be stored
     */
    public AuditLog(String LOG_FILE_PATH) {
        this.LOG_FILE_PATH = LOG_FILE_PATH;
    }

    /**
     * Logs an inspection action to the audit trail.
     *
     * Process:
     * 1. Generate a timestamp for the log entry (current time)
     * 2. Format timestamp to human-readable string
     * 3. Build a log entry string with all inspection details
     * 4. Append to log file (creates a file if it doesn't exist)
     * 5. Handle partial operations (when some units are restocked, some are discarded)
     *
     * File Handling:
     * - Opens a file in APPEND mode (preserves existing entries)
     * - Uses BufferedWriter for efficient I/O
     * - Uses PrintWriter for convenient println() formatting
     * - Try-with-resources ensures the file is closed even if an error occurs
     *
     * Error Handling:
     * - IOException caught and printed to stderr
     * - Does not throw exception - logging failure should not crash inspection
     *
     * @param returnID Unique identifier for the return being inspected
     * @param sku Stock Keeping Unit - identifies the product type
     * @param quantity Total quantity of items in the return
     * @param qtyRestocked Number of units returned to inventory (0 if all discarded)
     * @param qtyDiscarded Number of units removed from circulation (0 if all restocked)
     * @param action Final action taken: RESTOCKED or DISCARDED
     */
    public void logInspection(String returnID, String sku, int quantity,
                              int qtyRestocked, int qtyDiscarded, StockAction action) {

        // Try-with-resources: Automatically closes all resources when done
        // Even if an exception occurs, resources are guaranteed to be closed
        try (FileWriter writer = new FileWriter(LOG_FILE_PATH + LOG_FILE_NAME, true); // true = append mode
             BufferedWriter bw = new BufferedWriter(writer);  // Buffering for performance
             PrintWriter out = new PrintWriter(bw)) {         // Convenient println() method

            // Step 1: Get the current timestamp
            LocalDateTime timestamp = LocalDateTime.now();

            // Step 2: Format timestamp for human readability
            // Pattern: yyyy-MM-dd HH:mm (e.g., "2025-10-25 14:30")
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            // Step 3: Build log entry using StringBuilder for efficiency
            // (StringBuilder is faster than string concatenation for multiple operations)
            StringBuilder sb = new StringBuilder();

            // Add required fields
            sb.append(timestamp.format(formatter))           // When action occurred
                    .append(" | returnID = ").append(returnID)    // Which return
                    .append(" | SKU = ").append(sku)              // Which product
                    .append(" | action = ").append(action)        // What was done
                    .append(" | quantity = ").append(quantity);   // How many units

            // Step 4: Add optional breakdown for partial operations
            // Example: If 10 units returned, but only 7 restocked and 3 discarded,
            // This happens when some units are good and some are damaged in the same return
            if (qtyRestocked != 0 || qtyDiscarded != 0) {
                sb.append(" | qtyRestocked = ").append(qtyRestocked)
                        .append(" | qtyDiscarded = ").append(qtyDiscarded);
            }

            // Step 5: Write log entry to file
            // println() adds newline automatically - each entry on its own line
            out.println(sb);

        } catch (IOException e) {
            // Step 6: Handle file write errors gracefully
            // Print error but don't crash the inspection process
            // In production: use proper logging framework, alerts, or retry logic
            System.err.println("\nError writing to audit log file: " + e.getMessage());
        }
    }
}