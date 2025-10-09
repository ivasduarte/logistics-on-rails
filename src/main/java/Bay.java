import java.util.ArrayList;
import java.util.List;
/*
 * Represents a warehouse's bay.
 * Saves a list of Boxes in FEFO/FIFO order.
 * Uses recursive methods to:
 *      1. Insert a box in the correct position.
 *      2. Search for a box ID.
 *      3. Print the boxes in the bay.
 *      4. Sum quantities.
 *      5. Remove empty boxes.
 */
public class Bay {
    private String warehouseID;
    private int bay;
    private int aisle;
    private int boxCapacity;
    private List<Box> boxes;

    /*
     * Constructor.
     * @param warehouseID Warehouse identifier.
     * @param bay Bay number.
     * @param aisle Number of the aisle.
     * @param boxCapacity Maximum capacity (number of boxes).
     */
    public Bay(String warehouseID, int bay, int aisle, int boxCapacity, List<Box> boxes) {
        this.warehouseID = warehouseID;
        this.bay = bay;
        this.aisle = aisle;
        this.boxCapacity = boxCapacity;
        this.boxes = new ArrayList<>();
    }

    public String getWarehouseID() { return warehouseID; }

    public void setWarehouseID(String warehouseID) { this.warehouseID = warehouseID; }

    public int getBay() { return bay; }

    public void setBay(int bay) { this.bay = bay; }

    public int getAisle() { return aisle; }

    public void setAisle(int aisle) { this.aisle = aisle; }

    public int getBoxCapacity() { return boxCapacity; }

    public void setBoxCapacity(int boxCapacity) { this.boxCapacity = boxCapacity; }

    public List<Box> getBoxes() { return boxes; }

    public void setBoxes(List<Box> boxes) { this.boxes = boxes; }

    public boolean hasCapacity() { return boxes.size() < boxCapacity; }

    /*
     * Adds a box to the bay, keeping FEFO/FIFO order.
     * @param box Box to be added (can't be null).
     * @throws IllegalStateException if the bay is full.
     * @throws IllegalArgumentException if box is null.
     */
    public void addBox(Box box) {
        if(!hasCapacity()) throw new IllegalStateException("The bay is full");
        if(box == null) throw new IllegalArgumentException("Box cannot be null");
        insertBox(box, 0);
    }

    /*
     * Inserts a box in the correct position.
     * Base case: box is inserted at the end of the list.
     * Recursive case: box is inserted for i+1.
     */
    public void insertBox(Box box, int i) {
        if(i>=boxes.size()) { boxes.add(box); return; }
        Box currentBox = boxes.get(i);
        if(box.compareTo(currentBox) < 0) {
            boxes.add(i, box);
            return;
        }
        insertBox(box, i+1);
    }

    /*
     * Searches for and returns a box with the given ID, or null if not found.
     */
    public Box searchBox(String boxID, int i) {
        if(boxID == null) return null;
        if(i >= boxes.size()) return null;
        Box currentBox = boxes.get(i);
        if(boxID.equals(currentBox.getBoxID())) return currentBox;
        return searchBox(boxID, i+1);
    }

    /*
     * Removes a box with the given ID.
     * @returns true if the box was found and removed, false otherwise.
     */
    public boolean removeBox(String boxID, int i) {
        if(boxID == null) return false;
        if(i >= boxes.size()) return false;
        Box currentBox = boxes.get(i);
        if(boxID.equals(currentBox.getBoxID())) {
            boxes.remove(i);
            return true;
        }
        return removeBox(boxID, i+1);
    }

    /*
     * Prints the boxes in the bay, in FEFO/FIFO order.
     */
    public void printBoxes(int i) {
        if (i >= boxes.size()) return;
        System.out.println("  " + boxes.get(i));
        printBoxes(i + 1);
    }

    /*
     * Sums the quantities of all boxes in the bay.
     */
    public int totalQuantity(int i){
        if(i>=boxes.size()) return 0;
        return boxes.get(i).getQuantity() + totalQuantity(i + 1);
    }

    /*
     * Removes all empty boxes from the bay.
     */
    public int removeEmptyBoxes(int i){
        if(i>=boxes.size()) return 0;
        if(boxes.get(i).getQuantity() == 0){
            boxes.remove(i);
            return 1 + removeEmptyBoxes(1);
        }
        return removeEmptyBoxes(i + 1);
    }

    @Override
    public String toString() {
        return "Bay{" +
                "warehouseID='" + warehouseID + '\'' +
                ", bay=" + bay +
                ", aisle=" + aisle +
                ", boxCapacity=" + boxCapacity +
                ", boxes=" + boxes +
                '}';
    }
}
