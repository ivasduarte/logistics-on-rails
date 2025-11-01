package domain.returns;

import java.util.Locale;

/**
 * Enum representing all valid reasons for product returns.
 * Each reason has specific business rules that determine whether
 * the returned item should be discarded or restocked.
 *
 * Business Rules:
 * - REMORSE: Customer didn't want the product → Can be restocked
 * - DAMAGED: Product is physically broken → Must be discarded
 * - EXPIRED: Product passed expiry date → Must be discarded
 * - CYCLE_COUNT: Found during inventory audit → Can be restocked
 */
public enum ReturnReasons {
    REMORSE("Client didn't want the product"),
    DAMAGED("Product is damaged"),
    EXPIRED("Product is expired"),
    CYCLE_COUNT("Product was found during an audit");

    private final String description;

    /**
     * Constructor for enum values.
     * @param description Human-readable explanation of the return reason
     */
    ReturnReasons(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Determines if items with this return reason should be discarded.
     *
     * Business Logic:
     * - DAMAGED items are unusable → discard
     * - EXPIRED items are unsafe → discard
     * - All other reasons mean the item is still good → restock
     *
     * @return true if items should be discarded (DAMAGED or EXPIRED)
     */
    public boolean shouldBeDiscarded() {
        return this == DAMAGED || this == EXPIRED;
    }

    /**
     * Determines if items with this return reason can be restocked.
     * This is the inverse of shouldBeDiscarded().
     *
     * @return true if items can safely return to inventory (REMORSE or CYCLE_COUNT)
     */
    public boolean shouldBeRestocked() {
        return this == REMORSE || this == CYCLE_COUNT;
    }

    /**
     * Parses a string into a ReturnReasons enum value.
     * Handles various input formats and common aliases to improve robustness.
     *
     * Steps:
     * 1. Validates input (not null/blank)
     * 2. Normalizes to uppercase and replaces spaces/hyphens with underscores
     * 3. Tries direct enum match
     * 4. Falls back to common aliases (e.g., "CUSTOMER_REMORSE" → REMORSE)
     * 5. Throws exception if no match found
     *
     * @param raw String to parse (case-insensitive)
     * @return Matching ReturnReasons enum value
     * @throws IllegalArgumentException if input is null, blank, or unrecognized
     */
    public static ReturnReasons parse(String raw) {
        // Step 1: Validate input
        if (raw == null) throw new IllegalArgumentException("Reason cannot be null");
        String normalized = raw.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException("Reason cannot be blank");

        // Step 2: Normalize a format (uppercase, replace spaces/hyphens)
        String upper = normalized.toUpperCase(Locale.ROOT);

        // Step 3: Try direct match to enum names
        try {
            return ReturnReasons.valueOf(upper.replace(' ', '_').replace('-', '_'));
        } catch (IllegalArgumentException ignored) {
            // Not a direct match, try aliases
        }

        // Step 4: Map common aliases to enum values
        switch (upper.replace(' ', '_').replace('-', '_')) {
            case "CUSTOMER_REMORSE":
            case "REMORSE":
            case "CLIENT_REMORSE":
                return REMORSE;
            case "DAMAGED":
            case "DAMAGE":
                return DAMAGED;
            case "EXPIRED":
            case "EXPIRATION":
                return EXPIRED;
            case "CYCLE_COUNT":
            case "CYCLECOUNT":
            case "AUDIT":
                return CYCLE_COUNT;
            default:
                // Step 5: No match found
                throw new IllegalArgumentException("Unknown return reason: " + raw);
        }
    }
}