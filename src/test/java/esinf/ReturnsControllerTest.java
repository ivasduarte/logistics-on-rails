package esinf;

import org.junit.jupiter.api.*;
import warehouseMngt.controller.ReturnsController;
import warehouseMngt.domain.Bay;
import warehouseMngt.domain.Warehouse;
import warehouseMngt.operations.StockAction;
import warehouseMngt.returns.ReturnReasons;
import warehouseMngt.returns.Returns;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReturnsControllerTest {

    private ReturnsController controller;
    private Path tempLogFile;

    @BeforeEach
    void setUp() throws IOException {
        tempLogFile = Files.createTempFile("audit", ".log");

        Warehouse warehouse = createTestWarehouse();

        controller = new ReturnsController(warehouse, tempLogFile.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempLogFile);
    }

    @Test
    void testProcessBatchReturns_EmptyList() throws IOException {
        List<ReturnsController.ReturnProcessingResult> results =
                controller.processBatchReturns(Collections.emptyList());
        assertTrue(results.isEmpty(), "Should return empty list for no returns");
    }

    @Test
    void testProcessBatchReturns_RestockedItem() throws IOException {

        Returns item = new Returns("RET00006", "SKU0021", 5, ReturnReasons.REMORSE,
                "2025-09-03T17:00:00", "2025-11-11");

        List<ReturnsController.ReturnProcessingResult> results =
                controller.processBatchReturns(Collections.singletonList(item));

        assertEquals(1, results.size());
        ReturnsController.ReturnProcessingResult result = results.get(0);
        assertEquals("RET00006", result.getReturnId());
        assertEquals("SKU0021", result.getSku());
        assertEquals(5, result.getQuantity());
        assertEquals(ReturnReasons.REMORSE, result.getReason());
        assertEquals(StockAction.RESTOCKED, result.getAction());
    }

    @Test
    void testProcessBatchReturns_DiscardedItem() throws IOException {
        Returns item = new Returns("RET00011", "SKU0002", 17, ReturnReasons.DAMAGED,
                "2025-09-13T16:00:00", "2025-04-08");

        List<ReturnsController.ReturnProcessingResult> results =
                controller.processBatchReturns(Collections.singletonList(item));

        assertEquals(1, results.size());
        ReturnsController.ReturnProcessingResult result = results.get(0);
        assertEquals("RET00011", result.getReturnId());
        assertEquals(StockAction.DISCARDED, result.getAction());
    }

    @Test
    void testProcessBatchReturns_MultipleItems() throws IOException {
        List<Returns> returns = Arrays.asList(
                new Returns("RET-003", "SKU-111", 2, ReturnReasons.REMORSE,
                        "2025-04-17T16:34:15", "2026-01-01"),
                new Returns("RET-004", "SKU-222", 1, ReturnReasons.DAMAGED,
                        "2025-02-01T15:23:10", "2025-11-13"),
                new Returns("RET-005", "SKU-333", 10, ReturnReasons.EXPIRED,
                        "2025-11-13T08:14:04", "2025-03-28")
        );

        List<ReturnsController.ReturnProcessingResult> results =
                controller.processBatchReturns(returns);

        assertEquals(3, results.size());
        results.sort(Comparator.comparing(ReturnsController.ReturnProcessingResult::getReturnId));assertEquals("RET-003", results.get(0).getReturnId());
        assertEquals(StockAction.RESTOCKED, results.get(0).getAction());

        assertEquals("RET-004", results.get(1).getReturnId());
        assertEquals(StockAction.DISCARDED, results.get(1).getAction());

        assertEquals("RET-005", results.get(2).getReturnId());
        assertEquals(StockAction.DISCARDED, results.get(2).getAction());
    }

    @Test
    void testReturnProcessingResult_ToString() {
        ReturnsController.ReturnProcessingResult result =
                new ReturnsController.ReturnProcessingResult(
                        "RET-006", "SKU-999", 7, ReturnReasons.DAMAGED, StockAction.DISCARDED);

        String expected = "RET-006 | SKU: SKU-999 | Qty: 7 | Reason: DAMAGED | Action: DISCARDED";
        assertEquals(expected, result.toString());
    }

    @Test
    void testSaveWarehouse_Integration() {
        Warehouse warehouse = createTestWarehouse();

        assertNotNull(warehouse.getWarehouses());
        assertTrue(warehouse.getWarehouses().containsKey("AISLE1"));

        Set<Bay> baysInAisle1 = warehouse.getWarehouses().get("AISLE1");
        assertEquals(2, baysInAisle1.size());

        Bay firstBay = baysInAisle1.iterator().next();
        assertEquals(1, firstBay.getAisle());
    }

    private Warehouse createTestWarehouse() {
        Warehouse warehouse = new Warehouse();

        Bay bay1 = new Bay(1, 1, 10);
        Bay bay2 = new Bay(2, 1, 10);
        Bay bay3 = new Bay(1, 2, 20);

        Set<Bay> bayAisle = new HashSet<>();
        bayAisle.add(bay1);
        bayAisle.add(bay2);

        Set<Bay> bayAisle2 = new HashSet<>();
        bayAisle2.add(bay3);

        Map<String, Set<Bay>> location = new HashMap<>();
        location.put("AISLE1", bayAisle);
        location.put("AISLE2", bayAisle2);

        Warehouse temp = new Warehouse(null, null, null, null, null, location);

        warehouse.saveWarehouse(temp);

        return warehouse;
    }
}