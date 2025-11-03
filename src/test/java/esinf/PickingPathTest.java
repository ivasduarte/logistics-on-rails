package esinf;

import controller.PickingPathController;
import domain.order.Allocation;
import domain.picking.PickingPlan;
import domain.picking.Trolley;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PickingPathTest {

    private PickingPathController pickingPathController;

    @BeforeEach
    void setUp() {
        PickingPlan plan = new PickingPlan(null, 100.0);

        Trolley trolley = new Trolley(1, 100.0);
        Allocation allocation1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);
        Allocation allocation2 = new Allocation("O2", "B2", "W1", "SKU2", 1, 2, 2, 10);
        trolley.getAllocationEntries().add(new Trolley.AllocationEntry(allocation1, 10, false, 10));
        trolley.getAllocationEntries().add(new Trolley.AllocationEntry(allocation2, 20, false, 20));

        plan.addTrolley(trolley);
        pickingPathController = new PickingPathController(plan);
    }

    @Test
    void testGetTrolleys() {
        List<Trolley> result = pickingPathController.getTrolleys();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetTrolleyAllocations() {
        List<Trolley> trolleys = pickingPathController.getTrolleys();
        List<Allocation> allocations = pickingPathController.getTrolleyAllocations(trolleys.getFirst());
        assertNotNull(allocations);
        assertEquals(2, allocations.size());
    }

    @Test
    void testGetPathAscending() {
        List<Allocation> allocations = new ArrayList<>();
        Allocation allocation1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);
        Allocation allocation2 = new Allocation("O2", "B2", "W1", "SKU2", 1, 2, 2, 10);

        allocations.add(allocation1);
        allocations.add(allocation2);

        List<Allocation> path = pickingPathController.getPathAscending(allocations);
        assertEquals(2, path.size());

        assertEquals(1, path.get(0).getAisle());
        assertEquals(8, path.get(0).getBayNumber());
        assertEquals(2, path.get(1).getAisle());
        assertEquals(2, path.get(1).getBayNumber());
    }

    @Test
    void testGetPathNearest() {
        List<Allocation> allocations = new ArrayList<>();
        Allocation allocation1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);
        Allocation allocation2 = new Allocation("O2", "B2", "W1", "SKU2", 1, 2, 2, 10);

        allocations.add(allocation1);
        allocations.add(allocation2);

        List<Allocation> path = pickingPathController.getPathNearest(allocations);
        assertEquals(2, path.size());

        assertEquals(2, path.get(0).getAisle());
        assertEquals(2, path.get(0).getBayNumber());
        assertEquals(1, path.get(1).getAisle());
        assertEquals(8, path.get(1).getBayNumber());
    }

    @Test
    void testGetPathAscendingDuplicate() {
        List<Allocation> allocations = new ArrayList<>();
        Allocation allocation1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);
        Allocation allocation2 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);

        allocations.add(allocation1);
        allocations.add(allocation2);

        List<Allocation> path = pickingPathController.getPathAscending(allocations);
        assertEquals(1, path.size());
        assertEquals(20, path.getFirst().getQuantityAllocated());
    }

    @Test
    void testGetPathNearestDuplicate() {
        List<Allocation> allocations = new ArrayList<>();
        Allocation allocation1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);
        Allocation allocation2 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);

        allocations.add(allocation1);
        allocations.add(allocation2);

        List<Allocation> path = pickingPathController.getPathNearest(allocations);
        assertEquals(1, path.size());
        assertEquals(20, path.getFirst().getQuantityAllocated());
    }

    @Test
    void testCalculateDistanceAscending() {
        List<Allocation> allocations = new ArrayList<>();
        Allocation allocation1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);
        Allocation allocation2 = new Allocation("O2", "B2", "W1", "SKU2", 1, 2, 2, 10);

        allocations.add(allocation1);
        allocations.add(allocation2);

        int distance = pickingPathController.minimizeDistanceAscending(allocations);
        assertEquals(24, distance);
    }

    @Test
    void testCalculateDistanceNearest() {
        List<Allocation> allocations = new ArrayList<>();
        Allocation allocation1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);
        Allocation allocation2 = new Allocation("O2", "B2", "W1", "SKU2", 1, 2, 2, 10);

        allocations.add(allocation1);
        allocations.add(allocation2);

        int distance = pickingPathController.minimizeDistanceNearest(allocations);
        assertEquals(21, distance);
    }

    @Test
    void testCalculateDistanceAscendingEmpty() {
        List<Allocation> allocations = new ArrayList<>();
        int distance = pickingPathController.minimizeDistanceAscending(allocations);
        assertEquals(0, distance);
    }

    @Test
    void testCalculateDistanceNearestEmpty() {
        List<Allocation> allocations = new ArrayList<>();
        int distance = pickingPathController.minimizeDistanceNearest(allocations);
        assertEquals(0, distance);
    }

    @Test
    void testGetPathNearestWithMultipleAllocations() {
        List<Allocation> allocations = new ArrayList<>();
        Allocation alloc1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 2, 10);
        Allocation alloc2 = new Allocation("O2", "B2", "W1", "SKU2", 1, 1, 10, 10);
        Allocation alloc3 = new Allocation("O3", "B3", "W1", "SKU3", 1, 3, 5, 10);

        allocations.add(alloc2);
        allocations.add(alloc3);
        allocations.add(alloc1);

        List<Allocation> path = pickingPathController.getPathNearest(allocations);
        assertEquals(3, path.size());

        assertEquals(alloc1, path.get(0));
        assertEquals(alloc2, path.get(1));
        assertEquals(alloc3, path.get(2));
    }
}