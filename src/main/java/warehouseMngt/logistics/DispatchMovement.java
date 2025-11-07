package warehouseMngt.logistics;

import warehouseMngt.warehouse.Movement;

public class DispatchMovement extends Movement {
    public DispatchMovement(String boxID, String sku, int quantity, String from, String to) {
        super(boxID, sku, quantity, from, to);
    }

    @Override
    public MovementType getType() {
        return MovementType.DISPATCH;
    }
}
