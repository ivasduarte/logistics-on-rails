package esinf;

import esinf.us02.Allocation;
import esinf.us03.Trolley;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrolleyTest {

    @Test
    void constructorAndGetters() {
        Trolley t = new Trolley(2, 100.0);
        assertEquals(2, t.getTrolleyNumber());
        assertEquals(100.0, t.getCapacityWeight(), 1e-6);
        assertEquals(0.0, t.getUsedWeight(), 1e-6);
        assertTrue(t.isEmpty());
        assertEquals(100.0, t.getRemainingCapacity(), 1e-6);
        assertEquals(0.0, t.getUtilization(), 1e-6);
    }

    @Test
    void addAllocationFits() {
        Trolley t = new Trolley(1, 50.0);
        Allocation a = null;
        assertTrue(t.addAllocation(a, 20.0));
        assertEquals(20.0, t.getUsedWeight(), 1e-6);
        assertEquals(30.0, t.getRemainingCapacity(), 1e-6);
        assertEquals(1, t.getAllocationCount());
        assertFalse(t.isEmpty());
    }

    @Test
    void addAllocationTooHeavy() {
        Trolley t = new Trolley(1, 10.0);
        Allocation a = null;
        assertFalse(t.addAllocation(a, 20.0));
        assertEquals(0, t.getAllocationCount());
        assertEquals(0.0, t.getUsedWeight(), 1e-6);
    }

    @Test
    void canFitWorks() {
        Trolley t = new Trolley(1, 10.0);
        assertTrue(t.canFit(10.0));
        assertFalse(t.canFit(11.0));
    }

    @Test
    void getAllocationsReturnsCopy() {
        Trolley t = new Trolley(1, 100.0);
        Allocation a = null;
        t.addAllocation(a, 5.0);
        List<Allocation> list = t.getAllocations();
        assertEquals(1, list.size());
        list.clear(); // modifica a cópia
        assertEquals(1, t.getAllocationCount(), "modificar a cópia não deve alterar as alocações internas");
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
        assertEquals(5, t.getTrolleyNumber());
        assertEquals(20.0, t.getCapacityWeight(), 1e-6);

        Allocation a = null;
        t.addAllocation(a, 4.0);

        String s = t.toString();
        assertTrue(s.contains("Trolley 5"));
        assertTrue(s.contains("4.0"));
        assertTrue(s.contains("20.0"));
    }
}
