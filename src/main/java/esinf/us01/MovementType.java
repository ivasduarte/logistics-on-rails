package esinf.us01;

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