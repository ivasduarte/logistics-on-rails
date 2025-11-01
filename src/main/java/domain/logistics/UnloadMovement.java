package domain.logistics;

import domain.warehouse.Movement;

public class UnloadMovement extends Movement {
    public UnloadMovement(String boxID, String sku, int quantity, String from, String to) {
        super(boxID, sku, quantity, from, to);
    }
    @Override
    public MovementType getType() { return MovementType.UNLOAD; }
}
