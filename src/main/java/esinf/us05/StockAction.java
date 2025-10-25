package esinf.us05;

/**
 * Enum representing the final action taken on a returned item after inspection.
 * Used for audit logging to track what happened to each return.
 *
 * - DISCARDED: Item was removed from circulation (damaged/expired)
 * - RESTOCKED: Item was returned to inventory (good condition)
 */
public enum StockAction {
    DISCARDED,
    RESTOCKED;
}