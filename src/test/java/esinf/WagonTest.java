package esinf;

import org.junit.jupiter.api.Test;
import freightMngt.domain.Wagon;

import static org.junit.jupiter.api.Assertions.*;

public class WagonTest {
    @Test
    void testEqualsAndHashCode() {
        Wagon w1 = new Wagon("W1");
        Wagon w2 = new Wagon("W1");
        Wagon w3 = new Wagon("W2");

        assertEquals(w1, w2);
        assertNotEquals(w1, w3);
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test
    void testGetWagonID() {
        Wagon w = new Wagon("W1");
        assertEquals("W1", w.getWagonID());
    }

    @Test
    void testToString() {
        Wagon w = new Wagon("W1");
        assertTrue(w.toString().contains("W1"));
    }
}
