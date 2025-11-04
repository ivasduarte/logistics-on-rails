package esinf;

import domain.order.Allocation;
import domain.picking.Trolley;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrolleyTest {

    @Test
    void constructorAndGetters() {
        Trolley t = new Trolley(2, 100.0);
        assertAll(
                () -> assertEquals(2, t.getTrolleyNumber()),
                () -> assertEquals(100.0, t.getCapacityWeight(), 1e-6),
                () -> assertEquals(0.0, t.getUsedWeight(), 1e-6),
                () -> assertTrue(t.isEmpty()),
                () -> assertEquals(100.0, t.getRemainingCapacity(), 1e-6),
                () -> assertEquals(0.0, t.getUtilization(), 1e-6)
        );
    }

    @Test
    void addAllocationFits() {
        Trolley t = new Trolley(1, 50.0);
        Allocation a = null;
        assertTrue(t.addAllocation(a, 20.0));
        assertAll(
                () -> assertEquals(20.0, t.getUsedWeight(), 1e-6),
                () -> assertEquals(30.0, t.getRemainingCapacity(), 1e-6),
                () -> assertEquals(1, t.getAllocationCount()),
                () -> assertFalse(t.isEmpty())
        );
    }

    @Test
    void addAllocationTooHeavy() {
        Trolley t = new Trolley(1, 10.0);
        Allocation a = null;
        assertFalse(t.addAllocation(a, 20.0));
        assertAll(
                () -> assertEquals(0, t.getAllocationCount()),
                () -> assertEquals(0.0, t.getUsedWeight(), 1e-6)
        );
    }

    @Test
    void canFitWorks() {
        Trolley t = new Trolley(1, 10.0);
        assertTrue(t.canFit(10.0));
        assertFalse(t.canFit(11.0));
    }

    @Test
    void getAllocationsAndEntriesReturnCopies() {
        Trolley t = new Trolley(1, 100.0);
        Allocation a = null;
        t.addAllocation(a, 5.0);

        List<Allocation> allocations = t.getAllocations();
        List<Trolley.AllocationEntry> entries = t.getAllocationEntries();

        assertEquals(1, allocations.size());
        assertEquals(1, entries.size());

        allocations.clear();
        entries.clear();

        assertEquals(1, t.getAllocationCount());
        assertFalse(t.isEmpty());
    }

    @Test
    void getLogsReturnsCopy() {
        Trolley t = new Trolley(1, 50.0);
        t.addLog("first");
        t.addLog("second");

        List<String> logs = t.getLogs();
        assertEquals(2, logs.size());

        logs.clear();
        assertEquals(2, t.getLogs().size());
    }

    @Test
    void utilizationWithZeroCapacity() {
        Trolley t = new Trolley(1, 0.0);
        assertEquals(0.0, t.getUtilization(), 1e-6);
    }

    @Test
    void settersAndToString() {
        Trolley t = new Trolley(1, 10.0);
        t.setTrolleyNumber(5);
        t.setCapacityWeight(20.0);
        assertAll("setters",
                () -> assertEquals(5, t.getTrolleyNumber()),
                () -> assertEquals(20.0, t.getCapacityWeight(), 1e-6)
        );

        Allocation a = null;
        t.addAllocation(a, 4.0);

        String s = t.toString();
        assertAll(
                () -> assertTrue(s.contains("Trolley 5")),
                () -> assertTrue(s.contains("kg")),
                () -> assertTrue(s.contains("allocation") || s.contains("allocations"))
        );
    }

    @Test
    void zeroWeightAllocationIsAllowedButDoesNotChangeUsedWeight() {
        Trolley t = new Trolley(1, 10.0);
        Allocation a = null;
        assertTrue(t.addAllocation(a, 0.0));
        assertEquals(0.0, t.getUsedWeight(), 1e-6);
        assertEquals(1, t.getAllocationCount());
    }
}
