package warehouseMngt.operations;

public class DispatchMovement extends Movement {
    public DispatchMovement(String boxID, String sku, int quantity, String from, String to) {
        super(boxID, sku, quantity, from, to);
    }

    @Override
    public MovementType getType() {
        return MovementType.DISPATCH;
    }
}
