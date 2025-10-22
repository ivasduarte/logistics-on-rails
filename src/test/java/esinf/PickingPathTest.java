package esinf;

import esinf.us02.Allocation;
import esinf.us03.PickingPlan;
import esinf.us03.Trolley;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PickingPathTest {

    private PickingPath pickingPath;
    private PickingPlan plan;
    private List<Trolley> trolleys;

    @BeforeEach
    void setUp() {
        plan = new PickingPlan(null, 100.0);

        Trolley trolley = new Trolley(1, 100.0);
        Allocation allocation1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);
        Allocation allocation2 = new Allocation("O2", "B2", "W1", "SKU2", 1, 2, 2, 10);
        trolley.getAllocationEntries().add(new Trolley.AllocationEntry(allocation1, 10, false, 10));
        trolley.getAllocationEntries().add(new Trolley.AllocationEntry(allocation2, 20, false, 20));

        plan.addTrolley(trolley);
        pickingPath = new PickingPath(plan);
    }

    @Test
    void testGetTrolleys() {
        List<Trolley> result = pickingPath.getTrolleys();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetTrolleyAllocations() {
        List<Trolley> trolleys = pickingPath.getTrolleys();
        List<Allocation> allocations = pickingPath.getTrolleyAllocations(trolleys.getFirst());
        assertNotNull(allocations);
    }

    @Test
    void testMinimizeDistanceAscending() {
        List<Allocation> allocations = new ArrayList<>();
        Allocation allocation1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);
        Allocation allocation2 = new Allocation("O2", "B2", "W1", "SKU2", 1, 2, 2, 10);

        allocations.add(allocation1);
        allocations.add(allocation2);

        int distance = pickingPath.minimizeDistanceAscending(allocations);
        assertEquals(24, distance);
    }

    @Test
    void testMinimizeDistanceNearest() {
        List<Allocation> allocations = new ArrayList<>();
        Allocation allocation1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);
        Allocation allocation2 = new Allocation("O2", "B2", "W1", "SKU2", 1, 2, 2, 10);

        allocations.add(allocation1);
        allocations.add(allocation2);

        int distance = pickingPath.minimizeDistanceNearest(allocations);
        assertEquals(21, distance);
    }

    @Test
    void testMinimizeDistanceAscendingDuplicate() {
        List<Allocation> allocations = new ArrayList<>();
        Allocation allocation1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);
        Allocation allocation2 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);

        allocations.add(allocation1);
        allocations.add(allocation2);

        int distance = pickingPath.minimizeDistanceAscending(allocations);
        assertEquals(11, distance);
    }

    @Test
    void testMinimizeDistanceNearestDuplicate() {
        List<Allocation> allocations = new ArrayList<>();
        Allocation allocation1 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);
        Allocation allocation2 = new Allocation("O1", "B1", "W1", "SKU1", 1, 1, 8, 10);

        allocations.add(allocation1);
        allocations.add(allocation2);

        int distance = pickingPath.minimizeDistanceNearest(allocations);
        assertEquals(11, distance);
    }
}
