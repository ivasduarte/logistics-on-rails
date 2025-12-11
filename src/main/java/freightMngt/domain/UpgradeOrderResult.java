package freightMngt.domain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Data Transfer Object for USEI11 - Directed Line Upgrade Plan results.
 * Contains either a valid upgrade order or information about detected cycles.
 * This is a simple data holder - formatting logic is in the UI layer.
 */
public class UpgradeOrderResult {

    private final boolean success;
    private final LinkedList<Station> upgradeOrder;
    private final ArrayList<LinkedList<Station>> cycles;
    private final List<Station> stationsInCycles;
    private final String errorMessage;

    /**
     * Private constructor - use factory methods to create instances.
     */
    private UpgradeOrderResult(boolean success,
                               LinkedList<Station> upgradeOrder,
                               ArrayList<LinkedList<Station>> cycles,
                               List<Station> stationsInCycles,
                               String errorMessage) {
        this.success = success;
        this.upgradeOrder = upgradeOrder;
        this.cycles = cycles;
        this.stationsInCycles = stationsInCycles;
        this.errorMessage = errorMessage;
    }

    /**
     * Factory method: Creates a successful result with valid upgrade order.
     */
    public static UpgradeOrderResult success(LinkedList<Station> upgradeOrder) {
        // Removed 'complexity' argument
        return new UpgradeOrderResult(true, upgradeOrder, null, null, null);
    }

    /**
     * Factory method: Creates a failure result with detected cycles.
     */
    public static UpgradeOrderResult withCycles(ArrayList<LinkedList<Station>> cycles,
                                                List<Station> stationsInCycles) {
        return new UpgradeOrderResult(false, null, cycles, stationsInCycles, null);
    }

    /**
     * Factory method: Creates an error result.
     */
    public static UpgradeOrderResult error(String errorMessage) {
        return new UpgradeOrderResult(false, null, null, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public LinkedList<Station> getUpgradeOrder() {
        return upgradeOrder;
    }

    public ArrayList<LinkedList<Station>> getCycles() {
        return cycles;
    }

    public List<Station> getStationsInCycles() {
        return stationsInCycles;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return errorMessage != null;
    }

    public boolean hasCycles() {
        return cycles != null && !cycles.isEmpty();
    }
}