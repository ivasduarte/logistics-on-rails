package esinf.us05;

import esinf.us01.Box;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLog {
    private String LOG_FILE_PATH = "audit-log-file/";
    private String LOG_FILE_NAME = "audit-log.txt";

    public AuditLog(String LOG_FILE_PATH) {
        this.LOG_FILE_PATH = LOG_FILE_PATH;
    }

    public void logInspection(String returnID, String sku, int quantity, int qtyRestocked, int qtyDiscarded, StockAction action){
        try(FileWriter writer = new FileWriter(LOG_FILE_PATH + LOG_FILE_NAME, true);
        BufferedWriter bw = new BufferedWriter(writer);
        PrintWriter out = new PrintWriter(bw)) {

            LocalDateTime timestamp = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            StringBuilder sb = new StringBuilder();
            sb.append(timestamp.format(formatter)).append(" | returnID = ").append(returnID).append(" | SKU = ")
                    .append(sku).append(" | action = ").append(action).append(" | quantity = ").append(quantity);

            if(qtyRestocked != 0 || qtyDiscarded != 0){
                sb.append(" | qtyRestocked = ").append(qtyRestocked).append(" | qtyDiscarded = ").append(qtyDiscarded);
            }
            out.println(sb);
        } catch (IOException e){
            System.err.println("\nError writing to audit log file: " + e.getMessage());
        }
    }
}