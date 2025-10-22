package esinf.us05;

import java.util.Locale;

public enum ReturnReasons {
    REMORSE("Client didn't want the product"),
    DAMAGED("Product is damaged"),
    EXPIRED("Product is expired"),
    CYCLE_COUNT("Product was found during an audit");

    private final String description;

    ReturnReasons(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean shouldBeDiscarded() {
        return this == DAMAGED || this == EXPIRED;
    }

    public boolean shouldBeRestocked(){
        return this == REMORSE || this == CYCLE_COUNT;
    }

    public static ReturnReasons parse(String raw) {
        if (raw == null) throw new IllegalArgumentException("Reason cannot be null");
        String normalized = raw.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException("Reason cannot be blank");

        String upper = normalized.toUpperCase(Locale.ROOT);
        // Direct matches to enum names
        try {
            return ReturnReasons.valueOf(upper.replace(' ', '_').replace('-', '_'));
        } catch (IllegalArgumentException ignored) { }

        // Common aliases mapping
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
                throw new IllegalArgumentException("Unknown return reason: " + raw);
        }
    }
}
