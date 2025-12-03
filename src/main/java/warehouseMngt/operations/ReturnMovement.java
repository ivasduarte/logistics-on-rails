package warehouseMngt.operations;

public class ReturnMovement extends Movement {
    public ReturnMovement(String boxID, String sku, int quantity, String from, String to) {
        super(boxID, sku, quantity, from, to);
    }
    @Override
    public MovementType getType() { return MovementType.RETURN; }
}
