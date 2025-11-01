package domain.warehouse;

import domain.logistics.*;
import domain.order.*;
import domain.returns.Returns;
import domain.warehouse.Bay;
import domain.warehouse.Box;
import domain.warehouse.Item;

import java.time.LocalDateTime;
import java.util.*;

/**
 * This class manages storage, loading, unloading, dispatching, and relocating boxes
 * in a warehouse environment following FEFO/FIFO rules.
 */
public class Warehouse {
    private Map<Wagon, Set<Box>> wagons;
    private Map<String, Item> items;
    private Map<Order, Set<OrderLines>> order_lines;
    private Map<String, Order> orders;
    private Map<String, Returns> returns;
    private Map<String, Set<Bay>> warehouses;

    private MovementHistory movementHistory = new MovementHistory();
    private List<String> importErrors = new ArrayList<>();

    private static final Comparator<Box> FEFO_FIFO_COMPARATOR = (box1, box2) -> {
        LocalDateTime exp1 = box1.getExpiryDate();
        LocalDateTime exp2 = box2.getExpiryDate();

        // Boxes without expiry dates go to the end
        if (exp1 == null && exp2 == null) {
            int comp = box1.getReceivedAt().compareTo(box2.getReceivedAt());
            return comp != 0 ? comp : box1.getBoxID().compareTo(box2.getBoxID());
        }

        if (exp1 == null) return 1;
        if (exp2 == null) return -1;

        // Compare by expiration date, then ID
        int comp = exp1.compareTo(exp2);
        if (comp != 0) return comp;

        comp = box1.getReceivedAt().compareTo(box2.getReceivedAt());
        return comp != 0 ? comp : box1.getBoxID().compareTo(box2.getBoxID());
    };


    public Warehouse(Map<Wagon, Set<Box>> wagons,
                     Map<String, Item> items,
                     Map<Order, Set<OrderLines>> order_lines,
                     Map<String, Order> orders,
                     Map<String, Returns> returns,
                     Map<String, Set<Bay>> warehouses) {
        this.wagons = wagons;
        this.items = items;
        this.order_lines = order_lines;
        this.orders = orders;
        this.returns = returns;
        this.warehouses = warehouses;
    }

    // Getters
    public List<Movement> getMovementHistory() {
        return movementHistory.getAll();
    }

    public List<String> getImportErrors() {
        return importErrors;
    }

    public void unloadWagonsIntoBays() {
        for (Map.Entry<Wagon, Set<Box>> entry : wagons.entrySet()) {
            Wagon wagon = entry.getKey();
            Set<Box> boxes = entry.getValue();

            for (Box box : boxes) {
                boolean placed = false;

                for (Map.Entry<String, Set<Bay>> warehouseEntry : warehouses.entrySet()) {
                    String warehouseId = warehouseEntry.getKey();
                    Set<Bay> bays = warehouseEntry.getValue();

                    List<Bay> sortedBays = new ArrayList<>(bays);
                    sortedBays.sort(Comparator.comparingInt(Bay::getAisle)
                            .thenComparingInt(Bay::getBayNumber));

                    for (Bay bay : sortedBays) {
                        if (bay.hasCapacity()) {
                            bay.addBoxInFEFOOrder(box, FEFO_FIFO_COMPARATOR);
                            box.setLocation(warehouseId, bay.getAisle(), bay.getBayNumber());

                            movementHistory.add(new UnloadMovement(
                                    box.getBoxID(),
                                    box.getSku(),
                                    box.getQuantity(),
                                    "wagon:" + wagon.getWagonID(),
                                    warehouseId + ":" + bay.getAisle() + "/" + bay.getBayNumber()
                            ));

                            placed = true;
                            break;
                        }
                    }
                    if (placed) break;
                }

                if (!placed) {
                    importErrors.add("No available bay capacity for box " + box.getBoxID());
                }
            }
        }
        wagons.replaceAll((w, b) -> new HashSet<>());
    }

    /**
     * Inserts a box into the appropriate bay in the warehouse, ensuring that the
     * bay has space and follows the proper FEFO/FIFO order rules. If no suitable
     * bay is available, logs an error.
     *
     * @param box The box to be inserted into a bay. Must not be null.
     * @throws IllegalArgumentException if the provided box is null.
     */
    public void insertBox(Box box) {
        if (box == null) throw new IllegalArgumentException("Box cannot be null");

        Bay targetBay = findBay(box.getSku());
        if (targetBay == null) targetBay = findAlternativeBay(box.getSku());

        if (targetBay == null) {
            importErrors.add(String.format("Warehouse is full. Not possible to insert box %s", box.getBoxID()));
            return;
        }

        String warehouseID = findWarehouseOfBay(targetBay);

        if (!targetBay.addBoxInFEFOOrder(box, FEFO_FIFO_COMPARATOR)) {
            importErrors.add(String.format("Bay is full. Not possible to insert box %s", box.getBoxID()));
            return;
        }

        box.setLocation(warehouseID, targetBay.getAisle(), targetBay.getBayNumber());

        movementHistory.add(new ReturnMovement(
                box.getBoxID(),
                box.getSku(),
                box.getQuantity(),
                "returns",
                warehouseID + ":" + targetBay.getAisle() + "/" + targetBay.getBayNumber()
        ));
    }

