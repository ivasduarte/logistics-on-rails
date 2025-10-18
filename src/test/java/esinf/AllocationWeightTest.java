package esinf;

import esinf.us03.AllocationWeight;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AllocationWeightTest {

    @Test
    void constructorAndGetters_Defaults() {
        AllocationWeight aw = new AllocationWeight(null, 5.0);

        assertAll(
                () -> assertNull(aw.getAllocation()),
                () -> assertEquals(5.0, aw.getWeight(), 1e-6),
                () -> assertFalse(aw.isPartial()),
                () -> assertNull(aw.getOriginalQuantity())
        );
    }

    @Test
    void constructorWithAllFields() {
        AllocationWeight aw = new AllocationWeight(null, 3.5, true, 42);

        assertAll(
                () -> assertNull(aw.getAllocation()),
                () -> assertEquals(3.5, aw.getWeight(), 1e-6),
                () -> assertTrue(aw.isPartial()),
                () -> assertEquals(Integer.valueOf(42), aw.getOriginalQuantity())
        );
    }

    @Test
    void settersWork() {
        AllocationWeight aw = new AllocationWeight(null, 1.0);

        aw.setWeight(7.25);
        aw.setPartial(true);
        aw.setOriginalQuantity(10);
        aw.setAllocation(null);

        assertAll(
                () -> assertEquals(7.25, aw.getWeight(), 1e-6),
                () -> assertTrue(aw.isPartial()),
                () -> assertEquals(Integer.valueOf(10), aw.getOriginalQuantity()),
                () -> assertNull(aw.getAllocation())
        );
    }

    @Test
    void compareTo_SortsByWeightDescending() {
        AllocationWeight a = new AllocationWeight(null, 5.0);
        AllocationWeight b = new AllocationWeight(null, 10.0);
        AllocationWeight c = new AllocationWeight(null, 7.0);

        List<AllocationWeight> list = new ArrayList<>();
        list.add(a);
        list.add(b);
        list.add(c);

        Collections.sort(list);

        assertAll(
                () -> assertEquals(10.0, list.get(0).getWeight(), 1e-6),
                () -> assertEquals(7.0, list.get(1).getWeight(), 1e-6),
                () -> assertEquals(5.0, list.get(2).getWeight(), 1e-6)
        );
    }

    @Test
    void compareTo_EqualWeights() {
        AllocationWeight x = new AllocationWeight(null, 4.0);
        AllocationWeight y = new AllocationWeight(null, 4.0);

        assertEquals(0, x.compareTo(y));
    }
}
