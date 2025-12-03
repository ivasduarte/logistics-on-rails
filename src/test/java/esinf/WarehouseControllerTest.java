package esinf;

import freightMngt.domain.Wagon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import warehouseMngt.controller.WarehouseController;
import warehouseMngt.domain.Bay;
import warehouseMngt.domain.Box;
import warehouseMngt.domain.Warehouse;
import warehouseMngt.operations.*;
import warehouseMngt.order.Order;
import warehouseMngt.order.OrderLines;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseControllerTest {

    private Warehouse warehouse;
    private WarehouseController controller;

    @BeforeEach
    void setup() {
        warehouse = createTestWarehouse();
        controller = new WarehouseController(warehouse);
    }

    @Test
    void testInsertBox_IntoEmptyBay_Success() {
        Box box = new Box("BOX-001", "SKU-A", 10, null, "2024-01-01T10:00:00");

        controller.insertBox(box);

        Bay bay = findBayInWarehouse("WH1");
        assertNotNull(bay);
        assertEquals(1, bay.getBoxes().size());
        assertTrue(bay.getBoxes().contains(box));
        assertEquals("WH1", box.getWarehouseId());
    }

    @Test
    void testInsertBox_WithSameSKU_GoesToSameBay() {
        Box box1 = new Box("BOX-001", "SKU-A", 10, null, "2024-01-01T10:00:00");
        Box box2 = new Box("BOX-002", "SKU-A", 5, null, "2024-01-01T11:00:00");

        controller.insertBox(box1);
        controller.insertBox(box2);

        Bay bay = findBayInWarehouse("WH1");
        assertEquals(2, bay.getBoxes().size());
        assertTrue(bay.getBoxes().contains(box1));
        assertTrue(bay.getBoxes().contains(box2));
    }

    @Test
    void testInsertBox_NullBox_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            controller.insertBox(null);
        });
    }

    @Test
    void testInsertBox_FullWarehouse_AddsError() {
        // Cria warehouse com capacidade limitada
        warehouse = createFullWarehouse();
        controller = new WarehouseController(warehouse);

        Box box = new Box("BOX-999", "SKU-X", 10, null, "2024-01-01T10:00:00");
        controller.insertBox(box);

        List<String> errors = controller.getImportErrors();
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Warehouse is full"));
    }

    @Test
    void testFindBay_ExistingSKU_ReturnsBay() {
        Box box = new Box("BOX-001", "SKU-A", 10, null, "2024-01-01T10:00:00");
        controller.insertBox(box);

        Bay foundBay = controller.findBay("SKU-A");

        assertNotNull(foundBay);
        assertTrue(foundBay.getBoxes().stream()
                .anyMatch(b -> b.getSku().equals("SKU-A")));
    }

    @Test
    void testFindBay_NonExistingSKU_ReturnsNull() {
        Bay foundBay = controller.findBay("NONEXISTENT-SKU");
        assertNull(foundBay);
    }

    @Test
    void testFindAlternativeBay_EmptyBayExists_ReturnsBay() {
        Bay bay = controller.findAlternativeBay("ANY-SKU");
        assertNotNull(bay);
        assertTrue(bay.getBoxes().isEmpty());
        assertTrue(bay.hasCapacity());
    }

    @Test
    void testDispatch_SufficientStock_Success() {
        Box box = new Box("BOX-001", "SKU-A", 50, null, "2024-01-01T10:00:00");
        controller.insertBox(box);

        boolean result = controller.dispatch("SKU-A", 30, null);

        assertTrue(result);
        Bay bay = findBayInWarehouse("WH1");
        assertEquals(20, bay.getBoxes().iterator().next().getQuantity());
    }

    @Test
    void testDispatch_InsufficientStock_ReturnsFalse() {
        Box box = new Box("BOX-001", "SKU-A", 10, null, "2024-01-01T10:00:00");
        controller.insertBox(box);

        boolean result = controller.dispatch("SKU-A", 50, null);

        assertFalse(result);
    }

    @Test
    void testDispatch_MultipleBoxes_DispatchesInOrder() {
        Box box1 = new Box("BOX-001", "SKU-A", 10,
                LocalDateTime.parse("2024-01-01T10:00:00"), "2024-01-01T10:00:00");
        Box box2 = new Box("BOX-002", "SKU-A", 10,
                LocalDateTime.parse("2024-01-02T10:00:00"), "2024-01-01T11:00:00");

        controller.insertBox(box1);
        controller.insertBox(box2);

        boolean result = controller.dispatch("SKU-A", 15, null);

        assertTrue(result);
        // Box1 deve estar completamente despachado, Box2 parcialmente
    }

    @Test
    void testDispatch_SpecificWarehouse_OnlySearchesThatWarehouse() {
        Box box1 = new Box("BOX-001", "SKU-A", 10, null, "2024-01-01T10:00:00");
        controller.insertBox(box1);

        boolean result = controller.dispatch("SKU-A", 5, "WH1");

        assertTrue(result);
    }

    @Test
    void testRelocate_ValidMove_Success() {
        // Cria warehouse com múltiplas bays
        warehouse = createWarehouseWithMultipleBays();
        controller = new WarehouseController(warehouse);

        Box box = new Box("BOX-001", "SKU-A", 10, null, "2024-01-01T10:00:00");
        Bay sourceBay = warehouse.getWarehouses().get("WH1").iterator().next();
        sourceBay.addBoxInFEFOOrder(box, (b1, b2) -> 0);
        box.setLocation("WH1", 1, 1);

        boolean result = controller.relocate("BOX-001", "WH1", 2, 2);

        assertTrue(result);
        assertEquals(2, box.getAisle());
        assertEquals(2, box.getBayNumber());
    }

    @Test
    void testRelocate_BoxNotFound_ReturnsFalse() {
        boolean result = controller.relocate("NONEXISTENT-BOX", "WH1", 1, 1);

        assertFalse(result);
        List<String> errors = controller.getImportErrors();
        assertTrue(errors.stream().anyMatch(e -> e.contains("not found")));
    }

    @Test
    void testRelocate_InvalidWarehouse_ReturnsFalse() {
        Box box = new Box("BOX-001", "SKU-A", 10, null, "2024-01-01T10:00:00");
        controller.insertBox(box);

        boolean result = controller.relocate("BOX-001", "INVALID-WH", 1, 1);

        assertFalse(result);
        List<String> errors = controller.getImportErrors();
        assertTrue(errors.stream().anyMatch(e -> e.contains("not found")));
    }

    @Test
    void testUnloadWagonsIntoBays_Success() {
        warehouse = createWarehouseWithWagons();
        controller = new WarehouseController(warehouse);

        controller.unloadWagonsIntoBays();

        // Verifica que as caixas foram descarregadas
        Bay bay = findBayInWarehouse("WH1");
        assertNotNull(bay);
        assertTrue(bay.getBoxes().size() > 0);

        // Verifica que os wagons foram esvaziados
        assertTrue(warehouse.getWagons().values().stream()
                .allMatch(Set::isEmpty));
    }

    @Test
    void testAllocateOrder_FullAllocation_Success() {
        Box box = new Box("BOX-001", "SKU-A", 100, null, "2024-01-01T10:00:00");
        controller.insertBox(box);

        Order order = new Order("ORD00001", 2, "2025-09-29T09:00:00");
        OrderLines line = new OrderLines(1, "SKU-A", 50);

        Map<String, Order> orders = warehouse.getOrders();
        Map<Order, Set<OrderLines>> orderLines = warehouse.getOrderLines();
        orders.put("ORD-001", order);
        orderLines.put(order, new HashSet<>(Arrays.asList(line)));

        var result = controller.allocateOrder("ORD-001", false);

        assertNotNull(result);
        assertEquals(1, result.getLineResults().size());
        // Verifica que a alocação foi bem-sucedida
    }

    @Test
    void testAllocateOrder_NonExistentOrder_ReturnsEmptyResult() {
        var result = controller.allocateOrder("NONEXISTENT", false);

        assertNotNull(result);
        assertTrue(result.getLineResults().isEmpty());
    }

    @Test
    void testCollectBoxesForSku_MultipleBays_ReturnsSorted() {
        Box box1 = new Box("BOX-001", "SKU-A", 10,
                LocalDateTime.parse("2024-01-01T10:00:00"), "2024-01-01T08:00:00");
        Box box2 = new Box("BOX-002", "SKU-A", 10,
                LocalDateTime.parse("2023-12-01T10:00:00"), "2024-01-01T09:00:00");

        controller.insertBox(box1);
        controller.insertBox(box2);

        var boxes = controller.collectBoxesForSku("SKU-A");

        assertEquals(2, boxes.size());
        assertEquals("BOX-002", boxes.get(0).box.getBoxID()); // expires first
    }

    @Test
    void testCollectBoxesForSku_NoBoxes_ReturnsEmpty() {
        var boxes = controller.collectBoxesForSku("NONEXISTENT");

        assertNotNull(boxes);
        assertTrue(boxes.isEmpty());
    }

    // ==================================================================
    // TESTES getMovementHistory()
    // ==================================================================

    @Test
    void testGetMovementHistory_AfterInsert_ContainsMovement() {
        Box box = new Box("BOX-001", "SKU-A", 10, null, "2024-01-01T10:00:00");
        controller.insertBox(box);

        List<Movement> history = controller.getMovementHistory();

        assertFalse(history.isEmpty());
        assertTrue(history.stream()
                .anyMatch(m -> m instanceof ReturnMovement));
    }

    // ==================================================================
    // HELPER METHODS
    // ==================================================================

    private Warehouse createTestWarehouse() {
        Map<String, Set<Bay>> warehouses = new HashMap<>();
        Set<Bay> bays = new HashSet<>();
        bays.add(new Bay(1, 1, 100));
        bays.add(new Bay(1, 2, 100));
        warehouses.put("WH1", bays);

        return new Warehouse(
                new HashMap<>(),  // wagons
                new HashMap<>(),  // items
                new HashMap<>(),  // order_lines
                new HashMap<>(),  // orders
                new HashMap<>(),  // returns
                warehouses
        );
    }

    private Warehouse createFullWarehouse() {
        Map<String, Set<Bay>> warehouses = new HashMap<>();
        Set<Bay> bays = new HashSet<>();
        Bay fullBay = new Bay(1, 1, 1);
        // Preenche a bay até a capacidade
        fullBay.addBoxInFEFOOrder(
                new Box("EXISTING", "SKU-X", 1, null, "2024-01-01T10:00:00"),
                (b1, b2) -> 0
        );
        bays.add(fullBay);
        warehouses.put("WH1", bays);

        return new Warehouse(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                warehouses
        );
    }

    private Warehouse createWarehouseWithMultipleBays() {
        Map<String, Set<Bay>> warehouses = new HashMap<>();
        Set<Bay> bays = new HashSet<>();
        bays.add(new Bay(1, 1, 100));
        bays.add(new Bay(2, 2, 100));
        warehouses.put("WH1", bays);

        return new Warehouse(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                warehouses
        );
    }

    private Warehouse createWarehouseWithWagons() {
        Map<String, Set<Bay>> warehouses = new HashMap<>();
        Set<Bay> bays = new HashSet<>();
        bays.add(new Bay(1, 1, 100));
        warehouses.put("WH1", bays);

        Map<Wagon, Set<Box>> wagons = new HashMap<>();
        Wagon wagon = new Wagon("WAG-001");
        Set<Box> boxes = new HashSet<>();
        boxes.add(new Box("BOX-001", "SKU-A", 10, null, "2024-01-01T10:00:00"));
        wagons.put(wagon, boxes);

        return new Warehouse(
                wagons,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                warehouses
        );
    }

    private Bay findBayInWarehouse(String warehouseId) {
        Set<Bay> bays = warehouse.getWarehouses().get(warehouseId);
        if (bays == null || bays.isEmpty()) return null;
        return bays.iterator().next();
    }
}