    /**
     * Finds the best bay for a SKU that already exists in the warehouse.
     * Searches for a bay that already has that SKU and still has capacity.
     *
     * @param sku The product's SKU.
     * @return The best bay for the SKU, or null if no suitable bay was found.
     */
    public Bay findBay(String sku) {
        for (Set<Bay> bays : warehouses.values()) {
            for (Bay bay : bays) {
                if (bay.hasCapacity() && bay.getBoxes().stream().anyMatch(box -> box.getSku().equals(sku))) {
                    return bay;
                }
            }
        }
        return null;
    }

    /**
     * Finds an alternative bay for a new SKU.
     *
     * @param sku The product's SKU.
     * @return An empty bay or null if one isn't found.
     */
    public Bay findAlternativeBay(String sku) {
        for (Set<Bay> bays : warehouses.values()) {
            for (Bay bay : bays) {
                if (bay.getBoxes().isEmpty() && bay.hasCapacity()) {
                    return bay;
                }
            }
        }
        return null;
    }

    /**
     * Finds the warehouse of a specific bay.
     */
    private String findWarehouseOfBay(Bay bay) {
        for (Map.Entry<String, Set<Bay>> entry : warehouses.entrySet()) {
            if (entry.getValue().contains(bay)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Dispatches a specified quantity of a product (identified by its SKU)
     * from the available stock across one or more warehouses. The dispatch
     * process attempts to fulfill the quantity from the specified warehouse,
     * if provided, or searches across all warehouses. Stock is consumed
     * from bays in a FEFO/FIFO order until the quantity is fully dispatched
     * or all available stock is depleted.
     *
     * @param sku         the product's SKU to be dispatched.
     * @param quantity    the quantity of the product to be dispatched.
     * @param warehouseId the ID of the specific warehouse to search for stock
     *                    (optional). If null, searches across all available warehouses.
     * @return true if the requested quantity of the product has been fully
     * dispatched, false if the available stock is insufficient.
     */
    public boolean dispatch(String sku, int quantity, String warehouseId) {
        int remaining = quantity;

        List<String> searchList = (warehouseId != null && warehouses.containsKey(warehouseId))
                ? List.of(warehouseId)
                : new ArrayList<>(warehouses.keySet());

        for (String whId : searchList) {
            List<Bay> sortedBays = new ArrayList<>(warehouses.get(whId));
            sortedBays.sort(Comparator.comparingInt(Bay::getAisle)
                    .thenComparingInt(Bay::getBayNumber));

            for (Bay bay : sortedBays) {
                remaining = bay.dispatchFromFront(sku, remaining, movementHistory,
                        whId + ":" + bay.getAisle() + "/" + bay.getBayNumber(), null);

                if (remaining == 0) return true;
            }
        }
        return remaining == 0;
    }


    public boolean relocate(String boxId, String newWarehouseId, int newAisle, int newBayNumber) {
        Box boxToMove = null;
        Bay oldBay = null;
        String oldWarehouse = null;

        for (Map.Entry<String, Set<Bay>> entry : warehouses.entrySet()) {
            for (Bay bay : entry.getValue()) {
                Box removed = bay.removeBox(boxId);
                if (removed != null) {
                    boxToMove = removed;
                    oldBay = bay;
                    oldWarehouse = entry.getKey();
                    break;
                }
            }
            if (boxToMove != null) break;
        }

        if (boxToMove == null) {
            importErrors.add("Box " + boxId + " not found for relocation");
            return false;
        }

        Set<Bay> targetBays = warehouses.get(newWarehouseId);
        if (targetBays == null) {
            importErrors.add("Warehouse " + newWarehouseId + " not found");
            oldBay.addBoxInFEFOOrder(boxToMove, FEFO_FIFO_COMPARATOR);
            return false;
        }

        Bay targetBay = targetBays.stream()
                .filter(b -> b.getAisle() == newAisle && b.getBayNumber() == newBayNumber)
                .findFirst().orElse(null);

        if (targetBay == null || !targetBay.hasCapacity()) {
            importErrors.add("Invalid or full target bay");
            oldBay.addBoxInFEFOOrder(boxToMove, FEFO_FIFO_COMPARATOR);
            return false;
        }

        targetBay.addBoxInFEFOOrder(boxToMove, FEFO_FIFO_COMPARATOR);
        boxToMove.setLocation(newWarehouseId, newAisle, newBayNumber);

        movementHistory.add(new RelocateMovement(
                boxToMove.getBoxID(),
                boxToMove.getSku(),
                boxToMove.getQuantity(),
                oldWarehouse + ":" + oldBay.getAisle() + "/" + oldBay.getBayNumber(),
                newWarehouseId + ":" + newAisle + "/" + newBayNumber
        ));

        return true;
    }

    /**
     * Prints the complete movement history to console.
     */
    public void printMovementHistory() {
        movementHistory.print();
    }

    /**
     * Exports movement history to CSV format.
     *
     * @return CSV-formatted string containing movement history.
     */
    public String exportToCSV() {
        return movementHistory.exportToCSV();
    }

    /**
     * Allocates boxes for an order identified by its id.
     * Iterates order lines in ascending order, trying to allocate required quantities, from available boxes.
     * User a temporary per-box reservation map to avoid double allocation within the same order.
     * For each line a line result is created and its status os computed.
     *
     * @param orderId    the id of the order to allocate
     * @param strictMode if True, applies the strict mode when computing line status
     * @return allocation results per line in the order
     */
    public OrderAllocationResult allocateOrder(String orderId, boolean strictMode) {
        OrderAllocationResult result = new OrderAllocationResult(orderId);

        Order order = orders.get(orderId);
        if (order == null) {
            return result;
        }

        Set<OrderLines> lines = order_lines.get(order);
        if (lines == null || lines.isEmpty()) {
            return result;
        }

        List<OrderLines> sortedLines = new ArrayList<>(lines);
        sortedLines.sort(Comparator.comparingInt(OrderLines::getLineNo));

        Map<String, Integer> boxReservations = new HashMap<>();

        for (OrderLines line : sortedLines) {
            OrderLineResult lineResult = new OrderLineResult(
                    line.getSku(),
                    line.getLineNo(),
                    line.getQty()
            );

            int remainingQty = line.getQty();

            List<BoxLocation> availableBoxes = collectBoxesForSku(line.getSku());

            for (BoxLocation boxLoc : availableBoxes) {
                if (remainingQty <= 0) {
                    break; // Line fully satisfied
                }

                Box box = boxLoc.box;
                Bay bay = boxLoc.bay;
                String warehouseId = boxLoc.warehouseId;

                int reservedQty = boxReservations.getOrDefault(box.getBoxID(), 0);
                int availableQty = box.getQuantity() - reservedQty;

                if (availableQty <= 0) {
                    continue;
                }

                int allocateQty = Math.min(remainingQty, availableQty);

                Allocation allocation = new Allocation(
                        orderId,
                        box.getBoxID(),
                        warehouseId,
                        line.getSku(),
                        line.getLineNo(),
                        bay.getAisle(),
                        bay.getBayNumber(),
                        allocateQty
                );

                lineResult.addAllocation(allocation);
                boxReservations.put(box.getBoxID(), reservedQty + allocateQty);
                remainingQty -= allocateQty;
            }

            lineResult.computeStatus(strictMode);
            result.addLineResult(lineResult);
        }
        return result;
    }

    /**
     * Used to represent the location of a box within a bay and warehouse.
     * <p>
     * Holds references to the box, the bay where it is stored and the warehouse ID.
     * Compares different boxes to ensure FEFO/FIFO ordering when placed in sorted collections.
     */
    private static class BoxLocation implements Comparable<BoxLocation> {
        Box box;
        Bay bay;
        String warehouseId;

        BoxLocation(Box box, Bay bay, String warehouseId) {
            this.box = box;
            this.bay = bay;
            this.warehouseId = warehouseId;
        }

        @Override
        public int compareTo(BoxLocation other) {
            return FEFO_FIFO_COMPARATOR.compare(this.box, other.box);
        }
    }

    /**
     * Collects all boxes matching the given SKU across all warehouses and bays.
     * <p>
     * Scans each warehouse and its bays, creating BoxLocation entries for matching boxes.
     * The resulting list is sorted in FEFO/FIFO order based on box properties.
     *
     * @param sku the SKU to search for
     * @return a sorted list of BoxLocation instances with matching boxes
     */
    private List<BoxLocation> collectBoxesForSku(String sku) {
        List<BoxLocation> boxLocations = new ArrayList<>();

        for (Map.Entry<String, Set<Bay>> warehouseEntry : warehouses.entrySet()) {
            String warehouseId = warehouseEntry.getKey();
            Set<Bay> bays = warehouseEntry.getValue();

            for (Bay bay : bays) {
                for (Box box : bay.getBoxes()) {
                    if (box.getSku().equals(sku)) {
                        boxLocations.add(new BoxLocation(box, bay, warehouseId));
                    }
                }
            }
        }

        boxLocations.sort(Comparator.comparing(bl -> bl.box, FEFO_FIFO_COMPARATOR));

        return boxLocations;
    }
}