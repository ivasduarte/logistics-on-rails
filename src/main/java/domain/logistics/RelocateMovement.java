package domain.logistics;

import domain.warehouse.Movement;

public class RelocateMovement extends Movement {
    public RelocateMovement(String boxID, String sku, int quantity, String from, String to) {
        super(boxID, sku, quantity, from, to);
    }

    @Override
    public MovementType getType() {
        return MovementType.RELOCATE;
    }
}
