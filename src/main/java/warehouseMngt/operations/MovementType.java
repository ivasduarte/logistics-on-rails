package warehouseMngt.operations;

/**
 * The MovementType enum defines various types of movements
 * that can occur for items within a warehouse management system.
 * Each movement type is associated with a description providing
 * additional context about its purpose.
 */
public enum MovementType{
    UNLOAD("Unload from wagon"),
    DISPATCH("Dispatch to order"),
    RELOCATE("Relocate between bays"),
    RETURN("Return to warehouse");

    private final String description;

    MovementType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}