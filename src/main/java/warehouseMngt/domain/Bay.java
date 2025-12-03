package warehouseMngt.domain;

import warehouseMngt.operations.DispatchMovement;
import warehouseMngt.operations.MovementHistory;

import java.util.*;

/**
 * Bay represents a storage location in a warehouse aisle.
 * Maintains an ordered list of boxes following FEFO/FIFO rules.
 */
public class Bay {
    private int bayNumber;
    private int aisle;
    private int boxCapacity;
    private LinkedList<Box> boxes; // Ordered list for FEFO/FIFO

    public Bay(int bayNumber, int aisle, int boxCapacity) {
        this.bayNumber = bayNumber;
        this.aisle = aisle;
        this.boxCapacity = boxCapacity;
        this.boxes = new LinkedList<>();
    }

    // Getters
    public int getBayNumber() {
        return bayNumber;
    }
    public int getAisle() {
        return aisle;
    }
    public LinkedList<Box> getBoxes() {
        return boxes;
    }

    /**
     * Checks if bay has capacity for more boxes.
     */
    public boolean hasCapacity() {
        return boxes.size() < boxCapacity;
    }

    /**
     * Adds a box to the bay in the correct FEFO/FIFO position.
     * Uses Box.compareTo() to determine the correct insertion point.
     *
     * @param box the box to add
     * @return true if added successfully, false if no capacity
     */
    public boolean addBoxInFEFOOrder(Box box, Comparator<Box> comparator) {
        if (!hasCapacity()) {
            return false;
        }

        // Add and then sort
        boxes.add(box);
        boxes.sort(comparator);

        return true;
    }

    /**
     * Dispatches items from the front of the bay (FEFO/FIFO).
     * Removes or reduces quantities from boxes matching the given SKU.
     *
     * @param sku the SKU to dispatch
     * @param quantity the quantity needed
     * @return remaining quantity (0 if fully dispatched)
     */
    public int dispatchFromFront(String sku, int quantity) {
        int remaining = quantity;
        Iterator<Box> iterator = boxes.iterator();

        while (iterator.hasNext() && remaining > 0) {
            Box box = iterator.next();

            if (box.getSku().equals(sku)) {
                int available = box.getQuantity();

                if (available <= remaining) {
                    // Consume the entire box
                    remaining -= available;
                    iterator.remove();
                } else {
                    // Partial consumption
                    box.setQuantity(available - remaining);
                    remaining = 0;
                }
            }
        }

        return remaining;
    }

    /**
     * Dispatches items from the front of the bay (FEFO/FIFO).
     * Removes or reduces quantities from boxes matching the given SKU.
     * Records movements for WMS tracking.
     *
     * @param sku the SKU to dispatch
     * @param quantity the quantity needed
     * @param bayLocation the location string for this bay
     * @param orderId optional order ID
     * @return remaining quantity (0 if fully dispatched)
     */
    public int dispatchFromFront(String sku, int quantity, MovementHistory history,
                                 String bayLocation, String orderId) {
        int remaining = quantity;
        Iterator<Box> iterator = boxes.iterator();

        while (iterator.hasNext() && remaining > 0) {
            Box box = iterator.next();

            if (box.getSku().equals(sku)) {
                int available = box.getQuantity();
                int consumed = Math.min(available, remaining);

                // ✅ cria o movimento corretamente
                DispatchMovement movement = new DispatchMovement(
                        box.getBoxID(),
                        sku,
                        consumed,
                        bayLocation,
                        "dispatch"
                );

                if (orderId != null) {
                    movement.setOrderId(orderId);
                }

                history.add(movement);

                if (available <= remaining) {
                    remaining -= available;
                    iterator.remove();
                } else {
                    box.setQuantity(available - remaining);
                    remaining = 0;
                }
            }
        }

        return remaining;
    }


    /**
     * Removes a specific box by ID from the bay.
     *
     * @param boxId the box ID to remove
     * @return the removed box, or null if not found
     */
    public Box removeBox(String boxId) {
        Iterator<Box> iterator = boxes.iterator();
        while (iterator.hasNext()) {
            Box box = iterator.next();
            if (box.getBoxID().equals(boxId)) {
                iterator.remove();
                return box;
            }
        }
        return null;
    }

    /**
     * Finds a box by ID without removing it.
     *
     * @param boxId the box ID to find
     * @return the box, or null if not found
     */
    public Box findBox(String boxId) {
        for (Box box : boxes) {
            if (box.getBoxID().equals(boxId)) {
                return box;
            }
        }
        return null;
    }

    /**
     * Gets total quantity of a specific SKU in this bay.
     *
     * @param sku the SKU to count
     * @return total quantity
     */
    public int getSkuQuantity(String sku) {
        int total = 0;
        for (Box box : boxes) {
            if (box.getSku().equals(sku)) {
                total += box.getQuantity();
            }
        }
        return total;
    }

    /**
     * Returns a string representation of the bay showing its location and contents.
     */
    @Override
    public String toString() {
        return String.format("Bay[aisle=%d, bay=%d, capacity=%d, boxes=%d/%d]",
                aisle, bayNumber, boxCapacity, boxes.size(), boxCapacity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bay bay = (Bay) o;
        return bayNumber == bay.bayNumber && aisle == bay.aisle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bayNumber, aisle);
    }
}