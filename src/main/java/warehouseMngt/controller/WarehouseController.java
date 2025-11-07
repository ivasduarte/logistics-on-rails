package warehouseMngt.controller;

import warehouseMngt.logistics.*;
import warehouseMngt.order.*;
import warehouseMngt.warehouse.Bay;
import warehouseMngt.warehouse.Box;
import warehouseMngt.warehouse.Movement;
import warehouseMngt.warehouse.Warehouse;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller for managing warehouse operations (loading, unloading, dispatching, relocating, etc.)
 * based on the data in the Warehouse domain class.
 */
public class WarehouseController {
    private final Warehouse warehouse;

    private static final Comparator<Box> FEFO_FIFO_COMPARATOR = (box1, box2) -> {
        LocalDateTime exp1 = box1.getExpiryDate();
        LocalDateTime exp2 = box2.getExpiryDate();

        if (exp1 == null && exp2 == null) {
            int comp = box1.getReceivedAt().compareTo(box2.getReceivedAt());
            return comp != 0 ? comp : box1.getBoxID().compareTo(box2.getBoxID());
        }
        if (exp1 == null) return 1;
        if (exp2 == null) return -1;

        int comp = exp1.compareTo(exp2);
        if (comp != 0) return comp;
        comp = box1.getReceivedAt().compareTo(box2.getReceivedAt());
        return comp != 0 ? comp : box1.getBoxID().compareTo(box2.getBoxID());
    };

    public WarehouseController(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public List<Movement> getMovementHistory() {
        return warehouse.getMovementHistoryObject().getAll();
    }

    public List<String> getImportErrors() {
        return warehouse.getImportErrors();
    }

    public void unloadWagonsIntoBays() {
        Map<Wagon, Set<Box>> wagons = warehouse.getWagons();
        Map<String, Set<Bay>> warehouses = warehouse.getWarehouses();
        List<String> importErrors = warehouse.getImportErrors();
        MovementHistory movementHistory = warehouse.getMovementHistoryObject();

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
                if (!placed) importErrors.add("No available bay capacity for box " + box.getBoxID());
            }
        }
        wagons.replaceAll((w, b) -> new HashSet<>());
    }

    /**
     * Inserts a box into the appropriate bay in the warehouse, ensuring that the
     * bay has space and follows the proper FEFO/FIFO order rules. If no suitable
     * bay is available, logs an error.
     */
    public void insertBox(Box box) {
        if (box == null) throw new IllegalArgumentException("Box cannot be null");

        Map<String, Set<Bay>> warehouses = warehouse.getWarehouses();
        List<String> importErrors = warehouse.getImportErrors();
        MovementHistory movementHistory = warehouse.getMovementHistoryObject();

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

    public Bay findBay(String sku) {
        for (Set<Bay> bays : warehouse.getWarehouses().values()) {
            for (Bay bay : bays) {
                if (bay.hasCapacity() && bay.getBoxes().stream().anyMatch(box -> box.getSku().equals(sku))) {
                    return bay;
                }
            }
        }
        return null;
    }

    public Bay findAlternativeBay(String sku) {
        for (Set<Bay> bays : warehouse.getWarehouses().values()) {
            for (Bay bay : bays) {
                if (bay.getBoxes().isEmpty() && bay.hasCapacity()) {
                    return bay;
                }
            }
        }
        return null;
    }

    private String findWarehouseOfBay(Bay bay) {
        for (Map.Entry<String, Set<Bay>> entry : warehouse.getWarehouses().entrySet()) {
            if (entry.getValue().contains(bay)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean dispatch(String sku, int quantity, String warehouseId) {
        Map<String, Set<Bay>> warehouses = warehouse.getWarehouses();
        MovementHistory movementHistory = warehouse.getMovementHistoryObject();

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
        Map<String, Set<Bay>> warehouses = warehouse.getWarehouses();
        List<String> importErrors = warehouse.getImportErrors();
        MovementHistory movementHistory = warehouse.getMovementHistoryObject();

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

    public void printMovementHistory() {
        warehouse.getMovementHistoryObject().print();
    }

    public String exportToCSV() {
        return warehouse.getMovementHistoryObject().exportToCSV();
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

        Order order = warehouse.getOrders().get(orderId);
        if (order == null) {
            return result;
        }

        Set<OrderLines> lines = warehouse.getOrderLines().get(order);
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
     * Helper inner class for collecting box locations.
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

    public List<BoxLocation> collectBoxesForSku(String sku) {
        Map<String, Set<Bay>> warehouses = warehouse.getWarehouses();
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